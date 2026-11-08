package com.pkmngen.game;

public class Synced {
   static class HashMap<T, U> {
      public Player player;
      String objectName;
      java.util.HashMap<T, U> object = new java.util.HashMap<>();
      U response;

      public HashMap(String objectName) {
         this.objectName = objectName;
      }

      public U get(T key) {
         Game game = Game.staticGame;
         if (game.type == Game.Type.SERVER) {
            return this.object.get(key);
         }

         Game.staticGame.client.sendTCP(new Network.SyncedHashMap(this.objectName, "get", null));

         while (this.response == null) {
            try {
               Thread.sleep(1L);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }

         U response = this.response;
         this.response = null;
         return response;
      }

      public void put(T key, U val) {
         this.object.put(key, val);
      }
   }
}
