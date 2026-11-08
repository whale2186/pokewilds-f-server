package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

class PlayerRunning extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   Vector2 initialPos;
   int timer = 0;
   Vector2 targetPos;
   float xDist;
   float yDist;
   boolean alternate = false;
   boolean firstStep = true;
   Player player;

   public PlayerRunning(Game game, boolean alternate) {
      this(game, game.player, alternate);
   }

   public PlayerRunning(Game game, Player player, boolean alternate) {
      super();
      this.alternate = alternate;
      this.player = player;
      this.player.currState = "Running";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void firstStep(Game game) {
      this.initialPos = new Vector2(this.player.position);
      Tile startTile = game.map.tiles.get(this.initialPos);
      if (this.player.dirFacing.equals("up")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y + 16.0F);
      } else if (this.player.dirFacing.equals("down")) {
         this.targetPos = new Vector2(this.player.position.x, this.player.position.y - 16.0F);
      } else if (this.player.dirFacing.equals("left")) {
         this.targetPos = new Vector2(this.player.position.x - 16.0F, this.player.position.y);
      } else if (this.player.dirFacing.equals("right")) {
         this.targetPos = new Vector2(this.player.position.x + 16.0F, this.player.position.y);
      }

      if (this.player.hmPokemon != null) {
         this.player.hmPokemon.moveDirs.add(this.player.dirFacing);
         this.player.hmPokemon.numMoves.add(1.6F);
      }

      if (startTile != null && startTile.leavesFootprint()) {
         game.insertAction(this.player.new FootPrintFade(this.player.dirFacing, this.initialPos, null));
      }

      if (startTile != null && startTile.name.contains("puddle1")) {
         game.insertAction(new PlayMusic("puddle8", 0.2F, true, null));
         game.insertAction(game.player.new WaterSplash(true, new Vector2(0.0F, 0.0F), null));
      }

      if (startTile != null && startTile.isTidal) {
         game.insertAction(new PlayMusic("puddle11", 0.2F, true, null));
         game.insertAction(new PlayMusic("puddle9", 0.2F, true, null));
         game.insertAction(game.player.new WaterSplash(true, new Vector2(0.0F, 4.0F), null));
      }
   }

   public void localStep(Game game) {
      if (game.playerCanMove) {
         float speed = 1.6F;
         if (game.player.currFieldMove.equals("RIDE")) {
            speed = 2.0F;
         }

         if (game.debugInputEnabled && Gdx.input.isKeyPressed(62)) {
            speed = 8.0F;
         }

         this.timer = 20;
         if (game.player.dirFacing.equals("up")) {
            this.player.position.y += speed;
            game.cam.position.y += speed;
         } else if (game.player.dirFacing.equals("down")) {
            game.player.position.y -= speed;
            game.cam.position.y -= speed;
         } else if (game.player.dirFacing.equals("left")) {
            game.player.position.x -= speed;
            game.cam.position.x -= speed;
         } else if (game.player.dirFacing.equals("right")) {
            game.player.position.x += speed;
            game.cam.position.x += speed;
         }

         this.xDist = Math.abs(this.initialPos.x - game.player.position.x);
         this.yDist = Math.abs(this.initialPos.y - game.player.position.y);
         String spriteString = String.valueOf(game.player.dirFacing + "_running");
         if (!game.player.currFieldMove.equals("RIDE")) {
            if (this.yDist < 13.0F && this.yDist > 2.0F || this.xDist < 13.0F && this.xDist > 2.0F) {
               if (this.alternate) {
                  game.player.currSprite = game.player.altMovingSprites.get(spriteString);
                  if (game.player.currFieldMove.equals("RIDE")) {
                     DrawPlayerUpper.offsetY = -1;
                     DrawPlayerUpper.pokemonOffsetY = 2;
                  }
               } else {
                  game.player.currSprite = game.player.movingSprites.get(spriteString);
                  if (game.player.currFieldMove.equals("RIDE")) {
                     DrawPlayerUpper.offsetY = -1;
                     DrawPlayerUpper.pokemonOffsetY = 2;
                  }
               }
            } else {
               game.player.currSprite = game.player.standingSprites.get(spriteString);
               DrawPlayerUpper.offsetY = 0;
               DrawPlayerUpper.pokemonOffsetY = 0;
            }
         } else if (!this.alternate) {
            if (this.firstStep) {
               game.insertAction(new PlayMusic("ride1", 1.0F, true, null));
               this.firstStep = false;
            }

            game.player.currSprite = game.player.altMovingSprites.get(spriteString);
            DrawPlayerUpper.offsetY = -1;
            DrawPlayerUpper.pokemonOffsetY = 2;
            if (this.yDist > 10.0F || this.xDist > 10.0F) {
               DrawPlayerUpper.offsetY = 0;
            }
         } else {
            game.player.currSprite = game.player.standingSprites.get(spriteString);
            DrawPlayerUpper.offsetY = 0;
            DrawPlayerUpper.pokemonOffsetY = 0;
         }

         if ((int)this.xDist == 6 || (int)this.yDist == 6) {
            Tile currTile = game.map.tiles.get(this.targetPos);
            if (currTile != null && currTile.nameUpper.contains("gate")) {
               currTile.overSprite = null;
               game.insertAction(new PlayMusic("gate1", null));
            }
         } else if ((int)this.xDist == 12 || (int)this.yDist == 12) {
            Tile currTile = game.map.tiles.get(this.initialPos);
            if (currTile != null && currTile.nameUpper.contains("gate")) {
               currTile.init(currTile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo);
               game.insertAction(new PlayMusic("gate2", null));
            }
         }

         if ((this.xDist >= 16.0F || this.yDist >= 16.0F) && this.timer >= 14) {
            DrawPlayerUpper.offsetY = 0;
            DrawPlayerUpper.pokemonOffsetY = 0;
            Tile targetTile = game.map.tiles.get(this.targetPos);
            if (targetTile != null && (targetTile.name.contains("puddle1") || targetTile.isTidal)) {
               Vector2 offset = new Vector2(0.0F, 0.0F);
               Player.PuddleSplash puddleSplash = game.player.new PuddleSplash(offset, null);
               if (targetTile.isTidal) {
                  offset.set(0.0F, 4.0F);
                  puddleSplash.layer = Action.Layer.map_114;
               }

               game.insertAction(puddleSplash);
               game.insertAction(game.player.new WaterSplash(false, offset, null));
            }

            if (targetTile != null && targetTile.routeBelongsTo != null) {
               Route newRoute = targetTile.routeBelongsTo;
               String newBiome = targetTile.biome;
               if (!game.map.currRoute.type().equals(newRoute.type()) && !game.map.timeOfDay.equals("night")) {
                  game.musicController.fadeToDungeon = true;
                  System.out.println("game.player.enteredDesertBiome");
                  System.out.println(game.player.enteredDesertBiome);
                  if (newRoute.type().equals("desert") && !game.player.enteredDesertBiome) {
                     game.player.enteredDesertBiome = true;
                     game.player.canMove = false;
                     game.insertAction(
                        game.player.new Emote(
                           "!",
                           new DisplayText(
                              game, "The sun is scorching hot! This region feels dangerous...", null, null, new RunCode(() -> game.player.canMove = true, null)
                           )
                        )
                     );
                  }

                  if (newRoute.type().equals("graveyard")) {
                     game.insertAction(game.map.new ShadeEffect(newRoute.type()));
                  } else {
                     game.insertAction(game.map.new ShadeEffect(newRoute.type()));
                  }
               }

               if (game.map.currRoute != newRoute) {
                  if (game.map.currRoute.name.equals(newRoute.name)) {
                     newRoute.music = game.map.currRoute.music;
                  } else {
                     newRoute.music = game.map.currRoute.music;
                  }

                  game.map.currRoute = newRoute;
               }

               game.map.currBiome = newBiome;
            }

            game.player.position.set(this.targetPos);
            game.cam.position.set(this.targetPos.x + 16.0F, this.targetPos.y, 0.0F);
            game.player.isNearCampfire = game.player.checkNearCampfire();
            game.actionStack.remove(this);
            Action standingAction = new PlayerStanding(game, !this.alternate, true);
            game.insertAction(standingAction);
            standingAction.step(game);
         }
      }
   }

   public void remoteStep(Game game) {
      this.player.network.syncTimer++;
      float speed = 1.6F;
      if (this.player.dirFacing.equals("up")) {
         this.player.position.y += speed;
      } else if (this.player.dirFacing.equals("down")) {
         this.player.position.y -= speed;
      } else if (this.player.dirFacing.equals("left")) {
         this.player.position.x -= speed;
      } else if (this.player.dirFacing.equals("right")) {
         this.player.position.x += speed;
      }

      this.xDist = Math.abs(this.initialPos.x - this.player.position.x);
      this.yDist = Math.abs(this.initialPos.y - this.player.position.y);
      String spriteString = String.valueOf(this.player.dirFacing + "_running");
      if (this.yDist < 13.0F && this.yDist > 2.0F || this.xDist < 13.0F && this.xDist > 2.0F) {
         if (this.alternate) {
            this.player.currSprite = this.player.altMovingSprites.get(spriteString);
         } else {
            this.player.currSprite = this.player.movingSprites.get(spriteString);
         }
      } else {
         this.player.currSprite = this.player.standingSprites.get(spriteString);
      }

      if (this.xDist >= 16.0F || this.yDist >= 16.0F) {
         this.player.position.set(this.targetPos);
         this.player.standingAction.alternate = !this.alternate;
         this.player.standingAction.isRunning = true;
         game.insertAction(this.player.standingAction);
         this.player.standingAction.step(game);
         game.actionStack.remove(this);
      }
   }

   @Override
   public void step(Game game) {
      if (this.player.type == Player.Type.LOCAL) {
         this.localStep(game);
      } else if (this.player.type == Player.Type.REMOTE) {
         this.remoteStep(game);
      }
   }
}
