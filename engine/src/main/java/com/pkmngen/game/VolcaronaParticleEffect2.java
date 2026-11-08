package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;

class VolcaronaParticleEffect2 extends Action {
   public Action.Layer layer = Action.Layer.gui_106;
   ParticleEffect fireEffect = new ParticleEffect();
   ParticleEffect fireEffect2 = new ParticleEffect();
   public int timer = 0;

   public VolcaronaParticleEffect2(Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.fireEffect.load(Gdx.files.internal("fire1.p"), Gdx.files.internal(""));
      this.fireEffect2.load(Gdx.files.internal("fire1-flipped.p"), Gdx.files.internal(""));
   }

   @Override
   public void firstStep(Game game) {
      this.fireEffect.setPosition(-40.0F, 48.0F);
      this.fireEffect.scaleEffect(6.0F);
      this.fireEffect.start();
      this.fireEffect2.setPosition(40.0F, 48.0F);
      this.fireEffect2.scaleEffect(6.0F);
      this.fireEffect2.start();
   }

   @Override
   public void step(Game game) {
      this.fireEffect.update(Gdx.graphics.getDeltaTime() * 0.75F);
      this.fireEffect2.update(Gdx.graphics.getDeltaTime() * 0.75F);
      this.fireEffect.draw(game.uiBatch);
      this.fireEffect2.draw(game.uiBatch);
      this.timer = (this.timer + 1) % 2;
   }
}
