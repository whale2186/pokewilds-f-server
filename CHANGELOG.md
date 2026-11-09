# Changelog

## v0.1.3 — Item packs and menus

Remember player appearance between sessions. Fix washed-out night lighting, headless trade saves and ownership of traded Pokémon. Player battles use the normal battle window and move menu.

Drop items from the bag as packs that players can claim. Packs stay in the saved world until picked up.

Fix Pokémon disappearing from view when positions overlap or after leaving and returning. Keep creatures on the correct floor and preserve them through save/reload.

Spectating uses the normal battle layout. Player interactions have clearer menus and mouse support. Exit saves and returns to the main menu. Hide Save in multiplayer.

## v0.1.2 — Battle status and spectating

Poké Ball icons beside battling players' names. Interact and choose Spectate, or press F5 nearby. Live combatant sprites, HP/status, turns and narration; Esc/X leaves. Watching stops on battle end, departure or disconnect. Spectators cannot issue battle commands or alter their party/items through the viewer. Original attack effects are not replayed.

## v0.1.1 — Windows/Linux platform packages

Separate Windows x64 ZIP and Linux x64 tar.gz packages for client and server, each with a checksum-pinned Temurin Java 11 runtime and its original notices. Windows adds native .exe launchers; Linux preserves executable launcher permissions. No game rules, protocol or save-format change from v0.1.0.

## v0.1.0 — Experimental first release

Based on PokeWilds v0.8.11 by SheerSt and contributors. Adds LAN co-op, standalone headless server hosting, stable player UUIDs, remote character colors/followers/field-action visuals, movement smoothing, atomic resource edits, and dedicated world persistence/simulation.

Includes responsive window-size-aware HUD/player lists, T-to-chat, one-second chat popups with fade, F1-to-show retained chat, hidden HUD by default, false-conflict fixes, inventory/party rollback on rejected edits, and deferred host-save fixes.

Validation during development includes 19 headless regression tests, real loopback socket tests, graphical host/guest/late-join, contested pickup/harvest rollback, and orderly save/restart. The latest responsive HUD also passed its targeted layout/chat and graphical multiplayer checks. This is experimental; physical multi-PC LAN, Windows/macOS, full progression and Android are not verified.
