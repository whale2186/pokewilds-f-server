package com.pkmngen.game.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProxyBatch extends SpriteBatch {
   boolean disabled = false;
   public boolean tintSprites;
   private float[] tintedVertices = new float[80];

   @Override public void draw(Texture texture, float[] vertices, int offset, int count) {
      com.badlogic.gdx.graphics.Color tint = getColor();
      if (!tintSprites || tint.equals(com.badlogic.gdx.graphics.Color.WHITE)) { super.draw(texture, vertices, offset, count); return; }
      if (tintedVertices.length < count) tintedVertices = new float[count];
      System.arraycopy(vertices, offset, tintedVertices, 0, count);
      for (int i = 2; i < count; i += 5) {
         int rgba = com.badlogic.gdx.utils.NumberUtils.floatToIntColor(tintedVertices[i]);
         int r = (int)((rgba & 255) * tint.r), g = (int)(((rgba >>> 8) & 255) * tint.g);
         int b = (int)(((rgba >>> 16) & 255) * tint.b), a = (int)(((rgba >>> 24) & 255) * tint.a);
         tintedVertices[i] = com.badlogic.gdx.utils.NumberUtils.intToFloatColor(r | g << 8 | b << 16 | a << 24);
      }
      super.draw(texture, tintedVertices, 0, count);
   }

   public void draw(SpriteProxy sprite, float x, float y) {
      Texture texture = sprite.getTexture();
      if (SpriteProxy.inverseColors) {
         sprite.setTexture(sprite.inverseTexture);
         texture = sprite.inverseTexture;
      } else if (!SpriteProxy.inverseColors && texture == sprite.inverseTexture) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.darkenAllColors1 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.darkenTexture1);
         texture = sprite.darkenTexture1;
      } else if (!SpriteProxy.darkenAllColors1 && texture == sprite.darkenTexture1) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.darkenAllColors2 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.darkenTexture2);
         texture = sprite.darkenTexture2;
      } else if (!SpriteProxy.darkenAllColors2 && texture == sprite.darkenTexture2) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.darkenAllColors3 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.darkenTexture3);
         texture = sprite.darkenTexture3;
      } else if (!SpriteProxy.darkenAllColors3 && texture == sprite.darkenTexture3) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.confuseRayColors1 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.confuseRayTexture1);
         texture = sprite.confuseRayTexture1;
      } else if (!SpriteProxy.confuseRayColors1 && texture == sprite.confuseRayTexture1) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.confuseRayColors2 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.confuseRayTexture2);
         texture = sprite.confuseRayTexture2;
      } else if (!SpriteProxy.confuseRayColors2 && texture == sprite.confuseRayTexture2) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      super.draw(sprite, x, y);
   }

   public void draw(SmolSpriteProxy sprite, float x, float y) {
      Texture texture = sprite.getTexture();
      if (SpriteProxy.inverseColors) {
         sprite.setTexture(sprite.inverseTexture);
         texture = sprite.inverseTexture;
      } else if (!SpriteProxy.inverseColors && texture == sprite.inverseTexture) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.darkenAllColors1 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.darkenTexture1);
         texture = sprite.darkenTexture1;
      } else if (!SpriteProxy.darkenAllColors1 && texture == sprite.darkenTexture1) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.darkenAllColors2 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.darkenTexture2);
         texture = sprite.darkenTexture2;
      } else if (!SpriteProxy.darkenAllColors2 && texture == sprite.darkenTexture2) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.darkenAllColors3 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.darkenTexture3);
         texture = sprite.darkenTexture3;
      } else if (!SpriteProxy.darkenAllColors3 && texture == sprite.darkenTexture3) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.confuseRayColors1 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.confuseRayTexture1);
         texture = sprite.confuseRayTexture1;
      } else if (!SpriteProxy.confuseRayColors1 && texture == sprite.confuseRayTexture1) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      if (SpriteProxy.confuseRayColors2 && texture == sprite.originalTexture) {
         sprite.setTexture(sprite.confuseRayTexture2);
         texture = sprite.confuseRayTexture2;
      } else if (!SpriteProxy.confuseRayColors2 && texture == sprite.confuseRayTexture2) {
         sprite.setTexture(sprite.originalTexture);
         texture = sprite.originalTexture;
      }

      super.draw(sprite, x, y);
   }
}
