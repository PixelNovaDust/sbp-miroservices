-- Content catalog (sbp-content-service): normalized core + link tables (external ids only, no cross-DB FK)

DROP TABLE IF EXISTS content_baseline;

CREATE TABLE
    contents (
        content_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- aggregate root
        title VARCHAR(500) NOT NULL, -- display title
        tagline VARCHAR(500), -- short tagline
        overview TEXT, -- synopsis
        adult BOOLEAN NOT NULL DEFAULT FALSE, -- adult flag (promo / gating hints)
        content_type VARCHAR(32) NOT NULL, -- MOVIE | TV | SCENES
        content_rating VARCHAR(64) NOT NULL, -- age / indexing tier
        status VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- lifecycle
        visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE', -- exposure
        original_language VARCHAR(16), -- ISO 639-1 (e.g. en)
        release_date DATE, -- premiere / publish date
        runtime_minutes INT, -- duration minutes
        poster_path TEXT, -- storage key / path
        backdrop_path TEXT, -- storage key / path
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- audit created (UTC)
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- audit updated (UTC)
        version INT NOT NULL DEFAULT 0, -- optimistic lock
        CONSTRAINT chk_contents_content_type CHECK (
            content_type IN ('MOVIE', 'TV', 'SCENES')
        ),
        CONSTRAINT chk_contents_content_rating CHECK (
            content_rating IN (
                'ALL_AGES',
                'MATURE_16_PLUS',
                'ADULT_18_PLUS',
                'RESTRICTED_ADULT_21_PLUS'
            )
        ),
        CONSTRAINT chk_contents_status CHECK (
            status IN ('DRAFT', 'SCHEDULED', 'RELEASED', 'ARCHIVED')
        ),
        CONSTRAINT chk_contents_visibility CHECK (
            visibility IN ('PUBLIC', 'UNLISTED', 'PRIVATE')
        ),
        CONSTRAINT chk_contents_runtime CHECK (
            runtime_minutes IS NULL
            OR runtime_minutes > 0
        )
    );

CREATE INDEX IF NOT EXISTS idx_contents_release_date ON contents (release_date);

CREATE INDEX IF NOT EXISTS idx_contents_content_type ON contents (content_type);

CREATE INDEX IF NOT EXISTS idx_contents_status ON contents (status);

CREATE INDEX IF NOT EXISTS idx_contents_visibility ON contents (visibility);

CREATE TABLE
    content_production_countries (
        content_id UUID NOT NULL REFERENCES contents (content_id) ON DELETE CASCADE,
        country_code CHAR(2) NOT NULL, -- ISO 3166-1 alpha-2 (origin / production)
        sort_order INT NOT NULL DEFAULT 0,
        PRIMARY KEY (content_id, country_code),
        CONSTRAINT chk_content_production_countries_sort CHECK (sort_order >= 0)
    );

CREATE TABLE
    content_studios (
        content_id UUID NOT NULL REFERENCES contents (content_id) ON DELETE CASCADE,
        studio_id UUID NOT NULL, -- studio-service id (validated via HTTP)
        sort_order INT NOT NULL DEFAULT 0,
        PRIMARY KEY (content_id, studio_id),
        CONSTRAINT chk_content_studios_sort CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_content_studios_studio ON content_studios (studio_id);

CREATE TABLE
    content_cast (
        content_id UUID NOT NULL REFERENCES contents (content_id) ON DELETE CASCADE,
        person_id UUID NOT NULL, -- person-service id (validated via HTTP)
        sort_order INT NOT NULL DEFAULT 0,
        PRIMARY KEY (content_id, person_id),
        CONSTRAINT chk_content_cast_sort CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_content_cast_person ON content_cast (person_id);

CREATE TABLE
    content_taxonomies (
        content_id UUID NOT NULL REFERENCES contents (content_id) ON DELETE CASCADE,
        taxonomy_id UUID NOT NULL, -- reference-service id (validated via HTTP)
        taxonomy_type VARCHAR(32) NOT NULL, -- denormalized: TAG | GENRE | MOOD_TAG
        sort_order INT NOT NULL DEFAULT 0,
        PRIMARY KEY (content_id, taxonomy_id),
        CONSTRAINT chk_content_taxonomies_type CHECK (
            taxonomy_type IN ('TAG', 'GENRE', 'MOOD_TAG')
        ),
        CONSTRAINT chk_content_taxonomies_sort CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_content_taxonomies_taxonomy ON content_taxonomies (taxonomy_id);

CREATE INDEX IF NOT EXISTS idx_content_taxonomies_type ON content_taxonomies (content_id, taxonomy_type);

CREATE TABLE
    content_assets (
        asset_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        content_id UUID NOT NULL REFERENCES contents (content_id) ON DELETE CASCADE,
        asset_type VARCHAR(32) NOT NULL, -- CLIP | TRAILER | BTS | TEASER
        sort_order INT NOT NULL DEFAULT 0,
        CONSTRAINT chk_content_assets_type CHECK (
            asset_type IN ('CLIP', 'TRAILER', 'BTS', 'TEASER')
        ),
        CONSTRAINT chk_content_assets_sort CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_content_assets_content ON content_assets (content_id);

CREATE TABLE
    content_asset_keys (
        key_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        asset_id UUID NOT NULL REFERENCES content_assets (asset_id) ON DELETE CASCADE,
        storage_key TEXT NOT NULL, -- object storage path / URL path
        sort_order INT NOT NULL DEFAULT 0,
        CONSTRAINT chk_content_asset_keys_sort CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_content_asset_keys_asset ON content_asset_keys (asset_id);
