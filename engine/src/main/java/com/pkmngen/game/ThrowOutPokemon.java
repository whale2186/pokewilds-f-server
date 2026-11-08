package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class ThrowOutPokemon extends Action {
   Sprite pokeballSprite;
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite[][]> sprites;
   Sprite[][] sprite;
   ArrayList<Integer> repeats;
   ArrayList<String> sounds;
   String sound;
   public Action.Layer layer = Action.Layer.gui_104;
   Sprite helperSprite;
   boolean doneYet;
   boolean firstStep = true;

   public ThrowOutPokemon(Game game, Action nextAction) {
      super();
      this.doneYet = false;
      this.nextAction = nextAction;
      this.position = new Vector2(16.0F, 32.0F);
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(-6.0F, 18.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(12.0F, 12.0F));
      this.sprites = new ArrayList<>();
      this.sprites.add(null);
      Texture text = TextureCache.get(Gdx.files.internal("poof_sheet1.png"));
      this.sprites.add(new Sprite[][]{{new Sprite(text, 0, 0, 48, 48)}});
      this.sprites.add(new Sprite[][]{{new Sprite(text, 48, 0, 48, 48)}});
      this.sprites.add(new Sprite[][]{{new Sprite(text, 96, 0, 48, 48)}});
      this.sprites.add(new Sprite[][]{{new Sprite(text, 144, 0, 48, 48)}});
      this.sprites.add(new Sprite[][]{{new Sprite(text, 192, 0, 48, 48)}});
      this.sprites.add(null);
      Sprite temp = new Sprite(game.player.currPokemon.backSprite);
      TextureRegion[][] tempRegion = temp.split(4, 4);
      Sprite[][] temp2 = new Sprite[7][7];

      for (int i = 0; i < tempRegion.length; i++) {
         for (int j = 0; j < tempRegion[i].length; j++) {
            temp2[i][j] = new Sprite(tempRegion[6 - j][i]);
            temp2[i][j].setScale(2.0F);
         }
      }

      for (int i = 0; i < temp2.length; i++) {
         temp2[i][2] = temp2[i][3];
         temp2[i][3] = temp2[i][5];
         temp2[i][4] = temp2[i][6];
         temp2[i][5] = null;
         temp2[i][6] = null;
      }

      temp2[2] = temp2[1];
      temp2[1] = temp2[0];
      temp2[0] = new Sprite[0];
      temp2[4] = temp2[5];
      temp2[5] = temp2[6];
      temp2[6] = new Sprite[0];
      Sprite[][] temp3 = new Sprite[7][7];

      for (int i = 0; i < temp2.length; i++) {
         for (int j = 0; j < temp2[i].length; j++) {
            temp3[i][j] = temp2[i][j];
         }
      }

      for (int i = 0; i < temp3.length; i++) {
         if (temp3[i].length > 0) {
            temp3[i][1] = temp3[i][2];
            temp3[i][2] = temp3[i][4];
            temp3[i][3] = null;
            temp3[i][4] = null;
         }
      }

      temp3[2] = temp3[1];
      temp3[1] = new Sprite[0];
      temp3[4] = temp3[5];
      temp3[5] = new Sprite[0];
      Sprite temp4 = new Sprite(tempRegion[0][4]);
      temp4.setScale(2.0F);
      this.sprites.add(new Sprite[][]{new Sprite[0], new Sprite[0], new Sprite[0], {temp4}});
      this.sprites.add(temp3);
      this.sprites.add(temp2);
      this.sprites.add(new Sprite[][]{{game.player.currPokemon.backSprite}});
      this.repeats = new ArrayList<>();
      this.repeats.add(40);

      for (int i = 0; i < 4; i++) {
         this.repeats.add(5);
      }

      this.repeats.add(10);
      this.repeats.add(1);
      this.repeats.add(3);
      this.repeats.add(3);
      this.repeats.add(6);
      this.sounds = new ArrayList<>();
      this.sounds.add(null);
      this.sounds.add("poof1");

      for (int i = 0; i < 9; i++) {
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
      if (this.firstStep) {
         game.battle.drawAction.drawFriendlyPokemonAction = this;
         this.firstStep = false;
      }

      this.sprite = this.sprites.get(0);
      if (!this.positions.isEmpty() && !this.sprites.isEmpty()) {
         if (this.sprite != null) {
            for (int i = 0; i < this.sprite.length; i++) {
               for (int j = 0; j < this.sprite[i].length; j++) {
                  if (this.sprite[i][j] != null) {
                     this.sprite[i][j].setPosition(this.position.x + 8 * i, this.position.y + 8 * j);
                     this.sprite[i][j].draw(game.uiBatch);
                  }
               }
            }
         }

         this.sound = this.sounds.get(0);
         if (this.sound != null) {
            game.insertAction(new PlayMusic(this.sound, null));
            this.sounds.set(0, null);
         }

         if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.position = this.position.add(this.positions.get(0));
            this.positions.remove(0);
            this.sprites.remove(0);
            this.repeats.remove(0);
            this.sounds.remove(0);
         }
      } else {
         if (!this.doneYet) {
            game.insertAction(this.nextAction);
            game.player.currPokemon.backSprite.setPosition(this.position.x, this.position.y);

            for (int i = 0; i < this.sprite.length; i++) {
               for (int j = 0; j < this.sprite[i].length; j++) {
                  this.sprite[i][j].setPosition(this.position.x, this.position.y);
               }
            }

            this.doneYet = true;
            this.layer = Action.Layer.gui_114;
            game.actionStack.remove(this);
            game.insertAction(this);
         }

         for (int i = 0; i < this.sprite.length; i++) {
            for (int j = 0; j < this.sprite[i].length; j++) {
               this.sprite[i][j].draw(game.uiBatch);
            }
         }

         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      }
   }
}
