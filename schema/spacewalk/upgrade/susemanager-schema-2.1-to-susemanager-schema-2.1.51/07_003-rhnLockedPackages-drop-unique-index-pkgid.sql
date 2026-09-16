-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

drop index rhn_lp_pkg_id_uq;

create index rhn_lp_pkg_id_idx
  on rhnLockedPackages (pkg_id);
