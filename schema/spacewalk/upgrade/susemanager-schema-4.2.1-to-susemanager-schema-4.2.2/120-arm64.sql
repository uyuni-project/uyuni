insert into rhnCpuArch (id, label, name) select
sequence_nextval('rhn_cpu_arch_id_seq'), 'arm64', 'ARM64' from dual
where not exists (select 1 from rhnCpuArch where label = 'arm64');


insert into rhnPackageArch (id, label, name, arch_type_id) select
sequence_nextval('rhn_package_arch_id_seq'), 'arm64-deb', 'ARM64-deb', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnPackageArch where label = 'arm64-deb');


insert into rhnServerArch (id, label, name, arch_type_id) select
sequence_nextval('rhn_server_arch_id_seq'), 'arm64-debian-linux', 'ARM64 Debian', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnServerArch where label = 'arm64-debian-linux');


insert into rhnChannelArch (id, label, name, arch_type_id) select
sequence_nextval('rhn_channel_arch_id_seq'), 'channel-arm64-deb', 'ARM64 Debian', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnChannelArch where label = 'channel-arm64-deb');


insert into rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id, created, modified) select 
LOOKUP_PACKAGE_ARCH('arm64-deb'), LOOKUP_PACKAGE_ARCH('all-deb'), current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnPackageUpgradeArchCompat where package_arch_id = LOOKUP_PACKAGE_ARCH('arm64-deb') and package_upgrade_arch_id = LOOKUP_PACKAGE_ARCH('all-deb'));

insert into rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id, created, modified) select 
LOOKUP_PACKAGE_ARCH('arm64-deb'), LOOKUP_PACKAGE_ARCH('arm64-deb'), current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnPackageUpgradeArchCompat where package_arch_id = LOOKUP_PACKAGE_ARCH('arm64-deb') and package_upgrade_arch_id = LOOKUP_PACKAGE_ARCH('arm64-deb'));

insert into rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id, created, modified) select 
LOOKUP_PACKAGE_ARCH('all-deb'), LOOKUP_PACKAGE_ARCH('arm64-deb'), current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnPackageUpgradeArchCompat where package_arch_id = LOOKUP_PACKAGE_ARCH('all-deb') and package_upgrade_arch_id = LOOKUP_PACKAGE_ARCH('arm64-deb'));


insert into rhnChannelPackageArchCompat (channel_arch_id, package_arch_id) select 
LOOKUP_CHANNEL_ARCH('channel-arm64-deb'), LOOKUP_PACKAGE_ARCH('arm64-deb') from dual
where not exists (select 1 from rhnChannelPackageArchCompat where channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-arm64-deb') and package_arch_id = LOOKUP_PACKAGE_ARCH('arm64-deb'));

insert into rhnChannelPackageArchCompat (channel_arch_id, package_arch_id) select 
LOOKUP_CHANNEL_ARCH('channel-arm64-deb'), LOOKUP_PACKAGE_ARCH('all-deb') from dual
where not exists (select 1 from rhnChannelPackageArchCompat where channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-arm64-deb') and package_arch_id = LOOKUP_PACKAGE_ARCH('all-deb'));

insert into rhnChannelPackageArchCompat (channel_arch_id, package_arch_id) select 
LOOKUP_CHANNEL_ARCH('channel-arm64-deb'), LOOKUP_PACKAGE_ARCH('src-deb') from dual
where not exists (select 1 from rhnChannelPackageArchCompat where channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-arm64-deb') and package_arch_id = LOOKUP_PACKAGE_ARCH('src-deb'));


insert into rhnServerChannelArchCompat (server_arch_id, channel_arch_id) select
LOOKUP_SERVER_ARCH('arm64-debian-linux'), LOOKUP_CHANNEL_ARCH('channel-arm64-deb') from dual
where not exists (select 1 from rhnServerChannelArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('arm64-debian-linux') and channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-arm64-deb'));


insert into rhnServerPackageArchCompat (server_arch_id, package_arch_id, preference) select
LOOKUP_SERVER_ARCH('arm64-debian-linux'), LOOKUP_PACKAGE_ARCH('arm64-deb'), 0 from dual
where not exists (select 1 from rhnServerPackageArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('arm64-debian-linux') and package_arch_id = LOOKUP_PACKAGE_ARCH('arm64-deb'));

insert into rhnServerPackageArchCompat (server_arch_id, package_arch_id, preference) select
LOOKUP_SERVER_ARCH('arm64-debian-linux'), LOOKUP_PACKAGE_ARCH('all-deb'), 1000 from dual
where not exists (select 1 from rhnServerPackageArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('arm64-debian-linux') and package_arch_id = LOOKUP_PACKAGE_ARCH('all-deb'));



insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('enterprise_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('virtualization_host') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('virtualization_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('bootstrap_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('salt_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('salt_entitled'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('foreign_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('foreign_entitled'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('container_build_host') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat ( server_arch_id, server_group_type) select 
lookup_server_arch('arm64-debian-linux'), lookup_sg_type('monitoring_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('arm64-debian-linux') and server_group_type = lookup_sg_type('monitoring_entitled'));


insert into rhnChildChannelArchCompat (parent_arch_id, child_arch_id) select
LOOKUP_CHANNEL_ARCH('channel-arm64-deb'), LOOKUP_CHANNEL_ARCH('channel-arm64-deb') from dual
where not exists (select 1 from rhnChildChannelArchCompat where parent_arch_id = LOOKUP_CHANNEL_ARCH('channel-arm64-deb') and child_arch_id = LOOKUP_CHANNEL_ARCH('channel-arm64-deb'));

commit;

