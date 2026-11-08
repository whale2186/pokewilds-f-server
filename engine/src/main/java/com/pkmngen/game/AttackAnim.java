package com.pkmngen.game;

import java.util.Locale;

class AttackAnim extends Action {
   public Action.Layer layer = Action.Layer.map_500;
   Attack attack;
   boolean isFriendly;

   public AttackAnim(Game game, Attack attack, boolean isFriendly, Action nextAction) {
      super();
      this.attack = attack;
      this.isFriendly = isFriendly;
      this.nextAction = nextAction;
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      game.actionStack.remove(this);
      Action action;
      if (this.attack != null) {
         action = Battle.getAttackAction(game, this.attack, this.isFriendly, null);
      } else {
         action = new Action();
      }

      Pokemon friendlyPokemon;
      if (this.isFriendly) {
         friendlyPokemon = game.player.currPokemon;
         Pokemon enemyPokemon = game.battle.oppPokemon;
      } else {
         friendlyPokemon = game.battle.oppPokemon;
         Pokemon enemyPokemon = game.player.currPokemon;
      }

      String enemy = this.isFriendly ? "" : "Enemy ";
      if (friendlyPokemon.status != null) {
         if (friendlyPokemon.status.equals("poison")) {
            int damage = friendlyPokemon.maxStats.get("hp") / 8;
            if (damage < 1) {
               damage = 1;
            }

            action.append(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(
                        game,
                        enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' hurt by poison!",
                        null,
                        true,
                        true,
                        new Battle.LoadAndPlayAnimation(
                           game, "status_poison", friendlyPokemon, Battle.depleteHealth(game, !this.isFriendly, damage, new WaitFrames(game, 13, null))
                        )
                     )
                  )
               )
            );
         } else if (friendlyPokemon.status.equals("burn")) {
            int damage = friendlyPokemon.maxStats.get("hp") / 8;
            if (damage < 1) {
               damage = 1;
            }

            action.append(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(
                        game,
                        enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' hurt by its burn!",
                        null,
                        true,
                        false,
                        Battle.depleteHealth(game, !this.isFriendly, damage, new WaitFrames(game, 13, null))
                     )
                  )
               )
            );
         } else if (friendlyPokemon.status.equals("toxic")) {
            int damage = friendlyPokemon.maxStats.get("hp") * friendlyPokemon.statusCounter / 16;
            if (damage < 1) {
               damage = 1;
            }

            action.append(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(
                        game,
                        enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' hurt by poison!",
                        null,
                        true,
                        true,
                        new Battle.LoadAndPlayAnimation(
                           game, "status_poison", friendlyPokemon, Battle.depleteHealth(game, !this.isFriendly, damage, new WaitFrames(game, 13, null))
                        )
                     )
                  )
               )
            );
            friendlyPokemon.statusCounter++;
         }
      }

      if (friendlyPokemon.volatileStatus.contains("curse")) {
         int damage = friendlyPokemon.maxStats.get("hp") / 4;
         if (damage < 1) {
            damage = 1;
         }

         action.append(
            new DisplayText.Clear(
               game,
               new WaitFrames(
                  game,
                  3,
                  new DisplayText(
                     game,
                     enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' hurt by the CURSE!",
                     null,
                     true,
                     false,
                     Battle.depleteHealth(game, !this.isFriendly, damage, new WaitFrames(game, 13, null))
                  )
               )
            )
         );
      }

      action.append(this.nextAction);
      game.insertAction(action);
   }
}
