package com.pkmngen.game;

import java.util.HashMap;
import com.badlogic.gdx.math.Vector2;

public final class LanTileCacheTest {
    public static void main(String[] args) {
        LanTileCache cache = new LanTileCache();
        Network.TileData data = new Network.TileData();
        data.pos = new Vector2(16, 32); data.tileName = "green9"; data.tileNameUpper = "";
        data.items = new HashMap<>(); data.items.put("log", 1);
        String first = cache.json("tile:-1:16:32", data);
        if (cache.json("tile:-1:16:32", data) != first) throw new AssertionError("Unchanged tile was reserialized");
        data.items.put("log", 2);
        String second = cache.json("tile:-1:16:32", data);
        if (first.equals(second)) throw new AssertionError("Mutable inventory change hidden by cache");
        data.message = "changed sign";
        if (second.equals(cache.json("tile:-1:16:32", data))) throw new AssertionError("Message change hidden");
        data.routeBelongsTo = "new-route";
        if (!cache.json("tile:-1:16:32", data).contains("new-route")) throw new AssertionError("Route change hidden");
        System.out.println("PASS exact tile cache reuse, nested mutation and metadata invalidation");
    }
}
