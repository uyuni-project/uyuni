-- Copyright (c) 2020 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 509, 'virt.pool_refresh', 'Refresh a virtual storage pool', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 509)
);

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 510, 'virt.pool_start', 'Starts a virtual storage pool', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 510)
);

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 511, 'virt.pool_stop', 'Stops a virtual storage pool', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 511)
);

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 512, 'virt.pool_delete', 'Deletes a virtual storage pool', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 512)
);

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 513, 'virt.pool_create', 'Creates a virtual storage pool', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 513)
);

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only) (
    select 514, 'virt.volume_delete', 'Deletes a virtual storage volume', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 514)
);
