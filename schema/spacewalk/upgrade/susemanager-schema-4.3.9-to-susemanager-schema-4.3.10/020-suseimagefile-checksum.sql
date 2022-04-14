ALTER TABLE suseImageFile
  ADD COLUMN IF NOT EXISTS checksum_id NUMERIC
  CONSTRAINT suse_fileinfo_chsum_fk
  REFERENCES rhnChecksum (id);
