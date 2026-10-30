# Development

## Prerequisites

- JDK 17+
- **No global Maven needed** — use repo **`mvnw.cmd`** (Windows) or VS Code task **Maven: install sbp-lib-common**
- Docker Desktop
- GNU Make (Git Bash / WSL on Windows)
- VS Code with Extension Pack for Java and Spring Boot Extension Pack

## Shared library (`sbp-lib-common`)

Services depend on `com.sbp:sbp-lib-common:1.0.0`, which is **not** on Maven Central. Install it once (or after lib changes):

```powershell
# Windows (repo root)
.\mvnw.cmd -f sbp-lib-common/pom.xml clean install -DskipTests

# Or VS Code: Terminal → Run Task → Maven: install sbp-lib-common
# Or: .\scripts\install-lib.ps1
# Or Docker: Run Task → Maven: install sbp-lib-common (Docker)
```

Then **Java: Clean Java Language Server Workspace** (Command Palette) or reload the window if the IDE still shows _Missing artifact_.

Open the **root** `pom.xml` in VS Code so the Java extension imports the full reactor (lib-common + services).

## Formatting (no npm)

This repo is **Java + Maven only** — there is no frontend and no root `package.json`.

- **Java** — format on save via **Extension Pack for Java** (Red Hat formatter); tabs per `.editorconfig`.
- **YAML** — optional **YAML** extension (`redhat.vscode-yaml`) for Compose and `application.yml`.
- **JSON / Markdown / HTML** — EditorConfig for indentation; use built-in formatters in the IDE if needed.
- **SQL (Liquibase / PostgreSQL)** — install **Prettier SQL VSCode** (`inferrinizzard.prettier-sql-vscode`). Workspace sets **`Prettier-SQL.dialect`: `postgresql`** (required for `$$ … BEGIN` PL/pgSQL). Default `sql` dialect causes `Parse error: Unexpected "$$ BEGIN"`. Format: save file or **Format Document**.

Shared rules live in [`.editorconfig`](../.editorconfig) at the repo root.

## Environment

Copy templates and adjust values locally (never commit secrets):

```bash
cp env/.env.dev.example env/.env.dev
cp env/.env.prod.example env/.env.prod
```

All services, Docker Compose, and VS Code debug configs read from `env/.env.dev` or `env/.env.prod`.

After changing env values, restart running services or debug sessions so Spring picks up the new variables.

### DBeaver (one-click dev DB connections)

The repo includes a DBeaver **project** under [`.dbeaver/`](../.dbeaver/) (six Postgres connections in folder **SBP Dev**). Credentials are synced from `env/.env.dev`:

```powershell
.\scripts\run.ps1 dbeaver-sync
```

Run this after copying or changing `env/.env.dev` (especially `*_DB_PASSWORD`). Generated `.dbeaver/data-sources.json` and `credentials-config.json` are gitignored (passwords live in `auth-properties.userName` / `userPassword` until DBeaver encrypts them on first connect).

**First-time setup in DBeaver:**

1. **Database → Driver Manager** — ensure **PostgreSQL** is installed (default in DBeaver CE).
2. **Project → Manage Projects → Create from existing directory** — choose this repository root (`sbp-miroservices`).
3. Set **Active Project** to **sbp-miroservices** (Project menu).
4. In **Database Navigator**, expand **SBP Dev** → double-click a connection (e.g. **SBP Auth (dev)**). Start dev DBs with `.\scripts\run.ps1 infra-dev` first.

If DBeaver asks to re-save credentials, run `dbeaver-sync` again and reconnect.

If you see **SCRAM-based auth but no password was provided**: run `dbeaver-sync` (fixes UTF-8 BOM and stale `credentials-config.json`), then **Project → Refresh** or restart DBeaver. Connect only under project **sbp-miroservices** (not duplicate entries under **General**). Remove old `credentials-config.json` in the workspace copy of the project if you previously connected with `save-password` enabled.

### Database passwords

Postgres containers read `*_DB_PASSWORD` from `env/.env.dev` only when a **new** data volume is created. `down-dev` / `docker compose down` **does not** remove volumes, so changing passwords in `.env.dev` leaves old credentials inside existing volumes and migrations fail with `password authentication failed`.

After changing any `*_DB_PASSWORD`, recreate volumes:

```powershell
.\scripts\run.ps1 reset-dev
```

Or: `.\scripts\run.ps1 down-dev-volumes` then `.\scripts\run.ps1 infra-dev`. This **deletes all local dev DB data**.

