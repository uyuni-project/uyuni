-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhn_os_commands_xref disable constraint rhn_oscxr_os_id_commands_id_pk;
drop index rhn_oscxr_os_id_commands_id_pk;
alter table rhn_os_commands_xref enable constraint rhn_oscxr_os_id_commands_id_pk;
