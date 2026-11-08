package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.nio.file.*;
import java.util.*;

/** Connects to a real headless authority, edits shared terrain, then verifies restart persistence. */
public final class DedicatedServerTest {
    public static void main(String[] args) throws Exception {
        // Same endpoint/name parsing used by Game.joinCoop from both menus.
        LanOptions options = LanOptions.forJoin("127.0.0.1:" + args[0], "test-player");
        try (LanSession client = LanSession.join(options.address, options.port, options.name, "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")) {
            LanCoop.WorldInfo world = Json.gson.fromJson(client.bootstrap(), LanCoop.WorldInfo.class);
            LanCoop.validateWorld(world);
            if (!world.dedicated) throw new AssertionError("Not dedicated");
            if (args.length > 1 && args[1].equals("probe")) {
                if (client.resources().size() < 100) throw new AssertionError("Missing world terrain");
                LanSession.Value clock = client.resources().get("clock");
                Thread.sleep(2200);
                if (!client.isConnected()) throw new AssertionError("Server disconnected");
                if (client.resources().get("clock").revision <= clock.revision) throw new AssertionError("Clock stalled");
            } else if (args.length > 1 && args[1].equals("verify")) {
                LanSession.Pose saved = client.peers().get(client.id());
                if (!client.hasSavedPosition() || saved.x != 16 || saved.y != 16 || saved.layer != -1 || !saved.direction.equals("left"))
                    throw new AssertionError("Player pose lost on dedicated restart");
                if (!"persisted".equals(Json.gson.fromJson(client.resources().get("tile:-1:0:0").json, Network.TileData.class).message))
                    throw new AssertionError("Edit lost on restart");
                Path captured = Paths.get("captured-key.txt");
                if (Files.exists(captured)) {
                    LanSession.Value value = client.resources().get(new String(Files.readAllBytes(captured), java.nio.charset.StandardCharsets.UTF_8));
                    if (value != null && value.json != null) throw new AssertionError("Captured creature returned on restart");
                }
            } else {
                client.pose(new LanSession.Pose(client.id(), "test-player", "gold", "down", world.mapId, -1, 0, 0, false));
                LanSession.Value value = client.resources().get("tile:-1:0:0");
                Network.TileData tile = Json.gson.fromJson(value.json, Network.TileData.class);
                tile.message = "persisted";
                String tx = UUID.randomUUID().toString();
                client.transact(tx, Collections.singletonMap("tile:-1:0:0", value.revision), Collections.singletonMap("tile:-1:0:0", Json.gson.toJson(tile)));
                long deadline = System.nanoTime() + 5_000_000_000L;
                boolean accepted = false;
                while (System.nanoTime() < deadline) {
                    LanSession.TransactionResult result = client.drainTransactions().get(tx);
                    if (result != null) { accepted = result.accepted; break; }
                    Thread.sleep(10);
                }
                if (!accepted) throw new AssertionError("Edit not accepted");
                for (Map.Entry<String, LanSession.Value> entry : client.resources().entrySet()) if (entry.getKey().startsWith("pokemon:") && entry.getValue().json != null) {
                    LanCoop.CreatureInfo info = Json.gson.fromJson(entry.getValue().json, LanCoop.CreatureInfo.class);
                    client.pose(new LanSession.Pose(client.id(), "test-player", "gold", "down", world.mapId, info.layer, info.data.position.x, info.data.position.y, false));
                    Thread.sleep(100);
                    String capture = UUID.randomUUID().toString();
                    LanSession.Value current = client.resources().get(entry.getKey());
                    client.transact(capture, Collections.singletonMap(entry.getKey(), current.revision), Collections.singletonMap(entry.getKey(), (String)null));
                    deadline = System.nanoTime() + 5_000_000_000L;
                    boolean captured = false;
                    while (System.nanoTime() < deadline) {
                        LanSession.TransactionResult result = client.drainTransactions().get(capture);
                        if (result != null) { captured = result.accepted; break; }
                        Thread.sleep(10);
                    }
                    if (!captured) throw new AssertionError("Capture mutation rejected");
                    Files.write(Paths.get("captured-key.txt"), entry.getKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    break;
                }
                client.pose(new LanSession.Pose(client.id(), "test-player", "gold", "left", world.mapId, -1, 16, 16, false));
                Thread.sleep(100);
            }
        }
        ServerList list = new ServerList();
        list.entries.add(new ServerList.Entry("Local test", "127.0.0.1:" + args[0])); list.save();
        ServerList restored = new ServerList(); restored.load();
        if (!restored.entries.get(0).name.equals("Local test")) throw new AssertionError("Bookmark lost");
        System.out.println("PASS");
    }
}
