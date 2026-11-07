ALTER TABLE persons
    ADD COLUMN IF NOT EXISTS profile_file_id UUID,
    ADD COLUMN IF NOT EXISTS backdrop_file_id UUID;
