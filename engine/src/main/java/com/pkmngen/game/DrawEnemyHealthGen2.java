package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.Locale;
import java.util.Map;

class DrawEnemyHealthGen2 extends Action {
   public Action.Layer layer = Action.Layer.gui_129;
   public static boolean shouldDraw = true;
   SpriteProxy bgSprite;
   SpriteProxy healthSprite;
   SpriteProxy healthSpriteGreen;
   SpriteProxy healthSpriteYellow;
   SpriteProxy healthSpriteRed;
   int currHealth;
   public Vector2 translateAmt = new Vector2();
   char[] levelChars;
   SpriteProxy levelCharacter;
   Map<Character, SpriteProxy> textDict;

   public DrawEnemyHealthGen2(Game game) {
      super();
      game.battle.drawAction.drawEnemyHealthAction = this;
      this.currHealth = (int)Math.ceil(game.battle.oppPokemon.currentStats.get("hp").intValue() * 48.0F / game.battle.oppPokemon.maxStats.get("hp").intValue());
      Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/health1.png"));
      this.healthSpriteGreen = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health_yellow.png"));
      this.healthSpriteYellow = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health_red.png"));
      this.healthSpriteRed = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   public void updateHealthBarColor() {
      if (this.currHealth > 23) {
         this.healthSprite = this.healthSpriteGreen;
      } else if (this.currHealth > 10) {
         this.healthSprite = this.healthSpriteYellow;
      } else {
         this.healthSprite = this.healthSpriteRed;
      }
   }

   @Override
   public void firstStep(Game game) {
      if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
         this.textDict = game.textDictInverse;
         Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/enemy_healthbar1_inverse.png"));
         this.bgSprite = new SpriteProxy(Color.BLACK, text, 0, 0, 160, 144);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/enemy_levelchar_inverse.png"));
         this.levelCharacter = new SpriteProxy(Color.BLACK, text, 0, 0, 160, 144);
      } else {
         this.textDict = game.textDict;
         Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/enemy_healthbar1.png"));
         this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/enemy_levelchar.png"));
         this.levelCharacter = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);
      }

      this.updateHealthBarColor();
   }

   @Override
   public void step(Game game) {
      if (DrawEnemyHealth.shouldDraw) {
         game.uiBatch.draw(this.bgSprite, this.translateAmt.x, this.translateAmt.y);
         String name = game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT);
         char[] textArray = name.toCharArray();

         for (int i = 0; i < textArray.length; i++) {
            SpriteProxy letterSprite = this.textDict.get(textArray[i]);
            game.uiBatch.draw(letterSprite, 8 + 8 * i + this.translateAmt.x, 136.0F);
         }

         if (game.battle.oppPokemon.status != null
            && !game.battle.oppPokemon.status.equals("confuse")
            && !game.battle.oppPokemon.status.equals("attract")
            && !game.battle.oppPokemon.status.equals("curse")) {
            String text = "";
            if (game.battle.oppPokemon.status.equals("poison") || game.battle.oppPokemon.status.equals("toxic")) {
               text = "PSN";
            } else if (game.battle.oppPokemon.status.equals("paralyze")) {
               text = "PAR";
            } else if (game.battle.oppPokemon.status.equals("freeze")) {
               text = "FRZ";
            } else if (game.battle.oppPokemon.status.equals("sleep")) {
               text = "SLP";
            } else if (game.battle.oppPokemon.status.equals("burn")) {
               text = "BRN";
            }

            textArray = text.toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               SpriteProxy letterSprite = this.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 48 + 8 * i + this.translateAmt.x, 128.0F);
            }
         } else {
            this.levelChars = String.valueOf(game.battle.oppPokemon.level).toCharArray();
            int offset = 0;
            if (this.levelChars.length < 3) {
               game.uiBatch.draw(this.levelCharacter, 0.0F + this.translateAmt.x, 0.0F);
               offset += 8;
            }

            for (char c : this.levelChars) {
               SpriteProxy letterSprite = this.textDict.get(c);
               game.uiBatch.draw(letterSprite, 48 + offset + this.translateAmt.x, 128.0F);
               offset += 8;
            }
         }

         for (int i = 0; i < this.currHealth; i++) {
            game.uiBatch.draw(this.healthSprite, 32 + i + this.translateAmt.x, 123.0F);
         }

         if (!game.battle.oppPokemon.isGhost) {
            if (game.battle.oppPokemon.gender.equals("male")) {
               if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
                  game.uiBatch.draw(TextureCache.maleSymbolInverse, 72.0F + this.translateAmt.x, 128.0F);
               } else {
                  game.uiBatch.draw(TextureCache.maleSymbol, 72.0F + this.translateAmt.x, 128.0F);
               }
            } else if (game.battle.oppPokemon.gender.equals("female")) {
               if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
                  game.uiBatch.draw(TextureCache.femaleSymbolInverse, 72.0F + this.translateAmt.x, 128.0F);
               } else {
                  game.uiBatch.draw(TextureCache.femaleSymbol, 72.0F + this.translateAmt.x, 128.0F);
               }
            }
         }
      }

      if (game.battle.drawAction == null) {
         game.actionStack.remove(this);
      }
   }
}
