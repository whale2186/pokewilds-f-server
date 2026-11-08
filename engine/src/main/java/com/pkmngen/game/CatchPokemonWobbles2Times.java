package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class CatchPokemonWobbles2Times extends Action {
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite> sprites;
   Sprite sprite;
   ArrayList<Integer> repeats;
   String sound;
   ArrayList<String> sounds;
   ArrayList<Float> alphas;
   public Action.Layer layer = Action.Layer.gui_120;
   Sprite helperSprite;

   public CatchPokemonWobbles2Times(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("pokeball1_color.png"));
      this.position = new Vector2(114.0F, 88.0F);
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(-1.0F, 0.0F));
      this.positions.add(new Vector2(1.0F, 0.0F));
      this.positions.add(new Vector2(1.0F, 0.0F));

      for (int i = 0; i < 1; i++) {
         this.positions.add(new Vector2(-1.0F, 0.0F));
         this.positions.add(new Vector2(-1.0F, 0.0F));
         this.positions.add(new Vector2(1.0F, 0.0F));
         this.positions.add(new Vector2(1.0F, 0.0F));
      }

      this.positions.add(new Vector2(-19.0F, -16.0F));

      for (int i = 0; i < 6; i++) {
         this.positions.add(new Vector2(0.0F, 0.0F));
      }

      this.sprites = new ArrayList<>();
      this.sprites.add(null);
      text = TextureCache.get(Gdx.files.internal("pokeball_wiggleSheet1_color.png"));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 12, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 24, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 12, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 24, 0, 12, 12));
      text = TextureCache.get(Gdx.files.internal("poof_sheet1.png"));
      this.sprites.add(new Sprite(text, 0, 0, 48, 48));
      this.sprites.add(new Sprite(text, 48, 0, 48, 48));
      this.sprites.add(new Sprite(text, 96, 0, 48, 48));
      this.sprites.add(new Sprite(text, 144, 0, 48, 48));
      this.sprites.add(new Sprite(text, 192, 0, 48, 48));
      this.sprites.add(null);
      this.repeats = new ArrayList<>();
      this.repeats.add(12);
      this.repeats.add(43);

      for (int i = 0; i < 3; i++) {
         this.repeats.add(3);
      }

      this.repeats.add(43);

      for (int i = 0; i < 2; i++) {
         this.repeats.add(3);
      }

      this.repeats.add(13);

      for (int i = 0; i < 4; i++) {
         this.repeats.add(4);
      }

      this.repeats.add(9);
      this.repeats.add(3);
      this.sounds = new ArrayList<>();
      this.sounds.add("pokeball_wiggle1");
      this.sounds.add(null);
      this.sounds.add("pokeball_wiggle1");

      for (int i = 0; i < 6; i++) {
         this.sounds.add(null);
      }

      this.sounds.add("poof1");

      for (int i = 0; i < 5; i++) {
         this.sounds.add(null);
      }

      this.alphas = new ArrayList<>();

      for (int i = 0; i < 14; i++) {
         this.alphas.add(0.0F);
      }

      this.alphas.add(1.0F);
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
         float currAlpha = this.alphas.get(0);
         if (currAlpha == 0.0F) {
            DrawBattle.shouldDrawOppPokemon = false;
         } else {
            DrawBattle.shouldDrawOppPokemon = true;
         }

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
            this.alphas.remove(0);
         }
      } else {
         String textString = "Aww! It appeared to be caught!";
         game.insertAction(new DisplayText(game, textString, null, null, this.nextAction));
         game.actionStack.remove(this);
      }
   }
}
