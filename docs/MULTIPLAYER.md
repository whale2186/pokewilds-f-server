# Experimental LAN co-op

The reconstructed v0.8.11 engine now has an opt-in LAN implementation. This is not the original release's legacy Kryo multiplayer, and an unmodified release cannot join it. Use matching builds on all machines. Changes are local and unpublished.

## Dedicated server and server list

Run `./scripts/dev.sh run`, select **JOIN** in setup, highlight **Servers (A)**,
and press A/Enter. Select **Add Server**, enter its name, IP/hostname, and port,
then select **Save**. Select a saved entry and press Enter to join. Up/Down
navigates and Escape goes back. Bookmarks live in `servers.json` in the client
runtime directory; existing `servers.properties` bookmarks are loaded as a fallback.
Empty player names in setup use `player`; the server browser validates names before joining.

Start a server without a display, audio device, or local player:

```bash
./scripts/server.sh shared 54555
# Or, using the normal launcher:
./scripts/dev.sh run --server shared 54555
```

The dedicated runner uses `runtime/` by default; `PW_RUNTIME` selects another
save directory. It imports an existing `shared.sav/game.json.zip` and its current
island map. If the world does not exist (or its directory is empty), it generates
and immediately saves an island using the original `GenIsland1` generation and
`PkmnMap` transition logic, including biome routes, structures, interiors, and
Pokemon initialization. CPU pixmaps provide the original tile metadata without
opening a window or requiring a display. Generation uses the normal 30000 size;
allow extra time and memory on the first launch. Existing dedicated snapshots
are resumed as saved; choose a new world name to generate a fresh original island.
Headless worlds persist directly in `shared.sav/dedicated-state.json.zip`; they do
not create solo `game.json.zip`/map files. An incomplete nonempty save directory
is rejected with an error rather than replaced. The default TCP port is 54555, bound to
all local interfaces. Clients on other PCs enter the server machine's LAN IP.
The server runs independently of connected players. Standard-input commands are
`help`, `list` / `players`, `kick <name|id>`, `time <day|night>`, `save`, and `stop`.
Use an id from `list` when names are duplicated. Both `stop` and Ctrl+C save before
shutdown. Closing standard input leaves the server running.

Dedicated clients do not receive local world-save prompts. Their private player
checkpoints remain separate from server world persistence. Remote followers,
interaction sprite frames, and emotes are carried in ephemeral player updates;
followers are never added to the shared wild Pokemon inventory.

The first launch imports terrain, existing interiors, creatures, routes, clock,
and world identity from the graphical save. Shared changes are written atomically
to `shared.sav/dedicated-state.json.zip` every second and on orderly shutdown;
later dedicated launches load that snapshot. Original solo world files remain
separate: dedicated edits are currently resumed by the dedicated runner, rather
than imported back into solo play. Keep the snapshot and world identity together.
A world lock prevents two dedicated runners from opening the same directory.

Dedicated mode advances the shared clock and accepts the same revisioned client
edits as graphical co-op. Autonomous creature movement, spawning, and tidal
simulation still depend on graphical host actions and do not run on the dedicated
server. Existing co-op limits below still apply. The dedicated authority occupies
one protocol slot but has no rendered avatar. Player bags and parties remain
private client saves.

`python3 -m unittest discover -s tests -p test_dedicated_server.py -v` checks
startup through `--server` with DISPLAY unset, a real client connection and terrain
transaction, clock advancement, fresh headless generation, persistence across server
restart, rejection of incomplete saves, and server bookmark reload. When the local
`runtime/asd.sav` exists, it also tests startup, sustained joins, and restart against
a temporary copy of that world, leaving the original untouched. These checks use
real sockets with the same endpoint parser and LanSession handshake as menu JOIN;
they do not exercise graphical rendering.

The legacy setup menu's **JOIN** also uses LanCoop/LanSession, just like the server
list and `--coop-join`. Its Server IP accepts IPv4 with an optional `:port` (type
semicolon for the colon); omitting the port uses 54555. Use the server list for
hostnames. `Game.initClient` remains the separate legacy Kryo API and is not used
by these JOIN menus.

## Start a session

From `/mnt/ssd1/pokewiilds`, on the same PC in separate terminals:

```bash
./scripts/coop.sh host 127.0.0.1:54555 Alice --coop-new=shared --coop-size=30000
./scripts/coop.sh join 127.0.0.1:54555 Bob
```

