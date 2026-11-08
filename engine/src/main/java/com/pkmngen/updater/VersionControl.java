package com.pkmngen.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JOptionPane;

public class VersionControl {
   public String localVersion = "";
   public String latestVersion = "";
   private String versionEndpoint = "https://github.com/SheerSt/pokemon-wilds/releases/latest/download/version.txt";

   public void checkVersion() {
      try {
         URL url = new URL(this.versionEndpoint);
         InputStream in = url.openStream();
         URLConnection connection = url.openConnection();
         connection.connect();
         File versionFile = new File("version.txt");
         if (!versionFile.exists()) {
            versionFile.createNewFile();
         }

         BufferedReader installed = new BufferedReader(new FileReader(versionFile));
         String local = "";

         while ((local = installed.readLine()) != null) {
            System.out.println("Currently installed: " + local);
            this.localVersion = local;
         }

         installed.close();
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         String line = "";

         while ((line = reader.readLine()) != null) {
            System.out.println("Latest version: " + line);
            this.latestVersion = line;
         }

         reader.close();
         if (this.localVersion.equals("") || this.localVersion.isEmpty()) {
            this.localVersion = "Unknown";
         }

         if (!this.localVersion.matches(this.latestVersion)) {
            String text = "A new update is available!\nCurrently Installed: " + this.localVersion + "\nLatest: " + this.latestVersion + "\n\nClick to update.";
            if (JOptionPane.showConfirmDialog(null, text, "Update Available", 0) == 0) {
               this.runUpdater();
            }
         } else {
            System.out.println("Up to date");
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void runUpdater() throws IOException {
      String filePath = "./Updater.jar";
      Runtime runtime = Runtime.getRuntime();
      runtime.exec(" java -jar " + filePath);
      System.exit(0);
   }

   public String GetLatestVersion() {
      return this.latestVersion;
   }
}
