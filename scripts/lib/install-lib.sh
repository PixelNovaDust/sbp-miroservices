#!/usr/bin/env bash
# Installs sbp-lib-common into ~/.m2 (no global Maven required).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

if [[ -f "$ROOT/mvnw" ]]; then
  # shellcheck source=service-runtime.sh
  source "$ROOT/scripts/lib/service-runtime.sh"
  sbp_run_mvnw "$ROOT" -f sbp-lib-common/pom.xml clean install -DskipTests
  exit $?
fi

echo "mvnw not found; using Docker Maven..."
docker run --rm \
  -v "${ROOT}:/ws" \
  -v "${HOME}/.m2:/root/.m2" \
  -w /ws maven:3.9-eclipse-temurin-21 \
  mvn -f sbp-lib-common/pom.xml clean install -DskipTests
