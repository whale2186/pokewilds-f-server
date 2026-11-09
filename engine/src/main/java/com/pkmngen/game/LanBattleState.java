package com.pkmngen.game;

import com.pkmngen.game.util.Json;

/** Public battle presentation only; excludes parties, bags, move sets and actionable commands. */
final class LanBattleState {
    String id, mode, text;
    int turn;
    Fighter friendly, enemy;
    static final class Fighter {
        String species, name, status;
        Pokemon.Generation generation;
        int level, hp, maxHp;
        boolean shiny, ghost;
        static Fighter of(Pokemon pokemon) {
            if (pokemon == null || pokemon.specie == null) return null;
            Fighter out=new Fighter(); out.species=pokemon.specie.name;
            out.name=shortText(pokemon.nickname,24); out.status=shortText(pokemon.status,16);
            out.generation=pokemon.generation; out.level=pokemon.level; out.shiny=pokemon.isShiny; out.ghost=pokemon.isGhost;
            out.maxHp=pokemon.maxStats.getOrDefault("hp",1);
            out.hp=Math.max(0,Math.min(out.maxHp,pokemon.currentStats.getOrDefault("hp",0)));
            return out;
        }
        boolean valid() {
            return species!=null && species.matches("[a-z0-9_]{1,40}") && generation!=null
                    && name!=null && name.length()<=24 && LanChat.clean(name).equals(name)
                    && status!=null && status.length()<=16 && LanChat.clean(status).equals(status)
                    && level>=1 && level<=1000 && maxHp>0 && maxHp<=1_000_000 && hp>=0 && hp<=maxHp;
        }
    }
    static String shortText(String value,int limit) {
        String text=LanChat.clean(value); return text.substring(0,Math.min(limit,text.length()));
    }
    static LanBattleState of(String id,String mode,int turn,Pokemon friendly,Pokemon enemy,String text) {
        LanBattleState out=new LanBattleState(); out.id=id; out.mode=mode; out.turn=Math.max(1,turn);
        out.friendly=Fighter.of(friendly); out.enemy=Fighter.of(enemy); out.text=shortText(text,120);
        return out.valid()?out:null;
    }
    boolean valid() {
        return id!=null && id.matches("[0-9a-f-]{36}") && ("wild".equals(mode)||"pvp".equals(mode)||"safari".equals(mode))
                && turn>=1 && turn<=1_000_000 && text!=null && text.length()<=120 && LanChat.clean(text).equals(text)
                && ((friendly!=null && friendly.valid()) || (friendly==null && "safari".equals(mode)))
                && enemy!=null && enemy.valid();
    }
    static LanBattleState readVisual(String visual) {
        if (visual==null || visual.length()>2048 || visual.isEmpty()) return null;
        try {
            com.google.gson.JsonObject object=Json.gson.fromJson(visual,com.google.gson.JsonObject.class);
            if (object==null || !object.has("battle")) return null;
            LanBattleState state=Json.gson.fromJson(object.get("battle"),LanBattleState.class);
            return state!=null && state.valid()?state:null;
        } catch (RuntimeException invalid) { return null; }
    }
}
