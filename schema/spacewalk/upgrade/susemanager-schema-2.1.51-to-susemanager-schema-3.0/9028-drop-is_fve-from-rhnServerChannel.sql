-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnServerChannel DROP CONSTRAINT rhn_server_channel_is_fve_ck;
ALTER TABLE rhnServerChannel DROP COLUMN is_fve;

