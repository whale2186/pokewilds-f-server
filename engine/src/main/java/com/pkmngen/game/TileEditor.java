package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.Arrays;

class TileEditor extends Action {
   Vector3 touchPos = new Vector3();
   boolean justTouched = false;
   public int timer = 0;
   public int brushSize = 1;
   public Tile newTile = null;
   public static int buildingTileUsed = 0;
   String[] buildingTileTypes = new String[]{"general", "wooded_lake", "tundra", "desert", "bridge"};
   Color prevColor = new Color();
   ArrayList<String> overTiles = new ArrayList<>();
   ArrayList<String> underTiles;
   Tile currTile;
   int currIndex;

   public TileEditor() {
      super();
      this.overTiles.add("rock1_color");
      this.overTiles.add("cactus7");
      this.overTiles.add("tree7");
      this.overTiles.add("aloe_large1");
      this.overTiles.add("tree_large1");
      this.overTiles.add("tree_large2");
      this.overTiles.add("tree_large3");
      this.overTiles.add("tree_savanna1");
      this.overTiles.add("bush_savanna1");
      this.overTiles.add("rock_ice2");
      this.underTiles = new ArrayList<>();
      this.underTiles.add("green1");
      this.underTiles.add("sand1");
      this.underTiles.add("sand2");
      this.underTiles.add("sand3");
      this.underTiles.add("mountain1");
      this.underTiles.add("flower2");
      this.underTiles.add("flower3");
      this.underTiles.add("flower4");
      this.underTiles.add("flower5");
      this.underTiles.add("grass2");
      this.underTiles.add("grass3");
      this.underTiles.add("grass4");
      this.underTiles.add("water2");
      this.underTiles.add("grass_graveyard1");
      this.underTiles.add("green9");
      this.underTiles.add("green10");
      this.underTiles.add("green11");
      this.underTiles.add("green12");
      this.underTiles.add("grass_sand1");
      this.underTiles.add("grass_sand2");
      this.underTiles.add("grass_sand3");
      this.underTiles.add("desert1");
      this.underTiles.add("desert2");
      this.underTiles.add("desert3");
      this.underTiles.add("desert4");
      this.underTiles.add("ruins1_NE");
      this.underTiles.add("ruins1_NSEW");
      this.underTiles.add("ruins1_SE");
      this.underTiles.add("ruins1_NW");
      this.underTiles.add("ruins1_SW");
      this.underTiles.add("ruins1_pillar1");
      this.underTiles.add("ruins_floor2");
      this.underTiles.add("ruins2_floor");
      this.underTiles.add("ruins2_door");
      this.underTiles.add("ruins2_wall1");
      this.underTiles.add("cave1_ramp1");
      this.underTiles.add("black1");
      this.underTiles.add("cave1_floor1");
      this.underTiles.add("cave1_right1");
      this.overTiles.add("bush1");
      this.overTiles.add("tree2");
      this.overTiles.add("tree4");
      this.overTiles.add("tree6");
      this.overTiles.add("cactus1");
      this.overTiles.add("cactus2");
      this.overTiles.add("cactus3");
      this.overTiles.add("cactus4");
      this.overTiles.add("cactus5");
      this.overTiles.add("cactus6");
      this.overTiles.add("gravestone1");
      this.overTiles.add("gravestone2");
      this.overTiles.add("stairs_down2");
      this.overTiles.add("rock1");
      this.overTiles.add("rock5");
      this.overTiles.add("ledges3_N");
      this.overTiles.add("ledges3_S");
      this.overTiles.add("ledges3_E");
      this.overTiles.add("ledges3_W");
      this.overTiles.add("black1");
      this.underTiles.add("grass3");
      this.underTiles.add("grass_fairy1");
      this.underTiles.add("grass_fairy2");
      this.underTiles.add("grass_fairy3");
      this.underTiles.add("tree_fairy1");
      this.underTiles.add("tree_fairy2");
      this.underTiles.add("grass_savanna1");
      this.underTiles.add("grass_savanna2");
      this.underTiles.add("green_savanna1");
      this.underTiles.add("green_savanna3");
      this.underTiles.add("green_savanna4");
      this.underTiles.add("green_savanna5");
      this.underTiles.add("ice2");
      this.underTiles.add("snow1");
      this.underTiles.add("snow2");
      this.underTiles.add("snow3");
      this.underTiles.add("lava1_");
      this.underTiles.add("soot1");
      this.underTiles.add("volcano1");
      this.underTiles.addAll(this.overTiles);
      this.currIndex = 0;
   }

   @Override
   public void firstStep(Game game) {
      this.currTile = new Tile(this.underTiles.get(0), new Vector2(0.0F, 0.0F), true, null);
   }

