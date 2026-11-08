package com.pkmngen.game;

import java.util.Locale;

class GainExpAnimation extends Action {
   Pokemon pokemon;

   public GainExpAnimation(Pokemon pokemon, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      if (this.pokemon.level < 100 && this.pokemon.calcExpForLevel(this.pokemon.level + 1) <= this.pokemon.exp) {
         this.pokemon.gainLevel(1);
         game.actionStack.remove(this);
         Action action = new DisplayText.Clear(
            game,
            new WaitFrames(
               game,
               3,
               new DisplayText(
                  game,
                  this.pokemon.nickname.toUpperCase(Locale.ROOT) + " grew to level " + this.pokemon.level + "!",
                  "fanfare1.ogg",
                  true,
                  true,
                  new CheckMovesLearned(this.pokemon, this)
               )
            )
         );
         game.insertAction(action);
         this.pokemon.gainedLevel = true;
      } else {
         game.actionStack.remove(this);
         game.insertAction(new DisplayText.Clear(game, new WaitFrames(game, 3, this.nextAction)));
      }
   }
}
