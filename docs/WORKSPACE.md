> Historical local-workspace notes retained for recovery provenance. The current repository README and release layout supersede these development-machine paths.

# PokeWilds local development workspace

This is a **local reconstruction of PokeWilds v0.8.11**, not the author's private source repository. The public SheerSt/pokewilds repository contains assets and releases. Recovered code and assets retain their original rights; neither REA's MIT license nor this workspace grants permission to redistribute the game or its reconstructed engine. Nothing has been pushed or published.

## Build, run, debug

```bash
/mnt/ssd1/pokewiilds/scripts/dev.sh build
/mnt/ssd1/pokewiilds/scripts/dev.sh run dev
/mnt/ssd1/pokewiilds/scripts/dev.sh test
/mnt/ssd1/pokewiilds/scripts/dev.sh debug dev
```

`run` without `dev` uses normal game mode. `debug` suspends before main and listens **only on 127.0.0.1:5005**. Open this workspace in VS Code with the Java Extension Pack, start the debug command in a terminal, then select the included attach configuration. Debug information is emitted by the build. Build/test tasks are included. VS Code integration files are supplied; the editor extension itself is not installed or tested here.

The script prefers the existing JDK 11 at `/usr/lib/jvm/java-11-openjdk`; set `JAVA_HOME` to select another installed JDK. It compiles to Java 8 bytecode using `--release 8`. No Gradle/Maven download is needed. Compilation retains upstream unchecked/deprecated-API notes in `/mnt/ssd1/pokewiilds/logs/build.log`.

Game settings, saves, XDG directories and Java native-extraction temporary files are directed to `/mnt/ssd1/pokewiilds/runtime/`. X11 is selected, with Wayland unset. This is **not a sandbox**: the game still runs with your user permissions. Normal saves are relative to the runtime working directory. Keep test saves separate from any real playthrough.

## Dedicated server

