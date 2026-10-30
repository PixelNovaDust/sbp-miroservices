-- Reference DB: entity ↔ taxonomy (entity_id in other services; no cross-DB FK)
CREATE TABLE
    IF NOT EXISTS entity_taxonomies (
        entity_taxonomies_id UUID PRIMARY KEY DEFAULT gen_random_uuid (), -- PK; link id
        entity_id UUID NOT NULL, -- target entity id
        entity_type VARCHAR(50) NOT NULL, -- PERSON, VIDEO, STUDIO, COLLECTION
        taxonomy_id UUID NOT NULL REFERENCES taxonomies (taxonomies_id), -- FK taxonomies
        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW (), -- created (UTC)
        CONSTRAINT uq_entity_taxonomies_entity_taxonomy UNIQUE (entity_id, entity_type, taxonomy_id),
        CONSTRAINT chk_entity_taxonomies_entity_type CHECK (
            entity_type IN ('PERSON', 'VIDEO', 'STUDIO', 'COLLECTION')
        )
    );

CREATE INDEX IF NOT EXISTS idx_entity_taxonomy_entity ON entity_taxonomies (entity_id);

CREATE INDEX IF NOT EXISTS idx_entity_taxonomy_type ON entity_taxonomies (entity_type);

CREATE INDEX IF NOT EXISTS idx_entity_taxonomy_taxonomy ON entity_taxonomies (taxonomy_id);