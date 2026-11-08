package com.pkmngen.game.multiplayer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Explicit, length-prefixed UTF-8 JSON LAN protocol. No game or LibGDX dependencies. */
public final class LanSession implements AutoCloseable {
    public interface MutationPolicy {
        boolean allow(String actorId, Pose actor, String key, String previousJson, String proposedJson);
    }

    public interface CommitListener {
        void beforeCommit(Map<String, Value> updates) throws IOException;
    }
    private CommitListener commitListener;
    public void setCommitListener(CommitListener listener) { synchronized (lock) { commitListener = listener; } }

    public static final class Value {
        public final long revision;
        public final String json;
        public Value(long revision, String json) { this.revision = revision; this.json = json; }
    }

    public static final class TransactionResult {
        public final boolean accepted;
        public final Map<String, Value> authoritative;
        private TransactionResult(boolean accepted, Map<String, Value> authoritative) {
            this.accepted = accepted;
            this.authoritative = Collections.unmodifiableMap(new LinkedHashMap<String, Value>(authoritative));
        }
    }

    private static final class Transaction {
        final String id;
        final Map<String, Long> expected;
        final Map<String, String> proposed;
        Transaction(String id, Map<String, Long> expected, Map<String, String> proposed) {
            this.id = id; this.expected = expected; this.proposed = proposed;
        }
    }

    public static final class Pose {
        public final String id, name, character, direction, mapId;
        public final int layer;
        public final float x, y;
        public final boolean moving, running;
        public final int color, skinColor;
        public final String visual;
        public Pose(String id, String name, String character, String direction, String mapId,
                    int layer, float x, float y, boolean moving) {
            this(id, name, character, direction, mapId, layer, x, y, moving, false, 0xf83808ff, 0xffce48ff);
        }
        public Pose(String id, String name, String character, String direction, String mapId,
                    int layer, float x, float y, boolean moving, boolean running, int color, int skinColor) {
            this(id, name, character, direction, mapId, layer, x, y, moving, running, color, skinColor, "");
        }
        public Pose(String id, String name, String character, String direction, String mapId,
                    int layer, float x, float y, boolean moving, boolean running, int color, int skinColor, String visual) {
            this.visual = visual == null ? "" : visual;
            this.running = running; this.color = color; this.skinColor = skinColor;
            this.id = id; this.name = name; this.character = character; this.direction = direction;
            this.mapId = mapId; this.layer = layer; this.x = x; this.y = y; this.moving = moving;
        }
    }

    public static final class Message {
        public final String sender, payload;
        Message(String sender, String payload) { this.sender = sender; this.payload = payload; }
    }
    private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(128);
    public List<Message> drainMessages() {
        List<Message> result = new ArrayList<>(); messages.drainTo(result); return result;
    }
    public boolean message(String recipient, String payload) {
        if (!connected || payload == null || payload.length() > 32768 || !validId(recipient)) return false;
        JsonObject packet = frame("MESSAGE", "recipient", recipient, "payload", payload);
        if (host) return commands.offer(() -> {
            synchronized (lock) { routeMessage(id, recipient, payload); }
        });
        return upstream.offer(() -> upstream.write(packet));
    }
    private void routeMessage(String sender, String recipient, String payload) {
        if (recipient.equals(id)) {
            if (!messages.offer(new Message(sender, payload))) throw new IllegalStateException("interaction queue full");
        } else {
            Connection target = clients.get(recipient);
            if (target != null) target.send(frame("MESSAGE", "sender", sender, "payload", payload));
        }
    }

    private static final int VERSION = 1;
    private static final int MAX_FRAME = 524288;
    private static final int MAX_RESOURCES = 524288;
    private static final int MAX_PEERS = 16;
    private static final int MAX_BOOTSTRAP = 8 * 1024 * 1024;
    private static final int MAX_JSON = 65536;
    private static final long MAX_TOTAL_JSON = 128L * 1024 * 1024;
    private static final int CHUNK = 16384;
    private static final int SETUP_TIMEOUT = 60000;
    private static final int MAX_TRANSACTION_KEYS = 128, MAX_TRANSACTION_BYTES = 256 * 1024;
    private static final int MAX_PENDING_KEYS = 256, MAX_TRANSACTIONS = 256;
    private final boolean host;
    private final Object lock = new Object();
    private final Map<String, Value> changedValues = new LinkedHashMap<String, Value>();
    private final Object deltaLock = new Object();
    private final java.util.concurrent.atomic.AtomicReference<Pose> outgoingPose = new java.util.concurrent.atomic.AtomicReference<Pose>();
    private volatile Map<String, Pose> publishedPeers = Collections.emptyMap();
    private void publishPeers() { publishedPeers = Collections.unmodifiableMap(new LinkedHashMap<String, Pose>(roster)); }
    private final Map<String, Value> values = new LinkedHashMap<String, Value>();
    private final Map<String, Pose> savedPositions = new LinkedHashMap<String, Pose>();
    private final Map<String, Pose> roster = new LinkedHashMap<String, Pose>();
    private final Map<String, Connection> clients = new LinkedHashMap<String, Connection>();
    private final Set<Connection> connections = new HashSet<Connection>();
    private final Set<Thread> workers = Collections.newSetFromMap(new ConcurrentHashMap<Thread, Boolean>());
    private final String threadPrefix = "LanSession-" + UUID.randomUUID() + "-";
    private final MutationPolicy policy;
    private volatile String id, bootstrap, failure;
    private volatile String authorityId;
    private volatile boolean connected, closed;
    private volatile boolean returning;
    public boolean hasSavedPosition() { return returning; }
    private volatile int port;
    private ServerSocket listener;
    private Connection upstream;
    private long rejections;
    private long totalJson;
    private int broadcastDepth;
    private boolean flushingLeaves;
    private final java.util.ArrayDeque<String> deferredLeaves = new java.util.ArrayDeque<String>();
    private final ArrayBlockingQueue<Runnable> commands = new ArrayBlockingQueue<Runnable>(256);
    // Admission never waits for the world/policy lock or performs socket IO.
    private final Object transactionLock = new Object();
    private final Map<String, Transaction> pending = new LinkedHashMap<String, Transaction>();
    private final Map<String, TransactionResult> results = new LinkedHashMap<String, TransactionResult>();
    private int reservedKeys;
    private String assemblingId;
    private boolean assemblingAccepted;
    private final Map<String, Value> assemblingValues = new LinkedHashMap<String, Value>();

