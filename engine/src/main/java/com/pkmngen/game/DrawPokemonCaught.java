package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class DrawPokemonCaught extends Action {
   public Action.Layer layer = Action.Layer.gui_110;
   Sprite pokeball;

   public DrawPokemonCaught(Game game) {
      super();
      Texture text = TextureCache.get(Gdx.files.internal("pokeball1.png"));
      this.pokeball = new Sprite(text, 0, 0, 12, 12);
      this.pokeball.setScale(2.0F);
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
      for (int i = 0; i < game.player.pokemon.size(); i++) {
         game.uiBatch.draw(this.pokeball, i * 16 * 3, 420.0F);
      }
   }
}
