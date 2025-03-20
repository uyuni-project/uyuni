-- Include org_id in the unique constraint
DROP INDEX IF EXISTS access.access_group_label_uq;
CREATE UNIQUE INDEX IF NOT EXISTS access_group_label_org_uq
ON access.accessGroup(label, org_id);

-- Create RBAC roles for all orgs
INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'activation_key_admin', 'Activation Key Administrator'
FROM web_customer
ON CONFLICT (org_id, label) DO NOTHING;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'image_admin', 'Image Administrator'
FROM web_customer
ON CONFLICT (org_id, label) DO NOTHING;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'config_admin', 'Configuration Administrator'
FROM web_customer
ON CONFLICT (org_id, label) DO NOTHING;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'channel_admin', 'Channel Administrator'
FROM web_customer
ON CONFLICT (org_id, label) DO NOTHING;

INSERT INTO access.accessGroup (org_id, label, description)
SELECT id, 'system_group_admin', 'System Group Administrator'
FROM web_customer
ON CONFLICT (org_id, label) DO NOTHING;

-- Automatically assign RBAC roles based on old roles.
WITH
    labels AS (
        VALUES ('activation_key_admin'), ('image_admin'), ('config_admin'), ('channel_admin'), ('system_group_admin')
    ),
    old_group_type AS (
        SELECT ugt.id, l.column1 AS label
        FROM rhnUserGroupType ugt, labels l
        WHERE ugt.label = l.column1
    ),
    old_role AS (
        SELECT ug.id, ug.org_id, ogt.label
        FROM rhnUserGroup ug
        JOIN old_group_type ogt ON ug.group_type = ogt.id
    ),
    old_role_members AS (
        SELECT ugm.user_id, old_r.org_id, old_r.label
        FROM rhnUserGroupMembers ugm
        JOIN old_role old_r ON ugm.user_group_id = old_r.id
    ),
    new_role AS (
        SELECT id, org_id, label FROM access.accessGroup
    )
    INSERT INTO access.userAccessGroup(user_id, group_id)
    SELECT old_rm.user_id, new_rl.id
    FROM old_role_members old_rm
    JOIN new_role new_rl
    ON old_rm.org_id = new_rl.org_id
    AND old_rm.label = new_rl.label
    ON CONFLICT (user_id, group_id) DO NOTHING;

-- Bypass old permissions by assigning them to every user.
WITH all_users AS (
    SELECT id AS user_id, org_id FROM web_contact
),
all_old_roles AS (
    SELECT ug.id AS user_group_id, ug.org_id, ugt.label
    FROM rhnUserGroup ug
    JOIN rhnUserGroupType ugt ON ug.group_type = ugt.id
    WHERE ugt.label IN ('activation_key_admin', 'image_admin', 'config_admin', 'channel_admin', 'system_group_admin')
)
INSERT INTO rhnUserGroupMembers(user_id, user_group_id, temporary)
SELECT u.user_id, r.user_group_id, 'N'
FROM all_users u
JOIN all_old_roles r ON u.org_id = r.org_id
ON CONFLICT (user_id, user_group_id, temporary) DO NOTHING;

--Replace create org function to also create RBAC roles
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

        insert into access.accessGroup (org_id, label, description)
        values (new_org_id, 'system_group_admin', 'System Group Administrator');

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

        insert into access.accessGroup (org_id, label, description)
        values (new_org_id, 'activation_key_admin', 'Activation Key Administrator');

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

        insert into access.accessGroup (org_id, label, description)
        values (new_org_id, 'channel_admin', 'Channel Administrator');

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

        insert into access.accessGroup (org_id, label, description)
        values (new_org_id, 'config_admin', 'Configuration Administrator');

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

        insert into access.accessGroup (org_id, label, description)
        values (new_org_id, 'image_admin', 'Image Administrator');

        select nextval('rhn_user_group_id_seq') into group_val from dual;

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

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'ansible_control_node';

        insert into rhnServerGroup
                ( id, name, description, group_type, org_id )
                select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name,
                        sgt.id, new_org_id
                from rhnServerGroupType sgt
                where sgt.label = 'peripheral_server';

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
