package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;
import java.util.Locale;

class FossilMachinePowerUp extends Action {
   int timer = 0;
   ArrayList<Tile> buildingTiles = new ArrayList<>();
   Music soundEffect;
   boolean outro;

   public FossilMachinePowerUp(boolean outro, Action nextAction) {
      super();
      this.outro = outro;
      this.nextAction = nextAction;
   }

   @Override
   public void firstStep(Game game) {
      for (Tile tile : game.map.tiles.values()) {
         if (tile.name.contains("building1") && !tile.name.contains("building1_machine") && !tile.name.equals("building1_pokecenter1_right")) {
            this.buildingTiles.add(tile);
         }
      }

      if (!this.outro) {
         game.loadedMusic.put("para1", AudioLoader.loadMusic("sounds/para1.ogg"));
      } else {
         for (Tile tile : this.buildingTiles) {
            tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
         }

         game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   @Override
   public void step(Game game) {
      for (Tile tile : this.buildingTiles) {
         if (this.timer < 50) {
            if (this.timer % 16 == 0) {
               tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
               if (tile.overSprite != null) {
                  tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
               }

               game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
            }

            if (this.timer % 16 == 13) {
               game.insertAction(new PlayMusic("para1", 0.7F, true, null));
               tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
               if (tile.overSprite != null) {
                  tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
               }

               game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            }
         } else if (this.timer < 90) {
            if (this.timer % 4 == 0) {
               tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
               if (tile.overSprite != null) {
                  tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
               }

               game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
            }

            if (this.timer % 4 == 2) {
               tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
               if (tile.overSprite != null) {
                  tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
               }

               game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            }
         }
      }

      if (this.timer >= 70) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
   }

   static class DoRevive extends Action {
      int timer = 0;
      int phase = 0;
      Tile machineTile;
      String pokemonName;
      Sprite fossilSprite;
      Music soundEffect;
      int offsetY = 0;
      int regionOffsetY = 0;

      public DoRevive(String pokemonName, Action nextAction) {
         super();
         this.pokemonName = pokemonName;
         this.fossilSprite = new Sprite(TextureCache.get(Gdx.files.internal("tiles/fossil1.png")), 0, 0, 16, 16);
         this.nextAction = nextAction;
         if (pokemonName.equals("OMANYTE")) {
            this.regionOffsetY = 16;
         } else if (pokemonName.equals("KABUTO")) {
            this.regionOffsetY = 32;
         } else if (pokemonName.equals("AERODACTYL")) {
            this.regionOffsetY = 0;
         } else if (pokemonName.equals("LILEEP")) {
            this.regionOffsetY = 48;
         } else if (pokemonName.equals("ANORITH")) {
            this.regionOffsetY = 64;
         } else if (pokemonName.equals("SHIELDON")) {
            this.regionOffsetY = 80;
         } else if (pokemonName.equals("CRANIDOS")) {
            this.regionOffsetY = 96;
         }

         this.fossilSprite.setRegion(0, this.regionOffsetY, 16, 16);
      }

      @Override
      public void firstStep(Game game) {
         for (Tile tile : game.map.tiles.values()) {
            if (tile.name.equals("building1_pokecenter1")) {
               this.machineTile = tile;
               break;
            }
         }

         this.soundEffect = AudioLoader.loadMusic("sounds/machine1.ogg");
         this.soundEffect.setLooping(true);
         this.soundEffect.setVolume(0.7F);
      }

      @Override
      public String getCamera() {
         return "map";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.map_0;
      }

      @Override
      public void step(Game game) {
         if (this.phase == 0) {
            if (this.timer == 120) {
               this.phase++;
               this.timer = 0;
            }
         } else if (this.phase == 1) {
            if (this.timer == 1) {
               this.soundEffect.play();
            }

            if (this.timer < 362) {
               if (this.timer % 4 == 0) {
                  game.cam.translate(0.0F, 2.0F);
                  game.player.position.add(0.0F, 2.0F);
                  if (game.player.hmPokemon != null) {
                     game.player.hmPokemon.position.add(0.0F, 2.0F);
                  }
               } else if (this.timer % 4 == 1) {
                  this.offsetY = 0;
               } else if (this.timer % 4 == 2) {
                  game.cam.translate(0.0F, -2.0F);
                  game.player.position.add(0.0F, -2.0F);
                  if (game.player.hmPokemon != null) {
                     game.player.hmPokemon.position.add(0.0F, -2.0F);
                  }
               } else if (this.timer % 4 == 3) {
                  this.offsetY = -2;
               }

               if (this.timer % 8 == 0) {
                  this.fossilSprite.setRegion(0, this.regionOffsetY, 16, 16);
               } else if (this.timer % 8 == 4) {
                  this.fossilSprite.setRegion(16, this.regionOffsetY, 16, 16);
               }

               if (this.timer % 16 == 0) {
                  game.insertAction(new PlayMusic("teleport1", 0.5F, true, null));
               }
            } else {
               this.soundEffect.stop();
               this.soundEffect.dispose();
               this.machineTile.nameUpper = "revived_" + this.pokemonName;
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
               Pokemon tempPokemon = new Pokemon(this.pokemonName.toLowerCase(Locale.ROOT), 2, Pokemon.Generation.CRYSTAL, false, false);
               game.insertAction(
                  new WaitFrames(
                     game,
                     90,
                     new PokemonFrame(
                        tempPokemon,
                        new WaitFrames(
                           game,
                           20,
                           new SplitAction(
                              new FossilMachinePowerUp.LightFlicker(null),
                              new PlayMusic(
                                 "heal1_downshift",
                                 new SplitAction(
                                    new PlayMusic("stomp1", null),
                                    new FossilMachinePowerUp(
                                       true,
                                       new SetField(
                                          game,
                                          "playerCanMove",
                                          true,
                                          new SetField(game.player, "isCrafting", false, new SetField(game.player.hmPokemon, "dirFacing", "left", null))
                                       )
                                    )
                                 )
                              )
                           )
                        )
                     )
                  )
               );
            }
         }

         game.mapBatch.draw(this.fossilSprite, this.machineTile.position.x + 8.0F, this.machineTile.position.y + 8.0F + this.offsetY);
         this.timer++;
      }
   }

   static class LightFlicker extends Action {
      int timer = 0;
      int phase = 0;
      ArrayList<Tile> buildingTiles = new ArrayList<>();

      public LightFlicker(Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         for (Tile tile : game.map.tiles.values()) {
            if (tile.name.contains("building1") && !tile.name.contains("building1_machine") && !tile.name.equals("building1_pokecenter1_right")) {
               this.buildingTiles.add(tile);
            }
         }
      }

      @Override
      public String getCamera() {
         return "map";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.map_0;
      }

      @Override
      public void step(Game game) {
         if (this.phase == 0) {
            if (this.timer < 24) {
               if (this.timer % 24 == 0) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (this.timer % 24 == 12) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
               }
            } else {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 1) {
            if (this.timer < 40) {
               if (this.timer % 40 == 0) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (this.timer % 40 == 20) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
               }
            } else {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 2) {
            if (this.timer < 48) {
               if (this.timer % 48 == 0) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (this.timer % 48 == 24) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
               }
            } else {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 3) {
            if (this.timer < 48) {
               if (this.timer % 48 == 0) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (this.timer % 48 == 24) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
               }
            } else {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 4) {
            if (this.timer < 56) {
               if (this.timer % 56 == 0) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion(0, 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (this.timer % 56 == 28) {
                  for (Tile tile : this.buildingTiles) {
                     tile.sprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.sprite.getHeight());
                     if (tile.overSprite != null) {
                        tile.overSprite.setRegion((int)tile.sprite.getHeight(), 0, (int)tile.sprite.getHeight(), (int)tile.overSprite.getHeight());
                     }
                  }

                  game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
               }
            } else {
               this.phase++;
               this.timer = 0;
            }
         }

         if (this.phase == 5) {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         this.timer++;
      }
   }
}