Creating an island can take time. Wait for the host world before joining. To reopen that host save, use `--coop-world=shared` instead of `--coop-new=shared`. Creation refuses to overwrite an existing directory. Names allow 1–24 letters/digits/underscores/hyphens; world names allow 1–32. Ordinary `scripts/dev.sh run` remains single-player.

For different PCs, replace **both** loopback addresses with the host's LAN IPv4 address. Bind only to the intended LAN interface; allow that TCP port through the host firewall only for the trusted LAN. No firewall changes, public listeners, router forwarding, authentication or Internet matchmaking were configured by this work. Do not expose this protocol to the Internet.

The wrapper builds before launching. `JAVA_HOME` can select a JDK; the tested JDK is OpenJDK 11, compiling Java 8 bytecode. Linux launches use X11. The two clients on one PC need separate names/runtime directories.

## What is shared

- One host-owned island, its terrain/interior-layer tiles, tile items and world Pokémon.
- Player presence, walking position, direction and visible remote avatars on the same layer.
- Host time-of-day state. World-creature AI runs on the host; guests initialize replica drawing/interaction state without running a second autonomous simulation.
- Revisioned resource edits. A batch succeeds entirely or fails entirely. Conflicting private party/item effects are rolled back; absent-key rejections remove optimistic creations and refund costs.
- Pokémon ownership identifiers persist in network/save data. Dropping waits for party removal before publishing; stale queued pickups cannot add an already-collected creature again.

These are **local private state**, not shared inventories: player bag, party, selection and ordinary encounter battles. Co-op is not shared combat, PvP or a trading protocol.

## Saves and identity

Default runtime directories are `runtime/coop-host/` and `runtime/coop-join/`, independent of display names. Launchers reuse an existing matching legacy folder or the sole legacy folder for that mode; if several exist, set `PW_RUNTIME` to the original player folder. `PW_RUNTIME` overrides the location. Keep each player's runtime private and stable:

- Host: `shared.sav/`, including a persistent `coop-world-id.txt` and saved transaction receipts.
- Local identity: `coop-profile.txt`, a persistent UUID independent of display name. Existing `coop-profile-NAME.txt` identities migrate without changing the UUID. Keep separate runtime directories for separate local players.
- Guest party/bag: `coop-player-WORLD_UUID-PROFILE_UUID.json.zip`.
- Interrupted shared edit: `coop-pending-WORLD_UUID-PROFILE_UUID.json.zip`.

Different same-named worlds receive different UUIDs. Preserve the profile file and guest save together when moving a local runtime. A corrupt identity is rejected, not silently replaced. On reconnect, an interrupted acknowledgement journal is resolved against the host's receipt set. Failed private writes stop gameplay and retain an outstanding journal rather than silently declaring it saved.

**Save the host before ending a session, and keep backups.** Orderly host/guest save and restart were tested. Host world files and guest party files are not one distributed durable database: abrupt host loss, power failure, disk corruption and rollback to an older host backup can still lose or disagree with acknowledged guest progress. Do not treat this as crash-proof or universally duplication-proof persistence.

## Deliberate limits

- One island per session. Cross-island generation/travel is blocked.
- Shared storage-box operations are blocked. Shared overworld battles use a revision-checked creature reservation; competing interactions are rejected until the battle ends. Grass/fishing encounters remain local by design; their complete combat flows were not part of the graphical acceptance suite.
- Existing interior layers have protocol representation and layer-aware avatars. Full building placement, new route/interior allocation, every field move, dungeon progression and all menu paths have **not** been exhaustively tested.
- No PvP, trading, shared battle turns, host migration or automatic reconnection UI.
- Trusted-LAN prototype, not an anti-cheat/security boundary. Socket identities and payload/queue limits do not authenticate human players or validate every game rule. Modified peers can cheat.
- Protocol limits: 16 peers, 524,288 resource keys including tombstones/receipts, 128 MiB aggregate UTF-8 resource data, 64 KiB per resource value; 128 keys and 256 KiB per atomic transaction. The adapter reserves one transaction key for a private-effect receipt. These are bounds, **not** tested 16-player performance claims. Very large worlds or long-running sessions can hit them.
- Terrain scans and host-authoritative updates are periodic; this is not latency-optimized networking. Higher-latency physical LANs, Windows/macOS and sustained multi-hour sessions remain untested.

## Reproduce verification

Fast headless/protocol suite:

```bash
./scripts/dev.sh test
```

