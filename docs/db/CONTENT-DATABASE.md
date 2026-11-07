# Content database (`sbp_content_*`)

Schema is owned by **`sbp-migration`** under `migration/content/changes/`. The content service stores **only content-domain rows**; studio, person, and taxonomy identities are **UUID references** validated and enriched via HTTP (no cross-database foreign keys).

## Changelog

| Version | File                               | Purpose                                                                                             |
| ------- | ---------------------------------- | --------------------------------------------------------------------------------------------------- |
| V001    | `V001__initial_schema.sql`         | Placeholder baseline (replaced by V002)                                                             |
| V002    | `V002__content_catalog_schema.sql` | Catalog aggregate + association tables (`content_studios`, `content_cast`, `content_taxonomies`, …) |
| V003    | `V003__content_country_code_varchar.sql` | `content_production_countries.country_code` → `VARCHAR(2)` (matches JPA / Hibernate validate) |
| V004    | `V004__content_media_file_ids.sql` | `poster_file_id`, `backdrop_file_id`, `content_asset_keys.storage_file_id` → file registry |

After pulling new changesets, run `./scripts/run.sh migrate dev` (content DB must be up). If migrate fails on **V002 checksum**, run `./scripts/run.sh migrate dev --repair` once, then `migrate dev` again.

## Enum constraints (PostgreSQL `CHECK`)

| Column / use                       | Allowed values                                                            |
| ---------------------------------- | ------------------------------------------------------------------------- |
| `contents.content_type`            | `MOVIE`, `TV`, `SCENES`                                                   |
| `contents.content_rating`          | `ALL_AGES`, `MATURE_16_PLUS`, `ADULT_18_PLUS`, `RESTRICTED_ADULT_21_PLUS` |
| `contents.status`                  | `DRAFT`, `SCHEDULED`, `RELEASED`, `ARCHIVED`                              |
| `contents.visibility`              | `PUBLIC`, `UNLISTED`, `PRIVATE`                                           |
| `content_assets.asset_type`        | `CLIP`, `TRAILER`, `BTS`, `TEASER`                                        |
| `content_taxonomies.taxonomy_type` | `TAG`, `GENRE`, `MOOD_TAG` (denormalized from reference taxonomy `type`)  |

## Tables

### `contents` (aggregate root)

Poster/backdrop: legacy **`poster_path`** / **`backdrop_path`** (optional text keys) plus **`poster_file_id`** / **`backdrop_file_id`** (UUIDs in **`file_storage`**). Reads expose presigned **`posterUrl`** / **`backdropUrl`** when file ids are set.

Core metadata: title, tagline, overview, `adult`, type/rating, lifecycle (`status`, `visibility`), language, release date, runtime, audit timestamps, optimistic `version`.

### `content_production_countries`

ISO 3166-1 alpha-2 origin / production countries per title (`US`, …) with `sort_order`.

### `content_studios`

`(content_id, studio_id)` — same pattern as **`person_studios`**; `studio_id` is **studio-service** UUID (no FK).

### `content_cast`

`(content_id, person_id)` — credited performers; **person-service** UUID (no FK).

### `content_taxonomies`

`(content_id, taxonomy_id)` plus denormalized `taxonomy_type` (`TAG`, `GENRE`, `MOOD_TAG`). Taxonomy rows live in **reference-service**.

### `content_assets` / `content_asset_keys`

One asset row per logical asset (type + order); many storage keys per asset (variants, CDN paths, etc.). Keys may set optional **`storage_file_id`** (registry UUID) in addition to **`storage_key`** text.

## Stats (not persisted yet)

Profile **`stats`** (`averageRating`, `totalLikes`, `totalViews`) are **computed defaults** (zeros) in the API until a future analytics or engagement schema is added.

## Service independence

| Need                | Approach                                    |
| ------------------- | ------------------------------------------- |
| Studio name on read | `GET /api/v1/studios/internal/{studioId}`   |
| Taxonomy names      | `GET /internal/v1/taxonomies/batch?ids=...` |
| Cast names          | `GET /api/v1/persons/{personId}`            |
| Write validation    | Same HTTP calls before insert/update        |

See [API-ENDPOINTS.md](../API-ENDPOINTS.md) (Content section) and [MIGRATION.md](../MIGRATION.md).
