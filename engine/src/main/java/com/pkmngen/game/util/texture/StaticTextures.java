package com.pkmngen.game.util.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import java.util.HashMap;
import java.util.Map;

public class StaticTextures {
   private static final String PREFIX = "static_";
   private static final Map<String, AtlasRegion> tilesCache = new HashMap<>();

   public static AtlasRegion getOrCreate(String name) {
      if (tilesCache.containsKey(name)) {
         return tilesCache.get(name);
      }

      FileHandle file = Gdx.files.local("mods/tiles/" + name + ".png");
      if (!file.exists()) {
         file = Gdx.files.internal("tiles/" + name + ".png");
      }

      Pixmap pixmap = new Pixmap(file);
      TilesAtlas.pack("static_", name, pixmap);
      TilesAtlas.update();
      pixmap.dispose();
      AtlasRegion region = TilesAtlas.get("static_", name);
      tilesCache.put(name, region);
      return region;
   }
}
