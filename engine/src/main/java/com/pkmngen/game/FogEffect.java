package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;

class FogEffect extends Action {
   public Action.Layer layer = Action.Layer.map_0;
   public static FogEffect.Type type = null;
   public static boolean refresh = true;
   Texture fogTexture1;
   Texture fogTexture2;
   Pixmap fogPixmap1;
   Pixmap fogPixmap2;
   Texture shadeTexture1;
   Texture shadeTexture2;
   Pixmap shadePixmap1;
   Pixmap shadePixmap2;
   Texture smokeTexture1;
   Texture smokeTexture2;
   Pixmap smokePixmap1;
   Pixmap smokePixmap2;
   ArrayList<Vector2> fogPositions1 = new ArrayList<>();
   ArrayList<Vector2> fogPositions2 = new ArrayList<>();
   Vector2 startPos;
   Vector2 endPos;
   int timer = 0;
   Color prevColor = new Color();
   Vector2 currOffset = new Vector2();
   Pixmap pixmap;
   Texture texture;
   Vector2 prevPos = new Vector2();
   Vector2 newPos = new Vector2();
   FogEffect.LightenScreen lightenScreen;
   ArrayList<Vector2> positionsQueue = new ArrayList<>();
   FogEffect.Type prevType = null;
   public static Rectangle effectArea = new Rectangle(0.0F, 0.0F, 64.0F, 64.0F);

   public FogEffect() {
      super();
      this.fogTexture1 = TextureCache.get(Gdx.files.internal("fog1_alpha.png"));
      this.fogTexture2 = TextureCache.get(Gdx.files.internal("fog2_alpha.png"));
      TextureData temp = this.fogTexture1.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.fogPixmap1 = temp.consumePixmap();
      temp = this.fogTexture2.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.fogPixmap2 = temp.consumePixmap();
      this.shadeTexture1 = TextureCache.get(Gdx.files.internal("shade5.png"));
      this.shadeTexture2 = TextureCache.get(Gdx.files.internal("shade6.png"));
      temp = this.shadeTexture1.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.shadePixmap1 = temp.consumePixmap();
      temp = this.shadeTexture2.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.shadePixmap2 = temp.consumePixmap();
      this.smokeTexture1 = TextureCache.get(Gdx.files.internal("shade3_dithered.png"));
      this.smokeTexture2 = TextureCache.get(Gdx.files.internal("shade4_dithered.png"));
      temp = this.smokeTexture1.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.smokePixmap1 = temp.consumePixmap();
      temp = this.smokeTexture2.getTextureData();
      if (!temp.isPrepared()) {
         temp.prepare();
      }

      this.smokePixmap2 = temp.consumePixmap();
      this.pixmap = new Pixmap(880, 576, Format.RGBA8888);
      this.pixmap.setColor(new Color(1.0F, 1.0F, 1.0F, 0.0F));
      this.texture = TextureCache.get(this.pixmap);
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
   public void firstStep(Game game) {
      this.lightenScreen = new FogEffect.LightenScreen();
   }

   @Override
   public void step(Game game) {
      if (type != null && !game.map.timeOfDay.equals("night") && game.map.tiles == game.map.overworldTiles) {
         if (type == FogEffect.Type.FOG) {
            this.fogStep(game);
            this.lightenScreen.step(game);
         } else if (type == FogEffect.Type.DEEPFOREST) {
            this.deepForestStep(game);
         } else if (type == FogEffect.Type.SMOKE) {
            this.smokeStep(game);
         }
      }
   }

   public void deepForestStep(Game game) {
      if (refresh) {
         this.positionsQueue.clear();
         Vector2 centerPos = game.player.position.cpy();
         centerPos.x = centerPos.x - Math.floorMod((int)centerPos.x, 16);
         centerPos.y = centerPos.y - Math.floorMod((int)centerPos.y, 16);
         this.startPos = centerPos.cpy().add(-this.pixmap.getWidth() / 2, -this.pixmap.getHeight() / 2);
         this.startPos.x = this.startPos.x - Math.floorMod((int)this.startPos.x, 16);
         this.startPos.y = this.startPos.y - Math.floorMod((int)this.startPos.y, 16);
         this.endPos = centerPos.cpy().add(this.pixmap.getWidth() / 2, this.pixmap.getHeight() / 2);
         this.endPos.x = this.endPos.x - Math.floorMod((int)this.endPos.x, 16);
         this.endPos.y = this.endPos.y - Math.floorMod((int)this.endPos.y, 16);
         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            this.positionsQueue.add(currPos.cpy());
            currPos.x += 4.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 8.0F;
            }
         }

         effectArea.setCenter(centerPos);
         this.pixmap.fill();
         refresh = false;
         this.fogPositions1.clear();
         this.fogPositions2.clear();
         if (this.prevType != type) {
            this.prevType = type;
            this.texture.draw(this.pixmap, 0, 0);
         }
      }

