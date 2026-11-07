ALTER TABLE social_platforms
    ADD COLUMN IF NOT EXISTS logo UUID,
    ADD COLUMN IF NOT EXISTS svg TEXT;

UPDATE social_platforms
SET svg = svg_icon
WHERE svg IS NULL
    AND svg_icon IS NOT NULL
    AND BTRIM(svg_icon) <> '';
