package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.multiplayer.LanSession.Pose;
import com.pkmngen.game.util.Json;
import java.nio.file.*;
import java.util.*;

/** Two actual clients, one headless authority; viewer never enters the local battle model. */
public final class LanSpectatorE2E extends Game {
    private final boolean source=Boolean.getBoolean("e2e.source");
    private final Path barrier=Paths.get(System.getProperty("e2e.barrier"));
    private final long started=System.nanoTime();
    private int phase;
    private String target;
    private java.util.List<Pokemon> party;
    private Map<String,Integer> inventory;
    private Pokemon selected;
    private Battle ownBattle;
    private long settled;
    private Object interaction;
    private int menuPhase;
    private DrawPlayerMenu startMenu;
    LanSpectatorE2E(String[] args) { super(args,2); }
    static Object get(Object obj,String name) throws Exception { java.lang.reflect.Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj); }
    static void set(Object obj,String name,Object value) throws Exception { java.lang.reflect.Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,value); }
    private void mark(String name) throws Exception { Files.write(barrier.resolve(name),new byte[]{1});System.out.println("SPECTATOR "+name); }
    private boolean exists(String name) { return Files.exists(barrier.resolve(name)); }
    @Override public void create() { super.create(); InputProcessor.acceptInput=false; Game.musicDisabled=true; }
    @Override public void render() {
        try {
            super.render();
            InputProcessor.clearControls();
            if(System.nanoTime()-started>150_000_000_000L) throw new AssertionError("Spectator deadline phase="+phase);
            if(coop!=null && coop.problem()!=null) throw new AssertionError(coop.problem());
            if(!source && phase==8) {
                if(coop==null && map==null && actionStack.stream().anyMatch(a -> a instanceof TitleMenu)) {
                    mark("PASS-viewer"); phase=9; Gdx.app.exit();
                }
                return;
            }
            if(coop==null || !coop.ready()) return;
            if(source) sourceStep(); else viewerStep();
        } catch(Throwable failure) { failure.printStackTrace();System.exit(1); }
    }
    private void sourceStep() throws Exception {
        if(phase==0) {
            player.position.set(coop.sharedSpawn());
            player.pokemon.clear(); player.currPokemon=new Pokemon("pikachu",20,Pokemon.Generation.CRYSTAL); player.pokemon.add(player.currPokemon);
            battle.oppPokemon=new Pokemon("rattata",15,Pokemon.Generation.CRYSTAL);
            battle.oppPokemon.position.set(player.position); battle.oppPokemon.mapTiles=map.overworldTiles;
            playerCanMove=false; insertAction(Battle.getIntroAction(this)); phase=1; mark("source-ready");
        } else if(phase==1 && exists("watching-wild")) {
            player.currPokemon.currentStats.put("hp",player.currPokemon.currentStats.get("hp")-7);
            battle.oppPokemon.currentStats.put("hp",battle.oppPokemon.currentStats.get("hp")-5); battle.turnNumber=2;
            insertAction(new DisplayText(this,"PIKACHU used THUNDERSHOCK!",null,null,null));
            Files.write(barrier.resolve("wild-expected.json"),Json.gson.toJson(coop.spectator.capture()).getBytes("UTF-8")); phase=2;
        } else if(phase==2 && exists("wild-updated")) {
            insertAction(new BattleFadeOut(this,new SetField(this,"playerCanMove",true,null))); phase=3;
        } else if(phase==3 && exists("wild-ended")) {
            interaction=get(coop,"interactions");
            set(interaction,"kind","battle"); set(interaction,"accepted",true); set(interaction,"token",UUID.randomUUID().toString());
            set(interaction,"offered",new Pokemon("pikachu",20,Pokemon.Generation.CRYSTAL));
            set(interaction,"opponent",new Pokemon("charmander",18,Pokemon.Generation.CRYSTAL));
            set(interaction,"status","Choosing a move"); phase=4; mark("pvp-ready");
        } else if(phase==4 && exists("watching-pvp")) {
            Pokemon mine=(Pokemon)get(interaction,"offered"), enemy=(Pokemon)get(interaction,"opponent");
            mine.currentStats.put("hp",mine.currentStats.get("hp")-4); enemy.currentStats.put("hp",enemy.currentStats.get("hp")-2);
            set(interaction,"turn",1);set(interaction,"status","PIKACHU used TACKLE!");
            Files.write(barrier.resolve("pvp-expected.json"),Json.gson.toJson(coop.spectator.capture()).getBytes("UTF-8"));phase=5;
        } else if(phase==5 && exists("pvp-updated")) { mark("source-disconnecting");Gdx.app.exit(); }
    }
    private void unchanged() {
        if(!party.equals(player.pokemon) || !inventory.equals(player.getItemsDict()) || selected!=player.currPokemon || ownBattle!=battle
                || battle.drawAction!=null || coop.settledEdits()!=settled) throw new AssertionError("Spectating changed local battle/party/inventory/world edits");
    }
    private void picture(String name) {
        com.badlogic.gdx.graphics.Pixmap p=ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        try { PixmapIO.writePNG(Gdx.files.local(name),p,-1,true); } finally { p.dispose(); }
    }
    private boolean received(String expected) throws Exception {
        if(!exists(expected)) return false;
        LanBattleState actual=(LanBattleState)get(coop.spectator,"state");
        LanBattleState want=Json.gson.fromJson(new String(Files.readAllBytes(barrier.resolve(expected)),"UTF-8"),LanBattleState.class);
        return actual!=null && actual.friendly.hp==want.friendly.hp && actual.enemy.hp==want.enemy.hp && actual.turn==want.turn;
    }
    private void checkCreatureVisibility() {
        Map<com.badlogic.gdx.math.Vector2,Pokemon> saved=new HashMap<>(map.pokemon);
        com.badlogic.gdx.math.Rectangle bounds=new com.badlogic.gdx.math.Rectangle(map.cacheBounds);
        com.badlogic.gdx.math.Vector2 pos=coop.sharedSpawn();
        Pokemon first=new Pokemon("machop",6), second=new Pokemon("pikachu",6), upstairs=new Pokemon("rattata",6);
        first.mapTiles=second.mapTiles=map.tiles; upstairs.mapTiles=map.getInteriorLayer(0);
        first.position.set(pos); second.position.set(pos); upstairs.position.set(pos);
        try {
            map.indexPokemon(pos,first); map.indexPokemon(pos,second); map.indexPokemon(pos,upstairs);
            map.cacheBounds.set(pos.x-32,pos.y-32,64,64); map.cacheOnscreenPokemon(this);
            if(!map.onscreenPokemon.contains(first) || !map.onscreenPokemon.contains(second) || map.onscreenPokemon.contains(upstairs)
                    || map.pokemonAt(pos)==null || map.pokemonAt(pos).mapTiles!=map.tiles) throw new AssertionError("Overlapping/layered creatures became invisible");
            map.cacheBounds.set(pos.x+2000,pos.y+2000,64,64); map.cacheOnscreenPokemon(this);
            if(map.onscreenPokemon.contains(first)) throw new AssertionError("Far creature left in render cache");
            map.cacheBounds.set(pos.x-32,pos.y-32,64,64); map.cacheOnscreenPokemon(this);
            if(!map.onscreenPokemon.contains(first) || !map.onscreenPokemon.contains(second)) throw new AssertionError("Creature missing after long-distance return");
            map.removePokemon(first);
            if(map.pokemon.containsValue(first) || !map.pokemon.containsValue(second) || !map.pokemon.containsValue(upstairs)) throw new AssertionError("Pickup removed another creature at same coordinates");
        } finally {
            map.pokemon.clear(); map.pokemon.putAll(saved); map.cacheBounds.set(bounds); map.cacheOnscreenPokemon(this);
        }
    }
    private void viewerStep() throws Exception {
        if(menuPhase==0) {
            checkCreatureVisibility();
            startMenu=new DrawPlayerMenu(this,new SetField(this,"playerCanMove",true,null));
            if(Arrays.asList(startMenu.entries).contains("SAVE")) throw new AssertionError("Guest menu exposes SAVE");
            playerCanMove=false; insertAction(startMenu); menuPhase=1; return;
        }
        if(menuPhase==1) {
            if(startMenu.cursorDelay<3) { startMenu.cursorDelay=3; return; }
            picture("multiplayer-start-menu.png");
            if(!startMenu.entries[startMenu.currIndex].equals("EXIT")) { InputProcessor.downJustPressed=true; return; }
            InputProcessor.aJustPressed=true; menuPhase=2; return;
        }
        if(menuPhase==2) {
            DrawExitMenu confirm=null;
            for(Action action:actionStack) if(action instanceof DrawExitMenu) confirm=(DrawExitMenu)action;
            if(confirm==null || (Integer)get(confirm,"frames")<3) return;
            picture("exit-confirmation.png"); InputProcessor.bJustPressed=true; menuPhase=3; return;
        }
        if(menuPhase==3) {
            if(!actionStack.contains(startMenu)) return;
            if(startMenu.disabled) throw new AssertionError("Back left start menu disabled");
            InputProcessor.bJustPressed=true; menuPhase=4; return;
        }
        if(menuPhase==4) {
            if(actionStack.contains(startMenu) || !playerCanMove) return;
            menuPhase=5;
        }
        if(phase==0) {
            player.position.set(coop.sharedSpawn()).add(16,0); player.dirFacing="left";
            party=new ArrayList<>(player.pokemon);inventory=new HashMap<>(player.getItemsDict());selected=player.currPokemon;ownBattle=battle;settled=coop.settledEdits();phase=1;
        } else if(phase==1) {
            for(Pose peer:coop.session().peers().values()) if(peer.name.equals("battler") && LanBattleState.readVisual(peer.visual)!=null) target=peer.id;
            if(target==null || !coop.peerBattling(target)) return;
            picture("battle-icon.png");
            if(!coop.interactWithPlayer()) throw new AssertionError("Cannot interact with battling player");
            Object dialog=get(get(coop,"interactions"),"dialog");
            if(!((String[])get(dialog,"choices"))[0].equals("Spectate")) throw new AssertionError("Spectate choice missing");
            phase=2;
        } else if(phase==2) {
            Object dialog=get(get(coop,"interactions"),"dialog");
            if(dialog!=null && (Integer)get(dialog,"frames")>=3) { picture("interaction-menu.png"); InputProcessor.aJustPressed=true; }
            if(!coop.spectator.watching()) return;
            InputProcessor.aPressed=InputProcessor.leftPressed=true;new InputProcessor().step(this);
            if(InputProcessor.aPressed || InputProcessor.leftPressed) throw new AssertionError("Spectator input leaked");
            unchanged();mark("watching-wild");phase=3;
        } else if(phase==3 && received("wild-expected.json")) {
            unchanged();picture("wild-spectate.png");
            coop.spectator.stop(null);unchanged();
            if(coop.spectator.watching() || !coop.spectator.start(target)) throw new AssertionError("Leave/rejoin failed");
            mark("wild-updated");phase=4;
        } else if(phase==4 && !coop.spectator.watching() && !coop.peerBattling(target)) {
            unchanged();mark("wild-ended");phase=5;
        } else if(phase==5) {
            Pose peer=coop.session().peers().get(target); LanBattleState state=peer==null?null:LanBattleState.readVisual(peer.visual);
            if(state==null || !state.mode.equals("pvp")) return;
            if(!coop.spectator.start(target)) return;
            mark("watching-pvp");phase=6;
        } else if(phase==6 && received("pvp-expected.json")) {
            unchanged();picture("pvp-spectate.png");mark("pvp-updated");phase=7;
        } else if(phase==7 && !coop.session().peers().containsKey(target) && !coop.spectator.watching()) {
            unchanged();coop.requestExit();phase=8;
        }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("Spectator test");c.setInitialVisible(false);c.setWindowedMode(640,576);c.setForegroundFPS(60);c.setIdleFPS(60);c.disableAudio(true);
        new Lwjgl3Application(new LanSpectatorE2E(args),c);
    }
}
