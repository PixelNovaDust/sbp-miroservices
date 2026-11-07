-- Hibernate maps @Column(length = 64) String to VARCHAR(64); V001 used CHAR(64) (bpchar).
ALTER TABLE file_storage
    ALTER COLUMN checksum_sha256 TYPE VARCHAR(64);
