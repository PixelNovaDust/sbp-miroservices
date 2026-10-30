-- Extended person profile fields for API document (roles, career, source metadata)
ALTER TABLE persons
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(50) NOT NULL DEFAULT 'PERSON',
    ADD COLUMN IF NOT EXISTS roles JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS career JSONB,
    ADD COLUMN IF NOT EXISTS source_metadata JSONB;

ALTER TABLE persons
    ADD CONSTRAINT chk_persons_content_type CHECK (content_type IN ('PERSON'));
