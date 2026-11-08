package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

class DrawItemPickup extends Action {
   Sprite textBoxTop;
   Sprite textBoxMiddle;
   Sprite textBoxBottom;
   ArrayList<String> words = new ArrayList<>();
   ArrayList<Color> wordColors = new ArrayList<>();
   Vector2 topLeft = new Vector2(89.0F, 144.0F);
   int timer = 0;
   HashMap<String, Integer> items;
   Color color = new Color();

   public DrawItemPickup(HashMap<String, Integer> items, Action nextAction) {
      super();
      this.items = items;
      this.nextAction = nextAction;
      Texture texture = TextureCache.get(Gdx.files.internal("menu/selected_menu_top.png"));
      this.textBoxTop = new Sprite(texture, 0, 0, 71, 19);
      texture = TextureCache.get(Gdx.files.internal("menu/selected_menu_middle.png"));
      this.textBoxMiddle = new Sprite(texture, 0, 0, 71, 16);
      texture = TextureCache.get(Gdx.files.internal("menu/selected_menu_bottom.png"));
      this.textBoxBottom = new Sprite(texture, 0, 0, 71, 19);
      this.words.clear();
      this.wordColors.clear();
      this.words.add("GOT");
      this.wordColors.add(new Color(1.0F, 1.0F, 1.0F, 1.0F));

      for (String item : this.items.keySet()) {
         String[] texts = item.toUpperCase(Locale.ROOT).split(" ");

         for (int i = 0; i < texts.length; i++) {
            String text = texts[i];
            if (text.contains("APRICORN")) {
               text = "APRCN";
            } else if (text.equals("BEDDING")) {
               text = "BED";
            }

            if (i >= texts.length - 1) {
               int numGot = this.items.get(item);
               int length = text.length();

               for (int j = 0; j < 5 - length; j++) {
                  text = text + " ";
               }

               text = text + "x";
               text = text + String.valueOf(numGot);
            }

            this.wordColors.add(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            this.words.add(text);
         }
      }
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public void firstStep(Game game) {
   }

   @Override
   public void step(Game game) {
      if (this.timer > 90) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
      int height = this.words.size();
      if (height < 2) {
         height = 2;
      }

      for (int i = 0; i < height; i++) {
         if (i == 0) {
            game.uiBatch.draw(this.textBoxTop, this.topLeft.x, this.topLeft.y - 19.0F);
         } else if (i == height - 1) {
            game.uiBatch.draw(this.textBoxBottom, this.topLeft.x, this.topLeft.y - 19.0F - 16 * i);
         } else {
            game.uiBatch.draw(this.textBoxMiddle, this.topLeft.x, this.topLeft.y - 19.0F - 16 * i);
         }

         this.color.set(game.uiBatch.getColor());
         if (i < this.words.size()) {
            String word = this.words.get(i);

            for (int j = 0; j < word.length(); j++) {
               char letter = word.charAt(j);
               Sprite letterSprite = new Sprite(game.textDict.get(letter));
               letterSprite.setPosition(this.topLeft.x + 8.0F + 8 * j, this.topLeft.y - 14.0F - 16 * i);
               game.uiBatch.setColor(this.color.r, this.color.g, this.color.b, this.wordColors.get(i).a);
               game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
            }
         }

         game.uiBatch.setColor(this.color);
      }
   }
}
