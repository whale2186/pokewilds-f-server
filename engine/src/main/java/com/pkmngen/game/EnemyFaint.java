package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

class EnemyFaint extends Action {
   ArrayList<Vector2> positions;
   ArrayList<Integer> repeats;
   ArrayList<Boolean> playSound;
   Vector2 move;
   Vector2 position;
   Sprite sprite;
   Sprite breathingSprite = null;
   public Action.Layer layer = Action.Layer.gui_120;
   boolean firstStep;
   Sprite helperSprite;

   public EnemyFaint(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.firstStep = true;
      this.position = null;
      this.positions = new ArrayList<>();

      for (int i = 0; i < 7; i++) {
         this.positions.add(new Vector2(0.0F, -8.0F));
      }

      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.repeats = new ArrayList<>();
      this.repeats.add(24);

      for (int i = 0; i < 8; i++) {
         this.repeats.add(2);
      }

      this.playSound = new ArrayList<>();
      if (!game.map.timeOfDay.equals("night")) {
         this.playSound.add(true);
      } else {
         this.playSound.add(false);
      }

      for (int i = 0; i < 15; i++) {
         this.playSound.add(false);
      }

      this.sprite = new Sprite(game.battle.oppPokemon.sprite);
      if (game.battle.oppPokemon.breathingSprite != null) {
         this.breathingSprite = game.battle.oppPokemon.breathingSprite;
      }
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
      if (this.firstStep) {
         DrawBattle.shouldDrawOppPokemon = false;
         this.firstStep = false;
      }

      if (!this.positions.isEmpty() && !this.repeats.isEmpty()) {
         this.sprite.draw(game.uiBatch);
         if (this.breathingSprite != null) {
            this.breathingSprite.draw(game.uiBatch);
         }

         if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.sprite.setRegionHeight(this.sprite.getRegionHeight() + (int)this.positions.get(0).y);
            this.sprite.setSize(this.sprite.getWidth(), this.sprite.getHeight() + (int)this.positions.get(0).y);
            if (this.breathingSprite != null) {
               this.breathingSprite.setRegionHeight(this.breathingSprite.getRegionHeight() + (int)this.positions.get(0).y);
               this.breathingSprite.setSize(this.breathingSprite.getWidth(), this.breathingSprite.getHeight() + (int)this.positions.get(0).y);
            }

            if (this.playSound.get(0) && !(game.battle.drawAction instanceof RegigigasBattle.Draw)) {
               game.musicController.battleVictoryFanfare = true;
            }

            this.positions.remove(0);
            this.repeats.remove(0);
            this.playSound.remove(0);
         }
      } else {
         if (game.type != Game.Type.CLIENT) {
            game.map.currRoute.storedPokemon.remove(game.battle.oppPokemon);
         }

         game.actionStack.remove(game.battle.drawAction.drawEnemyHealthAction);
         game.battle.drawAction.drawEnemyHealthAction = null;
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }
}
