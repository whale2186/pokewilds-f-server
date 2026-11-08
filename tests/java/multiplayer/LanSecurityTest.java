package multiplayer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pkmngen.game.multiplayer.LanSession;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/** Adversarial packets are explicit transport fixtures, never game assets. */
public final class LanSecurityTest {
    public static void main(String[] args) throws Exception {
        malformedPeersAreIsolated();
        System.out.println("PASS malformedPeersAreIsolated");
        socketIdentityCannotBeStolen();
        System.out.println("PASS socketIdentityCannotBeStolen");
        incompleteSnapshotCannotBecomeReady();
        System.out.println("PASS incompleteSnapshotCannotBecomeReady");
        readyRequiresVersionedInventoryManifest();
        System.out.println("PASS readyRequiresVersionedInventoryManifest");
        connectionResetsCannotStopListener();
        System.out.println("PASS connectionResetsCannotStopListener");
        handshakePrecedesHeartbeats();
        System.out.println("PASS handshakePrecedesHeartbeats");
    }

    private static void malformedPeersAreIsolated() throws Exception {
        String[] badHello = {
            "{\"type\":\"CAS\",\"key\":\"bad\",\"revision\":0,\"json\":\"{}\"}",
            "{\"type\":\"HELLO\",\"version\":2,\"name\":\"x\"}",
            "{\"type\":\"HELLO\",\"version\":\"1\",\"name\":\"x\"}",
            "{\"type\":\"HELLO\",\"version\":1,\"name\":\"x\",\"actor\":\"host\"}",
            "{\"type\":\"HELLO\",\"version\":1,\"name\":\"x\",\"name\":\"y\"}",
            "{\"type\":\"HELLO\",\"version\":1,\"name\":{\"class\":\"java.lang.Runtime\"}}",
            "{\"type\":\"HELLO\",\"version\":1,\"name\":\"x\"} {}",
            "{type:'HELLO',version:1,name:'x'}"
        };
        try (LanSession host = host(); LanSession good = LanSession.join("127.0.0.1", host.port(), "good")) {
            for (String packet : badHello) {
                try (Socket attacker = socket(host.port())) {
                    send(attacker, packet);
                    expectEof(attacker, "malformed handshake");
                }
            }
            for (int size : new int[] { -1, 0, 1, 524289, Integer.MAX_VALUE }) {
                try (Socket attacker = socket(host.port())) {
                    new DataOutputStream(attacker.getOutputStream()).writeInt(size);
                    expectEof(attacker, "oversized/invalid length " + size);
                }
            }
            try (Socket attacker = socket(host.port())) {
                DataOutputStream output = new DataOutputStream(attacker.getOutputStream());
                output.writeInt(2); output.write(new byte[] {(byte) 0xc3, (byte) 0x28}); output.flush();
                expectEof(attacker, "invalid UTF-8");
            }
            String[] badJoined = {
                "{\"type\":\"BOOT\",\"data\":\"replace world\"}",
                "{\"type\":\"CAS\",\"key\":\"x\",\"revision\":-1,\"json\":null}",
                "{\"type\":\"CAS\",\"key\":\"x\",\"revision\":0.5,\"json\":null}",
                "{\"type\":\"CAS\",\"key\":\"x\",\"revision\":9223372036854775808,\"json\":null}",
                "{\"type\":\"CAS\",\"key\":\"x\",\"revision\":0e2147483647,\"json\":null}",
                "{\"type\":\"CAS\",\"key\":\"x\",\"revision\":0,\"json\":{},\"actor\":\"host\"}",
                posePacket("id", "bad", "1e99", "0", "-1"),
                posePacket("id", "bad", "0", "0", "4294967295")
            };
            for (String packet : badJoined) {
                try (Socket attacker = socket(host.port())) {
                    rawJoin(attacker);
                    send(attacker, packet);
                    expectEof(attacker, "malformed joined packet");
                }
            }
            await(() -> host.peers().size() == 2 && good.peers().size() == 2, "offenders removed");
            check(host.isConnected() && good.isConnected(), "healthy connections survive malformed peers");
            check(host.resources().isEmpty() && "fixture".equals(good.bootstrap()), "no unauthenticated mutation/bootstrap change");
        }
    }

