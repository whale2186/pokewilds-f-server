package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class PokemonFrame extends Action {
   int timer = 0;
   Pokemon pokemon;
   Sprite bg;
   public boolean isDone = false;

   public PokemonFrame(Pokemon pokemon, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("menu/frame1.png"));
      this.bg = new Sprite(text, 0, 0, 160, 144);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return Action.Layer.gui_0;
   }

   @Override
   public void firstStep(Game game) {
      game.battle.oppPokemon = this.pokemon;
      game.insertAction(new WaitFrames(game, 4, new PlayMusic(this.pokemon, null)));
      game.insertAction(new PokemonIntroAnim(new WaitFrames(game, 30, new SetField(this, "isDone", true, null))));
   }

   @Override
   public void step(Game game) {
      game.uiBatch.draw(this.bg, 0.0F, 0.0F);
      Sprite sprite = new Sprite(this.pokemon.sprite);
      game.uiBatch.draw(sprite, 84 - (int)(this.pokemon.sprite.getWidth() / 2.0F), 48.0F);
      if (this.isDone) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }
}
