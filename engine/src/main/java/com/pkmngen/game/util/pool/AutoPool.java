package com.pkmngen.game.util.pool;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class AutoPool<T> extends Pool<AutoPoolable<T>> {
   private final Pool<T> pool;

   public AutoPool(Class<T> type) {
      this.pool = Pools.get(type, 15);
   }

   public AutoPoolable<T> obtain() {
      AutoPoolable<T> poolable = (AutoPoolable<T>)super.obtain();
      poolable.set(this.pool.obtain());
      return poolable;
   }

   protected AutoPoolable<T> newObject() {
      return new AutoPoolable<>(this, this.pool);
   }
}
