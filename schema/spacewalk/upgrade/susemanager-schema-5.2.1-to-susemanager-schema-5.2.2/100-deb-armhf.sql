insert into rhnCpuArch (id, label, name) select
sequence_nextval('rhn_cpu_arch_id_seq'), 'armhf', 'ARMHF' from dual
where not exists (select 1 from rhnCpuArch where label = 'armhf');


insert into rhnPackageArch (id, label, name, arch_type_id) select
sequence_nextval('rhn_package_arch_id_seq'), 'armhf-deb', 'ARMHF-deb', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnPackageArch where label = 'armhf-deb');


insert into rhnServerArch (id, label, name, arch_type_id) select
sequence_nextval('rhn_server_arch_id_seq'), 'armhf-debian-linux', 'ARMHF Debian', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnServerArch where label = 'armhf-debian-linux');


insert into rhnChannelArch (id, label, name, arch_type_id) select
sequence_nextval('rhn_channel_arch_id_seq'), 'channel-armhf-deb', 'ARMHF Debian', lookup_arch_type('deb') from dual
where not exists (select 1 from rhnChannelArch where label = 'channel-armhf-deb');


insert into rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id, created, modified) select
LOOKUP_PACKAGE_ARCH('armhf-deb'), LOOKUP_PACKAGE_ARCH('all-deb'), current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnPackageUpgradeArchCompat where package_arch_id = LOOKUP_PACKAGE_ARCH('armhf-deb') and package_upgrade_arch_id = LOOKUP_PACKAGE_ARCH('all-deb'));

insert into rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id, created, modified) select
LOOKUP_PACKAGE_ARCH('armhf-deb'), LOOKUP_PACKAGE_ARCH('armhf-deb'), current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnPackageUpgradeArchCompat where package_arch_id = LOOKUP_PACKAGE_ARCH('armhf-deb') and package_upgrade_arch_id = LOOKUP_PACKAGE_ARCH('armhf-deb'));

insert into rhnPackageUpgradeArchCompat (package_arch_id, package_upgrade_arch_id, created, modified) select
LOOKUP_PACKAGE_ARCH('all-deb'), LOOKUP_PACKAGE_ARCH('armhf-deb'), current_timestamp, current_timestamp from dual
where not exists (select 1 from rhnPackageUpgradeArchCompat where package_arch_id = LOOKUP_PACKAGE_ARCH('all-deb') and package_upgrade_arch_id = LOOKUP_PACKAGE_ARCH('armhf-deb'));


insert into rhnChannelPackageArchCompat (channel_arch_id, package_arch_id) select
LOOKUP_CHANNEL_ARCH('channel-armhf-deb'), LOOKUP_PACKAGE_ARCH('armhf-deb') from dual
where not exists (select 1 from rhnChannelPackageArchCompat where channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-armhf-deb') and package_arch_id = LOOKUP_PACKAGE_ARCH('armhf-deb'));

insert into rhnChannelPackageArchCompat (channel_arch_id, package_arch_id) select
LOOKUP_CHANNEL_ARCH('channel-armhf-deb'), LOOKUP_PACKAGE_ARCH('all-deb') from dual
where not exists (select 1 from rhnChannelPackageArchCompat where channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-armhf-deb') and package_arch_id = LOOKUP_PACKAGE_ARCH('all-deb'));

insert into rhnChannelPackageArchCompat (channel_arch_id, package_arch_id) select
LOOKUP_CHANNEL_ARCH('channel-armhf-deb'), LOOKUP_PACKAGE_ARCH('src-deb') from dual
where not exists (select 1 from rhnChannelPackageArchCompat where channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-armhf-deb') and package_arch_id = LOOKUP_PACKAGE_ARCH('src-deb'));


insert into rhnServerChannelArchCompat (server_arch_id, channel_arch_id) select
LOOKUP_SERVER_ARCH('armhf-debian-linux'), LOOKUP_CHANNEL_ARCH('channel-armhf-deb') from dual
where not exists (select 1 from rhnServerChannelArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('armhf-debian-linux') and channel_arch_id = LOOKUP_CHANNEL_ARCH('channel-armhf-deb'));


insert into rhnServerPackageArchCompat (server_arch_id, package_arch_id, preference) select
LOOKUP_SERVER_ARCH('armhf-debian-linux'), LOOKUP_PACKAGE_ARCH('armhf-deb'), 0 from dual
where not exists (select 1 from rhnServerPackageArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('armhf-debian-linux') and package_arch_id = LOOKUP_PACKAGE_ARCH('armhf-deb'));

insert into rhnServerPackageArchCompat (server_arch_id, package_arch_id, preference) select
LOOKUP_SERVER_ARCH('armhf-debian-linux'), LOOKUP_PACKAGE_ARCH('arm-deb'), 100 from dual
where not exists (select 1 from rhnServerPackageArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('armhf-debian-linux') and package_arch_id = LOOKUP_PACKAGE_ARCH('arm-deb'));

insert into rhnServerPackageArchCompat (server_arch_id, package_arch_id, preference) select
LOOKUP_SERVER_ARCH('armhf-debian-linux'), LOOKUP_PACKAGE_ARCH('all-deb'), 1000 from dual
where not exists (select 1 from rhnServerPackageArchCompat where server_arch_id = LOOKUP_SERVER_ARCH('armhf-debian-linux') and package_arch_id = LOOKUP_PACKAGE_ARCH('all-deb'));


insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('enterprise_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('enterprise_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('virtualization_host') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('virtualization_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('bootstrap_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('bootstrap_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('salt_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('salt_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('foreign_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('foreign_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('container_build_host') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('container_build_host'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('monitoring_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('monitoring_entitled'));

insert into rhnServerServerGroupArchCompat (server_arch_id, server_group_type) select
lookup_server_arch('armhf-debian-linux'), lookup_sg_type('proxy_entitled') from dual
where not exists (select 1 from rhnServerServerGroupArchCompat where server_arch_id = lookup_server_arch('armhf-debian-linux') and server_group_type = lookup_sg_type('proxy_entitled'));


insert into rhnChildChannelArchCompat (parent_arch_id, child_arch_id) select
LOOKUP_CHANNEL_ARCH('channel-armhf-deb'), LOOKUP_CHANNEL_ARCH('channel-armhf-deb') from dual
where not exists (select 1 from rhnChildChannelArchCompat where parent_arch_id = LOOKUP_CHANNEL_ARCH('channel-armhf-deb') and child_arch_id = LOOKUP_CHANNEL_ARCH('channel-armhf-deb'));

commit;
