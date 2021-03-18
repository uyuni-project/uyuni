-- Copyright (c) 2021 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

insert into rhnActionType (id, label, name, trigger_snapshot, unlocked_only, maintenance_mode_only) (
    select 520, 'virt.network_create', 'Creates a virtual network', 'N', 'N', 'N'
    from dual
    where not exists (select 1 from rhnActionType where id = 520)
);
