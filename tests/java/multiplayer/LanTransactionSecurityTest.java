package multiplayer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pkmngen.game.multiplayer.LanSession;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static multiplayer.LanSecurityTest.*;

public final class LanTransactionSecurityTest {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "duplicate".equals(args[0])) {
            duplicateOutstandingRemoteIdDisconnectsOnlyOffender();
            System.out.println("PASS duplicateOutstandingRemoteIdDisconnectsOnlyOffender");
        }
        if (args.length == 0) {
            malformedTransactionsAreIsolated();
            System.out.println("PASS malformedTransactionsAreIsolated");
            transactionActorIsSocketBound();
            System.out.println("PASS transactionActorIsSocketBound");
            for (String mode : new String[] {"unsolicited", "wrong-id", "wrong-key", "incomplete", "duplicate-key", "revision", "payload", "owner", "interleaved", "mixed"}) invalidAck(mode);
            System.out.println("PASS fakeHostCannotForgeOrMismatchTransactionResults (10 cases)");
        }
    }

    private static void duplicateOutstandingRemoteIdDisconnectsOnlyOffender() throws Exception {
        try (LanSession host = host(); LanSession good = LanSession.join("127.0.0.1", host.port(), "good"); Socket raw = socket(host.port())) {
            String identity = rawJoin(raw);
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            Object connection;
            synchronized (field(host, "lock")) { connection = ((Map<?, ?>) field(host, "clients")).get(identity); }
            blockWriter(connection, entered, release);
            try {
                send(raw, tx("same-id", "x", 0, "x").toString());
                await(() -> host.resources().containsKey("x"), "first transaction applied");
                send(raw, tx("same-id", "y", 0, "y").toString());
                await(() -> !host.peers().containsKey(identity), "duplicate outstanding ID evicts offender");
                check(!host.resources().containsKey("y"), "duplicate ID cannot mutate again");
                check(good.isConnected(), "duplicate offender isolated");
            } finally { release.countDown(); }
        }
    }

    private static void malformedTransactionsAreIsolated() throws Exception {
        try (LanSession host = host(); LanSession good = LanSession.join("127.0.0.1", host.port(), "good")) {
            JsonObject[] bad = {tx("bad id", "x", 0, "x"), tx("bad", "", 0, "x"), tx("bad", "x", -1, "x"), tx("bad", "x", 0, "x"), tx("bad", "x", 0, "x"), tx("bad", "x", 0, "x")};
            bad[3].addProperty("actor", host.id());
            bad[4].addProperty("batch", "[{\"type\":\"ENTRY\",\"key\":\"x\",\"revision\":0,\"json\":null,\"key\":\"y\"}]");
            bad[5].addProperty("batch", "[]");
            for (JsonObject packet : bad) {
                try (Socket raw = socket(host.port())) { rawJoin(raw); send(raw, packet.toString()); expectEof(raw, "malformed TX"); }
            }
            try (Socket raw = socket(host.port())) { send(raw, tx("not-joined", "x", 0, "x").toString()); expectEof(raw, "TX before HELLO"); }
            await(() -> host.peers().size() == 2 && good.peers().size() == 2, "bad TX clients removed");
            check(host.resources().isEmpty() && good.isConnected(), "malformed transactions isolated");
        }
    }

    private static void transactionActorIsSocketBound() throws Exception {
        AtomicReference<String> actorSeen = new AtomicReference<>();
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "", Collections.emptyMap(),
                (actor, pose, key, previous, proposed) -> { actorSeen.set(actor); return !key.equals(actor); });
             Socket raw = socket(host.port())) {
            String identity = rawJoin(raw);
            send(raw, posePacket(host.id(), "forged", "2", "3", "-1"));
            send(raw, tx("bound", identity, 0, "x").toString());
            JsonObject ack;
            do { ack = read(raw); } while (!"TX_ACK".equals(ack.get("type").getAsString()));
            check(!ack.get("accepted").getAsBoolean() && identity.equals(actorSeen.get()), "transaction actor socket-bound");
            check(host.resources().isEmpty(), "bound actor policy denial");
        }
    }

    private static void invalidAck(String mode) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch ready = new CountDownLatch(1), done = new CountDownLatch(1);
        try (ServerSocket server = new ServerSocket(0)) {
            Thread fixture = new Thread(() -> {
                try (Socket socket = server.accept()) {
                    read(socket); snapshot(socket); ready.await();
                    if (!"unsolicited".equals(mode)) {
                        JsonObject request;
                        do { request = read(socket); } while (!"TX".equals(request.get("type").getAsString()));
                    }
                    JsonObject ack = ack("request", "a", true, 1, "new", false);
                    if ("unsolicited".equals(mode) || "wrong-id".equals(mode)) ack.addProperty("id", "someone-else");
                    if ("wrong-key".equals(mode)) ack.addProperty("key", "not-requested");
                    if ("incomplete".equals(mode)) ack.addProperty("last", true);
                    if ("revision".equals(mode)) ack.addProperty("revision", 2);
                    if ("payload".equals(mode)) ack.addProperty("json", "forged");
                    if ("owner".equals(mode)) ack.addProperty("actor", UUID.randomUUID().toString());
                    send(socket, ack.toString());
                    if ("duplicate-key".equals(mode)) send(socket, ack.toString());
                    if ("mixed".equals(mode)) send(socket, ack("request", "b", false, 0, null, true).toString());
                    if ("interleaved".equals(mode)) send(socket, "{\"type\":\"PING\"}");
                    done.await();
                } catch (Throwable error) { failure.set(error); }
            }, "fake-transaction-host-" + mode);
            fixture.start();
            try (LanSession guest = LanSession.join("127.0.0.1", server.getLocalPort(), "guest")) {
                if (!"unsolicited".equals(mode)) {
                    java.util.LinkedHashMap<String, Long> expected = new java.util.LinkedHashMap<>(); expected.put("a", 0L); expected.put("b", 0L);
                    java.util.LinkedHashMap<String, String> proposed = new java.util.LinkedHashMap<>(); proposed.put("a", "new"); proposed.put("b", "new");
                    check(guest.transact("request", expected, proposed), "admitted request");
                }
                ready.countDown();
                await(() -> !guest.isConnected(), "invalid ACK " + mode + " disconnects");
                check(guest.resources().isEmpty() && guest.drainTransactions().isEmpty(), "invalid ACK cannot change state/deliver results");
                check(guest.failure() != null, "invalid ACK actionable failure");
            } finally { ready.countDown(); done.countDown(); fixture.join(5000); }
            check(!fixture.isAlive() && failure.get() == null, "fake ACK host completed: " + failure.get());
        }
    }

    static void snapshot(Socket socket) throws Exception {
        String self = UUID.randomUUID().toString(), host = UUID.randomUUID().toString();
        send(socket, "{\"type\":\"WELCOME\",\"version\":1,\"id\":\"" + self + "\",\"hostId\":\"" + host
            + "\",\"bootstrapLength\":0,\"resourceCount\":0,\"peerCount\":2}");
        send(socket, posePacket(self, "guest", "0", "0", "-1")); send(socket, posePacket(host, "host", "0", "0", "-1"));
        send(socket, "{\"type\":\"READY\"}");
    }
    static JsonObject tx(String id, String key, long revision, String json) {
        JsonObject entry = new JsonObject(); entry.addProperty("type", "ENTRY"); entry.addProperty("key", key);
        entry.addProperty("revision", revision); if (json == null) entry.add("json", com.google.gson.JsonNull.INSTANCE); else entry.addProperty("json", json);
        JsonArray batch = new JsonArray(); batch.add(entry);
        JsonObject packet = new JsonObject(); packet.addProperty("type", "TX"); packet.addProperty("id", id); packet.addProperty("batch", batch.toString()); return packet;
    }
    static JsonObject ack(String id, String key, boolean accepted, long revision, String json, boolean last) {
        JsonObject packet = new JsonObject(); packet.addProperty("type", "TX_ACK"); packet.addProperty("id", id);
        packet.addProperty("key", key); packet.addProperty("accepted", accepted); packet.addProperty("revision", revision);
        if (json == null) packet.add("json", com.google.gson.JsonNull.INSTANCE); else packet.addProperty("json", json);
        packet.addProperty("last", last); return packet;
    }
    static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    static void blockWriter(Object connection, CountDownLatch entered, CountDownLatch release) throws Exception {
        Class<?> outbound = Class.forName("com.pkmngen.game.multiplayer.LanSession$Outbound");
        Object blocker = java.lang.reflect.Proxy.newProxyInstance(outbound.getClassLoader(), new Class<?>[] {outbound},
            (proxy, method, arguments) -> {
                entered.countDown(); try { release.await(); } catch (InterruptedException interrupted) { throw new java.io.IOException(interrupted); } return null;
            });
        @SuppressWarnings("unchecked") ArrayBlockingQueue<Object> queue = (ArrayBlockingQueue<Object>) field(connection, "queue");
        check(queue.offer(blocker) && entered.await(5, java.util.concurrent.TimeUnit.SECONDS), "writer blocked");
    }
}
