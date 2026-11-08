package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.pkmngen.game.action.DrawAction;
import com.pkmngen.game.util.GameProfiler;
import com.pkmngen.game.util.TextureCache;

class DrawMapGrass extends DrawAction {
   public Action.Layer layer = Action.Layer.map_120;
   Sprite blankSprite;
   Pixmap pixels;
   Texture texture;
   Vector3 startPos;
   Vector3 endPos;
   Sprite fossilSprite;
   public static Sprite tidalSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/water2.png")), 0, 0, 16, 16);
   Tile tile;
   Tile exteriorTile;
   Color prevColor = new Color();

   public DrawMapGrass(GameProfiler profiler) {
      super(profiler);
      this.fossilSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/fossil1.png")), 0, 0, 16, 16);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   protected void draw(Game game) {
      if (!game.player.dontDrawMapDuringBattle) {
         for (Tile tile : game.map.onscreenGrass) {
            game.mapBatch.draw(tile.overSprite, tile.sprite.getX(), tile.sprite.getY());
         }

         this.prevColor.set(game.mapBatch.getColor());
         game.mapBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.b, 0.5F);

         for (Tile tile : game.map.onscreenExteriorTidal) {
            game.mapBatch.draw(tidalSprite, tile.position.x, tile.position.y);
         }

         game.mapBatch.setColor(this.prevColor);

         for (Tile tile : game.map.onscreenFossils) {
            if (tile.hasItem != null) {
               if (!tile.hasItem.contains("fossil") && !tile.hasItem.equals("old amber")) {
                  if (!tile.isSign() && !tile.hasItem.equals("secret key") && !tile.hasItem.equals("poké ball")) {
                     game.mapBatch.draw(TextureCache.get(Gdx.files.internal("tiles/pokeball1.png")), tile.position.x, tile.position.y);
                  }
               } else {
                  if (tile.hasItem.equals("helix fossil")) {
                     this.fossilSprite.setRegion(0, 16, 16, 16);
                  } else if (tile.hasItem.equals("dome fossil")) {
                     this.fossilSprite.setRegion(0, 32, 16, 16);
                  } else if (tile.hasItem.equals("old amber")) {
                     this.fossilSprite.setRegion(0, 0, 16, 16);
                  } else if (tile.hasItem.equals("root fossil")) {
                     this.fossilSprite.setRegion(0, 48, 16, 16);
                  } else if (tile.hasItem.equals("claw fossil")) {
                     this.fossilSprite.setRegion(0, 64, 16, 16);
                  } else if (tile.hasItem.equals("shield fossil")) {
                     this.fossilSprite.setRegion(0, 80, 16, 16);
                  } else if (tile.hasItem.equals("skull fossil")) {
                     this.fossilSprite.setRegion(0, 96, 16, 16);
                  }

                  game.mapBatch.draw(this.fossilSprite, tile.position.x, tile.position.y);
               }
            }
         }

         for (Pokemon pokemon : game.map.onscreenBurrowed) {
            if (pokemon.drawUpper != null) {
               pokemon.drawUpper.draw(game);
            }
         }
      }
   }
}
