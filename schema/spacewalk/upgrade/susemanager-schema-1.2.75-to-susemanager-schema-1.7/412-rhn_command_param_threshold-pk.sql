-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_command_param_threshold disable constraint rhn_coptr_id_p_name_p_type_pk;
drop index rhn_coptr_id_p_name_p_type_pk;
alter table rhn_command_param_threshold enable constraint rhn_coptr_id_p_name_p_type_pk;
