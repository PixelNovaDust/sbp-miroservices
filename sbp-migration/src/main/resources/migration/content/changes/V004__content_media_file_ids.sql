ALTER TABLE contents
    ADD COLUMN IF NOT EXISTS poster_file_id UUID,
    ADD COLUMN IF NOT EXISTS backdrop_file_id UUID;

ALTER TABLE content_asset_keys
    ADD COLUMN IF NOT EXISTS storage_file_id UUID;
