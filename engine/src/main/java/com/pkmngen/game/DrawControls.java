package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import java.util.ArrayList;

class DrawControls extends Action {
   public boolean remove = false;
   boolean displayControls = true;
   int timer = 0;
   int timerPadding = 240;
   float alpha = 1.0F;
   String currTrainerTip = "";
   int prevIndex = -1;
   ArrayList<String> messages;
   int randomIndex = 0;

   public DrawControls() {
      super();
      this.messages = new ArrayList<>(TrainerTipsTile.messages);
      this.randomIndex = Game.rand.nextInt(this.messages.size());
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
      PkmnMap.PeriodicSave.resetTimer();
      if (this.remove) {
         game.actionStack.remove(this);
      }

      game.otherFlipY = true;
      if (this.timer <= 60) {
         if (this.timer % 10 == 0) {
            this.alpha = this.timer / 60.0F;
         }
      } else if (this.timer >= 300 + this.timerPadding) {
         if (this.timer % 10 == 0) {
            this.alpha = 1.0F - this.timer % 60 / 60.0F;
         }
      } else if (InputProcessor.aJustPressed) {
         this.timer = 300 + this.timerPadding;
      }

      if (++this.timer >= 360 + this.timerPadding) {
         this.timer = 0;
         this.displayControls = false;
         this.timerPadding = 60;
         this.randomIndex++;
         if (this.randomIndex >= this.messages.size()) {
            this.randomIndex = 0;
         }

         this.currTrainerTip = "   TRAINER TIPS!  " + this.messages.get(this.randomIndex);

         for (int tries = 0; tries < 4 && (this.randomIndex == this.prevIndex || this.currTrainerTip.length() > 105); tries++) {
            this.randomIndex++;
            if (this.randomIndex >= this.messages.size()) {
               this.randomIndex = 0;
            }

            this.currTrainerTip = "   TRAINER TIPS!  " + this.messages.get(this.randomIndex);
         }

         this.prevIndex = this.randomIndex;
      }

      if (!this.displayControls) {
         char[] textArray = this.currTrainerTip.toCharArray();
         int i = 0;
         int j = 0;

         for (int k = 0; k < textArray.length; k++) {
            char character = textArray[k];
            if (character == ' ' && k + 1 < textArray.length) {
               int length = 1;
               char nextChar = textArray[k + length];

               while (nextChar != ' ' && k + length < textArray.length) {
                  nextChar = textArray[k + length++];
               }

               if (i + length > 20) {
                  i = 0;
                  j++;
               }
            }

            Sprite letterSprite = game.transparentDict.get(character);
            letterSprite.setPosition(8 * i, 128 - 16 * j);
            letterSprite.draw(game.uiBatch, this.alpha);
            if (++i > 20) {
               i = 0;
               j++;
            }
         }
      } else {
         for (int j = 0; j < 8; j++) {
            if (j == 0) {
               char[] textArray = "   - Controls -".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.transparentDict.get(textArray[i]);
                  letterSprite.setPosition(8 + 8 * i, 128 - 16 * j);
                  letterSprite.draw(game.uiBatch, this.alpha);
               }
            } else if (j == 1) {
               char[] textArray = "Arrows  - Movement".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.transparentDict.get(textArray[i]);
                  letterSprite.setPosition(8 + 8 * i, 128 - 16 * j);
                  letterSprite.draw(game.uiBatch, this.alpha);
               }
            } else if (j == 2) {
               char[] textArray = "Z       - A button".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.transparentDict.get(textArray[i]);
                  letterSprite.setPosition(8 + 8 * i, 128 - 16 * j);
                  letterSprite.draw(game.uiBatch, this.alpha);
               }
            } else if (j == 3) {
               char[] textArray = "X       - B button".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.transparentDict.get(textArray[i]);
                  letterSprite.setPosition(8 + 8 * i, 128 - 16 * j);
                  letterSprite.draw(game.uiBatch, this.alpha);
               }
            } else if (j == 4) {
               char[] textArray = "Enter   - Menu".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.transparentDict.get(textArray[i]);
                  letterSprite.setPosition(8 + 8 * i, 128 - 16 * j);
                  letterSprite.draw(game.uiBatch, this.alpha);
               }
            } else if (j == 5) {
               char[] textArray = "Hold X to run".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.transparentDict.get(textArray[i]);
                  letterSprite.setPosition(8 + 8 * i, 128 - 16 * j);
                  letterSprite.draw(game.uiBatch, this.alpha);
               }
            }
         }
      }
   }
}
