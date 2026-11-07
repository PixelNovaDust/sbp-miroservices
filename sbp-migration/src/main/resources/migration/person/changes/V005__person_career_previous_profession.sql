-- Career timeline fields on persons (API career.previousProfession, career.startYear)
ALTER TABLE persons
    ADD COLUMN IF NOT EXISTS previous_profession VARCHAR(255),
    ADD COLUMN IF NOT EXISTS career_start_year SMALLINT;

ALTER TABLE persons
DROP CONSTRAINT IF EXISTS chk_persons_career_start_year;

ALTER TABLE persons
ADD CONSTRAINT chk_persons_career_start_year CHECK (
    career_start_year IS NULL
    OR career_start_year > 1950
);
