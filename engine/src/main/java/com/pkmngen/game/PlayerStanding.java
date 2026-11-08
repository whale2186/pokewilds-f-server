package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.LoadingZone;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

class PlayerStanding extends Action {
   public Action.Layer layer = Action.Layer.map_130;
   public float initialWait;
   boolean alternate;
   boolean checkWildEncounter = true;
   boolean isRunning;
   Player player;
   public static int moveTimer = 0;
   public static int bTimer = 0;
   public int cTimer = 0;
   public int vTimer = 0;
   public Tile currTile;

   public PlayerStanding(Game game) {
      super();
      this.alternate = true;
      this.checkWildEncounter = false;
      this.isRunning = false;
      this.player = game.player;
   }

   public PlayerStanding(Game game, boolean alternate) {
      super();
      this.alternate = alternate;
      this.isRunning = false;
      this.player = game.player;
   }

   public PlayerStanding(Game game, boolean alternate, boolean isRunning) {
      super();
      this.alternate = alternate;
      this.isRunning = isRunning;
      this.player = game.player;
   }

   public PlayerStanding(Game game, Player player, boolean alternate, boolean isRunning) {
      super();
      this.alternate = alternate;
      this.isRunning = isRunning;
      this.checkWildEncounter = false;
      this.player = player;
   }

   boolean checkWildEncounter(Game game) {
      Pokemon pokemon = this.checkWildEncounter(game, game.player.position);
      if (pokemon != null) {
         game.battle.oppPokemon = pokemon;
      }

      return pokemon != null;
   }

   Pokemon checkWildEncounter(Game game, Vector2 position) {
      Tile currTile = game.map.tiles.get(position);
      if (currTile != null && currTile.routeBelongsTo != null && currTile.isGrass) {
         if (currTile.items().containsKey("trapinch")) {
            return null;
         }

         int randomNum = game.map.rand.nextInt(100) + 1;
         int rate = 10;
         if (game.map.tiles == game.map.interiorTiles.get(game.map.interiorTilesIndex)) {
            rate = 4;
         } else if (currTile.name.contains("desert2")) {
            rate = 7;
         } else if (currTile.isTidal) {
            rate = 0;
         }

         if (randomNum < rate) {
            ArrayList<String> eligiblePokemon = new ArrayList<>();

            for (String name : currTile.routeBelongsTo.allowedPokemon()) {
               name = name.toLowerCase(Locale.ROOT);
               if (currTile.routeBelongsTo.isDungeon
                  || (game.map.timeOfDay.equals("day") || !currTile.routeBelongsTo.dayPokemon().contains(name))
                     && (game.map.timeOfDay.equals("night") || !currTile.routeBelongsTo.nightPokemon().contains(name))) {
                  eligiblePokemon.add(name);
               }
            }

            if (eligiblePokemon.size() <= 0) {
               return null;
            }

            int index = game.map.rand.nextInt(eligiblePokemon.size());
            String name = eligiblePokemon.get(index);
            int level = currTile.routeBelongsTo.level + Game.rand.nextInt(3);
            if (game.levelScalingEnabled && !currTile.routeBelongsTo.isDungeon) {
               int averageLevel = 0;
               int numberPokemon = 0;

               for (Pokemon mon : game.player.pokemon) {
                  if (!mon.isEgg) {
                     averageLevel += mon.level;
                     numberPokemon++;
                  }
               }

               if (numberPokemon <= 0) {
                  System.out.println("WARNING: this should never happen. Might want to look into it.");
                  numberPokemon = 1;
               }

               averageLevel /= numberPokemon;
               averageLevel = averageLevel - 3 + Game.rand.nextInt(3);
               if (averageLevel > 50) {
                  averageLevel = 50;
               }

               if (averageLevel < 2) {
                  averageLevel = 2;
               }

               level = averageLevel;
            }

            Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
            String evolveTo = null;
            int extraLevel = 0;
            int timesEvolved = 0;
            boolean failed = false;
            if (currTile.routeBelongsTo.name.equals("ruins1_inner")) {
               failed = true;
            }

            label125:
            while (!failed) {
               failed = true;
               Map<String, String> evos = Specie.gen2Evos.get(pokemon.specie.name);
               Iterator var16 = evos.keySet().iterator();

               while (true) {
                  String evo;
                  boolean isBaseSpecies;
                  boolean hasEvo;
                  do {
                     if (!var16.hasNext()) {
                        continue label125;
                     }

                     evo = (String)var16.next();
                     if (currTile.routeBelongsTo.isDungeon) {
                        extraLevel = 10 * (timesEvolved + 1);
                        break;
                     }

                     name = evos.get(evo).toLowerCase(Locale.ROOT);
                     if (!Specie.species.containsKey(name)) {
                        Specie.species.put(name, new Specie(name));
                     }

                     isBaseSpecies = Pokemon.baseSpecies.get(name).equalsIgnoreCase(name);
                     hasEvo = !Specie.gen2Evos.get(name).isEmpty();
                  } while (!isBaseSpecies && !hasEvo);

                  try {
                     int evolutionLevel = Integer.valueOf(evo);
                     if (evolutionLevel <= pokemon.level + extraLevel && Game.rand.nextInt(256) >= 128) {
                        evolveTo = evos.get(evo);
                        pokemon.evolveTo(evolveTo);
                        timesEvolved++;
                        failed = false;
                        break;
                     }
                  } catch (NumberFormatException e) {
                     if (Game.rand.nextInt(256) >= 192) {
                        evolveTo = evos.get(evo);
                        pokemon.evolveTo(evolveTo);
                        timesEvolved++;
                        failed = false;
                        break;
                     }
                  }
               }
            }

            if (evolveTo != null) {
               if (currTile.routeBelongsTo.isDungeon) {
                  pokemon.level += 10 * timesEvolved;
               }

               pokemon.exp = pokemon.calcExpForLevel(pokemon.level);
               pokemon.calcMaxStats();
               pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
            }

            if (game.dangerousDebugInputEnabled) {
               pokemon = new Pokemon("honchkrow", 20, Pokemon.Generation.CRYSTAL);
               pokemon.attacks[0] = "splash";
               pokemon.attacks[1] = "splash";
               pokemon.attacks[2] = "splash";
               pokemon.attacks[3] = "splash";
            }

            if (game.debugInputEnabled && Gdx.input.isKeyPressed(62)) {
               pokemon = null;
            }

            return pokemon;
         }
      }

      return null;
   }

   public void detectIsHouseBuilt(Game game, Tile currTile) {
      if (game.map.tiles == game.map.overworldTiles) {
         Vector2 pos = currTile.position.cpy();
         if (currTile.nameUpper.contains("house")) {
            Tile startTile = currTile;
            Tile nextTile = currTile;
            Tile doorTile = null;
            boolean found = true;

            do {
               if (nextTile.nameUpper.contains("middle") && !nextTile.nameUpper.contains("roof")) {
                  for (Tile var8 = game.map.tiles.get(pos.add(0.0F, 16.0F));
                     !var8.nameUpper.contains("middle");
                     var8 = game.map.tiles.get(pos.add(0.0F, 16.0F))
                  ) {
                     if (!var8.nameUpper.contains("house")) {
                        found = false;
                        break;
                     }
                  }
               }

               if (nextTile.nameUpper.contains("door")) {
                  doorTile = nextTile;
               }

               if (!found || !nextTile.nameUpper.contains("house")) {
                  found = false;
                  break;
               }

               if (nextTile.nameUpper.contains("roof")) {
                  if (nextTile.nameUpper.contains("right")) {
                     nextTile = game.map.tiles.get(nextTile.position.cpy().add(0.0F, -16.0F));
                  } else {
                     nextTile = game.map.tiles.get(nextTile.position.cpy().add(16.0F, 0.0F));
                  }
               } else if (nextTile.nameUpper.contains("left")) {
                  nextTile = game.map.tiles.get(nextTile.position.cpy().add(0.0F, 16.0F));
               } else {
                  nextTile = game.map.tiles.get(nextTile.position.cpy().add(-16.0F, 0.0F));
               }
            } while (!nextTile.equals(startTile));

            if (found ? doorTile != null : doorTile != null) {
            }
         }
      }
   }

