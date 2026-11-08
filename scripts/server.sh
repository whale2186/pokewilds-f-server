#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT/scripts/dev.sh" build
RUNTIME="${PW_RUNTIME:-$ROOT/runtime}"
mkdir -p "$RUNTIME"
cd -- "$RUNTIME"
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -Djava.awt.headless=true -cp "$ROOT/build/pokewilds-engine.jar:$ROOT/vendor/runtime-deps.jar" com.pkmngen.game.DedicatedServer "$@"
