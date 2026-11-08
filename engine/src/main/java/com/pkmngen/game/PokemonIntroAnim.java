package com.pkmngen.game;

import com.pkmngen.game.util.SpriteProxy;

class PokemonIntroAnim extends Action {
   public Action.Layer layer = Action.Layer.gui_140;
   int currFrame = 0;
   SpriteProxy originalSprite;
   boolean firstStep = true;

   public PokemonIntroAnim(Action nextAction) {
      super();
      this.nextAction = nextAction;
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (this.firstStep) {
         this.originalSprite = game.battle.oppPokemon.sprite;
         this.firstStep = false;
      }

      if (this.currFrame >= game.battle.oppPokemon.introAnim.size()) {
         game.battle.oppPokemon.sprite = this.originalSprite;
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      } else {
         game.battle.oppPokemon.sprite = game.battle.oppPokemon.introAnim.get(this.currFrame);
         game.battle.oppPokemon.sprite.setPosition(this.originalSprite.getX(), this.originalSprite.getY());
         this.currFrame++;
      }
   }
}
