-- oracle equivalent source sha1 98c02e574d4346f7eecade4697a2b26a13abaa28

ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS matcher VARCHAR(32);
ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS field VARCHAR(32);
ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS value VARCHAR(128);
ALTER TABLE suseContentFilter DROP COLUMN IF EXISTS criteria;

