package com.pkmngen.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.pkmngen.game.Game;
import com.pkmngen.game.util.texture.StaticTextures;
import java.util.HashMap;

public class TextureCache {
   public static HashMap<FileHandle, Texture> textMap = new HashMap<>();
   public static HashMap<Texture, Texture[]> effectsTextMap = new HashMap<>();
   public static HashMap<Texture, Color[]> colorsTextMap = new HashMap<>();
   public static Texture currTexture;
   public static HashMap<String, Texture> eggTextures = new HashMap<>();
   public static SpriteProxy maleSymbol;
   public static SpriteProxy femaleSymbol;
   public static SpriteProxy shinySymbol;
   public static SpriteProxy maleSymbolInverse;
   public static SpriteProxy femaleSymbolInverse;
   public static SpriteProxy shinySymbolInverse;

   public static Texture get(final Pixmap pixmap) {
      if (Thread.currentThread() != Game.staticGame.gameThread) {
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
               try {
                  TextureCache.currTexture = new Texture(pixmap);
               } catch (Exception e) {
                  e.printStackTrace();
               }

               synchronized (this) {
                  this.notify();
               }
            }
         };
         try {
            synchronized (runnable) {
               Gdx.app.postRunnable(runnable);
               runnable.wait();
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      } else {
         currTexture = new Texture(pixmap);
      }

      return currTexture;
   }

   public static SmolSprite getTileSprite(String name, int srcX, int srcY, int width, int height) {
      SmolSprite sprite = getTileSprite(name);
      sprite.setRegion(srcX, srcY, width, height);
      return sprite;
   }

   public static SmolSprite getTileSprite(String name, int width, int height) {
      return getTileSprite(name, 0, 0, width, height);
   }

   public static SmolSprite getTileSprite(String name) {
      AtlasRegion region = StaticTextures.getOrCreate(name);
      return new SmolSprite(region);
   }

   public static Texture get(final FileHandle file) {
      if (!textMap.containsKey(file)) {
         if (Thread.currentThread() != Game.staticGame.gameThread) {
            Runnable runnable = new Runnable() {
               @Override
               public void run() {
                  try {
                     TextureCache.textMap.put(file, new Texture(file));
                  } catch (Exception e) {
                     e.printStackTrace();
                  }

                  synchronized (this) {
                     this.notify();
                  }
               }
            };
            try {
               synchronized (runnable) {
                  Gdx.app.postRunnable(runnable);
                  runnable.wait();
               }
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         } else {
            FileHandle moddedFile = Gdx.files.local("mods/" + file.path());

            try {
               if (moddedFile.exists()) {
                  textMap.put(file, new Texture(moddedFile));
               } else {
                  textMap.put(file, new Texture(file));
               }
            } catch (Exception e) {
               e.printStackTrace();
               moddedFile = Gdx.files.internal("tiles/blank3.png");
               textMap.put(file, new Texture(moddedFile));
            }
         }
      }

      return textMap.get(file);
   }

   static {
      Texture text = new Texture(Gdx.files.internal("male_symbol1.png"));
      maleSymbol = new SpriteProxy(Color.WHITE, text, 0, 0, 8, 8);
      text = new Texture(Gdx.files.internal("female_symbol1.png"));
      femaleSymbol = new SpriteProxy(Color.WHITE, text, 0, 0, 8, 8);
      text = new Texture(Gdx.files.internal("shiny.png"));
      shinySymbol = new SpriteProxy(Color.WHITE, text, 0, 0, 8, 8);
      text = new Texture(Gdx.files.internal("male_symbol1_inverse.png"));
      maleSymbolInverse = new SpriteProxy(Color.BLACK, text, 0, 0, 8, 8);
      text = new Texture(Gdx.files.internal("female_symbol1_inverse.png"));
      femaleSymbolInverse = new SpriteProxy(Color.BLACK, text, 0, 0, 8, 8);
      text = new Texture(Gdx.files.internal("shiny_inverse.png"));
      shinySymbolInverse = new SpriteProxy(Color.BLACK, text, 0, 0, 8, 8);
   }
}
