package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import java.util.Locale;

/** Battle skin drawing without battle actions or mutable combat state. */
final class BattleWindow {
    private BattleWindow() {}
    static Texture texture(String path) { return TextureCache.get(Gdx.files.internal(path)); }
    static void background(Game game, Sprite sprite) { sprite.draw(game.uiBatch); }
    static Sprite background() {
        Sprite sprite=new Sprite(texture("battle/battle_bg3.png"),0,0,176,160);
        sprite.setPosition(-8,-8); return sprite;
    }
    static void text(Game game,String text,float x,float y,int limit) {
        String value=text.toUpperCase(Locale.ROOT);
        for(int i=0;i<Math.min(limit,value.length());i++) {
            Sprite glyph=game.textDict.get(value.charAt(i));
            if(glyph==null) glyph=game.textDict.get(null);
            if(glyph!=null) game.uiBatch.draw(glyph,x+8*i,y);
        }
    }
    static void health(Game game,LanBattleState.Fighter fighter,boolean friendly) {
        String dir=fighter.generation==Pokemon.Generation.CRYSTAL?"battle/gsc/":"battle/";
        String side=friendly?"friendly":"enemy";
        game.uiBatch.draw(texture(dir+side+"_healthbar1.png"),0,0);
        String name=fighter.name.isEmpty()?fighter.species:fighter.name;
        float nx=friendly?96+(name.length()>5?-16:name.length()>2?-8:0):8;
        text(game,name,nx,friendly?80:136,friendly?9:10);
        String status=status(fighter.status);
        if(status.isEmpty()) {
            String level=String.valueOf(fighter.level);
            if(level.length()<3) game.uiBatch.draw(texture(dir+side+"_levelchar.png"),0,0);
            text(game,level,(friendly?112:48)+(level.length()<3?8:0),friendly?72:128,3);
        } else text(game,status,friendly?112:48,friendly?72:128,3);
        int units=(int)Math.ceil(48f*fighter.hp/fighter.maxHp);
        String health="health1.png";
        if(fighter.generation==Pokemon.Generation.CRYSTAL) health=units>23?health:units>10?"health_yellow.png":"health_red.png";
        Texture hp=texture(dir+health);
        for(int i=0;i<units;i++) game.uiBatch.draw(hp,friendly?96+i:32+i,friendly?67:123,1,2,0,0,1,2,false,false);
        if(friendly) {
            String current=String.valueOf(fighter.hp), max=String.valueOf(fighter.maxHp);
            text(game,current,112-8*Math.min(3,current.length()),56,3);
            text(game,max,144-8*Math.min(3,max.length()),56,3);
        }
    }
    private static String status(String value) {
        if("poison".equals(value)||"toxic".equals(value)) return "PSN";
        if("paralyze".equals(value)) return "PAR";
        if("freeze".equals(value)) return "FRZ";
        if("sleep".equals(value)) return "SLP";
        if("burn".equals(value)) return "BRN";
        return "";
    }
}
