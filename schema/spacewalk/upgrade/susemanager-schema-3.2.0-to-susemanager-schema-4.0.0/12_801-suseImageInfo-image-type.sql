-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseImageInfo ADD image_type VARCHAR(32) NOT NULL DEFAULT 'dockerfile';
ALTER TABLE suseImageInfo ALTER COLUMN image_type DROP DEFAULT;

