-- Auth DB: consumer IdPs + email-domain routing
INSERT INTO
  auth_identity_providers (provider_key, display_name, login_url)
VALUES
  (
    'google-gmail',
    'Gmail',
    'https://accounts.google.com/'
  ),
  (
    'microsoft-hotmail',
    'Hotmail',
    'https://login.live.com/'
  ),
  (
    'microsoft-outlook',
    'Outlook',
    'https://outlook.live.com/'
  ) ON CONFLICT (provider_key) DO NOTHING;

CREATE TABLE
  IF NOT EXISTS auth_idp_email_domains (
    id BIGSERIAL PRIMARY KEY, -- PK; mapping id
    email_domain VARCHAR(255) NOT NULL, -- email domain (unique)
    idp_id BIGINT NOT NULL REFERENCES auth_identity_providers (id), -- FK IdP
    CONSTRAINT uq_auth_idp_email_domain UNIQUE (email_domain)
  );

CREATE INDEX IF NOT EXISTS idx_auth_idp_email_domains_domain ON auth_idp_email_domains (LOWER(email_domain));

INSERT INTO
  auth_idp_email_domains (email_domain, idp_id)
SELECT
  v.domain,
  p.id
FROM
  (
    VALUES
      ('gmail.com', 'google-gmail'),
      ('googlemail.com', 'google-gmail'),
      ('hotmail.com', 'microsoft-hotmail'),
      ('outlook.com', 'microsoft-outlook'),
      ('live.com', 'microsoft-outlook'),
      ('msn.com', 'microsoft-outlook')
  ) AS v (domain, provider_key)
  JOIN auth_identity_providers p ON p.provider_key = v.provider_key ON CONFLICT (email_domain) DO NOTHING;

-- allow null idp_id; resolve IdP from domain at check time
ALTER TABLE auth_invitations
ALTER COLUMN idp_id
DROP NOT NULL;

INSERT INTO
  auth_invitations (email, idp_id, status, expires_at)
SELECT
  'user@gmail.com',
  p.id,
  'PENDING',
  NOW () + INTERVAL '30 days'
FROM
  auth_identity_providers p
WHERE
  p.provider_key = 'google-gmail'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      auth_invitations i
    WHERE
      LOWER(i.email) = 'user@gmail.com'
  );

INSERT INTO
  auth_invitations (email, idp_id, status, expires_at)
SELECT
  'user@hotmail.com',
  p.id,
  'PENDING',
  NOW () + INTERVAL '30 days'
FROM
  auth_identity_providers p
WHERE
  p.provider_key = 'microsoft-hotmail'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      auth_invitations i
    WHERE
      LOWER(i.email) = 'user@hotmail.com'
  );

INSERT INTO
  auth_invitations (email, idp_id, status, expires_at)
SELECT
  'user@outlook.com',
  p.id,
  'PENDING',
  NOW () + INTERVAL '30 days'
FROM
  auth_identity_providers p
WHERE
  p.provider_key = 'microsoft-outlook'
  AND NOT EXISTS (
    SELECT
      1
    FROM
      auth_invitations i
    WHERE
      LOWER(i.email) = 'user@outlook.com'
  );

INSERT INTO
  auth_invitations (email, idp_id, status, expires_at)
SELECT
  'anyone@gmail.com',
  NULL,
  'PENDING',
  NOW () + INTERVAL '30 days'
WHERE
  NOT EXISTS (
    SELECT
      1
    FROM
      auth_invitations i
    WHERE
      LOWER(i.email) = 'anyone@gmail.com'
  );