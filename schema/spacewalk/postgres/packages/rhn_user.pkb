-- oracle equivalent source sha1 98b987747d62806810341a5c56981bf16069c68e
--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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

-- create schema rhn_user;

-- setup search_path so that these functions are created in appropriate schema.
update pg_settings set setting = 'rhn_user,' || setting where name = 'search_path';

create or replace function
check_role(user_id_in in numeric, role_in in varchar)
    returns numeric as $$
    declare
    	throwaway numeric;
    begin
    	-- the idea: if we get past this query, the org has the setting, else catch the exception and return 0
	select 1 into throwaway
	  from rhnUserGroupType UGT,
	       rhnUserGroup UG,
	       rhnUserGroupMembers UGM
	 where UGM.user_id = user_id_in
	   and UGM.user_group_id = UG.id
	   and UG.group_type = UGT.id
	   and UGT.label = role_in;

        if not found then
            return 0;
        end if;

	return 1;
    end;
$$ language plpgsql;

create or replace
    function check_role_implied(user_id_in in numeric, role_in in varchar)
    returns numeric as $$
    declare
    	throwaway numeric;
    begin
    	-- if the user directly has the role, they win
    	if rhn_user.check_role(user_id_in, role_in) = 1
	then
	    return 1;
    	end if;

	-- config_admin and channel_admin are automatically implied for org admins	
	if role_in = 'config_admin' and rhn_user.check_role(user_id_in, 'org_admin') = 1
	then
	    return 1;
	end if;

	if role_in = 'channel_admin' and rhn_user.check_role(user_id_in, 'org_admin') = 1
	then
	    return 1;
	end if;

	if role_in = 'image_admin' and rhn_user.check_role(user_id_in, 'org_admin') = 1
	then
	    return 1;
	end if;

	return 0;
    end;
$$ language plpgsql;

create or replace
    function get_org_id(user_id_in in numeric)
    returns numeric as $$
    declare
    	org_id_out numeric;
    begin
    	select org_id into org_id_out
	  from web_contact
	 where id = user_id_in;
	 
	return org_id_out;
    end;
$$ language plpgsql;

create or replace
	function add_servergroup_perm(
		user_id_in in numeric,
		server_group_id_in in numeric
	) returns void as $$
        declare
            okay record;
	begin
		for okay in
			select	1
			from	rhnServerGroup sg,
					web_contact u
			where	u.id = user_id_in
				and sg.id = server_group_id_in
				and sg.org_id = u.org_id
                loop
		insert into rhnUserServerGroupPerms(user_id, server_group_id)
				values (user_id_in, server_group_id_in);
			perform rhn_cache.update_perms_for_user(user_id_in);
			return;
		end loop;
		perform rhn_exception.raise_exception('usgp_different_orgs');
	exception when UNIQUE_VIOLATION then
		perform rhn_exception.raise_exception('usgp_already_allowed');
	end;
$$ language plpgsql;

create or replace
	function add_to_usergroup(
		user_id_in in numeric,
		user_group_id_in in numeric
	) returns void as $$
        declare
            ugr record;
	begin
		insert into rhnUserGroupMembers(user_id, user_group_id)
			values (user_id_in, user_group_id_in);

		for ugr in
			select	user_group_id_in
			from	rhnUserGroup		ug,
					rhnUserGroupType	ugt
			where	ugt.label in ('org_admin') -- and server_group_admin ?
				and ugr.id = user_group_id_in
				and ugr.group_type = ugt.id
                 loop
			perform rhn_cache.update_perms_for_user(user_id_in);
			return;
		end loop;
	end;
$$ language plpgsql;

create or replace
	function remove_from_usergroup(
		user_id_in in numeric,
		user_group_id_in in numeric
	) returns void as $$
        declare
            ugr record;
	begin
		-- we only do anything if you're really in the group, because
		-- testing is significantly cheaper than rebuilding the user's
		-- cache for no reason.
		for ugr in
			select	label
			from	rhnUserGroupType	ugt,
					rhnUserGroupMembers	ugm,
					rhnUserGroup		ug
			where	1=1
				and ug.id = user_group_id_in
				and ugm.user_group_id = user_group_id_in
				and ug.group_type = ugt.id
				and ugm.user_id = user_id_in
                 loop
			delete from rhnUserGroupMembers
				where	user_id = user_id_in
					and user_group_id = user_group_id_in;
			if ugr.label in ('org_admin') then
				perform rhn_cache.update_perms_for_user(user_id_in);
			end if;
		end loop;
	end;
$$ language plpgsql;

create or replace
        function role_names (user_id_in in numeric)
	returns varchar
	as
$$
	declare
		rec record;
		tmp varchar(4000);
	begin
		for rec in (
			select type_name
			from rhnUserTypeBase
			where user_id = user_id_in
			order by type_id
			) loop
			if tmp is null then
				tmp := rec.type_name;
			else
				tmp := tmp || ', ' || rec.type_name;
			end if;
		end loop;
		return tmp;
	end;
$$ language plpgsql;

-- restore the original setting
update pg_settings set setting = overlay( setting placing '' from 1 for (length('rhn_user')+1) ) where name = 'search_path';
