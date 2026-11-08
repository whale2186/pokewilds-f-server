package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Network {
   public static final int port = 54555;

   public static void register(EndPoint endPoint) {
      Kryo kryo = endPoint.getKryo();
      kryo.register(Vector2.class);
      kryo.register(Filter.class);
      kryo.register(ArrayList.class);
      kryo.register(HashMap.class);
      kryo.register(String[].class);
      kryo.register(Color.class);
      kryo.register(Network.MapTiles.class);
      kryo.register(Network.TileDataBase.class);
      kryo.register(Network.Login.class);
      kryo.register(Network.Logout.class);
      kryo.register(Network.PlayerDataBase.class);
      kryo.register(Network.PokemonDataBase.class);
      kryo.register(Network.MovePlayer.class);
      kryo.register(Pokemon.Generation.class);
      kryo.register(Network.ServerPlayerData.class);
      kryo.register(Network.BattleData.class);
      kryo.register(Network.BattleTurnData.class);
      kryo.register(Attack.class);
      kryo.register(Network.DoBattleAction.class);
      kryo.register(Battle.DoTurn.Type.class);
      kryo.register(Network.RelocatePlayer.class);
      kryo.register(Network.RouteData.class);
      kryo.register(Network.UseHM.class);
      kryo.register(Network.UseItem.class);
      kryo.register(Network.Sleep.class);
      kryo.register(Network.Craft.class);
      kryo.register(Network.DropItem.class);
      kryo.register(Network.DropPokemon.class);
      kryo.register(Network.PausePokemon.class);
      kryo.register(Network.SaveData.class);
      kryo.register(Network.PickupItem.class);
      kryo.register(Network.LearnMove.class);
      kryo.register(Network.MovePokemon.class);
      kryo.register(Network.OverworldPokemonData.class);
      kryo.register(Network.PlayerDataV06.class);
      kryo.register(Network.PokemonDataV04.class);
      kryo.register(Network.PokemonDataV05.class);
      kryo.register(Network.PlayerDataV07.class);
      kryo.register(Network.PokemonDataV07.class);
      kryo.register(Network.GameSaveData.class);
      kryo.register(Network.MapSaveData.class);
      kryo.register(Network.PlayerData.class);
      kryo.register(Network.PokemonData.class);
      kryo.register(Network.TileData.class);
      kryo.register(Network.PlayerSpawnLocationData.class);
      kryo.register(LinkedHashMap.class);
   }

   public static class ActionData {
      String className;
      String objectId;
      Object[] params;
      Network.ActionData nextAction;

      public ActionData(Action action) {
         this.className = action.getClass().toString();
         this.params = action.params;

         for (int i = 0; i < this.params.length; i++) {
            Object param = this.params[i];
            if (param instanceof Action) {
               this.params[i] = new Network.ActionData((Action)param);
            }
         }
      }
   }

   public static class BattleData {
      Network.PokemonData pokemonData;
      String playerId = null;

      public BattleData() {
      }

      public BattleData(Pokemon pokemon) {
         this.pokemonData = new Network.PokemonData(pokemon);
      }

      public BattleData(Pokemon pokemon, String playerId) {
         this(pokemon);
         this.playerId = playerId;
      }
   }

   public static class BattleTurnData {
      boolean oppFirst;
      Attack playerAttack;
      String playerTrappedBy = null;
      int playerTrapCounter = 0;
      Attack enemyAttack;
      String enemyTrappedBy = null;
      int enemyTrapCounter = 0;
      String itemName;
      int numWobbles;
      boolean runSuccessful;
   }

   public static class Craft {
      String playerId;
      int craftIndex;
      int amount;

      public Craft() {
      }

      public Craft(String playerId, int craftIndex, int amount) {
         this.playerId = playerId;
         this.craftIndex = craftIndex;
         this.amount = amount;
      }
   }

   public static class DoBattleAction {
      String playerId;
      String attack;
      String itemName;
      int pokemonIndex;
      Battle.DoTurn.Type type = Battle.DoTurn.Type.ATTACK;

      public DoBattleAction() {
      }

      public DoBattleAction(String playerId, Battle.DoTurn.Type type, int pokemonIndex) {
         this(playerId, type);
         this.pokemonIndex = pokemonIndex;
      }

      public DoBattleAction(String playerId, Battle.DoTurn.Type type, String action) {
         this(playerId, type);
         this.attack = action;
         this.itemName = action;
      }

      public DoBattleAction(String playerId, Battle.DoTurn.Type type) {
         this.playerId = playerId;
         this.type = type;
      }
   }

   public static class DropItem {
      String playerId;
      String itemName;
      int amount;
      Vector2 pos;

      public DropItem() {
      }

      public DropItem(String playerId, String itemName, int amount, Vector2 pos) {
         this.playerId = playerId;
         this.itemName = itemName;
         this.amount = amount;
         this.pos = pos;
      }
   }

   public static class DropPokemon {
      String playerId;
      int index;
      String dirFacing;
      boolean pickingUp;
      boolean pausing = false;
      Vector2 pos;

      public DropPokemon() {
      }

      public DropPokemon(String playerId, Vector2 pos) {
         this.playerId = playerId;
         this.pos = pos;
         this.pickingUp = true;
      }

      public DropPokemon(String playerId, String dirFacing, boolean pausing) {
         this.playerId = playerId;
         this.dirFacing = dirFacing;
         this.pausing = pausing;
      }

      public DropPokemon(String playerId, int index, String dirFacing) {
         this.playerId = playerId;
         this.index = index;
         this.dirFacing = dirFacing;
         this.pickingUp = false;
      }
   }

   public static class GameSaveData {
      ArrayList<Network.PlayerData> players = new ArrayList<>();
      Network.PlayerData playerData;
      String currMapId;
      String timeOfDay;
      int dayTimer;
      ArrayList<String> alreadyUsedDungeons = new ArrayList<>();

      public GameSaveData() {
      }

      public GameSaveData(Game game) {
         this.timeOfDay = game.map.timeOfDay;
         this.dayTimer = CycleDayNight.dayTimer;
         this.currMapId = game.map.currMapId;

         if (game.coop == null) {
            for (Player player : game.players.values()) this.players.add(new Network.PlayerData(player));
         }

         this.playerData = new Network.PlayerData(game.player);
         this.playerData.isInterior = game.map.tiles != game.map.overworldTiles;
         this.alreadyUsedDungeons = game.alreadyUsedDungeons;
      }
   }

   public static class LearnMove {
      String playerId;
      int pokemonIndex;
      int replaceIndex;
      String moveName;

      public LearnMove() {
      }

      public LearnMove(String playerId, int pokemonIndex, int replaceIndex, String moveName) {
         this.playerId = playerId;
         this.pokemonIndex = pokemonIndex;
         this.replaceIndex = replaceIndex;
         this.moveName = moveName;
      }
   }

   public static class Login {
      public String playerId;
      Color color;

      public Login() {
      }

      public Login(String playerId, Color color) {
         this.playerId = playerId;
         this.color = color;
      }
   }

   public static class Logout {
      public String playerId;

      public Logout() {
      }

      public Logout(String playerId) {
         this.playerId = playerId;
      }
   }

   public static class MapSaveData {
      Network.MapTiles mapTiles = new Network.MapTiles();
      HashMap<Vector2, Network.PokemonData> overworldPokemon = new HashMap<>();

      public MapSaveData() {
      }

      public MapSaveData(Game game) {
         this.mapTiles.timeOfDay = game.map.timeOfDay;
         this.mapTiles.dayTimer = CycleDayNight.dayTimer;

         for (Tile tile : game.map.overworldTiles.values()) {
            if (tile.routeBelongsTo != null && !this.mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
               this.mapTiles.routes.put(tile.routeBelongsTo.toString(), new Network.RouteData(tile.routeBelongsTo));
            }

            this.mapTiles.tiles.add(new Network.TileData(tile));
         }

         for (HashMap<Vector2, Tile> tiles : game.map.interiorTiles) {
            HashMap<Vector2, Network.TileData> tileDatas = null;
            if (tiles != null) {
               tileDatas = new HashMap<>();

               for (Tile tile : tiles.values()) {
                  if (tile.routeBelongsTo != null && !this.mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
                     this.mapTiles.routes.put(tile.routeBelongsTo.toString(), new Network.RouteData(tile.routeBelongsTo));
                  }

                  tileDatas.put(tile.position, new Network.TileData(tile));
               }
            }

            this.mapTiles.interiorTiles.add(tileDatas);
         }

         this.mapTiles.interiorTilesIndex = game.map.interiorTilesIndex;
         this.mapTiles.edges = game.map.edges;

         for (Vector2 pos : game.map.pokemon.keySet()) {
            Pokemon currPokemon = game.map.pokemon.get(pos);
            this.overworldPokemon.put(pos, new Network.PokemonData(currPokemon));
         }
      }
   }

   public static class MapTiles {
      public ArrayList<Network.TileData> tiles = new ArrayList<>();
      public HashMap<String, Network.RouteData> routes = new HashMap<>();
      public ArrayList<Vector2> edges = new ArrayList<>();
      public ArrayList<HashMap<Vector2, Network.TileData>> interiorTiles = new ArrayList<>();
      int interiorTilesIndex;
      String timeOfDay = null;
      int dayTimer;
   }

   public static class MovePlayer {
      String playerId;
      public String dirFacing;
      boolean isRunning;

      public MovePlayer() {
      }

      public MovePlayer(String playerId, String dirFacing, boolean isRunning) {
         this.playerId = playerId;
         this.dirFacing = dirFacing;
         this.isRunning = isRunning;
      }
   }

   public static class MovePokemon {
      Vector2 position;
      public String dirFacing;

      public MovePokemon() {
      }

      public MovePokemon(Pokemon pokemon) {
         this.position = pokemon.position;
         this.dirFacing = pokemon.dirFacing;
      }
   }

   public static class OverworldPokemonData extends Network.PokemonData {
      Vector2 overworldPos;
      boolean remove = false;

      public OverworldPokemonData() {
      }

      public OverworldPokemonData(Pokemon pokemon, Vector2 position, boolean remove) {
         this(pokemon, position);
         this.remove = remove;
      }

      public OverworldPokemonData(Pokemon pokemon, Vector2 position) {
         super(pokemon);
         this.overworldPos = position;
      }
   }

   public static class PausePokemon {
      String playerId;
      Vector2 position;
      boolean shouldPause;

      public PausePokemon() {
      }

      public PausePokemon(String playerId, Vector2 position, boolean shouldPause) {
         this.playerId = playerId;
         this.position = position;
         this.shouldPause = shouldPause;
      }
   }

   public static class PickupItem {
      String playerId;
      String dirFacing;
      Vector2 pos;

      public PickupItem() {
      }

      public PickupItem(String playerId, String dirFacing) {
         this.playerId = playerId;
         this.dirFacing = dirFacing;
      }
   }

   public static class PlayerData extends Network.PlayerDataV07 {
      String currFieldMove = null;
      int fieldMovePokemonIndex = -1;

      public PlayerData() {
      }

      public PlayerData(Player player) {
         super(player);
         if (player.hmPokemon != null) {
            this.currFieldMove = player.currFieldMove;
            this.fieldMovePokemonIndex = player.pokemon.indexOf(player.hmPokemon);
         }
      }
   }

   public static class PlayerDataBase {
      public Vector2 position;
      public String name;
      ArrayList<Network.PokemonData> pokemon;
      Network.PokemonData currPokemon;
      Map<String, Integer> itemsDict;
      String id;
      String number;
      Color color;
      String dirFacing;
      public Vector2 spawnLoc;
      boolean isInterior;
      boolean displayedMaxPartyText;
      boolean isFlying;
      int flyingIndex = 0;

      public PlayerDataBase() {
      }

      public PlayerDataBase(Player player) {
         this.position = player.position.cpy();
         this.position.x = this.position.x - this.position.x % 16.0F;
         this.position.y = this.position.y - this.position.y % 16.0F;
         this.name = player.name;
         this.pokemon = new ArrayList<>();

         for (Pokemon pokemon : player.pokemon) {
            this.pokemon.add(new Network.PokemonData(pokemon));
         }

         if (player.currPokemon != null) {
            this.currPokemon = new Network.PokemonData(player.currPokemon);
         }

         this.itemsDict = player.getItemsDict();
         this.id = player.network.id;
         this.number = player.network.number;
         this.color = player.color;
         this.dirFacing = player.dirFacing;
         this.spawnLoc = player.spawnLoc;
         this.displayedMaxPartyText = player.displayedMaxPartyText;
         this.isFlying = player.currFieldMove.equals("FLY");
         if (player.flyingAction != null) {
            this.flyingIndex = player.pokemon.indexOf(player.flyingAction.pokemon);
         }
      }
   }

   public static class PlayerDataV06 extends Network.PlayerDataBase {
      public int spawnIndex = -1;

      public PlayerDataV06() {
      }

      public PlayerDataV06(Player player) {
         super(player);
         this.spawnIndex = player.spawnIndex;
      }
   }

   public static class PlayerDataV07 extends Network.PlayerDataV06 {
      String character = "gold";
      Color skinColor = new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F);
      boolean enteredDesertBiome = false;

      public PlayerDataV07() {
      }

      public PlayerDataV07(Player player) {
         super(player);
         this.character = player.character;
         this.skinColor = new Color(player.skinColor);
         this.enteredDesertBiome = player.enteredDesertBiome;
      }
   }

   public static class PlayerSpawnLocationData {
      Vector2 position;
      int spawnIndex;

      public PlayerSpawnLocationData() {
      }

      public PlayerSpawnLocationData(Player player) {
         this.position = player.spawnLoc;
         this.spawnIndex = player.spawnIndex;
      }
   }

   public static class PokemonData extends Network.PokemonDataV07 {
      public int exp = 1;
      public String lanOwnerId;
      public String lanOwnerName;

      public PokemonData() {
      }

      public PokemonData(Pokemon pokemon) {
         super(pokemon);
         this.exp = pokemon.exp;
         if (Game.staticGame != null && Game.staticGame.coop != null)
            Game.staticGame.coop.identifyOwner(pokemon);
         this.lanOwnerId = pokemon.lanOwnerId; this.lanOwnerName = pokemon.lanOwnerName;
      }

      public PokemonData(Pokemon pokemon, int index) {
         super(pokemon, index);
         this.exp = pokemon.exp;
      }
   }

   public static class PokemonDataBase {
      String name;
      int level;
      Pokemon.Generation generation;
      int hp;
      boolean isShiny;
      String[] attacks = new String[4];
      int index;
      String status = null;
      String previousOwnerName = null;
      Vector2 position;
      boolean isInterior = false;
      int harvestTimer = 0;

      public PokemonDataBase() {
      }

      public PokemonDataBase(Pokemon pokemon) {
         this.name = pokemon.specie.name;
         this.level = pokemon.level;
         this.generation = pokemon.generation;
         this.isShiny = pokemon.isShiny;
         this.hp = pokemon.currentStats.get("hp");
         this.attacks[0] = pokemon.attacks[0];
         this.attacks[1] = pokemon.attacks[1];
         this.attacks[2] = pokemon.attacks[2];
         this.attacks[3] = pokemon.attacks[3];
         this.position = pokemon.position;
         if (pokemon.mapTiles != null) {
            this.isInterior = pokemon.mapTiles != Game.staticGame.map.overworldTiles;
         }

         this.status = pokemon.status;
         this.harvestTimer = pokemon.harvestTimer;
         if (pokemon.previousOwner != null) {
            this.previousOwnerName = pokemon.previousOwner.name;
         }
      }

      public PokemonDataBase(Pokemon pokemon, int index) {
         this(pokemon);
         this.index = index;
      }
   }

   public static class PokemonDataV04 extends Network.PokemonDataBase {
      public boolean test = false;

      public PokemonDataV04() {
      }

      public PokemonDataV04(Pokemon pokemon) {
         super(pokemon);
      }

      public PokemonDataV04(Pokemon pokemon, int index) {
         super(pokemon, index);
      }
   }

   public static class PokemonDataV05 extends Network.PokemonDataV04 {
      public String gender = null;
      public String eggHatchInto = null;
      public int friendliness = 0;
      public boolean aggroPlayer = false;

      public PokemonDataV05() {
      }

      public PokemonDataV05(Pokemon pokemon) {
         super(pokemon);
         this.gender = pokemon.gender;
         if (pokemon.isEgg) {
            this.eggHatchInto = pokemon.specie.name;
            this.name = "egg";
         }

         this.friendliness = pokemon.happiness;
         this.aggroPlayer = pokemon.aggroPlayer;
      }

      public PokemonDataV05(Pokemon pokemon, int index) {
         super(pokemon, index);
         this.gender = pokemon.gender;
         if (pokemon.isEgg) {
            this.eggHatchInto = pokemon.specie.name;
            this.name = "egg";
         }

         this.friendliness = pokemon.happiness;
         this.aggroPlayer = pokemon.aggroPlayer;
      }
   }

   public static class PokemonDataV07 extends Network.PokemonDataV05 {
      public String nickname = null;
      public int interiorIndex = 100;

      public PokemonDataV07() {
      }

      public PokemonDataV07(Pokemon pokemon) {
         super(pokemon);
         this.nickname = pokemon.nickname;
         this.interiorIndex = pokemon.interiorIndex;
      }

      public PokemonDataV07(Pokemon pokemon, int index) {
         super(pokemon, index);
         this.nickname = pokemon.nickname;
      }
   }

   public static class RelocatePlayer {
      Vector2 position;

      public RelocatePlayer() {
      }

      public RelocatePlayer(Vector2 position) {
         this.position = position.cpy();
      }
   }

   public static class RouteData {
      String classId;
      String name;
      int level;
      ArrayList<Network.PokemonData> pokemon = new ArrayList<>();
      ArrayList<String> allowedPokemon;
      ArrayList<String> musics;
      int musicsIndex = 0;

      public RouteData() {
      }

      public RouteData(Route route) {
         this.classId = route.toString();
         this.name = route.name;
         this.level = route.level;
         this.musicsIndex = route.musicsIndex;

         for (Pokemon pokemon : route.storedPokemon) {
            this.pokemon.add(new Network.PokemonData(pokemon));
         }
      }
   }

   public static class SaveData {
      Network.MapTiles mapTiles = new Network.MapTiles();
      ArrayList<Network.PlayerDataBase> players = new ArrayList<>();
      Network.PlayerDataBase playerData;
      HashMap<Vector2, Network.PokemonDataBase> overworldPokemon = new HashMap<>();

      public SaveData() {
      }

      public SaveData(Game game) {
         for (Tile tile : game.map.overworldTiles.values()) {
            if (tile.routeBelongsTo != null && !this.mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
               this.mapTiles.routes.put(tile.routeBelongsTo.toString(), new Network.RouteData(tile.routeBelongsTo));
            }

            this.mapTiles.tiles.add(new Network.TileData(tile));
         }

         for (HashMap<Vector2, Tile> tiles : game.map.interiorTiles) {
            HashMap<Vector2, Network.TileData> tileDatas = null;
            if (tiles != null) {
               tileDatas = new HashMap<>();

               for (Tile tile : tiles.values()) {
                  if (tile.routeBelongsTo != null && !this.mapTiles.routes.containsKey(tile.routeBelongsTo.toString())) {
                     this.mapTiles.routes.put(tile.routeBelongsTo.toString(), new Network.RouteData(tile.routeBelongsTo));
                  }

                  tileDatas.put(tile.position, new Network.TileData(tile));
               }
            }

            this.mapTiles.interiorTiles.add(tileDatas);
         }

         this.mapTiles.interiorTilesIndex = game.map.interiorTilesIndex;
         this.mapTiles.edges = game.map.edges;
         this.mapTiles.timeOfDay = game.map.timeOfDay;
         this.mapTiles.dayTimer = CycleDayNight.dayTimer;

         if (game.coop == null) {
            for (Player player : game.players.values()) this.players.add(new Network.PlayerData(player));
         }

         this.playerData = new Network.PlayerData(game.player);
         this.playerData.isInterior = game.map.tiles != game.map.overworldTiles;

         for (Vector2 pos : game.map.pokemon.keySet()) {
            Pokemon currPokemon = game.map.pokemon.get(pos);
            this.overworldPokemon.put(pos, new Network.PokemonData(currPokemon));
         }
      }
   }

   public static class ServerPlayerData {
      public Vector2 position;
      public String name;
      String number;
      Color color;
      boolean isInterior;

      public ServerPlayerData() {
      }

      public ServerPlayerData(Player player) {
         this.position = player.position;
         this.name = player.name;
         this.number = player.network.number;
         this.color = player.color;
         this.isInterior = player.network.tiles != Game.staticGame.map.overworldTiles;
      }
   }

   public static class Sleep {
      String playerId;
      boolean isSleeping;

      public Sleep() {
      }

      public Sleep(String playerId, boolean isSleeping) {
         this.playerId = playerId;
         this.isSleeping = isSleeping;
      }
   }

   public static class SyncedHashMap {
      String name;
      String operation;
      Object putgetMe;

      public SyncedHashMap() {
      }

      public SyncedHashMap(String name, String operation, Object putgetMe) {
         this.name = name;
         this.operation = operation;
         this.putgetMe = putgetMe;
      }
   }

   public static class TileData extends Network.TileDataBase {
      public String squishedTiles = null;

      public TileData() {
      }

      public TileData(Tile tile) {
         super(tile);
         this.squishedTiles = tile.squishedTiles;
      }

      public TileData(Tile tile, int interiorIndex) {
         super(tile, interiorIndex);
         this.squishedTiles = tile.squishedTiles;
      }
   }

   public static class TileDataBase {
      public Vector2 pos;
      public String tileName;
      public String tileNameUpper;
      String routeBelongsTo;
      HashMap<String, Integer> items;
      public String hasItem;
      public int hasItemAmount;
      String biome;
      boolean isUnown;
      String message = "";
      int interiorIndex;
      ArrayList<Vector2> doorTiles;

      public TileDataBase() {
      }

      public TileDataBase(Tile tile, int interiorIndex) {
         this(tile);
         this.interiorIndex = interiorIndex;
      }

      public TileDataBase(Tile tile) {
         this.pos = tile.position.cpy();
         this.tileName = tile.name;
         this.tileNameUpper = tile.nameUpper;
         if (tile.routeBelongsTo != null) {
            this.routeBelongsTo = tile.routeBelongsTo.toString();
         }

         if (TrainerTipsTile.class.isInstance(tile)) {
            TrainerTipsTile tTile = (TrainerTipsTile)tile;
            this.isUnown = tTile.isUnown;
            this.message = tTile.message;
         }

         this.items = tile.items;
         this.hasItem = tile.hasItem;
         this.hasItemAmount = tile.hasItemAmount;
         this.doorTiles = tile.doorTiles;
         this.biome = tile.biome;
      }
   }

   public static class UseHM {
      String playerId;
      int pokemonIndex;
      String hm;
      String dirFacing;
      int movePos;

      public UseHM() {
      }

      public UseHM(String playerId, int pokemonIndex, String hm, int movePos) {
         this(playerId, pokemonIndex, hm);
         this.movePos = movePos;
      }

      public UseHM(String playerId, int pokemonIndex, String hm, String dirFacing) {
         this(playerId, pokemonIndex, hm);
         this.dirFacing = dirFacing;
      }

      public UseHM(String playerId, int pokemonIndex, String hm) {
         this.playerId = playerId;
         this.pokemonIndex = pokemonIndex;
         this.hm = hm;
      }
   }

   public static class UseItem {
      String playerId;
      String item;
      String dirFacing;

      public UseItem() {
      }

      public UseItem(String playerId, String item, String dirFacing) {
         this.playerId = playerId;
         this.item = item;
         this.dirFacing = dirFacing;
      }
   }
}
