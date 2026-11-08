package com.pkmngen.game;

import java.lang.reflect.Method;

class CallMethod extends Action {
   Object object;
   String method;
   Object[] params;
   Class<?>[] paramTypes;

   public CallMethod(Object object, String method, Class<?>[] paramTypes, Object[] params, Action nextAction) {
      super();
      this.object = object;
      this.method = method;
      this.params = params;
      this.paramTypes = paramTypes;
      this.nextAction = nextAction;
   }

   public CallMethod(Object object, String method, Object[] params, Action nextAction) {
      super();
      this.object = object;
      this.method = method;
      this.params = params;
      this.nextAction = nextAction;
      this.paramTypes = new Class[params.length];

      for (int i = 0; i < params.length; i++) {
         this.paramTypes[i] = params[i].getClass();
         if (this.paramTypes[i].getName().equals("java.lang.Float")) {
            this.paramTypes[i] = float.class;
         } else if (this.paramTypes[i].getName().equals("java.lang.Integer")) {
            this.paramTypes[i] = int.class;
         } else if (this.paramTypes[i].getName().equals("java.lang.Boolean")) {
            this.paramTypes[i] = boolean.class;
         } else if (this.paramTypes[i].getName().equals("java.lang.Long")) {
            this.paramTypes[i] = long.class;
         } else if (this.paramTypes[i].getName().equals("java.lang.Double")) {
            this.paramTypes[i] = double.class;
         }
      }
   }

   @Override
   public void step(Game game) {
      try {
         Method method = this.object.getClass().getMethod(this.method, this.paramTypes);
         method.invoke(this.object, this.params);
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
