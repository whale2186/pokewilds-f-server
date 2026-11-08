package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;
import java.util.Map;

class DrawBattleMenu1 extends Action {
   Sprite arrow;
   public Action.Layer layer = Action.Layer.gui_129;
   Map<String, Vector2> getCoords = new HashMap<>();
   String curr;
   Vector2 newPos;

   public DrawBattleMenu1(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      this.arrow.setScale(3.0F);
      this.getCoords.put("tr", new Vector2(368.0F, 79.0F));
      this.getCoords.put("tl", new Vector2(224.0F, 79.0F));
      this.getCoords.put("br", new Vector2(368.0F, 31.0F));
      this.getCoords.put("bl", new Vector2(224.0F, 31.0F));
      this.newPos = new Vector2(224.0F, 79.0F);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.curr = "tl";
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
      if (InputProcessor.upPressed) {
         if (this.curr.equals("bl") || this.curr.equals("br")) {
            this.curr = "t" + String.valueOf(this.curr.charAt(1));
            this.newPos = this.getCoords.get(this.curr);
         }
      } else if (InputProcessor.downPressed) {
         if (this.curr.equals("tl") || this.curr.equals("tr")) {
            this.curr = "b" + String.valueOf(this.curr.charAt(1));
            this.newPos = this.getCoords.get(this.curr);
         }
      } else if (InputProcessor.leftPressed) {
         if (this.curr.equals("tr") || this.curr.equals("br")) {
            this.curr = this.curr.charAt(0) + "l";
            this.newPos = this.getCoords.get(this.curr);
         }
      } else if (InputProcessor.rightPressed && (this.curr.equals("tl") || this.curr.equals("bl"))) {
         this.curr = this.curr.charAt(0) + "r";
         this.newPos = this.getCoords.get(this.curr);
      }

      if (Gdx.input.isKeyPressed(54)) {
         if (this.curr.equals("tl")) {
            game.insertAction(new ThrowRock(game, this));
            game.actionStack.remove(this);
         }

         if (this.curr.equals("br")) {
            game.insertAction(new BattleFadeOut(game, new WaitFrames(game, 18, new DisplayText(game, "Got away safely!", null, null, null))));
            game.insertAction(new BattleFadeOutMusic(game, null));
            game.insertAction(new PlayMusic("click1", new PlayMusic("run1", null)));
            game.actionStack.remove(this);
            game.battle.drawAction.cleanup(game);
            game.battle.oppPokemon.inBattle = false;
         }
      }

      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.arrow.draw(game.uiBatch);
   }
}
