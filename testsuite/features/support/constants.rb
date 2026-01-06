# Copyright (c) 2019-2025 SUSE LLC
# Licensed under the terms of the MIT license.

ENV_VAR_BY_HOST = {
  'localhost' => 'HOSTNAME',
  'proxy' => 'PROXY',
  'server' => 'SERVER',
  'sle_minion' => 'MINION',
  'ssh_minion' => 'SSH_MINION',
  'rhlike_minion' => 'RHLIKE_MINION',
  'deblike_minion' => 'DEBLIKE_MINION',
  'build_host' => 'BUILD_HOST',
  # Build Validation environment
  'sle12sp5_minion' => 'SLE12SP5_MINION',
  'sle12sp5_ssh_minion' => 'SLE12SP5_SSHMINION',
  'sle15sp3_minion' => 'SLE15SP3_MINION',
  'sle15sp3_ssh_minion' => 'SLE15SP3_SSHMINION',
  'sle15sp4_minion' => 'SLE15SP4_MINION',
  'sle15sp4_ssh_minion' => 'SLE15SP4_SSHMINION',
  'monitoring_server' => 'MONITORING_SERVER',
  'sle15sp5_minion' => 'SLE15SP5_MINION',
  'sle15sp5_ssh_minion' => 'SLE15SP5_SSHMINION',
  'sle15sp6_minion' => 'SLE15SP6_MINION',
  'sle15sp6_ssh_minion' => 'SLE15SP6_SSHMINION',
  'sle15sp6_buildhost' => 'SLE15SP6_BUILDHOST',
  'sle15sp7_minion' => 'SLE15SP7_MINION',
  'sle15sp7_ssh_minion' => 'SLE15SP7_SSHMINION',
  'sle15sp7_buildhost' => 'SLE15SP7_BUILDHOST',
  'slemicro51_minion' => 'SLEMICRO51_MINION',
  'slemicro51_ssh_minion' => 'SLEMICRO51_SSHMINION',
  'slemicro52_minion' => 'SLEMICRO52_MINION',
  'slemicro52_ssh_minion' => 'SLEMICRO52_SSHMINION',
  'slemicro53_minion' => 'SLEMICRO53_MINION',
  'slemicro53_ssh_minion' => 'SLEMICRO53_SSHMINION',
  'slemicro54_minion' => 'SLEMICRO54_MINION',
  'slemicro54_ssh_minion' => 'SLEMICRO54_SSHMINION',
  'slemicro55_minion' => 'SLEMICRO55_MINION',
  'slemicro55_ssh_minion' => 'SLEMICRO55_SSHMINION',
  'slmicro60_minion' => 'SLMICRO60_MINION',
  'slmicro60_ssh_minion' => 'SLMICRO60_SSHMINION',
  'slmicro61_minion' => 'SLMICRO61_MINION',
  'slmicro61_ssh_minion' => 'SLMICRO61_SSHMINION',
  'alma8_minion' => 'ALMA8_MINION',
  'alma8_ssh_minion' => 'ALMA8_SSHMINION',
  'alma9_minion' => 'ALMA9_MINION',
  'alma9_ssh_minion' => 'ALMA9_SSHMINION',
  'amazon2023_minion' => 'AMAZON2023_MINION',
  'amazon2023_ssh_minion' => 'AMAZON2023_SSHMINION',
  'centos7_minion' => 'CENTOS7_MINION',
  'centos7_ssh_minion' => 'CENTOS7_SSHMINION',
  'liberty9_minion' => 'LIBERTY9_MINION',
  'liberty9_ssh_minion' => 'LIBERTY9_SSHMINION',
  'oracle9_minion' => 'ORACLE9_MINION',
  'oracle9_ssh_minion' => 'ORACLE9_SSHMINION',
  'rhel9_minion' => 'RHEL9_MINION',
  'rhel9_ssh_minion' => 'RHEL9_SSHMINION',
  'rocky8_minion' => 'ROCKY8_MINION',
  'rocky8_ssh_minion' => 'ROCKY8_SSHMINION',
  'rocky9_minion' => 'ROCKY9_MINION',
  'rocky9_ssh_minion' => 'ROCKY9_SSHMINION',
  'ubuntu2204_minion' => 'UBUNTU2204_MINION',
  'ubuntu2204_ssh_minion' => 'UBUNTU2204_SSHMINION',
  'ubuntu2404_minion' => 'UBUNTU2404_MINION',
  'ubuntu2404_ssh_minion' => 'UBUNTU2404_SSHMINION',
  'debian12_minion' => 'DEBIAN12_MINION',
  'debian12_ssh_minion' => 'DEBIAN12_SSHMINION',
  'opensuse156arm_minion' => 'OPENSUSE156ARM_MINION',
  'opensuse156arm_ssh_minion' => 'OPENSUSE156ARM_SSHMINION',
  'sle15sp5s390_minion' => 'SLE15SP5S390_MINION',
  'sle15sp5s390_ssh_minion' => 'SLE15SP5S390_SSHMINION',
  'salt_migration_minion' => 'SALT_MIGRATION_MINION'
}.freeze

# TODO: the values for pxeboot_minion, sle15sp6_terminal, sle15sp7_terminal and proxy can now be set in sumaform
#       remove them from this array when we read them from .bashrc
PRIVATE_ADDRESSES = {
  'network'           => '0',
  'pxeboot_minion'    => '4',
  'sle15sp6_terminal' => '6',
  'sle15sp7_terminal' => '7',
  'dhcp_dns'          => '53',
  'range begin'       => '128',
  'range end'         => '253',
  'proxy'             => '254',
  'broadcast'         => '255'
}.freeze

FIELD_IDS = {
  'NIC'                             => 'branch_network#nic',
  'IP'                              => 'branch_network#ip',
  'virtual network mode'            => 'default_net#mode',
  'domain name server'              => 'dhcpd#domain_name_servers#0',
  'network IP'                      => 'dhcpd#subnets#0#$key',
  'dynamic IP range begin'          => 'dhcpd#subnets#0#range#0',
  'dynamic IP range end'            => 'dhcpd#subnets#0#range#1',
  'broadcast address'               => 'dhcpd#subnets#0#broadcast_address',
  'routers'                         => 'dhcpd#subnets#0#routers#0',
  'next server'                     => 'dhcpd#subnets#0#next_server',
  'network mask'                    => 'dhcpd#subnets#0#netmask',
  'filename'                        => 'dhcpd#subnets#0#filename',
  'first reserved IP'               => 'dhcpd#hosts#0#fixed_address',
  'second reserved IP'              => 'dhcpd#hosts#1#fixed_address',
  'first reserved hostname'         => 'dhcpd#hosts#0#$key',
  'second reserved hostname'        => 'dhcpd#hosts#1#$key',
  'first reserved MAC'              => 'dhcpd#hosts#0#hardware',
  'second reserved MAC'             => 'dhcpd#hosts#1#hardware',
  'domain name'                     => 'dhcpd#domain_name',
  'listen interfaces'               => 'dhcpd#listen_interfaces#0',
  'first option'                    => 'bind#config#options#0#0',
  'first value'                     => 'bind#config#options#0#1',
  'TFTP base directory'             => 'tftpd#root_dir',
  'internal network address'        => 'tftpd#listen_ip',
  'branch id'                       => 'pxe#branch_id',
  'disk id'                         => 'partitioning#0#$key',
  'disk device'                     => 'partitioning#0#device',
  'disk label'                      => 'partitioning#0#disklabel',
  'first filesystem format'         => 'partitioning#0#partitions#0#format',
  'first partition flags'           => 'partitioning#0#partitions#0#flags',
  'first partition id'              => 'partitioning#0#partitions#0#$key',
  'first partition size'            => 'partitioning#0#partitions#0#size_MiB',
  'first mount point'               => 'partitioning#0#partitions#0#mountpoint',
  'first OS image'                  => 'partitioning#0#partitions#0#image',
  'second filesystem format'        => 'partitioning#0#partitions#1#format',
  'second partition flags'          => 'partitioning#0#partitions#1#flags',
  'second partition id'             => 'partitioning#0#partitions#1#$key',
  'second partition size'           => 'partitioning#0#partitions#1#size_MiB',
  'second mount point'              => 'partitioning#0#partitions#1#mountpoint',
  'second OS image'                 => 'partitioning#0#partitions#1#image',
  'third OS image'                  => 'partitioning#0#partitions#2#image',
  'third filesystem format'         => 'partitioning#0#partitions#2#format',
  'third partition flags'           => 'partitioning#0#partitions#2#flags',
  'timezone name'                   => 'timezone#name',
  'language'                        => 'keyboard_and_language#language',
  'keyboard layout'                 => 'keyboard_and_language#keyboard_layout'
}.freeze

BOX_IDS = {
  'enable SLAAC with routing' => 'branch_network#firewall#enable_SLAAC_with_routing',
  'include forwarders'        => 'bind#config#include_forwarders',
  'enable route'              => 'branch_network#firewall#enable_route',
  'enable NAT'                => 'branch_network#firewall#enable_NAT',
  'containerized proxy'       => 'saltboot#containerized_proxy'
}.freeze

BULLET_STYLE = {
  'failing' => 'fa-times text-danger',
  'warning' => 'fa-hand-o-right text-danger',
  'success' => 'fa-check text-success',
  'pending' => 'fa-hand-o-right text-success',
  'refreshing' => 'fa-refresh text-warning'
}.freeze

# Used for testing software installation/removal in BV
# The value is the package to be installed/removed
PACKAGE_BY_CLIENT = {
  'sle_minion' => 'bison',
  'ssh_minion' => 'bison',
  'rhlike_client' => 'autoconf',
  'rhlike_minion' => 'autoconf',
  'deblike_minion' => 'bison',
  'sle12sp5_minion' => 'bison',
  'sle12sp5_ssh_minion' => 'bison',
  'sle15sp3_minion' => 'bison',
  'sle15sp3_ssh_minion' => 'bison',
  'sle15sp4_minion' => 'bison',
  'sle15sp4_ssh_minion' => 'bison',
  'sle15sp5_minion' => 'bison',
  'sle15sp5_ssh_minion' => 'bison',
  'sle15sp6_minion' => 'bison',
  'sle15sp6_ssh_minion' => 'bison',
  'sle15sp7_minion' => 'bison',
  'sle15sp7_ssh_minion' => 'bison',
  'slemicro51_minion' => 'dejavu',
  'slemicro51_ssh_minion' => 'dejavu',
  'slemicro52_minion' => 'dejavu',
  'slemicro52_ssh_minion' => 'dejavu',
  'slemicro53_minion' => 'dejavu',
  'slemicro53_ssh_minion' => 'dejavu',
  'slemicro54_minion' => 'dejavu',
  'slemicro54_ssh_minion' => 'dejavu',
  'slemicro55_minion' => 'dejavu',
  'slemicro55_ssh_minion' => 'dejavu',
  'slmicro60_minion' => 'dejavu',
  'slmicro60_ssh_minion' => 'dejavu',
  'slmicro61_minion' => 'dejavu',
  'slmicro61_ssh_minion' => 'dejavu',
  'alma8_minion' => 'autoconf',
  'alma8_ssh_minion' => 'autoconf',
  'alma9_minion' => 'autoconf',
  'alma9_ssh_minion' => 'autoconf',
  'amazon2023_minion' => 'autoconf',
  'amazon2023_ssh_minion' => 'autoconf',
  'centos7_minion' => 'autoconf',
  'centos7_ssh_minion' => 'autoconf',
  'liberty9_minion' => 'autoconf',
  'liberty9_ssh_minion' => 'autoconf',
  'oracle9_minion' => 'autoconf',
  'oracle9_ssh_minion' => 'autoconf',
  'rhel9_minion' => 'autoconf',
  'rhel9_ssh_minion' => 'autoconf',
  'rocky8_minion' => 'bison',
  'rocky8_ssh_minion' => 'bison',
  'rocky9_minion' => 'autoconf',
  'rocky9_ssh_minion' => 'autoconf',
  'ubuntu2204_minion' => 'bison',
  'ubuntu2204_ssh_minion' => 'bison',
  'ubuntu2404_minion' => 'bison',
  'ubuntu2404_ssh_minion' => 'bison',
  'debian12_minion' => 'bison',
  'debian12_ssh_minion' => 'bison',
  'opensuse156arm_minion' => 'bison',
  'opensuse156arm_ssh_minion' => 'bison',
  'sle15sp5s390_minion' => 'bison',
  'sle15sp5s390_ssh_minion' => 'bison',
  'salt_migration_minion' => 'bison'
}.freeze

