package com.pkmngen.game.util;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;

public class LoadingZone extends Rectangle {
   public LoadingZone inner;

   public LoadingZone() {
   }

   public LoadingZone(LoadingZone loadingZone) {
      super(loadingZone);
   }

   public Vector2 bottomLeft() {
      return new Vector2(this.x, this.y);
   }

   public Vector2 topRight() {
      return new Vector2(this.x + this.height, this.y + this.width);
   }

   public void translate(float x, float y) {
      Vector2 center = new Vector2();
      this.getCenter(center);
      center.add(x, y);
      this.setCenter(center);
      this.inner.setCenter(center);
   }

   public ArrayList<Vector2> diff(LoadingZone rect) {
      ArrayList<Vector2> allPositions = this.allPositions();

      for (Vector2 pos : new ArrayList<>(allPositions)) {
         if (rect.contains(pos.cpy().add(8.0F, 8.0F))) {
            allPositions.remove(pos);
         }
      }

      return allPositions;
   }

   public ArrayList<Vector2> allPositions() {
      ArrayList<Vector2> allPositions = new ArrayList<>();

      for (Vector2 position = this.bottomLeft(); position.y < this.topRight().y; position.add(16.0F, 0.0F)) {
         if (position.x > this.topRight().x) {
            position.add(0.0F, 16.0F);
            position.x = this.x - 16.0F;
         } else {
            allPositions.add(new Vector2(position));
         }
      }

      return allPositions;
   }

   public ArrayList<Object> getAll(HashMap<Vector2, Object> hashMap) {
      ArrayList<Object> objects = new ArrayList<>();

      for (Vector2 pos : hashMap.keySet()) {
         if (this.contains(pos)) {
            objects.add(hashMap.get(pos));
         }
      }

      return objects;
   }
}
