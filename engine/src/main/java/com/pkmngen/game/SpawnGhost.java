package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

class SpawnGhost extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   Sprite[] sprites;
   int part1;
   int part2;
   int part3;
   Vector2 position;
   boolean graveyardSpawn = false;

   public SpawnGhost(Game game, Vector2 position) {
      this(game, position, false);
   }

   public SpawnGhost(Game game, Vector2 position, boolean graveyardSpawn) {
      super();
      this.part1 = 80;
      this.part2 = 40;
      this.part3 = 50;
      this.position = position;
      this.graveyardSpawn = graveyardSpawn;
      Texture ghostTexture1 = TextureCache.get(Gdx.files.internal("ghost_spawn1.png"));
      Texture ghostTexture2 = TextureCache.get(Gdx.files.internal("ghost_sheet1.png"));
      this.sprites = new Sprite[]{new Sprite(ghostTexture1, 0, 0, 40, 40), new Sprite(ghostTexture2, 0, 0, 32, 32), new Sprite(ghostTexture2, 32, 0, 32, 32)};
      this.sprites[0].setPosition(position.x - 4.0F, position.y - 4.0F);
      this.sprites[1].setPosition(position.x, position.y);
      this.sprites[2].setPosition(position.x, position.y);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (this.part1 == 80) {
         game.playerCanMove = false;
         float dx = this.position.x - game.player.position.x;
         float dy = this.position.y - game.player.position.y;
         if (dx < dy) {
            if (game.player.position.y < this.position.y) {
               game.player.currSprite = game.player.standingSprites.get("up");
            } else {
               game.player.currSprite = game.player.standingSprites.get("left");
            }
         } else if (game.player.position.x < this.position.x) {
            game.player.currSprite = game.player.standingSprites.get("right");
         } else {
            game.player.currSprite = game.player.standingSprites.get("down");
         }

         game.musicController.startNightAlert = "night1_alert1";
      }

      if (this.part1 > 0) {
         if (this.part1 % 4 >= 2) {
            this.sprites[0].draw(game.mapBatch);
         }

         this.part1--;
      } else {
         if (this.part2 == 40) {
            game.insertAction(new PlayMusic("ghost1", null));
         }

         if (this.part2 == 32) {
            game.insertAction(new PlayMusic("ghost1", null));
         }

         if (this.part2 > 0) {
            if (this.part2 % 8 >= 4) {
               this.sprites[1].draw(game.mapBatch);
            } else {
               this.sprites[2].draw(game.mapBatch);
            }

            this.part2--;
         } else if (this.part3 > 0) {
            this.sprites[2].draw(game.mapBatch);
            this.part3--;
         } else {
            game.musicController.startNightAlert = "night1_chase1";
            game.playerCanMove = true;
            game.actionStack.remove(this);
            game.insertAction(new DrawGhost(game, this.position, this.graveyardSpawn));
         }
      }
   }
}
