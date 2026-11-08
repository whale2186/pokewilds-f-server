package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.util.*;

/** One-second authoritative steps using revision-checked writes, shared with client interactions. */
final class DedicatedSimulation {
    private final LanCoop.WorldInfo world;
    private final Random random;
    DedicatedSimulation(LanCoop.WorldInfo world, Random random) { this.world = world; this.random = random; }

    void tick(LanSession session) {
        Map<String, LanSession.Value> values = session.resources();
        Collection<LanSession.Pose> peers = session.peers().values();
        Set<String> occupied = new HashSet<>();
        Updates updates = new Updates(session);
        for (Map.Entry<String, LanSession.Value> entry : values.entrySet()) if (entry.getKey().startsWith("pokemon:") && entry.getValue().json != null) {
            LanCoop.CreatureInfo info = Json.gson.fromJson(entry.getValue().json, LanCoop.CreatureInfo.class);
            occupied.add(tileKey(0, info.data.position.x, info.data.position.y));
        }
        for (Map.Entry<String, LanSession.Value> entry : values.entrySet()) {
            if (!entry.getKey().startsWith("pokemon:") || entry.getValue().json == null) continue;
            LanCoop.CreatureInfo info = Json.gson.fromJson(entry.getValue().json, LanCoop.CreatureInfo.class);
            if (info.encounterOwner != null) {
                if (session.peers().containsKey(info.encounterOwner)) continue;
                info.encounterOwner = null;
                updates.add(entry.getKey(), entry.getValue().revision, Json.gson.toJson(info));
                continue;
            }
            // Clients own interactions and battles. Keep nearby creatures stable for their duration.
            boolean nearby = false;
            for (LanSession.Pose peer : peers) if (!peer.id.equals(session.id()) && world.mapId.equals(peer.mapId)
                    && peer.layer == info.layer && Math.abs(peer.x - info.data.position.x) <= 80 && Math.abs(peer.y - info.data.position.y) <= 80) nearby = true;
            if ("egg".equals(info.data.name) || info.data.hp <= 0) continue;
            DedicatedSpecies species;
            try { species = DedicatedSpecies.get(info.data.name); }
            catch (IllegalArgumentException | IllegalStateException unknown) { continue; } // Modded creatures remain client-interactable.
            String oldKey = tileKey(info.layer, info.data.position.x, info.data.position.y);
            Network.TileData ground = tile(values, oldKey);
            if (ground == null) continue;
            if (info.harvestable == null) {
                info.data.harvestTimer += habitatRate(values, info, species);
                if (info.data.harvestTimer >= species.harvestTimerMax) {
                    info.harvestable = species.harvest.get(random.nextInt(species.harvest.size()));
                    info.data.harvestTimer = 0;
                }
            }
            // Owned creatures stay where their owners placed them.
            if (!nearby && info.data.lanOwnerId == null && info.data.previousOwnerName == null && random.nextInt(3) == 0) {
                int direction = random.nextInt(4);
                int[] dx = {0, 0, -16, 16}, dy = {16, -16, 0, 0};
                String[] facing = {"up", "down", "left", "right"};
                float x = info.data.position.x + dx[direction], y = info.data.position.y + dy[direction];
                String nextKey = tileKey(info.layer, x, y);
                Network.TileData next = tile(values, nextKey);
                if (walkable(next, species) && !occupied.contains(tileKey(0, x, y))) {
                    occupied.remove(tileKey(0, info.data.position.x, info.data.position.y)); occupied.add(tileKey(0, x, y));
                    info.data.position.set(x, y); info.direction = facing[direction];
                }
            }
            String json = Json.gson.toJson(info);
            if (!json.equals(entry.getValue().json)) updates.add(entry.getKey(), entry.getValue().revision, json);
        }
        updates.flush();
    }
    /** Bound both batch size and encoded payload, avoiding a packet per creature each tick. */
    private static final class Updates {
        final LanSession session;
        final Map<String,Long> expected = new LinkedHashMap<>();
        final Map<String,String> proposed = new LinkedHashMap<>();
        int bytes;
        Updates(LanSession session) { this.session = session; }
        void add(String key, long revision, String json) {
            int size = json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length * 2 + key.length() * 2 + 256;
            if (expected.size() >= 32 || bytes + size > 200 * 1024) flush();
            expected.put(key, revision); proposed.put(key, json); bytes += size;
        }
        void flush() {
            if (expected.isEmpty()) return;
            session.drainTransactions();
            session.transact(UUID.randomUUID().toString(), expected, proposed);
            expected.clear(); proposed.clear(); bytes = 0;
        }
    }
    private static int habitatRate(Map<String, LanSession.Value> values, LanCoop.CreatureInfo info, DedicatedSpecies species) {
        Set<String> missing = new HashSet<>(species.habitats);
        int roofs = 0, fences = 0;
        for (int dx = -48; dx <= 48; dx += 16) for (int dy = -48; dy <= 48; dy += 16) {
            Network.TileData tile = tile(values, tileKey(info.layer, info.data.position.x + dx, info.data.position.y + dy));
            if (tile == null) continue;
            if (tile.tileNameUpper.contains("fence")) fences++;
            else if (tile.tileNameUpper.contains("roof")) roofs++;
            for (String group : species.habitats) if (species.matches(group, tile)) missing.remove(group);
        }
        return missing.isEmpty() ? roofs >= 3 && fences >= 2 ? 120 : 60 : 0;
    }
    private static String tileKey(int layer, float x, float y) { return "tile:" + layer + ":" + (int)x + ":" + (int)y; }
    private static Network.TileData tile(Map<String, LanSession.Value> values, String key) {
        LanSession.Value value = values.get(key);
        return value == null || value.json == null ? null : Json.gson.fromJson(value.json, Network.TileData.class);
    }
    static boolean walkable(Network.TileData tile, DedicatedSpecies species) {
        if (tile == null || tile.tileNameUpper == null || !tile.tileNameUpper.isEmpty()) return false;
        boolean water = tile.tileName != null && tile.tileName.startsWith("water");
        if (species.onlySwim) return water;
        if (water) return species.types.contains("WATER") || species.types.contains("FLYING");
        return walkable(tile);
    }
    static boolean walkable(Network.TileData tile) {
        if (tile == null || tile.tileNameUpper == null || !tile.tileNameUpper.isEmpty()) return false;
        String name = tile.tileName;
        return name != null && (name.startsWith("green") || name.startsWith("grass") || name.equals("sand1")
                || name.equals("mountain1") || name.equals("desert1") || name.equals("snow1") || name.equals("snow3") || name.equals("soot1") || name.equals("volcano1") || name.equals("flower1") || name.equals("ruins_floor2"));
    }
}
