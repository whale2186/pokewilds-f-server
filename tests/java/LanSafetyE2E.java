package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.Json;
import com.pkmngen.game.util.Save;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/** Real game processes contend for a dropped Pokemon and a tile's resources. */
public final class LanSafetyE2E extends Game {
    private final String role = System.getProperty("e2e.role");
    private final Path barrier = Paths.get(System.getProperty("e2e.barrier"));
    private final long launched = System.nanoTime();
    private Pokemon fixture;
    private String creatureKey;
    private int stage, initialParty, initialStones;
    private long beforeEdits, stageAt, saveAt;
    private boolean won;
    private Vector2 edit;
    LanSafetyE2E(String[] args) { super(args, 2); }
    private void mark(String message) { System.out.println("SAFETY " + role + " " + message); }
    @Override public void create() {
        super.create(); InputProcessor.acceptInput = false; Game.musicDisabled = true;
        if (role.equals("host")) {
            fixture = new Pokemon("machop", 6); fixture.nickname = "LANRACE";
            fixture.previousOwner = player; player.pokemon.add(fixture);
        }
    }
    @Override public void render() {
        if (stage == 6) return; // LWJGL can render once more for a queued resize after exit().
        try {
            super.render();
            if (System.nanoTime() - launched > 180_000_000_000L) throw new AssertionError("Safety suite deadline");
            if (coop.problem() != null) throw new AssertionError(coop.problem());
            if (!coop.ready() || coop.simulationBlocked()) return;
            edit = coop.sharedSpawn();
            if (role.equals("host")) hostStep();
            else if (role.equals("reload")) reloadStep();
            else if (role.startsWith("restore-")) restoreStep();
            else guestStep();
        } catch (Throwable error) { error.printStackTrace(); System.exit(1); }
    }
    private void hostStep() throws Exception {
        if (stage == 0) {
            fixture.position.set(edit); fixture.mapTiles = map.overworldTiles; fixture.canMove = false;
            fixture.new RemoveFromInventory().firstStep(this);
            fixture.canMove = false;
            Tile ground=map.overworldTiles.get(edit);
            ground.nameUpper=""; ground.hasItem=null; ground.init();
            player.setItemAmount("stone",2);
            new DrawUseTossMenu.DropItem("stone",2,edit,null).step(this);
            if(!"item_pack".equals(map.overworldTiles.get(edit).nameUpper) || amount("stone")>0)
                throw new AssertionError("Bag drop did not create a pack and consume inventory");
            Pokemon placed=new Pokemon("abra",6); placed.nickname="LANPEN"; placed.previousOwner=player;
            placed.position.set(edit).add(32,0); placed.mapTiles=map.overworldTiles; placed.canMove=false;
            player.pokemon.add(placed); placed.new RemoveFromInventory().firstStep(this); placed.canMove=false;
            Vector2 packPosition=edit.cpy().add(16,-16);
            Tile clear=new Tile("green9",packPosition.cpy());
            clear.routeBelongsTo=map.overworldTiles.get(packPosition).routeBelongsTo;
            map.overworldTiles.put(packPosition,clear);
            player.setItemAmount("log",3);
            new DrawUseTossMenu.DropItem("log",3,packPosition,null).step(this);
            beforeEdits = coop.settledEdits(); stage = 1;
        } else if (stage == 1 && coop.settledEdits() > beforeEdits) {
            com.badlogic.gdx.graphics.Pixmap image=com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
            try { com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("item-packs.png"),image,-1,true); } finally { image.dispose(); }
            mark("OFFER_READY"); stage = 2;
        } else if (stage == 2 && Files.exists(barrier.resolve("a/result.json")) && Files.exists(barrier.resolve("b/result.json"))) {
            if (map.pokemon.values().stream().anyMatch(p -> "LANRACE".equals(p.nickname))) return;
            Tile tile = map.overworldTiles.get(edit);
            if (tile.hasItem != null || tile.hasItemAmount != 0) throw new AssertionError("Contested tile not consumed exactly once");
            saveAt = System.currentTimeMillis(); saveGame(); stage = 3;
        } else if (stage == 3) {
            Path save = Paths.get(map.id + ".sav/map" + map.currMapId + ".json.zip");
            if (!Files.exists(save) || Files.getLastModifiedTime(save).toMillis() < saveAt) return;
            Network.MapSaveData saved = Save.readData(map.id + ".sav/map" + map.currMapId, Network.MapSaveData.class);
            for (Network.TileData tile : saved.mapTiles.tiles) if (tile.pos.equals(edit) && (tile.hasItem != null || tile.hasItemAmount != 0))
                throw new AssertionError("Saved tile differs from committed state");
            for (Network.PokemonData data : saved.overworldPokemon.values()) if ("LANRACE".equals(data.nickname))
                throw new AssertionError("Taken Pokemon survived in host save");
            if(saved.overworldPokemon.values().stream().noneMatch(p -> "LANPEN".equals(p.nickname))) throw new AssertionError("Placed Pokemon missing from saved world");
            boolean storedPack=false;
            for(Network.TileData data:saved.mapTiles.tiles) if(data.pos.equals(edit.cpy().add(16,-16)) && "item_pack".equals(data.tileNameUpper) && data.hasItemAmount==3) storedPack=true;
            if(!storedPack) throw new AssertionError("Unclaimed pack missing from saved world");
            mark("HOST_COMMITTED_SAVE_OK"); stage = 4;
        } else if (stage == 4 && coop.session().peers().size() == 1 && players.isEmpty()) {
            mark("HOST_CLEAN_EXIT"); Gdx.app.exit();
        }
    }
    private int amount(String item) { Integer value = player.getItemAmount(item); return value == null ? 0 : value; }
    private void guestStep() throws Exception {
        if (stage == 0) {
            for (Pokemon candidate : map.pokemon.values()) if ("LANRACE".equals(candidate.nickname)) fixture = candidate;
            if (fixture == null) return;
            if (!coop.worldMatchesAuthority()) throw new AssertionError("Initial guest snapshot differs from installed revisions");
            Vector2 extra = new Vector2(16000,16000);
            map.getInteriorLayer(0).put(extra, new Tile("green9", extra.cpy()));
            try {
                if (coop.worldMatchesAuthority()) throw new AssertionError("Authority comparison ignored an extra local resource");
            } finally { map.getInteriorLayer(0).remove(extra); }
            if (fixture.drawLower == null || fixture.drawUpper == null || fixture.standingAction == null)
                throw new AssertionError("Guest replica is not drawable/interactable");
            if (fixture.previousOwner == null || fixture.previousOwner == player || !"host".equals(fixture.previousOwner.name))
                throw new AssertionError("Remote ownership became local");
            creatureKey = coop.worldCreatureId(fixture);
            initialParty = player.pokemon.size(); initialStones = amount("stone");
            Files.write(Paths.get("ready"), new byte[]{1}); mark("RACE_READY"); stage = 1;
        } else if (stage == 1 && Files.exists(barrier.resolve("go"))) {
            beforeEdits = coop.settledEdits();
            fixture.new AddToInventory(fixture.position.cpy(), null).firstStep(this);
            Tile pack=map.overworldTiles.get(edit);
            if(!"item_pack".equals(pack.nameUpper)) throw new AssertionError("Dropped pack missing on guest");
            ItemPackMenu menu=new ItemPackMenu(edit);
            InputProcessor.aJustPressed=true;
            uiBatch.begin();
            try { menu.step(this); menu.step(this); menu.step(this); } finally { uiBatch.end(); InputProcessor.clearControls(); }
            if(pack.hasItem!=null || !pack.nameUpper.isEmpty()) throw new AssertionError("Claim left pack contents behind");
            Files.write(Paths.get("taken"), new byte[]{1});
            String other = role.equals("a") ? "b" : "a";
            long deadline = System.nanoTime() + 10_000_000_000L;
            while (!Files.exists(barrier.resolve(other + "/taken"))) {
                if (System.nanoTime() > deadline) throw new AssertionError("Simultaneous take barrier failed");
                Thread.sleep(5);
            }
            stage = 2;
        } else if (stage == 2 && coop.settledEdits() > beforeEdits) {
            won = coop.lastEditAccepted();
            if (player.pokemon.size() != initialParty + (won ? 1 : 0) || amount("stone") != initialStones + (won ? 2 : 0))
                throw new AssertionError("Contested pickup/harvest left duplicated private effects");
            if (coop.session().resources().get(creatureKey).json != null) throw new AssertionError("Taken world entity survived");
            int count = player.pokemon.size();
            fixture.new AddToInventory(fixture.position.cpy(), null).firstStep(this);
            if (player.pokemon.size() != count) throw new AssertionError("Stale queued pickup duplicated a Pokemon");
            mark(won ? "RACE_WIN" : "RACE_LOSS");
            player.setItemAmount("log", 20); stageAt = System.nanoTime(); stage = 3;
        } else if (stage == 3 && System.nanoTime() - stageAt > 1_500_000_000L) {
            beforeEdits = coop.settledEdits();
            Tile invalid = new Tile("green9", new Vector2(16000,16000));
            map.getInteriorLayer(0).put(invalid.position.cpy(), invalid);
            player.setItemAmount("log", 15); stage = 4;
        } else if (stage == 4 && coop.settledEdits() > beforeEdits) {
            if (coop.lastEditAccepted() || amount("log") != 20 || map.getInteriorLayer(0).containsKey(new Vector2(16000,16000)))
                throw new AssertionError("Rejected absent-key creation was not fully refunded/removed");
            Map<String,Object> result = new LinkedHashMap<>();
            result.put("won", won); result.put("party", player.pokemon.size()); result.put("stones", amount("stone"));
            result.put("initial_party", initialParty); result.put("initial_stones", initialStones);
            result.put("names", player.pokemon.stream().map(p -> p.nickname).toArray());
            Files.write(Paths.get("result.json"), Json.gson.toJson(result).getBytes("UTF-8"));
            coop.saveGuest(); stage = 5;
            mark("REJECTED_CREATE_ROLLED_BACK");
        } else if (stage == 5 && Files.exists(barrier.resolve("finish"))) {
            Path playerSave;
            try (java.util.stream.Stream<Path> files = Files.list(Paths.get("."))) {
                playerSave = files.filter(p -> p.getFileName().toString().startsWith("coop-player-") && p.toString().endsWith(".json.zip")).findFirst().get();
            }
            Path backup = Paths.get(playerSave.toString() + ".backup");
            Files.move(playerSave, backup); Files.createDirectory(playerSave);
            try {
                coop.saveGuest();
                long deadline = System.nanoTime() + 5_000_000_000L;
                while (coop.problem() == null && System.nanoTime() < deadline) Thread.sleep(5);
                if (coop.problem() == null) throw new AssertionError("Failed private save did not fail closed");
            } finally { Files.delete(playerSave); Files.move(backup, playerSave); }
            mark("SAVE_FAILURE_FAILS_CLOSED");
            mark("PASS_GAMEPLAY_SAFETY"); stage = 6; Gdx.app.exit();
        }
    }
    private void reloadStep() throws Exception {
        if (stage == 0) {
            Tile tile = map.overworldTiles.get(edit);
            if (tile.hasItem != null || tile.hasItemAmount != 0 || map.pokemon.values().stream().anyMatch(p -> "LANRACE".equals(p.nickname)))
                throw new AssertionError("Restarted host restored consumed resources");
            if(map.pokemon.values().stream().noneMatch(p -> "LANPEN".equals(p.nickname))) throw new AssertionError("Placed Pokemon disappeared after world reload");
            Tile pack=map.overworldTiles.get(edit.cpy().add(16,-16));
            if(!"item_pack".equals(pack.nameUpper) || pack.hasItemAmount!=3) throw new AssertionError("Unclaimed pack disappeared after world reload");
            mark("HOST_WORLD_RELOAD_OK"); stage = 1;
        } else if (Files.exists(barrier.resolve("restore-a/restore-done")) && Files.exists(barrier.resolve("restore-b/restore-done"))
                && coop.session().peers().size() == 1 && players.isEmpty()) {
            mark("HOST_RELOAD_CLEAN"); Gdx.app.exit();
        }
    }
    private void restoreStep() throws Exception {
        com.google.gson.JsonObject expected = Json.gson.fromJson(new String(Files.readAllBytes(Paths.get("result.json")), "UTF-8"), com.google.gson.JsonObject.class);
        if (player.pokemon.size() != expected.get("party").getAsInt() || amount("stone") != expected.get("stones").getAsInt() || amount("log") != 20)
            throw new AssertionError("Restored private party/items differ from committed state");
        for (int i = 0; i < player.pokemon.size(); i++) {
            Pokemon member = player.pokemon.get(i);
            if (!member.nickname.equals(expected.getAsJsonArray("names").get(i).getAsString()) || member.previousOwner != player)
                throw new AssertionError("Restored party identity/ownership changed");
        }
        if (player.currPokemon == null || !player.pokemon.contains(player.currPokemon) || !"".equals(player.currFieldMove))
            throw new AssertionError("Restored selection/HM state is invalid");
        Pokemon placed=map.pokemon.values().stream().filter(p -> "LANPEN".equals(p.nickname)).findFirst().orElseThrow(() -> new AssertionError("Placed Pokemon missing after reconnect"));
        map.cacheBounds.set(placed.position.x-32,placed.position.y-32,64,64); map.cacheOnscreenPokemon(this);
        if(!map.onscreenPokemon.contains(placed) || placed.drawLower==null || placed.drawUpper==null) throw new AssertionError("Placed Pokemon not drawable after reconnect");
        Files.write(Paths.get("restore-done"), new byte[]{1}); mark("PRIVATE_RESTORE_OK"); Gdx.app.exit();
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setInitialVisible(false);
        config.setTitle("PokeWilds LAN safety " + System.getProperty("e2e.role"));
        config.setWindowedMode(480,432); config.setForegroundFPS(60); config.setIdleFPS(60); config.disableAudio(true);
        new Lwjgl3Application(new LanSafetyE2E(args), config);
        System.out.println("SAFETY " + System.getProperty("e2e.role") + " EXIT_CLEAN");
    }
}
