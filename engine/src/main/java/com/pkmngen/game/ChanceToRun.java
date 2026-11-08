package com.pkmngen.game;

import java.util.Locale;

class ChanceToRun extends Action {
   public Action.Layer layer = Action.Layer.gui_120;

   public ChanceToRun(Game game, Action nextAction) {
      super();
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
      int x = game.battle.oppPokemon.currentStats.get("speed") % 256;
      x *= 2;
      System.out.println("Chance to run, x: " + String.valueOf(x));
      if (x > 255) {
         String textString = "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " ran!";
         game.insertAction(
            new DisplayText(
               game,
               textString,
               null,
               null,
               new SplitAction(
                  new OppPokemonFlee(game, new SplitAction(new BattleFadeOut(game, null), new BattleFadeOutMusic(game, null))),
                  new WaitFrames(game, 8, new PlayMusic("run1", null))
               )
            )
         );
         game.player.adrenaline = 0;
         game.actionStack.remove(this);
      } else {
         if (game.battle.oppPokemon.angry > 0) {
            x *= 2;
            if (x > 255) {
               x = 255;
            }
         } else if (game.battle.oppPokemon.eating > 0) {
            x /= 4;
         }

         int r = game.map.rand.nextInt(256);
         if (r < x) {
            String textString = "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " ran!";
            game.insertAction(
               new DisplayText(
                  game,
                  textString,
                  null,
                  null,
                  new SplitAction(
                     new OppPokemonFlee(game, new SplitAction(new BattleFadeOut(game, null), new BattleFadeOutMusic(game, null))),
                     new WaitFrames(game, 8, new PlayMusic("run1", null))
                  )
               )
            );
            game.player.adrenaline = 0;
            game.actionStack.remove(this);
         } else {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }
}
