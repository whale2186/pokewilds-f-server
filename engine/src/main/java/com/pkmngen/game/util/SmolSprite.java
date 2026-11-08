package com.pkmngen.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.pkmngen.game.util.texture.TilesAtlas;

public class SmolSprite extends TextureRegion {
   private float x;
   private float y;
   public static Color prevColor = new Color();
   private int offsetX = 0;
   private int offsetY = 0;

   public SmolSprite() {
   }

   public SmolSprite(Texture texture) {
      this(texture, 0, 0, texture.getWidth(), texture.getHeight());
   }

   public SmolSprite(Texture texture, int srcWidth, int srcHeight) {
      this(texture, 0, 0, srcWidth, srcHeight);
   }

   public SmolSprite(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      if (texture == null) {
         throw new IllegalArgumentException("texture cannot be null.");
      }

      this.setTexture(texture);
      this.setRegion(srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSprite(TextureRegion region) {
      this.setRegion(region);
   }

   public SmolSprite(AtlasRegion atlasRegion) {
      this((TextureRegion)atlasRegion);
      this.offsetX = atlasRegion.getRegionX();
      this.offsetY = atlasRegion.getRegionY();
   }

   @Override
   public void setRegion(int x, int y, int width, int height) {
      super.setRegion(x + this.offsetX, y + this.offsetY, width, height);
   }

   public void setRegion(AtlasRegion atlasRegion) {
      super.setRegion(atlasRegion);
      this.offsetX = atlasRegion.getRegionX();
      this.offsetY = atlasRegion.getRegionY();
   }

   @Override
   public void setRegionX(int x) {
      super.setRegionX(x + this.offsetX);
   }

   @Override
   public void setRegionY(int y) {
      super.setRegionY(y + this.offsetY);
   }

   public void draw(Batch batch) {
      batch.draw(this, this.x, this.y);
   }

   public void setPosition(float x, float y) {
      this.x = x;
      this.y = y;
   }

   @Override
   public Texture getTexture() {
      return super.getTexture();
   }

   public Pixmap getPixmap() {
      Texture texture = this.getTexture();
      Pixmap originalPixmap = TilesAtlas.getAtlasPixmap(texture);
      if (originalPixmap == null) {
         TextureData textureData = texture.getTextureData();
         if (!textureData.isPrepared()) {
            textureData.prepare();
         }

         originalPixmap = textureData.consumePixmap();
      }

      return originalPixmap;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getWidth() {
      return this.getRegionWidth();
   }

   public float getHeight() {
      return this.getRegionHeight();
   }

   public void translateX(float xAmount) {
      this.x += xAmount;
   }

   public void translateY(float yAmount) {
      this.y += yAmount;
   }

   public void draw(Batch batch, float alphaModulation) {
      prevColor.set(batch.getColor());
      batch.setColor(prevColor.r, prevColor.g, prevColor.b, alphaModulation);
      this.draw(batch);
      batch.setColor(prevColor);
   }
}
