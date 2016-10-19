-- oracle equivalent source sha1 a5487d897e23034ec095f5db6b4c65e3a9b828f6
--
-- Copyright (c) 2016 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
create or replace function suse_prdext_mod_trig_fun() returns trigger
as
$$
begin
        new.modified := current_timestamp;

        return new;
end;
$$
language plpgsql;

drop trigger if exists prdext_mod_trig on suseProductExtension;

create trigger
prdext_mod_trig
before insert or update on suseProductExtension
for each row
execute procedure suse_prdext_mod_trig_fun();
