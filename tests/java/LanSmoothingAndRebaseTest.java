package com.pkmngen.game;

import com.pkmngen.game.multiplayer.PoseBuffer;
import com.pkmngen.game.util.Json;

public final class LanSmoothingAndRebaseTest {
    public static void main(String[] args) {
        PoseBuffer buffer = new PoseBuffer();
        long epoch = 1_000_000_000L;
        buffer.add(0, 0, epoch, false);
        buffer.add(4, 0, epoch + 50_000_000, false);
        buffer.add(8, 0, epoch + 100_000_000, false);
        buffer.add(12, 0, epoch + 150_000_000, false);
        float previous = -1;
        for (int i = 0; i <= 150; i++) {
            float x = buffer.sample(epoch + 100_000_000 + i*1_000_000L, true);
            if (Math.abs(x - i*.08f) > .001f || x < previous) throw new AssertionError("Uneven glide " + x);
            previous = x;
        }
        buffer.add(12, 0, epoch + 200_000_000, false);
        for (int i = 150; i <= 400; i++)
            if (Math.abs(buffer.sample(epoch + 100_000_000 + i*1_000_000L, true) - 12) > .001f) throw new AssertionError("Stop overshoot");
        buffer.add(500, 600, epoch + 500_000_000, true);
        if (buffer.sample(epoch + 500_000_000, true) != 500) throw new AssertionError("Teleport did not reset buffer");
        String merged = LanCoop.rebase("{\"data\":{\"timer\":1,\"hp\":9}}", "{\"data\":{\"timer\":1,\"hp\":5}}", "{\"data\":{\"timer\":2,\"hp\":9}}");
        if (!Json.gson.fromJson(merged, com.google.gson.JsonObject.class).getAsJsonObject("data").get("timer").getAsString().equals("2")) throw new AssertionError("Timer lost");
        if (!merged.contains("\"hp\":5")) throw new AssertionError("Edit lost");
        if (LanCoop.rebase("{\"hp\":9}", "{\"hp\":5}", "{\"hp\":3}") != null) throw new AssertionError("Overlapping edit overwritten");
        if (LanCoop.rebase("{}", "{}", "{\"remote\":1}") == null) throw new AssertionError("Remote addition lost");
        String normalized = LanCoop.rebase("{\"hp\":9,\"timer\":0,\"defaults\":{}}", "{\"hp\":5,\"timer\":0,\"defaults\":{}}", "{\"hp\":9,\"timer\":2}");
        if (!"{\"hp\":5,\"timer\":2}".equals(normalized)) throw new AssertionError("Replica defaults leaked into edit: " + normalized);
        String noop = LanCoop.rebase("{\"hp\":9,\"defaults\":{}}", "{\"hp\":9,\"defaults\":{}}", "{\"hp\":9}");
        if (!"{\"hp\":9}".equals(noop)) throw new AssertionError("Replica normalization generated a change");
        if (!LanCoop.nearbyEdit("tile:-1:16:0", "{}", "{}", -1, 0, 0)
                || LanCoop.nearbyEdit("tile:-1:160:0", "{}", "{}", -1, 0, 0)
                || LanCoop.nearbyEdit("tile:0:16:0", "{}", "{}", -1, 0, 0)) throw new AssertionError("Guest audit range/layer invalid");
        java.util.Map<com.badlogic.gdx.math.Vector2,Object> index = new java.util.HashMap<>();
        Object first = new Object(), second = new Object();
        com.badlogic.gdx.math.Vector2 position = new com.badlogic.gdx.math.Vector2(16,16);
        LanCoop.indexValue(index, position, first); LanCoop.indexValue(index, position, second);
        if (index.size()!=2 || !index.containsValue(first) || index.get(position)!=second)
            throw new AssertionError("Overlapping creature was lost from snapshot");
        LanCoop.indexValue(index, new com.badlogic.gdx.math.Vector2(32,16), second);
        LanCoop.indexValue(index, position, first);
        if (index.size()!=2 || index.get(position)!=first) throw new AssertionError("Overlap did not settle without duplication");
        String oldCreature = "{\"data\":{\"name\":\"pikachu\",\"harvestTimer\":1},\"direction\":\"down\"}";
        String timedCreature = "{\"data\":{\"name\":\"pikachu\",\"harvestTimer\":2},\"direction\":\"up\"}";
        if (!LanCoop.canRebaseRemoval("pokemon:test",oldCreature,null,timedCreature)
                || LanCoop.canRebaseRemoval("pokemon:test",oldCreature,null,null)
                || LanCoop.canRebaseRemoval("pokemon:test",oldCreature,null,timedCreature.substring(0,timedCreature.length()-1)+",\"encounterOwner\":\"other\"}"))
            throw new AssertionError("Timer-only pickup retry or competing reservation incorrect");
        CreatureMotion creature = new CreatureMotion(0, 0);
        creature.target(16, 0, epoch);
        if (creature.x(epoch) != 0 || creature.x(epoch + 125_000_000L) != 8
                || creature.x(epoch + 300_000_000L) != 16) throw new AssertionError("Creature did not glide/stop");
        creature.target(32, 0, epoch + 125_000_000L);
        if (creature.x(epoch + 125_000_000L) != 8) throw new AssertionError("Retarget jumped");
        creature.target(500, 600, epoch + 150_000_000L);
        if (creature.x(epoch + 150_000_000L) != 500) throw new AssertionError("Creature teleport not snapped");
        System.out.println("PASS cubic glide, stop, teleport, disjoint rebase and genuine contention");
    }
}
