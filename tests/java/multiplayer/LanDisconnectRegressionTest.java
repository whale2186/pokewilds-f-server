package multiplayer;

import com.pkmngen.game.multiplayer.LanSession;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static multiplayer.LanSecurityTest.*;

/** Real sockets; reflection only provides deterministic writer backpressure. */
public final class LanDisconnectRegressionTest {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "leave".equals(args[0])) {
            for (String target : new String[] {"self", "host", "unknown", "bad"}) invalidLeave(target);
            System.out.println("PASS malformedLiveLeaveFailsCleanly");
        }
        if (args.length == 0 || "overflow".equals(args[0])) {
            overflowingBroadcastCannotResurrectPeer();
            System.out.println("PASS overflowingBroadcastCannotResurrectPeer");
        }
    }

    private static void invalidLeave(String target) throws Exception {
        String self = UUID.randomUUID().toString(), host = UUID.randomUUID().toString();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch ready = new CountDownLatch(1), done = new CountDownLatch(1);
        try (ServerSocket server = new ServerSocket(0)) {
            Thread fixture = new Thread(() -> {
                try (Socket socket = server.accept()) {
                    read(socket);
                    send(socket, "{\"type\":\"WELCOME\",\"version\":1,\"id\":\"" + self + "\",\"hostId\":\"" + host
                        + "\",\"bootstrapLength\":0,\"resourceCount\":0,\"peerCount\":2}");
                    send(socket, posePacket(self, "guest", "0", "0", "-1"));
                    send(socket, posePacket(host, "host", "0", "0", "-1"));
                    send(socket, "{\"type\":\"READY\"}");
                    ready.await();
                    String victim = "self".equals(target) ? self : "host".equals(target) ? host
                        : "unknown".equals(target) ? UUID.randomUUID().toString() : "not-an-id";
                    send(socket, "{\"type\":\"LEAVE\",\"id\":\"" + victim + "\"}");
                    done.await();
                } catch (Throwable failure) { error.set(failure); }
            }, "invalid-leave-fixture");
            fixture.start();
            try (LanSession guest = LanSession.join("127.0.0.1", server.getLocalPort(), "guest")) {
                ready.countDown();
                await(() -> !guest.isConnected(), "invalid LEAVE " + target + " disconnects guest");
                check(guest.failure() != null, "invalid LEAVE has actionable failure");
                guest.pose(new LanSession.Pose(null, null, "red", "down", "0,0", -1, 1, 2, false));
            } finally { ready.countDown(); done.countDown(); fixture.join(5000); }
            check(!fixture.isAlive() && error.get() == null, "fake host completed: " + error.get());
        }
    }

    private static void overflowingBroadcastCannotResurrectPeer() throws Exception {
        try (LanSession host = host(); Socket slow = socket(host.port())) {
            String slowId = rawJoin(slow);
            try (LanSession survivor = LanSession.join("127.0.0.1", host.port(), "survivor")) {
                await(() -> survivor.peers().containsKey(slowId), "slow peer visible");
                Object lock = field(host, "lock");
                synchronized (lock) {
                    Object connection = ((Map<?, ?>) field(host, "clients")).get(slowId);
                    ArrayBlockingQueue<?> queue = (ArrayBlockingQueue<?>) field(connection, "queue");
                    // A writer already executing a frame is unable to shutdown under this lock.
                    // Stop its queue consumption with a blocking outbound task, then fill the queue.
                    Class<?> outbound = Class.forName("com.pkmngen.game.multiplayer.LanSession$Outbound");
                    CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
                    Object blocker = java.lang.reflect.Proxy.newProxyInstance(outbound.getClassLoader(), new Class<?>[] {outbound},
                        (proxy, method, arguments) -> {
                            entered.countDown();
                            try { release.await(); }
                            catch (InterruptedException interrupted) { throw new java.io.IOException(interrupted); }
                            return null;
                        });
                    @SuppressWarnings("unchecked") ArrayBlockingQueue<Object> writable = (ArrayBlockingQueue<Object>) queue;
                    writable.put(blocker);
                    check(entered.await(5, java.util.concurrent.TimeUnit.SECONDS), "writer blocked");
                    for (int i = 0; i < 256; i++) check(writable.offer(blocker), "filled writer queue");
                    try {
                        send(slow, posePacket(slowId, "raw", "123", "0", "-1"));
                    } finally {
                        // Reader applies the PEER only after we release the session lock.
                        Thread releaser = new Thread(() -> {
                            try { Thread.sleep(1000); } catch (InterruptedException ignored) { }
                            release.countDown();
                        }, "writer-release-fixture");
                        releaser.start();
                    }
                }
                await(() -> !host.peers().containsKey(slowId), "overflow evicts slow peer");
                Thread.sleep(200);
                check(!survivor.peers().containsKey(slowId), "LEAVE must follow PEER; no ghost resurrection");
                check(survivor.isConnected(), "survivor remains online");
            }
        }
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
}
