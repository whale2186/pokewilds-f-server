package com.pkmngen.game;

import com.badlogic.gdx.audio.Music;
import com.pkmngen.game.util.audio.AudioLoader;

class FadeMusic extends Action {
   Music music;
   float rate = -0.05F;
   float maxVol = 1.0F;
   public static Action currFadeMusic;
   public boolean insertNextAction = false;
   public static Music silence;

   public FadeMusic(Music music, float rate, Action nextAction) {
      this(music, rate, 1.0F, nextAction);
   }

   public FadeMusic(Music music, float rate, float maxVol, Action nextAction) {
      this(music, rate, 1.0F, false, nextAction);
   }

   public FadeMusic(Music music, float rate, float maxVol, boolean insertNextAction, Action nextAction) {
      super();
      silence = AudioLoader.loadMusic("music/silence1.ogg");
      silence.setVolume(0.0F);
      silence.setLooping(true);
      this.music = music;
      this.rate = rate;
      this.maxVol = maxVol;
      this.insertNextAction = insertNextAction;
      this.nextAction = nextAction;
   }

   @Override
   public void firstStep(Game game) {
      currFadeMusic = this;
      if (this.music == null) {
         this.music = silence;
      }
   }

   @Override
   public void step(Game game) {
      if (currFadeMusic == this) {
         float volume = this.music.getVolume() + this.rate;
         if (volume < 0.0F) {
            volume = 0.0F;
         }

         this.music.setVolume(volume);
      }

      if (currFadeMusic != this || this.music.getVolume() <= 0.1F || this.music.getVolume() >= this.maxVol) {
         if (this.music.getVolume() < this.maxVol) {
            this.music.pause();
         }

         this.music.setVolume(this.maxVol);
         game.actionStack.remove(this);
         if (currFadeMusic == this || this.insertNextAction) {
            game.insertAction(this.nextAction);
         }
      }
   }
}
