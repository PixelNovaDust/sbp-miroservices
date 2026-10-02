# Development

## Prerequisites

- JDK 17+
- Docker Desktop
- GNU Make (Git Bash / WSL on Windows)
- VS Code with Extension Pack for Java and Spring Boot Extension Pack

## Environment

Copy templates and adjust values locally (never commit secrets):

```bash
cp env/.env.dev.example env/.env.dev
cp env/.env.prod.example env/.env.prod
```

All services, Docker Compose, and VS Code debug configs read from `env/.env.dev` or `env/.env.prod`.

After changing env values, restart running services or debug sessions so Spring picks up the new variables.

## Make targets

| Command | Description |
|---------|-------------|
| `make db-dev` | Start Postgres (auth + user) with dev env |
| `make start-dev` | Build and start full dev stack |
| `make db-prod` | Start Postgres with prod env |
| `make start-prod` | Build and start full prod stack |
| `make down-dev` | Stop dev stack |
| `make down-prod` | Stop prod stack |

## Local run (hot reload)

1. `make db-dev`
2. VS Code → Run and Debug → **All services (dev)** (or run a single service)

Spring Boot DevTools restarts the app when classpath changes. Keep **Java › Autobuild: Enabled** (see root `.vscode/settings.json`).

Build and test from the IDE (Extension Pack for Java embeds Maven) or via Docker (`make start-dev`). Per-module Maven Wrapper scripts were removed; Docker builds use the Maven image directly.

## API paths

| Service | Public base path |
|---------|------------------|
| API Gateway | `/api/v1` |
| Auth | `/api/auth` |
| Users | `/api/v1/users` |

Health checks (examples):

- `GET http://localhost:8080/api/v1/health`
- `GET http://localhost:8081/api/auth/health`
- `GET http://localhost:8082/api/v1/users/health`

Internal service-to-service routes (planned): `/internal/{gateway|auth|users}/...`

Configuration keys are documented in each service's `META-INF/additional-spring-configuration-metadata.json`.