# Names of our base/parent channels
# The keys are the RemoteNode targets
# The values can be found in the webUI under Software -> Manage -> Channels -> Create Channel
# The required product has to be synced before
# Then take a look at the Parent Channel selections
BASE_CHANNEL_BY_CLIENT = {
  'SUSE Manager' => {
    'proxy' => 'SL-Micro-6.1-Pool for x86_64',
    'proxy_container' => 'SL-Micro-6.1-Pool for x86_64',
    'proxy_nontransactional' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'ssh_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'rhlike_minion' => 'RHEL8-Pool for x86_64',
    'deblike_minion' => 'ubuntu-2404-amd64-main for amd64',
    'pxeboot_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'build_host' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle12sp5_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle15sp3_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp3_ssh_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp4_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_ssh_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'monitoring_server' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp5_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
    'sle15sp5_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
    'sle15sp6_minion' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp6_buildhost' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp6_terminal' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp6_ssh_minion' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp7_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp7_buildhost' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp7_ssh_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp7_terminal' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'slemicro51_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
    'slemicro51_ssh_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
    'slemicro52_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
    'slemicro52_ssh_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
    'slemicro53_minion' => 'SLE-Micro-5.3-Pool for x86_64',
    'slemicro53_ssh_minion' => 'SLE-Micro-5.3-Pool for x86_64',
    'slemicro54_minion' => 'SLE-Micro-5.4-Pool for x86_64',
    'slemicro54_ssh_minion' => 'SLE-Micro-5.4-Pool for x86_64',
    'slemicro55_minion' => 'SLE-Micro-5.5-Pool for x86_64',
    'slemicro55_ssh_minion' => 'SLE-Micro-5.5-Pool for x86_64',
    'slmicro60_minion' => 'SL-Micro-6.0-Pool for x86_64',
    'slmicro60_ssh_minion' => 'SL-Micro-6.0-Pool for x86_64',
    'slmicro61_minion' => 'SL-Micro-6.1-Pool for x86_64',
    'slmicro61_ssh_minion' => 'SL-Micro-6.1-Pool for x86_64',
    'alma8_minion' => 'almalinux8 for x86_64',
    'alma8_ssh_minion' => 'almalinux8 for x86_64',
    'alma9_minion' => 'almalinux9 for x86_64',
    'alma9_ssh_minion' => 'almalinux9 for x86_64',
    'amazon2023_minion' => 'amazonlinux2023 for x86_64',
    'amazon2023_ssh_minion' => 'amazonlinux2023 for x86_64',
    'centos7_minion' => 'RES-7-LTSS-Updates for x86_64',
    'centos7_ssh_minion' => 'RES-7-LTSS-Updates for x86_64',
    'liberty9_minion' => 'EL9-Pool for x86_64',
    'liberty9_ssh_minion' => 'EL9-Pool for x86_64',
    'oracle9_minion' => 'oraclelinux9 for x86_64',
    'oracle9_ssh_minion' => 'oraclelinux9 for x86_64',
    'rhel9_minion' => 'EL9-Pool for x86_64',
    'rhel9_ssh_minion' => 'EL9-Pool for x86_64',
    'rocky8_minion' => 'rockylinux-8 for x86_64',
    'rocky8_ssh_minion' => 'rockylinux-8 for x86_64',
    'rocky9_minion' => 'rockylinux-9 for x86_64',
    'rocky9_ssh_minion' => 'rockylinux-9 for x86_64',
    'ubuntu2204_minion' => 'ubuntu-2204-amd64-main for amd64',
    'ubuntu2204_ssh_minion' => 'ubuntu-2204-amd64-main for amd64',
    'ubuntu2404_minion' => 'ubuntu-2404-amd64-main for amd64',
    'ubuntu2404_ssh_minion' => 'ubuntu-2404-amd64-main for amd64',
    'debian12_minion' => 'debian-12-pool for amd64',
    'debian12_ssh_minion' => 'debian-12-pool for amd64',
    'opensuse156arm_minion' => 'openSUSE-Leap-15.6-Pool for aarch64',
    'opensuse156arm_ssh_minion' => 'openSUSE-Leap-15.6-Pool for aarch64',
    'sle15sp5s390_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'sle15sp5s390_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'salt_migration_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64'
  },
  'Uyuni' => {
    'proxy' => 'openSUSE Tumbleweed (x86_64)',
    'proxy_container' => 'openSUSE Tumbleweed (x86_64)',
    'proxy_nontransactional' => 'openSUSE Tumbleweed (x86_64)',
    'sle_minion' => 'openSUSE Tumbleweed (x86_64)',
    'ssh_minion' => 'openSUSE Tumbleweed (x86_64)',
    'rhlike_minion' => 'RHEL8-Pool for x86_64',
    'deblike_minion' => 'Ubuntu 24.04 LTS AMD64 Base for Uyuni',
    'pxeboot_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'build_host' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle12sp5_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle15sp3_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp3_ssh_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp4_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_ssh_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'monitoring_server' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp5_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
    'sle15sp5_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
    'sle15sp6_minion' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp6_ssh_minion' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp6_buildhost' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp6_terminal' => 'SLE-Product-SLES15-SP6-Pool for x86_64',
    'sle15sp7_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp7_ssh_minion' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp7_buildhost' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'sle15sp7_terminal' => 'SLE-Product-SLES15-SP7-Pool for x86_64',
    'slemicro51_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
    'slemicro51_ssh_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
    'slemicro52_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
    'slemicro52_ssh_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
    'slemicro53_minion' => 'SLE-Micro-5.3-Pool for x86_64',
    'slemicro53_ssh_minion' => 'SLE-Micro-5.3-Pool for x86_64',
    'slemicro54_minion' => 'SLE-Micro-5.4-Pool for x86_64',
    'slemicro54_ssh_minion' => 'SLE-Micro-5.4-Pool for x86_64',
    'slemicro55_minion' => 'SLE-Micro-5.5-Pool for x86_64',
    'slemicro55_ssh_minion' => 'SLE-Micro-5.5-Pool for x86_64',
    'slmicro60_minion' => 'SL-Micro-6.0-Pool for x86_64',
    'slmicro60_ssh_minion' => 'SL-Micro-6.0-Pool for x86_64',
    'slmicro61_minion' => 'SL-Micro-6.1-Pool for x86_64',
    'slmicro61_ssh_minion' => 'SL-Micro-6.1-Pool for x86_64',
    'alma8_minion' => 'AlmaLinux 8 (x86_64)',
    'alma8_ssh_minion' => 'AlmaLinux 8 (x86_64)',
    'alma9_minion' => 'AlmaLinux 9 (x86_64)',
    'alma9_ssh_minion' => 'AlmaLinux 9 (x86_64)',
    'amazon2023_minion' => 'Amazon Linux 2023 x86_64',
    'amazon2023_ssh_minion' => 'Amazon Linux 2023 x86_64',
    'centos7_minion' => 'CentOS 7 (x86_64)',
    'centos7_ssh_minion' => 'CentOS 7 (x86_64)',
    'liberty9_minion' => 'EL9-Pool for x86_64',
    'liberty9_ssh_minion' => 'EL9-Pool for x86_64',
    'oracle9_minion' => 'Oracle Linux 9 (x86_64)',
    'oracle9_ssh_minion' => 'Oracle Linux 9 (x86_64)',
    'rhel9_minion' => 'EL9-Pool for x86_64',
    'rhel9_ssh_minion' => 'EL9-Pool for x86_64',
    'rocky8_minion' => 'Rocky Linux 8 (x86_64)',
    'rocky8_ssh_minion' => 'Rocky Linux 8 (x86_64)',
    'rocky9_minion' => 'Rocky Linux 9 (x86_64)',
    'rocky9_ssh_minion' => 'Rocky Linux 9 (x86_64)',
    'ubuntu2204_minion' => 'Ubuntu 22.04 LTS AMD64 Base for Uyuni',
    'ubuntu2204_ssh_minion' => 'Ubuntu 22.04 LTS AMD64 Base for Uyuni',
    'ubuntu2404_minion' => 'Ubuntu 24.04 LTS AMD64 Base for Uyuni',
    'ubuntu2404_ssh_minion' => 'Ubuntu 24.04 LTS AMD64 Base for Uyuni',
    'debian12_minion' => 'Debian 12 (bookworm) pool for amd64 for Uyuni',
    'debian12_ssh_minion' => 'Debian 12 (bookworm) pool for amd64 for Uyuni',
    'opensuse156arm_minion' => 'openSUSE Leap 15.6 (aarch64)',
    'opensuse156arm_ssh_minion' => 'openSUSE Leap 15.6 (aarch64)',
    'sle15sp5s390_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'sle15sp5s390_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'salt_migration_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64'
  },
  'Fake' => {
    'sle_minion' => 'Fake-Base-Channel-SUSE-like',
    'pxeboot_minion' => 'Fake-Base-Channel-SUSE-like',
    'proxy' => 'Fake-Base-Channel-SUSE-like',
    'build_host' => 'Fake-Base-Channel-SUSE-like'
  }
}.freeze

