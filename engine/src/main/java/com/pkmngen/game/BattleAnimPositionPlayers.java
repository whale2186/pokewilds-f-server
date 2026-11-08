package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

class BattleAnimPositionPlayers extends Action {
   ArrayList<Vector2> moves_relative;
   Vector2 move;
   public Action.Layer layer = Action.Layer.gui_140;

   public BattleAnimPositionPlayers(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.moves_relative = new ArrayList<>();

      for (int i = 0; i < 72; i++) {
         this.moves_relative.add(new Vector2(2.0F, 0.0F));
      }

      game.player.battleSprite.setPosition(160.0F, 49.0F);
      game.battle.oppPokemon.sprite.setPosition(-48.0F, 88.0F);
      if (game.battle.oppPokemon.sprite.getWidth() <= 48.0F) {
         game.battle.oppPokemon.sprite.setPosition(-42.0F, 88.0F);
      } else if (game.battle.oppPokemon.sprite.getWidth() <= 40.0F) {
         game.battle.oppPokemon.sprite.setPosition(-36.0F, 88.0F);
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
      if (this.moves_relative.isEmpty()) {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      } else {
         this.move = this.moves_relative.get(0);
         float xPos = game.player.battleSprite.getX() - this.move.x;
         game.player.battleSprite.setX(xPos);
         xPos = game.battle.oppPokemon.sprite.getX() + this.move.x;
         game.battle.oppPokemon.sprite.setX(xPos);
         this.moves_relative.remove(0);
      }
   }
}
