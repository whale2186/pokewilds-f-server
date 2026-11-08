package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class LightningFlash extends Action {
   Sprite sprite;
   int timer = 0;
   Color color = new Color();

   public LightningFlash(Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.sprite = new Sprite(text1);
      this.sprite.setPosition(0.0F, 0.0F);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public void step(Game game) {
      if (this.timer % 14 < 7) {
         this.color.set(game.uiBatch.getColor());
         game.uiBatch.setColor(0.5F, 0.5F, 0.5F, 1.0F);

         for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
               game.uiBatch.draw(this.sprite, 160 * i, 144 * j);
            }
         }

         game.uiBatch.setColor(this.color);
      }

      if (this.timer >= 14) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
   }
}
