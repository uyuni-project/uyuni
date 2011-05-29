-- oracle equivalent source sha1 61269cfb6c211c84a44a2d193aaa581c147ac9bb
-- retrieved from ./1290790062/5e78063ef0ba88a3e95b0f77fbd8d842353665a0/schema/spacewalk/oracle/triggers/suseProductChannel.sql
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
create or replace function suse_product_channel_mod_trig_fun() returns trigger
as
$$
begin
            new.modified := current_timestamp;

            return new;
end;
$$
language plpgsql;


create trigger
suseproductchannel_mod_trig
before insert or update on suseProductChannel
for each row
execute procedure suse_product_channel_mod_trig_fun();
