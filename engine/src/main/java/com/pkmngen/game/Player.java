package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.LoadingZone;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Player {
   public String dirFacing = "down";
   Map<String, Sprite> standingSprites = new HashMap<>();
   Map<String, Sprite> movingSprites = new HashMap<>();
   Map<String, Sprite> altMovingSprites = new HashMap<>();
   public Pokemon hmPokemon = null;
   public int spriteOffsetY = 0;
   public Vector2 position;
   public Sprite currSprite = new Sprite();
   SpriteProxy battleSprite;
   Sprite fishingSprites;
   Sprite sittingSprites;
   Sprite sleepingSprite;
   Sprite sleepingBagSprite;
   String name;
   Color color = new Color(0.9137255F, 0.5294118F, 0.1764706F, 1.0F);
   ArrayList<Pokemon> pokemon;
   public Pokemon currPokemon;
   private Map<String, Integer> itemsDict;
   int adrenaline;
   String currState;
   public String currFieldMove = "";
   Tile currDigTile = new Tile("hole1", new Vector2(0.0F, 0.0F));
   public boolean isCrafting = false;
   public boolean isSitting = false;
   public boolean isSleeping = false;
   public Vector2 sleepingDir = null;
   public Player.Flying flyingAction = null;
   public boolean drawSleepingBag = false;
   public boolean acceptInput = true;
   boolean isNearCampfire = false;
   Tile currBuildTile;
   int buildTileIndex = 0;
   ArrayList<Tile> terrainTiles = new ArrayList<>();
   ArrayList<Tile> buildTiles = new ArrayList<>();
   ArrayList<Tile> outdoorBuildTiles = new ArrayList<>();
   ArrayList<Tile> indoorBuildTiles = new ArrayList<>();
   ArrayList<Tile> desertBuildTiles = new ArrayList<>();
   ArrayList<Tile> smeargleBuildTiles = new ArrayList<>();
   ArrayList<Tile> smeargleInteriorTiles = new ArrayList<>();
   ArrayList<Tile> bridgeBuildTiles = new ArrayList<>();
   ArrayList<Tile> tundraBuildTiles = new ArrayList<>();
   ArrayList<Tile> woodedLakeBuildTiles = new ArrayList<>();
   HashMap<String, HashMap<String, Integer>> buildTileRequirements = new HashMap<>();
   public boolean canMove = true;
   int numFlees = 0;
   Sprite zSprite;
   int zsTimer = 0;
   public int repelCounter = 0;
   Vector2 spawnLoc = new Vector2(0.0F, 0.0F);
   int spawnIndex = -1;
   PlayerStanding standingAction;
   public boolean displayedMaxPartyText = false;
   public boolean enteredDesertBiome = false;
   public int eggStepTimer = 0;
   public int friendshipStepTimer = 0;
   public boolean nearAggroPokemon = false;
   public boolean nearCacturne = false;
   String character = "gold";
   Color skinColor = new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F);
   public String currPlanting = null;
   public String currRod = "";
   public boolean isFishing = false;
   public static Sprite sproutSprite;
   public static Sprite sproutSprite2;
   public static boolean drawSproutSprite = true;
   public static Sprite desertGrassSprite;
   public static Sprite snowDriftSprite;
   public static Sprite sootDriftSprite;
   public static Sprite currGrassOverSprite;
   ArrayList<String> alreadyDoneHarvestables = new ArrayList<>();
   public static HashMap<String, String> oppDirs = new HashMap<>();
   public boolean dontDrawMapDuringBattle = false;
   public DrawPlayerLower drawPlayerLower = new DrawPlayerLower();
   public DrawPlayerUpper drawPlayerUpper = new DrawPlayerUpper();
   public Player.Type type;
   Player.Network network;
   public static ArrayList<Player.Craft> crafts = new ArrayList<>();
   public ArrayList<Player.Craft> fossilCrafts = new ArrayList<>();
   public ArrayList<Player.Craft> regiCrafts;
   public static ArrayList<Player.Craft> kilnCrafts = new ArrayList<>();

   Map<String, Integer> getItemsDict() {
      return this.itemsDict;
   }

   boolean hasItem(String name) {
      return this.itemsDict.containsKey(name);
   }

   Integer getItemAmount(String name) {
      return this.itemsDict.get(name);
   }

   void setItemAmount(String name, int amount) {
      this.itemsDict.put(name, amount);
   }

   void removeItem(String name) {
      this.itemsDict.remove(name);
   }

   void setPlayerItems(Map<String, Integer> itemsDict) {
      this.itemsDict = itemsDict;
   }

   public Player() {
      Player.Craft craft = new Player.Craft("AERODACTYL", 1);
      craft.requirements.add(new Player.Craft("old amber", 1));
      this.fossilCrafts.add(craft);
      craft = new Player.Craft("OMANYTE", 1);
      craft.requirements.add(new Player.Craft("helix fossil", 1));
      this.fossilCrafts.add(craft);
      craft = new Player.Craft("KABUTO", 1);
      craft.requirements.add(new Player.Craft("dome fossil", 1));
      this.fossilCrafts.add(craft);
      craft = new Player.Craft("ANORITH", 1);
      craft.requirements.add(new Player.Craft("claw fossil", 1));
      this.fossilCrafts.add(craft);
      craft = new Player.Craft("LILEEP", 1);
      craft.requirements.add(new Player.Craft("root fossil", 1));
      this.fossilCrafts.add(craft);
      craft = new Player.Craft("SHIELDON", 1);
      craft.requirements.add(new Player.Craft("shield fossil", 1));
      this.fossilCrafts.add(craft);
      craft = new Player.Craft("CRANIDOS", 1);
      craft.requirements.add(new Player.Craft("skull fossil", 1));
      this.fossilCrafts.add(craft);
      this.regiCrafts = new ArrayList<>();
      craft = new Player.Craft("REGISTEEL", 1);
      craft.requirements.add(new Player.Craft("metal coat", 47));
      craft.requirements.add(new Player.Craft("spell tag", 1));
      this.regiCrafts.add(craft);
      craft = new Player.Craft("REGIROCK", 1);
      craft.requirements.add(new Player.Craft("hard stone", 47));
      craft.requirements.add(new Player.Craft("spell tag", 1));
      this.regiCrafts.add(craft);
      craft = new Player.Craft("REGICE", 1);
      craft.requirements.add(new Player.Craft("nevermeltice", 47));
      craft.requirements.add(new Player.Craft("spell tag", 1));
      this.regiCrafts.add(craft);
      craft = new Player.Craft("REGIDRAGO", 1);
      craft.requirements.add(new Player.Craft("dragon scale", 43));
      craft.requirements.add(new Player.Craft("dragon fang", 4));
      craft.requirements.add(new Player.Craft("spell tag", 1));
      this.regiCrafts.add(craft);
      craft = new Player.Craft("REGIELEKI", 1);
      craft.requirements.add(new Player.Craft("magnet", 43));
      craft.requirements.add(new Player.Craft("binding band", 4));
      craft.requirements.add(new Player.Craft("spell tag", 1));
      this.regiCrafts.add(craft);
      Texture text = TextureCache.get(Gdx.files.internal("tiles/zs1.png"));
      this.zSprite = new Sprite(text, 0, 0, 16, 16);
      Texture playerText = TextureCache.get(Gdx.files.internal("player/gold-walking.png"));
      this.standingSprites.put("down", new Sprite(playerText, 0, 0, 16, 16));
      this.standingSprites.put("up", new Sprite(playerText, 16, 0, 16, 16));
      this.standingSprites.put("left", new Sprite(playerText, 32, 0, 16, 16));
      this.standingSprites.put("right", new Sprite(playerText, 48, 0, 16, 16));
      this.movingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
      this.movingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
      this.movingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
      this.movingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));
      this.altMovingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
      this.altMovingSprites.get("down").flip(true, false);
      this.altMovingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
      this.altMovingSprites.get("up").flip(true, false);
      this.altMovingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
      this.altMovingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));
      playerText = TextureCache.get(Gdx.files.internal("player/gold-running.png"));
      this.standingSprites.put("down_running", new Sprite(playerText, 0, 0, 16, 16));
      this.standingSprites.put("up_running", new Sprite(playerText, 16, 0, 16, 16));
      this.standingSprites.put("left_running", new Sprite(playerText, 32, 0, 16, 16));
      this.standingSprites.put("right_running", new Sprite(playerText, 48, 0, 16, 16));
      this.movingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
      this.movingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
      this.movingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
      this.movingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));
      this.altMovingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
      this.altMovingSprites.get("down_running").flip(true, false);
      this.altMovingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
      this.altMovingSprites.get("up_running").flip(true, false);
      this.altMovingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
      this.altMovingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));
      Texture var19 = TextureCache.get(Gdx.files.internal("battle/player_back_color1.png"));
      this.battleSprite = new SpriteProxy(var19, 0, 0, 45, 46);
      Texture var20 = TextureCache.get(Gdx.files.internal("player/gold-sleepingbag.png"));
      this.sleepingSprite = new Sprite(var20, 0, 0, 24, 16);
      Texture var21 = TextureCache.get(Gdx.files.internal("tiles/sleeping_bag1.png"));
      this.sleepingBagSprite = new Sprite(var21, 0, 0, 24, 16);
      Texture var22 = TextureCache.get(Gdx.files.internal("player/gold-fishing.png"));
      this.fishingSprites = new Sprite(var22, 0, 0, 56, 24);
      Texture var23 = TextureCache.get(Gdx.files.internal("player/gold-sitting.png"));
      this.sittingSprites = new Sprite(var23, 0, 0, 48, 16);
      this.position = new Vector2(0.0F, 0.0F);
      this.currSprite = new Sprite(this.standingSprites.get(this.dirFacing));
      this.name = "AAAA";
      this.pokemon = new ArrayList<>();
      this.adrenaline = 0;
      this.currState = "";
      this.outdoorBuildTiles.add(new Tile("torch1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("house5_door1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("house5_middle1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("house5_roof_middle1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("exteriorwindows1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("chimney1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("campfire1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("fence1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("bridge1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("potted3", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("potted4", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("statue1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("house_gym1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("house_plant1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("house_plant2", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("gravestone3", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("sign_built1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("warp_tile1", new Vector2(0.0F, 0.0F)));
      this.outdoorBuildTiles.add(new Tile("chest1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("torch1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("house9_door1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("house9_NEW", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("house9_roof_middle1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("exteriorwindows1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("chimney1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("campfire1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("fence1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("bridge1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("potted3", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("potted4", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("statue1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("house_gym1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("house_plant1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("house_plant2", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("gravestone3", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("sign_built1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("warp_tile1", new Vector2(0.0F, 0.0F)));
      this.woodedLakeBuildTiles.add(new Tile("chest1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("torch1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("house6_door1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("house6_NEW", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("house6_roof_middle1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("exteriorwindows1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("chimney1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("campfire1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("fence2", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("bridge1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("potted1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("potted2", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("gravestone3", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("sign_desert1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("warp_tile1", new Vector2(0.0F, 0.0F)));
      this.desertBuildTiles.add(new Tile("chest1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("torch1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("house7_wall_door1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("house7_wall_S", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("house7_roof_EW", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("exteriorwindows1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("chimney1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("fence1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("bridge1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("potted3", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("potted4", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("sign_built1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("warp_tile1", new Vector2(0.0F, 0.0F)));
      this.bridgeBuildTiles.add(new Tile("chest1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("torch1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("house8_door1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("house8_SEW", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("house8_roof_middle1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("exteriorwindows1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("chimney1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("campfire1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("fence2", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("potted1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("potted2", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("gravestone3", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("sign_desert1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("warp_tile1", new Vector2(0.0F, 0.0F)));
      this.tundraBuildTiles.add(new Tile("chest1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("torch1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_plant1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_plant2", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_gym1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_shelf1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_wardrobe1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_vanity1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_vanity2", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_stool1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_bed1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_table1_default", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet2", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet3", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet4", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet5", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet6", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet7", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_carpet8", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("chest1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("stairs_up1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("ball_kiln1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_clock1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("interiorwall1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_couch1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_couch2", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_couch3", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_couch4", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_couch5", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_trashcan1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_plushpichu", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_plushduskull", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_plushskitty", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_plushjigglypuff", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_window1", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("house_window2", new Vector2(0.0F, 0.0F)));
      this.indoorBuildTiles.add(new Tile("sign_built1", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture1", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture2", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture3", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture4", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture5", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture6", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture7", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture8", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture9", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture10", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture11", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture12", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture13", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture14", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture15", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture16", new Vector2(0.0F, 0.0F)));
      this.smeargleBuildTiles.add(new Tile("house_picture17", new Vector2(0.0F, 0.0F)));
      this.smeargleInteriorTiles.addAll(this.smeargleBuildTiles);
      this.smeargleInteriorTiles.add(new Tile("house_picture18", new Vector2(0.0F, 0.0F)));
      this.smeargleInteriorTiles.add(new Tile("house_picture19", new Vector2(0.0F, 0.0F)));
      this.smeargleInteriorTiles.add(new Tile("house_picture20", new Vector2(0.0F, 0.0F)));
      this.buildTiles = this.outdoorBuildTiles;
      this.currBuildTile = this.buildTiles.get(this.buildTileIndex);
      this.terrainTiles.add(new Tile("sand1", new Vector2(0.0F, 0.0F), true, new Route("beach2", 22)));
      this.terrainTiles.add(new Tile("desert4", new Vector2(0.0F, 0.0F), true, new Route("desert1", 22)));
      this.terrainTiles.add(new Tile("mountain1", new Vector2(0.0F, 0.0F), true, new Route("mountain1", 22)));
      this.terrainTiles.add(new Tile("snow2", new Vector2(0.0F, 0.0F), true, new Route("snow1", 22)));
      this.terrainTiles.add(new Tile("green1", new Vector2(0.0F, 0.0F), true, new Route("forest1", 22)));
      this.terrainTiles.add(new Tile("flower4", new Vector2(0.0F, 0.0F), true, new Route("forest1", 22)));
      this.terrainTiles.add(new Tile("volcano1", new Vector2(0.0F, 0.0F), true, new Route("volcano1", 22)));
      this.terrainTiles.add(new Tile("green12", new Vector2(0.0F, 0.0F), true, new Route("graveyard1", 22)));
      this.terrainTiles.add(new Tile("green_savanna1", new Vector2(0.0F, 0.0F), true, new Route("savanna2", 22)));
      this.terrainTiles.add(new Tile("ledges3_none", new Vector2(0.0F, 0.0F)));
      this.terrainTiles.add(new Tile("green_woodedlake", new Vector2(0.0F, 0.0F), true, new Route("wooded_lake1", 22)));
      this.terrainTiles.add(new Tile("green_deepforest", new Vector2(0.0F, 0.0F), true, new Route("deep_forest", 22)));
      ArrayList<Tile> allTiles = new ArrayList<>(this.outdoorBuildTiles);
      allTiles.addAll(this.indoorBuildTiles);
      allTiles.addAll(this.desertBuildTiles);
      allTiles.addAll(this.terrainTiles);
      allTiles.addAll(this.smeargleInteriorTiles);
      allTiles.addAll(this.bridgeBuildTiles);
      allTiles.addAll(this.tundraBuildTiles);
      allTiles.addAll(this.woodedLakeBuildTiles);

      for (Tile tile : allTiles) {
         if (!tile.nameUpper.equals("sleeping_bag1")) {
            HashMap<String, Integer> requirements = new HashMap<>();
            this.buildTileRequirements.put(tile.name, requirements);
            if (tile.name.contains("campfire")) {
               requirements.put("log", 4);
               requirements.put("grass", 2);
            } else if (tile.name.contains("bridge")) {
               requirements.put("log", 1);
               requirements.put("hard stone", 1);
            } else if (tile.name.equals("house_bed1")) {
               requirements.put("log", 4);
               requirements.put("soft bedding", 1);
            } else if (tile.name.contains("house_couch")) {
               requirements.put("log", 1);
               requirements.put("soft wool", 1);
            } else if (tile.name.equals("house_gym1")) {
               requirements.put("hard stone", 2);
            } else if (tile.name.contains("house_plant")) {
               requirements.put("log", 1);
               requirements.put("miracle seed", 1);
            } else if (tile.name.contains("carpet")) {
               requirements.put("grass", 1);
            } else if (tile.name.contains("picture")) {
               requirements.put("thin paper", 1);
            } else if (tile.name.contains("house_window")) {
               requirements.put("clear glass", 1);
            } else if (tile.name.contains("clock")) {
               requirements.put("log", 1);
               requirements.put("clear glass", 1);
            } else if (tile.name.contains("house6_")) {
               requirements.put("hard stone", 1);
            } else if (tile.name.contains("house8_")) {
               requirements.put("grass", 1);
               requirements.put("log", 1);
            } else if (tile.name.contains("house9_")) {
               requirements.put("grass", 1);
               requirements.put("log", 1);
            } else if (tile.name.contains("potted")) {
               requirements.put("hard stone", 1);
               requirements.put("miracle seed", 1);
            } else if (tile.name.contains("vanity")) {
               requirements.put("log", 1);
               requirements.put("clear glass", 1);
            } else if (tile.name.contains("plush")) {
               if (tile.name.contains("pichu")) {
                  requirements.put("magnet", 1);
               } else if (tile.name.contains("duskull")) {
                  requirements.put("spell tag", 1);
               } else {
                  requirements.put("grass", 1);
               }

               requirements.put("soft wool", 1);
            } else if (tile.name.contains("house_")) {
               requirements.put("log", 1);
            } else if (tile.name.equals("sand1")) {
               requirements.put("soft sand", 1);
            } else if (tile.name.equals("desert4")) {
               requirements.put("dry sand", 1);
            } else if (tile.name.equals("mountain1")) {
               requirements.put("light clay", 1);
            } else if (tile.name.equals("snow2")) {
               requirements.put("nevermeltice", 1);
            } else if (tile.name.equals("green1")) {
               requirements.put("grass", 1);
            } else if (tile.name.equals("flower4")) {
               requirements.put("flowers", 1);
            } else if (tile.name.equals("volcano1")) {
               requirements.put("ashen soil", 1);
            } else if (tile.name.equals("green12")) {
               requirements.put("cursed soil", 1);
            } else if (tile.name.equals("green_savanna1")) {
               requirements.put("dry soil", 1);
            } else if (tile.name.equals("green_deepforest")) {
               requirements.put("mystic soil", 1);
            } else if (tile.name.equals("green_woodedlake")) {
               requirements.put("damp soil", 1);
            } else if (tile.name.equals("ledges3_none")) {
               requirements.put("hard stone", 1);
            } else if (tile.name.equals("statue1")) {
               requirements.put("hard stone", 2);
            } else if (tile.name.equals("interiorwall")) {
               requirements.put("log", 1);
            } else if (tile.name.contains("window")) {
               requirements.put("log", 1);
               requirements.put("clear glass", 1);
            } else if (tile.name.equals("gravestone3")) {
               requirements.put("hard stone", 1);
            } else if (tile.name.equals("warp_tile1")) {
               requirements.put("dusk stone", 2);
               requirements.put("psi energy", 2);
               requirements.put("life force", 2);
            } else if (tile.name.equals("chest1")) {
               requirements.put("log", 2);
            } else if (tile.name.equals("ball_kiln1")) {
               requirements.put("hard stone", 2);
               requirements.put("charcoal", 2);
            } else if (tile.name.contains("stairs_up")) {
               requirements.put("log", 2);
            } else if (tile.name.contains("chimney")) {
               requirements.put("log", 1);
            } else {
               requirements.put("grass", 1);
               requirements.put("log", 1);
            }
         }
      }

      this.buildTileRequirements.put("sleeping_bag1", new HashMap<>());
      this.buildTileRequirements.get("sleeping_bag1").put("sleeping bag", 1);
      this.setPlayerItems(new LinkedHashMap<>());
      this.setItemAmount("sleeping bag", 1);
      this.setItemAmount("escape rope", 1);
      if (Game.staticGame.debugInputEnabled) {
         this.setItemAmount("secret key", 1);
         this.setItemAmount("master ball", 99);
         this.setItemAmount("grass", 99);
         this.setItemAmount("log", 99);
         this.setItemAmount("black apricorn", 99);
         this.setItemAmount("green apricorn", 3);
         this.setItemAmount("pink apricorn", 2);
         this.setItemAmount("manure", 99);
         this.setItemAmount("berry juice", 99);
         this.setItemAmount("moomoo milk", 99);
         this.setItemAmount("ancientpowder", 99);
         this.setItemAmount("silph scope", 1);
         this.setItemAmount("soft sand", 18);
         this.setItemAmount("metal coat", 99);
         this.setItemAmount("hard stone", 99);
         this.setItemAmount("nevermeltice", 99);
         this.setItemAmount("dragon scale", 99);
         this.setItemAmount("dragon fang", 99);
         this.setItemAmount("magnet", 99);
         this.setItemAmount("binding band", 2);
         this.setItemAmount("spell tag", 88);
         this.setItemAmount("moon ball", 99);
         this.setItemAmount("love ball", 99);
         this.setItemAmount("heavy ball", 99);
         this.setItemAmount("level ball", 99);
         this.setItemAmount("soft bedding", 99);
         this.setItemAmount("miracle seed", 99);
         this.setItemAmount("berry seed", 99);
         this.setItemAmount("great ball", 99);
         this.setItemAmount("ultra ball", 99);
         this.setItemAmount("dark energy", 99);
         this.setItemAmount("thin paper", 99);
         this.setItemAmount("clear glass", 99);
         this.setItemAmount("hard stone", 99);
         this.setItemAmount("old amber", 99);
         this.setItemAmount("helix fossil", 99);
         this.setItemAmount("dome fossil", 99);
         this.setItemAmount("root fossil", 99);
         this.setItemAmount("claw fossil", 99);
         this.setItemAmount("shield fossil", 99);
         this.setItemAmount("skull fossil", 99);
         this.setItemAmount("flowers", 99);
         this.setItemAmount("light clay", 99);
         this.setItemAmount("ragecandybar", 99);
         this.setItemAmount("old rod", 1);
         this.setItemAmount("good rod", 1);
         this.setItemAmount("super rod", 1);
         this.setItemAmount("silky thread", 99);
         this.setItemAmount("thunderstone", 99);
         this.setItemAmount("fire stone", 99);
         this.setItemAmount("leaf stone", 99);
         this.setItemAmount("moon stone", 99);
         this.setItemAmount("sun stone", 99);
         this.setItemAmount("water stone", 99);
         this.setItemAmount("dawn stone", 99);
         this.setItemAmount("dusk stone", 99);
         this.setItemAmount("shiny stone", 99);
         this.setItemAmount("ice stone", 99);
         this.setItemAmount("soft wool", 99);
         this.setItemAmount("life force", 109);
         this.setItemAmount("poké doll", 99);
      }

      this.network = new Player.Network(this.position);
      this.type = Player.Type.LOCAL;
   }

   public Player(com.pkmngen.game.Network.PlayerDataBase playerData) {
      this();
      if (playerData instanceof com.pkmngen.game.Network.PlayerDataV06) {
         this.spawnIndex = ((com.pkmngen.game.Network.PlayerDataV06)playerData).spawnIndex;
      }

      this.spawnLoc = playerData.spawnLoc;
      this.dirFacing = playerData.dirFacing;
      this.position = playerData.position;
      this.name = playerData.name == null || playerData.name.trim().isEmpty() ? "player" : playerData.name.trim();
      this.pokemon = new ArrayList<>();

      for (com.pkmngen.game.Network.PokemonDataBase pokemonData : playerData.pokemon) {
         if (pokemonData instanceof com.pkmngen.game.Network.PokemonDataV07) {
            ((com.pkmngen.game.Network.PokemonDataV07)pokemonData).interiorIndex = 100;
         }

         this.pokemon.add(new Pokemon(pokemonData));
      }

      if (this.pokemon.size() > 0) {
         this.currPokemon = this.pokemon.get(0);
      }

      this.setPlayerItems(playerData.itemsDict);
      this.network = new Player.Network(this.position.cpy());
      this.network.id = playerData.id;
      this.network.number = playerData.number;
      this.displayedMaxPartyText = playerData.displayedMaxPartyText;
      if (playerData.isInterior) {
         this.network.tiles = Game.staticGame.map.interiorTiles.get(Game.staticGame.map.interiorTilesIndex);
         this.buildTiles = this.indoorBuildTiles;
      } else {
         this.network.tiles = Game.staticGame.map.overworldTiles;
      }

      if (playerData instanceof com.pkmngen.game.Network.PlayerDataV07) {
         this.character = ((com.pkmngen.game.Network.PlayerDataV07)playerData).character;
         this.enteredDesertBiome = ((com.pkmngen.game.Network.PlayerDataV07)playerData).enteredDesertBiome;
      }

      if (Game.staticGame.dangerousDebugInputEnabled) {
         this.pokemon.clear();
         Pokemon pokemon = new Pokemon("aexeggutor", 99, Pokemon.Generation.CRYSTAL);
         pokemon.gender = "female";
         pokemon.attacks[1] = "leech life";
         pokemon.attacks[2] = "recover";
         pokemon.happiness = 0;
         this.pokemon.add(pokemon);
         this.pokemon.add(new Pokemon("rhydon", 99, Pokemon.Generation.CRYSTAL));
         this.pokemon.add(new Pokemon("smeargle", 100, Pokemon.Generation.CRYSTAL));
         this.pokemon.add(new Pokemon("sharpedo", 60, Pokemon.Generation.CRYSTAL));
         this.pokemon.add(new Pokemon("gallade", 3, Pokemon.Generation.CRYSTAL));
         this.pokemon.add(new Pokemon("pidgeot", 30, Pokemon.Generation.CRYSTAL));
         this.character = "brendan";
         this.name = "brendan";
         playerData.color = new Color(0.972549F, 0.21960784F, 0.03137255F, 1.0F);
         this.setItemAmount("rare candy", 99);
         this.setItemAmount("master ball", 99);
         this.setItemAmount("spell tag", 99);
         this.setItemAmount("nevermeltice", 99);
         this.setItemAmount("metal coat", 99);
         this.setItemAmount("magnet", 99);
         this.setItemAmount("berry seed", 99);
         this.setItemAmount("cursed soil", 99);
         this.setItemAmount("ashen soil", 99);
         this.setItemAmount("dry soil", 99);
         this.setItemAmount("dry sand", 99);
         this.setItemAmount("log", 99);
         this.setItemAmount("grass", 99);
         this.setItemAmount("lum berry", 99);
         this.setItemAmount("pecha berry", 99);
         this.setItemAmount("cheri berry", 99);
         this.setItemAmount("aspear berry", 99);
         this.setItemAmount("chesto berry", 99);
         this.setItemAmount("rawst berry", 99);
         this.setItemAmount("persim berry", 99);
         this.setItemAmount("love ball", 99);
         this.setItemAmount("fast ball", 99);
         this.setItemAmount("lure ball", 99);
         this.setItemAmount("moon ball", 99);
         this.setItemAmount("friend ball", 99);
         this.setItemAmount("heavy ball", 99);
         this.setItemAmount("stardust", 99);
         this.setItemAmount("psi energy", 99);
         this.setItemAmount("charcoal", 99);
         this.setItemAmount("hard shell", 99);
         this.setItemAmount("sweet apple", 99);
         this.setItemAmount("secret key", 99);
         this.setItemAmount("pink apricorn", 99);
         this.setItemAmount("life force", 99);
         this.setItemAmount("big pearl", 99);
         this.setItemAmount("dusk ball", 99);
         this.setItemAmount("damp soil", 99);
         this.setItemAmount("mystic soil", 99);
         this.setItemAmount("repel", 99);
      }

      if (playerData instanceof com.pkmngen.game.Network.PlayerData && ((com.pkmngen.game.Network.PlayerData)playerData).fieldMovePokemonIndex > -1) {
         this.currFieldMove = ((com.pkmngen.game.Network.PlayerData)playerData).currFieldMove;
         this.hmPokemon = this.pokemon.get(((com.pkmngen.game.Network.PlayerData)playerData).fieldMovePokemonIndex);
      }

      if (playerData instanceof com.pkmngen.game.Network.PlayerDataV07 && ((com.pkmngen.game.Network.PlayerDataV07)playerData).skinColor != null)
         this.skinColor = new Color(((com.pkmngen.game.Network.PlayerDataV07)playerData).skinColor);
      this.setColor(playerData.color);
   }

   public boolean hasCraftRequirements(ArrayList<Player.Craft> crafts, int craftIndex, int amount) {
      return this.hasCraftRequirements(crafts.get(craftIndex), amount);
   }

   public boolean hasCraftRequirements(Player.Craft craft, int amount) {
      boolean hasRequirements = true;

      for (Player.Craft req : craft.requirements) {
         hasRequirements = false;
         Iterator var6 = this.getItemsDict().keySet().iterator();

         while (true) {
            if (var6.hasNext()) {
               String item = (String)var6.next();
               if (!item.equals(req.name) || this.getItemAmount(item) < req.amount * amount) {
                  continue;
               }

               hasRequirements = true;
            }

            if (!hasRequirements) {
               return hasRequirements;
            }
            break;
         }
      }

      return hasRequirements;
   }

   public boolean checkNearCampfire() {
      this.nearAggroPokemon = false;
      this.nearCacturne = false;
      boolean foundCampfire = this.hmPokemon != null && this.hmPokemon.hms.contains("FLASH");
      Vector2 startPos = this.position.cpy().add(-64.0F, -64.0F);
      startPos.x = (int)startPos.x - (int)startPos.x % 16;
      startPos.y = (int)startPos.y - (int)startPos.y % 16;
      Vector2 endPos = this.position.cpy().add(64.0F, 64.0F);
      endPos.x = (int)endPos.x - (int)endPos.x % 16;
      endPos.y = (int)endPos.y - (int)endPos.y % 16;
      Vector2 currPos = new Vector2(startPos.x, startPos.y);

      while (currPos.y < endPos.y) {
         Tile tile = Game.staticGame.map.tiles.get(currPos);
         currPos.x += 16.0F;
         if (currPos.x > endPos.x) {
            currPos.x = startPos.x;
            currPos.y += 16.0F;
         }

         if (tile != null) {
            if (tile.nameUpper.contains("campfire")) {
               foundCampfire = true;
            } else if (tile.items != null && tile.items.containsKey("torch")) {
               foundCampfire = true;
            } else if (tile.nameUpper.equals("cactus2_cacturne")) {
               this.nearCacturne = true;
            } else if (Game.staticGame.map.pokemon.containsKey(tile.position)) {
               Pokemon pokemon = Game.staticGame.map.pokemon.get(tile.position);
               if (pokemon.aggroPlayer) {
                  this.nearAggroPokemon = true;
               }

               if (pokemon.hms.contains("FLASH")) {
                  foundCampfire = true;
               }
            }
         }
      }

      if (Game.staticGame.map.tiles == Game.staticGame.map.overworldTiles) {
         this.updateMiniMap(Game.staticGame);
      }

      if (FogEffect.type != null && !FogEffect.effectArea.contains(this.position)) {
         FogEffect.refresh = true;
      }

      return foundCampfire;
   }

   public void updateBuildTiles(Game game) {
      Vector2 facingPos = this.facingPos();
      Tile nextTile = game.map.tiles.get(facingPos);
      if (this.currFieldMove.equals("BUILD")) {
         if (game.map.tiles == game.map.overworldTiles && !Game.staticGame.debugInputEnabled) {
            if (this.hmPokemon != null && this.hmPokemon.specie.name.equals("smeargle")) {
               this.buildTiles = this.smeargleBuildTiles;
            } else if (nextTile.name.contains("desert")) {
               this.buildTiles = this.desertBuildTiles;
            } else if (nextTile.name.contains("bridge")) {
               this.buildTiles = this.bridgeBuildTiles;
            } else if (nextTile.biome.equals("tundra")) {
               this.buildTiles = this.tundraBuildTiles;
            } else if (nextTile.biome.equals("wooded_lake")) {
               this.buildTiles = this.woodedLakeBuildTiles;
            } else {
               this.buildTiles = this.outdoorBuildTiles;
            }
         } else if (game.map.tiles == game.map.overworldTiles && Game.staticGame.debugInputEnabled) {
            if (this.hmPokemon != null && this.hmPokemon.specie.name.equals("smeargle")) {
               this.buildTiles = this.smeargleBuildTiles;
            } else if (TileEditor.buildingTileUsed == 0) {
               this.buildTiles = this.outdoorBuildTiles;
            } else if (TileEditor.buildingTileUsed == 1) {
               this.buildTiles = this.woodedLakeBuildTiles;
            } else if (TileEditor.buildingTileUsed == 2) {
               this.buildTiles = this.tundraBuildTiles;
            } else if (TileEditor.buildingTileUsed == 3) {
               this.buildTiles = this.desertBuildTiles;
            } else {
               this.buildTiles = this.bridgeBuildTiles;
            }
         } else if (this.hmPokemon != null && this.hmPokemon.specie.name.equals("smeargle")) {
            this.buildTiles = this.smeargleInteriorTiles;
         } else {
            this.buildTiles = this.indoorBuildTiles;
         }
      } else if (this.currFieldMove.equals("DIG")) {
         this.buildTiles = this.terrainTiles;
         if (nextTile == null) {
            game.player.currBuildTile = game.player.currDigTile;
            return;
         }

         if (!nextTile.nameUpper.contains("hole") && !nextTile.name.contains("water5") && !nextTile.name.contains("water2") && !nextTile.name.contains("lava")) {
            game.player.currBuildTile = game.player.currDigTile;
            return;
         }
      }

      while (this.buildTileIndex > 0 && this.buildTileIndex >= this.buildTiles.size()) {
         this.buildTileIndex--;
      }

      this.currBuildTile = this.buildTiles.get(this.buildTileIndex);
   }

   public void updateMiniMap(Game game) {
      if (this.dirFacing.equals("right")) {
         Vector2 startPos = this.position.cpy().add(112.0F, -128.0F);

         for (int i = 0; i < 16; i++) {
            startPos.y += 16.0F;
            Tile currTile = game.map.tiles.get(startPos);
            if (currTile != null) {
               game.map.minimapQueue.add(currTile);
            }
         }
      } else if (this.dirFacing.equals("left")) {
         Vector2 startPos = this.position.cpy().add(-112.0F, -128.0F);

         for (int i = 0; i < 16; i++) {
            startPos.y += 16.0F;
            Tile currTile = game.map.tiles.get(startPos);
            if (currTile != null) {
               game.map.minimapQueue.add(currTile);
            }
         }
      } else if (this.dirFacing.equals("up")) {
         Vector2 startPos = this.position.cpy().add(-128.0F, 112.0F);

         for (int i = 0; i < 16; i++) {
            startPos.x += 16.0F;
            Tile currTile = game.map.tiles.get(startPos);
            if (currTile != null) {
               game.map.minimapQueue.add(currTile);
            }
         }
      } else if (this.dirFacing.equals("down")) {
         Vector2 startPos = this.position.cpy().add(-128.0F, -112.0F);

         for (int i = 0; i < 16; i++) {
            startPos.x += 16.0F;
            Tile currTile = game.map.tiles.get(startPos);
            if (currTile != null) {
               game.map.minimapQueue.add(currTile);
            }
         }
      }
   }

   public void checkIfRouteChanged(Game game) {
      Tile targetTile = game.map.tiles.get(this.position);
      if (targetTile != null && targetTile.routeBelongsTo != null) {
         Route newRoute = targetTile.routeBelongsTo;
         if (!game.map.currRoute.type().equals(newRoute.type()) && !game.map.timeOfDay.equals("night")) {
            game.musicController.fadeToDungeon = true;
            if (newRoute.type().equals("desert") && !game.player.enteredDesertBiome) {
               game.player.enteredDesertBiome = true;
               game.player.canMove = false;
               game.insertAction(
                  game.player.new Emote(
                     "!",
                     new DisplayText(
                        game, "The sun is scorching hot! This region feels dangerous...", null, null, new RunCode(() -> game.player.canMove = true, null)
                     )
                  )
               );
            }

            game.insertAction(game.map.new ShadeEffect(newRoute.type()));
         }

         if (!game.map.currRoute.name.equals(newRoute.name)) {
            newRoute.music = game.map.currRoute.music;
            game.map.currRoute = newRoute;
         }

         game.map.currBiome = targetTile.biome;
      }
   }

   public void craftItem(ArrayList<Player.Craft> crafts, int craftIndex, int amount) {
      this.craftItem(crafts.get(craftIndex), amount);
   }

   public void craftItem(Player.Craft craft, int amount) {
      for (Player.Craft req : craft.requirements) {
         int newAmt = this.getItemAmount(req.name) - req.amount * amount;
         this.setItemAmount(req.name, newAmt);
         if (newAmt <= 0) {
            this.getItemsDict().remove(req.name);
         }
      }

      int newAmt = amount;
      if (this.getItemsDict().containsKey(craft.name)) {
         newAmt += this.getItemAmount(craft.name);
      }

      this.setItemAmount(craft.name, newAmt);
   }

   public Vector2 facingPos() {
      return this.facingPos(this.dirFacing);
   }

   public Vector2 facingPos(String dirFacing) {
      Vector2 pos = null;
      if (dirFacing.equals("right")) {
         pos = this.position.cpy().add(16.0F, 0.0F);
      } else if (dirFacing.equals("left")) {
         pos = this.position.cpy().add(-16.0F, 0.0F);
      } else if (dirFacing.equals("up")) {
         pos = this.position.cpy().add(0.0F, 16.0F);
      } else if (dirFacing.equals("down")) {
         pos = this.position.cpy().add(0.0F, -16.0F);
      }

      return pos;
   }

   public void swapSpritesSurfing(Pokemon pokemon) {
      Map<String, Sprite> tempSprites = this.standingSprites;
      this.standingSprites = pokemon.standingSprites;
      if (pokemon.surfSprites != null) {
         this.standingSprites = pokemon.surfSprites;
      }

      pokemon.standingSprites = tempSprites;
      tempSprites = this.movingSprites;
      this.movingSprites = pokemon.movingSprites;
      if (pokemon.surfMovingSprites != null) {
         this.movingSprites = pokemon.surfMovingSprites;
      }

      pokemon.movingSprites = tempSprites;
      tempSprites = this.altMovingSprites;
      this.altMovingSprites = pokemon.altMovingSprites;
      if (pokemon.surfMovingSprites != null) {
         this.altMovingSprites = pokemon.surfMovingSprites;
      }

      pokemon.altMovingSprites = tempSprites;
      int temp = this.spriteOffsetY;
      this.spriteOffsetY = pokemon.spriteOffsetY;
      pokemon.spriteOffsetY = temp;
      this.hmPokemon = pokemon;
   }

   public void swapSprites(Pokemon pokemon) {
      Map<String, Sprite> tempSprites = this.standingSprites;
      this.standingSprites = pokemon.standingSprites;
      pokemon.standingSprites = tempSprites;
      tempSprites = this.movingSprites;
      this.movingSprites = pokemon.movingSprites;
      pokemon.movingSprites = tempSprites;
      tempSprites = this.altMovingSprites;
      this.altMovingSprites = pokemon.altMovingSprites;
      pokemon.altMovingSprites = tempSprites;
      int temp = this.spriteOffsetY;
      this.spriteOffsetY = pokemon.spriteOffsetY;
      pokemon.spriteOffsetY = temp;
      this.hmPokemon = pokemon;
   }

   public Texture preventHairFlip(Texture text, int regionX, int offset) {
      TextureData temp = text.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      Pixmap currPixmap = temp.consumePixmap();
      Pixmap newPixmap = new Pixmap(16, 16, Format.RGBA8888);

      for (int i = 0; i < 16; i++) {
         for (int j = 0; j < 16; j++) {
            if (j < offset) {
               newPixmap.drawPixel(i, j, currPixmap.getPixel(i + regionX, j));
            } else {
               newPixmap.drawPixel(i, j, currPixmap.getPixel(15 - i + regionX, j));
            }
         }
      }

      return TextureCache.get(newPixmap);
   }

   public void setColor(Color newColor) {
      this.setColor(newColor, false);
   }

   public void setColor(Color newColor, boolean skinColor) {
      if (skinColor) {
         this.skinColor = newColor;
      } else {
         this.color = newColor;
      }

      boolean doingMod = true;
      FileHandle file = Gdx.files.local("mods/player/" + this.character + "/walking.png");
      if (!file.exists()) {
         doingMod = false;
         file = Gdx.files.internal("player/" + this.character + "-walking.png");
         if (!file.exists()) {
            this.character = "kris";
            file = Gdx.files.internal("player/" + this.character + "-walking.png");
         }
      }

      Color replaceColor;
      if (doingMod) {
         replaceColor = new Color(0.972549F, 0.21960784F, 0.03137255F, 1.0F);
      } else if (this.character.equals("gold")
         || this.character.equals("mark")
         || this.character.equals("red")
         || this.character.equals("chase")
         || this.character.equals("gloria")
         || this.character.equals("hilbert")
         || this.character.equals("lyra")
         || this.character.equals("serena")
         || this.character.equals("brendan")
         || this.character.equals("summer")) {
         replaceColor = new Color(0.972549F, 0.21960784F, 0.03137255F, 1.0F);
      } else if (this.character.equals("lunick")) {
         replaceColor = new Color(0.34901962F, 0.28235295F, 1.0F, 1.0F);
      } else if (this.character.equals("kellyn")) {
         replaceColor = new Color(0.9647059F, 0.03137255F, 0.003921569F, 1.0F);
      } else {
         replaceColor = new Color(0.3137255F, 0.28235295F, 0.972549F, 1.0F);
      }

      Texture playerText = TextureCache.get(file);
      if (!playerText.getTextureData().isPrepared()) {
         playerText.getTextureData().prepare();
      }

      Pixmap pixmap = playerText.getTextureData().consumePixmap();
      Color clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      Pixmap coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Format.RGBA8888);
      coloredPixmap.setColor(clearColor);
      coloredPixmap.fill();
      Color replaceWith = this.color;

      for (int i = 0; i < playerText.getWidth(); i++) {
         for (int j = 0; j < playerText.getHeight(); j++) {
            Color color = new Color(pixmap.getPixel(i, j));
            if (color.equals(new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F))) {
               color = this.skinColor;
            } else if (color.equals(replaceColor)) {
               color = replaceWith;
            }
            coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
         }
      }

      playerText = new Texture(coloredPixmap);
      this.standingSprites.put("down", new Sprite(playerText, 0, 0, 16, 16));
      this.standingSprites.put("up", new Sprite(playerText, 16, 0, 16, 16));
      this.standingSprites.put("left", new Sprite(playerText, 32, 0, 16, 16));
      this.standingSprites.put("right", new Sprite(playerText, 48, 0, 16, 16));
      this.movingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
      this.movingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
      this.movingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
      this.movingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));
      if (this.character.equals("hilda")) {
         this.altMovingSprites.put("down", new Sprite(playerText, 64, 0, 16, 16));
         this.altMovingSprites.get("down").flip(true, false);
         this.altMovingSprites.put("up", new Sprite(playerText, 80, 0, 16, 16));
         this.altMovingSprites.get("up").flip(true, false);
      } else {
         int offset = 9;
         if (this.character.equals("elaine")) {
            offset = 11;
         } else if (this.character.equals("gloria")) {
            offset = 10;
         }

         this.altMovingSprites.put("down", new Sprite(this.preventHairFlip(playerText, 64, offset), 0, 0, 16, 16));
         this.altMovingSprites.put("up", new Sprite(this.preventHairFlip(playerText, 80, offset), 0, 0, 16, 16));
      }

      this.altMovingSprites.put("left", new Sprite(playerText, 96, 0, 16, 16));
      this.altMovingSprites.put("right", new Sprite(playerText, 112, 0, 16, 16));
      file = Gdx.files.local("mods/player/" + this.character + "/running.png");
      if (!file.exists()) {
         file = Gdx.files.internal("player/" + this.character + "-running.png");
      }

      playerText = TextureCache.get(file);
      if (!playerText.getTextureData().isPrepared()) {
         playerText.getTextureData().prepare();
      }

      pixmap = playerText.getTextureData().consumePixmap();
      clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Format.RGBA8888);
      coloredPixmap.setColor(clearColor);
      coloredPixmap.fill();

      for (int i = 0; i < playerText.getWidth(); i++) {
         for (int j = 0; j < playerText.getHeight(); j++) {
            Color color = new Color(pixmap.getPixel(i, j));
            if (color.equals(new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F))) {
               color = this.skinColor;
            } else if (color.equals(replaceColor)) {
               color = replaceWith;
            }
            coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
         }
      }

      playerText = new Texture(coloredPixmap);
      this.standingSprites.put("down_running", new Sprite(playerText, 0, 0, 16, 16));
      this.standingSprites.put("up_running", new Sprite(playerText, 16, 0, 16, 16));
      this.standingSprites.put("left_running", new Sprite(playerText, 32, 0, 16, 16));
      this.standingSprites.put("right_running", new Sprite(playerText, 48, 0, 16, 16));
      this.movingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
      this.movingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
      this.movingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
      this.movingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));
      if (this.character.equals("hilda")) {
         this.altMovingSprites.put("down_running", new Sprite(playerText, 64, 0, 16, 16));
         this.altMovingSprites.get("down_running").flip(true, false);
         this.altMovingSprites.put("up_running", new Sprite(playerText, 80, 0, 16, 16));
         this.altMovingSprites.get("up_running").flip(true, false);
      } else {
         int offset = 10;
         if (this.character.equals("elaine")) {
            offset = 12;
         } else if (this.character.equals("gloria")) {
            offset = 11;
         }

         this.altMovingSprites.put("down_running", new Sprite(this.preventHairFlip(playerText, 64, offset), 0, 0, 16, 16));
         this.altMovingSprites.put("up_running", new Sprite(this.preventHairFlip(playerText, 80, offset), 0, 0, 16, 16));
      }

      this.altMovingSprites.put("left_running", new Sprite(playerText, 96, 0, 16, 16));
      this.altMovingSprites.put("right_running", new Sprite(playerText, 112, 0, 16, 16));
      file = Gdx.files.local("mods/player/" + this.character + "/sleepingbag.png");
      if (!file.exists()) {
         file = Gdx.files.internal("player/" + this.character + "-sleepingbag.png");
      }

      playerText = TextureCache.get(file);
      if (!playerText.getTextureData().isPrepared()) {
         playerText.getTextureData().prepare();
      }

      pixmap = playerText.getTextureData().consumePixmap();
      clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Format.RGBA8888);
      coloredPixmap.setColor(clearColor);
      coloredPixmap.fill();

      for (int i = 0; i < playerText.getWidth(); i++) {
         for (int j = 0; j < playerText.getHeight(); j++) {
            Color color = new Color(pixmap.getPixel(i, j));
            if (color.equals(new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F))) {
               color = this.skinColor;
            } else if (color.equals(replaceColor)) {
               color = replaceWith;
            }
            coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
         }
      }

      playerText = new Texture(coloredPixmap);
      this.sleepingSprite = new Sprite(playerText, 0, 0, 24, 16);
      file = Gdx.files.local("mods/player/" + this.character + "/fishing.png");
      if (!file.exists()) {
         file = Gdx.files.internal("player/" + this.character + "-fishing.png");
      }

      playerText = TextureCache.get(file);
      if (!playerText.getTextureData().isPrepared()) {
         playerText.getTextureData().prepare();
      }

      pixmap = playerText.getTextureData().consumePixmap();
      clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Format.RGBA8888);
      coloredPixmap.setColor(clearColor);
      coloredPixmap.fill();

      for (int i = 0; i < playerText.getWidth(); i++) {
         for (int j = 0; j < playerText.getHeight(); j++) {
            Color color = new Color(pixmap.getPixel(i, j));
            if (color.equals(new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F))) {
               color = this.skinColor;
            } else if (color.equals(replaceColor)) {
               color = replaceWith;
            }
            coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
         }
      }

      playerText = new Texture(coloredPixmap);
      this.fishingSprites = new Sprite(playerText, 0, 0, 56, 24);
      file = Gdx.files.local("mods/player/" + this.character + "/sitting.png");
      if (!file.exists()) {
         file = Gdx.files.internal("player/" + this.character + "-sitting.png");
      }

      playerText = TextureCache.get(file);
      if (!playerText.getTextureData().isPrepared()) {
         playerText.getTextureData().prepare();
      }

      pixmap = playerText.getTextureData().consumePixmap();
      clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Format.RGBA8888);
      coloredPixmap.setColor(clearColor);
      coloredPixmap.fill();

      for (int i = 0; i < playerText.getWidth(); i++) {
         for (int j = 0; j < playerText.getHeight(); j++) {
            Color color = new Color(pixmap.getPixel(i, j));
            if (color.equals(new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F))) {
               color = this.skinColor;
            } else if (color.equals(replaceColor)) {
               color = replaceWith;
            }
            coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
         }
      }

      playerText = new Texture(coloredPixmap);
      this.sittingSprites = new Sprite(playerText, 0, 0, 48, 16);
      doingMod = true;
      file = Gdx.files.local("mods/player/" + this.character + "/back.png");
      if (!file.exists()) {
         doingMod = false;
         file = Gdx.files.internal("player/" + this.character + "-back.png");
      }

      playerText = TextureCache.get(file);
      if (doingMod) {
         replaceColor = new Color(0.6901961F, 0.28235295F, 0.15686275F, 1.0F);
      } else if (this.character.equals("kris") || this.character.equals("elaine") || this.character.equals("leaf")) {
         replaceColor = new Color(0.21960784F, 0.15686275F, 0.972549F, 1.0F);
      } else if (this.character.equals("calem")) {
         replaceColor = new Color(0.21176471F, 0.3254902F, 0.77254903F, 1.0F);
      } else if (this.character.equals("gloria")) {
         replaceColor = new Color(0.21568628F, 0.5254902F, 0.43137255F, 1.0F);
      } else if (this.character.equals("hilbert")) {
         replaceColor = new Color(0.34509805F, 0.3764706F, 0.72156864F, 1.0F);
      } else if (this.character.equals("hilda")) {
         replaceColor = new Color(0.60784316F, 0.22745098F, 0.1882353F, 1.0F);
      } else if (this.character.equals("lunick")) {
         replaceColor = new Color(0.7764706F, 0.22352941F, 0.2901961F, 1.0F);
      } else if (this.character.equals("kellyn")) {
         replaceColor = new Color(0.9098039F, 0.30980393F, 0.30980393F, 1.0F);
      } else if (this.character.equals("mark")) {
         replaceColor = new Color(0.9098039F, 0.30980393F, 0.30980393F, 1.0F);
      } else if (this.character.equals("mint")) {
         replaceColor = new Color(0.16078432F, 0.41960785F, 0.8235294F, 1.0F);
      } else if (this.character.equals("rosa")) {
         replaceColor = new Color(0.3137255F, 0.28235295F, 0.2509804F, 1.0F);
      } else if (this.character.equals("serena")) {
         replaceColor = new Color(0.87058824F, 0.16470589F, 0.16470589F, 1.0F);
      } else if (this.character.equals("summer")) {
         replaceColor = new Color(0.9098039F, 0.30980393F, 0.30980393F, 1.0F);
      } else {
         replaceColor = new Color(0.6901961F, 0.28235295F, 0.15686275F, 1.0F);
      }

      if (!playerText.getTextureData().isPrepared()) {
         playerText.getTextureData().prepare();
      }

      pixmap = playerText.getTextureData().consumePixmap();
      clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
      coloredPixmap = new Pixmap(playerText.getWidth(), playerText.getHeight(), Format.RGBA8888);
      coloredPixmap.setColor(clearColor);
      coloredPixmap.fill();

      for (int i = 0; i < playerText.getWidth(); i++) {
         for (int j = 0; j < playerText.getHeight(); j++) {
            Color color = new Color(pixmap.getPixel(i, j));
            if (color.equals(new Color(1.0F, 0.80784315F, 0.28235295F, 1.0F))) {
               color = this.skinColor;
            } else if (color.equals(replaceColor)) {
               color = replaceWith;
            }
            coloredPixmap.drawPixel(i, j, Color.rgba8888(color));
         }
      }

      playerText = new Texture(coloredPixmap);
      this.battleSprite = new SpriteProxy(playerText, 0, 0, 48, 48);
   }

   public void setCurrPokemon() {
      for (Pokemon pokemon : this.pokemon) {
         pokemon.participatedInBattle = false;
      }

      if (this.hmPokemon != null && this.hmPokemon.currentStats.get("hp") > 0 && !this.hmPokemon.isEgg) {
         this.currPokemon = this.hmPokemon;
      } else {
         for (Pokemon currPokemon : this.pokemon) {
            if (currPokemon.currentStats.get("hp") > 0 && !currPokemon.isEgg) {
               this.currPokemon = currPokemon;
               return;
            }
         }
      }
   }

   static {
      Texture text = TextureCache.get(Gdx.files.internal("sprout_sheet2.png"));
      sproutSprite = new Sprite(text, 64, 0, 16, 16);
      text = TextureCache.get(Gdx.files.internal("tiles/place_something1.png"));
      sproutSprite2 = new Sprite(text, 0, 0, 16, 16);
      text = TextureCache.get(Gdx.files.internal("grass_over_sheet3.png"));
      desertGrassSprite = new Sprite(text, 0, 0, 16, 16);
      text = TextureCache.get(Gdx.files.internal("grass_over_sheet4.png"));
      snowDriftSprite = new Sprite(text, 0, 0, 16, 16);
      text = TextureCache.get(Gdx.files.internal("grass_over_sheet5.png"));
      sootDriftSprite = new Sprite(text, 0, 0, 16, 16);
      currGrassOverSprite = desertGrassSprite;
      oppDirs.put("up", "down");
      oppDirs.put("down", "up");
      oppDirs.put("right", "left");
      oppDirs.put("left", "right");
      Player.Craft craft = new Player.Craft("heavy ball", 1);
      craft.requirements.add(new Player.Craft("black apricorn", 1));
      crafts.add(craft);
      Player.Craft var6 = new Player.Craft("lure ball", 1);
      var6.requirements.add(new Player.Craft("blue apricorn", 1));
      crafts.add(var6);
      Player.Craft var7 = new Player.Craft("friend ball", 1);
      var7.requirements.add(new Player.Craft("green apricorn", 1));
      crafts.add(var7);
      Player.Craft var8 = new Player.Craft("love ball", 1);
      var8.requirements.add(new Player.Craft("pink apricorn", 1));
      crafts.add(var8);
      Player.Craft var9 = new Player.Craft("level ball", 1);
      var9.requirements.add(new Player.Craft("red apricorn", 1));
      crafts.add(var9);
      Player.Craft var10 = new Player.Craft("fast ball", 1);
      var10.requirements.add(new Player.Craft("white apricorn", 1));
      crafts.add(var10);
      Player.Craft var11 = new Player.Craft("moon ball", 1);
      var11.requirements.add(new Player.Craft("yellow apricorn", 1));
      crafts.add(var11);
      Player.Craft var12 = new Player.Craft("soft bedding", 1);
      var12.requirements.add(new Player.Craft("soft feather", 3));
      var12.requirements.add(new Player.Craft("silky thread", 3));
      crafts.add(var12);
      Player.Craft var13 = new Player.Craft("repel", 1);
      var13.requirements.add(new Player.Craft("charcoal", 1));
      var13.requirements.add(new Player.Craft("manure", 1));
      crafts.add(var13);
      Player.Craft var14 = new Player.Craft("max repel", 1);
      var14.requirements.add(new Player.Craft("poison barb", 1));
      var14.requirements.add(new Player.Craft("repel", 1));
      crafts.add(var14);
      Player.Craft var15 = new Player.Craft("rare candy", 1);
      var15.requirements.add(new Player.Craft("berry juice", 3));
      var15.requirements.add(new Player.Craft("ancientpowder", 1));
      crafts.add(var15);
      Player.Craft var16 = new Player.Craft("clear glass", 1);
      var16.requirements.add(new Player.Craft("soft sand", 3));
      crafts.add(var16);
      Player.Craft var17 = new Player.Craft("silph scope", 1);
      var17.requirements.add(new Player.Craft("clear glass", 2));
      var17.requirements.add(new Player.Craft("metal coat", 2));
      crafts.add(var17);
      Player.Craft var18 = new Player.Craft("binding band", 1);
      var18.requirements.add(new Player.Craft("grass", 3));
      crafts.add(var18);
      Player.Craft var19 = new Player.Craft("thin paper", 1);
      var19.requirements.add(new Player.Craft("log", 1));
      crafts.add(var19);
      Player.Craft var20 = new Player.Craft("ragecandybar", 1);
      var20.requirements.add(new Player.Craft("honey", 2));
      var20.requirements.add(new Player.Craft("charcoal", 1));
      crafts.add(var20);
      Player.Craft var21 = new Player.Craft("old rod", 1);
      var21.requirements.add(new Player.Craft("log", 1));
      var21.requirements.add(new Player.Craft("silky thread", 1));
      crafts.add(var21);
      Player.Craft var22 = new Player.Craft("good rod", 1);
      var22.requirements.add(new Player.Craft("old rod", 1));
      var22.requirements.add(new Player.Craft("metal coat", 2));
      crafts.add(var22);
      Player.Craft var23 = new Player.Craft("super rod", 1);
      var23.requirements.add(new Player.Craft("good rod", 1));
      var23.requirements.add(new Player.Craft("magnet", 3));
      crafts.add(var23);
      Player.Craft var24 = new Player.Craft("spell tag", 1);
      var24.requirements.add(new Player.Craft("life force", 1));
      var24.requirements.add(new Player.Craft("thin paper", 1));
      crafts.add(var24);
      Player.Craft var25 = new Player.Craft("stardust", 1);
      var25.requirements.add(new Player.Craft("star piece", 1));
      crafts.add(var25);
      Player.Craft var26 = new Player.Craft("poké ball", 1);
      var26.requirements.add(new Player.Craft("magnet", 1));
      var26.requirements.add(new Player.Craft("hard shell", 1));
      crafts.add(var26);
      Player.Craft craftx = new Player.Craft("great ball", 1);
      craftx.requirements.add(new Player.Craft("poké ball", 1));
      craftx.requirements.add(new Player.Craft("metal coat", 1));
      kilnCrafts.add(craftx);
      Player.Craft var28 = new Player.Craft("ultra ball", 1);
      var28.requirements.add(new Player.Craft("great ball", 1));
      var28.requirements.add(new Player.Craft("life force", 1));
      kilnCrafts.add(var28);
      Player.Craft var29 = new Player.Craft("dusk ball", 1);
      var29.requirements.add(new Player.Craft("moon ball", 1));
      var29.requirements.add(new Player.Craft("dark energy", 1));
      kilnCrafts.add(var29);
      Player.Craft var30 = new Player.Craft("net ball", 1);
      var30.requirements.add(new Player.Craft("lure ball", 1));
      var30.requirements.add(new Player.Craft("silky thread", 1));
      kilnCrafts.add(var30);
      Player.Craft var31 = new Player.Craft("dive ball", 1);
      var31.requirements.add(new Player.Craft("lure ball", 1));
      var31.requirements.add(new Player.Craft("hard shell", 1));
      kilnCrafts.add(var31);
      Player.Craft var32 = new Player.Craft("quick ball", 1);
      var32.requirements.add(new Player.Craft("fast ball", 1));
      var32.requirements.add(new Player.Craft("magnet", 1));
      kilnCrafts.add(var32);
      Player.Craft var33 = new Player.Craft("heal ball", 1);
      var33.requirements.add(new Player.Craft("friend ball", 1));
      var33.requirements.add(new Player.Craft("stardust", 1));
      kilnCrafts.add(var33);
      Player.Craft var34 = new Player.Craft("nest ball", 1);
      var34.requirements.add(new Player.Craft("friend ball", 1));
      var34.requirements.add(new Player.Craft("binding band", 1));
      kilnCrafts.add(var34);
      Player.Craft var35 = new Player.Craft("dream ball", 1);
      var35.requirements.add(new Player.Craft("love ball", 1));
      var35.requirements.add(new Player.Craft("psi energy", 1));
      kilnCrafts.add(var35);
      Player.Craft var36 = new Player.Craft("timer ball", 1);
      var36.requirements.add(new Player.Craft("heavy ball", 1));
      var36.requirements.add(new Player.Craft("hard stone", 1));
      kilnCrafts.add(var36);
   }

   public class CaughtFishAnim extends Action {
      public Action.Layer layer = Action.Layer.map_0;
      int timer = 0;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public CaughtFishAnim(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         if (this.timer % 4 == 0) {
            Player.this.position.y--;
         } else if (this.timer % 4 == 2) {
            Player.this.position.y++;
         }

         if (this.timer >= 63) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   static class Craft {
      String name;
      int amount;
      ArrayList<Player.Craft> requirements = new ArrayList<>();

      public Craft(String name, int amount) {
         this.name = name;
         this.amount = amount;
      }
   }

   public class Emote extends Action {
      public Action.Layer layer = Action.Layer.map_109;
      String type;
      Sprite sprite;
      HashMap<String, Sprite> sprites = new HashMap<>();
      int timer = 0;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public Emote(String type, Action nextAction) {
         super();
         this.type = type;
         Texture text = TextureCache.get(Gdx.files.internal("emotes.png"));
         int i = 0;

         for (String name : new String[]{"!", "?", "happy", "skull", "heart", "bolt", "sleep", "fish", "uncomfortable", "..."}) {
            this.sprites.put(name, new Sprite(text, 16 * i, 0, 16, 16));
            i++;
         }

         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         this.sprite = this.sprites.get(this.type);
      }

      @Override
      public void step(Game game) {
         game.mapBatch.draw(this.sprite, Player.this.position.x, Player.this.position.y + 4.0F + 16.0F);
         if (this.timer >= 60) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   public class Flying extends Action {
      public Pokemon pokemon;
      Sprite shadow;
      int animIndex = 0;
      public int xOffset = 0;
      public int yOffset = 32;
      int yOffsetCounter = 0;
      public boolean acceptInput = true;
      public Sprite spritePart;
      boolean takingOff = false;
      public Color batchColor = new Color();
      public boolean drawShadow = true;
      public int xOffset2 = 0;
      public int yOffset2 = 0;

      public Flying(Pokemon pokemon, boolean takingOff, Action nextAction) {
         super();
         this.pokemon = pokemon;
         this.takingOff = takingOff;
         Texture text = TextureCache.get(Gdx.files.internal("shadow1.png"));
         this.shadow = new Sprite(text, 0, 0, 16, 16);
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         game.insertAction(this.nextAction);
         Player.this.flyingAction = this;
         if (this.takingOff) {
            game.insertAction(new Player.Flying.TakingOff(true, null));
            this.yOffset = 0;
         }

         game.player.hmPokemon = this.pokemon;
      }

      @Override
      public void step(Game game) {
         if (this.drawShadow) {
            game.mapBatch.draw(this.shadow, Player.this.position.x, Player.this.position.y);
         }

         int otherOffsetY = 0;
         if (!this.pokemon.specie.name.equals("mewtwo")) {
            if (this.animIndex < 68) {
               this.pokemon.currOwSprite = this.pokemon.standingSprites.get(Player.this.dirFacing);
            } else if (this.animIndex < 72) {
               this.pokemon.currOwSprite = this.pokemon.movingSprites.get(Player.this.dirFacing);
               int var8 = -2;
            } else if (this.animIndex < 76) {
               this.pokemon.currOwSprite = this.pokemon.standingSprites.get(Player.this.dirFacing);
            } else if (this.animIndex < 80) {
               this.pokemon.currOwSprite = this.pokemon.movingSprites.get(Player.this.dirFacing);
               int var9 = -2;
            }
         } else {
            this.pokemon.currOwSprite = this.pokemon.standingSprites.get(Player.this.dirFacing);
         }

         this.xOffset2 = 0;
         this.yOffset2 = 0;
         if (game.player.dirFacing.equals("up")) {
            this.yOffset2++;
         }

         if (game.player.dirFacing.equals("down")) {
            this.yOffset2 += 2;
         } else if (game.player.dirFacing.equals("right")) {
            this.xOffset2 += -2;
            if (game.player.hmPokemon != null && game.player.hmPokemon.specie.name.equals("mewtwo")) {
               this.yOffset2 += 2;
               this.xOffset2--;
            } else if (this.pokemon.specie.name.equals("dragonite")) {
               this.xOffset2 -= 2;
               this.yOffset2 += 2;
            }
         } else if (game.player.dirFacing.equals("left")) {
            this.xOffset2 += 2;
            if (game.player.hmPokemon != null && game.player.hmPokemon.specie.name.equals("mewtwo")) {
               this.yOffset2 += 2;
               this.xOffset2++;
            } else if (this.pokemon.specie.name.equals("dragonite")) {
               this.xOffset2 += 2;
               this.yOffset2 += 2;
            }
         }

         Player.this.currSprite = new Sprite(Player.this.standingSprites.get(Player.this.dirFacing));
         this.spritePart = new Sprite(game.player.currSprite);
         this.spritePart.setRegionY(0);
         this.spritePart.setRegionHeight(14);
         this.batchColor.set(game.mapBatch.getColor());
         if (this.pokemon.hms.contains("FLASH")) {
            game.mapBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         game.mapBatch
            .draw(this.spritePart, game.player.position.x + this.xOffset2 + this.xOffset, game.player.position.y + 21.0F + this.yOffset2 + this.yOffset);
         this.pokemon.position.set(Player.this.position.x + this.xOffset, Player.this.position.y + (16 + this.yOffset) / 2);
         game.mapBatch.draw(this.pokemon.currOwSprite, Player.this.position.x + this.xOffset, Player.this.position.y + 16.0F + this.yOffset);
         if (this.pokemon.specie.name.equals("dragonite")) {
            if (Player.this.dirFacing.equals("up")) {
               game.mapBatch
                  .draw(this.spritePart, game.player.position.x + this.xOffset2 + this.xOffset, game.player.position.y + 21.0F + this.yOffset2 + this.yOffset);
            }
         } else if (!Player.this.dirFacing.equals("down") || this.pokemon.specie.name.equals("mantine")) {
            game.mapBatch
               .draw(this.spritePart, game.player.position.x + this.xOffset2 + this.xOffset, game.player.position.y + 21.0F + this.yOffset2 + this.yOffset);
         }

         game.mapBatch.setColor(this.batchColor);
         if (!this.takingOff) {
            this.animIndex = (this.animIndex + 1) % 80;
            this.yOffsetCounter++;
            if (this.yOffsetCounter % 4 == 0) {
               if (this.yOffsetCounter % 32 > 15) {
                  this.yOffset++;
               } else {
                  this.yOffset--;
               }
            }

            if (this.yOffsetCounter % 32 >= 31) {
               this.yOffset = 32;
            }

            if (this.yOffsetCounter >= 255) {
               this.yOffsetCounter = 0;
               this.xOffset = 0;
            }

            if (Player.this.canMove) {
               if (InputProcessor.startJustPressed) {
                  game.insertAction(
                     new DrawPlayerMenu.Intro(game, new DrawPlayerMenu(game, new WaitFrames(game, 1, new SetField(Player.this, "canMove", true, null))))
                  );
                  Player.this.canMove = false;
               } else if (this.acceptInput) {
                  boolean shouldMove = false;
                  if (InputProcessor.upPressed) {
                     game.player.dirFacing = "up";
                     shouldMove = true;
                  } else if (InputProcessor.downPressed) {
                     game.player.dirFacing = "down";
                     shouldMove = true;
                  } else if (InputProcessor.leftPressed) {
                     game.player.dirFacing = "left";
                     shouldMove = true;
                  } else if (InputProcessor.rightPressed) {
                     game.player.dirFacing = "right";
                     shouldMove = true;
                  } else if (InputProcessor.bJustPressed) {
                     Tile tile = game.map.tiles.get(game.player.position);
                     if (tile.isWater) {
                        Pokemon surfMon = null;

                        for (Pokemon pokemon : game.player.pokemon) {
                           if (pokemon.hms.contains("SURF")) {
                              surfMon = pokemon;
                              break;
                           }
                        }

                        Pokemon belowPokemon = game.map.pokemon.get(game.player.position);
                        boolean pokemonInTheWay = belowPokemon != null && belowPokemon.mapTiles == game.map.tiles;
                        if (surfMon != null && !pokemonInTheWay) {
                           Player.this.hmPokemon = surfMon;
                           this.takingOff = true;
                           game.insertAction(new Player.Flying.TakingOff(false, new PlayerStanding(game)));
                           return;
                        }
                     }

                     if (!tile.isSolid && !tile.isLedge) {
                        this.takingOff = true;
                        game.insertAction(new Player.Flying.TakingOff(false, new PlayerStanding(game)));
                        return;
                     }
                  }

                  if (shouldMove) {
                     this.acceptInput = false;
                     game.insertAction(new Player.Flying.Moving());
                  }
               }
            }
         }
      }

      public class Moving extends Action {
         int timer = 0;

         public Moving() {
            super();
         }

         @Override
         public void step(Game game) {
            if (Player.this.dirFacing.equals("up")) {
               Player.this.position.y += 2.0F;
            } else if (Player.this.dirFacing.equals("down")) {
               Player.this.position.y -= 2.0F;
            } else if (Player.this.dirFacing.equals("right")) {
               Player.this.position.x += 2.0F;
            } else if (Player.this.dirFacing.equals("left")) {
               Player.this.position.x -= 2.0F;
            }

            game.cam.position.set(Player.this.position.x + 16.0F, Player.this.position.y + 16.0F, 0.0F);
            this.timer++;
            if (this.timer >= 8) {
               game.actionStack.remove(this);
               Player.this.checkIfRouteChanged(game);
               if (FogEffect.type != null && !FogEffect.effectArea.contains(Player.this.position)) {
                  FogEffect.refresh = true;
               }

               Player.this.updateMiniMap(game);
               if (!game.map.boundingBox().contains(Player.this.position)) {
                  String awayDir;
                  if (Player.this.position.y > game.map.boundingBox().y + game.map.boundingBox().height) {
                     awayDir = "down";
                  } else if (Player.this.position.y < game.map.boundingBox().y) {
                     awayDir = "up";
                  } else if (Player.this.position.x < game.map.boundingBox().x) {
                     awayDir = "right";
                  } else {
                     awayDir = "left";
                  }

                  int something = 100;
                  int randNum = Game.rand.nextInt(4) + 2;
                  if (randNum > 4) {
                     something = 80;
                  } else if (randNum > 3) {
                     something = 90;
                  }

                  int size = 100 * something * (randNum + 2);
                  game.insertAction(
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
                                    new GenerateWorld(size, Player.this.dirFacing, Flying.this.new Moving())
                                 )
                              )
                           ),
                           new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(Player.this, "dirFacing", awayDir, Flying.this.new Moving())))
                        )
                     )
                  );
                  return;
               }

               Flying.this.acceptInput = true;
            }
         }
      }

      public class TakingOff extends Action {
         int timer = 0;
         boolean takingOff = true;

         public TakingOff(boolean takingOff, Action nextAction) {
            super();
            this.takingOff = takingOff;
            this.nextAction = nextAction;
         }

         @Override
         public void firstStep(Game game) {
            if (this.takingOff) {
               if (Flying.this.pokemon.specie.name.equals("mewtwo")) {
                  game.insertAction(new PlayMusic("teleport_start1", 0.4F, null));
               } else {
                  game.insertAction(new PlayMusic("fly_takingoff1", null));
               }

               Tile currTile = game.map.tiles.get(Player.this.position);
               if (currTile != null && currTile.nameUpper.contains("gate")) {
                  currTile.init(currTile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo);
                  game.insertAction(new PlayMusic("gate2", null));
               }
            } else if (Flying.this.pokemon.specie.name.equals("mewtwo")) {
               game.insertAction(new PlayMusic("teleport_end1", 0.4F, null));
            } else {
               game.insertAction(new PlayMusic("fly_landing1", null));
            }
         }

         @Override
         public void step(Game game) {
            int extra = 0;
            if (this.takingOff && this.timer >= 64) {
               extra = 8;
            } else if (this.timer % 4 == 0) {
               if (this.takingOff) {
                  Flying.this.yOffset += 2;
               } else {
                  Flying.this.yOffset -= 2;
               }
            }

            Flying.this.animIndex = (Flying.this.animIndex - 80 + 1) % (8 + extra) + 80;
            if (!Flying.this.pokemon.specie.name.equals("mewtwo")) {
               if (Flying.this.animIndex < 84 + extra / 2) {
                  Flying.this.pokemon.currOwSprite = Flying.this.pokemon.standingSprites.get(Player.this.dirFacing);
               } else {
                  Flying.this.pokemon.currOwSprite = Flying.this.pokemon.movingSprites.get(Player.this.dirFacing);
               }
            } else {
               Flying.this.pokemon.currOwSprite = Flying.this.pokemon.standingSprites.get(Player.this.dirFacing);
            }

            this.timer++;
            if (!this.takingOff && this.timer < 80 && this.timer >= 64) {
               game.cam.translate(0.0F, -1.0F);
            }

            if (this.takingOff && this.timer < 17 && this.timer >= 0) {
               game.cam.translate(0.0F, 1.0F);
            }

            if (!this.takingOff && this.timer >= 80) {
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
               game.player.flyingAction = null;
               game.actionStack.remove(Flying.this);
               Tile currTile = game.map.tiles.get(Player.this.position);
               if (currTile != null && currTile.nameUpper.contains("gate")) {
                  currTile.overSprite = null;
                  game.insertAction(new PlayMusic("gate1", null));
               }

               if (currTile != null && currTile.isWater) {
                  Player.this.currFieldMove = "SURF";
                  Player.this.swapSpritesSurfing(Player.this.hmPokemon);
               } else {
                  Player.this.currFieldMove = "";
                  Player.this.hmPokemon = null;
               }
            } else if (this.takingOff && this.timer >= 96) {
               Flying.this.takingOff = false;
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
            }
         }
      }
   }

   public class FootPrintFade extends Action {
      public Action.Layer layer = Action.Layer.map_131;
      String dirFacing;
      Color color = new Color();
      float alpha = 0.35F;
      Vector2 position;
      int timer = 0;
      public Sprite sprite;

      public FootPrintFade(String dirFacing, Vector2 position, Action nextAction) {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("footprints1.png"));
         this.sprite = new Sprite(text, 0, 0, 16, 16);
         this.position = position;
         this.dirFacing = dirFacing;
         this.nextAction = nextAction;
      }

      @Override
      public String getCamera() {
         return "map";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void firstStep(Game game) {
         if (this.dirFacing.equals("left")) {
            this.sprite.setRegion(0, 0, 16, 16);
         } else if (this.dirFacing.equals("up")) {
            this.sprite.setRegion(16, 0, 16, 16);
         } else if (this.dirFacing.equals("right")) {
            this.sprite.setRegion(0, 0, 16, 16);
            this.sprite.flip(true, false);
         } else if (this.dirFacing.equals("down")) {
            this.sprite.setRegion(16, 0, 16, 16);
            this.sprite.flip(false, true);
         }
      }

      @Override
      public void step(Game game) {
         this.color.set(game.mapBatch.getColor());
         game.mapBatch.setColor(this.color.r, this.color.g, this.color.b, this.alpha);
         game.mapBatch.draw(this.sprite, this.position.x, this.position.y);
         game.mapBatch.setColor(this.color);
         this.timer++;
         if (this.timer > 60 && this.timer % 8 == 0) {
            this.alpha -= 0.1F;
         }

         if (this.alpha <= 0.0F) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }

   class Network {
      LoadingZone loadingZone = new LoadingZone();
      String id = null;
      boolean shouldMove = false;
      String dirFacing = "down";
      boolean isRunning = false;
      int connectionId;
      String number;
      com.pkmngen.game.Network.BattleData doEncounter;
      int syncTimer = 0;
      Map<Vector2, Tile> tiles;
      boolean isInterior;

      public Network(Vector2 position) {
         this.loadingZone.setSize(768.0F, 768.0F);
         this.loadingZone.setCenter(position);
         this.loadingZone.inner = new LoadingZone();
         this.loadingZone.inner.setSize(224.0F, 224.0F);
         this.loadingZone.inner.setCenter(position);
      }
   }

   public class PuddleSplash extends Action {
      public Action.Layer layer = Action.Layer.map_141;
      Vector2 offset;
      Sprite sprite;
      int timer = 0;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public PuddleSplash(Vector2 offset, Action nextAction) {
         super();
         this.offset = offset;
         this.nextAction = nextAction;
         this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("puddle3.png")));
         this.sprite.setRegion(0, 0, 16, 16);
      }

      @Override
      public void firstStep(Game game) {
         this.sprite.setPosition(Player.this.position.x, Player.this.position.y);
      }

      @Override
      public void step(Game game) {
         if (this.timer == 12) {
            this.sprite.setRegion(16, 0, 16, 16);
         }

         if (this.timer == 24) {
            this.sprite.setRegion(32, 0, 16, 16);
         }

         game.mapBatch.draw(this.sprite, this.sprite.getX() + this.offset.x, this.sprite.getY() + this.offset.y);
         this.timer++;
         if (this.timer > 36) {
            game.actionStack.remove(this);
         }
      }
   }

   public class RemoveFromInventory extends Action {
      String itemName;
      int amount;

      public RemoveFromInventory(String itemName, int amount, Action nextAction) {
         super();
         this.itemName = itemName;
         this.amount = amount;
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         Player.this.setItemAmount(this.itemName, Player.this.getItemAmount(this.itemName) - this.amount);
         if (game.player.getItemAmount(this.itemName) <= 0) {
            game.player.removeItem(this.itemName);
         }

         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   public class Sitting extends Action {
      Tile sittingOn;
      Action standingAction;
      Sprite sittingSprite;
      int stopTimer = 0;
      int offsetX = 0;
      int offsetY = 0;

      public Sitting(Tile sittingOn) {
         super();
         this.sittingOn = sittingOn;
         this.sittingSprite = new Sprite(Player.this.sittingSprites);
         if (this.sittingOn.nameUpper.equals("house_couch1")) {
            this.sittingSprite.setRegion(0, 0, 16, 16);
            this.offsetY = 3;
         } else if (this.sittingOn.nameUpper.equals("house_couch2") || this.sittingOn.nameUpper.equals("house_couch3")) {
            this.sittingSprite.setRegion(32, 0, 16, 16);
            this.sittingSprite.flip(true, false);
            this.offsetX = 3;
            this.offsetY = 5;
         } else if (this.sittingOn.nameUpper.equals("house_couch4") || this.sittingOn.nameUpper.equals("house_couch5")) {
            this.sittingSprite.setRegion(32, 0, 16, 16);
            this.offsetX = -3;
            this.offsetY = 5;
         }
      }

      @Override
      public void firstStep(Game game) {
         Player.this.isSitting = true;
         this.standingAction = Player.this.standingAction;
         game.actionStack.remove(this.standingAction);
      }

      @Override
      public void step(Game game) {
         game.mapBatch.draw(this.sittingSprite, this.sittingOn.position.x + this.offsetX, this.sittingOn.position.y + this.offsetY);
         if (InputProcessor.bJustPressed) {
            this.stopTimer = 30;
         }

         if (this.stopTimer > 0) {
            this.stopTimer--;
            if (this.stopTimer <= 0) {
               game.actionStack.remove(this);
               game.insertAction(new SetField(Player.this, "isSitting", false, new SetField(game, "playerCanMove", true, this.standingAction)));
            }
         }
      }
   }

   class Sliding extends Action {
      public Action.Layer layer = Action.Layer.map_114;
      Vector2 initialPos;
      Vector2 targetPos;
      int timer = 0;

      public Sliding(Action nextAction) {
         super();
         this.nextAction = nextAction;
         if (Player.this.hmPokemon != null) {
            Player.this.hmPokemon.moveDirs.add(Player.this.dirFacing);
            Player.this.hmPokemon.numMoves.add(1.0F);
         }
      }

      @Override
      public void firstStep(Game game) {
         this.initialPos = new Vector2(Player.this.position);
         Tile startTile = game.map.tiles.get(this.initialPos);
         this.targetPos = Player.this.facingPos();
         Tile targetTile = game.map.tiles.get(this.targetPos);
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (game.playerCanMove) {
            if (Player.this.dirFacing.equals("up")) {
               Player.this.position.y += 2.0F;
               game.cam.position.y += 2.0F;
            } else if (Player.this.dirFacing.equals("down")) {
               Player.this.position.y -= 2.0F;
               game.cam.position.y -= 2.0F;
            } else if (Player.this.dirFacing.equals("left")) {
               Player.this.position.x -= 2.0F;
               game.cam.position.x -= 2.0F;
            } else if (Player.this.dirFacing.equals("right")) {
               Player.this.position.x += 2.0F;
               game.cam.position.x += 2.0F;
            }

            if (this.timer == 2) {
               Tile currTile = game.map.tiles.get(this.targetPos);
               if (currTile != null && currTile.nameUpper.contains("gate")) {
                  currTile.overSprite = null;
                  game.insertAction(new PlayMusic("gate1", null));
               }
            } else if (this.timer == 4) {
               Tile currTile = game.map.tiles.get(this.initialPos);
               if (currTile != null && currTile.nameUpper.contains("gate")) {
                  currTile.init(currTile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo);
                  game.insertAction(new PlayMusic("gate2", null));
               }
            }

            if (this.timer > 6) {
               Tile nextTile = game.map.tiles.get(this.targetPos);
               if (nextTile != null && (nextTile.name.contains("puddle1") || nextTile.isTidal)) {
                  Vector2 offset = new Vector2(0.0F, 0.0F);
                  Player.PuddleSplash puddleSplash = game.player.new PuddleSplash(offset, null);
                  if (nextTile.isTidal) {
                     offset.set(0.0F, 4.0F);
                     puddleSplash.layer = Action.Layer.map_116;
                  }

                  game.insertAction(puddleSplash);
                  game.insertAction(game.player.new WaterSplash(false, offset, null));
               }

               if (nextTile != null && nextTile.routeBelongsTo != null) {
                  Route newRoute = nextTile.routeBelongsTo;
                  String newBiome = nextTile.biome;
                  if (!game.map.currRoute.type().equals(newRoute.type()) && !game.map.timeOfDay.equals("night")) {
                     game.musicController.fadeToDungeon = true;
                     if (newRoute.type().equals("desert") && !game.player.enteredDesertBiome) {
                        game.player.enteredDesertBiome = true;
                        game.player.canMove = false;
                        game.insertAction(
                           game.player.new Emote(
                              "!",
                              new DisplayText(
                                 game,
                                 "The sun is scorching hot! This region feels dangerous...",
                                 null,
                                 null,
                                 new RunCode(() -> game.player.canMove = true, null)
                              )
                           )
                        );
                     }

                     if (newRoute.type().equals("graveyard")) {
                        game.insertAction(game.map.new ShadeEffect(newRoute.type()));
                     } else {
                        game.insertAction(game.map.new ShadeEffect(newRoute.type()));
                     }
                  }

                  if (game.map.currRoute != newRoute) {
                     if (game.map.currRoute.name.equals(newRoute.name)) {
                        newRoute.music = game.map.currRoute.music;
                     } else {
                        newRoute.music = game.map.currRoute.music;
                        System.out.println("New Route: " + newRoute.name);
                     }

                     game.map.currRoute = newRoute;
                  }

                  game.map.currBiome = newBiome;
               }

               Player.this.isNearCampfire = Player.this.checkNearCampfire();
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
               this.nextAction.step(game);
            }

            this.timer++;
         }
      }
   }

   public class SurfWakeAnim extends Action {
      public Action.Layer layer = Action.Layer.map_129;
      Sprite sprite;
      int timer = 0;
      boolean flipX = false;
      boolean flipY = false;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public SurfWakeAnim(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("wake1.png")));
         this.sprite.setPosition(Player.this.position.x, Player.this.position.y - 4.0F);
         if (Player.this.dirFacing.equals("right")) {
            this.flipX = true;
         } else if (Player.this.dirFacing.equals("up")) {
            this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("wake2.png")));
            this.flipY = true;
            this.sprite.setPosition(Player.this.position.x, Player.this.position.y);
         } else if (Player.this.dirFacing.equals("down")) {
            this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("wake2.png")));
            this.sprite.setPosition(Player.this.position.x, Player.this.position.y);
         }

         this.sprite.setRegion(0, 0, 16, 16);
      }

      @Override
      public void step(Game game) {
         if (this.timer == 12) {
            this.sprite.setRegion(16, 0, 16, 16);
         }

         if (this.timer == 24) {
            this.sprite.setRegion(32, 0, 16, 16);
         }

         float U = this.sprite.getU();
         float U2 = this.sprite.getU2();
         if (this.flipX) {
            U = this.sprite.getU2();
            U2 = this.sprite.getU();
         }

         float V = this.sprite.getV();
         float V2 = this.sprite.getV2();
         if (this.flipY) {
            V = this.sprite.getV2();
            V2 = this.sprite.getV();
         }

         game.mapBatch
            .draw(this.sprite.getTexture(), this.sprite.getX(), this.sprite.getY(), this.sprite.getRegionWidth(), this.sprite.getRegionHeight(), U, V, U2, V2);
         this.timer++;
         if (this.timer > 36) {
            game.actionStack.remove(this);
         }
      }
   }

   class TeleportDown extends Action {
      public Action.Layer layer = Action.Layer.map_0;
      int timer = 0;
      int phase = 0;

      public TeleportDown(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         game.insertAction(new PlayMusic("teleport_end1", 0.4F, null));
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (this.phase == 0) {
            if (this.timer == 0) {
               Player.this.isSitting = false;
               Player.this.dirFacing = "down";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y -= 9.0F;
            } else if (this.timer == 2) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 1) {
            if (this.timer == 0) {
               Player.this.dirFacing = "right";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y -= 9.0F;
            } else if (this.timer == 2) {
               Player.this.position.y -= 9.0F;
            } else if (this.timer == 4) {
               Player.this.position.y -= 8.0F;
            } else if (this.timer == 6) {
               Player.this.position.y -= 7.0F;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 2) {
            if (this.timer == 0) {
               Player.this.dirFacing = "up";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y -= 7.0F;
            } else if (this.timer == 2) {
               Player.this.position.y -= 7.0F;
            } else if (this.timer == 4) {
               Player.this.position.y -= 5.0F;
            } else if (this.timer == 6) {
               Player.this.position.y -= 5.0F;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 3) {
            if (this.timer == 0) {
               Player.this.dirFacing = "left";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y -= 4.0F;
            } else if (this.timer == 2) {
               Player.this.position.y -= 3.0F;
            } else if (this.timer == 4) {
               Player.this.position.y -= 3.0F;
            } else if (this.timer == 6) {
               Player.this.position.y--;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 4) {
            if (this.timer == 0) {
               Player.this.dirFacing = "down";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y--;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 5) {
            if (this.timer == 0) {
               Player.this.dirFacing = "right";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 6) {
            if (this.timer == 0) {
               Player.this.dirFacing = "up";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 7) {
            if (this.timer == 0) {
               Player.this.dirFacing = "left";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 8) {
            if (this.timer == 0) {
               Player.this.dirFacing = "down";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
               Tile tile = game.map.tiles.get(game.player.position);
               if (tile != null) {
                  Route newRoute = tile.routeBelongsTo;
                  if (newRoute != null) {
                     game.map.currRoute = newRoute;
                     if (game.map.timeOfDay.equals("day")) {
                        game.insertAction(game.map.new ShadeEffect(newRoute.type()));
                     }
                  }

                  if (!game.map.currRoute.name.equals(newRoute.name)) {
                     newRoute.music = game.map.currRoute.music;
                     game.map.currRoute = newRoute;
                  }

                  game.map.currBiome = tile.biome;
               }

               Player.this.isNearCampfire = Player.this.checkNearCampfire();
            }
         }

         this.timer++;
      }
   }

   class TeleportUp extends Action {
      public Action.Layer layer = Action.Layer.map_0;
      int timer = 0;
      int phase = 0;

      public TeleportUp(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         game.insertAction(new PlayMusic("teleport_start1", 0.4F, null));
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (this.phase == 0) {
            if (this.timer == 0) {
               Player.this.dirFacing = "right";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 1) {
            if (this.timer == 0) {
               Player.this.dirFacing = "up";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 2) {
            if (this.timer == 0) {
               Player.this.dirFacing = "left";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 3) {
            if (this.timer == 0) {
               Player.this.dirFacing = "down";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
            } else if (this.timer == 2) {
               Player.this.position.y++;
            } else if (this.timer == 4) {
               Player.this.position.y++;
            } else if (this.timer == 6) {
               Player.this.position.y += 3.0F;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 4) {
            if (this.timer == 0) {
               Player.this.dirFacing = "right";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y += 3.0F;
            } else if (this.timer == 2) {
               Player.this.position.y += 4.0F;
            } else if (this.timer == 4) {
               Player.this.position.y += 5.0F;
            } else if (this.timer == 6) {
               Player.this.position.y += 5.0F;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 5) {
            if (this.timer == 0) {
               Player.this.dirFacing = "up";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y += 7.0F;
            } else if (this.timer == 2) {
               Player.this.position.y += 7.0F;
            } else if (this.timer == 4) {
               Player.this.position.y += 7.0F;
            } else if (this.timer == 6) {
               Player.this.position.y += 8.0F;
            } else if (this.timer == 8) {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 6) {
            if (this.timer == 0) {
               Player.this.dirFacing = "left";
               Player.this.currSprite = Player.this.standingSprites.get(game.player.dirFacing);
               Player.this.position.y += 9.0F;
            } else if (this.timer == 2) {
               Player.this.position.y += 9.0F;
            } else if (this.timer == 4) {
               Player.this.position.y += 9.0F;
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 7) {
            if (this.timer == 2) {
               Player.this.isSitting = true;
            } else if (this.timer == 5) {
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
            }
         }

         this.timer++;
      }
   }

   enum Type {
      LOCAL,
      REMOTE;
   }

   public class WaterSplash extends Action {
      public Action.Layer layer = Action.Layer.map_114;
      Vector2 offset;
      boolean flip = false;
      Sprite sprite;
      int timer = 0;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public WaterSplash(boolean flip, Action nextAction) {
         this(flip, new Vector2(0.0F, 0.0F), nextAction);
      }

      public WaterSplash(boolean flip, Vector2 offset, Action nextAction) {
         super();
         this.offset = offset;
         this.nextAction = nextAction;
         this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("splash_anim4.png")));
         this.sprite.setRegion(0, 0, 16, 16);
         this.flip = flip;
      }

      @Override
      public void step(Game game) {
         if (this.timer == 4) {
            this.sprite.setRegion(16, 0, 16, 16);
         } else if (this.timer == 5) {
            this.sprite.setRegion(32, 0, 16, 16);
         }

         game.mapBatch
            .draw(
               this.sprite.getTexture(),
               Player.this.position.x + this.offset.x,
               Player.this.position.y + 3.0F + this.offset.y,
               16.0F,
               16.0F,
               this.sprite.getRegionX(),
               this.sprite.getRegionY(),
               this.sprite.getRegionWidth(),
               this.sprite.getRegionHeight(),
               this.flip,
               false
            );
         this.timer++;
         if (this.timer >= 10) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
