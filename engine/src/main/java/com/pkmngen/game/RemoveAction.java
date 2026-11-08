package com.pkmngen.game;

class RemoveAction extends Action {
   Action action;

   public RemoveAction(Action action, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.action = action;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
      game.actionStack.remove(this.action);
      game.insertAction(this.nextAction);
   }
}
