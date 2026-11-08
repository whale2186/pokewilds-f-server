package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;
import java.util.Locale;

class RegigigasBattle {
   public static Action getIntroAction(Game game) {
      float currVol = game.currMusic.getVolume();
      currVol = 0.004F * (currVol / 0.3F);
      RegigigasBattle.Draw drawBattle = new RegigigasBattle.Draw(game, null);
      Action introAction = new DisplayText(
         game,
         "...",
         null,
         null,
         new FadeMusic(
            game.currMusic,
            -currVol,
            new WaitFrames(
               game,
               90,
               new SetField(
                  game.musicController,
                  "startBattle",
                  "gigas_battle1",
                  new SplitAction(
                     new CallMethod(game.uiBatch, "setColor", new Object[]{new Color(1.0F, 1.0F, 1.0F, 1.0F)}, null),
                     new CallMethod(
                        game.mapBatch,
                        "setColor",
                        new Object[]{new Color(0.5F, 0.5F, 0.5F, 1.0F)},
                        new WaitFrames(
                           game, 190, new BattleIntro(new RegigigasBattle.BattleIntro1(drawBattle, new RegigigasBattle.PositionPlayers(game, null)))
                        )
                     )
                  )
               )
            )
         )
      );
      if (game.battle.oppPokemon.isShiny) {
         introAction.append(new Battle.LoadAndPlayAnimation(game, "shiny", game.player.currPokemon, null));
      }

      introAction.append(
         new SplitAction(
            new WaitFrames(game, 4, new PlaySound(game.battle.oppPokemon, null)),
            new PokemonIntroAnim(
               new WaitFrames(
                  game,
                  11,
                  new DisplayText(
                     game,
                     "The battlefield quakes under REGIGIGAS' presence!",
                     null,
                     null,
                     new SetField(
                        drawBattle,
                        "shouldFadeAlpha",
                        true,
                        new SetField(
                           drawBattle,
                           "alsoDoShockwave",
                           true,
                           new WaitFrames(
                              game,
                              60,
                              new WaitFrames(
                                 game,
                                 39,
                                 new SplitAction(
                                    new WaitFrames(game, 1, new DrawEnemyHealthGen2(game)),
                                    new MovePlayerOffScreen(
                                       game,
                                       new DisplayText(
                                          game,
                                          "Go! " + game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + "!",
                                          null,
                                          true,
                                          false,
                                          new ThrowOutPokemonCrystal(game, null)
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
      if (game.player.currPokemon.isShiny) {
         introAction.append(new Battle.LoadAndPlayAnimation(game, "shiny", game.battle.oppPokemon, null));
      }

      introAction.append(
         new PlaySound(
            game.player.currPokemon,
            new WaitFrames(
               game,
               game.player.currPokemon.specie.cryLengthInFrames(),
               new WaitFrames(
                  game, 6, new DrawFriendlyHealthGen2(game, new DisplayText.Clear(game, new WaitFrames(game, 15, new DrawBattleMenuNormal(game, null))))
               )
            )
         )
      );
      return introAction;
   }

   static class BattleIntro1 extends Action {
      ArrayList<Sprite> frames;
      Sprite frame;
      public Action.Layer layer = Action.Layer.gui_139;
      Action nextAction2;
      Sprite bgSprite;

      public BattleIntro1(Action nextAction2, Action nextAction) {
         super();
         this.nextAction = nextAction;
         this.nextAction2 = nextAction2;
         this.frames = new ArrayList<>();
         Texture text = TextureCache.get(Gdx.files.internal("battle/battle_intro_anim1_sheet1.png"));

         for (int i = 0; i < 28; i++) {
            this.frames.add(new Sprite(text, i * 160, 0, 160, 144));
         }

         for (int i = 0; i < 95; i++) {
            this.frames.add(new Sprite(text, 4320, 0, 160, 144));
         }

         text = TextureCache.get(Gdx.files.internal("battle/intro_frame3.png"));
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
         this.frame = this.frames.get(0);
         if (this.frame != null) {
            this.frame.draw(game.uiBatch);
         }

         this.frames.remove(0);
         game.uiBatch.draw(this.bgSprite, -160.0F, 0.0F);
         game.uiBatch.draw(this.bgSprite, 160.0F, 0.0F);
         game.uiBatch.draw(this.bgSprite, 0.0F, -144.0F);
         game.uiBatch.draw(this.bgSprite, 0.0F, 144.0F);
         if (this.frames.isEmpty()) {
            game.insertAction(this.nextAction);
            game.insertAction(this.nextAction2);
            game.actionStack.remove(this);
         }
      }
   }

   static class Draw extends DrawBattle {
      public Action.Layer layer = Action.Layer.gui_130;
      SpriteProxy spriteProxy;
      Texture helper;
      Texture helper2;
      Texture textBox2;
      Sprite bg1;
      double timer = 0.0;
      double waveTimer = Math.PI * 8;
      int waveSoundTimer = 0;
      Color color = new Color();
      double waveTimer2 = 0.0;
      double alphaTimer = 0.0;
      Music soundEffect;
      public static boolean doShockwave = false;
      public static boolean shouldBreathe = true;
      int breathingTimer = 300;
      public static int breathingOffsetY = 0;
      public static SpriteProxy upperSprite;
      public static SpriteProxy lowerSprite;
      public boolean shouldFadeAlpha = false;
      public boolean alsoDoShockwave = false;
      float fadeAlpha = 1.0F;
      public static boolean canMove = true;
      public static int specialAttackCounter = 0;

      public Draw(Game game, Action nextAction) {
         super(game);
         this.nextAction = nextAction;
         this.spriteProxy = new SpriteProxy(TextureCache.get(Gdx.files.internal("gigas_ground4.png")), 0, 0, 32, 32);
         this.helper2 = TextureCache.get(Gdx.files.internal("battle/battle_bg4.png"));
         this.bg1 = new Sprite(TextureCache.get(Gdx.files.internal("battle/intro_frame6.png")), 0, 0, 160, 144);
         this.textBox2 = TextureCache.get(Gdx.files.internal("battle/battle_bg6.png"));
         if (game.battle.oppPokemon != null && game.battle.oppPokemon.isShiny) {
            upperSprite = new SpriteProxy(TextureCache.get(Gdx.files.internal("pokemon/regigigas_upper_shiny.png")), 0, 0, 56, 56);
            lowerSprite = new SpriteProxy(TextureCache.get(Gdx.files.internal("pokemon/regigigas_lower_shiny.png")), 0, 0, 56, 56);
         } else {
            upperSprite = new SpriteProxy(TextureCache.get(Gdx.files.internal("pokemon/regigigas_upper.png")), 0, 0, 56, 56);
            lowerSprite = new SpriteProxy(TextureCache.get(Gdx.files.internal("pokemon/regigigas_lower.png")), 0, 0, 56, 56);
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
      public void firstStep(Game game) {
         this.soundEffect = AudioLoader.loadMusic("sounds/ap3.ogg");
         this.soundEffect.setLooping(false);
         this.soundEffect.setVolume(0.65F);
         canMove = true;
         specialAttackCounter = 0;
      }

      @Override
      public void step(Game game) {
         if (shouldBreathe) {
            this.breathingTimer--;
            if (this.breathingTimer == 149) {
               breathingOffsetY = -1;
            } else if (this.breathingTimer <= 0) {
               this.breathingTimer = 300;
               breathingOffsetY = 0;
            }
         }

         if (this.shouldFadeAlpha) {
            this.fadeAlpha -= 0.05F;
         }

         game.uiBatch.flush();
         Gdx.gl.glClear(16384);
         if (doShockwave || this.alsoDoShockwave) {
            doShockwave = false;
            this.alsoDoShockwave = false;
            this.waveTimer = 0.0;
            this.waveSoundTimer = 0;
            if (this.fadeAlpha > 0.0F) {
               game.insertAction(new PlayMusic("boss_appear1", null));
            } else {
               game.insertAction(new WaitFrames(game, 30, new PlayMusic("boss_appear1", null)));
            }
         }

         this.color.set(game.uiBatch.getColor());
         if (this.fadeAlpha > 0.0F) {
            game.uiBatch.setColor(1.0F, 1.0F, 1.0F, this.fadeAlpha);
            game.uiBatch.draw(this.bg1, 0.0F, 0.0F);
         }

         int offsetY = 0;

         for (int i = 10; i >= -4; i--) {
            if (i == 5) {
               game.uiBatch.setColor(this.color);
               if (game.battle.oppPokemon != null && DrawBattle.shouldDrawOppPokemon) {
                  game.uiBatch.draw(lowerSprite, game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY());
                  game.uiBatch.draw(upperSprite, game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY() + breathingOffsetY);
               }
            }

            if (this.shouldFadeAlpha) {
               game.uiBatch.setColor((10.0F - i) / 10.0F, (10.0F - i) / 10.0F, (10.0F - i) / 10.0F, 1.0F);
               float offsetX = (float)Math.sin(this.timer + i * (Math.PI / 12));
               float offsetX2 = 32.0F - 24.0F * (i / 10.0F);
               if (i >= 2) {
                  if (i < 5) {
                     offsetY = -2 - 2 * (i - 2);
                  } else if (i < 9) {
                     offsetY = -2 - 2 * (i - 2) - 2 - 2 * (i - 6);
                  } else {
                     offsetY = -2 - 2 * (i - 2) - 2 - 2 * (i - 6) - 2 - 2 * (i - 10);
                  }
               }

               for (int j = -3; j < 21; j++) {
                  double offsetY2 = 0.0;
                  double x = 0.0;
                  x = (20 - j) * 0.10471975511965977;
                  x *= x;
                  x = this.waveTimer - (10 - i) * (Math.PI / 5) - x;
                  x = Math.max(0.0, x);
                  x = Math.min(Math.PI, x);
                  float offsetY2Dampen = 32.0F - 28.0F * (i / 10.0F) * ((j + 20) / 20.0F);
                  if (this.alphaTimer < Math.PI * 8) {
                     float alpha = (float)(Math.min(Math.PI / 2, x) / (Math.PI / 2));
                     game.uiBatch.setColor((10.0F - i) / 10.0F, (10.0F - i) / 10.0F, (10.0F - i) / 10.0F, alpha);
                     offsetY2Dampen /= 4.0F;
                     offsetY2 = (float)Math.sin(x);
                  } else if (i < j + 4 && i < 20 - j - 2) {
                     offsetY2 = (float)Math.sin(x);
                  }

                  x = this.waveTimer2 + i * (Math.PI / 12) - (20 - j) * 0.06544984694978735;
                  x = Math.max(0.0, x);
                  x = Math.min(Math.PI, x);
                  double offsetY3 = (float)Math.sin(x);
                  game.uiBatch
                     .draw(
                        this.spriteProxy,
                        (int)(offsetX * offsetX2) + j * 16 - 80,
                        24 + i * 8 + offsetY + (int)(offsetY2 * offsetY2Dampen) + (int)(offsetY3 * 3.0)
                     );
               }
            }
         }

         if (canMove) {
            this.timer += 0.02454369260617026;
            if (this.timer >= Math.PI * 2) {
               this.timer = 0.0;
            }
         }

         if (this.waveTimer < Math.PI * 8) {
            this.waveTimer += 0.1308996938995747;
         }

         if (this.alphaTimer > Math.PI * 4 && Math.PI < this.waveTimer && this.waveTimer < Math.PI * 4) {
            this.waveSoundTimer++;
            if (this.waveSoundTimer % 5 == 0) {
               this.soundEffect.stop();
               this.soundEffect.play();
            }
         }

         this.waveTimer2 += 0.04908738521234052;
         if (this.waveTimer2 > Math.PI * 4) {
            this.waveTimer2 = -Math.PI * 2;
         }

         if (this.shouldFadeAlpha && this.alphaTimer < Math.PI * 8) {
            this.alphaTimer += 0.04908738521234052;
         }

         game.uiBatch.setColor(this.color);
         game.player.battleSprite.draw(game.uiBatch);
         game.uiBatch.draw(this.textBox2, -92.0F, -8.0F);
         if (this.fadeAlpha > 0.0F) {
            game.uiBatch.setColor(1.0F, 1.0F, 1.0F, this.fadeAlpha);
            game.uiBatch.draw(this.helper2, -8.0F, -8.0F);

            for (int i = -1; i < 2; i++) {
               for (int j = -1; j < 2; j++) {
                  if (i != 0 || j != 0) {
                     game.uiBatch.draw(this.bg1, 160 * i, 144 * j);
                  }
               }
            }

            game.uiBatch.setColor(this.color);
         }
      }
   }

   static class PositionPlayers extends Action {
      public Action.Layer layer = Action.Layer.gui_140;
      int timer = 0;
      float xPos = 0.0F;

      public PositionPlayers(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
         game.player.battleSprite.setPosition(160.0F, 48.0F);
         game.battle.oppPokemon.sprite.setPosition(-49.0F, 88.0F);
      }

      @Override
      public void firstStep(Game game) {
         SpriteProxy.darkenAllColors3 = true;
      }

      @Override
      public void step(Game game) {
         if (this.timer <= 144) {
            this.xPos = game.player.battleSprite.getX() - 1.0F;
            game.player.battleSprite.setX(this.xPos);
            this.xPos = game.battle.oppPokemon.sprite.getX() + 1.0F;
            game.battle.oppPokemon.sprite.setX(this.xPos);
         }

         this.timer++;
         if (this.timer > 159) {
            SpriteProxy.darkenAllColors3 = false;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }

   static class SpecialAttack extends Action {
      public Action.Layer layer = Action.Layer.gui_0;
      int timer = 0;

      public SpecialAttack(Action nextAction) {
         super();
         this.nextAction = nextAction;
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
         if (this.timer == 0) {
            RegigigasBattle.Draw.canMove = false;
            RegigigasBattle.Draw.breathingOffsetY = 0;
            RegigigasBattle.Draw.shouldBreathe = false;
         } else if (this.timer == 96) {
            RegigigasBattle.Draw.doShockwave = true;
         } else if (this.timer == 116) {
            RegigigasBattle.Draw.breathingOffsetY = -17;
            game.insertAction(new Battle.LoadAndPlayAnimation(game, "neutral_effective", game.player.currPokemon, null));
         } else if (this.timer == 160) {
            SpriteProxy.darkenAllColors3 = false;
            RegigigasBattle.Draw.shouldBreathe = true;
         } else if (this.timer == 170) {
            RegigigasBattle.Draw.canMove = true;
         } else if (this.timer == 180) {
            SpriteProxy.darkenAllColors2 = false;
         } else if (this.timer == 181) {
            game.insertAction(this.nextAction);
         } else if (this.timer == 200) {
            SpriteProxy.darkenAllColors1 = false;
         }

         if (this.timer < 160) {
            SpriteProxy.darkenAllColors3 = true;
         } else if (this.timer < 180) {
            SpriteProxy.darkenAllColors2 = true;
         } else if (this.timer < 200) {
            SpriteProxy.darkenAllColors1 = true;
         }

         if (this.timer >= 170) {
            if (this.timer < 182) {
               RegigigasBattle.Draw.breathingOffsetY++;
            } else if (this.timer < 192 && this.timer % 2 == 0) {
               RegigigasBattle.Draw.breathingOffsetY++;
            }
         }

         this.timer++;
         if (this.timer > 200) {
            game.actionStack.remove(this);
         }
      }
   }
}
