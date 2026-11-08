package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.pkmngen.game.util.Save;
import com.pkmngen.game.util.SmolSpriteProxy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.zeroturnaround.zip.ZipUtil;

public class PkmnMap {
   public Rectangle viewBounds = new Rectangle();
   public Rectangle cacheBounds = new Rectangle();
   public boolean refreshCache = true;
   public boolean refreshOnscreenPokemon = false;
   ArrayList<Tile> onscreenTiles = new ArrayList<>();
   ArrayList<Tile> onscreenSpriteProxies = new ArrayList<>();
   ArrayList<Tile> onscreenSprites = new ArrayList<>();
   ArrayList<Tile> onscreenShoreTidal = new ArrayList<>();
   ArrayList<Tile> onscreenShoreOcean = new ArrayList<>();
   ArrayList<Tile> onscreenOverSprites = new ArrayList<>();
   ArrayList<Tile> onscreenSavannaTrees = new ArrayList<>();
   ArrayList<Tile> onscreenBelowBridgeTiles = new ArrayList<>();
   ArrayList<Tile> onscreenDrawAsTree = new ArrayList<>();
   ArrayList<Tile> onscreenGrass = new ArrayList<>();
   ArrayList<Tile> onscreenExteriorTidal = new ArrayList<>();
   ArrayList<Tile> onscreenFossils = new ArrayList<>();
   ArrayList<Pokemon> onscreenPokemon = new ArrayList<>();
   ArrayList<Pokemon> onscreenEggs = new ArrayList<>();
   ArrayList<Pokemon> onscreenBurrowed = new ArrayList<>();
   PkmnMap.UpkeepTimers upkeepTimers = new PkmnMap.UpkeepTimers();
   public Map<Vector2, Tile> overworldTiles = new HashMap<>();
   public Map<Vector2, Tile> tiles = this.overworldTiles;
   public ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<>();
   public int interiorTilesIndex = 100;
   Map<Vector2, Pokemon> pokemon = new HashMap<>();
   Map<Vector2, Pokemon> burrowedPokemon = new HashMap<>();
   Vector2 bottomLeft = null;
   Vector2 topRight = null;
   ArrayList<Vector2> edges = new ArrayList<>();
   public Route currRoute;
   String currBiome = "";
   Random rand;
   String timeOfDay = "day";
   String id;
   public static Color nightColor;
   ArrayList<String> unownUsed = new ArrayList<>();
   private Vector2 tilePos = new Vector2();
   public Pixmap minimap;
   public ArrayList<Tile> minimapQueue;
   public PkmnMap.ShadeEffect shadeEffect;
   public String currMapId;
   Rectangle boundingBox;
   private static final Vector2[] POSITIONS;
   ArrayList<PkmnMap.YSort> onscreenYsort1;

   public Rectangle boundingBox() {
      if (this.boundingBox == null) {
         this.boundingBox = new Rectangle(
            this.bottomLeft.x + 16.0F, this.bottomLeft.y + 16.0F, this.topRight.x - this.bottomLeft.x - 32.0F, this.topRight.y - this.bottomLeft.y - 32.0F
         );
      }

      return this.boundingBox;
   }

   public PkmnMap(String mapName) {
      char[] textArray = "abcdefghijklmnopqrstuvwxyz".toCharArray();

      for (int i = 0; i < textArray.length; i++) {
         this.unownUsed.add(String.valueOf(textArray[i]));
      }

      this.unownUsed.add("!");
      this.unownUsed.add("qmark");
      this.minimapQueue = new ArrayList<>();
      this.currMapId = "0,0";
      this.boundingBox = null;
      this.onscreenYsort1 = new ArrayList<>();
      this.id = mapName;
      this.rand = new Random();

      for (int i = 0; i < 100; i++) {
         this.interiorTiles.add(null);
      }

      this.interiorTiles.add(new HashMap<>());
      this.interiorTiles.add(new HashMap<>());
      this.currRoute = new Route("Demo_SteelRoute", 20);
   }

   public HashMap<Vector2, Tile> getInteriorLayer(int interiorIndex) {
      while (interiorIndex >= this.interiorTiles.size()) {
         this.interiorTiles.add(new HashMap<>());
      }

      HashMap<Vector2, Tile> interiorTiles = this.interiorTiles.get(interiorIndex);
      if (interiorTiles == null) {
         this.interiorTiles.remove(interiorIndex);
         interiorTiles = new HashMap<>();
         this.interiorTiles.add(interiorIndex, interiorTiles);
      }

      return interiorTiles;
   }

   public int getBuildingHeight(Tile startTile) {
      int numberOfWalls = 0;
      Tile currTile = startTile;
      boolean shouldStop = false;
      boolean foundRoof = false;

      while (!shouldStop) {
         if (currTile.isWall()) {
            numberOfWalls++;
         }

         if (currTile.nameUpper.contains("roof")) {
            foundRoof = true;
         }

         currTile = this.overworldTiles.get(currTile.position.cpy().add(0.0F, 16.0F));
         shouldStop = !currTile.nameUpper.contains("house");
         if (foundRoof) {
            shouldStop |= currTile.isWall();
         }
      }

      if (!foundRoof) {
         return -1;
      }

      currTile = this.overworldTiles.get(startTile.position.cpy().add(0.0F, -16.0F));
      shouldStop = !currTile.nameUpper.contains("house");
      boolean foundWall = currTile.isWall();
      if (foundWall) {
         shouldStop |= currTile.nameUpper.contains("roof");
      }

      while (!shouldStop) {
         if (currTile.isWall()) {
            numberOfWalls++;
         }

         currTile = this.overworldTiles.get(currTile.position.cpy().add(0.0F, -16.0F));
         shouldStop = !currTile.nameUpper.contains("house");
         if (foundWall) {
            shouldStop |= currTile.nameUpper.contains("roof");
         }

         if (!currTile.nameUpper.contains("roof")) {
            foundWall = true;
         }
      }

      return numberOfWalls;
   }

   public void adjustSurroundingTiles(Tile currTile) {
      this.adjustSurroundingTiles(currTile, this.tiles);
   }

   public void adjustSurroundingTiles(Tile currTile, Map<Vector2, Tile> currTiles) {
      this.adjustSurroundingTiles(currTile, currTiles, currTiles == this.overworldTiles);
   }

   public void adjustSurroundingTiles(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
      Vector2 pos = new Vector2();

      for (int i = -16; i < 17; i += 16) {
         for (int j = -16; j < 17; j += 16) {
            pos.set(currTile.position.cpy().add(i, j));
            Tile tile = currTiles.get(pos);
            if (tile != null) {
               this.adjustTile(tile, currTiles, isOverworld);
               this.coastify(tile, currTiles, isOverworld);
               this.waterfallify(tile, currTiles, isOverworld);
               this.adjustPuddles(tile, currTiles, isOverworld);
            }
         }
      }
   }

   public void adjustTile(Tile currTile, Map<Vector2, Tile> currTiles) {
      this.adjustTile(currTile, currTiles, currTiles == this.overworldTiles);
   }

   public void waterfallify(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
      if ((currTile.name.contains("water") || currTile.name.contains("lava")) && currTile.nameUpper.contains("ledges")) {
         String name = "waterfall1";
         if (currTile.name.contains("lava")) {
            name = "lavafall";
         }

         String ext = "_";
         String[] tokens = currTile.nameUpper.split("_", 2);
         if (tokens.length < 2) {
            return;
         }

         ext = ext + tokens[1];
         currTile.name = name + ext;
         currTile.init();
      }
   }

   public void adjustPuddles(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
      if (currTile.name.contains("_puddle1")) {
         String direction = "";
         String[] directions = new String[]{"W", "E", "S", "N", "SW", "NE", "SE", "NW"};
         Vector2[] positions = new Vector2[]{
            new Vector2(-16.0F, 0.0F),
            new Vector2(16.0F, 0.0F),
            new Vector2(0.0F, -16.0F),
            new Vector2(0.0F, 16.0F),
            new Vector2(-16.0F, -16.0F),
            new Vector2(16.0F, 16.0F),
            new Vector2(16.0F, -16.0F),
            new Vector2(-16.0F, 16.0F)
         };

         for (int i = 0; i < positions.length && i < 4; i++) {
            Tile nextTile = currTiles.get(currTile.position.cpy().add(positions[i]));
            if (nextTile != null && nextTile.name.contains("_puddle1")) {
               direction = direction + "[" + directions[i] + "]";
            }
         }

         String name = currTile.name.split("_puddle1")[0];
         currTile.name = name + "_puddle1" + direction;
         currTile.init();
      }
   }

