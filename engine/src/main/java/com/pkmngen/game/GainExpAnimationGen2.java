package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.Locale;

class GainExpAnimationGen2 extends Action {
   Pokemon pokemon;
   int targetIndex = 0;
   int timer = 0;
   Sound xpGainSound;
   Sound levelUpSound;
   boolean leveledUp = false;

   public GainExpAnimationGen2(Pokemon pokemon, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.nextAction = nextAction;
      this.xpGainSound = AudioLoader.loadSound("sounds/xp-gain1.ogg");
      this.levelUpSound = AudioLoader.loadSound("sounds/xp-levelup1.ogg");
   }

   @Override
   public void firstStep(Game game) {
      this.timer = 0;
      int currentLevelXp = this.pokemon.calcExpForLevel(this.pokemon.level);
      int nextLevelXp = this.pokemon.calcExpForLevel(this.pokemon.level + 1);
      this.targetIndex = (int)(67.0F * ((float)(this.pokemon.exp - currentLevelXp) / (nextLevelXp - currentLevelXp)));
      this.xpGainSound.play();
   }

   @Override
   public void step(Game game) {
      if (this.timer < 9) {
         this.timer++;
      } else {
         if (DrawFriendlyHealthGen2.xpBarIndex == 67) {
            this.xpGainSound.stop();
            this.levelUpSound.play();
            this.pokemon.gainLevel(1);
            game.battle.drawAction.drawFriendlyHealthAction.layer = Action.Layer.gui_104;
            game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction);
            game.insertAction(game.battle.drawAction.drawFriendlyHealthAction);
         }

         if (DrawFriendlyHealthGen2.xpBarIndex >= 67) {
            if (DrawFriendlyHealthGen2.xpBarIndex < 76) {
               DrawFriendlyHealthGen2.xpBarIndex++;
            } else {
               this.leveledUp = true;
               game.actionStack.remove(this);
               Action action = new WaitFrames(
                  game,
                  13,
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           this.pokemon.nickname.toUpperCase(Locale.ROOT) + " grew to level " + this.pokemon.level + "!",
                           "fanfare1.ogg",
                           true,
                           true,
                           new CheckMovesLearned(this.pokemon, new RunCode(() -> {
                              DrawFriendlyHealthGen2.xpBarIndex = 0;
                              this.firstStep(game);
                           }, this))
                        )
                     )
                  )
               );
               game.battle.drawAction.drawFriendlyHealthAction.layer = Action.Layer.gui_129;
               game.actionStack.remove(game.battle.drawAction.drawFriendlyHealthAction);
               game.insertAction(game.battle.drawAction.drawFriendlyHealthAction);
               game.insertAction(action);
               this.pokemon.gainedLevel = true;
            }
         } else {
            if (DrawFriendlyHealthGen2.xpBarIndex >= this.targetIndex) {
               this.xpGainSound.stop();
               this.xpGainSound.dispose();
               this.levelUpSound.stop();
               this.levelUpSound.dispose();
               game.actionStack.remove(this);
               Action action = new Action();
               if (this.leveledUp) {
                  action.append(new DisplayText.Clear(game, new WaitFrames(game, 2, new GainExpAnimationGen2.ShowLevelUpStats(null))));
               }

               action.append(this.nextAction);
               game.insertAction(action);
            }

            DrawFriendlyHealthGen2.xpBarIndex++;
         }
      }
   }

   static class ShowLevelUpStats extends Action {
      public Action.Layer layer = Action.Layer.gui_103;
      public int timer = 0;
      Sprite bgSprite;
      String[] allStats = new String[]{"attack", "defense", "specialAtk", "specialDef", "speed"};

      public ShowLevelUpStats(Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/gsc/levelup_stats_screen1.png"));
         this.bgSprite = new Sprite(text);
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
         game.uiBatch.draw(this.bgSprite, 0.0F, 0.0F);
         int i = 0;

         for (String stat : this.allStats) {
            String text = stat.toUpperCase(Locale.ROOT);
            if (text.equals("SPECIALATK")) {
               text = "SPCL.ATK";
            } else if (text.equals("SPECIALDEF")) {
               text = "SPCL.DEF";
            }

            char[] textArray = text.toCharArray();

            for (int j = 0; j < textArray.length; j++) {
               Sprite letterSprite = game.transparentDict.get(textArray[j]);
               game.uiBatch.draw(letterSprite, 88 + 8 * j, 128 - 8 * i);
            }

            i++;
            textArray = String.valueOf(game.player.currPokemon.maxStats.get(stat)).toCharArray();
            int offset = 8 * (3 - textArray.length);

            for (char c : textArray) {
               Sprite currSprite = game.transparentDict.get(c);
               game.uiBatch.draw(currSprite, 120 + offset, 128 - 8 * i);
               offset += 8;
            }

            i++;
         }

         if (this.timer < 82) {
            this.timer++;
         } else if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
