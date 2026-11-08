package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Save;
import java.nio.file.*;
import java.util.Arrays;

/** Exercise the real Exit button, save, title navigation and same-window reconnect. */
public final class MainMenuReturnE2E extends Game {
    private final String mode=System.getProperty("e2e.mode");
    private final long started=System.nanoTime();
    private int phase, returns, titleFrames;
    private DrawExitMenu confirmation;
    private LanSession leaving;
    private String savePath;
    private final String endpoint=System.getProperty("e2e.endpoint");
    MainMenuReturnE2E(String[] args) { super(args,2); }
    static Object field(Object object,String name) throws Exception {
        java.lang.reflect.Field f=object.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(object);
    }
    @Override public void create() {
        super.create(); InputProcessor.acceptInput=false; Game.musicDisabled=true;
        if(mode.equals("single")) {
            actionStack.clear(); insertAction(new InputProcessor());
            server=new com.esotericsoftware.kryonet.Server(); Network.register(server);
            map=new PkmnMap("fixture"); start(); map.loadFromFile(this);
        }
    }
    private TitleMenu title() {
        for(Action action:actionStack) if(action instanceof TitleMenu) return (TitleMenu)action;
        return null;
    }
    private void exitMenu() throws Exception {
        player.setItemAmount("log",73);
        if(coop!=null) leaving=coop.session();
        if(coop!=null && coop.isGuest()) {
            java.lang.reflect.Method path=LanCoop.class.getDeclaredMethod("playerSavePath"); path.setAccessible(true);
            savePath=(String)path.invoke(coop);
        } else savePath=map.id+".sav/game";
        playerCanMove=false;
        confirmation=new DrawExitMenu(this,new DrawPlayerMenu(this,null));
        insertAction(confirmation); phase=1;
    }
    @Override public void render() {
        try {
            super.render(); InputProcessor.clearControls();
            if(System.nanoTime()-started>100_000_000_000L) throw new AssertionError("Menu return deadline mode="+mode+" phase="+phase);
            if(coop!=null && coop.problem()!=null) throw new AssertionError(coop.problem());
            if(phase==0) {
                if(coop!=null && !coop.ready()) return;
                if(map==null) throw new AssertionError("No fixture loaded");
                if(Arrays.asList(new DrawPlayerMenu(this,null).entries).contains("SAVE") != mode.equals("single")) throw new AssertionError("Wrong Save visibility");
                if(mode.equals("guest")) {
                    ServerList list=new ServerList(); list.entries.add(new ServerList.Entry("Fixture",endpoint)); list.save();
                }
                exitMenu();
            } else if(phase==1) {
                if((Integer)field(confirmation,"frames")<3) return;
                InputProcessor.upJustPressed=InputProcessor.aJustPressed=true; phase=2;
            } else if(phase==2) {
                TitleMenu menu=title(); if(menu==null) return;
                if(coop!=null || map!=null || server!=null || client!=null || type!=null || !players.isEmpty() || !battles.isEmpty()) throw new AssertionError("Old session leaked into title");
                if(leaving!=null && leaving.isConnected()) throw new AssertionError("LAN session still connected");
                if(actionStack.size()!=2 || Gdx.input.getInputProcessor()==null) throw new AssertionError("World actions/input remained at title");
                if(mode.equals("guest")) {
                    Network.PlayerData saved=Save.readData(savePath,Network.PlayerData.class);
                    if(saved.itemsDict.get("log")!=73) throw new AssertionError("Guest exit lost inventory");
                } else {
                    Network.GameSaveData saved=Save.readData(savePath,Network.GameSaveData.class);
                    if(saved.playerData.itemsDict.get("log")!=73) throw new AssertionError("Exit lost inventory");
                }
                if(++titleFrames<10) return;
                com.badlogic.gdx.graphics.Pixmap picture=ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
                try { PixmapIO.writePNG(Gdx.files.local("main-menu.png"),picture,-1,true); } finally { picture.dispose(); }
                returns++; titleFrames=0;
                phase=mode.equals("guest") && returns==1?3:5;
            } else if(phase==3) {
                TitleMenu menu=title();
                if(field(menu,"screen").toString().equals("TITLE")) {
                    if((Integer)field(menu,"selected")!=1) InputProcessor.downJustPressed=true;
                    else InputProcessor.aJustPressed=true;
                } else {
                    InputProcessor.aJustPressed=true;
                    if((Integer)field(menu,"selected")==2) phase=4;
                }
            } else if(phase==4) {
                if(coop==null || !coop.ready()) return;
                if(coop.session()==leaving || player.getItemAmount("log")!=73) throw new AssertionError("Same-window reconnect failed");
                exitMenu();
            } else if(phase==5) {
                InputProcessor.aJustPressed=true; phase=6;
            } else if(phase==6) {
                for(Action action:actionStack) if(action instanceof DrawSetupMenu && !action.firstStep) {
                    System.out.println("PASS_MAIN_MENU "+mode+" returns="+returns);
                    phase=7; Gdx.app.exit(); return;
                }
            }
        } catch(Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration(); c.setTitle("Main menu return test");
        c.setInitialVisible(false); c.setWindowedMode(640,576); c.setForegroundFPS(60); c.setIdleFPS(60); c.disableAudio(true);
        new Lwjgl3Application(new MainMenuReturnE2E(args),c);
    }
}
