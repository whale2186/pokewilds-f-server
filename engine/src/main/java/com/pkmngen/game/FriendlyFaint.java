package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

class FriendlyFaint extends Action {
   ArrayList<Vector2> positions;
   ArrayList<Integer> repeats;
   ArrayList<Boolean> playSound;
   Vector2 move;
   Vector2 position;
   Sprite sprite;
   public Action.Layer layer = Action.Layer.gui_120;
   boolean firstStep;

   public FriendlyFaint(Game game, Action nextAction) {
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
      this.playSound.add(true);

      for (int i = 0; i < 15; i++) {
         this.playSound.add(false);
      }

      this.sprite = new Sprite(game.player.currPokemon.backSprite);
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
         game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction);
         game.battle.drawAction.drawFriendlyHealthAction = null;
         game.actionStack.remove(game.battle.drawAction.drawFriendlyPokemonAction);
         game.battle.drawAction.drawFriendlyPokemonAction = null;
         game.player.currPokemon.status = null;
         game.player.currPokemon.statusCounter = 0;
         game.player.currPokemon.volatileStatus.clear();
         game.player.currPokemon.volatileStatusCounter.clear();
         this.firstStep = false;
      }

      if (!this.positions.isEmpty() && !this.repeats.isEmpty()) {
         this.sprite.draw(game.uiBatch);
         if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.sprite.setRegionHeight(this.sprite.getRegionHeight() + (int)this.positions.get(0).y / 2);
            this.sprite.setSize(this.sprite.getWidth(), this.sprite.getHeight() + (int)this.positions.get(0).y / 2);
            if (this.playSound.get(0)) {
               game.insertAction(
                  new PlaySound(game.player.currPokemon, new WaitFrames(game, game.player.currPokemon.specie.cryLengthInFrames(), this.nextAction))
               );
            }

            this.positions.remove(0);
            this.repeats.remove(0);
            this.playSound.remove(0);
         }
      } else {
         game.actionStack.remove(this);
      }
   }
}
