-- Optional brand assets: logo = file_storage.file_id (logical link), svg = inline markup
ALTER TABLE studios
    ADD COLUMN IF NOT EXISTS logo UUID,
    ADD COLUMN IF NOT EXISTS svg TEXT;
