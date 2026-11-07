#!/usr/bin/env bash
# SBP stack commands for macOS / Linux (use when make is unavailable).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"
# shellcheck source=lib/service-runtime.sh
source "$SCRIPT_DIR/lib/service-runtime.sh"

ENV_DEV="$REPO_ROOT/env/.env.dev"
ENV_PROD="$REPO_ROOT/env/.env.prod"
INFRA_DEV="auth-db user-db person-db studio-db content-db reference-db file-storage-db prometheus grafana"
INFRA_PROD="$INFRA_DEV"
DBS_DEV="auth-db user-db person-db studio-db content-db reference-db file-storage-db"

compose_prod() {
  docker compose -p sbp-prod --env-file "$ENV_PROD" -f "$REPO_ROOT/docker/docker-compose.prod.yaml" "$@"
}

cmd_migrate() {
  local env="${1:-dev}"
  local repair=""
  shift || true
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --repair) repair=1 ;;
      *) echo "Unknown migrate flag: $1" >&2; return 1 ;;
    esac
    shift
  done
  if [[ "$env" == dev ]]; then
    if [[ -n "$repair" ]]; then
      SBP_CLEAR_LIQUIBASE_CHECKSUMS=true sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" run --rm --build sbp-migration
    else
      sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" run --rm --build sbp-migration
    fi
  elif [[ "$env" == prod ]]; then
    if [[ -n "$repair" ]]; then
      echo "migrate prod --repair is not supported; fix checksums manually." >&2
      return 1
    fi
    compose_prod run --rm --build sbp-migration
  else
    echo "Usage: migrate dev|prod [--repair]" >&2
    return 1
  fi
}

cmd_up() {
  local env="${1:-dev}"
  shift || true
  local mode=full
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --infra) mode=infra ;;
      *) echo "Unknown up flag: $1 (use --infra for DBs + monitoring only)" >&2; return 1 ;;
    esac
    shift
  done
  if [[ "$env" == dev ]]; then
    if [[ "$mode" == infra ]]; then
      sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" up -d $INFRA_DEV
      cmd_migrate dev
    else
      sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" up -d $DBS_DEV
      cmd_migrate dev
      sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" up -d --build
    fi
  elif [[ "$env" == prod ]]; then
    if [[ "$mode" == infra ]]; then
      compose_prod up -d $INFRA_PROD
      cmd_migrate prod
    else
      compose_prod up -d $DBS_DEV
      cmd_migrate prod
      compose_prod up -d --build
    fi
  else
    echo "Usage: up dev|prod [--infra]" >&2
    return 1
  fi
}

cmd_down() {
  local env="${1:-dev}"
  shift || true
  local volumes=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --volumes) volumes=1 ;;
      *) echo "Unknown down flag: $1" >&2; return 1 ;;
    esac
    shift
  done
  if [[ "$env" == dev ]]; then
    if [[ -n "$volumes" ]]; then
      sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" down -v
    else
      sbp_compose_dev "$REPO_ROOT" "$ENV_DEV" down
    fi
  elif [[ "$env" == prod ]]; then
    compose_prod down
  else
    echo "Usage: down dev|prod [--volumes]" >&2
    return 1
  fi
}

cmd_reset() {
  local env="${1:-dev}"
  if [[ "$env" != dev ]]; then
    echo "reset is only supported for dev (down --volumes + up --infra)." >&2
    return 1
  fi
  cmd_down dev --volumes
  cmd_up dev --infra
}

cmd_seed() {
  local which="${1:-}"
  shift || true
  if [[ "$which" != dev ]]; then
    echo "Usage: seed dev [--migrate] [--reset] [--reset-db] [--force] [-- …]" >&2
    return 1
  fi
  local migrate="" reset="" reset_db="" force="" seed_extra=()
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --migrate) migrate=1 ;;
      --reset) reset=1 ;;
      --reset-db) reset_db=1 ;;
      --force) force=1 ;;
      --) shift; seed_extra=("$@"); break ;;
      *) seed_extra+=("$1") ;;
    esac
    shift
  done
  if [[ -n "$migrate" ]]; then
    cmd_migrate dev
  fi
  local args=()
  [[ -n "$reset_db" ]] && args+=(--reset-db)
  [[ -n "$reset" && -z "$reset_db" ]] && args+=(--reset)
  [[ -n "$force" ]] && args+=(--force)
  if [[ ${#seed_extra[@]} -gt 0 ]]; then
    args+=("${seed_extra[@]}")
  fi
  if [[ ${#args[@]} -eq 0 ]]; then
    "$SCRIPT_DIR/seed/seed-dev.sh"
  else
    "$SCRIPT_DIR/seed/seed-dev.sh" "${args[@]}"
  fi
}

cmd_svc() {
  local action="${1:-}"
  local alias="${2:-}"
  local docker_flag="${3:-}"
  case "$action" in
    list)
      sbp_show_service_aliases
      ;;
    status|info)
      sbp_show_service_info "$REPO_ROOT" "$ENV_DEV"
      ;;
    start|stop|restart|logs)
      if [[ -z "${alias// /}" ]]; then
        sbp_show_service_aliases
        return 1
      fi
      sbp_resolve_service "$alias"
      case "$action" in
        start)
          if [[ "$docker_flag" == docker || "$docker_flag" == --docker ]]; then
            sbp_start_service_docker "$REPO_ROOT" "$ENV_DEV" || return 1
          else
            sbp_start_service_local "$REPO_ROOT" "$ENV_DEV" 0 || return 1
          fi
          ;;
        stop)
          sbp_stop_service_local "$REPO_ROOT" "$ENV_DEV"
          sbp_stop_service_docker "$REPO_ROOT" "$ENV_DEV"
          ;;
        restart)
          if [[ "$docker_flag" == docker || "$docker_flag" == --docker ]]; then
            sbp_restart_service "$REPO_ROOT" "$ENV_DEV" 1
          else
            sbp_restart_service "$REPO_ROOT" "$ENV_DEV" 0
          fi
          ;;
        logs)
          sbp_tail_service_logs "$REPO_ROOT" "$ENV_DEV"
          ;;
      esac
      ;;
    *)
      echo "Usage: svc list|status|start|stop|restart|logs <alias> [--docker]" >&2
      return 1
      ;;
  esac
}

