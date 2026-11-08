package com.pkmngen.game;

import java.util.HashMap;

class PickupItem extends Action {
   HashMap<String, Integer> items;
   String whichItem;

   public PickupItem(HashMap<String, Integer> items, String whichItem, Action nextAction) {
      super();
      this.items = items;
      this.whichItem = whichItem;
      this.nextAction = nextAction;
   }

   @Override
   public void step(Game game) {
      game.insertAction(new PlayMusic("seed1", null));
      HashMap<String, Integer> items = new HashMap<>();
      this.items.remove(this.whichItem);
      if (this.whichItem.equals("torch")) {
         items.put("log", 1);
         items.put("grass", 1);
         game.insertAction(new DrawItemPickup(items, null));
      }

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
