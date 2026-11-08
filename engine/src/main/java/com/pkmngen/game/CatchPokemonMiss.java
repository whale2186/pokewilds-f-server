package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import java.util.ArrayList;

class CatchPokemonMiss extends Action {
   ArrayList<Float> alphas;
   public Action.Layer layer = Action.Layer.gui_120;
   Sprite helperSprite;

   public CatchPokemonMiss(Game game, Action nextAction) {
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
      String textString = "You missed the POKéMON!";
      game.insertAction(new WaitFrames(game, 3, new DisplayText(game, textString, null, null, this.nextAction)));
      game.actionStack.remove(this);
   }
}
