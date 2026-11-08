package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;

class OverworldAnimation extends Action {
   public Action.Layer layer = Action.Layer.map_0;
   Vector2 position;
   String name;
   HashMap<Integer, String> metadata = new HashMap<>();
   Music sound;
   int frameNum = 1;
   int timer = 2;
   Texture currText;
   Sprite currFrame;
   boolean flip = false;
   boolean firstStep = true;

   public OverworldAnimation(Game game, String name, Vector2 position, boolean flip, Action nextAction) {
      super();
      this.name = name.toLowerCase(Locale.ROOT).replace(' ', '_');
      this.position = position;
      this.flip = flip;
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
   public void step(Game game) {
      if (this.firstStep) {
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
         } catch (GdxRuntimeException var10) {
         }

         this.firstStep = false;
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

      FileHandle filehandle = Gdx.files.internal("attacks/" + this.name + "/output/frame-" + String.format(Locale.ROOT, "%03d", this.frameNum) + ".png");
      if (!filehandle.exists()) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      } else {
         this.currText = TextureCache.get(filehandle);
         this.currFrame = new Sprite(this.currText, 0, 0, 160, 144);
         if (this.flip) {
            this.currFrame.flip(true, false);
         }

         game.mapBatch.draw(this.currFrame, this.position.x, this.position.y);
         if (this.metadata.containsKey(this.frameNum)) {
            String var14 = this.metadata.get(this.frameNum);
         }

         this.timer--;
         if (this.timer <= 0) {
            this.timer = 2;
            this.frameNum++;
         }
      }
   }
}
