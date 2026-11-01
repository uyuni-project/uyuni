-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

-- add the new column to store the products which have no successors

ALTER TABLE rhnActionDup ADD COLUMN IF NOT EXISTS missing_successors VARCHAR(512);
