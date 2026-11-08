package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;
import java.util.Map;

class DrawGhost extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   Sprite currSprite;
   Vector2 basePos;
   float velX;
   float velY;
   float maxVel;
   float sineTimer;
   String dirFacing;
   Map<String, Sprite[]> sprites;
   int animIndex;
   int animFrame;
   Pokemon pokemon;
   boolean inBattle = false;
   int noEncounterTimer = 0;
   Vector2 startPos;
   Vector2 endPos;
   boolean graveyardSpawn = false;

   public DrawGhost(Game game, Vector2 position) {
      this(game, position, false);
   }

   public DrawGhost(Game game, Vector2 position, boolean graveyardSpawn) {
      super();
      this.basePos = position;
      this.sineTimer = 0.0F;
      this.dirFacing = "down";
      this.sprites = new HashMap<>();
      this.animIndex = 0;
      this.animFrame = 0;
      this.maxVel = 1.2F;
      this.graveyardSpawn = graveyardSpawn;
      String[] pokemon = new String[]{"litwick", "lampent", "chandelure", "mimikyu", "misdreavus", "sableye", "gastly", "haunter", "gengar"};
      this.pokemon = new Pokemon(pokemon[Game.rand.nextInt(pokemon.length)], 21);
      this.pokemon.spookify();
      Texture ghostTexture = TextureCache.get(Gdx.files.internal("ghost_sheet1.png"));
      this.sprites
         .put(
            "down",
            new Sprite[]{
               new Sprite(ghostTexture, 0, 0, 32, 32),
               new Sprite(ghostTexture, 32, 0, 32, 32),
               new Sprite(ghostTexture, 0, 0, 32, 32),
               new Sprite(ghostTexture, 32, 0, 32, 32),
               new Sprite(ghostTexture, 0, 0, 32, 32),
               new Sprite(ghostTexture, 32, 0, 32, 32),
               new Sprite(ghostTexture, 64, 0, 32, 32),
               new Sprite(ghostTexture, 96, 0, 32, 32)
            }
         );
      this.sprites
         .put(
            "left",
            new Sprite[]{
               new Sprite(ghostTexture, 128, 0, 32, 32),
               new Sprite(ghostTexture, 160, 0, 32, 32),
               new Sprite(ghostTexture, 128, 0, 32, 32),
               new Sprite(ghostTexture, 160, 0, 32, 32),
               new Sprite(ghostTexture, 128, 0, 32, 32),
               new Sprite(ghostTexture, 160, 0, 32, 32),
               new Sprite(ghostTexture, 192, 0, 32, 32),
               new Sprite(ghostTexture, 224, 0, 32, 32)
            }
         );
      this.sprites
         .put(
            "up",
            new Sprite[]{
               new Sprite(ghostTexture, 256, 0, 32, 32),
               new Sprite(ghostTexture, 288, 0, 32, 32),
               new Sprite(ghostTexture, 256, 0, 32, 32),
               new Sprite(ghostTexture, 288, 0, 32, 32),
               new Sprite(ghostTexture, 256, 0, 32, 32),
               new Sprite(ghostTexture, 288, 0, 32, 32),
               new Sprite(ghostTexture, 320, 0, 32, 32),
               new Sprite(ghostTexture, 352, 0, 30, 32)
            }
         );
      Sprite temp = new Sprite(ghostTexture, 128, 0, 32, 32);
      temp.flip(true, false);
      Sprite temp2 = new Sprite(ghostTexture, 160, 0, 32, 32);
      temp2.flip(true, false);
      Sprite temp3 = new Sprite(ghostTexture, 192, 0, 32, 32);
      temp3.flip(true, false);
      Sprite temp4 = new Sprite(ghostTexture, 224, 0, 32, 32);
      temp4.flip(true, false);
      this.sprites.put("right", new Sprite[]{temp, temp2, temp, temp2, temp, temp2, temp3, temp4});
      this.currSprite = this.sprites.get(this.dirFacing)[this.animIndex];
      this.currSprite.setPosition(position.x, position.y);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (!game.map.timeOfDay.equals("night")) {
         this.currSprite.draw(game.mapBatch);
         game.actionStack.remove(this);
         game.insertAction(new DespawnGhost(this.basePos.cpy()));
         game.musicController.nightAlert = false;
         game.musicController.resumeOverworldMusic = true;
      } else {
         boolean hasAlivePokemon = false;

         for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.currentStats.get("hp") > 0 && !pokemon.isEgg) {
               hasAlivePokemon = true;
               break;
            }
         }

         if (!hasAlivePokemon || !this.inBattle && this.pokemon.currentStats.get("hp") <= 0 || this.pokemon.previousOwner != null) {
            this.currSprite.draw(game.mapBatch);
            game.actionStack.remove(this);
            game.insertAction(new DespawnGhost(this.basePos.cpy()));
            boolean foundGhost = false;

            for (Action action : game.actionStack) {
               if (DrawGhost.class.isInstance(action)) {
                  foundGhost = true;
                  break;
               }
            }

            if (!foundGhost && hasAlivePokemon) {
               game.musicController.nightAlert = false;
               game.musicController.resumeOverworldMusic = true;
            }
         } else if (!game.playerCanMove) {
            this.currSprite.draw(game.mapBatch);
         } else if (this.inBattle) {
            this.pokemon = game.battle.oppPokemon;
            if (this.noEncounterTimer % 4 >= 2) {
               this.currSprite.draw(game.mapBatch);
            }

            if (this.noEncounterTimer < 128) {
               this.noEncounterTimer++;
            } else {
               this.inBattle = false;
            }
         } else {
            boolean foundCampfire = false;
            this.startPos = this.basePos.cpy().add(-64.0F, -64.0F);
            this.startPos.x = (int)this.startPos.x - Math.floorMod((int)this.startPos.x, 16);
            this.startPos.y = (int)this.startPos.y - Math.floorMod((int)this.startPos.y, 16);
            this.endPos = this.basePos.cpy().add(64.0F, 64.0F);
            this.endPos.x = (int)this.endPos.x - Math.floorMod((int)this.endPos.x, 16);
            this.endPos.y = (int)this.endPos.y - Math.floorMod((int)this.endPos.y, 16);
            Vector2 currPos = this.startPos.cpy();

            while (currPos.y < this.endPos.y) {
               Tile tile = game.map.tiles.get(currPos);
               currPos.x += 16.0F;
               if (currPos.x > this.endPos.x) {
                  currPos.x = this.startPos.x;
                  currPos.y += 16.0F;
               }

               if (tile != null) {
                  if (tile.nameUpper.contains("campfire")) {
                     foundCampfire = true;
                     break;
                  }

                  if (tile.items != null && tile.items.containsKey("torch") && this.basePos.dst2(tile.position) < 1024.0F) {
                     foundCampfire = true;
                     break;
                  }

                  Pokemon pokemon = game.map.pokemon.get(tile.position);
                  if (pokemon != null && pokemon.hms.contains("FLASH")) {
                     foundCampfire = true;
                     break;
                  }
               }
            }

            if (game.player.hmPokemon != null && this.basePos.dst2(game.player.position) < 4096.0F && game.player.hmPokemon.hms.contains("FLASH")) {
               foundCampfire = true;
            }

            if (foundCampfire) {
               this.currSprite.draw(game.mapBatch);
               game.actionStack.remove(this);
               game.insertAction(new DespawnGhost(this.basePos.cpy()));
               game.musicController.nightAlert = false;
               game.musicController.resumeOverworldMusic = true;
            } else {
               float dx = this.basePos.x - game.player.position.x;
               float dy = this.basePos.y - game.player.position.y;
               if (dx < dy) {
                  if (game.player.position.y < this.basePos.y) {
                     this.dirFacing = "down";
                  } else {
                     this.dirFacing = "right";
                  }
               } else if (game.player.position.x < this.basePos.x) {
                  this.dirFacing = "left";
               } else {
                  this.dirFacing = "up";
               }

               this.currSprite = this.sprites.get(this.dirFacing)[this.animIndex];
               if (this.dirFacing.equals("left")) {
                  if (this.velX > -this.maxVel) {
                     this.velX -= 0.1F;
                  }

                  this.velY = this.velY - this.velY / 16.0F;
               } else if (this.dirFacing.equals("right")) {
                  if (this.velX < this.maxVel) {
                     this.velX += 0.1F;
                  }

                  this.velY = this.velY - this.velY / 16.0F;
               } else if (this.dirFacing.equals("down")) {
                  if (this.velY > -this.maxVel) {
                     this.velY -= 0.1F;
                  }

                  this.velX = this.velX - this.velX / 16.0F;
               } else if (this.dirFacing.equals("up")) {
                  if (this.velY < this.maxVel) {
                     this.velY += 0.1F;
                  }

                  this.velX = this.velX - this.velX / 16.0F;
               }

               this.basePos.add(this.velX, this.velY);
               float shiftPosX = (float)Math.sin(this.sineTimer);
               float shiftPosY = (float)Math.sin(2.0F * this.sineTimer);
               this.currSprite.setPosition(this.basePos.x + shiftPosX, this.basePos.y + shiftPosY);
               this.currSprite.draw(game.mapBatch);
               this.sineTimer += 0.125F;
               if (this.sineTimer >= 6.28) {
                  this.sineTimer = 0.0F;
               }

               this.animFrame++;
               if (this.animFrame > 10) {
                  this.animFrame = 0;
                  this.animIndex++;
               }

               if (this.animIndex >= this.sprites.get(this.dirFacing).length) {
                  this.animIndex = 0;
               }

               game.player.currSprite.setPosition(game.player.position.x, game.player.position.y);
               if (!game.player.currFieldMove.equals("FLY")
                  && game.player.currSprite.getBoundingRectangle().contains(this.basePos.x + 16.0F, this.basePos.y + 16.0F)) {
                  if (game.player.isSleeping) {
                     game.player.isSleeping = false;
                     if (game.player.sleepingDir == null) {
                        game.player.drawSleepingBag = false;
                        game.player.acceptInput = true;
                     } else {
                        game.player.sleepingDir = null;
                     }
                  }

                  if (game.player.acceptInput) {
                     this.inBattle = true;
                     this.noEncounterTimer = 0;
                     game.battle.oppPokemon = this.pokemon;
                     game.playerCanMove = false;
                     game.musicController.inBattle = true;
                     game.player.setCurrPokemon();
                     game.insertAction(Battle.getIntroAction(game));
                     if (game.type == Game.Type.CLIENT) {
                        game.client.sendTCP(new Network.BattleData(this.pokemon, game.player.network.id));
                     }
                  }
               }
            }
         }
      }
   }
}
