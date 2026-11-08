package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

class DrawItemMenu extends Menu {
   public static int lastCurrIndex = 0;
   public static int lastCursorPos = 0;
   Sprite arrow;
   Sprite arrowWhite;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_106;
   Map<Integer, Vector2> arrowCoords;
   Vector2 newPos;
   Sprite helperSprite;
   ArrayList<ArrayList<Sprite>> spritesToDraw = new ArrayList<>();
   int cursorDelay;
   int cursorPos;
   int currIndex;
   ArrayList<String> itemsList;
   Sprite downArrow;
   int downArrowTimer;
   boolean refresh = false;
   public int upTimer = 0;
   public int downTimer = 0;
   public boolean isGuideMenu = false;
   public static int lastCurrIndexGuide = 0;
   public static int lastCursorPosGuide = 0;
   public boolean isSorting = false;
   public int sortingIndex = 0;

   public DrawItemMenu(Game game, Menu prevMenu) {
      this(game, false, prevMenu);
   }

   public DrawItemMenu(Game game, boolean isGuideMenu, Menu prevMenu) {
      this.isGuideMenu = isGuideMenu;
      this.prevMenu = prevMenu;
      this.disabled = false;
      this.cursorDelay = 0;
      this.arrowCoords = new HashMap<>();
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("menu/item_menu1.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      text = TextureCache.get(Gdx.files.internal("arrow_down.png"));
      this.downArrow = new Sprite(text, 0, 0, 7, 5);
      this.downArrow.setPosition(144.0F, 50.0F);
      this.downArrowTimer = 0;
      this.currIndex = lastCurrIndex;
      this.cursorPos = lastCursorPos;
      this.arrowCoords.put(0, new Vector2(41.0F, 104.0F));
      this.arrowCoords.put(1, new Vector2(41.0F, 88.0F));
      this.arrowCoords.put(2, new Vector2(41.0F, 72.0F));
      this.arrowCoords.put(3, new Vector2(41.0F, 56.0F));
      this.newPos = this.arrowCoords.get(this.cursorPos % 4);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
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
   public void firstStep(Game game) {
      if (this.isGuideMenu) {
         this.itemsList = new ArrayList<>(DrawItemMenu.DrawGuideText.entries);
      } else {
         this.itemsList = new ArrayList<>(game.player.getItemsDict().keySet());
      }

      this.itemsList.add("Cancel");
      if (this.currIndex + this.cursorPos > this.itemsList.size()) {
         if (this.currIndex > 0) {
            this.currIndex--;
         } else if (this.cursorPos > 0) {
            this.cursorPos--;
         }
      }

      this.spritesToDraw.clear();

      for (String entry : this.itemsList) {
         String text = entry;
         if (text.toLowerCase(Locale.ROOT).contains("apricorn")) {
            text = text.toLowerCase(Locale.ROOT).replace("apricorn", "aprcn");
         }

         char[] textArray = text.toUpperCase(Locale.ROOT).toCharArray();
         int i = 0;
         ArrayList<Sprite> word = new ArrayList<>();

         for (char letter : textArray) {
            Sprite letterSprite = game.textDict.get(letter);
            if (letterSprite == null) {
               letterSprite = game.textDict.get(null);
            }

            Sprite currSprite = new Sprite(letterSprite);
            currSprite.setPosition(48 + 8 * i, 104.0F);
            word.add(currSprite);
            i++;
         }

         if (game.player.hasItem(entry)) {
            char[] numItemsArray = String.format(Locale.ROOT, "%02d", game.player.getItemAmount(entry)).toCharArray();
            i = 10;

            for (char letter : numItemsArray) {
               if (letter != '0' || ++i != 11) {
                  Sprite letterSprite = game.textDict.get(letter);
                  Sprite currSprite = new Sprite(letterSprite);
                  currSprite.setPosition(48 + 8 * i, 104.0F);
                  word.add(currSprite);
               }
            }
         }

         this.spritesToDraw.add(word);
      }
   }

   @Override
   public void step(Game game) {
      if (this.prevMenu != null) {
         this.prevMenu.step(game);
      }

      game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());
      int j = 0;

      for (int i = 0; i < this.spritesToDraw.size(); i++) {
         if (i >= this.currIndex && i < this.currIndex + 4) {
            for (Sprite sprite : this.spritesToDraw.get(i)) {
               game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j * 16);
            }

            j++;
         }
      }

      if (this.disabled) {
         this.refresh = true;
         game.uiBatch.draw(this.arrowWhite, this.newPos.x, this.newPos.y);
      } else {
         if (this.refresh) {
            this.firstStep(game);
            this.refresh = false;
         }

         if (InputProcessor.upPressed) {
            if (this.upTimer < 20) {
               this.upTimer++;
            }
         } else if (InputProcessor.downPressed) {
            if (this.downTimer < 20) {
               this.downTimer++;
            }
         } else {
            this.upTimer = 0;
            this.downTimer = 0;
         }

         if (!InputProcessor.upJustPressed && this.upTimer < 20) {
            if (InputProcessor.downJustPressed || this.downTimer >= 20) {
               if (this.cursorPos < 2 && this.currIndex + this.cursorPos + 1 < this.itemsList.size()) {
                  this.cursorPos++;
                  this.newPos = this.arrowCoords.get(this.cursorPos);
               } else if (this.currIndex < this.itemsList.size() - 3) {
                  this.currIndex++;
               }
            }
         } else if (this.cursorPos > 0) {
            this.cursorPos--;
            this.newPos = this.arrowCoords.get(this.cursorPos);
         } else if (this.currIndex > 0) {
            this.currIndex--;
         }

         if (this.cursorDelay >= 5) {
            game.uiBatch.draw(this.arrow, this.newPos.x, this.newPos.y);
         } else {
            this.cursorDelay++;
         }

         if (this.isSorting) {
            int index = this.sortingIndex - this.currIndex;
            if (index >= 0 && index < this.arrowCoords.size()) {
               Vector2 pos = this.arrowCoords.get(index);
               game.uiBatch.draw(this.arrowWhite, pos.x, pos.y);
            }
         }

         if (this.itemsList.size() - this.currIndex > 4) {
            if (this.downArrowTimer < 22) {
               game.uiBatch.draw(this.downArrow, this.downArrow.getX(), this.downArrow.getY());
            }

            this.downArrowTimer++;
         } else {
            this.downArrowTimer = 0;
         }

         if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
         }

         if (InputProcessor.aJustPressed) {
            if (this.isSorting) {
               String fromItem = this.itemsList.get(this.sortingIndex);
               String toItem = this.itemsList.get(this.currIndex + this.cursorPos);
               LinkedHashMap<String, Integer> newMap = new LinkedHashMap<>();

               for (String key : game.player.getItemsDict().keySet()) {
                  if (key.equals(fromItem)) {
                     newMap.put(toItem, game.player.getItemAmount(toItem));
                  } else if (key.equals(toItem)) {
                     newMap.put(fromItem, game.player.getItemAmount(fromItem));
                  } else {
                     newMap.put(key, game.player.getItemAmount(key));
                  }
               }

               game.player.getItemsDict().clear();
               game.player.setPlayerItems(newMap);
               this.isSorting = false;
               this.firstStep(game);
            } else {
               game.insertAction(new PlayMusic("click1", null));
               String name = this.itemsList.get(this.currIndex + this.cursorPos);
               if (this.isGuideMenu) {
                  lastCurrIndexGuide = this.currIndex;
                  lastCursorPosGuide = this.cursorPos;
               } else {
                  lastCurrIndex = this.currIndex;
                  lastCursorPos = this.cursorPos;
               }

               if ("Cancel".equals(name)) {
                  this.prevMenu.disabled = false;
                  game.insertAction(this.prevMenu);
                  game.actionStack.remove(this);
               } else {
                  this.disabled = true;
                  game.actionStack.remove(this);
                  if (this.isGuideMenu) {
                     game.insertAction(new DrawWhiteScreen(30, new DrawItemMenu.DrawGuideText(this.currIndex + this.cursorPos, this)));
                  } else {
                     game.insertAction(new DrawUseTossMenu(game, this, name));
                  }
               }
            }
         } else if (InputProcessor.bJustPressed) {
            if (this.isSorting) {
               this.isSorting = false;
            }

            if (this.isGuideMenu) {
               lastCurrIndexGuide = this.currIndex;
               lastCursorPosGuide = this.cursorPos;
            } else {
               lastCurrIndex = this.currIndex;
               lastCursorPos = this.cursorPos;
            }

            this.prevMenu.disabled = false;
            game.actionStack.remove(this);
            game.insertAction(this.prevMenu);
            game.insertAction(new PlayMusic("click1", null));
         } else {
            if (InputProcessor.startJustPressed && !this.isGuideMenu) {
               this.isSorting = true;
               this.sortingIndex = this.currIndex + this.cursorPos;
            }
         }
      }
   }

   static class DrawGuideText extends Menu {
      Sprite bgSprite;
      Color prevColor = new Color();
      HashMap<Integer, Character> alphanumericKeys = new HashMap<>();
      HashMap<Integer, Character> alphanumericKeysShift = new HashMap<>();
      ArrayList<Character> text = new ArrayList<>();
      int index = 0;
      public static ArrayList<String> texts = new ArrayList<>();
      public static ArrayList<String> entries = new ArrayList<>();
      int backspaceTimer = 0;
      public boolean done = false;

      public DrawGuideText(int index, Menu prevMenu) {
         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
         this.bgSprite = new Sprite(text, 0, 0, 160, 144);
         char[] textArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

         for (int i = 0; i < textArray.length; i++) {
            this.alphanumericKeys.put(Keys.valueOf(String.valueOf(textArray[i])), String.valueOf(textArray[i]).toLowerCase(Locale.ROOT).charAt(0));
            this.alphanumericKeysShift.put(Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
         }

         textArray = "1234567890".toCharArray();

         for (int i = 0; i < textArray.length; i++) {
            this.alphanumericKeys.put(Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
            this.alphanumericKeysShift.put(Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
         }

         this.alphanumericKeys.put(56, '.');
         this.alphanumericKeysShift.put(76, '?');
         this.alphanumericKeysShift.put(8, '!');
         this.index = index;
         this.prevMenu = prevMenu;
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.gui_106;
      }

      @Override
      public void firstStep(Game game) {
         this.text.clear();

         for (char c : texts.get(this.index).toCharArray()) {
            this.text.add(c);
         }
      }

      @Override
      public void step(Game game) {
         this.prevColor.set(game.mapBatch.getColor());
         game.uiBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         game.uiBatch.draw(this.bgSprite, 0.0F, 0.0F);
         game.uiBatch.setColor(this.prevColor);
         int i = 0;
         int j = 0;

         for (int k = 0; k < this.text.size(); k++) {
            char character = this.text.get(k);
            if (character == ' ' && k + 1 < this.text.size()) {
               int length = 1;

               for (char nextChar = this.text.get(k + length); nextChar != ' ' && k + length < this.text.size(); length++) {
                  nextChar = this.text.get(k + length);
               }

               if (i + length > 19) {
                  i = -1;
                  j++;
               }
            }

            Sprite letterSprite = game.textDict.get(character);
            letterSprite.setPosition(8 * i + 8, 128 - 16 * j);
            letterSprite.draw(game.uiBatch, 1.0F);
            if (++i > 19) {
               i = 0;
               j++;
            }
         }

         if (this.done) {
            game.actionStack.remove(this);
         }

         if (!this.disabled) {
            if (InputProcessor.bJustPressed) {
               game.actionStack.remove(this);
               game.insertAction(new DrawWhiteScreen(30, new SetField(this.prevMenu, "disabled", false, this.prevMenu)));
            } else if (InputProcessor.upJustPressed) {
               this.firstStep = true;
               this.index = Math.floorMod(this.index - 1, texts.size());
               game.actionStack.remove(this);
               game.insertAction(new PlayMusic("menu_switch2", null));
               game.insertAction(new DrawWhiteScreen(this));
            } else if (InputProcessor.downJustPressed) {
               this.firstStep = true;
               this.index = Math.floorMod(this.index + 1, texts.size());
               game.actionStack.remove(this);
               game.insertAction(new PlayMusic("menu_switch2", null));
               game.insertAction(new DrawWhiteScreen(this));
            }
         }
      }

      static {
         texts.add("Stand still while holding X to stop using a Field Move.");
         texts.add("You can craft items at a campfire. Craft POKéBALLS using Apricorns.");
         texts.add("If you white out during battle, you will return to the last place you used a sleeping bag. ");
         texts.add("Ghosts may appear in the woods at night. A campfire will ward them off. ");
         texts.add("Grass types know CUT. Use CUT on trees and tall grass to get building materials. ");
         texts.add("Fighting-types know BUILD. Use CUT buildings to remove them.");
         texts.add("POKéMON will lay EGGS when located near a compatible mate. ");
         texts.add("Build fences to prevent your POKéMON from wandering off when you let them out of their POKéBALL. ");
         texts.add("Your POKéMON will be happier if fenced in and located near a shelter. ");
         texts.add("POKéMON are happier when located in their natural habitat. If they are happy enough, they may give you items! ");
         texts.add("Be wary of taking EGGS near wild POKéMON. Angry parents may attack! ");
         texts.add("You can build a door between two house tiles to build a back or side door to your house. ");
         texts.add("Sleeping indoors will restore hp twice as fast as sleeping outdoors. ");
         texts.add("Sleeping in a bed will over time cure your POKéMON' status conditions.");
         texts.add("Using your sleeping bag will slowly restore your party' hp. ");
         texts.add("Use the escape rope to return to the nearest location on the shore. ");
         texts.add("You can plant Miracle Seeds and Apricorns to grow grass and trees. ");
         texts.add("You can build a gate by building a door between two fences.");
         texts.add("Eggs must be carried to hatch. RIDE does count while FLY does not.");
         texts.add("Rocks types know SMASH. Use it on boulders to collect rocks. Rocks respawn daily.");
         texts.add("Collect Soft Feather and Silky Thread to make a soft bed for your home.");
         texts.add("Bug types drop Silky Thread, Flying types drop Soft Feather.");
         entries.add("Field moves");
         entries.add("Crafting");
         entries.add("Fainting");
         entries.add("Ghosts");
         entries.add("Building 1");
         entries.add("Building 2");
         entries.add("Breeding 3");
         entries.add("Habitats 1");
         entries.add("Habitats 2");
         entries.add("Habitats 3");
         entries.add("Wilds Eggs");
         entries.add("Back doors");
         entries.add("Healing 1");
         entries.add("Healing 2");
         entries.add("Healing 3");
         entries.add("Escape Rope");
         entries.add("Plants");
         entries.add("Gates");
         entries.add("Eggs");
         entries.add("Rock Smash");
         entries.add("Soft Bed 1");
         entries.add("Soft Bed 2");
      }
   }

   static class Intro extends Action {
      int length;
      public Action.Layer layer = Action.Layer.gui_110;
      Menu prevMenu;

      public Intro(Menu prevMenu, int length, Action nextAction) {
         super();
         this.prevMenu = prevMenu;
         this.nextAction = nextAction;
         this.length = length;
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
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         this.length--;
         if (this.length <= 0) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }
}
