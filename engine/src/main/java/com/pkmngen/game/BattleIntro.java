package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class BattleIntro extends Action {
   ArrayList<Sprite> frames;
   Sprite frame;
   public Action.Layer layer = Action.Layer.gui_139;

   public BattleIntro(Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.frames = new ArrayList<>();
      this.frames.add(null);
      this.frames.add(null);
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame1.png"));
      Sprite sprite1 = new Sprite(text1);
      this.frames.add(sprite1);
      this.frames.add(sprite1);
      Texture text2 = TextureCache.get(Gdx.files.internal("battle/intro_frame2.png"));
      Sprite sprite2 = new Sprite(text2);
      this.frames.add(sprite2);
      this.frames.add(sprite2);
      Texture text3 = TextureCache.get(Gdx.files.internal("battle/intro_frame3.png"));
      Sprite sprite3 = new Sprite(text3);
      this.frames.add(sprite3);
      this.frames.add(sprite3);
      this.frames.add(sprite2);
      this.frames.add(sprite2);
      this.frames.add(sprite1);
      this.frames.add(sprite1);
      this.frames.add(null);
      this.frames.add(null);
      Texture text4 = TextureCache.get(Gdx.files.internal("battle/intro_frame4.png"));
      Sprite sprite4 = new Sprite(text4);
      this.frames.add(sprite4);
      this.frames.add(sprite4);
      Texture text5 = TextureCache.get(Gdx.files.internal("battle/intro_frame5.png"));
      Sprite sprite5 = new Sprite(text5);
      this.frames.add(sprite5);
      this.frames.add(sprite5);
      Texture text6 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      Sprite sprite6 = new Sprite(text6);
      this.frames.add(sprite6);
      this.frames.add(sprite6);
      this.frames.add(sprite5);
      this.frames.add(sprite5);
      this.frames.add(sprite4);
      this.frames.add(sprite4);
      ArrayList<Sprite> cpyFrames = new ArrayList<>(this.frames);
      this.frames.addAll(cpyFrames);
      this.frames.addAll(cpyFrames);
      this.frames.add(null);
      this.frames.add(null);
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
      this.frame = this.frames.get(0);
      if (this.frame != null) {
         this.frame.setScale(3.0F);
         this.frame.setPosition(0.0F, 0.0F);
         this.frame.draw(game.uiBatch);
      }

      this.frames.remove(0);
      if (this.frames.isEmpty()) {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      }
   }
}
