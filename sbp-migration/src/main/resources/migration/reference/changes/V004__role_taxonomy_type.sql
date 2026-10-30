-- ROLE taxonomy type + seed role taxonomies for person profiles
INSERT INTO
    taxonomy_types (code, label, sort_order, created_by, updated_by)
VALUES
    ('ROLE', 'Role', 60, 'migration', 'migration')
ON CONFLICT (code) DO
UPDATE
SET
    label = EXCLUDED.label,
    sort_order = EXCLUDED.sort_order,
    updated_at = NOW(),
    updated_by = EXCLUDED.updated_by;

ALTER TABLE taxonomies
DROP CONSTRAINT IF EXISTS chk_taxonomies_type;

INSERT INTO
    taxonomies (type, name, slug, description, created_by, updated_by)
VALUES
    ('ROLE', 'Performer', 'performer', 'On-screen performer', 'migration', 'migration'),
    ('ROLE', 'Director', 'director', 'Director', 'migration', 'migration'),
    ('ROLE', 'Producer', 'producer', 'Producer', 'migration', 'migration'),
    ('ROLE', 'Model', 'model', 'Model / photoshoot talent', 'migration', 'migration')
ON CONFLICT (slug) DO
UPDATE
SET
    type = EXCLUDED.type,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    updated_at = NOW(),
    updated_by = EXCLUDED.updated_by;

DO $$
DECLARE
    invalid_count INTEGER;
    invalid_sample TEXT;
BEGIN
    SELECT COUNT(*), COALESCE(MAX(type), '')
    INTO invalid_count, invalid_sample
    FROM taxonomies
    WHERE type NOT IN ('CATEGORY', 'TAG', 'SPECIALTIES', 'MOOD_TAG', 'GENRE', 'ROLE');

    IF invalid_count > 0 THEN
        RAISE EXCEPTION
            'Cannot update taxonomy type constraint: % taxonomies have invalid type (example: %).',
            invalid_count,
            invalid_sample;
    END IF;
END $$;

ALTER TABLE taxonomies
ADD CONSTRAINT chk_taxonomies_type CHECK (
    type IN (
        'CATEGORY',
        'TAG',
        'SPECIALTIES',
        'MOOD_TAG',
        'GENRE',
        'ROLE'
    )
);
