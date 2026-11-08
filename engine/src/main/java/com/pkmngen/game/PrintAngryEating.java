package com.pkmngen.game;

import java.util.Locale;

class PrintAngryEating extends Action {
   public Action.Layer layer = Action.Layer.gui_120;

   public PrintAngryEating(Game game, Action nextAction) {
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
      if (game.battle.oppPokemon.angry > 0) {
         String textString = "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " is angry!";
         game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
         game.actionStack.remove(this);
         game.battle.oppPokemon.angry--;
         if (game.battle.oppPokemon.angry <= 0) {
            int baseCatchRate = game.battle.oppPokemon.baseStats.get("catchRate");
            game.battle.oppPokemon.currentStats.put("catchRate", baseCatchRate);
         }
      } else if (game.battle.oppPokemon.eating > 0) {
         String textString = "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " is eating!";
         game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
         game.actionStack.remove(this);
         game.battle.oppPokemon.eating--;
      } else {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      }
   }
}
