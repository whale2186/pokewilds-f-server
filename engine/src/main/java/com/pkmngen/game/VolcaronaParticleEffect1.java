package com.pkmngen.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;

class VolcaronaParticleEffect1 extends Action {
   public Action.Layer layer = Action.Layer.gui_0;
   int timer = 0;
   Pixmap pixmap;
   Texture texture;
   int[] offsets = new int[]{0, 0, 0, 0};
   Texture[] textures = new Texture[4];
   int oneFourth = 1;

   public VolcaronaParticleEffect1(Action nextAction) {
      super();
      this.nextAction = nextAction;
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
      this.pixmap = Pixmap.createFromFrameBuffer(0, 0, 160, 144);

      for (int i = 0; i < 4; i++) {
         this.textures[i] = new Texture(this.pixmap.getWidth(), this.pixmap.getHeight(), Format.RGBA8888);
      }
   }

   @Override
   public void step(Game game) {
      if (this.timer == 0) {
         game.uiBatch.flush();
         this.pixmap = Pixmap.createFromFrameBuffer(0, 0, 160, 144);
         this.oneFourth = this.pixmap.getHeight() / 4;

         for (int i = 0; i < 4; i++) {
            this.textures[i].draw(this.pixmap, 0, 0 - this.oneFourth * i);
         }
      }

      for (int i = 0; i < 4; i++) {
         game.uiBatch.draw(this.textures[i], 0.0F, 20.0F);
      }

      this.timer++;
      if (this.timer > 60) {
         this.timer = 0;
      }
   }
}
