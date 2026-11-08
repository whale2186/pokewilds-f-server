package com.pkmngen.game;

import java.lang.reflect.Field;
import java.util.HashMap;

class SetStaticField extends Action {
   private static HashMap<String, Field> sFieldCache = new HashMap<>();
   Class<?> clazz;
   String fieldName;
   Object value;

   public SetStaticField(String clazz, String fieldName, Object value, Action nextAction) throws ClassNotFoundException {
      super();
      this.nextAction = nextAction;
      this.clazz = Class.forName(clazz);
      this.fieldName = fieldName;
      this.value = value;
   }

   @Override
   public void step(Game game) {
      try {
         Field field = getField(this.clazz, this.fieldName);
         field.set(this.clazz, this.value);
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (Throwable e) {
         e.printStackTrace();
      }

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }

   private static Field getField(Class<?> clazz, String fieldName) throws Throwable {
      String fieldFullName = genFieldFullName(clazz, fieldName);
      if (sFieldCache.containsKey(fieldFullName)) {
         return sFieldCache.get(fieldFullName);
      }

      Field field = null;

      try {
         field = clazz.getField(fieldName);
      } catch (NoSuchFieldException var7) {
      }

      if (field == null) {
         try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
         } catch (NoSuchFieldException var6) {
         }
      }

      if (field == null) {
         for (clazz = clazz.getSuperclass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
               field = clazz.getDeclaredField(fieldName);
               field.setAccessible(true);
            } catch (NoSuchFieldException var5) {
            }
         }
      }

      if (field == null) {
         String msg = "";
         msg = "Can't get Field from Class " + clazz.getSimpleName() + ":" + fieldName;
         throw new Throwable(msg);
      } else {
         sFieldCache.put(fieldFullName, field);
         return field;
      }
   }

   private static String genFieldFullName(Class<?> clazz, String fieldName) {
      StringBuilder name = new StringBuilder();
      name.append(clazz.getName());
      name.append(":");
      name.append(fieldName);
      return name.toString();
   }
}
