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

create or replace function suse_minion_info_up_trig_fun() returns trigger as
$$
begin
        update suseSCCRegCache
           set scc_reg_required = 'Y'
         where server_id = new.server_id;
	return new;
end;
$$ language plpgsql;

drop trigger if exists suse_minion_info_up_trig on suseMinionInfo;

create trigger
suse_minion_info_up_trig
after update on suseMinionInfo
for each row
when (OLD.container_runtime is distinct from NEW.container_runtime OR OLD.uname is distinct from NEW.uname)
execute procedure suse_minion_info_up_trig_fun();
