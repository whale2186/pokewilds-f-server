package com.pkmngen.game;

import com.badlogic.gdx.audio.Music;
import com.pkmngen.game.util.audio.AudioLoader;

class RegigigasOutroAnim extends Action {
   int timer = 0;
   int phase = 0;
   Tile regiTile;
   Tile pedistalTile;
   Music soundEffect;
   Music soundEffect2;

   public RegigigasOutroAnim(Action nextAction) {
      super();
      this.nextAction = nextAction;
   }

   @Override
   public void firstStep(Game game) {
      for (Tile tile : game.map.tiles.values()) {
         if (tile.name.equals("cave1_regi2")) {
            this.regiTile = tile;
            break;
         }
      }

      this.soundEffect2 = AudioLoader.loadMusic("sounds/splash1.ogg");
      this.soundEffect2.setLooping(false);
      this.soundEffect2.setVolume(0.3F);
      this.soundEffect = AudioLoader.loadMusic("sounds/stomp1.ogg");
      this.soundEffect.setLooping(true);
      this.soundEffect.setVolume(0.7F);
   }

   @Override
   public void step(Game game) {
      if (this.phase == 0) {
         if (this.timer == 0) {
            this.soundEffect.play();
         }

         int step = 40;
         if (this.timer < step * 8) {
            if (this.timer % 4 == 0) {
               game.cam.translate(0.0F, 2.0F);
               game.player.position.add(0.0F, 2.0F);
               this.regiTile.sprite.translateY(2.0F);
            } else if (this.timer % 4 == 2) {
               game.cam.translate(0.0F, -2.0F);
               game.player.position.add(0.0F, -2.0F);
               this.regiTile.sprite.translateY(-2.0F);
            }
         }

         if (this.timer == step * 2) {
            this.regiTile.sprite.setRegion(96, 0, 32, 32);
         } else if (this.timer == step * 4) {
            this.regiTile.sprite.setRegion(64, 0, 32, 32);
         } else if (this.timer == step * 6) {
            this.regiTile.sprite.setRegion(32, 0, 32, 32);
         } else if (this.timer == step * 8) {
            this.regiTile.sprite.setRegion(0, 0, 32, 32);
            this.soundEffect.stop();
         } else if (this.timer == step * 9) {
            this.phase = 1;
            this.timer = 0;
         }
      } else if (this.phase == 1) {
         this.regiTile.name = "cave1_regi1";
         this.soundEffect.dispose();
         this.soundEffect2.stop();
         this.soundEffect2.dispose();
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      this.timer++;
   }
}
