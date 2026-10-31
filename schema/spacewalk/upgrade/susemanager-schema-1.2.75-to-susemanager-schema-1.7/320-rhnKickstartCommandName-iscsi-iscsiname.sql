-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

update rhnKickstartCommandName set sort_order = 40 where name = 'iscsiname';
update rhnKickstartCommandName set sort_order = 41 where name = 'iscsi';
commit;

