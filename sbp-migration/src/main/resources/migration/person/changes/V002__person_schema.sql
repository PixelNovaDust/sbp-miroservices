-- Person DB: profiles, lookups, studio links, social accounts (sbp-person-service)
-- Taxonomies / entity_taxonomies / social_platforms live in reference DB; studios in studio DB.
CREATE TABLE IF NOT EXISTS
    body_types (
        body_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; body type id
        name VARCHAR(100) NOT NULL, -- display name (unique)
        slug VARCHAR(100) NOT NULL, -- URL slug (unique)
        description TEXT, -- classification notes
        active BOOLEAN NOT NULL DEFAULT TRUE, -- enabled flag
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        created_by VARCHAR(100) NOT NULL, -- created by
        updated_by VARCHAR(100) NOT NULL, -- updated by
        version INTEGER NOT NULL DEFAULT 1, -- optimistic lock
        CONSTRAINT uq_body_types_name UNIQUE (name),
        CONSTRAINT uq_body_types_slug UNIQUE (slug)
    );

CREATE TABLE IF NOT EXISTS
    relationship_types (
        relationship_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; relationship type id
        name VARCHAR(100) NOT NULL, -- display name (unique)
        slug VARCHAR(100) NOT NULL, -- URL slug (unique)
        description TEXT, -- relationship notes
        active BOOLEAN NOT NULL DEFAULT TRUE, -- enabled flag
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        created_by VARCHAR(100) NOT NULL, -- created by
        updated_by VARCHAR(100) NOT NULL, -- updated by
        version INTEGER NOT NULL DEFAULT 1, -- optimistic lock
        CONSTRAINT uq_relationship_types_name UNIQUE (name),
        CONSTRAINT uq_relationship_types_slug UNIQUE (slug)
    );

CREATE TABLE IF NOT EXISTS
    persons (
        person_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; person id
        name VARCHAR(150) NOT NULL, -- public display name
        slug VARCHAR(150) NOT NULL, -- URL slug
        stage_name VARCHAR(150), -- stage / professional name
        legal_name VARCHAR(150) NOT NULL, -- legal name
        bio TEXT, -- biography
        birth_date DATE, -- date of birth
        birth_place VARCHAR(255), -- birth location
        nationality VARCHAR(100), -- nationality
        gender VARCHAR(30), -- MALE, FEMALE, TRANS
        height_cm SMALLINT, -- height in cm
        weight_kg SMALLINT, -- weight in kg
        verified BOOLEAN NOT NULL DEFAULT FALSE, -- verification flag
        active BOOLEAN NOT NULL DEFAULT TRUE, -- active profile flag
        debut_year SMALLINT, -- career debut year
        career_status VARCHAR(50), -- e.g. Active, Retired
        profile_path TEXT, -- profile image path
        backdrop_path TEXT, -- cover image path
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        created_by VARCHAR(100) NOT NULL, -- created by
        updated_by VARCHAR(100) NOT NULL, -- updated by
        version INTEGER NOT NULL DEFAULT 1, -- optimistic lock
        CONSTRAINT uq_persons_slug UNIQUE (slug),
        CONSTRAINT chk_persons_gender CHECK (
            gender IS NULL
            OR gender IN ('MALE', 'FEMALE', 'TRANS')
        ),
        CONSTRAINT chk_persons_birth_date CHECK (
            birth_date IS NULL
            OR birth_date > DATE '1950-01-01'
        ),
        CONSTRAINT chk_persons_debut_year CHECK (
            debut_year IS NULL
            OR debut_year > 1950
        ),
        CONSTRAINT chk_persons_height_cm CHECK (
            height_cm IS NULL
            OR height_cm > 0
        ),
        CONSTRAINT chk_persons_weight_kg CHECK (
            weight_kg IS NULL
            OR weight_kg > 0
        )
    );

CREATE INDEX IF NOT EXISTS idx_person_name ON persons (name);

CREATE INDEX IF NOT EXISTS idx_person_stage_name ON persons (stage_name);

CREATE INDEX IF NOT EXISTS idx_person_active ON persons (active);

CREATE INDEX IF NOT EXISTS idx_person_verified ON persons (verified);

CREATE TABLE IF NOT EXISTS
    person_aliases (
        person_aliases_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; alias row id
        person_id UUID NOT NULL REFERENCES persons (person_id) ON DELETE CASCADE, -- FK person
        alias VARCHAR(255) NOT NULL, -- alternate name
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        CONSTRAINT uq_person_aliases_person_alias UNIQUE (person_id, alias)
    );

CREATE INDEX IF NOT EXISTS idx_alias_person ON person_aliases (person_id);

CREATE INDEX IF NOT EXISTS idx_alias_name ON person_aliases (alias);

