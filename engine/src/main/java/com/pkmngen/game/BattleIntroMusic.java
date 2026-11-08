package com.pkmngen.game;

class BattleIntroMusic extends Action {
   public Action.Layer layer = Action.Layer.gui_139;

   public BattleIntroMusic(Action nextAction) {
      super();
      this.nextAction = nextAction;
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
      game.currMusic.pause();
      game.currMusic = game.battle.music;
      game.currMusic.play();
      game.insertAction(this.nextAction);
      game.actionStack.remove(this);
   }
}
