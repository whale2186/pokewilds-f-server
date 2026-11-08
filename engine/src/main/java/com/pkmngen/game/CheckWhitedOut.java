package com.pkmngen.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.Locale;
import java.util.Map;

class CheckWhitedOut extends Action {
   public Action.Layer layer = Action.Layer.map_129;

   public CheckWhitedOut(Action nextAction) {
      super();
      this.nextAction = nextAction;
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
      boolean hasAlivePokemon = false;

      for (Pokemon pokemon : game.player.pokemon) {
         if (pokemon.currentStats.get("hp") > 0 && !pokemon.isEgg) {
            hasAlivePokemon = true;
            break;
         }
      }

      if (hasAlivePokemon) {
         game.insertAction(this.nextAction);
      } else {
         for (Pokemon pokemon : game.player.pokemon) {
            if (!pokemon.isEgg) {
               pokemon.currentStats.put("hp", pokemon.maxStats.get("hp") / 2);
            }
         }

         BattleFadeOut.whiteScreen = true;
         game.musicController.playerFainted = true;
         int interiorTilesIndex = 100;
         game.battle.oppPokemon.aggroPlayer = false;
         Map<Vector2, Tile> tiles = game.map.overworldTiles;
         ScreenUtils.clear(0.0F, 0.0F, 0.0F, 1.0F, true);
         if (game.player.spawnIndex != -1) {
            tiles = game.map.interiorTiles.get(game.player.spawnIndex);
            interiorTilesIndex = game.player.spawnIndex;
         }

         Tile playerTile = tiles.get(game.player.spawnLoc);
         if (playerTile == null) {
            tiles = game.map.overworldTiles;
            playerTile = tiles.get(game.player.spawnLoc);
         }

         for (Action action : game.actionStack) {
            if (action instanceof PlayerMoving || action instanceof PlayerRunning || action instanceof PlayerLedgeJump || action instanceof PlayerLedgeJumpFast
               )
             {
               System.out.println("found standingaction");
               game.actionStack.remove(action);
               game.insertAction(new PlayerStanding(game, false, true));
               break;
            }
         }

         Route newRoute = playerTile.routeBelongsTo;
         if (newRoute != null && newRoute.name.contains("pkmnmansion")) {
            game.mapBatch.setColor(new Color(0.8F, 0.8F, 0.8F, 1.0F));
         } else if (newRoute == null || !newRoute.name.equals("regi_cave1")) {
            if (!game.map.timeOfDay.equals("night")) {
               game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
            } else {
               game.mapBatch.setColor(new Color(PkmnMap.nightColor));
            }
         }

         if (newRoute == null) {
            newRoute = new Route("", 2);
         }

         if (game.player.hmPokemon != null) {
            game.player.hmPokemon.position = game.player.spawnLoc.cpy();
            game.player.hmPokemon.dirFacing = "down";
            if (game.player.hmPokemon.standingAction instanceof Pokemon.Follow) {
               ((Pokemon.Follow)game.player.hmPokemon.standingAction).onPlayer = true;
            }
         }

         Route finalRoute = newRoute;
         game.insertAction(
            new DisplayText.Clear(
               game,
               new WaitFrames(
                  game,
                  3,
                  new DisplayText(
                     game,
                     "" + game.player.name.toUpperCase(Locale.ROOT) + " is out of useable POKéMON!",
                     null,
                     null,
                     new DisplayText(
                        game,
                        "" + game.player.name.toUpperCase(Locale.ROOT) + " whited out!",
                        null,
                        null,
                        new SetField(
                           game.player,
                           "position",
                           game.player.spawnLoc.cpy(),
                           new SetField(
                              game.map,
                              "tiles",
                              tiles,
                              new SetField(
                                 game.map,
                                 "interiorTilesIndex",
                                 interiorTilesIndex,
                                 new SetField(
                                    game.player,
                                    "dirFacing",
                                    "down",
                                    new SetField(
                                       game.player,
                                       "currSprite",
                                       game.player.standingSprites.get("down"),
                                       new RunCode(
                                          () -> {
                                             game.map.currRoute = finalRoute;
                                             game.insertAction(game.map.new ShadeEffect(finalRoute.type()));
                                          },
                                          new Game.SetCamPos(
                                             game.player.spawnLoc.cpy().add(16.0F, 0.0F),
                                             new SplitAction(
                                                new BattleFadeOut(game, 4, null),
                                                new BattleFadeOutMusic(
                                                   game,
                                                   new DisplayText(
                                                      game,
                                                      "Weary from battle, you flee to the last safe place...",
                                                      null,
                                                      null,
                                                      new BattleFadeOut.WhiteScreen(
                                                         false,
                                                         new SplitAction(
                                                            new SetField(
                                                               game.musicController,
                                                               "playerFainted",
                                                               false,
                                                               new SetField(
                                                                  game.musicController,
                                                                  "nightAlert",
                                                                  false,
                                                                  new SetField(game.musicController, "resumeOverworldMusic", true, null)
                                                               )
                                                            ),
                                                            new SetField(game, "playerCanMove", true, null)
                                                         )
                                                      )
                                                   )
                                                )
                                             )
                                          )
                                       )
                                    )
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
}
