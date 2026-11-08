package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.pkmngen.game.util.audio.AudioLoader;

class PlaySound extends Action {
   Sound sound;
   float volume = 1.0F;
   int waitFrames = 0;

   public PlaySound(Pokemon pokemon, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.volume = 0.5F;
      String fileName = "mods/pokemon/" + pokemon.specie.name + "/cry.ogg";
      FileHandle file = Gdx.files.internal(fileName);
      if (!file.exists()) {
         String dexNumber = pokemon.dexNumber;
         if (pokemon.specie.name.equals("araichu")) {
            dexNumber = "raichu-alola";
            this.volume = 1.0F;
         } else if (pokemon.specie.name.equals("aexeggutor")) {
            dexNumber = "exeggutor-alola";
            this.volume = 1.0F;
         } else if (pokemon.specie.name.equals("amarowak")) {
            dexNumber = "marowak-alola";
            this.volume = 1.0F;
         }

         fileName = "pokemon/cries/" + dexNumber + ".ogg";
         file = Gdx.files.internal(fileName);
      }

      if (pokemon.isEgg) {
         this.sound = AudioLoader.loadSound("sounds/egg_noise1.ogg");
         this.volume = 1.0F;
         this.waitFrames = Specie.cryLengthInFrames("sounds/egg_noise1");
      } else if (pokemon.isGhost) {
         this.sound = AudioLoader.loadSound("pokemon/cries/000.ogg");
         this.volume = 0.5F;
         this.waitFrames = Specie.cryLengthInFrames("pokemon/cries/000");
      } else {
         this.sound = AudioLoader.loadSound(file.path());
         this.waitFrames = pokemon.specie.cryLengthInFrames();
      }
   }

   public PlaySound(String soundName, Action nextAction) {
      this(soundName, 1.0F, nextAction);
   }

   public PlaySound(String soundName, float volume, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.volume = volume;
      this.sound = null;
      if (!Game.staticGame.loadedSound.containsKey(soundName)) {
         Game.staticGame.loadedSound.put(soundName, AudioLoader.loadSound("sounds/" + soundName + ".ogg"));
      }

      this.sound = Game.staticGame.loadedSound.get(soundName);
   }

   @Override
   public void firstStep(Game game) {
      if (this.sound != null) {
         this.sound.play(this.volume);
      }

      game.insertAction(this.nextAction);
   }

   @Override
   public void step(Game game) {
      if (--this.waitFrames <= 0) {
         game.actionStack.remove(this);
         if (!Game.staticGame.loadedSound.containsValue(this.sound)) {
            this.sound.stop();
            this.sound.dispose();
         }
      }
   }
}