# Used for creating activation keys
# The keys are the values of BASE_CHANNEL_BY_CLIENT
# MLM: The values can be found under Admin -> Setup Wizard -> Products
# Select the desired product and have a look at its product channels
# The required product has to be synced before
# Uyuni: You have to use `spacewalk-common-channels -l` to get the proper values
LABEL_BY_BASE_CHANNEL = {
  'SUSE Manager' => {
    'SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
    'SLES12-SP5-Pool for x86_64' => 'sles12-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP6-Pool for x86_64' => 'sle-product-sles15-sp6-pool-x86_64',
    'SLE-Product-SLES15-SP7-Pool for x86_64' => 'sle-product-sles15-sp7-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'sle-product-sles15-sp5-pool-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'SL-Micro-6.0-Pool for x86_64' => 'sl-micro-6.0-pool-x86_64',
    'SL-Micro-6.1-Pool for x86_64' => 'sl-micro-6.1-pool-x86_64',
    'almalinux8 for x86_64' => 'almalinux8-x86_64',
    'almalinux9 for x86_64' => 'almalinux9-x86_64',
    'amazonlinux2023 for x86_64' => 'amazonlinux2023-x86_64',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
    'RES-7-LTSS-Updates for x86_64' => 'res-7-ltss-updates-x86_64',
    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
    'oraclelinux9 for x86_64' => 'oraclelinux9-x86_64',
    'rockylinux-8 for x86_64' => 'rockylinux-8-x86_64',
    'rockylinux-9 for x86_64' => 'rockylinux-9-x86_64',
    'ubuntu-2204-amd64-main for amd64' => 'ubuntu-2204-amd64-main-amd64',
    'ubuntu-2404-amd64-main for amd64' => 'ubuntu-2404-amd64-main-amd64',
    'debian-12-pool for amd64' => 'debian-12-pool-amd64',
    'openSUSE-Leap-15.6-Pool for aarch64' => 'opensuse-leap-15.6-pool-aarch64'
  },
  'Uyuni' => {
    'openSUSE Tumbleweed (x86_64)' => 'opensuse_tumbleweed-x86_64',
    'openSUSE Leap 15.6 (x86_64)' => 'opensuse_leap15_6-x86_64',
    'openSUSE Leap Micro 5.5 (x86_64)' => 'opensuse_micro5_5-x86_64',
    'SLES12-SP5-Pool for x86_64' => 'sles12-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP6-Pool for x86_64' => 'sle-product-sles15-sp6-pool-x86_64',
    'SLE-Product-SLES15-SP7-Pool for x86_64' => 'sle-product-sles15-sp7-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'sle-product-sles15-sp5-pool-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'SL-Micro-6.0-Pool for x86_64' => 'sl-micro-6.0-pool-x86_64',
    'SL-Micro-6.1-Pool for x86_64' => 'sl-micro-6.1-pool-x86_64',
    'AlmaLinux 8 (x86_64)' => 'almalinux8-x86_64',
    'AlmaLinux 9 (x86_64)' => 'almalinux9-x86_64',
    'Amazon Linux 2023 x86_64' => 'amazonlinux2023-x86_64',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'CentOS 7 (x86_64)' => 'centos7-x86_64',
    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
    'Oracle Linux 9 (x86_64)' => 'oraclelinux9-x86_64',
    'Rocky Linux 8 (x86_64)' => 'rockylinux8-x86_64',
    'Rocky Linux 9 (x86_64)' => 'rockylinux9-x86_64',
    'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2204-pool-amd64-uyuni',
    'Ubuntu 24.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2404-pool-amd64-uyuni',
    'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian-12-pool-amd64-uyuni',
    'openSUSE Leap 15.6 (aarch64)' => 'opensuse_leap15_6-aarch64'
  }
}.freeze

# Used for creating bootstrap repositories
# The keys are the values of BASE_CHANNEL_BY_CLIENT
# The values can be found out on the server by running 'mgr-create-bootstrap-repo'
# Then select the correct name for the product you want
CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL = {
  'SUSE Manager' => {
    'SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64' => 'SUMA-43-PROXY-x86_64',
    'SLES12-SP5-Pool for x86_64' => 'SLE-12-SP5-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'SLE-15-SP3-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'SLE-15-SP4-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'SLE-15-SP5-x86_64',
    'SLE-Product-SLES15-SP6-Pool for x86_64' => 'SLE-15-SP6-x86_64',
    'SLE-Product-SLES15-SP7-Pool for x86_64' => 'SLE-15-SP7-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'SLE-15-SP5-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'SLE-MICRO-5.1-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'SLE-MICRO-5.2-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'SLE-MICRO-5.3-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'SLE-MICRO-5.4-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'SLE-MICRO-5.5-x86_64',
    'SL-Micro-6.0-Pool for x86_64' => 'SL-MICRO-6.0-x86_64',
    'SL-Micro-6.1-Pool for x86_64' => 'SL-MICRO-6.1-x86_64',
    'almalinux8 for x86_64' => 'almalinux-8-x86_64',
    'almalinux9 for x86_64' => 'almalinux-9-x86_64',
    'amazonlinux2023 for x86_64' => 'amazonlinux-2023-x86_64',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'RES-7-LTSS-Updates for x86_64' => 'SLL7-LTSS-x86_64',
    'EL9-Pool for x86_64' => 'SUSE-LibertyLinux9-x86_64',
    'oraclelinux9 for x86_64' => 'oracle-9-x86_64',
    'RHEL8-Pool for x86_64' => 'SLE-ES8-x86_64',
    'rockylinux-8 for x86_64' => 'rockylinux-8-x86_64',
    'rockylinux-9 for x86_64' => 'rockylinux-9-x86_64',
    'ubuntu-2204-amd64-main for amd64' => 'ubuntu-22.04-amd64',
    'ubuntu-2404-amd64-main for amd64' => 'ubuntu-24.04-amd64',
    'debian-12-pool for amd64' => 'debian12-amd64',
    'openSUSE-Leap-15.6-Pool for aarch64' => 'openSUSE-Leap-15.6-aarch64'
  },
  'Uyuni' => {
    'openSUSE Leap 15.6 (x86_64)' => 'openSUSE-Leap-15.6-x86_64-uyuni',
    'openSUSE Leap Micro 5.5 (x86_64)' => 'openSUSE-Leap-Micro-5.5-x86_64-uyuni',
    'openSUSE Tumbleweed (x86_64)' => 'openSUSE-Tumbleweed-x86_64-uyuni',
    'SLES12-SP5-Pool for x86_64' => 'SLE-12-SP5-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'SLE-15-SP3-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'SLE-15-SP4-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'SLE-15-SP5-x86_64',
    'SLE-Product-SLES15-SP6-Pool for x86_64' => 'SLE-15-SP6-x86_64',
    'SLE-Product-SLES15-SP7-Pool for x86_64' => 'SLE-15-SP7-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'SLE-15-SP5-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'SLE-MICRO-5.1-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'SLE-MICRO-5.2-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'SLE-MICRO-5.3-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'SLE-MICRO-5.4-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'SLE-MICRO-5.5-x86_64',
    'SL-Micro-6.0-Pool for x86_64' => 'SL-MICRO-6.0-x86_64',
    'SL-Micro-6.1-Pool for x86_64' => 'SL-MICRO-6.1-x86_64',
    'AlmaLinux 8 (x86_64)' => 'almalinux-8-x86_64-uyuni',
    'AlmaLinux 9 (x86_64)' => 'almalinux-9-x86_64-uyuni',
    'Amazon Linux 2023 x86_64' => 'amazonlinux-2023-x86_64-uyuni',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'CentOS 7 (x86_64)' => 'centos-7-x86_64-uyuni',
    'EL9-Pool for x86_64' => 'SUSE-LibertyLinux9-x86_64',
    'Oracle Linux 9 (x86_64)' => 'oracle-9-x86_64-uyuni',
    'Rocky Linux 8 (x86_64)' => 'rockylinux8-x86_64-uyuni',
    'Rocky Linux 9 (x86_64)' => 'rockylinux9-x86_64-uyuni',
    'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2204-amd64-uyuni',
    'Ubuntu 24.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2404-amd64-uyuni',
    'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian12-amd64-uyuni',
    'openSUSE Leap 15.6 (aarch64)' => 'openSUSE-Leap-15.6-aarch64-uyuni'
  }
}.freeze

# Used for creating bootstrap repositories
# MLM: The values can be found under Admin -> Setup Wizard -> Products
# Select the desired product and have a look at its product channels
# The required product has to be synced before
# Uyuni: You have to use `spacewalk-common-channels -l` with the appended architecture
# e.g. almalinux9 -> almalinux9-x86_64
PARENT_CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL = {
  'SUSE Manager' => {
    'SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
    'SLES12-SP5-Pool for x86_64' => nil,
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP6-Pool for x86_64' => 'sle-product-sles15-sp6-pool-x86_64',
    'SLE-Product-SLES15-SP7-Pool for x86_64' => 'sle-product-sles15-sp7-pool-x86_64',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'SL-Micro-6.0-Pool for x86_64' => 'sl-micro-6.0-pool-x86_64',
    'SL-Micro-6.1-Pool for x86_64' => 'sl-micro-6.1-pool-x86_64',
    'almalinux8 for x86_64' => nil,
    'almalinux9 for x86_64' => nil,
    'amazonlinux2023 for x86_64' => nil,
    'Fake-Base-Channel-SUSE-like' => nil,
    'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
    'RES-7-LTSS-Updates for x86_64' => nil,
    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
    'oraclelinux9 for x86_64' => nil,
    'RHEL8-Pool for x86_64' => nil,
    'rockylinux-8 for x86_64' => nil,
    'rockylinux-9 for x86_64' => nil,
    'ubuntu-2204-amd64-main for amd64' => nil,
    'ubuntu-2404-amd64-main for amd64' => nil,
    'debian-12-pool for amd64' => 'debian-12-pool-amd64',
    'openSUSE-Leap-15.6-Pool for aarch64' => nil
  },
  'Uyuni' => {
    'openSUSE Leap 15.6 (x86_64)' => nil,
    'openSUSE Leap Micro 5.5 (x86_64)' => 'opensuse_micro5_5-x86_64',
    'openSUSE Tumbleweed (x86_64)' => nil,
    'SLES12-SP5-Pool for x86_64' => nil,
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP6-Pool for x86_64' => 'sle-product-sles15-sp6-pool-x86_64',
    'SLE-Product-SLES15-SP7-Pool for x86_64' => 'sle-product-sles15-sp7-pool-x86_64',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'SL-Micro-6.0-Pool for x86_64' => 'sl-micro-6.0-pool-x86_64',
    'SL-Micro-6.1-Pool for x86_64' => 'sl-micro-6.1-pool-x86_64',
    'AlmaLinux 8 (x86_64)' => nil,
    'AlmaLinux 9 (x86_64)' => nil,
    'Amazon Linux 2023 x86_64' => nil,
    'Fake-Base-Channel-SUSE-like' => nil,
    'CentOS 7 (x86_64)' => 'centos-7-x86_64-uyuni',
    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
    'Oracle Linux 9 (x86_64)' => nil,
    'Rocky Linux 8 (x86_64)' => nil,
    'Rocky Linux 9 (x86_64)' => nil,
    'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => nil,
    'Ubuntu 24.04 LTS AMD64 Base for Uyuni' => nil,
    'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian12-amd64-uyuni',
    'openSUSE Leap 15.6 (aarch64)' => nil
  }
}.freeze

