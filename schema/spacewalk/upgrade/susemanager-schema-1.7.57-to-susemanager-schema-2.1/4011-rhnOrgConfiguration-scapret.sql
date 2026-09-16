-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhnOrgConfiguration
  add scap_retention_period_days
      NUMERIC default (90)
      constraint rhn_org_conf_scap_reten_chk check (scap_retention_period_days >= 0);
