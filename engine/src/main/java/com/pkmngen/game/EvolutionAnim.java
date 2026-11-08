package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.Locale;

class EvolutionAnim extends Action {
   public static Sprite bgSprite;
   public static boolean drawSprite = true;
   public static boolean drawPostEvoBottom = false;
   public static boolean drawPostEvoTop = false;
   public static boolean isGreyscale = false;
   public static boolean playSound = false;
   public static boolean isDone = false;
   public static boolean drawPostEvo = false;
   public static String vertexShader = "attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord0;\n\nuniform mat4 u_projTrans;\n\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\n\nvoid main() {\n    v_color = a_color;\n    v_texCoords = a_texCoord0;\n    gl_Position = u_projTrans * a_position;\n}";
   public static String fragmentShader = "#ifdef GL_ES\n    precision mediump float;\n#endif\n\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\n\nvoid main() {\n  vec4 c = v_color * texture2D(u_texture, v_texCoords);\n  int r = int(c.r*31.0);\n  int g = int(c.g*31.0);\n  int b = int(c.b*31.0);\n  int sum = r + g + b;\n  float grey = float(int(sum*sum*sum)/25947)/31.0;\n  gl_FragColor = vec4(grey, grey, grey, c.a);\n}";
   public Action.Layer layer = Action.Layer.gui_107;
   Sprite spritePart;
   Sprite preEvoSprite;
   Sprite postEvoSprite;
   Sprite preEvoSpriteTop;
   Sprite postEvoSpriteTop;
   Sprite preEvoSpriteBottom;
   Sprite postEvoSpriteBottom;
   Music preEvoCry;
   Music postEvoCry;
   int timer = 0;
   Pokemon targetPokemon;
   String targetName;
   ShaderProgram grayscaleShader = new ShaderProgram(vertexShader, fragmentShader);

   public EvolutionAnim(Pokemon targetPokemon, String evolveTo, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.targetPokemon = targetPokemon;
      this.targetName = evolveTo;
      Texture text = TextureCache.get(Gdx.files.internal("battle/battle_bg3.png"));
      bgSprite = new Sprite(text, 0, 0, 176, 160);
      this.preEvoSprite = new Sprite(targetPokemon.sprite);
      this.preEvoSprite.flip(true, false);
      this.preEvoSpriteBottom = new Sprite(this.preEvoSprite);
      this.preEvoSpriteBottom.setRegionY((int)this.preEvoSprite.getWidth() / 2);
      this.preEvoSpriteBottom.setRegionHeight((int)this.preEvoSprite.getWidth() / 2);
      this.preEvoSpriteTop = new Sprite(this.preEvoSprite);
      this.preEvoSpriteTop.setRegionY(0);
      this.preEvoSpriteTop.setRegionHeight((int)this.preEvoSprite.getWidth() / 2);
   }

   @Override
   public void firstStep(Game game) {
      drawSprite = false;
      drawPostEvoBottom = false;
      drawPostEvoTop = false;
      isGreyscale = false;
      playSound = false;
      isDone = false;
      drawPostEvo = false;
      if (!Specie.species.containsKey(this.targetName)) {
         Specie.species.put(this.targetName, new Specie(this.targetName));
      }

      Specie specie = Specie.species.get(this.targetName);
      this.postEvoSprite = new Sprite(specie.sprite);
      if (this.targetPokemon.isShiny) {
         this.postEvoSprite = new Sprite(specie.spriteShiny);
      }

      this.postEvoSprite.flip(true, false);
      this.postEvoSpriteBottom = new Sprite(this.postEvoSprite);
      this.postEvoSpriteBottom.setRegionY((int)this.postEvoSprite.getWidth() - (int)this.preEvoSprite.getWidth() + (int)this.preEvoSprite.getWidth() / 2);
      this.postEvoSpriteBottom.setRegionHeight((int)this.preEvoSprite.getWidth() / 2);
      this.postEvoSpriteTop = new Sprite(this.postEvoSprite);
      this.postEvoSpriteTop.setRegionY(0);
      this.postEvoSpriteTop.setRegionHeight((int)this.postEvoSprite.getWidth() - (int)this.preEvoSprite.getWidth() + (int)this.preEvoSprite.getWidth() / 2);
      if (game.battle.drawAction != null) {
         game.battle.drawAction.cleanup(game);
      }

      game.currMusic.stop();
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
      game.uiBatch.draw(bgSprite, -8.0F, -8.0F);
      if (this.timer == 33) {
         drawSprite = true;
         game.insertAction(this.nextAction);
      }

      if (playSound) {
         playSound = false;
         Music sound = AudioLoader.loadMusic("sounds/evolve_fanfare1.ogg");
         sound.play();
      }

      if (drawSprite) {
         if (isGreyscale) {
            game.uiBatch.setShader(this.grayscaleShader);
         }

         if (drawPostEvo) {
            this.spritePart.set(this.targetPokemon.sprite);
            this.spritePart.flip(true, false);
            game.uiBatch.draw(this.spritePart, 80 - (int)this.spritePart.getWidth() / 2 + 4, 72.0F);
         } else {
            if (drawPostEvoBottom) {
               this.spritePart = this.postEvoSpriteBottom;
            } else {
               this.spritePart = this.preEvoSpriteBottom;
            }

            game.uiBatch.draw(this.spritePart, 80 - (int)this.spritePart.getWidth() / 2 + 4, 72.0F);
            if (drawPostEvoTop) {
               this.spritePart = this.postEvoSpriteTop;
            } else {
               this.spritePart = this.preEvoSpriteTop;
            }

            game.uiBatch.draw(this.spritePart, 80 - (int)this.spritePart.getWidth() / 2 + 4, 72 + (int)this.preEvoSpriteTop.getWidth() / 2);
         }

         game.uiBatch.setShader(null);
      }

      if (isDone) {
         game.actionStack.remove(this);
      }

      this.timer++;
   }

