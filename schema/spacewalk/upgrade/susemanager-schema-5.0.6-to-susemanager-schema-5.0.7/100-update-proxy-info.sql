-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE rhnProxyInfo ADD COLUMN IF NOT EXISTS ssh_public_key BYTEA;
