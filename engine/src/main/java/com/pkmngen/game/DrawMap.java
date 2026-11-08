package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.pkmngen.game.action.DrawAction;
import com.pkmngen.game.util.GameProfiler;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.SmolSpriteProxy;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.texture.DynamicTextures;

class DrawMap extends DrawAction {
   public Action.Layer layer = Action.Layer.map_140;
   SmolSprite belowBridgeSprite;
   Vector3 startPos;
   Vector3 endPos;
   Tile tile;
   Sprite spritePart = new Sprite();
   int zsTimer = 0;
   float done1 = 0.0F;
   float done2 = 0.0F;
   float done3 = 0.0F;
   Color prevColor = new Color();
   Sprite redrawThis;
   final com.badlogic.gdx.graphics.g2d.GlyphLayout remoteLabel = new com.badlogic.gdx.graphics.g2d.GlyphLayout();

   public DrawMap(GameProfiler profiler) {
      super(profiler);
      this.belowBridgeSprite = TextureCache.getTileSprite("bridge1_lower");
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   protected void draw(Game game) {
      if (!game.player.dontDrawMapDuringBattle) {
         for (int i = 0; !game.map.minimapQueue.isEmpty() && i < 2; i++) {
            game.map.minimapQueue.remove(0).updateMiniMap(game);
         }

         this.done1 = 0.0F;
         this.done2 = 0.0F;
         this.done3 = 0.0F;
         game.map.setView(game, game.cam);

         for (Tile tile : game.map.onscreenSpriteProxies) {
            game.mapBatch.draw((SmolSpriteProxy)tile.sprite, tile.sprite.getX(), tile.sprite.getY());
         }

         for (Tile tile : game.map.onscreenSprites) {
            game.mapBatch.draw(tile.sprite, tile.sprite.getX(), tile.sprite.getY());
         }

         for (Tile tile : game.map.onscreenShoreOcean) {
            if (tile.shoreOcean != null) {
               game.mapBatch.draw(DynamicTextures.get(tile.shoreOcean, MoveWater.shoreIndex), tile.sprite.getX(), tile.sprite.getY());
            }
         }

         this.prevColor.set(game.mapBatch.getColor());
         game.mapBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.b, 0.5F);

         for (Tile tile : game.map.onscreenShoreTidal) {
            if (tile.shoreTidal != null) {
               game.mapBatch.draw(DynamicTextures.get(tile.shoreTidal, MoveWater.shoreIndex), tile.sprite.getX(), tile.sprite.getY());
            }
         }

         game.mapBatch.setColor(this.prevColor);

         for (Tile tile : game.map.onscreenBelowBridgeTiles) {
            game.mapBatch.draw(this.belowBridgeSprite, tile.position.x, tile.position.y);
         }

         for (Tile tile : game.map.onscreenOverSprites) {
            if (tile.overSprite != null) {
               game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY() - tile.yOffset);
            }
         }

         for (Tile tile : game.map.onscreenSavannaTrees) {
            game.mapBatch.draw(tile.overSprite, tile.overSprite.getX(), tile.overSprite.getY());
         }

         if (game.playerCanMove) {
            for (Pokemon pokemon : game.map.onscreenEggs) {
               Pokemon.Standing standingAction = (Pokemon.Standing)pokemon.standingAction;
               float volume = 1.0F - pokemon.position.dst2(game.player.position.x, game.player.position.y) / 52480.0F;
               if (standingAction.danceCounter == 0 && volume > this.done1) {
                  this.done1 = volume;
               } else if (standingAction.danceCounter == 16 && volume > this.done2) {
                  this.done2 = volume;
               } else if (standingAction.danceCounter == 90 && volume > this.done3) {
                  this.done3 = volume;
               }
            }

            if (this.done1 > 0.0F) {
               game.insertAction(new PlayMusic("ledge2", this.done1, null));
            }

            if (this.done2 > 0.0F) {
               game.insertAction(new PlayMusic("ledge2", this.done2, null));
            }

            if (this.done3 > 0.0F) {
               game.insertAction(new PlayMusic("headbutt1", this.done3, null));
            }
         }

         for (Player player : game.players.values()) {
            if (player.network.tiles == game.map.tiles
                  && game.cam.frustum.boundsInFrustum(player.position.x + 8, player.position.y + 16, 0, 24, 32, 1)) {
               if (game.coop != null) game.coop.drawFollower(player);
               if (player.isSleeping) {
                  player.zSprite.draw(game.mapBatch);
                  game.mapBatch.draw(player.sleepingSprite, player.position.x, player.position.y);
               } else {
                  com.badlogic.gdx.math.Vector2 drawPosition = game.coop == null ? player.position : game.coop.renderPosition(player);
                  if (game.coop != null) drawPosition.add(game.coop.interactionOffset(player));
                  player.currSprite.setPosition(drawPosition.x, drawPosition.y + 4.0F);
                  player.currSprite.draw(game.mapBatch);
                  if (game.coop != null) game.coop.recordAvatarDraw(player);
               }
               if (game.coop != null) game.coop.drawInteraction(player);
               if (player.name != null && !player.name.isEmpty()) {
                  com.badlogic.gdx.math.Vector2 labelPosition = game.coop == null ? player.position : game.coop.renderPosition(player);
                  remoteLabel.setText(game.font, player.name);
                  game.font.draw(game.mapBatch, remoteLabel, labelPosition.x + 8 - remoteLabel.width / 2, labelPosition.y + 29);
               }
            }
         }
      }
   }
}
