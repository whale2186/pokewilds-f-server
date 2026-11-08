#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
if (( $# < 3 )) || [[ "$1" != host && "$1" != join ]]; then
  printf 'Usage: %s {host|join} ADDRESS:PORT NAME [--coop-new=WORLD|--coop-world=WORLD] [--coop-size=30000]\n' "$0" >&2
  exit 2
fi
mode="$1"; endpoint="$2"; name="$3"; shift 3
if [[ ! "$name" =~ ^[A-Za-z0-9_-]{1,24}$ ]]; then
  printf 'Invalid player name (1-24 letters/digits/_/-).\n' >&2
  exit 2
fi
if [[ -z "${PW_RUNTIME:-}" ]]; then
  PW_RUNTIME="$ROOT/runtime/coop-$mode"
  if [[ ! -d "$PW_RUNTIME" ]]; then
    shopt -s nullglob
    legacy=("$ROOT/runtime/coop-$mode-"*)
    if [[ -d "$ROOT/runtime/coop-$mode-$name" ]]; then
      PW_RUNTIME="$ROOT/runtime/coop-$mode-$name"
    elif (( ${#legacy[@]} == 1 )); then
      PW_RUNTIME="${legacy[0]}"
    elif (( ${#legacy[@]} > 1 )); then
      printf 'Multiple legacy player folders. Set PW_RUNTIME to your original player folder.\n' >&2
      exit 2
    fi
  fi
fi
export PW_RUNTIME
exec "$ROOT/scripts/dev.sh" run "--coop-$mode=$endpoint" "--coop-name=$name" "$@"
