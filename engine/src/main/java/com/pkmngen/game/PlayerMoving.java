package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;

class PlayerMoving extends Action {
   public Action.Layer layer = Action.Layer.map_114;
   Vector2 initialPos;
   Vector2 targetPos;
   float xDist;
   float yDist;
   boolean alternate = false;
   Player player;

   public PlayerMoving(Game game, boolean alternate) {
      this(game, game.player, alternate);
   }

   public PlayerMoving(Game game, Player player, boolean alternate) {
      this(game, player, alternate, new PlayerStanding(game, !alternate));
   }

   public PlayerMoving(Game game, Player player, boolean alternate, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.alternate = alternate;
      this.player = player;
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
         this.player.hmPokemon.numMoves.add(1.0F);
      }

      Tile targetTile = game.map.tiles.get(this.targetPos);
      if (targetTile == null || !targetTile.name.contains("desert2") && !targetTile.name.equals("soot1") && !targetTile.name.equals("snow3")) {
         if (startTile != null && (startTile.name.contains("desert2") || startTile.name.equals("soot1") || startTile.name.equals("snow3"))) {
            DrawPlayerUpper.desertGrass = true;
            Player.currGrassOverSprite = Player.snowDriftSprite;
            if (startTile.name.contains("desert2")) {
               Player.currGrassOverSprite = Player.desertGrassSprite;
            } else if (startTile.name.equals("soot1")) {
               Player.currGrassOverSprite = Player.sootDriftSprite;
            }
         } else {
            DrawPlayerUpper.desertGrass = false;
         }
      } else {
         DrawPlayerUpper.desertGrass = true;
         Player.currGrassOverSprite = Player.snowDriftSprite;
         if (targetTile.name.contains("desert2")) {
            Player.currGrassOverSprite = Player.desertGrassSprite;
         } else if (targetTile.name.equals("soot1")) {
            Player.currGrassOverSprite = Player.sootDriftSprite;
         }

         DrawPlayerUpper.timer = 0;
      }

      boolean startIsTidal = startTile != null && startTile.isTidal;
      Tile exteriorTile = game.map.overworldTiles.get(this.initialPos);
      boolean startExteriorIsTidal = exteriorTile != null && exteriorTile.isTidal;
      boolean targetIsTidal = targetTile != null && targetTile.isTidal;
      exteriorTile = game.map.overworldTiles.get(this.targetPos);
      boolean targetExteriorIsTidal = exteriorTile != null && exteriorTile.isTidal;
      if (targetIsTidal || targetExteriorIsTidal) {
         DrawPlayerUpper.drawTidal = true;
         DrawPlayerUpper.timer = 0;
      } else if (!startIsTidal && !startExteriorIsTidal) {
         DrawPlayerUpper.drawTidal = false;
      } else {
         DrawPlayerUpper.drawTidal = true;
      }

      if (startTile != null && startTile.leavesFootprint()) {
         game.insertAction(this.player.new FootPrintFade(this.player.dirFacing, this.initialPos, null));
      }

      if (startTile != null && startTile.name.contains("puddle1")) {
         if (game.player.hmPokemon == null || !game.player.hmPokemon.specie.name.equals("probopass")) {
            game.insertAction(new PlayMusic("puddle8", 0.2F, true, null));
         }

         game.insertAction(game.player.new WaterSplash(true, new Vector2(0.0F, 0.0F), null));
      }

      if (startIsTidal || startExteriorIsTidal) {
         game.insertAction(new PlayMusic("puddle11", 0.2F, true, null));
         game.insertAction(new PlayMusic("puddle9", 0.2F, true, null));
         game.insertAction(game.player.new WaterSplash(true, new Vector2(0.0F, 4.0F), null));
      }