   public void localStep(Game game) {
      if (game.playerCanMove) {
         boolean shouldMove = false;
         Vector2 newPos = new Vector2();
         if (this.checkWildEncounter) {
            game.player.updateBuildTiles(game);
            Tile tile = game.map.tiles.get(game.player.position);
            if (tile != null && (tile.nameUpper.contains("stairs") || tile.name.contains("stairs"))) {
               int downUp = 0;
               byte var39;
               if (!tile.nameUpper.contains("up") && !tile.name.contains("up")) {
                  var39 = -1;
               } else {
                  var39 = 1;
               }

               game.playerCanMove = false;
               Map<Vector2, Tile> whichTiles = game.map.interiorTiles.get(game.map.interiorTilesIndex + var39);
               if (tile.name.contains("exit") || tile.nameUpper.contains("exit")) {
                  whichTiles = game.map.overworldTiles;
               }

               Action action = new EnterBuilding(
                  game,
                  "enter",
                  whichTiles,
                  game.map.interiorTilesIndex + var39,
                  new SetField(game.map, "interiorTilesIndex", game.map.interiorTilesIndex + var39, new SetField(game, "playerCanMove", true, null))
               );
               game.insertAction(action);
               this.checkWildEncounter = false;
               return;
            }

            game.player.eggStepTimer++;
            game.player.friendshipStepTimer++;
            if (Gdx.input.isKeyPressed(62) && game.dangerousDebugInputEnabled) {
               game.player.eggStepTimer += 16;
               game.player.friendshipStepTimer += 512;
            }

            if (game.player.eggStepTimer >= 16) {
               for (Pokemon pokemon : game.player.pokemon) {
                  if (pokemon.isEgg) {
                     pokemon.happiness--;
                     System.out.println(pokemon.nickname);
                     System.out.println(pokemon.specie.name);
                     System.out.println(pokemon.happiness);
                     if (pokemon.happiness <= 0) {
                        game.playerCanMove = false;
                        String[] huhs = new String[]{"Huh?", "Oh?"};
                        Action hatchAnimation = new WaitFrames(
                           game,
                           61,
                           new WaitFrames(
                              game,
                              3,
                              new DisplayText(
                                 game,
                                 huhs[Game.rand.nextInt(huhs.length)],
                                 null,
                                 true,
                                 false,
                                 new WaitFrames(
                                    game,
                                    51,
                                    new DisplayText.Clear(
                                       game,
                                       new EggHatchAnim(
                                          pokemon,
                                          new SplitAction(
                                             new SetField(game.musicController, "startEvolveMusic", true, null),
                                             new Battle.LoadAndPlayAnimation(
                                                game,
                                                "egg_hatch",
                                                null,
                                                new SplitAction(
                                                   new WaitFrames(game, 4, new PlayMusic(new Pokemon(pokemon.specie.name, 10), null)),
                                                   new PokemonIntroAnim(
                                                      new WaitFrames(
                                                         game,
                                                         4,
                                                         new DisplayText(
                                                            game,
                                                            pokemon.getNickname().toUpperCase(Locale.ROOT) + " came out of its EGG!",
                                                            "fanfare2.ogg",
                                                            false,
                                                            true,
                                                            new SplitAction(
                                                               new EggHatchAnim.Done(13, new SetField(game.musicController, "evolveMusicFadeout", true, null)),
                                                               new WaitFrames(
                                                                  game, 13, new EnterBuilding(game, "", 8, new SetField(game, "playerCanMove", true, null))
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
                        game.insertAction(hatchAnimation);
                        this.checkWildEncounter = false;
                        return;
                     }
                  }
               }

               game.player.eggStepTimer = 0;
            }

            if (game.player.friendshipStepTimer >= 512) {
               for (Pokemon pokemon : game.player.pokemon) {
                  if (!pokemon.isEgg) {
                     pokemon.gainHappiness(1);
                     System.out.println(pokemon.nickname);
                     System.out.println(pokemon.happiness);
                  }
               }

               game.player.friendshipStepTimer = 0;
            }
         }

         if (this.checkWildEncounter && game.type != Game.Type.CLIENT) {
            if (this.checkWildEncounter(game)) {
               this.player.setCurrPokemon();
               boolean repelling = game.player.repelCounter > 0 && game.battle.oppPokemon.level < game.player.currPokemon.level;
               repelling = repelling
                  || game.player.hmPokemon != null && game.player.currFieldMove.equals("REPEL") && game.battle.oppPokemon.level < game.player.hmPokemon.level;
               if (!repelling) {
                  game.playerCanMove = false;
                  game.musicController.startBattle = "wild";
                  if (game.musicController.unownMusic) {
                     String unownLetter = game.map.unownUsed.get(game.map.rand.nextInt(game.map.unownUsed.size()));
                     game.battle.oppPokemon = new Pokemon("unown_" + unownLetter, 13);
                  }

                  game.insertAction(Battle.getIntroAction(game));
                  this.checkWildEncounter = false;
                  return;
               }
            }

            if (game.player.repelCounter > 0) {
               game.player.repelCounter--;
               if (game.player.repelCounter == 0) {
                  game.playerCanMove = false;
                  game.insertAction(
                     new DisplayText(
                        game, "The effects of REPEL wore off.", null, null, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null))
                     )
                  );
                  return;
               }
            }

            this.checkWildEncounter = false;
         } else if (this.player.network.doEncounter != null) {
            this.player.setCurrPokemon();
            game.playerCanMove = false;
            Network.PokemonData pokemonData = this.player.network.doEncounter.pokemonData;
            game.battle.oppPokemon = new Pokemon(pokemonData.name, pokemonData.level);
            game.battle.oppPokemon.currentStats.put("hp", pokemonData.hp);
            game.musicController.startBattle = "wild";
            game.insertAction(Battle.getIntroAction(game));
            this.checkWildEncounter = false;
            this.player.network.doEncounter = null;
            return;
         }

         if (game.map.tiles.get(game.player.position) != null
            && (game.map.tiles.get(game.player.position).nameUpper.contains("door") || game.map.tiles.get(game.player.position).name.contains("door"))) {
            game.playerCanMove = false;
            Action action;
            if (game.map.tiles == game.map.overworldTiles) {
               action = new EnterBuilding(game, new SetField(game, "playerCanMove", true, null));
            } else {
               action = new EnterBuilding(game, "exit", new SetField(game, "playerCanMove", true, null));
            }

            game.insertAction(action);
         } else if (game.player.acceptInput || game.player.isSleeping) {
            if (InputProcessor.startJustPressed) {
               game.insertAction(
                  new DrawPlayerMenu.Intro(game, new DrawPlayerMenu(game, new WaitFrames(game, 1, new SetField(game, "playerCanMove", true, null))))
               );
               game.playerCanMove = false;
            } else if (game.player.isSleeping) {
               if (InputProcessor.bJustPressed) {
                  if (game.type == Game.Type.CLIENT) {
                     game.client.sendTCP(new Network.Sleep(game.player.network.id, false));
                  }

                  game.player.isSleeping = false;
                  if (game.player.sleepingDir == null) {
                     game.player.dirFacing = "left";
                     game.insertAction(
                        new WaitFrames(
                           game,
                           24,
                           new SetField(
                              game.player,
                              "dirFacing",
                              "right",
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
                                             false,
                                             new WaitFrames(
                                                game,
                                                24,
                                                new PlayerMoving(
                                                   game,
                                                   game.player,
                                                   true,
                                                   new SetField(
                                                      game.player,
                                                      "acceptInput",
                                                      true,
                                                      new SetField(game.player, "currSprite", game.player.standingSprites.get("down"), null)
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
                  } else {
                     game.player.sleepingDir = null;
                  }
               }
            } else {
               this.currTile = game.map.tiles.get(game.player.position);
               if (this.currTile != null && this.currTile.name.equals("ice2")) {
                  Vector2 targetPos = game.player.facingPos();
                  Tile nextTile = game.map.tiles.get(targetPos);
                  Pokemon pokemon = game.map.pokemon.get(targetPos);
                  boolean isSolid = nextTile == null;
                  isSolid |= nextTile != null && nextTile.isSolid;
                  isSolid |= pokemon != null;
                  if (nextTile == null || nextTile.ledgeDir == null) {
                     if (this.currTile.ledgeDir().equals("up") && game.player.dirFacing.equals("up")) {
                        game.actionStack.remove(this);
                        game.insertAction(new PlayerLedgeJumpFast(game));
                        return;
                     }

                     if (!isSolid) {
                        game.actionStack.remove(this);
                        game.insertAction(game.player.new Sliding(this));
                        return;
                     }
                  } else if (!nextTile.ledgeDir.equals("up") && nextTile.ledgeDir.equals(game.player.dirFacing)) {
                     Vector2 diff = targetPos.cpy().sub(game.player.position);
                     Tile fartherOutTile = game.map.tiles.get(targetPos.cpy().add(diff));
                     if (fartherOutTile != null && !fartherOutTile.isSolid && fartherOutTile.ledgeDir == null) {
                        game.actionStack.remove(this);
                        game.insertAction(new PlayerLedgeJump(game));
                        return;
                     }
                  } else if (nextTile.ledgeDir.equals("up") && !game.player.dirFacing.equals("down")) {
                     game.actionStack.remove(this);
                     game.insertAction(game.player.new Sliding(this));
                     return;
                  }
               }

               if (InputProcessor.upPressed) {
                  game.player.dirFacing = "up";
                  newPos = new Vector2(game.player.position.x, game.player.position.y + 16.0F);
                  if (moveTimer == 0) {
                     game.player.updateBuildTiles(game);
                  } else if (moveTimer > 3) {
                     shouldMove = true;
                  }

                  moveTimer++;
               } else if (InputProcessor.downPressed) {
                  game.player.dirFacing = "down";
                  newPos = new Vector2(game.player.position.x, game.player.position.y - 16.0F);
                  if (moveTimer == 0) {
                     game.player.updateBuildTiles(game);
                  } else if (moveTimer > 3) {
                     shouldMove = true;
                  }

                  moveTimer++;
               } else if (InputProcessor.leftPressed) {
                  game.player.dirFacing = "left";
                  newPos = new Vector2(game.player.position.x - 16.0F, game.player.position.y);
                  if (moveTimer == 0) {
                     game.player.updateBuildTiles(game);
                  } else if (moveTimer > 3) {
                     shouldMove = true;
                  }

                  moveTimer++;
               } else if (InputProcessor.rightPressed) {
                  game.player.dirFacing = "right";
                  newPos = new Vector2(game.player.position.x + 16.0F, game.player.position.y);
                  if (moveTimer == 0) {
                     game.player.updateBuildTiles(game);
                  } else if (moveTimer > 3) {
                     shouldMove = true;
                  }

                  moveTimer++;
               } else {
                  moveTimer = 0;
               }

               if (game.player.currFieldMove.equals("BUILD") || game.player.currFieldMove.equals("DIG")) {
                  if (InputProcessor.lPressed) {
                     if (this.cTimer < 30) {
                        this.cTimer++;
                     }
                  } else {
                     this.cTimer = 0;
                  }

                  if (InputProcessor.rPressed) {
                     if (this.vTimer < 30) {
                        this.vTimer++;
                     }
                  } else {
                     this.vTimer = 0;
                  }
               }

               if (InputProcessor.lJustPressed || this.cTimer >= 30) {
                  if (game.player.currFieldMove.equals("BUILD")) {
                     game.player.buildTileIndex--;
                     if (game.player.buildTileIndex < 0) {
                        game.player.buildTileIndex = game.player.buildTiles.size() - 1;
                     }

                     game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                  } else {
                     boolean requirementsMet;
                     if (game.player.currFieldMove.equals("DIG") && !game.player.currBuildTile.name.contains("hole")) {
                        do {
                           game.player.buildTileIndex--;
                           if (game.player.buildTileIndex < 0) {
                              game.player.buildTileIndex = game.player.terrainTiles.size() - 1;
                           }

                           game.player.currBuildTile = game.player.terrainTiles.get(game.player.buildTileIndex);
                           requirementsMet = true;

                           for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                              if (!game.player.hasItem(reqName)) {
                                 requirementsMet = false;
                                 break;
                              }

                              int playerOwns = game.player.getItemAmount(reqName);
                              if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
                                 requirementsMet = false;
                                 break;
                              }
                           }
                        } while (!requirementsMet && game.player.buildTileIndex != 0);
                     }
                  }
               }

               if (InputProcessor.rJustPressed || this.vTimer >= 30) {
                  if (game.player.currFieldMove.equals("BUILD")) {
                     game.player.buildTileIndex++;
                     if (game.player.buildTileIndex >= game.player.buildTiles.size()) {
                        game.player.buildTileIndex = 0;
                     }

                     game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
                  } else {
                     boolean requirementsMet;
                     if (game.player.currFieldMove.equals("DIG") && !game.player.currBuildTile.name.contains("hole")) {
                        do {
                           game.player.buildTileIndex++;
                           if (game.player.buildTileIndex >= game.player.terrainTiles.size()) {
                              game.player.buildTileIndex = 0;
                           }

                           game.player.currBuildTile = game.player.terrainTiles.get(game.player.buildTileIndex);
                           requirementsMet = true;

                           for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                              if (!game.player.hasItem(reqName)) {
                                 requirementsMet = false;
                                 break;
                              }

                              int playerOwns = game.player.getItemAmount(reqName);
                              if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
                                 requirementsMet = false;
                                 break;
                              }
                           }
                        } while (!requirementsMet && game.player.buildTileIndex != 0);
                     }
                  }
               }

               if (InputProcessor.bJustPressed) {
                  bTimer = 0;
               }

               if (shouldMove) {
                  bTimer = 20;
               }

               if (InputProcessor.bPressed) {
                  if (bTimer == 19) {
                     if (game.type == Game.Type.CLIENT && !game.player.currFieldMove.equals("")) {
                        game.client.sendTCP(new Network.UseHM(game.player.network.id, DrawPokemonMenu.currIndex, "STOP"));
                     }

                     if (game.player.currPlanting != null) {
                        game.player.currPlanting = null;
                        bTimer = 0;
                        return;
                     }

                     if (!game.player.currRod.equals("")) {
                        game.player.currRod = "";
                        bTimer = 0;
                        return;
                     }

                     if (game.player.hmPokemon != null && !game.player.currFieldMove.equals("SURF")) {
                        if (!game.player.currFieldMove.equals("RIDE")) {
                           game.playerCanMove = false;
                           game.actionStack.remove(game.player.hmPokemon.standingAction);
                           if (game.player.hmPokemon.ledgeJumps.size() > 0) {
                              game.player.hmPokemon.standingAction = game.player.hmPokemon.new LedgeJump(
                                 game.player.hmPokemon.dirFacing, game.player.hmPokemon.ledgeJumps.remove(0), true, null
                              );
                           } else {
                              game.player.hmPokemon.standingAction = game.player.hmPokemon.new Moving(
                                 game.player.hmPokemon.dirFacing, 1, 1.0F, true, true, null
                              );
                           }

                           if (!game.player.currFieldMove.equals("")) {
                              game.player
                                 .hmPokemon
                                 .standingAction
                                 .append(new CallMethod(game.player, "swapSprites", new Object[]{game.player.hmPokemon}, null));
                           }

                           game.player
                              .hmPokemon
                              .standingAction
                              .append(
                                 new CallMethod(
                                    game.player.hmPokemon,
                                    "removeDrawActions",
                                    new Object[]{game},
                                    new SetField(game.player, "hmPokemon", null, new SetField(game, "playerCanMove", true, null))
                                 )
                              );
                           game.player.currFieldMove = "";
                           game.insertAction(game.player.hmPokemon.standingAction);
                           return;
                        }

                        game.player.swapSprites(game.player.hmPokemon);
                        game.player.hmPokemon = null;
                        game.player.currFieldMove = "";
                     }
                  } else {
                     bTimer++;
                  }
               }

               if (InputProcessor.aJustPressed) {
                  if (game.coop != null && game.coop.interactWithPlayer()) return;
                  Vector2 pos = game.player.facingPos();
                  Tile currTile = game.map.tiles.get(pos);
                  if (currTile != null) {
                     System.out.println("facingTile.name");
                     System.out.println(currTile.name);
                     System.out.println("facingTile.nameUpper");
                     System.out.println(currTile.nameUpper);
                     System.out.println("facingTile.biome");
                     System.out.println(currTile.biome);
                     if (currTile.routeBelongsTo != null) {
                        System.out.println("facingTile.route");
                        System.out.println(currTile.routeBelongsTo.name);
                     }
                  }

                  if (game.player.currFieldMove.equals("BUILD")) {
                     boolean requirementsMet = true;

                     for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                        if (!game.player.hasItem(reqName)) {
                           requirementsMet = false;
                           break;
                        }

                        int playerOwns = game.player.getItemAmount(reqName);
                        if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
                           requirementsMet = false;
                           break;
                        }
                     }

                     if (game.player.currBuildTile.name.contains("house")) {
                        Tile upTile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(pos.cpy().add(0.0F, 16.0F));
                        requirementsMet = requirementsMet && (upTile == null || upTile.name.contains("house") || upTile.name.contains("rug"));
                     }

                     if (game.player.currBuildTile.name.contains("picture")) {
                        requirementsMet = requirementsMet
                           && currTile != null
                           && !currTile.nameUpper.contains("_left")
                           && !currTile.nameUpper.contains("_right");
                     }

                     boolean isTorch = game.player.currBuildTile.name.contains("torch")
                        || game.player.currBuildTile.name.equals("house_clock1")
                        || game.player.currBuildTile.name.contains("picture")
                        || game.player.currBuildTile.name.contains("window");
                     if (isTorch) {
                        requirementsMet = requirementsMet
                           && currTile != null
                           && !currTile.items().containsKey("torch")
                           && currTile.isSolid
                           && !currTile.isWater
                           && !currTile.nameUpper.contains("roof");
                        String[] notAllowedTiles = new String[]{"regi", "tree", "bush", "table", "bed", "shelf", "plant", "couch", "kiln", "chest", "trashcan"};

                        for (String name : notAllowedTiles) {
                           requirementsMet = requirementsMet && currTile != null && !currTile.name.contains(name) && !currTile.nameUpper.contains(name);
                        }
                     }

                     if (game.player.currBuildTile.name.contains("door")) {
                        requirementsMet &= !game.map.overworldTiles.get(pos.cpy().add(0.0F, -16.0F)).nameUpper.contains("roof");
                     }

                     if (game.player.currBuildTile.name.contains("window")) {
                        requirementsMet &= currTile.name.contains("house") || currTile.nameUpper.contains("house");
                     }

                     boolean isCarpet = game.player.currBuildTile.name.contains("carpet");
                     if (isCarpet) {
                        Tile exteriorTile = game.map.overworldTiles.get(pos);
                        requirementsMet = requirementsMet
                           && exteriorTile != null
                           && exteriorTile.nameUpper.contains("house")
                           && (
                              currTile == null
                                 || !currTile.name.contains("rug")
                                    && !game.player.currBuildTile.name.equals(currTile.name)
                                    && (!game.player.currBuildTile.name.equals("house_carpet1") || !currTile.name.equals("house5_floor1"))
                           );
                     }

                     boolean isBridge = game.player.currBuildTile.name.contains("bridge");
                     if (isBridge) {
                        requirementsMet = requirementsMet
                           && currTile != null
                           && (currTile.name.contains("water") || currTile.name.contains("_tidal") || currTile.name.contains("lava"));
                     }

                     boolean isStairs = game.player.currBuildTile.name.contains("stairs_up");
                     if (isStairs) {
                        int currLevel = game.map.interiorTilesIndex - 100;
                        if (currLevel < 0) {
                           game.playerCanMove = false;
                           game.insertAction(
                              new DisplayText(game, "Canì build stairs underground.", null, null, new SetField(game, "playerCanMove", true, null))
                           );
                           return;
                        }

                        Tile exteriorTile = game.map.overworldTiles.get(pos);
                        int buildingHeight = game.map.getBuildingHeight(exteriorTile);
                        if (buildingHeight == -1) {
                           game.playerCanMove = false;
                           game.insertAction(
                              new DisplayText(game, "This house needs a completed roof.", null, null, new RunCode(() -> game.playerCanMove = true, null))
                           );
                           return;
                        }

                        if (currLevel + 1 >= buildingHeight) {
                           game.playerCanMove = false;
                           game.insertAction(
                              new DisplayText(game, "This house needs to be taller.", null, null, new SetField(game, "playerCanMove", true, null))
                           );
                           return;
                        }
                     }

                     boolean isChimney = game.player.currBuildTile.name.contains("chimney");
                     if (isChimney) {
                        requirementsMet = requirementsMet && currTile != null && currTile.nameUpper.contains("roof");
                     }

                     if (!game.player.currBuildTile.name.contains("bed")
                        && !game.player.currBuildTile.name.equals("house_couch2")
                        && !game.player.currBuildTile.name.equals("house_couch4")) {
                        if (game.player.currBuildTile.name.equals("house_couch1")) {
                           Tile rightTile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(pos.cpy().add(16.0F, 0.0F));
                           requirementsMet &= rightTile != null && !rightTile.isSolid && !rightTile.nameUpper.contains("door");
                        }
                     } else {
                        Tile upTile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(pos.cpy().add(0.0F, 16.0F));
                        requirementsMet &= upTile != null && !upTile.isSolid && !upTile.nameUpper.contains("door");
                     }

                     requirementsMet &= currTile == null || !currTile.nameUpper.contains("stairs_down");
                     requirementsMet &= currTile == null || !currTile.nameUpper.contains("gate");
                     if (!isTorch) {
                        requirementsMet &= currTile == null || !currTile.isLedge;
                     }

                     boolean isSolid = currTile == null || currTile.isSolid;
                     boolean isDoor = currTile != null && currTile.nameUpper.contains("door");
                     requirementsMet &= currTile == null || !currTile.name.contains("rug");
                     if ((isTorch || isCarpet || isBridge || isChimney || !isSolid) && !isDoor && requirementsMet) {
                        if (currTile == null) {
                           currTile = new Tile(game.player.currBuildTile.name, pos.cpy(), true, null);
                           game.map.tiles.put(pos.cpy(), currTile);
                        } else if (game.player.currBuildTile.name.contains("torch")) {
                           currTile.items().put("torch", 1);
                           currTile.isTorch = true;
                        } else if (!isCarpet) {
                           if (isBridge) {
                              currTile.squish(currTile.name);
                              currTile.name = game.player.currBuildTile.name;
                              currTile.belowBridge = false;
                              currTile.init();
                              Tile down = game.map.tiles.get(currTile.position.cpy().add(0.0F, -16.0F));
                              if (down != null && (down.name.contains("water") || down.name.contains("lava"))) {
                                 down.belowBridge = true;
                              }
                           } else if (currTile.nameUpper.contains("interiorwall") && !game.player.currBuildTile.name.contains("torch")) {
                              currTile.nameUpper = currTile.nameUpper + "|" + game.player.currBuildTile.name;
                              currTile.init();
                           } else if (game.player.currBuildTile.name.contains("exteriorwindows")) {
                              currTile.nameUpper = currTile.nameUpper + "exteriorwindows";
                              currTile.init();
                           } else if (isChimney) {
                              currTile.nameUpper = currTile.nameUpper + "chimney";
                              currTile.init();
                           } else {
                              currTile.nameUpper = game.player.currBuildTile.name;
                              currTile.init();
                           }
                        } else {
                           Tile down = game.map.tiles.get(currTile.position.cpy().add(0.0F, -16.0F));

                           for (Tile tile : new Tile[]{down, currTile}) {
                              if (tile != null && tile.nameUpper.contains("bed")) {
                                 tile.nameUpper = tile.nameUpper.split("colored")[0] + "colored" + game.player.currBuildTile.name;
                                 tile.init();
                              }
                           }

                           currTile.name = game.player.currBuildTile.name;
                           currTile.init();
                        }

                        if (game.player.currBuildTile.name.contains("chest")) {
                           if (currTile.routeBelongsTo == null) {
                              currTile.routeBelongsTo = new Route("", 2);
                           } else {
                              currTile.routeBelongsTo = new Route(currTile.routeBelongsTo.name, currTile.routeBelongsTo.level);
                           }
                        }

                        currTile.updateMiniMap(game);
                        game.map.adjustSurroundingTiles(currTile);
                        if (!game.player.currBuildTile.name.contains("bed")
                           && !game.player.currBuildTile.name.equals("house_couch2")
                           && !game.player.currBuildTile.name.equals("house_couch4")) {
                           if (game.player.currBuildTile.name.equals("house_couch1")) {
                              Tile right = game.map.tiles.get(currTile.position.cpy().add(16.0F, 0.0F));
                              right.nameUpper = "onpress_left";
                              right.isSolid = true;
                           } else if (game.player.currBuildTile.name.equals("stairs_up1")) {
                              int interiorIndex = game.map.interiorTilesIndex + 1;
                              HashMap<Vector2, Tile> interiorTiles = game.map.getInteriorLayer(interiorIndex);

                              for (int i = -1; i < 2; i++) {
                                 for (int j = -1; j < 2; j++) {
                                    String nameUpper = "";
                                    if (i == 0 && j == 0) {
                                       nameUpper = "stairs_down1";
                                    }

                                    Vector2 interiorPos = currTile.position.cpy().add(i * 16, j * 16);
                                    Tile exteriorTile = game.map.overworldTiles.get(interiorPos);
                                    Tile interiorTile = interiorTiles.get(interiorPos);
                                    if (exteriorTile != null
                                       && exteriorTile.nameUpper.contains("house")
                                       && (nameUpper.equals("stairs_down1") || interiorTile == null || interiorTile.name.contains("wall"))) {
                                       interiorTile = new Tile("house5_floor1", nameUpper, interiorPos.cpy());
                                       interiorTiles.put(interiorPos.cpy(), interiorTile);
                                    }
                                 }
                              }
                           }
                        } else {
                           Tile upTile = game.map.tiles.get(currTile.position.cpy().add(0.0F, 16.0F));
                           upTile.nameUpper = "onpress_above";
                           upTile.isSolid = true;
                        }

                        game.player.updateBuildTiles(game);
                        game.playerCanMove = false;
                        int timer = 30;
                        String soundName = "strength1";
                        if (isCarpet) {
                           timer = 10;
                           soundName = "gate1";
                        }

                        game.insertAction(new PlayMusic(soundName, null));
                        game.insertAction(new WaitFrames(game, timer, new SetField(game, "playerCanMove", true, null)));

                        for (String name : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                           int value = game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(name);
                           int newValue = game.player.getItemAmount(name) - value;
                           game.player.setItemAmount(name, newValue);
                           if (newValue <= 0) {
                              game.player.removeItem(name);
                           }

                           if (currTile.items().containsKey(name)) {
                              value += currTile.items().get(name);
                           }

                           currTile.items().put(name, value);
                        }

                        DrawBuildRequirements.currBuilding = "";
                     }
                  } else if (currTile != null) {
                     if (game.player.currPlanting != null) {
                        if (game.player.currPlanting.equals("manure")) {
                           if (!currTile.nameUpper.contains("fertilized")
                              && (currTile.nameUpper.contains("tree_planted") || currTile.nameUpper.contains("bush2_color"))) {
                              currTile.nameUpper = currTile.nameUpper + "_fertilized";
                              game.map
                                 .tiles
                                 .put(currTile.position, new Tile("mountain1", currTile.nameUpper, currTile.position.cpy(), true, currTile.routeBelongsTo));
                              game.insertAction(new PlayMusic("seed1", null));
                           }
                        } else if (game.player.currPlanting.equals("miracle seed")) {
                           if (currTile.nameUpper.equals("")
                              && (
                                 currTile.name.equals("green1")
                                    || currTile.name.contains("snow")
                                    || currTile.name.contains("sand")
                                    || currTile.name.contains("desert")
                                    || currTile.name.contains("green_savanna")
                                    || currTile.name.equals("green9")
                                    || currTile.name.equals("green10")
                                    || currTile.name.equals("green11")
                                    || currTile.name.equals("green12")
                                    || currTile.name.contains("flower")
                              )) {
                              game.map
                                 .tiles
                                 .put(currTile.position, new Tile(currTile.name, "grass_planted", currTile.position.cpy(), true, currTile.routeBelongsTo));
                              game.insertAction(new PlayMusic("seed1", null));
                           }
                        } else if (game.player.currPlanting.equals("berry seed")) {
                           if (currTile.nameUpper.equals("")
                              && (
                                 currTile.name.equals("green1")
                                    || currTile.name.contains("snow")
                                    || currTile.name.contains("sand")
                                    || currTile.name.contains("desert")
                                    || currTile.name.equals("green9")
                                    || currTile.name.equals("green10")
                                    || currTile.name.equals("green11")
                                    || currTile.name.equals("green12")
                                    || currTile.name.contains("volcano")
                                    || currTile.name.contains("savanna")
                                    || currTile.name.contains("graveyard")
                                    || currTile.name.contains("flower")
                              )) {
                              game.playerCanMove = false;
                              game.insertAction(
                                 new WaitFrames(
                                    game,
                                    10,
                                    new SplitAction(
                                       new PlantTree("berry_planted", pos, null),
                                       new PlayMusic("seed1", new SetField(game, "playerCanMove", true, new WaitFrames(game, 4, new PlayMusic("ledge2", null))))
                                    )
                                 )
                              );
                           }
                        } else if (game.player.currPlanting.contains("apricorn")) {
                           if (!currTile.name.equals("green1")
                                 && !currTile.name.contains("flower")
                                 && !currTile.name.contains("sand")
                                 && !currTile.name.contains("snow")
                                 && !currTile.name.contains("desert")
                              || currTile.nameUpper.contains("tree")
                              || currTile.isSolid) {
                              return;
                           }

                           game.playerCanMove = false;
                           game.insertAction(
                              new WaitFrames(
                                 game,
                                 10,
                                 new SplitAction(
                                    new PlantTree(pos, null),
                                    new PlayMusic("seed1", new SetField(game, "playerCanMove", true, new WaitFrames(game, 4, new PlayMusic("ledge2", null))))
                                 )
                              )
                           );
                        }

                        game.player.setItemAmount(game.player.currPlanting, game.player.getItemAmount(game.player.currPlanting) - 1);
                        if (game.player.getItemAmount(game.player.currPlanting) <= 0) {
                           game.player.removeItem(game.player.currPlanting);
                           game.player.currPlanting = null;
                        }

                        return;
                     }

                     if (game.player.currFieldMove.equals("DIG")) {
                        if (currTile.hasItem != null) {
                           currTile.onPressA(game);
                           return;
                        }

                        if (!game.player.currBuildTile.name.contains("hole")) {
                           boolean requirementsMet = true;

                           for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                              if (!game.player.hasItem(reqName)) {
                                 requirementsMet = false;
                                 break;
                              }

                              int playerOwns = game.player.getItemAmount(reqName);
                              if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
                                 requirementsMet = false;
                                 break;
                              }
                           }

                           if (requirementsMet) {
                              if (!game.player.currBuildTile.name.equals("ledges3_none")) {
                                 if (!game.player.currBuildTile.name.equals("green_woodedlake") && !game.player.currBuildTile.name.equals("green_deepforest")) {
                                    currTile.name = game.player.currBuildTile.name;
                                    currTile.nameUpper = "";
                                    currTile.guessBiomeType();
                                    currTile.init();
                                    currTile.guessBiomeType();
                                    currTile.updateMiniMap(game);
                                    game.map.adjustSurroundingTiles(currTile);
                                 } else {
                                    currTile.name = "green1";
                                    currTile.nameUpper = "";
                                    currTile.routeBelongsTo = game.player.currBuildTile.routeBelongsTo;
                                    if (game.player.currBuildTile.name.equals("green_woodedlake")) {
                                       currTile.biome = "wooded_lake";
                                    } else if (game.player.currBuildTile.name.equals("green_deepforest")) {
                                       currTile.biome = "deep_forest";
                                    }

                                    currTile.init();
                                    currTile.updateMiniMap(game);
                                    game.map.adjustSurroundingTiles(currTile);
                                 }
                              } else {
                                 String name = "ledges3_none";
                                 if (currTile.name.contains("snow") || currTile.name.contains("ice")) {
                                    name = "ledges3snow_none";
                                 } else if (currTile.name.contains("volcano") || currTile.name.contains("soot")) {
                                    name = "ledges3volcano_none";
                                 }

                                 currTile.nameUpper = name;
                                 currTile.init();
                                 currTile.updateMiniMap(game);
                                 game.map.ledgify(currTile);
                              }

                              for (String name : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
                                 int value = game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(name);
                                 int newValue = game.player.getItemAmount(name) - value;
                                 game.player.setItemAmount(name, newValue);
                                 if (newValue <= 0) {
                                    game.player.removeItem(name);
                                 }
                              }

                              game.playerCanMove = false;
                              game.player.currFieldMove = "";
                              game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
                              game.insertAction(
                                 new WaitFrames(game, 16, new SetField(game.player, "currFieldMove", "DIG", new SetField(game, "playerCanMove", true, null)))
                              );
                              game.insertAction(
                                 new WaitFrames(game, 10, new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing), null))
                              );
                              game.insertAction(new PlayMusic("ap1", 1.0F, true, null));
                           }

                           return;
                        }

                        boolean requirementsMet = currTile.nameUpper.equals("")
                           || currTile.nameUpper.contains("_cracked")
                           || currTile.nameUpper.contains("ledges3");
                        if (!currTile.nameUpper.contains("ledges3")) {
                           requirementsMet &= !currTile.isSolid
                              && !currTile.name.contains("door")
                              && !currTile.name.contains("bridge")
                              && !currTile.name.contains("rug");
                        }

                        if (currTile.routeBelongsTo != null) {
                           requirementsMet &= !currTile.routeBelongsTo.isDungeon;
                        }

                        if (!requirementsMet) {
                           currTile.onPressA(game);
                           return;
                        }

                        String gotTerrain = "grass";
                        if (currTile.nameUpper.contains("ledges3")) {
                           gotTerrain = "hard stone";
                        } else if (currTile.name.contains("sand") || currTile.name.equals("stalagmite1")) {
                           gotTerrain = "soft sand";
                        } else if (currTile.name.contains("snow") || currTile.name.contains("ice")) {
                           gotTerrain = "nevermeltice";
                        } else if (currTile.name.contains("mountain")) {
                           gotTerrain = "light clay";
                        } else if (currTile.name.contains("desert")) {
                           gotTerrain = "dry sand";
                        } else if (currTile.name.contains("flower")) {
                           gotTerrain = "flowers";
                        } else if (currTile.name.contains("volcano") || currTile.name.contains("soot")) {
                           gotTerrain = "ashen soil";
                        } else if (currTile.name.equals("green9")
                           || currTile.name.contains("green10")
                           || currTile.name.contains("green11")
                           || currTile.name.contains("green12")) {
                           gotTerrain = "cursed soil";
                        } else if (currTile.name.contains("ledges3")) {
                           gotTerrain = "hard stone";
                        } else if (currTile.name.contains("green_savanna")) {
                           gotTerrain = "dry soil";
                        } else if (currTile.routeBelongsTo != null && currTile.routeBelongsTo.name.equals("wooded_lake1")) {
                           gotTerrain = "damp soil";
                        } else if (currTile.routeBelongsTo != null && currTile.routeBelongsTo.name.equals("deep_forest")) {
                           gotTerrain = "mystic soil";
                        }

                        if (currTile.nameUpper.equals("desert4_cracked")) {
                           String[] fossilTypes = new String[]{
                              "old amber",
                              "dome fossil",
                              "helix fossil",
                              "claw fossil",
                              "root fossil",
                              "shield fossil",
                              "skull fossil",
                              "sun stone",
                              "fire stone"
                           };
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("forest_cracked")) {
                           String[] fossilTypes = new String[]{"leaf stone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("deepforest_cracked")) {
                           String[] fossilTypes = new String[]{"dawn stone", "shiny stone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("graveyard_cracked")) {
                           String[] fossilTypes = new String[]{"dusk stone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("snow_cracked")) {
                           String[] fossilTypes = new String[]{"ice stone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("volcano_cracked")) {
                           String[] fossilTypes = new String[]{"fire stone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("beach_cracked")) {
                           String[] fossilTypes = new String[]{"water stone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("mountain3_cracked")) {
                           String[] fossilTypes = new String[]{"thunderstone"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        } else if (currTile.nameUpper.equals("tidal1_cracked")) {
                           String[] fossilTypes = new String[]{"clear glass", "water stone", "revive", "ultra ball", "big pearl", "star piece"};
                           currTile.hasItem = fossilTypes[Game.rand.nextInt(fossilTypes.length)];
                           currTile.hasItemAmount = 1;
                        }

                        currTile.nameUpper = game.player.currDigTile.name;
                        currTile.init();
                        currTile.updateMiniMap(game);
                        game.map.adjustSurroundingTiles(currTile);
                        game.insertAction(new PlayMusic("dig1", 1.0F, true, null));
                        game.insertAction(new OverworldAnimation(game, "dig_overworld_gsc", currTile.position.cpy().add(-104.0F, -52.0F), true, null));
                        game.insertAction(new OverworldAnimation(game, "dig_overworld_gsc", currTile.position.cpy().add(-40.0F, -52.0F), false, null));
                        game.playerCanMove = false;
                        game.player.currFieldMove = "";
                        game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
                        game.insertAction(
                           new WaitFrames(
                              game,
                              8,
                              new WaitFrames(game, 8, new SetField(game.player, "currFieldMove", "DIG", new SetField(game, "playerCanMove", true, null)))
                           )
                        );
                        game.insertAction(
                           new WaitFrames(game, 10, new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing), null))
                        );
                        HashMap<String, Integer> items = new HashMap<>();
                        items.put(gotTerrain, 1);
                        game.insertAction(new DrawItemPickup(items, null));
                        if (game.player.hasItem(gotTerrain)) {
                           int currQuantity = game.player.getItemAmount(gotTerrain);
                           game.player.setItemAmount(gotTerrain, currQuantity + 1);
                        } else {
                           game.player.setItemAmount(gotTerrain, 1);
                        }

                        return;
                     }

                     if (game.player.currFieldMove.equals("HEADBUTT")) {
                        if (currTile.isHeadbuttable) {
                           Action nextAction = new SetField(game, "playerCanMove", true, null);
                           if (currTile.routeBelongsTo != null && !currTile.routeBelongsTo.storedPokemon.isEmpty()) {
                              game.playerCanMove = false;
                              game.battle.oppPokemon = currTile.routeBelongsTo.storedPokemon.get(0);
                              game.player.setCurrPokemon();
                              game.map.currRoute = currTile.routeBelongsTo;
                              nextAction = new WaitFrames(game, 16, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)));
                              this.checkWildEncounter = false;
                              shouldMove = false;
                           }

                           game.insertAction(new HeadbuttTreeAnim(game, game.map.tiles.get(pos), nextAction));
                           game.playerCanMove = false;
                        }
                     } else if (!game.player.currFieldMove.equals("CUT")) {
                        if (game.player.currFieldMove.equals("SMASH")) {
                           if (currTile.isSmashable) {
                              Action action = new CutTreeAnim(game, game.map.tiles.get(pos), null);
                              game.playerCanMove = false;
                              currTile.items().remove("torch");
                              if (!currTile.items().isEmpty()) {
                                 action.append(new SplitAction(new DrawItemPickup(currTile.items(), null), null));

                                 for (String item : currTile.items().keySet()) {
                                    if (game.player.hasItem(item)) {
                                       int currQuantity = game.player.getItemAmount(item);
                                       game.player.setItemAmount(item, currQuantity + currTile.items().get(item));
                                    } else {
                                       game.player.setItemAmount(item, currTile.items().get(item));
                                    }
                                 }

                                 currTile.items().clear();
                              }

                              if (currTile.routeBelongsTo != null && !currTile.routeBelongsTo.storedPokemon.isEmpty()) {
                                 game.player.setCurrPokemon();
                                 game.playerCanMove = false;
                                 game.battle.oppPokemon = currTile.routeBelongsTo.storedPokemon.get(0);
                                 action.append(new WaitFrames(game, 16, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game))));
                                 this.checkWildEncounter = false;
                                 shouldMove = false;
                              } else if (currTile.nameUpper.contains("gravestone") && Game.rand.nextInt(9) == 0) {
                                 game.player.setCurrPokemon();
                                 game.playerCanMove = false;
                                 String[] pokemon = new String[]{
                                    "cubone",
                                    "marowak",
                                    "whismur",
                                    "machop",
                                    "machoke",
                                    "phanpy",
                                    "makuhita",
                                    "houndour",
                                    "growlithe",
                                    "oddish",
                                    "pidgey",
                                    "hoppip",
                                    "taillow",
                                    "bulbasaur",
                                    "charmander",
                                    "chikorita",
                                    "pikachu",
                                    "mankey",
                                    "ponyta",
                                    "cyndaquil",
                                    "eevee",
                                    "rattata",
                                    "sentret",
                                    "mareep",
                                    "squirtle",
                                    "totodile",
                                    "marill",
                                    "sandshrew",
                                    "sandslash",
                                    "swinub"
                                 };
                                 game.battle.oppPokemon = new Pokemon(pokemon[Game.rand.nextInt(pokemon.length)], 22);
                                 game.battle.oppPokemon.spookify();
                                 action.append(
                                    new SplitAction(
                                       new CallMethod(game.currMusic, "pause", new Object[0], new PlayMusic("ledge2", null)),
                                       game.player.new Emote(
                                          "!",
                                          new DisplayText(
                                             game,
                                             "You feel a presence around you...",
                                             null,
                                             null,
                                             new WaitFrames(game, 16, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
                                          )
                                       )
                                    )
                                 );
                              }

                              action.append(new SetField(game, "playerCanMove", true, null));
                              game.insertAction(action);
                           }
                        } else {
                           currTile.onPressA(game);
                        }
                     } else {
                        if (currTile.nameUpper.equals("solid")) {
                           Vector2[] positions = new Vector2[]{pos.cpy().add(-16.0F, 0.0F), pos.cpy().add(-16.0F, -16.0F), pos.cpy().add(0.0F, -16.0F)};

                           for (Vector2 position : positions) {
                              Tile tile = game.map.tiles.get(position);
                              if (tile.nameUpper.equals("aloe_large1")) {
                                 pos = position;
                                 currTile = game.map.tiles.get(pos);
                                 break;
                              }
                           }
                        }

                        if (currTile.isCuttable) {
                           for (int interiorIndex = 100; interiorIndex < game.map.interiorTiles.size(); interiorIndex++) {
                              HashMap<Vector2, Tile> interiorTiles = game.map.interiorTiles.get(interiorIndex);
                              Tile interiorTile = interiorTiles.get(pos);
                              if (interiorTile != null
                                 && interiorTile.nameUpper.contains("chest1")
                                 && interiorTile.routeBelongsTo != null
                                 && !interiorTile.routeBelongsTo.storedPokemon.isEmpty()) {
                                 game.playerCanMove = false;
                                 game.insertAction(
                                    new DisplayText(
                                       game,
                                       "A box indoors must be emptied before this can be CUT.",
                                       null,
                                       null,
                                       new SetField(game, "playerCanMove", true, null)
                                    )
                                 );
                                 return;
                              }
                           }

                           if (currTile.nameUpper.contains("chest1") && currTile.routeBelongsTo != null && !currTile.routeBelongsTo.storedPokemon.isEmpty()) {
                              game.playerCanMove = false;
                              game.insertAction(
                                 new DisplayText(
                                    game, "The box must be emptied before it can be CUT.", null, null, new SetField(game, "playerCanMove", true, null)
                                 )
                              );
                              return;
                           }

                           if (currTile.name.contains("tree_large1") || currTile.nameUpper.equals("tree_savanna1")) {
                              if (game.player.hmPokemon == null || game.player.hmPokemon.hasEvo()) {
                                 game.playerCanMove = false;
                                 game.insertAction(
                                    new DisplayText(
                                       game,
                                       game.player.hmPokemon.nickname.toUpperCase(Locale.ROOT) + " must be fully evolved to CUT large trees.",
                                       null,
                                       null,
                                       new SetField(game, "playerCanMove", true, null)
                                    )
                                 );
                                 return;
                              }

                              if (currTile.name.contains("tree_large1") && !currTile.name.equals("tree_large1")) {
                                 Vector2[] positions = new Vector2[]{pos.cpy().add(-16.0F, 0.0F), pos.cpy().add(-16.0F, -16.0F), pos.cpy().add(0.0F, -16.0F)};

                                 for (Vector2 position : positions) {
                                    Tile tile = game.map.tiles.get(position);
                                    if (tile.name.equals("tree_large1")) {
                                       pos = position;
                                       break;
                                    }
                                 }
                              }
                           }

                           Action action = new CutTreeAnim(game, game.map.tiles.get(pos), null);
                           Tile upTile = game.map.tiles.get(pos.cpy().add(0.0F, 16.0F));
                           if (upTile != null && upTile.name.contains("rug") && currTile.nameUpper.contains("roof")) {
                              game.map.tiles.put(upTile.position.cpy(), new Tile("green1", upTile.position.cpy(), true, upTile.routeBelongsTo));
                           }

                           Tile left = game.map.tiles.get(pos.cpy().add(-16.0F, 0.0F));
                           if (left != null && left.name.contains("rug") && (currTile.nameUpper.contains("E") || currTile.nameUpper.contains("left"))) {
                              game.map.tiles.put(left.position.cpy(), new Tile("green1", left.position.cpy(), true, left.routeBelongsTo));
                           }

                           Tile right = game.map.tiles.get(pos.cpy().add(16.0F, 0.0F));
                           if (right != null && right.name.contains("rug") && (currTile.nameUpper.contains("W") || currTile.nameUpper.contains("right"))) {
                              game.map.tiles.put(right.position.cpy(), new Tile("green1", right.position.cpy(), true, right.routeBelongsTo));
                           }

                           if (upTile != null
                              && (currTile.nameUpper.contains("bed") || currTile.nameUpper.equals("house_couch2") || currTile.nameUpper.equals("house_couch4"))
                              )
                            {
                              upTile.nameUpper = "";
                              upTile.isSolid = false;
                           }

                           if (right != null && currTile.nameUpper.equals("house_couch1")) {
                              right.nameUpper = "";
                              right.isSolid = false;
                           }

                           if (currTile.nameUpper.contains("stairs_up")) {
                              int interiorIndex = game.map.interiorTilesIndex + 1;
                              HashMap<Vector2, Tile> interiorTiles = game.map.getInteriorLayer(interiorIndex);
                              Tile aboveTile = interiorTiles.get(pos);
                              if (aboveTile != null && aboveTile.nameUpper.contains("stairs_down")) {
                                 aboveTile.nameUpper = "";
                                 aboveTile.overSprite = null;
                                 aboveTile.init();
                              }
                           }

                           if (game.map.tiles == game.map.overworldTiles) {
                              for (int interiorIndex = 100; interiorIndex < game.map.interiorTiles.size(); interiorIndex++) {
                                 HashMap<Vector2, Tile> interiorTiles = game.map.interiorTiles.get(interiorIndex);
                                 Tile interiorTile = interiorTiles.remove(pos);
                                 if (interiorTile != null) {
                                    upTile = interiorTiles.get(interiorTile.position.cpy().add(0.0F, 16.0F));
                                    if (upTile != null && (upTile.name.contains("wall") || upTile.name.contains("door"))) {
                                       interiorTiles.remove(upTile.position);
                                    }

                                    if (upTile != null
                                       && (
                                          currTile.nameUpper.contains("bed")
                                             || currTile.nameUpper.equals("house_couch2")
                                             || currTile.nameUpper.equals("house_couch4")
                                       )) {
                                       upTile.nameUpper = "";
                                       upTile.isSolid = false;
                                    }

                                    right = interiorTiles.get(interiorTile.position.cpy().add(16.0F, 0.0F));
                                    if (right != null && currTile.nameUpper.equals("house_couch1")) {
                                       right.nameUpper = "";
                                       right.isSolid = false;
                                    }

                                    for (String name : interiorTile.items().keySet()) {
                                       int value = interiorTile.items().get(name);
                                       if (currTile.items().containsKey(name)) {
                                          value += currTile.items().get(name);
                                       }

                                       currTile.items().put(name, value);
                                    }
                                 }
                              }
                           }

                           game.playerCanMove = false;
                           if (!currTile.items().isEmpty()) {
                              currTile.items().remove("torch");
                              action.append(new SplitAction(new DrawItemPickup(currTile.items(), null), null));

                              for (String item : currTile.items().keySet()) {
                                 System.out.println(item);
                                 if (game.player.hasItem(item)) {
                                    int currQuantity = game.player.getItemAmount(item);
                                    game.player.setItemAmount(item, currQuantity + currTile.items().get(item));
                                 } else {
                                    game.player.setItemAmount(item, currTile.items().get(item));
                                 }
                              }

                              currTile.items().clear();
                           }

                           action.append(new SetField(game, "playerCanMove", true, null));
                           game.insertAction(action);
                           if (game.type == Game.Type.CLIENT) {
                              game.client.sendTCP(new Network.UseHM(game.player.network.id, 0, "CUT", game.player.dirFacing));
                           }
                        }
                     }
                  }
               }

               if (this.isRunning) {
                  game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing + "_running"));
               } else {
                  game.player.currSprite = new Sprite(game.player.standingSprites.get(game.player.dirFacing));
               }

               if (shouldMove) {
                  game.actionStack.remove(this);
                  if (game.type == Game.Type.CLIENT) {
                     game.client.sendTCP(new Network.MovePlayer(game.player.network.id, game.player.dirFacing, InputProcessor.bPressed));
                  }

                  Tile facingTile = game.map.tiles.get(newPos);
                  String oppDir = Player.oppDirs.get(game.player.dirFacing);
                  if (!this.currTile.name.equals("rug2_right") && !this.currTile.name.equals("rug2_left")) {
                     if (this.currTile.name.contains("rug") && (facingTile == null || facingTile.name.contains("wall"))
                        || facingTile != null && facingTile.name.contains("entrance")
                        || game.map.tiles == game.map.overworldTiles && game.player.dirFacing.equals("down") && this.currTile.name.contains("rug")) {
                        if (game.map.tiles == game.map.overworldTiles) {
                           game.insertAction(
                              new EnterBuilding(game, "enter", game.map.interiorTiles.get(game.map.interiorTilesIndex), new PlayerMoving(game, this.alternate))
                           );
                        } else {
                           game.insertAction(new EnterBuilding(game, "exit", game.map.overworldTiles, new PlayerMoving(game, this.alternate)));
                        }

                        return;
                     }
                  } else {
                     if (game.player.dirFacing.equals("left") && this.currTile.name.equals("rug2_left")) {
                        if (game.map.tiles == game.map.overworldTiles) {
                           game.insertAction(
                              new EnterBuilding(game, "enter", game.map.interiorTiles.get(game.map.interiorTilesIndex), new PlayerMoving(game, this.alternate))
                           );
                        } else {
                           game.insertAction(new EnterBuilding(game, "exit", game.map.overworldTiles, new PlayerMoving(game, this.alternate)));
                        }

                        return;
                     }

                     if (game.player.dirFacing.equals("right") && this.currTile.name.equals("rug2_right")) {
                        if (game.map.tiles == game.map.overworldTiles) {
                           game.insertAction(
                              new EnterBuilding(game, "enter", game.map.interiorTiles.get(game.map.interiorTilesIndex), new PlayerMoving(game, this.alternate))
                           );
                        } else {
                           game.insertAction(new EnterBuilding(game, "exit", game.map.overworldTiles, new PlayerMoving(game, this.alternate)));
                        }

                        return;
                     }
                  }

                  if (facingTile == null) {
                     game.insertAction(new PlayerBump(game));
                  } else if (Gdx.input.isKeyPressed(62) && game.debugInputEnabled) {
                     if (InputProcessor.bPressed) {
                        game.insertAction(new PlayerRunning(game, this.alternate));
                     } else {
                        game.insertAction(new PlayerMoving(game, this.alternate));
                     }
                  } else if (game.map.pokemon.containsKey(newPos) && game.map.pokemon.get(newPos).mapTiles == game.map.tiles) {
                     game.insertAction(new PlayerBump(game));
                  } else {
                     if (game.player.currFieldMove.equals("RIDE")) {
                        if (facingTile.ledgeDir != null && (!facingTile.ledgeDir.equals("up") || game.player.dirFacing.equals("down"))) {
                           if (!facingTile.ledgeDir.equals(game.player.dirFacing) && !facingTile.ledgeDir.equals(oppDir)) {
                              if (facingTile.isLedge && this.currTile != null && this.currTile.isLedge && this.currTile.nameUpper.contains("inner")) {
                                 game.insertAction(new PlayerLedgeJumpFast(game));
                                 return;
                              }

                              game.insertAction(new PlayerBump(game));
                              return;
                           }

                           if (!facingTile.nameUpper.contains("fence")) {
                              game.insertAction(new PlayerLedgeJumpFast(game));
                              return;
                           }

                           Vector2 diff = newPos.cpy().sub(game.player.position);
                           Tile fartherOutTile = game.map.tiles.get(newPos.cpy().add(diff));
                           if (fartherOutTile != null && !fartherOutTile.isSolid) {
                              game.insertAction(new PlayerLedgeJump(game));
                           } else {
                              game.insertAction(new PlayerBump(game));
                           }

                           return;
                        }

                        if (!facingTile.isSolid
                           && this.currTile != null
                           && this.currTile.ledgeDir != null
                           && !this.currTile.ledgeDir.equals("up")
                           && (this.currTile.ledgeDir.equals(game.player.dirFacing) || this.currTile.ledgeDir.equals(oppDir))) {
                           game.insertAction(new PlayerLedgeJumpFast(game));
                           return;
                        }
                     }

                     if (game.player.currFieldMove.equals("SURF")) {
                        if (facingTile.isSolid && !facingTile.isWater) {
                           game.insertAction(new PlayerBump(game));
                        } else if (!facingTile.isWater) {
                           game.player.swapSprites(game.player.hmPokemon);
                           game.player.currFieldMove = "";
                           game.player.acceptInput = false;
                           game.player.hmPokemon.mapTiles = game.map.tiles;
                           game.player.hmPokemon.dirFacing = game.player.dirFacing;
                           game.player.hmPokemon.inWater = true;
                           game.player.hmPokemon.position = game.player.position.cpy();
                           game.player.hmPokemon.currOwSprite = game.player.hmPokemon.standingSprites.get(game.player.hmPokemon.dirFacing);
                           Pokemon.DrawUpper drawUpper = game.player.hmPokemon.new DrawUpper(true);
                           drawUpper.layer = Action.Layer.map_131;
                           drawUpper.firstStep(game);
                           drawUpper.firstStep = false;
                           game.insertAction(drawUpper);
                           game.insertAction(
                              new PlayerLedgeJumpFast(
                                 game,
                                 game.player,
                                 new SetField(
                                    game.player,
                                    "dirFacing",
                                    oppDir,
                                    new SetField(
                                       game.player,
                                       "currSprite",
                                       game.player.standingSprites.get(oppDir),
                                       new WaitFrames(
                                          game,
                                          24,
                                          new CallMethod(
                                             game.player.hmPokemon,
                                             "removeDrawActions",
                                             new Object[]{game},
                                             new PlayMusic("seed1", new SetField(game.player, "acceptInput", true, new PlayerStanding(game)))
                                          )
                                       )
                                    )
                                 )
                              )
                           );
                           game.player.hmPokemon = null;
                        } else if (!game.map.boundingBox().contains(newPos)) {
                           int something = 100;
                           int randNum = Game.rand.nextInt(4) + 2;
                           if (randNum > 4) {
                              something = 80;
                           } else if (randNum > 3) {
                              something = 90;
                           }

                           int size = 100 * something * (randNum + 2);
                           String awayDir;
                           if (newPos.y > game.map.boundingBox().y + game.map.boundingBox().height) {
                              awayDir = "down";
                           } else if (newPos.y < game.map.boundingBox().y) {
                              awayDir = "up";
                           } else if (newPos.x < game.map.boundingBox().x) {
                              awayDir = "right";
                           } else {
                              awayDir = "left";
                           }

                           game.insertAction(
                              new PlayerMoving(
                                 game,
                                 this.player,
                                 this.alternate,
                                 new RunCode(
                                    () -> game.playerCanMove = false,
                                    new DisplayText(
                                       game,
                                       "Going further will take you to a new area... keep going?",
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
                                                   true,
                                                   new GenerateWorld(
                                                      size,
                                                      this.player.dirFacing,
                                                      new RunCode(() -> game.playerCanMove = true, new PlayerMoving(game, this.alternate))
                                                   )
                                                )
                                             )
                                          ),
                                          new DisplayText.Clear(game, new WaitFrames(game, 3, new RunCode(() -> {
                                             game.playerCanMove = true;
                                             game.player.dirFacing = awayDir;
                                          }, new PlayerMoving(game, this.alternate))))
                                       )
                                    )
                                 )
                              )
                           );
                        } else {
                           game.insertAction(new PlayerMoving(game, this.alternate));
                        }
                     } else if (facingTile.isSolid) {
                        game.insertAction(new PlayerBump(game));
                     } else if (this.currTile != null
                        && this.currTile.ledgeDir != null
                        && this.currTile.ledgeDir.equals("up")
                        && game.player.dirFacing.equals("up")) {
                        game.insertAction(new PlayerLedgeJumpFast(game));
                     } else if (facingTile.ledgeDir != null && facingTile.ledgeDir.equals("up") && game.player.dirFacing.equals("down")) {
                        game.insertAction(new PlayerBump(game));
                     } else if (facingTile.ledgeDir != null && !facingTile.ledgeDir.equals("up")) {
                        Vector2 diff = newPos.cpy().sub(game.player.position);
                        Tile fartherOutTile = game.map.tiles.get(newPos.cpy().add(diff));
                        if (!facingTile.ledgeDir.equals(game.player.dirFacing)
                           || fartherOutTile != null && (fartherOutTile.isSolid || fartherOutTile.ledgeDir != null)) {
                           game.insertAction(new PlayerBump(game));
                        } else {
                           game.insertAction(new PlayerLedgeJump(game));
                        }
                     } else if (InputProcessor.bPressed
                        && !facingTile.name.contains("desert2")
                        && !facingTile.name.contains("soot1")
                        && !facingTile.name.contains("snow3")
                        && !facingTile.isTidal) {
                        game.insertAction(new PlayerRunning(game, this.alternate));
                     } else {
                        game.insertAction(new PlayerMoving(game, this.alternate));
                     }
                  }
               } else {
                  this.alternate = false;
                  this.isRunning = false;
               }
            }
         }
      }
   }

   public void remoteStep(Game game) {
      if (this.player.canMove) {
         Vector2 newPos = new Vector2();
         if (game.type == Game.Type.SERVER) {
            if (this.player.network.syncTimer > 0) {
               this.player.network.syncTimer++;
            }

            if (this.player.network.syncTimer > 240) {
               this.player.network.syncTimer = 0;
            }

            if (!this.player.network.loadingZone.inner.contains(this.player.position)) {
               LoadingZone oldZone = new LoadingZone(this.player.network.loadingZone);
               oldZone.inner = new LoadingZone(this.player.network.loadingZone.inner);
               if (this.player.position.x < oldZone.inner.x) {
                  this.player.network.loadingZone.translate(-112.0F, 0.0F);
               } else if (this.player.position.x >= oldZone.inner.topRight().x) {
                  this.player.network.loadingZone.translate(112.0F, 0.0F);
               }

               if (this.player.position.y < oldZone.inner.y) {
                  this.player.network.loadingZone.translate(0.0F, -112.0F);
               } else if (this.player.position.y >= oldZone.inner.topRight().y) {
                  this.player.network.loadingZone.translate(0.0F, 112.0F);
               }

               Network.MapTiles mapTiles = new Network.MapTiles();

               for (Vector2 position : this.player.network.loadingZone.diff(oldZone)) {
                  Tile tile = game.map.tiles.get(position);
                  if (tile != null) {
                     mapTiles.tiles.add(new Network.TileData(tile));
                     tile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(position);
                     if (tile != null) {
                        mapTiles.tiles.add(new Network.TileData(tile, game.map.interiorTilesIndex));
                     }

                     if (mapTiles.tiles.size() >= 14) {
                        game.server.sendToTCP(this.player.network.connectionId, mapTiles);
                        mapTiles.tiles.clear();
                     }

                     if (game.map.pokemon.containsKey(position)) {
                        Pokemon pokemon = game.map.pokemon.get(position);
                        game.server.sendToTCP(this.player.network.connectionId, new Network.OverworldPokemonData(pokemon, position));
                     }
                  }
               }

               game.server.sendToTCP(this.player.network.connectionId, mapTiles);

               for (Player otherPlayer : game.players.values()) {
                  if (otherPlayer != this.player && this.player.network.loadingZone.contains(otherPlayer.position)) {
                     Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                     game.server.sendToTCP(this.player.network.connectionId, serverPlayerData);
                  }
               }
            }
         }

         if (this.player.network.isRunning) {
            this.player.currSprite = new Sprite(this.player.standingSprites.get(this.player.dirFacing + "_running"));
         } else {
            this.player.currSprite = new Sprite(this.player.standingSprites.get(this.player.dirFacing));
         }

         if (this.player.network.tiles.get(this.player.position) == null
            || !this.player.network.tiles.get(this.player.position).nameUpper.contains("door")
               && !this.player.network.tiles.get(this.player.position).name.contains("door")) {
            if (this.player.network.shouldMove) {
               game.actionStack.remove(this);
               this.player.network.shouldMove = false;
               if (this.player.network.dirFacing.equals("up")) {
                  this.player.dirFacing = "up";
                  newPos = new Vector2(this.player.position.x, this.player.position.y + 16.0F);
               } else if (this.player.network.dirFacing.equals("down")) {
                  this.player.dirFacing = "down";
                  newPos = new Vector2(this.player.position.x, this.player.position.y - 16.0F);
               } else if (this.player.network.dirFacing.equals("left")) {
                  this.player.dirFacing = "left";
                  newPos = new Vector2(this.player.position.x - 16.0F, this.player.position.y);
               } else if (this.player.network.dirFacing.equals("right")) {
                  this.player.dirFacing = "right";
                  newPos = new Vector2(this.player.position.x + 16.0F, this.player.position.y);
               }

               Tile currTile = this.player.network.tiles.get(this.player.position);
               Tile temp = this.player.network.tiles.get(newPos);
               if (this.player.dirFacing.equals("down") && currTile != null && currTile.name.contains("rug")) {
                  if (this.player.network.tiles == game.map.overworldTiles) {
                     this.player.network.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                     game.insertAction(new PlayerMoving(game, this.player, this.alternate));
                  } else {
                     this.player.network.tiles = game.map.overworldTiles;
                     game.insertAction(new PlayerMoving(game, this.player, this.alternate));
                  }
               } else {
                  String oppDir = "";
                  if (this.player.dirFacing.equals("up")) {
                     oppDir = "down";
                  } else if (this.player.dirFacing.equals("down")) {
                     oppDir = "up";
                  } else if (this.player.dirFacing.equals("right")) {
                     oppDir = "left";
                  } else if (this.player.dirFacing.equals("left")) {
                     oppDir = "right";
                  }

                  if (temp == null) {
                     game.insertAction(new PlayerBump(game, this.player));
                  } else if (temp.isSolid) {
                     game.insertAction(new PlayerBump(game, this.player));
                  } else if (game.map.pokemon.containsKey(newPos) && game.map.pokemon.get(newPos).mapTiles == game.map.tiles) {
                     game.insertAction(new PlayerBump(game, this.player));
                  } else {
                     if (game.player.currFieldMove.equals("RIDE")) {
                        if (temp.isLedge && (!temp.ledgeDir().equals("up") || this.player.dirFacing.equals("down"))) {
                           if (!temp.ledgeDir().equals(this.player.dirFacing) && !temp.ledgeDir().equals(oppDir)) {
                              game.insertAction(new PlayerBump(game, this.player));
                              return;
                           }

                           game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                           return;
                        }

                        if (currTile != null
                           && currTile.isLedge
                           && !currTile.ledgeDir().equals("up")
                           && (currTile.ledgeDir().equals(this.player.dirFacing) || currTile.ledgeDir().equals(oppDir))) {
                           game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                           return;
                        }
                     }

                     if (currTile != null && currTile.isLedge && currTile.ledgeDir().equals("up") && this.player.dirFacing.equals("up")) {
                        game.insertAction(new PlayerLedgeJumpFast(game, this.player));
                     } else if (temp.isLedge && temp.ledgeDir().equals("up") && this.player.dirFacing.equals("down")) {
                        game.insertAction(new PlayerBump(game, this.player));
                     } else if (temp.isLedge && !temp.ledgeDir().equals("up")) {
                        Vector2 diff = newPos.cpy().sub(this.player.position);
                        Tile fartherOutTile = game.map.tiles.get(newPos.cpy().add(diff));
                        if (!temp.ledgeDir().equals(this.player.dirFacing) || fartherOutTile != null && (fartherOutTile.isSolid || fartherOutTile.isLedge)) {
                           game.insertAction(new PlayerBump(game, this.player));
                        } else {
                           game.insertAction(new PlayerLedgeJump(game, this.player));
                        }
                     } else {
                        if (this.player.network.isRunning) {
                           game.insertAction(new PlayerRunning(game, this.player, this.alternate));
                        } else {
                           game.insertAction(new PlayerMoving(game, this.player, this.alternate));
                        }

                        if (game.type == Game.Type.SERVER) {
                           Pokemon pokemon = this.checkWildEncounter(game, newPos);
                           if (pokemon != null) {
                              this.player.setCurrPokemon();
                              this.player.canMove = false;
                              game.server.sendToTCP(this.player.network.connectionId, new Network.BattleData(pokemon));
                              game.battles.put(this.player.network.id, new Battle());
                              game.battles.get(this.player.network.id).oppPokemon = pokemon;
                           }
                        }
                     }
                  }
               }
            } else {
               this.player.network.isRunning = false;
            }
         } else {
            if (this.player.network.tiles == game.map.overworldTiles) {
               this.player.network.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
            } else {
               this.player.network.tiles = game.map.overworldTiles;
            }
         }
      }
   }

   @Override
   public void firstStep(Game game) {
      if (this.player == null) {
         this.player = game.player;
      }
   }

   @Override
   public void step(Game game) {
      if (this.player.isSleeping) {
         if (this.player.zsTimer < 64) {
            this.player.zSprite.setPosition(this.player.position.x + 8.0F, this.player.position.y + 18.0F);
         } else {
            this.player.zSprite.setPosition(this.player.position.x + 16.0F, this.player.position.y + 18.0F);
         }

         this.player.zsTimer++;
         if (this.player.zsTimer >= 128) {
            boolean outdoors = this.player.network.tiles == game.map.overworldTiles;
            if (this.player.type == Player.Type.LOCAL) {
               outdoors = game.map.tiles == game.map.overworldTiles;
            }

            for (Pokemon pokemon : this.player.pokemon) {
               if (!pokemon.isEgg) {
                  if (outdoors) {
                     pokemon.currentStats.put("hp", pokemon.currentStats.get("hp") + (int)Math.ceil(pokemon.maxStats.get("hp").intValue() / 50.0F));
                  } else {
                     pokemon.currentStats.put("hp", pokemon.currentStats.get("hp") + (int)Math.ceil(pokemon.maxStats.get("hp").intValue() / 25.0F));
                  }

                  if (this.player.sleepingDir != null) {
                     pokemon.currentStats.put("hp", pokemon.currentStats.get("hp") + (int)Math.ceil(pokemon.maxStats.get("hp").intValue() / 15.0F));
                  }

                  if (pokemon.currentStats.get("hp") >= pokemon.maxStats.get("hp")) {
                     pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                     if (this.player.sleepingDir != null) {
                        pokemon.status = null;
                     }
                  }
               }
            }

            this.player.zsTimer = 0;
         }
      }

      if (this.player.type == Player.Type.LOCAL) {
         this.localStep(game);
      } else if (this.player.type == Player.Type.REMOTE) {
         this.remoteStep(game);
      }
   }
}
