package com.pkmngen.game;

class WaitFrames extends Action {
   int length;
   public Action.Layer layer = Action.Layer.gui_110;

   public WaitFrames(Game game, int length, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.length = length;
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
      this.length--;
      if (this.length <= 0) {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      }
   }
}
