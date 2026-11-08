package com.pkmngen.game;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.pkmngen.game.util.LinkedMusic;
import com.pkmngen.game.util.audio.AudioLoader;

class MusicController extends Action {
   boolean firstStep = true;
   public String startBattle = null;
   public boolean battleVictoryFanfare = false;
   public boolean inBattle = false;
   public boolean battleFadeOut = false;
   public boolean playerFainted = false;
   public String startTimeOfDay = null;
   public boolean unownMusic = false;
   public String currOverworldMusic = null;
   public String startOverworldMusic = null;
   public String startDungeonMusic = null;
   public boolean fadeToDungeon = false;
   public boolean resumeOverworldMusic = false;
   public boolean inTransition = false;
   public boolean startEvolveMusic = false;
   public boolean evolving = false;
   public boolean evolveMusicFadeout = false;
   public String startNightAlert = null;
   public boolean nightAlert = false;
   public boolean teleporting = false;
   String currDayTime = null;
   OnCompletionListener musicCompletionListener;

   public MusicController(Game game) {
      super();
      game.musicController = this;
   }

   @Override
   public void firstStep(Game game) {
      this.startTimeOfDay = game.map.timeOfDay;
   }

   public boolean isBiome(Game game) {
      boolean isDesert = game.map.currRoute != null && game.map.currRoute.type().equals("desert");
      boolean isGraveyard = game.map.currRoute != null && game.map.currRoute.type().equals("graveyard");
      boolean isVolcano = game.map.currRoute != null && game.map.currRoute.type().equals("volcano");
      boolean isDeepForest = game.map.currRoute != null && game.map.currRoute.type().equals("deep_forest");
      return isDesert || isGraveyard || isVolcano || isDeepForest;
   }

