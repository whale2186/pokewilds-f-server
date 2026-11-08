package com.pkmngen.game;

import com.pkmngen.game.action.DrawAction;
import com.pkmngen.game.util.GameProfiler;

class DrawPokemonLowers extends DrawAction {
   public Action.Layer layer = Action.Layer.map_130;

   public DrawPokemonLowers(GameProfiler profiler) {
      super(profiler);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   protected void draw(Game game) {
      if (!game.player.dontDrawMapDuringBattle) {
         for (Pokemon pokemon : game.map.onscreenPokemon) {
            if (pokemon.drawLower != null) {
               pokemon.drawLower.draw(game);
            }
         }

         if (game.player.hmPokemon != null && game.player.hmPokemon.drawLower != null) {
            game.player.hmPokemon.drawLower.draw(game);
         }

         game.player.drawPlayerLower.step(game);
      }
   }
}
