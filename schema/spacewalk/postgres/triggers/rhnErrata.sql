-- oracle equivalent source sha1 7b707c1278cbf541c8908ad8c2a6298b3e19c345

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

create or replace function rhn_errata_ins_trig_fun() returns trigger
as
$$
begin
     if ( new.last_modified is null ) then
        new.last_modified := current_timestamp;
     end if;

     new.modified := current_timestamp;

     return new;
end;
$$ language plpgsql;

create trigger
rhn_errata_ins_trig
before insert on rhnErrata
for each row
execute procedure rhn_errata_ins_trig_fun();


create or replace function rhn_errata_upd_trig_fun() returns trigger
as
$$
begin
     if ( new.last_modified = old.last_modified ) or
        ( new.last_modified is null )  then
        new.last_modified := current_timestamp;
     end if;

     new.modified := current_timestamp;

     return new;
end;
$$ language plpgsql;

create trigger
rhn_errata_upd_trig
before update on rhnErrata
for each row
execute procedure rhn_errata_upd_trig_fun();
