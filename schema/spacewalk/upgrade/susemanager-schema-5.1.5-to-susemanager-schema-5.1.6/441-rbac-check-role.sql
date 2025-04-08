set search_path to rhn_user, current;

create or replace function
check_role(user_id_in in numeric, role_in in varchar)
    returns numeric as $$
    declare
    	throwaway numeric;
    begin
    	-- the idea: if we get past this query, the org has the setting, else catch the exception and return 0
        if role_in = 'org_admin' or role_in = 'sat_admin' then
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