    private LanSession(boolean host, MutationPolicy policy) { this.host = host; this.policy = policy; }

    public static LanSession host(String bindAddress, int port, String name, String bootstrap,
                                  Map<String, String> initialResources, MutationPolicy policy) throws IOException {
        return host(bindAddress, port, name, bootstrap, initialResources, policy, null);
    }

    public static LanSession host(String bindAddress, int port, String name, String bootstrap,
                                  Map<String, String> initialResources, MutationPolicy policy, Map<String, Pose> positions) throws IOException {
        validName(name);
        if (bootstrap == null || bootstrap.length() > MAX_BOOTSTRAP) throw new IllegalArgumentException("bootstrap size");
        if (initialResources == null || initialResources.size() > MAX_RESOURCES) throw new IllegalArgumentException("resource count");
        LanSession session = new LanSession(true, policy);
        session.id = UUID.randomUUID().toString();
        session.authorityId = session.id;
        session.bootstrap = bootstrap;
        session.restorePositions(positions);
        for (Map.Entry<String, String> entry : initialResources.entrySet()) {
            validResource(entry.getKey(), 0, entry.getValue());
            session.totalJson += textSize(entry.getValue());
            if (session.totalJson > MAX_TOTAL_JSON) throw new IllegalArgumentException("resource inventory too large");
            session.values.put(entry.getKey(), new Value(1, entry.getValue()));
        }
        session.roster.put(session.id, defaultPose(session.id, name));
        session.publishPeers();
        try {
            session.listener = new ServerSocket();
            session.listener.bind(new InetSocketAddress(bindAddress, port), MAX_PEERS);
            session.port = session.listener.getLocalPort();
            session.connected = true;
            session.startWatchdog();
            session.startPoseTicker();
            session.startWorker("commands", () -> {
                try {
                    while (!session.closed) {
                        Runnable command = session.commands.poll(1000, TimeUnit.MILLISECONDS);
                        if (command != null) command.run();
                    }
                } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            });
            session.startWorker("accept", () -> session.acceptLoop());
            return session;
        } catch (IOException | RuntimeException error) { session.close(); throw error; }
    }

    public static LanSession join(String hostAddress, int port, String name) throws IOException {
        return join(hostAddress, port, name, null);
    }

