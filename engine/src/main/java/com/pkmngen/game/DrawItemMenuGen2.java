package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

class DrawItemMenuGen2 extends Menu {
   public Action.Layer layer = Action.Layer.gui_106;
   Sprite arrow;
   Sprite arrowWhite;
   Texture background;
   Map<Integer, Vector2> arrowCoords;
   Vector2 newPos;
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
   public boolean isSorting = false;
   public int sortingIndex = 0;
   public TextureRegion[] bagNames = new TextureRegion[5];
   public TextureRegion[] bagIcons = new TextureRegion[5];
   public static int boxIndex = 0;
   public Texture xText;
   public boolean drawItems = false;
   public boolean drawArrow = false;
   public static HashMap<String, String> itemDescriptions = new HashMap<>();
   public static ArrayList<String> ballItems = new ArrayList<>();
   public static ArrayList<String> materialItems = new ArrayList<>();
   public static ArrayList<String> keyItems = new ArrayList<>();
   public static ArrayList<String> medicineItems = new ArrayList<>();

   public DrawItemMenuGen2(Game game, Menu prevMenu) {
      this.prevMenu = prevMenu;
      this.disabled = false;
      this.cursorDelay = 0;
      this.arrowCoords = new HashMap<>();
      Texture text = TextureCache.get(Gdx.files.internal("battle/redarrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/redarrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      this.background = TextureCache.get(Gdx.files.internal("menu/item_menu_gsc1.png"));
      text = TextureCache.get(Gdx.files.internal("arrow_down.png"));
      this.downArrow = new Sprite(text, 0, 0, 7, 5);
      this.downArrow.setPosition(40.0F, 50.0F);
      this.downArrowTimer = 0;
      this.currIndex = DrawItemMenu.lastCurrIndex;
      this.cursorPos = DrawItemMenu.lastCursorPos;
      this.arrowCoords.put(0, new Vector2(57.0F, 120.0F));
      this.arrowCoords.put(1, new Vector2(57.0F, 104.0F));
      this.arrowCoords.put(2, new Vector2(57.0F, 88.0F));
      this.arrowCoords.put(3, new Vector2(57.0F, 72.0F));
      this.arrowCoords.put(4, new Vector2(57.0F, 56.0F));
      this.newPos = this.arrowCoords.get(this.cursorPos);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      text = TextureCache.get(Gdx.files.internal("menu/bag-sheet1.png"));

      for (int i = 0; i < 5; i++) {
         this.bagNames[i] = new TextureRegion(text, i * 41, 0, 40, 24);
         this.bagIcons[i] = new TextureRegion(text, i * 41, 26, 40, 24);
      }

      this.xText = TextureCache.get(Gdx.files.internal("menu/x1.png"));
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
      this.drawArrow = true;
      this.drawItems = true;
      this.itemsList = new ArrayList<>();
      ArrayList<String> currFilter = null;
      if (boxIndex == 1) {
         currFilter = ballItems;
      } else if (boxIndex == 2) {
         currFilter = materialItems;
      } else if (boxIndex == 3) {
         currFilter = keyItems;
      } else if (boxIndex == 4) {
         currFilter = medicineItems;
      }

      for (String entry : game.player.getItemsDict().keySet()) {
         if (currFilter == null) {
            if (!ballItems.contains(entry.toLowerCase(Locale.ROOT))
               && !materialItems.contains(entry.toLowerCase(Locale.ROOT))
               && !keyItems.contains(entry.toLowerCase(Locale.ROOT))
               && !medicineItems.contains(entry.toLowerCase(Locale.ROOT))) {
               this.itemsList.add(entry);
            }
         } else if (currFilter.contains(entry.toLowerCase(Locale.ROOT))) {
            this.itemsList.add(entry);
         }
      }

      this.itemsList.add("Cancel");
      if (this.currIndex + this.cursorPos >= this.itemsList.size()) {
         this.currIndex = this.itemsList.size() - 5;
         if (this.currIndex < 0) {
            this.currIndex = 0;
         }
      }

      while (this.currIndex + this.cursorPos >= this.itemsList.size()) {
         if (this.currIndex > 0) {
            this.currIndex--;
         } else {
            this.cursorPos--;
         }
      }

      this.newPos = this.arrowCoords.get(this.cursorPos);
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
            currSprite.setPosition(64 + 8 * i, 120.0F);
            word.add(currSprite);
            i++;
         }

         if (game.player.hasItem(entry)) {
            char[] numItemsArray = String.valueOf(game.player.getItemAmount(entry)).toCharArray();
            int var21 = 10;
            var21 += 3 - numItemsArray.length;
            int xPos = 137;
            if (numItemsArray.length >= 3) {
               xPos -= 8 * (numItemsArray.length - 2);
            }

            Sprite currSprite = new Sprite(this.xText);
            currSprite.setPosition(xPos, 113.0F);
            word.add(currSprite);

            for (char letter : numItemsArray) {
               Sprite letterSprite = game.textDict.get(letter);
               currSprite = new Sprite(letterSprite);
               currSprite.setPosition(56 + 8 * var21, 112.0F);
               word.add(currSprite);
               var21++;
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

      game.uiBatch.draw(this.background, 0.0F, 0.0F);
      if (this.drawItems) {
         int j = 0;

         for (int i = 0; i < this.spritesToDraw.size(); i++) {
            if (this.currIndex <= i && i < this.currIndex + 5) {
               for (Sprite sprite : this.spritesToDraw.get(i)) {
                  game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j * 16);
               }

               j++;
            }
         }
      }

      game.uiBatch.draw(this.bagIcons[boxIndex], 0.0F, 64.0F);
      game.uiBatch.draw(this.bagNames[boxIndex], 0.0F, 96.0F);
      if (this.disabled) {
         this.refresh = true;
         if (this.drawArrow) {
            game.uiBatch.draw(this.arrowWhite, this.newPos.x, this.newPos.y);
         }
      } else {
         if (this.refresh) {
            this.firstStep(game);
            this.refresh = false;
         }

         int i = 0;
         int j = 0;
         String currItem = this.itemsList.get(this.currIndex + this.cursorPos);
         currItem = itemDescriptions.get(currItem.toLowerCase(Locale.ROOT));
         char[] itemText = null;
         if (currItem != null) {
            itemText = currItem.toCharArray();
         }

         for (int k = 0; itemText != null && k < itemText.length; k++) {
            char character = itemText[k];
            if (character == ' ' && k + 1 < itemText.length) {
               int length = 1;
               char nextChar = itemText[k + length];

               while (nextChar != ' ' && k + length < itemText.length) {
                  nextChar = itemText[k + length++];
               }

               if (i + length > 19 && j < 5) {
                  i = -1;
                  j++;
               }
            }

            Sprite letterSprite = game.textDict.get(character);
            if (i != -1 || character != ' ') {
               game.uiBatch.draw(letterSprite, 8 * i + 8, 25 - 16 * j);
            }

            if (++i > 19) {
               i = 0;
               j++;
            }
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
            if (!InputProcessor.downJustPressed && this.downTimer < 20) {
               if (InputProcessor.rightJustPressed) {
                  this.disabled = true;
                  this.drawItems = false;
                  this.drawArrow = false;
                  game.insertAction(new PlayMusic("menu_switch2", null));
                  game.insertAction(
                     new WaitFrames(game, 4, new RunCode(() -> boxIndex = Math.floorMod(boxIndex + 1, 5), new WaitFrames(game, 4, new RunCode(() -> {
                        this.firstStep(game);
                        this.drawArrow = false;
                     }, new WaitFrames(game, 4, new RunCode(() -> {
                        this.disabled = false;
                        this.refresh = false;
                        this.drawArrow = true;
                     }, null))))))
                  );
               } else if (InputProcessor.leftJustPressed) {
                  this.disabled = true;
                  this.drawItems = false;
                  this.drawArrow = false;
                  game.insertAction(new PlayMusic("menu_switch2", null));
                  game.insertAction(
                     new WaitFrames(game, 4, new RunCode(() -> boxIndex = Math.floorMod(boxIndex - 1, 5), new WaitFrames(game, 4, new RunCode(() -> {
                        this.firstStep(game);
                        this.drawArrow = false;
                     }, new WaitFrames(game, 4, new RunCode(() -> {
                        this.disabled = false;
                        this.refresh = false;
                        this.drawArrow = true;
                     }, null))))))
                  );
               }
            } else if (this.cursorPos < 4 && this.currIndex + this.cursorPos + 1 < this.itemsList.size()) {
               this.cursorPos++;
               this.newPos = this.arrowCoords.get(this.cursorPos);
            } else if (this.currIndex < this.itemsList.size() - 5) {
               this.currIndex++;
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

         if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
         }

         if (InputProcessor.aJustPressed) {
            if (!this.isSorting) {
               game.insertAction(new PlayMusic("click1", null));
               String name = this.itemsList.get(this.currIndex + this.cursorPos);
               DrawItemMenu.lastCurrIndex = this.currIndex;
               DrawItemMenu.lastCursorPos = this.cursorPos;
               if ("Cancel".equals(name)) {
                  this.prevMenu.disabled = false;
                  game.insertAction(this.prevMenu);
                  game.actionStack.remove(this);
               } else {
                  this.disabled = true;
                  game.actionStack.remove(this);
                  game.insertAction(new DrawUseTossMenu(game, this, name, new Vector2(-104.0F, 23.0F)));
               }
            } else {
               String toItem = this.itemsList.get(this.currIndex + this.cursorPos);
               if (!toItem.equals("Cancel")) {
                  String fromItem = this.itemsList.get(this.sortingIndex);
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
               }
            }
         } else if (InputProcessor.bJustPressed) {
            if (this.isSorting) {
               this.isSorting = false;
            }

            DrawItemMenu.lastCurrIndex = this.currIndex;
            DrawItemMenu.lastCursorPos = this.cursorPos;
            game.actionStack.remove(this);
            game.insertAction(
               new DrawWhiteScreen(12, new DrawItemMenu.Intro(this.prevMenu, 13, new RunCode(() -> this.prevMenu.disabled = false, this.prevMenu)))
            );
            game.insertAction(new PlayMusic("click1", null));
         } else {
            if (InputProcessor.startJustPressed) {
               int targetSortingIndex = this.currIndex + this.cursorPos;
               if (targetSortingIndex == this.itemsList.size() - 1) {
                  return;
               }

               this.isSorting = true;
               this.sortingIndex = targetSortingIndex;
            }
         }
      }
   }

   static {
      itemDescriptions.put("White Apricorn".toLowerCase(Locale.ROOT), "A white APRICORN.");
      itemDescriptions.put("Black Apricorn".toLowerCase(Locale.ROOT), "A black APRICORN.");
      itemDescriptions.put("Red Apricorn".toLowerCase(Locale.ROOT), "A red APRICORN.");
      itemDescriptions.put("Blue Apricorn".toLowerCase(Locale.ROOT), "A blue APRICORN.");
      itemDescriptions.put("Yellow Apricorn".toLowerCase(Locale.ROOT), "A yellow APRICORN.");
      itemDescriptions.put("Pink Apricorn".toLowerCase(Locale.ROOT), "A pink APRICORN.");
      itemDescriptions.put("Green Apricorn".toLowerCase(Locale.ROOT), "A green APRICORN.");
      itemDescriptions.put("Old Rod".toLowerCase(Locale.ROOT), "Use by water to fish for POKéMON.");
      itemDescriptions.put("Good Rod".toLowerCase(Locale.ROOT), "Use by water to fish for POKéMON.");
      itemDescriptions.put("Super Rod".toLowerCase(Locale.ROOT), "Use by water to fish for POKéMON.");
      itemDescriptions.put("Pecha Berry".toLowerCase(Locale.ROOT), "Cures <> of  POISON.");
      itemDescriptions.put("Cheri Berry".toLowerCase(Locale.ROOT), "Cures <> of PARALYSIS.");
      itemDescriptions.put("Rawst Berry".toLowerCase(Locale.ROOT), "Cures <> of BURN.");
      itemDescriptions.put("Aspear Berry".toLowerCase(Locale.ROOT), "Cures <> of  FREEZE.");
      itemDescriptions.put("Persim Berry".toLowerCase(Locale.ROOT), "Cures <> of CONFUSION.");
      itemDescriptions.put("Chesto Berry".toLowerCase(Locale.ROOT), "Cures <> of SLEEP.");
      itemDescriptions.put("Lum Berry".toLowerCase(Locale.ROOT), "Cures <> of STATUS EFFECTS.");
      itemDescriptions.put("Berry Seed".toLowerCase(Locale.ROOT), "Can be planted to grow BERRIES.");
      itemDescriptions.put("Log".toLowerCase(Locale.ROOT), "A sturdy LOG.");
      itemDescriptions.put("Grass".toLowerCase(Locale.ROOT), "Freshly cut GRASS.");
      itemDescriptions.put("Dry Sand".toLowerCase(Locale.ROOT), "Hot, dry sand from the desert.");
      itemDescriptions.put("Light Clay".toLowerCase(Locale.ROOT), "Soft clay from the mountains.");
      itemDescriptions.put("Flowers".toLowerCase(Locale.ROOT), "Small, beautiful flowers.");
      itemDescriptions.put("Cursed Soil".toLowerCase(Locale.ROOT), "Soil taken from a graveyard.");
      itemDescriptions.put("Ashen Soil".toLowerCase(Locale.ROOT), "Soil found near a volcano.");
      itemDescriptions.put("Dry Soil".toLowerCase(Locale.ROOT), "Dry soil from the savannah.");
      itemDescriptions.put("Soft Sand".toLowerCase(Locale.ROOT), "Soft, light sand from the beach.");
      itemDescriptions.put("Poké Ball".toLowerCase(Locale.ROOT), "A tool for catching POKéMON.");
      itemDescriptions.put("Great Ball".toLowerCase(Locale.ROOT), "A good tool for catching POKéMON.");
      itemDescriptions.put("Ultra Ball".toLowerCase(Locale.ROOT), "An excellent tool for catching <>.");
      itemDescriptions.put("Love Ball".toLowerCase(Locale.ROOT), "Catch <> of the opposite gender.");
      itemDescriptions.put("Fast Ball".toLowerCase(Locale.ROOT), "Most effective on fast POKéMON.");
      itemDescriptions.put("Lure Ball".toLowerCase(Locale.ROOT), "Most effective while fishing.");
      itemDescriptions.put("Moon Ball".toLowerCase(Locale.ROOT), "For <> that evolve by Moon Stone.");
      itemDescriptions.put("Friend Ball".toLowerCase(Locale.ROOT), "A ball that raises FRIENDSHIP.");
      itemDescriptions.put("Heavy Ball".toLowerCase(Locale.ROOT), "Most effective on heavy POKéMON.");
      itemDescriptions.put("Level Ball".toLowerCase(Locale.ROOT), "Most effective on low-level POKéMON.");
      itemDescriptions.put("Dusk Ball".toLowerCase(Locale.ROOT), "Most effective in dark areas.");
      itemDescriptions.put("Net Ball".toLowerCase(Locale.ROOT), "Best for BUG and WATER type <>.");
      itemDescriptions.put("Dive Ball".toLowerCase(Locale.ROOT), "Catch POKéMON underwater.");
      itemDescriptions.put("Quick Ball".toLowerCase(Locale.ROOT), "Best when used at start of encounter");
      itemDescriptions.put("Heal Ball".toLowerCase(Locale.ROOT), "Heals POKéMON it contains.");
      itemDescriptions.put("Nest Ball".toLowerCase(Locale.ROOT), "Most effective on weaker POKéMON.");
      itemDescriptions.put("Dream Ball".toLowerCase(Locale.ROOT), "Catch sleeping POKéMON.");
      itemDescriptions.put("Timer Ball".toLowerCase(Locale.ROOT), "Most effective in a longer battle.");
      itemDescriptions.put("Old Amber".toLowerCase(Locale.ROOT), "Amber containing DNA of ancient <>.");
      itemDescriptions.put("Helix Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Dome Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Claw Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Root Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Armor Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Skull Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Shield Fossil".toLowerCase(Locale.ROOT), "A fossil of an ancient POKéMON.");
      itemDescriptions.put("Stardust".toLowerCase(Locale.ROOT), "A sparkling  powder.");
      itemDescriptions.put("Psi Energy".toLowerCase(Locale.ROOT), "An energy exuded by PSYCHIC types.");
      itemDescriptions.put("Charcoal".toLowerCase(Locale.ROOT), "Wood burned by a FIRE type.");
      itemDescriptions.put("Hard Shell".toLowerCase(Locale.ROOT), "Fragments of a WATER type' shell.");
      itemDescriptions.put("Secret Key".toLowerCase(Locale.ROOT), "A mysterious key.");
      itemDescriptions.put("Manure".toLowerCase(Locale.ROOT), "A smelly fertilizer.");
      itemDescriptions.put("Sweet Apple".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Star Piece".toLowerCase(Locale.ROOT), "A sparkling gem.");
      itemDescriptions.put("Dark Energy".toLowerCase(Locale.ROOT), "An energy exuded by DARK types.");
      itemDescriptions.put("Life Force".toLowerCase(Locale.ROOT), "An energy from GHOST types.");
      itemDescriptions.put("Berry Juice".toLowerCase(Locale.ROOT), "A healing drink that restores HP.");
      itemDescriptions.put("Ancientpowder".toLowerCase(Locale.ROOT), "A mysterious and rare powder.");
      itemDescriptions.put("Moomoo Milk".toLowerCase(Locale.ROOT), "A nutritious drink that restores HP.");
      itemDescriptions.put("Honey".toLowerCase(Locale.ROOT), "A sweet and sticky treat.");
      itemDescriptions.put("Soft Wool".toLowerCase(Locale.ROOT), "Thick, fluffy  wool.");
      itemDescriptions.put("Magnet".toLowerCase(Locale.ROOT), "A small but powerful magnet.");
      itemDescriptions.put("Nevermeltice".toLowerCase(Locale.ROOT), "A special ice that doesnì melt.");
      itemDescriptions.put("Dragon Scale".toLowerCase(Locale.ROOT), "A scale from a DRAGON type.");
      itemDescriptions.put("Dragon Fang".toLowerCase(Locale.ROOT), "A fang from a DRAGON type.");
      itemDescriptions.put("Binding Band".toLowerCase(Locale.ROOT), "A durable band.");
      itemDescriptions.put("Metal Coat".toLowerCase(Locale.ROOT), "A coat shed by STEEL types.");
      itemDescriptions.put("Poison Barb".toLowerCase(Locale.ROOT), "A small, poisonous barb.");
      itemDescriptions.put("Miracle Seed".toLowerCase(Locale.ROOT), "Can be planted to grow GRASS.");
      itemDescriptions.put("Hard Stone".toLowerCase(Locale.ROOT), "Durable rock.");
      itemDescriptions.put("Soft Feather".toLowerCase(Locale.ROOT), "A stray feather from a FLYING type");
      itemDescriptions.put("Silky Thread".toLowerCase(Locale.ROOT), "Delicate thread from a BUG type.");
      itemDescriptions.put("Thin Paper".toLowerCase(Locale.ROOT), "A soft paper good for making posters");
      itemDescriptions.put("Soft Bedding".toLowerCase(Locale.ROOT), "For a comfy bed and faster healing");
      itemDescriptions.put("Max Repel".toLowerCase(Locale.ROOT), "A spray that repels weak POKéMON.");
      itemDescriptions.put("Repel".toLowerCase(Locale.ROOT), "A spray that repels weak POKéMON.");
      itemDescriptions.put("Clear Glass".toLowerCase(Locale.ROOT), "Clear, delicate glass.");
      itemDescriptions.put("Silph Scope".toLowerCase(Locale.ROOT), "An item that identifies ghosts.");
      itemDescriptions.put("Ragecandybar".toLowerCase(Locale.ROOT), "A candy bar that can restore HP.");
      itemDescriptions.put("Rare Candy".toLowerCase(Locale.ROOT), "Raises a POKéMON' level by 1.");
      itemDescriptions.put("Poké Doll".toLowerCase(Locale.ROOT), "A cute plushie.");
      itemDescriptions.put("Water Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Fire Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Leaf Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Thunderstone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Moon Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Sun Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Ice Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Dawn Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Dusk Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("Shiny Stone".toLowerCase(Locale.ROOT), "Can evolve certain POKéMON.");
      itemDescriptions.put("revive", "Restores a fainted <> to half hp.");
      itemDescriptions.put("max revive", "Fully restores a fainted <>.");
      itemDescriptions.put("master ball", "The best BALL. It never misses.");
      itemDescriptions.put("escape rope", "Takes you to a nearby shore.");
      itemDescriptions.put("spell tag", "A sinister, eerie tag.");
      itemDescriptions.put("sleeping bag", "Slowly restores party hp.");
      itemDescriptions.put("big pearl", "A big, beautiful pearl.");
      itemDescriptions.put("Mystic Soil".toLowerCase(Locale.ROOT), "Soil from the Forest.");
      itemDescriptions.put("Damp Soil".toLowerCase(Locale.ROOT), "Soil from the Lake");
      ballItems.add("poké ball");
      ballItems.add("great ball");
      ballItems.add("ultra ball");
      ballItems.add("love ball");
      ballItems.add("fast ball");
      ballItems.add("lure ball");
      ballItems.add("moon ball");
      ballItems.add("friend ball");
      ballItems.add("heavy ball");
      ballItems.add("dusk ball");
      ballItems.add("net ball");
      ballItems.add("dive ball");
      ballItems.add("quick ball");
      ballItems.add("heal ball");
      ballItems.add("nest ball");
      ballItems.add("dream ball");
      ballItems.add("timer ball");
      ballItems.add("level ball");
      ballItems.add("master ball");
      ballItems.add("black apricorn");
      ballItems.add("blue apricorn");
      ballItems.add("green apricorn");
      ballItems.add("pink apricorn");
      ballItems.add("red apricorn");
      ballItems.add("white apricorn");
      ballItems.add("yellow apricorn");
      materialItems.add("log");
      materialItems.add("grass");
      materialItems.add("dry sand");
      materialItems.add("light clay");
      materialItems.add("flowers");
      materialItems.add("cursed soil");
      materialItems.add("ashen soil");
      materialItems.add("dry soil");
      materialItems.add("stardust");
      materialItems.add("psi energy");
      materialItems.add("charcoal");
      materialItems.add("hard shell");
      materialItems.add("manure");
      materialItems.add("psi energy");
      materialItems.add("star piece");
      materialItems.add("dark energy");
      materialItems.add("life force");
      materialItems.add("ancientpowder");
      materialItems.add("honey");
      materialItems.add("soft wool");
      materialItems.add("magnet");
      materialItems.add("nevermeltice");
      materialItems.add("dragon scale");
      materialItems.add("dragon fang");
      materialItems.add("stardust");
      materialItems.add("binding band");
      materialItems.add("metal coat");
      materialItems.add("soft sand");
      materialItems.add("poison barb");
      materialItems.add("miracle seed");
      materialItems.add("charcoal");
      materialItems.add("hard stone");
      materialItems.add("soft feather");
      materialItems.add("hard shell");
      materialItems.add("silky thread");
      materialItems.add("clear glass");
      materialItems.add("damp soil");
      materialItems.add("mystic soil");
      keyItems.add("secret key");
      keyItems.add("silph scope");
      keyItems.add("old rod");
      keyItems.add("good rod");
      keyItems.add("super rod");
      keyItems.add("sleeping bag");
      keyItems.add("escape rope");
      medicineItems.add("lum berry");
      medicineItems.add("pecha berry");
      medicineItems.add("cheri berry");
      medicineItems.add("aspear berry");
      medicineItems.add("chesto berry");
      medicineItems.add("rawst berry");
      medicineItems.add("persim berry");
      medicineItems.add("max repel");
      medicineItems.add("repel");
      medicineItems.add("moomoo milk");
      medicineItems.add("berry juice");
   }
}
