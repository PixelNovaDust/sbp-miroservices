-- Auth DB baseline (sbp-auth-services)
CREATE TABLE
    IF NOT EXISTS auth_users (
        id BIGSERIAL PRIMARY KEY, -- PK; surrogate id
        email VARCHAR(320) NOT NULL, -- login email (unique)
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- last updated (UTC)
        CONSTRAINT uq_auth_users_email UNIQUE (email)
    );

CREATE INDEX IF NOT EXISTS idx_auth_users_email ON auth_users (email);