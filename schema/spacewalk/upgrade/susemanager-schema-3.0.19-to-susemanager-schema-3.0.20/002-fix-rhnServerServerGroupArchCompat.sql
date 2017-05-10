insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alphaev6-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alphaev6-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('arm-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('arm-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv7l-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv7l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('athlon-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('athlon-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-i86pc-solaris'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-i86pc-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i486-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i486-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i586-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i586-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i686-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i686-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia32e-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia32e-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('iSeries-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('iSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('mips-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('mips-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('powerpc-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('powerpc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64le-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64le-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('pSeries-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('pSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390x-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390x-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc64-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-debian-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4m-solaris'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4m-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4u-solaris'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4u-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4v-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparcv9-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparcv9-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('x86_64-redhat-linux'), LOOKUP_SG_TYPE('bootstrap_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('x86_64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('aarch64-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('aarch64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alphaev6-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alphaev6-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('arm-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('arm-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6hl-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6hl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv7l-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv7l-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv7l-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv7l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('athlon-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('athlon-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-i86pc-solaris'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-i86pc-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i486-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i486-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i586-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i586-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i686-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i686-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia32e-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia32e-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('iSeries-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('iSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('mips-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('mips-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('powerpc-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('powerpc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64le-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64le-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('pSeries-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('pSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390x-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390x-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc64-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-debian-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4m-solaris'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4m-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4u-solaris'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4u-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4v-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparcv9-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparcv9-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('x86_64-redhat-linux'), LOOKUP_SG_TYPE('enterprise_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('x86_64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('aarch64-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('aarch64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alphaev6-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alphaev6-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('arm-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('arm-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6hl-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6hl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv7l-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv7l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('athlon-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('athlon-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-i86pc-solaris'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-i86pc-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i486-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i486-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i586-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i586-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i686-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i686-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia32e-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia32e-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('iSeries-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('iSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('mips-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('mips-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('powerpc-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('powerpc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64le-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64le-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('pSeries-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('pSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390x-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390x-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc64-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-debian-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4m-solaris'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4m-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4u-solaris'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4u-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4v-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparcv9-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparcv9-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('x86_64-redhat-linux'), LOOKUP_SG_TYPE('foreign_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('x86_64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('aarch64-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('aarch64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alphaev6-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alphaev6-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('alpha-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('alpha-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('arm-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('arm-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6hl-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6hl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv7l-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv7l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('athlon-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('athlon-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-i86pc-solaris'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-i86pc-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i486-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i486-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i586-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i586-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i686-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i686-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia32e-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia32e-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('iSeries-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('iSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('mips-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('mips-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('powerpc-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('powerpc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64iseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64le-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64le-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64pseries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('pSeries-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('pSeries-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390x-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390x-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc64-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-debian-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4m-solaris'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4m-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4u-solaris'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4u-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparc-sun4v-solaris'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparc-sun4v-solaris')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('sparcv9-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('sparcv9-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('x86_64-redhat-linux'), LOOKUP_SG_TYPE('salt_entitled') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('x86_64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('aarch64-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('aarch64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-debian-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('amd64-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('amd64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv5tejl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6hl-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6hl-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv6l-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv6l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('armv7l-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('armv7l-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('athlon-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('athlon-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-debian-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i386-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i386-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i486-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i486-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i586-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i586-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('i686-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('i686-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia32e-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia32e-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-debian-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ia64-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ia64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('powerpc-debian-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('powerpc-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64le-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64le-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc64-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('ppc-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('ppc-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-debian-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-debian-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('s390x-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('s390x-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
  select LOOKUP_SERVER_ARCH('x86_64-redhat-linux'), LOOKUP_SG_TYPE('virtualization_host') from dual
   where not exists (select 1 from rhnServerServerGroupArchCompat
                      where server_arch_id = LOOKUP_SERVER_ARCH('x86_64-redhat-linux')
                        and server_group_type = LOOKUP_SG_TYPE('virtualization_host'));

