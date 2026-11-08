package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.Locale;

class CatchPokemonWobblesThenCatch extends Action {
   ArrayList<Vector2> positions;
   Vector2 position;
   ArrayList<Sprite> sprites;
   Sprite sprite;
   ArrayList<Integer> repeats;
   String sound;
   ArrayList<String> sounds;
   Tile pedistalTile;
   String ballUsed;
   public Action.Layer layer = Action.Layer.gui_120;

   public CatchPokemonWobblesThenCatch(Game game, String ballUsed, Action nextAction) {
      super();
      this.ballUsed = ballUsed;
      this.nextAction = nextAction;
      Texture text = TextureCache.get(Gdx.files.internal("pokeball1_color.png"));
      this.position = new Vector2(114.0F, 88.0F);
      this.positions = new ArrayList<>();
      this.positions.add(new Vector2(0.0F, 0.0F));

      for (int i = 0; i < 3; i++) {
         this.positions.add(new Vector2(-1.0F, 0.0F));
         this.positions.add(new Vector2(1.0F, 0.0F));
         this.positions.add(new Vector2(1.0F, 0.0F));
         this.positions.add(new Vector2(-1.0F, 0.0F));
      }

      this.sprites = new ArrayList<>();
      this.sprites.add(null);
      text = TextureCache.get(Gdx.files.internal("pokeball_wiggleSheet1_color.png"));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 12, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 24, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 12, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 24, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 12, 0, 12, 12));
      this.sprites.add(new Sprite(text, 0, 0, 12, 12));
      this.sprites.add(new Sprite(text, 24, 0, 12, 12));
      this.repeats = new ArrayList<>();
      this.repeats.add(12);
      this.repeats.add(43);

      for (int i = 0; i < 3; i++) {
         this.repeats.add(3);
      }

      this.repeats.add(43);

      for (int i = 0; i < 3; i++) {
         this.repeats.add(3);
      }

      this.repeats.add(43);

      for (int i = 0; i < 2; i++) {
         this.repeats.add(3);
      }

      this.repeats.add(5);
      this.sounds = new ArrayList<>();
      this.sounds.add("pokeball_wiggle1");
      this.sounds.add(null);
      this.sounds.add("pokeball_wiggle1");

      for (int i = 0; i < 3; i++) {
         this.sounds.add(null);
      }

      this.sounds.add("pokeball_wiggle1");

      for (int i = 0; i < 6; i++) {
         this.sounds.add(null);
      }
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
      for (Tile tile : game.map.tiles.values()) {
         if (tile.name.contains("cave1_regipedistal1")) {
            this.pedistalTile = tile;
            break;
         }
      }
   }

   @Override
   public void step(Game game) {
      DrawBattle.shouldDrawOppPokemon = false;
      if (!this.positions.isEmpty() && !this.sprites.isEmpty()) {
         this.sound = this.sounds.get(0);
         if (this.sound != null) {
            game.insertAction(new PlaySound(this.sound, null));
            this.sounds.set(0, null);
         }

         this.sprite = this.sprites.get(0);
         if (this.sprite != null) {
            this.sprite.setPosition(this.position.x, this.position.y);
            this.sprite.draw(game.uiBatch);
         }

         if (this.repeats.get(0) > 0) {
            this.repeats.set(0, this.repeats.get(0) - 1);
         } else {
            this.position = this.position.add(this.positions.get(0));
            this.positions.remove(0);
            this.sprites.remove(0);
            this.repeats.remove(0);
            this.sounds.remove(0);
         }
      } else {
         if (game.battle.oppPokemon.standingAction != null) {
            game.battle.oppPokemon.removeDrawActions(game);
            game.battle.oppPokemon.standingAction = null;
            if (game.battle.oppPokemon.isTrapping) {
               Tile tile = game.map.tiles.get(game.battle.oppPokemon.position);
               tile.items.remove(game.battle.oppPokemon.specie.name.toLowerCase(Locale.ROOT));
               game.map.burrowedPokemon.remove(game.battle.oppPokemon.position);
            } else {
               game.map.pokemon.remove(game.battle.oppPokemon.position);
            }
         }

         if (game.battle.oppPokemon.isTrapping) {
            Tile tile = game.map.tiles.get(game.battle.oppPokemon.position);
            tile.items.remove(game.battle.oppPokemon.specie.name.toLowerCase(Locale.ROOT));
         }

         PokemonCaughtEvents newAction = new PokemonCaughtEvents(
            game,
            new DisplayText(game, "All right! " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " was caught!", "catch_fanfare.ogg", null, null)
         );
         if (Game.catchExpEnabled) {
            ArrayList<Pokemon> participated = new ArrayList<>();

            for (Pokemon pokemon : game.player.pokemon) {
               if (!pokemon.isEgg && pokemon.level < 100 && pokemon.currentStats.get("hp") > 0) {
                  if (pokemon == game.player.currPokemon) {
                     participated.add(0, pokemon);
                  } else {
                     participated.add(pokemon);
                  }
               }
            }

            if (!participated.isEmpty()) {
               int expAmount = game.battle.calcFaintExp(participated.size());
               newAction.append(
                  new DisplayText(game, game.player.name.toUpperCase(Locale.ROOT) + "' POKéMON gained " + expAmount + " EXP. Points!", null, null, null)
               );

               for (Pokemon pokemon : participated) {
                  pokemon.exp += expAmount;
                  if (pokemon == game.player.currPokemon) {
                     newAction.append(new GainExpAnimationGen2(pokemon, null));
                  } else {
                     newAction.append(new GainExpAnimation(pokemon, null));
                  }
               }

               for (Pokemon pokemon : participated) {
                  newAction.append(new CheckEvo(pokemon, null));
               }
            }
         }

         newAction.append(new SetField(newAction, "done", true, new SplitAction(new BattleFadeOut(game, null), new BattleFadeOutMusic(game, null))));
         if (!game.battle.oppPokemon.specie.name.contains("regi")) {
            newAction.append(new SetField(game.musicController, "resumeOverworldMusic", true, null));
         }

         game.battle.oppPokemon.previousOwner = game.player;
         game.battle.oppPokemon.aggroPlayer = false;
         game.battle.oppPokemon.cantEscapeBy = null;
         game.battle.oppPokemon.volatileStatus.clear();
         game.battle.oppPokemon.volatileStatusCounter.clear();
         if (game.player.pokemon.size() < 6) {
            game.player.pokemon.add(game.battle.oppPokemon);
         } else {
            game.battle.oppPokemon.position = game.player.spawnLoc.cpy();
            game.battle.oppPokemon.mapTiles = game.map.overworldTiles;
            if (game.player.spawnIndex != -1) {
               game.battle.oppPokemon.mapTiles = game.map.interiorTiles.get(game.player.spawnIndex);
               game.battle.oppPokemon.interiorIndex = game.player.spawnIndex;
            }

            newAction.append(
               new DisplayText(
                  game,
                  "Your party is full! " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " was sent to the last safe place.",
                  null,
                  null,
                  null
               )
            );
            game.insertAction(game.battle.oppPokemon.new Standing());
         }

         if (SpecialMewtwo1.class.isInstance(game.battle.oppPokemon)) {
            Tile tile = ((SpecialMewtwo1)game.battle.oppPokemon).tile;
            tile.nameUpper = "";
            tile.init();
            if (game.battle.oppPokemon.isShiny) {
               game.battle.oppPokemon.sprite = game.battle.oppPokemon.specie.spriteShiny;
            } else {
               game.battle.oppPokemon.sprite = game.battle.oppPokemon.specie.sprite;
            }
         } else if (game.battle.oppPokemon.onTile != null && game.battle.oppPokemon.onTile.nameUpper.contains("revived_")) {
            game.battle.oppPokemon.onTile.nameUpper = "";
         } else if (game.battle.oppPokemon.onTile != null && game.battle.oppPokemon.onTile.nameUpper.equals("volcarona")) {
            Tile tile = game.battle.oppPokemon.onTile;
            tile.nameUpper = "";
            tile.init(tile.name, tile.nameUpper, tile.position, true, tile.routeBelongsTo);
         } else if (game.battle.oppPokemon.onTile != null && game.battle.oppPokemon.onTile.nameUpper.equals("spiritomb")) {
            Tile tile = game.battle.oppPokemon.onTile;
            tile.nameUpper = "";
            tile.init();
         } else {
            game.map.currRoute.storedPokemon.remove(game.battle.oppPokemon);
         }

         if (game.player.pokemon.size() >= 6 && !game.player.displayedMaxPartyText) {
            game.player.displayedMaxPartyText = true;
            newAction.append(new DisplayText(game, "Your party is full! You will need to DROP some of them in order to catch more.", null, null, null));
         }

         if (game.battle.oppPokemon.specie.name.equals("regigigas")) {
            Tile regiTile = null;

            for (Tile tile : game.map.tiles.values()) {
               if (tile.name.contains("cave1_regi3")) {
                  regiTile = tile;
                  break;
               }
            }

            game.map.tiles.remove(regiTile.position);
            newAction.append(new SetField(game, "playerCanMove", true, null));
         } else if (this.pedistalTile != null && game.battle.oppPokemon.specie.name.contains("regi")) {
            String text = "";
            if (this.pedistalTile.nameUpper.equals("REGICE")) {
               text = "Cold ... very cold ... too cold ... but ... even with heat ... it lasts forever ... ";
            } else if (this.pedistalTile.nameUpper.equals("REGIROCK")) {
               text = "Simple stones ... such a creation ... each one so unique ... but only stones ... even in the head ... ";
            } else if (this.pedistalTile.nameUpper.equals("REGISTEEL")) {
               text = "Metals ... from underground ... so tough ... so flexible ... yet it' so old ... it has an unearthly presence ... ";
            } else if (this.pedistalTile.nameUpper.equals("REGIDRAGO")) {
               text = "Such a sad sight ... my child ... its my fault ... incomplete ... just a dragon' head ... but you are still ... so strong ... ";
            } else if (this.pedistalTile.nameUpper.equals("REGIELEKI")) {
               text = "Such a sad sight ... my child ... your electricity ... restrained ... but ... you are still ... so strong ... ";
            }

            this.pedistalTile.nameUpper = "";
            this.pedistalTile.items().remove(game.battle.oppPokemon.specie.name.toUpperCase(Locale.ROOT));
            this.pedistalTile.init();
            if (!this.pedistalTile.items().isEmpty()) {
               Texture texture = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_regipedistal1.png"));
               this.pedistalTile.overSprite = new SmolSprite(texture, 0, 0, 16, 16);
               this.pedistalTile.overSprite.setPosition(this.pedistalTile.position.x, this.pedistalTile.position.y);
               text = text + "I desire more ...";
               newAction.append(new DisplayText(game, text, null, null, true, new RegigigasOutroAnim(null)));
            } else {
               Tile regiTile = null;

               for (Tile tile : game.map.tiles.values()) {
                  if (tile.name.contains("cave1_regi2")) {
                     regiTile = tile;
                     break;
                  }
               }

               Texture texture = TextureCache.get(Gdx.files.internal("tiles/cave1/cave1_floor2.png"));
               this.pedistalTile.sprite = new SmolSprite(texture, 0, 0, 16, 16);
               this.pedistalTile.sprite.setPosition(this.pedistalTile.position.x, this.pedistalTile.position.y);
               this.pedistalTile.name = "cave1_floor2";
               this.pedistalTile.isSolid = false;
               text = text + "My work is ... complete...";
               newAction.append(new DisplayText(game, text, null, null, true, new SetField(regiTile, "name", "cave1_regi3", null)));
            }

            newAction.append(new SetField(game.musicController, "resumeOverworldMusic", true, null));
         }

         if (this.ballUsed.equals("heal ball")) {
            newAction.append(
               game.battle.oppPokemon.new SetStat("hp", game.battle.oppPokemon.maxStats.get("hp"), new SetField(game.battle.oppPokemon, "status", null, null))
            );
         }

         newAction.append(new WaitFrames(game, 6, new SetField(game, "playerCanMove", true, null)));
         game.insertAction(newAction);
         newAction.step(game);
         game.actionStack.remove(this);
      }
   }
}
