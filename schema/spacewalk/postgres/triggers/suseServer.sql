-- oracle equivalent source sha1 a5714bc2dc05c20b7cc5c8aa0012c7f99bdde29c
-- retrieved from ./1290790062/5e78063ef0ba88a3e95b0f77fbd8d842353665a0/schema/spacewalk/oracle/triggers/suseServer.sql
--
-- Copyright (c) 2010 Novell
-- Copyright (c) 2011 SUSE Linux Products GmbH
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
create or replace function suse_server_mod_trig_fun() returns trigger
as
$$
begin
            new.modified := current_timestamp;

            return new;
end;
$$
language plpgsql;


create trigger
suseserver_mod_trig
before insert or update on suseServer
for each row
execute procedure suse_server_mod_trig_fun();


create or replace function suse_server_del_trig_fun() returns trigger
as
$$
begin
    insert into suseDelServer ( guid ) values ( old.guid );

    return old;
end;
$$
language plpgsql;

create trigger
suse_server_del_trig
before delete on suseServer
for each row
execute procedure suse_server_del_trig_fun();
