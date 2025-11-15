--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

create or replace function suse_server_sap_workload_up_trig_fun() returns trigger as
$$
begin
        if (TG_OP = 'DELETE') then
            update suseSCCRegCache
            set scc_reg_required = 'Y'
            where server_id = old.server_id;
            return null;
        else
            update suseSCCRegCache
            set scc_reg_required = 'Y'
            where server_id = new.server_id;
            return new;
        end if;
end;
$$ language plpgsql;

drop trigger if exists suse_server_sap_workload_up_trig on suseServerSAPWorkload;

create trigger
suse_server_sap_workload_up_trig
after insert or update or delete on suseServerSAPWorkload
for each row
execute procedure suse_server_sap_workload_up_trig_fun();
