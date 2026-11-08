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
import java.util.Map;

class DrawCraftsMenu extends Menu {
   public static int lastCurrIndex = 0;
   public static int lastCursorPos = 0;
   Sprite arrow;
   Sprite arrowWhite;
   Sprite textBox;
   public Action.Layer layer = Action.Layer.gui_106;
   Map<Integer, Vector2> arrowCoords = new HashMap<>();
   Vector2 newPos;
   Sprite helperSprite;
   ArrayList<ArrayList<Sprite>> spritesToDraw = new ArrayList<>();
   int cursorDelay;
   int cursorPos;
   int currIndex;
   ArrayList<String> craftsList;
   Sprite downArrow;
   int downArrowTimer;
   Sprite craftReqsTextbox;
   Sprite titleTextbox;
   String currCraft = "";
   ArrayList<String> craftReqs = new ArrayList<>();
   ArrayList<Color> craftReqColors = new ArrayList<>();
   Vector2 topLeft = new Vector2(0.0F, 26.0F);
   boolean refresh = false;
   ArrayList<Player.Craft> crafts;
   Color prevColor = new Color();

   public DrawCraftsMenu(Game game, Action nextAction) {
      this(game, Player.crafts, (Menu)null);
      this.nextAction = nextAction;
   }

   public DrawCraftsMenu(Game game, ArrayList<Player.Craft> crafts, Action nextAction) {
      this(game, crafts, (Menu)null);
      this.nextAction = nextAction;
   }

   public DrawCraftsMenu(Game game, Menu prevMenu) {
      this(game, Player.crafts, prevMenu);
   }

   public DrawCraftsMenu(Game game, ArrayList<Player.Craft> crafts, Menu prevMenu) {
      this.crafts = crafts;
      this.prevMenu = prevMenu;
      this.disabled = false;
      this.cursorDelay = 0;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("menu/item_menu1.png"));
      this.textBox = new Sprite(text, 0, 0, 160, 144);
      text = TextureCache.get(Gdx.files.internal("textbox_bg1.png"));
      this.craftReqsTextbox = new Sprite(text, 0, 0, 160, 144);
      text = TextureCache.get(Gdx.files.internal("title_bg1.png"));
      this.titleTextbox = new Sprite(text, 0, 0, 160, 144);
      text = TextureCache.get(Gdx.files.internal("arrow_down.png"));
      this.downArrow = new Sprite(text, 0, 0, 7, 5);
      this.downArrow.setPosition(144.0F, 50.0F);
      this.downArrowTimer = 0;
      this.currIndex = lastCurrIndex;
      this.cursorPos = lastCursorPos;
      this.arrowCoords.put(0, new Vector2(41.0F, 104.0F));
      this.arrowCoords.put(1, new Vector2(41.0F, 88.0F));
      this.arrowCoords.put(2, new Vector2(41.0F, 72.0F));
      this.arrowCoords.put(3, new Vector2(41.0F, 56.0F));
      this.newPos = this.arrowCoords.get(this.cursorPos);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
      this.craftsList = new ArrayList<>();

      for (Player.Craft craft : this.crafts) {
         this.craftsList.add(craft.name);
      }

