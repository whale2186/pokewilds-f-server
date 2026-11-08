package com.pkmngen.game.util;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;

public class Json {
   public static final Gson gson = createGson();

   @NotNull
   private static Gson createGson() {
      return new GsonBuilder()
         .registerTypeAdapter(Vector2.class, new Json.Vector2TypeAdapter())
         .registerTypeAdapter(GridPoint2.class, new Json.GridPoint2TypeAdapter())
         .enableComplexMapKeySerialization()
         .create();
   }

   private static class GridPoint2TypeAdapter extends Json.KeyTypeAdapter<GridPoint2> {
      private GridPoint2TypeAdapter() {
      }

      protected int getKey(GridPoint2 coordinate) {
         return coordinate.x << 16 | coordinate.y & 65535;
      }

      protected GridPoint2 getCoordinate(int key) {
         return new GridPoint2().set(key >> 16, (short)key);
      }
   }

   private abstract static class KeyTypeAdapter<T> implements JsonDeserializer<T>, JsonSerializer<T> {
      private KeyTypeAdapter() {
      }

      protected abstract int getKey(T var1);

      protected abstract T getCoordinate(int var1);

      @Override
      public final T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         int key = json.getAsJsonPrimitive().getAsInt();
         return this.getCoordinate(key);
      }

      @Override
      public final JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
         return new JsonPrimitive(this.getKey(src));
      }
   }

   private static class Vector2TypeAdapter extends Json.KeyTypeAdapter<Vector2> {
      private Vector2TypeAdapter() {
      }

      protected int getKey(Vector2 coordinate) {
         return (int)coordinate.x << 16 | (int)coordinate.y & 65535;
      }

      protected Vector2 getCoordinate(int key) {
         return new Vector2().set(key >> 16, (short)key);
      }
   }
}
