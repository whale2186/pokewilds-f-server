package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

class GenIsland1 extends Action {
   public Action.Layer layer = Action.Layer.map_0;
   ArrayList<Vector2> freePositions;
   HashMap<Vector2, Tile> tilesToAdd;
   ArrayList<HashMap<Vector2, Tile>> interiorTilesToAdd = new ArrayList<>();
   HashMap<Vector2, Pokemon> pokemonToAdd;
   ArrayList<Action> doActions;
   Vector2 bottomLeft;
   Vector2 topRight;
   Vector2 origin;
   int radius;
   ArrayList<Tile> edges;
   Random rand;
   public static boolean donePkmnMansion = false;
   public static boolean donePkmnMansionKey = false;
   public static boolean doneRegiDungeon = false;
   public static boolean doneFossilBuilding = false;
   public static int unownCounter = 0;
   ArrayList<Vector2> keystoneSpawns;
   public ArrayList<Vector2> noIceHerePlease;
   public ArrayList<Vector2> bottomMtnLayer;
   public HashMap<Vector2, Tile> mtnTiles;
   public int doneDesert;
   public HashMap<Pokemon, Integer> interiorPokemon;
   public ArrayList<Vector2> trapinchSpawns;
   public Vector2 randOffset;
   public static int numSandPits = 0;

