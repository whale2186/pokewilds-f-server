package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.regex.Pattern;

class DisplayText extends Action {
   public static boolean textPersist = false;
   ArrayList<Sprite> spritesNotDrawn;
   ArrayList<Sprite> spritesBeingDrawn;
   Sprite arrowSprite;
   Action playSoundAction;
   boolean playSound;
   Action scrollUpAction;
   public Action.Layer layer = Action.Layer.gui_105;
   Sprite helperSprite;
   Sprite bgSprite;
   int timer;
   int speedTimer;
   int speed;
   Action triggerAction;
   boolean foundTrigger;
   boolean checkTrigger;
   boolean persist = false;
   boolean waitInput = true;
   public boolean braillify = false;
   boolean firstStep;
   public static boolean unownText = false;
   Texture unownTiles;
   public static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

   private static String stripDiacritics(String str) {
      str = Normalizer.normalize(str, Form.NFD);
      return DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
   }

   public DisplayText(Game game, String textString, String playSound, Action triggerAction, Action nextAction) {
      this(game, textString, playSound, triggerAction, false, nextAction);
   }

   public DisplayText(Game game, String textString, String playSound, Action triggerAction, boolean braillify, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.firstStep = true;
      this.triggerAction = triggerAction;
      this.foundTrigger = false;
      this.checkTrigger = false;
      this.braillify = braillify;
      this.spritesNotDrawn = new ArrayList<>();
      this.spritesBeingDrawn = new ArrayList<>();
      if (playSound != null) {
         this.playSoundAction = new DisplayText.PlaySoundText(playSound, null);
         this.playSound = true;
      } else {
         this.playSound = false;
      }

      this.speed = Game.textSpeed.framesToWait;
      this.speedTimer = this.speed;
      String line = "";
      String lines = "";
      String[] words = textString.split(" ");

      for (String word : words) {
         if (line.length() + word.length() < 19) {
            line = line + word;
            if (line.length() != 18) {
               line = line + " ";
            }
         } else {
            while (line.length() < 18) {
               line = line + " ";
            }

            lines = lines + line;
            if (this.braillify) {
               lines = lines + line;
            }

            line = word + " ";
         }
      }

      if (!this.braillify) {
         lines = lines + line;
      } else {
         while (line.length() < 18) {
            line = line + " ";
         }

         lines = lines + line;
         lines = lines + line;
      }

      char[] textArray = lines.toCharArray();
      this.unownTiles = TextureCache.get(Gdx.files.internal("unown_font.png"));
      int i = 0;
      int j = 0;

      for (char letter : textArray) {
         boolean skipLetter = false;
         Sprite letterSprite;
         if (!unownText) {
            letterSprite = game.textDict.get(letter);
            if (letterSprite == null) {
               letterSprite = game.textDict.get(null);
            }
         } else {
            letter = stripDiacritics(Character.toString(letter)).charAt(0);
            if (letter == '!') {
               letterSprite = new Sprite(this.unownTiles, 260, 0, 10, 9);
            } else if (letter == '?') {
               letterSprite = new Sprite(this.unownTiles, 270, 0, 10, 9);
            } else if (Character.isAlphabetic(letter)) {
               if (Character.isLowerCase(letter)) {
                  letterSprite = new Sprite(this.unownTiles, 10 * (letter - 'a'), 0, 10, 9);
               } else {
                  letterSprite = new Sprite(this.unownTiles, 10 * (letter - 'A'), 0, 10, 9);
               }
            } else if (letter == ' ') {
               letterSprite = game.textDict.get(' ');
            } else {
               letterSprite = game.textDict.get(null);
            }
         }

         Sprite currSprite = new Sprite(letterSprite);
         if (this.braillify && j % 2 == 0) {
            letterSprite = game.brailleDict.get(letter);
            if (letterSprite == null) {
               letterSprite = game.textDict.get(' ');
            }

            currSprite = new Sprite(letterSprite);
         }

         if (!skipLetter) {
            currSprite.setPosition(10 + 8 * i + 2 - 4, 26 - 16 * j + 2 - 4);
            this.spritesNotDrawn.add(currSprite);
            if (i >= 17) {
               i = 0;
               j++;
            } else {
               i++;
            }
         }
      }

      Texture text = TextureCache.get(Gdx.files.internal("arrow_down.png"));
      this.arrowSprite = new Sprite(text, 0, 0, 7, 5);
      this.arrowSprite.setPosition(144.0F, 10.0F);
      this.timer = 0;
      text = TextureCache.get(Gdx.files.internal("textbox_bg1.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
   }

   public DisplayText(Game game, String textString, String playSound, boolean textPersist, Action nextAction) {
      this(game, textString, playSound, textPersist, true, nextAction);
   }

   public DisplayText(Game game, String textString, String playSound, boolean textPersist, boolean waitInput, boolean braillify, Action nextAction) {
      this(game, textString, playSound, null, braillify, nextAction);
      this.persist = textPersist;
      this.waitInput = waitInput;
   }

   public DisplayText(Game game, String textString, String playSound, boolean textPersist, boolean waitInput, Action nextAction) {
      this(game, textString, playSound, textPersist, waitInput, false, nextAction);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (this.firstStep) {
         game.displayTextAction = this;
         this.firstStep = false;
         if (this.persist) {
            textPersist = true;
         }
      }

      if (!game.uiBatch.isDrawing()) {
         game.uiBatch.begin();
      }

      this.bgSprite.draw(game.uiBatch);

      for (Sprite sprite : this.spritesBeingDrawn) {
         if (!(sprite.getY() > 38.0F) && !(sprite.getY() < 6.0F)) {
            sprite.draw(game.uiBatch);
         }
      }

      if (!game.actionStack.contains(this.scrollUpAction)) {
         if (!game.actionStack.contains(this.playSoundAction)) {
            if (!this.checkTrigger && (!this.persist || textPersist)) {
               if (this.spritesBeingDrawn.size() < 37 && !this.spritesNotDrawn.isEmpty()) {
                  if (this.speedTimer > 0) {
                     this.speedTimer--;
                  } else {
                     this.speedTimer = this.speed;
                     if (this.speed == 0) {
                        this.extract(5);
                     } else {
                        if (!InputProcessor.aPressed && !InputProcessor.bPressed) {
                           this.extract(1);
                        } else {
                           this.extract(3);
                        }
                     }
                  }
               } else if (this.playSound && this.spritesNotDrawn.isEmpty()) {
                  game.insertAction(this.playSoundAction);
                  this.playSoundAction.step(game);
                  this.playSound = false;
               } else if (this.triggerAction == null && (!this.spritesNotDrawn.isEmpty() || !textPersist || this.waitInput)) {
                  if (this.timer <= 0) {
                     if (this.timer <= -35) {
                        this.timer = 33;
                     } else {
                        this.arrowSprite.draw(game.uiBatch);
                     }
                  }

                  this.timer--;
                  if (InputProcessor.aJustPressed) {
                     Action playSound = new PlayMusic("click1", null);
                     game.insertAction(playSound);
                     playSound.step(game);
                     if (this.spritesNotDrawn.isEmpty()) {
                        if (textPersist) {
                           game.insertAction(this.nextAction);
                           this.checkTrigger = true;
                           return;
                        }

                        game.insertAction(this.nextAction);
                        game.actionStack.remove(this);
                     } else {
                        this.scrollUpAction = new DisplayText.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn, this.braillify);
                        game.insertAction(this.scrollUpAction);
                     }
                  }
               } else {
                  game.insertAction(this.nextAction);
                  this.checkTrigger = true;
               }
            } else if (this.triggerAction == null && !textPersist) {
               game.actionStack.remove(this);
            } else if (game.actionStack.contains(this.triggerAction)) {
               this.foundTrigger = true;
            } else if (this.foundTrigger) {
               game.actionStack.remove(this);
            }
         }
      }
   }

   private void extract(int amount) {
      for (int i = 0; i < amount && !this.spritesNotDrawn.isEmpty(); i++) {
         this.spritesBeingDrawn.add(this.spritesNotDrawn.remove(0));
         if (this.braillify && !this.spritesNotDrawn.isEmpty() && 35 - this.spritesBeingDrawn.size() >= 0) {
            this.spritesBeingDrawn.add(this.spritesNotDrawn.remove(35 - this.spritesBeingDrawn.size()));
         }
      }
   }

   static class Clear extends Action {
      public Clear(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         DisplayText.textPersist = false;
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   class PlaySoundText extends Action {
      Music music;
      float initialVolume;
      boolean playedYet;

      public PlaySoundText(String sound, Action nextAction) {
         super();
         this.nextAction = nextAction;
         this.playedYet = false;
         String path = "";
         float volume = 1.0F;
         if (sound.contains("cries")) {
            volume = 0.5F;
         } else if (!sound.contains("attacks")) {
            path = "sounds/";
         }

         this.music = AudioLoader.loadMusic(path + sound);
         this.music.setLooping(false);
         this.music.setVolume(volume);
      }

      @Override
      public void step(Game game) {
         if (this.music != null && !this.playedYet) {
            this.initialVolume = game.currMusic.getVolume();
            game.currMusic.setVolume(0.0F);
            this.music.play();
            this.playedYet = true;
         }

         if (!this.music.isPlaying()) {
            game.currMusic.setVolume(this.initialVolume);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }

   class ScrollTextUp extends Action {
      ArrayList<Vector2> positions;
      Vector2 position;
      ArrayList<Sprite> text;
      ArrayList<Sprite> otherText;
      boolean braillify = false;
      int numRemove = 18;
      public Action.Layer layer = Action.Layer.gui_110;

      public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
         this(game, text, otherText, false);
      }

      public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText, boolean braillify) {
         super();
         this.text = text;
         this.otherText = otherText;
         this.braillify = braillify;
         this.positions = new ArrayList<>();

         for (int i = 0; i < 5; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.positions.add(new Vector2(0.0F, 8.0F));

         for (int i = 0; i < 5; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.positions.add(new Vector2(0.0F, 8.0F));
         if (this.braillify) {
            this.numRemove = 36;
         }
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         this.position = this.positions.remove(0);

         for (Sprite sprite : this.text) {
            sprite.setPosition(sprite.getX() + this.position.x, sprite.getY() + this.position.y);
         }

         for (Sprite sprite : this.otherText) {
            sprite.setPosition(sprite.getX() + this.position.x, sprite.getY() + this.position.y);
         }

         if (this.positions.isEmpty()) {
            if (this.braillify) {
               this.braillify = false;

               for (int i = 0; i < 5; i++) {
                  this.positions.add(new Vector2(0.0F, 0.0F));
               }

               this.positions.add(new Vector2(0.0F, 8.0F));

               for (int i = 0; i < 5; i++) {
                  this.positions.add(new Vector2(0.0F, 0.0F));
               }

               this.positions.add(new Vector2(0.0F, 8.0F));
            } else {
               for (int i = 0; i < this.numRemove; i++) {
                  this.text.remove(0);
               }

               game.actionStack.remove(this);
            }
         }
      }
   }
}