   public boolean coastify(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
      currTile.shoreOcean = null;
      currTile.shoreTidal = null;
      if (!currTile.isWater
         && !currTile.nameUpper.contains("ledges")
         && !currTile.name.equals("ice2")
         && !currTile.name.contains("bridge")
         && !currTile.name.contains("waterfall")) {
         String direction = "";
         String directionTidal = "";
         String[] directions = new String[]{"W", "E", "S", "N", "SW", "NE", "SE", "NW"};
         boolean found = false;

         for (int i = 0; i < POSITIONS.length; i++) {
            this.tilePos.set(currTile.position).add(POSITIONS[i]);
            Tile nextTile = currTiles.get(this.tilePos);
            if (nextTile != null && !nextTile.name.equals("water6")) {
               if (!currTile.isTidal && nextTile.isTidal) {
                  found = false;

                  for (int k = 0; directions[i].length() >= 2 && k < directions[i].length(); k++) {
                     if (directionTidal.contains("[" + directions[i].substring(k, k + 1) + "]")) {
                        found = true;
                        break;
                     }
                  }

                  if (!found) {
                     directionTidal = directionTidal + "[" + directions[i] + "]";
                  }
               } else if (nextTile.isWater) {
                  found = false;

                  for (int k = 0; directions[i].length() >= 2 && k < directions[i].length(); k++) {
                     if (direction.contains("[" + directions[i].substring(k, k + 1) + "]")) {
                        found = true;
                        break;
                     }
                  }

                  if (!found) {
                     direction = direction + "[" + directions[i] + "]";
                     directionTidal = directionTidal + "[" + directions[i] + "]";
                  }
               }
            }
         }

         boolean coastified = false;
         if (!direction.equals("")) {
            currTile.shoreOcean = direction;
            coastified = true;
         }

         if (!directionTidal.equals("")) {
            currTile.shoreTidal = directionTidal;
            coastified = true;
         }

         currTile.init();
         return coastified;
      } else {
         return false;
      }
   }

   public void ledgify(Tile tile) {
      String nullType = "black1";
      Tile bl = this.tiles.get(tile.position.cpy().add(-16.0F, -16.0F));
      Tile down = this.tiles.get(tile.position.cpy().add(0.0F, -16.0F));
      Tile br = this.tiles.get(tile.position.cpy().add(16.0F, -16.0F));
      Tile left = this.tiles.get(tile.position.cpy().add(-16.0F, 0.0F));
      Tile tl = this.tiles.get(tile.position.cpy().add(-16.0F, 16.0F));
      Tile up = this.tiles.get(tile.position.cpy().add(0.0F, 16.0F));
      Tile tr = this.tiles.get(tile.position.cpy().add(16.0F, 16.0F));
      Tile right = this.tiles.get(tile.position.cpy().add(16.0F, 0.0F));
      if (bl == null) {
         bl = new Tile(nullType, tile.position.cpy());
      }

      if (down == null) {
         down = new Tile(nullType, tile.position.cpy());
      }

      if (br == null) {
         br = new Tile(nullType, tile.position.cpy());
      }

      if (left == null) {
         left = new Tile(nullType, tile.position.cpy());
      }

      if (tl == null) {
         tl = new Tile(nullType, tile.position.cpy());
      }

      if (up == null) {
         up = new Tile(nullType, tile.position.cpy());
      }

      if (tr == null) {
         tr = new Tile(nullType, tile.position.cpy());
      }

      if (right == null) {
         right = new Tile(nullType, tile.position.cpy());
      }

      String ledgeName = "ledges3";
      if (tile.nameUpper.contains("snow")) {
         ledgeName = "ledges3snow";
      } else if (tile.nameUpper.contains("volcano")) {
         ledgeName = "ledges3volcano";
      }

      if (left.nameUpper.contains("ledges3") && !down.nameUpper.contains("ledges3") && right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_N";
         tile.init();
      } else if (up.nameUpper.contains("ledges3")
         && right.nameUpper.contains("ledges3")
         && !down.nameUpper.contains("ledges3")
         && !left.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_NE";
         tile.init();
      } else if (up.nameUpper.contains("ledges3") && !left.nameUpper.contains("ledges3") && down.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_E";
         tile.init();
      } else if (!up.nameUpper.contains("ledges3")
         && !left.nameUpper.contains("ledges3")
         && right.nameUpper.contains("ledges3")
         && down.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_SE";
         tile.init();
      } else if (!up.nameUpper.contains("ledges3") && left.nameUpper.contains("ledges3") && right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_S";
         tile.init();
      } else if (!up.nameUpper.contains("ledges3")
         && down.nameUpper.contains("ledges3")
         && left.nameUpper.contains("ledges3")
         && !right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_SW";
         tile.init();
      } else if (up.nameUpper.contains("ledges3") && down.nameUpper.contains("ledges3") && !right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_W";
         tile.init();
      } else if (up.nameUpper.contains("ledges3")
         && left.nameUpper.contains("ledges3")
         && !down.nameUpper.contains("ledges3")
         && !right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_NW";
         tile.init();
      } else if (!br.nameUpper.contains("ledges3") && down.nameUpper.contains("ledges3") && right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_NW_inner";
         tile.init();
      } else if (!bl.nameUpper.contains("ledges3") && down.nameUpper.contains("ledges3") && left.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_NE_inner";
         tile.init();
      } else if (!tr.nameUpper.contains("ledges3") && up.nameUpper.contains("ledges3") && right.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_SW_inner";
         tile.init();
      } else if (!tl.nameUpper.contains("ledges3") && up.nameUpper.contains("ledges3") && left.nameUpper.contains("ledges3")) {
         tile.nameUpper = ledgeName + "_SE_inner";
         tile.init();
      }
   }

