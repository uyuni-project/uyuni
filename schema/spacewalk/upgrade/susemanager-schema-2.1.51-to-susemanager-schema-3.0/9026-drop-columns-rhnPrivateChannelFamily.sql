-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

alter table rhnPrivateChannelFamily drop column max_members;
alter table rhnPrivateChannelFamily drop column current_members;
alter table rhnPrivateChannelFamily drop column fve_max_members;
alter table rhnPrivateChannelFamily drop column fve_current_members;
