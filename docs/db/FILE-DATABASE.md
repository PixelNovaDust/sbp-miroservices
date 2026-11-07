# File storage database (`sbp_file_storage_*`)

Registry for object storage metadata (**`sbp-file-storage-service`**). Liquibase: `migration/file/changes/`. Binary bytes live in S3/MinIO/etc.; this DB holds keys, ownership, and lifecycle.

## Changelog

| Version | File                         | Purpose |
| ------- | ---------------------------- | ------- |
| V001    | `V001__file_storage.sql`     | `file_storage` table + status/visibility checks |
| V002    | `V002__file_storage_checksum_varchar.sql` | `checksum_sha256` → `VARCHAR(64)` (Hibernate validation) |

## Table: `file_storage`

| Area | Columns |
| ---- | ------- |
| Identity | `file_id` (UUID PK) |
| Object location | `storage_provider`, `bucket_name`, `object_key`, optional `region`, `etag`, `checksum_sha256` |
| File meta | `file_name`, `original_file_name`, `mime_type`, `file_size_bytes` |
| Lifecycle | `status`, `visibility`, `deleted_at` |
| Ownership | `owner_service`, `owner_entity_type`, `owner_entity_id`, `purpose`, `sort_order`, optional `parent_file_id` |
| Extra | `metadata` (JSONB), audit + `version` |

### `status`

`PENDING_UPLOAD`, `AVAILABLE`, `PROCESSING`, `FAILED`, `ARCHIVED`, `DELETED`.

### `visibility`

`PUBLIC`, `PRIVATE`, `UNLISTED`.

### `storage_provider`

`S3`, `MINIO`, `GCS`, `AZURE_BLOB`, `LOCAL`.

## Cross-service references

Domain services store **`file_id`** UUIDs (person profile/backdrop, studio logo, content poster, platform logo, asset keys) — validated via **`FileStorageServiceClient`** / HTTP, not FK across databases.

See [FILE-STORAGE.md](../FILE-STORAGE.md), [MIGRATION.md](../MIGRATION.md), and [API-ENDPOINTS.md](../API-ENDPOINTS.md).
