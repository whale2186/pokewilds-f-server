package com.pkmngen.game.util;

import java.util.ArrayList;

public class TextField {
   private static final int ASCII_BACKSPACE = 8;
   private static final char[] SYMBOLS = new char[]{'.', ',', '-', '’', '?', '!', '<', '>', ' '};

   public static boolean updateField(char character, ArrayList<Character> characters, int maxLength, boolean allowSymbols) {
      if (character == '\b' && characters.size() > 0) {
         characters.remove(characters.size() - 1);
         return true;
      }

      if (characters.size() >= maxLength) {
         return false;
      }

      if ((character < 'a' || character > 'z') && (character < 'A' || character > 'Z') && (character < '0' || character > '9')) {
         if (allowSymbols) {
            for (int i = 0; i < SYMBOLS.length; i++) {
               char symbol = SYMBOLS[i];
               if (character == symbol) {
                  characters.add(character);
                  return true;
               }
            }
         }

         return false;
      } else {
         characters.add(character);
         return true;
      }
   }
}