      this.craftsList.add("Cancel");
      if (this.crafts == game.player.regiCrafts || this.crafts == Player.kilnCrafts) {
         this.textBox.setPosition(0.0F, 16.0F);
         text = TextureCache.get(Gdx.files.internal("textbox_bg2.png"));
         this.craftReqsTextbox = new Sprite(text, 0, 0, 160, 144);
         this.downArrow.setPosition(144.0F, 66.0F);
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

   public void refresh(Game game, int index) {
      this.craftReqs.clear();
      this.craftReqColors.clear();

      for (Player.Craft craft : this.crafts.get(index).requirements) {
         String text = craft.name.toUpperCase(Locale.ROOT);
         int numNeeded = craft.amount;
         int offset = 0;
         if (numNeeded >= 100) {
            offset = 2;
         } else if (numNeeded >= 10) {
            offset = 1;
         }

         for (int i = 0; i < 16 - craft.name.length() - offset; i++) {
            text = text + " ";
         }

         text = text + "x";
         text = text + String.valueOf(numNeeded);
         if (game.player.hasItem(craft.name) && game.player.getItemAmount(craft.name) >= numNeeded) {
            this.craftReqColors.add(new Color(1.0F, 1.0F, 1.0F, 1.0F));
         } else {
            this.craftReqColors.add(new Color(1.0F, 1.0F, 1.0F, 0.5F));
         }

         this.craftReqs.add(text);
      }

      this.spritesToDraw.clear();

      for (String entry : this.craftsList) {
         char[] textArray = entry.toUpperCase(Locale.ROOT).toCharArray();
         int i = 0;
         ArrayList<Sprite> word = new ArrayList<>();

         for (char letter : textArray) {
            Sprite letterSprite = game.textDict.get(letter);
            if (letterSprite == null) {
               letterSprite = game.textDict.get(null);
            }

            if (this.crafts == game.player.regiCrafts) {
               letterSprite = game.brailleDict.get(letter);
               if (letterSprite == null) {
                  letterSprite = game.textDict.get(' ');
               }
            }

            Sprite currSprite = new Sprite(letterSprite);
            currSprite.setPosition(48 + 8 * i, 104.0F);
            word.add(currSprite);
            i++;
         }

         if (game.player.hasItem(entry)) {
            char[] numItemsArray = String.format(Locale.ROOT, "%02d", game.player.getItemAmount(entry)).toCharArray();
            i = 10;
            boolean foundDigit = false;

            for (char letter : numItemsArray) {
               i++;
               if (letter != '0' || foundDigit) {
                  foundDigit = true;
                  Sprite letterSprite = game.textDict.get(letter);
                  Sprite currSprite = new Sprite(letterSprite);
                  currSprite.setPosition(48 + 8 * i, 104.0F);
                  word.add(currSprite);
               }
            }
         }

         this.spritesToDraw.add(word);
      }
   }

   @Override
   public void firstStep(Game game) {
      while (this.currIndex + this.cursorPos > this.crafts.size() - 2) {
         if (this.currIndex > 0) {
            this.currIndex--;
         } else {
            if (this.cursorPos > 0) {
               this.cursorPos--;
               continue;
            }
            break;
         }
      }

      lastCurrIndex = this.currIndex;
      lastCursorPos = this.cursorPos;
      this.newPos = this.arrowCoords.get(this.cursorPos);
      this.arrow.setPosition(this.newPos.x, this.newPos.y);
   }

   @Override
   public void step(Game game) {
      if (this.prevMenu != null) {
         this.prevMenu.step(game);
      }

      if (this.crafts != game.player.regiCrafts && this.crafts != Player.kilnCrafts) {
         game.uiBatch.draw(this.titleTextbox, this.titleTextbox.getX(), this.titleTextbox.getY());
      }

      game.uiBatch.draw(this.craftReqsTextbox, this.craftReqsTextbox.getX(), this.craftReqsTextbox.getY());
      game.uiBatch.draw(this.textBox, this.textBox.getX(), this.textBox.getY());
      int j = 0;

      for (int i = 0; i < this.spritesToDraw.size(); i++) {
         if (i >= this.currIndex && i < this.currIndex + 4) {
            for (Sprite sprite : this.spritesToDraw.get(i)) {
               int extra = 0;
               if (this.crafts == game.player.regiCrafts || this.crafts == Player.kilnCrafts) {
                  extra = 16;
               }

               game.uiBatch.draw(sprite, sprite.getX(), sprite.getY() - j * 16 + extra);
            }

            j++;
         }
      }

      int index = lastCurrIndex + lastCursorPos;
      if (index >= this.crafts.size()) {
         this.craftReqs.clear();
         this.craftReqColors.clear();
         this.currCraft = "";
      } else {
         String curr = this.crafts.get(index).name;
         if (curr != null && !this.currCraft.equals(curr)) {
            this.currCraft = curr;
            this.refresh(game, index);
         }
      }

      char[] textArray = "CRAFTING MENU".toCharArray();
      if (this.crafts == game.player.fossilCrafts) {
         textArray = "FOSSIL MENU".toCharArray();
      }

      j = 0;

      for (char letter : textArray) {
         Sprite letterSprite = game.textDict.get(letter);
         if (letterSprite == null) {
            letterSprite = game.textDict.get(null);
         }

         if (this.crafts == game.player.regiCrafts || this.crafts == Player.kilnCrafts) {
            break;
         }

         game.uiBatch.draw(letterSprite, 8 + 8 * j, 128.0F);
         j++;
      }

      for (int i = 0; i < this.craftReqs.size(); i++) {
         String word = this.craftReqs.get(i);

         for (int k = 0; k < word.length(); k++) {
            char letter = word.charAt(k);
            int extra = 0;
            if (this.crafts == game.player.regiCrafts || this.crafts == Player.kilnCrafts) {
               extra = 16;
            }

            Sprite letterSprite = new Sprite(game.textDict.get(letter));
            letterSprite.setPosition(this.topLeft.x + 8.0F + 8 * k, this.topLeft.y - 16 * i + extra);
            this.prevColor.set(game.uiBatch.getColor());
            game.uiBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.b, this.craftReqColors.get(i).a);
            game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
            game.uiBatch.setColor(this.prevColor);
         }
      }

      if (this.disabled) {
         int extra = 0;
         if (this.crafts == game.player.regiCrafts || this.crafts == Player.kilnCrafts) {
            extra = 16;
         }

         this.arrowWhite.setPosition(this.newPos.x, this.newPos.y + extra);
         game.uiBatch.draw(this.arrowWhite, this.arrowWhite.getX(), this.arrowWhite.getY());
      } else {
         if (this.refresh) {
            this.refresh(game, index);
            this.refresh = false;
         }

         if (InputProcessor.upJustPressed) {
            if (this.cursorPos > 0) {
               this.cursorPos--;
               this.newPos = this.arrowCoords.get(this.cursorPos);
            } else if (this.currIndex > 0) {
               this.currIndex--;
            }

            lastCurrIndex = this.currIndex;
            lastCursorPos = this.cursorPos;
         } else if (InputProcessor.downJustPressed) {
            if (this.cursorPos < 2 && this.cursorPos + 1 < this.craftsList.size()) {
               this.cursorPos++;
               this.newPos = this.arrowCoords.get(this.cursorPos);
            } else if (this.currIndex < this.craftsList.size() - 3) {
               this.currIndex++;
            }

            lastCurrIndex = this.currIndex;
            lastCursorPos = this.cursorPos;
         }

         if (this.cursorDelay >= 5) {
            int extra = 0;
            if (this.crafts == game.player.regiCrafts || this.crafts == Player.kilnCrafts) {
               extra = 16;
            }

            this.arrow.setPosition(this.newPos.x, this.newPos.y + extra);
            game.uiBatch.draw(this.arrow, this.arrow.getX(), this.arrow.getY());
         } else {
            this.cursorDelay++;
         }

         if (this.craftsList.size() - this.currIndex > 4) {
            if (this.downArrowTimer < 22) {
               game.uiBatch.draw(this.downArrow, this.downArrow.getX(), this.downArrow.getY());
            }

            this.downArrowTimer++;
         } else {
            this.downArrowTimer = 0;
         }

         if (this.downArrowTimer > 41) {
            this.downArrowTimer = 0;
         }

         if (InputProcessor.aJustPressed) {
            game.actionStack.remove(this);
            game.insertAction(new PlayMusic("click1", null));
            String name = this.craftsList.get(this.currIndex + this.cursorPos);
            if ("Cancel".equals(name)) {
               lastCurrIndex = this.currIndex;
               lastCursorPos = this.cursorPos;
               if (this.prevMenu != null) {
                  this.prevMenu.disabled = false;
               }

               game.insertAction(new WaitFrames(game, 3, this.prevMenu));
               game.insertAction(new WaitFrames(game, 3, this.nextAction));
            } else {
               this.refresh = true;
               this.disabled = true;
               if (this.crafts != game.player.regiCrafts && this.crafts != game.player.fossilCrafts) {
                  game.insertAction(new DrawCraftsMenu.SelectAmount(this.crafts, this));
               } else {
                  DrawCraftsMenu.SelectAmount.amount = 1;
                  game.insertAction(new DrawCraftsMenu.Selected(this.crafts, this));
               }
            }
         } else if (InputProcessor.bJustPressed) {
            lastCurrIndex = this.currIndex;
            lastCursorPos = this.cursorPos;
            if (this.prevMenu != null) {
               this.prevMenu.disabled = false;
            }

            game.actionStack.remove(this);
            game.insertAction(this.prevMenu);
            game.insertAction(this.nextAction);
            game.insertAction(new PlayMusic("click1", null));
         }
      }
   }

