package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.Locale;
import java.util.Map;

class DrawFriendlyHealthGen2 extends Action {
   public Action.Layer layer = Action.Layer.gui_129;
   SpriteProxy bgSprite;
   SpriteProxy healthSprite;
   SpriteProxy healthSpriteGreen;
   SpriteProxy healthSpriteYellow;
   SpriteProxy healthSpriteRed;
   char[] levelChars;
   SpriteProxy levelCharacter;
   Map<Character, SpriteProxy> textDict;
   int currHealth;
   int currHealthRemaining;
   Sprite helper;
   public static int xpBarIndex = 0;
   public static SpriteProxy[] xpBarAnim = new SpriteProxy[77];

   public DrawFriendlyHealthGen2(Game game) {
      this(game, null);
   }

   public DrawFriendlyHealthGen2(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/health1.png"));
      this.healthSpriteGreen = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health_yellow.png"));
      this.healthSpriteYellow = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health_red.png"));
      this.healthSpriteRed = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/helper1.png"));
      this.helper = new Sprite(text);
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
      this.currHealth = (int)Math.ceil(
         game.player.currPokemon.currentStats.get("hp").intValue() * 48.0F / game.player.currPokemon.maxStats.get("hp").intValue()
      );
      this.currHealthRemaining = game.player.currPokemon.currentStats.get("hp");
      game.battle.drawAction.drawFriendlyHealthAction = this;
      if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
         this.textDict = game.textDictInverse;
         Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/friendly_healthbar1_inverse.png"));
         this.bgSprite = new SpriteProxy(Color.BLACK, text, 0, 0, 160, 144);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/friendly_levelchar_inverse.png"));
         this.levelCharacter = new SpriteProxy(Color.BLACK, text, 0, 0, 160, 144);
      } else {
         this.textDict = game.textDict;
         Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/friendly_healthbar1.png"));
         this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);
         text = TextureCache.get(Gdx.files.internal("battle/gsc/friendly_levelchar.png"));
         this.levelCharacter = new SpriteProxy(Color.WHITE, text, 0, 0, 160, 144);
      }

      int currentLevelXp = game.player.currPokemon.calcExpForLevel(game.player.currPokemon.level);
      int nextLevelXp = game.player.currPokemon.calcExpForLevel(game.player.currPokemon.level + 1);
      xpBarIndex = (int)(68.0F * ((float)(game.player.currPokemon.exp - currentLevelXp) / (nextLevelXp - currentLevelXp)));
      this.updateHealthBarColor();
   }

   @Override
   public void step(Game game) {
      if (this.nextAction != null) {
         game.insertAction(this.nextAction);
         this.nextAction = null;
      }

      if (DrawFriendlyHealth.shouldDraw) {
         game.uiBatch.draw(this.bgSprite, 0.0F, 0.0F);
         game.uiBatch.draw(xpBarAnim[xpBarIndex], 0.0F, 0.0F);
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
               SpriteProxy letterSprite = this.textDict.get(c);
               game.uiBatch.draw(letterSprite, 112 + var15, 72.0F);
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
         hundredsPlace = this.currHealthRemaining / 100;
         if (hundredsPlace > 0) {
            SpriteProxy hudredsPlaceSprite = this.textDict.get(Character.forDigit(hundredsPlace, 10));
            game.uiBatch.draw(hudredsPlaceSprite, 88.0F, 56.0F);
         }

         tensPlace = this.currHealthRemaining % 100 / 10;
         if (tensPlace > 0 || hundredsPlace > 0) {
            SpriteProxy tensPlaceSprite = this.textDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 96.0F, 56.0F);
         }

         onesPlace = this.currHealthRemaining % 10;
         onesPlaceSprite = this.textDict.get(Character.forDigit(onesPlace, 10));
         game.uiBatch.draw(onesPlaceSprite, 104.0F, 56.0F);
      }

      if (game.battle.drawAction == null) {
         game.actionStack.remove(this);
      }
   }

   static {
      for (int i = 0; i < 77; i++) {
         String fileName = "attacks/xp_animation_gsc/output/frame-" + String.format(Locale.ROOT, "%03d", i + 1) + ".png";
         Texture texture = TextureCache.get(Gdx.files.internal(fileName));
         xpBarAnim[i] = new SpriteProxy(texture, 0, 0, 160, 144);
      }
   }
}
