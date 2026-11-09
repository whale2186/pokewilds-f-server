package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.util.*;

/** Authoritative pursuit and roaming use revision-checked writes shared with client interactions. */
final class DedicatedSimulation {
    private final LanCoop.WorldInfo world;
    private final Random random;
    DedicatedSimulation(LanCoop.WorldInfo world, Random random) { this.world = world; this.random = random; }

    private int wanderFrames;
    void tick(LanSession session) { tick(session,60,false); }
    void tick(LanSession session,int frames,boolean aggressionOnly) {
        wanderFrames += frames;
        boolean wander = wanderFrames >= 60;
        if (wander) wanderFrames %= 60;
        Map<String, LanSession.Value> values = session.resources();
        Collection<LanSession.Pose> peers = session.peers().values();
        Set<String> occupied = new HashSet<>();
        Updates updates = new Updates(session);
        for (Map.Entry<String, LanSession.Value> entry : values.entrySet()) if (entry.getKey().startsWith("pokemon:") && entry.getValue().json != null) {
            LanCoop.CreatureInfo info = Json.gson.fromJson(entry.getValue().json, LanCoop.CreatureInfo.class);
            occupied.add(tileKey(info.layer, info.data.position.x, info.data.position.y));
        }
        for (Map.Entry<String, LanSession.Value> entry : values.entrySet()) {
            if (!entry.getKey().startsWith("pokemon:") || entry.getValue().json == null) continue;
            LanCoop.CreatureInfo info = Json.gson.fromJson(entry.getValue().json, LanCoop.CreatureInfo.class);
            if (info == null || info.data == null || info.data.hp <= 0) continue;
            if (info.encounterOwner != null) {
                if (session.peers().containsKey(info.encounterOwner)) continue;
                info.encounterOwner = null;
                updates.add(entry.getKey(), entry.getValue().revision, Json.gson.toJson(info));
                continue;
            }
            // Idle roaming pauses near players; hostile pursuit below continues until an encounter is reserved.
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
            if (!aggressionOnly && info.harvestable == null) {
                info.data.harvestTimer += habitatRate(values, info, species) * frames / 60;
                if (info.data.harvestTimer >= species.harvestTimerMax) {
                    info.harvestable = species.harvest.get(random.nextInt(species.harvest.size()));
                    info.data.harvestTimer = 0;
                }
            }
            boolean owned = info.data.lanOwnerId != null || info.data.previousOwnerName != null;
            if (!owned && "drapion".equals(info.data.name)) info.data.aggroPlayer = true;
            if (!owned && info.data.aggroPlayer) {
                LanSession.Pose target = target(session,peers,info);
                info.pursuitTarget = target == null ? null : target.id;
                if (target != null) chase(values,occupied,info,species,target);
            } else {
                info.provokedBy = info.pursuitTarget = null;
                if (!aggressionOnly && !nearby && !owned && wander && random.nextInt(3) == 0) {
                    int direction = random.nextInt(4);
                    step(values,occupied,info,species,direction);
                }
            }
            String json = Json.gson.toJson(info);
            if (!json.equals(entry.getValue().json)) updates.add(entry.getKey(), entry.getValue().revision, json);
        }
        updates.flush();
    }
    private LanSession.Pose target(LanSession session,Collection<LanSession.Pose> peers,LanCoop.CreatureInfo info) {
        if (info.provokedBy != null) {
            for (LanSession.Pose peer:peers) if (peer.id.equals(info.provokedBy) && inRange(session,peer,info))
                return available(peer,info) ? peer : null;
            info.provokedBy = null;
        }
        LanSession.Pose best=null; float distance=Float.MAX_VALUE;
        for (LanSession.Pose peer:peers) if (inRange(session,peer,info) && available(peer,info)) {
            float dx=peer.x-info.data.position.x,dy=peer.y-info.data.position.y,d=dx*dx+dy*dy;
            if(d<distance || d==distance && best!=null && peer.id.compareTo(best.id)<0) { best=peer;distance=d; }
        }
        return best;
    }
    private boolean inRange(LanSession session,LanSession.Pose peer,LanCoop.CreatureInfo info) {
        float dx=peer.x-info.data.position.x,dy=peer.y-info.data.position.y;
        return (!world.dedicated || !peer.id.equals(session.id())) && world.mapId.equals(peer.mapId)
                && peer.layer==info.layer && dx*dx+dy*dy<14400;
    }
    private static boolean available(LanSession.Pose peer,LanCoop.CreatureInfo info) {
        try {
            if(peer.visual==null || peer.visual.isEmpty()) return true;
            com.google.gson.JsonObject visual=Json.gson.fromJson(peer.visual,com.google.gson.JsonObject.class);
            return visual!=null && !flag(visual,"busy") && !flag(visual,"flying")
                && (!visual.has("calmingLevel") || visual.get("calmingLevel").getAsInt()<info.data.level);
        } catch(RuntimeException invalid) { return false; }
    }
    private static boolean flag(com.google.gson.JsonObject visual,String name) {
        return visual.has(name) && visual.get(name).getAsBoolean();
    }
    private void chase(Map<String,LanSession.Value> values,Set<String> occupied,LanCoop.CreatureInfo info,DedicatedSpecies species,LanSession.Pose target) {
        float dx=target.x-info.data.position.x,dy=target.y-info.data.position.y;
        if(dx*dx+dy*dy<64) return;
        int horizontal=dx<0?2:3,vertical=dy<0?1:0;
        if(Math.abs(dx)>=Math.abs(dy)) {
            if(Math.abs(dx)>=8 && step(values,occupied,info,species,horizontal)) return;
            if(Math.abs(dy)>=8) step(values,occupied,info,species,vertical);
        } else {
            if(Math.abs(dy)>=8 && step(values,occupied,info,species,vertical)) return;
            if(Math.abs(dx)>=8) step(values,occupied,info,species,horizontal);
        }
    }
    private static boolean step(Map<String,LanSession.Value> values,Set<String> occupied,LanCoop.CreatureInfo info,DedicatedSpecies species,int direction) {
        int[] dx={0,0,-16,16},dy={16,-16,0,0};String[] facing={"up","down","left","right"};
        float x=info.data.position.x+dx[direction],y=info.data.position.y+dy[direction];
        String key=tileKey(info.layer,x,y);
        if(!walkable(tile(values,key),species) || occupied.contains(key)) return false;
        occupied.remove(tileKey(info.layer,info.data.position.x,info.data.position.y));occupied.add(key);
        info.data.position.set(x,y);info.direction=facing[direction];return true;
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
