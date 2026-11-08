package com.pkmngen.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.pkmngen.game.util.GameProfiler;
import com.pkmngen.game.util.ProxyBatch;
import com.pkmngen.game.util.SpriteProxy;
import com.pkmngen.game.util.StackTraces;
import com.pkmngen.game.util.TextureCache;
import com.pkmngen.game.util.texture.TilesAtlas;
import com.pkmngen.leaks.LeakTracer;
import com.pkmngen.updater.VersionControl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.swing.JOptionPane;

public class Game extends ApplicationAdapter {
   public static Game staticGame;
   public static LeakTracer leakTracer;
   public ArrayList<Action> actionStack = new ArrayList<>();
   public ArrayList<Action> actionStackCopy = new ArrayList<>();
   public ProxyBatch mapBatch;
   public ProxyBatch uiBatch;
   public BitmapFont font;
   public OrthographicCamera cam;
   private ScreenViewport viewport;
   Vector3 touchLoc = new Vector3();
   public boolean playerCanMove;
   public Action displayTextAction;
   Vector2 currScreen = new Vector2();
   boolean debugInputEnabled = false;
   boolean dangerousDebugInputEnabled = false;
   Player player;
   PkmnMap map;
   Battle battle;
   public Music currMusic;
   MusicController musicController = null;
   HashMap<String, Music> loadedMusic = new HashMap<>();
   HashMap<String, Sound> loadedSound = new HashMap<>();
   Map<Character, SpriteProxy> textDict;
   HashMap<Character, SpriteProxy> transparentDict = new HashMap<>();
   HashMap<String, Player> players = new HashMap<>();
   HashMap<String, Battle> battles = new HashMap<>();
   public VersionControl versionControl = new VersionControl();
   private boolean skipUpdateCheck = false;
   Texture cloudTexture;
   Texture nightTexture;
   Texture currCloudTexture;
   Texture islandTexture;
   Color currSunsetColor = new Color(1.0F, 1.0F, 1.0F, 1.0F);
   Color prevColor = new Color();
   Vector2 cloudOffset = new Vector2(0.0F, 0.0F);
   int rippleTimer = 0;
   Vector2 startPos = new Vector2();
   Vector2 endPos = new Vector2();
   boolean otherFlipY = false;
   public Client client;
   public Server server;
   Game.Type type;
   public static Random rand = new Random();
   public Thread gameThread;
   DrawCampfireAuras drawCampfireAuras;
   Texture shadow;
   public FrameBuffer frameBuffer;
   public boolean levelScalingEnabled = true;
   public static boolean fairyTypeEnabled = true;
   public static boolean catchExpEnabled = true;
   public static boolean specialPhysicalSplitEnabled = true;
   public static boolean musicDisabled = false;
   public static boolean photosensitiveMode = true;
   public static boolean battleAnims = true;
   public static TextSpeed textSpeed = TextSpeed.MID;
   public static boolean drawReflections = true;
   public static boolean drawOverlays = true;
   HashMap<Character, SpriteProxy> brailleDict = new HashMap<>();
   HashMap<Character, SpriteProxy> textDictInverse = new HashMap<>();
   boolean cinematic = false;
   public long startTime = 0L;
   public long currTime = 0L;
   public HashMap<String, Long> timings = new HashMap<>();
   ArrayList<String> alreadyUsedDungeons = new ArrayList<>();
   static Matrix4 identity = new Matrix4();
   public static boolean canUseFrameBuffers = true;
   int temp1;
   int temp2;
   int scale = 3;
   public static String[][] defaultSettings = new String[][]{
      {"keyboard-A", "Z"},
      {"keyboard-B", "X"},
      {"keyboard-Left", "Left"},
      {"keyboard-Right", "Right"},
      {"keyboard-Up", "Up"},
      {"keyboard-Down", "Down"},
      {"keyboard-Start", "Enter"},
      {"keyboard-L", "C"},
      {"keyboard-R", "V"},
      {"muteMusic", "false"},
      {"specPhysSplitEnabled", "true"},
      {"photosensitiveMode", "true"},
      {"battleAnims", "true"},
      {"textSpeed", "mid"},
      {"gamepad-A", "A"},
      {"gamepad-B", "B"},
      {"gamepadDeadZone", "0.3"},
      {"zoom", "1.0"},
      {"drawReflections", "true"},
      {"drawOverlays", "true"}
   };
   public static Controller gamepad;
   int shinyRate = 256;
   private GameProfiler profiler;
   public static List<String> evoStones = new ArrayList<>();
   private final GridPoint2 oldWindowSize = new GridPoint2();
   private boolean inFullScreen = false;

   LanCoop coop;

   public Game() {
   }

   public Game(String[] args, int scale) {
      this();

      for (String arg : args) {
         switch (arg) {
            case "dev":
               this.debugInputEnabled = true;
               break;
            case "cin":
               this.cinematic = true;
               break;
            case "skipUpdateCheck":
               this.skipUpdateCheck = true;
               break;
            case "dangerous":
               this.dangerousDebugInputEnabled = true;
               this.debugInputEnabled = true;
         }
      }

      this.scale = scale;
      LanOptions lan = LanOptions.parse(args);
      if (lan.mode != null) this.coop = new LanCoop(this, lan);
   }

   public static ArrayList<String> readLines(FileHandle file) throws IOException {
      Reader reader = file.reader();
      BufferedReader br = new BufferedReader(reader);
      ArrayList<String> lines = new ArrayList<>();

      String line;
      while ((line = br.readLine()) != null) {
         lines.add(line);
      }

      reader.close();
      return lines;
   }

