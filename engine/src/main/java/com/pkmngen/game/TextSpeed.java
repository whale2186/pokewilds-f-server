package com.pkmngen.game;

public enum TextSpeed {
   SLOW(3),
   MID(2),
   FAST(1),
   INST(0);

   public final int framesToWait;

   TextSpeed(int framesToWait) {
      this.framesToWait = framesToWait;
   }

   public static TextSpeed parse(String raw) {
      switch (raw) {
         case "slow":
            return SLOW;
         case "fast":
            return FAST;
         case "inst":
            return INST;
         default:
            return MID;
      }
   }
}
