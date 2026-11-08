package com.pkmngen.game;

class DepleteEnemyHealth extends Action {
   public Action.Layer layer = Action.Layer.gui_129;
   boolean firstStep;
   int timer;
   int removeNumber;
   int targetSize;
   int damage = 0;

   public DepleteEnemyHealth(Game game, Action nextAction) {
      this(game, 0, nextAction);
   }

   public DepleteEnemyHealth(Game game, int damage, Action nextAction) {
      super();
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
            int currHealth = game.battle.oppPokemon.currentStats.get("hp");
            int finalHealth = currHealth - this.damage > 0 ? currHealth - this.damage : 0;
            game.battle.oppPokemon.currentStats.put("hp", finalHealth);
         }

         this.targetSize = (int)Math.ceil(
            game.battle.oppPokemon.currentStats.get("hp").intValue() * 48.0F / game.battle.oppPokemon.maxStats.get("hp").intValue()
         );
         this.firstStep = false;
      }

      this.timer++;
      if (this.timer >= 4) {
         this.timer = 0;
         game.battle.drawAction.drawEnemyHealthAction.currHealth = game.battle.drawAction.drawEnemyHealthAction.currHealth - this.removeNumber;
         game.battle.drawAction.drawEnemyHealthAction.updateHealthBarColor();
         if (this.removeNumber == 2) {
            this.removeNumber = 1;
         } else {
            this.removeNumber = 2;
         }

         if (game.battle.drawAction.drawEnemyHealthAction.currHealth <= this.targetSize) {
            game.battle.drawAction.drawEnemyHealthAction.currHealth = this.targetSize;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
