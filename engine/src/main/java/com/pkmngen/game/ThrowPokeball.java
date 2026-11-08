package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class ThrowPokeball extends Action {
   Sprite pokeballSprite;
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite> sprites;
   Sprite sprite;
   ArrayList<Integer> repeats;
   String sound;
   ArrayList<String> sounds;
   public Action.Layer layer = Action.Layer.gui_120;

   public ThrowPokeball(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("pokeball1_color.png"));
      this.pokeballSprite = new Sprite(text, 0, 0, 12, 12);
      this.position = new Vector2(34.0F, 56.0F);
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 4.0F));
      this.positions.add(new Vector2(8.0F, 4.0F));
      this.positions.add(new Vector2(8.0F, 1.0F));
      this.positions.add(new Vector2(8.0F, -1.0F));
      this.positions.add(new Vector2(8.0F, -1.0F));
      this.positions.add(new Vector2(8.0F, -1.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(-18.0F, -22.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.sprites = new ArrayList<>();

      for (int i = 0; i < 11; i++) {
         this.sprites.add(this.pokeballSprite);
      }

      this.sprites.add(null);
      text = TextureCache.get(Gdx.files.internal("poof_sheet1.png"));
      this.sprites.add(new Sprite(text, 0, 0, 48, 48));
      this.sprites.add(new Sprite(text, 48, 0, 48, 48));
      this.sprites.add(new Sprite(text, 96, 0, 48, 48));
      this.sprites.add(new Sprite(text, 144, 0, 48, 48));
      this.sprites.add(new Sprite(text, 192, 0, 48, 48));
      this.repeats = new ArrayList<>();

      for (int i = 0; i < 11; i++) {
         this.repeats.add(2);
      }

      this.repeats.add(9);

      for (int i = 0; i < 4; i++) {
         this.repeats.add(4);
      }

      this.repeats.add(9);
      this.sounds = new ArrayList<>();
      this.sounds.add("throw_pokeball1");

      for (int i = 0; i < 11; i++) {
         this.sounds.add(null);
      }

      this.sounds.add("poof1");

      for (int i = 0; i < 11; i++) {
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
      if (!this.positions.isEmpty() && !this.sprites.isEmpty()) {
         this.sound = this.sounds.get(0);
         if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null);
         }

         this.sprite = this.sprites.get(0);
         if (this.sprite != null) {
            this.sprite.setPosition(this.position.x, this.position.y);
            this.sprite.draw(game.uiBatch);
         }

         if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.position = this.position.add(this.positions.get(0));
            this.positions.remove(0);
            this.sprites.remove(0);
            this.repeats.remove(0);
            this.sounds.remove(0);
         }
      } else {
         game.insertAction(this.nextAction);
         game.actionStack.remove(this);
      }
   }
}
