package com.pkmngen.game;

import com.pkmngen.game.util.Json;
import java.util.UUID;

public final class LanBattleStateTest {
    static LanBattleState state() {
        LanBattleState battle=new LanBattleState(); battle.id=UUID.randomUUID().toString(); battle.mode="wild"; battle.turn=2; battle.text="PIKACHU used THUNDERSHOCK!";
        battle.friendly=fighter("pikachu"); battle.enemy=fighter("rattata"); return battle;
    }
    static LanBattleState.Fighter fighter(String species) {
        LanBattleState.Fighter f=new LanBattleState.Fighter(); f.species=species; f.name=species; f.level=20;
        f.generation=Pokemon.Generation.CRYSTAL; f.status=""; f.maxHp=50; f.hp=40; return f;
    }
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        LanBattleState battle=state(); check(battle.valid(),"Valid battle rejected");
        LanVisual visual=new LanVisual(); visual.battle=battle;
        String encoded=Json.gson.toJson(visual); check(encoded.length()<2048,"Pose visual budget exceeded");
        LanBattleState read=LanBattleState.readVisual(encoded);
        check(read!=null && read.friendly.hp==40 && read.enemy.species.equals("rattata"),"Combatant state lost");
        check(LanSpectator.sameBattle(battle.id,read),"Battle identity lost");
        check(!LanSpectator.sameBattle(UUID.randomUUID().toString(),read),"Different battle followed silently");
        check(LanBattleState.readVisual("{}") == null && LanBattleState.readVisual("{broken")==null,"Missing/malformed state accepted");
        battle.friendly.hp=-1; check(!battle.valid(),"Negative HP accepted"); battle.friendly.hp=51; check(!battle.valid(),"HP beyond maximum accepted");
        battle.friendly.hp=40; battle.enemy.species="../asset"; check(!battle.valid(),"Asset path traversal accepted");
        battle=state(); battle.id="forged"; check(!battle.valid(),"Bad battle identity accepted");
        battle=state(); battle.text=new String(new char[121]); check(!battle.valid(),"Unbounded text accepted");
        battle=state(); battle.mode="pvp"; check(battle.valid(),"PvP state rejected");
        battle=state(); battle.mode="safari"; battle.friendly=null; check(battle.valid(),"Trainer-only encounter rejected");
        battle.mode="wild"; check(!battle.valid(),"Missing regular combatant accepted");
        com.pkmngen.game.multiplayer.LanSession.Pose peer=new com.pkmngen.game.multiplayer.LanSession.Pose("peer","same","gold","down","0,0",-1,16,0,false);
        check(LanSpectator.nearby(peer,"0,0",-1,0,0),"Nearby peer rejected");
        check(!LanSpectator.nearby(peer,"0,0",0,0,0) && !LanSpectator.nearby(peer,"1,0",-1,0,0)
                && !LanSpectator.nearby(peer,"0,0",-1,200,0),"Spectator crosses map/layer/range");
        System.out.println("PASS bounded battle snapshots, invalid input, battle identity, PvP and spectator range");
    }
}
