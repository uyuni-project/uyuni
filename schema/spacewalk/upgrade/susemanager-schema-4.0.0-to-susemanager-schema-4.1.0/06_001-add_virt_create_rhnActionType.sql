-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 508, 'virt.create', 'Creates a virtual domain.', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 508)
);
