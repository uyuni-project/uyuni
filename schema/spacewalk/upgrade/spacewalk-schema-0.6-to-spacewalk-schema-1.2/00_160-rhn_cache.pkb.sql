--
-- Copyright (c) 2008 Red Hat, Inc.
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

create or replace package body
rhn_cache
is
	body_version varchar2(100) := '';

	-- this searches out all users who get perms...
	procedure update_perms_for_server(
		server_id_in in number
	) is
	begin
                -- delete rows which are no more valid
		delete from rhnUserServerPerms p
                      where server_id = server_id_in
                        and user_id not in (select user_id
                                              from rhnUserServerPermsDupes d
                                             where p.server_id = d.server_id);
                -- insert newly added rows
		insert into rhnUserServerPerms(user_id, server_id) (
			select distinct user_id, server_id_in
				   from	rhnUserServerPermsDupes d
                                  where server_id = server_id_in
                                    and user_id not in (
                                                select user_id
                                                  from rhnUserServerPerms p
                                                 where p.server_id = d.server_id)
			);
	end update_perms_for_server;

        -- update rhnUserServerPerms cache from rhnUserServerPermsDupes
	procedure update_perms_for_user(
		user_id_in in number
	) is
	begin
                -- first delete rows which are not in rhnUserServerPermsDupes
                delete from rhnUserServerPerms up
                 where user_id = user_id_in
                   and not exists (
                       select 1
                         from rhnUserServerPermsDupes uspd
                        where uspd.user_id = up.user_id
                          and uspd.server_id = up.server_id);

                -- then insert rest of rows from rhnUserServerPermsDupes
                insert into rhnUserServerPerms (user_id, server_id)
                       select distinct user_id_in, server_id
                         from rhnUserServerPermsDupes uspd
                        where uspd.user_id = user_id_in
                          and not exists (
                              select 1
                                from rhnUserServerPerms usp
                               where usp.user_id = user_id_in
                                 and usp.server_id = uspd.server_id);
	end update_perms_for_user;

	-- this means a server got added or removed, so we
	-- can't key off of a server anywhere.
	procedure update_perms_for_server_group(
		server_group_id_in in number
	) is
		cursor users is
			-- org admins aren't affected, so don't test for them
			select	usgp.user_id id
			from	rhnUserServerGroupPerms usgp
			where	usgp.server_group_id = server_group_id_in
				and not exists (
					select	1
					from	rhnUserGroup ug,
							rhnUserGroupMembers ugm,
							rhnServerGroup sg,
							rhnUserGroupType ugt
					where	ugt.label = 'org_admin'
						and ugt.id = ug.group_type
						and sg.id = server_group_id_in
						and ugm.user_id = usgp.user_id
						and ug.org_id = sg.org_id
						and ugm.user_group_id = ug.id
					);
	begin
		for u in users loop
			update_perms_for_user(u.id);
		end loop;
	end update_perms_for_server_group;
end rhn_cache;
/
show errors