PKGARCH_BY_CLIENT = {
  'proxy' => 'x86_64',
  'sle_minion' => 'x86_64',
  'ssh_minion' => 'x86_64',
  'rhlike_minion' => 'x86_64',
  'deblike_minion' => 'amd64',
  'sle12sp5_minion' => 'x86_64',
  'sle12sp5_ssh_minion' => 'x86_64',
  'sle15_ssh_minion' => 'x86_64',
  'sle15sp3_minion' => 'x86_64',
  'sle15sp3_ssh_minion' => 'x86_64',
  'sle15sp4_minion' => 'x86_64',
  'sle15sp4_ssh_minion' => 'x86_64',
  'sle15sp5_minion' => 'x86_64',
  'sle15sp5_ssh_minion' => 'x86_64',
  'sle15sp6_minion' => 'x86_64',
  'sle15sp6_ssh_minion' => 'x86_64',
  'sle15sp7_minion' => 'x86_64',
  'sle15sp7_ssh_minion' => 'x86_64',
  'slemicro51_minion' => 'x86_64',
  'slemicro51_ssh_minion' => 'x86_64',
  'slemicro52_minion' => 'x86_64',
  'slemicro52_ssh_minion' => 'x86_64',
  'slemicro53_minion' => 'x86_64',
  'slemicro53_ssh_minion' => 'x86_64',
  'slemicro54_minion' => 'x86_64',
  'slemicro54_ssh_minion' => 'x86_64',
  'slemicro55_minion' => 'x86_64',
  'slemicro55_ssh_minion' => 'x86_64',
  'slmicro60_minion' => 'x86_64',
  'slmicro60_ssh_minion' => 'x86_64',
  'slmicro61_minion' => 'x86_64',
  'slmicro61_ssh_minion' => 'x86_64',
  'alma8_minion' => 'x86_64',
  'alma8_ssh_minion' => 'x86_64',
  'alma9_minion' => 'x86_64',
  'alma9_ssh_minion' => 'x86_64',
  'amazon2023_minion' => 'x86_64',
  'amazon2023_ssh_minion' => 'x86_64',
  'centos7_minion' => 'x86_64',
  'centos7_ssh_minion' => 'x86_64',
  'liberty9_minion' => 'x86_64',
  'liberty9_ssh_minion' => 'x86_64',
  'oracle9_minion' => 'x86_64',
  'oracle9_ssh_minion' => 'x86_64',
  'rhel9_minion' => 'x86_64',
  'rhel9_ssh_minion' => 'x86_64',
  'rocky8_minion' => 'x86_64',
  'rocky8_ssh_minion' => 'x86_64',
  'rocky9_minion' => 'x86_64',
  'rocky9_ssh_minion' => 'x86_64',
  'ubuntu2204_minion' => 'amd64',
  'ubuntu2204_ssh_minion' => 'amd64',
  'ubuntu2404_minion' => 'amd64',
  'ubuntu2404_ssh_minion' => 'amd64',
  'debian12_minion' => 'amd64',
  'debian12_ssh_minion' => 'amd64',
  'opensuse156arm_minion' => 'aarch64',
  'opensuse156arm_ssh_minion' => 'aarch64',
  'sle15sp5s390_minion' => 's390x',
  'sle15sp5s390_ssh_minion' => 's390x'
}.freeze

