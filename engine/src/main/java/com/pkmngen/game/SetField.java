package com.pkmngen.game;

class SetField extends Action {
   Object object;
   String field;
   Object setTo;

   public SetField(Object object, String field, Object setTo, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.object = object;
      this.field = field;
      this.setTo = setTo;
   }

   @Override
   public void step(Game game) {
      if (this.object != null) {
         try {
            this.object.getClass().getField(this.field).set(this.object, this.setTo);
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
         } catch (IllegalAccessException e) {
            e.printStackTrace();
         } catch (NoSuchFieldException e) {
            e.printStackTrace();
         } catch (SecurityException e) {
            e.printStackTrace();
         }
      }

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
