-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

delete from rhn_config_parameter where group_name='cf_db' and name='tableowner';
delete from rhn_config_parameter where group_name='cs_db' and name='tableowner';
delete from rhn_config_macro where name='RHN_DB_TABLE_OWNER';
