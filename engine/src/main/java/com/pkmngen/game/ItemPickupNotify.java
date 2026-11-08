package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class ItemPickupNotify extends Action {
   public Action.Layer layer = Action.Layer.gui_114;
   Sprite bgSprite;
   int signCounter = 150;
   String itemName;
   int quantity;

   public ItemPickupNotify(Game game, String itemName, int quantity) {
      super();
      this.itemName = itemName;
      this.quantity = quantity;
      Texture text = TextureCache.get(Gdx.files.internal("text2.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      this.bgSprite.setPosition(0.0F, -144.0F);
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
         game.font.draw(game.uiBatch, "Picked up " + this.itemName + " x" + this.quantity + ".", 42.0F, this.bgSprite.getY() + 134.0F);
      }
   }
}
