#!/usr/bin/env bash
# Service catalog and local/Docker runtime helpers (sourced from run.sh).
# shellcheck disable=SC2034

SBP_JAVA_VERSION="${SBP_JAVA_VERSION:-21}"

sbp_java_major_version() {
  local line ver
  line="$(java -version 2>&1 | head -1)"
  if [[ "$line" =~ version\ \"([0-9]+) ]]; then
    echo "${BASH_REMATCH[1]}"
    return 0
  fi
  echo ""
  return 1
}

sbp_check_java_for_local_run() {
  local major
  major="$(sbp_java_major_version 2>/dev/null || true)"
  if [[ -z "$major" ]]; then
    echo "Java not found in PATH. Install JDK ${SBP_JAVA_VERSION} and set JAVA_HOME." >&2
    return 1
  fi
  if [[ "$major" -lt "$SBP_JAVA_VERSION" ]]; then
    echo "JDK ${major} detected; this project targets JDK ${SBP_JAVA_VERSION}." >&2
    echo "Install Temurin ${SBP_JAVA_VERSION} and: export JAVA_HOME=\$(/usr/libexec/java_home -v ${SBP_JAVA_VERSION})" >&2
    return 1
  fi
  return 0
}

sbp_spring_boot_jvm_args() {
  local jvm_tz="${1:-Asia/Kolkata}"
  local major extra=""
  major="$(sbp_java_major_version 2>/dev/null || true)"
  if [[ -n "$major" && "$major" -ge 22 ]]; then
    extra=" --enable-native-access=ALL-UNNAMED"
  fi
  printf '%s' "-Duser.timezone=${jvm_tz}${extra}"
}

sbp_lower() {
  echo "$1" | tr '[:upper:]' '[:lower:]'
}

# Sets: SBP_SVC_KEY SBP_SVC_MODULE SBP_SVC_COMPOSE SBP_SVC_PORT_ENV SBP_SVC_LABEL
sbp_resolve_service() {
  local name
  name="$(sbp_lower "$(echo "$1" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')")"
  if [[ -z "$name" ]]; then
    echo "Service name is required." >&2
    return 1
  fi
  case "$name" in
    auth-service|auth)
      SBP_SVC_KEY=auth SBP_SVC_MODULE=sbp-auth-services SBP_SVC_COMPOSE=sbp-auth-service
      SBP_SVC_PORT_ENV=AUTH_PORT SBP_SVC_LABEL=Auth ;;
    user-service|user|users)
      SBP_SVC_KEY=user SBP_SVC_MODULE=sbp-user-services SBP_SVC_COMPOSE=sbp-user-service
      SBP_SVC_PORT_ENV=USER_PORT SBP_SVC_LABEL=User ;;
    gateway|api-gateway)
      SBP_SVC_KEY=gateway SBP_SVC_MODULE=sbp-api-gateway SBP_SVC_COMPOSE=sbp-api-gateway
      SBP_SVC_PORT_ENV=GATEWAY_PORT SBP_SVC_LABEL="API Gateway" ;;
    discovery|eureka)
      SBP_SVC_KEY=discovery SBP_SVC_MODULE=sbp-discovery-server SBP_SVC_COMPOSE=sbp-discovery-server
      SBP_SVC_PORT_ENV=DISCOVERY_PORT SBP_SVC_LABEL=Discovery ;;
    admin-service|admin)
      SBP_SVC_KEY=admin SBP_SVC_MODULE=sbp-admin-service SBP_SVC_COMPOSE=sbp-admin-service
      SBP_SVC_PORT_ENV=ADMIN_PORT SBP_SVC_LABEL=Admin ;;
    person-service|person)
      SBP_SVC_KEY=person SBP_SVC_MODULE=sbp-person-service SBP_SVC_COMPOSE=sbp-person-service
      SBP_SVC_PORT_ENV=PERSON_PORT SBP_SVC_LABEL=Person ;;
    studio-service|studio)
      SBP_SVC_KEY=studio SBP_SVC_MODULE=sbp-studio-service SBP_SVC_COMPOSE=sbp-studio-service
      SBP_SVC_PORT_ENV=STUDIO_PORT SBP_SVC_LABEL=Studio ;;
    content-service|content)
      SBP_SVC_KEY=content SBP_SVC_MODULE=sbp-content-service SBP_SVC_COMPOSE=sbp-content-service
      SBP_SVC_PORT_ENV=CONTENT_PORT SBP_SVC_LABEL=Content ;;
    reference-service|reference)
      SBP_SVC_KEY=reference SBP_SVC_MODULE=sbp-reference-service SBP_SVC_COMPOSE=sbp-reference-service
      SBP_SVC_PORT_ENV=REFERENCE_PORT SBP_SVC_LABEL=Reference ;;
    file-storage-service|file-storage|files)
      SBP_SVC_KEY=file-storage SBP_SVC_MODULE=sbp-file-storage-service SBP_SVC_COMPOSE=sbp-file-storage-service
      SBP_SVC_PORT_ENV=FILE_STORAGE_PORT SBP_SVC_LABEL="File storage" ;;
    *)
      echo "Unknown service '$1'. Try: auth, user, gateway, discovery, admin, person, studio, content, reference, file-storage (and -service variants)." >&2
      return 1
      ;;
  esac
}

