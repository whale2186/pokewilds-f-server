package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Locale;

class CheckEndOfBattle extends Action {
   public Action.Layer layer = Action.Layer.gui_129;

   public CheckEndOfBattle(Action nextAction) {
      super();
      this.nextAction = nextAction;
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
      game.actionStack.remove(this);
      if (game.battle.oppPokemon.currentStats.get("hp") <= 0) {
         Action nextAction = new Action() {
            @Override
            public String getCamera() {
               return "gui";
            }
         };
         ArrayList<Pokemon> participated = new ArrayList<>();

         for (Pokemon pokemon : game.player.pokemon) {
            if (pokemon.participatedInBattle && pokemon.currentStats.get("hp") > 0 && pokemon.level < 100) {
               if (pokemon == game.player.currPokemon) {
                  participated.add(0, pokemon);
               } else {
                  participated.add(pokemon);
               }
            }
         }

         if (!participated.isEmpty()) {
            for (Pokemon pokemon : participated) {
               int exp = game.battle.calcFaintExp(participated.size());
               pokemon.exp += exp;
               if (pokemon != game.player.currPokemon) {
                  nextAction.append(new DisplayText.Clear(game, null));
                  nextAction.append(new WaitFrames(game, 3, null));
               }

               nextAction.append(new DisplayText(game, pokemon.nickname.toUpperCase(Locale.ROOT) + " gained " + exp + " EXP. Points!", null, true, true, null));
               if (pokemon == game.player.currPokemon) {
                  nextAction.append(new GainExpAnimationGen2(pokemon, null));
               } else {
                  nextAction.append(new GainExpAnimation(pokemon, null));
               }
            }

            for (Pokemon pokemon : participated) {
               nextAction.append(new CheckEvo(pokemon, null));
            }
         }

         nextAction.append(
            new WaitFrames(
               game,
               58,
               new DisplayText.Clear(
                  game,
                  new SplitAction(
                     new BattleFadeOut(game, null),
                     new SplitAction(
                        new BattleFadeOutMusic(game, null), new WaitFrames(game, 30, new SetField(game.musicController, "resumeOverworldMusic", true, null))
                     )
                  )
               )
            )
         );
         if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
            SpecialMewtwo1 mewtwo = (SpecialMewtwo1)game.battle.oppPokemon;
            mewtwo.tile.nameUpper = "mewtwo_overworld_hidden";
            mewtwo.tile.init();
            nextAction.append(new DisplayText(game, "MEWTWO fled... it may return when it' had time to recover.", null, null, null));
         }

         if (game.battle.oppPokemon.standingAction != null) {
            game.battle.oppPokemon.removeDrawActions(game);
            game.battle.oppPokemon.standingAction = null;
            if (game.battle.oppPokemon.isTrapping) {
               Tile tile = game.map.tiles.get(game.battle.oppPokemon.position);
               if (tile.items != null) {
                  tile.items.remove(game.battle.oppPokemon.specie.name.toLowerCase(Locale.ROOT));
               }
            } else {
               game.map.pokemon.remove(game.battle.oppPokemon.position);
            }
         }

         nextAction.append(new SetField(game, "playerCanMove", true, null));
         game.insertAction(nextAction);
      } else {
         game.insertAction(this.nextAction);
      }
   }
}
