-- Person DB slice only (aliases, physical, social account ids, studio links, career highlights).
-- Taxonomies (including ROLE) and platform slugs come from sbp-reference-service; studio names from sbp-studio-service.
-- Parameter: :person_id (UUID)
SELECT jsonb_build_object(
    'id', p.person_id,
    'active', p.active,
    'name', p.name,
    'stageName', p.stage_name,
    'legalName', p.legal_name,
    'bio', p.bio,
    'birthDate', p.birth_date,
    'birthPlace', p.birth_place,
    'nationality', p.nationality,
    'gender', p.gender,
    'height', p.height_cm,
    'weight', p.weight_kg,
    'verified', p.verified,
    'debutYear', p.debut_year,
    'careerStatus', p.career_status,
    'retiredYear', p.retired_year,
    'lastActiveYear', p.last_active_year,
    'careerDetails', p.career_details,
    'profilePath', p.profile_path,
    'backdropPath', p.backdrop_path,
    'contentType', p.content_type,
    'sourceMetadata', p.source_metadata,
    'stats', '{}'::jsonb,
    'audit', jsonb_build_object(
        'createdAt', to_char(p.created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),
        'version', p.version
    ),
    'aliases', COALESCE(
        (SELECT jsonb_agg(pa.alias ORDER BY pa.alias)
         FROM person_aliases pa
         WHERE pa.person_id = p.person_id),
        '[]'::jsonb
    ),
    'careerHighlights', COALESCE(
        (SELECT jsonb_agg(pch.highlight_text ORDER BY pch.sort_order)
         FROM person_career_highlights pch
         WHERE pch.person_id = p.person_id),
        '[]'::jsonb
    ),
    'featuredContentIds', COALESCE(
        (SELECT jsonb_agg(pfc.content_id ORDER BY pfc.sort_order)
         FROM person_featured_content pfc
         WHERE pfc.person_id = p.person_id),
        '[]'::jsonb
    ),
    'measurements', (
        SELECT jsonb_build_object(
            'bust', pp.bust,
            'waist', pp.waist,
            'hips', pp.hips,
            'unit', pp.unit,
            'eyeColor', pp.eye_color,
            'hairColor', pp.hair_color,
            'bodyTypeId', pp.body_type_id,
            'bodyType', bt.name
        )
        FROM person_physical_attributes pp
        LEFT JOIN body_types bt ON bt.body_type_id = pp.body_type_id
        WHERE pp.person_id = p.person_id
    ),
    'socialPresence', COALESCE(
        (SELECT jsonb_agg(
            jsonb_build_object(
                'platformId', psa.social_platform_id,
                'username', psa.username,
                'verified', psa.verified,
                'available', psa.available
            ) ORDER BY psa.username
        )
         FROM person_social_accounts psa
         WHERE psa.person_id = p.person_id),
        '[]'::jsonb
    ),
    'studioLinks', COALESCE(
        (SELECT jsonb_agg(
            jsonb_build_object(
                'studioId', ps.studio_id,
                'relationshipType', rt.slug
            ) ORDER BY ps.created_at
        )
         FROM person_studios ps
         JOIN relationship_types rt ON rt.relationship_type_id = ps.relationship_type_id
         WHERE ps.person_id = p.person_id),
        '[]'::jsonb
    )
) AS person_local_document
FROM persons p
WHERE p.person_id = :person_id;
