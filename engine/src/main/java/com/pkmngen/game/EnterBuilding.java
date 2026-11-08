package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.util.TextureCache;
import java.util.Map;

class EnterBuilding extends Action {
   Sprite sprite;
   String action;
   Action nextAction;
   Map<Vector2, Tile> whichTiles;
   public Action.Layer layer = Action.Layer.gui_114;
   int timer = 0;
   int slow = 1;
   int interiorTilesIndex = 0;

   public EnterBuilding(Game game, Action nextAction) {
      this(game, "enter", nextAction);
   }

   public EnterBuilding(Game game, String action, Action nextAction) {
      this(game, action, null, nextAction);
   }

   public EnterBuilding(Game game, String action, int slow, Action nextAction) {
      this(game, action, null, nextAction);
      this.slow = slow;
   }

   public EnterBuilding(Game game, String action, Map<Vector2, Tile> whichTiles, int interiorTilesIndex, Action nextAction) {
      this(game, action, whichTiles, nextAction);
      this.interiorTilesIndex = interiorTilesIndex;
   }

   public EnterBuilding(Game game, String action, Map<Vector2, Tile> whichTiles, Action nextAction) {
      super();
      this.whichTiles = whichTiles;
      this.nextAction = nextAction;
      this.action = action;
      Texture text1 = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.sprite = new Sprite(text1);
      this.sprite.setPosition(0.0F, 0.0F);
      this.interiorTilesIndex = game.map.interiorTilesIndex;
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
      if (this.slow != 1) {
         for (int i = 0; i < 12 * this.slow; i++) {
            this.step(game);
         }
      }
   }

   @Override
   public void step(Game game) {
      if (this.timer < 2 * this.slow) {
         if (this.timer == 0 && this.action.equals("enter") || this.action.equals("exit")) {
            game.insertAction(new PlayMusic(this.action + "1", null));
         }
      } else if (this.timer < 4 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.25F);
      } else if (this.timer < 6 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.5F);
      } else if (this.timer < 12 * this.slow) {
         if (this.timer == 6 * this.slow) {
            Tile tile = game.map.overworldTiles.get(game.player.position);
            if (tile != null && tile.name.equals("cave1_door1")) {
               if (this.action.equals("enter")) {
                  game.map.interiorTilesIndex = 90;
               } else {
                  game.map.interiorTilesIndex = 100;
               }
            }

            game.map.refreshCache = true;
            if (this.whichTiles != null) {
               game.map.tiles = this.whichTiles;
            } else if (this.action.equals("enter")) {
               game.map.tiles = game.map.interiorTiles.get(game.map.interiorTilesIndex);
            } else if (this.action.equals("exit")) {
               game.map.tiles = game.map.overworldTiles;
            }

            game.player.updateBuildTiles(game);
            ScreenUtils.clear(0.0F, 0.0F, 0.0F, 1.0F, true);
            tile = game.map.tiles.get(game.player.position);
            if (tile != null) {
               Route newRoute = tile.routeBelongsTo;
               if (newRoute != null && !newRoute.name.equals(game.map.currRoute.name) && newRoute.isDungeon != game.map.currRoute.isDungeon) {
                  game.musicController.fadeToDungeon = true;
                  game.map.currRoute = newRoute;
               } else if (newRoute != null && newRoute.type().equals("desert") && !game.map.timeOfDay.equals("night")) {
                  game.musicController.fadeToDungeon = true;
                  game.map.currRoute = newRoute;
               } else if (newRoute != null && newRoute.type().equals("graveyard") && !game.map.timeOfDay.equals("night")) {
                  game.musicController.fadeToDungeon = true;
                  game.map.currRoute = newRoute;
                  FogEffect.type = FogEffect.Type.FOG;
                  FogEffect.refresh = true;
               } else if (newRoute != null && newRoute.type().equals("volcano") && !game.map.timeOfDay.equals("night")) {
                  game.musicController.fadeToDungeon = true;
                  game.map.currRoute = newRoute;
                  FogEffect.type = FogEffect.Type.SMOKE;
                  FogEffect.refresh = true;
               } else if (newRoute != null && newRoute.type().equals("deep_forest") && !game.map.timeOfDay.equals("night")) {
                  game.musicController.fadeToDungeon = true;
                  game.map.currRoute = newRoute;
                  FogEffect.type = FogEffect.Type.DEEPFOREST;
                  FogEffect.refresh = true;
               }

               if (newRoute != null && newRoute.name.contains("pkmnmansion")) {
                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (newRoute != null && newRoute.name.equals("fossil_lab1")) {
                  game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
               } else if (newRoute != null && newRoute.name.equals("ruins1_inner")) {
                  System.out.println(this.interiorTilesIndex);
                  if (this.interiorTilesIndex >= 99) {
                     game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
                  } else if (this.interiorTilesIndex == 98) {
                     game.mapBatch.setColor(new Color(0.3F, 0.3F, 0.6F, 1.0F));
                  } else if (this.interiorTilesIndex == 97) {
                     game.mapBatch.setColor(new Color(0.04F, 0.04F, 0.1F, 1.0F));
                  } else if (this.interiorTilesIndex == 96) {
                     game.mapBatch.setColor(new Color(0.01F, 0.01F, 0.04F, 1.0F));
                  } else {
                     game.mapBatch.setColor(new Color(0.02F, 0.02F, 0.05F, 1.0F));
                  }
               } else if (newRoute == null || !newRoute.name.equals("regi_cave1")) {
                  if (newRoute != null && newRoute.type().equals("graveyard") && !game.map.timeOfDay.equals("night")) {
                     game.mapBatch.setColor(new Color(0.9F, 0.9F, 1.0F, 1.0F));
                  } else if (newRoute != null && newRoute.type().equals("deep_forest") && !game.map.timeOfDay.equals("night")) {
                     game.mapBatch.setColor(new Color(0.7F, 0.7F, 1.0F, 1.0F));
                  } else if (!game.map.timeOfDay.equals("night")) {
                     game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
                  } else {
                     game.mapBatch.setColor(new Color(PkmnMap.nightColor));
                  }
               }
            }
         }

         this.sprite.draw(game.uiBatch, 1.0F);
      } else if (this.timer < 14 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.75F);
      } else if (this.timer < 16 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.5F);
      } else if (this.timer < 18 * this.slow) {
         this.sprite.draw(game.uiBatch, 0.25F);
      } else {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
   }
}
