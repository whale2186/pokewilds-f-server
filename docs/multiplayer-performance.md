# Multiplayer performance

Code-path diagnosis (not a graphical frame-time benchmark):

- `LanCoop.afterFrame` already throttled poses to 50 ms and captures to 500 ms. However, each capture visited every terrain object, built a full serialized creature inventory, and sorted the combined resource keys on the render thread. A 17,500-tile island therefore produced periodic work spikes even when nothing changed.
- `LanSession` already used socket reader/writer workers and revision deltas. Its resource-change drain shared the world lock with admission, snapshot creation, policies, and packet handling. Guest pose admission also built the protocol frame on the caller thread.
- `DedicatedServer` copied the entire resource inventory to read one clock value. Its simulation already runs once per second outside rendering and batches creature edits.
- `DrawMap` already uses visible terrain lists, but remote avatars and labels had no camera culling.

The normal capture path now checks at most 256 audit terrain entries plus an 81-position local interaction zone every 500 ms. It retains unchanged terrain JSON and uses unsorted key comparison. Creature serialization prioritizes loading zones within 384 world units of players and audits up to 32 additional creatures per capture. Existing creatures still receive cheap membership checks to detect removals. Avatars outside the current map/layer or 512-unit loading zone skip animation updates; drawing uses padded camera bounds.

Pose submissions use a single latest-value mailbox, consumed by a session worker every 50 ms. Socket serialization and relaying occur outside rendering. Resource deltas use a separate short-held lock; transaction batches enter the delta stream atomically. A slow world policy no longer blocks render-side pose admission or delta polling. Dedicated clock reads use a single-key accessor.

Full capture remains deliberate for bootstrap, explicit save preparation, and diagnostic world comparisons. Terrain outside the interaction zone is eventually checked by the audit: a 17,500-tile baseline takes about 35 seconds at the nominal capture cadence (longer while actions or transactions pause capture). Full resource string-map copying/comparison, authoritative delta application, private inventory checkpoints, and initial world installation still run on the game thread. Reliable world edits still replicate globally; this change culls capture/visual processing rather than changing late-join or transaction visibility semantics.

Validation includes real loopback transport tests, transaction atomicity, late joins, server restart/save checks, and a stalled-policy regression proving pose admission and resource delta draining complete while the world lock is held. Interactive multiplayer frame-time and network-throughput measurements are still needed to quantify the improvement on a particular world and machine.

The periodic capture now returns sparse terrain updates rather than copying every baseline tile into a temporary map and rebuilding a full terrain-key union every 500 ms. Missing audited tiles carry explicit null tombstones; unaudited terrain is omitted from the comparison. Creature removals are still detected separately. Full captures remain unchanged for bootstrap, saves and diagnostics. This removes a recurring allocation source; the duration of any remaining frame stalls still requires measurement.
