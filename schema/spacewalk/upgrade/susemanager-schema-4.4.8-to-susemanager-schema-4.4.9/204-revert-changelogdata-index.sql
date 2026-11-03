-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

drop index if exists rhn_pkg_cld_ntt_idx;
create index if not exists rhn_pkg_cld_nt_idx
    on rhnPackageChangeLogData (name, time);
