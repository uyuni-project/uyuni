-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX IF NOT EXISTS rhn_snc_seid_idx
    ON rhnServerNeededCache (server_id, errata_id);
