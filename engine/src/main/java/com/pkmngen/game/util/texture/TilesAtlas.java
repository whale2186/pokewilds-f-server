package com.pkmngen.game.util.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.Page;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.pkmngen.game.Game;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TilesAtlas {
   private static final PixmapPacker packer = new PixmapPacker(2048, 2048, Format.RGBA8888, 2, true);
   private static final TextureAtlas atlas = new TextureAtlas();
   private static final List<Texture> atlasTextures = new ArrayList<>();
   private static final List<Pixmap> atlasPixmaps = new ArrayList<>();

   public static AtlasRegion get(String prefix, String name) {
      return atlas.findRegion(prefix + name);
   }

   public static AtlasRegion get(String prefix, String name, int index) {
      return atlas.findRegion(prefix + name, index);
   }

   public static Rectangle pack(String prefix, String name, Pixmap pixmap) {
      return packer.pack(prefix + name, pixmap);
   }

   public static Rectangle pack(String prefix, String name, int index, Pixmap pixmap) {
      return packer.pack(prefix + name + "_" + index, pixmap);
   }

   public static void update() {
      Runnable runnable = new Runnable() {
         public void done() {
            synchronized (this) {
               this.notify();
            }
         }

         @Override
         public void run() {
            TilesAtlas.packer.updateTextureAtlas(TilesAtlas.atlas, TextureFilter.Nearest, TextureFilter.Nearest, false);
            Array<Page> updatedPages = TilesAtlas.packer.getPages();
            if (updatedPages.size == TilesAtlas.atlasTextures.size()) {
               this.done();
            } else {
               int firstUpdatedPageIndex = TilesAtlas.atlasTextures.size();

               for (int i = firstUpdatedPageIndex; i < updatedPages.size; i++) {
                  Page page = updatedPages.get(i);
                  TilesAtlas.atlasTextures.add(page.getTexture());
                  TilesAtlas.atlasPixmaps.add(page.getPixmap());
               }

               this.done();
            }
         }
      };
      if (Thread.currentThread() != Game.staticGame.gameThread) {
         try {
            synchronized (runnable) {
               Gdx.app.postRunnable(runnable);
               runnable.wait();
            }
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      } else {
         runnable.run();
      }
   }

   @Null
   public static Pixmap getAtlasPixmap(Texture texture) {
      for (int i = 0; i < atlasTextures.size(); i++) {
         if (atlasTextures.get(i) == texture) {
            return atlasPixmaps.get(i);
         }
      }

      return null;
   }

   public static void saveAtlasToFile() {
      try {
         new PixmapPackerIO().save(Gdx.files.local("generated-atlas.atlas"), packer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
