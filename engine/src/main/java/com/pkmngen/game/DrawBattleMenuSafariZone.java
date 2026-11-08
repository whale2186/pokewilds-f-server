package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;
import java.util.Map;

class DrawBattleMenuSafariZone extends Action {
   Sprite arrow;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_129;
   Map<String, Vector2> getCoords = new HashMap<>();
   String curr;
   Vector2 newPos;

   public DrawBattleMenuSafariZone(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/battle_text_safarizone.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      this.getCoords.put("tr", new Vector2(105.0F, 24.0F));
      this.getCoords.put("tl", new Vector2(9.0F, 24.0F));
      this.getCoords.put("br", new Vector2(105.0F, 8.0F));
      this.getCoords.put("bl", new Vector2(9.0F, 8.0F));
      this.newPos = new Vector2(9.0F, 24.0F);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.curr = "tl";
   }

   @Deprecated
   Action calcIfCaught(Game game) {
      int maxRand = 150;
      int randomNum = game.map.rand.nextInt(maxRand + 1);
      int statusValue = 0;
      boolean breaksFree = false;
      int ball = 15;
      int adrenaline = game.player.adrenaline;
      if (adrenaline > 25) {
         adrenaline = 25;
      }

      int modFactor = 100;
      int f = (int)Math.floor(game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4 / (modFactor * ball));
      if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate")) {
      }

      int randomNum_M = game.map.rand.nextInt(256);
      if (f + adrenaline * 10 >= randomNum_M) {
         breaksFree = false;
      } else {
         breaksFree = true;
      }

      System.out.println("(randomNum_M / f / adr): (" + String.valueOf(randomNum_M) + " / " + f + " / +" + adrenaline * 10 + ")");
      if (!breaksFree) {
         return new CatchPokemonWobblesThenCatch(game, "", this);
      } else {
         randomNum_M = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
         if (randomNum_M >= 256) {
            return new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this)));
         } else {
            int s = 0;
            int x = randomNum_M * f / 255 + s;
            if (x < 10) {
               return new CatchPokemonMiss(game, new PrintAngryEating(game, new ChanceToRun(game, this)));
            } else if (x < 30) {
               return new CatchPokemonWobbles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, this)));
            } else {
               return x < 70
                  ? new CatchPokemonWobbles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, this)))
                  : new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, this)));
            }
         }
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
      if (InputProcessor.upPressed) {
         if (this.curr.equals("bl") || this.curr.equals("br")) {
            this.curr = "t" + String.valueOf(this.curr.charAt(1));
            this.newPos = this.getCoords.get(this.curr);
         }
      } else if (InputProcessor.downPressed) {
         if (this.curr.equals("tl") || this.curr.equals("tr")) {
            this.curr = "b" + String.valueOf(this.curr.charAt(1));
            this.newPos = this.getCoords.get(this.curr);
         }
      } else if (InputProcessor.leftPressed) {
         if (this.curr.equals("tr") || this.curr.equals("br")) {
            this.curr = this.curr.charAt(0) + "l";
            this.newPos = this.getCoords.get(this.curr);
         }
      } else if (InputProcessor.rightPressed && (this.curr.equals("tl") || this.curr.equals("bl"))) {
         this.curr = this.curr.charAt(0) + "r";
         this.newPos = this.getCoords.get(this.curr);
      }

      if (InputProcessor.aJustPressed) {
         if (this.curr.equals("tl")) {
            Action catchAction = this.calcIfCaught(game);
            String textString = game.player.name + " used SAFARI BALL!";
            if (game.player.adrenaline < 5) {
               game.insertAction(new DisplayText(game, textString, null, catchAction, new ThrowPokeball(game, catchAction)));
            } else if (game.player.adrenaline < 15) {
               game.insertAction(new DisplayText(game, textString, null, catchAction, new ThrowFastPokeball(game, catchAction)));
            } else {
               game.insertAction(new DisplayText(game, textString, null, catchAction, new ThrowHyperPokeball(game, catchAction)));
            }

            game.actionStack.remove(this);
         } else if (this.curr.equals("bl")) {
            Action throwRockAction = new ThrowRock(game, new PrintAngryEating(game, new ChanceToRun(game, this)));
            String textString = game.player.name + " threw a ROCK.";
            game.insertAction(new DisplayText(game, textString, null, throwRockAction, throwRockAction));
            game.actionStack.remove(this);
         } else if (this.curr.equals("tr")) {
            Action throwBaitAction = new ThrowBait(game, new PrintAngryEating(game, new ChanceToRun(game, this)));
            String textString = game.player.name + " threw some BAIT.";
            game.insertAction(new DisplayText(game, textString, null, throwBaitAction, throwBaitAction));
            game.actionStack.remove(this);
         } else if (this.curr.equals("br")) {
            game.actionStack.remove(this);
            game.insertAction(
               new WaitFrames(
                  game,
                  18,
                  new DisplayText(game, "Got away safely!", null, null, new SplitAction(new BattleFadeOut(game, null), new BattleFadeOutMusic(game, null)))
               )
            );
            game.insertAction(new PlayMusic("click1", new PlayMusic("run1", null)));
         }
      }

      this.textBox.draw(game.uiBatch);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.arrow.draw(game.uiBatch);
   }
}
