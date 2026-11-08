package com.pkmngen.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.pkmngen.game.Game;
import com.pkmngen.game.PkmnMap;
import com.pkmngen.game.util.Dirs;
import com.pkmngen.leaks.JvmLeakTracer;
import com.pkmngen.leaks.LeakTracer;
import com.pkmngen.leaks.SharkLoggerImpl;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.lwjgl.system.Configuration;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import shark.SharkLog;

public class DesktopLauncher {
   public static void main(String[] args) {
      if (args.length > 0 && "--server".equals(args[0])) {
         com.pkmngen.game.DedicatedServer.main(java.util.Arrays.copyOfRange(args, 1, args.length));
         return;
      }
      try { com.pkmngen.game.LanCoop.validateArgs(args); }
      catch (IllegalArgumentException invalid) { System.err.println(invalid.getMessage()); System.exit(2); return; }
      allowMacOsToRunWithoutFlag();
      checkForUserHomeInTempFolder();
      int scale = getScale();
      Lwjgl3ApplicationConfiguration config = createConfig(scale);
      processArgs(args, config);
      System.out.print("Scale: ");
      System.out.println(scale);
      boolean shouldTraceLeaks = traceLeaks(args);
      if (shouldTraceLeaks) {
         SharkLog.INSTANCE.setLogger(new SharkLoggerImpl());
         Game.leakTracer = new JvmLeakTracer();
      } else {
         Game.leakTracer = LeakTracer.NoOp.INSTANCE;
      }

      try {
         new Lwjgl3Application(new Game(args, scale), config);
      } catch (Throwable throwable) {
         Game.saveErrorLogAndNotifyUser("PokeWilds crashed :( reason:\n\n", throwable);
      }
   }

   private static boolean traceLeaks(String[] args) {
      for (String arg : args) {
         if ("traceLeaks".equals(arg)) {
            return true;
         }
      }

      return false;
   }

   private static void allowMacOsToRunWithoutFlag() {
      if (SharedLibraryLoader.isMac) {
         Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
      }
   }

   private static void checkForUserHomeInTempFolder() {
      String savePath = new File("").getAbsoluteFile().getAbsolutePath().toLowerCase();
      System.out.println("Save path: " + savePath);
      if (savePath.startsWith("/private") || savePath.startsWith("c:\\windows\\system32")) {
         warnAboutSaveDir(savePath);
      }
   }

   private static void warnAboutSaveDir(String userHome) {
      String message = "Warning! The current save directory is: "
         + userHome
         + ". PokeWilds might not be able to save your game, or the save file might get lost, please check the README file for instructions, or ask for help on Discord.";
      JTextArea textArea = new JTextArea(message);
      Font font = textArea.getFont();
      textArea.setFont(font.deriveFont(42.0F));
      textArea.setWrapStyleWord(true);
      textArea.setLineWrap(true);
      textArea.setOpaque(false);
      textArea.setBorder(null);
      textArea.setEditable(false);
      textArea.setFocusable(false);
      JScrollPane scrollPane = new JScrollPane(textArea) {
         @Override
         public Dimension getPreferredSize() {
            return new Dimension(800, 600);
         }
      };
      JOptionPane.showMessageDialog(null, scrollPane, "Problem with save!", 0);
   }

   private static void processArgs(String[] args, Lwjgl3ApplicationConfiguration config) {
      for (String arg : args) {
         switch (arg.toLowerCase()) {
            case "angle_gles20":
               System.out.println("Using ANGLE_GLES20.");
               config.setOpenGLEmulation(GLEmulation.ANGLE_GLES20, 0, 0);
               break;
            case "testdirs":
               Dirs dirs = new Dirs();
               System.out.println("App dirs:");
               System.out.println("Config: " + dirs.configDir());
               System.out.println("Data: " + dirs.dataDir());
               System.exit(0);
         }
      }
   }

   private static Lwjgl3ApplicationConfiguration createConfig(int scale) {
      Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
      config.setWindowedMode(scale * 160, scale * 144);
      config.setTitle("PokeWilds");
      config.setWindowIcon("icon-64.png", "icon-32.png", "icon-256.png");
      config.setForegroundFPS(60);
      config.setWindowListener(
         new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
               if (!Game.staticGame.isDedicatedClient() && PkmnMap.PeriodicSave.isSaveOld()
                  && Game.staticGame.isMapLoaded()
                  && JOptionPane.showConfirmDialog(null, "Save your progress?", "WARNING", 0) == 0) {
                  Game.staticGame.saveGame();
               }

               return true;
            }
         }
      );

      try {
         printSystemInfo(config);
      } catch (Exception e) {
         e.printStackTrace();
      }

      return config;
   }

   private static int getScale() {
      int height = Lwjgl3ApplicationConfiguration.getDisplayMode().height;
      if (height <= 800) {
         return 2;
      } else {
         return height > 1080 ? 6 : 3;
      }
   }

   private static void printSystemInfo(Lwjgl3ApplicationConfiguration config) {
      SystemInfo systemInfo = new SystemInfo();
      HardwareAbstractionLayer hardware = systemInfo.getHardware();
      OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
      String os = operatingSystem.getFamily();
      String arch = hardware.getProcessor().getProcessorIdentifier().getMicroarchitecture();
      System.out.printf("os = %s%n", os);
      System.out.printf("model = %s%n", hardware.getComputerSystem().getModel());
      System.out.printf("arch = %s%n", arch);
   }
}
