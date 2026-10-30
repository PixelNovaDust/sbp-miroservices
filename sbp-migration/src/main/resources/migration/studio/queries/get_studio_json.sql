-- Parameter: :studio_id (UUID)
SELECT jsonb_build_object(
    'id', s.studio_id,
    'name', s.name,
    'slug', s.slug,
    'website', s.website,
    'description', s.description,
    'active', s.active,
    'audit', jsonb_build_object(
        'createdAt', to_char(s.created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),
        'version', s.version
    )
) AS studio_document
FROM studios s
WHERE s.studio_id = :studio_id;
