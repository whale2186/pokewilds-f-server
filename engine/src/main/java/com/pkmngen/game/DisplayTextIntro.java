package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;

class DisplayTextIntro extends Action {
   ArrayList<Sprite> spritesNotDrawn;
   ArrayList<Sprite> spritesBeingDrawn;
   Sprite arrowSprite;
   Sprite arrowSprite2;
   Action playSoundAction;
   boolean playSound;
   Action scrollUpAction;
   int charsPerLine = 32;
   int spacing = 6;
   public Action.Layer layer = Action.Layer.gui_110;
   Sprite helperSprite;
   Sprite bgSprite;
   int timer;
   int speedTimer;
   int speed;
   Action triggerAction;
   boolean foundTrigger;
   boolean checkTrigger;
   Vector3 touchLoc = new Vector3();
   Vector2 touchLoc2d = new Vector2();
   boolean firstStep = true;
   boolean exitWhenDone = true;
   boolean waitingOnExit = false;

   public DisplayTextIntro(Game game, String textString, String playSound, Action triggerAction, boolean exitWhenDone, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.exitWhenDone = exitWhenDone;
      this.triggerAction = triggerAction;
      this.foundTrigger = false;
      this.checkTrigger = false;
      this.spritesNotDrawn = new ArrayList<>();
      this.spritesBeingDrawn = new ArrayList<>();
      if (playSound != null) {
         this.playSoundAction = new DisplayTextIntro.PlaySoundText(playSound, null);
         this.playSound = true;
      } else {
         this.playSound = false;
      }

      this.speed = 3;
      this.speedTimer = this.speed;
      String line = "";
      String lines = "";
      String[] words = textString.split(" ");

      for (String word : words) {
         if (line.length() + word.length() < 18) {
            line = line + word;
            if (line.length() != 17) {
               line = line + " ";
            }
         } else {
            while (line.length() < 18) {
               line = line + " ";
            }

            lines = lines + line;
            line = word + " ";
         }
      }

      lines = lines + line;
      char[] textArray = lines.toCharArray();
      int i = 0;
      int j = 0;

      for (char letter : textArray) {
         Sprite letterSprite = game.textDict.get(letter);
         if (letterSprite == null) {
            letterSprite = game.textDict.get(null);
         }

         Sprite currSprite = new Sprite(letterSprite);
         currSprite.setPosition(10 + 8 * i + 2 - 4, 26 - 16 * j + 2 - 4);
         this.spritesNotDrawn.add(currSprite);
         if (i >= 17) {
            i = 0;
            j++;
         } else {
            i++;
         }
      }

      Texture text = TextureCache.get(Gdx.files.internal("arrow_down.png"));
      this.arrowSprite = new Sprite(text, 0, 0, 7, 5);
      this.arrowSprite.setPosition(144.0F, 10.0F);
      this.timer = 0;
      text = TextureCache.get(Gdx.files.internal("textbox_bg1.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
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
      }

      this.bgSprite.draw(game.uiBatch);

      for (Sprite sprite : this.spritesBeingDrawn) {
         sprite.draw(game.uiBatch);
      }

      if (!this.waitingOnExit) {
         if (!game.actionStack.contains(this.scrollUpAction)) {
            if (!game.actionStack.contains(this.playSoundAction)) {
               if (this.checkTrigger) {
                  if (game.actionStack.contains(this.triggerAction)) {
                     this.foundTrigger = true;
                  } else if (this.foundTrigger) {
                     game.actionStack.remove(this);
                  }
               } else if (this.spritesBeingDrawn.size() < 36 && !this.spritesNotDrawn.isEmpty()) {
                  if (this.speedTimer > 0) {
                     this.speedTimer--;
                  } else {
                     this.speedTimer = this.speed;

                     for (int i = 0; i < 1 && !this.spritesNotDrawn.isEmpty() && this.spritesBeingDrawn.size() < 36; i++) {
                        this.spritesBeingDrawn.add(this.spritesNotDrawn.remove(0));
                     }
                  }
               } else if (this.playSound && this.spritesNotDrawn.isEmpty()) {
                  game.insertAction(this.playSoundAction);
                  this.playSoundAction.step(game);
                  this.playSound = false;
               } else if (this.triggerAction != null) {
                  game.insertAction(this.nextAction);
                  this.checkTrigger = true;
               } else {
                  if (this.spritesNotDrawn.isEmpty()) {
                     game.insertAction(this.nextAction);
                     if (this.exitWhenDone) {
                        game.actionStack.remove(this);
                     } else {
                        this.waitingOnExit = true;
                     }
                  } else {
                     this.scrollUpAction = new DisplayTextIntro.ScrollTextUp(game, this.spritesBeingDrawn, this.spritesNotDrawn);
                     game.insertAction(this.scrollUpAction);
                  }
               }
            }
         }
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
         if (!sound.contains("cries")) {
            path = "sounds/";
         } else {
            volume = 0.5F;
         }

         this.music = AudioLoader.loadMusic(path + sound);
         this.music.setLooping(false);
         this.music.setVolume(volume);
      }

      @Override
      public void step(Game game) {
         if (this.music != null && !this.playedYet) {
            this.music.play();
            this.playedYet = true;
         }

         if (!this.music.isPlaying()) {
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
      public Action.Layer layer = Action.Layer.gui_110;

      public ScrollTextUp(Game game, ArrayList<Sprite> text, ArrayList<Sprite> otherText) {
         super();
         this.text = text;
         this.otherText = otherText;
         this.positions = new ArrayList<>();

         for (int i = 0; i < 5; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.positions.add(new Vector2(0.0F, 8.0F));

         for (int i = 0; i < 5; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.positions.add(new Vector2(0.0F, 8.0F));
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
         this.position = this.positions.get(0);
         this.positions.remove(0);

         for (Sprite sprite : this.text) {
            sprite.setPosition(sprite.getX() + this.position.x, sprite.getY() + this.position.y);
         }

         for (Sprite sprite : this.otherText) {
            sprite.setPosition(sprite.getX() + this.position.x, sprite.getY() + this.position.y);
         }

         if (this.positions.isEmpty()) {
            for (int i = 0; i < 18; i++) {
               this.text.remove(0);
            }

            game.actionStack.remove(this);
         }
      }
   }
}