   @Override
   public void create() {
      this.profiler = new GameProfiler(this.debugInputEnabled);
      staticGame = this;
      this.gameThread = Thread.currentThread();
      this.mapBatch = new ProxyBatch();
      this.uiBatch = new ProxyBatch();
      this.mapBatch.enableBlending();
      this.uiBatch.enableBlending();
      this.mapBatch.setBlendFunction(770, 771);
      this.uiBatch.setBlendFunction(770, 771);

      try {
         this.frameBuffer = new FrameBuffer(Format.RGBA8888, 640, 432, false);
      } catch (GdxRuntimeException e) {
         canUseFrameBuffers = false;
         System.out.println("WARNING: Frame Buffers not supported.");
      }

      this.drawCampfireAuras = new DrawCampfireAuras(this);
      this.shadow = TextureCache.get(Gdx.files.internal("shadow2.png"));
      this.cam = new OrthographicCamera(160.0F, 144.0F);
      this.cam.position.set(16.0F, 0.0F, 0.0F);
      this.cam.zoom = 1.0F;
      this.viewport = new ScreenViewport(this.cam);
      FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts.ttf"));
      FreeTypeFontParameter parameter = new FreeTypeFontParameter();
      parameter.size = 10;
      this.font = gen.generateFont(parameter);
      this.font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
      this.font.setColor(Color.BLACK);
      this.player = new Player();
      this.battle = new Battle();
      this.textDict = this.initTextDict();
      this.initTransparentDict();
      this.initBrailleDict();
      this.initInverseTextDict();
      this.insertAction(new InputProcessor());
      if (Gdx.app.getType() == ApplicationType.Android) {
         this.insertAction(new DrawMobileControls(this));
      }

      this.insertAction(new DrawSetupMenu(this, null));
      ScreenUtils.clear(1.0F, 1.0F, 1.0F, 1.0F, true);
      ShaderProgram.pedantic = false;
      System.out.println("Java version info");
      System.out.println(System.getProperty("java.version"));
      System.out.println(System.getProperty("java.specification.version"));
      System.out.println("Graphics processor info");
      System.out.println(Gdx.gl.glGetString(7936));
      System.out.println(Gdx.gl.glGetString(7937));
      System.out.println("OpenGL info");
      System.out.println(Gdx.gl.glGetString(7938));
      this.cloudTexture = TextureCache.get(Gdx.files.local("cloud5.png"));
      this.nightTexture = TextureCache.get(Gdx.files.local("stars1.png"));
      this.currCloudTexture = this.cloudTexture;
      this.islandTexture = TextureCache.get(Gdx.files.local("island1.png"));
      if (this.skipUpdateCheck) {
         this.versionControl.checkVersion();
      }

      gamepad = Controllers.getCurrent();

      try {
         FileHandle file = Gdx.files.local("settings.txt");
         this.loadSettings(file);
      } catch (GdxRuntimeException e) {
         String settingsString = "";

         for (int i = 0; i < defaultSettings.length; i++) {
            settingsString = settingsString + defaultSettings[i][0] + "=" + defaultSettings[i][1];
            if (i < defaultSettings.length - 1) {
               settingsString = settingsString + "\r\n";
            }
         }

         try {
            FileHandle file = Gdx.files.local("settings.txt");
            file.writeString(settingsString, false);
            this.loadSettings(file);
         } catch (GdxRuntimeException err) {
            err.printStackTrace();
         }
      }

      this.viewport.setUnitsPerPixel(1.0F / this.scale);
      System.out.print("Frame Buffers enabled: ");
      System.out.println(canUseFrameBuffers);
      if (this.coop != null) this.coop.create();
   }

   public boolean isDedicatedClient() {
      return this.coop != null && this.coop.isDedicated();
   }

   public boolean isMapLoaded() {
      return this.map != null;
   }

   public void saveGame() {
      if (this.coop != null && this.coop.isGuest()) { this.coop.saveGuest(); return; }
      if (this.map != null) {
         this.map.saveToFileNew(this);
      }
   }

   @Override
   public void dispose() {
      if (this.coop != null) this.coop.close();
      this.mapBatch.dispose();
      this.uiBatch.dispose();

      for (Music music : this.loadedMusic.values()) {
         music.dispose();
      }

      for (Sound sound : this.loadedSound.values()) {
         sound.dispose();
      }

      if (this.server != null) {
         this.server.close();
      }

      if (this.type == Game.Type.CLIENT) {
         Network.Logout logoutPlayer = new Network.Logout(this.player.network.id);
         this.client.sendTCP(logoutPlayer);
      }
   }

