# Service package layout

Every Spring Boot microservice under `com.sbp.<service>` follows the same **layer packages**. Shared code lives in **`sbp-lib-common`** under **`com.sbp.common.<module>`** (e.g. `response`, `logging`) and is not duplicated per service.

## Standard layers

| Package      | Responsibility                                             |
| ------------ | ---------------------------------------------------------- |
| `config`     | Spring configuration, OpenAPI setup, beans (non-security)  |
| `controller` | REST endpoints; HTTP requests and responses                |
| `service`    | Business logic; service interfaces and implementations     |
| `repository` | Database access (Spring Data JPA or other persistence)     |
| `entity`     | Persistence models mapped to the database                  |
| `dto`        | API request/response objects (not exposed entities)        |
| `mapper`     | Entity ↔ DTO conversion                                    |
| `exception`  | Domain exceptions and `@RestControllerAdvice` handlers     |
| `security`   | Authentication, authorization, JWT, security filter chains |
| `util`       | Small, stateless helpers with no business rules            |

The main application class stays in the root package (`com.sbp.auth`, `com.sbp.user`, …).

## Rules

1. **Controllers stay thin** — delegate to `service`; return DTOs or shared envelopes (`ApiResponse` from `sbp-lib-common`).
2. **No entities in JSON** — map through `dto` and `mapper`.
3. **Security only in `security`** — do not put `SecurityFilterChain` beans in `config`.
4. **Skip empty layers until needed** — you do not need every folder on day one; add a package when the first type appears there.
5. **Infrastructure-only services** (gateway, discovery) typically use `config`, `controller`, `security`, and sometimes `service` / `dto` — omit `entity`, `repository`, and `mapper` unless the service gains persistence.

## Layout by module

```
com.sbp.auth/
├── SbpAuthServicesApplication.java
├── config/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
├── exception/
├── security/
└── util/

com.sbp.user/          # same layers as auth

com.sbp.admin/
├── config/            # MonitoringProperties, RestClient, cache
├── controller/        # MonitoringController, future admin APIs
├── service/           # MonitoringService, probes, Prometheus, etc.
├── dto/               # MonitoringDtos, MonitoringStatus
├── exception/
├── security/
└── util/              # TargetHostValidator

com.sbp.gateway/
├── config/
├── controller/
├── security/

com.sbp.discovery/
├── config/
├── security/
```

## New service checklist

1. Create `sbp-<name>-service` with base package `com.sbp.<name>`.
2. Add the layer folders you need from the table above.
3. Depend on `sbp-lib-common` for API envelopes, logging, and other shared utilities.
4. Register routes on the gateway under `/api/v1/<name>/**` and set `server.servlet.context-path` (or WebFlux equivalent) on the service.
5. Document public endpoints in `docs/DEVELOPMENT.md` or a service-specific doc.

See also [PROJECT-STRUCTURE.md](PROJECT-STRUCTURE.md) and [LIB-COMMON.md](LIB-COMMON.md).
