package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;
import java.util.Locale;

class SpecialBattleMegaGengar extends Action {
   public Action.Layer layer = Action.Layer.gui_106;
   Music music;
   boolean firstStep = true;
   int timer = 0;
   Music temp;

   public SpecialBattleMegaGengar(Game game) {
      super();
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
         this.music = AudioLoader.loadMusic("battle/mgengar_battle_intro1.wav");
         this.music.setLooping(false);
         this.music.setVolume(0.2F);
         game.currMusic.stop();
         game.currMusic.dispose();
         game.currMusic = this.music;
         game.currMusic.stop();
         game.currMusic.play();
         game.currMusic.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(Music aMusic) {
               SpecialBattleMegaGengar.this.temp.play();
            }
         });
         this.firstStep = false;
      }

      if (this.timer == 0) {
         game.actionStack.remove(game.displayTextAction);
         game.displayTextAction = null;
         SpecialMegaGengar1 gengar = new SpecialMegaGengar1(70);
         game.battle.oppPokemon = gengar;
         Action triggerAction = new PlayMusic(game.player.currPokemon.specie.name, new WaitFrames(game, 6, new DrawBattleMenuNormal(game, null)));
         Action nextAction = new BattleIntro(
            new SpecialBattleMegaGengar.BattleIntro1(
               new SplitAction(
                  new SpecialBattleMegaGengar.DrawBattle1(game),
                  new SplitAction(
                     new SpecialBattleMegaGengar.DrawBreathingSprite(gengar),
                     new SpecialBattleMegaGengar.IntroAnim(
                        game,
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
                                    new SplitAction(new DrawFriendlyHealthGen2(game), new ThrowOutPokemon(game, triggerAction))
                                 )
                              )
                           )
                        )
                     )
                  )
               )
            )
         );
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
         this.frame = this.frames.get(0);
         if (this.frame != null) {
            this.frame.draw(game.uiBatch);
         }

         this.frames.remove(0);
         if (this.frames.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            this.nextAction.step(game);
         }
      }
   }

   class DrawBattle1 extends DrawBattle {
      public Action.Layer layer = Action.Layer.gui_130;
      Sprite bgSprite;

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
         game.player.battleSprite.draw(game.uiBatch);
      }
   }

   static class DrawBreathingSprite extends Action {
      static boolean shouldBreathe = false;
      public Action.Layer layer = Action.Layer.gui_131;
      SpecialMegaGengar1 gengar;
      int timer = 300;
      int offsetY = 0;
      int offsetY2 = 0;
      Sprite bgSprite;

      public DrawBreathingSprite(SpecialMegaGengar1 gengar) {
         super();
         this.gengar = gengar;
         Texture text = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
         this.bgSprite = new Sprite(text, 0, 0, 160, 144);
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
            if (this.timer == 74) {
               this.offsetY2 = -1;
            } else if (this.timer == 274) {
               this.offsetY2 = 0;
            }

            if (this.timer == 149) {
               this.offsetY = -1;
            } else if (this.timer == 0) {
               this.timer = 300;
               this.offsetY = 0;
            }
         }

         this.bgSprite.draw(game.uiBatch);
         this.gengar.breathingSprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() - this.offsetY);
         this.gengar.breathingSprite.draw(game.uiBatch);
         this.gengar.sprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() - this.offsetY2);
         this.gengar.sprite.draw(game.uiBatch);
         this.gengar.sprite.setPosition(this.gengar.sprite.getX(), this.gengar.sprite.getY() + this.offsetY2);
         if (game.battle.drawAction == null) {
            game.actionStack.remove(this);
         }
      }
   }

   static class IntroAnim extends Action {
      public Action.Layer layer = Action.Layer.gui_140;
      ArrayList<Vector2> moves_relative;
      Vector2 move;
      int timer = 0;
      boolean firstStep = true;

      public IntroAnim(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
         this.moves_relative = new ArrayList<>();

         for (int i = 0; i < 144; i++) {
            this.moves_relative.add(new Vector2(1.0F, 0.0F));
         }

         game.player.battleSprite.setPosition(166.0F, 62.0F);
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
         if (this.firstStep) {
            ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.8F));
            game.uiBatch.setShader(shader);
            this.firstStep = false;
         }

         if (!this.moves_relative.isEmpty()) {
            this.move = this.moves_relative.get(0);
            float xPos = game.player.battleSprite.getX() - this.move.x;
            game.player.battleSprite.setX(xPos);
            xPos = game.battle.oppPokemon.sprite.getX() + this.move.x;
            game.battle.oppPokemon.sprite.setX(xPos);
            this.moves_relative.remove(0);
         } else {
            if (this.timer == 30) {
               game.insertAction(new PlayMusic(game.battle.oppPokemon.specie.name, null));
            } else if (this.timer == 20) {
               ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.7F));
               game.uiBatch.setShader(shader);
            } else if (this.timer == 44) {
               ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.6F));
               game.uiBatch.setShader(shader);
            } else if (this.timer == 68) {
               ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.4F));
               game.uiBatch.setShader(shader);
            } else if (this.timer == 92) {
               ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.2F));
               game.uiBatch.setShader(shader);
            } else if (this.timer == 116) {
               ShaderProgram shader = new ShaderProgram(EvolutionAnim.vertexShader, this.getShader(0.0F));
               game.uiBatch.setShader(shader);
            }

            if (this.timer == 20) {
               game.insertAction(new DisplayTextIntro(game, "MEGA GENGAR attacked!", null, null, false, null));
            }

            if (this.timer == 216) {
               game.insertAction(this.nextAction);
            }

            if (this.timer >= 246) {
               game.actionStack.remove(game.displayTextAction);
               game.displayTextAction = null;
               game.actionStack.remove(this);
               SpecialBattleMegaGengar.DrawBreathingSprite.shouldBreathe = true;
            }

            this.timer++;
         }
      }
   }
}
