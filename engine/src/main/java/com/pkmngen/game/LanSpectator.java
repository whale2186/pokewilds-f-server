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
    private Sprite background;
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
            if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)||Gdx.input.isKeyJustPressed(InputProcessor.keyboardB)||exitClicked()) { stop(null); return; }
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
    private boolean exitClicked() {
        if(!Gdx.input.justTouched()) return false;
        float scale=Math.min(Gdx.graphics.getWidth()/160f,Gdx.graphics.getHeight()/144f);
        float x=(Gdx.input.getX()-(Gdx.graphics.getWidth()-160*scale)/2)/scale;
        float y=(Gdx.graphics.getHeight()-Gdx.input.getY()-(Gdx.graphics.getHeight()-144*scale)/2)/scale;
        return x>=112 && x<=160 && y>=0 && y<=18;
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
            if(background==null) background=BattleWindow.background();
            BattleWindow.background(game,background);
            Player trainer=game.players.get(target);
            Sprite back=ownView!=null?new Sprite(ownView.backSprite):trainer==null?null:new Sprite(trainer.battleSprite);
            Sprite front=new Sprite(enemyView.sprite);
            front.setPosition(front.getWidth()<=48?102:96,88); front.draw(game.uiBatch);
            if(back!=null) { back.setPosition(ownView!=null?22:16,ownView!=null?62:49); back.draw(game.uiBatch); }
            BattleWindow.health(game,state.enemy,false);
            if(state.friendly!=null) BattleWindow.health(game,state.friendly,true);
            game.uiBatch.draw(BattleWindow.texture("textbox_bg1.png"),0,0);
            if(!state.text.equals(lastText)) {
                lastText=state.text;
                narration=LanHudLayout.wrap(state.text,144,value->value.length()*8);
            }
            for(int i=0;i<Math.min(2,narration.size());i++) BattleWindow.text(game,narration.get(i),8,32-i*12,18);
            game.font.getData().setScale(.55f); game.font.setColor(Color.BLACK);
            Pose peer=coop.session().peers().get(target);
            game.font.draw(game.uiBatch,"Watching "+LanBattleState.shortText(peer==null?"player":peer.name,14),8,11);
            rect(112,1,47,16,new Color(.82f,.9f,1,1));
            game.uiBatch.setColor(Color.WHITE);
            BattleWindow.text(game,"EXIT",120,5,4);

        } finally {
            game.uiBatch.setColor(oldBatch); game.font.setColor(oldFont); game.font.getData().setScale(sx,sy);
            game.uiBatch.setProjectionMatrix(previousProjection);
        }
    }
    void dispose() { target=null; state=null; if(pixel!=null) { pixel.dispose(); pixel=null; } }
}
