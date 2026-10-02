-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnServerNetInterface DROP COLUMN ip_addr;
ALTER TABLE rhnServerNetInterface DROP COLUMN netmask;
ALTER TABLE rhnServerNetInterface DROP COLUMN broadcast;
