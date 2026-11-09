package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Server;
import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.multiplayer.LanSession.Pose;
import com.pkmngen.game.multiplayer.LanSession.Value;
import com.pkmngen.game.util.Json;
import com.pkmngen.game.util.Save;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Opt-in trusted-LAN co-op. All graphics/world access stays on the render thread. */
public final class LanCoop implements AutoCloseable {
    final Game game;
    private final LanInteractions interactions;
    final LanHud hud;
    final LanSpectator spectator;
    final LanOptions options;
    private volatile LanSession session;
    private volatile String failure;
    private volatile boolean closed;
    private Thread worker;
    private boolean initialized;
    private long lightingRevision = -1;
    private WorldInfo world;
    private final Map<String, Route> routes = new HashMap<>();
    private final IdentityHashMap<Route, String> routeIds = new IdentityHashMap<>();
    private final IdentityHashMap<Pokemon, String> creatureIds = new IdentityHashMap<>();
    private final Map<String, Pokemon> creatures = new HashMap<>();
    private final IdentityHashMap<Pokemon, CreatureMotion> creatureMotion = new IdentityHashMap<>();
    private final Map<String, String> encounters = new HashMap<>();
    private final Map<String, Value> observed = new HashMap<>();
    private final Map<String, String> baseline = new HashMap<>();
    private final Map<String, String> replicaBaseline = new HashMap<>();
    private final Map<String, String> pending = new HashMap<>();
    private final LanTileCache tileCache = new LanTileCache();
    private long lastScan;
    private final java.util.List<String> terrainAuditKeys = new java.util.ArrayList<>();
    private int terrainAuditCursor;
    private int creatureAuditCursor;
    private long lastPose;
    private String pendingTransaction;
    private boolean saveRequested;
    private boolean exitRequested, exitSaving;
    private java.util.concurrent.Future<?> exitFlush;
    void requestExit() { exitRequested = true; }
    private void finishExit() {
        if (!exitRequested || pendingTransaction != null || saveRequested || failure != null) return;
        if (!exitSaving) {
            exitSaving = true;
            game.saveGame();
            if (pendingTransaction != null || saveRequested || failure != null) return;
        }
        if (exitFlush == null) exitFlush = disk.submit(() -> {});
        if (exitFlush.isDone() && failure == null) game.requestMainMenu();
    }
    private final java.util.concurrent.ExecutorService disk = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "coop-save"); t.setDaemon(true); return t;
    });
    private java.util.concurrent.Future<?> journal;
    private Map<String,Long> journalExpected;
    private boolean transactionSent;
    private long transactionStarted;
    private int editRetries;
    private LanInventoryCheckpoint<Pokemon> privateCheckpoint;
    private boolean privateEffects;
    boolean sharedEditPending() { return pendingTransaction != null; }
    boolean privateEditPending() { return pendingTransaction != null && privateEffects; }
    private void checkpointPrivate() {
        privateCheckpoint = new LanInventoryCheckpoint<>(game.player.getItemsDict(), game.player.pokemon, game.player.currPokemon);
    }
    private void restorePrivate() {
        if (privateCheckpoint == null) return;
        privateCheckpoint.restore(game.player.getItemsDict(), game.player.pokemon);
        game.player.currPokemon = privateCheckpoint.selected;
        if (game.player.hmPokemon != null && !game.player.pokemon.contains(game.player.hmPokemon)) game.player.hmPokemon = null;
    }
    private String notice;
    private long noticeUntil;
    private long renderedAvatars;
    private long settledEdits;
    private boolean lastEditAccepted;
    private float oldX;
    private float oldY;
    private String status = "Connecting";
    private String playerIdentity;
    private final Map<String, Player> owners = new HashMap<>();
    private final Map<String, LanVisual> remoteVisuals = new HashMap<>();
    private final Map<String, RemotePose> remotePoses = new HashMap<>();
    private static final float SNAP_DISTANCE_SQUARED = 48 * 48;
    private static final class RemotePose {
        float x, y, targetX, targetY, velocityX, velocityY;
        Pose latest;
        long receivedAt;
        float distance;
        final com.pkmngen.game.multiplayer.PoseBuffer buffer = new com.pkmngen.game.multiplayer.PoseBuffer();
        String character;
        int color, skinColor;
        String mapId, layerKey;
        RemotePose(Pose pose, long now) {
            x = targetX = pose.x; y = targetY = pose.y; receivedAt = now;
            buffer.add(pose.x, pose.y, now, false);
            mapId = pose.mapId; layerKey = pose.mapId + ":" + pose.layer;
        }
    }

    static final class WorldInfo {
        String worldId;
        boolean dedicated;
        String worldUid;
        String mapId;
        String timeOfDay;
        int dayTimer;
        Vector2 bottomLeft;
        Vector2 topRight;
        Vector2 spawn;
        int interiorIndex;
        ArrayList<Vector2> edges;
        Map<String, Network.RouteData> routes = new HashMap<>();
    }
    static final class CreatureInfo {
        Network.PokemonData data;
        int layer;
        String direction;
        String harvestable;
        String encounterOwner;
    }
    static final class ClockInfo {
        String timeOfDay;
        int dayTimer;
    }
    static final class ReceiptStore { Map<String,String> values = new HashMap<>(); Map<String,Pose> players = new HashMap<>(); }
    static final class PendingEdit {
        String receiptKey;
        String before;
        String after;
        boolean trade;
    }

    LanCoop(Game game, LanOptions options) { this.game = game; this.options = options; interactions = new LanInteractions(game, this); hud = new LanHud(game, this); spectator = new LanSpectator(game,this); }
    boolean interactWithPlayer() { return ready() && pendingTransaction == null && !hud.blocksInput() && !spectator.blocksInput() && interactions.openNearby(); }
    boolean interactionBusy() { return interactions.busy(); }
    LanBattleState interactionBattle() { return interactions.battleView(); }
    boolean peerBattling(String id) {
        LanVisual visual=remoteVisuals.get(id);
        return visual!=null && visual.battle!=null;
    }
    boolean editPending() { return pendingTransaction != null; }
    String editNotice() { return notice != null && System.nanoTime() < noticeUntil ? notice : null; }
    public static void validateArgs(String[] args) { LanOptions.parse(args); }
    boolean isDedicated() { return isGuest() && world != null && world.dedicated; }
    boolean isGuest() { return "join".equals(options.mode); }
    boolean ready() { return initialized && session != null && session.isConnected() && failure == null; }
    String problem() { return failure; }
    LanSession session() { return session; }
    long renderedAvatars() { return renderedAvatars; }
    long settledEdits() { return settledEdits; }
    boolean lastEditAccepted() { return lastEditAccepted; }
    String worldCreatureId(Pokemon pokemon) { return creatureIds.get(pokemon); }
    void recordAvatarDraw(Player player) {
        if (game.cam.frustum.pointInFrustum(player.position.x + 8, player.position.y + 12, 0)) renderedAvatars++;
    }
    Vector2 renderPosition(Player player) {
        RemotePose pose = remotePoses.get(player.network.id);
        return pose == null ? player.position : new Vector2(pose.x, pose.y);
    }
    Vector2 creatureRenderPosition(Pokemon pokemon) {
        CreatureMotion motion = creatureMotion.get(pokemon);
        if (motion == null) return pokemon.position;
        long now = System.nanoTime();
        float x = motion.x(now), y = motion.y(now);
        boolean moving = Vector2.dst2(x, y, pokemon.position.x, pokemon.position.y) > .01f;
        pokemon.currOwSprite = moving && (now / 100_000_000L) % 2 == 0
                ? pokemon.movingSprites.get(pokemon.dirFacing) : pokemon.standingSprites.get(pokemon.dirFacing);
        return new Vector2(x, y);
    }
    Vector2 interactionOffset(Player player) {
        LanVisual visual = remoteVisuals.get(player.network.id);
        return visual == null ? new Vector2() : visual.playerOffset();
    }
    void drawFollower(Player player) {
        LanVisual visual = remoteVisuals.get(player.network.id);
        if (visual != null) visual.draw(game, renderPosition(player).sub(player.position));
    }
    void drawInteraction(Player player) {
        LanVisual visual = remoteVisuals.get(player.network.id);
        if (visual != null) visual.drawEmote(game, renderPosition(player));
    }
    boolean isWorldCreature(Pokemon pokemon) { return game.map != null && game.map.pokemon.containsValue(pokemon); }
    void identifyOwner(Pokemon pokemon) {
        if (pokemon.previousOwner == game.player) {
            pokemon.lanOwnerId = playerIdentity; pokemon.lanOwnerName = options.name;
        } else if (pokemon.lanOwnerId != null) {
            pokemon.previousOwner = resolveOwner(pokemon.lanOwnerId, pokemon.lanOwnerName);
        } else if (pokemon.previousOwner != null) {
            pokemon.lanOwnerId = "legacy-" + pokemon.previousOwner.name;
            pokemon.lanOwnerName = pokemon.previousOwner.name;
        }
    }
    Player resolveOwner(String identity, String name) {
        if (identity.equals(playerIdentity)) return game.player;
        Player owner = owners.get(identity);
        if (owner == null) {
            owner = new Player(); owner.name = name == null ? "another player" : name;
            owners.put(identity, owner);
        }
        return owner;
    }
    Vector2 sharedSpawn() { return world.spawn.cpy(); }

    void create() {
        try { playerIdentity = LanIdentity.player(java.nio.file.Paths.get("."), options.name); }
        catch (IOException error) { failure = error.getMessage(); return; }
        game.player.name = options.name;
        game.rememberPlayerProfile(options.name);
        if (isGuest()) {
            stopSetup();
            game.playerCanMove = false;
            worker = new Thread(() -> {
                try {
                    LanSession connected = LanSession.join(options.address, options.port, options.name, playerIdentity);
                    if (closed) connected.close(); else session = connected;
                } catch (Exception error) { failure = "Join failed: " + error.getMessage(); }
            }, "coop-join");
            worker.setDaemon(true);
            worker.start();
        } else if (options.world != null) {
            stopSetup();
            game.map = new PkmnMap(options.world);
            game.server = new Server();
            Network.register(game.server);
            if (!options.createWorld) {
                if (!Gdx.files.local(options.world + ".sav/game.json.zip").exists()) {
                    failure = "Host save does not exist: " + options.world;
                    return;
                }
                Player setupPlayer = game.player;
                game.start();
                game.map.loadFromFile(game);
                if (game.player.currFieldMove.isEmpty()) {
                    game.player.character = setupPlayer.character;
                    game.player.skinColor = new Color(setupPlayer.skinColor);
                    game.player.setColor(new Color(setupPlayer.color));
                    game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
                }
                game.player.name = options.name;
            } else {
                if (Gdx.files.local(options.world + ".sav").exists()) {
                    failure = "Refusing to overwrite existing world: " + options.world;
                    return;
                }
                status = "Generating host island";
                worker = new Thread(() -> {
                    try {
                        final GenIsland1 generated = new GenIsland1(game, new Vector2(), options.size);
                        Gdx.app.postRunnable(() -> {
                            if (closed) return;
                            try {
                                game.start();
                                game.insertAction(generated);
                                generated.step(game);
                                if (game.map.bottomLeft != null && !game.map.edges.isEmpty()) makeMinimap();
                                game.map.refreshCache = true;
                            } catch (Exception error) { failure = "Generation failed: " + error.getMessage(); error.printStackTrace(); }
                        });
                    } catch (Exception error) { failure = "Generation failed: " + error.getMessage(); error.printStackTrace(); }
                }, "coop-world-generation");
                worker.setDaemon(true);
                worker.start();
            }
        }
    }

    private void stopSetup() {
        game.actionStack.removeIf(action -> action instanceof DrawSetupMenu);
        Gdx.input.setInputProcessor(null);
        if (game.currMusic != null) { game.currMusic.stop(); game.currMusic.dispose(); game.currMusic = null; }
    }

    void beforeFrame() {
        if (closed) return;
        spectator.update();
        hud.update();
        if (failure != null) { game.playerCanMove = false; return; }
        if (isGuest() && session != null && !initialized) {
            try { installSnapshot(); }
            catch (Exception error) { failure = "World snapshot failed: " + error.getMessage(); error.printStackTrace(); session.close(); }
        }
        if (initialized && session != null) {
            finishTransaction(); finishExit();
            if (isGuest()) {
                Value clock = session.resource("clock");
                if (clock != null && clock.json != null && clock.revision != lightingRevision) {
                    installResource("clock", clock.json); lightingRevision = clock.revision;
                }
            }
        }
        if (initialized && session != null && !session.isConnected()) {
            failure = "Disconnected: " + session.failure();
            game.playerCanMove = false;
            game.players.clear();
            System.err.println("COOP_DISCONNECTED " + failure);
        }
    }

    void afterFrame() {
        if (closed || failure != null) return;
        if (!initialized && !isGuest() && game.map != null && game.map.minimap == null && game.map.bottomLeft != null && !game.map.edges.isEmpty()) makeMinimap();
        if (!initialized && !isGuest() && game.map != null && game.map.minimap != null
                && game.map.bottomLeft != null && game.map.topRight != null && !game.map.edges.isEmpty()) {
            try { startHost(); }
            catch (Exception error) { failure = "Host failed: " + error.getMessage(); error.printStackTrace(); }
        }
        if (!ready()) return;
        long now = System.nanoTime();
        if (now - lastPose > 50_000_000L) {
            int layer = game.map.tiles == game.map.overworldTiles ? -1 : game.map.interiorTilesIndex;
            boolean moving = game.player.position.x != oldX || game.player.position.y != oldY;
            session.pose(new Pose(session.id(), options.name, game.player.character, game.player.dirFacing,
                    game.map.currMapId, layer, game.player.position.x, game.player.position.y, moving,
                    game.actionStack.stream().anyMatch(action -> action instanceof PlayerRunning),
                    Color.rgba8888(game.player.color), Color.rgba8888(game.player.skinColor), LanVisual.capture(game)));
            oldX = game.player.position.x; oldY = game.player.position.y; lastPose = now;
        }
        if (!isGuest()) {
            Set<String> connected = session.peers().keySet();
            encounters.entrySet().removeIf(entry -> !connected.contains(entry.getValue()));
        }
        updateAvatars();
        interactions.update();
        // Field actions may update terrain before awarding their items in later frames.
        // Publish/reconcile only after the action releases movement, so both effects
        // belong to one revision-checked transaction and one inventory checkpoint.
        if (game.playerCanMove && now - lastScan > 500_000_000L) {
            scanChanges();
            if (pendingTransaction == null) reconcile();
            lastScan = now;
        }
    }

    void drawStatus() {
        if (failure != null) game.font.draw(game.uiBatch, "LAN: " + failure, 1, 142);
        else if (!ready()) game.font.draw(game.uiBatch, "LAN: " + status, 1, 142);
        else {
            if (spectator.watching()) spectator.draw(); else hud.draw();
        }
    }

    private void makeMinimap() {
        int width = Math.max(1, game.map.width() / 8);
        int height = Math.max(1, game.map.height() / 8);
        game.map.minimap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        game.map.minimap.setColor(Color.BLACK);
        game.map.minimap.fill();
    }

    private void startHost() throws IOException {
        game.players.clear();
        world = new WorldInfo();
        world.worldId = game.map.id; world.mapId = game.map.currMapId;
        world.worldUid = LanIdentity.loadOrCreate(java.nio.file.Paths.get(game.map.id + ".sav", "coop-world-id.txt"));
        world.timeOfDay = game.map.timeOfDay; world.dayTimer = CycleDayNight.dayTimer;
        world.bottomLeft = game.map.bottomLeft.cpy(); world.topRight = game.map.topRight.cpy();
        world.spawn = chooseSpawn(); world.edges = new ArrayList<>(game.map.edges);
        world.interiorIndex = game.map.interiorTilesIndex;
        registerRoutes(game.map.overworldTiles);
        for (Map<Vector2, Tile> layer : game.map.interiorTiles) if (layer != null) registerRoutes(layer);
        if (game.map.currRoute != null) routeId(game.map.currRoute);
        DedicatedWorld.addBuildingRoutes(world);
        for (Map.Entry<String, Network.RouteData> entry : world.routes.entrySet()) if (!routes.containsKey(entry.getKey())) {
            Route route = new Route(entry.getValue()); routes.put(entry.getKey(), route); routeIds.put(route, entry.getKey());
        }
        Map<String, String> initial = capture();
        ReceiptStore receipts;
        try {
            receipts = Save.readDataIfPresent(game.map.id + ".sav/coop-receipts", ReceiptStore.class);
            if (receipts != null) initial.putAll(receipts.values);
        } catch (Save.ReadFailure invalid) { throw new IOException("Cannot read LAN transaction receipts", invalid); }
        PendingEdit interruptedTrade;
        try { interruptedTrade = Save.readDataIfPresent(pendingSavePath(), PendingEdit.class); }
        catch (Save.ReadFailure error) { throw new IOException("Cannot recover trade journal", error); }
        if (interruptedTrade != null && interruptedTrade.trade) {
            boolean committedTrade = initial.containsKey(interruptedTrade.receiptKey);
            Player recovered = new Player(Json.gson.fromJson(committedTrade ? interruptedTrade.after : interruptedTrade.before, Network.PlayerData.class));
            game.player.pokemon.clear(); game.player.pokemon.addAll(recovered.pokemon);
            game.player.getItemsDict().clear(); game.player.getItemsDict().putAll(recovered.getItemsDict());
            game.player.currPokemon = recovered.currPokemon;
            discardTrade();
        }
        validateWorld(world);
        session = LanSession.host(options.address, options.port, options.name, Json.gson.toJson(world), initial, this::allowMutation, receipts == null ? null : receipts.players);
        game.player.network.id = session.id();
        installTradePersistence(session, java.nio.file.Paths.get(game.map.id + ".sav"));
        observed.putAll(session.resources()); baseline.putAll(initial);
        initialized = true; game.playerCanMove = true; checkpointPrivate();
        System.out.println("COOP_READY host port=" + session.port() + " tiles=" + game.map.overworldTiles.size());
    }

    private Vector2 chooseSpawn() {
        Vector2 best = game.player.position.cpy();
        float distance = Float.MAX_VALUE;
        for (Tile tile : game.map.overworldTiles.values()) {
            if (tile.isSolid || tile.isWater || tile.isLedge || game.map.pokemon.containsKey(tile.position)) continue;
            boolean neighbor = false;
            for (Vector2 delta : new Vector2[]{new Vector2(16,0),new Vector2(-16,0),new Vector2(0,16),new Vector2(0,-16)}) {
                Tile next = game.map.overworldTiles.get(tile.position.cpy().add(delta));
                if (next != null && !next.isSolid && !next.isWater && !next.isLedge && !game.map.pokemon.containsKey(next.position)) neighbor = true;
            }
            float d = tile.position.dst2(game.player.position);
            if (neighbor && d < distance) { best = tile.position.cpy(); distance = d; }
        }
        return best;
    }

    private Vector2 guestSpawn() {
        ArrayList<Vector2> candidates = new ArrayList<>();
        for (Vector2 offset : new Vector2[]{new Vector2(16,0), new Vector2(-16,0), new Vector2(0,16), new Vector2(0,-16)}) {
            Vector2 position = world.spawn.cpy().add(offset);
            Tile tile = game.map.overworldTiles.get(position);
            if (tile != null && !tile.isSolid && !tile.isWater && !tile.isLedge && !game.map.pokemon.containsKey(position)) candidates.add(position);
        }
        return candidates.isEmpty() ? world.spawn.cpy() : candidates.get(Math.floorMod(session.id().hashCode(), candidates.size()));
    }
    private String routeId(Route route) {
        if (route == null) return null;
        String id = routeIds.get(route);
        if (id == null) {
            id = "route" + routeIds.size(); routeIds.put(route, id); routes.put(id, route);
            world.routes.put(id, new Network.RouteData(route));
        }
        return id;
    }
    private void registerRoutes(Map<Vector2, Tile> layer) { for (Tile tile : layer.values()) routeId(tile.routeBelongsTo); }
    private String tileKey(int layer, Vector2 position) { return "tile:" + layer + ":" + (int)position.x + ":" + (int)position.y; }

    private Map<String, String> capture() { return capture(true); }

    private Map<String, String> capture(boolean full) {
        if (isGuest()) for (Pokemon pokemon : new ArrayList<>(game.map.pokemon.values()))
            if (!game.map.pokemon.containsKey(pokemon.position)) indexValue(game.map.pokemon, pokemon.position, pokemon);
        Map<String, String> state = new LinkedHashMap<>();
        if (full) {
            captureTiles(state, game.map.overworldTiles, -1);
            for (int i = 0; i < game.map.interiorTiles.size(); i++) {
                Map<Vector2, Tile> layer = game.map.interiorTiles.get(i);
                if (layer != null) captureTiles(state, layer, i);
            }
        } else {
            // Preserve unvisited terrain. Audit a bounded slice for remote timer changes,
            // and inspect the interaction zone immediately so edits stay transactional.
            if (terrainAuditCursor >= terrainAuditKeys.size()) {
                terrainAuditKeys.clear();
                for (String key : baseline.keySet()) if (key.startsWith("tile:")) terrainAuditKeys.add(key);
                terrainAuditCursor = 0;
            }
            for (int count = 0; count < 256 && terrainAuditCursor < terrainAuditKeys.size(); count++) {
                String key = terrainAuditKeys.get(terrainAuditCursor++);
                String[] parts = key.split(":");
                int index = Integer.parseInt(parts[1]);
                Map<Vector2, Tile> layer = index == -1 ? game.map.overworldTiles
                        : index < game.map.interiorTiles.size() ? game.map.interiorTiles.get(index) : null;
                Tile tile = layer == null ? null : layer.get(new Vector2(Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
                if (tile == null) state.put(key, null); else captureTile(state, tile, index);
            }
            int index = game.map.tiles == game.map.overworldTiles ? -1 : game.map.interiorTilesIndex;
            Vector2 position = new Vector2();
            int x = (int)Math.floor(game.player.position.x / 16) * 16;
            int y = (int)Math.floor(game.player.position.y / 16) * 16;
            for (int dx = -64; dx <= 64; dx += 16) for (int dy = -64; dy <= 64; dy += 16) {
                position.set(x + dx, y + dy);
                Tile tile = game.map.tiles.get(position);
                if (tile == null) state.put(tileKey(index, position), null); else captureTile(state, tile, index);
            }
        }
        int creatureIndex = 0;
        int creatureCount = game.map.pokemon.size();
        for (Pokemon pokemon : game.map.pokemon.values()) {
            int auditOffset = creatureCount == 0 ? 0 : (creatureIndex++ - creatureAuditCursor + creatureCount) % creatureCount;
            if (game.player.pokemon.contains(pokemon)) continue; // DROP publishes only after party removal completes.
            String id = creatureIds.get(pokemon);
            if (id == null) { id = "pokemon:" + UUID.randomUUID(); creatureIds.put(pokemon, id); creatures.put(id, pokemon); }
            int creatureLayer = pokemon.mapTiles == game.map.overworldTiles || pokemon.mapTiles == null ? -1 : pokemon.interiorIndex;
            if (!full && auditOffset >= 32 && baseline.containsKey(id)
                    && !inLoadingZone(creatureLayer, pokemon.position.x, pokemon.position.y)) {
                state.put(id, isGuest() ? replicaBaseline.getOrDefault(id, baseline.get(id)) : baseline.get(id)); continue;
            }
            CreatureInfo info = new CreatureInfo(); info.data = new Network.PokemonData(pokemon); info.direction = pokemon.dirFacing;
            info.harvestable = pokemon.hasItem;
            info.encounterOwner = encounters.get(id);
            info.layer = pokemon.mapTiles == game.map.overworldTiles || pokemon.mapTiles == null ? -1 : pokemon.interiorIndex;
            state.put(id, Json.gson.toJson(info));
        }
        if (!full && creatureCount > 0) creatureAuditCursor = (creatureAuditCursor + 32) % creatureCount;
        if (!isGuest()) {
            ClockInfo clock = new ClockInfo(); clock.timeOfDay = game.map.timeOfDay; clock.dayTimer = CycleDayNight.dayTimer / 120 * 120;
            state.put("clock", Json.gson.toJson(clock));
        } else if (baseline.containsKey("clock")) state.put("clock", baseline.get("clock"));
        if (full) for (Map.Entry<String,String> entry : baseline.entrySet())
            if ((entry.getKey().startsWith("receipt:") || entry.getKey().startsWith("trade:"))) state.put(entry.getKey(), entry.getValue());
        if (full) tileCache.retain(state.keySet());
        return state;
    }
    private boolean inLoadingZone(int layer, float x, float y) {
        int localLayer = game.map.tiles == game.map.overworldTiles ? -1 : game.map.interiorTilesIndex;
        if (layer == localLayer && Math.abs(x - game.player.position.x) <= 384 && Math.abs(y - game.player.position.y) <= 384) return true;
        if (!isGuest()) for (Pose peer : session.peers().values())
            if (world.mapId.equals(peer.mapId) && layer == peer.layer && Math.abs(x - peer.x) <= 384 && Math.abs(y - peer.y) <= 384) return true;
        return false;
    }

    private void captureTiles(Map<String,String> state, Map<Vector2,Tile> layer, int index) {
        for (Tile tile : layer.values()) captureTile(state, tile, index);
    }
    private void captureTile(Map<String,String> state, Tile tile, int index) {
            String key = tileKey(index, tile.position);
            String cached = tileCache.unchanged(key, tile, index, routeIds.get(tile.routeBelongsTo));
            if (cached != null) { state.put(key, cached); return; }
            Network.TileData data = new Network.TileData(tile, index);
            data.routeBelongsTo = routeIds.get(tile.routeBelongsTo);
            if (data.routeBelongsTo == null && tile.routeBelongsTo != null) {
                for (Map.Entry<String,Route> route : routes.entrySet()) {
                    if (route.getValue().name.equals(tile.routeBelongsTo.name) && route.getValue().level == tile.routeBelongsTo.level) {
                        data.routeBelongsTo = route.getKey(); routeIds.put(tile.routeBelongsTo, route.getKey()); break;
                    }
                }
                if (data.routeBelongsTo == null) data.routeBelongsTo = "unsupported-route";
            }
            state.put(key, tileCache.json(key, data));
    }

    private void installSnapshot() throws Exception {
        world = Json.gson.fromJson(session.bootstrap(), WorldInfo.class);
        validateWorld(world);
        game.map = new PkmnMap("coop-session");
        game.map.currMapId = world.mapId; game.map.bottomLeft = world.bottomLeft; game.map.topRight = world.topRight;
        game.map.edges = world.edges; game.map.timeOfDay = world.timeOfDay; CycleDayNight.dayTimer = world.dayTimer;
        game.map.interiorTilesIndex = world.interiorIndex;
        for (Map.Entry<String, Network.RouteData> entry : world.routes.entrySet()) {
            Route route = new Route(entry.getValue()); routes.put(entry.getKey(), route); routeIds.put(route, entry.getKey());
        }
        Map<String, Value> snapshot = session.resources();
        // Install terrain before entities, so Pokemon constructors can resolve their world layer.
        for (Map.Entry<String, Value> entry : snapshot.entrySet()) if (entry.getKey().startsWith("tile:")) apply(entry.getKey(), entry.getValue().json);
        game.map.tiles = game.map.overworldTiles;
        Player setupPlayer = game.player;
        game.player = new Player(); game.player.name = options.name;
        String playerSave = playerSavePath();
        Network.PlayerData previous = Save.readDataIfPresent(playerSave, Network.PlayerData.class);
        PendingEdit interrupted = Save.readDataIfPresent(pendingSavePath(), PendingEdit.class);
        if (interrupted != null) {
            Value receipt = snapshot.get(interrupted.receiptKey);
            boolean accepted = receipt != null && (interrupted.trade || equal(receipt.json, playerIdentity));
            previous = Json.gson.fromJson(accepted ? interrupted.after : interrupted.before, Network.PlayerData.class);
            Save.saveData(previous, playerSave);
            String journalPath = pendingSavePath() + ".json.zip";
            disk.submit(() -> java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(journalPath)));
            System.out.println("COOP_RECOVERED_EDIT accepted=" + accepted);
        }
        if (previous != null) game.player = new Player(previous);
        // Setup appearance is the player's current choice; saves restore inventory/progress.
        game.player.character = setupPlayer.character;
        game.player.skinColor = new Color(setupPlayer.skinColor);
        game.player.setColor(new Color(setupPlayer.color));
        game.player.name = options.name; game.player.network.id = session.id();
        // A restored party spawns standing on this host island, not in a stale HM animation.
        game.player.currFieldMove = ""; game.player.hmPokemon = null; game.player.flyingAction = null;
        for (Pokemon member : game.player.pokemon) member.previousOwner = member.lanOwnerId == null ? game.player : resolveOwner(member.lanOwnerId, member.lanOwnerName);
        for (Map.Entry<String, Value> entry : snapshot.entrySet()) if (!entry.getKey().startsWith("tile:")) apply(entry.getKey(), entry.getValue().json);
        Pose restored = session.peers().get(session.id());
        int savedLayer = restored == null ? -1 : restored.layer;
        Map<Vector2, Tile> restoredTiles = savedLayer >= -1 && savedLayer < game.map.interiorTiles.size() ? tiles(savedLayer) : null;
        Vector2 savedPosition = restored == null ? null : new Vector2(restored.x, restored.y);
        Tile savedTile = restoredTiles == null || savedPosition == null ? null : restoredTiles.get(new Vector2(
                (float)Math.floor(savedPosition.x / 16f) * 16f, (float)Math.floor(savedPosition.y / 16f) * 16f));
        boolean restore = session.hasSavedPosition() && restored != null && world.mapId.equals(restored.mapId) && savedTile != null && !savedTile.isSolid;
        // A disconnect can interrupt a tile step; standing actions require a grid coordinate.
        Vector2 spawn = restore ? savedTile.position.cpy() : guestSpawn();
        if (restore) {
            game.map.tiles = restoredTiles;
            if (savedLayer >= 0) game.map.interiorTilesIndex = savedLayer;
            game.player.dirFacing = restored.direction;
        }
        game.player.position.set(spawn); game.player.spawnLoc.set(spawn); game.player.spawnIndex = restore ? savedLayer : -1;
        game.player.network.tiles = game.map.tiles;
        game.cam.position.set(spawn.x + 16, spawn.y, 0);
        game.server = new Server(); Network.register(game.server); // legacy-save reader only; never bound/listening
        game.players.clear();
        game.start();
        if (previous != null && game.player.pokemon.size() > previous.pokemon.size()) {
            game.player.pokemon.remove(game.player.pokemon.size() - 1); game.player.setCurrPokemon();
        }
        game.playerCanMove = true; makeMinimap();
        observed.putAll(snapshot);
        for (Map.Entry<String, Value> entry : snapshot.entrySet()) baseline.put(entry.getKey(), entry.getValue().json);
        game.player.currSprite = game.player.standingSprites.get(game.player.dirFacing);
        initialized = true; checkpointPrivate();
        System.out.println("COOP_READY join id=" + session.id() + " tiles=" + game.map.overworldTiles.size());
    }

    boolean simulationBlocked() { return initialized && failure != null; }
    private void reconcile() {
        Map<String, Value> authoritative = session.drainResourceChanges();
        for (Map.Entry<String, Value> entry : authoritative.entrySet()) {
            String key = entry.getKey(); Value value = entry.getValue(); Value old = observed.get(key);
            // ACK installation can overtake an older queued delta on the render thread.
            if (old == null || old.revision < value.revision) {
                apply(key, value.json);
                baseline.put(key, value.json); observed.put(key, value);
            }
        }
    }
    private void finishTransaction() {
        if (pendingTransaction == null) return;
        if (!transactionSent) {
            if (journal != null && !journal.isDone()) return;
            try {
                if (journal != null) journal.get();
                transactionSent = session.transact(pendingTransaction, journalExpected, pending);
                if (!transactionSent) failure = "Cannot send shared edit; reconnect to recover safely";
            } catch (Exception error) { failure = "Cannot journal LAN edit: " + error.getMessage(); }
            journal = null;
            transactionStarted = System.nanoTime();
            if (failure != null) return;
        }
        LanSession.TransactionResult result = session.takeTransactionResult(pendingTransaction);
        if (result == null) {
            if (System.nanoTime() - transactionStarted > 10_000_000_000L) {
                failure = "Shared edit timed out; reconnect to recover it safely";
                session.close();
            }
            return;
        }
        boolean stale = false;
        for (Map.Entry<String,Value> entry : result.authoritative.entrySet())
            if (entry.getValue().revision != journalExpected.get(entry.getKey())) stale = true;
        if (!result.accepted && stale && editRetries++ < 3) {
            // Rebase only disjoint JSON fields. A timer/AI update must not reject a
            // terrain edit, and a world edit must never restore private inventory.
            Map<String,String> rebased = new LinkedHashMap<>();
            for (Map.Entry<String,String> edit : pending.entrySet()) {
                Value current = result.authoritative.get(edit.getKey());
                if (privateEffects && !equal(baseline.get(edit.getKey()), current.json)
                        && equal(edit.getValue(), current.json)) { rebased.clear(); break; }
                String next = rebase(baseline.get(edit.getKey()), edit.getValue(), current.json);
                if (next == null && !(equal(baseline.get(edit.getKey()), current.json) || equal(edit.getValue(), current.json)
                        || canRebaseRemoval(edit.getKey(), baseline.get(edit.getKey()), edit.getValue(), current.json))) { rebased.clear(); break; }
                rebased.put(edit.getKey(), next);
            }
            if (!rebased.isEmpty()) {
                pending.clear(); pending.putAll(rebased);
                journalExpected = new LinkedHashMap<>();
                for (Map.Entry<String,Value> entry : result.authoritative.entrySet()) {
                    journalExpected.put(entry.getKey(), entry.getValue().revision);
                    apply(entry.getKey(), rebased.get(entry.getKey()));
                    observed.put(entry.getKey(), entry.getValue());
                    baseline.put(entry.getKey(), entry.getValue().json);
                }
                pendingTransaction = UUID.randomUUID().toString();
                transactionSent = false; journal = null;
                return;
            }
        }
        settledEdits++; lastEditAccepted = result.accepted;
        if (!result.accepted) {
            if (privateEffects) restorePrivate();
            System.err.println("COOP_EDIT_REJECTED stale=" + stale + " keys=" + pending.keySet());
            notice = "Shared resource changed; please try again";
            noticeUntil = System.nanoTime() + 5_000_000_000L;
        }
        editRetries = 0;
        for (Map.Entry<String,Value> entry : result.authoritative.entrySet()) {
            String key = entry.getKey(); Value value = entry.getValue();
            if (!result.accepted || !equal(pending.get(key), value.json)) apply(key, value.json);
            if (value.revision == 0) { observed.remove(key); baseline.remove(key); }
            else { observed.put(key, value); baseline.put(key, value.json); }
            rememberReplica(key);
        }
        pending.clear(); pendingTransaction = null; privateEffects = false;
        checkpointPrivate();
        reconcile();
        if (isGuest()) saveGuest(true);
        if (saveRequested) {
            saveRequested = false;
            saveHostReceipts(); saveHostSnapshot();
        }
    }
    private void scanChanges() { scanChanges(false); }

    private void scanChanges(boolean full) {
        if (pendingTransaction != null) return;
        boolean hasPrivateEffects = privateCheckpoint != null
                && privateCheckpoint.changed(game.player.getItemsDict(), game.player.pokemon);
        Map<String, String> current = capture(full);
        if (isGuest() && hasPrivateEffects && !full) {
            captureNewTiles(current, game.map.overworldTiles, -1);
            for (int i=0; i<game.map.interiorTiles.size(); i++)
                if (game.map.interiorTiles.get(i) != null) captureNewTiles(current, game.map.interiorTiles.get(i), i);
        }
        Set<String> keys = new java.util.LinkedHashSet<>(current.keySet());
        // Incremental captures contain only audited terrain, never the entire island.
        // Creature membership still needs deletion detection; full saves include all keys.
        for (String key : baseline.keySet())
            if (full || key.startsWith("pokemon:")) keys.add(key);
        Map<String,String> changed = new LinkedHashMap<>();
        int layer = game.map.tiles == game.map.overworldTiles ? -1 : game.map.interiorTilesIndex;
        for (String key : keys) {
            String before = isGuest() && replicaBaseline.containsKey(key) ? replicaBaseline.get(key) : baseline.get(key);
            String next = current.get(key);
            if (equal(before, next)) continue;
            if (isGuest()) {
                if (!hasPrivateEffects && !nearbyEdit(key, before, next, layer, game.player.position.x, game.player.position.y)) continue;
                // Project actual local changes onto server JSON, preserving normalized/default fields.
                if (next != null) {
                    String projected = rebase(before, next, baseline.get(key));
                    if (projected == null) continue;
                    next = projected;
                }
            }
            if (!equal(baseline.get(key), next)) changed.put(key, next);
        }
        if (changed.isEmpty()) { checkpointPrivate(); return; }
        privateEffects = hasPrivateEffects;
        Map<String,Long> expected = new LinkedHashMap<>();
        for (Map.Entry<String,String> entry : changed.entrySet()) {
            Value old = observed.get(entry.getKey());
            expected.put(entry.getKey(), old == null ? 0L : old.revision);
            pending.put(entry.getKey(), entry.getValue());
            if (pending.size() == 127) break;
        }
        String transaction = UUID.randomUUID().toString();
        pendingTransaction = transaction; transactionStarted = System.nanoTime();
        journalExpected = expected; transactionSent = false;
        if (journal != null) return;
        try {
            transactionSent = session.transact(transaction, expected, pending);
            if (!transactionSent) {
                failure = "Cannot send shared edit; reconnect to recover safely";
            }
        } catch (IllegalArgumentException invalid) {
            if (privateEffects) restorePrivate();
            for (String key : pending.keySet()) apply(key, baseline.get(key));
            pending.clear(); pendingTransaction = null;
            failure = "Shared edit rejected locally: " + invalid.getMessage();
        }
    }
    private void captureNewTiles(Map<String,String> state, Map<Vector2,Tile> tiles, int layer) {
        for (Tile tile : tiles.values()) if (!baseline.containsKey(tileKey(layer,tile.position))) captureTile(state,tile,layer);
    }
    static boolean canRebaseRemoval(String key, String before, String proposed, String current) {
        if (!key.startsWith("pokemon:") || proposed != null || before == null || current == null) return false;
        try {
            CreatureInfo original = Json.gson.fromJson(before, CreatureInfo.class);
            CreatureInfo head = Json.gson.fromJson(current, CreatureInfo.class);
            head.data.harvestTimer = original.data.harvestTimer;
            head.data.position = original.data.position;
            head.direction = original.direction;
            return equal(Json.gson.toJson(original), Json.gson.toJson(head));
        } catch (RuntimeException invalid) { return false; }
    }
    static boolean nearbyEdit(String key, String before, String proposed, int layer, float x, float y) {
        try {
            if (key.startsWith("tile:")) {
                String[] fields = key.split(":");
                return fields.length == 4 && Integer.parseInt(fields[1]) == layer
                        && Math.abs(Integer.parseInt(fields[2])-x) <= 64 && Math.abs(Integer.parseInt(fields[3])-y) <= 64;
            }
            if (key.startsWith("pokemon:")) {
                CreatureInfo info = Json.gson.fromJson(proposed == null ? before : proposed, CreatureInfo.class);
                return info != null && info.data != null && info.data.position != null && info.layer == layer
                        && Math.abs(info.data.position.x-x) <= 64 && Math.abs(info.data.position.y-y) <= 64;
            }
            return key.startsWith("receipt:") || key.startsWith("trade:");
        } catch (RuntimeException invalid) { return false; }
    }
    static String rebase(String before, String proposed, String current) {
        if (equal(before, current) || equal(proposed, current)) return proposed;
        if (before == null || proposed == null || current == null) return null;
        try {
            return Json.gson.toJson(mergeJson(Json.gson.fromJson(before, com.google.gson.JsonElement.class),
                    Json.gson.fromJson(proposed, com.google.gson.JsonElement.class), Json.gson.fromJson(current, com.google.gson.JsonElement.class)));
        } catch (RuntimeException invalid) { return null; }
    }

    private static com.google.gson.JsonElement mergeJson(com.google.gson.JsonElement base,
            com.google.gson.JsonElement edit, com.google.gson.JsonElement head) {
        if (equal(base, head) || equal(edit, head)) return edit;
        if (equal(base, edit)) return head;
        if (base == null || edit == null || head == null || !base.isJsonObject() || !edit.isJsonObject() || !head.isJsonObject())
            throw new IllegalArgumentException("Concurrent edit to the same field");
        com.google.gson.JsonObject result = head.getAsJsonObject().deepCopy();
        Set<String> fields = new HashSet<>(base.getAsJsonObject().keySet()); fields.addAll(edit.getAsJsonObject().keySet());
        for (String field : fields) {
            com.google.gson.JsonElement value = mergeJson(base.getAsJsonObject().get(field), edit.getAsJsonObject().get(field), result.get(field));
            if (value == null) result.remove(field); else result.add(field, value);
        }
        return result;
    }

    boolean prepareSave() {
        if (isGuest()) return true;
        if (!initialized) return false;
        if (pendingTransaction == null) scanChanges(true);
        if (pendingTransaction != null) { saveRequested = true; return false; }
        saveHostReceipts();
        return true;
    }
    private void saveHostReceipts() {
        ReceiptStore store = new ReceiptStore();
        for (Map.Entry<String,Value> entry : session.resources().entrySet())
            if ((entry.getKey().startsWith("receipt:") || entry.getKey().startsWith("trade:")) && entry.getValue().json != null) store.values.put(entry.getKey(), entry.getValue().json);
        store.players = session.savedPositions();
        String path = game.map.id + ".sav/coop-receipts";
        disk.submit(() -> {
            try { writeReceipts(java.nio.file.Paths.get(path), store); }
            catch (Save.SaveFailure error) { failure = "Cannot save LAN receipts: " + error.getMessage(); }
        });
    }
    void saveHostSnapshot() {
        // Freeze mutable game objects on the render thread; compression and disk I/O belong to the worker.
        com.google.gson.JsonElement gameData = Json.gson.toJsonTree(new Network.GameSaveData(game));
        com.google.gson.JsonElement mapData = Json.gson.toJsonTree(new Network.MapSaveData(game));
        com.google.gson.JsonElement spawnData = Json.gson.toJsonTree(new Network.PlayerSpawnLocationData(game.player));
        String directory = game.map.id + ".sav/", mapId = game.map.currMapId, id = game.player.network.id;
        Pixmap minimap = null;
        if (game.map.minimap != null) {
            minimap = new Pixmap(game.map.minimap.getWidth(), game.map.minimap.getHeight(), Pixmap.Format.RGBA8888);
            minimap.setBlending(Pixmap.Blending.None); minimap.drawPixmap(game.map.minimap, 0, 0);
        }
        final Pixmap image = minimap;
        disk.submit(() -> {
            try {
                java.io.File folder = new java.io.File(directory);
                if (folder.isDirectory()) {
                    try { org.zeroturnaround.zip.ZipUtil.pack(folder, new java.io.File(directory.substring(0, directory.length() - 1) + ".zip")); }
                    catch (Exception backupError) { System.err.println("Host backup failed: " + backupError.getMessage()); }
                }
                java.nio.file.Files.createDirectories(java.nio.file.Paths.get(directory));
                Save.saveData(gameData, directory + "game");
                Save.saveData(mapData, directory + "map" + mapId);
                Save.saveData(spawnData, directory + "spawn" + id + mapId);
                if (image != null) com.badlogic.gdx.graphics.PixmapIO.writePNG(new com.badlogic.gdx.files.FileHandle(directory + "map" + mapId + ".png"), image);
            } catch (Exception error) { failure = "Cannot save hosted world: " + error.getMessage(); }
            finally { if (image != null) image.dispose(); }
        });
    }
    private static boolean equal(Object a, Object b) { return a == b || a != null && a.equals(b); }

    private Map<Vector2, Tile> tiles(int layer) { return layer == -1 ? game.map.overworldTiles : game.map.getInteriorLayer(layer); }
    // Moving creatures can temporarily share a position in a streamed snapshot.
    // Keep both identities in the spatial index instead of losing one and proposing its deletion.
    static <T> void indexValue(Map<Vector2,T> index, Vector2 position, T value) {
        index.values().removeIf(existing -> existing == value);
        T displaced = index.get(position);
        if (displaced != null) {
            Vector2 spare = position.cpy();
            do { spare.x += .0625f; } while (index.containsKey(spare));
            index.put(spare, displaced);
        }
        index.put(position.cpy(), value);
    }
    private void apply(String key, String json) {
        installResource(key, json);
        rememberReplica(key);
    }
    private void rememberReplica(String key) {
        if (!isGuest()) return;
        if (key.startsWith("tile:")) {
            String[] parts = key.split(":"); int layer = Integer.parseInt(parts[1]);
            Tile tile = tiles(layer).get(new Vector2(Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
            if (tile == null) replicaBaseline.put(key, null);
            else {
                Map<String,String> state = new HashMap<>(); captureTile(state, tile, layer);
                replicaBaseline.put(key, state.get(key));
            }
        } else if (key.startsWith("pokemon:")) {
            Pokemon pokemon = creatures.get(key);
            if (pokemon == null) replicaBaseline.put(key, null);
            else {
                CreatureInfo info = new CreatureInfo(); info.data = new Network.PokemonData(pokemon);
                info.direction = pokemon.dirFacing; info.harvestable = pokemon.hasItem;
                info.encounterOwner = encounters.get(key);
                info.layer = pokemon.mapTiles == game.map.overworldTiles || pokemon.mapTiles == null ? -1 : pokemon.interiorIndex;
                replicaBaseline.put(key, Json.gson.toJson(info));
            }
        }
    }
    private void installResource(String key, String json) {
        if (key.startsWith("trade:")) {
            // Decision persistence runs on the authority before its commit is broadcast.
        } else if (key.equals("clock")) {
            if (json != null) {
                ClockInfo clock = Json.gson.fromJson(json, ClockInfo.class);
                String previousTime = game.map.timeOfDay;
                game.map.timeOfDay = clock.timeOfDay; CycleDayNight.dayTimer = clock.dayTimer;
                if (isGuest()) {
                    game.mapBatch.setColor("night".equals(clock.timeOfDay) ? PkmnMap.nightColor : Color.WHITE);
                    game.currCloudTexture = "night".equals(clock.timeOfDay) ? game.nightTexture : game.cloudTexture;
                    if (!equal(previousTime, clock.timeOfDay) && game.musicController != null)
                        game.musicController.startTimeOfDay = clock.timeOfDay;
                }
            }
        } else if (key.startsWith("tile:")) {
            String[] fields = key.split(":"); int layer = Integer.parseInt(fields[1]);
            Vector2 pos = new Vector2(Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
            if (json == null) tiles(layer).remove(pos);
            else {
                Network.TileData data = Json.gson.fromJson(json, Network.TileData.class);
                Tile tile = Tile.get(data, routes.get(data.routeBelongsTo));
                tiles(layer).put(pos, tile);
            }
            game.map.refreshCache = true;
        } else if (key.startsWith("pokemon:")) {
            // Timer updates must not replace objects referenced by interaction actions.
            Pokemon existing = creatures.get(key);
            String oldJson = baseline.get(key);
            if (existing != null && oldJson != null && json != null) {
                CreatureInfo oldInfo = Json.gson.fromJson(oldJson, CreatureInfo.class);
                CreatureInfo newInfo = Json.gson.fromJson(json, CreatureInfo.class);
                oldInfo.data.harvestTimer = newInfo.data.harvestTimer; oldInfo.harvestable = newInfo.harvestable;
                if (isGuest()) {
                    oldInfo.data.position = newInfo.data.position;
                    oldInfo.direction = newInfo.direction;
                    oldInfo.encounterOwner = newInfo.encounterOwner;
                }
                if (equal(Json.gson.toJson(oldInfo), Json.gson.toJson(newInfo))) {
                    if (isGuest()) {
                        if (!existing.position.equals(newInfo.data.position)) {
                            CreatureMotion motion = creatureMotion.computeIfAbsent(existing,
                                    p -> new CreatureMotion(p.position.x, p.position.y));
                            motion.target(newInfo.data.position.x, newInfo.data.position.y, System.nanoTime());
                            game.map.pokemon.values().removeIf(p -> p == existing);
                            existing.position.set(newInfo.data.position);
                            indexValue(game.map.pokemon, existing.position, existing);
                            game.map.refreshOnscreenPokemon = true;
                        }
                        existing.dirFacing = newInfo.direction == null ? "down" : newInfo.direction;
                        existing.currOwSprite = existing.standingSprites.get(existing.dirFacing);
                        if (newInfo.encounterOwner == null) encounters.remove(key);
                        else encounters.put(key, newInfo.encounterOwner);
                    }
                    existing.harvestTimer = newInfo.data.harvestTimer; existing.hasItem = newInfo.harvestable;
                    return;
                }
            }
            encounters.remove(key);
            Pokemon previous = creatures.remove(key);
            if (previous != null) {
                game.map.pokemon.values().removeIf(pokemon -> pokemon == previous);
                previous.removeDrawActions(game);
                game.actionStack.removeIf(action -> action == previous.standingAction);
                creatureIds.remove(previous); creatureMotion.remove(previous);
            }
            if (json != null) {
                CreatureInfo info = Json.gson.fromJson(json, CreatureInfo.class);
                if (info.encounterOwner != null) encounters.put(key, info.encounterOwner);
                Pokemon pokemon = new Pokemon(info.data); pokemon.position = info.data.position.cpy();
                pokemon.dirFacing = info.direction == null ? "down" : info.direction;
                pokemon.hasItem = info.harvestable;
                pokemon.mapTiles = tiles(info.layer); pokemon.interiorIndex = info.layer == -1 ? 100 : info.layer;
                creatures.put(key, pokemon); creatureIds.put(pokemon, key); indexValue(game.map.pokemon, pokemon.position, pokemon);
                pokemon.standingAction = pokemon.new Standing();
                if (isGuest()) {
                    // Initialize sprite/draw/interaction bookkeeping without scheduling
                    // the host-only Standing AI on the guest render loop.
                    pokemon.standingAction.firstStep(game);
                    pokemon.standingAction.firstStep = false;
                } else game.insertAction(pokemon.standingAction);
            }
            game.map.refreshOnscreenPokemon = true; game.map.refreshCache = true;
        }
    }

    boolean ownsBattle(Pokemon pokemon) {
        return session != null && session.id().equals(encounters.get(creatureIds.get(pokemon)));
    }
    boolean reserved(Pokemon pokemon) { return encounters.containsKey(creatureIds.get(pokemon)); }
    void endWorldBattle(Pokemon pokemon) {
        if (ownsBattle(pokemon)) encounters.remove(creatureIds.get(pokemon));
    }
    Action reserveBattle(final boolean fishing) {
        return new Action() {
            String creatureId;
            boolean requested;
            @Override public void step(Game current) {
                if (!requested) {
                    if (pendingTransaction != null) return;
                    creatureId = creatureIds.get(current.battle.oppPokemon);
                    if (creatureId == null || encounters.containsKey(creatureId)) { denied(current); return; }
                    encounters.put(creatureId, session.id());
                    scanChanges();
                    requested = true;
                    return;
                }
                if (pendingTransaction != null) return;
                Pokemon pokemon = creatures.get(creatureId);
                current.actionStack.remove(this);
                Value committed = observed.get(creatureId);
                CreatureInfo claim = committed == null || committed.json == null ? null : Json.gson.fromJson(committed.json, CreatureInfo.class);
                if (pokemon == null || claim == null || !session.id().equals(claim.encounterOwner) || !ownsBattle(pokemon)) {
                    if (pokemon != null && ownsBattle(pokemon)) encounters.remove(creatureId);
                    denied(current); return;
                }
                current.battle.oppPokemon = pokemon;
                pokemon.inBattle = true; pokemon.canMove = false;
                current.insertAction(Battle.getIntroAction(current, fishing));
            }
            private void denied(Game current) {
                current.actionStack.remove(this);
                if (current.musicController != null) current.musicController.startBattle = null;
                current.insertAction(new DisplayText(current, "That POKEMON is already in another encounter.", null, null,
                        new SetField(current, "playerCanMove", true, null)));
            }
        };
    }

    private boolean allowMutation(String actorId, Pose actor, String key, String previous, String proposed) {
        return allowWorldMutation(world, session != null && actorId.equals(session.id()), actor, key, previous, proposed);
    }
    static boolean allowWorldMutation(WorldInfo world, boolean localHost, Pose actor, String key, String previous, String proposed) {
        if (key.startsWith("trade:")) return previous == null && key.matches("trade:[0-9a-f-]{36}")
                && proposed != null && actor != null && proposed.equals(actor.id);
        if (key.startsWith("receipt:")) return previous == null && key.matches("receipt:[0-9a-f-]{36}")
                && proposed != null && proposed.matches("[0-9a-f-]{36}");
        if (key.equals("clock")) return localHost;
        if (!localHost && (actor == null || !world.mapId.equals(actor.mapId))) return false;
        try {
            if (key.startsWith("tile:")) {
                String[] parts = key.split(":"); if (parts.length != 4) return false;
                int layer = Integer.parseInt(parts[1]);
                float x = Integer.parseInt(parts[2]); float y = Integer.parseInt(parts[3]);
                if (layer < -1 || layer > 255 || x % 16 != 0 || y % 16 != 0) return false;
                if (!localHost && (layer != actor.layer || Math.abs(actor.x - x) > 64 || Math.abs(actor.y - y) > 64)) return false;
                if (proposed == null) return previous != null && (localHost || layer >= 0);
                Network.TileData data = Json.gson.fromJson(proposed, Network.TileData.class);
                return data != null && data.pos != null && data.pos.x == x && data.pos.y == y && data.interiorIndex == layer
                        && validName(data.tileName) && validName(data.tileNameUpper)
                        && data.hasItemAmount >= 0 && data.hasItemAmount <= 999 && (data.message == null || data.message.length() <= 2048)
                        && (data.items == null || data.items.size() <= 64) && (data.doorTiles == null || data.doorTiles.size() <= 32)
                        && (data.routeBelongsTo == null || world.routes.containsKey(data.routeBelongsTo));
            }
            if (key.startsWith("pokemon:") && key.length() <= 64) {
                CreatureInfo old = previous == null ? null : Json.gson.fromJson(previous, CreatureInfo.class);
                if (!localHost && old != null && old.encounterOwner != null && !old.encounterOwner.equals(actor.id)) return false;
                CreatureInfo info = Json.gson.fromJson(proposed == null ? previous : proposed, CreatureInfo.class);
                if (!localHost && proposed != null && info != null && info.encounterOwner != null && !info.encounterOwner.equals(actor.id)) return false;
                return info != null && info.data != null && info.layer >= -1 && info.layer <= 255 && info.data.position != null
                        && Float.isFinite(info.data.position.x) && Float.isFinite(info.data.position.y)
                        && (localHost || info.layer == actor.layer && Math.abs(actor.x - info.data.position.x) <= 64 && Math.abs(actor.y - info.data.position.y) <= 64)
                        && info.data.name != null && info.data.name.matches("[A-Za-z0-9_-]{1,64}")
                        && info.data.attacks != null && info.data.attacks.length == 4
                        && info.data.level >= 1 && info.data.level <= 100;
            }
        } catch (RuntimeException invalid) { return false; }
        return false;
    }
    private static boolean validName(String name) { return name != null && name.matches("[A-Za-z0-9_ -]{0,100}"); }

    private void updateAvatars() {
        Map<String, Pose> peers = session.peers();
        game.players.keySet().removeIf(id -> !peers.containsKey(id) || id.equals(session.id()));
        remoteVisuals.keySet().retainAll(peers.keySet());
        remotePoses.keySet().removeIf(id -> !peers.containsKey(id) || id.equals(session.id()));
        long now = System.nanoTime();
        for (Pose pose : peers.values()) {
            if (pose.id.equals(session.id()) || world.dedicated && pose.id.equals(session.authorityId())) continue;
            Player player = game.players.get(pose.id);
            int localLayer = game.map.tiles == game.map.overworldTiles ? -1 : game.map.interiorTilesIndex;
            if (!game.map.currMapId.equals(pose.mapId) || pose.layer != localLayer
                    || Math.abs(pose.x - game.player.position.x) > 512 || Math.abs(pose.y - game.player.position.y) > 512) {
                if (player != null) player.network.tiles = null;
                continue;
            }
            if (player == null) {
                player = new Player(); player.name = pose.name; player.type = Player.Type.REMOTE;
                player.character = pose.character;

                player.network.id = pose.id; game.players.put(pose.id, player);
                remotePoses.put(pose.id, new RemotePose(pose, now));
            }
            RemotePose smooth = remotePoses.get(pose.id);
            if (!pose.character.equals(smooth.character) || pose.color != smooth.color || pose.skinColor != smooth.skinColor) {
                player.character = pose.character;
                player.skinColor = new Color(pose.skinColor);
                player.setColor(new Color(pose.color));
                smooth.character = pose.character; smooth.color = pose.color; smooth.skinColor = pose.skinColor;
            }
            player.name = pose.name;
            String layerKey = pose.mapId + ":" + pose.layer;
            if (!layerKey.equals(smooth.layerKey) || Vector2.dst2(smooth.x, smooth.y, pose.x, pose.y) > SNAP_DISTANCE_SQUARED) {
                smooth.x = pose.x; smooth.y = pose.y;
            }
            if (smooth.latest != pose) {
                boolean reset = !layerKey.equals(smooth.layerKey)
                        || Vector2.dst2(smooth.targetX, smooth.targetY, pose.x, pose.y) > SNAP_DISTANCE_SQUARED;
                smooth.buffer.add(pose.x, pose.y, now, reset);
                smooth.targetX = pose.x; smooth.targetY = pose.y;
                smooth.receivedAt = now; smooth.latest = pose;
            }
            smooth.mapId = pose.mapId; smooth.layerKey = layerKey;
            float previousX = smooth.x, previousY = smooth.y;
            smooth.x = smooth.buffer.sample(now, true);
            smooth.y = smooth.buffer.sample(now, false);
            float dx = smooth.x - previousX, dy = smooth.y - previousY;
            smooth.distance += (float)Math.sqrt(dx * dx + dy * dy);
            player.position.set(pose.x, pose.y); player.dirFacing = pose.direction;
            String spriteKey = pose.direction + (pose.running ? "_running" : "");
            boolean stepping = pose.moving || dx * dx + dy * dy > 0.04f;
            int phase = (int)(smooth.distance / 4) % 8;
            player.currSprite = !stepping || phase % 4 == 0 ? player.standingSprites.get(spriteKey)
                    : phase < 4 ? player.movingSprites.get(spriteKey) : player.altMovingSprites.get(spriteKey);
            remoteVisuals.put(pose.id, LanVisual.read(pose.visual, remoteVisuals.get(pose.id), player));
            player.network.tiles = world.mapId.equals(pose.mapId) ? tiles(pose.layer) : null;
        }
    }

    static void validateWorld(WorldInfo value) {
        if (value == null || value.worldUid == null || !value.worldUid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                || value.worldId == null || !value.worldId.matches("[A-Za-z0-9_-]{1,32}")
                || value.mapId == null || !value.mapId.matches("-?[0-9]{1,5},-?[0-9]{1,5}")
                || value.spawn == null || value.routes == null || value.routes.size() > 32768
                || value.edges == null || value.edges.isEmpty() || value.edges.size() > 32768
                || value.bottomLeft == null || value.topRight == null || value.interiorIndex < 0 || value.interiorIndex > 255
                || !("day".equals(value.timeOfDay) || "night".equals(value.timeOfDay)))
            throw new IllegalArgumentException("Invalid host world metadata: id=" + (value == null ? "null" : value.worldId + " map=" + value.mapId + " time=" + value.timeOfDay + " layer=" + value.interiorIndex + " edges=" + (value.edges == null ? -1 : value.edges.size()) + " routes=" + (value.routes == null ? -1 : value.routes.size())));
        for (Vector2 position : new Vector2[]{value.spawn, value.bottomLeft, value.topRight}) {
            if (!Float.isFinite(position.x) || !Float.isFinite(position.y) || Math.abs(position.x) > 32752 || Math.abs(position.y) > 32752)
                throw new IllegalArgumentException("Invalid world coordinates");
        }
        float width = value.topRight.x - value.bottomLeft.x, height = value.topRight.y - value.bottomLeft.y;
        if (width <= 0 || height <= 0 || width > 16384 || height > 16384
                || value.spawn.x < value.bottomLeft.x || value.spawn.x > value.topRight.x
                || value.spawn.y < value.bottomLeft.y || value.spawn.y > value.topRight.y)
            throw new IllegalArgumentException("Invalid world bounds");
    }

    boolean worldMatchesAuthority() {
        Map<String,String> local = capture();
        // Compare with the revision set installed by the render thread. The socket
        // reader can advance session.resources() while a full-world scan is running.
        for (Map.Entry<String,Value> entry : observed.entrySet()) {
            if (!equal(entry.getValue().json, local.get(entry.getKey()))) {
                System.err.println("COOP_STATE_MISMATCH key=" + entry.getKey() + " expected=" + entry.getValue().json + " actual=" + local.get(entry.getKey()));
                return false;
            }
        }
        for (Map.Entry<String,String> entry : local.entrySet()) {
            if (!observed.containsKey(entry.getKey()) && entry.getValue() != null) return false;
        }
        return true;
    }
    private static synchronized void writeReceipts(java.nio.file.Path path, ReceiptStore store) throws Save.SaveFailure {
        try {
            ReceiptStore existing = Save.readDataIfPresent(path.toString(), ReceiptStore.class);
            if (existing != null) store.values.putAll(existing.values);
        } catch (Save.ReadFailure error) { throw new IllegalStateException("Cannot read trade decisions", error); }
        Save.saveData(store, path.toString());
    }
    private void flushPlayerSaves() {
        try { disk.submit(() -> {}).get(); }
        catch (Exception error) { throw new IllegalStateException("Cannot flush local player save", error); }
    }
    static void installTradePersistence(LanSession authority, java.nio.file.Path directory) {
        authority.setCommitListener(updates -> {
            if (updates.keySet().stream().noneMatch(key -> key.startsWith("trade:"))) return;
            ReceiptStore store = new ReceiptStore();
            Map<String,Value> all = new LinkedHashMap<>(authority.resources()); all.putAll(updates);
            for (Map.Entry<String,Value> entry : all.entrySet())
                if ((entry.getKey().startsWith("receipt:") || entry.getKey().startsWith("trade:")) && entry.getValue().json != null)
                    store.values.put(entry.getKey(), entry.getValue().json);
            store.players = authority.savedPositions();
            try { writeReceipts(directory.resolve("coop-receipts"), store); }
            catch (Save.SaveFailure error) { throw new IOException("Cannot persist trade decision", error); }
        });
    }

    String identity() { return playerIdentity; }
    void prepareTrade(int slot, Pokemon replacement, String tradeId) {
        if (pendingTransaction != null || slot < 0 || slot >= game.player.pokemon.size()) throw new IllegalStateException("A shared edit is still settling");
        flushPlayerSaves();
        PendingEdit record = new PendingEdit(); record.trade = true; record.receiptKey = "trade:" + tradeId;
        record.before = Json.gson.toJson(new Network.PlayerData(game.player));
        Pokemon original = game.player.pokemon.set(slot, replacement);
        Pokemon selected = game.player.currPokemon;
        try {
            if (selected == original) game.player.currPokemon = replacement;
            record.after = Json.gson.toJson(new Network.PlayerData(game.player));
        } finally { game.player.pokemon.set(slot, original); game.player.currPokemon = selected; }
        try {
            Save.saveData(Json.gson.fromJson(record.before, Network.PlayerData.class), playerSavePath());
            Save.saveData(record, pendingSavePath());
        } catch (Save.SaveFailure error) { throw new IllegalStateException("Cannot prepare trade", error); }
    }
    void persistTrade() {
        flushPlayerSaves();
        try { Save.saveData(new Network.PlayerData(game.player), playerSavePath()); }
        catch (Save.SaveFailure error) { throw new IllegalStateException("Cannot persist trade", error); }
        if (!isGuest()) {
            // Save the host snapshot before clearing its recovery record.
            saveHostSnapshot(); flushPlayerSaves();
            if (failure != null) throw new IllegalStateException(failure);
        }
        try { java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(pendingSavePath() + ".json.zip")); }
        catch (IOException error) { throw new IllegalStateException("Cannot clear trade journal", error); }
        checkpointPrivate();
    }
    void discardTrade() {
        try { java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(pendingSavePath() + ".json.zip")); }
        catch (IOException error) { throw new IllegalStateException("Cannot clear trade journal", error); }
    }

    private String playerSavePath() { return "coop-player-" + world.worldUid + "-" + playerIdentity; }
    private String pendingSavePath() { return "coop-pending-" + world.worldUid + "-" + playerIdentity; }
    boolean saveGuest() { return saveGuest(false); }
    private boolean saveGuest(boolean clearJournal) {
        if (isGuest() && initialized && pendingTransaction == null) {
            try {
                com.google.gson.JsonElement snapshot = Json.gson.toJsonTree(new Network.PlayerData(game.player));
                String path = playerSavePath(), journalPath = pendingSavePath() + ".json.zip";
                disk.submit(() -> {
                    try {
                        Save.saveData(snapshot, path);
                        if (clearJournal) java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(journalPath));
                    }
                    catch (Exception error) { failure = "Cannot persist co-op player: " + error.getMessage(); }
                });
                return true;
            }
            catch (RuntimeException error) {
                failure = "Cannot persist local co-op party; reconnect after fixing storage: " + error.getMessage();
                System.err.println(failure);
            }
        }
        return false;
    }
    @Override public void close() {
        interactions.dispose(); spectator.dispose(); hud.dispose();
        closed = true; saveGuest();
        if (session != null) session.close();
        if (worker != null) worker.interrupt();
        disk.shutdown();
        try { disk.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); }
    }
}