    private static void socketIdentityCannotBeStolen() throws Exception {
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(),
                (actor, pose, key, previous, proposed) -> !actor.equals(key));
             LanSession observer = LanSession.join("127.0.0.1", host.port(), "observer");
             Socket attacker = socket(host.port())) {
            String identity = rawJoin(attacker);
            send(attacker, posePacket(host.id(), "stolen-name", "77", "88", "-1"));
            await(() -> host.peers().get(identity).x == 77 && observer.peers().get(identity).y == 88, "socket-bound pose");
            check(host.peers().get(host.id()).x == 0 && "raw".equals(host.peers().get(identity).name), "host identity unchanged");
            send(attacker, "{\"type\":\"CAS\",\"key\":\"" + identity + "\",\"revision\":0,\"json\":\"{}\"}");
            JsonObject response;
            do { response = read(attacker); } while (!"REJECT".equals(response.get("type").getAsString()));
            check(!host.resources().containsKey(identity), "policy actor bound to socket");
        }
    }

    private static void incompleteSnapshotCannotBecomeReady() throws Exception {
        try (ServerSocket fake = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            AtomicReference<Throwable> serverFailure = new AtomicReference<>();
            Thread server = new Thread(() -> {
                try (Socket peer = fake.accept()) {
                    read(peer);
                    send(peer, "{\"type\":\"WELCOME\",\"version\":1,\"id\":\"fixture-id\"}");
                    send(peer, "{\"type\":\"READY\"}");
                } catch (Throwable error) { serverFailure.set(error); }
            }, "malicious-host-fixture");
            server.start();
            try {
                LanSession unexpected = LanSession.join("127.0.0.1", fake.getLocalPort(), "guest");
                unexpected.close(); throw new AssertionError("incomplete snapshot was ready");
            } catch (IOException expected) { check(expected.getMessage().contains("LAN"), "actionable setup failure"); }
            server.join(5000);
            check(!server.isAlive() && serverFailure.get() == null, "fake server completed");
        }
    }

    private static void readyRequiresVersionedInventoryManifest() throws Exception {
        try (ServerSocket fake = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            java.util.concurrent.CountDownLatch finished = new java.util.concurrent.CountDownLatch(1);
            Thread server = new Thread(() -> {
                try (Socket peer = fake.accept()) {
                    read(peer);
                    send(peer, "{\"type\":\"WELCOME\",\"version\":1,\"id\":\"self\"}");
                    send(peer, posePacket("self", "fixture", "0", "0", "-1"));
                    send(peer, "{\"type\":\"READY\"}");
                    finished.await(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (Exception ignored) { }
            }, "incomplete-manifest-fixture");
            server.start();
            try {
                try (LanSession unexpected = LanSession.join("127.0.0.1", fake.getLocalPort(), "guest")) {
                    throw new AssertionError("ready without versioned inventory manifest/host roster");
                } catch (IOException expected) { }
            } finally { finished.countDown(); server.join(5000); }
        }
    }

    private static void connectionResetsCannotStopListener() throws Exception {
        try (LanSession host = host()) {
            for (int i = 0; i < 200; i++) {
                try (Socket reset = socket(host.port())) { reset.setSoLinger(true, 0); }
                Thread.sleep(2);
            }
            try (LanSession healthy = LanSession.join("127.0.0.1", host.port(), "healthy")) {
                check(host.isConnected() && healthy.isConnected(), "resetting clients cannot kill listener");
            }
        }
    }

    private static void handshakePrecedesHeartbeats() throws Exception {
        try (LanSession host = host(); Socket delayed = socket(host.port())) {
            Thread.sleep(1250);
            send(delayed, "{\"type\":\"HELLO\",\"version\":1,\"name\":\"delayed\"}");
            check("WELCOME".equals(read(delayed).get("type").getAsString()), "WELCOME must precede heartbeat");
        }
    }

    static LanSession host() throws Exception {
        return LanSession.host("127.0.0.1", 0, "host", "fixture", Collections.emptyMap(), null);
    }
    static Socket socket(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port); socket.setSoTimeout(5000); return socket;
    }
    static String rawJoin(Socket socket) throws Exception {
        send(socket, "{\"type\":\"HELLO\",\"version\":1,\"name\":\"raw\"}");
        String identity = null;
        while (true) {
            JsonObject frame = read(socket);
            String type = frame.get("type").getAsString();
            if ("WELCOME".equals(type)) identity = frame.get("id").getAsString();
            if ("READY".equals(type)) return identity;
        }
    }
    static String posePacket(String id, String name, String x, String y, String layer) {
        return "{\"type\":\"PEER\",\"id\":\"" + id + "\",\"name\":\"" + name
                + "\",\"character\":\"red\",\"direction\":\"down\",\"mapId\":\"0,0\",\"layer\":" + layer
                + ",\"x\":" + x + ",\"y\":" + y + ",\"moving\":true}";
    }
    static void send(Socket socket, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeInt(bytes.length); output.write(bytes); output.flush();
    }
    static JsonObject read(Socket socket) throws IOException {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        int length = input.readInt();
        check(length > 0 && length <= 524288, "bounded server frame");
        byte[] bytes = new byte[length]; input.readFully(bytes);
        return new JsonParser().parse(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }
    static void expectEof(Socket socket, String label) throws Exception {
        long deadline = System.nanoTime() + 5000000000L;
        try {
            while (true) {
                read(socket);
                check(System.nanoTime() < deadline, "offending peer not disconnected: " + label);
            }
        }
        catch (EOFException | SocketException expected) { }
    }
    static void await(Callable<Boolean> condition, String label) throws Exception {
        long until = System.nanoTime() + 8000000000L;
        while (!condition.call() && System.nanoTime() < until) Thread.sleep(10);
        check(condition.call(), "timed out: " + label);
    }
    static void check(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
