package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

/** Claim uses the same revision-checked tile/inventory transaction as other rewards. */
final class ItemPackMenu extends Menu {
    private final Vector2 position;
    private final GameMenuPanel panel = new GameMenuPanel();
    private int cursor, frames;
    ItemPackMenu(Vector2 position) { this.position=position.cpy(); }
    @Override public String getCamera() { return "gui"; }
    @Override public Layer getLayer() { return Layer.gui_140; }
    @Override public void step(Game game) {
        Tile tile=game.map.tiles.get(position);
        if(tile==null || !"item_pack".equals(tile.nameUpper) || tile.hasItem==null) { close(game); return; }
        panel.draw(game,"Item pack",tile.hasItem+" x"+tile.hasItemAmount,new String[]{"Claim","Exit"},cursor,false);
        if(frames++<2) return;
        if(InputProcessor.upJustPressed || InputProcessor.downJustPressed) cursor=1-cursor;
        int click=panel.clicked();
        if(InputProcessor.bJustPressed || Gdx.input.isKeyJustPressed(Keys.ESCAPE) || click==1) { close(game); return; }
        if(click==0 || InputProcessor.menuConfirmJustPressed()) {
            if(click!=0 && cursor==1) { close(game); return; }
            tile.pickUpItem(game.player); game.map.refreshCache=true;
            game.insertAction(new PlayMusic("click1",null)); close(game);
        }
    }
    private void close(Game game) { game.actionStack.remove(this); game.playerCanMove=true; }
}
