-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

delete from rhnServerGroupMembers where server_group_id in (select id from rhnServerGroup where name = 'Non-Linux Entitled Servers' and group_type is not null);

delete from rhnServerGroup where name = 'Non-Linux Entitled Servers' and group_type is not null;

delete from rhnServerGroupType where label = 'nonlinux_entitled';
