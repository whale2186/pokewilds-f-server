package com.pkmngen.game;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.multiplayer.LanSession.Pose;
import com.pkmngen.game.util.Json;
import java.util.Locale;
import java.util.UUID;

/** Render-thread interaction state; transport authenticates the sender, never the payload. */
final class LanInteractions {
    private final Game game;
    private final LanCoop coop;
    private final LanBattleScreen battleScreen;
    private boolean turnPending;
    private long revealUntil;
    private StringBuilder battleLog;
    private Dialog dialog;
    private String peer, token, kind;
    private boolean leader, accepted, confirmed, remoteConfirmed, committed;
    private int slot, turn, myMove = -1, theirMove = -1;
    private Pokemon offered, opponent;
    private Pokemon mine;
    private long activity;
    private String status;
    private String decisionTransaction;
    private String remoteIdentity, remoteName;
    private boolean prepared;
    static final class Packet {
        String token, type, kind, identity, name, text;
        Network.PokemonData pokemon;
        int turn, move, hp, otherHp;
    }
    LanInteractions(Game game, LanCoop coop) { this.game = game; this.coop = coop; this.battleScreen = new LanBattleScreen(game); }
    boolean busy() { return dialog!=null; }
    LanBattleState battleView() {
        if (!"battle".equals(kind) || !accepted || offered==null || opponent==null || committed) return null;
        return LanBattleState.of(token,"pvp",turn+1,offered,opponent,status);
    }
    boolean openNearby() {
        if (dialog != null) return true;
        for (Pose pose : coop.session().peers().values()) {
            if (near(pose) && game.player.facingPos().dst2(pose.x, pose.y) <= 8*8) {
                peer = pose.id; token = UUID.randomUUID().toString(); leader = true;
                if (LanBattleState.readVisual(pose.visual)!=null) {
                    status=pose.name+": battling";
                    show(new String[]{"Spectate", "Cancel"},choice->{
                        String watching=pose.id; end(false);
                        if(choice==0) coop.spectator.start(watching);
                    });
                    return true;
                }
                kind = null; status = pose.name; show(new String[]{"Battle", "Trade", "Cancel"}, choice -> {
                    if (choice == 2) { end(false); return; }
                    kind = choice == 0 ? "battle" : "trade";
                    send("invite"); status = "Waiting for response"; show(new String[]{"Cancel"}, i -> end(true));
                });
                return true;
            }
        }
        return false;
    }
    private boolean near(Pose pose) {
        int layer = game.map.tiles == game.map.overworldTiles ? -1 : game.map.interiorTilesIndex;
        return !pose.id.equals(coop.session().id()) && pose.layer == layer
                && game.map.currMapId.equals(pose.mapId) && game.player.position.dst2(pose.x, pose.y) <= 24*24;
    }
    void dispose() { battleScreen.dispose(); }
    void update() {
        if (turnPending && dialog != null && System.nanoTime() >= revealUntil) finishTurn();
        if (dialog != null && prepared && !committed && coop.session().resource("trade:" + token) != null) {
            commitTrade();
        }
        if (decisionTransaction != null) {
            LanSession.TransactionResult result = coop.session().takeTransactionResult(decisionTransaction);
            if (result != null) { decisionTransaction = null; if (!result.accepted) { System.err.println("LAN trade decision rejected " + token); if (prepared) { coop.discardTrade(); prepared = false; } end(true); } }
        }
        for (LanSession.Message message : coop.session().drainMessages()) {
            if (coop.hud.chat.receive(message, coop.session().peers(), System.nanoTime())) continue;
            try {
                Packet packet = Json.gson.fromJson(message.payload, Packet.class);
                if (packet == null || packet.type == null || packet.token == null || packet.token.length() != 36) continue;
                if ("invite".equals(packet.type)) {
                    Pose pose = coop.session().peers().get(message.sender);
                    if (dialog != null || coop.spectator.watching() || coop.hud.typing() || coop.sharedEditPending() || !game.playerCanMove || pose == null || !near(pose)
                            || !("battle".equals(packet.kind) || "trade".equals(packet.kind))) {
                        Packet busy=new Packet();busy.token=packet.token;busy.type="busy";
                        coop.session().message(message.sender,Json.gson.toJson(busy));continue;
                    }
                    peer = message.sender; token = packet.token; kind = packet.kind; leader = false;
                    status = pose.name + ": " + kind + "?";
                    show(new String[]{"Accept", "Decline"}, i -> {
                        if (i == 1) { end(true); return; }
                        accepted = true; send("accept"); choosePokemon();
                    });
                    continue;
                }
                if (dialog == null || !message.sender.equals(peer) || !packet.token.equals(token)) continue;
                activity = System.nanoTime();
                switch (packet.type) {
                    case "busy": status="Player is busy. Try again.";show(new String[]{"Close"},i->end(false));break;
                    case "cancel":
                        if (decisionTransaction == null) {
                            if (prepared && coop.session().resource("trade:" + token) != null) commitTrade();
                            else { if (prepared) { coop.discardTrade(); prepared = false; } end(false); }
                        }
                        break;
                    case "accept": if (leader && !accepted) { accepted = true; choosePokemon(); } break;
                    case "offer":
                        if (!accepted || confirmed || !valid(packet.pokemon)) break;
                        if (opponent != null || packet.identity == null || !packet.identity.matches("[0-9a-f-]{36}")) break;
                        remoteIdentity = packet.identity; remoteName = packet.name;
                        opponent = new Pokemon(packet.pokemon); remoteConfirmed = false;
                        if (offered != null) ready();
                        break;
                    case "confirm":
                        if (!"trade".equals(kind) || offered == null || opponent == null) break;
                        remoteConfirmed = true; maybeCommit(); break;
                    case "move":
                        if (!leader || !"battle".equals(kind) || packet.turn != turn || theirMove != -1
                                || opponent == null || packet.move < 0 || packet.move >= 4) break;
                        theirMove = packet.move; resolve(); break;
                    case "result":
                        if (leader || !"battle".equals(kind) || packet.turn != turn || myMove < 0 || offered == null) break;
                        if (packet.hp < 0 || packet.hp > offered.maxStats.get("hp") || packet.otherHp < 0
                                || packet.otherHp > opponent.maxStats.get("hp")) break;
                        offered.currentStats.put("hp", packet.hp); opponent.currentStats.put("hp", packet.otherHp);
                        nextTurn(packet.text == null ? "Turn complete" : LanBattleState.shortText(packet.text,120)); break;
                }
            } catch (RuntimeException invalid) {
                System.err.println("LAN interaction rejected: " + invalid.getMessage());
            }
        }
        if (dialog != null && !coop.session().peers().containsKey(peer)) {
            if (decisionTransaction == null) {
                if (prepared && coop.session().resource("trade:" + token) != null) commitTrade();
                else { if (prepared) { coop.discardTrade(); prepared = false; } end(false); }
            }
        } else if (dialog != null && !confirmed && System.nanoTime() - activity > 120_000_000_000L) end(true);
    }
    private boolean valid(Network.PokemonData data) {
        return data != null && data.name != null && data.name.matches("[A-Za-z0-9_-]{1,64}")
                && data.level >= 1 && data.level <= 100 && data.attacks != null && data.attacks.length == 4
                && data.hp >= 0;
    }
    private void choosePokemon() {
        if (game.player.pokemon.isEmpty()) { end(true); return; }
        String[] choices = new String[game.player.pokemon.size() + 1];
        for (int i = 0; i < choices.length - 1; i++) choices[i] = (i + 1) + " " + game.player.pokemon.get(i).specie.name;
        choices[choices.length - 1] = "Cancel";
        status = "Choose POKEMON";
        show(choices, index -> {
            if (index == choices.length - 1) { end(true); return; }
            slot = index; mine = game.player.pokemon.get(index);
            if (mine.isEgg || ("battle".equals(kind) && mine.currentStats.get("hp") <= 0)) { choosePokemon(); return; }
            if ("trade".equals(kind) && mine == game.player.hmPokemon && !game.player.currFieldMove.isEmpty()) {
                status="Stop the field action or choose another POKEMON.";
                show(new String[]{"Choose another","Cancel"},i->{ if(i==0) choosePokemon(); else end(true); });return;
            }
            // PvP uses a private copy; no wild battle capture/reward side effects.
            offered = new Pokemon(new Network.PokemonData(mine));
            Packet packet = packet("offer"); packet.pokemon = new Network.PokemonData(offered); send(packet);
            status = "Waiting for offer"; show(new String[]{"Cancel"}, i -> end(true));
            if (opponent != null) ready();
        });
    }
    private void ready() {
        if ("trade".equals(kind)) {
            status = "Receive " + opponent.specie.name + "?";
            show(new String[]{"Confirm trade", "Cancel"}, index -> {
                if (index == 1) { end(true); return; }
                opponent.previousOwner = game.player;
                opponent.lanOwnerId = coop.identity(); opponent.lanOwnerName = game.player.name;
                opponent.mapTiles = game.map.tiles;
                opponent.interiorIndex = game.map.tiles == game.map.overworldTiles ? 100 : game.map.interiorTilesIndex;
                opponent.position = game.player.position.cpy();
                opponent.isRunning = false;
                try { coop.prepareTrade(slot, opponent, token); prepared = true; }
                catch (RuntimeException error) { error.printStackTrace(); status = "Cannot save trade"; show(new String[]{"Close"}, i -> end(true)); return; }
                confirmed = true; send("confirm"); status = "Waiting for confirmation";
                show(new String[]{"Waiting..."}, i -> {}); maybeCommit();
            });
        } else moves();
    }
    private void maybeCommit() {
        if (leader && confirmed && remoteConfirmed && !committed && decisionTransaction == null) {
            decisionTransaction = UUID.randomUUID().toString();
            String key = "trade:" + token;
            if (!coop.session().transact(decisionTransaction, java.util.Collections.singletonMap(key, 0L),
                    java.util.Collections.singletonMap(key, coop.session().id()))) {
                decisionTransaction = null;
                if (prepared) { coop.discardTrade(); prepared = false; }
                end(true);
            }
        }
    }
    private void commitTrade() {
        if (slot >= game.player.pokemon.size() || game.player.pokemon.get(slot) != mine) { System.err.println("LAN trade party changed before commit"); end(true); return; }
        committed = true;
        if (game.player.hmPokemon == mine) {
            mine.removeDrawActions(game);game.player.hmPokemon=null;game.player.currFieldMove="";
        }
        game.player.pokemon.set(slot, opponent);
        if (game.player.currPokemon == mine) game.player.currPokemon = opponent;
        try { coop.persistTrade(); }
        catch (RuntimeException error) {
            status = "Save failed; reconnect";
            show(new String[]{"Retry save"}, i -> { coop.persistTrade(); prepared = false; end(false); });
            return;
        }
        prepared = false;
        coop.hud.chat.add("Received " + opponent.nickname + " from " + remoteName + "!", System.nanoTime());
        end(false);
    }
    private void moves() {
        String[] moves = new String[5];
        for (int i = 0; i < 4; i++) moves[i] = offered.attacks[i] == null ? "-" : offered.attacks[i];
        moves[4] = "Forfeit";
        status = "Choose a move.";
        show(moves, index -> {
            if (index == 4) { end(true); return; }
            if (offered.attacks[index] == null || !game.battle.attacks.containsKey(offered.attacks[index].toLowerCase(Locale.ROOT))) return;
            myMove = index;
            Packet packet = packet("move"); packet.move = index; send(packet);
            status = "Waiting for opponent"; show(new String[]{"Forfeit"}, i -> end(true));
            if (leader) resolve();
        });
    }
    private void resolve() {
        if (myMove < 0 || theirMove < 0 || turnPending) return;
        battleLog = new StringBuilder();
        Attack a = attack(offered, myMove), b = attack(opponent, theirMove);
        if (a == null || b == null) { end(true); return; }
        boolean first = a.priority != b.priority ? a.priority > b.priority
                : offered.currentStats.get("speed") >= opponent.currentStats.get("speed");
        if (first) { hit(offered, a, opponent); if (opponent.currentStats.get("hp") > 0) hit(opponent, b, offered); }
        else { hit(opponent, b, offered); if (offered.currentStats.get("hp") > 0) hit(offered, a, opponent); }
        Packet packet = packet("result"); packet.hp = opponent.currentStats.get("hp"); packet.otherHp = offered.currentStats.get("hp"); packet.text = battleLog.toString(); send(packet);
        nextTurn(packet.text);
    }
    private Attack attack(Pokemon pokemon, int move) {
        String name = pokemon.attacks[move]; return name == null ? null : game.battle.attacks.get(name.toLowerCase(Locale.ROOT));
    }
    private void hit(Pokemon source, Attack attack, Pokemon target) {
        if (battleLog.length()>0) battleLog.append(" ");
        battleLog.append(source.nickname.toUpperCase(Locale.ROOT)).append(" used ").append(attack.name.toUpperCase(Locale.ROOT)).append("!");
        if (attack.accuracy > 0 && game.map.rand.nextInt(100) >= attack.accuracy) { battleLog.append(" Missed!"); return; }
        if (attack.power > 0 || attack.effect.equals("EFFECT_LEVEL_DAMAGE") || attack.effect.equals("EFFECT_STATIC_DAMAGE")) {
            int damage = Battle.gen2CalcDamage(source, new Attack(attack), target);
            target.currentStats.put("hp", Math.max(0, target.currentStats.get("hp") - Math.max(0, damage)));
        } else if (attack.effect.equals("EFFECT_HEAL")) {
            source.currentStats.put("hp",Math.min(source.maxStats.get("hp"),source.currentStats.get("hp")+Math.max(1,source.maxStats.get("hp")/2)));
        } else {
            String effect=attack.effect;
            boolean up=effect.contains("UP"),down=effect.contains("DOWN");
            String stat=effect.contains("ATTACK")?"attack":effect.contains("DEFENSE")?"defense":effect.contains("SPEED")?"speed":null;
            if(stat!=null && (up||down)) {
                Pokemon affected=up?source:target;
                float multiplier=up?1.5f:2f/3;
                affected.currentStats.put(stat,Math.max(1,(int)(affected.currentStats.get(stat)*multiplier)));
            } else battleLog.append(" No effect.");
        }
    }
    private void nextTurn(String text) {
        turn++; myMove = theirMove = -1; status=LanBattleState.shortText(text,120);
        turnPending=true; revealUntil=System.nanoTime()+1_300_000_000L;
        show(new String[]{"Continue"}, i -> finishTurn());
    }
    private void finishTurn() {
        turnPending=false;
        if (offered==null || opponent==null) return;
        if (offered.currentStats.get("hp") <= 0 || opponent.currentStats.get("hp") <= 0) {
            status = offered.currentStats.get("hp") > 0 ? "You won!" : "You lost!";
            show(new String[]{"Close"}, i -> end(false));
        } else moves();
    }
    private Packet packet(String type) {
        Packet packet = new Packet(); packet.token = token; packet.type = type; packet.kind = kind; packet.turn = turn; packet.identity = coop.identity(); packet.name = game.player.name; return packet;
    }
    private boolean send(String type) { return send(packet(type)); }
    private boolean send(Packet packet) { return coop.session().message(peer, Json.gson.toJson(packet)); }
    private interface Select { void select(int index); }
    private void show(String[] choices, Select select) {
        if (dialog != null) game.actionStack.remove(dialog);
        game.playerCanMove = false; activity = System.nanoTime();
        dialog = new Dialog(choices, select); game.insertAction(dialog);
    }
    private void end(boolean notify) {
        // Once both confirmations have been sent, retain the recovery journal until
        // an authoritative decision arrives, even if the connection disappears.
        if (notify && token != null) send("cancel");
        if (prepared && !confirmed) { coop.discardTrade(); prepared = false; }
        if (dialog != null) game.actionStack.remove(dialog);
        dialog = null; peer = token = kind = null; offered = opponent = mine = null;
        turnPending=false; battleScreen.reset();
        accepted = confirmed = remoteConfirmed = committed = false; prepared = false; remoteIdentity = remoteName = null;
        turn = 0; myMove = theirMove = -1; game.playerCanMove = true;
    }
    private final class Dialog extends Menu {
        final String[] choices; final Select select; int cursor, frames;
        Dialog(String[] choices, Select select) { this.choices = choices; this.select = select; }
        @Override public String getCamera() { return "gui"; }
        @Override public Layer getLayer() { return Layer.gui_140; }
        private final GameMenuPanel panel = new GameMenuPanel();
        @Override public void step(Game current) {
            boolean locked = confirmed && "trade".equals(kind);
            String[] labels=choices.clone();
            for(int i=0;i<labels.length;i++) {
                if ("Cancel".equals(labels[i]) || "Close".equals(labels[i]) || "Decline".equals(labels[i])) labels[i]="Exit";
                else if ("Forfeit".equals(labels[i])) labels[i]="Exit battle";
            }
            Pose player=coop.session().peers().get(peer);
            String title=player==null?"Player interaction":player.name;
            boolean battle = "battle".equals(kind) && accepted && offered != null && opponent != null;
            if (battle) battleScreen.draw(offered,opponent,title,status,choices,cursor);
            else panel.draw(game,title,status,labels,cursor,locked);
            if (frames++ < 2) return;
            if (InputProcessor.upJustPressed) cursor = (cursor + choices.length - 1) % choices.length;
            if (InputProcessor.downJustPressed) cursor = (cursor + 1) % choices.length;
            if (battle && choices.length==5) {
                if (InputProcessor.leftJustPressed) cursor=4;
                if (InputProcessor.rightJustPressed && cursor==4) cursor=0;
            }
            int click=battle?battleScreen.clicked(choices.length):panel.clicked();
            if ((InputProcessor.bJustPressed || com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) && !locked) end(true);
            else if(click>=0) { cursor=click; select.select(cursor); }
            else if (InputProcessor.menuConfirmJustPressed()) select.select(cursor);
        }
    }
}
