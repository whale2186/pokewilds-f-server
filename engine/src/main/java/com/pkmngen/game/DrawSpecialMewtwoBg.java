package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class DrawSpecialMewtwoBg extends Action {
   public Action.Layer layer = Action.Layer.map_141;
   Sprite bgSprite;

   public DrawSpecialMewtwoBg() {
      super();
      Texture text = TextureCache.get(Gdx.files.internal("lab1_fl1.png"));
      this.bgSprite = new Sprite(text, 0, 0, 479, 448);
      this.bgSprite.setPosition(-79.0F, -177.0F);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      this.bgSprite.draw(game.mapBatch);
   }
}
