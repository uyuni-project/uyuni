-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseImageInfo ADD external_image CHAR(1) DEFAULT ('N') NOT NULL;
