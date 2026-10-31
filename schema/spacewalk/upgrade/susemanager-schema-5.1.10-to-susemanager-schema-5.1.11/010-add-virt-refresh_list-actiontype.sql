-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnActionType 
select 527, 'virt.refresh_list', 'Refresh virtual instance information', 'N', 'N', 'N'
where not exists (select 1 from rhnActionType where id = 527);
