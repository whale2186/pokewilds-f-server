package multiplayer;

import com.pkmngen.game.multiplayer.LanSession;
import com.pkmngen.game.multiplayer.LanSession.TransactionResult;
import com.pkmngen.game.multiplayer.LanSession.Value;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static multiplayer.LanSecurityTest.*;

/** Atomic gameplay protocol fixtures over real sockets, not graphical E2E. */
public final class LanTransactionTest {
    public static void main(String[] args) throws Exception {
        acceptedGroupReplicatesAndReturnsPrivateResult();
        System.out.println("PASS acceptedGroupReplicatesAndReturnsPrivateResult");
        conflictingPickupHasOneWholeWinner();
        System.out.println("PASS conflictingPickupHasOneWholeWinner");
        policyDenialReturnsEveryCurrentValueIncludingAbsent();
        System.out.println("PASS policyDenialReturnsEveryCurrentValueIncludingAbsent");
        readersNeverObservePartialCommitAndLateJoinConverges();
        System.out.println("PASS readersNeverObservePartialCommitAndLateJoinConverges");
        largeRejectedValuesAreChunkedAtomically();
        System.out.println("PASS largeRejectedValuesAreChunkedAtomically");
        localInputsAreBoundedAndDefensivelyCopied();
        System.out.println("PASS localInputsAreBoundedAndDefensivelyCopied");
        closedSessionCannotAdmitTransactions();
        System.out.println("PASS closedSessionCannotAdmitTransactions");
    }

    private static void acceptedGroupReplicatesAndReturnsPrivateResult() throws Exception {
        try (LanSession host = host(); LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest");
             LanSession observer = LanSession.join("127.0.0.1", host.port(), "observer")) {
            check(guest.transact("pickup-1", expected(0, "tile", "pokemon"), changes("tile", "cleared", "pokemon", null)), "admitted transaction");
            TransactionResult result = result(guest, "pickup-1");
            check(result.accepted && result.authoritative.size() == 2 && result.authoritative.get("pokemon").revision == 1, "accepted all result keys/tombstone");
            await(() -> observer.resources().size() == 2, "atomic publication observer");
            check(host.resources().size() == 2 && "cleared".equals(observer.resources().get("tile").json), "all group values");
            check(host.drainTransactions().isEmpty() && observer.drainTransactions().isEmpty() && guest.drainTransactions().isEmpty(), "result requester-only and drained");
            try { result.authoritative.clear(); throw new AssertionError("mutable result"); } catch (UnsupportedOperationException expected) { }
            try { guest.drainTransactions().clear(); throw new AssertionError("mutable drained map"); } catch (UnsupportedOperationException expected) { }
        }
    }

