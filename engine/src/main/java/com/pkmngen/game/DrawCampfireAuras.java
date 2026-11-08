package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

class DrawCampfireAuras extends Action {
   public Action.Layer layer = Action.Layer.map_110;
   Texture[] campfireTextures = new Texture[2];
   Texture[] torchTextures = new Texture[2];
   int campfireTimer = 0;
   float xPos;
   float yPos;
   Texture campfireTexture;
   Texture torchTexture;
   Color color = new Color();

   public DrawCampfireAuras(Game game) {
      super();
      this.campfireTextures[0] = TextureCache.get(Gdx.files.internal("fire_mask3.png"));
      this.campfireTextures[1] = TextureCache.get(Gdx.files.internal("fire_mask4.png"));
      this.torchTextures[0] = TextureCache.get(Gdx.files.internal("torch_mask1.png"));
      this.torchTextures[1] = TextureCache.get(Gdx.files.internal("torch_mask2.png"));
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (game.map != null && game.map.tiles != null) {
         this.color.set(game.mapBatch.getColor());
         game.mapBatch.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
         this.campfireTexture = this.campfireTextures[1];
         this.torchTexture = this.torchTextures[1];
         if (this.campfireTimer % 20 < 10) {
            this.campfireTexture = this.campfireTextures[0];
            this.torchTexture = this.torchTextures[0];
         }

         for (int i = 0; i < game.map.onscreenTiles.size(); i++) {
            Tile tile = game.map.onscreenTiles.get(i);
            if (tile.nameUpper.equals("campfire1")) {
               this.xPos = tile.position.x + 8.0F - this.campfireTexture.getWidth() / 2.0F;
               this.yPos = tile.position.y + 8.0F - this.campfireTexture.getHeight() / 2.0F;
               game.mapBatch.draw(this.campfireTexture, this.xPos, this.yPos);
            } else if (tile.isTorch) {
               this.xPos = tile.position.x + 8.0F - this.torchTexture.getWidth() / 2.0F;
               this.yPos = tile.position.y + 8.0F - this.torchTexture.getHeight() / 2.0F;
               game.mapBatch.draw(this.torchTexture, this.xPos, this.yPos);
            } else if (tile.nameUpper.equals("volcarona")) {
               this.xPos = tile.position.x + 8.0F - this.campfireTexture.getWidth() / 2.0F;
               this.yPos = tile.position.y + 8.0F - this.campfireTexture.getHeight() / 2.0F;
               game.mapBatch.draw(this.campfireTexture, this.xPos, this.yPos);
            } else if (tile.isLava) {
               this.xPos = tile.position.x + 8.0F - this.torchTexture.getWidth() / 2.0F;
               this.yPos = tile.position.y + 8.0F - this.torchTexture.getHeight() / 2.0F;
               game.mapBatch.draw(this.torchTexture, this.xPos, this.yPos);
            }
         }

         for (int i = 0; i < game.map.onscreenPokemon.size(); i++) {
            Pokemon pokemon = game.map.onscreenPokemon.get(i);
            if (pokemon != null && pokemon.mapTiles == game.map.tiles && pokemon.hms.contains("FLASH")) {
               this.xPos = pokemon.position.x + 8.0F - this.campfireTexture.getWidth() / 2.0F;
               this.yPos = pokemon.position.y + 8.0F - this.campfireTexture.getHeight() / 2.0F;
               game.mapBatch.draw(this.campfireTexture, this.xPos, this.yPos);
            }
         }

         if (game.player.hmPokemon != null && game.player.hmPokemon.currOwSprite != null && game.player.hmPokemon.hms.contains("FLASH")) {
            Vector2 position = game.player.hmPokemon.position;
            if (!game.player.currFieldMove.equals("")) {
               position = game.player.position;
            }

            this.xPos = position.x + 8.0F - this.campfireTexture.getWidth() / 2.0F;
            this.yPos = position.y + 8.0F - this.campfireTexture.getHeight() / 2.0F;
            game.mapBatch.draw(this.campfireTexture, this.xPos, this.yPos);
         }

         if (this.campfireTimer < 79) {
            this.campfireTimer++;
         } else {
            this.campfireTimer = 0;
         }

         game.mapBatch.setColor(this.color);
      }
   }
}
