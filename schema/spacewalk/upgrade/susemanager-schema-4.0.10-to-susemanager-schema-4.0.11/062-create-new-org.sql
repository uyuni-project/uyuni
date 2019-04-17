create or replace function create_new_org
(
        name_in      in varchar,
        password_in  in varchar
        --org_id_out   out number
) returns numeric
as
$$
declare
        ug_type                 numeric;
        group_val               numeric;
        new_org_id              numeric;
        org_id_out		numeric;
begin

        select nextval('web_customer_id_seq') into new_org_id from dual;

        insert into web_customer (
                id, name
        ) values (
                new_org_id, name_in
        );

        select nextval('rhn_user_group_id_seq') into group_val from dual;

        select  id
        into    ug_type
        from    rhnUserGroupType
        where   label = 'org_admin';

        insert into rhnUserGroup (
                id, name,
                description,
                max_members, group_type, org_id
        ) values (
                group_val, 'Organization Administrators',
                'Organization Administrators for Org ' || name_in,
                NULL, ug_type, new_org_id
        );

        select nextval('rhn_user_group_id_seq') into group_val from dual;

        select  id
        into    ug_type
        from    rhnUserGroupType
        where   label = 'system_group_admin';

        insert into rhnUserGroup (
                id, name,
                description,
                max_members, group_type, org_id
        ) values (
                group_val, 'System Group Administrators',
                'System Group Administrators for Org ' || name_in,
                NULL, ug_type, new_org_id
        );


        select nextval('rhn_user_group_id_seq') into group_val from dual;

        select  id
        into    ug_type
        from    rhnUserGroupType
        where   label = 'activation_key_admin';

        insert into rhnUserGroup (
                id, name,
                description,
                max_members, group_type, org_id
        ) values (
                group_val, 'Activation Key Administrators',
                'Activation Key Administrators for Org ' || name_in,
                NULL, ug_type, new_org_id
        );

        select nextval('rhn_user_group_id_seq') into group_val from dual;

        select  id
        into    ug_type
        from    rhnUserGroupType
        where   label = 'channel_admin';

        insert into rhnUserGroup (
                id, name,
                description,
                max_members, group_type, org_id
        ) values (
                group_val, 'Channel Administrators',
                'Channel Administrators for Org ' || name_in,
                NULL, ug_type, new_org_id
        );

	if new_org_id = 1 then
            select nextval('rhn_user_group_id_seq') into group_val from dual;

            select  id
            into    ug_type
            from    rhnUserGroupType
            where   label = 'satellite_admin';

            insert into rhnUserGroup (
                    id, name,
                    description,
                    max_members, group_type, org_id
            ) values (
                    group_val, 'SUSE Manager Administrators',
                    'SUSE Manager Administrators for Org ' || name_in,
                    NULL, ug_type, new_org_id
            );
        end if;

        select nextval('rhn_user_group_id_seq') into group_val from dual;

        select  id
        into    ug_type
        from    rhnUserGroupType
        where   label = 'config_admin';

        insert into rhnUserGroup (
                id, name,
                description,
                max_members, group_type, org_id
        ) values (
                group_val, 'Configuration Administrators',
                'Configuration Administrators for Org ' || name_in,
                NULL, ug_type, new_org_id
        );

        select nextval('rhn_user_group_id_seq') into group_val from dual;

        select  id
        into    ug_type
        from    rhnUserGroupType
        where   label = 'image_admin';

        insert into rhnUserGroup (
                id, name,
                description,
                max_members, group_type, org_id
        ) values (
                group_val, 'Image Administrators',
                'Image Administrators for Org ' || name_in,
                NULL, ug_type, new_org_id
        );

        -- there aren't any users yet, so we don't need to update
        -- rhnUserServerPerms

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'bootstrap_entitled';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'enterprise_entitled';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'salt_entitled';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'foreign_entitled';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'virtualization_host';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'container_build_host';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'osimage_build_host';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'monitoring_entitled';

        insert into suseImageStore (id, label, uri, store_type_id, org_id)
        values (
            nextval('suse_imgstore_id_seq'),
            'SUSE Manager OS Image Store',
            new_org_id || '/',
            (SELECT id FROM suseImageStoreType WHERE label = 'os_image'),
            new_org_id
        );

        org_id_out := new_org_id;

	-- Returning the value of OUT parameter
        return org_id_out;

end;
$$
language plpgsql;