package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.pkmngen.game.util.audio.AudioLoader;

class PlayMusic extends Action {
   Music music;
   String sound;
   boolean cached = false;
   boolean playedYet;

   public PlayMusic(Pokemon pokemon, Action nextAction) {
      this(pokemon, false, nextAction);
   }

   public PlayMusic(Pokemon pokemon, boolean cached, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.playedYet = false;
      this.sound = pokemon.specie.name;
      this.music = null;
      this.cached = cached;
      if (pokemon.isEgg) {
         this.music = AudioLoader.loadMusic("sounds/egg_noise1.ogg");
         this.music.setLooping(false);
      } else if (pokemon.generation == Pokemon.Generation.CRYSTAL) {
         float volume = 0.5F;
         FileHandle file = Gdx.files.local("mods/pokemon/" + pokemon.specie.name + "/cry.ogg");
         if (!file.exists()) {
            String dexNumber = pokemon.dexNumber;
            if (pokemon.specie.name.equals("araichu")) {
               dexNumber = "raichu-alola";
               volume = 1.0F;
            } else if (pokemon.specie.name.equals("aexeggutor")) {
               dexNumber = "exeggutor-alola";
               volume = 1.0F;
            } else if (pokemon.specie.name.equals("amarowak")) {
               dexNumber = "marowak-alola";
               volume = 1.0F;
            }

            file = Gdx.files.internal("pokemon/cries/" + dexNumber + ".ogg");
         }

         if (cached) {
            if (!Game.staticGame.loadedMusic.containsKey(this.sound)) {
               Game.staticGame.loadedMusic.put(this.sound, AudioLoader.loadMusic(file.path()));
            }

            this.music = Game.staticGame.loadedMusic.get(this.sound);
            this.music.stop();
            this.music.setVolume(volume);
            this.music.setLooping(false);
            return;
         }

         if (pokemon.isGhost) {
            this.music = AudioLoader.loadMusic("pokemon/cries/000.ogg");
            this.music.setVolume(0.5F);
         } else {
            this.music = AudioLoader.loadMusic(file.path());
            this.music.setVolume(volume);
         }

         this.music.setLooping(false);
      }
   }

   public PlayMusic(String sound, Action nextAction) {
      this(sound, 1.0F, nextAction);
   }

   public PlayMusic(String sound, float volume, Action nextAction) {
      this(sound, volume, false, nextAction);
   }

   public PlayMusic(String sound, float volume, boolean cached, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.playedYet = false;
      this.sound = sound;
      this.music = null;
      this.cached = cached;
      if (cached) {
         if (!Game.staticGame.loadedMusic.containsKey(sound)) {
            Game.staticGame.loadedMusic.put(sound, AudioLoader.loadMusic("sounds/" + sound + ".ogg"));
         }

         this.music = Game.staticGame.loadedMusic.get(sound);
         this.music.stop();
         this.music.setVolume(volume);
         this.music.setLooping(false);
      } else {
         this.music = AudioLoader.loadMusic("sounds/" + sound + ".ogg");
         this.music.setLooping(false);
         this.music.setVolume(volume);
      }
   }

   @Override
   public void step(Game game) {
      if (this.sound.equals("mgengar_battle1")) {
         game.loadedMusic.get("mgengar_battle1").play();
         game.currMusic = game.loadedMusic.get("mgengar_battle1");
      } else if (this.music == null) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      } else {
         if (!this.playedYet) {
            this.music.play();
            this.playedYet = true;
         }

         if (!this.music.isPlaying()) {
            if (!this.cached) {
               this.music.dispose();
            }

            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }
}
