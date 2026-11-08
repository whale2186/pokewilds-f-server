package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;

/** Optional graphical smoke: use a fresh dedicated authority; no existing user saves are modified. */
public final class DedicatedClientE2E extends Game {
    private long started = System.nanoTime(), ready;
    private int stage;
    DedicatedClientE2E(String[] args) { super(args, 2); }
    @Override public void create() { super.create(); InputProcessor.acceptInput = false; Game.musicDisabled = true; }
    @Override public void render() {
        try {
            super.render();
            if (System.nanoTime() - started > 60_000_000_000L) throw new AssertionError("Client deadline");
            if (coop != null && coop.problem() != null) throw new AssertionError(coop.problem());
            if (coop == null || !coop.ready() || coop.simulationBlocked()) return;
            if (stage == 0) {
                if (!coop.worldMatchesAuthority()) throw new AssertionError("Installed world differs from authority");
                if (map.overworldTiles.size() != 129 * 129) throw new AssertionError("Wrong overworld");
                if (map.interiorTiles.get(98).isEmpty()) throw new AssertionError("Missing dungeon");
                for (Pokemon pokemon : map.pokemon.values()) {
                    if (pokemon.drawLower == null || pokemon.drawUpper == null) throw new AssertionError("Missing creature rendering");
                    if (pokemon.currentStats.get("hp") > pokemon.maxStats.get("hp")) throw new AssertionError("Invalid HP: " + pokemon.specie.name);
                    for (String attack : pokemon.attacks) if (attack != null && !battle.attacks.containsKey(attack)) throw new AssertionError("Invalid move: " + attack);
                    if (pokemon.mapTiles == map.overworldTiles && pokemon.previousOwner == null) battle.oppPokemon = pokemon;
                }
                if (battle.oppPokemon == null) throw new AssertionError("No wild creature");
                player.position.set(battle.oppPokemon.position).add(0, -16);
                cam.position.set(player.position.x, player.position.y, 0);
                ready = System.nanoTime(); stage = 1;
                System.out.println("DEDICATED_CLIENT world loaded creatures=" + map.pokemon.size());
            } else if (stage == 1 && System.nanoTime() - ready > 1_000_000_000L) {
                playerCanMove = false;
                insertAction(Battle.getIntroAction(this)); stage = 2;
            } else if (stage == 2 && coop.ownsBattle(battle.oppPokemon)) {
                System.out.println("DEDICATED_CLIENT battle reserved"); ready = System.nanoTime(); stage = 3;
            } else if (stage == 3 && System.nanoTime() - ready > 4_000_000_000L) {
                System.out.println("DEDICATED_CLIENT PASS"); Gdx.app.exit();
            }
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dedicated client smoke"); config.setWindowedMode(480, 432); config.setForegroundFPS(60); config.disableAudio(true);
        new Lwjgl3Application(new DedicatedClientE2E(args), config);
    }
}