sbp_catalog_by_key() {
  case "$1" in
    auth) sbp_resolve_service auth ;;
    user) sbp_resolve_service user ;;
    gateway) sbp_resolve_service gateway ;;
    discovery) sbp_resolve_service discovery ;;
    admin) sbp_resolve_service admin ;;
    person) sbp_resolve_service person ;;
    studio) sbp_resolve_service studio ;;
    content) sbp_resolve_service content ;;
    reference) sbp_resolve_service reference ;;
    file-storage) sbp_resolve_service file-storage ;;
    *) echo "No catalog entry for key $1" >&2; return 1 ;;
  esac
}

sbp_resolve_gateway_base_url() {
  local candidate="${1:-}"
  if [[ -n "${candidate// /}" ]]; then
    candidate="${candidate// /}"
    candidate="${candidate%/}"
    if [[ "$candidate" =~ ^https?://[^[:space:]/]+ ]]; then
      echo "$candidate"
      return 0
    fi
  fi
  local key val normalized
  for key in GATEWAY_SERVICE_URL API_GATEWAY_URL GATEWAY_BASE_URL GATEWAY_URL; do
    val="${!key:-}"
    if [[ -n "${val// /}" ]]; then
      normalized="${val// /}"
      normalized="${normalized%/}"
      if [[ "$normalized" =~ ^https?://[^[:space:]/]+ ]]; then
        echo "$normalized"
        return 0
      fi
    fi
  done
  local port="${GATEWAY_PORT:-8080}"
  port="$(echo "$port" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  echo "http://localhost:${port}"
}

sbp_normalize_port_env_value() {
  local val=$1
  val="${val#:}"
  val="$(echo "$val" | sed 's/--$//')"
  echo "$val"
}

# Generic PORT (Codespaces/shell typos like PORT=:8080) breaks Reactor Netty / gateway startup.
# This stack uses GATEWAY_PORT, AUTH_PORT, etc. — drop PORT silently when loading env.
sbp_sanitize_paas_env() {
  if [[ -n "${PORT:-}" ]]; then
    unset PORT
  fi
}

sbp_import_dev_env() {
  local env_file_path=$1
  if [[ ! -f "$env_file_path" ]]; then
    echo "Missing $env_file_path" >&2
    return 1
  fi
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="$(echo "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    [[ -z "$line" || "$line" == \#* ]] && continue
    [[ "$line" != *=* ]] && continue
    local name="${line%%=*}"
    local val="${line#*=}"
    name="$(echo "$name" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    val="$(echo "$val" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    if [[ ${#val} -ge 2 && "$val" == \"*\" && "$val" == *\" ]]; then
      val="${val:1:${#val}-2}"
    fi
    if [[ "$name" == *_PORT ]]; then
      val="$(sbp_normalize_port_env_value "$val")"
    fi
    if [[ "$name" == "PORT" ]]; then
      continue
    fi
    export "${name}=${val}"
  done < "$env_file_path" || true
  export SPRING_PROFILES_ACTIVE=dev
  export APP_TIMEZONE="${APP_TIMEZONE:-Asia/Kolkata}"
  sbp_sanitize_paas_env
}

sbp_runtime_dir() {
  local repo_root=$1
  local dir="$repo_root/scripts/.runtime"
  mkdir -p "$dir"
  echo "$dir"
}

sbp_runtime_state_path() {
  local repo_root=$1 service_key=$2
  echo "$(sbp_runtime_dir "$repo_root")/${service_key}.json"
}

sbp_runtime_log_path() {
  local repo_root=$1 service_key=$2
  echo "$(sbp_runtime_dir "$repo_root")/${service_key}.log"
}

sbp_runtime_err_log_path() {
  local repo_root=$1 service_key=$2
  echo "$(sbp_runtime_dir "$repo_root")/${service_key}.err.log"
}

sbp_write_startup_log_tail() {
  local log_path=$1 err_log_path=$2
  local tail_lines=${3:-30}
  local shown=0
  for pair in "stdout:$log_path" "stderr:$err_log_path"; do
    local label="${pair%%:*}"
    local path="${pair#*:}"
    if [[ -f "$path" ]]; then
      shown=1
      echo "--- Last ${tail_lines} lines (${label}): ${path} ---"
      tail -n "$tail_lines" "$path" 2>/dev/null || true
    fi
  done
  if [[ "$shown" -eq 0 ]]; then
    echo "No log files found yet. Use: ./scripts/run.sh svc logs <alias>"
  fi
}

sbp_read_runtime_state() {
  local path=$1
  [[ -f "$path" ]] || return 0
  cat "$path"
}

sbp_remove_runtime_state() {
  local path=$1
  [[ -f "$path" ]] && rm -f "$path"
}

sbp_process_alive() {
  local pid=$1
  [[ -z "$pid" || "$pid" -le 0 ]] && return 1
  kill -0 "$pid" 2>/dev/null
}

sbp_port_owner_pid() {
  local port=$1
  [[ -z "$port" || "$port" -le 0 ]] && return 0
  # lsof exits 1 when nothing is listening; with pipefail that must not abort callers.
  ( lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true ) | head -1
}

sbp_maven_dist_version() {
  local repo_root=$1
  local props="$repo_root/.mvn/wrapper/maven-wrapper.properties"
  if [[ ! -f "$props" ]]; then
    echo ""
    return 1
  fi
  local url
  url="$(grep -E '^distributionUrl=' "$props" | cut -d= -f2- | tr -d '[:space:]')"
  # .../apache-maven-3.9.9-bin.zip -> 3.9.9
  if [[ "$url" =~ apache-maven-([0-9.]+)-bin ]]; then
    echo "${BASH_REMATCH[1]}"
    return 0
  fi
  echo ""
  return 1
}

sbp_maven_wrapper_home() {
  local repo_root=$1
  local version
  version="$(sbp_maven_dist_version "$repo_root")"
  [[ -n "$version" ]] || return 1
  local props="$repo_root/.mvn/wrapper/maven-wrapper.properties"
  local url hash dir
  url="$(grep -E '^distributionUrl=' "$props" | cut -d= -f2- | tr -d '[:space:]')"
  hash="$(python3 -c "
h=0
for c in '''$url''':
    h=(h*31+ord(c))%4294967296
print(format(h,'x'))
")"
  dir="${MAVEN_USER_HOME:-${HOME}/.m2}/wrapper/dists/apache-maven-${version}/${hash}"
  echo "$dir"
}

sbp_maven_boot_ok() {
  local home=$1 jar
  [[ -d "$home/boot" ]] || return 1
  jar="$(find "$home/boot" -maxdepth 1 -name 'plexus-classworlds-*.jar' 2>/dev/null | head -1)"
  [[ -n "$jar" ]]
}

# Ensures mvnw works (re-downloads Maven if wrapper cache under ~/.m2 is incomplete).
sbp_ensure_maven_wrapper() {
  local repo_root=$1
  local mvnw="$repo_root/mvnw"
  if [[ ! -f "$mvnw" ]]; then
    echo "Missing $mvnw" >&2
    return 1
  fi
  if [[ ! -x "$mvnw" ]]; then
    chmod +x "$mvnw" 2>/dev/null || true
  fi

  local home out
  home="$(sbp_maven_wrapper_home "$repo_root" 2>/dev/null || true)"
  if [[ -n "$home" && -d "$home" ]] && ! sbp_maven_boot_ok "$home"; then
    echo "Maven wrapper cache is incomplete (missing boot jars under $home)."
    echo "Removing corrupt cache and re-downloading Maven..."
    rm -rf "$home"
  fi

  if out="$(/bin/sh "$mvnw" -v 2>&1)"; then
    return 0
  fi
  if echo "$out" | grep -q 'ClassNotFoundException.*Launcher'; then
    home="$(sbp_maven_wrapper_home "$repo_root" 2>/dev/null || true)"
    if [[ -n "$home" && -d "$home" ]]; then
      echo "Maven still broken after repair attempt; removing $home and retrying once..."
      rm -rf "$home"
      if /bin/sh "$mvnw" -v >/dev/null 2>&1; then
        return 0
      fi
    fi
  fi
  echo "Maven wrapper failed (mvnw -v):" >&2
  echo "$out" >&2
  echo "Try: rm -rf \"\${HOME}/.m2/wrapper/dists/apache-maven-$(sbp_maven_dist_version "$repo_root")\" && /bin/sh \"$mvnw\" -v" >&2
  echo "Use JDK ${SBP_JAVA_VERSION}, set JAVA_HOME if needed, and run without sudo." >&2
  return 1
}

sbp_run_mvnw() {
  local repo_root=$1
  shift
  sbp_ensure_maven_wrapper "$repo_root" || return 1
  /bin/sh "$repo_root/mvnw" "$@"
}

sbp_stop_process_tree() {
  local pid=$1
  [[ -z "$pid" || "$pid" -le 0 ]] && return 0
  sbp_process_alive "$pid" || return 0
  local child
  for child in $(pgrep -P "$pid" 2>/dev/null || true); do
    sbp_stop_process_tree "$child"
  done
  kill -TERM "$pid" 2>/dev/null || true
  sleep 0.5
  kill -KILL "$pid" 2>/dev/null || true
}

sbp_stop_java_listener_on_port() {
  local port=$1 label=$2
  [[ -z "$port" || "$port" -le 0 ]] && return 1
  local port_pid
  port_pid="$(sbp_port_owner_pid "$port")"
  [[ -z "$port_pid" ]] && return 1
  local comm args
  comm="$(ps -p "$port_pid" -o comm= 2>/dev/null || true)"
  args="$(ps -p "$port_pid" -o args= 2>/dev/null || true)"
  if [[ "$comm" != *java* && "$args" != *java* ]]; then
    echo "Warning: $label port $port is held by non-JVM PID $port_pid ($comm); not killing." >&2
    return 1
  fi
  sbp_stop_process_tree "$port_pid"
  echo "Stopped JVM on port $port (PID $port_pid)."
  return 0
}

sbp_service_port() {
  local port_env=$1 env_file_path=$2
  local port="${!port_env:-}"
  port="$(echo "$port" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
  if [[ -z "$port" || "$port" -le 0 ]]; then
    echo "Port env $port_env is not set in $env_file_path" >&2
    return 1
  fi
  echo "$port"
}

sbp_compose_dev() {
  local repo_root=$1 env_file=$2
  shift 2
  docker compose -p sbp-dev --env-file "$env_file" -f "$repo_root/docker/docker-compose.dev.yaml" "$@"
}

sbp_get_docker_service_row() {
  local repo_root=$1 env_file=$2 compose_service=$3
  local json line
  json="$(sbp_compose_dev "$repo_root" "$env_file" ps --format json "$compose_service" 2>/dev/null || true)"
  [[ -z "${json// /}" ]] && return 0
  while IFS= read -r line; do
    [[ -z "${line// /}" ]] && continue
    python3 -c "
import json, sys
o = json.loads(sys.argv[1])
if o.get('Service') == sys.argv[2]:
    print(json.dumps(o))
    sys.exit(0)
" "$line" "$compose_service" 2>/dev/null && return 0
  done <<< "$json"
}

sbp_docker_service_running() {
  local row
  row="$(sbp_get_docker_service_row "$1" "$2" "$3")"
  [[ -n "$row" ]] && { echo "$row" | grep -q '"State".*running' || return 1; }
  return 1
}

sbp_test_service_running() {
  local repo_root=$1 env_file=$2
  sbp_import_dev_env "$env_file"
  local port
  port="$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file")"

  local state_path state_json pid
  state_path="$(sbp_runtime_state_path "$repo_root" "$SBP_SVC_KEY")"
  state_json="$(sbp_read_runtime_state "$state_path")"
  if [[ -n "$state_json" ]]; then
    pid="$(python3 -c "import json,sys; print(json.load(sys.stdin).get('pid',0))" <<<"$state_json" 2>/dev/null || echo 0)"
    if sbp_process_alive "$pid"; then
      return 0
    fi
  fi
  [[ -n "$(sbp_port_owner_pid "$port")" ]] && return 0
  sbp_docker_service_running "$repo_root" "$env_file" "$SBP_SVC_COMPOSE" && return 0
  return 1
}

sbp_wait_port_ready() {
  local port=$1 label=$2 timeout_sec=${3:-120} watch_pid=${4:-}
  [[ -z "$port" || "$port" -le 0 ]] && return 0
  local deadline=$((SECONDS + timeout_sec))
  while (( SECONDS < deadline )); do
    if [[ -n "$watch_pid" ]] && ! sbp_process_alive "$watch_pid"; then
      echo "Warning: $label process (PID $watch_pid) exited before port $port was ready." >&2
      return 1
    fi
    if [[ -n "$(sbp_port_owner_pid "$port")" ]]; then
      echo "$label is listening on port $port."
      return 0
    fi
    sleep 2
  done
  echo "Warning: $label did not bind port $port within ${timeout_sec}s." >&2
  return 1
}

sbp_wait_port_free() {
  local port=$1 label=$2 timeout_sec=${3:-45}
  [[ -z "$port" || "$port" -le 0 ]] && return 0
  local deadline=$((SECONDS + timeout_sec))
  while (( SECONDS < deadline )); do
    [[ -z "$(sbp_port_owner_pid "$port")" ]] && return 0
    sleep 1
  done
  local owner
  owner="$(sbp_port_owner_pid "$port")"
  if [[ -n "$owner" ]]; then
    echo "Warning: $label port $port still in use (PID $owner) after ${timeout_sec}s." >&2
  fi
}

# Domain services assume Discovery, API Gateway, and File storage are up first.
sbp_caller_needs_file_storage_dep() {
  local caller_key=$1
  [[ "$caller_key" != discovery && "$caller_key" != gateway && "$caller_key" != file-storage ]]
}

sbp_ensure_dependency_running_local() {
  local repo_root=$1 env_file=$2 dep_key=$3 start_msg=$4 port_label=$5
  sbp_catalog_by_key "$dep_key"
  if sbp_test_service_running "$repo_root" "$env_file"; then
    return 0
  fi
  echo "$start_msg"
  if ! sbp_start_service_local "$repo_root" "$env_file" 1; then
    echo "Failed to start $port_label (required dependency)." >&2
    return 1
  fi
  sbp_import_dev_env "$env_file"
  sbp_wait_port_ready "$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file")" "$port_label" || true
}

sbp_ensure_dependencies_local() {
  local repo_root=$1 env_file=$2 caller_key=$3

  if [[ "$caller_key" != discovery ]]; then
    sbp_ensure_dependency_running_local "$repo_root" "$env_file" discovery \
      "Dependency: starting Discovery (Eureka)..." "Discovery" || return 1
  fi

  if [[ "$caller_key" != discovery && "$caller_key" != gateway ]]; then
    sbp_ensure_dependency_running_local "$repo_root" "$env_file" gateway \
      "Dependency: starting API Gateway..." "API Gateway" || return 1
  fi

  if sbp_caller_needs_file_storage_dep "$caller_key"; then
    sbp_ensure_dependency_running_local "$repo_root" "$env_file" file-storage \
      "Dependency: starting File storage..." "File storage" || return 1
  fi

  sbp_catalog_by_key "$caller_key"
}

# Caller must set SBP_SVC_* before calling start helpers.
sbp_start_service_local() {
  local repo_root=$1 env_file=$2 skip_deps=${3:-0}
  local caller_key=$SBP_SVC_KEY

  if [[ "$skip_deps" -eq 0 ]]; then
    sbp_ensure_dependencies_local "$repo_root" "$env_file" "$caller_key" || return 1
  fi

  local state_path log_path err_log_path
  state_path="$(sbp_runtime_state_path "$repo_root" "$SBP_SVC_KEY")"
  log_path="$(sbp_runtime_log_path "$repo_root" "$SBP_SVC_KEY")"
  err_log_path="$(sbp_runtime_err_log_path "$repo_root" "$SBP_SVC_KEY")"

  local state_json pid
  state_json="$(sbp_read_runtime_state "$state_path")"
  if [[ -n "$state_json" ]]; then
    pid="$(python3 -c "import json,sys; print(json.load(sys.stdin).get('pid',0))" <<<"$state_json" 2>/dev/null || echo 0)"
    if sbp_process_alive "$pid"; then
      echo "Already running (local PID $pid). Use 'stop ${SBP_SVC_KEY}' first."
      return 0
    fi
  fi
  if sbp_test_service_running "$repo_root" "$env_file"; then
    echo "${SBP_SVC_LABEL} already running (port or Docker)."
    return 0
  fi

  sbp_import_dev_env "$env_file"
  local port
  port="$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file")"

  local port_pid
  port_pid="$(sbp_port_owner_pid "$port")"
  if [[ -n "$port_pid" ]]; then
    echo "Port $port is already in use (PID $port_pid). Stop that process or change ${SBP_SVC_PORT_ENV} in $env_file." >&2
    return 1
  fi

  if ! sbp_check_java_for_local_run; then
    return 1
  fi
  if ! sbp_ensure_maven_wrapper "$repo_root"; then
    return 1
  fi
  local mvnw="$repo_root/mvnw"
  local pom="$repo_root/${SBP_SVC_MODULE}/pom.xml"
  if [[ ! -f "$pom" ]]; then
    echo "Missing module POM: $pom" >&2
    return 1
  fi
  local jvm_tz="${APP_TIMEZONE:-Asia/Kolkata}"
  local boot_jvm_args
  boot_jvm_args="$(sbp_spring_boot_jvm_args "$jvm_tz")"

  : >"$log_path"
  : >"$err_log_path"

  echo "Starting ${SBP_SVC_LABEL} in background (port $port, log: $log_path)..."
  cd "$repo_root"
  nohup /bin/sh "$mvnw" -f "$pom" spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    "-Dspring-boot.run.jvmArguments=${boot_jvm_args}" \
    >>"$log_path" 2>>"$err_log_path" &
  pid=$!

  local started_at
  started_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  python3 <<PY
import json
state = {
    "mode": "local",
    "key": "$SBP_SVC_KEY",
    "module": "$SBP_SVC_MODULE",
    "compose": "$SBP_SVC_COMPOSE",
    "port": int("$port"),
    "pid": int("$pid"),
    "logFile": "$log_path",
    "errLogFile": "$err_log_path",
    "startedAt": "$started_at",
}
with open("$state_path", "w", encoding="utf-8") as f:
    json.dump(state, f, separators=(",", ":"))
PY

  echo "Started ${SBP_SVC_LABEL} - Maven wrapper PID $pid, HTTP port $port"
  echo "Tail logs: ./scripts/run.sh svc logs ${SBP_SVC_KEY}"

  local wait_sec=180
  [[ "$skip_deps" -eq 1 ]] && wait_sec=90

  local ready=0
  if sbp_wait_port_ready "$port" "$SBP_SVC_LABEL" "$wait_sec" "$pid"; then
    ready=1
  fi
  local wrapper_alive=0
  sbp_process_alive "$pid" && wrapper_alive=1

  if [[ "$ready" -eq 0 ]]; then
    if [[ "$wrapper_alive" -eq 0 ]]; then
      echo "Maven process exited before the app listened on port $port."
      sbp_write_startup_log_tail "$log_path" "$err_log_path"
      return 1
    fi
    if [[ "$skip_deps" -eq 1 ]]; then
      echo "Dependency ${SBP_SVC_LABEL} still starting in background (continuing)."
      return 0
    fi
    echo "Still starting (Maven compile can take several minutes on first run). Tail logs in another window:"
    sbp_write_startup_log_tail "$log_path" "$err_log_path"
  fi
}

sbp_ensure_dependency_running_docker() {
  local repo_root=$1 env_file=$2 dep_key=$3 start_msg=$4 port_label=$5
  sbp_catalog_by_key "$dep_key"
  if sbp_test_service_running "$repo_root" "$env_file"; then
    return 0
  fi
  echo "$start_msg"
  sbp_compose_dev "$repo_root" "$env_file" up -d --build "$SBP_SVC_COMPOSE"
  sbp_import_dev_env "$env_file"
  sbp_wait_port_ready "$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file")" "$port_label" || true
}

sbp_ensure_dependencies_docker() {
  local repo_root=$1 env_file=$2 caller_key=$3

  if [[ "$caller_key" != discovery ]]; then
    sbp_ensure_dependency_running_docker "$repo_root" "$env_file" discovery \
      "Dependency: starting Discovery (Docker)..." "Discovery" || return 1
  fi

  if [[ "$caller_key" != discovery && "$caller_key" != gateway ]]; then
    sbp_ensure_dependency_running_docker "$repo_root" "$env_file" gateway \
      "Dependency: starting API Gateway (Docker)..." "API Gateway" || return 1
  fi

  if sbp_caller_needs_file_storage_dep "$caller_key"; then
    sbp_ensure_dependency_running_docker "$repo_root" "$env_file" file-storage \
      "Dependency: starting File storage (Docker)..." "File storage" || return 1
  fi

  sbp_catalog_by_key "$caller_key"
}

sbp_start_service_docker() {
  local repo_root=$1 env_file=$2
  local caller_key=$SBP_SVC_KEY
  sbp_ensure_dependencies_docker "$repo_root" "$env_file" "$caller_key"
  echo "Starting ${SBP_SVC_LABEL} via Docker dev compose..."
  sbp_compose_dev "$repo_root" "$env_file" up -d --build "$SBP_SVC_COMPOSE"
  sbp_import_dev_env "$env_file"
  local port
  port="$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file")"
  sbp_wait_port_ready "$port" "$SBP_SVC_LABEL" 120
  echo "Docker logs: ./scripts/run.sh svc logs ${SBP_SVC_KEY}"
}

sbp_stop_service_local() {
  local repo_root=$1 env_file=$2
  local state_path
  state_path="$(sbp_runtime_state_path "$repo_root" "$SBP_SVC_KEY")"
  local state_json pid stopped=0

  state_json="$(sbp_read_runtime_state "$state_path")"
  if [[ -n "$state_json" ]]; then
    pid="$(python3 -c "import json,sys; print(json.load(sys.stdin).get('pid',0))" <<<"$state_json" 2>/dev/null || echo 0)"
    if sbp_process_alive "$pid"; then
      sbp_stop_process_tree "$pid"
      echo "Stopped local ${SBP_SVC_LABEL} (PID $pid, process tree)."
      stopped=1
    fi
  fi

  sbp_import_dev_env "$env_file"
  local port
  port="$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file" 2>/dev/null || echo 0)"
  if sbp_stop_java_listener_on_port "$port" "$SBP_SVC_LABEL"; then
    stopped=1
  fi

  sbp_remove_runtime_state "$state_path"
  if [[ "$stopped" -eq 0 ]]; then
    echo "No local ${SBP_SVC_LABEL} process found (checked state file and port $port)."
  fi
}

sbp_stop_service_docker() {
  local repo_root=$1 env_file=$2
  sbp_compose_dev "$repo_root" "$env_file" stop "$SBP_SVC_COMPOSE"
  echo "Stopped Docker container ${SBP_SVC_COMPOSE}."
}

sbp_restart_service() {
  local repo_root=$1 env_file=$2 use_docker=${3:-0}
  echo "Restarting ${SBP_SVC_LABEL}..."
  sbp_stop_service_local "$repo_root" "$env_file"
  sbp_stop_service_docker "$repo_root" "$env_file"
  sbp_import_dev_env "$env_file"
  local port
  port="$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file")"
  sbp_wait_port_free "$port" "$SBP_SVC_LABEL"
  if [[ -n "$(sbp_port_owner_pid "$port")" ]]; then
    sbp_stop_java_listener_on_port "$port" "$SBP_SVC_LABEL" || true
    sbp_wait_port_free "$port" "$SBP_SVC_LABEL" 15
  fi
  if [[ "$use_docker" -eq 1 ]]; then
    sbp_start_service_docker "$repo_root" "$env_file"
  else
    sbp_start_service_local "$repo_root" "$env_file" 0
  fi
}

sbp_show_service_aliases() {
  echo "Service aliases (use with svc start / svc stop):"
  echo "  auth - Auth"
  echo "  user - User"
  echo "  gateway - API Gateway"
  echo "  discovery - Discovery"
  echo "  admin - Admin"
  echo "  person - Person"
  echo "  studio - Studio"
  echo "  content - Content"
  echo "  reference - Reference"
  echo "  file-storage - File storage"
}

# Sets db_port and db_status (Connected|failed|N/A) from env + optional Postgres probe.
sbp_service_db_status() {
  local repo_root=$1 env_file=$2
  local prefix="" compose_db=""
  case "$SBP_SVC_KEY" in
    auth) prefix=AUTH; compose_db=auth-db ;;
    user) prefix=USER; compose_db=user-db ;;
    person) prefix=PERSON; compose_db=person-db ;;
    studio) prefix=STUDIO; compose_db=studio-db ;;
    content) prefix=CONTENT; compose_db=content-db ;;
    reference) prefix=REFERENCE; compose_db=reference-db ;;
    file-storage) prefix=FILE_STORAGE; compose_db=file-storage-db ;;
    *)
      db_port="N/A"
      db_status="N/A"
      return 0
      ;;
  esac

  local host_var="${prefix}_DB_HOST" port_var="${prefix}_DB_PORT"
  local user_var="${prefix}_DB_USERNAME" name_var="${prefix}_DB_NAME" pass_var="${prefix}_DB_PASSWORD"
  local host="${!host_var:-localhost}"
  local dbp="${!port_var:-}"
  local user="${!user_var:-}"
  local dbname="${!name_var:-}"
  local pass="${!pass_var:-}"
  dbp="$(sbp_normalize_port_env_value "${dbp:-}" 2>/dev/null || echo "${dbp:-}")"
  if [[ -z "${dbp// /}" || "$dbp" -le 0 ]]; then
    db_port="-"
    db_status=FAILED
    return 0
  fi
  db_port="$dbp"

  local ok=0
  if command -v pg_isready >/dev/null 2>&1; then
    if PGPASSWORD="$pass" pg_isready -h "$host" -p "$dbp" -U "$user" -d "$dbname" -q 2>/dev/null; then
      ok=1
    fi
  elif sbp_docker_service_running "$repo_root" "$env_file" "$compose_db" 2>/dev/null; then
    if sbp_compose_dev "$repo_root" "$env_file" exec -T "$compose_db" \
      pg_isready -U "$user" -d "$dbname" -q 2>/dev/null; then
      ok=1
    fi
  elif command -v nc >/dev/null 2>&1 && nc -z "$host" "$dbp" 2>/dev/null; then
    ok=1
  fi

  if [[ "$ok" -eq 1 ]]; then
    db_status=CONNECTED
  else
    db_status=FAILED
  fi
}

