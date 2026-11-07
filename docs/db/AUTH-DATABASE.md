# Auth database (`sbp_auth_*`)

Schema for **`sbp-auth-services`**. Liquibase: `migration/auth/changes/`. JPA uses **`ddl-auto=validate`** — schema changes go through migration only.

## Changelog

| Version | File                               | Purpose |
| ------- | ---------------------------------- | ------- |
| V001    | `V001__initial_schema.sql`         | `auth_users` (email login identity) |
| V002    | `V002__invitations_and_idp.sql`    | `auth_identity_providers`, `auth_invitations`; demo corporate IdP (`demo@example.com` → Okta) |
| V003    | `V003__consumer_idp_domains.sql`   | Consumer IdPs (Gmail, Hotmail, Outlook), `idp_email_domains`, sample invitations |

## Tables

### `auth_users`

Surrogate `id`, unique **`email`**, audit timestamps. Core login identity row.

### `auth_identity_providers` (V002+)

External IdP definitions (Okta, Google, Microsoft, …): slug, display metadata, OAuth/OIDC endpoints, secrets references, active flag.

### `auth_invitations` (V002+)

Pending invites: email, optional IdP hint, token/expiry, acceptance state.

### `idp_email_domains` (V003+)

Maps email domains to a preferred **`auth_identity_providers`** row for routing sign-in.

## Service notes

- Auth DB is isolated from user profiles (**user DB** links via `external_id` / email at the application layer).
- Env: `AUTH_DB_*` in `env/.env.dev` (host port **5432** in default dev compose).

See [MIGRATION.md](../MIGRATION.md) and [API-ENDPOINTS.md](../API-ENDPOINTS.md) (Auth section).
