# Infra service (`sbp-infra-service`)

Central file metadata lives in **`file_storage`** (Postgres, **`INFRA_DB_*`**). Bytes live in **S3 / MinIO** via **`ObjectStorageClient`** in **`sbp-lib-common`**.

## Architecture

| Layer                                  | Responsibility                                                    |
| -------------------------------------- | ----------------------------------------------------------------- |
| **`ObjectStorageClient` (lib-common)** | Bucket/region/credentials, object key, MIME, PUT/presign GET      |
| **`sbp-infra-service`**                | REST API, JPA on `file_storage`, S3 orchestration, presigned URLs |
| **Domain services**                    | Call infra over HTTP (`INFRA_SERVICE_URL`); store **`fileId`**    |

```text
Client → person-service → INFRA_SERVICE_URL (upload/register) → S3 + file_storage
```

## Env

| Variable                               | Purpose                                 |
| -------------------------------------- | --------------------------------------- |
| `INFRA_PORT` / `INFRA_MANAGEMENT_PORT` | Service (8088 / 9088)                   |
| `INFRA_SERVICE_URL`                    | Direct base URL                         |
| `INFRA_DB_*`                           | Postgres for `file_storage`             |
| `SBP_STORAGE_*`                        | Object store (enabled on infra service) |
| `INFRA_MAX_UPLOAD_SIZE`                | Multipart limit                         |

## API (gateway)

Base: **`/api/v1/files`** → `lb://sbp-infra-service`

See prior file-storage API table in git history; endpoints unchanged.

## Local dev

```bash
./scripts/run.sh migrate dev
./scripts/run.sh lib
./scripts/run.sh svc start infra
```

## Database

Liquibase: `migration/file/changes/V001__file_storage.sql` (table name unchanged).
