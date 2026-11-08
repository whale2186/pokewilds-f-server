#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -n "${JAVA_HOME:-}" ]]; then
  JDK="$JAVA_HOME"
elif [[ -x /usr/lib/jvm/java-11-openjdk/bin/javac ]]; then
  JDK=/usr/lib/jvm/java-11-openjdk
else
  JDK="$(dirname -- "$(dirname -- "$(readlink -f -- "$(command -v javac)")")")"
fi
DEPS="$ROOT/vendor/runtime-deps.jar"
if [[ ! -f "$DEPS" && -f "$ROOT/lib/runtime-deps.jar" ]]; then DEPS="$ROOT/lib/runtime-deps.jar"; fi
MAIN=com.pkmngen.game.desktop.DesktopLauncher
build() {
  mkdir -p "$ROOT/build" "$ROOT/logs"
  local classes
  classes="$(mktemp -d "$ROOT/build/classes.XXXXXX")"
  trap 'rm -rf -- "$classes"' RETURN
  mapfile -d '' sources < <(find "$ROOT/engine/src/main/java" -name '*.java' -print0 | sort -z)
  if (( ${#sources[@]} == 0 )); then
    printf 'No engine Java sources found.\n' >&2
    return 1
  fi
  "$JDK/bin/javac" --release 8 -g -encoding UTF-8 -cp "$DEPS" -d "$classes" "${sources[@]}" > "$ROOT/logs/build.log" 2>&1 || {
    cat "$ROOT/logs/build.log" >&2
    return 1
  }
  local artifact
  artifact="$(mktemp "$ROOT/build/pokewilds-engine.XXXXXX.jar")"
  "$JDK/bin/jar" --create --file "$artifact" -C "$classes" .
  mv -f -- "$artifact" "$ROOT/build/pokewilds-engine.jar"
  printf 'Built %s Java source files: %s/build/pokewilds-engine.jar\n' "${#sources[@]}" "$ROOT"
}
case "${1:-help}" in
  build) build ;;
  run|debug)
    mode="$1"; shift
    build
    RUNTIME="${PW_RUNTIME:-$ROOT/runtime}"
    mkdir -p "$RUNTIME/cache" "$RUNTIME/config" "$RUNTIME/data" "$RUNTIME/tmp"
    RUNTIME="$(cd -- "$RUNTIME" && pwd)"
    if [[ ! -f "$RUNTIME/settings.txt" ]]; then
      cp -- "$ROOT/vendor/pokewilds-v0.8.11-otherplatforms/settings.txt" "$RUNTIME/settings.txt"
    fi
    cd -- "$RUNTIME"
    debug=()
    if [[ "$mode" == debug ]]; then
      debug=(-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:5005)
    fi
    exec env -u WAYLAND_DISPLAY DISPLAY="${DISPLAY:-:0}" \
      XDG_CACHE_HOME="$RUNTIME/cache" XDG_CONFIG_HOME="$RUNTIME/config" XDG_DATA_HOME="$RUNTIME/data" \
      "$JDK/bin/java" -Djava.io.tmpdir="$RUNTIME/tmp" "${debug[@]}" \
      -cp "$ROOT/build/pokewilds-engine.jar:$DEPS" "$MAIN" "$@"
    ;;
  test) exec python3 -m unittest discover -s "$ROOT/tests" -v ;;
  rea) shift; exec "$ROOT/tools/rea-cli/node_modules/.bin/rea" "$@" ;;
  *) printf 'Usage: %s {build|run [dev]|debug [dev]|test|rea ARGS...}\n' "$0" ;;
esac
