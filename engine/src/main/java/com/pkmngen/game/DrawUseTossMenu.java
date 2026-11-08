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

class DrawUseTossMenu extends Menu {
   Sprite arrow;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_105;
   Map<Integer, Vector2> getCoords = new HashMap<>();
   int curr;
   Vector2 newPos;
   Sprite helperSprite;
   int cursorDelay;
   String itemName;
   Menu prevMenu;
   ArrayList<String> words = new ArrayList<>();
   Vector2 offset;

   public DrawUseTossMenu(Game game, Menu prevMenu, String itemName) {
      this(game, prevMenu, itemName, new Vector2());
   }

   public DrawUseTossMenu(Game game, Menu prevMenu, String itemName, Vector2 offset) {
      this.prevMenu = prevMenu;
      this.itemName = itemName;
      this.offset = offset;
      this.cursorDelay = 0;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("menu/usetoss_menu2.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      this.getCoords.put(0, new Vector2(113.0F, 48.0F));
      this.getCoords.put(1, new Vector2(113.0F, 32.0F));
      this.newPos = new Vector2(113.0F, 48.0F);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.curr = 0;
      this.words.add("USE");
      this.words.add("DROP");
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

      game.uiBatch.draw(this.textBox, this.textBox.getX() + this.offset.x, this.textBox.getY() + this.offset.y);

      for (int i = 0; i < this.words.size(); i++) {
         String word = this.words.get(i);

         for (int j = 0; j < word.length(); j++) {
            char letter = word.charAt(j);
            game.uiBatch.draw(game.textDict.get(letter), 120 + 8 * j + this.offset.x, 48 - 16 * i + this.offset.y);
         }
      }

      if (!this.disabled) {
         if (InputProcessor.upJustPressed) {
            if (this.curr > 0) {
               this.curr--;
               this.newPos = this.getCoords.get(this.curr);
            }
         } else if (InputProcessor.downJustPressed && this.curr < 1) {
            this.curr++;
            this.newPos = this.getCoords.get(this.curr);
         }

         if (this.cursorDelay >= 2) {
            game.uiBatch.draw(this.arrow, this.newPos.x + this.offset.x, this.newPos.y + this.offset.y);
         } else {
            this.cursorDelay++;
         }

         if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            if (this.curr == 0) {
               this.useItem(game, this.itemName);
            } else if (game.battle.drawAction == null) {
               Vector2 pos = game.player.facingPos();
               Tile currTile = game.map.tiles.get(pos);
               if (currTile != null && !currTile.isSolid && !currTile.isSign()) {
                  this.disabled = true;
                  game.insertAction(new DrawUseTossMenu.SelectAmount(this.itemName, pos, this));
               } else {
                  this.disabled = true;
                  game.insertAction(
                     new DisplayText(game, "Something is in the way!", null, false, true, new SetField(this.prevMenu, "disabled", false, this.prevMenu))
                  );
               }
            } else {
               game.insertAction(this.prevMenu);
               game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
            }
         } else if (InputProcessor.bJustPressed) {
            this.prevMenu.disabled = false;
            game.actionStack.remove(this);
            game.insertAction(this.prevMenu);
         }
      }
   }

