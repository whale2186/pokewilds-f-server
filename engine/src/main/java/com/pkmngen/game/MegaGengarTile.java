package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;

class MegaGengarTile extends Tile {
   public MegaGengarTile(Vector2 pos) {
      super("mega_gengar_overworld1", pos);
   }

   @Override
   public void onPressA(Game game) {
      game.playerCanMove = false;
      game.insertAction(new SpecialBattleMegaGengar(game));
   }
}
