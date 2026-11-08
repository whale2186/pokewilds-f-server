package com.pkmngen.game;

import java.util.Locale;

class CheckMovesLearned extends Action {
   Pokemon pokemon;

   public CheckMovesLearned(Pokemon pokemon, Action nextAction) {
      super();
      this.pokemon = pokemon;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      Action action = new Action();
      if (this.pokemon.learnSet.containsKey(this.pokemon.level)) {
         for (String attack : this.pokemon.learnSet.get(this.pokemon.level)) {
            if (Pokemon.attacksImplemented.contains(attack.toLowerCase(Locale.ROOT))) {
               boolean alreadyLearned = false;

               for (int i = 0; i < 4; i++) {
                  if (this.pokemon.attacks[i] != null && this.pokemon.attacks[i].equals(attack)) {
                     alreadyLearned = true;
                     break;
                  }
               }

               if (!alreadyLearned) {
                  for (int i = 0; i < 4; i++) {
                     if (this.pokemon.attacks[i] == null) {
                        action.append(
                           new DisplayText.Clear(
                              game,
                              new WaitFrames(
                                 game,
                                 3,
                                 new DisplayText(
                                    game,
                                    this.pokemon.nickname.toUpperCase(Locale.ROOT) + " learned " + attack.toUpperCase(Locale.ROOT) + "!",
                                    "fanfare1.ogg",
                                    true,
                                    true,
                                    null
                                 )
                              )
                           )
                        );
                        this.pokemon.attacks[i] = attack;
                        if (game.type == Game.Type.CLIENT) {
                           game.client.sendTCP(new Network.LearnMove(game.player.network.id, 0, i, attack));
                        }

                        alreadyLearned = true;
                        break;
                     }
                  }

                  if (!alreadyLearned) {
                     action.append(
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game,
                              3,
                              new DisplayText(
                                 game,
                                 this.pokemon.nickname.toUpperCase(Locale.ROOT) + " is trying to learn " + attack.toUpperCase(Locale.ROOT) + ".",
                                 null,
                                 null,
                                 new DisplayText(game, "Which move should be forgotten?", null, true, true, new DrawAttacksMenu(attack, this.pokemon, null))
                              )
                           )
                        )
                     );
                  }
               }
            }
         }
      }

      action.append(this.nextAction);
      game.actionStack.remove(this);
      game.insertAction(action);
   }
}
