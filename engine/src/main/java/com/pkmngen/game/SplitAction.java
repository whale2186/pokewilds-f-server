package com.pkmngen.game;

class SplitAction extends Action {
   private Action nextAction2;
   String camera = "map";

   public SplitAction(Action nextAction1, Action nextAction2) {
      super();
      this.nextAction2 = nextAction1;
      this.nextAction = nextAction2;
   }

   @Override
   public String getCamera() {
      return this.camera;
   }

   @Override
   public Action.Layer getLayer() {
      return Action.Layer.map_500;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
      game.insertAction(this.nextAction2);
      game.insertAction(this.nextAction);
   }
}
