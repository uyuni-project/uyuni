-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseMinionInfo ADD COLUMN IF NOT EXISTS
    ssh_push_port NUMERIC;
