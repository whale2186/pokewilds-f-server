package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/** Keep the render/network loop running while multiplayer saves settle before exit. */
final class DrawExitMenu extends Menu {
    private final DrawPlayerMenu previous;
    private final GameMenuPanel panel = new GameMenuPanel();
    private int cursor=1, frames;
    private boolean saving;
    private String error;
    DrawExitMenu(Game game, DrawPlayerMenu previous) { this.previous=previous; }
    @Override public String getCamera() { return "gui"; }
    @Override public Layer getLayer() { return Layer.gui_140; }
    @Override public void step(Game game) {
        panel.draw(game,"Exit game?",error!=null?error:saving?"Saving...":"Your progress will be saved.",
                saving?new String[]{"Saving..."}:new String[]{"Exit game","Back"},cursor,false);
        if(saving || frames++<2) return;
        if(InputProcessor.upJustPressed || InputProcessor.downJustPressed) cursor=1-cursor;
        int click=panel.clicked();
        if(InputProcessor.bJustPressed || Gdx.input.isKeyJustPressed(Keys.ESCAPE) || click==1) { back(game); return; }
        if(click==0 || InputProcessor.menuConfirmJustPressed()) {
            if(click!=0 && cursor==1) { back(game); return; }
            try {
                if(game.coop!=null) { game.coop.requestExit(); saving=true; cursor=0; }
                else { game.saveGame(); Gdx.app.exit(); }
            } catch(RuntimeException failure) { error="Save failed. Check storage and try again."; }
        }
    }
    private void back(Game game) {
        previous.disabled=false; game.actionStack.remove(this); game.insertAction(previous);
    }
}
