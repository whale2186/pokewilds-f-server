package multiplayer;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.multiplayer.LanSession.TransactionResult;
import com.pkmngen.game.multiplayer.LanSession.Value;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static multiplayer.LanSecurityTest.*;
import static multiplayer.LanTransactionTest.*;
import static multiplayer.LanTransactionSecurityTest.field;
import static multiplayer.LanTransactionSecurityTest.blockWriter;

public final class LanTransactionBoundsTest {
    public static void main(String[] args) throws Exception {
        hostCommandAdmissionFailureHasNoEffects();
        System.out.println("PASS hostCommandAdmissionFailureHasNoEffects");
        guestWriterAdmissionFailureHasNoEffects();
        System.out.println("PASS guestWriterAdmissionFailureHasNoEffects");
        pendingAndResultsReserveCapacityUntilDrained();
        System.out.println("PASS pendingAndResultsReserveCapacityUntilDrained");
        policyCannotBlockTransactionCaller();
        System.out.println("PASS policyCannotBlockTransactionCaller");
        resourceCountLimitRejectsWholeGroup();
        System.out.println("PASS resourceCountLimitRejectsWholeGroup");
        totalUtf8LimitRejectsWholeGroupAndAccountsNetReplacement();
        System.out.println("PASS totalUtf8LimitRejectsWholeGroupAndAccountsNetReplacement");
        revisionOverflowAndPolicyExceptionsRejectSafely();
        System.out.println("PASS revisionOverflowAndPolicyExceptionsRejectSafely");
        await(() -> ownedWorkers() == 0, "transaction workers shut down");
        System.out.println("PASS transactionBoundsNoOwnedWorkerLeaks");
    }

    private static void hostCommandAdmissionFailureHasNoEffects() throws Exception {
        try (LanSession host = host()) {
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            ArrayBlockingQueue<Runnable> queue = commands(host);
            check(queue.offer(() -> { entered.countDown(); waitFor(release); }) && entered.await(5, TimeUnit.SECONDS), "command consumer paused");
            try {
                for (int i = 0; i < 256; i++) check(queue.offer(() -> { }), "command queue full");
                long started = System.nanoTime();
                check(!host.transact("no-room", expected(0, "a"), changes("a", "new")), "command queue failure returns false");
                check(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 500, "command admission nonblocking");
                check(host.resources().isEmpty() && host.drainTransactions().isEmpty(), "failed command admission has no state/result");
                check(((Map<?, ?>) field(host, "pending")).isEmpty(), "failed command admission released reservation");
            } finally { release.countDown(); }
            await(() -> queue.isEmpty(), "command queue drained");
            check(host.transact("no-room", expected(0, "a"), changes("a", "new")) && result(host, "no-room").accepted, "failed admission ID reusable");
        }
    }

    private static void guestWriterAdmissionFailureHasNoEffects() throws Exception {
        try (LanSession host = host(); LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            Object upstream = field(guest, "upstream");
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            blockWriter(upstream, entered, release);
            ArrayBlockingQueue<?> queue = (ArrayBlockingQueue<?>) field(upstream, "queue");
            // Reuse one bounded no-op task after releasing its latch.
            Class<?> outbound = Class.forName("com.pkmngen.game.multiplayer.LanSession$Outbound");
            Object noop = java.lang.reflect.Proxy.newProxyInstance(outbound.getClassLoader(), new Class<?>[] {outbound}, (p, m, a) -> null);
            @SuppressWarnings("unchecked") ArrayBlockingQueue<Object> writable = (ArrayBlockingQueue<Object>) queue;
            try {
                for (int i = 0; i < 256; i++) check(writable.offer(noop), "guest queue full");
                check(!guest.transact("no-room", expected(0, "a"), changes("a", "new")), "guest queue failure returns false");
                check(guest.isConnected() && host.resources().isEmpty() && guest.drainTransactions().isEmpty(), "local admission failure does not disconnect or mutate");
                check(((Map<?, ?>) field(guest, "pending")).isEmpty(), "guest failed admission released reservation");
            } finally { release.countDown(); }
            await(() -> queue.isEmpty(), "guest queue drained");
            check(guest.transact("no-room", expected(0, "a"), changes("a", "new")) && result(guest, "no-room").accepted, "guest queue recovered");
        }
    }

    private static void pendingAndResultsReserveCapacityUntilDrained() throws Exception {
        try (LanSession host = host()) {
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            check(commands(host).offer(() -> { entered.countDown(); waitFor(release); }) && entered.await(5, TimeUnit.SECONDS), "consumer paused");
            try {
                for (int i = 0; i < 256; i++) check(host.transact("id-" + i, expected(0, "k" + i), changes("k" + i, "x")), "reserved pending slot " + i);
                check(!host.transact("overflow", expected(0, "overflow"), changes("overflow", "x")), "pending capacity returns false before gameplay optimism");
            } finally { release.countDown(); }
            await(() -> { synchronized (field(host, "transactionLock")) { return ((Map<?, ?>) field(host, "results")).size() == 256; } }, "all admitted results retained");
            check(!host.transact("overflow", expected(0, "overflow"), changes("overflow", "x")), "undrained results hold capacity");
            Map<String, TransactionResult> results = host.drainTransactions();
            check(results.size() == 256, "every reserved transaction returns a result");
            for (TransactionResult value : results.values()) check(value.accepted && value.authoritative.size() == 1, "retained accepted result");
            check(host.transact("overflow", expected(0, "overflow"), changes("overflow", "x")) && result(host, "overflow").accepted, "drain releases capacity");
        }
    }

