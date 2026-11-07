# Studio database (`sbp_studio_*`)

Schema for **`sbp-studio-service`**. Liquibase: `migration/studio/changes/`.

## Changelog

| Version | File                           | Purpose                                            |
| ------- | ------------------------------ | -------------------------------------------------- |
| V001    | `V001__initial_schema.sql`     | `studio_baseline` placeholder                      |
| V002    | `V002__studios.sql`            | `studios` master table + update triggers           |
| V003    | `V003__studio_active_flag.sql` | `active` flag on `studios`                         |
| V004    | `V004__studio_logo_svg.sql`    | `logo` (file registry UUID), inline `svg` branding |

## Tables

### `studios`

UUID **`studio_id`**, unique **`name`** / **`slug`**, website, description, **`active`**, optional **`logo`** + **`svg`**, audit fields, optimistic **`version`**.

### `studio_baseline`

External sync placeholder (`external_id`, `display_name`) for integrations and seed data.

## Service notes

- **`person_studios`** and **`content_studios`** reference **`studio_id`** without FK.
- Internal read: `GET /api/v1/studios/internal/{studioId}`.
- SQL helper: `migration/studio/queries/get_studio_json.sql` (see [PERSON-PROFILE-QUERIES.md](PERSON-PROFILE-QUERIES.md)).

See [API-ENDPOINTS.md](../API-ENDPOINTS.md) (Studio section) and [MIGRATION.md](../MIGRATION.md).
