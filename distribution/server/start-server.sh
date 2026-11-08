#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
ENGINE="$ROOT/lib/pokewilds-engine.jar"
DEPS="$ROOT/lib/runtime-deps.jar"
if [[ ! -f "$ENGINE" ]]; then ENGINE="$ROOT/build/pokewilds-engine.jar"; fi
if [[ ! -f "$DEPS" ]]; then DEPS="$ROOT/vendor/runtime-deps.jar"; fi
RUNTIME="${PW_RUNTIME:-$ROOT/runtime}"
mkdir -p "$RUNTIME"
RUNTIME="$(cd "$RUNTIME" && pwd)"
cd "$RUNTIME"
exec "$JAVA" -Xmx2g -Djava.awt.headless=true -cp "$ENGINE:$DEPS" com.pkmngen.game.DedicatedServer "${1:-shared}" "${2:-54555}"