   public static class Done extends Action {
      int timer = 0;

      public Done(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
         EvolutionAnim.bgSprite = new Sprite(text, 0, 0, 176, 160);
      }

      @Override
      public void step(Game game) {
         if (this.timer == 2) {
            EvolutionAnim.drawSprite = false;
         }

         if (this.timer == 36) {
            game.insertAction(new EnterBuilding(game, "", null));
         }

         if (this.timer == 45) {
            EvolutionAnim.isDone = true;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   public static class HandleInput extends Action {
      int timer = 0;
      Action currAction;
      String evolveTo;
      Pokemon pokemon;

      public HandleInput(Pokemon pokemon, String evolveTo, Action nextAction) {
         super();
         this.pokemon = pokemon;
         this.evolveTo = evolveTo;
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         this.currAction = new Battle.LoadAndPlayAnimation(
            game,
            "evolve",
            null,
            new WaitFrames(
               game,
               30,
               new RunCode(
                  () -> {
                     if (game.player.hmPokemon == this.pokemon && !game.player.currFieldMove.equals("")) {
                        game.player.swapSprites(game.player.hmPokemon);
                     }

                     this.pokemon.evolveTo(this.evolveTo);
                     if (game.player.hmPokemon == this.pokemon && !game.player.currFieldMove.equals("")) {
                        if (game.player.currFieldMove.equals("SURF")) {
                           game.player.swapSpritesSurfing(game.player.hmPokemon);
                        } else {
                           game.player.swapSprites(game.player.hmPokemon);
                        }
                     }

                     game.battle.oppPokemon = this.pokemon;
                     EvolutionAnim.drawPostEvo = true;
                     game.insertAction(new WaitFrames(game, 4, new PlayMusic(new Pokemon(this.evolveTo.toLowerCase(Locale.ROOT), 10), null)));
                  },
                  new PokemonIntroAnim(
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(
                              game,
                              "Congratulations! Your " + this.pokemon.nickname.toUpperCase(Locale.ROOT),
                              null,
                              true,
                              true,
                              new DisplayText.Clear(
                                 game,
                                 new WaitFrames(
                                    game,
                                    3,
                                    new DisplayText(
                                       game,
                                       "evolved into " + this.evolveTo.toUpperCase(Locale.ROOT) + "!",
                                       "fanfare2.ogg",
                                       true,
                                       false,
                                       new CheckMovesLearned(
                                          this.pokemon,
                                          new DisplayText.Clear(
                                             game,
                                             new WaitFrames(
                                                game,
                                                3,
                                                new EvolutionAnim.Done(new SetField(game.musicController, "evolveMusicFadeout", true, this.nextAction))
                                             )
                                          )
                                       )
                                    )
                                 )
                              )
                           )
                        )
                     )
                  )
               )
            )
         );
         game.insertAction(this.currAction);
      }

      @Override
      public void step(Game game) {
         if (InputProcessor.bJustPressed) {
            EvolutionAnim.isGreyscale = false;
            EvolutionAnim.drawPostEvoTop = false;
            EvolutionAnim.drawPostEvoBottom = false;
            game.actionStack.remove(this.currAction);
            game.actionStack.remove(this);
            game.insertAction(
               new WaitFrames(
                  game,
                  30,
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           "Huh? " + this.pokemon.nickname.toUpperCase(Locale.ROOT) + " stopped evolving!",
                           null,
                           true,
                           true,
                           new DisplayText.Clear(
                              game,
                              new WaitFrames(game, 3, new EvolutionAnim.Done(new SetField(game.musicController, "evolveMusicFadeout", true, this.nextAction)))
                           )
                        )
                     )
                  )
               )
            );
         } else {
            this.timer++;
            if (this.timer >= 464) {
               game.actionStack.remove(this);
            }
         }
      }
   }

   public static class StartMusic extends Action {
      public StartMusic() {
         super();
      }

      @Override
      public void firstStep(Game game) {
         game.currMusic = AudioLoader.loadMusic("sounds/evolution1.ogg");
         game.currMusic.play();
         game.actionStack.remove(this);
      }
   }
}