      for (int i = 0; !this.positionsQueue.isEmpty() && i < 768; i++) {
         int scale = 16;
         Vector2 currPos = this.positionsQueue.remove(0);
         if (Math.floorMod((int)currPos.x / 4, 2) == Math.floorMod((int)currPos.y / 8, 2)) {
            Vector2 rotate = currPos.cpy().rotateDeg(0.0F);
            int offsetY = Math.floorMod((int)Math.abs(rotate.y / scale), 14) - 7;
            int offsetX = Math.floorMod((int)Math.abs(rotate.x / scale), 14) - 7;
            offsetX = Math.abs(offsetX);
            offsetY = Math.abs(offsetY);
            int offset1 = offsetX + offsetY;
            offsetY = Math.abs(Math.floorMod((int)Math.abs(rotate.y / scale), 20) - 10);
            offsetX = Math.abs(Math.floorMod((int)Math.abs(rotate.x / scale), 20) - 10);
            int offset2 = offsetX + offsetY;
            offset2 /= 5;
            if (offset1 * offset2 > 7) {
               this.pixmap.drawPixmap(this.shadePixmap1, (int)(currPos.x - this.startPos.x), this.pixmap.getHeight() - (int)(currPos.y - this.startPos.y));
            } else if (offset1 * offset2 > 4) {
               this.pixmap.drawPixmap(this.shadePixmap2, (int)(currPos.x - this.startPos.x), this.pixmap.getHeight() - (int)(currPos.y - this.startPos.y));
            }
         }

         if (this.positionsQueue.isEmpty()) {
            this.texture.draw(this.pixmap, 0, 0);
            effectArea.getCenter(this.prevPos);
         }
      }