    public static LanSession join(String hostAddress, int port, String name, String profile) throws IOException {
        validName(name);
        if (profile != null && !profile.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) throw new IllegalArgumentException("Invalid profile UUID");
        LanSession session = new LanSession(false, null);
        session.port = port;
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(hostAddress, port), 5000);
            Connection connection = session.new Connection(socket);
            session.upstream = connection;
            synchronized (session.lock) { session.connections.add(connection); }
            connection.startWriter();
            session.startWatchdog();
            session.startPoseTicker();
            connection.send(profile == null ? frame("HELLO", "version", VERSION, "name", name)
                    : frame("HELLO", "version", VERSION, "name", name, "profile", profile));
            session.startWorker("reader", () -> session.readGuest(connection));
            synchronized (session.lock) {
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(SETUP_TIMEOUT);
                while (!session.connected && session.failure == null) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) throw new IOException("LAN join timed out waiting for snapshot");
                    try { TimeUnit.NANOSECONDS.timedWait(session.lock, remaining); }
                    catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IOException("LAN join interrupted", error); }
                }
                if (!session.connected) throw new IOException(session.failure);
            }
            return session;
        } catch (IOException | RuntimeException error) {
            try { socket.close(); } catch (IOException ignored) { }
            session.close(); throw error;
        }
    }

    public boolean isHost() { return host; }
    public String id() { return id; }
    public String authorityId() { return authorityId; }
    public int port() { return port; }
    public String bootstrap() { return bootstrap; }
    public boolean isConnected() { return connected && !closed; }
    public String failure() { return failure; }
    public long rejectedMutations() { synchronized (lock) { return rejections; } }
    public boolean transact(String transactionId, Map<String, Long> expected, Map<String, String> proposed) {
        Transaction transaction = validTransaction(transactionId, expected, proposed);
        JsonObject packet = transactionFrame("TX", transaction);
        if (encodedSize(packet) > MAX_TRANSACTION_BYTES) throw new IllegalArgumentException("transaction batch too large");
        synchronized (transactionLock) {
            if (!isConnected()) return false;
            if (pending.containsKey(transactionId) || results.containsKey(transactionId))
                throw new IllegalArgumentException("transaction ID already outstanding");
            if (pending.size() + results.size() >= MAX_TRANSACTIONS
                    || reservedKeys + transaction.expected.size() > MAX_PENDING_KEYS) return false;
            pending.put(transactionId, transaction);
            reservedKeys += transaction.expected.size();
            boolean admitted;
            if (host) admitted = commands.offer(() -> {
                synchronized (lock) { if (!closed) mutateTransaction(id, transaction); }
            });
            else admitted = upstream.offer(() -> upstream.write(packet));
            if (!admitted) {
                pending.remove(transactionId);
                reservedKeys -= transaction.expected.size();
            }
            return admitted;
        }
    }

    public Map<String, TransactionResult> drainTransactions() {
        synchronized (transactionLock) {
            Map<String, TransactionResult> drained = Collections.unmodifiableMap(
                    new LinkedHashMap<String, TransactionResult>(results));
            for (TransactionResult result : results.values()) reservedKeys -= result.authoritative.size();
            results.clear();
            return drained;
        }
    }

    public TransactionResult takeTransactionResult(String transactionId) {
        synchronized (transactionLock) {
            TransactionResult result = results.remove(transactionId);
            if (result != null) reservedKeys -= result.authoritative.size();
            return result;
        }
    }

    private void completeTransaction(String transactionId, boolean accepted, Map<String, Value> authoritative) {
        synchronized (transactionLock) {
            if (closed || pending.remove(transactionId) == null) return;
            results.put(transactionId, new TransactionResult(accepted, authoritative));
        }
    }

    private void mutateTransaction(String actorId, Transaction transaction) {
        if (closed || !roster.containsKey(actorId)) return;
        boolean allowed = true;
        long nextTotal = totalJson;
        int nextCount = values.size();
        Map<String, Value> authoritative = new LinkedHashMap<String, Value>();
        for (String key : transaction.expected.keySet()) {
            Value previous = values.get(key);
            long revision = previous == null ? 0 : previous.revision;
            if (revision != transaction.expected.get(key) || revision == Long.MAX_VALUE) allowed = false;
            if (previous == null) nextCount++;
            nextTotal += textSize(transaction.proposed.get(key)) - textSize(previous == null ? null : previous.json);
            authoritative.put(key, previous == null ? new Value(0, null) : previous);
        }
        allowed &= nextCount <= MAX_RESOURCES && nextTotal <= MAX_TOTAL_JSON;
        if (policy != null) for (String key : transaction.expected.keySet()) {
            try {
                if (!policy.allow(actorId, roster.get(actorId), key, authoritative.get(key).json,
                        transaction.proposed.get(key))) allowed = false;
            } catch (RuntimeException denied) { allowed = false; }
        }
        if (allowed && commitListener != null) {
            Map<String,Value> updates = new LinkedHashMap<>();
            for (String key : transaction.expected.keySet())
                updates.put(key, new Value(authoritative.get(key).revision + 1, transaction.proposed.get(key)));
            try { commitListener.beforeCommit(Collections.unmodifiableMap(updates)); }
            catch (IOException | RuntimeException error) { allowed = false; }
        }
        if (allowed) {
            for (String key : transaction.expected.keySet()) {
                Value next = new Value(authoritative.get(key).revision + 1, transaction.proposed.get(key));
                values.put(key, next); authoritative.put(key, next);
            }
            synchronized (deltaLock) { changedValues.putAll(authoritative); }
            totalJson = nextTotal;
            broadcast(batchFrame("TX_VALUE", authoritative), null);
        }
        if (actorId.equals(id)) {
            if (!allowed) rejections++;
            completeTransaction(transaction.id, allowed, authoritative);
        } else {
            Connection requester = clients.get(actorId);
            if (requester != null) requester.sendTransactionResult(transaction.id, allowed, authoritative);
        }
    }
    public void propose(String key, long expectedRevision, String json) {
        validResource(key, expectedRevision, json);
        synchronized (lock) {
            if (!isConnected()) return;
            if (host) {
                if (!commands.offer(() -> {
                    synchronized (lock) { if (!closed) mutate(id, key, expectedRevision, json); }
                })) rejections++;
            } else upstream.send(frame("CAS", "key", key, "revision", expectedRevision, "json", json));
        }
    }

    private void mutate(String actorId, String key, long expectedRevision, String proposed) {
        if (!roster.containsKey(actorId)) return;
        Value previous = values.get(key);
        long revision = previous == null ? 0 : previous.revision;
        long nextTotal = totalJson - textSize(previous == null ? null : previous.json) + textSize(proposed);
        boolean allowed = revision == expectedRevision && revision < Long.MAX_VALUE
                && (previous != null || values.size() < MAX_RESOURCES) && nextTotal <= MAX_TOTAL_JSON;
        if (allowed && policy != null) {
            try { allowed = policy.allow(actorId, roster.get(actorId), key, previous == null ? null : previous.json, proposed); }
            catch (RuntimeException error) { allowed = false; }
        }
        if (!allowed) {
            if (actorId.equals(id)) rejections++;
            else {
                Connection proposer = clients.get(actorId);
                if (proposer != null) proposer.send(valueFrame("REJECT", key, previous == null ? new Value(0, null) : previous));
            }
            return;
        }
        Value next = new Value(revision + 1, proposed);
        totalJson = nextTotal;
        values.put(key, next); recordChange(key, next);
        broadcast(valueFrame("VALUE", key, next), null);
    }
    /** Coalesce render-thread submissions; serialization and sending run at 20 Hz. */
    public void pose(Pose pose) {
        validPose(pose);
        if (isConnected()) outgoingPose.set(pose);
    }

    private void startPoseTicker() {
        startWorker("poses", () -> {
            try {
                while (!closed) {
                    TimeUnit.MILLISECONDS.sleep(50);
                    Pose pose = outgoingPose.getAndSet(null);
                    if (pose == null || !isConnected()) continue;
                    if (host) {
                        synchronized (lock) { if (!closed) applyPose(id, pose); }
                    } else {
                        Pose self = publishedPeers.get(id);
                        if (self != null) upstream.send(poseFrame(new Pose(id, self.name, pose.character, pose.direction,
                                pose.mapId, pose.layer, pose.x, pose.y, pose.moving, pose.running,
                                pose.color, pose.skinColor, pose.visual)));
                    }
                }
            } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        });
    }

    private void applyPose(String actorId, Pose incoming) {
        Pose previous = roster.get(actorId);
        if (previous == null) return;
        Pose assigned = new Pose(actorId, previous.name, incoming.character, incoming.direction,
                incoming.mapId, incoming.layer, incoming.x, incoming.y, incoming.moving, incoming.running, incoming.color, incoming.skinColor, incoming.visual);
        roster.put(actorId, assigned); publishPeers();
        Connection owner = clients.get(actorId);
        if (owner != null && owner.profile != null) savedPositions.put(owner.profile, assigned);
        broadcast(poseFrame(assigned), null);
    }
    /** Single-consumer delta stream for the game adapter; avoids copying an island every frame. */
    public Map<String, Value> drainResourceChanges() {
        synchronized (deltaLock) {
            Map<String, Value> changes = new LinkedHashMap<String, Value>(changedValues);
            changedValues.clear();
            return changes;
        }
    }
    private void recordChange(String key, Value value) {
        synchronized (deltaLock) { changedValues.put(key, value); }
    }
    public Value resource(String key) {
        synchronized (lock) { return values.get(key); }
    }
    public Map<String, Value> resources() {
        synchronized (lock) { return Collections.unmodifiableMap(new LinkedHashMap<String, Value>(values)); }
    }
    public Map<String, Pose> savedPositions() {
        synchronized (lock) { return new LinkedHashMap<String, Pose>(savedPositions); }
    }
    public void restorePositions(Map<String, Pose> positions) {
        synchronized (lock) { if (positions != null) savedPositions.putAll(positions); }
    }
    public Map<String, Pose> peers() {
        return publishedPeers;
    }

    /** Disconnect a specific remote peer; names need not be unique. */
    public boolean kick(String peerId) {
        synchronized (lock) {
            if (!host) return false;
            Connection connection = clients.get(peerId);
            if (connection == null) return false;
            connection.shutdown();
            return true;
        }
    }

    private void acceptLoop() {
        while (!closed) {
            Socket socket;
            try {
                socket = listener.accept();
            } catch (IOException error) {
                if (!closed) { failure = "LAN listener failed: " + error.getMessage(); close(); }
                break;
            }
            try {
                synchronized (lock) {
                    if (closed || connections.size() >= MAX_PEERS - 1) { socket.close(); continue; }
                    Connection connection = new Connection(socket);
                    connections.add(connection);
                    connection.startWriter();
                    startWorker("reader", () -> readHost(connection));
                }
            } catch (IOException error) {
                // An accepted socket that resets during setup is not a listener failure.
                try { socket.close(); } catch (IOException ignored) { }
            }
        }
    }

    private void readHost(Connection connection) {
        try {
            JsonObject hello = connection.read();
            if (hello.has("profile")) {
                schema(hello, "HELLO", "version", "name", "profile");
                connection.profile = string(hello, "profile");
                if (!connection.profile.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) throw new IOException("Invalid profile UUID");
            } else schema(hello, "HELLO", "version", "name");
            if (integer(hello, "version") != VERSION) throw new IOException("incompatible protocol version");
            String name = string(hello, "name"); validName(name);
            synchronized (lock) {
                if (closed || connection.dead) return;
                connection.identity = UUID.randomUUID().toString();
                if (connection.profile != null) for (Connection peer : clients.values())
                    if (connection.profile.equals(peer.profile)) throw new IOException("Profile already connected");
                Pose saved = savedPositions.get(connection.profile);
                roster.put(connection.identity, saved == null ? defaultPose(connection.identity, name)
                        : new Pose(connection.identity, name, saved.character, saved.direction, saved.mapId,
                                saved.layer, saved.x, saved.y, false, false, saved.color, saved.skinColor));
                publishPeers();
                clients.put(connection.identity, connection);
                final Map<String, Value> snapshot = new LinkedHashMap<String, Value>(values);
                final List<Pose> peers = new ArrayList<Pose>(roster.values());
                connection.enqueue(() -> {
                    JsonObject manifest = frame("WELCOME", "version", VERSION, "id", connection.identity,
                            "hostId", id, "bootstrapLength", bootstrap.length(),
                            "resourceCount", snapshot.size(), "peerCount", peers.size());
                    if (connection.profile != null) manifest.addProperty("returning", saved != null);
                    connection.write(manifest);
                    for (int offset = 0; offset < bootstrap.length();) {
                        int end = Math.min(offset + CHUNK, bootstrap.length());
                        if (end < bootstrap.length() && Character.isHighSurrogate(bootstrap.charAt(end - 1))) end--;
                        connection.write(frame("BOOT", "data", bootstrap.substring(offset, end)));
                        offset = end;
                    }
                    for (Map.Entry<String, Value> entry : snapshot.entrySet()) {
                        connection.write(valueFrame("VALUE", entry.getKey(), entry.getValue()));
                    }
                    for (Pose peer : peers) connection.write(poseFrame(peer));
                    connection.write(frame("READY"));
                });
                if (!connection.dead) broadcast(poseFrame(roster.get(connection.identity)), connection);
            }
            while (!closed && !connection.dead) {
                JsonObject packet = connection.read();
                if ("PEER".equals(string(packet, "type"))) {
                    Pose pose = readPose(packet);
                    synchronized (lock) { applyPose(connection.identity, pose); }
                } else if ("MESSAGE".equals(string(packet, "type"))) {
                    schema(packet, "MESSAGE", "recipient", "payload");
                    String recipient = string(packet, "recipient"), payload = string(packet, "payload");
                    if (!validId(recipient) || payload.length() > 32768) throw new IOException("invalid interaction");
                    synchronized (lock) { routeMessage(connection.identity, recipient, payload); }
                } else if ("CAS".equals(string(packet, "type"))) {
                    schema(packet, "CAS", "key", "revision", "json");
                    String key = string(packet, "key");
                    long revision = integer(packet, "revision");
                    String proposed = nullableString(packet, "json");
                    validResource(key, revision, proposed);
                    synchronized (lock) { mutate(connection.identity, key, revision, proposed); }
                } else if ("TX".equals(string(packet, "type"))) {
                    Transaction transaction = readTransaction(packet);
                    synchronized (lock) {
                        if (connection.dead || closed) return;
                        if (connection.outstandingTransactions.containsKey(transaction.id)
                                || connection.outstandingKeys + transaction.expected.size() > MAX_PENDING_KEYS)
                            throw new IOException("duplicate or excessive outstanding transactions");
                        connection.outstandingTransactions.put(transaction.id, transaction.expected.size());
                        connection.outstandingKeys += transaction.expected.size();
                        mutateTransaction(connection.identity, transaction);
                    }
                } else schema(packet, "PING");
            }
        } catch (IOException | RuntimeException ignored) {
            // A peer's malformed input never tears down the listener or another peer.
        } finally { connection.shutdown(); }
    }

    private void readGuest(Connection connection) {
        StringBuilder boot = new StringBuilder();
        boolean welcome = false;
        String hostId = null;
        long bootstrapLength = -1, resourceCount = -1, peerCount = -1;
        try {
            while (!closed && !connection.dead) {
                JsonObject packet = connection.read();
                String type = string(packet, "type");
                synchronized (lock) {
                    if (assemblingId != null && !"TX_ACK".equals(type)) throw new IOException("interleaved transaction result");
                    if ("WELCOME".equals(type) && !welcome && !connected) {
                        if (packet.has("returning")) {
                            schema(packet, type, "version", "id", "hostId", "bootstrapLength", "resourceCount", "peerCount", "returning");
                            returning = bool(packet, "returning");
                        } else schema(packet, type, "version", "id", "hostId", "bootstrapLength", "resourceCount", "peerCount");
                        if (integer(packet, "version") != VERSION) throw new IOException("incompatible protocol version");
                        id = string(packet, "id"); hostId = string(packet, "hostId"); authorityId = hostId;
                        if (!validId(id) || !validId(hostId) || id.equals(hostId)) throw new IOException("invalid assigned identities");
                        bootstrapLength = integer(packet, "bootstrapLength");
                        resourceCount = integer(packet, "resourceCount"); peerCount = integer(packet, "peerCount");
                        if (bootstrapLength < 0 || bootstrapLength > MAX_BOOTSTRAP || resourceCount < 0
                                || resourceCount > MAX_RESOURCES || peerCount < 2 || peerCount > MAX_PEERS) throw new IOException("invalid snapshot manifest");
                        welcome = true;
                    } else if ("BOOT".equals(type) && welcome && !connected) {
                        schema(packet, type, "data");
                        String data = string(packet, "data");
                        if (data.length() == 0 || data.length() > CHUNK || boot.length() + data.length() > bootstrapLength) throw new IOException("bootstrap too large");
                        boot.append(data);
                    } else if (("VALUE".equals(type) || "REJECT".equals(type) && connected) && welcome) {
                        schema(packet, type, "key", "revision", "json");
                        String key = string(packet, "key");
                        if (!connected && values.containsKey(key)) throw new IOException("duplicate snapshot resource");
                        if (!values.containsKey(key) && values.size() >= MAX_RESOURCES) throw new IOException("too many resources");
                        long revision = integer(packet, "revision");
                        String json = nullableString(packet, "json");
                        validResource(key, revision, json);
                        if (revision == 0 && (!"REJECT".equals(type) || json != null)) throw new IOException("invalid revision zero");
                        Value previous = values.get(key);
                        totalJson = totalJson - textSize(previous == null ? null : previous.json) + textSize(json);
                        if (totalJson > MAX_TOTAL_JSON) throw new IOException("resource inventory too large");
                        if ("REJECT".equals(type)) rejections++;
                        if (revision == 0) values.remove(key);
                        else values.put(key, new Value(revision, json));
                        if (connected && (revision != 0 || previous != null)) recordChange(key, new Value(revision, json));
                    } else if ("MESSAGE".equals(type) && connected) {
                        schema(packet, type, "sender", "payload");
                        String sender = string(packet, "sender"), payload = string(packet, "payload");
                        if (!roster.containsKey(sender) || payload.length() > 32768
                                || !messages.offer(new Message(sender, payload))) throw new IOException("invalid interaction");
                    } else if ("TX_VALUE".equals(type) && connected) {
                        schema(packet, type, "batch");
                        Map<String, Value> updates = readBatch(string(packet, "batch"));
                        applyValues(updates, false);
                    } else if ("TX_ACK".equals(type) && connected) {
                        readTransactionResult(packet);
                    } else if ("PEER".equals(type) && welcome) {
                        Pose pose = readPose(packet);
                        if (!validId(pose.id)) throw new IOException("invalid assigned identity");
                        if (!connected && roster.containsKey(pose.id)) throw new IOException("duplicate snapshot peer");
                        if (!roster.containsKey(pose.id) && roster.size() >= MAX_PEERS) throw new IOException("too many peers");
                        roster.put(pose.id, pose); publishPeers();
                    } else if ("READY".equals(type) && welcome && !connected) {
                        schema(packet, type);
                        if (!roster.containsKey(id) || !roster.containsKey(hostId)
                                || boot.length() != bootstrapLength || values.size() != resourceCount
                                || roster.size() != peerCount) throw new IOException("incomplete snapshot inventory/roster");
                        bootstrap = boot.toString(); connected = true; lock.notifyAll();
                    } else if ("LEAVE".equals(type) && connected) {
                        schema(packet, type, "id");
                        String departed = string(packet, "id");
                        if (!validId(departed) || departed.equals(id) || departed.equals(hostId)
                                || !roster.containsKey(departed)) throw new IOException("invalid departed peer");
                        roster.remove(departed); publishPeers();
                    } else if ("PING".equals(type) && welcome) { schema(packet, type); }
                    else throw new IOException("unexpected packet " + type);
                }
            }
        } catch (IOException | RuntimeException error) {
            if (!closed) failure = "LAN host disconnected or sent invalid data: " + error.getMessage();
        } finally {
            connected = false;
            connection.shutdown();
            synchronized (lock) { lock.notifyAll(); }
        }
    }

    private void broadcast(JsonObject packet, Connection except) {
        broadcastDepth++;
        try {
            for (Connection connection : new ArrayList<Connection>(clients.values())) {
                if (connection != except) connection.send(packet);
            }
        } finally {
            broadcastDepth--;
            // Finish the current event before its resulting disconnects. Never recurse
            // through queue-overflow shutdowns and reorder LEAVE ahead of PEER.
            if (broadcastDepth == 0 && !flushingLeaves) {
                flushingLeaves = true;
                try {
                    while (!deferredLeaves.isEmpty()) broadcast(frame("LEAVE", "id", deferredLeaves.remove()), null);
                } finally { flushingLeaves = false; }
            }
        }
    }

    private interface Outbound { void write() throws IOException; }
    private void startWatchdog() {
        startWorker("watchdog", () -> {
            try {
                while (!closed) {
                    if (!host && upstream != null && upstream.dead) break;
                    Thread.sleep(250);
                    List<Connection> snapshot;
                    synchronized (lock) { snapshot = new ArrayList<Connection>(connections); }
                    long now = System.nanoTime();
                    for (Connection connection : snapshot) {
                        long writing = connection.writeStarted, reading = connection.readStarted;
                        if (writing != 0 && now - writing > TimeUnit.SECONDS.toNanos(5)
                                || reading != 0 && now - reading > TimeUnit.MILLISECONDS.toNanos(SETUP_TIMEOUT)) {
                            if (!host && !closed) failure = "LAN host timed out (stalled socket read/write)";
                            connection.shutdown();
                        }
                    }
                }
            } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        });
    }

    private final class Connection {
        final Socket socket;
        final DataInputStream input;
        final DataOutputStream output;
        final ArrayBlockingQueue<Outbound> queue = new ArrayBlockingQueue<Outbound>(256);
        volatile boolean dead;
        volatile long writeStarted, readStarted;
        boolean sentAny;
        String identity, profile;
        final Map<String, Integer> outstandingTransactions = new LinkedHashMap<String, Integer>();
        int outstandingKeys;
        Connection(Socket socket) throws IOException {
            this.socket = socket;
            socket.setTcpNoDelay(true); socket.setSoTimeout(SETUP_TIMEOUT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        }
        void startWriter() {
            startWorker("writer", () -> {
                try {
                    while (!dead && !closed) {
                        Outbound next = queue.poll(1000, TimeUnit.MILLISECONDS);
                        if (next != null) next.write();
                        else if (sentAny) write(frame("PING"));
                    }
                } catch (IOException | InterruptedException error) {
                    if (!closed && !host) failure = "LAN host connection lost: " + error.getMessage();
                } finally { shutdown(); }
            });
        }
        void send(JsonObject packet) { enqueue(() -> write(packet)); }
        boolean offer(Outbound outgoing) { return !dead && !closed && queue.offer(outgoing); }
        void enqueue(Outbound outgoing) { if (!offer(outgoing) && !dead) shutdown(); }
        void sendTransactionResult(String transactionId, boolean accepted, Map<String, Value> authoritative) {
            // Rejections may be much larger than requests. Publish bounded frames as
            // one queue item; guests stage them until the last requested key arrives.
            enqueue(() -> {
                try {
                    int remaining = authoritative.size();
                    for (Map.Entry<String, Value> entry : authoritative.entrySet()) {
                        Value value = entry.getValue();
                        write(frame("TX_ACK", "id", transactionId, "accepted", accepted, "key", entry.getKey(),
                                "revision", value.revision, "json", value.json, "last", --remaining == 0));
                    }
                } finally {
                    synchronized (lock) {
                        Integer count = outstandingTransactions.remove(transactionId);
                        if (count != null) outstandingKeys -= count;
                    }
                }
            });
        }
        void write(JsonObject packet) throws IOException {
            byte[] bytes = packet.toString().getBytes(StandardCharsets.UTF_8);
            if (bytes.length > MAX_FRAME) throw new IOException("packet too large");
            writeStarted = System.nanoTime();
            try { output.writeInt(bytes.length); output.write(bytes); output.flush(); sentAny = true; }
            finally { writeStarted = 0; }
        }
        JsonObject read() throws IOException {
            readStarted = System.nanoTime();
            try {
                int length = input.readInt();
                if (length < 2 || length > MAX_FRAME) throw new IOException("packet length out of bounds");
                byte[] bytes = new byte[length]; input.readFully(bytes);
                String text;
                try { text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString(); }
                catch (CharacterCodingException error) { throw new IOException("invalid UTF-8", error); }
                return parsePacket(text);
            } finally { readStarted = 0; }
        }
        void shutdown() {
            synchronized (lock) {
                if (dead) return;
                dead = true;
                try { socket.close(); } catch (IOException ignored) { }
                queue.clear(); connections.remove(this);
                outstandingTransactions.clear(); outstandingKeys = 0;
                if (host && identity != null) {
                    clients.remove(identity); roster.remove(identity); publishPeers();
                    if (!closed) {
                        deferredLeaves.add(identity);
                        if (broadcastDepth == 0 && !flushingLeaves) {
                            String departed = deferredLeaves.remove();
                            broadcast(frame("LEAVE", "id", departed), null);
                        }
                    }
                } else if (!host) {
                    connected = false;
                    if (!closed && failure == null) failure = "LAN host disconnected";
                    lock.notifyAll();
                }
            }
        }
    }

    private void startWorker(String role, Runnable work) {
        Thread worker = new Thread(() -> {
            try { work.run(); } finally { workers.remove(Thread.currentThread()); }
        }, threadPrefix + role);
        worker.setDaemon(true); workers.add(worker); worker.start();
    }

    @Override public void close() {
        synchronized (lock) {
            if (closed) return;
            closed = true; connected = false;
            synchronized (transactionLock) {
                commands.clear(); pending.clear(); results.clear(); reservedKeys = 0;
            }
            assemblingValues.clear(); assemblingId = null;
            if (listener != null) try { listener.close(); } catch (IOException ignored) { }
            for (Connection connection : new ArrayList<Connection>(connections)) connection.shutdown();
            lock.notifyAll();
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        for (Thread worker : new ArrayList<Thread>(workers)) {
            if (worker == Thread.currentThread()) continue;
            worker.interrupt();
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) break;
            try { TimeUnit.NANOSECONDS.timedJoin(worker, remaining); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); break; }
        }
    }

    private static Pose defaultPose(String id, String name) {
        return new Pose(id, name, "red", "down", "0,0", -1, 0, 0, false);
    }
    private static JsonObject poseFrame(Pose pose) {
        return frame("PEER", "id", pose.id, "name", pose.name, "character", pose.character,
                "direction", pose.direction, "mapId", pose.mapId, "layer", pose.layer,
                "x", pose.x, "y", pose.y, "moving", pose.moving, "running", pose.running, "color", pose.color, "skinColor", pose.skinColor, "visual", pose.visual);
    }
    private static int packedColor(JsonObject packet, String key) throws IOException {
        long value = integer(packet, key);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) throw new IOException("invalid packed color");
        return (int)value;
    }
    private static Pose readPose(JsonObject packet) throws IOException {
        String visual = packet.has("visual") ? string(packet, "visual") : "";
        if (visual.length() > 2048) throw new IOException("visual state too large");
        packet.remove("visual");
        boolean appearance = packet.has("skinColor");
        if (appearance) schema(packet, "PEER", "id", "name", "character", "direction", "mapId", "layer", "x", "y", "moving", "running", "color", "skinColor");
        else schema(packet, "PEER", "id", "name", "character", "direction", "mapId", "layer", "x", "y", "moving");
        long layer = integer(packet, "layer");
        if (layer < -1 || layer > 255) throw new IOException("invalid layer");
        Pose pose = new Pose(string(packet, "id"), string(packet, "name"), string(packet, "character"),
                string(packet, "direction"), string(packet, "mapId"), (int) layer,
                number(packet, "x"), number(packet, "y"), bool(packet, "moving"), appearance && bool(packet, "running"), appearance ? packedColor(packet, "color") : 0xf83808ff, appearance ? packedColor(packet, "skinColor") : 0xffce48ff, visual);
        validPose(pose); validName(pose.name);
        if (pose.id.length() > 64) throw new IOException("invalid peer identity");
        return pose;
    }
    private static JsonObject valueFrame(String type, String key, Value value) {
        return frame(type, "key", key, "revision", value.revision, "json", value.json);
    }
    private static JsonObject frame(String type, Object... entries) {
        JsonObject packet = new JsonObject(); packet.addProperty("type", type);
        for (int i = 0; i < entries.length; i += 2) {
            String key = (String) entries[i]; Object value = entries[i + 1];
            if (value == null) packet.add(key, com.google.gson.JsonNull.INSTANCE);
            else if (value instanceof Number) packet.addProperty(key, (Number) value);
            else if (value instanceof Boolean) packet.addProperty(key, (Boolean) value);
            else packet.addProperty(key, (String) value);
        }
        return packet;
    }
    private static JsonObject parsePacket(String text) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(text)); reader.setLenient(false);
        JsonObject packet = readFlatObject(reader);
        if (reader.peek() != JsonToken.END_DOCUMENT) throw new IOException("trailing input");
        return packet;
    }
    private static JsonObject readFlatObject(JsonReader reader) throws IOException {
        JsonObject packet = new JsonObject(); reader.beginObject(); int count = 0;
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (++count > 16 || key.length() > 32 || packet.has(key)) throw new IOException("invalid or duplicate field");
            JsonToken token = reader.peek();
            if (token == JsonToken.STRING) packet.addProperty(key, reader.nextString());
            else if (token == JsonToken.NUMBER) {
                String value = reader.nextString();
                if (value.length() > 32) throw new IOException("number too long");
                java.math.BigDecimal number = new java.math.BigDecimal(value);
                if (number.scale() < -64 || number.scale() > 64) throw new IOException("numeric exponent out of bounds");
                packet.add(key, new JsonPrimitive(number));
            } else if (token == JsonToken.BOOLEAN) packet.addProperty(key, reader.nextBoolean());
            else if (token == JsonToken.NULL) { reader.nextNull(); packet.add(key, com.google.gson.JsonNull.INSTANCE); }
            else throw new IOException("nested values forbidden in protocol");
        }
        reader.endObject();
        return packet;
    }

    private static Transaction validTransaction(String id, Map<String, Long> expected, Map<String, String> proposed) {
        if (id == null || !id.matches("[A-Za-z0-9_][A-Za-z0-9_-]{0,63}")) throw new IllegalArgumentException("invalid transaction ID");
        if (expected == null || proposed == null || expected.isEmpty() || expected.size() > MAX_TRANSACTION_KEYS
                || !expected.keySet().equals(proposed.keySet())) throw new IllegalArgumentException("invalid transaction key sets");
        Map<String, Long> revisions = new LinkedHashMap<String, Long>(expected);
        Map<String, String> changes = new LinkedHashMap<String, String>(proposed);
        for (String key : revisions.keySet()) {
            Long revision = revisions.get(key);
            if (revision == null) throw new IllegalArgumentException("missing expected revision");
            validResource(key, revision, changes.get(key));
        }
        return new Transaction(id, revisions, changes);
    }
    private static int encodedSize(JsonObject packet) { return packet.toString().getBytes(StandardCharsets.UTF_8).length; }
    private static JsonObject transactionFrame(String type, Transaction transaction) {
        Map<String, Value> entries = new LinkedHashMap<String, Value>();
        for (String key : transaction.expected.keySet()) entries.put(key, new Value(transaction.expected.get(key), transaction.proposed.get(key)));
        JsonObject packet = batchFrame(type, entries); packet.addProperty("id", transaction.id); return packet;
    }
    private static JsonObject batchFrame(String type, Map<String, Value> entries) {
        com.google.gson.JsonArray batch = new com.google.gson.JsonArray();
        for (Map.Entry<String, Value> entry : entries.entrySet()) batch.add(valueFrame("ENTRY", entry.getKey(), entry.getValue()));
        return frame(type, "batch", batch.toString());
    }
    private static Map<String, Value> readBatch(String text) throws IOException {
        if (text.length() > MAX_FRAME) throw new IOException("batch too large");
        JsonReader reader = new JsonReader(new StringReader(text)); reader.setLenient(false); reader.beginArray();
        Map<String, Value> entries = new LinkedHashMap<String, Value>();
        while (reader.hasNext()) {
            if (entries.size() >= MAX_TRANSACTION_KEYS) throw new IOException("too many transaction keys");
            JsonObject entry = readFlatObject(reader); schema(entry, "ENTRY", "key", "revision", "json");
            String key = string(entry, "key"), json = nullableString(entry, "json");
            long revision = integer(entry, "revision"); validResource(key, revision, json);
            if (entries.put(key, new Value(revision, json)) != null) throw new IOException("duplicate transaction key");
        }
        reader.endArray();
        if (entries.isEmpty() || reader.peek() != JsonToken.END_DOCUMENT) throw new IOException("invalid batch");
        return entries;
    }
    private static Transaction readTransaction(JsonObject packet) throws IOException {
        schema(packet, "TX", "id", "batch");
        if (encodedSize(packet) > MAX_TRANSACTION_BYTES) throw new IOException("transaction batch too large");
        Map<String, Value> entries = readBatch(string(packet, "batch"));
        Map<String, Long> expected = new LinkedHashMap<String, Long>();
        Map<String, String> proposed = new LinkedHashMap<String, String>();
        for (Map.Entry<String, Value> entry : entries.entrySet()) {
            expected.put(entry.getKey(), entry.getValue().revision); proposed.put(entry.getKey(), entry.getValue().json);
        }
        return validTransaction(string(packet, "id"), expected, proposed);
    }
    private void applyValues(Map<String, Value> updates, boolean allowAbsent) throws IOException {
        long nextTotal = totalJson;
        int nextCount = values.size();
        for (Map.Entry<String, Value> entry : updates.entrySet()) {
            Value next = entry.getValue(), previous = values.get(entry.getKey());
            if (next.revision == 0 && (!allowAbsent || next.json != null)) throw new IOException("invalid revision zero");
            nextTotal += textSize(next.json) - textSize(previous == null ? null : previous.json);
            if (previous == null && next.revision != 0) nextCount++;
            if (previous != null && next.revision == 0) nextCount--;
        }
        if (nextTotal > MAX_TOTAL_JSON || nextCount > MAX_RESOURCES) throw new IOException("resource inventory too large");
        totalJson = nextTotal;
        for (Map.Entry<String, Value> entry : updates.entrySet()) {
            if (entry.getValue().revision == 0) values.remove(entry.getKey());
            else values.put(entry.getKey(), entry.getValue());
        }
        synchronized (deltaLock) { changedValues.putAll(updates); }
    }
    private void readTransactionResult(JsonObject packet) throws IOException {
        schema(packet, "TX_ACK", "id", "accepted", "key", "revision", "json", "last");
        String transactionId = string(packet, "id"), key = string(packet, "key"), json = nullableString(packet, "json");
        boolean accepted = bool(packet, "accepted"), last = bool(packet, "last");
        long revision = integer(packet, "revision"); validResource(key, revision, json);
        synchronized (transactionLock) {
            Transaction transaction = pending.get(transactionId);
            if (transaction == null || !transaction.expected.containsKey(key)) throw new IOException("unsolicited transaction result/key");
            if (assemblingId == null) { assemblingId = transactionId; assemblingAccepted = accepted; }
            if (!assemblingId.equals(transactionId) || assemblingAccepted != accepted || assemblingValues.containsKey(key))
                throw new IOException("mismatched transaction result");
            if (revision == 0 && (accepted || json != null)) throw new IOException("invalid absent transaction value");
            if (accepted && (transaction.expected.get(key) == Long.MAX_VALUE || revision != transaction.expected.get(key) + 1
                    || !java.util.Objects.equals(json, transaction.proposed.get(key)))) throw new IOException("invalid accepted transaction value");
            assemblingValues.put(key, new Value(revision, json));
            if (last != (assemblingValues.size() == transaction.expected.size())) throw new IOException("incomplete transaction result");
            if (last) {
                applyValues(assemblingValues, !accepted);
                if (!accepted) rejections++;
                completeTransaction(transactionId, accepted, assemblingValues);
                assemblingValues.clear(); assemblingId = null;
            }
        }
    }
    private static void schema(JsonObject packet, String type, String... fields) throws IOException {
        if (!type.equals(string(packet, "type")) || packet.entrySet().size() != fields.length + 1) throw new IOException("invalid packet schema");
        for (String field : fields) if (!packet.has(field)) throw new IOException("missing field " + field);
    }
    private static String string(JsonObject packet, String key) throws IOException {
        JsonElement value = packet.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new IOException("expected string " + key);
        return value.getAsString();
    }
    private static String nullableString(JsonObject packet, String key) throws IOException {
        return packet.has(key) && packet.get(key).isJsonNull() ? null : string(packet, key);
    }
    private static long integer(JsonObject packet, String key) throws IOException {
        JsonElement value = packet.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IOException("expected integer " + key);
        try { return value.getAsBigDecimal().longValueExact(); }
        catch (ArithmeticException error) { throw new IOException("integer out of range", error); }
    }
    private static float number(JsonObject packet, String key) throws IOException {
        JsonElement value = packet.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IOException("expected number " + key);
        return value.getAsFloat();
    }
    private static boolean bool(JsonObject packet, String key) throws IOException {
        JsonElement value = packet.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) throw new IOException("expected boolean " + key);
        return value.getAsBoolean();
    }
    private static void validName(String name) {
        if (name == null || name.length() == 0 || name.length() > 24 || hasControls(name)) throw new IllegalArgumentException("invalid player name (1..24 characters)");
    }
    private static boolean validId(String value) {
        try { return UUID.fromString(value).toString().equals(value); }
        catch (IllegalArgumentException error) { return false; }
    }
    private static long textSize(String value) { return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length; }
    private static boolean hasControls(String value) {
        for (int i = 0; i < value.length(); i++) if (Character.isISOControl(value.charAt(i))) return true;
        return false;
    }
    private static void validResource(String key, long revision, String json) {
        if (key == null || key.length() == 0 || key.length() > 256 || hasControls(key)) throw new IllegalArgumentException("invalid resource key (1..256 characters)");
        if (revision < 0 || json != null && json.length() > MAX_JSON) throw new IllegalArgumentException("invalid revision or resource size (max 65536 characters)");
    }
    private static void validPose(Pose pose) {
        if (pose == null || !Float.isFinite(pose.x) || !Float.isFinite(pose.y)
                || Math.abs(pose.x) > 1000000 || Math.abs(pose.y) > 1000000
                || pose.layer < -1 || pose.layer > 255) throw new IllegalArgumentException("invalid pose coordinates/layer");
        if (pose.visual.length() > 2048) throw new IllegalArgumentException("visual state too large");
        if (pose.character == null || !pose.character.matches("[A-Za-z0-9_][A-Za-z0-9_-]{0,31}")) throw new IllegalArgumentException("invalid character identifier");
        if (!("up".equals(pose.direction) || "down".equals(pose.direction) || "left".equals(pose.direction) || "right".equals(pose.direction))) throw new IllegalArgumentException("invalid direction");
        if (pose.mapId == null || !pose.mapId.matches("-?[0-9]{1,7},-?[0-9]{1,7}")) throw new IllegalArgumentException("invalid map identifier");
        String[] coordinates = pose.mapId.split(",");
        if (Math.abs(Integer.parseInt(coordinates[0])) > 1000000 || Math.abs(Integer.parseInt(coordinates[1])) > 1000000) throw new IllegalArgumentException("map identifier out of bounds");
    }
}
