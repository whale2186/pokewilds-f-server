package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.ScreenUtils;

public final class LanAppearanceE2E extends Game {
    private final boolean writer=Boolean.getBoolean("e2e.writer");
    private int frames,phase;
    private double night;
    private final long started=System.nanoTime();
    LanAppearanceE2E(String[] args) { super(args,2); }
    private void verify() {
        if(!player.name.equals("PersistentGuest") || !player.character.equals("kris")
                || Color.rgba8888(player.color)!=0x34bc67ff || Color.rgba8888(player.skinColor)!=0xe6bd92ff)
            throw new AssertionError("Name/character/colors did not persist: "+player.name+" "+player.character+" "+player.color);
    }
    @Override public void create() {
        super.create();Game.musicDisabled=true;InputProcessor.acceptInput=false;
        if(writer) {
            player.name="PersistentGuest";player.character="kris";player.skinColor=new Color(0xe6bd92ff);player.setColor(new Color(0x34bc67ff));
            rememberPlayerProfile("Temporary");
            actionStack.removeIf(a->a instanceof DrawSetupMenu);insertAction(new DrawSetupMenu(this,null));DrawSetupMenu.currIndex=4;
        }
    }
    private double picture(String name) {
        Pixmap p=ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        try {
            PixmapIO.writePNG(Gdx.files.local(name),p,-1,true);double total=0;long count=0;
            for(int y=p.getHeight()/5;y<p.getHeight()*4/5;y+=3) for(int x=p.getWidth()/5;x<p.getWidth()*4/5;x+=3) {
                int rgba=p.getPixel(x,y);total+=.2126*((rgba>>>24)&255)+.7152*((rgba>>>16)&255)+.0722*((rgba>>>8)&255);count++;
            }
            return total/count;
        } finally { p.dispose(); }
    }
    @Override public void render() {
        try {
            super.render();InputProcessor.clearControls();
            if(System.nanoTime()-started>100_000_000_000L) throw new AssertionError("Appearance deadline");
            if(writer) {
                if(++frames==2) {
                    com.badlogic.gdx.InputProcessor input=Gdx.input.getInputProcessor();
                    for(int i=0;i<9;i++) input.keyTyped('\b');
                    for(char c:"PersistentGuest".toCharArray()) input.keyTyped(c);
                }
                if(frames>=10) { verify();picture("saved-appearance.png");System.out.println("PROFILE_WRITTEN");Gdx.app.exit(); }return; }
            if(coop!=null && coop.problem()!=null) throw new AssertionError(coop.problem());
            if(coop==null || !coop.ready()) return;
            verify();
            if(player.currSprite.getTexture()!=player.standingSprites.get(player.dirFacing).getTexture()) throw new AssertionError("Appearance restored to fields but not the visible avatar");
            if(phase==0 && map.timeOfDay.equals("night")) {
                if(++frames<30) return;
                night=picture("night.png");System.out.println("NIGHT_READY "+night);phase=1;frames=0;
            } else if(phase==1 && map.timeOfDay.equals("day")) {
                if(++frames<30) return;
                double day=picture("day.png");
                if(night>=day*.72) throw new AssertionError("Night is not visibly darker: "+night+" / "+day);
                System.out.println("PASS appearance cold restart, LAN default name and night/day brightness "+night+" / "+day);phase=2;Gdx.app.exit();
            }
        } catch(Throwable failure) { failure.printStackTrace();System.exit(1); }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setInitialVisible(false);c.setWindowedMode(640,576);c.setForegroundFPS(60);c.setIdleFPS(60);c.disableAudio(true);
        new Lwjgl3Application(new LanAppearanceE2E(args),c);
    }
}
