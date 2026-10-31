-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhnChannelPermission disable constraint rhn_cperm_rid_fk;
alter table rhnChannelPermissionRole disable constraint rhn_cperm_role_id_pk;
drop index rhn_cperm_role_id_pk;
alter table rhnChannelPermissionRole enable constraint rhn_cperm_role_id_pk;
alter table rhnChannelPermission enable constraint rhn_cperm_rid_fk;
