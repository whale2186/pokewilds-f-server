package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class LightFadeIn extends Action {
   Sprite sprite;
   public Action.Layer layer = Action.Layer.gui_114;
   int timer = 0;
   int slow = 3;
   Color color = new Color();
   public static boolean goAway = false;

   public LightFadeIn(Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture texture = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.sprite = new Sprite(texture, 0, 0, 160, 144);
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
   public void firstStep(Game game) {
      goAway = false;
   }

   @Override
   public void step(Game game) {
      if (goAway) {
         game.actionStack.remove(this);
      } else {
         if (this.timer >= 2 * this.slow) {
            if (this.timer < 4 * this.slow) {
               this.color.set(game.uiBatch.getColor());
               game.uiBatch.setColor(1.0F, 1.0F, 1.0F, 0.25F);

               for (int i = -1; i < 2; i++) {
                  for (int j = -1; j < 2; j++) {
                     game.uiBatch.draw(this.sprite, 160 * i, 144 * j);
                  }
               }

               game.uiBatch.setColor(this.color);
            } else if (this.timer < 6 * this.slow) {
               this.color.set(game.uiBatch.getColor());
               game.uiBatch.setColor(1.0F, 1.0F, 1.0F, 0.5F);

               for (int i = -1; i < 2; i++) {
                  for (int j = -1; j < 2; j++) {
                     game.uiBatch.draw(this.sprite, 160 * i, 144 * j);
                  }
               }

               game.uiBatch.setColor(this.color);
            } else {
               if (this.timer != 6 * this.slow) {
                  this.color.set(game.uiBatch.getColor());
                  game.uiBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                  for (int i = -1; i < 2; i++) {
                     for (int j = -1; j < 2; j++) {
                        game.uiBatch.draw(this.sprite, 160 * i, 144 * j);
                     }
                  }

                  game.uiBatch.setColor(this.color);
                  return;
               }

               game.insertAction(this.nextAction);
               this.color.set(game.uiBatch.getColor());
               game.uiBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);

               for (int i = -1; i < 2; i++) {
                  for (int j = -1; j < 2; j++) {
                     game.uiBatch.draw(this.sprite, 160 * i, 144 * j);
                  }
               }

               game.uiBatch.setColor(this.color);
            }
         }

         this.timer++;
      }
   }
}
