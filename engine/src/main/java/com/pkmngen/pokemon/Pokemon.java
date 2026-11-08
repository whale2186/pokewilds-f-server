package com.pkmngen.pokemon;

public class Pokemon {
   private final int dexNumber;
   private final String id;
   private final String name;
   private final float weight;
   private final int catchRate;
   private final Aggressivity aggressivity;
   private final boolean waterOnly;
   private final boolean skittish;

   public Pokemon(int dexNumber, String id, String name, float weight, int catchRate, Aggressivity aggressivity, boolean waterOnly, boolean skittish) {
      this.dexNumber = dexNumber;
      this.id = id;
      this.name = name;
      this.weight = weight;
      this.catchRate = catchRate;
      this.aggressivity = aggressivity;
      this.waterOnly = waterOnly;
      this.skittish = skittish;
   }

   public static Pokemon.Builder createBuilder(int dexNumber, String id, String name) {
      return new Pokemon.Builder(dexNumber, id, name);
   }

   public int getDexNumber() {
      return this.dexNumber;
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   private float getWeight() {
      return this.weight;
   }

   public int getCatchRate() {
      return this.catchRate;
   }

   public Aggressivity getAggressivity() {
      return this.aggressivity;
   }

   public boolean isWaterOnly() {
      return this.waterOnly;
   }

   public boolean isSkittish() {
      return this.skittish;
   }

   public static class Builder {
      private final int dexNumber;
      private final String id;
      private final String name;
      private float weight = 0.0F;
      private int catchRate = 0;
      private Aggressivity aggressivity = Aggressivity.NORMAL;
      private boolean waterOnly = false;
      private boolean skittish = false;

      public Builder(int dexNumber, String id, String name) {
         this.dexNumber = dexNumber;
         this.id = id;
         this.name = name;
      }

      public Pokemon.Builder weight(float weight) {
         this.weight = weight;
         return this;
      }

      public Pokemon.Builder catchRate(int catchRate) {
         this.catchRate = catchRate;
         return this;
      }

      public Pokemon.Builder aggressivity(Aggressivity aggressivity) {
         this.aggressivity = aggressivity;
         return this;
      }

      public Pokemon.Builder waterOnly(boolean waterOnly) {
         this.waterOnly = waterOnly;
         return this;
      }

      public Pokemon.Builder skittish(boolean skittish) {
         this.skittish = skittish;
         return this;
      }

      public Pokemon build() {
         return new Pokemon(this.dexNumber, this.id, this.name, this.weight, this.catchRate, this.aggressivity, this.waterOnly, this.skittish);
      }
   }
}
