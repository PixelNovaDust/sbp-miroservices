# Development

## Prerequisites

- **JDK 21** (LTS; matches Docker images and `java.version` in POMs). Java 25+ on your PATH often triggers Maven/Jansi warnings—use 21 for local runs.
- **No global Maven needed** — use repo **`mvnw`** / **`mvnw.cmd`** or VS Code task **Maven: install sbp-lib-common**

### macOS JDK 21 (recommended)

```bash
brew install temurin@21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
java -version   # should show 21.x
```

Add `JAVA_HOME` to your shell profile, or rely on `.java-version` if you use **jenv** / **asdf**.

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

### Lombok

Spring services use **Lombok** (optional dependency + annotation processor in each service POM). Conventions:

- **JPA entities:** `@Getter`, `@Setter`, `@NoArgsConstructor` (avoid `@Data` on entities).
- **Embeddable IDs:** add `@AllArgsConstructor` / `@EqualsAndHashCode` where needed.
- **`@ConfigurationProperties`:** `@Getter` / `@Setter` on the type and nested static classes.
- **Spring beans:** `@RequiredArgsConstructor` for constructor injection when the constructor only assigns `final` fields.
- **Logging:** `@Slf4j` instead of manual `LoggerFactory` (except multi-logger helpers like `SbpLoggerImpl`).

Keep hand-written constructors when they build clients (`RestClient`, `JdbcTemplate`, etc.) or normalize config.

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

For real S3/R2/MinIO uploads, fill the **object storage** block and **`AWS_*`** keys in `.env.dev` (templates and commented R2/MinIO examples in `env/.env.dev.example`). Only **`sbp-file-storage-service`** needs those variables.

After changing env values, restart running services or debug sessions so Spring picks up the new variables.

### DBeaver (one-click dev DB connections)

The repo includes a DBeaver **project** under [`.dbeaver/`](../.dbeaver/) (seven Postgres connections in folder **SBP Dev**, including file storage). Credentials are synced from `env/.env.dev`:

```powershell
.\scripts\run.ps1 dbeaver
```

Run this after copying or changing `env/.env.dev` (especially `*_DB_PASSWORD`). Generated `.dbeaver/data-sources.json` and `credentials-config.json` are gitignored (passwords live in `auth-properties.userName` / `userPassword` until DBeaver encrypts them on first connect).

**First-time setup in DBeaver:**

1. **Database → Driver Manager** — ensure **PostgreSQL** is installed (default in DBeaver CE).
2. **Project → Manage Projects → Create from existing directory** — choose this repository root (`sbp-miroservices`).
3. Set **Active Project** to **sbp-miroservices** (Project menu).
4. In **Database Navigator**, expand **SBP Dev** → double-click a connection (e.g. **SBP Auth (dev)**). Start dev DBs with `.\scripts\run.ps1 up dev -Infra` first.

If DBeaver asks to re-save credentials, run `dbeaver` again and reconnect.

If you see **SCRAM-based auth but no password was provided**: run `dbeaver` (fixes UTF-8 BOM and stale `credentials-config.json`), then **Project → Refresh** or restart DBeaver. Connect only under project **sbp-miroservices** (not duplicate entries under **General**). Remove old `credentials-config.json` in the workspace copy of the project if you previously connected with `save-password` enabled.

### Database passwords

Postgres containers read `*_DB_PASSWORD` from `env/.env.dev` only when a **new** data volume is created. `down dev` / `docker compose down` **does not** remove volumes, so changing passwords in `.env.dev` leaves old credentials inside existing volumes and migrations fail with `password authentication failed`.

After changing any `*_DB_PASSWORD`, recreate volumes:

```powershell
.\scripts\run.ps1 reset dev
```

Or: `.\scripts\run.ps1 down dev -Volumes` then `.\scripts\run.ps1 up dev -Infra`. This **deletes all local dev DB data**.

## Docker stack names

Compose project names group containers, networks, and default labels in Docker Desktop:

| Profile | Stack name   | Compose file                      |
| ------- | ------------ | --------------------------------- |
| Dev     | **sbp-dev**  | `docker/docker-compose.dev.yaml`  |
| Prod    | **sbp-prod** | `docker/docker-compose.prod.yaml` |

Examples: `docker compose -p sbp-dev ps`, or use the Make targets below.

## Stack commands (Make or PowerShell)

On **Windows**, if **`make`** fails with _Application Control policy has blocked this file_, use:

```powershell
.\scripts\run.ps1 help
.\scripts\run.ps1 up dev -Infra    # DBs + monitoring + migrate
.\scripts\run.ps1 migrate dev      # migrations only
.\scripts\run.ps1 up dev           # full stack
```

