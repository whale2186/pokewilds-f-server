package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.SmolSpriteProxy;
import com.pkmngen.game.util.TextField;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.texture.DynamicTextures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class Tile {
   boolean isSolid = false;
   boolean isLedge = false;
   boolean isWater = false;
   boolean isGrass = false;
   boolean isTree = false;
   boolean isSmashable = false;
   boolean isCuttable = false;
   boolean isHeadbuttable = false;
   String ledgeDir;
   public Vector2 position;
   public SmolSprite sprite;
   public SmolSprite overSprite;
   public String hasItem;
   public int hasItemAmount = 0;
   Route routeBelongsTo;
   public boolean isTorch = false;
   public boolean belowBridge = false;
   public boolean drawAsTree = false;
   public boolean drawUpperBelowPlayer = false;
   public boolean drawSavannaTree = false;
   public boolean isChimney = false;
   public String squishedTiles = null;
   public String name;
   public String nameUpper = "";
   String biome = "";
   ArrayList<Vector2> doorTiles;
   HashMap<String, Integer> items = null;
   public boolean isBottomMtnLayer = false;
   String shoreOcean = null;
   String shoreTidal = null;
   boolean isTidal = false;
   public boolean isWaterfall = false;
   public boolean isLava = false;
   public int yOffset = 0;

   public void squish(String tileName) {
      if (this.squishedTiles == null) {
         this.squishedTiles = "";
      }

      this.squishedTiles = tileName + "&" + this.squishedTiles;
   }

   public String unSquish() {
      if (this.squishedTiles == null) {
         return null;
      }

      String[] results = this.squishedTiles.split("\\&", 2);
      if (results.length < 2) {
         this.squishedTiles = null;
      } else {
         this.squishedTiles = results[1];
         System.out.println(this.squishedTiles);
      }

      return results[0];
   }

   public HashMap<String, Integer> items() {
      if (this.items == null) {
         this.items = new HashMap<>();
         if (this.name.equals("grass2")) {
            this.items.put("grass", 1);
            if (Game.rand.nextInt(256) < 64) {
               this.items.put("miracle seed", 1);
            }
         } else if (this.name.equals("grass3")) {
            this.items.put("grass", 1);
         } else if (this.name.equals("grass4")) {
            this.items.put("grass", 2);
         } else if (this.name.equals("grass_sand1")) {
            this.items.put("grass", 1);
         } else if (this.name.equals("grass_sand3")) {
            this.items.put("grass", 2);
         } else if (this.name.equals("grass_graveyard1")) {
            this.items.put("grass", 2);
         } else if (this.name.equals("tree5")) {
            this.items.put("log", 2);
         } else if (this.name.contains("tree_large1")) {
            this.items.put("log", 4);
         } else if (this.nameUpper.equals("rock_ice2")) {
            this.items.put("nevermeltice", 2);
         } else if (this.nameUpper.equals("grass_savanna1")) {
            this.items.put("grass", 4);
         } else if (this.nameUpper.equals("grass_savanna2")) {
            this.items.put("grass", 2);
         } else if (this.nameUpper.equals("bush_savanna1")) {
            this.items.put("log", 1);
            this.items.put("grass", 1);
         } else if (this.nameUpper.equals("tree_savanna1")) {
            this.items.put("log", 4);
         } else if (this.nameUpper.equals("aloe_large1")) {
            this.items.put("log", 1);
            this.items.put("grass", 4);
         } else if (this.nameUpper.equals("tree6")) {
            this.items.put("log", 2);
         } else if (this.nameUpper.contains("bush2_color")) {
            this.items.put("log", 1);
            String[] items = new String[]{
               "black apricorn", "blue apricorn", "green apricorn", "pink apricorn", "red apricorn", "white apricorn", "yellow apricorn"
            };
            if (Game.staticGame.map.rand.nextInt(2) == 0) {
               this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 1);
            }
         } else if (this.nameUpper.equals("tree2")) {
            this.items.put("log", 2);
            String[] items = new String[]{
               "black apricorn", "blue apricorn", "green apricorn", "pink apricorn", "red apricorn", "white apricorn", "yellow apricorn"
            };
            if (Game.staticGame.map.rand.nextInt(2) == 0) {
               this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 2);
            }
         } else if (this.nameUpper.equals("tree4")) {
            this.items.put("log", 2);
            String[] items = new String[]{
               "black apricorn", "blue apricorn", "green apricorn", "pink apricorn", "red apricorn", "white apricorn", "yellow apricorn"
            };
            if (Game.staticGame.map.rand.nextInt(2) == 0) {
               this.items.put(items[Game.staticGame.map.rand.nextInt(items.length)], 2);
            }
         } else if (this.nameUpper.equals("grass_planted")) {
            this.items.put("miracle seed", 1);
         } else if (this.nameUpper.contains("berrytree_")) {
            this.items.put("berry seed", 2);
         }

         if (!this.nameUpper.equals("")) {
            if (!this.nameUpper.contains("floor")
               && !this.nameUpper.contains("pokeball")
               && !this.nameUpper.contains("bush")
               && !this.nameUpper.equals("pokemon_mansion_key")
               && !this.nameUpper.contains("REGI")
               && !this.nameUpper.contains("tree")
               && !this.nameUpper.contains("rock")
               && !this.nameUpper.equals("grass_planted")
               && !this.nameUpper.contains("campfire")
               && !this.nameUpper.contains("fence")
               && !this.nameUpper.contains("house")
               && !this.nameUpper.contains("gravestone")
               && !this.nameUpper.contains("savanna")) {
               this.items.put("grass", 1);
               this.items.put("log", 1);
            }

            if (this.nameUpper.equals("rock1") || this.nameUpper.equals("rock1_color")) {
               this.items.put("hard stone", 1);
               if (Game.rand.nextInt(10) == 0) {
                  this.items.put("moon stone", 1);
               }
            } else if (this.nameUpper.equals("gravestone2")) {
               this.items.put("hard stone", 1);
               this.items.put("life force", 1);
            } else if (this.nameUpper.equals("rock_volcano1")) {
               this.items.put("hard stone", 1);
               if (Game.rand.nextInt(10) == 0) {
                  this.items.put("fire stone", 1);
               }
            }
         }
      }

      return this.items;
   }

   public String getAttr(String attrName) {
      String value = "";
      String[] tokens = this.name.split("_" + attrName);
      value = tokens[tokens.length - 1];
      if (!value.equals("")) {
         value = value.split("_")[0];
      }

      return value;
   }

   public static Tile get(Network.TileDataBase tileData, Route routeBelongsTo) {
      if (tileData.tileNameUpper.equals("sign1")) {
         return new TrainerTipsTile(tileData.pos.cpy(), routeBelongsTo, tileData.isUnown, tileData.message);
      }

      Tile tile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, routeBelongsTo);
      tile.items = tileData.items;
      tile.hasItem = tileData.hasItem;
      tile.hasItemAmount = tileData.hasItemAmount;
      tile.doorTiles = tileData.doorTiles;
      tile.biome = tileData.biome;
      tile.isTorch = tile.items != null && tile.items.containsKey("torch");
      if (tileData instanceof Network.TileData) {
         tile.squishedTiles = ((Network.TileData)tileData).squishedTiles;
      }

      return tile;
   }

   public Tile(String tileName, String nameUpper, Vector2 pos) {
      this(tileName, nameUpper, pos, false);
   }

   public Tile(String tileName, String nameUpper, Vector2 pos, boolean color) {
      this(tileName, nameUpper, pos, color, null);
   }

   public Tile(String tileName, String nameUpper, Vector2 pos, boolean color, Route routeBelongsTo) {
      this.init(tileName, nameUpper, pos, color, routeBelongsTo);
   }

   public void init() {
      this.init(this.name, this.nameUpper, this.position, true, this.routeBelongsTo);
   }

   public void init(String tileName, String nameUpper, Vector2 pos, boolean color, Route routeBelongsTo) {
      if (Game.staticGame.map != null && !Game.staticGame.map.refreshCache) {
         Game.staticGame.map.refreshCache = true;
      }

      this.isSolid = false;
      this.isLedge = false;
      this.isWater = false;
      this.isGrass = false;
      this.isTree = false;
      this.isSmashable = false;
      this.isCuttable = false;
      this.isHeadbuttable = false;
      this.isTidal = false;
      this.isWaterfall = false;
      this.isLava = false;
      this.drawUpperBelowPlayer = false;
      this.isChimney = false;
      if (tileName.contains("_tidalwater")) {
         this.isTidal = true;
         this.isGrass = true;
      }

      this.overSprite = null;
      this.ledgeDir = null;
      this.name = tileName;
      this.nameUpper = nameUpper;
      this.position = pos;
      this.routeBelongsTo = routeBelongsTo;

      for (String shoreType : new String[]{this.shoreOcean, this.shoreTidal}) {
         if (shoreType != null) {
            DynamicTextures.cacheShorelines(shoreType);
         }
      }

      if (tileName.contains("puddle1")) {
         String[] names = tileName.split("_");
         String baseTile = names[0];
         if (baseTile.equals("sand4")) {
            baseTile = "sand1";
         } else if (baseTile.equals("mountain4")) {
            baseTile = "mountain1";
         }

         DynamicTextures.cachePuddle(tileName, baseTile, this.getAttr("puddle1"));
         this.sprite = new SmolSprite(DynamicTextures.get(tileName));
         if (this.isTidal) {
            this.sprite = TextureCache.getTileSprite(baseTile);
         }
      } else if (tileName.equals("ground1")) {
         Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
         this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
      } else if (tileName.equals("ground2")) {
         Texture playerText = TextureCache.get(Gdx.files.internal("ground2.png"));
         this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
      } else if (tileName.equals("block1")) {
         Texture playerText = TextureCache.get(Gdx.files.internal("block1.png"));
         this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
         this.isSolid = true;
      } else if (tileName.equals("grass1")) {
         this.sprite = TextureCache.getTileSprite("blank2");
         Texture playerText = TextureCache.get(Gdx.files.internal("grass1.png"));
         this.overSprite = new SmolSprite(playerText, 0, 0, 16, 16);
         this.isGrass = true;
      } else if (tileName.equals("grass_planted")) {
         this.sprite = TextureCache.getTileSprite("grass_planted");
      } else if (tileName.equals("grass2")) {
         this.sprite = TextureCache.getTileSprite("grass2_under");
         this.overSprite = TextureCache.getTileSprite("grass2_over");
         this.isGrass = true;
         this.isCuttable = true;
      } else if (tileName.equals("grass3")) {
         this.sprite = TextureCache.getTileSprite("grass3_under");
         this.overSprite = TextureCache.getTileSprite("grass3_over");
         this.isGrass = true;
         this.isCuttable = true;
      } else if (tileName.equals("grass4")) {
         this.sprite = TextureCache.getTileSprite("green1");
         this.overSprite = TextureCache.getTileSprite("grass4_over");
         this.isGrass = true;
         this.isCuttable = true;
      } else if (tileName.equals("grass_sand1")) {
         this.sprite = TextureCache.getTileSprite("sand1");
         this.overSprite = TextureCache.getTileSprite("grass2_over");
         this.isGrass = true;
         this.isCuttable = true;
      } else if (tileName.equals("flower1")) {
         this.sprite = TextureCache.getTileSprite("flower1");
      } else if (tileName.equals("ground3")) {
         this.sprite = TextureCache.getTileSprite("ground3");
      } else if (tileName.equals("mountain1") || tileName.equals("mountain4") || tileName.equals("mountain5")) {
         this.sprite = TextureCache.getTileSprite("mountain1");
      } else if (tileName.equals("mountain2")) {
         this.sprite = TextureCache.getTileSprite("mountain2");
      } else if (tileName.equals("mountain3")) {
         this.sprite = TextureCache.getTileSprite("mountain3");
      } else if (tileName.equals("mountain6")) {
         this.sprite = TextureCache.getTileSprite(tileName);
      } else if (tileName.contains("mountain")) {
         this.sprite = TextureCache.getTileSprite(tileName);
         if (tileName.contains("left")) {
            this.isLedge = true;
            this.ledgeDir = "left";
         } else if (tileName.contains("right")) {
            this.isLedge = true;
            this.ledgeDir = "right";
         } else if (tileName.contains("top")) {
            this.isLedge = true;
            this.ledgeDir = "up";
         } else if (tileName.contains("bottom")) {
            this.isLedge = true;
            this.ledgeDir = "down";
         } else if (!tileName.contains("inner")) {
            this.isSolid = true;
         }
      } else if (!tileName.contains("cave1") && !tileName.contains("cave2")) {
         if (tileName.equals("tree_large1")) {
            if (color) {
               this.sprite = TextureCache.getTileSprite("tree_large1_color");
            } else {
               this.sprite = TextureCache.getTileSprite("tree_large1");
            }

            this.isSolid = true;
            this.isCuttable = true;
         } else if (tileName.equals("tree_large2")) {
            this.sprite = TextureCache.getTileSprite("tree_large2");
            this.isSolid = true;
         } else if (tileName.equals("tree_large4")) {
            this.sprite = TextureCache.getTileSprite("tree_large4");
            this.isSolid = true;
         } else if (tileName.equals("tree_large3")
            || tileName.equals("tree_fairy1")
            || tileName.equals("tree_fairy2")
            || tileName.equals("grass_fairy1")
            || tileName.equals("grass_fairy2")
            || tileName.equals("grass_fairy3")) {
            this.sprite = TextureCache.getTileSprite(tileName);
            this.isSolid = true;
         } else if (tileName.equals("tree_large1_noSprite")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.isSolid = true;
            this.isCuttable = true;
         } else if (tileName.equals("ledge1_down")) {
            Texture playerText;
            if (color) {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_down_color.png"));
            } else {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_down.png"));
            }

            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isLedge = true;
            this.ledgeDir = "down";
         } else if (tileName.contains("ledge2")) {
            this.sprite = TextureCache.getTileSprite(tileName);
            this.isLedge = true;
         } else if (tileName.equals("ledge1_left")) {
            Texture playerText;
            if (color) {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_left_color.png"));
            } else {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_left.png"));
            }

            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isLedge = true;
            this.ledgeDir = "left";
         } else if (tileName.equals("ledge1_right")) {
            Texture playerText;
            if (color) {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_right_color.png"));
            } else {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_right.png"));
            }

            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isLedge = true;
            this.ledgeDir = "right";
         } else if (tileName.equals("ground2_top")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground2_top.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isLedge = true;
         } else if (tileName.equals("ledge1_corner_bl")) {
            Texture playerText;
            if (color) {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_bl_color.png"));
            } else {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_bl.png"));
            }

            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("ledge1_corner_br")) {
            Texture playerText;
            if (color) {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_br_color.png"));
            } else {
               playerText = TextureCache.get(Gdx.files.internal("ledge1_corner_br.png"));
            }

            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("ledge2_corner_tl")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ledge2_corner_tl.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("ledge2_corner_tr")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ledge2_corner_tr.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("ledge_grass_ramp")) {
            if (color) {
               this.sprite = TextureCache.getTileSprite("ledge_grass_ramp_color");
            } else {
               this.sprite = TextureCache.getTileSprite("ledge_grass_ramp");
            }
         } else if (tileName.equals("ledge_grass_safari_up")) {
            this.sprite = TextureCache.getTileSprite("ledge_grass_safari_up");
            this.isLedge = true;
            this.ledgeDir = "up";
         } else if (tileName.equals("ledge_grass_down")) {
            if (color) {
               this.sprite = TextureCache.getTileSprite("ledge1_down_color");
            } else {
               this.sprite = TextureCache.getTileSprite("ledge_grass_down");
            }

            this.isLedge = true;
            this.ledgeDir = "down";
         } else if (tileName.equals("ledge_grass_left")) {
            this.sprite = TextureCache.getTileSprite("ledge_grass_left");
            this.isLedge = true;
            this.ledgeDir = "left";
         } else if (tileName.equals("ledge_grass_right")) {
            this.sprite = TextureCache.getTileSprite("ledge_grass_right");
            this.isLedge = true;
            this.ledgeDir = "right";
         } else if (tileName.equals("ledge_grass_inside_tl")) {
            this.sprite = TextureCache.getTileSprite("ledge_grass_inside_tl");
            this.isSolid = true;
         } else if (tileName.equals("ledge_grass_inside_tr")) {
            this.sprite = TextureCache.getTileSprite("ledge_grass_inside_tr");
            this.isSolid = true;
         } else if (tileName.equals("water1")) {
            this.sprite = TextureCache.getTileSprite("water1");
            this.isSolid = true;
            this.isWater = true;
         } else if (tileName.equals("water1_ledge1_left")) {
            this.sprite = TextureCache.getTileSprite("water1");
            this.isSolid = true;
            this.isWater = true;
            this.overSprite = TextureCache.getTileSprite("water1_ledge1_left");
         } else if (tileName.equals("water1_ledge1_right")) {
            this.sprite = TextureCache.getTileSprite("water1");
            this.isSolid = true;
            this.isWater = true;
            this.overSprite = TextureCache.getTileSprite("water1_ledge1_right");
         } else if (tileName.equals("water1_ledge1_tl")) {
            this.sprite = TextureCache.getTileSprite("water1");
            this.isSolid = true;
            this.isWater = true;
            this.overSprite = TextureCache.getTileSprite("water1_ledge1_tl");
         } else if (tileName.equals("water1_ledge1_top")) {
            this.sprite = TextureCache.getTileSprite("water1");
            this.isSolid = true;
            this.isWater = true;
            this.overSprite = TextureCache.getTileSprite("water1_ledge1_top");
         } else if (tileName.equals("water1_ledge1_tr")) {
            this.sprite = TextureCache.getTileSprite("water1");
            this.isSolid = true;
            this.isWater = true;
            this.overSprite = TextureCache.getTileSprite("water1_ledge1_tr");
         } else if (tileName.equals("grass_short2")) {
            this.sprite = TextureCache.getTileSprite("grass_short2");
         } else if (tileName.equals("grass_short3")) {
            this.sprite = TextureCache.getTileSprite("grass_short3");
         } else if (tileName.equals("warp1_greyed")) {
            this.sprite = TextureCache.getTileSprite("warp1_greyed");
         } else if (tileName.equals("warp1")) {
            this.sprite = TextureCache.getTileSprite("warp1");
         } else if (tileName.equals("raikou_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("pokemon/raikou_overw1.png"));
            this.overSprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("entei_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("pokemon/entei_overw1.png"));
            this.overSprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("suicune_overw1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("ground1.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("pokemon/suicune_overw1.png"));
            this.overSprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("mega_gengar_overworld1")) {
            this.sprite = TextureCache.getTileSprite("blank");
            Texture playerText = TextureCache.get(Gdx.files.internal("pokemon/mgengar_overworld1.png"));
            this.overSprite = new SmolSprite(playerText, 0, 0, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("solid")) {
            this.sprite = TextureCache.getTileSprite("qmark_tile1");
            this.isSolid = true;
         } else if (tileName.equals("bush1")) {
            if (color) {
               this.overSprite = TextureCache.getTileSprite("bush2_color");
               this.nameUpper = "bush2_color";
            }

            this.name = "green1";
            this.sprite = TextureCache.getTileSprite("green1");
            this.isSolid = true;
            this.isCuttable = true;
            this.isHeadbuttable = true;
         } else if (tileName.equals("bush2")) {
            this.overSprite = TextureCache.getTileSprite("bush2_color");
            this.sprite = TextureCache.getTileSprite("green1");
            this.isSolid = true;
         } else if (tileName.equals("tree_small1")) {
            this.sprite = TextureCache.getTileSprite("tree_small1");
            this.isSolid = true;
         } else if (tileName.equals("rock1")) {
            this.isSolid = true;
            this.sprite = TextureCache.getTileSprite("sand1");
            this.name = "sand1";
            this.nameUpper = "rock1";
         } else if (tileName.equals("rock2")) {
            this.sprite = TextureCache.getTileSprite("rock2");
            this.isSolid = true;
         } else if (tileName.equals("rock3")) {
            this.sprite = TextureCache.getTileSprite("rock3");
            this.isSolid = true;
         } else if (tileName.equals("rock4")) {
            this.sprite = TextureCache.getTileSprite("rock4");
         } else if (tileName.equals("rock5")) {
            this.sprite = TextureCache.getTileSprite("sand1");
            this.name = "sand1";
            this.nameUpper = tileName;
         } else if (tileName.equals("bridge1_tidalwater")) {
            this.sprite = TextureCache.getTileSprite("bridge1");
            this.isCuttable = true;
            this.isTidal = false;
         } else if (tileName.contains("_tidal")) {
            String name = tileName.split("_")[0];
            if (name.equals("sand4") || name.contains("bridge")) {
               name = "sand1";
            }

            this.sprite = TextureCache.getTileSprite(name);
            if (tileName.contains("_tidalwater")) {
               this.isTidal = true;
               this.isGrass = true;
            }
         } else if (tileName.equals("sand1") || tileName.equals("sand4") || tileName.equals("sand4_tidal")) {
            this.sprite = TextureCache.getTileSprite("sand1");
         } else if (tileName.equals("sand4_black")) {
            this.sprite = TextureCache.getTileSprite("sand4_black");
         } else if (tileName.equals("sand3_black")) {
            this.sprite = TextureCache.getTileSprite("sand3_black");
         } else if (tileName.equals("sand2")) {
            this.sprite = TextureCache.getTileSprite("sand2");
         } else if (tileName.equals("sand3") || tileName.equals("sand3_desertEdge")) {
            this.sprite = TextureCache.getTileSprite("sand3");
         } else if (tileName.equals("path1")) {
            this.sprite = TextureCache.getTileSprite("path1");
         } else if (tileName.equals("green_woodedlake") || tileName.equals("green_deepforest")) {
            this.sprite = TextureCache.getTileSprite("green1");
         } else if (tileName.contains("green")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.contains("flower")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.contains("snow")) {
            this.sprite = TextureCache.getTileSprite(tileName);
            if (tileName.equals("snow3")) {
               this.isGrass = true;
               this.overSprite = this.sprite;
            }
         } else if (tileName.equals("soot1")) {
            this.sprite = TextureCache.getTileSprite(tileName);
            this.isGrass = true;
            this.overSprite = this.sprite;
         } else if (tileName.equals("tree1")) {
            this.sprite = TextureCache.getTileSprite("tree1");
            this.isSolid = true;
         } else if (tileName.equals("tree2")) {
            this.sprite = TextureCache.getTileSprite("green1");
            this.overSprite = TextureCache.getTileSprite("tree2");
            this.name = "green1";
            this.nameUpper = "tree2";
            this.isSolid = true;
            this.isCuttable = true;
            this.isHeadbuttable = true;
         } else if (tileName.equals("tree4")) {
            this.sprite = TextureCache.getTileSprite("snow1");
            this.overSprite = TextureCache.getTileSprite("tree4");
            this.name = "snow1";
            this.nameUpper = "tree4";
            this.isSolid = true;
            this.isHeadbuttable = true;
         } else if (tileName.equals("tree5")) {
            this.sprite = TextureCache.getTileSprite("green1");
            this.overSprite = TextureCache.getTileSprite("tree6");
            this.isSolid = true;
            this.isTree = true;
            this.isHeadbuttable = true;
            this.isCuttable = true;
         } else if (tileName.equals("tree6")) {
            this.sprite = TextureCache.getTileSprite("tree6");
            this.isSolid = true;
            this.isTree = true;
            this.isHeadbuttable = true;
         } else if (tileName.equals("tree7")) {
            this.sprite = TextureCache.getTileSprite("tree7");
            this.isSolid = true;
            this.isTree = true;
            this.isHeadbuttable = true;
         } else if (tileName.equals("aloe_large1")) {
            this.sprite = TextureCache.getTileSprite("aloe_large1");
            this.isSolid = true;
            this.isCuttable = true;
         } else if (tileName.contains("cactus")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/desert6.png"));
            this.sprite = TextureCache.getTileSprite("aloe_large1");
            playerText = TextureCache.get(Gdx.files.internal("tiles/" + tileName + ".png"));
            this.overSprite = new SmolSprite(playerText, 0, 0, 16, 16);
            if (tileName.equals("cactus2") || tileName.equals("cactus3") || tileName.equals("cactus9")) {
               this.overSprite = new SmolSprite(playerText, 0, 0, 16, 32);
            }

            if (tileName.equals("cactus10")) {
               this.overSprite = new SmolSprite(playerText, 0, 0, 32, 16);
            }

            if (!tileName.equals("cactus10") && !tileName.equals("cactus7") && !tileName.equals("cactus8") && !tileName.equals("cactus9")) {
               this.name = "desert6";
               this.nameUpper = tileName;
            }

            this.isSolid = true;
            this.isHeadbuttable = true;
         } else if (tileName.contains("ruins")) {
            if (tileName.contains("pillar")) {
               this.sprite = TextureCache.getTileSprite("desert4");
               this.overSprite = TextureCache.getTileSprite(tileName);
               this.isSolid = true;
               this.drawAsTree = true;
            } else if (tileName.contains("floor")) {
               this.sprite = TextureCache.getTileSprite(tileName);
            } else if (tileName.contains("picture")) {
               this.sprite = TextureCache.getTileSprite(tileName);
               this.isSolid = true;
            } else if (tileName.contains("path")) {
               this.sprite = TextureCache.getTileSprite(tileName);
            } else if (tileName.contains("wall")) {
               this.sprite = TextureCache.getTileSprite(tileName);
               this.isSolid = true;
               this.drawAsTree = true;
               this.overSprite = this.sprite;
            } else {
               Texture text = TextureCache.get(Gdx.files.internal("tiles/ruins1_all.png"));
               int offsetX = 8;
               int offsetY = 8;
               if (this.name.contains("E")) {
                  offsetX -= 8;
               }

               if (this.name.contains("W")) {
                  offsetX += 8;
               }

               if (this.name.contains("N")) {
                  offsetY += 8;
               }

               if (this.name.contains("S")) {
                  offsetY -= 8;
               }

               this.sprite = new SmolSprite(text, offsetX, offsetY, 16, 16);
            }
         } else if (tileName.contains("waterfall") || tileName.contains("lavafall")) {
            this.isWaterfall = true;
            String name = "waterfall_sheet2";
            if (tileName.contains("lavafall")) {
               name = "lavafall_sheet2";
            }

            Texture text = TextureCache.get(Gdx.files.internal("tiles/" + name + ".png"));
            int mul = 1;
            if (tileName.contains("inner")) {
               text = TextureCache.get(Gdx.files.internal("tiles/" + name + "_inner.png"));
               mul = -1;
            }

            int offsetX = 16;
            int offsetY = 16;
            if (this.name.contains("E")) {
               offsetX -= mul * 16;
            }

            if (this.name.contains("W")) {
               offsetX += mul * 16;
            }

            if (this.name.contains("N")) {
               offsetY += mul * 16;
            }

            if (this.name.contains("S")) {
               offsetY -= mul * 16;
            }

            this.sprite = new SmolSprite(text, offsetX, offsetY, 16, 16);
         } else if (tileName.contains("lava")) {
            this.isLava = true;
            Texture text = TextureCache.get(Gdx.files.internal("tiles/lava_sheet1.png"));
            int offsetX = 16;
            int offsetY = 16;
            if (this.name.contains("E")) {
               offsetX += 16;
            }

            if (this.name.contains("W")) {
               offsetX -= 16;
            }

            if (this.name.contains("N")) {
               offsetY -= 16;
            }

            if (this.name.contains("S")) {
               offsetY += 16;
            }

            this.sprite = new SmolSprite(text, offsetX, offsetY, 16, 16);
            this.isSolid = true;
         } else if (tileName.equals("tree_plant1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/green1.png"));
            this.sprite = new SmolSprite(playerText, 0, 0, 16, 16);
            playerText = TextureCache.get(Gdx.files.internal("tiles/buildings/plant1.png"));
            this.overSprite = new SmolSprite(playerText, 0, 0, 16, 32);
            this.isSolid = true;
            this.isTree = true;
         } else if (tileName.equals("pkmnmansion_statue1")) {
            this.sprite = TextureCache.getTileSprite("buildings/pkmnmansion_floor1");
            this.overSprite = TextureCache.getTileSprite("buildings/pkmnmansion_statue1");
            this.isSolid = true;
            this.drawAsTree = true;
         } else if (tileName.equals("pkmnmansion_struct1")) {
            this.sprite = TextureCache.getTileSprite("buildings/pkmnmansion_floor1");
            this.overSprite = TextureCache.getTileSprite("buildings/pkmnmansion_struct1");
            this.isSolid = true;
         } else if (tileName.equals("pkmnmansion_shelf1")) {
            this.sprite = TextureCache.getTileSprite("buildings/pkmnmansion_floor1");
            this.overSprite = TextureCache.getTileSprite("buildings/pkmnmansion_shelf1");
            this.isSolid = true;
            this.drawAsTree = true;
         } else if (tileName.equals("pkmnmansion_shelf1_NS")) {
            this.sprite = TextureCache.getTileSprite("buildings/pkmnmansion_floor1");
            this.overSprite = TextureCache.getTileSprite("buildings/pkmnmansion_shelf1_NS");
            this.isSolid = true;
            this.drawAsTree = true;
         } else if (tileName.equals("water2")) {
            this.sprite = TextureCache.getTileSprite("water2");
            this.isSolid = true;
            this.isWater = true;
         } else if (tileName.equals("water2_edge1")) {
            this.sprite = TextureCache.getTileSprite("water2_edge1");
            this.isSolid = true;
            this.isWater = true;
         } else if (tileName.equals("water3")) {
            this.sprite = TextureCache.getTileSprite("water3");
            this.isSolid = true;
            this.isWater = true;
         } else if (tileName.equals("water5")) {
            this.sprite = TextureCache.getTileSprite("water2");
            this.isSolid = true;
            this.isWater = true;
         } else if (tileName.equals("black1")) {
            this.sprite = TextureCache.getTileSprite("blank3");
            this.isSolid = true;
         } else if (tileName.equals("blank1")) {
            this.sprite = TextureCache.getTileSprite("blank");
         } else if (tileName.equals("campfire1")) {
            this.sprite = TextureCache.getTileSprite("campfire1", 16, 20);
            this.isSolid = true;
         } else if (tileName.equals("fence1")) {
            this.sprite = TextureCache.getTileSprite("fence1");
            this.isSolid = true;
         } else if (tileName.equals("fence2")) {
            this.sprite = TextureCache.getTileSprite("fence2");
            this.isSolid = true;
         } else if (tileName.equals("bridge1_water2_lower")) {
            this.sprite = TextureCache.getTileSprite("water2");
            this.isWater = true;
            this.isSolid = true;
            this.belowBridge = true;
         } else if (tileName.equals("bridge1_water5_lower")) {
            this.sprite = TextureCache.getTileSprite("water2");
            this.isWater = true;
            this.isSolid = true;
            this.belowBridge = true;
         } else if (tileName.contains("bridge")) {
            this.sprite = TextureCache.getTileSprite("bridge1");
            this.isCuttable = true;
         } else if (tileName.equals("sleeping_bag1")) {
            this.sprite = TextureCache.getTileSprite("sleeping_bag1");
         } else if (tileName.equals("house_bed1")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_bed1");
            this.isSolid = true;
         } else if (tileName.equals("house_plant1")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_plant1");
            this.isSolid = true;
         } else if (tileName.equals("house_plant2")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_plant2");
            this.isSolid = true;
         } else if (tileName.equals("house_gym1")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_gym1");
            this.isSolid = true;
         } else if (tileName.equals("house_wardrobe1")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_wardrobe1");
            this.isSolid = true;
         } else if (tileName.contains("house_vanity")) {
            this.sprite = TextureCache.getTileSprite("buildings/" + tileName);
            this.isSolid = true;
         } else if (tileName.contains("house_couch1")) {
            this.sprite = TextureCache.getTileSprite("buildings/" + tileName);
            this.isSolid = true;
         } else if (tileName.contains("house_couch")) {
            this.sprite = TextureCache.getTileSprite("buildings/" + tileName);
            this.isSolid = true;
         } else if (tileName.equals("house_shelf1")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_shelf1");
            this.isSolid = true;
         } else if (tileName.equals("house_stool1")) {
            this.sprite = TextureCache.getTileSprite("buildings/house_stool1");
         } else if (tileName.equals("torch1")) {
            this.sprite = TextureCache.getTileSprite("torch_sheet1", 16, 20);
         } else if (tileName.contains("desert")) {
            if (tileName.equals("desert2_trapinch_spawn")) {
               tileName = "desert2";
            }

            if (tileName.equals("desert4_isGrass")) {
               tileName = "desert4";
               this.isGrass = true;
            }

            this.sprite = TextureCache.getTileSprite(tileName);
            if (tileName.equals("desert2")) {
               this.isGrass = true;
               this.overSprite = this.sprite;
            }
         } else if (tileName.equals("grass_sand2")) {
            this.sprite = TextureCache.getTileSprite("desert1");
            this.overSprite = TextureCache.getTileSprite("grass2_over");
            this.isGrass = true;
            this.isCuttable = true;
         } else if (tileName.equals("grass_sand3")) {
            this.sprite = TextureCache.getTileSprite("desert6");
            this.overSprite = TextureCache.getTileSprite("grass5_over");
            this.isGrass = true;
            this.isCuttable = true;
         } else if (tileName.equals("grass_graveyard1")) {
            this.sprite = TextureCache.getTileSprite("green11");
            this.overSprite = TextureCache.getTileSprite("grass6_over");
            this.isGrass = true;
            this.isCuttable = true;
         } else if (tileName.contains("house6")) {
            this.sprite = TextureCache.getTileSprite("blank3");
            this.nameUpper = tileName;
         } else if (tileName.contains("house7")) {
            this.sprite = TextureCache.getTileSprite("blank3");
            this.nameUpper = tileName;
         } else if (tileName.contains("house8")) {
            this.sprite = TextureCache.getTileSprite("blank3");
            this.nameUpper = tileName;
         } else if (tileName.contains("house9")) {
            this.sprite = TextureCache.getTileSprite("blank3");
            this.nameUpper = tileName;
         } else if (tileName.contains("potted")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.nameUpper = tileName;
         } else if (tileName.contains("hole1")) {
            this.sprite = TextureCache.getTileSprite("hole1");
         } else if (tileName.equals("building1_pokecenter1_right")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.isSolid = true;
         } else if (tileName.equals("building1_pokecenter1")) {
            this.sprite = TextureCache.getTileSprite("buildings/building1_pokecenter1", 0, 0, 32, 32);
            this.isSolid = true;
         } else if (tileName.equals("building1_machine2")) {
            this.sprite = TextureCache.getTileSprite("buildings/building1_machine2");
            this.isSolid = true;
         } else if (tileName.equals("pedistal1")) {
            this.nameUpper = this.name;
         } else if (tileName.equals("statue1")) {
            this.sprite = TextureCache.getTileSprite("statue1");
            this.isSolid = true;
         } else if (tileName.contains("gate1")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.equals("building1_fossilreviver1")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.nameUpper = tileName;
         } else if (tileName.equals("interiorwall1")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.nameUpper = tileName;
         } else if (tileName.contains("gravestone")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.nameUpper = tileName;
         } else if (tileName.contains("sign_")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.nameUpper = tileName;
         } else if (tileName.equals("ledges3_none")) {
            this.sprite = TextureCache.getTileSprite("ledges3_none", 8, 8, 16, 16);
         } else if (tileName.contains("ledges3")) {
            this.sprite = TextureCache.getTileSprite("blank");
            this.nameUpper = tileName;
         } else if (tileName.equals("stalagmite1")) {
            this.sprite = TextureCache.getTileSprite("stalagmite1");
         } else if (tileName.contains("_savanna")) {
            this.sprite = TextureCache.getTileSprite(tileName);
            if (tileName.equals("grass_savanna1")) {
               this.overSprite = this.sprite;
               this.sprite = TextureCache.getTileSprite("green_savanna1");
               this.isGrass = true;
               this.isCuttable = true;
            } else if (tileName.equals("grass_savanna2")) {
               this.overSprite = this.sprite;
               this.sprite = TextureCache.getTileSprite("green_savanna1");
               this.isGrass = true;
               this.isCuttable = true;
            }
         } else if (tileName.contains("ice")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.equals("water6")) {
            this.sprite = TextureCache.getTileSprite(tileName);
            this.isWater = true;
         } else if (tileName.equals("volcano1") || tileName.equals("volcano2")) {
            this.sprite = TextureCache.getTileSprite("volcano1");
         } else if (tileName.equals("volcano3")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.equals("warp_tile1")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.equals("chest1")) {
            this.sprite = TextureCache.getTileSprite(tileName, 16, 32);
         } else if (tileName.equals("stairs_up1")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.equals("stairs_down1")) {
            this.sprite = TextureCache.getTileSprite(tileName);
         } else if (tileName.equals("ball_kiln1")) {
            this.sprite = TextureCache.getTileSprite(tileName, 16, 24);
         } else {
            this.sprite = TextureCache.getTileSprite("buildings/" + tileName, 16, 16);
            if (!tileName.contains("door")
               && !tileName.contains("floor")
               && !tileName.contains("rug")
               && !tileName.contains("__off")
               && !tileName.contains("carpet")
               && !tileName.contains("cables")) {
               this.isSolid = true;
            }
         }
      } else {
         if (tileName.equals("cave1_regi1")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/" + tileName + ".png"));
            this.sprite = new SmolSpriteProxy(
               new Color(0.21960784F, 0.21960784F, 0.21960784F, 1.0F),
               new Color(0.7529412F, 0.5647059F, 0.21960784F, 1.0F),
               new Color(0.627451F, 0.47058824F, 0.09411765F, 1.0F),
               playerText,
               0,
               0,
               16,
               16
            );
            this.sprite = new SmolSpriteProxy(playerText, 0, 0, 32, 32);
         } else if (!tileName.equals("cave1_regi2") && !tileName.equals("cave1_regi3")) {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/" + tileName + ".png"));
            this.sprite = new SmolSpriteProxy(
               new Color(0.21960784F, 0.21960784F, 0.21960784F, 1.0F),
               new Color(0.7529412F, 0.5647059F, 0.21960784F, 1.0F),
               new Color(0.627451F, 0.47058824F, 0.09411765F, 1.0F),
               playerText,
               0,
               0,
               16,
               16
            );
         } else {
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_regi1.png"));
            this.sprite = new SmolSpriteProxy(playerText, 128, 0, 32, 32);
         }

         if (tileName.contains("up")) {
            this.isLedge = true;
            this.ledgeDir = "up";
         } else if (!tileName.contains("floor") && !tileName.contains("ramp") && !tileName.contains("door")) {
            this.isSolid = true;
         }

         if (this.name.equals("cave1_regipedistal1")) {
            this.overSprite = this.sprite;
            Texture playerText = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_up1.png"));
            this.sprite = new SmolSpriteProxy(
               new Color(0.21960784F, 0.21960784F, 0.21960784F, 1.0F),
               new Color(0.7529412F, 0.5647059F, 0.21960784F, 1.0F),
               new Color(0.627451F, 0.47058824F, 0.09411765F, 1.0F),
               playerText,
               0,
               0,
               16,
               16
            );
         }
      }

      if (tileName.contains("pkmnmansion_floor")) {
         this.isGrass = true;
      }

      if (!this.nameUpper.equals("")) {
         if (this.nameUpper.contains("REGI")) {
            Texture text = TextureCache.get(Gdx.files.internal("tiles/cave1/overworld2.png"));
            if (this.nameUpper.equals("REGIDRAGO")) {
               this.overSprite = new SmolSprite(text, 16, 0, 16, 16);
            } else if (this.nameUpper.equals("REGIELEKI")) {
               this.overSprite = new SmolSprite(text, 32, 0, 16, 16);
            } else if (this.nameUpper.equals("REGICE")) {
               this.overSprite = new SmolSprite(text, 48, 0, 16, 16);
            } else if (this.nameUpper.equals("REGIROCK")) {
               this.overSprite = new SmolSprite(text, 64, 0, 16, 16);
            } else if (this.nameUpper.equals("REGISTEEL")) {
               this.overSprite = new SmolSprite(text, 80, 0, 16, 16);
            }
         } else if (this.nameUpper.contains("ledges3")) {
            String name = "ledges3";
            if (this.nameUpper.contains("snow")) {
               name = "ledges3snow";
            } else if (this.nameUpper.contains("volcano")) {
               name = "ledges3volcano";
            }

            if (this.nameUpper.contains("inner")) {
               name = name + "_inner";
            } else if (this.nameUpper.contains("none")) {
               name = name + "_none";
            }

            int offsetX = 8;
            int offsetY = 8;
            int numDirs = 0;
            String ledgeDir = "left";
            if (this.nameUpper.contains("E")) {
               offsetX -= 8;
               ledgeDir = "left";
               numDirs++;
            }

            if (this.nameUpper.contains("W")) {
               offsetX += 8;
               ledgeDir = "right";
               numDirs++;
            }

            if (this.nameUpper.contains("N")) {
               offsetY += 8;
               ledgeDir = "down";
               numDirs++;
            }

            if (this.nameUpper.contains("S")) {
               offsetY -= 8;
               ledgeDir = "up";
               numDirs++;
            }

            this.isLedge = true;
            this.isSolid = false;
            if (numDirs == 1) {
               this.ledgeDir = ledgeDir;
            } else if (!this.nameUpper.contains("inner")) {
               this.isSolid = true;
            }

            this.drawUpperBelowPlayer = true;
            this.overSprite = TextureCache.getTileSprite(name, offsetX, offsetY, 16, 16);
         } else if (this.nameUpper.contains("house6")) {
            String name = "house6";
            if (this.nameUpper.contains("roof")) {
               name = name + "_roof";
            }

            if (this.nameUpper.contains("inner")) {
               name = name + "_inner";
            }

            if (!this.nameUpper.contains("door") && !this.nameUpper.contains("left") && !this.nameUpper.contains("middle") && !this.nameUpper.contains("right")
               )
             {
               name = "buildings/" + name + "_all";
               int offsetX = 8;
               int offsetY = 8;
               if (this.nameUpper.contains("E")) {
                  offsetX -= 8;
               }

               if (this.nameUpper.contains("W")) {
                  offsetX += 8;
               }

               if (this.nameUpper.contains("N")) {
                  offsetY += 8;
               }

               if (this.nameUpper.contains("S")) {
                  offsetY -= 8;
               }

               this.overSprite = TextureCache.getTileSprite(name, offsetX, offsetY, 16, 16);
            } else {
               name = this.nameUpper;
               name = name.replace("chimney", "");
               name = "buildings/" + name;
               this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 16);
            }
         } else if (this.nameUpper.contains("house7_roof")) {
            String name = "house7_all";
            this.overSprite = TextureCache.getTileSprite(name, 16, 16, 16, 16);
            String[] exts = this.nameUpper.split("_");
            if (exts.length > 1) {
               String ext = exts[exts.length - 1];
               if (ext.equals("E")) {
                  this.overSprite.setRegion(0, 0, 16, 16);
               } else if (ext.equals("EW")) {
                  this.overSprite.setRegion(16, 0, 16, 16);
               } else if (ext.equals("SE")) {
                  this.overSprite.setRegion(64, 0, 16, 16);
               } else if (ext.equals("W")) {
                  this.overSprite.setRegion(48, 0, 16, 16);
               } else if (ext.equals("SW")) {
                  this.overSprite.setRegion(96, 0, 16, 16);
               } else if (ext.equals("NE")) {
                  this.overSprite.setRegion(64, 16, 16, 16);
               } else if (ext.equals("NEW") || ext.equals("N")) {
                  this.overSprite.setRegion(80, 16, 16, 16);
               } else if (ext.equals("NW")) {
                  this.overSprite.setRegion(96, 16, 16, 16);
               } else if (ext.equals("NSE")) {
                  this.overSprite.setRegion(112, 0, 16, 16);
               } else if (ext.equals("S") || ext.equals("SEW")) {
                  this.overSprite.setRegion(80, 0, 16, 16);
               } else if (ext.equals("NSEW")) {
                  this.overSprite.setRegion(128, 0, 16, 16);
               } else if (ext.equals("NSW")) {
                  this.overSprite.setRegion(144, 0, 16, 16);
               }
            }
         } else if (this.nameUpper.contains("house7")) {
            String name = "house7_all";
            this.overSprite = TextureCache.getTileSprite(name, 176, 16, 16, 16);
            String[] exts = this.nameUpper.split("_");
            if (exts.length > 1) {
               String ext = exts[exts.length - 1];
               if (this.nameUpper.contains("door")) {
                  this.overSprite.setRegion(208, 16, 16, 16);
               } else if (ext.equals("E")) {
                  this.overSprite.setRegion(0, 16, 16, 16);
               } else if (ext.equals("SE")) {
                  this.overSprite.setRegion(16, 16, 16, 16);
               } else if (ext.equals("W")) {
                  this.overSprite.setRegion(48, 16, 16, 16);
               } else if (ext.equals("SW")) {
                  this.overSprite.setRegion(32, 16, 16, 16);
               } else if (ext.equals("NE")) {
                  this.overSprite.setRegion(160, 16, 16, 16);
               } else if (ext.equals("NEW") || ext.equals("N")) {
                  this.overSprite.setRegion(176, 16, 16, 16);
               } else if (ext.equals("NW")) {
                  this.overSprite.setRegion(192, 16, 16, 16);
               } else if (ext.equals("NSE")) {
                  this.overSprite.setRegion(160, 0, 16, 16);
               } else if (ext.equals("NSEW")) {
                  this.overSprite.setRegion(176, 0, 16, 16);
               } else if (ext.equals("S") || ext.equals("SEW")) {
                  this.overSprite.setRegion(32, 0, 16, 16);
               } else if (ext.equals("NSW")) {
                  this.overSprite.setRegion(192, 0, 16, 16);
               }
            }
         } else if (this.nameUpper.contains("house8")) {
            String name = "house8";
            if (this.nameUpper.contains("roof")) {
               name = name + "_roof";
            }

            if (!this.nameUpper.contains("door") && !this.nameUpper.contains("left") && !this.nameUpper.contains("middle") && !this.nameUpper.contains("right")
               )
             {
               name = "buildings/" + name + "_all";
               int offsetX = 16;
               int offsetY = 16;
               if (this.nameUpper.contains("E")) {
                  offsetX -= 16;
               }

               if (this.nameUpper.contains("W")) {
                  offsetX += 16;
               }

               if (this.nameUpper.contains("N")) {
                  offsetY += 16;
               }

               if (this.nameUpper.contains("S")) {
                  offsetY -= 16;
               }

               if (!this.nameUpper.contains("roof") && !this.nameUpper.contains("S")) {
                  this.yOffset = 4;
               }

               this.overSprite = TextureCache.getTileSprite(name, offsetX, offsetY, 16, 16 + this.yOffset);
            } else {
               name = this.nameUpper;
               name = name.replace("chimney", "");
               name = "buildings/" + name;
               this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 16);
            }
         } else if (this.nameUpper.contains("house9")) {
            String name = "house9";
            int scale = 8;
            if (this.nameUpper.contains("roof")) {
               name = name + "_roof";
               scale = 16;
            }

            if (!this.nameUpper.contains("door") && !this.nameUpper.contains("left") && !this.nameUpper.contains("middle") && !this.nameUpper.contains("right")
               )
             {
               name = "buildings/" + name + "_all";
               int offsetX = scale;
               int offsetY = scale;
               if (this.nameUpper.contains("E")) {
                  offsetX -= scale;
               }

               if (this.nameUpper.contains("W")) {
                  offsetX += scale;
               }

               if (this.nameUpper.contains("N")) {
                  offsetY += scale;
               }

               if (this.nameUpper.contains("S")) {
                  offsetY -= scale;
               }

               this.overSprite = TextureCache.getTileSprite(name, offsetX, offsetY, 16, 16);
            } else {
               name = this.nameUpper;
               name = name.replace("chimney", "");
               name = "buildings/" + name;
               this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 16);
            }
         } else if (this.nameUpper.contains("house_bed1colored")) {
            DynamicTextures.cacheColoredHouseBed(this.nameUpper);
            this.overSprite = new SmolSprite(DynamicTextures.get(this.nameUpper));
         } else if (this.nameUpper.contains("house") && !this.nameUpper.contains("|")) {
            String name = this.nameUpper;
            if (this.nameUpper.contains("house5")) {
               name = name.replace("exteriorwindows", "");
               name = name.replace("chimney", "");
            }

            name = "buildings/" + name;
            this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 16);
         } else if (this.nameUpper.equals("pedistal1") || this.nameUpper.equals("ruins_statue1") || this.nameUpper.equals("statue1")) {
            this.overSprite = TextureCache.getTileSprite(this.nameUpper, 0, 0, 16, 32);
            this.drawAsTree = true;
         } else if (this.nameUpper.contains("building1_fossilreviver1")) {
            String name = "buildings/building1_pc1";
            this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 24);
         } else if (this.nameUpper.equals("solid")) {
            this.overSprite = null;
         } else if (this.nameUpper.equals("onpress_above")) {
            this.overSprite = null;
         } else if (this.nameUpper.equals("onpress_left")) {
            this.overSprite = null;
         } else if (this.nameUpper.contains("revived")) {
            this.overSprite = null;
         } else if (this.nameUpper.contains("volcarona")) {
            this.overSprite = null;
         } else if (this.nameUpper.contains("spiritomb")) {
            this.overSprite = null;
         } else if (this.nameUpper.contains("hole1_water")) {
            DynamicTextures.cacheWaterHole(this.nameUpper);
            this.overSprite = new SmolSprite(DynamicTextures.get(this.nameUpper));
         } else if (this.nameUpper.contains("hole1")) {
            DynamicTextures.cacheHole(this.nameUpper);
            this.overSprite = new SmolSprite(DynamicTextures.get(this.nameUpper));
         } else if (this.nameUpper.contains("interiorwall1")) {
            DynamicTextures.cacheInteriorWall(this.nameUpper);
            this.overSprite = new SmolSprite(DynamicTextures.get(this.nameUpper));
         } else if (this.nameUpper.contains("_cracked")) {
            String name = "desert4_cracked";
            this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 16);
         } else if (this.nameUpper.equals("tree_large1")) {
            String name = "tree_large1_color";
            this.overSprite = TextureCache.getTileSprite(name, 0, 0, 32, 32);
         } else if (this.nameUpper.equals("tree_large2") || this.nameUpper.equals("tree_large3")) {
            this.overSprite = TextureCache.getTileSprite(this.nameUpper);
         } else if (this.nameUpper.contains("_savanna")) {
            this.overSprite = TextureCache.getTileSprite(this.nameUpper);
            if (this.nameUpper.contains("tree")) {
               this.drawUpperBelowPlayer = true;
               this.drawSavannaTree = true;
            } else if (this.nameUpper.contains("bush")) {
               this.isCuttable = true;
            } else if (this.nameUpper.contains("grass")) {
               this.isGrass = true;
            }
         } else if (this.nameUpper.contains("pokedoll1")) {
            String name = "pokedoll1";
            this.overSprite = TextureCache.getTileSprite(name, 0, 0, 16, 16);
         } else if (this.nameUpper.contains("waterfall") || this.nameUpper.contains("lavafall")) {
            this.isWaterfall = true;
            String name = "waterfall_sheet2";
            if (this.nameUpper.contains("lavafall")) {
               name = "lavafall_sheet2";
            }

            Texture text = TextureCache.get(Gdx.files.internal("tiles/" + name + ".png"));
            int mul = 1;
            if (this.nameUpper.contains("inner")) {
               text = TextureCache.get(Gdx.files.internal("tiles/" + name + "_inner.png"));
               mul = -1;
            }

            int offsetX = 16;
            int offsetY = 16;
            if (this.nameUpper.contains("E")) {
               offsetX -= mul * 16;
            }

            if (this.nameUpper.contains("W")) {
               offsetX += mul * 16;
            }

            if (this.nameUpper.contains("N")) {
               offsetY += mul * 16;
            }

            if (this.nameUpper.contains("S")) {
               offsetY -= mul * 16;
            }

            this.sprite = new SmolSprite(text, offsetX, offsetY, 16, 16);
            this.overSprite = this.sprite;
         } else if (this.nameUpper.contains("berrytree_")) {
            String name = this.nameUpper.split("_")[1];
            name = "berrytree_" + name;
            int offsetX = 0;
            if (this.nameUpper.contains("full")) {
               offsetX = 32;
            } else if (this.nameUpper.contains("empty")) {
               offsetX = 64;
            }

            this.overSprite = TextureCache.getTileSprite(name, offsetX, 0, 16, 32);
            this.isCuttable = true;
         } else if (this.nameUpper.contains("pkmnmansion")) {
            this.overSprite = TextureCache.getTileSprite("buildings/" + this.nameUpper, 0, 0, 16, 16);
         } else {
            this.overSprite = TextureCache.getTileSprite(this.nameUpper, 0, 0, 16, 16);
         }

         if (this.nameUpper.contains("house") && this.nameUpper.contains("exteriorwindows")) {
            DynamicTextures.cacheHouseWithWindow(this.nameUpper, this.overSprite);
            this.overSprite = new SmolSprite(DynamicTextures.get(this.nameUpper));
            this.overSprite.setRegionHeight(this.overSprite.getRegionHeight() + this.yOffset);
         } else if (this.nameUpper.contains("house") && this.nameUpper.contains("chimney")) {
            this.isChimney = true;
            DynamicTextures.cacheHouseWithChimney(this.nameUpper, this.overSprite);
            this.overSprite = new SmolSprite(DynamicTextures.get(this.nameUpper));
         }

         if (this.nameUpper.equals("house_couch1")) {
            this.overSprite.setRegion(0, 0, 32, 16);
            this.drawSavannaTree = true;
         } else if (this.nameUpper.contains("house_couch")) {
            this.overSprite.setRegion(0, 0, 16, 32);
         } else if (this.nameUpper.contains("ball_kiln1")) {
            this.overSprite.setRegion(0, 0, 16, 24);
         } else if (this.nameUpper.equals("campfire1")) {
            this.overSprite.setRegion(0, 0, 16, 20);
         } else if (this.nameUpper.equals("ice2_pit2")) {
            this.overSprite.setRegion(0, 0, 32, 32);
            this.drawSavannaTree = true;
         }

         if (this.nameUpper.contains("house_bed1")
            || this.nameUpper.contains("tree2")
            || this.nameUpper.contains("tree4")
            || this.nameUpper.contains("tree6")
            || this.nameUpper.contains("tree7")
            || this.nameUpper.contains("tree8")
            || this.nameUpper.contains("house_plant1")
            || this.nameUpper.contains("house_plant2")
            || this.nameUpper.contains("house_gym1")
            || this.nameUpper.contains("house_wardrobe1")
            || this.nameUpper.contains("house_vanity")
            || this.nameUpper.contains("cactus2")
            || this.nameUpper.contains("cactus3")
            || this.nameUpper.contains("cactus9")
            || this.nameUpper.contains("house_shelf1")) {
            this.overSprite.setRegion(0, 0, 16, 32);
         }

         if (this.nameUpper.equals("cactus10")) {
            this.overSprite.setRegion(0, 0, 32, 16);
            this.drawSavannaTree = true;
         }

         if (this.nameUpper.equals("aloe_large1")) {
            this.overSprite.setRegion(0, 0, 32, 32);
            this.drawSavannaTree = true;
         }

         if (this.nameUpper.equals("chest1")) {
            this.overSprite.setRegion(0, 0, 16, 32);
         }

         if (!this.nameUpper.contains("door")
            && !this.nameUpper.contains("floor")
            && !this.nameUpper.contains("tree_planted")
            && !this.nameUpper.equals("berry_planted")
            && !this.nameUpper.contains("stool")
            && !this.nameUpper.contains("gate")
            && !this.nameUpper.equals("grass_planted")
            && !this.nameUpper.contains("_cracked")
            && !this.nameUpper.contains("ledges3")
            && !this.nameUpper.contains("warp_tile")
            && !this.nameUpper.contains("waterfall")
            && !this.nameUpper.contains("grass_savanna")) {
            this.isSolid = true;
         }

         if (this.nameUpper.contains("cactus1") || this.nameUpper.contains("cactus2")) {
            this.isHeadbuttable = true;
         }

         if (!this.nameUpper.contains("floor")
            && !this.nameUpper.contains("pokeball")
            && !this.nameUpper.contains("bush")
            && !this.nameUpper.equals("pokemon_mansion_key")
            && !this.nameUpper.contains("REGI")
            && !this.nameUpper.contains("tree")
            && !this.nameUpper.contains("rock")
            && !this.nameUpper.contains("ledges3")
            && !this.nameUpper.contains("onpress")
            && !this.nameUpper.contains("waterfall")
            && !this.nameUpper.contains("stairs_down")
            && !this.nameUpper.contains("hole")
            && !this.nameUpper.contains("cracked")
            && !this.nameUpper.equals("pot1")
            && !this.nameUpper.contains("ultraball")
            && !this.nameUpper.contains("rubble")
            && !this.nameUpper.contains("odd_keystone")
            && !this.nameUpper.contains("pokedoll")
            && !this.nameUpper.contains("building1_fossilreviver1")
            && !this.nameUpper.contains("pkmnmansion")
            && !this.nameUpper.equals("solid")) {
            this.isCuttable = true;
         }

         if (this.nameUpper.contains("bush2_color")
            || this.nameUpper.equals("tree2")
            || this.nameUpper.equals("tree6")
            || this.nameUpper.equals("tree7")
            || this.nameUpper.equals("tree8")
            || this.nameUpper.equals("tree9")
            || this.nameUpper.equals("tree4")) {
            this.isHeadbuttable = true;
            this.isCuttable = true;
         }

         if (this.nameUpper.equals("tree_savanna1")) {
            this.isCuttable = true;
         }

         if (this.nameUpper.equals("pokeball1")) {
            this.isSolid = true;
            this.hasItem = "poké ball";
            this.hasItemAmount = 1;
         } else if (this.nameUpper.equals("ultraball1")) {
            this.isSolid = true;
            this.hasItem = "ultra ball";
            this.hasItemAmount = 1;
         }

         if (this.nameUpper.equals("pokemon_mansion_key")) {
            this.isSolid = true;
            this.hasItem = "secret key";
            this.hasItemAmount = 1;
         }

         if (this.nameUpper.contains("stairs")) {
            this.isSolid = false;
            this.isGrass = false;
         }

         if (this.nameUpper.equals("mewtwo_overworld_hidden")) {
            this.isSolid = false;
         }

         if (this.nameUpper.equals("sign1")) {
            this.isCuttable = false;
         }

         if (this.nameUpper.equals("rock1")
            || this.nameUpper.equals("rock1_color")
            || this.nameUpper.equals("rock5")
            || this.nameUpper.equals("rock_volcano1")
            || this.nameUpper.contains("gravestone")) {
            this.isSmashable = true;
            this.isCuttable = false;
         }

         if (this.nameUpper.equals("rock_ice2")) {
            this.isSmashable = true;
            this.isCuttable = false;
         }

         if (this.nameUpper.contains("fence")) {
            if (this.nameUpper.equals("fence1") || this.nameUpper.equals("fence2")) {
               this.isLedge = true;
               this.ledgeDir = "down";
            } else if (this.nameUpper.equals("fence1_NS") || this.nameUpper.equals("fence2_NS")) {
               this.isLedge = true;
               this.ledgeDir = "left";
            }
         }

         if (this.nameUpper.contains("stairs")
            || this.nameUpper.contains("door")
            || this.nameUpper.contains("_cracked")
            || this.nameUpper.equals("grass_planted")
            || this.nameUpper.contains("warp_tile")
            || this.nameUpper.contains("stool")) {
            this.drawUpperBelowPlayer = true;
         }

         if (this.nameUpper.contains("warp_tile")) {
            DrawMiniMap.warpTiles.put(this.position.cpy(), this);
         }
      }

      if (this.overSprite != null
         && !this.nameUpper.contains("tree_planted")
         && (
            this.name.contains("tree")
               || this.nameUpper.contains("tree")
               || this.nameUpper.contains("house_gym")
               || this.nameUpper.contains("house_plant")
               || this.nameUpper.contains("cactus2")
               || this.nameUpper.equals("cactus9")
               || this.nameUpper.equals("building1_fossilreviver1")
               || this.nameUpper.contains("house_shelf")
               || this.nameUpper.contains("interiorwall")
               || this.nameUpper.contains("house_vanity")
               || this.nameUpper.contains("ball_kiln")
               || this.nameUpper.contains("pkmnmansion_statue1")
               || this.nameUpper.contains("pkmnmansion_shelf1")
               || this.nameUpper.equals("rock1_color")
               || this.nameUpper.contains("house_wardrobe")
         )) {
         this.drawAsTree = true;
      }

      this.sprite.setPosition(pos.x, pos.y);
      if (this.overSprite != null) {
         this.overSprite.setPosition(pos.x, pos.y);
         if (this.nameUpper.equals("rock1_color")) {
            this.overSprite.setPosition(pos.x, pos.y + 4.0F);
         }

         if (this.nameUpper.equals("tree_savanna1")) {
            this.overSprite.setPosition(pos.x - 16.0F, pos.y);
         }
      }

      if (this.items != null && this.items.containsKey("torch")) {
         this.isTorch = true;
      }

      if (this.belowBridge) {
         this.isSolid = true;
      }

      if (this.routeBelongsTo != null && (this.routeBelongsTo.name.equals("wooded_lake1") || this.routeBelongsTo.name.equals("deep_forest"))) {
         String name = this.name + "_darker";
         DynamicTextures.cacheDarkerTile(name, this.sprite);
         this.sprite.setRegion(DynamicTextures.get(name));
      }
   }

   public Tile(String tileName, Vector2 pos) {
      this(tileName, "", pos);
   }

   public Tile(String tileName, Vector2 pos, boolean color) {
      this(tileName, "", pos, color, null);
   }

   public Tile(String tileName, Vector2 pos, boolean color, Route routeBelongsTo) {
      this(tileName, "", pos, color, routeBelongsTo);
   }

   public boolean isSign() {
      return this.nameUpper.equals("gravestone3")
         || this.nameUpper.equals("sign_built1")
         || this.nameUpper.equals("sign_desert1")
         || this.nameUpper.equals("warp_tile1");
   }

   public boolean isWall() {
      return !this.nameUpper.contains("roof");
   }

   public boolean isWall2() {
      return !this.nameUpper.contains("gate") && !this.nameUpper.contains("fence") && !this.nameUpper.contains("roof");
   }

   public boolean is(String name) {
      return this.name.contains(name) || this.nameUpper.contains(name);
   }

   public String ledgeDir() {
      return this.ledgeDir == null ? "" : this.ledgeDir;
   }

   public void updateMiniMap(Game game) {
      if (!this.name.equals("tree_large1_noSprite") && !this.name.equals("blank1")) {
         Pixmap currPixmap;
         int left;
         int bottom;
         int regionWidth;
         int regionHeight;
         if (this.overSprite != null) {
            currPixmap = this.overSprite.getPixmap();
            left = this.overSprite.getRegionX();
            bottom = this.overSprite.getRegionY();
            regionWidth = this.overSprite.getRegionWidth();
            regionHeight = this.overSprite.getRegionHeight();
         } else {
            currPixmap = this.sprite.getPixmap();
            left = this.sprite.getRegionX();
            bottom = this.sprite.getRegionY();
            regionWidth = this.sprite.getRegionWidth();
            regionHeight = this.sprite.getRegionHeight();
         }

         int offset = 7;
         if (this.name.contains("grass")) {
            offset = 4;
         } else if (this.name.equals("tree_large1")) {
            offset = 6;
         }

         int x = (int)(this.position.x - game.map.bottomLeft.x) / 8;
         int y = (int)(game.map.topRight.y - this.position.y) / 8;
         int offsetY = (regionHeight / 16 - 1) * 2;

         for (int m = 0; m * 16 < regionWidth; m++) {
            for (int n = 0; n * 16 < regionHeight; n++) {
               int color = currPixmap.getPixel(m * 16 + offset + left, n * 16 + offset + bottom);
               Game.staticGame.map.minimap.drawPixel(x + m * 2, y + n * 2 - offsetY, color);
               color = currPixmap.getPixel((m + 1) * 16 - 1 - offset + left, n * 16 + offset + bottom);
               Game.staticGame.map.minimap.drawPixel(x + m * 2 + 1, y + n * 2 - offsetY, color);
               color = currPixmap.getPixel(m * 16 + offset + left, (n + 1) * 16 - 1 - offset + bottom);
               Game.staticGame.map.minimap.drawPixel(x + m * 2, y + n * 2 + 1 - offsetY, color);
               color = currPixmap.getPixel((m + 1) * 16 - 1 - offset + left, (n + 1) * 16 - 1 - offset + bottom);
               Game.staticGame.map.minimap.drawPixel(x + m * 2 + 1, y + n * 2 + 1 - offsetY, color);
            }
         }
      }
   }

   public void guessBiomeType() {
      if (this.name.contains("sand")) {
         this.biome = "beach";
         this.routeBelongsTo = new Route("beach2", 22);
      } else if (this.name.contains("desert")) {
         this.biome = "desert";
         this.routeBelongsTo = new Route("desert1", 22);
      } else if (this.name.contains("mountain")) {
         this.biome = "mountain";
         this.routeBelongsTo = new Route("mountain1", 22);
      } else if (this.name.contains("snow")) {
         this.biome = "tundra";
         this.routeBelongsTo = new Route("snow1", 22);
      } else if (this.name.contains("volcano")) {
         this.biome = "volcano";
         this.routeBelongsTo = new Route("volcano1", 22);
      } else if (this.name.contains("graveyard")
         || this.name.equals("green9")
         || this.name.equals("green10")
         || this.name.equals("green11")
         || this.name.equals("green12")) {
         this.biome = "graveyard";
         this.routeBelongsTo = new Route("graveyard1", 22);
      } else if (this.name.contains("savanna")) {
         this.biome = "savanna";
         this.routeBelongsTo = new Route("savanna2", 22);
      } else if (this.name.contains("green")) {
         this.biome = "savanna";
         this.routeBelongsTo = new Route("forest1", 22);
      }
   }

   public void pickUpItem(Player player) {
      if (!this.nameUpper.contains("hole")) {
         this.nameUpper = "";
         this.init();
      }

      int amount = this.hasItemAmount;
      if (player.hasItem(this.hasItem)) {
         amount += player.getItemAmount(this.hasItem);
      }

      player.setItemAmount(this.hasItem, amount);
      this.hasItem = null;
      this.hasItemAmount = 0;
   }

   public void onPressA(Game game) {
      if (this.hasItem != null && !this.isSign()) {
         if (game.type == Game.Type.CLIENT) {
            game.client.sendTCP(new Network.PickupItem(game.player.network.id, game.player.dirFacing));
         } else {
            game.playerCanMove = false;
            String number = "a";
            String plural = "";
            if (this.hasItemAmount > 1) {
               number = String.valueOf(this.hasItemAmount);
               plural = "S";
               if (this.hasItem.endsWith("s")) {
                  plural = "ES";
               }
            }

            game.insertAction(
               new DisplayText(
                  game,
                  "Found " + number + " " + this.hasItem.toUpperCase(Locale.ROOT) + plural + "!",
                  "fanfare1.ogg",
                  null,
                  new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
               )
            );
            this.pickUpItem(game.player);
         }
      } else {
         class ChoiceAction extends Action {
            Player.Craft craft;

            public ChoiceAction(Player.Craft craft, Action nextAction) {
               super();
               this.craft = craft;
               this.nextAction = nextAction;
            }

            @Override
            public void step(Game game) {
               Tile.this.hasItemAmount = Tile.this.hasItemAmount + this.craft.amount;
               if (Tile.this.hasItemAmount > 107) {
                  this.craft.amount = this.craft.amount - (Tile.this.hasItemAmount - 107);
               }

               if (Tile.this.hasItemAmount >= 107) {
                  this.nextAction = new WaitFrames(
                     game,
                     20,
                     new SetField(
                        Tile.this,
                        "nameUpper",
                        "spiritomb",
                        new CallMethod(
                           Tile.this,
                           "init",
                           new Object[0],
                           new PokemonFrame(new Pokemon("spiritomb", 2, Pokemon.Generation.CRYSTAL, false, false), this.nextAction)
                        )
                     )
                  );
               }

               if (this.craft.amount == 69) {
                  this.nextAction = new DisplayText(game, "69... Nice...", null, null, this.nextAction);
               }

               game.actionStack.remove(this);
               game.insertAction(
                  game.player.new RemoveFromInventory(
                     this.craft.name,
                     this.craft.amount,
                     new DisplayText(game, "The spirits entered into the keystone...", "attacks/hypnosis_player_gsc/sound.ogg", null, this.nextAction)
                  )
               );
            }
         }


         class DrawMysteryNPC extends Action {
            Texture texture;
            Vector2 position;
            public boolean stop = false;

            public DrawMysteryNPC(Action nextAction) {
               super();
               this.texture = TextureCache.get(Gdx.files.internal("npc1.png"));
               this.nextAction = nextAction;
            }

            @Override
            public String getCamera() {
               return "map";
            }

            @Override
            public Action.Layer getLayer() {
               return Action.Layer.map_114;
            }

            @Override
            public void firstStep(Game game) {
               game.insertAction(this.nextAction);
               Vector3 worldCoords = game.cam.unproject(new Vector3(0.0F, 0.0F, 0.0F));
               this.position = new Vector2(worldCoords.x, worldCoords.y - 16.0F);
            }

            @Override
            public void step(Game game) {
               game.mapBatch.draw(this.texture, this.position.x, this.position.y + 1.0F);
               if (this.stop) {
                  game.actionStack.remove(this);
               }
            }
         }

         if (game.map.pokemon.containsKey(this.position)
            && game.map.pokemon.get(this.position).mapTiles == game.map.tiles
            && !game.map.pokemon.get(this.position).aggroPlayer) {
            Pokemon pokemon = game.map.pokemon.get(this.position);
            if (game.type == Game.Type.CLIENT) {
               game.client.sendTCP(new Network.PausePokemon(game.player.network.id, pokemon.position, true));
               return;
            }

            Action nextAction = new Action() {
               @Override
               public String getCamera() {
                  return "gui";
               }
            };
            game.playerCanMove = false;
            if (pokemon.specie.name.equals("darmanitanzen") && pokemon.previousOwner == null) {
               nextAction.append(new DisplayText(game, "A statue of an ancient Pokemon.", null, null, null));
               if (game.player.hasItem("ragecandybar")) {
                  nextAction.append(
                     new DisplayText(
                        game,
                        "Give it a RageCandyBar?",
                        null,
                        true,
                        false,
                        new DrawYesNoMenu(
                           null,
                           new DisplayText.Clear(
                              game,
                              new WaitFrames(
                                 game,
                                 6,
                                 game.player.new RemoveFromInventory(
                                    "ragecandybar",
                                    1,
                                    new DisplayText(
                                       game,
                                       "The statue responded to the RageCandyBar...",
                                       null,
                                       null,
                                       new DisplayText(
                                          game,
                                          "The awakened DARMANITAN attacked!",
                                          null,
                                          null,
                                          new SetField(
                                             game.battle,
                                             "oppPokemon",
                                             pokemon,
                                             new CallMethod(
                                                game.player,
                                                "setCurrPokemon",
                                                new Object[0],
                                                new SetField(game.musicController, "startBattle", "wild", new Battle.GetIntroAction(null))
                                             )
                                          )
                                       )
                                    )
                                 )
                              )
                           ),
                           new DisplayText.Clear(
                              game, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, new SetField(pokemon, "canMove", true, null)))
                           )
                        )
                     )
                  );
               } else {
                  nextAction.append(new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null)));
               }

               game.insertAction(nextAction);
               return;
            }

            pokemon.canMove = false;
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

            boolean isBaseSpecies = true;
            if (Pokemon.baseSpecies.get(pokemon.specie.name) != null) {
               isBaseSpecies = Pokemon.baseSpecies.get(pokemon.specie.name).equals(pokemon.specie.name);
            }

            if (game.player.currFieldMove.equals("ATTACK")) {
               if (isBaseSpecies && !pokemon.isEgg) {
                  pokemon.happiness = pokemon.specie.baseHappiness;
               }

               game.battle.oppPokemon = pokemon;
               game.player.setCurrPokemon();
               game.insertAction(
                  new SetField(
                     pokemon,
                     "dirFacing",
                     oppDir,
                     new WaitFrames(
                        game,
                        20,
                        new SplitAction(
                           pokemon.new Emote("!", null),
                           new WaitFrames(
                              game, 20, new PlayMusic(pokemon, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
                           )
                        )
                     )
                  )
               );
               return;
            }

            if (pokemon.previousOwner != game.player && !pokemon.isEgg && Pokemon.skittishMons.contains(pokemon.specie.name)) {
               game.battle.oppPokemon = pokemon;
               game.player.setCurrPokemon();
               game.insertAction(
                  new SetField(
                     pokemon,
                     "dirFacing",
                     oppDir,
                     new WaitFrames(
                        game,
                        20,
                        new SplitAction(
                           pokemon.new Emote("!", null),
                           new WaitFrames(
                              game, 20, new PlayMusic(pokemon, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
                           )
                        )
                     )
                  )
               );
               return;
            }

            if (pokemon.previousOwner == game.player && pokemon.hasItem != null) {
               nextAction.append(
                  new DisplayText(
                     game,
                     pokemon.nickname.toUpperCase(Locale.ROOT) + " looks like it' holding something...",
                     null,
                     false,
                     true,
                     new DisplayText(
                        game,
                        pokemon.nickname.toUpperCase(Locale.ROOT) + " gave you " + pokemon.hasItem.toUpperCase(Locale.ROOT) + "!",
                        "fanfare1.ogg",
                        false,
                        true,
                        null
                     )
                  )
               );
               if (!game.player.alreadyDoneHarvestables.contains(pokemon.hasItem)) {
                  if (pokemon.hasItem.contains("manure")) {
                     nextAction.append(new DisplayText(game, "It smells pretty bad... surely it will come in handy!", null, false, true, null));
                  } else if (pokemon.hasItem.contains("wool")) {
                     nextAction.append(new DisplayText(game, "Mmm... feels soft and warm.", null, false, true, null));
                  } else if (pokemon.hasItem.contains("hard shell")) {
                     nextAction.append(new DisplayText(game, "It' a piece of it' shell... feels nice and sturdy.", null, false, true, null));
                  }

                  game.player.alreadyDoneHarvestables.add(pokemon.hasItem);
               }

               nextAction.append(
                  new SetField(
                     pokemon,
                     "harvestTimer",
                     0,
                     new SetField(
                        pokemon,
                        "hasItem",
                        null,
                        new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, new SetField(pokemon, "canMove", true, null)))
                     )
                  )
               );
               int amount = 1;
               if (game.player.hasItem(pokemon.hasItem)) {
                  amount = game.player.getItemAmount(pokemon.hasItem) + 1;
               }

               game.player.setItemAmount(pokemon.hasItem, amount);
               game.insertAction(nextAction);
               return;
            }

            boolean shouldAggro = Pokemon.aggroAnyway.contains(pokemon.specie.name) || !isBaseSpecies && !Pokemon.dontAggro.contains(pokemon.specie.name);
            if (pokemon.previousOwner != game.player && !pokemon.isEgg && shouldAggro) {
               if (!pokemon.interactedWith) {
                  nextAction = new SetField(
                     pokemon,
                     "dirFacing",
                     oppDir,
                     new WaitFrames(game, 20, new SplitAction(pokemon.new Emote("skull", null), new WaitFrames(game, 20, new PlayMusic(pokemon, null))))
                  );
                  nextAction.append(
                     new DisplayText(
                        game, pokemon.nickname.toUpperCase(Locale.ROOT) + " seems aggressive... it may attack if provoked!", null, false, true, null
                     )
                  );
                  pokemon.interactedWith = true;
                  nextAction.append(new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, new SetField(pokemon, "canMove", true, null))));
               } else {
                  nextAction = new SetField(
                     pokemon,
                     "dirFacing",
                     oppDir,
                     new WaitFrames(
                        game,
                        20,
                        new SplitAction(
                           pokemon.new Emote("!", null),
                           new WaitFrames(
                              game,
                              20,
                              new PlayMusic(
                                 pokemon,
                                 new WaitFrames(
                                    game,
                                    10,
                                    new SetField(
                                       game, "playerCanMove", true, new SetField(pokemon, "canMove", true, new SetField(pokemon, "aggroPlayer", true, null))
                                    )
                                 )
                              )
                           )
                        )
                     )
                  );
               }

               game.insertAction(nextAction);
               return;
            }

            Action emote;
            if (pokemon.isEgg) {
               emote = pokemon.new Emote("...", null);
            } else if (pokemon.previousOwner != game.player) {
               emote = pokemon.new Emote("happy", null);
            } else if (!pokemon.inHabitat) {
               emote = pokemon.new Emote("uncomfortable", null);
            } else if (pokemon.loveInterest != null) {
               emote = pokemon.new Emote("heart", null);
            } else {
               emote = pokemon.new Emote("happy", null);
            }

            nextAction = new SetField(
               pokemon, "dirFacing", oppDir, new WaitFrames(game, 20, new SplitAction(emote, new WaitFrames(game, 20, new PlayMusic(pokemon, null))))
            );
            if (pokemon.isEgg) {
               String[] huhs = new String[]{"Neat!", "Hey!", "Look!", "Wow!", "Huh?", "Hmm..."};
               nextAction.append(new DisplayText(game, huhs[Game.rand.nextInt(huhs.length)] + " A POKéMON egg!", null, false, true, null));
            } else if (pokemon.previousOwner != game.player) {
               nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " seems friendly. ", null, false, true, null));
            } else if (pokemon.mapTiles != game.map.overworldTiles) {
               nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " is enjoying itself.", null, false, true, null));
            } else if (!pokemon.inHabitat) {
               nextAction.append(
                  new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " seems uncomfortable in this environment. ", null, false, true, null)
               );
            } else if (pokemon.loveInterest != null) {
               nextAction.append(
                  new DisplayText(
                     game,
                     pokemon.nickname.toUpperCase(Locale.ROOT) + " seems interested in " + pokemon.loveInterest.nickname.toUpperCase(Locale.ROOT) + ".",
                     null,
                     false,
                     true,
                     null
                  )
               );
            } else {
               nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " seems happy. ", null, false, true, null));
            }

            String text = pokemon.isEgg ? "Pick it up?" : "Add " + pokemon.nickname.toUpperCase(Locale.ROOT) + " to your party?";
            nextAction.append(
               new DisplayText(
                  game,
                  text,
                  null,
                  true,
                  false,
                  new DrawYesNoMenu(
                     null,
                     new DisplayText.Clear(game, new WaitFrames(game, 6, new RunCode(() -> {
                        Vector2 position = pokemon.position.cpy();
                        if (pokemon.standingAction instanceof Pokemon.Moving) {
                           position = ((Pokemon.Moving)pokemon.standingAction).targetPos;
                        } else if (pokemon.standingAction instanceof Pokemon.Sliding) {
                           position = ((Pokemon.Sliding)pokemon.standingAction).targetPos;
                        }

                        Action action = pokemon.new AddToInventory(position, null);
                        action.append(new RunCode(() -> {
                           game.playerCanMove = true;
                           pokemon.canMove = true;
                        }, null));
                        action.firstStep(game);
                     }, null))),
                     new DisplayText.Clear(
                        game, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, new SetField(pokemon, "canMove", true, null)))
                     )
                  )
               )
            );
            game.insertAction(nextAction);
         } else if (game.player.hmPokemon != null
            && !game.player.hmPokemon.isEgg
            && game.player.currFieldMove.equals("")
            && game.player.hmPokemon.dirFacing.equals(Player.oppDirs.get(game.player.dirFacing))) {
            game.playerCanMove = false;
            Tile facingTile = game.map.tiles.get(game.player.facingPos());
            if (game.map.timeOfDay.equals("night")) {
               if (game.player.hmPokemon.types.contains("PSYCHIC")) {
                  String[] texts = new String[]{"POKEMON looks sleepy."};
               } else if (game.player.hmPokemon.types.contains("DARK")) {
                  String[] texts = new String[]{"POKEMON blends into the night.", "POKEMON is scouting for ghosts."};
               } else if (game.player.hmPokemon.types.contains("FAIRY")) {
                  String[] var128 = new String[]{"POKEMON doesn't want me to go too far."};
               }
            }

            String[] texts;
            if (facingTile.routeBelongsTo != null && facingTile.routeBelongsTo.type().equals("desert")) {
               if (game.player.hmPokemon.types.contains("ICE")) {
                  texts = new String[]{"POKEMON keeps away from the sunlight.", "POKEMON dislikes the heat."};
               } else if (game.player.hmPokemon.types.contains("DRAGON")) {
                  texts = new String[]{"POKEMON is soaking up the sun."};
               } else if (game.player.hmPokemon.types.contains("DRAGON")) {
                  texts = new String[]{"[Steel Type] is hot to the touch."};
               } else if (game.player.hmPokemon.types.contains("GROUND")) {
                  texts = new String[]{"POKEMON is submerged in sand.", "POKEMON is moving easily in the sand."};
               } else if (game.player.hmPokemon.types.contains("FIRE")) {
                  texts = new String[]{"POKEMON loves the hot sun.", "POKEMON is having trouble with the sand."};
               } else {
                  texts = new String[]{
                     "POKEMON feels sunlight on its back.", "POKEMON digs into the sand.", "POKEMON makes its way across the sand.", "POKEMON shakes sand off."
                  };
               }
            } else if (facingTile.routeBelongsTo != null && facingTile.routeBelongsTo.name.contains("pkmnmansion")) {
               if (game.player.hmPokemon.types.contains("PSYCHIC")) {
                  texts = new String[]{"POKEMON picks up a signal.", "POKEMON pieces events together."};
               } else {
                  texts = new String[]{
                     "POKEMON watches its step.", "POKEMON notices scattered papers.", "POKEMON nervously follows.", "POKEMON looks confused."
                  };
               }
            } else if (facingTile.routeBelongsTo != null && facingTile.routeBelongsTo.name.contains("snow")) {
               if (game.player.hmPokemon.types.contains("ICE")) {
                  texts = new String[]{"POKEMON feels comfortable under the snow.", "POKEMON takes in the cold environment."};
               } else if (game.player.hmPokemon.types.contains("GRASS") || game.player.hmPokemon.types.contains("GROUND")) {
                  texts = new String[]{"POKEMON shutters from the air.", "POKEMON does not take a liking to the weather."};
               } else if (game.player.hmPokemon.types.contains("DRAGON")) {
                  texts = new String[]{"POKEMON is shivering."};
               } else if (game.player.hmPokemon.types.contains("PSYCHIC")) {
                  texts = new String[]{"POKEMON has a brain freeze."};
               } else {
                  texts = new String[]{
                     "POKEMON notices the frigid air.", "POKEMON stares into ice crystals.", "POKEMON collects snow.", "POKEMON feels the powdery snow."
                  };
               }
            } else if (facingTile.routeBelongsTo != null && facingTile.routeBelongsTo.name.contains("beach")) {
               if (game.player.hmPokemon.types.contains("WATER")) {
                  texts = new String[]{"POKEMON takes in the sea's aroma.", "POKEMON sticks to the waves."};
               } else {
                  texts = new String[]{
                     "POKEMON notices the crashing waves.", "POKEMON plays in the sand.", "POKEMON spots shells in the sand.", "POKEMON shakes sand off."
                  };
               }
            } else if (facingTile.routeBelongsTo != null && facingTile.routeBelongsTo.name.contains("mountain")) {
               if (game.player.hmPokemon.types.contains("ROCK") || game.player.hmPokemon.types.contains("GROUND")) {
                  texts = new String[]{"POKEMON is impressed by the landscape.", "Wow! POKEMON blends in with the environment!"};
               } else if (game.player.hmPokemon.types.contains("FIGHTING")) {
                  texts = new String[]{"POKEMON wants to mountain climb."};
               } else if (game.player.hmPokemon.types.contains("FLYING")) {
                  texts = new String[]{"POKEMON is enjoying the breeze.", "POKEMON wants to soar the skies."};
               } else {
                  texts = new String[]{"POKEMON enjoys the view.", "POKEMON spots pretty rocks.", "POKEMON watches by the cliffside."};
               }
            } else if (facingTile.routeBelongsTo != null && facingTile.routeBelongsTo.name.contains("forest")) {
               if (game.player.hmPokemon.types.contains("GRASS")) {
                  texts = new String[]{"POKEMON admires the fresh plants.", "POKEMON reaches for a branch."};
               } else if (game.player.hmPokemon.types.contains("BUG")) {
                  texts = new String[]{"POKEMON is chittering away.", "POKEMON is enjoying the flowers.", "POKEMON is on the lookout."};
               } else if (game.player.hmPokemon.types.contains("FLYING")) {
                  texts = new String[]{"POKEMON is on the lookout."};
               } else {
                  texts = new String[]{"POKEMON stares into the treetops.", "POKEMON takes in the natural scent.", "POKEMON makes its way across the path."};
               }
            } else if (game.player.hmPokemon.types.contains("FIGHTING")) {
               texts = new String[]{"POKEMON is stretching.", "POKEMON wants to spar."};
            } else if (game.player.hmPokemon.types.contains("PSYCHIC")) {
               texts = new String[]{"POKEMON is thinking hard."};
            } else if (game.player.hmPokemon.types.contains("DARK")) {
               texts = new String[]{"POKEMON is looking wary."};
            } else if (game.player.hmPokemon.types.contains("FAIRY")) {
               texts = new String[]{"POKEMON wants to play."};
            } else {
               texts = new String[]{
                  "POKEMON is carefully eyeing you.",
                  "POKEMON is keeping at your pace.",
                  "POKEMON is eager to help.",
                  "POKEMON is surveying the area.",
                  "POKEMON looks towards the sky.",
                  "POKEMON notices you.",
                  "POKEMON looks happy.",
                  "POKEMON darts around.",
                  "POKEMON passes the time.",
                  "What? POKEMON hugs you!",
                  "POKEMON shakes its head."
               };
            }

            String text = texts[Game.rand.nextInt(texts.length)];
            text = text.replace("POKEMON", game.player.hmPokemon.nickname.toUpperCase(Locale.ROOT));
            game.insertAction(new DisplayText(game, text, null, null, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null))));
         } else if (this.nameUpper.equals("odd_keystone2")) {
            game.playerCanMove = false;
            int amountLeft = 107 - this.hasItemAmount;
            String text = "A voice whispers from within... " + String.valueOf(amountLeft) + "... bring " + amountLeft + "...";
            if (amountLeft == 69) {
               text = "A voice whispers from within... bring 69... Nice...";
            }

            Action nextAction = new DisplayText(game, text, null, null, null);
            if (game.player.hasItem("life force")) {
               Player.Craft craft = new Player.Craft("life force", 1);
               craft.requirements.add(new Player.Craft("life force", 1));
               nextAction.append(
                  new DisplayText(
                     game,
                     "Use life force?",
                     null,
                     true,
                     false,
                     new SelectAmount(
                        craft,
                        new DisplayText.Clear(game, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null))),
                        new DisplayText.Clear(
                           game, new WaitFrames(game, 6, new ChoiceAction(craft, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null))))
                        )
                     )
                  )
               );
            } else {
               nextAction.append(new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null)));
            }

            game.insertAction(nextAction);
         } else if (this.nameUpper.contains("warp_tile")) {
            game.playerCanMove = false;
            Action nextAction = new DisplayText(
               game,
               "Set label text?",
               null,
               true,
               false,
               new DrawYesNoMenu(
                  null,
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(game, 3, new Tile.SetSignText(this.hasItem, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null))))
                  ),
                  new DisplayText.Clear(game, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null)))
               )
            );
            game.insertAction(nextAction);
         } else if (this.isSign() && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            if (this.hasItem == null) {
               Action nextAction = new Tile.SetSignText(null, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null)));
               game.insertAction(nextAction);
            } else {
               Action nextAction = new DisplayText(game, this.hasItem, null, null, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null)));
               game.insertAction(nextAction);
            }
         } else if (this.nameUpper.equals("gravestone2") && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            Action nextAction = new DisplayText(
               game, "The text is too faded to make out...", null, null, new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null))
            );
            Tile down = game.map.tiles.get(this.position.cpy().add(0.0F, -32.0F));
            Tile right = game.map.tiles.get(this.position.cpy().add(16.0F, -16.0F));
            if (Game.rand.nextInt(128) == 0 && (down != null && !down.isSolid || right != null && !right.isSolid)) {
               game.playerCanMove = true;
               game.player.acceptInput = false;
               DrawMysteryNPC drawMysteryNPC = new DrawMysteryNPC(null);
               drawMysteryNPC.append(new DisplayText(game, "The text is too faded to make out...", null, null, null));
               if (down != null && !down.isSolid) {
                  drawMysteryNPC.append(
                     new SetField(game.player, "dirFacing", "down", new SetField(game.player, "currSprite", game.player.standingSprites.get("down"), null))
                  );
               } else {
                  drawMysteryNPC.append(
                     new SetField(game.player, "dirFacing", "right", new SetField(game.player, "currSprite", game.player.standingSprites.get("right"), null))
                  );
               }

               drawMysteryNPC.append(
                  new PlayerMoving(
                     game,
                     game.player,
                     false,
                     new WaitFrames(game, 10, new SetField(game.player, "acceptInput", true, new SetField(drawMysteryNPC, "stop", true, null)))
                  )
               );
               nextAction = drawMysteryNPC;
            }

            game.insertAction(nextAction);
         } else if (this.name.equals("cave1_regi5")) {
            Tile leftTile = game.map.tiles.get(this.position.cpy().add(-16.0F, 0.0F));
            if (leftTile != null) {
               leftTile.onPressA(game);
            }
         } else if (this.name.equals("cave1_regi2")) {
            game.playerCanMove = false;
            Action nextAction = new DisplayText(game, "...", null, false, true, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null)));
            game.insertAction(nextAction);
         } else if (this.name.equals("cave1_regi3")) {
            MoveWater.regiTimer2 = 0;
            game.playerCanMove = false;
            game.battle.oppPokemon = new Pokemon("regigigas", 70);
            game.battle.oppPokemon.position = this.position.cpy();
            game.player.setCurrPokemon();
            game.insertAction(RegigigasBattle.getIntroAction(game));
         } else if (this.nameUpper.contains("REGI")) {
            Pokemon regi = new Pokemon(this.nameUpper.toLowerCase(Locale.ROOT), 40);
            game.playerCanMove = false;
            game.battle.oppPokemon = regi;
            game.player.setCurrPokemon();
            String cryText = "";
            if (this.nameUpper.equals("REGIELEKI")) {
               cryText = "Zizi zizizi.";
            } else if (this.nameUpper.equals("REGIDRAGO")) {
               cryText = "Zagd.";
            } else if (this.nameUpper.equals("REGISTEEL")) {
               cryText = "Ji-ji-ze-ji-zoh.";
            } else if (this.nameUpper.equals("REGIROCK")) {
               cryText = "Zaza zari za...";
            } else if (this.nameUpper.equals("REGICE")) {
               cryText = "Jakiih!";
            }

            game.insertAction(
               new DisplayText(
                  game,
                  cryText,
                  "pokemon/cries/" + regi.dexNumber + ".ogg",
                  null,
                  new SetField(
                     game.musicController,
                     "startBattle",
                     "regi_battle1",
                     new SplitAction(
                        new CallMethod(game.uiBatch, "setColor", new Object[]{new Color(1.0F, 1.0F, 1.0F, 1.0F)}, null),
                        new CallMethod(
                           game.mapBatch,
                           "setColor",
                           new Object[]{new Color(0.5F, 0.5F, 0.5F, 1.0F)},
                           new SplitAction(new RegigigasIntroAnim.BattleIntro(null), new WaitFrames(game, 230, Battle.getIntroAction(game)))
                        )
                     )
                  )
               )
            );
         } else if (this.name.equals("cave1_regipedistal1")) {
            game.playerCanMove = false;
            game.player.isCrafting = true;
            game.player.regiCrafts.clear();
            if (this.items().containsKey("REGISTEEL")) {
               Player.Craft craft = new Player.Craft("REGISTEEL", 1);
               craft.requirements.add(new Player.Craft("metal coat", 47));
               craft.requirements.add(new Player.Craft("spell tag", 1));
               game.player.regiCrafts.add(craft);
            }

            if (this.items().containsKey("REGIROCK")) {
               Player.Craft craft = new Player.Craft("REGIROCK", 1);
               craft.requirements.add(new Player.Craft("hard stone", 47));
               craft.requirements.add(new Player.Craft("spell tag", 1));
               game.player.regiCrafts.add(craft);
            }

            if (this.items().containsKey("REGICE")) {
               Player.Craft craft = new Player.Craft("REGICE", 1);
               craft.requirements.add(new Player.Craft("nevermeltice", 47));
               craft.requirements.add(new Player.Craft("spell tag", 1));
               game.player.regiCrafts.add(craft);
            }

            if (this.items().containsKey("REGIDRAGO")) {
               Player.Craft craft = new Player.Craft("REGIDRAGO", 1);
               craft.requirements.add(new Player.Craft("dragon scale", 43));
               craft.requirements.add(new Player.Craft("dragon fang", 4));
               craft.requirements.add(new Player.Craft("spell tag", 1));
               game.player.regiCrafts.add(craft);
            }

            if (this.items().containsKey("REGIELEKI")) {
               Player.Craft craft = new Player.Craft("REGIELEKI", 1);
               craft.requirements.add(new Player.Craft("magnet", 43));
               craft.requirements.add(new Player.Craft("binding band", 4));
               craft.requirements.add(new Player.Craft("spell tag", 1));
               game.player.regiCrafts.add(craft);
            }

            game.insertAction(
               new DrawCraftsMenu.Intro(
                  null,
                  9,
                  new DrawCraftsMenu(
                     game, game.player.regiCrafts, new SetField(game, "playerCanMove", true, new SetField(game.player, "isCrafting", false, null))
                  )
               )
            );
         } else if (this.nameUpper.equals("ball_kiln1")) {
            game.playerCanMove = false;
            game.player.isCrafting = true;
            game.insertAction(
               new DrawCraftsMenu.Intro(
                  null,
                  9,
                  new DrawCraftsMenu(game, Player.kilnCrafts, new SetField(game, "playerCanMove", true, new SetField(game.player, "isCrafting", false, null)))
               )
            );
         } else if (this.name.equals("building1_pokecenter1_right")) {
            Tile leftTile = game.map.tiles.get(this.position.cpy().add(-16.0F, 0.0F));
            if (leftTile != null) {
               leftTile.onPressA(game);
            }
         } else if (this.name.equals("building1_fossilreviver1") && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            boolean foundRevivedMon = false;
            Iterator var71 = game.map.tiles.values().iterator();

            while (true) {
               if (var71.hasNext()) {
                  Tile tile = (Tile)var71.next();
                  if (!tile.nameUpper.contains("revived")) {
                     continue;
                  }

                  foundRevivedMon = true;
               }

               if (foundRevivedMon) {
                  Action nextAction = new DisplayText(
                     game, "It' not responding...", null, false, true, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
                  );
                  game.insertAction(nextAction);
                  return;
               }

               if (game.player.hmPokemon == null || !game.player.currFieldMove.equals("POWER")) {
                  Action nextAction = new DisplayText(
                     game,
                     "No power... an ELECTRIC type could get it running.",
                     null,
                     false,
                     true,
                     new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
                  );
                  game.insertAction(nextAction);
                  return;
               }

               game.player.isCrafting = true;
               Action nextAction = new FossilMachinePowerUp(
                  false,
                  new PlayMusic(
                     "pc_on1",
                     new DisplayText(
                        game,
                        game.player.hmPokemon.nickname.toUpperCase(Locale.ROOT) + " powered up the machine!",
                        null,
                        false,
                        true,
                        new DrawCraftsMenu.Intro(
                           null,
                           9,
                           new DrawCraftsMenu(
                              game,
                              game.player.fossilCrafts,
                              new FossilMachinePowerUp(
                                 true,
                                 new PlayMusic("pc_off1", new SetField(game, "playerCanMove", true, new SetField(game.player, "isCrafting", false, null)))
                              )
                           )
                        )
                     )
                  )
               );
               game.insertAction(nextAction);
               break;
            }
         } else if (this.nameUpper.contains("revived_")) {
            game.playerCanMove = false;
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

            String name = this.nameUpper.split("_")[1].toLowerCase(Locale.ROOT);
            Pokemon pokemon = new Pokemon(name, averageLevel, Pokemon.Generation.CRYSTAL);
            if (game.player.currFieldMove.equals("CHARM") && game.player.hmPokemon != null) {
               if (game.player.pokemon.size() >= 6) {
                  game.insertAction(
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new PlayMusic(
                              "error1",
                              new DisplayText(
                                 game, "Not enough room in your party!", null, null, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
                              )
                           )
                        )
                     )
                  );
                  return;
               }

               Action newAction = new DisplayText(
                  game,
                  game.player.name + " received " + pokemon.nickname.toUpperCase(Locale.ROOT) + "!",
                  "fanfare1.ogg",
                  null,
                  new SetField(game, "playerCanMove", true, null)
               );
               this.nameUpper = "";
               game.player.pokemon.add(pokemon);
               pokemon.previousOwner = game.player;
               game.insertAction(newAction);
               return;
            }

            game.playerCanMove = false;
            game.battle.oppPokemon = pokemon;
            pokemon.onTile = this;
            game.player.setCurrPokemon();
            game.insertAction(
               new DisplayText(
                  game,
                  pokemon.nickname.toUpperCase(Locale.ROOT) + " attacked!",
                  "pokemon/cries/" + pokemon.dexNumber + ".ogg",
                  null,
                  new WaitFrames(game, 10, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
               )
            );
         } else if (this.nameUpper.equals("volcarona")) {
            String name = this.nameUpper;
            Pokemon pokemon = new Pokemon(name, 60, Pokemon.Generation.CRYSTAL);
            game.playerCanMove = false;
            game.battle.oppPokemon = pokemon;
            pokemon.onTile = this;
            game.player.setCurrPokemon();
            game.insertAction(
               new DisplayText(
                  game,
                  "Vraahhbrbrbr!",
                  "pokemon/cries/" + pokemon.dexNumber + ".ogg",
                  null,
                  new WaitFrames(
                     game, 10, new SetField(game.musicController, "startBattle", "bw_legendary_theme3", new WaitFrames(game, 80, Battle.getIntroAction(game)))
                  )
               )
            );
         } else if (this.nameUpper.equals("spiritomb")) {
            String name = this.nameUpper;
            Pokemon pokemon = new Pokemon(name, 25, Pokemon.Generation.CRYSTAL);
            game.playerCanMove = false;
            game.battle.oppPokemon = pokemon;
            pokemon.onTile = this;
            game.player.setCurrPokemon();
            game.insertAction(
               new DisplayText(
                  game,
                  "Yulaaah!",
                  "pokemon/cries/" + pokemon.dexNumber + ".ogg",
                  null,
                  new WaitFrames(game, 10, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
               )
            );
         } else if (this.nameUpper.equals("pokedoll1")) {
            String name = "poké doll";
            game.playerCanMove = false;
            game.insertAction(
               new SplitAction(
                  new WaitFrames(
                     game, 15, new SetField(this, "nameUpper", "", new SetField(this, "overSprite", null, new CallMethod(this, "init", new Object[0], null)))
                  ),
                  new DisplayText(
                     game,
                     "Found a " + name.toUpperCase(Locale.ROOT) + "!",
                     "fanfare1.ogg",
                     null,
                     new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
                  )
               )
            );
            int amount = 1;
            if (game.player.hasItem(name)) {
               amount += game.player.getItemAmount(name);
            }

            game.player.setItemAmount(name, amount);
         } else if (this.nameUpper.equals("pokedoll1_banette") || this.nameUpper.equals("pokedoll1_mimikyu")) {
            String name = "banette";
            if (this.nameUpper.equals("pokedoll1_mimikyu")) {
               name = "mimikyu";
            }

            game.playerCanMove = false;
            game.battle.oppPokemon = new Pokemon(name, 24, Pokemon.Generation.CRYSTAL);
            game.battle.oppPokemon.mapTiles = game.map.overworldTiles;
            game.battle.oppPokemon.position = this.position.cpy();
            game.battle.oppPokemon.canMove = false;
            game.battle.oppPokemon.aggroPlayer = true;
            game.player.setCurrPokemon();
            game.insertAction(
               new SplitAction(
                  new PlayMusic("ledge2", null),
                  game.player.new Emote(
                     "!",
                     new DisplayText(
                        game,
                        "The doll became animated... and attacked!",
                        null,
                        null,
                        new SplitAction(
                           new WaitFrames(
                              game,
                              400,
                              new SetField(
                                 this,
                                 "nameUpper",
                                 "",
                                 new SetField(this, "overSprite", null, new CallMethod(this, "init", new Object[0], game.battle.oppPokemon.new Standing()))
                              )
                           ),
                           new WaitFrames(game, 10, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
                        )
                     )
                  )
               )
            );
         } else if (this.nameUpper.contains("house_wardrobe")) {
            game.playerCanMove = false;
            if (!game.player.currFieldMove.equals("")) {
               game.insertAction(
                  new PlayMusic(
                     "error1",
                     new DisplayText(
                        game, "Canì use this while using a Field Move.", null, null, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
                     )
                  )
               );
               return;
            }

            Action nextAction = new DisplayText(
               game,
               "Arrow left or right to change clothes color.",
               null,
               true,
               true,
               new ChangePlayerColor(new DisplayText.Clear(game, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))))
            );
            game.insertAction(nextAction);
         } else if (this.nameUpper.contains("house_vanity")) {
            game.playerCanMove = false;
            if (!game.player.currFieldMove.equals("")) {
               game.insertAction(
                  new PlayMusic(
                     "error1",
                     new DisplayText(
                        game, "Canì use this while using a Field Move.", null, null, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))
                     )
                  )
               );
               return;
            }

            Action nextAction = new DisplayText(
               game,
               "Arrow left or right to change appearance.",
               null,
               true,
               true,
               new ChangePlayerCharacter(new DisplayText.Clear(game, new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null))))
            );
            game.insertAction(nextAction);
         } else if (this.name.contains("pkmnmansion_ext_locked") || this.nameUpper.contains("pkmnmansion_ext_locked")) {
            game.playerCanMove = false;
            Action nextAction;
            if (!game.player.hasItem("secret key")) {
               nextAction = new DisplayText(game, "It' locked...", null, false, true, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null)));
            } else {
               nextAction = new DisplayText(
                  game,
                  "It' locked...",
                  null,
                  false,
                  true,
                  new DisplayText(
                     game,
                     "Open using the SECRET KEY?",
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
                              game.player.new RemoveFromInventory(
                                 "secret key",
                                 1,
                                 new Tile.PutTile(
                                    new Tile("pkmnmansion_ext_door", this.position.cpy(), true, this.routeBelongsTo),
                                    this.position.cpy(),
                                    new DisplayText(game, "The door opened!", "fanfare1.ogg", null, new SetField(game, "playerCanMove", true, null))
                                 )
                              )
                           )
                        ),
                        new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null)))
                     )
                  )
               );
            }

            game.insertAction(nextAction);
         } else if (this.name.equals("pkmnmansion_statue1") && this.doorTiles != null && game.player.dirFacing.equals("up")) {
            game.playerCanMove = false;
            ArrayList<Tile> flipDoors = new ArrayList<>();

            for (Vector2 pos : this.doorTiles) {
               flipDoors.add(game.map.interiorTiles.get(game.map.interiorTilesIndex - 1).get(pos));
            }

            Action nextAction = new DisplayText(
               game,
               "A hidden switch! Press it?",
               null,
               true,
               false,
               new DrawYesNoMenu(
                  null,
                  new DisplayText.Clear(
                     game, new WaitFrames(game, 20, new PlayMusic("enter1", new Tile.FlipDoorTile(flipDoors, new SetField(game, "playerCanMove", true, null))))
                  ),
                  new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null)))
               )
            );
            game.insertAction(nextAction);
         } else if (this.nameUpper.contains("bed")) {
            Vector2 newSpawnLoc = game.player.position.cpy();
            game.playerCanMove = false;
            Vector2 newPos = this.position.cpy();
            Action nextAction = new DisplayText(
               game,
               "Do you want to sleep?",
               null,
               true,
               false,
               new DrawYesNoMenu(null, new DisplayText.Clear(game, new WaitFrames(game, 20, new RunCode(() -> {
                  game.player.sleepingDir = newPos;
                  game.player.isSleeping = true;
                  game.playerCanMove = true;
                  game.player.spawnLoc = newSpawnLoc;
                  game.player.spawnIndex = -1;
                  if (game.map.tiles != game.map.overworldTiles) {
                     game.player.spawnIndex = game.map.interiorTilesIndex;
                  }
               }, null))), new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null))))
            );
            game.insertAction(nextAction);
         } else if (this.nameUpper.contains("onpress_above")) {
            Tile down = game.map.tiles.get(this.position.cpy().add(0.0F, -16.0F));
            String name = this.name;
            String nameUpper = this.nameUpper;
            Vector2 position = this.position;
            this.name = down.name;
            this.nameUpper = down.nameUpper;
            this.position = down.position;
            this.onPressA(game);
            this.name = name;
            this.nameUpper = nameUpper;
            this.position = position;
         } else if (this.nameUpper.contains("onpress_left")) {
            Tile left = game.map.tiles.get(this.position.cpy().add(-16.0F, 0.0F));
            String name = this.name;
            String nameUpper = this.nameUpper;
            this.name = left.name;
            this.nameUpper = left.nameUpper;
            this.onPressA(game);
            this.name = name;
            this.nameUpper = nameUpper;
         } else if (this.nameUpper.contains("house_couch")) {
            game.playerCanMove = false;
            game.insertAction(new WaitFrames(game, 30, game.player.new Sitting(this)));
         } else if (this.nameUpper.equals("house_window1")) {
            game.insertAction(new PlayMusic("seed1", null));
            if (this.overSprite.getRegionX() == 0) {
               this.overSprite.setRegion(16, 0, 16, 16);
            } else if (this.overSprite.getRegionX() == 16) {
               this.overSprite.setRegion(32, 0, 16, 16);
            } else {
               this.overSprite.setRegion(0, 0, 16, 16);
            }
         } else if (this.nameUpper.equals("house_window2")) {
            game.insertAction(new PlayMusic("seed1", null));
            if (this.overSprite.getRegionX() == 0) {
               this.overSprite.setRegion(16, 0, 16, 16);
            } else {
               this.overSprite.setRegion(0, 0, 16, 16);
            }
         } else if (this.nameUpper.contains("_plush")) {
            String name = "";
            String[] names = this.nameUpper.split("_plush");
            if (names.length > 1) {
               name = names[1];
            }

            game.playerCanMove = false;
            game.insertAction(
               new DisplayText(
                  game,
                  "A " + name.toUpperCase(Locale.ROOT) + " doll. Cute!",
                  null,
                  null,
                  new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null))
               )
            );
         } else if (this.nameUpper.equals("mewtwo_overworld")) {
            game.playerCanMove = false;
            SpecialMewtwo1 mewtwo = new SpecialMewtwo1(50, this);
            game.battle.oppPokemon = mewtwo;
            game.player.setCurrPokemon();
            game.musicController.inBattle = true;
            Action fadeMusic = new FadeMusic(game.currMusic, -0.025F, null);
            game.insertAction(
               new SplitAction(
                  fadeMusic,
                  new WaitFrames(game, 20, new DisplayText(game, "...", null, false, true, new WaitFrames(game, 100, new SpecialBattleMewtwo(game, mewtwo))))
               )
            );
         } else if (this.items().containsKey("torch")) {
            game.playerCanMove = false;
            game.insertAction(
               new DisplayText(
                  game,
                  "Remove torch?",
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
                           new PickupItem(this.items(), "torch", new SetField(this, "isTorch", false, new SetField(game, "playerCanMove", true, null)))
                        )
                     ),
                     new DisplayText.Clear(game, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null)))
                  )
               )
            );
         } else if (this.nameUpper.contains("rock1_color")) {
            game.playerCanMove = false;
            game.insertAction(
               new DisplayText(
                  game,
                  "A Rock-type POKéMON can break this using ROCK SMASH.",
                  null,
                  null,
                  new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null))
               )
            );
         } else if ((this.name.contains("water") || this.name.equals("desert2")) && game.player.currFieldMove.equals("") && game.player.currRod.contains("rod")
            )
          {
            String rodName = game.player.currRod;
            game.playerCanMove = false;
            String dirFacing = game.player.dirFacing;
            Sprite fishingSprite = new Sprite(game.player.fishingSprites);
            if (dirFacing.equals("up")) {
               fishingSprite.setRegion(0, 0, 16, 24);
            } else if (dirFacing.equals("down")) {
               fishingSprite.setRegion(16, 0, 16, 24);
            } else if (dirFacing.equals("left")) {
               fishingSprite.setRegion(32, 0, 24, 16);
               fishingSprite.flip(true, false);
            } else if (dirFacing.equals("right")) {
               fishingSprite.setRegion(32, 0, 24, 16);
            }

            Action nextAction = new PlayMusic(
               "rod1", new SplitAction(new SetField(game.player, "isFishing", true, null), new SetField(game.player, "currSprite", fishingSprite, null))
            );
            if (Game.rand.nextInt(2) != 0) {
               String[] huhs = new String[]{"!", "..."};
               String huh = huhs[Game.rand.nextInt(huhs.length)];
               nextAction.append(
                  new WaitFrames(
                     game,
                     120,
                     new DisplayText(
                        game,
                        "Not even a nibble" + huh,
                        null,
                        null,
                        new SplitAction(
                           new SetField(game.player, "isFishing", false, null), new SetField(game, "playerCanMove", true, new WaitFrames(game, 30, null))
                        )
                     )
                  )
               );
            } else {
               Route route = new Route("", 2);
               if (this.routeBelongsTo != null && this.routeBelongsTo.name.equals("oasis1")) {
                  route.name = "oasis_pond1";
               } else if (this.routeBelongsTo != null && this.routeBelongsTo.name.equals("wooded_lake_water1")) {
                  route.name = "wooded_lake_fishing1";
               } else if (this.name.equals("water2")) {
                  route.name = "sea1";
               } else if (this.name.equals("desert2")) {
                  route.name = "sand_fishing1";
               } else {
                  route.name = "river1";
               }

               ArrayList<String> eligiblePokemon = new ArrayList<>(route.allowedPokemon());

               for (String name : eligiblePokemon) {
                  System.out.println(name);
               }

               if (rodName.equals("old rod") || rodName.equals("good rod")) {
                  for (String name : route.superRodPokemon()) {
                     eligiblePokemon.remove(name);
                  }
               }

               if (rodName.equals("old rod")) {
                  for (String name : route.goodRodPokemon()) {
                     eligiblePokemon.remove(name);
                  }
               }

               for (String name : eligiblePokemon) {
                  System.out.println(name);
               }

               String name = eligiblePokemon.get(Game.rand.nextInt(eligiblePokemon.size()));
               int level = 10;
               if (rodName.equals("good rod")) {
                  level = 20;
               } else if (rodName.equals("super rod")) {
                  level = 30;
               }

               Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
               String evolveTo = null;
               boolean failed = false;

               while (!failed) {
                  failed = true;
                  Map<String, String> evos = Specie.gen2Evos.get(pokemon.specie.name);

                  for (String evo : evos.keySet()) {
                     try {
                        int evoLevel = Integer.valueOf(evo);
                        if (evoLevel <= pokemon.level && Game.rand.nextInt(256) >= 128) {
                           evolveTo = evos.get(evo);
                           pokemon.evolveTo(evolveTo);
                           failed = false;
                           break;
                        }
                     } catch (NumberFormatException e) {
                        if (Game.rand.nextInt(256) >= 192) {
                           evolveTo = evos.get(evo);
                           pokemon.evolveTo(evolveTo);
                           failed = false;
                           break;
                        }
                     }
                  }
               }

               if (evolveTo != null) {
                  pokemon.getCurrentAttacks();
               }

               String[] huhs = new String[]{"Whoa!             ", "Oh!               ", "Huh?              ", "What!?            "};
               String huh = huhs[Game.rand.nextInt(huhs.length)];
               game.player.setCurrPokemon();
               game.battle.oppPokemon = pokemon;
               nextAction.append(
                  new WaitFrames(
                     game,
                     120,
                     game.player.new CaughtFishAnim(
                        game.player.new Emote(
                           "!",
                           new DisplayText(
                              game,
                              huh + "A bite!",
                              null,
                              null,
                              new WaitFrames(
                                 game,
                                 30,
                                 new SplitAction(
                                    new SetField(game.player, "isFishing", false, null),
                                    new SetField(
                                       game.player,
                                       "currSprite",
                                       game.player.standingSprites.get(dirFacing),
                                       new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game, true))
                                    )
                                 )
                              )
                           )
                        )
                     )
                  )
               );
            }

            game.insertAction(nextAction);
         } else if (this.isWater) {
            Pokemon surfMon = null;

            for (Pokemon pokemon : game.player.pokemon) {
               if (pokemon.hms.contains("SURF")) {
                  surfMon = pokemon;
                  break;
               }
            }

            Pokemon finalSurfMon = surfMon;
            Vector2 pos = game.player.facingPos();
            Pokemon facingPokemon = game.map.pokemon.get(pos);
            boolean pokemonInTheWay = facingPokemon != null && facingPokemon.mapTiles == game.map.tiles;
            if (surfMon != null && !game.player.currFieldMove.equals("SURF") && !pokemonInTheWay) {
               Action yesAction = new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game, 3, new PlayMusic(surfMon, new DisplayText(game, surfMon.nickname.toUpperCase(Locale.ROOT) + " is using SURF!", null, null, null))
                  )
               );
               if (game.player.hmPokemon != null) {
                  if (!game.player.currFieldMove.equals("")) {
                     yesAction.append(new RunCode(() -> {
                        game.player.swapSprites(game.player.hmPokemon);
                        game.player.currPlanting = null;
                     }, null));
                  }

                  yesAction.append(new RunCode(() -> game.player.hmPokemon.removeDrawActions(game), null));
               }

               yesAction.append(new RunCode(() -> {
                  game.player.swapSpritesSurfing(finalSurfMon);
                  game.player.currFieldMove = "SURF";
               }, new PlayerMoving(game, game.player, false, new RunCode(() -> game.player.acceptInput = true, null))));
               game.player.acceptInput = false;
               game.insertAction(
                  new DisplayText(
                     game,
                     "Want to SURF?",
                     null,
                     true,
                     false,
                     new DrawYesNoMenu(
                        null, yesAction, new DisplayText.Clear(game, new WaitFrames(game, 10, new SetField(game.player, "acceptInput", true, null)))
                     )
                  )
               );
            }
         } else if (this.nameUpper.contains("berrytree_")) {
            if (this.routeBelongsTo != null && !this.routeBelongsTo.storedPokemon.isEmpty()) {
               Pokemon pokemon = this.routeBelongsTo.storedPokemon.remove(0);
               game.playerCanMove = false;
               game.battle.oppPokemon = pokemon;
               game.battle.oppPokemon.mapTiles = game.map.overworldTiles;
               game.battle.oppPokemon.position = this.position.cpy();
               game.battle.oppPokemon.canMove = false;
               game.player.setCurrPokemon();
               game.insertAction(
                  new SplitAction(
                     new PlayMusic("ledge2", null),
                     game.player.new Emote(
                        "!",
                        new DisplayText(
                           game,
                           "The tree became agitated and attacked!",
                           null,
                           null,
                           new SplitAction(
                              new WaitFrames(
                                 game,
                                 400,
                                 new SetField(
                                    this,
                                    "nameUpper",
                                    "",
                                    new SetField(this, "overSprite", null, new CallMethod(this, "init", new Object[0], game.battle.oppPokemon.new Standing()))
                                 )
                              ),
                              new WaitFrames(game, 10, new SetField(game.musicController, "startBattle", "wild", Battle.getIntroAction(game)))
                           )
                        )
                     )
                  )
               );
               return;
            }

            if (this.nameUpper.contains("_full")) {
               game.playerCanMove = false;
               String text = "Look! A";
               String berryType = this.nameUpper.split("_")[1];
               String an = "a";
               if (berryType.equals("aspear")) {
                  an = an + "n";
               }

               text = text + berryType.toUpperCase(Locale.ROOT) + " berry!";
               berryType = berryType + " berry";
               Action nextAction = new DisplayText(
                  game,
                  game.player.name.toUpperCase(Locale.ROOT) + " got " + an + " " + berryType.toUpperCase(Locale.ROOT) + "!",
                  "Berry_Get.ogg",
                  false,
                  true,
                  new WaitFrames(game, 10, new SetField(game, "playerCanMove", true, null))
               );
               int amount = 1;
               System.out.println(berryType);
               if (game.player.hasItem(berryType)) {
                  amount = game.player.getItemAmount(berryType) + 1;
               }

               game.player.setItemAmount(berryType, amount);
               game.insertAction(nextAction);
               this.nameUpper = this.nameUpper.replace("_full", "_empty");
               this.init();
               return;
            }
         } else {
            if (this.nameUpper.equals("chest1")) {
               if (this.routeBelongsTo == null) {
                  this.routeBelongsTo = new Route("", 2);
               }

               game.playerCanMove = false;
               game.insertAction(
                  new WaitFrames(game, 10, new CallMethod(this.overSprite, "setRegion", new Object[]{16, 0, 16, 32}, new PlayMusic("enter1", null)))
               );
               game.insertAction(new WaitFrames(game, 16, new DrawPokemonMenu.Intro(new DrawPokemonMenu(game, true, this, null))));
               return;
            }

            if (this.nameUpper.contains("campfire") && game.playerCanMove) {
               game.playerCanMove = false;
               game.player.isCrafting = true;
               game.insertAction(
                  new DrawCraftsMenu.Intro(
                     null, 9, new DrawCraftsMenu(game, new SetField(game, "playerCanMove", true, new SetField(game.player, "isCrafting", false, null)))
                  )
               );
               return;
            }
         }
      }
   }

   public void flipDoorTile() {
      String[] tokens = this.name.split("__");
      if (tokens.length < 2) {
         System.out.println("somethings wrong.");
      } else {
         String name = tokens[0];
         String onOff = tokens[1];
         if (onOff.equals("on")) {
            name = name + "__off";
            this.isSolid = false;
         } else {
            name = name + "__on";
            this.isSolid = true;
         }

         this.name = name;
         this.sprite = TextureCache.getTileSprite("buildings/" + name);
         this.sprite.setPosition(this.position.x, this.position.y);
      }
   }

   public void onWalkOver() {
   }

   public boolean leavesFootprint() {
      return this.name.equals("desert4") || this.name.equals("snow2") || this.name.equals("snow5") || this.name.equals("snow6");
   }

   public static class FlipDoorTile extends Action {
      ArrayList<Tile> flipDoors;

      @Override
      public void step(Game game) {
         for (Tile tile : this.flipDoors) {
            tile.flipDoorTile();
         }

         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      public FlipDoorTile(ArrayList<Tile> flipDoors, Action nextAction) {
         super();
         this.flipDoors = flipDoors;
         this.nextAction = nextAction;
      }
   }

   public class Moving extends Action {
      int timer = 0;
      Tile target;
      Vector2 currPos;
      Vector2 offset = new Vector2();
      SmolSprite overSprite;

      public Moving(Tile target, Vector2 position, Action nextAction) {
         super();
         this.target = target;
         this.offset = position.cpy().sub(Tile.this.position);
         this.offset.x /= 16.0F;
         this.offset.y /= 16.0F;
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         this.overSprite = Tile.this.overSprite;
         Tile.this.drawUpperBelowPlayer = false;
         Tile.this.overSprite = null;
         this.currPos = Tile.this.position.cpy();
      }

      @Override
      public void step(Game game) {
         this.currPos.add(this.offset);
         game.mapBatch.draw(this.overSprite, this.currPos.x, this.currPos.y);
         this.timer++;
         if (this.timer >= 16) {
            this.target.nameUpper = Tile.this.nameUpper;
            this.target.init();
            Tile.this.nameUpper = "";
            Tile.this.init();
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }

   public class PutTile extends Action {
      Tile tile;
      Vector2 pos;

      public PutTile(Tile tile, Vector2 pos, Action nextAction) {
         super();
         this.tile = tile;
         this.pos = pos;
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         game.map.overworldTiles.put(this.pos.cpy(), this.tile);
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   class SetSignText extends Action {
      Texture bgTexture;
      Color prevColor = new Color();
      int avatarAnimCounter = 0;
      ArrayList<Character> text = new ArrayList<>();
      int backspaceTimer = 0;
      public boolean done = false;
      public boolean disabled = false;

      public SetSignText(String existingText, Action nextAction) {
         super();
         this.bgTexture = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
         if (existingText != null) {
            for (char c : existingText.toCharArray()) {
               this.text.add(c);
            }
         }

         this.nextAction = nextAction;
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.gui_106;
      }

      @Override
      public void firstStep(Game game) {
         game.insertAction(new DisplayText(game, "Press Enter to set text", null, true, false, null));
         Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
               TextField.updateField(character, SetSignText.this.text, 100, true);
               return false;
            }
         });
      }

      @Override
      public void step(Game game) {
         this.prevColor.set(game.uiBatch.getColor());
         game.uiBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         game.uiBatch.draw(this.bgTexture, -8.0F, -8.0F);
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

            Sprite letterSprite = game.textDict.get(character);
            letterSprite.setPosition(8 * i, 128 - 16 * j);
            letterSprite.draw(game.uiBatch);
            if (++i > 20) {
               i = 0;
               j++;
            }
         }

         if (this.avatarAnimCounter >= 12) {
            Sprite letterSprite = game.textDict.get('_');
            game.uiBatch.draw(letterSprite, 8 * i, 128 - 16 * j);
         }

         game.uiBatch.setColor(this.prevColor);
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
               Gdx.input.setInputProcessor(null);
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
                              new DisplayText.Clear(
                                 game,
                                 new WaitFrames(
                                    game,
                                    3,
                                    new SetField(
                                       Tile.this,
                                       "hasItem",
                                       allText,
                                       new PlayMusic(
                                          "coin1",
                                          0.6F,
                                          new WaitFrames(game, 10, new SetField(this, "done", true, new SetField(game, "playerCanMove", true, null)))
                                       )
                                    )
                                 )
                              ),
                              new DisplayText.Clear(
                                 game, new WaitFrames(game, 10, new SetField(this, "done", true, new SetField(game, "playerCanMove", true, null)))
                              )
                           )
                        )
                     )
                  )
               );
            }

            this.avatarAnimCounter--;
            if (this.avatarAnimCounter <= 0) {
               this.avatarAnimCounter = 24;
            }
         }
      }
   }
}
