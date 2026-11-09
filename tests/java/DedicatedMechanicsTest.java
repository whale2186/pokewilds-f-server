package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.util.*;

/** Real authority transactions exercise generation, simulation, harvesting, and capture persistence state. */
public final class DedicatedMechanicsTest {
    public static void main(String[] args) throws Exception {
        if (!DedicatedSpecies.get("araichu").types.equals("ELECTRIC PSYCHIC")) throw new AssertionError("Uncommented species types not parsed");
        DedicatedServer.Snapshot state = DedicatedWorld.generate("mechanics", 1234);
        verifyFollowerVisuals();
        DedicatedWorld.addBuildingRoutes(state.world);
        final LanSession[] authority = new LanSession[1];
        try (LanSession host = LanSession.host("127.0.0.1", 0, "server", Json.gson.toJson(state.world), state.resources,
                (id, actor, key, before, after) -> LanCoop.allowWorldMutation(state.world, authority[0] != null && id.equals(authority[0].id()), actor, key, before, after))) {
            authority[0] = host;
            java.nio.file.Path tradeDirectory=java.nio.file.Files.createTempDirectory("headless-trade-");
            LanCoop.installTradePersistence(host,tradeDirectory);
            String key = null;
            for (Map.Entry<String, String> entry : state.resources.entrySet()) if (entry.getKey().startsWith("pokemon:")) {
                LanCoop.CreatureInfo info = Json.gson.fromJson(entry.getValue(), LanCoop.CreatureInfo.class);
                if (info.layer == -1 && DedicatedSpecies.get(info.data.name).habitat(Json.gson.fromJson(state.resources.get(tileKey(info)), Network.TileData.class))) { key = entry.getKey(); break; }
            }
            if (key == null) throw new AssertionError("No creatures in habitat");
            LanSession.Value value = host.resources().get(key);
            LanCoop.CreatureInfo creature = Json.gson.fromJson(value.json, LanCoop.CreatureInfo.class);
            creature.data.harvestTimer = 8999;
            // Keep this creature in habitat so the timer deterministically completes.
            creature.data.lanOwnerId = "owner";
            update(host, key, value.revision, Json.gson.toJson(creature));
            String abandonedKey = null;
            for (String candidate : state.resources.keySet()) if (candidate.startsWith("pokemon:") && !candidate.equals(key)) { abandonedKey = candidate; break; }
            LanSession.Value abandonedValue = host.resources().get(abandonedKey);
            LanCoop.CreatureInfo abandoned = Json.gson.fromJson(abandonedValue.json, LanCoop.CreatureInfo.class);
            abandoned.encounterOwner = "disconnected-player";
            update(host, abandonedKey, abandonedValue.revision, Json.gson.toJson(abandoned));
            Map<String, LanSession.Value> before = host.resources();
            DedicatedSimulation simulation = new DedicatedSimulation(state.world, new Random(4));
            for (int i = 0; i < 8; i++) { simulation.tick(host); Thread.sleep(80); }
            creature = Json.gson.fromJson(host.resources().get(key).json, LanCoop.CreatureInfo.class);
            if (creature.harvestable == null) throw new AssertionError("Harvest timer stalled");
            boolean moved = false;
            for (Map.Entry<String, LanSession.Value> entry : before.entrySet()) if (entry.getKey().startsWith("pokemon:")) {
                LanCoop.CreatureInfo old = Json.gson.fromJson(entry.getValue().json, LanCoop.CreatureInfo.class);
                LanCoop.CreatureInfo now = Json.gson.fromJson(host.resources().get(entry.getKey()).json, LanCoop.CreatureInfo.class);
                if (!old.data.position.equals(now.data.position)) {
                    moved = true;
                    if (!DedicatedSimulation.walkable(Json.gson.fromJson(host.resources().get(tileKey(now)).json, Network.TileData.class), DedicatedSpecies.get(now.data.name))) throw new AssertionError("Moved into obstacle");
                }
            }
            if (!moved) throw new AssertionError("Roaming stalled");
            if (Json.gson.fromJson(host.resources().get(abandonedKey).json, LanCoop.CreatureInfo.class).encounterOwner != null) throw new AssertionError("Disconnected battle remained reserved");
            try (LanSession client = LanSession.join("127.0.0.1", host.port(), "player")) {
                client.pose(new LanSession.Pose(client.id(), "player", "gold", "down", state.world.mapId, creature.layer, creature.data.position.x, creature.data.position.y, false));
                Thread.sleep(100);
                String trade="trade:"+UUID.randomUUID();
                update(client,trade,0L,client.id());
                LanCoop.ReceiptStore saved=com.pkmngen.game.util.Save.readData(tradeDirectory.resolve("coop-receipts").toString(),LanCoop.ReceiptStore.class);
                if(!client.id().equals(saved.values.get(trade))) throw new AssertionError("Headless trade decision not durable");
                String terrainKey = tileKey(creature);
                LanSession.Value terrain = client.resources().get(terrainKey);
                Network.TileData chopped = Json.gson.fromJson(terrain.json, Network.TileData.class);
                chopped.tileNameUpper = ""; chopped.items = new HashMap<>();
                update(client, terrainKey, terrain.revision, Json.gson.toJson(chopped));
                chopped.tileName = "snow1"; chopped.routeBelongsTo = "terrain-snow1";
                chopped.tileNameUpper = "fence1";
                update(client, terrainKey, client.resources().get(terrainKey).revision, Json.gson.toJson(chopped));
                if (!"fence1".equals(Json.gson.fromJson(host.resources().get(terrainKey).json, Network.TileData.class).tileNameUpper)) throw new AssertionError("Building edit missing");
                value = client.resources().get(key);
                creature.harvestable = null; creature.data.harvestTimer = 0;
                update(client, key, value.revision, Json.gson.toJson(creature));
                creature.encounterOwner = client.id();
                update(client, key, client.resources().get(key).revision, Json.gson.toJson(creature));
                try (LanSession other = LanSession.join("127.0.0.1", host.port(), "other")) {
                    other.pose(new LanSession.Pose(other.id(), "other", "gold", "down", state.world.mapId, creature.layer, creature.data.position.x, creature.data.position.y, false));
                    Thread.sleep(100);
                    String tx = UUID.randomUUID().toString();
                    other.transact(tx, Collections.singletonMap(key, other.resources().get(key).revision), Collections.singletonMap(key, (String)null));
                    long deadline = System.nanoTime() + 5_000_000_000L;
                    boolean rejected = false;
                    while (System.nanoTime() < deadline) {
                        LanSession.TransactionResult result = other.drainTransactions().get(tx);
                        if (result != null) { rejected = !result.accepted; break; }
                        Thread.sleep(10);
                    }
                    if (!rejected) throw new AssertionError("Another player captured a reserved creature");
                }
                simulation.tick(host); Thread.sleep(100);
                if (!client.id().equals(Json.gson.fromJson(host.resources().get(key).json, LanCoop.CreatureInfo.class).encounterOwner)) throw new AssertionError("AI changed battle reservation");
                // The same deletion used by capture must remain deleted on subsequent AI ticks.
                update(client, key, client.resources().get(key).revision, null);
                simulation.tick(host); Thread.sleep(100);
                if (host.resources().get(key).json != null) throw new AssertionError("Captured creature resurrected");
            }
        }
        System.out.println("PASS");
    }
    private static void verifyFollowerVisuals() {
        Game game = Game.staticGame;
        Pokemon follower = new Pokemon("pikachu", 5, Pokemon.Generation.CRYSTAL, true);
        game.player.pokemon.add(follower); game.player.hmPokemon = follower;
        follower.position.set(game.player.position); follower.dirFacing = "left";
        follower.currOwSprite = follower.movingSprites.get("left");
        follower.new DrawLower(true);
        Player remote = new Player(); remote.position.set(game.player.position);
        game.insertAction(game.player.new Emote("!", null));
        int sharedCount = game.map.pokemon.size();
        LanVisual visual = LanVisual.read(LanVisual.capture(game), null, remote);
        if (visual == null || visual.follower == null || !visual.shiny || visual.phase != 1 || visual.emoteSprite == null)
            throw new AssertionError("Follower or interaction appearance lost");
        if (game.map.pokemon.size() != sharedCount) throw new AssertionError("Remote follower entered shared world");
        game.player.hmPokemon = null;
        LanVisual recalled = LanVisual.read(LanVisual.capture(game), visual, remote);
        if (recalled == null || recalled.follower != null) throw new AssertionError("Recalled follower remained visible");
        game.player.currSprite = new com.badlogic.gdx.graphics.g2d.Sprite(game.player.altMovingSprites.get("down"));
        game.player.currSprite.flip(true, false);
        if (LanVisual.read(LanVisual.capture(game), null, remote) == null) throw new AssertionError("Flipped interaction frame lost");
    }
    private static String tileKey(LanCoop.CreatureInfo info) { return "tile:" + info.layer + ":" + (int)info.data.position.x + ":" + (int)info.data.position.y; }
    private static void update(LanSession session, String key, long revision, String json) throws Exception {
        String id = UUID.randomUUID().toString();
        if (!session.transact(id, Collections.singletonMap(key, revision), Collections.singletonMap(key, json))) throw new AssertionError("Not queued");
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < deadline) {
            LanSession.TransactionResult result = session.drainTransactions().get(id);
            if (result != null) { if (!result.accepted) throw new AssertionError("Rejected " + key); return; }
            Thread.sleep(10);
        }
        throw new AssertionError("Transaction timeout");
    }
}
