package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

class SpecialBattleMewtwo extends Action {
   public Action.Layer layer = Action.Layer.gui_106;
   Music music;
   SpecialMewtwo1 mewtwo;
   boolean firstStep = true;
   public static boolean doneYet = false;
   public static int specialAttackCounter = 0;
   int timer = 0;

   public SpecialBattleMewtwo(Game game, SpecialMewtwo1 mewtwo) {
      super();
      this.mewtwo = mewtwo;
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
      if (this.firstStep) {
         if (!Game.musicDisabled) {
            game.currMusic.pause();
            String musicName = "music/pokemon_mansion_remix_eq";
            if (!game.loadedMusic.containsKey(musicName)) {
               Music music = AudioLoader.loadMusic(musicName + ".ogg");
               music.setLooping(true);
               game.loadedMusic.put(musicName, music);
            }

            this.music = game.loadedMusic.get(musicName);
            this.music.setVolume(0.7F);
            game.currMusic = this.music;
            game.currMusic.play();
         }

         if (doneYet) {
            this.timer = 1708;
            game.currMusic.setPosition(28.55F);
         }

         doneYet = true;
         specialAttackCounter = 0;
         this.firstStep = false;
      }

      if (this.timer != 0) {
         if (this.timer == 100) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "Humans...", null, null, false, null));
         } else if (this.timer == 280) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "They cared nothing for me...", null, null, false, null));
         } else if (this.timer == 560) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "From the moment I first opened my eyes, they have sought to control me...", null, null, false, null));
         } else if (this.timer == 960) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "But no more.", null, null, false, null));
         } else if (this.timer == 1160) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "Why are you here?", null, null, false, null));
         } else if (this.timer == 1310) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "You seek to control me, just like the others.", null, null, false, null));
         } else if (this.timer == 1580) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "...", null, null, false, null));
         }
      }

      if (this.timer >= 1708) {
         game.actionStack.remove(game.displayTextAction);
         game.displayTextAction = null;
         Action afterTrigger = new WaitFrames(game, 15, new DrawBattleMenuNormal(game, null));
         Action triggerAction = new PlaySound(
            game.player.currPokemon,
            new WaitFrames(game, game.player.currPokemon.specie.cryLengthInFrames(), new WaitFrames(game, 6, new DrawFriendlyHealthGen2(game, afterTrigger)))
         );
         Action nextAction = new BattleIntro(
            new SpecialBattleMewtwo.BattleIntro1(
               new SplitAction(
                  new SpecialBattleMewtwo.DrawBattle1(game),
                  new SplitAction(
                     new SpecialBattleMewtwo.DrawBreathingSprite(this.mewtwo),
                     new SplitAction(
                        new SpecialBattleMewtwo.RocksEffect2(),
                        new SpecialBattleMewtwo.IntroAnim(
                           game, new SplitAction(new SpecialBattleMewtwo.RocksEffect1(), new SplitAction(new SpecialBattleMewtwo.RippleEffect1(), null))
                        )
                     )
                  )
               )
            )
         );
         if (this.mewtwo.isShiny) {
            nextAction.append(new Battle.LoadAndPlayAnimation(game, "shiny", game.player.currPokemon, null));
         }

         nextAction.append(
            new PlaySound(
               "150",
               0.8F,
               new WaitFrames(
                  game,
                  game.battle.oppPokemon.specie.cryLengthInFrames(),
                  new DisplayText(
                     game,
                     "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " appeared!",
                     null,
                     null,
                     new SplitAction(
                        new WaitFrames(game, 1, new DrawEnemyHealthGen2(game)),
                        new WaitFrames(
                           game,
                           39,
                           new MovePlayerOffScreen(
                              game,
                              new DisplayText(
                                 game,
                                 "Go! " + game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + "!",
                                 null,
                                 triggerAction,
                                 new ThrowOutPokemonCrystal(
                                    game,
                                    game.player.currPokemon.isShiny
                                       ? new Battle.LoadAndPlayAnimation(game, "shiny", game.battle.oppPokemon, triggerAction)
                                       : triggerAction
                                 )
                              )
                           )
                        )
                     )
                  )
               )
            )
         );
         game.actionStack.remove(this);
         game.insertAction(nextAction);
      }

      this.timer++;
   }

   class BattleIntro1 extends Action {
      ArrayList<Sprite> frames;
      Sprite frame;
      public Action.Layer layer = Action.Layer.gui_139;

      public BattleIntro1(Action nextAction) {
         super();
         this.nextAction = nextAction;
         this.frames = new ArrayList<>();
         Texture text1 = TextureCache.get(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));

         for (int i = 0; i < 28; i++) {
            this.frames.add(new Sprite(text1, i * 160, 0, 160, 144));
         }

         for (int i = 0; i < 126; i++) {
            this.frames.add(new Sprite(text1, 4320, 0, 160, 144));
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
         if (this.frames.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            this.nextAction.step(game);
         } else {
            this.frame = this.frames.get(0);
            if (this.frame != null) {
               this.frame.draw(game.uiBatch);
            }

            this.frames.remove(0);
         }
      }
   }

   class DrawBattle1 extends DrawBattle {
      public Action.Layer layer = Action.Layer.gui_130;

      public DrawBattle1(Game game) {
         super(game);
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
         if (shouldDrawOppPokemon) {
            game.battle.oppPokemon.sprite.draw(game.uiBatch);
         }

         game.player.battleSprite.draw(game.uiBatch);
      }
   }

   static class DrawBreathingSprite extends Action {
      static boolean shouldBreathe = false;
      public Action.Layer layer = Action.Layer.gui_129;
      Sprite bgSprite2;
      SpecialMewtwo1 mewtwo;
      int timer = 300;
      int offsetY = 0;

      public DrawBreathingSprite(SpecialMewtwo1 mewtwo) {
         super();
         this.mewtwo = mewtwo;
         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
         this.bgSprite2 = new Sprite(text, 0, 0, 160, 144);
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
         if (shouldBreathe) {
            this.timer--;
            if (this.timer == 149) {
               this.offsetY = -1;
            } else if (this.timer == 0) {
               this.timer = 300;
               this.offsetY = 0;
            }
         }

         if (DrawBattle.shouldDrawOppPokemon) {
            this.mewtwo.breathingSprite.setPosition(this.mewtwo.sprite.getX(), this.mewtwo.sprite.getY() + this.offsetY);
            this.mewtwo.breathingSprite.draw(game.uiBatch);
         }

         for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
               if (i != 0 || j != 0) {
                  game.uiBatch.draw(this.bgSprite2, 160 * i, 144 * j);
               }
            }
         }

         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      }
   }

   static class IntroAnim extends Action {
      ArrayList<Vector2> moves_relative;
      Vector2 move;
      public Action.Layer layer = Action.Layer.gui_140;
      int timer = 0;

      public IntroAnim(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
         this.moves_relative = new ArrayList<>();

         for (int i = 0; i < 144; i++) {
            this.moves_relative.add(new Vector2(1.0F, 0.0F));
         }

         game.player.battleSprite.setPosition(162.0F, 49.0F);
         game.battle.oppPokemon.sprite.setPosition(-49.0F, 88.0F);
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      String getShader(float level) {
         return "#ifdef GL_ES\n    precision mediump float;\n#endif\n\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\n\nvoid main() {\n    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n    float level = "
            + level
            + ";\n    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n           color = vec4(color.r, color.g, color.b, color.a);\n    }\n    else {\n        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n    }\n    gl_FragColor = color;\n}";
      }

      @Override
      public void step(Game game) {
         if (!this.moves_relative.isEmpty()) {
            this.move = this.moves_relative.get(0);
            float xPos = game.player.battleSprite.getX() - this.move.x;
            game.player.battleSprite.setX(xPos);
            xPos = game.battle.oppPokemon.sprite.getX() + this.move.x;
            game.battle.oppPokemon.sprite.setX(xPos);
            this.moves_relative.remove(0);
            if (this.moves_relative.isEmpty()) {
               game.insertAction(new DisplayTextIntro(game, "An oppressive force surrounds you...", null, null, false, null));
            }
         }

         if (this.timer == 380) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            game.insertAction(new DisplayTextIntro(game, "MEWTWO unleashes its full power!", null, null, false, null));
         }

         if (this.timer == 0) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.8F));
            game.uiBatch.setShader(shader);
         }

         if (this.timer == 490) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.6F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 496) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.4F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 502) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.2F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 508) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.0F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 514) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.2F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 520) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.4F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 526) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.6F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 532) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.8F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 538) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.85F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 544) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.9F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 550) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.95F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 556) {
            game.actionStack.remove(game.displayTextAction);
            game.displayTextAction = null;
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-1.0F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 607) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(-0.5F));
            game.uiBatch.setShader(shader);
         } else if (this.timer == 609) {
            game.uiBatch.setShader(null);
         } else if (this.timer == 620) {
            SpecialBattleMewtwo.DrawBreathingSprite.shouldBreathe = true;
         }

         if (this.timer >= 640) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   class RippleEffect1 extends Action {
      public Action.Layer layer = Action.Layer.gui_109;
      ShaderProgram shader;
      String fragShader;
      int yPos = -16;
      Pixmap pixmap;
      Sprite sprite;
      int[] offsets = new int[]{0, 0, 0, 1, 2, 2, 2, 3, 4, 4, 4, 3, 2, 2, 2, 1};

      public RippleEffect1() {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("battle/pixel1.png"));
         this.sprite = new Sprite(text, 0, 0, 1, 1);
         this.fragShader = "precision mediump float;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nuniform mat4 u_projTrans;\nbool equals(float a, float b) {\n    return abs(a-b) < 0.0001;\n}\nvoid main() {\n    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n    gl_FragColor = color;\n}\n";
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      String getVertexShader(int timer) {
         return "attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nvoid main()\n{\n    v_color = a_color;\n    v_texCoords = a_texCoord0;\n    int timer = "
            + timer
            + ";\n    int offset = (timer + int(a_position.y)) % 16;\n        if (offset == 0 || offset == 4) {offset = 0;}\n        else if (offset == 1 || offset == 3) {offset = 1;}\n        else if (offset == 2) {offset = 2;}\n        else if (offset == 5 || offset == 7) {offset = -1;}\n        else if (offset == 6) {offset = -2;}\n    gl_Position =  u_projTrans * vec4(a_position.x + offset, a_position.y, a_position.z, 1.0);\n}\n";
      }

      @Override
      public void step(Game game) {
         if (this.yPos <= 144) {
            this.pixmap = ScreenUtils.getFrameBufferPixmap(0, this.yPos * 3, 480, 48);

            for (int j = 0; j < 16; j++) {
               for (int i = 0; i < 160; i++) {
                  this.sprite.setColor(new Color(this.pixmap.getPixel(i * 3, j * 3)));
                  this.sprite.setPosition(i + this.offsets[j], j + this.yPos);
                  this.sprite.draw(game.uiBatch);
               }
            }
         }

         if (this.yPos > 576) {
            this.yPos = 0;
         }

         if (this.yPos % 2 == 0) {
            this.yPos += 3;
         } else {
            this.yPos += 4;
         }

         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      }
   }

   static class RocksEffect1 extends Action {
      public static int velocityX = 0;
      public static boolean shouldMoveX = false;
      public static boolean shouldMoveY = true;
      public Action.Layer layer = Action.Layer.gui_111;
      Sprite textboxSprite;
      Sprite[] sprites = new Sprite[10];
      int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int velocity = 1;
      int whichVelocity = 0;
      Random rand = new Random();
      boolean firstStep = true;

      public RocksEffect1() {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("battle/battle_bg4.png"));
         this.textboxSprite = new Sprite(text, 0, 0, 176, 160);
         this.textboxSprite.setPosition(-8.0F, -8.0F);
         text = TextureCache.get(Gdx.files.internal("battle/rock1.png"));
         this.sprites[0] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[0].setColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.sprites[1] = new Sprite(text, 32, 0, 32, 32);
         this.sprites[2] = new Sprite(text, 64, 0, 32, 32);
         this.sprites[3] = new Sprite(text, 160, 0, 32, 32);
         this.sprites[4] = new Sprite(text, 192, 0, 32, 32);
         this.sprites[5] = new Sprite(text, 32, 0, 32, 32);
         this.sprites[6] = new Sprite(text, 64, 0, 32, 32);
         this.sprites[7] = new Sprite(text, 160, 0, 32, 32);
         this.sprites[8] = new Sprite(text, 192, 0, 32, 32);
         this.sprites[9] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[9].setColor(1.0F, 1.0F, 1.0F, 1.0F);

         for (int i = 0; i < 10; i++) {
            this.sprites[i].setPosition(this.rand.nextInt(128), this.rand.nextInt(144) - 144);
            this.velocities[i] = this.rand.nextInt(2) + 1;
            this.velocities2[i] = this.velocities[i] - 1 + this.rand.nextInt(2);
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
         if (this.firstStep) {
            SpecialBattleMewtwo.RocksEffect2.drawRocks = true;
            this.firstStep = false;
         }

         for (int i = 0; i < 10; i++) {
            if (this.whichVelocity == 0) {
               this.velocity = this.velocities[i];
            } else {
               this.velocity = this.velocities2[i];
            }

            if (!shouldMoveY) {
               this.velocity = 0;
            }

            this.sprites[i].setPosition(this.sprites[i].getX() + velocityX, this.sprites[i].getY() + this.velocity);
            if (this.sprites[i].getY() > 144.0F) {
               this.sprites[i].setPosition(this.rand.nextInt(128), this.rand.nextInt(144) - 144);
               this.velocities[i] = this.rand.nextInt(2) + 1;
               this.velocities2[i] = this.velocities[i] - 1 + this.rand.nextInt(2);
            }

            if (this.sprites[i].getX() < 0.0F) {
               this.sprites[i].setPosition(160.0F, this.rand.nextInt(144) - 32);
            }

            this.sprites[i].draw(game.uiBatch);
         }

         this.whichVelocity = (this.whichVelocity + 1) % 2;
         this.textboxSprite.draw(game.uiBatch);
         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      }
   }

   static class RocksEffect2 extends Action {
      public static int velocityX = 0;
      public static boolean shouldMoveX = false;
      public static boolean shouldMoveY = true;
      static boolean drawRocks = false;
      public Action.Layer layer = Action.Layer.gui_131;
      Sprite bgSprite;
      Sprite bgSprite2;
      Sprite[] sprites = new Sprite[10];
      int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int velocity = 1;
      int whichVelocity = 0;
      Random rand = new Random();
      float[] bg_values = new float[]{
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         8.0F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         8.0F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.8F,
         0.9F,
         0.9F,
         0.9F,
         0.9F,
         0.9F,
         0.9F,
         0.95F,
         0.95F,
         0.95F,
         0.95F,
         0.95F,
         0.95F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         1.0F,
         0.95F,
         0.95F,
         0.95F,
         0.95F,
         0.95F,
         0.95F,
         0.9F,
         0.9F,
         0.9F,
         0.9F,
         0.9F,
         0.9F
      };
      int bg_values_idx = 0;
      float curr_value = 0.0F;

      public RocksEffect2() {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
         this.bgSprite = new Sprite(text, 0, 0, 176, 160);
         this.bgSprite.setPosition(-8.0F, -8.0F);
         text = TextureCache.get(Gdx.files.internal("battle/battle_bg5.png"));
         this.bgSprite2 = new Sprite(text, 0, 0, 176, 160);
         this.bgSprite2.setPosition(-8.0F, -8.0F);
         text = TextureCache.get(Gdx.files.internal("battle/rock1.png"));
         this.sprites[0] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[0].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[1] = new Sprite(text, 32, 0, 32, 32);
         this.sprites[1].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[2] = new Sprite(text, 64, 0, 32, 32);
         this.sprites[2].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[3] = new Sprite(text, 160, 0, 32, 32);
         this.sprites[3].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[4] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[4].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[5] = new Sprite(text, 32, 0, 32, 32);
         this.sprites[5].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[6] = new Sprite(text, 64, 0, 32, 32);
         this.sprites[6].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[7] = new Sprite(text, 160, 0, 32, 32);
         this.sprites[7].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[8] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[8].setColor(0.0F, 0.0F, 0.0F, 1.0F);
         this.sprites[9] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[9].setColor(0.0F, 0.0F, 0.0F, 1.0F);

         for (int i = 0; i < 10; i++) {
            this.sprites[i].setPosition(this.rand.nextInt(128), this.rand.nextInt(46) + 20);
            this.velocities[i] = this.rand.nextInt(5) + 4;
            this.velocities2[i] = this.velocities[i];
            this.sprites[i].setRotation(this.rand.nextInt(4) * 90);
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
         this.bgSprite.draw(game.uiBatch);
         if (drawRocks) {
            for (int i = 0; i < 10; i++) {
               if (this.velocities2[i] <= 0) {
                  this.velocity = 1;
                  this.velocities2[i] = this.velocities[i];
               } else {
                  this.velocity = 0;
               }

               this.velocities2[i]--;
               if (!shouldMoveY) {
                  this.velocity = 0;
               }

               this.sprites[i].setPosition(this.sprites[i].getX() + velocityX, this.sprites[i].getY() + this.velocity);
               if (this.sprites[i].getY() > 144.0F) {
                  this.sprites[i].setPosition(this.rand.nextInt(128), this.rand.nextInt(46) + 20);
                  this.sprites[i].setRotation(this.rand.nextInt(4) * 90);
                  this.velocities[i] = this.rand.nextInt(5) + 4;
                  this.velocities2[i] = this.velocities[i];
               }

               if (this.sprites[i].getX() < 0.0F) {
                  this.sprites[i].setPosition(160.0F, this.rand.nextInt(112));
                  this.sprites[i].setRotation(this.rand.nextInt(4) * 90);
               }

               this.sprites[i].draw(game.uiBatch);
            }

            this.whichVelocity = (this.whichVelocity + 1) % 2;
            this.bgSprite2.draw(game.uiBatch);
         }

         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      }
   }
}
