package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class SelectAmount extends Menu {
   public Action.Layer layer = Action.Layer.gui_105;
   Sprite textbox;
   public int amount = 0;
   int maxAmount = 1;
   Player.Craft craft;
   Action prevAction;
   boolean isMenu = false;

   public SelectAmount(Player.Craft craft, Action prevAction, Action nextAction) {
      this.craft = craft;
      this.prevAction = prevAction;
      if (this.prevAction != null && this.prevAction instanceof Menu) {
         this.isMenu = true;
      }

      Texture text = TextureCache.get(Gdx.files.internal("amount_bg1.png"));
      this.textbox = new Sprite(text, 0, 0, 160, 144);
      this.nextAction = nextAction;
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
      this.amount = 1;
   }

   @Override
   public void step(Game game) {
      if (this.isMenu) {
         this.prevAction.step(game);
      }

      this.textbox.draw(game.uiBatch);
      if (!this.disabled) {
         if (InputProcessor.upJustPressed) {
            if (game.player.hasCraftRequirements(this.craft, this.amount + 1)) {
               this.amount++;
            }
         } else if (InputProcessor.downJustPressed) {
            if (this.amount > 1) {
               this.amount--;
            }
         } else if (InputProcessor.rightJustPressed) {
            for (int i = 0; i < 10; i++) {
               if (game.player.hasCraftRequirements(this.craft, this.amount + 1)) {
                  this.amount++;
               }
            }
         } else if (InputProcessor.leftJustPressed) {
            this.amount -= 10;
            if (this.amount <= 1) {
               this.amount = 1;
            }
         }

         String word = "";
         if (this.amount <= 99) {
            word = word + "x";
         }

         word = word + String.format(Locale.ROOT, "%02d", this.amount);

         for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            Sprite letterSprite = game.textDict.get(letter);
            game.uiBatch.draw(letterSprite, 130 + 8 * i, 56.0F);
         }

         if (InputProcessor.aJustPressed) {
            this.disabled = true;
            this.craft.amount = this.amount;
            game.actionStack.remove(this);
            game.insertAction(new SelectAmount.Selected(this.craft, this, this.nextAction));
         } else if (InputProcessor.bJustPressed) {
            game.insertAction(new PlayMusic("click1", null));
            game.actionStack.remove(this);
            if (this.isMenu) {
               ((Menu)this.prevAction).disabled = false;
            }

            game.insertAction(this.prevAction);
            return;
         }
      }
   }

   class Selected extends Menu {
      Sprite arrow;
      Sprite arrowWhite;
      Sprite textBoxTop;
      Sprite textBoxMiddle;
      Sprite textBoxBottom;
      public Action.Layer layer = Action.Layer.gui_104;
      Map<Integer, Vector2> getCoords;
      int curr;
      Vector2 newPos;
      Sprite helperSprite;
      ArrayList<String> words;
      int textboxDelay = 0;
      Player.Craft craft;
      Action prevAction;
      boolean isMenu = false;

      public Selected(Player.Craft craft, Action prevAction, Action nextAction) {
         this.craft = craft;
         this.prevAction = prevAction;
         if (prevAction != null && prevAction instanceof Menu) {
            this.isMenu = true;
         }

         this.nextAction = nextAction;
         this.getCoords = new HashMap<>();
         this.words = new ArrayList<>();
         this.words.add("USE");
         this.words.add("CANCEL");
         this.getCoords.put(0, new Vector2(97.0F, 56.0F));
         this.getCoords.put(1, new Vector2(97.0F, 40.0F));
         Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
         this.arrow = new Sprite(text, 0, 0, 5, 7);
         text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
         this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
         text = TextureCache.get(Gdx.files.internal("menu/selected_menu_top.png"));
         this.textBoxTop = new Sprite(text, 0, 0, 71, 19);
         text = TextureCache.get(Gdx.files.internal("menu/selected_menu_middle.png"));
         this.textBoxMiddle = new Sprite(text, 0, 0, 71, 16);
         text = TextureCache.get(Gdx.files.internal("menu/selected_menu_bottom.png"));
         this.textBoxBottom = new Sprite(text, 0, 0, 71, 19);
         this.newPos = this.getCoords.get(0);
         this.arrow.setPosition(this.newPos.x, this.newPos.y);
         this.curr = 0;
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
         if (this.isMenu) {
            this.prevAction.step(game);
         }

         if (this.textboxDelay < 1) {
            this.textboxDelay++;
         } else {
            for (int i = 0; i < this.words.size(); i++) {
               if (i == 0) {
                  game.uiBatch.draw(this.textBoxTop, 89.0F, 68 + 16 * (this.words.size() - 3));
               } else if (i == this.words.size() - 1) {
                  game.uiBatch.draw(this.textBoxBottom, 89.0F, 33.0F);
               } else {
                  game.uiBatch.draw(this.textBoxMiddle, 89.0F, 52 + 16 * (this.words.size() - i - 2));
               }

               String word = this.words.get(i);

               for (int j = 0; j < word.length(); j++) {
                  char letter = word.charAt(j);
                  Sprite letterSprite = game.textDict.get(letter);
                  game.uiBatch.draw(letterSprite, 104 + 8 * j, 72 - 16 * (i - this.words.size() + 3));
               }
            }

            this.arrowWhite.setPosition(this.newPos.x, this.newPos.y);
            this.arrowWhite.draw(game.uiBatch);
            if (!this.disabled) {
               this.arrow.setPosition(this.newPos.x, this.newPos.y);
               game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
               if (InputProcessor.upJustPressed) {
                  if (this.curr > 0) {
                     this.curr--;
                     this.newPos = this.getCoords.get(this.curr);
                  }
               } else if (InputProcessor.downJustPressed && this.curr < this.words.size() - 1) {
                  this.curr++;
                  this.newPos = this.getCoords.get(this.curr);
               }

               if (InputProcessor.aJustPressed) {
                  String word = this.words.get(this.curr);
                  if ("CANCEL".equals(word)) {
                     game.insertAction(new PlayMusic("click1", null));
                     game.actionStack.remove(this);
                     if (this.isMenu) {
                        ((Menu)this.prevAction).disabled = false;
                     }

                     game.insertAction(this.prevAction);
                     return;
                  }

                  if ("USE".equals(word)) {
                     game.actionStack.remove(this);
                     game.insertAction(this.nextAction);
                  }
               } else if (InputProcessor.bJustPressed) {
                  game.insertAction(new PlayMusic("click1", null));
                  game.actionStack.remove(this);
                  if (this.isMenu) {
                     ((Menu)this.prevAction).disabled = false;
                  }

                  game.insertAction(this.prevAction);
                  return;
               }
            }
         }
      }
   }
}
