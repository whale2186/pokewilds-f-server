package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

class DrawPokemonMenu extends Menu {
   public static boolean drawChoosePokemonText = true;
   public static int avatarAnimCounter = 24;
   public static int currIndex = 0;
   public static int lastIndex = 0;
   public Action.Layer layer = Action.Layer.gui_108;
   Sprite bgSprite;
   Sprite helperSprite;
   Sprite arrow;
   Sprite arrowWhite;
   Sprite healthBar;
   Sprite healthSprite;
   Vector2 newPos;
   Map<Integer, Vector2> arrowCoords;
   String item = null;
   ArrayList<String> ableWords = new ArrayList<>();
   boolean isStorageChest = false;
   public static ArrayList<Pokemon> allPokemon;
   public static Tile storageChestTile;
   Sprite arrowLeft;
   public static int scrollIndex = 0;
   int upTimer = 0;
   int downTimer = 0;
   public boolean refresh = false;
   char[] levelChars;
   public static Pokemon.Generation generation = Pokemon.Generation.CRYSTAL;
   public static boolean animsPaused = false;
   public static int avatarHopAnimCounter = 0;
   public static int avatarAnimCounterGen2 = 0;
   public static int avatarYellowHealthAnimCounter = 0;
   public static int avatarRedHealthAnimCounter = 0;
   Sprite healthSpriteGreen;
   Sprite healthSpriteYellow;
   Sprite healthSpriteRed;

   public DrawPokemonMenu(Game game, boolean isStorageChest, Tile storageChestTile, Menu prevMenu) {
      this(game, prevMenu);
      this.isStorageChest = isStorageChest;
      DrawPokemonMenu.storageChestTile = storageChestTile;
   }

   public DrawPokemonMenu(Game game, String item, Menu prevMenu) {
      this(game, prevMenu);
      this.item = item;
   }

   public DrawPokemonMenu(Game game, Menu prevMenu) {
      this.prevMenu = prevMenu;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      this.arrowLeft = new Sprite(text, 0, 0, 5, 7);
      this.arrowLeft.flip(true, false);
      currIndex = lastIndex;
      this.arrowCoords = new HashMap<>();
      if (generation == Pokemon.Generation.RED) {
         text = TextureCache.get(Gdx.files.internal("menu/health_bar.png"));
         this.healthBar = new Sprite(text, 0, 0, 160, 16);
         text = TextureCache.get(Gdx.files.internal("battle/health1.png"));
         this.healthSprite = new Sprite(text, 0, 0, 1, 2);

         for (int i = -1; i < 6; i++) {
            this.arrowCoords.put(i, new Vector2(1.0F, 128 - 16 * i));
         }

         text = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
         this.bgSprite = new Sprite(text, 8, 8, 160, 144);
      } else {
         text = TextureCache.get(Gdx.files.internal("menu/gsc/health_bar1.png"));
         this.healthBar = new Sprite(text, 0, 0, 160, 16);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/health1.png"));
         this.healthSpriteGreen = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/health_yellow.png"));
         this.healthSpriteYellow = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/health_red.png"));
         this.healthSpriteRed = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);

         for (int i = -1; i < 6; i++) {
            this.arrowCoords.put(i, new Vector2(1.0F, 129 - 16 * i));
         }

         text = TextureCache.get(Gdx.files.internal("menu/gsc/background1.png"));
         this.bgSprite = new Sprite(text);
      }

      this.newPos = this.arrowCoords.get(currIndex);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   public void refreshCursorPosition() {
      if (currIndex < 0) {
         currIndex = 0;
      }

      if (currIndex + scrollIndex >= allPokemon.size()) {
         scrollIndex = allPokemon.size() - 6;
         if (scrollIndex < 0) {
            scrollIndex = 0;
         }
      }

      while (currIndex + scrollIndex >= allPokemon.size()) {
         if (scrollIndex > 0) {
            scrollIndex--;
         } else {
            currIndex--;
         }
      }
   }

