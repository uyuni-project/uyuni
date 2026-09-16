-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnActionScap ADD COLUMN IF NOT EXISTS
    ovalfiles   VARCHAR(8192);
