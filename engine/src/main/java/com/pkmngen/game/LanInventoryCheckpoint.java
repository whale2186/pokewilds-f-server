package com.pkmngen.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Identity-preserving private inventory/party checkpoint for a shared edit epoch. */
final class LanInventoryCheckpoint<T> {
    private final Map<String,Integer> items;
    private final List<T> members;
    final T selected;

    LanInventoryCheckpoint(Map<String,Integer> inventory, List<T> party, T selected) {
        this.items = new LinkedHashMap<>(inventory);
        this.members = new ArrayList<>(party);
        this.selected = selected;
    }

    boolean changed(Map<String,Integer> inventory, List<T> party) {
        return !items.equals(inventory) || !members.equals(party);
    }

    void restore(Map<String,Integer> inventory, List<T> party) {
        inventory.clear(); inventory.putAll(items);
        party.clear(); party.addAll(members);
    }
}
