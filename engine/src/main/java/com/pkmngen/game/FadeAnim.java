package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class FadeAnim extends Action {
   Sprite sprite;
   public Action.Layer layer = Action.Layer.gui_114;
   int timer = 0;
   int slow = 1;

   public FadeAnim(Game game, int slow, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.slow = slow;
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.sprite = new Sprite(text1);
      this.sprite.setPosition(0.0F, 0.0F);
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
   }

   @Override
   public void step(Game game) {
      if (this.timer >= 2 * this.slow) {
         if (this.timer < 4 * this.slow) {
            this.sprite.draw(game.uiBatch, 0.25F);
         } else if (this.timer < 6 * this.slow) {
            this.sprite.draw(game.uiBatch, 0.5F);
         } else if (this.timer < 12 * this.slow) {
            this.sprite.draw(game.uiBatch, 1.0F);
         } else if (this.timer < 14 * this.slow) {
            this.sprite.draw(game.uiBatch, 0.75F);
         } else if (this.timer < 16 * this.slow) {
            this.sprite.draw(game.uiBatch, 0.5F);
         } else if (this.timer < 18 * this.slow) {
            this.sprite.draw(game.uiBatch, 0.25F);
         } else {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }

      this.timer++;
   }
}
