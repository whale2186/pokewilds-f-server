package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;

class SpecialMewtwo1 extends Pokemon {
   Tile tile;

   public SpecialMewtwo1(int level, Tile tile) {
      super("mewtwo", level);
      this.tile = tile;
      if (this.isShiny) {
         Texture text = TextureCache.get(Gdx.files.internal("pokemon/mewtwo_special1_shiny.png"));
         this.sprite = new SpriteProxy(text, 0, 0, 56, 56);
         text = TextureCache.get(Gdx.files.internal("pokemon/mewtwo_special2_shiny.png"));
         this.breathingSprite = new SpriteProxy(text, 0, 0, 56, 56);
      } else {
         Texture text = TextureCache.get(Gdx.files.internal("pokemon/mewtwo_special1.png"));
         this.sprite = new SpriteProxy(text, 0, 0, 56, 56);
         text = TextureCache.get(Gdx.files.internal("pokemon/mewtwo_special2.png"));
         this.breathingSprite = new SpriteProxy(text, 0, 0, 56, 56);
      }

      this.attacks[0] = "psychic";
      this.attacks[1] = "recover";
      this.attacks[2] = "swift";
      this.attacks[3] = "disable";
      this.initHabitatValues();
   }
}
