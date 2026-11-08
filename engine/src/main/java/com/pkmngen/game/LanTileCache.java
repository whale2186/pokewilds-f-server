package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Exact field snapshots, not hash-only invalidation; mutable collections are copied. */
final class LanTileCache {
    private static final class Entry {
        final Network.TileData data;
        final String json;
        Entry(Network.TileData value) {
            json = Json.gson.toJson(value);
            data = new Network.TileData();
            data.pos = value.pos == null ? null : value.pos.cpy();
            data.tileName = value.tileName; data.tileNameUpper = value.tileNameUpper;
            data.routeBelongsTo = value.routeBelongsTo;
            data.items = value.items == null ? null : new HashMap<>(value.items);
            data.hasItem = value.hasItem; data.hasItemAmount = value.hasItemAmount;
            data.biome = value.biome; data.isUnown = value.isUnown; data.message = value.message;
            data.interiorIndex = value.interiorIndex; data.squishedTiles = value.squishedTiles;
            if (value.doorTiles != null) {
                data.doorTiles = new ArrayList<>();
                for (Vector2 door : value.doorTiles) data.doorTiles.add(door.cpy());
            }
        }
        boolean matches(Network.TileData other) {
            return Objects.equals(data.pos, other.pos) && Objects.equals(data.tileName, other.tileName)
                    && Objects.equals(data.tileNameUpper, other.tileNameUpper) && Objects.equals(data.routeBelongsTo, other.routeBelongsTo)
                    && Objects.equals(data.items, other.items) && Objects.equals(data.hasItem, other.hasItem)
                    && data.hasItemAmount == other.hasItemAmount && Objects.equals(data.biome, other.biome)
                    && data.isUnown == other.isUnown && Objects.equals(data.message, other.message)
                    && data.interiorIndex == other.interiorIndex && Objects.equals(data.doorTiles, other.doorTiles)
                    && Objects.equals(data.squishedTiles, other.squishedTiles);
        }
    }
    private final Map<String,Entry> entries = new HashMap<>();
    String json(String key, Network.TileData data) {
        Entry entry = entries.get(key);
        if (entry == null || !entry.matches(data)) { entry = new Entry(data); entries.put(key, entry); }
        return entry.json;
    }
    String unchanged(String key, Tile tile, int layer, String route) {
        Entry entry = entries.get(key);
        if (entry == null) return null;
        Network.TileData data = entry.data;
        boolean tips = tile instanceof TrainerTipsTile;
        return Objects.equals(data.pos, tile.position) && Objects.equals(data.tileName, tile.name)
                && Objects.equals(data.tileNameUpper, tile.nameUpper) && Objects.equals(data.routeBelongsTo, route)
                && Objects.equals(data.items, tile.items) && Objects.equals(data.hasItem, tile.hasItem)
                && data.hasItemAmount == tile.hasItemAmount && Objects.equals(data.biome, tile.biome)
                && data.isUnown == (tips && ((TrainerTipsTile)tile).isUnown)
                && Objects.equals(data.message, tips ? ((TrainerTipsTile)tile).message : "")
                && data.interiorIndex == layer && Objects.equals(data.doorTiles, tile.doorTiles)
                && Objects.equals(data.squishedTiles, tile.squishedTiles) ? entry.json : null;
    }
    void retain(java.util.Set<String> keys) { entries.keySet().retainAll(keys); }
}
