package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Server;
import com.pkmngen.game.util.LinkedMusic;
import com.pkmngen.game.util.StackTraces;
import com.pkmngen.game.util.TextField;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

class DrawSetupMenu extends Action {
   public static boolean drawChoosePokemonText = true;
   public static int avatarAnimCounter = 24;
   public static int currIndex = 0;
   public static int lastIndex = 0;
   public Action.Layer layer = Action.Layer.gui_5000;
   Sprite bgSprite;
   Sprite helperSprite;
   Sprite arrow;
   Sprite arrowFlipped;
   Sprite arrowDown;
   Sprite arrowDownFlipped;
   Sprite selectColorBox;
   Sprite arrowWhite;
   ArrayList<Sprite> avatarSprites = new ArrayList<>();
   int avatarColorIndex = 0;
   int playerColorIndex = 0;
   int playerTypeIndex = 0;
   ArrayList<String> playerTypes = new ArrayList<>();
   int localHostJoinIndex;
   int newLoadIndex;
   int sizeIndex;
   int fileIndex;
   int shinyRateIndex;
   int shinyRatePow;
   int offset;
   int offset2;
   int offset3;
   Vector2 newPos;
   Map<Integer, Vector2> arrowCoords;
   ArrayList<Character> name;
   ArrayList<Character> mapName;
   ArrayList<Character> serverIp;
   HashMap<Integer, Character> numberKeys;
   ArrayList<Color> colors;
   ArrayList<String> fileNames;
   private DrawSetupMenu.TextFieldSelected fieldSelected;

