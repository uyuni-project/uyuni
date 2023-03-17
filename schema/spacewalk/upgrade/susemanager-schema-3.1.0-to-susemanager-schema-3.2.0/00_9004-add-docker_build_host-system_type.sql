-- container_build_host type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'container_build_host', 'Container Build Host',
      'N', 'N'
   );


-- container_build_host* compatibilities --

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('i386-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('i386-debian-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('i486-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('i586-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('i686-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('athlon-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('amd64-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('amd64-debian-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('ia64-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('ia64-debian-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('ia32e-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('s390-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('s390-debian-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('s390x-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('ppc-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('powerpc-debian-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('ppc64-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('ppc64le-redhat-linux'),
            lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
    values (lookup_server_arch('x86_64-redhat-linux'),
            lookup_sg_type('container_build_host'));

-- rhnSGTypeBaseAddonCompat

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('container_build_host'));
