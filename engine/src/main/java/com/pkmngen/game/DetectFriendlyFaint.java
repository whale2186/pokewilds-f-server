package com.pkmngen.game;

import java.util.Locale;

class DetectFriendlyFaint extends Action {
   public Action.Layer layer = Action.Layer.gui_129;
   public Pokemon pokemon;
   public boolean checkEndOfBattle;

   public DetectFriendlyFaint(Pokemon pokemon, Action nextAction) {
      this(pokemon, true, nextAction);
   }

   public DetectFriendlyFaint(Pokemon pokemon, boolean checkEndOfBattle, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.checkEndOfBattle = checkEndOfBattle;
      this.nextAction = nextAction;
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
      game.actionStack.remove(this);
      if (this.pokemon.currentStats.get("hp") <= 0) {
         if (game.battle.oppPokemon.level - this.pokemon.level < 30) {
            this.pokemon.gainHappiness(-1);
         } else if (this.pokemon.happiness < 200) {
            this.pokemon.gainHappiness(-5);
         } else {
            this.pokemon.gainHappiness(-10);
         }

         Action nextAction = new FriendlyFaint(
            game,
            new RemoveDisplayText(
               new WaitFrames(game, 3, new DisplayText(game, this.pokemon.nickname.toUpperCase(Locale.ROOT) + " fainted!", null, null, null))
            )
         );
         if (this.checkEndOfBattle) {
            nextAction.append(new CheckWhitedOut(null));
            nextAction.append(new CheckShouldSwitch(null));
         } else {
            nextAction.append(this.nextAction);
         }

         game.insertAction(nextAction);
      } else {
         game.insertAction(this.nextAction);
      }
   }
}