# Explanations:
# - 'default' is required for auto-installation tests.
# - '# CHECKED' means that we verified that the list of channels matches the results in /var/log/rhn/reposync,
#   and that we took the occasion to evaluate a reasonable timeout for them
CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION = {
  'SUSE Manager' => {
    'default' => # CHECKED
      %w[
        sle-product-sles15-sp7-pool-x86_64
        sle-product-sles15-sp7-updates-x86_64
        sle15-sp7-installer-updates-x86_64
        sle-module-basesystem15-sp7-pool-x86_64
        sle-module-basesystem15-sp7-updates-x86_64
        managertools-sle15-pool-x86_64-sp7
        managertools-sle15-updates-x86_64-sp7
        sle-module-python3-15-sp7-pool-x86_64
        sle-module-python3-15-sp7-updates-x86_64
        sle-module-server-applications15-sp7-pool-x86_64
        sle-module-server-applications15-sp7-updates-x86_64
        sle-module-desktop-applications15-sp7-pool-x86_64
        sle-module-desktop-applications15-sp7-updates-x86_64
        sle-module-devtools15-sp7-updates-x86_64
        sle-module-devtools15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-updates-x86_64
        sle-module-containers15-sp7-pool-x86_64
        sle-module-containers15-sp7-updates-x86_64
      ],
    'almalinux8' => # CHECKED
      %w[
        almalinux8-x86_64
        almalinux8-appstream-x86_64
        managertools-el8-pool-x86_64-alma
        managertools-el8-updates-x86_64-alma
        managertools-beta-el8-pool-x86_64-alma
        managertools-beta-el8-updates-x86_64-alma
      ],
    'almalinux9' => # CHECKED
      %w[
        almalinux9-x86_64
        almalinux9-appstream-x86_64
        managertools-el9-pool-x86_64-alma
        managertools-el9-updates-x86_64-alma
        managertools-beta-el9-pool-x86_64-alma
        managertools-beta-el9-updates-x86_64-alma
      ],
    'amazonlinux2023' => # CHECKED
      %w[
        amazonlinux2023-x86_64
        managertools-el9-pool-x86_64-amazon
        managertools-el9-updates-x86_64-amazon
        managertools-beta-el9-pool-x86_64-amazon
        managertools-beta-el9-updates-x86_64-amazon
      ],
    'debian-12' => # CHECKED
      %w[
        debian-12-pool-amd64
        debian-12-main-security-amd64
        debian-12-main-updates-amd64
        managertools-debian12-updates-amd64
        managertools-debian12-beta-updates-amd64
      ],
    'sll-7-ltss' => # CHECKED
      %w[
        res-7-ltss-updates-x86_64
        managertools-el7-pool-x86_64-lbt7
        managertools-el7-updates-x86_64-lbt7
      ],
    'sll-9' => # CHECKED
      %w[
        sll-cb-9-updates-x86_64
        sll-as-9-updates-x86_64
        sll-9-updates-x86_64
      ],
    'el9' => # CHECKED
      %w[
        el9-pool-x86_64
        managertools-el9-pool-x86_64
        managertools-el9-updates-x86_64
        managertools-beta-el9-pool-x86_64
        managertools-beta-el9-updates-x86_64
        managertools-el9-pool-x86_64-rocky
        managertools-el9-updates-x86_64-rocky
      ],
    'rockylinux8' => # CHECKED
      %w[
        rockylinux-8-x86_64
        rockylinux-8-appstream-x86_64
        managertools-el8-pool-x86_64-rocky
        managertools-el8-updates-x86_64-rocky
      ],
    'rockylinux9' => # CHECKED
      %w[
        rockylinux-9-x86_64
        rockylinux-9-appstream-x86_64
        managertools-el9-pool-x86_64-rocky
        managertools-el9-updates-x86_64-rocky
      ],
    'oraclelinux9' => # CHECKED
      %w[
        oraclelinux9-x86_64
        oraclelinux9-appstream-x86_64
      ],
    'sles12-sp5' => # CHECKED
      %w[
        sles12-sp5-pool-x86_64
        sles12-sp5-updates-x86_64
        managertools-sle12-pool-x86_64-sp5
        managertools-sle12-updates-x86_64-sp5
        sles12-sp5-installer-updates-x86_64
        sles12-sp5-ltss-updates-x86_64
      ],
    'sles15-sp3' => # CHECKED
      %w[
        sle-product-sles15-sp3-pool-x86_64
        sle-product-sles15-sp3-updates-x86_64
        sle15-sp3-installer-updates-x86_64
        sle-module-basesystem15-sp3-updates-x86_64
        sle-module-basesystem15-sp3-pool-x86_64
        managertools-sle15-pool-x86_64-sp3
        managertools-sle15-updates-x86_64-sp3
        sle-module-server-applications15-sp3-updates-x86_64
        sle-module-server-applications15-sp3-pool-x86_64
        sle-product-sles15-sp3-ltss-updates-x86_64
        sle-module-desktop-applications15-sp3-updates-x86_64
        sle-module-desktop-applications15-sp3-pool-x86_64
        sle-module-devtools15-sp3-pool-x86_64
        sle-module-devtools15-sp3-updates-x86_64
      ],
    'sles15-sp4' => # CHECKED
      %w[
        sle-product-sles15-sp4-pool-x86_64
        sle-product-sles15-sp4-updates-x86_64
        sle15-sp4-installer-updates-x86_64
        sle-module-basesystem15-sp4-updates-x86_64
        sle-module-basesystem15-sp4-pool-x86_64
        sle-module-desktop-applications15-sp4-updates-x86_64
        sle-module-desktop-applications15-sp4-pool-x86_64
        sle-module-server-applications15-sp4-pool-x86_64
        sle-module-server-applications15-sp4-updates-x86_64
        sle-product-sles15-sp4-ltss-updates-x86_64
        managertools-sle15-pool-x86_64-sp4
        managertools-sle15-updates-x86_64-sp4
        managertools-beta-sle15-pool-x86_64-sp4
        managertools-beta-sle15-updates-x86_64-sp4
        sle-module-devtools15-sp4-updates-x86_64
        sle-module-devtools15-sp4-pool-x86_64
        sle-module-containers15-sp4-pool-x86_64
        sle-module-containers15-sp4-updates-x86_64
      ],
    'sles15-sp5' => # CHECKED
      %w[
        sle-product-sles15-sp5-pool-x86_64
        sle-product-sles15-sp5-updates-x86_64
        sle-module-basesystem15-sp5-pool-x86_64
        sle-module-basesystem15-sp5-updates-x86_64
        sle-module-python3-15-sp5-pool-x86_64
        sle-module-python3-15-sp5-updates-x86_64
        managertools-sle15-pool-x86_64-sp5
        managertools-sle15-updates-x86_64-sp5
        sle-module-server-applications15-sp5-pool-x86_64
        sle-module-server-applications15-sp5-updates-x86_64
        sle-module-desktop-applications15-sp5-updates-x86_64
        sle-module-desktop-applications15-sp5-pool-x86_64
        sle-module-devtools15-sp5-pool-x86_64
        sle-module-devtools15-sp5-updates-x86_64
        sle-product-sles15-sp5-ltss-updates-x86_64
      ],
    'sles15-sp6' => # CHECKED
      %w[
        sle-product-sles15-sp6-pool-x86_64
        sle-product-sles15-sp6-updates-x86_64
        sle-module-basesystem15-sp6-pool-x86_64
        sle-module-basesystem15-sp6-updates-x86_64
        managertools-sle15-pool-x86_64-sp6
        managertools-sle15-updates-x86_64-sp6
        sle-module-server-applications15-sp6-pool-x86_64
        sle-module-server-applications15-sp6-updates-x86_64
        sle-module-desktop-applications15-sp6-pool-x86_64
        sle-module-desktop-applications15-sp6-updates-x86_64
        sle-module-devtools15-sp6-updates-x86_64
        sle-module-devtools15-sp6-pool-x86_64
        sle-module-systems-management-15-sp6-pool-x86_64
        sle-module-systems-management-15-sp6-updates-x86_64
        sle-product-sles15-sp6-ltss-updates-x86_64
      ],
    'sles15-sp7' =>
      %w[
        sle-product-sles15-sp7-pool-x86_64
        sle-product-sles15-sp7-updates-x86_64
        sle15-sp7-installer-updates-x86_64
        sle-module-basesystem15-sp7-pool-x86_64
        sle-module-basesystem15-sp7-updates-x86_64
        managertools-sle15-pool-x86_64-sp7
        managertools-sle15-updates-x86_64-sp7
        sle-module-python3-15-sp7-pool-x86_64
        sle-module-python3-15-sp7-updates-x86_64
        sle-module-server-applications15-sp7-pool-x86_64
        sle-module-server-applications15-sp7-updates-x86_64
        sle-module-desktop-applications15-sp7-pool-x86_64
        sle-module-desktop-applications15-sp7-updates-x86_64
        sle-module-devtools15-sp7-updates-x86_64
        sle-module-devtools15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-updates-x86_64
      ],
    'slesforsap15-sp5' =>
      %w[
        managertools-sle15-pool-x86_64-sap-sp5
        managertools-sle15-updates-x86_64-sap-sp5
        sle-module-basesystem15-sp5-pool-x86_64-sap
        sle-module-basesystem15-sp5-updates-x86_64-sap
        sle-module-desktop-applications15-sp5-pool-x86_64-sap
        sle-module-desktop-applications15-sp5-updates-x86_64-sap
        sle-module-devtools15-sp5-pool-x86_64-sap
        sle-module-devtools15-sp5-updates-x86_64-sap
        sle-module-server-applications15-sp5-pool-x86_64-sap
        sle-module-server-applications15-sp5-updates-x86_64-sap
        sle-product-sles_sap15-sp5-pool-x86_64
        sle-product-sles_sap15-sp5-updates-x86_64
        sle-product-ha15-sp5-pool-x86_64-sap
        sle-product-ha15-sp5-updates-x86_64-sap
        sle-module-sap-applications15-sp5-pool-x86_64
        sle-module-sap-applications15-sp5-updates-x86_64
      ],
    'sles15-sp5-s390x' =>
      %w[
        managertools-sle15-pool-s390x-sp5
        managertools-sle15-updates-s390x-sp5
        sle-module-basesystem15-sp5-pool-s390x
        sle-module-basesystem15-sp5-updates-s390x
        sle-module-server-applications15-sp5-pool-s390x
        sle-module-server-applications15-sp5-updates-s390x
        sle-product-sles15-sp5-pool-s390x
        sle-product-sles15-sp5-updates-s390x
      ],
    'res7' => # CHECKED
      %w[
        rhel-x86_64-server-7
        res7-x86_64
        managertools-el7-pool-x86_64-lbt
        managertools-el7-updates-x86_64-lbt
      ],
    'res8' =>
      %w[
        rhel8-pool-x86_64
        res-8-updates-x86_64
        res-as-8-updates-x86_64
        res-cb-8-updates-x86_64
        managertools-el8-pool-x86_64
        managertools-el8-updates-x86_64
      ],
    'leap15.6-x86_64' =>
      %w[
        opensuse_leap15_6-x86_64
        opensuse_leap15_6-x86_64-non-oss
        opensuse_leap15_6-x86_64-non-oss-updates
        opensuse_leap15_6-x86_64-updates
        opensuse_leap15_6-x86_64-backports-updates
        opensuse_leap15_6-x86_64-sle-updates
      ],
    'leap15.6-aarch64' =>
      %w[
        opensuse-backports-15.6-updates-aarch64
        opensuse-leap-15.6-pool-aarch64
        opensuse-leap-15.6-updates-aarch64
        opensuse-sle-15.6-updates-aarch64
        managertools-sle15-pool-aarch64-opensuse-15.6
        managertools-sle15-updates-aarch64-opensuse-15.6
      ],
    'tumbleweed-x86_64' =>
      %w[
        opensuse_tumbleweed-x86_64
      ],
    'suse-microos-5.1' => # CHECKED
      %w[
        suse-microos-5.1-pool-x86_64
        suse-microos-5.1-updates-x86_64
      ],
    'suse-microos-5.2' => # CHECKED
      %w[
        suse-microos-5.2-pool-x86_64
        suse-microos-5.2-updates-x86_64
      ],
    'sle-micro-5.3' => # CHECKED
      %w[
        sle-micro-5.3-pool-x86_64
        sle-micro-5.3-updates-x86_64
      ],
    'sle-micro-5.4' => # CHECKED
      %w[
        sle-micro-5.4-pool-x86_64
        sle-micro-5.4-updates-x86_64
      ],
    'sle-micro-5.5' => # CHECKED
      %w[
        sle-micro-5.5-pool-x86_64
        sle-micro-5.5-updates-x86_64
      ],
    'sl-micro-6.0' =>
      %w[
        sl-micro-6.0-pool-x86_64
        managertools-sl-micro-6.0-x86_64
      ],
    'sl-micro-6.1' =>
      %w[
        sl-micro-6.1-pool-x86_64
        managertools-sl-micro-6.1-x86_64
      ],
    'ubuntu-2204' =>
      %w[
        ubuntu-2204-amd64-main-amd64
        ubuntu-2204-amd64-main-updates-amd64
        ubuntu-2204-amd64-main-security-amd64
        managertools-ubuntu2204-updates-amd64
        managertools-beta-ubuntu2204-updates-amd64
      ],
    'ubuntu-2404' =>
      %w[
        ubuntu-2404-amd64-main-amd64
        ubuntu-2404-amd64-main-updates-amd64
        ubuntu-2404-amd64-main-security-amd64
        managertools-ubuntu2404-updates-amd64
        managertools-beta-ubuntu2404-updates-amd64
      ],
    'suma-proxy-extension-50' => # CHECKED
      %w[
        suse-manager-proxy-5.0-pool-x86_64
        suse-manager-proxy-5.0-updates-x86_64
      ],
    'suma-retail-branch-server-extension-50' => # CHECKED
      %w[
        suse-manager-retail-branch-server-5.0-pool-x86_64
        suse-manager-retail-branch-server-5.0-updates-x86_64
      ],
    'suse-multi-linux-manager-proxy-51' =>
      %w[
        suse-multi-linux-manager-proxy-5.1-x86_64
      ],
    'suse-multi-linux-manager-retail-branch-server-51' =>
      %w[
        suse-multi-linux-manager-retail-branch-server-5.1-x86_64
      ],
    'suse-multi-linux-manager-proxy-52' =>
      %w[
        suse-multi-linux-manager-proxy-5.2-x86_64
      ],
    'suse-multi-linux-manager-retail-branch-server-52' =>
      %w[
        suse-multi-linux-manager-retail-branch-server-5.2-x86_64
      ],
    # the following entries use convenience product names meant for the testsuite
    # these product names do not exist in sumatoolbox
    # (but the channel names do exist in sumatoolbox):
    'suma-proxy-extension-50-sp6' =>
      %w[
        suse-manager-proxy-5.0-pool-x86_64-sp6
        suse-manager-proxy-5.0-updates-x86_64-sp6
      ],
    'suma-retail-branch-server-extension-50-sp6' =>
      %w[
        suse-manager-retail-branch-server-5.0-pool-x86_64-sp6
        suse-manager-retail-branch-server-5.0-updates-x86_64-sp6
      ],
    'suse-multi-linux-manager-proxy-51-sp7' =>
      %w[
        sle-module-containers15-sp7-pool-x86_64
        sle-module-containers15-sp7-updates-x86_64
        suse-multi-linux-manager-proxy-sle-5.1-pool-x86_64-sp7
        suse-multi-linux-manager-proxy-sle-5.1-updates-x86_64-sp7
      ],
    'suse-multi-linux-manager-retail-branch-server-51-sp7' =>
      %w[
        suse-multi-linux-manager-retail-branch-server-sle-5.1-pool-x86_64-sp7
        suse-multi-linux-manager-retail-branch-server-sle-5.1-updates-x86_64-sp7
      ],
    'suse-multi-linux-manager-proxy-52-sp7' =>
      %w[
        sle-module-containers15-sp7-pool-x86_64
        sle-module-containers15-sp7-updates-x86_64
        suse-multi-linux-manager-proxy-sle-5.2-pool-x86_64-sp7
        suse-multi-linux-manager-proxy-sle-5.2-updates-x86_64-sp7
      ],
    'suse-multi-linux-manager-retail-branch-server-52-sp7' =>
      %w[
        suse-multi-linux-manager-retail-branch-server-sle-5.2-pool-x86_64-sp7
        suse-multi-linux-manager-retail-branch-server-sle-5.2-updates-x86_64-sp7
      ]
  },
  'Uyuni' => {
    'default' => # CHECKED
      %w[
        sle-product-sles15-sp7-pool-x86_64
        sle-product-sles15-sp7-updates-x86_64
        sle15-sp7-installer-updates-x86_64
        sle-module-basesystem15-sp7-pool-x86_64
        sle-module-basesystem15-sp7-updates-x86_64
        sle-module-python3-15-sp7-pool-x86_64
        sle-module-python3-15-sp7-updates-x86_64
        sle-module-server-applications15-sp7-pool-x86_64
        sle-module-server-applications15-sp7-updates-x86_64
        sle-module-desktop-applications15-sp7-pool-x86_64
        sle-module-desktop-applications15-sp7-updates-x86_64
        sle-module-devtools15-sp7-updates-x86_64
        sle-module-devtools15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-updates-x86_64
        sles15-sp7-devel-uyuni-client-x86_64
      ],
    'almalinux8' => # CHECKED
      %w[
        almalinux8-x86_64
        almalinux8-x86_64-appstream
        almalinux8-x86_64-extras
        almalinux8-uyuni-client-devel-x86_64
      ],
    'almalinux9' => # CHECKED
      %w[
        almalinux9-x86_64
        almalinux9-x86_64-appstream
        almalinux9-x86_64-extras
        almalinux9-uyuni-client-devel-x86_64
      ],
    'amazonlinux2023' =>
      %w[
        amazonlinux2023-x86_64
        amazonlinux2023-uyuni-client-devel-x86_64
      ],
    'centos7' => # CHECKED
      %w[
        centos7-x86_64
        centos7-x86_64-extras
        centos7-uyuni-client-devel-x86_64
      ],
    'debian-12' => # CHECKED
      %w[
        debian-12-pool-amd64-uyuni
        debian-12-amd64-main-updates-uyuni
        debian-12-amd64-main-security-uyuni
        debian-12-amd64-uyuni-client-devel
      ],
    'sll-9' =>
      %w[
        sll-9-updates-x86_64
        sll-as-9-updates-x86_64
        sll-cb-9-updates-x86_64
      ],
    'el9' =>
      %w[
        el9-pool-x86_64
      ],
    'rockylinux8' => # CHECKED
      %w[
        rockylinux-8-x86_64
        rockylinux-8-appstream-x86_64
        rockylinux-8-extras-x86_64
        rockylinux8-uyuni-client-devel-x86_64
      ],
    'rockylinux9' => # CHECKED
      %w[
        rockylinux-9-x86_64
        rockylinux-9-appstream-x86_64
        rockylinux-9-extras-x86_64
        rockylinux9-uyuni-client-devel-x86_64
      ],
    'oraclelinux9' => # CHECKED
      %w[
        oraclelinux9-x86_64
        oraclelinux9-appstream-x86_64
        oraclelinux9-uyuni-client-devel-x86_64
      ],
    'sles12-sp5' => # CHECKED
      %w[
        sles12-sp5-installer-updates-x86_64
        sles12-sp5-pool-x86_64
        sles12-sp5-updates-x86_64
        sles12-sp5-ltss-updates-x86_64
        sles12-sp5-uyuni-client-devel-x86_64
      ],
    'sles15-sp3' => # CHECKED
      %w[
        sle-product-sles15-sp3-ltss-updates-x86_64
        sle-product-sles15-sp3-pool-x86_64
        sle-product-sles15-sp3-updates-x86_64
        sle-module-basesystem15-sp3-pool-x86_64
        sle-module-basesystem15-sp3-updates-x86_64
        sle-module-desktop-applications15-sp3-pool-x86_64
        sle-module-desktop-applications15-sp3-updates-x86_64
        sle-module-devtools15-sp3-pool-x86_64
        sle-module-devtools15-sp3-updates-x86_64
        sle-module-server-applications15-sp3-pool-x86_64
        sle-module-server-applications15-sp3-updates-x86_64
        sle15-sp3-installer-updates-x86_64
        sles15-sp3-devel-uyuni-client-x86_64
      ],
    'sles15-sp4' => # CHECKED
      %w[
        sle-product-sles15-sp4-pool-x86_64
        sle-product-sles15-sp4-updates-x86_64
        sle-module-basesystem15-sp4-pool-x86_64
        sle-module-basesystem15-sp4-updates-x86_64
        sle-module-desktop-applications15-sp4-pool-x86_64
        sle-module-desktop-applications15-sp4-updates-x86_64
        sle-product-sles15-sp4-ltss-updates-x86_64
        sle-module-devtools15-sp4-pool-x86_64
        sle-module-devtools15-sp4-updates-x86_64
        sle-module-containers15-sp4-pool-x86_64
        sle-module-containers15-sp4-updates-x86_64
        sle-module-server-applications15-sp4-pool-x86_64
        sle-module-server-applications15-sp4-updates-x86_64
        sle15-sp4-installer-updates-x86_64
        sles15-sp4-devel-uyuni-client-x86_64
      ],
    'sles15-sp5' => # CHECKED
      %w[
        sle-product-sles15-sp5-pool-x86_64
        sle-product-sles15-sp5-updates-x86_64
        sle-module-basesystem15-sp5-pool-x86_64
        sle-module-basesystem15-sp5-updates-x86_64
        sle-module-desktop-applications15-sp5-pool-x86_64
        sle-module-desktop-applications15-sp5-updates-x86_64
        sle-module-devtools15-sp5-pool-x86_64
        sle-module-devtools15-sp5-updates-x86_64
        sle-module-python3-15-sp5-pool-x86_64
        sle-module-python3-15-sp5-updates-x86_64
        sle-module-server-applications15-sp5-pool-x86_64
        sle-module-server-applications15-sp5-updates-x86_64
        sle-product-sles15-sp5-ltss-updates-x86_64
        sles15-sp5-devel-uyuni-client-x86_64
      ],
    'sles15-sp6' => # CHECKED
      %w[
        sle-product-sles15-sp6-pool-x86_64
        sle-product-sles15-sp6-updates-x86_64
        sle-module-basesystem15-sp6-pool-x86_64
        sle-module-basesystem15-sp6-updates-x86_64
        sle-module-desktop-applications15-sp6-pool-x86_64
        sle-module-desktop-applications15-sp6-updates-x86_64
        sle-module-devtools15-sp6-pool-x86_64
        sle-module-devtools15-sp6-updates-x86_64
        sle-module-python3-15-sp6-pool-x86_64
        sle-module-python3-15-sp6-updates-x86_64
        sle-module-server-applications15-sp6-pool-x86_64
        sle-module-server-applications15-sp6-updates-x86_64
        sle-product-sles15-sp6-ltss-updates-x86_64
        sles15-sp6-devel-uyuni-client-x86_64
      ],
    'sles15-sp7' =>
      %w[
        sle-product-sles15-sp7-pool-x86_64
        sle-product-sles15-sp7-updates-x86_64
        sle15-sp7-installer-updates-x86_64
        sle-module-basesystem15-sp7-pool-x86_64
        sle-module-basesystem15-sp7-updates-x86_64
        sle-module-python3-15-sp7-pool-x86_64
        sle-module-python3-15-sp7-updates-x86_64
        sle-module-server-applications15-sp7-pool-x86_64
        sle-module-server-applications15-sp7-updates-x86_64
        sle-module-desktop-applications15-sp7-pool-x86_64
        sle-module-desktop-applications15-sp7-updates-x86_64
        sle-module-devtools15-sp7-updates-x86_64
        sle-module-devtools15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-pool-x86_64
        sle-module-systems-management-15-sp7-updates-x86_64
        sles15-sp7-devel-uyuni-client-x86_64
      ],
    'slesforsap15-sp5' =>
      %w[
        sle-module-basesystem15-sp5-pool-x86_64-sap
        sle-module-basesystem15-sp5-updates-x86_64-sap
        sle-module-desktop-applications15-sp5-pool-x86_64-sap
        sle-module-desktop-applications15-sp5-updates-x86_64-sap
        sle-module-devtools15-sp5-pool-x86_64-sap
        sle-module-devtools15-sp5-updates-x86_64-sap
        sle-module-server-applications15-sp5-pool-x86_64-sap
        sle-module-server-applications15-sp5-updates-x86_64-sap
        sle-product-sles_sap15-sp5-pool-x86_64
        sle-product-sles_sap15-sp5-updates-x86_64
        sle-product-ha15-sp5-pool-x86_64-sap
        sle-product-ha15-sp5-updates-x86_64-sap
        sle-module-sap-applications15-sp5-pool-x86_64
        sle-module-sap-applications15-sp5-updates-x86_64
      ],
    'sles15-sp5-s390x' =>
      %w[
        sle-module-basesystem15-sp5-pool-s390x
        sle-module-basesystem15-sp5-updates-s390x
        sle-module-server-applications15-sp5-pool-s390x
        sle-module-server-applications15-sp5-updates-s390x
        sle-product-sles15-sp5-pool-s390x
        sle-product-sles15-sp5-updates-s390x
      ],
    'res7' =>
      %w[
        rhel-x86_64-server-7
        res7-x86_64
      ],
    'res8' =>
      %w[
        rhel8-pool-x86_64
        res-8-updates-x86_64
        res-as-8-updates-x86_64
        res-cb-8-updates-x86_64
        sll8-uyuni-client-x86_64
      ],
    'leap15.6-x86_64' => # CHECKED
      %w[
        opensuse_leap15_6-x86_64
        opensuse_leap15_6-x86_64-backports-updates
        opensuse_leap15_6-x86_64-non-oss
        opensuse_leap15_6-x86_64-non-oss-updates
        opensuse_leap15_6-x86_64-updates
        opensuse_leap15_6-x86_64-sle-updates
        opensuse_leap15_6-uyuni-client-x86_64
        opensuse_leap15_6-uyuni-client-devel-x86_64
        uyuni-proxy-devel-leap-x86_64
      ],
    # this on its own is needed due to the reposync killing in Uyuni
    'leap15.6-client-tools-x86_64' => # CHECKED
      %w[
        opensuse_leap15_6-uyuni-client-x86_64
        opensuse_leap15_6-uyuni-client-devel-x86_64
      ],
    'leap15.6-aarch64' => # CHECKED
      %w[
        opensuse_leap15_6-aarch64
        opensuse_leap15_6-aarch64-backports-updates
        opensuse_leap15_6-aarch64-non-oss
        opensuse_leap15_6-aarch64-non-oss-updates
        opensuse_leap15_6-aarch64-sle-updates
        opensuse_leap15_6-aarch64-updates
        opensuse_leap15_6-uyuni-client-devel-aarch64
      ],
    'leap-micro5.5-x86_64' => # CHECKED
      %w[
        opensuse_micro5_5-x86_64
        opensuse_micro5_5-x86_64-sle-updates
      ],
    'leap-micro5.5-client-tools-x86_64' => # CHECKED
      %w[
        opensuse_micro5_5-uyuni-client-x86_64
        opensuse_micro5_5-uyuni-client-devel-x86_64
      ],
    'suse-microos-5.1' => # CHECKED
      %w[
        suse-microos-5.1-pool-x86_64
        suse-microos-5.1-updates-x86_64
        suse-microos-5.1-devel-uyuni-client-x86_64
      ],
    'suse-microos-5.2' => # CHECKED
      %w[
        suse-microos-5.2-pool-x86_64
        suse-microos-5.2-updates-x86_64
        suse-microos-5.2-devel-uyuni-client-x86_64
      ],
    'sle-micro-5.3' => # CHECKED
      %w[
        sle-micro-5.3-pool-x86_64
        sle-micro-5.3-updates-x86_64
        sle-micro-5.3-devel-uyuni-client-x86_64
      ],
    'sle-micro-5.4' => # CHECKED
      %w[
        sle-micro-5.4-pool-x86_64
        sle-micro-5.4-updates-x86_64
        sle-micro-5.4-devel-uyuni-client-x86_64
      ],
    'sle-micro-5.5' => # CHECKED
      %w[
        sle-micro-5.5-pool-x86_64
        sle-micro-5.5-updates-x86_64
        sle-micro-5.5-devel-uyuni-client-x86_64
      ],
    'sl-micro-6.0' =>
      %w[
        sl-micro-6.0-pool-x86_64
        sl-micro-6.0-devel-uyuni-client-x86_64
      ],
    'sl-micro-6.1' =>
      %w[
        sl-micro-6.1-pool-x86_64
        sl-micro-6.1-devel-uyuni-client-x86_64
      ],
    'tumbleweed' =>
      %w[
        opensuse_tumbleweed-x86_64
        opensuse_tumbleweed-uyuni-client-x86_64
        opensuse_tumbleweed-uyuni-client-devel-x86_64
      ],
    'tumbleweed-client-tools-x86_64' => # CHECKED
      %w[
        opensuse_tumbleweed-uyuni-client-x86_64
        opensuse_tumbleweed-uyuni-client-devel-x86_64
      ],
    'ubuntu-2204' => # CHECKED
      %w[
        ubuntu-2204-pool-amd64-uyuni
        ubuntu-2204-amd64-main-security-uyuni
        ubuntu-2204-amd64-main-updates-uyuni
        ubuntu-2204-amd64-main-uyuni
        ubuntu-2204-amd64-universe-backports-uyuni
        ubuntu-2204-amd64-universe-security-uyuni
        ubuntu-2204-amd64-universe-updates-uyuni
        ubuntu-2204-amd64-universe-uyuni
        ubuntu-2204-amd64-uyuni-client-devel
      ],
    'ubuntu-2404' =>
      %w[
        ubuntu-2404-pool-amd64-uyuni
        ubuntu-2404-amd64-main-security-uyuni
        ubuntu-2404-amd64-main-updates-uyuni
        ubuntu-2404-amd64-main-uyuni
        ubuntu-2404-amd64-universe-backports-uyuni
        ubuntu-2404-amd64-universe-security-uyuni
        ubuntu-2404-amd64-universe-updates-uyuni
        ubuntu-2404-amd64-universe-uyuni
        ubuntu-2404-amd64-uyuni-client-devel
      ],
    'uyuni-proxy' => # CHECKED
      %w[
        opensuse_tumbleweed-x86_64
        opensuse_tumbleweed-uyuni-client-devel-x86_64
        uyuni-proxy-devel-tumbleweed-x86_64
      ]
    # There are no channels for Retail under Uyuni
  }
}.freeze

