-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionImageBuild RENAME COLUMN tag to version;
ALTER TABLE rhnActionImageInspect RENAME COLUMN tag to version;
