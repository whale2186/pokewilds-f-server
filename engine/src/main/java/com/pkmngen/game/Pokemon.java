package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Pokemon extends OverworldThing {
   public static ArrayList<String> attacksImplemented = new ArrayList<>();
   public static List<String> onlySwim;
   public static ArrayList<String> aggroAnyway;
   public static ArrayList<String> dontAggro;
   private Boolean _isSkittish = null;
   public static ArrayList<String> skittishMons;
   public static HashMap<String, Texture> textures;
   public static HashMap<String, String> baseSpecies;
   public static HashMap<String, ArrayList<String>> eggMoves;
   private static final Vector2[] POSITIONS;
   private Vector2 startPos = new Vector2();
   private Vector2 endPos = new Vector2();
   private Vector2 currPos = new Vector2();
   public static HashMap<String, Float> weights;
   public String nickname;
   public Specie specie;
   public int level;
   public int exp;
   public String dexNumber;
   public int happiness = 1;
   public boolean isShiny = false;
   public String growthRateGroup = "";
   Map<String, Integer> baseStats = new HashMap<>();
   Map<String, Integer> currentStats = new HashMap<>();
   Map<String, Integer> statStages = new HashMap<>();
   Map<String, Integer> maxStats;
   ArrayList<String> hms;
   public String status;
   public int statusCounter;
   public ArrayList<String> volatileStatus;
   public HashMap<String, Integer> volatileStatusCounter;
   public int disabledIndex;
   public int disabledCounter;
   public boolean flinched;
   SpriteProxy sprite;
   SpriteProxy backSprite;
   public Map<String, Sprite> standingSprites;
   public Map<String, Sprite> movingSprites;
   public Map<String, Sprite> altMovingSprites;
   public Action standingAction;
   public Pokemon.DrawUpper drawUpper;
   public Pokemon.DrawLower drawLower;
   public Sprite currOwSprite;
   public int spriteOffsetY;
   public boolean canMove;
   Map<Vector2, Tile> mapTiles;
   public int interiorIndex;
   boolean isRunning;
   public boolean shouldMove;
   Player.Type type;
   Player previousOwner;
   public boolean inHabitat;
   public boolean inShelter;
   private List<String[]> habitats;
   public String hasItem;
   public int harvestTimer;
   public int harvestTimerMax;
   public ArrayList<String> harvestables;
   ArrayList<Sprite> avatarSprites;
   SpriteProxy breathingSprite;
   ArrayList<String> types;
   int angry;
   int eating;
   String[] attacks;
   String gender;
   String[] eggGroups;
   boolean isEgg;
   boolean isGhost;
   Pokemon loveInterest;
   public static int layEggTimerMax;
   int layEggTimer;
   int nearbyEggs;
   int habitatCheckCounter;
   String trappedBy;
   int trapCounter;
   Pokemon cantEscapeBy;
   Map<Integer, String[]> learnSet;
   boolean inBattle;
   public boolean aggroPlayer;
   public boolean interactedWith;
   public boolean drawThisFrame;
   public boolean isTrapping;
   public Tile onTile;
   public boolean inWater;
   public boolean isCharmed;
   public boolean participatedInBattle;
   public boolean gainedLevel;
   Pokemon.Generation generation;
   ArrayList<SpriteProxy> introAnim;
   public ArrayList<String> moveDirs;
   public ArrayList<Integer> ledgeJumps;
   public ArrayList<Float> numMoves;
   public static Random rand;
   public Map<String, Sprite> surfSprites;
   public Map<String, Sprite> surfMovingSprites;
   Pokemon friend;
   public static int friendTimerMax;
   int friendTimer;
   public String _baseSpecie;
   public static int currStagger;
   public static int maxStagger;
   private static final Vector2[] FRIEND_OFFSETS;

   public boolean isSkittish() {
      if (this._isSkittish == null) {
         this._isSkittish = skittishMons.contains(this.specie.name);
      }

      return this._isSkittish;
   }

   public static String nameToIndex(String name) {
      name = name.toLowerCase(Locale.ROOT);
      if (name.contains("unown")) {
         name = "unown";
      }

      if (name.equals("farfetch_d")) {
         name = "farfetch�d";
      }

      if (name.equals("ho_oh")) {
         name = "ho-oh";
      }

      if (name.equals("aexeggutor")) {
         name = "exeggutor";
      } else if (name.equals("araichu")) {
         name = "raichu";
      } else if (name.equals("gcorsola")) {
         name = "corsola";
      } else if (name.equals("gyamask")) {
         name = "yamask";
      } else if (name.equals("gdarumaka")) {
         name = "darumaka";
      } else if (name.equals("gdarmanitan")) {
         name = "darmanitan";
      } else if (name.equals("gdarmanitanzen")) {
         name = "darmanitan";
      } else if (name.equals("darmanitanzen")) {
         name = "darmanitan";
      } else if (name.equals("combee_female")) {
         name = "combee";
      } else if (name.equals("amarowak")) {
         name = "marowak";
      }

      int lineNum = 1;

      try {
         FileHandle file = Gdx.files.internal("pokemon/pokemon_to_index.txt");
         Reader reader = file.reader();
         BufferedReader br = new BufferedReader(reader);

         String line;
         while ((line = br.readLine()) != null && !line.equalsIgnoreCase(name)) {
            lineNum++;
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      if (name.equals("straigar")) {
         lineNum = 2;
      }

      return String.format(Locale.ROOT, "%03d", lineNum);
   }

   public String lanOwnerId;
   public String lanOwnerName;

   public Pokemon(Network.PokemonDataBase pokemonData) {
      this.statStages.put("attack", 0);
      this.statStages.put("defense", 0);
      this.statStages.put("specialAtk", 0);
      this.statStages.put("specialDef", 0);
      this.statStages.put("speed", 0);
      this.statStages.put("accuracy", 0);
      this.statStages.put("evasion", 0);
      this.maxStats = new HashMap<>();
      this.hms = new ArrayList<>();
      this.status = null;
      this.statusCounter = 0;
      this.volatileStatus = new ArrayList<>();
      this.volatileStatusCounter = new HashMap<>();
      this.disabledIndex = -1;
      this.disabledCounter = 0;
      this.flinched = false;
      this.standingSprites = new HashMap<>();
      this.movingSprites = new HashMap<>();
      this.altMovingSprites = new HashMap<>();
      this.standingAction = null;
      this.drawUpper = null;
      this.drawLower = null;
      this.currOwSprite = null;
      this.spriteOffsetY = 0;
      this.canMove = true;
      this.interiorIndex = 100;
      this.isRunning = false;
      this.shouldMove = false;
      this.type = Player.Type.LOCAL;
      this.previousOwner = null;
      this.inHabitat = false;
      this.inShelter = false;
      this.habitats = new ArrayList<>();
      this.habitats.add(new String[]{"green"});
      this.hasItem = null;
      this.harvestTimer = 0;
      this.harvestTimerMax = 9000;
      this.harvestables = new ArrayList<>();
      this.harvestables.add("manure");
      this.avatarSprites = new ArrayList<>();
      this.breathingSprite = null;
      this.gender = null;
      this.eggGroups = new String[2];
      this.isEgg = false;
      this.isGhost = false;
      this.loveInterest = null;
      this.layEggTimer = layEggTimerMax;
      this.nearbyEggs = 0;
      this.habitatCheckCounter = 0;
      this.trappedBy = null;
      this.trapCounter = 0;
      this.cantEscapeBy = null;
      this.inBattle = false;
      this.aggroPlayer = false;
      this.interactedWith = false;
      this.drawThisFrame = true;
      this.isTrapping = false;
      this.inWater = false;
      this.isCharmed = false;
      this.participatedInBattle = false;
      this.gainedLevel = false;
      this.moveDirs = new ArrayList<>();
      this.ledgeJumps = new ArrayList<>();
      this.numMoves = new ArrayList<>();
      this.surfSprites = null;
      this.surfMovingSprites = null;
      this.friend = null;
      this.friendTimer = friendTimerMax;
      this._baseSpecie = null;
      if (pokemonData instanceof Network.PokemonDataV05) {
         this.isEgg = ((Network.PokemonDataV05)pokemonData).name.equals("egg");
         if (((Network.PokemonDataV05)pokemonData).eggHatchInto != null) {
            pokemonData.name = ((Network.PokemonDataV05)pokemonData).eggHatchInto;
         }
      }

      this.init(pokemonData.name, pokemonData.level, pokemonData.generation, pokemonData.isShiny, this.isEgg);
      this.currentStats.put("hp", pokemonData.hp);
      this.attacks[0] = pokemonData.attacks[0];
      this.attacks[1] = pokemonData.attacks[1];
      this.attacks[2] = pokemonData.attacks[2];
      this.attacks[3] = pokemonData.attacks[3];
      this.position = pokemonData.position;
      if (pokemonData instanceof Network.PokemonDataV07) {
         this.interiorIndex = ((Network.PokemonDataV07)pokemonData).interiorIndex;
      }

      if (pokemonData.isInterior) {
         System.out.println("this.nickname");
         System.out.println(this.nickname);
         System.out.println("Game.staticGame.map.interiorTilesIndex");
         System.out.println(Game.staticGame.map.interiorTilesIndex);
         System.out.println("this.interiorIndex");
         System.out.println(this.interiorIndex);
         this.mapTiles = Game.staticGame.map.interiorTiles.get(this.interiorIndex);
      } else {
         this.mapTiles = Game.staticGame.map.overworldTiles;
      }

      this.initHabitatValues();
      this.status = pokemonData.status;
      this.harvestTimer = pokemonData.harvestTimer;
      if (pokemonData.previousOwnerName != null) {
         this.previousOwner = Game.staticGame.player;
         if (Game.staticGame.players.containsKey(pokemonData.previousOwnerName)) {
            this.previousOwner = Game.staticGame.players.get(pokemonData.previousOwnerName);
         }
      }

      if (pokemonData instanceof Network.PokemonDataV05) {
         this.gender = ((Network.PokemonDataV05)pokemonData).gender;
         this.happiness = ((Network.PokemonDataV05)pokemonData).friendliness;
         this.aggroPlayer = ((Network.PokemonDataV05)pokemonData).aggroPlayer;
         if (this.previousOwner == null && this.lanOwnerId == null && "drapion".equals(this.specie.name)) {
            this.aggroPlayer = true;
         }
      }

      if (pokemonData instanceof Network.PokemonDataV07) {
         this.nickname = ((Network.PokemonDataV07)pokemonData).nickname;
      }

      if (pokemonData instanceof Network.PokemonData) {
         Network.PokemonData data = (Network.PokemonData)pokemonData;
         this.exp = data.exp;
         this.lanOwnerId = data.lanOwnerId; this.lanOwnerName = data.lanOwnerName;
         if (Game.staticGame.coop != null && this.lanOwnerId != null)
            this.previousOwner = Game.staticGame.coop.resolveOwner(this.lanOwnerId, this.lanOwnerName);
      }
   }

   public Pokemon(String name, int level) {
      this(name, level, Pokemon.Generation.CRYSTAL);
   }

   public Pokemon(String name, int level, Pokemon.Generation generation) {
      this(name, level, generation, rand.nextInt(Game.staticGame.shinyRate) == 0);
   }

   public Pokemon(String name, int level, Pokemon.Generation generation, boolean isShiny) {
      this(name, level, generation, isShiny, false);
   }

   public Pokemon(String name, int level, Pokemon.Generation generation, boolean isShiny, boolean isEgg) {
      this.statStages.put("attack", 0);
      this.statStages.put("defense", 0);
      this.statStages.put("specialAtk", 0);
      this.statStages.put("specialDef", 0);
      this.statStages.put("speed", 0);
      this.statStages.put("accuracy", 0);
      this.statStages.put("evasion", 0);
      this.maxStats = new HashMap<>();
      this.hms = new ArrayList<>();
      this.status = null;
      this.statusCounter = 0;
      this.volatileStatus = new ArrayList<>();
      this.volatileStatusCounter = new HashMap<>();
      this.disabledIndex = -1;
      this.disabledCounter = 0;
      this.flinched = false;
      this.standingSprites = new HashMap<>();
      this.movingSprites = new HashMap<>();
      this.altMovingSprites = new HashMap<>();
      this.standingAction = null;
      this.drawUpper = null;
      this.drawLower = null;
      this.currOwSprite = null;
      this.spriteOffsetY = 0;
      this.canMove = true;
      this.interiorIndex = 100;
      this.isRunning = false;
      this.shouldMove = false;
      this.type = Player.Type.LOCAL;
      this.previousOwner = null;
      this.inHabitat = false;
      this.inShelter = false;
      this.habitats = new ArrayList<>();
      this.habitats.add(new String[]{"green"});
      this.hasItem = null;
      this.harvestTimer = 0;
      this.harvestTimerMax = 9000;
      this.harvestables = new ArrayList<>();
      this.harvestables.add("manure");
      this.avatarSprites = new ArrayList<>();
      this.breathingSprite = null;
      this.gender = null;
      this.eggGroups = new String[2];
      this.isEgg = false;
      this.isGhost = false;
      this.loveInterest = null;
      this.layEggTimer = layEggTimerMax;
      this.nearbyEggs = 0;
      this.habitatCheckCounter = 0;
      this.trappedBy = null;
      this.trapCounter = 0;
      this.cantEscapeBy = null;
      this.inBattle = false;
      this.aggroPlayer = false;
      this.interactedWith = false;
      this.drawThisFrame = true;
      this.isTrapping = false;
      this.inWater = false;
      this.isCharmed = false;
      this.participatedInBattle = false;
      this.gainedLevel = false;
      this.moveDirs = new ArrayList<>();
      this.ledgeJumps = new ArrayList<>();
      this.numMoves = new ArrayList<>();
      this.surfSprites = null;
      this.surfMovingSprites = null;
      this.friend = null;
      this.friendTimer = friendTimerMax;
      this._baseSpecie = null;
      this.init(name, level, generation, isShiny, isEgg);
      if (this.specie.name.equals("combee") && this.gender.equals("female")) {
         this.updateSpecieInfo("combee_female");
      }
   }

   public void updateSpecieInfo(String specieName) {
      this.updateSpecieInfo(specieName, null);
   }

   public void updateSpecieInfo(String specieName, String prevSpecieNickname) {
      if (!Specie.species.containsKey(specieName)) {
         if (Thread.currentThread() != Game.staticGame.gameThread) {
            final String finalName = specieName;
            Runnable runnable = new Runnable() {
               @Override
               public void run() {
                  try {
                     Specie.species.put(finalName, new Specie(finalName));
                  } catch (Exception e) {
                     e.printStackTrace();
                  }

                  synchronized (this) {
                     this.notify();
                  }
               }
            };
            try {
               synchronized (runnable) {
                  Gdx.app.postRunnable(runnable);
                  runnable.wait();
               }
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         } else {
            Specie.species.put(specieName, new Specie(specieName));
         }
      }

      this.specie = Specie.species.get(specieName);
      if (this.specie == null) {
         System.out.println("No such specie exists: " + specieName);
      }

      if (prevSpecieNickname == null || prevSpecieNickname.equalsIgnoreCase(this.nickname)) {
         this.nickname = this.getNickname();
      }

      if (this.isEgg) {
         this.nickname = "egg";
         this.sprite = Specie.spriteEgg;
         this.backSprite = Specie.backSpriteEgg;
         this.introAnim = Specie.introAnimEgg;
      } else if (this.isShiny) {
         this.sprite = this.specie.spriteShiny;
         this.backSprite = this.specie.backSpriteShiny;
         this.introAnim = this.specie.introAnimShiny;
      } else {
         this.sprite = this.specie.sprite;
         this.backSprite = this.specie.backSprite;
         this.introAnim = this.specie.introAnim;
      }

      this.learnSet = this.specie.learnSet;
      this.dexNumber = this.specie.dexNumber;
      this.baseStats = this.specie.baseStats;
      this.types = this.specie.types;
      this.growthRateGroup = this.specie.growthRateGroup;
      this.eggGroups = this.specie.eggGroups;
      if (!this.isEgg) {
         this.hms = this.specie.hms;
      }

      this.loadOverworldSprites();
      this.calcMaxStats();
      this.initHabitatValues();
   }

   public void init(String specieName, int level, Pokemon.Generation generation, boolean isShiny, boolean isEgg) {
      if (level <= 0) {
         System.out.println("Bad level: " + String.valueOf(level));
         level = 1;
      }

      this.generation = generation;
      this.level = level;
      this.isEgg = isEgg;
      this.isShiny = isShiny;
      this.updateSpecieInfo(specieName);
      this.currentStats = new HashMap<>(this.maxStats);
      this.angry = 0;
      this.eating = 0;
      this.attacks = new String[]{null, null, null, null};
      if (this.specie.genderRatio.equals("GENDER_UNKNOWN")) {
         this.gender = "unknown";
      } else {
         int percentFemale = 0;
         if (this.specie.genderRatio.equals("GENDER_F12_5")) {
            percentFemale = 125;
         } else if (this.specie.genderRatio.equals("GENDER_F25")) {
            percentFemale = 250;
         } else if (this.specie.genderRatio.equals("GENDER_F50")) {
            percentFemale = 500;
         } else if (this.specie.genderRatio.equals("GENDER_F75")) {
            percentFemale = 750;
         } else if (this.specie.genderRatio.equals("GENDER_F100")) {
            percentFemale = 1000;
         }

         if (rand.nextInt(1000) < percentFemale) {
            this.gender = "female";
         } else {
            this.gender = "male";
         }
      }

      this.happiness = this.specie.baseHappiness;
      if (this.isEgg) {
         this.happiness = this.specie.eggCycles;
      }

      this.getCurrentAttacks();
      this.exp = this.calcExpForLevel(this.level);
   }

   public String baseSpecie() {
      if (this._baseSpecie == null) {
         String name = this.specie.name;
         if (name.contains("unown")) {
            name = "unown";
         }

         this._baseSpecie = baseSpecies.get(name);
      }

      return this._baseSpecie;
   }

   public boolean hasEvo() {
      return !Specie.gen2Evos.get(this.specie.name).isEmpty();
   }

   void calcMaxStats() {
      this.maxStats.put("hp", (this.baseStats.get("hp") + 50) * this.level / 50 + 10);
      this.maxStats.put("attack", this.baseStats.get("attack") * this.level / 50 + 10);
      this.maxStats.put("defense", this.baseStats.get("defense") * this.level / 50 + 10);
      this.maxStats.put("specialAtk", this.baseStats.get("specialAtk") * this.level / 50 + 10);
      this.maxStats.put("specialDef", this.baseStats.get("specialDef") * this.level / 50 + 10);
      this.maxStats.put("speed", this.baseStats.get("speed") * this.level / 50 + 10);
      this.maxStats.put("catchRate", this.baseStats.get("catchRate"));
   }

   void checkHabitat(Game game) {
      Pokemon prevLoveInterest = this.loveInterest;
      if (this.loveInterest != null) {
         this.loveInterest.loveInterest = null;
      }

      this.loveInterest = null;
      this.startPos.set(this.position).add(-48.0F, -48.0F);
      this.startPos.x = (int)this.startPos.x - Math.floorMod((int)this.startPos.x, 16);
      this.startPos.y = (int)this.startPos.y - Math.floorMod((int)this.startPos.y, 16);
      this.endPos.set(this.position).add(48.0F, 48.0F);
      this.endPos.x = (int)this.endPos.x - Math.floorMod((int)this.endPos.x, 16);
      this.endPos.y = (int)this.endPos.y - Math.floorMod((int)this.endPos.y, 16);
      int fenceCount = 0;
      int roofCount = 0;
      this.nearbyEggs = 0;
      ArrayList<String[]> notFoundHabitats = new ArrayList<>(this.habitats);
      this.currPos.set(this.startPos.x, this.startPos.y);

      while (this.currPos.y <= this.endPos.y) {
         Tile tile = this.mapTiles.get(this.currPos);
         this.currPos.x += 16.0F;
         if (this.currPos.x > this.endPos.x) {
            this.currPos.x = this.startPos.x;
            this.currPos.y += 16.0F;
         }

         if (tile != null) {
            if (game.map.tiles == this.mapTiles && game.player.position.equals(tile.position) && this.aggroPlayer) {
               game.player.nearAggroPokemon = true;
            }

            if (tile.nameUpper.contains("fence")) {
               fenceCount++;
            } else if (tile.nameUpper.contains("roof")) {
               roofCount++;
            }

            for (int i = 0; i < this.habitats.size(); i++) {
               String[] habitat = this.habitats.get(i);

               for (int j = 0; j < habitat.length; j++) {
                  String name = habitat[j];
                  if (tile.name.contains(name) || tile.nameUpper.contains(name)) {
                     notFoundHabitats.remove(habitat);
                     break;
                  }
               }
            }

            if (game.map.pokemon.containsKey(tile.position)) {
               Pokemon pokemon = game.map.pokemon.get(tile.position);
               if (pokemon != this && pokemon.isEgg && pokemon.mapTiles == this.mapTiles) {
                  this.nearbyEggs++;
               }
            }

            if (!this.isEgg
               && this.loveInterest == null
               && game.map.pokemon.containsKey(tile.position)
               && game.map.pokemon.get(tile.position) != this
               && game.map.pokemon.get(tile.position).mapTiles == this.mapTiles) {
               Pokemon potentialMate = game.map.pokemon.get(tile.position);
               String oppGender = this.gender.equals("male") ? "female" : "male";
               boolean genderCompatible = potentialMate.gender.equals(oppGender);
               boolean sameEggGroup = false;

               eggGroupCheck:
               for (int i = 0; i < this.eggGroups.length; i++) {
                  String group1 = this.eggGroups[i];
                  if (group1 == null || group1.equals("EGG_NONE")) continue;

                  for (int j = 0; j < potentialMate.eggGroups.length; j++) {
                     String group2 = potentialMate.eggGroups[j];
                     if (group2 == null || group2.equals("EGG_NONE")) continue;

                     if (group1.equals("EGG_DITTO") || group2.equals("EGG_DITTO")) {
                        genderCompatible = true;
                        sameEggGroup = true;
                        break eggGroupCheck;
                     }

                     if (group1.equals(group2)) {
                        sameEggGroup = true;
                        break eggGroupCheck;
                     }
                  }
               }

               boolean doubleDitto = this.specie.name.equals("ditto") && potentialMate.specie.name.equals("ditto");
               if (!potentialMate.isEgg && genderCompatible && sameEggGroup && !doubleDitto && potentialMate.loveInterest == null) {
                  this.loveInterest = potentialMate;
                  potentialMate.loveInterest = this;
                  this.friend = null;
                  potentialMate.friend = null;
                  if (potentialMate != prevLoveInterest) {
                     if (!potentialMate.aggroPlayer) {
                        game.insertAction(potentialMate.new Emote("heart", null));
                     }

                     if (!this.aggroPlayer) {
                        game.insertAction(new Pokemon.Emote("heart", null));
                     }
                  }
               }

               if (this.loveInterest == null
                  && this.friend == null
                  && this.friend != potentialMate
                  && !potentialMate.isEgg
                  && !genderCompatible
                  && sameEggGroup) {
                  this.friend = potentialMate;
                  potentialMate.friend = this;
               }
            }
         }
      }

      if (this.types.contains("DARK") && !game.map.timeOfDay.equals("night")) {
         notFoundHabitats.add(new String[]{"night"});
      }

      this.inHabitat = notFoundHabitats.size() == 0;
      this.inShelter = roofCount >= 3 && fenceCount >= 2;
   }

   void gainLevel(int numLevels) {
      int prevMaxHp = this.maxStats.get("hp");
      this.level += numLevels;
      this.calcMaxStats();

      for (String stat : this.maxStats.keySet()) {
         if (stat.equals("hp")) {
            int prevCurrentHp = this.currentStats.get("hp");
            this.currentStats.put(stat, prevCurrentHp + (this.maxStats.get("hp") - prevMaxHp));
         } else {
            this.currentStats.put(stat, this.maxStats.get(stat));
         }
      }

      if (this.happiness < 100) {
         this.gainHappiness(5);
      } else if (this.happiness < 200) {
         this.gainHappiness(3);
      } else {
         this.gainHappiness(2);
      }
   }

   void gainHappiness(int amount) {
      this.happiness += amount;
      if (this.happiness > 220) {
         this.happiness = 220;
      } else if (this.happiness < 0) {
         this.happiness = 0;
      }
   }

   public String getNickname() {
      String nickname = this.specie.name;
      if (this.specie.modNickname != null) {
         nickname = this.specie.modNickname;
      } else if (nickname.equals("aexeggutor")) {
         nickname = "exeggutor";
      } else if (nickname.equals("araichu")) {
         nickname = "raichu";
      } else if (nickname.equals("gcorsola")) {
         nickname = "corsola";
      } else if (nickname.equals("gyamask")) {
         nickname = "yamask";
      } else if (nickname.equals("gdarumaka")) {
         nickname = "darumaka";
      } else if (nickname.equals("gdarmanitan")) {
         nickname = "darmanitan";
      } else if (nickname.equals("gdarmanitanzen")) {
         nickname = "darmanitan";
      } else if (nickname.equals("darmanitanzen")) {
         nickname = "darmanitan";
      } else if (nickname.equals("combee_female")) {
         nickname = "combee";
      } else if (nickname.equals("mrmime")) {
         nickname = "mr.mime";
      } else if (nickname.contains("unown")) {
         nickname = "unown";
      } else if (nickname.equals("farfetch_d")) {
         nickname = "farfetch�d";
      } else if (nickname.equals("nidoran_f") || nickname.equals("nidoran_m")) {
         nickname = "nidoran";
      } else if (nickname.equals("straigar")) {
         nickname = "raitora";
      } else if (nickname.equals("amarowak")) {
         nickname = "marowak";
      }

      return nickname;
   }

   void hatch() {
      this.isEgg = false;
      this.loadOverworldSprites();
      this.nickname = this.getNickname();
      this.hms = this.specie.hms;
      if (this.isShiny) {
         this.sprite = this.specie.spriteShiny;
         this.backSprite = this.specie.backSpriteShiny;
         this.introAnim = this.specie.introAnimShiny;
      } else {
         this.sprite = this.specie.sprite;
         this.backSprite = this.specie.backSprite;
         this.introAnim = this.specie.introAnim;
      }

      this.happiness = 100;
   }

   void layEgg(Game game, Vector2 eggPos) {
      this.layEggTimer = layEggTimerMax;
      String base = baseSpecies.get(this.specie.name.toLowerCase(Locale.ROOT));
      if (this.specie.name.equals("ditto") && this.loveInterest != null) {
         base = baseSpecies.get(this.loveInterest.specie.name.toLowerCase(Locale.ROOT));
      }

      if (base == null) {
         base = this.specie.name.toLowerCase(Locale.ROOT);
      }

      if (base.equals("nidoran_f") && Game.rand.nextInt(256) < 128) {
         base = "nidoran_m";
      }

      Pokemon pokemonEgg = new Pokemon(base, 5, Pokemon.Generation.CRYSTAL, Game.rand.nextInt(256) == 0, true);
      int currIndex = 0;

      for (int i = 0; i < pokemonEgg.attacks.length; i++) {
         if (pokemonEgg.attacks[i] == null) {
            currIndex = i;
            break;
         }
      }

      if (Pokemon.eggMoves.get(base) != null && this.loveInterest != null) {
         for (String move : Pokemon.eggMoves.get(base)) {
            for (String attack : this.loveInterest.attacks) {
               if (move.equals(attack)) {
                  boolean foundMove = false;

                  for (int j = 0; j < pokemonEgg.attacks.length; j++) {
                     if (this.attacks[j] != null && this.attacks[j].equalsIgnoreCase(attack)) {
                        foundMove = true;
                        break;
                     }
                  }

                  if (!foundMove) {
                     pokemonEgg.attacks[currIndex] = move;
                     currIndex = (currIndex + 1) % 4;
                  }
               }
            }
         }
      }

      pokemonEgg.mapTiles = this.mapTiles;
      pokemonEgg.interiorIndex = this.interiorIndex;
      pokemonEgg.position = eggPos.cpy();
      pokemonEgg.previousOwner = this.previousOwner;
      pokemonEgg.lanOwnerId = this.lanOwnerId;
      pokemonEgg.lanOwnerName = this.lanOwnerName;
      game.map.indexPokemon(pokemonEgg.position, pokemonEgg);
      game.insertAction(pokemonEgg.new Standing());
   }

   void spookify() {
      this.isGhost = true;
      this.nickname = "ghost";
      this.sprite = Specie.spriteGhost;
      this.backSprite = null;
      this.introAnim = Specie.introAnimGhost;
   }

   public Pokemon revealGhost() {
      if (this.types.contains("GHOST")) {
         this.isGhost = false;
      }

      this.loadOverworldSprites();
      this.nickname = this.specie.name;
      float x = this.sprite.getX();
      float y = this.sprite.getY();
      if (this.isShiny) {
         this.sprite = this.specie.spriteShiny;
         this.backSprite = this.specie.backSpriteShiny;
         this.introAnim = this.specie.introAnimShiny;
      } else {
         this.sprite = this.specie.sprite;
         this.backSprite = this.specie.backSprite;
         this.introAnim = this.specie.introAnim;
      }

      this.sprite.setPosition(x, y);
      return this;
   }

   public void removeDrawActions(Game game) {
      game.actionStack.remove(this.standingAction);
      game.actionStack.remove(this.drawUpper);
      game.actionStack.remove(this.drawLower);
      game.map.onscreenPokemon.remove(this);
      this.drawUpper = null;
      this.drawLower = null;
   }

   void evolveTo(String targetName) {
      int prevMaxHp = this.maxStats.get("hp");
      int prevCurrentHp = this.currentStats.get("hp");
      this.updateSpecieInfo(targetName, this.getNickname());
      this.currentStats.put("hp", prevCurrentHp + (this.maxStats.get("hp") - prevMaxHp));
      this.status = null;
   }

   void initHabitatValues() {
      this.harvestables = this.specie.harvestables;
      this.habitats = this.parseHabitats(this.specie.habitats);
      this.harvestTimerMax = this.specie.harvestTimerMax;
   }

   private List<String[]> parseHabitats(List<String> originals) {
      List<String[]> habitats = new ArrayList<>();

      for (int i = 0; i < originals.size(); i++) {
         String original = originals.get(i);
         String[] names = original.split("\\|");
         habitats.add(names);
      }

      return habitats;
   }

   boolean gen2ApplyStatStage(String stat, int stage) {
      if (!stat.equals("all")) {
         return this.gen2ApplyStatStage(stat, stage, false);
      }

      boolean succeeded = false;
      String[] statNames = new String[]{"attack", "defense", "specialAtk", "specialDef", "speed"};

      for (String statName : statNames) {
         succeeded = succeeded || this.gen2ApplyStatStage(statName, stage, false);
      }

      return succeeded;
   }

   boolean gen2ApplyStatStage(String stat, int stage, boolean override) {
      int newStage = this.statStages.get(stat) + stage;
      if (override || newStage >= -6 && newStage <= 6) {
         this.statStages.put(stat, newStage);
         if (!stat.equals("accuracy") && !stat.equals("evasion")) {
            float multiplier = (float)Math.max(2, 2 + newStage) / Math.max(2, 2 - newStage);
            int currStat = this.maxStats.get(stat);
            if (this.status != null) {
               if (stat.equals("speed") && this.status.equals("paralyze")) {
                  currStat /= 4;
               } else if (stat.equals("attack") && this.status.equals("burn")) {
                  currStat /= 4;
               }
            }

            int newStat = (int)(multiplier * currStat);
            if (newStat < 1) {
               newStat = 1;
            }

            if (newStat > 999) {
               newStat = 999;
            }

            this.currentStats.put(stat, newStat);
            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   int calcExpForLevel(int level) {
      if (this.growthRateGroup.contains("FAST")) {
         return 4 * level * level * level / 5;
      }

      if (this.growthRateGroup.contains("MEDIUM_FAST")) {
         return level * level * level;
      }

      if (this.growthRateGroup.contains("MEDIUM_SLOW")) {
         return 6 * level * level * level / 5 - 15 * level * level + 100 * level - 140;
      }

      if (this.growthRateGroup.contains("SLOW")) {
         return 5 * level * level * level / 4;
      }

      if (this.growthRateGroup.contains("FLUCTUATING")) {
         if (level < 15) {
            return (int)(level * level * level * ((Math.floor((level + 1.0F) / 3.0F) + 24.0) / 50.0));
         } else {
            return level < 36
               ? (int)(level * level * level * ((level + 14.0F) / 50.0F))
               : (int)(level * level * level * ((Math.floor(level / 2.0F) + 32.0) / 50.0));
         }
      } else if (this.growthRateGroup.contains("ERRATIC")) {
         if (level < 50) {
            return (int)(level * level * level * (100.0F - level) / 50.0F);
         } else if (level < 68) {
            return (int)(level * level * level * (150.0F - level) / 100.0F);
         } else {
            return level < 98
               ? (int)(level * level * level * Math.floor((1911.0F - 10.0F * level) / 3.0F) / 500.0)
               : (int)(level * level * level * (160.0F - level) / 100.0F);
         }
      } else {
         System.out.println("Error: invalid growth group for " + this.specie.name + ", group: " + this.growthRateGroup);
         return level * level * level;
      }
   }

   void getCurrentAttacks() {
      int i = 0;

      for (Integer level : this.learnSet.keySet()) {
         for (String attack : this.learnSet.get(level)) {
            if (level <= this.level) {
               boolean foundMove = false;

               for (int j = 0; j < this.attacks.length; j++) {
                  if (this.attacks[j] != null && this.attacks[j].equalsIgnoreCase(attack)) {
                     foundMove = true;
                     break;
                  }
               }

               if (!foundMove) {
                  this.attacks[i] = attack;
                  if (++i >= this.attacks.length) {
                     i = 0;
                  }
               }
            }
         }
      }
   }

   void loadOverworldSprites() {
      if (this.isEgg) {
         this.movingSprites = this.specie.movingSpritesEgg;
         this.altMovingSprites = this.specie.altMovingSpritesEgg;
         this.standingSprites = this.specie.standingSpritesEgg;
         this.avatarSprites = this.specie.avatarSpritesEgg;
      } else {
         this.spriteOffsetY = this.specie.spriteOffsetY;
         this.movingSprites = this.specie.movingSprites;
         this.altMovingSprites = this.specie.altMovingSprites;
         this.standingSprites = this.specie.standingSprites;
         this.avatarSprites = this.specie.avatarSprites;
         this.surfSprites = this.specie.surfSprites;
         this.surfMovingSprites = this.specie.surfMovingSprites;
      }
   }

   void resetStatStages() {
      this.statStages.put("attack", 0);
      this.statStages.put("defense", 0);
      this.statStages.put("specialAtk", 0);
      this.statStages.put("specialDef", 0);
      this.statStages.put("speed", 0);
      this.statStages.put("accuracy", 0);
      this.statStages.put("evasion", 0);

      for (String stat : this.maxStats.keySet()) {
         if (!stat.equals("hp")) {
            this.currentStats.put(stat, this.maxStats.get(stat));
         }
      }

      if (this.status != null) {
         if (this.status.equals("paralyze")) {
            this.currentStats.put("speed", this.currentStats.get("speed") / 4);
         } else if (this.status.equals("burn")) {
            this.currentStats.put("attack", this.currentStats.get("attack") / 4);
         }
      }
   }

   static {
      FileHandle file = Gdx.files.internal("pokemon/attacks_implemented.txt");
      Reader reader = file.reader();
      BufferedReader br = new BufferedReader(reader);

      String line;
      try {
         while ((line = br.readLine()) != null) {
            attacksImplemented.add(line);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

      onlySwim = new ArrayList<>();
      onlySwim.add("magikarp");
      onlySwim.add("gyarados");
      onlySwim.add("remoraid");
      onlySwim.add("chinchou");
      onlySwim.add("lanturn");
      onlySwim.add("goldeen");
      onlySwim.add("seaking");
      onlySwim.add("horsea");
      onlySwim.add("seadra");
      onlySwim.add("kingdra");
      onlySwim.add("feebas");
      onlySwim.add("lapras");
      onlySwim.add("carvanha");
      onlySwim.add("sharpedo");
      aggroAnyway = new ArrayList<>();
      aggroAnyway.add("poochyena");
      aggroAnyway.add("sneasel");
      aggroAnyway.add("skarmory");
      aggroAnyway.add("pinsir");
      aggroAnyway.add("tauros");
      aggroAnyway.add("duraludon");
      dontAggro = new ArrayList<>();
      dontAggro.add("bellossom");
      dontAggro.add("gardevoir");
      dontAggro.add("jumpluff");
      dontAggro.add("marill");
      dontAggro.add("chansey");
      dontAggro.add("blissey");
      dontAggro.add("pikachu");
      skittishMons = new ArrayList<>();
      skittishMons.add("ekans");
      skittishMons.add("pidgey");
      skittishMons.add("spearow");
      skittishMons.add("rattata");
      skittishMons.add("chansey");
      textures = new HashMap<>();
      baseSpecies = new HashMap<>();
      eggMoves = new HashMap<>();

      try {
         for (String path : new String[]{"credited/", ""}) {
            FileHandle filex = Gdx.files.internal("pokemon/" + path + "evos_attacks.asm");
            Reader readerx = filex.reader();
            BufferedReader brx = new BufferedReader(readerx);
            String currMon = null;

            String linex;
            while ((linex = brx.readLine()) != null) {
               if (linex.contains("EvosAttacks:")) {
                  currMon = linex.split("EvosAttacks:")[0].toLowerCase(Locale.ROOT);
               } else if (currMon != null && !linex.contains(";")) {
                  if (linex.contains("EVOLVE")) {
                     String[] vals = linex.split(", ");
                     String baseMon = baseSpecies.get(currMon);
                     if (baseMon == null) {
                        baseMon = currMon;
                     }

                     baseSpecies.put(vals[2].toLowerCase(Locale.ROOT), baseMon);
                  } else if (!linex.contains("\t")) {
                     String baseMon = baseSpecies.get(currMon);
                     if (baseMon == null) {
                        baseMon = baseSpecies.put(currMon, currMon);
                     }

                     currMon = null;
                  }
               }
            }

            readerx.close();
            baseSpecies.put("darmanitanzen", "darumaka");
            baseSpecies.put("aexeggutor", "exeggcute");
            baseSpecies.put("araichu", "pichu");
            baseSpecies.put("mantine", "mantyke");
            baseSpecies.put("combee_female", "combee");
            if (Game.fairyTypeEnabled) {
               baseSpecies.put("azurill", "azurill");
               baseSpecies.put("marill", "azurill");
               baseSpecies.put("azumarill", "azurill");
            }

            baseSpecies.put("amarowak", "cubone");
            filex = Gdx.files.internal("pokemon/" + path + "egg_moves.asm");
            readerx = filex.reader();
            brx = new BufferedReader(readerx);
            currMon = null;

            while ((linex = brx.readLine()) != null) {
               if (linex.contains("EggMoves:")) {
                  currMon = linex.split("EggMoves:")[0].toLowerCase(Locale.ROOT);
                  eggMoves.put(currMon, new ArrayList<>());
               } else if (currMon != null && !linex.contains(";")) {
                  if (!linex.contains("\t")) {
                     currMon = null;
                  } else {
                     eggMoves.get(currMon).add(linex.split("db ")[1].trim().toLowerCase(Locale.ROOT).replace("_", " "));
                  }
               }
            }

            readerx.close();
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      POSITIONS = new Vector2[]{
         new Vector2(-16.0F, -16.0F),
         new Vector2(0.0F, -16.0F),
         new Vector2(16.0F, -16.0F),
         new Vector2(-16.0F, 0.0F),
         new Vector2(0.0F, 0.0F),
         new Vector2(16.0F, 0.0F),
         new Vector2(-16.0F, 16.0F),
         new Vector2(0.0F, 16.0F),
         new Vector2(16.0F, 16.0F)
      };
      weights = new HashMap<>();

      try {
         file = Gdx.files.internal("pokemon/pokemon_weights_kg.txt");
         reader = file.reader();
         br = new BufferedReader(reader);

         while ((line = br.readLine()) != null) {
            String[] attrs = line.split("\t");
            String key = String.format(Locale.ROOT, "%03d", Integer.valueOf(attrs[0]));
            if (!weights.containsKey(key)) {
               weights.put(key, Float.valueOf(attrs[1]));
            }
         }

         reader.close();
      } catch (IOException e) {
         e.printStackTrace();
      }

      layEggTimerMax = 7200;
      rand = new Random();
      friendTimerMax = 960;
      currStagger = 0;
      maxStagger = 96;
      FRIEND_OFFSETS = new Vector2[]{new Vector2(-16.0F, 0.0F), new Vector2(16.0F, 0.0F), new Vector2(0.0F, -16.0F), new Vector2(0.0F, 16.0F)};
   }

   public class AddToInventory extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      Vector2 position;

      public AddToInventory(Vector2 position, Action nextAction) {
         super();
         this.position = position;
         this.nextAction = nextAction;
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void firstStep(Game game) {
         if (game.coop != null && (!game.coop.isWorldCreature(Pokemon.this) || game.player.pokemon.contains(Pokemon.this))) {
            game.actionStack.remove(this);
            game.playerCanMove = true;
            game.insertAction(this.nextAction);
            return;
         }
         if (Pokemon.this.type == Player.Type.REMOTE) {
            game.client.sendTCP(new Network.DropPokemon(game.player.network.id, Pokemon.this.position));
         }

         if (game.player.pokemon.size() >= 6) {
            game.actionStack.remove(this);
            game.insertAction(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new PlayMusic("error1", new DisplayText(game, "Not enough room in your party!", null, null, new WaitFrames(game, 6, this.nextAction)))
                  )
               )
            );
         } else {
            game.player.pokemon.add(Pokemon.this);
            game.map.removePokemon(Pokemon.this);
            Pokemon.this.removeDrawActions(game);
            Action newAction;
            if (Pokemon.this.isEgg && Pokemon.this.previousOwner != game.player) {
               newAction = new DisplayText(game, game.player.name + " received an EGG.", "Berry_Get.ogg", null, null);
               boolean playedSound = false;
               Vector2 startPos = Pokemon.this.position.cpy().add(-160.0F, -160.0F);
               startPos.x = (int)startPos.x - (int)startPos.x % 16;
               startPos.y = (int)startPos.y - (int)startPos.y % 16;
               Vector2 endPos = Pokemon.this.position.cpy().add(160.0F, 160.0F);
               endPos.x = (int)endPos.x - (int)endPos.x % 16;
               endPos.y = (int)endPos.y - (int)endPos.y % 16;
               int offSet = 0;
               Vector2 currPos = new Vector2(startPos.x, startPos.y);

               while (currPos.y < endPos.y) {
                  Pokemon pokemon = game.map.pokemon.get(currPos);
                  currPos.x += 16.0F;
                  if (currPos.x > endPos.x) {
                     currPos.x = startPos.x;
                     currPos.y += 16.0F;
                  }

                  if (pokemon != null && !pokemon.isEgg && pokemon.previousOwner != game.player) {
                     boolean sameBaseSpecie = pokemon.baseSpecie().equals(Pokemon.this.baseSpecie());
                     boolean shareEggGroup = false;

                     for (String group1 : pokemon.eggGroups) {
                        for (String group2 : Pokemon.this.eggGroups) {
                           if (group1.equals(group2)) {
                              shareEggGroup = true;
                              break;
                           }
                        }
                     }

                     if (sameBaseSpecie || shareEggGroup) {
                        if (!playedSound) {
                           game.insertAction(new PlayMusic("ledge2", null));
                           playedSound = true;
                        }

                        game.insertAction(
                           new SetField(
                              pokemon,
                              "canMove",
                              false,
                              pokemon.new Emote("!", new SetField(pokemon, "canMove", true, new SetField(pokemon, "aggroPlayer", true, null)))
                           )
                        );
                        if (pokemon.standingAction != null && pokemon.standingAction instanceof Pokemon.Standing) {
                           ((Pokemon.Standing)pokemon.standingAction).aggroTimer = -2 * offSet;
                        }

                        offSet++;
                     }
                  }
               }
            } else {
               newAction = new PlayMusic("seed1", null);
            }

            if (game.player.pokemon.size() >= 6 && !game.player.displayedMaxPartyText) {
               game.player.displayedMaxPartyText = true;
               newAction.append(
                  new DisplayText(game, "Your party is full! You will need to DROP some of them in order to catch more.", null, false, true, null)
               );
            }

            Pokemon.this.previousOwner = game.player;
            Pokemon.this.aggroPlayer = false;
            Pokemon.this.isRunning = false;
            if (Pokemon.this.loveInterest != null) {
               Pokemon.this.loveInterest.loveInterest = null;
               Pokemon.this.loveInterest = null;
            }

            if (Pokemon.this.friend != null) {
               Pokemon.this.friend.friend = null;
               Pokemon.this.friend = null;
            }

            newAction.append(this.nextAction);
            game.actionStack.remove(this);
            game.insertAction(newAction);
         }
      }

      @Override
      public void step(Game game) {
      }
   }

   public class Burrowed extends Action {
      public Action.Layer layer = Action.Layer.map_119;
      Sprite whirlpoolSprite;
      int whirlTimer = 0;
      boolean popOut = false;
      int jumpTimer = 0;
      int offsetY = 0;
      Sprite spritePart;
      Sprite sandSprite;
      Sprite trapinchSprite;

      public Burrowed() {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("whirlpool_desert2.png"));
         this.whirlpoolSprite = new Sprite(text, 0, 0, 16, 16);
         text = TextureCache.get(Gdx.files.internal("grass_over_sheet3.png"));
         this.sandSprite = new Sprite(text, 0, 0, 16, 16);
         text = TextureCache.get(Gdx.files.internal("trapinch_ow1.png"));
         this.trapinchSprite = new Sprite(text, 0, 0, 16, 16);
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
         Pokemon.this.dirFacing = "right";
         Pokemon.this.currOwSprite = this.trapinchSprite;
         Pokemon.this.drawUpper = new Pokemon.Burrowed.DrawUpper();
         Pokemon.this.standingAction = this;
         game.map.burrowedPokemon.put(Pokemon.this.position.cpy(), Pokemon.this);
         game.actionStack.remove(this);
      }

      @Override
      public void step(Game game) {
      }

      class DrawUpper extends Pokemon.DrawUpper {
         public Action.Layer layer = Action.Layer.map_114;
         Sprite spritePart = new Sprite();

         @Override
         public String getCamera() {
            return "map";
         }

         @Override
         public Action.Layer getLayer() {
            return this.layer;
         }

         public DrawUpper() {
            Pokemon.this.drawUpper = this;
         }

         @Override
         public void firstStep(Game game) {
         }

         @Override
         public void step(Game game) {
         }

         @Override
         public void draw(Game game) {
            if (Pokemon.this.mapTiles == game.map.tiles && game.player.position.equals(Pokemon.this.position) && !game.player.currFieldMove.equals("FLY")) {
               Burrowed.this.popOut = true;
            }

            if (Burrowed.this.popOut) {
               if (Burrowed.this.jumpTimer < 50) {
                  Burrowed.this.jumpTimer++;
                  if (Burrowed.this.jumpTimer % 8 == 0) {
                     game.insertAction(new PlayMusic("move_object", 0.6F, true, null));
                  }
               } else if (Burrowed.this.jumpTimer < 53) {
                  Burrowed.this.jumpTimer++;
                  Burrowed.this.offsetY += 4;
                  if (Burrowed.this.jumpTimer % 8 < 4) {
                     Burrowed.this.trapinchSprite.setRegion(0, 0, 16, 16);
                  } else {
                     Burrowed.this.trapinchSprite.setRegion(16, 0, 16, 16);
                  }
               } else if (Burrowed.this.jumpTimer < 66) {
                  Burrowed.this.jumpTimer++;
               } else if (!game.player.position.equals(Pokemon.this.position)) {
                  Burrowed.this.jumpTimer++;
                  if (Burrowed.this.jumpTimer >= 120 && Burrowed.this.jumpTimer < 156) {
                     if (Burrowed.this.jumpTimer % 8 < 4) {
                        Burrowed.this.trapinchSprite.setRegion(0, 0, 16, 16);
                     } else {
                        Burrowed.this.trapinchSprite.setRegion(16, 0, 16, 16);
                     }

                     if (Burrowed.this.jumpTimer % 8 == 0) {
                        game.insertAction(new PlayMusic("move_object", 0.6F, true, null));
                     }

                     if (Burrowed.this.jumpTimer >= 141) {
                        Burrowed.this.offsetY--;
                     }
                  }

                  if (Burrowed.this.jumpTimer >= 152) {
                     Burrowed.this.popOut = false;
                     Burrowed.this.jumpTimer = 0;
                  }
               }

               if (Burrowed.this.jumpTimer >= 63 && game.player.position.equals(Pokemon.this.position) && game.playerCanMove) {
                  game.playerCanMove = false;
                  game.musicController.startBattle = "wild";
                  game.battle.oppPokemon = Pokemon.this;
                  game.player.setCurrPokemon();
                  game.insertAction(Battle.getIntroAction(game));
               }

               if (Burrowed.this.jumpTimer == 34) {
                  game.insertAction(new PlayMusic(Pokemon.this, null));
               }
            }

            if (Burrowed.this.jumpTimer > 0) {
               if (Burrowed.this.whirlTimer == 0) {
                  Burrowed.this.whirlpoolSprite.setRegion(0, 0, 16, 16);
               } else if (Burrowed.this.whirlTimer == 40) {
                  Burrowed.this.whirlpoolSprite.setRegion(16, 0, 16, 16);
               }

               game.mapBatch.draw(Burrowed.this.whirlpoolSprite, Pokemon.this.position.x, Pokemon.this.position.y);
            }

            Burrowed.this.whirlTimer++;
            if (Burrowed.this.whirlTimer >= 80) {
               Burrowed.this.whirlTimer = 0;
            }

            this.spritePart.set(Pokemon.this.currOwSprite);
            this.spritePart.setRegionHeight(Burrowed.this.offsetY);
            game.mapBatch.draw(this.spritePart, Pokemon.this.position.x, Pokemon.this.position.y + 6.0F);
            if (Burrowed.this.jumpTimer >= 50 && Burrowed.this.jumpTimer < 66) {
               if (Burrowed.this.jumpTimer == 50) {
                  Burrowed.this.sandSprite.setRegion(0, 0, 16, 16);
               } else if (Burrowed.this.jumpTimer == 58) {
                  Burrowed.this.sandSprite.setRegion(16, 0, 16, 16);
               }

               game.mapBatch.draw(Burrowed.this.sandSprite, Pokemon.this.position.x, Pokemon.this.position.y + 6.0F);
            }
         }
      }
   }

   public class Cacturnt extends Action {
      public Action.Layer layer = Action.Layer.map_107;
      public int aggroTimer = 0;
      public boolean alternate = false;
      public int campfireDespawn = 0;
      Sprite tornadoSprite;
      private Vector2 newPos = new Vector2();

      public Cacturnt(Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("tornado_sheet1.png"));
         this.tornadoSprite = new Sprite(text, 0, 0, 16, 16);
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
         game.map.indexPokemon(Pokemon.this.position.cpy(), Pokemon.this);
         Pokemon.this.standingAction = this;
         Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
         game.insertAction(Pokemon.this.new DrawLower());
         game.insertAction(Pokemon.this.new DrawUpper());
      }

      @Override
      public void step(Game game) {
         if (game.playerCanMove) {
            if (this.campfireDespawn > 0) {
               if (this.campfireDespawn == 60) {
                  Pokemon.this.dirFacing = "down";
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                  game.insertAction(Pokemon.this.new CactusSpawn(null, null));
               }

               if (this.campfireDespawn < 40) {
                  if (this.campfireDespawn % 4 == 3) {
                     this.tornadoSprite.setRegion(0, 0, 18, 16);
                  } else if (this.campfireDespawn % 4 == 1) {
                     this.tornadoSprite.setRegion(18, 0, 18, 16);
                  }

                  game.mapBatch.draw(this.tornadoSprite, Pokemon.this.position.x - 2.0F, Pokemon.this.position.y + 2.0F);
               }

               if (this.campfireDespawn <= 1) {
                  game.actionStack.remove(this);
                  game.map.pokemon.remove(Pokemon.this.position);
                  Pokemon.this.removeDrawActions(game);
                  boolean foundCacturnt = false;

                  for (Action action : game.actionStack) {
                     if (action instanceof Pokemon.Cacturnt) {
                        foundCacturnt = true;
                        break;
                     }
                  }

                  if (!foundCacturnt) {
                     game.musicController.nightAlert = false;
                     game.musicController.resumeOverworldMusic = true;
                  }
               }

               this.campfireDespawn--;
            } else if (game.map.timeOfDay.equals("day")) {
               this.campfireDespawn = 60;
            } else {
               if (this.aggroTimer > 240) {
                  this.aggroTimer = 0;
               }

               this.aggroTimer++;
               if (this.aggroTimer == 1) {
                  game.insertAction(Pokemon.this.new Emote("skull", null));
               }

               float dst2 = Pokemon.this.position.dst2(game.player.position);
               if (this.aggroTimer == 1) {
                  game.insertAction(new PlayMusic(Pokemon.this, true, null));
               }

               if (this.aggroTimer < 4) {
                  if (this.aggroTimer == 0) {
                     game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                  }

                  Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 8) {
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 12) {
                  if (this.aggroTimer == 8) {
                     game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                  }

                  Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 16) {
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 20) {
                  if (this.aggroTimer == 16) {
                     game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                  }

                  Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 24) {
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 28) {
                  if (this.aggroTimer == 24) {
                     game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                  }

                  Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
               } else if (this.aggroTimer < 32) {
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
               } else {
                  ArrayList<String> preferredMoves = new ArrayList<>();
                  float dx = Pokemon.this.position.x - game.player.position.x;
                  float dy = Pokemon.this.position.y - game.player.position.y;
                  if (dx < dy) {
                     if (game.player.position.y < Pokemon.this.position.y) {
                        preferredMoves.add("down");
                        preferredMoves.add("right");
                     } else {
                        preferredMoves.add("right");
                        preferredMoves.add("up");
                     }
                  } else if (game.player.position.x < Pokemon.this.position.x) {
                     preferredMoves.add("left");
                     preferredMoves.add("down");
                  } else {
                     preferredMoves.add("up");
                     preferredMoves.add("left");
                  }

                  if (dst2 < 64.0F && this.aggroTimer > 32 && !game.player.currFieldMove.equals("FLY")) {
                     if (game.player.isSleeping) {
                        game.player.isSleeping = false;
                        if (game.player.sleepingDir == null) {
                           game.player.drawSleepingBag = false;
                           game.player.acceptInput = true;
                        } else {
                           game.player.sleepingDir = null;
                        }
                     }

                     if (game.player.acceptInput) {
                        game.playerCanMove = false;
                        this.aggroTimer = 0;
                        game.musicController.startBattle = "wild";
                        game.battle.oppPokemon = Pokemon.this;
                        game.player.setCurrPokemon();
                        game.insertAction(Battle.getIntroAction(game));
                        return;
                     }
                  }

                  for (String move : preferredMoves) {
                     this.newPos = Pokemon.this.facingPos(this.newPos, move);
                     Tile facingTile = Pokemon.this.mapTiles.get(this.newPos);
                     Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                     boolean isLedge = facingTile != null && facingTile.isLedge
                        || currTile != null && currTile.isLedge && currTile.ledgeDir().equals("up") && move.equals("up");
                     if (!facingTile.isSolid
                        && !isLedge
                        && !facingTile.name.contains("door")
                        && !facingTile.nameUpper.contains("gate")
                        && !facingTile.nameUpper.contains("door")
                        && !game.map.pokemon.containsKey(this.newPos)) {
                        Vector2 startPos = Pokemon.this.position.cpy().add(-80.0F, -80.0F);
                        startPos.x = (int)startPos.x - (int)startPos.x % 16;
                        startPos.y = (int)startPos.y - (int)startPos.y % 16;
                        Vector2 endPos = Pokemon.this.position.cpy().add(80.0F, 80.0F);
                        endPos.x = (int)endPos.x - (int)endPos.x % 16;
                        endPos.y = (int)endPos.y - (int)endPos.y % 16;
                        Vector2 currPos = new Vector2(startPos.x, startPos.y);

                        while (currPos.y < endPos.y) {
                           Tile tile = game.map.tiles.get(currPos);
                           currPos.x += 16.0F;
                           if (currPos.x > endPos.x) {
                              currPos.x = startPos.x;
                              currPos.y += 16.0F;
                           }

                           if (tile != null) {
                              if (tile.nameUpper.contains("campfire")) {
                                 this.campfireDespawn = 200;
                                 return;
                              }

                              if (tile.items != null && tile.items.containsKey("torch") && Pokemon.this.position.dst2(tile.position) < 1024.0F) {
                                 this.campfireDespawn = 200;
                                 return;
                              }

                              if (game.map.pokemon.containsKey(tile.position)) {
                                 Pokemon pokemon = game.map.pokemon.get(tile.position);
                                 if (pokemon.hms.contains("FLASH")) {
                                    this.campfireDespawn = 200;
                                    return;
                                 }
                              } else if (game.player.hmPokemon != null
                                 && Pokemon.this.position.dst2(game.player.position) < 4096.0F
                                 && game.player.hmPokemon.hms.contains("FLASH")) {
                                 this.campfireDespawn = 200;
                                 return;
                              }
                           }
                        }

                        if (game.map.pokemon.containsKey(Pokemon.this.position) && game.map.pokemon.get(Pokemon.this.position) == Pokemon.this) {
                           game.map.pokemon.remove(Pokemon.this.position);
                        }

                        game.map.indexPokemon(this.newPos.cpy(), Pokemon.this);
                        Pokemon.this.dirFacing = move;
                        game.actionStack.remove(this);
                        Action action = Pokemon.this.new Moving(1, 1.5F, this.alternate, this);
                        this.alternate = !this.alternate;
                        game.insertAction(action);
                        Pokemon.this.standingAction = action;
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   public class CactusSpawn extends Action {
      public Action.Layer layer = Action.Layer.map_116;
      Tile tile;
      int timer = 0;

      public CactusSpawn(Tile tile, Action nextAction) {
         super();
         this.tile = tile;
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
      }

      @Override
      public void step(Game game) {
         if (this.timer % 4 < 2) {
            Pokemon.this.drawThisFrame = false;
         } else {
            Pokemon.this.drawThisFrame = true;
         }

         if (this.timer > 60) {
            Pokemon.this.drawThisFrame = true;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   public class DrawLower extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      Sprite spritePart = new Sprite();
      public boolean isEgg = false;
      public boolean following = false;
      int timer;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public DrawLower() {
         super();
         Pokemon.currStagger = Math.floorMod(Pokemon.currStagger + 1, Pokemon.maxStagger);
         this.timer = Pokemon.currStagger;
         Pokemon.this.drawLower = this;
         Game game = Game.staticGame;
         if (game.map != null && !game.map.onscreenPokemon.contains(Pokemon.this)) {
            game.map.refreshOnscreenPokemon = true;
         }
      }

      public DrawLower(boolean following) {
         super();
         Pokemon.currStagger = Math.floorMod(Pokemon.currStagger + 1, Pokemon.maxStagger);
         this.timer = Pokemon.currStagger;
         Pokemon.this.drawLower = this;
         this.following = following;
      }

      @Override
      public void firstStep(Game game) {
         this.isEgg = Pokemon.this.isEgg;
         game.map.upkeepTimers.pokemonBuckets[this.timer].add(Pokemon.this);
         game.actionStack.remove(this);
      }

      @Override
      public void step(Game game) {
         if (game.coop != null && game.coop.reserved(Pokemon.this)) return;
         if (this.isEgg || Pokemon.this.isEgg) return;

         boolean isBreedingAuth = game.coop == null || game.coop.isAuthorityForBreeding(Pokemon.this);
         if (isBreedingAuth) {
            Pokemon.this.habitatCheckCounter++;
            if (Pokemon.this.habitatCheckCounter >= 60 || !Pokemon.this.inHabitat || Pokemon.this.loveInterest == null) {
               Pokemon.this.habitatCheckCounter = 0;
               Pokemon.this.checkHabitat(game);
            }
         }

         if (Pokemon.this.hasItem == null && !this.isEgg) {
            if (game.coop == null || !game.coop.isGuest() || this.following || !game.coop.isWorldCreature(Pokemon.this)) {
               if (Pokemon.this.inHabitat) {
                  Pokemon.this.harvestTimer = Pokemon.this.harvestTimer + Pokemon.maxStagger;
                  if (Pokemon.this.inShelter) {
                     Pokemon.this.harvestTimer = Pokemon.this.harvestTimer + Pokemon.maxStagger;
                  }
               }

               if (Pokemon.this.harvestTimer >= Pokemon.this.harvestTimerMax) {
                  Pokemon.this.hasItem = Pokemon.this.harvestables.get(game.map.rand.nextInt(Pokemon.this.harvestables.size()));
               }
            }
         }

         if (isBreedingAuth
            && Pokemon.this.loveInterest != null
            && Pokemon.this.inHabitat
            && Pokemon.this.nearbyEggs < 3
            && (Pokemon.this.gender.equals("female") || (Pokemon.this.eggGroups[0].equals("EGG_DITTO") && !Pokemon.this.loveInterest.gender.equals("female")))) {
            if (Pokemon.this.layEggTimer > 0) {
               Pokemon.this.layEggTimer = Pokemon.this.layEggTimer - Pokemon.maxStagger;
            }

            if (Pokemon.this.layEggTimer <= 0) {
               Vector2[] adj = new Vector2[]{
                  new Vector2(Pokemon.this.position.x, Pokemon.this.position.y - 16.0F),
                  new Vector2(Pokemon.this.position.x, Pokemon.this.position.y + 16.0F),
                  new Vector2(Pokemon.this.position.x - 16.0F, Pokemon.this.position.y),
                  new Vector2(Pokemon.this.position.x + 16.0F, Pokemon.this.position.y)
               };
               for (Vector2 pos : adj) {
                  Tile tile = Pokemon.this.mapTiles.get(pos);
                  if (tile != null && !tile.isSolid && !tile.isLedge
                     && !tile.name.contains("door") && !tile.nameUpper.contains("gate") && !tile.nameUpper.contains("door")
                     && !game.map.pokemon.containsKey(pos) && (game.player == null || !game.player.position.equals(pos))) {
                     Pokemon.this.layEgg(game, pos);
                     break;
                  }
               }
            }
         }

         if (Pokemon.this.friend != null && Pokemon.this.friendTimer > 0) {
            Pokemon.this.friendTimer = Pokemon.this.friendTimer - Pokemon.maxStagger;
         }
      }

      public void draw(Game game) {
         Vector2 render = game.coop == null ? Pokemon.this.position : game.coop.creatureRenderPosition(Pokemon.this);
         if (Pokemon.this.drawThisFrame || this.following) {
            if (game.map.tiles == Pokemon.this.mapTiles) {
               if (!Pokemon.this.inWater) {
                  this.spritePart.set(Pokemon.this.currOwSprite);
                  this.spritePart.setRegionY(Pokemon.this.currOwSprite.getRegionY() + (Pokemon.this.currOwSprite.getRegionHeight() - 8));
                  this.spritePart.setRegionHeight(8);
                  game.mapBatch.draw(this.spritePart, render.x, render.y + 4.0F);
               }
            }
         }
      }
   }

   public class DrawUpper extends Action {
      public Action.Layer layer = Action.Layer.map_116;
      Sprite spritePart = new Sprite();
      public boolean following = false;
      public int floatTimer = 0;
      public int floatOffset = 0;
      public int floatOffset2 = 0;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public DrawUpper() {
         super();
         Pokemon.this.drawUpper = this;
      }

      public DrawUpper(boolean following) {
         super();
         this.following = following;
         Pokemon.this.drawUpper = this;
      }

      @Override
      public void firstStep(Game game) {
         if (Pokemon.this.specie.name.equals("lapras")) {
            this.floatOffset2 = 5;
         } else if (Pokemon.this.specie.name.equals("anorith")) {
            this.floatOffset2 = 2;
         } else if (Pokemon.this.specie.name.equals("araichu")) {
            this.floatOffset2 = 7;
         } else if (Pokemon.this.specie.name.equals("mantine")) {
            this.floatOffset2 = 4;
         } else if (Pokemon.this.specie.name.equals("sharpedo")) {
            this.floatOffset2 = 2;
         }

         game.actionStack.remove(this);
      }

      @Override
      public void step(Game game) {
         this.draw(game);
      }

      public void draw(Game game) {
         Vector2 render = game.coop == null ? Pokemon.this.position : game.coop.creatureRenderPosition(Pokemon.this);
         if (Pokemon.this.drawThisFrame || this.following) {
            if (game.map.tiles == Pokemon.this.mapTiles) {
               this.spritePart.set(Pokemon.this.currOwSprite);
               this.spritePart.setRegionY(Pokemon.this.currOwSprite.getRegionY());
               if (Pokemon.this.inWater) {
                  this.spritePart.setRegionHeight(Pokemon.this.currOwSprite.getRegionHeight() - 8 + this.floatOffset + this.floatOffset2);
                  this.floatTimer++;
                  if (this.floatTimer > 80) {
                     this.floatTimer = 0;
                  }

                  if (this.floatTimer % 80 == 0) {
                     this.floatOffset = 0;
                  } else if (this.floatTimer % 80 == 40) {
                     this.floatOffset = 1;
                  }

                  game.mapBatch.draw(this.spritePart, render.x, render.y + 8.0F);
               } else {
                  this.spritePart.setRegionHeight(Pokemon.this.currOwSprite.getRegionHeight() - 8);
                  game.mapBatch.draw(this.spritePart, render.x, render.y + 12.0F);
               }
            }
         }
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
         if (game.map.tiles == Pokemon.this.mapTiles) {
            game.mapBatch.draw(this.sprite, Pokemon.this.position.x, Pokemon.this.position.y + 4.0F + 16.0F);
         }

         if (this.timer >= 60) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   public class Follow extends Action {
      public Action.Layer layer = Action.Layer.map_151;
      boolean alternate = true;
      boolean onPlayer = false;
      Player player;
      Tile targetTile;
      private Vector2 pos = new Vector2();

      public Follow(Player player) {
         super();
         this.player = player;
      }

      @Override
      public void firstStep(Game game) {
         Pokemon.this.standingAction = this;
         Pokemon.this.dirFacing = this.player.dirFacing;
         String oppDir = Player.oppDirs.get(this.player.dirFacing);
         Vector2 pos = this.player.facingPos(oppDir);
         if (game.map.tiles.get(pos) != null && !game.map.tiles.get(pos).isSolid && !game.map.tiles.get(pos).isLedge) {
            Pokemon.this.position = pos;
         } else {
            Pokemon.this.position = this.player.position.cpy();
            this.onPlayer = true;
         }

         Pokemon.this.mapTiles = game.map.tiles;
         Pokemon.this.moveDirs.clear();
         Pokemon.this.numMoves.clear();
         Pokemon.this.ledgeJumps.clear();
         game.insertAction(Pokemon.this.new DrawLower(true));
         game.insertAction(Pokemon.this.new DrawUpper(true));
         Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
         Pokemon.this.inWater = false;
         if (currTile != null && currTile.name.contains("water") && !currTile.name.contains("_tidal")) {
            Pokemon.this.inWater = true;
         }
      }

      @Override
      public void step(Game game) {
         Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
         Pokemon.this.mapTiles = game.map.tiles;
         if (Pokemon.this.moveDirs.size() > 0) {
            String moveDir = Pokemon.this.moveDirs.remove(0);
            float numMove = Pokemon.this.numMoves.remove(0);
            int ledgeJump = 0;
            if (Pokemon.this.ledgeJumps.size() > 0) {
               ledgeJump = Pokemon.this.ledgeJumps.remove(0);
            }

            this.targetTile = Pokemon.this.mapTiles.get(Pokemon.this.facingPos(this.pos, Pokemon.this.dirFacing));
            if (!this.onPlayer) {
               game.actionStack.remove(this);
               Action action;
               if (this.targetTile != null && this.targetTile.name.equals("ice2")) {
                  action = Pokemon.this.new Sliding(Pokemon.this.dirFacing, this);
               } else if (ledgeJump <= 0) {
                  action = Pokemon.this.new Moving(Pokemon.this.dirFacing, 1, numMove, this.alternate, true, this);
               } else {
                  action = Pokemon.this.new LedgeJump(Pokemon.this.dirFacing, ledgeJump, true, this);
               }

               this.alternate = !this.alternate;
               game.insertAction(action);
               Pokemon.this.standingAction = action;
            }

            this.onPlayer = false;
            Pokemon.this.dirFacing = moveDir;
         }
      }
   }

   public class FollowPath extends Action {
      public Action.Layer layer = Action.Layer.map_151;
      boolean alternate = true;
      Tile targetTile;
      private Vector2 pos = new Vector2();

      public FollowPath(Action nextAction) {
         super();
         this.nextAction = nextAction;
         Pokemon.this.standingAction = this;
      }

      @Override
      public void firstStep(Game game) {
      }

      @Override
      public void step(Game game) {
         if (Pokemon.this.moveDirs.size() > 0) {
            String moveDir = Pokemon.this.moveDirs.remove(0);
            float numMove = Pokemon.this.numMoves.remove(0);
            Pokemon.this.dirFacing = moveDir;
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            int ledgeJump = 0;
            if (Pokemon.this.ledgeJumps.size() > 0) {
               ledgeJump = Pokemon.this.ledgeJumps.remove(0);
            }

            this.targetTile = Pokemon.this.mapTiles.get(Pokemon.this.facingPos(this.pos, Pokemon.this.dirFacing));
            boolean isSolid;
            if (!Pokemon.this.inWater) {
               isSolid = this.targetTile == null || this.targetTile.isSolid;
            } else if (Pokemon.onlySwim.contains(Pokemon.this.specie.name)) {
               isSolid = this.targetTile == null || !this.targetTile.isWater;
            } else {
               isSolid = this.targetTile != null && !this.targetTile.isWater && this.targetTile.isSolid;
            }

            if (this.targetTile == null
               || isSolid
               || this.targetTile.isLedge
               || this.targetTile.name.contains("door")
               || this.targetTile.nameUpper.contains("gate")
               || this.targetTile.nameUpper.contains("door")
               || game.map.pokemon.containsKey(Pokemon.this.facingPos(this.pos, Pokemon.this.dirFacing))
               || game.player.position.equals(Pokemon.this.facingPos(this.pos, Pokemon.this.dirFacing))) {
               game.actionStack.remove(this);
               Pokemon.this.standingAction = this.nextAction;
               game.insertAction(this.nextAction);
               return;
            }

            Action action;
            if (this.targetTile.name.equals("ice2")) {
               action = Pokemon.this.new Sliding(Pokemon.this.dirFacing, this);
            } else if (ledgeJump <= 0) {
               action = Pokemon.this.new Moving(Pokemon.this.dirFacing, 1, numMove, this.alternate, false, this);
            } else {
               action = Pokemon.this.new LedgeJump(Pokemon.this.dirFacing, ledgeJump, false, this);
            }

            this.alternate = !this.alternate;
            game.actionStack.remove(this);
            game.insertAction(action);
            Pokemon.this.standingAction = action;
         } else {
            game.actionStack.remove(this);
            Pokemon.this.standingAction = this.nextAction;
            game.insertAction(this.nextAction);
         }
      }
   }

   public enum Generation {
      RED,
      CRYSTAL;
   }

   class LedgeJump extends Action {
      public Action.Layer layer = Action.Layer.map_131;
      float xDist;
      float yDist;
      Vector2 initialPos;
      Vector2 targetPos;
      Vector2 shadowPos;
      Sprite shadow;
      int timer1 = 0;
      ArrayList<Integer> yMovesList = new ArrayList<>();
      ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<>();
      int speed = 1;
      String dirFacing;
      boolean isFollowing;

      public LedgeJump(String dirFacing, int speed, boolean isFollowing, Action nextAction) {
         super();
         this.dirFacing = dirFacing;
         this.speed = speed;
         this.isFollowing = isFollowing;
         this.nextAction = nextAction;
         this.initialPos = new Vector2(Pokemon.this.position);
         if (Pokemon.this.dirFacing.equals("up")) {
            this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y + 32 / this.speed);
         } else if (Pokemon.this.dirFacing.equals("down")) {
            this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y - 32 / this.speed);
         } else if (Pokemon.this.dirFacing.equals("left")) {
            this.targetPos = new Vector2(Pokemon.this.position.x - 32 / this.speed, Pokemon.this.position.y);
         } else if (Pokemon.this.dirFacing.equals("right")) {
            this.targetPos = new Vector2(Pokemon.this.position.x + 32 / this.speed, Pokemon.this.position.y);
         }

         Texture shadowText = TextureCache.get(Gdx.files.internal("shadow1.png"));
         this.shadow = new Sprite(shadowText, 0, 0, 16, 16);
         this.shadowPos = this.initialPos.cpy();
         this.yMovesList.add(4);
         this.yMovesList.add(2);
         this.yMovesList.add(2);
         this.yMovesList.add(2);
         this.yMovesList.add(1);
         this.yMovesList.add(1);
         this.yMovesList.add(0);
         this.yMovesList.add(0);
         this.yMovesList.add(-1);
         this.yMovesList.add(-1);
         this.yMovesList.add(-1);
         this.yMovesList.add(-1);
         this.yMovesList.add(-2);
         this.yMovesList.add(-3);
         this.yMovesList.add(-3);
         this.yMovesList.add(0);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.movingSprites);
         this.spriteAnim.add(Pokemon.this.movingSprites);
         this.spriteAnim.add(Pokemon.this.movingSprites);
         this.spriteAnim.add(Pokemon.this.movingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.altMovingSprites);
         this.spriteAnim.add(Pokemon.this.altMovingSprites);
         this.spriteAnim.add(Pokemon.this.altMovingSprites);
         this.spriteAnim.add(Pokemon.this.altMovingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
         this.spriteAnim.add(Pokemon.this.standingSprites);
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void firstStep(Game game) {
         if (this.speed == 2) {
            game.insertAction(new PlayMusic("ledge2", null));
         } else {
            game.insertAction(new PlayMusic("ledge1", null));
         }
      }

      @Override
      public void step(Game game) {
         if (this.timer1 < 32 / this.speed) {
            if (this.dirFacing.equals("up")) {
               Pokemon.this.position.y++;
               this.shadowPos.y++;
            } else if (this.dirFacing.equals("down")) {
               Pokemon.this.position.y--;
               this.shadowPos.y--;
            } else if (this.dirFacing.equals("left")) {
               Pokemon.this.position.x--;
               this.shadowPos.x--;
            } else if (this.dirFacing.equals("right")) {
               Pokemon.this.position.x++;
               this.shadowPos.x++;
            }

            if (this.timer1 % 2 == 1) {
               Pokemon.this.position.y = Pokemon.this.position.y + this.yMovesList.remove(0).intValue();
               Pokemon.this.currOwSprite = this.spriteAnim.remove(0).get(this.dirFacing);

               for (int i = 0; i < this.speed - 1; i++) {
                  this.yMovesList.remove(0);
                  this.spriteAnim.remove(0);
               }
            }
         } else {
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(this.dirFacing);
         }

         game.mapBatch.draw(this.shadow, this.shadowPos.x, this.shadowPos.y - 6.0F);
         if (this.timer1 >= 38 / this.speed) {
            Pokemon.this.position.set(this.targetPos);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            Pokemon.this.standingAction = this.nextAction;
            if (!this.isFollowing) {
               Pokemon.this.checkHabitat(game);
            }

            this.nextAction.step(game);
         }

         this.timer1++;
      }
   }

   public class Moving extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      Vector2 initialPos;
      Vector2 targetPos;
      float xDist;
      float yDist;
      boolean alternate;
      int delay = 1;
      int timer = 1;
      float numMove = 1.0F;
      String dirFacing;
      boolean isFollowing = false;
      int timer2 = 0;
      Sprite standingSprite;

      public Moving(int delay, float numMove, boolean alternate, Action nextAction) {
         this(Pokemon.this.dirFacing, delay, numMove, alternate, false, nextAction);
      }

      public Moving(String dirFacing, int delay, float numMove, boolean alternate, boolean isFollowing, Action nextAction) {
         super();
         this.dirFacing = dirFacing;
         this.delay = delay;
         this.numMove = numMove;
         this.alternate = alternate;
         this.isFollowing = isFollowing;
         this.nextAction = nextAction;
         this.initialPos = new Vector2(Pokemon.this.position);
         if (this.dirFacing.equals("up")) {
            this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y + 16.0F);
         } else if (this.dirFacing.equals("down")) {
            this.targetPos = new Vector2(Pokemon.this.position.x, Pokemon.this.position.y - 16.0F);
         } else if (this.dirFacing.equals("left")) {
            this.targetPos = new Vector2(Pokemon.this.position.x - 16.0F, Pokemon.this.position.y);
         } else if (this.dirFacing.equals("right")) {
            this.targetPos = new Vector2(Pokemon.this.position.x + 16.0F, Pokemon.this.position.y);
         }

         if (!this.isFollowing) {
            if (Game.staticGame.map.pokemon.containsKey(Pokemon.this.position) && Game.staticGame.map.pokemon.get(Pokemon.this.position) == Pokemon.this) {
               Game.staticGame.map.pokemon.remove(Pokemon.this.position);
            }

            Game.staticGame.map.indexPokemon(this.targetPos.cpy(), Pokemon.this);
         }

         Pokemon.this.canMove = true;
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
         if (!this.isFollowing
            && game.battle.drawAction == null
            && game.playerCanMove
            && game.map.tiles == Pokemon.this.mapTiles
            && Pokemon.this.position.dst2(game.player.position) < 16384.0F
            && !(this.numMove <= 1.0F)) {
            if (this.numMove <= 1.5F) {
               if (Pokemon.this.inWater) {
                  game.insertAction(new PlayMusic("swim1_dupe", 0.5F, true, null));
                  game.insertAction(new PlayMusic("sand1_dupe", 0.4F, true, null));
                  game.insertAction(Pokemon.this.new SurfWakeAnim(null));
                  game.insertAction(Pokemon.this.new WaterSplash(true, new Vector2(0.0F, 0.0F), null));
               } else {
                  game.insertAction(new PlayMusic("ride1", 1.0F, true, null));
               }
            } else if (this.numMove <= 8.0F && Pokemon.this.inWater) {
               game.insertAction(new PlayMusic("puddle3", 0.6F, true, null));
               game.insertAction(new PlayMusic("menu_open1", 0.9F, true, null));
               game.insertAction(Pokemon.this.new SurfWakeAnim(null));
               game.insertAction(Pokemon.this.new WaterSplash(true, new Vector2(0.0F, 0.0F), null));
            }
         }

         if (Pokemon.this.layEggTimer <= 0 && Pokemon.this.loveInterest != null && !game.map.pokemon.containsKey(this.initialPos)) {
            Pokemon.this.layEgg(game, this.initialPos);
         }

         if (game.type == Game.Type.SERVER) {
            for (Player player : game.players.values()) {
               if (player.network.loadingZone.contains(this.targetPos)) {
                  game.server.sendToTCP(player.network.connectionId, new Network.MovePokemon(Pokemon.this));
               } else if (player.network.loadingZone.contains(Pokemon.this.position)) {
                  game.server.sendToTCP(player.network.connectionId, new Network.OverworldPokemonData(Pokemon.this, Pokemon.this.position, true));
               }
            }
         }

         this.standingSprite = new Sprite(Pokemon.this.standingSprites.get(this.dirFacing));
         if (!this.alternate || !Pokemon.this.specie.name.equals("dratini") || !Pokemon.this.dirFacing.equals("up") && !Pokemon.this.dirFacing.equals("down")) {
            if (this.alternate
               && Pokemon.this.specie.name.equals("dragonair")
               && (Pokemon.this.dirFacing.equals("up") || Pokemon.this.dirFacing.equals("down"))) {
               this.standingSprite.flip(true, false);
            }
         } else {
            this.standingSprite.flip(true, false);
         }
      }

      @Override
      public void step(Game game) {
         if (Pokemon.this.canMove) {
            this.timer--;
            if (this.timer <= 0) {
               this.timer = this.delay;
               if (this.dirFacing.equals("up")) {
                  Pokemon.this.position.y = Pokemon.this.position.y + this.numMove;
               } else if (this.dirFacing.equals("down")) {
                  Pokemon.this.position.y = Pokemon.this.position.y - this.numMove;
               } else if (this.dirFacing.equals("left")) {
                  Pokemon.this.position.x = Pokemon.this.position.x - this.numMove;
               } else if (this.dirFacing.equals("right")) {
                  Pokemon.this.position.x = Pokemon.this.position.x + this.numMove;
               }
            }

            this.xDist = Math.abs(this.initialPos.x - Pokemon.this.position.x);
            this.yDist = Math.abs(this.initialPos.y - Pokemon.this.position.y);
            if (this.yDist < 13.0F && this.yDist > 2.0F || this.xDist < 13.0F && this.xDist > 2.0F) {
               if (this.alternate) {
                  Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(this.dirFacing);
               } else {
                  Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(this.dirFacing);
               }
            } else {
               Pokemon.this.currOwSprite = this.standingSprite;
            }

            if ((int)this.xDist == 8 || (int)this.yDist == 8 || (int)this.xDist == 9 || (int)this.yDist == 9) {
               Tile currTile = Pokemon.this.mapTiles.get(this.targetPos);
               Pokemon.this.inWater = false;
               if (currTile != null && currTile.name.contains("water") && !currTile.name.contains("_tidal")) {
                  Pokemon.this.inWater = true;
               }
            }

            if (this.xDist >= 16.0F || this.yDist >= 16.0F) {
               Pokemon.this.position.set(this.targetPos);
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
               Pokemon.this.standingAction = this.nextAction;
               if (this.isFollowing) {
                  if (game.player.currFieldMove.equals("")) {
                     Pokemon.this.checkHabitat(game);
                  }

                  this.nextAction.step(game);
               } else {
                  Pokemon.this.checkHabitat(game);
               }
            }
         }
      }
   }

   public class RemoveFromInventory extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      int moveTimer = 0;

      public RemoveFromInventory() {
         super();
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void firstStep(Game game) {
         if (game.type == Game.Type.CLIENT) {
            Pokemon.this.type = Player.Type.REMOTE;
            game.client.sendTCP(new Network.DropPokemon(game.player.network.id, DrawPokemonMenu.currIndex, game.player.dirFacing));
         }

         game.player.pokemon.remove(Pokemon.this);
         game.map.indexPokemon(Pokemon.this.position.cpy(), Pokemon.this);
         Pokemon.this.moveDirs.clear();
         Pokemon.this.numMoves.clear();
         Pokemon.this.ledgeJumps.clear();
         if (game.player.currPokemon == Pokemon.this) {
            game.player.currPokemon = game.player.pokemon.get(0);
         }

         game.actionStack.remove(this);
         Pokemon.this.mapTiles = game.map.tiles;
         Pokemon.this.interiorIndex = game.map.tiles == game.map.overworldTiles ? 100 : game.map.interiorTilesIndex;
         Pokemon.this.drawThisFrame = true;
         if (Pokemon.this.previousOwner != null || Pokemon.this.lanOwnerId != null) Pokemon.this.aggroPlayer = false;
         else if ("drapion".equals(Pokemon.this.specie.name)) Pokemon.this.aggroPlayer = true;
         Pokemon.this.isRunning = false;
         game.insertAction(Pokemon.this.new Standing());
      }

      @Override
      public void step(Game game) {
      }
   }

   class SetNickname extends Action {
      Color prevColor = new Color();
      int avatarAnimCounter = 0;
      HashMap<Integer, Character> alphanumericKeys = new HashMap<>();
      HashMap<Integer, Character> alphanumericKeysShift = new HashMap<>();
      ArrayList<Character> text = new ArrayList<>();
      int backspaceTimer = 0;
      public boolean done = false;
      public boolean disabled = false;

      public SetNickname(Action nextAction) {
         super();
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

         for (char c : Pokemon.this.nickname.toCharArray()) {
            this.text.add(c);
         }

         this.nextAction = nextAction;
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.gui_104;
      }

      @Override
      public void firstStep(Game game) {
         game.insertAction(new DisplayText(game, "Press enter to set", null, true, false, null));
      }

      @Override
      public void step(Game game) {
         int i = 0;
         int j = 0;

         for (int k = 0; k < this.text.size(); k++) {
            char character = this.text.get(k);
            if (character == ' ' && k + 1 < this.text.size()) {
               int length = 1;
               char nextChar = this.text.get(k + length);

               while (nextChar != ' ' && k + length < this.text.size()) {
                  nextChar = this.text.get(k + length++);
               }

               if (i + length > 20 && j < 5) {
                  i = -1;
                  j++;
               }
            }

            SpriteProxy letterSprite = game.textDict.get(character);
            letterSprite.setPosition(8 + 8 * i, 8 - 16 * j);
            letterSprite.draw(game.uiBatch);
            if (++i > 20) {
               i = 0;
               j++;
            }
         }

         if (this.avatarAnimCounter >= 12) {
            SpriteProxy letterSprite = game.textDict.get('_');
            game.uiBatch.draw(letterSprite, 8 + 8 * i, 8 - 16 * j);
         }

         if (this.done) {
            game.actionStack.remove(this);
         }

         if (!this.disabled) {
            if (Gdx.input.isKeyPressed(67)) {
               if (this.backspaceTimer < 30) {
                  this.backspaceTimer++;
               }
            } else {
               this.backspaceTimer = 0;
            }

            if ((this.backspaceTimer >= 30 || Gdx.input.isKeyJustPressed(67)) && this.text.size() > 0) {
               this.text.remove(this.text.size() - 1);
            }

            if (Gdx.input.isKeyJustPressed(66)) {
               this.disabled = true;
               String allText = "";

               for (char c : this.text) {
                  allText = allText + c;
               }

               game.insertAction(
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           "Keep changes?",
                           null,
                           true,
                           false,
                           new DrawYesNoMenu(
                              null,
                              new SetField(
                                 this,
                                 "done",
                                 true,
                                 new DisplayText.Clear(
                                    game,
                                    new WaitFrames(
                                       game,
                                       3,
                                       new SetField(Pokemon.this, "nickname", allText, new PlayMusic("coin1", 0.6F, new WaitFrames(game, 10, this.nextAction)))
                                    )
                                 )
                              ),
                              new SetField(this, "done", true, new DisplayText.Clear(game, new WaitFrames(game, 10, this.nextAction)))
                           )
                        )
                     )
                  )
               );
            }

            if (i < 10) {
               if (Gdx.input.isKeyJustPressed(62)) {
                  this.text.add(' ');
               }

               if (!Gdx.input.isKeyPressed(59) && !Gdx.input.isKeyPressed(60)) {
                  for (Integer key : this.alphanumericKeys.keySet()) {
                     if (Gdx.input.isKeyJustPressed(key)) {
                        this.text.add(this.alphanumericKeys.get(key));
                     }
                  }
               } else {
                  for (Integer key : this.alphanumericKeysShift.keySet()) {
                     if (Gdx.input.isKeyJustPressed(key)) {
                        this.text.add(this.alphanumericKeysShift.get(key));
                     }
                  }
               }

               this.avatarAnimCounter--;
               if (this.avatarAnimCounter <= 0) {
                  this.avatarAnimCounter = 24;
               }
            }
         }
      }
   }

   public class SetStat extends Action {
      public Action.Layer layer = Action.Layer.map_5000;
      String stat;
      int setTo;

      public SetStat(String stat, int setTo, Action nextAction) {
         super();
         this.stat = stat;
         this.setTo = setTo;
         this.nextAction = nextAction;
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public String getCamera() {
         return "map";
      }

      @Override
      public void step(Game game) {
         Pokemon.this.currentStats.put(this.stat, this.setTo);
         System.out.println(Pokemon.this.currentStats.get(this.stat));
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   public class Shadowed extends Action {
      public Action.Layer layer = Action.Layer.map_107;
      public int timer = 0;
      public int timesMoved = 0;
      boolean checkEncounter = false;

      public Shadowed(Action nextAction) {
         super();
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
         Pokemon.this.standingAction = this;
         Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
         game.insertAction(new Pokemon.Shadowed.DrawShadow());
      }

      @Override
      public void step(Game game) {
         if (game.playerCanMove) {
            if (!game.player.isSleeping) {
               game.actionStack.remove(this);
               Pokemon.this.removeDrawActions(game);
            } else {
               if (this.checkEncounter) {
                  this.checkEncounter = false;
                  float dst2 = Pokemon.this.position.dst2(game.player.position);
                  if (dst2 < 64.0F && !game.player.currFieldMove.equals("FLY") && game.playerCanMove) {
                     game.playerCanMove = false;
                     game.battle.oppPokemon = Pokemon.this;
                     game.player.setCurrPokemon();
                     game.insertAction(new WaitFrames(game, 16, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game))));
                     game.insertAction(
                        new WaitFrames(
                           game,
                           500,
                           new SetField(
                              game.player,
                              "isSleeping",
                              false,
                              new SetField(game.player, "acceptInput", true, new SetField(game.player, "drawSleepingBag", false, null))
                           )
                        )
                     );
                     return;
                  }
               }

               this.timer--;
               if (this.timer <= 0) {
                  this.timer = (Game.rand.nextInt(2) + 3) * 60;
                  if (this.timesMoved < 3) {
                     this.timesMoved++;
                  } else if (this.timesMoved < 5) {
                     this.timer = 30;
                  } else {
                     this.timer = 10;
                  }

                  ArrayList<String> preferredMoves = new ArrayList<>();
                  float dx = Pokemon.this.position.x - game.player.position.x;
                  float dy = Pokemon.this.position.y - game.player.position.y;
                  if (dx < dy) {
                     if (game.player.position.y < Pokemon.this.position.y) {
                        preferredMoves.add("down");
                        preferredMoves.add("right");
                     } else {
                        preferredMoves.add("right");
                        preferredMoves.add("up");
                     }
                  } else if (game.player.position.x < Pokemon.this.position.x) {
                     preferredMoves.add("left");
                     preferredMoves.add("down");
                  } else {
                     preferredMoves.add("up");
                     preferredMoves.add("left");
                  }

                  Iterator var5 = preferredMoves.iterator();
                  if (var5.hasNext()) {
                     String move = (String)var5.next();
                     Vector2 startPos = Pokemon.this.position.cpy().add(-80.0F, -80.0F);
                     startPos.x = (int)startPos.x - (int)startPos.x % 16;
                     startPos.y = (int)startPos.y - (int)startPos.y % 16;
                     Vector2 endPos = Pokemon.this.position.cpy().add(80.0F, 80.0F);
                     endPos.x = (int)endPos.x - (int)endPos.x % 16;
                     endPos.y = (int)endPos.y - (int)endPos.y % 16;
                     Vector2 currPos = new Vector2(startPos.x, startPos.y);

                     while (currPos.y < endPos.y) {
                        Tile tile = game.map.tiles.get(currPos);
                        currPos.x += 16.0F;
                        if (currPos.x > endPos.x) {
                           currPos.x = startPos.x;
                           currPos.y += 16.0F;
                        }

                        if (tile != null) {
                           if (tile.nameUpper.contains("campfire")) {
                              return;
                           }

                           if (tile.items != null && tile.items.containsKey("torch") && Pokemon.this.position.dst2(tile.position) < 1024.0F) {
                              return;
                           }

                           if (game.map.pokemon.containsKey(tile.position)) {
                              Pokemon pokemon = game.map.pokemon.get(tile.position);
                              if (pokemon.hms.contains("FLASH")) {
                                 return;
                              }
                           } else if (game.player.hmPokemon != null
                              && Pokemon.this.position.dst2(game.player.position) < 4096.0F
                              && game.player.hmPokemon.hms.contains("FLASH")) {
                              return;
                           }
                        }
                     }

                     Pokemon.this.dirFacing = move;
                     game.actionStack.remove(this);
                     Action action = Pokemon.this.new Moving(Pokemon.this.dirFacing, 1, 1.5F, true, true, this);
                     game.insertAction(action);
                     Pokemon.this.standingAction = action;
                     this.checkEncounter = true;
                  }
               }
            }
         }
      }

      class DrawShadow extends Pokemon.DrawUpper {
         Texture texture;
         public Action.Layer layer = Action.Layer.map_130;

         @Override
         public String getCamera() {
            return "map";
         }

         @Override
         public Action.Layer getLayer() {
            return this.layer;
         }

         public DrawShadow() {
            Pokemon.this.drawUpper = this;
            this.texture = TextureCache.get(Gdx.files.internal("shadow1.png"));
         }

         @Override
         public void firstStep(Game game) {
         }

         @Override
         public void step(Game game) {
            game.mapBatch.draw(this.texture, Pokemon.this.position.x, Pokemon.this.position.y - 1.0F);
         }
      }
   }

   class Sliding extends Action {
      public Action.Layer layer = Action.Layer.map_113;
      Vector2 startPos;
      Vector2 targetPos;
      Tile startTile;
      Tile targetTile;
      String dirFacing;
      int timer = 0;

      public Sliding(String dirFacing, Action nextAction) {
         super();
         this.dirFacing = dirFacing;
         this.nextAction = nextAction;
         this.startPos = new Vector2(Pokemon.this.position);
         this.targetPos = Pokemon.this.facingPos(new Vector2(), this.dirFacing);
         if (!(Pokemon.this.standingAction instanceof Pokemon.Follow)) {
            if (Game.staticGame.map.pokemon.containsKey(Pokemon.this.position) && Game.staticGame.map.pokemon.get(Pokemon.this.position) == Pokemon.this) {
               Game.staticGame.map.pokemon.remove(Pokemon.this.position);
            }

            Game.staticGame.map.indexPokemon(this.targetPos.cpy(), Pokemon.this);
         }

         Pokemon.this.canMove = true;
      }

      @Override
      public void firstStep(Game game) {
         this.startTile = game.map.tiles.get(this.startPos);
         this.targetTile = game.map.tiles.get(this.targetPos);
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
      public void step(Game game) {
         if (Pokemon.this.canMove) {
            if (game.playerCanMove) {
               if (this.dirFacing.equals("up")) {
                  Pokemon.this.position.y += 2.0F;
               } else if (this.dirFacing.equals("down")) {
                  Pokemon.this.position.y -= 2.0F;
               } else if (this.dirFacing.equals("left")) {
                  Pokemon.this.position.x -= 2.0F;
               } else if (this.dirFacing.equals("right")) {
                  Pokemon.this.position.x += 2.0F;
               }

               if (this.timer > 6) {
                  game.actionStack.remove(this);
                  game.insertAction(this.nextAction);
                  Pokemon.this.position.set(this.targetPos);
                  Pokemon.this.standingAction = this.nextAction;
                  if (!(Pokemon.this.standingAction instanceof Pokemon.Follow)) {
                     if (game.player.currFieldMove.equals("")) {
                        Pokemon.this.checkHabitat(game);
                     }
                  } else {
                     Pokemon.this.checkHabitat(game);
                  }

                  this.nextAction.step(game);
               }

               this.timer++;
            }
         }
      }
   }

   public class Standing extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      int moveTimer = 0;
      int runTimer = 0;
      int danceCounter = 240;
      boolean alternate = true;
      public int aggroTimer = 0;
      public boolean justAggroed = true;
      public boolean isEgg = false;
      public int losTimer = 0;
      public String nearbyDir = null;
      public String nearbyDir2 = null;
      public int charmTimer = 0;
      public int lungeCounter = 0;
      public String lungeDir = null;
      private Vector2 pos1 = new Vector2();
      private Vector2 pos2 = new Vector2();
      private Vector2 pos3 = new Vector2();

      public Standing() {
         super();
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
         this.moveTimer = game.map.rand.nextInt(180) + 60;
         game.map.indexPokemon(Pokemon.this.position.cpy(), Pokemon.this);
         Pokemon.this.standingAction = this;
         Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
         game.insertAction(Pokemon.this.new DrawLower());
         game.insertAction(Pokemon.this.new DrawUpper());
         Pokemon.this.checkHabitat(game);
         this.isEgg = Pokemon.this.isEgg;
         Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
         Pokemon.this.inWater = false;
         if (currTile != null && currTile.name.contains("water") && !currTile.name.contains("_tidal")) {
            Pokemon.this.inWater = true;
         }
      }

      public void localStep(Game game) {
         if (Pokemon.this.previousOwner != null || !Pokemon.this.specie.name.equals("darmanitanzen")) {
            if (this.isEgg) {
               if (this.danceCounter < 4) {
                  Pokemon.this.spriteOffsetY = 16;
                  Pokemon.this.currOwSprite = Pokemon.this.avatarSprites.get(1);
               } else if (this.danceCounter < 16) {
                  Pokemon.this.spriteOffsetY = 0;
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
               } else if (this.danceCounter < 20) {
                  Pokemon.this.spriteOffsetY = 16;
                  Pokemon.this.currOwSprite = Pokemon.this.avatarSprites.get(1);
               } else if (this.danceCounter < 90) {
                  Pokemon.this.spriteOffsetY = 0;
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
               } else if (this.danceCounter < 94) {
                  Pokemon.this.spriteOffsetY = 32;
                  Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get("down");
               } else if (this.danceCounter < 98) {
                  Pokemon.this.spriteOffsetY = 0;
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
               } else if (this.danceCounter < 102) {
                  Pokemon.this.spriteOffsetY = 48;
                  Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get("down");
               } else if (this.danceCounter < 106) {
                  Pokemon.this.spriteOffsetY = 0;
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
               } else if (this.danceCounter < 110) {
                  Pokemon.this.spriteOffsetY = 32;
                  Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get("down");
               } else if (this.danceCounter < 114) {
                  Pokemon.this.spriteOffsetY = 0;
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
               } else if (this.danceCounter < 118) {
                  Pokemon.this.spriteOffsetY = 48;
                  Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get("down");
               } else {
                  Pokemon.this.spriteOffsetY = 0;
                  Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get("down");
               }

               this.danceCounter++;
               if (this.danceCounter >= 240 + 30 * (Pokemon.this.happiness - 1)) {
                  this.danceCounter = 0;
               }
            } else {
               Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
               this.charmTimer++;
               if (this.charmTimer > 240) {
                  this.charmTimer = 0;
                  Pokemon.this.isCharmed = game.player.currFieldMove.equals("CHARM")
                     && game.player.hmPokemon != null
                     && game.player.hmPokemon.level >= Pokemon.this.level;
                  if (Pokemon.this.isCharmed) {
                     game.insertAction(Pokemon.this.new Emote("heart", null));
                  }
               }

               if (Pokemon.this.aggroPlayer && Pokemon.this.mapTiles == game.map.tiles) {
                  if (!game.playerCanMove) {
                     return;
                  }

                  if (this.aggroTimer > 240) {
                     this.aggroTimer = 0;
                  }

                  this.aggroTimer++;
                  if (this.aggroTimer == 1) {
                     game.insertAction(Pokemon.this.new Emote("skull", null));
                  }

                  float dst2 = Pokemon.this.position.dst2(game.player.position);
                  boolean hasLos = true;
                  if (Pokemon.this.mapTiles != game.map.overworldTiles) {
                     if (dst2 < 16384.0F && this.losTimer <= 0) {
                        Vector2 startPos = this.pos1.set(Pokemon.this.position);
                        Vector2 losVector = this.pos2.set(game.player.position).sub(startPos).nor().scl(16.0F);
                        Vector2 checkPos = this.pos3;

                        while (Pokemon.this.position.dst2(startPos) < dst2) {
                           startPos.add(losVector);
                           checkPos.set((int)startPos.x - (int)startPos.x % 16, (int)startPos.y - (int)startPos.y % 16);
                           Tile mapTile = Pokemon.this.mapTiles.get(checkPos);
                           if (mapTile == null) {
                              hasLos = false;
                              break;
                           }
                        }

                        if (hasLos) {
                           this.losTimer = 60;
                        }
                     }

                     if (this.losTimer > 0) {
                        this.losTimer--;
                     }
                  }

                  if (dst2 < 14400.0F && hasLos && !Pokemon.this.isCharmed) {
                     if (this.justAggroed) {
                        game.insertAction(Pokemon.this.new Emote("skull", null));
                        this.aggroTimer = 1;
                        this.justAggroed = false;
                     }

                     if (this.aggroTimer == 1) {
                        if (Pokemon.this.specie.name.equals("dusclops")) {
                           game.insertAction(new PlayMusic("pokemon/cries/" + Pokemon.this.dexNumber, 0.2F, null));
                        } else {
                           game.insertAction(new PlayMusic(Pokemon.this, null));
                        }
                     }

                     if (this.aggroTimer < 4) {
                        if (this.aggroTimer == 0) {
                           game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                        }

                        Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 8) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 12) {
                        if (this.aggroTimer == 8) {
                           game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                        }

                        Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 16) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 20) {
                        if (this.aggroTimer == 16) {
                           game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                        }

                        Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 24) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 28) {
                        if (this.aggroTimer == 24) {
                           game.insertAction(new PlayMusic("ride1", 0.5F, true, null));
                        }

                        Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     if (this.aggroTimer < 32) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                        return;
                     }

                     ArrayList<String> preferredMoves = new ArrayList<>();
                     if (Pokemon.this.position.x < game.player.position.x) {
                        preferredMoves.add("right");
                     } else if (Pokemon.this.position.x > game.player.position.x) {
                        preferredMoves.add("left");
                     }

                     if (Pokemon.this.position.y < game.player.position.y) {
                        preferredMoves.add("up");
                     } else if (Pokemon.this.position.y > game.player.position.y) {
                        preferredMoves.add("down");
                     }

                     if (!preferredMoves.isEmpty() && Game.rand.nextInt(256) < 128) {
                        preferredMoves.add(preferredMoves.remove(0));
                     }

                     if (this.lungeCounter <= 0 && dst2 <= 4096.0F && Pokemon.this.specie.name.equals("sharpedo")) {
                        boolean facingPlayer = (Pokemon.this.dirFacing.equals("down") || Pokemon.this.dirFacing.equals("up"))
                           && Pokemon.this.position.x == game.player.position.x;
                        facingPlayer |= (Pokemon.this.dirFacing.equals("left") || Pokemon.this.dirFacing.equals("right"))
                           && Pokemon.this.position.y == game.player.position.y;
                        if (facingPlayer) {
                           this.lungeCounter = 10;
                           this.lungeDir = Pokemon.this.dirFacing;
                           this.aggroTimer = 0;
                           return;
                        }
                     }

                     if (this.lungeCounter > 0) {
                        preferredMoves.clear();
                        preferredMoves.add(this.lungeDir);
                        this.lungeCounter--;
                     }

                     if (dst2 < 64.0F && this.aggroTimer > 32 && !game.player.currFieldMove.equals("FLY")
                           && game.battle.drawAction == null
                           && (game.battle.oppPokemon == null || !game.battle.oppPokemon.inBattle)
                           && (game.coop == null || !game.coop.hasHostilityCooldown())) {
                        if (game.player.isSleeping) {
                           game.player.isSleeping = false;
                           if (game.player.sleepingDir == null) {
                              game.player.drawSleepingBag = false;
                              game.player.acceptInput = true;
                           } else {
                              game.player.sleepingDir = null;
                           }
                        }

                        if (game.player.acceptInput) {
                           game.playerCanMove = false;
                           this.aggroTimer = 0;
                           game.musicController.startBattle = "wild";
                           game.battle.oppPokemon = Pokemon.this;
                           game.player.setCurrPokemon();
                           game.insertAction(Battle.getIntroAction(game));
                           return;
                        }
                     }

                     for (int i = 0; i < preferredMoves.size(); i++) {
                        String move = preferredMoves.get(i);
                        Vector2 facingPos = Pokemon.this.facingPos(this.pos1, move);
                        Tile facingTile = Pokemon.this.mapTiles.get(facingPos);
                        Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                        boolean isLedge = facingTile != null && facingTile.isLedge
                           || currTile != null && currTile.isLedge && currTile.ledgeDir().equals("up") && move.equals("up");
                        boolean isSolid;
                        if (Pokemon.this.inWater) {
                           if (Pokemon.onlySwim.contains(Pokemon.this.specie.name)) {
                              isSolid = facingTile == null || !facingTile.isWater;
                           } else {
                              isSolid = facingTile != null && !facingTile.isWater && facingTile.isSolid;
                           }
                        } else {
                           isSolid = facingTile == null || facingTile.isSolid;
                        }

                        if (facingTile != null
                           && !isSolid
                           && !isLedge
                           && !facingTile.name.contains("door")
                           && !facingTile.nameUpper.contains("gate")
                           && !facingTile.nameUpper.contains("door")
                           && !game.map.pokemon.containsKey(facingPos)) {
                           Pokemon.this.dirFacing = move;
                           game.actionStack.remove(this);
                           float speed = 1.5F;
                           if (this.lungeCounter > 0) {
                              speed = 2.7F;
                           }

                           Action action = Pokemon.this.new Moving(1, speed, this.alternate, this);
                           this.alternate = !this.alternate;
                           game.insertAction(action);
                           Pokemon.this.standingAction = action;
                           break;
                        }
                     }

                     return;
                  }

                  this.justAggroed = true;
               }

               if (Pokemon.this.isSkittish()
                  && this.moveTimer % 32 == 0
                  && this.runTimer <= 0
                  && (game.coop == null ? Pokemon.this.previousOwner != game.player
                        : Pokemon.this.previousOwner == null && Pokemon.this.lanOwnerId == null)
                  && !Pokemon.this.isRunning
                  && !Pokemon.this.isCharmed) {
                  float dst2 = Pokemon.this.position.dst2(game.player.position);
                  if (dst2 < 4096.0F) {
                     this.runTimer = 180;
                  }
               }

               if (this.runTimer > 0 && !Pokemon.this.isRunning) {
                  if (this.runTimer == 170) {
                     game.insertAction(Pokemon.this.new Emote("!", null));
                     game.insertAction(new PlayMusic("ledge2", null));
                  }

                  if (this.runTimer == 110) {
                     Pokemon.this.dirFacing = "left";
                  }

                  if (this.runTimer == 100) {
                     Pokemon.this.dirFacing = "right";
                  }

                  if (this.runTimer == 90) {
                     Pokemon.this.dirFacing = "left";
                  }

                  if (this.runTimer == 80) {
                     Pokemon.this.dirFacing = "right";
                  }

                  if (this.runTimer == 70) {
                     Pokemon.this.isRunning = true;
                  }

                  this.runTimer--;
               } else if (Pokemon.this.isRunning && Pokemon.this.canMove) {
                  if (Pokemon.this.mapTiles.containsKey(Pokemon.this.position)) {
                     Tile tile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                     if (tile.isGrass) {
                        Pokemon.this.isRunning = false;
                        game.map.pokemon.remove(Pokemon.this.position);
                        Pokemon.this.removeDrawActions(game);
                        return;
                     }
                  }

                  ArrayList<String> preferMoves = new ArrayList<>();
                  String moveDir = null;
                  Vector2 tl = this.pos1;
                  tl.set(Pokemon.this.position).add(-96.0F, -96.0F);

                  for (; tl.y < Pokemon.this.position.y + 96.0F; tl.x += 16.0F) {
                     if (tl.x > Pokemon.this.position.x + 96.0F) {
                        tl.x = Pokemon.this.position.x - 112.0F;
                        tl.y += 16.0F;
                     } else {
                        Tile tile = Pokemon.this.mapTiles.get(tl);
                        if (tile != null && tile.isGrass && game.map.rand.nextInt(5) == 0) {
                           float dx = Pokemon.this.position.x - tile.position.x;
                           float dy = Pokemon.this.position.y - tile.position.y;
                           if (dx < dy) {
                              if (tile.position.y < Pokemon.this.position.y) {
                                 preferMoves.add("down");
                              } else {
                                 preferMoves.add("right");
                              }
                           } else if (tile.position.x < Pokemon.this.position.x) {
                              preferMoves.add("left");
                           } else {
                              preferMoves.add("up");
                           }
                           break;
                        }
                     }
                  }

                  float dx = Pokemon.this.position.x - game.player.position.x;
                  float dy = Pokemon.this.position.y - game.player.position.y;
                  if (dx < dy) {
                     Vector2 up = this.pos1.set(Pokemon.this.position.x, Pokemon.this.position.y + 16.0F);
                     Vector2 left = this.pos2.set(Pokemon.this.position.x - 16.0F, Pokemon.this.position.y);
                     Tile upTile = Pokemon.this.mapTiles.get(up);
                     Tile leftTile = Pokemon.this.mapTiles.get(left);
                     if (game.player.position.y < Pokemon.this.position.y) {
                        preferMoves.add("up");
                     } else if (!leftTile.isSolid) {
                        preferMoves.add("left");
                     }
                  }

                  Vector2 right = this.pos1.set(Pokemon.this.position.x + 16.0F, Pokemon.this.position.y);
                  Vector2 down = this.pos2.set(Pokemon.this.position.x, Pokemon.this.position.y - 16.0F);
                  Tile rightTile = Pokemon.this.mapTiles.get(right);
                  Tile downTile = Pokemon.this.mapTiles.get(down);
                  if (game.player.position.x < Pokemon.this.position.x) {
                     preferMoves.add("right");
                  } else {
                     preferMoves.add("down");
                  }

                  Vector2 up = this.pos1.set(Pokemon.this.position.x, Pokemon.this.position.y + 16.0F);
                  Tile upTile = Pokemon.this.mapTiles.get(up);
                  Vector2 left = this.pos2.set(Pokemon.this.position.x - 16.0F, Pokemon.this.position.y);
                  Tile leftTile = Pokemon.this.mapTiles.get(left);
                  if (game.player.position.y < Pokemon.this.position.y) {
                     preferMoves.add("up");
                  } else {
                     preferMoves.add("left");
                  }

                  for (String move : preferMoves) {
                     if (move.equals("up") && !upTile.isSolid && !upTile.isLedge && !upTile.nameUpper.contains("gate") && !upTile.name.contains("door")) {
                        moveDir = move;
                        break;
                     }

                     if (move.equals("right")
                        && !rightTile.isSolid
                        && !rightTile.isLedge
                        && !rightTile.nameUpper.contains("gate")
                        && !rightTile.name.contains("door")) {
                        moveDir = move;
                        break;
                     }

                     if (move.equals("down")
                        && !downTile.isSolid
                        && !downTile.isLedge
                        && !downTile.nameUpper.contains("gate")
                        && !downTile.name.contains("door")) {
                        moveDir = move;
                        break;
                     }

                     if (move.equals("left")
                        && !leftTile.isSolid
                        && !leftTile.isLedge
                        && !leftTile.nameUpper.contains("gate")
                        && !leftTile.name.contains("door")) {
                        moveDir = move;
                        break;
                     }
                  }

                  if (moveDir != null) {
                     Pokemon.this.dirFacing = moveDir;
                     game.actionStack.remove(this);
                     Action action = Pokemon.this.new Moving(1, 2.0F, this.alternate, this);
                     this.alternate = !this.alternate;
                     game.insertAction(action);
                     Pokemon.this.standingAction = action;
                  }
               } else {
                  if (Pokemon.this.hasItem != null) {
                     this.danceCounter++;
                     if (this.danceCounter <= 16) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                     } else if (this.danceCounter <= 32) {
                        Pokemon.this.currOwSprite = Pokemon.this.movingSprites.get(Pokemon.this.dirFacing);
                     } else if (this.danceCounter <= 48) {
                        Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                     } else if (this.danceCounter < 64) {
                        Pokemon.this.currOwSprite = Pokemon.this.altMovingSprites.get(Pokemon.this.dirFacing);
                     } else {
                        this.danceCounter = 0;
                     }
                  }

                  if (Pokemon.this.canMove) {
                     if (!Pokemon.this.moveDirs.isEmpty()) {
                        this.moveTimer = game.map.rand.nextInt(180) + 60;
                        game.actionStack.remove(this);
                        game.insertAction(Pokemon.this.new FollowPath(this));
                     } else if (Pokemon.this.friendTimer <= 0 && Pokemon.this.friend != null) {
                        Pokemon.this.friendTimer = Pokemon.friendTimerMax;
                        ArrayList<Vector2> checkThese = new ArrayList<>();
                        HashMap<Vector2, Vector2> prevTiles = new HashMap<>();
                        Vector2 pos = this.pos1.set(Pokemon.this.position);
                        checkThese.add(pos);
                        prevTiles.put(pos, null);
                        Pokemon foundPokemon = null;

                        for (int tries = 0; tries < 20 && !checkThese.isEmpty(); tries++) {
                           pos = checkThese.remove(0);
                           foundPokemon = game.map.pokemon.get(pos);
                           if (foundPokemon != null) {
                              if (foundPokemon == Pokemon.this.friend) {
                                 break;
                              }

                              if (foundPokemon != Pokemon.this) {
                                 continue;
                              }
                           }

                           for (int i = 0; i < Pokemon.FRIEND_OFFSETS.length; i++) {
                              Vector2 offset = Pokemon.FRIEND_OFFSETS[i];
                              Vector2 newPos = this.pos2.set(pos).add(offset);
                              Tile temp = Pokemon.this.mapTiles.get(newPos);
                              if (temp != null
                                 && !temp.isSolid
                                 && !temp.isLedge
                                 && !temp.name.contains("door")
                                 && !temp.nameUpper.contains("gate")
                                 && !temp.nameUpper.contains("door")
                                 && !game.player.position.equals(newPos)
                                 && !prevTiles.containsKey(newPos)) {
                                 checkThese.add(newPos.cpy());
                                 prevTiles.put(newPos.cpy(), pos.cpy());
                              }
                           }
                        }

                        if (foundPokemon == Pokemon.this.friend) {
                           foundPokemon.moveDirs.clear();
                           foundPokemon.numMoves.clear();

                           while (prevTiles.get(pos) != null) {
                              Vector2 prevPos = prevTiles.get(pos);
                              String direction = null;
                              if (prevPos.x < pos.x) {
                                 direction = "left";
                              } else if (prevPos.x > pos.x) {
                                 direction = "right";
                              } else if (prevPos.y < pos.y) {
                                 direction = "down";
                              } else if (prevPos.y > pos.y) {
                                 direction = "up";
                              }

                              if (direction == null) {
                                 return;
                              }

                              foundPokemon.moveDirs.add(direction);
                              foundPokemon.numMoves.add(1.2F);
                              pos = prevPos;
                           }

                           checkThese.clear();
                           prevTiles.clear();
                           pos.set(Pokemon.this.position);
                           checkThese.add(pos);
                           prevTiles.put(pos, null);

                           int tries;
                           for (tries = 0; tries < 30 && !checkThese.isEmpty(); tries++) {
                              pos = checkThese.remove(0);

                              for (Vector2 offset : Pokemon.FRIEND_OFFSETS) {
                                 Vector2 newPos = this.pos2.set(pos).add(offset);
                                 Tile temp = Pokemon.this.mapTiles.get(newPos);
                                 if (temp != null
                                    && !temp.isSolid
                                    && !temp.isLedge
                                    && !temp.name.contains("door")
                                    && !temp.nameUpper.contains("gate")
                                    && !temp.nameUpper.contains("door")
                                    && !game.map.pokemon.containsKey(newPos)
                                    && !game.player.position.equals(newPos)
                                    && !prevTiles.containsKey(newPos)) {
                                    checkThese.add(newPos.cpy());
                                    prevTiles.put(newPos.cpy(), pos.cpy());
                                 }
                              }
                           }

                           Pokemon.this.moveDirs.clear();
                           Pokemon.this.numMoves.clear();

                           while (tries >= 20 && prevTiles.get(pos) != null) {
                              Vector2 prevPos = prevTiles.get(pos);
                              String direction = null;
                              if (prevPos.x < pos.x) {
                                 direction = "right";
                              } else if (prevPos.x > pos.x) {
                                 direction = "left";
                              } else if (prevPos.y < pos.y) {
                                 direction = "up";
                              } else if (prevPos.y > pos.y) {
                                 direction = "down";
                              }

                              if (direction == null) {
                                 return;
                              }

                              Pokemon.this.moveDirs.add(direction);
                              Pokemon.this.numMoves.add(1.2F);
                              pos = prevPos;
                           }

                           float dst2 = Pokemon.this.position.dst2(game.player.position);
                           if (dst2 < 16384.0F && game.battle.drawAction == null && game.playerCanMove && game.map.tiles == Pokemon.this.mapTiles) {
                              game.insertAction(new PlayMusic(Pokemon.this, true, null));
                           }

                           game.insertAction(Pokemon.this.new Emote("happy", null));
                           game.insertAction(Pokemon.this.friend.new Emote("happy", null));
                        }
                     } else {
                        if (this.moveTimer <= 0) {
                           if (Pokemon.this.previousOwner == null && Pokemon.this.specie.name.equals("sigilyph")) {
                              if (Pokemon.this.dirFacing.equals("up") && Pokemon.this.position.y % 64.0F == 0.0F) {
                                 if (Pokemon.this.position.x % 128.0F == 0.0F) {
                                    Pokemon.this.dirFacing = "left";
                                 } else {
                                    Pokemon.this.dirFacing = "right";
                                 }
                              } else if (Pokemon.this.dirFacing.equals("left") && Pokemon.this.position.x % 64.0F == 0.0F) {
                                 if (Pokemon.this.position.y % 128.0F == 64.0F) {
                                    Pokemon.this.dirFacing = "down";
                                 } else {
                                    Pokemon.this.dirFacing = "up";
                                 }
                              } else if (Pokemon.this.dirFacing.equals("down") && Pokemon.this.position.y % 64.0F == 0.0F) {
                                 if (Pokemon.this.position.x % 128.0F == 0.0F) {
                                    Pokemon.this.dirFacing = "left";
                                 } else {
                                    Pokemon.this.dirFacing = "right";
                                 }
                              } else if (Pokemon.this.dirFacing.equals("right") && Pokemon.this.position.x % 64.0F == 0.0F) {
                                 if (Pokemon.this.position.y % 128.0F == 64.0F) {
                                    Pokemon.this.dirFacing = "down";
                                 } else {
                                    Pokemon.this.dirFacing = "up";
                                 }
                              }

                              Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
                              Vector2 newPos = Pokemon.this.facingPos(this.pos1);
                              Tile temp = Pokemon.this.mapTiles.get(newPos);
                              boolean collidesWithPlayer = false;

                              for (Player player : game.players.values()) {
                                 if (newPos.equals(player.position)) {
                                    collidesWithPlayer = true;
                                    break;
                                 }
                              }

                              Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                              boolean isLedge = temp != null && temp.isLedge
                                 || currTile != null && currTile.isLedge && currTile.ledgeDir().equals("up") && Pokemon.this.dirFacing.equals("up");
                              if (temp != null
                                 && !temp.isSolid
                                 && !isLedge
                                 && !temp.name.contains("door")
                                 && !temp.nameUpper.contains("gate")
                                 && !temp.nameUpper.contains("door")
                                 && !game.map.pokemon.containsKey(newPos)
                                 && !game.player.position.equals(newPos)
                                 && !collidesWithPlayer) {
                                 game.actionStack.remove(this);
                                 Action action = Pokemon.this.new Moving(2, 1.0F, true, null);
                                 action.append(this);
                                 game.insertAction(action);
                                 Pokemon.this.standingAction = action;
                                 return;
                              }

                              if (Pokemon.this.dirFacing.equals("up")) {
                                 Pokemon.this.dirFacing = "down";
                              } else if (Pokemon.this.dirFacing.equals("left")) {
                                 Pokemon.this.dirFacing = "right";
                              } else if (Pokemon.this.dirFacing.equals("down")) {
                                 Pokemon.this.dirFacing = "up";
                              } else if (Pokemon.this.dirFacing.equals("right")) {
                                 Pokemon.this.dirFacing = "left";
                              }

                              return;
                           }

                           this.nearbyDir = null;
                           this.nearbyDir2 = null;
                           if (!Pokemon.this.specie.name.equals("diglett")
                              && !Pokemon.this.specie.name.equals("dugtrio")
                              && !Pokemon.this.specie.name.equals("sharpedo")) {
                              Vector2 startPos = this.pos1.set(Pokemon.this.position).add(-80.0F, -80.0F);
                              startPos.x = (int)startPos.x - (int)startPos.x % 16;
                              startPos.y = (int)startPos.y - (int)startPos.y % 16;
                              Vector2 endPos = this.pos2.set(Pokemon.this.position).add(80.0F, 80.0F);
                              endPos.x = (int)endPos.x - (int)endPos.x % 16;
                              endPos.y = (int)endPos.y - (int)endPos.y % 16;
                              Vector2 currPos = this.pos3;
                              currPos.set(startPos);

                              while (currPos.y <= endPos.y) {
                                 Tile tile = Pokemon.this.mapTiles.get(currPos);
                                 Pokemon pokemon = game.map.pokemon.get(currPos);
                                 currPos.x += 16.0F;
                                 if (currPos.x > endPos.x) {
                                    currPos.x = startPos.x;
                                    currPos.y += 16.0F;
                                 }

                                 if (tile != null && pokemon != null && pokemon != Pokemon.this) {
                                    if (pokemon.baseSpecie() == null) {
                                       System.out.println("pokemon.specie.name");
                                       System.out.println(pokemon.specie.name);
                                    }

                                    if (Pokemon.this.baseSpecie() == null) {
                                       System.out.println("Pokemon.this.specie.name");
                                       System.out.println(Pokemon.this.specie.name);
                                    }

                                    boolean sameBaseSpecie = pokemon.baseSpecie().equals(Pokemon.this.baseSpecie());
                                    if (Pokemon.this.specie.name.equals("tauros")) {
                                       sameBaseSpecie = sameBaseSpecie || pokemon.baseSpecie().equals("miltank");
                                    } else if (Pokemon.this.specie.name.equals("miltank")) {
                                       sameBaseSpecie = sameBaseSpecie || pokemon.baseSpecie().equals("tauros");
                                    } else if (Pokemon.this.baseSpecie().equals("nidoran_f")) {
                                       sameBaseSpecie = sameBaseSpecie || pokemon.baseSpecie().equals("nididoran_m");
                                    } else if (Pokemon.this.baseSpecie().equals("nidoran_m")) {
                                       sameBaseSpecie = sameBaseSpecie || pokemon.baseSpecie().equals("nididoran_f");
                                    }

                                    if (sameBaseSpecie && pokemon.mapTiles == Pokemon.this.mapTiles) {
                                       float dx = Math.abs(Pokemon.this.position.x - pokemon.position.x);
                                       float dy = Math.abs(Pokemon.this.position.y - pokemon.position.y);
                                       String nearbyDir;
                                       if (dx < dy) {
                                          if (pokemon.position.y < Pokemon.this.position.y) {
                                             nearbyDir = "down";
                                          } else {
                                             nearbyDir = "up";
                                          }
                                       } else if (pokemon.position.x < Pokemon.this.position.x) {
                                          nearbyDir = "left";
                                       } else {
                                          nearbyDir = "right";
                                       }

                                       this.nearbyDir2 = nearbyDir;
                                       if (this.nearbyDir == null) {
                                          this.nearbyDir = nearbyDir;
                                       }
                                    }
                                 }
                              }

                              this.moveTimer = game.map.rand.nextInt(180) + 60;
                           } else {
                              this.moveTimer = 1;
                           }

                           String[] dirs = new String[]{"up", "down", "left", "right"};
                           Pokemon.this.dirFacing = dirs[game.map.rand.nextInt(dirs.length)];
                           if (this.nearbyDir != null && Game.rand.nextInt(100) < 30) {
                              if (this.nearbyDir2 != null && Game.rand.nextInt(100) < 50) {
                                 Pokemon.this.dirFacing = this.nearbyDir2;
                              } else {
                                 Pokemon.this.dirFacing = this.nearbyDir;
                              }
                           }

                           Vector2 newPos = Pokemon.this.facingPos(this.pos1);
                           Tile temp = Pokemon.this.mapTiles.get(newPos);
                           boolean collidesWithPlayer = false;

                           for (Player player : game.players.values()) {
                              if (newPos.equals(player.position)) {
                                 collidesWithPlayer = true;
                                 break;
                              }
                           }

                           if (temp == null) {
                              return;
                           }

                           Tile currTile = Pokemon.this.mapTiles.get(Pokemon.this.position);
                           if (Pokemon.this.inWater) {
                              boolean foundShore = false;

                              for (int i = 0; i < Pokemon.POSITIONS.length; i++) {
                                 Vector2 position = Pokemon.POSITIONS[i];
                                 Vector2 tilePosition = this.pos2.set(Pokemon.this.position).add(position);
                                 Tile tile = Pokemon.this.mapTiles.get(tilePosition);
                                 if (tile != null && !tile.name.contains("water")) {
                                    foundShore = true;
                                    break;
                                 }
                              }

                              if (foundShore) {
                                 foundShore = false;

                                 for (int i = 0; i < Pokemon.POSITIONS.length; i++) {
                                    Vector2 position = Pokemon.POSITIONS[i];
                                    Vector2 tilePosition = this.pos2.set(newPos).add(position);
                                    Tile tile = Pokemon.this.mapTiles.get(tilePosition);
                                    if (tile != null && !tile.name.contains("water")) {
                                       foundShore = true;
                                       break;
                                    }
                                 }

                                 if (!foundShore || !temp.name.contains("water") || game.map.pokemon.containsKey(newPos)) {
                                    return;
                                 }
                              }
                           }

                           boolean isSolid;
                           if (Pokemon.this.inWater) {
                              if (Pokemon.onlySwim.contains(Pokemon.this.specie.name)) {
                                 isSolid = temp == null || !temp.isWater;
                              } else {
                                 isSolid = temp != null && !temp.isWater && temp.isSolid;
                              }
                           } else {
                              isSolid = temp == null || temp.isSolid;
                           }

                           boolean isLedge = temp.isLedge
                              || currTile != null && currTile.isLedge && currTile.ledgeDir().equals("up") && Pokemon.this.dirFacing.equals("up");
                           if (isSolid
                              || isLedge
                              || temp.name.contains("door")
                              || temp.nameUpper.contains("gate")
                              || temp.nameUpper.contains("door")
                              || game.map.pokemon.containsKey(newPos)
                              || game.player.position.equals(newPos)
                              || collidesWithPlayer) {
                              return;
                           }

                           game.actionStack.remove(this);
                           Action action = Pokemon.this.new Moving(2, 1.0F, true, null);
                           action.append(this);
                           game.insertAction(action);
                           Pokemon.this.standingAction = action;
                        }

                        this.moveTimer--;
                     }
                  }
               }
            }
         }
      }

      public void remoteStep(Game game) {
         Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
         if (Pokemon.this.shouldMove) {
            Pokemon.this.shouldMove = false;
            game.actionStack.remove(this);
            Action action = Pokemon.this.new Moving(2, 1.0F, true, null);
            action.append(this);
            game.insertAction(action);
            Pokemon.this.standingAction = action;
         }
      }

      @Override
      public void step(Game game) {
         if (this.isEgg || Pokemon.this.isEgg) {
            this.localStep(game);
            return;
         }

         if (game.coop != null && (game.coop.isGuest() || game.coop.reserved(Pokemon.this) || game.coop.managesAggression(Pokemon.this)) && game.coop.isWorldCreature(Pokemon.this)) {
            Pokemon.this.currOwSprite = Pokemon.this.standingSprites.get(Pokemon.this.dirFacing);
            return;
         }
         if (Pokemon.this.type == Player.Type.LOCAL) {
            this.localStep(game);
         } else {
            this.remoteStep(game);
         }
      }
   }

   public class UnPause extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      int moveTimer = 0;

      public UnPause() {
         super();
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void firstStep(Game game) {
         if (game.type == Game.Type.CLIENT) {
            game.client.sendTCP(new Network.PausePokemon(game.player.network.id, Pokemon.this.position, false));
         }

         game.actionStack.remove(this);
      }
   }
}
