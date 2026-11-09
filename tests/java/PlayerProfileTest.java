package com.pkmngen.game;

import java.nio.file.*;

public final class PlayerProfileTest {
    public static void main(String[] args) throws Exception {
        Path file=Paths.get(args[0],"appearance.json");
        PlayerProfile profile=new PlayerProfile();profile.name="PersistentPlayerName24";profile.character="kris";
        profile.color=0x34bc67ff;profile.skinColor=0xe6bd92ff;
        profile.write(file);
        if(!profile.same(PlayerProfile.read(file))) throw new AssertionError("Appearance/name did not round trip");
        byte[] valid=Files.readAllBytes(file);profile.character="../invalid";
        try { profile.write(file);throw new AssertionError("Unsafe character accepted"); } catch(java.io.IOException expected) {}
        if(!java.util.Arrays.equals(valid,Files.readAllBytes(file))) throw new AssertionError("Invalid update replaced profile");
        Files.write(file,"{".getBytes("UTF-8"));
        if(PlayerProfile.read(file)!=null) throw new AssertionError("Corrupt preferences accepted");
        System.out.println("PASS persistent name, character, clothing/skin colors and atomic profile validation");
    }
}
