package com.pkmngen.game;

class CheckShouldSwitch extends Action {
   public Action.Layer layer = Action.Layer.map_129;

   public CheckShouldSwitch(Action nextAction) {
      super();
      this.nextAction = nextAction;
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
      if (game.player.currPokemon.currentStats.get("hp") > 0) {
         game.insertAction(this.nextAction);
      } else {
         game.battle.network.expectPlayerSwitch = true;
         game.insertAction(new DisplayText.Clear(game, new WaitFrames(game, 3, new DrawPokemonMenu.Intro(new DrawPokemonMenu(game, null)))));
      }
   }
}
