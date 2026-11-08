package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.ArrayList;

class ChangePlayerCharacter extends Action {
   int index = 0;
   ArrayList<String> characters = new ArrayList<>();

   public ChangePlayerCharacter(Action nextAction) {
      super();
      this.characters.add("leaf");
      this.characters.add("gold");
      this.characters.add("kris");
      this.characters.add("brendan");
      this.characters.add("may");
      this.characters.add("hilbert");
      this.characters.add("hilda");
      this.characters.add("rosa");
      this.characters.add("calem");
      this.characters.add("serena");
      this.characters.add("chase");
      this.characters.add("elaine");
      this.characters.add("gloria");
      this.characters.add("mark");
      this.characters.add("mint");
      this.characters.add("lunick");
      this.characters.add("summer");
      this.characters.add("kellyn");
      this.nextAction = nextAction;
      FileHandle directory = Gdx.files.local("mods/player/");
      FileHandle[] files = directory.list();

      for (FileHandle file : files) {
         if (file.isDirectory()) {
            String filename = file.name();
            if (!this.characters.contains(filename)) {
               this.characters.add(filename);
               System.out.println("Found mods for: " + filename);
            }
         }
      }
   }

   @Override
   public void firstStep(Game game) {
      this.index = 0;

      for (String character : this.characters) {
         if (character.equals(game.player.character)) {
            break;
         }

         this.index++;
      }

      if (this.index >= this.characters.size()) {
         this.index = this.characters.size() - 1;
      }
   }

   @Override
   public void step(Game game) {
      if (InputProcessor.leftJustPressed) {
         this.index--;
         if (this.index < 0) {
            this.index = this.characters.size() - 1;
         }

         game.player.character = this.characters.get(this.index);
         game.player.setColor(game.player.color);
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      } else if (InputProcessor.rightJustPressed) {
         this.index++;
         if (this.index >= this.characters.size()) {
            this.index = 0;
         }

         game.player.character = this.characters.get(this.index);
         game.player.setColor(game.player.color);
         game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
      }

      if (InputProcessor.bJustPressed) {
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }
   }
}
