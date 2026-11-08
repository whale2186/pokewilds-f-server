package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.util.LinkedMusic;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class Battle {
   public static HashMap<String, Attack.Category> specPhysLookup = new HashMap<>();
   public static ArrayList<String> gen2PhysicalTypes;
   public Pokemon oppPokemon;
   DrawBattle drawAction;
   Music music;
   Music victoryFanfare;
   HashMap<String, HashMap<String, Float>> gen2TypeEffectiveness;
   HashMap<String, Attack> attacks = new HashMap<>();
   Battle.Network network = new Battle.Network();
   Runnable runnable;
   public int turnNumber = 1;
   public Pokemon.Generation generation = Pokemon.Generation.CRYSTAL;
   public boolean playerReflectDown = false;
   public boolean oppReflectDown = false;
   public int playerReflectCounter = 0;
   public int oppReflectCounter = 0;
   public boolean playerLightScreenDown = false;
   public boolean oppLightScreenDown = false;
   public int playerLightScreenCounter = 0;
   public int oppLightScreenCounter = 0;
   public String weather;
   public int weatherCounter = 0;
   public boolean playerAirborne = false;
   public boolean playerUnderground = false;
   public boolean oppAirborne = false;
   public boolean oppUnderground = false;
   public boolean playerSafeguardDown = false;
   public int playerSafeguardCounter = 0;
   public boolean oppSafeguardDown = false;
   public int oppSafeguardCounter = 0;
   public int playerLockedMove = -1;
   public int playerLockedMoveCounter = 0;
   public int oppLockedMove = -1;
   public int oppLockedMoveCounter = 0;
   public boolean playerSpikesDown = false;
   public boolean oppSpikesDown = false;

   static int gen2CalcDamage(Pokemon source, Attack attack, Pokemon target) {
      if (attack.name.equals("Mewtwo_Special1")) {
         float multiplier = 1.0F;
         String prevType = "";

         for (String type : target.types) {
            if (!type.equals(prevType)) {
               prevType = type;
               multiplier *= Game.staticGame.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase(Locale.ROOT));
            }
         }

         return (int)(30.0F * multiplier);
      } else if (attack.name.equals("Regigigas_Special1")) {
         float multiplier = 1.0F;
         String prevType = "";

         for (String type : target.types) {
            if (!type.equals(prevType)) {
               prevType = type;
               multiplier *= Game.staticGame.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase(Locale.ROOT));
            }
         }

         return (int)(30.0F * multiplier);
      } else {
         int power = attack.power;
         if (attack.name.equals("dragon energy")) {
            power = power * source.currentStats.get("hp") / source.maxStats.get("hp");
            if (power < 1) {
               power = 1;
            }
         } else if (attack.name.equals("crush grip")) {
            power = 1 + 120 * target.currentStats.get("hp") / target.maxStats.get("hp");
         }

         if (attack.name.equals("rollout")) {
            int lockedMoveCounter = Game.staticGame.battle.oppLockedMoveCounter;
            String prevAttack = null;
            if (DrawBattle.prevEnemyAttackIndex != -1) {
               prevAttack = Game.staticGame.battle.oppPokemon.attacks[DrawBattle.prevEnemyAttackIndex];
            }

            System.out.println();
            if (target == Game.staticGame.battle.oppPokemon) {
               lockedMoveCounter = Game.staticGame.battle.playerLockedMoveCounter;
               if (DrawBattle.prevFriendlyAttackIndex != -1) {
                  prevAttack = Game.staticGame.player.currPokemon.attacks[DrawBattle.prevFriendlyAttackIndex];
               }
            }

            if (prevAttack != null && prevAttack.equals("defense curl")) {
               power *= 2;
            }

            for (int i = 1; i < 6 - lockedMoveCounter; i++) {
               power *= 2;
            }
         }

         if (!attack.effect.equals("EFFECT_LEVEL_DAMAGE") && !attack.effect.equals("EFFECT_STATIC_DAMAGE") && !attack.effect.equals("EFFECT_PSYWAVE")) {
            int attackStat = attack.isPhysical ? source.currentStats.get("attack") : source.currentStats.get("specialAtk");
            int defenseStat = attack.isPhysical ? target.currentStats.get("defense") : target.currentStats.get("specialDef");
            if (attack.isCrit) {
               int attackStage = attack.isPhysical ? source.statStages.get("attack") : source.statStages.get("specialAtk");
               int defenseStage = attack.isPhysical ? target.statStages.get("attack") : target.statStages.get("specialAtk");
               if (attackStage >= defenseStage) {
                  attackStat = attack.isPhysical ? source.maxStats.get("attack") : source.maxStats.get("specialAtk");
                  defenseStat = attack.isPhysical ? target.maxStats.get("defense") : target.maxStats.get("specialDef");
               }
            }

            if (attack.effect.equals("EFFECT_SELFDESTRUCT")) {
               defenseStat /= 2;
            }

            int damage = (int)Math.floor(Math.floor(Math.floor(2 * source.level / 5 + 2) * attackStat * power / defenseStat) / 50.0) + 2;
            if (attack.isCrit) {
               damage *= 2;
            }

            boolean targetAirborne = false;
            boolean targetUnderground = false;
            if (target == Game.staticGame.battle.oppPokemon) {
               targetAirborne = Game.staticGame.battle.oppAirborne;
               targetUnderground = Game.staticGame.battle.oppUnderground;
            } else if (target == Game.staticGame.player.currPokemon) {
               targetAirborne = Game.staticGame.battle.playerAirborne;
               targetUnderground = Game.staticGame.battle.playerUnderground;
            }

            if ((attack.effect.equals("EFFECT_TWISTER") || attack.name.equals("gust")) && targetAirborne) {
               damage *= 2;
            }

            if ((attack.effect.equals("EFFECT_EARTHQUAKE") || attack.effect.equals("EFFECT_MAGNITUDE")) && targetUnderground) {
               damage *= 2;
            }

            if (damage > 997) {
               damage = 997;
            }

            damage += 2;
            if (source.types.contains(attack.type.toUpperCase(Locale.ROOT))) {
               damage = (int)(damage * 1.5F);
            }

            float multiplier = 1.0F;
            String prevType = "";

            for (String type : target.types) {
               if (!type.equals(prevType)) {
                  prevType = type;
                  multiplier *= Game.staticGame.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase(Locale.ROOT));
               }
            }

            damage = (int)(damage * multiplier);
            int randInt = Game.rand.nextInt(39) + 217;
            return damage * randInt / 255;
         } else {
            int damage = 0;
            if (attack.effect.equals("EFFECT_LEVEL_DAMAGE")) {
               damage = source.level;
            } else if (attack.effect.equals("EFFECT_STATIC_DAMAGE")) {
               damage = attack.power;
            } else if (attack.effect.equals("EFFECT_PSYWAVE")) {
               damage = (int)(source.level * 1.5F);
               damage = Game.rand.nextInt(damage);
               if (damage <= 0) {
                  damage = 1;
               }
            }

            float multiplier = 1.0F;
            String prevType = "";

            for (String type : target.types) {
               if (!type.equals(prevType)) {
                  prevType = type;
                  multiplier *= Game.staticGame.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase(Locale.ROOT));
               }
            }

            if (multiplier != 0.0F) {
               multiplier = 1.0F;
            }

            damage = (int)(damage * multiplier);
            System.out.println(attack.name + " " + damage);
            return damage;
         }
      }
   }

   static boolean gen2DetermineCrit(Pokemon source, Attack attack) {
      int c = 0;
      if (attack.name.equals("aero blast")
         || attack.name.equals("crabhammer")
         || attack.name.equals("cross chop")
         || attack.name.equals("karate chop")
         || attack.name.equals("razor leaf")
         || attack.name.equals("slash")) {
         c += 2;
      } else if (attack.name.equals("air cutter")) {
         c++;
      }

      short var3;
      if (c == 0) {
         var3 = 17;
      } else if (c == 1) {
         var3 = 32;
      } else if (c == 2) {
         var3 = 64;
      } else if (c == 3) {
         var3 = 85;
      } else {
         var3 = 128;
      }

      return Game.staticGame.map.rand.nextInt(256) < var3;
   }

   @Deprecated
   public static Action calcIfCaught(Game game, Action nextAction) {
      int maxRand = 150;
      int randomNum = game.map.rand.nextInt(maxRand + 1);
      int statusValue = 0;
      boolean breaksFree = false;
      int ball = 15;
      int adrenaline = game.player.adrenaline;
      if (adrenaline > 25) {
         adrenaline = 25;
      }

      int modFactor = 100;
      int f = (int)Math.floor(game.battle.oppPokemon.currentStats.get("catchRate") * 255 * 4 / (modFactor * ball));
      if (randomNum - statusValue > game.battle.oppPokemon.currentStats.get("catchRate")) {
      }

      int randomNum_M = game.map.rand.nextInt(256);
      if (f + adrenaline * 10 >= randomNum_M) {
         breaksFree = false;
      } else {
         breaksFree = true;
      }

      System.out.println("(randomNum_M / f / adr): (" + String.valueOf(randomNum_M) + " / " + f + " / +" + adrenaline * 10 + ")");
      if (!breaksFree) {
         return new CatchPokemonWobblesThenCatch(game, "", nextAction);
      } else {
         randomNum_M = game.battle.oppPokemon.currentStats.get("catchRate") * 100 / maxRand;
         if (randomNum_M >= 256) {
            return new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction)));
         } else {
            int s = 0;
            int x = randomNum_M * f / 255 + s;
            if (x < 10) {
               return new CatchPokemonMiss(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction)));
            } else if (x < 30) {
               return new CatchPokemonWobbles1Time(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction)));
            } else {
               return x < 70
                  ? new CatchPokemonWobbles2Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction)))
                  : new CatchPokemonWobbles3Times(game, new PrintAngryEating(game, new ChanceToRun(game, nextAction)));
            }
         }
      }
   }

   public static int gen2CalcIfCaught(Game game, Pokemon pokemon, String ballUsed) {
      ballUsed = ballUsed.toLowerCase(Locale.ROOT);
      if (ballUsed.equals("master ball")) {
         return -1;
      }

      int rateModified = pokemon.maxStats.get("catchRate");
      if (ballUsed.equals("great ball") || ballUsed.equals("park ball")) {
         rateModified = (int)(rateModified * 1.5F);
      } else if (ballUsed.equals("ultra ball")) {
         rateModified *= 2;
      } else if (ballUsed.equals("dusk ball")) {
         if (game.map.timeOfDay.equals("night") || game.map.tiles != game.map.overworldTiles) {
            rateModified = (int)(rateModified * 3.5F);
         }
      } else if (ballUsed.equals("fast ball")) {
         if (pokemon.baseStats.get("speed") >= 100) {
            rateModified *= 4;
         }
      } else if (ballUsed.equals("quick ball")) {
         if (game.battle.turnNumber == 1) {
            rateModified *= 5;
         }
      } else if (ballUsed.equals("net ball")) {
         if (game.battle.oppPokemon.types.contains("WATER") || game.battle.oppPokemon.types.contains("BUG")) {
            rateModified = (int)(rateModified * Math.min(1.0F + game.battle.turnNumber * 0.30004883F, 4.0F));
         }
      } else if (ballUsed.equals("dive ball")) {
         if (game.player.currFieldMove.equals("SURF") || game.player.currRod != null) {
            rateModified = (int)(rateModified * 3.5F);
         }
      } else if (ballUsed.equals("nest ball")) {
         if (game.battle.oppPokemon.level <= 29) {
            rateModified = (int)(rateModified * ((41.0F - game.battle.oppPokemon.level) / 10.0F));
         }
      } else if (ballUsed.equals("dream ball")) {
         if (game.battle.oppPokemon.status != null && game.battle.oppPokemon.status.equals("sleep")) {
            rateModified *= 4;
         }
      } else if (ballUsed.equals("heavy ball")) {
         float weight = Pokemon.weights.get(pokemon.dexNumber);
         int foundRate = rateModified + 30;
         if (weight < 100.0F) {
            foundRate = rateModified - 20;
         } else if (weight < 200.0F) {
            foundRate = rateModified;
         } else if (weight < 300.0F) {
            foundRate = rateModified + 20;
         }

         rateModified = foundRate;
      } else if (ballUsed.equals("level ball")) {
         if (game.player.currPokemon.level / 4 > pokemon.level) {
            rateModified *= 8;
         } else if (game.player.currPokemon.level / 2 > pokemon.level) {
            rateModified *= 4;
         } else if (game.player.currPokemon.level > pokemon.level) {
            rateModified *= 2;
         }
      } else if (ballUsed.equals("love ball")) {
         boolean oppGender = !pokemon.gender.equals("unknown")
            && !game.player.currPokemon.gender.equals("unknown")
            && !game.player.currPokemon.gender.equals(pokemon.gender);
         if (oppGender && game.player.currPokemon.specie.name.equals(pokemon.specie.name)) {
            rateModified *= 8;
         }
      } else if (ballUsed.equals("moon ball")) {
         String[] moonStoneMons = new String[]{"clefairy", "jigglypuff", "nidorina", "nidorino"};

         for (String name : moonStoneMons) {
            if (pokemon.specie.name.equals(name)) {
               rateModified *= 4;
               break;
            }
         }
      }

      if (rateModified > 255) {
         rateModified = 255;
      }

      int bonusStatus = 0;
      if (pokemon.status != null && (pokemon.status.equals("sleep") || pokemon.status.equals("freeze"))) {
         bonusStatus = 10;
      }

      int m = 3 * pokemon.maxStats.get("hp");
      int h = 2 * pokemon.currentStats.get("hp");
      if (m > 255) {
         m /= 2;
         m /= 2;
         h /= 2;
         h /= 2;
      }

      int a = (m - h) * rateModified / m;
      if (a < 1) {
         a = 1;
      }

      a += bonusStatus;
      if (a > 255) {
         a = 255;
      }

      System.out.println(a);
      if (game.map.rand.nextInt(256) <= a) {
         return -1;
      }

      int[] aLookup = new int[]{1, 2, 3, 4, 5, 7, 10, 15, 20, 30, 40, 50, 60, 80, 100, 120, 140, 160, 180, 200, 220, 240, 254, 255};
      int[] bLookup = new int[]{63, 75, 84, 90, 95, 103, 113, 126, 134, 149, 160, 169, 177, 191, 201, 211, 220, 227, 234, 240, 246, 251, 253, 255};
      int i = 0;
      int prevVal = -1;

      for (int val : aLookup) {
         if (a > prevVal && a <= val) {
            break;
         }

         prevVal = val;
         i++;
      }

      int b = bLookup[i];
      if (game.map.rand.nextInt(256) >= b) {
         return 0;
      } else if (game.map.rand.nextInt(256) >= b) {
         return 1;
      } else {
         return game.map.rand.nextInt(256) >= b ? 2 : 3;
      }
   }

   public static Action getAttackAction(Game game, Attack attack, boolean isFriendly, Action nextAction) {
      int power = 40;
      int accuracy = 100;
      byte var35;
      int var36;
      if (attack.name.equals("Aurora Beam")) {
         var35 = 65;
         var36 = 100;
      } else if (attack.name.equals("Clamp")) {
         var35 = 35;
         var36 = 85;
      } else if (attack.name.equals("Supersonic")) {
         var35 = 0;
         var36 = 55;
      } else if (attack.name.equals("Withdraw")) {
         var35 = 20;
         var36 = 100;
      } else {
         if (!attack.name.equals("Struggle")) {
            if (attack.name.equals("Psychic")) {
               if (isFriendly) {
                  return new Attack.Psychic(game, game.battle.oppPokemon, false, nextAction);
               }

               return new Attack.Psychic(game, game.player.currPokemon, false, nextAction);
            }

            if (attack.name.equals("Mewtwo_Special1")) {
               return new Attack.Mewtwo_Special1(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }

            if (attack.name.equals("Night Shade")) {
               if (isFriendly) {
                  return new Attack.Psychic(game, game.battle.oppPokemon, true, nextAction);
               }

               return new Attack.Psychic(game, game.player.currPokemon, true, nextAction);
            }

            if (attack.name.equals("Slash")) {
               if (isFriendly) {
                  return new Attack.Default(game, power, accuracy, nextAction);
               }

               return new Attack.Slash(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }

            if (attack.name.equals("Shadow Claw")) {
               if (isFriendly) {
                  return new Attack.Default(game, power, accuracy, nextAction);
               }

               return new Attack.ShadowClaw(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }

            if (attack.name.equals("Lick")) {
               if (isFriendly) {
                  return new Attack.Default(game, power, accuracy, nextAction);
               }

               return new Attack.Lick(game, game.battle.oppPokemon, game.player.currPokemon, nextAction);
            }

            if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon) && !isFriendly) {
               SpecialBattleMewtwo.specialAttackCounter++;
               if (SpecialBattleMewtwo.specialAttackCounter >= 3) {
                  Attack mewtwoSpecial1 = game.battle.attacks.get("Mewtwo_Special1");
                  mewtwoSpecial1.damage = gen2CalcDamage(game.battle.oppPokemon, mewtwoSpecial1, game.player.currPokemon);
                  nextAction = new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           "A wave of psychic power unleashes!",
                           null,
                           true,
                           false,
                           getAttackAction(
                              game,
                              mewtwoSpecial1,
                              !isFriendly,
                              depleteHealth(
                                 game,
                                 false,
                                 mewtwoSpecial1.damage,
                                 true,
                                 new WaitFrames(game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, nextAction)))
                              )
                           )
                        )
                     )
                  );
                  SpecialBattleMewtwo.specialAttackCounter = 0;
               }
            }

            if (RegigigasBattle.Draw.class.isInstance(game.battle.drawAction) && !isFriendly) {
               RegigigasBattle.Draw.specialAttackCounter++;
               if (RegigigasBattle.Draw.specialAttackCounter >= 3) {
                  Attack regigigasSpecial = game.battle.attacks.get("Regigigas_Special1");
                  regigigasSpecial.damage = gen2CalcDamage(game.battle.oppPokemon, regigigasSpecial, game.player.currPokemon);
                  String effectiveness = "neutral_effective";
                  float multiplier = 1.0F;
                  String prevType = "";

                  for (String type : game.player.currPokemon.types) {
                     if (!type.equals(prevType)) {
                        prevType = type;
                        multiplier *= game.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase(Locale.ROOT));
                     }
                  }

                  if (multiplier > 1.0F) {
                     effectiveness = "super_effective";
                  } else if (multiplier == 1.0F) {
                     effectiveness = "neutral_effective";
                  } else if (multiplier > 0.0F) {
                     effectiveness = "not_very_effective";
                  }

                  nextAction = new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           "The ground is shaking violently!",
                           null,
                           null,
                           new RegigigasBattle.SpecialAttack(
                              new Battle.LoadAndPlayAnimation(
                                 game,
                                 effectiveness,
                                 game.player.currPokemon,
                                 depleteHealth(
                                    game,
                                    false,
                                    regigigasSpecial.damage,
                                    true,
                                    new WaitFrames(game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, nextAction)))
                                 )
                              )
                           )
                        )
                     )
                  );
                  RegigigasBattle.Draw.specialAttackCounter = 0;
               }
            }

            Pokemon friendlyPokemon;
            Pokemon enemyPokemon;
            if (isFriendly) {
               friendlyPokemon = game.player.currPokemon;
               enemyPokemon = game.battle.oppPokemon;
            } else {
               friendlyPokemon = game.battle.oppPokemon;
               enemyPokemon = game.player.currPokemon;
            }

            Action attackAction = new Action() {
               @Override
               public String getCamera() {
                  return "gui";
               }
            };
            String enemy = isFriendly ? "" : "Enemy ";
            if (friendlyPokemon.disabledCounter > 0) {
               friendlyPokemon.disabledCounter--;
            }

            if (friendlyPokemon.disabledCounter <= 0 && friendlyPokemon.disabledIndex != -1) {
               attackAction.append(
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           enemy
                              + friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                              + "' "
                              + friendlyPokemon.attacks[friendlyPokemon.disabledIndex]
                              + " is no longer disabled!",
                           null,
                           true,
                           false,
                           null
                        )
                     )
                  )
               );
               friendlyPokemon.disabledIndex = -1;
            }

            if (friendlyPokemon.flinched) {
               return new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(
                        game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " flinched!", null, true, true, new WaitFrames(game, 10, nextAction)
                     )
                  )
               );
            }

            int previousAttackIndex;
            if (isFriendly) {
               previousAttackIndex = DrawBattle.prevFriendlyAttackIndex;
            } else {
               previousAttackIndex = DrawBattle.prevEnemyAttackIndex;
            }

            String previousAttack = "";
            if (previousAttackIndex >= 0) {
               previousAttack = friendlyPokemon.attacks[previousAttackIndex];
            }

            if ("hyper beam".equals(previousAttack)) {
               if (isFriendly) {
                  DrawBattle.prevFriendlyAttackIndex = -1;
               } else {
                  DrawBattle.prevEnemyAttackIndex = -1;
               }

               return new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game,
                     3,
                     new DisplayText(
                        game,
                        enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " must recharge!",
                        null,
                        true,
                        true,
                        new WaitFrames(game, 10, nextAction)
                     )
                  )
               );
            }

            int attackIndex = -1;

            for (int i = 0; i < friendlyPokemon.attacks.length; i++) {
               if (friendlyPokemon.attacks[i] != null && friendlyPokemon.attacks[i].equals(attack.name)) {
                  attackIndex = i;
                  break;
               }
            }

            if (attackIndex > -1 && attackIndex == friendlyPokemon.disabledIndex) {
               int lockedMove = game.battle.oppLockedMove;
               if (isFriendly) {
                  lockedMove = game.battle.playerLockedMove;
               }

               boolean resetHidden = false;
               if (lockedMove != -1) {
                  if (friendlyPokemon.attacks[lockedMove].equals("dig") || friendlyPokemon.attacks[lockedMove].equals("fly")) {
                     resetHidden = true;
                  }

                  if (isFriendly) {
                     game.battle.playerLockedMove = -1;
                     if (friendlyPokemon.attacks[lockedMove].equals("dig")) {
                        game.battle.playerUnderground = false;
                     } else if (friendlyPokemon.attacks[lockedMove].equals("fly")) {
                        game.battle.playerAirborne = false;
                     }
                  } else {
                     game.battle.oppLockedMove = -1;
                     if (friendlyPokemon.attacks[lockedMove].equals("dig")) {
                        game.battle.oppUnderground = false;
                     } else if (friendlyPokemon.attacks[lockedMove].equals("fly")) {
                        game.battle.oppAirborne = false;
                     }
                  }
               }

               Action disabledAction = new Action();
               disabledAction.append(new DisplayText.Clear(game, null));
               disabledAction.append(new WaitFrames(game, 3, null));
               disabledAction.append(
                  new DisplayText(
                     game,
                     enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' " + attack.name.toUpperCase(Locale.ROOT) + " is DISABLED!",
                     null,
                     null,
                     null
                  )
               );
               disabledAction.append(new WaitFrames(game, 10, null));
               if (resetHidden) {
                  String field = "hideOppPokemon";
                  if (isFriendly) {
                     field = "hideOwnPokemon";
                  }

                  disabledAction.append(new SetField(game.battle.drawAction, field, false, null));
               }

               disabledAction.append(nextAction);
               return disabledAction;
            }

            if (friendlyPokemon.status != null) {
               if (friendlyPokemon.status.equals("sleep")) {
                  friendlyPokemon.statusCounter--;
                  if (friendlyPokemon.statusCounter > 0) {
                     return new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(
                              game,
                              enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " is fast asleep!",
                              null,
                              true,
                              true,
                              new WaitFrames(game, 23, new Battle.LoadAndPlayAnimation(game, "status_sleep", friendlyPokemon, nextAction))
                           )
                        )
                     );
                  }

                  attackAction.append(
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(
                              game,
                              enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " woke up!",
                              null,
                              true,
                              true,
                              new SetField(friendlyPokemon, "status", null, null)
                           )
                        )
                     )
                  );
               } else if (friendlyPokemon.status.equals("paralyze")) {
                  if (game.map.rand.nextInt(256) < 128) {
                     return new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(
                              game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " is fully paralyzed!", null, true, true, nextAction
                           )
                        )
                     );
                  }
               } else if (friendlyPokemon.status.equals("freeze")) {
                  if (game.map.rand.nextInt(256) >= 51) {
                     return new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " is frozen solid!", null, true, true, nextAction)
                        )
                     );
                  }

                  attackAction.append(
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(
                              game,
                              enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " thawed out!",
                              null,
                              true,
                              true,
                              new SetField(friendlyPokemon, "status", null, null)
                           )
                        )
                     )
                  );
               }
            }

            ArrayList<String> removeStatus = new ArrayList<>();
            boolean returnAction = false;
            Collections.sort(friendlyPokemon.volatileStatus);

            for (int i = 0; i < friendlyPokemon.volatileStatus.size(); i++) {
               String status = friendlyPokemon.volatileStatus.get(i);
               if (status.equals("confuse")) {
                  friendlyPokemon.volatileStatusCounter.put(status, friendlyPokemon.volatileStatusCounter.get(status) - 1);
                  if (friendlyPokemon.volatileStatusCounter.get(status) > 0) {
                     attackAction.append(
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game,
                              3,
                              new DisplayText(
                                 game,
                                 enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " is confused!",
                                 null,
                                 true,
                                 true,
                                 new WaitFrames(game, 24, new Battle.LoadAndPlayAnimation(game, "status_confuse", friendlyPokemon, null))
                              )
                           )
                        )
                     );
                     if (game.map.rand.nextInt(256) < 128) {
                        Attack confustionAtk = game.battle.attacks.get("confusion_hit");
                        confustionAtk.damage = gen2CalcDamage(friendlyPokemon, confustionAtk, friendlyPokemon);
                        attackAction.append(
                           new DisplayText.Clear(
                              game,
                              new WaitFrames(
                                 game,
                                 3,
                                 new DisplayText(
                                    game,
                                    "It hurt itself in confusion!",
                                    null,
                                    true,
                                    false,
                                    new Battle.LoadAndPlayAnimation(
                                       game,
                                       "struggle",
                                       friendlyPokemon,
                                       depleteHealth(game, !isFriendly, confustionAtk.damage, new WaitFrames(game, 13, nextAction))
                                    )
                                 )
                              )
                           )
                        );
                        returnAction = true;
                        break;
                     }
                  } else {
                     removeStatus.add(status);
                     attackAction.append(
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game,
                              3,
                              new DisplayText(
                                 game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " snapped out of confusion!", null, true, true, null
                              )
                           )
                        )
                     );
                  }
               } else if (status.equals("attract")) {
                  String enemy2 = !isFriendly ? "" : "enemy ";
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(
                     new DisplayText(
                        game,
                        enemy
                           + friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + " is in love with "
                           + enemy2
                           + enemyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + "!",
                        null,
                        true,
                        true,
                        null
                     )
                  );
                  attackAction.append(new WaitFrames(game, 24, null));
                  attackAction.append(new Battle.LoadAndPlayAnimation(game, "love", enemyPokemon, null));
                  if (game.map.rand.nextInt(256) < 128) {
                     attackAction.append(new DisplayText.Clear(game, null));
                     attackAction.append(new WaitFrames(game, 3, null));
                     attackAction.append(
                        new DisplayText(
                           game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' infatuation kept it from attacking!", null, true, true, null
                        )
                     );
                     returnAction = true;
                     break;
                  }
               }
            }

            for (int i = 0; i < removeStatus.size(); i++) {
               for (int j = 0; j < friendlyPokemon.volatileStatus.size(); j++) {
                  if (friendlyPokemon.volatileStatus.get(j) == removeStatus.get(i)) {
                     friendlyPokemon.volatileStatus.remove(j);
                     break;
                  }
               }
            }

            if (returnAction) {
               return attackAction;
            }

            int accuracyStage = friendlyPokemon.statStages.get("accuracy");
            float multiplier = (float)Math.max(3, 3 + accuracyStage) / Math.max(3, 3 - accuracyStage);
            var36 = (int)(attack.accuracy * 255 / 100 * multiplier);
            if (var36 < 1) {
               var36 = 1;
            }

            int evasionStage = enemyPokemon.statStages.get("evasion");
            multiplier = (float)Math.max(3, 3 - evasionStage) / Math.max(3, 3 + evasionStage);
            var36 = (int)(var36 * multiplier);
            if (var36 < 1) {
               var36 = 1;
            }

            if (var36 > 255) {
               var36 = 255;
            }

            boolean attackMisses = var36 < 255 && game.map.rand.nextInt(256) >= var36;
            boolean targetAirborne = isFriendly ? game.battle.oppAirborne : game.battle.playerAirborne;
            boolean userAirborne = isFriendly ? game.battle.playerAirborne : game.battle.oppAirborne;
            boolean targetUnderground = isFriendly ? game.battle.oppUnderground : game.battle.playerUnderground;
            boolean userUnderground = isFriendly ? game.battle.playerUnderground : game.battle.oppUnderground;
            if (targetAirborne
               && !attack.name.equals("gust")
               && !attack.name.equals("thunder")
               && !attack.name.equals("twister")
               && !attack.name.equals("whirlwind")) {
               attackMisses = true;
            }

            if (targetUnderground && !attack.name.equals("earthquake") && !attack.name.equals("magnitude") && !attack.name.equals("fissure")) {
               attackMisses = true;
            }

            if (attack.effect.equals("EFFECT_ALWAYS_HIT")) {
               if (game.battle.generation == Pokemon.Generation.CRYSTAL) {
                  if (!targetAirborne && !targetUnderground) {
                     attackMisses = false;
                  }
               } else {
                  attackMisses = false;
               }
            }

            if (attack.name.equals("thunder") && game.battle.weather != null && game.battle.weather.equals("rain dance")) {
               attackMisses = false;
               if (targetUnderground) {
                  attackMisses = true;
               }
            }

            boolean setup = false;
            if (attack.effect.equals("EFFECT_FLY")) {
               if (attack.name.equals("dig") && !userUnderground || attack.name.equals("fly") && !userAirborne) {
                  attackMisses = false;
                  setup = true;
               }
            } else if (attack.effect.equals("EFFECT_SOLARBEAM")) {
               boolean sunnyDay = false;
               if (game.battle.weather != null && game.battle.weather.equals("sunny day")) {
                  sunnyDay = true;
               }

               if (!sunnyDay) {
                  String prevAttack = null;
                  int lockedMove = game.battle.oppLockedMove;
                  int prevAttackIndex = DrawBattle.prevEnemyAttackIndex;
                  if (prevAttackIndex >= 0) {
                     prevAttack = game.battle.oppPokemon.attacks[prevAttackIndex];
                  }

                  if (isFriendly) {
                     prevAttack = null;
                     prevAttackIndex = DrawBattle.prevFriendlyAttackIndex;
                     if (prevAttackIndex >= 0) {
                        prevAttack = game.player.currPokemon.attacks[prevAttackIndex];
                     }

                     lockedMove = game.battle.playerLockedMove;
                  }

                  boolean setupCompleted = false;
                  if (prevAttack != null && prevAttack.equals(attack.name) && lockedMove == prevAttackIndex) {
                     setupCompleted = true;
                  }

                  if (!setupCompleted) {
                     attackMisses = false;
                     setup = true;
                  }
               }
            } else if (attack.effect.equals("EFFECT_RAZOR_WIND") || attack.effect.equals("EFFECT_SKY_ATTACK")) {
               String prevAttack = null;
               int lockedMove = game.battle.oppLockedMove;
               int prevAttackIndex = DrawBattle.prevEnemyAttackIndex;
               if (prevAttackIndex >= 0) {
                  prevAttack = game.battle.oppPokemon.attacks[prevAttackIndex];
               }

               if (isFriendly) {
                  prevAttack = null;
                  prevAttackIndex = DrawBattle.prevFriendlyAttackIndex;
                  if (prevAttackIndex >= 0) {
                     prevAttack = game.player.currPokemon.attacks[prevAttackIndex];
                  }

                  lockedMove = game.battle.playerLockedMove;
               }

               boolean setupCompleted = false;
               if (prevAttack != null && prevAttack.equals(attack.name) && lockedMove == prevAttackIndex) {
                  setupCompleted = true;
               }

               if (!setupCompleted) {
                  attackMisses = false;
                  setup = true;
               }
            }

            String usedText = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " used " + attack.name.toUpperCase(Locale.ROOT) + "!";
            if (setup) {
               usedText = " ";
            }

            attackAction.append(new DisplayText.Clear(game, null));
            attackAction.append(new WaitFrames(game, 3, null));
            attackAction.append(new DisplayText(game, usedText, null, true, false, null));
            boolean presentHealed = false;
            if (attack.effect.equals("EFFECT_PRESENT")) {
               int randomNum = Game.rand.nextInt(100);
               if (randomNum < 40) {
                  attack.power = 40;
               } else if (randomNum < 70) {
                  attack.power = 80;
               } else if (randomNum < 80) {
                  attack.power = 120;
               } else {
                  attack.power = 0;
                  presentHealed = true;
               }
            }

            if (attack.power != 0) {
               if (attackMisses) {
                  int lockedMove = game.battle.oppLockedMove;
                  if (isFriendly) {
                     lockedMove = game.battle.playerLockedMove;
                  }

                  boolean resetHidden = false;
                  if (lockedMove != -1) {
                     if (friendlyPokemon.attacks[lockedMove].equals("dig") || friendlyPokemon.attacks[lockedMove].equals("fly")) {
                        resetHidden = true;
                     }

                     if (isFriendly) {
                        game.battle.playerLockedMove = -1;
                        if (friendlyPokemon.attacks[lockedMove].equals("dig")) {
                           game.battle.playerUnderground = false;
                        } else if (friendlyPokemon.attacks[lockedMove].equals("fly")) {
                           game.battle.playerAirborne = false;
                        }
                     } else {
                        game.battle.oppLockedMove = -1;
                        if (friendlyPokemon.attacks[lockedMove].equals("dig")) {
                           game.battle.oppUnderground = false;
                        } else if (friendlyPokemon.attacks[lockedMove].equals("fly")) {
                           game.battle.oppAirborne = false;
                        }
                     }
                  }

                  attackAction.append(
                     new WaitFrames(
                        game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "The attack missed!", null, true, true, null)))
                     )
                  );
                  if (resetHidden) {
                     String field = "hideOppPokemon";
                     if (isFriendly) {
                        field = "hideOwnPokemon";
                     }

                     attackAction.append(new SetField(game.battle.drawAction, field, false, null));
                  }

                  attackAction.append(nextAction);
                  return attackAction;
               }

               if (attack.effect.equals("EFFECT_ROLLOUT")) {
                  if (isFriendly) {
                     if (game.battle.playerLockedMove == -1) {
                        game.battle.playerLockedMove = attackIndex;
                        game.battle.playerLockedMoveCounter = 5;
                     }
                  } else if (game.battle.oppLockedMove == -1) {
                     game.battle.oppLockedMove = attackIndex;
                     game.battle.oppLockedMoveCounter = 5;
                  }
               }

               String finalAttackName = attack.name;
               if (setup) {
                  String text = null;
                  if (isFriendly) {
                     game.battle.playerLockedMove = attackIndex;
                     game.battle.playerLockedMoveCounter = 2;
                  } else {
                     game.battle.oppLockedMove = attackIndex;
                     game.battle.oppLockedMoveCounter = 2;
                  }

                  if (attack.name.equals("dig")) {
                     text = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " dug a hole!";
                     if (isFriendly) {
                        game.battle.playerUnderground = true;
                     } else {
                        game.battle.oppUnderground = true;
                     }
                  } else if (attack.name.equals("fly")) {
                     text = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " flew up high!";
                     if (isFriendly) {
                        game.battle.playerAirborne = true;
                     } else {
                        game.battle.oppAirborne = true;
                     }
                  } else if (attack.name.equals("solarbeam")) {
                     text = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " took in sunlight!";
                  } else if (attack.name.equals("razor wind")) {
                     text = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " made a whirlwind!";
                  } else if (attack.name.equals("sky attack")) {
                     text = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " is glowing!";
                  }

                  attackAction.append(new Battle.LoadAndPlayAnimation(game, finalAttackName + " setup", enemyPokemon, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, text, null, null, null));
               } else {
                  attack.isCrit = gen2DetermineCrit(friendlyPokemon, attack);
                  attack.damage = gen2CalcDamage(friendlyPokemon, attack, enemyPokemon);
                  multiplier = 1.0F;
                  String prevType = "";

                  for (String type : enemyPokemon.types) {
                     if (!type.equals(prevType)) {
                        prevType = type;
                        multiplier *= game.battle.gen2TypeEffectiveness.get(attack.type).get(type.toLowerCase(Locale.ROOT));
                     }
                  }

                  if ((attack.effect.equals("EFFECT_LEVEL_DAMAGE") || attack.effect.equals("EFFECT_STATIC_DAMAGE")) && multiplier != 0.0F) {
                     multiplier = 1.0F;
                  }

                  String effectiveness = "neutral_effective";
                  String text_string = "";
                  if (multiplier > 1.0F) {
                     effectiveness = "super_effective";
                     text_string = "It' super- effective!";
                  } else if (multiplier == 1.0F) {
                     effectiveness = "neutral_effective";
                  } else {
                     if (!(multiplier > 0.0F)) {
                        int lockedMove = game.battle.oppLockedMove;
                        if (isFriendly) {
                           lockedMove = game.battle.playerLockedMove;
                        }

                        boolean resetHidden = false;
                        if (lockedMove != -1) {
                           if (friendlyPokemon.attacks[lockedMove].equals("dig") || friendlyPokemon.attacks[lockedMove].equals("fly")) {
                              resetHidden = true;
                           }

                           if (isFriendly) {
                              game.battle.playerLockedMove = -1;
                              if (friendlyPokemon.attacks[lockedMove].equals("dig")) {
                                 game.battle.playerUnderground = false;
                              } else if (friendlyPokemon.attacks[lockedMove].equals("fly")) {
                                 game.battle.playerAirborne = false;
                              }
                           } else {
                              game.battle.oppLockedMove = -1;
                              if (friendlyPokemon.attacks[lockedMove].equals("dig")) {
                                 game.battle.oppUnderground = false;
                              } else if (friendlyPokemon.attacks[lockedMove].equals("fly")) {
                                 game.battle.oppAirborne = false;
                              }
                           }
                        }

                        attackAction.append(
                           new WaitFrames(
                              game,
                              30,
                              new DisplayText.Clear(
                                 game,
                                 new WaitFrames(
                                    game,
                                    3,
                                    new DisplayText(
                                       game, "It doesnì affect " + enemy + enemyPokemon.nickname.toUpperCase(Locale.ROOT) + "!", null, true, true, null
                                    )
                                 )
                              )
                           )
                        );
                        if (resetHidden) {
                           String field = "hideOppPokemon";
                           if (isFriendly) {
                              field = "hideOwnPokemon";
                           }

                           attackAction.append(new SetField(game.battle.drawAction, field, false, null));
                        }

                        attackAction.append(nextAction);
                        return attackAction;
                     }

                     effectiveness = "not_very_effective";
                     text_string = "It' not very effective...";
                  }

                  if (attack.effect.equals("EFFECT_FALSE_SWIPE") && attack.damage >= enemyPokemon.currentStats.get("hp")) {
                     attack.damage = enemyPokemon.currentStats.get("hp") - 1;
                     if (attack.damage < 1) {
                        attack.damage = 0;
                     }
                  }

                  if (attack.effect.equals("EFFECT_MULTI_HIT") || attack.effect.equals("EFFECT_DOUBLE_HIT")) {
                     int numHits = 1;
                     if (attack.effect.equals("EFFECT_MULTI_HIT")) {
                        numHits = Game.rand.nextInt(256);
                        if (numHits < 96) {
                           numHits = 1;
                        } else if (numHits < 192) {
                           numHits = 2;
                        } else if (numHits < 224) {
                           numHits = 3;
                        } else {
                           numHits = 4;
                        }
                     }

                     ArrayList<String> leftRightAnims = new ArrayList<>();
                     leftRightAnims.add("doubleslap");
                     leftRightAnims.add("double kick");
                     leftRightAnims.add("fury swipes");
                     boolean usesLeftRightAnim = false;
                     if (leftRightAnims.contains(attack.name)) {
                        usesLeftRightAnim = true;
                     }

                     for (int i = 0; i < numHits; i++) {
                        String attackName = attack.name;
                        if (usesLeftRightAnim) {
                           if ((i & 1) == 0) {
                              attackName = attackName + " left";
                              finalAttackName = attack.name + " right";
                           } else {
                              attackName = attackName + " right";
                              finalAttackName = attack.name + " left";
                           }
                        }

                        attackAction.append(new Battle.LoadAndPlayAnimation(game, attackName, enemyPokemon, null));
                        attackAction.append(depleteHealth(game, isFriendly, attack.damage, false, new WaitFrames(game, 13, new WaitFrames(game, 30, null))));
                        if (attack.isCrit) {
                           attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "Critical hit!", null, null, null))));
                        }

                        attackAction.append(detectFaint(game, isFriendly, null));
                        attack.isCrit = gen2DetermineCrit(friendlyPokemon, attack);
                        attack.damage = gen2CalcDamage(friendlyPokemon, attack, enemyPokemon);
                     }
                  }

                  attackAction.append(new Battle.LoadAndPlayAnimation(game, finalAttackName, enemyPokemon, null));
                  attackAction.append(
                     new Battle.LoadAndPlayAnimation(
                        game, effectiveness, enemyPokemon, depleteHealth(game, isFriendly, attack.damage, false, new WaitFrames(game, 13, null))
                     )
                  );
                  if (attack.isCrit) {
                     attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "Critical hit!", null, null, null))));
                  }

                  if (!effectiveness.equals("neutral_effective")) {
                     attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, text_string, null, true, true, null))));
                  }

                  if (attack.effect.contains("FLINCH_HIT") || attack.effect.contains("EFFECT_TWISTER")) {
                     var36 = attack.effectChance * 255 / 100;
                     attackMisses = var36 < 255 && game.map.rand.nextInt(256) >= var36;
                     if (!attackMisses) {
                        enemyPokemon.flinched = true;
                     }
                  }

                  if (attack.effect.equals("EFFECT_FLY")) {
                     if (attack.name.equals("dig")) {
                        if (isFriendly) {
                           game.battle.playerUnderground = false;
                        } else {
                           game.battle.oppUnderground = false;
                        }
                     } else if (attack.name.equals("fly")) {
                        if (isFriendly) {
                           game.battle.playerAirborne = false;
                        } else {
                           game.battle.oppAirborne = false;
                        }
                     }
                  }
               }
            }

            if (isFriendly) {
               DrawBattle.prevFriendlyAttackIndex = attackIndex;
            } else {
               DrawBattle.prevEnemyAttackIndex = attackIndex;
            }

            if (attack.effect.equals("EFFECT_RECOIL_HIT") || attack.name.equals("flare blitz")) {
               int recoilDamage = attack.damage / 4;
               if (attack.name.equals("brave bird")
                  || attack.name.equals("flare blitz")
                  || attack.name.equals("volt tackle")
                  || attack.name.equals("wood hammer")) {
                  recoilDamage = attack.damage / 3;
               } else if (attack.name.equals("head smash") || attack.name.equals("light of ruin")) {
                  recoilDamage = attack.damage / 2;
               }

               attackAction.append(
                  depleteHealth(
                     game,
                     !isFriendly,
                     recoilDamage,
                     false,
                     new WaitFrames(
                        game,
                        13,
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game, 3, new DisplayText(game, friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' hit with recoil!", null, null, null)
                           )
                        )
                     )
                  )
               );
            }

            if (attack.effect.equals("EFFECT_HEAL")
               || attack.effect.equals("EFFECT_SYNTHESIS")
               || attack.effect.equals("EFFECT_LEECH_HIT")
               || attack.effect.equals("EFFECT_DRAINING_KISS")
               || attack.effect.equals("EFFECT_MORNING_SUN")
               || attack.effect.equals("EFFECT_MOONLIGHT")
               || presentHealed) {
               int amount = friendlyPokemon.maxStats.get("hp") / 2;
               if (attack.effect.equals("EFFECT_SYNTHESIS")) {
                  amount = friendlyPokemon.maxStats.get("hp") / 4;
                  if (game.map.timeOfDay.equals("day")) {
                     amount *= 2;
                  }
               } else if (attack.effect.equals("EFFECT_LEECH_HIT")) {
                  amount = attack.damage / 2;
                  if (amount < 1) {
                     amount = 1;
                  }

                  int enemyHp = enemyPokemon.currentStats.get("hp");
                  if (amount > enemyHp) {
                     amount = enemyHp;
                  }
               } else if (attack.effect.equals("EFFECT_DRAINING_KISS")) {
                  amount = 3 * attack.damage / 4;
                  if (amount < 1) {
                     amount = 1;
                  }

                  int enemyHp = enemyPokemon.currentStats.get("hp");
                  if (amount > enemyHp) {
                     amount = enemyHp;
                  }
               } else if (attack.effect.equals("EFFECT_MORNING_SUN")) {
                  amount = friendlyPokemon.maxStats.get("hp") / 4;
                  if (game.map.timeOfDay.equals("day")) {
                     amount *= 2;
                  }
               } else if (attack.effect.equals("EFFECT_MOONLIGHT")) {
                  amount = friendlyPokemon.maxStats.get("hp") / 4;
                  if (game.map.timeOfDay.equals("night")) {
                     amount *= 2;
                  }
               } else if (attack.effect.equals("EFFECT_PRESENT")) {
                  amount = enemyPokemon.maxStats.get("hp") / 4;
                  isFriendly = !isFriendly;
                  Pokemon temp = friendlyPokemon;
                  friendlyPokemon = enemyPokemon;
                  enemyPokemon = temp;
               }

               enemy = isFriendly ? "" : "Enemy ";
               if (friendlyPokemon.currentStats.get("hp") >= friendlyPokemon.maxStats.get("hp")) {
                  attackAction.append(
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game, 3, new DisplayText(game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' HP is full!", null, false, true, null)
                        )
                     )
                  );
               } else {
                  if (!attack.effect.contains("HIT")) {
                     attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  }

                  attackAction.append(
                     new RestoreHealth(
                        friendlyPokemon,
                        -amount,
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game, 3, new DisplayText(game, friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " regained health!", null, false, true, null)
                           )
                        )
                     )
                  );
               }
            }

            if (attack.effect.equals("EFFECT_SELFDESTRUCT")) {
               friendlyPokemon.currentStats.put("hp", 0);
            }

            if (attack.effect.equals("EFFECT_CURSE")) {
               if (!friendlyPokemon.types.contains("GHOST")) {
                  boolean attackWorked = friendlyPokemon.gen2ApplyStatStage("attack", 1);
                  boolean defenseWorked = friendlyPokemon.gen2ApplyStatStage("defense", 1);
                  if (!attackWorked && !defenseWorked) {
                     attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, false, true, null))));
                  } else {
                     attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                     attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, null)));
                     boolean speedWorked = friendlyPokemon.gen2ApplyStatStage("speed", -1);
                     String text = "";
                     String stat = "speed";
                     int stage = -1;
                     if (speedWorked) {
                        text = friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " fell!";
                     } else {
                        text = friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + "' "
                           + stat.toUpperCase(Locale.ROOT)
                           + " wonì go any "
                           + (stage > 0 ? "higher!" : "lower!");
                     }

                     attackAction.append(new DisplayText(game, text, null, null, null));
                     stat = "attack";
                     int var140 = 1;
                     if (attackWorked) {
                        text = friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " went up!";
                     } else {
                        text = friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + "' "
                           + stat.toUpperCase(Locale.ROOT)
                           + " wonì go any "
                           + (var140 > 0 ? "higher!" : "lower!");
                     }

                     attackAction.append(new DisplayText(game, text, null, null, null));
                     stat = "defense";
                     var140 = 1;
                     if (defenseWorked) {
                        text = friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " went up!";
                     } else {
                        text = friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + "' "
                           + stat.toUpperCase(Locale.ROOT)
                           + " wonì go any "
                           + (var140 > 0 ? "higher!" : "lower!");
                     }

                     attackAction.append(new DisplayText(game, text, null, null, null));
                  }
               } else {
                  boolean alreadyHasStatus = false;

                  for (int i = 0; i < enemyPokemon.volatileStatus.size(); i++) {
                     if (enemyPokemon.volatileStatus.get(i).equals("curse")) {
                        alreadyHasStatus = true;
                        break;
                     }
                  }

                  if (alreadyHasStatus) {
                     attackAction.append(
                        new WaitFrames(
                           game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, false, true, null)))
                        )
                     );
                  } else {
                     enemyPokemon.volatileStatus.add("curse");
                     int selfDamage = friendlyPokemon.maxStats.get("hp") / 2;
                     attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name + "_ghost", enemyPokemon, null));
                     attackAction.append(depleteHealth(game, !isFriendly, selfDamage, false, null));
                     attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, null)));
                     attackAction.append(new DisplayText(game, friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " cut its own hp and", null, null, null));
                     attackAction.append(
                        new DisplayText(game, "put a CURSE on " + enemy + enemyPokemon.nickname.toUpperCase(Locale.ROOT) + "!", null, null, null)
                     );
                  }
               }
            }

            if (attack.effect.equals("EFFECT_FORCE_SWITCH")) {
               boolean failed = false;
               if (friendlyPokemon.level < enemyPokemon.level
                  && Game.rand.nextInt(100) <= (int)Math.ceil((float)(enemyPokemon.level / 4) / (enemyPokemon.level + friendlyPokemon.level + 1) * 100.0F)) {
                  failed = true;
               }

               if (!failed) {
                  String drivenAwayText = enemyPokemon.nickname.toUpperCase(Locale.ROOT) + " fled in fear!";
                  if (attack.name.contains("whirlwind")) {
                     drivenAwayText = enemyPokemon.nickname.toUpperCase(Locale.ROOT) + " was blown away!";
                  }

                  if (enemyPokemon == game.battle.oppPokemon) {
                     drivenAwayText = "Enemy " + drivenAwayText;
                  }

                  attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  attackAction.append(new DrivenAway(enemyPokemon, drivenAwayText));
                  return attackAction;
               }

               attackAction.append(
                  new WaitFrames(
                     game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, false, true, null)))
                  )
               );
            }

            if (attack.effect.equals("EFFECT_SPLASH")) {
               attackAction.append(
                  new Battle.LoadAndPlayAnimation(
                     game,
                     attack.name,
                     enemyPokemon,
                     new WaitFrames(
                        game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But nothing happened.", null, false, true, null)))
                     )
                  )
               );
            }

            if (attack.effect.equals("EFFECT_TELEPORT")) {
               boolean failed = false;
               if (friendlyPokemon.level < enemyPokemon.level
                  && Game.rand.nextInt(100) <= (int)Math.ceil((float)(enemyPokemon.level / 4) / (enemyPokemon.level + friendlyPokemon.level + 1) * 100.0F)) {
                  failed = true;
               }

               if (friendlyPokemon.cantEscapeBy != null) {
                  failed = true;
               }

               if (!failed) {
                  String teleportedAwayText = friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " fled from battle!";
                  if (friendlyPokemon == game.battle.oppPokemon) {
                     teleportedAwayText = "Enemy " + teleportedAwayText;
                  }

                  attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  attackAction.append(new DrivenAway(friendlyPokemon, teleportedAwayText));
                  return attackAction;
               }

               attackAction.append(new WaitFrames(game, 30, null));
               attackAction.append(new DisplayText.Clear(game, null));
               attackAction.append(new WaitFrames(game, 3, null));
               attackAction.append(new DisplayText(game, "But it failed!", null, false, true, null));
            }

            if (attack.effect.equals("EFFECT_MEAN_LOOK")) {
               if (enemyPokemon.cantEscapeBy != null) {
                  attackAction.append(new WaitFrames(game, 30, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, "But it failed!", null, false, true, null));
               } else {
                  String escapeText = enemyPokemon.nickname.toUpperCase(Locale.ROOT) + " canì escape now!";
                  if (enemyPokemon == game.battle.oppPokemon) {
                     escapeText = "Enemy " + escapeText;
                  }

                  attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, escapeText, null, false, true, null));
                  enemyPokemon.cantEscapeBy = friendlyPokemon;
               }
            }

            if (attack.effect.equals("EFFECT_METRONOME")) {
               String[] cantSelect = new String[]{
                  "counter", "destiny bond", "detect", "endure", "mimic", "mirror coat", "protect", "sketch", "sleep talk", "struggle", "thief"
               };
               String selectedAttack = null;

               while (selectedAttack == null) {
                  String randomAttack = Pokemon.attacksImplemented.get(Game.rand.nextInt(Pokemon.attacksImplemented.size() - 1));
                  if (!Arrays.asList(cantSelect).contains(randomAttack) && !Arrays.asList(friendlyPokemon.attacks).contains(randomAttack)) {
                     selectedAttack = randomAttack;
                  }
               }

               attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
               attackAction.append(new DisplayText.Clear(game, null));
               attackAction.append(new WaitFrames(game, 3, null));
               attackAction.append(getAttackAction(game, game.battle.attacks.get(selectedAttack), isFriendly, null));
               return attackAction;
            }

            if (attack.effect.equals("EFFECT_REFLECT") || attack.effect.equals("EFFECT_LIGHT_SCREEN")) {
               boolean failed = false;
               if (!isFriendly
                  || (!attack.effect.equals("EFFECT_REFLECT") || !game.battle.playerReflectDown)
                     && (!attack.effect.equals("EFFECT_LIGHT_SCREEN") || !game.battle.playerLightScreenDown)) {
                  if (!isFriendly
                     && (
                        attack.effect.equals("EFFECT_REFLECT") && game.battle.oppReflectDown
                           || attack.effect.equals("EFFECT_LIGHT_SCREEN") && game.battle.oppLightScreenDown
                     )) {
                     failed = true;
                  }
               } else {
                  failed = true;
               }

               if (failed) {
                  attackAction.append(new WaitFrames(game, 30, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, "But it failed!", null, false, true, null));
               } else {
                  String stat = "DEFENSE";
                  if (attack.effect.equals("EFFECT_REFLECT")) {
                     if (isFriendly) {
                        game.battle.playerReflectDown = true;
                        game.battle.playerReflectCounter = 5;
                     } else {
                        game.battle.oppReflectDown = true;
                        game.battle.oppReflectCounter = 5;
                     }
                  } else {
                     stat = "SPCL.DEF";
                     if (isFriendly) {
                        game.battle.playerLightScreenDown = true;
                        game.battle.playerLightScreenCounter = 5;
                     } else {
                        game.battle.oppLightScreenDown = true;
                        game.battle.oppLightScreenCounter = 5;
                     }
                  }

                  String reflectText = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' " + stat + " rose!";
                  attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, reflectText, null, false, true, null));
               }
            }

            if (friendlyPokemon.status != null && friendlyPokemon.status.equals("freeze") && attack.effect.equals("EFFECT_SACRED_FIRE")) {
               attackAction.append(new DisplayText.Clear(game, null));
               attackAction.append(new WaitFrames(game, 3, null));
               attackAction.append(new DisplayText(game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " thawed out!", null, true, true, null));
               attackAction.append(new SetField(friendlyPokemon, "status", null, null));
            }

            if (attack.effect.equals("EFFECT_RAIN_DANCE") || attack.effect.equals("EFFECT_SUNNY_DAY") || attack.effect.equals("EFFECT_SANDSTORM")) {
               boolean failed = false;
               if (game.battle.weather != null && game.battle.weather.equals("sandstorm") && attack.effect.equals("EFFECT_SANDSTORM")) {
                  failed = true;
               }

               if (failed) {
                  attackAction.append(new WaitFrames(game, 30, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, "But it failed!", null, false, true, null));
               } else {
                  String text = "A downpour started!";
                  if (attack.effect.equals("EFFECT_SUNNY_DAY")) {
                     text = "The sunlight got bright!";
                  } else if (attack.effect.equals("EFFECT_SANDSTORM")) {
                     text = "A SANDSTORM brewed!";
                  }

                  game.battle.weather = attack.name;
                  game.battle.weatherCounter = 5;
                  attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, text, null, false, true, null));
               }
            }

            if (attack.effect.equals("EFFECT_SAFEGUARD")) {
               boolean safeguardDown = isFriendly ? game.battle.playerSafeguardDown : game.battle.oppSafeguardDown;
               if (safeguardDown) {
                  if (attack.power != 0) {
                     return attackAction;
                  }

                  attackAction.append(new WaitFrames(game, 30, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(
                     new DisplayText(game, enemy + enemyPokemon.nickname.toUpperCase(Locale.ROOT) + " is protected by SAFEGUARD!", null, true, true, nextAction)
                  );
                  return attackAction;
               }

               if (isFriendly) {
                  game.battle.playerSafeguardDown = true;
                  game.battle.playerSafeguardCounter = 5;
               } else {
                  game.battle.oppSafeguardDown = true;
                  game.battle.oppSafeguardCounter = 5;
               }

               String reflectText = enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + "' covered by a veil!";
               attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
               attackAction.append(new DisplayText.Clear(game, null));
               attackAction.append(new WaitFrames(game, 3, null));
               attackAction.append(new DisplayText(game, reflectText, null, false, true, null));
            }

            if (attack.effect.equals("EFFECT_SPIKES")) {
               boolean failed = false;
               if (isFriendly && game.battle.oppSpikesDown || !isFriendly && game.battle.playerSpikesDown) {
                  failed = true;
               }

               if (failed) {
                  attackAction.append(new WaitFrames(game, 30, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(new DisplayText(game, "But it failed!", null, false, true, null));
               } else {
                  String enemy2 = "";
                  if (isFriendly) {
                     game.battle.oppSpikesDown = true;
                     enemy2 = "enemy ";
                  } else {
                     game.battle.playerSpikesDown = true;
                  }

                  attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(
                     new DisplayText(
                        game, "SPIKES scattered all around " + enemy2 + enemyPokemon.nickname.toUpperCase(Locale.ROOT) + " !", null, false, true, null
                     )
                  );
               }
            }

            if (attack.effect.equals("EFFECT_RAPID_SPIN")) {
               boolean removeSpikes = false;
               if (isFriendly && game.battle.playerSpikesDown) {
                  removeSpikes = true;
                  game.battle.playerSpikesDown = false;
               } else if (!isFriendly && game.battle.oppSpikesDown) {
                  removeSpikes = true;
                  game.battle.oppSpikesDown = false;
               }

               if (removeSpikes) {
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(
                     new DisplayText(game, enemy + friendlyPokemon.nickname.toUpperCase(Locale.ROOT) + " blew away SPIKES!", null, false, true, null)
                  );
               }

               boolean removeTrap = false;
               String enemy2 = "";
               if (isFriendly && game.player.currPokemon.trappedBy != null) {
                  removeTrap = true;
                  enemy2 = "enemy ";
                  game.player.currPokemon.trappedBy = null;
                  game.player.currPokemon.trapCounter = 0;
               } else if (!isFriendly && game.battle.oppPokemon.trappedBy != null) {
                  removeTrap = true;
                  game.battle.oppPokemon.trappedBy = null;
                  game.battle.oppPokemon.trapCounter = 0;
               }

               if (removeTrap) {
                  attackAction.append(new DisplayText.Clear(game, null));
                  attackAction.append(new WaitFrames(game, 3, null));
                  attackAction.append(
                     new DisplayText(
                        game,
                        enemy
                           + friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + " was released by "
                           + enemy2
                           + enemyPokemon.nickname.toUpperCase(Locale.ROOT)
                           + "!",
                        null,
                        false,
                        true,
                        null
                     )
                  );
               }
            }

            attackAction.append(detectFaint(game, isFriendly, false, null));
            attackAction.append(detectFaint(game, !isFriendly, false, null));
            attackAction.append(new CheckWhitedOut(null));
            attackAction.append(new CheckEndOfBattle(null));
            attackAction.append(new CheckShouldSwitch(null));
            String stat = null;
            if (attack.effect.contains("EFFECT_ATTACK")) {
               stat = "attack";
            } else if (attack.effect.contains("EFFECT_DEFENSE")) {
               stat = "defense";
            } else if (attack.effect.contains("EFFECT_SPEED")) {
               stat = "speed";
            } else if (attack.effect.contains("EFFECT_SP_ATK")) {
               stat = "specialAtk";
            } else if (attack.effect.contains("EFFECT_SP_DEF")) {
               stat = "specialDef";
            } else if (attack.effect.contains("EFFECT_ACCURACY")) {
               stat = "accuracy";
            } else if (attack.effect.contains("EFFECT_EVASION")) {
               stat = "evasion";
            } else if (attack.effect.contains("EFFECT_ALL")) {
               stat = "all";
            }

            if (stat != null) {
               Pokemon target = friendlyPokemon;
               if (!attack.effect.contains("UP_HIT")
                  && (
                     attack.effect.contains("HIT")
                        || attack.name.equals("growl")
                        || attack.name.equals("leer")
                        || attack.name.equals("screech")
                        || attack.name.equals("spider web")
                        || attack.name.equals("string shot")
                        || attack.name.equals("tail whip")
                        || attack.name.equals("sand attack")
                        || attack.name.equals("smokescreen")
                        || attack.name.equals("kinesis")
                        || attack.name.equals("flash")
                        || attack.name.equals("mud slap")
                        || attack.name.equals("octazooka")
                        || attack.name.equals("cotton spore")
                        || attack.name.equals("play rough")
                        || attack.name.equals("sweet scent")
                        || attack.name.equals("scary face")
                        || attack.name.equals("charm")
                  )) {
                  target = enemyPokemon;
               }

               if (attack.effect.contains("HIT")) {
                  var36 = attack.effectChance * 255 / 100;
                  attackMisses = var36 < 255 && game.map.rand.nextInt(256) >= var36;
               }

               if (attackMisses && (target != friendlyPokemon || attack.name.equals("ancientpower"))) {
                  if (!attack.effect.contains("HIT")) {
                     attackAction.append(
                        new WaitFrames(
                           game,
                           30,
                           new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, true, true, nextAction)))
                        )
                     );
                     return attackAction;
                  }
               } else {
                  int stage = 1;
                  if (attack.effect.contains("2")) {
                     stage = 2;
                  }

                  if (attack.effect.contains("DOWN")) {
                     stage *= -1;
                  }

                  boolean worked = target.gen2ApplyStatStage(stat, stage);
                  String text = null;
                  if (!worked) {
                     if (!attack.effect.contains("HIT")) {
                        text = target.nickname.toUpperCase(Locale.ROOT)
                           + "' "
                           + stat.toUpperCase(Locale.ROOT)
                           + " wonì go any "
                           + (stage > 0 ? "higher!" : "lower!");
                     }
                  } else {
                     if (stat.equals("specialAtk")) {
                        stat = "special attack";
                     } else if (stat.equals("specialDef")) {
                        stat = "special defense";
                     }

                     if (stat.equals("all")) {
                        text = target.nickname.toUpperCase(Locale.ROOT) + "' stats went up!";
                     } else if (stage == 2) {
                        text = target.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " went way up!";
                     } else if (stage == 1) {
                        text = target.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " went up!";
                     } else if (stage == -1) {
                        text = target.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " fell!";
                     } else if (stage == -2) {
                        text = target.nickname.toUpperCase(Locale.ROOT) + "' " + stat.toUpperCase(Locale.ROOT) + " sharply fell!";
                     }
                  }

                  if (worked && !attack.effect.contains("HIT")) {
                     attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  }

                  if (text != null) {
                     if (worked && !attack.effect.contains("HIT") && target != friendlyPokemon) {
                        attackAction.append(new Battle.LoadAndPlayAnimation(game, "stat_stage_hit", enemyPokemon, null));
                     }

                     attackAction.append(
                        new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, text, null, true, false, new WaitFrames(game, 30, null))))
                     );
                  }
               }
            }

            String status = null;
            enemy = isFriendly ? "Enemy " : "";
            if (attack.effect.contains("EFFECT_PARALYZE")) {
               status = "paralyze";
            } else if (attack.effect.equals("EFFECT_SLEEP")) {
               status = "sleep";
            } else if (attack.effect.contains("EFFECT_POISON")) {
               status = "poison";
            } else if (attack.effect.contains("EFFECT_CONFUSE")) {
               status = "confuse";
            } else if (attack.effect.contains("EFFECT_BURN")) {
               status = "burn";
            } else if (attack.effect.contains("EFFECT_FREEZE")) {
               status = "freeze";
            } else if (attack.effect.contains("EFFECT_TOXIC")) {
               status = "toxic";
            } else if (attack.effect.contains("EFFECT_ATTRACT")) {
               status = "attract";
            }

            if (status != null) {
               if (attack.effect.contains("HIT")) {
                  var36 = attack.effectChance * 255 / 100;
                  attackMisses = var36 < 255 && game.map.rand.nextInt(256) >= var36;
               }

               if (attack.type.equals("electric") && enemyPokemon.types.contains("ELECTRIC") && status.equals("paralyze")) {
                  attackMisses = true;
               }

               if (attack.type.equals("poison") && enemyPokemon.types.contains("STEEL") && (status.equals("poison") || status.equals("toxic"))) {
                  attackMisses = true;
               }

               if (attack.type.equals("electric") && enemyPokemon.types.contains("ELECTRIC")) {
                  attackMisses = true;
               }

               if (attack.type.equals("poison") && enemyPokemon.types.contains("POISON") && (status.equals("poison") || status.equals("toxic"))) {
                  attackMisses = true;
               }

               if (attack.type.equals("ice") && enemyPokemon.types.contains("ICE") && status.equals("freeze")) {
                  attackMisses = true;
               }

               if (attack.type.equals("fire") && enemyPokemon.types.contains("FIRE") && status.equals("burn")) {
                  attackMisses = true;
               }

               if (attack.name.equals("attract")
                  && (friendlyPokemon.gender.equals("unknown") || enemyPokemon.gender.equals("unknown") || friendlyPokemon.gender == enemyPokemon.gender)) {
                  attackMisses = true;
               }

               boolean volatileStatus = false;
               boolean newVolatileStatus = false;
               if (status.equals("confuse") || status.equals("attract")) {
                  volatileStatus = true;
                  newVolatileStatus = true;

                  for (int i = 0; i < enemyPokemon.volatileStatus.size(); i++) {
                     if (enemyPokemon.volatileStatus.get(i).equals(status)) {
                        newVolatileStatus = false;
                        attackMisses = true;
                        break;
                     }
                  }
               }

               Pokemon target = enemyPokemon;
               if ((attackMisses || !volatileStatus && target.status != null) && !attack.effect.contains("HIT")) {
                  attackAction.append(
                     new WaitFrames(
                        game, 30, new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, true, true, nextAction)))
                     )
                  );
                  return attackAction;
               }

               if (!attackMisses && !volatileStatus) {
                  boolean safeguardDown = isFriendly ? game.battle.oppSafeguardDown : game.battle.playerSafeguardDown;
                  if (safeguardDown) {
                     attackAction.append(new WaitFrames(game, 30, null));
                     attackAction.append(new DisplayText.Clear(game, null));
                     attackAction.append(new WaitFrames(game, 3, null));
                     attackAction.append(
                        new DisplayText(game, enemy + target.nickname.toUpperCase(Locale.ROOT) + " is protected by SAFEGUARD!", null, true, true, nextAction)
                     );
                     return attackAction;
                  }
               }

               if (!attackMisses && (target.status == null || newVolatileStatus)) {
                  if (!attack.effect.contains("HIT")) {
                     attackAction.append(new Battle.LoadAndPlayAnimation(game, attack.name, enemyPokemon, null));
                  } else {
                     attackAction.append(new WaitFrames(game, 49, null));
                  }

                  if (!status.equals("sleep") && !status.equals("poison") && !status.equals("attract")) {
                     attackAction.append(new Battle.LoadAndPlayAnimation(game, "status_" + status, target, null));
                  }

                  attackAction.append(new WaitFrames(game, 30, null));
                  String text = null;
                  if (status.equals("paralyze")) {
                     target.currentStats.put("speed", target.currentStats.get("speed") / 4);
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + " was PARALYZED! It might not be able to attack!";
                  } else if (status.equals("burn")) {
                     target.currentStats.put("attack", target.currentStats.get("attack") / 4);
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + " was burned!";
                  } else if (status.equals("sleep")) {
                     target.statusCounter = game.map.rand.nextInt(5) + 1;
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + " fell asleep!";
                  } else if (status.equals("poison")) {
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + " was poisoned!";
                  } else if (status.equals("confuse")) {
                     target.volatileStatusCounter.put(status, game.map.rand.nextInt(5) + 1);
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + " is confused!";
                  } else if (status.equals("freeze")) {
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + "' frozen solid!";
                  } else if (status.equals("toxic")) {
                     target.statusCounter = 1;
                     text = enemy + target.nickname.toUpperCase(Locale.ROOT) + "' badly poisoned!";
                  } else if (status.equals("attract")) {
                     text = enemy
                        + enemyPokemon.nickname.toUpperCase(Locale.ROOT)
                        + " became infatuated with "
                        + friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                        + "!";
                  }

                  if (text != null) {
                     attackAction.append(new DisplayText.Clear(game, null));
                     attackAction.append(new WaitFrames(game, 3, null));
                     attackAction.append(new DisplayText(game, text, null, true, true, null));
                     if (!status.equals("confuse") && !status.equals("attract")) {
                        attackAction.append(new SetField(target, "status", status, null));
                     } else {
                        target.volatileStatus.add(status);
                     }
                  }
               }
            }

            if (attack.name.equals("disable")) {
               attackIndex = DrawBattle.prevFriendlyAttackIndex;
               if (isFriendly) {
                  attackIndex = DrawBattle.prevEnemyAttackIndex;
               }

               if (attackIndex != -1 && enemyPokemon.disabledIndex < 0) {
                  enemyPokemon.disabledIndex = attackIndex;
                  enemyPokemon.disabledCounter = game.map.rand.nextInt(7) + 2;
                  attackAction.append(
                     new Battle.LoadAndPlayAnimation(
                        game,
                        attack.name,
                        enemyPokemon,
                        new DisplayText.Clear(
                           game,
                           new WaitFrames(
                              game,
                              3,
                              new DisplayText(
                                 game,
                                 enemy
                                    + enemyPokemon.nickname.toUpperCase(Locale.ROOT)
                                    + "' "
                                    + enemyPokemon.attacks[attackIndex].toUpperCase(Locale.ROOT)
                                    + " was disabled!",
                                 null,
                                 false,
                                 true,
                                 null
                              )
                           )
                        )
                     )
                  );
               } else {
                  attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, false, true, null))));
               }
            } else if (attack.effect.equals("EFFECT_SKETCH")) {
               attackIndex = DrawBattle.prevFriendlyAttackIndex;
               if (isFriendly) {
                  attackIndex = DrawBattle.prevEnemyAttackIndex;
               }

               if (attackIndex == -1) {
                  attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, new DisplayText(game, "But it failed!", null, false, true, null))));
               } else {
                  int index = 0;

                  for (String name : friendlyPokemon.attacks) {
                     if (name.equals(attack.name)) {
                        break;
                     }

                     index++;
                  }

                  friendlyPokemon.attacks[index] = enemyPokemon.attacks[attackIndex];
                  attackAction.append(
                     new DisplayText.Clear(
                        game,
                        new WaitFrames(
                           game,
                           3,
                           new DisplayText(
                              game,
                              friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                                 + " sketched "
                                 + enemyPokemon.attacks[attackIndex].toUpperCase(Locale.ROOT)
                                 + "!",
                              null,
                              false,
                              true,
                              null
                           )
                        )
                     )
                  );
               }
            } else if (attack.effect.equals("EFFECT_TRANSFORM")) {
               Pokemon enemyPokemonFinal = enemyPokemon;
               Pokemon friendlyPokemonFinal = friendlyPokemon;
               boolean isFriendlyFinal = isFriendly;
               String text = enemy
                  + friendlyPokemon.nickname.toUpperCase(Locale.ROOT)
                  + " transformed into "
                  + enemyPokemon.nickname.toUpperCase(Locale.ROOT)
                  + "!";
               Action runCode = new RunCode(() -> {
                  Pokemon pokemon = new Pokemon(enemyPokemonFinal.specie.name, friendlyPokemonFinal.level);
                  String[] statNames = new String[]{"hp", "attack", "defense", "specialAtk", "specialDef", "speed"};

                  for (String statName : statNames) {
                     pokemon.currentStats.put(statName, friendlyPokemonFinal.currentStats.get(statName));
                  }

                  for (int ix = 0; ix < 4; ix++) {
                     pokemon.attacks[ix] = enemyPokemonFinal.attacks[ix];
                  }

                  pokemon.types = new ArrayList<>(friendlyPokemonFinal.types);
                  pokemon.nickname = friendlyPokemonFinal.nickname;
                  pokemon.gender = friendlyPokemonFinal.gender;
                  pokemon.happiness = friendlyPokemonFinal.happiness;
                  pokemon.status = friendlyPokemonFinal.status;
                  pokemon.statusCounter = friendlyPokemonFinal.statusCounter;
                  pokemon.trappedBy = friendlyPokemonFinal.trappedBy;
                  pokemon.trapCounter = friendlyPokemonFinal.trapCounter;
                  pokemon.sprite.setX(friendlyPokemonFinal.sprite.getX());
                  pokemon.sprite.setY(friendlyPokemonFinal.sprite.getY());
                  pokemon.exp = friendlyPokemonFinal.exp;
                  pokemon.position = friendlyPokemonFinal.position.cpy();
                  SpriteProxy enemySprite = enemyPokemonFinal.sprite;
                  SpriteProxy friendlySprite = friendlyPokemonFinal.sprite;
                  SpriteProxy targetSprite = pokemon.sprite;
                  if (isFriendlyFinal) {
                     enemySprite = enemyPokemonFinal.backSprite;
                     friendlySprite = friendlyPokemonFinal.backSprite;
                     targetSprite = pokemon.backSprite;
                  }

                  Color color = new Color();
                  Texture texture = enemySprite.getTexture();
                  TextureData temp = texture.getTextureData();
                  if (!temp.isPrepared()) {
                     temp.prepare();
                  }

                  Pixmap pixmap = temp.consumePixmap();

                  for (int m = 0; m < pixmap.getWidth(); m++) {
                     for (int n = 0; n < pixmap.getHeight(); n++) {
                        color.set(pixmap.getPixel(m, n));
                        if (color.equals(enemySprite.color1)) {
                           color = friendlySprite.color1;
                        } else if (color.equals(enemySprite.color2)) {
                           color = friendlySprite.color2;
                        }

                        pixmap.drawPixel(m, n, Color.argb8888(color));
                     }
                  }

                  texture = TextureCache.get(pixmap);
                  targetSprite.setTexture(texture);
                  targetSprite.setRegion(enemySprite);
                  if (isFriendlyFinal) {
                     game.player.currPokemon = pokemon;
                  } else {
                     game.battle.oppPokemon = pokemon;
                  }
               }, new DisplayText(game, text, null, false, true, null));
               attackAction.append(new DisplayText.Clear(game, new WaitFrames(game, 3, runCode)));
            }

            if (enemyPokemon.trappedBy == null && !attackMisses && (attack.effect.equals("EFFECT_TRAP_TARGET") || attack.effect.equals("EFFECT_BIND"))) {
               enemy = isFriendly ? "Enemy " : "";
               attackAction.append(
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game, 3, new DisplayText(game, enemy + enemyPokemon.nickname.toUpperCase(Locale.ROOT) + " was trapped!", null, true, true, null)
                     )
                  )
               );
               enemyPokemon.trappedBy = attack.name.toLowerCase(Locale.ROOT);
               enemyPokemon.trapCounter = game.map.rand.nextInt(4) + 2;
               if (attack.name.toLowerCase(Locale.ROOT).equals("thunder cage")) {
                  enemyPokemon.trapCounter = game.map.rand.nextInt(2) + 4;
               }
            }

            attackAction.append(nextAction);
            return attackAction;
         }

         var35 = 50;
         var36 = 100;
      }

      return isFriendly
         ? new Attack.Default(game, var35, var36, nextAction)
         : new Attack.DefaultEnemy(game.battle.oppPokemon, game.player.currPokemon, var35, var36, nextAction);
   }

   public static Action getIntroAction(Game game) {
      return getIntroAction(game, false);
   }

   public static Action getIntroAction(Game game, boolean fishing) {
      if (game.coop != null && game.coop.isWorldCreature(game.battle.oppPokemon) && !game.coop.ownsBattle(game.battle.oppPokemon)) {
         return game.coop.reserveBattle(fishing);
      }
      game.battle.resetState();
      DrawBattle.hideOwnPokemon = false;
      DrawBattle.hideOppPokemon = false;
      if (game.player.pokemon.isEmpty()) {
         return new SplitAction(
            new BattleIntro(
               new BattleIntroAnim1(
                  new SplitAction(
                     new DrawBattle(game),
                     new BattleAnimPositionPlayers(
                        game,
                        new PlayMusic(
                           game.battle.oppPokemon,
                           new DisplayText(
                              game,
                              "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " appeared!",
                              null,
                              null,
                              new WaitFrames(
                                 game,
                                 39,
                                 game.player.adrenaline > 0
                                    ? new DisplayText(
                                       game,
                                       "" + game.player.name + " has ADRENALINE " + Integer.toString(game.player.adrenaline) + "!",
                                       null,
                                       null,
                                       new PrintAngryEating(game, new DrawBattleMenuSafariZone(game, null))
                                    )
                                    : new PrintAngryEating(game, new DrawBattleMenuSafariZone(game, null))
                              )
                           )
                        )
                     )
                  )
               )
            ),
            null
         );
      }

      if (game.battle.oppPokemon.generation == Pokemon.Generation.CRYSTAL) {
         Action triggerAction;
         if (game.player.currPokemon.generation == Pokemon.Generation.RED) {
            triggerAction = new PlayMusic(game.player.currPokemon.specie.name, new WaitFrames(game, 6, new DrawBattleMenuNormal(game, null)));
         } else {
            Action afterTrigger = new WaitFrames(game, 15, new DrawBattleMenuNormal(game, null));
            triggerAction = new PlaySound(
               game.player.currPokemon,
               new WaitFrames(game, game.player.currPokemon.specie.cryLengthInFrames(), new WaitFrames(game, 6, new DrawFriendlyHealthGen2(game, afterTrigger)))
            );
         }

         Action introAction = new BattleIntro(new BattleIntroAnim1(new SplitAction(new DrawBattle(game), new BattleAnimPositionPlayers(game, null))));
         if (game.battle.oppPokemon.isShiny) {
            introAction.append(new Battle.LoadAndPlayAnimation(game, "shiny", game.player.currPokemon, null));
         }

         introAction.append(
            new SplitAction(new WaitFrames(game, 4, new PlaySound(game.battle.oppPokemon, null)), new PokemonIntroAnim(new WaitFrames(game, 11, null)))
         );
         if (game.battle.oppPokemon.aggroPlayer) {
            introAction.append(
               new DisplayText(
                  game,
                  "Angry " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " attacked!",
                  null,
                  null,
                  new DisplayText(game, "Enemy " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + "' attack went way up!", null, null, null)
               )
            );
            game.battle.oppPokemon.gen2ApplyStatStage("attack", 2);
         } else if (fishing) {
            introAction.append(new DisplayText(game, "Unhooked " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " attacked!", null, null, null));
         } else {
            introAction.append(new DisplayText(game, "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " appeared!", null, null, null));
         }

         if (game.battle.oppPokemon.isTrapping) {
            introAction.append(new DisplayText(game, "It' holding your leg! You canì flee!", null, null, null));
         }

         introAction.append(
            new SplitAction(
               new WaitFrames(game, 1, new DrawEnemyHealthGen2(game)),
               new WaitFrames(
                  game,
                  39,
                  new MovePlayerOffScreen(
                     game,
                     new DisplayText(
                        game,
                        "Go! " + game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + "!",
                        null,
                        triggerAction,
                        game.player.currPokemon.generation == Pokemon.Generation.RED
                           ? new SplitAction(new DrawFriendlyHealthGen2(game), new ThrowOutPokemon(game, triggerAction))
                           : new ThrowOutPokemonCrystal(
                              game,
                              game.player.currPokemon.isShiny
                                 ? new Battle.LoadAndPlayAnimation(game, "shiny", game.battle.oppPokemon, triggerAction)
                                 : triggerAction
                           )
                     )
                  )
               )
            )
         );
         return introAction;
      } else {
         Action triggerAction = new PlayMusic(game.player.currPokemon.specie.name, new WaitFrames(game, 6, new DrawBattleMenuNormal(game, null)));
         return new BattleIntro(
            new BattleIntroAnim1(
               new SplitAction(
                  new DrawBattle(game),
                  new BattleAnimPositionPlayers(
                     game,
                     new PlayMusic(
                        game.battle.oppPokemon.specie.name,
                        new DisplayText(
                           game,
                           "Wild " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " appeared!",
                           null,
                           null,
                           new SplitAction(
                              new WaitFrames(game, 1, new DrawEnemyHealthGen2(game)),
                              new WaitFrames(
                                 game,
                                 39,
                                 new MovePlayerOffScreen(
                                    game,
                                    new DisplayText(
                                       game,
                                       "Go! " + game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + "!",
                                       null,
                                       triggerAction,
                                       new SplitAction(new DrawFriendlyHealthGen2(game), new ThrowOutPokemon(game, triggerAction))
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

   public void resetState() {
      this.turnNumber = 1;
      this.playerReflectDown = false;
      this.oppReflectDown = false;
      this.playerReflectCounter = 0;
      this.oppReflectCounter = 0;
      this.playerLightScreenDown = false;
      this.oppLightScreenDown = false;
      this.playerLightScreenCounter = 0;
      this.oppLightScreenCounter = 0;
      this.weather = null;
      this.weatherCounter = 0;
      this.playerAirborne = false;
      this.playerUnderground = false;
      this.oppAirborne = false;
      this.oppUnderground = false;
      this.playerSafeguardDown = false;
      this.playerSafeguardCounter = 0;
      this.oppSafeguardDown = false;
      this.oppSafeguardCounter = 0;
      this.playerLockedMove = -1;
      this.playerLockedMoveCounter = 0;
      this.oppLockedMove = -1;
      this.oppLockedMoveCounter = 0;
      this.playerSpikesDown = false;
      this.oppSpikesDown = false;
   }

   public Battle() {
      FileHandle musicMod = Gdx.files.local("mods/wild_battle.ogg");
      if (musicMod.exists()) {
         System.out.println("Found battle music mod.");
         this.music = AudioLoader.loadMusic(musicMod.path());
      } else {
         this.music = new LinkedMusic("music/wild_battle_intro", "music/wild_battle");
      }

      this.victoryFanfare = new LinkedMusic("music/victory_fanfare1_intro", "music/victory_fanfare1");
      this.victoryFanfare.setVolume(1.0F);
      this.gen2TypeEffectiveness = new HashMap<>();
      this.gen2TypeEffectiveness.put("normal", new HashMap<>());
      this.gen2TypeEffectiveness.get("normal").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("rock", 0.5F);
      this.gen2TypeEffectiveness.get("normal").put("ghost", 0.0F);
      this.gen2TypeEffectiveness.get("normal").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("normal").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("normal").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("fire", new HashMap<>());
      this.gen2TypeEffectiveness.get("fire").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("fire", 0.5F);
      this.gen2TypeEffectiveness.get("fire").put("water", 0.5F);
      this.gen2TypeEffectiveness.get("fire").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("grass", 2.0F);
      this.gen2TypeEffectiveness.get("fire").put("ice", 2.0F);
      this.gen2TypeEffectiveness.get("fire").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("bug", 2.0F);
      this.gen2TypeEffectiveness.get("fire").put("rock", 0.5F);
      this.gen2TypeEffectiveness.get("fire").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("dragon", 0.5F);
      this.gen2TypeEffectiveness.get("fire").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("fire").put("steel", 2.0F);
      this.gen2TypeEffectiveness.get("fire").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("water", new HashMap<>());
      this.gen2TypeEffectiveness.get("water").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("fire", 2.0F);
      this.gen2TypeEffectiveness.get("water").put("water", 0.5F);
      this.gen2TypeEffectiveness.get("water").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("grass", 0.5F);
      this.gen2TypeEffectiveness.get("water").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("ground", 2.0F);
      this.gen2TypeEffectiveness.get("water").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("rock", 2.0F);
      this.gen2TypeEffectiveness.get("water").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("dragon", 0.5F);
      this.gen2TypeEffectiveness.get("water").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("steel", 1.0F);
      this.gen2TypeEffectiveness.get("water").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("electric", new HashMap<>());
      this.gen2TypeEffectiveness.get("electric").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("water", 2.0F);
      this.gen2TypeEffectiveness.get("electric").put("electric", 0.5F);
      this.gen2TypeEffectiveness.get("electric").put("grass", 0.5F);
      this.gen2TypeEffectiveness.get("electric").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("ground", 0.0F);
      this.gen2TypeEffectiveness.get("electric").put("flying", 2.0F);
      this.gen2TypeEffectiveness.get("electric").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("dragon", 0.5F);
      this.gen2TypeEffectiveness.get("electric").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("steel", 1.0F);
      this.gen2TypeEffectiveness.get("electric").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("grass", new HashMap<>());
      this.gen2TypeEffectiveness.get("grass").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("fire", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("water", 2.0F);
      this.gen2TypeEffectiveness.get("grass").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("grass", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("poison", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("ground", 2.0F);
      this.gen2TypeEffectiveness.get("grass").put("flying", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("bug", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("rock", 2.0F);
      this.gen2TypeEffectiveness.get("grass").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("dragon", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("grass").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("grass").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("ice", new HashMap<>());
      this.gen2TypeEffectiveness.get("ice").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("fire", 0.5F);
      this.gen2TypeEffectiveness.get("ice").put("water", 0.5F);
      this.gen2TypeEffectiveness.get("ice").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("grass", 2.0F);
      this.gen2TypeEffectiveness.get("ice").put("ice", 0.5F);
      this.gen2TypeEffectiveness.get("ice").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("ground", 2.0F);
      this.gen2TypeEffectiveness.get("ice").put("flying", 2.0F);
      this.gen2TypeEffectiveness.get("ice").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("dragon", 2.0F);
      this.gen2TypeEffectiveness.get("ice").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("ice").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("ice").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("fighting", new HashMap<>());
      this.gen2TypeEffectiveness.get("fighting").put("normal", 2.0F);
      this.gen2TypeEffectiveness.get("fighting").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("ice", 2.0F);
      this.gen2TypeEffectiveness.get("fighting").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("poison", 0.5F);
      this.gen2TypeEffectiveness.get("fighting").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("flying", 0.5F);
      this.gen2TypeEffectiveness.get("fighting").put("psychic", 0.5F);
      this.gen2TypeEffectiveness.get("fighting").put("bug", 0.5F);
      this.gen2TypeEffectiveness.get("fighting").put("rock", 2.0F);
      this.gen2TypeEffectiveness.get("fighting").put("ghost", 0.0F);
      this.gen2TypeEffectiveness.get("fighting").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("fighting").put("dark", 2.0F);
      this.gen2TypeEffectiveness.get("fighting").put("steel", 2.0F);
      this.gen2TypeEffectiveness.get("fighting").put("fairy", 0.5F);
      this.gen2TypeEffectiveness.put("poison", new HashMap<>());
      this.gen2TypeEffectiveness.get("poison").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("grass", 2.0F);
      this.gen2TypeEffectiveness.get("poison").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("poison", 0.5F);
      this.gen2TypeEffectiveness.get("poison").put("ground", 0.5F);
      this.gen2TypeEffectiveness.get("poison").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("rock", 0.5F);
      this.gen2TypeEffectiveness.get("poison").put("ghost", 0.5F);
      this.gen2TypeEffectiveness.get("poison").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("poison").put("steel", 0.0F);
      this.gen2TypeEffectiveness.get("poison").put("fairy", 2.0F);
      this.gen2TypeEffectiveness.put("ground", new HashMap<>());
      this.gen2TypeEffectiveness.get("ground").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("fire", 2.0F);
      this.gen2TypeEffectiveness.get("ground").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("electric", 2.0F);
      this.gen2TypeEffectiveness.get("ground").put("grass", 0.5F);
      this.gen2TypeEffectiveness.get("ground").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("poison", 2.0F);
      this.gen2TypeEffectiveness.get("ground").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("flying", 0.0F);
      this.gen2TypeEffectiveness.get("ground").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("bug", 0.5F);
      this.gen2TypeEffectiveness.get("ground").put("rock", 2.0F);
      this.gen2TypeEffectiveness.get("ground").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("ground").put("steel", 2.0F);
      this.gen2TypeEffectiveness.get("ground").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("flying", new HashMap<>());
      this.gen2TypeEffectiveness.get("flying").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("electric", 0.5F);
      this.gen2TypeEffectiveness.get("flying").put("grass", 2.0F);
      this.gen2TypeEffectiveness.get("flying").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("fighting", 2.0F);
      this.gen2TypeEffectiveness.get("flying").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("bug", 2.0F);
      this.gen2TypeEffectiveness.get("flying").put("rock", 0.5F);
      this.gen2TypeEffectiveness.get("flying").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("flying").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("flying").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("psychic", new HashMap<>());
      this.gen2TypeEffectiveness.get("psychic").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("fighting", 2.0F);
      this.gen2TypeEffectiveness.get("psychic").put("poison", 2.0F);
      this.gen2TypeEffectiveness.get("psychic").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("psychic", 0.5F);
      this.gen2TypeEffectiveness.get("psychic").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("psychic").put("dark", 0.0F);
      this.gen2TypeEffectiveness.get("psychic").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("psychic").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("bug", new HashMap<>());
      this.gen2TypeEffectiveness.get("bug").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("fire", 0.5F);
      this.gen2TypeEffectiveness.get("bug").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("grass", 2.0F);
      this.gen2TypeEffectiveness.get("bug").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("fighting", 0.5F);
      this.gen2TypeEffectiveness.get("bug").put("poison", 0.5F);
      this.gen2TypeEffectiveness.get("bug").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("flying", 0.5F);
      this.gen2TypeEffectiveness.get("bug").put("psychic", 2.0F);
      this.gen2TypeEffectiveness.get("bug").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("ghost", 0.5F);
      this.gen2TypeEffectiveness.get("bug").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("bug").put("dark", 2.0F);
      this.gen2TypeEffectiveness.get("bug").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("bug").put("fairy", 0.5F);
      this.gen2TypeEffectiveness.put("rock", new HashMap<>());
      this.gen2TypeEffectiveness.get("rock").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("fire", 2.0F);
      this.gen2TypeEffectiveness.get("rock").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("ice", 2.0F);
      this.gen2TypeEffectiveness.get("rock").put("fighting", 0.5F);
      this.gen2TypeEffectiveness.get("rock").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("ground", 0.5F);
      this.gen2TypeEffectiveness.get("rock").put("flying", 2.0F);
      this.gen2TypeEffectiveness.get("rock").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("bug", 2.0F);
      this.gen2TypeEffectiveness.get("rock").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("rock").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("rock").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("ghost", new HashMap<>());
      this.gen2TypeEffectiveness.get("ghost").put("normal", 0.0F);
      this.gen2TypeEffectiveness.get("ghost").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("psychic", 2.0F);
      this.gen2TypeEffectiveness.get("ghost").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("ghost", 2.0F);
      this.gen2TypeEffectiveness.get("ghost").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("dark", 0.5F);
      this.gen2TypeEffectiveness.get("ghost").put("steel", 1.0F);
      this.gen2TypeEffectiveness.get("ghost").put("fairy", 1.0F);
      this.gen2TypeEffectiveness.put("dragon", new HashMap<>());
      this.gen2TypeEffectiveness.get("dragon").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("dragon", 2.0F);
      this.gen2TypeEffectiveness.get("dragon").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("dragon").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("dragon").put("fairy", 0.0F);
      this.gen2TypeEffectiveness.put("dark", new HashMap<>());
      this.gen2TypeEffectiveness.get("dark").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("fire", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("fighting", 0.5F);
      this.gen2TypeEffectiveness.get("dark").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("psychic", 2.0F);
      this.gen2TypeEffectiveness.get("dark").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("ghost", 2.0F);
      this.gen2TypeEffectiveness.get("dark").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("dark", 0.5F);
      this.gen2TypeEffectiveness.get("dark").put("steel", 1.0F);
      this.gen2TypeEffectiveness.get("dark").put("fairy", 0.5F);
      this.gen2TypeEffectiveness.put("steel", new HashMap<>());
      this.gen2TypeEffectiveness.get("steel").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("fire", 0.5F);
      this.gen2TypeEffectiveness.get("steel").put("water", 0.5F);
      this.gen2TypeEffectiveness.get("steel").put("electric", 0.5F);
      this.gen2TypeEffectiveness.get("steel").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("ice", 2.0F);
      this.gen2TypeEffectiveness.get("steel").put("fighting", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("poison", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("rock", 2.0F);
      this.gen2TypeEffectiveness.get("steel").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("dragon", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("dark", 1.0F);
      this.gen2TypeEffectiveness.get("steel").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("steel").put("fairy", 2.0F);
      this.gen2TypeEffectiveness.put("fairy", new HashMap<>());
      this.gen2TypeEffectiveness.get("fairy").put("normal", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("fire", 0.5F);
      this.gen2TypeEffectiveness.get("fairy").put("water", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("electric", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("grass", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("ice", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("fighting", 2.0F);
      this.gen2TypeEffectiveness.get("fairy").put("poison", 0.5F);
      this.gen2TypeEffectiveness.get("fairy").put("ground", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("flying", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("psychic", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("bug", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("rock", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("ghost", 1.0F);
      this.gen2TypeEffectiveness.get("fairy").put("dragon", 2.0F);
      this.gen2TypeEffectiveness.get("fairy").put("dark", 2.0F);
      this.gen2TypeEffectiveness.get("fairy").put("steel", 0.5F);
      this.gen2TypeEffectiveness.get("fairy").put("fairy", 1.0F);

      try {
         FileHandle file = Gdx.files.internal("pokemon/moves.asm");
         Reader reader = file.reader();
         BufferedReader br = new BufferedReader(reader);

         String line;
         while ((line = br.readLine()) != null) {
            if (line.contains("\tmove ") && !line.contains(";")) {
               String[] attrs = line.split("\tmove ")[1].split(",\\s+");
               String attackType = attrs[3].toLowerCase(Locale.ROOT);
               Attack attack = new Attack(
                  attrs[0].toLowerCase(Locale.ROOT).replace('_', ' '),
                  attrs[1],
                  Integer.valueOf(attrs[2]),
                  attackType,
                  Integer.valueOf(attrs[4]),
                  Integer.valueOf(attrs[5]),
                  Integer.valueOf(attrs[6])
               );
               this.attacks.put(attack.name, attack);
            }
         }

         reader.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      Attack attack = new Attack("Mewtwo_Special1", "EFFECT_NORMAL_HIT", 0, "psychic", 100, 1, 100);
      attack.category = Attack.Category.SPECIAL;
      this.attacks.put(attack.name, attack);
      attack = new Attack("confusion_hit", "EFFECT_NORMAL_HIT", 40, "normal", 100, 1, 100);
      attack.category = Attack.Category.PHYSICAL;
      this.attacks.put(attack.name, attack);
      attack = new Attack("Regigigas_Special1", "EFFECT_NORMAL_HIT", 40, "ground", 100, 1, 100);
      attack.category = Attack.Category.PHYSICAL;
      this.attacks.put(attack.name, attack);
   }

   int calcFaintExp(int numParticipated) {
      int a = 1;
      int t = 1;
      int b = this.oppPokemon.baseStats.get("baseExp");
      int e = 1;
      int l = this.oppPokemon.level;
      int s = numParticipated;
      int exp = a * t * b * e * l / (7 * s);
      return exp * 5;
   }

   boolean calcIfRunSuccessful(Game game, Player player) {
      if (player.currPokemon.cantEscapeBy != null) {
         return false;
      }

      int currSpeed = player.currPokemon.currentStats.get("speed");
      int oppSpeed = this.oppPokemon.currentStats.get("speed");
      if (currSpeed >= oppSpeed) {
         return true;
      }

      int b = oppSpeed / 4 % 256;
      if (b == 0) {
         return true;
      }

      int x = currSpeed * 32 / b + 30 * (player.numFlees + 1);
      return x > 255 ? true : game.map.rand.nextInt(256) < x;
   }

   public static Action depleteHealth(Game game, boolean isFriendly, int damage, Action nextAction) {
      return depleteHealth(game, isFriendly, damage, true, nextAction);
   }

   public static Action depleteHealth(Game game, boolean isFriendly, int damage, boolean detectFaint, Action nextAction) {
      Action newAction;
      if (!isFriendly) {
         newAction = new DepleteFriendlyHealth(game.player.currPokemon, damage, null);
      } else {
         newAction = new DepleteEnemyHealth(game, damage, null);
      }

      if (detectFaint) {
         newAction.append(detectFaint(game, isFriendly, null));
      }

      newAction.append(nextAction);
      return newAction;
   }

   public static Action detectFaint(Game game, boolean isFriendly, Action nextAction) {
      return detectFaint(game, isFriendly, true, nextAction);
   }

   public static Action detectFaint(Game game, boolean isFriendly, boolean checkEndOfBattle, Action nextAction) {
      Action newAction;
      if (!isFriendly) {
         newAction = new DetectFriendlyFaint(game.player.currPokemon, checkEndOfBattle, null);
      } else {
         newAction = new DetectEnemyFaint(checkEndOfBattle, null);
      }

      newAction.append(nextAction);
      return newAction;
   }

   static {
      try {
         FileHandle file = Gdx.files.internal("pokemon/spec_phys_lookup.txt");
         Reader reader = file.reader();
         BufferedReader br = new BufferedReader(reader);

         String line;
         while ((line = br.readLine()) != null) {
            String[] attrs = line.split(",");
            specPhysLookup.put(attrs[0].toLowerCase(Locale.ROOT).replace("_", " "), Attack.Category.valueOf(attrs[1]));
         }

         reader.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }

      gen2PhysicalTypes = new ArrayList<>();
      gen2PhysicalTypes.add("normal");
      gen2PhysicalTypes.add("fighting");
      gen2PhysicalTypes.add("poison");
      gen2PhysicalTypes.add("ground");
      gen2PhysicalTypes.add("flying");
      gen2PhysicalTypes.add("bug");
      gen2PhysicalTypes.add("rock");
      gen2PhysicalTypes.add("ghost");
      gen2PhysicalTypes.add("steel");
   }

   class CheckLockedMoves extends Action {
      public CheckLockedMoves(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         Action action = new Action() {
            @Override
            public String getCamera() {
               return "gui";
            }
         };
         if (game.battle.playerLockedMove != -1) {
            game.battle.playerLockedMoveCounter--;
            if (game.battle.playerLockedMoveCounter <= 0) {
               game.battle.playerLockedMove = -1;
            }
         }

         if (game.battle.oppLockedMove != -1) {
            game.battle.oppLockedMoveCounter--;
            if (game.battle.oppLockedMoveCounter <= 0) {
               game.battle.oppLockedMove = -1;
            }
         }

         action.append(this.nextAction);
         game.actionStack.remove(this);
         game.insertAction(action);
      }
   }

   class CheckScreens extends Action {
      public CheckScreens(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         Action action = new Action() {
            @Override
            public String getCamera() {
               return "gui";
            }
         };
         if (game.battle.playerReflectDown) {
            game.battle.playerReflectCounter--;
            if (game.battle.playerReflectCounter <= 0) {
               game.battle.playerReflectDown = false;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, "Player POKéMON' REFLECT faded!", null, true, true, null));
            }
         }

         if (game.battle.oppReflectDown) {
            game.battle.oppReflectCounter--;
            if (game.battle.oppReflectCounter <= 0) {
               game.battle.oppReflectDown = false;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, "Enemy POKéMON' REFLECT faded!", null, true, true, null));
            }
         }

         if (game.battle.playerLightScreenDown) {
            game.battle.playerLightScreenCounter--;
            if (game.battle.playerLightScreenCounter <= 0) {
               game.battle.playerLightScreenDown = false;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, "Player POKéMON' LIGHT SCREEN faded!", null, true, true, null));
            }
         }

         if (game.battle.oppLightScreenDown) {
            game.battle.oppLightScreenCounter--;
            if (game.battle.oppLightScreenCounter <= 0) {
               game.battle.oppLightScreenDown = false;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, "Enemy POKéMON' LIGHT SCREEN faded!", null, true, true, null));
            }
         }

         if (game.battle.playerSafeguardDown) {
            game.battle.playerSafeguardCounter--;
            if (game.battle.playerSafeguardCounter <= 0) {
               game.battle.playerSafeguardDown = false;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + "' SAFEGUARD faded!", null, true, true, null));
            }
         }

         if (game.battle.oppSafeguardDown) {
            game.battle.oppSafeguardCounter--;
            if (game.battle.oppSafeguardCounter <= 0) {
               game.battle.oppSafeguardDown = false;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(
                  new DisplayText(game, "Enemy " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + "' SAFEGUARD faded!", null, true, true, null)
               );
            }
         }

         action.append(this.nextAction);
         game.actionStack.remove(this);
         game.insertAction(action);
      }
   }

   static class CheckSpikes extends Action {
      boolean isFriendly;

      public CheckSpikes(Game game, boolean isFriendly, Action nextAction) {
         super();
         this.isFriendly = isFriendly;
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         Action action = new Action() {
            @Override
            public String getCamera() {
               return "gui";
            }
         };
         Pokemon target = null;
         String enemy = "";
         boolean depleteFriendly = !this.isFriendly;
         if (this.isFriendly && game.battle.playerSpikesDown && !game.player.currPokemon.types.contains("FLYING")) {
            target = game.player.currPokemon;
         } else if (!this.isFriendly && game.battle.oppSpikesDown && !game.battle.oppPokemon.types.contains("FLYING")) {
            target = game.battle.oppPokemon;
            enemy = "Enemy ";
            depleteFriendly = this.isFriendly;
         }

         if (target != null) {
            int hp = (int)Math.ceil(target.maxStats.get("hp").intValue() / 100.0 * 12.5);
            action.append(new DisplayText.Clear(game, null));
            action.append(new WaitFrames(game, 3, null));
            action.append(Battle.depleteHealth(game, depleteFriendly, hp, true, null));
            action.append(new DisplayText(game, enemy + target.nickname.toUpperCase(Locale.ROOT) + "' hurt by SPIKES!", null, true, true, null));
            action.append(new WaitFrames(game, 13, null));
         }

         action.append(this.nextAction);
         game.actionStack.remove(this);
         game.insertAction(action);
      }
   }

   class CheckTrapped extends Action {
      public CheckTrapped(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         Action action = new Action() {
            @Override
            public String getCamera() {
               return "gui";
            }
         };
         if (game.player.currPokemon.trappedBy != null) {
            Attack trap = game.battle.attacks.get(game.player.currPokemon.trappedBy);
            trap.damage = Battle.gen2CalcDamage(game.battle.oppPokemon, trap, game.player.currPokemon);
            if (trap.name.equals("thunder cage")) {
               trap.damage = game.player.currPokemon.maxStats.get("hp") / 8;
            }

            action.append(
               new Battle.LoadAndPlayAnimation(
                  game,
                  game.player.currPokemon.trappedBy,
                  game.player.currPokemon,
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           game.player.currPokemon.nickname.toUpperCase(Locale.ROOT)
                              + "' hurt by "
                              + game.player.currPokemon.trappedBy.toUpperCase(Locale.ROOT)
                              + "!",
                           null,
                           true,
                           new DepleteFriendlyHealth(
                              game.player.currPokemon, trap.damage, new DetectFriendlyFaint(game.player.currPokemon, true, new WaitFrames(game, 13, null))
                           )
                        )
                     )
                  )
               )
            );
            game.player.currPokemon.trapCounter--;
            if (game.player.currPokemon.trapCounter <= 0) {
               game.player.currPokemon.trappedBy = null;
            }
         }

         if (game.battle.oppPokemon.trappedBy != null) {
            Attack trap = game.battle.attacks.get(game.battle.oppPokemon.trappedBy);
            trap.damage = Battle.gen2CalcDamage(game.player.currPokemon, trap, game.battle.oppPokemon);
            if (trap.name.equals("thunder cage")) {
               trap.damage = game.battle.oppPokemon.maxStats.get("hp") / 8;
            }

            action.append(
               new Battle.LoadAndPlayAnimation(
                  game,
                  game.battle.oppPokemon.trappedBy,
                  game.battle.oppPokemon,
                  new DisplayText.Clear(
                     game,
                     new WaitFrames(
                        game,
                        3,
                        new DisplayText(
                           game,
                           game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT)
                              + "' hurt by "
                              + game.battle.oppPokemon.trappedBy.toUpperCase(Locale.ROOT)
                              + "!",
                           null,
                           true,
                           new DepleteEnemyHealth(game, trap.damage, new DetectEnemyFaint(true, new WaitFrames(game, 13, null)))
                        )
                     )
                  )
               )
            );
            game.battle.oppPokemon.trapCounter--;
            if (game.battle.oppPokemon.trapCounter <= 0) {
               game.battle.oppPokemon.trappedBy = null;
            }
         }

         action.append(this.nextAction);
         game.actionStack.remove(this);
         game.insertAction(action);
      }
   }

   class CheckWeather extends Action {
      public CheckWeather(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         Action action = new Action() {
            @Override
            public String getCamera() {
               return "gui";
            }
         };
         if (game.battle.weather != null) {
            game.battle.weatherCounter--;
            if (game.battle.weatherCounter <= 0) {
               String text = "The rain stopped";
               if (game.battle.weather.equals("sunny day")) {
                  text = "The sunlight faded.";
               } else if (game.battle.weather.equals("sandstorm")) {
                  text = "The SANDSTORM subsided.";
               }

               game.battle.weather = null;
               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, text, null, true, true, null));
            } else {
               String text = "Rain continues to fall.";
               if (game.battle.weather.equals("sunny day")) {
                  text = "The sunlight is strong.";
               } else if (game.battle.weather.equals("sandstorm")) {
                  text = "The SANDSTORM rages.";
               }

               action.append(new DisplayText.Clear(game, null));
               action.append(new WaitFrames(game, 3, null));
               action.append(new DisplayText(game, text, null, true, true, null));
               if (game.battle.weather.equals("sandstorm")) {
                  if (!game.player.currPokemon.types.contains("STEEL")
                     && !game.player.currPokemon.types.contains("ROCK")
                     && !game.player.currPokemon.types.contains("GROUND")) {
                     int damage = game.player.currPokemon.maxStats.get("hp") / 8;
                     action.append(new Battle.LoadAndPlayAnimation(game, "sandstorm_hit", game.player.currPokemon, null));
                     action.append(new DisplayText.Clear(game, null));
                     action.append(new WaitFrames(game, 3, null));
                     action.append(
                        new DisplayText(game, "The SANDSTORM hits " + game.player.currPokemon.nickname.toUpperCase(Locale.ROOT) + "!", null, true, true, null)
                     );
                     action.append(new DepleteFriendlyHealth(game.player.currPokemon, damage, null));
                     action.append(new DetectFriendlyFaint(game.player.currPokemon, true, null));
                     action.append(new WaitFrames(game, 13, null));
                  }

                  if (!game.battle.oppPokemon.types.contains("STEEL")
                     && !game.battle.oppPokemon.types.contains("ROCK")
                     && !game.battle.oppPokemon.types.contains("GROUND")) {
                     int damage = game.battle.oppPokemon.maxStats.get("hp") / 8;
                     action.append(new Battle.LoadAndPlayAnimation(game, "sandstorm_hit", game.battle.oppPokemon, null));
                     action.append(new DisplayText.Clear(game, null));
                     action.append(new WaitFrames(game, 3, null));
                     action.append(
                        new DisplayText(
                           game, "The SANDSTORM hits Enemy " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + "!", null, true, true, null
                        )
                     );
                     action.append(new DepleteEnemyHealth(game, damage, null));
                     action.append(new DetectEnemyFaint(true, null));
                     action.append(new WaitFrames(game, 13, null));
                  }
               }
            }
         }

         action.append(this.nextAction);
         game.actionStack.remove(this);
         game.insertAction(action);
      }
   }

   static class DoTurn extends Action {
      Battle.DoTurn.Type type = Battle.DoTurn.Type.ATTACK;

      public DoTurn(Game game, Action nextAction) {
         this(game, Battle.DoTurn.Type.ATTACK, nextAction);
      }

      public DoTurn(Game game, Battle.DoTurn.Type type, Action nextAction) {
         super();
         this.type = type;
         this.nextAction = nextAction;
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.map_500;
      }

      @Override
      public void step(Game game) {
         game.player.currPokemon.flinched = false;
         game.battle.oppPokemon.flinched = false;
         boolean enemyFirst = false;
         boolean isFriendly = true;
         Attack enemyAttack;
         if (game.type != Game.Type.CLIENT) {
            if (game.battle.oppLockedMove != -1) {
               enemyAttack = new Attack(game.battle.attacks.get(game.battle.oppPokemon.attacks[game.battle.oppLockedMove]));
            } else {
               ArrayList<String> validAttacks = new ArrayList<>();
               int i = 0;

               for (String attack : game.battle.oppPokemon.attacks) {
                  if (attack != null && game.battle.oppPokemon.disabledIndex != i) {
                     validAttacks.add(attack);
                  }

                  i++;
               }

               String attackChoice = null;
               if (validAttacks.isEmpty()) {
                  attackChoice = "struggle";
               } else {
                  attackChoice = validAttacks.get(game.map.rand.nextInt(validAttacks.size()));
               }

               enemyAttack = new Attack(game.battle.attacks.get(attackChoice.toLowerCase(Locale.ROOT)));
            }

            if (game.debugInputEnabled && Gdx.input.isKeyPressed(62)) {
               enemyAttack.damage = 0;
            }
         } else {
            com.pkmngen.game.Network.BattleTurnData turnData = game.battle.network.turnData;
            enemyAttack = turnData.enemyAttack;
            game.battle.oppPokemon.trappedBy = turnData.enemyTrappedBy;
            game.battle.oppPokemon.trapCounter = turnData.enemyTrapCounter;
         }

         Action enemyAction = new AttackAnim(game, enemyAttack, !isFriendly, null);
         Action playerAction;
         if (this.type == Battle.DoTurn.Type.SWITCH) {
            game.player.numFlees = 0;
            Pokemon nextPokemon = game.player.pokemon.get(DrawPokemonMenu.currIndex);
            playerAction = new SetField(
               game.player,
               "currPokemon",
               nextPokemon,
               new RemoveAction(
                  game.battle.drawAction.drawFriendlyHealthAction,
                  new SetField(
                     game.battle.drawAction,
                     "drawFriendlyHealthAction",
                     null,
                     new RemoveAction(
                        game.battle.drawAction.drawFriendlyPokemonAction,
                        new SetField(
                           game.battle.drawAction,
                           "drawFriendlyPokemonAction",
                           null,
                           new DrawPokemonMenu.Intro(
                              13,
                              new DisplayText(
                                 game,
                                 "Go! " + nextPokemon.nickname.toUpperCase(Locale.ROOT) + "!",
                                 null,
                                 true,
                                 false,
                                 new ThrowOutPokemonCrystal(
                                    game,
                                    new PlayMusic(
                                       nextPokemon, new WaitFrames(game, 6, new DrawFriendlyHealthGen2(game, new Battle.CheckSpikes(game, isFriendly, null)))
                                    )
                                 )
                              )
                           )
                        )
                     )
                  )
               )
            );
            if (enemyAttack.name.equals("pursuit")) {
               enemyFirst = true;
               enemyAttack.power *= 2;
               enemyAttack.damage = Battle.gen2CalcDamage(game.battle.oppPokemon, enemyAttack, game.player.currPokemon);
            }

            game.battle.oppPokemon.cantEscapeBy = null;
            game.player.currPokemon.volatileStatus.clear();
            game.player.currPokemon.volatileStatusCounter.clear();
         } else if (this.type == Battle.DoTurn.Type.ITEM) {
            game.player.numFlees = 0;
            com.pkmngen.game.Network.BattleTurnData turnData = game.battle.network.turnData;
            String itemName = turnData.itemName.toLowerCase(Locale.ROOT);
            if (itemName.contains("ball")) {
               int numWobbles;
               if (game.type != Game.Type.CLIENT) {
                  numWobbles = Battle.gen2CalcIfCaught(game, game.battle.oppPokemon, itemName);
               } else {
                  numWobbles = turnData.numWobbles;
               }

               Action catchAction;
               if (numWobbles == 0) {
                  catchAction = new CatchPokemonWobbles0Times(game, null);
               } else if (numWobbles == 1) {
                  catchAction = new CatchPokemonWobbles1Time(game, null);
               } else if (numWobbles == 2) {
                  catchAction = new CatchPokemonWobbles2Times(game, null);
               } else if (numWobbles == 3) {
                  catchAction = new CatchPokemonWobbles3Times(game, null);
               } else {
                  catchAction = new CatchPokemonWobblesThenCatch(game, itemName, null);
               }

               playerAction = new DisplayText(
                  game, game.player.name + " used " + itemName.toUpperCase(Locale.ROOT) + "!", null, catchAction, new ThrowPokeball(game, catchAction)
               );
               if (numWobbles == -1) {
                  game.battle.oppPokemon.inBattle = false;
                  game.insertAction(playerAction);
                  game.actionStack.remove(this);
                  game.battle.network.turnData = null;
                  return;
               }
            } else if (itemName.equals("silph scope")) {
               playerAction = new DisplayText(
                  game,
                  game.player.name + " used " + itemName.toUpperCase(Locale.ROOT) + "!",
                  null,
                  true,
                  false,
                  new SplitAction(
                     new WaitFrames(game, 96, new CallMethod(game.battle.oppPokemon, "revealGhost", new Object[0], null)),
                     new FadeAnim(
                        game,
                        8,
                        new SplitAction(
                           new WaitFrames(game, 4, new PlayMusic(new Pokemon(game.battle.oppPokemon.specie.name, 10), null)),
                           new PokemonIntroAnim(
                              new DisplayText.Clear(
                                 game,
                                 new WaitFrames(
                                    game,
                                    3,
                                    new DisplayText(
                                       game, "Enemy " + game.battle.oppPokemon.specie.name.toUpperCase(Locale.ROOT) + " was revealed!", null, null, null
                                    )
                                 )
                              )
                           )
                        )
                     )
                  )
               );
            } else if (!itemName.contains("berry") && !itemName.equals("moomoo milk") && !itemName.equals("revive")) {
               if (itemName.contains("poké doll")) {
                  game.player.numFlees = 0;
                  game.battle.oppPokemon.inBattle = false;
                  game.actionStack.remove(this);
                  game.insertAction(
                     new DisplayText(
                        game,
                        game.player.name + " used " + itemName.toUpperCase(Locale.ROOT) + "!",
                        null,
                        null,
                        new SplitAction(
                           new PlayMusic("run1", null),
                           new WaitFrames(
                              game,
                              18,
                              new DisplayText(
                                 game,
                                 "Got away safely!",
                                 null,
                                 null,
                                 new SplitAction(
                                    new BattleFadeOut(game, new SetField(game, "playerCanMove", true, null)),
                                    new BattleFadeOutMusic(game, new SetField(game.musicController, "resumeOverworldMusic", true, null))
                                 )
                              )
                           )
                        )
                     )
                  );
                  game.battle.network.turnData = null;
                  return;
               }

               playerAction = new DisplayText(game, "Dev note - Invalid item.", null, null, null);
            } else {
               playerAction = new Action() {
                  @Override
                  public String getCamera() {
                     return "gui";
                  }
               };
            }
         } else if (this.type == Battle.DoTurn.Type.RUN) {
            boolean runSuccessful;
            if (game.type != Game.Type.CLIENT) {
               runSuccessful = game.battle.calcIfRunSuccessful(game, game.player);
            } else {
               com.pkmngen.game.Network.BattleTurnData turnData = game.battle.network.turnData;
               runSuccessful = turnData.runSuccessful;
            }

            if (runSuccessful) {
               game.player.numFlees = 0;
               game.battle.oppPokemon.inBattle = false;
               game.actionStack.remove(this);
               game.insertAction(
                  new WaitFrames(
                     game,
                     18,
                     new DisplayText(
                        game,
                        "Got away safely!",
                        null,
                        null,
                        new SplitAction(
                           new BattleFadeOut(game, new SetField(game, "playerCanMove", true, null)),
                           new BattleFadeOutMusic(game, new SetField(game.musicController, "resumeOverworldMusic", true, null))
                        )
                     )
                  )
               );
               game.insertAction(new PlayMusic("run1", null));
               game.battle.network.turnData = null;
               return;
            }

            game.player.numFlees++;
            playerAction = new DisplayText(game, "Canì escape!", null, null, new AttackAnim(game, null, isFriendly, null));
         } else {
            game.player.numFlees = 0;
            Attack playerAttack;
            if (game.type != Game.Type.CLIENT) {
               String attackName = game.player.currPokemon.attacks[DrawAttacksMenu.curr];
               if (attackName == null) {
                  attackName = "struggle";
               }

               playerAttack = game.battle.attacks.get(attackName.toLowerCase(Locale.ROOT));
               int yourSpeed = game.player.currPokemon.currentStats.get("speed");
               int oppSpeed = game.battle.oppPokemon.currentStats.get("speed");
               if (playerAttack.priority != enemyAttack.priority) {
                  enemyFirst = enemyAttack.priority > playerAttack.priority;
               } else if (yourSpeed > oppSpeed) {
                  enemyFirst = false;
               } else if (yourSpeed < oppSpeed) {
                  enemyFirst = true;
               } else {
                  int randNum = game.map.rand.nextInt(2);
                  if (randNum == 0) {
                     enemyFirst = true;
                  }
               }
            } else {
               com.pkmngen.game.Network.BattleTurnData turnData = game.battle.network.turnData;
               enemyFirst = turnData.oppFirst;
               playerAttack = turnData.playerAttack;
               game.player.currPokemon.trappedBy = turnData.playerTrappedBy;
               game.player.currPokemon.trapCounter = turnData.playerTrapCounter;
            }

            playerAction = new AttackAnim(game, playerAttack, isFriendly, null);
         }

         if (game.battle.network.expectPlayerSwitch) {
            game.battle.network.expectPlayerSwitch = false;
            Action doTurn = playerAction;
            doTurn.append(new DisplayText.Clear(game, new WaitFrames(game, 3, this.nextAction)));
            game.actionStack.remove(this);
            game.insertAction(doTurn);
            game.battle.network.turnData = null;
         } else {
            Action doTurn;
            if (!enemyFirst) {
               doTurn = playerAction;
               doTurn.append(new DisplayText.Clear(game, new WaitFrames(game, 3, enemyAction)));
            } else {
               doTurn = enemyAction;
               doTurn.append(new DisplayText.Clear(game, new WaitFrames(game, 3, playerAction)));
            }

            doTurn.append(game.battle.new CheckWeather(game, null));
            doTurn.append(game.battle.new CheckTrapped(game, null));
            doTurn.append(game.battle.new CheckScreens(game, null));
            doTurn.append(game.battle.new CheckLockedMoves(game, null));
            doTurn.append(new SetField(game.battle, "turnNumber", game.battle.turnNumber + 1, null));
            doTurn.append(new DisplayText.Clear(game, new WaitFrames(game, 3, this.nextAction)));
            game.actionStack.remove(this);
            game.insertAction(doTurn);
            game.battle.network.turnData = null;
         }
      }

      public enum Type {
         ATTACK,
         SWITCH,
         ITEM,
         RUN;
      }
   }

   public static class GetIntroAction extends Action {
      public Action.Layer layer = Action.Layer.map_0;

      public GetIntroAction(Action nextAction) {
         super();
         this.nextAction = nextAction;
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
         game.insertAction(Battle.getIntroAction(game));
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }

   static class LoadAndPlayAnimation extends Action {
      public Action.Layer layer = Action.Layer.gui_103;
      String name;
      HashMap<Integer, String> metadata = new HashMap<>();
      Music sound;
      int frameNum = 1;
      Texture currText;
      Sprite currFrame;
      Pokemon target;
      boolean firstStep = true;
      Matrix4 translation;
      Vector2 playerSpriteOrigin;
      Vector2 enemySpriteOrigin = new Vector2();
      Pixmap pixmap;
      Pixmap newPixmap;
      Texture drawTexture;
      int pixmapX;
      int pixmapY;
      int dispLastFrame = 1;
      int dispProgress = 0;
      Pixmap regionPixmap;
      Texture regionTexture;
      SpriteProxy regionProxy;
      int[][] screenRegions = new int[][]{{94, 84, 66, 56}, {0, 48, 80, 48}, {0, 104, 92, 40}, {68, 48, 92, 40}};
      int inverseLastFrame = -1;
      ShaderProgram grayscaleShader = new ShaderProgram(EvolutionAnim.vertexShader, EvolutionAnim.fragmentShader);

      public LoadAndPlayAnimation(Game game, String name, Pokemon target, Action nextAction) {
         super();
         this.name = name.toLowerCase(Locale.ROOT).replace(' ', '_');
         if (target == game.player.currPokemon) {
            this.name = this.name + "_enemy_gsc";
         } else if (game.battle.oppPokemon != null && target == game.battle.oppPokemon) {
            this.name = this.name + "_player_gsc";
         } else {
            this.name = this.name + "_gsc";
         }

         this.target = target;
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
         if (this.firstStep) {
            if (!Game.battleAnims && !this.name.contains("_effective_") && !this.name.contains("shiny_") && !this.name.contains("stat_stage_hit")) {
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
               return;
            }

            if (this.name.contains("crush_grip")) {
               game.actionStack.remove(this);
               game.insertAction(new Attack.CrushGrip(game, this.target, this.nextAction));
               return;
            }

            System.out.println(this.name);

            try {
               FileHandle file = Gdx.files.internal("attacks/" + this.name + "/metadata.out");
               Reader reader = file.reader();
               BufferedReader br = new BufferedReader(reader);

               String line;
               while ((line = br.readLine()) != null) {
                  int frameNum = Integer.valueOf(line.split(", ")[0]);
                  String properties = line.split(", ")[1];
                  this.metadata.put(frameNum, properties);
               }

               reader.close();
               if (!this.name.contains("evolve")) {
                  this.sound = AudioLoader.loadMusic("attacks/" + this.name + "/sound.ogg");
                  this.sound.play();
               }
            } catch (FileNotFoundException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            } catch (GdxRuntimeException var19) {
            }

            this.firstStep = false;
            this.playerSpriteOrigin = new Vector2(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY());
            if (game.battle.oppPokemon != null) {
               this.enemySpriteOrigin = new Vector2(game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY());
            }

            int i = 0;

            while (true) {
               FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format(Locale.ROOT, "%03d", i) + ".png");
               if (!filehandle.exists()) {
                  break;
               }

               TextureCache.get(filehandle);
               i++;
            }
         }

         DrawEnemyHealth.shouldDraw = true;
         DrawFriendlyHealth.shouldDraw = true;
         DrawBattle.shouldDrawOwnPokemon = true;
         DrawBattle.shouldDrawOppPokemon = true;
         EvolutionAnim.isGreyscale = false;
         SpriteProxy.inverseColors = false;
         SpriteProxy.darkenAllColors1 = false;
         SpriteProxy.darkenAllColors2 = false;
         SpriteProxy.darkenAllColors3 = false;
         SpriteProxy.lightenAllColors1 = false;
         SpriteProxy.lightenAllColors2 = false;
         SpriteProxy.confuseRayColors1 = false;
         SpriteProxy.confuseRayColors2 = false;
         game.player.currPokemon.backSprite.lightenColors1 = false;
         game.player.currPokemon.backSprite.lightenColors2 = false;
         game.player.currPokemon.backSprite.darkenColors1 = false;
         game.player.currPokemon.backSprite.darkenColors2 = false;
         game.player.currPokemon.backSprite.darkenColors3 = false;
         if (game.battle.oppPokemon != null) {
            game.battle.oppPokemon.sprite.lightenColors1 = false;
            game.battle.oppPokemon.sprite.lightenColors2 = false;
            game.battle.oppPokemon.sprite.darkenColors1 = false;
            game.battle.oppPokemon.sprite.darkenColors2 = false;
            game.battle.oppPokemon.sprite.darkenColors3 = false;
            game.battle.oppPokemon.sprite.setPosition(this.enemySpriteOrigin.x, this.enemySpriteOrigin.y);
         }

         game.player.currPokemon.backSprite.lightenColors2 = false;
         game.player.currPokemon.backSprite.setPosition(this.playerSpriteOrigin.x, this.playerSpriteOrigin.y);
         if (game.battle.drawAction != null) {
            game.battle.drawAction.drawEnemyHealthAction.translateAmt.set(0.0F, 0.0F);
         }

         game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(0.0F, 0.0F, 0.0F), new Quaternion(), new Vector3(1.0F, 1.0F, 1.0F)));
         FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format(Locale.ROOT, "%03d", this.frameNum) + ".png");
         if (!filehandle.exists()) {
            if (this.drawTexture != null) {
               this.drawTexture.dispose();
            }

            if (this.regionProxy != null) {
               this.regionProxy.getTexture().dispose();
            }

            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         } else {
            EvolutionAnim.drawPostEvoBottom = false;
            EvolutionAnim.drawPostEvoTop = false;
            if (Game.photosensitiveMode && this.metadata.containsKey(this.frameNum)) {
               String properties = this.metadata.get(this.frameNum);
               if (this.inverseLastFrame == -1 && properties.contains("inverse_colors")) {
                  this.inverseLastFrame = this.frameNum;
               } else if (this.inverseLastFrame > -1 && !properties.contains("inverse_colors")) {
                  if (this.frameNum - this.inverseLastFrame < 20) {
                     properties = properties + " inverse_colors";
                     this.metadata.put(this.frameNum, properties);
                  } else {
                     this.inverseLastFrame = -1;
                  }
               }
            }

            if (this.metadata.containsKey(this.frameNum)) {
               String properties = this.metadata.get(this.frameNum);
               if (properties.contains("screenshot")) {
                  String[] values = properties.split("screenshot:")[1].split(" ")[0].split(",");
                  this.pixmapX = Integer.valueOf(values[0]);
                  this.pixmapY = Integer.valueOf(values[1]);
                  int offsetX = (int)((game.currScreen.x - 160.0F * game.currScreen.y / 144.0F) / 2.0F);
                  this.pixmap = ScreenUtils.getFrameBufferPixmap(offsetX, 0, (int)game.currScreen.x - offsetX * 2, (int)game.currScreen.y);
                  float heightM = game.currScreen.y / 144.0F;
                  this.newPixmap = new Pixmap((int)(this.pixmap.getWidth() / heightM), (int)(this.pixmap.getHeight() / heightM), Format.RGBA8888);
                  this.newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
                  this.drawTexture = new Texture(this.newPixmap);
               }

               if (properties.contains("row_copy") || properties.contains("row_displace") || properties.contains("row_split")) {
                  float heightM = game.currScreen.y / 144.0F;
                  this.newPixmap.fill();

                  for (int i = 0; i < this.newPixmap.getWidth(); i++) {
                     for (int j = 0; j < this.newPixmap.getHeight(); j++) {
                        Color color = new Color(this.pixmap.getPixel((int)(i * heightM), (int)(j * heightM)));
                        this.newPixmap.drawPixel(i, j, Color.rgba8888(color));
                     }
                  }

                  boolean playerSpriteIgnore = this.name.contains("player") && !this.name.contains("surf") && !this.name.contains("whirlpool");
                  boolean enemySpriteIgnore = this.name.contains("enemy") && !this.name.contains("surf") && !this.name.contains("whirlpool");
                  if (properties.contains("row_copy")) {
                     String[] copies = properties.split(" row_copy:");
                     int i = 0;

                     for (String copy : copies) {
                        copy = copy.split(" ")[0];
                        if (++i != 1) {
                           int targetY = Integer.valueOf(copy.split(",")[0]);
                           int sourceY = Integer.valueOf(copy.split(",")[1]);

                           for (int x = 0; x < this.newPixmap.getWidth(); x++) {
                              if ((!playerSpriteIgnore || x >= 86 || 144 - targetY >= 112) && (!enemySpriteIgnore || x < 96 || 144 - targetY < 88)) {
                                 Color color = new Color(this.pixmap.getPixel((int)(x * heightM), (int)((144 - sourceY) * heightM)));
                                 this.newPixmap.drawPixel(x, 144 - targetY, Color.rgba8888(color));
                              }
                           }
                        }
                     }
                  }

                  if (properties.contains("row_displace")) {
                     double speed = Double.valueOf(properties.split("row_displace:")[1].split(" ")[0]);
                     if (this.frameNum >= this.dispLastFrame + 1.0 / speed) {
                        this.dispLastFrame = this.frameNum;
                        this.dispProgress++;
                     }

                     int bottomLimit = 48;
                     playerSpriteIgnore = false;
                     enemySpriteIgnore = false;
                     if (properties.contains("displace_target")) {
                        String disp_target = properties.split("displace_target:")[1].split(" ")[0];
                        if (disp_target.contains("enemy")) {
                           playerSpriteIgnore = true;
                        } else if (disp_target.contains("player")) {
                           enemySpriteIgnore = true;
                        }
                     }

                     for (int y = bottomLimit; y < this.newPixmap.getHeight(); y++) {
                        if ((!playerSpriteIgnore || y >= 88) && (!enemySpriteIgnore || y < 92)) {
                           int shift = (int)(5.0 * Math.sin((Math.PI / 6) * (y + this.dispProgress)));

                           for (int x = 0; x < this.newPixmap.getWidth(); x++) {
                              if ((!playerSpriteIgnore || x >= 86) && (!enemySpriteIgnore || x < 96)) {
                                 Color color = new Color(this.pixmap.getPixel((int)(x * heightM), (int)(y * heightM)));
                                 this.newPixmap.drawPixel(x + shift, y, Color.rgba8888(color));
                              }
                           }
                        }
                     }
                  }

                  if (properties.contains("row_split")) {
                     int progress = Integer.valueOf(properties.split("row_split:")[1].split(" ")[0]);
                     int bottomLimit = 48;
                     playerSpriteIgnore = this.name.contains("enemy");
                     enemySpriteIgnore = this.name.contains("player");

                     for (int y = bottomLimit; y < this.newPixmap.getHeight(); y++) {
                        int sign;
                        if (y % 2 == 0) {
                           sign = -1;
                        } else {
                           sign = 1;
                        }

                        if ((!playerSpriteIgnore || y >= 88) && (!enemySpriteIgnore || y < 92)) {
                           for (int x = 0; x < this.newPixmap.getWidth(); x++) {
                              if ((!playerSpriteIgnore || x >= 86) && (!enemySpriteIgnore || x < 96)) {
                                 Color color = new Color(this.pixmap.getPixel((int)(x * heightM), (int)(y * heightM)));
                                 this.newPixmap.drawPixel(x + progress * sign, y, Color.rgba8888(color));
                              }
                           }
                        }
                     }
                  }

                  this.drawTexture.draw(this.newPixmap, 0, 0);
                  game.uiBatch
                     .draw(
                        this.drawTexture,
                        0.0F,
                        0.0F,
                        this.drawTexture.getWidth(),
                        this.drawTexture.getHeight(),
                        0,
                        0,
                        this.drawTexture.getWidth(),
                        this.drawTexture.getHeight(),
                        false,
                        true
                     );
                  if (properties.contains(" darken_effect1")) {
                     SpriteProxy.darkenAllColors1 = true;
                  }

                  if (properties.contains(" darken_effect2")) {
                     SpriteProxy.darkenAllColors2 = true;
                  }

                  if (properties.contains(" darken_effect3")) {
                     SpriteProxy.darkenAllColors3 = true;
                  }

                  if (properties.contains(" lighten_effect1")) {
                     SpriteProxy.lightenAllColors1 = true;
                  }

                  if (properties.contains(" lighten_effect2")) {
                     SpriteProxy.lightenAllColors2 = true;
                  }

                  if (properties.contains(" inverse_colors")) {
                     SpriteProxy.inverseColors = true;
                  }

                  if (SpriteProxy.darkenAllColors1
                     || SpriteProxy.darkenAllColors2
                     || SpriteProxy.darkenAllColors3
                     || SpriteProxy.lightenAllColors1
                     || SpriteProxy.lightenAllColors2
                     || SpriteProxy.inverseColors) {
                     if (properties.contains(" inverse_colors")) {
                        this.regionPixmap = new Pixmap(160, 96, Format.RGBA8888);
                        this.regionPixmap.setColor(Color.BLACK);
                        this.regionPixmap.fill();
                        this.regionTexture = new Texture(this.regionPixmap);
                        game.uiBatch
                           .draw(
                              this.regionTexture,
                              0.0F,
                              48.0F,
                              this.regionTexture.getWidth(),
                              this.regionTexture.getHeight(),
                              0,
                              0,
                              this.regionTexture.getWidth(),
                              this.regionTexture.getHeight(),
                              false,
                              false
                           );
                     }

                     for (int r = 0; r < this.screenRegions.length; r++) {
                        String colors = "";
                        int numColors = 0;
                        this.regionPixmap = new Pixmap(this.screenRegions[r][2], this.screenRegions[r][3], Format.RGBA8888);
                        this.regionPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
                        this.regionPixmap.fill();

                        for (int i = 0; i < this.regionPixmap.getWidth(); i++) {
                           for (int j = 0; j < this.regionPixmap.getHeight(); j++) {
                              Color color = new Color(this.newPixmap.getPixel(this.screenRegions[r][0] + i, this.screenRegions[r][1] + j));
                              if (color.r != 1.0 || !(color.g >= 0.0F) || color.b != 1.0) {
                                 this.regionPixmap.drawPixel(i, j, Color.rgba8888(color));
                                 if (!colors.contains(color.toString())) {
                                    colors = colors + " " + color.toString();
                                    numColors++;
                                 }
                              } else if (properties.contains(" inverse_colors") && color.r == 1.0 && color.g == 1.0 && color.b == 1.0) {
                                 this.regionPixmap.drawPixel(i, j, Color.rgba8888(color));
                              }
                           }
                        }

                        if (numColors > 1) {
                           this.regionTexture = new Texture(this.regionPixmap);
                           this.regionProxy = new SpriteProxy(this.regionTexture, 0, 0, this.screenRegions[r][2], this.screenRegions[r][3]);
                           if (this.regionProxy.color1 != null) {
                              this.regionProxy.flip(false, true);
                              this.regionProxy.setPosition(this.screenRegions[r][0], this.screenRegions[r][1]);
                              this.regionProxy.draw(game.uiBatch);
                           }
                        }
                     }

                     this.regionPixmap.dispose();
                     this.regionTexture.dispose();
                  }
               }
            }

            if (this.metadata.containsKey(this.frameNum)) {
               String properties = this.metadata.get(this.frameNum);
               if (properties.contains("player_shrink") || properties.contains("enemy_shrink")) {
                  int progress = 1;
                  Sprite baseSprite = game.player.currPokemon.backSprite;
                  Vector2 position = new Vector2(0.0F, 48.0F);
                  if (properties.contains("enemy_shrink")) {
                     baseSprite = game.battle.oppPokemon.sprite;
                     progress = Integer.valueOf(properties.split("enemy_shrink:")[1].split(" ")[0]);
                     position.x = game.battle.oppPokemon.sprite.getX() - (56.0F - game.battle.oppPokemon.sprite.getWidth());
                     position.y = game.battle.oppPokemon.sprite.getY();
                  } else {
                     progress = Integer.valueOf(properties.split("player_shrink:")[1].split(" ")[0]);
                  }

                  Sprite temp = new Sprite(baseSprite);
                  TextureRegion[][] tempRegion = temp.split(8, 8);
                  TextureRegion[][] tempRegion2 = new TextureRegion[7][7];
                  int offsetX = 7 - tempRegion.length;

                  for (int i = 0; i < tempRegion.length; i++) {
                     for (int j = 0; j < tempRegion[i].length; j++) {
                        tempRegion2[i][j + offsetX] = tempRegion[i][j];
                     }
                  }

                  Sprite[][] temp2 = new Sprite[7][7];

                  for (int i = 0; i < tempRegion2.length; i++) {
                     for (int j = 0; j < tempRegion2[i].length; j++) {
                        if (tempRegion2[6 - j][i] != null) {
                           temp2[i][j] = new Sprite(tempRegion2[6 - j][i]);
                        }
                     }
                  }

                  for (int i = 0; i < temp2.length; i++) {
                     temp2[i][1] = temp2[i][2];
                     temp2[i][2] = temp2[i][3];
                     temp2[i][3] = temp2[i][5];
                     temp2[i][4] = null;
                     temp2[i][5] = null;
                  }

                  temp2[2] = temp2[1];
                  temp2[1] = new Sprite[0];
                  temp2[5] = temp2[6];
                  temp2[6] = new Sprite[0];
                  Sprite[][][] drawSprite = new Sprite[][][]{temp2, null, null};
                  if (progress == 2) {
                     Sprite[][] temp3 = new Sprite[7][7];

                     for (int i = 0; i < temp2.length; i++) {
                        for (int j = 0; j < temp2[i].length; j++) {
                           temp3[i][j] = temp2[i][j];
                        }
                     }

                     for (int i = 0; i < temp3.length; i++) {
                        if (temp3[i].length > 0) {
                           temp3[i][1] = temp3[i][3];
                           temp3[i][2] = null;
                           temp3[i][3] = null;
                           temp3[i][4] = null;
                           temp3[i][5] = null;
                        }
                     }

                     temp3[3] = temp3[2];
                     temp3[2] = new Sprite[0];
                     temp3[4] = temp3[5];
                     temp3[5] = new Sprite[0];
                     temp3[6] = new Sprite[0];
                     drawSprite = new Sprite[][][]{temp3, null, null};
                  }

                  for (int k = 0; k < drawSprite.length; k++) {
                     if (drawSprite[k] != null) {
                        for (int i = 0; i < drawSprite[k].length; i++) {
                           for (int j = 0; j < drawSprite[k][i].length; j++) {
                              if (drawSprite[k][i][j] != null) {
                                 drawSprite[k][i][j].setPosition(position.x + 8 * i - 4 * k, position.y + 8 * j - 8 * k);
                                 drawSprite[k][i][j].draw(game.uiBatch);
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.currText = TextureCache.get(filehandle);
            this.currFrame = new Sprite(this.currText, 0, 0, 160, 144);
            this.currFrame.draw(game.uiBatch);
            if (this.metadata.containsKey(this.frameNum)) {
               String properties = this.metadata.get(this.frameNum);
               if (properties.contains("enemy_healthbar_gone")) {
                  DrawEnemyHealth.shouldDraw = false;
               }

               if (properties.contains("player_healthbar_gone")) {
                  DrawFriendlyHealth.shouldDraw = false;
               }

               if (properties.contains("player_sprite_gone")) {
                  DrawBattle.shouldDrawOwnPokemon = false;
               }

               if (properties.contains("enemy_sprite_gone")) {
                  DrawBattle.shouldDrawOppPokemon = false;
               }

               if (properties.contains("player_sprite_hide")) {
                  DrawBattle.hideOwnPokemon = true;
               }

               if (properties.contains("enemy_sprite_hide")) {
                  DrawBattle.hideOppPokemon = true;
               }

               if (properties.contains("player_sprite_show")) {
                  DrawBattle.hideOwnPokemon = false;
               }

               if (properties.contains("enemy_sprite_show")) {
                  DrawBattle.hideOppPokemon = false;
               }

               if (properties.contains("screen_translate_y")) {
                  int translateAmt = Integer.valueOf(properties.split("screen_translate_y:")[1].split(" ")[0]);
                  game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(0.0F, translateAmt, 0.0F), new Quaternion(), new Vector3(1.0F, 1.0F, 1.0F)));
               }

               if (properties.contains("screen_translate_x")) {
                  int translateAmt = Integer.valueOf(properties.split("screen_translate_x:")[1].split(" ")[0]);
                  game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(translateAmt, 0.0F, 0.0F), new Quaternion(), new Vector3(1.0F, 1.0F, 1.0F)));
               }

               if (properties.contains("player_translate_x")) {
                  int translateAmt = Integer.valueOf(properties.split("player_translate_x:")[1].split(" ")[0]);
                  game.player
                     .currPokemon
                     .backSprite
                     .setPosition(game.player.currPokemon.backSprite.getX() + translateAmt, game.player.currPokemon.backSprite.getY());
               }

               if (properties.contains("player_translate_y")) {
                  int translateAmt = Integer.valueOf(properties.split("player_translate_y:")[1].split(" ")[0]);
                  game.player
                     .currPokemon
                     .backSprite
                     .setPosition(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY() + translateAmt);
               }

               if (properties.contains("enemy_translate_x")) {
                  int translateAmt = Integer.valueOf(properties.split("enemy_translate_x:")[1].split(" ")[0]);
                  game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX() + translateAmt, game.battle.oppPokemon.sprite.getY());
                  game.battle.drawAction.drawEnemyHealthAction.translateAmt.set(translateAmt, 0.0F);
               }

               if (properties.contains("enemy_sprite_translate_x")) {
                  int translateAmt = Integer.valueOf(properties.split("enemy_sprite_translate_x:")[1].split(" ")[0]);
                  game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX() + translateAmt, game.battle.oppPokemon.sprite.getY());
               }

               if (properties.contains("enemy_translate_y")) {
                  int translateAmt = Integer.valueOf(properties.split("enemy_translate_y:")[1].split(" ")[0]);
                  game.battle.oppPokemon.sprite.setPosition(game.battle.oppPokemon.sprite.getX(), game.battle.oppPokemon.sprite.getY() + translateAmt);
               }

               if (properties.contains("evo_top_sprite_changed")) {
                  EvolutionAnim.drawPostEvoTop = true;
               }

               if (properties.contains("evo_bottom_sprite_changed")) {
                  EvolutionAnim.drawPostEvoBottom = true;
               }

               if (properties.contains("draw_hatch_bottom_sprite")) {
                  EggHatchAnim.drawPostHatchBottom = true;
               }

               if (properties.contains("draw_hatch_top_sprite")) {
                  EggHatchAnim.drawPostHatchTop = true;
               }

               if (properties.contains("sprite_greyscale")) {
                  EvolutionAnim.isGreyscale = true;
               }

               if (properties.contains(" darken_effect1")) {
                  SpriteProxy.darkenAllColors1 = true;
               }

               if (properties.contains(" darken_effect2")) {
                  SpriteProxy.darkenAllColors2 = true;
               }

               if (properties.contains(" darken_effect3")) {
                  SpriteProxy.darkenAllColors3 = true;
               }

               if (properties.contains("confuseray_effect1")) {
                  SpriteProxy.confuseRayColors1 = true;
               }

               if (properties.contains("confuseray_effect2")) {
                  SpriteProxy.confuseRayColors2 = true;
               }

               if (properties.contains(" lighten_effect1")) {
                  SpriteProxy.lightenAllColors1 = true;
               }

               if (properties.contains(" lighten_effect2")) {
                  SpriteProxy.lightenAllColors2 = true;
               }

               if (properties.contains("player_lighten_effect1")) {
                  game.player.currPokemon.backSprite.lightenColors1 = true;
               }

               if (properties.contains("player_lighten_effect2")) {
                  game.player.currPokemon.backSprite.lightenColors2 = true;
               }

               if (properties.contains("enemy_lighten_effect1")) {
                  game.battle.oppPokemon.sprite.lightenColors1 = true;
               }

               if (properties.contains("enemy_lighten_effect2")) {
                  game.battle.oppPokemon.sprite.lightenColors2 = true;
               }

               if (properties.contains("player_darken_effect1")) {
                  game.player.currPokemon.backSprite.darkenColors1 = true;
               }

               if (properties.contains("player_darken_effect2")) {
                  game.player.currPokemon.backSprite.darkenColors2 = true;
               }

               if (properties.contains("player_darken_effect3")) {
                  game.player.currPokemon.backSprite.darkenColors3 = true;
               }

               if (properties.contains("enemy_darken_effect1")) {
                  game.battle.oppPokemon.sprite.darkenColors1 = true;
               }

               if (properties.contains("enemy_darken_effect2")) {
                  game.battle.oppPokemon.sprite.darkenColors2 = true;
               }

               if (properties.contains("enemy_darken_effect3")) {
                  game.battle.oppPokemon.sprite.darkenColors3 = true;
               }

               if (properties.contains("play_evo_fanfare")) {
                  EvolutionAnim.playSound = true;
               }

               if (properties.contains("inverse_colors")) {
                  SpriteProxy.inverseColors = true;
               }

               if (properties.contains("user_cry")) {
                  if (this.sound != null) {
                     this.sound.dispose();
                  }

                  Pokemon user = game.player.currPokemon;
                  if (this.target == game.player.currPokemon) {
                     user = game.battle.oppPokemon;
                  }

                  this.sound = AudioLoader.loadMusic("pokemon/cries/" + user.dexNumber + ".ogg");
                  this.sound.play();
               }
            }

            this.frameNum++;
         }
      }
   }

   class Network {
      com.pkmngen.game.Network.BattleTurnData turnData;
      boolean expectPlayerSwitch = false;

      public Network() {
      }
   }

   static class WaitTurnData extends Action {
      Action text;

      public WaitTurnData(Game game, Action nextAction) {
         super();
         this.nextAction = nextAction;
      }

      @Override
      public void firstStep(Game game) {
         DisplayText.textPersist = false;
         this.text = new DisplayText(game, "Waiting for server...", null, true, false, null);
         game.insertAction(this.text);
         this.text.step(game);
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.gui_500;
      }

      @Override
      public void step(Game game) {
         if (game.battle.network.turnData != null) {
            game.actionStack.remove(this.text);
            DisplayText.textPersist = false;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
