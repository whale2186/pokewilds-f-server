package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class DrawMobileControls extends Action {
   public static Sprite upArrowSprite = new Sprite();
   public static Sprite downArrowSprite = new Sprite();
   public static Sprite leftArrowSprite = new Sprite();
   public static Sprite rightArrowSprite = new Sprite();
   public static Sprite aSprite = new Sprite();
   public static Sprite bSprite = new Sprite();
   public static Sprite startSprite = new Sprite();

   public DrawMobileControls(Game game) {
      super();
      Texture text = TextureCache.get(Gdx.files.internal("gb_arrow2.png"));
      upArrowSprite = new Sprite(text, 0, 0, 24, 24);
      downArrowSprite = new Sprite(text, 0, 0, 24, 24);
      downArrowSprite.flip(false, true);
      rightArrowSprite = new Sprite(text, 0, 0, 24, 24);
      rightArrowSprite.rotate90(true);
      leftArrowSprite = new Sprite(text, 0, 0, 24, 24);
      leftArrowSprite.rotate90(false);
      aSprite = new Sprite(text, 0, 0, 24, 24);
      bSprite = new Sprite(text, 0, 0, 24, 24);
      startSprite = new Sprite(text, 0, 0, 24, 24);
   }

   @Override
   public void firstStep(Game game) {
      float scaleX = 160.0F / game.currScreen.x;
      int offsetY = (int)((game.currScreen.y - 144.0F / scaleX) / 2.0F * scaleX) + 32;
      upArrowSprite.setPosition(30.0F, 90 - offsetY - 35);
      downArrowSprite.setPosition(30.0F, 40 - offsetY - 35);
      leftArrowSprite.setPosition(5.0F, 65 - offsetY - 35);
      rightArrowSprite.setPosition(55.0F, 65 - offsetY - 35);
      bSprite.setPosition(100.0F, 75 - offsetY - 35);
      aSprite.setPosition(125.0F, 85 - offsetY - 35);
      startSprite.setPosition(100.0F, 45 - offsetY - 35);
   }

   @Override
   public void step(Game game) {
      upArrowSprite.draw(game.uiBatch);
      downArrowSprite.draw(game.uiBatch);
      leftArrowSprite.draw(game.uiBatch);
      rightArrowSprite.draw(game.uiBatch);
      aSprite.draw(game.uiBatch);
      bSprite.draw(game.uiBatch);
      startSprite.draw(game.uiBatch);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return Action.Layer.gui_0;
   }
}