macOS/Linux: same verbs with `./scripts/run.sh` (flags use `--infra`, `--volumes`, `--repair`).

With Git Bash / WSL and an allowed `make.exe`, you can still run **`make help`**.

| Make                    | `run.sh` / `run.ps1`                                 |
| ----------------------- | ---------------------------------------------------- |
| `make infra-dev`        | `up dev --infra` / `up dev -Infra`                   |
| `make migrate-dev`      | `migrate dev`                                        |
| `make start-dev`        | `up dev`                                             |
| `make down-dev`         | `down dev`                                           |
| `make down-dev-volumes` | `down dev --volumes` / `-Volumes`                    |
| `make reset-dev`        | `reset dev`                                          |
| `make install-lib`      | `lib`                                                |
| —                       | `svc start auth-service` (local JVM, `env/.env.dev`) |
| —                       | `svc start auth-service -Docker`                     |
| —                       | `svc stop auth-service`                              |
| —                       | `svc status`                                         |
| —                       | `seed dev`                                           |

VS Code: **Run Task → SBP: up dev -Infra (PowerShell)**.

### Dev catalog seed (reference, studio, person)

After **Liquibase** (`migrate dev`) and with **discovery, gateway, reference, studio, person, and content** running:

```bash
./scripts/run.sh migrate dev    # if not already applied
./scripts/run.sh up dev           # or svc start gateway, reference, studio, person locally
./scripts/run.sh seed dev
```

Re-seed from scratch (removes prior `dev-seed` rows): `./scripts/run.sh seed dev --reset`.  
Truncate the whole person DB then re-seed: `./scripts/run.sh seed dev --reset-db`.  
Re-apply links for existing persons: `./scripts/run.sh seed dev --force`.

Data: adult-industry themed studios/taxonomies; performers **Natasha Nice**, **Britney Amber**, **Koda Monroe**, **Kendra Lust** (all via `seed dev` — no separate persons seed).  
Script: `scripts/seed/seed-dev-data.py` (HTTP APIs for reference/studio/person/content + SQL for `*_baseline`, `person_featured_content`, `last_synced_at`). Person **specialties** use `SPECIALTIES` taxonomies; **career** includes `previousProfession` and `startYear` (person DB migration **V005**).

**Scripts layout:** only `scripts/run.sh` and `scripts/run.ps1` at the repo’s `scripts/` root; seeds live under `scripts/seed/`; shared helpers under `scripts/lib/`. Run `./scripts/run.sh help` for the full command table.

**Database schemas:** Liquibase in `sbp-migration`; per-DB notes in **[db/](db/)** (see [MIGRATION.md](MIGRATION.md)).

## Local run (hot reload)

1. `make infra-dev` or `./scripts/run.sh up dev --infra` (Postgres, Prometheus, Grafana)
2. VS Code → **Discovery (dev)** (Eureka on `:8761`)
3. VS Code → **Microservices (dev)** (Gateway, Auth, Users, Admin)
4. For person/studio/content/reference APIs also run **Domain services (dev)** or start **Reference (dev)**, **Studio (dev)**, **Person (dev)** individually.

**Spring Boot Dashboard:** see **[IDE-SPRING-BOOT-DASHBOARD.md](IDE-SPRING-BOOT-DASHBOARD.md)** if **sbp-reference-service** (or any module) is missing from the dashboard. Quick check: **Java Projects** must list `sbp-reference-service` before the dashboard can show it.

Spring Boot DevTools restarts the app when classpath changes. Keep **Java › Autobuild: Enabled** (see root `.vscode/settings.json`).

Build and test from the IDE (Extension Pack for Java embeds Maven) or via Docker (`make start-dev`). Per-module Maven Wrapper scripts were removed; Docker builds use the Maven image directly.

## API paths

Full endpoint inventory (methods, filters, internal APIs): **[API-ENDPOINTS.md](API-ENDPOINTS.md)**.

| Service            | Public base path                                       |
| ------------------ | ------------------------------------------------------ |
| API Gateway        | `/api/v1/gateway/**`                                   |
| Auth               | `/api/v1/auth/**`                                      |
| Users              | `/api/v1/users/**`                                     |
| Person             | `/api/v1/persons/**`                                   |
| Studio             | `/api/v1/studios/**`                                   |
| Content            | `/api/v1/content/**`                                   |
| Reference          | `/api/v1/taxonomies/**`, `/api/v1/social-platforms/**` |
| Admin (monitoring) | `/api/v1/admin/monitoring/**`                          |

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
