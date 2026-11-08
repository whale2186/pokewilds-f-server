package com.pkmngen.game;

/** Dedicated entry point to the original island generator and shared building route metadata. */
final class DedicatedWorld {
    static DedicatedServer.Snapshot generate(String name, long seed) {
        return OriginalWorldGenerator.generate(name, seed);
    }

    static void addBuildingRoutes(LanCoop.WorldInfo world) {
        String[] names = {"beach2", "desert1", "mountain1", "snow1", "forest1", "volcano1", "graveyard1", "savanna2", "wooded_lake1", "deep_forest"};
        for (String name : names) {
            String id = "terrain-" + name;
            if (!world.routes.containsKey(id)) {
                Network.RouteData route = new Network.RouteData(); route.name = name; route.level = 22; route.classId = id;
                world.routes.put(id, route);
            }
        }
    }

}
