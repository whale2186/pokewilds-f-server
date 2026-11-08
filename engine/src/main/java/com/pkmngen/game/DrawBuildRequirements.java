package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.action.DrawAction;
import com.pkmngen.game.util.GameProfiler;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

class DrawBuildRequirements extends DrawAction {
   Sprite textBoxTop;
   Sprite textBoxMiddle;
   Sprite textBoxBottom;
   ArrayList<String> words = new ArrayList<>();
   ArrayList<Color> wordColors = new ArrayList<>();
   Vector2 topLeft = new Vector2(89.0F, 144.0F);
   public static String currBuilding = "";
   HashMap<String, String> namesMap = new HashMap<>();
   Color color = new Color();

   public DrawBuildRequirements(GameProfiler profiler) {
      super(profiler);
      Texture text = TextureCache.get(Gdx.files.internal("menu/selected_menu_top.png"));
      this.textBoxTop = new Sprite(text, 0, 0, 71, 19);
      text = TextureCache.get(Gdx.files.internal("menu/selected_menu_middle.png"));
      this.textBoxMiddle = new Sprite(text, 0, 0, 71, 16);
      text = TextureCache.get(Gdx.files.internal("menu/selected_menu_bottom.png"));
      this.textBoxBottom = new Sprite(text, 0, 0, 71, 19);
      this.namesMap.put("house5_roof_middle1", "Roof");
      this.namesMap.put("campfire1", "Fire");
      this.namesMap.put("fence1", "Fence");
      this.namesMap.put("house_bed1", "Bed");
      this.namesMap.put("house5_door1", "Door");
      this.namesMap.put("house5_middle1", "Wall");
      this.namesMap.put("bridge1", "Bridge");
      this.namesMap.put("house6_NEW", "Wall");
      this.namesMap.put("house6_roof_middle1", "Roof");
      this.namesMap.put("house6_door1", "Door");
      this.namesMap.put("fence2", "Fence");
      this.namesMap.put("potted1", "Flower");
      this.namesMap.put("potted2", "Flower");
      this.namesMap.put("potted3", "Flower");
      this.namesMap.put("potted4", "Flower");
      this.namesMap.put("statue1", "Statue");
      this.namesMap.put("house8_SEW", "Wall");
      this.namesMap.put("house8_roof_middle1", "Roof");
      this.namesMap.put("house8_door1", "Door");
      this.namesMap.put("house9_NEW", "Wall");
      this.namesMap.put("house9_roof_middle1", "Roof");
      this.namesMap.put("house9_door1", "Door");
      this.namesMap.put("torch1", "Torch");
      this.namesMap.put("house7_wall_S", "Wall");
      this.namesMap.put("house7_roof_EW", "Roof");
      this.namesMap.put("house7_wall_door1", "Door");
      this.namesMap.put("exteriorwindows1", "Window");
      this.namesMap.put("chimney1", "Chimney");
      this.namesMap.put("house_plant1", "Plant");
      this.namesMap.put("house_plant2", "Plant");
      this.namesMap.put("house_gym1", "Statue");
      this.namesMap.put("house_shelf1", "Shelf");
      this.namesMap.put("house_wardrobe1", "Dresser");
      this.namesMap.put("house_vanity1", "Vanity");
      this.namesMap.put("house_vanity2", "Vanity");
      this.namesMap.put("house_stool1", "Stool");
      this.namesMap.put("house_table1_default", "Table");
      this.namesMap.put("house_carpet1", "Floor");
      this.namesMap.put("house_carpet2", "Floor");
      this.namesMap.put("house_carpet3", "Floor");
      this.namesMap.put("house_carpet4", "Floor");
      this.namesMap.put("house_carpet5", "Floor");
      this.namesMap.put("house_carpet6", "Floor");
      this.namesMap.put("house_carpet7", "Floor");
      this.namesMap.put("house_carpet8", "Floor");
      this.namesMap.put("house_clock1", "Clock");
      this.namesMap.put("house_picture1", "Picture");
      this.namesMap.put("house_picture2", "Picture");
      this.namesMap.put("house_picture3", "Picture");
      this.namesMap.put("house_picture4", "Picture");
      this.namesMap.put("house_picture5", "Picture");
      this.namesMap.put("house_picture6", "Picture");
      this.namesMap.put("house_picture7", "Picture");
      this.namesMap.put("house_picture8", "Picture");
      this.namesMap.put("house_picture9", "Picture");
      this.namesMap.put("house_picture10", "Picture");
      this.namesMap.put("house_picture11", "Picture");
      this.namesMap.put("house_picture12", "Picture");
      this.namesMap.put("house_picture13", "Picture");
      this.namesMap.put("house_picture14", "Picture");
      this.namesMap.put("house_picture15", "Picture");
      this.namesMap.put("house_picture16", "Picture");
      this.namesMap.put("house_picture17", "Picture");
      this.namesMap.put("house_picture18", "Picture");
      this.namesMap.put("house_picture19", "Picture");
      this.namesMap.put("house_picture20", "Picture");
      this.namesMap.put("interiorwall1", "Wall");
      this.namesMap.put("house_couch1", "Couch");
      this.namesMap.put("house_couch2", "Couch");
      this.namesMap.put("house_couch3", "Couch");
      this.namesMap.put("house_couch4", "Couch");
      this.namesMap.put("house_couch5", "Couch");
      this.namesMap.put("house_trashcan1", "Trash");
      this.namesMap.put("house_plushpichu", "Doll");
      this.namesMap.put("house_plushduskull", "Doll");
      this.namesMap.put("house_plushskitty", "Doll");
      this.namesMap.put("house_plushjigglypuff", "Doll");
      this.namesMap.put("house_window1", "Window");
      this.namesMap.put("house_window2", "Window");
      this.namesMap.put("gravestone3", "Grave");
      this.namesMap.put("sign_built1", "Sign");
      this.namesMap.put("sign_desert1", "Sign");
      this.namesMap.put("warp_tile1", "Beacon");
      this.namesMap.put("chest1", "Storage");
      this.namesMap.put("stairs_up1", "Stairs");
      this.namesMap.put("ball_kiln1", "Kiln");
      this.namesMap.put("sand1", "Sand");
      this.namesMap.put("desert4", "Desert");
      this.namesMap.put("snow2", "Snow");
      this.namesMap.put("mountain1", "Clay");
      this.namesMap.put("green1", "Grass");
      this.namesMap.put("flower4", "Flowers");
      this.namesMap.put("volcano1", "Ashen");
      this.namesMap.put("green12", "Cursed");
      this.namesMap.put("green_savanna1", "Savanna");
      this.namesMap.put("ledges3_none", "Ledge");
      this.namesMap.put("green_deepforest", "Forest");
      this.namesMap.put("green_woodedlake", "Lake");
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public void firstStep(Game game) {
   }

   @Override
   protected void draw(Game game) {
      if (game.player.currFieldMove.equals("BUILD") || game.player.currFieldMove.equals("DIG")) {
         if (game.playerCanMove) {
            if (!game.player.currFieldMove.equals("DIG") || !game.player.currBuildTile.name.contains("hole")) {
               String curr = game.player.buildTiles.get(game.player.buildTileIndex).name;
               if (curr != null && !currBuilding.equals(curr)) {
                  currBuilding = curr;
                  this.words.clear();
                  this.wordColors.clear();
                  this.words.add("Need");
                  this.wordColors.add(new Color(1.0F, 1.0F, 1.0F, 1.0F));

                  for (String req : game.player.buildTileRequirements.get(curr).keySet()) {
                     int numNeeded = game.player.buildTileRequirements.get(curr).get(req);
                     String newReq = req.toUpperCase(Locale.ROOT);
                     if (newReq.equals("NEVERMELTICE")) {
                        newReq = "NVRMLT ICE";
                     }

                     String[] texts = newReq.split(" ");

                     for (int j = 0; j < texts.length; j++) {
                        String text = texts[j];
                        if (text.equals("BEDDING")) {
                           text = "BED";
                        }

                        if (j >= texts.length - 1) {
                           int length = text.length();

                           for (int i = 0; i < 5 - length; i++) {
                              text = text + " ";
                           }

                           text = text + "x";
                           text = text + String.valueOf(numNeeded);
                        }

                        if (game.player.hasItem(req) && game.player.getItemAmount(req) >= numNeeded) {
                           this.wordColors.add(new Color(1.0F, 1.0F, 1.0F, 1.0F));
                        } else {
                           this.wordColors.add(new Color(1.0F, 1.0F, 1.0F, 0.5F));
                        }

                        this.words.add(text);
                     }
                  }
               }

               for (int i = 0; i < this.words.size(); i++) {
                  if (i == 0) {
                     game.uiBatch.draw(this.textBoxTop, this.topLeft.x, this.topLeft.y - 19.0F);
                  } else if (i == this.words.size() - 1) {
                     game.uiBatch.draw(this.textBoxBottom, this.topLeft.x, this.topLeft.y - 19.0F - 16 * i);
                  } else {
                     game.uiBatch.draw(this.textBoxMiddle, this.topLeft.x, this.topLeft.y - 19.0F - 16 * i);
                  }

                  this.color.set(game.uiBatch.getColor());
                  if (i < this.words.size()) {
                     String word = this.words.get(i);

                     for (int j = 0; j < word.length(); j++) {
                        char letter = word.charAt(j);
                        Sprite letterSprite = new Sprite(game.textDict.get(letter));
                        letterSprite.setPosition(this.topLeft.x + 8.0F + 8 * j, this.topLeft.y - 14.0F - 16 * i);
                        game.uiBatch.setColor(this.color.r, this.color.g, this.color.b, this.wordColors.get(i).a);
                        game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
                     }
                  }

                  game.uiBatch.setColor(this.color);
               }

               game.uiBatch.draw(this.textBoxTop, 0.0F, 125.0F);
               game.uiBatch.draw(this.textBoxBottom, 0.0F, 119.0F);
               String name = this.namesMap.get(currBuilding);
               if (name == null) {
                  name = "null";
               }

               for (int j = 0; j < name.length(); j++) {
                  char letter = name.charAt(j);
                  Sprite letterSprite = new Sprite(game.textDict.get(letter));
                  letterSprite.setPosition(8 + 8 * j, 128.0F);
                  game.uiBatch.draw(letterSprite, letterSprite.getX(), letterSprite.getY());
               }
            }
         }
      }
   }
}
