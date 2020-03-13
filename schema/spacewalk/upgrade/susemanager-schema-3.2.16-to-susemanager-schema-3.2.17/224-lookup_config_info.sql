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

create or replace function
lookup_config_info(
    username_in     in varchar,
    groupname_in    in varchar,
    filemode_in     in numeric,
    selinux_ctx_in  in varchar,
    symlink_target_id in numeric
)
returns numeric
as
$$
declare
    config_info_id    numeric;
begin
    select id
      into config_info_id
      from rhnConfigInfo
      where (username = username_in or (username is null and username_in is null))
        and (groupname = groupname_in or (groupname is null and groupname_in is null))
        and (filemode = filemode_in or (filemode is null and filemode_in is null))
        and (selinux_ctx = selinux_ctx_in or (selinux_ctx is null and selinux_ctx_in is null))
        and (symlink_target_filename_id = symlink_target_id or (symlink_target_filename_id is null and symlink_target_id is null));

    if not found then
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_config_info(username_in, groupname_in, filemode_in, selinux_ctx_in, symlink_target_id);
    end if;

    return config_info_id;
end;
$$ language plpgsql immutable;
