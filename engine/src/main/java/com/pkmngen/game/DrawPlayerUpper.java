package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

class DrawPlayerUpper extends Action {
   public Action.Layer layer = Action.Layer.map_115;
   Sprite spritePart = new Sprite();
   public static int offsetY = 0;
   public static int pokemonOffsetY = 0;
   public static boolean desertGrass = false;
   public static boolean drawTidal = false;
   public static int timer = 0;
   int fishingOffsetX = 0;
   int fishingOffsetY = 0;
   int floatOffset = 0;
   int floatTimer = 0;

   public DrawPlayerUpper() {
      super();
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (!game.player.isSitting) {
         if (game.player.isSleeping) {
            if (game.player.sleepingDir == null) {
               game.player.zSprite.draw(game.mapBatch);
               game.mapBatch.draw(game.player.sleepingSprite, game.player.position.x, game.player.position.y);
            } else {
               Vector2 pos = game.player.sleepingDir.cpy().sub(game.player.position);
               game.mapBatch.draw(game.player.zSprite, game.player.zSprite.getX() + pos.x - 12.0F, game.player.zSprite.getY() + pos.y + 16.0F);
               this.spritePart.set(game.player.currSprite);
               this.spritePart.setRegionY(game.player.spriteOffsetY);
               this.spritePart.setRegionHeight(6);
               game.mapBatch.draw(this.spritePart, game.player.sleepingDir.x, game.player.sleepingDir.y + 12.0F + 8.0F);
            }
         } else if (!game.player.currFieldMove.equals("FLY")) {
            if (game.player.drawSleepingBag) {
               game.mapBatch.draw(game.player.sleepingBagSprite, game.player.sleepingBagSprite.getX(), game.player.sleepingBagSprite.getY());
            }

            this.spritePart.set(game.player.currSprite);
            this.fishingOffsetX = 0;
            this.fishingOffsetY = 0;
            if (game.player.isFishing) {
               if (game.player.dirFacing.equals("left")) {
                  this.fishingOffsetX = -8;
               } else if (game.player.dirFacing.equals("down")) {
                  this.fishingOffsetY = -8;
               }
            }

            if (desertGrass) {
               this.spritePart.setRegionY(game.player.spriteOffsetY);
               this.spritePart.setRegionHeight((int)this.spritePart.getHeight() - 3);
               game.mapBatch
                  .draw(this.spritePart, game.player.position.x + this.fishingOffsetX, game.player.position.y + 7.0F + pokemonOffsetY + this.fishingOffsetY);
            } else if (drawTidal) {
               this.spritePart.setRegionY(game.player.spriteOffsetY);
               this.spritePart.setRegionHeight((int)this.spritePart.getHeight() - 3);
               game.mapBatch
                  .draw(this.spritePart, game.player.position.x + this.fishingOffsetX, game.player.position.y + 7.0F + pokemonOffsetY + this.fishingOffsetY);
            } else if (game.player.currFieldMove.equals("SURF") && game.player.hmPokemon != null) {
               int offsetY = 0;
               if (game.player.hmPokemon.specie.name.equals("lapras")) {
                  offsetY += 3;
               } else if (game.player.hmPokemon.specie.name.equals("araichu")) {
                  offsetY += 3;
               } else if (game.player.hmPokemon.specie.name.equals("raichu")) {
                  offsetY += 4;
               } else if (game.player.hmPokemon.specie.name.equals("pikachu")) {
                  offsetY += 4;
               } else if (game.player.hmPokemon.specie.name.equals("ludicolo")) {
                  offsetY -= 3;
               } else if (game.player.dirFacing.equals("left")) {
                  if (game.player.hmPokemon.specie.name.equals("feraligatr") || game.player.hmPokemon.specie.name.equals("kingdra")) {
                     offsetY += 2;
                  }
               } else if (game.player.dirFacing.equals("right")
                  && (game.player.hmPokemon.specie.name.equals("feraligatr") || game.player.hmPokemon.specie.name.equals("kingdra"))) {
                  offsetY += 2;
               }

               this.spritePart.setRegionHeight((int)this.spritePart.getHeight() - 6 + offsetY + this.floatOffset);
               game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y + 4.0F);
            } else {
               this.spritePart.setRegionY(game.player.spriteOffsetY);
               this.spritePart.setRegionHeight((int)this.spritePart.getHeight() - 8);
               game.mapBatch
                  .draw(this.spritePart, game.player.position.x + this.fishingOffsetX, game.player.position.y + 12.0F + pokemonOffsetY + this.fishingOffsetY);
            }

            if (game.player.currFieldMove.equals("RIDE") && game.player.hmPokemon != null) {
               int offsetX = 0;
               int offsetY = 0;
               if (game.player.dirFacing.equals("up")) {
                  offsetY -= 2;
                  if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                     offsetY += 5;
                  }

                  if (game.player.hmPokemon.specie.name.equals("probopass")) {
                     offsetY += 8;
                  }
               }

               if (game.player.dirFacing.equals("down")) {
                  offsetY += 3;
                  if (game.player.hmPokemon.specie.name.equals("ponyta") || game.player.hmPokemon.specie.name.equals("rapidash")) {
                     offsetY--;
                  }

                  if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                     offsetY += 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                     offsetY++;
                  }

                  if (game.player.hmPokemon.specie.name.equals("probopass")) {
                     offsetY += 4;
                  }
               } else if (game.player.dirFacing.equals("right")) {
                  offsetX -= 4;
                  if (game.player.hmPokemon.specie.name.equals("ninetales") || game.player.hmPokemon.specie.name.equals("arcanine")) {
                     offsetX++;
                  }

                  if (game.player.hmPokemon.specie.name.equals("ponyta") || game.player.hmPokemon.specie.name.equals("rapidash")) {
                     offsetX += 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                     offsetY += 5;
                     offsetX += 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("donphan")) {
                     offsetY += 2;
                     offsetX += 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                     offsetY++;
                     offsetX++;
                  }

                  if (game.player.hmPokemon.specie.name.equals("probopass")) {
                     offsetY += 7;
                     offsetX += 2;
                  }
               } else if (game.player.dirFacing.equals("left")) {
                  offsetX += 4;
                  if (game.player.hmPokemon.specie.name.equals("ninetales") || game.player.hmPokemon.specie.name.equals("arcanine")) {
                     offsetX--;
                  }

                  if (game.player.hmPokemon.specie.name.equals("ponyta") || game.player.hmPokemon.specie.name.equals("rapidash")) {
                     offsetX -= 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("mamoswine")) {
                     offsetY += 5;
                     offsetX -= 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("donphan")) {
                     offsetY += 2;
                     offsetX -= 2;
                  }

                  if (game.player.hmPokemon.specie.name.equals("rhyhorn")) {
                     offsetY++;
                     offsetX--;
                  }

                  if (game.player.hmPokemon.specie.name.equals("probopass")) {
                     offsetY += 7;
                     offsetX -= 2;
                  }
               }

               game.player.hmPokemon.currOwSprite = game.player.hmPokemon.standingSprites.get(game.player.dirFacing);
               this.spritePart = new Sprite(game.player.hmPokemon.currOwSprite);
               this.spritePart.setRegionY(game.player.hmPokemon.spriteOffsetY);
               this.spritePart.setRegionHeight(14);
               game.mapBatch
                  .draw(this.spritePart, game.player.position.x + offsetX, game.player.position.y + 10.0F + offsetY + DrawPlayerUpper.offsetY + pokemonOffsetY);
               if (game.player.dirFacing.equals("down")
                  && !game.player.hmPokemon.specie.name.equals("rhyhorn")
                  && !game.player.hmPokemon.specie.name.equals("probopass")) {
                  this.spritePart.set(game.player.currSprite);
                  this.spritePart.setRegionY(game.player.spriteOffsetY);
                  this.spritePart.setRegionHeight(8);
                  game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y + 12.0F + pokemonOffsetY);
               }
            } else if (game.player.currFieldMove.equals("SURF") && game.player.hmPokemon != null) {
               this.floatTimer++;
               if (this.floatTimer > 80 || DrawPlayerUpper.offsetY < 0) {
                  this.floatTimer = 0;
               }

               if (this.floatTimer % 80 == 0) {
                  this.floatOffset = 0;
               } else if (this.floatTimer % 80 == 40) {
                  this.floatOffset = 1;
               }

               int offsetX = 0;
               int offsetY = 0;
               int regionHeight = 14;
               if (game.player.dirFacing.equals("up")) {
                  offsetY--;
               }

               if (game.player.dirFacing.equals("down")) {
                  offsetY += 2;
                  if (game.player.hmPokemon.specie.name.equals("ludicolo")) {
                     offsetY -= 2;
                  }
               } else if (game.player.dirFacing.equals("right")) {
                  offsetX -= 4;
                  if (game.player.hmPokemon.specie.name.equals("raichu")) {
                     offsetX--;
                  } else if (game.player.hmPokemon.specie.name.equals("pikachu")) {
                     offsetX--;
                  } else if (game.player.hmPokemon.specie.name.equals("ludicolo")) {
                     offsetX += 4;
                  } else if (game.player.hmPokemon.specie.name.equals("sharpedo")) {
                     offsetX += 2;
                  }
               } else if (game.player.dirFacing.equals("left")) {
                  offsetX += 4;
                  if (game.player.hmPokemon.specie.name.equals("raichu")) {
                     offsetX++;
                  } else if (game.player.hmPokemon.specie.name.equals("pikachu")) {
                     offsetX++;
                  } else if (game.player.hmPokemon.specie.name.equals("ludicolo")) {
                     offsetX -= 4;
                  } else if (game.player.hmPokemon.specie.name.equals("sharpedo")) {
                     offsetX -= 2;
                  }
               }

               if (game.player.hmPokemon.specie.name.equals("araichu")) {
                  regionHeight = 15;
                  offsetY -= 2;
               } else if (game.player.hmPokemon.specie.name.equals("raichu")) {
                  regionHeight = 15;
                  offsetY -= 2;
               } else if (game.player.hmPokemon.specie.name.equals("pikachu")) {
                  regionHeight = 15;
                  offsetY -= 2;
               }

               game.player.hmPokemon.currOwSprite = game.player.hmPokemon.standingSprites.get(game.player.dirFacing);
               this.spritePart.set(game.player.hmPokemon.currOwSprite);
               this.spritePart.setRegionY(game.player.hmPokemon.spriteOffsetY);
               this.spritePart.setRegionHeight(regionHeight + this.floatOffset);
               game.mapBatch.draw(this.spritePart, game.player.position.x + offsetX, game.player.position.y + 8.0F + offsetY + DrawPlayerUpper.offsetY);
               if (game.player.dirFacing.equals("down") && !game.player.hmPokemon.specie.name.equals("ludicolo")) {
                  int var8 = 0;
                  if (game.player.hmPokemon.specie.name.equals("lapras")) {
                     var8 += 3;
                  } else if (game.player.hmPokemon.specie.name.equals("araichu")) {
                     var8 += 3;
                  } else if (game.player.hmPokemon.specie.name.equals("raichu")) {
                     var8 += 4;
                  } else if (game.player.hmPokemon.specie.name.equals("pikachu")) {
                     var8 += 4;
                  }

                  this.spritePart.set(game.player.currSprite);
                  this.spritePart.setRegionHeight((int)this.spritePart.getHeight() - 6 + var8 + this.floatOffset);
                  game.mapBatch.draw(this.spritePart, game.player.position.x, game.player.position.y + 4.0F);
               }
            }

            if (desertGrass) {
               if (timer < 6) {
                  if (timer == 0) {
                     Player.currGrassOverSprite.setRegion(16, 0, 16, 16);
                     game.insertAction(new PlayMusic("sand2", 0.4F, true, null));
                  }

                  game.mapBatch.draw(Player.currGrassOverSprite, game.player.position.x, game.player.position.y + 7.0F);
               } else if (timer < 12) {
                  if (timer == 6) {
                     Player.currGrassOverSprite.setRegion(0, 0, 16, 16);
                  }

                  game.mapBatch.draw(Player.currGrassOverSprite, game.player.position.x, game.player.position.y + 7.0F);
               } else if (timer < 16) {
                  if (timer == 12) {
                     Player.currGrassOverSprite.setRegion(16, 0, 16, 16);
                  }

                  game.mapBatch.draw(Player.currGrassOverSprite, game.player.position.x, game.player.position.y + 7.0F);
               }

               if (timer < 16) {
                  timer++;
               }
            }
         }
      }
   }
}
