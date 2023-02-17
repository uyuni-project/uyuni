
--
-- Copyright (c) 2008--2016 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
--
-- 
--

create or replace function rhn_virtinst_mod_trig_fun() returns trigger
as
$$
begin
        new.modified := current_timestamp;

	return new;
end;
$$
language plpgsql;


create trigger
rhn_virtinst_mod_trig
before insert or update on rhnVirtualInstance
for each row
execute procedure rhn_virtinst_mod_trig_fun();

create or replace function rhn_virtinst_iud_trig_fun() returns trigger
as
$$
begin
        if tg_op='INSERT' or tg_op='UPDATE' then
                if new.host_system_id is not null and new.virtual_system_id is not null then
                        update suseSCCRegCache
                           set scc_reg_required = 'Y'
                         where server_id = new.host_system_id;
                end if;
                return new;
        end if;
        if tg_op='DELETE' then
                if old.host_system_id is not null and old.virtual_system_id is not null then
                        update suseSCCRegCache
                           set scc_reg_required = 'Y'
                         where server_id = old.host_system_id;
                end if;
                return old;
        end if;
end;
$$ language plpgsql;


create trigger
rhn_virtinst_iud_trig
after insert or update or delete on rhnVirtualInstance
execute procedure rhn_virtinst_iud_trig_fun();
