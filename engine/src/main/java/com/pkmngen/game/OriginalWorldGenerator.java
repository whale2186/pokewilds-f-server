package com.pkmngen.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.pkmngen.game.util.Json;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Runs the local generator with CPU pixmaps and a no-op GPU; no window or display is needed. */
final class OriginalWorldGenerator {
    static DedicatedServer.Snapshot generate(String name, long seed) {
        GdxNativesLoader.load();
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        Gdx.files = new Lwjgl3Files();
        Gdx.gl = Gdx.gl20 = proxy(GL20.class, (method, args) -> {
            if (method.equals("glGetString")) return "";
            if (method.equals("glGenTexture")) return 1;
            return null;
        });
        Gdx.graphics = proxy(Graphics.class, (method, args) -> method.equals("getGL20") ? Gdx.gl20 : null);
        Gdx.app = proxy(Application.class, (method, args) -> {
            if (method.equals("postRunnable")) queue.add((Runnable)args[0]);
            if (method.equals("getType")) return Application.ApplicationType.HeadlessDesktop;
            return null;
        });
        Game game = new Game(); Game.staticGame = game;
        game.gameThread = Thread.currentThread(); Game.rand.setSeed(seed);
        game.map = new PkmnMap(name); game.map.rand.setSeed(seed);
        game.cam = new OrthographicCamera(); game.player = new Player();
        AtomicReference<GenIsland1> generated = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try { generated.set(new GenIsland1(game, new Vector2(), 30000)); }
            catch (Throwable error) { failure.set(error); }
        }, "original-island-generator");
        worker.start();
        try {
            while (worker.isAlive() || !queue.isEmpty()) {
                Runnable task = queue.poll(10, TimeUnit.MILLISECONDS);
                if (task != null) task.run();
            }
            if (failure.get() != null) throw new IllegalStateException("Original island generation failed", failure.get());
            GenIsland1 island = generated.get();
            // This is the same installation step used by the local new-game menu.
            island.step(game);
            while (!island.doActions.isEmpty()) island.step(game);
            for (Action action : new ArrayList<>(game.actionStack)) {
                if (action instanceof Pokemon.Standing || action instanceof Pokemon.Burrowed) action.firstStep(game);
            }
            DedicatedServer.Snapshot state = new DedicatedServer.Snapshot();
            LanCoop.WorldInfo world = state.world = new LanCoop.WorldInfo();
            world.dedicated = true; world.worldId = name; world.worldUid = UUID.randomUUID().toString();
            world.mapId = game.map.currMapId; world.timeOfDay = game.map.timeOfDay; world.dayTimer = 18000;
            world.bottomLeft = game.map.bottomLeft; world.topRight = game.map.topRight;
            world.edges = game.map.edges; world.spawn = game.player.spawnLoc.cpy(); world.interiorIndex = game.map.interiorTilesIndex;
            Map<Route,String> routes = new IdentityHashMap<>();
            exportTiles(state, game.map.overworldTiles, -1, routes);
            for (int i = 0; i < game.map.interiorTiles.size(); i++)
                if (game.map.interiorTiles.get(i) != null) exportTiles(state, game.map.interiorTiles.get(i), i, routes);
            for (Pokemon pokemon : game.map.pokemon.values()) {
                LanCoop.CreatureInfo info = new LanCoop.CreatureInfo(); info.data = new Network.PokemonData(pokemon);
                info.direction = pokemon.dirFacing; info.harvestable = pokemon.hasItem;
                info.layer = pokemon.mapTiles == game.map.overworldTiles || pokemon.mapTiles == null ? -1 : pokemon.interiorIndex;
                state.resources.put("pokemon:" + UUID.randomUUID(), Json.gson.toJson(info));
            }
            LanCoop.ClockInfo clock = new LanCoop.ClockInfo(); clock.timeOfDay = world.timeOfDay; clock.dayTimer = world.dayTimer;
            state.resources.put("clock", Json.gson.toJson(clock));
            return state;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt(); throw new IllegalStateException(interrupted);
        }
    }
    private static void exportTiles(DedicatedServer.Snapshot state, Map<Vector2,Tile> tiles, int layer, Map<Route,String> routes) {
        for (Tile tile : tiles.values()) {
            Network.TileData data = new Network.TileData(tile, layer);
            if (tile.routeBelongsTo != null) {
                String id = routes.get(tile.routeBelongsTo);
                if (id == null) {
                    id = "route" + routes.size(); routes.put(tile.routeBelongsTo, id);
                    Network.RouteData route = new Network.RouteData(tile.routeBelongsTo); route.classId = id;
                    state.world.routes.put(id, route);
                }
                data.routeBelongsTo = id;
            }
            DedicatedServer.addTile(state, data, layer);
        }
    }
    interface Handler { Object call(String method, Object[] args); }
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Handler handler) {
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (object, method, args) -> {
            Object result = handler.call(method.getName(), args);
            if (result != null || !method.getReturnType().isPrimitive() || method.getReturnType() == void.class) return result;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType() == float.class) return 0f;
            if (method.getReturnType() == long.class) return 0L;
            return 0;
        });
    }
}