   public void adjustTile(Tile currTile, Map<Vector2, Tile> currTiles, boolean isOverworld) {
      if (currTile.name.contains("lava") && currTile.nameUpper.equals("")) {
         String name = "lava1";
         String ext = "_";
         String[] cols = new String[]{"S", "", "N"};
         String[] rows = new String[]{"W", "", "E"};
         if (Game.rand.nextInt(3) != 0) {
            if (Game.rand.nextBoolean()) {
               ext = ext + cols[Game.rand.nextInt(3)];
               ext = ext + rows[Math.floorMod((int)currTile.position.x, 48) / 16];
            } else {
               ext = ext + cols[Math.floorMod((int)currTile.position.y, 48) / 16];
               ext = ext + rows[Game.rand.nextInt(3)];
            }
         }

         currTile.name = name + ext;
         currTile.init();
      } else {
         Tile up = currTiles.get(currTile.position.cpy().add(0.0F, 16.0F));
         Tile down = currTiles.get(currTile.position.cpy().add(0.0F, -16.0F));
         Tile right = currTiles.get(currTile.position.cpy().add(16.0F, 0.0F));
         Tile left = currTiles.get(currTile.position.cpy().add(-16.0F, 0.0F));
         String[] tokens = currTile.nameUpper.split("_");
         String name = tokens[0];

         for (int i = 1; i < tokens.length - 1; i++) {
            name = name + "_" + tokens[i];
         }

         boolean touchUp = up != null && up.nameUpper.contains(name);
         boolean touchDown = down != null && down.nameUpper.contains(name);
         boolean touchRight = right != null && right.nameUpper.contains(name);
         boolean touchLeft = left != null && left.nameUpper.contains(name);
         String ext = "_";
         HashMap<Vector2, Tile> interiorTiles = this.interiorTiles.get(this.interiorTilesIndex);
         if (!isOverworld) {
            if (currTile.nameUpper.contains("table")) {
               if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                  return;
               }

               if (touchUp) {
                  ext = ext + "N";
               }

               if (touchDown) {
                  ext = ext + "S";
               }

               if (touchRight) {
                  ext = ext + "E";
               }

               if (touchLeft) {
                  ext = ext + "W";
               }

               if (ext.equals("_E")) {
                  ext = "_left1";
               } else if (ext.equals("_W")) {
                  ext = "_right1";
               } else if (ext.equals("_EW")) {
                  ext = "_middle1";
               } else if (ext.equals("_NS")) {
                  ext = "_middle2";
               } else if (ext.equals("_N")) {
                  ext = "_down1";
               } else if (ext.equals("_S")) {
                  ext = "_up1";
               }

               currTile.nameUpper = name + ext;
               currTile.init();
            } else if (currTile.nameUpper.contains("interiorwall")) {
               if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                  return;
               }

               if (touchUp || up.name.contains("wall")) {
                  ext = ext + "N";
               }

               if (touchDown) {
                  ext = ext + "S";
               }

               if (touchRight || right == null) {
                  ext = ext + "E";
               }

               if (touchLeft || left == null) {
                  ext = ext + "W";
               }

               currTile.nameUpper = name + ext;
               currTile.init();
            } else if (currTile.name.contains("ruins1")) {
               if (currTile.name.contains("pillar")) {
                  return;
               }

               tokens = currTile.name.split("_");
               name = tokens[0];

               for (int i = 1; i < tokens.length - 1; i++) {
                  name = name + "_" + tokens[i];
               }

               touchUp = up != null && up.name.contains(name);
               touchDown = down != null && down.name.contains(name);
               touchRight = right != null && right.name.contains(name);
               touchLeft = left != null && left.name.contains(name);
               if (touchUp) {
                  ext = ext + "N";
               }

               if (touchDown) {
                  ext = ext + "S";
               }

               if (touchRight) {
                  ext = ext + "E";
               }

               if (touchLeft) {
                  ext = ext + "W";
               }

               currTile.name = name + ext;
               currTile.init();
            }
         } else {
            if (currTile.nameUpper.contains("ledges3")) {
               if (up != null && up.nameUpper.equals("ledges3_E") && right != null && right.nameUpper.equals("ledges3_N")) {
                  currTile.nameUpper = "ledges3_NE";
                  currTile.init();
               } else if (up != null && up.nameUpper.equals("ledges3_W") && left != null && left.nameUpper.equals("ledges3_N")) {
                  currTile.nameUpper = "ledges3_N";
                  currTile.init();
               } else if (down != null && down.nameUpper.equals("ledges3_W") && left != null && left.nameUpper.equals("ledges3_S")) {
                  currTile.nameUpper = "ledges3_SW";
                  currTile.init();
               } else if (down != null && down.nameUpper.equals("ledges3_E") && right != null && right.nameUpper.equals("ledges3_S")) {
                  currTile.nameUpper = "ledges3_SE";
                  currTile.init();
               }
            }

            if (currTile.nameUpper.contains("picture")) {
               return;
            }

            if (currTile.nameUpper.contains("house_plant")) {
               return;
            }

            if (currTile.nameUpper.contains("house_gym")) {
               return;
            }

            if (currTile.nameUpper.contains("hole")) {
               touchUp = touchUp || up != null && (up.name.contains("hole") || up.nameUpper.contains("hole"));
               touchDown = touchDown || down != null && (down.name.contains("hole") || down.nameUpper.contains("hole"));
               touchRight = touchRight || right != null && (right.name.contains("hole") || right.nameUpper.contains("hole"));
               touchLeft = touchLeft || left != null && (left.name.contains("hole") || left.nameUpper.contains("hole"));
               touchUp = touchUp || up != null && (up.name.contains("water") || up.nameUpper.contains("water"));
               touchDown = touchDown || down != null && (down.name.contains("water") || down.nameUpper.contains("water"));
               touchRight = touchRight || right != null && (right.name.contains("water") || right.nameUpper.contains("water"));
               touchLeft = touchLeft || left != null && (left.name.contains("water") || left.nameUpper.contains("water"));
               touchUp = touchUp || up != null && (up.name.contains("lava") || up.nameUpper.contains("lava"));
               touchDown = touchDown || down != null && (down.name.contains("lava") || down.nameUpper.contains("lava"));
               touchRight = touchRight || right != null && (right.name.contains("lava") || right.nameUpper.contains("lava"));
               touchLeft = touchLeft || left != null && (left.name.contains("lava") || left.nameUpper.contains("lava"));
               Tile NE = currTiles.get(currTile.position.cpy().add(16.0F, 16.0F));
               Tile SE = currTiles.get(currTile.position.cpy().add(16.0F, -16.0F));
               Tile SW = currTiles.get(currTile.position.cpy().add(-16.0F, -16.0F));
               Tile NW = currTiles.get(currTile.position.cpy().add(-16.0F, 16.0F));
               boolean touchNE = NE != null && (NE.name.contains("water") || NE.nameUpper.contains("water") || NE.nameUpper.contains("hole"));
               boolean touchSE = SE != null && (SE.name.contains("water") || SE.nameUpper.contains("water") || SE.nameUpper.contains("hole"));
               boolean touchSW = SW != null && (SW.name.contains("water") || SW.nameUpper.contains("water") || SW.nameUpper.contains("hole"));
               boolean touchNW = NW != null && (NW.name.contains("water") || NW.nameUpper.contains("water") || NW.nameUpper.contains("hole"));
               if (touchUp) {
                  ext = ext + "N";
               }

               if (touchDown) {
                  ext = ext + "S";
               }

               if (touchRight) {
                  ext = ext + "E";
               }

               if (touchLeft) {
                  ext = ext + "W";
               }

               if (ext.equals("_")) {
                  ext = "";
               } else {
                  if (touchNE && touchUp && touchRight) {
                     ext = ext + "[NE]";
                  }

                  if (touchSE && touchDown && touchRight) {
                     ext = ext + "[SE]";
                  }

                  if (touchSW && touchDown && touchLeft) {
                     ext = ext + "[SW]";
                  }

                  if (touchNW && touchUp && touchLeft) {
                     ext = ext + "[NW]";
                  }
               }

               currTile.nameUpper = name + ext;
               currTile.init();
            } else if (currTile.nameUpper.contains("fence") && !currTile.nameUpper.contains("house") && !currTile.nameUpper.contains("gate")) {
               if (up == null || down == null || right == null || left == null) {
                  return;
               }

               if (down.nameUpper.contains("house")) {
                  touchDown = true;
               }

               if (left.nameUpper.contains("house") && !left.nameUpper.contains("roof")) {
                  touchLeft = true;
               }

               if (right.nameUpper.contains("house") && !right.nameUpper.contains("roof")) {
                  touchRight = true;
               }

               if (!currTile.nameUpper.contains("fence2") && up.nameUpper.contains("house")) {
                  touchUp = true;
               }

               if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                  return;
               }

               if (touchUp) {
                  ext = ext + "N";
               }

               if (touchDown) {
                  ext = ext + "S";
               }

               if (touchRight) {
                  ext = ext + "E";
               }

               if (touchLeft) {
                  ext = ext + "W";
               }

               if (touchLeft && touchRight && !touchUp && !touchDown) {
                  ext = "";
               }

               if ((touchUp || touchDown) && !touchLeft && !touchRight) {
                  ext = "_NS";
               }

               if (ext.length() == 2) {
                  ext = "";
               }

               currTile.nameUpper = name + ext;
               currTile.init();
            } else if (currTile.nameUpper.contains("house") && !currTile.nameUpper.contains("roof") && !currTile.nameUpper.contains("door")) {
               String window = "";
               if (currTile.nameUpper.contains("exteriorwindows")) {
                  window = "exteriorwindows";
               }

               if (currTile.nameUpper.equals("house5_middle1") && down.nameUpper.contains("fence1") && !(touchUp ^ touchDown)) {
                  currTile.nameUpper = "house5_middle1_fence1" + window;
                  currTile.init();
                  if (!interiorTiles.containsKey(currTile.position)) {
                     Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                     interiorTiles.put(currTile.position.cpy(), interiorTile);
                  }

                  return;
               }

               if (currTile.nameUpper.contains("house6")) {
                  touchUp = up != null && up.nameUpper.contains(name) && !up.nameUpper.contains("roof");
                  touchDown = down != null && down.nameUpper.contains(name) && !down.nameUpper.contains("roof");
                  touchRight = right != null && right.nameUpper.contains(name) && !right.nameUpper.contains("roof");
                  touchLeft = left != null && left.nameUpper.contains(name) && !left.nameUpper.contains("roof");
                  if (touchUp) {
                     ext = ext + "N";
                  }

                  if (touchDown) {
                     ext = ext + "S";
                  }

                  if (touchRight) {
                     ext = ext + "E";
                  }

                  if (touchLeft) {
                     ext = ext + "W";
                  }

                  if (touchLeft && touchRight && !touchUp && !touchDown) {
                     ext = "_NEW";
                  }

                  if (ext.length() <= 2) {
                     ext = ext + "N";
                  }

                  currTile.nameUpper = name + ext + window;
                  currTile.init();
                  if (!interiorTiles.containsKey(currTile.position)) {
                     Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                     interiorTiles.put(currTile.position.cpy(), interiorTile);
                  }

                  return;
               }

               if (currTile.nameUpper.contains("house7")) {
                  if (touchUp) {
                     ext = ext + "N";
                  }

                  if (touchDown) {
                     ext = ext + "S";
                  }

                  if (touchRight) {
                     ext = ext + "E";
                  }

                  if (touchLeft) {
                     ext = ext + "W";
                  }

                  if (ext.length() < 2) {
                     ext = ext + "S";
                  }

                  currTile.nameUpper = name + ext + window;
                  currTile.init();
                  if (!interiorTiles.containsKey(currTile.position)) {
                     Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                     interiorTiles.put(currTile.position.cpy(), interiorTile);
                  }

                  return;
               }

               if (currTile.nameUpper.contains("house8")) {
                  touchUp = up != null && up.nameUpper.contains(name) && !up.nameUpper.contains("roof");
                  touchDown = down != null && down.nameUpper.contains(name) && !down.nameUpper.contains("roof");
                  touchRight = right != null && right.nameUpper.contains(name) && !right.nameUpper.contains("roof");
                  touchLeft = left != null && left.nameUpper.contains(name) && !left.nameUpper.contains("roof");
                  if (touchUp) {
                     ext = ext + "N";
                  }

                  if (touchDown) {
                     ext = ext + "S";
                  }

                  if (touchRight) {
                     ext = ext + "E";
                  }

                  if (touchLeft) {
                     ext = ext + "W";
                  }

                  if (touchLeft && touchRight && !touchUp && !touchDown) {
                     ext = "_NEW";
                  }

                  if (ext.length() <= 2) {
                     ext = ext + "N";
                  }

                  currTile.nameUpper = name + ext + window;
                  currTile.init();
                  if (!interiorTiles.containsKey(currTile.position)) {
                     Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                     interiorTiles.put(currTile.position.cpy(), interiorTile);
                  }

                  return;
               }

               if (currTile.nameUpper.contains("house9")) {
                  touchUp = up != null && up.nameUpper.contains(name) && !up.nameUpper.contains("roof");
                  touchDown = down != null && down.nameUpper.contains(name) && !down.nameUpper.contains("roof");
                  touchRight = right != null && right.nameUpper.contains(name);
                  touchLeft = left != null && left.nameUpper.contains(name);
                  if (touchUp) {
                     ext = ext + "N";
                  }

                  if (touchDown) {
                     ext = ext + "S";
                  }

                  if (touchRight) {
                     ext = ext + "E";
                  }

                  if (touchLeft) {
                     ext = ext + "W";
                  }

                  if (touchLeft && touchRight && !touchUp && !touchDown) {
                     ext = "_NEW";
                  }

                  if (ext.length() <= 2) {
                     ext = ext + "N";
                  }

                  currTile.nameUpper = name + ext + window;
                  currTile.init();
                  if (!interiorTiles.containsKey(currTile.position)) {
                     Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                     interiorTiles.put(currTile.position.cpy(), interiorTile);
                  }

                  return;
               }

               if (!touchLeft && !touchRight) {
                  return;
               }

               if (touchRight && touchLeft) {
                  ext = ext + "middle1";
               } else if (touchRight && touchDown) {
                  ext = ext + "E";
               } else if (touchLeft && touchDown) {
                  ext = ext + "W";
               } else if (touchRight) {
                  ext = ext + "left1";
               } else if (touchLeft) {
                  ext = ext + "right1";
               }

               currTile.nameUpper = name + ext + window;
               currTile.init();
               if (!interiorTiles.containsKey(currTile.position)) {
                  Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                  interiorTiles.put(currTile.position.cpy(), interiorTile);
               }
            } else if (currTile.nameUpper.contains("house") && currTile.nameUpper.contains("roof")) {
               String chimney = "";
               if (currTile.nameUpper.contains("chimney")) {
                  chimney = "chimney";
               }

               if (!touchUp && !touchDown && !touchLeft && !touchRight) {
                  return;
               }

               if (touchUp) {
                  ext = ext + "N";
               }

               if (touchDown) {
                  ext = ext + "S";
               }

               if (touchRight) {
                  ext = ext + "E";
               }

               if (touchLeft) {
                  ext = ext + "W";
               }

               if (touchLeft && touchRight && !touchUp && !touchDown) {
                  ext = "_middle1";
               }

               if ((touchUp || touchDown) && !touchLeft && !touchRight) {
                  ext = "_NSEW";
               }

               if (ext.length() == 2) {
                  ext = "";
                  if (touchLeft) {
                     ext = "_right1";
                  } else if (touchRight) {
                     ext = "_left1";
                  }
               }

               if (currTile.nameUpper.contains("house6") && ext.length() > 3) {
                  Tile NE = currTiles.get(currTile.position.cpy().add(16.0F, 16.0F));
                  Tile SE = currTiles.get(currTile.position.cpy().add(16.0F, -16.0F));
                  Tile SW = currTiles.get(currTile.position.cpy().add(-16.0F, -16.0F));
                  Tile NW = currTiles.get(currTile.position.cpy().add(-16.0F, 16.0F));
                  boolean touchNE = NE != null && NE.nameUpper.contains("roof");
                  boolean touchSE = SE != null && SE.nameUpper.contains("roof");
                  boolean touchSW = SW != null && SW.nameUpper.contains("roof");
                  boolean touchNW = NW != null && NW.nameUpper.contains("roof");
                  if (!touchSE && touchDown && touchRight) {
                     ext = "_SEinner";
                  } else if (!touchSW && touchDown && touchLeft) {
                     ext = "_SWinner";
                  } else if (!touchNE && touchUp && touchRight) {
                     ext = "_NEinner";
                  } else if (!touchNW && touchUp && touchLeft) {
                     ext = "_NWinner";
                  }
               }

               if (currTile.nameUpper.contains("house7")) {
                  ext = "_";
                  if (touchUp) {
                     ext = ext + "N";
                  }

                  if (touchDown) {
                     ext = ext + "S";
                  }

                  if (touchRight) {
                     ext = ext + "E";
                  }

                  if (touchLeft) {
                     ext = ext + "W";
                  }
               }

               currTile.nameUpper = name + ext + chimney;
               currTile.init();
               if (!interiorTiles.containsKey(currTile.position) || interiorTiles.get(currTile.position).name.contains("wall")) {
                  Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                  interiorTiles.put(currTile.position.cpy(), interiorTile);
               }

               if (!interiorTiles.containsKey(up.position) || interiorTiles.get(up.position) == null) {
                  interiorTiles.put(up.position.cpy(), new Tile("house5_wall1", up.position.cpy()));
               }
            } else if (currTile.nameUpper.contains("door")) {
               if (right != null && left != null) {
                  if ((left.nameUpper.contains("roof") || left.nameUpper.contains("door"))
                     && (right.nameUpper.contains("roof") || right.nameUpper.contains("door"))
                     && (!left.nameUpper.contains("door") || !right.nameUpper.contains("door"))) {
                     String name2 = "house5_roof_middle1";
                     if (currTile.nameUpper.contains("house6")) {
                        name2 = "house6_roof_middle1";
                     } else if (currTile.nameUpper.contains("house7")) {
                        name2 = "house7_roof_S";
                     } else if (currTile.nameUpper.contains("house8")) {
                        name2 = "house8_roof_S";
                     } else if (currTile.nameUpper.contains("house9")) {
                        name2 = "house9_roof_S";
                     }

                     currTile.nameUpper = name2;
                     currTile.init();
                     this.adjustSurroundingTiles(currTile, currTiles);
                     if (!up.isSolid) {
                        currTiles.put(up.position.cpy(), new Tile("rug2", "", up.position.cpy(), true, up.routeBelongsTo));
                        if (!interiorTiles.containsKey(up.position) || interiorTiles.get(up.position).name.contains("wall")) {
                           Tile interiorTile = new Tile("house5_door1", up.position.cpy());
                           interiorTiles.put(up.position.cpy(), interiorTile);
                        }
                     }

                     if (interiorTiles.containsKey(currTile.position) && interiorTiles.get(currTile.position).name.contains("rug")) {
                        Tile interiorTile = new Tile("house5_floor1", currTile.position.cpy());
                        interiorTiles.put(currTile.position.cpy(), interiorTile);
                     }

                     return;
                  }

                  if ((
                        left.nameUpper.contains("fence")
                           || left.nameUpper.contains("door")
                           || left.nameUpper.contains("wall")
                           || left.nameUpper.contains("right")
                           || left.nameUpper.contains("W")
                     )
                     && (
                        right.nameUpper.contains("fence")
                           || right.nameUpper.contains("door")
                           || right.nameUpper.contains("wall")
                           || right.nameUpper.contains("left")
                           || right.nameUpper.contains("E")
                     )
                     && (!left.isWall2() || !right.isWall2())) {
                     String fenceName = "fence1";
                     if (name.contains("house6") || name.contains("house8")) {
                        fenceName = "fence2";
                     }

                     if (currTile.nameUpper.contains("door")) {
                        interiorTiles.remove(currTile.position.cpy());
                     }

                     currTile.nameUpper = fenceName + "gate1";
                     currTile.init();
                     this.adjustSurroundingTiles(currTile, currTiles);
                     return;
                  }
               }

               label1366:
               if (up != null && down != null) {
                  if (up.nameUpper.contains("fence")
                     || down.nameUpper.contains("fence")
                     || !up.nameUpper.contains("roof")
                        && !up.nameUpper.contains("E")
                        && !up.nameUpper.contains("W")
                        && !up.nameUpper.contains("left")
                        && !up.nameUpper.contains("right")
                        && !up.nameUpper.contains("door")
                     || !down.nameUpper.contains("roof")
                        && !down.nameUpper.contains("E")
                        && !down.nameUpper.contains("W")
                        && !down.nameUpper.contains("left")
                        && !down.nameUpper.contains("right")
                        && !down.nameUpper.contains("door")
                     || up.nameUpper.contains("door") && down.nameUpper.contains("door")) {
                     if (up.nameUpper.contains("roof")
                        || up.nameUpper.contains("house") && up.nameUpper.contains("S")
                        || !up.nameUpper.contains("fence")
                           && !up.nameUpper.contains("gate")
                           && !up.nameUpper.contains("wall")
                           && !up.nameUpper.contains("left")
                           && !up.nameUpper.contains("right")
                           && !up.nameUpper.contains("W")
                           && !up.nameUpper.contains("E")
                           && !up.nameUpper.contains("door")
                        || !down.nameUpper.contains("fence")
                           && !down.nameUpper.contains("gate")
                           && !down.nameUpper.contains("wall")
                           && !down.nameUpper.contains("left")
                           && !down.nameUpper.contains("right")
                           && !down.nameUpper.contains("W")
                           && !down.nameUpper.contains("E")
                           && !down.nameUpper.contains("door")) {
                        break label1366;
                     }

                     String fenceName = "fence1";
                     if (name.contains("house6") || name.contains("house8")) {
                        fenceName = "fence2";
                     }

                     if (currTile.nameUpper.contains("door")) {
                        interiorTiles.remove(currTile.position.cpy());
                     }

                     currTile.nameUpper = fenceName + "gate1_NS";
                     currTile.init();
                     this.adjustSurroundingTiles(currTile, currTiles);
                     return;
                  }

                  int roofCount = 0;
                  if (right != null && right.nameUpper.contains("roof")) {
                     roofCount++;
                  }

                  if (left != null && left.nameUpper.contains("roof")) {
                     roofCount++;
                  }

                  String name2 = "house5_middle1";
                  if (roofCount > 0) {
                     name2 = "house5_roof_middle1";
                  }

                  if (name.contains("house6")) {
                     name2 = "house6_NS";
                     if (roofCount > 0) {
                        name2 = "house6_roof_NS";
                     }
                  } else if (name.contains("house7")) {
                     name2 = "house7_wall_NS";
                     if (roofCount > 0) {
                        name2 = "house7_roof_NS";
                     }
                  } else if (name.contains("house8")) {
                     name2 = "house8_NS";
                     if (roofCount > 0) {
                        name2 = "house8_roof_NS";
                     }
                  } else if (name.contains("house9")) {
                     name2 = "house9_NS";
                     if (roofCount > 0) {
                        name2 = "house9_roof_NS";
                     }
                  }

                  currTile.nameUpper = name2;
                  currTile.init();
                  this.adjustSurroundingTiles(currTile);
                  if (!left.isSolid) {
                     this.tiles.put(left.position.cpy(), new Tile("rug2_right", "", left.position.cpy(), true, left.routeBelongsTo));
                     Tile interiorTile = interiorTiles.get(currTile.position);
                     if (interiorTile == null || interiorTile.name.contains("floor")) {
                        interiorTile = new Tile("rug2_left", currTile.position.cpy());
                        interiorTiles.put(currTile.position.cpy(), interiorTile);
                     }
                  } else if (!right.isSolid) {
                     this.tiles.put(right.position.cpy(), new Tile("rug2_left", "", right.position.cpy(), true, right.routeBelongsTo));
                     Tile interiorTile = interiorTiles.get(currTile.position);
                     if (interiorTile == null || interiorTile.name.contains("floor")) {
                        interiorTile = new Tile("rug2_right", currTile.position.cpy());
                        interiorTiles.put(currTile.position.cpy(), interiorTile);
                     }
                  }

                  return;
               }

               if (!interiorTiles.containsKey(currTile.position)) {
                  Tile interiorTile = new Tile("house5_floor_rug1", currTile.position.cpy());
                  interiorTiles.put(currTile.position.cpy(), interiorTile);
               }
            }
         }
      }
   }

   public void loadFromFileLegacy(Game game) {
      try {
         InputStream inputStream = new InflaterInputStream(new FileInputStream(this.id + ".sav"));
         Input input = new Input(inputStream);
         Network.SaveData saveData = game.server.getKryo().readObject(input, Network.SaveData.class);
         input.close();
         this.tiles.clear();
         this.bottomLeft = new Vector2();
         this.topRight = new Vector2();
         HashMap<String, Route> loadedRoutes = new HashMap<>();

         for (Network.TileDataBase tileData : saveData.mapTiles.tiles) {
            if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
               loadedRoutes.put(tileData.routeBelongsTo, new Route(saveData.mapTiles.routes.get(tileData.routeBelongsTo)));
            }

            Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
            Tile newTile = Tile.get(tileData, tempRoute);
            this.tiles.put(tileData.pos.cpy(), newTile);
            if (newTile.items != null && newTile.items.containsKey("trapinch")) {
               Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
               trapinch.isTrapping = true;
               trapinch.position = newTile.position.cpy();
               trapinch.mapTiles = game.map.tiles;
               game.insertAction(trapinch.new Burrowed());
            }

            if (this.topRight.x < tileData.pos.x) {
               this.topRight.x = tileData.pos.x;
            }

            if (this.topRight.y < tileData.pos.y) {
               this.topRight.y = tileData.pos.y;
            }

            if (this.bottomLeft.x > tileData.pos.x) {
               this.bottomLeft.x = tileData.pos.x;
            }

            if (this.bottomLeft.y > tileData.pos.y) {
               this.bottomLeft.y = tileData.pos.y;
            }
         }

         this.interiorTiles.clear();

         for (HashMap<Vector2, Network.TileData> tileDatas : saveData.mapTiles.interiorTiles) {
            HashMap<Vector2, Tile> tiles = null;
            if (tileDatas != null) {
               tiles = new HashMap<>();

               for (Network.TileDataBase tileData : tileDatas.values()) {
                  if (tileData.routeBelongsTo == null && tileData.tileName.contains("pkmnmansion") && !tileData.tileName.contains("stairs")) {
                     Route route = null;
                     tileData.routeBelongsTo = "pkmnmansion_temp";
                     if (!loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                        route = new Route("pkmnmansion1", 30);
                        loadedRoutes.put(tileData.routeBelongsTo, route);
                     }
                  }

                  if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                     loadedRoutes.put(tileData.routeBelongsTo, new Route(saveData.mapTiles.routes.get(tileData.routeBelongsTo)));
                  }

                  Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
                  Tile newTile = Tile.get(tileData, tempRoute);
                  tiles.put(newTile.position.cpy(), newTile);
               }
            }

            this.interiorTiles.add(tiles);
         }

         this.interiorTilesIndex = saveData.mapTiles.interiorTilesIndex;
         this.timeOfDay = saveData.mapTiles.timeOfDay;
         CycleDayNight.dayTimer = saveData.mapTiles.dayTimer;
         this.edges = saveData.mapTiles.edges;

         for (Network.PlayerDataBase playerData : saveData.players) {
            Player player = new Player(playerData);
            player.type = Player.Type.REMOTE;
            game.players.put(playerData.id, player);
         }

         game.player = new Player(saveData.playerData);

         for (Pokemon pokemon : game.player.pokemon) {
            pokemon.previousOwner = game.player;
         }

         game.cam.position.set(game.player.position.x + 16.0F, game.player.position.y, 0.0F);
         game.player.type = Player.Type.LOCAL;
         if (saveData.playerData.isInterior) {
            game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
         }

         if (game.player.currFieldMove.equals("FLY")) {
            PlayerStanding standingAction = null;

            for (Action action : game.actionStack) {
               if (PlayerStanding.class.isInstance(action)) {
                  standingAction = (PlayerStanding)action;
                  break;
               }
            }

            game.actionStack.remove(standingAction);
            game.insertAction(game.player.new Flying(game.player.pokemon.get(saveData.playerData.flyingIndex), false, null));
            game.cam.translate(0.0F, 16.0F, 0.0F);
         }

         game.map.pokemon.clear();

         for (Vector2 pos : saveData.overworldPokemon.keySet()) {
            Network.PokemonDataBase pokemonData = saveData.overworldPokemon.get(pos);
            Pokemon pokemon = new Pokemon(pokemonData);
            pokemon.position = pos.cpy();
            game.map.pokemon.put(pos, pokemon);
            game.insertAction(pokemon.new Standing());
         }

         float startTime = (float)System.currentTimeMillis();

         for (Tile tile : game.map.overworldTiles.values()) {
            game.map.coastify(tile, game.map.overworldTiles, true);
            if (tile.name.contains("_lower")) {
               System.out.println("tile.name");
               System.out.println(tile.name);
               tile.name = tile.name.split("_")[1];
               tile.init();
            }

            if (tile.name.equals("bridge1_water2") || tile.name.equals("bridge1_water5")) {
               String[] vals = tile.name.split("_");
               tile.name = vals[0];
               tile.squish(vals[1]);
            }
         }

         for (Tile tile : game.map.overworldTiles.values()) {
            if (tile.name.contains("bridge")) {
               Tile down = this.overworldTiles.get(tile.position.cpy().add(0.0F, -16.0F));
               if (down != null && (down.name.contains("water") || down.name.contains("_tidal") || down.name.contains("lava"))) {
                  down.belowBridge = true;
                  down.isSolid = true;
               }
            }
         }

         System.out.println("End coastify: " + String.valueOf((float)System.currentTimeMillis() - startTime));
         FileHandle file = Gdx.files.local(this.id + ".png");
         if (file.exists()) {
            this.minimap = new Pixmap(file);
         } else {
            int width = (int)(Game.staticGame.map.topRight.x - Game.staticGame.map.bottomLeft.x) / 8;
            int height = (int)(Game.staticGame.map.topRight.y - Game.staticGame.map.bottomLeft.y) / 8;
            Game.staticGame.map.minimap = new Pixmap(width, height, Format.RGBA8888);
            Game.staticGame.map.minimap.setColor(0.0F, 0.0F, 0.0F, 1.0F);
            Game.staticGame.map.minimap.fill();
            Vector2 startPos = game.player.position.cpy().add(-128.0F, -128.0F);
            startPos.x = (int)startPos.x - (int)startPos.x % 16;
            startPos.y = (int)startPos.y - (int)startPos.y % 16;
            Vector2 endPos = game.player.position.cpy().add(128.0F, 128.0F);
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
                  tile.updateMiniMap(game);
               }
            }
         }
      } catch (FileNotFoundException e) {
         System.out.println("No save file found for map: " + this.id);
      }
   }

   public void saveToFileNew(Game game) {
      if (game.coop != null && game.coop.isGuest()) { game.coop.saveGuest(); return; }
      if (game.coop != null && !game.coop.prepareSave()) return;
      PkmnMap.PeriodicSave.resetTimer();
      if (game.coop != null) { game.coop.saveHostSnapshot(); return; }
      System.out.println("Backing up previous save file...");

      try {
         ZipUtil.pack(new File(game.map.id + ".sav/"), new File(game.map.id + ".sav.zip"));
      } catch (Exception e) {
         e.printStackTrace();
      }

      System.out.println("Saving game to file...");
      FileHandle directory = Gdx.files.local(this.id + ".sav/");
      if (directory.exists() && !directory.isDirectory()) {
         directory.moveTo(Gdx.files.local(this.id + ".sav.old"));
      }

      if (!directory.exists()) {
         directory.mkdirs();
      }

      try {
         System.out.println("Writing game data...");
         Save.saveData(new Network.GameSaveData(game), this.id + ".sav/game");
         System.out.println("Writing map data...");
         Save.saveData(new Network.MapSaveData(game), this.id + ".sav/map" + this.currMapId + "");
         System.out.println("Writing player spawn data...");
         Save.saveData(new Network.PlayerSpawnLocationData(game.player), this.id + ".sav/spawn" + game.player.network.id + this.currMapId + "");
         System.out.println("Writing minimap data...");
         Save.saveMinimap(this, this.id + ".sav/map" + this.currMapId + ".png");
      } catch (Save.SaveFailure e) {
         Game.saveErrorLogAndNotifyUser("An issue occurred while saving:\n\n", e);
      }

      System.out.println("Done.");
   }

   public int height() {
      return (int)(this.topRight.y - this.bottomLeft.y);
   }

   public int width() {
      return (int)(this.topRight.x - this.bottomLeft.x);
   }

   public void loadMapFromFile(Game game) {
      System.out.println("currMapId");
      System.out.println(this.currMapId);

      try {
         Network.MapSaveData mapData = Save.readData(this.id + ".sav/map" + this.currMapId, Network.MapSaveData.class);
         this.overworldTiles.clear();
         this.bottomLeft = new Vector2();
         this.topRight = new Vector2();
         HashMap<String, Route> loadedRoutes = new HashMap<>();

         for (Network.TileDataBase tileData : mapData.mapTiles.tiles) {
            if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
               loadedRoutes.put(tileData.routeBelongsTo, new Route(mapData.mapTiles.routes.get(tileData.routeBelongsTo)));
            }

            Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
            if (tileData.tileNameUpper.equals("house5_middle1_right1")) {
               tileData.tileNameUpper = "house5_right1";
            }

            if (tileData.tileNameUpper.equals("house5_middle1_left1")) {
               tileData.tileNameUpper = "house5_left1";
            }

            if (tileData.tileNameUpper.equals("house5_middle1_middle1")) {
               tileData.tileNameUpper = "house5_middle1";
            }

            if (tileData.tileName.equals("ledge1_corner_bl")) {
               tileData.tileName = "green1";
               tileData.tileNameUpper = "ledges3_NE";
            } else if (tileData.tileName.equals("ledge1_corner_br")) {
               tileData.tileName = "green1";
               tileData.tileNameUpper = "ledges3_NW";
            }

            Tile newTile = Tile.get(tileData, tempRoute);
            this.overworldTiles.put(tileData.pos.cpy(), newTile);
            if (newTile.items != null && newTile.items.containsKey("trapinch")) {
               Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
               trapinch.isTrapping = true;
               trapinch.position = newTile.position.cpy();
               trapinch.mapTiles = this.overworldTiles;
               game.insertAction(trapinch.new Burrowed());
            }

            if (this.topRight.x < tileData.pos.x) {
               this.topRight.x = tileData.pos.x;
            }

            if (this.topRight.y < tileData.pos.y) {
               this.topRight.y = tileData.pos.y;
            }

            if (this.bottomLeft.x > tileData.pos.x) {
               this.bottomLeft.x = tileData.pos.x;
            }

            if (this.bottomLeft.y > tileData.pos.y) {
               this.bottomLeft.y = tileData.pos.y;
            }
         }

         this.interiorTiles.clear();

         for (HashMap<Vector2, Network.TileData> tileDatas : mapData.mapTiles.interiorTiles) {
            HashMap<Vector2, Tile> tiles = null;
            if (tileDatas != null) {
               tiles = new HashMap<>();

               for (Network.TileDataBase tileData : tileDatas.values()) {
                  if (tileData.routeBelongsTo == null && tileData.tileName.contains("pkmnmansion") && !tileData.tileName.contains("stairs")) {
                     Route route = null;
                     tileData.routeBelongsTo = "pkmnmansion_temp";
                     if (!loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                        route = new Route("pkmnmansion1", 30);
                        loadedRoutes.put(tileData.routeBelongsTo, route);
                     }
                  }

                  if (tileData.routeBelongsTo != null && !loadedRoutes.containsKey(tileData.routeBelongsTo)) {
                     loadedRoutes.put(tileData.routeBelongsTo, new Route(mapData.mapTiles.routes.get(tileData.routeBelongsTo)));
                  }

                  Route tempRoute = loadedRoutes.get(tileData.routeBelongsTo);
                  Tile newTile = Tile.get(tileData, tempRoute);
                  tiles.put(newTile.position.cpy(), newTile);
               }
            }

            this.interiorTiles.add(tiles);
         }

         this.interiorTilesIndex = mapData.mapTiles.interiorTilesIndex;
         this.edges = mapData.mapTiles.edges;
         this.pokemon.clear();

         for (Vector2 pos : mapData.overworldPokemon.keySet()) {
            Network.PokemonDataBase pokemonData = mapData.overworldPokemon.get(pos);
            Pokemon pokemon = new Pokemon(pokemonData);
            pokemon.position = pos.cpy();
            this.pokemon.put(pos, pokemon);
            game.insertAction(pokemon.new Standing());
         }

         for (Tile tile : this.overworldTiles.values()) {
            this.coastify(tile, this.overworldTiles, true);
            if (tile.name.contains("_lower")) {
               tile.name = tile.name.split("_")[1];
               tile.init();
            }

            if (tile.name.contains("bridge")) {
               Tile down = this.overworldTiles.get(tile.position.cpy().add(0.0F, -16.0F));
               if (down != null && (down.name.contains("water") || down.name.contains("_tidal") || down.name.contains("lava"))) {
                  down.belowBridge = true;
                  down.isSolid = true;
               }
            }
         }

         Network.PlayerSpawnLocationData spawnLocData = Save.readDataIfPresent(
            this.id + ".sav/spawn" + game.player.network.id + this.currMapId, Network.PlayerSpawnLocationData.class
         );
         if (spawnLocData != null) {
            game.player.spawnLoc = spawnLocData.position;
            game.player.spawnIndex = spawnLocData.spawnIndex;
            System.out.println("Loading player spawn from file");
            System.out.println("game.player.spawnLoc");
            System.out.println(game.player.spawnLoc);
            System.out.println("game.player.spawnIndex");
            System.out.println(game.player.spawnIndex);
         }

         FileHandle fileHandle = Gdx.files.local(this.id + ".sav/map" + this.currMapId + ".png");
         boolean fileLoaded = false;
         if (fileHandle.exists()) {
            try {
               this.minimap = new Pixmap(fileHandle);
               fileLoaded = true;
            } catch (GdxRuntimeException e) {
               e.printStackTrace();
            }
         }

         if (!fileLoaded) {
            int width = (int)(this.topRight.x - this.bottomLeft.x) / 8;
            int height = (int)(this.topRight.y - this.bottomLeft.y) / 8;
            this.minimap = new Pixmap(width, height, Format.RGBA8888);
            this.minimap.setColor(0.0F, 0.0F, 0.0F, 1.0F);
            this.minimap.fill();
            Vector2 startPos = game.player.position.cpy().add(-128.0F, -128.0F);
            startPos.x = (int)startPos.x - (int)startPos.x % 16;
            startPos.y = (int)startPos.y - (int)startPos.y % 16;
            Vector2 endPos = game.player.position.cpy().add(128.0F, 128.0F);
            endPos.x = (int)endPos.x - (int)endPos.x % 16;
            endPos.y = (int)endPos.y - (int)endPos.y % 16;
            Vector2 currPos = new Vector2(startPos.x, startPos.y);

            while (currPos.y < endPos.y) {
               Tile tile = this.overworldTiles.get(currPos);
               currPos.x += 16.0F;
               if (currPos.x > endPos.x) {
                  currPos.x = startPos.x;
                  currPos.y += 16.0F;
               }

               if (tile != null) {
                  tile.updateMiniMap(game);
               }
            }
         }
      } catch (GdxRuntimeException | Save.ReadFailure e) {
         System.out.println("No save file found for map: " + this.id + this.currMapId);
         System.out.println(this.id + this.currMapId);
         System.out.println(this.currMapId);
      }
   }

   public void loadFromFile(Game game) {
      try {
         FileHandle file = Gdx.files.local(game.map.id + ".sav");
         if (!file.isDirectory()) {
            System.out.println("Using legacy loading");
            this.loadFromFileLegacy(game);
            game.alreadyUsedDungeons.add("mansion1");
            game.alreadyUsedDungeons.add("regi1");
            game.alreadyUsedDungeons.add("desert_ruins1");
            return;
         }

         Network.GameSaveData gameData = Save.readData(game.map.id + ".sav/game", Network.GameSaveData.class);

         for (Network.PlayerDataBase playerData : gameData.players) {
            Player player = new Player(playerData);
            player.type = Player.Type.REMOTE;
            game.players.put(playerData.id, player);
         }

         if (Float.isNaN(gameData.playerData.position.x) || Float.isNaN(gameData.playerData.position.y)) {
            gameData.playerData.position.x = 0.0F;
            gameData.playerData.position.y = 0.0F;
         }

         game.player = new Player(gameData.playerData);

         for (Pokemon pokemon : game.player.pokemon) {
            pokemon.previousOwner = game.player;
         }

         game.cam.position.set(game.player.position.x + 16.0F, game.player.position.y, 0.0F);
         game.player.type = Player.Type.LOCAL;
         if (game.player.hmPokemon != null) {
            String currFieldMove = game.player.currFieldMove;
            if (!currFieldMove.equals("FLY")) {
               if (!currFieldMove.equals("")) {
                  game.player.swapSprites(game.player.hmPokemon);
               }

               if (!currFieldMove.equals("RIDE") && !currFieldMove.equals("SURF")) {
                  game.insertAction(game.player.hmPokemon.new Follow(game.player));
               }
            } else {
               PlayerStanding standingAction = null;

               for (Action action : game.actionStack) {
                  if (PlayerStanding.class.isInstance(action)) {
                     standingAction = (PlayerStanding)action;
                     break;
                  }
               }

               game.actionStack.remove(standingAction);
               game.insertAction(game.player.new Flying(game.player.hmPokemon, false, null));
               game.cam.translate(0.0F, 16.0F, 0.0F);
            }
         }

         this.timeOfDay = gameData.timeOfDay;
         CycleDayNight.dayTimer = gameData.dayTimer;
         this.currMapId = gameData.currMapId;
         game.alreadyUsedDungeons = gameData.alreadyUsedDungeons;
         System.out.println("used dungeons");

         for (String name : game.alreadyUsedDungeons) {
            System.out.println(name);
         }

         this.loadMapFromFile(game);
         if (gameData.playerData.isInterior) {
            game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
         }
      } catch (GdxRuntimeException | Save.ReadFailure e) {
         System.out.println("No save file found for map: " + this.id + this.currMapId);
      }
   }

   public void setView(Game game, OrthographicCamera camera) {
      float width = camera.viewportWidth * camera.zoom;
      float height = camera.viewportHeight * camera.zoom;
      this.viewBounds.set(camera.position.x - width / 2.0F, camera.position.y - height / 2.0F, width, height);
      if (!this.cacheBounds.contains(this.viewBounds) || this.refreshCache) {
         this.cacheOnscreenTiles(game);
      }

      if (this.refreshOnscreenPokemon) {
         this.cacheOnscreenPokemon(game);
      }
   }

   public void cacheOnscreenPokemon(Game game) {
      this.refreshOnscreenPokemon = false;
      System.out.println("Caching onscreen Pokemon");
      this.onscreenPokemon.clear();
      this.onscreenYsort1.clear();
      int unitScale = 1;
      float layerTileWidth = 16 * unitScale;
      float layerTileHeight = 16 * unitScale;
      int col1 = (int)(this.cacheBounds.x / layerTileWidth) - 2;
      int col2 = (int)((this.cacheBounds.x + this.cacheBounds.width + layerTileWidth) / layerTileWidth) + 1;
      int row1 = (int)(this.cacheBounds.y / layerTileHeight) - 3;
      int row2 = (int)((this.cacheBounds.y + this.cacheBounds.height + layerTileHeight) / layerTileHeight) + 3;
      Vector2 position = new Vector2();

      for (int row = row2; row >= row1; row--) {
         PkmnMap.YSort ySort = new PkmnMap.YSort(row * 16);

         for (int col = col1; col < col2; col++) {
            position.set(col * 16, row * 16);
            Pokemon pokemon = this.pokemon.get(position);
            if (pokemon != null) {
               this.onscreenPokemon.add(pokemon);
               ySort.ySortPokemon.add(pokemon);
            }

            Tile tile = this.tiles.get(position);
            if (tile != null && tile.overSprite != null) {
               if (tile.drawSavannaTree) {
                  ySort.ySortSavannaTrees.add(tile);
               }

               if (tile.drawAsTree) {
                  ySort.ySortTrees.add(tile);
               }
            }
         }

         this.onscreenYsort1.add(ySort);
      }
   }

   public void cacheOnscreenTiles(Game game) {
      this.refreshCache = false;
      if (game.debugInputEnabled) {
         System.out.println("Caching onscreen tiles");
      }

      this.onscreenBelowBridgeTiles.clear();
      this.onscreenDrawAsTree.clear();
      this.onscreenEggs.clear();
      this.onscreenExteriorTidal.clear();
      this.onscreenFossils.clear();
      this.onscreenGrass.clear();
      this.onscreenOverSprites.clear();
      this.onscreenPokemon.clear();
      this.onscreenSavannaTrees.clear();
      this.onscreenShoreOcean.clear();
      this.onscreenShoreTidal.clear();
      this.onscreenSpriteProxies.clear();
      this.onscreenSprites.clear();
      this.onscreenTiles.clear();
      this.onscreenBurrowed.clear();
      this.onscreenYsort1.clear();
      this.cacheBounds.x = this.viewBounds.x - 16.0F;
      this.cacheBounds.y = this.viewBounds.y - 16.0F;
      this.cacheBounds.width = this.viewBounds.width + 32.0F;
      this.cacheBounds.height = this.viewBounds.height + 32.0F;
      int unitScale = 1;
      float layerTileWidth = 16 * unitScale;
      float layerTileHeight = 16 * unitScale;
      int col1 = (int)(this.cacheBounds.x / layerTileWidth) - 2;
      int col2 = (int)((this.cacheBounds.x + this.cacheBounds.width + layerTileWidth) / layerTileWidth) + 1;
      int row1 = (int)(this.cacheBounds.y / layerTileHeight) - 3;
      int row2 = (int)((this.cacheBounds.y + this.cacheBounds.height + layerTileHeight) / layerTileHeight) + 3;
      Vector2 position = new Vector2();

      for (int row = row2; row >= row1; row--) {
         PkmnMap.YSort ySort = new PkmnMap.YSort(row * 16);

         for (int col = col1; col < col2; col++) {
            position.set(col * 16, row * 16);
            Tile tile = this.tiles.get(position);
            Pokemon pokemon = this.pokemon.get(position);
            if (pokemon != null) {
               this.onscreenPokemon.add(pokemon);
               ySort.ySortPokemon.add(pokemon);
               if (pokemon.isEgg && pokemon.mapTiles == this.tiles && pokemon.standingAction instanceof Pokemon.Standing) {
                  this.onscreenEggs.add(pokemon);
               }
            }

            pokemon = this.burrowedPokemon.get(position);
            if (pokemon != null) {
               this.onscreenBurrowed.add(pokemon);
            }

            if (tile != null) {
               this.onscreenTiles.add(tile);
               if (tile.sprite instanceof SmolSpriteProxy) {
                  this.onscreenSpriteProxies.add(tile);
               } else {
                  this.onscreenSprites.add(tile);
               }

               if (tile.shoreOcean != null) {
                  this.onscreenShoreOcean.add(tile);
               }

               if (tile.shoreTidal != null) {
                  this.onscreenShoreTidal.add(tile);
               }

               if (tile.belowBridge) {
                  this.onscreenBelowBridgeTiles.add(tile);
               }

               if (tile.overSprite != null) {
                  if (tile.isLedge) {
                     if (!tile.isWaterfall) {
                        this.onscreenOverSprites.add(tile);
                     }
                  } else {
                     this.onscreenOverSprites.add(tile);
                  }

                  if (tile.drawSavannaTree) {
                     this.onscreenSavannaTrees.add(tile);
                     ySort.ySortSavannaTrees.add(tile);
                  }

                  if (tile.isGrass && !tile.isTidal) {
                     this.onscreenGrass.add(tile);
                  }

                  if (tile.drawAsTree) {
                     this.onscreenDrawAsTree.add(tile);
                     ySort.ySortTrees.add(tile);
                  }
               }

               Tile exteriorTile = this.overworldTiles.get(position);
               if (exteriorTile != null && exteriorTile.isTidal) {
                  this.onscreenExteriorTidal.add(tile);
               }

               if (tile.hasItem != null) {
                  this.onscreenFossils.add(tile);
               }
            }
         }

         this.onscreenYsort1.add(ySort);
      }
   }

   static {
      if (Game.staticGame.cinematic) {
         nightColor = new Color(0.3F, 0.3F, 0.7F, 1.0F);
      } else {
         nightColor = new Color(0.3F, 0.3F, 0.7F, 1.0F);
      }

      POSITIONS = new Vector2[]{
         new Vector2(-16.0F, 0.0F),
         new Vector2(16.0F, 0.0F),
         new Vector2(0.0F, -16.0F),
         new Vector2(0.0F, 16.0F),
         new Vector2(-16.0F, -16.0F),
         new Vector2(16.0F, 16.0F),
         new Vector2(16.0F, -16.0F),
         new Vector2(-16.0F, 16.0F)
      };
   }

   public static class PeriodicSave extends Action {
      private float timeDelta = 60.0F;
      private float saveInterval = 60.0F;
      private static int framesSinceLastSave = 0;
      OutputStream outputStream;
      Output output;

      public static boolean isSaveOld() {
         return framesSinceLastSave >= 1200;
      }

      public static void resetTimer() {
         framesSinceLastSave = 0;
      }

      public static void increaseTimer() {
         framesSinceLastSave++;
      }

      public PeriodicSave(Game game) {
         super();
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.map_500;
      }

      @Override
      public void step(Game game) {
         resetTimer();
         if (game.isDedicatedClient()) return;
         if (game.type != Game.Type.CLIENT) {
            this.timeDelta = this.timeDelta + Gdx.graphics.getDeltaTime();
            if (this.timeDelta >= this.saveInterval) {
               this.timeDelta = 0.0F;
               System.out.println("Backing up previous save file...");

               try {
                  Files.copy(Paths.get(game.map.id + ".sav"), Paths.get(game.map.id + ".sav.backup"), StandardCopyOption.REPLACE_EXISTING);
               } catch (IOException e) {
                  e.printStackTrace();
               }

               System.out.println("Saving game to file...");

               try {
                  this.outputStream = new DeflaterOutputStream(new FileOutputStream(game.map.id + ".sav"));
                  this.output = new Output(this.outputStream);
               } catch (FileNotFoundException e) {
                  e.printStackTrace();
               }

               game.server.getKryo().writeObject(this.output, new Network.SaveData(game));
               this.output.close();
               System.out.println("Done.");
               FileHandle file = new FileHandle(game.map.id + ".png");
               PixmapIO.writePNG(file, game.map.minimap);
            }
         }
      }
   }

   public class ShadeEffect extends Action {
      int timer = 0;
      Color color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
      String type;

      public ShadeEffect(String type) {
         this(type, 30);
      }

      public ShadeEffect(String type, int timer) {
         super();
         if (type.equals("graveyard")) {
            this.color = new Color(0.9F, 0.9F, 1.0F, 1.0F);
         } else if (type.equals("volcano")) {
            this.color = new Color(1.0F, 0.7F, 0.7F, 1.0F);
         } else if (type.equals("deep_forest")) {
            this.color = new Color(0.7F, 0.7F, 1.0F, 1.0F);
         }

         this.type = type;
         this.timer = timer;
      }

      @Override
      public void firstStep(Game game) {
         game.actionStack.remove(PkmnMap.this.shadeEffect);
         PkmnMap.this.shadeEffect = this;
      }

      @Override
      public void step(Game game) {
         this.timer--;
         if (this.timer < 0) {
            game.mapBatch.setColor(this.color);
            if (this.type.equals("graveyard")) {
               FogEffect.type = FogEffect.Type.FOG;
               FogEffect.refresh = true;
            } else if (this.type.equals("volcano")) {
               FogEffect.type = FogEffect.Type.SMOKE;
               FogEffect.refresh = true;
            } else if (this.type.equals("deep_forest")) {
               FogEffect.type = FogEffect.Type.DEEPFOREST;
               FogEffect.refresh = true;
            } else {
               FogEffect.type = null;
            }

            game.actionStack.remove(this);
         }
      }
   }

   class UpkeepTimers extends Action {
      public Action.Layer layer = Action.Layer.map_130;
      int currIndex = 0;
      ArrayList<Pokemon>[] pokemonBuckets = new ArrayList[Pokemon.maxStagger];
      ArrayList<Pokemon> removeThese;

      UpkeepTimers() {
         super();

         for (int i = 0; i < Pokemon.maxStagger; i++) {
            this.pokemonBuckets[i] = new ArrayList<>();
         }

         this.removeThese = new ArrayList<>();
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void firstStep(Game game) {
         this.currIndex = Pokemon.currStagger;
      }

      @Override
      public void step(Game game) {
         for (Pokemon pokemon : this.pokemonBuckets[this.currIndex]) {
            if (pokemon.drawLower == null) {
               this.removeThese.add(pokemon);
            } else {
               pokemon.drawLower.step(game);
            }
         }

         while (!this.removeThese.isEmpty()) {
            this.pokemonBuckets[this.currIndex].remove(this.removeThese.remove(0));
         }

         this.currIndex = ++this.currIndex % Pokemon.maxStagger;
      }
   }

   public class YSort {
      public ArrayList<Tile> ySortTrees = new ArrayList<>();
      public ArrayList<Tile> ySortSavannaTrees = new ArrayList<>();
      public ArrayList<Pokemon> ySortPokemon = new ArrayList<>();
      int yPos;

      public YSort(int yPos) {
         this.yPos = yPos;
      }
   }
}
