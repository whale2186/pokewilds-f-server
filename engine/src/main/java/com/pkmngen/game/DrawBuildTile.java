package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.action.DrawAction;
import com.pkmngen.game.util.GameProfiler;

class DrawBuildTile extends DrawAction {
   public Action.Layer layer = Action.Layer.map_108;
   public int terrainTimer = 0;
   Color prevColor = new Color();
   Matrix4 prevCombined = new Matrix4();

   public DrawBuildTile(GameProfiler profiler) {
      super(profiler);
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
   protected void draw(Game game) {
      if (Game.canUseFrameBuffers && game.player != null) {
         game.mapBatch.setProjectionMatrix(Game.identity);
         this.prevColor.set(game.mapBatch.getColor());
         game.mapBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         game.temp1 = game.mapBatch.getBlendSrcFunc();
         game.temp2 = game.mapBatch.getBlendDstFunc();
         game.mapBatch.setBlendFunction(774, 770);
         if (this.prevColor.r < 1.0F) {
            game.mapBatch.draw(game.frameBuffer.getColorBufferTexture(), -1.0F, 1.0F, 2.0F, -2.0F);
         }

         if (this.prevColor.r < 0.5F) {
            game.mapBatch.draw(game.frameBuffer.getColorBufferTexture(), -1.0F, 1.0F, 2.0F, -2.0F);
         }

         if (this.prevColor.r < 0.1F) {
            game.mapBatch.draw(game.frameBuffer.getColorBufferTexture(), -1.0F, 1.0F, 2.0F, -2.0F);
         }

         game.mapBatch.setBlendFunction(game.temp1, game.temp2);
         game.mapBatch.setColor(this.prevColor);
      }

      this.prevCombined.set(game.cam.combined);
      game.cam.update();
      game.mapBatch.setProjectionMatrix(game.cam.combined);
      if (game.player.currFieldMove.equals("BUILD")) {
         Vector2 pos = game.player.facingPos();
         Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
         sprite.setAlpha(0.8F);
         sprite.setPosition(pos.x, pos.y);
         Tile nextTile = game.map.tiles.get(pos);
         boolean isTorch = nextTile != null
            && (
               game.player.currBuildTile.name.contains("torch")
                  || game.player.currBuildTile.name.equals("house_clock1")
                  || game.player.currBuildTile.name.contains("picture")
                  || game.player.currBuildTile.name.contains("window")
            );
         if (isTorch && !nextTile.isSolid || nextTile != null && nextTile.isSolid) {
            sprite.setColor(1.0F, 0.7F, 0.7F, 0.8F);
         }

         boolean requirementsMet = true;

         for (String reqName : game.player.buildTileRequirements.get(game.player.currBuildTile.name).keySet()) {
            if (!game.player.hasItem(reqName)) {
               requirementsMet = false;
               break;
            }

            int playerOwns = game.player.getItemAmount(reqName);
            if (playerOwns < game.player.buildTileRequirements.get(game.player.currBuildTile.name).get(reqName)) {
               requirementsMet = false;
               break;
            }
         }

         if (nextTile != null && game.player.currBuildTile.name.contains("house")) {
            Tile upTile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(nextTile.position.cpy().add(0.0F, 16.0F));
            requirementsMet = requirementsMet && (upTile == null || upTile.name.contains("house") || upTile.name.contains("rug"));
         }

         if (isTorch) {
            requirementsMet = requirementsMet && !nextTile.items().containsKey("torch") && nextTile.isSolid && !nextTile.nameUpper.contains("roof");
            String[] notAllowedTiles = new String[]{"regi", "tree", "bush", "table", "bed", "shelf", "plant", "couch"};

            for (String name : notAllowedTiles) {
               requirementsMet = requirementsMet && !nextTile.name.contains(name) && !nextTile.nameUpper.contains(name);
            }
         }

         if (nextTile != null && game.player.currBuildTile.name.contains("door")) {
            requirementsMet &= !game.map.overworldTiles.get(pos.cpy().add(0.0F, -16.0F)).nameUpper.contains("roof");
         }

         boolean isBridge = game.player.currBuildTile.name.contains("bridge");
         if (isBridge) {
            requirementsMet = requirementsMet && nextTile != null && nextTile.name.contains("water") && !nextTile.name.contains("bridge");
         }

         if (!requirementsMet) {
            sprite.setColor(1.0F, 0.7F, 0.7F, 0.8F);
         }

         sprite.draw(game.mapBatch);
         if (game.player.currBuildTile.overSprite != null) {
            sprite = new Sprite(game.player.currBuildTile.overSprite);
            sprite.setAlpha(0.8F);
            sprite.setPosition(pos.x, pos.y);
            if (nextTile != null && (nextTile.isSolid || nextTile.nameUpper.contains("door"))) {
               sprite.setColor(1.0F, 0.7F, 0.7F, 0.8F);
            }

            sprite.draw(game.mapBatch);
         }
      } else if (game.player.currFieldMove.equals("DIG")) {
         Vector2 pos = game.player.facingPos();
         Tile nextTile = game.map.tiles.get(pos);
         if (nextTile != null) {
            if (!nextTile.nameUpper.contains("hole")
               && !nextTile.name.contains("water5")
               && !nextTile.name.contains("water2")
               && !nextTile.name.contains("lava")) {
               this.terrainTimer = 0;
               game.player.currBuildTile = game.player.currDigTile;
            } else if (this.terrainTimer < 25) {
               this.terrainTimer++;
            } else {
               game.player.currBuildTile = game.player.buildTiles.get(game.player.buildTileIndex);
            }
         }

         Sprite sprite = new Sprite(game.player.currBuildTile.sprite);
         sprite.setAlpha(0.8F);
         sprite.setPosition(pos.x, pos.y);
         sprite.draw(game.mapBatch);
      }

      if (game.player.currPlanting != null) {
         Vector2 pos = game.player.facingPos();
         Player.sproutSprite.setAlpha(0.7F);
         Player.sproutSprite.setPosition(pos.x, pos.y + 2.0F);
         Player.sproutSprite2.setPosition(pos.x, pos.y);
         Tile nextTile = game.map.tiles.get(pos);
         if (game.player.currPlanting.contains("apricorn")) {
            if (nextTile == null
               || (
                     nextTile.name.equals("green1")
                        || nextTile.name.contains("flower")
                        || nextTile.name.contains("sand")
                        || nextTile.name.contains("snow")
                        || nextTile.name.contains("desert")
                  )
                  && !nextTile.nameUpper.contains("tree")
                  && !nextTile.isSolid) {
               if (nextTile != null) {
                  Player.sproutSprite2.setColor(0.7F, 1.0F, 0.7F, 0.7F);
               }
            } else {
               Player.sproutSprite.setColor(1.0F, 0.7F, 0.7F, 0.7F);
               Player.sproutSprite2.setColor(1.0F, 0.7F, 0.7F, 0.7F);
            }
         } else if (game.player.currPlanting.equals("manure")) {
            if (nextTile == null
               || nextTile.nameUpper.contains("fertilized")
               || !nextTile.nameUpper.contains("tree_planted") && !nextTile.nameUpper.contains("bush2_color")) {
               if (nextTile != null) {
                  Player.sproutSprite2.setColor(1.0F, 0.7F, 0.7F, 0.7F);
               }
            } else {
               Player.sproutSprite2.setColor(0.7F, 1.0F, 0.7F, 0.7F);
            }
         } else if (game.player.currPlanting.equals("miracle seed")) {
            if (nextTile == null
               || !nextTile.nameUpper.equals("")
               || !nextTile.name.equals("green1")
                  && !nextTile.name.contains("snow")
                  && !nextTile.name.contains("sand")
                  && !nextTile.name.contains("desert")
                  && !nextTile.name.contains("green9")
                  && !nextTile.name.contains("green10")
                  && !nextTile.name.contains("green11")
                  && !nextTile.name.contains("green12")
                  && !nextTile.name.contains("green_savanna")
                  && !nextTile.name.contains("flower")) {
               if (nextTile != null) {
                  Player.sproutSprite2.setColor(1.0F, 0.7F, 0.7F, 0.7F);
               }
            } else {
               Player.sproutSprite2.setColor(0.7F, 1.0F, 0.7F, 0.7F);
            }
         } else if (!game.player.currPlanting.equals("berry seed")) {
            if (game.player.currPlanting.contains("rod")) {
               if (nextTile != null && nextTile.name.contains("water")) {
                  Player.sproutSprite2.setColor(0.7F, 1.0F, 0.7F, 0.7F);
               } else if (nextTile != null) {
                  Player.sproutSprite2.setColor(1.0F, 0.7F, 0.7F, 0.7F);
               }
            }
         } else if (nextTile == null
            || !nextTile.nameUpper.equals("")
            || !nextTile.name.equals("green1")
               && !nextTile.name.contains("snow")
               && !nextTile.name.contains("sand")
               && !nextTile.name.contains("desert")
               && !nextTile.name.contains("green9")
               && !nextTile.name.contains("green10")
               && !nextTile.name.contains("green11")
               && !nextTile.name.contains("green12")
               && !nextTile.name.contains("volcano")
               && !nextTile.name.contains("savanna")
               && !nextTile.name.contains("graveyard")
               && !nextTile.name.contains("flower")) {
            if (nextTile != null) {
               Player.sproutSprite2.setColor(1.0F, 0.7F, 0.7F, 0.7F);
            }
         } else {
            Player.sproutSprite2.setColor(0.7F, 1.0F, 0.7F, 0.7F);
         }

         if (Player.drawSproutSprite) {
            Player.sproutSprite2.draw(game.mapBatch);
            if (game.player.currPlanting.contains("apricorn")) {
               Player.sproutSprite.draw(game.mapBatch);
            }
         }
      }

      game.mapBatch.setProjectionMatrix(this.prevCombined);
   }
}
