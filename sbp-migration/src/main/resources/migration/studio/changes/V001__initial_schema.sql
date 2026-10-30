-- Studio DB baseline placeholder (sbp-studio-service)
CREATE TABLE
    IF NOT EXISTS studio_baseline (
        id BIGSERIAL PRIMARY KEY, -- PK; surrogate id
        external_id VARCHAR(64) NOT NULL, -- external ref (unique)
        display_name VARCHAR(255), -- display name
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- last updated (UTC)
        CONSTRAINT uq_studio_baseline_external_id UNIQUE (external_id)
    );

CREATE INDEX IF NOT EXISTS idx_studio_baseline_external_id ON studio_baseline (external_id);