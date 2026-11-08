package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;

/** Claim uses the same revision-checked tile/inventory transaction as other rewards. */
final class ItemPackMenu extends Menu {
    private static com.badlogic.gdx.graphics.Texture icon;
    static com.pkmngen.game.util.SmolSprite icon() {
        if(icon==null) {
            // Reuse the bag glyph without its opaque menu cell/background.
            com.badlogic.gdx.graphics.Pixmap source=new com.badlogic.gdx.graphics.Pixmap(Gdx.files.internal("menu/bag-sheet1.png"));
            com.badlogic.gdx.graphics.Pixmap crop=new com.badlogic.gdx.graphics.Pixmap(20,20,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            try {
                crop.setBlending(com.badlogic.gdx.graphics.Pixmap.Blending.None);
                crop.drawPixmap(source,0,0,10,29,20,20);
                boolean[] seen=new boolean[400]; int[] queue=new int[400]; int head=0,tail=0;
                for(int i=0;i<400;i++) if((i%20==0 || i%20==19 || i/20==0 || i/20==19) && crop.getPixel(i%20,i/20)==-1) {
                    seen[i]=true; queue[tail++]=i;
                }
                while(head<tail) {
                    int cell=queue[head++], x=cell%20,y=cell/20; crop.drawPixel(x,y,0);
                    for(int next:new int[]{x>0?cell-1:-1,x<19?cell+1:-1,y>0?cell-20:-1,y<19?cell+20:-1})
                        if(next>=0 && !seen[next] && crop.getPixel(next%20,next/20)==-1) { seen[next]=true; queue[tail++]=next; }
                }
                icon=com.pkmngen.game.util.TextureCache.get(crop);
            } finally { source.dispose(); crop.dispose(); }
        }
        return new com.pkmngen.game.util.SmolSprite(icon);
    }
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
