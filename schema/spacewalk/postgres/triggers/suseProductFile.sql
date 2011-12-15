-- oracle equivalent source sha1 4834dfd6bcb83eecd485d413326c05cb610ea2fb
-- retrieved from ./1240273396/cea26e10fb65409287d4579c2409403b45e5e838/schema/spacewalk/oracle/triggers/suseProductFile.sql
--
-- Copyright (c) 2011 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- triggers for suseProductFile



create or replace function suse_product_file_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;



create trigger
suse_product_file_mod_trig
before insert or update on suseProductFile
for each row
execute procedure suse_product_file_mod_trig_fun();
