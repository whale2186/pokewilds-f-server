package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

class DespawnGhost extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   Sprite[] sprites;
   int part1 = 80;
   int part2;
   int part3;
   Vector2 position;
   Sprite sprite;

   public DespawnGhost(Vector2 position) {
      super();
      this.position = position;
      Texture ghostTexture1 = TextureCache.get(Gdx.files.internal("ghost_spawn1.png"));
      this.sprite = new Sprite(ghostTexture1, 0, 0, 40, 40);
      this.sprite.setPosition(position.x - 4.0F, position.y - 4.0F);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (this.part1 > 0) {
         if (this.part1 % 4 >= 2) {
            this.sprite.draw(game.mapBatch);
         }

         this.part1--;
      } else {
         game.actionStack.remove(this);
      }
   }
}
