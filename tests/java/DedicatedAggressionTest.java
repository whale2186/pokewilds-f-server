package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.util.Json;
import java.util.*;

/** Pursuit uses real authority revisions and each player's layer and current activity. */
public final class DedicatedAggressionTest {
    static final String KEY="pokemon:hostile";
    static LanCoop.WorldInfo world;
    public static void main(String[] args) throws Exception {
        world=new LanCoop.WorldInfo();world.mapId="0,0";world.dedicated=true;
        Map<String,String> initial=new HashMap<>();
        for(int layer=-1;layer<=0;layer++) for(int x=-64;x<=128;x+=16) for(int y=-64;y<=64;y+=16) {
            Network.TileData tile=new Network.TileData();tile.pos=new Vector2(x,y);tile.tileName="desert1";tile.tileNameUpper="";
            initial.put("tile:"+layer+":"+x+":"+y,Json.gson.toJson(tile));
        }
        initial.put(KEY,creature("tauros",true,null,null));
        try(LanSession host=LanSession.host("127.0.0.1",0,"server",Json.gson.toJson(world),initial,(id,p,k,b,a)->true);
            LanSession first=LanSession.join("127.0.0.1",host.port(),"first");
            LanSession second=LanSession.join("127.0.0.1",host.port(),"second")) {
            pose(host,first,64,0,-1,"{}");pose(host,second,-16,0,-1,"{}");
            set(host,KEY,creature("tauros",true,first.id(),null));
            DedicatedSimulation ai=new DedicatedSimulation(world,new Random(2));
            tick(host,ai);check(host,16,first.id(),"provoked creature chose the closer bystander");
            tick(host,ai);check(host,32,first.id(),"pursuit stopped near a player");
            pose(host,first,64,0,-1,"{\"busy\":true}");tick(host,ai);check(host,32,null,"busy player was chased");
            pose(host,first,64,0,-1,"{\"flying\":true}");tick(host,ai);check(host,32,null,"flying player was chased");
            pose(host,first,64,0,-1,"{\"calmingLevel\":50}");tick(host,ai);check(host,32,null,"Charm was ignored");
            pose(host,first,64,0,-1,"{}");
            Network.TileData fence=Json.gson.fromJson(host.resource("tile:-1:48:0").json,Network.TileData.class);fence.tileNameUpper="fence1";
            set(host,"tile:-1:48:0",Json.gson.toJson(fence));tick(host,ai);check(host,32,first.id(),"walked through fence");
            fence.tileNameUpper="";set(host,"tile:-1:48:0",Json.gson.toJson(fence));
            LanCoop.CreatureInfo info=read(host);info.encounterOwner=first.id();set(host,KEY,Json.gson.toJson(info));tick(host,ai);check(host,32,first.id(),"battle reservation moved");
            pose(host,second,-16,0,0,"{}");set(host,KEY,creature("drapion",false,null,null));tick(host,ai);check(host,16,first.id(),"wild Drapion did not chase");
            if(!read(host).data.aggroPlayer) throw new AssertionError("Drapion aggression flag lost");
            set(host,KEY,creature("drapion",true,null,first.id()));tick(host,ai);check(host,0,null,"owned Drapion chased");
            set(host,KEY,creature("tauros",true,first.id(),null));pose(host,first,64,0,0,"{}");tick(host,ai);check(host,0,null,"chased through another layer");
            pose(host,first,64,0,-1,"{}");first.close();
            long deadline=System.nanoTime()+3_000_000_000L;while(host.peers().containsKey(first.id())&&System.nanoTime()<deadline)Thread.sleep(10);
            tick(host,ai);if(read(host).provokedBy!=null)throw new AssertionError("Disconnected provoker retained");
            world.dedicated=false;pose(host,host,64,0,-1,"{}");tick(host,ai);check(host,16,host.id(),"graphical host was excluded from pursuit");
            set(host,KEY,creature("tauros",false,null,null));tick(host,ai);check(host,0,null,"de-aggroed Tauros chased");
            LanCoop.CreatureInfo fainted=read(host);fainted.data.hp=0;set(host,KEY,Json.gson.toJson(fainted));tick(host,ai);check(host,0,null,"fainted creature chased");
        }
        System.out.println("PASS provocation, Drapion, busy/FLY/CHARM, fences, layers, ownership, reservation and disconnect");
    }
    static String creature(String name,boolean aggro,String provoker,String owner) {
        LanCoop.CreatureInfo info=new LanCoop.CreatureInfo();info.layer=-1;info.direction="down";info.provokedBy=provoker;
        info.data=new Network.PokemonData();info.data.name=name;info.data.hp=50;info.data.level=20;info.data.aggroPlayer=aggro;info.data.position=new Vector2();info.data.lanOwnerId=owner;
        return Json.gson.toJson(info);
    }
    static LanCoop.CreatureInfo read(LanSession host) {return Json.gson.fromJson(host.resource(KEY).json,LanCoop.CreatureInfo.class);}
    static void check(LanSession host,float x,String target,String error) {LanCoop.CreatureInfo c=read(host);if(c.data.position.x!=x||c.data.position.y!=0||!Objects.equals(c.pursuitTarget,target))throw new AssertionError(error+": "+Json.gson.toJson(c));}
    static void tick(LanSession host,DedicatedSimulation ai)throws Exception {ai.tick(host,15,false);Thread.sleep(60);}
    static void pose(LanSession host,LanSession client,float x,float y,int layer,String visual)throws Exception {
        client.pose(new LanSession.Pose(client.id(),"player","gold","down",world.mapId,layer,x,y,false,false,0,0,visual));
        long deadline=System.nanoTime()+3_000_000_000L;
        while(System.nanoTime()<deadline){LanSession.Pose p=host.peers().get(client.id());if(p!=null&&p.x==x&&p.y==y&&p.layer==layer&&p.visual.equals(visual))return;Thread.sleep(10);}throw new AssertionError("pose timeout");
    }
    static void set(LanSession host,String key,String json)throws Exception {
        String id=UUID.randomUUID().toString();LanSession.Value v=host.resource(key);
        host.transact(id,Collections.singletonMap(key,v==null?0L:v.revision),Collections.singletonMap(key,json));
        long deadline=System.nanoTime()+3_000_000_000L;
        while(System.nanoTime()<deadline){LanSession.TransactionResult r=host.drainTransactions().get(id);if(r!=null){if(!r.accepted)throw new AssertionError("rejected "+key);return;}Thread.sleep(10);}throw new AssertionError("edit timeout");
    }
}
