package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;

class ChangePlayerColor extends Action {
   int index = 0;
   ArrayList<Color> colors = new ArrayList<>();

   public ChangePlayerColor(Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.colors.add(new Color(0.972549F, 0.21960784F, 0.03137255F, 1.0F));
      this.colors.add(new Color(0.9411765F, 0.3137255F, 0.1882353F, 1.0F));
      this.colors.add(new Color(0.3137255F, 0.28235295F, 0.972549F, 1.0F));
      this.colors.add(Color.CYAN);
      this.colors.add(new Color(0.21960784F, 0.72156864F, 0.09411765F, 1.0F));
      this.colors.add(Color.MAGENTA);
      this.colors.add(Color.MAROON);
      this.colors.add(Color.YELLOW);
      this.colors.add(new Color(0.47058824F, 0.3137255F, 0.09411765F, 1.0F));
      this.colors.add(Color.OLIVE);
      this.colors.add(Color.TEAL);
      this.colors.add(Color.RED);
      this.colors.add(Color.PURPLE);
      this.colors.add(new Color(1.0F, 0.4509804F, 0.78431374F, 1.0F));
   }

   @Override
   public void step(Game game) {
      if (InputProcessor.leftJustPressed) {
         this.index--;
         if (this.index < 0) {
            this.index = this.colors.size() - 1;
         }

         game.player.setColor(this.colors.get(this.index));
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      } else if (InputProcessor.rightJustPressed) {
         this.index++;
         if (this.index >= this.colors.size()) {
            this.index = 0;
         }

         game.player.setColor(this.colors.get(this.index));
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      }

      if (InputProcessor.bJustPressed) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }
}
