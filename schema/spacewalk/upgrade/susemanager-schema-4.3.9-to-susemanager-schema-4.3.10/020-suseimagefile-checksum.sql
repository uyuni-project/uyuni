-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseImageFile
  ADD COLUMN IF NOT EXISTS checksum_id NUMERIC
  CONSTRAINT suse_fileinfo_chsum_fk
  REFERENCES rhnChecksum (id);