    private static void policyCannotBlockTransactionCaller() throws Exception {
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", changes(),
                (actor, pose, key, previous, proposed) -> { if (key.equals("slow")) { entered.countDown(); waitFor(release); } return true; })) {
            check(host.transact("slow", expected(0, "slow"), changes("slow", "x")) && entered.await(5, TimeUnit.SECONDS), "policy running on worker");
            try {
                long started = System.nanoTime();
                check(host.transact("next", expected(0, "next"), changes("next", "x")), "render caller admitted while policy running");
                check(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 500, "render caller never waits for policy lock");
            } finally { release.countDown(); }
            Map<String, TransactionResult> all = new LinkedHashMap<>();
            await(() -> { all.putAll(host.drainTransactions()); return all.size() == 2; }, "policy/next results");
        } finally { release.countDown(); }
    }

    private static void resourceCountLimitRejectsWholeGroup() throws Exception {
        Map<String, String> initial = new LinkedHashMap<>();
        for (int i = 0; i < 524287; i++) initial.put("k" + i, "0");
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", initial, null);
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            check(guest.transact("count-limit", expected(0, "new-a", "new-b"), changes("new-a", "x", "new-b", "x")), "count overflow admitted for authoritative decision");
            TransactionResult denied = result(guest, "count-limit");
            check(!denied.accepted && denied.authoritative.size() == 2 && denied.authoritative.get("new-a").revision == 0
                && denied.authoritative.get("new-b").revision == 0, "count limit returns all absent values");
            check(host.resources().size() == 524287 && guest.resources().size() == 524287, "no partial insert at capacity");
            check(guest.transact("last-slot", expected(0, "new-a"), changes("new-a", "x")) && result(guest, "last-slot").accepted, "last allowed inventory slot works");
        }
    }

    private static void totalUtf8LimitRejectsWholeGroupAndAccountsNetReplacement() throws Exception {
        String payload = repeat("\u00e9", 32768); Map<String, String> initial = new LinkedHashMap<>();
        for (int i = 0; i < 2048; i++) initial.put("k" + i, payload);
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", initial, null);
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            Map<String, Long> revisions = expected(0, "new", "k0"); revisions.put("k0", 1L);
            check(guest.transact("byte-limit", revisions, changes("new", payload, "k0", "x")), "byte overflow request admitted");
            TransactionResult denied = result(guest, "byte-limit");
            check(!denied.accepted && denied.authoritative.get("new").revision == 0 && payload.equals(denied.authoritative.get("k0").json), "UTF-8 net +1 byte rejected atomically");
            check(host.resources().size() == 2048 && guest.resources().get("k0").revision == 1, "byte rejection unchanged");
            // Insertion precedes deletion in iteration; check final net size, not intermediate size.
            check(guest.transact("net-zero", revisions, changes("new", payload, "k0", null)), "net-zero replacement admitted");
            check(result(guest, "net-zero").accepted && guest.resources().get("k0").json == null, "net-zero UTF-8 replacement at 128MiB accepted");
        }
    }

    private static void revisionOverflowAndPolicyExceptionsRejectSafely() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", changes("max", "old"), null)) {
            synchronized (field(host, "lock")) {
                @SuppressWarnings("unchecked") Map<String, Value> values = (Map<String, Value>) field(host, "values");
                values.put("max", new Value(Long.MAX_VALUE, "old"));
            }
            try (LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
                check(guest.transact("max-revision", expected(Long.MAX_VALUE, "max"), changes("max", "new")), "max revision request admitted");
                TransactionResult denied = result(guest, "max-revision");
                check(!denied.accepted && denied.authoritative.get("max").revision == Long.MAX_VALUE, "revision cannot overflow");
            }
        }
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", changes(), (a, p, k, old, next) -> { throw new IllegalStateException("fixture"); });
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            check(guest.transact("exception", expected(0, "a", "b"), changes("a", "x", "b", "y")), "exception request admitted");
            check(!result(guest, "exception").accepted && guest.isConnected() && host.resources().isEmpty(), "policy exception becomes atomic rejection");
        }
    }

    @SuppressWarnings("unchecked") private static ArrayBlockingQueue<Runnable> commands(LanSession host) throws Exception { return (ArrayBlockingQueue<Runnable>) field(host, "commands"); }
    private static void waitFor(CountDownLatch latch) { try { latch.await(); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); } }
    private static int ownedWorkers() { int count = 0; for (Thread thread : Thread.getAllStackTraces().keySet()) if (thread.isAlive() && thread.getName().startsWith("LanSession-")) count++; return count; }
}
