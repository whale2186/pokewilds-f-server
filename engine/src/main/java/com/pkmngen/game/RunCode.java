package com.pkmngen.game;

class RunCode extends Action {
   Runnable runnable;

   public RunCode(Runnable runnable, Action nextAction) {
      super();
      this.runnable = runnable;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      this.runnable.run();
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
