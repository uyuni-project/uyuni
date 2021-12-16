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
create or replace function suse_srv_inst_prod_mod_trig_fun() returns trigger
as
$$
begin
            new.modified := current_timestamp;

            return new;
end;
$$
language plpgsql;


create trigger
susesrvinstprod_mod_trig
before insert or update on suseServerInstalledProduct
for each row
execute procedure suse_srv_inst_prod_mod_trig_fun();


create or replace function suse_srv_inst_prod_iud_trig_fun() returns trigger as
$$
begin
	if tg_op='INSERT' or tg_op='UPDATE' then
		update suseSCCRegCache
                   set scc_reg_required = 'Y'
                 where server_id = new.rhn_server_id;
                return new;
        end if;
        if tg_op='DELETE' then
                update suseSCCRegCache
                   set scc_reg_required = 'Y'
                 where server_id = old.rhn_server_id;
                return old;
        end if;
end;
$$ language plpgsql;

create trigger
susesrvinstprod_iud_trig
after insert or update or delete on suseServerInstalledProduct
for each row
execute procedure suse_srv_inst_prod_iud_trig_fun();
