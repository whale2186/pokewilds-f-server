package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import java.util.List;

/** Normal battle assets and coordinates, rendered from the private PvP combatants. */
final class LanBattleScreen {
    private final Game game;
    private final Matrix4 saved=new Matrix4(),projection=new Matrix4();
    private Sprite background,arrow;
    private Texture pixel;
    private int previousOwn=-1,previousEnemy=-1;
    private long ownHit,enemyHit;
    LanBattleScreen(Game game) { this.game=game; }
    private void rect(float x,float y,float w,float h,Color color) {
        if(pixel==null) { Pixmap p=new Pixmap(1,1,Pixmap.Format.RGBA8888);p.setColor(Color.WHITE);p.fill();pixel=new Texture(p);p.dispose(); }
        game.uiBatch.setColor(color); game.uiBatch.draw(pixel,x,y,w,h); game.uiBatch.setColor(Color.WHITE);
    }
    void draw(Pokemon own,Pokemon enemy,String trainer,String status,String[] choices,int cursor) {
        float scale=Math.min(Gdx.graphics.getWidth()/160f,Gdx.graphics.getHeight()/144f);
        float w=Gdx.graphics.getWidth()/scale,h=Gdx.graphics.getHeight()/scale,ox=(w-160)/2,oy=(h-144)/2;
        saved.set(game.uiBatch.getProjectionMatrix()); projection.setToOrtho2D(-ox,-oy,w,h);
        Color batch=new Color(game.uiBatch.getColor()),font=new Color(game.font.getColor());
        float sx=game.font.getData().scaleX,sy=game.font.getData().scaleY;
        game.uiBatch.setProjectionMatrix(projection);
        try {
            rect(-ox,-oy,w,h,new Color(.06f,.07f,.1f,1));
            if(background==null) background=BattleWindow.background();
            BattleWindow.background(game,background);
            long now=System.nanoTime();
            int hp=own.currentStats.get("hp"),enemyHp=enemy.currentStats.get("hp");
            if(previousOwn>=0 && hp<previousOwn) ownHit=now;
            if(previousEnemy>=0 && enemyHp<previousEnemy) enemyHit=now;
            previousOwn=hp;previousEnemy=enemyHp;
            Sprite front=new Sprite(enemy.sprite),back=new Sprite(own.backSprite);
            front.setPosition(front.getWidth()<=48?102:96,88);back.setPosition(22,62);
            if((now-enemyHit)/65_000_000L<6 && (now-enemyHit)/65_000_000L%2==0) front.setAlpha(.2f);
            if((now-ownHit)/65_000_000L<6 && (now-ownHit)/65_000_000L%2==0) back.setAlpha(.2f);
            front.draw(game.uiBatch);back.draw(game.uiBatch);
            BattleWindow.health(game,LanBattleState.Fighter.of(enemy),false);
            BattleWindow.health(game,LanBattleState.Fighter.of(own),true);
            if(choices.length==5) {
                game.uiBatch.draw(BattleWindow.texture("menu/attack_screen1.png"),0,0);
                for(int i=0;i<4;i++) BattleWindow.text(game,choices[i],48,32-8*i,13);
                if(arrow==null) arrow=new Sprite(BattleWindow.texture("battle/arrow_right1.png"),0,0,5,7);
                game.uiBatch.draw(arrow,cursor==4?0:41,cursor==4?8:32-8*cursor);
                BattleWindow.text(game,"EXIT",8,8,4);
                Attack move=cursor<4?game.battle.attacks.get(choices[cursor].toLowerCase(java.util.Locale.ROOT)):null;
                if(move!=null) {
                    BattleWindow.text(game,move.type,16,56,11);
                    BattleWindow.text(game,move.category.name(),16,48,11);
                }
            } else {
                game.uiBatch.draw(BattleWindow.texture("textbox_bg1.png"),0,0);
                List<String> text=LanHudLayout.wrap(status==null?"":status,144,t->t.length()*8);
                for(int i=0;i<Math.min(2,text.size());i++) BattleWindow.text(game,text.get(i),8,32-12*i,18);
                rect(104,1,55,16,new Color(.82f,.9f,1,1));
                game.font.getData().setScale(.7f);game.font.setColor(Color.BLACK);
                game.font.draw(game.uiBatch,choices[0].equals("Continue")?"CONTINUE":"EXIT",109,12);
            }
        } finally {
            game.uiBatch.setColor(batch);game.font.getData().setScale(sx,sy);game.font.setColor(font);
            game.uiBatch.setProjectionMatrix(saved);
        }
    }
    int clicked(int count) {
        if(!Gdx.input.justTouched()) return -1;
        float scale=Math.min(Gdx.graphics.getWidth()/160f,Gdx.graphics.getHeight()/144f);
        float x=(Gdx.input.getX()-(Gdx.graphics.getWidth()-160*scale)/2)/scale;
        float y=(Gdx.graphics.getHeight()-Gdx.input.getY()-(Gdx.graphics.getHeight()-144*scale)/2)/scale;
        if(count==5) {
            if(x>=40 && x<=160 && y>=4 && y<40) return Math.min(3,(int)((40-y)/8));
            if(x>=0 && x<40 && y>=0 && y<=18) return 4;
        } else if(x>=104 && x<=160 && y>=0 && y<=18) return 0;
        return -1;
    }
    void reset() { previousOwn=previousEnemy=-1;ownHit=enemyHit=0; }
    void dispose() { if(pixel!=null) { pixel.dispose();pixel=null; } }
}
