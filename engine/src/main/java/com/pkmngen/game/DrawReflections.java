package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

class DrawReflections extends Action {
   public Action.Layer layer = Action.Layer.map_142;
   Sprite blankSprite;
   Pixmap pixels;
   Texture texture;
   Tile tile;
   Color prevColor = new Color();
   int temp1;
   int temp2;
   public int regionWidth = 8;
   public int middleWidth = 1;
   public float uvWidth1 = 1.0F;
   public float uvWidth2 = 1.0F;

   public DrawReflections(Game game) {
      super();
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void firstStep(Game game) {
      if (game.map.timeOfDay.equals("night")) {
         game.currCloudTexture = game.nightTexture;
      }
   }

   @Override
   public void step(Game game) {
      if (!game.player.dontDrawMapDuringBattle) {
         if (game.map.tiles == game.map.overworldTiles) {
            this.prevColor.set(game.mapBatch.getColor());
            game.mapBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.b, 0.5F);

            for (Pokemon pokemon : game.map.onscreenPokemon) {
               if (pokemon.currOwSprite != null && !pokemon.types.contains("GHOST")) {
                  this.drawWithRipple(game, pokemon.currOwSprite, pokemon.position.x, pokemon.position.y - 4.0F - (pokemon.currOwSprite.getRegionHeight() - 8));
               }
            }

            for (Tile tile : game.map.onscreenTiles) {
               this.tile = tile;
               if (this.tile.overSprite != null && !this.tile.drawUpperBelowPlayer) {
                  if (game.rippleTimer < 9) {
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX(),
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           tile.overSprite.getWidth(),
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX(),
                           tile.overSprite.getRegionY(),
                           tile.overSprite.getRegionWidth(),
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                  } else if (game.rippleTimer < 24) {
                     this.regionWidth = tile.overSprite.getRegionWidth() / 2;
                     this.middleWidth = 1;
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX(),
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           this.regionWidth,
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX(),
                           tile.overSprite.getRegionY(),
                           this.regionWidth,
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX() + this.regionWidth,
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           this.middleWidth,
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX() + this.regionWidth,
                           tile.overSprite.getRegionY(),
                           this.middleWidth,
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX() + this.regionWidth + this.middleWidth,
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           this.regionWidth,
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX() + this.regionWidth,
                           tile.overSprite.getRegionY(),
                           this.regionWidth,
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                  } else if (game.rippleTimer < 33) {
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX(),
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           tile.overSprite.getWidth(),
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX(),
                           tile.overSprite.getRegionY(),
                           tile.overSprite.getRegionWidth(),
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                  } else if (game.rippleTimer < 48) {
                     this.regionWidth = tile.overSprite.getRegionWidth() / 2;
                     this.middleWidth = 1;
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX() + this.middleWidth,
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           this.regionWidth - this.middleWidth,
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX(),
                           tile.overSprite.getRegionY(),
                           this.regionWidth - this.middleWidth,
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                     game.mapBatch
                        .draw(
                           tile.overSprite.getTexture(),
                           tile.overSprite.getX() + this.regionWidth,
                           tile.overSprite.getY() - (tile.overSprite.getRegionHeight() - 8),
                           this.regionWidth,
                           tile.overSprite.getHeight(),
                           tile.overSprite.getRegionX() + this.regionWidth,
                           tile.overSprite.getRegionY(),
                           this.regionWidth,
                           tile.overSprite.getRegionHeight(),
                           false,
                           true
                        );
                  }
               }
            }

            if (game.player.hmPokemon != null && game.player.hmPokemon.currOwSprite != null) {
               if (game.player.flyingAction != null) {
                  if (game.player.dirFacing.equals("down")) {
                     this.drawWithRipple(
                        game,
                        game.player.flyingAction.spritePart,
                        game.player.position.x + game.player.flyingAction.xOffset2 + game.player.flyingAction.xOffset,
                        game.player.position.y - 6.0F - 21.0F - game.player.flyingAction.yOffset2 - game.player.flyingAction.yOffset
                     );
                  }

                  this.drawWithRipple(
                     game,
                     game.player.hmPokemon.currOwSprite,
                     game.player.hmPokemon.position.x,
                     game.player.position.y - 16.0F - game.player.flyingAction.yOffset - (game.player.hmPokemon.currOwSprite.getRegionHeight() - 8)
                  );
                  if (!game.player.dirFacing.equals("down") || game.player.flyingAction.pokemon.specie.name.equals("mantine")) {
                     this.drawWithRipple(
                        game,
                        game.player.flyingAction.spritePart,
                        game.player.position.x + game.player.flyingAction.xOffset2 + game.player.flyingAction.xOffset,
                        game.player.position.y - 6.0F - 21.0F - game.player.flyingAction.yOffset2 - game.player.flyingAction.yOffset
                     );
                  }
               } else if (!game.player.currFieldMove.equals("RIDE") && !game.player.hmPokemon.types.contains("GHOST")) {
                  this.drawWithRipple(
                     game,
                     game.player.hmPokemon.currOwSprite,
                     game.player.hmPokemon.position.x,
                     game.player.hmPokemon.position.y - 4.0F - (game.player.hmPokemon.currOwSprite.getRegionHeight() - 8)
                  );
               }
            }

            if (game.player.flyingAction == null) {
               this.drawWithRipple(
                  game, game.player.currSprite, game.player.position.x, game.player.position.y - 4.0F - (game.player.currSprite.getRegionHeight() - 8)
               );
            }

            game.mapBatch.setColor(this.prevColor);
         }
      }
   }

