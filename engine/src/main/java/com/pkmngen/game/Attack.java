package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.audio.AudioLoader;
import java.util.ArrayList;
import java.util.Locale;

class Attack {
   String name;
   String effect;
   String type;
   int accuracy;
   int pp;
   int effectChance;
   boolean isPhysical = false;
   int priority = 0;
   int power;
   int damage;
   boolean isCrit = false;
   public Attack.Category category = null;

   public Attack() {
   }

   public Attack(Attack attack) {
      this.name = attack.name;
      this.effect = attack.effect;
      this.power = attack.power;
      this.type = attack.type;
      this.accuracy = attack.accuracy;
      this.pp = attack.pp;
      this.effectChance = attack.effectChance;
      this.isCrit = attack.isCrit;
      this.damage = attack.damage;
      this.priority = attack.priority;
      this.isPhysical = attack.isPhysical;
      this.category = attack.category;
   }

   public Attack(String name, String effect, int power, String type, int accuracy, int pp, int effectChance) {
      this.name = name;
      this.effect = effect;
      this.power = power;
      if (!Game.fairyTypeEnabled && type.equals("FAIRY")) {
         type = "NORMAL";
      }

      this.type = type;
      this.accuracy = accuracy;
      this.pp = pp;
      this.effectChance = effectChance;
      if (this.name.equals("endure") || this.name.equals("protect") || this.name.equals("detect")) {
         this.priority = 2;
      } else if (this.effect.equals("EFFECT_PRIORITY_HIT")
         || this.name.equals("quick attack")
         || this.name.equals("mach punch")
         || this.name.equals("extremespeed")) {
         this.priority = 1;
      } else if (!this.name.equals("counter")
         && !this.name.equals("mirror coat")
         && !this.name.equals("whirlwind")
         && !this.name.equals("roar")
         && !this.name.equals("vital throw")) {
         this.priority = 0;
      } else {
         this.priority = -1;
      }

      if (Game.specialPhysicalSplitEnabled) {
         this.category = Battle.specPhysLookup.get(this.name.toLowerCase(Locale.ROOT).replace("_", " "));
         if (this.category == null) {
            System.out.println("WARNING - attack with no category (fix this): " + this.name);
         }

         this.isPhysical = this.category == Attack.Category.PHYSICAL;
      } else if (Battle.gen2PhysicalTypes.contains(this.type.toLowerCase(Locale.ROOT))) {
         this.isPhysical = true;
      }
   }

   public enum Category {
      SPECIAL,
      PHYSICAL,
      STATUS;
   }

   static class CrushGrip extends Action {
      public Action.Layer layer = Action.Layer.gui_109;
      public int timer = -40;
      boolean drawFriendly = false;
      Sprite sprite;
      Sprite sprite2;
      Music soundEffect;
      Music soundEffect2;
      Vector2 pos;

      public CrushGrip(Game game, Pokemon target, Action nextAction) {
         super();
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("attacks/crush_grip1.png"));
         this.sprite = new Sprite(text, 192, 0, 32, 32);
         text = TextureCache.get(Gdx.files.internal("attacks/crush_grip1.png"));
         this.sprite2 = new Sprite(text, 64, 0, 32, 32);
         if (target == game.battle.oppPokemon) {
            this.pos = new Vector2(86.0F, 82.0F);
            this.drawFriendly = false;
         } else {
            this.pos = new Vector2(54.0F, 62.0F);
            this.drawFriendly = true;
         }
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
      public void firstStep(Game game) {
         this.soundEffect = AudioLoader.loadMusic("attacks/rolling_kick_player_gsc/sound.ogg");
         this.soundEffect.setLooping(false);
         this.soundEffect.setVolume(1.0F);
         this.soundEffect2 = AudioLoader.loadMusic("attacks/rock_throw_player_gsc/sound.ogg");
         this.soundEffect2.setLooping(false);
         this.soundEffect2.setVolume(1.0F);
      }

      @Override
      public void step(Game game) {
         int offset = 0;
         if (this.timer >= -20) {
            if (this.timer < -10) {
               int friendly = 1;
               if (this.drawFriendly) {
                  friendly = -1;
               }

               this.pos.add(2 * friendly, 1 * friendly);
            } else if (this.timer >= 20) {
               if (this.timer < 166) {
                  if (this.timer % 2 == 0) {
                     offset = 2;
                  } else {
                     offset = -2;
                  }

                  game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(offset, 0.0F, 0.0F), new Quaternion(), new Vector3(1.0F, 1.0F, 1.0F)));
               } else if (this.timer >= 206) {
                  if (this.timer < 260) {
                     SpriteProxy.inverseColors = true;
                  } else {
                     SpriteProxy.inverseColors = false;
                     this.soundEffect.stop();
                     this.soundEffect.dispose();
                     this.soundEffect2.stop();
                     this.soundEffect2.dispose();
                     DrawFriendlyHealth.shouldDraw = true;
                     DrawEnemyHealth.shouldDraw = true;
                     game.actionStack.remove(this);
                     game.insertAction(this.nextAction);
                  }
               }
            }
         }