    private static void conflictingPickupHasOneWholeWinner() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", changes("tile", "occupied", "pokemon", "wild"), null);
             LanSession a = LanSession.join("127.0.0.1", host.port(), "a"); LanSession b = LanSession.join("127.0.0.1", host.port(), "b")) {
            CountDownLatch start = new CountDownLatch(1);
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread first = contender(a, "a", start, failure), second = contender(b, "b", start, failure);
            first.start(); second.start(); start.countDown(); first.join(5000); second.join(5000);
            check(!first.isAlive() && !second.isAlive() && failure.get() == null, "concurrent contenders completed: " + failure.get());
            TransactionResult ar = result(a, "pickup"), br = result(b, "pickup");
            check(ar.accepted != br.accepted && a.rejectedMutations() + b.rejectedMutations() == 1, "one whole winner and one rejection");
            String winner = ar.accepted ? "a" : "b";
            Map<String, Value> snapshot = host.resources();
            check(winner.equals(snapshot.get("tile").json) && winner.equals(snapshot.get("pokemon").json), "no mixed winning resource owner");
            check(snapshot.get("tile").revision == 2 && snapshot.get("pokemon").revision == 2, "exactly one revision advance");
            for (TransactionResult answer : new TransactionResult[] {ar, br}) {
                check(answer.authoritative.size() == 2 && winner.equals(answer.authoritative.get("tile").json)
                        && winner.equals(answer.authoritative.get("pokemon").json), "both results include current full group");
            }
        }
    }
    private static Thread contender(LanSession session, String owner, CountDownLatch start, AtomicReference<Throwable> failure) {
        return new Thread(() -> {
            try { start.await(); check(session.transact("pickup", expected(1, "tile", "pokemon"), changes("tile", owner, "pokemon", owner)), "pickup admitted"); }
            catch (Throwable error) { failure.set(error); }
        }, "transaction-contender-" + owner);
    }

    private static void policyDenialReturnsEveryCurrentValueIncludingAbsent() throws Exception {
        java.util.Set<String> evaluated = Collections.synchronizedSet(new java.util.HashSet<String>());
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", changes("existing", "old"),
                (actor, pose, key, previous, proposed) -> { evaluated.add(key); return !key.equals("denied"); });
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            Map<String, Long> expected = expected(0, "denied", "absent", "existing"); expected.put("existing", 1L);
            check(guest.transact("deny", expected, changes("denied", "x", "absent", "x", "existing", "new")), "denied request admitted");
            TransactionResult denied = result(guest, "deny");
            check(!denied.accepted && guest.rejectedMutations() == 1 && evaluated.size() == 3, "whole denial evaluates all policies");
            check(denied.authoritative.size() == 3 && denied.authoritative.get("absent").revision == 0
                && denied.authoritative.get("absent").json == null && denied.authoritative.get("denied").revision == 0, "all absent rejected creates returned");
            check("old".equals(denied.authoritative.get("existing").json) && host.resources().size() == 1 && guest.resources().size() == 1, "denial leaves every resource untouched");
            check(host.transact("host-deny", expected, changes("denied", "x", "absent", "x", "existing", "new")), "host request admitted");
            check(!result(host, "host-deny").accepted && host.rejectedMutations() == 1, "host denial counted once");
        }
    }

    private static void readersNeverObservePartialCommitAndLateJoinConverges() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", changes("a", "0", "b", "0"), null);
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            AtomicBoolean stop = new AtomicBoolean(); AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread observer = new Thread(() -> {
                try {
                    while (!stop.get()) {
                        for (LanSession session : new LanSession[] {host, guest}) {
                            Map<String, Value> snapshot = session.resources();
                            check(snapshot.get("a").revision == snapshot.get("b").revision
                                && snapshot.get("a").json.equals(snapshot.get("b").json), "no partial transaction visible");
                        }
                        Thread.yield();
                    }
                } catch (Throwable error) { failure.set(error); }
            }, "atomic-snapshot-observer");
            observer.start();
            try {
                for (int i = 1; i <= 100; i++) {
                    check(host.transact("update-" + i, expected(i, "a", "b"), changes("a", "" + i, "b", "" + i)), "atomic host write");
                    check(result(host, "update-" + i).accepted, "host result accepted");
                    if (i == 50) try (LanSession late = LanSession.join("127.0.0.1", host.port(), "late")) {
                        check(late.resources().get("a").revision == 51 && "50".equals(late.resources().get("b").json), "transaction late snapshot");
                    }
                }
                await(() -> guest.resources().get("a").revision == 101, "atomic guest final revision");
            } finally { stop.set(true); observer.join(5000); }
            check(!observer.isAlive() && failure.get() == null, "atomic snapshot observer passed: " + failure.get());
        }
    }

    private static void largeRejectedValuesAreChunkedAtomically() throws Exception {
        String payload = repeat("x", 65536); Map<String, String> initial = new LinkedHashMap<>();
        Map<String, Long> expected = new LinkedHashMap<>(); Map<String, String> proposed = new LinkedHashMap<>();
        for (int i = 0; i < 20; i++) { initial.put("k" + i, payload); expected.put("k" + i, 0L); proposed.put("k" + i, null); }
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", initial, null);
             LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            check(guest.transact("large-rollback", expected, proposed), "small stale request admitted");
            TransactionResult answer = result(guest, "large-rollback");
            check(!answer.accepted && answer.authoritative.size() == 20, "rollback above single-frame size complete");
            for (Value value : answer.authoritative.values()) check(value.revision == 1 && payload.equals(value.json), "full large authoritative values");
            check(guest.isConnected() && guest.rejectedMutations() == 1, "large rejection remains connected");
        }
    }

    private static void localInputsAreBoundedAndDefensivelyCopied() throws Exception {
        try (LanSession host = host()) {
            Map<String, Long> one = expected(0, "a"); Map<String, String> change = changes("a", "new");
            invalid(() -> host.transact("", one, change)); invalid(() -> host.transact("bad id", one, change));
            invalid(() -> host.transact(repeat("x", 65), one, change)); invalid(() -> host.transact(null, one, change));
            invalid(() -> host.transact("bad", null, change)); invalid(() -> host.transact("bad", one, null));
            invalid(() -> host.transact("bad", Collections.emptyMap(), Collections.emptyMap()));
            invalid(() -> host.transact("bad", one, changes("b", "x")));
            invalid(() -> host.transact("bad", expected(-1, "a"), change));
            invalid(() -> host.transact("bad", expected(0, ""), changes("", "x")));
            invalid(() -> host.transact("bad", expected(0, "a\n"), changes("a\n", "x")));
            invalid(() -> host.transact("bad", Collections.singletonMap("a", null), change));
            invalid(() -> host.transact("bad", one, changes("a", repeat("x", 65537))));
            Map<String, Long> many = new LinkedHashMap<>(); Map<String, String> manyChanges = new LinkedHashMap<>();
            for (int i = 0; i < 129; i++) { many.put("k" + i, 0L); manyChanges.put("k" + i, "x"); }
            invalid(() -> host.transact("bad", many, manyChanges));
            Map<String, Long> five = expected(0, "a", "b", "c", "d", "e"); Map<String, String> big = new LinkedHashMap<>();
            for (String key : five.keySet()) big.put(key, repeat("\u00e9", 65536));
            invalid(() -> host.transact("bad", five, big));
            check(host.resources().isEmpty(), "invalid requests leave state untouched");
            check(host.transact("copy", one, change), "copied request admitted"); one.put("a", 99L); change.put("a", "mutated");
            check("new".equals(result(host, "copy").authoritative.get("a").json), "caller maps defensively copied");
            check(host.transact("duplicate", expected(1, "a"), changes("a", "next")), "first unique id admitted");
            invalid(() -> host.transact("duplicate", expected(1, "a"), changes("a", "next")));
            check(result(host, "duplicate").accepted, "outstanding ID has one result");
            Map<String, Long> limit = new LinkedHashMap<>(); Map<String, String> limitChanges = new LinkedHashMap<>();
            for (int i = 0; i < 128; i++) { limit.put("limit" + i, 0L); limitChanges.put("limit" + i, "ok"); }
            check(host.transact(repeat("x", 64), limit, limitChanges) && result(host, repeat("x", 64)).accepted, "128 keys and 64-char ID accepted");
        }
    }

    private static void closedSessionCannotAdmitTransactions() throws Exception {
        LanSession host = host(); LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest");
        try {
            guest.close(); check(!guest.transact("closed", expected(0, "a"), changes("a", "x")), "closed guest returns false");
            host.close(); check(!host.transact("closed", expected(0, "a"), changes("a", "x")), "closed host returns false");
            check(host.resources().isEmpty() && host.drainTransactions().isEmpty(), "closed session has no admitted effects");
        } finally { guest.close(); host.close(); }
    }

    static TransactionResult result(LanSession session, String id) throws Exception {
        Map<String, TransactionResult> results = new LinkedHashMap<>();
        await(() -> { results.putAll(session.drainTransactions()); return results.containsKey(id); }, "transaction result " + id);
        return results.get(id);
    }
    static Map<String, Long> expected(long revision, String... keys) {
        Map<String, Long> map = new LinkedHashMap<>(); for (String key : keys) map.put(key, revision); return map;
    }
    static Map<String, String> changes(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>(); for (int i = 0; i < pairs.length; i += 2) map.put(pairs[i], pairs[i + 1]); return map;
    }
    static String repeat(String value, int count) { StringBuilder text = new StringBuilder(); for (int i = 0; i < count; i++) text.append(value); return text.toString(); }
    static void invalid(Action action) throws Exception { try { action.run(); throw new AssertionError("invalid transaction admitted"); } catch (IllegalArgumentException expected) { } }
    interface Action { void run() throws Exception; }
}
