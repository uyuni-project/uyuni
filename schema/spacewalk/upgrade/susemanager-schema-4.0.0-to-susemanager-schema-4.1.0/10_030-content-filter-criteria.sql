-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS matcher VARCHAR(32);
ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS field VARCHAR(32);
ALTER TABLE suseContentFilter ADD COLUMN IF NOT EXISTS value VARCHAR(128);
ALTER TABLE suseContentFilter DROP COLUMN IF EXISTS criteria;

