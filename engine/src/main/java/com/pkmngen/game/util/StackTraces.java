package com.pkmngen.game.util;

import java.io.PrintWriter;

public class StackTraces {
   public static void writeStackTrace(Throwable e, PrintWriter writer) {
      String message = e.getMessage();
      if (message != null) {
         writer.println(message);
         writer.print("\n");
      }

      for (StackTraceElement stackTraceElement : e.getStackTrace()) {
         writer.println(stackTraceElement);
      }

      if (e.getCause() != null) {
         writer.print("\nCaused by: ");
         writeStackTrace(e.getCause(), writer);
      }
   }
}
