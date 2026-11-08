package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class DrawText extends Menu {
   String text = null;
   Texture texture;

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return Action.Layer.gui_106;
   }

   public DrawText(String text) {
      this.text = text;
      this.texture = TextureCache.get(Gdx.files.internal("textbox_bg1.png"));
   }

   @Override
   public void step(Game game) {
      if (this.goAway) {
         game.actionStack.remove(this);
      } else {
         if (this.text != null) {
            game.uiBatch.draw(this.texture, 0.0F, 0.0F);
            int i = 0;
            int j = 0;
            char[] characters = this.text.toCharArray();

            for (int k = 0; k < characters.length; k++) {
               char character = characters[k];
               if (character == ' ' && k + 1 < characters.length) {
                  int length = 1;
                  char nextChar = characters[k + length];

                  while (nextChar != ' ' && k + length < characters.length) {
                     nextChar = characters[k + length++];
                  }

                  if (i + length > 20 && j < 5) {
                     i = -1;
                     j++;
                  }
               }

               Sprite letterSprite = game.textDict.get(character);
               letterSprite.setPosition(8 + 8 * i, 24 - 16 * j);
               letterSprite.draw(game.uiBatch, 1.0F);
               if (++i > 20) {
                  i = 0;
                  j++;
               }
            }
         }
      }
   }
}