   public void drawWithRipple(Game game, Sprite sprite, float x, float y) {
      if (game.rippleTimer < 9) {
         game.mapBatch
            .draw(sprite.getTexture(), x, y, sprite.getRegionWidth(), sprite.getRegionHeight(), sprite.getU(), sprite.getV(), sprite.getU2(), sprite.getV2());
      } else if (game.rippleTimer < 24) {
         this.uvWidth1 = sprite.getU2() - sprite.getU();
         this.uvWidth2 = this.uvWidth1 / 16.0F;
         this.uvWidth1 /= 2.0F;
         game.mapBatch
            .draw(
               sprite.getTexture(),
               x,
               y,
               9.0F,
               sprite.getRegionHeight(),
               sprite.getU(),
               sprite.getV(),
               sprite.getU2() - (this.uvWidth1 - this.uvWidth2),
               sprite.getV2()
            );
         game.mapBatch
            .draw(
               sprite.getTexture(),
               x + 8.0F + 1.0F,
               y,
               8.0F,
               sprite.getRegionHeight(),
               sprite.getU() + this.uvWidth1,
               sprite.getV(),
               sprite.getU2(),
               sprite.getV2()
            );
      } else if (game.rippleTimer < 33) {
         game.mapBatch
            .draw(sprite.getTexture(), x, y, sprite.getRegionWidth(), sprite.getRegionHeight(), sprite.getU(), sprite.getV(), sprite.getU2(), sprite.getV2());
      } else if (game.rippleTimer < 48) {
         this.uvWidth1 = sprite.getU2() - sprite.getU();
         this.uvWidth2 = this.uvWidth1 / 16.0F;
         this.uvWidth1 /= 2.0F;
         game.mapBatch
            .draw(
               sprite.getTexture(),
               x + 1.0F,
               y,
               7.0F,
               sprite.getRegionHeight(),
               sprite.getU(),
               sprite.getV(),
               sprite.getU2() - this.uvWidth1 - this.uvWidth2,
               sprite.getV2()
            );
         game.mapBatch
            .draw(
               sprite.getTexture(), x + 8.0F, y, 8.0F, sprite.getRegionHeight(), sprite.getU() + this.uvWidth1, sprite.getV(), sprite.getU2(), sprite.getV2()
            );
      }
   }
}
