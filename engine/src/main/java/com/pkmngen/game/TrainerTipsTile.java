package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;

class TrainerTipsTile extends Tile {
   public static ArrayList<String> messages = new ArrayList<>();
   String message = "";
   boolean isUnown;

   public TrainerTipsTile(Vector2 pos, Route route) {
      this(pos, route, false, "");
   }

   public TrainerTipsTile(Vector2 pos, Route route, boolean isUnown, String message) {
      super("sand1", "sign1", pos, true, route);
      this.isUnown = isUnown;
      this.message = message;
      if (this.message.equals("")) {
         if (messages.size() <= 0) {
            initMessages();
         }

         this.message = messages.get(Game.staticGame.map.rand.nextInt(messages.size()));
         messages.remove(this.message);
      }
   }

   public static void initMessages() {
      messages.clear();
      messages.add("Stand still while holding X to stop using a Field Move.");
      messages.add("You can craft POKéBALLS out of Apricorns at a campfire.");
      messages.add("If you white out during battle, you will return to the last place you used a sleeping bag.");
      messages.add("Using your sleeping bag will slowly restore your party' hp.");
      messages.add("Ghosts may appear in the woods at night. A campfire will ward them off.");
      messages.add("Use CUT on trees and tall grass to get building materials.");
      messages.add("POKéMON will lay EGGS when located near a compatible mate.");
      messages.add("Stand still while holding X to stop using a Field Move.");
      messages.add("Build fences to prevent your POKéMON from wandering off when you let them out of their POKéBALL.");
      messages.add("You can build a door between two roof tiles to build a back door to your house.");
      messages.add("Sleeping indoors will restore hp twice as fast as sleeping outdoors.");
      messages.add("Use CUT on buildings to remove them.");
      messages.add("Your POKéMON will be happier if fenced in and located near a shelter.");
      messages.add("POKéMON are happier when located in their natural habitat. If they are happy enough, they may give you items!");
      messages.add("Sleeping in a bed will over time cure your POKéMON' status conditions.");
      messages.add("Be wary of taking EGGS near wild POKéMON. Angry parents may attack!");
      messages.add("Use the escape rope to return to the nearest location on the shore.");
      messages.add("You can plant Miracle Seeds and Apricorns to grow grass and trees.");
      messages.add("You can build a gate by building a door between two fences.");
   }

   @Override
   public void onPressA(Game game) {
      game.playerCanMove = false;
      if (this.isUnown) {
         game.musicController.unownMusic = true;
         if (!Game.musicDisabled) {
            game.currMusic.pause();
            if (!game.loadedMusic.containsKey("unown1")) {
               game.loadedMusic.put("unown1", AudioLoader.loadMusic("music/unown1.ogg"));
            }

            game.currMusic = game.loadedMusic.get("unown1");
            game.currMusic.stop();
            game.currMusic.setLooping(true);
            game.currMusic.setVolume(1.0F);
            game.currMusic.play();
         }

         DisplayText.unownText = true;
         this.isUnown = false;
      }

      game.insertAction(
         new DisplayText(
            game,
            "TRAINER TIPS!    ",
            null,
            null,
            new DisplayText.Clear(
               game, new DisplayText(game, this.message, null, null, new WaitFrames(game, 3, new SetField(game, "playerCanMove", true, null)))
            )
         )
      );
   }

   static {
      initMessages();
   }
}
