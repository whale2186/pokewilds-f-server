# LAN co-op integration contract

Goal: a playable host and joining players share a host-owned island, player presence/poses and world tile/Pokemon state. Separate local parties and battles remain player-owned; no PvP/trading or Internet matchmaking claim. Use a new explicit LAN protocol, not the broken legacy Kryo listeners or network-driven reflective Actions. Do not mutate source data/save files belonging to an existing playthrough during tests.

## Modules

- `com.pkmngen.game.multiplayer`: transport/state, no LibGDX dependencies; Gson available in runtime-deps.jar.
- `com.pkmngen.game.LanCoop`: bootstrap, renderer/game integration, identity, journals and private-effect rollback. Scripts and graphical E2E verify this separately from protocol fixtures.

## Transport API (public Java 8)

Class `com.pkmngen.game.multiplayer.LanSession implements AutoCloseable`:

- `static LanSession host(String bindAddress, int port, String name, String bootstrap, Map<String,String> initialResources, MutationPolicy policy) throws IOException`
- `static LanSession join(String hostAddress, int port, String name) throws IOException`
- `boolean isHost()`, `String id()`, `int port()`, `String bootstrap()`, `boolean isConnected()`, `String failure()` (null if no failure)
- `Map<String,Value> resources()` immutable defensive snapshot of authoritative resource map
- `Map<String,Pose> peers()` immutable defensive snapshot INCLUDING self and host
- `void pose(Pose pose)` nonblocking send; identity/name assigned by server, never trusted from packet
- `void propose(String key, long expectedRevision, String json)` nonblocking CAS request; null json = tombstone/delete. Host also uses this API. A rejected CAS retains current authoritative state; exposes rejection via `long rejectedMutations()`.
- `void close()` idempotent bounded shutdown of only owned listeners, sockets and workers; disconnected peers removed for remaining users.
- `interface MutationPolicy { boolean allow(String actorId, Pose actor, String key, String previousJson, String proposedJson); }`
- `Value` immutable public fields `long revision`, `String json`. Keep tombstone Values in resources, so deleted keys retain revisions.
- `Pose` immutable public fields `String id,name,character,direction,mapId; int layer; float x,y; boolean moving;` and public constructor `Pose(String id,String name,String character,String direction,String mapId,int layer,float x,float y,boolean moving)`. Coordinates finite and bounded, names max 24, character max32 identifier, directions enum up/down/left/right, map ID bounded numeric x,y, layers -1=overworld and 0..255=interior.

## State invariants

- Versioned fixed handshake. Readiness only after bootstrap, all resources and roster received; no independently generated guest world.
- Server assigns random session IDs; display-name collisions do not steal identities. All proposals/poses bound to socket identity; remote cannot claim another peer or change bootstrap.
- Revision 1 initial values. Absent key expectedRevision=0. Each successful write increments its key revision; stale CAS rejects and returns authoritative value. Mutations from unjoined clients rejected. Policy denial increments reject counter for proposer.
- Host serializes mutations atomically; broadcast accepted Values to all including initiator. Snapshot/join ordering must not lose concurrent mutations.
- Bound packet length, resource count, string sizes, peer count, queues and timeouts; chunk bootstrap resource inventory instead of sending one giant frame. Never ObjectInputStream or dynamic class construction. Strict packet types; malformed payload disconnects only offending client.
- Socket IO must not block render-thread caller. Dedicated owned writer queues; slow peers disconnect rather than unbounded queue growth. Initial host/join may block only during explicit setup, with finite timeouts.
- Roster/pose updates and disconnect converge to all peers. Host close makes guests disconnected with actionable failure, not continued false online status.
- No changes to the legacy Kryo registration order; regression harness depends on it.

## Atomic gameplay transactions

`LanSession` additionally provides:
- `public boolean transact(String transactionId, Map<String,Long> expected, Map<String,String> proposed)` enqueues one atomic all-or-nothing mutation group. Same nonempty key sets, max128 keys, transactionId safe identifier max64, aggregate encoded batch <=256KiB; invalid local inputs throw IllegalArgumentException. Return false only if disconnected or local queue admission fails. Socket/render caller must not block. No effects admitted after close.
- `public Map<String,TransactionResult> drainTransactions()` returns and drains caller-local immutable result map. `TransactionResult` immutable fields `boolean accepted; Map<String,Value> authoritative`. Every requested key is included, with `Value(0,null)` for absent rejected creates. Results are only delivered to requester; result IDs bound to socket and unique outstanding ID. Bound pending/results queues; reject before optimistic gameplay is stranded.
- Preserve existing `propose` and tests. Accepted transaction changes publish atomically to all peers; rejections leave every resource untouched, increment `rejectedMutations`, and return current values for every key. Policy evaluated on all keys before applying any; actor socket bound. Revision and aggregate inventory limits apply. No new Java object deserialization.
- Fix queue-overflow broadcast reentrancy: failed peer LEAVE must be enqueued after current broadcast to surviving peers, avoiding PEER resurrection.
- Reject live invalid LEAVE for self or host and fail guest connection cleanly; guard missing self in pose().
- Add real-socket TDD tests for multi-key single winner/conflicting pick-up, rejection of absent-key creates, all-or-nothing policy denial, transaction late join, actor binding, bounded inputs, close, queue-overflow ghost regression, malformed live LEAVE. Own changes only to `LanSession.java` and `tests/java/multiplayer/`.

## Required verification

Transport tests must use real loopback sockets: host+two peers, snapshot fidelity, distinct duplicate-name identities, both-direction pose replication, successful tile mutation and late join, stale concurrent CAS rejection, policy denial, malformed/oversized packet, client disconnect and host shutdown, no owned worker leaks. Write tests first and show initial expected failure. Tests may use a compact synthetic world for protocol checks but must not be called game E2E.

Parent must separately launch actual graphical host+guest JVMs, host-generated or loaded real map, assert matching world state and peer rendering/movement, real shared tile mutation, late join/disconnect, persistence and cleanup. Retain exact results and document remaining non-goals/limits honestly.
