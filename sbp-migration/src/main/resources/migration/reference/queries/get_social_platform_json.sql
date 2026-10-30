-- Parameter: :social_platform_id (UUID)
SELECT jsonb_build_object(
    'platformId', sp.social_platform_id,
    'platform', sp.slug,
    'name', sp.name,
    'baseUrl', sp.base_url,
    'brandColor', sp.brand_color,
    'active', sp.active,
    'audit', jsonb_build_object(
        'createdAt', to_char(sp.created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),
        'version', sp.version
    )
) AS social_platform_document
FROM social_platforms sp
WHERE sp.social_platform_id = :social_platform_id;
