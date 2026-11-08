package com.pkmngen.game;

import java.nio.file.Files;
import java.nio.file.Path;

public final class LanIdentityTest {
    public static void main(String[] args) throws Exception {
        Path root = java.nio.file.Paths.get(args[0]);
        Path a = root.resolve("host-a/default.sav/coop-world-id.txt");
        Path b = root.resolve("host-b/default.sav/coop-world-id.txt");
        String first = LanIdentity.loadOrCreate(a);
        if (!first.equals(LanIdentity.loadOrCreate(a))) throw new AssertionError("World identity changed on reload");
        if (first.equals(LanIdentity.loadOrCreate(b))) throw new AssertionError("Unrelated same-name hosts share identity");
        Path damaged = root.resolve("broken-id.txt"); Files.write(damaged, "../invalid".getBytes("UTF-8"));
        try { LanIdentity.loadOrCreate(damaged); throw new AssertionError("Corrupt identity accepted/replaced"); }
        catch (java.io.IOException expected) { }
        if (!new String(Files.readAllBytes(damaged), "UTF-8").equals("../invalid")) throw new AssertionError("User identity overwritten");
        Path profile = Files.createDirectories(root.resolve("profile"));
        String legacy = LanIdentity.loadOrCreate(profile.resolve("coop-profile-Alice.txt"));
        if (!legacy.equals(LanIdentity.player(profile, "Bob"))) throw new AssertionError("Rename lost legacy identity");
        if (!legacy.equals(LanIdentity.player(profile, "Carol"))) throw new AssertionError("Rename changed stable identity");
        Path fresh = Files.createDirectories(root.resolve("fresh-profile"));
        String id = LanIdentity.player(fresh, "Alice");
        if (!id.equals(LanIdentity.player(fresh, "Renamed"))) throw new AssertionError("New profile depends on name");
        Path ambiguous = Files.createDirectories(root.resolve("ambiguous-profile"));
        LanIdentity.loadOrCreate(ambiguous.resolve("coop-profile-A.txt"));
        LanIdentity.loadOrCreate(ambiguous.resolve("coop-profile-B.txt"));
        try { LanIdentity.player(ambiguous, "C"); throw new AssertionError("Ambiguous profile silently selected"); }
        catch (java.io.IOException expected) { }
        System.out.println("PASS persisted distinct world/player identities and corrupt-file refusal");
    }
}
