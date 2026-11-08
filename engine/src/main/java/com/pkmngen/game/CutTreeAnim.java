package com.pkmngen.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

class CutTreeAnim extends Action {
   int timer = 0;
   Tile tile;
   Sprite left;
   Sprite right;
   int offsetX = 0;

   public CutTreeAnim(Game game, Tile tile, Action nextAction) {
      super();
      this.tile = tile;
      this.nextAction = nextAction;
      if (this.tile.nameUpper.equals("tree_savanna1")) {
         this.offsetX = 16;
      }
   }

   @Override
   public void step(Game game) {
      if (this.timer >= 13) {
         if (this.timer == 13) {
            game.insertAction(new PlayMusic("cut1", null));
         } else if (this.timer >= 19) {
            if (this.timer == 19) {
               game.map.tiles.remove(this.tile.position);
               String name = this.tile.name;
               if (name.equals("grass2")) {
                  name = "green1";
               } else if (name.equals("grass3")) {
                  name = "snow1";
               } else if (name.equals("grass_sand1")) {
                  name = "sand1";
               } else if (name.equals("grass4")) {
                  name = "green1";
               } else if (name.equals("grass_sand3")) {
                  name = "desert4";
               } else if (name.equals("grass_graveyard1")) {
                  name = "green11";
               } else if (name.equals("tree5")) {
                  name = "green1";
               } else if (!name.equals("tree_large1")) {
                  if (this.tile.nameUpper.equals("aloe_large1")) {
                     name = "green1";
                     Vector2[] positions = new Vector2[]{
                        this.tile.position.cpy().add(16.0F, 0.0F), this.tile.position.cpy().add(16.0F, 16.0F), this.tile.position.cpy().add(0.0F, 16.0F)
                     };

                     for (Vector2 position : positions) {
                        Tile newTile = game.map.tiles.get(position);
                        newTile.name = name;
                        newTile.nameUpper = "";
                        newTile.overSprite = null;
                        newTile.init();
                        newTile.updateMiniMap(game);
                     }
                  } else if (name.contains("bridge") && this.tile.nameUpper.equals("")) {
                     name = this.tile.unSquish();
                     if (name == null) {
                        name = "water2";
                     }

                     this.tile.overSprite = this.tile.sprite;
                     Tile down = game.map.tiles.get(this.tile.position.cpy().add(0.0F, -16.0F));
                     if (down != null && down.belowBridge) {
                        down.belowBridge = false;
                        down.isSolid = false;
                     }
                  }
               } else {
                  name = "green1";
                  Vector2[] positions = new Vector2[]{
                     this.tile.position.cpy().add(16.0F, 0.0F), this.tile.position.cpy().add(16.0F, 16.0F), this.tile.position.cpy().add(0.0F, 16.0F)
                  };

                  for (Vector2 position : positions) {
                     Tile newTile = game.map.tiles.get(position);
                     newTile.name = name;
                     newTile.nameUpper = "";
                     newTile.overSprite = null;
                     newTile.init();
                     newTile.updateMiniMap(game);
                  }

                  this.tile.overSprite = this.tile.sprite;
               }

               Tile newTile = new Tile(name, this.tile.position.cpy(), true, this.tile.routeBelongsTo);
               newTile.biome = this.tile.biome;
               game.map.tiles.put(this.tile.position.cpy(), newTile);
               newTile.updateMiniMap(game);
               if (this.tile.nameUpper.contains("warp_tile")) {
                  DrawMiniMap.warpTiles.remove(this.tile.position.cpy());
               }

               if (newTile.routeBelongsTo != null) {
                  newTile.routeBelongsTo.storedPokemon.clear();
               }

               game.mapBatch.draw(this.tile.sprite, this.tile.sprite.getX(), this.tile.sprite.getY());
               game.mapBatch.draw(this.tile.overSprite, this.tile.overSprite.getX(), this.tile.overSprite.getY());
            } else if (this.timer == 20) {
               Sprite temp = new Sprite(this.tile.overSprite);
               TextureRegion[][] tempRegion = temp.split((int)this.tile.overSprite.getWidth() / 2, (int)this.tile.overSprite.getHeight());
               this.left = new Sprite(tempRegion[0][0]);
               this.left.setPosition(this.tile.position.x - 2.0F - this.offsetX, this.tile.position.y);
               this.right = new Sprite(tempRegion[0][1]);
               this.right.setPosition(this.tile.position.x + 2.0F - this.offsetX + (int)this.tile.overSprite.getWidth() / 2, this.tile.position.y);
               game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
               game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
            } else if (this.timer < 37) {
               game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
               game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
            } else if (this.timer >= 39) {
               if (this.timer == 39) {
                  this.left.translateX(-2.0F);
                  this.right.translateX(2.0F);
                  game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
                  game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
               } else if (this.timer < 41) {
                  game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
                  game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
               } else if (this.timer >= 43) {
                  if (this.timer == 43) {
                     this.left.translateX(-2.0F);
                     this.right.translateX(2.0F);
                     game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
                     game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
                  } else if (this.timer < 45) {
                     game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
                     game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
                  } else if (this.timer >= 47) {
                     if (this.timer == 47) {
                        this.left.translateX(-2.0F);
                        this.right.translateX(2.0F);
                        game.mapBatch.draw(this.left, this.left.getX(), this.left.getY());
                        game.mapBatch.draw(this.right, this.right.getX(), this.right.getY());
                     } else if (this.timer >= 49) {
                        game.actionStack.remove(this);
                        game.insertAction(this.nextAction);
                     }
                  }
               }
            }
         }
      }

      this.timer++;
   }
}