         if (this.timer >= 90) {
            if (this.timer < 98) {
               game.uiBatch.draw(this.sprite2, this.pos.x - offset - 8.0F, this.pos.y + 8.0F);
            } else if (this.timer >= 106) {
               if (this.timer < 114) {
                  game.uiBatch.draw(this.sprite2, this.pos.x - offset + 8.0F, this.pos.y + 8.0F);
               } else if (this.timer >= 122) {
                  if (this.timer < 130) {
                     game.uiBatch.draw(this.sprite2, this.pos.x - offset - 8.0F, this.pos.y - 8.0F);
                  } else if (this.timer >= 138 && this.timer < 146) {
                     game.uiBatch.draw(this.sprite2, this.pos.x - offset + 8.0F, this.pos.y - 8.0F);
                  }
               }
            }
         }

         if (this.timer == -20) {
            this.soundEffect.play();
         } else if (this.timer == 20) {
            this.soundEffect.stop();
            this.soundEffect.dispose();
            this.soundEffect2.play();
            this.sprite.setRegion(160, 0, 32, 32);
         } else if (this.timer == 70) {
            this.soundEffect2.pause();
         } else if (this.timer == 90) {
            this.soundEffect = AudioLoader.loadMusic("sounds/ap1.ogg");
            this.soundEffect.play();
         } else if (this.timer == 98) {
            this.sprite.setRegion(160, 0, 32, 32);
         } else if (this.timer == 106) {
            this.soundEffect.stop();
            this.soundEffect.play();
         } else if (this.timer == 114) {
            this.sprite.setRegion(160, 0, 32, 32);
         } else if (this.timer == 122) {
            this.soundEffect.stop();
            this.soundEffect.play();
         } else if (this.timer == 130) {
            this.sprite.setRegion(160, 0, 32, 32);
         } else if (this.timer == 138) {
            this.soundEffect.stop();
            this.soundEffect.play();
         } else if (this.timer == 166) {
            game.uiBatch.setTransformMatrix(new Matrix4(new Vector3(0.0F, 0.0F, 0.0F), new Quaternion(), new Vector3(1.0F, 1.0F, 1.0F)));
         } else if (this.timer == 206) {
            this.soundEffect2.play();
         }

         if (this.drawFriendly) {
            DrawEnemyHealth.shouldDraw = false;
         } else {
            DrawFriendlyHealth.shouldDraw = false;
         }

