-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionApplyStates ADD COLUMN IF NOT EXISTS
    pillars TEXT;
