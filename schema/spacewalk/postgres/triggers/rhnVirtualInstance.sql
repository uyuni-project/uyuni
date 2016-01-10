-- oracle equivalent source sha1 4dc3bef26b1701f5e6a67cd87e1f9df1abdec922

--
-- Copyright (c) 2008--2015 Red Hat, Inc.
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
--
-- 
--

create or replace function rhn_virtinst_mod_trig_fun() returns trigger
as
$$
begin
        new.modified := current_timestamp;

	return new;
end;
$$
language plpgsql;


create trigger
rhn_virtinst_mod_trig
before insert or update on rhnVirtualInstance
for each row
execute procedure rhn_virtinst_mod_trig_fun();

create or replace function rhn_virtinst_del_trig_fun() returns trigger
as
$$
begin
  delete from rhnVirtualInstance where virtual_system_id is NULL and host_system_id is NULL and uuid is not NULL;
  return null;
end;
$$
language plpgsql;

create trigger
rhn_virtinst_del_trig
after update on rhnVirtualInstance
for each row
execute procedure rhn_virtinst_del_trig_fun();
