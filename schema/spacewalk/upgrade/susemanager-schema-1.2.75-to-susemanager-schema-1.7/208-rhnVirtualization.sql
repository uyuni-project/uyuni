-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX rhn_vi_uuid_idx
    ON rhnVirtualInstance (uuid)
    TABLESPACE [[64k_tbs]];
