-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_config_macro disable constraint rhn_confm_name_pk;
drop index rhn_confm_name_pk;
alter table rhn_config_macro enable constraint rhn_confm_name_pk;