         game.uiBatch.draw(this.sprite, this.pos.x - offset, this.pos.y);
         this.timer++;
      }
   }

   public static class Default extends Action {
      ArrayList<Vector2> positions;
      Vector2 position;
      ArrayList<Sprite> sprites;
      Sprite sprite;
      ArrayList<Integer> repeats;
      ArrayList<Float> alphas;
      ArrayList<String> sounds;
      String sound;
      public Action.Layer layer = Action.Layer.gui_120;
      Sprite helperSprite;
      boolean doneYet;
      int power;
      int accuracy;

      public Default(Game game, int power, int accuracy, Action nextAction) {
         super();
         this.power = power;
         this.accuracy = accuracy;
         this.doneYet = false;
         this.nextAction = nextAction;
         this.position = new Vector2(game.player.currPokemon.backSprite.getX(), game.player.currPokemon.backSprite.getY());
         this.positions = new ArrayList<>();
         this.positions.add(new Vector2(8.0F, 0.0F));
         this.positions.add(new Vector2(-8.0F, 0.0F));

         for (int i = 0; i < 13; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.sprites = new ArrayList<>();

         for (int i = 0; i < 15; i++) {
            this.sprites.add(null);
         }

         this.repeats = new ArrayList<>();
         this.repeats.add(7);
         this.repeats.add(3);
         this.repeats.add(17);
         this.repeats.add(6);
         this.repeats.add(7);
         this.repeats.add(5);
         this.repeats.add(9);
         this.repeats.add(4);
         this.repeats.add(8);
         this.repeats.add(6);
         this.repeats.add(7);
         this.repeats.add(5);
         this.repeats.add(9);
         this.repeats.add(4);
         this.repeats.add(11);
         this.alphas = new ArrayList<>();
         this.alphas.add(1.0F);
         this.alphas.add(1.0F);
         this.alphas.add(1.0F);

         for (int i = 0; i < 6; i++) {
            this.alphas.add(0.0F);
            this.alphas.add(1.0F);
         }

         this.sounds = new ArrayList<>();
         this.sounds.add(null);
         this.sounds.add("tackle1");

         for (int i = 0; i < 13; i++) {
            this.sounds.add(null);
         }
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
         if (!this.positions.isEmpty() && !this.sprites.isEmpty()) {
            this.sprite = this.sprites.get(0);
            float currAlpha = this.alphas.get(0);
            game.battle.oppPokemon.sprite.setAlpha(currAlpha);
            if (game.battle.oppPokemon.breathingSprite != null) {
               game.battle.oppPokemon.breathingSprite.setAlpha(currAlpha);
            }

            this.sound = this.sounds.get(0);
            if (this.sound != null) {
               game.insertAction(new PlayMusic(this.sound, null));
               this.sounds.set(0, null);
            }

            if (this.repeats.get(0) > 1) {
               this.repeats.set(0, this.repeats.get(0) - 1);
            } else {
               this.position = this.position.add(this.positions.get(0));
               game.player.currPokemon.backSprite.setPosition(this.position.x, this.position.y);
               this.positions.remove(0);
               this.sprites.remove(0);
               this.repeats.remove(0);
               this.sounds.remove(0);
               this.alphas.remove(0);
            }
         } else {
            int currHealth = game.battle.oppPokemon.currentStats.get("hp");
            int finalHealth = currHealth - this.power;
            if (finalHealth < 0) {
               finalHealth = 0;
            }

            game.battle.oppPokemon.currentStats.put("hp", finalHealth);
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   public static class DefaultEnemy extends Action {
      Pokemon pokemon;
      Pokemon oppPokemon;
      ArrayList<Vector2> positions;
      Vector2 position;
      ArrayList<Sprite> sprites;
      Sprite sprite;
      ArrayList<Integer> repeats;
      ArrayList<Float> alphas;
      ArrayList<String> sounds;
      String sound;
      public Action.Layer layer = Action.Layer.gui_120;
      Sprite helperSprite;
      boolean doneYet;
      int power;
      int accuracy;

      public DefaultEnemy(Pokemon attackingPokemon, Pokemon oppPokemon, int power, int accuracy, Action nextAction) {
         super();
         this.pokemon = attackingPokemon;
         this.oppPokemon = oppPokemon;
         this.power = power;
         this.accuracy = accuracy;
         this.doneYet = false;
         this.nextAction = nextAction;
         this.position = new Vector2(this.pokemon.sprite.getX(), this.pokemon.sprite.getY());
         this.positions = new ArrayList<>();
         this.positions.add(new Vector2(-8.0F, 0.0F));
         this.positions.add(new Vector2(8.0F, 0.0F));

         for (int i = 0; i < 13; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.sprites = new ArrayList<>();

         for (int i = 0; i < 15; i++) {
            this.sprites.add(null);
         }

         this.repeats = new ArrayList<>();
         this.repeats.add(7);
         this.repeats.add(3);
         this.repeats.add(17);
         this.repeats.add(6);
         this.repeats.add(7);
         this.repeats.add(5);
         this.repeats.add(9);
         this.repeats.add(4);
         this.repeats.add(8);
         this.repeats.add(6);
         this.repeats.add(7);
         this.repeats.add(5);
         this.repeats.add(9);
         this.repeats.add(4);
         this.repeats.add(11);
         this.alphas = new ArrayList<>();
         this.alphas.add(1.0F);
         this.alphas.add(1.0F);
         this.alphas.add(1.0F);

         for (int i = 0; i < 6; i++) {
            this.alphas.add(0.0F);
            this.alphas.add(1.0F);
         }

         this.sounds = new ArrayList<>();
         this.sounds.add(null);
         this.sounds.add("tackle1");

         for (int i = 0; i < 13; i++) {
            this.sounds.add(null);
         }
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
         if (!this.positions.isEmpty() && !this.sprites.isEmpty()) {
            this.sprite = this.sprites.get(0);
            float currAlpha = this.alphas.get(0);
            this.oppPokemon.backSprite.setAlpha(currAlpha);
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
               game.insertAction(new PlayMusic(this.sound, null));
               this.sounds.set(0, null);
            }

            if (this.repeats.get(0) > 1) {
               this.repeats.set(0, this.repeats.get(0) - 1);
            } else {
               this.position = this.position.add(this.positions.get(0));
               this.pokemon.sprite.setPosition(this.position.x, this.position.y);
               this.positions.remove(0);
               this.sprites.remove(0);
               this.repeats.remove(0);
               this.sounds.remove(0);
               this.alphas.remove(0);
            }
         } else {
            int currHealth = this.oppPokemon.currentStats.get("hp");
            int finalHealth = currHealth - this.power;
            if (finalHealth < 0) {
               finalHealth = 0;
            }

            this.oppPokemon.currentStats.put("hp", finalHealth);
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   static class Lick extends Action {
      ArrayList<Vector2> positions;
      Vector2 position;
      ArrayList<Vector2> screenPositions;
      Vector2 currPosition;
      ArrayList<Sprite> sprites;
      Sprite sprite;
      ArrayList<Integer> repeats;
      String sound;
      ArrayList<String> sounds;
      Sprite blockSprite;
      public Action.Layer layer = Action.Layer.gui_108;
      Sprite helperSprite;
      Pokemon attacker;
      Pokemon target;
      Pixmap pixmap;
      int power = 30;
      int accuracy = 100;

      public Lick(Game game, Pokemon attacker, Pokemon target, Action nextAction) {
         super();
         this.attacker = attacker;
         this.target = target;
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/pixel1.png"));
         this.blockSprite = new Sprite(text, 0, 0, 1, 1);
         this.repeats = new ArrayList<>();
         this.repeats.add(18);
         this.repeats.add(12);

         for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
               this.repeats.add(5);
            }

            this.repeats.add(6);
         }

         this.repeats.add(22);
         this.position = new Vector2(0.0F, 0.0F);
         this.positions = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.screenPositions = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 5; i++) {
            this.screenPositions.add(new Vector2(2.0F, 0.0F));
         }

         for (int i = 0; i < 9; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 4; i++) {
            this.screenPositions.add(new Vector2(1.0F, 0.0F));
         }

         for (int i = 0; i < 19; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         text = TextureCache.get(Gdx.files.internal("attacks/enemy_lick_sheet1.png"));
         this.sprites = new ArrayList<>();
         this.sprites.add(null);

         for (int i = 0; i < 7; i++) {
            this.sprites.add(new Sprite(text, 160 * i, 0, 160, 144));
         }

         this.sprites.add(null);
         this.sounds = new ArrayList<>();
         this.sounds.add("lick1");

         for (int i = 0; i < 7; i++) {
            this.sounds.add(null);
         }

         this.sounds.add("hit_normal1");
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
         if (!this.repeats.isEmpty()) {
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
               game.insertAction(new PlayMusic(this.sound, null));
               this.sounds.set(0, null);
            }

            this.sprite = this.sprites.get(0);
            if (this.sprite != null) {
               this.sprite.setPosition(this.position.x, this.position.y);
               this.sprite.draw(game.uiBatch);
            }

            if (this.repeats.get(0) > 0) {
               this.repeats.set(0, this.repeats.get(0) - 1);
            } else {
               this.position = this.position.add(this.positions.get(0));
               this.positions.remove(0);
               this.sprites.remove(0);
               this.repeats.remove(0);
               this.sounds.remove(0);
            }
         } else {
            if (!this.screenPositions.isEmpty()) {
               this.currPosition = this.screenPositions.get(0);
               this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 480, 432);

               for (int j = 0; j < 144; j++) {
                  for (int i = 0; i < 160; i++) {
                     this.blockSprite.setColor(new Color(this.pixmap.getPixel(i * 3, j * 3)));
                     this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                     this.blockSprite.draw(game.uiBatch);
                  }
               }

               this.screenPositions.remove(0);
            } else {
               int currHealth = this.target.currentStats.get("hp");
               int finalHealth = currHealth - this.power;
               if (finalHealth < 0) {
                  finalHealth = 0;
               }

               this.target.currentStats.put("hp", finalHealth);
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
            }
         }
      }
   }

   static class Mewtwo_Special1 extends Action {
      public Action.Layer layer = Action.Layer.gui_109;
      int power = 100;
      int accuracy = 100;
      Pokemon attacker;
      Pokemon target;
      Pixmap pixmap = null;
      Sprite sprite;
      ArrayList<int[]> offsets = new ArrayList<>();
      int[] currOffsets;
      ArrayList<Vector2> positions = new ArrayList<>();
      Vector2 currPosition;
      ArrayList<String> shaderVals = new ArrayList<>();
      String currShaderVal;
      ShaderProgram currShader;
      String vertexShader;
      boolean firstStep = true;
      boolean isNightShade = false;
      boolean hitSound = true;
      int timer = 0;

      public Mewtwo_Special1(Game game, Pokemon attacker, Pokemon target, Action nextAction) {
         super();
         this.target = target;
         this.attacker = attacker;
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/pixel1.png"));
         this.sprite = new Sprite(text, 0, 0, 1, 1);
         this.vertexShader = "attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nvoid main()\n{\n    v_color = a_color;\n    v_texCoords = a_texCoord0;\n    gl_Position =  u_projTrans * a_position;\n}\n";
         String darken1 = this.getShader(-0.33F);
         String darken2 = this.getShader(-0.66F);
         String darken3 = this.getShader(-1.0F);
         String lighten1 = this.getShader(0.33F);
         String lighten2 = this.getShader(0.66F);
         String lighten3 = this.getShader(1.0F);
         String normal = this.getShader(0.0F);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken1);
         this.shaderVals.add(normal);
         this.shaderVals.add(normal);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(normal);
         this.shaderVals.add(normal);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken1);
         this.shaderVals.add(normal);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(normal);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken1);
         this.shaderVals.add(normal);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten1);

         for (int i = 0; i < 8; i++) {
            this.shaderVals.add(normal);
         }

         for (int i = 0; i < 9; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 5; i++) {
            this.positions.add(new Vector2(2.0F, 0.0F));
         }

         for (int i = 0; i < 9; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 4; i++) {
            this.positions.add(new Vector2(1.0F, 0.0F));
         }

         for (int i = 0; i < 19; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      String getShader(float level) {
         return "#ifdef GL_ES\n    precision mediump float;\n#endif\n\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\n\nvoid main() {\n    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n    float level = "
            + level
            + ";\n    if (color.r >= .9 && color.g >= .9 && color.b >= .9) {\n           color = vec4(color.r, color.g, color.b, color.a);\n    }\n    else {\n        color = vec4(color.r-level, color.g-level, color.b-level, color.a);\n    }\n    gl_FragColor = color;\n}";
      }

      @Override
      public void step(Game game) {
         if (this.timer == 0) {
            this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.8F));
            game.uiBatch.setShader(this.currShader);
            SpecialBattleMewtwo.RocksEffect1.shouldMoveY = false;
            SpecialBattleMewtwo.RocksEffect2.shouldMoveY = false;
         } else if (this.timer >= 50) {
            if (this.timer == 60) {
               game.insertAction(new PlayMusic("Mewtwo_Special1", null));
               SpecialBattleMewtwo.RocksEffect1.velocityX = -8;
               SpecialBattleMewtwo.RocksEffect2.velocityX = -2;
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.6F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 120) {
               SpecialBattleMewtwo.RocksEffect1.velocityX = -12;
               SpecialBattleMewtwo.RocksEffect2.velocityX = -3;
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.4F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 180) {
               SpecialBattleMewtwo.RocksEffect1.velocityX = -16;
               SpecialBattleMewtwo.RocksEffect2.velocityX = -4;
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.2F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 190) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.3F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 200) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.0F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 210) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.1F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 220) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.2F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 230) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.1F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 240) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.4F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 250) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.3F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 260) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.6F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 270) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.5F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer == 280) {
               this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.8F));
               game.uiBatch.setShader(this.currShader);
            } else if (this.timer >= 300) {
               if (this.timer < 550) {
                  if (this.timer % 2 == 0) {
                     game.player.currPokemon.backSprite.setAlpha(1.0F);
                  } else {
                     game.player.currPokemon.backSprite.setAlpha(0.0F);
                  }

                  if (this.timer == 340) {
                     this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.6F));
                     game.uiBatch.setShader(this.currShader);
                  }

                  if (this.timer == 400) {
                     this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.4F));
                     game.uiBatch.setShader(this.currShader);
                  }

                  if (this.timer == 460) {
                     this.currShader = new ShaderProgram(this.vertexShader, this.getShader(-0.2F));
                     game.uiBatch.setShader(this.currShader);
                  }

                  if (this.timer == 455) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -14;
                     SpecialBattleMewtwo.RocksEffect2.velocityX = -3;
                  } else if (this.timer == 465) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -12;
                  } else if (this.timer == 475) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -10;
                  } else if (this.timer == 485) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -8;
                     SpecialBattleMewtwo.RocksEffect2.velocityX = -2;
                  } else if (this.timer == 495) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -6;
                  } else if (this.timer == 505) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -6;
                  } else if (this.timer == 515) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -4;
                     SpecialBattleMewtwo.RocksEffect2.velocityX = -1;
                  } else if (this.timer == 525) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = -2;
                  } else if (this.timer == 535) {
                     SpecialBattleMewtwo.RocksEffect1.velocityX = 0;
                     SpecialBattleMewtwo.RocksEffect2.velocityX = 0;
                  }
               } else if (this.timer == 550) {
                  game.player.currPokemon.backSprite.setAlpha(1.0F);
                  this.currShader = new ShaderProgram(this.vertexShader, this.getShader(0.0F));
                  game.uiBatch.setShader(this.currShader);
                  SpecialBattleMewtwo.RocksEffect1.shouldMoveY = true;
                  SpecialBattleMewtwo.RocksEffect2.shouldMoveY = true;
               } else if (this.timer >= 600) {
                  game.actionStack.remove(this);
                  game.insertAction(this.nextAction);
               }
            }
         }

         this.timer++;
      }
   }

   static class Psychic extends Action {
      public Action.Layer layer = Action.Layer.gui_103;
      int power = 100;
      int accuracy = 100;
      Pokemon target;
      Pixmap pixmap = null;
      Sprite sprite;
      ArrayList<int[]> offsets = new ArrayList<>();
      int[] currOffsets;
      ArrayList<Vector2> positions = new ArrayList<>();
      Vector2 currPosition;
      ArrayList<String> shaderVals = new ArrayList<>();
      String currShaderVal;
      ShaderProgram currShader;
      boolean firstStep = true;
      boolean isNightShade = false;
      boolean hitSound = true;

      public Psychic(Game game, Pokemon target, boolean isNightShade, Action nextAction) {
         super();
         this.isNightShade = isNightShade;
         this.target = target;
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/pixel1.png"));
         this.sprite = new Sprite(text, 0, 0, 1, 1);
         String darken1 = this.getShader(-0.33F);
         String darken2 = this.getShader(-0.66F);
         String darken3 = this.getShader(-1.0F);
         String lighten1 = this.getShader(0.33F);
         String lighten2 = this.getShader(0.66F);
         String lighten3 = this.getShader(1.0F);
         String normal = this.getShader(0.0F);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken1);
         this.shaderVals.add(normal);
         this.shaderVals.add(normal);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(normal);
         this.shaderVals.add(normal);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken1);
         this.shaderVals.add(normal);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(normal);
         this.shaderVals.add(darken1);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken3);
         this.shaderVals.add(darken2);
         this.shaderVals.add(darken1);
         this.shaderVals.add(normal);
         this.shaderVals.add(lighten1);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten3);
         this.shaderVals.add(lighten2);
         this.shaderVals.add(lighten1);

         for (int i = 0; i < 8; i++) {
            this.shaderVals.add(normal);
         }

         for (int i = 0; i < 9; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 5; i++) {
            this.positions.add(new Vector2(2.0F, 0.0F));
         }

         for (int i = 0; i < 9; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 4; i++) {
            this.positions.add(new Vector2(1.0F, 0.0F));
         }

         for (int i = 0; i < 19; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.offsets.add(new int[]{2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
         this.offsets.add(new int[]{2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
         this.offsets.add(new int[]{1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
         this.offsets.add(new int[]{0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2});
         this.offsets.add(new int[]{2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0});
         this.offsets.add(new int[]{0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2});
         this.offsets.add(new int[]{2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
         this.offsets.add(new int[]{2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0});
         this.offsets.add(new int[]{0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
         this.offsets.add(new int[]{1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2});
         this.offsets.add(new int[]{2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2});
         this.offsets.add(new int[]{2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2});
         this.offsets.add(new int[]{2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
         this.offsets.add(new int[]{1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
         this.offsets.add(new int[]{0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
         this.offsets.add(new int[]{0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1});
         this.offsets.add(new int[]{-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
         this.offsets.add(new int[]{-2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
         this.offsets.add(new int[]{2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
         this.offsets.add(new int[]{1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
         this.offsets.add(new int[]{1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0});
         this.offsets.add(new int[]{0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
         this.offsets.add(new int[]{1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
         this.offsets.add(new int[]{0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1});
         this.offsets.add(new int[]{2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2});
         this.offsets.add(new int[]{2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
         this.offsets.add(new int[]{1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
         this.offsets.add(new int[]{-1, -2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
         this.offsets.add(new int[]{2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{-2, -2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
         this.offsets.add(new int[]{1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{-2, -2, -1, 0, 0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2});
         this.offsets.add(new int[]{1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{0, 0, 1, 2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0});
         this.offsets.add(new int[]{-1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
         this.offsets.add(new int[]{0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
         this.offsets.add(new int[]{1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
         this.offsets.add(new int[]{2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2});
         this.offsets.add(new int[]{2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1});
         this.offsets.add(new int[]{2, 2, 2, 1, 0, 0, 0, -1, -2, -2, -2, -1, 0, 0, 0, 1});
         this.offsets.add(new int[]{1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0});
         this.offsets.add(new int[]{0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0});
         this.offsets.add(new int[]{0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{-2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2});
         this.offsets.add(new int[]{-2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1});
         this.offsets.add(new int[]{-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
         this.offsets.add(new int[]{-1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1});
         this.offsets.add(new int[]{-1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0});
         this.offsets.add(new int[]{0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2, 1, 1});
         this.offsets.add(new int[]{-1, 0, 0, 1, 1, 2, 2, 1, 1, 0, 0, -1, -1, -2, -2, -1});
         this.offsets.add(new int[]{1, 1, 0, 0, -1, -1, -2, -2, -1, -1, 0, 0, 1, 1, 2, 2});
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      String getShader(float level) {
         return "#ifdef GL_ES\n    precision mediump float;\n#endif\n\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\n\nvoid main() {\n    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n    float level = "
            + level
            + ";\n    color = vec4(color.r+level, color.g+level, color.b+level, color.a);\n    gl_FragColor = color;\n}";
      }

      @Override
      public void step(Game game) {
         if (this.firstStep) {
            if (this.isNightShade) {
               game.insertAction(new PlayMusic("night_shade1", null));
            } else {
               game.insertAction(new PlayMusic("psychic1", null));
            }

            this.firstStep = false;
         }

         if (!this.shaderVals.isEmpty()) {
            this.currShaderVal = this.shaderVals.get(0);
            this.currShader = new ShaderProgram(EvolutionAnim.vertexShader, this.currShaderVal);
            game.uiBatch.setShader(this.currShader);
            this.shaderVals.remove(0);
         } else if (!this.offsets.isEmpty()) {
            this.currOffsets = this.offsets.get(0);
            float heightM = game.currScreen.y / 144.0F;
            if (this.pixmap == null) {
               game.uiBatch.flush();
               int offsetX = (int)((game.currScreen.x - 160.0F * game.currScreen.y / 144.0F) / 2.0F);
               this.pixmap = ScreenUtils.getFrameBufferPixmap(offsetX, 0, (int)game.currScreen.x - offsetX * 2, (int)game.currScreen.y);
            }

            for (int j = 0; j < 141; j++) {
               for (int i = 0; i < 160; i++) {
                  this.sprite.setColor(new Color(this.pixmap.getPixel((int)(i * heightM), (int)(j * heightM))));
                  this.sprite.setPosition(i + this.currOffsets[j % 16], j);
                  this.sprite.draw(game.uiBatch);
               }
            }

            this.offsets.remove(0);
         } else {
            game.actionStack.remove(this);
            game.insertAction(this.nextAction);
         }
      }
   }

   static class ShadowClaw extends Action {
      ArrayList<Vector2> positions;
      Vector2 position;
      ArrayList<Vector2> screenPositions;
      Vector2 currPosition;
      ArrayList<Sprite> sprites;
      Sprite sprite;
      ArrayList<Integer> repeats;
      String sound;
      ArrayList<String> sounds;
      String currShaderVal;
      ArrayList<String> shaderVals = new ArrayList<>();
      ShaderProgram currShader;
      String vertexShader;
      Sprite blockSprite;
      public Action.Layer layer = Action.Layer.gui_100;
      Sprite helperSprite;
      Pokemon attacker;
      Pokemon target;
      Pixmap pixmap;
      int power = 70;
      int accuracy = 100;

      public ShadowClaw(Game game, Pokemon attacker, Pokemon target, Action nextAction) {
         super();
         this.attacker = attacker;
         this.target = target;
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/pixel1.png"));
         this.blockSprite = new Sprite(text, 0, 0, 1, 1);
         text = TextureCache.get(Gdx.files.internal("attacks/enemy_slash_sheet1.png"));
         this.position = new Vector2(16.0F, 40.0F);
         this.positions = new ArrayList<>();

         for (int i = 0; i < 7; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.screenPositions = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 5; i++) {
            this.screenPositions.add(new Vector2(2.0F, 0.0F));
         }

         for (int i = 0; i < 9; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 4; i++) {
            this.screenPositions.add(new Vector2(1.0F, 0.0F));
         }

         for (int i = 0; i < 19; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         this.sprites = new ArrayList<>();
         this.sprites.add(null);
         this.sprites.add(null);

         for (int i = 0; i < 4; i++) {
            this.sprites.add(new Sprite(text, 48 * i, 0, 48, 48));
         }

         this.sprites.add(null);
         this.repeats = new ArrayList<>();
         this.repeats.add(19);
         this.repeats.add(39);

         for (int i = 0; i < 4; i++) {
            this.repeats.add(6);
         }

         this.repeats.add(5);
         this.sounds = new ArrayList<>();
         this.sounds.add(null);
         this.sounds.add(null);
         this.sounds.add("slash1");

         for (int i = 0; i < 4; i++) {
            this.sounds.add(null);
         }

         this.vertexShader = "attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projTrans;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nvoid main()\n{\n    v_color = a_color;\n    v_texCoords = a_texCoord0;\n    gl_Position =  u_projTrans * a_position;\n}\n";
         float level = 0.0F;
         String normalShader = "precision mediump float;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nuniform mat4 u_projTrans;\nbool equals(float a, float b) {\n    return abs(a-b) < 0.0001;\n}\nvoid main() {\n    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n    float level = "
            + level
            + ";\n    color = vec4(color.r+level, color.g+level, color.b+level, color.a);\n    gl_FragColor = color;\n}\n";
         String inverseShader = "precision mediump float;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nuniform mat4 u_projTrans;\nbool equals(float a, float b) {\n    return abs(a-b) < 0.0001;\n}\nvoid main() {\n    vec4 color = texture2D (u_texture, v_texCoords) * v_color;\n    color = vec4(1-color.r, 1-color.g, 1-color.b, color.a);\n    gl_FragColor = color;\n}\n";
         this.shaderVals.add(inverseShader);

         for (int i = 0; i < 5; i++) {
            this.shaderVals.add(inverseShader);
         }

         this.shaderVals.add(normalShader);
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
         if (!this.repeats.isEmpty()) {
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
               game.insertAction(new PlayMusic(this.sound, null));
               this.sounds.set(0, null);
            }

            this.sprite = this.sprites.get(0);
            if (this.sprite != null) {
               this.sprite.setPosition(this.position.x, this.position.y);
               this.sprite.draw(game.uiBatch);
            }

            if (this.repeats.get(0) > 0) {
               this.repeats.set(0, this.repeats.get(0) - 1);
            } else {
               this.position = this.position.add(this.positions.get(0));
               this.positions.remove(0);
               this.sprites.remove(0);
               this.repeats.remove(0);
               this.sounds.remove(0);
               this.currShaderVal = this.shaderVals.get(0);
               this.currShader = new ShaderProgram(this.vertexShader, this.currShaderVal);
               game.uiBatch.setShader(this.currShader);
               this.shaderVals.remove(0);
            }
         } else {
            if (!this.screenPositions.isEmpty()) {
               this.currPosition = this.screenPositions.get(0);
               this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 480, 432);

               for (int j = 0; j < 144; j++) {
                  for (int i = 0; i < 160; i++) {
                     this.blockSprite.setColor(new Color(this.pixmap.getPixel(i * 3, j * 3)));
                     this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                     this.blockSprite.draw(game.uiBatch);
                  }
               }

               this.screenPositions.remove(0);
            } else {
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
            }
         }
      }
   }

   static class Slash extends Action {
      ArrayList<Vector2> positions;
      Vector2 position;
      ArrayList<Vector2> screenPositions;
      Vector2 currPosition;
      ArrayList<Sprite> sprites;
      Sprite sprite;
      ArrayList<Integer> repeats;
      String sound;
      ArrayList<String> sounds;
      Sprite blockSprite;
      public Action.Layer layer = Action.Layer.gui_108;
      Sprite helperSprite;
      Pokemon attacker;
      Pokemon target;
      Pixmap pixmap;
      int power = 70;
      int accuracy = 100;

      public Slash(Game game, Pokemon attacker, Pokemon target, Action nextAction) {
         super();
         this.attacker = attacker;
         this.target = target;
         this.nextAction = nextAction;
         Texture text = TextureCache.get(Gdx.files.internal("battle/pixel1.png"));
         this.blockSprite = new Sprite(text, 0, 0, 1, 1);
         text = TextureCache.get(Gdx.files.internal("attacks/enemy_slash_sheet1.png"));
         this.position = new Vector2(16.0F, 40.0F);
         this.positions = new ArrayList<>();

         for (int i = 0; i < 6; i++) {
            this.positions.add(new Vector2(0.0F, 0.0F));
         }

         this.screenPositions = new ArrayList<>();

         for (int i = 0; i < 9; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 5; i++) {
            this.screenPositions.add(new Vector2(2.0F, 0.0F));
         }

         for (int i = 0; i < 9; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         for (int i = 0; i < 4; i++) {
            this.screenPositions.add(new Vector2(1.0F, 0.0F));
         }

         for (int i = 0; i < 19; i++) {
            this.screenPositions.add(new Vector2(0.0F, 0.0F));
         }

         this.sprites = new ArrayList<>();
         this.sprites.add(null);

         for (int i = 0; i < 4; i++) {
            this.sprites.add(new Sprite(text, 48 * i, 0, 48, 48));
         }

         this.sprites.add(null);
         this.repeats = new ArrayList<>();
         this.repeats.add(19);

         for (int i = 0; i < 4; i++) {
            this.repeats.add(6);
         }

         this.repeats.add(5);
         this.sounds = new ArrayList<>();
         this.sounds.add("slash1");

         for (int i = 0; i < 5; i++) {
            this.sounds.add(null);
         }
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
         if (!this.repeats.isEmpty()) {
            this.sound = this.sounds.get(0);
            if (this.sound != null) {
               game.insertAction(new PlayMusic(this.sound, null));
               this.sounds.set(0, null);
            }

            this.sprite = this.sprites.get(0);
            if (this.sprite != null) {
               this.sprite.setPosition(this.position.x, this.position.y);
               this.sprite.draw(game.uiBatch);
            }

            if (this.repeats.get(0) > 0) {
               this.repeats.set(0, this.repeats.get(0) - 1);
            } else {
               this.position = this.position.add(this.positions.get(0));
               this.positions.remove(0);
               this.sprites.remove(0);
               this.repeats.remove(0);
               this.sounds.remove(0);
            }
         } else {
            if (!this.screenPositions.isEmpty()) {
               this.currPosition = this.screenPositions.get(0);
               this.pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, 480, 432);

               for (int j = 0; j < 144; j++) {
                  for (int i = 0; i < 160; i++) {
                     this.blockSprite.setColor(new Color(this.pixmap.getPixel(i * 3, j * 3)));
                     this.blockSprite.setPosition(i + this.currPosition.x, j + this.currPosition.y);
                     this.blockSprite.draw(game.uiBatch);
                  }
               }

               this.screenPositions.remove(0);
            } else {
               int currHealth = this.target.currentStats.get("hp");
               int finalHealth = currHealth - this.power;
               if (finalHealth < 0) {
                  finalHealth = 0;
               }

               this.target.currentStats.put("hp", finalHealth);
               game.actionStack.remove(this);
               game.insertAction(this.nextAction);
            }
         }
      }
   }
}
