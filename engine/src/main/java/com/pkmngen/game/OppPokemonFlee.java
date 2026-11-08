package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

class OppPokemonFlee extends Action {
   ArrayList<Vector2> positions;
   Vector2 position;
   Sprite sprite;
   ArrayList<Integer> repeats;
   String sound;
   ArrayList<String> sounds;
   public Action.Layer layer = Action.Layer.gui_120;
   Sprite helperSprite;

   public OppPokemonFlee(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.position = new Vector2(game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY());
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(0.0F, 0.0F));

      for (int i = 0; i < 13; i++) {
         this.positions.add(new Vector2(8.0F, 0.0F));
      }

      this.repeats = new ArrayList<>();
      this.repeats.add(15);

      for (int i = 0; i < 13; i++) {
         this.repeats.add(2);
      }

      this.sounds = new ArrayList<>();

      for (int i = 0; i < 14; i++) {
         this.sounds.add(null);
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
      if (this.positions.isEmpty()) {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      } else {
         this.sound = this.sounds.get(0);
         if (this.sound != null) {
            game.insertAction(new PlayMusic(this.sound, null));
            this.sounds.set(0, null);
         }

         game.battle.oppPokemon.sprite.setPosition(this.position.x, this.position.y);
         game.battle.oppPokemon.sprite.draw(game.uiBatch);
         if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.position = this.position.add(this.positions.get(0));
            this.positions.remove(0);
            this.repeats.remove(0);
            this.sounds.remove(0);
         }
      }
   }
}
