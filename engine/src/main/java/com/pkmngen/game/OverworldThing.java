package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

public class OverworldThing {
   public String dirFacing = "down";
   public Vector2 position = new Vector2();

   public Vector2 facingPos(Vector2 pos) {
      return this.facingPos(pos, this.dirFacing);
   }

   public Vector2 facingPos(Vector2 pos, String dirFacing) {
      if (dirFacing.equals("right")) {
         pos.set(this.position).add(16.0F, 0.0F);
      } else if (dirFacing.equals("left")) {
         pos.set(this.position).add(-16.0F, 0.0F);
      } else if (dirFacing.equals("up")) {
         pos.set(this.position).add(0.0F, 16.0F);
      } else if (dirFacing.equals("down")) {
         pos.set(this.position).add(0.0F, -16.0F);
      }

      return pos;
   }

   public class SurfWakeAnim extends Action {
      public Action.Layer layer = Action.Layer.map_129;
      Sprite sprite;
      int timer = 0;
      boolean flipX = false;
      boolean flipY = false;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public SurfWakeAnim(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("wake1.png")));
         this.sprite.setPosition(OverworldThing.this.position.x, OverworldThing.this.position.y);
         if (OverworldThing.this.dirFacing.equals("right")) {
            this.flipX = true;
         } else if (OverworldThing.this.dirFacing.equals("up")) {
            this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("wake2.png")));
            this.flipY = true;
            this.sprite.setPosition(OverworldThing.this.position.x, OverworldThing.this.position.y);
         } else if (OverworldThing.this.dirFacing.equals("down")) {
            this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("wake2.png")));
            this.sprite.setPosition(OverworldThing.this.position.x, OverworldThing.this.position.y);
         }

         this.sprite.setRegion(0, 0, 16, 16);
      }

      @Override
      public void step(Game game) {
         if (this.timer == 12) {
            this.sprite.setRegion(16, 0, 16, 16);
         }

         if (this.timer == 24) {
            this.sprite.setRegion(32, 0, 16, 16);
         }

         float U = this.sprite.getU();
         float U2 = this.sprite.getU2();
         if (this.flipX) {
            U = this.sprite.getU2();
            U2 = this.sprite.getU();
         }

         float V = this.sprite.getV();
         float V2 = this.sprite.getV2();
         if (this.flipY) {
            V = this.sprite.getV2();
            V2 = this.sprite.getV();
         }

         game.mapBatch
            .draw(this.sprite.getTexture(), this.sprite.getX(), this.sprite.getY(), this.sprite.getRegionWidth(), this.sprite.getRegionHeight(), U, V, U2, V2);
         this.timer++;
         if (this.timer > 36) {
            game.actionStack.remove(this);
         }
      }
   }

   public class WaterSplash extends Action {
      public Action.Layer layer = Action.Layer.map_114;
      Vector2 offset;
      boolean flip = false;
      Sprite sprite;
      int timer = 0;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public WaterSplash(boolean flip, Action nextAction) {
         this(flip, new Vector2(0.0F, 0.0F), nextAction);
      }

      public WaterSplash(boolean flip, Vector2 offset, Action nextAction) {
         super();
         this.offset = offset;
         this.nextAction = nextAction;
         this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("splash_anim4.png")));
         this.sprite.setRegion(0, 0, 16, 16);
         this.flip = flip;
      }

      @Override
      public void step(Game game) {
         if (this.timer == 4) {
            this.sprite.setRegion(16, 0, 16, 16);
         } else if (this.timer == 5) {
            this.sprite.setRegion(32, 0, 16, 16);
         }

         game.mapBatch
            .draw(
               this.sprite.getTexture(),
               OverworldThing.this.position.x + this.offset.x,
               OverworldThing.this.position.y + 3.0F + this.offset.y + 4.0F,
               16.0F,
               16.0F,
               this.sprite.getRegionX(),
               this.sprite.getRegionY(),
               this.sprite.getRegionWidth(),
               this.sprite.getRegionHeight(),
               this.flip,
               false
            );
         this.timer++;
         if (this.timer >= 10) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
