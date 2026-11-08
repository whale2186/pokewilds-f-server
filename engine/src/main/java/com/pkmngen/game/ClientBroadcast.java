package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

class ClientBroadcast extends Action {
   int playerIndex;
   int timeStep = 0;
   public Action.Layer layer = Action.Layer.map_1;

   public Action deserializeAction(Network.ActionData actionData) {
      Action newAction = null;

      try {
         Class cl = Class.forName(actionData.className);
         Class[] paramTypes = new Class[actionData.params.length];

         for (int i = 0; i < actionData.params.length; i++) {
            if (actionData.params[i] instanceof Network.ActionData) {
               actionData.params[i] = this.deserializeAction((Network.ActionData)actionData.params[i]);
            }

            paramTypes[i] = actionData.params[i].getClass();
         }

         Constructor con = cl.getConstructor(paramTypes);
         newAction = (Action)con.newInstance(actionData.params);
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

      return newAction;
   }

   public ClientBroadcast(final Game game) {
      super();
      this.playerIndex = 1;
      game.client
         .addListener(
            new Listener() {
               @Override
               public void connected(Connection connection) {
               }

               @Override
               public void received(Connection connection, final Object object) {
                  if (object instanceof Network.ActionData) {
                     Network.ActionData actionData = (Network.ActionData)object;
                     game.insertAction(ClientBroadcast.this.deserializeAction(actionData));
                  }

                  Runnable runnable = new Runnable() {
                     @Override
                     public void run() {
                        try {
                           if (object instanceof Network.PlayerData) {
                              Network.PlayerData playerData = (Network.PlayerData)object;
                              game.player = new Player(playerData);
                              game.cam.position.set(game.player.position.x + 16.0F, game.player.position.y, 0.0F);
                           } else if (object instanceof Network.RelocatePlayer) {
                              Network.RelocatePlayer relocatePlayer = (Network.RelocatePlayer)object;
                              game.player.position = relocatePlayer.position.cpy();
                              game.cam.position.set(relocatePlayer.position.cpy().add(16.0F, 0.0F), 0.0F);
                           } else if (object instanceof Network.Logout) {
                              Network.Logout logout = (Network.Logout)object;
                              if (!game.players.containsKey(logout.playerId)) {
                                 System.out.println("Logout: Invalid player ID " + logout.playerId + ", sent by server");
                                 throw new Exception();
                              }

                              Player player = game.players.get(logout.playerId);
                              game.actionStack.remove(player.standingAction);
                              game.players.remove(logout.playerId);
                           } else if (object instanceof Network.MapTiles) {
                              Network.MapTiles mapTiles = (Network.MapTiles)object;

                              for (Network.TileDataBase tileData : mapTiles.tiles) {
                                 Tile tile = Tile.get(tileData, null);
                                 if (tileData.interiorIndex != 0) {
                                    game.map.interiorTiles.get(tileData.interiorIndex).put(tileData.pos.cpy(), tile);
                                 } else {
                                    game.map.overworldTiles.put(tileData.pos.cpy(), tile);
                                 }
                              }

                              if (mapTiles.timeOfDay != null) {
                                 CycleDayNight.dayTimer = mapTiles.dayTimer;
                                 game.map.timeOfDay = mapTiles.timeOfDay;
                              }
                           } else if (object instanceof Network.ServerPlayerData) {
                              Network.ServerPlayerData serverPlayerData = (Network.ServerPlayerData)object;
                              if (!game.players.containsKey(serverPlayerData.number)) {
                                 Player player = new Player();
                                 player.name = serverPlayerData.name;
                                 player.position = serverPlayerData.position.cpy();
                                 player.type = Player.Type.REMOTE;
                                 player.network.id = serverPlayerData.number;
                                 if (serverPlayerData.isInterior) {
                                    player.network.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
                                 } else {
                                    player.network.tiles = game.map.overworldTiles;
                                 }

                                 player.setColor(serverPlayerData.color);
                                 game.players.put(serverPlayerData.number, player);
                              }

                              Player player = game.players.get(serverPlayerData.number);
                              if (game.actionStack.contains(player.standingAction)) {
                                 player.standingAction = new PlayerStanding(game, player, false, true);
                                 game.insertAction(player.standingAction);
                              }
                           } else if (object instanceof Network.MovePlayer) {
                              Network.MovePlayer movePlayer = (Network.MovePlayer)object;
                              if (!game.players.containsKey(movePlayer.playerId)) {
                                 System.out.println("MovePlayer: Invalid player ID " + movePlayer.playerId + ", sent by server");
                                 throw new Exception();
                              }

                              Player player = game.players.get(movePlayer.playerId);
                              player.network.shouldMove = true;
                              player.network.dirFacing = movePlayer.dirFacing;
                              player.network.isRunning = movePlayer.isRunning;
                           } else if (object instanceof Network.MovePokemon) {
                              Network.MovePokemon movePokemon = (Network.MovePokemon)object;
                              if (!game.map.pokemon.containsKey(movePokemon.position)) {
                                 System.out.println("MovePokemon: Invalid pokemon position " + movePokemon.position.toString() + ", sent by server");
                                 throw new Exception();
                              }

                              Pokemon pokemon = game.map.pokemon.get(movePokemon.position);
                              pokemon.dirFacing = movePokemon.dirFacing;
                              pokemon.shouldMove = true;
                           } else if (object instanceof Network.BattleData) {
                              Network.BattleData battleData = (Network.BattleData)object;
                              game.player.network.doEncounter = battleData;
                           } else if (object instanceof Network.BattleTurnData) {
                              Network.BattleTurnData turnData = (Network.BattleTurnData)object;
                              game.battle.network.turnData = turnData;
                           } else if (object instanceof Network.TileData) {
                              Network.TileData tileData = (Network.TileData)object;
                              Tile newTile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, null);
                              newTile.hasItem = tileData.hasItem;
                              newTile.hasItemAmount = tileData.hasItemAmount;
                              game.map.tiles.put(tileData.pos.cpy(), newTile);
                              game.map.adjustSurroundingTiles(newTile);
                           } else if (object instanceof Network.OverworldPokemonData) {
                              Network.OverworldPokemonData pokemonData = (Network.OverworldPokemonData)object;
                              if (pokemonData.remove) {
                                 Pokemon pokemon = game.map.pokemon.get(pokemonData.overworldPos);
                                 if (pokemon == null) {
                                    System.out.println("OverworldPokemonData: Invalid position " + pokemonData.overworldPos.toString() + ", sent by server");
                                    throw new Exception();
                                 }

                                 game.map.pokemon.remove(pokemonData.overworldPos);
                                 game.actionStack.remove(pokemon.standingAction);
                              } else {
                                 Pokemon pokemon = new Pokemon(pokemonData);
                                 pokemon.position = pokemonData.overworldPos.cpy();
                                 game.map.pokemon.put(pokemonData.overworldPos, pokemon);
                                 pokemon.type = Player.Type.REMOTE;
                                 game.insertAction(pokemon.new Standing());
                              }
                           } else if (object instanceof Network.PokemonData) {
                              Network.PokemonData pokemonData = (Network.PokemonData)object;
                              game.player.pokemon.set(pokemonData.index, new Pokemon(pokemonData));
                           } else if (object instanceof Network.PickupItem) {
                              Network.PickupItem pickupItem = (Network.PickupItem)object;
                              Tile tile = game.map.tiles.get(pickupItem.pos);
                              game.playerCanMove = false;
                              String number = "a";
                              String plural = "";
                              if (tile.hasItemAmount > 1) {
                                 number = String.valueOf(tile.hasItemAmount);
                                 plural = "S";
                                 if (tile.hasItem.endsWith("s")) {
                                    plural = "ES";
                                 }
                              }

                              game.insertAction(
                                 new DisplayText(
                                    game,
                                    "Found " + number + " " + tile.hasItem.toUpperCase(Locale.ROOT) + plural + "!",
                                    "fanfare1.ogg",
                                    null,
                                    new SetField(game, "playerCanMove", true, null)
                                 )
                              );
                              tile.pickUpItem(game.player);
                           } else if (object instanceof Network.UseHM) {
                              Network.UseHM useHM = (Network.UseHM)object;
                              if (!game.players.containsKey(useHM.playerId)) {
                                 System.out.println("UseHM: Invalid player id " + useHM.playerId + ", sent by: server");
                                 throw new Exception();
                              }

                              Player player = game.players.get(useHM.playerId);
                              if (useHM.hm.equals("STOP")) {
                                 player.currFieldMove = "";
                              } else if (useHM.hm.equals("CUT")) {
                                 Vector2 pos = player.facingPos(useHM.dirFacing);
                                 Tile currTile = player.network.tiles.get(pos);
                                 if (currTile.isCuttable) {
                                    Action action = new CutTreeAnim(game, game.map.overworldTiles.get(pos), null);
                                    game.map.interiorTiles.get(game.map.interiorTilesIndex).remove(currTile.position.cpy());
                                    game.insertAction(action);
                                 }
                              } else if (useHM.hm.equals("JUMP")) {
                                 player.currFieldMove = "RIDE";
                              }
                           } else if (object instanceof Network.UseItem) {
                              Network.UseItem useItem = (Network.UseItem)object;
                              if (!game.players.containsKey(useItem.playerId)) {
                                 System.out.println("UseHM: Invalid player id " + useItem.playerId + ", sent by: server");
                                 throw new Exception();
                              }

                              Player player = game.players.get(useItem.playerId);
                              Vector2 pos = player.facingPos(useItem.dirFacing);
                              if (useItem.item.contains("apricorn")) {
                                 game.insertAction(new PlantTree(pos, null));
                              }
                           } else if (object instanceof Network.PausePokemon) {
                              Network.PausePokemon pausePokemon = (Network.PausePokemon)object;
                              Pokemon pokemon = game.map.pokemon.get(pausePokemon.position);
                              pokemon.canMove = false;
                              game.playerCanMove = false;
                              String oppDir = "down";
                              if (game.player.dirFacing.equals("up")) {
                                 oppDir = "down";
                              } else if (game.player.dirFacing.equals("down")) {
                                 oppDir = "up";
                              } else if (game.player.dirFacing.equals("right")) {
                                 oppDir = "left";
                              } else if (game.player.dirFacing.equals("left")) {
                                 oppDir = "right";
                              }

                              Action nextAction = new SetField(
                                 pokemon,
                                 "dirFacing",
                                 oppDir,
                                 new WaitFrames(
                                    game, 20, new SplitAction(pokemon.new Emote("happy", null), new WaitFrames(game, 20, new PlayMusic(pokemon, null)))
                                 )
                              );
                              if (pokemon.previousOwner != game.player) {
                                 nextAction.append(
                                    new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " seems friendly. ", null, false, true, null)
                                 );
                              }

                              nextAction.append(
                                 new DisplayText(
                                    game,
                                    "Add " + pokemon.nickname.toUpperCase(Locale.ROOT) + " to your party?",
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
                                             pokemon.new AddToInventory(
                                                pausePokemon.position, new SetField(game, "playerCanMove", true, new SetField(pokemon, "canMove", true, null))
                                             )
                                          )
                                       ),
                                       new DisplayText.Clear(
                                          game,
                                          new WaitFrames(
                                             game, 3, new SetField(game, "playerCanMove", true, new SetField(pokemon, "canMove", true, pokemon.new UnPause()))
                                          )
                                       )
                                    )
                                 )
                              );
                              game.insertAction(nextAction);
                           }
                        } catch (Exception e) {
                           e.printStackTrace();
                        }

                        synchronized (this) {
                           this.notify();
                        }
                     }
                  };
                  Gdx.app.postRunnable(runnable);

                  try {
                     synchronized (runnable) {
                        runnable.wait();
                     }
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
               }
            }
         );
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }
}
