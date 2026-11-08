package com.pkmngen.game;

import java.util.function.Consumer;

class Do2<T> extends Action implements Consumer<T> {
   T val;

   public Do2(T val, Action nextAction) {
      super();
      this.val = val;
      this.nextAction = nextAction;
   }

   @Override
   public void accept(T t) {
   }

   @Override
   public void step(Game game) {
      this.accept(this.val);
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
