package com.pkmngen.game.util;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.pkmngen.game.util.audio.AudioLoader;
import gme.VGMPlayer;

public class LinkedMusic implements Music {
   Music music1;
   Music music2;
   Music currMusic;
   public static VGMPlayer gbsPlayer = new VGMPlayer(44100);
   public LinkedMusic.Type type = LinkedMusic.Type.NORMAL;

   public LinkedMusic(String m1, String m2) {
      if (!m1.contains("pkmnmansion1") && !m2.contains("pkmnmansion1")) {
         this.music1 = AudioLoader.loadMusic(m1 + ".ogg");
         this.music1.setLooping(false);
         this.currMusic = this.music1;
         if (!m2.equals("")) {
            this.music1.setOnCompletionListener(new OnCompletionListener() {
               @Override
               public void onCompletion(Music music) {
                  LinkedMusic.this.music2.play();
                  LinkedMusic.this.currMusic = LinkedMusic.this.music2;
               }
            });
            this.music2 = AudioLoader.loadMusic(m2 + ".ogg");
            this.music2.setLooping(true);
            this.music2.play();
            this.music2.pause();
         }
      } else {
         this.type = LinkedMusic.Type.GME;

         try {
            gbsPlayer.loadFile("DMG-APEE-USA.gbs", "DMG-APEE-USA.gbs");
            gbsPlayer.startTrack(38, 100);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   @Override
   public void play() {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            if (!gbsPlayer.isPlaying()) {
               gbsPlayer.play();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         this.currMusic.play();
      }
   }

   @Override
   public void pause() {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            if (gbsPlayer.isPlaying()) {
               gbsPlayer.pause();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         this.currMusic.pause();
      }
   }

   @Override
   public void stop() {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            gbsPlayer.stop();
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         if (this.music2 != null) {
            this.music2.stop();
         }

         this.music1.stop();
         this.currMusic = this.music1;
      }
   }

   @Override
   public boolean isPlaying() {
      return this.type == LinkedMusic.Type.GME ? gbsPlayer.isPlaying() : this.currMusic.isPlaying();
   }

   @Override
   public void setLooping(boolean isLooping) {
      if (this.type == LinkedMusic.Type.GME) {
         System.out.println("hi");
      } else {
         this.currMusic.setLooping(isLooping);
      }
   }

   @Override
   public boolean isLooping() {
      return this.type == LinkedMusic.Type.GME ? true : this.currMusic.isLooping();
   }

   @Override
   public void setVolume(float volume) {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            gbsPlayer.setVolume(volume);
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         if (this.music2 != null) {
            this.music2.setVolume(volume);
         }

         this.music1.setVolume(volume);
      }
   }

   @Override
   public float getVolume() {
      return this.type == LinkedMusic.Type.GME ? (float)gbsPlayer.getVolume() : this.currMusic.getVolume();
   }

   @Override
   public void setPan(float pan, float volume) {
      if (this.type != LinkedMusic.Type.GME) {
         this.currMusic.setPan(pan, volume);
      }
   }

   @Override
   public void setPosition(float position) {
      if (this.type != LinkedMusic.Type.GME) {
         this.currMusic.setPosition(position);
      }
   }

   @Override
   public float getPosition() {
      return this.type == LinkedMusic.Type.GME ? 0.0F : this.currMusic.getPosition();
   }

   @Override
   public void dispose() {
      if (this.type != LinkedMusic.Type.GME) {
         this.music1.dispose();
         if (this.music2 != null) {
            this.music2.dispose();
         }
      }
   }

   @Override
   public void setOnCompletionListener(OnCompletionListener listener) {
      if (this.type != LinkedMusic.Type.GME) {
         this.currMusic.setOnCompletionListener(listener);
      }
   }

   public enum Type {
      NORMAL,
      GME;
   }
}
