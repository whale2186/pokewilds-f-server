package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.Locale;

class DrawStatsScreen extends Menu {
   public Action.Layer layer = Action.Layer.gui_106;
   Sprite[] bgSprites = new Sprite[3];
   Sprite helperSprite;
   public int currIndex = 0;
   Pokemon pokemon;
   int pokeIndex;
   Sprite healthSprite;
   PokemonIntroAnim intro;
   char[] levelChars;
   Sprite healthSpriteGreen;
   Sprite healthSpriteYellow;
   Sprite healthSpriteRed;
   int targetSize;

   public DrawStatsScreen(Game game, Pokemon pokemon, Menu prevMenu) {
      this.prevMenu = prevMenu;

      for (int i = 0; i < 3; i++) {
         Texture text = TextureCache.get(Gdx.files.internal("menu/stats_screen" + String.valueOf(i + 1) + ".png"));
         this.bgSprites[i] = new Sprite(text, 0, 0, 160, 144);
      }

      this.pokemon = pokemon;
      this.pokeIndex = DrawPokemonMenu.allPokemon.indexOf(pokemon);
      Texture text = TextureCache.get(Gdx.files.internal("battle/health1.png"));
      this.healthSprite = new Sprite(text, 0, 0, 1, 2);
      this.intro = new PokemonIntroAnim(null);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health1.png"));
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

   @Override
   public void firstStep(Game game) {
      game.battle.oppPokemon = this.pokemon;
      game.insertAction(new WaitFrames(game, 4, new PlayMusic(this.pokemon, null)));
      game.insertAction(this.intro);
      Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/health1.png"));
      this.healthSpriteGreen = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health_yellow.png"));
      this.healthSpriteYellow = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      text = TextureCache.get(Gdx.files.internal("battle/gsc/health_red.png"));
      this.healthSpriteRed = new SpriteProxy(new Color(0.9375F, 0.8125F, 0.46875F, 1.0F), text, 0, 0, 1, 2);
      this.targetSize = (int)Math.ceil(this.pokemon.currentStats.get("hp").intValue() * 48.0F / this.pokemon.maxStats.get("hp").intValue());
      if (this.targetSize > 23) {
         this.healthSprite = this.healthSpriteGreen;
      } else if (this.targetSize > 10) {
         this.healthSprite = this.healthSpriteYellow;
      } else {
         this.healthSprite = this.healthSpriteRed;
      }
   }

   @Override
   public void step(Game game) {
      game.uiBatch.draw(this.bgSprites[this.currIndex], 0.0F, 0.0F);
      Sprite sprite = new Sprite(this.pokemon.sprite);
      sprite.flip(true, false);
      game.uiBatch.draw(sprite, (56.0F - this.pokemon.sprite.getWidth()) / 2.0F, 88.0F);
      this.levelChars = String.valueOf(this.pokemon.level).toCharArray();
      int offset = -8 * (this.pokemon.level / 100);

      for (char c : this.levelChars) {
         Sprite currSprite = game.textDict.get(c);
         game.uiBatch.draw(currSprite, 120 + offset, 136.0F);
         offset += 8;
      }

      if (this.pokemon.gender.equals("male")) {
         game.uiBatch.draw(TextureCache.maleSymbol, 144.0F, 136.0F);
      } else if (this.pokemon.gender.equals("female")) {
         game.uiBatch.draw(TextureCache.femaleSymbol, 144.0F, 136.0F);
      }

      if (this.pokemon.isShiny) {
         game.uiBatch.draw(TextureCache.shinySymbol, 152.0F, 136.0F);
      }

      char[] textArray = this.pokemon.nickname.toUpperCase(Locale.ROOT).toCharArray();

      for (int j = 0; j < textArray.length; j++) {
         Sprite letterSprite = game.transparentDict.get(textArray[j]);
         game.uiBatch.draw(letterSprite, 64 + 8 * j, 120.0F);
      }

      if (this.currIndex == 0) {
         int currHealth = this.pokemon.currentStats.get("hp");
         int hundredsPlace = currHealth / 100;
         if (hundredsPlace > 0) {
            Sprite hundredsPlaceSprite = game.transparentDict.get(Character.forDigit(hundredsPlace, 10));
            game.uiBatch.draw(hundredsPlaceSprite, 8.0F, 56.0F);
         }

         int tensPlace = currHealth % 100 / 10;
         if (tensPlace > 0 || hundredsPlace > 0) {
            Sprite tensPlaceSprite = game.transparentDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 16.0F, 56.0F);
         }

         int onesPlace = currHealth % 10;
         Sprite onesPlaceSprite = game.transparentDict.get(Character.forDigit(onesPlace, 10));
         game.uiBatch.draw(onesPlaceSprite, 24.0F, 56.0F);
         int maxHealth = this.pokemon.maxStats.get("hp");
         hundredsPlace = maxHealth / 100;
         if (hundredsPlace > 0) {
            Sprite hundredsPlaceSprite = game.transparentDict.get(Character.forDigit(hundredsPlace, 10));
            game.uiBatch.draw(hundredsPlaceSprite, 40.0F, 56.0F);
         }

         tensPlace = maxHealth % 100 / 10;
         if (tensPlace > 0 || hundredsPlace > 0) {
            Sprite tensPlaceSprite = game.transparentDict.get(Character.forDigit(tensPlace, 10));
            game.uiBatch.draw(tensPlaceSprite, 48.0F, 56.0F);
         }

         onesPlace = maxHealth % 10;
         onesPlaceSprite = game.transparentDict.get(Character.forDigit(onesPlace, 10));
         game.uiBatch.draw(onesPlaceSprite, 56.0F, 56.0F);
         String text = "OK";
         if (this.pokemon.status != null) {
            if (this.pokemon.status.equals("poison") || this.pokemon.status.equals("toxic")) {
               text = "PSN";
            } else if (this.pokemon.status.equals("paralyze")) {
               text = "PAR";
            } else if (this.pokemon.status.equals("freeze")) {
               text = "FRZ";
            } else if (this.pokemon.status.equals("sleep")) {
               text = "SLP";
            } else if (this.pokemon.status.equals("burn")) {
               text = "BRN";
            }
         }

         textArray = text.toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 48 + 8 * j, 32.0F);
         }

