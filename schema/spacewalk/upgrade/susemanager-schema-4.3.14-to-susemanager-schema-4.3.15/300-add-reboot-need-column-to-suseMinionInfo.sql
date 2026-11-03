-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseMinionInfo
    ADD COLUMN IF NOT EXISTS reboot_needed CHAR(1);
