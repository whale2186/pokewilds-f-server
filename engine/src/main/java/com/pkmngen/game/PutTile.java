package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.Map;

class PutTile extends Action {
   public Action.Layer layer = Action.Layer.map_0;
   public Map<Vector2, Tile> tiles;
   public Tile tile;

   public PutTile(Game game, Tile tile, Action nextAction) {
      super();
      this.tile = tile;
      this.tiles = game.map.tiles;
      this.nextAction = nextAction;
   }

   @Override
   public String getCamera() {
      return "map";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      this.tiles.put(this.tile.position.cpy(), this.tile);
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
