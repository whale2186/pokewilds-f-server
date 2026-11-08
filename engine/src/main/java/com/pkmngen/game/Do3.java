package com.pkmngen.game;

class Do3 extends Action {
   Do3.DoThis doThis;

   public Do3(Do3.DoThis doThis, Action nextAction) {
      super();
      this.doThis = doThis;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      this.doThis.doThis();
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }

   @FunctionalInterface
   interface DoThis {
      void doThis();
   }
}
