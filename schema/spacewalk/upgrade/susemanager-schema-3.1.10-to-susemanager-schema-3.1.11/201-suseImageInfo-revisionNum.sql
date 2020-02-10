-- oracle equivalent source sha1 23c7c005b231cfb3cfaf650f1249fc94b941373a
ALTER TABLE suseImageInfo ADD COLUMN curr_revision_num NUMERIC NOT NULL DEFAULT 1;
ALTER TABLE suseImageInfo ALTER COLUMN curr_revision_num DROP DEFAULT;
