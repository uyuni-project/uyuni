-- monitoring_entitled type ----------------------------------------------------

insert into rhnServerGroupType (id, label, name, permanent, is_base)
   select sequence_nextval('rhn_servergroup_type_seq'),
      'monitoring_entitled', 'Monitoring',
      'N', 'N'
   from dual where not exists (
       SELECT 1 FROM rhnServerGroupType WHERE
       label='monitoring_entitled'
   );

-- monitoring_entitled base compatibility

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
    select lookup_sg_type('salt_entitled'),
        lookup_sg_type('monitoring_entitled')
    from dual where not exists(
        select 1 from rhnSGTypeBaseAddonCompat where
        base_id=lookup_sg_type('salt_entitled') and
        addon_id=lookup_sg_type('monitoring_entitled')
    );

-- monitoring_entitled compatibilities

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    select lookup_server_arch('amd64-redhat-linux'),
            lookup_sg_type('monitoring_entitled')
    from dual where not exists (
        select 1 from rhnServerServerGroupArchCompat where
        server_arch_id=lookup_server_arch('amd64-redhat-linux') and
        server_group_type=lookup_sg_type('monitoring_entitled')
    );

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    select lookup_server_arch('x86_64-redhat-linux'),
            lookup_sg_type('monitoring_entitled')
    from dual where not exists (
        select 1 from rhnServerServerGroupArchCompat where
        server_arch_id=lookup_server_arch('x86_64-redhat-linux') and
        server_group_type=lookup_sg_type('monitoring_entitled')
    );

-- add monitoring entitlement to all orgs

insert into rhnServerGroup
        ( id, name, description, group_type, org_id )
        select nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, sgt.id, org.id
        from rhnServerGroupType sgt, web_customer org
        where sgt.label = 'monitoring_entitled' and org.id not in (
            select sg.org_id from rhnServerGroup sg where sg.name = 'Monitoring'
        );
