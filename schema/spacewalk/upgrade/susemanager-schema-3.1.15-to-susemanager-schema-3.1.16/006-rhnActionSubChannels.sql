-- oracle equivalent source sha1 bbe801570c36b11d613162bc18c76c9f86de0971

--
-- Copyright (c) 2018 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

create or replace function rhn_actionsbscrch_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_actionsbscrch_mod_trig
before insert or update on rhnActionSubChannels
for each row
execute procedure rhn_actionsbscrch_mod_trig_fun();

