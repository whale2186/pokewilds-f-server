package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.pkmngen.game.util.TextureCache;
import java.util.List;

/** Shared, window-sized interaction/exit panel with keyboard and pointer rows. */
final class GameMenuPanel {
    private NinePatch frame;
    private final Matrix4 saved = new Matrix4(), projection = new Matrix4();
    private float x, y, width, height, rowTop, canvasWidth, canvasHeight;
    private int firstRow, visibleRows, count;
    private static final float ROW = 30;

    void draw(Game game, String title, String detail, String[] choices, int cursor, boolean locked) {
        LanHudLayout layout = new LanHudLayout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        canvasWidth=layout.width; canvasHeight=layout.height;
        width=Math.min(420,canvasWidth-24);
        visibleRows=Math.max(1,Math.min(choices.length,(int)((canvasHeight-122)/ROW)));
        firstRow=Math.max(0,Math.min(cursor-visibleRows+1,choices.length-visibleRows));
        count=choices.length;
        height=104+visibleRows*ROW; x=(canvasWidth-width)/2; y=(canvasHeight-height)/2;
        rowTop=y+height-74;
        if(frame==null) frame=new NinePatch(new TextureRegion(TextureCache.get(Gdx.files.internal("menu/menu6.png")),80,0,80,96),8,8,8,8);
        saved.set(game.uiBatch.getProjectionMatrix());
        Color batch=new Color(game.uiBatch.getColor()), font=new Color(game.font.getColor());
        float sx=game.font.getData().scaleX, sy=game.font.getData().scaleY;
        projection.setToOrtho2D(0,0,canvasWidth,canvasHeight); game.uiBatch.setProjectionMatrix(projection);
        try {
            game.uiBatch.setColor(Color.WHITE); frame.draw(game.uiBatch,x,y,width,height);
            game.font.getData().setScale(1.2f); game.font.setColor(Color.BLACK);
            game.font.draw(game.uiBatch,fit(game,title,width-32),x+16,y+height-16);
            game.font.setColor(Color.DARK_GRAY);
            List<String> lines=LanHudLayout.wrap(detail==null?"":detail,width-32,t->new GlyphLayout(game.font,t).width);
            if(!lines.isEmpty()) game.font.draw(game.uiBatch,lines.get(0),x+16,y+height-40);
            for(int row=0;row<visibleRows;row++) {
                int i=firstRow+row;
                game.uiBatch.setColor(i==cursor?new Color(.78f,.9f,1,1):Color.WHITE);
                frame.draw(game.uiBatch,x+12,rowTop-(row+1)*ROW,width-24,ROW-2);
                game.font.setColor(locked?Color.GRAY:Color.BLACK);
                game.font.draw(game.uiBatch,fit(game,(i==cursor?"> ":"  ")+choices[i],width-44),x+22,rowTop-row*ROW-8);
            }
            game.font.setColor(Color.DARK_GRAY);
            game.font.draw(game.uiBatch,locked?"Finishing trade...":"C/Enter: Select   X/Esc: Back",x+16,y+19);
        } finally {
            game.font.getData().setScale(sx,sy); game.font.setColor(font);
            game.uiBatch.setColor(batch); game.uiBatch.setProjectionMatrix(saved);
        }
    }
    private String fit(Game game,String value,float max) {
        String text=value==null?"":value;
        if(new GlyphLayout(game.font,text).width<=max) return text;
        while(!text.isEmpty() && new GlyphLayout(game.font,text+"...").width>max) text=text.substring(0,text.length()-1);
        return text+"...";
    }
    int clicked() {
        if(!Gdx.input.justTouched()) return -1;
        float px=Gdx.input.getX()*canvasWidth/Math.max(1,Gdx.graphics.getWidth());
        float py=canvasHeight-Gdx.input.getY()*canvasHeight/Math.max(1,Gdx.graphics.getHeight());
        int row=(int)Math.floor((rowTop-py)/ROW);
        if(px<x+12 || px>x+width-12 || py>rowTop || row<0 || row>=visibleRows) return -1;
        int index=firstRow+row;
        return index<count?index:-1;
    }
}