CREATE TABLE IF NOT EXISTS
    person_physical_attributes (
        person_id UUID PRIMARY KEY REFERENCES persons (person_id) ON DELETE CASCADE, -- PK/FK person
        bust SMALLINT, -- bust measurement
        waist SMALLINT, -- waist measurement
        hips SMALLINT, -- hip measurement
        body_type_id UUID REFERENCES body_types (body_type_id), -- FK body type
        eye_color VARCHAR(50), -- eye color
        hair_color VARCHAR(50), -- hair color
        unit VARCHAR(20) NOT NULL DEFAULT 'inches', -- inches or cm
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        CONSTRAINT chk_person_physical_bust CHECK (
            bust IS NULL
            OR bust > 0
        ),
        CONSTRAINT chk_person_physical_waist CHECK (
            waist IS NULL
            OR waist > 0
        ),
        CONSTRAINT chk_person_physical_hips CHECK (
            hips IS NULL
            OR hips > 0
        )
    );

CREATE TABLE IF NOT EXISTS
    person_studios (
        person_studio_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; link id
        person_id UUID NOT NULL REFERENCES persons (person_id) ON DELETE CASCADE, -- FK person
        studio_id UUID NOT NULL, -- studio id (studio DB; no cross-DB FK)
        relationship_type_id UUID NOT NULL REFERENCES relationship_types (relationship_type_id), -- FK type
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        CONSTRAINT uq_person_studios_link UNIQUE (person_id, studio_id, relationship_type_id)
    );

CREATE INDEX IF NOT EXISTS idx_person_studio_person ON person_studios (person_id);

CREATE INDEX IF NOT EXISTS idx_person_studio_studio ON person_studios (studio_id);

CREATE TABLE IF NOT EXISTS
    person_social_accounts (
        person_social_account_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; account row id
        person_id UUID NOT NULL REFERENCES persons (person_id) ON DELETE CASCADE, -- FK person
        social_platform_id UUID NOT NULL, -- platform id (reference DB; no FK)
        username VARCHAR(150) NOT NULL, -- platform username
        verified BOOLEAN NOT NULL DEFAULT FALSE, -- verified on platform
        available BOOLEAN NOT NULL DEFAULT TRUE, -- link still available
        followers_count BIGINT, -- last known followers
        last_synced_at TIMESTAMPTZ, -- last sync (UTC)
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        created_by VARCHAR(100) NOT NULL, -- created by
        updated_by VARCHAR(100) NOT NULL, -- updated by
        version INTEGER NOT NULL DEFAULT 1, -- optimistic lock
        CONSTRAINT uq_person_social_person_platform UNIQUE (person_id, social_platform_id),
        CONSTRAINT uq_person_social_accounts_username UNIQUE (username)
    );

CREATE INDEX IF NOT EXISTS idx_person_social_entity ON person_social_accounts (username);

CREATE INDEX IF NOT EXISTS idx_person_social_platform ON person_social_accounts (social_platform_id);

CREATE OR REPLACE FUNCTION person_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION person_bump_version()
RETURNS TRIGGER AS $$
BEGIN
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_body_types_set_updated_at ON body_types;
CREATE TRIGGER trg_body_types_set_updated_at
    BEFORE UPDATE ON body_types
    FOR EACH ROW EXECUTE FUNCTION person_set_updated_at();

DROP TRIGGER IF EXISTS trg_body_types_bump_version ON body_types;
CREATE TRIGGER trg_body_types_bump_version
    BEFORE UPDATE ON body_types
    FOR EACH ROW EXECUTE FUNCTION person_bump_version();

DROP TRIGGER IF EXISTS trg_relationship_types_set_updated_at ON relationship_types;
CREATE TRIGGER trg_relationship_types_set_updated_at
    BEFORE UPDATE ON relationship_types
    FOR EACH ROW EXECUTE FUNCTION person_set_updated_at();

DROP TRIGGER IF EXISTS trg_relationship_types_bump_version ON relationship_types;
CREATE TRIGGER trg_relationship_types_bump_version
    BEFORE UPDATE ON relationship_types
    FOR EACH ROW EXECUTE FUNCTION person_bump_version();

DROP TRIGGER IF EXISTS trg_persons_set_updated_at ON persons;
CREATE TRIGGER trg_persons_set_updated_at
    BEFORE UPDATE ON persons
    FOR EACH ROW EXECUTE FUNCTION person_set_updated_at();

DROP TRIGGER IF EXISTS trg_persons_bump_version ON persons;
CREATE TRIGGER trg_persons_bump_version
    BEFORE UPDATE ON persons
    FOR EACH ROW EXECUTE FUNCTION person_bump_version();

DROP TRIGGER IF EXISTS trg_person_physical_attributes_set_updated_at ON person_physical_attributes;
CREATE TRIGGER trg_person_physical_attributes_set_updated_at
    BEFORE UPDATE ON person_physical_attributes
    FOR EACH ROW EXECUTE FUNCTION person_set_updated_at();

DROP TRIGGER IF EXISTS trg_person_social_accounts_set_updated_at ON person_social_accounts;
CREATE TRIGGER trg_person_social_accounts_set_updated_at
    BEFORE UPDATE ON person_social_accounts
    FOR EACH ROW EXECUTE FUNCTION person_set_updated_at();

DROP TRIGGER IF EXISTS trg_person_social_accounts_bump_version ON person_social_accounts;
CREATE TRIGGER trg_person_social_accounts_bump_version
    BEFORE UPDATE ON person_social_accounts
    FOR EACH ROW EXECUTE FUNCTION person_bump_version();