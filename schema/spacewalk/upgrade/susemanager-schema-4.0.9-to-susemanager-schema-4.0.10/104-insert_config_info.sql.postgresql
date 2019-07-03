-- oracle equivalent source sha1 c0296a9f14cc334cc0fd07b3296bced60ba7fb4f

create or replace function
insert_config_info(
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
    config_info_id := nextval('rhn_confinfo_id_seq');

    insert into rhnConfigInfo (id, username, groupname, filemode, selinux_ctx, symlink_target_filename_id)
        values (config_info_id, username_in, groupname_in, filemode_in, selinux_ctx_in, symlink_target_id)
        on conflict do nothing;

    select id
      into config_info_id
      from rhnConfigInfo
      where (username = username_in or (username is null and username_in is null))
        and (groupname = groupname_in or (groupname is null and groupname_in is null))
        and (filemode = filemode_in or (filemode is null and filemode_in is null))
        and (selinux_ctx = selinux_ctx_in or (selinux_ctx is null and selinux_ctx_in is null))
        and (symlink_target_filename_id = symlink_target_id or (symlink_target_filename_id is null and symlink_target_id is null));

    return config_info_id;
end;
$$ language plpgsql;
