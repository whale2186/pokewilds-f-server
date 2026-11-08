package multiplayer;

import com.pkmngen.game.multiplayer.LanSession;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Callable;

/** Real loopback transport fixtures, not a graphical game E2E. */
public final class LanSessionTest {
    public static void main(String[] args) throws Exception {
        renderAdmissionDoesNotWaitForPolicy();
        System.out.println("PASS renderAdmissionDoesNotWaitForPolicy");
        profilePositionSurvivesReconnect();
        System.out.println("PASS profilePositionSurvivesReconnect");
        snapshotAndDuplicateNames();
        System.out.println("PASS snapshotAndDuplicateNames");
        poseReplicationBindsIdentity();
        System.out.println("PASS poseReplicationBindsIdentity");
        authoritativeCasAndLateJoin();
        System.out.println("PASS authoritativeCasAndLateJoin");
        invalidLocalInputsAreBounded();
        System.out.println("PASS invalidLocalInputsAreBounded");
        chunkedInventoryDoesNotLoseConcurrentWrites();
        System.out.println("PASS chunkedInventoryDoesNotLoseConcurrentWrites");
        largeIslandCapacityIsBounded();
        System.out.println("PASS largeIslandCapacityIsBounded");
        totalInventoryLimitCountsUtf8Bytes();
        System.out.println("PASS totalInventoryLimitCountsUtf8Bytes");
    }

