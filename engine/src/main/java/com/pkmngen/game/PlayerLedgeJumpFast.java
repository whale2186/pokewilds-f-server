package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.Map;

class PlayerLedgeJumpFast extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   float xDist;
   float yDist;
   Vector2 initialPos;
   Vector2 targetPos;
   int timer1 = 0;
   ArrayList<Integer> yMovesList = new ArrayList<>();
   ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<>();
   Player player;
   PlayerLedgeJumpFast.DrawShadow drawShadow;

   public PlayerLedgeJumpFast(Game game) {
      this(game, game.player);
   }

   public PlayerLedgeJumpFast(Game game, Player player) {
      this(game, player, new PlayerStanding(game, player, false, false));
   }

   public PlayerLedgeJumpFast(Game game, Player player, Action nextAction) {
      super();
      this.player = player;
      this.nextAction = nextAction;
      this.initialPos = new Vector2(this.player.position);
      if (this.player.dirFacing.equals("up")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y + 16.0F);
      } else if (this.player.dirFacing.equals("down")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y - 16.0F);
      } else if (this.player.dirFacing.equals("left")) {
         this.targetPos = new Vector2(this.player.position.x - 16.0F, this.player.position.y);
      } else if (this.player.dirFacing.equals("right")) {
         this.targetPos = new Vector2(this.player.position.x + 16.0F, this.player.position.y);
      }

      game.insertAction(new PlayerLedgeJumpFast.DrawShadow());
      game.insertAction(new PlayMusic("ledge2", null));
      this.yMovesList.add(4);
      this.yMovesList.add(2);
      this.yMovesList.add(1);
      this.yMovesList.add(0);
      this.yMovesList.add(-1);
      this.yMovesList.add(-1);
      this.yMovesList.add(-2);
      this.yMovesList.add(-3);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.movingSprites);
      this.spriteAnim.add(this.player.movingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.altMovingSprites);
      this.spriteAnim.add(this.player.altMovingSprites);
      this.spriteAnim.add(this.player.standingSprites);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void firstStep(Game game) {
      if (this.player.hmPokemon != null) {
         this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
         this.player.hmPokemon.numMoves.add(1.0F);
      }
   }

   public void localStep(Game game) {
      if (this.timer1 < 16) {
         if (game.player.dirFacing.equals("up")) {
            game.player.position.y++;
            game.cam.position.y++;
         } else if (game.player.dirFacing.equals("down")) {
            game.cam.position.y--;
            game.player.position.y--;
         } else if (game.player.dirFacing.equals("left")) {
            game.player.position.x--;
            game.cam.position.x--;
         } else if (game.player.dirFacing.equals("right")) {
            game.player.position.x++;
            game.cam.position.x++;
         }

         if (this.timer1 % 2 == 1) {
            game.player.position.y = game.player.position.y + this.yMovesList.get(0).intValue();
            this.yMovesList.remove(0);
            game.player.currSprite = this.spriteAnim.get(0).get(game.player.dirFacing);
            this.spriteAnim.remove(0);
         }
      } else {
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      }

      if (this.timer1 >= 19) {
         game.player.position.set(this.targetPos);
         game.cam.position.set(this.targetPos.x + 16.0F, this.targetPos.y, 0.0F);
         game.player.isNearCampfire = game.player.checkNearCampfire();
         game.actionStack.remove(this);
         game.actionStack.remove(this.drawShadow);
         game.insertAction(this.nextAction);
         if (this.player.hmPokemon != null) {
            this.player.hmPokemon.ledgeJumps.add(2);
         }
      }

      this.timer1++;
   }

   public void remoteStep(Game game) {
      this.player.network.syncTimer++;
      if (this.timer1 < 16) {
         if (this.player.dirFacing.equals("up")) {
            this.player.position.y++;
         } else if (this.player.dirFacing.equals("down")) {
            this.player.position.y--;
         } else if (this.player.dirFacing.equals("left")) {
            this.player.position.x--;
         } else if (this.player.dirFacing.equals("right")) {
            this.player.position.x++;
         }

         if (this.timer1 % 2 == 1) {
            this.player.position.y = this.player.position.y + this.yMovesList.get(0).intValue();
            this.yMovesList.remove(0);
            this.player.currSprite = this.spriteAnim.get(0).get(this.player.dirFacing);
            this.spriteAnim.remove(0);
         }
      } else {
         this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
      }

      if (this.timer1 >= 19) {
         this.player.position.set(this.targetPos);
         this.player.standingAction.alternate = true;
         this.player.standingAction.isRunning = false;
         game.insertAction(this.player.standingAction);
         game.actionStack.remove(this);
      }

      this.timer1++;
   }

   @Override
   public void step(Game game) {
      if (this.player.type == Player.Type.LOCAL) {
         this.localStep(game);
      } else if (this.player.type == Player.Type.REMOTE) {
         this.remoteStep(game);
      }
   }

   class DrawShadow extends Action {
      public Action.Layer layer = Action.Layer.map_131;
      Texture shadow = TextureCache.get(Gdx.files.internal("shadow1.png"));

      public DrawShadow() {
         super();
         PlayerLedgeJumpFast.this.drawShadow = this;
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         game.mapBatch.draw(this.shadow, game.cam.position.x - 16.0F, game.cam.position.y - 4.0F);
      }
   }
}
