package com.pkmngen.game;

class DoneAction extends Action {
   DoneAction() {
      super();
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
   }
}