CLIENT_TOOLS_DEPENDENCIES_BY_BASE_CHANNEL = {
  'opensuse_tumbleweed-x86_64' => %w[
    dmidecode
    libunwind
    golang-github-prometheus-node_exporter
    golang-github-lusitaniae-apache_exporter
    prometheus-postgres_exporter
    golang-github-QubitProducts-exporter_exporter
    system-user-prometheus
    ansible
    python313-packaging
    python313-rpm
    ansible-core
    libgomp1
    python313-resolvelib
    librpmbuild10
  ]
}.freeze

# The timeouts are determining experimentally, by looking at the files in /var/log/rhn/reposync on the server
# Formula: (end date - startup date) * 2, rounded to upper 60 seconds
# Please keep this list sorted alphabetically
TIMEOUT_BY_CHANNEL_NAME = {
  'almalinux8-appstream-x86_64' => 1920,
  'almalinux8-uyuni-client-devel-x86_64' => 60,
  'almalinux8-x86_64' => 900,
  'almalinux8-x86_64-appstream' => 1740,
  'almalinux8-x86_64-extras' => 60,
  'almalinux9-appstream-x86_64' => 600,
  'almalinux9-uyuni-client-devel-x86_64' => 60,
  'almalinux9-x86_64' => 240,
  'almalinux9-x86_64-appstream' => 720,
  'almalinux9-x86_64-extras' => 60,
  'amazonlinux2023-uyuni-client-devel-x86_64' => 60,
  'amazonlinux2023-x86_64' => 2460,
  'centos-7-iso' => 540,
  'centos7-uyuni-client-devel-x86_64' => 60,
  'centos7-x86_64' => 960,
  'centos7-x86_64-extras' => 120,
  'debian-12-amd64-main-security-uyuni' => 240,
  'debian-12-amd64-main-updates-uyuni' => 120,
  'debian-12-amd64-uyuni-client-devel' => 60,
  'debian-12-main-security-amd64' => 300,
  'debian-12-main-updates-amd64' => 120,
  'debian-12-pool-amd64' => 8400,
  'debian-12-pool-amd64-uyuni' => 28_260,
  'devel-build-host-channel' => 120,
  'devel-debian-like-channel' => 120,
  'devel-rh-like-channel' => 120,
  'devel-suse-channel' => 120,
  'el9-pool-x86_64' => 60,
  'fake-base-channel-appstream' => 360,
  'fake-base-channel-debian-like' => 300,
  'fake-base-channel-rh-like' => 360,
  'fake-child-channel-i586' => 300,
  'fake-child-channel-suse-like' => 240,
  'fake-rpm-suse-channel' => 120,
  'fake-rpm-terminal-channel' => 360,
  'managertools-beta-el8-pool-x86_64-alma' => 60,
  'managertools-beta-el8-updates-x86_64-alma' => 60,
  'managertools-beta-el9-pool-x86_64' => 60,
  'managertools-beta-el9-pool-x86_64-alma' => 60,
  'managertools-beta-el9-pool-x86_64-amazon' => 60,
  'managertools-beta-el9-updates-x86_64' => 60,
  'managertools-beta-el9-updates-x86_64-alma' => 60,
  'managertools-beta-el9-updates-x86_64-amazon' => 60,
  'managertools-beta-sle15-pool-x86_64-sp4' => 60,
  'managertools-beta-sle15-updates-x86_64-sp4' => 60,
  'managertools-beta-ubuntu2204-updates-amd64' => 60,
  'managertools-beta-ubuntu2404-updates-amd64' => 60,
  'managertools-debian12-beta-updates-amd64' => 60,
  'managertools-debian12-updates-amd64' => 60,
  'managertools-el7-pool-x86_64-lbt7' => 60,
  'managertools-el7-updates-x86_64-lbt7' => 60,
  'managertools-el7-pool-x86_64-lbt' => 60,
  'managertools-el7-updates-x86_64-lbt' => 60,
  'managertools-el8-pool-x86_64' => 60,
  'managertools-el8-pool-x86_64-alma' => 60,
  'managertools-el8-updates-x86_64' => 60,
  'managertools-el8-updates-x86_64-alma' => 60,
  'managertools-el9-pool-x86_64' => 60,
  'managertools-el9-pool-x86_64-alma' => 60,
  'managertools-el9-pool-x86_64-amazon' => 60,
  'managertools-el9-pool-x86_64-rocky' => 60,
  'managertools-el9-updates-x86_64' => 60,
  'managertools-el9-updates-x86_64-alma' => 60,
  'managertools-el9-updates-x86_64-amazon' => 60,
  'managertools-el9-updates-x86_64-rocky' => 60,
  'managertools-sle12-pool-x86_64-sp5' => 60,
  'managertools-sle12-updates-x86_64-sp5' => 60,
  'managertools-sle15-pool-aarch64-opensuse-15.6' => 60,
  'managertools-sle15-pool-s390x-sp5' => 60,
  'managertools-sle15-pool-x86_64-sap-sp5' => 60,
  'managertools-sle15-pool-x86_64-sp3' => 60,
  'managertools-sle15-pool-x86_64-sp4' => 60,
  'managertools-sle15-pool-x86_64-sp5' => 60,
  'managertools-sle15-pool-x86_64-sp6' => 60,
  'managertools-sle15-pool-x86_64-sp7' => 60,
  'managertools-sle15-updates-aarch64-opensuse-15.6' => 60,
  'managertools-sle15-updates-s390x-sp5' => 60,
  'managertools-sle15-updates-x86_64-sap-sp5' => 60,
  'managertools-sle15-updates-x86_64-sp3' => 60,
  'managertools-sle15-updates-x86_64-sp4' => 60,
  'managertools-sle15-updates-x86_64-sp5' => 60,
  'managertools-sle15-updates-x86_64-sp6' => 60,
  'managertools-sle15-updates-x86_64-sp7' => 60,
  'managertools-ubuntu2204-updates-amd64' => 60,
  'managertools-ubuntu2404-updates-amd64' => 60,
  'opensuse-backports-15.6-updates-aarch64' => 300,
  'opensuse_leap15_6-aarch64' => 10_020,
  'opensuse_leap15_6-aarch64-backports-updates' => 420,
  'opensuse_leap15_6-aarch64-non-oss' => 60,
  'opensuse_leap15_6-aarch64-non-oss-updates' => 60,
  'opensuse_leap15_6-aarch64-sle-updates' => 4140,
  'opensuse_leap15_6-aarch64-updates' => 60,
  'opensuse-leap-15.6-pool-aarch64' => 2640,
  'opensuse-leap-15.6-updates-aarch64' => 60,
  'opensuse_leap15_6-uyuni-client-devel-aarch64' => 120,
  'opensuse_leap15_6-uyuni-client-devel-x86_64' => 120,
  'opensuse_leap15_6-uyuni-client-x86_64' => 120,
  'opensuse_leap15_6-x86_64' => 10_380,
  'opensuse_leap15_6-x86_64-backports-updates' => 360,
  'opensuse_leap15_6-x86_64-non-oss' => 60,
  'opensuse_leap15_6-x86_64-non-oss-updates' => 120,
  'opensuse_leap15_6-x86_64-sle-updates' => 5400,
  'opensuse_leap15_6-x86_64-updates' => 60,
  'opensuse_micro5_5-uyuni-client-devel-x86_64' => 120,
  'opensuse_micro5_5-uyuni-client-x86_64' => 120,
  'opensuse_micro5_5-x86_64' => 240,
  'opensuse_micro5_5-x86_64-sle-updates' => 5400,
  'opensuse-sle-15.6-updates-aarch64' => 3120,
  'oraclelinux9-appstream-x86_64' => 3480,
  'opensuse_tumbleweed-x86_64' => 12_600,
  'opensuse_tumbleweed-uyuni-client-x86_64' => 120,
  'opensuse_tumbleweed-uyuni-client-devel-x86_64' => 120,
  'oraclelinux9-uyuni-client-devel-x86_64' => 120,
  'oraclelinux9-x86_64' => 1620,
  'res-7-ltss-updates-x86_64' => 1020,
  'res7-x86_64' => 10_080,
  'res8-manager-tools-pool-x86_64-rocky' => 60,
  'res8-manager-tools-updates-x86_64-alma' => 240,
  'managertools-el8-updates-x86_64-rocky' => 60,
  'rhel-x86_64-server-7' => 60,
  'rocky-8-iso' => 1200,
  'rockylinux8-uyuni-client-devel-x86_64' => 120,
  'rockylinux-8-appstream-x86_64' => 1620,
  'rockylinux-8-extras-x86_64' => 420,
  'rockylinux-8-x86_64' => 900,
  'rockylinux9-uyuni-client-devel-x86_64' => 120,
  'rockylinux-9-appstream-x86_64' => 780,
  'rockylinux-9-extras-x86_64' => 120,
  'rockylinux-9-x86_64' => 180,
  'sle15-sp3-installer-updates-x86_64' => 60,
  'sle15-sp4-installer-updates-x86_64' => 60,
  'sle15-sp7-installer-updates-x86_64' => 60,
  'sle-micro-5.3-devel-uyuni-client-x86_64' => 120,
  'sle-micro-5.3-pool-x86_64' => 180,
  'sle-micro-5.3-updates-x86_64' => 360,
  'sle-micro-5.4-devel-uyuni-client-x86_64' => 120,
  'sle-micro-5.4-pool-x86_64' => 60,
  'sle-micro-5.4-updates-x86_64' => 120,
  'sle-micro-5.5-devel-uyuni-client-x86_64' => 120,
  'sle-micro-5.5-pool-x86_64' => 120,
  'sle-micro-5.5-updates-x86_64' => 840,
  'sle-module-basesystem15-sp3-pool-x86_64' => 240,
  'sle-module-basesystem15-sp3-updates-x86_64' => 720,
  'sle-module-basesystem15-sp4-pool-x86_64' => 180,
  'sle-module-basesystem15-sp4-pool-x86_64-proxy-4.3' => 60,
  'sle-module-basesystem15-sp4-pool-x86_64-smrbs-4.3' => 60,
  'sle-module-basesystem15-sp4-updates-x86_64' => 480,
  'sle-module-basesystem15-sp4-updates-x86_64-proxy-4.3' => 180,
  'sle-module-basesystem15-sp4-updates-x86_64-smrbs-4.3' => 180,
  'sle-module-basesystem15-sp5-pool-s390x' => 240,
  'sle-module-basesystem15-sp5-pool-x86_64' => 180,
  'sle-module-basesystem15-sp5-updates-s390x' => 1320,
  'sle-module-basesystem15-sp5-updates-x86_64' => 1080,
  'sle-module-basesystem15-sp6-pool-x86_64' => 120,
  'sle-module-basesystem15-sp6-updates-x86_64' => 420,
  'sle-module-basesystem15-sp7-pool-x86_64' => 180,
  'sle-module-basesystem15-sp7-updates-x86_64' => 240,
  'sle-module-containers15-sp4-pool-x86_64' => 60,
  'sle-module-containers15-sp4-pool-x86_64-proxy-4.3' => 60,
  'sle-module-containers15-sp4-pool-x86_64-smrbs-4.3' => 60,
  'sle-module-containers15-sp7-pool-x86_64' => 60,
  'sle-module-containers15-sp4-updates-x86_64' => 120,
  'sle-module-containers15-sp4-updates-x86_64-proxy-4.3' => 60,
  'sle-module-containers15-sp4-updates-x86_64-smrbs-4.3' => 60,
  'sle-module-containers15-sp7-updates-x86_64' => 60,
  'sle-module-desktop-applications15-sp3-pool-x86_64' => 300,
  'sle-module-desktop-applications15-sp3-updates-x86_64' => 120,
  'sle-module-desktop-applications15-sp4-pool-x86_64' => 180,
  'sle-module-desktop-applications15-sp4-updates-x86_64' => 60,
  'sle-module-desktop-applications15-sp5-pool-x86_64' => 120,
  'sle-module-desktop-applications15-sp5-updates-x86_64' => 60,
  'sle-module-desktop-applications15-sp6-pool-x86_64' => 120,
  'sle-module-desktop-applications15-sp6-updates-x86_64' => 60,
  'sle-module-desktop-applications15-sp7-pool-x86_64' => 120,
  'sle-module-desktop-applications15-sp7-updates-x86_64' => 60,
  'sle-module-devtools15-sp3-pool-x86_64' => 60,
  'sle-module-devtools15-sp3-updates-x86_64' => 420,
  'sle-module-devtools15-sp4-pool-x86_64' => 60,
  'sle-module-devtools15-sp4-updates-x86_64' => 420,
  'sle-module-devtools15-sp5-pool-x86_64' => 180,
  'sle-module-devtools15-sp5-updates-x86_64' => 960,
  'sle-module-devtools15-sp6-pool-x86_64' => 60,
  'sle-module-devtools15-sp6-updates-x86_64' => 360,
  'sle-module-devtools15-sp7-pool-x86_64' => 120,
  'sle-module-devtools15-sp7-updates-x86_64' => 240,
  'sle-module-public-cloud15-sp4-pool-x86_64' => 840,
  'sle-module-public-cloud15-sp4-updates-x86_64' => 600,
  'sle-module-public-cloud15-sp5-pool-x86_64' => 600,
  'sle-module-public-cloud15-sp5-updates-x86_64' => 420,
  'sle-module-public-cloud15-sp6-pool-x86_64' => 60,
  'sle-module-public-cloud15-sp6-updates-x86_64' => 60,
  'sle-module-public-cloud15-sp7-pool-x86_64' => 60,
  'sle-module-public-cloud15-sp7-updates-x86_64' => 60,
  'sle-module-python3-15-sp5-pool-x86_64' => 60,
  'sle-module-python3-15-sp5-updates-x86_64' => 60,
  'sle-module-python3-15-sp6-pool-x86_64' => 60,
  'sle-module-python3-15-sp6-updates-x86_64' => 60,
  'sle-module-systems-management-15-sp6-pool-x86_64' => 60,
  'sle-module-systems-management-15-sp6-updates-x86_64' => 60,
  'sle-module-python3-15-sp7-pool-x86_64' => 60,
  'sle-module-python3-15-sp7-updates-x86_64' => 60,
  'sle-module-systems-management-15-sp7-pool-x86_64' => 60,
  'sle-module-systems-management-15-sp7-updates-x86_64' => 60,
  'sle-module-server-applications15-sp3-pool-x86_64' => 60,
  'sle-module-server-applications15-sp3-updates-x86_64' => 120,
  'sle-module-server-applications15-sp4-pool-x86_64' => 60,
  'sle-module-server-applications15-sp4-pool-x86_64-smrbs-4.3' => 60,
  'sle-module-server-applications15-sp4-updates-x86_64' => 180,
  'sle-module-server-applications15-sp4-updates-x86_64-proxy-4.3' => 60,
  'sle-module-server-applications15-sp4-updates-x86_64-smrbs-4.3' => 60,
  'sle-module-server-applications15-sp5-pool-s390x' => 60,
  'sle-module-server-applications15-sp5-pool-x86_64' => 60,
  'sle-module-server-applications15-sp5-updates-s390x' => 120,
  'sle-module-server-applications15-sp5-updates-x86_64' => 60,
  'sle-module-server-applications15-sp6-pool-x86_64' => 60,
  'sle-module-server-applications15-sp6-updates-x86_64' => 120,
  'sle-module-server-applications15-sp7-pool-x86_64' => 60,
  'sle-module-server-applications15-sp7-updates-x86_64' => 60,
  'sles12-sp5-ltss-updates-x86_64' => 420,
  'sle-product-sles15-sp3-ltss-updates-x86_64' => 1620,
  'sle-product-sles15-sp3-pool-x86_64' => 60,
  'sle-product-sles15-sp3-updates-x86_64' => 60,
  'sle-product-sles15-sp4-ltss-updates-x86_64' => 900,
  'sle-product-sles15-sp4-pool-x86_64' => 60,
  'sle-product-sles15-sp4-updates-x86_64' => 60,
  'sle-product-sles15-sp5-pool-s390x' => 60,
  'sle-product-sles15-sp5-pool-x86_64' => 60,
  'sle-product-sles15-sp5-updates-s390x' => 60,
  'sle-product-sles15-sp5-updates-x86_64' => 60,
  'sle-product-sles15-sp6-pool-x86_64' => 60,
  'sle-product-sles15-sp6-updates-x86_64' => 60,
  'sle-product-sles15-sp6-ltss-updates-x86_64' => 60,
  'sle-product-sles15-sp7-pool-x86_64' => 60,
  'sle-product-sles15-sp7-updates-x86_64' => 60,
  'sles12-sp5-installer-updates-x86_64' => 60,
  'sles12-sp5-pool-x86_64' => 120,
  'sles12-sp5-updates-x86_64' => 1920,
  'sles12-sp5-uyuni-client-devel-x86_64' => 120,
  'sles15-sp3-devel-uyuni-client-x86_64' => 120,
  'sles15-sp4-devel-uyuni-client-x86_64' => 120,
  'sles15-sp5-devel-uyuni-client-x86_64' => 120,
  'sles15-sp6-devel-uyuni-client-x86_64' => 120,
  'sles15-sp7-devel-uyuni-client-x86_64' => 60,
  'sll-9-updates-x86_64' => 2580,
  'sll-as-9-updates-x86_64' => 2460,
  'sll-cb-9-updates-x86_64' => 2160,
  'sl-micro-6.0-devel-uyuni-client-x86_64' => 120,
  'sl-micro-6.0-pool-x86_64' => 600,
  'managertools-sl-micro-6.0-x86_64' => 60,
  'sl-micro-6.1-devel-uyuni-client-x86_64' => 120,
  'sl-micro-6.1-pool-x86_64' => 300,
  'managertools-sl-micro-6.1-x86_64' => 60,
  'suse-manager-proxy-5.0-pool-x86_64' => 60,
  'suse-manager-proxy-5.0-pool-x86_64-sp6' => 60,
  'suse-manager-proxy-5.0-updates-x86_64' => 60,
  'suse-manager-proxy-5.0-updates-x86_64-sp6' => 60,
  'suse-manager-retail-branch-server-5.0-pool-x86_64' => 60,
  'suse-manager-retail-branch-server-5.0-pool-x86_64-sp6' => 60,
  'suse-manager-retail-branch-server-5.0-updates-x86_64' => 60,
  'suse-manager-retail-branch-server-5.0-updates-x86_64-sp6' => 60,
  'suse-microos-5.1-devel-uyuni-client-x86_64' => 120,
  'suse-multi-linux-manager-proxy-5.1-x86_64' => 60, # for slmicro6.1
  'suse-multi-linux-manager-proxy-5.2-x86_64' => 60, # for slmicro6.1
  'suse-multi-linux-manager-proxy-sle-5.1-pool-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-proxy-sle-5.1-updates-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-proxy-sle-5.2-pool-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-proxy-sle-5.2-updates-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-retail-branch-server-5.1-x86_64' => 60, # for slmicro6.1
  'suse-multi-linux-manager-retail-branch-server-5.2-x86_64' => 60, # for slmicro6.1
  'suse-multi-linux-manager-retail-branch-server-sle-5.1-pool-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-retail-branch-server-sle-5.1-updates-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-retail-branch-server-sle-5.2-pool-x86_64-sp7' => 60, # for sles15sp7
  'suse-multi-linux-manager-retail-branch-server-sle-5.2-updates-x86_64-sp7' => 60, # for sles15sp7
  'suse-microos-5.1-pool-x86_64' => 120,
  'suse-microos-5.1-updates-x86_64' => 1080,
  'suse-microos-5.2-devel-uyuni-client-x86_64' => 120,
  'suse-microos-5.2-pool-x86_64' => 60,
  'suse-microos-5.2-updates-x86_64' => 120,
  'test-child-channel-x86_64' => 360,
  'ubuntu-2204-amd64-main-amd64' => 540,
  'ubuntu-2204-amd64-main-security-amd64' => 2640,
  'ubuntu-2204-amd64-main-security-uyuni' => 2040,
  'ubuntu-2204-amd64-main-updates-amd64' => 420,
  'ubuntu-2204-amd64-main-updates-uyuni' => 300,
  'ubuntu-2204-amd64-main-uyuni' => 780,
  'ubuntu-2204-amd64-universe-backports-uyuni' => 60,
  'ubuntu-2204-amd64-universe-security-uyuni' => 1020,
  'ubuntu-2204-amd64-universe-updates-uyuni' => 240,
  'ubuntu-2204-amd64-universe-uyuni' => 24_000,
  'ubuntu-2204-amd64-uyuni-client-devel' => 120,
  'ubuntu-2204-pool-amd64-uyuni' => 60,
  'ubuntu-2404-amd64-main-amd64' => 540,
  'ubuntu-2404-amd64-main-security-amd64' => 120,
  'ubuntu-2404-amd64-main-security-uyuni' => 2040,
  'ubuntu-2404-amd64-main-updates-amd64' => 1620,
  'ubuntu-2404-amd64-main-updates-uyuni' => 300,
  'ubuntu-2404-amd64-main-uyuni' => 780,
  'ubuntu-2404-amd64-universe-backports-uyuni' => 60,
  'ubuntu-2404-amd64-universe-security-uyuni' => 1020,
  'ubuntu-2404-amd64-universe-updates-uyuni' => 240,
  'ubuntu-2404-amd64-universe-uyuni' => 24_000,
  'ubuntu-2404-amd64-uyuni-client-devel' => 120,
  'ubuntu-2404-pool-amd64-uyuni' => 60,
  'uyuni-proxy-devel-tumbleweed-x86_64' => 120,
  'uyuni-proxy-stable-tumbleweed-x86_64' => 120
}.freeze

EMPTY_CHANNELS = %w[
  el9-pool-x86_64
  suse-multi-linux-manager-proxy-sle-5.1-updates-x86_64-sp7
  suse-multi-linux-manager-proxy-sle-5.2-updates-x86_64-sp7
  suse-multi-linux-manager-retail-branch-server-sle-5.1-updates-x86_64-sp7
  suse-multi-linux-manager-retail-branch-server-sle-5.2-updates-x86_64-sp7
  managertools-sle15-updates-x86_64-sp7
  sle-product-sles15-sp6-ltss-updates-x86_64
  suse-manager-proxy-5.0-updates-x86_64
  suse-manager-retail-branch-server-5.0-updates-x86_64
  sle-module-suse-manager-retail-branch-server-4.3-updates-x86_64
  fake-base-channel-suse-like
  fake-base-channel-i586
  test-base-channel-x86_64
  managertools-sle15-updates-x86_64-sp4
  managertools-beta-sle15-updates-x86_64-sp4
].freeze
