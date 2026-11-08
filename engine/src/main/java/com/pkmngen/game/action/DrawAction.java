package com.pkmngen.game.action;

import com.pkmngen.game.Action;
import com.pkmngen.game.Game;
import com.pkmngen.game.util.GameProfiler;

public abstract class DrawAction extends Action {
   private final GameProfiler profiler;

   protected DrawAction(GameProfiler profiler) {
      super();
      this.profiler = profiler;
   }

   protected abstract void draw(Game var1);

   @Override
   public final void step(Game game) {
      this.profiler.reset();
      this.draw(game);
      this.profiler.logGl(this);
   }
}
