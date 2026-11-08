package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

class InputProcessor extends Action {
   public Action.Layer layer = Action.Layer.map_5000;
   public static boolean acceptInput = true;
   public static boolean upPressed = false;
   public static boolean downPressed = false;
   public static boolean leftPressed = false;
   public static boolean rightPressed = false;
   public static boolean aPressed = false;
   public static boolean bPressed = false;
   public static boolean startPressed = false;
   public static boolean upJustPressed = false;
   public static boolean downJustPressed = false;
   public static boolean leftJustPressed = false;
   public static boolean rightJustPressed = false;
   public static boolean aJustPressed = false;
   public static boolean bJustPressed = false;
   public static boolean startJustPressed = false;
   public static boolean lPressed = false;
   public static boolean lJustPressed = false;
   public static boolean rPressed = false;
   public static boolean rJustPressed = false;
   Vector2 touchLoc = new Vector2();
   public static int keyboardA = Keys.Z;
   public static int keyboardB = 52;
   public static int keyboardLeft = 21;
   public static int keyboardRight = 22;
   public static int keyboardUp = 19;
   public static int keyboardDown = 20;
   public static int keyboardStart = Keys.ENTER;
   public static int keyboardL = 31;
   public static int keyboardR = 50;
   boolean gamepadA;
   boolean gamepadB;
   boolean gamepadLeft;
   boolean gamepadRight;
   boolean gamepadUp;
   boolean gamepadDown;
   boolean gamepadStart;
   boolean gamepadL;
   boolean gamepadR;
   float gamepadAxisLeftX;
   float gamepadAxisLeftY;
   float gamepadAxisRightX;
   float gamepadAxisRightY;
   public static double gamepadMinAxis = 0.3;
   public static int gamepadA1;
   public static int gamepadA2;
   public static int gamepadB1;
   public static int gamepadB2;

   static boolean menuConfirmJustPressed() {
      return aJustPressed || startJustPressed
         || Gdx.input.isKeyJustPressed(keyboardA)
         || Gdx.input.isKeyJustPressed(Keys.ENTER)
         || Gdx.input.isKeyJustPressed(Keys.Z);
   }

   InputProcessor() {
      super();
   }

   static void clearControls() {
      upPressed = downPressed = leftPressed = rightPressed = aPressed = bPressed = startPressed = lPressed = rPressed = false;
      upJustPressed = downJustPressed = leftJustPressed = rightJustPressed = aJustPressed = bJustPressed = startJustPressed = lJustPressed = rJustPressed = false;
   }

