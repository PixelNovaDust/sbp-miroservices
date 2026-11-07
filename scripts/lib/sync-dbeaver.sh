#!/usr/bin/env bash
# Writes DBeaver project connections from env/.env.dev (one-click connect in Database Navigator).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
ENV_FILE="${1:-env/.env.dev}"

cd "$REPO_ROOT"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE — copy env/.env.dev.example to env/.env.dev first." >&2
  exit 1
fi

export REPO_ROOT ENV_FILE
python3 <<'PY'
import json
import os
import sys
import urllib.parse
from pathlib import Path

repo_root = Path(os.environ["REPO_ROOT"])
env_file = os.environ["ENV_FILE"]
env_path = repo_root / env_file if not os.path.isabs(env_file) else Path(env_file)

def read_dotenv(path: Path) -> dict[str, str]:
    out: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, _, val = line.partition("=")
        key = key.strip()
        val = val.strip()
        if len(val) >= 2 and val[0] == '"' and val[-1] == '"':
            val = val[1:-1]
        out[key] = val
    return out

def required(env: dict[str, str], key: str) -> str:
    val = env.get(key, "").strip()
    if not val:
        raise SystemExit(f"Missing or empty {key} in {env_file}")
    return val

env_map = read_dotenv(env_path)

databases = [
    ("AUTH", "postgres-jdbc-sbp-dev-auth", "SBP Auth (dev)"),
    ("USER", "postgres-jdbc-sbp-dev-user", "SBP User (dev)"),
    ("PERSON", "postgres-jdbc-sbp-dev-person", "SBP Person (dev)"),
    ("STUDIO", "postgres-jdbc-sbp-dev-studio", "SBP Studio (dev)"),
    ("CONTENT", "postgres-jdbc-sbp-dev-content", "SBP Content (dev)"),
    ("REFERENCE", "postgres-jdbc-sbp-dev-reference", "SBP Reference (dev)"),
    ("FILE_STORAGE", "postgres-jdbc-sbp-dev-file-storage", "SBP File storage (dev)"),
]

connections: dict = {}
for prefix, conn_id, label in databases:
    host = required(env_map, f"{prefix}_DB_HOST")
    port = required(env_map, f"{prefix}_DB_PORT")
    db_name = required(env_map, f"{prefix}_DB_NAME")
    user = required(env_map, f"{prefix}_DB_USERNAME")
    password = required(env_map, f"{prefix}_DB_PASSWORD")
    enc_user = urllib.parse.quote(user, safe="")
    enc_pass = urllib.parse.quote(password, safe="")
    jdbc_url = f"jdbc:postgresql://{host}:{port}/{db_name}?user={enc_user}&password={enc_pass}"

    connections[conn_id] = {
        "provider": "postgresql",
        "driver": "postgres-jdbc",
        "name": label,
        "description": f"Local dev Postgres ({prefix}) from {env_file}",
        "folder": "SBP Dev",
        "save-password": False,
        "read-only": False,
        "configuration": {
            "host": host,
            "port": str(port),
            "database": db_name,
            "url": jdbc_url,
            "configurationType": "URL",
            "type": "dev",
            "properties": {
                "connectTimeout": "20",
                "loginTimeout": "20",
                "user": user,
                "password": password,
            },
            "auth-model": "native",
            "auth-properties": {
                "userName": user,
                "userPassword": password,
            },
        },
    }

dev_type = {
    "name": "Development",
    "color": "rgb(0,128,0)",
    "description": "Local SBP dev databases",
    "auto-commit": True,
    "confirm-execute": False,
    "confirm-data-change": False,
    "auto-close-transactions": False,
}

data_sources = {
    "folders": {},
    "connections": connections,
    "connection-types": {"dev": dev_type},
}

dbeaver_dir = repo_root / ".dbeaver"
dbeaver_dir.mkdir(parents=True, exist_ok=True)

data_sources_path = dbeaver_dir / "data-sources.json"
credentials_path = dbeaver_dir / "credentials-config.json"
metadata_path = dbeaver_dir / "project-metadata.json"
settings_path = dbeaver_dir / "project-settings.json"

json_text = json.dumps(data_sources, indent=2, ensure_ascii=False)
json_text = json_text.replace("\\u0026", "&")
data_sources_path.write_text(json_text, encoding="utf-8")

if credentials_path.exists():
    credentials_path.unlink()

metadata = {
    "id": "sbp-miroservices",
    "name": "sbp-miroservices",
    "description": "SBP microservices dev Postgres connections",
}
settings = {"id": "sbp-miroservices"}
metadata_path.write_text(json.dumps(metadata, indent=2), encoding="utf-8")
settings_path.write_text(json.dumps(settings, indent=2), encoding="utf-8")

print(f"DBeaver project updated: {dbeaver_dir}")
print("Removed stale credentials-config.json (if any).")
print()
print("In DBeaver:")
print("  1. Project → Refresh (or restart DBeaver)")
print(f"  2. Active project must be sbp-miroservices pointing at: {repo_root}")
print("  3. Delete duplicate SBP Dev connections under the General project if present")
print("  4. Connect from SBP Dev folder under sbp-miroservices only")
PY