   private void handleDebuggingInput() {
      if (this.playerCanMove || this.dangerousDebugInputEnabled) {
         if (Gdx.input.isKeyPressed(45) && this.cam.zoom < 8.0F) {
            this.cam.zoom = (float)(this.cam.zoom + 0.5);
         }

         if (Gdx.input.isKeyPressed(33) && this.cam.zoom > 1.0F) {
            this.cam.zoom = (float)(this.cam.zoom - 0.5);
            this.map.refreshCache = true;
         }

         if (this.cinematic && !Gdx.input.isKeyPressed(62)) {
            int amt = 3;
            if (Gdx.input.isKeyPressed(29) && this.cam.position.x > -10000.0F) {
               this.cam.translate(-amt, 0.0F, 0.0F);
            }

            if (Gdx.input.isKeyPressed(32) && this.cam.position.x < 10000.0F) {
               this.cam.translate(amt, 0.0F, 0.0F);
            }

            if (Gdx.input.isKeyPressed(47) && this.cam.position.y > -10000.0F) {
               this.cam.translate(0.0F, -amt, 0.0F);
            }

            if (Gdx.input.isKeyPressed(51) && this.cam.position.y < 10000.0F) {
               this.cam.translate(0.0F, amt, 0.0F);
            }
         } else {
            if (Gdx.input.isKeyPressed(29) && this.cam.position.x > -10000.0F) {
               this.cam.translate(-30.0F, 0.0F, 0.0F);
            }

            if (Gdx.input.isKeyPressed(32) && this.cam.position.x < 10000.0F) {
               this.cam.translate(30.0F, 0.0F, 0.0F);
            }

            if (Gdx.input.isKeyPressed(47) && this.cam.position.y > -10000.0F) {
               this.cam.translate(0.0F, -30.0F, 0.0F);
            }

            if (Gdx.input.isKeyPressed(51) && this.cam.position.y < 10000.0F) {
               this.cam.translate(0.0F, 30.0F, 0.0F);
            }
         }

         if (Gdx.input.isKeyJustPressed(35)) {
            TilesAtlas.saveAtlasToFile();
         }

         if (Gdx.input.isKeyJustPressed(34)) {
            this.cam.zoom = 1.0F;
         }

         if (Gdx.input.isKeyJustPressed(48)) {
            if (CycleDayNight.dayTimer < 5000) {
               CycleDayNight.dayTimer = 100;
            } else if (CycleDayNight.dayTimer < 13000) {
               CycleDayNight.dayTimer = 5100;
            } else if (CycleDayNight.dayTimer < 18000) {
               CycleDayNight.dayTimer = 13100;
            } else {
               CycleDayNight.dayTimer = 18100;
            }
         }

         if (Gdx.input.isKeyJustPressed(44) && this.map != null) {
            for (String name : this.timings.keySet()) {
               System.out.println(name + ": " + this.timings.get(name));
            }

            System.out.println("Layer, Name");

            for (Action action : this.actionStack) {
               System.out.println(action.getLayer() + "  " + action.getClass().getName());
            }

            System.out.println("Time of day: " + this.map.timeOfDay + " " + CycleDayNight.dayTimer);

            for (Pokemon pokemon : this.player.pokemon) {
               System.out.println(pokemon.previousOwner.name);
            }
         }

         if (Gdx.input.isKeyJustPressed(92) && this.map != null) {
            if (this.map.interiorTilesIndex < this.map.interiorTiles.size() - 1) {
               this.map.interiorTilesIndex++;
               this.map.tiles = this.map.interiorTiles.get(this.map.interiorTilesIndex);
               this.map.refreshCache = true;
            }
         } else if (Gdx.input.isKeyJustPressed(93) && this.map != null) {
            this.map.interiorTilesIndex--;
            this.map.tiles = this.map.interiorTiles.get(this.map.interiorTilesIndex);
            this.map.refreshCache = true;
         } else if (Gdx.input.isKeyJustPressed(123) && this.map != null) {
            this.map.tiles = this.map.overworldTiles;
            this.map.refreshCache = true;
         } else if (Gdx.input.isKeyJustPressed(41) && this.map != null) {
            String nextMusicName = this.map.currRoute.getNextMusic(true);
            Action nextMusic = new FadeMusic(
               this.currMusic,
               -0.025F,
               new SetField(this.musicController, "startOverworldMusic", nextMusicName, new SetField(this.musicController, "inTransition", false, null))
            );
            this.insertAction(nextMusic);
         }

         if (Gdx.input.isKeyJustPressed(37) && this.map != null) {
            for (Tile tile : this.map.overworldTiles.values()) {
               if (tile.nameUpper.equals("pokemon_mansion_key")) {
                  System.out.println(this.cam.position);
                  System.out.println(tile.position);
                  this.cam.position.set(tile.position.x, tile.position.y, 1.0F);
                  break;
               }
            }
         }

         if (Gdx.input.isKeyJustPressed(43) && this.map != null) {
            for (Tile tile : this.map.overworldTiles.values()) {
               if (tile.name.equals("cave1_door1")) {
                  System.out.println(this.cam.position);
                  System.out.println(tile.position);
                  this.cam.position.set(tile.position.x, tile.position.y, 1.0F);
                  break;
               }
            }
         }

         if (Gdx.input.isKeyJustPressed(40) && this.map != null) {
            try {
               Vector2 tl = null;
               Vector2 br = null;

               for (Tile tile : this.map.tiles.values()) {
                  if (tl == null) {
                     tl = tile.position.cpy();
                  }

                  if (br == null) {
                     br = tile.position.cpy();
                  }

                  if (tile.position.x < tl.x) {
                     tl.x = tile.position.x;
                  } else if (tile.position.x > br.x) {
                     br.x = tile.position.x;
                  }

                  if (tile.position.y < br.y) {
                     br.y = tile.position.y;
                  } else if (tile.position.y > tl.y) {
                     tl.y = tile.position.y;
                  }
               }

               System.out.println("Creating screenshot of full map...");
               System.out.println((int)(br.x - tl.x) + 16);
               System.out.println((int)(tl.y - br.y) + 16);

               Pixmap pixmap;
               while (true) {
                  try {
                     pixmap = new Pixmap((int)(br.x - tl.x) + 16, (int)(tl.y - br.y) + 16, Format.RGBA8888);
                     break;
                  } catch (Exception e) {
                     br.x -= 16.0F;
                     br.y += 16.0F;
                     tl.x += 16.0F;
                     tl.y -= 16.0F;
                  }
               }

               TextureData temp = TextureCache.get(Gdx.files.internal("tiles/water4.png")).getTextureData();
               if (!temp.isPrepared()) {
                  temp.prepare();
               }

               Pixmap tidalPixmap = temp.consumePixmap();
               HashMap<String, Pixmap> pixmapCache = new HashMap<>();

               for (Vector2 currPos = tl.cpy(); currPos.y >= br.y - 16.0F; currPos.x += 16.0F) {
                  if (currPos.x > br.x + 16.0F) {
                     currPos.x = tl.x - 16.0F;
                     currPos.y -= 16.0F;
                  } else {
                     Tile currTile = this.map.tiles.get(currPos);
                     if (currTile != null && currTile.name != null) {
                        String name = currTile.name;
                        if (!pixmapCache.containsKey(name)) {
                           pixmapCache.put(name, currTile.sprite.getPixmap());
                        }

                        Pixmap currPixmap = pixmapCache.get(name);
                        pixmap.drawPixmap(
                           currPixmap,
                           (int)(currPos.x - tl.x),
                           (int)(tl.y - currPos.y) + (16 - currTile.sprite.getRegionHeight()),
                           currTile.sprite.getRegionX(),
                           currTile.sprite.getRegionY(),
                           currTile.sprite.getRegionWidth(),
                           currTile.sprite.getRegionHeight()
                        );
                        if (currTile.isTidal) {
                           pixmap.drawPixmap(
                              tidalPixmap,
                              (int)(currPos.x - tl.x),
                              (int)(tl.y - currPos.y) + (16 - currTile.sprite.getRegionHeight()),
                              currTile.sprite.getRegionX(),
                              currTile.sprite.getRegionY(),
                              currTile.sprite.getRegionWidth(),
                              currTile.sprite.getRegionHeight()
                           );
                        }
                     }
                  }
               }

               for (Vector2 currPos = tl.cpy(); currPos.y >= br.y - 16.0F; currPos.x += 16.0F) {
                  if (currPos.x > br.x + 16.0F) {
                     currPos.x = tl.x - 16.0F;
                     currPos.y -= 16.0F;
                  } else {
                     Tile currTile = this.map.tiles.get(currPos);
                     if (currTile != null && !currTile.nameUpper.equals("solid") && currTile.overSprite != null && !currTile.isWaterfall) {
                        String name = currTile.nameUpper;
                        if (!pixmapCache.containsKey(name)) {
                           pixmapCache.put(name, currTile.overSprite.getPixmap());
                        }

                        Pixmap currPixmap = pixmapCache.get(name);
                        pixmap.drawPixmap(
                           currPixmap,
                           (int)(currPos.x - tl.x),
                           (int)(tl.y - currPos.y) + (16 - currTile.overSprite.getRegionHeight()),
                           currTile.overSprite.getRegionX(),
                           currTile.overSprite.getRegionY(),
                           currTile.overSprite.getRegionWidth(),
                           currTile.overSprite.getRegionHeight()
                        );
                     }
                  }
               }

               Pixmap prismPixmap = this.createPixmap(pixmapCache, "pokemon/overworlds_sheet.png");
               Pixmap crystalPixmap = this.createPixmap(pixmapCache, "pokemon/crystal-overworld-sprites1.png");

               for (Vector2 currPos = tl.cpy(); currPos.y >= br.y - 16.0F; currPos.x += 16.0F) {
                  if (currPos.x > br.x + 16.0F) {
                     currPos.x = tl.x - 16.0F;
                     currPos.y -= 16.0F;
                  } else {
                     Pokemon currPokemon = this.map.pokemon.get(currPos);
                     if (currPokemon != null) {
                        Pixmap currPixmap = prismPixmap;
                        if (currPokemon.currOwSprite.getTexture() == TextureCache.get(Gdx.files.internal("pokemon/crystal-overworld-sprites1.png"))) {
                           currPixmap = crystalPixmap;
                        }

                        pixmap.drawPixmap(
                           currPixmap,
                           (int)(currPos.x - tl.x),
                           (int)(tl.y - currPos.y) + (16 - (int)currPokemon.currOwSprite.getHeight()),
                           currPokemon.currOwSprite.getRegionX(),
                           currPokemon.currOwSprite.getRegionY(),
                           currPokemon.currOwSprite.getRegionWidth(),
                           currPokemon.currOwSprite.getRegionHeight()
                        );
                     }
                  }
               }

               FileHandle file = new FileHandle("screenshot1.png");
               PixmapIO.writePNG(file, pixmap);
               System.out.println("Done.");
            } catch (GdxRuntimeException e) {
               e.printStackTrace();
            }
         }
      }
   }

