package com.pkmngen.game;

class DepleteFriendlyHealth extends Action {
   public Action.Layer layer = Action.Layer.gui_129;
   Pokemon pokemon;
   boolean firstStep;
   int timer;
   int removeNumber;
   int targetSize;
   int damage = 0;

   public DepleteFriendlyHealth(Pokemon friendlyPokemon, Action nextAction) {
      this(friendlyPokemon, 0, nextAction);
   }

   public DepleteFriendlyHealth(Pokemon friendlyPokemon, int damage, Action nextAction) {
      super();
      this.pokemon = friendlyPokemon;
      this.nextAction = nextAction;
      this.firstStep = true;
      this.timer = 3;
      this.removeNumber = 2;
      this.damage = damage;
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
      if (this.firstStep) {
         if (this.damage > 0) {
            int currHealth = game.player.currPokemon.currentStats.get("hp");
            int finalHealth = currHealth - this.damage > 0 ? currHealth - this.damage : 0;
            game.player.currPokemon.currentStats.put("hp", finalHealth);
         }

         this.targetSize = (int)Math.ceil(this.pokemon.currentStats.get("hp").intValue() * 48.0F / this.pokemon.maxStats.get("hp").intValue());
         this.firstStep = false;
      }

      this.timer++;
      if (this.timer >= 4) {
         this.timer = 0;
         game.battle.drawAction.drawFriendlyHealthAction.currHealth = game.battle.drawAction.drawFriendlyHealthAction.currHealth - this.removeNumber;
         game.battle.drawAction.drawFriendlyHealthAction.currHealthRemaining = game.battle.drawAction.drawFriendlyHealthAction.currHealth
            * this.pokemon.maxStats.get("hp")
            / 48;
         game.battle.drawAction.drawFriendlyHealthAction.updateHealthBarColor();
         if (this.removeNumber == 2) {
            this.removeNumber = 1;
         } else {
            this.removeNumber = 2;
         }

         if (game.battle.drawAction.drawFriendlyHealthAction.currHealth <= this.targetSize) {
            game.battle.drawAction.drawFriendlyHealthAction.currHealth = this.targetSize;
            game.battle.drawAction.drawFriendlyHealthAction.currHealthRemaining = game.player.currPokemon.currentStats.get("hp");
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
