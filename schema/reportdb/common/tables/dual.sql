--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- oracle equivalent source none

create table dual ( dummy char );

insert into dual values ( 'X' );

create or replace rule deny_insert_dual as on insert to dual do instead nothing;
create or replace rule deny_update_dual as on update to dual do instead nothing;
create or replace rule deny_delete_dual as on delete to dual do instead nothing;
