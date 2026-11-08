package com.pkmngen.game.util.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.pkmngen.game.Game;

public class AudioLoader {
   public static Music loadMusic(String name) {
      return Gdx.audio.newMusic(Game.checkForMods(name));
   }

   public static Sound loadSound(String name) {
      return Gdx.audio.newSound(Game.checkForMods(name));
   }
}
