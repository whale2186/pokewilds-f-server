package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import java.util.HashMap;

class SpecialMegaGengar1 extends Pokemon {
   public SpecialMegaGengar1(int level) {
      super("Mega Gengar", level);
      this.baseStats.put("hp", 300);
      this.baseStats.put("attack", 65);
      this.baseStats.put("defense", 80);
      this.baseStats.put("specialAtk", 170);
      this.baseStats.put("specialDef", 95);
      this.baseStats.put("speed", 130);
      this.baseStats.put("catchRate", 3);
      Texture pokemonText = TextureCache.get(Gdx.files.internal("pokemon/mgengar_base1.png"));
      this.breathingSprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
      pokemonText = TextureCache.get(Gdx.files.internal("pokemon/mgengar_over1.png"));
      this.sprite = new SpriteProxy(pokemonText, 0, 0, 56, 56);
      this.learnSet.put(1, new String[]{"Shadow Claw", "Night Shade", "Lick", null});
      this.types.add("Ghost");
      this.types.add("Poison");
      this.getCurrentAttacks();
      this.calcMaxStats();
      this.currentStats = new HashMap<>(this.maxStats);
      this.initHabitatValues();
   }
}
