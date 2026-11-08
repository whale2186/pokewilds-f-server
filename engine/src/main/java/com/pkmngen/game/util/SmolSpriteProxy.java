package com.pkmngen.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;

public class SmolSpriteProxy extends SmolSprite {
   public static boolean inverseColors = false;
   public boolean darkenColors1 = false;
   public boolean darkenColors2 = false;
   public boolean darkenColors3 = false;
   public static boolean darkenAllColors1 = false;
   public static boolean darkenAllColors2 = false;
   public static boolean darkenAllColors3 = false;
   public static boolean confuseRayColors1 = false;
   public static boolean confuseRayColors2 = false;
   public boolean lightenColors1 = false;
   public boolean lightenColors2 = false;
   public static boolean lightenAllColors1 = false;
   public static boolean lightenAllColors2 = false;
   public Color color1 = null;
   public Color color2 = new Color();
   public Color black = Color.BLACK;
   Texture originalTexture;
   Texture inverseTexture;
   Texture darkenTexture1;
   Texture darkenTexture2;
   Texture darkenTexture3;
   Texture lightenTexture1;
   Texture lightenTexture2;
   Texture confuseRayTexture1;
   Texture confuseRayTexture2;

   @Override
   public void draw(Batch batch) {
      Texture texture = this.getTexture();
      if (SpriteProxy.inverseColors) {
         this.setTexture(this.inverseTexture);
         texture = this.inverseTexture;
      } else if (!SpriteProxy.inverseColors && texture == this.inverseTexture) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.darkenAllColors1 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture1);
         texture = this.darkenTexture1;
      } else if (!SpriteProxy.darkenAllColors1 && texture == this.darkenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.darkenAllColors2 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture2);
         texture = this.darkenTexture2;
      } else if (!SpriteProxy.darkenAllColors2 && texture == this.darkenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.darkenAllColors3 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture3);
         texture = this.darkenTexture3;
      } else if (!SpriteProxy.darkenAllColors3 && texture == this.darkenTexture3) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.lightenAllColors1 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture1);
         texture = this.lightenTexture1;
      } else if (!SpriteProxy.lightenAllColors1 && texture == this.lightenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.lightenAllColors2 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture2);
         texture = this.lightenTexture2;
      } else if (!SpriteProxy.lightenAllColors2 && texture == this.lightenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.lightenColors1 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture1);
         texture = this.lightenTexture1;
      } else if (!this.lightenColors1 && !SpriteProxy.lightenAllColors1 && texture == this.lightenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.lightenColors2 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture2);
         texture = this.lightenTexture2;
      } else if (!this.lightenColors2 && !SpriteProxy.lightenAllColors2 && texture == this.lightenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.confuseRayColors1 && texture == this.originalTexture) {
         this.setTexture(this.confuseRayTexture1);
         texture = this.confuseRayTexture1;
      } else if (!SpriteProxy.confuseRayColors1 && texture == this.confuseRayTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.confuseRayColors2 && texture == this.originalTexture) {
         this.setTexture(this.confuseRayTexture2);
         texture = this.confuseRayTexture2;
      } else if (!SpriteProxy.confuseRayColors2 && texture == this.confuseRayTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.darkenColors1 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture1);
         texture = this.darkenTexture1;
      } else if (!this.lightenColors1 && !SpriteProxy.darkenAllColors1 && texture == this.darkenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.darkenColors2 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture2);
         texture = this.darkenTexture2;
      } else if (!this.darkenColors2 && !SpriteProxy.darkenAllColors2 && texture == this.darkenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.darkenColors3 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture3);
         texture = this.darkenTexture3;
      } else if (!this.darkenColors3 && !SpriteProxy.darkenAllColors3 && texture == this.darkenTexture3) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      super.draw(batch);
   }

   public SmolSpriteProxy(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      this(null, texture, srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSpriteProxy(Color color1, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      this(color1, new Color(), texture, srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSpriteProxy(Color color1, Color color2, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      this(Color.BLACK, color1, new Color(), texture, srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSpriteProxy(Color black, Color color1, Color color2, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      super(texture, srcX, srcY, srcWidth, srcHeight);
      this.color1 = color1;
      this.color2 = color2;
      this.black = black;
      this.originalTexture = texture;
      if (!TextureCache.effectsTextMap.containsKey(texture)) {
         Texture[] textures = new Texture[8];
         TextureData temp = texture.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         Pixmap newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         int i = 0;

         for (int j = 0; j < texture.getHeight(); i++) {
            if (i > texture.getWidth()) {
               i = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(i, j));
               if ((color.r != 0.0F || color.g != 0.9843137F || color.b != 0.0F)
                  && (color.r != 0.0F || color.g != 0.0F || color.b != 0.0F)
                  && (color.r != 1.0F || color.g != 1.0F || color.b != 1.0F)) {
                  if (this.color1 == null) {
                     this.color1 = color;
                  } else if (color.r != this.color1.r || color.g != this.color1.g || color.b != this.color1.b) {
                     this.color2 = color;
                     break;
                  }
               }
            }
         }

         if (this.color1 != null && this.color2 != null && this.color1.r + this.color1.g + this.color1.b < this.color2.r + this.color2.g + this.color2.b) {
            Color tempColor = this.color1;
            this.color1 = this.color2;
            this.color2 = tempColor;
         }

         Color[] colors = new Color[]{this.color1, this.color2};
         TextureCache.colorsTextMap.put(texture, colors);
         int ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (color.r == 0.0F && color.g == 0.0F && color.b == 0.0F) {
                  color = Color.WHITE;
               } else if (color.r == 1.0F && color.g == 1.0F && color.b == 1.0F) {
                  color = this.black;
               } else if (color.r == 1.0F && color.g == 0.9843137F && color.b == 1.0F) {
                  color = this.black;
               } else if (color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                  color = this.color2;
               } else if (color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                  color = this.color1;
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[0] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if ((color.r != 0.0F || color.g != 0.0F || color.b != 0.0F)
                  && (color.r != 1.0F || color.g != 1.0F || color.b != 1.0F)
                  && (color.r != 1.0F || color.g != 0.9843137F || color.b != 1.0F)
                  && this.color1 != null
                  && color.r == this.color2.r
                  && color.g == this.color2.g
                  && color.b == this.color2.b) {
                  color = this.black;
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[1] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if ((color.r != 0.0F || color.g != 0.0F || color.b != 0.0F)
                  && (color.r != 1.0F || color.g != 1.0F || color.b != 1.0F)
                  && (color.r != 1.0F || color.g != 0.9843137F || color.b != 1.0F)) {
                  if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                     color = this.black;
                  } else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                     color = this.color2;
                  }
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[2] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (color.r == 0.0F && color.g == 0.0F && color.b == 0.0F) {
                  color = this.color2;
               } else if ((color.r != 1.0F || color.g != 1.0F || color.b != 1.0F) && (color.r != 1.0F || color.g != 0.9843137F || color.b != 1.0F)) {
                  if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                     color = this.color1;
                  } else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                     color = Color.WHITE;
                  }
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[3] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (color.r == 0.0F && color.g == 0.0F && color.b == 0.0F) {
                  color = this.color1;
               } else if ((color.r != 1.0F || color.g != 1.0F || color.b != 1.0F) && (color.r != 1.0F || color.g != 0.9843137F || color.b != 1.0F)) {
                  if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                     color = Color.WHITE;
                  } else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                     color = Color.WHITE;
                  }
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[4] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (color.r == 0.0F && color.g == 0.0F && color.b == 0.0F) {
                  color = this.color1;
               } else if (color.r == 1.0F && color.g == 1.0F && color.b == 1.0F) {
                  color = this.black;
               } else if (color.r == 1.0F && color.g == 0.9843137F && color.b == 1.0F) {
                  color = this.black;
               } else if ((this.color1 == null || color.r != this.color2.r || color.g != this.color2.g || color.b != this.color2.b)
                  && this.color1 != null
                  && color.r == this.color1.r
                  && color.g == this.color1.g
                  && color.b == this.color1.b) {
                  color = Color.WHITE;
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[5] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (color.r == 0.0F && color.g == 0.0F && color.b == 0.0F) {
                  color = this.color2;
               } else if (color.r == 1.0F && color.g == 1.0F && color.b == 1.0F) {
                  color = this.black;
               } else if (color.r == 1.0F && color.g == 0.9843137F && color.b == 1.0F) {
                  color = this.black;
               } else if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                  color = Color.WHITE;
               } else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[6] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int j = 0; j < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, j));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if ((color.r != 0.0F || color.g != 0.0F || color.b != 0.0F)
                  && (color.r != 1.0F || color.g != 1.0F || color.b != 1.0F)
                  && (color.r != 1.0F || color.g != 0.9843137F || color.b != 1.0F)) {
                  if (this.color1 != null && color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                     color = this.black;
                  } else if (this.color1 != null && color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                     color = this.black;
                  }
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, j, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[7] = TextureCache.get(newPixmap);
         TextureCache.effectsTextMap.put(texture, textures);
      } else {
         this.color1 = TextureCache.colorsTextMap.get(texture)[0];
         this.color2 = TextureCache.colorsTextMap.get(texture)[1];
      }

      this.inverseTexture = TextureCache.effectsTextMap.get(texture)[0];
      this.darkenTexture1 = TextureCache.effectsTextMap.get(texture)[1];
      this.darkenTexture2 = TextureCache.effectsTextMap.get(texture)[2];
      this.lightenTexture1 = TextureCache.effectsTextMap.get(texture)[3];
      this.lightenTexture2 = TextureCache.effectsTextMap.get(texture)[4];
      this.confuseRayTexture1 = TextureCache.effectsTextMap.get(texture)[5];
      this.confuseRayTexture2 = TextureCache.effectsTextMap.get(texture)[6];
      this.darkenTexture3 = TextureCache.effectsTextMap.get(texture)[7];
   }
}