This runs 12 unittest cases: rebuild isolation, original-release differential checks (56 assertions per implementation), real JDWP handshake, adapter/identity/checkpoint/cache checks, and seven real-socket Java protocol fixtures. The fixtures cover chunked snapshots, concurrent CAS, atomic transactions, malformed packets/ACKs, bounded queues/data/revisions, roster-overflow ghosts, invalid LEAVE, shutdown and owned-worker cleanup. Protocol fixtures are not graphical gameplay evidence.

Graphical suite (creates an isolated real island and three JVMs):

```bash
python scripts/test_multiplayer.py
```

To use a populated saved island, supply its directory; it is copied, never modified:

```bash
PW_E2E_WORLD=/absolute/path/to/world.sav python scripts/test_multiplayer.py
python scripts/test_gameplay_conflicts.py /absolute/path/to/world.sav
```

A graphical X11 session and several GiB of free RAM are required. Every run retains its `report.json`, game logs and isolated saves beneath `build/lan-e2e-*` or `build/lan-safety-*`. The first suite also captures actual framebuffers as `peers.png`.

The safety suite starts a real host and two guests, uses production DROP/removal and pickup actions plus a tile/bag mutation, and forces both guests to attempt the same resources before either publishes. It requires one Pokémon/item winner and exact loser rollback; it also exercises a stale queued pickup, a denied out-of-layer creation/refund and an intentionally blocked private-save path. It then shuts everything down and launches a new host plus both returning profiles to verify saved world, party, inventory and ownership. This directly drives game actions/state; it is not a complete keyboard-driven UI walkthrough of harvesting/building menus.

## Verified local evidence

Final protocol/headless run: **12 tests passed**. Current engine compiles from **199 Java files into 430 class files**; the dependency JAR still excludes the original 410 rebuilt engine classes.

- Presence/movement/edit/late-join run: `build/lan-e2e-w3e931cl/report.json` — success; host, guest and late join exited 0. Loaded a copy of a real saved island, not a synthetic protocol map.
- Contention/persistence run: `build/lan-safety-uceib5i0/report.json` — success; all six initial/restart JVMs exited 0. Guest `a` retained one party member and zero stones; guest `b` alone gained `LANRACE` and two stones. Both rejected creations were refunded, and both profiles restored correctly after host restart.
- Negative fixtures were observed failing before their fixes: stale pickup duplication, silent private-save failure, and one-sided world equality that ignored extra local resources. Failed runs remain in `build/` for diagnosis.

All network verification used loopback on this Linux machine. No second physical computer was used. See `MULTIPLAYER-CONTRACT.md` for transport boundaries; game state integration is in `LanCoop.java`.

## Dedicated generation and simulation

New dedicated worlds use `DedicatedWorld`, a graphics-free save-data generator. It creates a 129×129 overworld with forest, deep forest, savanna, desert, snow, volcano, graveyard, wooded lake, mountain and oasis terrain, trees, tall grass, flowers, rocks, wild Pokémon and a three-floor ruin. Existing worlds are loaded without regeneration. The generator is separate from `GenIsland1`: the original mansion, fossil laboratory, legendary puzzles, breeding and special scripted encounters are not reproduced.

`DedicatedSimulation` advances land and water roaming and habitat harvest timers using bundled species metadata. Habitat and harvest rules are shared with `Specie`, including the surrounding habitat search and shelter bonus. Moves and HP use the game data. Roaming respects tile occupancy and pauses within 80 pixels of players; habitat timers continue. Owned Pokémon stay where placed. Battle reservations stop simulation and competing edits, and dedicated servers clear reservations whose player has disconnected. Grass and fishing encounters continue through the ordinary client battle path. Battles are private to the player who reserves the creature.

Terrain edits, chopping, smashing, harvesting, placement and creature capture use the existing atomic resource transactions and dedicated snapshot persistence. Canonical terrain routes are included in bootstrap metadata so terraforming can change biome routes. Timer updates preserve client creature object identity for interaction actions. The headless tests cover the protocol state changes, simulation, reservation contention and save/restart; they do not establish complete graphical menu or dungeon progression coverage.

Optional graphical dedicated smoke (requires a working X display):

```bash
python3 scripts/test_dedicated_gameplay.py
```

This creates an isolated fresh world, loads its terrain and creature sprites in a real client, validates Pokémon HP/moves, and enters a reserved overworld battle. The test rejects internally logged game exceptions as well as process failures. It does not play an entire battle or exercise every field move through keyboard input.

## Multiplayer presentation fixes

