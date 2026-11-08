package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.Map;

class PlayerKnockedBack extends Action {
   public Action.Layer layer = Action.Layer.map_131;
   float xDist;
   float yDist;
   Vector2 initialPos;
   Vector2 targetPos;
   Sprite shadow;
   int timer1 = 0;
   ArrayList<Integer> yMovesList = new ArrayList<>();
   ArrayList<Map<String, Sprite>> spriteAnim = new ArrayList<>();
   Player player;
   String dir;

   public PlayerKnockedBack(Game game, Player player, String dir) {
      super();
      this.player = player;
      this.dir = dir;
      this.initialPos = new Vector2(this.player.position);
      if (dir.equals("up")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y + 32.0F);
      } else if (dir.equals("down")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y - 32.0F);
      } else if (dir.equals("left")) {
         this.targetPos = new Vector2(this.player.position.x - 32.0F, this.player.position.y);
      } else if (dir.equals("right")) {
         this.targetPos = new Vector2(this.player.position.x + 32.0F, this.player.position.y);
      }

      Texture shadowText = TextureCache.get(Gdx.files.internal("shadow1.png"));
      this.shadow = new Sprite(shadowText, 0, 0, 16, 16);
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

   @Override
   public void step(Game game) {
      if (this.timer1 < 32) {
         if (this.dir.equals("up")) {
            game.player.position.y++;
            game.cam.position.y++;
         } else if (this.dir.equals("down")) {
            game.cam.position.y--;
            game.player.position.y--;
         } else if (this.dir.equals("left")) {
            game.player.position.x--;
            game.cam.position.x--;
         } else if (this.dir.equals("right")) {
            game.player.position.x++;
            game.cam.position.x++;
         }

         if (this.timer1 % 2 == 1) {
            game.player.position.y = game.player.position.y + this.yMovesList.get(0).intValue();
            this.yMovesList.remove(0);
            game.player.currSprite = this.spriteAnim.get(0).get(game.player.dirFacing);
            this.spriteAnim.remove(0);
         }

         game.cam.update();
         game.mapBatch.setProjectionMatrix(game.cam.combined);
      } else {
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      }

      game.mapBatch.draw(this.shadow, game.cam.position.x - 16.0F, game.cam.position.y - 4.0F);
      if (this.timer1 >= 38) {
         game.player.position.set(this.targetPos);
         game.cam.position.set(this.targetPos.x + 16.0F, this.targetPos.y, 0.0F);
         game.player.isNearCampfire = game.player.checkNearCampfire();
         game.actionStack.remove(this);
         if (this.player.hmPokemon != null) {
            this.player.hmPokemon.ledgeJumps.add(1);
         }
      }

      this.timer1++;
   }
}
