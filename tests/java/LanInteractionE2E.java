package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import java.lang.reflect.Field;

/** Two real render loops exercise invitations, selections, trade ownership and battle turns. */
public final class LanInteractionE2E extends Game {
    private final boolean host = Boolean.getBoolean("e2e.host");
    private final long launched = System.nanoTime();
    private int phase;
    private boolean setup;
    private long lastClick, finished;
    private Pokemon original;
    private Pokemon traded;
    private long dropEdits;
    private int partyHp;
    private final java.nio.file.Path barrier=java.nio.file.Paths.get(System.getProperty("e2e.barrier"));
    private String role() { return host?"battler":"viewer"; }
    private void mark(String name) throws Exception { java.nio.file.Files.write(barrier.resolve(role()+"-"+name),new byte[]{1}); }
    private boolean both(String name) { return java.nio.file.Files.exists(barrier.resolve("battler-"+name)) && java.nio.file.Files.exists(barrier.resolve("viewer-"+name)); }
    private String initialSpecies;
    private String lastDialog="";
    private long busyUntil;
    private boolean sawBusy;
    LanInteractionE2E(String[] args) { super(args, 2); }
    static Object field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(object);
    }
    @Override public void create() { super.create(); InputProcessor.acceptInput = false; Game.musicDisabled = true; }
    @Override public void render() {
        try {
            super.render(); InputProcessor.aJustPressed = false;
            if (System.nanoTime() - launched > 90_000_000_000L) throw new AssertionError("Interaction timeout phase=" + phase);
            if (coop.problem() != null) throw new AssertionError(coop.problem());
            if (!coop.ready()) return;
            if (Boolean.getBoolean("e2e.restore")) {
                Pokemon received=player.pokemon.stream().filter(p -> p.nickname.equals(host?"SWAPCHAR":"SWAPPIKA")).findFirst().orElseThrow(() -> new AssertionError("Traded Pokemon missing after reconnect"));
                if(player.pokemon.size()!=2 || received.previousOwner!=player || !coop.identity().equals(received.lanOwnerId)) throw new AssertionError("Trade ownership/save not restored");
                System.out.println("PASS trade restore");Gdx.app.exit();return;
            }
            if (!setup) {
                player.pokemon.clear(); original = new Pokemon(host ? "pikachu" : "charmander", 20, Pokemon.Generation.CRYSTAL);
                original.attacks = new String[]{"tackle", "tackle", "tackle", "tackle"};
                original.nickname=host?"SWAPPIKA":"SWAPCHAR";original.previousOwner=player;
                player.pokemon.add(original);player.pokemon.add(new Pokemon("machop",6)); player.currPokemon = original; initialSpecies = original.specie.name;
                player.position.set(coop.sharedSpawn()).add(host ? 0 : 16, 0); player.dirFacing = host ? "right" : "left";
                setup = true;
                if(!host) busyUntil=System.nanoTime()+1_500_000_000L;
            }
            if(!host && busyUntil>0) {
                if(System.nanoTime()<busyUntil) { playerCanMove=false;return; }
                busyUntil=0;playerCanMove=true;
            }
            if(phase==10) {
                if(!both("trade")) return;
                if(!coop.identity().equals(traded.lanOwnerId) || traded.previousOwner!=player) throw new AssertionError("Trade retained sender ownership");
                com.badlogic.gdx.math.Vector2 drop=null;
                for(com.badlogic.gdx.math.Vector2 d:new com.badlogic.gdx.math.Vector2[]{new com.badlogic.gdx.math.Vector2(0,-16),new com.badlogic.gdx.math.Vector2(0,16),new com.badlogic.gdx.math.Vector2(host?-16:16,0)}) {
                    com.badlogic.gdx.math.Vector2 pos=player.position.cpy().add(d);Tile tile=map.tiles.get(pos);
                    if(tile!=null && !tile.isSolid && !tile.isWater && !tile.isLedge && map.pokemonAt(pos)==null) { drop=pos;break; }
                }
                if(drop==null) throw new AssertionError("No placement fixture tile");
                dropEdits=coop.settledEdits();traded.position.set(drop);traded.mapTiles=map.tiles;
                traded.new RemoveFromInventory().firstStep(this);playerCanMove=true;phase=11;return;
            }
            if(phase==11) {
                Pokemon placed=map.pokemon.values().stream().filter(p -> p.nickname.equals(traded.nickname)).findFirst().orElse(null);
                if(placed==null || coop.settledEdits()<=dropEdits) return;
                String key=coop.worldCreatureId(placed);
                if(key==null || coop.session().resource(key)==null || coop.session().resource(key).json==null) return;
                map.cacheOnscreenPokemon(this);
                if(!map.onscreenPokemon.contains(placed) || placed.drawLower==null || placed.drawUpper==null) throw new AssertionError("Traded Pokemon disappeared after dropping");
                mark("drop");traded=placed;phase=12;return;
            }
            if(phase==12) {
                if(!both("drop")) return;
                boolean peerPlaced=map.pokemon.values().stream().anyMatch(p -> p.nickname.equals(host?"SWAPPIKA":"SWAPCHAR"));
                if(!peerPlaced) return;
                dropEdits=coop.settledEdits();traded.new AddToInventory(traded.position.cpy(),null).firstStep(this);playerCanMove=true;phase=13;return;
            }
            if(phase==13) {
                if(coop.settledEdits()<=dropEdits || !player.pokemon.contains(traded)) return;
                player.pokemon.remove(traded);player.pokemon.add(0,traded);player.currPokemon=traded;original=traded;partyHp=traded.currentStats.get("hp");
                mark("pickup");phase=1;return;
            }
            Object interactions = field(coop, "interactions");
            Object dialog = field(interactions, "dialog");
            if (phase == 0 && player.pokemon.get(0) == original && dialog == null && host && coop.session().peers().size() >= 2 && !players.isEmpty()) {
                if (coop.interactWithPlayer()) System.out.println("E2E OPEN_INTERACTION");
                return;
            }
            if (dialog != null) {
                if ((Integer)field(dialog, "frames") < 3 || System.nanoTime() - lastClick < 100_000_000L) return;
                String[] choices = (String[])field(dialog, "choices");
                String state=(String)field(interactions,"status");
                if(state.startsWith("Player is busy")) sawBusy=true;
                String trace=java.util.Arrays.toString(choices)+" "+state;
                if(!trace.equals(lastDialog)) { System.out.println("E2E DIALOG "+trace); lastDialog=trace; }
                if(choices.length==5 && !Gdx.files.local("player-battle.png").exists()) {
                    com.badlogic.gdx.graphics.Pixmap image=com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
                    try { com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("player-battle.png"),image,-1,true); } finally { image.dispose(); }
                }
                int choice = 0;
                if (choices[0].equals("Battle")) choice = phase == 0 ? 1 : 0;
                else if (choices[0].startsWith("Waiting") || choices[0].equals("Cancel") || choices[0].equals("Forfeit")) return;
                else if (choices[0].equals("Close") && phase>0 && phase<10) phase = 3;
                Field cursor = dialog.getClass().getDeclaredField("cursor"); cursor.setAccessible(true); cursor.setInt(dialog, choice);
                InputProcessor.aJustPressed = true; lastClick = System.nanoTime();
                return;
            }
            if (phase == 0 && player.pokemon.get(0) != original) {
                if(host && !sawBusy) throw new AssertionError("Busy invite had no feedback");
                if (player.pokemon.size() != 2 || player.pokemon.get(0).specie.name.equals(initialSpecies)) throw new AssertionError("Trade did not exchange slots");
                if (player.pokemon.get(0).lanOwnerId == null) throw new AssertionError("Ownership missing");
                original = player.pokemon.get(0);traded=original;mark("trade");phase=10;
                System.out.println("E2E TRADE_COMPLETE " + original.specie.name);
            }
            if (phase == 1 && host && both("pickup")) {
                if (coop.interactWithPlayer()) phase = 2;
            }
            if (phase == 3) {
                if (player.pokemon.get(0).currentStats.get("hp")!=partyHp) throw new AssertionError("PvP damaged the real party");
                if (player.pokemon.get(0) != original) throw new AssertionError("PvP modified party identity");
                if (finished == 0) { finished = System.nanoTime(); System.out.println("PASS trade and PvP"); }
                if (System.nanoTime() - finished > 1_000_000_000L) Gdx.app.exit();
            }
        } catch (Throwable error) { error.printStackTrace(); System.exit(1); }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setInitialVisible(false); config.setWindowedMode(480, 432); config.setForegroundFPS(60); config.setIdleFPS(60); config.disableAudio(true);
        new Lwjgl3Application(new LanInteractionE2E(args), config);
    }
}
