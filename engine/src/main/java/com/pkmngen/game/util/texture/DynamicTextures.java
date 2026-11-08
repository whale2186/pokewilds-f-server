package com.pkmngen.game.util.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.SmolSprite;
import com.pkmngen.game.util.SmolSpriteProxy;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DynamicTextures {
   private static final String PREFIX = "dynamic_";
   private static final Set<String> addedTiles = new HashSet<>();
   public static final int SHORELINES_COUNT = 7;
   public static final int WATER_HOLE_COUNT = 4;
   public static final int CHIMNEY_COUNT = 4;
   private static Map<String, AtlasRegion> texturesCache = new HashMap<>();

   public static AtlasRegion get(String name) {
      String key = cacheKey(name);
      if (!texturesCache.containsKey(key)) {
         texturesCache.put(key, TilesAtlas.get("dynamic_", name));
      }

      return texturesCache.get(key);
   }

   public static TextureRegion get(String name, int index) {
      String key = cacheKey(name, index);
      if (!texturesCache.containsKey(key)) {
         texturesCache.put(key, TilesAtlas.get("dynamic_", name, index));
      }

      return texturesCache.get(key);
   }

   private static void addToLocalCache(String name) {
      texturesCache.put(cacheKey(name), TilesAtlas.get("dynamic_", name));
   }

   private static void addToLocalCache(String name, int index) {
      texturesCache.put(cacheKey(name, index), TilesAtlas.get("dynamic_", name, index));
   }

   private static String cacheKey(String name) {
      return cacheKey(name, -1);
   }

   private static String cacheKey(String name, int index) {
      return name + "_" + index;
   }

   private static Rectangle pack(String name, Pixmap pixmap) {
      return TilesAtlas.pack("dynamic_", name, pixmap);
   }

   private static Rectangle pack(String name, int index, Pixmap pixmap) {
      return TilesAtlas.pack("dynamic_", name, index, pixmap);
   }

   private static void updateTextureAtlas() {
      TilesAtlas.update();
   }

   public static void cacheShorelines(String shoreType) {
      if (!addedTiles.contains(shoreType)) {
         Texture text = TextureCache.get(Gdx.files.internal("tiles/shore_sheet4.png"));
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap outerPixmap = temp.consumePixmap();
         text = TextureCache.get(Gdx.files.internal("tiles/shore1inner_sheet2.png"));
         temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap innerPixmap = temp.consumePixmap();
         Vector2 quadNE = new Vector2(24.0F, 24.0F);
         if (shoreType.contains("[E]")) {
            quadNE.add(16.0F, 0.0F);
         }

         if (shoreType.contains("[N]")) {
            quadNE.add(0.0F, 16.0F);
         }

         if (shoreType.contains("[NE]") && !shoreType.contains("[N]") && !shoreType.contains("[E]")) {
            quadNE = null;
         }

         Vector2 quadNW = new Vector2(16.0F, 24.0F);
         if (shoreType.contains("[W]")) {
            quadNW.add(-16.0F, 0.0F);
         }

         if (shoreType.contains("[N]")) {
            quadNW.add(0.0F, 16.0F);
         }

         if (shoreType.contains("[NW]") && !shoreType.contains("[N]") && !shoreType.contains("[W]")) {
            quadNW = null;
         }

         Vector2 quadSE = new Vector2(24.0F, 16.0F);
         if (shoreType.contains("[E]")) {
            quadSE.add(16.0F, 0.0F);
         }

         if (shoreType.contains("[S]")) {
            quadSE.add(0.0F, -16.0F);
         }

         if (shoreType.contains("[SE]") && !shoreType.contains("[S]") && !shoreType.contains("[E]")) {
            quadSE = null;
         }

         Vector2 quadSW = new Vector2(16.0F, 16.0F);
         if (shoreType.contains("[W]")) {
            quadSW.add(-16.0F, 0.0F);
         }

         if (shoreType.contains("[S]")) {
            quadSW.add(0.0F, -16.0F);
         }

         if (shoreType.contains("[SW]") && !shoreType.contains("[S]") && !shoreType.contains("[W]")) {
            quadSW = null;
         }

         int scale = 48;

         for (int k = 0; k < 7; k++) {
            Pixmap newPixmap = new Pixmap(16, 16, Format.RGBA8888);
            Vector2[] offsets = new Vector2[]{new Vector2(0.0F, 0.0F), new Vector2(24.0F, 0.0F), new Vector2(0.0F, 24.0F), new Vector2(24.0F, 24.0F)};
            Vector2[] origins = new Vector2[]{new Vector2(0.0F, 0.0F), new Vector2(8.0F, 0.0F), new Vector2(0.0F, 8.0F), new Vector2(8.0F, 8.0F)};
            Vector2 offset = new Vector2();
            Vector2[] quads = new Vector2[]{quadNW, quadNE, quadSW, quadSE};

            for (int i = 0; i < quads.length && i < offsets.length; i++) {
               Pixmap currPixmap;
               byte var20;
               if (quads[i] == null) {
                  offset.set(offsets[i]);
                  currPixmap = innerPixmap;
                  var20 = 32;
               } else {
                  offset.x = quads[i].x;
                  offset.y = 40.0F - quads[i].y;
                  currPixmap = outerPixmap;
                  var20 = 48;
               }

               newPixmap.drawPixmap(currPixmap, (int)origins[i].x, (int)origins[i].y, (int)offset.x + k * var20, (int)offset.y, 8, 8);
            }

            pack(shoreType, k, newPixmap);
         }

         updateTextureAtlas();

         for (int k = 0; k < 7; k++) {
            addToLocalCache(shoreType, k);
         }

         addedTiles.add(shoreType);
      }
   }

   public static void cachePuddle(String tileName, String baseTile, String directions) {
      if (!addedTiles.contains(tileName)) {
         Texture text = TextureCache.get(Gdx.files.internal("tiles/puddle_sheet1.png"));
         SpriteProxy tempSprite = new SpriteProxy(text, 0, 0, text.getWidth(), text.getHeight());
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         Vector2 quadNE = new Vector2(40.0F, 8.0F);
         if (directions.contains("[E]")) {
            quadNE.set(8.0F, 8.0F);
         }

         if (directions.contains("[N]")) {
            quadNE.set(24.0F, 8.0F);
         }

         if (directions.contains("[N]") && directions.contains("[E]")) {
            quadNE.set(56.0F, 8.0F);
         }

         Vector2 quadNW = new Vector2(32.0F, 8.0F);
         if (directions.contains("[W]")) {
            quadNW.set(0.0F, 8.0F);
         }

         if (directions.contains("[N]")) {
            quadNW.set(16.0F, 8.0F);
         }

         if (directions.contains("[N]") && directions.contains("[W]")) {
            quadNW.set(48.0F, 8.0F);
         }

         Vector2 quadSE = new Vector2(40.0F, 0.0F);
         if (directions.contains("[E]")) {
            quadSE.set(8.0F, 0.0F);
         }

         if (directions.contains("[S]")) {
            quadSE.set(24.0F, 0.0F);
         }

         if (directions.contains("[S]") && directions.contains("[E]")) {
            quadSE.set(56.0F, 0.0F);
         }

         Vector2 quadSW = new Vector2(32.0F, 0.0F);
         if (directions.contains("[W]")) {
            quadSW.set(0.0F, 0.0F);
         }

         if (directions.contains("[S]")) {
            quadSW.set(16.0F, 0.0F);
         }

         if (directions.contains("[S]") && directions.contains("[W]")) {
            quadSW.set(48.0F, 0.0F);
         }

         text = TextureCache.get(Gdx.files.internal("tiles/" + baseTile + ".png"));
         temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap basePixmap = temp.consumePixmap();
         Pixmap newPixmap = new Pixmap(basePixmap.getWidth(), basePixmap.getHeight(), Format.RGBA8888);
         int offsetX = 0;
         int offsetY = 0;
         int height = text.getHeight();
         int width = text.getWidth();
         int blueColor = -1869022977;
         int greyColor = -1684300801;
         int i = 0;

         for (int j = 0; j < height; i++) {
            if (i > width) {
               i = -1;
               j++;
            } else {
               if (i < 8) {
                  if (j < 8) {
                     offsetX = (int)quadNW.x;
                     offsetY = 8 - (int)quadNW.y;
                  } else {
                     offsetX = (int)quadSW.x;
                     offsetY = 8 - (int)quadSW.y;
                  }
               } else if (j < 8) {
                  offsetX = (int)quadNE.x;
                  offsetY = 8 - (int)quadNE.y;
               } else {
                  offsetX = (int)quadSE.x;
                  offsetY = 8 - (int)quadSE.y;
               }

               int color2 = currPixmap.getPixel(i % 8 + offsetX, j % 8 + offsetY);
               if (color2 == 0 || color2 == 943208703 || color2 == 1891672064 || color2 == -1869023232) {
                  color2 = basePixmap.getPixel(i, j);
               } else if (color2 == greyColor) {
                  color2 = Color.rgba8888(tempSprite.color1);
               } else if (color2 == blueColor) {
                  int var27 = 0;
                  Color tempColor = new Color(basePixmap.getPixel(i, j));
                  tempColor.a = 0.25F;
                  color2 = Color.rgba8888(tempColor);
               }

               newPixmap.drawPixel(i, j, color2);
            }
         }

         pack(tileName, newPixmap);
         updateTextureAtlas();
         addToLocalCache(tileName);
         addedTiles.add(tileName);
      }
   }

   public static void cacheColoredHouseBed(String name) {
      if (!addedTiles.contains(name)) {
         String carpetName = name.replace("house_bed1colored", "");
         Texture text = TextureCache.get(Gdx.files.internal("tiles/buildings/house_bed1_colorbase.png"));
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap bedPixmap = temp.consumePixmap();
         text = TextureCache.get(Gdx.files.internal("tiles/buildings/" + carpetName + ".png"));
         temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap carpetPixmap = temp.consumePixmap();
         Pixmap newPixmap = new Pixmap(16, 32, Format.RGBA8888);
         newPixmap.setColor(0.0F, 0.0F, 0.0F, 0.0F);
         newPixmap.drawPixmap(carpetPixmap, 0, 12);
         newPixmap.drawPixmap(bedPixmap, 0, 0);
         pack(name, newPixmap);
         updateTextureAtlas();
         addToLocalCache(name);
         addedTiles.add(name);
      }
   }

   public static void cacheWaterHole(String name) {
      if (!addedTiles.contains(name)) {
         String[] names = name.split("_");
         String ending = "";
         if (names.length > 2 && !names[names.length - 1].equals("default")) {
            ending = "_" + names[names.length - 1];
         }

         for (int k = 0; k < 4; k++) {
            Texture text = TextureCache.get(Gdx.files.internal("tiles/hole1" + ending + ".png"));
            TextureData temp = text.getTextureData();
            if (!temp.isPrepared()) {
               temp.prepare();
            }

            Pixmap newPixmap = temp.consumePixmap();
            int height = text.getHeight();
            int width = text.getWidth();
            text = TextureCache.get(Gdx.files.internal("tiles/water2.png"));
            temp = text.getTextureData();
            if (!temp.isPrepared()) {
               temp.prepare();
            }

            Pixmap currPixmap = temp.consumePixmap();
            int i = 0;

            for (int j = 0; j < height; i++) {
               if (i > width) {
                  i = -1;
                  j++;
               } else {
                  Color color3 = new Color(newPixmap.getPixel(i, j));
                  if (color3.r == 0.21960784F && color3.g == 0.21960784F && color3.b == 0.21960784F) {
                     boolean isBlack = true;
                  } else {
                     boolean isBlack = false;
                  }

                  if (color3.a != 0.0F) {
                     Color color2 = new Color(currPixmap.getPixel(i + k, j));
                     newPixmap.drawPixel(i, j, Color.rgba8888(color2.r, color2.g, color2.b, color2.a));
                  }
               }
            }

            pack(name, k, newPixmap);
         }

         updateTextureAtlas();

         for (int k = 0; k < 4; k++) {
            addToLocalCache(name, k);
         }

         addedTiles.add(name);
      }
   }

   public static void cacheHole(String name) {
      String oldName = name;
      if (!addedTiles.contains(oldName)) {
         name = name.replace("[NE]", "");
         name = name.replace("[SE]", "");
         name = name.replace("[SW]", "");
         name = name.replace("[NW]", "");
         Texture text = TextureCache.get(Gdx.files.internal("tiles/" + name + ".png"));
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap newPixmap = temp.consumePixmap();
         int height = text.getHeight();
         int width = text.getWidth();
         int i = 0;

         for (int j = 0; j < height; i++) {
            if (i > width) {
               i = -1;
               j++;
            } else if (j < 10) {
               if (i < 8) {
                  if (oldName.contains("[NW]")) {
                     newPixmap.drawPixel(i, j, Color.rgba8888(0.627451F, 0.47058824F, 0.09411765F, 1.0F));
                  }
               } else if (oldName.contains("[NE]")) {
                  newPixmap.drawPixel(i, j, Color.rgba8888(0.627451F, 0.47058824F, 0.09411765F, 1.0F));
               }
            } else if (i < 8) {
               if (oldName.contains("[SW]")) {
                  newPixmap.drawPixel(i, j, Color.rgba8888(0.627451F, 0.47058824F, 0.09411765F, 1.0F));
               }
            } else if (oldName.contains("[SE]")) {
               newPixmap.drawPixel(i, j, Color.rgba8888(0.627451F, 0.47058824F, 0.09411765F, 1.0F));
            }
         }

         pack(oldName, newPixmap);
         updateTextureAtlas();
         addedTiles.add(oldName);
      }
   }

   public static void cacheInteriorWall(String tileName) {
      if (!addedTiles.contains(tileName)) {
         Texture text = TextureCache.get(Gdx.files.internal("tiles/buildings/interiorwall1.png"));
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         int width = 16;
         int height = 32;
         Pixmap newPixmap = new Pixmap(width, height, Format.RGBA8888);
         Vector2 bl = new Vector2(0.0F, 0.0F);
         Vector2 tr = new Vector2(8.0F, 8.0F);
         if (tileName.contains("N")) {
            bl.add(0.0F, 8.0F);
         }

         if (tileName.contains("S")) {
            tr.add(0.0F, -8.0F);
         }

         if (tileName.contains("E")) {
            tr.add(-8.0F, 0.0F);
         }

         if (tileName.contains("W")) {
            bl.add(8.0F, 0.0F);
         }

         int offsetX = 0;
         int offsetY = 0;
         int i = 0;

         for (int j = 0; j < 16; i++) {
            if (i > width) {
               i = -1;
               j++;
            } else {
               if (i < 8) {
                  offsetX = (int)bl.x;
               } else {
                  offsetX = (int)tr.x;
               }

               if (j < 8) {
                  offsetY = (int)bl.y;
               } else {
                  offsetY = (int)tr.y;
               }

               int color3 = currPixmap.getPixel(i + offsetX, j + offsetY);
               newPixmap.drawPixel(i, j, color3);
            }
         }

         text = TextureCache.get(Gdx.files.internal("tiles/buildings/house5_wall1.png"));
         temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         currPixmap = temp.consumePixmap();
         i = 0;

         for (int j = 0; j < 16; i++) {
            if (i > width) {
               i = -1;
               j++;
            } else {
               int color3 = currPixmap.getPixel(i, j);
               newPixmap.drawPixel(i, j + 16, color3);
            }
         }

         String[] names = tileName.split("\\|");
         if (names.length > 1) {
            String name = names[1];
            text = TextureCache.get(Gdx.files.internal("tiles/buildings/" + name + ".png"));
            temp = text.getTextureData();
            if (!temp.isPrepared()) {
               temp.prepare();
            }

            currPixmap = temp.consumePixmap();
            int ix = 0;

            for (int j = 0; j < 16; ix++) {
               if (ix > width) {
                  ix = -1;
                  j++;
               } else {
                  int color3 = currPixmap.getPixel(ix, j);
                  newPixmap.drawPixel(ix, j + 16, color3);
               }
            }
         }

         pack(tileName, newPixmap);
         updateTextureAtlas();
         addToLocalCache(tileName);
         addedTiles.add(tileName);
      }
   }

   public static void cacheHouseWithWindow(String name, SmolSprite overSprite) {
      if (!addedTiles.contains(name)) {
         Pixmap oldPixmap = overSprite.getPixmap();
         int left = overSprite.getRegionX();
         int bottom = overSprite.getRegionY();
         int regionWidth = overSprite.getRegionWidth();
         int regionHeight = overSprite.getRegionHeight();
         Pixmap newPixmap = new Pixmap(regionWidth, regionHeight, Format.RGBA8888);
         Texture text = TextureCache.get(Gdx.files.internal("tiles/buildings/exteriorwindows1.png"));
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         boolean touchEast = name.contains("E");
         boolean touchWest = name.contains("W");
         if (name.contains("house5")) {
            touchEast |= name.contains("left1");
            touchWest |= name.contains("right1");
         }

         for (int m = 0; m < newPixmap.getWidth(); m++) {
            for (int n = 0; n < newPixmap.getHeight(); n++) {
               int color2;
               if (n < 8) {
                  if (m < 8 && !touchEast) {
                     color2 = currPixmap.getPixel(m, n);
                  } else if (m >= 8 && !touchWest) {
                     color2 = currPixmap.getPixel(m, n);
                  } else if (name.contains("EW")) {
                     color2 = currPixmap.getPixel(m, n);
                  } else {
                     color2 = oldPixmap.getPixel(left + m, bottom + n);
                  }
               } else {
                  color2 = oldPixmap.getPixel(left + m, bottom + n);
               }

               newPixmap.drawPixel(m, n, color2);
            }
         }

         pack(name, newPixmap);
         updateTextureAtlas();
         addToLocalCache(name);
         newPixmap.dispose();
         addedTiles.add(name);
      }
   }

   public static void cacheHouseWithChimney(String name, SmolSprite overSprite) {
      if (!addedTiles.contains(name)) {
         Pixmap oldPixmap = overSprite.getPixmap();
         Pixmap newPixmap = new Pixmap(64, 32, Format.RGBA8888);
         newPixmap.setColor(0.0F, 0.0F, 0.0F, 0.0F);
         SmolSpriteProxy tempSprite = new SmolSpriteProxy(
            overSprite.getTexture(), overSprite.getRegionX(), overSprite.getRegionY(), overSprite.getRegionWidth(), overSprite.getRegionHeight()
         );
         int purple1 = -9181185;
         int purple2 = -14045697;
         int left = overSprite.getRegionX();
         int bottom = overSprite.getRegionY();
         Texture text = TextureCache.get(Gdx.files.internal("tiles/chimney_sheet1.png"));
         TextureData temp = text.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap chimneyPixmap = temp.consumePixmap();

         for (int i = 0; i < 4; i++) {
            newPixmap.fill();
            newPixmap.drawPixmap(oldPixmap, 0, 16, left, bottom, 16, 16);

            for (int m = 0; m < newPixmap.getWidth(); m++) {
               for (int n = 0; n < newPixmap.getHeight(); n++) {
                  int color2 = chimneyPixmap.getPixel(m + i * 64, n);
                  if (color2 == purple1) {
                     color2 = Color.rgba8888(tempSprite.color1);
                  } else if (color2 == purple2) {
                     color2 = Color.rgba8888(tempSprite.color2);
                  }

                  newPixmap.drawPixel(m, n, color2);
               }
            }

            pack(name, i, newPixmap);
         }

         newPixmap.dispose();
         updateTextureAtlas();

         for (int i = 0; i < 4; i++) {
            addToLocalCache(name, i);
         }

         addedTiles.add(name);
      }
   }

   public static void cacheDarkerTile(String name, SmolSprite sprite) {
      if (!addedTiles.contains(name)) {
         int regionWidth = sprite.getRegionWidth();
         int regionHeight = sprite.getRegionHeight();
         Pixmap atlasPixmap = sprite.getPixmap();
         Pixmap tilePixmap = new Pixmap(regionWidth, regionHeight, atlasPixmap.getFormat());
         tilePixmap.drawPixmap(atlasPixmap, 0, 0, sprite.getRegionX(), sprite.getRegionY(), regionWidth, regionHeight);
         Pixmap newPixmap = new Pixmap(tilePixmap.getWidth(), tilePixmap.getHeight(), Format.RGBA8888);
         int green1 = 678428927;
         int green1_2 = Color.rgba8888(new Color(0.20784314F, 0.40784314F, 0.007843138F, 1.0F));
         int green2 = 1623722239;
         int green2_2 = Color.rgba8888(new Color(0.40784314F, 0.80784315F, 0.007843138F, 1.0F));
         int green3 = -1325903617;
         int green3_2 = Color.rgba8888(new Color(0.60784316F, 1.0F, 0.20784314F, 1.0F));

         for (int m = 0; m < tilePixmap.getWidth(); m++) {
            for (int n = 0; n < tilePixmap.getHeight(); n++) {
               int color2 = tilePixmap.getPixel(m, n);
               if (color2 == green1 || color2 == green1_2) {
                  color2 = 760742655;
               } else if (color2 == green2 || color2 == green2_2) {
                  color2 = 1487864575;
               } else if (color2 == green3 || color2 == green3_2) {
                  color2 = -2066207233;
               }

               newPixmap.drawPixel(m, n, color2);
            }
         }

         pack(name, newPixmap);
         updateTextureAtlas();
         addToLocalCache(name);
         addedTiles.add(name);
      }
   }
}
