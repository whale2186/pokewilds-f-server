package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;

class EggHatchAnim extends Action {
   public Action.Layer layer = Action.Layer.gui_130;
   public Sprite bgSprite;
   public static boolean drawPostHatchBottom = false;
   public static boolean drawPostHatchTop = false;
   public static boolean drawSprite = false;
   Pokemon pokemon;
   public static boolean isDone = false;

   public EggHatchAnim(Pokemon pokemon, Action nextAction) {
      super();
      Texture text = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      this.bgSprite = new Sprite(text, 0, 0, 160, 144);
      this.pokemon = pokemon;
      this.nextAction = nextAction;
   }

   @Override
   public void firstStep(Game game) {
      drawPostHatchBottom = false;
      drawPostHatchTop = false;
      drawSprite = false;
      isDone = false;
      this.pokemon.hatch();
      game.battle.oppPokemon = this.pokemon;
      FileHandle fileHandle = Gdx.files.internal("attacks/egg_hatch_gsc/output/frame-452.png");
      Texture text = TextureCache.get(fileHandle);
      TextureData temp = text.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      Pixmap currPixmap = temp.consumePixmap();
      Pixmap newPixmap = new Pixmap(text.getWidth(), text.getHeight(), Format.RGBA8888);
      newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
      newPixmap.fill();
      int i = 0;

      for (int j = 0; j < text.getHeight(); i++) {
         if (i > text.getWidth()) {
            i = -1;
            j++;
         } else {
            Color color = new Color(currPixmap.getPixel(i, j));
            if ((int)(color.r * 255.0F) == 240 && (int)(color.g * 255.0F) == 208 && (int)(color.b * 255.0F) == 88) {
               color.r = this.pokemon.sprite.color1.r;
               color.g = this.pokemon.sprite.color1.g;
               color.b = this.pokemon.sprite.color1.b;
            } else if ((int)(color.r * 255.0F) == 184 && (int)(color.g * 255.0F) == 128 && (int)(color.b * 255.0F) == 0) {
               color.r = this.pokemon.sprite.color2.r;
               color.g = this.pokemon.sprite.color2.g;
               color.b = this.pokemon.sprite.color2.b;
            }

            newPixmap.drawPixel(i, j, Color.rgba8888(color.r, color.g, color.b, color.a));
         }
      }

      TextureCache.textMap.put(fileHandle, TextureCache.get(newPixmap));
      game.insertAction(this.nextAction);
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (isDone) {
         game.actionStack.remove(this);
      } else {
         game.uiBatch.draw(this.bgSprite, 0.0F, 0.0F);
         if (drawPostHatchTop) {
            this.pokemon.sprite.setRegionY(0);
            this.pokemon.sprite.setRegionHeight((int)this.pokemon.sprite.getHeight());
            drawSprite = true;
            drawPostHatchTop = false;
         }

         if (drawPostHatchBottom) {
            this.pokemon.sprite.setRegionY((int)this.pokemon.sprite.getWidth() - (int)this.pokemon.sprite.getWidth() + (int)this.pokemon.sprite.getWidth() / 2);
            this.pokemon.sprite.setRegionHeight((int)this.pokemon.sprite.getWidth() / 2);
            drawSprite = true;
            drawPostHatchBottom = false;
         }

         if (drawSprite) {
            game.uiBatch.draw(this.pokemon.sprite, 80 - (int)this.pokemon.sprite.getWidth() / 2 + 4, 64.0F);
         }
      }
   }

   public static class Done extends Action {
      public int timer = 13;

      public Done(int timer, Action nextAction) {
         super();
         this.timer = timer;
         this.nextAction = nextAction;
      }

      @Override
      public void step(Game game) {
         this.timer--;
         if (this.timer <= 0) {
            EggHatchAnim.isDone = true;
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }

         EggHatchAnim.drawSprite = false;
      }
   }
}