   @Override
   public void step(Game game) {
      if (this.timer < 60) {
         this.timer++;
      } else if (this.timer == 60) {
      }

      if (game.debugInputEnabled && Gdx.input.isKeyJustPressed(30)) {
         if (buildingTileUsed < 4) {
            buildingTileUsed++;
         } else {
            buildingTileUsed = 0;
         }

         game.player.updateBuildTiles(game);
         System.out.println("New tile type: " + this.buildingTileTypes[buildingTileUsed]);
      }

      if (game.debugInputEnabled && Gdx.input.isKeyPressed(44)) {
         Vector3 worldCoordsTL = game.cam.unproject(new Vector3(-256.0F, 0.0F, 0.0F));
         Vector3 worldCoordsBR = game.cam.unproject(new Vector3(game.currScreen.x, game.currScreen.y + 128.0F + 64.0F, 0.0F));
         worldCoordsTL.x = (int)worldCoordsTL.x - (int)worldCoordsTL.x % 16;
         worldCoordsTL.y = (int)worldCoordsTL.y - (int)worldCoordsTL.y % 16;
         worldCoordsBR.x = (int)worldCoordsBR.x - (int)worldCoordsBR.x % 16;
         worldCoordsBR.y = (int)worldCoordsBR.y - (int)worldCoordsBR.y % 16;
         Vector3 startPos = worldCoordsTL;
         Vector3 endPos = worldCoordsBR;
         String[][] allTiles = new String[100][];
         ArrayList<String> currTiles = new ArrayList<>();
         int i = 0;
         Vector2 currPos = new Vector2(startPos.x, startPos.y);

         while (currPos.y > endPos.y) {
            Tile tile = game.map.tiles.get(currPos);
            currPos.x += 16.0F;
            if (currPos.x > endPos.x) {
               currPos.x = startPos.x;
               currPos.y -= 16.0F;
               String[] currTilesArray = new String[currTiles.size()];
               int j = 0;

               for (String currTile : currTiles) {
                  currTilesArray[j] = currTile;
                  j++;
               }

               allTiles[i] = currTilesArray;
               currTiles.clear();
               i++;
            }

            if (game.cam.frustum.pointInFrustum(currPos.x, currPos.y - 32.0F, game.cam.position.z)
               || game.cam.frustum.pointInFrustum(currPos.x + 16.0F, currPos.y + 16.0F + 64.0F, game.cam.position.z)
               || game.cam.frustum.pointInFrustum(currPos.x + 16.0F, currPos.y - 32.0F, game.cam.position.z)
               || game.cam.frustum.pointInFrustum(currPos.x, currPos.y + 16.0F + 64.0F, game.cam.position.z)) {
               if (tile == null) {
                  currTiles.add(null);
               } else {
                  currTiles.add(tile.name);
               }
            }
         }

         System.out.println(Arrays.deepToString(allTiles));
      }

      if (this.currTile != null) {
         this.prevColor.set(game.mapBatch.getColor());
         game.mapBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.g, 0.7F);
         this.touchPos.x = Gdx.input.getX();
         this.touchPos.y = Gdx.input.getY();
         game.cam.unproject(this.touchPos);
         int offsetX = 0;
         int offsetY = 0;
         if (this.touchPos.x < 0.0F) {
            offsetX = -16;
         }

         if (this.touchPos.y < 0.0F) {
            offsetY = -16;
         }

         for (int i = 0; i < this.brushSize; i++) {
            for (int j = 0; j < this.brushSize; j++) {
               this.currTile
                  .sprite
                  .setPosition(this.touchPos.x - this.touchPos.x % 16.0F + offsetX + 16 * i, this.touchPos.y - this.touchPos.y % 16.0F + offsetY + 16 * j);
               this.currTile.sprite.draw(game.mapBatch);
               if (this.currTile.overSprite != null) {
                  this.currTile
                     .overSprite
                     .setPosition(this.touchPos.x - this.touchPos.x % 16.0F + offsetX + 16 * i, this.touchPos.y - this.touchPos.y % 16.0F + offsetY + 16 * j);
                  this.currTile.overSprite.draw(game.mapBatch);
               }
            }
         }

         game.mapBatch.setColor(this.prevColor);
      }

      if (Gdx.input.isTouched()) {
         Vector2 position = new Vector2();

         for (int i = 0; i < this.brushSize; i++) {
            for (int j = 0; j < this.brushSize; j++) {
               position.set(this.currTile.sprite.getX() - 16 * i, this.currTile.sprite.getY() - 16 * j);
               Tile currTile = game.map.tiles.remove(position);
               this.newTile = new Tile(this.underTiles.get(this.currIndex), position.cpy(), true, null);
               if (!this.currTile.name.equals("black1") && this.overTiles.contains(this.currTile.name)) {
                  this.newTile = new Tile(currTile.name, this.underTiles.get(this.currIndex), position.cpy(), true, null);
               }

               this.newTile.guessBiomeType();
               game.map.tiles.put(this.newTile.position.cpy(), this.newTile);
               if (this.currTile.name.equals("black1")) {
                  game.map.tiles.remove(this.newTile.position.cpy());
               }
            }
         }
      } else if (this.newTile != null) {
         game.map.adjustSurroundingTiles(this.newTile);
         this.newTile = null;
      }

      if (InputProcessor.lJustPressed) {
         this.currIndex--;
         if (this.currIndex < 0) {
            this.currIndex = this.underTiles.size() - 1;
         }

         this.currTile = new Tile(this.underTiles.get(this.currIndex), new Vector2(0.0F, 0.0F), true, null);
      } else if (InputProcessor.rJustPressed) {
         this.currIndex++;
         if (this.currIndex >= this.underTiles.size()) {
            this.currIndex = 0;
         }

         this.currTile = new Tile(this.underTiles.get(this.currIndex), new Vector2(0.0F, 0.0F), true, null);
      }

      if (Gdx.input.isKeyJustPressed(129)) {
         if (this.brushSize > 0) {
            this.brushSize--;
         }
      } else if (Gdx.input.isKeyJustPressed(57) && this.brushSize < 10) {
         this.brushSize++;
      }
   }
}
