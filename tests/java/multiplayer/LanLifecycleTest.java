package multiplayer;

import com.pkmngen.game.multiplayer.LanSession;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import static multiplayer.LanSecurityTest.*;

public final class LanLifecycleTest {
    public static void main(String[] args) throws Exception {
        disconnectConvergesAndHostCloseReleasesPort();
        System.out.println("PASS disconnectConvergesAndHostCloseReleasesPort");
        slowSnapshotPeerIsEvicted();
        System.out.println("PASS slowSnapshotPeerIsEvicted");
        peerAdmissionIsBounded();
        System.out.println("PASS peerAdmissionIsBounded");
        idleHeartbeatKeepsHealthyPeersConnected();
        System.out.println("PASS idleHeartbeatKeepsHealthyPeersConnected");
        await(() -> ownedWorkers() == 0, "no owned worker leaks");
        System.out.println("PASS noOwnedWorkerLeaks");
    }

    private static void disconnectConvergesAndHostCloseReleasesPort() throws Exception {
        LanSession host = host();
        int port = host.port();
        try (LanSession a = LanSession.join("127.0.0.1", port, "a");
             LanSession b = LanSession.join("127.0.0.1", port, "b")) {
            a.close(); a.close();
            await(() -> host.peers().size() == 2 && b.peers().size() == 2, "client disconnect roster convergence");
            long begin = System.nanoTime(); host.close(); host.close();
            check(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin) < 2500, "bounded host close");
            await(() -> !b.isConnected() && b.failure() != null, "host close invalidates guest online state");
            check(b.failure().contains("LAN"), "actionable disconnected failure");
            try (ServerSocket released = new ServerSocket()) {
                released.bind(new InetSocketAddress("127.0.0.1", port));
            }
        } finally { host.close(); }
        try { LanSession.join("127.0.0.1", port, "no-host").close(); throw new AssertionError("joined closed host"); }
        catch (IOException expected) { }
    }

    private static void slowSnapshotPeerIsEvicted() throws Exception {
        StringBuilder bootstrap = new StringBuilder();
        for (int i = 0; i < 8 * 1024 * 1024; i++) bootstrap.append('x');
        try (LanSession host = LanSession.host("127.0.0.1", 0, "host", bootstrap.toString(), Collections.emptyMap(), null);
             Socket slow = new Socket()) {
            slow.setReceiveBufferSize(1024);
            slow.connect(new InetSocketAddress("127.0.0.1", host.port()));
            slow.setSoTimeout(5000);
            send(slow, "{\"type\":\"HELLO\",\"version\":1,\"name\":\"slow\"}");
            read(slow); // Only consume WELCOME; deliberately stop reading the large snapshot.
            await(() -> host.peers().size() == 2, "slow peer admitted");
            long begin = System.nanoTime();
            host.pose(new LanSession.Pose(null, null, "red", "up", "0,0", -1, 1, 2, true));
            host.propose("nonblocking", 0, "{}");
            check(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin) < 500, "render caller does not perform socket IO");
            await(() -> host.peers().size() == 1, "slow writer evicted within 8 seconds");
            check(host.isConnected(), "slow reader does not stop host");
        }
    }

    private static void peerAdmissionIsBounded() throws Exception {
        List<Socket> peers = new ArrayList<>();
        try (LanSession host = host()) {
            try {
                for (int i = 0; i < 15; i++) {
                    Socket peer = socket(host.port()); peers.add(peer); rawJoin(peer);
                }
                check(host.peers().size() == 16, "host plus fifteen peer bound");
                try { LanSession.join("127.0.0.1", host.port(), "overflow").close(); throw new AssertionError("peer overflow admitted"); }
                catch (IOException expected) { }
                peers.remove(0).close();
                await(() -> host.peers().size() == 15, "capacity released on disconnect");
                try (LanSession replacement = LanSession.join("127.0.0.1", host.port(), "replacement")) {
                    check(replacement.isConnected() && replacement.peers().size() == 16, "replacement admitted");
                }
            } finally { for (Socket peer : peers) peer.close(); }
        }
    }

    private static void idleHeartbeatKeepsHealthyPeersConnected() throws Exception {
        try (LanSession host = host(); LanSession guest = LanSession.join("127.0.0.1", host.port(), "guest")) {
            Thread.sleep(16000);
            check(host.isConnected() && guest.isConnected() && guest.failure() == null, "heartbeats outlive read timeout");
        }
    }

    private static int ownedWorkers() {
        int count = 0;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.isAlive() && thread.getName().startsWith("LanSession-")) count++;
        }
        return count;
    }
}
