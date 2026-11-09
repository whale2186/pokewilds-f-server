package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Graphics-free LAN authority. Clients simulate their private player; this process owns shared state. */
public final class DedicatedServer {
    static final class Snapshot {
        LanCoop.WorldInfo world;
        Map<String, LanSession.Pose> players = new LinkedHashMap<>();
        Map<String, String> resources = new LinkedHashMap<>();
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1 || args.length > 2 || !args[0].matches("[A-Za-z0-9_-]{1,32}"))
                throw new IllegalArgumentException("Usage: --server <saved-world> [port]");
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 54555;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port must be 1..65535");
            run(args[0], port);
        } catch (Exception error) {
            System.err.println("Dedicated server: " + error.getMessage());
            System.exit(1);
        }
    }

    private static <T> T read(Path path, Class<T> type) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            ZipEntry entry = zip.getEntry("data.json");
            if (entry == null) throw new IOException("Missing data.json in " + path);
            try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                T value = Json.gson.fromJson(reader, type);
                if (value == null) throw new IOException("Empty save: " + path);
                return value;
            }
        }
    }

    private static Snapshot load(String name, Path directory, int port) throws Exception {
        Path snapshot = directory.resolve("dedicated-state.json.zip");
        if (Files.exists(snapshot)) {
            Snapshot restored = read(snapshot, Snapshot.class);
            Path decisions = directory.resolve("coop-receipts.json.zip");
            if (Files.exists(decisions)) restored.resources.putAll(read(decisions, LanCoop.ReceiptStore.class).values);
            return restored;
        }
        if (!Files.exists(directory.resolve("game.json.zip"))) {
            // Never silently replace an incomplete or damaged graphical save.
            try (java.util.stream.Stream<Path> files = Files.list(directory)) {
                if (files.anyMatch(path -> !path.getFileName().toString().equals("dedicated.lock")))
                    throw new IOException("Incomplete world " + name + ": missing game.json.zip; restore the save or choose a new world name");
            }
            // Check availability before generating, while preserving save-corruption diagnostics.
            try (java.net.ServerSocket probe = new java.net.ServerSocket()) {
                probe.bind(new java.net.InetSocketAddress("0.0.0.0", port));
            }
            return DedicatedWorld.generate(name, new Random().nextLong());
        }
        Network.GameSaveData game = read(directory.resolve("game.json.zip"), Network.GameSaveData.class);
        Network.MapSaveData map = read(directory.resolve("map" + game.currMapId + ".json.zip"), Network.MapSaveData.class);
        Snapshot state = new Snapshot();
        LanCoop.WorldInfo world = state.world = new LanCoop.WorldInfo();
        world.dedicated = true; world.worldId = name; world.mapId = game.currMapId;
        world.worldUid = LanIdentity.loadOrCreate(directory.resolve("coop-world-id.txt"));
        world.timeOfDay = game.timeOfDay; world.dayTimer = game.dayTimer;
        world.edges = map.mapTiles.edges; world.routes = map.mapTiles.routes;
        world.interiorIndex = map.mapTiles.interiorTilesIndex;
        world.bottomLeft = new Vector2(); world.topRight = new Vector2();
        for (Network.TileData tile : map.mapTiles.tiles) {
            world.bottomLeft.x = Math.min(world.bottomLeft.x, tile.pos.x);
            world.bottomLeft.y = Math.min(world.bottomLeft.y, tile.pos.y);
            world.topRight.x = Math.max(world.topRight.x, tile.pos.x);
            world.topRight.y = Math.max(world.topRight.y, tile.pos.y);
            addTile(state, tile, -1);
        }
        for (int i = 0; i < map.mapTiles.interiorTiles.size(); i++) {
            Map<Vector2, Network.TileData> layer = map.mapTiles.interiorTiles.get(i);
            if (layer != null) for (Network.TileData tile : layer.values()) addTile(state, tile, i);
        }
        world.spawn = world.edges.get(0).cpy();
        for (Map.Entry<Vector2, Network.PokemonData> entry : map.overworldPokemon.entrySet()) {
            LanCoop.CreatureInfo creature = new LanCoop.CreatureInfo();
            creature.data = entry.getValue();
            Vector2 position = entry.getKey();
            creature.data.position = new Vector2((float)Math.floor(position.x/16)*16, (float)Math.floor(position.y/16)*16);
            creature.layer = creature.data.isInterior ? creature.data.interiorIndex : -1;
            creature.direction = "down";
            state.resources.put("pokemon:" + UUID.randomUUID(), Json.gson.toJson(creature));
        }
        LanCoop.ClockInfo clock = new LanCoop.ClockInfo(); clock.timeOfDay = world.timeOfDay; clock.dayTimer = world.dayTimer;
        state.resources.put("clock", Json.gson.toJson(clock));
        Path receiptsPath = directory.resolve("coop-receipts.json.zip");
        if (Files.exists(receiptsPath)) state.resources.putAll(read(receiptsPath, LanCoop.ReceiptStore.class).values);
        return state;
    }

    static void addTile(Snapshot state, Network.TileData tile, int layer) {
        tile.interiorIndex = layer;
        state.resources.put("tile:" + layer + ":" + (int)tile.pos.x + ":" + (int)tile.pos.y, Json.gson.toJson(tile));
    }

    private static synchronized void save(Path directory, Snapshot state, LanSession session) throws IOException {
        state.players = session.savedPositions();
        state.resources.clear();
        for (Map.Entry<String, LanSession.Value> entry : session.resources().entrySet())
            if (entry.getValue().json != null) state.resources.put(entry.getKey(), entry.getValue().json);
        Path temp = Files.createTempFile(directory, "dedicated-state-", ".tmp");
        try {
            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temp));
                 Writer writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8)) {
                zip.putNextEntry(new ZipEntry("data.json")); Json.gson.toJson(state, writer);
            }
            Files.move(temp, directory.resolve("dedicated-state.json.zip"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { Files.deleteIfExists(temp); }
    }

    private static boolean command(String line, Path directory, Snapshot state, LanSession session) throws IOException {
        String[] parts = line.trim().split("\\s+", 2);
        String argument = parts.length > 1 ? parts[1].trim() : "";
        switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "": return false;
            case "help": System.out.println("Commands: help, list / players, kick <name|id>, time <day|night>, save, stop"); break;
            case "list": case "players":
                for (LanSession.Pose peer : session.peers().values())
                    if (!peer.id.equals(session.id())) System.out.println(peer.name + " " + peer.id);
                System.out.println((session.peers().size() - 1) + " players online"); break;
            case "kick":
                List<LanSession.Pose> matches = new ArrayList<>();
                for (LanSession.Pose peer : session.peers().values())
                    if (!peer.id.equals(session.id()) && (peer.id.equals(argument) || peer.name.equals(argument))) matches.add(peer);
                if (matches.size() != 1) System.out.println("Specify a unique player name or id (see list)");
                else { session.kick(matches.get(0).id); System.out.println("Kicked " + matches.get(0).name); }
                break;
            case "time":
                if (!argument.equals("day") && !argument.equals("night")) { System.out.println("Usage: time <day|night>"); break; }
                LanSession.Value current = session.resource("clock");
                LanCoop.ClockInfo clock = new LanCoop.ClockInfo(); clock.timeOfDay = argument; clock.dayTimer = argument.equals("day") ? 18000 : 5000;
                session.transact(UUID.randomUUID().toString(), Collections.singletonMap("clock", current == null ? 0L : current.revision),
                        Collections.singletonMap("clock", Json.gson.toJson(clock)));
                session.drainTransactions(); System.out.println("Time set to " + argument); break;
            case "save": save(directory, state, session); System.out.println("World saved"); break;
            case "stop": System.out.println("Stopping server"); return true;
            default: System.out.println("Unknown command; type help");
        }
        return false;
    }

    private static void run(String name, int port) throws Exception {
        Path directory = Paths.get(name + ".sav");
        Files.createDirectories(directory);
        // Prevent two authorities from writing the same dedicated world.
        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(directory.resolve("dedicated.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             java.nio.channels.FileLock lock = channel.tryLock()) {
            if (lock == null) throw new IOException("World is already running");
            Snapshot state = load(name, directory, port);
            DedicatedWorld.addBuildingRoutes(state.world);
            LanCoop.validateWorld(state.world);
            final LanSession[] authority = new LanSession[1];
            try (LanSession session = LanSession.host("0.0.0.0", port, "dedicated-server", Json.gson.toJson(state.world), state.resources,
                    (id, actor, key, before, after) -> LanCoop.allowWorldMutation(state.world, authority[0] != null && id.equals(authority[0].id()), actor, key, before, after), state.players)) {
                authority[0] = session;
                LanCoop.installTradePersistence(session, directory);
                save(directory, state, session);
                Thread shutdown = new Thread(() -> {
                    session.close();
                    try { save(directory, state, session); }
                    catch (IOException error) { System.err.println("World save failed: " + error.getMessage()); }
                    session.close();
                }, "dedicated-shutdown");
                Runtime.getRuntime().addShutdownHook(shutdown);
                long lastSave = System.nanoTime(), lastClock = lastSave;
                long tiles = state.resources.keySet().stream().filter(key -> key.startsWith("tile:")).count();
                long creatures = state.resources.keySet().stream().filter(key -> key.startsWith("pokemon:")).count();
                System.out.println("DEDICATED_READY world=" + name + " port=" + session.port() + " tiles=" + tiles + " creatures=" + creatures);
                java.util.concurrent.BlockingQueue<String> commands = new java.util.concurrent.LinkedBlockingQueue<>();
                Thread console = new Thread(() -> {
                    try {
                        BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                        String line;
                        while ((line = input.readLine()) != null) commands.offer(line);
                    } catch (IOException error) { System.err.println("Console input: " + error.getMessage()); }
                }, "dedicated-console");
                console.setDaemon(true); console.start();
                DedicatedSimulation simulation = new DedicatedSimulation(state.world, new Random());
                try {
                    while (session.isConnected()) {
                        Thread.sleep(250);
                        String command;
                        boolean stopping = false;
                        while ((command = commands.poll()) != null) {
                            try { if (command(command, directory, state, session)) { stopping = true; break; } }
                            catch (Exception error) { System.err.println("Command failed: " + error.getMessage()); }
                        }
                        if (stopping) break;
                        LanSession.Value clockValue = session.resource("clock");
                        if (clockValue != null && System.nanoTime() - lastClock >= 1_000_000_000L) {
                            lastClock = System.nanoTime();
                            LanCoop.ClockInfo clock = Json.gson.fromJson(clockValue.json, LanCoop.ClockInfo.class);
                            clock.dayTimer -= 60;
                            if (clock.dayTimer <= 0) {
                                clock.timeOfDay = "day".equals(clock.timeOfDay) ? "night" : "day";
                                clock.dayTimer = "day".equals(clock.timeOfDay) ? 18000 : 5000;
                            }
                            session.transact(UUID.randomUUID().toString(), Collections.singletonMap("clock", clockValue.revision),
                                    Collections.singletonMap("clock", Json.gson.toJson(clock)));
                            session.drainTransactions();
                        }
                        simulation.tick(session,15,false);
                        if (System.nanoTime() - lastSave >= 30_000_000_000L) {
                            save(directory, state, session); lastSave = System.nanoTime();
                        }
                    }
                } finally {
                    session.close();
                    save(directory, state, session);
                    try { Runtime.getRuntime().removeShutdownHook(shutdown); } catch (IllegalStateException stopping) { }
                }
            }
        }
    }
}
