package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;
import java.util.Map;

class DrawBattleMenuNormal extends Menu {
   Sprite arrow;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_109;
   Map<String, Vector2> getCoords = new HashMap<>();
   String curr;
   Vector2 newPos;
   Sprite helperSprite;

   public DrawBattleMenuNormal(Game game, Action nextAction) {
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/battle_menu1.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      this.getCoords.put("tr", new Vector2(121.0F, 24.0F));
      this.getCoords.put("tl", new Vector2(73.0F, 24.0F));
      this.getCoords.put("br", new Vector2(121.0F, 8.0F));
      this.getCoords.put("bl", new Vector2(73.0F, 8.0F));
      this.newPos = new Vector2(73.0F, 24.0F);
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
      if (!this.disabled) {
         if (game.battle.playerLockedMove != -1) {
            Action attack = new Action();
            if (game.type == Game.Type.CLIENT) {
               String attackName = game.player.currPokemon.attacks[game.battle.playerLockedMove];
               attack.append(new Battle.WaitTurnData(game, null));
               game.client.sendTCP(new Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.ATTACK, attackName));
            }

            attack.append(new Battle.DoTurn(game, Battle.DoTurn.Type.ATTACK, null));
            attack.append(new WaitFrames(game, 4, this));
            game.actionStack.remove(this);
            game.insertAction(attack);
         } else {
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

            if (InputProcessor.aJustPressed) {
               if (this.curr.equals("tl")) {
                  game.insertAction(new PlayMusic("click1", null));
                  game.insertAction(new WaitFrames(game, 4, new DrawAttacksMenu(new WaitFrames(game, 4, this))));
                  game.actionStack.remove(this);
               } else if (this.curr.equals("bl")) {
                  this.disabled = true;
                  game.actionStack.remove(this);
                  game.insertAction(new PlayMusic("click1", null));
                  DrawItemMenuGen2 menu = new DrawItemMenuGen2(game, this);
                  menu.disabled = true;
                  game.insertAction(
                     new DrawItemMenu.Intro(this, 6, new DrawWhiteScreen(18, new DrawItemMenu.Intro(menu, 9, new RunCode(() -> menu.disabled = false, menu))))
                  );
               } else if (this.curr.equals("tr")) {
                  this.disabled = true;
                  game.insertAction(new DisplayText.Clear(game, new WaitFrames(game, 3, new DrawPokemonMenu.Intro(new DrawPokemonMenu(game, this)))));
                  game.actionStack.remove(this);
               } else if (this.curr.equals("br")) {
                  Action runAction = new SplitAction(new PlayMusic("click1", null), null);
                  if (game.battle.oppPokemon.isTrapping) {
                     runAction.append(new DisplayText(game, "Canì escape!", null, null, this));
                     game.actionStack.remove(this);
                     game.insertAction(runAction);
                     return;
                  }

                  if (game.type == Game.Type.CLIENT) {
                     runAction.append(new Battle.WaitTurnData(game, null));
                     game.client.sendTCP(new Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.RUN, ""));
                  }

                  runAction.append(new Battle.DoTurn(game, Battle.DoTurn.Type.RUN, this));
                  game.actionStack.remove(this);
                  game.insertAction(runAction);
               }
            }

            this.textBox.draw(game.uiBatch);
            this.arrow.setPosition(this.newPos.x, this.newPos.y);
            this.arrow.draw(game.uiBatch);
         }
      }
   }
}