   @Override
   public void step(Game game) {
      if (game.coop != null && (game.coop.hud.blocksInput() || game.coop.spectator.blocksInput() || game.coop.privateEditPending())) { clearControls(); return; }
      if (acceptInput) {
         if (Gdx.input.isTouched()) {
            float scaleX = 160.0F / game.currScreen.x;
            int offsetY = (int)((game.currScreen.y - 144.0F / scaleX) / 2.0F * scaleX);
            this.touchLoc.set(Gdx.input.getX() * scaleX, (game.currScreen.y - Gdx.input.getY()) * scaleX - offsetY);
         } else {
            this.touchLoc.set(-1.0F, -1.0F);
         }

         this.gamepadA = this.gamepadB = this.gamepadLeft = this.gamepadRight = this.gamepadUp = this.gamepadDown = this.gamepadStart = this.gamepadL = this.gamepadR = false;
         if (Game.gamepad != null) {
            if (Game.gamepad.getButton(gamepadA1) || Game.gamepad.getButton(gamepadA2)) {
               this.gamepadA = true;
            }

            if (Game.gamepad.getButton(gamepadB1) || Game.gamepad.getButton(gamepadB2)) {
               this.gamepadB = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonStart)) {
               this.gamepadStart = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonL1) || Game.gamepad.getButton(Game.gamepad.getMapping().buttonL2)) {
               this.gamepadL = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonR1) || Game.gamepad.getButton(Game.gamepad.getMapping().buttonR2)) {
               this.gamepadR = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonDpadLeft)) {
               this.gamepadLeft = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonDpadRight)) {
               this.gamepadRight = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonDpadUp)) {
               this.gamepadUp = true;
            }

            if (Game.gamepad.getButton(Game.gamepad.getMapping().buttonDpadDown)) {
               this.gamepadDown = true;
            }

            this.gamepadAxisLeftX = Game.gamepad.getAxis(Game.gamepad.getMapping().axisLeftX);
            this.gamepadAxisLeftY = Game.gamepad.getAxis(Game.gamepad.getMapping().axisLeftY);
            if (this.gamepadAxisLeftX > gamepadMinAxis || this.gamepadAxisLeftX < -gamepadMinAxis) {
               if (this.gamepadAxisLeftX > 0.0F) {
                  this.gamepadRight = true;
               } else {
                  this.gamepadLeft = true;
               }
            }

            if (this.gamepadAxisLeftY > gamepadMinAxis || this.gamepadAxisLeftY < -gamepadMinAxis) {
               if (this.gamepadAxisLeftY > 0.0F) {
                  this.gamepadDown = true;
               } else {
                  this.gamepadUp = true;
               }
            }

            this.gamepadAxisRightX = Game.gamepad.getAxis(Game.gamepad.getMapping().axisRightX);
            this.gamepadAxisRightY = Game.gamepad.getAxis(Game.gamepad.getMapping().axisRightY);
            if (this.gamepadAxisRightX > gamepadMinAxis || this.gamepadAxisRightX < -gamepadMinAxis) {
               if (this.gamepadAxisRightX > 0.0F) {
                  this.gamepadRight = true;
               } else {
                  this.gamepadLeft = true;
               }
            }

            if (this.gamepadAxisRightY > gamepadMinAxis || this.gamepadAxisRightY < -gamepadMinAxis) {
               if (this.gamepadAxisRightY > 0.0F) {
                  this.gamepadDown = true;
               } else {
                  this.gamepadUp = true;
               }
            }
         }

         if (!Gdx.input.isKeyPressed(keyboardUp) && !this.gamepadUp && !DrawMobileControls.upArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            upJustPressed = false;
            upPressed = false;
         } else {
            upJustPressed = false;
            if (!upPressed) {
               upJustPressed = true;
            }

            upPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardDown) && !this.gamepadDown && !DrawMobileControls.downArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            downJustPressed = false;
            downPressed = false;
         } else {
            downJustPressed = false;
            if (!downPressed) {
               downJustPressed = true;
            }

            downPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardLeft) && !this.gamepadLeft && !DrawMobileControls.leftArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            leftJustPressed = false;
            leftPressed = false;
         } else {
            leftJustPressed = false;
            if (!leftPressed) {
               leftJustPressed = true;
            }

            leftPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardRight)
            && !this.gamepadRight
            && !DrawMobileControls.rightArrowSprite.getBoundingRectangle().contains(this.touchLoc)) {
            rightJustPressed = false;
            rightPressed = false;
         } else {
            rightJustPressed = false;
            if (!rightPressed) {
               rightJustPressed = true;
            }

            rightPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardA) && !Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A) && !Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.Z) && !Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ENTER) && !this.gamepadA && !DrawMobileControls.aSprite.getBoundingRectangle().contains(this.touchLoc)) {
            aJustPressed = false;
            aPressed = false;
         } else {
            aJustPressed = false;
            if (!aPressed) {
               aJustPressed = true;
            }

            aPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardB) && !this.gamepadB && !DrawMobileControls.bSprite.getBoundingRectangle().contains(this.touchLoc)) {
            bJustPressed = false;
            bPressed = false;
         } else {
            bJustPressed = false;
            if (!bPressed) {
               bJustPressed = true;
            }

            bPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardStart) && !this.gamepadStart && !DrawMobileControls.startSprite.getBoundingRectangle().contains(this.touchLoc)) {
            startJustPressed = false;
            startPressed = false;
         } else {
            startJustPressed = false;
            if (!startPressed) {
               startJustPressed = true;
            }

            startPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardL) && !this.gamepadL) {
            lJustPressed = false;
            lPressed = false;
         } else {
            lJustPressed = false;
            if (!lPressed) {
               lJustPressed = true;
            }

            lPressed = true;
         }

         if (!Gdx.input.isKeyPressed(keyboardR) && !this.gamepadR) {
            rJustPressed = false;
            rPressed = false;
         } else {
            rJustPressed = false;
            if (!rPressed) {
               rJustPressed = true;
            }

            rPressed = true;
         }
      }
   }

   @Override
   public String getCamera() {
      return "map";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }
}