show_help() {
  local c_cmd c_desc c_note rst
  if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
    c_cmd=$'\033[1;36m'
    c_desc=$'\033[0;33m'
    c_note=$'\033[2m'
    rst=$'\033[0m'
  else
    c_cmd="" c_desc="" c_note="" rst=""
  fi
  help_row() {
    # shellcheck disable=SC2059
    printf "  ${c_cmd}%-36s${rst} ${c_desc}%s${rst}\n" "$1" "$2"
  }
  help_row "help" "Print this command list"
  help_row "lib" "Install the shared library into ~/.m2 for Maven and the IDE"
  help_row "dbeaver" "Update DBeaver connections from env/.env.dev (after env changes)"
  echo ""
  help_row "up dev" "Start dev databases, migrate, then build and run all services"
  help_row "up dev --infra" "Databases and monitoring only — handy when apps run in the IDE"
  help_row "up prod" "Start prod databases, migrate, then the full production stack"
  help_row "up prod --infra" "Production databases and monitoring, with migrations"
  echo ""
  help_row "down dev" "Stop the dev stack but keep database data on disk"
  help_row "down dev --volumes" "Stop dev and delete database volumes (all local DB data gone)"
  help_row "down prod" "Stop the production compose project"
  echo ""
  help_row "reset dev" "Fresh dev databases: wipe volumes, then infra + migrate again"
  echo ""
  help_row "migrate dev" "Apply schema changes (dev Postgres containers must be up)"
  help_row "migrate dev --repair" "Clear a Liquibase checksum mismatch, then migrate (dev only)"
  help_row "migrate prod" "Apply schema changes on production databases"
  echo ""
  help_row "seed dev" "Load sample taxonomies, studios, performers, and titles via the APIs"
  help_row "seed dev --migrate" "Run migrations first, then load the dev sample catalog"
  help_row "seed dev --reset" "Remove previous seed rows, then load the catalog again"
  help_row "seed dev --reset-db" "Empty the person database, then run a full seed"
  help_row "seed dev --force" "Refresh links and extras for performers already in the catalog"
  echo ""
  help_row "svc list" "Show service short names (auth, gateway, person, …)"
  help_row "svc status" "See what is running, on which ports, locally or in Docker"
  help_row "svc start <alias> [--docker]" "Run one service on your machine (or via Compose)"
  help_row "svc stop <alias>" "Stop that service locally and in Docker if applicable"
  help_row "svc restart <alias> [--docker]" "Stop and start the service again"
  help_row "svc logs <alias>" "Follow log output for a service (Ctrl+C to stop)"
  echo ""
  printf "  ${c_note}Scripts: run.sh here · sample data in seed/ · helpers in lib/${rst}\n"
}

dispatch() {
  local group="${1:-help}"
  group="$(sbp_lower "$group")"
  shift || true
  case "$group" in
    help|-h|--help) show_help ;;
    lib) "$SCRIPT_DIR/lib/install-lib.sh" ;;
    dbeaver) "$SCRIPT_DIR/lib/sync-dbeaver.sh" "env/.env.dev" ;;
    up) cmd_up "$@" ;;
    down) cmd_down "$@" ;;
    reset) cmd_reset "$@" ;;
    migrate) cmd_migrate "$@" ;;
    seed) cmd_seed "$@" ;;
    svc) cmd_svc "$@" ;;
    *)
      echo "Unknown command: $group"
      show_help
      return 1
      ;;
  esac
}

if [[ $# -eq 0 ]]; then
  dispatch help
else
  dispatch "$@"
fi
