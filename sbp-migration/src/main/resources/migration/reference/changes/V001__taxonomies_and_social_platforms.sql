-- Reference DB: taxonomies + social platforms (sbp-reference-service)
CREATE TABLE IF NOT EXISTS
    taxonomies (
        taxonomies_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; taxonomy id
        type VARCHAR(50) NOT NULL, -- CATEGORY, TAG, SPECIALTIES, etc.
        name VARCHAR(100) NOT NULL, -- display name (unique)
        slug VARCHAR(100) NOT NULL, -- URL slug (unique)
        description TEXT, -- internal notes
        active BOOLEAN NOT NULL DEFAULT TRUE, -- enabled flag
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        created_by VARCHAR(100) NOT NULL, -- created by
        updated_by VARCHAR(100) NOT NULL, -- updated by
        version INTEGER NOT NULL DEFAULT 1, -- row version (auto on update)
        CONSTRAINT uq_taxonomies_name UNIQUE (name),
        CONSTRAINT uq_taxonomies_slug UNIQUE (slug)
    );

CREATE INDEX IF NOT EXISTS idx_taxonomy_type ON taxonomies (
    type
);

-- filter by type
CREATE INDEX IF NOT EXISTS idx_taxonomy_name ON taxonomies (name);

-- lookup by name
CREATE TABLE IF NOT EXISTS
    social_platforms (
        social_platform_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; platform id
        name VARCHAR(100) NOT NULL, -- platform name (unique)
        slug VARCHAR(100) NOT NULL, -- URL slug (unique)
        base_url VARCHAR(255) NOT NULL, -- platform base URL
        svg_icon TEXT, -- SVG icon markup
        brand_color VARCHAR(20), -- hex brand color
        active BOOLEAN NOT NULL DEFAULT TRUE, -- enabled flag
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- last updated (UTC)
        created_by VARCHAR(100) NOT NULL, -- created by
        updated_by VARCHAR(100) NOT NULL, -- updated by
        version INTEGER NOT NULL DEFAULT 1, -- row version (auto on update)
        CONSTRAINT uq_social_platforms_name UNIQUE (name),
        CONSTRAINT uq_social_platforms_slug UNIQUE (slug)
    );

CREATE INDEX IF NOT EXISTS idx_social_platforms_name ON social_platforms (name);

CREATE INDEX IF NOT EXISTS idx_social_platforms_slug ON social_platforms (slug);

CREATE
OR REPLACE FUNCTION reference_set_updated_at () RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE
OR REPLACE FUNCTION reference_bump_version () RETURNS TRIGGER AS $$
BEGIN
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_taxonomies_set_updated_at BEFORE
UPDATE ON taxonomies FOR EACH ROW
EXECUTE FUNCTION reference_set_updated_at ();

CREATE TRIGGER trg_taxonomies_bump_version BEFORE
UPDATE ON taxonomies FOR EACH ROW
EXECUTE FUNCTION reference_bump_version ();

CREATE TRIGGER trg_social_platforms_set_updated_at BEFORE
UPDATE ON social_platforms FOR EACH ROW
EXECUTE FUNCTION reference_set_updated_at ();

CREATE TRIGGER trg_social_platforms_bump_version BEFORE
UPDATE ON social_platforms FOR EACH ROW
EXECUTE FUNCTION reference_bump_version ();