package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;

class DrawPlayerLower extends Action {
   public Action.Layer layer = Action.Layer.map_130;
   Sprite spritePart = new Sprite();
   int fishingOffsetX = 0;
   int fishingOffsetY = 0;

   public DrawPlayerLower() {
      super();
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (!game.player.isSitting) {
         if (!game.player.isSleeping && !game.player.currFieldMove.equals("FLY") && !game.player.currFieldMove.equals("SURF")) {
            this.spritePart.set(game.player.currSprite);
            this.fishingOffsetX = 0;
            this.fishingOffsetY = 0;
            if (game.player.isFishing) {
               if (game.player.dirFacing.equals("left")) {
                  this.fishingOffsetX = -8;
               } else if (game.player.dirFacing.equals("down")) {
                  this.fishingOffsetY = -8;
               }
            }

            this.spritePart.setRegionY(game.player.spriteOffsetY + 8);
            this.spritePart.setRegionHeight((int)this.spritePart.getHeight() - 8);
            game.mapBatch
               .draw(
                  this.spritePart,
                  game.player.position.x + this.fishingOffsetX,
                  game.player.position.y + 4.0F + DrawPlayerUpper.pokemonOffsetY + this.fishingOffsetY
               );
         }
      }
   }
}
