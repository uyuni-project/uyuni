-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionImageBuild ALTER COLUMN image_profile_id DROP NOT NULL;
ALTER TABLE rhnActionImageInspect ALTER COLUMN image_store_id DROP NOT NULL;
