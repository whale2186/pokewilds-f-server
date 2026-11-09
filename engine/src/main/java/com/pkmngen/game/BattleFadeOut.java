package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class BattleFadeOut extends Action {
   ArrayList<Sprite> frames;
   Sprite frame;
   int speed = 1;
   public static boolean whiteScreen = false;
   public Action.Layer layer = Action.Layer.gui_129;

   public BattleFadeOut(Game game, Action nextAction) {
      this(game, 1, nextAction);
   }

   public BattleFadeOut(Game game, int speed, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.speed = speed;
      this.frames = new ArrayList<>();
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      Sprite sprite1 = new Sprite(text1);

      for (int i = 0; i < 14 * this.speed; i++) {
         this.frames.add(sprite1);
      }

      text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame5.png"));
      sprite1 = new Sprite(text1);

      for (int i = 0; i < 8 * this.speed; i++) {
         this.frames.add(sprite1);
      }

      text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame4.png"));
      sprite1 = new Sprite(text1);

      for (int i = 0; i < 8 * this.speed; i++) {
         this.frames.add(sprite1);
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
   public void firstStep(Game game) {
      if (game.battle.drawAction != null) {
         game.battle.drawAction.cleanup(game);
      }
   }

   @Override
   public void step(Game game) {
      if (!this.frames.isEmpty()) {
         this.frame = this.frames.get(0);
         if (this.frame != null) {
            this.frame.setScale(3.0F);
            this.frame.setPosition(160.0F, 144.0F);
            this.frame.draw(game.uiBatch);
         }

         if (!whiteScreen) {
            this.frames.remove(0);
         }
      } else {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
         DrawBattle.shouldDrawOwnPokemon = true;
         DrawBattle.shouldDrawOppPokemon = true;
         if (DisplayText.unownText) {
            ArrayList<TrainerTipsTile> signTiles = new ArrayList<>();

            for (Tile tile : game.map.overworldTiles.values()) {
               if (tile.nameUpper.equals("sign1")) {
                  signTiles.add((TrainerTipsTile)tile);
               }
            }

            if (signTiles.size() > 0) {
               TrainerTipsTile tile = signTiles.get(game.map.rand.nextInt(signTiles.size()));
               tile.isUnown = true;
            }
         }

         DisplayText.unownText = false;
         game.musicController.unownMusic = false;
         game.player.currPokemon.trappedBy = null;
         game.player.currPokemon.trapCounter = 0;
         game.player.currPokemon.disabledIndex = -1;
         game.player.currPokemon.disabledCounter = 0;
         game.player.currPokemon.cantEscapeBy = null;
         if (game.battle.oppPokemon != null) {
            game.battle.oppPokemon.inBattle = false;
            game.battle.oppPokemon.aggroPlayer = false;
            if (game.battle.oppPokemon.standingAction instanceof Pokemon.Standing) {
               Pokemon.Standing standing = (Pokemon.Standing)game.battle.oppPokemon.standingAction;
               standing.aggroTimer = -300;
               standing.justAggroed = true;
            }
            if (game.coop != null) game.coop.endWorldBattle(game.battle.oppPokemon);
            game.battle.oppPokemon.canMove = true;
            game.battle.oppPokemon.cantEscapeBy = null;
         }
         for (Action action : new ArrayList<>(game.actionStack)) {
            if (action instanceof DrawGhost) {
               DrawGhost ghost = (DrawGhost)action;
               game.actionStack.remove(ghost);
               game.insertAction(new DespawnGhost(ghost.basePos.cpy()));
               game.musicController.nightAlert = false;
               game.musicController.resumeOverworldMusic = true;
            }
         }
      }
   }

   public static class WhiteScreen extends Action {
      boolean whiteScreen;

      public WhiteScreen(boolean whiteScreen, Action nextAction) {
         super();
         this.whiteScreen = whiteScreen;
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         BattleFadeOut.whiteScreen = this.whiteScreen;
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }
}
