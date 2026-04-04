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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

set search_path to rhn_user, current;

create or replace function
check_role(user_id_in in numeric, role_in in varchar)
    returns numeric as $$
    declare
        throwaway numeric;
    begin
        -- the idea: if we get past this query, the org has the setting, else catch the exception and return 0
        if role_in = 'org_admin' or role_in = 'satellite_admin' then
            select 1 into throwaway
              from rhnUserGroupType UGT,
                   rhnUserGroup UG,
                   rhnUserGroupMembers UGM
             where UGM.user_id = user_id_in
               and UGM.user_group_id = UG.id
               and UG.group_type = UGT.id
               and UGT.label = role_in;
        else
            select 1 into throwaway
              from access.accessGroup ag
              join access.userAccessGroup uag on ag.id = uag.group_id
             where ag.label = role_in
               and uag.user_id = user_id_in;
        end if;

        if not found then
            return 0;
        end if;

	return 1;
    end;
$$ language plpgsql;
