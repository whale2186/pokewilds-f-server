package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class PokemonCaughtEvents extends Action {
   Action displayTextAction;
   Sprite pokeballSprite;
   public Action.Layer layer = Action.Layer.gui_120;
   public boolean done = false;

   public PokemonCaughtEvents(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("pokeball_wiggleSheet1_color.png"));
      this.pokeballSprite = new Sprite(text, 24, 0, 12, 12);
      this.pokeballSprite.setPosition(115.0F, 88.0F);
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
      game.insertAction(this.nextAction);
   }

   @Override
   public void step(Game game) {
      this.pokeballSprite.draw(game.uiBatch);
      if (this.done) {
         game.battle.oppPokemon.sprite.setAlpha(1.0F);
         game.actionStack.remove(this);
      }
   }
}
