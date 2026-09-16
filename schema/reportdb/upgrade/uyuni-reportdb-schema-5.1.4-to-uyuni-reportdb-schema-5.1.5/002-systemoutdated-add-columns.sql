-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE SystemOutdated ADD COLUMN IF NOT EXISTS extra_pkg_count BIGINT;
ALTER TABLE SystemOutdated ADD COLUMN IF NOT EXISTS status VARCHAR(20);
