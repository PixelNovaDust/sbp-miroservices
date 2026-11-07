-- File storage registry (sbp-file-storage-service): metadata for objects in S3/MinIO/etc.

CREATE TABLE IF NOT EXISTS
    file_storage (
        file_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        storage_provider VARCHAR(32) NOT NULL,
        bucket_name VARCHAR(255) NOT NULL,
        object_key TEXT NOT NULL,
        file_name VARCHAR(500),
        object_reference TEXT,
        region VARCHAR(64),
        original_file_name VARCHAR(500),
        mime_type VARCHAR(127),
        file_size_bytes BIGINT,
        checksum_sha256 CHAR(64),
        etag VARCHAR(128),
        status VARCHAR(32) NOT NULL,
        visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
        deleted_at TIMESTAMPTZ,
        owner_service VARCHAR(64) NOT NULL,
        owner_entity_type VARCHAR(32) NOT NULL,
        owner_entity_id UUID NOT NULL,
        purpose VARCHAR(64) NOT NULL,
        sort_order SMALLINT NOT NULL DEFAULT 0,
        parent_file_id UUID REFERENCES file_storage (file_id),
        metadata JSONB,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        created_by VARCHAR(100) NOT NULL,
        updated_by VARCHAR(100) NOT NULL,
        version INTEGER NOT NULL DEFAULT 1,
        CONSTRAINT chk_file_storage_provider CHECK (
            storage_provider IN ('S3', 'MINIO', 'GCS', 'AZURE_BLOB', 'LOCAL')
        ),
        CONSTRAINT chk_file_storage_status CHECK (
            status IN (
                'PENDING_UPLOAD',
                'AVAILABLE',
                'PROCESSING',
                'FAILED',
                'ARCHIVED',
                'DELETED'
            )
        ),
        CONSTRAINT chk_file_storage_visibility CHECK (
            visibility IN ('PUBLIC', 'PRIVATE', 'UNLISTED')
        ),
        CONSTRAINT chk_file_storage_size CHECK (
            file_size_bytes IS NULL
            OR file_size_bytes >= 0
        ),
        CONSTRAINT chk_file_storage_sort CHECK (sort_order >= 0)
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_file_storage_object_active ON file_storage (
    storage_provider,
    bucket_name,
    object_key
)
WHERE
    status <> 'DELETED';

CREATE INDEX IF NOT EXISTS idx_file_storage_owner ON file_storage (owner_entity_type, owner_entity_id);

CREATE INDEX IF NOT EXISTS idx_file_storage_owner_service_purpose ON file_storage (owner_service, purpose);

CREATE INDEX IF NOT EXISTS idx_file_storage_status ON file_storage (status);

CREATE INDEX IF NOT EXISTS idx_file_storage_created_at ON file_storage (created_at);

CREATE OR REPLACE FUNCTION file_storage_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION file_storage_bump_version()
RETURNS TRIGGER AS $$
BEGIN
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_file_storage_set_updated_at
    BEFORE UPDATE ON file_storage
    FOR EACH ROW EXECUTE FUNCTION file_storage_set_updated_at();

CREATE TRIGGER trg_file_storage_bump_version
    BEFORE UPDATE ON file_storage
    FOR EACH ROW EXECUTE FUNCTION file_storage_bump_version();
