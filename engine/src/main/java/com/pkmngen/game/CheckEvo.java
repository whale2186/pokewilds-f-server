package com.pkmngen.game;

import java.util.Locale;
import java.util.Map;

class CheckEvo extends Action {
   Pokemon pokemon;

   public CheckEvo(Pokemon pokemon, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      if (this.pokemon.gainedLevel) {
         this.pokemon.gainedLevel = false;
         String evolveTo = null;

         for (int i = 1; i <= this.pokemon.level; i++) {
            if (Specie.gen2Evos.get(this.pokemon.specie.name.toLowerCase(Locale.ROOT)).containsKey(String.valueOf(i))) {
               evolveTo = Specie.gen2Evos.get(this.pokemon.specie.name.toLowerCase(Locale.ROOT)).get(String.valueOf(i));
               break;
            }
         }

         if (evolveTo == null && this.pokemon.happiness >= 120) {
            Map<String, String> evos = Specie.gen2Evos.get(this.pokemon.specie.name);

            for (String evo : evos.keySet()) {
               if (evo.equals("tr mornday") && game.map.timeOfDay.equals("day")) {
                  evolveTo = evos.get(evo);
                  break;
               }

               if (evo.equals("tr nite") && game.map.timeOfDay.equals("night")) {
                  evolveTo = evos.get(evo);
                  break;
               }

               if (evo.equals("tr anytime")) {
                  evolveTo = evos.get(evo);
                  break;
               }
            }
         }

         if (evolveTo == null) {
            Map<String, String> evos = Specie.gen2Evos.get(this.pokemon.specie.name);

            for (Pokemon currPokemon : game.player.pokemon) {
               if (currPokemon != this.pokemon && evos.containsKey(currPokemon.specie.name)) {
                  evolveTo = evos.get(currPokemon.specie.name);
                  break;
               }
            }
         }

         if (evolveTo == null) {
            Map<String, String> evos = Specie.gen2Evos.get(this.pokemon.specie.name);

            for (String attack : this.pokemon.attacks) {
               if (evos.containsKey(attack)) {
                  evolveTo = evos.get(attack);
                  break;
               }
            }
         }

         if (evolveTo != null) {
            this.nextAction = new DisplayText.Clear(
               game,
               new WaitFrames(
                  game,
                  3,
                  new WaitFrames(
                     game,
                     61,
                     new DisplayText(
                        game,
                        "What? " + this.pokemon.nickname.toUpperCase(Locale.ROOT) + " is evolving!",
                        null,
                        true,
                        false,
                        new WaitFrames(
                           game,
                           51,
                           new EvolutionAnim(
                              this.pokemon,
                              evolveTo,
                              new PlayMusic(
                                 this.pokemon,
                                 new SplitAction(
                                    new SetField(game.musicController, "startEvolveMusic", true, null),
                                    new EvolutionAnim.HandleInput(this.pokemon, evolveTo, this.nextAction)
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

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