sbp_print_svc_status_hints() {
  local c_note=$1 c_rst=$2
  printf "\n"
  printf "${c_note}Environment: env/.env.dev  ·  runtime state: scripts/.runtime/${c_rst}\n"
  printf "${c_note}Start:  ./scripts/run.sh svc start <alias>   Docker: add --docker${c_rst}\n"
  printf "${c_note}        Ensures Discovery → API Gateway → File storage (if needed), then your service.${c_rst}\n"
  printf "${c_note}Logs:   ./scripts/run.sh svc logs <alias>   Aliases: ./scripts/run.sh svc list${c_rst}\n"
}

# Fixed display order (not sorted by port); each row prints as soon as it is computed.
SBP_SVC_STATUS_ORDER=(discovery gateway auth user admin person studio content reference file-storage)

sbp_print_service_status_line() {
  local c_run=$1 c_stop=$2 c_head=$3 c_db_ok=$4 c_db_fail=$5 c_rst=$6
  local label=$7 alias=$8 port=$9 proc_id=${10} since=${11} row_status=${12} mode=${13} db_port=${14} db_status=${15}
  local color db_color
  row_status="$(sbp_upper "$row_status")"
  db_status="$(sbp_upper "$db_status")"
  if [[ "$row_status" == RUNNING ]]; then
    color="$c_run"
  else
    color="$c_stop"
  fi
  case "$db_status" in
    CONNECTED) db_color="$c_db_ok" ;;
    FAILED) db_color="$c_db_fail" ;;
    *) db_color="$c_head" ;;
  esac
  printf "${color}%-16s %-12s %-6s %-8s %-20s %-8s %-14s${c_rst} %-7s ${db_color}%-10s${c_rst}\n" \
    "$label" "$alias" "$port" "$proc_id" "$since" "$row_status" "$mode" "$db_port" "$db_status"
}

