package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class DrawWhiteScreen extends Action {
   public Action.Layer layer = Action.Layer.gui_110;
   int duration = 18;
   Sprite bgSprite;

   public DrawWhiteScreen(Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
   }

   public DrawWhiteScreen(int duration, Action nextAction) {
      this(nextAction);
      this.duration = duration;
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
      this.bgSprite.draw(game.uiBatch);
      this.duration--;
      if (this.duration <= 0) {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      }
   }
}
