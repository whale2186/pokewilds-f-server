package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class DrawPlayerMenu extends Menu {
   public static int lastIndex = 0;
   Sprite arrow;
   Sprite arrowWhite;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_108;
   Map<Integer, Vector2> arrowCoords;
   Vector2 newPos;
   Sprite helperSprite;
   ArrayList<ArrayList<Sprite>> spritesToDraw;
   int cursorDelay;
   String[] entries;

   public DrawPlayerMenu(Game game, Action nextAction) {
      this.disabled = false;
      this.drawArrowWhite = false;
      this.nextAction = nextAction;
      this.cursorDelay = 0;
      this.arrowCoords = new HashMap<>();
      this.spritesToDraw = new ArrayList<>();
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("menu/menu6.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      this.arrowCoords.put(0, new Vector2(89.0F, 120.0F));
      this.arrowCoords.put(1, new Vector2(89.0F, 104.0F));
      this.arrowCoords.put(2, new Vector2(89.0F, 88.0F));
      this.arrowCoords.put(3, new Vector2(89.0F, 72.0F));
      this.arrowCoords.put(4, new Vector2(89.0F, 56.0F));
      this.currIndex = lastIndex;
      this.newPos = this.arrowCoords.get(this.currIndex);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.entries = new String[]{"POKéMON", "ITEM", "MAP", "GUIDE", "SAVE"};

      for (String entry : this.entries) {
         char[] textArray = entry.toCharArray();
         int i = 0;
         int j = 0;
         ArrayList<Sprite> word = new ArrayList<>();

         for (char letter : textArray) {
            Sprite letterSprite = game.textDict.get(letter);
            if (letterSprite == null) {
               letterSprite = game.textDict.get(null);
            }

            Sprite currSprite = new Sprite(letterSprite);
            currSprite.setPosition(96 + 8 * i, 120 - 8 * j);
            word.add(currSprite);
            if (i >= 17) {
               i = 0;
               j++;
            } else {
               i++;
            }
         }

         this.spritesToDraw.add(word);
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
      game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());
      int j = 0;

      for (ArrayList<Sprite> word : this.spritesToDraw) {
         for (Sprite sprite : word) {
            game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j * 16);
         }

         j++;
      }

      if (this.disabled) {
         if (this.drawArrowWhite) {
            this.arrowWhite.setPosition(this.newPos.x, this.newPos.y);
            this.arrowWhite.draw(game.uiBatch);
         }
      } else {
         if (InputProcessor.upJustPressed) {
            if (this.currIndex > 0) {
               this.currIndex--;
               this.newPos = this.arrowCoords.get(this.currIndex);
            }
         } else if (InputProcessor.downJustPressed && this.currIndex < 4) {
            this.currIndex++;
            this.newPos = this.arrowCoords.get(this.currIndex);
         }

         if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            game.insertAction(new PlayMusic("click1", null));
            this.disabled = true;
            String currEntry = this.entries[this.currIndex];
            if (currEntry.equals("POKéMON")) {
               game.insertAction(new DrawPokemonMenu.Intro(new DrawPokemonMenu(game, this)));
            } else if (currEntry.equals("ITEM")) {
               DrawItemMenuGen2 menu = new DrawItemMenuGen2(game, this);
               menu.disabled = true;
               game.insertAction(
                  new DrawItemMenu.Intro(this, 6, new DrawWhiteScreen(18, new DrawItemMenu.Intro(menu, 9, new RunCode(() -> menu.disabled = false, menu))))
               );
            } else if (currEntry.equals("MAP")) {
               game.insertAction(new DrawMiniMap.Intro(this, 9, new DrawMiniMap(game, this)));
            } else if (currEntry.equals("GUIDE")) {
               game.insertAction(new DrawItemMenu.Intro(this, 9, new DrawItemMenu(game, true, this)));
            } else if (currEntry.equals("SAVE")) {
               game.map.saveToFileNew(game);
               game.insertAction(
                  new DisplayText(
                     game,
                     game.player.name.toUpperCase(Locale.ROOT) + " saved the game!",
                     "save1.ogg",
                     null,
                     new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, new SetField(game.player, "canMove", true, null)))
                  )
               );
            }
         } else if (InputProcessor.bJustPressed || InputProcessor.startJustPressed) {
            lastIndex = this.currIndex;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            game.insertAction(new PlayMusic("click1", null));
         }

         if (this.cursorDelay >= 2) {
            game.uiBatch.draw(this.arrow, this.newPos.x, this.newPos.y);
         } else {
            this.cursorDelay++;
         }
      }
   }

   public static class Intro extends Action {
      ArrayList<Sprite> sprites;
      Sprite sprite;
      ArrayList<Integer> repeats;
      ArrayList<String> sounds;
      String sound;
      public Action.Layer layer = Action.Layer.gui_120;
      Sprite helperSprite;

      public Intro(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("menu/menu6.png"));
         this.sprites = new ArrayList<>();
         this.sprite = new Sprite(text, 0, 0, 160, 144);
         this.repeats = new ArrayList<>();
         this.repeats.add(17);
         this.sounds = new ArrayList<>();
         this.sounds.add(null);
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
         game.uiBatch.draw(this.sprite, this.sprite.getX(), this.sprite.getY());
         if (this.repeats.isEmpty()) {
            game.insertAction(new PlayMusic("menu_open1", null));
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         } else {
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
               game.insertAction(new PlayMusic("menu_open1", null));
               this.sounds.set(0, null);
            }

            if (this.repeats.get(0) > 1) {
               this.repeats.set(0, this.repeats.get(0) - 1);
            } else {
               this.repeats.remove(0);
               this.sounds.remove(0);
            }
         }
      }
   }
}