## Docker stack names

Compose project names group containers, networks, and default labels in Docker Desktop:

| Profile | Stack name   | Compose file                      |
| ------- | ------------ | --------------------------------- |
| Dev     | **sbp-dev**  | `docker/docker-compose.dev.yaml`  |
| Prod    | **sbp-prod** | `docker/docker-compose.prod.yaml` |

Examples: `docker compose -p sbp-dev ps`, or use the Make targets below.

## Stack commands (Make or PowerShell)

On **Windows**, if **`make`** fails with *Application Control policy has blocked this file*, use:

```powershell
.\scripts\run.ps1 help
.\scripts\run.ps1 infra-dev
.\scripts\run.ps1 migrate-dev
.\scripts\run.ps1 start-dev
```

With Git Bash / WSL and an allowed `make.exe`, you can still run **`make help`**.

| Make | PowerShell |
| ---- | ---------- |
| `make infra-dev` | `.\scripts\run.ps1 infra-dev` |
| `make migrate-dev` | `.\scripts\run.ps1 migrate-dev` |
| `make start-dev` | `.\scripts\run.ps1 start-dev` |
| `make down-dev` | `.\scripts\run.ps1 down-dev` |
| `make down-dev-volumes` | `.\scripts\run.ps1 down-dev-volumes` |
| `make reset-dev` | `.\scripts\run.ps1 reset-dev` |
| `make install-lib` | `.\scripts\run.ps1 install-lib` |
| — | `.\scripts\run.ps1 start auth-service` (local JVM, `env/.env.dev`) |
| — | `.\scripts\run.ps1 start auth-service docker` |
| — | `.\scripts\run.ps1 stop auth-service` |
| — | `.\scripts\run.ps1 svc-info` |

VS Code: **Run Task → SBP: infra-dev (PowerShell)**.

## Local run (hot reload)

1. `make infra-dev` (Postgres, Prometheus, Grafana)
2. VS Code → **Discovery (dev)** (Eureka on `:8761`)
3. VS Code → **Microservices (dev)** (Gateway, Auth, Users, Admin)
4. For person/studio/content/reference APIs also run **Domain services (dev)** or start **Reference (dev)**, **Studio (dev)**, **Person (dev)** individually.

**Spring Boot Dashboard:** see **[IDE-SPRING-BOOT-DASHBOARD.md](IDE-SPRING-BOOT-DASHBOARD.md)** if **sbp-reference-service** (or any module) is missing from the dashboard. Quick check: **Java Projects** must list `sbp-reference-service` before the dashboard can show it.

Spring Boot DevTools restarts the app when classpath changes. Keep **Java › Autobuild: Enabled** (see root `.vscode/settings.json`).

Build and test from the IDE (Extension Pack for Java embeds Maven) or via Docker (`make start-dev`). Per-module Maven Wrapper scripts were removed; Docker builds use the Maven image directly.

## API paths

Full endpoint inventory (methods, filters, internal APIs): **[API-ENDPOINTS.md](API-ENDPOINTS.md)**.

| Service            | Public base path              |
| ------------------ | ----------------------------- |
| API Gateway        | `/api/v1/gateway/**`          |
| Auth               | `/api/v1/auth/**`             |
| Users              | `/api/v1/users/**`            |
| Person             | `/api/v1/persons/**`          |
| Studio             | `/api/v1/studios/**`          |
| Content            | `/api/v1/content/**`          |
| Reference          | `/api/v1/taxonomies/**`, `/api/v1/social-platforms/**` |
| Admin (monitoring) | `/api/v1/admin/monitoring/**` |

Health checks (examples — same path via gateway `:8080` or direct service port):

- `GET http://localhost:8080/api/v1/gateway/health`
- `GET http://localhost:8080/api/v1/auth/health`
- `GET http://localhost:8080/api/v1/users/health`
- `GET http://localhost:8080/api/v1/persons/health`
- `GET http://localhost:8080/api/v1/studios/health`
- `GET http://localhost:8080/api/v1/content/health`
- `GET http://localhost:8080/api/v1/admin/monitoring/overview`
- Reference health/ready (direct `:8087` only): `GET http://localhost:8087/health`, `GET http://localhost:8087/ready`

Internal service-to-service routes: see [API-ENDPOINTS.md](API-ENDPOINTS.md#internal-apis-service-to-service).

Configuration keys are documented in each service's `META-INF/additional-spring-configuration-metadata.json`.