   static class Intro extends Action {
      int length;
      public Action.Layer layer = Action.Layer.gui_110;
      Menu prevMenu;

      public Intro(Menu prevMenu, int length, Action nextAction) {
         super();
         this.prevMenu = prevMenu;
         this.nextAction = nextAction;
         this.length = length;
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

         this.length--;
         if (this.length <= 0) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   static class SelectAmount extends Menu {
      public Action.Layer layer = Action.Layer.gui_105;
      Sprite textbox;
      public static int amount = 0;
      int maxAmount = 1;
      ArrayList<Player.Craft> crafts;

      public SelectAmount(ArrayList<Player.Craft> crafts, Menu prevMenu) {
         this.crafts = crafts;
         this.prevMenu = prevMenu;
         Texture text = TextureCache.get(Gdx.files.internal("amount_bg1.png"));
         this.textbox = new Sprite(text, 0, 0, 160, 144);
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
         amount = 1;
      }

      @Override
      public void step(Game game) {
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         this.textbox.draw(game.uiBatch);
         if (!this.disabled) {
            if (InputProcessor.upJustPressed) {
               int index = DrawCraftsMenu.lastCurrIndex + DrawCraftsMenu.lastCursorPos;
               if (game.player.hasCraftRequirements(this.crafts, index, amount + 1)) {
                  amount++;
               }
            } else if (InputProcessor.downJustPressed) {
               if (amount > 1) {
                  amount--;
               }
            } else if (InputProcessor.rightJustPressed) {
               int index = DrawCraftsMenu.lastCurrIndex + DrawCraftsMenu.lastCursorPos;

               for (int i = 0; i < 10; i++) {
                  if (game.player.hasCraftRequirements(this.crafts, index, amount + 1)) {
                     amount++;
                  }
               }

               if (amount >= 99) {
                  amount = 99;
               }
            } else if (InputProcessor.leftJustPressed) {
               amount -= 10;
               if (amount <= 1) {
                  amount = 1;
               }
            }

            String word = "x" + String.format(Locale.ROOT, "%02d", amount);

            for (int i = 0; i < word.length(); i++) {
               char letter = word.charAt(i);
               Sprite letterSprite = game.textDict.get(letter);
               game.uiBatch.draw(letterSprite, 130 + 8 * i, 56.0F);
            }

            if (InputProcessor.aJustPressed) {
               this.disabled = true;
               game.actionStack.remove(this);
               game.insertAction(new DrawCraftsMenu.Selected(this.crafts, this));
            } else if (InputProcessor.bJustPressed) {
               game.insertAction(new PlayMusic("click1", null));
               game.actionStack.remove(this);
               this.prevMenu.disabled = false;
               game.insertAction(this.prevMenu);
               return;
            }
         }
      }
   }

   static class Selected extends Menu {
      Sprite arrow;
      Sprite arrowWhite;
      Sprite textBoxTop;
      Sprite textBoxMiddle;
      Sprite textBoxBottom;
      public Action.Layer layer = Action.Layer.gui_105;
      Map<Integer, Vector2> getCoords;
      int curr;
      Vector2 newPos;
      Sprite helperSprite;
      ArrayList<String> words;
      int textboxDelay = 0;
      ArrayList<Player.Craft> crafts;

      public Selected(ArrayList<Player.Craft> crafts, Menu prevMenu) {
         this.crafts = crafts;
         this.prevMenu = prevMenu;
         this.getCoords = new HashMap<>();
         this.words = new ArrayList<>();
         if (crafts == Game.staticGame.player.fossilCrafts) {
            this.words.add("CREATE");
         } else {
            this.words.add("CRAFT");
         }

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
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
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
                     this.prevMenu.disabled = false;
                     game.insertAction(this.prevMenu);
                     return;
                  }

                  if ("CRAFT".equals(word) || "CREATE".equals(word)) {
                     int index = DrawCraftsMenu.lastCurrIndex + DrawCraftsMenu.lastCursorPos;
                     if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.Craft(game.player.network.id, index, DrawCraftsMenu.SelectAmount.amount));
                     }

                     if (game.player.hasCraftRequirements(this.crafts, index, DrawCraftsMenu.SelectAmount.amount)) {
                        if (this.crafts == game.player.regiCrafts) {
                           Player.Craft craft = this.crafts.get(index);

                           for (Player.Craft req : craft.requirements) {
                              int newAmt = game.player.getItemAmount(req.name) - req.amount * DrawCraftsMenu.SelectAmount.amount;
                              game.player.setItemAmount(req.name, newAmt);
                              if (newAmt <= 0) {
                                 game.player.removeItem(req.name);
                              }
                           }

                           Player.Craft regiCraft = this.crafts.remove(index);
                           game.actionStack.remove(this);
                           game.actionStack.remove(this.prevMenu);
                           String dirFacing = game.player.dirFacing;
                           game.player.dirFacing = "up";
                           game.insertAction(
                              new WaitFrames(
                                 game,
                                 20,
                                 game.player.new Emote(
                                    "!",
                                    new WaitFrames(
                                       game,
                                       30,
                                       new SetField(
                                          game.player,
                                          "currSprite",
                                          game.player.standingSprites.get(game.player.dirFacing),
                                          new RegigigasIntroAnim(regiCraft.name, dirFacing, null)
                                       )
                                    )
                                 )
                              )
                           );
                           return;
                        }

                        if (this.crafts == game.player.fossilCrafts) {
                           Player.Craft craft = this.crafts.get(index);

                           for (Player.Craft req : craft.requirements) {
                              int newAmt = game.player.getItemAmount(req.name) - req.amount * DrawCraftsMenu.SelectAmount.amount;
                              game.player.setItemAmount(req.name, newAmt);
                              if (newAmt <= 0) {
                                 game.player.removeItem(req.name);
                              }
                           }

                           Player.Craft fossilCraft = this.crafts.get(index);
                           game.actionStack.remove(this);
                           game.actionStack.remove(this.prevMenu);
                           game.player.dirFacing = "right";
                           Action newAction = new WaitFrames(
                              game,
                              20,
                              game.player.new Emote(
                                 "!",
                                 new WaitFrames(game, 30, new SetField(game.player, "currSprite", game.player.standingSprites.get(game.player.dirFacing), null))
                              )
                           );
                           if (game.player.hmPokemon != null) {
                              newAction.append(new SetField(game.player.hmPokemon, "dirFacing", "right", null));
                           }

                           game.insertAction(new WaitFrames(game, 20, new PlayMusic("computer1", 0.5F, false, null)));
                           game.insertAction(new FossilMachinePowerUp.DoRevive(fossilCraft.name, null));
                           game.insertAction(newAction);
                           return;
                        }

                        game.player.craftItem(this.crafts, index, DrawCraftsMenu.SelectAmount.amount);
                        game.actionStack.remove(this);
                        game.insertAction(this.prevMenu.prevMenu);
                        String plural = "";
                        if (DrawCraftsMenu.SelectAmount.amount > 1 && !this.crafts.get(index).name.toLowerCase(Locale.ROOT).endsWith("s")) {
                           plural = plural + "S";
                        }

                        game.insertAction(
                           new DisplayText(
                              game,
                              "Crafted "
                                 + String.valueOf(DrawCraftsMenu.SelectAmount.amount)
                                 + " "
                                 + this.crafts.get(index).name.toUpperCase(Locale.ROOT)
                                 + plural
                                 + "!",
                              "fanfare1.ogg",
                              true,
                              true,
                              new DisplayText.Clear(game, new SetField(this.prevMenu.prevMenu, "disabled", false, null))
                           )
                        );
                     } else {
                        this.disabled = true;
                        game.insertAction(new PlayMusic("error1", new SetField(this, "disabled", false, null)));
                     }
                  }
               } else if (InputProcessor.bJustPressed) {
                  game.insertAction(new PlayMusic("click1", null));
                  game.actionStack.remove(this);
                  this.prevMenu.disabled = false;
                  game.insertAction(this.prevMenu);
                  return;
               }
            }
         }
      }
   }
}
