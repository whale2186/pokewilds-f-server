package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class PlantTree extends Action {
   public Action.Layer layer = Action.Layer.gui_0;
   String type;
   Vector2 pos;
   ArrayList<SmolSprite> sprites = new ArrayList<>();
   int timer = 0;
   Tile currTile;

   public PlantTree(Vector2 pos, Action nextAction) {
      this("tree_planted2", pos, nextAction);
   }

   public PlantTree(String type, Vector2 pos, Action nextAction) {
      super();
      this.type = type;
      this.pos = pos;
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("sprout_sheet2.png"));

      for (int i = 0; i < 5; i++) {
         SmolSprite sprite = new SmolSprite(text, 16 * i, 0, 16, 16);
         sprite.setPosition(pos.x, pos.y + 2.0F);
         this.sprites.add(sprite);
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
      this.currTile = game.map.tiles.get(this.pos);
      this.currTile.nameUpper = "tree_planted2";
   }

   @Override
   public void step(Game game) {
      Player.drawSproutSprite = false;
      if (this.timer == 0) {
         this.currTile.overSprite = this.sprites.get(0);
      } else if (this.timer == 12) {
         this.currTile.overSprite = this.sprites.get(1);
      } else if (this.timer == 15) {
         this.currTile.overSprite = this.sprites.get(2);
      } else if (this.timer == 18) {
         this.currTile.overSprite = this.sprites.get(3);
      } else if (this.timer == 21) {
         this.currTile.overSprite = this.sprites.get(4);
      } else if (this.timer == 24) {
         Player.drawSproutSprite = true;
         game.map.tiles.put(this.pos.cpy(), new Tile(this.currTile.name, this.type, this.pos.cpy(), true, this.currTile.routeBelongsTo));
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
   }
}
