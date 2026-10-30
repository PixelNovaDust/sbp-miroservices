# SBP Microservices

Spring Boot **4.1.1** microservices with Eureka service discovery, API Gateway routing, and a dedicated **Admin monitoring** service (Prometheus + Grafana).

## Quick start (Docker)

Stack names: **sbp-dev** (development), **sbp-prod** (production).

```powershell
copy env\.env.dev.example env\.env.dev
.\scripts\run.ps1 start-dev
```

(On Git Bash / WSL you can use `make start-dev` instead. If Windows blocks `make.exe`, use `run.ps1` only.)

- Gateway: http://localhost:8080/api/v1/gateway/health
- Auth: http://localhost:8080/api/v1/auth/health
- Monitoring: http://localhost:8080/api/v1/admin/monitoring/overview
- Eureka: http://localhost:8761
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (set `GRAFANA_ADMIN_PASSWORD` in env)

## Quick start (VS Code)

1. `make infra-dev` — Docker infra (DBs, Prometheus, Grafana)
2. **Discovery (dev)** — Eureka from the IDE (`:8761`)
3. **Microservices (dev)** — Gateway, Auth, Users, Admin from the IDE

## Modules

| Module                 | Description                          |
| ---------------------- | ------------------------------------ |
| `sbp-discovery-server` | Eureka registry                      |
| `sbp-api-gateway`      | Spring Cloud Gateway + Eureka client |
| `sbp-auth-services`    | Auth API + own DB                    |
| `sbp-user-services`    | User API + own DB                    |
| `sbp-admin-service`    | Dynamic monitoring APIs              |

No shared parent POM; each module builds independently.

Shared library: [`sbp-lib-common`](docs/LIB-COMMON.md) — run **`make install-lib`** once so the IDE resolves `com.sbp:sbp-lib-common:1.0.0`.

See [docs/API-ENDPOINTS.md](docs/API-ENDPOINTS.md), [docs/LIB-COMMON.md](docs/LIB-COMMON.md), [docs/SERVICE-PACKAGE-LAYOUT.md](docs/SERVICE-PACKAGE-LAYOUT.md), [docs/MONITORING.md](docs/MONITORING.md), and [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).
