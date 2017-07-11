ALTER TABLE suseImageInfo ADD COLUMN curr_revision_num NUMBER NOT NULL DEFAULT 1;
ALTER TABLE suseImageInfo ALTER COLUMN curr_revision_num DROP DEFAULT;
