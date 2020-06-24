insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
    select lookup_server_arch('s390x-redhat-linux'),
            lookup_sg_type('monitoring_entitled')
    from dual where not exists (
        select 1 from rhnServerServerGroupArchCompat where
        server_arch_id=lookup_server_arch('s390x-redhat-linux') and
        server_group_type=lookup_sg_type('monitoring_entitled')
    );

