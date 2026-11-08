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

class DrawAttacksMenu extends Action {
   public Action.Layer layer = Action.Layer.gui_103;
   public static int curr = 0;
   Sprite arrow;
   Sprite arrowWhite;
   Sprite textBox;
   Map<Integer, Vector2> coords = new HashMap<>();
   Vector2 newPos;
   Sprite helperSprite;
   ArrayList<ArrayList<Sprite>> spritesToDraw = new ArrayList<>();
   int cursorDelay;
   String attackLearning = null;
   Vector2 offset = new Vector2(0.0F, 0.0F);
   Pokemon pokemon;
   boolean isSorting = false;
   int sortingIndex = -1;

   public DrawAttacksMenu(Action nextAction) {
      this(null, Game.staticGame.player.currPokemon, nextAction);
   }

   public DrawAttacksMenu(String attackLearning, Pokemon pokemon, Action nextAction) {
      super();
      this.attackLearning = attackLearning;
      this.pokemon = pokemon;
      if (this.attackLearning != null) {
         this.offset.add(0.0F, 48.0F);
      }

      this.nextAction = nextAction;
      this.cursorDelay = 0;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("menu/attack_screen1.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      this.textBox.setPosition(this.offset.x, this.offset.y);
      this.coords.put(0, new Vector2(41.0F, 32.0F));
      this.coords.put(1, new Vector2(41.0F, 24.0F));
      this.coords.put(2, new Vector2(41.0F, 16.0F));
      this.coords.put(3, new Vector2(41.0F, 8.0F));
   }

   @Override
   public void firstStep(Game game) {
      this.newPos = this.coords.get(curr);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.spritesToDraw.clear();

      for (String attack : this.pokemon.attacks) {
         if (attack == null) {
            attack = "-";
         }

         char[] textArray = attack.toUpperCase(Locale.ROOT).toCharArray();
         int i = 0;
         int j = 0;
         ArrayList<Sprite> word = new ArrayList<>();

         for (char letter : textArray) {
            Sprite letterSprite = Game.staticGame.textDict.get(letter);
            if (letterSprite == null) {
               letterSprite = Game.staticGame.textDict.get(null);
            }

            Sprite currSprite = new Sprite(letterSprite);
            currSprite.setPosition(10 + 8 * i + 2 - 4, this.offset.y + 26.0F - 16 * j + 2.0F - 4.0F);
            word.add(currSprite);
            if (i >= 17) {
               i = 0;
               j++;
            } else {
               i++;
            }
         }

         this.spritesToDraw.add(word);
      }
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
      if (InputProcessor.upJustPressed) {
         if (curr != 0) {
            curr--;
            this.newPos = this.coords.get(curr);
         }
      } else if (InputProcessor.downJustPressed && curr + 1 < this.pokemon.attacks.length && this.pokemon.attacks[curr + 1] != null) {
         curr++;
         this.newPos = this.coords.get(curr);
      }

      if (InputProcessor.aJustPressed) {
         if (this.isSorting) {
            String temp = this.pokemon.attacks[this.sortingIndex];
            this.pokemon.attacks[this.sortingIndex] = this.pokemon.attacks[curr];
            this.pokemon.attacks[curr] = temp;
            this.isSorting = false;
            this.sortingIndex = -1;
            this.firstStep(game);
         } else if (this.attackLearning != null) {
            game.actionStack.remove(this);
            game.insertAction(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(
                        game,
                        "Replace " + this.pokemon.attacks[curr].toUpperCase(Locale.ROOT) + "?",
                        null,
                        true,
                        true,
                        new DrawYesNoMenu(
                           null,
                           new DisplayText.Clear(
                              game,
                              new WaitFrames(
                                 game,
                                 3,
                                 new DisplayText(
                                    game,
                                    "And one... two... three... POOF!",
                                    null,
                                    null,
                                    new DisplayText(
                                       game,
                                       this.pokemon.nickname.toUpperCase(Locale.ROOT)
                                          + " forgot "
                                          + this.pokemon.attacks[curr].toUpperCase(Locale.ROOT)
                                          + " and...",
                                       null,
                                       null,
                                       new DisplayText(
                                          game,
                                          this.pokemon.nickname.toUpperCase(Locale.ROOT) + " learned " + this.attackLearning.toUpperCase(Locale.ROOT) + "!",
                                          "fanfare1.ogg",
                                          false,
                                          true,
                                          new SetArrayAtIndex(this.pokemon.attacks, curr, this.attackLearning, this.nextAction)
                                       )
                                    )
                                 )
                              )
                           ),
                           new DisplayText.Clear(
                              game, new WaitFrames(game, 3, new DisplayText(game, "Which move should be forgotten?", null, true, true, this))
                           )
                        )
                     )
                  )
               )
            );
         } else if (this.pokemon.disabledIndex == curr) {
            game.actionStack.remove(this);
            game.insertAction(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(game, this.pokemon.attacks[curr].toUpperCase(Locale.ROOT) + " is disabled!", null, null, new WaitFrames(game, 3, this))
                  )
               )
            );
         } else {
            game.player.numFlees = 0;
            Action attack = new SplitAction(new PlayMusic("click1", null), null);
            if (game.type == Game.Type.CLIENT) {
               String attackName = this.pokemon.attacks[curr];
               attack.append(new Battle.WaitTurnData(game, null));
               game.client.sendTCP(new Network.DoBattleAction(game.player.network.id, Battle.DoTurn.Type.ATTACK, attackName));
            }

            attack.append(new Battle.DoTurn(game, Battle.DoTurn.Type.ATTACK, this.nextAction));
            game.actionStack.remove(this);
            game.insertAction(attack);
         }
      } else {
         if (InputProcessor.bJustPressed) {
            if (this.attackLearning != null) {
               game.actionStack.remove(this);
               game.insertAction(
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           "Give up on learning " + this.attackLearning.toUpperCase(Locale.ROOT) + "?",
                           null,
                           true,
                           true,
                           new DrawYesNoMenu(
                              null,
                              new DisplayText.Clear(
                                 game,
                                 new WaitFrames(
                                    game,
                                    3,
                                    new DisplayText(
                                       game,
                                       this.pokemon.nickname.toUpperCase(Locale.ROOT) + " did not learn " + this.attackLearning.toUpperCase(Locale.ROOT) + ".",
                                       null,
                                       null,
                                       this.nextAction
                                    )
                                 )
                              ),
                              new DisplayText.Clear(
                                 game, new WaitFrames(game, 3, new DisplayText(game, "Which move should be forgotten?", null, true, true, this))
                              )
                           )
                        )
                     )
                  )
               );
               return;
            }

            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         } else if (InputProcessor.startJustPressed && this.attackLearning == null) {
            this.isSorting = true;
            this.sortingIndex = curr;
         }

         this.textBox.draw(game.uiBatch);
         int j = 0;

         for (ArrayList<Sprite> word : this.spritesToDraw) {
            for (Sprite sprite : word) {
               game.uiBatch.draw(sprite, sprite.getX() + 40.0F, sprite.getY() - j * 8 + 8.0F);
            }

            j++;
         }

         Attack attack = game.battle.attacks.get(this.pokemon.attacks[curr]);
         String type = attack.type.toUpperCase(Locale.ROOT);
         int qmarkOffset = 0;
         if (type.equals("CURSE_T")) {
            type = "???";
            qmarkOffset = 1;
         }

         char[] textArray = type.toCharArray();

         for (int m = 0; m < textArray.length; m++) {
            Sprite letterSprite = game.textDict.get(textArray[m]);
            game.uiBatch.draw(letterSprite, this.offset.x + 16.0F + 8 * m, this.offset.y + 56.0F + qmarkOffset);
         }

         if (Game.specialPhysicalSplitEnabled) {
            textArray = attack.category.name().toUpperCase(Locale.ROOT).toCharArray();

            for (int m = 0; m < textArray.length; m++) {
               Sprite letterSprite = game.textDict.get(textArray[m]);
               game.uiBatch.draw(letterSprite, this.offset.x + 16.0F + 8 * m, this.offset.y + 56.0F - 8.0F);
            }
         }

         if (this.cursorDelay >= 2) {
            this.arrow.setPosition(this.newPos.x + this.offset.x, this.newPos.y + this.offset.y);
            this.arrow.draw(game.uiBatch);
         } else {
            this.cursorDelay++;
         }

         if (this.isSorting) {
            Vector2 arrowPos = this.coords.get(this.sortingIndex);
            if (arrowPos != null) {
               game.uiBatch.draw(this.arrowWhite, arrowPos.x + this.offset.x, arrowPos.y + this.offset.y);
            }
         }
      }
   }
}
