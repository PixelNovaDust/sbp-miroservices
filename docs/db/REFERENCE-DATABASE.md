# Reference database (`sbp_reference_*`)

Shared catalog for **`sbp-reference-service`**: taxonomies, social platforms, and cross-entity taxonomy links. Liquibase: `migration/reference/changes/`.

## Changelog

| Version | File                                           | Purpose |
| ------- | ---------------------------------------------- | ------- |
| V001    | `V001__taxonomies_and_social_platforms.sql`    | `taxonomies`, `social_platforms` |
| V002    | `V002__entity_taxonomies.sql`                  | `entity_taxonomies` (link taxonomies to PERSON, STUDIO, …) |
| V003    | `V003__taxonomy_types.sql`                     | Taxonomy `type` constraints / seed types |
| V004    | `V004__role_taxonomy_type.sql`                 | **`ROLE`** type; seed role taxonomies (`performer`, `director`, …) |
| V005    | `V005__social_platform_logo_svg.sql`           | `logo` (file UUID), `svg`; backfill from legacy `svg_icon` |

## Tables

### `taxonomies`

UUID id, **`type`** (`CATEGORY`, `TAG`, `SPECIALTIES`, `ROLE`, `MOOD_TAG`, …), name, slug, active, audit + version.

### `social_platforms`

Platform catalog: name, slug, **`base_url`**, branding (`svg`, **`logo`**, `brand_color`), active, audit + version.

### `entity_taxonomies`

`(entity_type, entity_id, taxonomy_id)` — attaches reference taxonomies to persons, studios, or other entities **without** storing person/studio rows in this DB.

## Service notes

- Person **roles** and specialty links are **`entity_taxonomies`** rows, not person DB JSON.
- Batch read: `GET /internal/v1/taxonomies/batch?ids=...`.
- SQL helper: `migration/reference/queries/get_social_platform_json.sql`.

See [API-ENDPOINTS.md](../API-ENDPOINTS.md) (Reference / taxonomies / social platforms) and [MIGRATION.md](../MIGRATION.md).
