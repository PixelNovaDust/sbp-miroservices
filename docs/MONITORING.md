# Monitoring architecture

## Components

| Service                | Port (app) | Port (management) | Public API prefix    |
| ---------------------- | ---------- | ----------------- | -------------------- |
| `sbp-discovery-server` | 8761       | —                 | (not via gateway)    |
| `sbp-api-gateway`      | 8080       | 9080              | `/api/v1/gateway/**` |
| `sbp-auth-service`     | 8081       | 9081              | `/api/v1/auth/**`    |
| `sbp-user-service`     | 8082       | 9082              | `/api/v1/users/**`   |
| `sbp-admin-service`    | 8083       | 9083              | `/api/v1/admin/**`   |
| Prometheus             | 9090       | —                 | —                    |
| Grafana                | 3000       | —                 | —                    |

Spring Boot **4.1.1** with Spring Cloud **2025.1.3** (Oakwood).

## URL convention

Every microservice exposes public HTTP APIs under:

`/api/v1/{service-name}/**`

Gateway routes use the **same paths** (`lb://` to Eureka). No path rewrites.

| Service name segment | Example                             |
| -------------------- | ----------------------------------- |
| `gateway`            | `/api/v1/gateway/health`            |
| `auth`               | `/api/v1/auth/health`               |
| `users`              | `/api/v1/users/health`              |
| `admin`              | `/api/v1/admin/monitoring/overview` |

## Dynamic discovery

Services register with Eureka using `spring.application.name`. The Admin Service uses `DiscoveryClient` — **no hardcoded service list**.

## Status model

| Status           | Meaning                                                |
| ---------------- | ------------------------------------------------------ |
| `UP`             | All instance health checks pass                        |
| `DEGRADED`       | Mixed instance health                                  |
| `DOWN`           | All instances down or required component unreachable   |
| `UNKNOWN`        | Health not established or registry/metrics unavailable |
| `NOT_REGISTERED` | No Eureka registration                                 |

## Monitoring endpoints (Admin)

Base: **`/api/v1/admin/monitoring`** (direct on `:8083` or via gateway `:8080` with the same path).

| Method | Path                                                     |
| ------ | -------------------------------------------------------- |
| GET    | `/api/v1/admin/monitoring/overview`                      |
| GET    | `/api/v1/admin/monitoring/services`                      |
| GET    | `/api/v1/admin/monitoring/services/{serviceName}`        |
| GET    | `/api/v1/admin/monitoring/services/{serviceName}/health` |
| GET    | `/api/v1/admin/monitoring/instances`                     |
| GET    | `/api/v1/admin/monitoring/instances/{instanceId}`        |
| GET    | `/api/v1/admin/monitoring/infrastructure`                |
| GET    | `/api/v1/admin/monitoring/infrastructure/{name}/health`  |
| GET    | `/api/v1/admin/monitoring/metrics`                       |
| GET    | `/api/v1/admin/monitoring/events`                        |

## Actuator (management ports)

Each app service exposes on its **management port** only:

- `/actuator/health` (readiness/liveness probes enabled)
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

## Prometheus limitation

Scrape targets are in `docker/prometheus/*.yml` (static Docker DNS + management ports). Adding a service requires a new scrape job unless you add Eureka SD later.

## Local VS Code order

1. `make infra-dev` (Postgres, Prometheus, Grafana)
2. **Discovery (dev)** (Eureka — not part of `infra-dev`)
3. **Microservices (dev)** (Gateway, Auth, Users, Admin)
