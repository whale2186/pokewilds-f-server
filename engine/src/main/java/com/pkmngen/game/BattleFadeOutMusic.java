package com.pkmngen.game;

import com.badlogic.gdx.audio.Music;
import java.util.ArrayList;

class BattleFadeOutMusic extends Action {
   ArrayList<Float> frames;
   Float frame;
   float originalVolume;
   Music music;
   String timeOfDay;
   public static boolean playerFainted = false;
   public static boolean stop = false;
   public Action.Layer layer = Action.Layer.gui_129;

   public BattleFadeOutMusic(Game game, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.frames = new ArrayList<>();

      for (int i = 0; i < 14; i++) {
         this.frames.add(0.3F);
      }

      for (int i = 0; i < 14; i++) {
         this.frames.add(0.25F);
      }

      for (int i = 0; i < 14; i++) {
         this.frames.add(0.2F);
      }

      for (int i = 0; i < 14; i++) {
         this.frames.add(0.15F);
      }

      for (int i = 0; i < 14; i++) {
         this.frames.add(0.1F);
      }

      for (int i = 0; i < 7; i++) {
         this.frames.add(0.05F);
      }

      for (int i = 0; i < 7; i++) {
         this.frames.add(0.025F);
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
      this.music = game.currMusic;
      this.timeOfDay = game.map.timeOfDay;
      game.musicController.battleFadeOut = true;
   }

   @Override
   public void step(Game game) {
      if (this.frames.isEmpty()) {
         game.musicController.inBattle = false;
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      } else {
         this.frame = this.frames.get(0);
         this.frames.remove(0);
      }
   }

   public void oldStep(Game game) {
      if (game.map.timeOfDay.equals("night") && !playerFainted) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      } else {
         if (this.firstStep) {
            this.originalVolume = game.currMusic.getVolume();
            this.music = game.currMusic;
            stop = false;
            this.firstStep = false;
         }

         if (!this.frames.isEmpty() && !stop) {
            this.frame = this.frames.get(0);
            this.music.setVolume(this.frame);
            this.frames.remove(0);
         } else {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
            if (!stop) {
               this.music.stop();
            }

            this.music.setVolume(this.originalVolume);
            if (game.battle.drawAction == null && !playerFainted) {
               game.currMusic = game.map.currRoute.music;
               game.currMusic.play();
            }

            playerFainted = false;
            stop = false;
         }
      }
   }
}
