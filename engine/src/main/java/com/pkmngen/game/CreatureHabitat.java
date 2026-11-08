package com.pkmngen.game;

import java.util.ArrayList;
import java.util.List;

/** Pure species habitat and harvest rules, shared by graphical and dedicated simulation. */
final class CreatureHabitat {
    final List<String> types;
    final ArrayList<String> harvestables = new ArrayList<>();
    final ArrayList<String> habitats = new ArrayList<>();
    int harvestTimerMax = 9000;
    CreatureHabitat(String name, List<String> types, Iterable<String> evolutionStones) {
        this.types = types;
      this.harvestables.clear();
      this.habitats.clear();
      if (this.types.contains("BUG")) {
         this.habitats.add("flower|potted");
         this.harvestables.add("silky thread");
      }

      if (this.types.contains("WATER")) {
         this.habitats.add("water");
         this.harvestables.add("hard shell");
      }

      if (this.types.contains("FLYING")) {
         this.habitats.add("tree|bush");
         this.harvestables.add("soft feather");
      }

      if (this.types.contains("ROCK")) {
         this.habitats.add("rock|statue|gym");
         this.harvestables.add("hard stone");
      }

      if (this.types.contains("FIRE")) {
         this.habitats.add("campfire|lava|soot|volcano");
         this.harvestables.add("charcoal");
      }

      if (this.types.contains("GRASS")) {
         this.habitats.add("grass");
         this.harvestables.add("miracle seed");
      }

      if (this.types.contains("POISON")) {
         this.harvestables.add("poison barb");
      }

      if (this.types.contains("GROUND")) {
         this.habitats.add("mountain|sand|desert");
         this.harvestables.add("soft sand");
      }

      if (this.types.contains("STEEL")) {
         this.habitats.add("mountain");
         this.harvestables.add("metal coat");
      }

      if (this.types.contains("PSYCHIC")) {
         this.harvestables.add("psi energy");
      }

      if (this.types.contains("DARK")) {
         this.harvestables.add("dark energy");
      }

      if (this.types.contains("GHOST")) {
         this.habitats.add("gravestone|green12");
         this.harvestables.add("life force");
      }

      if (this.types.contains("FIGHTING")) {
         this.harvestables.add("binding band");
      }

      if (this.types.contains("FAIRY")) {
         this.harvestables.add("stardust");
      }

      if (name.equals("dratini") || name.equals("dragonair") || name.equals("dragonite")) {
         this.habitats.clear();
         this.habitats.add("water");
         this.harvestables.clear();
      }

      if (this.types.contains("DRAGON")) {
         this.harvestables.add("dragon fang");
         this.harvestables.add("dragon scale");
         this.harvestables.add("dragon scale");
         this.harvestables.add("dragon scale");
         this.harvestables.add("dragon scale");
      }

      if (this.types.contains("ICE")) {
         this.habitats.add("snow");
         this.harvestables.add("nevermeltice");
      }

      if (this.types.contains("ELECTRIC")) {
         this.harvestables.add("magnet");
      }

      if (name.equals("mareep") || name.equals("flaaffy") || name.equals("ampharos")) {
         this.harvestables.clear();
         this.harvestables.add("soft wool");
      } else if (name.equals("beedrill") || name.contains("combee") || name.equals("vespiquen") || name.equals("cutiefly") || name.equals("ribombee")) {
         this.harvestables.clear();
         this.harvestables.add("honey");
      } else if (name.equals("miltank")) {
         this.harvestables.clear();
         this.harvestables.add("moomoo milk");
      } else if (name.equals("delibird")) {
         this.harvestables.clear();
         this.harvestables.add("nevermeltice");
         this.harvestables.add("magnet");
         this.harvestables.add("ancientpowder");
         this.harvestables.add("soft wool");
         this.harvestables.add("moomoo milk");
         this.harvestables.add("hard stone");
         this.harvestables.add("manure");
         this.harvestables.add("berry juice");
         this.harvestables.add("soft sand");
         this.harvestables.add("dragon fang");
         this.harvestables.add("dragon scale");
         this.harvestables.add("life force");
         this.harvestables.add("psi energy");
         this.harvestables.add("dark energy");
         this.harvestables.add("metal coat");
         this.harvestables.add("silky thread");
         this.harvestables.add("hard shell");
         this.harvestables.add("soft feather");
         this.harvestables.add("charcoal");
         this.harvestables.add("grass");
         this.harvestables.add("honey");
      } else if (name.contains("unown")) {
         this.harvestables.clear();
         this.harvestables.add("ancientpowder");
         this.harvestTimerMax = 12600;
      } else if (name.contains("regi")) {
         this.harvestables.add("ancientpowder");
      } else if (name.equals("mewtwo")) {
         this.harvestables.add("ancientpowder");
      } else if (name.equals("shuckle")) {
         this.harvestables.clear();
         this.harvestables.add("berry juice");
      } else if (name.equals("sableye")) {
         this.harvestables.clear();

         for (String itemName : evolutionStones) {
            this.harvestables.add(itemName);
            this.harvestables.add("dark energy");
            this.harvestables.add("life force");
         }
      } else if (name.equals("applin") || name.equals("appletun")) {
         this.harvestables.add("sweet apple");
         this.harvestables.add("sweet apple");
         this.harvestables.add("sweet apple");
      } else if (name.equals("staryu") || name.equals("starmie")) {
         this.harvestables.add("star piece");
         this.harvestables.add("star piece");
      }

      if (this.habitats.size() <= 0) {
         this.habitats.add("green");
      }

      if (this.harvestables.size() <= 0) {
         this.harvestables.add("manure");
      }
    }
}
