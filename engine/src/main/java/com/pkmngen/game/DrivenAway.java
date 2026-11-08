package com.pkmngen.game;

class DrivenAway extends Action {
   Pokemon target;
   String message;

   public DrivenAway(Pokemon t, String m) {
      super();
      this.target = t;
      this.message = m;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
      if (this.target == game.battle.oppPokemon) {
         DrawBattle.shouldDrawOppPokemon = false;
      } else {
         DrawBattle.shouldDrawOwnPokemon = false;
      }

      game.insertAction(
         new DisplayText.Clear(
            game,
            new WaitFrames(
               game,
               3,
               new DisplayText(
                  game,
                  this.message,
                  null,
                  false,
                  true,
                  new SplitAction(
                     new BattleFadeOut(game, null),
                     new BattleFadeOutMusic(
                        game,
                        new SplitAction(
                           new SetField(game, "playerCanMove", true, new SetField(game.musicController, "resumeOverworldMusic", true, null)),
                           new WaitFrames(game, 8, null)
                        )
                     )
                  )
               )
            )
         )
      );
   }
}
