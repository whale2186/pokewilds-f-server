package multiplayer;

import com.pkmngen.game.multiplayer.LanSession;
import java.util.Collections;
import java.util.List;

public final class LanInteractionTest {
    public static void main(String[] args) throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "test", Collections.emptyMap(), null);
             LanSession a = LanSession.join("127.0.0.1", host.port(), "same");
             LanSession b = LanSession.join("127.0.0.1", host.port(), "same")) {
            await(() -> a.peers().size() == 3, "roster");
            check(a.message(b.id(), "invite"), "admit routed message");
            List<LanSession.Message> inbox = receive(b);
            check(inbox.size() == 1 && inbox.get(0).sender.equals(a.id()) && inbox.get(0).payload.equals("invite"), "authenticated routing");
            check(host.drainMessages().isEmpty() && a.drainMessages().isEmpty(), "only recipient receives message");
            b.message(host.id(), "accept");
            check(receive(host).get(0).sender.equals(b.id()), "guest to authority");
            host.message(a.id(), "turn");
            check(receive(a).get(0).sender.equals(host.id()), "authority to guest");
            check(!a.message("forged", "bad"), "reject invalid recipient");
            check(!a.message(b.id(), new String(new char[32769])), "payload bound");
            host.setCommitListener(updates -> { throw new java.io.IOException("disk full"); });
            a.transact("failed-decision", Collections.singletonMap("trade:test", 0L), Collections.singletonMap("trade:test", "commit"));
            await(() -> a.resource("trade:test") == null && !a.drainTransactions().isEmpty(), "failed decision response");
            check(host.resource("trade:test") == null, "failed persistence cannot commit");
            final boolean[] durable = {false};
            host.setCommitListener(updates -> durable[0] = true);
            a.transact("decision", Collections.singletonMap("trade:test", 0L), Collections.singletonMap("trade:test", "commit"));
            await(() -> b.resource("trade:test") != null, "commit broadcast");
            check(durable[0], "persist before broadcast");
        }
        System.out.println("PASS interaction routing and durable decisions");
    }
    interface Condition { boolean test(); }
    static void await(Condition test, String name) throws Exception {
        long end = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < end) { if (test.test()) return; Thread.sleep(10); }
        throw new AssertionError(name);
    }
    static List<LanSession.Message> receive(LanSession session) throws Exception {
        long end = System.nanoTime() + 5_000_000_000L;
        while (System.nanoTime() < end) {
            List<LanSession.Message> messages = session.drainMessages();
            if (!messages.isEmpty()) return messages;
            Thread.sleep(10);
        }
        throw new AssertionError("message timeout");
    }
    static void check(boolean condition, String name) { if (!condition) throw new AssertionError(name); }
}
