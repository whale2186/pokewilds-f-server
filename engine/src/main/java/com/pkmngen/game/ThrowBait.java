package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class ThrowBait extends Action {
   Sprite baitSprite;
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite> sprites;
   Sprite sprite;
   ArrayList<Integer> frames;
   String sound;
   ArrayList<String> sounds;
   public Action.Layer layer = Action.Layer.gui_120;
   Sprite helperSprite;

   public ThrowBait(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("bait_small1.png"));
      this.baitSprite = new Sprite(text, 0, 0, 8, 8);
      this.positions = new ArrayList<>();
      this.position = new Vector2(32.0F, 64.0F);
      this.positions.add(new Vector2(8.0F, 12.0F));
      this.positions.add(new Vector2(8.0F, 12.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 8.0F));
      this.positions.add(new Vector2(8.0F, 2.0F));
      this.positions.add(new Vector2(8.0F, -2.0F));
      this.positions.add(new Vector2(8.0F, -9.0F));
      this.positions.add(new Vector2(8.0F, -9.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.sprites = new ArrayList<>();

      for (int i = 0; i < 11; i++) {
         this.sprites.add(this.baitSprite);
      }

      this.sprites.add(null);
      this.frames = new ArrayList<>();

      for (int i = 0; i < 11; i++) {
         this.frames.add(3);
      }

      this.frames.add(72);
      this.sounds = new ArrayList<>();
      this.sounds.add("throw_rock1");

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
            game.insertAction(new PlayMusic(this.sound, null));
            this.sounds.set(0, null);
         }

         this.sprite = this.sprites.get(0);
         if (this.sprite != null) {
            this.sprite.setPosition(this.position.x, this.position.y);
            this.sprite.draw(game.uiBatch);
         }

         if (this.frames.get(0) > 0) {
            this.frames.set(0, this.frames.get(0) - 1);
         } else {
            this.position = this.position.add(this.positions.get(0));
            this.positions.remove(0);
            this.sprites.remove(0);
            this.frames.remove(0);
            this.sounds.remove(0);
         }
      } else {
         int currCatchRate = game.battle.oppPokemon.currentStats.get("catchRate");
         game.battle.oppPokemon.currentStats.put("catchRate", currCatchRate / 2);
         int randomNum = game.map.rand.nextInt(5) + 1;
         game.battle.oppPokemon.eating += randomNum;
         if (game.battle.oppPokemon.eating > 255) {
            game.battle.oppPokemon.eating = 255;
         }

         game.battle.oppPokemon.angry = 0;
         System.out.println("eating counter: " + String.valueOf(game.battle.oppPokemon.eating));
         System.out.println("Catch Rate: " + String.valueOf(game.battle.oppPokemon.currentStats.get("catchRate")));
         game.insertAction(new WaitFrames(game, 2, this.nextAction));
         game.actionStack.remove(this);
      }
   }
}
