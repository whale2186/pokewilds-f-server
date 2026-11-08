package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class DrawYesNoMenu extends Menu {
   Action nextAction2;
   Sprite arrow;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_104;
   Map<Integer, Vector2> getCoords = new HashMap<>();
   int curr;
   Vector2 newPos;
   Sprite helperSprite;
   int cursorDelay;
   String itemName;
   Menu prevMenu;
   ArrayList<String> words = new ArrayList<>();

   public DrawYesNoMenu(Menu prevMenu, Action yesAction, Action noAction) {
      this(prevMenu, 0, yesAction, noAction);
   }

   public DrawYesNoMenu(Menu prevMenu, int startIndex, Action yesAction, Action noAction) {
      this.prevMenu = prevMenu;
      this.nextAction = yesAction;
      this.nextAction2 = noAction;
      this.cursorDelay = 0;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("menu/yesno_bg1.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      this.getCoords.put(0, new Vector2(121.0F, 72.0F));
      this.getCoords.put(1, new Vector2(121.0F, 56.0F));
      this.curr = startIndex;
      this.newPos = this.getCoords.get(this.curr);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.words.add("YES");
      this.words.add("NO");
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
      if (this.prevMenu != null) {
         this.prevMenu.step(game);
      }

      game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());

      for (int i = 0; i < this.words.size(); i++) {
         String word = this.words.get(i);

         for (int j = 0; j < word.length(); j++) {
            char letter = word.charAt(j);
            game.uiBatch.draw(game.textDict.get(letter), 128 + 8 * j, 71 - 16 * i);
         }
      }

      if (!this.disabled) {
         if (InputProcessor.upJustPressed) {
            if (this.curr > 0) {
               this.curr--;
               this.newPos = this.getCoords.get(this.curr);
            }
         } else if (InputProcessor.downJustPressed && this.curr < 1) {
            this.curr++;
            this.newPos = this.getCoords.get(this.curr);
         }

         if (this.cursorDelay >= 2) {
            this.arrow.setPosition(this.newPos.x, this.newPos.y);
            game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
         } else {
            this.cursorDelay++;
         }

         if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            if (this.curr == 0) {
               game.insertAction(this.nextAction);
            } else {
               game.insertAction(this.nextAction2);
            }
         } else if (InputProcessor.bJustPressed) {
            game.actionStack.remove(this);
            if (this.prevMenu != null) {
               this.prevMenu.disabled = false;
               game.insertAction(this.prevMenu);
            } else {
               game.insertAction(this.nextAction2);
            }
         }
      }
   }
}
