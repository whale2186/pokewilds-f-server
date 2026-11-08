package com.pkmngen.game;

public final class LanOptionsTest {
    private static int checks;
    private static void check(boolean value) { if (!value) throw new AssertionError("check " + checks); checks++; }
    private static void invalid(String... args) { try { LanOptions.parse(args); throw new AssertionError("accepted invalid options"); } catch (IllegalArgumentException expected) { checks++; } }
    public static void main(String[] args) {
        check(LanOptions.parse(new String[]{"dev"}).mode == null);
        LanOptions host = LanOptions.parse(new String[]{"--coop-host=127.0.0.1:54555", "--coop-name=alice", "--coop-new=island", "--coop-size=30000"});
        check(host.mode.equals("host") && host.port == 54555 && host.address.equals("127.0.0.1"));
        check(host.name.equals("alice") && host.world.equals("island") && host.createWorld && host.size == 30000);
        LanOptions guest = LanOptions.parse(new String[]{"--coop-join=localhost:12345", "--coop-name=bob"});
        check(guest.mode.equals("join") && guest.port == 12345);
        LanOptions menu = LanOptions.forJoin("127.0.0.1", "bob");
        check(menu.mode.equals("join") && menu.port == 54555);
        check(LanOptions.forJoin("", "bob").address.equals("127.0.0.1"));
        check(LanOptions.forJoin("localhost:54666", "bob").port == 54666);
        invalid("--coop-host=localhost:0");
        invalid("--coop-host=localhost:65536");
        invalid("--coop-host=localhost:54555", "--coop-join=localhost:54555");
        invalid("--coop-join=localhost:54555", "--coop-world=test");
        invalid("--coop-host=localhost:54555", "--coop-new=../escape");
        invalid("--coop-host=localhost:54555", "--coop-name=");
        invalid("--coop-host=localhost:54555", "--coop-size=1");
        invalid("--coop-host=localhost:54555", "--coop-new=one", "--coop-world=two");
        System.out.println("PASS " + checks + " LAN option checks");
    }
}
