package com.pkmngen.game;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.pkmngen.game.action.DrawAction;
import com.pkmngen.game.util.GameProfiler;

class DrawMapTrees extends DrawAction {
   public Action.Layer layer = Action.Layer.map_115;
   Vector3 startPos;
   Vector3 endPos;
   Tile tile;
   GridPoint2 topLeft = new GridPoint2();

   public DrawMapTrees(GameProfiler profiler) {
      super(profiler);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   protected void draw(Game game) {
      if (!game.player.dontDrawMapDuringBattle) {
         int i;
         for (i = 0; i < game.map.onscreenYsort1.size(); i++) {
            PkmnMap.YSort ySort = game.map.onscreenYsort1.get(i);
            if (ySort.yPos <= game.player.position.y) {
               break;
            }

            for (int j = 0; j < ySort.ySortPokemon.size(); j++) {
               Pokemon pokemon = ySort.ySortPokemon.get(j);
               if (pokemon.drawUpper != null) {
                  pokemon.drawUpper.draw(game);
               }
            }

            for (int j = 0; j < ySort.ySortSavannaTrees.size(); j++) {
               Tile tile = ySort.ySortSavannaTrees.get(j);
               game.mapBatch
                  .draw(
                     tile.overSprite.getTexture(),
                     tile.overSprite.getX(),
                     tile.overSprite.getY() + 16.0F,
                     tile.overSprite.getRegionX(),
                     tile.overSprite.getRegionY(),
                     tile.overSprite.getRegionWidth(),
                     tile.overSprite.getRegionHeight() - 16
                  );
            }

            for (int j = 0; j < ySort.ySortTrees.size(); j++) {
               Tile tile = ySort.ySortTrees.get(j);
               game.mapBatch
                  .draw(
                     tile.overSprite.getTexture(),
                     tile.overSprite.getX(),
                     tile.overSprite.getY() + 8.0F,
                     tile.overSprite.getRegionX(),
                     tile.overSprite.getRegionY(),
                     tile.overSprite.getRegionWidth(),
                     tile.overSprite.getRegionHeight() - 8
                  );
            }
         }

         if (game.player.hmPokemon != null && game.player.hmPokemon.drawUpper != null) {
            game.player.hmPokemon.drawUpper.draw(game);
         }

         game.player.drawPlayerUpper.step(game);

         while (i < game.map.onscreenYsort1.size()) {
            PkmnMap.YSort ySort = game.map.onscreenYsort1.get(i);

            for (Pokemon pokemon : ySort.ySortPokemon) {
               if (pokemon.drawUpper != null) {
                  pokemon.drawUpper.draw(game);
               }
            }

            for (Tile tile : ySort.ySortSavannaTrees) {
               game.mapBatch
                  .draw(
                     tile.overSprite.getTexture(),
                     tile.overSprite.getX(),
                     tile.overSprite.getY() + 16.0F,
                     tile.overSprite.getRegionX(),
                     tile.overSprite.getRegionY(),
                     tile.overSprite.getRegionWidth(),
                     tile.overSprite.getRegionHeight() - 16
                  );
            }

            for (Tile tile : ySort.ySortTrees) {
               game.mapBatch
                  .draw(
                     tile.overSprite.getTexture(),
                     tile.overSprite.getX(),
                     tile.overSprite.getY() + 8.0F,
                     tile.overSprite.getRegionX(),
                     tile.overSprite.getRegionY(),
                     tile.overSprite.getRegionWidth(),
                     tile.overSprite.getRegionHeight() - 8
                  );
            }

            i++;
         }
      }
   }
}
