-- Allowed taxonomy type codes (reference data) + FK from taxonomies.type
CREATE TABLE IF NOT EXISTS
    taxonomy_types (
        code VARCHAR(50) PRIMARY KEY, -- CATEGORY, TAG, SPECIALTIES, MOOD_TAG, GENRE
        label VARCHAR(100) NOT NULL, -- display label
        active BOOLEAN NOT NULL DEFAULT TRUE,
        sort_order INTEGER NOT NULL DEFAULT 0,
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
        created_by VARCHAR(100) NOT NULL,
        updated_by VARCHAR(100) NOT NULL
    );

INSERT INTO
    taxonomy_types (code, label, sort_order, created_by, updated_by)
VALUES
    ('CATEGORY', 'Category', 10, 'migration', 'migration'),
    ('TAG', 'Tag', 20, 'migration', 'migration'),
    ('SPECIALTIES', 'Specialties', 30, 'migration', 'migration'),
    ('MOOD_TAG', 'Mood tag', 40, 'migration', 'migration'),
    ('GENRE', 'Genre', 50, 'migration', 'migration')
ON CONFLICT (code) DO
UPDATE
SET
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW(),
    updated_by = EXCLUDED.updated_by;

-- Normalize existing taxonomy rows before FK / CHECK
UPDATE taxonomies
SET
    type = UPPER(BTRIM(type))
WHERE
    type IS NOT NULL;

UPDATE taxonomies
SET
    type = 'SPECIALTIES'
WHERE
    type IN ('SPECIALTY', 'SPECIALITIES');

DO $$
DECLARE
    invalid_count INTEGER;
    invalid_sample TEXT;
BEGIN
    SELECT COUNT(*), COALESCE(MAX(type), '')
    INTO invalid_count, invalid_sample
    FROM taxonomies
    WHERE type NOT IN ('CATEGORY', 'TAG', 'SPECIALTIES', 'MOOD_TAG', 'GENRE');

    IF invalid_count > 0 THEN
        RAISE EXCEPTION
            'Cannot add taxonomy type constraint: % taxonomies have invalid type (example: %). Fix or deactivate those rows first.',
            invalid_count,
            invalid_sample;
    END IF;
END $$;

ALTER TABLE taxonomies
DROP CONSTRAINT IF EXISTS chk_taxonomies_type;

ALTER TABLE taxonomies
ADD CONSTRAINT chk_taxonomies_type CHECK (
    type IN (
        'CATEGORY',
        'TAG',
        'SPECIALTIES',
        'MOOD_TAG',
        'GENRE'
    )
);

ALTER TABLE taxonomies
DROP CONSTRAINT IF EXISTS fk_taxonomies_type;

ALTER TABLE taxonomies
ADD CONSTRAINT fk_taxonomies_type FOREIGN KEY (type) REFERENCES taxonomy_types (code);

CREATE TRIGGER trg_taxonomy_types_set_updated_at BEFORE
UPDATE ON taxonomy_types FOR EACH ROW
EXECUTE FUNCTION reference_set_updated_at ();
