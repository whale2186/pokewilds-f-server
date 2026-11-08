package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import java.lang.reflect.Field;

/** Two real render loops exercise invitations, selections, trade ownership and battle turns. */
public final class LanInteractionE2E extends Game {
    private final boolean host = Boolean.getBoolean("e2e.host");
    private final long launched = System.nanoTime();
    private int phase;
    private boolean setup;
    private long lastClick, finished;
    private Pokemon original;
    private String initialSpecies;
    LanInteractionE2E(String[] args) { super(args, 2); }
    static Object field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(object);
    }
    @Override public void create() { super.create(); InputProcessor.acceptInput = false; Game.musicDisabled = true; }
    @Override public void render() {
        try {
            super.render(); InputProcessor.aJustPressed = false;
            if (System.nanoTime() - launched > 90_000_000_000L) throw new AssertionError("Interaction timeout phase=" + phase);
            if (coop.problem() != null) throw new AssertionError(coop.problem());
            if (!coop.ready()) return;
            if (!setup) {
                player.pokemon.clear(); original = new Pokemon(host ? "pikachu" : "charmander", 20, Pokemon.Generation.CRYSTAL);
                original.attacks = new String[]{"tackle", "tackle", "tackle", "tackle"};
                player.pokemon.add(original); player.currPokemon = original; initialSpecies = original.specie.name;
                player.position.set(coop.sharedSpawn()).add(host ? 0 : 16, 0); player.dirFacing = host ? "right" : "left";
                setup = true;
            }
            Object interactions = field(coop, "interactions");
            Object dialog = field(interactions, "dialog");
            if (phase == 0 && player.pokemon.get(0) == original && dialog == null && host && coop.session().peers().size() == 2 && !players.isEmpty()) {
                if (coop.interactWithPlayer()) System.out.println("E2E OPEN_INTERACTION");
                return;
            }
            if (dialog != null) {
                if ((Integer)field(dialog, "frames") < 3 || System.nanoTime() - lastClick < 100_000_000L) return;
                String[] choices = (String[])field(dialog, "choices");
                int choice = 0;
                if (choices[0].equals("Battle")) choice = phase == 0 ? 1 : 0;
                else if (choices[0].startsWith("Waiting") || choices[0].equals("Cancel") || choices[0].equals("Forfeit")) return;
                else if (choices[0].equals("Close")) phase = 3;
                Field cursor = dialog.getClass().getDeclaredField("cursor"); cursor.setAccessible(true); cursor.setInt(dialog, choice);
                InputProcessor.aJustPressed = true; lastClick = System.nanoTime();
                return;
            }
            if (phase == 0 && player.pokemon.get(0) != original) {
                if (player.pokemon.size() != 1 || player.pokemon.get(0).specie.name.equals(initialSpecies)) throw new AssertionError("Trade did not exchange slots");
                if (player.pokemon.get(0).lanOwnerId == null) throw new AssertionError("Ownership missing");
                original = player.pokemon.get(0); phase = 1;
                System.out.println("E2E TRADE_COMPLETE " + original.specie.name);
            }
            if (phase == 1 && host) {
                if (coop.interactWithPlayer()) phase = 2;
            }
            if (phase == 3) {
                if (player.pokemon.get(0) != original) throw new AssertionError("PvP modified party identity");
                if (finished == 0) { finished = System.nanoTime(); System.out.println("PASS trade and PvP"); }
                if (System.nanoTime() - finished > 1_000_000_000L) Gdx.app.exit();
            }
        } catch (Throwable error) { error.printStackTrace(); System.exit(1); }
    }
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(480, 432); config.setForegroundFPS(60); config.setIdleFPS(60); config.disableAudio(true);
        new Lwjgl3Application(new LanInteractionE2E(args), config);
    }
}