   public GenIsland1(final Game game, Vector2 origin, int radius) {
      super();

      for (int i = 0; i < 102; i++) {
         this.interiorTilesToAdd.add(null);
      }

      this.pokemonToAdd = new HashMap<>();
      this.bottomLeft = null;
      this.topRight = null;
      this.edges = new ArrayList<>();
      this.keystoneSpawns = new ArrayList<>();
      this.noIceHerePlease = new ArrayList<>();
      this.bottomMtnLayer = new ArrayList<>();
      this.mtnTiles = new HashMap<>();
      this.doneDesert = 0;
      this.interiorPokemon = new HashMap<>();
      this.trapinchSpawns = new ArrayList<>();
      this.randOffset = new Vector2();
      this.randOffset.x = Game.rand.nextInt(100) * 16.0F;
      this.randOffset.y = Game.rand.nextInt(100) * 16.0F;
      this.radius = radius;
      this.origin = origin;
      this.rand = new Random();
      this.tilesToAdd = new HashMap<>();
      this.doActions = new ArrayList<>();
      int maxDist = this.radius;
      Tile originTile = new Tile("sand1", this.origin.cpy());
      this.tilesToAdd.put(originTile.position.cpy(), originTile);
      ArrayList<Tile> endPoints = this.ApplyBlotchMountain(game, originTile, maxDist, this.mtnTiles);
      System.out.println("endPoints size");
      System.out.println(endPoints.size());

      for (Tile tile : endPoints) {
         if (this.doneDesert == 1) {
            Route blotchRoute = new Route("desert1", 40);
            this.ApplyBlotch(game, "desert", tile, maxDist / 18 + maxDist / 4, this.tilesToAdd, 1, true, blotchRoute);
         } else {
            this.doneDesert++;
            Route blotchRoute = new Route("forest1", 40);
            this.ApplyBlotch(game, "island", tile, maxDist / 15, this.tilesToAdd, 1, true, blotchRoute);
         }
      }

      long startTime = System.currentTimeMillis();

      for (Tile tile : this.mtnTiles.values()) {
         if (this.tilesToAdd.containsKey(tile.position)) {
            Tile currTile = this.tilesToAdd.get(tile.position);
            if (!currTile.name.equals("water2") && this.bottomMtnLayer.contains(tile.position)) {
               continue;
            }
         }

         this.tilesToAdd.put(tile.position, tile);
      }

      System.out.println("End mountain tile dither: " + String.valueOf(System.currentTimeMillis() - startTime));

      for (Vector2 pos : this.mtnTiles.keySet()) {
         if (this.pokemonToAdd.containsKey(pos)) {
            this.pokemonToAdd.remove(pos);
         }
      }

      ArrayList<Tile> desertTiles = new ArrayList<>();
      ArrayList<Tile> deepForestTiles = new ArrayList<>();
      ArrayList<Tile> forestTiles = new ArrayList<>();
      Vector2 maxPos = this.origin.cpy();
      Vector2 minPos = this.origin.cpy();

      for (Tile tile : new ArrayList<>(this.tilesToAdd.values())) {
         if (maxPos.x < tile.position.x) {
            maxPos.x = tile.position.x;
         }

         if (maxPos.y < tile.position.y) {
            maxPos.y = tile.position.y;
         }

         if (minPos.x > tile.position.x) {
            minPos.x = tile.position.x;
         }

         if (minPos.y > tile.position.y) {
            minPos.y = tile.position.y;
         }

         if (tile.name.contains("desert")) {
            desertTiles.add(tile);
         } else if (tile.biome.equals("deep_forest")) {
            deepForestTiles.add(tile);
         } else if (tile.routeBelongsTo != null && tile.routeBelongsTo.name.equals("forest1")) {
            forestTiles.add(tile);
         }
      }

      int placeNum = this.rand.nextInt(2) + 2;
      int tries3 = 20;

      while (deepForestTiles.size() > 0) {
         Tile originTile2 = forestTiles.get(this.rand.nextInt(forestTiles.size()));
         boolean isInForest = originTile2.routeBelongsTo != null && originTile2.routeBelongsTo.name.equals("forest1");
         boolean nearWater = false;

         for (Tile newTile : this.tilesToAdd.values()) {
            if (newTile.name.contains("water") && newTile.position.dst2(originTile2.position) < 57600.0F) {
               nearWater = true;
               break;
            }
         }

         if (!this.tilesToAdd.get(originTile2.position).biome.equals("savanna") && isInForest && !nearWater) {
            HashMap<Vector2, Tile> newTiles = new HashMap<>();
            Route blotchRoute = new Route("savanna2", 22);
            this.ApplyBlotch(game, "savanna1", originTile2, 800, newTiles, 0, false, blotchRoute);

            for (Vector2 position : newTiles.keySet()) {
               if (!this.mtnTiles.containsKey(position)) {
                  this.tilesToAdd.put(position, newTiles.get(position));
               }
            }

            if (--placeNum <= 0) {
               break;
            }
         } else if (--tries3 <= 0) {
            break;
         }
      }

      placeNum = this.rand.nextInt(2) + 2;
      tries3 = 20;

      while (deepForestTiles.size() > 0) {
         Tile originTile2 = deepForestTiles.get(this.rand.nextInt(deepForestTiles.size()));
         if (!originTile2.biome.equals("graveyard") && !this.mtnTiles.containsKey(originTile2.position)) {
            HashMap<Vector2, Tile> newTiles = new HashMap<>();
            Route blotchRoute = new Route("graveyard1", 22);
            this.ApplyBlotch(game, "graveyard1", originTile2, 300, newTiles, 0, false, blotchRoute);

            for (Vector2 position : newTiles.keySet()) {
               if (!this.mtnTiles.containsKey(position)) {
                  this.tilesToAdd.put(position, newTiles.get(position));
               }
            }

            if (--placeNum <= 0) {
               break;
            }
         } else if (--tries3 <= 0) {
            break;
         }
      }

      if (!this.keystoneSpawns.isEmpty()) {
         Vector2 edge = this.keystoneSpawns.get(this.rand.nextInt(this.keystoneSpawns.size()));
         Tile newTile4 = this.tilesToAdd.get(edge);
         newTile4 = new Tile("green12", "odd_keystone2", edge.cpy(), true, newTile4.routeBelongsTo);
         newTile4.biome = "graveyard";
         this.tilesToAdd.put(edge, newTile4);
      }

      Route riverRoute = new Route("wooded_lake_water1", 30);
      Route currRoute = null;

      for (Tile tile : this.tilesToAdd.values()) {
         if (!tile.isTidal) {
            currRoute = riverRoute;
            String name = "water2";
            float scale = 60.0F;
            int threshold = 850;
            if (desertTiles.contains(tile)) {
               name = "sand1";
               currRoute = tile.routeBelongsTo;
               threshold = 1250;
            }

            if (this.mtnTiles.containsKey(tile.position)) {
               if (this.noIceHerePlease.contains(tile.position)) {
                  continue;
               }

               if (tile.biome.equals("tundra")) {
                  name = "ice2";
                  currRoute = tile.routeBelongsTo;
               }

               if (this.bottomMtnLayer.contains(tile.position)) {
                  name = "sand3_tidalwater";
                  if (tile.biome.equals("desert")) {
                     continue;
                  }
               }

               scale = 59.0F;
            }

            Vector2 rotate = tile.position.cpy().rotateDeg(-((float)Math.toDegrees(Math.atan(tile.position.x / tile.position.y))));
            float wavelength = 4.0F;
            float amplitude = 4.0F;
            float offset3 = (float)Math.sin(rotate.x / (scale * wavelength));
            offset3 *= amplitude;
            float offset1 = Math.abs(Math.floorMod((int)(rotate.y / scale - offset3), 14) - 7);
            offset1 *= 2.0F;
            float offset2 = Math.abs(Math.floorMod((int)(rotate.y / scale - offset3), 20) - 10);
            offset2 *= 2.0F;
            if (offset1 * 5.0F * offset2 > threshold && !name.equals("waterfall1")) {
               tile.name = name;
               tile.nameUpper = "";
               tile.routeBelongsTo = currRoute;
               tile.init();
               if (this.pokemonToAdd.containsKey(tile.position)) {
                  this.pokemonToAdd.remove(tile.position);
               }
            }
         }
      }

      Rectangle islandBoundary = new Rectangle(
         minPos.x - 672.0F, minPos.y - 672.0F, maxPos.x - minPos.x + 672.0F + 672.0F, maxPos.y - minPos.y + 672.0F + 336.0F
      );
      maxPos.add(896.0F, 448.0F);
      minPos.sub(896.0F, 896.0F);
      Vector2 pos = new Vector2();
      Route oceanRoute = new Route("ocean1", 40);
      Route tempRoute = null;

      for (float i = minPos.x; i <= maxPos.x; i += 16.0F) {
         for (float j = minPos.y; j <= maxPos.y; j += 16.0F) {
            pos.set(i, j);
            String tileName2 = "water2";
            if (j == maxPos.y) {
               tileName2 = "water2_edge1";
            }

            tempRoute = null;
            if (!islandBoundary.contains(pos)) {
               tempRoute = oceanRoute;
            }

            if (!this.tilesToAdd.containsKey(pos)) {
               this.tilesToAdd.put(pos.cpy(), new Tile(tileName2, pos.cpy(), true, tempRoute));
            }
         }
      }

      this.bottomLeft = minPos.cpy();
      this.topRight = maxPos.cpy();
      this.ledgify(this.tilesToAdd, this.tilesToAdd, "mountain4", "sand4", "beach");

      for (Tile tile : this.tilesToAdd.values()) {
         if (tile.nameUpper.equals("stalagmite1")) {
            tile.name = "stalagmite1";
            tile.nameUpper = "";
            tile.overSprite = null;
            if (this.rand.nextInt(10) == 0) {
               tile.nameUpper = "tree6";
               tile.init();
            } else if (this.rand.nextInt(10) == 0) {
               tile.nameUpper = "rock1";
               tile.init();
            } else if (this.rand.nextInt(10) == 0) {
               tile.nameUpper = "rock1_color";
               tile.init();
            }
         }
      }

      this.ledgify(this.tilesToAdd, this.tilesToAdd, "stalagmite1", "mountain4", "beach");

      for (Tile tile : desertTiles) {
         if (this.pokemonToAdd.containsKey(tile.position)) {
            this.pokemonToAdd.remove(tile.position);
         }
      }

      boolean doneDesertRuins = false;

      for (String dungeonName : game.alreadyUsedDungeons) {
         if (dungeonName.equals("mansion1")) {
            donePkmnMansion = true;
            donePkmnMansionKey = true;
         } else if (dungeonName.equals("desert_ruins1")) {
            doneDesertRuins = true;
         } else if (dungeonName.equals("regi1")) {
            doneRegiDungeon = true;
         }
      }

      if (!donePkmnMansion || !doneDesertRuins) {
         doneRegiDungeon = true;
      }

      Rectangle[] hitBoxes = new Rectangle[]{
         new Rectangle(0.0F, 0.0F, 1056.0F, 1056.0F), new Rectangle(0.0F, 0.0F, 640.0F, 640.0F), new Rectangle(0.0F, 0.0F, 208.0F, 160.0F)
      };
      Vector2[] origins = new Vector2[]{null, null, null};
      Vector2 mansionPos = new Vector2(0.0F, 224.0F);
      Rectangle mansionBox = new Rectangle(0.0F, 0.0F, 528.0F, 496.0F);
      mansionBox.setCenter(mansionPos);
      int i = 0;
      int tries = 0;

      while (i < hitBoxes.length) {
         if (i == 0 && doneDesertRuins) {
            i++;
         } else if (i == 2 && doneRegiDungeon) {
            i++;
         } else {
            if (i < 2 && desertTiles.size() > 0) {
               origins[i] = desertTiles.get(this.rand.nextInt(desertTiles.size())).position;
            } else {
               origins[i] = deepForestTiles.get(this.rand.nextInt(deepForestTiles.size())).position;
            }

            if (i == 0) {
               for (int tries2 = 0; tries2 < 80 && desertTiles.size() > 0 && (origins[i].x % 32.0F != 0.0F || origins[i].y % 32.0F != 16.0F); tries2++) {
                  origins[i] = desertTiles.get(this.rand.nextInt(desertTiles.size())).position;
               }
            }

            hitBoxes[i].setCenter(origins[i]);
            if (i != 0 || !hitBoxes[i].overlaps(mansionBox)) {
               if (i > 1) {
                  boolean hitsMountain = false;

                  for (Vector2 position : this.mtnTiles.keySet()) {
                     if (hitBoxes[i].contains(position)) {
                        hitsMountain = true;
                        break;
                     }
                  }

                  if (hitsMountain) {
                     continue;
                  }
               }

               boolean worked = true;

               for (int j = 0; j < i; j++) {
                  if (hitBoxes[i].overlaps(hitBoxes[j])) {
                     worked = false;
                     break;
                  }
               }

               if (!worked && tries <= 10) {
                  tries++;
               } else {
                  i++;
                  tries = 0;
                  if (tries > 10) {
                     System.out.println("Gave up.");
                     System.out.println(i);
                  }
               }
            }
         }
      }

      if (!doneDesertRuins) {
         this.generateDesertRuins(game, origins[0]);
         game.alreadyUsedDungeons.add("desert_ruins1");
      }

      this.generateOasis(game, origins[1]);
      boolean complete = false;
      Vector2 keyLoc = null;
      HashMap<Vector2, Tile> mansionExteriorTiles = new HashMap<>();
      ArrayList<HashMap<Vector2, Tile>> mansionInteriorTiles = new ArrayList<>();

      label1117:
      while (!complete) {
         complete = true;

         for (Tile tile : new ArrayList<>(this.tilesToAdd.values())) {
            if (tile.name.contains("tree_large1")) {
               boolean found = false;

               for (int j = 0; j < 2; j++) {
                  for (int k = 0; k < 2; k++) {
                     Vector2 tl = tile.position.cpy().add(-16 * j, 16 * k);
                     Vector2 tr = tile.position.cpy().add(-16 * j + 16, 16 * k);
                     Vector2 bl = tile.position.cpy().add(-16 * j, 16 * k - 16);
                     Vector2 br = tile.position.cpy().add(-16 * j + 16, 16 * k - 16);
                     boolean foundTl = this.tilesToAdd.containsKey(tl) && this.tilesToAdd.get(tl).name.equals("tree_large1_noSprite");
                     boolean foundTr = this.tilesToAdd.containsKey(tr) && this.tilesToAdd.get(tr).name.equals("tree_large1_noSprite");
                     boolean foundBl = this.tilesToAdd.containsKey(bl) && this.tilesToAdd.get(bl).name.equals("tree_large1");
                     boolean foundBr = this.tilesToAdd.containsKey(br) && this.tilesToAdd.get(br).name.equals("tree_large1_noSprite");
                     if (foundTl && foundTr && foundBl && foundBr) {
                        found = true;
                        break;
                     }
                  }

                  if (found) {
                     break;
                  }
               }

               if (!found) {
                  int randInt = this.rand.nextInt(3);
                  if (randInt == 2) {
                     tile.routeBelongsTo = new Route("", 22);
                     String[] pokemon = new String[]{
                        "pineco",
                        "aipom",
                        "kakuna",
                        "metapod",
                        "spinarak",
                        "heracross",
                        "ledyba",
                        "hoothoot",
                        "zubat",
                        "pidgey",
                        "spearow",
                        "forretress",
                        "applin"
                     };
                     randInt = this.rand.nextInt(pokemon.length);
                     tile.routeBelongsTo.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                  }

                  tile.name = "bush1";
                  tile.nameUpper = "";
               }
            } else if (!tile.name.equals("rock1")) {
               if (tile.nameUpper.equals("cactus10")) {
                  this.tilesToAdd
                     .put(tile.position.cpy().add(16.0F, 0.0F), new Tile("desert4", "solid", tile.position.cpy().add(16.0F, 0.0F), true, tile.routeBelongsTo));
               }
            } else {
               Vector2 left = tile.position.cpy().add(-16.0F, 0.0F);
               Vector2 tl = tile.position.cpy().add(-16.0F, 16.0F);
               Vector2 bl = tile.position.cpy().add(-16.0F, -16.0F);
               Vector2 right = tile.position.cpy().add(16.0F, 0.0F);
               Vector2 tr = tile.position.cpy().add(16.0F, 16.0F);
               Vector2 br = tile.position.cpy().add(16.0F, -16.0F);
               Vector2 up = tile.position.cpy().add(0.0F, 16.0F);
               Vector2 down = tile.position.cpy().add(0.0F, -16.0F);
               boolean touchLeft = this.tilesToAdd.containsKey(left) && this.tilesToAdd.get(left).isSolid
                  || this.tilesToAdd.containsKey(tl) && this.tilesToAdd.get(tl).isSolid
                  || this.tilesToAdd.containsKey(bl) && this.tilesToAdd.get(bl).isSolid;
               boolean touchRight = this.tilesToAdd.containsKey(right) && this.tilesToAdd.get(right).isSolid
                  || this.tilesToAdd.containsKey(tr) && this.tilesToAdd.get(tr).isSolid
                  || this.tilesToAdd.containsKey(br) && this.tilesToAdd.get(br).isSolid;
               boolean touchUp = this.tilesToAdd.containsKey(up) && this.tilesToAdd.get(up).isSolid
                  || this.tilesToAdd.containsKey(tl) && this.tilesToAdd.get(tl).isSolid
                  || this.tilesToAdd.containsKey(tr) && this.tilesToAdd.get(tr).isSolid;
               boolean touchDown = this.tilesToAdd.containsKey(down) && this.tilesToAdd.get(down).isSolid
                  || this.tilesToAdd.containsKey(bl) && this.tilesToAdd.get(bl).isSolid
                  || this.tilesToAdd.containsKey(br) && this.tilesToAdd.get(br).isSolid;
               if (touchLeft && touchRight || touchUp && touchDown) {
                  this.tilesToAdd.put(tile.position, new Tile("sand1", tile.position.cpy(), true, tile.routeBelongsTo));
               }
            }

            if (this.pokemonToAdd.containsKey(tile.position)
               && (tile.isSolid && !tile.name.contains("water") || tile.name.contains("ledge") || tile.name.contains("door") || tile.nameUpper.contains("door"))
               )
             {
               Pokemon var163 = this.pokemonToAdd.remove(tile.position);
            }

            if (tile.biome.equals("deep_forest")
               && !donePkmnMansion
               && !this.mtnTiles.containsKey(tile.position.cpy().add(0.0F, -368.0F))
               && !this.mtnTiles.containsKey(tile.position.cpy().add(-224.0F, 0.0F))
               && !this.mtnTiles.containsKey(tile.position.cpy().add(144.0F, -240.0F))) {
               mansionPos = tile.position.cpy();
               if (this.radius < 40000) {
                  mansionPos = new Vector2(0.0F, 224.0F);
               }

               mansionBox.setCenter(mansionPos);
               if (!mansionBox.overlaps(hitBoxes[0]) && !mansionBox.overlaps(hitBoxes[1])) {
                  game.alreadyUsedDungeons.add("mansion1");
                  donePkmnMansion = true;
                  complete = false;
                  int triesx = 0;

                  while (triesx < 4) {
                     try {
                        this.generateMansion(game, mansionExteriorTiles, mansionInteriorTiles, mansionPos);
                        this.tilesToAdd.putAll(mansionExteriorTiles);

                        for (int ix = 0; ix < mansionInteriorTiles.size(); ix++) {
                           HashMap<Vector2, Tile> currLayer = mansionInteriorTiles.get(ix);
                           if (ix >= this.interiorTilesToAdd.size()) {
                              this.interiorTilesToAdd.add(currLayer);
                           } else if (currLayer != null) {
                              if (this.interiorTilesToAdd.get(ix) == null) {
                                 this.interiorTilesToAdd.remove(ix);
                                 this.interiorTilesToAdd.add(ix, currLayer);
                              } else {
                                 for (Vector2 key : currLayer.keySet()) {
                                    this.interiorTilesToAdd.get(ix).put(key, currLayer.get(key));
                                 }
                              }
                           }
                        }
                        break;
                     } catch (Exception e) {
                        System.out.println("Failed to generate mansion: " + e.getMessage());
                        System.out.println("Retrying...");
                        mansionExteriorTiles.clear();
                        mansionInteriorTiles.clear();
                        triesx++;
                        e.printStackTrace();
                     }
                  }
                  break;
               }
            } else {
               if (tile.biome.equals("deep_forest") && tile.name.contains("green") && !donePkmnMansionKey) {
                  Vector2 down = tile.position.cpy().add(0.0F, -16.0F);
                  if (this.tilesToAdd.containsKey(down) && !this.tilesToAdd.get(down).nameUpper.contains("tree")) {
                     donePkmnMansionKey = true;
                     complete = false;
                     this.tilesToAdd.put(tile.position, new Tile(tile.name, "pokemon_mansion_key", tile.position.cpy(), true, null));
                     Vector2 var119 = tile.position;
                     break;
                  }
               }

               if (tile.biome.equals("deep_forest")
                  && !doneRegiDungeon
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(0.0F, -368.0F))
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(-224.0F, 0.0F))
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(144.0F, -240.0F))
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(32.0F, 32.0F))
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(-32.0F, 32.0F))
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(32.0F, -32.0F))
                  && !this.mtnTiles.containsKey(tile.position.cpy().add(-32.0F, -32.0F))
                  && this.rand.nextInt(3) == 2) {
                  Vector2 regiPos = tile.position.cpy();
                  boolean collidesWithMansion = false;
                  hitBoxes[2].setCenter(regiPos);
                  Iterator var182 = mansionExteriorTiles.values().iterator();

                  while (true) {
                     if (var182.hasNext()) {
                        Tile mansionTile = (Tile)var182.next();
                        if (!hitBoxes[2].contains(mansionTile.position)) {
                           continue;
                        }

                        collidesWithMansion = true;
                     }

                     if (!collidesWithMansion) {
                        game.alreadyUsedDungeons.add("regi1");
                        doneRegiDungeon = true;
                        complete = false;
                        this.generateRegiDungeon(game, regiPos);
                        continue label1117;
                     }
                     break;
                  }
               }
            }
         }
      }

      ArrayList<Tile> mtnWaterTiles = new ArrayList<>();
      startTime = System.currentTimeMillis();
      Vector2[] positions = new Vector2[]{
         new Vector2(-16.0F, 0.0F),
         new Vector2(16.0F, 0.0F),
         new Vector2(-16.0F, -16.0F),
         new Vector2(16.0F, 16.0F),
         new Vector2(16.0F, -16.0F),
         new Vector2(-16.0F, 16.0F),
         new Vector2(0.0F, -16.0F),
         new Vector2(0.0F, 16.0F)
      };

      for (int ix = 0; ix < 1; ix++) {
         for (Tile tile : new ArrayList<>(this.tilesToAdd.values())) {
            if (tile.routeBelongsTo != null && tile.routeBelongsTo.name.equals("wooded_lake_water1")) {
               for (Vector2 position : positions) {
                  Tile nextTile = this.tilesToAdd.get(tile.position.cpy().add(position));
                  if (nextTile != null
                     && nextTile.name.equals("water2")
                     && (nextTile.routeBelongsTo == null || !nextTile.routeBelongsTo.name.equals("wooded_lake_water1"))) {
                     tile.name = "sand3_tidalwater";
                     tile.init();
                  }
               }
            } else if (!tile.name.equals("water2")
               && !tile.name.equals("water2_edge1")
               && !tile.name.contains("mountain4")
               && !tile.name.contains("ice")
               && !tile.name.contains("waterfall")) {
               for (Vector2 position : positions) {
                  Tile nextTile = this.tilesToAdd.get(tile.position.cpy().add(position));
                  if (nextTile != null && nextTile.name.equals("water2")) {
                     if (this.noIceHerePlease.contains(tile.position) && tile.nameUpper.contains("ledges3")) {
                        tile.name = "sand3";
                        tile.init();
                        if (tile.position.x == nextTile.position.x || tile.position.y == nextTile.position.y) {
                           mtnWaterTiles.add(tile);
                           break;
                        }
                     } else if (ix <= 0 || nextTile.routeBelongsTo == null || !nextTile.routeBelongsTo.name.equals("wooded_lake_water1")) {
                        String name = "sand3";
                        if (tile.name.contains("desert")) {
                           name = "sand3_desertEdge";
                        } else if (tile.name.contains("_tidal")) {
                           name = "sand3_tidalwater";
                        } else if (tile.biome.equals("beach_blacksand")) {
                           name = "sand3_black";
                        } else if (ix > 0) {
                           name = "sand3_tidalwater";
                        }

                        Tile newTile = new Tile(name, nextTile.position.cpy(), true, tile.routeBelongsTo);
                        this.tilesToAdd.put(newTile.position.cpy(), newTile);
                        if (!tile.biome.contains("beach")
                           && !tile.biome.contains("mountain")
                           && !tile.biome.contains("savanna")
                           && !tile.biome.contains("wooded_lake")
                           && ix == 0
                           && !tile.name.contains("desert")
                           && (tile.routeBelongsTo == null || !tile.routeBelongsTo.name.equals("oasis1") && !tile.routeBelongsTo.name.equals("ruins1_outer"))
                           && (nextTile.routeBelongsTo == null || !nextTile.routeBelongsTo.name.equals("wooded_lake_water1"))) {
                           this.edges.add(newTile);
                        }
                     }
                  }
               }
            }
         }
      }

      System.out.println("End post-process: " + String.valueOf(System.currentTimeMillis() - startTime));
      ArrayList<Tile> newWaterTiles = new ArrayList<>();
      ArrayList<Vector2> waterPositions = new ArrayList<>();

      for (Tile tile : mtnWaterTiles) {
         Tile nextTile = tile;
         waterPositions.add(nextTile.position.cpy());

         while (!waterPositions.isEmpty()) {
            pos = waterPositions.remove(0);
            nextTile = this.tilesToAdd.get(pos);
            newWaterTiles.add(nextTile);
            if (newWaterTiles.size() > 25) {
               newWaterTiles.clear();
               break;
            }

            if (!nextTile.nameUpper.contains("ledge")) {
               if (!nextTile.name.contains("water")) {
                  newWaterTiles.clear();
               }
               break;
            }

            if (nextTile.nameUpper.contains("N")) {
               waterPositions.add(pos.cpy().add(0.0F, -16.0F));
            }

            if (nextTile.nameUpper.contains("S")) {
               waterPositions.add(pos.cpy().add(0.0F, 16.0F));
            }

            if (nextTile.nameUpper.contains("E")) {
               waterPositions.add(pos.cpy().add(-16.0F, 0.0F));
            }

            if (nextTile.nameUpper.contains("W")) {
               waterPositions.add(pos.cpy().add(16.0F, 0.0F));
            }
         }

         if (newWaterTiles.size() > 2) {
            String prevName = "";

            for (Tile tile2 : newWaterTiles) {
               if (tile2.nameUpper.contains("ledges3")) {
                  tile2.name = "water2";
                  tile2.init();
                  game.map.waterfallify(tile2, this.tilesToAdd, true);
                  prevName = tile2.name;
               } else {
                  tile2.name = "sand3";
                  tile2.nameUpper = prevName;
                  tile2.init();
               }
            }
         }

         newWaterTiles.clear();
         waterPositions.clear();
      }

      startTime = System.currentTimeMillis();
      final ArrayList<Tile> puddleSand = new ArrayList<>();
      final HashMap<Vector2, Tile> finalTiles = this.tilesToAdd;
      Runnable runnable = new Runnable() {
         @Override
         public void run() {
            try {
               for (Tile tile : finalTiles.values()) {
                  boolean coastified = game.map.coastify(tile, finalTiles, true);
                  if (tile.name.contains("_puddle1")) {
                     puddleSand.add(tile);
                  }
               }
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

      String[] directions = new String[]{"W", "E", "S", "N", "SW", "NE", "SE", "NW"};
      positions = new Vector2[]{
         new Vector2(-16.0F, 0.0F),
         new Vector2(16.0F, 0.0F),
         new Vector2(0.0F, -16.0F),
         new Vector2(0.0F, 16.0F),
         new Vector2(-16.0F, -16.0F),
         new Vector2(16.0F, 16.0F),
         new Vector2(16.0F, -16.0F),
         new Vector2(-16.0F, 16.0F)
      };
      System.out.println("End post-process: " + String.valueOf(System.currentTimeMillis() - startTime));
      System.out.println("Timing: " + String.valueOf(Game.staticGame.currTime));

      for (Tile tile : puddleSand) {
         String direction = "";

         for (int ix = 0; ix < positions.length && ix < 4; ix++) {
            Tile nextTile = this.tilesToAdd.get(tile.position.cpy().add(positions[ix]));
            if (nextTile != null && nextTile.name.contains("_puddle1")) {
               direction = direction + "[" + directions[ix] + "]";
            }
         }

         tile.name = tile.name.replace("_puddle1", "_puddle1" + direction);
         tile.init();
      }

      for (Tile tile : this.tilesToAdd.values()) {
         if ((!tile.isSolid || tile.name.contains("water"))
            && tile.nameUpper.equals("")
            && tile.routeBelongsTo != null
            && !tile.routeBelongsTo.name.equals("")
            && tile.routeBelongsTo.allowedPokemon().size() > 0
            && !tile.routeBelongsTo.dontSpawnOverworlds
            && (
               !tile.name.contains("water")
                  || tile.routeBelongsTo != null
                     && (
                        tile.routeBelongsTo.name.contains("beach2")
                           || tile.routeBelongsTo.name.equals("ocean1")
                           || tile.routeBelongsTo.name.equals("mountain1_water1")
                           || tile.routeBelongsTo.name.equals("wooded_lake_water1")
                     )
            )) {
            if (!tile.routeBelongsTo.name.equals("desert1")) {
               int baseChance = 509;
               if (tile.routeBelongsTo.name.contains("forest")) {
                  baseChance = 509;
               } else if (tile.routeBelongsTo.name.contains("oasis")) {
                  baseChance = 460;
               } else if (tile.routeBelongsTo.name.contains("ruins")) {
                  baseChance = 480;
               } else if (tile.routeBelongsTo.name.equals("beach2")) {
                  baseChance = 509;
               } else if (tile.routeBelongsTo.name.equals("beach2_water")) {
                  baseChance = 506;
               } else if (tile.routeBelongsTo.name.equals("beach2_rocks")) {
                  baseChance = 500;
               } else if (tile.routeBelongsTo.name.equals("beach2_plateau")) {
                  baseChance = 500;
               } else if (tile.routeBelongsTo.name.contains("beach")) {
                  baseChance = 480;
               } else if (tile.routeBelongsTo.name.contains("graveyard")) {
                  baseChance = 506;
               } else if (tile.routeBelongsTo.name.equals("savanna2")) {
                  baseChance = 509;
               } else if (tile.routeBelongsTo.name.equals("mountain1_water1")) {
                  baseChance = 506;
               } else if (tile.routeBelongsTo.name.equals("volcano1")) {
                  baseChance = 508;
               } else if (tile.routeBelongsTo.name.equals("ocean1")) {
                  baseChance = 511;
               } else if (tile.routeBelongsTo.name.equals("wooded_lake1")) {
                  baseChance = 509;
               }

               if (this.rand.nextInt(512) >= baseChance) {
                  String name = tile.routeBelongsTo.allowedPokemon().get(this.rand.nextInt(tile.routeBelongsTo.allowedPokemon().size()));
                  int level = tile.routeBelongsTo.level + Game.rand.nextInt(3);
                  Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
                  String evolveTo = null;
                  int timesEvolved = 0;
                  boolean failed = false;

                  while (!failed) {
                     failed = true;
                     Map<String, String> evos = Specie.gen2Evos.get(pokemon.specie.name);

                     for (String evo : evos.keySet()) {
                        try {
                           int evoLevel = Integer.valueOf(evo);
                           if (evoLevel <= pokemon.level + 10 * (timesEvolved + 1) && Game.rand.nextInt(256) >= 128) {
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
                     pokemon.level += 10 * timesEvolved;
                     pokemon.exp = pokemon.calcExpForLevel(pokemon.level);
                     pokemon.getCurrentAttacks();
                     pokemon.calcMaxStats();
                     pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                  }

                  if (Pokemon.baseSpecies.get(pokemon.specie.name.toLowerCase(Locale.ROOT)) == null) {
                     System.out.println("Fix:");
                     System.out.println(pokemon.specie.name);
                  }

                  boolean isBaseSpecies = Pokemon.baseSpecies.get(pokemon.specie.name.toLowerCase(Locale.ROOT)).equalsIgnoreCase(pokemon.specie.name);
                  if (isBaseSpecies) {
                     if (pokemon.level > 10) {
                        pokemon.level = 10;
                        pokemon.exp = pokemon.calcExpForLevel(pokemon.level);
                        pokemon.getCurrentAttacks();
                        pokemon.calcMaxStats();
                        pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                     }
                  } else if (Pokemon.dontAggro.contains(pokemon.specie.name) && pokemon.level > 30) {
                     pokemon.level = 30;
                     pokemon.exp = pokemon.calcExpForLevel(pokemon.level);
                     pokemon.getCurrentAttacks();
                     pokemon.calcMaxStats();
                     pokemon.currentStats.put("hp", pokemon.maxStats.get("hp"));
                  }

                  boolean hasEvo = !Specie.gen2Evos.get(pokemon.specie.name).isEmpty();
                  boolean requirementsMet = !hasEvo;
                  if (tile.routeBelongsTo.name.contains("beach")) {
                     requirementsMet = hasEvo;
                  }

                  if (tile.routeBelongsTo.name.contains("ruins")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.contains("graveyard")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("beach2")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("beach2_water")) {
                     requirementsMet = tile.isWater;
                  }

                  if (tile.routeBelongsTo.name.equals("beach2_rocks")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("beach2_plateau")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("savanna2")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("mountain1_water1")) {
                     requirementsMet = tile.isWater;
                  }

                  if (tile.routeBelongsTo.name.equals("volcano1")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("wooded_lake1")) {
                     requirementsMet = true;
                  }

                  if (tile.routeBelongsTo.name.equals("wooded_lake_water1")) {
                     requirementsMet = tile.isWater;
                  } else if (tile.routeBelongsTo.name.contains("mountain")) {
                     requirementsMet = !hasEvo
                        || pokemon.specie.name.equals("machoke")
                        || pokemon.specie.name.equals("rhydon")
                        || pokemon.specie.name.equals("onix");
                  }

                  if (tile.routeBelongsTo.name.contains("snow")) {
                     requirementsMet = !hasEvo || pokemon.specie.name.equals("sneasel") || pokemon.specie.name.equals("piloswine");
                  }

                  if (requirementsMet) {
                     pokemon.position = tile.position.cpy();
                     pokemon.mapTiles = game.map.overworldTiles;
                     pokemon.standingAction = pokemon.new Standing();
                     this.pokemonToAdd.put(tile.position.cpy(), pokemon);
                     if (pokemon.specie.name.equals("froslass")) {
                        pokemon.aggroPlayer = true;
                        pokemon.gender = "female";
                     } else if (pokemon.specie.name.equals("sharpedo")) {
                        pokemon.aggroPlayer = true;
                     } else if (pokemon.specie.name.equals("primeape")) {
                        pokemon.aggroPlayer = true;
                     } else if (pokemon.specie.name.equals("gallade")) {
                        pokemon.gender = "male";
                     }
                  }
               }
            } else if (this.rand.nextInt(512) >= 511) {
               String name = tile.routeBelongsTo.allowedPokemon().get(this.rand.nextInt(tile.routeBelongsTo.allowedPokemon().size()));
               int level = tile.routeBelongsTo.level + Game.rand.nextInt(3);
               if (name.equals("numel") || name.equals("kangaskhan") || name.equals("cubone")) {
                  level = 10;
               }

               Pokemon pokemon = new Pokemon(name, level, Pokemon.Generation.CRYSTAL);
               pokemon.position = tile.position.cpy();
               pokemon.mapTiles = game.map.overworldTiles;
               pokemon.standingAction = pokemon.new Standing();
               this.pokemonToAdd.put(tile.position.cpy(), pokemon);
               if (pokemon.specie.name.equals("drapion")) {
                  pokemon.aggroPlayer = true;
               }
            }
         }
      }

      ArrayList<TrainerTipsTile> signTiles = new ArrayList<>();

      for (Tile tile : this.tilesToAdd.values()) {
         if (tile.nameUpper.equals("sign1")) {
            signTiles.add((TrainerTipsTile)tile);
         }
      }

      if (signTiles.size() > 0) {
         TrainerTipsTile tile = signTiles.get(game.map.rand.nextInt(signTiles.size()));
         tile.isUnown = true;
      }
   }

   public void AddMtnLayer(
      HashMap<Vector2, Tile> levelTiles,
      HashMap<Tile, Integer> tileLevels,
      HashMap<Vector2, Tile> mtnTiles,
      int newLevel,
      String name,
      Route route,
      boolean iceAllowed
   ) {
      Vector2 pos = new Vector2();

      for (Tile tile : new ArrayList<>(levelTiles.values())) {
         pos.set(tile.position.x + 16.0F, tile.position.y);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x - 16.0F, tile.position.y);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x, tile.position.y + 16.0F);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x, tile.position.y - 16.0F);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x + 16.0F, tile.position.y + 16.0F);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x - 16.0F, tile.position.y + 16.0F);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x - 16.0F, tile.position.y - 16.0F);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         pos.set(tile.position.x + 16.0F, tile.position.y - 16.0F);
         if (!mtnTiles.containsKey(pos)) {
            Tile nextTile = new Tile(name, "", pos.cpy(), true, route);
            nextTile.biome = "mountain";
            levelTiles.put(nextTile.position.cpy(), nextTile);
            tileLevels.put(nextTile, newLevel);
            mtnTiles.put(nextTile.position.cpy(), nextTile);
         }

         levelTiles.remove(tile.position);
         if (!iceAllowed) {
            this.noIceHerePlease.add(tile.position);
         }
      }
   }

   public void ApplyBlotch(Game game, String type, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd) {
      this.ApplyBlotch(game, type, originTile, maxDist, tilesToAdd, 0, true);
   }

   public void ApplyBlotch(Game game, String type, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd, int isMaze, boolean doNext) {
      this.ApplyBlotch(game, type, originTile, maxDist, tilesToAdd, isMaze, doNext, null);
   }

   public void ApplyBlotch(Game game, String type, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd, int isMaze, boolean doNext, Route currRoute) {
      HashMap<Vector2, Tile> edgeTiles = new HashMap<>();
      ArrayList<Tile> prevTiles = new ArrayList<>();
      Tile prevTile = originTile;
      edgeTiles.put(originTile.position.cpy(), originTile);
      HashMap<Vector2, Tile> grassTiles = new HashMap<>();
      if (!type.equals("beach1")) {
         isMaze = 1;
      }

      HashMap<Vector2, Tile> forestMazeTiles = new HashMap<>();
      ArrayList<Vector2> deepForestMazeTiles = new ArrayList<>();
      ArrayList<Vector2> fenceSpawns = new ArrayList<>();
      int newSize = maxDist - this.rand.nextInt((int)Math.ceil(maxDist / 4.0F)) - maxDist / 4;
      if (type.equals("sand_pit1")) {
         newSize = maxDist;
      }

      int doneOasis = maxDist;
      int numDarms = 10;
      Route beachRoute = null;
      Route beachRoutePlateau = null;
      Route beachRouteWater = null;
      Route deepForestRoute = null;
      if (type.equals("island")) {
         beachRoute = new Route("", 2);
         deepForestRoute = new Route("deep_forest", 33);
      } else if (type.equals("beach1")) {
         beachRoute = new Route("beach2_rocks", 22);
         beachRoutePlateau = new Route("beach2_plateau", 22);
         beachRouteWater = new Route("beach2_water", 22);
      } else if (type.equals("mtn_green1")) {
         beachRoute = new Route("mountain1_water1", 30);
      } else if (type.equals("wooded_lake1")) {
         beachRoute = new Route("wooded_lake_water1", 30);
      }

      while (!edgeTiles.isEmpty()) {
         for (Tile tile : new ArrayList<>(edgeTiles.values())) {
            for (Vector2 edge : new Vector2[]{
               tile.position.cpy().add(-16.0F, 0.0F),
               tile.position.cpy().add(16.0F, 0.0F),
               tile.position.cpy().add(0.0F, 16.0F),
               tile.position.cpy().add(0.0F, -16.0F)
            }) {
               float distance = edge.dst(originTile.position);
               if (!tilesToAdd.containsKey(edge)) {
                  int putTile = this.rand.nextInt(maxDist) + (int)distance;
                  boolean shouldPut = (int)distance < 7 * maxDist / 16;
                  if (type.equals("desert")) {
                     shouldPut = (int)distance < 10 * maxDist / 16;
                  } else if (type.equals("sand_pit1")) {
                     shouldPut = (int)distance < 10 * maxDist / 16;
                  } else if (type.equals("graveyard1")) {
                     shouldPut = (int)distance < 10 * maxDist / 16;
                  } else if (type.equals("beach1")) {
                     shouldPut = (int)distance < 12 * maxDist / 16;
                  } else if (type.contains("volcano")) {
                     shouldPut = (int)distance < 10 * maxDist / 16;
                  } else if (type.contains("island")) {
                     shouldPut = (int)distance < 8 * maxDist / 16;
                  } else if (type.equals("wooded_lake1")) {
                     shouldPut = (int)distance < 12 * maxDist / 16;
                  }

                  Tile newTile = null;
                  if (putTile < maxDist || shouldPut) {
                     if (type.equals("desert")) {
                        newTile = new Tile("desert4", edge, true, currRoute);
                        newTile.biome = "desert";
                        if (this.rand.nextInt(maxDist) < 6) {
                           int nextSize = 200;
                           this.ApplyBlotch(game, "desert_cacti2", newTile, nextSize, grassTiles, 0, false, currRoute);
                           nextSize = this.rand.nextInt(40) + 20;
                           HashMap<Vector2, Tile> newTiles = new HashMap<>();
                           this.ApplyBlotch(game, "desert_cacti1", newTile, nextSize, newTiles, 0, false, currRoute);
                           grassTiles.putAll(newTiles);
                        } else if (this.rand.nextInt(maxDist) < 3) {
                           int nextSize = 30 + this.rand.nextInt(20);
                           HashMap<Vector2, Tile> newTiles = new HashMap<>();
                           this.ApplyBlotch(game, "desert_cacti3", newTile, nextSize, newTiles, 0, false, currRoute);
                           grassTiles.putAll(newTiles);
                        } else if (this.rand.nextInt(maxDist) < 3) {
                           newTile = new Tile("desert4", "cactus10", edge, true, currRoute);
                        } else if (this.rand.nextInt(maxDist) < 3) {
                           newTile = new Tile("desert4", "desert4_cracked", edge, true, currRoute);
                        } else if (this.rand.nextInt(maxDist) < 3) {
                           numSandPits = 0;
                           int nextSize = 45;
                           newTile = new Tile("desert2_trapinch_spawn", edge, true, currRoute);
                           HashMap<Vector2, Tile> newTiles = new HashMap<>();
                           Route tempRoute = new Route("sand_pit1", 44);
                           this.ApplyBlotch(game, "sand_pit1", newTile, nextSize, newTiles, 0, true, tempRoute);
                           grassTiles.putAll(newTiles);
                        } else {
                           float scale = 30.0F;
                           Vector2 rotate = edge.cpy().rotateDeg(45.0F);
                           float offsetY = Math.floorMod((int)(rotate.y / scale), 14) - 7;
                           float offsetX = Math.floorMod((int)(rotate.x / scale - rotate.y / scale), 14) - 7;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           float offset1 = offsetX + offsetY;
                           offsetY = Math.floorMod((int)(rotate.y / scale), 20) - 10;
                           offsetX = Math.floorMod((int)(rotate.x / scale), 20) - 10;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           float offset2 = offsetX + offsetY;
                           offset2 /= 5.0F;
                           if (offset1 * offset2 > 45.0F) {
                              String nameUpper = "";
                              if (this.rand.nextInt(10) < 5) {
                                 nameUpper = "berrytree_rawst_full";
                              }

                              newTile = new Tile("desert4", nameUpper, edge, true, currRoute);
                           }
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                        if (this.doneDesert < 2) {
                           this.doneDesert = 2;
                        }

                        if (doneOasis > 0) {
                           doneOasis--;
                        }
                     } else if (type.equals("island")) {
                        newTile = new Tile("sand1", edge, true, beachRoute);
                        int isRock = this.rand.nextInt(maxDist) + (int)distance;
                        if (isRock > maxDist + maxDist / 2) {
                           Route tempRoute = null;
                           int randInt = this.rand.nextInt(3);
                           if (randInt == 2) {
                              tempRoute = new Route("", 11);
                              String[] pokemon = new String[]{"shellder", "krabby", "staryu", "dwebble"};
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 11 + this.rand.nextInt(3)));
                           }

                           newTile = new Tile("rock1", edge, true, tempRoute);
                        }

                        int isGrass = this.rand.nextInt(maxDist / 8) + (int)distance;
                        if (distance < 8 * maxDist / 16) {
                           if ((int)distance < 3 * maxDist / 8) {
                              if (this.rand.nextInt(24) == 0) {
                                 newTile = new Tile("flower4", edge, true, currRoute);
                              } else {
                                 newTile = new Tile("green1", edge, true, currRoute);
                              }
                           } else if (distance > 15 * maxDist / 32) {
                              Vector2 rotate = edge.cpy().rotateDeg(-15.0F);
                              int offsetY = (int)Math.abs(rotate.y / 8.0F) % 14 - 7;
                              int offsetX = ((int)Math.abs(rotate.x / 8.0F) - offsetY * 2) % 14 - 7;
                              offsetX = Math.abs(offsetX);
                              offsetY = Math.abs(offsetY);
                              int offset1 = offsetX + offsetY;
                              offsetY = Math.abs((int)Math.abs(rotate.y / 8.0F) % 20 - 10);
                              offsetX = Math.abs((int)Math.abs(rotate.x / 8.0F) % 20 - 10);
                              int offset2 = offsetX + offsetY;
                              offset2 /= 5;
                              if (offset1 * offset2 > 6) {
                                 newTile = new Tile("green1", edge, true, currRoute);
                              }
                           } else {
                              newTile = new Tile("green1", edge, true, currRoute);
                           }

                           float scale = 30.0F;
                           Vector2 rotate = edge.cpy().rotateDeg(45.0F);
                           float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                           float offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           float offset1 = offsetX + offsetY;
                           offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                           offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                           float offset2 = offsetX + offsetY;
                           offset2 /= 5.0F;
                           if (offset1 * offset2 > 42.0F) {
                              String nameUpper = "";
                              Route tempRoute = currRoute;
                              if (this.rand.nextInt(10) < 5) {
                                 nameUpper = "berrytree_persim_full";
                                 int randInt = this.rand.nextInt(6);
                                 if (randInt == 0) {
                                    tempRoute = new Route("", 22);
                                    String[] pokemon = new String[]{"sudowoodo"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                                 }
                              }

                              newTile = new Tile(newTile.name, nameUpper, edge, true, tempRoute);
                           }
                        } else if (isRock <= maxDist + maxDist / 2 && isGrass < maxDist - maxDist / 4 && this.rand.nextInt(40) == 0) {
                           Route tempRoute = new Route("", 11);
                           int randInt = this.rand.nextInt(5);
                           if (randInt == 4) {
                              tempRoute.storedPokemon.add(new Pokemon("Exeggutor", 10 + this.rand.nextInt(4)));
                           } else if (randInt > 1) {
                              tempRoute.storedPokemon.add(new Pokemon("Exeggcute", 10 + this.rand.nextInt(4)));
                           }

                           if (this.rand.nextInt(2) == 0) {
                              this.ApplyBlotch(game, "grass_sand", newTile, 35, grassTiles, 0, false, new Route("beach1", 3));
                           }

                           if (this.rand.nextInt(10) == 0) {
                              newTile = new TrainerTipsTile(edge, tempRoute, unownCounter++ % 64 == 0, "");
                           } else {
                              newTile = new Tile("tree5", edge, true, tempRoute);
                           }
                        } else if (isRock <= maxDist + maxDist / 2 && isGrass < maxDist - maxDist / 4 && this.rand.nextInt(200) == 0) {
                           newTile = new Tile(newTile.name, "pokeball1", edge, true, currRoute);
                        } else if (isRock <= maxDist + maxDist / 2 && isGrass < maxDist - maxDist / 4 && this.rand.nextInt(450) == 0) {
                           int centerLevel;
                           if (maxDist > 300) {
                              centerLevel = 30;
                           } else {
                              centerLevel = 15;
                           }

                           int level = (int)(centerLevel * (1.0F - distance / (2 * maxDist / 5)));
                           if (level < 4) {
                              level = 4;
                           }

                           Route blotchRoute;
                           if ((int)distance < 2 * maxDist / 8 && maxDist > 600) {
                              blotchRoute = new Route("deep_forest", level);
                           } else if ((int)distance < 3 * maxDist / 8) {
                              blotchRoute = new Route("forest1", level);
                           } else {
                              blotchRoute = new Route("savanna1", level);
                           }

                           Pokemon pokemon = new Pokemon(
                              blotchRoute.allowedPokemon().get(this.rand.nextInt(blotchRoute.allowedPokemon().size())),
                              blotchRoute.level + this.rand.nextInt(3),
                              Pokemon.Generation.CRYSTAL
                           );
                           pokemon.position = edge.cpy();
                           pokemon.mapTiles = game.map.overworldTiles;
                           if (pokemon.specie.name.toLowerCase(Locale.ROOT).equals("ekans")
                              || pokemon.specie.name.toLowerCase(Locale.ROOT).equals("pidgey")
                              || pokemon.specie.name.toLowerCase(Locale.ROOT).equals("spearow")
                              || pokemon.specie.name.toLowerCase(Locale.ROOT).equals("rattata")) {
                              pokemon.happiness = 0;
                           }

                           pokemon.standingAction = pokemon.new Standing();
                           this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);
                           if (GenForest2.mates2.containsKey(pokemon.specie.name)) {
                              String oppGender = pokemon.gender.equals("male") ? "female" : "male";
                              Pokemon mate = new Pokemon(GenForest2.mates2.get(pokemon.specie.name), pokemon.level);
                              mate.gender = oppGender;
                              mate.position = pokemon.position.cpy().add(16.0F, 0.0F);
                              mate.mapTiles = game.map.overworldTiles;
                              mate.standingAction = mate.new Standing();
                              this.pokemonToAdd.put(mate.position.cpy(), mate);
                           }
                        } else {
                           float scale = 30.0F;
                           Vector2 rotate = edge.cpy().rotateDeg(45.0F);
                           float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                           float offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           float offset1 = offsetX + offsetY;
                           offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                           offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                           float offset2 = offsetX + offsetY;
                           offset2 /= 5.0F;
                           if (offset1 * offset2 > 39.0F) {
                              String nameUpper = "";
                              Route tempRoute = currRoute;
                              if (this.rand.nextInt(10) < 5) {
                                 nameUpper = "berrytree_pecha_full";
                                 int randInt = this.rand.nextInt(6);
                                 if (randInt == 0) {
                                    tempRoute = new Route("", 22);
                                    String[] pokemon = new String[]{"sudowoodo"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                                 }
                              }

                              newTile = new Tile(newTile.name, nameUpper, edge, true, tempRoute);
                           }
                        }

                        if (distance < maxDist / 4 && maxDist > 300 && this.rand.nextInt(750) == 0) {
                           int centerLevel;
                           if (maxDist > 300) {
                              centerLevel = 50;
                           } else {
                              centerLevel = 15;
                           }

                           int level = (int)(centerLevel * (1.0F - distance / (2 * maxDist / 5)));
                           if (level < 4) {
                              level = 4;
                           }

                           String name = new ArrayList<>(GenForest2.mates.keySet()).get(Game.rand.nextInt(GenForest2.mates.keySet().size()));
                           Pokemon pokemon = new Pokemon(name, level);
                           pokemon.position = edge.cpy();
                           pokemon.mapTiles = game.map.overworldTiles;
                           pokemon.standingAction = pokemon.new Standing();
                           this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);
                           String oppGender = pokemon.gender.equals("male") ? "female" : "male";
                           Pokemon mate = new Pokemon(GenForest2.mates.get(pokemon.specie.name), pokemon.level);
                           mate.gender = oppGender;
                           mate.position = pokemon.position.cpy().add(16.0F, 0.0F);
                           mate.mapTiles = game.map.overworldTiles;
                           mate.standingAction = mate.new Standing();
                           this.pokemonToAdd.put(mate.position.cpy(), mate);
                        }

                        if (isMaze == 0) {
                           int isTree = this.rand.nextInt(maxDist / 4) + (int)distance;
                           if (isTree < 1 * maxDist / 3 && newTile.position.y % 32.0F == 0.0F) {
                              newTile = new Tile("tree2", edge);
                           }
                        } else if ((int)distance + this.rand.nextInt(maxDist / 14 + 1) < 8 * maxDist / 16) {
                           forestMazeTiles.put(newTile.position.cpy(), newTile);
                        }

                        int grassBlotchHere = this.rand.nextInt(maxDist / 4) + (int)distance;
                        int grassBlotchHere2 = this.rand.nextInt(maxDist);
                        int grassBlotchHere3 = this.rand.nextInt(maxDist);
                        if ((int)distance < maxDist - maxDist / 5
                           && grassBlotchHere < 3 * maxDist / 7
                           && grassBlotchHere2 < maxDist / 4
                           && (maxDist < 300 || grassBlotchHere3 < maxDist / 8)) {
                           int nextSize = (int)Math.ceil(maxDist / 6.0F);
                           if (maxDist > 300) {
                              nextSize = (int)Math.ceil(maxDist / 12.0F);
                           }

                           if (maxDist > 1000) {
                              nextSize = (int)Math.ceil(maxDist / 32.0F);
                           }

                           int centerLevel;
                           if (maxDist > 300) {
                              centerLevel = 30;
                           } else {
                              centerLevel = 15;
                           }

                           int level = (int)(centerLevel * (1.0F - distance / (2 * maxDist / 5)));
                           if (level < 4) {
                              level = 4;
                           }

                           Route blotchRoute;
                           if ((int)distance < 2 * maxDist / 8 && maxDist > 600) {
                              blotchRoute = deepForestRoute;
                           } else if ((int)distance < 4 * maxDist / 8) {
                              blotchRoute = new Route("forest1", level);
                           } else {
                              blotchRoute = new Route("savanna1", level);
                           }

                           this.ApplyBlotch(game, "grass", newTile, nextSize, grassTiles, 0, false, blotchRoute);
                        }

                        if ((int)distance < 2 * maxDist / 8 && maxDist > 300) {
                           newTile.biome = "deep_forest";
                           deepForestMazeTiles.add(newTile.position.cpy());
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("grass")) {
                        String name = "grass2";
                        if (currRoute.name.contains("savanna") || currRoute.name.contains("oasis")) {
                           name = "grass4";
                        }

                        newTile = new Tile(name, edge, false, currRoute);
                        if ((int)distance < 3 * maxDist / 8 && maxDist > 300) {
                           newTile.biome = "deep_forest";
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("grass_sand")) {
                        newTile = new Tile("grass_sand1", edge, false, currRoute);
                        newTile.biome = originTile.biome;
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("grass_desert1")) {
                        newTile = new Tile("desert4", edge);
                        if (distance < 2 * maxDist / 3) {
                           newTile = new Tile("grass_sand3", edge, true, currRoute);
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.contains("grass_")) {
                        newTile = new Tile(type, edge, false, currRoute);
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("sand_pit1")) {
                        newTile = new Tile("desert4", edge);
                        if (distance < 2 * maxDist / 3) {
                           newTile = new Tile("desert2", edge, true, currRoute);
                           if (this.rand.nextInt(256) < 8) {
                              this.trapinchSpawns.add(edge.cpy());
                           }
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("ruins1_upper")) {
                        newTile = new Tile("desert4", edge);
                        if (distance < 2 * maxDist / 5) {
                           int putTile2 = this.rand.nextInt(maxDist) + (int)distance;
                           if (putTile2 < maxDist) {
                              newTile = new Tile("ruins1", edge, true, currRoute);
                           } else if (edge.y % 32.0F == 0.0F && edge.x % 64.0F == 0.0F) {
                              newTile = new Tile("ruins1_pillar1", edge, true, currRoute);
                           }
                        } else if (distance < 3 * maxDist / 5) {
                           if (this.rand.nextInt(20) > 10 && edge.y % 32.0F == 0.0F && edge.x % 64.0F == 0.0F) {
                              if (this.rand.nextInt(20) > 9) {
                                 newTile = new Tile("ruins1_pillar1_broken", edge, true, currRoute);
                              } else {
                                 newTile = new Tile("ruins1_pillar1", edge, true, currRoute);
                              }
                           } else if (this.rand.nextInt(20) > numDarms && edge.y % 32.0F == 0.0F && edge.x % 32.0F == 0.0F) {
                              Pokemon darmanitan = new Pokemon("darmanitanzen", 35, Pokemon.Generation.CRYSTAL);
                              darmanitan.position = newTile.position.cpy();
                              darmanitan.mapTiles = game.map.overworldTiles;
                              darmanitan.standingAction = darmanitan.new Standing();
                              this.pokemonToAdd.put(darmanitan.position.cpy(), darmanitan);
                              numDarms++;
                           }
                        } else if (this.rand.nextInt(10) > 4) {
                           int nextSize = 95;
                           newTile = new Tile("grass_sand3", edge, true, currRoute);
                           HashMap<Vector2, Tile> newTiles = new HashMap<>();
                           this.ApplyBlotch(game, "grass_desert1", newTile, nextSize, newTiles, 0, true, currRoute);
                           grassTiles.putAll(newTiles);
                           if (this.rand.nextInt(2) > 0) {
                              newTile = new Tile("grass_sand3", edge, true, currRoute);
                              newTiles = new HashMap<>();
                              if (this.rand.nextInt(2) > 0) {
                                 nextSize = this.rand.nextInt(40) + 20;
                                 this.ApplyBlotch(game, "desert_cacti1", newTile, nextSize, newTiles, 0, true, currRoute);
                              } else {
                                 nextSize = 30 + this.rand.nextInt(20);
                                 this.ApplyBlotch(game, "desert_cacti3", newTile, nextSize, newTiles, 0, false, currRoute);
                              }

                              grassTiles.putAll(newTiles);
                           }
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("ruins1_upper2")) {
                        newTile = new Tile("desert4", edge);
                        if (this.rand.nextInt(3) > 0) {
                           newTile = new Tile("ruins1_NSEW", edge, true, currRoute);
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("pond1")) {
                        newTile = new Tile("water2", edge, true, currRoute);
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("oasis1")) {
                        newTile = new Tile("sand1", "", edge, true, currRoute);
                        int isGrass = this.rand.nextInt(maxDist / 8) + (int)distance;
                        if (isGrass < 1 * maxDist / 2) {
                           if ((int)distance < 3 * maxDist / 8) {
                              if (this.rand.nextInt(24) == 0) {
                                 newTile = new Tile("flower4", edge, true, currRoute);
                              } else {
                                 newTile = new Tile("green1", edge, true, currRoute);
                              }
                           } else {
                              newTile = new Tile("green1", edge, true, currRoute);
                           }
                        }

                        boolean putGrass = this.rand.nextInt(90) < 1;
                        if (distance < maxDist / 2) {
                           putGrass = this.rand.nextInt(40) < 1;
                        }

                        boolean putRock = false;
                        if (distance > maxDist / 2) {
                           putRock = this.rand.nextInt(10) < 1;
                        }

                        if (putGrass) {
                           int nextSize = 40;
                           Route blotchRoute = new Route("oasis1", 30);
                           this.ApplyBlotch(game, "grass", newTile, nextSize, grassTiles, 0, false, blotchRoute);
                        } else if (this.rand.nextInt(10) < 1) {
                           Route tempRoute = new Route("", 22);
                           tempRoute.name = "oasis1";
                           int randInt = this.rand.nextInt(2);
                           if (randInt == 0) {
                              String[] pokemon = new String[]{"aexeggutor"};
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level, Pokemon.Generation.CRYSTAL));
                           }

                           newTile = new Tile("tree5", edge, true, tempRoute);
                        } else if (this.rand.nextInt(110) < 1) {
                           newTile = new Tile("green1", "aloe_large1", edge, true, currRoute);
                        } else if (putRock) {
                           Route tempRoute = new Route("", 22);
                           tempRoute.name = "oasis1";
                           int randInt = this.rand.nextInt(2);
                           if (randInt == 0) {
                              String[] pokemon = new String[]{"shellder", "krabby", "staryu", "dwebble"};
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level, Pokemon.Generation.CRYSTAL));
                           }

                           newTile = new Tile(newTile.name, "rock1_color", edge.cpy(), true, tempRoute);
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("desert_cacti1")) {
                        newTile = new Tile("desert4", edge);
                        boolean doCactus = true;
                        if (distance > 0.0F) {
                           doCactus = this.rand.nextInt((int)distance) < maxDist / 8;
                        }

                        if (doCactus) {
                           Route tempRoute = null;
                           int randInt = this.rand.nextInt(2);
                           if (randInt == 0) {
                              String[] pokemon = new String[]{"maractus"};
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute = new Route("", 22);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level, Pokemon.Generation.CRYSTAL));
                           }

                           if (distance < maxDist / 4) {
                              newTile = new Tile("cactus2", edge, true, tempRoute);
                           } else if (newTile.position.x != originTile.position.x || newTile.position.y != originTile.position.y + 16.0F) {
                              newTile = new Tile("cactus1", edge, true, tempRoute);
                           }
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("desert_cacti3")) {
                        newTile = new Tile("desert4", edge, true, currRoute);
                        boolean doCactus = true;
                        if (distance > 0.0F) {
                           doCactus = this.rand.nextInt((int)distance) < maxDist / 8;
                        }

                        if (doCactus) {
                           Route tempRoute = null;
                           if (distance < maxDist / 4) {
                              newTile = new Tile("desert6", "cactus9", edge, true, tempRoute);
                           } else {
                              newTile = new Tile("desert6", "cactus8", edge, true, tempRoute);
                           }
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("desert_cacti4")) {
                        newTile = new Tile("desert4", edge, true, currRoute);
                        if (this.rand.nextInt((int)(maxDist - distance)) < maxDist) {
                           Route tempRoute = null;
                           if (distance < maxDist / 4) {
                              if (this.rand.nextInt(2) == 1) {
                                 newTile = new Tile("desert6", "cactus7", edge, true, tempRoute);
                              }
                           } else {
                              newTile = new Tile("desert6", "cactus7", edge, true, tempRoute);
                           }
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("desert_cacti2")) {
                        newTile = new Tile("desert4", edge, true, currRoute);
                        if (!(distance < 2 * maxDist / 7) && distance < 5 * maxDist / 7) {
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("beach1")) {
                        String name = "sand4_tidalwater";
                        String nameUpper = "";
                        Route tileRoute = currRoute;
                        if (distance > 5 * maxDist / 9) {
                           name = "water2";
                           tileRoute = beachRouteWater;
                        }

                        int scale = 16;
                        Vector2 rotate = edge.cpy().rotateDeg(45.0F);
                        int offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        int offsetX = (int)Math.abs(rotate.x / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        int offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        int offset2 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 60 - 30);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 60 - 30);
                        int offset3 = offsetX + offsetY;
                        offset3 *= 2;
                        if (offset1 * (offset2 / 5) * offset3 <= 320) {
                           name = "mountain4";
                           if (offset1 < 2) {
                              name = "water2";
                           } else if (offset2 < 7) {
                              nameUpper = "stalagmite1";
                           }

                           if (offset3 >= 30) {
                              tileRoute = beachRoute;
                           } else {
                              tileRoute = beachRoutePlateau;
                           }
                        }

                        rotate = edge.cpy().rotateDeg(45.0F);
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        offset2 /= 5;
                        if (offset1 * offset2 > 16) {
                           name = "water2";
                        } else if (!name.equals("water2") && !name.equals("mountain4") && offset1 * offset2 < 13 && offset1 * offset2 > 9) {
                           name = name + "_puddle1";
                        }

                        if (name.contains("sand4") && nameUpper.equals("")) {
                           if (this.rand.nextInt(50) == 0) {
                              nameUpper = "tree6";
                           } else if (this.rand.nextInt(50) == 0) {
                              name = "rock1";
                           }
                        } else if (name.equals("mountain4") && nameUpper.equals("")) {
                           int rate = 5;
                           if (offset3 < 30) {
                              rate = 20;
                           }

                           if (this.rand.nextInt(rate) == 0) {
                              nameUpper = "rock5";
                           } else if (this.rand.nextInt(rate) == 0) {
                              nameUpper = "rock1_color";
                           }
                        }

                        newTile = new Tile(name, nameUpper, edge, true, tileRoute);
                        newTile.biome = "beach";
                        if (this.rand.nextInt(70) < 1) {
                           int nextSize = 40;
                           this.ApplyBlotch(game, "grass_sand", newTile, nextSize, grassTiles, 0, false, currRoute);
                        }

                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("savanna1")) {
                        String name = "green_savanna1";
                        String nameUpper = "";
                        Route tempRoute = currRoute;
                        int scale = 8;
                        Vector2 rotate = edge.cpy().rotateDeg(-15.0F);
                        int offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        int offsetX = ((int)Math.abs(rotate.x / scale) - offsetY * 2) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        int offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        int offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5) > 6) {
                           name = "green_savanna3";
                           if (this.rand.nextInt(5) == 0) {
                              if (edge.x % 32.0F == 0.0F) {
                                 name = "green_savanna4";
                              } else {
                                 name = "green_savanna5";
                              }
                           }
                        } else if (offset1 * offset2 < 8) {
                           name = "sand1";
                        }

                        int var209 = 12;
                        rotate = edge.cpy().rotate(0.0F);
                        offsetY = Math.abs((int)Math.abs(rotate.y / var209) % 60 - 30);
                        offsetX = Math.abs((int)Math.abs(rotate.y / var209) % 34 - 12);
                        int offset3 = offsetX + offsetY;
                        offset3 /= 2;
                        offsetY = (int)Math.abs(rotate.y / var209) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / var209 - offset3) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / var209) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / var209 - offset3) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5) > 16) {
                           nameUpper = "grass_savanna1";
                        } else if (offset1 * (offset2 / 5) > 14) {
                           nameUpper = "grass_savanna2";
                        } else if (offset1 * offset2 < 1) {
                           nameUpper = "tree_savanna1";
                        } else if (this.rand.nextInt(50) == 0) {
                           nameUpper = "bush_savanna1";
                        } else {
                           var209 = 16;
                           rotate = edge.cpy().rotateDeg(45.0F);
                           offsetY = (int)Math.abs(rotate.y / var209) % 14 - 7;
                           offsetX = (int)Math.abs(rotate.x / var209 - rotate.y / var209) % 14 - 7;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           offset1 = offsetX + offsetY;
                           offsetY = Math.abs((int)Math.abs(rotate.y / var209) % 20 - 10);
                           offsetX = Math.abs((int)Math.abs(rotate.x / var209) % 20 - 10);
                           offset2 = offsetX + offsetY;
                           offset2 /= 5;
                           if (offset1 * offset2 > 36 && this.rand.nextInt(10) < 4) {
                              nameUpper = "berrytree_cheri_full";
                              int randInt = this.rand.nextInt(6);
                              if (randInt == 0) {
                                 tempRoute = new Route("", 22);
                                 String[] pokemon = new String[]{"sudowoodo"};
                                 randInt = this.rand.nextInt(pokemon.length);
                                 tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                              }
                           }
                        }

                        newTile = new Tile(name, nameUpper, edge, true, tempRoute);
                        newTile.biome = "savanna";
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("graveyard1")) {
                        if (!this.riverContains(tile.position)) {
                           String name = "green12";
                           String nameUpper = "";
                           Route tempRoute = currRoute;
                           Vector2 rotate = edge.cpy().rotateDeg(-15.0F);
                           int offsetY = (int)Math.abs(rotate.y / 8.0F) % 14 - 7;
                           int offsetX = ((int)Math.abs(rotate.x / 8.0F) - offsetY * 2) % 14 - 7;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           int offset1 = offsetX + offsetY;
                           offsetY = Math.abs((int)Math.abs(rotate.y / 8.0F) % 20 - 10);
                           offsetX = Math.abs((int)Math.abs(rotate.x / 8.0F) % 20 - 10);
                           int offset2 = offsetX + offsetY;
                           offset2 /= 5;
                           if (offset1 * offset2 > 6) {
                              name = "green11";
                              if (this.rand.nextInt(5) == 0) {
                                 if (this.rand.nextBoolean()) {
                                    name = "green10";
                                 } else {
                                    name = "green9";
                                 }
                              }
                           } else if (offset1 < 2) {
                              if (this.rand.nextInt(256) < 80) {
                                 name = "flower5";
                              }
                           } else if (this.rand.nextInt(15) == 0) {
                              name = "flower5";
                           }

                           if (!name.equals("flower5") && this.rand.nextInt(110) == 0) {
                              int randNum = this.rand.nextInt(3);
                              if (randNum == 0) {
                                 nameUpper = "tree8";
                                 int randInt = this.rand.nextInt(3);
                                 if (randInt == 2) {
                                    tempRoute = new Route("", 30);
                                    String[] pokemon = new String[]{"trevenant"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level + this.rand.nextInt(4)));
                                 }
                              } else if (randNum == 1) {
                                 nameUpper = "berrytree_chesto_full";
                                 int randInt = this.rand.nextInt(6);
                                 if (randInt == 0) {
                                    tempRoute = new Route("", 22);
                                    String[] pokemon = new String[]{"sudowoodo"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                                 }
                              } else {
                                 nameUpper = "tree9";
                                 int randInt = this.rand.nextInt(3);
                                 if (randInt == 2) {
                                    tempRoute = new Route("", 30);
                                    String[] pokemon = new String[]{"phantump"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 10 + this.rand.nextInt(4)));
                                 }
                              }
                           }

                           if (edge.y / 16.0F % 2.0F == (int)(edge.y / 32.0F % 11.0F / 5.0F)
                              && edge.x / 16.0F % 2.0F == (int)((edge.x / 16.0F + edge.y / 2.0F) % 13.0F / 5.0F)) {
                              int scale = 16;
                              rotate = edge.cpy().rotateDeg(0.0F);
                              offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                              offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                              offsetX = Math.abs(offsetX);
                              offsetY = Math.abs(offsetY);
                              offset1 = offsetX + offsetY;
                              offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                              offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                              offset2 = offsetX + offsetY;
                              offset2 /= 5;
                              if (offset1 * offset2 > 9) {
                                 name = "green12";
                                 nameUpper = "gravestone2";
                              }
                           }

                           newTile = new Tile(name, nameUpper, edge, true, tempRoute);
                           boolean putGrass = this.rand.nextInt(90) < 1;
                           if (distance > maxDist / 2) {
                              putGrass = this.rand.nextInt(40) < 1;
                           }

                           if (putGrass) {
                              int nextSize = 40;
                              this.ApplyBlotch(game, "grass_graveyard1", newTile, nextSize, grassTiles, 0, false, currRoute);
                           }

                           newTile.biome = "graveyard";
                           tilesToAdd.put(newTile.position.cpy(), newTile);
                           edgeTiles.put(newTile.position.cpy(), newTile);
                           if (offset1 < 1) {
                              fenceSpawns.add(edge.cpy());
                           }
                        }
                     } else if (type.equals("mtn_snow1")) {
                        String name = "snow1";
                        Route tempRoute = currRoute;
                        name = "snow6";
                        if (edge.x % 32.0F == edge.y % 32.0F) {
                           name = "snow5";
                        }

                        String nameUpper = "";
                        float scale = 8.0F;
                        Vector2 rotate = edge.cpy().rotate(-15.0F);
                        float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        float offsetX = ((int)Math.abs(rotate.x / scale) - offsetY * 2.0F) % 14.0F - 7.0F;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        float offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        float offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5.0F) > 16.0F) {
                           name = "snow2";
                        }

                        scale = 12.0F;
                        rotate = edge.cpy().rotateDeg(0.0F);
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 60 - 30);
                        offsetX = Math.abs((int)Math.abs(rotate.y / scale) % 34 - 12);
                        float offset3 = offsetX + offsetY;
                        offset3 /= 2.0F;
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - offset3) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale - offset3) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5.0F) > 16.0F) {
                           name = "snow3";
                        }

                        if (!this.noIceHerePlease.contains(edge)) {
                           scale = 40.0F;
                           rotate = edge.cpy().add(256.0F, 256.0F).rotate(0.0F);
                           offset3 = (float)Math.sin(rotate.y / (scale * 2.0F));
                           offset3 *= 3.0F;
                           offsetY = Math.abs(Math.abs(rotate.y / scale) % 14.0F - 7.0F);
                           offsetX = Math.abs(Math.abs(rotate.x / scale - offset3) % 14.0F - 7.0F);
                           offset1 = offsetX + offsetY;
                           offsetY = Math.abs(Math.abs(rotate.y / scale) % 20.0F - 10.0F);
                           offsetX = Math.abs(Math.abs(rotate.x / scale - offset3) % 20.0F - 10.0F);
                           offset2 = offsetX + offsetY;
                           if (offset1 * 5.0F * offset2 > 500.0F) {
                              name = "ice2";
                           }
                        }

                        if (name.equals("ice2")) {
                           if (this.rand.nextInt(18) == 0) {
                              nameUpper = "rock_ice2";
                           } else if (this.rand.nextInt(56) == 0) {
                              name = "water6";
                              nameUpper = "ice2_pit1";
                              if (edge.x % 32.0F == 0.0F && this.rand.nextInt(4) == 0) {
                                 nameUpper = "ice2_pit2";
                                 Tile otherTile = new Tile(name, "solid", edge.cpy().add(16.0F, 0.0F), true, currRoute);
                                 otherTile.biome = "tundra";
                                 tilesToAdd.put(otherTile.position.cpy(), otherTile);
                                 otherTile = new Tile(name, "solid", edge.cpy().add(0.0F, 16.0F), true, currRoute);
                                 otherTile.biome = "tundra";
                                 tilesToAdd.put(otherTile.position.cpy(), otherTile);
                                 otherTile = new Tile(name, "solid", edge.cpy().add(16.0F, 16.0F), true, currRoute);
                                 otherTile.biome = "tundra";
                                 tilesToAdd.put(otherTile.position.cpy(), otherTile);
                              }
                           }
                        } else {
                           scale = 12.0F;
                           rotate = edge.cpy().rotateDeg(45.0F);
                           offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                           offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                           offsetX = Math.abs(offsetX);
                           offsetY = Math.abs(offsetY);
                           offset1 = offsetX + offsetY;
                           offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                           offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                           offset2 = offsetX + offsetY;
                           offset2 /= 5.0F;
                           if (offset1 * offset2 > 14.0F) {
                              if (this.rand.nextInt(5) < 3) {
                                 name = "snow2";
                                 nameUpper = "tree4";
                                 int randInt = this.rand.nextInt(3);
                                 if (randInt == 2) {
                                    tempRoute = new Route("", 22);
                                    String[] pokemon = new String[]{"snover"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                                 }
                              }

                              tempRoute.dontSpawnOverworlds = true;
                           } else {
                              scale = 30.0F;
                              rotate = edge.cpy().rotateDeg(45.0F);
                              offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                              offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                              offsetX = Math.abs(offsetX);
                              offsetY = Math.abs(offsetY);
                              offset1 = offsetX + offsetY;
                              offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                              offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                              offset2 = offsetX + offsetY;
                              offset2 /= 5.0F;
                              if (offset1 * offset2 < 1.0F && this.rand.nextInt(10) < 4) {
                                 nameUpper = "berrytree_aspear_full";
                                 int randInt = this.rand.nextInt(6);
                                 if (randInt == 0) {
                                    tempRoute = new Route("", 22);
                                    String[] pokemon = new String[]{"sudowoodo"};
                                    randInt = this.rand.nextInt(pokemon.length);
                                    tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                                 }
                              }
                           }
                        }

                        newTile = new Tile(name, nameUpper, edge, true, tempRoute);
                        newTile.biome = "tundra";
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("volcano1_lava")) {
                        String name = "lava1";
                        String nameUpper = "";
                        float scale = 12.0F;
                        Vector2 rotate = tile.position.cpy().add(this.randOffset).rotate(45.0F);
                        float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        float offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        float offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        float offset2 = offsetX + offsetY;
                        offset2 /= 5.0F;
                        if (offset1 * offset2 > 22.0F) {
                           name = "volcano2";
                        }

                        newTile = new Tile(name, nameUpper, edge, true, currRoute);
                        newTile.biome = "volcano";
                        game.map.adjustSurroundingTiles(newTile, tilesToAdd, true);
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("volcano1")) {
                        String name = "volcano1";
                        String nameUpper = "";
                        Route tempRoute = currRoute;
                        float scale = 12.0F;
                        Vector2 rotate = edge.cpy().add(this.randOffset).rotateDeg(0.0F);
                        float offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 60 - 30);
                        float offsetX = Math.abs((int)Math.abs(rotate.y / scale) % 34 - 12);
                        float offset3 = offsetX + offsetY;
                        offset3 /= 2.0F;
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - offset3) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        float offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale - offset3) % 20 - 10);
                        float offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5.0F) > 16.0F) {
                           nameUpper = "soot1";
                        }

                        scale = 12.0F;
                        rotate = edge.cpy().add(this.randOffset).rotateDeg(45.0F);
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        offset2 /= 5.0F;
                        if (offset1 * offset2 > 14.0F && this.rand.nextInt(5) == 0) {
                           nameUpper = "rock_volcano1";
                        }

                        if (this.rand.nextInt(50) == 0) {
                           nameUpper = "rock1_color";
                           int randInt = this.rand.nextInt(2);
                           if (randInt == 0) {
                              tempRoute = new Route("", 22);
                              String[] pokemon = new String[]{"slugma", "geodude"};
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level + this.rand.nextInt(3)));
                           }
                        }

                        newTile = new Tile(name, nameUpper, edge, true, tempRoute);
                        newTile.biome = "volcano";
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("wooded_lake1")) {
                        String name = "green1";
                        String nameUpper = "";
                        Route tempRoute = currRoute;
                        if (this.rand.nextInt(8) == 0) {
                           if (this.rand.nextBoolean()) {
                              name = "flower4";
                           } else {
                              name = "flower3";
                              if (edge.x % 32.0F == 0.0F) {
                                 name = "flower2";
                              }
                           }
                        }

                        float scale = 12.0F;
                        Vector2 rotate = edge.cpy().rotateDeg(0.0F);
                        float offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 60 - 30);
                        float offsetX = Math.abs((int)Math.abs(rotate.y / scale) % 34 - 12);
                        float offset3 = offsetX + offsetY;
                        offset3 /= 2.0F;
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - offset3) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        float offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale - offset3) % 20 - 10);
                        float offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5.0F) > 16.0F) {
                           name = "grass4";
                        }

                        scale = 30.0F;
                        rotate = edge.cpy().rotateDeg(45.0F);
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        offset2 /= 5.0F;
                        if (offset1 * offset2 > 8.0F) {
                           if (this.rand.nextInt(5) < 4) {
                              name = "green1";
                              nameUpper = "tree2";
                              int randInt = this.rand.nextInt(3);
                              if (randInt == 2) {
                                 tempRoute = new Route("", 22);
                                 String[] pokemon = new String[]{
                                    "pineco",
                                    "aipom",
                                    "kakuna",
                                    "metapod",
                                    "spinarak",
                                    "heracross",
                                    "ledyba",
                                    "hoothoot",
                                    "zubat",
                                    "pidgey",
                                    "spearow",
                                    "forretress",
                                    "applin"
                                 };
                                 randInt = this.rand.nextInt(pokemon.length);
                                 tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                              }
                           }

                           tempRoute.dontSpawnOverworlds = true;
                        }

                        scale = 16.0F;
                        rotate = edge.cpy().rotateDeg(45.0F);
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        offset2 /= 5.0F;
                        if (offset1 * offset2 > 36.0F && this.rand.nextInt(10) < 4) {
                           nameUpper = "berrytree_persim_full";
                           int randInt = this.rand.nextInt(6);
                           if (randInt == 0) {
                              tempRoute = new Route("", 22);
                              String[] pokemon = new String[]{"sudowoodo"};
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                           }
                        }

                        scale = 60.0F;
                        rotate = edge.cpy().rotateDeg(45.0F);
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        offset2 /= 5.0F;
                        if (offset1 * offset2 < 5.0F) {
                           name = "water2";
                           nameUpper = "";
                           tempRoute = beachRoute;
                        }

                        newTile = new Tile(name, nameUpper, edge, true, tempRoute);
                        newTile.biome = "wooded_lake";
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     } else if (type.equals("mtn_green1")) {
                        String name = "green1";
                        String nameUpper = "";
                        Route tempRoute = currRoute;
                        if (this.rand.nextInt(8) == 0) {
                           if (this.rand.nextBoolean()) {
                              name = "flower4";
                           } else {
                              name = "flower3";
                              if (edge.x % 32.0F == 0.0F) {
                                 name = "flower2";
                              }
                           }
                        }

                        float scale = 8.0F;
                        Vector2 rotate = edge.cpy().rotateDeg(-15.0F);
                        float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        float offsetX = ((int)Math.abs(rotate.x / scale) - offsetY * 2.0F) % 14.0F - 7.0F;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        float offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        float offset2 = offsetX + offsetY;
                        scale = 12.0F;
                        rotate = edge.cpy().rotateDeg(0.0F);
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 60 - 30);
                        offsetX = Math.abs((int)Math.abs(rotate.y / scale) % 34 - 12);
                        float offset3 = offsetX + offsetY;
                        offset3 /= 2.0F;
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - offset3) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale - offset3) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        if (offset1 * (offset2 / 5.0F) > 16.0F) {
                           name = "grass2";
                        }

                        scale = 12.0F;
                        rotate = edge.cpy().rotateDeg(45.0F);
                        offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
                        offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
                        offsetX = Math.abs(offsetX);
                        offsetY = Math.abs(offsetY);
                        offset1 = offsetX + offsetY;
                        offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
                        offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
                        offset2 = offsetX + offsetY;
                        offset2 /= 5.0F;
                        if (offset1 * offset2 > 14.0F && this.rand.nextInt(5) < 3) {
                           name = "green1";
                           nameUpper = "tree2";
                           int randInt = this.rand.nextInt(3);
                           if (randInt == 2) {
                              tempRoute = new Route("", 22);
                              String[] pokemon = new String[]{
                                 "pineco",
                                 "aipom",
                                 "kakuna",
                                 "metapod",
                                 "spinarak",
                                 "heracross",
                                 "ledyba",
                                 "hoothoot",
                                 "zubat",
                                 "pidgey",
                                 "spearow",
                                 "forretress",
                                 "applin"
                              };
                              randInt = this.rand.nextInt(pokemon.length);
                              tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 20 + this.rand.nextInt(4)));
                           }
                        }

                        if (!this.noIceHerePlease.contains(edge)) {
                           scale = 40.0F;
                           rotate = edge.cpy().add(256.0F, 256.0F).rotate(0.0F);
                           offset3 = (float)Math.sin(rotate.y / (scale * 2.0F));
                           offset3 *= 3.0F;
                           offsetY = Math.abs(Math.abs(rotate.y / scale) % 14.0F - 7.0F);
                           offsetX = Math.abs(Math.abs(rotate.x / scale - offset3) % 14.0F - 7.0F);
                           offset1 = offsetX + offsetY;
                           offsetY = Math.abs(Math.abs(rotate.y / scale) % 20.0F - 10.0F);
                           offsetX = Math.abs(Math.abs(rotate.x / scale - offset3) % 20.0F - 10.0F);
                           offset2 = offsetX + offsetY;
                           if (offset1 * 5.0F * offset2 > 500.0F) {
                              name = "water2";
                              nameUpper = "";
                              tempRoute = beachRoute;
                           }
                        }

                        newTile = new Tile(name, nameUpper, edge, true, tempRoute);
                        newTile.biome = "mountain";
                        tilesToAdd.put(newTile.position.cpy(), newTile);
                        edgeTiles.put(newTile.position.cpy(), newTile);
                     }
                  }
               }
            }

            if (tile.position.dst(originTile.position) > prevTile.position.dst(originTile.position)
               && !tile.name.equals("sand1")
               && !tile.name.equals("rock1")
               && !tile.name.equals("tree5")) {
               prevTile = tile;
               prevTiles.add(0, tile);
            }

            edgeTiles.remove(tile.position);
         }
      }

      for (Tile tile : grassTiles.values()) {
         if (tilesToAdd.containsKey(tile.position) && !tilesToAdd.get(tile.position).isSolid) {
            tilesToAdd.put(tile.position.cpy(), tile);
         }
      }

      if (isMaze != 0 && forestMazeTiles.size() > 0) {
         Vector2 bl = null;
         Vector2 tr = null;

         for (Tile tile : forestMazeTiles.values()) {
            if (bl == null) {
               bl = tile.position.cpy();
            }

            if (tr == null) {
               tr = tile.position.cpy();
            }

            if (tile.position.x < bl.x) {
               bl.x = tile.position.x;
            } else if (tile.position.x > tr.x) {
               tr.x = tile.position.x;
            }

            if (tile.position.y < bl.y) {
               bl.y = tile.position.y;
            } else if (tile.position.y > tr.y) {
               tr.y = tile.position.y;
            }
         }

         tr.x = tr.x - Math.floorMod((int)tr.x, 96);
         bl.x = bl.x - Math.floorMod((int)bl.x, 96);
         tr.y = tr.y + (96 - Math.floorMod((int)tr.y, 96));
         bl.y = bl.y + (96 - Math.floorMod((int)bl.y, 96));
         float density = 0.1F;
         float complexity = 0.9F;
         int squareSize = 96;
         HashMap<Vector2, MazeNode> nodes = GenForest2.Maze_Algo1(
            (int)(tr.x - bl.x) * 2 / squareSize, (int)(tr.y - bl.y) * 2 / squareSize, density, complexity, squareSize, this.rand
         );

         for (MazeNode node : nodes.values()) {
            ArrayList<Tile> tileSquare;
            if (node.type == "platform1") {
               tileSquare = GenForest2.getTileSquarePlatform1(node, bl, this.rand);
            } else {
               tileSquare = GenForest2.getTileSquare(node, bl, this.rand, true);
            }

            for (Tile tile : tileSquare) {
               if (forestMazeTiles.containsKey(tile.position)) {
                  tilesToAdd.put(tile.position.cpy(), tile);
               }
            }
         }

         if (tilesToAdd.size() > 0) {
            Action temp = new GenForest2.ApplyForestBiome(tilesToAdd, bl.cpy(), tr.cpy(), true, null);
            temp.step(game);
         }

         for (Tile tile : new ArrayList<>(tilesToAdd.values())) {
            if ((tile.name.equals("ledge_grass_down") || tile.name.equals("ledge_ramp_down"))
               && tilesToAdd.containsKey(tile.position.cpy().add(16.0F, 0.0F))
               && !tilesToAdd.get(tile.position.cpy().add(16.0F, 0.0F)).isSolid
               && !tilesToAdd.get(tile.position.cpy().add(16.0F, 0.0F)).isLedge) {
               HashMap<Vector2, Tile> upTiles = new HashMap<>();
               HashMap<Vector2, Tile> rightTiles = new HashMap<>();
               upTiles.put(tile.position.cpy().add(16.0F, 0.0F), new Tile("ledge1_corner_br", tile.position.cpy().add(16.0F, 0.0F), true));
               rightTiles.put(tile.position.cpy().add(16.0F, 0.0F), new Tile("ledge_grass_down", tile.position.cpy().add(16.0F, 0.0F), true));
               int i = 16;

               while (i < 80) {
                  if (!tilesToAdd.containsKey(tile.position.cpy().add(16.0F, i))
                     || !tilesToAdd.get(tile.position.cpy().add(16.0F, i)).isSolid && !tilesToAdd.get(tile.position.cpy().add(16.0F, i)).isLedge) {
                     if (!tilesToAdd.containsKey(tile.position.cpy().add(16 + i, 0.0F))
                        || !tilesToAdd.get(tile.position.cpy().add(16 + i, 0.0F)).isSolid && !tilesToAdd.get(tile.position.cpy().add(16 + i, 0.0F)).isLedge) {
                        upTiles.put(tile.position.cpy().add(16.0F, i), new Tile("ledge1_right", tile.position.cpy().add(16.0F, i), true));
                        rightTiles.put(tile.position.cpy().add(16 + i, 0.0F), new Tile("ledge_grass_down", tile.position.cpy().add(16 + i, 0.0F), true));
                        i += 16;
                        continue;
                     }

                     tilesToAdd.putAll(rightTiles);
                     break;
                  }

                  tilesToAdd.putAll(upTiles);
                  break;
               }
            }

            if ((tile.name.equals("ledge_grass_down") || tile.name.equals("ledge_ramp_down"))
               && tilesToAdd.containsKey(tile.position.cpy().add(-16.0F, 0.0F))
               && !tilesToAdd.get(tile.position.cpy().add(-16.0F, 0.0F)).isSolid
               && !tilesToAdd.get(tile.position.cpy().add(-16.0F, 0.0F)).isLedge) {
               HashMap<Vector2, Tile> upTiles = new HashMap<>();
               HashMap<Vector2, Tile> leftTiles = new HashMap<>();
               upTiles.put(tile.position.cpy().add(-16.0F, 0.0F), new Tile("ledge1_corner_bl", tile.position.cpy().add(-16.0F, 0.0F), true));
               leftTiles.put(tile.position.cpy().add(-16.0F, 0.0F), new Tile("ledge_grass_down", tile.position.cpy().add(-16.0F, 0.0F), true));
               int i = 16;

               while (i < 80) {
                  if (!tilesToAdd.containsKey(tile.position.cpy().add(-16.0F, i))
                     || !tilesToAdd.get(tile.position.cpy().add(-16.0F, i)).isSolid && !tilesToAdd.get(tile.position.cpy().add(-16.0F, i)).isLedge) {
                     if (!tilesToAdd.containsKey(tile.position.cpy().add(-16 - i, 0.0F))
                        || !tilesToAdd.get(tile.position.cpy().add(-16 - i, 0.0F)).isSolid && !tilesToAdd.get(tile.position.cpy().add(-16 - i, 0.0F)).isLedge) {
                        upTiles.put(tile.position.cpy().add(-16.0F, i), new Tile("ledge1_left", tile.position.cpy().add(-16.0F, i), true));
                        leftTiles.put(tile.position.cpy().add(-16 - i, 0.0F), new Tile("ledge_grass_down", tile.position.cpy().add(-16 - i, 0.0F), true));
                        i += 16;
                        continue;
                     }

                     tilesToAdd.putAll(leftTiles);
                     break;
                  }

                  tilesToAdd.putAll(upTiles);
                  break;
               }
            }

            if (deepForestMazeTiles.contains(tile.position)) {
               tile.routeBelongsTo = deepForestRoute;
               tile.biome = "deep_forest";
               if (tile.name.equals("grass2")) {
                  tile.name = "grass4";
               }

               tile.init();
            }
         }
      }

      if (type.equals("graveyard1")) {
         for (int numFences = this.rand.nextInt(4) + 1; numFences > 0 && !fenceSpawns.isEmpty(); numFences--) {
            boolean doneDoll = false;
            Vector2 edge = fenceSpawns.remove(this.rand.nextInt(fenceSpawns.size()));
            int size = 4 + this.rand.nextInt(3);
            int left = this.rand.nextInt(size);
            int doorOffset = this.rand.nextInt(2) * 16;
            int numDoors = this.rand.nextInt(4) + 1;
            Vector2 bl = edge.cpy().add(-16 * left, -16 * left);
            bl.x = bl.x + bl.x % 32.0F;
            bl.y = bl.y + (bl.y / 16.0F % 2.0F + (int)(bl.y / 32.0F % 11.0F / 5.0F)) * 16.0F;
            Vector2 tr = bl.cpy().add(16 * size, 16 * size);
            tr.x = tr.x + tr.x % 32.0F;
            tr.y = tr.y + (tr.y / 16.0F % 2.0F + (int)(tr.y / 32.0F % 11.0F / 5.0F)) * 16.0F;
            Vector2 currPos = bl.cpy();

            while (currPos.y <= tr.y) {
               String name = "flower5";
               String nameUpper = "";
               boolean replace = true;
               if (currPos.x == bl.x && currPos.y == bl.y) {
                  name = "green12";
                  nameUpper = "fence1_NE";
               } else if (currPos.x == tr.x && currPos.y == tr.y) {
                  name = "green12";
                  nameUpper = "fence1_SW";
               } else if (currPos.x == bl.x && currPos.y == tr.y) {
                  name = "green12";
                  nameUpper = "fence1_SE";
               } else if (currPos.x == tr.x && currPos.y == bl.y) {
                  name = "green12";
                  nameUpper = "fence1_NW";
               } else if (currPos.y == bl.y || currPos.y == tr.y) {
                  name = "green12";
                  nameUpper = "fence1";
                  if (numDoors > 0 && currPos.x == bl.x + size / 2 * 16 + doorOffset) {
                     nameUpper = "house5_door1";
                     numDoors--;
                  }
               } else if (currPos.x != bl.x && currPos.x != tr.x) {
                  if (tilesToAdd.get(currPos) != null) {
                     if (!tilesToAdd.get(currPos).nameUpper.equals("") && currPos.y != bl.y + 16.0F) {
                        replace = false;
                     }

                     if (tilesToAdd.get(currPos).nameUpper.contains("fence")) {
                        replace = true;
                     }
                  }

                  if (replace && currPos.y % 32.0F == 0.0F && currPos.x != bl.x + 16.0F && currPos.x != tr.x - 16.0F && currPos.y != bl.y + 16.0F) {
                     this.keystoneSpawns.add(currPos.cpy());
                     if (this.rand.nextInt(3) == 0) {
                        name = "green12";
                        nameUpper = "gravestone2";
                     }

                     if (!doneDoll && (size <= 5 || this.rand.nextInt(3) == 0)) {
                        doneDoll = true;
                        int randInt = this.rand.nextInt(3);
                        nameUpper = "pokedoll1";
                        if (randInt == 0) {
                           nameUpper = "pokedoll1_mimikyu";
                        } else if (randInt == 1) {
                           nameUpper = "pokedoll1_banette";
                        }
                     }
                  }
               } else {
                  name = "green12";
                  nameUpper = "fence1_NS";
                  if (this.rand.nextBoolean() && numDoors > 0 && currPos.y == bl.y + size / 2 * 16 + doorOffset) {
                     nameUpper = "house5_door1";
                     numDoors--;
                  }
               }

               if (replace) {
                  Tile newTile = new Tile(name, nameUpper, currPos.cpy(), true, currRoute);
                  newTile.biome = "graveyard";
                  tilesToAdd.put(currPos.cpy(), newTile);
                  game.map.adjustSurroundingTiles(newTile, tilesToAdd, true);
               }

               currPos.x += 16.0F;
               if (currPos.x > tr.x) {
                  currPos.x = bl.x;
                  currPos.y += 16.0F;
               }
            }
         }
      }

      if (newSize > 0) {
         int randInt = 1;
         int[] vals = new int[]{1, 1, 2, 3};
         randInt = vals[this.rand.nextInt(vals.length)];
         if (prevTiles.size() < randInt) {
            randInt = prevTiles.size();
         }

         HashMap<Vector2, Tile> nextIslandTiles = new HashMap<>();
         int next = 0;
         if (type.equals("oasis1")) {
            vals = new int[]{3, 3, 4, 4, 5, 5};
            randInt = vals[this.rand.nextInt(vals.length)];
            if (prevTiles.size() < randInt) {
               randInt = prevTiles.size();
            }

            newSize += maxDist / 10;
         } else if (type.equals("sand_pit1")) {
            if (randInt > 1) {
               randInt = 1;
            }

            if (numSandPits >= 20) {
               newSize = 1;
            } else {
               numSandPits++;
            }
         } else if (type.equals("beach1")) {
            newSize = maxDist;
            if (prevTiles.size() > 0 && --isMaze > 0) {
               Tile origin = prevTiles.get(next);
               tilesToAdd.put(origin.position.cpy(), origin);
               this.ApplyBlotch(game, type, origin, newSize, nextIslandTiles, isMaze, true, currRoute);
            }
         }

         if ((type.equals("island") || type.equals("desert") || type.equals("oasis1") || type.equals("sand_pit1") || type.equals("wooded_lake1")) && doNext) {
            for (int i = 0; i < randInt; i++) {
               if (this.doneDesert == 1) {
                  tilesToAdd.put(prevTiles.get(next).position.cpy(), prevTiles.get(next));
                  Route blotchRoute = new Route("desert1", 40);
                  this.ApplyBlotch(game, "desert", prevTiles.get(next), newSize + maxDist / 4, nextIslandTiles, 1, true, blotchRoute);
               } else {
                  tilesToAdd.put(prevTiles.get(next).position.cpy(), prevTiles.get(next));
                  String newType = type;
                  if (type.equals("desert")) {
                     newType = "island";
                     currRoute = new Route("forest1", 40);
                  } else if (!type.equals("wooded_lake1")
                     && this.doneDesert > 1
                     && !newType.equals("sand_pit1")
                     && !newType.equals("oasis1")
                     && this.rand.nextInt(5) == 0) {
                     currRoute = new Route("wooded_lake1", 40);
                     newType = "wooded_lake1";
                     if (newSize < 400) {
                        newSize = 400;
                     }
                  }

                  this.ApplyBlotch(game, newType, prevTiles.get(next), newSize, nextIslandTiles, 1, true, currRoute);
                  next += this.rand.nextInt(prevTiles.size() - next) + next;
                  if (next >= prevTiles.size()) {
                     break;
                  }
               }
            }
         }

         boolean didBeach = false;
         if (prevTiles.size() > 1 && (type.equals("island") || type.equals("wooded_lake1"))) {
            Route blotchRoute = new Route("beach2", 12);
            Tile origin = prevTiles.remove(1);
            this.ApplyBlotch(game, "beach1", origin, 400, nextIslandTiles, 6, false, blotchRoute);
            didBeach = true;
         }

         for (Tile tile : nextIslandTiles.values()) {
            if (tilesToAdd.containsKey(tile.position)) {
               Tile currTile = tilesToAdd.get(tile.position);
               if (type.equals("beach1")) {
                  if (tile.name.contains("sand4") || tile.name.equals("mountain4")) {
                     tilesToAdd.put(tile.position.cpy(), tile);
                  }
               } else if (type.equals("sand_pit1")) {
                  if (tile.name.equals("desert2")) {
                     tilesToAdd.put(tile.position.cpy(), tile);
                  }
               } else if (tile.name.contains("desert")) {
                  tilesToAdd.put(tile.position.cpy(), tile);
               } else if (!currTile.name.contains("desert")) {
                  if (tile.routeBelongsTo != null && tile.routeBelongsTo.name.equals("wooded_lake1")) {
                     tilesToAdd.put(tile.position.cpy(), tile);
                  } else if (!type.equals("wooded_lake1") && type.equals("island")) {
                     if ((
                           currTile.name.equals("sand1")
                              || currTile.name.equals("rock1")
                              || currTile.name.equals("tree5")
                              || currTile.name.equals("grass_sand1")
                        )
                        && !tile.name.equals("sand1")) {
                        tilesToAdd.put(tile.position.cpy(), tile);
                     }

                     if (tile.name.equals("tree_large1") && !forestMazeTiles.containsKey(tile.position)) {
                        tilesToAdd.put(tile.position.cpy(), tile);
                        tilesToAdd.put(tile.position.cpy().add(16.0F, 0.0F), new Tile("tree_large1_noSprite", tile.position.cpy().add(16.0F, 0.0F)));
                        tilesToAdd.put(tile.position.cpy().add(0.0F, 16.0F), new Tile("tree_large1_noSprite", tile.position.cpy().add(0.0F, 16.0F)));
                        tilesToAdd.put(tile.position.cpy().add(16.0F, 16.0F), new Tile("tree_large1_noSprite", tile.position.cpy().add(16.0F, 16.0F)));
                     }

                     if (tile.nameUpper.equals("tree2") && !forestMazeTiles.containsKey(tile.position)) {
                        tilesToAdd.put(tile.position.cpy(), tile);
                     }
                  }
               }
            } else {
               tilesToAdd.put(tile.position.cpy(), tile);
            }
         }
      }
   }

   public ArrayList<Tile> ApplyBlotchMountain(Game game, Tile originTile, int maxDist, HashMap<Vector2, Tile> tilesToAdd) {
      ArrayList<Tile> edgeTiles = new ArrayList<>();
      HashMap<Tile, Vector2> edgeDirs = new HashMap<>();
      HashMap<Vector2, Tile> mtnTiles = new HashMap<>();
      Tile copyTile = new Tile(originTile.name, originTile.position.cpy());
      edgeTiles.add(originTile);
      edgeTiles.add(copyTile);
      Vector2 dir1 = new Vector2(this.rand.nextInt(200) - 100, this.rand.nextInt(200) - 100);
      Vector2 dir2 = dir1.cpy().rotate(180.0F);
      edgeDirs.put(originTile, dir1);
      edgeDirs.put(copyTile, dir2);
      HashMap<Tile, Vector2> origins = new HashMap<>();
      HashMap<Tile, Integer> originDists = new HashMap<>();
      origins.put(originTile, originTile.position.cpy());
      origins.put(copyTile, originTile.position.cpy());
      originDists.put(originTile, maxDist);
      originDists.put(copyTile, maxDist);
      Tile copyTile2 = new Tile(originTile.name, originTile.position.cpy());
      Tile copyTile3 = new Tile(originTile.name, originTile.position.cpy());
      edgeTiles.add(copyTile2);
      edgeTiles.add(copyTile3);
      edgeDirs.put(copyTile2, dir1.cpy().rotateDeg(90.0F));
      edgeDirs.put(copyTile3, dir2.cpy().rotateDeg(90.0F));
      origins.put(copyTile2, originTile.position.cpy());
      origins.put(copyTile3, originTile.position.cpy());
      originDists.put(copyTile2, (int)(2.0F * Math.abs(maxDist) / 8.0F));
      originDists.put(copyTile3, (int)(2.0F * Math.abs(maxDist) / 8.0F));
      ArrayList<Tile> endPoints = new ArrayList<>();
      this.noIceHerePlease.clear();
      int currLevel = 0;
      int newLevel = 0;
      int numLayers = 0;
      int[] levels2 = new int[]{0, 0, -1, -1, -1};
      HashMap<Vector2, Tile> levelTiles = new HashMap<>();
      HashMap<Tile, Integer> tileLevels = new HashMap<>();
      levelTiles.put(originTile.position.cpy(), originTile);
      levelTiles.put(copyTile.position.cpy(), copyTile);
      tileLevels.put(originTile, currLevel);
      tileLevels.put(copyTile, currLevel);
      Route mtnRoute = new Route("mountain1", 33);
      Route snowRoute = new Route("snow1", 44);

      while (!edgeTiles.isEmpty() || currLevel > -Math.ceil(maxDist / 6000) || numLayers < 13) {
         System.out.println(newLevel);

         for (Tile tile : new ArrayList<>(edgeTiles)) {
            edgeTiles.remove(tile);
            Vector2 currDir = edgeDirs.get(tile);
            edgeDirs.remove(tile);
            float distance = tile.position.dst(origins.get(tile));
            int maxDist2 = originDists.get(tile);
            int putTile = this.rand.nextInt(maxDist2 + 1) + (int)distance - maxDist2 / 32;
            if (putTile < maxDist2) {
               Vector2 newDir = new Vector2();
               putTile = this.rand.nextInt(Math.abs((int)currDir.x) + 1) + this.rand.nextInt(Math.abs((int)currDir.y) + 1);
               if (putTile < Math.abs(currDir.y) + 2.0F) {
                  if (currDir.y < 0.0F) {
                     newDir.x = 0.0F;
                     newDir.y = -1.0F;
                  } else {
                     newDir.x = 0.0F;
                     newDir.y = 1.0F;
                  }
               } else if (currDir.x < 0.0F) {
                  newDir.x = -1.0F;
                  newDir.y = 0.0F;
               } else {
                  newDir.x = 1.0F;
                  newDir.y = 0.0F;
               }

               Tile nextTile = new Tile("mountain1", "", tile.position.cpy().add(16.0F * newDir.x, 16.0F * newDir.y), true, mtnRoute);
               mtnTiles.put(nextTile.position.cpy(), nextTile);
               edgeTiles.add(nextTile);
               edgeDirs.put(nextTile, currDir);
               origins.put(nextTile, origins.get(tile));
               originDists.put(nextTile, originDists.get(tile));
               levelTiles.put(nextTile.position.cpy(), nextTile);
               tileLevels.put(nextTile, newLevel);
               if (this.rand.nextInt((int)Math.ceil(1.0F / (4.0F / ((maxDist + maxDist2) / 350.0F)))) == 1) {
                  int degrees = this.rand.nextInt(80) + 10;
                  Vector2 branchDir = currDir.cpy().rotate(degrees);
                  nextTile = new Tile("mountain1", "", tile.position.cpy(), true, mtnRoute);
                  edgeTiles.add(nextTile);
                  edgeDirs.put(nextTile, branchDir);
                  origins.put(nextTile, tile.position.cpy());
                  originDists.put(nextTile, (int)(1.0F * Math.abs(maxDist2 - distance) / 8.0F));
                  branchDir = currDir.cpy().rotate(-degrees);
                  nextTile = new Tile("mountain1", "", tile.position.cpy(), true, mtnRoute);
                  edgeTiles.add(nextTile);
                  edgeDirs.put(nextTile, branchDir);
                  origins.put(nextTile, tile.position.cpy());
                  originDists.put(nextTile, (int)(1.0F * Math.abs(maxDist2 - distance) / 8.0F));
               }
            } else if (distance > 100.0F) {
               System.out.println("distance");
               System.out.println(distance);
               endPoints.add(tile);
            }

            origins.remove(tile);
            originDists.remove(tile);
         }

         if (this.rand.nextInt((int)Math.ceil(1.0F / (1.0F / (maxDist / 1000.0F)))) == 1) {
            if (numLayers < 10) {
               newLevel = currLevel + levels2[this.rand.nextInt(levels2.length)];
            } else if (numLayers >= maxDist / 1800) {
               newLevel--;
            }

            this.AddMtnLayer(
               levelTiles,
               tileLevels,
               mtnTiles,
               newLevel,
               newLevel <= -1 ? "mountain3" : "snow1",
               newLevel <= -1 ? mtnRoute : snowRoute,
               numLayers >= 10 && numLayers < maxDist / 1800
            );
            currLevel = newLevel;
            numLayers++;
         }
      }

      while (numLayers <= maxDist / 1800) {
         this.AddMtnLayer(
            levelTiles,
            tileLevels,
            mtnTiles,
            currLevel,
            currLevel <= -1 ? "mountain3" : "snow1",
            currLevel <= -1 ? mtnRoute : snowRoute,
            numLayers >= 10 && numLayers <= maxDist / 1800
         );
         numLayers++;
      }

      this.AddMtnLayer(levelTiles, tileLevels, mtnTiles, currLevel - 1, "mountain3", mtnRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, mtnTiles, currLevel - 2, "mountain3", mtnRoute, false);
      int bottomLevel = currLevel - 2;
      Route volcanoRoute = new Route("volcano1", 22);
      levelTiles.clear();
      HashMap<Vector2, Tile> volcanoTiles = new HashMap<>();
      this.ApplyBlotch(game, "volcano1", originTile, 350, volcanoTiles, 0, false, volcanoRoute);
      levelTiles.clear();
      this.ApplyBlotch(game, "volcano1_lava", originTile, 200, levelTiles, 0, false, volcanoRoute);
      this.ledgify(levelTiles, levelTiles, "volcano2", "lava1_", "volcano");
      volcanoTiles.putAll(levelTiles);
      this.ledgify(volcanoTiles, volcanoTiles, "volcano1", "lava1_", "volcano", "volcano1");

      for (Tile tile : volcanoTiles.values()) {
         if (tile.nameUpper.equals("soot1")) {
            tile.name = tile.nameUpper;
            tile.nameUpper = "";
            tile.init();
         }
      }

      levelTiles.clear();
      levelTiles.putAll(volcanoTiles);
      currLevel = -5;

      for (Tile tile : levelTiles.values()) {
         tileLevels.put(tile, currLevel);
      }

      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel++, "volcano1", volcanoRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel++, "volcano1", volcanoRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel++, "volcano1", volcanoRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel++, "volcano1", volcanoRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel++, "volcano1", volcanoRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel, "mountain3", volcanoRoute, false);
      this.AddMtnLayer(levelTiles, tileLevels, volcanoTiles, currLevel, "mountain3", volcanoRoute, false);
      mtnTiles.putAll(volcanoTiles);
      HashMap<Vector2, Tile> biomeTiles = new HashMap<>();
      boolean done = false;

      while (!done) {
         done = true;

         for (Tile tile : mtnTiles.values()) {
            Tile bl = mtnTiles.get(tile.position.cpy().add(-16.0F, -16.0F));
            Tile bot = mtnTiles.get(tile.position.cpy().add(0.0F, -16.0F));
            Tile br = mtnTiles.get(tile.position.cpy().add(16.0F, -16.0F));
            Tile left = mtnTiles.get(tile.position.cpy().add(-16.0F, 0.0F));
            Tile tl = mtnTiles.get(tile.position.cpy().add(-16.0F, 16.0F));
            Tile top = mtnTiles.get(tile.position.cpy().add(0.0F, 16.0F));
            Tile tr = mtnTiles.get(tile.position.cpy().add(16.0F, 16.0F));
            Tile right = mtnTiles.get(tile.position.cpy().add(16.0F, 0.0F));
            if ((!tileLevels.containsKey(left) || tileLevels.get(left) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(right) || tileLevels.get(right) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(bot) || tileLevels.get(bot) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(top) || tileLevels.get(top) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(tr) || tileLevels.get(tr) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(tl) || tileLevels.get(tl) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(bl) || tileLevels.get(bl) >= tileLevels.get(tile) - 1)
               && (!tileLevels.containsKey(br) || tileLevels.get(br) >= tileLevels.get(tile) - 1)) {
               if (tileLevels.containsKey(left)
                  && tileLevels.get(left) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(right)
                  && tileLevels.get(right) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(top)
                  && tileLevels.get(top) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(bot)
                  && tileLevels.get(bot) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(top)
                  && tileLevels.get(top) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(left)
                  && tileLevels.get(left) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(br)
                  && tileLevels.get(br) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(top)
                  && tileLevels.get(top) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(right)
                  && tileLevels.get(right) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(bl)
                  && tileLevels.get(bl) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(bot)
                  && tileLevels.get(bot) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(right)
                  && tileLevels.get(right) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(tl)
                  && tileLevels.get(tl) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(bot)
                  && tileLevels.get(bot) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(left)
                  && tileLevels.get(left) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(tr)
                  && tileLevels.get(tr) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(bot)
                  && tileLevels.get(bot) == tileLevels.get(tile)
                  && tileLevels.containsKey(left)
                  && tileLevels.get(left) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(br)
                  && tileLevels.get(br) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(bot)
                  && tileLevels.get(bot) == tileLevels.get(tile)
                  && tileLevels.containsKey(right)
                  && tileLevels.get(right) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(bl)
                  && tileLevels.get(bl) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(top)
                  && tileLevels.get(top) == tileLevels.get(tile)
                  && tileLevels.containsKey(left)
                  && tileLevels.get(left) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(tr)
                  && tileLevels.get(tr) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(top)
                  && tileLevels.get(top) == tileLevels.get(tile)
                  && tileLevels.containsKey(right)
                  && tileLevels.get(right) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(tl)
                  && tileLevels.get(tl) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(right)
                  && tileLevels.get(right) == tileLevels.get(tile)
                  && tileLevels.containsKey(top)
                  && tileLevels.get(top) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(br)
                  && tileLevels.get(br) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(right)
                  && tileLevels.get(right) == tileLevels.get(tile)
                  && tileLevels.containsKey(bot)
                  && tileLevels.get(bot) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(tr)
                  && tileLevels.get(tr) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(left)
                  && tileLevels.get(left) == tileLevels.get(tile)
                  && tileLevels.containsKey(bot)
                  && tileLevels.get(bot) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(tl)
                  && tileLevels.get(tl) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }

               if (tileLevels.containsKey(left)
                  && tileLevels.get(left) == tileLevels.get(tile)
                  && tileLevels.containsKey(top)
                  && tileLevels.get(top) <= tileLevels.get(tile) - 1
                  && tileLevels.containsKey(bl)
                  && tileLevels.get(bl) <= tileLevels.get(tile) - 1) {
                  tileLevels.put(tile, tileLevels.get(tile) - 1);
                  done = false;
               }
            } else {
               tileLevels.put(tile, tileLevels.get(tile) - 1);
               done = false;
            }
         }
      }

      for (Tile tile : mtnTiles.values()) {
         Tile bl = mtnTiles.get(tile.position.cpy().add(-16.0F, -16.0F));
         Tile bot = mtnTiles.get(tile.position.cpy().add(0.0F, -16.0F));
         Tile br = mtnTiles.get(tile.position.cpy().add(16.0F, -16.0F));
         Tile left = mtnTiles.get(tile.position.cpy().add(-16.0F, 0.0F));
         Tile tl = mtnTiles.get(tile.position.cpy().add(-16.0F, 16.0F));
         Tile top = mtnTiles.get(tile.position.cpy().add(0.0F, 16.0F));
         Tile tr = mtnTiles.get(tile.position.cpy().add(16.0F, 16.0F));
         Tile right = mtnTiles.get(tile.position.cpy().add(16.0F, 0.0F));
         if (tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile)
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_N";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile)
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile)
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile) - 1) {
            tile.nameUpper = "ledges3_NE";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile)
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_E";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile)
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_SE";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile)
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_S";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile)
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile)
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile) - 1) {
            tile.nameUpper = "ledges3_SW";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile)
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile)
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile) - 1) {
            tile.nameUpper = "ledges3_W";
            tile.init();
         } else if (tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile)
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile)
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile) - 1) {
            tile.nameUpper = "ledges3_NW";
            tile.init();
         } else if (tileLevels.containsKey(br)
            && tileLevels.get(br) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile)
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_NW_inner";
            tile.init();
         } else if (tileLevels.containsKey(bl)
            && tileLevels.get(bl) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(bot)
            && tileLevels.get(bot) == tileLevels.get(tile)
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_NE_inner";
            tile.init();
         } else if (tileLevels.containsKey(tr)
            && tileLevels.get(tr) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile)
            && tileLevels.containsKey(right)
            && tileLevels.get(right) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_SW_inner";
            tile.init();
         } else if (tileLevels.containsKey(tl)
            && tileLevels.get(tl) == tileLevels.get(tile) - 1
            && tileLevels.containsKey(top)
            && tileLevels.get(top) == tileLevels.get(tile)
            && tileLevels.containsKey(left)
            && tileLevels.get(left) == tileLevels.get(tile)) {
            tile.nameUpper = "ledges3_SE_inner";
            tile.init();
         }
      }

      for (Tile tile : new ArrayList<>(mtnTiles.values())) {
         if (this.rand.nextInt((int)Math.ceil(1.0F / (1.0F / (maxDist / 500.0F)))) == 1) {
            float distance = tile.position.dst(originTile.position);
            int level = (int)(30.0F * (1.0F - tile.position.dst(this.origin) / (this.radius / 12)));
            if (level < 4) {
               level = 4;
            }

            if (tileLevels.get(tile) <= currLevel) {
               level = 10;
            } else if (level > 30) {
               level = 30;
            }

            if (distance < 1 * maxDist / 40) {
               Route blotchRoute = new Route("snow1", level);
               this.ApplyBlotch(game, "mtn_snow1", tile, 7 * (int)(maxDist / distance), biomeTiles, 0, false, blotchRoute);
            } else {
               Route blotchRoute = new Route("mountain1", level);
               this.ApplyBlotch(game, "mtn_green1", tile, maxDist / 240, biomeTiles, 0, false, blotchRoute);
            }
         }
      }

      for (Tile tile : new ArrayList<>(mtnTiles.values())) {
         if (!this.noIceHerePlease.contains(tile.position)) {
            float scale = 12.0F;
            Vector2 rotate = tile.position.cpy().rotate(45.0F);
            float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
            float offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
            offsetX = Math.abs(offsetX);
            offsetY = Math.abs(offsetY);
            float offset1 = offsetX + offsetY;
            offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
            offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
            float offset2 = offsetX + offsetY;
            offset2 /= 5.0F;
            if (offset1 * offset2 > 14.0F) {
               tile.name = "mountain4";
               tile.init();
            }
         }

         if (this.rand.nextInt(24) == 0 && !biomeTiles.containsKey(tile.position)) {
            Route tempRoute = null;
            int randInt = this.rand.nextInt(2);
            if (randInt == 0) {
               tempRoute = new Route("", 22);
               String[] pokemon = new String[]{"slugma", "geodude", "shuckle"};
               randInt = this.rand.nextInt(pokemon.length);
               tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], tempRoute.level + this.rand.nextInt(3)));
            }

            Tile newTile = new Tile(tile.name, "rock1_color", tile.position.cpy(), true, tempRoute);
            biomeTiles.put(tile.position.cpy(), newTile);
         }

         if (tile.name.equals("mountain3")) {
            float scale = 8.0F;
            Vector2 rotate = tile.position.cpy().rotateDeg(-15.0F);
            float offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
            float offsetX = ((int)Math.abs(rotate.x / scale) - offsetY * 2.0F) % 14.0F - 7.0F;
            offsetX = Math.abs(offsetX);
            offsetY = Math.abs(offsetY);
            float offset1 = offsetX + offsetY;
            offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
            offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
            float offset2 = offsetX + offsetY;
            if (offset1 * (offset2 / 5.0F) > 16.0F) {
               tile.name = "mountain6";
               tile.init();
            }
         }
      }

      biomeTiles.putAll(volcanoTiles);

      for (Tile mtnTile : new ArrayList<>(mtnTiles.values())) {
         if (tileLevels.get(mtnTile) <= bottomLevel) {
            this.bottomMtnLayer.add(mtnTile.position);
            this.noIceHerePlease.remove(mtnTile.position);
         } else {
            Tile biomeTile = biomeTiles.get(mtnTile.position);
            if (biomeTile != null) {
               if (mtnTile.nameUpper.equals("")) {
                  mtnTiles.put(biomeTile.position.cpy(), biomeTile);
               } else {
                  mtnTile.name = biomeTile.name;
                  mtnTile.biome = biomeTile.biome;
                  if (mtnTile.name.contains("grass3")) {
                     mtnTile.name = "snow1";
                  } else if (mtnTile.name.contains("grass2")) {
                     mtnTile.name = "green1";
                  }

                  if (mtnTile.nameUpper.contains("ledges3") && mtnTile.biome.contains("tundra")) {
                     mtnTile.nameUpper = mtnTile.nameUpper.replace("ledges3", "ledges3snow");
                  } else if (mtnTile.nameUpper.contains("ledges3") && mtnTile.name.contains("volcano")) {
                     mtnTile.nameUpper = mtnTile.nameUpper.replace("ledges3", "ledges3volcano");
                  }

                  mtnTile.init();
               }
            }
         }
      }

      for (Tile tile : mtnTiles.values()) {
         if (tile.nameUpper.contains("ledges3") && tile.name.equals("water2")) {
            game.map.adjustSurroundingTiles(tile, mtnTiles, true);
         }
      }

      this.ledgify(mtnTiles, mtnTiles, "mountain4", "mountain3", "mountain");
      tilesToAdd.putAll(mtnTiles);
      return endPoints;
   }

   public void generateRegiDungeon(Game game, Vector2 origin) {
      HashMap<Vector2, Tile> exteriorTiles = new HashMap<>();
      ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<>();
      String[][] names = new String[][]{
         {null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null},
         {null, null, null, "cave2_bl", "cave2_br", null, null, null, "cave2_bl", "cave2_br", null, null, null},
         {null, null, null, null, null, "cave2_tl", "cave2_up", "cave2_tr", null, null, null, null, null},
         {null, null, null, null, "cave2_tl", "cave2_left", "cave2_floor", "cave2_right", "cave2_tr", null, null, null, null},
         {
               "cave2_tl_alternate",
               "cave2_tr_alternate",
               null,
               "sand1",
               "cave2_left",
               "cave2_bl",
               "cave2_down",
               "cave2_br",
               "cave2_right",
               "sand1",
               null,
               "cave2_tl_alternate",
               "cave2_tr_alternate"
         },
         {"cave2_bl", "cave2_br", null, "sand1", "cave2_bl", "cave2_down", "cave1_door1", "cave2_down", "cave2_br", "sand1", null, "cave2_bl", "cave2_br"},
         {null, null, null, null, "sand1", "sand2", "sand2", "sand2", "sand1", null, null, null, null},
         {null, null, null, null, null, "sand1", "sand1", "sand1", null, null, null, null, null},
         {null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null, "cave2_tl_alternate", "cave2_tr_alternate", null, null, null},
         {null, null, null, "cave2_bl", "cave2_br", null, null, null, "cave2_bl", "cave2_br", null, null, null}
      };
      Route currRoute = new Route("", 2);

      for (int i = 0; i < names.length; i++) {
         for (int j = 0; j < names[i].length; j++) {
            if (names[i][j] != null) {
               Vector2 pos = new Vector2(origin.x - 96.0F + j * 16, origin.y + 80.0F - i * 16);
               exteriorTiles.put(pos, new Tile(names[i][j], pos, true, currRoute));
            }
         }
      }

      origin.add(-288.0F, 272.0F);

      for (int i = 0; i < 90; i++) {
         interiorTiles.add(null);
      }

      HashMap<Vector2, Tile> currLayer = new HashMap<>();
      names = new String[][]{
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_br_inner",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_bl_inner",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_right1",
               "cave1_br_inner",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_down1",
               "cave1_bl_inner",
               "cave1_left1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_br_inner",
               "cave1_br1",
               "cave1_right1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_down1_dark",
               "cave1_down1_dark",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_bl1",
               "cave1_bl_inner",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_br_inner",
               "cave1_br1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_stone1",
               null,
               null,
               "cave1_stone1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_bl1",
               "cave1_bl_inner",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_right1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_regi1",
               "cave1_regi5",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_right1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_tl1",
               "cave1_up1",
               "cave1_regipedistal1",
               "cave1_tr1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_right1",
               "cave1_floor1",
               "cave1_stone1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor1",
               "cave1_stone1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_right1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_bl1",
               "cave1_ramp1",
               "cave1_ramp1",
               "cave1_br1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_right1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_tr1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_stone1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_stone1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_tl1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_floor1",
               "cave1_left1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_up1",
               "cave1_up1",
               "cave1_up1",
               "cave1_up1",
               "cave1_entrance1",
               "cave1_up1",
               "cave1_up1",
               "cave1_up1",
               "cave1_up1",
               "cave1_up1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         },
         {
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_right1",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2",
               "cave1_left1",
               "cave1_floor2"
         }
      };
      currRoute = new Route("regi_cave1", 44);

      for (int i = 0; i < names.length; i++) {
         for (int j = 0; j < names[i].length; j++) {
            if (names[i][j] != null) {
               Vector2 pos = new Vector2(origin.x + j * 16, origin.y - i * 16);
               Tile tile = new Tile(names[i][j], pos, true, currRoute);
               if (names[i][j].equals("cave1_regipedistal1")) {
                  tile.items().put("REGISTEEL", 1);
                  tile.items().put("REGICE", 1);
                  tile.items().put("REGIROCK", 1);
                  tile.items().put("REGIELEKI", 1);
                  tile.items().put("REGIDRAGO", 1);
               }

               currLayer.put(pos, tile);
            }
         }
      }

      interiorTiles.add(currLayer);

      for (Tile tile : exteriorTiles.values()) {
         Tile currTile = this.tilesToAdd.get(tile.position);
         if (currTile != null && currTile.nameUpper.equals("pokemon_mansion_key")) {
            this.tilesToAdd.put(tile.position.cpy(), new Tile(tile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo));
         } else {
            this.tilesToAdd.put(tile.position.cpy(), tile);
         }
      }

      for (int i = 0; i < interiorTiles.size(); i++) {
         currLayer = interiorTiles.get(i);
         if (i >= this.interiorTilesToAdd.size()) {
            this.interiorTilesToAdd.add(currLayer);
         } else if (currLayer != null) {
            if (this.interiorTilesToAdd.get(i) == null) {
               this.interiorTilesToAdd.remove(i);
               this.interiorTilesToAdd.add(i, currLayer);
            } else {
               for (Vector2 key : currLayer.keySet()) {
                  this.interiorTilesToAdd.get(i).put(key, currLayer.get(key));
               }
            }
         }
      }
   }

   public void generateOasis(Game game, Vector2 origin) {
      Route blotchRoute = new Route("oasis1", 30);
      Tile newTile = this.tilesToAdd.get(origin);
      newTile.biome = "desert";
      int nextSize = 250 + this.rand.nextInt(50);
      HashMap<Vector2, Tile> newTiles = new HashMap<>();
      this.ApplyBlotch(game, "oasis1", newTile, nextSize, newTiles, 0, true, blotchRoute);
      HashMap<Vector2, Tile> newTiles2 = new HashMap<>();
      nextSize /= 2;
      this.ApplyBlotch(game, "pond1", newTile, nextSize, newTiles2, 0, true, blotchRoute);
      Pokemon pokemon = new Pokemon("milotic", 44);
      pokemon.position = newTile.position.cpy();
      pokemon.mapTiles = game.map.overworldTiles;
      pokemon.standingAction = pokemon.new Standing();
      this.pokemonToAdd.put(pokemon.position.cpy(), pokemon);
      newTiles.putAll(newTiles2);

      for (Tile tile2 : new ArrayList<>(newTiles.values())) {
         if (tile2.nameUpper.equals("aloe_large1")) {
            newTiles.put(tile2.position.cpy().add(16.0F, 0.0F), new Tile("green1", "solid", tile2.position.cpy().add(16.0F, 0.0F), true, tile2.routeBelongsTo));
            newTiles.put(tile2.position.cpy().add(0.0F, 16.0F), new Tile("green1", "solid", tile2.position.cpy().add(0.0F, 16.0F), true, tile2.routeBelongsTo));
            newTiles.put(
               tile2.position.cpy().add(16.0F, 16.0F), new Tile("green1", "solid", tile2.position.cpy().add(16.0F, 16.0F), true, tile2.routeBelongsTo)
            );
         }
      }

      ArrayList<Tile> oasisTiles = new ArrayList<>();

      for (Tile tile : newTiles.values()) {
         if (!this.mtnTiles.containsKey(tile.position)) {
            if (!tile.name.contains("water")) {
               oasisTiles.add(tile);
            }

            this.tilesToAdd.put(tile.position.cpy(), tile);
         }
      }

      newTile = oasisTiles.remove(this.rand.nextInt(oasisTiles.size()));
      HashMap<Vector2, Tile> exteriorTiles = new HashMap<>();
      ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<>();

      for (int i = 0; i < 100; i++) {
         interiorTiles.add(null);
      }

      HashMap<Vector2, Tile> currLayer = new HashMap<>();
      Vector2 tl2 = newTile.position.cpy();
      String[][] names = new String[][]{
         {"building1_wall1", "building1_wall1", "building1_wall1", "building1_wall1", "building1_wall1", "building1_wall1"},
         {"building1_machine2", null, null, null, null, null},
         {"building1_fossilreviver1", "building1_cables1", "building1_cables1", "building1_cables1", "building1_pokecenter1", "building1_pokecenter1_right"},
         {"building1_floor2", "building1_floor1", "rug2", "building1_floor1", "building1_floor2", "building1_floor2"}
      };
      Route interiorRoute = new Route("fossil_lab1", 11);

      for (int i = 0; i < names.length; i++) {
         for (int j = 0; j < names[i].length; j++) {
            if (names[i][j] != null) {
               Vector2 pos2 = new Vector2(tl2.x - 96.0F + j * 16, tl2.y + 80.0F - i * 16);
               currLayer.put(pos2, new Tile(names[i][j], pos2, true, interiorRoute));
            }
         }
      }

      interiorTiles.add(currLayer);
      names = new String[][]{
         {"pkmnmansion_roof_NW", "pkmnmansion_roof_N", "pkmnmansion_roof_N", "pkmnmansion_roof_N_damaged", "pkmnmansion_roof_N", "pkmnmansion_roof_NE"},
         {"pkmnmansion_roof_SW", "pkmnmansion_roof_S_damaged", "pkmnmansion_roof_S", "pkmnmansion_roof_S", "pkmnmansion_roof_S", "pkmnmansion_roof_SE"},
         {
               "pkmnmansion_ext_W_window",
               "pkmnmansion_ext_windows",
               "pkmnmansion_ext_windows_W_damaged",
               "pkmnmansion_ext_windows_E_damaged",
               "pkmnmansion_ext_windows",
               "pkmnmansion_ext_E_window_damaged"
         },
         {
               "pkmnmansion_ext_SW",
               "pkmnmansion_ext_S",
               "pkmnmansion_ext_door",
               "pkmnmansion_ext_S_windows_damaged",
               "pkmnmansion_ext_S",
               "pkmnmansion_ext_SE_damaged"
         },
         {"", "", "", "", "", ""}
      };

      for (int i = 0; i < names.length; i++) {
         for (int j = 0; j < names[i].length; j++) {
            if (names[i][j] != null) {
               Vector2 pos2 = new Vector2(tl2.x - 96.0F + j * 16, tl2.y + 80.0F - i * 16);
               Tile currTile = exteriorTiles.get(pos2);
               if (currTile != null && currTile.name.equals("aloe_large1")) {
                  Vector2[] nexts = new Vector2[]{new Vector2(16.0F, 0.0F), new Vector2(0.0F, 16.0F), new Vector2(16.0F, 16.0F)};

                  for (Vector2 next : nexts) {
                     Vector2 nextPos = pos2.cpy().add(next);
                     Tile nextTile = exteriorTiles.get(nextPos);
                     if (nextTile.nameUpper.equals("solid")) {
                        nextTile.nameUpper = "";
                        nextTile.isSolid = false;
                     }
                  }
               }

               exteriorTiles.put(pos2, new Tile("sand1", names[i][j], pos2, true, newTile.routeBelongsTo));
            }
         }
      }

      for (Tile tile2 : exteriorTiles.values()) {
         Tile currTile = this.tilesToAdd.get(tile2.position);
         if (currTile != null && currTile.nameUpper.equals("pokemon_mansion_key")) {
            this.tilesToAdd.put(tile2.position.cpy(), new Tile(tile2.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo));
         } else {
            this.tilesToAdd.put(tile2.position.cpy(), tile2);
         }
      }

      for (int i = 0; i < interiorTiles.size(); i++) {
         currLayer = interiorTiles.get(i);
         if (i >= this.interiorTilesToAdd.size()) {
            this.interiorTilesToAdd.add(currLayer);
         } else if (currLayer != null) {
            if (this.interiorTilesToAdd.get(i) == null) {
               this.interiorTilesToAdd.remove(i);
               this.interiorTilesToAdd.add(i, currLayer);
            } else {
               for (Vector2 key : currLayer.keySet()) {
                  this.interiorTilesToAdd.get(i).put(key, currLayer.get(key));
               }
            }
         }
      }
   }

   public void generateDesertRuins(Game game, Vector2 origin) {
      int nextSize = 230;
      Route currRoute = new Route("ruins1_outer", 25);
      Tile newTile = this.tilesToAdd.get(origin);
      HashMap<Vector2, Tile> newTiles = new HashMap<>();
      HashMap<Vector2, Tile> newTiles2 = new HashMap<>();
      this.ApplyBlotch(game, "ruins1_upper2", newTile, nextSize, newTiles2, 0, false, currRoute);
      newTiles.putAll(newTiles2);
      newTiles2.clear();
      int var13 = 240;
      this.ApplyBlotch(game, "ruins1_upper", newTile, var13, newTiles2, 0, false, currRoute);
      newTiles.putAll(newTiles2);

      for (Tile tile : newTiles.values()) {
         if (tile.name.contains("ruins1")) {
            game.map.adjustSurroundingTiles(tile, newTiles);
         }
      }

      for (Tile tile : newTiles.values()) {
         if (!this.mtnTiles.containsKey(tile.position)) {
            this.tilesToAdd.put(tile.position.cpy(), tile);
         }
      }

      for (int i = -1; i < 2; i++) {
         for (int j = -1; j < 2; j++) {
            Tile currTile = this.tilesToAdd.get(newTile.position.cpy().add(16 * i, 16 * j));
            if (!currTile.name.equals("desert4")) {
               this.tilesToAdd.put(currTile.position.cpy(), new Tile("ruins_floor2", currTile.position.cpy(), true, null));
            }
         }
      }

      Vector2[] positions2 = new Vector2[]{
         newTile.position.cpy().add(-32.0F, 16.0F),
         newTile.position.cpy().add(32.0F, 16.0F),
         newTile.position.cpy().add(-32.0F, -16.0F),
         newTile.position.cpy().add(32.0F, -16.0F)
      };

      for (Vector2 position : positions2) {
         if (this.rand.nextInt(3) > 0) {
            this.tilesToAdd.put(position.cpy(), new Tile("ruins1_pillar1", position.cpy(), true, null));
         }
      }

      this.tilesToAdd.put(newTile.position.cpy(), new Tile("ruins1_NSEW", "stairs_down2", newTile.position.cpy(), true, currRoute));
      this.generateRuinsInterior(newTile.position.cpy());
   }

   public void generateRuinsInterior(Vector2 origin) {
      String[][] names = new String[][]{
         {"ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_wall1"},
         {"ruins2_wall1", "ruins2_wall1", "ruins2_wall1", "ruins2_volcarona_picture1", null, null, "ruins2_wall1", "ruins2_wall1", "ruins2_wall1"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "pedistal1", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "pedistal1", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "pedistal1", "ruins2_path1", "blank1", "blank1", "pedistal1", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins1_pillar1", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins1_pillar1", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins1_pillar1", "ruins2_path1", "blank1", "blank1", "ruins1_pillar1", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {"ruins_floor2", "ruins1_pillar1", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins1_pillar1", "ruins_floor2"},
         {"ruins_floor2", "ruins_floor2", "ruins_floor2", "ruins2_path1", "blank1", "blank1", "ruins_floor2", "ruins_floor2", "ruins_floor2"},
         {null, null, null, "ruins2_path1", "ruins_floor2_stairs", "blank1", null, null, null, null}
      };
      ArrayList<HashMap<Vector2, Tile>> interiorTiles = new ArrayList<>();

      for (int i = 0; i < 5; i++) {
         interiorTiles.add(new HashMap<>());
      }

      Vector2 startLoc = new Vector2(10.0F, 10.0F);
      Vector2 bl = origin.add(-496.0F, -496.0F);
      ArrayList<Vector2> startLocs = new ArrayList<>();
      ArrayList<Vector2> endPoints = new ArrayList<>();
      ArrayList<Vector2> dusclopsPositions = new ArrayList<>();
      endPoints.add(startLoc);
      Route currRoute = new Route("ruins1_inner", 22);

      for (int levelNum = interiorTiles.size() - 1; levelNum >= 0; levelNum--) {
         HashMap<Vector2, Tile> currLayer = interiorTiles.get(levelNum);
         if (levelNum == 0) {
            for (int i = 0; i < names.length; i++) {
               for (int j = 0; j < names[i].length; j++) {
                  if (names[i][j] != null) {
                     Vector2 startPos = endPoints.get(0);
                     Vector2 position = bl.cpy().add((startPos.x * 3.0F - 3.0F + j) * 16.0F, (startPos.y * 3.0F + 17.0F - i) * 16.0F);
                     Tile newTile;
                     if (names[i][j].equals("ruins_floor2_stairs")) {
                        newTile = new Tile("blank1", "stairs_up1", position.cpy(), true, currRoute);
                     } else if (names[i][j].equals("pedistal1")) {
                        newTile = new Tile("ruins_floor2", "pedistal1", position.cpy(), true, currRoute);
                     } else {
                        newTile = new Tile(names[i][j], position.cpy(), true, currRoute);
                     }

                     if (names[i][j].contains("pillar")) {
                        newTile.items().put("torch", 1);
                     }

                     if (i == 2 && j == 4) {
                        newTile.nameUpper = "volcarona";
                     }

                     currLayer.put(position, newTile);
                  }
               }
            }
         } else {
            float complexity = 15.0F;

            for (Vector2 position : endPoints) {
               String nameUpper = "stairs_up1";
               if (levelNum == interiorTiles.size() - 1) {
                  nameUpper = "stairs_up1_exit";
               }

               currLayer.put(
                  bl.cpy().add((position.x * 3.0F + 1.0F) * 16.0F, (position.y * 3.0F + 1.0F) * 16.0F),
                  new Tile("desert4", nameUpper, bl.cpy().add((position.x * 3.0F + 1.0F) * 16.0F, (position.y * 3.0F + 1.0F) * 16.0F), true, currRoute)
               );
               startLocs.add(position);
               startLocs.add(position);
               if (this.rand.nextInt(2) == 0 || levelNum == interiorTiles.size() - 1) {
                  startLocs.add(position);
                  if (levelNum != interiorTiles.size() - 1) {
                     complexity = 10.0F;
                  }
               }
            }

            endPoints.clear();
            int width = 20;
            int height = 20;
            float density = startLocs.size();
            boolean[][] maze = GenForest2.Maze_Algo3(width, height, complexity, density, this.rand, startLocs);
            System.out.print("\n");
            System.out.print("\n");

            for (int j = maze.length - 1; j >= 0; j--) {
               for (int i = 0; i < maze[j].length; i++) {
                  System.out.print((maze[i][j] ? 1 : " ") + " ");
               }

               System.out.print("\n");
            }

            dusclopsPositions.clear();

            for (int i = 0; i < maze.length; i++) {
               for (int j = 0; j < maze[i].length; j++) {
                  if (maze[i][j]) {
                     for (int k = 0; k < 3; k++) {
                        for (int l = 0; l < 4; l++) {
                           Vector2 position = bl.cpy().add((i * 3 + k) * 16, (j * 3 + l) * 16);
                           Tile currTile = currLayer.get(position);
                           if (currTile == null || currTile.name.contains("wall")) {
                              if (l == 3) {
                                 Tile newTile = new Tile("ruins2_wall1", position.cpy(), true, null);
                                 if (levelNum != interiorTiles.size() - 1 && i % 2 == 0 && k == 1 && this.rand.nextInt(4) > 0) {
                                    newTile.items().put("torch", 1);
                                 }

                                 currLayer.put(position, newTile);
                              } else {
                                 String name = "desert4";
                                 if (levelNum == interiorTiles.size() - 1) {
                                    name = "desert4_isGrass";
                                    int offset = Math.abs((i * 3 + k) % 12 - 6) + Math.abs((j * 3 + l) % 12 - 6);
                                    if (this.rand.nextInt(offset + 1) + offset > 14) {
                                       name = "ruins_floor2";
                                    }
                                 } else if (levelNum == interiorTiles.size() - 2) {
                                    name = "desert4_isGrass";
                                    int offset = Math.abs((i * 3 + k) % 12 - 6) + Math.abs((j * 3 + l) % 12 - 6);
                                    if (this.rand.nextInt(offset + 1) + offset > 10) {
                                       name = "ruins_floor2";
                                    }
                                 } else if (levelNum == interiorTiles.size() - 3) {
                                    int offset = Math.abs((i * 3 + k) % 12 - 6) + Math.abs((j * 3 + l) % 12 - 6);
                                    if (this.rand.nextInt(offset + 1) + offset > 7) {
                                       name = "ruins_floor2";
                                    }
                                 } else {
                                    name = "ruins_floor2";
                                 }

                                 currLayer.put(position, new Tile(name, position.cpy(), true, currRoute));
                              }

                              if (levelNum < 3 && k == 1 && l == 1 && i % 3 == 0 && j % 3 == 0) {
                                 dusclopsPositions.add(position);
                              }
                           }
                        }
                     }

                     if (!startLocs.contains(new Vector2(i, j)) && i % 2 == 0 && j % 2 == 0 && this.rand.nextInt(3) == 0) {
                        ArrayList<Vector2> potPositions = new ArrayList<>();
                        if (j - 1 >= 0 && !maze[i][j - 1]) {
                           potPositions.add(bl.cpy().add((i * 3 + 0) * 16, (j * 3 + 0) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 1) * 16, (j * 3 + 0) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 2) * 16, (j * 3 + 0) * 16));
                        }

                        if (j + 1 < height && !maze[i][j + 1]) {
                           potPositions.add(bl.cpy().add((i * 3 + 0) * 16, (j * 3 + 2) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 1) * 16, (j * 3 + 2) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 2) * 16, (j * 3 + 2) * 16));
                        }

                        if (i + 1 < width && !maze[i + 1][j]) {
                           potPositions.add(bl.cpy().add((i * 3 + 2) * 16, (j * 3 + 0) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 2) * 16, (j * 3 + 1) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 2) * 16, (j * 3 + 2) * 16));
                        }

                        if (i - 1 >= 0 && !maze[i - 1][j]) {
                           potPositions.add(bl.cpy().add((i * 3 + 0) * 16, (j * 3 + 0) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 0) * 16, (j * 3 + 1) * 16));
                           potPositions.add(bl.cpy().add((i * 3 + 0) * 16, (j * 3 + 2) * 16));
                        }

                        for (Vector2 position : potPositions) {
                           if (this.rand.nextInt(7) > 3) {
                              Tile tile = currLayer.get(position);
                              tile.nameUpper = "pot1";
                              tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                           }
                        }
                     } else if (levelNum == 1 && i % 2 == 0 && j % 2 == 0 && this.rand.nextInt(2) == 0) {
                        Vector2 position = bl.cpy().add((i * 3 + 1) * 16, (j * 3 + 1) * 16);
                        Tile tile = currLayer.get(position);
                        if (tile.nameUpper.equals("")) {
                           tile.nameUpper = "ruins_statue1";
                           tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                        }
                     }
                  }
               }
            }

            int size = dusclopsPositions.size();

            for (int i = 0; i < 2 + this.rand.nextInt(3) && i < size; i++) {
               Vector2 position = dusclopsPositions.remove(this.rand.nextInt(dusclopsPositions.size()));
               int interiorIndex = levelNum + 100 - 5;
               Pokemon pokemon = new Pokemon("dusclops", 44, Pokemon.Generation.CRYSTAL);
               pokemon.position = position.cpy();
               pokemon.standingAction = pokemon.new Standing();
               pokemon.aggroPlayer = true;
               this.interiorPokemon.put(pokemon, interiorIndex);
            }

            size = startLocs.size();
            int numStairs = 2;
            if (levelNum == 1) {
               numStairs = 1;
            }

            for (int i = 0; i < size; i++) {
               startLoc = startLocs.remove(this.rand.nextInt(startLocs.size()));
               System.out.println(startLoc);
               if (i < numStairs) {
                  for (int k = 0; k < 3; k++) {
                     for (int l = 0; l < 3; l++) {
                        Vector2 position = bl.cpy().add((startLoc.x * 3.0F + k) * 16.0F, (startLoc.y * 3.0F + l) * 16.0F);
                        if (k == 1 && l == 1) {
                           currLayer.put(position.cpy(), new Tile("ruins_floor2", "stairs_down2", position.cpy(), true, currRoute));
                        } else if (this.rand.nextInt(5) > 1) {
                           currLayer.put(position.cpy(), new Tile("ruins_floor2", position.cpy(), true, currRoute));
                        } else {
                           currLayer.put(position.cpy(), new Tile("desert4", position.cpy(), true, currRoute));
                        }
                     }
                  }

                  endPoints.add(startLoc);
               } else {
                  int numPots = 0;

                  for (int k = 0; k < 3; k++) {
                     for (int l = 0; l < 3; l++) {
                        Vector2 position = bl.cpy().add((startLoc.x * 3.0F + k) * 16.0F, (startLoc.y * 3.0F + l) * 16.0F);
                        Tile tile = currLayer.get(position);
                        if (k != 1 || l != 1) {
                           if (this.rand.nextInt(7) > 3 && numPots < 2) {
                              currLayer.put(position.cpy(), new Tile(tile.name, "pot1", position.cpy(), true, currRoute));
                              numPots++;
                           }
                        } else if (tile != null && !tile.nameUpper.contains("stairs")) {
                           tile.nameUpper = "pokeball1";
                           tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
                           if (this.rand.nextInt(2) == 0) {
                              tile.hasItem = "ultra ball";
                              tile.hasItemAmount = 2;
                           } else {
                              tile.hasItem = "ancientpowder";
                              tile.hasItemAmount = this.rand.nextInt(3) + 2;
                           }
                        }
                     }
                  }
               }
            }

            startLocs.clear();
         }
      }

      interiorTiles.add(new HashMap<>());

      for (int i = 0; i < interiorTiles.size(); i++) {
         HashMap<Vector2, Tile> currLayer = interiorTiles.get(i);
         int interiorIndex = i + 100 - 5;
         if (interiorIndex >= this.interiorTilesToAdd.size()) {
            this.interiorTilesToAdd.add(currLayer);
         } else if (this.interiorTilesToAdd.get(interiorIndex) == null) {
            this.interiorTilesToAdd.remove(interiorIndex);
            this.interiorTilesToAdd.add(interiorIndex, currLayer);
         } else {
            for (Vector2 key : currLayer.keySet()) {
               this.interiorTilesToAdd.get(interiorIndex).put(key, currLayer.get(key));
            }
         }
      }
   }

   public void generateMansion(Game game, HashMap<Vector2, Tile> mansionExteriorTiles, ArrayList<HashMap<Vector2, Tile>> mansionInteriorTiles, Vector2 bl) {
      int height = 28;
      int width = 30;
      int doWindows = 0;
      bl.add(-(width * 16) / 2, -(height * 16) / 2);
      Tile prevStatue = null;
      Route currRoute = new Route("snow1", 40);

      for (int i = 0; i < 95; i++) {
         mansionInteriorTiles.add(null);
      }

      mansionInteriorTiles.add(new HashMap<>());

      for (int i = -8; i <= height; i++) {
         doWindows = this.rand.nextInt(2) + 1;

         for (int j = 0; j <= width; j++) {
            if (j == width - 1) {
               doWindows = 1;
            }

            if (i >= 0 || j != width / 2 && j != width / 2 + 1) {
               if (i == -2) {
                  String name = "green1";
                  if (this.rand.nextInt(24) == 0) {
                     name = "flower4";
                  }

                  mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile(name, bl.cpy().add(j * 16, i * 16), true, currRoute));
               } else if (i == -1) {
                  mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile("sand1", bl.cpy().add(j * 16, i * 16), true, currRoute));
               } else if (i == 0) {
                  if (j == 0) {
                     mansionExteriorTiles.put(
                        bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_ext_SW", bl.cpy().add(j * 16, i * 16), true, currRoute)
                     );
                  } else if (j == width) {
                     mansionExteriorTiles.put(
                        bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_ext_SE", bl.cpy().add(j * 16, i * 16), true, currRoute)
                     );
                  } else if (j == width / 2) {
                     mansionExteriorTiles.put(
                        bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_ext_locked", bl.cpy().add(j * 16, i * 16), true, currRoute)
                     );
                  } else {
                     String name = "pkmnmansion_ext_S";
                     if (doWindows != 0) {
                        name = "pkmnmansion_ext_S_windows";
                     }

                     mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile("sand1", name, bl.cpy().add(j * 16, i * 16), true, currRoute));
                  }
               } else if (i != 1 && i != 2) {
                  if (i == 3) {
                     if (j == 0) {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_SW", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     } else if (j == width) {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_SE", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     } else {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_S", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     }
                  } else if (i == height) {
                     if (j == 0) {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_NW", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     } else if (j == width) {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_NE", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     } else {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_N", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     }
                  } else if (i > 0) {
                     if (j == 0) {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_W", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     } else if (j == width) {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof_E", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     } else {
                        mansionExteriorTiles.put(
                           bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_roof", bl.cpy().add(j * 16, i * 16), true, currRoute)
                        );
                     }
                  }
               } else if (j == 0) {
                  mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_ext_W", bl.cpy().add(j * 16, i * 16), true, currRoute));
               } else if (j == width) {
                  mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile("sand1", "pkmnmansion_ext_E", bl.cpy().add(j * 16, i * 16), true, currRoute));
               } else {
                  String name = "pkmnmansion_ext";
                  if (doWindows != 0) {
                     name = "pkmnmansion_ext_windows";
                  }

                  mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile("sand1", name, bl.cpy().add(j * 16, i * 16), true, currRoute));
               }
            } else {
               String name = "path1";
               if (this.rand.nextInt(6) <= 1) {
                  name = "sand1";
               }

               mansionExteriorTiles.put(bl.cpy().add(j * 16, i * 16), new Tile(name, bl.cpy().add(j * 16, i * 16), true, currRoute));
            }

            if (this.rand.nextInt(1 + doWindows) == 0) {
               doWindows = (doWindows + 1) % 2;
            }
         }
      }

      ArrayList<Vector2> alreadyChecked = new ArrayList<>();
      Tile stairsUp = null;
      Tile stairsDown = null;
      Tile statue = null;
      bl.add(-64.0F, -64.0F);

      for (int levelNum = 0; levelNum < 6; levelNum++) {
         currRoute = new Route("pkmnmansion1", 40 - levelNum * 2);
         stairsUp = null;
         stairsDown = null;
         statue = null;
         ArrayList<Boolean> rowIsSolid = new ArrayList<>();
         ArrayList<Integer> sizes = new ArrayList<>();
         sizes.add(1);

         for (int k = 0; k < 2; k++) {
            sizes.add(2);
            sizes.add(4);
            sizes.add(5);
         }

         if (levelNum < 4) {
            sizes.add(4);
            sizes.add(2);
            sizes.add(4);
            sizes.add(2);
         }

         int numRows = 0;
         int counter = 0;

         for (int i = 0; i < 27 + (4 - levelNum) * 2; i++) {
            if (i == 26 + (4 - levelNum) * 2) {
               rowIsSolid.add(false);
            } else if (counter == 0) {
               rowIsSolid.add(true);
               counter = sizes.remove(this.rand.nextInt(sizes.size()));
               numRows += 2;
            } else {
               rowIsSolid.add(false);
               counter--;
            }
         }

         rowIsSolid.add(true);
         ArrayList<Boolean> columnIsSolid = new ArrayList<>();
         sizes = new ArrayList<>();
         sizes.add(1);

         for (int k = 0; k < 2; k++) {
            sizes.add(2);
            sizes.add(4);
            sizes.add(5);
         }

         if (levelNum < 4) {
            sizes.add(4);
            sizes.add(2);
            sizes.add(4);
            sizes.add(2);
         }

         int numCols = 0;
         counter = 0;

         for (int i = 0; i < 29 + (4 - levelNum) * 2; i++) {
            if (i == 28 + (4 - levelNum) * 2) {
               columnIsSolid.add(false);
            } else if (counter == 0) {
               columnIsSolid.add(true);
               counter = sizes.remove(this.rand.nextInt(sizes.size()));
               numCols += 2;
            } else {
               columnIsSolid.add(false);
               counter--;
            }
         }

         columnIsSolid.add(true);
         float density = 0.05F;
         float complexity = 0.7F;
         boolean[][] maze = GenForest2.Maze_Algo2(numCols, numRows, density, complexity, this.rand);
         HashMap<Vector2, Tile> currLayer = new HashMap<>();
         mansionInteriorTiles.add(currLayer);
         int i = 0;

         for (int k = 0; k < rowIsSolid.size(); k++) {
            boolean rowSolid = rowIsSolid.get(k);
            int j = 0;

            for (int l = 0; l < columnIsSolid.size(); l++) {
               boolean columnSolid = columnIsSolid.get(l);
               if (maze[j][i]) {
                  currLayer.put(bl.cpy().add(l * 16, k * 16), new Tile("pkmnmansion_wall", bl.cpy().add(l * 16, k * 16), true, null));
               } else {
                  currLayer.put(bl.cpy().add(l * 16, k * 16), new Tile("pkmnmansion_floor1", bl.cpy().add(l * 16, k * 16), true, currRoute));
               }

               if (columnSolid || l + 1 < columnIsSolid.size() && columnIsSolid.get(l + 1)) {
                  j++;
               }
            }

            if (rowSolid || k + 1 < rowIsSolid.size() && rowIsSolid.get(k + 1)) {
               i++;
            }
         }

         alreadyChecked.clear();

         for (Vector2 pos : currLayer.keySet()) {
            if (!alreadyChecked.contains(pos) && currLayer.get(pos).isSolid) {
               alreadyChecked.add(pos);
               int size = 0;
               ArrayList<Vector2> checkThese = new ArrayList<>();
               ArrayList<Vector2> found = new ArrayList<>();
               found.add(pos);
               checkThese.add(pos.cpy().add(-16.0F, 0.0F));
               checkThese.add(pos.cpy().add(16.0F, 0.0F));
               checkThese.add(pos.cpy().add(0.0F, 16.0F));
               checkThese.add(pos.cpy().add(0.0F, -16.0F));

               while (checkThese.size() > 0) {
                  Vector2 pos2 = checkThese.remove(0);
                  if (!alreadyChecked.contains(pos2)) {
                     alreadyChecked.add(pos2);
                     if (currLayer.containsKey(pos2) && currLayer.get(pos2).isSolid) {
                        size++;
                        found.add(pos2);
                        checkThese.add(pos2.cpy().add(-16.0F, 0.0F));
                        checkThese.add(pos2.cpy().add(16.0F, 0.0F));
                        checkThese.add(pos2.cpy().add(0.0F, 16.0F));
                        checkThese.add(pos2.cpy().add(0.0F, -16.0F));
                     }
                  }
               }

               if (size < 10) {
                  for (Vector2 pos3 : found) {
                     currLayer.put(pos3.cpy(), new Tile("tree_plant1", pos3.cpy(), true, null));
                  }
               }
            }
         }

         for (Vector2 pos : currLayer.keySet()) {
            if (currLayer.get(pos).name.contains("pkmnmansion_wall")) {
               Vector2 left = pos.cpy().add(-16.0F, 0.0F);
               Vector2 right = pos.cpy().add(16.0F, 0.0F);
               Vector2 up = pos.cpy().add(0.0F, 16.0F);
               Vector2 down = pos.cpy().add(0.0F, -16.0F);
               boolean touchLeft = currLayer.containsKey(left) && currLayer.get(left).name.contains("pkmnmansion_wall");
               boolean touchRight = currLayer.containsKey(right) && currLayer.get(right).name.contains("pkmnmansion_wall");
               boolean touchUp = currLayer.containsKey(up) && currLayer.get(up).name.contains("pkmnmansion_wall");
               boolean touchDown = currLayer.containsKey(down) && currLayer.get(down).name.contains("pkmnmansion_wall");
               if (touchDown && touchUp) {
                  currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_NS", pos.cpy(), true, null));
               } else if (touchDown && touchLeft) {
                  if (this.rand.nextInt(2) == 0) {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                  } else {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_NE", pos.cpy(), true, null));
                  }
               } else if (touchDown && touchRight) {
                  if (this.rand.nextInt(2) == 0) {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
                  } else {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_NW", pos.cpy(), true, null));
                  }
               } else if (touchDown) {
                  currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_N", pos.cpy(), true, null));
               } else if (touchUp && touchLeft) {
                  if (this.rand.nextInt(2) == 0) {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                  } else {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_E", pos.cpy(), true, null));
                  }
               } else if (touchUp && touchRight) {
                  if (this.rand.nextInt(2) == 0) {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
                  } else {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_W", pos.cpy(), true, null));
                  }
               } else if (touchUp) {
                  currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_S", pos.cpy(), true, null));
               } else if (!touchLeft || !touchRight) {
                  if (touchLeft) {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_E", pos.cpy(), true, null));
                  } else if (touchRight) {
                     currLayer.put(pos.cpy(), new Tile("pkmnmansion_wall_W", pos.cpy(), true, null));
                  }
               }
            }
         }

         alreadyChecked.clear();

         for (Vector2 pos : currLayer.keySet()) {
            if (!alreadyChecked.contains(pos)) {
               alreadyChecked.add(pos);
               if (currLayer.get(pos).name.contains("pkmnmansion_wall") && this.rand.nextInt(13) == 0) {
                  ArrayList<Vector2> checkThese = new ArrayList<>();
                  ArrayList<Vector2> found = new ArrayList<>();
                  int size = 0;
                  found.add(pos);
                  checkThese.add(pos.cpy().add(-16.0F, 0.0F));
                  checkThese.add(pos.cpy().add(16.0F, 0.0F));
                  checkThese.add(pos.cpy().add(0.0F, 16.0F));
                  checkThese.add(pos.cpy().add(0.0F, -16.0F));

                  while (checkThese.size() > 0) {
                     Vector2 pos2 = checkThese.remove(0);
                     if (!alreadyChecked.contains(pos2)) {
                        alreadyChecked.add(pos2);
                        if (currLayer.containsKey(pos2)) {
                           if (!currLayer.get(pos2).isSolid) {
                              Vector2 left = pos2.cpy().add(-16.0F, 0.0F);
                              Vector2 tl = pos2.cpy().add(-16.0F, 16.0F);
                              Vector2 ble = pos2.cpy().add(-16.0F, -16.0F);
                              Vector2 right = pos2.cpy().add(16.0F, 0.0F);
                              Vector2 tr = pos2.cpy().add(16.0F, 16.0F);
                              Vector2 br = pos2.cpy().add(16.0F, -16.0F);
                              Vector2 up = pos2.cpy().add(0.0F, 16.0F);
                              Vector2 down = pos2.cpy().add(0.0F, -16.0F);
                              boolean touchLeft = currLayer.containsKey(left) && currLayer.get(left).isSolid;
                              boolean touchTl = currLayer.containsKey(tl) && currLayer.get(tl).isSolid;
                              boolean touchBle = currLayer.containsKey(ble) && currLayer.get(ble).isSolid;
                              boolean touchRight = currLayer.containsKey(right) && currLayer.get(right).isSolid;
                              boolean touchTr = currLayer.containsKey(tr) && currLayer.get(tr).isSolid;
                              boolean touchBr = currLayer.containsKey(br) && currLayer.get(br).isSolid;
                              boolean touchUp = currLayer.containsKey(up) && currLayer.get(up).isSolid;
                              boolean touchDown = currLayer.containsKey(down) && currLayer.get(down).isSolid;
                              if (touchLeft && (touchTr && !touchUp || touchBr && !touchDown)
                                 || touchUp && (touchBle && !touchLeft || touchBr && !touchRight)
                                 || touchRight && (touchTl && !touchUp || touchBle && !touchDown)
                                 || touchDown && (touchTl && !touchLeft || touchTr && !touchRight)) {
                                 continue;
                              }
                           } else if (!currLayer.get(pos2).name.contains("pkmnmansion_wall")) {
                              continue;
                           }

                           if (size <= 10 || this.rand.nextInt(35) - size > 0) {
                              size++;
                              currLayer.put(pos2.cpy(), new Tile("pkmnmansion_floor1", "rubble1", pos2.cpy(), true, null));
                              checkThese.add(pos2.cpy().add(-16.0F, 0.0F));
                              checkThese.add(pos2.cpy().add(16.0F, 0.0F));
                              checkThese.add(pos2.cpy().add(0.0F, 16.0F));
                              checkThese.add(pos2.cpy().add(0.0F, -16.0F));
                           }
                        }
                     }
                  }
               }
            }
         }

         alreadyChecked.clear();

         for (Vector2 pos : currLayer.keySet()) {
            if (!alreadyChecked.contains(pos)) {
               alreadyChecked.add(pos);
               int numShelves = 0;
               HashMap<Vector2, Tile> currSet = new HashMap<>();
               if (!currLayer.get(pos).isSolid
                  && (
                     currLayer.get(pos.cpy().add(0.0F, 16.0F)).name.contains("pkmnmansion_wall")
                        || currLayer.get(pos.cpy().add(-16.0F, 0.0F)).name.contains("pkmnmansion_wall")
                        || currLayer.get(pos.cpy().add(16.0F, 0.0F)).name.contains("pkmnmansion_wall")
                  )
                  && this.rand.nextInt(5 + numShelves) == 0) {
                  ArrayList<Vector2> checkThese = new ArrayList<>();
                  ArrayList<Vector2> found = new ArrayList<>();
                  int size = 0;
                  found.add(pos);
                  checkThese.add(pos.cpy().add(-16.0F, 0.0F));
                  checkThese.add(pos.cpy().add(16.0F, 0.0F));
                  checkThese.add(pos.cpy().add(0.0F, 16.0F));
                  checkThese.add(pos.cpy().add(0.0F, -16.0F));
                  numShelves++;

                  while (checkThese.size() > 0) {
                     Vector2 pos2 = checkThese.remove(0);
                     if (!alreadyChecked.contains(pos2)) {
                        alreadyChecked.add(pos2);
                        if (currLayer.containsKey(pos2) && !currLayer.get(pos2).isSolid) {
                           Vector2 left = pos2.cpy().add(-16.0F, 0.0F);
                           Vector2 right = pos2.cpy().add(16.0F, 0.0F);
                           Vector2 up = pos2.cpy().add(0.0F, 16.0F);
                           Vector2 down = pos2.cpy().add(0.0F, -16.0F);
                           boolean touchLeft = currLayer.get(left).name.contains("pkmnmansion_wall");
                           boolean touchRight = currLayer.get(right).name.contains("pkmnmansion_wall");
                           boolean touchUp = currLayer.get(up).name.contains("pkmnmansion_wall");
                           boolean touchDown = currLayer.get(down).name.contains("pkmnmansion_wall");
                           if (!touchDown && (!touchLeft || !touchRight) && (touchLeft || touchRight || touchUp)) {
                              Vector2 tl = pos2.cpy().add(-16.0F, 16.0F);
                              Vector2 ble = pos2.cpy().add(-16.0F, -16.0F);
                              Vector2 tr = pos2.cpy().add(16.0F, 16.0F);
                              Vector2 br = pos2.cpy().add(16.0F, -16.0F);
                              boolean touchTl = currLayer.get(tl).isSolid;
                              boolean touchBle = currLayer.get(ble).isSolid;
                              boolean touchTr = currLayer.get(tr).isSolid;
                              boolean touchBr = currLayer.get(br).isSolid;
                              touchLeft = currLayer.get(left).isSolid;
                              touchRight = currLayer.get(right).isSolid;
                              touchUp = currLayer.get(up).isSolid;
                              touchDown = currLayer.get(down).isSolid;
                              if ((!touchLeft || (!touchTr || touchUp) && (!touchBr || touchDown))
                                 && (!touchUp || (!touchBle || touchLeft) && (!touchBr || touchRight))
                                 && (!touchRight || (!touchTl || touchUp) && (!touchBle || touchDown))
                                 && (!touchDown || (!touchTl || touchLeft) && (!touchTr || touchRight))
                                 && (size <= 10 || this.rand.nextInt(35) - size > 0)) {
                                 size++;
                                 currSet.put(pos2.cpy(), new Tile("pkmnmansion_shelf1", pos2.cpy(), true, null));
                                 checkThese.add(pos2.cpy().add(-16.0F, 0.0F));
                                 checkThese.add(pos2.cpy().add(16.0F, 0.0F));
                                 checkThese.add(pos2.cpy().add(0.0F, 16.0F));
                                 checkThese.add(pos2.cpy().add(0.0F, -16.0F));
                              }
                           }
                        }
                     }
                  }

                  if (currSet.keySet().size() > 3) {
                     for (Vector2 pos2 : currSet.keySet()) {
                        Vector2 up = pos2.cpy().add(0.0F, 16.0F);
                        boolean touchUp = currSet.containsKey(up);
                        if (touchUp) {
                           currLayer.put(pos2.cpy(), new Tile("pkmnmansion_shelf1_NS", pos2.cpy(), true, null));
                        } else {
                           currLayer.put(pos2.cpy(), new Tile("pkmnmansion_shelf1", pos2.cpy(), true, null));
                        }
                     }
                  }
               }
            }
         }

         if (levelNum == 4) {
         }

         alreadyChecked.clear();
         ArrayList<HashMap<Vector2, Tile>> possibleSpots = new ArrayList<>();

         label1787:
         for (Vector2 pos = bl.cpy(); pos.y < bl.y + 448.0F; pos.x += 16.0F) {
            if (pos.x > bl.x + 480.0F) {
               pos.x = bl.x - 16.0F;
               pos.y += 16.0F;
            } else if (currLayer.containsKey(pos) && !alreadyChecked.contains(pos) && currLayer.get(pos).name.contains("pkmnmansion_floor1")) {
               int h = 4;
               int w = 4;
               Vector2 lastWorking = null;
               boolean yDir = false;

               while (true) {
                  boolean works = true;

                  for (int x = 0; x < w; x++) {
                     for (int y = 0; y < h; y++) {
                        if (currLayer.get(pos.cpy().add(x * 16, y * 16)).isSolid) {
                           works = false;
                           break;
                        }
                     }

                     if (!works) {
                        break;
                     }
                  }

                  if (works) {
                     lastWorking = new Vector2(w, h);
                     if (yDir) {
                        h++;
                     } else {
                        w++;
                     }
                  } else {
                     if (yDir) {
                        if (lastWorking == null) {
                           break;
                        }

                        if (lastWorking.x < 7.0F && lastWorking.y < 7.0F) {
                           HashMap<Vector2, Tile> putThese = new HashMap<>();

                           for (int x = 1; x < lastWorking.x - 1.0F; x++) {
                              for (int y = 1; y < lastWorking.y - 1.0F; y++) {
                                 if (x == 1 && y == 1) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_SW", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (x == lastWorking.x - 2.0F && y == lastWorking.y - 2.0F) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_NE", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (x == 1 && y == lastWorking.y - 2.0F) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_NW", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (x == lastWorking.x - 2.0F && y == 1) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_SE", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (x == 1) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_W", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (x == lastWorking.x - 2.0F) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_E", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (y == 1) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_S", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else if (y == lastWorking.y - 2.0F) {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table_N", pos.cpy().add(x * 16, y * 16), true, null));
                                 } else {
                                    putThese.put(pos.cpy().add(x * 16, y * 16), new Tile("table", pos.cpy().add(x * 16, y * 16), true, null));
                                 }
                              }
                           }

                           possibleSpots.add(putThese);
                        }

                        int x = 0;

                        while (true) {
                           if (!(x < lastWorking.x)) {
                              continue label1787;
                           }

                           for (int y = 0; y < lastWorking.y + 1.0F; y++) {
                              alreadyChecked.add(pos.cpy().add(x * 16, y * 16));
                           }

                           x++;
                        }
                     }

                     yDir = true;
                  }
               }
            }
         }

         for (HashMap<Vector2, Tile> putThese : possibleSpots) {
            boolean works = true;

            for (Vector2 pos : putThese.keySet()) {
               if (currLayer.get(pos).isSolid) {
                  works = false;
                  break;
               }
            }

            if (works) {
               for (Vector2 pos : putThese.keySet()) {
                  currLayer.put(pos, putThese.get(pos));
               }
            }
         }

         bl.add(16.0F, 16.0F);
      }

      bl.add(-32.0F, -32.0F);
      Tile nextStairsUp = null;
      HashMap<Vector2, Tile> currLayer = mansionInteriorTiles.get(100);
      HashMap<Vector2, Tile> layerAbove = mansionInteriorTiles.get(101);
      currRoute = new Route("pkmnmansion1", 30);
      int yMax = this.rand.nextInt(4) * 2 + 6;

      while (true) {
         Vector2 pos = bl.cpy().add(240.0F, (yMax - 1) * 16);
         if (layerAbove.get(pos) == null || !layerAbove.get(pos).isSolid) {
            for (int x = 0; x < 10; x++) {
               for (int y = 0; y < yMax; y++) {
                  Vector2 posx = bl.cpy().add((x + 10) * 16, y * 16);
                  if (y == yMax - 1 && x == 5) {
                     nextStairsUp = new Tile("pkmnmansion_floor2", "stairs_up1", posx.cpy(), true, null);
                     currLayer.put(posx.cpy(), nextStairsUp);
                     stairsUp = new Tile("pkmnmansion_floor2", "stairs_down1", posx.cpy(), true, null);
                     layerAbove.put(posx.cpy(), stairsUp);
                  } else if (y == 0 && x == 5) {
                     currLayer.put(posx.cpy(), new Tile("pkmnmansion_rug", posx.cpy(), true, currRoute));
                  } else if ((x == 2 || x == 7) && y % 2 == 1 && y != 1) {
                     currLayer.put(posx.cpy(), new Tile("pkmnmansion_block1", posx.cpy(), true, null));
                  } else if (x == 3) {
                     currLayer.put(posx.cpy(), new Tile("pkmnmansion_floor2_W", posx.cpy(), true, currRoute));
                  } else if (x == 6) {
                     currLayer.put(posx.cpy(), new Tile("pkmnmansion_floor2_E", posx.cpy(), true, currRoute));
                  } else if (x > 3 && x < 6) {
                     currLayer.put(posx.cpy(), new Tile("pkmnmansion_floor2", posx.cpy(), true, currRoute));
                  } else if (y != 0) {
                     currLayer.put(posx.cpy(), new Tile("pkmnmansion_floor1", posx.cpy(), true, currRoute));
                  }
               }
            }

            HashMap<Vector2, Tile> layerBelow = null;

            label1347:
            for (int levelNum = 6; levelNum > 0; levelNum--) {
               currLayer = mansionInteriorTiles.get(95 + levelNum);
               HashMap<Vector2, Tile> var74 = mansionInteriorTiles.get(94 + levelNum);
               if (stairsUp != null) {
                  alreadyChecked.clear();
                  ArrayList<Vector2> checkThese = new ArrayList<>();
                  ArrayList<Vector2> shuffleThese = new ArrayList<>();
                  ArrayList<Vector2> endPoints = new ArrayList<>();
                  checkThese.add(stairsUp.position.cpy());
                  alreadyChecked.add(stairsUp.position.cpy());

                  while (checkThese.size() > 0) {
                     for (Vector2 posx : new ArrayList<>(checkThese)) {
                        boolean addedAnother = false;
                        boolean touchTable = false;
                        boolean touchEndpoint = false;
                        checkThese.remove(posx);
                        shuffleThese.clear();
                        shuffleThese.add(posx.cpy().add(16.0F, 0.0F));
                        shuffleThese.add(posx.cpy().add(-16.0F, 0.0F));
                        shuffleThese.add(posx.cpy().add(0.0F, 16.0F));
                        shuffleThese.add(posx.cpy().add(0.0F, -16.0F));

                        do {
                           Vector2 pos2 = shuffleThese.remove(this.rand.nextInt(shuffleThese.size()));
                           if (!alreadyChecked.contains(pos2)
                              && currLayer.containsKey(pos2)
                              && !currLayer.get(pos2).isSolid
                              && !currLayer.get(pos2).nameUpper.contains("stairs")) {
                              checkThese.add(pos2);
                              alreadyChecked.add(pos2);
                              addedAnother = true;
                           }

                           if (currLayer.containsKey(pos2) && currLayer.get(pos2).name.contains("table")) {
                              touchTable = true;
                           }

                           if (endPoints.contains(pos2)) {
                              touchEndpoint = true;
                           }
                        } while (shuffleThese.size() > 0);

                        if (!touchTable
                           && !touchEndpoint
                           && !addedAnother
                           && !currLayer.get(posx).name.contains("floor2")
                           && !currLayer.get(posx).name.contains("rug")) {
                           endPoints.add(posx);
                        }
                     }
                  }

                  Vector2 pos3 = endPoints.get(endPoints.size() - 1);

                  while (endPoints.size() > 0) {
                     if (endPoints.size() > 4) {
                        pos3 = endPoints.remove(this.rand.nextInt(5) + endPoints.size() - 5);
                     } else {
                        pos3 = endPoints.remove(endPoints.size() - 1);
                     }

                     if (var74.get(pos3) == null || !((Tile)var74.get(pos3)).isSolid) {
                        stairsDown = new Tile("pkmnmansion_floor2", "stairs_down1", pos3.cpy(), true, null);
                        break;
                     }
                  }

                  if (levelNum != 6) {
                     currLayer.put(pos3.cpy(), stairsDown);
                     nextStairsUp = new Tile("pkmnmansion_floor1", "stairs_up1", pos3.cpy(), true, null);
                     var74.put(pos3.cpy(), nextStairsUp);
                  }

                  int maxJ = this.rand.nextInt(3) + 3;

                  for (int j = 0; j < maxJ * 3 && endPoints.size() > 5; j++) {
                     pos3 = endPoints.remove(endPoints.size() - 1);
                     if (j % 3 == 0) {
                        currLayer.put(pos3.cpy(), new Tile("pkmnmansion_floor1", "ultraball1", pos3.cpy(), true, null));
                     }
                  }

                  alreadyChecked.clear();
                  checkThese.clear();
                  shuffleThese.clear();
                  endPoints.clear();
                  checkThese.add(stairsUp.position.cpy());
                  alreadyChecked.add(stairsUp.position.cpy());
                  checkThese.add(stairsDown.position.cpy());
                  alreadyChecked.add(stairsUp.position.cpy());

                  while (checkThese.size() > 0) {
                     for (Vector2 posx : new ArrayList<>(checkThese)) {
                        boolean addedAnother = false;
                        boolean touchTable = false;
                        boolean touchEndpoint = false;
                        int numTouch = 0;
                        checkThese.remove(posx);
                        shuffleThese.clear();
                        shuffleThese.add(posx.cpy().add(16.0F, 0.0F));
                        shuffleThese.add(posx.cpy().add(-16.0F, 0.0F));
                        shuffleThese.add(posx.cpy().add(0.0F, 16.0F));
                        shuffleThese.add(posx.cpy().add(0.0F, -16.0F));

                        do {
                           Vector2 pos2 = shuffleThese.remove(this.rand.nextInt(shuffleThese.size()));
                           if (currLayer.containsKey(pos2) && currLayer.get(pos2).isSolid) {
                              numTouch++;
                           }

                           if (!alreadyChecked.contains(pos2)
                              && currLayer.containsKey(pos2)
                              && !currLayer.get(pos2).isSolid
                              && !currLayer.get(pos2).nameUpper.contains("stairs")) {
                              checkThese.add(pos2);
                              alreadyChecked.add(pos2);
                              addedAnother = true;
                           }

                           if (currLayer.containsKey(pos2) && currLayer.get(pos2).name.contains("table")) {
                              touchTable = true;
                           }

                           if (endPoints.contains(pos2)) {
                              touchEndpoint = true;
                           }
                        } while (shuffleThese.size() > 0);

                        boolean touchDown = currLayer.containsKey(posx.cpy().add(0.0F, -16.0F)) && currLayer.get(posx.cpy().add(0.0F, -16.0F)).isSolid;
                        boolean touchBl = currLayer.containsKey(posx.cpy().add(-16.0F, -16.0F)) && currLayer.get(posx.cpy().add(-16.0F, -16.0F)).isSolid;
                        boolean touchBr = currLayer.containsKey(posx.cpy().add(16.0F, -16.0F)) && currLayer.get(posx.cpy().add(16.0F, -16.0F)).isSolid;
                        boolean touchTl = currLayer.containsKey(posx.cpy().add(-16.0F, 16.0F)) && currLayer.get(posx.cpy().add(-16.0F, 16.0F)).isSolid;
                        boolean touchTr = currLayer.containsKey(posx.cpy().add(16.0F, 16.0F)) && currLayer.get(posx.cpy().add(16.0F, 16.0F)).isSolid;
                        if (!touchDown
                           && (!touchBl || !touchBr)
                           && (!touchTl || !touchTr)
                           && numTouch < 2
                           && !touchTable
                           && !touchEndpoint
                           && !addedAnother
                           && !currLayer.get(posx).name.contains("floor2")
                           && !currLayer.get(posx).name.contains("rug")) {
                           endPoints.add(posx);
                        }
                     }
                  }

                  if (endPoints.size() > 4) {
                     pos3 = endPoints.remove(this.rand.nextInt(5) + endPoints.size() - 5);
                  } else {
                     pos3 = endPoints.remove(endPoints.size() - 1);
                  }

                  statue = new Tile("pkmnmansion_statue1", pos3.cpy(), true, null);
                  if (levelNum != 1) {
                     currLayer.put(pos3.cpy(), statue);
                  }

                  ArrayList<ArrayList<Tile>> allDoors = new ArrayList<>();
                  alreadyChecked.clear();
                  Iterator var102 = currLayer.keySet().iterator();

                  while (true) {
                     Tile currTile;
                     int length;
                     ArrayList<Vector2> checkDirs;
                     while (true) {
                        if (!var102.hasNext()) {
                           if (prevStatue != null) {
                              prevStatue.doorTiles = new ArrayList<>();
                              int j = 0;

                              for (ArrayList<Tile> door : allDoors) {
                                 for (Tile tile : door) {
                                    currLayer.put(tile.position.cpy(), tile);
                                    if (!prevStatue.doorTiles.contains(tile.position)) {
                                       prevStatue.doorTiles.add(tile.position.cpy());
                                    }
                                 }

                                 j++;
                              }

                              ArrayList<ArrayList<Tile>> stairsDownDoors = new ArrayList<>();

                              for (ArrayList<Tile> door : allDoors) {
                                 stairsDownDoors.add(door);
                              }

                              int numRemoved = 0;
                              boolean changedDoor = true;

                              while (changedDoor) {
                                 boolean pathToStairs = this.isPathBetween(currLayer, stairsUp, stairsDown);
                                 boolean pathToStatue = this.isPathBetween(currLayer, stairsUp, statue);
                                 changedDoor = false;

                                 for (int k = 0; k < stairsDownDoors.size(); k++) {
                                    ArrayList<Tile> door = stairsDownDoors.get(this.rand.nextInt(stairsDownDoors.size()));

                                    for (Tile tile : door) {
                                       tile.flipDoorTile();
                                    }

                                    if (!pathToStairs && this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                       for (Tile tile : door) {
                                          tile.flipDoorTile();
                                       }
                                    } else if (pathToStatue && !this.isPathBetween(currLayer, stairsUp, statue)) {
                                       for (Tile tile : door) {
                                          tile.flipDoorTile();
                                       }
                                    } else {
                                       stairsDownDoors.remove(door);
                                       changedDoor = true;
                                       numRemoved++;
                                    }
                                 }
                              }

                              stairsDownDoors.clear();

                              for (ArrayList<Tile> door : allDoors) {
                                 for (Tile tile : door) {
                                    if (tile.name.contains("__on")) {
                                       stairsDownDoors.add(door);
                                       break;
                                    }
                                 }
                              }

                              for (Vector2 posx : prevStatue.doorTiles) {
                                 if (currLayer.get(posx).name.contains("__off")) {
                                    currLayer.get(posx).flipDoorTile();
                                 }
                              }

                              int var104 = 0;

                              for (ArrayList<Tile> door : allDoors) {
                                 for (Tile tile : door) {
                                    if (tile.name.contains("__off")) {
                                       tile.flipDoorTile();
                                    }
                                 }

                                 var104++;
                              }

                              ArrayList<ArrayList<Tile>> statueDoors = new ArrayList<>();

                              for (ArrayList<Tile> door : allDoors) {
                                 if (!stairsDownDoors.contains(door)) {
                                    statueDoors.add(door);
                                 }
                              }

                              int var132 = 0;
                              changedDoor = true;

                              while (changedDoor) {
                                 boolean pathToStairs = this.isPathBetween(currLayer, stairsUp, stairsDown);
                                 boolean pathToStatue = this.isPathBetween(currLayer, stairsUp, statue);
                                 changedDoor = false;

                                 for (int k = 0; k < statueDoors.size(); k++) {
                                    ArrayList<Tile> door = statueDoors.get(this.rand.nextInt(statueDoors.size()));

                                    for (Tile tile : door) {
                                       tile.flipDoorTile();
                                    }

                                    if (!pathToStatue && this.isPathBetween(currLayer, stairsUp, statue)) {
                                       for (Tile tile : door) {
                                          tile.flipDoorTile();
                                       }
                                    } else if (pathToStairs && !this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                       for (Tile tile : door) {
                                          tile.flipDoorTile();
                                       }
                                    } else {
                                       statueDoors.remove(door);
                                       changedDoor = true;
                                       var132++;
                                    }
                                 }
                              }

                              statueDoors.clear();

                              for (ArrayList<Tile> door : allDoors) {
                                 for (Tile tile : door) {
                                    if (tile.name.contains("__on")) {
                                       statueDoors.add(door);
                                       break;
                                    }
                                 }
                              }

                              for (ArrayList<Tile> door : allDoors) {
                                 for (Tile tile : door) {
                                    currLayer.put(tile.position.cpy(), new Tile("pkmnmansion_floor1", tile.position.cpy(), true, null));
                                    prevStatue.doorTiles.remove(tile.position);
                                 }
                              }

                              prevStatue.doorTiles.clear();

                              for (ArrayList<Tile> door : statueDoors) {
                                 for (Tile tile : door) {
                                    currLayer.put(tile.position.cpy(), tile);
                                    prevStatue.doorTiles.add(tile.position.cpy());
                                    if (tile.name.contains("__off")) {
                                       tile.flipDoorTile();
                                    }
                                 }
                              }

                              if (!this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                 for (ArrayList<Tile> door : statueDoors) {
                                    for (Tile tile : door) {
                                       currLayer.put(tile.position.cpy(), new Tile("pkmnmansion_floor1", tile.position.cpy(), true, null));
                                       prevStatue.doorTiles.remove(tile.position);
                                    }

                                    if (this.isPathBetween(currLayer, stairsUp, stairsDown)) {
                                       break;
                                    }
                                 }
                              }

                              for (ArrayList<Tile> door : stairsDownDoors) {
                                 for (Tile tile : door) {
                                    currLayer.put(tile.position.cpy(), tile);
                                    if (!prevStatue.doorTiles.contains(tile.position)) {
                                       prevStatue.doorTiles.add(tile.position.cpy());
                                    }

                                    if (tile.name.contains("__on")) {
                                       tile.flipDoorTile();
                                    }
                                 }
                              }

                              if (this.rand.nextInt(2) == 0) {
                                 for (Vector2 posx : prevStatue.doorTiles) {
                                    currLayer.get(posx).flipDoorTile();
                                 }
                              }
                           }

                           if (levelNum == 1) {
                              bl = stairsDown.position.cpy().add(-80.0F, -16.0F);
                              mansionInteriorTiles.remove(94 + levelNum);
                              mansionInteriorTiles.add(94 + levelNum, new HashMap<>());
                              HashMap<Vector2, Tile> var75 = mansionInteriorTiles.get(94 + levelNum);
                              Tile currTilex = null;

                              for (int x = 0; x < 10; x++) {
                                 for (int y = 0; y < 11; y++) {
                                    Vector2 posx = bl.cpy().add(x * 16, y * 16);
                                    if (y != 2 && y != 6 || x != 2 && x != 3 && x != 6 && x != 7) {
                                       if (x == 0 || y == 0 || y == 10 || x == 9) {
                                          currTilex = new Tile("pkmnmansion_wall", posx.cpy(), true, null);
                                       } else if (x == 4 && y == 9) {
                                          currTilex = new Tile("pkmnmansion_struct1", posx.cpy(), true, null);
                                       } else if (x != 5 || y != 9) {
                                          if (x == 5 && y == 8) {
                                             currTilex = new Tile("pkmnmansion_floor1", "mewtwo_overworld", posx.cpy(), true, null);
                                          } else if (x == 5 && y == 1) {
                                             currTilex = new Tile("pkmnmansion_floor1", "stairs_up1", posx.cpy(), true, null);
                                          } else {
                                             currTilex = new Tile("pkmnmansion_floor1", posx.cpy(), true, null);
                                          }
                                       }
                                    } else {
                                       currTilex = new Tile("tree_plant1", posx.cpy(), true, null);
                                    }

                                    var75.put(posx.cpy(), currTilex);
                                 }
                              }

                              for (Vector2 posx : var75.keySet()) {
                                 if (((Tile)var75.get(posx)).name.contains("pkmnmansion_wall")) {
                                    Vector2 left = posx.cpy().add(-16.0F, 0.0F);
                                    Vector2 right = posx.cpy().add(16.0F, 0.0F);
                                    Vector2 up = posx.cpy().add(0.0F, 16.0F);
                                    Vector2 down = posx.cpy().add(0.0F, -16.0F);
                                    boolean touchLeft = var75.containsKey(left) && ((Tile)var75.get(left)).name.contains("pkmnmansion_wall");
                                    boolean touchRight = var75.containsKey(right) && ((Tile)var75.get(right)).name.contains("pkmnmansion_wall");
                                    boolean touchUp = var75.containsKey(up) && ((Tile)var75.get(up)).name.contains("pkmnmansion_wall");
                                    boolean touchDown = var75.containsKey(down) && ((Tile)var75.get(down)).name.contains("pkmnmansion_wall");
                                    if (touchDown && touchUp) {
                                       var75.put(posx.cpy(), new Tile("pkmnmansion_wall_NS", posx.cpy(), true, null));
                                    } else if (touchDown && touchLeft) {
                                       if (this.rand.nextInt(2) == 0) {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_N", posx.cpy(), true, null));
                                       } else {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_NE", posx.cpy(), true, null));
                                       }
                                    } else if (touchDown && touchRight) {
                                       if (this.rand.nextInt(2) == 0) {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_N", posx.cpy(), true, null));
                                       } else {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_NW", posx.cpy(), true, null));
                                       }
                                    } else if (touchDown) {
                                       var75.put(posx.cpy(), new Tile("pkmnmansion_wall_N", posx.cpy(), true, null));
                                    } else if (touchUp && touchLeft) {
                                       if (this.rand.nextInt(2) == 0) {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_S", posx.cpy(), true, null));
                                       } else {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_E", posx.cpy(), true, null));
                                       }
                                    } else if (touchUp && touchRight) {
                                       if (this.rand.nextInt(2) == 0) {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_S", posx.cpy(), true, null));
                                       } else {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_W", posx.cpy(), true, null));
                                       }
                                    } else if (touchUp) {
                                       var75.put(posx.cpy(), new Tile("pkmnmansion_wall_S", posx.cpy(), true, null));
                                    } else if (!touchLeft || !touchRight) {
                                       if (touchLeft) {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_E", posx.cpy(), true, null));
                                       } else if (touchRight) {
                                          var75.put(posx.cpy(), new Tile("pkmnmansion_wall_W", posx.cpy(), true, null));
                                       }
                                    }
                                 }
                              }
                           }

                           prevStatue = statue;
                           stairsUp = nextStairsUp;
                           continue label1347;
                        }

                        Vector2 posx = (Vector2)var102.next();
                        currTile = currLayer.get(posx);
                        if (currTile.isSolid) {
                           length = 0;
                           checkDirs = new ArrayList<>();
                           if (currTile.name.equals("pkmnmansion_wall_S")) {
                              checkDirs.add(new Vector2(-16.0F, 0.0F));
                              checkDirs.add(new Vector2(0.0F, -16.0F));
                              checkDirs.add(new Vector2(16.0F, 0.0F));
                              break;
                           }

                           if (currTile.name.equals("pkmnmansion_wall_N")) {
                              checkDirs.add(new Vector2(-16.0F, 0.0F));
                              checkDirs.add(new Vector2(0.0F, 16.0F));
                              checkDirs.add(new Vector2(16.0F, 0.0F));
                              break;
                           }

                           if (currTile.name.equals("pkmnmansion_wall_W")) {
                              checkDirs.add(new Vector2(-16.0F, 0.0F));
                              checkDirs.add(new Vector2(0.0F, 16.0F));
                              checkDirs.add(new Vector2(0.0F, -16.0F));
                              break;
                           }

                           if (currTile.name.equals("pkmnmansion_wall_NW")) {
                              checkDirs.add(new Vector2(-16.0F, 0.0F));
                              checkDirs.add(new Vector2(0.0F, 16.0F));
                              break;
                           }

                           if (currTile.name.equals("pkmnmansion_wall_E")) {
                              checkDirs.add(new Vector2(16.0F, 0.0F));
                              checkDirs.add(new Vector2(0.0F, 16.0F));
                              checkDirs.add(new Vector2(0.0F, -16.0F));
                              break;
                           }

                           if (currTile.name.equals("pkmnmansion_wall_NE")) {
                              checkDirs.add(new Vector2(16.0F, 0.0F));
                              checkDirs.add(new Vector2(0.0F, 16.0F));
                              break;
                           }
                        }
                     }

                     for (Vector2 dir : checkDirs) {
                        ArrayList<Tile> door = new ArrayList<>();
                        Vector2 initialDir = dir.cpy();

                        while (true) {
                           Tile var142 = currLayer.get(currTile.position.cpy().add(dir));
                           if (var142 != currTile
                              && (
                                 var142 == null
                                    || var142.name.contains("rug")
                                    || var142.name.contains("stairs")
                                    || var142.nameUpper.contains("stairs")
                                    || var142.isSolid && !var142.nameUpper.contains("pokeball")
                              )) {
                              if (var142 != null
                                 && (
                                    !var142.name.contains("pkmnmansion_wall") && !var142.nameUpper.contains("rubble")
                                       || alreadyChecked.contains(var142.position)
                                 )) {
                                 var142 = null;
                              }

                              if (var142 != null && length < 7) {
                                 allDoors.add(door);
                              }
                              break;
                           }

                           String name = "pkmnmansion_gate_EW__on";
                           if (dir.x == 0.0F) {
                              name = "pkmnmansion_gate_NS__on";
                           }

                           door.add(new Tile(name, var142.position.cpy(), true, null));
                           dir = dir.add(initialDir);
                           length++;
                        }
                     }
                  }
               }
            }

            return;
         }

         yMax++;
      }
   }

   public boolean isPathBetween(HashMap<Vector2, Tile> currLayer, Tile tileA, Tile tileB) {
      ArrayList<Tile> checkTiles = new ArrayList<>();
      ArrayList<Vector2> alreadyChecked = new ArrayList<>();
      checkTiles.add(tileA);

      while (checkTiles.size() > 0) {
         Tile tile = checkTiles.remove(0);
         if (tile != null && tile.position.equals(tileB.position)) {
            return true;
         }

         if (tile == tileA || tile != null && (!tile.isSolid || tile.nameUpper.contains("pokeball")) && !alreadyChecked.contains(tile.position)) {
            alreadyChecked.add(tile.position);
            checkTiles.add(currLayer.get(tile.position.cpy().add(-16.0F, 0.0F)));
            checkTiles.add(currLayer.get(tile.position.cpy().add(16.0F, 0.0F)));
            checkTiles.add(currLayer.get(tile.position.cpy().add(0.0F, -16.0F)));
            checkTiles.add(currLayer.get(tile.position.cpy().add(0.0F, 16.0F)));
         }
      }

      return false;
   }

   public void ledgify(HashMap<Vector2, Tile> tilesToAdd, HashMap<Vector2, Tile> nextIslandTiles, String tileType, String replaceWith, String biome) {
      this.ledgify(tilesToAdd, nextIslandTiles, tileType, replaceWith, biome, "black1");
   }

   public boolean riverContains(Vector2 position) {
      float scale = 60.0F;
      int threshold = 850;
      Vector2 rotate = position.cpy().rotateDeg(-((float)Math.toDegrees(Math.atan(position.x / position.y))));
      float wavelength = 4.0F;
      float amplitude = 4.0F;
      float offset3 = (float)Math.sin(rotate.x / (scale * wavelength));
      offset3 *= amplitude;
      float offset1 = Math.abs(Math.floorMod((int)(rotate.y / scale - offset3), 14) - 7);
      offset1 *= 2.0F;
      float offset2 = Math.abs(Math.floorMod((int)(rotate.y / scale - offset3), 20) - 10);
      offset2 *= 2.0F;
      return offset1 * 5.0F * offset2 > threshold;
   }

   public void ledgify(
      HashMap<Vector2, Tile> tilesToAdd, HashMap<Vector2, Tile> nextIslandTiles, String tileType, String replaceWith, String biome, String nullType
   ) {
      boolean done = false;
      String ledgeType = "ledges3";
      if (tileType.contains("volcano")) {
         ledgeType = "ledges3volcano";
      }

      while (!done) {
         done = true;

         for (Tile tile : nextIslandTiles.values()) {
            if (tile.name.equals(tileType)) {
               Tile bl = tilesToAdd.get(tile.position.cpy().add(-16.0F, -16.0F));
               Tile bot = tilesToAdd.get(tile.position.cpy().add(0.0F, -16.0F));
               Tile br = tilesToAdd.get(tile.position.cpy().add(16.0F, -16.0F));
               Tile left = tilesToAdd.get(tile.position.cpy().add(-16.0F, 0.0F));
               Tile tl = tilesToAdd.get(tile.position.cpy().add(-16.0F, 16.0F));
               Tile top = tilesToAdd.get(tile.position.cpy().add(0.0F, 16.0F));
               Tile tr = tilesToAdd.get(tile.position.cpy().add(16.0F, 16.0F));
               Tile right = tilesToAdd.get(tile.position.cpy().add(16.0F, 0.0F));
               if (bl == null) {
                  bl = new Tile(nullType, tile.position.cpy());
               }

               if (bot == null) {
                  bot = new Tile(nullType, tile.position.cpy());
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

               if (top == null) {
                  top = new Tile(nullType, tile.position.cpy());
               }

               if (tr == null) {
                  tr = new Tile(nullType, tile.position.cpy());
               }

               if (right == null) {
                  right = new Tile(nullType, tile.position.cpy());
               }

               Tile newTile = tilesToAdd.get(tile.position);
               if (newTile == null || newTile.name.equals(tileType)) {
                  if (!left.name.equals(tileType) && !right.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (!top.name.equals(tileType) && !bot.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (!top.name.equals(tileType) && !left.name.equals(tileType) && !br.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (!top.name.equals(tileType) && !right.name.equals(tileType) && !bl.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (!bot.name.equals(tileType) && !right.name.equals(tileType) && !tl.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (!bot.name.equals(tileType) && !left.name.equals(tileType) && !tr.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (bot.name.equals(tileType) && !left.name.equals(tileType) && !br.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (bot.name.equals(tileType) && !right.name.equals(tileType) && !bl.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (top.name.equals(tileType) && !left.name.equals(tileType) && !tr.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (top.name.equals(tileType) && !right.name.equals(tileType) && !tl.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (right.name.equals(tileType) && !top.name.equals(tileType) && !br.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (right.name.equals(tileType) && !bot.name.equals(tileType) && !tr.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (left.name.equals(tileType) && !bot.name.equals(tileType) && !tl.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }

                  if (left.name.equals(tileType) && !top.name.equals(tileType) && !bl.name.equals(tileType)) {
                     newTile = new Tile(replaceWith, tile.position.cpy());
                     newTile.biome = biome;
                     tilesToAdd.put(tile.position.cpy(), newTile);
                     nextIslandTiles.put(tile.position.cpy(), newTile);
                     done = false;
                  }
               }
            }
         }
      }

      for (Tile tile : nextIslandTiles.values()) {
         Tile bl = tilesToAdd.get(tile.position.cpy().add(-16.0F, -16.0F));
         Tile down = tilesToAdd.get(tile.position.cpy().add(0.0F, -16.0F));
         Tile br = tilesToAdd.get(tile.position.cpy().add(16.0F, -16.0F));
         Tile left = tilesToAdd.get(tile.position.cpy().add(-16.0F, 0.0F));
         Tile tl = tilesToAdd.get(tile.position.cpy().add(-16.0F, 16.0F));
         Tile up = tilesToAdd.get(tile.position.cpy().add(0.0F, 16.0F));
         Tile tr = tilesToAdd.get(tile.position.cpy().add(16.0F, 16.0F));
         Tile right = tilesToAdd.get(tile.position.cpy().add(16.0F, 0.0F));
         tile = tilesToAdd.get(tile.position);
         if (tile != null && tile.name.equals(tileType)) {
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

            if (left.name.equals(tileType) && !down.name.equals(tileType) && right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_N", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (up.name.equals(tileType) && right.name.equals(tileType) && !down.name.equals(tileType) && !left.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_NE", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (up.name.equals(tileType) && !left.name.equals(tileType) && down.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_E", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!up.name.equals(tileType) && !left.name.equals(tileType) && right.name.equals(tileType) && down.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_SE", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!up.name.equals(tileType) && left.name.equals(tileType) && right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_S", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!up.name.equals(tileType) && down.name.equals(tileType) && left.name.equals(tileType) && !right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_SW", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (up.name.equals(tileType) && down.name.equals(tileType) && !right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_W", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (up.name.equals(tileType) && left.name.equals(tileType) && !down.name.equals(tileType) && !right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_NW", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!br.name.equals(tileType) && down.name.equals(tileType) && right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_NW_inner", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!bl.name.equals(tileType) && down.name.equals(tileType) && left.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_NE_inner", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!tr.name.equals(tileType) && up.name.equals(tileType) && right.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_SW_inner", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            } else if (!tl.name.equals(tileType) && up.name.equals(tileType) && left.name.equals(tileType)) {
               Tile newTile = new Tile(tileType, ledgeType + "_SE_inner", tile.position.cpy());
               newTile.biome = biome;
               tilesToAdd.put(tile.position.cpy(), newTile);
            }
         }
      }
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      game.map.tiles.putAll(this.tilesToAdd);
      this.tilesToAdd.clear();

      for (int i = 0; i < this.interiorTilesToAdd.size(); i++) {
         HashMap<Vector2, Tile> tiles = this.interiorTilesToAdd.get(i);
         if (tiles != null) {
            if (game.map.interiorTiles.get(i) == null) {
               game.map.interiorTiles.remove(i);
               game.map.interiorTiles.add(i, new HashMap<>());
            }

            System.out.println(i);
            game.map.interiorTiles.get(i).putAll(this.interiorTilesToAdd.get(i));
         }
      }

      this.interiorTilesToAdd.clear();

      for (Pokemon pokemon : this.pokemonToAdd.values()) {
         game.insertAction(pokemon.standingAction);
         if (pokemon.specie.name.contains("nido") && pokemon.gender.equals("female")) {
            game.map.tiles.put(pokemon.position.cpy(), new Tile("mountain3", pokemon.position.cpy(), true));
         } else if (pokemon.specie.name.equals("charizard") && pokemon.gender.equals("female")) {
            game.map.tiles.put(pokemon.position.cpy(), new Tile("green1", "campfire1", pokemon.position.cpy(), true));
         }
      }

      this.pokemonToAdd.clear();

      for (Pokemon pokemon : this.interiorPokemon.keySet()) {
         if (!game.map.pokemon.containsKey(pokemon.position)) {
            int interiorIndex = this.interiorPokemon.get(pokemon);
            game.insertAction(pokemon.standingAction);
            pokemon.mapTiles = game.map.interiorTiles.get(interiorIndex);
            pokemon.interiorIndex = interiorIndex;
            game.map.pokemon.put(pokemon.position.cpy(), pokemon);
         }
      }

      this.interiorPokemon.clear();

      for (Vector2 position : this.trapinchSpawns) {
         Tile tile = game.map.overworldTiles.get(position);
         if (tile != null && tile.name.equals("desert2")) {
            Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
            trapinch.mapTiles = game.map.overworldTiles;
            trapinch.position = position.cpy();
            trapinch.isTrapping = true;
            game.insertAction(trapinch.new Burrowed());
            tile.items().put("trapinch", 1);
         }
      }

      this.trapinchSpawns.clear();
      if (this.tilesToAdd.isEmpty()) {
         if (!this.doActions.isEmpty()) {
            Action currAction = this.doActions.get(0);
            this.doActions.remove(0);
            game.insertAction(currAction);
            game.actionStack.remove(this);
         } else {
            game.actionStack.remove(this);

            for (Tile edgeTile : this.edges) {
               game.map.edges.add(edgeTile.position.cpy());
            }

            Vector2 startLoc = this.edges.get(game.map.rand.nextInt(this.edges.size())).position;
            game.map.bottomLeft = this.bottomLeft;
            game.map.topRight = this.topRight;
            game.player.position.set(startLoc);
            game.player.spawnLoc.set(startLoc);
            game.player.spawnIndex = -1;
            game.cam.position.set(startLoc.x + 16.0F, startLoc.y, 0.0F);
         }
      }
   }

   public enum Biome {
      DESERT1 {
         @Override
         public Tile buildTile(Vector2 position) {
            return new Tile("", position, true);
         }
      },
      ISLAND1 {
         @Override
         public Tile buildTile(Vector2 position) {
            return new Tile("", position, true);
         }
      };

      Biome() {
      }

      public abstract Tile buildTile(Vector2 var1);
   }
}