      Color tempColor = game.mapBatch.getColor().cpy();
      game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 0.4F));
      game.mapBatch.draw(this.texture, this.prevPos.x + 8.0F + 32.0F - this.pixmap.getWidth() / 2, this.prevPos.y + 8.0F - this.pixmap.getHeight() / 2);
      game.mapBatch.setColor(tempColor);
   }

   public void fogStep(Game game) {
      if (refresh) {
         this.positionsQueue.clear();
         Vector2 centerPos = game.player.position.cpy();
         centerPos.x = centerPos.x - Math.floorMod((int)centerPos.x, 16);
         centerPos.y = centerPos.y - Math.floorMod((int)centerPos.y, 16);
         this.startPos = centerPos.cpy().add(-this.pixmap.getWidth() / 2, -this.pixmap.getHeight() / 2);
         this.startPos.x = this.startPos.x - Math.floorMod((int)this.startPos.x, 16);
         this.startPos.y = this.startPos.y - Math.floorMod((int)this.startPos.y, 16);
         this.endPos = centerPos.cpy().add(this.pixmap.getWidth() / 2, this.pixmap.getHeight() / 2);
         this.endPos.x = this.endPos.x - Math.floorMod((int)this.endPos.x, 16);
         this.endPos.y = this.endPos.y - Math.floorMod((int)this.endPos.y, 16);
         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            this.positionsQueue.add(currPos.cpy());
            currPos.x += 4.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 8.0F;
            }
         }

         effectArea.setCenter(centerPos);
         this.pixmap.fill();
         refresh = false;
         this.fogPositions1.clear();
         this.fogPositions2.clear();
         if (this.prevType != type) {
            this.prevType = type;
            this.texture.draw(this.pixmap, 0, 0);
         }
      }

      for (int i = 0; !this.positionsQueue.isEmpty() && i < 768; i++) {
         int scale = 16;
         Vector2 currPos = this.positionsQueue.remove(0);
         if (Math.floorMod((int)currPos.x / 8, 2) == Math.floorMod((int)currPos.y / 16, 2)) {
            Vector2 rotate = currPos.cpy().rotateDeg(0.0F);
            int offsetY = Math.floorMod((int)(rotate.y / scale), 14) - 7;
            int offsetX = Math.floorMod((int)(rotate.x / scale - rotate.y / scale), 14) - 7;
            offsetX = Math.abs(offsetX);
            offsetY = Math.abs(offsetY);
            int offset1 = offsetX + offsetY;
            offsetY = Math.floorMod((int)(rotate.y / scale), 20) - 10;
            offsetX = Math.floorMod((int)(rotate.x / scale), 20) - 10;
            offsetX = Math.abs(offsetX);
            offsetY = Math.abs(offsetY);
            int offset2 = offsetX + offsetY;
            offset2 /= 5;
            offset2 = offset1 * offset2;
            if (offset2 > 9) {
               Vector2 newPos = currPos.cpy().add(offset2 % 3 - 1, offset1 % 3 - 1);
               this.pixmap.drawPixmap(this.fogPixmap2, (int)(newPos.x - this.startPos.x), this.pixmap.getHeight() - (int)(newPos.y - this.startPos.y));
            }

            if (offset2 > 11) {
               Vector2 newPos = currPos.cpy().add(offset2 % 3 - 1, offset1 % 3 - 1);
               this.pixmap.drawPixmap(this.fogPixmap1, (int)(newPos.x - this.startPos.x), this.pixmap.getHeight() - (int)(newPos.y - this.startPos.y));
            }

            if (offset2 > 13) {
               Vector2 newPos = currPos.cpy().add(offset2 % 3 - 1, offset1 % 3 - 1);
               this.pixmap.drawPixmap(this.fogPixmap1, (int)(newPos.x - this.startPos.x), this.pixmap.getHeight() - (int)(newPos.y - this.startPos.y));
               newPos = currPos.cpy().add(offset1 % 3 - 1, 4.0F);
               this.pixmap.drawPixmap(this.fogPixmap1, (int)(newPos.x - this.startPos.x), this.pixmap.getHeight() - (int)(newPos.y - this.startPos.y));
            }
         }

         if (this.positionsQueue.isEmpty()) {
            this.texture.draw(this.pixmap, 0, 0);
            effectArea.getCenter(this.prevPos);
         }
      }

      this.prevColor.set(game.mapBatch.getColor());
      game.mapBatch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      game.mapBatch
         .draw(
            this.texture,
            this.prevPos.x + 8.0F + 32.0F - this.pixmap.getWidth() / 2 + this.currOffset.x,
            this.prevPos.y + 8.0F - this.pixmap.getHeight() / 2 + this.currOffset.y
         );
      this.timer++;
      if (this.timer >= 151) {
         this.currOffset.add(-2.0F, 0.0F);
         this.timer = 0;
      }

      game.mapBatch.setColor(this.prevColor);
   }

   public void coolCloudsEffect(Game game) {
      if (refresh) {
         this.pixmap.fill();
         this.prevPos.set(game.player.position.x, game.player.position.y);
         refresh = false;
         this.fogPositions1.clear();
         this.fogPositions2.clear();
         this.startPos = game.player.position.cpy().add(-384.0F, -256.0F);
         this.startPos.x = this.startPos.x - this.startPos.x % 16.0F;
         this.startPos.y = this.startPos.y - this.startPos.y % 16.0F;
         this.endPos = game.player.position.cpy().add(384.0F, 256.0F);
         this.endPos.x = this.endPos.x - this.endPos.x % 16.0F;
         this.endPos.y = this.endPos.y - this.endPos.y % 16.0F;
         int scale = 16;
         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            if (currPos.x / 8.0F % 2.0F == currPos.y / 16.0F % 2.0F) {
               Vector2 rotate = currPos.cpy().sub(this.currOffset).rotate(0.0F);
               int offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
               int offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
               offsetX = Math.abs(offsetX);
               offsetY = Math.abs(offsetY);
               int offset1 = offsetX + offsetY;
               offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
               offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
               int offset2 = offsetX + offsetY;
               offset2 /= 5;
               if (offset1 * offset2 > 9) {
                  this.fogPositions2.add(currPos.cpy().add(Game.rand.nextInt(3) - 1, Game.rand.nextInt(3) - 1));
               }

               if (offset1 * offset2 > 11) {
                  this.fogPositions1.add(currPos.cpy().add(Game.rand.nextInt(3) - 1, Game.rand.nextInt(3) - 1));
               }

               if (offset1 * offset2 > 13) {
                  this.fogPositions1.add(currPos.cpy().add(Game.rand.nextInt(3) - 1, Game.rand.nextInt(3) - 1));
                  this.fogPositions1.add(currPos.cpy().add(Game.rand.nextInt(3) - 1, 4.0F));
               }
            }

            currPos.x += 4.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 8.0F;
            }
         }

         for (Vector2 currPosx : this.fogPositions1) {
            this.pixmap.drawPixmap(this.shadePixmap1, (int)(currPosx.x - this.startPos.x), (int)(currPosx.y - this.startPos.y));
         }

         for (Vector2 currPosx : this.fogPositions2) {
            this.pixmap.drawPixmap(this.shadePixmap2, (int)(currPosx.x - this.startPos.x), (int)(currPosx.y - this.startPos.y));
         }
      }

      Color tempColor = game.mapBatch.getColor().cpy();
      game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 0.4F));
      this.texture.draw(this.pixmap, 0, 0);
      game.mapBatch.draw(this.texture, this.prevPos.x + 8.0F - 320.0F, this.prevPos.y + 8.0F - 216.0F);
      game.mapBatch.setColor(tempColor);
   }

   public void smokeStepTriedButFailedX2(Game game) {
      if (refresh) {
         effectArea.setCenter(game.player.position);
         this.pixmap.fill();
         refresh = false;
         int width = this.pixmap.getWidth() / 2;
         int height = this.pixmap.getHeight() / 2;
         if (this.prevPos == null) {
            this.prevPos = new Vector2(game.player.position.x - width, game.player.position.y - height);
         }

         this.newPos.set(game.player.position.x - width, game.player.position.y - height);
         this.newPos.x = this.newPos.x - (this.prevPos.x - this.newPos.x);
         this.newPos.y = this.newPos.y - (this.prevPos.y - this.newPos.y);
         this.newPos = this.newPos.sub(this.currOffset);
         int modX = Math.floorMod((int)this.newPos.x, 16);
         int modY = Math.floorMod((int)this.newPos.y, 16);
         this.newPos.x -= modX;
         this.newPos.y -= modY;
         this.currOffset.x += modX;
         this.currOffset.y += modY;
         this.prevPos.set(this.newPos);
         this.startPos = this.newPos.cpy();
         this.endPos = this.startPos.cpy().add(this.pixmap.getWidth(), this.pixmap.getHeight());
         this.fogPositions1.clear();
         this.fogPositions2.clear();
         int scale = 16;
         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            if (Math.floorMod((int)currPos.x / 8, 2) == Math.floorMod((int)currPos.y / 16, 2)) {
               Vector2 rotate = currPos.cpy().rotateDeg(0.0F);
               int offsetY = (int)(Math.abs(rotate.y / scale) % 14.0F) - 7;
               int offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
               offsetX = Math.abs(offsetX);
               offsetY = Math.abs(offsetY);
               int offset1 = offsetX + offsetY;
               offsetY = Math.abs((int)(Math.abs(rotate.y / scale) % 20.0F) - 10);
               offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
               int offset2 = offsetX + offsetY;
               offset2 /= 5;
               offset2 = offset1 * offset2;
               if (offset2 > 17) {
                  int xPos = (int)(currPos.x + offset2 % 3 - 1.0F - this.startPos.x);
                  int yPos = this.pixmap.getHeight() - (int)(currPos.y + offset1 % 3 - 1.0F - this.startPos.y);
                  this.pixmap.drawPixmap(this.smokePixmap1, xPos, yPos);
               } else if (offset2 > 9) {
                  int xPos = (int)(currPos.x + offset2 % 3 - 1.0F - this.startPos.x);
                  int yPos = this.pixmap.getHeight() - (int)(currPos.y + offset1 % 3 - 1.0F - this.startPos.y);
                  this.pixmap.drawPixmap(this.smokePixmap2, xPos, yPos);
               }
            }

            currPos.x += 4.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 8.0F;
            }
         }

         this.texture.draw(this.pixmap, 0, 0);
      }

      this.prevColor.set(game.mapBatch.getColor());
      game.mapBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.b, 0.5F);
      game.mapBatch.draw(this.texture, this.prevPos.x + this.currOffset.x, this.prevPos.y + this.currOffset.y);
      this.timer++;
      if (this.timer >= 20) {
         this.currOffset.add(2.0F, 2.0F);
         effectArea.x += 2.0F;
         effectArea.y += 2.0F;
         if (!effectArea.contains(game.player.position)) {
            refresh = true;
            System.out.println("FogEffect refresh");
            System.out.println(effectArea.x);
            System.out.println(effectArea.y);
         }

         this.timer = 0;
      }

      game.mapBatch.setColor(this.prevColor);
   }

   public void smokeStep(Game game) {
      if (refresh) {
         this.positionsQueue.clear();
         Vector2 centerPos = game.player.position.cpy();
         centerPos.x = centerPos.x - Math.floorMod((int)centerPos.x, 16);
         centerPos.y = centerPos.y - Math.floorMod((int)centerPos.y, 16);
         this.startPos = centerPos.cpy().add(-this.pixmap.getWidth() / 2, -this.pixmap.getHeight() / 2);
         this.startPos.x = this.startPos.x - Math.floorMod((int)this.startPos.x, 16);
         this.startPos.y = this.startPos.y - Math.floorMod((int)this.startPos.y, 16);
         this.endPos = centerPos.cpy().add(this.pixmap.getWidth() / 2, this.pixmap.getHeight() / 2);
         this.endPos.x = this.endPos.x - Math.floorMod((int)this.endPos.x, 16);
         this.endPos.y = this.endPos.y - Math.floorMod((int)this.endPos.y, 16);
         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            this.positionsQueue.add(currPos.cpy());
            currPos.x += 4.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 8.0F;
            }
         }

         effectArea.setCenter(centerPos);
         this.pixmap.fill();
         refresh = false;
         this.fogPositions1.clear();
         this.fogPositions2.clear();
         if (this.prevType != type) {
            this.prevType = type;
            this.texture.draw(this.pixmap, 0, 0);
         }
      }

      for (int i = 0; !this.positionsQueue.isEmpty() && i < 768; i++) {
         int scale = 16;
         Vector2 currPos = this.positionsQueue.remove(0);
         if (Math.floorMod((int)currPos.x / 8, 2) == Math.floorMod((int)currPos.y / 16, 2)) {
            Vector2 rotate = currPos.cpy().rotateDeg(0.0F);
            int offsetY = (int)Math.abs(rotate.y / scale) % 14 - 7;
            int offsetX = (int)Math.abs(rotate.x / scale - rotate.y / scale) % 14 - 7;
            offsetX = Math.abs(offsetX);
            offsetY = Math.abs(offsetY);
            int offset1 = offsetX + offsetY;
            offsetY = Math.abs((int)Math.abs(rotate.y / scale) % 20 - 10);
            offsetX = Math.abs((int)Math.abs(rotate.x / scale) % 20 - 10);
            int offset2 = offsetX + offsetY;
            offset2 /= 5;
            offset2 = offset1 * offset2;
            if (offset2 > 17) {
               int xPos = (int)(currPos.x + offset2 % 3 - 1.0F - this.startPos.x);
               int yPos = this.pixmap.getHeight() - (int)(currPos.y + offset1 % 3 - 1.0F - this.startPos.y);
               this.pixmap.drawPixmap(this.smokePixmap1, xPos, yPos);
            } else if (offset2 > 9) {
               int xPos = (int)(currPos.x + offset2 % 3 - 1.0F - this.startPos.x);
               int yPos = this.pixmap.getHeight() - (int)(currPos.y + offset1 % 3 - 1.0F - this.startPos.y);
               this.pixmap.drawPixmap(this.smokePixmap2, xPos, yPos);
            }
         }

         if (this.positionsQueue.isEmpty()) {
            this.texture.draw(this.pixmap, 0, 0);
            effectArea.getCenter(this.prevPos);
         }
      }

      this.prevColor.set(game.mapBatch.getColor());
      game.mapBatch.setColor(this.prevColor.r, this.prevColor.g, this.prevColor.b, 0.5F);
      game.mapBatch
         .draw(
            this.texture,
            this.prevPos.x + 8.0F + 32.0F - this.pixmap.getWidth() / 2 + this.currOffset.x,
            this.prevPos.y + 8.0F - this.pixmap.getHeight() / 2 + this.currOffset.y
         );
      this.timer++;
      if (this.timer >= 151) {
         this.currOffset.add(2.0F, 2.0F);
         this.timer = 0;
      }

      game.mapBatch.setColor(this.prevColor);
   }

   class LightenScreen extends Action {
      Texture texture;
      Color prevColor = new Color();
      Matrix4 prevCombined = new Matrix4();

      public LightenScreen() {
         super();
         this.texture = TextureCache.get(Gdx.files.internal("battle/intro_frame6.png"));
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return Action.Layer.gui_0;
      }

      @Override
      public void step(Game game) {
         if (FogEffect.type != null && !game.map.timeOfDay.equals("night")) {
            this.prevColor.set(game.mapBatch.getColor());
            this.prevCombined.set(game.cam.combined);
            game.mapBatch.setProjectionMatrix(Game.identity);
            game.mapBatch.setColor(1.0F, 1.0F, 1.0F, 0.2F);

            for (int i = -1; i < 1; i++) {
               for (int j = -1; j < 1; j++) {
                  game.mapBatch.draw(this.texture, 256 * i, 256 * j);
               }
            }

            game.mapBatch.setColor(this.prevColor);
            game.mapBatch.setProjectionMatrix(this.prevCombined);
         }
      }
   }

   public enum Type {
      SMOKE,
      FOG,
      DEEPFOREST;
   }
}
