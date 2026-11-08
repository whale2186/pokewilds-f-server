package com.pkmngen.game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Reads the same bundled species data as the graphical game without initializing Pokemon. */
final class DedicatedSpecies {
    private static final Map<String, DedicatedSpecies> CACHE = new HashMap<>();
    private static final Set<String> IMPLEMENTED = new HashSet<>(Arrays.asList(resource("pokemon/attacks_implemented.txt").split("\r?\n")));
    private static final String LEARNSETS = resource("pokemon/evos_attacks.asm") + "\n" + resource("pokemon/credited/evos_attacks.asm");
    private static final Set<String> SWIMMERS = new HashSet<>(Arrays.asList("magikarp", "gyarados", "remoraid", "chinchou", "lanturn", "goldeen", "seaking", "horsea", "seadra", "kingdra", "feebas", "lapras", "carvanha", "sharpedo"));
    final boolean onlySwim;
    final int hp;
    private final String growth;
    final String types;
    final int harvestTimerMax;
    final List<String> harvest = new ArrayList<>();
    final List<String> habitats = new ArrayList<>();
    private final String learnset;

    static DedicatedSpecies get(String name) { return CACHE.computeIfAbsent(name, DedicatedSpecies::new); }
    private DedicatedSpecies(String name) {
        if (name.startsWith("unown_")) name = "unown";
        if (name.equals("combee_female")) name = "combee";
        onlySwim = SWIMMERS.contains(name);
        String stats = resource("pokemon/base_stats/" + name + ".asm");
        Matcher numbers = Pattern.compile("db\\s+(\\d+)\\s*,\\s*\\d+\\s*,\\s*\\d+").matcher(stats);
        if (!numbers.find()) throw new IllegalArgumentException("Missing stats for " + name);
        hp = Integer.parseInt(numbers.group(1));
        Matcher growthMatch = Pattern.compile("GROWTH_([A-Z_]+)").matcher(stats);
        growth = growthMatch.find() ? growthMatch.group(1) : "MEDIUM_FAST";
        Matcher type = Pattern.compile("db\\s+([A-Z]+),\\s*([A-Z]+)").matcher(stats);
        if (!type.find()) throw new IllegalArgumentException("Missing types for " + name);
        types = type.group(1) + " " + type.group(2);
        CreatureHabitat rules = new CreatureHabitat(name, Arrays.asList(type.group(1), type.group(2)),
                Arrays.asList("water stone", "fire stone", "leaf stone", "thunderstone", "moon stone", "sun stone", "ice stone", "dawn stone", "dusk stone", "shiny stone", "sweet apple"));
        harvest.addAll(rules.harvestables); habitats.addAll(rules.habitats); harvestTimerMax = rules.harvestTimerMax;
        Matcher moves = Pattern.compile("(?is)(?:^|\\n)" + Pattern.quote(name) + "EvosAttacks:(.*?)(?=\\n[A-Za-z0-9]+EvosAttacks:|\\z)").matcher(LEARNSETS);
        if (!moves.find()) throw new IllegalArgumentException("Missing learnset for " + name);
        learnset = moves.group(1);
    }
    int experience(int level) {
        int cube = level * level * level;
        if (growth.contains("FAST")) return 4 * cube / 5; // Matches Pokemon.calcExpForLevel, including MEDIUM_FAST.
        if (growth.contains("MEDIUM_SLOW")) return 6 * cube / 5 - 15 * level * level + 100 * level - 140;
        if (growth.contains("SLOW")) return 5 * cube / 4;
        if (growth.contains("FLUCTUATING")) return (int)(cube * (level < 15 ? (Math.floor((level + 1) / 3.0) + 24) / 50.0 : level < 36 ? (level + 14) / 50.0 : (Math.floor(level / 2.0) + 32) / 50.0));
        if (growth.contains("ERRATIC")) return (int)(cube * (level < 50 ? (100 - level) / 50.0 : level < 68 ? (150 - level) / 100.0 : level < 98 ? Math.floor((1911 - 10 * level) / 3.0) / 500.0 : (160 - level) / 100.0));
        return cube;
    }
    String[] attacks(int level) {
        String[] attacks = new String[4]; int index = 0;
        Matcher move = Pattern.compile("db\\s+(\\d+),\\s*([A-Z_0-9]+)").matcher(learnset);
        while (move.find()) {
            String attack = move.group(2).toLowerCase(Locale.ROOT).replace('_', ' ');
            if (Integer.parseInt(move.group(1)) <= level && IMPLEMENTED.contains(attack)) attacks[index++ % 4] = attack;
        }
        return attacks;
    }
    boolean habitat(Network.TileData tile) {
        for (String group : habitats) if (!matches(group, tile)) return false;
        return true;
    }
    boolean matches(String group, Network.TileData tile) {
        for (String pattern : group.split("\\|")) if (tile.tileName.contains(pattern) || tile.tileNameUpper.contains(pattern)) return true;
        return false;
    }
    private static InputStream openResource(String path) {
        ClassLoader loader = DedicatedSpecies.class.getClassLoader();
        InputStream input = loader.getResourceAsStream(path);
        if (input == null && path.startsWith("pokemon/base_stats/")) input = loader.getResourceAsStream(path.replace("pokemon/base_stats/", "pokemon/credited/base_stats/"));
        return input;
    }
    private static String resource(String path) {
        try (InputStream input = openResource(path)) {
            if (input == null) throw new IOException("Missing " + path);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(); byte[] buffer = new byte[4096]; int count;
            while ((count = input.read(buffer)) >= 0) bytes.write(buffer, 0, count);
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException error) { throw new IllegalStateException(error); }
    }
}
