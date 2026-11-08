# PokeWilds-F Server

A headless dedicated server for [PokeWilds-F](https://github.com/whale2186/pokewilds-f), based on PokeWilds v0.8.11.

**Original game by [SheerSt and the PokeWilds contributors](https://github.com/SheerSt/pokewilds).** This unofficial server modification is maintained by [whale2186](https://github.com/whale2186). It is not an official upstream release. See [full credits](CREDITS.md).

[Download server releases](https://github.com/whale2186/pokewilds-f-server/releases) · [Client releases](https://github.com/whale2186/pokewilds-f/releases) · [Multiplayer guide](docs/MULTIPLAYER.md)

## Run a server

Download and extract the **v0.1.2** package for your operating system. **Java 11 is bundled**, and the server needs no display, sound device, or local game client.

| Platform | Download | Launch |
|---|---|---|
| Windows x64 | [Windows ZIP](https://github.com/whale2186/pokewilds-f-server/releases/download/v0.1.2/pokewilds-f-server-v0.1.2-windows-x64.zip) | `pokewilds-f-server.exe shared 54555` |
| Linux x64 (glibc) | [Linux tar.gz](https://github.com/whale2186/pokewilds-f-server/releases/download/v0.1.2/pokewilds-f-server-v0.1.2-linux-x64.tar.gz) | `./start-server.sh shared 54555` |

Keep `jre/`, `lib/`, and the launcher together. Windows launchers are unsigned. Allow at least 2 GiB heap capacity plus room for the OS; first world generation can be expensive.

Linux:

```bash
bash start-server.sh shared 54555
```

Windows:

```bat
pokewilds-f-server.exe shared 54555
```

The launchers default to world `shared` and TCP port `54555`, and allow a Java heap up to 2 GiB. The server binds to all local interfaces. Players use the matching PokeWilds-F release, choose JOIN → Servers (A), and enter this machine's LAN IP and port. The original unmodified PokeWilds release cannot join this protocol.

Worlds live in `runtime/WORLD.sav/`. New worlds are generated automatically; existing dedicated snapshots resume. Set `PW_RUNTIME` to an absolute directory to choose another save location. Keep the entire world directory, especially `dedicated-state.json.zip` and its identity, together when backing up or upgrading. Do not run two servers against the same world directory.

## Console commands

| Command | Purpose |
|---|---|
| `help` | Show available commands |
| `list` / `players` | Show connected player names and IDs |
| `kick <name\|id>` | Disconnect a player; use the ID for duplicate names |
| `time day` / `time night` | Change world time and lighting |
| `save` | Save shared world state |
| `stop` | Save and shut down |

Ctrl+C also attempts an orderly save. Closing standard input leaves the server running. Prefer `stop` and retain backups.

## What it supports

Shared terrain/world Pokémon, atomic client edits, battle reservations, original island generation through a headless adapter, creature roaming/habitat timers, world clock, persistence and player pose restoration. Player bags and parties remain private client checkpoints. Server chat routing uses the same authenticated socket identities as the client protocol.

This is an experimental trusted-LAN server. It does not authenticate human accounts or validate every game rule. Do not expose it as a hardened public Internet service. One island per session, incomplete progression/field-move coverage, and no crash-proof distributed transaction guarantee. Dedicated edits are resumed from the dedicated snapshot; they are not automatically converted back into solo saves. See [the guide](docs/MULTIPLAYER.md) for detailed limitations.

Linux headless startup, socket tests, simulation and save/restart have been tested. Windows smoke checks under Wine do not establish native-Windows compatibility. No Android or macOS package is supplied in v0.1.2.

## Build from source

This repository retains the shared engine sources because original world generation references game classes even in headless mode. The server runs the dedicated entry point, not the desktop launcher.

Install JDK 11 and Python 3. Obtain `lib/runtime-deps.jar` from this release and copy it into `vendor/runtime-deps.jar` in the checkout. Then:

```bash
bash scripts/dev.sh build
bash scripts/server.sh shared 54555
python3 -m unittest discover -s tests -v
python3 scripts/fetch-java-runtimes.py
python3 scripts/package-release.py --version v0.1.2 --kind server --platform all
```

The dependency/asset JAR is kept out of Git history. Release packages include compiled engine/runtime JARs and launchers; platform packages also bundle Eclipse Temurin Java 11 with its notices. Local worlds, profiles, logs and credentials are excluded.

## Credits and provenance

PokeWilds was created by **SheerSt and the upstream contributors**. This engine was reconstructed from the public v0.8.11 release and modified independently. Upstream assets, Pokémon content and third-party software retain their original rights; no blanket license is asserted. Read [CREDITS.md](CREDITS.md) and [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
