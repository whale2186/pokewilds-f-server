package com.pkmngen.game;

public class Action {
   Action nextAction = null;
   boolean firstStep = true;
   Object[] params;
   public Action.Layer layer;

   public Action(Object... parameters) {
      this.params = parameters;
   }

   public void append(Action action) {
      Action currAction = this;

      while (currAction.nextAction != null) {
         currAction = currAction.nextAction;
      }

      currAction.nextAction = action;
   }

   public void firstStep(Game game) {
   }

   public String getCamera() {
      return "map";
   }

   public Action.Layer getLayer() {
      return Action.Layer.map_0;
   }

   public void step(Game game) {
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
      if (this.nextAction != null) {
         this.nextAction.step(game);
      }
   }

   public enum Layer {
      gui_0,
      gui_100,
      gui_103,
      gui_104,
      gui_105,
      gui_106,
      gui_107,
      gui_108,
      gui_109,
      gui_110,
      gui_111,
      gui_114,
      gui_120,
      gui_129,
      gui_130,
      gui_131,
      gui_139,
      gui_140,
      gui_500,
      gui_5000,
      map_neg_1,
      map_0,
      map_1,
      map_107,
      map_108,
      map_109,
      map_110,
      map_113,
      map_114,
      map_115,
      map_116,
      map_119,
      map_120,
      map_121,
      map_129,
      map_130,
      map_131,
      map_140,
      map_141,
      map_142,
      map_151,
      map_500,
      map_5000;
   }
}
