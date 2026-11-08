package com.pkmngen.game.util.pool;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class AutoPoolable<T> implements AutoCloseable, Poolable {
   private final Pool<AutoPoolable<T>> pool;
   private final Pool<T> tPool;
   private T t;

   public AutoPoolable(Pool<AutoPoolable<T>> pool, Pool<T> tPool) {
      this.pool = pool;
      this.tPool = tPool;
   }

   @Override
   public void close() {
      this.pool.free(this);
   }

   public T get() {
      return this.t;
   }

   void set(T t) {
      this.t = t;
   }

   @Override
   public void reset() {
      this.tPool.free(this.t);
      this.t = null;
   }
}
