-- oracle equivalent source sha1 7c1e11869feea6172a6a4d76fc47633a62f649b8
-- retrieved from ./1240273396/cea26e10fb65409287d4579c2409403b45e5e838/schema/spacewalk/oracle/triggers/susePackageProductFile.sql
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
-- triggers for susePackageProductFile



create or replace function suse_pack_product_file_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;



create trigger
suse_pack_product_file_mod_trig
before insert or update on susePackageProductFile
for each row
execute procedure suse_pack_product_file_mod_trig_fun();
