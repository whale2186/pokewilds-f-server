package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.Json;

/** Real guest loop: native provocation, contact battle, ghost reveal and ghost battle. */
public final class LanHostilityE2E extends Game {
    final boolean ghosts=Boolean.getBoolean("e2e.ghosts");
    final long started=System.nanoTime();
    int phase,frames;boolean moved;Vector2 original;Pokemon hostile;
    LanHostilityE2E(String[] args){super(args,2);}
    @Override public void create(){super.create();Game.musicDisabled=true;InputProcessor.acceptInput=false;}
    CycleDayNight cycle(){return (CycleDayNight)actionStack.stream().filter(a->a instanceof CycleDayNight).findFirst().orElseThrow(()->new AssertionError("missing night action"));}
    boolean ghost(){return actionStack.stream().anyMatch(a->a instanceof SpawnGhost || a instanceof DrawGhost);}
    void resetLight(CycleDayNight c)throws Exception{java.lang.reflect.Field f=CycleDayNight.class.getDeclaredField("lightCheckTimer");f.setAccessible(true);f.setInt(c,0);c.countDownToGhost=1;}
    @Override public void render(){try{
        super.render();InputProcessor.clearControls();
        if(System.nanoTime()-started>75_000_000_000L)throw new AssertionError("Hostility timeout phase="+phase);
        if(coop==null||!coop.ready())return;
        if(coop.problem()!=null)throw new AssertionError(coop.problem());
        if(phase==0){
            player.pokemon.clear();Pokemon p=new Pokemon("machamp",50);p.previousOwner=player;player.pokemon.add(p);player.currPokemon=p;
            original=map.pokemon.values().stream().filter(w->w.specie.name.equals("tauros")).findFirst().orElseThrow(()->new AssertionError("missing fixture")).position.cpy();
            player.position.set(ghosts?new Vector2():original.cpy().add(-16,0));player.dirFacing="right";player.acceptInput=true;phase=1;frames=0;return;
        }
        if(ghosts){
            CycleDayNight c=cycle();
            if(phase==1){
                if(++frames<40)return;
                String time=map.timeOfDay;map.timeOfDay="day";resetLight(c);c.step(this);if(ghost())throw new AssertionError("day ghost");map.timeOfDay=time;
                Tile ground=map.tiles.get(new Vector2());ground.items=new java.util.HashMap<>();ground.items.put("torch",1);
                resetLight(c);c.step(this);if(ghost())throw new AssertionError("torch protection failed");ground.items=null;
                Pokemon light=new Pokemon("ampharos",30);player.hmPokemon=light;
                if(!light.hms.contains("FLASH"))throw new AssertionError("fixture lacks Flash");
                resetLight(c);c.step(this);if(ghost())throw new AssertionError("Flash protection failed");player.hmPokemon=null;
                resetLight(c);c.step(this);if(!ghost())throw new AssertionError("guest ghost did not spawn");
                System.out.println("GHOST_REVEAL day, torch and Flash checks passed");phase=2;
            }
            if(phase==2){
                for(Action a:actionStack)if(a instanceof DrawGhost){DrawGhost g=(DrawGhost)a;if(!g.pokemon.isGhost)throw new AssertionError("unmasked ghost");g.basePos.set(player.position.x-8,player.position.y-8);g.velX=0;g.velY=0;phase=3;break;}
            }
            if(phase==3&&actionStack.stream().anyMatch(a->a instanceof DrawBattle)){
                if(!battle.oppPokemon.isGhost)throw new AssertionError("ghost battle lost disguise");
                System.out.println("PASS guest ghost reveal, chase and native battle");Gdx.app.exit();
            }
        }else{
            Pokemon p=hostile==null?map.pokemonAt(original):hostile;
            if(phase==1){
                if(++frames<45)return;if(p==null)throw new AssertionError("missing Tauros fixture");
                hostile=p;p.hasItem=null;p.interactedWith=true;map.tiles.get(original).onPressA(this);phase=2;return;
            }
            if(phase==2){
                if(p==null||!p.aggroPlayer)return;
                player.position.set(original).add(48,0);phase=3;System.out.println("PROVOKED native interaction completed");
            }
            if(phase==3){
                for(Pokemon w:map.pokemon.values())if(w.specie.name.equals("tauros")&&w.position.x>original.x)moved=true;
                if(actionStack.stream().anyMatch(a->a instanceof DrawBattle)){
                    LanCoop.CreatureInfo info=Json.gson.fromJson(coop.session().resource(coop.worldCreatureId(battle.oppPokemon)).json,LanCoop.CreatureInfo.class);
                    if(!coop.session().id().equals(info.provokedBy)||!moved||!battle.oppPokemon.specie.name.equals("tauros")||!coop.ownsBattle(battle.oppPokemon))throw new AssertionError("pursuit/native encounter reservation failed");
                    System.out.println("PASS provocation, visible pursuit and reserved native wild battle");Gdx.app.exit();
                }
            }
        }
    }catch(Throwable t){t.printStackTrace();System.exit(1);}}
    public static void main(String[] args){Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setInitialVisible(false);c.setWindowedMode(640,576);c.setForegroundFPS(60);c.setIdleFPS(60);c.disableAudio(true);new Lwjgl3Application(new LanHostilityE2E(args),c);}
}
