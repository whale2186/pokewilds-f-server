package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.texture.DynamicTextures;
import java.util.ArrayList;
import java.util.HashMap;

class MoveWater extends Action {
   public Action.Layer layer = Action.Layer.map_109;
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Integer> repeats;
   Tile tile;
   Sprite[] campfireSprites;
   Sprite[] torchSprites = new Sprite[2];
   int campfireTimer = 0;
   int avatarTimer = 0;
   int waterTimer = 0;
   int regiCaveTimer = 0;
   int shoreTimer = 0;
   public static int shoreIndex = 0;
   int modX = 0;
   public static int regiTimer2 = 0;
   public boolean justFlipped = false;
   int regiCaveInterval = 40;
   Pixmap pixmap;
   Pixmap roamingPixmap;
   Texture roamingTexture;
   Pixmap firePixmap1;
   Pixmap firePixmap2;
   Pixmap torchPixmap1;
   Pixmap torchPixmap2;
   int timer = 0;
   Vector2 prevPos = new Vector2();
   public HashMap<Tile, String> placeWater = new HashMap<>();
   Pixmap currPixmap;
   Pixmap currTorchPixmap;

   public MoveWater(Game game) {
      super();
      this.positions = new ArrayList<>();
      this.resetVars();
      Texture text = TextureCache.get(Gdx.files.internal("tiles/campfire1.png"));
      this.campfireSprites = new Sprite[4];
      this.campfireSprites[0] = new Sprite(text, 0, 0, 16, 20);
      this.campfireSprites[1] = new Sprite(text, 16, 0, 16, 20);
      text = TextureCache.get(Gdx.files.internal("tiles/torch_sheet1.png"));
      this.torchSprites[0] = new Sprite(text, 0, 60, 16, 20);
      this.torchSprites[1] = new Sprite(text, 16, 60, 16, 20);
      text = TextureCache.get(Gdx.files.internal("torch_mask1.png"));
      TextureData temp = text.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.torchPixmap1 = temp.consumePixmap();
      text = TextureCache.get(Gdx.files.internal("torch_mask2.png"));
      temp = text.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.torchPixmap2 = temp.consumePixmap();
      text = TextureCache.get(Gdx.files.internal("fire_mask3.png"));
      this.campfireSprites[2] = new Sprite(text, 0, 0, 160, 144);
      temp = text.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.firePixmap1 = temp.consumePixmap();
      text = TextureCache.get(Gdx.files.internal("fire_mask4.png"));
      this.campfireSprites[3] = new Sprite(text, 0, 0, 160, 144);
      temp = text.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.firePixmap2 = temp.consumePixmap();
      this.pixmap = new Pixmap(640, 432, Format.RGBA8888);
      this.pixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
      this.roamingPixmap = new Pixmap(640, 432, Format.RGBA8888);
      this.roamingPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 1.0F));
      this.roamingTexture = TextureCache.get(this.roamingPixmap);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   public void resetVars() {
      this.position = new Vector2(0.0F, 0.0F);
      this.positions.add(new Vector2(0.0F, 0.0F));
      this.positions.add(new Vector2(1.0F, 0.0F));
      this.positions.add(new Vector2(2.0F, 0.0F));
      this.positions.add(new Vector2(3.0F, 0.0F));
      this.positions.add(new Vector2(2.0F, 0.0F));
      this.positions.add(new Vector2(1.0F, 0.0F));
      this.repeats = new ArrayList<>();
      this.repeats.add(19);
      this.repeats.add(19);
      this.repeats.add(19);
      this.repeats.add(19);
      this.repeats.add(19);
      this.repeats.add(19);
   }

   @Override
   public void step(Game game) {
      if (!game.player.dontDrawMapDuringBattle) {
         this.roamingPixmap.fill();
         if (this.campfireTimer % 10 == 0) {
            this.pixmap.fill();
            this.prevPos.set(game.player.position.x, game.player.position.y);
         }

         if (!Game.canUseFrameBuffers) {
            this.currPixmap = this.firePixmap2;
            this.currTorchPixmap = this.torchPixmap2;
            if (this.campfireTimer % 20 < 10) {
               this.currPixmap = this.firePixmap1;
               this.currTorchPixmap = this.torchPixmap1;
            }
         }

         for (int i = 0; i < game.map.onscreenTiles.size(); i++) {
            Tile tile = game.map.onscreenTiles.get(i);
            if (tile.isWater) {
               tile.sprite.setRegionX((int)this.position.x);
               tile.sprite.setRegionWidth((int)tile.sprite.getWidth());
            } else if (tile.name.contains("flower")) {
               if (this.timer == 0) {
                  tile.sprite.setRegion(0, 0, 16, 16);
               } else if (this.timer == 26) {
                  tile.sprite.setRegion(16, 0, 16, 16);
               }
            } else if (tile.isWaterfall) {
               if (this.campfireTimer % 40 == 0) {
                  tile.sprite.setRegion(tile.sprite.getRegionX() % 48, tile.sprite.getRegionY(), 16, 16);
               } else if (this.campfireTimer % 40 == 19) {
                  tile.sprite.setRegion(tile.sprite.getRegionX() % 48 + 48, tile.sprite.getRegionY(), 16, 16);
               }
            } else if (tile.isLava) {
               if (this.campfireTimer % 20 == 0) {
                  tile.sprite.setRegion(tile.sprite.getRegionX() % 48 + 96 * (this.campfireTimer / 20), tile.sprite.getRegionY(), 16, 16);
               } else if (this.campfireTimer % 20 == 3) {
                  tile.sprite.setRegion(tile.sprite.getRegionX() % 48 + 48 + 96 * (this.campfireTimer / 20), tile.sprite.getRegionY(), 16, 16);
               }
            }

            if (tile.nameUpper.equals("campfire1")) {
               if (this.campfireTimer == 0) {
                  SmolSprite newSprite = new SmolSprite(this.campfireSprites[0]);
                  newSprite.setPosition(tile.overSprite.getX(), tile.overSprite.getY());
                  tile.overSprite = newSprite;
               } else if (this.campfireTimer == 40) {
                  SmolSprite newSprite = new SmolSprite(this.campfireSprites[1]);
                  newSprite.setPosition(tile.overSprite.getX(), tile.overSprite.getY());
                  tile.overSprite = newSprite;
               }

               if (!Game.canUseFrameBuffers && this.campfireTimer % 10 == 0 && game.mapBatch.getColor().r < 0.5F) {
                  int xPos = (int)(tile.position.x - 80.0F - (game.player.position.x - 320.0F));
                  int yPos = (int)(296.0F - (tile.position.y + 8.0F - 72.0F - (game.player.position.y - 216.0F)));
                  this.pixmap.drawPixmap(this.currPixmap, xPos, yPos);
               }
            } else if (tile.isTorch) {
               Sprite newSprite;
               if (this.campfireTimer < 40) {
                  newSprite = new Sprite(this.torchSprites[0]);
               } else {
                  newSprite = new Sprite(this.torchSprites[1]);
               }

               Color color = game.mapBatch.getColor().cpy();
               if (game.mapBatch.getColor().r < 0.5F) {
                  game.mapBatch.setColor(0.2F, 0.2F, 0.2F, 1.0F);
                  if (!Game.canUseFrameBuffers && this.campfireTimer % 10 == 0) {
                     int xPos = (int)(tile.position.x - 80.0F - (game.player.position.x - 320.0F));
                     int yPos = (int)(296.0F - (tile.position.y + 8.0F - 72.0F - (game.player.position.y - 216.0F)));
                     this.pixmap.drawPixmap(this.currTorchPixmap, xPos + 40, yPos + 36);
                  }
               }

               int offsetX = 0;
               game.mapBatch.draw(newSprite, tile.position.x + offsetX, tile.position.y + 4.0F + 2.0F);
               game.mapBatch.setColor(color);
            } else if (tile.name.equals("cave1_regi3")) {
               if (game.battle.drawAction != null) {
                  if (regiTimer2 == 0) {
                     tile.sprite.setRegion(160, 0, 32, 32);
                  }

                  this.justFlipped = true;
               } else {
                  if (this.justFlipped) {
                     this.justFlipped = false;
                     tile.sprite.setPosition(tile.position.x, tile.position.y);
                     tile.sprite.setRegion(128, 0, 32, 32);
                  }

                  if (regiTimer2 == 0) {
                     tile.sprite.setRegion(160, 0, 32, 32);
                  } else if (regiTimer2 == 4) {
                     tile.sprite.setRegion(128, 0, 32, 32);
                  } else if (regiTimer2 == 8) {
                     tile.sprite.setRegion(160, 0, 32, 32);
                  } else if (regiTimer2 == 12) {
                     tile.sprite.setRegion(128, 0, 32, 32);
                  } else if (regiTimer2 == 16) {
                     tile.sprite.setRegion(160, 0, 32, 32);
                  } else if (regiTimer2 == 20) {
                     tile.sprite.setRegion(128, 0, 32, 32);
                  } else if (regiTimer2 == 52) {
                     tile.sprite.setRegion(192, 0, 32, 32);
                  } else if (regiTimer2 == 56) {
                     tile.sprite.setRegion(224, 0, 32, 32);
                  } else if (regiTimer2 == 60) {
                     tile.sprite.setRegion(256, 0, 32, 32);
                  } else if (regiTimer2 == 64) {
                     tile.sprite.setRegion(288, 0, 32, 32);
                  } else if (regiTimer2 == 68) {
                     tile.sprite.setRegion(128, 0, 32, 32);
                  }
               }
            } else if (tile.nameUpper.contains("revived_")) {
               String name = tile.nameUpper.split("_")[1];
               if (!Specie.species.containsKey(name)) {
                  Specie.species.put(name, new Specie(name));
               }

               int index = 0;
               if (this.avatarTimer < 60) {
                  index = 1;
               }

               game.mapBatch.draw(Specie.species.get(name).avatarSprites.get(index), tile.position.x + 8.0F, tile.position.y + 8.0F);
            } else if (tile.nameUpper.equals("volcarona")) {
               if (!Game.canUseFrameBuffers && this.campfireTimer % 10 == 0 && game.mapBatch.getColor().r < 0.5F) {
                  int xPos = (int)(tile.position.x - 80.0F - (game.player.position.x - 320.0F));
                  int yPos = (int)(296.0F - (tile.position.y + 8.0F - 72.0F - (game.player.position.y - 216.0F)));
                  this.pixmap.drawPixmap(this.currPixmap, xPos, yPos);
               }

               String name = tile.nameUpper;
               if (!Specie.species.containsKey(name)) {
                  Specie.species.put(name, new Specie(name));
               }

               int index = 0;
               if (this.avatarTimer < 60) {
                  index = 1;
               }

               game.mapBatch.draw(Specie.species.get(name).avatarSprites.get(index), tile.position.x, tile.position.y + 4.0F);
            } else if (tile.nameUpper.equals("spiritomb")) {
               String name = tile.nameUpper;
               if (!Specie.species.containsKey(name)) {
                  Specie.species.put(name, new Specie(name));
               }

               int index = 0;
               if (this.avatarTimer < 60) {
                  index = 1;
               }

               game.mapBatch.draw(Specie.species.get(name).avatarSprites.get(index), tile.position.x, tile.position.y + 4.0F);
            } else if (this.waterTimer == 0 && tile.nameUpper.contains("hole") && !tile.nameUpper.contains("water")) {
               Vector2[] positions = new Vector2[]{new Vector2(0.0F, -16.0F), new Vector2(0.0F, 16.0F), new Vector2(-16.0F, 0.0F), new Vector2(16.0F, 0.0F)};
               boolean placeWater = false;
               boolean placeLava = false;

               for (Vector2 position : positions) {
                  Tile currTile = game.map.tiles.get(tile.position.cpy().add(position));
                  if (currTile != null) {
                     if (currTile.name.contains("water") || currTile.nameUpper.contains("water")) {
                        placeWater = true;
                     } else if (currTile.name.contains("lava") || currTile.nameUpper.contains("lava")) {
                        placeLava = true;
                     }
                  }
               }

               if (placeWater && placeLava) {
                  this.placeWater.put(tile, "soot1");
               } else if (placeLava) {
                  this.placeWater.put(tile, "lava1");
               } else if (placeWater) {
                  this.placeWater.put(tile, "water2");
               }
            } else if (tile.isChimney) {
               tile.overSprite.setRegion(DynamicTextures.get(tile.nameUpper, this.campfireTimer / 20));
            }

            if (!Game.canUseFrameBuffers) {
               Pokemon pokemon = game.map.pokemon.get(tile.position);
               if (pokemon != null && pokemon.mapTiles == game.map.tiles && pokemon.hms.contains("FLASH") && game.mapBatch.getColor().r < 0.5F) {
                  int xPos = (int)(pokemon.position.x - 80.0F - (game.player.position.x - 320.0F));
                  int yPos = (int)(296.0F - (pokemon.position.y + 8.0F - 72.0F - (game.player.position.y - 216.0F)));
                  this.roamingPixmap.drawPixmap(this.currPixmap, xPos, yPos);
               }
            }
         }

         if (!Game.canUseFrameBuffers) {
            if (game.mapBatch.getColor().r < 0.5F
               && game.player.hmPokemon != null
               && game.player.hmPokemon.currOwSprite != null
               && game.player.hmPokemon.hms.contains("FLASH")) {
               Vector2 position = game.player.hmPokemon.position;
               if (!game.player.currFieldMove.equals("")) {
                  position = game.player.position;
               }

               int xPos = (int)(position.x - 80.0F - (game.player.position.x - 320.0F));
               int yPos = (int)(296.0F - (position.y + 8.0F - 72.0F - (game.player.position.y - 216.0F)));
               this.roamingPixmap.drawPixmap(this.currPixmap, xPos, yPos);
            }

            if (game.mapBatch.getColor().r < 1.0F) {
               Color tempColor = game.mapBatch.getColor().cpy();
               game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
               int temp1 = game.mapBatch.getBlendSrcFunc();
               int temp2 = game.mapBatch.getBlendDstFunc();
               game.mapBatch.setBlendFunction(774, 770);
               this.roamingPixmap.drawPixmap(this.pixmap, (int)(this.prevPos.x - game.player.position.x), (int)(game.player.position.y - this.prevPos.y));
               this.roamingTexture.draw(this.roamingPixmap, 0, 0);
               game.mapBatch.draw(this.roamingTexture, game.player.position.x + 8.0F - 320.0F, game.player.position.y + 8.0F - 216.0F);
               if (tempColor.r < 0.5F) {
                  game.mapBatch.draw(this.roamingTexture, game.player.position.x + 8.0F - 320.0F, game.player.position.y + 8.0F - 216.0F);
               }

               if (tempColor.r < 0.1F) {
                  game.mapBatch.draw(this.roamingTexture, game.player.position.x + 8.0F - 320.0F, game.player.position.y + 8.0F - 216.0F);
               }

               game.mapBatch.setBlendFunction(temp1, temp2);
               game.mapBatch.setColor(tempColor);
            }
         }

         if (this.shoreTimer == 0) {
            shoreIndex = 1;
         } else if (this.shoreTimer == 16) {
            shoreIndex = 2;
         } else if (this.shoreTimer == 32) {
            shoreIndex = 3;
            Vector2 pos = game.player.position.cpy();
            pos.x = pos.x - pos.x % 16.0F;
            pos.y = pos.y - pos.y % 16.0F;
            Tile currTile = game.map.tiles.get(pos);
            if (currTile != null && (currTile.shoreOcean != null || currTile.shoreTidal != null)) {
               game.insertAction(game.player.new WaterSplash(true, game.player.new WaterSplash(false, null)));
            }
         } else if (this.shoreTimer == 48) {
            shoreIndex = 4;
            Vector2 pos = game.player.position.cpy();
            pos.x = pos.x - pos.x % 16.0F;
            pos.y = pos.y - pos.y % 16.0F;
            Tile currTile = game.map.tiles.get(pos);
            if (currTile != null && (currTile.shoreOcean != null || currTile.shoreTidal != null)) {
               game.insertAction(game.player.new WaterSplash(true, game.player.new WaterSplash(false, null)));
            }
         } else if (this.shoreTimer == 64) {
            shoreIndex = 5;
         } else if (this.shoreTimer == 80) {
            shoreIndex = 6;
         } else if (this.shoreTimer == 96) {
            shoreIndex = 0;
         }

         if (this.shoreTimer < 128) {
            this.shoreTimer++;
         } else {
            this.shoreTimer = 0;
         }

         if (this.campfireTimer < 79) {
            this.campfireTimer++;
         } else {
            this.campfireTimer = 0;
         }

         if (this.avatarTimer < 120) {
            this.avatarTimer++;
         } else {
            this.avatarTimer = 0;
         }

         if (this.waterTimer < 60) {
            this.waterTimer++;
         } else {
            this.waterTimer = 0;
         }

         if (game.battle.drawAction != null && regiTimer2 < 30) {
            regiTimer2++;
         } else if (game.battle.drawAction == null && regiTimer2 < 160) {
            regiTimer2++;
         } else {
            regiTimer2 = 0;
         }

         if (game.map.currRoute != null && game.battle.drawAction == null && game.map.currRoute.name.equals("regi_cave1")) {
            game.uiBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            game.mapBatch.setColor(new Color(0.6F, 0.6F, 0.6F, 1.0F));
            if (this.regiCaveTimer < this.regiCaveInterval * 1) {
               game.uiBatch.setColor(new Color(0.7F, 0.7F, 0.8F, 1.0F));
               game.mapBatch.setColor(new Color(0.5F, 0.5F, 0.8F, 1.0F));
            } else if (this.regiCaveTimer < this.regiCaveInterval * 2) {
               game.uiBatch.setColor(new Color(0.6F, 0.6F, 0.7F, 1.0F));
               game.mapBatch.setColor(new Color(0.4F, 0.4F, 0.7F, 1.0F));
            } else if (this.regiCaveTimer < this.regiCaveInterval * 3) {
               game.uiBatch.setColor(new Color(0.7F, 0.7F, 0.8F, 1.0F));
               game.mapBatch.setColor(new Color(0.3F, 0.3F, 0.6F, 1.0F));
            } else if (this.regiCaveTimer < this.regiCaveInterval * 4) {
               game.uiBatch.setColor(new Color(0.6F, 0.6F, 0.7F, 1.0F));
               game.mapBatch.setColor(new Color(0.2F, 0.2F, 0.5F, 1.0F));
            } else if (this.regiCaveTimer < this.regiCaveInterval * 5) {
               game.uiBatch.setColor(new Color(0.7F, 0.7F, 0.8F, 1.0F));
               game.mapBatch.setColor(new Color(0.3F, 0.3F, 0.6F, 1.0F));
            } else {
               game.uiBatch.setColor(new Color(0.6F, 0.6F, 0.7F, 1.0F));
               game.mapBatch.setColor(new Color(0.4F, 0.4F, 0.7F, 1.0F));
            }

            if (this.regiCaveTimer < this.regiCaveInterval * 6) {
               this.regiCaveTimer++;
            } else {
               this.regiCaveTimer = 0;
            }
         } else {
            game.uiBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
         }

         if (!this.placeWater.isEmpty()) {
            for (Tile tile : this.placeWater.keySet()) {
               tile.name = this.placeWater.get(tile);
               tile.nameUpper = "";
               tile.init();
               game.map.adjustSurroundingTiles(tile);
            }

            if (game.battle.drawAction == null && game.playerCanMove) {
               game.insertAction(new PlayMusic("sand1", 0.5F, true, null));
            }

            this.placeWater.clear();
         }

         if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.position = this.positions.remove(0);
            this.repeats.remove(0);
            if (this.positions.isEmpty()) {
               this.resetVars();
            }

            DrawMapGrass.tidalSprite.setRegionX((int)this.positions.get(0).x);
            DrawMapGrass.tidalSprite.setRegionWidth((int)DrawMapGrass.tidalSprite.getWidth());
         }

         this.timer++;
         if (this.timer >= 52) {
            this.timer = 0;
         }
      }
   }
}
