package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.util.TextureCache;

class EscapeRope extends Action {
   Sprite sprite;
   Sprite sprite2;
   public Action.Layer layer = Action.Layer.gui_114;
   int timer = 0;
   int slow = 3;

   public EscapeRope(Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.sprite = new Sprite(text1);
      this.sprite.setPosition(0.0F, 0.0F);
      this.sprite2 = new Sprite(text1);
      this.sprite.setPosition(-144.0F, 0.0F);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (this.timer < 2 * this.slow) {
         if (this.timer == 0) {
            game.insertAction(new PlayMusic("enter1", null));
         }
      } else if (this.timer < 4 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.25F);
         this.sprite2.draw(game.uiBatch, 0.25F);
      } else if (this.timer < 6 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.5F);
         this.sprite2.draw(game.uiBatch, 0.5F);
      } else if (this.timer < 22 * this.slow) {
         if (this.timer == 6 * this.slow) {
            game.map.interiorTilesIndex = 100;
            game.map.tiles = game.map.overworldTiles;
            game.player.dirFacing = "down";
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
            ScreenUtils.clear(0.0F, 0.0F, 0.0F, 1.0F, true);
            if (!game.map.timeOfDay.equals("night")) {
               game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            } else {
               game.mapBatch.setColor(new Color(PkmnMap.nightColor));
            }
         }

         this.sprite.draw(game.uiBatch, 1.0F);
         this.sprite2.draw(game.uiBatch, 1.0F);
      } else if (this.timer < 24 * this.slow) {
         if (this.timer == 22 * this.slow) {
            game.insertAction(new PlayMusic("exit1", null));
         }

         this.sprite.draw(game.uiBatch, 0.75F);
         this.sprite2.draw(game.uiBatch, 0.75F);
      } else if (this.timer < 26 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.5F);
         this.sprite2.draw(game.uiBatch, 0.5F);
      } else if (this.timer < 28 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.25F);
         this.sprite2.draw(game.uiBatch, 0.25F);
      } else {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
   }
}
