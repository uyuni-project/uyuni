
ALTER TABLE suseImageInfo ADD image_type VARCHAR(32) NOT NULL DEFAULT 'dockerfile';
ALTER TABLE suseImageInfo ALTER COLUMN image_type DROP DEFAULT;

