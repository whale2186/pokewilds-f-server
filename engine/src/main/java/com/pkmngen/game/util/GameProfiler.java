package com.pkmngen.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

public class GameProfiler {
   private final GLProfiler profiler = new GLProfiler(Gdx.graphics);
   private final boolean enabled;

   public GameProfiler(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         Gdx.app.setLogLevel(3);
         this.profiler.enable();
      }
   }

   public void reset() {
      if (this.enabled) {
         this.profiler.reset();
      }
   }

   public void logGl(Object owner) {
      if (this.enabled && Gdx.input.isKeyPressed(44)) {
         Gdx.app.debug("Profiler", "=*=*=");
         Gdx.app.debug("Profiler", owner.getClass().getSimpleName());
         Gdx.app.debug("Profiler", "Texture bindings: " + this.profiler.getTextureBindings());
         Gdx.app.debug("Profiler", "Draw calls: " + this.profiler.getDrawCalls());
         Gdx.app.debug("Profiler", "=====");
      }
   }
}
