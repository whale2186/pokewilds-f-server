package com.pkmngen.game.util;

import java.util.HashMap;
import java.util.Map;

public enum Direction {
   UP(0),
   LEFT(2),
   DOWN(4),
   RIGHT(6),
   N(0),
   E(2),
   S(4),
   W(6),
   NE(1),
   SE(3),
   SW(5),
   NW(7);

   final int id;
   public static final Map<Integer, Direction> oppDirs = new HashMap<>();

   Direction(int id) {
      this.id = id;
      this.init();
   }

   public Direction init() {
      System.out.println("constructor was called, hi there");
      System.out.println(this);
      return this;
   }

   public int getValue() {
      return this.id;
   }

   public boolean equals(Direction direction) {
      return this.getValue() == direction.getValue();
   }

   public Direction oppDir() {
      return oppDirs.get(this.getValue());
   }

   static {
      for (Direction direction : values()) {
         oppDirs.put((direction.getValue() + 4) % 8, direction);
      }
   }
}