Setup character, clothing and skin colors take precedence over restored player appearance; bags and parties still restore from the UUID-based checkpoint. CUT, BUILD and DIG transmit the active Pokémon atlas and frame when control swaps to a Pokémon. Wild creature movement updates retain replica objects and use a 250 ms visual glide; authoritative positions remain immediate for collisions, interactions and saves. Large teleports snap. Older queued resource deltas cannot replace a newer acknowledged revision. Genuine competing resource edits still reject and roll back rather than duplicate rewards.

## Chat, player list and HUD

The multiplayer HUD starts hidden. Press **F1** to show the online player count and day/night status. The dedicated authority is excluded from the player count and roster.

- **T** opens chat during overworld play. Enter sends; Escape closes and keeps the draft. Movement, confirm/cancel and debug shortcuts are suppressed while typing. Closing chat preserves the previously hidden HUD.
- **F3** toggles a paged online player list; Page Up/Down changes pages. Short session ID tags distinguish identical display names.
- **F4** toggles FPS, host/guest role, edit-sync status and tile coordinates.
- **F1** hides/shows the HUD. Panels recalculate their size, text scale, and positions from the current window dimensions. Chat wraps by measured text width, the input scrolls horizontally, and the roster adjusts its page size to available height. Wide windows use side-by-side panels; narrow windows stack them. Connection failures remain visible even when the HUD is hidden.
- Chat supports `/help`, `/players` and `/clear`. Page Up/Down scrolls chat history while the editor is open; Ctrl+V pastes text.

Chat is session-only, with 40 retained messages, 160 printable ASCII characters per message and a one-second send cooldown. New messages appear even while the HUD is hidden, hold for one second, then fade over half a second. F1 shows the HUD and retained messages; T opens scrollable history. Join/leave notices appear in the same feed. Chat uses the existing socket-bound message sender, not a name supplied inside the message. Dedicated servers route client chat without storing it in world resources or consuming an unused authority inbox. Messages sent before a player joins are not replayed to that player.

## Shared edit conflicts

Guest change detection compares the installed local replica with the current local state, then projects actual edits onto the server representation. Constructor defaults and normalized metadata do not become user edits. Background guest audits cannot submit changes to distant objects or another layer. Transiently overlapping Pokémon retain separate identities in the spatial index instead of silently deleting one.

Only stale revisions can trigger a retry. A competing identical pickup/harvest cannot be rebased into a second reward. Final rejected edits restore the private inventory/party checkpoint; provisional retries are not reported as completed edits. `COOP_EDIT_REJECTED` logs list the affected keys and whether the rejection used stale revisions. Genuine competing edits can still be rejected.

Deferred host saves write their prepared snapshot after the pending edit settles, avoiding an endless rescan of advancing creature timers. These changes do not establish crash-proof distributed persistence.

## Battle status and spectating

A Poké Ball beside a remote player's name means they are in a battle. It is visible even when the HUD is hidden. The player list also marks active battles.

Face a battling player and interact, then choose **Spectate**. **F5** watches the nearest active battle within the nearby interaction area, on the same map/layer. **Esc** or the configured cancel key (X by default) leaves. Chat and normal gameplay input are suppressed while watching.

The spectator display receives only the active combatants, HP/status, turn and battle text. It uses the normal battle frame, text and HP bars with independent display sprites; it does not enter the viewer's battle model, change their party/bag, or send battle actions. It supports wild, trainer-only encounters and player battle presentation. Watching ends when the battle ends, its identity changes, the player disconnects/leaves the area, or the viewer enters their own battle. Source clients must be updated to publish battle status. No world-save format change is required.

The display is a live battle summary with Pokémon sprites and narration; it does not replay every original attack effect/animation. Transported state is bounded by the existing 2048-character visual budget and is not persisted as a world resource.

Optional graphical check: `python3 scripts/test_spectator.py` uses an isolated copy of `runtime/asd.sav` (or `PW_E2E_WORLD`). It runs a headless authority and two graphical clients, checking status icons, the Spectate option, HP/turn updates, local input isolation, leaving/rejoining, battle completion, PvP presentation using interaction copies, and source disconnect.

## Item packs

Choose an item in the bag, select **DROP**, and choose an amount. Face clear ground to place a pack. Face the pack and interact, then select **Claim** to take its contents or **Exit** to leave it. Packs remain in the saved world until claimed. Simultaneous claims give the items to one player.

The start menu hides **Save** in multiplayer. **Exit** saves your progress and returns to the main menu. **Quit** on the main menu closes the game. Use updated clients for the pack sprite and menus.

Optional menu check: `python3 scripts/test_main_menu.py` verifies saving and returning to the main menu in single-player, graphical hosting and guest play, including reconnecting in the same window. It uses copied worlds.
