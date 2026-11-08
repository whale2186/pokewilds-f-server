package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class BattleIntroAnim1 extends Action {
   ArrayList<Sprite> frames;
   Sprite frame;
   Sprite bgSprite;
   public Action.Layer layer = Action.Layer.gui_139;

   public BattleIntroAnim1(Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.frames = new ArrayList<>();
      Texture text = TextureCache.get(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));

      for (int i = 0; i < 28; i++) {
         this.frames.add(new Sprite(text, i * 160, 0, 160, 144));
      }

      int numFrames = 42;

      for (int i = 0; i < numFrames; i++) {
         this.frames.add(new Sprite(text, 4320, 0, 160, 144));
      }

      text = TextureCache.get(Gdx.files.internal("battle/intro_frame3.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
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
      if (this.frames.isEmpty()) {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
         this.nextAction.step(game);
      } else {
         this.frame = this.frames.get(0);
         if (this.frame != null) {
            this.frame.draw(game.uiBatch);
         }

         this.frames.remove(0);
         game.uiBatch.draw(this.bgSprite, -160.0F, 0.0F);
         game.uiBatch.draw(this.bgSprite, 160.0F, 0.0F);
         game.uiBatch.draw(this.bgSprite, 0.0F, -144.0F);
         game.uiBatch.draw(this.bgSprite, 0.0F, 144.0F);
      }
   }
}
