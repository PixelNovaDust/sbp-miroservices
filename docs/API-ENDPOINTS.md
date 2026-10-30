# API endpoints

All public business APIs are intended to be called through the **API Gateway** at `http://localhost:8080` (dev). Responses use the shared **`ApiResponse`** envelope from `sbp-lib-common` unless noted.

## Gateway routes

| Gateway prefix                | Service (Eureka)        |
| ----------------------------- | ----------------------- |
| `/api/v1/auth/**`             | `sbp-auth-service`      |
| `/api/v1/users/**`            | `sbp-user-service`      |
| `/api/v1/persons/**`          | `sbp-person-service`    |
| `/api/v1/studios/**`          | `sbp-studio-service`    |
| `/api/v1/content/**`          | `sbp-content-service`   |
| `/api/v1/taxonomies/**`       | `sbp-reference-service` |
| `/api/v1/social-platforms/**` | `sbp-reference-service` |
| `/api/v1/admin/**`            | `sbp-admin-service`     |

**Not routed on the gateway:** reference internal APIs (`/internal/v1/...`) and studio internal GET (`/api/v1/studios/internal/{studioId}`). Use service-to-service HTTP (see [Internal APIs](#internal-apis-service-to-service)).

## Default dev ports (direct)

| Service   | App port | Management | Context path                                          |
| --------- | -------- | ---------- | ----------------------------------------------------- |
| Gateway   | 8080     | 9080       | —                                                     |
| Auth      | 8081     | 9081       | `/api/v1/auth`                                        |
| User      | 8082     | 9082       | `/api/v1/users`                                       |
| Admin     | 8083     | 9083       | `/api/v1/admin`                                       |
| Person    | 8084     | 9084       | `/api/v1/persons`                                     |
| Studio    | 8085     | 9085       | `/api/v1/studios`                                     |
| Content   | 8086     | 9086       | `/api/v1/content`                                     |
| Reference | 8087     | 9087       | _(none — paths include `/api/v1/...` on controllers)_ |

Env vars: `env/.env.dev.example`.

---

## API Gateway

| Method | Path                     | Description                       |
| ------ | ------------------------ | --------------------------------- |
| GET    | `/api/v1/gateway/health` | Gateway health (local controller) |

---

## Auth (`sbp-auth-service`)

| Method | Path                             | Description                  |
| ------ | -------------------------------- | ---------------------------- |
| GET    | `/api/v1/auth/health`            | Health                       |
| POST   | `/api/v1/auth/invitations/check` | Check invitation eligibility |

---

## User (`sbp-user-service`)

| Method | Path                   | Description |
| ------ | ---------------------- | ----------- |
| GET    | `/api/v1/users/health` | Health      |

_Domain user CRUD is not implemented yet._

---

## Person (`sbp-person-service`)

Base path: **`/api/v1/persons`**.

### Person

| Method | Path                  | Description                                            |
| ------ | --------------------- | ------------------------------------------------------ |
| GET    | `/health`             | Health                                                 |
| GET    | `/ready`              | Readiness (database check)                             |
| GET    | `/`                   | List persons                                           |
| GET    | `/{personId}`         | Get person **full profile** (aggregated JSON document) |
| POST   | `/`                   | Create person                                          |
| PUT    | `/{personId}`         | Replace person profile                                 |
| PATCH  | `/{personId}`         | Partial update                                         |
| DELETE | `/{personId}`         | Soft deactivate                                        |
| POST   | `/{personId}/restore` | Restore after soft delete                              |

**List filters (query params):**

| Param      | Example          |
| ---------- | ---------------- |
| `name`     | `?name=Natasha`  |
| `active`   | `?active=true`   |
| `verified` | `?verified=true` |

`GET /{personId}` enriches data from **reference** (taxonomies including **ROLE** → profile `roles`, plus categories/tags/specialties, social platform slugs) and **studio** (studio names for `career.knownFor`) via internal HTTP — not cross-database SQL.

**Create/update body (career & roles):**

- **`roles`** — assign via `POST /{personId}/taxonomies` using taxonomies with `type = ROLE` (not a JSON field on the person row).
- **`career`** — `{ "details": "...", "highlights": ["..."] }`; `knownFor` is **read-only** on GET (from studio links). `featuredContent` lists `{ contentId, title?, slug? }` from `person_featured_content` (title/slug reserved for future content-service enrichment). Timeline: `debutYear`, `careerStatus` (`ACTIVE` | `RETIRED` | `HIATUS` | `DEBUT`), optional `retiredYear`, `lastActiveYear`.
- Profile **`stats`** includes counts (studios, social accounts, taxonomy assignments, featured content links).

### Aliases

| Method | Path                            | Description  |
| ------ | ------------------------------- | ------------ |
| GET    | `/{personId}/aliases`           | List aliases |
| POST   | `/{personId}/aliases`           | Add alias    |
| DELETE | `/{personId}/aliases/{aliasId}` | Remove alias |

### Lookups (person DB)

| Method | Path                                 | Description                           |
| ------ | ------------------------------------ | ------------------------------------- |
| GET    | `/body-types`                        | List body types (`?active=true`)      |
| GET    | `/body-types/by-slug/{slug}`         | Get body type by slug                 |
| POST   | `/body-types`                        | Create body type (idempotent by slug) |
| GET    | `/relationship-types`                | List relationship types               |
| GET    | `/relationship-types/by-slug/{slug}` | Get relationship type by slug         |
| POST   | `/relationship-types`                | Create relationship type              |

### Physical attributes

| Method | Path                              | Description                                         |
| ------ | --------------------------------- | --------------------------------------------------- |
| GET    | `/{personId}/physical-attributes` | Get attributes (`bodyTypeId`, `bodyTypeName`)       |
| PUT    | `/{personId}/physical-attributes` | Create or replace attributes (`bodyTypeId` in body) |

_PATCH for physical attributes is not implemented yet._

### Social accounts

| Method | Path                                      | Description    |
| ------ | ----------------------------------------- | -------------- |
| GET    | `/{personId}/social-accounts`             | List accounts  |
| POST   | `/{personId}/social-accounts`             | Add account    |
| PUT    | `/{personId}/social-accounts/{accountId}` | Update account |
| DELETE | `/{personId}/social-accounts/{accountId}` | Remove account |

### Studio relationships

| Method | Path                           | Description                                    |
| ------ | ------------------------------ | ---------------------------------------------- |
| GET    | `/{personId}/studios`          | List person–studio links                       |
| POST   | `/{personId}/studios`          | Link studio                                    |
| DELETE | `/{personId}/studios/{linkId}` | Remove link (uses **link** id, not `studioId`) |

_PATCH on a person–studio relationship is not implemented yet._

### Taxonomies (proxy to reference)

Person service does **not** create taxonomy definitions; it assigns **existing** taxonomy ids to the person via the reference DB.

| Method | Path                                       | Description       |
| ------ | ------------------------------------------ | ----------------- |
| GET    | `/{personId}/taxonomies`                   | List assignments  |
| POST   | `/{personId}/taxonomies`                   | Assign taxonomy   |
| DELETE | `/{personId}/taxonomies?taxonomyId={uuid}` | Remove assignment |

---

## Studio (`sbp-studio-service`)

Base path: **`/api/v1/studios`**.

| Method | Path              | Description                 |
| ------ | ----------------- | --------------------------- |
| GET    | `/health`         | Health                      |
| GET    | `/ready`          | Readiness (database check)  |
| GET    | `/`               | List studios                |
| GET    | `/by-name/{name}` | Get by name                 |
| GET    | `/{studioId}`     | Get by id                   |
| POST   | `/`               | Create studio               |
| PUT    | `/{studioId}`     | Full update                 |
| PATCH  | `/{studioId}`     | Partial update              |
| DELETE | `/{studioId}`     | Deactivate (`active=false`) |

**List filters:**

| Param    | Example          |
| -------- | ---------------- |
| `name`   | `?name=Brazzers` |
| `active` | `?active=true`   |

---

## Reference (`sbp-reference-service`)

Owns **taxonomies**, **social platforms**, and **entity_taxonomies** (links). Port **8087**; controllers use full paths `/api/v1/taxonomies` and `/api/v1/social-platforms` (no servlet context-path).

### Operational (service root)

| Method | Path      | Description                       |
| ------ | --------- | --------------------------------- |
| GET    | `/health` | Health _(direct `:8087` only)_    |
| GET    | `/ready`  | Readiness _(direct `:8087` only)_ |

These are **not** under `/api/v1/taxonomies/**` and are **not** exposed on the gateway.

### Taxonomies (public, gateway)

| Method | Path                                | Description                 |
| ------ | ----------------------------------- | --------------------------- |
| GET    | `/api/v1/taxonomies`                | List taxonomies             |
| GET    | `/api/v1/taxonomies/types`          | Supported taxonomy types    |
| GET    | `/api/v1/taxonomies/by-slug/{slug}` | Get by slug                 |
| GET    | `/api/v1/taxonomies/{taxonomyId}`   | Get by id                   |
| POST   | `/api/v1/taxonomies`                | Create                      |
| PUT    | `/api/v1/taxonomies/{taxonomyId}`   | Update                      |
| DELETE | `/api/v1/taxonomies/{taxonomyId}`   | Deactivate (`active=false`) |

**List filters:**

| Param    | Example          |
| -------- | ---------------- |
| `type`   | `?type=CATEGORY` |
| `name`   | `?name=MILF`     |
| `active` | `?active=true`   |

Taxonomy `type` values include **`CATEGORY`**, **`TAG`**, **`SPECIALTIES`**, **`MOOD_TAG`**, **`GENRE`**, **`ROLE`** (person profile maps these to `categories`, `tags`, `specialties`, and **`roles`**).

### Social platforms (public, gateway)

| Method | Path                                      | Description    |
| ------ | ----------------------------------------- | -------------- |
| GET    | `/api/v1/social-platforms`                | List platforms |
| GET    | `/api/v1/social-platforms/by-slug/{slug}` | Get by slug    |
| GET    | `/api/v1/social-platforms/{platformId}`   | Get by id      |
| POST   | `/api/v1/social-platforms`                | Create         |
| PUT    | `/api/v1/social-platforms/{platformId}`   | Update         |
| DELETE | `/api/v1/social-platforms/{platformId}`   | Deactivate     |

**List filters:** `?name=`, `?active=true`

---

## Internal APIs (service-to-service)

Use from other microservices (e.g. person → reference/studio). Configure base URLs via `REFERENCE_SERVICE_URL`, `STUDIO_SERVICE_URL` in `env/.env.dev`.

### Reference

| Method | Path                                                               | Description                                          |
| ------ | ------------------------------------------------------------------ | ---------------------------------------------------- |
| GET    | `/internal/v1/entity-taxonomies?entityId={uuid}&entityType=PERSON` | List entity–taxonomy links                           |
| POST   | `/internal/v1/entity-taxonomies`                                   | Assign link (body: entityId, entityType, taxonomyId) |
| DELETE | `/internal/v1/entity-taxonomies?entityId=&entityType=&taxonomyId=` | Remove link                                          |
| GET    | `/internal/v1/taxonomies/batch?ids={uuid},{uuid}`                  | Batch load taxonomies                                |

### Studio

| Method | Path                                  | Description                            |
| ------ | ------------------------------------- | -------------------------------------- |
| GET    | `/api/v1/studios/internal/{studioId}` | Minimal studio payload for aggregation |

---

## Content (`sbp-content-service`)

Base path: **`/api/v1/content`**. Responses use **`ApiResponse`**. Detail payloads match the catalog shape documented in [CONTENT-DATABASE.md](CONTENT-DATABASE.md).

| Method | Path                     | Description                                                                |
| ------ | ------------------------ | -------------------------------------------------------------------------- |
| GET    | `/api/v1/content/health` | Health                                                                     |
| GET    | `/api/v1/content`        | List (optional `title`, `contentType`, `status`, `visibility`)             |
| GET    | `/api/v1/content/{id}`   | Full detail (studios, cast, taxonomies, assets enriched via internal HTTP) |
| POST   | `/api/v1/content`        | Create catalog entry + graph                                               |
| PUT    | `/api/v1/content/{id}`   | Replace scalars and entire link/asset graph                                |
| DELETE | `/api/v1/content/{id}`   | Hard delete (cascade link tables)                                          |

**Write body (`ContentWriteRequest`):** scalars (`title`, `tagline`, `overview`, `adult`, `contentType`, `contentRating`, optional `status` / `visibility`, `originalLanguage`, `originCountry[]`, `releaseDate`, `runtime`, paths), plus ID lists: `studioIds`, `castPersonIds`, `tagIds`, `genreIds`, `moodTagIds`, and `assets[]` with `type` (`CLIP` \| `TRAILER` \| `BTS` \| `TEASER`) and `keys[]` `{ "value": "<storage path>" }`.

**Read-only enrichment on GET:** `studios`, `casts`, `tags`, `genre`, `moodTags` include `{ id, name }` from studio, person, and reference services. **`stats`** defaults to `{ "averageRating": 0, "totalLikes": 0, "totalViews": 0 }` (not stored yet).

**Service clients (content JVM):** `REFERENCE_SERVICE_URL`, `STUDIO_SERVICE_URL`, `PERSON_SERVICE_URL` (see `env/.env.dev.example`).

---

## Admin (`sbp-admin-service`)

Base path: **`/api/v1/admin`**.

| Method | Path                                        | Description          |
| ------ | ------------------------------------------- | -------------------- |
| GET    | `/monitoring/overview`                      | Monitoring overview  |
| GET    | `/monitoring/services`                      | Registered services  |
| GET    | `/monitoring/services/{serviceName}`        | Service detail       |
| GET    | `/monitoring/services/{serviceName}/health` | Service health probe |
| GET    | `/monitoring/instances`                     | Eureka instances     |
| GET    | `/monitoring/instances/{instanceId}`        | Instance detail      |
| GET    | `/monitoring/infrastructure`                | DB/infra targets     |
| GET    | `/monitoring/infrastructure/{name}/health`  | Infra health         |
| GET    | `/monitoring/metrics`                       | Metrics snapshot     |
| GET    | `/monitoring/events`                        | Recent events        |

---

## Actuator and infrastructure

Each Spring service exposes **Spring Boot Actuator** on its **management port** (e.g. person `9084`), typically:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus` (where enabled)

**Eureka:** `http://localhost:8761`  
**Prometheus / Grafana:** see [MONITORING.md](MONITORING.md) and `docker/docker-compose.dev.yaml`.

---

## Related docs

- [DEVELOPMENT.md](DEVELOPMENT.md) — run stack, env, gateway stack name
- [sql/PERSON-PROFILE-QUERIES.md](sql/PERSON-PROFILE-QUERIES.md) — SQL for single-DB slices vs aggregated GET person
- [SERVICE-PACKAGE-LAYOUT.md](SERVICE-PACKAGE-LAYOUT.md) — controller/service layering
- [API-RESPONSE-LIBRARY.md](API-RESPONSE-LIBRARY.md) — `ApiResponse` envelope