   public void useItem(Game game, String itemName) {
      itemName = itemName.toLowerCase(Locale.ROOT);
      if (game.battle.drawAction == null) {
         ArrayList<String> notAllowedWhileFlying = new ArrayList<>();
         notAllowedWhileFlying.add("old rod");
         notAllowedWhileFlying.add("good rod");
         notAllowedWhileFlying.add("super rod");
         notAllowedWhileFlying.add("sleeping bag");
         notAllowedWhileFlying.add("escape rope");
         notAllowedWhileFlying.add("berry seed");
         notAllowedWhileFlying.add("manure");
         notAllowedWhileFlying.add("miracle seed");
         notAllowedWhileFlying.add("black apricorn");
         notAllowedWhileFlying.add("blue apricorn");
         notAllowedWhileFlying.add("green apricorn");
         notAllowedWhileFlying.add("pink apricorn");
         notAllowedWhileFlying.add("red apricorn");
         notAllowedWhileFlying.add("white apricorn");
         notAllowedWhileFlying.add("yellow apricorn");
         if (game.player.currFieldMove.equals("FLY") && notAllowedWhileFlying.contains(itemName.toLowerCase(Locale.ROOT))) {
            game.insertAction(this.prevMenu);
            game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
         } else if (itemName.equals("sleeping bag")) {
            if (!game.player.currFieldMove.equals("")) {
               this.disabled = true;
               game.insertAction(this.prevMenu);
               game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
            } else {
               Tile right = game.map.tiles.get(game.player.position.cpy().add(16.0F, 0.0F));
               if (right != null && !right.isSolid && !right.isLedge && !right.is("door") && !right.is("stairs")) {
                  Tile currTile = game.map.tiles.get(game.player.position.cpy().add(0.0F, 0.0F));
                  if (currTile.routeBelongsTo != null && currTile.routeBelongsTo.isDungeon) {
                     this.disabled = true;
                     game.actionStack.remove(this);
                     game.insertAction(
                        new DisplayText(
                           game, "Canì use this in a dangerous area!", null, false, true, new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                        )
                     );
                  } else if (currTile.name.contains("stairs")
                     || currTile.name.contains("mountain")
                     || currTile.name.contains("snow")
                     || currTile.name.contains("desert")) {
                     this.disabled = true;
                     game.actionStack.remove(this);
                     game.insertAction(
                        new DisplayText(game, "Canì sleep on harsh terrain!", null, false, true, new SetField(this.prevMenu, "disabled", false, this.prevMenu))
                     );
                  } else if (!currTile.isGrass && !game.player.nearAggroPokemon) {
                     if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.Sleep(game.player.network.id, true));
                     }

                     game.player.spawnLoc = game.player.position.cpy();
                     game.player.spawnIndex = -1;
                     if (game.map.tiles != game.map.overworldTiles) {
                        game.player.spawnIndex = game.map.interiorTilesIndex;
                     }

                     game.playerCanMove = true;
                     game.player.acceptInput = false;
                     game.player.dirFacing = "right";
                     game.player.sleepingBagSprite.setPosition(game.player.position.x, game.player.position.y);
                     game.insertAction(
                        new PlayerMoving(
                           game,
                           game.player,
                           false,
                           new SetField(
                              game.player,
                              "dirFacing",
                              "left",
                              new SetField(
                                 game.player,
                                 "currSprite",
                                 game.player.standingSprites.get("left"),
                                 new WaitFrames(
                                    game,
                                    24,
                                    new SetField(
                                       game.player,
                                       "drawSleepingBag",
                                       true,
                                       new WaitFrames(game, 24, new PlayerMoving(game, game.player, true, new SetField(game.player, "isSleeping", true, null)))
                                    )
                                 )
                              )
                           )
                        )
                     );
                  } else {
                     this.disabled = true;
                     game.actionStack.remove(this);
                     game.insertAction(
                        new DisplayText(
                           game, "Canì use this while Pokémon are nearby!", null, false, true, new SetField(this.prevMenu, "disabled", false, this.prevMenu)
                        )
                     );
                  }
               } else {
                  this.disabled = true;
                  game.actionStack.remove(this);
                  game.insertAction(new DisplayText(game, "Not enough room!", null, false, true, new SetField(this.prevMenu, "disabled", false, this.prevMenu)));
               }
            }
         } else if (!itemName.equals("escape rope")) {
            if (itemName.contains("repel")) {
               game.player.setItemAmount(itemName, game.player.getItemAmount(itemName) - 1);
               if (game.player.getItemAmount(itemName) <= 0) {
                  game.player.removeItem(itemName);
               }

               game.player.repelCounter = 100;
               if (itemName.contains("max")) {
                  game.player.repelCounter = 250;
               }

               game.insertAction(this.prevMenu);
               game.insertAction(
                  new DisplayText(
                     game, "Applied " + itemName.toUpperCase(Locale.ROOT) + "!", "fanfare1.ogg", null, new SetField(this.prevMenu, "disabled", false, null)
                  )
               );
            } else if (itemName.contains("rod")) {
               game.insertAction(this.prevMenu);
               game.player.currRod = itemName;
               Action newAction = new DisplayText(
                  game,
                  "Press Z to cast the line.",
                  null,
                  false,
                  true,
                  new WaitFrames(game, 10, new RemoveAction(this.prevMenu, new SetField(game, "playerCanMove", true, null)))
               );
               game.insertAction(newAction);
            } else if (itemName.contains("apricorn") || itemName.equals("manure") || itemName.equals("miracle seed") || itemName.equals("berry seed")) {
               game.insertAction(this.prevMenu);
               game.player.currPlanting = itemName;
               if (game.player.hmPokemon != null && !game.player.currFieldMove.equals("")) {
                  game.player.swapSprites(game.player.hmPokemon);
                  game.player.hmPokemon.removeDrawActions(game);
                  game.player.hmPokemon = null;
                  game.player.currFieldMove = "";
               }

               String text = "Press Z to plant seeds.";
               if (itemName.equals("manure")) {
                  text = "Press Z to fertilize saplings and small trees.";
               }

               game.insertAction(
                  new DisplayText(
                     game, text, null, false, true, new WaitFrames(game, 10, new RemoveAction(this.prevMenu, new SetField(game, "playerCanMove", true, null)))
                  )
               );
            } else if (!itemName.equals("moomoo milk")
               && !itemName.equals("berry juice")
               && !itemName.equals("revive")
               && !itemName.equals("rare candy")
               && !itemName.contains(" berry")
               && !Game.evoStones.contains(itemName)) {
               game.insertAction(this.prevMenu);
               game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
            } else {
               this.disabled = true;
               game.insertAction(new DrawPokemonMenu.Intro(new DrawPokemonMenu(game, itemName, this.prevMenu)));
            }
         } else {
            Vector2 nearest = null;
            int dst2 = 0;
            Vector2[] positions = new Vector2[]{new Vector2(-16.0F, 0.0F), new Vector2(0.0F, 16.0F), new Vector2(16.0F, 0.0F), new Vector2(0.0F, -16.0F)};

            for (Vector2 pos : game.map.edges) {
               Tile tile = game.map.overworldTiles.get(pos);
               if (tile != null && tile.name.equals("sand3") && !tile.biome.contains("mountain") && !tile.isSolid) {
                  boolean found = false;

                  for (Vector2 position : positions) {
                     Tile otherTile = game.map.overworldTiles.get(pos.cpy().add(position));
                     if (otherTile != null && otherTile.name.equals("water2")) {
                        found = true;
                        break;
                     }
                  }

                  if (found) {
                     int dist = (int)game.player.position.dst2(pos);
                     if (nearest == null || dist > 1024 && dist < dst2) {
                        dst2 = dist;
                        nearest = pos;
                     }
                  }
               }
            }

            if (nearest == null) {
               this.disabled = true;
               game.insertAction(this.prevMenu);
               game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
            } else {
               Vector2 nearestFinal = nearest.cpy();
               game.playerCanMove = false;
               game.insertAction(
                  new DisplayText(
                     game,
                     "Return to the shore?",
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
                              new FadeMusic(
                                 game.currMusic,
                                 -0.0125F,
                                 game.currMusic.getVolume(),
                                 new WaitFrames(
                                    game,
                                    60,
                                    new SplitAction(
                                       new WaitFrames(game, 18, new RunCode(() -> {
                                          game.cam.position.set(nearestFinal.x + 16.0F, nearestFinal.y, 0.0F);
                                          game.player.position = nearestFinal.cpy();
                                          if (game.player.hmPokemon != null) {
                                             game.player.hmPokemon.dirFacing = "down";
                                             game.player.hmPokemon.position = nearestFinal.cpy();
                                             if (game.player.hmPokemon.standingAction instanceof Pokemon.Follow) {
                                                ((Pokemon.Follow)game.player.hmPokemon.standingAction).onPlayer = true;
                                             }
                                          }

                                          if (game.player.currFieldMove.equals("SURF")) {
                                             game.player.swapSprites(game.player.hmPokemon);
                                             game.player.hmPokemon.removeDrawActions(game);
                                             game.player.currFieldMove = "";
                                             game.player.hmPokemon = null;
                                          }
                                       }, null)),
                                       new EscapeRope(
                                          new SetField(
                                             game.map,
                                             "currRoute",
                                             new Route("", 2),
                                             new SetField(
                                                game.map,
                                                "interiorTilesIndex",
                                                100,
                                                new WaitFrames(
                                                   game,
                                                   60,
                                                   new SetField(
                                                      game.musicController, "resumeOverworldMusic", true, new SetField(game, "playerCanMove", true, null)
                                                   )
                                                )
                                             )
                                          )
                                       )
                                    )
                                 )
                              )
                           )
                        ),
                        new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null)))
                     )
                  )
               );
            }
         }
      } else if (!itemName.contains("ball")
         && !itemName.contains("berry")
         && !itemName.equals("moomoo milk")
         && !itemName.equals("revive")
         && !itemName.equals("poké doll")
         && !itemName.equals("silph scope")) {
         game.insertAction(this.prevMenu);
         game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
      } else if (itemName.equals("silph scope") && !game.battle.oppPokemon.isGhost) {
         game.insertAction(this.prevMenu);
         game.insertAction(new PlayMusic("error1", new SetField(this.prevMenu, "disabled", false, null)));
      } else if (!itemName.contains("ball") || !game.battle.oppPokemon.isGhost && !game.battle.oppPokemon.isEgg) {
         if (!itemName.equals("moomoo milk") && !itemName.equals("berry juice") && !itemName.contains(" berry") && !itemName.equals("revive")) {
            this.prevMenu.prevMenu.disabled = false;
            Action action = new SplitAction(new PlayMusic("click1", null), null);
            if (game.type == Game.Type.CLIENT) {
               action.append(new Battle.WaitTurnData(game, null));
               game.client.sendTCP(new Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.ITEM, itemName));
            } else {
               game.battle.network.turnData = new Network.BattleTurnData();
               game.battle.network.turnData.itemName = itemName;
            }

            if (!itemName.equals("silph scope")) {
               game.player.setItemAmount(itemName, game.player.getItemAmount(itemName) - 1);
               if (game.player.getItemAmount(itemName) <= 0) {
                  game.player.removeItem(itemName);
               }
            }

            action.append(new Battle.DoTurn(game, Battle.DoTurn.Type.ITEM, this.prevMenu.prevMenu));
            game.actionStack.remove(this);
            game.insertAction(action);
         } else {
            this.disabled = true;
            game.insertAction(new DrawPokemonMenu.Intro(new DrawPokemonMenu(game, itemName, this.prevMenu)));
         }
      } else {
         game.insertAction(
            new DisplayText(
               game,
               game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " canì be caught!",
               null,
               null,
               new SetField(this.prevMenu, "disabled", false, this.prevMenu)
            )
         );
      }
   }

   public static class DropItem extends Action {
      public Action.Layer layer = Action.Layer.gui_0;
      Vector2 pos;
      Tile currTile;
      String item;
      int amount;

      public DropItem(String item, int amount, Vector2 pos, Action nextAction) {
         super();
         this.item = item;
         this.amount = amount;
         this.pos = pos;
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
      public void firstStep(Game game) {
         this.currTile = game.map.tiles.get(this.pos);
      }

      @Override
      public void step(Game game) {
         game.player.setItemAmount(this.item, game.player.getItemAmount(this.item) - this.amount);
         if (game.player.getItemAmount(this.item) <= 0) {
            game.player.removeItem(this.item);
         }

         Tile newTile = new Tile(this.currTile.name, "pokeball1", this.pos.cpy(), true, this.currTile.routeBelongsTo);
         newTile.hasItem = this.item;
         newTile.hasItemAmount = this.amount;
         if (game.type != Game.Type.CLIENT) {
            game.map.tiles.put(this.pos.cpy(), newTile);
         } else {
            game.client.sendTCP(new Network.TileData(newTile));
         }

         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   static class SelectAmount extends Menu {
      public Action.Layer layer = Action.Layer.gui_105;
      Sprite textbox;
      String itemName;
      public static int amount = 0;
      int maxAmount = 1;
      Vector2 pos;

      public SelectAmount(String itemName, Vector2 pos, Menu prevMenu) {
         this.pos = pos;
         this.itemName = itemName;
         this.prevMenu = prevMenu;
         Texture text = TextureCache.get(Gdx.files.internal("amount_bg1.png"));
         this.textbox = new Sprite(text, 0, 0, 160, 144);
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
         amount = 1;
      }

      @Override
      public void step(Game game) {
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         this.textbox.draw(game.uiBatch);
         if (!this.disabled) {
            if (InputProcessor.upJustPressed) {
               if (amount + 1 <= game.player.getItemAmount(this.itemName)) {
                  amount++;
               }
            } else if (InputProcessor.downJustPressed) {
               if (amount > 1) {
                  amount--;
               }
            } else if (InputProcessor.rightJustPressed) {
               amount += 10;
               if (amount > game.player.getItemAmount(this.itemName)) {
                  amount = game.player.getItemAmount(this.itemName);
               }
            } else if (InputProcessor.leftJustPressed) {
               amount -= 10;
               if (amount <= 1) {
                  amount = 1;
               }
            }

            String word = "x" + String.format(Locale.ROOT, "%02d", amount);

            for (int i = 0; i < word.length(); i++) {
               char letter = word.charAt(i);
               Sprite letterSprite = game.textDict.get(letter);
               game.uiBatch.draw(letterSprite, 130 + 8 * i, 56.0F);
            }

            if (InputProcessor.aJustPressed) {
               if (game.type == Game.Type.CLIENT) {
                  game.client.sendTCP(new Network.DropItem(game.player.network.id, this.itemName, amount, this.pos));
               }

               this.disabled = true;
               game.actionStack.remove(this);
               game.playerCanMove = false;
               game.insertAction(
                  new WaitFrames(
                     game,
                     10,
                     new SplitAction(
                        new DrawUseTossMenu.DropItem(this.itemName, amount, this.pos, null),
                        new PlayMusic("seed1", new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null)))
                     )
                  )
               );
            } else if (InputProcessor.bJustPressed) {
               game.insertAction(new PlayMusic("click1", null));
               game.actionStack.remove(this);
               this.prevMenu.disabled = false;
               game.insertAction(this.prevMenu);
               return;
            }
         }
      }
   }
}