      if (game.player.currFieldMove.equals("SURF")) {
         if (startTile != null && startTile.isWater) {
            game.insertAction(game.player.new SurfWakeAnim(null));
         }

         if (!this.alternate) {
            game.insertAction(new PlayMusic("swim1", 0.2F, true, null));
         }

         game.insertAction(new PlayMusic("sand1", 0.1F, true, null));
         game.insertAction(game.player.new WaterSplash(true, new Vector2(0.0F, 0.0F), null));
      }
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   public void localStep(Game game) {
      if (game.playerCanMove) {
         if (game.player.dirFacing.equals("up")) {
            game.player.position.y++;
         } else if (game.player.dirFacing.equals("down")) {
            game.player.position.y--;
         } else if (game.player.dirFacing.equals("left")) {
            game.player.position.x--;
         } else if (game.player.dirFacing.equals("right")) {
            game.player.position.x++;
         }

         game.cam.position.set(this.player.position.x + 16.0F, this.player.position.y, 0.0F);
         this.xDist = Math.abs(this.initialPos.x - game.player.position.x);
         this.yDist = Math.abs(this.initialPos.y - game.player.position.y);
         if (this.yDist < 13.0F && this.yDist > 2.0F || this.xDist < 13.0F && this.xDist > 2.0F) {
            if (this.alternate) {
               game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
               DrawPlayerUpper.offsetY = -1;
            } else {
               game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
               DrawPlayerUpper.offsetY = -1;
            }
         } else {
            game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
            DrawPlayerUpper.offsetY = 0;
         }

         if ((int)this.xDist == 6 || (int)this.yDist == 6) {
            Tile currTile = game.map.tiles.get(this.targetPos);
            if (currTile != null && currTile.nameUpper.contains("gate")) {
               currTile.overSprite = null;
               game.insertAction(new PlayMusic("gate1", null));
            }
         } else if ((int)this.xDist == 11 || (int)this.yDist == 11) {
            Tile currTile = game.map.tiles.get(this.initialPos);
            if (currTile != null && currTile.nameUpper.contains("gate")) {
               currTile.init(currTile.name, currTile.nameUpper, currTile.position, true, currTile.routeBelongsTo);
               game.insertAction(new PlayMusic("gate2", null));
            }
         }

         if (this.xDist >= 16.0F || this.yDist >= 16.0F) {
            Tile nextTile = game.map.tiles.get(this.targetPos);
            Tile exteriorTile = game.map.overworldTiles.get(this.targetPos);
            boolean exteriorIsTidal = exteriorTile != null && exteriorTile.isTidal;
            if (nextTile != null && (nextTile.name.contains("puddle1") || nextTile.isTidal || exteriorIsTidal)) {
               Vector2 offset = new Vector2(0.0F, 0.0F);
               Player.PuddleSplash puddleSplash = game.player.new PuddleSplash(offset, null);
               if (nextTile.isTidal || exteriorIsTidal) {
                  offset.set(0.0F, 4.0F);
                  puddleSplash.layer = Action.Layer.map_116;
               }

               game.insertAction(puddleSplash);
               game.insertAction(game.player.new WaterSplash(false, offset, null));
            }

            if (game.player.currFieldMove.equals("SURF")) {
               game.insertAction(game.player.new WaterSplash(true, new Vector2(0.0F, 0.0F), null));
            }

            if (nextTile != null && nextTile.nameUpper.equals("warp_tile1") && nextTile.hasItem != null) {
               DrawText drawText = new DrawText(nextTile.hasItem);
               game.insertAction(drawText);
               game.insertAction(new WaitFrames(game, 60, new SetField(drawText, "goAway", true, null)));
            }

            if (nextTile != null && nextTile.routeBelongsTo != null) {
               Route newRoute = nextTile.routeBelongsTo;
               String newBiome = nextTile.biome;
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
                     System.out.println("New Route: " + newRoute.name);
                  }

                  game.map.currRoute = newRoute;
               }

               game.map.currBiome = newBiome;
            }

            game.player.position.set(this.targetPos);
            game.player.isNearCampfire = game.player.checkNearCampfire();
            game.cam.position.set(this.targetPos.x + 16.0F, this.targetPos.y, 0.0F);
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
            this.nextAction.step(game);
         }
      }
   }

   public void remoteStep(Game game) {
      this.player.network.syncTimer++;
      if (this.player.dirFacing.equals("up")) {
         this.player.position.y++;
      } else if (this.player.dirFacing.equals("down")) {
         this.player.position.y--;
      } else if (this.player.dirFacing.equals("left")) {
         this.player.position.x--;
      } else if (this.player.dirFacing.equals("right")) {
         this.player.position.x++;
      }

      this.xDist = Math.abs(this.initialPos.x - this.player.position.x);
      this.yDist = Math.abs(this.initialPos.y - this.player.position.y);
      if (this.yDist < 13.0F && this.yDist > 2.0F || this.xDist < 13.0F && this.xDist > 2.0F) {
         if (this.alternate) {
            this.player.currSprite = this.player.altMovingSprites.get(this.player.dirFacing);
         } else {
            this.player.currSprite = this.player.movingSprites.get(this.player.dirFacing);
         }
      } else {
         this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
      }

      if (this.xDist >= 16.0F || this.yDist >= 16.0F) {
         this.player.position.set(this.targetPos);
         this.player.standingAction.alternate = !this.alternate;
         this.player.standingAction.isRunning = false;
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
