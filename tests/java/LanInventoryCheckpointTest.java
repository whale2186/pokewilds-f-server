package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** Private side effects must roll back with a rejected shared-world transaction. */
public final class LanInventoryCheckpointTest {
    public static void main(String[] args) {
        Object starter = new Object();
        Object shared = new Object();
        ArrayList<Object> party = new ArrayList<>(Arrays.asList(starter));
        Map<String,Integer> items = new LinkedHashMap<>(); items.put("log", 4);
        LanInventoryCheckpoint<Object> before = new LanInventoryCheckpoint<>(items, party, starter);
        party.add(shared); items.put("stone", 1); items.put("log", 1);
        if (!before.changed(items, party)) throw new AssertionError("Missed optimistic private effects");
        before.restore(items, party);
        if (party.size() != 1 || party.get(0) != starter || party.contains(shared))
            throw new AssertionError("Rejected pickup left duplicate in party");
        if (items.size() != 1 || items.get("log") != 4 || items.containsKey("stone"))
            throw new AssertionError("Rejected harvest/build did not restore inventory");
        if (before.changed(items, party) || before.selected != starter)
            throw new AssertionError("Checkpoint did not preserve identity");
        party.remove(starter); items.clear(); before.restore(items, party);
        if (party.get(0) != starter || items.get("log") != 4)
            throw new AssertionError("Rejected drop consumed private property");
        System.out.println("PASS private pickup/harvest/build/drop rollback and identity");
    }
}
