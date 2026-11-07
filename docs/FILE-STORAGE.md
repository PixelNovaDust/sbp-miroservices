# File storage

Central metadata lives in **`file_storage`** (Postgres, **`sbp-file-storage-service`**). Bytes live in **S3 / MinIO** via **`ObjectStorageClient`** in **`sbp-lib-common`**.

## Who calls what (no direct `/files/upload` for end users)

Browsers and mobile apps **do not** call `POST /api/v1/files/upload` with JSON metadata. They call **your domain API** (e.g. person profile upload). **Your microservice** does the work using reusable code; the client only sends the file (and maybe nothing extra if the person id is in the URL).

```text
Client  →  POST /api/v1/persons/{id}/profile-image  (person-service)
              ↓
         person-service uses shared storage + registry (see below)
              ↓
         S3/MinIO object + file_storage row + fileId on person
```

The **`/api/v1/files/*`** routes are for **service-to-service** (or ops/tools), not the primary UX path.

## Two layers (important)

| Layer                                  | What it does automatically                                   | What it does _not_ do                            |
| -------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------ |
| **`ObjectStorageClient` (lib-common)** | Bucket/region/credentials, object key, MIME, PUT/presign GET | **`file_storage` row**, owner, visibility, audit |
| **`sbp-file-storage-service`**         | S3 **and** DB row, status lifecycle, download URLs           | Your person/content business rules               |

So “reusable upload” in lib-common **automatically** handles object-store details when you pass **stream + filename + (optional) bucket + key prefix**. It does **not** by itself register **`fileId`** in Postgres—that is **`file-storage-service`** (or a small HTTP client wrapper you add in lib-common later).

## Recommended pattern (DRY, one place owns S3 + DB)

Enable **`sbp.storage.enabled=true` only on `sbp-file-storage-service`**. Other services **do not** talk to S3 directly.

Person (or content) service adds an endpoint and delegates:

Domain services use **`FileStorageServiceClient`** in **`sbp-lib-common`** (`com.sbp.common.client`): multipart **`POST /api/v1/files/upload`** and **`GET /api/v1/files/{fileId}/download-url`**. Configure **`sbp.clients.file-storage-base-url`** / **`FILE_STORAGE_SERVICE_URL`**. After upload, persist the returned **`fileId`** on the domain row (`persons.profile_file_id`, `studios.logo`, etc.). **File-storage service** alone enables **`ObjectStorageClient`** for S3/MinIO I/O.

## Alternative (S3 from many services)

If a service enables lib-common storage and calls **`ObjectStorageClient.upload`**, it must **also** register metadata (HTTP to file-storage or shared orchestrator). Otherwise you get orphan S3 objects with no **`fileId`**.

## Object storage env (`SBP_STORAGE_*`)

Used where **`sbp.storage.enabled=true`** (typically **file-storage service only**):

| Variable                     | Purpose                           |
| ---------------------------- | --------------------------------- |
| `SBP_STORAGE_PROVIDER`       | `S3`, `MINIO`, …                  |
| `SBP_STORAGE_REGION`         | AWS region                        |
| `SBP_STORAGE_DEFAULT_BUCKET` | Default when request omits bucket |
| `SBP_STORAGE_ENDPOINT`       | MinIO / custom (empty for AWS)    |
| `SBP_STORAGE_KEY_PREFIX`     | e.g. `dev/` on every key          |

Credentials: AWS SDK default chain — set **`AWS_ACCESS_KEY_ID`** / **`AWS_SECRET_ACCESS_KEY`** (and optional **`AWS_REGION`**) in **`env/.env.dev`** (see **`env/.env.dev.example`** for AWS S3, **R2**, and **MinIO** blocks). Docker dev compose passes these into **`sbp-file-storage-service`**.

## File-storage HTTP API (service-to-service)

Base: **`/api/v1/files`** (gateway or `FILE_STORAGE_SERVICE_URL`).

| Method | Path                                  | Use                                                  |
| ------ | ------------------------------------- | ---------------------------------------------------- |
| POST   | `/upload`                             | Another service forwards multipart + owner metadata  |
| POST   | `/register-reference`                 | Register an existing object key (dev seed / imports) without upload |
| POST   | `/presign-upload`                     | Large files: client PUTs to S3, then `/{id}/confirm` |
| GET    | `/{fileId}`, `/{fileId}/download-url` | Resolve metadata / signed read URL                   |
| DELETE | `/{fileId}`                           | Soft delete + object delete                          |

## Database

Liquibase: `migration/file/changes/V001__file_storage.sql` — env **`FILE_STORAGE_DB_*`**.

## Local dev

1. Vars from `env/.env.dev.example` → `env/.env.dev`.
2. `./scripts/run.sh migrate dev` → `./scripts/run.sh lib` → `./scripts/run.sh svc start <service>`. **`svc start`** ensures prerequisites in order: **Discovery → API Gateway → File storage** (each step skipped if already up), then the service you asked for. **File storage** itself only pulls Discovery and Gateway.
3. Set bucket + credentials (or MinIO: `SBP_STORAGE_PROVIDER=MINIO`, `SBP_STORAGE_ENDPOINT`, `SBP_STORAGE_PATH_STYLE=true`).

## Next implementation step

Add **`FileStorageServiceClient`** in lib-common (or person-service client package) that wraps `/upload` and `/download-url` so domain code is one call—no curl, no manual metadata JSON from the UI.
