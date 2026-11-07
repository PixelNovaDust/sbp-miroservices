#!/usr/bin/env bash
# Seed reference, studio, and person dev data (run after migrate dev when services are up).
set -euo pipefail

SEED_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="$(cd "$SEED_DIR/.." && pwd)"
REPO_ROOT="$(cd "$SCRIPTS_DIR/.." && pwd)"
cd "$REPO_ROOT"
# shellcheck source=../lib/service-runtime.sh
source "$SCRIPTS_DIR/lib/service-runtime.sh"
SCRIPT_DIR="$SEED_DIR"

GATEWAY_BASE="http://localhost:8080"
REFERENCE_SERVICE_URL=""
RESET=""
RESET_DB=""
EXTRA_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --reset) RESET=1 ;;
    --reset-db) RESET_DB=1 ;;
    --GatewayBase) GATEWAY_BASE="$2"; shift ;;
    --ReferenceBase) REFERENCE_SERVICE_URL="$2"; shift ;;
    --force) EXTRA_ARGS+=(--force) ;;
    --) shift; EXTRA_ARGS+=("$@"); break ;;
    *) echo "Unknown argument: $1" >&2; exit 1 ;;
  esac
  shift
done

ENV_DEV="$REPO_ROOT/env/.env.dev"
sbp_import_dev_env "$ENV_DEV"
GATEWAY_BASE="$(sbp_resolve_gateway_base_url "$GATEWAY_BASE")"
REFERENCE_SERVICE_URL="${REFERENCE_SERVICE_URL:-http://localhost:${REFERENCE_PORT:-8087}}"

export REPO_ROOT ENV_DEV GATEWAY_BASE REFERENCE_SERVICE_URL SEED_ACTOR=dev-seed

if [[ -n "$RESET_DB" ]]; then
  EXTRA_ARGS=(--reset-db "${EXTRA_ARGS[@]}")
elif [[ -n "$RESET" ]]; then
  EXTRA_ARGS=(--reset "${EXTRA_ARGS[@]}")
fi

echo "Gateway: $GATEWAY_BASE"
echo "Reference (internal entity-taxonomies): $REFERENCE_SERVICE_URL"
echo "Ensure discovery, gateway, reference, studio, person, and content services are running."

python3 "$SCRIPT_DIR/seed-dev-data.py" "${EXTRA_ARGS[@]}"
