package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class ThrowHyperPokeball extends Action {
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite> sprites;
   Sprite sprite;
   ArrayList<Integer> repeats;
   String sound;
   ArrayList<String> sounds;
   public Action.Layer layer = Action.Layer.gui_120;
   Sprite helperSprite;

   public ThrowHyperPokeball(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.position = new Vector2(50.0F, 72.0F);
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(46.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      Texture text = TextureCache.get(Gdx.files.internal("hyperbeam_sheet2.png"));
      this.sprites = new ArrayList<>();
      this.sprites.add(null);

      for (int i = 0; i < 6; i++) {
         this.sprites.add(new Sprite(text, i * 72, 0, 72, 40));
      }

      this.sprites.add(null);
      text = TextureCache.get(Gdx.files.internal("poof_sheet1.png"));
      this.sprites.add(new Sprite(text, 0, 0, 48, 48));
      this.sprites.add(new Sprite(text, 48, 0, 48, 48));
      this.sprites.add(new Sprite(text, 96, 0, 48, 48));
      this.sprites.add(new Sprite(text, 144, 0, 48, 48));
      this.sprites.add(new Sprite(text, 192, 0, 48, 48));
      this.repeats = new ArrayList<>();
      this.repeats.add(13);

      for (int i = 0; i < 3; i++) {
         this.repeats.add(4);
         this.repeats.add(3);
      }

      this.repeats.add(2);

      for (int i = 0; i < 4; i++) {
         this.repeats.add(4);
      }

      this.repeats.add(9);
      this.sounds = new ArrayList<>();
      this.sounds.add("hyperbeam1");

      for (int i = 0; i < 7; i++) {
         this.sounds.add(null);
      }

      this.sounds.add("poof1");

      for (int i = 0; i < 4; i++) {
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
            game.insertAction(new PlayMusic(this.sound, null));
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
