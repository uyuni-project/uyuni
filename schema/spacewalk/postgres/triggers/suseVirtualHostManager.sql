-- oracle equivalent source sha1 26aeb29154dc42698d3dcb4ef14552178c209468
--
-- Copyright (c) 2015 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- triggers for suseVirtualHostManager

create or replace function suse_vhms_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
suse_vhms_mod_trig
before insert or update on suseVirtualHostManager
for each row
execute procedure suse_vhms_mod_trig_fun();
