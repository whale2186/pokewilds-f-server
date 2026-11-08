package com.pkmngen.game.util;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public class Dirs {
   private static final String APP_NAME = "pokewilds";
   private static final Dirs.DataVersion DATA_VERSION = Dirs.DataVersion.INITIAL;
   private static final Dirs.ConfigVersion CONFIG_VERSION = Dirs.ConfigVersion.INITIAL;
   private final AppDirs appDirs = AppDirsFactory.getInstance();

   public String dataDir() {
      return this.appDirs.getUserDataDir("pokewilds", DATA_VERSION.versionName, null);
   }

   public String configDir() {
      return this.appDirs.getUserConfigDir("pokewilds", CONFIG_VERSION.versionName, null);
   }

   private enum ConfigVersion {
      INITIAL("1.0.0");

      final String versionName;

      ConfigVersion(String versionName) {
         this.versionName = versionName;
      }
   }

   private enum DataVersion {
      INITIAL("1.0.0");

      final String versionName;

      DataVersion(String versionName) {
         this.versionName = versionName;
      }
   }
}