Launch or create a world headlessly with `./scripts/server.sh WORLD [PORT]` (default
54555). Set `PW_RUNTIME` to its save directory if needed. In the game, choose
**JOIN** in setup, highlight **Servers (A)** and press A/Enter. Add a server
name, IP/hostname, and port, save, and select the entry to join. Bookmarks are
stored in `servers.json`. New dedicated worlds use the original island generator.
Console commands: `help`, `list`/`players`, `kick <name|id>`, `time day|night`,
`save`, and `stop`. See the
[LAN guide](docs/MULTIPLAYER.md#dedicated-server-and-server-list) for persistence
and dedicated simulation limits.

## Experimental LAN co-op

Multiplayer is implemented locally and tested with real graphical host/guest processes. See [the LAN guide](docs/MULTIPLAYER.md) for commands, saves, evidence and limits. It supports one shared island, remote avatars/movement, host-authoritative world edits and conflict rollback with private parties/bags. Shared storage, shared overworld battles and cross-island travel are disabled. Testing used loopback, not two physical PCs; this is not Internet-ready or crash-proof distributed persistence.

```bash
/mnt/ssd1/pokewiilds/scripts/coop.sh host 127.0.0.1:54555 Alice --coop-new=shared
/mnt/ssd1/pokewiilds/scripts/coop.sh join 127.0.0.1:54555 Bob
```

Use separate terminals. On a later host launch, replace `--coop-new=shared` with `--coop-world=shared`. For different PCs, replace loopback with the host's LAN IP on both commands and use matching builds. Keep existing playthroughs backed up.

## Layout

| Path beneath `/mnt/ssd1/pokewiilds/` | Purpose |
|---|---|
| `engine/src/main/java/` | Editable reconstructed Java engine, desktop launcher and updater |
| `build/pokewilds-engine.jar` | Fully recompiled Java engine plus LAN implementation (430 class files) |
| `vendor/runtime-deps.jar` | Release libraries/assets plus original Kotlin leak-diagnostics helpers; all 410 original Java engine class files are removed |
| `vendor/pokewilds-v0.8.11-otherplatforms/` | Unmodified extracted release |
| `reference/vineflower-raw/` | Raw re-decompilation with unsafe NotNull resugaring disabled |
| `reference/cfr/` | Alternate CFR output for comparison; not compiled |
| `reference/kotlin-decompiled/` | Diagnostic-helper Java views; not compiled |
| `pokewilds/` | Clean, independent upstream assets checkout |
| `rea/` | Clean, independent REA source/documentation checkout |
| `tools/rea-cli/` | Workspace-local pinned REA npm installation |
| `evidence/` | REA artifact inventory and runtime-capture Evidence/scenarios |
| `tests/` | Rebuild/differential/debug checks, real-socket LAN regressions and graphical drivers |

Do not copy the upstream main-branch assets wholesale into the older v0.8.11 runtime; their versions can differ. The runtime uses assets from the matching release. For mods, use `/mnt/ssd1/pokewiilds/runtime/mods/` and consult `/mnt/ssd1/pokewiilds/vendor/pokewilds-v0.8.11-otherplatforms/mods/README.txt`.

## REA

REA **6.7.0** is installed locally and was used, not merely downloaded:

```bash
/mnt/ssd1/pokewiilds/scripts/dev.sh rea --version
/mnt/ssd1/pokewiilds/scripts/dev.sh rea inspect-artifact /mnt/ssd1/pokewiilds/vendor/pokewilds-v0.8.11-otherplatforms/pokewilds.jar --format json
/mnt/ssd1/pokewiilds/scripts/dev.sh rea capture-process /mnt/ssd1/pokewiilds/evidence/rebuilt-startup.scenario.json --format json
```

Redirect the last two commands to distinct Evidence files: complete inventory output is large. Existing records are `/mnt/ssd1/pokewiilds/evidence/rea-artifact.json`, `/mnt/ssd1/pokewiilds/evidence/rea-rebuilt-startup.json` (initial menu observation) and `/mnt/ssd1/pokewiilds/evidence/rea-rebuilt-final-startup.json` (final source build). Read their limitations and cleanup fields before drawing conclusions. The startup capture is deliberately bounded and ends by timeout/SIGTERM; it is not a full gameplay or network test. Native audio priority/SLF4J warnings were present but startup reached the game menu. REA reported an unrelated process it could not inspect and left untouched, plus bounded filesystem-hash omissions.

No global npm installation or Hermes MCP/config change was made. Use the CLI immediately; no chat restart is needed. REA's documented managed-code parser is for .NET, and its JADX workflow is APK-oriented, not a desktop JAR source-recovery workflow. Here REA supplies package/runtime evidence while Vineflower supplies Java decompilation.

## Recovery provenance

- Release: `https://github.com/SheerSt/pokewilds/releases/tag/v0.8.11`, asset `pokewilds-otherplatforms.zip`.
- Release ZIP SHA-256: `5c0aca7f447ee6b4ed587f3ab2cefaf445219059d790862a7121c56a72fb22ba`.
- Original game JAR SHA-256: `0a72e17bf5cc3bd14ac97a6d001fe16c32cf8bd2d6caf1271bb2d3f234a6f248`.
- Vineflower **1.12.0** from Maven Central; SHA-256 `1dfcfe974395734fa467ce620661c7623d05ba83670de0529b1fbd63ff548b9d`.
- CFR **0.152** from benf.org; SHA-256 `f686e8f3ded377d7bc87d216a90e9e9512df4156e75b06c655a16648ae8765b2`. It generated unstructured Java and is retained only as a reference.
- Upstream assets revision: `83133ed40be8b7c7dff5f9058eef16dd8916be6a`.
- REA reference revision: `aacf740a047078e79c71d528e8e188afe2ba1aff`.

The original JAR contains 418 `com/pkmngen` class files. Only these were fed to the decompiler via `/mnt/ssd1/pokewiilds/vendor/engine-bytecode.jar`. Eight Kotlin leak-helper class files stay binary dependencies; the other 410 class files are rebuilt from 193 Java source files. Third-party dependencies were not recompiled.

The final reference decompilation was generated with:

```bash
java -Xmx3g -jar /mnt/ssd1/pokewiilds/tools/vineflower-1.12.0.jar \
  --folder --thread-count=4 --log-level=WARN --kt-enable=false \
  --resugar-idea-notnull=false \
  --add-external=/mnt/ssd1/pokewiilds/vendor/pokewilds-v0.8.11-otherplatforms/pokewilds.jar \
  /mnt/ssd1/pokewiilds/vendor/engine-bytecode.jar \
  /mnt/ssd1/pokewiilds/reference/vineflower-raw
```

Do not regenerate over edited engine sources. Compared with that raw output, three Java type-recovery corrections are applied: `Battle.var36` is an int rather than a byte; `PlayerStanding` uses a separate integer evolution-level local instead of a reused boolean; an unused `DynamicTextures` integer initializes with `0`, not `false`. Disabling IdeaNotNull resugaring preserves `Save.readData`'s real missing-file exception, which a differential regression test verifies.

## Verification scope and next entry points

`test` now runs **12 unittest cases**, including source recompilation with **no original Java engine classes** on its dependency classpath, a real loopback JDWP handshake, **56 headless assertions per implementation** with matching stdout against the original release, adapter helpers and seven real-socket LAN fixtures. Differential checks exercise coordinate serialization across signed 16-bit boundaries, JSON/ZIP save round-trip, missing-save failure, and four Kryo registration IDs. This is not proof of semantic equivalence for the entire engine.

LAN gameplay uses the new `LanCoop` adapter and `multiplayer/LanSession`, not the legacy reflective Kryo action listeners. Separate graphical suites verify movement/avatar rendering, bidirectional edits, late join, simultaneous pickup/harvest with exactly one winner, rejection refunds, failed-save handling, and orderly host/guest restart. Their reports and remaining untested behavior are recorded in [docs/MULTIPLAYER.md](docs/MULTIPLAYER.md). No multiplayer listener is left running, and nothing has been committed, pushed or published.
