-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

drop index rhn_puac_pa_pua;
create unique index rhn_puac_pa_pua_uq
     on rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id);

