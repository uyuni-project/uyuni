-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE INDEX IF NOT EXISTS rhn_pkg_cap_name_idx
    ON rhnPackageCapability USING HASH (name);
