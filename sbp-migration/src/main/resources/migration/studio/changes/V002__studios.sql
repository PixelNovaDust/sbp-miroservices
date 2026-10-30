-- Studio DB: master studio / company (sbp-studio-service)

CREATE TABLE IF NOT EXISTS studios (
    studio_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- PK; studio id
    name            VARCHAR(150) NOT NULL,                       -- studio name (unique)
    slug            VARCHAR(150) NOT NULL,                       -- URL slug (unique)
    website         TEXT,                                        -- public website
    description     TEXT,                                        -- description
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),          -- created (UTC)
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),          -- last updated (UTC)
    created_by      VARCHAR(100) NOT NULL,                       -- created by
    updated_by      VARCHAR(100) NOT NULL,                       -- updated by
    version         INTEGER NOT NULL DEFAULT 1,                  -- row version (auto on update)
    CONSTRAINT uq_studios_name UNIQUE (name),
    CONSTRAINT uq_studios_slug UNIQUE (slug)
);

CREATE INDEX IF NOT EXISTS idx_platform_name ON studios (name);  -- lookup by name

CREATE OR REPLACE FUNCTION studio_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION studio_bump_version()
RETURNS TRIGGER AS $$
BEGIN
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_studios_set_updated_at
    BEFORE UPDATE ON studios
    FOR EACH ROW EXECUTE FUNCTION studio_set_updated_at();

CREATE TRIGGER trg_studios_bump_version
    BEFORE UPDATE ON studios
    FOR EACH ROW EXECUTE FUNCTION studio_bump_version();
