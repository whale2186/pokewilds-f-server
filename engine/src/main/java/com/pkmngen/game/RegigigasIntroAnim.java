package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.Random;

class RegigigasIntroAnim extends Action {
   int timer = 0;
   int phase = 0;
   Tile regiTile;
   Tile pedistalTile;
   Music soundEffect;
   Music soundEffect2;
   Sprite lightningSprite;
   String dirFacing = "left";
   String regiName;
   int regionNum = 0;

   public RegigigasIntroAnim(String regiName, String dirFacing, Action nextAction) {
      super();
      this.regiName = regiName;
      this.dirFacing = dirFacing;
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("lightning1.png"));
      this.lightningSprite = new Sprite(text, 0, 0, 160, 144);
   }

   @Override
   public void firstStep(Game game) {
      for (Tile tile : game.map.tiles.values()) {
         if (tile.name.equals("cave1_regi1")) {
            this.regiTile = tile;
            break;
         }
      }

      for (Tile tile : game.map.tiles.values()) {
         if (tile.name.contains("cave1_regipedistal1")) {
            this.pedistalTile = tile;
            break;
         }
      }

      this.soundEffect2 = AudioLoader.loadMusic("sounds/splash1.ogg");
      this.soundEffect2.setLooping(false);
      this.soundEffect2.setVolume(0.3F);
      this.soundEffect = AudioLoader.loadMusic("sounds/stomp1.ogg");
      this.soundEffect.setLooping(true);
      this.soundEffect.setVolume(0.7F);
   }

   @Override
   public void step(Game game) {
      if (this.phase == 0) {
         if (this.timer == 0) {
            this.soundEffect.play();
         }

         int step = 60;
         if (this.timer < step * 8) {
            if (this.timer % 4 == 0) {
               game.cam.translate(0.0F, 2.0F);
               game.player.position.add(0.0F, 2.0F);
               this.regiTile.sprite.translateY(2.0F);
            } else if (this.timer % 4 == 2) {
               game.cam.translate(0.0F, -2.0F);
               game.player.position.add(0.0F, -2.0F);
               this.regiTile.sprite.translateY(-2.0F);
            }
         }

         if (this.timer == step * 2) {
            this.regiTile.sprite.setRegionX(32);
            this.regiTile.sprite.setRegionWidth(32);
            this.soundEffect2.play();
         } else if (this.timer == step * 4) {
            this.regiTile.sprite.setRegionX(64);
            this.regiTile.sprite.setRegionWidth(32);
            this.soundEffect2.play();
         } else if (this.timer == step * 6) {
            this.regiTile.sprite.setRegionX(96);
            this.regiTile.sprite.setRegionWidth(32);
            this.soundEffect2.play();
         } else if (this.timer == step * 8) {
            this.regiTile.sprite.setRegionX(128);
            this.regiTile.sprite.setRegionWidth(32);
            this.soundEffect.stop();
         } else if (this.timer == step * 10) {
            this.phase = 1;
            this.timer = 0;
         }
      } else if (this.phase == 1) {
         if (this.timer == 1) {
            this.soundEffect.stop();
            this.soundEffect.dispose();
            this.soundEffect = AudioLoader.loadMusic("sounds/gigas_noises14.ogg");
            this.soundEffect.setVolume(1.0F);
            this.soundEffect.play();
         }

         if (this.timer < 36) {
            if (this.timer < 18) {
               if (this.timer % 4 == 0) {
                  this.regiTile.sprite.setRegion(192, 0, 32, 32);
               } else if (this.timer % 4 == 2) {
                  this.regiTile.sprite.setRegion(128, 0, 32, 32);
               }
            } else if (this.timer < 36) {
               if (this.timer % 6 == 0) {
                  this.regiTile.sprite.setRegion(192, 0, 32, 32);
               } else if (this.timer % 6 == 3) {
                  this.regiTile.sprite.setRegion(128, 0, 32, 32);
               }
            }
         } else if (this.timer >= 70) {
            if (this.timer < 106) {
               if (this.timer == 70) {
                  this.soundEffect.stop();
                  this.soundEffect.play();
               }

               if (this.timer < 88) {
                  if (this.timer % 4 == 0) {
                     this.regiTile.sprite.setRegion(224, 0, 32, 32);
                  } else if (this.timer % 4 == 2) {
                     this.regiTile.sprite.setRegion(128, 0, 32, 32);
                  }
               } else if (this.timer < 106) {
                  if (this.timer % 6 == 0) {
                     this.regiTile.sprite.setRegion(224, 0, 32, 32);
                  } else if (this.timer % 6 == 3) {
                     this.regiTile.sprite.setRegion(128, 0, 32, 32);
                  }
               }
            } else if (this.timer >= 110) {
               if (this.timer < 146) {
                  if (this.timer == 110) {
                     this.soundEffect.stop();
                     this.soundEffect.play();
                  }

                  if (this.timer < 128) {
                     if (this.timer % 4 == 0) {
                        this.regiTile.sprite.setRegion(288, 0, 32, 32);
                     } else if (this.timer % 4 == 2) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                     }
                  } else if (this.timer < 146) {
                     if (this.timer % 6 == 0) {
                        this.regiTile.sprite.setRegion(288, 0, 32, 32);
                     } else if (this.timer % 6 == 3) {
                        this.regiTile.sprite.setRegion(128, 0, 32, 32);
                     }
                  }
               } else if (this.timer >= 150) {
                  if (this.timer < 186) {
                     if (this.timer == 150) {
                        this.soundEffect.stop();
                        this.soundEffect.play();
                     }

                     if (this.timer < 168) {
                        if (this.timer % 4 == 0) {
                           this.regiTile.sprite.setRegion(256, 0, 32, 32);
                        } else if (this.timer % 4 == 2) {
                           this.regiTile.sprite.setRegion(128, 0, 32, 32);
                        }
                     } else if (this.timer < 186) {
                        if (this.timer % 6 == 0) {
                           this.regiTile.sprite.setRegion(256, 0, 32, 32);
                        } else if (this.timer % 6 == 3) {
                           this.regiTile.sprite.setRegion(128, 0, 32, 32);
                        }
                     }
                  } else if (this.timer >= 220) {
                     this.phase = 4;
                     this.timer = 0;
                  }
               }
            }
         }
      } else if (this.phase == 1) {
         if (this.timer == 20) {
            this.soundEffect.stop();
            this.soundEffect.dispose();
            this.soundEffect = AudioLoader.loadMusic("sounds/gigas_noises2.ogg");
            this.soundEffect.setVolume(1.0F);
            this.soundEffect.play();
            this.soundEffect.pause();
            this.soundEffect.setPosition(5.0F);
         }

         if (this.timer >= 120) {
            if (this.timer < 180) {
               if (this.timer == 120) {
                  this.soundEffect.play();
               }

               if (this.timer % 6 == 0) {
                  this.regiTile.sprite.setRegionX(160);
                  this.regiTile.sprite.setRegionWidth(32);
               } else if (this.timer % 6 == 3) {
                  this.regiTile.sprite.setRegionX(128);
                  this.regiTile.sprite.setRegionWidth(32);
               }
            } else if (this.timer >= 240) {
               this.soundEffect.stop();
               this.phase++;
               this.timer = 0;
            }
         }
      } else if (this.phase == 1) {
         if (this.timer == 2) {
            this.soundEffect.stop();
            this.soundEffect.dispose();
         }

         if (this.timer == 20) {
            this.soundEffect = AudioLoader.loadMusic("sounds/gigas_noises7.ogg");
            this.soundEffect.setVolume(1.0F);
         }

         if (this.timer >= 80) {
            if (this.timer < 140) {
               if (this.timer % 4 == 0) {
                  this.regiTile.sprite.setRegionX(128);
                  this.regiTile.sprite.setRegionWidth(32);
                  this.soundEffect.stop();
                  this.soundEffect.play();
               } else if (this.timer % 4 == 3) {
                  this.regiTile.sprite.setRegionX(160);
                  this.regiTile.sprite.setRegionWidth(32);
               }
            } else if (this.timer >= 200) {
               this.phase++;
               this.timer = 0;
            }
         }
      } else if (this.phase != 2 && this.phase != 3) {
         if (this.phase == 4) {
            if (this.timer >= 30) {
               if (this.timer < 60) {
                  if (this.timer % 6 == 0) {
                     this.regiTile.sprite.setRegion(128, 0, 32, 32);
                  } else if (this.timer % 6 == 3) {
                     this.regiTile.sprite.setRegion(160, 0, 32, 32);
                  }
               } else if (this.timer >= 80) {
                  if (this.timer < 160) {
                     if (this.timer % 4 == 0) {
                        game.cam.translate(0.0F, 2.0F);
                        game.player.position.add(0.0F, 2.0F);
                        this.regiTile.sprite.translateY(2.0F);
                     } else if (this.timer % 4 == 2) {
                        game.cam.translate(0.0F, -2.0F);
                        game.player.position.add(0.0F, -2.0F);
                        this.regiTile.sprite.translateY(-2.0F);
                     }
                  } else if (this.timer < 220) {
                     if (this.timer % 6 == 0) {
                        this.pedistalTile.overSprite.setRegion(0, 0, 16, 16);
                     } else if (this.timer % 6 == 3) {
                        this.pedistalTile.overSprite.setRegion(this.regionNum, 0, 16, 16);
                     }
                  }
               }
            }

            if (this.timer == 1) {
               this.lightningSprite.setRegion(640, 0, 160, 144);
               this.soundEffect.stop();
               this.soundEffect.dispose();
               this.soundEffect2.stop();
               this.soundEffect2.dispose();
               if (this.regiName.equals("REGIDRAGO")) {
                  this.regionNum = 16;
               } else if (this.regiName.equals("REGIELEKI")) {
                  this.regionNum = 32;
               } else if (this.regiName.equals("REGICE")) {
                  this.regionNum = 48;
               } else if (this.regiName.equals("REGIROCK")) {
                  this.regionNum = 64;
               } else if (this.regiName.equals("REGISTEEL")) {
                  this.regionNum = 80;
               }
            } else if (this.timer == 60) {
               this.soundEffect = AudioLoader.loadMusic("attacks/thunderpunch_player_gsc/sound.ogg");
               this.soundEffect.setVolume(1.0F);
               this.soundEffect.play();
               SpriteProxy.inverseColors = true;
               this.lightningSprite.setRegion(0, 0, 160, 144);
               game.insertAction(new LightningFlash(null));
            } else if (this.timer == 62) {
               SpriteProxy.inverseColors = false;
               this.lightningSprite.setRegion(160, 0, 160, 144);
            } else if (this.timer == 64) {
               SpriteProxy.inverseColors = true;
               this.lightningSprite.setRegion(320, 0, 160, 144);
            } else if (this.timer == 68) {
               SpriteProxy.inverseColors = false;
               this.lightningSprite.setRegion(480, 0, 160, 144);
            } else if (this.timer == 80) {
               this.lightningSprite.setRegion(640, 0, 160, 144);
            } else if (this.timer == 160) {
               this.regiTile.sprite.setRegion(128, 0, 32, 32);
               this.soundEffect2 = AudioLoader.loadMusic("sounds/hit.ogg");
               this.soundEffect2.setVolume(1.0F);
               this.soundEffect2.play();
               String knockBackDir = "left";
               if (this.dirFacing.equals("right")) {
                  knockBackDir = "left";
               } else if (this.dirFacing.equals("up")) {
                  knockBackDir = "down";
               }

               game.player.dirFacing = this.dirFacing;
               game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
               game.insertAction(new PlayerKnockedBack(game, game.player, knockBackDir));
               this.regiTile.name = "cave1_regi2";
               this.pedistalTile.nameUpper = this.regiName;
               Texture text = TextureCache.get(Gdx.files.internal("tiles/cave1/overworld2.png"));
               this.pedistalTile.overSprite = new SmolSprite(text, this.regionNum, 0, 16, 16);
               this.pedistalTile.overSprite.setPosition(this.pedistalTile.position.x, this.pedistalTile.position.y);
            } else if (this.timer == 190) {
               game.playerCanMove = true;
            } else if (this.timer == 230) {
               this.soundEffect.stop();
               this.soundEffect.dispose();
               this.soundEffect2.stop();
               this.soundEffect2.dispose();
               game.actionStack.remove(this);
            }

            game.mapBatch.draw(this.lightningSprite, this.regiTile.position.x - 110.0F, this.regiTile.position.y - 94.0F);
         }
      } else {
         if (this.timer == 2) {
            this.soundEffect.stop();
            this.soundEffect.dispose();
         }

         if (this.timer == 20) {
            this.soundEffect = AudioLoader.loadMusic("sounds/gigas_noises9.ogg");
            this.soundEffect.setVolume(1.0F);
            this.soundEffect.play();
            this.regiTile.sprite.setRegion(192, 0, 32, 32);
         } else if (this.timer == 24) {
            this.regiTile.sprite.setRegion(224, 0, 32, 32);
         } else if (this.timer == 28) {
            this.regiTile.sprite.setRegion(256, 0, 32, 32);
         } else if (this.timer == 32) {
            this.regiTile.sprite.setRegion(288, 0, 32, 32);
         } else if (this.timer == 36) {
            this.regiTile.sprite.setRegion(128, 0, 32, 32);
         } else if (this.timer == 100) {
            this.phase++;
            this.timer = 0;
         }
      }

      this.timer++;
   }

   public static class BattleIntro extends Action {
      public int timer = 0;
      public Sprite sprite;

      public BattleIntro(Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("tiles/cave1/regi_eye1.png"));
         this.sprite = new Sprite(text, 0, 0, 16, 16);
      }

      @Override
      public void firstStep(Game game) {
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.gui_140;
      }

      @Override
      public void step(Game game) {
         if (this.timer > 0) {
            float alpha = 1.0F;
            if (this.timer < 2) {
               alpha = 0.3F;
            } else if (this.timer < 4) {
               alpha = 0.6F;
            }

            this.sprite.setAlpha(alpha);
            this.sprite.setPosition(48.0F, 8.0F);
            this.sprite.draw(game.uiBatch);
         }

         if (this.timer > 70) {
            float alpha = 1.0F;
            if (this.timer < 72) {
               alpha = 0.3F;
            } else if (this.timer < 74) {
               alpha = 0.6F;
            }

            this.sprite.setAlpha(alpha);
            this.sprite.setPosition(16.0F, 56.0F);
            this.sprite.draw(game.uiBatch);
         }

         if (this.timer > 140) {
            float alpha = 1.0F;
            if (this.timer < 142) {
               alpha = 0.3F;
            } else if (this.timer < 144) {
               alpha = 0.6F;
            }

            this.sprite.setAlpha(alpha);
            this.sprite.setPosition(48.0F, 104.0F);
            this.sprite.draw(game.uiBatch);
         }

         if (this.timer > 184) {
            float alpha = 1.0F;
            if (this.timer < 186) {
               alpha = 0.3F;
            } else if (this.timer < 188) {
               alpha = 0.6F;
            }

            this.sprite.setAlpha(alpha);
            game.uiBatch.draw(this.sprite, 96.0F, 104.0F);
         }

         if (this.timer > 218) {
            float alpha = 1.0F;
            if (this.timer < 220) {
               alpha = 0.3F;
            } else if (this.timer < 222) {
               alpha = 0.6F;
            }

            this.sprite.setAlpha(alpha);
            this.sprite.setPosition(128.0F, 56.0F);
            this.sprite.draw(game.uiBatch);
         }

         if (this.timer > 244) {
            float alpha = 1.0F;
            if (this.timer < 246) {
               alpha = 0.3F;
            } else if (this.timer < 248) {
               alpha = 0.6F;
            }

            this.sprite.setAlpha(alpha);
            this.sprite.setPosition(96.0F, 8.0F);
            this.sprite.draw(game.uiBatch);
         }

         if (this.timer >= 370) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }

   static class RocksEffect1 extends Action {
      public static float velocityX = 2.0F;
      public static boolean shouldMoveX = true;
      public static boolean shouldMoveY = true;
      public Action.Layer layer = Action.Layer.gui_111;
      Sprite textboxSprite;
      Sprite[] sprites = new Sprite[20];
      int[] velocities = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int[] velocities2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
      int velocity = 1;
      int whichVelocity = 0;
      Random rand = new Random();
      boolean firstStep = true;

      public RocksEffect1() {
         super();
         Texture text = TextureCache.get(Gdx.files.internal("battle/battle_bg4.png"));
         this.textboxSprite = new Sprite(text, 0, 0, 176, 160);
         this.textboxSprite.setPosition(-8.0F, -8.0F);
         text = TextureCache.get(Gdx.files.internal("battle/rock2.png"));
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
         this.sprites[10] = new Sprite(text, 32, 0, 32, 32);
         this.sprites[11] = new Sprite(text, 64, 0, 32, 32);
         this.sprites[12] = new Sprite(text, 160, 0, 32, 32);
         this.sprites[13] = new Sprite(text, 192, 0, 32, 32);
         this.sprites[14] = new Sprite(text, 32, 0, 32, 32);
         this.sprites[15] = new Sprite(text, 64, 0, 32, 32);
         this.sprites[16] = new Sprite(text, 160, 0, 32, 32);
         this.sprites[17] = new Sprite(text, 192, 0, 32, 32);
         this.sprites[18] = new Sprite(text, 0, 0, 32, 32);
         this.sprites[19] = new Sprite(text, 0, 0, 32, 32);

         for (int i = 0; i < 20; i++) {
            this.sprites[i].setPosition(this.rand.nextInt(192) - 32, this.rand.nextInt(144) - 144);
            this.sprites[i].setRotation(this.rand.nextInt(4) * 90);
            this.velocities[i] = 2;
            this.velocities2[i] = this.velocities[i] - 1;
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
            RegigigasIntroAnim.RocksEffect2.drawRocks = true;
            this.firstStep = false;
         }

         for (int i = 0; i < 20; i++) {
            if (this.whichVelocity == 0) {
               this.velocity = this.velocities[i];
            } else {
               this.velocity = this.velocities2[i];
            }

            if (!shouldMoveY) {
               this.velocity = 0;
            }

            velocityX = 0.0F;
            this.sprites[i].setPosition(this.sprites[i].getX() + velocityX, this.sprites[i].getY() + this.velocity);
            if (this.sprites[i].getY() > 144.0F) {
               this.sprites[i].setPosition(this.rand.nextInt(192) - 32, this.rand.nextInt(144) - 144);
            }

            if (this.sprites[i].getX() > 160.0F) {
               this.sprites[i].setPosition(-32.0F, this.sprites[i].getY());
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
