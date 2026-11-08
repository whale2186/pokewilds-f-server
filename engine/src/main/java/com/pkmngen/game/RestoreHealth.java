package com.pkmngen.game;

class RestoreHealth extends Action {
   Pokemon pokemon;
   int damage = 0;
   int finalHealth;
   int timer = 3;
   int removeNumber = 1;
   int sign = 1;

   public RestoreHealth(Pokemon pokemon, int damage, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.damage = damage;
      this.nextAction = nextAction;
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public void firstStep(Game game) {
      int currHealth = this.pokemon.currentStats.get("hp");
      this.finalHealth = currHealth - this.damage;
      if (this.finalHealth > this.pokemon.maxStats.get("hp")) {
         this.finalHealth = this.pokemon.maxStats.get("hp");
      } else if (this.finalHealth < 0) {
         this.finalHealth = 0;
      }

      if (this.finalHealth > this.pokemon.currentStats.get("hp")) {
         this.sign = -1;
      }

      this.removeNumber = this.removeNumber * this.sign;
   }

   @Override
   public void step(Game game) {
      this.timer++;
      if (this.timer >= 2) {
         this.timer = 0;
         int removeHealth = (int)Math.ceil(this.removeNumber / 48.0F * this.pokemon.maxStats.get("hp").intValue());
         if (removeHealth <= 0) {
            removeHealth = 1 * this.sign;
         }

         this.pokemon.currentStats.put("hp", this.pokemon.currentStats.get("hp") - removeHealth);
         if (game.battle.drawAction != null) {
            if (this.pokemon == game.battle.oppPokemon) {
               game.battle.drawAction.drawEnemyHealthAction.currHealth = (int)Math.ceil(
                  this.pokemon.currentStats.get("hp").intValue() * 48.0F / this.pokemon.maxStats.get("hp").intValue()
               );
               game.battle.drawAction.drawEnemyHealthAction.updateHealthBarColor();
            } else if (this.pokemon == game.player.currPokemon) {
               game.battle.drawAction.drawFriendlyHealthAction.currHealth = (int)Math.ceil(
                  this.pokemon.currentStats.get("hp").intValue() * 48.0F / this.pokemon.maxStats.get("hp").intValue()
               );
               game.battle.drawAction.drawFriendlyHealthAction.currHealthRemaining = this.pokemon.currentStats.get("hp");
               game.battle.drawAction.drawFriendlyHealthAction.updateHealthBarColor();
            }
         }

         this.removeNumber = 1;
         this.removeNumber = this.removeNumber * this.sign;
         if (this.sign == 1 && this.pokemon.currentStats.get("hp") <= this.finalHealth
            || this.sign == -1 && this.pokemon.currentStats.get("hp") >= this.finalHealth) {
            this.pokemon.currentStats.put("hp", this.finalHealth);
            if (game.battle.drawAction != null && game.battle.drawAction.drawFriendlyHealthAction != null && this.pokemon == game.player.currPokemon) {
               game.battle.drawAction.drawFriendlyHealthAction.currHealthRemaining = this.pokemon.currentStats.get("hp");
            }

            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
