-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhnOrgConfiguration
  add scapfile_upload_enabled
      char(1) default ('N') not null
      constraint rhn_org_conf_scap_upload_chk check (scapfile_upload_enabled in ('Y', 'N'));
alter table rhnOrgConfiguration
  add scap_file_sizelimit
      NUMERIC default(2097152) not null
      constraint rhn_org_conf_scap_szlmt_chk check (scap_file_sizelimit >= 0);
