# Person / studio / platform SQL reference

Cross-database aggregates are built in **sbp-person-service** (`GET /api/v1/persons/{personId}`) using internal HTTP calls to reference and studio services—not cross-DB SQL.

## Person DB (local document)

File: [`sbp-migration/src/main/resources/migration/person/queries/get_person_local_profile_json.sql`](../sbp-migration/src/main/resources/migration/person/queries/get_person_local_profile_json.sql)

```bash
# Example (psql against person DB)
psql -h localhost -p 5434 -U sbp_person -d sbp_person_dev -v person_id="'c91f2a44-2d8a-47f1-bfa2-7d33b6f8e291'" -f sbp-migration/src/main/resources/migration/person/queries/get_person_local_profile_json.sql
```

Replace `:person_id` with `\set person_id` or use `-v` depending on client.

## Studio DB

[`sbp-migration/src/main/resources/migration/studio/queries/get_studio_json.sql`](../sbp-migration/src/main/resources/migration/studio/queries/get_studio_json.sql)

Prefer **`GET /api/v1/studios/{studioId}`** (gateway) or **`GET /api/v1/studios/internal/{studioId}`** (service-to-service).

## Reference DB (social platform)

[`sbp-migration/src/main/resources/migration/reference/queries/get_social_platform_json.sql`](../sbp-migration/src/main/resources/migration/reference/queries/get_social_platform_json.sql)

Prefer **`GET /api/v1/social-platforms/{platformId}`**.

## Taxonomies for a person

Stored in **reference DB** `entity_taxonomies`. Person service exposes:

- `GET /api/v1/persons/{personId}/taxonomies`
- Assign/remove via POST/DELETE on the same prefix (proxied to reference internal API).
