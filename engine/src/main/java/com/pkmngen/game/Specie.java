package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Specie {
   public static HashMap<String, Texture> textures = new HashMap<>();
   public static HashMap<String, Map<String, String>> gen2Evos = new HashMap<>();
   public static HashMap<String, Map<Integer, String[]>> gen2Attacks = new HashMap<>();
   public static HashMap<String, ArrayList<String>> eggMoves = new HashMap<>();
   public static ArrayList<String> creditedPokemon = new ArrayList<>();
   public static HashMap<String, Specie> species;
   String name;
   String dexNumber;
   String genderRatio;
   String[] eggGroups = new String[2];
   int baseHappiness = 70;
   int eggCycles = 0;
   ArrayList<String> types;
   String growthRateGroup = "";
   Map<Integer, String[]> learnSet = new HashMap<>();
   ArrayList<String> hms = new ArrayList<>();
   Map<String, Integer> baseStats = new HashMap<>();
   Pokemon.Generation generation;
   String modNickname = null;
   public Integer cryLengthInFrames = null;
   SpriteProxy sprite;
   SpriteProxy backSprite;
   SpriteProxy spriteShiny;
   SpriteProxy backSpriteShiny;
   static SpriteProxy spriteGhost;
   static SpriteProxy spriteEgg;
   static SpriteProxy backSpriteEgg;
   ArrayList<SpriteProxy> introAnim;
   ArrayList<SpriteProxy> introAnimShiny;
   static ArrayList<SpriteProxy> introAnimGhost;
   static ArrayList<SpriteProxy> introAnimEgg;
   public int spriteOffsetY = 0;
   public Map<String, Sprite> standingSprites = new HashMap<>();
   public Map<String, Sprite> movingSprites = new HashMap<>();
   public Map<String, Sprite> altMovingSprites = new HashMap<>();
   ArrayList<Sprite> avatarSprites = new ArrayList<>();
   public Map<String, Sprite> standingSpritesEgg = new HashMap<>();
   public Map<String, Sprite> movingSpritesEgg = new HashMap<>();
   public Map<String, Sprite> altMovingSpritesEgg = new HashMap<>();
   ArrayList<Sprite> avatarSpritesEgg = new ArrayList<>();
   public Map<String, Sprite> surfSprites = null;
   public Map<String, Sprite> surfMovingSprites = null;
   public int harvestTimerMax = 9000;
   public ArrayList<String> harvestables = new ArrayList<>();
   public ArrayList<String> habitats;

   public int cryLengthInFrames() {
      if (this.cryLengthInFrames == null) {
         this.cryLengthInFrames = cryLengthInFrames("pokemon/cries/" + this.dexNumber);
      }

      return this.cryLengthInFrames;
   }

   public static int cryLengthInFrames(String path) {
      String fileName = path + ".length";
      FileHandle file = Game.checkForMods(fileName);
      Reader reader = file.reader();
      BufferedReader br = new BufferedReader(reader);

      try {
         String line;
         if ((line = br.readLine()) != null) {
            return (int)(Float.valueOf(line) * 60.0F) + 1;
         }
      } catch (NumberFormatException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return 0;
   }

   Specie(String name) {
      this(name, Pokemon.Generation.CRYSTAL);
   }

   Specie(String name, Pokemon.Generation gen) {
      this.harvestables.add("manure");
      this.habitats = new ArrayList<>();
      this.habitats.add("green");
      this.init(name, gen);
   }

   public void init(String n, Pokemon.Generation generation) {
      this.name = n.toLowerCase(Locale.ROOT);
      this.generation = generation;
      this.types = new ArrayList<>();
      this.learnSet = new HashMap<>();
      if (generation.equals(Pokemon.Generation.CRYSTAL)) {
         this.dexNumber = Pokemon.nameToIndex(this.name);
         this.loadFromAsm();
         this.loadOverworldSprites();
         if (this.name.equals("sneasel")
            || this.name.equals("weavile")
            || this.name.equals("scyther")
            || this.name.equals("scizor")
            || this.name.equals("krabby")
            || this.name.equals("kingler")
            || this.name.equals("pinsir")
            || this.name.equals("corphish")
            || this.name.equals("crawdaunt")
            || this.name.equals("absol")) {
            this.hms.add("CUT");
         }

         if (this.name.equals("riolu") || this.name.equals("lucario")) {
            this.hms.add("SMASH");
         }

         if (this.name.equals("smeargle")) {
            this.hms.add("PAINT");
         }

         if (this.types.contains("GROUND") || this.name.contains("tyranitar")) {
            this.hms.add("DIG");
         }

         if (this.types.contains("ELECTRIC") || this.name.contains("porygon")) {
            this.hms.add("POWER");
         }

         if (this.types.contains("GRASS")) {
            this.hms.add("CUT");
         }

         if (this.types.contains("ROCK")) {
            this.hms.add("SMASH");
         }

         if (this.types.contains("FIGHTING")) {
            this.hms.add("BUILD");
         }

         if (this.types.contains("FAIRY")) {
            this.hms.add("CHARM");
         }

         if (this.types.contains("POISON")) {
            this.hms.add("REPEL");
         }

         if (this.types.contains("FIRE")
            || this.name.equals("chinchou")
            || this.name.equals("lanturn")
            || this.name.equals("mareep")
            || this.name.equals("flaaffy")
            || this.name.equals("ampharos")
            || this.name.equals("volbeat")) {
            this.hms.add("FLASH");
         }

         if (this.name.equals("squirtle")
            || this.name.equals("wartortle")
            || this.name.equals("blastoise")
            || this.name.equals("slowpoke")
            || this.name.equals("slowbro")
            || this.name.equals("slowking")
            || this.name.equals("seel")
            || this.name.equals("dewgong")
            || this.name.equals("drowzee")
            || this.name.equals("hypno")
            || this.name.equals("cubone")
            || this.name.equals("marowak")
            || this.name.equals("kangaskhan")
            || this.name.equals("munchlax")
            || this.name.equals("snorlax")
            || this.name.equals("snubbull")
            || this.name.equals("granbull")
            || this.name.equals("miltank")
            || this.name.equals("aron")
            || this.name.equals("lairon")
            || this.name.equals("aggron")
            || this.name.equals("bagon")
            || this.name.equals("shelgon")
            || this.name.equals("salamence")
            || this.name.equals("bidoof")
            || this.name.equals("bibarel")
            || this.name.equals("cranidos")
            || this.name.equals("rampardos")
            || this.name.equals("darumaka")
            || this.name.equals("darmanitan")
            || this.name.equals("darmanitanzen")
            || this.name.equals("scraggy")
            || this.name.equals("scrafty")
            || this.name.equals("elgyem")
            || this.name.equals("beheeyem")
            || this.name.equals("scorbunny")
            || this.name.equals("raboot")
            || this.name.equals("cinderace")
            || this.name.equals("wooloo")
            || this.name.equals("dubwool")
            || this.name.equals("obstagoon")
            || this.name.equals("nidoran_m")
            || this.name.equals("nidoran_f")
            || this.name.equals("nidorina")
            || this.name.equals("nidorino")
            || this.name.equals("nidoking")
            || this.name.equals("nidoqueen")
            || this.name.equals("whismur")
            || this.name.equals("loudred")
            || this.name.equals("exploud")
            || this.name.equals("heracross")
            || this.name.equals("shieldon")
            || this.name.equals("bastiodon")
            || this.name.equals("teddiursa")
            || this.name.equals("ursaring")
            || this.name.equals("tauros")) {
            this.hms.add("HEADBUTT");
         }

         if (this.name.equals("stantler")
            || this.name.equals("ponyta")
            || this.name.equals("arcanine")
            || this.name.equals("donphan")
            || this.name.equals("girafarig")
            || this.name.equals("houndoom")
            || this.name.equals("rapidash")
            || this.name.equals("tauros")
            || this.name.equals("ninetales")
            || this.name.equals("piloswine")
            || this.name.equals("mamoswine")
            || this.name.equals("dodrio")
            || this.name.equals("mightyena")
            || this.name.equals("kangaskhan")
            || this.name.equals("persian")
            || this.name.equals("onix")
            || this.name.equals("steelix")
            || this.name.equals("haunter")
            || this.name.equals("rhyhorn")
            || this.name.equals("rhydon")
            || this.name.equals("rhyperior")
            || this.name.equals("bastiodon")
            || this.name.equals("camerupt")
            || this.name.equals("probopass")
            || this.name.equals("luxray")
            || this.name.equals("absol")) {
            this.hms.add("RIDE");
         }

         if (this.name.equals("pidgeot")
            || this.name.equals("aerodactyl")
            || this.name.equals("charizard")
            || this.name.equals("dragonair")
            || this.name.equals("dragonite")
            || this.name.equals("salamence")
            || this.name.equals("ho_oh")
            || this.name.equals("lugia")
            || this.name.equals("skarmory")
            || this.name.equals("articuno")
            || this.name.equals("zapdos")
            || this.name.equals("moltres")
            || this.name.equals("crobat")
            || this.name.equals("noctowl")
            || this.name.equals("xatu")
            || this.name.equals("flygon")
            || this.name.equals("togekiss")
            || this.name.equals("swellow")
            || this.name.equals("pelipper")
            || this.name.equals("altaria")
            || this.name.equals("rayquaza")
            || this.name.equals("drifblim")
            || this.name.equals("honchkrow")
            || this.name.equals("yanmega")
            || this.name.equals("garchomp")
            || this.name.equals("volcarona")
            || this.name.equals("fearow")
            || this.name.equals("mewtwo")
            || this.name.equals("mantine")
            || this.name.equals("toucannon")
            || this.name.equals("noivern")) {
            this.hms.add("FLY");
         }

         if (this.types.contains("DARK")) {
            this.hms.add("ATTACK");
         }

         if (this.types.contains("PSYCHIC")) {
            this.hms.add("TELEPORT");
         }

         boolean hasEvo = !gen2Evos.get(this.name).isEmpty();
         if (this.name.equals("rhydon")
            || this.name.equals("rhyperior")
            || this.name.equals("pikachu")
            || this.name.equals("raichu")
            || this.name.equals("araichu")
            || this.name.equals("aggron")
            || this.types.contains("WATER") && !hasEvo) {
            this.hms.add("SURF");
         }

         this.initHabitatValues();
      }
   }

   void loadFromAsm() {
      String newName = this.name;
      if (this.name.contains("unown")) {
         newName = "unown";
      }

      if (this.name.equals("combee_female")) {
         newName = "combee";
      }

      String path = "";
      if (creditedPokemon.contains(newName)) {
         path = "credited/";
      }

      boolean doingMod = true;
      FileHandle file = Gdx.files.local("mods/pokemon/" + newName + "/base_stats.asm");
      if (!file.exists()) {
         doingMod = false;
         file = Gdx.files.internal("pokemon/" + path + "base_stats/" + newName + ".asm");
      }

      try {
         Reader reader = file.reader();
         BufferedReader br = new BufferedReader(reader);

         String line;
         for (int lineNum = 0; (line = br.readLine()) != null; lineNum++) {
            if (doingMod && lineNum == 0) {
               this.modNickname = line.split("db")[1].split(";")[0].trim();
            }

            if (lineNum == 2) {
               String[] stats = line.split("db")[1].split(",");
               this.baseStats.put("hp", Integer.valueOf(stats[0].replace(" ", "")));
               this.baseStats.put("attack", Integer.valueOf(stats[1].replace(" ", "")));
               this.baseStats.put("defense", Integer.valueOf(stats[2].replace(" ", "")));
               this.baseStats.put("speed", Integer.valueOf(stats[3].replace(" ", "")));
               this.baseStats.put("specialAtk", Integer.valueOf(stats[4].replace(" ", "")));
               this.baseStats.put("specialDef", Integer.valueOf(stats[5].replace(" ", "")));
            } else if (lineNum != 5) {
               if (lineNum == 6) {
                  String catchRate = line.split("db ")[1].split(" ;")[0];
                  this.baseStats.put("catchRate", Integer.valueOf(catchRate));
               } else if (lineNum == 7) {
                  String baseExp = line.split("db ")[1].split(" ;")[0];
                  this.baseStats.put("baseExp", Integer.valueOf(baseExp));
               } else if (lineNum == 9) {
                  this.genderRatio = line.split("db ")[1].split(" ;")[0];
               } else if (lineNum == 11) {
                  String eggCycles = line.split("db ")[1].split(" ;")[0];
                  this.eggCycles = Integer.valueOf(eggCycles);
               } else if (lineNum == 15) {
                  this.growthRateGroup = line.split("db ")[1].split(" ;")[0];
               } else if (lineNum == 16) {
                  String groups = line.split("dn ")[1].split(" ;")[0];
                  this.eggGroups = groups.split(", ");

                  for (int i = 0; i < this.eggGroups.length; i++) {
                     if (this.eggGroups[i].equals("EGG_GROUND")) {
                        this.eggGroups[i] = "EGG_FIELD";
                     }
                  }
               }
            } else {
               String[] types = line.split("db ")[1].split(" ; ")[0].split(", ");
               String prevType = "NORMAL";

               for (String type : types) {
                  if (Game.fairyTypeEnabled || !type.equals("FAIRY")) {
                     prevType = type;
                     this.types.add(type);
                  }
               }

               if (this.types.isEmpty()) {
                  this.types.add("NORMAL");
               }

               if (this.types.size() < 2) {
                  this.types.add(prevType);
               }
            }
         }

         reader.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      if (!textures.containsKey(this.name + "_front")) {
         file = Gdx.files.local("mods/pokemon/" + this.name + "/front.png");
         if (!file.exists()) {
            file = Gdx.files.internal("pokemon/" + path + "pokemon/" + this.name + "/front.png");
         }

         Texture text = TextureCache.get(file);
         if (this.name.contains("unown") && !this.name.equals("unown_!") && !this.name.equals("unown_qmark")) {
            TextureData temp = text.getTextureData();
            if (!temp.isPrepared()) {
               temp.prepare();
            }

            Pixmap currPixmap = temp.consumePixmap();
            Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Format.RGBA8888);
            newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
            newPixmap.fill();
            int i = 0;

            for (int j = 0; j < text.getHeight(); i++) {
               if (i > text.getWidth()) {
                  i = -1;
                  j++;
               } else {
                  Color color = new Color(currPixmap.getPixel(i, j));
                  newPixmap.drawPixel(i, j, Color.rgba8888(color.a, color.a, color.a, 1.0F));
               }
            }

            text = new Texture(newPixmap);
         }

         textures.put(this.name + "_front", text);
         Color normalColor1 = null;
         Color normalColor2 = new Color();
         Color shinyColor1 = null;
         Color shinyColor2 = new Color();
         if (creditedPokemon.contains(newName)) {
            path = "credited/";
         }

         try {
            file = Gdx.files.local("mods/pokemon/" + newName + "/shiny.pal");
            if (!file.exists()) {
               file = Gdx.files.internal("pokemon/" + path + "pokemon/" + newName + "/shiny.pal");
            } else {
               System.out.println("Found mods for: " + newName);
            }

            Reader reader = file.reader();
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
               if (line.contains("RGB")) {
                  String[] vals = line.split("\tRGB ")[1].split(", ");
                  if (shinyColor1 == null) {
                     shinyColor1 = new Color();
                     shinyColor1.r = Float.valueOf(vals[0]) * 8.0F / 256.0F;
                     shinyColor1.g = Float.valueOf(vals[1]) * 8.0F / 256.0F;
                     shinyColor1.b = Float.valueOf(vals[2]) * 8.0F / 256.0F;
                     shinyColor1.a = 1.0F;
                  } else {
                     shinyColor2.r = Float.valueOf(vals[0]) * 8.0F / 256.0F;
                     shinyColor2.g = Float.valueOf(vals[1]) * 8.0F / 256.0F;
                     shinyColor2.b = Float.valueOf(vals[2]) * 8.0F / 256.0F;
                     shinyColor2.a = 1.0F;
                  }
               }
            }

            reader.close();
            SpriteProxy tempSprite = new SpriteProxy(textures.get(this.name + "_front"), 0, 0, text.getWidth(), text.getWidth());
            if (tempSprite.color1.r + tempSprite.color1.g + tempSprite.color1.b == tempSprite.color2.r + tempSprite.color2.g + tempSprite.color2.b) {
               System.out.println("TODO: verify that this Pokemon's shiny is correct: " + this.name);
            }

            normalColor1 = new Color();
            normalColor1.r = tempSprite.color1.r * 32.0F;
            normalColor1.g = tempSprite.color1.g * 32.0F;
            normalColor1.b = tempSprite.color1.b * 32.0F;
            normalColor2 = new Color();
            normalColor2.r = tempSprite.color2.r * 32.0F;
            normalColor2.g = tempSprite.color2.g * 32.0F;
            normalColor2.b = tempSprite.color2.b * 32.0F;
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }

         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         int i = 0;

         for (int j = 0; j < text.getHeight(); i++) {
            if (i > text.getWidth()) {
               i = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(i, j));
               if ((int)(color.r * 32.0F) == (int)normalColor1.r
                  && (int)(color.g * 32.0F) == (int)normalColor1.g
                  && (int)(color.b * 32.0F) == (int)normalColor1.b) {
                  color = shinyColor1;
               } else if ((int)(color.r * 32.0F) == (int)normalColor2.r
                  && (int)(color.g * 32.0F) == (int)normalColor2.g
                  && (int)(color.b * 32.0F) == (int)normalColor2.b) {
                  color = shinyColor2;
               }

               newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, color.a));
            }
         }

         text = TextureCache.get(newPixmap);
         textures.put(this.name + "_front_shiny", text);
         file = Gdx.files.local("mods/pokemon/" + this.name + "/back.png");
         if (!file.exists()) {
            file = Gdx.files.internal("pokemon/" + path + "pokemon/" + this.name + "/back.png");
         }

         text = TextureCache.get(file);
         textures.put(this.name + "_back", text);
         temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         currPixmap = temp.consumePixmap();
         newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         i = 0;

         for (int j = 0; j < text.getHeight(); i++) {
            if (i > text.getWidth()) {
               i = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(i, j));
               if ((int)(color.r * 32.0F) == (int)normalColor1.r
                  && (int)(color.g * 32.0F) == (int)normalColor1.g
                  && (int)(color.b * 32.0F) == (int)normalColor1.b) {
                  color = shinyColor1;
               } else if ((int)(color.r * 32.0F) == (int)normalColor2.r
                  && (int)(color.g * 32.0F) == (int)normalColor2.g
                  && (int)(color.b * 32.0F) == (int)normalColor2.b) {
                  color = shinyColor2;
               }

               newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, color.a));
            }
         }

         text = TextureCache.get(newPixmap);
         textures.put(this.name + "_back_shiny", text);
      }

      Texture pokemonText = textures.get(this.name + "_front");
      int height = pokemonText.getWidth();
      this.sprite = new SpriteProxy(pokemonText, 0, 0, height, height);
      pokemonText = textures.get(this.name + "_back");
      this.backSprite = new SpriteProxy(pokemonText, 0, 0, 48, 48);
      pokemonText = textures.get(this.name + "_front_shiny");
      height = pokemonText.getWidth();
      this.spriteShiny = new SpriteProxy(pokemonText, 0, 0, height, height);
      pokemonText = textures.get(this.name + "_back_shiny");
      this.backSpriteShiny = new SpriteProxy(pokemonText, 0, 0, 48, 48);
      this.introAnim = new ArrayList<>();
      this.introAnimShiny = new ArrayList<>();

      try {
         file = Gdx.files.local("mods/pokemon/" + this.name + "/anim.asm");
         if (!file.exists()) {
            file = Gdx.files.internal("pokemon/" + path + "pokemon/" + this.name + "/anim.asm");
         }

         int setrepeat = 0;
         ArrayList<String> lines = Game.readLines(file);
         int i = 0;

         while (i < lines.size()) {
            String line = lines.get(i);
            if (line.contains("setrepeat")) {
               setrepeat = Integer.valueOf(line.split("setrepeat ")[1]);
            } else if (line.contains("frame")) {
               String[] vals = line.split("frame ")[1].split(", ");
               int numFrames = Integer.valueOf(vals[1].trim());
               int frame = Integer.valueOf(vals[0]);

               for (int j = 0; j < numFrames; j++) {
                  pokemonText = textures.get(this.name + "_front");
                  SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height * frame, height, height);
                  this.introAnim.add(sprite);
                  pokemonText = textures.get(this.name + "_front_shiny");
                  sprite = new SpriteProxy(pokemonText, 0, height * frame, height, height);
                  this.introAnimShiny.add(sprite);
               }
            } else if (line.contains("dorepeat") && setrepeat != 0) {
               i = Integer.valueOf(line.split("dorepeat ")[1]);
               setrepeat--;
               continue;
            }

            i++;
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      if (creditedPokemon.contains(newName)) {
         path = "credited/";
      }

      if (!gen2Attacks.containsKey(newName) || !gen2Evos.containsKey(newName)) {
         Map<Integer, String[]> attacks = new HashMap<>();
         Map<String, String> evos = new HashMap<>();
         boolean inSection = true;
         doingMod = false;
         file = Gdx.files.local("mods/pokemon/" + newName + "/evos_attacks.asm");
         if (!file.exists()) {
            inSection = false;
            doingMod = true;
            file = Gdx.files.internal("pokemon/" + path + "evos_attacks.asm");
         }

         try {
            ArrayList<String> lines = Game.readLines(file);

            for (int i = 0; i < lines.size(); i++) {
               String line = lines.get(i);
               if (line.toLowerCase(Locale.ROOT).equals(newName + "evosattacks:")) {
                  inSection = true;
               } else if (inSection && !line.contains(";")) {
                  if (doingMod && line.contains("EVOLVE")) {
                     String[] vals = line.split(", ");
                     String baseMon = Pokemon.baseSpecies.get(newName);
                     if (baseMon == null) {
                        baseMon = newName;
                     }

                     Pokemon.baseSpecies.put(vals[2].toLowerCase(Locale.ROOT), baseMon);
                  }

                  if (line.contains("EVOLVE_LEVEL")) {
                     String[] vals = line.split(", ");
                     evos.put(vals[1], vals[2].toLowerCase(Locale.ROOT));
                  } else if (line.contains("EVOLVE_ITEM")
                     || line.contains("EVOLVE_TRADE")
                     || line.contains("EVOLVE_HAPPINESS")
                     || line.contains("EVOLVE_MOVE")
                     || line.contains("EVOLVE_INPARTY")
                     || line.contains("EVOLVE_STAT")) {
                     String[] vals = line.split(", ");
                     evos.put(vals[1].toLowerCase(Locale.ROOT).replace("_", " "), vals[2].toLowerCase(Locale.ROOT));
                  } else if (!line.contains("\t")) {
                     inSection = false;
                  } else if (!line.contains("db 0")) {
                     String[] vals = line.split(", ");
                     String attack = vals[1].toLowerCase(Locale.ROOT).replace('_', ' ');
                     if (Pokemon.attacksImplemented.contains(attack)) {
                        int level = Integer.valueOf(vals[0].split(" ")[1]);
                        String[] attacksArray = new String[]{attack};
                        if (attacks.containsKey(level)) {
                           String[] oldArray = attacks.get(level);
                           attacksArray = new String[oldArray.length + 1];

                           for (int j = 0; j < oldArray.length; j++) {
                              attacksArray[j] = oldArray[j];
                           }

                           attacksArray[oldArray.length] = attack;
                        }

                        attacks.put(level, attacksArray);
                     }
                  }
               }
            }
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }

         gen2Attacks.put(newName, attacks);
         if (newName.equals("combee")) {
            gen2Evos.put(newName, new HashMap<>());
            gen2Evos.put("combee_female", evos);
         } else {
            gen2Evos.put(this.name, evos);
         }

         file = Gdx.files.local("mods/pokemon/" + newName + "/egg_moves.asm");
         if (file.exists()) {
            try {
               Reader reader = file.reader();
               BufferedReader br = new BufferedReader(reader);
               Pokemon.eggMoves.put(newName, new ArrayList<>());

               String line;
               while ((line = br.readLine()) != null) {
                  if (!line.contains(";") && line.contains("\t")) {
                     Pokemon.eggMoves.get(newName).add(line.split("db ")[1].trim().toLowerCase(Locale.ROOT).replace("_", " "));
                  }
               }

               reader.close();
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }

      this.learnSet = gen2Attacks.get(newName);
   }

   void loadOverworldSprites() {
      if (this.name.equals("pikachu") || this.name.equals("raichu")) {
         Texture text = TextureCache.get(Gdx.files.internal("pokemon/" + this.name + "_surf.png"));
         int row = 0;
         int col = 0;
         this.surfSprites = new HashMap<>();
         this.surfMovingSprites = new HashMap<>();
         this.surfMovingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
         this.surfMovingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.surfMovingSprites.get("right").flip(true, false);
         this.surfSprites.put("left", new Sprite(text, ++col * 16, row * 16, 16, 16));
         this.surfSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.surfSprites.get("right").flip(true, false);
         this.surfMovingSprites.put("up", new Sprite(text, ++col * 16, row * 16, 16, 16));
         this.surfSprites.put("up", new Sprite(text, ++col * 16, row * 16, 16, 16));
         this.surfSprites.put("down", new Sprite(text, ++col * 16, row * 16, 16, 16));
         this.surfMovingSprites.put("down", new Sprite(text, ++col * 16, row * 16, 16, 16));
      }

      FileHandle filehandle = Gdx.files.local("mods/pokemon/" + this.name + "/overworld.png");
      if (filehandle.exists()) {
         boolean flip = true;
         Texture text = TextureCache.get(filehandle);
         int row = 0;
         int col = 0;
         this.movingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
         this.movingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.movingSprites.get("right").flip(true, false);
         this.altMovingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.get("right").flip(true, false);
         this.standingSprites.put("left", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.standingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.standingSprites.get("right").flip(true, false);
         this.movingSprites.put("up", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.altMovingSprites.put("up", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.get("up").flip(true, false);
         this.standingSprites.put("up", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.movingSprites.put("down", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.altMovingSprites.put("down", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.get("down").flip(true, false);
         this.standingSprites.put("down", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.avatarSprites.add(this.standingSprites.get("down"));
         this.avatarSprites.add(this.movingSprites.get("down"));
         this.avatarSprites.add(this.standingSprites.get("down"));
         this.avatarSprites.add(this.altMovingSprites.get("down"));

         for (String key : new ArrayList<>(this.standingSprites.keySet())) {
            this.standingSprites.put(key + "_running", this.standingSprites.get(key));
         }

         for (String key : new ArrayList<>(this.movingSprites.keySet())) {
            this.movingSprites.put(key + "_running", this.movingSprites.get(key));
         }

         for (String key : new ArrayList<>(this.altMovingSprites.keySet())) {
            this.altMovingSprites.put(key + "_running", this.altMovingSprites.get(key));
         }

         this.loadEggTextures("mods/pokemon/" + this.name + "/back.png");
      } else if (this.name.equals("aexeggutor")) {
         Texture text = TextureCache.get(Gdx.files.internal("pokemon/" + this.name + "_ow.png"));
         int row = 0;
         int col = 0;
         this.movingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 48));
         this.altMovingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 48));
         this.movingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 48));
         this.movingSprites.get("right").flip(true, false);
         this.altMovingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 48));
         this.altMovingSprites.get("right").flip(true, false);
         this.standingSprites.put("left", new Sprite(text, ++col * 16, row * 16, 16, 48));
         this.standingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 48));
         this.standingSprites.get("right").flip(true, false);
         this.movingSprites.put("up", new Sprite(text, ++col * 16, row * 16, 16, 48));
         this.altMovingSprites.put("up", new Sprite(text, col * 16, row * 16, 16, 48));
         this.altMovingSprites.get("up").flip(true, false);
         this.standingSprites.put("up", new Sprite(text, ++col * 16, row * 16, 16, 48));
         this.standingSprites.put("down", new Sprite(text, ++col * 16, row * 16, 16, 48));
         this.movingSprites.put("down", new Sprite(text, ++col * 16, row * 16, 16, 48));
         this.altMovingSprites.put("down", new Sprite(text, col * 16, row * 16, 16, 48));
         this.altMovingSprites.get("down").flip(true, false);
         this.avatarSprites.add(this.standingSprites.get("down"));
         this.avatarSprites.add(this.movingSprites.get("down"));
         this.avatarSprites.add(this.standingSprites.get("down"));
         this.avatarSprites.add(this.altMovingSprites.get("down"));

         for (String key : new ArrayList<>(this.standingSprites.keySet())) {
            this.standingSprites.put(key + "_running", this.standingSprites.get(key));
         }

         for (String key : new ArrayList<>(this.movingSprites.keySet())) {
            this.movingSprites.put(key + "_running", this.movingSprites.get(key));
         }

         for (String key : new ArrayList<>(this.altMovingSprites.keySet())) {
            this.altMovingSprites.put(key + "_running", this.altMovingSprites.get(key));
         }

         this.loadEggTextures("pokemon/credited/pokemon/" + this.name + "/back.png");
      } else if (this.name.contains("unown")) {
         Texture text = TextureCache.get(Gdx.files.internal("pokemon/unown_ow.png"));
         String alphabet_lower = "abcdefghijklmnopqrstuvwxyz!";
         String suffix = this.name.split("_")[1];
         int col = alphabet_lower.indexOf(suffix);
         int row = 0;
         if (suffix.equals("qmark")) {
            col = 27;
         }

         this.standingSprites.put("down", new Sprite(text, col * 16, row * 16, 16, 16));
         this.movingSprites.put("down", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.put("down", new Sprite(text, col * 16, row * 16, 16, 16));
         this.standingSprites.put("up", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.movingSprites.put("up", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.put("up", new Sprite(text, col * 16, row * 16, 16, 16));
         this.standingSprites.put("left", new Sprite(text, col * 16, ++row * 16, 16, 16));
         this.movingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
         this.standingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.movingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.altMovingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
         this.standingSprites.get("right").flip(true, false);
         this.movingSprites.get("right").flip(true, false);
         this.altMovingSprites.get("right").flip(true, false);
         this.avatarSprites.add(this.standingSprites.get("down"));
         this.avatarSprites.add(this.movingSprites.get("down"));
         this.avatarSprites.add(this.standingSprites.get("down"));
         this.avatarSprites.add(this.altMovingSprites.get("down"));

         for (String key : new ArrayList<>(this.standingSprites.keySet())) {
            this.standingSprites.put(key + "_running", this.standingSprites.get(key));
         }

         for (String key : new ArrayList<>(this.movingSprites.keySet())) {
            this.movingSprites.put(key + "_running", this.movingSprites.get(key));
         }

         for (String key : new ArrayList<>(this.altMovingSprites.keySet())) {
            this.altMovingSprites.put(key + "_running", this.altMovingSprites.get(key));
         }
      } else {
         try {
            FileHandle file = Gdx.files.internal("pokemon/pokemon_overworld_adjustments.asm");
            ArrayList<String> poa_lines = Game.readLines(file);
            file = Gdx.files.internal("pokemon/pokemon_names.asm");
            ArrayList<String> lines = Game.readLines(file);
            boolean found = false;
            boolean flip = true;

            for (int i = 0; i < lines.size(); i++) {
               String line = lines.get(i);

               for (int z = 0; z < poa_lines.size(); z += 5) {
                  String poa_line = poa_lines.get(z);
                  if (poa_line.toLowerCase(Locale.ROOT).equals(this.name + ":")) {
                     String[] vals = poa_lines.get(z + 1).split(", ");
                     i = Integer.valueOf(vals[1]);
                     vals = poa_lines.get(z + 2).split(", ");
                     found = Boolean.valueOf(vals[1]);
                     vals = poa_lines.get(z + 3).split(", ");
                     flip = Boolean.valueOf(vals[1]);
                     break;
                  }
               }

               String currName = line.split("db \"")[1].split("\"")[0].toLowerCase(Locale.ROOT).replace("@", "");
               if (currName.equals(this.name) || found) {
                  found = true;
                  Texture text = TextureCache.get(Gdx.files.internal("pokemon/overworlds_sheet.png"));
                  int col = i * 6 % 156;
                  int row = i * 6 / 156;
                  this.spriteOffsetY = row * 16;
                  this.movingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
                  this.altMovingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
                  this.standingSprites.put("left", new Sprite(text, col * 16 + 16, row * 16, 16, 16));
                  this.movingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
                  this.movingSprites.get("right").flip(true, false);
                  this.altMovingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
                  this.altMovingSprites.get("right").flip(true, false);
                  this.standingSprites.put("right", new Sprite(text, col * 16 + 16, row * 16, 16, 16));
                  this.standingSprites.get("right").flip(true, false);
                  this.movingSprites.put("up", new Sprite(text, col * 16 + 32, row * 16, 16, 16));
                  this.altMovingSprites.put("up", new Sprite(text, col * 16 + 32, row * 16, 16, 16));
                  if (flip) {
                     this.altMovingSprites.get("up").flip(true, false);
                  }

                  this.standingSprites.put("up", new Sprite(text, col * 16 + 48, row * 16, 16, 16));
                  this.movingSprites.put("down", new Sprite(text, col * 16 + 64, row * 16, 16, 16));
                  this.altMovingSprites.put("down", new Sprite(text, col * 16 + 64, row * 16, 16, 16));
                  if (flip) {
                     this.altMovingSprites.get("down").flip(true, false);
                  }

                  this.standingSprites.put("down", new Sprite(text, col * 16 + 80, row * 16, 16, 16));
                  this.avatarSprites.add(this.standingSprites.get("down"));
                  this.avatarSprites.add(this.movingSprites.get("down"));
                  this.avatarSprites.add(this.standingSprites.get("down"));
                  this.avatarSprites.add(this.altMovingSprites.get("down"));

                  for (String key : new ArrayList<>(this.standingSprites.keySet())) {
                     this.standingSprites.put(key + "_running", this.standingSprites.get(key));
                  }

                  for (String key : new ArrayList<>(this.movingSprites.keySet())) {
                     this.movingSprites.put(key + "_running", this.movingSprites.get(key));
                  }

                  for (String key : new ArrayList<>(this.altMovingSprites.keySet())) {
                     this.altMovingSprites.put(key + "_running", this.altMovingSprites.get(key));
                  }
                  break;
               }
            }

            String path = "";
            if (creditedPokemon.contains(this.name)) {
               path = "credited/";
            }

            this.loadEggTextures("pokemon/" + path + "pokemon/" + this.name + "/back.png");
            if (found) {
               return;
            }

            filehandle = Gdx.files.internal("pokemon/" + path + "pokemon/" + this.name + "/overworld.png");
            if (filehandle.exists()) {
               flip = false;
               Texture text = TextureCache.get(filehandle);
               int row = 0;
               int col = 0;
               this.movingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
               this.altMovingSprites.put("left", new Sprite(text, col * 16, row * 16, 16, 16));
               this.altMovingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
               this.altMovingSprites.get("right").flip(true, false);
               this.movingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
               this.movingSprites.get("right").flip(true, false);
               this.standingSprites.put("left", new Sprite(text, col * 16, ++row * 16, 16, 16));
               this.standingSprites.put("right", new Sprite(text, col * 16, row * 16, 16, 16));
               this.standingSprites.get("right").flip(true, false);
               this.movingSprites.put("up", new Sprite(text, col * 16, ++row * 16, 16, 16));
               this.altMovingSprites.put("up", new Sprite(text, col * 16, row * 16, 16, 16));
               if (flip) {
                  this.altMovingSprites.get("up").flip(true, false);
               }

               this.standingSprites.put("up", new Sprite(text, col * 16, ++row * 16, 16, 16));
               this.movingSprites.put("down", new Sprite(text, col * 16, ++row * 16, 16, 16));
               this.altMovingSprites.put("down", new Sprite(text, col * 16, row * 16, 16, 16));
               if (flip) {
                  this.altMovingSprites.get("down").flip(true, false);
               }

               this.standingSprites.put("down", new Sprite(text, col * 16, ++row * 16, 16, 16));
               this.avatarSprites.add(this.standingSprites.get("down"));
               this.avatarSprites.add(this.movingSprites.get("down"));
               this.avatarSprites.add(this.standingSprites.get("down"));
               this.avatarSprites.add(this.altMovingSprites.get("down"));

               for (String key : new ArrayList<>(this.standingSprites.keySet())) {
                  this.standingSprites.put(key + "_running", this.standingSprites.get(key));
               }

               for (String key : new ArrayList<>(this.movingSprites.keySet())) {
                  this.movingSprites.put(key + "_running", this.movingSprites.get(key));
               }

               for (String key : new ArrayList<>(this.altMovingSprites.keySet())) {
                  this.altMovingSprites.put(key + "_running", this.altMovingSprites.get(key));
               }

               return;
            }

            Texture text = TextureCache.get(Gdx.files.internal("pokemon/crystal-overworld-sprites1.png"));
            int dexNumber = Integer.valueOf(this.dexNumber) - 1;
            if (this.name.equals("honchkrow")) {
               dexNumber = 197;
            }

            int col = dexNumber % 15 * 2;
            int row = dexNumber / 15;
            this.spriteOffsetY = 31 + row * 25;

            for (String dir : new String[]{"up", "down", "left", "right"}) {
               this.standingSprites.put(dir, new Sprite(text, 1 + col * 17, 31 + row * 25, 16, 16));
               this.movingSprites.put(dir, new Sprite(text, 1 + col * 17 + 17, 31 + row * 25, 16, 16));
               this.altMovingSprites.put(dir, new Sprite(text, 1 + col * 17 + 17, 31 + row * 25, 16, 16));
            }

            this.avatarSprites.add(this.standingSprites.get("down"));
            this.avatarSprites.add(this.movingSprites.get("down"));
            this.avatarSprites.add(this.standingSprites.get("down"));
            this.avatarSprites.add(this.movingSprites.get("down"));

            for (String key : new ArrayList<>(this.standingSprites.keySet())) {
               this.standingSprites.put(key + "_running", this.standingSprites.get(key));
            }

            for (String key : new ArrayList<>(this.movingSprites.keySet())) {
               this.movingSprites.put(key + "_running", this.movingSprites.get(key));
            }

            for (String key : new ArrayList<>(this.altMovingSprites.keySet())) {
               this.altMovingSprites.put(key + "_running", this.altMovingSprites.get(key));
            }
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   void loadEggTextures(String path) {
      if (!TextureCache.eggTextures.containsKey(this.name) && !this.name.equals("phione")) {
         Texture text = TextureCache.get(Gdx.files.internal(path));
         int height = text.getWidth();
         SpriteProxy tempSprite = new SpriteProxy(text, 0, 0, height, height);
         text = TextureCache.get(Gdx.files.internal("pokemon/egg1.png"));
         SpriteProxy tempEggSprite = new SpriteProxy(text, 0, 0, text.getWidth(), text.getHeight());
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         int i = 0;

         for (int j = 0; j < text.getHeight(); i++) {
            if (i > text.getWidth()) {
               i = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(i, j));
               if (color.r == tempEggSprite.color1.r && color.g == tempEggSprite.color1.g && color.b == tempEggSprite.color1.b) {
                  color.r = tempSprite.color1.r;
                  color.g = tempSprite.color1.g;
                  color.b = tempSprite.color1.b;
               } else if (color.r == tempEggSprite.color2.r && color.g == tempEggSprite.color2.g && color.b == tempEggSprite.color2.b) {
                  color.r = tempSprite.color2.r;
                  color.g = tempSprite.color2.g;
                  color.b = tempSprite.color2.b;
               }

               newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, color.a));
            }
         }

         TextureCache.eggTextures.put(this.name, TextureCache.get(newPixmap));
      } else if (!TextureCache.eggTextures.containsKey(this.name)) {
         Texture text = TextureCache.get(Gdx.files.internal("phione-egg.png"));
         TextureCache.eggTextures.put(this.name, text);
      }

      Texture text = TextureCache.eggTextures.get(this.name);

      for (String dir : new String[]{"up", "down", "left", "right"}) {
         this.standingSpritesEgg.put(dir, new Sprite(text, 0, 0, 16, 16));
         this.movingSpritesEgg.put(dir, new Sprite(text, 0, 32, 16, 16));
         this.altMovingSpritesEgg.put(dir, new Sprite(text, 0, 48, 16, 16));
      }

      Sprite hopSprite = new Sprite(text, 0, 16, 16, 16);
      this.avatarSpritesEgg.add(this.standingSpritesEgg.get("down"));
      this.avatarSpritesEgg.add(hopSprite);
      this.avatarSpritesEgg.add(this.standingSpritesEgg.get("down"));
      this.avatarSpritesEgg.add(hopSprite);
   }

   void initHabitatValues() {
      CreatureHabitat rules = new CreatureHabitat(this.name.toLowerCase(Locale.ROOT), this.types, Game.evoStones);
      this.harvestables.clear(); this.harvestables.addAll(rules.harvestables);
      this.habitats.clear(); this.habitats.addAll(rules.habitats);
      this.harvestTimerMax = rules.harvestTimerMax;
   }

   static {
      String[] temp = new String[]{
         "aggron",
         "aron",
         "exploud",
         "gardevoir",
         "hariyama",
         "kirlia",
         "lairon",
         "lombre",
         "lotad",
         "loudred",
         "ludicolo",
         "makuhita",
         "ralts",
         "taillow",
         "swellow",
         "whismur",
         "poochyena",
         "mightyena",
         "wingull",
         "pelipper",
         "shroomish",
         "breloom",
         "surskit",
         "masquerain",
         "sableye",
         "numel",
         "camerupt",
         "sylveon",
         "glaceon",
         "leafeon",
         "zigzagoon",
         "linoone",
         "maractus",
         "araichu",
         "phantump",
         "appletun",
         "trevenant",
         "pikipek",
         "trumbeak",
         "toucannon",
         "amarowak",
         "volbeat",
         "plusle",
         "minun",
         "floatzel",
         "buizel",
         "noibat",
         "noivern",
         "honchkrow",
         "gallade",
         "absol",
         "torkoal",
         "trapinch",
         "vibrava",
         "flygon",
         "duskull",
         "dusclops",
         "dusknoir",
         "skorupi",
         "drapion",
         "cacnea",
         "cacturne",
         "shinx",
         "luxio",
         "luxray",
         "gible",
         "gabite",
         "garchomp",
         "drifloon",
         "drifblim",
         "feebas",
         "milotic",
         "snorunt",
         "glalie",
         "froslass",
         "solrock",
         "lunatone",
         "beldum",
         "metang",
         "metagross",
         "magmortar",
         "electivire",
         "mamoswine",
         "mismagius",
         "rhyperior",
         "weavile",
         "yanmega",
         "anorith",
         "armaldo",
         "shieldon",
         "bastiodon",
         "lileep",
         "cradily",
         "cranidos",
         "rampardos",
         "chingling",
         "chimecho",
         "shuppet",
         "banette",
         "spiritomb",
         "riolu",
         "lucario",
         "aexeggutor",
         "dwebble",
         "crustle",
         "litwick",
         "lampent",
         "chandelure",
         "corphish",
         "crawdaunt",
         "mimikyu",
         "regieleki",
         "regidrago",
         "registeel",
         "regirock",
         "regice",
         "regigigas",
         "bronzor",
         "bronzong",
         "darumaka",
         "darmanitan",
         "darmanitanzen",
         "elgyem",
         "beheeyem",
         "sandile",
         "krokorok",
         "krookodile",
         "cutiefly",
         "ribombee",
         "combee",
         "combee_female",
         "vespiquen",
         "nosepass",
         "sigilyph",
         "snover",
         "abomasnow",
         "goomy",
         "swirlix",
         "zigzagoon",
         "larvesta",
         "volcarona",
         "mantyke",
         "phione",
         "finneon",
         "probopass",
         "gcorsola",
         "gyamask",
         "runerigus",
         "straigar",
         "ambipom",
         "gdarumaka",
         "gdarmanitan",
         "gdarmanitanzen",
         "pumpkaboo",
         "carvanha",
         "sharpedo",
         "sandygast",
         "palossand",
         "luvdisc",
         "duraludon",
         "snom",
         "jirachi",
         "frosmoth",
         "cosmog",
         "cosmoem",
         "deino",
         "zweilous",
         "hydreigon",
         "applin",
         "stonjourner"
      };

      for (String t : temp) {
         creditedPokemon.add(t);
      }

      if (Game.fairyTypeEnabled) {
         temp = new String[]{"azurill", "marill", "azumarill"};

         for (String t : temp) {
            creditedPokemon.add(t);
         }
      }

      species = new HashMap<>();
      Texture text = null;
      if (!textures.containsKey("egg_front")) {
         Texture var17 = TextureCache.get(Gdx.files.internal("pokemon/pokemon/egg/front.png"));
         textures.put("egg_front", var17);
         Texture var18 = TextureCache.get(Gdx.files.internal("pokemon/pokemon/egg/back.png"));
         textures.put("egg_back", var18);
      }

      if (!textures.containsKey("ghost_front")) {
         Texture var19 = TextureCache.get(Gdx.files.internal("pokemon/credited/pokemon/ghost/front.png"));
         textures.put("ghost_front", var19);
      }

      Texture pokemonText = textures.get("egg_front");
      int height = pokemonText.getWidth();
      spriteEgg = new SpriteProxy(pokemonText, 0, 0, height, height);
      pokemonText = textures.get("egg_back");
      backSpriteEgg = new SpriteProxy(pokemonText, 0, 0, 48, 48);
      pokemonText = textures.get("ghost_front");
      height = pokemonText.getWidth();
      spriteGhost = new SpriteProxy(pokemonText, 0, 0, height, height);
      introAnimGhost = new ArrayList<>();
      introAnimEgg = new ArrayList<>();

      try {
         FileHandle file = Gdx.files.internal("pokemon/credited/pokemon/ghost/anim.asm");
         int setrepeat = 0;
         ArrayList<String> lines = Game.readLines(file);
         int i = 0;

         while (i < lines.size()) {
            String line = lines.get(i);
            if (line.contains("setrepeat")) {
               setrepeat = Integer.valueOf(line.split("setrepeat ")[1]);
            } else if (line.contains("frame")) {
               String[] vals = line.split("frame ")[1].split(", ");
               int numFrames = Integer.valueOf(vals[1].trim());
               int frame = Integer.valueOf(vals[0]);

               for (int j = 0; j < numFrames; j++) {
                  pokemonText = textures.get("ghost_front");
                  SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height * frame, height, height);
                  introAnimGhost.add(sprite);
               }
            } else if (line.contains("dorepeat") && setrepeat != 0) {
               i = Integer.valueOf(line.split("dorepeat ")[1]);
               setrepeat--;
               continue;
            }

            i++;
         }

         file = Gdx.files.internal("pokemon/pokemon/egg/anim.asm");
         setrepeat = 0;
         lines = Game.readLines(file);
         i = 0;

         while (i < lines.size()) {
            String line = lines.get(i);
            if (line.contains("setrepeat")) {
               setrepeat = Integer.valueOf(line.split("setrepeat ")[1]);
            } else if (line.contains("frame")) {
               String[] vals = line.split("frame ")[1].split(", ");
               int numFrames = Integer.valueOf(vals[1]);
               int frame = Integer.valueOf(vals[0]);

               for (int j = 0; j < numFrames; j++) {
                  pokemonText = textures.get("egg_front");
                  height = pokemonText.getWidth();
                  SpriteProxy sprite = new SpriteProxy(pokemonText, 0, height * frame, height, height);
                  introAnimEgg.add(sprite);
               }
            } else if (line.contains("dorepeat") && setrepeat != 0) {
               i = Integer.valueOf(line.split("dorepeat ")[1]);
               setrepeat--;
               continue;
            }

            i++;
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
