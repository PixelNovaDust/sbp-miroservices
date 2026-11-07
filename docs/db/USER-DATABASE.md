# User database (`sbp_user_*`)

Schema for **`sbp-user-services`**. Liquibase: `migration/user/changes/`.

## Changelog

| Version | File                       | Purpose |
| ------- | -------------------------- | ------- |
| V001    | `V001__initial_schema.sql` | `user_profiles` baseline |

## Tables

### `user_profiles`

Surrogate `id`, unique **`external_id`** (string key from auth or upstream identity), optional **`display_name`**, audit timestamps.

## Service notes

- No cross-database FK to auth; correlate users by **`external_id`** or email in application code.
- Env: `USER_DB_*` (host port **5433** in default dev compose).

See [MIGRATION.md](../MIGRATION.md) and [API-ENDPOINTS.md](../API-ENDPOINTS.md) (User section).
