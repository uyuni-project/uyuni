-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseImageInfo ADD COLUMN curr_revision_num NUMERIC NOT NULL DEFAULT 1;
ALTER TABLE suseImageInfo ALTER COLUMN curr_revision_num DROP DEFAULT;
