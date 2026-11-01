-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE System ADD COLUMN IF NOT EXISTS last_boot_time TIMESTAMPTZ;
