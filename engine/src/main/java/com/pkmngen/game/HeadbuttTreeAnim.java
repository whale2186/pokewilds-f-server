package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SmolSprite;

class HeadbuttTreeAnim extends Action {
   SmolSprite originalSprite;
   SmolSprite rightSprite;
   SmolSprite leftSprite;
   int index = 0;
   Tile tile;
   Action nextAction;
   Sprite left;
   Sprite right;

   public HeadbuttTreeAnim(Game game, Tile tile, Action nextAction) {
      super();
      this.tile = tile;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      if (this.index == 0) {
         this.originalSprite = this.tile.overSprite;
         int width = (int)this.originalSprite.getWidth();
         int height = (int)this.originalSprite.getHeight();
         Pixmap pixmap = this.tile.overSprite.getPixmap();
         Color clearColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
         Pixmap rightPixmap = new Pixmap(width + 4, height, Format.RGBA8888);
         rightPixmap.setColor(clearColor);
         rightPixmap.fill();
         Pixmap leftPixmap = new Pixmap(width + 4, height, Format.RGBA8888);
         leftPixmap.setColor(clearColor);
         leftPixmap.fill();
         int left = this.tile.overSprite.getRegionX();
         int bottom = this.tile.overSprite.getRegionY();

         for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
               int additionalX = 0;
               if (height - j >= height / 4) {
                  if (height - j < 5 * height / 8) {
                     additionalX = 1;
                  } else if (height - j < 7 * height / 8) {
                     additionalX = 2;
                  } else if (height - j < 15 * height / 16) {
                     additionalX = 3;
                  } else {
                     additionalX = 4;
                  }
               }

               Color color = new Color(pixmap.getPixel(left + i, bottom + j));
               rightPixmap.drawPixel(i + additionalX, j, Color.rgba8888(color));
               leftPixmap.drawPixel(i - additionalX + 4, j, Color.rgba8888(color));
            }
         }

         this.rightSprite = new SmolSprite(new Texture(rightPixmap));
         this.rightSprite.setPosition(this.originalSprite.getX(), this.originalSprite.getY());
         this.leftSprite = new SmolSprite(new Texture(leftPixmap));
         this.leftSprite.setPosition((int)this.originalSprite.getX() - 4, this.originalSprite.getY());
         leftPixmap.dispose();
         rightPixmap.dispose();
      } else if (this.index == 14) {
         game.insertAction(new PlayMusic("headbutt1", null));
      } else if (this.index == 21) {
         this.tile.overSprite = this.rightSprite;
      } else if (this.index == 24) {
         this.tile.overSprite = this.originalSprite;
      } else if (this.index == 27) {
         this.tile.overSprite = this.leftSprite;
      } else if (this.index == 30) {
         this.tile.overSprite = this.originalSprite;
      } else if (this.index == 33) {
         this.tile.overSprite = this.rightSprite;
      } else if (this.index == 36) {
         this.tile.overSprite = this.originalSprite;
      } else if (this.index == 39) {
         this.tile.overSprite = this.leftSprite;
      } else if (this.index == 42) {
         this.tile.overSprite = this.originalSprite;
      } else if (this.index == 45) {
         this.tile.overSprite = this.rightSprite;
      } else if (this.index == 48) {
         this.tile.overSprite = this.originalSprite;
      } else if (this.index == 59) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.index++;
   }
}
