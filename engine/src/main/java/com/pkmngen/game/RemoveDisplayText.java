package com.pkmngen.game;

class RemoveDisplayText extends Action {
   public RemoveDisplayText(Action nextAction) {
      super();
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(game.displayTextAction);
      game.displayTextAction = null;
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
