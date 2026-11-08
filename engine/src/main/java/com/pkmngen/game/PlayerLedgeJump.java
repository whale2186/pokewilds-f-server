package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.Map;

class PlayerLedgeJump extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   float xDist;
   float yDist;
   Vector2 initialPos;
   Vector2 targetPos;
   int timer1 = 0;
   ArrayList<Integer> yMovesList = new ArrayList<>();
   PlayerLedgeJump.DrawShadow drawShadow;
   ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<>();
   Player player;

   public PlayerLedgeJump(Game game) {
      this(game, game.player);
   }

   public PlayerLedgeJump(Game game, Player player) {
      super();
      this.player = player;
      this.initialPos = new Vector2(this.player.position);
      if (this.player.dirFacing.equals("up")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y + 32.0F);
      } else if (this.player.dirFacing.equals("down")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y - 32.0F);
      } else if (this.player.dirFacing.equals("left")) {
         this.targetPos = new Vector2(this.player.position.x - 32.0F, this.player.position.y);
      } else if (this.player.dirFacing.equals("right")) {
         this.targetPos = new Vector2(this.player.position.x + 32.0F, this.player.position.y);
      }

      game.insertAction(new PlayerLedgeJump.DrawShadow());
      game.insertAction(new PlayMusic("ledge1", null));
      this.yMovesList.add(4);
      this.yMovesList.add(2);
      this.yMovesList.add(2);
      this.yMovesList.add(2);
      this.yMovesList.add(1);
      this.yMovesList.add(1);
      this.yMovesList.add(0);
      this.yMovesList.add(0);
      this.yMovesList.add(-1);
      this.yMovesList.add(-1);
      this.yMovesList.add(-1);
      this.yMovesList.add(-1);
      this.yMovesList.add(-2);
      this.yMovesList.add(-3);
      this.yMovesList.add(-3);
      this.yMovesList.add(0);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.movingSprites);
      this.spriteAnim.add(this.player.movingSprites);
      this.spriteAnim.add(this.player.movingSprites);
      this.spriteAnim.add(this.player.movingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.standingSprites);
      this.spriteAnim.add(this.player.altMovingSprites);
      this.spriteAnim.add(this.player.altMovingSprites);
      this.spriteAnim.add(this.player.altMovingSprites);
      this.spriteAnim.add(this.player.altMovingSprites);
      this.spriteAnim.add(this.player.standingSprites);
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
      if (this.timer1 < 32) {
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

      if (this.timer1 >= 38) {
         game.player.position.set(this.targetPos);
         game.cam.position.set(this.targetPos.x + 16.0F, this.targetPos.y, 0.0F);
         game.player.isNearCampfire = game.player.checkNearCampfire();
         game.actionStack.remove(this);
         game.actionStack.remove(this.drawShadow);
         Action playerStanding = new PlayerStanding(game);
         game.insertAction(playerStanding);
         if (this.player.hmPokemon != null) {
            this.player.hmPokemon.ledgeJumps.add(1);
         }
      }

      this.timer1++;
   }

   public void remoteStep(Game game) {
      this.player.network.syncTimer++;
      if (this.timer1 < 32) {
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

      if (this.timer1 >= 38) {
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
         PlayerLedgeJump.this.drawShadow = this;
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