   @Override
   public void step(Game game) {
      if (game.type != Game.Type.SERVER && !game.cinematic && !Game.musicDisabled) {
         boolean isDungeon = game.map.currRoute != null && game.map.currRoute.isDungeon;
         if (this.startBattle != null && !this.inBattle) {
            System.out.println("startBattle");
            if (isDungeon || !this.currDayTime.equals("night") && !this.unownMusic) {
               game.currMusic.pause();
               if (this.startBattle.equals("wild")) {
                  game.currMusic = game.battle.music;
                  game.currMusic.stop();
               } else {
                  if (!game.loadedMusic.containsKey(this.startBattle)) {
                     game.loadedMusic.put(this.startBattle, new LinkedMusic("music/" + this.startBattle + "_intro", "music/" + this.startBattle));
                  }

                  game.currMusic = game.loadedMusic.get(this.startBattle);
                  game.currMusic.stop();
                  game.currMusic.setVolume(0.4F);
                  if (this.startBattle.contains("regi")) {
                     game.currMusic.setVolume(0.3F);
                  } else if (this.startBattle.equals("bw_legendary_theme3")) {
                     game.currMusic.setVolume(0.6F);
                  }
               }

               game.currMusic.play();
            }

            this.inBattle = true;
            this.startBattle = null;
         }

         if (this.battleVictoryFanfare && this.inBattle) {
            if (!SpecialMewtwo1.class.isInstance(game.battle.oppPokemon) && !this.unownMusic) {
               game.currMusic.pause();
               game.currMusic = game.battle.victoryFanfare;
               game.currMusic.stop();
               game.currMusic.play();
            }

            this.battleVictoryFanfare = false;
         }

         if (this.battleFadeOut && this.inBattle) {
            System.out.println("battleFadeOut");
            if (isDungeon || !this.currDayTime.equals("night") || game.map.timeOfDay.equals("day") || this.playerFainted || this.unownMusic) {
               float currVol = game.currMusic.getVolume();
               currVol = 0.004F * (currVol / 0.3F);
               System.out.println(currVol);
               game.insertAction(new FadeMusic(game.currMusic, -currVol, null));
            }

            this.unownMusic = false;
            this.battleFadeOut = false;
         }

         if (this.startEvolveMusic) {
            game.currMusic.pause();
            game.currMusic = AudioLoader.loadMusic("sounds/evolution1.ogg");
            game.currMusic.play();
            this.evolving = true;
            this.startEvolveMusic = false;
         }

         if (this.evolveMusicFadeout && this.evolving) {
            game.insertAction(
               new FadeMusic(
                  game.currMusic,
                  -0.02F,
                  new CallMethod(
                     game.currMusic,
                     "stop",
                     new Object[0],
                     new CallMethod(
                        game.currMusic, "dispose", new Object[0], new SetField(this, "evolving", false, new SetField(this, "resumeOverworldMusic", true, null))
                     )
                  )
               )
            );
            this.evolveMusicFadeout = false;
         }

         if (this.resumeOverworldMusic && !this.inBattle && (!this.nightAlert || this.teleporting)) {
            System.out.println("resume overworld music");
            System.out.println(game.map.timeOfDay.equals("night"));
            System.out.println(this.inBattle);
            game.currMusic.pause();
            boolean isDesert = game.map.currRoute != null && game.map.currRoute.type().equals("desert") && !this.currDayTime.equals("night");
            boolean isGraveyard = game.map.currRoute != null && game.map.currRoute.type().equals("graveyard") && !this.currDayTime.equals("night");
            boolean isVolcano = game.map.currRoute != null && game.map.currRoute.type().equals("volcano") && !this.currDayTime.equals("night");
            boolean isDeepForest = game.map.currRoute != null && game.map.currRoute.type().equals("deep_forest") && !this.currDayTime.equals("night");
            if (isDungeon || isDesert || isGraveyard || isVolcano || isDeepForest) {
               String musicName = game.map.currRoute.getNextMusic(true);
               if (!game.loadedMusic.containsKey(musicName)) {
                  Music music;
                  if (isDesert) {
                     music = new LinkedMusic("music/" + musicName + "_intro", "music/" + musicName);
                  } else {
                     music = AudioLoader.loadMusic("music/" + musicName + ".ogg");
                     music.setLooping(true);
                  }

                  game.loadedMusic.put(musicName, music);
               }

               game.currMusic = game.loadedMusic.get(musicName);
               game.currMusic.stop();
               if (musicName.contains("sealed_chamber")) {
                  game.currMusic.setVolume(0.25F);
               }
            } else if (this.startTimeOfDay == null) {
               System.out.println(this.currOverworldMusic);
               System.out.println(game.currMusic.getVolume());
               String overworldMusic = this.currOverworldMusic;
               if (this.teleporting && this.nightAlert) {
                  overworldMusic = "night1_chase1";
               }

               if (!game.loadedMusic.containsKey(overworldMusic)) {
                  game.loadedMusic.put(overworldMusic, AudioLoader.loadMusic("music/" + overworldMusic + ".ogg"));
               }

               game.currMusic = game.loadedMusic.get(overworldMusic);
            }

            if (!game.currMusic.isPlaying()) {
               game.currMusic.play();
            }

            this.resumeOverworldMusic = false;
            this.playerFainted = false;
            this.teleporting = false;
         }

         if (this.fadeToDungeon && !this.inBattle) {
            String nextMusicName = this.currOverworldMusic;
            if (isDungeon || nextMusicName.contains("night")) {
               nextMusicName = game.map.currRoute.getNextMusic(true);
            }

            float rate = game.currMusic.getVolume() * -0.025F;
            Action action = new FadeMusic(game.currMusic, rate, null);
            action.append(new SetField(this, "resumeOverworldMusic", true, null));
            game.insertAction(action);
            if (!game.map.timeOfDay.equals("night")
               && this.currOverworldMusic.contains("night")
               && !this.isBiome(game)
               && game.map.currRoute != null
               && !game.map.currRoute.isDungeon) {
               this.currOverworldMusic = game.map.currRoute.getNextMusic(true);
               System.out.println("this.currOverworldMusic");
               System.out.println(this.currOverworldMusic);
            }

            FadeMusic.currFadeMusic = null;
            this.fadeToDungeon = false;
         }

         if (this.startNightAlert != null) {
            game.currMusic.pause();
            if (!game.loadedMusic.containsKey(this.startNightAlert)) {
               game.loadedMusic.put(this.startNightAlert, AudioLoader.loadMusic("music/" + this.startNightAlert + ".ogg"));
            }

            Music music = game.loadedMusic.get(this.startNightAlert);
            music.stop();
            music.setLooping(true);
            game.currMusic = music;
            game.currMusic.play();
            this.nightAlert = true;
            this.startNightAlert = null;
         }

         if (this.startOverworldMusic != null && !this.inBattle && !this.evolving) {
            System.out.println("startOverworldMusic");
            System.out.println(this.startOverworldMusic);
            if (!isDungeon && !this.currDayTime.equals("night") && !this.unownMusic) {
               game.currMusic.pause();
               if (!game.loadedMusic.containsKey(this.startOverworldMusic)) {
                  Music music;
                  if (this.startOverworldMusic.equals("route_111-2")) {
                     music = new LinkedMusic("music/" + this.startOverworldMusic + "_intro", "music/" + this.startOverworldMusic);
                  } else {
                     music = AudioLoader.loadMusic("music/" + this.startOverworldMusic + ".ogg");
                     music.setOnCompletionListener(this.musicCompletionListener);
                  }

                  game.loadedMusic.put(this.startOverworldMusic, music);
               }

               game.currMusic = game.loadedMusic.get(this.startOverworldMusic);
               if (!this.isBiome(game)) {
                  this.currOverworldMusic = this.startOverworldMusic;
               }

               game.currMusic.play();
            }

            this.startOverworldMusic = null;
         }

         if (this.startTimeOfDay != null && !this.inBattle && !this.playerFainted && !this.unownMusic && !this.evolving && !isDungeon) {
            this.currDayTime = this.startTimeOfDay;
            System.out.println("startTimeOfDay");
            System.out.println(this.startTimeOfDay);
            if (this.startTimeOfDay.equals("day")) {
               this.musicCompletionListener = new OnCompletionListener() {
                  @Override
                  public void onCompletion(Music aMusic) {
                     String nextMusicName = Game.staticGame.map.currRoute.getNextMusic(true);
                     Game.staticGame.loadedMusic.remove(MusicController.this.currOverworldMusic);
                     Action nextMusic = new FadeMusic(
                        aMusic,
                        -0.0125F,
                        new WaitFrames(
                           Game.staticGame,
                           360,
                           new SetField(
                              Game.staticGame.musicController, "startOverworldMusic", nextMusicName, new CallMethod(aMusic, "dispose", new Object[0], null)
                           )
                        )
                     );
                     Game.staticGame.insertAction(nextMusic);
                     System.out.println("loaded musics");

                     for (String name : Game.staticGame.loadedMusic.keySet()) {
                        System.out.println(name);
                     }
                  }
               };
               if (!this.firstStep) {
                  String nextMusicName = game.map.currRoute.getNextMusic(true);
                  Action nextMusic = new FadeMusic(
                     game.currMusic,
                     -0.025F,
                     new WaitFrames(
                        game,
                        360,
                        new SetField(
                           game.musicController, "startOverworldMusic", nextMusicName, new SetField(game.musicController, "inTransition", false, null)
                        )
                     )
                  );
                  game.insertAction(nextMusic);
                  if (!this.isBiome(game)) {
                     this.currOverworldMusic = nextMusicName;
                  }
               } else {
                  this.currOverworldMusic = "nature1_render";
                  Tile playerTile = game.map.tiles.get(game.player.position);
                  float position = 130.0F;
                  float volume = 1.0F;
                  if (playerTile != null
                     && (
                        playerTile.name.contains("sand")
                           || playerTile.routeBelongsTo != null && playerTile.routeBelongsTo.name.contains("beach")
                           || playerTile.biome.contains("beach")
                     )) {
                     this.currOverworldMusic = "waves1";
                     position = 0.0F;
                     volume = 1.0F;
                  }

                  if (playerTile != null
                     && (
                        playerTile.name.contains("volcano")
                           || playerTile.routeBelongsTo != null && playerTile.routeBelongsTo.name.contains("volcano")
                           || playerTile.biome.contains("volcano")
                     )) {
                     this.currOverworldMusic = "ambient_rumbling";
                     position = 0.0F;
                     volume = 1.0F;
                  }

                  game.currMusic = AudioLoader.loadMusic("music/" + this.currOverworldMusic + ".ogg");
                  if (!game.loadedMusic.containsKey(this.currOverworldMusic)) {
                     game.loadedMusic.put(this.currOverworldMusic, game.currMusic);
                  }

                  game.currMusic.setLooping(false);
                  game.currMusic.setVolume(volume);
                  game.currMusic.play();
                  game.currMusic.pause();
                  game.currMusic.setPosition(position);
                  game.currMusic.play();
                  game.currMusic.setOnCompletionListener(this.musicCompletionListener);
               }
            } else {
               System.out.println("start night music");
               game.currMusic.stop();
               if (!game.loadedMusic.containsKey("night1")) {
                  game.loadedMusic.put("night1", AudioLoader.loadMusic("music/night1.ogg"));
               }

               Music music = game.loadedMusic.get("night1");
               music.setLooping(true);
               game.currMusic = music;
               this.currOverworldMusic = "night1";
               game.currMusic.play();
            }

            this.startTimeOfDay = null;
         }

         this.firstStep = false;
      }
   }
}
