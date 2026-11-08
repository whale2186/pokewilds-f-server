package com.pkmngen.game;

import java.util.function.Consumer;

class Do4<T> extends Action {
   Consumer<? super T> consumer;
   T val;

   public Do4(Consumer<? super T> consumer, T val, Action nextAction) {
      super();
      this.consumer = consumer;
      this.val = val;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      this.consumer.accept(this.val);
      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
