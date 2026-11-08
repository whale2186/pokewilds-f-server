package com.pkmngen.game;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
public final class LanMetadataTest {
    static LanCoop.WorldInfo valid() {
        LanCoop.WorldInfo value = new LanCoop.WorldInfo();
        value.worldId="island"; value.worldUid="12345678-1234-1234-1234-123456789abc"; value.mapId="0,0"; value.timeOfDay="day";
        value.bottomLeft=new Vector2(-160,-160); value.topRight=new Vector2(160,160); value.spawn=new Vector2();
        value.edges=new ArrayList<>(); value.edges.add(new Vector2()); value.interiorIndex=100;
        return value;
    }
    static void reject(LanCoop.WorldInfo value) {
        try { LanCoop.validateWorld(value); throw new AssertionError("accepted dangerous metadata"); }
        catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) {
        LanCoop.validateWorld(valid());
        LanCoop.WorldInfo value=valid(); value.worldId="../../escaped"; reject(value);
        value=valid(); value.spawn.x=Float.NaN; reject(value);
        value=valid(); value.topRight.x=Float.POSITIVE_INFINITY; reject(value);
        value=valid(); value.topRight.x=30000; reject(value);
        value=valid(); value.interiorIndex=1000000; reject(value);
        value=valid(); value.mapId="../world"; reject(value);
        com.pkmngen.game.multiplayer.LanSession.Pose actor = new com.pkmngen.game.multiplayer.LanSession.Pose(
                "guest", "guest", "leaf", "up", "0,0", -1, 0, 0, false);
        Network.TileData tile = new Network.TileData();
        tile.pos = new Vector2(0,16); tile.interiorIndex = -1; tile.tileName = "green1"; tile.tileNameUpper = "";
        String cut = com.pkmngen.game.util.Json.gson.toJson(tile);
        if (!LanCoop.allowWorldMutation(valid(), false, actor, "tile:-1:0:16", "tree", cut))
            throw new AssertionError("nearby field edit rejected");
        if (LanCoop.allowWorldMutation(valid(), false, actor, "tile:-1:0:160", "tree", cut))
            throw new AssertionError("distant field edit accepted");
        tile.hasItemAmount = -1;
        if (LanCoop.allowWorldMutation(valid(), false, actor, "tile:-1:0:16", "tree", com.pkmngen.game.util.Json.gson.toJson(tile)))
            throw new AssertionError("invalid harvest amount accepted");
        System.out.println("PASS LAN metadata and field action validation checks");
    }
}