   private Pixmap createPixmap(Map<String, Pixmap> pixmapCache, String name) {
      if (!pixmapCache.containsKey(name)) {
         TextureData temp = TextureCache.get(Gdx.files.internal(name)).getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         pixmapCache.put(name, temp.consumePixmap());
      }

      return pixmapCache.get(name);
   }

   /** Menu and server-list joins use the same revisioned LAN protocol as DedicatedServer. */
   public void joinCoop(String endpoint, String name) {
      LanOptions options = LanOptions.forJoin(endpoint, name);
      if (this.coop != null) this.coop.close();
      this.coop = new LanCoop(this, options);
      this.coop.create();
   }

   public void initClient(String ip) throws IOException {
      if (this.client != null) {
         this.client.close();
      }

      this.client = new Client();
      this.type = Game.Type.CLIENT;
      Network.register(this.client);
      this.client.start();

      try {
         this.client.connect(5000, ip, 54555);
      } catch (IOException e) {
         e.printStackTrace();
      }

      this.player.network.id = this.player.name;
      this.player.type = Player.Type.LOCAL;
      this.map.tiles.clear();
      this.client.sendTCP(new Network.Login(this.player.network.id, this.player.color));
      this.insertAction(new ClientBroadcast(this));
   }

   public void initServer() throws IOException {
      this.server = new Server();
      Network.register(this.server);
      this.type = Game.Type.SERVER;
      this.server.bind(54555);
      this.server.start();

      while (this.server.getUpdateThread() == null) {
         try {
            Thread.sleep(1L);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   public void initTransparentDict() {
      Texture text = TextureCache.get(Gdx.files.internal("text_sheet1_transparent.png"));
      char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
      char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();

      for (int i = 0; i < 26; i++) {
         this.transparentDict.put(alphabet_upper[i], new SpriteProxy(text, 10 + 16 * i, 5, 8, 8));
      }

      for (int i = 0; i < 26; i++) {
         this.transparentDict.put(alphabet_lower[i], new SpriteProxy(text, 10 + 16 * i, 17, 8, 8));
      }

      for (int i = 0; i < 10; i++) {
         this.transparentDict.put(Character.forDigit(i, 10), new SpriteProxy(text, 10 + 16 * i, 29, 8, 8));
      }

      this.transparentDict.put(' ', new SpriteProxy(text, 170, 29, 8, 8));
      this.transparentDict.put('<', new SpriteProxy(text, 10, 41, 8, 8));
      this.transparentDict.put('>', new SpriteProxy(text, 26, 41, 8, 8));
      this.transparentDict.put('_', new SpriteProxy(text, 42, 41, 8, 8));
      this.transparentDict.put('?', new SpriteProxy(text, 58, 41, 8, 8));
      this.transparentDict.put('!', new SpriteProxy(text, 74, 41, 8, 8));
      this.transparentDict.put('.', new SpriteProxy(text, 122, 41, 8, 8));
      this.transparentDict.put(',', new SpriteProxy(text, 138, 41, 8, 8));
      this.transparentDict.put('é', new SpriteProxy(text, 154, 41, 8, 8));
      this.transparentDict.put('É', new SpriteProxy(text, 154, 41, 8, 8));
      this.transparentDict.put('-', new SpriteProxy(text, 170, 41, 8, 8));
      this.transparentDict.put('\'', new SpriteProxy(text, 186, 41, 8, 8));
      this.transparentDict.put('ì', new SpriteProxy(text, 202, 41, 8, 8));
      this.transparentDict.put('\u0092', new SpriteProxy(text, 136, 45, 8, 8));
      this.transparentDict.put(null, new SpriteProxy(text, 10, 53, 8, 8));
   }

   public void initBrailleDict() {
      Texture text = TextureCache.get(Gdx.files.internal("braille_sheet1.png"));
      char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
      char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();

      for (int i = 0; i < 26; i++) {
         this.brailleDict.put(alphabet_upper[i], new SpriteProxy(text, 10 + 16 * i, 5, 8, 8));
      }

      for (int i = 0; i < 26; i++) {
         this.brailleDict.put(alphabet_lower[i], new SpriteProxy(text, 10 + 16 * i, 5, 8, 8));
      }
   }

   public void initInverseTextDict() {
      Texture text = TextureCache.get(Gdx.files.internal("text_sheet1_transparent_inverse.png"));
      char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
      char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();

      for (int i = 0; i < 26; i++) {
         this.textDictInverse.put(alphabet_upper[i], new SpriteProxy(text, 10 + 16 * i, 5, 8, 8));
      }

      for (int i = 0; i < 26; i++) {
         this.textDictInverse.put(alphabet_lower[i], new SpriteProxy(text, 10 + 16 * i, 17, 8, 8));
      }

      for (int i = 0; i < 10; i++) {
         this.textDictInverse.put(Character.forDigit(i, 10), new SpriteProxy(text, 10 + 16 * i, 29, 8, 8));
      }

      this.textDictInverse.put(' ', new SpriteProxy(text, 170, 29, 8, 8));
      this.textDictInverse.put('<', new SpriteProxy(text, 10, 41, 8, 8));
      this.textDictInverse.put('>', new SpriteProxy(text, 26, 41, 8, 8));
      this.textDictInverse.put('_', new SpriteProxy(text, 42, 41, 8, 8));
      this.textDictInverse.put('?', new SpriteProxy(text, 58, 41, 8, 8));
      this.textDictInverse.put('!', new SpriteProxy(text, 74, 41, 8, 8));
      this.textDictInverse.put('.', new SpriteProxy(text, 122, 41, 8, 8));
      this.textDictInverse.put(',', new SpriteProxy(text, 138, 41, 8, 8));
      this.textDictInverse.put('é', new SpriteProxy(text, 154, 41, 8, 8));
      this.textDictInverse.put('É', new SpriteProxy(text, 154, 41, 8, 8));
      this.textDictInverse.put('-', new SpriteProxy(text, 170, 41, 8, 8));
      this.textDictInverse.put('\'', new SpriteProxy(text, 186, 41, 8, 8));
      this.textDictInverse.put('ì', new SpriteProxy(text, 202, 41, 8, 8));
      this.textDictInverse.put('\u0092', new SpriteProxy(text, 136, 45, 8, 8));
      this.transparentDict.put('Í', new SpriteProxy(text, 136, 49, 8, 8));
      this.textDictInverse.put(null, new SpriteProxy(text, 10, 53, 8, 8));
   }

   public Map<Character, SpriteProxy> initTextDict() {
      Map<Character, SpriteProxy> textDict = new HashMap<>();
      Texture text = TextureCache.get(Gdx.files.internal("text_sheet1.png"));
      char[] alphabet_upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
      char[] alphabet_lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();

      for (int i = 0; i < 26; i++) {
         textDict.put(alphabet_upper[i], new SpriteProxy(text, 10 + 16 * i, 5, 8, 8));
      }

      for (int i = 0; i < 26; i++) {
         textDict.put(alphabet_lower[i], new SpriteProxy(text, 10 + 16 * i, 17, 8, 8));
      }

      for (int i = 0; i < 10; i++) {
         textDict.put(Character.forDigit(i, 10), new SpriteProxy(text, 10 + 16 * i, 29, 8, 8));
      }

      textDict.put(' ', new SpriteProxy(text, 170, 29, 8, 8));
      textDict.put('<', new SpriteProxy(text, 10, 41, 8, 8));
      textDict.put('>', new SpriteProxy(text, 26, 41, 8, 8));
      textDict.put('_', new SpriteProxy(text, 42, 41, 8, 8));
      textDict.put('?', new SpriteProxy(text, 58, 41, 8, 8));
      textDict.put('!', new SpriteProxy(text, 74, 41, 8, 8));
      textDict.put('.', new SpriteProxy(text, 122, 41, 8, 8));
      textDict.put(',', new SpriteProxy(text, 138, 41, 8, 8));
      textDict.put('é', new SpriteProxy(text, 154, 41, 8, 8));
      textDict.put('É', new SpriteProxy(text, 154, 41, 8, 8));
      textDict.put('-', new SpriteProxy(text, 170, 41, 8, 8));
      textDict.put('\'', new SpriteProxy(text, 186, 41, 8, 8));
      textDict.put('ì', new SpriteProxy(text, 202, 41, 8, 8));
      textDict.put('\u0092', new SpriteProxy(text, 136, 45, 8, 8));
      textDict.put(null, new SpriteProxy(text, 10, 53, 8, 8));
      return textDict;
   }

   public static FileHandle checkForMods(String path) {
      FileHandle file = Gdx.files.local("mods/" + path);
      if (!file.exists()) {
         file = Gdx.files.internal(path);
      }

      return file;
   }

   public void insertAction(Action actionToInsert) {
      if (actionToInsert != null) {
         for (int i = 0; i < this.actionStack.size(); i++) {
            if (actionToInsert.getLayer().ordinal() >= this.actionStack.get(i).getLayer().ordinal()) {
               this.actionStack.add(i, actionToInsert);
               return;
            }
         }

         this.actionStack.add(actionToInsert);
      }
   }

   @Override
   public void pause() {
   }

   @Override
   public void render() {
      if (this.coop != null) {
         this.coop.beforeFrame();
         if (this.coop.simulationBlocked()) {
            this.uiBatch.begin(); this.coop.drawStatus(); this.uiBatch.end();
            return;
         }
      }
      this.profiler.reset();
      if (this.debugInputEnabled && (this.coop == null || !this.coop.hud.blocksInput())) {
         this.handleDebuggingInput();
      }

      if (Gdx.input.isKeyJustPressed(141)) {
         this.toggleFullScreen();
      }

      if (this.map != null && this.map.bottomLeft != null && this.map.topRight != null) {
         if (this.cam.position.x - this.currScreen.x / 6.0F < this.map.bottomLeft.x) {
            this.cam.position.x = this.map.bottomLeft.x + this.currScreen.x / 6.0F;
         }

         if (this.cam.position.x + this.currScreen.x / 6.0F > this.map.topRight.x + 16.0F) {
            this.cam.position.x = this.map.topRight.x - this.currScreen.x / 6.0F + 16.0F;
         }

         if (this.cam.position.y - this.currScreen.y / 6.0F < this.map.bottomLeft.y) {
            this.cam.position.y = this.map.bottomLeft.y + this.currScreen.y / 6.0F;
         }
      }

      this.cam.update();
      this.mapBatch.setProjectionMatrix(this.cam.combined);
      this.mapBatch.begin();
      if (!this.player.dontDrawMapDuringBattle && canUseFrameBuffers && this.mapBatch.getColor().r < 1.0F) {
         this.frameBuffer.begin();
         Gdx.gl.glClear(16384);
         this.temp1 = this.mapBatch.getBlendSrcFunc();
         this.temp2 = this.mapBatch.getBlendDstFunc();
         this.mapBatch.setBlendFunctionSeparate(770, 771, 1, 1);
         this.drawCampfireAuras.step(this);
         this.mapBatch.setBlendFunction(this.temp1, this.temp2);
         this.mapBatch.flush();
         this.frameBuffer.end();
      }

      Gdx.gl.glClear(16384);
      this.prevColor.set(this.mapBatch.getColor());
      if (CycleDayNight.fadeToDay) {
         if (CycleDayNight.fadeToDayAnim.index == 1) {
            this.currCloudTexture = this.cloudTexture;
         }

         this.currSunsetColor.set(CycleDayNight.fadeToDayAnim.currentThing());
      } else if (CycleDayNight.fadeToNight) {
         if (CycleDayNight.animContainer.index == 0) {
            this.currSunsetColor.set(1.0F, 0.9F, 0.5F, 1.0F);
            this.mapBatch.setColor(this.currSunsetColor);
         } else if (CycleDayNight.animContainer.index == 1) {
            this.currSunsetColor.set(0.9F, 0.55F, 0.2F, 1.0F);
            this.mapBatch.setColor(this.currSunsetColor);
         } else if (CycleDayNight.animContainer.index == 2) {
            this.currSunsetColor.set(0.4F, 0.3F, 0.3F, 1.0F);
            this.mapBatch.setColor(this.currSunsetColor);
         } else if (CycleDayNight.animContainer.index == 3) {
            this.currSunsetColor.set(CycleDayNight.animContainer.currentThing());
            this.currCloudTexture = this.nightTexture;
         }
      }

      if (!this.player.dontDrawMapDuringBattle && this.map != null && this.map.tiles == this.map.overworldTiles) {
         this.startPos.set(this.cam.position.x - 576.0F, this.cam.position.y - 384.0F);
         this.endPos.set(this.cam.position.x + 576.0F, this.cam.position.y + 192.0F);
         boolean flipY = false;
         if (this.map.topRight != null && this.cam.position.y > this.map.topRight.y - 384.0F) {
            flipY = true;
         }

         Vector2 currPos = this.startPos.cpy();

         while (currPos.y <= this.endPos.y) {
            this.mapBatch
               .draw(
                  this.currCloudTexture,
                  currPos.x - this.cam.position.x / 1.5F % 192.0F + this.cloudOffset.x,
                  currPos.y - this.cam.position.y / 1.5F % 192.0F,
                  this.currCloudTexture.getWidth(),
                  this.currCloudTexture.getHeight(),
                  0,
                  0,
                  this.currCloudTexture.getWidth(),
                  this.currCloudTexture.getHeight(),
                  false,
                  flipY || this.otherFlipY
               );
            currPos.x += 192.0F;
            if (currPos.x > this.endPos.x) {
               currPos.x = this.startPos.x;
               currPos.y += 192.0F;
            }
         }

         this.otherFlipY = false;
         if (flipY) {
            this.mapBatch
               .draw(
                  this.islandTexture,
                  this.cam.position.x + 256.0F - this.cam.position.x / 1.5F % 1024.0F,
                  this.map.topRight.y - 5.0F - (this.map.topRight.y - this.cam.position.y) / 6.0F
               );
         }

         this.rippleTimer++;
         if (this.rippleTimer >= 48) {
            this.rippleTimer = 0;
         }

         if (!this.map.timeOfDay.equals("night")) {
            this.cloudOffset.y++;
            if (this.cloudOffset.y % 16.0F == 0.0F) {
               this.cloudOffset.x--;
               if (this.cloudOffset.x < -192.0F) {
                  this.cloudOffset.x = 0.0F;
               }
            }

            if (this.cloudOffset.y > 64.0F) {
               this.cloudOffset.y = 0.0F;
            }
         }
      }

      this.mapBatch.setColor(this.prevColor);
      this.actionStackCopy.clear();
      this.actionStackCopy.addAll(this.actionStack);

      for (Action action : this.actionStackCopy) {
         if (action != null && action.getCamera().equals("map")) {
            try {
               if (action.firstStep) {
                  action.firstStep(this);
                  action.firstStep = false;
               }

               action.step(this);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      this.mapBatch.end();
      if (this.uiBatch.isDrawing()) {
         this.uiBatch.end();
      }

      this.uiBatch.begin();
      this.actionStackCopy.clear();
      this.actionStackCopy.addAll(this.actionStack);

      for (Action action : this.actionStackCopy) {
         if (action != null) {
            try {
               if (action.getCamera().equals("gui")) {
                  if (action.firstStep) {
                     action.firstStep(this);
                     action.firstStep = false;
                  }

                  action.step(this);
               }
            } catch (Exception e) {
               e.printStackTrace();
               System.out.println(action);
            }
         }
      }

      if (this.dangerousDebugInputEnabled && this.map != null && this.textDict != null) {
         char[] text = ("FPS " + String.valueOf(Gdx.graphics.getFramesPerSecond())).toCharArray();

         for (int i = 0; i < text.length; i++) {
            this.uiBatch.draw(this.textDict.get(text[i]), 104 + i * 8, 136.0F);
         }
      }

      if (this.coop != null) this.coop.drawStatus();
      this.uiBatch.end();
      if (this.coop != null) this.coop.afterFrame();
      if (!PkmnMap.PeriodicSave.isSaveOld()) {
         PkmnMap.PeriodicSave.increaseTimer();
      }
   }

   private void toggleFullScreen() {
      if (this.inFullScreen) {
         Gdx.graphics.setUndecorated(false);
         Gdx.graphics.setWindowedMode(this.oldWindowSize.x, this.oldWindowSize.y);
      } else {
         this.oldWindowSize.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         Gdx.graphics.setUndecorated(true);
         DisplayMode displayMode = Gdx.graphics.getDisplayMode();
         Gdx.graphics.setWindowedMode(displayMode.width, displayMode.height + 1);
      }

      this.inFullScreen = !this.inFullScreen;
   }

   @Override
   public void resize(int width, int height) {
      if (height > 0) {
         this.viewport.update(width + width % 2, height + height % 2);
         if (Gdx.app.getType() == ApplicationType.Android) {
            int menuHeight = 160 * height / width;
            int offsetY = (menuHeight - 144) / 2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(0.0F, -offsetY, 160.0F, menuHeight);
            int viewportWidth = 160;
            System.out.printf("Setting android width = %f, height = %f%n", this.cam.viewportWidth, this.cam.viewportHeight);
         } else {
            float pixelHeight = 0.0F;
            if (height % 2 == 1) {
               pixelHeight = 144.0F / height;
            }

            int menuWidth = 144 * width / height;
            int offsetX = (menuWidth - 160) / 2;
            this.uiBatch.getProjectionMatrix().setToOrtho2D(-offsetX, 0.0F, menuWidth, 144.0F - pixelHeight);
            int scale = this.scale;
            if (this.cinematic) {
               scale++;
            }

            int viewportWidth = Math.round((float)width / scale - (float)width / scale % 2.0F);
            int viewportHeight = Math.round((float)height / scale - (float)height / scale % 2.0F);
            System.out.printf("Setting else width = %f, height = %f%n", this.cam.viewportWidth, this.cam.viewportHeight);
         }

         this.currScreen.set(width, height);
      }
   }

   @Override
   public void resume() {
   }

   public void start() {
      if (drawReflections) {
         this.insertAction(new DrawReflections(this));
      }

      this.insertAction(new DrawMap(this.profiler));
      this.insertAction(new DrawPokemonLowers(this.profiler));
      this.insertAction(new DrawMapGrass(this.profiler));
      if (this.type != Game.Type.SERVER) {
         this.insertAction(new PlayerStanding(this, null, false, false));
         this.insertAction(new DrawBuildTile(this.profiler));
         this.insertAction(new DrawBuildRequirements(this.profiler));
      }

      this.insertAction(new MusicController(this));
      this.insertAction(new DrawMapTrees(this.profiler));
      this.insertAction(new MoveWater(this));
      this.insertAction(this.map.upkeepTimers);
      this.playerCanMove = true;
      this.insertAction(new CycleDayNight(this));
      if (drawOverlays) {
         this.insertAction(new FogEffect());
      }

      this.player.pokemon.add(new Pokemon("machop", 6));
      if (this.debugInputEnabled) {
         this.player.pokemon.clear();
         this.player.pokemon.add(new Pokemon("breloom", 60));
         this.player.pokemon.add(new Pokemon("rapidash", 60));
         this.player.pokemon.add(new Pokemon("pidgeot", 60, Pokemon.Generation.CRYSTAL));
         this.player.pokemon.add(new Pokemon("rhydon", 46, Pokemon.Generation.CRYSTAL));
         this.player.pokemon.add(new Pokemon("meganium", 46, Pokemon.Generation.CRYSTAL));
         Log.set(2);
      }

      for (Pokemon pokemon : this.player.pokemon) {
         pokemon.previousOwner = this.player;
      }

      for (Pokemon currPokemon : this.player.pokemon) {
         if (currPokemon.currentStats.get("hp") > 0) {
            this.player.currPokemon = currPokemon;
            break;
         }
      }
   }

   public static String capitalize(String str) {
      return str != null && !str.isEmpty() ? str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1) : str;
   }

   public void loadSettings(FileHandle file) {
      try {
         Reader reader = file.reader();
         BufferedReader br = new BufferedReader(reader);
         HashMap<String, String> values = new HashMap<>();

         String line;
         while ((line = br.readLine()) != null) {
            String[] vals = line.split("\\/\\/")[0].trim().split("=");
            if (vals.length >= 2) {
               values.put(vals[0], vals[1]);
            }
         }

         reader.close();
         boolean overwriteFile = false;

         for (int i = 0; i < defaultSettings.length; i++) {
            String v = values.get(defaultSettings[i][0]);
            if (v == null) {
               overwriteFile = true;
               values.put(defaultSettings[i][0], defaultSettings[i][1]);
            }
         }

         InputProcessor.keyboardA = Keys.valueOf(capitalize(values.get("keyboard-A")));
         InputProcessor.keyboardB = Keys.valueOf(capitalize(values.get("keyboard-B")));
         InputProcessor.keyboardLeft = Keys.valueOf(capitalize(values.get("keyboard-Left")));
         InputProcessor.keyboardRight = Keys.valueOf(capitalize(values.get("keyboard-Right")));
         InputProcessor.keyboardUp = Keys.valueOf(capitalize(values.get("keyboard-Up")));
         InputProcessor.keyboardDown = Keys.valueOf(capitalize(values.get("keyboard-Down")));
         InputProcessor.keyboardStart = Keys.valueOf(capitalize(values.get("keyboard-Start")));
         InputProcessor.keyboardL = Keys.valueOf(capitalize(values.get("keyboard-L")));
         InputProcessor.keyboardR = Keys.valueOf(capitalize(values.get("keyboard-R")));
         musicDisabled = Boolean.valueOf(values.get("muteMusic"));
         specialPhysicalSplitEnabled = Boolean.valueOf(values.get("specPhysSplitEnabled"));
         photosensitiveMode = Boolean.valueOf(values.get("photosensitiveMode"));
         battleAnims = Boolean.valueOf(values.get("battleAnims"));
         textSpeed = TextSpeed.parse(values.get("textSpeed"));
         float zoom = Float.valueOf(values.get("zoom") + "f");
         if (zoom > 0.0F) {
            this.scale = (int)(zoom * this.scale);
         }

         drawReflections = Boolean.valueOf(values.get("drawReflections"));
         drawOverlays = Boolean.valueOf(values.get("drawOverlays"));
         if (values.containsKey("frameBuffersEnabled")) {
            canUseFrameBuffers = Boolean.valueOf(values.get("frameBuffersEnabled"));
         }

         if (gamepad != null) {
            if (values.get("gamepad-A").equals("B")) {
               InputProcessor.gamepadA1 = gamepad.getMapping().buttonB;
               InputProcessor.gamepadA2 = gamepad.getMapping().buttonY;
            } else {
               InputProcessor.gamepadA1 = gamepad.getMapping().buttonA;
               InputProcessor.gamepadA2 = gamepad.getMapping().buttonX;
            }

            if (values.get("gamepad-B").equals("A")) {
               InputProcessor.gamepadB1 = gamepad.getMapping().buttonA;
               InputProcessor.gamepadB2 = gamepad.getMapping().buttonX;
            } else {
               InputProcessor.gamepadB1 = gamepad.getMapping().buttonB;
               InputProcessor.gamepadB2 = gamepad.getMapping().buttonY;
            }

            InputProcessor.gamepadMinAxis = Double.valueOf(values.get("gamepadDeadZone"));
         }

         if (overwriteFile) {
            String settingsString = "";

            for (int i = 0; i < defaultSettings.length; i++) {
               settingsString = settingsString + defaultSettings[i][0] + "=" + values.get(defaultSettings[i][0]);
               if (defaultSettings[i][0].equals("gamepad-A") || defaultSettings[i][0].equals("gamepad-B")) {
                  settingsString = settingsString + "     // A or B";
               }

               if (i < defaultSettings.length - 1) {
                  settingsString = settingsString + "\r\n";
               }
            }

            file.writeString(settingsString, false);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void saveErrorLogAndNotifyUser(String message, Throwable throwable) {
      File logFile = new File("error.log");

      try {
         PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));

         try {
            OffsetDateTime now = OffsetDateTime.now();
            writer.print(now.format(DateTimeFormatter.ISO_DATE_TIME));
            writer.print(" ");
            StackTraces.writeStackTrace(throwable, writer);
            StringWriter traceback = new StringWriter();
            StackTraces.writeStackTrace(throwable, new PrintWriter(traceback));
            JOptionPane.showMessageDialog(
               null,
               message
                  + traceback.toString()
                  + "\n\nPlease report this issue on the PokeWilds discord or Github, and attach "
                  + logFile.getAbsolutePath()
                  + " to the bug report."
            );
         } catch (Throwable var7) {
            try {
               writer.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         writer.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   static {
      evoStones.add("water stone");
      evoStones.add("fire stone");
      evoStones.add("leaf stone");
      evoStones.add("thunderstone");
      evoStones.add("moon stone");
      evoStones.add("sun stone");
      evoStones.add("ice stone");
      evoStones.add("dawn stone");
      evoStones.add("dusk stone");
      evoStones.add("shiny stone");
      evoStones.add("sweet apple");
   }

   public enum CacheLayer {
      FireAuras,
      Reflections;
   }

   public static class SetCamPos extends Action {
      Vector2 pos;

      @Override
      public void step(Game game) {
         game.cam.position.set(this.pos.x, this.pos.y, 0.0F);
         game.actionStack.remove(this);
         game.insertAction(this.nextAction);
      }

      public SetCamPos(Vector2 pos, Action nextAction) {
         super();
         this.pos = pos;
         this.nextAction = nextAction;
      }
   }

   enum Type {
      CLIENT,
      SERVER;
   }
}
