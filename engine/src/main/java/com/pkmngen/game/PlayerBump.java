package com.pkmngen.game;

class PlayerBump extends Action {
   public Action.Layer layer = Action.Layer.map_130;
   int timer = 0;
   int maxTime = 10;
   boolean alternate = false;
   public static boolean alternate2 = false;
   Player player;

   public PlayerBump(Game game) {
      this(game, game.player);
   }

   public PlayerBump(Game game, Player player) {
      super();
      this.player = player;
      game.insertAction(new PlayMusic("bump2", null));
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   public void localStep(Game game) {
      this.timer++;
      if (this.timer >= 2 * this.maxTime) {
         this.alternate = !this.alternate;
         this.timer = 0;
         game.insertAction(new PlayMusic("bump2", null));
      }

      if (this.timer < this.maxTime) {
         if (this.alternate) {
            game.player.currSprite = game.player.altMovingSprites.get(game.player.dirFacing);
         } else {
            game.player.currSprite = game.player.movingSprites.get(game.player.dirFacing);
         }
      } else {
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      }

      if (!InputProcessor.upPressed && game.player.dirFacing.equals("up")) {
         game.insertAction(new PlayerStanding(game));
         game.actionStack.remove(this);
      } else if (!InputProcessor.downPressed && game.player.dirFacing.equals("down")) {
         game.insertAction(new PlayerStanding(game));
         game.actionStack.remove(this);
      } else if (!InputProcessor.leftPressed && game.player.dirFacing.equals("left")) {
         game.insertAction(new PlayerStanding(game));
         game.actionStack.remove(this);
      } else if (!InputProcessor.rightPressed && game.player.dirFacing.equals("right")) {
         game.insertAction(new PlayerStanding(game));
         game.actionStack.remove(this);
      }
   }

   public void remoteStep(Game game) {
      this.player.network.syncTimer++;
      this.timer++;
      if (this.timer < this.maxTime) {
         if (this.alternate) {
            this.player.currSprite = this.player.altMovingSprites.get(this.player.dirFacing);
         } else {
            this.player.currSprite = this.player.movingSprites.get(this.player.dirFacing);
         }
      } else {
         this.player.currSprite = this.player.standingSprites.get(this.player.dirFacing);
      }

      if (this.timer >= 2 * this.maxTime || this.player.network.shouldMove) {
         this.player.standingAction.alternate = true;
         this.player.standingAction.isRunning = false;
         game.insertAction(this.player.standingAction);
         game.actionStack.remove(this);
      }
   }

   @Override
   public void step(Game game) {
      if (this.player.type == Player.Type.LOCAL) {
         this.localStep(game);
      } else if (this.player.type == Player.Type.REMOTE) {
         this.remoteStep(game);
      }
   }
}