   @Override
   public void firstStep(Game game) {
      if (this.item != null && Game.evoStones.contains(this.item)) {
         this.ableWords.clear();

         for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.isEgg) {
               this.ableWords.add("NOT ABLE");
            } else {
               Map<String, String> evos = Specie.gen2Evos.get(pokemon.specie.name);
               boolean foundItem = false;
               Iterator var6 = evos.keySet().iterator();

               while (true) {
                  if (var6.hasNext()) {
                     String evo = (String)var6.next();
                     if (!evo.equals(this.item)
                        || pokemon.specie.name.equals("snorunt") && !pokemon.gender.equals("female")
                        || pokemon.specie.name.equals("kirlia") && !pokemon.gender.equals("male")) {
                        continue;
                     }

                     foundItem = true;
                  }

                  if (foundItem) {
                     this.ableWords.add("ABLE");
                  } else {
                     this.ableWords.add("NOT ABLE");
                  }
                  break;
               }
            }
         }
      }

      allPokemon = game.player.pokemon;
      this.refreshCursorPosition();
      avatarHopAnimCounter = 0;
      avatarAnimCounterGen2 = 0;
   }

   public void stepGen1(Game game) {
      game.uiBatch.draw(this.bgSprite, this.bgSprite.getX(), this.bgSprite.getY());

      for (int i = 0; i + scrollIndex < allPokemon.size() && i < 6; i++) {
         Pokemon currPokemon = allPokemon.get(i + scrollIndex);
         if (i == currIndex) {
            if (avatarAnimCounter >= 18) {
               game.uiBatch.draw(currPokemon.avatarSprites.get(0), 8.0F, 128 - 16 * i);
            } else if (avatarAnimCounter >= 12) {
               game.uiBatch.draw(currPokemon.avatarSprites.get(1), 8.0F, 128 - 16 * i);
            } else if (avatarAnimCounter >= 6) {
               game.uiBatch.draw(currPokemon.avatarSprites.get(2), 8.0F, 128 - 16 * i);
            } else {
               game.uiBatch.draw(currPokemon.avatarSprites.get(3), 8.0F, 128 - 16 * i);
            }
         } else {
            game.uiBatch.draw(currPokemon.avatarSprites.get(0), 8.0F, 128 - 16 * i);
         }

         char[] textArray;
         if (i < this.ableWords.size()) {
            textArray = this.ableWords.get(i).toUpperCase(Locale.ROOT).toCharArray();
         } else {
            textArray = currPokemon.nickname.toUpperCase(Locale.ROOT).toCharArray();
         }

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.textDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 24 + 8 * j, 136 - 16 * i);
         }

         if (!currPokemon.isEgg) {
            game.uiBatch.draw(this.healthBar, 0.0F, 128 - 16 * i);
            if (currPokemon.status != null && !currPokemon.status.equals("confuse") && !currPokemon.status.equals("attract")) {
               String text = "";
               if (currPokemon.status.equals("poison") || currPokemon.status.equals("toxic")) {
                  text = "PSN";
               } else if (currPokemon.status.equals("paralyze")) {
                  text = "PAR";
               } else if (currPokemon.status.equals("freeze")) {
                  text = "FRZ";
               } else if (currPokemon.status.equals("sleep")) {
                  text = "SLP";
               } else if (currPokemon.status.equals("burn")) {
                  text = "BRN";
               }

               textArray = text.toCharArray();

               for (int j = 0; j < textArray.length; j++) {
                  Sprite letterSprite = game.textDict.get(textArray[j]);
                  game.uiBatch.draw(letterSprite, 104 + 8 * j, 136 - 16 * i);
               }
            } else {
               this.levelChars = String.valueOf(currPokemon.level).toCharArray();
               int offset = -8 * (currPokemon.level / 100);

               for (char c : this.levelChars) {
                  Sprite currSprite = game.textDict.get(c);
                  game.uiBatch.draw(currSprite, 112 + offset, 136 - 16 * i);
                  offset += 8;
               }
            }

            textArray = String.valueOf(currPokemon.maxStats.get("hp")).toCharArray();
            int offset = -8 * (textArray.length - 1);

            for (char c : textArray) {
               Sprite currSprite = game.textDict.get(c);
               game.uiBatch.draw(currSprite, 152 + offset, 128 - 16 * i);
               offset += 8;
            }

            textArray = String.valueOf(currPokemon.currentStats.get("hp")).toCharArray();
            offset = -8 * (textArray.length - 1);

            for (char c : textArray) {
               Sprite currSprite = game.textDict.get(c);
               game.uiBatch.draw(currSprite, 120 + offset, 128 - 16 * i);
               offset += 8;
            }

            int targetSize = (int)Math.ceil(currPokemon.currentStats.get("hp").intValue() * 48.0F / currPokemon.maxStats.get("hp").intValue());

            for (int j = 0; j < targetSize; j++) {
               game.uiBatch.draw(this.healthSprite, 48 + 1 * j, 131 - 16 * i);
            }

            if (currPokemon.gender.equals("male")) {
               game.uiBatch.draw(TextureCache.maleSymbol, 136.0F, 136 - 16 * i);
            } else if (currPokemon.gender.equals("female")) {
               game.uiBatch.draw(TextureCache.femaleSymbol, 136.0F, 136 - 16 * i);
            }
         }
      }

      if (this.isStorageChest) {
         if (allPokemon == game.player.pokemon) {
            char[] textArray = "Your POKéMON".toCharArray();

            for (int l = 0; l < textArray.length; l++) {
               Sprite letterSprite = game.textDict.get(textArray[l]);
               game.uiBatch.draw(letterSprite, 32 + 8 * l, 24.0F);
            }

            game.uiBatch.draw(this.arrow, 136.0F, 24.0F);
         } else {
            char[] textArray = "Stored POKéMON".toCharArray();

            for (int l = 0; l < textArray.length; l++) {
               Sprite letterSprite = game.textDict.get(textArray[l]);
               game.uiBatch.draw(letterSprite, 24 + 8 * l, 24.0F);
            }

            game.uiBatch.draw(this.arrowLeft, 8.0F, 24.0F);
         }
      } else if (drawChoosePokemonText) {
         char[] textArray = "Choose a POKéMON.".toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.textDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 8 + 8 * j, 24.0F);
         }
      }
   }

   public void stepGen2(Game game) {
      game.uiBatch.draw(this.bgSprite, 0.0F, 0.0F);

      for (int i = 0; i + scrollIndex < allPokemon.size() && i < 6; i++) {
         Pokemon currPokemon = allPokemon.get(i + scrollIndex);
         char[] textArray;
         if (i < this.ableWords.size()) {
            textArray = this.ableWords.get(i).toUpperCase(Locale.ROOT).toCharArray();
         } else {
            textArray = currPokemon.nickname.toUpperCase(Locale.ROOT).toCharArray();
         }

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.textDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 24 + 8 * j, 128 - 16 * i);
         }

         int targetSize = (int)Math.ceil(currPokemon.currentStats.get("hp").intValue() * 48.0F / currPokemon.maxStats.get("hp").intValue());
         int offset = 0;
         int offsetY = 0;
         int interval = 9;
         int counter = avatarAnimCounterGen2;
         if (i == currIndex || i == DrawPokemonMenu.SelectedMenu.Switch.index) {
            offset = 8;
            if (avatarHopAnimCounter >= 16 && !currPokemon.isEgg) {
               offsetY = 1;
            }
         }

         if (targetSize > 23) {
            this.healthSprite = this.healthSpriteGreen;
         } else if (targetSize > 10) {
            interval = 73;
            counter = avatarYellowHealthAnimCounter;
            this.healthSprite = this.healthSpriteYellow;
         } else {
            interval = 138;
            counter = avatarRedHealthAnimCounter;
            offsetY = 0;
            this.healthSprite = this.healthSpriteRed;
         }

         if (counter >= interval * 3) {
            game.uiBatch.draw(currPokemon.avatarSprites.get(0), offset, 126 - 16 * i + offsetY);
         } else if (counter >= interval * 2) {
            game.uiBatch.draw(currPokemon.avatarSprites.get(1), offset, 126 - 16 * i + offsetY);
         } else if (counter >= interval * 1) {
            game.uiBatch.draw(currPokemon.avatarSprites.get(2), offset, 126 - 16 * i + offsetY);
         } else {
            game.uiBatch.draw(currPokemon.avatarSprites.get(3), offset, 126 - 16 * i + offsetY);
         }

         if (!currPokemon.isEgg) {
            for (int j = 0; j < targetSize; j++) {
               game.uiBatch.draw(this.healthSprite, 104 + 1 * j, 123 - 16 * i);
            }

            game.uiBatch.draw(this.healthBar, 0.0F, 120 - 16 * i);
            this.levelChars = String.valueOf(currPokemon.level).toCharArray();
            offset = -8 * (currPokemon.level / 100);

            for (char c : this.levelChars) {
               Sprite currSprite = game.textDict.get(c);
               game.uiBatch.draw(currSprite, 72 + offset, 120 - 16 * i);
               offset += 8;
            }

            String text = "";
            if (currPokemon.status != null) {
               if (currPokemon.status.equals("poison") || currPokemon.status.equals("toxic")) {
                  text = "PSN";
               } else if (currPokemon.status.equals("paralyze")) {
                  text = "PAR";
               } else if (currPokemon.status.equals("freeze")) {
                  text = "FRZ";
               } else if (currPokemon.status.equals("sleep")) {
                  text = "SLP";
               } else if (currPokemon.status.equals("burn")) {
                  text = "BRN";
               }
            }

            if (currPokemon.currentStats.get("hp") <= 0) {
               text = "FNT";
            }

            textArray = text.toCharArray();

            for (int j = 0; j < textArray.length; j++) {
               Sprite letterSprite = game.textDict.get(textArray[j]);
               game.uiBatch.draw(letterSprite, 40 + 8 * j, 120 - 16 * i);
            }

            textArray = String.valueOf(currPokemon.maxStats.get("hp")).toCharArray();
            offset = -8 * (textArray.length - 1);

            for (char c : textArray) {
               Sprite currSprite = game.textDict.get(c);
               game.uiBatch.draw(currSprite, 152 + offset, 128 - 16 * i);
               offset += 8;
            }

            textArray = String.valueOf(currPokemon.currentStats.get("hp")).toCharArray();
            offset = -8 * (textArray.length - 1);

            for (char c : textArray) {
               Sprite currSprite = game.textDict.get(c);
               game.uiBatch.draw(currSprite, 120 + offset, 128 - 16 * i);
               offset += 8;
            }

            if (currPokemon.gender.equals("male")) {
               game.uiBatch.draw(TextureCache.maleSymbol, 24.0F, 120 - 16 * i);
            } else if (currPokemon.gender.equals("female")) {
               game.uiBatch.draw(TextureCache.femaleSymbol, 24.0F, 120 - 16 * i);
            }
         }
      }

      if (this.isStorageChest) {
         if (allPokemon == game.player.pokemon) {
            char[] textArray = "Your POKéMON".toCharArray();

            for (int l = 0; l < textArray.length; l++) {
               Sprite letterSprite = game.textDict.get(textArray[l]);
               game.uiBatch.draw(letterSprite, 32 + 8 * l, 8.0F);
            }

            game.uiBatch.draw(this.arrow, 136.0F, 8.0F);
         } else {
            char[] textArray = "Stored POKéMON".toCharArray();

            for (int l = 0; l < textArray.length; l++) {
               Sprite letterSprite = game.textDict.get(textArray[l]);
               game.uiBatch.draw(letterSprite, 24 + 8 * l, 8.0F);
            }

            game.uiBatch.draw(this.arrowLeft, 8.0F, 8.0F);
         }
      } else if (drawChoosePokemonText) {
         char[] textArray = "Choose a POKéMON.".toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.textDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 8 + 8 * j, 8.0F);
         }
      }
   }

   @Override
   public void step(Game game) {
      if (this.prevMenu != null) {
         this.prevMenu.step(game);
      }

      if (generation == Pokemon.Generation.RED) {
         this.stepGen1(game);
      } else {
         this.stepGen2(game);
      }

      if (this.drawArrowWhite) {
         game.uiBatch.draw(this.arrowWhite, this.newPos.x, this.newPos.y);
      }

      if (this.goAway) {
         game.actionStack.remove(this);
      } else {
         if (!animsPaused) {
            avatarHopAnimCounter++;
            if (avatarHopAnimCounter >= 32) {
               avatarHopAnimCounter = 0;
            }

            avatarAnimCounterGen2++;
            if (avatarAnimCounterGen2 >= 36) {
               avatarAnimCounterGen2 = 0;
            }

            avatarYellowHealthAnimCounter++;
            if (avatarYellowHealthAnimCounter >= 304) {
               avatarYellowHealthAnimCounter = 0;
            }

            avatarRedHealthAnimCounter++;
            if (avatarRedHealthAnimCounter >= 552) {
               avatarRedHealthAnimCounter = 0;
            }
         }

         if (!this.disabled) {
            if (this.refresh) {
               this.refreshCursorPosition();
            }

            avatarAnimCounter--;
            if (avatarAnimCounter <= 0) {
               avatarAnimCounter = 23;
            }

            if (this.isStorageChest) {
               if (InputProcessor.rightJustPressed && allPokemon == game.player.pokemon) {
                  allPokemon = storageChestTile.routeBelongsTo.storedPokemon;
                  this.refreshCursorPosition();
                  game.actionStack.remove(this);
                  game.insertAction(new DrawWhiteScreen(30, this));
                  game.insertAction(new PlayMusic("menu_switch2", null));
                  return;
               }

               if (InputProcessor.leftJustPressed && allPokemon != game.player.pokemon) {
                  allPokemon = game.player.pokemon;
                  this.refreshCursorPosition();
                  game.actionStack.remove(this);
                  game.insertAction(new DrawWhiteScreen(30, this));
                  game.insertAction(new PlayMusic("menu_switch2", null));
                  return;
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
               if ((InputProcessor.downJustPressed || this.downTimer >= 20) && currIndex + scrollIndex < allPokemon.size() - 1) {
                  if (currIndex < 5) {
                     currIndex++;
                  } else {
                     scrollIndex++;
                  }

                  avatarAnimCounter = 24;
               }
            } else if (currIndex + scrollIndex > 0) {
               if (currIndex > 0) {
                  currIndex--;
               } else {
                  scrollIndex--;
               }

               avatarAnimCounter = 24;
            }

            this.newPos = this.arrowCoords.get(currIndex);
            game.uiBatch.draw(this.arrow, this.newPos.x, this.newPos.y);
            if (InputProcessor.aJustPressed && currIndex >= 0) {
               game.insertAction(new PlayMusic("click1", null));
               this.disabled = true;
               this.drawArrowWhite = true;
               Pokemon currPokemon = allPokemon.get(currIndex + scrollIndex);
               if (this.item == null) {
                  this.refresh = true;
                  if (generation == Pokemon.Generation.RED) {
                     game.actionStack.remove(this);
                     game.insertAction(new DrawPokemonMenu.SelectedMenu(this, currPokemon, this.isStorageChest));
                  } else {
                     animsPaused = true;
                     game.insertAction(new WaitFrames(game, 8, new RunCode(() -> {
                        game.actionStack.remove(this);
                        animsPaused = false;
                     }, new DrawPokemonMenu.SelectedMenu(this, currPokemon, this.isStorageChest))));
                  }
               } else if (currPokemon.isEgg) {
                  game.insertAction(
                     new DisplayText(game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu)))
                  );
               } else if (this.item.equals("moomoo milk") || this.item.equals("berry juice") || this.item.equals("revive")) {
                  int diff = currPokemon.maxStats.get("hp") - currPokemon.currentStats.get("hp");
                  if (this.item.equals("revive")) {
                     if (currPokemon.currentStats.get("hp") > 0) {
                        game.insertAction(
                           new DisplayText(
                              game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu))
                           )
                        );
                        return;
                     }
                  } else if (diff <= 0 || currPokemon.currentStats.get("hp") <= 0) {
                     game.insertAction(
                        new DisplayText(
                           game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu))
                        )
                     );
                     return;
                  }

                  game.player.setItemAmount(this.item, game.player.getItemAmount(this.item) - 1);
                  if (game.player.getItemAmount(this.item) <= 0) {
                     game.player.removeItem(this.item);
                  }

                  int restoreAmt = 100;
                  if (this.item.equals("berry juice")) {
                     restoreAmt = 20;
                  } else if (this.item.equals("revive")) {
                     restoreAmt = currPokemon.maxStats.get("hp") / 2;
                  }

                  if (diff < restoreAmt) {
                     restoreAmt = diff;
                  }

                  game.insertAction(new PlayMusic("potion1", null));
                  Action nextAction = new RestoreHealth(
                     currPokemon,
                     -restoreAmt,
                     new DisplayText(
                        game,
                        currPokemon.nickname.toUpperCase(Locale.ROOT) + " gained " + restoreAmt + " hp!",
                        null,
                        null,
                        new SetField(this, "goAway", true, null)
                     )
                  );
                  if (game.battle.drawAction == null) {
                     nextAction.append(new DrawPokemonMenu.Outro(this.prevMenu));
                  } else {
                     game.battle.network.turnData = new Network.BattleTurnData();
                     game.battle.network.turnData.itemName = this.item;
                     System.out.println(this.prevMenu.prevMenu);
                     this.prevMenu.prevMenu.disabled = false;
                     nextAction.append(new DrawPokemonMenu.Outro(new Battle.DoTurn(game, Battle.DoTurn.Type.ITEM, this.prevMenu.prevMenu)));
                     this.prevMenu.prevMenu = null;
                  }

                  game.insertAction(nextAction);
               } else if (this.item.equals("rare candy")) {
                  if (currPokemon.level > 99) {
                     game.insertAction(
                        new DisplayText(
                           game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu))
                        )
                     );
                  } else {
                     game.player.setItemAmount(this.item, game.player.getItemAmount(this.item) - 1);
                     if (game.player.getItemAmount(this.item) <= 0) {
                        game.player.removeItem(this.item);
                     }

                     currPokemon.exp = currPokemon.calcExpForLevel(currPokemon.level + 1);
                     game.insertAction(
                        new GainExpAnimation(
                           currPokemon, new CheckEvo(currPokemon, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu)))
                        )
                     );
                  }
               } else if (!this.item.contains(" berry")) {
                  if (Game.evoStones.contains(this.item)) {
                     if (!this.ableWords.get(currIndex).equals("ABLE")) {
                        game.insertAction(
                           new DisplayText(
                              game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu))
                           )
                        );
                     } else {
                        game.player.setItemAmount(this.item, game.player.getItemAmount(this.item) - 1);
                        if (game.player.getItemAmount(this.item) <= 0) {
                           game.player.removeItem(this.item);
                        }

                        String evolveTo = Specie.gen2Evos.get(currPokemon.specie.name).get(this.item);
                        Action nextAction = new WaitFrames(
                           game,
                           61,
                           new WaitFrames(
                              game,
                              3,
                              new DisplayText(
                                 game,
                                 "What? " + currPokemon.nickname.toUpperCase(Locale.ROOT) + " is evolving!",
                                 null,
                                 true,
                                 false,
                                 new WaitFrames(
                                    game,
                                    51,
                                    new SetField(
                                       this,
                                       "goAway",
                                       true,
                                       new EvolutionAnim(
                                          currPokemon,
                                          evolveTo,
                                          new PlayMusic(
                                             currPokemon,
                                             new SplitAction(
                                                new SetField(game.musicController, "startEvolveMusic", true, null),
                                                new Battle.LoadAndPlayAnimation(
                                                   game,
                                                   "evolve",
                                                   null,
                                                   new WaitFrames(
                                                      game,
                                                      30,
                                                      new RunCode(
                                                         () -> {
                                                            if (game.player.hmPokemon == currPokemon && !game.player.currFieldMove.equals("")) {
                                                               game.player.swapSprites(game.player.hmPokemon);
                                                            }

                                                            currPokemon.evolveTo(evolveTo);
                                                            if (game.player.hmPokemon == currPokemon && !game.player.currFieldMove.equals("")) {
                                                               if (game.player.currFieldMove.equals("SURF")) {
                                                                  game.player.swapSpritesSurfing(game.player.hmPokemon);
                                                               } else {
                                                                  game.player.swapSprites(game.player.hmPokemon);
                                                               }
                                                            }

                                                            game.battle.oppPokemon = currPokemon;
                                                            EvolutionAnim.drawPostEvo = true;
                                                            game.insertAction(
                                                               new WaitFrames(game, 4, new PlayMusic(new Pokemon(evolveTo.toLowerCase(Locale.ROOT), 10), null))
                                                            );
                                                         },
                                                         new PokemonIntroAnim(
                                                            new DisplayText.Clear(
                                                               game,
                                                               new WaitFrames(
                                                                  game,
                                                                  3,
                                                                  new DisplayText(
                                                                     game,
                                                                     "Congratulations! Your " + currPokemon.nickname.toUpperCase(Locale.ROOT),
                                                                     null,
                                                                     true,
                                                                     true,
                                                                     new DisplayText.Clear(
                                                                        game,
                                                                        new WaitFrames(
                                                                           game,
                                                                           3,
                                                                           new DisplayText(
                                                                              game,
                                                                              "evolved into " + evolveTo.toUpperCase(Locale.ROOT) + "!",
                                                                              "fanfare2.ogg",
                                                                              true,
                                                                              false,
                                                                              new CheckMovesLearned(
                                                                                 currPokemon,
                                                                                 new DisplayText.Clear(
                                                                                    game,
                                                                                    new WaitFrames(
                                                                                       game,
                                                                                       3,
                                                                                       new EvolutionAnim.Done(
                                                                                          new SetField(
                                                                                             game.musicController,
                                                                                             "evolveMusicFadeout",
                                                                                             true,
                                                                                             new SetField(
                                                                                                game,
                                                                                                "playerCanMove",
                                                                                                true,
                                                                                                new SetField(game.player, "canMove", true, null)
                                                                                             )
                                                                                          )
                                                                                       )
                                                                                    )
                                                                                 )
                                                                              )
                                                                           )
                                                                        )
                                                                     )
                                                                  )
                                                               )
                                                            )
                                                         )
                                                      )
                                                   )
                                                )
                                             )
                                          )
                                       )
                                    )
                                 )
                              )
                           )
                        );
                        game.insertAction(nextAction);
                     }
                  } else {
                     game.insertAction(
                        new DisplayText(
                           game, "Dev note - invalid item.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu))
                        )
                     );
                  }
               } else if (currPokemon.status == null && currPokemon.volatileStatus.isEmpty()) {
                  game.insertAction(
                     new DisplayText(game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu)))
                  );
               } else {
                  String status = "";
                  String text = currPokemon.nickname.toUpperCase(Locale.ROOT);
                  if (this.item.contains("cheri")) {
                     status = "paralyze";
                     text = text + "' rid of paralysis";
                  } else if (this.item.contains("chesto")) {
                     status = "sleep";
                     text = text + " woke up";
                  } else if (this.item.contains("pecha")) {
                     text = text + "' cured of poison";
                  } else if (this.item.contains("rawst")) {
                     status = "burn";
                     text = text + "' burn was healed";
                  } else if (this.item.contains("aspear")) {
                     status = "freeze";
                     text = text + " was defrosted";
                  } else if (this.item.contains("persim")) {
                     status = "confuse";
                     text = text + " came to it' senses";
                  } else {
                     text = text + "' health returned";
                  }

                  boolean condition = !status.equals(currPokemon.status);
                  if (this.item.contains("pecha")) {
                     condition = !"poison".equals(currPokemon.status) && !"toxic".equals(currPokemon.status);
                  } else if (this.item.contains("lum")) {
                     condition = currPokemon.status == null;
                  } else if (this.item.contains("persim")) {
                     condition = !currPokemon.volatileStatus.contains("confuse");
                  }

                  if (condition) {
                     game.insertAction(
                        new DisplayText(
                           game, "It wonì have any effect.", null, null, new SetField(this, "goAway", true, new DrawPokemonMenu.Outro(this.prevMenu))
                        )
                     );
                  } else {
                     game.player.setItemAmount(this.item, game.player.getItemAmount(this.item) - 1);
                     if (game.player.getItemAmount(this.item) <= 0) {
                        game.player.removeItem(this.item);
                     }

                     Action nextAction = new DisplayText(game, text + ".", "fanfare1.ogg", null, new SetField(this, "goAway", true, null));
                     if (game.battle.drawAction == null) {
                        nextAction.append(new DrawPokemonMenu.Outro(this.prevMenu));
                     } else {
                        game.battle.network.turnData = new Network.BattleTurnData();
                        game.battle.network.turnData.itemName = this.item;
                        this.prevMenu.prevMenu.disabled = false;
                        nextAction.append(new DrawPokemonMenu.Outro(new Battle.DoTurn(game, Battle.DoTurn.Type.ITEM, this.prevMenu.prevMenu)));
                        this.prevMenu.prevMenu = null;
                     }

                     game.insertAction(nextAction);
                     if (this.item.contains("persim")) {
                        currPokemon.volatileStatus.remove("confuse");
                     } else {
                        currPokemon.status = null;
                     }
                  }
               }
            } else {
               if (InputProcessor.bJustPressed) {
                  if (this.prevMenu != null) {
                     lastIndex = currIndex;
                     game.actionStack.remove(this);
                     game.insertAction(new DrawPokemonMenu.Outro(this.prevMenu));
                     return;
                  }

                  if (this.isStorageChest) {
                     lastIndex = currIndex;
                     game.actionStack.remove(this);
                     Action nextAction = new DrawPokemonMenu.Outro(new SetField(game, "playerCanMove", true, null));
                     if (storageChestTile != null) {
                        nextAction.append(
                           new WaitFrames(
                              game, 10, new CallMethod(storageChestTile.overSprite, "setRegion", new Object[]{0, 0, 16, 32}, new PlayMusic("exit1", null))
                           )
                        );
                     }

                     game.insertAction(nextAction);
                     return;
                  }
               }
            }
         }
      }
   }

   static class Intro extends Action {
      public Action.Layer layer = Action.Layer.gui_110;
      int duration = 18;
      Sprite bgSprite;

      public Intro(Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
         this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      }

      public Intro(int duration, Action nextAction) {
         this(nextAction);
         this.duration = duration;
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
         this.bgSprite.draw(game.uiBatch);
         this.duration--;
         if (this.duration <= 0) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   static class Outro extends Action {
      Menu prevMenu;
      public Action.Layer layer = Action.Layer.gui_110;
      int duration = 34;
      Sprite bgSprite;

      public Outro(Action nextAction) {
         this((Menu)null);
         this.nextAction = nextAction;
      }

      public Outro(Menu prevMenu) {
         super();
         this.prevMenu = prevMenu;
         if (this.prevMenu != null) {
            this.prevMenu.drawArrowWhite = true;
         }

         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
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
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         this.duration--;
         if (this.duration > 0) {
            this.bgSprite.draw(game.uiBatch);
         }

         if (this.duration <= 0) {
            if (this.prevMenu != null) {
               game.insertAction(this.prevMenu);
               this.prevMenu.disabled = false;
               this.prevMenu.drawArrowWhite = false;
            }

            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   static class SelectedMenu extends Menu {
      Sprite arrow;
      Sprite textBoxTop;
      Sprite textBoxMiddle;
      Sprite textBoxBottom;
      Pokemon pokemon;
      public Action.Layer layer = Action.Layer.gui_105;
      Map<Integer, Vector2> getCoords = new HashMap<>();
      int curr;
      Vector2 newPos;
      ArrayList<String> words = new ArrayList<>();
      int textboxDelay = 0;
      boolean isStorageChest = false;

      public SelectedMenu(Menu prevMenu, Pokemon pokemon) {
         this(prevMenu, pokemon, false);
      }

      public SelectedMenu(Menu prevMenu, Pokemon pokemon, boolean isStorageChest) {
         this.prevMenu = prevMenu;
         this.pokemon = pokemon;
         this.isStorageChest = isStorageChest;
         Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
         this.arrow = new Sprite(text, 0, 0, 5, 7);
         text = TextureCache.get(Gdx.files.internal("menu/gsc/selected_menu_top.png"));
         this.textBoxTop = new Sprite(text, 0, 0, 112, 24);
         text = TextureCache.get(Gdx.files.internal("menu/gsc/selected_menu_middle.png"));
         this.textBoxMiddle = new Sprite(text, 0, 0, 112, 16);
         text = TextureCache.get(Gdx.files.internal("menu/gsc/selected_menu_bottom.png"));
         this.textBoxBottom = new Sprite(text, 0, 0, 112, 24);
      }

      public void swapPokemon(Game game, Pokemon pokemon, boolean toParty) {
         if (game.coop != null) return; // Shared storage needs its own transactional protocol.
         if (toParty) {
            game.player.pokemon.add(pokemon);
            DrawPokemonMenu.storageChestTile.routeBelongsTo.storedPokemon.remove(pokemon);
         } else {
            DrawPokemonMenu.storageChestTile.routeBelongsTo.storedPokemon.add(pokemon);
            game.player.pokemon.remove(pokemon);
         }
      }

      public void releasePokemon(Game game, Pokemon pokemon, boolean inParty) {
         if (game.coop != null && !inParty) return;
         if (inParty) {
            game.player.pokemon.remove(pokemon);
         } else {
            DrawPokemonMenu.storageChestTile.routeBelongsTo.storedPokemon.remove(pokemon);
         }
      }

      public Action getAction(Game game, String word, Menu prevMenu) {
         if (game.coop != null && (word.equals("STORE") || word.equals("TAKE")
                 || word.equals("RELEASE") && DrawPokemonMenu.allPokemon != game.player.pokemon)) {
            game.insertAction(this.prevMenu);
            return new DisplayText(game, "Shared storage is unavailable in LAN co-op. Use your party or DROP instead.", null, null,
                    new SetField(this.prevMenu, "disabled", false, null));
         }
         if (!word.equals("STORE") && !word.equals("TAKE")) {
            if (word.equals("RELEASE")) {
               if (DrawPokemonMenu.allPokemon == game.player.pokemon && game.player.pokemon.size() <= 1) {
                  this.disabled = true;
                  game.insertAction(this.prevMenu);
                  return new PlayMusic(
                     "error1",
                     new DisplayText(game, "You need at least 1 POKéMON in your party.", null, null, new SetField(this.prevMenu, "disabled", false, null))
                  );
               } else {
                  Pokemon pokemon = DrawPokemonMenu.allPokemon.get(DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex);
                  game.insertAction(this.prevMenu);
                  boolean inParty = DrawPokemonMenu.allPokemon == game.player.pokemon;
                  int startIndex = 1;
                  return new DisplayText(
                     game,
                     "Release " + pokemon.nickname + " to the wild? This canì be undone!",
                     null,
                     true,
                     false,
                     new DrawYesNoMenu(
                        null,
                        startIndex,
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game,
                              3,
                              new PlayMusic(
                                 pokemon,
                                 new CallMethod(
                                    this, "releasePokemon", new Object[]{game, pokemon, inParty}, new SetField(this.prevMenu, "disabled", false, null)
                                 )
                              )
                           )
                        ),
                        new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(this.prevMenu, "disabled", false, null)))
                     )
                  );
               }
            } else {
               if (word.equals("NICKNAME")) {
                  Pokemon pokemon = DrawPokemonMenu.allPokemon.get(DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex);
                  game.insertAction(this.prevMenu);
                  return pokemon.new SetNickname(new WaitFrames(game, 10, new SetField(this.prevMenu, "disabled", false, null)));
               }

               if (word.equals("STATS")) {
                  Pokemon currPokemon = DrawPokemonMenu.allPokemon.get(DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex);
                  return new DrawStatsScreen.Intro(new DrawStatsScreen(game, currPokemon, prevMenu));
               }

               if (word.equals("SWITCH")) {
                  Pokemon currPokemon = DrawPokemonMenu.allPokemon.get(DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex);
                  if (game.battle.drawAction == null) {
                     return new DrawPokemonMenu.SelectedMenu.Switch(prevMenu);
                  }

                  if (game.player.currPokemon.currentStats.get("hp") <= 0
                     || game.player.currPokemon.trappedBy == null && game.player.currPokemon.cantEscapeBy == null) {
                     if (currPokemon.currentStats.get("hp") <= 0 || currPokemon.isEgg) {
                        this.disabled = true;
                        game.insertAction(this.prevMenu);
                        return new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null));
                     }

                     if (currPokemon == game.player.currPokemon) {
                        this.disabled = true;
                        game.insertAction(this.prevMenu);
                        return new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null));
                     }

                     if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.SWITCH, DrawPokemonMenu.currIndex));
                     }

                     return new SplitAction(
                        new PlayMusic("click1", null),
                        new Battle.DoTurn(game, Battle.DoTurn.Type.SWITCH, new WaitFrames(game, 15, new DrawBattleMenuNormal(game, null)))
                     );
                  } else {
                     this.disabled = true;
                     game.insertAction(this.prevMenu);
                     return new PlayMusic(
                        "error1",
                        new DisplayText(
                           game,
                           game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + " is trapped!",
                           null,
                           null,
                           new SetField(this.prevMenu, "disabled", false, null)
                        )
                     );
                  }
               } else {
                  if (game.player.currFieldMove.equals("FLY") && !word.equals("TELEPORT")) {
                     this.disabled = true;
                     game.insertAction(this.prevMenu);
                     return new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null));
                  }

                  if (game.player.currFieldMove.equals("SURF") && !word.equals("SURF") && !word.equals("FLY") && !word.equals("TELEPORT")) {
                     this.disabled = true;
                     game.insertAction(this.prevMenu);
                     return new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null));
                  }

                  if (game.player.isSleeping) {
                     this.disabled = true;
                     game.insertAction(this.prevMenu);
                     return new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null));
                  }

                  if (word.equals("DROP")) {
                     Vector2 pos = game.player.facingPos();
                     Tile currTile = game.map.tiles.get(pos);
                     Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                     if (currTile == null || currTile.isSolid && !currTile.name.contains("water") || currTile.isLedge || game.map.pokemonAt(pos) != null) {
                        this.disabled = true;
                        return new DisplayText(
                           game, "Canì place here - something is in the way.", null, null, new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                        );
                     }

                     if (game.player.pokemon.size() <= 1) {
                        this.disabled = true;
                        game.insertAction(this.prevMenu);
                        return new PlayMusic(
                           "error1",
                           new DisplayText(game, "You need at least 1 POKéMON in your party.", null, null, new SetField(this.prevMenu, "disabled", false, null))
                        );
                     }

                     if (currTile.routeBelongsTo != null && currTile.routeBelongsTo.isDungeon) {
                        this.disabled = true;
                        return new DisplayText(
                           game, "Canì leave Pokemon in a dangerous area.", null, null, new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                        );
                     }

                     if (currTile.name.contains("water")) {
                        ArrayList<String> swimMons = new ArrayList<>();
                        swimMons.add("azurill");
                        swimMons.add("dratini");
                        swimMons.add("dragonair");
                        swimMons.add("dragonite");
                        swimMons.add("anorith");
                        swimMons.add("armaldo");
                        swimMons.add("lileep");
                        swimMons.add("cradily");
                        swimMons.add("araichu");
                        if (!pokemon.types.contains("WATER") && !swimMons.contains(pokemon.specie.name)) {
                           this.disabled = true;
                           return new DisplayText(
                              game,
                              pokemon.nickname.toUpperCase(Locale.ROOT) + " canì be placed in water.",
                              null,
                              null,
                              new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                           );
                        }
                     } else if (Pokemon.onlySwim.contains(pokemon.specie.name)) {
                        this.disabled = true;
                        return new DisplayText(
                           game,
                           pokemon.nickname.toUpperCase(Locale.ROOT) + " must be placed in water.",
                           null,
                           null,
                           new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                        );
                     }

                     boolean hasOneHealthyPokemon = false;

                     for (Pokemon otherPokemon : game.player.pokemon) {
                        if (pokemon != otherPokemon && otherPokemon.currentStats.get("hp") > 0 && !otherPokemon.isEgg) {
                           hasOneHealthyPokemon = true;
                           break;
                        }
                     }

                     if (!hasOneHealthyPokemon) {
                        this.disabled = true;
                        game.insertAction(this.prevMenu);
                        return new PlayMusic(
                           "error1",
                           new DisplayText(
                              game, "You need at least 1 healthy POKéMON in your party.", null, null, new SetField(this.prevMenu, "disabled", false, null)
                           )
                        );
                     }

                     pokemon.position = pos.cpy();
                     pokemon.mapTiles = game.map.tiles;
                     game.map.indexPokemon(pos, pokemon);
                     if (game.player.hmPokemon == pokemon) {
                        if (!game.player.currFieldMove.equals("")) {
                           game.player.swapSprites(game.player.hmPokemon);
                        }

                        game.player.hmPokemon.removeDrawActions(game);
                        game.player.hmPokemon = null;
                        game.player.currFieldMove = "";
                     }

                     return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(this.prevMenu, new PlayMusic(pokemon, pokemon.new RemoveFromInventory()));
                  } else {
                     if (game.player.hmPokemon == game.player.pokemon.get(DrawPokemonMenu.currIndex) && word.equals(game.player.currFieldMove)) {
                        this.disabled = true;
                        game.insertAction(this.prevMenu);
                        return new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null));
                     }

                     if (game.player.hmPokemon != null && !word.equals("TELEPORT")) {
                        if (!game.player.currFieldMove.equals("")) {
                           game.player.swapSprites(game.player.hmPokemon);
                           game.player.currPlanting = null;
                        }

                        game.player.hmPokemon.removeDrawActions(game);
                        game.player.hmPokemon = null;
                        game.player.currFieldMove = "";
                     }

                     if (word.equals("FLY")) {
                        if (game.map.tiles != game.map.overworldTiles) {
                           game.insertAction(this.prevMenu);
                           return new PlayMusic(
                              "error1", new DisplayText(game, "Canì do this here!", null, null, new SetField(this.prevMenu, "disabled", false, null))
                           );
                        }

                        PlayerStanding standingAction = null;

                        for (Action action : game.actionStack) {
                           if (PlayerStanding.class.isInstance(action)) {
                              standingAction = (PlayerStanding)action;
                              break;
                           }
                        }

                        game.actionStack.remove(standingAction);
                        game.player.currFieldMove = "FLY";
                        return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                           this.prevMenu,
                           new PlayMusic(this.pokemon, new SplitAction(new WaitFrames(game, 40, game.player.new Flying(this.pokemon, true, null)), null))
                        );
                     } else if (word.equals("DIG")) {
                        Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                        game.player.swapSprites(pokemon);
                        game.insertAction(pokemon.new Follow(game.player));
                        game.player.currFieldMove = "DIG";
                        game.player.buildTiles = game.player.terrainTiles;

                        while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
                           game.player.buildTileIndex--;
                        }

                        game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                        return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                           this.prevMenu,
                           new PlayMusic(
                              pokemon,
                              new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " used DIG! Press C and V to select terrain.", null, null, null)
                           )
                        );
                     } else {
                        if (word.equals("BUILD")) {
                           Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                           game.player.swapSprites(pokemon);
                           game.insertAction(pokemon.new Follow(game.player));
                           game.player.currFieldMove = "BUILD";
                           game.player.updateBuildTiles(game);
                           return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                              this.prevMenu,
                              new PlayMusic(
                                 pokemon,
                                 new DisplayText(
                                    game, pokemon.nickname.toUpperCase(Locale.ROOT) + " used BUILD! Press C and V to select tiles.", null, null, null
                                 )
                              )
                           );
                        }

                        if (word.equals("PAINT")) {
                           Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                           game.player.swapSprites(pokemon);
                           game.insertAction(pokemon.new Follow(game.player));
                           game.player.currFieldMove = "BUILD";
                           game.player.buildTiles = game.player.smeargleBuildTiles;

                           while (game.player.buildTileIndex > 0 && game.player.buildTileIndex >= game.player.buildTiles.size()) {
                              game.player.buildTileIndex--;
                           }

                           game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                           return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                              this.prevMenu,
                              new PlayMusic(
                                 pokemon,
                                 new DisplayText(
                                    game, pokemon.nickname.toUpperCase(Locale.ROOT) + " used PAINT! Press C and V to select designs.", null, null, null
                                 )
                              )
                           );
                        } else {
                           if (word.equals("CHARM")) {
                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.insertAction(pokemon.new Follow(game.player));
                              game.player.currFieldMove = "CHARM";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using CHARM!", null, null, null))
                              );
                           }

                           if (word.equals("CUT")) {
                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.insertAction(pokemon.new Follow(game.player));
                              game.player.currFieldMove = "CUT";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using CUT!", null, null, null))
                              );
                           }

                           if (word.equals("SMASH")) {
                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.insertAction(pokemon.new Follow(game.player));
                              game.player.currFieldMove = "SMASH";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(
                                    pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using ROCK SMASH!", null, null, null)
                                 )
                              );
                           }

                           if (word.equals("REPEL")) {
                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.insertAction(pokemon.new Follow(game.player));
                              game.player.currFieldMove = "REPEL";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using REPEL!", null, null, null))
                              );
                           }

                           if (word.equals("POWER")) {
                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.insertAction(pokemon.new Follow(game.player));
                              game.player.currFieldMove = "POWER";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(
                                    pokemon,
                                    new DisplayText(
                                       game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using POWER! Power machinery by pressing Z.", null, null, null
                                    )
                                 )
                              );
                           }

                           if (word.equals("HEADBUTT")) {
                              if (game.type == Game.Type.CLIENT) {
                                 game.client.sendTCP(new Network.UseHM(game.player.network.id, DrawPokemonMenu.currIndex, word));
                              }

                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.insertAction(pokemon.new Follow(game.player));
                              game.player.currFieldMove = "HEADBUTT";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(
                                    pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using HEADBUTT!", null, null, null)
                                 )
                              );
                           } else if (word.equals("RIDE")) {
                              if (game.type == Game.Type.CLIENT) {
                                 game.client.sendTCP(new Network.UseHM(game.player.network.id, DrawPokemonMenu.currIndex, word));
                              }

                              Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                              game.player.swapSprites(pokemon);
                              game.player.currFieldMove = "RIDE";
                              return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                 this.prevMenu,
                                 new PlayMusic(pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using RIDE!", null, null, null))
                              );
                           } else {
                              if (word.equals("ATTACK")) {
                                 Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                                 game.player.swapSprites(pokemon);
                                 game.insertAction(pokemon.new Follow(game.player));
                                 game.player.currFieldMove = "ATTACK";
                                 return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                    this.prevMenu,
                                    new PlayMusic(
                                       pokemon, new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is using ATTACK!", null, null, null)
                                    )
                                 );
                              }

                              if (word.equals("TELEPORT")) {
                                 return new DrawPokemonMenu.Intro(new DrawMiniMap(game, this.pokemon, this.prevMenu));
                              }

                              if (word.equals("SURF")) {
                                 Vector2 pos = game.player.facingPos();
                                 Pokemon facingPokemon = game.map.pokemon.get(pos);
                                 if (facingPokemon != null && facingPokemon.mapTiles == game.map.tiles) {
                                    this.disabled = true;
                                    return new DisplayText(
                                       game,
                                       "Canì SURF here - something is in the way.",
                                       null,
                                       null,
                                       new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                                    );
                                 } else {
                                    Tile facingTile = game.map.tiles.get(pos);
                                    if (facingTile == null) {
                                       this.disabled = true;
                                       return new DisplayText(
                                          game,
                                          "Canì SURF here - something is in the way.",
                                          null,
                                          null,
                                          new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                                       );
                                    } else if (!facingTile.isWater) {
                                       this.disabled = true;
                                       return new DisplayText(
                                          game, "Canì SURF on land.", null, null, new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                                       );
                                    } else {
                                       game.player.acceptInput = false;
                                       game.playerCanMove = true;
                                       Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                                       game.player.swapSpritesSurfing(pokemon);
                                       game.player.currFieldMove = "SURF";
                                       game.insertAction(this.prevMenu);
                                       return new PlayMusic(
                                          pokemon,
                                          new DisplayText(
                                             game,
                                             pokemon.nickname.toUpperCase(Locale.ROOT) + " is using SURF!",
                                             null,
                                             null,
                                             new SetField(
                                                this.prevMenu,
                                                "goAway",
                                                true,
                                                new DrawPokemonMenu.Outro(
                                                   new PlayerMoving(game, game.player, false, new SetField(game.player, "acceptInput", true, null))
                                                )
                                             )
                                          )
                                       );
                                    }
                                 }
                              } else if (word.equals("FOLLOW")) {
                                 Pokemon pokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
                                 if (Pokemon.onlySwim.contains(pokemon.specie.name)) {
                                    this.disabled = true;
                                    return new DisplayText(
                                       game,
                                       pokemon.nickname.toUpperCase(Locale.ROOT) + " canì walk on land.",
                                       null,
                                       null,
                                       new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                                    );
                                 } else {
                                    game.insertAction(pokemon.new Follow(game.player));
                                    game.player.hmPokemon = pokemon;
                                    return new DrawPokemonMenu.SelectedMenu.ExitAfterActions(
                                       this.prevMenu,
                                       new PlayMusic(
                                          pokemon,
                                          new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is following you around!", null, null, null)
                                       )
                                    );
                                 }
                              } else {
                                 return null;
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else if (DrawPokemonMenu.allPokemon != game.player.pokemon) {
            if (game.player.pokemon.size() > 5) {
               this.disabled = true;
               game.insertAction(this.prevMenu);
               return new PlayMusic("error1", new DisplayText(game, "Your party is full!", null, null, new SetField(this.prevMenu, "disabled", false, null)));
            } else {
               Pokemon pokemon = DrawPokemonMenu.allPokemon.get(DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex);
               game.insertAction(this.prevMenu);
               return new DisplayText(
                  game,
                  "Add to your party?",
                  null,
                  true,
                  false,
                  new DrawYesNoMenu(
                     null,
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new PlayMusic(
                              "ball_switch1",
                              new CallMethod(this, "swapPokemon", new Object[]{game, pokemon, true}, new SetField(this.prevMenu, "disabled", false, null))
                           )
                        )
                     ),
                     new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(this.prevMenu, "disabled", false, null)))
                  )
               );
            }
         } else {
            Pokemon pokemon = DrawPokemonMenu.allPokemon.get(DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex);
            boolean hasOneHealthyPokemon = false;

            for (Pokemon otherPokemon : game.player.pokemon) {
               if (pokemon != otherPokemon && otherPokemon.currentStats.get("hp") > 0 && !otherPokemon.isEgg) {
                  hasOneHealthyPokemon = true;
                  break;
               }
            }

            if (!hasOneHealthyPokemon) {
               this.disabled = true;
               game.insertAction(this.prevMenu);
               return new PlayMusic(
                  "error1",
                  new DisplayText(game, "You need at least 1 POKéMON in your party.", null, null, new SetField(this.prevMenu, "disabled", false, null))
               );
            } else if (game.player.hmPokemon == pokemon && game.player.currFieldMove.equals("SURF")) {
               this.disabled = true;
               game.insertAction(this.prevMenu);
               return new PlayMusic(
                  "error1", new DisplayText(game, "Canì store a Pokemon that' using SURF.", null, null, new SetField(this.prevMenu, "disabled", false, null))
               );
            } else {
               game.insertAction(this.prevMenu);
               return new DisplayText(
                  game,
                  "Move to storage?",
                  null,
                  true,
                  false,
                  new DrawYesNoMenu(null, new DisplayText.Clear(game, new WaitFrames(game, 3, new PlayMusic("ball_switch1", new RunCode(() -> {
                     if (game.player.hmPokemon == pokemon) {
                        if (!game.player.currFieldMove.equals("")) {
                           game.player.swapSprites(game.player.hmPokemon);
                        }

                        game.player.hmPokemon.removeDrawActions(game);
                        game.player.hmPokemon = null;
                        game.player.currFieldMove = "";
                     }

                     this.swapPokemon(game, pokemon, false);
                     this.prevMenu.disabled = false;
                  }, null)))), new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(this.prevMenu, "disabled", false, null))))
               );
            }
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
      public void firstStep(Game game) {
         if (this.isStorageChest) {
            if (DrawPokemonMenu.allPokemon == game.player.pokemon) {
               this.words.add("STORE");
            } else {
               this.words.add("TAKE");
            }

            this.words.add("RELEASE");
            this.words.add("NICKNAME");
            this.words.add("STATS");
         } else {
            ArrayList<String> fieldMoves = new ArrayList<>(this.pokemon.hms);
            fieldMoves.remove("FLASH");
            fieldMoves.add("FOLLOW");
            if (game.battle.drawAction == null) {
               for (String hm : fieldMoves) {
                  this.words.add(hm);
               }

               this.words.add("STATS");
            }
         }

         this.words.add("SWITCH");
         if (game.battle.drawAction == null && !this.isStorageChest) {
            this.words.add("DROP");
         }

         this.words.add("CANCEL");

         for (int i = 0; i < this.words.size(); i++) {
            this.getCoords.put(i, new Vector2(57.0F, -7 + 16 * (this.words.size() - i)));
         }

         this.newPos = this.getCoords.get(0);
         this.arrow.setPosition(this.newPos.x, this.newPos.y);
         this.curr = 0;
      }

      @Override
      public void step(Game game) {
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         if (this.textboxDelay < 1) {
            this.textboxDelay++;
         } else {
            if (InputProcessor.upJustPressed) {
               if (this.curr > 0) {
                  this.curr--;
                  this.newPos = this.getCoords.get(this.curr);
               }
            } else if (InputProcessor.downJustPressed && this.curr < this.words.size() - 1) {
               this.curr++;
               this.newPos = this.getCoords.get(this.curr);
            }

            for (int i = 0; i < this.words.size(); i++) {
               if (i == 0) {
                  game.uiBatch.draw(this.textBoxTop, 48.0F, 38 + 16 * (this.words.size() - 3));
               } else if (i == this.words.size() - 1) {
                  game.uiBatch.draw(this.textBoxBottom, 48.0F, 0.0F);
               } else {
                  game.uiBatch.draw(this.textBoxMiddle, 48.0F, 23 + 16 * (this.words.size() - i - 2));
               }

               String word = this.words.get(i);

               for (int j = 0; j < word.length(); j++) {
                  char letter = word.charAt(j);
                  Sprite letterSprite = game.textDict.get(letter);
                  game.uiBatch.draw(letterSprite, 64 + 8 * j, 40 - 16 * (i - this.words.size() + 3));
               }
            }

            game.uiBatch.draw(this.arrow, this.newPos.x, this.newPos.y);
            if (InputProcessor.aJustPressed) {
               String word = this.words.get(this.curr);
               if (!"CANCEL".equals(word)) {
                  Action action = this.getAction(game, word, this.prevMenu);
                  game.actionStack.remove(this);
                  game.insertAction(action);
               } else {
                  DrawPokemonMenu.avatarAnimCounter = 24;
                  DrawPokemonMenu.avatarHopAnimCounter = 0;
                  DrawPokemonMenu.avatarAnimCounterGen2 = 0;
                  DrawPokemonMenu.avatarYellowHealthAnimCounter = 0;
                  DrawPokemonMenu.avatarRedHealthAnimCounter = 0;
                  game.insertAction(new PlayMusic("click1", null));
                  game.actionStack.remove(this);
                  if (game.battle.drawAction == null && !this.isStorageChest) {
                     game.insertAction(new DrawPokemonMenu.Outro(this.prevMenu.prevMenu));
                  } else {
                     this.prevMenu.disabled = false;
                     game.insertAction(this.prevMenu);
                  }
               }
            } else if (InputProcessor.bJustPressed) {
               game.insertAction(new PlayMusic("click1", null));
               DrawPokemonMenu.avatarAnimCounter = 24;
               DrawPokemonMenu.avatarHopAnimCounter = 0;
               DrawPokemonMenu.avatarAnimCounterGen2 = 0;
               DrawPokemonMenu.avatarYellowHealthAnimCounter = 0;
               DrawPokemonMenu.avatarRedHealthAnimCounter = 0;
               game.actionStack.remove(this);
               game.insertAction(new DrawPokemonMenu.SelectedMenu.Outro(this.prevMenu));
            }
         }
      }

      static class ExitAfterActions extends Menu {
         public Action.Layer layer = Action.Layer.gui_106;
         boolean firstStep = true;

         public ExitAfterActions(Menu prevMenu, Action nextAction) {
            this.prevMenu = prevMenu;
            this.nextAction = nextAction;
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
               game.insertAction(this.nextAction);
               this.firstStep = false;
            }

            if (this.prevMenu != null) {
               this.prevMenu.step(game);
            }

            Action action = this.nextAction;

            while (action != null && !game.actionStack.contains(action)) {
               action = action.nextAction;
            }

            if (action == null) {
               DrawPokemonMenu.lastIndex = this.prevMenu.currIndex;
               DrawPokemonMenu.lastIndex = this.prevMenu.currIndex;
               game.actionStack.remove(this);
               game.insertAction(new DrawPokemonMenu.Outro((Menu)null));
               game.insertAction(new WaitFrames(game, 30, new SetField(game, "playerCanMove", true, null)));
            }
         }
      }

      static class Outro extends Action {
         Menu prevMenu;
         public Action.Layer layer = Action.Layer.gui_110;
         int duration = 9;

         public Outro(Menu prevMenu) {
            super();
            this.prevMenu = prevMenu;
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
            this.prevMenu.drawArrowWhite = false;
            if (this.prevMenu != null) {
               this.prevMenu.step(game);
            }

            this.duration--;
            if (this.duration == 6) {
               DrawPokemonMenu.drawChoosePokemonText = false;
            } else if (this.duration == 3) {
               DrawPokemonMenu.drawChoosePokemonText = true;
            } else if (this.duration <= 0) {
               game.insertAction(this.prevMenu);
               this.prevMenu.disabled = false;
               game.actionStack.remove(this);
            }
         }
      }

      static class Switch extends Menu {
         Sprite arrow;
         public Action.Layer layer = Action.Layer.gui_105;
         Map<Integer, Vector2> arrowCoords;
         int curr;
         int scrollIndex;
         int startPosition;
         Vector2 newPos;
         int timer = 0;
         public int upTimer = 0;
         public int downTimer = 0;
         public static int index = -1;

         public Switch(Menu prevMenu) {
            this.prevMenu = prevMenu;
            Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
            this.arrow = new Sprite(text, 0, 0, 5, 7);
            this.arrowCoords = new HashMap<>();

            for (int i = 0; i < 6; i++) {
               this.arrowCoords.put(i, new Vector2(1.0F, 128 - 16 * i));
            }

            this.scrollIndex = DrawPokemonMenu.scrollIndex;
            this.curr = DrawPokemonMenu.currIndex;
            this.startPosition = this.scrollIndex + this.curr;
            this.newPos = this.arrowCoords.get(this.curr);
            this.arrow.setPosition(this.newPos.x, this.newPos.y);
            index = this.curr;
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

            this.arrow.setPosition(this.newPos.x, this.newPos.y);
            game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
            if (!this.disabled) {
               if (this.timer < 10) {
                  this.timer++;
               }

               DrawPokemonMenu.avatarAnimCounter--;
               if (DrawPokemonMenu.avatarAnimCounter <= 0) {
                  DrawPokemonMenu.avatarAnimCounter = 11;
               }

               if (this.timer == 2) {
                  DrawPokemonMenu.drawChoosePokemonText = false;
               } else if (this.timer > 4) {
                  if (DrawPokemonMenu.generation == Pokemon.Generation.RED) {
                     char[] textArray = "Move POKéMON".toCharArray();

                     for (int j = 0; j < textArray.length; j++) {
                        Sprite letterSprite = game.textDict.get(textArray[j]);
                        game.uiBatch.draw(letterSprite, 8 + 8 * j, 24.0F);
                     }

                     textArray = "where?".toCharArray();

                     for (int j = 0; j < textArray.length; j++) {
                        Sprite letterSprite = game.textDict.get(textArray[j]);
                        game.uiBatch.draw(letterSprite, 8 + 8 * j, 8.0F);
                     }
                  } else {
                     char[] textArray = "Move to where?".toCharArray();

                     for (int j = 0; j < textArray.length; j++) {
                        Sprite letterSprite = game.textDict.get(textArray[j]);
                        game.uiBatch.draw(letterSprite, 8 + 8 * j, 8.0F);
                     }
                  }
               }

               if (this.timer > 8) {
                  if (!InputProcessor.upJustPressed && this.upTimer < 20) {
                     if ((InputProcessor.downJustPressed || this.downTimer >= 20) && this.curr + this.scrollIndex < DrawPokemonMenu.allPokemon.size() - 1) {
                        if (this.curr < 5) {
                           this.curr++;
                           DrawPokemonMenu.currIndex = this.curr;
                           this.newPos = this.arrowCoords.get(this.curr);
                        } else {
                           this.scrollIndex++;
                        }
                     }
                  } else if (this.curr + this.scrollIndex > 0) {
                     if (this.curr > 0) {
                        this.curr--;
                        DrawPokemonMenu.currIndex = this.curr;
                        this.newPos = this.arrowCoords.get(this.curr);
                     } else {
                        this.scrollIndex--;
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

                  if (InputProcessor.aJustPressed) {
                     Pokemon movePokemon = DrawPokemonMenu.allPokemon.get(this.startPosition);
                     Pokemon movePokemon2 = DrawPokemonMenu.allPokemon.get(this.curr + this.scrollIndex);
                     DrawPokemonMenu.allPokemon.remove(this.startPosition);
                     DrawPokemonMenu.allPokemon.add(this.startPosition, movePokemon2);
                     DrawPokemonMenu.allPokemon.remove(this.curr + this.scrollIndex);
                     DrawPokemonMenu.allPokemon.add(this.curr + this.scrollIndex, movePokemon);
                     if (DrawPokemonMenu.allPokemon == game.player.pokemon) {
                        if (this.startPosition == 0) {
                           game.player.currPokemon = movePokemon2;
                        }

                        if (this.curr == 0) {
                           game.player.currPokemon = movePokemon;
                        }
                     }

                     this.disabled = true;
                     game.insertAction(new PlayMusic("click1", null));
                     index = -1;
                     DrawPokemonMenu.avatarAnimCounter = 24;
                     game.actionStack.remove(this);
                     game.insertAction(new DrawPokemonMenu.SelectedMenu.Switch.Outro(this));
                     if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.UseHM(game.player.network.id, this.startPosition, "SWITCH", this.curr));
                     }

                     return;
                  }

                  if (InputProcessor.bJustPressed) {
                     this.disabled = true;
                     game.insertAction(new PlayMusic("click1", null));
                     index = -1;
                     DrawPokemonMenu.avatarAnimCounter = 24;
                     game.actionStack.remove(this);
                     game.insertAction(new DrawPokemonMenu.SelectedMenu.Switch.Outro(this));
                     return;
                  }
               }
            }
         }

         static class Outro extends Menu {
            public Action.Layer layer = Action.Layer.gui_110;
            int timer = 0;

            public Outro(Menu prevMenu) {
               this.prevMenu = prevMenu;
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

               this.timer++;
               if (this.timer == 1) {
                  this.prevMenu = this.prevMenu.prevMenu;
                  this.prevMenu.drawArrowWhite = false;
               } else if (this.timer == 3) {
                  DrawPokemonMenu.drawChoosePokemonText = true;
               } else if (this.timer >= 7) {
                  DrawPokemonMenu.avatarAnimCounter = 25;
                  game.actionStack.remove(this);
                  game.insertAction(this.prevMenu);
                  this.prevMenu.disabled = false;
               }
            }
         }
      }
   }
}
