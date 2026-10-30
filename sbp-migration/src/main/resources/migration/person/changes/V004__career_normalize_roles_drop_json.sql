-- Partial career normalization; drop roles/career JSON (roles use reference ROLE taxonomies)
ALTER TABLE persons
    ADD COLUMN IF NOT EXISTS career_details TEXT,
    ADD COLUMN IF NOT EXISTS retired_year SMALLINT,
    ADD COLUMN IF NOT EXISTS last_active_year SMALLINT;

UPDATE persons
SET
    career_details = COALESCE(
        career_details,
        NULLIF(BTRIM(career ->> 'details'), '')
    )
WHERE
    career IS NOT NULL;

UPDATE persons
SET
    career_status = UPPER(BTRIM(career_status))
WHERE
    career_status IS NOT NULL;

UPDATE persons
SET
    career_status = 'ACTIVE'
WHERE
    career_status IN ('ACTIVE', 'Active', 'active');

UPDATE persons
SET
    career_status = 'RETIRED'
WHERE
    career_status IN ('RETIRED', 'Retired', 'retired');

UPDATE persons
SET
    career_status = 'HIATUS'
WHERE
    career_status IN ('HIATUS', 'Hiatus', 'hiatus');

UPDATE persons
SET
    career_status = 'ACTIVE'
WHERE
    career_status IS NOT NULL
    AND career_status NOT IN ('ACTIVE', 'RETIRED', 'HIATUS', 'DEBUT');

CREATE TABLE IF NOT EXISTS
    person_career_highlights (
        highlight_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        person_id UUID NOT NULL REFERENCES persons (person_id) ON DELETE CASCADE,
        sort_order SMALLINT NOT NULL DEFAULT 0,
        highlight_text TEXT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        CONSTRAINT uq_person_career_highlight_order UNIQUE (person_id, sort_order),
        CONSTRAINT chk_person_career_highlight_text CHECK (char_length(BTRIM(highlight_text)) > 0)
    );

CREATE INDEX IF NOT EXISTS idx_person_career_highlights_person ON person_career_highlights (person_id);

CREATE TABLE IF NOT EXISTS
    person_featured_content (
        person_id UUID NOT NULL REFERENCES persons (person_id) ON DELETE CASCADE,
        content_id UUID NOT NULL,
        sort_order SMALLINT NOT NULL DEFAULT 0,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        PRIMARY KEY (person_id, content_id),
        CONSTRAINT chk_person_featured_content_sort CHECK (sort_order >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_person_featured_content_person ON person_featured_content (person_id);

ALTER TABLE persons
DROP COLUMN IF EXISTS roles,
DROP COLUMN IF EXISTS career;

ALTER TABLE persons
DROP CONSTRAINT IF EXISTS chk_persons_career_status;

ALTER TABLE persons
ADD CONSTRAINT chk_persons_career_status CHECK (
    career_status IS NULL
    OR career_status IN ('ACTIVE', 'RETIRED', 'HIATUS', 'DEBUT')
);

ALTER TABLE persons
ADD CONSTRAINT chk_persons_retired_year CHECK (
    retired_year IS NULL
    OR retired_year > 1950
);

ALTER TABLE persons
ADD CONSTRAINT chk_persons_last_active_year CHECK (
    last_active_year IS NULL
    OR last_active_year > 1950
);
