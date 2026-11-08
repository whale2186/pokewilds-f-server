package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class RequirementNotify extends Action {
   public Action.Layer layer = Action.Layer.gui_114;
   Sprite bgSprite;
   int signCounter = 100;
   String tileName;
   String text = "";

   public RequirementNotify(Game game, String tileName) {
      super();
      this.tileName = tileName;
      Texture text = TextureCache.get(Gdx.files.internal("text2.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      this.bgSprite.setPosition(0.0F, -144.0F);

      for (String name : game.player.buildTileRequirements.get(tileName).keySet()) {
         this.text = this.text + name + ": " + game.player.buildTileRequirements.get(tileName).get(name) + " ";
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
   public void step(Game game) {
      if (this.signCounter > 0) {
         this.signCounter--;
         if (this.signCounter <= 100) {
            if (this.signCounter > 78) {
               this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY() + 1.0F);
            } else if (this.signCounter <= 22) {
               this.bgSprite.setPosition(this.bgSprite.getX(), this.bgSprite.getY() - 1.0F);
            }
         }

         this.bgSprite.draw(game.uiBatch);
         game.font.draw(game.uiBatch, "Requires: " + this.text, 10.0F, this.bgSprite.getY() + 134.0F);
      }
   }
}