         textArray = this.pokemon.types.get(0).toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 8 + 8 * j, 16.0F);
         }

         textArray = this.pokemon.types.get(1).toCharArray();

         for (int j = 0; j < textArray.length && !this.pokemon.types.get(0).equals(this.pokemon.types.get(1)); j++) {
            Sprite letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 8 + 8 * j, 8.0F);
         }

         String exp = String.valueOf(this.pokemon.exp);
         text = "";

         for (int i = 0; i < 10 - exp.length(); i++) {
            text = text + " ";
         }

         textArray = (text + exp).toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 80 + 8 * j, 56.0F);
         }

         for (int j = 0; j < this.targetSize; j++) {
            game.uiBatch.draw(this.healthSprite, 16 + 1 * j, 67.0F);
         }

         exp = String.valueOf(this.pokemon.calcExpForLevel(this.pokemon.level + 1) - this.pokemon.exp);
         text = "";

         for (int i = 0; i < 10 - exp.length(); i++) {
            text = text + " ";
         }

         textArray = (text + exp).toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 80 + 8 * j, 32.0F);
         }

         textArray = String.valueOf(this.pokemon.level + 1).toCharArray();

         for (int j = 0; j < textArray.length; j++) {
            Sprite letterSprite = game.transparentDict.get(textArray[j]);
            game.uiBatch.draw(letterSprite, 144 + 8 * j, 24.0F);
         }
      } else if (this.currIndex == 1) {
         for (int i = 0; i < this.pokemon.attacks.length; i++) {
            String attack = this.pokemon.attacks[i];
            if (attack == null) {
               attack = "-";
            }

            textArray = attack.toUpperCase(Locale.ROOT).toCharArray();

            for (int j = 0; j < textArray.length; j++) {
               Sprite letterSprite = game.transparentDict.get(textArray[j]);
               game.uiBatch.draw(letterSprite, 64 + 8 * j, 56 - i * 16);
            }
         }
      } else {
         if (this.pokemon.previousOwner != null) {
            textArray = this.pokemon.previousOwner.name.toUpperCase(Locale.ROOT).toCharArray();

            for (int j = 0; j < textArray.length; j++) {
               Sprite letterSprite = game.transparentDict.get(textArray[j]);
               game.uiBatch.draw(letterSprite, 16 + 8 * j, 32.0F);
            }
         }

         String[] allStats = new String[]{"attack", "defense", "specialAtk", "specialDef", "speed"};

         for (int i = 0; i < allStats.length; i++) {
            String val = String.valueOf(this.pokemon.maxStats.get(allStats[i]));
            String text = "";

            for (int k = 0; k < 3 - val.length(); k++) {
               text = text + " ";
            }

            textArray = (text + val).toCharArray();

            for (int j = 0; j < textArray.length; j++) {
               Sprite letterSprite = game.transparentDict.get(textArray[j]);
               game.uiBatch.draw(letterSprite, 136 + 8 * j, 64 - 16 * i);
            }
         }
      }

      if (InputProcessor.upJustPressed && this.prevMenu != null) {
         int newIndex = this.pokeIndex - 1;
         newIndex = newIndex < 0 ? DrawPokemonMenu.allPokemon.size() - 1 : newIndex;
         if (newIndex != this.pokeIndex) {
            this.scrollToNewPokemon(game, newIndex);
         }
      } else if (InputProcessor.downJustPressed && this.prevMenu != null) {
         int newIndex = this.pokeIndex + 1;
         newIndex = newIndex >= DrawPokemonMenu.allPokemon.size() ? 0 : newIndex;
         if (newIndex != this.pokeIndex) {
            this.scrollToNewPokemon(game, newIndex);
         }
      } else if (InputProcessor.leftJustPressed) {
         if (this.currIndex > 0) {
            this.currIndex--;
         }
      } else if (InputProcessor.rightJustPressed && this.currIndex < 2) {
         this.currIndex++;
      }

      if (InputProcessor.bJustPressed && this.prevMenu != null) {
         game.actionStack.remove(this);
         game.insertAction(new DrawStatsScreen.Outro(this.prevMenu));
      }
   }

   private void scrollToNewPokemon(Game game, int newIndex) {
      this.intro.currFrame = this.pokemon.introAnim.size();
      DrawPokemonMenu.currIndex = 0;
      DrawPokemonMenu.scrollIndex = 0;

      while (DrawPokemonMenu.currIndex + DrawPokemonMenu.scrollIndex < newIndex) {
         if (DrawPokemonMenu.currIndex < 5) {
            DrawPokemonMenu.currIndex++;
         } else {
            DrawPokemonMenu.scrollIndex++;
         }
      }

      game.actionStack.remove(this);
      DrawStatsScreen newScreen = new DrawStatsScreen(game, DrawPokemonMenu.allPokemon.get(newIndex), this.prevMenu);
      newScreen.currIndex = this.currIndex;
      game.insertAction(new DrawStatsScreen.Intro(newScreen));
   }

   static class Intro extends Action {
      public Action.Layer layer = Action.Layer.gui_110;
      int duration = 30;
      Sprite bgSprite;

      public Intro(Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
         this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      }

      public Intro(int duration, Action nextAction) {
         this(nextAction);
         this.duration = duration;
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
         this.bgSprite.draw(game.uiBatch);
         this.duration--;
         if (this.duration <= 0) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   static class Outro extends Action {
      Menu prevMenu;
      public Action.Layer layer = Action.Layer.gui_110;
      int duration = 34;
      Sprite bgSprite;

      public Outro(Menu prevMenu) {
         super();
         this.prevMenu = prevMenu;
         if (this.prevMenu != null) {
            this.prevMenu.drawArrowWhite = true;
         }

         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
         this.bgSprite = new Sprite(text, 0, 0, 160, 144);
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
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         this.duration--;
         if (this.duration > 0) {
            this.bgSprite.draw(game.uiBatch);
         }

         if (this.duration <= 0) {
            if (this.prevMenu != null) {
               game.insertAction(this.prevMenu);
               this.prevMenu.disabled = false;
               this.prevMenu.drawArrowWhite = false;
            }

            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }
}
