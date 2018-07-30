-- osimage_build_host type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'osimage_build_host', 'OS Image Build Host',
      'N', 'N'
   );

-- osimage_build_host* compatibilities --

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
   values (lookup_server_arch('amd64-redhat-linux'),
           lookup_sg_type('osimage_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type)
   values (lookup_server_arch('x86_64-redhat-linux'),
           lookup_sg_type('osimage_build_host'));

-- rhnSGTypeBaseAddonCompat

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('osimage_build_host'));