    private static void renderAdmissionDoesNotWaitForPolicy() throws Exception {
        java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(),
                (actor, pose, key, previous, proposed) -> {
                    entered.countDown();
                    try { release.await(5, java.util.concurrent.TimeUnit.SECONDS); }
                    catch (InterruptedException error) { Thread.currentThread().interrupt(); }
                    return true;
                })) {
            host.transact("blocked", Collections.singletonMap("tile", 0L), Collections.singletonMap("tile", "changed"));
            check(entered.await(2, java.util.concurrent.TimeUnit.SECONDS), "policy entered");
            java.util.concurrent.ExecutorService render = java.util.concurrent.Executors.newSingleThreadExecutor();
            try {
                render.submit(() -> {
                    host.drainResourceChanges();
                    for (int x = 0; x < 1000; x++)
                        host.pose(new LanSession.Pose(host.id(), "host", "red", "down", "0,0", -1, x, 0, true));
                }).get(1, java.util.concurrent.TimeUnit.SECONDS);
            } finally { release.countDown(); render.shutdownNow(); }
            await(() -> host.peers().get(host.id()).x == 999, "latest coalesced pose survives policy stall");
            await(() -> host.resource("tile") != null, "transaction completes");
            check("changed".equals(host.drainResourceChanges().get("tile").json), "delta survives policy stall");
        } finally { release.countDown(); }
    }

    private static void profilePositionSurvivesReconnect() throws Exception {
        String profile = "11111111-2222-3333-4444-555555555555";
        Map<String, LanSession.Pose> positions;
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(), null)) {
            String oldId;
            try (LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest", profile)) {
                check(!guest.hasSavedPosition(), "first visit uses world spawn");
                oldId = guest.id();
                guest.pose(new LanSession.Pose(guest.id(), "guest", "gold", "left", "2,3", 7, 123, 456, false));
                await(() -> host.savedPositions().containsKey(profile), "profile pose recorded");
            }
            await(() -> !host.peers().containsKey(oldId), "disconnect processed");
            try (LanSession returning = LanSession.join("127.0.0.1", host.port(), "renamed", profile)) {
                LanSession.Pose pose = returning.peers().get(returning.id());
                check(returning.hasSavedPosition() && pose.x == 123 && pose.y == 456 && pose.layer == 7
                        && pose.mapId.equals("2,3") && pose.direction.equals("left"), "all coordinates restored by UUID");
                check(!oldId.equals(returning.id()) && pose.name.equals("renamed"), "fresh connection identity");
            }
            positions = host.savedPositions();
        }
        try (LanSession restarted = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(), null)) {
            restarted.restorePositions(positions);
            try (LanSession guest = LanSession.join("127.0.0.1", restarted.port(), "guest", profile)) {
                check(guest.hasSavedPosition() && guest.peers().get(guest.id()).x == 123, "restored server snapshot");
            }
        }
    }

    private static void snapshotAndDuplicateNames() throws Exception {
        Map<String, String> initial = Collections.singletonMap("tile:0,0:1,2", "{\"tile\":7}");
        try (LanSession host = LanSession.host("127.0.0.1", 0, "same", "island-fixture", initial,
                (actor, pose, key, previous, proposed) -> true);
             LanSession a = LanSession.join("127.0.0.1", host.port(), "same");
             LanSession b = LanSession.join("127.0.0.1", host.port(), "same")) {
            check(host.isHost() && !a.isHost(), "roles");
            check(host.isConnected() && a.isConnected() && b.isConnected(), "ready after join");
            check("island-fixture".equals(a.bootstrap()), "bootstrap fidelity");
            check(a.resources().get("tile:0,0:1,2").revision == 1, "initial revision");
            check(initial.get("tile:0,0:1,2").equals(a.resources().get("tile:0,0:1,2").json), "resource fidelity");
            check(!a.id().equals(b.id()) && !a.id().equals(host.id()), "server identity despite duplicate names");
            await(() -> a.peers().size() == 3 && b.peers().size() == 3, "complete roster");
            check(a.peers().containsKey(host.id()) && a.peers().containsKey(a.id()), "host and self included");
            try { a.resources().clear(); throw new AssertionError("mutable resources"); }
            catch (UnsupportedOperationException expected) { }
            try { a.peers().clear(); throw new AssertionError("mutable peers"); }
            catch (UnsupportedOperationException expected) { }
        }
    }

    private static void poseReplicationBindsIdentity() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(), null);
             LanSession a = LanSession.join("127.0.0.1", host.port(), "guest");
             LanSession b = LanSession.join("127.0.0.1", host.port(), "observer")) {
            a.pose(new LanSession.Pose(host.id(), "stolen", "leaf", "left", "-2,3", 4, 19.5f, 23, true, true, 0x123456ff, 0x987654ff, "{\"species\":\"pikachu\",\"phase\":1}"));
            await(() -> host.peers().get(a.id()).x == 19.5f && b.peers().get(a.id()).x == 19.5f, "guest pose");
            LanSession.Pose observed = b.peers().get(a.id());
            check("guest".equals(observed.name) && a.id().equals(observed.id), "server-bound identity and name");
            check(observed.layer == 4 && "-2,3".equals(observed.mapId) && observed.moving, "all pose fields");
            check("leaf".equals(observed.character) && observed.running && observed.color == 0x123456ff && observed.skinColor == 0x987654ff, "appearance and sprint survive relay");
            check(observed.visual.contains("pikachu"), "follower and animation state survive relay");
            host.pose(new LanSession.Pose("fake", "fake", "blue", "up", "0,0", -1, 41, 42, false));
            await(() -> a.peers().get(host.id()).x == 41 && b.peers().get(host.id()).y == 42, "host pose");
            Map<String, LanSession.Pose> snapshot = b.peers();
            host.pose(new LanSession.Pose(null, null, "blue", "down", "0,0", -1, 50, 42, false));
            await(() -> b.peers().get(host.id()).x == 50, "later pose");
            check(snapshot.get(host.id()).x == 41, "defensive roster snapshot");
            check(!a.kick(b.id()), "guests cannot kick");
            check(host.kick(a.id()), "authority kicks a peer");
            await(() -> !a.isConnected() && !b.peers().containsKey(a.id()), "kick removes peer and appearance");
            check(b.isConnected(), "kick leaves other players connected");
        }
    }

    private static void authoritativeCasAndLateJoin() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(),
                (actor, pose, key, previous, proposed) -> !key.equals("denied") && pose != null);
             LanSession a = LanSession.join("127.0.0.1", host.port(), "a");
             LanSession b = LanSession.join("127.0.0.1", host.port(), "b")) {
            a.propose("tile", 0, "{\"tile\":2}");
            await(() -> b.resources().containsKey("tile"), "accepted CAS broadcast");
            check(host.resources().get("tile").revision == 1, "first create revision");
            Map<String, LanSession.Value> snapshot = a.resources();
            a.propose("tile", 1, "{\"tile\":3}");
            b.propose("tile", 1, "{\"tile\":4}");
            await(() -> a.rejectedMutations() + b.rejectedMutations() == 1, "stale CAS rejected exactly once");
            await(() -> a.resources().get("tile").revision == 2 && b.resources().get("tile").revision == 2, "authoritative revision converges");
            check(a.resources().get("tile").json.equals(b.resources().get("tile").json), "CAS has one winner");
            check(snapshot.get("tile").revision == 1, "defensive values snapshot");
            a.propose("denied", 0, "{}");
            await(() -> a.rejectedMutations() + b.rejectedMutations() == 2, "policy denial reported");
            check(!host.resources().containsKey("denied"), "policy retains absent state");
            host.propose("tile", 2, null);
            await(() -> a.resources().get("tile").revision == 3 && a.resources().get("tile").json == null, "host tombstone broadcast");
            try (LanSession late = LanSession.join("127.0.0.1", host.port(), "late")) {
                check(late.resources().get("tile").revision == 3 && late.resources().get("tile").json == null, "late join retains tombstone revision");
                late.propose("tile", 0, "{}");
                await(() -> late.rejectedMutations() == 1, "tombstone prevents ABA");
                late.propose("tile", 3, "{\"tile\":5}");
                await(() -> host.resources().get("tile").revision == 4, "recreate from tombstone");
            }
            host.propose("denied", 0, "{}");
            await(() -> host.rejectedMutations() == 1, "host policy denial");
        }
    }

    private static void invalidLocalInputsAreBounded() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(), null)) {
            expectInvalid(() -> host.pose(new LanSession.Pose(null, null, "red", "down", "0,0", -1, Float.NaN, 0, false)), "NaN coordinate");
            expectInvalid(() -> host.pose(new LanSession.Pose(null, null, "red", "down", "0,0", -1, 1000001, 0, false)), "coordinate bound");
            expectInvalid(() -> host.pose(new LanSession.Pose(null, null, "../red", "down", "0,0", -1, 0, 0, false)), "character identifier");
            expectInvalid(() -> host.pose(new LanSession.Pose(null, null, "red", "diagonal", "0,0", -1, 0, 0, false)), "direction enum");
            expectInvalid(() -> host.pose(new LanSession.Pose(null, null, "red", "down", "not-a-map", -1, 0, 0, false)), "map identifier");
            expectInvalid(() -> host.pose(new LanSession.Pose(null, null, "red", "down", "0,0", 256, 0, 0, false)), "layer range");
            expectInvalid(() -> host.propose("", 0, "{}"), "empty key");
            expectInvalid(() -> host.propose("x", -1, "{}"), "negative revision");
            expectInvalid(() -> host.propose("x", 0, repeat("x", 65537)), "resource size bound");
            expectInvalid(() -> LanSession.host("127.0.0.1", 0, "bad\nname", "", Collections.emptyMap(), null), "name controls");
            expectInvalid(() -> LanSession.host("127.0.0.1", 0, "host", "", Collections.singletonMap("", "{}"), null), "initial resource key");
            check(host.isConnected() && host.resources().isEmpty(), "invalid callers do not alter state");
        }
    }

    private static void chunkedInventoryDoesNotLoseConcurrentWrites() throws Exception {
        Map<String, String> fixture = new LinkedHashMap<>();
        String payload = "{\"fixture\":\"" + repeat("a", 512) + "\"}";
        for (int i = 0; i < 2000; i++) fixture.put("tile:" + i, payload);
        fixture.put("hot", "0");
        String bootstrap = repeat("x", 16383) + "\ud83c\udf0d" + repeat("y", 90000);
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", bootstrap, fixture, null)) {
            AtomicReference<Throwable> backgroundFailure = new AtomicReference<>();
            Thread updater = new Thread(() -> {
                try {
                    for (int i = 1; i <= 100; i++) {
                        host.propose("hot", i, Integer.toString(i));
                        final int revision = i + 1;
                        await(() -> host.resources().get("hot").revision == revision, "concurrent host write");
                    }
                } catch (Throwable error) { backgroundFailure.set(error); }
            }, "transport-fixture-updater");
            updater.start();
            try (LanSession guest = LanSession.join("127.0.0.1", host.port(), "late")) {
                check(bootstrap.equals(guest.bootstrap()), "Unicode bootstrap chunk fidelity");
                check(guest.resources().size() == 2001, "complete chunked inventory before ready");
                check(payload.equals(guest.resources().get("tile:1999").json), "inventory tail fidelity");
                updater.join(10000);
                check(!updater.isAlive() && backgroundFailure.get() == null, "updater completed: " + backgroundFailure.get());
                await(() -> guest.resources().get("hot").revision == 101, "no mutation gap during join snapshot");
                check("100".equals(guest.resources().get("hot").json), "final write matches");
            } finally { updater.join(10000); }
        }
    }

    private static void largeIslandCapacityIsBounded() throws Exception {
        Map<String, String> fixture = new LinkedHashMap<>();
        for (int i = 0; i < 200000; i++) fixture.put("capacity-fixture:" + i, "{\"fixture\":0}");
        long begin = System.nanoTime();
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", repeat("x", 500000), fixture, null);
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            check(guest.resources().size() == 200000, "island inventory above legacy 32768 ceiling");
            check(guest.bootstrap().length() == 500000, "large bootstrap");
            check(guest.resources().get("capacity-fixture:199999").revision == 1, "large inventory tail");
            System.out.println("INFO synthetic 200000-resource/500000-char bootstrap setup ms="
                    + java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        for (int i = 200000; i < 524289; i++) fixture.put("capacity-fixture:" + i, "0");
        expectInvalid(() -> LanSession.host("127.0.0.1", 0, "host", "fixture", fixture, null), "524288 resource count bound");
    }

    private static void totalInventoryLimitCountsUtf8Bytes() throws Exception {
        Map<String, String> fixture = new LinkedHashMap<>();
        String payload = "\"" + repeat("\u00e9", 65534) + "\"";
        for (int i = 0; i < 1100; i++) fixture.put("byte-fixture:" + i, payload);
        expectInvalid(() -> {
            try (LanSession unexpected = LanSession.host("127.0.0.1", 0, "host", "fixture", fixture, null)) { }
        }, "128 MiB UTF-8 inventory bound");
    }

    private static String repeat(String text, int count) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < count; i++) value.append(text);
        return value.toString();
    }

    private static void expectInvalid(ThrowingRunnable action, String label) throws Exception {
        try { action.run(); throw new AssertionError("accepted " + label); }
        catch (IllegalArgumentException expected) { }
    }
    private interface ThrowingRunnable { void run() throws Exception; }

    private static void await(Callable<Boolean> condition, String label) throws Exception {
        long until = System.nanoTime() + 8000000000L;
        while (!condition.call() && System.nanoTime() < until) Thread.sleep(10);
        check(condition.call(), "timed out: " + label);
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
