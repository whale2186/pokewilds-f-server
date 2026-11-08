package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.Locale;
import java.util.Map;

class DrawFriendlyHealth extends Action {
   public static boolean shouldDraw = true;
   SpriteProxy bgSprite;
   SpriteProxy healthSprite;
   char[] levelChars;
   SpriteProxy levelCharacter;
   int currHealth;
   public Action.Layer layer = Action.Layer.gui_129;
   boolean firstStep = true;
   Map<Character, SpriteProxy> textDict;

   public DrawFriendlyHealth(Game game) {
      this(game, null);
   }

   public DrawFriendlyHealth(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/health1.png"));
      this.healthSprite = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
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
      this.currHealth = (int)Math.ceil(
         game.player.currPokemon.currentStats.get("hp").intValue() * 48.0F / game.player.currPokemon.maxStats.get("hp").intValue()
      );
      game.battle.drawAction.drawFriendlyHealthActionGen1 = this;
      if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
         this.textDict = game.textDictInverse;
         Texture text = TextureCache.get(Gdx.files.internal("battle/friendly_healthbar1_inverse.png"));
         this.bgSprite = new SpriteProxy(Color.BLACK, text, 0, 0, 160, 144);
         text = TextureCache.get(Gdx.files.internal("battle/friendly_levelchar_inverse.png"));
         this.levelCharacter = new SpriteProxy(Color.BLACK, text, 0, 0, 160, 144);
      } else {
         this.textDict = game.textDict;
         Texture text = TextureCache.get(Gdx.files.internal("battle/friendly_healthbar1.png"));
         this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);
         text = TextureCache.get(Gdx.files.internal("battle/friendly_levelchar.png"));
         this.levelCharacter = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);
      }
   }

   @Override
   public void step(Game game) {
      if (this.nextAction != null) {
         game.insertAction(this.nextAction);
         this.nextAction = null;
      }

      if (shouldDraw) {
         this.bgSprite.draw(game.uiBatch);
         String name = game.player.currPokemon.nickname.toUpperCase(Locale.ROOT);
         char[] textArray = name.toCharArray();
         int offset = 0;
         if (textArray.length > 5) {
            offset = -16;
         } else if (textArray.length > 2) {
            offset = -8;
         }

         for (int i = 0; i < textArray.length; i++) {
            SpriteProxy letterSprite = this.textDict.get(textArray[i]);
            game.uiBatch.draw(letterSprite, offset + 96 + 8 * i, 80.0F);
         }

         if (game.player.currPokemon.status != null
            && !game.player.currPokemon.status.equals("confuse")
            && !game.player.currPokemon.status.equals("attract")
            && !game.player.currPokemon.status.equals("curse")) {
            String text = "";
            if (game.player.currPokemon.status.equals("poison") || game.player.currPokemon.status.equals("toxic")) {
               text = "PSN";
            } else if (game.player.currPokemon.status.equals("paralyze")) {
               text = "PAR";
            } else if (game.player.currPokemon.status.equals("freeze")) {
               text = "FRZ";
            } else if (game.player.currPokemon.status.equals("sleep")) {
               text = "SLP";
            } else if (game.player.currPokemon.status.equals("burn")) {
               text = "BRN";
            }

            textArray = text.toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               SpriteProxy letterSprite = this.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 112 + i * 8, 72.0F);
            }
         } else {
            this.levelChars = String.valueOf(game.player.currPokemon.level).toCharArray();
            int var15 = 0;
            if (this.levelChars.length < 3) {
               game.uiBatch.draw(this.levelCharacter, 0.0F, 0.0F);
               var15 += 8;
            }

            for (char c : this.levelChars) {
               Sprite currSprite = this.textDict.get(c);
               game.uiBatch.draw(currSprite, 112 + var15, 72.0F);
               var15 += 8;
            }
         }

         for (int i = 0; i < this.currHealth; i++) {
            game.uiBatch.draw(this.healthSprite, 96 + i, 67.0F);
         }

         if (game.player.currPokemon.gender.equals("male")) {
            if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
               game.uiBatch.draw(TextureCache.maleSymbolInverse, 136.0F, 72.0F);
            } else {
               game.uiBatch.draw(TextureCache.maleSymbol, 136.0F, 72.0F);
            }
         } else if (game.player.currPokemon.gender.equals("female")) {
            if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
               game.uiBatch.draw(TextureCache.femaleSymbolInverse, 136.0F, 72.0F);
            } else {
               game.uiBatch.draw(TextureCache.femaleSymbol, 136.0F, 72.0F);
            }
         }

         int maxHealth = game.player.currPokemon.maxStats.get("hp");
         int hundredsPlace = maxHealth / 100;
         if (hundredsPlace > 0) {
            SpriteProxy hudredsPlaceSprite = this.textDict.get(Character.forDigit(hundredsPlace, 10));
            game.uiBatch.draw(hudredsPlaceSprite, 120.0F, 56.0F);
         }

         int tensPlace = maxHealth % 100 / 10;
         if (tensPlace > 0 || hundredsPlace > 0) {
            SpriteProxy tensPlaceSprite = this.textDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 128.0F, 56.0F);
         }

         int onesPlace = maxHealth % 10;
         SpriteProxy onesPlaceSprite = this.textDict.get(Character.forDigit(onesPlace, 10));
         game.uiBatch.draw(onesPlaceSprite, 136.0F, 56.0F);
         int currHealthRemaining = this.currHealth * maxHealth / 48;
         hundredsPlace = currHealthRemaining / 100;
         if (hundredsPlace > 0) {
            SpriteProxy hudredsPlaceSprite = this.textDict.get(Character.forDigit(hundredsPlace, 10));
            game.uiBatch.draw(hudredsPlaceSprite, 88.0F, 56.0F);
         }

         tensPlace = currHealthRemaining % 100 / 10;
         if (tensPlace > 0 || hundredsPlace > 0) {
            SpriteProxy tensPlaceSprite = this.textDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 96.0F, 56.0F);
         }

         onesPlace = currHealthRemaining % 10;
         onesPlaceSprite = this.textDict.get(Character.forDigit(onesPlace, 10));
         game.uiBatch.draw(onesPlaceSprite, 104.0F, 56.0F);
      }

      if (game.battle.drawAction == null) {
         game.actionStack.remove(this);
      }
   }
}
