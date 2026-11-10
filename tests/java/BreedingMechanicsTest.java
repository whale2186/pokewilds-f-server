package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import java.util.*;

public final class BreedingMechanicsTest extends Game {
    public BreedingMechanicsTest(String[] args) {
        super(args, 2);
    }

    @Override
    public void create() {
        try {
            super.create();
            InputProcessor.acceptInput = false;
            Game.musicDisabled = true;

            this.map = new PkmnMap("0,0");
            this.map.tiles = new HashMap<>();
            this.map.pokemon = new HashMap<>();

            // Create a 7x7 field of green grass tiles
            for (int x = -48; x <= 48; x += 16) {
                for (int y = -48; y <= 48; y += 16) {
                    Vector2 pos = new Vector2(x, y);
                    Tile tile = new Tile("green1", "", pos, false);
                    this.map.tiles.put(pos, tile);
                }
            }

            // Test 1: Single egg group pair (Rattata: EGG_FIELD, EGG_NONE)
            Pokemon femaleRattata = new Pokemon("rattata", 5, Pokemon.Generation.CRYSTAL, false);
            femaleRattata.gender = "female";
            femaleRattata.position = new Vector2(0, 0);
            femaleRattata.mapTiles = this.map.tiles;

            Pokemon maleRattata = new Pokemon("rattata", 5, Pokemon.Generation.CRYSTAL, false);
            maleRattata.gender = "male";
            maleRattata.position = new Vector2(16, 0);
            maleRattata.mapTiles = this.map.tiles;

            this.map.pokemon.put(femaleRattata.position, femaleRattata);
            this.map.pokemon.put(maleRattata.position, maleRattata);

            femaleRattata.checkHabitat(this);
            if (femaleRattata.loveInterest != maleRattata) {
                throw new AssertionError("Rattata pair with EGG_NONE failed to fall in love: " + femaleRattata.loveInterest);
            }
            if (maleRattata.loveInterest != femaleRattata) {
                throw new AssertionError("Male Rattata did not reciprocate love interest: " + maleRattata.loveInterest);
            }
            System.out.println("PASS: Single egg group pair fell in love");

            // Test 2: Ditto with male partner
            Pokemon ditto = new Pokemon("ditto", 10, Pokemon.Generation.CRYSTAL, false);
            ditto.gender = "unknown";
            ditto.position = new Vector2(0, 16);
            ditto.mapTiles = this.map.tiles;

            this.map.pokemon.clear();
            maleRattata.loveInterest = null;
            this.map.pokemon.put(ditto.position, ditto);
            this.map.pokemon.put(maleRattata.position, maleRattata);

            ditto.checkHabitat(this);
            if (ditto.loveInterest != maleRattata) {
                throw new AssertionError("Ditto failed to pair with male mate: " + ditto.loveInterest);
            }
            System.out.println("PASS: Ditto pairs with male partner");

            // Test 3: Ditto with Ditto (should NOT pair)
            Pokemon ditto2 = new Pokemon("ditto", 10, Pokemon.Generation.CRYSTAL, false);
            ditto2.gender = "unknown";
            ditto2.position = new Vector2(16, 16);
            ditto2.mapTiles = this.map.tiles;

            this.map.pokemon.clear();
            this.map.pokemon.put(ditto.position, ditto);
            this.map.pokemon.put(ditto2.position, ditto2);

            ditto.checkHabitat(this);
            if (ditto.loveInterest != null) {
                throw new AssertionError("Ditto paired with another Ditto!");
            }
            System.out.println("PASS: Double Ditto cannot breed");

            // Test 4: nearbyEggs only counts actual eggs, not adult Pokemon
            this.map.pokemon.clear();
            this.map.pokemon.put(femaleRattata.position, femaleRattata);
            this.map.pokemon.put(maleRattata.position, maleRattata);
            this.map.pokemon.put(ditto.position, ditto);
            this.map.pokemon.put(ditto2.position, ditto2);

            femaleRattata.checkHabitat(this);
            if (femaleRattata.nearbyEggs != 0) {
                throw new AssertionError("nearbyEggs counted adult Pokemon: " + femaleRattata.nearbyEggs);
            }
            System.out.println("PASS: nearbyEggs correctly ignores adult Pokemon");

            // Test 5: Egg laying
            this.map.pokemon.clear();
            femaleRattata.position = new Vector2(0, 0);
            maleRattata.position = new Vector2(16, 0);
            this.map.pokemon.put(femaleRattata.position, femaleRattata);
            this.map.pokemon.put(maleRattata.position, maleRattata);

            femaleRattata.checkHabitat(this);
            femaleRattata.layEggTimer = 0;
            femaleRattata.drawLower = femaleRattata.new DrawLower();
            femaleRattata.drawLower.step(this);

            boolean eggFound = false;
            Pokemon foundEgg = null;
            for (Pokemon p : this.map.pokemon.values()) {
                if (p.isEgg) {
                    eggFound = true;
                    foundEgg = p;
                    break;
                }
            }
            if (!eggFound || foundEgg == null) {
                throw new AssertionError("Egg was not laid into map.pokemon!");
            }
            if (!"egg".equals(foundEgg.nickname)) {
                throw new AssertionError("Egg nickname is not 'egg': " + foundEgg.nickname);
            }
            if (!"rattata".equals(foundEgg.specie.name)) {
                throw new AssertionError("Egg species is not rattata: " + foundEgg.specie.name);
            }
            System.out.println("PASS: Egg laid on adjacent tile with correct species and egg properties: " + foundEgg.position);

            // Test 6: nearbyEggs detects the new egg
            femaleRattata.checkHabitat(this);
            if (femaleRattata.nearbyEggs != 1) {
                throw new AssertionError("nearbyEggs should be 1 after egg laid, got: " + femaleRattata.nearbyEggs);
            }
            System.out.println("PASS: nearbyEggs counts the laid egg");

            System.out.println("ALL BREEDING MECHANICS CHECKS PASSED!");
            Gdx.app.exit();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c = new Lwjgl3ApplicationConfiguration();
        c.setTitle("Breeding test");
        c.setInitialVisible(false);
        c.setWindowedMode(640, 576);
        c.setForegroundFPS(60);
        c.setIdleFPS(60);
        c.disableAudio(true);
        new Lwjgl3Application(new BreedingMechanicsTest(args), c);
    }
}