   public DrawSetupMenu(Game game, Action nextAction) {
      super(game, nextAction);
      this.playerTypes.add("leaf");
      this.playerTypes.add("gold");
      this.playerTypes.add("kris");
      this.playerTypes.add("brendan");
      this.playerTypes.add("may");
      this.playerTypes.add("hilbert");
      this.playerTypes.add("hilda");
      this.playerTypes.add("rosa");
      this.playerTypes.add("calem");
      this.playerTypes.add("serena");
      this.playerTypes.add("chase");
      this.playerTypes.add("elaine");
      this.playerTypes.add("gloria");
      this.playerTypes.add("mark");
      this.playerTypes.add("mint");
      this.playerTypes.add("lunick");
      this.playerTypes.add("summer");
      this.playerTypes.add("kellyn");
      this.localHostJoinIndex = 0;
      this.newLoadIndex = 0;
      this.sizeIndex = 0;
      this.fileIndex = 0;
      this.shinyRateIndex = 0;
      this.shinyRatePow = 8;
      this.offset = 0;
      this.offset2 = 0;
      this.offset3 = 0;
      this.name = new ArrayList<>(11);
      this.mapName = new ArrayList<>(11);
      this.serverIp = new ArrayList<>();
      this.numberKeys = new HashMap<>();
      this.colors = new ArrayList<>();
      this.fileNames = new ArrayList<>();
      this.fieldSelected = DrawSetupMenu.TextFieldSelected.NONE;
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      this.arrowFlipped = new Sprite(this.arrow);
      this.arrowFlipped.flip(true, false);
      this.avatarSprites.add(game.player.standingSprites.get("down"));
      this.avatarSprites.add(game.player.movingSprites.get("down"));
      this.avatarSprites.add(game.player.altMovingSprites.get("down"));
      currIndex = lastIndex;
      text = TextureCache.get(Gdx.files.internal("arrow_down.png"));
      this.arrowDown = new Sprite(text, 0, 0, 7, 5);
      this.arrowDownFlipped = new Sprite(this.arrowDown);
      this.arrowDownFlipped.flip(false, true);
      text = TextureCache.get(Gdx.files.internal("select_color1.png"));
      this.selectColorBox = new Sprite(text, 0, 0, 16, 16);
      this.arrowCoords = new HashMap<>();

      for (int i = 0; i < 8; i++) {
         this.arrowCoords.put(i, new Vector2(1.0F, 128 - 16 * i));
      }

      this.newPos = this.arrowCoords.get(currIndex);
      text = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
      this.bgSprite = new Sprite(text, 0, 0, 176, 160);
      this.bgSprite.setPosition(-8.0F, -8.0F);
      char[] textArray = "1234567890".toCharArray();

      for (int i = 0; i < textArray.length; i++) {
         this.numberKeys.put(Keys.valueOf(String.valueOf(textArray[i])), textArray[i]);
      }

      this.numberKeys.put(56, '.');
      this.numberKeys.put(Keys.SEMICOLON, ':');
      this.colors.add(new Color(0.972549F, 0.21960784F, 0.03137255F, 1.0F));
      this.colors.add(new Color(0.9411765F, 0.3137255F, 0.1882353F, 1.0F));
      this.colors.add(new Color(0.3137255F, 0.28235295F, 0.972549F, 1.0F));
      this.colors.add(Color.CYAN);
      this.colors.add(new Color(0.21960784F, 0.72156864F, 0.09411765F, 1.0F));
      this.colors.add(Color.MAGENTA);
      this.colors.add(Color.MAROON);
      this.colors.add(Color.YELLOW);
      this.colors.add(new Color(0.47058824F, 0.3137255F, 0.09411765F, 1.0F));
      this.colors.add(Color.OLIVE);
      this.colors.add(Color.TEAL);
      this.colors.add(Color.RED);
      this.colors.add(Color.PURPLE);
      this.colors.add(new Color(1.0F, 0.4509804F, 0.78431374F, 1.0F));
      FileHandle directory = Gdx.files.local("");
      FileHandle[] files = directory.list();

      for (FileHandle file : files) {
         String filename = file.name();
         if (filename.endsWith(".sav")) {
            this.fileNames.add(filename);
         }
      }

      directory = Gdx.files.local("mods/player/");
      files = directory.list();

      for (FileHandle file : files) {
         if (file.isDirectory()) {
            String filename = file.name();
            if (!this.playerTypes.contains(filename)) {
               this.playerTypes.add(filename);
               System.out.println("Found mods for: " + filename);
            }
         }
      }
      this.playerTypeIndex = Math.max(0, this.playerTypes.indexOf(game.player.character));
      for (int i = 0; i < this.colors.size(); i++) if (Color.rgba8888(this.colors.get(i)) == Color.rgba8888(game.player.color)) this.avatarColorIndex = i;
      if (game.playerProfile != null) for (char c : game.playerProfile.name.toCharArray()) this.name.add(c);
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
      if (Game.musicDisabled) {
         game.currMusic = AudioLoader.loadMusic("music/silence1.ogg");
         game.currMusic.setVolume(0.8F);
      } else {
         game.currMusic = new LinkedMusic("music/follow_me1_intro", "music/follow_me1");
         game.currMusic.setVolume(0.8F);
      }

      game.currMusic.play();
      game.player.setColor(game.playerProfile == null ? this.colors.get(this.avatarColorIndex) : game.player.color);
      this.avatarSprites.clear();
      this.avatarSprites.add(game.player.standingSprites.get("down"));
      this.avatarSprites.add(game.player.movingSprites.get("down"));
      this.avatarSprites.add(game.player.altMovingSprites.get("down"));
      bindInput();
   }

   void bindInput() {
      Gdx.input.setInputProcessor(new InputAdapter() {
         @Override
         public boolean keyTyped(char character) {
            switch (DrawSetupMenu.this.fieldSelected) {
               case NAME:
                  return DrawSetupMenu.this.updateField(character, DrawSetupMenu.this.name);
               case MAP_NAME:
                  return DrawSetupMenu.this.updateField(character, DrawSetupMenu.this.mapName);
               default:
                  return false;
            }
         }
      });
   }

   private void openServers(Game game) {
      StringBuilder enteredName = new StringBuilder();
      for (Character character : this.name) enteredName.append(character);
      this.fieldSelected = TextFieldSelected.NONE;
      game.actionStack.remove(this);
      game.insertAction(new TitleMenu(game, this, enteredName.toString()));
      InputProcessor.aJustPressed = false;
      InputProcessor.startJustPressed = false;
   }

   private boolean updateField(char character, ArrayList<Character> characters) {
      return TextField.updateField(character, characters, characters == this.name ? 24 : 11, false);
   }

