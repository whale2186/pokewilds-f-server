package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class ThrowOutPokemonCrystal extends Action {
   Sprite pokeballSprite;
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite[][][]> sprites;
   Sprite[][][] sprite;
   ArrayList<Integer> repeats;
   ArrayList<String> sounds;
   String sound;
   public Action.Layer layer = Action.Layer.gui_104;
   Sprite helperSprite;
   boolean doneYet = false;

   public ThrowOutPokemonCrystal(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
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
   public void firstStep(Game game) {
      this.position = new Vector2(16.0F, 48.0F);
      Sprite temp = new Sprite(game.player.currPokemon.backSprite);
      TextureRegion[][] tempRegion = temp.split(8, 8);
      Sprite[][] temp2 = new Sprite[6][6];

      for (int i = 0; i < tempRegion.length; i++) {
         for (int j = 0; j < tempRegion[i].length; j++) {
            temp2[i][j] = new Sprite(tempRegion[5 - j][i]);
         }
      }

      for (int i = 0; i < temp2.length; i++) {
         temp2[i][1] = temp2[i][2];
         temp2[i][2] = temp2[i][3];
         temp2[i][3] = temp2[i][5];
         temp2[i][4] = null;
         temp2[i][5] = null;
      }

      temp2[1] = temp2[0];
      temp2[0] = new Sprite[0];
      temp2[4] = temp2[5];
      temp2[5] = new Sprite[0];
      Sprite[][] temp3 = new Sprite[6][6];

      for (int i = 0; i < temp2.length; i++) {
         for (int j = 0; j < temp2[i].length; j++) {
            temp3[i][j] = temp2[i][j];
         }
      }

      for (int i = 0; i < temp3.length; i++) {
         if (temp3[i].length > 0) {
            temp3[i][1] = temp3[i][3];
            temp3[i][2] = null;
            temp3[i][3] = null;
            temp3[i][4] = null;
            temp3[i][5] = null;
         }
      }

      temp3[2] = temp3[1];
      temp3[1] = new Sprite[0];
      temp3[3] = temp3[4];
      temp3[4] = new Sprite[0];
      temp3[5] = new Sprite[0];
      Texture text = TextureCache.get(Gdx.files.internal("poof_sheet2.png"));
      this.sprites = new ArrayList<>();
      this.sprites.add(null);
      this.sprites.add(new Sprite[][][]{null, {{new Sprite(text, 0, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{null, {{new Sprite(text, 48, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{temp3, {{new Sprite(text, 48, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{temp3, {{new Sprite(text, 96, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{temp2, {{new Sprite(text, 96, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{temp2, {{new Sprite(text, 144, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{{{game.player.currPokemon.backSprite}}, {{new Sprite(text, 144, 0, 48, 48)}}});
      this.sprites.add(new Sprite[][][]{{{game.player.currPokemon.backSprite}}});
      this.sprites.add(new Sprite[][][]{{{game.player.currPokemon.backSprite}}});
      this.repeats = new ArrayList<>();
      this.repeats.add(34);
      this.repeats.add(4);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(2);
      this.repeats.add(26);
      this.sounds = new ArrayList<>();
      this.sounds.add(null);
      this.sounds.add("poof1");

      for (int i = 0; i < 9; i++) {
         this.sounds.add(null);
      }

      game.battle.drawAction.drawFriendlyPokemonAction = this;
      game.player.currPokemon.resetStatStages();
      game.player.currPokemon.disabledIndex = -1;
      game.player.currPokemon.disabledCounter = 0;
      game.player.currPokemon.trapCounter = 0;
      game.player.currPokemon.trappedBy = null;
      game.player.currPokemon.cantEscapeBy = null;
      game.player.currPokemon.participatedInBattle = true;
      DrawBattle.prevFriendlyAttackIndex = -1;
      game.player.currPokemon.volatileStatus.clear();
      game.player.currPokemon.volatileStatusCounter.clear();
      DrawAttacksMenu.curr = 0;
   }

   @Override
   public void step(Game game) {
      this.sprite = this.sprites.get(0);
      if (this.sprites.size() <= 1) {
         if (!this.doneYet) {
            game.insertAction(this.nextAction);
            game.player.currPokemon.backSprite.setPosition(this.position.x, this.position.y);

            for (int k = 0; k < this.sprite.length; k++) {
               if (this.sprite[k] != null) {
                  for (int i = 0; i < this.sprite[k].length; i++) {
                     for (int j = 0; j < this.sprite[k][i].length; j++) {
                        this.sprite[k][i][j].setPosition(this.position.x, this.position.y);
                     }
                  }
               }
            }

            this.doneYet = true;
            this.layer = Action.Layer.gui_114;
            game.actionStack.remove(this);
            game.insertAction(this);
         }

         if (DrawBattle.shouldDrawOwnPokemon && !DrawBattle.hideOwnPokemon) {
            for (int k = 0; k < this.sprite.length; k++) {
               for (int i = 0; i < this.sprite[k].length; i++) {
                  for (int j = 0; j < this.sprite[k][i].length; j++) {
                     this.sprite[k][i][j].draw(game.uiBatch);
                  }
               }
            }
         }

         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      } else {
         if (this.sprite != null) {
            for (int k = 0; k < this.sprite.length; k++) {
               if (this.sprite[k] != null) {
                  for (int i = 0; i < this.sprite[k].length; i++) {
                     for (int j = 0; j < this.sprite[k][i].length; j++) {
                        if (this.sprite[k][i][j] != null) {
                           this.sprite[k][i][j].setPosition(this.position.x + 8 * i - 4 * k, this.position.y + 8 * j - 8 * k);
                           this.sprite[k][i][j].draw(game.uiBatch);
                        }
                     }
                  }
               }
            }
         }

         this.sound = this.sounds.get(0);
         if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null);
         }

         if (this.repeats.get(0) > 1) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.sprites.remove(0);
            this.repeats.remove(0);
            this.sounds.remove(0);
         }
      }
   }
}
