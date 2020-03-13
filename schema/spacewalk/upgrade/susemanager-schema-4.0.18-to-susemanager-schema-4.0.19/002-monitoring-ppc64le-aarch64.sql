insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
    select lookup_server_arch('ppc64le-redhat-linux'),
            lookup_sg_type('monitoring_entitled')
    from dual where not exists (
        select 1 from rhnServerServerGroupArchCompat where
        server_arch_id=lookup_server_arch('ppc64le-redhat-linux') and
        server_group_type=lookup_sg_type('monitoring_entitled')
    );

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
    select lookup_server_arch('aarch64-redhat-linux'),
            lookup_sg_type('monitoring_entitled')
    from dual where not exists (
        select 1 from rhnServerServerGroupArchCompat where
        server_arch_id=lookup_server_arch('aarch64-redhat-linux') and
        server_group_type=lookup_sg_type('monitoring_entitled')
    );
