package com.pkmngen.game;

class MazeNode {
   int x;
   int y;
   boolean leftOpen;
   boolean downOpen;
   boolean[] isOpen;
   int size;
   String type;
   String rampLoc;

   public MazeNode(int x, int y, boolean[] isOpen, int size) {
      this.x = x;
      this.y = y;
      this.leftOpen = isOpen[0];
      this.downOpen = isOpen[1];
      this.isOpen = isOpen;
      this.size = size;
      this.type = "";
      this.rampLoc = "";
   }
}
