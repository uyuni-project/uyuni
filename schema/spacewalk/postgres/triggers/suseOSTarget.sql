-- oracle equivalent source sha1 63dff50795429016ee5e0802767f57d767a6eee7
-- retrieved from ./1294850094/1f572c8f1b2e19fe5c0e57447a2aaf1571bda2a7/schema/spacewalk/oracle/triggers/suseOSTarget.sql
--
-- Copyright (c) 2011 Novell
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
create or replace function suse_ostarget_mod_trig_fun() returns trigger
as
$$
begin
            new.modified := current_timestamp;

            return new;
end;
$$
language plpgsql;

create trigger
suseostarget_mod_trig
before insert or update on suseOSTarget
for each row
execute procedure suse_ostarget_mod_trig_fun();
