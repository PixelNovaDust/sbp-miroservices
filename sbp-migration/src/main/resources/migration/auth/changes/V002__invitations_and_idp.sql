-- Auth DB: identity providers + invitations
CREATE TABLE
    IF NOT EXISTS auth_identity_providers (
        id BIGSERIAL PRIMARY KEY, -- PK; provider id
        provider_key VARCHAR(64) NOT NULL, -- stable key (unique)
        display_name VARCHAR(255) NOT NULL, -- UI label
        login_url VARCHAR(1024), -- IdP login URL
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        CONSTRAINT uq_auth_idp_provider_key UNIQUE (provider_key)
    );

CREATE TABLE
    IF NOT EXISTS auth_invitations (
        id BIGSERIAL PRIMARY KEY, -- PK; invitation id
        email VARCHAR(320) NOT NULL, -- invited email
        idp_id BIGINT NOT NULL REFERENCES auth_identity_providers (id), -- FK IdP
        status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, etc.
        expires_at TIMESTAMPTZ, -- expiry (UTC)
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW () -- last updated (UTC)
    );

CREATE INDEX IF NOT EXISTS idx_auth_invitations_email ON auth_invitations (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_auth_invitations_status ON auth_invitations (status);

INSERT INTO
    auth_identity_providers (provider_key, display_name, login_url)
VALUES
    (
        'okta-corporate',
        'Corporate Okta',
        'https://example.okta.com/login'
    ) ON CONFLICT (provider_key) DO NOTHING;

INSERT INTO
    auth_invitations (email, idp_id, status, expires_at)
SELECT
    'demo@example.com',
    p.id,
    'PENDING',
    NOW () + INTERVAL '30 days'
FROM
    auth_identity_providers p
WHERE
    p.provider_key = 'okta-corporate'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            auth_invitations i
        WHERE
            LOWER(i.email) = LOWER('demo@example.com')
    );