package com.pkmngen.game.util.pool;

import com.badlogic.gdx.math.Vector2;

public final class ObjectPools {
   public static final AutoPool<Vector2> vectors = new ObjectPools.Vectors();

   private static class Vectors extends AutoPool<Vector2> {
      public Vectors() {
         super(Vector2.class);
      }
   }
}
