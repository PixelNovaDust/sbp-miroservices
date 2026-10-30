# Database migrations (Liquibase)

All schema changes live in **`sbp-migration`** — not inside individual microservices.

## Layout

```
sbp-migration/
├── src/main/resources/migration/
│   ├── auth/
│   │   db.changelog-master.yaml
│   │   └── changes/V001__initial_schema.sql
│   ├── user/
│   │   db.changelog-master.yaml
│   │   └── changes/V001__initial_schema.sql
│   ├── person/
│   ├── studio/
│   │   └── changes/V002__studios.sql
│   ├── content/
│   └── reference/
│       ├── changes/V001__taxonomies_and_social_platforms.sql
│       └── changes/V002__entity_taxonomies.sql
└── src/main/java/.../SbpMigrationRunner.java
```

Add new SQL (or YAML changesets) under the correct database folder. Never edit applied changesets; add `V002__...sql` and include it from the master changelog.

Column definitions use short end-of-line `--` comments in SQL files (stripped at runtime by Liquibase `stripComments`; for documentation only). If you must change an already-applied file, add a **new** changeset instead of editing SQL.

If dev DBs already ran an older version of a file and Liquibase reports **Validation Failed: checksum**, run:

```powershell
.\scripts\run.ps1 migrate-dev-repair
```

That sets `LIQUIBASE_CLEAR_CHECKSUMS=true` for one migration run (all databases). Do not use in production.

**Auth `V002`** adds `auth_identity_providers`, `auth_invitations`, and corporate demo `demo@example.com` → Okta.

**Auth `V003`** adds consumer IdPs (**Gmail**, **Hotmail**, **Outlook**), domain routing (`gmail.com`, `hotmail.com`, `outlook.com`, …), and sample invitations (`user@gmail.com`, `user@hotmail.com`, `user@outlook.com`, `anyone@gmail.com` with domain-based IdP).

## When migrations run

| Command                                  | Behavior                                                               |
| ---------------------------------------- | ---------------------------------------------------------------------- |
| `make infra-dev` / `make infra-prod`     | Starts DBs (+ monitoring), then **`docker compose run sbp-migration`** |
| `make start-dev` / `make start-prod`     | Starts DBs → migrations → full stack                                   |
| `make migrate-dev` / `make migrate-prod` | Migrations only (DB containers must be healthy)                        |

**Reference `V001–V002`** adds `taxonomies`, `social_platforms`, and `entity_taxonomies` (for future **sbp-reference-service**). **Studio `V002`** adds `studios`. **Person `V002`** adds `body_types`, `relationship_types`, `persons`, `person_aliases`, `person_physical_attributes`, `person_studios`, and **`person_social_accounts`** (platform id references reference DB without cross-DB FK). Categories/tags/specialties use **`entity_taxonomies`** in the reference DB with `entity_type = 'PERSON'`.

**Reference `V004`** registers taxonomy type **`ROLE`** and seeds role taxonomies (`performer`, `director`, `producer`, `model`). Person profile **`roles`** are `entity_taxonomies` links to those rows — not JSON on `persons`.

**Person `V004`** partially normalizes career: `career_details`, `retired_year`, `last_active_year`, `person_career_highlights`, `person_featured_content` (content ids for future content-service enrichment), drops `persons.roles` / `persons.career` JSONB, and constrains `career_status` to `ACTIVE`, `RETIRED`, `HIATUS`, `DEBUT`.

**Content `V002`** adds the catalog schema for **sbp-content-service**: `contents`, origin countries, studio/cast/taxonomy link tables (external UUIDs only), and normalized `content_assets` / `content_asset_keys`. See [CONTENT-DATABASE.md](CONTENT-DATABASE.md).

In Compose, **auth**, **user**, **person**, **studio**, and **content** services wait for **`sbp-migration`** (`service_completed_successfully`) before starting.

## Docker

The **`sbp-migration`** service connects to **`auth-db`**, **`user-db`**, **`person-db`**, **`studio-db`**, **`content-db`**, and **`reference-db`** on the Compose network (`REFERENCE_DB_HOST=reference-db`, etc.).

## Local IDE (Postgres on localhost)

After `make infra-dev`, run migrations against host ports:

```powershell
$env:AUTH_DB_HOST="localhost"; $env:AUTH_DB_PORT="5432"
$env:USER_DB_HOST="localhost"; $env:USER_DB_PORT="5433"
# load names/users/passwords from env/.env.dev
.\mvnw.cmd -f sbp-migration/pom.xml package -DskipTests
java -jar sbp-migration/target/sbp-migration-1.0.0.jar
```

(Set `AUTH_DB_*`, `USER_DB_*`, `PERSON_DB_*`, `STUDIO_DB_*`, `CONTENT_DB_*`, and `REFERENCE_DB_*` from `env/.env.dev`.)

## JPA

Auth and user services use **`spring.jpa.hibernate.ddl-auto=validate`** in dev/prod so Hibernate does not alter schema; Liquibase owns the database.
