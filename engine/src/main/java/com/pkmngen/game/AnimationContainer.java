package com.pkmngen.game;

import java.util.ArrayList;

class AnimationContainer<E> {
   ArrayList<E> animateThese = new ArrayList<>();
   ArrayList<Integer> numFrames = new ArrayList<>();
   int index = 0;

   public AnimationContainer() {
   }

   public void add(E thing, int numFrames) {
      this.animateThese.add(thing);
      this.numFrames.add(numFrames);
   }

   public int currentFrame() {
      return this.numFrames.get(this.index);
   }

   public E currentThing() {
      return this.animateThese.get(this.index);
   }
}
