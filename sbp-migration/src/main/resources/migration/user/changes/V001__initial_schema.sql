-- User DB baseline (sbp-user-services)
CREATE TABLE
    IF NOT EXISTS user_profiles (
        id BIGSERIAL PRIMARY KEY, -- PK; surrogate id
        external_id VARCHAR(64) NOT NULL, -- external user ref (unique)
        display_name VARCHAR(255), -- display name
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- last updated (UTC)
        CONSTRAINT uq_user_profiles_external_id UNIQUE (external_id)
    );

CREATE INDEX IF NOT EXISTS idx_user_profiles_external_id ON user_profiles (external_id);