package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.Save;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

class GenerateWorld extends Action {
   public int size;
   public String enterDirection;
   public float percent = 1.0F;
   public Action.Layer layer = Action.Layer.map_0;

   public GenerateWorld(int size, String enterDirection, Action nextAction) {
      super();
      this.size = size;
      this.enterDirection = enterDirection;
      this.nextAction = nextAction;
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void firstStep(final Game game) {
      game.actionStack.remove(this);
      if (game.coop != null) {
         game.playerCanMove = true;
         game.insertAction(new DisplayText(game, "LAN co-op stays on this island. Disconnect before exploring a new island.", null, true, true, null));
         return;
      }
      DrawMiniMap.warpTiles.clear();

      for (Action action : new ArrayList<>(game.actionStack)) {
         if (action instanceof Pokemon.Emote) {
            game.actionStack.remove(action);
         } else if (action instanceof CycleDayNight) {
            game.actionStack.remove(action);
         }
      }

      for (Pokemon pokemon : game.map.pokemon.values()) {
         pokemon.removeDrawActions(game);
      }

      game.actionStack.remove(game.map.upkeepTimers);
      System.out.println("Action stack before loading new area");
      System.out.println("Layer, Name");

      for (Action action : game.actionStack) {
         System.out.println(action.getLayer() + "  " + action.getClass().getName());
      }

      String[] vals = game.map.currMapId.split(",");
      int xPos = Integer.valueOf(vals[0]);
      int yPos = Integer.valueOf(vals[1]);
      if (this.enterDirection.equals("left")) {
         this.percent = (game.player.position.y - game.map.bottomLeft.y) / (game.map.topRight.y - game.map.bottomLeft.y);
         xPos--;
      } else if (this.enterDirection.equals("right")) {
         this.percent = (game.player.position.y - game.map.bottomLeft.y) / (game.map.topRight.y - game.map.bottomLeft.y);
         xPos++;
      } else if (this.enterDirection.equals("up")) {
         this.percent = (game.player.position.x - game.map.bottomLeft.x) / (game.map.topRight.x - game.map.bottomLeft.x);
         yPos++;
      } else if (this.enterDirection.equals("down")) {
         this.percent = (game.player.position.x - game.map.bottomLeft.x) / (game.map.topRight.x - game.map.bottomLeft.x);
         yPos--;
      }

      final String adjacentWorldId = xPos + "," + yPos;
      if (Save.isDataPresent(game.map.id + ".sav/map" + adjacentWorldId)) {
         System.out.println("Found existing world:");
         System.out.println("map" + adjacentWorldId);
         game.map.saveToFileNew(game);
         game.map.overworldTiles.clear();
         game.map.pokemon.clear();
         game.map.edges.clear();

         for (HashMap<Vector2, Tile> tiles : game.map.interiorTiles) {
            if (tiles != null) {
               tiles.clear();
            }
         }

         game.map.interiorTiles.clear();
         game.map.minimap.dispose();
         String prevTimeOfDay = game.map.timeOfDay;
         game.map = new PkmnMap(game.map.id);
         game.map.timeOfDay = prevTimeOfDay;
         game.map.currMapId = adjacentWorldId;
         game.insertAction(game.map.upkeepTimers);
         game.map.loadMapFromFile(game);
         InputProcessor.aJustPressed = false;
         Vector2 startLoc = new Vector2();
         if (this.enterDirection.equals("left")) {
            startLoc.x = game.map.topRight.x;
            startLoc.y = game.map.bottomLeft.y + this.percent * (game.map.topRight.y - game.map.bottomLeft.y);
         } else if (this.enterDirection.equals("right")) {
            startLoc.x = game.map.bottomLeft.x;
            startLoc.y = game.map.bottomLeft.y + this.percent * (game.map.topRight.y - game.map.bottomLeft.y);
         } else if (this.enterDirection.equals("up")) {
            startLoc.y = game.map.bottomLeft.y;
            startLoc.x = game.map.bottomLeft.x + this.percent * (game.map.topRight.x - game.map.bottomLeft.x);
         } else if (this.enterDirection.equals("down")) {
            startLoc.y = game.map.topRight.y;
            startLoc.x = game.map.bottomLeft.x + this.percent * (game.map.topRight.x - game.map.bottomLeft.x);
         }

         startLoc.x = startLoc.x - startLoc.x % 16.0F;
         startLoc.y = startLoc.y - startLoc.y % 16.0F;
         game.player.position.set(startLoc);
         game.cam.position.set(startLoc.x + 16.0F, startLoc.y, 0.0F);
         EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
         enterBuilding.slow = 8;
         game.insertAction(enterBuilding);
         game.insertAction(this.nextAction);
         game.insertAction(new CycleDayNight(game));
         game.musicController.startTimeOfDay = game.map.timeOfDay;
      } else {
         final Action drawControls = new DrawControls();
         game.map.saveToFileNew(game);
         game.map.overworldTiles.clear();
         game.map.pokemon.clear();
         game.map.edges.clear();

         for (HashMap<Vector2, Tile> tiles : game.map.interiorTiles) {
            if (tiles != null) {
               tiles.clear();
            }
         }

         game.map.interiorTiles.clear();
         game.map.minimap.dispose();
         String prevTimeOfDay = game.map.timeOfDay;
         game.map = new PkmnMap(game.map.id);
         game.map.timeOfDay = prevTimeOfDay;
         final int size = this.size;
         final String enterDirection = this.enterDirection;
         final float percent = this.percent;
         final Action nextAction = this.nextAction;
         Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  System.out.println("Generating map...");
                  System.out.println(LocalTime.now());
                  final GenIsland1 genIsland = new GenIsland1(game, new Vector2(0.0F, 0.0F), size);
                  System.out.println("Done.");
                  System.out.println(LocalTime.now());
                  Thread.sleep(4000L);
                  Runnable runnable = new Runnable() {
                     @Override
                     public void run() {
                        genIsland.step(game);
                        EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
                        enterBuilding.slow = 8;
                        game.insertAction(enterBuilding);
                        game.insertAction(new DisplayText.Clear(game, new SetField(drawControls, "remove", true, null)));
                        Vector2 startLoc = new Vector2();
                        if (enterDirection.equals("left")) {
                           startLoc.x = game.map.topRight.x - 16.0F;
                           startLoc.y = game.map.bottomLeft.y + percent * (game.map.topRight.y - game.map.bottomLeft.y);
                        } else if (enterDirection.equals("right")) {
                           startLoc.x = game.map.bottomLeft.x;
                           startLoc.y = game.map.bottomLeft.y + percent * (game.map.topRight.y - game.map.bottomLeft.y);
                        } else if (enterDirection.equals("up")) {
                           startLoc.y = game.map.bottomLeft.y;
                           startLoc.x = game.map.bottomLeft.x + percent * (game.map.topRight.x - game.map.bottomLeft.x);
                        } else if (enterDirection.equals("down")) {
                           startLoc.y = game.map.topRight.y - 16.0F;
                           startLoc.x = game.map.bottomLeft.x + percent * (game.map.topRight.x - game.map.bottomLeft.x);
                        }

                        startLoc.x = startLoc.x - startLoc.x % 16.0F;
                        startLoc.y = startLoc.y - startLoc.y % 16.0F;
                        game.player.position.set(startLoc);
                        game.cam.position.set(startLoc.x + 16.0F, startLoc.y, 0.0F);
                        Vector2 nearestEdge = game.map.edges.get(0);
                        float minDistance = game.player.position.dst2(nearestEdge);

                        for (Vector2 edge : game.map.edges) {
                           float currDistance = game.player.position.dst2(edge);
                           if (minDistance > currDistance) {
                              nearestEdge = edge;
                              minDistance = currDistance;
                           }
                        }

                        game.player.spawnLoc = nearestEdge.cpy();
                        game.player.spawnIndex = -1;
                        int width = (int)(game.map.topRight.x - game.map.bottomLeft.x) / 8;
                        int height = (int)(game.map.topRight.y - game.map.bottomLeft.y) / 8;
                        game.map.minimap = new Pixmap(width, height, Format.RGBA8888);
                        game.map.minimap.setColor(0.0F, 0.0F, 0.0F, 1.0F);
                        game.map.minimap.fill();
                        Vector2 startPos = game.player.position.cpy().add(-128.0F, -128.0F);
                        startPos.x = (int)startPos.x - (int)startPos.x % 16;
                        startPos.y = (int)startPos.y - (int)startPos.y % 16;
                        Vector2 endPos = game.player.position.cpy().add(128.0F, 128.0F);
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
                              tile.updateMiniMap(game);
                           }
                        }

                        game.insertAction(nextAction);
                        game.insertAction(new CycleDayNight(game));
                        game.musicController.startTimeOfDay = game.map.timeOfDay;
                        game.map.currMapId = adjacentWorldId;
                     }
                  };
                  Gdx.app.postRunnable(runnable);
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         });
         thread.setPriority(1);
         thread.start();
         game.insertAction(new DisplayText(game, "Generating... please wait...", null, true, false, null));
         game.insertAction(drawControls);
      }
   }

   @Override
   public void step(Game game) {
   }
}
