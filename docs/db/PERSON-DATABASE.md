# Person database (`sbp_person_*`)

Schema for **`sbp-person-service`**. Liquibase: `migration/person/changes/`. Taxonomies and social **platform definitions** live in the **reference DB**; studio master data in the **studio DB** — person tables store UUID links only (no cross-database FKs).

## Changelog

| Version | File                                           | Purpose |
| ------- | ---------------------------------------------- | ------- |
| V001    | `V001__initial_schema.sql`                     | `person_baseline` placeholder |
| V002    | `V002__person_schema.sql`                      | `body_types`, `relationship_types`, `persons`, aliases, physical attributes, `person_studios`, `person_social_accounts` |
| V003    | `V003__person_profile_extensions.sql`          | Profile extensions (paths, metadata) |
| V004    | `V004__career_normalize_roles_drop_json.sql`   | Career tables, featured content links; drops JSON `roles` / `career` on `persons`; `career_status` enum |
| V005    | `V005__person_career_previous_profession.sql`  | `previous_profession`, `start_year` on career |
| V006    | `V006__person_media_file_ids.sql`            | `profile_file_id`, `backdrop_file_id` → file registry |
| V007    | `V007__drop_person_media_paths.sql`          | Drops legacy `profile_path`, `backdrop_path` |

## Key tables

### `persons` (aggregate root)

Identity, bio, demographics, career summary fields, media **`profile_file_id`** / **`backdrop_file_id`** (UUIDs into file-storage DB), audit + optimistic **`version`**.

### Lookups

- **`body_types`**, **`relationship_types`** — person-local catalogs (slug + active).
- **`person_baseline`** — external sync placeholder (seed/dev).

### Associations (person DB only)

- **`person_aliases`**, **`person_physical_attributes`**, **`person_studios`** (`studio_id` UUID),
- **`person_social_accounts`** (`platform_id` UUID → reference DB),
- **`person_career_highlights`**, **`person_featured_content`** (`content_id` UUID → content service).

### Roles and taxonomies

**ROLE** / category / tag links for persons are stored in **reference** `entity_taxonomies` (`entity_type = 'PERSON'`), not as JSON on `persons`.

## Constraints

- **`gender`**: `MALE`, `FEMALE`, `TRANS` (nullable).
- **`career_status`**: `ACTIVE`, `RETIRED`, `HIATUS`, `DEBUT` (V004+).

## SQL helpers

Ad-hoc JSON for local person rows: [PERSON-PROFILE-QUERIES.md](PERSON-PROFILE-QUERIES.md).

See [API-ENDPOINTS.md](../API-ENDPOINTS.md) (Person section) and [MIGRATION.md](../MIGRATION.md).
