package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.Locale;

class ServerBroadcast extends Action {
   int timeStep = 0;
   public Action.Layer layer = Action.Layer.map_1;

   public ServerBroadcast(final Game game) {
      super();
      game.server
         .addListener(
            new Listener() {
               @Override
               public void received(final Connection connection, final Object object) {
                  Runnable runnable = new Runnable() {
                     @Override
                     public void run() {
                        try {
                           if (object instanceof Network.Login) {
                              Network.Login login = (Network.Login)object;
                              if (!game.players.containsKey(login.playerId)) {
                                 String playerId = login.playerId;
                                 Player player = new Player();
                                 Vector2 startLoc = game.map.edges.get(game.map.rand.nextInt(game.map.edges.size()));
                                 player.position.set(startLoc);
                                 player.spawnLoc.set(startLoc);
                                 player.type = Player.Type.REMOTE;
                                 player.network = player.new Network(player.position);
                                 player.network.connectionId = connection.getID();
                                 player.network.id = playerId;
                                 player.network.number = String.valueOf(game.players.keySet().size());
                                 player.setColor(login.color);
                                 player.network.tiles = game.map.overworldTiles;
                                 player.currPokemon = new Pokemon("machop", 6);
                                 player.pokemon.add(player.currPokemon);
                                 game.players.put(playerId, player);
                              }

                              Player player = game.players.get(login.playerId);
                              player.network.connectionId = connection.getID();
                              Network.PlayerData playerData = new Network.PlayerData(player);
                              connection.sendTCP(playerData);
                              if (game.battles.containsKey(player.network.id)) {
                                 Battle battle = game.battles.get(player.network.id);
                                 Pokemon oppPokemon = battle.oppPokemon;
                                 connection.sendTCP(new Network.BattleData(oppPokemon));
                              }

                              if (player.standingAction == null) {
                                 player.standingAction = new PlayerStanding(game, player, false, true);
                                 game.insertAction(player.standingAction);
                              }

                              Network.MapTiles mapTiles = new Network.MapTiles();

                              for (Vector2 position : player.network.loadingZone.allPositions()) {
                                 Tile tile = game.map.overworldTiles.get(position);
                                 if (tile != null) {
                                    mapTiles.tiles.add(new Network.TileData(tile));
                                    tile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(position);
                                    if (tile != null) {
                                       mapTiles.tiles.add(new Network.TileData(tile, game.map.interiorTilesIndex));
                                    }

                                    if (game.map.pokemon.containsKey(position)) {
                                       Pokemon pokemon = game.map.pokemon.get(position);
                                       connection.sendTCP(new Network.OverworldPokemonData(pokemon, position));
                                    }

                                    if (mapTiles.tiles.size() >= 16) {
                                       connection.sendTCP(mapTiles);
                                       mapTiles.tiles.clear();
                                    }
                                 }
                              }

                              mapTiles.dayTimer = CycleDayNight.dayTimer;
                              mapTiles.timeOfDay = game.map.timeOfDay;
                              connection.sendTCP(mapTiles);

                              for (Player otherPlayer : game.players.values()) {
                                 if (otherPlayer != player && player.network.loadingZone.contains(otherPlayer.position)) {
                                    Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                                    connection.sendTCP(serverPlayerData);
                                    serverPlayerData = new Network.ServerPlayerData(player);
                                    game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                 }
                              }
                           } else if (object instanceof Network.Logout) {
                              Network.Logout logout = (Network.Logout)object;
                              if (!game.players.containsKey(logout.playerId)) {
                                 System.out
                                    .println("Logout: Invalid player ID " + logout.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(logout.playerId);

                              for (Player otherPlayer : game.players.values()) {
                                 if (otherPlayer != player && otherPlayer.network.loadingZone.contains(player.position)) {
                                    Network.Logout logoutPlayer = new Network.Logout(player.network.number);
                                    game.server.sendToTCP(otherPlayer.network.connectionId, logoutPlayer);
                                 }
                              }
                           } else if (object instanceof Network.MovePlayer) {
                              Network.MovePlayer movePlayer = (Network.MovePlayer)object;
                              if (!game.players.containsKey(movePlayer.playerId)) {
                                 System.out
                                    .println(
                                       "MovePlayer: Invalid player ID " + movePlayer.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player player = game.players.get(movePlayer.playerId);
                              player.network.shouldMove = true;
                              player.network.dirFacing = movePlayer.dirFacing;
                              player.network.isRunning = movePlayer.isRunning;
                              Vector2 nextPos = player.position.cpy();
                              if (movePlayer.dirFacing.equals("up")) {
                                 nextPos.add(0.0F, 16.0F);
                              } else if (movePlayer.dirFacing.equals("down")) {
                                 nextPos.add(0.0F, -16.0F);
                              } else if (movePlayer.dirFacing.equals("right")) {
                                 nextPos.add(16.0F, 0.0F);
                              } else if (movePlayer.dirFacing.equals("left")) {
                                 nextPos.add(-16.0F, 0.0F);
                              }

                              for (Player otherPlayer : game.players.values()) {
                                 if (otherPlayer != player && otherPlayer.network.loadingZone.contains(nextPos)) {
                                    if (!otherPlayer.network.loadingZone.contains(player.position)) {
                                       Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(player);
                                       game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                       serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                                       game.server.sendToTCP(player.network.connectionId, serverPlayerData);
                                    }

                                    game.server
                                       .sendToTCP(
                                          otherPlayer.network.connectionId,
                                          new Network.MovePlayer(player.network.number, player.network.dirFacing, player.network.isRunning)
                                       );
                                 }
                              }
                           } else if (object instanceof Network.DoBattleAction) {
                              Network.DoBattleAction battleAction = (Network.DoBattleAction)object;
                              if (!game.players.containsKey(battleAction.playerId)) {
                                 System.out
                                    .println(
                                       "DoAttack: Invalid player id " + battleAction.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player player = game.players.get(battleAction.playerId);
                              if (!game.battles.containsKey(battleAction.playerId)) {
                                 System.out
                                    .println(
                                       "DoAttack: player ID not currently in battle "
                                          + battleAction.playerId
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Battle battle = game.battles.get(battleAction.playerId);
                              Network.BattleTurnData turnData = new Network.BattleTurnData();
                              if (battleAction.type == Battle.DoTurn.Type.SWITCH) {
                                 turnData.oppFirst = false;
                                 player.numFlees = 0;
                                 player.currPokemon = player.pokemon.get(battleAction.pokemonIndex);
                              } else if (battleAction.type == Battle.DoTurn.Type.ITEM) {
                                 turnData.oppFirst = false;
                                 player.numFlees = 0;
                                 turnData.itemName = battleAction.itemName;
                                 player.setItemAmount(turnData.itemName, player.getItemAmount(turnData.itemName) - 1);
                                 if (player.getItemAmount(turnData.itemName) <= 0) {
                                    player.removeItem(turnData.itemName);
                                 }

                                 if (turnData.itemName.contains("ball")) {
                                    turnData.numWobbles = Battle.gen2CalcIfCaught(game, battle.oppPokemon, battleAction.itemName);
                                    if (turnData.numWobbles == -1) {
                                       player.pokemon.add(battle.oppPokemon);
                                       Route var81 = game.map.tiles.get(player.position).routeBelongsTo;
                                    }
                                 }
                              } else if (battleAction.type == Battle.DoTurn.Type.RUN) {
                                 turnData.oppFirst = false;
                                 turnData.runSuccessful = battle.calcIfRunSuccessful(game, player);
                                 if (turnData.runSuccessful) {
                                    player.numFlees = 0;
                                 } else {
                                    player.numFlees++;
                                 }
                              } else {
                                 player.numFlees = 0;
                                 boolean found = false;

                                 for (int i = 0; i < player.currPokemon.attacks.length; i++) {
                                    if (player.currPokemon.attacks[i] != null
                                       && player.currPokemon.attacks[i].toLowerCase(Locale.ROOT).equals(battleAction.attack.toLowerCase(Locale.ROOT))) {
                                       found = true;
                                       break;
                                    }
                                 }

                                 if (!found) {
                                    System.out
                                       .println(
                                          "DoAttack: Invalid attack choice "
                                             + battleAction.attack
                                             + ", sent by: "
                                             + connection.getRemoteAddressTCP().toString()
                                       );
                                    throw new Exception();
                                 }

                                 turnData.playerAttack = battle.attacks.get(battleAction.attack.toLowerCase(Locale.ROOT));
                                 int yourSpeed = player.currPokemon.currentStats.get("speed");
                                 int oppSpeed = battle.oppPokemon.currentStats.get("speed");
                                 if (yourSpeed > oppSpeed) {
                                    turnData.oppFirst = false;
                                 } else if (yourSpeed < oppSpeed) {
                                    turnData.oppFirst = true;
                                 } else if (game.map.rand.nextInt(2) == 0) {
                                    turnData.oppFirst = true;
                                 }
                              }

                              if (battle.network.expectPlayerSwitch) {
                                 if (battleAction.type == Battle.DoTurn.Type.SWITCH) {
                                    battle.network.expectPlayerSwitch = false;
                                 }

                                 System.out.println("battle.network.expectPlayerSwitch check");
                                 throw new Exception();
                              }

                              String attackChoice = battle.oppPokemon.attacks[game.map.rand.nextInt(battle.oppPokemon.attacks.length)];
                              if (attackChoice == null) {
                                 attackChoice = "Struggle";
                              }

                              turnData.enemyAttack = battle.attacks.get(attackChoice.toLowerCase(Locale.ROOT));
                              if (!turnData.oppFirst) {
                                 int finalHealth = battle.oppPokemon.currentStats.get("hp");
                                 if (battleAction.type == Battle.DoTurn.Type.ATTACK) {
                                    int damage = Battle.gen2CalcDamage(player.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                    int currHealth = battle.oppPokemon.currentStats.get("hp");
                                    finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    battle.oppPokemon.currentStats.put("hp", finalHealth);
                                    turnData.playerAttack.damage = damage;
                                    if (finalHealth <= 0) {
                                       Route currRoute = game.map.tiles.get(player.position).routeBelongsTo;
                                       player.currPokemon.exp = player.currPokemon.exp + battle.calcFaintExp(1);

                                       while (
                                          player.currPokemon.level < 100
                                             && player.currPokemon.calcExpForLevel(player.currPokemon.level + 1) <= player.currPokemon.exp
                                       ) {
                                          player.currPokemon.level++;
                                       }

                                       for (int i = 1; i <= player.currPokemon.level; i++) {
                                          if (Specie.gen2Evos.get(player.currPokemon.specie.name.toLowerCase(Locale.ROOT)).containsKey(String.valueOf(i))) {
                                             String evolveTo = Specie.gen2Evos
                                                .get(player.currPokemon.specie.name.toLowerCase(Locale.ROOT))
                                                .get(String.valueOf(i));
                                             player.currPokemon.evolveTo(evolveTo);
                                             break;
                                          }
                                       }
                                    }
                                 }

                                 if (battleAction.type == Battle.DoTurn.Type.ATTACK && finalHealth > 0
                                    || battleAction.type == Battle.DoTurn.Type.RUN && !turnData.runSuccessful
                                    || battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles != -1) {
                                    int damage = Battle.gen2CalcDamage(battle.oppPokemon, turnData.enemyAttack, player.currPokemon);
                                    int currHealth = player.currPokemon.currentStats.get("hp");
                                    finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    player.currPokemon.currentStats.put("hp", finalHealth);
                                    turnData.enemyAttack.damage = damage;
                                    if (finalHealth <= 0) {
                                       battle.network.expectPlayerSwitch = true;
                                    }
                                 }
                              } else {
                                 int damage = Battle.gen2CalcDamage(battle.oppPokemon, turnData.enemyAttack, player.currPokemon);
                                 int currHealth = player.currPokemon.currentStats.get("hp");
                                 int finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                 player.currPokemon.currentStats.put("hp", finalHealth);
                                 turnData.enemyAttack.damage = damage;
                                 if (finalHealth <= 0) {
                                    battle.network.expectPlayerSwitch = true;
                                 } else {
                                    damage = Battle.gen2CalcDamage(player.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                    currHealth = battle.oppPokemon.currentStats.get("hp");
                                    finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    battle.oppPokemon.currentStats.put("hp", finalHealth);
                                    turnData.playerAttack.damage = damage;
                                    if (finalHealth <= 0) {
                                       Route currRoute = game.map.tiles.get(player.position).routeBelongsTo;
                                       player.currPokemon.exp = player.currPokemon.exp + battle.calcFaintExp(1);

                                       while (
                                          player.currPokemon.level < 100
                                             && player.currPokemon.calcExpForLevel(player.currPokemon.level + 1) <= player.currPokemon.exp
                                       ) {
                                          player.currPokemon.level++;
                                       }

                                       for (int i = 1; i <= player.currPokemon.level; i++) {
                                          if (Specie.gen2Evos.get(player.currPokemon.specie.name.toLowerCase(Locale.ROOT)).containsKey(String.valueOf(i))) {
                                             String evolveTo = Specie.gen2Evos
                                                .get(player.currPokemon.specie.name.toLowerCase(Locale.ROOT))
                                                .get(String.valueOf(i));
                                             player.currPokemon.evolveTo(evolveTo);
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }

                              if (battle.oppPokemon.currentStats.get("hp") <= 0
                                 || battleAction.type == Battle.DoTurn.Type.RUN && turnData.runSuccessful
                                 || battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles == -1) {
                                 battle.oppPokemon.inBattle = false;
                                 player.canMove = true;
                                 player.numFlees = 0;
                                 game.battles.remove(player.network.id);
                              }

                              boolean hasAlivePokemon = false;

                              for (Pokemon pokemon : player.pokemon) {
                                 if (pokemon.currentStats.get("hp") > 0) {
                                    hasAlivePokemon = true;
                                    break;
                                 }
                              }

                              if (!hasAlivePokemon) {
                                 battle.oppPokemon.inBattle = false;
                                 player.canMove = true;
                                 player.numFlees = 0;
                                 game.battles.remove(player.network.id);
                                 player.position.set(player.spawnLoc);

                                 for (Pokemon pokemon : player.pokemon) {
                                    pokemon.currentStats.put("hp", pokemon.maxStats.get("hp") / 2);
                                 }

                                 for (Player otherPlayer : game.players.values()) {
                                    if (otherPlayer != player && otherPlayer.network.loadingZone.contains(player.position)) {
                                       Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(player);
                                       game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                    }
                                 }
                              }

                              System.out.println("Sending turn data.");
                              game.server.sendToTCP(player.network.connectionId, turnData);
                           } else if (object instanceof Network.TileData) {
                              Network.TileData tileData = (Network.TileData)object;
                              if (!game.map.tiles.containsKey(tileData.pos)) {
                                 System.out
                                    .println(
                                       "TileData: Invalid tile position "
                                          + tileData.pos.toString()
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              if (game.map.tiles.get(tileData.pos).isSolid) {
                                 throw new Exception();
                              }

                              for (Player player : game.players.values()) {
                                 if (player.position.equals(tileData.pos)) {
                                    throw new Exception();
                                 }
                              }

                              Tile oldTile = game.map.tiles.get(tileData.pos);
                              Tile newTile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, oldTile.routeBelongsTo);
                              newTile.hasItem = tileData.hasItem;
                              newTile.hasItemAmount = tileData.hasItemAmount;
                              game.map.tiles.put(tileData.pos.cpy(), newTile);
                              game.map.adjustSurroundingTiles(newTile);

                              for (Player player : game.players.values()) {
                                 if (player.network.loadingZone.contains(tileData.pos)) {
                                    game.server.sendToTCP(player.network.connectionId, tileData);
                                 }
                              }
                           } else if (object instanceof Network.UseHM) {
                              Network.UseHM useHM = (Network.UseHM)object;
                              if (!game.players.containsKey(useHM.playerId)) {
                                 System.out.println("UseHM: Invalid player id " + useHM.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(useHM.playerId);
                              if (!player.pokemon.get(useHM.pokemonIndex).hms.contains(useHM.hm)) {
                                 System.out
                                    .println(
                                       "UseHM: Invalid HM "
                                          + useHM.hm
                                          + " for index "
                                          + useHM.pokemonIndex
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                              }

                              if (useHM.hm.equals("STOP")) {
                                 player.currFieldMove = "";
                              } else if (useHM.hm.equals("SWITCH")) {
                                 Pokemon movePokemon = player.pokemon.get(useHM.pokemonIndex);
                                 Pokemon movePokemon2 = player.pokemon.get(useHM.movePos);
                                 player.pokemon.remove(useHM.pokemonIndex);
                                 player.pokemon.add(useHM.pokemonIndex, movePokemon2);
                                 player.pokemon.remove(useHM.movePos);
                                 player.pokemon.add(useHM.movePos, movePokemon);
                                 if (useHM.pokemonIndex == 0) {
                                    player.currPokemon = movePokemon2;
                                 }

                                 if (useHM.movePos == 0) {
                                    player.currPokemon = movePokemon;
                                 }
                              } else if (!useHM.hm.equals("BUILD")) {
                                 if (useHM.hm.equals("CUT")) {
                                    Vector2 pos = player.facingPos(useHM.dirFacing);
                                    Tile currTile = player.network.tiles.get(pos);
                                    if (currTile.isCuttable) {
                                       Action action = new CutTreeAnim(game, game.map.overworldTiles.get(pos), null);
                                       game.map.interiorTiles.get(game.map.interiorTilesIndex).remove(currTile.position.cpy());
                                       if (!currTile.items().isEmpty()) {
                                          for (String item : currTile.items().keySet()) {
                                             if (player.hasItem(item)) {
                                                int currQuantity = player.getItemAmount(item);
                                                player.setItemAmount(item, currQuantity + currTile.items().get(item));
                                             } else {
                                                player.setItemAmount(item, currTile.items().get(item));
                                             }
                                          }

                                          currTile.items().clear();
                                       }

                                       game.insertAction(action);
                                    }
                                 } else if (useHM.hm.equals("HEADBUTT")) {
                                    player.currFieldMove = "HEADBUTT";
                                 } else if (useHM.hm.equals("JUMP")) {
                                    player.currFieldMove = "RIDE";
                                 }
                              }

                              for (Player otherPlayer : game.players.values()) {
                                 if (player != otherPlayer && otherPlayer.network.loadingZone.contains(player.position)) {
                                    useHM.playerId = player.network.number;
                                    game.server.sendToTCP(otherPlayer.network.connectionId, useHM);
                                 }
                              }
                           } else if (object instanceof Network.UseItem) {
                              Network.UseItem useItem = (Network.UseItem)object;
                              if (!game.players.containsKey(useItem.playerId)) {
                                 System.out
                                    .println("UseItem: Invalid player id " + useItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(useItem.playerId);
                              if (!player.hasItem(useItem.item)) {
                                 System.out
                                    .println(
                                       "UseItem: None of this item in inventory: " + useItem.item + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Vector2 pos = player.facingPos(useItem.dirFacing);
                              if (useItem.item.contains("apricorn")) {
                                 game.insertAction(new PlantTree(pos, null));
                              }

                              player.setItemAmount(useItem.item, player.getItemAmount(useItem.item) - 1);
                              if (player.getItemAmount(useItem.item) <= 0) {
                                 player.removeItem(useItem.item);
                              }

                              for (Player otherPlayer : game.players.values()) {
                                 if (player != otherPlayer && otherPlayer.network.loadingZone.contains(player.position)) {
                                    useItem.playerId = player.network.number;
                                    game.server.sendToTCP(otherPlayer.network.connectionId, useItem);
                                 }
                              }
                           } else if (object instanceof Network.Sleep) {
                              Network.Sleep sleep = (Network.Sleep)object;
                              if (!game.players.containsKey(sleep.playerId)) {
                                 System.out.println("UseHM: Invalid player id " + sleep.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(sleep.playerId);
                              player.isSleeping = sleep.isSleeping;
                              player.canMove = !sleep.isSleeping;
                              if (!player.isSleeping) {
                                 player.spawnLoc = player.position.cpy();
                                 int i = 0;

                                 for (Pokemon pokemon : player.pokemon) {
                                    Network.PokemonData pokemonData = new Network.PokemonData(pokemon, i);
                                    connection.sendTCP(pokemonData);
                                    i++;
                                 }
                              }
                           } else if (object instanceof Network.Craft) {
                              Network.Craft craft = (Network.Craft)object;
                              if (!game.players.containsKey(craft.playerId)) {
                                 System.out.println("Craft: Invalid player id " + craft.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(craft.playerId);
                              if (player.hasCraftRequirements(Player.crafts, craft.craftIndex, craft.amount)) {
                                 player.craftItem(Player.crafts, craft.craftIndex, craft.amount);
                              }
                           } else if (object instanceof Network.DropItem) {
                              Network.DropItem dropItem = (Network.DropItem)object;
                              if (!game.players.containsKey(dropItem.playerId)) {
                                 System.out
                                    .println("DropItem: Invalid player id " + dropItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(dropItem.playerId);
                              player.setItemAmount(dropItem.itemName, player.getItemAmount(dropItem.itemName) - dropItem.amount);
                              if (player.getItemAmount(dropItem.itemName) <= 0) {
                                 player.removeItem(dropItem.itemName);
                              }
                           } else if (object instanceof Network.DropPokemon) {
                              Network.DropPokemon dropPokemon = (Network.DropPokemon)object;
                              if (!game.players.containsKey(dropPokemon.playerId)) {
                                 System.out
                                    .println(
                                       "DropPokemon: Invalid player id " + dropPokemon.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player player = game.players.get(dropPokemon.playerId);
                              if (dropPokemon.pickingUp) {
                                 Vector2 pos = dropPokemon.pos;
                                 Pokemon pokemon = game.map.pokemon.get(pos);
                                 if (player.pokemon.size() < 6 && pokemon != null && player.network.loadingZone.contains(pos)) {
                                    player.pokemon.add(pokemon);
                                    game.map.pokemon.remove(pos);
                                    game.actionStack.remove(pokemon.standingAction);
                                 }

                                 for (Player otherPlayer : game.players.values()) {
                                    if (otherPlayer != player && player.network.loadingZone.contains(pos)) {
                                       game.server.sendToTCP(otherPlayer.network.connectionId, new Network.OverworldPokemonData(pokemon, pos, true));
                                    }
                                 }
                              } else {
                                 Vector2 pos = player.facingPos(dropPokemon.dirFacing);
                                 Pokemon pokemon = player.pokemon.get(dropPokemon.index);
                                 pokemon.position = pos;
                                 player.pokemon.remove(dropPokemon.index);
                                 if (dropPokemon.index == 0) {
                                    for (Pokemon currPokemon : player.pokemon) {
                                       if (currPokemon.currentStats.get("hp") > 0) {
                                          player.currPokemon = currPokemon;
                                          break;
                                       }
                                    }
                                 }

                                 pokemon.mapTiles = player.network.tiles;
                                 pokemon.canMove = true;
                                 game.insertAction(pokemon.new Standing());

                                 for (Player otherPlayer : game.players.values()) {
                                    if (otherPlayer != player && player.network.loadingZone.contains(pos)) {
                                       game.server.sendToTCP(otherPlayer.network.connectionId, new Network.OverworldPokemonData(pokemon, pos));
                                    }
                                 }
                              }
                           } else if (object instanceof Network.PausePokemon) {
                              Network.PausePokemon pausePokemon = (Network.PausePokemon)object;
                              if (!game.players.containsKey(pausePokemon.playerId)) {
                                 System.out
                                    .println(
                                       "DropPokemon: Invalid player id " + pausePokemon.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player player = game.players.get(pausePokemon.playerId);
                              if (player.network.loadingZone.contains(pausePokemon.position) && game.map.pokemon.containsKey(pausePokemon.position)) {
                                 Pokemon pokemon = game.map.pokemon.get(pausePokemon.position);
                                 pokemon.canMove = !pausePokemon.shouldPause;
                              }

                              if (pausePokemon.shouldPause) {
                                 connection.sendTCP(pausePokemon);
                              }
                           } else if (object instanceof Network.PickupItem) {
                              Network.PickupItem pickupItem = (Network.PickupItem)object;
                              if (!game.players.containsKey(pickupItem.playerId)) {
                                 System.out
                                    .println(
                                       "PickupItem: Invalid player id " + pickupItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player player = game.players.get(pickupItem.playerId);
                              Vector2 pos = new Vector2(0.0F, 0.0F);
                              if (pickupItem.dirFacing.equals("right")) {
                                 pos = new Vector2(player.position.cpy().add(16.0F, 0.0F));
                              } else if (pickupItem.dirFacing.equals("left")) {
                                 pos = new Vector2(player.position.cpy().add(-16.0F, 0.0F));
                              } else if (pickupItem.dirFacing.equals("up")) {
                                 pos = new Vector2(player.position.cpy().add(0.0F, 16.0F));
                              } else if (pickupItem.dirFacing.equals("down")) {
                                 pos = new Vector2(player.position.cpy().add(0.0F, -16.0F));
                              }

                              Tile tile = game.map.tiles.get(pos);
                              if (tile != null && tile.hasItem != null) {
                                 tile.pickUpItem(player);
                                 pickupItem.pos = pos;
                                 connection.sendTCP(pickupItem);
                                 Network.TileData tileData = new Network.TileData(tile);

                                 for (Player otherPlayer : game.players.values()) {
                                    if (player != otherPlayer && player.network.loadingZone.contains(tileData.pos)) {
                                       game.server.sendToTCP(player.network.connectionId, tileData);
                                    }
                                 }
                              }
                           } else if (object instanceof Network.LearnMove) {
                              Network.LearnMove learnMove = (Network.LearnMove)object;
                              if (!game.players.containsKey(learnMove.playerId)) {
                                 System.out
                                    .println("DropItem: Invalid player id " + learnMove.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player player = game.players.get(learnMove.playerId);
                              Pokemon pokemon = player.pokemon.get(learnMove.pokemonIndex);
                              learnMove.moveName = learnMove.moveName.toLowerCase(Locale.ROOT);
                              boolean foundMove = false;

                              for (int i = 0; i <= pokemon.level; i++) {
                                 if (pokemon.learnSet.containsKey(i)) {
                                    for (String attack : pokemon.learnSet.get(i)) {
                                       if (attack.toLowerCase(Locale.ROOT).equals(learnMove.moveName)) {
                                          foundMove = true;
                                          break;
                                       }
                                    }

                                    if (foundMove) {
                                       break;
                                    }
                                 }
                              }

                              if (foundMove) {
                                 pokemon.attacks[learnMove.replaceIndex] = learnMove.moveName;
                              }
                           } else if (object instanceof Network.BattleData) {
                              Network.BattleData battleData = (Network.BattleData)object;
                              if (!battleData.pokemonData.name.equals("ghost")) {
                                 System.out
                                    .println(
                                       "BattleData: Invalid encounter for "
                                          + battleData.pokemonData.name
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              if (!game.players.containsKey(battleData.playerId)) {
                                 System.out
                                    .println(
                                       "BattleData: Invalid player id " + battleData.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player player = game.players.get(battleData.playerId);
                              player.setCurrPokemon();
                              player.canMove = false;
                              game.battles.put(player.network.id, new Battle());
                              game.battles.get(player.network.id).oppPokemon = new Pokemon(battleData.pokemonData);
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
