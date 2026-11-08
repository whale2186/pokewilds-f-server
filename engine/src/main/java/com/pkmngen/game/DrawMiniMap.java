package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;
import java.util.Locale;

class DrawMiniMap extends Menu {
   Sprite arrow;
   Sprite arrowWhite;
   Vector2 currOffset = new Vector2();
   Texture texture;
   int moveTimer = 0;
   boolean justTouched = true;
   Vector3 touchPos = new Vector3();
   Vector3 prevTouchPos = new Vector3();
   int arrowTimer = 0;
   Vector2 playerPos = new Vector2();
   Vector2 spawnLoc = new Vector2();
   float currX = 0.0F;
   float currY = 0.0F;
   public Action.Layer layer = Action.Layer.map_neg_1;
   int cursorDelay;
   Sprite spritePart;
   Sprite zSprite;
   Sprite bagSprite;
   Texture backTexture;
   Pixmap warpTilePixmap;
   Texture warpTileTexture;
   Texture warpTileTexture2;
   public static HashMap<Vector2, Tile> warpTiles = new HashMap<>();
   Tile nearestWarpTile;
   Vector2 startPos = new Vector2();
   Vector2 endPos = new Vector2();
   DrawText drawText;
   boolean snapTo = false;
   Vector2 cursorPos = new Vector2();
   Pokemon teleportPokemon = null;

   public DrawMiniMap(Game game, Menu prevMenu) {
      this(game, null, prevMenu);
   }

   public DrawMiniMap(Game game, Pokemon teleportPokemon, Menu prevMenu) {
      this.teleportPokemon = teleportPokemon;
      this.prevMenu = prevMenu;
      this.disabled = false;
      this.cursorDelay = 0;
      Texture text = TextureCache.get(Gdx.files.internal("battle/arrow_right1.png"));
      this.arrow = new Sprite(text, 0, 0, 5, 7);
      text = TextureCache.get(Gdx.files.internal("battle/arrow_right_white.png"));
      this.arrowWhite = new Sprite(text, 0, 0, 5, 7);
      this.texture = TextureCache.get(game.map.minimap);
      this.spritePart = new Sprite(game.player.currSprite);
      this.spritePart.setRegionHeight(11);
      text = TextureCache.get(Gdx.files.internal("tiles/zs1.png"));
      this.zSprite = new Sprite(text, 0, 0, 16, 16);
      text = TextureCache.get(Gdx.files.internal("tiles/sleeping_bag1.png"));
      this.bagSprite = new Sprite(text, 0, 0, 24, 16);
      this.backTexture = TextureCache.get(Gdx.files.internal("battle/intro_frame3.png"));
      this.drawText = new DrawText(null);
      this.warpTileTexture2 = TextureCache.get(Gdx.files.internal("tiles/warp_tile1_light.png"));
   }

