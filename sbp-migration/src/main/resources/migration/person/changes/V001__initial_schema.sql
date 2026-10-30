-- Person DB baseline (sbp-person-service)
CREATE TABLE
    IF NOT EXISTS person_baseline (
        id BIGSERIAL PRIMARY KEY, -- PK; surrogate id
        external_id VARCHAR(64) NOT NULL, -- external person ref (unique)
        display_name VARCHAR(255), -- display name
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- last updated (UTC)
        CONSTRAINT uq_person_baseline_external_id UNIQUE (external_id)
    );

CREATE INDEX IF NOT EXISTS idx_person_baseline_external_id ON person_baseline (external_id);