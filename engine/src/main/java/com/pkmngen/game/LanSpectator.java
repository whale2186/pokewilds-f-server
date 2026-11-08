package com.pkmngen.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.pkmngen.game.multiplayer.LanSession.Pose;
import java.util.*;

/** Independent read-only rendering. Never enters Game.battle or installs battle actions. */
final class LanSpectator {
    private final Game game;
    private final LanCoop coop;
    private Object publishedBattle;
    private String publishedId;
    private String target, battleId;
    private Pose previousPose;
    private LanBattleState state;
    private boolean releasing;
    private Texture pixel;
    private Pokemon ownView, enemyView;
    private String lastText;
    private java.util.List<String> narration=Collections.emptyList();
    private final Matrix4 projection=new Matrix4(), previousProjection=new Matrix4();
    LanSpectator(Game game,LanCoop coop) { this.game=game; this.coop=coop; }
    boolean watching() { return target!=null; }
    boolean blocksInput() { return watching()||releasing; }
    LanBattleState capture() {
        LanBattleState interaction=coop.interactionBattle();
        if (interaction!=null) return interaction;
        if (game.battle==null || game.battle.drawAction==null) { publishedBattle=null; return null; }
        if (publishedBattle!=game.battle.drawAction) {
            publishedBattle=game.battle.drawAction; publishedId=UUID.randomUUID().toString();
        }
        String text="Choosing an action";
        for(Action action:game.actionStack) if(action instanceof DisplayText) { text=((DisplayText)action).sourceText; break; }
        boolean trainer=game.player.pokemon.isEmpty() || game.player.currPokemon==null;
        return LanBattleState.of(publishedId,trainer?"safari":"wild",game.battle.turnNumber,trainer?null:game.player.currPokemon,game.battle.oppPokemon,text);
    }
    static boolean nearby(Pose peer,String map,int layer,float x,float y) {
        return peer!=null && peer.layer==layer && map.equals(peer.mapId)
                && Math.abs(peer.x-x)<=64 && Math.abs(peer.y-y)<=64;
    }
    boolean start(String peerId) {
        if(peerId==null || !coop.ready() || watching() || !game.playerCanMove || coop.hud.typing()
                || coop.privateEditPending() || coop.interactionBusy() || capture()!=null || peerId.equals(coop.session().id())) return false;
        Pose peer=coop.session().peers().get(peerId);
        int layer=game.map.tiles==game.map.overworldTiles?-1:game.map.interiorTilesIndex;
        if(!nearby(peer,game.map.currMapId,layer,game.player.position.x,game.player.position.y)) return false;
        LanBattleState next=LanBattleState.readVisual(peer.visual);
        if(next==null) return false;
        target=peerId; battleId=next.id; state=next; previousPose=peer;
        InputProcessor.clearControls(); return true;
    }
    boolean startNearby() {
        java.util.List<Pose> peers=new ArrayList<>(coop.session().peers().values());
        peers.sort(Comparator.comparingDouble(p->game.player.position.dst2(p.x,p.y)));
        for(Pose peer:peers) if(start(peer.id)) return true;
        return false;
    }
    static boolean sameBattle(String id,LanBattleState state) { return state!=null && id.equals(state.id); }
    void update() {
        if(releasing && !Gdx.input.isKeyPressed(Keys.ESCAPE) && !Gdx.input.isKeyPressed(InputProcessor.keyboardB)
                && !Gdx.input.isKeyPressed(Keys.F5)) releasing=false;
        if(!coop.ready()) { if(watching()) stop("Disconnected from battle"); return; }
        if(watching()) {
            if(capture()!=null) { stop("Your battle started"); return; }
            if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)||Gdx.input.isKeyJustPressed(InputProcessor.keyboardB)) { stop(null); return; }
            Pose peer=coop.session().peers().get(target);
            if(peer==null) { stop("Player disconnected"); return; }
            int layer=game.map.tiles==game.map.overworldTiles?-1:game.map.interiorTilesIndex;
            if(!nearby(peer,game.map.currMapId,layer,game.player.position.x,game.player.position.y)) { stop("Player left the area"); return; }
            if(previousPose!=peer) {
                LanBattleState next=LanBattleState.readVisual(peer.visual);
                if(!sameBattle(battleId,next)) { stop("Battle ended"); return; }
                state=next; previousPose=peer;
            }
        } else if(!coop.hud.typing() && !releasing && game.playerCanMove && Gdx.input.isKeyJustPressed(Keys.F5)) {
            if(!startNearby()) coop.hud.chat.add("No nearby battle to watch",System.nanoTime());
        }
    }
    void stop(String message) {
        target=null; battleId=null; state=null; previousPose=null; ownView=enemyView=null;
        releasing=true; InputProcessor.clearControls();
        if(message!=null) coop.hud.chat.add(message,System.nanoTime());
    }
    private Pokemon view(Pokemon previous,LanBattleState.Fighter fighter) {
        if(previous!=null && previous.specie.name.equals(fighter.species) && previous.generation==fighter.generation
                && previous.isShiny==fighter.shiny) return previous;
        return new Pokemon(fighter.species,Math.min(100,fighter.level),fighter.generation,fighter.shiny);
    }
    private void rect(float x,float y,float w,float h,Color color) {
        if(pixel==null) { Pixmap map=new Pixmap(1,1,Pixmap.Format.RGBA8888); map.setColor(Color.WHITE); map.fill(); pixel=new Texture(map); map.dispose(); }
        game.uiBatch.setColor(color); game.uiBatch.draw(pixel,x,y,w,h);
    }
    private void health(LanBattleState.Fighter fighter,float x,float y) {
        rect(x-2,y-31,76,34,Color.WHITE); game.font.setColor(Color.BLACK);
        game.font.draw(game.uiBatch,LanBattleState.shortText(fighter.name.isEmpty()?fighter.species:fighter.name,12)+" L"+fighter.level,x,y);
        game.font.draw(game.uiBatch,fighter.hp+"/"+fighter.maxHp+" "+fighter.status,x,y-9);
        rect(x,y-25,70,5,Color.DARK_GRAY);
        float ratio=fighter.hp/(float)fighter.maxHp;
        rect(x+1,y-24,68*ratio,3,ratio>.5f?Color.GREEN:ratio>.2f?Color.YELLOW:Color.RED);
    }
    void draw() {
        if(!watching() || state==null) return;
        try { ownView=state.friendly==null?null:view(ownView,state.friendly); enemyView=view(enemyView,state.enemy); }
        catch(RuntimeException missingSpecies) { stop("Battle sprite unavailable"); return; }
        float scale=Math.min(Gdx.graphics.getWidth()/160f,Gdx.graphics.getHeight()/144f);
        float width=Gdx.graphics.getWidth()/scale,height=Gdx.graphics.getHeight()/scale;
        float ox=(width-160)/2,oy=(height-144)/2;
        previousProjection.set(game.uiBatch.getProjectionMatrix());
        projection.setToOrtho2D(-ox,-oy,width,height); game.uiBatch.setProjectionMatrix(projection);
        Color oldBatch=new Color(game.uiBatch.getColor()),oldFont=new Color(game.font.getColor());
        float sx=game.font.getData().scaleX,sy=game.font.getData().scaleY;
        try {
            rect(-ox,-oy,width,height,new Color(.06f,.07f,.1f,1));
            game.uiBatch.setColor(Color.WHITE);
            rect(0,0,160,144,Color.WHITE);
            Player trainer=game.players.get(target);
            Sprite back=ownView!=null?new Sprite(ownView.backSprite):trainer==null?null:new Sprite(trainer.battleSprite);
            Sprite front=new Sprite(enemyView.sprite);
            if(back!=null) { back.setBounds(6,35,56,56); back.draw(game.uiBatch); }
            front.setBounds(98,64,56,56); front.draw(game.uiBatch);
            game.font.getData().setScale(.62f); health(state.enemy,5,124);
            if(state.friendly!=null) health(state.friendly,83,65);
            rect(0,131,160,13,new Color(.06f,.07f,.1f,1));
            Pose peer=coop.session().peers().get(target); game.font.setColor(Color.WHITE);
            game.font.draw(game.uiBatch,"WATCHING "+LanBattleState.shortText(peer==null?"player":peer.name,18),4,140);
            rect(0,0,160,31,Color.WHITE); game.font.setColor(Color.BLACK);
            if(!state.text.equals(lastText)) {
                lastText=state.text;
                narration=LanHudLayout.wrap(state.text,152,value->{com.badlogic.gdx.graphics.g2d.GlyphLayout g=new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.font,value);return g.width;});
            }
            for(int i=0;i<Math.min(2,narration.size());i++) game.font.draw(game.uiBatch,narration.get(i),4,28-i*9);
            game.font.draw(game.uiBatch,"Turn "+state.turn+" | Esc / X leave",4,8);
        } finally {
            game.uiBatch.setColor(oldBatch); game.font.setColor(oldFont); game.font.getData().setScale(sx,sy);
            game.uiBatch.setProjectionMatrix(previousProjection);
        }
    }
    void dispose() { target=null; state=null; if(pixel!=null) { pixel.dispose(); pixel=null; } }
}
