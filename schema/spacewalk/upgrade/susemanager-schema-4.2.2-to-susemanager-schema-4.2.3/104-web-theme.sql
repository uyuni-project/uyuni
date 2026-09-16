-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnUserInfo ADD COLUMN IF NOT EXISTS web_theme VARCHAR(32);