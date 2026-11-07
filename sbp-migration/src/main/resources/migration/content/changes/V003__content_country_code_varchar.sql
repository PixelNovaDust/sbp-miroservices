-- Hibernate maps @Column(length = 2) String to VARCHAR(2); V002 used CHAR(2).
ALTER TABLE content_production_countries
    ALTER COLUMN country_code TYPE VARCHAR(2);