   @Override
   public String getCamera() {
      return "map";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void firstStep(Game game) {
      this.playerPos.x = (int)(game.player.position.x - game.map.bottomLeft.x) / 8;
      this.playerPos.y = (int)(game.player.position.y - game.map.bottomLeft.y) / 8;
      this.spawnLoc.x = (int)(game.player.spawnLoc.x - game.map.bottomLeft.x) / 8;
      this.spawnLoc.y = (int)(game.player.spawnLoc.y - game.map.bottomLeft.y) / 8;
      this.currX = game.cam.position.x - this.playerPos.x;
      this.currY = game.cam.position.y - this.playerPos.y;
      this.currOffset.x = 0.0F;
      this.currOffset.y = 0.0F;
      this.cursorPos.set(game.cam.position.x, game.cam.position.y);
      this.warpTilePixmap = new Pixmap(game.map.minimap.getWidth(), game.map.minimap.getHeight(), Format.RGBA8888);

      for (Tile tile : warpTiles.values()) {
         Pixmap currPixmap = tile.overSprite.getPixmap();
         this.warpTilePixmap
            .drawPixmap(
               currPixmap,
               (int)(tile.position.x - game.map.bottomLeft.x) / 8 - 8,
               (int)(game.map.topRight.y - tile.position.y) / 8 - 8,
               tile.overSprite.getRegionX(),
               tile.overSprite.getRegionY(),
               tile.overSprite.getRegionWidth(),
               tile.overSprite.getRegionHeight()
            );
      }

      this.warpTileTexture = TextureCache.get(this.warpTilePixmap);
      game.insertAction(this.drawText);
      if (this.teleportPokemon == null) {
         for (Pokemon pokemon : game.player.pokemon) {
            if (!pokemon.isEgg && pokemon.hms.contains("TELEPORT")) {
               this.teleportPokemon = pokemon;
               break;
            }
         }
      }
   }

   @Override
   public void step(Game game) {
      this.arrowTimer++;
      if (this.arrowTimer > 80) {
         this.arrowTimer = 0;
      }

      game.mapBatch.flush();
      Gdx.gl.glClear(16384);
      game.mapBatch.draw(this.texture, this.currX + this.currOffset.x, this.currY + this.currOffset.y);
      if (this.arrowTimer < 40) {
         game.mapBatch.draw(this.warpTileTexture, this.currX + this.currOffset.x, this.currY + this.currOffset.y);
         game.mapBatch.draw(this.bagSprite, this.currX + this.currOffset.x + this.spawnLoc.x - 12.0F, this.currY + this.currOffset.y + this.spawnLoc.y - 8.0F);
         game.mapBatch.draw(this.zSprite, this.currX + this.currOffset.x + this.spawnLoc.x + 4.0F, this.currY + this.currOffset.y + this.spawnLoc.y + 4.0F);
         game.mapBatch
            .draw(this.spritePart, this.currX + this.currOffset.x + this.playerPos.x - 8.0F, this.currY + this.currOffset.y + this.playerPos.y + 5.0F - 8.0F);
      }

      if (this.nearestWarpTile != null) {
         game.mapBatch
            .draw(
               this.warpTileTexture2,
               this.currX + this.currOffset.x + (int)(this.nearestWarpTile.position.x - game.map.bottomLeft.x) / 8 - 8.0F,
               this.currY + this.currOffset.y + (int)(this.nearestWarpTile.position.y - game.map.bottomLeft.y) / 8 - 8.0F + 2.0F
            );
         if (this.nearestWarpTile.hasItem != null && this.drawText.text == null) {
            this.drawText.text = this.nearestWarpTile.hasItem;
         }
      } else {
         this.drawText.text = null;
      }

      if (this.goAway) {
         this.texture.dispose();
         this.warpTileTexture.dispose();
         game.actionStack.remove(this.drawText);
         game.actionStack.remove(this);
      }

      if (!this.disabled) {
         this.snapTo = true;
         if (InputProcessor.upPressed) {
            this.moveTimer++;
            if (this.moveTimer >= 1) {
               this.moveTimer = 0;
               this.currY -= 2.0F;
               this.cursorPos.y += 16.0F;
            }

            this.snapTo = false;
         } else if (InputProcessor.downPressed) {
            this.moveTimer++;
            if (this.moveTimer >= 1) {
               this.moveTimer = 0;
               this.currY += 2.0F;
               this.cursorPos.y -= 16.0F;
            }

            this.snapTo = false;
         }

         if (InputProcessor.rightPressed) {
            this.moveTimer++;
            if (this.moveTimer >= 1) {
               this.moveTimer = 0;
               this.currX -= 2.0F;
               this.cursorPos.x += 16.0F;
            }

            this.snapTo = false;
         } else if (InputProcessor.leftPressed) {
            this.moveTimer++;
            if (this.moveTimer >= 1) {
               this.moveTimer = 0;
               this.currX += 2.0F;
               this.cursorPos.x -= 16.0F;
            }

            this.snapTo = false;
         }

         this.touchPos.x = Gdx.input.getX();
         this.touchPos.y = Gdx.input.getY();
         game.cam.unproject(this.touchPos);
         if (Gdx.input.isTouched()) {
            if (this.justTouched) {
               this.prevTouchPos = this.touchPos.cpy();
            }

            this.justTouched = false;
            int amtX = (int)(this.touchPos.x - this.prevTouchPos.x);
            int amtY = (int)(this.touchPos.y - this.prevTouchPos.y);
            this.currX += amtX;
            this.currY += amtY;
            this.cursorPos = this.cursorPos.sub(amtX * 8, amtY * 8);
            this.prevTouchPos = this.touchPos.cpy();
            this.snapTo = false;
         } else {
            this.justTouched = true;
         }

         this.nearestWarpTile = null;
         this.startPos.set(this.cursorPos.x, this.cursorPos.y);
         this.startPos.x = this.startPos.x - (Math.floorMod((int)this.startPos.x, 16) + 80);
         this.startPos.y = this.startPos.y - (Math.floorMod((int)this.startPos.y, 16) + 80);
         this.endPos.set(this.startPos.x, this.startPos.y);
         this.endPos.x += 160.0F;
         this.endPos.y += 160.0F;
         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            Tile tile = warpTiles.get(currPos);
            currPos.x += 16.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 16.0F;
            }

            if (tile != null) {
               this.nearestWarpTile = tile;
               break;
            }
         }

         if (this.snapTo && this.nearestWarpTile != null) {
            this.currX = game.cam.position.x - (int)(this.nearestWarpTile.position.x - game.map.bottomLeft.x) / 8;
            this.currY = game.cam.position.y - (int)(this.nearestWarpTile.position.y - game.map.bottomLeft.y) / 8;
            this.cursorPos.set(this.nearestWarpTile.position.x, this.nearestWarpTile.position.y);
         }

         if (InputProcessor.bJustPressed) {
            this.texture.dispose();
            this.warpTileTexture.dispose();
            if (this.prevMenu != null) {
               this.prevMenu.disabled = false;
            }

            game.actionStack.remove(this.drawText);
            game.actionStack.remove(this);
            game.insertAction(new DrawPokemonMenu.Intro(this.prevMenu));
            game.insertAction(new PlayMusic("click1", null));
         } else {
            if (InputProcessor.aJustPressed && this.teleportPokemon != null && this.nearestWarpTile != null) {
               this.disabled = true;
               DrawText drawText = new DrawText(this.nearestWarpTile.hasItem);
               float rate = 0.0125F * game.currMusic.getVolume();
               Action nextAction = new DisplayText(
                  game,
                  "Teleport to this location?",
                  null,
                  true,
                  false,
                  new DrawYesNoMenu(
                     null,
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new PlayMusic(
                              this.teleportPokemon,
                              new DisplayText(
                                 game,
                                 this.teleportPokemon.nickname.toUpperCase(Locale.ROOT) + " used TELEPORT!",
                                 null,
                                 null,
                                 new RunCode(
                                    () -> {
                                       this.goAway = true;
                                       if (game.player.flyingAction != null) {
                                          game.player.flyingAction.drawShadow = false;
                                       } else if (game.player.hmPokemon != null
                                          && !game.player.currFieldMove.equals("FLY")
                                          && !game.player.currFieldMove.equals("SURF")
                                          && !game.player.currFieldMove.equals("RIDE")) {
                                          if (!game.player.currFieldMove.equals("")) {
                                             game.player.swapSprites(game.player.hmPokemon);
                                          }

                                          game.player.hmPokemon.removeDrawActions(game);
                                          game.player.hmPokemon = null;
                                          game.player.currFieldMove = "";
                                          game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
                                       }
                                    },
                                    new DrawPokemonMenu.Outro(
                                       new FadeMusic(
                                          game.currMusic,
                                          -rate,
                                          game.currMusic.getVolume(),
                                          true,
                                          game.player.new TeleportUp(new LightFadeIn(new WaitFrames(game, 3, new RunCode(() -> {
                                             game.map.tiles = game.map.overworldTiles;
                                             game.player.position = this.nearestWarpTile.position.cpy().add(0.0F, 78.0F);
                                             Vector2 pos = this.nearestWarpTile.position.cpy().add(16.0F, 0.0F);
                                             game.cam.position.set(pos.x, pos.y, 0.0F);
                                             game.map.interiorTilesIndex = 100;
                                             if (game.map.timeOfDay.equals("day")) {
                                                game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
                                             } else {
                                                game.mapBatch.setColor(new Color(PkmnMap.nightColor));
                                             }

                                             if (game.player.hmPokemon != null && game.player.currFieldMove.equals("SURF")) {
                                                game.player.swapSprites(game.player.hmPokemon);
                                                game.player.hmPokemon.removeDrawActions(game);
                                                game.player.hmPokemon = null;
                                                game.player.currFieldMove = "";
                                                game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
                                             }
                                          }, new WaitFrames(
                                             game, 42, new LightFadeOut(new WaitFrames(game, 42, game.player.new TeleportDown(new RunCode(() -> {
                                                game.playerCanMove = true;
                                                game.player.canMove = true;
                                                if (game.player.flyingAction != null) {
                                                   game.player.flyingAction.drawShadow = true;
                                                }
                                             }, new WaitFrames(game, 20, new RunCode(() -> {
                                                game.musicController.resumeOverworldMusic = true;
                                                game.musicController.teleporting = true;
                                             }, new SplitAction(drawText, new WaitFrames(game, 60, new SetField(drawText, "goAway", true, null)))))))))
                                          )))))
                                       )
                                    )
                                 )
                              )
                           )
                        )
                     ),
                     new DisplayText.Clear(game, new WaitFrames(game, 10, new SetField(this, "disabled", false, null)))
                  )
               );
               game.insertAction(nextAction);
            }
         }
      }
   }

   static class Intro extends Action {
      int length;
      public Action.Layer layer = Action.Layer.gui_110;
      Menu prevMenu;

      public Intro(Menu prevMenu, int length, Action nextAction) {
         super();
         this.prevMenu = prevMenu;
         this.nextAction = nextAction;
         this.length = length;
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
         if (this.prevMenu != null) {
            this.prevMenu.step(game);
         }

         this.length--;
         if (this.length <= 0) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }
}
