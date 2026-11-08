#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
RUNTIME="${PW_RUNTIME:-$ROOT/runtime}"
mkdir -p "$RUNTIME/tmp"
if [[ ! -f "$RUNTIME/settings.txt" ]]; then cp "$ROOT/config/settings.txt" "$RUNTIME/settings.txt"; fi
RUNTIME="$(cd "$RUNTIME" && pwd)"
cd "$RUNTIME"
if [[ "$(uname -s)" == Darwin ]]; then
  exec "$JAVA" -XstartOnFirstThread -Djava.io.tmpdir="$RUNTIME/tmp" -cp "$ROOT/lib/pokewilds-engine.jar:$ROOT/lib/runtime-deps.jar" com.pkmngen.game.desktop.DesktopLauncher "$@"
fi
exec "$JAVA" -Djava.io.tmpdir="$RUNTIME/tmp" -cp "$ROOT/lib/pokewilds-engine.jar:$ROOT/lib/runtime-deps.jar" com.pkmngen.game.desktop.DesktopLauncher "$@"
