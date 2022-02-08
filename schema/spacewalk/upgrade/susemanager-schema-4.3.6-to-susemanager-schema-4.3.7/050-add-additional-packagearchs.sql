insert into rhnChannelArch (id, label, name, arch_type_id) select
  sequence_nextval('rhn_channel_arch_id_seq'), 'armel-deb', 'armel-deb', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnChannelArch where label = 'armel-deb');

insert into rhnChannelArch (id, label, name, arch_type_id) select
  sequence_nextval('rhn_channel_arch_id_seq'), 'riscv64-deb', 'riscv64-deb', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnChannelArch where label = 'riscv64-deb');

insert into rhnChannelArch (id, label, name, arch_type_id) select
  sequence_nextval('rhn_channel_arch_id_seq'), 'ppc64el-deb', 'ppc64el-deb', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnChannelArch where label = 'ppc64el-deb');

insert into rhnChannelArch (id, label, name, arch_type_id) select
  sequence_nextval('rhn_channel_arch_id_seq'), 's390x-deb', 's390x-deb', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnChannelArch where label = 's390x-deb');
