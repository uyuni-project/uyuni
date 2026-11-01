-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_config_parameter disable constraint rhn_confp_group_name_name_pk;
drop index rhn_confp_group_name_name_pk;
alter table rhn_config_parameter enable constraint rhn_confp_group_name_name_pk;
