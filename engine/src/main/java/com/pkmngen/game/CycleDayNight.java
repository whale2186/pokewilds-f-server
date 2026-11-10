package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

class CycleDayNight extends Action {
   public static int dayTimer = 18000;
   public Action.Layer layer = Action.Layer.gui_114;
   int[] currFrames;
   public static boolean fadeToDay;
   public static boolean fadeToNight;
   int animIndex;
   public static AnimationContainer<Color> animContainer;
   public static AnimationContainer<Color> fadeToDayAnim;
   Random rand;
   int countDownToGhost;
   private int lightCheckTimer;
   Sprite bgSprite;
   String text;
   int signCounter;
   int day;
   int night;
   int countDownToCacturne = 200;
   int countDownToGengar = 2000;
   Vector2 startPos;
   Vector2 endPos;
   ArrayList<Tile> tidalTiles = new ArrayList<>();
   ArrayList<Tile> changeThese = new ArrayList<>();
   public boolean lowerTide = false;
   boolean doneTidalSound = true;
   int tidalTimer = 0;

   public CycleDayNight(Game game) {
      super();
      this.day = 1;
      this.night = 0;
      animContainer = new AnimationContainer<>();
      animContainer.add(new Color(1.0F, 0.9F, 0.5F, 1.0F), 80);
      animContainer.add(new Color(0.9F, 0.55F, 0.2F, 1.0F), 80);
      animContainer.add(new Color(0.4F, 0.4F, 0.5F, 1.0F), 80);
      animContainer.add(new Color(PkmnMap.nightColor), 80);
      fadeToDayAnim = new AnimationContainer<>();
      fadeToDayAnim.add(new Color(PkmnMap.nightColor), 80);
      fadeToDayAnim.add(new Color(0.5F, 0.5F, 0.6F, 1.0F), 80);
      fadeToDayAnim.add(new Color(0.8F, 0.8F, 0.8F, 1.0F), 80);
      fadeToDayAnim.add(Color.WHITE, 80);
      fadeToDay = false;
      fadeToNight = false;
      this.animIndex = 0;
      this.rand = new Random();
      Texture text = TextureCache.get(Gdx.files.internal("text2.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      this.bgSprite.setPosition(0.0F, 24.0F);
      this.text = game.map.timeOfDay + ": ";
      this.countDownToGhost = this.rand.nextInt(500) + 500;
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   // Ghost encounters belong to each player; only the shared clock needs an authority.
   private void updateGhosts(Game game) {
      if (game.map.tiles != game.map.overworldTiles || !"night".equals(game.map.timeOfDay) || !game.playerCanMove
            || game.coop != null && (game.coop.hud.blocksInput() || game.coop.spectator.blocksInput() || game.coop.interactionBusy())) return;
      if (this.lightCheckTimer-- <= 0) { game.player.isNearCampfire = game.player.checkNearCampfire(); this.lightCheckTimer = 30; }
      if (game.player.isNearCampfire) return;
      for (Action action : game.actionStack) {
         if (action instanceof DrawGhost || action instanceof SpawnGhost) return;
      }
      Tile ground = game.map.tiles.get(new Vector2((float)Math.floor(game.player.position.x/16)*16,(float)Math.floor(game.player.position.y/16)*16));
      if (!isGhostBiome(game, ground)) return;
      if (--this.countDownToGhost <= 0) {
         Vector2 position = game.player.position.cpy().add(this.rand.nextInt(5)*16-48,this.rand.nextInt(5)*16-48);
         boolean graveyard = isGraveyard(game, ground);
         game.insertAction(new SpawnGhost(game, position, graveyard));
         this.countDownToGhost = this.rand.nextInt(2000) + 1000;
      }
   }

   private static boolean isGhostBiome(Game game, Tile ground) {
      if (isGhostBiomeName(game.map.currBiome)) return true;
      if (game.map.currRoute != null && (isGhostBiomeName(game.map.currRoute.name) || isGhostBiomeName(game.map.currRoute.type()))) return true;
      if (ground != null) {
         if (isGhostBiomeName(ground.biome)) return true;
         if (ground.routeBelongsTo != null && (isGhostBiomeName(ground.routeBelongsTo.name) || isGhostBiomeName(ground.routeBelongsTo.type()))) return true;
         if (isGhostBiomeName(ground.name) || isGhostBiomeName(ground.nameUpper)) return true;
      }
      return false;
   }

   private static boolean isGhostBiomeName(String name) {
      if (name == null || name.isEmpty()) return false;
      String lower = name.toLowerCase(java.util.Locale.ROOT);
      return lower.contains("deep_forest") || lower.contains("graveyard") || lower.contains("wooded_lake") || lower.contains("woodedlake");
   }

   private static boolean isGraveyard(Game game, Tile ground) {
      if (game.map.currBiome != null && game.map.currBiome.contains("graveyard")) return true;
      if (game.map.currRoute != null && game.map.currRoute.name != null && game.map.currRoute.name.contains("graveyard")) return true;
      if (ground != null) {
         if (ground.biome != null && ground.biome.contains("graveyard")) return true;
         if (ground.routeBelongsTo != null && ground.routeBelongsTo.name != null && ground.routeBelongsTo.name.contains("graveyard")) return true;
         if (ground.nameUpper != null && ground.nameUpper.contains("gravestone")) return true;
      }
      return false;
   }

   @Override
   public void step(Game game) {
      updateGhosts(game);
      if (game.coop != null && game.coop.isGuest()) return;
      dayTimer--;
      if (dayTimer <= 0) {
         if (game.map.timeOfDay.equals("day")) {
            fadeToNight = true;
            dayTimer = 5000;
            game.map.timeOfDay = "night";
         } else if (game.map.timeOfDay.equals("night")) {
            fadeToDay = true;
            dayTimer = 18000;
            game.map.timeOfDay = "day";
         }
      }

      if (fadeToDay) {
         if (!game.map.currRoute.isDungeon) {
            game.mapBatch.setColor(fadeToDayAnim.currentThing());
         }

         this.animIndex++;
         if (this.animIndex >= fadeToDayAnim.currentFrame()) {
            fadeToDayAnim.index++;
            this.animIndex = 0;
         }

         if (fadeToDayAnim.index >= fadeToDayAnim.animateThese.size()) {
            fadeToDay = false;
            game.map.timeOfDay = "day";
            fadeToDayAnim.index = 0;
            this.day++;
            this.signCounter = 300;
            this.bgSprite.setPosition(0.0F, 24.0F);
            game.musicController.startTimeOfDay = "day";
            if (!game.map.currRoute.isDungeon) {
               game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            }

            if (game.type != Game.Type.CLIENT) {
               int numRocks = 0;
               ArrayList<Tile> tidalTiles = new ArrayList<>();
               int numTidalHoles = 0;
               ArrayList<Tile> desertTiles = new ArrayList<>();
               int numFossils = 0;
               ArrayList<Tile> forestTiles = new ArrayList<>();
               int numForestHoles = 0;
               ArrayList<Tile> deepForestTiles = new ArrayList<>();
               int numDeepForestHoles = 0;
               ArrayList<Tile> graveyardTiles = new ArrayList<>();
               int numGraveyardHoles = 0;
               ArrayList<Tile> beachTiles = new ArrayList<>();
               int numBeachHoles = 0;
               ArrayList<Tile> mountainTiles = new ArrayList<>();
               int numMountainHoles = 0;
               ArrayList<Tile> snowTiles = new ArrayList<>();
               int numSnowHoles = 0;
               ArrayList<Tile> volcanoTiles = new ArrayList<>();
               int numVolcanoHoles = 0;
               ArrayList<Vector2> desertEdges = new ArrayList<>();
               HashMap<Vector2, Tile> interiorTiles2 = game.map.interiorTiles.get(100);
               if (interiorTiles2 != null) {
                  for (Tile tile : interiorTiles2.values()) {
                     if (!tile.nameUpper.contains("tree_planted") && !tile.nameUpper.equals("bush2_color_fertilized")) {
                        if (tile.nameUpper.equals("berry_planted")) {
                           String nameUpper;
                           if (tile.name.contains("desert")) {
                              nameUpper = "rawst";
                           } else if (tile.name.contains("sand")) {
                              nameUpper = "pecha";
                           } else if (tile.name.contains("snow")) {
                              nameUpper = "aspear";
                           } else if (tile.name.equals("green9") || tile.name.equals("green10") || tile.name.equals("green11") || tile.name.equals("green12")) {
                              nameUpper = "chesto";
                           } else if (tile.name.contains("savanna")) {
                              nameUpper = "cheri";
                           } else if (tile.name.contains("volcano")) {
                              nameUpper = "lum";
                           } else {
                              nameUpper = "persim";
                           }

                           tile.nameUpper = "berrytree_" + nameUpper + "_mid";
                           tile.init();
                        } else if (!tile.nameUpper.contains("berrytree_") || !tile.nameUpper.contains("_mid") && !tile.nameUpper.contains("_empty")) {
                           if (tile.nameUpper.equals("grass_planted")) {
                              Tile grass;
                              if (tile.name.contains("desert")) {
                                 grass = new Tile("grass_sand3", tile.position.cpy(), true, tile.routeBelongsTo);
                              } else if (tile.name.contains("sand")) {
                                 grass = new Tile("grass_sand1", tile.position.cpy(), true, tile.routeBelongsTo);
                              } else if (tile.name.contains("snow")) {
                                 grass = new Tile("grass3", tile.position.cpy(), true, tile.routeBelongsTo);
                              } else if (tile.name.contains("green_savanna")) {
                                 grass = new Tile(tile.name, "grass_savanna1", tile.position.cpy(), true, tile.routeBelongsTo);
                              } else if (!tile.name.equals("flower5")
                                 && !tile.name.equals("green9")
                                 && !tile.name.equals("green10")
                                 && !tile.name.equals("green11")
                                 && !tile.name.equals("green12")) {
                                 grass = new Tile("grass2", tile.position.cpy(), true, tile.routeBelongsTo);
                              } else {
                                 grass = new Tile("grass_graveyard1", tile.position.cpy(), true, tile.routeBelongsTo);
                              }

                              interiorTiles2.put(tile.position, grass);
                           }
                        } else {
                           tile.nameUpper = tile.nameUpper.replace("_mid", "_full");
                           tile.nameUpper = tile.nameUpper.replace("_empty", "_full");
                           tile.init();
                        }
                     } else {
                        Tile tree;
                        if (tile.name.contains("desert")) {
                           tree = new Tile(tile.name, "tree7", tile.position.cpy(), true, tile.routeBelongsTo);
                        } else if (tile.name.contains("sand")) {
                           tree = new Tile(tile.name, "tree6", tile.position.cpy(), true, tile.routeBelongsTo);
                        } else if (tile.name.contains("snow")) {
                           tree = new Tile(tile.name, "tree4", tile.position.cpy(), true, tile.routeBelongsTo);
                        } else if (tile.nameUpper.equals("bush2_color_fertilized")) {
                           tree = new Tile("green1", "tree2", tile.position.cpy(), true, tile.routeBelongsTo);
                        } else {
                           tree = new Tile("bush1", "", tile.position.cpy(), true, tile.routeBelongsTo);
                        }

                        interiorTiles2.put(tile.position, tree);
                        int amt = 1;
                        if (tile.nameUpper.contains("fertilized")) {
                           amt = 2;
                        }

                        String[] items = new String[]{
                           "black apricorn", "blue apricorn", "green apricorn", "pink apricorn", "red apricorn", "white apricorn", "yellow apricorn"
                        };
                        boolean found = false;
                        String[] pokemon = items;
                        int trapinch = pokemon.length;
                        int gender = 0;

                        while (true) {
                           if (gender < trapinch) {
                              String item = pokemon[gender];
                              if (!tree.items().containsKey(item)) {
                                 gender++;
                                 continue;
                              }

                              tree.items().put(item, tree.items().get(item) + amt);
                              found = true;
                           }

                           if (!found) {
                              tree.items().put(items[game.map.rand.nextInt(items.length)], 2);
                           }
                           break;
                        }
                     }
                  }
               }

               for (Tile tile : game.map.overworldTiles.values()) {
                  if (!tile.nameUpper.contains("tree_planted") && !tile.nameUpper.equals("bush2_color_fertilized")) {
                     if (tile.nameUpper.equals("berry_planted")) {
                        String nameUpper;
                        if (tile.name.contains("desert")) {
                           nameUpper = "rawst";
                        } else if (tile.name.contains("sand")) {
                           nameUpper = "pecha";
                        } else if (tile.name.contains("snow")) {
                           nameUpper = "aspear";
                        } else if (tile.name.equals("green9") || tile.name.equals("green10") || tile.name.equals("green11") || tile.name.equals("green12")) {
                           nameUpper = "chesto";
                        } else if (tile.name.contains("savanna")) {
                           nameUpper = "cheri";
                        } else if (tile.name.contains("volcano")) {
                           nameUpper = "lum";
                        } else {
                           nameUpper = "persim";
                        }

                        tile.nameUpper = "berrytree_" + nameUpper + "_mid";
                        tile.init();
                     } else if (!tile.nameUpper.contains("berrytree_") || !tile.nameUpper.contains("_mid") && !tile.nameUpper.contains("_empty")) {
                        if (tile.nameUpper.equals("grass_planted")) {
                           Tile grass;
                           if (tile.name.contains("desert")) {
                              grass = new Tile("grass_sand3", tile.position.cpy(), true, tile.routeBelongsTo);
                           } else if (tile.name.contains("sand")) {
                              grass = new Tile("grass_sand1", tile.position.cpy(), true, tile.routeBelongsTo);
                           } else if (tile.name.contains("snow")) {
                              grass = new Tile("grass3", tile.position.cpy(), true, tile.routeBelongsTo);
                           } else if (tile.name.contains("green_savanna")) {
                              grass = new Tile(tile.name, "grass_savanna1", tile.position.cpy(), true, tile.routeBelongsTo);
                           } else if (!tile.name.equals("flower5")
                              && !tile.name.equals("green9")
                              && !tile.name.equals("green10")
                              && !tile.name.equals("green11")
                              && !tile.name.equals("green12")) {
                              grass = new Tile("grass2", tile.position.cpy(), true, tile.routeBelongsTo);
                           } else {
                              grass = new Tile("grass_graveyard1", tile.position.cpy(), true, tile.routeBelongsTo);
                           }

                           game.map.overworldTiles.put(tile.position, grass);
                        } else if (tile.nameUpper.equals("cactus2_cacturne")) {
                           tile.nameUpper = "";
                           tile.init();
                        } else if (!tile.name.equals("desert2_trapinch_spawn")) {
                           if (tile.nameUpper.equals("rock1_color")) {
                              numRocks++;
                           } else if (tile.name.equals("sand3_desertEdge")) {
                              desertEdges.add(tile.position.cpy());
                           } else if (tile.name.contains("desert")) {
                              if (tile.nameUpper.equals("")) {
                                 desertTiles.add(tile);
                              } else if (tile.nameUpper.equals("desert4_cracked")) {
                                 numFossils++;
                              }
                           } else if (!tile.name.equals("green11") && !tile.name.equals("green12")) {
                              if (tile.name.equals("green1")) {
                                 if (tile.nameUpper.equals("")) {
                                    if (tile.biome.equals("deep_forest")) {
                                       deepForestTiles.add(tile);
                                    } else {
                                       forestTiles.add(tile);
                                    }
                                 } else if (tile.nameUpper.equals("forest_cracked")) {
                                    numForestHoles++;
                                 } else if (tile.nameUpper.equals("deepforest_cracked")) {
                                    numDeepForestHoles++;
                                 }
                              } else if (tile.name.equals("sand1")) {
                                 if (tile.nameUpper.equals("")) {
                                    beachTiles.add(tile);
                                 } else if (tile.nameUpper.equals("beach_cracked")) {
                                    numBeachHoles++;
                                 }
                              } else if (tile.name.equals("mountain3")) {
                                 if (tile.nameUpper.equals("")) {
                                    mountainTiles.add(tile);
                                 } else if (tile.nameUpper.equals("mountain3_cracked")) {
                                    numMountainHoles++;
                                 }
                              } else if (!tile.name.equals("snow1") && !tile.name.equals("snow2") && !tile.name.equals("snow5") && !tile.name.equals("snow6")) {
                                 if (tile.name.equals("volcano1")) {
                                    if (tile.nameUpper.equals("")) {
                                       volcanoTiles.add(tile);
                                    } else if (tile.nameUpper.equals("volcano_cracked")) {
                                       numVolcanoHoles++;
                                    }
                                 } else if (tile.name.contains("_tidal")) {
                                    if (tile.nameUpper.equals("")) {
                                       tidalTiles.add(tile);
                                    } else if (tile.nameUpper.equals("tidal1_cracked")) {
                                       numTidalHoles++;
                                    }
                                 } else if (numRocks < 7
                                    && Game.rand.nextInt(200) >= 197
                                    && !game.map.pokemon.containsKey(tile.position)
                                    && tile.name.contains("mountain")
                                    && !tile.isLedge
                                    && !tile.isSolid
                                    && tile.nameUpper.equals("")
                                    && tile.items().isEmpty()) {
                                    int level = 22;
                                    if (tile.routeBelongsTo != null) {
                                       level = tile.routeBelongsTo.level;
                                    }

                                    Route tempRoute = new Route("rock_smash1", level);
                                    Tile rockTile = new Tile(tile.name, "rock1_color", tile.position.cpy(), true, tempRoute);
                                    game.map.overworldTiles.put(tile.position, rockTile);
                                    numRocks++;
                                 }
                              } else if (tile.nameUpper.equals("")) {
                                 snowTiles.add(tile);
                              } else if (tile.nameUpper.equals("snow_cracked")) {
                                 numSnowHoles++;
                              }
                           } else if (tile.nameUpper.equals("")) {
                              graveyardTiles.add(tile);
                              Tile up = game.map.overworldTiles.get(tile.position.cpy().add(0.0F, 16.0F));
                              if (up != null && up.nameUpper.equals("gravestone2")) {
                                 for (int i = 0; i < 55; i++) {
                                    graveyardTiles.add(tile);
                                 }
                              }
                           } else if (tile.nameUpper.equals("graveyard_cracked")) {
                              numGraveyardHoles++;
                           }
                        } else {
                           Vector2 startPos = tile.position.cpy().add(-64.0F, -64.0F);
                           startPos.x = (int)startPos.x - (int)startPos.x % 16;
                           startPos.y = (int)startPos.y - (int)startPos.y % 16;
                           Vector2 endPos = tile.position.cpy().add(64.0F, 64.0F);
                           endPos.x = (int)endPos.x - (int)endPos.x % 16;
                           endPos.y = (int)endPos.y - (int)endPos.y % 16;
                           int numFound = 0;
                           Vector2 currPos = new Vector2(startPos.x, startPos.y);

                           while (currPos.y < endPos.y) {
                              Tile nextTile = game.map.overworldTiles.get(currPos);
                              currPos.x += 16.0F;
                              if (currPos.x > endPos.x) {
                                 currPos.x = startPos.x;
                                 currPos.y += 16.0F;
                              }

                              if (nextTile != null && nextTile.name.contains("desert2") && nextTile.items().containsKey("trapinch")) {
                                 numFound++;
                              }
                           }

                           currPos = new Vector2(startPos.x, startPos.y);

                           while (currPos.y < endPos.y && numFound < 3) {
                              Tile nextTile = game.map.overworldTiles.get(currPos);
                              currPos.x += 16.0F;
                              if (currPos.x > endPos.x) {
                                 currPos.x = startPos.x;
                                 currPos.y += 16.0F;
                              }

                              if (nextTile != null
                                 && nextTile.name.contains("desert2")
                                 && !nextTile.items().containsKey("trapinch")
                                 && Game.rand.nextInt(256) < 32) {
                                 numFound++;
                                 Pokemon trapinch = new Pokemon("trapinch", 22, Pokemon.Generation.CRYSTAL);
                                 trapinch.isTrapping = true;
                                 trapinch.position = nextTile.position.cpy();
                                 game.insertAction(trapinch.new Burrowed());
                                 trapinch.mapTiles = game.map.overworldTiles;
                                 nextTile.items().put("trapinch", 1);
                              }
                           }
                        }
                     } else {
                        tile.nameUpper = tile.nameUpper.replace("_mid", "_full");
                        tile.nameUpper = tile.nameUpper.replace("_empty", "_full");
                        tile.init();
                     }
                  } else {
                     Tile tree;
                     if (tile.name.contains("desert")) {
                        tree = new Tile(tile.name, "tree7", tile.position.cpy(), true, tile.routeBelongsTo);
                     } else if (tile.name.contains("sand")) {
                        tree = new Tile(tile.name, "tree6", tile.position.cpy(), true, tile.routeBelongsTo);
                     } else if (tile.name.contains("snow")) {
                        tree = new Tile(tile.name, "tree4", tile.position.cpy(), true, tile.routeBelongsTo);
                     } else if (tile.nameUpper.equals("bush2_color_fertilized")) {
                        tree = new Tile("green1", "tree2", tile.position.cpy(), true, tile.routeBelongsTo);
                     } else {
                        tree = new Tile("bush1", "", tile.position.cpy(), true, tile.routeBelongsTo);
                     }

                     game.map.overworldTiles.put(tile.position, tree);
                     int amt = 1;
                     if (tile.nameUpper.contains("fertilized")) {
                        amt = 2;
                     }

                     String[] items = new String[]{
                        "black apricorn", "blue apricorn", "green apricorn", "pink apricorn", "red apricorn", "white apricorn", "yellow apricorn"
                     };
                     boolean found = false;
                     String[] var114 = items;
                     int var120 = var114.length;
                     int var124 = 0;

                     while (true) {
                        if (var124 < var120) {
                           String item = var114[var124];
                           if (!tree.items().containsKey(item)) {
                              var124++;
                              continue;
                           }

                           tree.items().put(item, tree.items().get(item) + amt);
                           found = true;
                        }

                        if (!found) {
                           tree.items().put(items[game.map.rand.nextInt(items.length)], 2);
                        }
                        break;
                     }
                  }
               }

               while (numFossils < 10 && desertTiles.size() > 0) {
                  Tile fossilTile = desertTiles.remove(Game.rand.nextInt(desertTiles.size()));
                  fossilTile.nameUpper = "desert4_cracked";
                  fossilTile.init();
                  numFossils++;
               }

               desertTiles.clear();

               while (numForestHoles < 6 && forestTiles.size() > 0) {
                  Tile evoStoneTile = forestTiles.remove(Game.rand.nextInt(forestTiles.size()));
                  evoStoneTile.nameUpper = "forest_cracked";
                  evoStoneTile.init();
                  numForestHoles++;
               }

               forestTiles.clear();

               while (numDeepForestHoles < 6 && deepForestTiles.size() > 0) {
                  Tile evoStoneTile = deepForestTiles.remove(Game.rand.nextInt(deepForestTiles.size()));
                  evoStoneTile.nameUpper = "deepforest_cracked";
                  evoStoneTile.init();
                  numDeepForestHoles++;
               }

               deepForestTiles.clear();

               while (numGraveyardHoles < 6 && graveyardTiles.size() > 0) {
                  Tile evoStoneTile = graveyardTiles.remove(Game.rand.nextInt(graveyardTiles.size()));
                  evoStoneTile.nameUpper = "graveyard_cracked";
                  evoStoneTile.init();
                  numGraveyardHoles++;
               }

               graveyardTiles.clear();

               while (numSnowHoles < 6 && snowTiles.size() > 0) {
                  Tile evoStoneTile = snowTiles.remove(Game.rand.nextInt(snowTiles.size()));
                  evoStoneTile.nameUpper = "snow_cracked";
                  evoStoneTile.init();
                  numSnowHoles++;
               }

               snowTiles.clear();

               while (numBeachHoles < 6 && beachTiles.size() > 0) {
                  Tile evoStoneTile = beachTiles.remove(Game.rand.nextInt(beachTiles.size()));
                  evoStoneTile.nameUpper = "beach_cracked";
                  evoStoneTile.init();
                  numBeachHoles++;
               }

               beachTiles.clear();

               while (numMountainHoles < 6 && mountainTiles.size() > 0) {
                  Tile evoStoneTile = mountainTiles.remove(Game.rand.nextInt(mountainTiles.size()));
                  evoStoneTile.nameUpper = "mountain3_cracked";
                  evoStoneTile.init();
                  numMountainHoles++;
               }

               mountainTiles.clear();

               while (numVolcanoHoles < 2 && volcanoTiles.size() > 0) {
                  Tile evoStoneTile = volcanoTiles.remove(Game.rand.nextInt(volcanoTiles.size()));
                  evoStoneTile.nameUpper = "volcano_cracked";
                  evoStoneTile.init();
                  numVolcanoHoles++;
               }

               volcanoTiles.clear();

               while (numTidalHoles < 24 && tidalTiles.size() > 0) {
                  Tile evoStoneTile = tidalTiles.remove(Game.rand.nextInt(tidalTiles.size()));
                  evoStoneTile.nameUpper = "tidal1_cracked";
                  evoStoneTile.init();
                  numTidalHoles++;
               }

               tidalTiles.clear();

               for (HashMap<Vector2, Tile> interiorTiles : game.map.interiorTiles) {
                  if (interiorTiles != null) {
                     for (Tile tile : interiorTiles.values()) {
                        if (tile.nameUpper.equals("mewtwo_overworld_hidden")) {
                           tile.isSolid = true;
                           tile.nameUpper = "mewtwo_overworld";
                           Texture text = TextureCache.get(Gdx.files.internal("tiles/mewtwo_overworld.png"));
                           tile.overSprite = new SmolSprite(text, 0, 0, 16, 16);
                        }
                     }
                  }
               }

               int numDragonites = 0;
               int numGarchomps = 0;
               String dragoniteGender = null;
               String garchompGender = null;

               for (Vector2 pos : game.map.overworldTiles.keySet()) {
                  if (game.map.pokemon.containsKey(pos)) {
                     Pokemon pokemon = game.map.pokemon.get(pos);
                     if (pokemon.specie.name.equals("dragonite")) {
                        dragoniteGender = pokemon.gender;
                        numDragonites++;
                     } else if (pokemon.specie.name.equals("garchomp")) {
                        garchompGender = pokemon.gender;
                        numGarchomps++;
                     }

                     if (numDragonites >= 2 && numGarchomps >= 2) {
                        break;
                     }
                  }
               }

               int index = game.map.rand.nextInt(game.map.edges.size() - 1);

               for (String gender : new String[]{"male", "female"}) {
                  if (numDragonites >= 2) {
                     break;
                  }

                  if (!gender.equals(dragoniteGender)) {
                     System.out.println("numDragonites");
                     System.out.println(numDragonites);
                     Vector2 edge = game.map.edges.get(index);
                     Pokemon pokemon = new Pokemon("dragonite", 55);
                     pokemon.gender = gender;
                     pokemon.position = edge.cpy();
                     pokemon.mapTiles = game.map.overworldTiles;
                     pokemon.standingAction = pokemon.new Standing();
                     game.insertAction(pokemon.standingAction);
                     game.map.pokemon.put(pokemon.position.cpy(), pokemon);
                     numDragonites++;
                  }
               }

               index = Game.rand.nextInt(desertEdges.size() - 1);

               for (String gender : new String[]{"male", "female"}) {
                  if (desertEdges.size() <= 0 || numGarchomps >= 2) {
                     break;
                  }

                  if (!gender.equals(garchompGender)) {
                     System.out.println("numGarchomps");
                     System.out.println(numGarchomps);
                     Vector2 edge = desertEdges.get(index);
                     Pokemon pokemon = new Pokemon("garchomp", 50);
                     pokemon.gender = gender;
                     pokemon.position = edge.cpy();
                     pokemon.mapTiles = game.map.overworldTiles;
                     pokemon.standingAction = pokemon.new Standing();
                     game.insertAction(pokemon.standingAction);
                     game.map.pokemon.put(pokemon.position.cpy(), pokemon);
                  }
               }
            }
         }
      }

      if (fadeToNight) {
         if (!game.map.currRoute.isDungeon) {
            game.mapBatch.setColor(animContainer.currentThing());
         }

         if (animContainer.index == 0 && this.animIndex == 0) {
            this.countDownToGhost = this.rand.nextInt(500) + 500;
         }

         this.animIndex++;
         if (this.animIndex >= animContainer.currentFrame()) {
            animContainer.index++;
            this.animIndex = 0;
         }

         if (animContainer.index >= animContainer.animateThese.size()) {
            fadeToNight = false;
            game.map.timeOfDay = "night";
            animContainer.index = 0;
            this.countDownToCacturne = 200;

            for (Tile tile : game.map.overworldTiles.values()) {
               if (tile.name.equals("desert4")
                  && tile.nameUpper.equals("")
                  && Game.rand.nextInt(512) < 2
                  && !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y - 32.0F, game.cam.position.z)
                  && !game.cam
                     .frustum
                     .pointInFrustum(tile.position.x + tile.sprite.getWidth(), tile.position.y + tile.sprite.getHeight() + 64.0F, game.cam.position.z)
                  && !game.cam.frustum.pointInFrustum(tile.position.x + tile.sprite.getWidth(), tile.position.y - 32.0F, game.cam.position.z)
                  && !game.cam.frustum.pointInFrustum(tile.position.x, tile.position.y + tile.sprite.getHeight() + 64.0F, game.cam.position.z)) {
                  Vector2 startPos = tile.position.cpy().add(-64.0F, -64.0F);
                  startPos.x = (int)startPos.x - (int)startPos.x % 16;
                  startPos.y = (int)startPos.y - (int)startPos.y % 16;
                  Vector2 endPos = tile.position.cpy().add(64.0F, 64.0F);
                  endPos.x = (int)endPos.x - (int)endPos.x % 16;
                  endPos.y = (int)endPos.y - (int)endPos.y % 16;
                  int numPlaced = 0;
                  Vector2 currPos = new Vector2(startPos.x, startPos.y);

                  while (currPos.y < endPos.y) {
                     Tile nextTile = game.map.overworldTiles.get(currPos);
                     currPos.x += 16.0F;
                     if (currPos.x > endPos.x) {
                        currPos.x = startPos.x;
                        currPos.y += 16.0F;
                     }

                     if (nextTile != null
                        && nextTile.name.equals("desert4")
                        && nextTile.nameUpper.equals("")
                        && !nextTile.isSolid
                        && !nextTile.isLedge
                        && Game.rand.nextInt(256) < 32) {
                        nextTile.nameUpper = "cactus2_cacturne";
                        nextTile.init();
                        if (++numPlaced > 4) {
                           break;
                        }
                     }
                  }
               }
            }

            if (!game.map.currRoute.name.contains("pkmnmansion")) {
               game.musicController.startTimeOfDay = "night";
            }

            this.night++;
            this.signCounter = 150;
            this.bgSprite.setPosition(0.0F, 24.0F);
         }
      }

      if (game.map.tiles == game.map.overworldTiles && game.map.timeOfDay.equals("night") && game.playerCanMove && !game.player.isNearCampfire) {
         if (game.player.nearCacturne) {
            this.countDownToCacturne--;
            System.out.println(this.countDownToCacturne);
            if (this.countDownToCacturne <= 0) {
               this.countDownToCacturne = this.rand.nextInt(500) + 100;
               System.out.println("spawn cacturne");
               Pokemon cacturne = null;
               Vector2 startPos = game.player.position.cpy().add(-80.0F, -80.0F);
               startPos.x = (int)startPos.x - (int)startPos.x % 16;
               startPos.y = (int)startPos.y - (int)startPos.y % 16;
               Vector2 endPos = game.player.position.cpy().add(80.0F, 80.0F);
               endPos.x = (int)endPos.x - (int)endPos.x % 16;
               endPos.y = (int)endPos.y - (int)endPos.y % 16;
               Vector2 currPos = new Vector2(startPos.x, startPos.y);

               while (currPos.y < endPos.y) {
                  Tile nextTile = game.map.overworldTiles.get(currPos);
                  currPos.x += 16.0F;
                  if (currPos.x > endPos.x) {
                     currPos.x = startPos.x;
                     currPos.y += 16.0F;
                  }

                  if (nextTile != null && nextTile.nameUpper.equals("cactus2_cacturne")) {
                     nextTile.nameUpper = "";
                     nextTile.init();
                     cacturne = new Pokemon("cacturne", 32 + Game.rand.nextInt(4), Pokemon.Generation.CRYSTAL);
                     cacturne.position = nextTile.position.cpy();
                     cacturne.mapTiles = game.map.overworldTiles;
                     cacturne.aggroPlayer = true;
                     game.playerCanMove = false;
                     game.insertAction(cacturne.new Cacturnt(null));
                     game.insertAction(game.player.new Emote("!", null));
                     game.insertAction(
                        cacturne.new CactusSpawn(
                           nextTile,
                           cacturne.new Emote(
                              "skull",
                              new WaitFrames(
                                 game,
                                 60,
                                 new SetField(game, "playerCanMove", true, new SetField(game.musicController, "startNightAlert", "night1_chase1", null))
                              )
                           )
                        )
                     );
                  }
               }

               if (cacturne != null) {
                  game.insertAction(new WaitFrames(game, 60, new PlayMusic(cacturne, null)));
                  game.musicController.startNightAlert = "night1_alert1";
               }
            }
         }
      }

      if (game.map.currBiome.equals("graveyard")) {
         if (game.player.isSleeping) {
            if (this.countDownToGengar >= 0) {
               if (this.countDownToGengar == 0) {
                  this.countDownToGengar--;
                  Pokemon gengar = new Pokemon("gengar", 30, Pokemon.Generation.CRYSTAL);
                  int offset = Game.rand.nextInt(2) + 3;
                  gengar.position = game.player.position.cpy().add(offset * 16 * (Game.rand.nextInt(3) - 1), offset * 16 * (Game.rand.nextInt(3) - 1));
                  gengar.mapTiles = game.map.overworldTiles;
                  game.insertAction(gengar.new Shadowed(null));
               } else {
                  this.countDownToGengar--;
                  System.out.println(this.countDownToGengar);
               }
            }
         } else {
            this.countDownToGengar = 200;
         }
      }

      if (dayTimer == 5000 || dayTimer == 13000 || dayTimer == 18000) {
         this.lowerTide = dayTimer == 5000 && fadeToNight;
         this.lowerTide = this.lowerTide || dayTimer == 13000 && game.map.timeOfDay.equals("day");
         System.out.println("lowerTide");
         System.out.println(this.lowerTide);

         for (Tile tile : game.map.overworldTiles.values()) {
            if (tile.name.contains("_tidal")) {
               if (this.lowerTide) {
                  tile.name = tile.name.replace("_tidalwater", "_tidaloff");
               } else {
                  tile.name = tile.name.replace("_tidaloff", "_tidalwater");
               }

               this.tidalTiles.add(tile);
            }
         }
      }

      if (!this.tidalTiles.isEmpty()) {
         this.tidalTimer++;
         if (this.tidalTimer >= 64 && this.changeThese.isEmpty()) {
            this.tidalTimer = 0;
            this.doneTidalSound = false;
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
            boolean changedTideLevel = false;
            int max = 4;
            if (!this.lowerTide) {
               max = 8;
            }

            for (Tile tile : this.tidalTiles) {
               for (int i = 0; i < max; i++) {
                  Tile nextTile = game.map.overworldTiles.get(tile.position.cpy().add(positions[i]));
                  if (nextTile != null) {
                     boolean shouldPlace = nextTile.name.equals("water2") || nextTile.isTidal;
                     if (this.lowerTide) {
                        shouldPlace = !shouldPlace;
                     }

                     if (shouldPlace) {
                        this.changeThese.add(tile);
                        changedTideLevel = true;
                        break;
                     }
                  }
               }
            }

            if (!changedTideLevel) {
               System.out.println("done tidal.");

               for (Tile tile : this.tidalTiles) {
                  tile.init();

                  for (int i = 0; i < positions.length; i++) {
                     Tile nextTile = game.map.overworldTiles.get(tile.position.cpy().add(positions[i]));
                     if (nextTile != null && !nextTile.isTidal) {
                        game.map.coastify(nextTile, game.map.overworldTiles, true);
                     }
                  }

                  game.map.coastify(tile, game.map.overworldTiles, true);
               }

               this.tidalTiles.clear();
            }
         }
      }

      if (!this.changeThese.isEmpty()) {
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
         int numDone = 0;
         boolean nearPlayer = false;

         for (Tile tile : new ArrayList<>(this.changeThese)) {
            tile.init();

            for (int i = 0; i < positions.length; i++) {
               Tile nextTile = game.map.overworldTiles.get(tile.position.cpy().add(positions[i]));
               if (nextTile != null && !nextTile.isTidal) {
                  if (this.lowerTide) {
                     game.map.coastify(nextTile, game.map.overworldTiles, true);
                  } else {
                     nextTile.name = nextTile.name.replace("tidalwater", "tidaloff");
                     game.map.coastify(nextTile, game.map.overworldTiles, true);
                     nextTile.name = nextTile.name.replace("tidaloff", "tidalwater");
                  }
               }
            }

            game.map.coastify(tile, game.map.overworldTiles, true);
            this.changeThese.remove(tile);
            this.tidalTiles.remove(tile);
            if (!this.doneTidalSound && tile.position.dst2(game.player.position) < 16384.0F) {
               nearPlayer = true;
               this.doneTidalSound = true;
            }

            if (++numDone > 32) {
               break;
            }
         }

         if (nearPlayer) {
            game.insertAction(new PlayMusic("sand1", 0.5F, true, null));
         }
      }
   }
}
