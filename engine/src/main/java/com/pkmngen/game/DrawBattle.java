package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;

class DrawBattle extends Action {
   public static boolean shouldDrawOppPokemon = true;
   public static boolean shouldDrawOwnPokemon = true;
   public static boolean hideOppPokemon = false;
   public static boolean hideOwnPokemon = false;
   boolean doneNightOverlay = false;
   SpriteProxy bgSprite;
   SpriteProxy bgSprite2;
   SpriteProxy oppPokemonOver;
   public DrawFriendlyHealth drawFriendlyHealthActionGen1;
   public DrawFriendlyHealthGen2 drawFriendlyHealthAction;
   public DrawEnemyHealth drawEnemyHealthActionGen1;
   public DrawEnemyHealthGen2 drawEnemyHealthAction;
   public static int prevFriendlyAttackIndex = -1;
   public static int prevEnemyAttackIndex = -1;
   public Action drawFriendlyPokemonAction;
   public Action.Layer layer = Action.Layer.gui_130;
   public DrawBattle.DrawNightOverlay drawNightOverlay = null;

   public DrawBattle(Game game) {
      super();
      Texture text = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
      this.bgSprite = new SpriteProxy(Color.WHITE, text, 0, 0, 176, 160);
      game.battle.drawAction = this;
      this.bgSprite.setPosition(-8.0F, -8.0F);
      text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.bgSprite2 = new SpriteProxy(text, 0, 0, 160, 144);
      this.oppPokemonOver = new SpriteProxy(text, 0, 0, 56, 40);
      this.oppPokemonOver.setX(96.0F);
      this.oppPokemonOver.setY(48.0F);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   public void cleanup(Game game) {
      game.player.dontDrawMapDuringBattle = false;
      game.actionStack.remove(this);
      game.actionStack.remove(this.drawNightOverlay);
      if (game.battle.drawAction instanceof RegigigasBattle.Draw) {
         ((RegigigasBattle.Draw)game.battle.drawAction).soundEffect.stop();
         ((RegigigasBattle.Draw)game.battle.drawAction).soundEffect.dispose();
      }

      game.battle.drawAction = null;
   }

   @Override
   public void firstStep(Game game) {
      prevFriendlyAttackIndex = -1;
      prevEnemyAttackIndex = -1;
      game.player.dontDrawMapDuringBattle = true;
   }

   @Override
   public void step(Game game) {
      if (!this.doneNightOverlay
         && game.map.timeOfDay.equals("night")
         && (game.battle.oppPokemon == null || !SpecialMewtwo1.class.isInstance(game.battle.oppPokemon))) {
         this.drawNightOverlay = new DrawBattle.DrawNightOverlay();
         game.insertAction(this.drawNightOverlay);
         this.doneNightOverlay = true;
      }

      this.bgSprite.draw(game.uiBatch);
      if (shouldDrawOppPokemon && !hideOppPokemon) {
         game.battle.oppPokemon.sprite.draw(game.uiBatch);
         this.oppPokemonOver.draw(game.uiBatch);
      }

      game.player.battleSprite.draw(game.uiBatch);

      for (int i = -1; i < 2; i++) {
         for (int j = -1; j < 2; j++) {
            if (i != 0 || j != 0) {
               game.uiBatch.draw(this.bgSprite2, 160 * i, 144 * j);
            }
         }
      }
   }

   class DrawNightOverlay extends Action {
      public Action.Layer layer = Action.Layer.gui_0;
      Sprite bgSprite;

      public DrawNightOverlay() {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame1.png"));
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
         if (!game.map.timeOfDay.equals("day") && !game.map.currRoute.isDungeon) {
            for (int i = -1; i < 2; i++) {
               for (int j = -1; j < 2; j++) {
                  game.uiBatch.draw(this.bgSprite, 160 * i, 144 * j);
               }
            }
         } else {
            game.actionStack.remove(this);
         }
      }
   }
}
