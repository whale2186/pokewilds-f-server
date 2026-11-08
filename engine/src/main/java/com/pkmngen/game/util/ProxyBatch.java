package com.pkmngen.game.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProxyBatch extends SpriteBatch {
   boolean disabled = false;

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