sbp_upper() {
  printf '%s' "$1" | tr '[:lower:]' '[:upper:]'
}

sbp_emit_service_status_row() {
  local repo_root=$1 env_file=$2 key=$3
  local c_run=$4 c_stop=$5 c_head=$6 c_db_ok=$7 c_db_fail=$8 c_rst=$9
  sbp_catalog_by_key "$key"
  local port state_path state_json pid row_status mode proc_id since
  port="$(sbp_service_port "$SBP_SVC_PORT_ENV" "$env_file" 2>/dev/null || echo 0)"
  state_path="$(sbp_runtime_state_path "$repo_root" "$SBP_SVC_KEY")"
  state_json="$(sbp_read_runtime_state "$state_path")"
  row_status=stopped
  proc_id="-"
  since="-"
  mode="-"

  if [[ -n "$state_json" ]]; then
    read -r pid <<< "$(python3 -c "import json,sys; print(json.load(sys.stdin).get('pid',0))" <<<"$state_json" 2>/dev/null || echo 0)"
    if sbp_process_alive "$pid"; then
      row_status=running
      proc_id="$pid"
      mode=local
      since="$(python3 -c "
import json, sys
from datetime import datetime, timezone
s = json.load(sys.stdin).get('startedAt','')
try:
    started = datetime.fromisoformat(s.replace('Z','+00:00'))
    delta = datetime.now(timezone.utc) - started
    print(str(delta).split('.')[0])
except Exception:
    print(s)
" <<<"$state_json" 2>/dev/null || echo "-")"
    fi
  fi

  if [[ "$row_status" == stopped ]]; then
    pid="$(sbp_port_owner_pid "$port")"
    if [[ -n "$pid" ]]; then
      row_status=running
      proc_id="$pid"
      mode="local (port)"
      since="$(ps -p "$pid" -o etime= 2>/dev/null | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' || echo "-")"
    fi
  fi

  if sbp_docker_service_running "$repo_root" "$env_file" "$SBP_SVC_COMPOSE"; then
    local row cid docker_status
    row="$(sbp_get_docker_service_row "$repo_root" "$env_file" "$SBP_SVC_COMPOSE")"
    cid="$(python3 -c "import json,sys; o=json.loads(sys.argv[1]); print(o.get('ID','')[:12])" "$row" 2>/dev/null || echo "")"
    docker_status="$(python3 -c "import json,sys; o=json.loads(sys.argv[1]); print(o.get('Status',''))" "$row" 2>/dev/null || echo "")"
    if [[ "$row_status" == stopped ]]; then
      row_status=running
      mode=docker
      proc_id="$cid"
      since="$docker_status"
    else
      mode="${mode} + docker"
    fi
  fi

  local db_port db_status
  sbp_service_db_status "$repo_root" "$env_file"

  sbp_print_service_status_line "$c_run" "$c_stop" "$c_head" "$c_db_ok" "$c_db_fail" "$c_rst" \
    "$SBP_SVC_LABEL" "$SBP_SVC_KEY" "$port" "$proc_id" "$since" "$row_status" "$mode" "$db_port" "$db_status"
}

