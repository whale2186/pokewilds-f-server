package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

class MovePlayerOffScreen extends Action {
   public Action.Layer layer = Action.Layer.gui_140;
   ArrayList<Vector2> positions;
   ArrayList<Integer> repeats;
   Vector2 move;
   Vector2 position;
   Sprite sprite;

   public MovePlayerOffScreen(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.position = null;
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(0.0F, 0.0F));

      for (int i = 0; i < 3; i++) {
         this.positions.add(new Vector2(-8.0F, 0.0F));
         this.positions.add(new Vector2(-16.0F, 0.0F));
         this.positions.add(new Vector2(-8.0F, 0.0F));
         this.positions.add(new Vector2(-16.0F, 0.0F));
         this.positions.add(new Vector2(-8.0F, 0.0F));
         this.positions.add(new Vector2(-16.0F, 0.0F));
      }

      this.repeats = new ArrayList<>();
      this.repeats.add(38);
      this.repeats.add(2);

      for (int i = 0; i < 5; i++) {
         this.repeats.add(3);
      }

      this.sprite = game.player.battleSprite;
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
      if (!this.positions.isEmpty() && !this.repeats.isEmpty()) {
         if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.position = new Vector2(game.player.battleSprite.getX(), game.player.battleSprite.getY());
            this.position.add(this.positions.get(0));
            this.sprite.setPosition(this.position.x, this.position.y);
            this.positions.remove(0);
            this.repeats.remove(0);
         }
      } else {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      }
   }
}
