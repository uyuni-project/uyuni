-- oracle equivalent source sha1 1fc38615062350d2c3effd39f2563856ee68f5df
--
-- Copyright (c) 2011-2012 SUSE Linux Products GmbH
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
create or replace function suse_upgpath_mod_trig_fun() returns trigger
as
$$
begin
        new.modified := current_timestamp;

        return new;
end;
$$
language plpgsql;


create trigger
suseupgpath_mod_trig
before insert or update on suseUpgradePath
for each row
execute procedure suse_upgpath_mod_trig_fun();