   @Override
   public void step(final Game game) {
      StringBuilder profileName = new StringBuilder();
      for (char c : this.name) profileName.append(c);
      game.rememberPlayerProfile(profileName.toString());
      if (game.player.currFieldMove.isEmpty()) game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      if (currIndex == 0) {
         if (InputProcessor.leftJustPressed && this.localHostJoinIndex > 0) this.localHostJoinIndex--;
         if (InputProcessor.rightJustPressed && this.localHostJoinIndex < 2) this.localHostJoinIndex++;
         if (this.localHostJoinIndex == 2 && InputProcessor.menuConfirmJustPressed()) {
            openServers(game);
            return;
         }
      }
      this.newPos = this.arrowCoords.get(currIndex);
      this.fieldSelected = DrawSetupMenu.TextFieldSelected.NONE;

      for (int j = 0; j < 8; j++) {
         if (j == 0) {
            char[] textArray = "LOCAL  HOST   JOIN".toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               Sprite letterSprite = game.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
            }

            int offsetX = 0;
            this.offset = 0;
            if (this.localHostJoinIndex == 1) {
               offsetX = 56;
            }

            if (this.localHostJoinIndex == 2) {
               offsetX = 112;
               this.offset = -1;
               this.offset2 = 0;
               this.newLoadIndex = 0;
            }

            if (this.localHostJoinIndex == 1 && this.newLoadIndex == 0) {
               this.offset3 = -2;
            } else {
               this.offset3 = this.localHostJoinIndex == 2 ? -1 : 0;
            }

            if (j == currIndex) {
               this.newPos = this.newPos.cpy().add(offsetX, 0.0F);
            }

            game.uiBatch.draw(this.arrowWhite, this.arrowCoords.get(j).x + offsetX, this.arrowCoords.get(j).y);
         } else if (j == 1 + this.offset) {
            char[] textArray = "NEW    LOAD".toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               Sprite letterSprite = game.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
            }

            int offsetX = 0;
            if (this.newLoadIndex == 1) {
               offsetX = 56;
               this.offset2 = -3;
            } else {
               this.offset2 = 0;
            }

            if (j == currIndex) {
               if (InputProcessor.leftJustPressed && this.newLoadIndex > 0) {
                  this.newLoadIndex--;
               }

               if (InputProcessor.rightJustPressed && this.newLoadIndex < 1) {
                  this.newLoadIndex++;
               }

               this.newPos = this.newPos.cpy().add(offsetX, 0.0F);
            }

            game.uiBatch.draw(this.arrowWhite, this.arrowCoords.get(j).x + offsetX, this.arrowCoords.get(j).y);
         } else if (j == 2 + this.offset + this.offset2) {
            if (this.localHostJoinIndex == 2) {
               if (j == currIndex && InputProcessor.menuConfirmJustPressed()) {
                  openServers(game);
                  return;
               }
               char[] textArray = "Servers".toCharArray();
               for (int i = 0; i < textArray.length; i++) {
                  game.uiBatch.draw(game.textDict.get(textArray[i]), 8 + 8 * i, 128 - 16 * j);
               }
            } else {
               char[] textArray = "File".toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.textDict.get(textArray[i]);
                  game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
               }

               if (j == currIndex + this.offset) {
                  this.fieldSelected = DrawSetupMenu.TextFieldSelected.MAP_NAME;
                  if (avatarAnimCounter >= 12) {
                     Sprite letterSprite = game.textDict.get('_');
                     game.uiBatch.draw(letterSprite, 48 + 8 * this.mapName.size(), 128 - 16 * j);
                  }
               } else if (this.mapName.size() <= 0) {
                  textArray = "default".toCharArray();

                  for (int i = 0; i < textArray.length; i++) {
                     Sprite letterSprite = game.textDict.get(textArray[i]);
                     game.uiBatch.draw(letterSprite, 48 + 8 * i, 128 - 16 * j);
                  }
               }

               for (int i = 0; i < this.mapName.size(); i++) {
                  Sprite letterSprite = game.textDict.get(this.mapName.get(i));
                  game.uiBatch.draw(letterSprite, 48 + 8 * i, 128 - 16 * j);
               }
            }
         } else if (j == 3 + this.offset + this.offset2 && this.localHostJoinIndex != 2) {
            char[] textArray = "World S M L XL XXL".toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               Sprite letterSprite = game.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
            }

            int offsetX = 0;
            if (this.sizeIndex == 0) {
               offsetX = 0;
            } else if (this.sizeIndex < 5) {
               offsetX = 32 + 16 * this.sizeIndex;
            } else {
               offsetX = 40 + 16 * this.sizeIndex;
            }

            if (j == currIndex + this.offset) {
               if (InputProcessor.leftJustPressed && this.sizeIndex > 0) {
                  this.sizeIndex--;
               }

               if (InputProcessor.rightJustPressed && this.sizeIndex < 5) {
                  this.sizeIndex++;
               }

               this.newPos = this.newPos.cpy().add(offsetX, 0.0F);
            }

            game.uiBatch.draw(this.arrowWhite, this.arrowCoords.get(j).x + offsetX, this.arrowCoords.get(j).y);
         } else if (j == 4 + this.offset + this.offset2 + this.offset3) {
            char[] textArray = "Name".toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               Sprite letterSprite = game.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
            }

            if (j == currIndex) {
               this.fieldSelected = DrawSetupMenu.TextFieldSelected.NAME;
               if (avatarAnimCounter >= 12) {
                  Sprite letterSprite = game.textDict.get('_');
                  game.uiBatch.draw(letterSprite, 48 + 8 * Math.min(11, this.name.size()), 128 - 16 * j);
               }
            }

            for (int i = Math.max(0, this.name.size() - 11); i < this.name.size(); i++) {
               Sprite letterSprite = game.textDict.get(this.name.get(i));
               game.uiBatch.draw(letterSprite, 48 + 8 * (i - Math.max(0, this.name.size() - 11)), 128 - 16 * j);
            }
         } else if (j == 5 + this.offset + this.offset2 + this.offset3 && this.newLoadIndex != 1) {
            Sprite avatarSprite;
            if (j == currIndex) {
               if (avatarAnimCounter >= 18) {
                  avatarSprite = new Sprite(this.avatarSprites.get(0));
               } else if (avatarAnimCounter >= 12) {
                  avatarSprite = new Sprite(this.avatarSprites.get(1));
               } else if (avatarAnimCounter >= 6) {
                  avatarSprite = new Sprite(this.avatarSprites.get(0));
               } else {
                  avatarSprite = new Sprite(this.avatarSprites.get(2));
               }

               if (InputProcessor.leftJustPressed) {
                  this.playerColorIndex--;
                  if (this.playerColorIndex < 0) {
                     this.playerColorIndex = 0;
                  }
               } else if (InputProcessor.downJustPressed) {
                  if (this.playerColorIndex == 1) {
                     this.avatarColorIndex--;
                     if (this.avatarColorIndex < 0) {
                        this.avatarColorIndex = this.colors.size() - 1;
                     }

                     game.player.setColor(this.colors.get(this.avatarColorIndex));
                     this.avatarSprites.clear();
                     this.avatarSprites.add(game.player.standingSprites.get("down"));
                     this.avatarSprites.add(game.player.movingSprites.get("down"));
                     this.avatarSprites.add(game.player.altMovingSprites.get("down"));
                  } else if (this.playerColorIndex == 2) {
                     this.playerTypeIndex--;
                     if (this.playerTypeIndex < 0) {
                        this.playerTypeIndex = this.playerTypes.size() - 1;
                     }

                     String name = "";

                     for (int i = 0; i < this.name.size(); i++) {
                        name = name + this.name.get(i).toString();
                     }

                     if (name.equals("") || name.equalsIgnoreCase(game.player.character)) {
                        this.name.clear();
                        String newName = this.playerTypes.get(this.playerTypeIndex).toUpperCase(Locale.ROOT);

                        for (int i = 0; i < newName.length(); i++) {
                           this.name.add(newName.charAt(i));
                        }
                     }

                     game.player.character = this.playerTypes.get(this.playerTypeIndex);
                     game.player.setColor(this.colors.get(this.avatarColorIndex));
                     this.avatarSprites.clear();
                     this.avatarSprites.add(game.player.standingSprites.get("down"));
                     this.avatarSprites.add(game.player.movingSprites.get("down"));
                     this.avatarSprites.add(game.player.altMovingSprites.get("down"));
                  }
               } else if (InputProcessor.rightJustPressed) {
                  this.playerColorIndex++;
                  if (this.playerColorIndex > 2) {
                     this.playerColorIndex = 2;
                  }
               } else if (InputProcessor.upJustPressed) {
                  if (this.playerColorIndex == 1) {
                     this.avatarColorIndex++;
                     if (this.avatarColorIndex >= this.colors.size()) {
                        this.avatarColorIndex = 0;
                     }

                     game.player.setColor(this.colors.get(this.avatarColorIndex));
                     this.avatarSprites.clear();
                     this.avatarSprites.add(game.player.standingSprites.get("down"));
                     this.avatarSprites.add(game.player.movingSprites.get("down"));
                     this.avatarSprites.add(game.player.altMovingSprites.get("down"));
                  } else if (this.playerColorIndex == 2) {
                     this.playerTypeIndex++;
                     if (this.playerTypeIndex >= this.playerTypes.size()) {
                        this.playerTypeIndex = 0;
                     }

                     String name = "";

                     for (int i = 0; i < this.name.size(); i++) {
                        name = name + this.name.get(i).toString();
                     }

                     if (name.equals("") || name.equalsIgnoreCase(game.player.character)) {
                        this.name.clear();
                        String newName = this.playerTypes.get(this.playerTypeIndex).toUpperCase(Locale.ROOT);

                        for (int i = 0; i < newName.length(); i++) {
                           this.name.add(newName.charAt(i));
                        }
                     }

                     game.player.character = this.playerTypes.get(this.playerTypeIndex);
                     game.player.setColor(this.colors.get(this.avatarColorIndex));
                     this.avatarSprites.clear();
                     this.avatarSprites.add(game.player.standingSprites.get("down"));
                     this.avatarSprites.add(game.player.movingSprites.get("down"));
                     this.avatarSprites.add(game.player.altMovingSprites.get("down"));
                  }
               }
            } else {
               avatarSprite = new Sprite(this.avatarSprites.get(0));
            }

            if (this.playerColorIndex == 1) {
               game.uiBatch.draw(this.arrowDown, 58.0F, 124 - 16 * j);
               game.uiBatch.draw(this.arrowDownFlipped, 58.0F, 132 - 16 * j);
               this.newPos = this.newPos.cpy().add(16 + 32 * this.playerColorIndex, 0.0F);
            }

            if (this.playerColorIndex == 2) {
               game.uiBatch.draw(this.arrowDown, 88.0F, 124 - 16 * j);
               game.uiBatch.draw(this.arrowDownFlipped, 88.0F, 132 - 16 * j);
               this.newPos = this.newPos.cpy().add(16 + 32 * this.playerColorIndex, 0.0F);
            }

            this.selectColorBox.setColor(game.player.color);
            this.selectColorBox.setPosition(66.0F, 123 - 16 * j);
            this.selectColorBox.draw(game.uiBatch);
            game.uiBatch.draw(avatarSprite, 96.0F, 124 - 16 * j);
            char[] textArray = "Player".toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               Sprite letterSprite = game.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
            }
         } else if (j == 5 + this.offset + this.offset2 + this.offset3) {
            if (!this.fileNames.isEmpty()) {
               char[] textArray = this.fileNames.get(this.fileIndex).toCharArray();

               for (int i = 0; i < textArray.length; i++) {
                  Sprite letterSprite = game.textDict.get(textArray[i]);
                  game.uiBatch.draw(letterSprite, 32 + 8 * i, 124 - 16 * (j + this.offset));
               }

               game.uiBatch.draw(this.arrowFlipped, 16.0F, 124 - 16 * (j + this.offset));
               game.uiBatch.draw(this.arrow, 43 + 8 * this.fileNames.get(this.fileIndex).length(), 124 - 16 * j);
            }

            if (j == currIndex) {
               if (InputProcessor.leftJustPressed) {
                  this.fileIndex--;
                  if (this.fileIndex < 0) {
                     this.fileIndex = this.fileNames.size() - 1;
                  }
               }

               if (InputProcessor.rightJustPressed) {
                  this.fileIndex++;
                  if (this.fileIndex > this.fileNames.size() - 1) {
                     this.fileIndex = 0;
                  }
               }
            }
         } else if (j == 6 + this.offset + this.offset2 + this.offset3) {
            char[] textArray = "Go!".toCharArray();

            for (int i = 0; i < textArray.length; i++) {
               Sprite letterSprite = game.textDict.get(textArray[i]);
               game.uiBatch.draw(letterSprite, 8 + 8 * i, 128 - 16 * j);
            }

            if (j == currIndex && InputProcessor.menuConfirmJustPressed()) {
               if (this.localHostJoinIndex == 2) {
                  openServers(game);
                  return;
               }
               Gdx.input.setInputProcessor(null);
               String mapName = "";
               if (this.newLoadIndex == 0) {
                  for (int i = 0; i < this.mapName.size(); i++) {
                     mapName = mapName + this.mapName.get(i).toString();
                  }

                  if (mapName.equals("")) {
                     mapName = "default";
                  }
               } else {
                  mapName = this.fileNames.get(this.fileIndex).split("\\.sav")[0];
               }

               if (!game.debugInputEnabled
                  && this.newLoadIndex == 0
                  && this.fileNames.contains(mapName + ".sav")
                  && JOptionPane.showConfirmDialog(null, "A save file with the same name already exists - overwrite?", "WARNING", 0) == 1) {
                  return;
               }

               Action fadeMusic = new FadeMusic(
                  game.currMusic,
                  -0.0125F,
                  new CallMethod(game.currMusic, "stop", new Object[0], new CallMethod(game.currMusic, "dispose", new Object[0], null))
               );
               game.insertAction(fadeMusic);
               if (this.localHostJoinIndex != 2) {
                  Action hostAction = null;
                  if (this.localHostJoinIndex == 1) {
                     try {
                        game.initServer();
                     } catch (IOException e) {
                        e.printStackTrace();
                     }

                     game.insertAction(new ServerBroadcast(game));
                     game.debugInputEnabled = true;
                     hostAction = new DisplayText(game, "Welcome to host mode!           WASD moves camera     Q and E zooms", null, null, null);
                  } else {
                     game.server = new Server();
                     Network.register(game.server);
                  }

                  final Action hostActionFinal = hostAction;
                  final Action drawControls = new DrawControls();
                  game.map = new PkmnMap(mapName);
                  if (this.newLoadIndex != 0) {
                     game.actionStack.remove(this);
                     game.start();

                     try {
                        game.map.loadFromFile(game);
                        if (game.playerProfile != null) game.playerProfile.apply(game.player);
                        InputProcessor.aJustPressed = false;
                     } catch (Exception e) {
                        e.printStackTrace();
                        JFrame frame = new JFrame("Error");
                        StringWriter traceback = new StringWriter();
                        StackTraces.writeStackTrace(e, new PrintWriter(traceback));
                        JOptionPane.showMessageDialog(frame, "There was an error loading this file:\n\n" + traceback.toString());
                        System.exit(0);
                     }

                     EnterBuilding enterBuilding = new EnterBuilding(game, "", null);
                     enterBuilding.slow = 8;
                     game.insertAction(enterBuilding);
                     if (game.debugInputEnabled) {
                        game.insertAction(new TileEditor());
                     }
                  } else {
                     game.actionStack.remove(this);
                     if (this.sizeIndex == 0) {
                        if (!game.debugInputEnabled) {
                           this.sizeIndex = 1;
                        } else {
                           this.sizeIndex = -1;
                        }
                     }

                     int something = 100;
                     if (this.sizeIndex > 4) {
                        something = 80;
                     } else if (this.sizeIndex > 3) {
                        something = 90;
                     }

                     final int size = 100 * something * (this.sizeIndex + 2);
                     Thread thread = new Thread(
                        new Runnable() {
                           @Override
                           public void run() {
                              try {
                                 System.out.println("Generating map...");
                                 System.out.println(LocalTime.now());
                                 final Action genIsland = new GenIsland1(Game.staticGame, new Vector2(0.0F, 0.0F), size);
                                 System.out.println("Done.");
                                 System.out.println(LocalTime.now());
                                 Thread.sleep(4000L);
                                 Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                       Game.staticGame.start();
                                       Game.staticGame.insertAction(genIsland);
                                       genIsland.step(Game.staticGame);
                                       EnterBuilding enterBuilding = new EnterBuilding(Game.staticGame, "", null);
                                       enterBuilding.slow = 8;
                                       Game.staticGame.insertAction(enterBuilding);
                                       Game.staticGame
                                          .insertAction(new DisplayText.Clear(Game.staticGame, new SetField(drawControls, "remove", true, hostActionFinal)));
                                       int width = (int)(Game.staticGame.map.topRight.x - Game.staticGame.map.bottomLeft.x) / 8;
                                       int height = (int)(Game.staticGame.map.topRight.y - Game.staticGame.map.bottomLeft.y) / 8;
                                       Game.staticGame.map.minimap = new Pixmap(width, height, Format.RGBA8888);
                                       Game.staticGame.map.minimap.setColor(0.0F, 0.0F, 0.0F, 1.0F);
                                       Game.staticGame.map.minimap.fill();
                                       Vector2 startPos = game.player.position.cpy().add(-128.0F, -128.0F);
                                       startPos.x = (int)startPos.x - (int)startPos.x % 16;
                                       startPos.y = (int)startPos.y - (int)startPos.y % 16;
                                       Vector2 endPos = game.player.position.cpy().add(128.0F, 128.0F);
                                       endPos.x = (int)endPos.x - (int)endPos.x % 16;
                                       endPos.y = (int)endPos.y - (int)endPos.y % 16;
                                       Vector2 currPos = new Vector2(startPos.x, startPos.y);

                                       while (currPos.y < endPos.y) {
                                          Tile tile = Game.staticGame.map.tiles.get(currPos);
                                          currPos.x += 16.0F;
                                          if (currPos.x > endPos.x) {
                                             currPos.x = startPos.x;
                                             currPos.y += 16.0F;
                                          }

                                          if (tile != null) {
                                             tile.updateMiniMap(game);
                                          }
                                       }
                                    }
                                 };
                                 Gdx.app.postRunnable(runnable);
                              } catch (Exception e) {
                                 e.printStackTrace();
                              }
                           }
                        }
                     );
                     thread.setPriority(1);
                     thread.start();
                     game.insertAction(new DisplayText(game, "Generating... please wait...", null, true, false, null));
                     game.insertAction(drawControls);
                     String name = "";

                     for (int i = 0; i < this.name.size(); i++) {
                        name = name + this.name.get(i).toString();
                     }

                     name = name.trim().isEmpty() ? "player" : name.trim();
                     game.player.name = name;
                  }
               } else {
                  String name = "";

                  for (int i = 0; i < this.name.size(); i++) {
                     name = name + this.name.get(i).toString();
                  }

                  name = name.trim().isEmpty() ? "player" : name.trim();
                  game.player.name = name;
                  String ipAddr = "";

                  for (int i = 0; i < this.serverIp.size(); i++) {
                     ipAddr = ipAddr + this.serverIp.get(i).toString();
                  }

                  if (ipAddr.equals("")) {
                     ipAddr = "127.0.0.1";
                  }

                  try {
                     game.joinCoop(ipAddr, name);
                  } catch (IllegalArgumentException invalid) {
                     game.insertAction(new DisplayText(game, invalid.getMessage(), null, false, null));
                  }

               }
            }
         }
      }

      avatarAnimCounter--;
      if (avatarAnimCounter <= 0) {
         avatarAnimCounter = 24;
      }

      int max = 6;
      if (this.localHostJoinIndex == 2) {
         max = 4;
      }

      if (this.localHostJoinIndex == 1) {
         max = 4;
      }

      if (this.newLoadIndex == 1) {
         max = 3;
      }

      if (this.playerColorIndex == 0) {
         if (InputProcessor.upJustPressed) {
            if (currIndex > 0) {
               currIndex--;

               avatarAnimCounter = 24;
            } else {
               currIndex = max;
            }
         } else if (InputProcessor.downJustPressed) {
            if (currIndex < max) {
               currIndex++;

               avatarAnimCounter = 24;
            } else {
               currIndex = 0;
            }
         }

         this.arrow.setPosition(this.newPos.x, this.newPos.y);
         this.arrow.draw(game.uiBatch);
      }
   }

   private enum TextFieldSelected {
      NONE,
      MAP_NAME,
      NAME;
   }
}
