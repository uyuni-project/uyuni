-- oracle equivalent source sha1 ea4b38d3d3018218bc07876322f24e341cc7a1c3
--
-- Copyright (c) 2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
create or replace function susemddata_mod_trig_fun() returns trigger
as
$$
begin
            new.modified := current_timestamp;

            return new;
end;
$$
language plpgsql;


create trigger
susemddata_mod_trig
before insert or update on suseMdData
for each row
execute procedure susemddata_mod_trig_fun();

