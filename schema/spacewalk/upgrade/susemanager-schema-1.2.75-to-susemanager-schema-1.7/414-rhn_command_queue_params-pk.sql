-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_command_queue_params disable constraint rhn_cqprm_instance_id_ord_pk;
drop index rhn_cqprm_instance_id_ord_pk;
alter table rhn_command_queue_params enable constraint rhn_cqprm_instance_id_ord_pk;