sbp_show_service_info() {
  local repo_root=$1 env_file=$2
  sbp_import_dev_env "$env_file"

  local c_run c_stop c_head c_rst c_db_ok c_db_fail
  if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
    c_run=$'\033[32m'
    c_stop=$'\033[31m'
    c_head=$'\033[2m'
    c_db_ok=$'\033[32m'
    c_db_fail=$'\033[31m'
    c_rst=$'\033[0m'
  else
    c_run="" c_stop="" c_head="" c_db_ok="" c_db_fail="" c_rst=""
  fi

  printf "${c_head}%-16s %-12s %-6s %-8s %-20s %-8s %-14s %-7s %s${c_rst}\n" \
    Service Alias Port PID Uptime Status Mode "DB Port" "DB Status"

  local key
  for key in "${SBP_SVC_STATUS_ORDER[@]}"; do
    sbp_emit_service_status_row "$repo_root" "$env_file" "$key" \
      "$c_run" "$c_stop" "$c_head" "$c_db_ok" "$c_db_fail" "$c_rst" || true
  done

  sbp_print_svc_status_hints "$c_head" "$c_rst"
}

sbp_tail_service_logs() {
  local repo_root=$1 env_file=$2
  local log_path state_path state_json err_log_path
  log_path="$(sbp_runtime_log_path "$repo_root" "$SBP_SVC_KEY")"
  state_path="$(sbp_runtime_state_path "$repo_root" "$SBP_SVC_KEY")"
  state_json="$(sbp_read_runtime_state "$state_path")"
  err_log_path="$(sbp_runtime_err_log_path "$repo_root" "$SBP_SVC_KEY")"

  local docker_running=0
  sbp_docker_service_running "$repo_root" "$env_file" "$SBP_SVC_COMPOSE" && docker_running=1

  local state_pid=0 state_mode=""
  if [[ -n "$state_json" ]]; then
    state_pid="$(python3 -c "import json,sys; print(json.load(sys.stdin).get('pid',0))" <<<"$state_json" 2>/dev/null || echo 0)"
    state_mode="$(python3 -c "import json,sys; print(json.load(sys.stdin).get('mode',''))" <<<"$state_json" 2>/dev/null || echo "")"
    err_log_path="$(python3 -c "import json,sys; print(json.load(sys.stdin).get('errLogFile', sys.argv[1]))" "$err_log_path" <<<"$state_json" 2>/dev/null || echo "$err_log_path")"
  fi

  if [[ "$docker_running" -eq 1 ]] && { [[ -z "$state_json" ]] || [[ "$state_mode" == docker ]] || ! sbp_process_alive "$state_pid"; }; then
    echo "Tailing Docker logs for ${SBP_SVC_COMPOSE} (Ctrl+C to stop)..."
    sbp_compose_dev "$repo_root" "$env_file" logs -f --tail 100 "$SBP_SVC_COMPOSE"
    return 0
  fi

  if [[ ! -f "$log_path" ]]; then
    if [[ "$docker_running" -eq 1 ]]; then
      echo "No local log file; using Docker logs..."
      sbp_compose_dev "$repo_root" "$env_file" logs -f --tail 100 "$SBP_SVC_COMPOSE"
      return 0
    fi
    echo "Log file not found: $log_path. Start the service first with: ./scripts/run.sh svc start ${SBP_SVC_KEY}" >&2
    return 1
  fi

  echo "Tailing $log_path (Ctrl+C to stop)..."
  if [[ -f "$err_log_path" ]]; then
    echo "stderr file: $err_log_path (shown first if non-empty, then stdout stream)"
    if [[ -s "$err_log_path" ]]; then
      tail -n 40 "$err_log_path"
      echo "--- stdout (live) ---"
    fi
  fi
  tail -n 80 -f "$log_path"
}
