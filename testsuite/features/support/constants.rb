# Copyright (c) 2019-2024 SUSE LLC
# Licensed under the terms of the MIT license.

ENV_VAR_BY_HOST = {
  'localhost' => 'HOSTNAME',
  'proxy' => 'PROXY',
  'server' => 'SERVER',
  'kvm_server' => 'VIRTHOST_KVM_URL',
  'sle_minion' => 'MINION',
  'ssh_minion' => 'SSH_MINION',
  'rhlike_minion' => 'RHLIKE_MINION',
  'deblike_minion' => 'DEBLIKE_MINION',
  'build_host' => 'BUILD_HOST',
  # Build Validation environment
  'sle12sp5_minion' => 'SLE12SP5_MINION',
  'sle12sp5_ssh_minion' => 'SLE12SP5_SSHMINION',
  'sle12sp5_buildhost' => 'SLE12SP5_BUILDHOST',
  'sle15sp1_minion' => 'SLE15SP1_MINION',
  'sle15sp1_ssh_minion' => 'SLE15SP1_SSHMINION',
  'sle15sp2_minion' => 'SLE15SP2_MINION',
  'sle15sp2_ssh_minion' => 'SLE15SP2_SSHMINION',
  'sle15sp3_minion' => 'SLE15SP3_MINION',
  'sle15sp3_ssh_minion' => 'SLE15SP3_SSHMINION',
  'sle15sp4_minion' => 'SLE15SP4_MINION',
  'sle15sp4_ssh_minion' => 'SLE15SP4_SSHMINION',
  'sle15sp4_buildhost' => 'SLE15SP4_BUILDHOST',
  'monitoring_server' => 'MONITORING_SERVER',
  'sle15sp5_minion' => 'SLE15SP5_MINION',
  'sle15sp5_ssh_minion' => 'SLE15SP5_SSHMINION',
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
  'alma8_minion' => 'ALMA8_MINION',
  'alma8_ssh_minion' => 'ALMA8_SSHMINION',
  'alma9_minion' => 'ALMA9_MINION',
  'alma9_ssh_minion' => 'ALMA9_SSHMINION',
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
  'ubuntu2004_minion' => 'UBUNTU2004_MINION',
  'ubuntu2004_ssh_minion' => 'UBUNTU2004_SSHMINION',
  'ubuntu2204_minion' => 'UBUNTU2204_MINION',
  'ubuntu2204_ssh_minion' => 'UBUNTU2204_SSHMINION',
  'debian10_minion' => 'DEBIAN10_MINION',
  'debian10_ssh_minion' => 'DEBIAN10_SSHMINION',
  'debian11_minion' => 'DEBIAN11_MINION',
  'debian11_ssh_minion' => 'DEBIAN11_SSHMINION',
  'debian12_minion' => 'DEBIAN12_MINION',
  'debian12_ssh_minion' => 'DEBIAN12_SSHMINION',
  'opensuse154arm_minion' => 'OPENSUSE154ARM_MINION',
  'opensuse154arm_ssh_minion' => 'OPENSUSE154ARM_SSHMINION',
  'opensuse155arm_minion' => 'OPENSUSE155ARM_MINION',
  'opensuse155arm_ssh_minion' => 'OPENSUSE155ARM_SSHMINION',
  'sle15sp5s390_minion' => 'SLE15SP5S390_MINION',
  'sle15sp5s390_ssh_minion' => 'SLE15SP5S390_SSHMINION',
  'salt_migration_minion' => 'SALT_MIGRATION_MINION'
}.freeze

# TODO: the values for pxeboot_minion, sle12sp5_terminal, sle15sp4_terminal, and proxy can now be set in sumaform
#       remove them from this array when we read them from .bashrc
PRIVATE_ADDRESSES = {
  'network'           => '0',
  'pxeboot_minion'    => '4',
  'sle12sp5_terminal' => '5',
  'sle15sp4_terminal' => '6',
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
  'sle15sp1_minion' => 'bison',
  'sle15sp1_ssh_minion' => 'bison',
  'sle15sp2_minion' => 'bison',
  'sle15sp2_ssh_minion' => 'bison',
  'sle15sp3_minion' => 'bison',
  'sle15sp3_ssh_minion' => 'bison',
  'sle15sp4_minion' => 'bison',
  'sle15sp4_ssh_minion' => 'bison',
  'sle15sp5_minion' => 'bison',
  'sle15sp5_ssh_minion' => 'bison',
  'slemicro51_minion' => 'ethtool',
  'slemicro51_ssh_minion' => 'ethtool',
  'slemicro52_minion' => 'ethtool',
  'slemicro52_ssh_minion' => 'ethtool',
  'slemicro53_minion' => 'ethtool',
  'slemicro53_ssh_minion' => 'ethtool',
  'slemicro54_minion' => 'ethtool',
  'slemicro54_ssh_minion' => 'ethtool',
  'slemicro55_minion' => 'ethtool',
  'slemicro55_ssh_minion' => 'ethtool',
  'alma8_minion' => 'autoconf',
  'alma8_ssh_minion' => 'autoconf',
  'alma9_minion' => 'autoconf',
  'alma9_ssh_minion' => 'autoconf',
  'centos7_minion' => 'autoconf',
  'centos7_ssh_minion' => 'autoconf',
  'liberty9_minion' => 'autoconf',
  'liberty9_ssh_minion' => 'autoconf',
  'oracle9_minion' => 'autoconf',
  'oracle9_ssh_minion' => 'autoconf',
  'rhel9_minion' => 'autoconf',
  'rhel9_ssh_minion' => 'autoconf',
  'rocky8_minion' => 'autoconf',
  'rocky8_ssh_minion' => 'autoconf',
  'rocky9_minion' => 'autoconf',
  'rocky9_ssh_minion' => 'autoconf',
  'ubuntu2004_minion' => 'bison',
  'ubuntu2004_ssh_minion' => 'bison',
  'ubuntu2204_minion' => 'bison',
  'ubuntu2204_ssh_minion' => 'bison',
  'debian10_minion' => 'bison',
  'debian10_ssh_minion' => 'bison',
  'debian11_minion' => 'bison',
  'debian11_ssh_minion' => 'bison',
  'debian12_minion' => 'bison',
  'debian12_ssh_minion' => 'bison',
  'opensuse154arm_minion' => 'bison',
  'opensuse154arm_ssh_minion' => 'bison',
  'opensuse155arm_minion' => 'bison',
  'opensuse155arm_ssh_minion' => 'bison',
  'sle15sp5s390_minion' => 'bison',
  'sle15sp5s390_ssh_minion' => 'bison',
  'salt_migration_minion' => 'bison'
}.freeze

# Names of our base/parent channels
# The keys are the Twopence targets
# The values can be found in the webUI under Software -> Manage -> Channels -> Create Channel
# The required product has to be synced before
# Then take a look at the Parent Channel selections
BASE_CHANNEL_BY_CLIENT = {
  'SUSE Manager' => {
    'proxy_container' => 'SLE-Micro-5.5-Pool for x86_64',
    'proxy_traditional' => 'SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64',
    'sle_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'ssh_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'rhlike_minion' => 'RHEL8-Pool for x86_64',
    'deblike_minion' => 'ubuntu-2004-amd64-main for amd64',
    'pxeboot_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'buildhost' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle12sp5_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_buildhost' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_terminal' => 'SLES12-SP5-Pool for x86_64',
    'sle15sp1_minion' => 'SLE-Product-SLES15-SP1-Pool for x86_64',
    'sle15sp1_ssh_minion' => 'SLE-Product-SLES15-SP1-Pool for x86_64',
    'sle15sp2_minion' => 'SLE-Product-SLES15-SP2-Pool for x86_64',
    'sle15sp2_ssh_minion' => 'SLE-Product-SLES15-SP2-Pool for x86_64',
    'sle15sp3_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp3_ssh_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp4_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_ssh_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_buildhost' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'monitoring_server' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_terminal' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp5_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
    'sle15sp5_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
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
    'alma8_minion' => 'almalinux8 for x86_64',
    'alma8_ssh_minion' => 'almalinux8 for x86_64',
    'alma9_minion' => 'almalinux9 for x86_64',
    'alma9_ssh_minion' => 'almalinux9 for x86_64',
    'centos7_minion' => 'RHEL x86_64 Server 7',
    'centos7_ssh_minion' => 'RHEL x86_64 Server 7',
    'liberty9_minion' => 'EL9-Pool for x86_64',
    'liberty9_ssh_minion' => 'EL9-Pool for x86_64',
    'oracle9_minion' => 'oraclelinux9 for x86_64',
    'oracle9_ssh_minion' => 'oraclelinux9 for x86_64',
    'rhel9_minion' => 'EL9-Pool for x86_64',
    'rhel9_ssh_minion' => 'EL9-Pool for x86_64',
    'rocky8_minion' => 'RHEL8-Pool for x86_64',
    'rocky8_ssh_minion' => 'RHEL8-Pool for x86_64',
    'rocky9_minion' => 'rockylinux-9 for x86_64',
    'rocky9_ssh_minion' => 'rockylinux-9 for x86_64',
    'ubuntu2004_minion' => 'ubuntu-2004-amd64-main for amd64',
    'ubuntu2004_ssh_minion' => 'ubuntu-2004-amd64-main for amd64',
    'ubuntu2204_minion' => 'ubuntu-2204-amd64-main for amd64',
    'ubuntu2204_ssh_minion' => 'ubuntu-2204-amd64-main for amd64',
    'debian10_minion' => 'debian-10-pool for amd64',
    'debian10_ssh_minion' => 'debian-10-pool for amd64',
    'debian11_minion' => 'debian-11-pool for amd64',
    'debian11_ssh_minion' => 'debian-11-pool for amd64',
    'debian12_minion' => 'debian-12-pool for amd64',
    'debian12_ssh_minion' => 'debian-12-pool for amd64',
    'opensuse154arm_minion' => 'openSUSE-Leap-15.4-Pool for aarch64',
    'opensuse154arm_ssh_minion' => 'openSUSE-Leap-15.4-Pool for aarch64',
    'opensuse155arm_minion' => 'openSUSE-Leap-15.5-Pool for aarch64',
    'opensuse155arm_ssh_minion' => 'openSUSE-Leap-15.5-Pool for aarch64',
    'sle15sp5s390_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'sle15sp5s390_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'salt_migration_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64'
  },
  'Uyuni' => {
    'proxy_container' => 'openSUSE Leap Micro 5.5 (x86_64)',
    'proxy_traditional' => 'openSUSE Leap 15.5 (x86_64)',
    'sle_minion' => 'openSUSE Leap 15.5 (x86_64)',
    'ssh_minion' => 'openSUSE Leap 15.5 (x86_64)',
    'rhlike_minion' => 'RHEL8-Pool for x86_64',
    'deblike_minion' => 'Ubuntu 20.04 LTS AMD64 Base for Uyuni',
    'pxeboot_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'buildhost' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle12sp5_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_buildhost' => 'SLES12-SP5-Pool for x86_64',
    'sle12sp5_terminal' => 'SLES12-SP5-Pool for x86_64',
    'sle15sp1_minion' => 'SLE-Product-SLES15-SP1-Pool for x86_64',
    'sle15sp1_ssh_minion' => 'SLE-Product-SLES15-SP1-Pool for x86_64',
    'sle15sp2_minion' => 'SLE-Product-SLES15-SP2-Pool for x86_64',
    'sle15sp2_ssh_minion' => 'SLE-Product-SLES15-SP2-Pool for x86_64',
    'sle15sp3_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp3_ssh_minion' => 'SLE-Product-SLES15-SP3-Pool for x86_64',
    'sle15sp4_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_ssh_minion' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_buildhost' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'monitoring_server' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp4_terminal' => 'SLE-Product-SLES15-SP4-Pool for x86_64',
    'sle15sp5_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
    'sle15sp5_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64',
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
    'alma8_minion' => 'AlmaLinux 8 (x86_64)',
    'alma8_ssh_minion' => 'AlmaLinux 8 (x86_64)',
    'alma9_minion' => 'AlmaLinux 9 (x86_64)',
    'alma9_ssh_minion' => 'AlmaLinux 9 (x86_64)',
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
    'ubuntu2004_minion' => 'Ubuntu 20.04 LTS AMD64 Base for Uyuni',
    'ubuntu2004_ssh_minion' => 'Ubuntu 20.04 LTS AMD64 Base for Uyuni',
    'ubuntu2204_minion' => 'Ubuntu 22.04 LTS AMD64 Base for Uyuni',
    'ubuntu2204_ssh_minion' => 'Ubuntu 22.04 LTS AMD64 Base for Uyuni',
    'debian10_minion' => 'Debian 10 (buster) pool for amd64 for Uyuni',
    'debian10_ssh_minion' => 'Debian 10 (buster) pool for amd64 for Uyuni',
    'debian11_minion' => 'Debian 11 (bullseye) pool for amd64 for Uyuni',
    'debian11_ssh_minion' => 'Debian 11 (bullseye) pool for amd64 for Uyuni',
    'debian12_minion' => 'Debian 12 (bookworm) pool for amd64 for Uyuni',
    'debian12_ssh_minion' => 'Debian 12 (bookworm) pool for amd64 for Uyuni',
    'opensuse154arm_minion' => 'openSUSE Leap 15.4 (aarch64)',
    'opensuse154arm_ssh_minion' => 'openSUSE Leap 15.4 (aarch64)',
    'opensuse155arm_minion' => 'openSUSE Leap 15.5 (aarch64)',
    'opensuse155arm_ssh_minion' => 'openSUSE Leap 15.5 (aarch64)',
    'sle15sp5s390_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'sle15sp5s390_ssh_minion' => 'SLE-Product-SLES15-SP5-Pool for s390x',
    'salt_migration_minion' => 'SLE-Product-SLES15-SP5-Pool for x86_64'
  },
  'Fake' => {
    'sle_minion' => 'Fake-Base-Channel-SUSE-like',
    'pxeboot_minion' => 'Fake-Base-Channel-SUSE-like',
    'proxy' => 'Fake-Base-Channel-SUSE-like',
    'buildhost' => 'Fake-Base-Channel-SUSE-like'
  }
}.freeze

# Used for creating activation keys
# The keys are the values of BASE_CHANNEL_BY_CLIENT
# SUMA: The values can be found under Admin -> Setup Wizard -> Products
# Select the desired product and have a look at its product channels
# The required product has to be synced before
# Uyuni: You have to use `spacewalk-common-channels -l` to get the proper values
LABEL_BY_BASE_CHANNEL = {
  'SUSE Manager' => {
    'SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
    'SLES12-SP5-Pool for x86_64' => 'sles12-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP1-Pool for x86_64' => 'sle-product-sles15-sp1-pool-x86_64',
    'SLE-Product-SLES15-SP2-Pool for x86_64' => 'sle-product-sles15-sp2-pool-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'sle-product-sles15-sp5-pool-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'almalinux8 for x86_64' => 'no-appstream-alma-8-result-almalinux8-x86_64',
    'almalinux9 for x86_64' => 'no-appstream-alma-9-result-almalinux9-x86_64',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
    'EL9-Pool for x86_64' => 'no-appstream-liberty-9-result-el9-pool-x86_64',
    'oraclelinux9 for x86_64' => 'no-appstream-oracle-9-result-oraclelinux9-x86_64',
    'RHEL8-Pool for x86_64' => 'no-appstream-8-result-rhel8-pool-x86_64',
    'rockylinux-9 for x86_64' => 'no-appstream-9-result-rockylinux-9-x86_64',
    'ubuntu-2004-amd64-main for amd64' => 'ubuntu-2004-amd64-main-amd64',
    'ubuntu-2204-amd64-main for amd64' => 'ubuntu-2204-amd64-main-amd64',
    'debian-10-pool for amd64' => 'debian-10-pool-amd64',
    'debian-11-pool for amd64' => 'debian-11-pool-amd64',
    'debian-12-pool for amd64' => 'debian-12-pool-amd64',
    'openSUSE-Leap-15.4-Pool for aarch64' => 'opensuse-leap-15.4-pool-aarch64',
    'openSUSE-Leap-15.5-Pool for aarch64' => 'opensuse-leap-15.5-pool-aarch64'
  },
  'Uyuni' => {
    'openSUSE Leap 15.4 (x86_64)' => 'opensuse_leap15_4-x86_64',
    'openSUSE Leap 15.5 (x86_64)' => 'opensuse_leap15_5-x86_64',
    'SLES12-SP5-Pool for x86_64' => 'sles12-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP1-Pool for x86_64' => 'sle-product-sles15-sp1-pool-x86_64',
    'SLE-Product-SLES15-SP2-Pool for x86_64' => 'sle-product-sles15-sp2-pool-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'sle-product-sles15-sp5-pool-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'AlmaLinux 8 (x86_64)' => 'no-appstream-alma-8-result-almalinux8-x86_64',
    'AlmaLinux 9 (x86_64)' => 'no-appstream-alma-9-result-almalinux9-x86_64',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'CentOS 7 (x86_64)' => 'centos7-x86_64',
    'EL9-Pool for x86_64' => 'no-appstream-liberty-9-result-el9-pool-x86_64',
    'Oracle Linux 9 (x86_64)' => 'no-appstream-oracle-9-result-oraclelinux9-x86_64',
    'Rocky Linux 8 (x86_64)' => 'no-appstream-8-result-rockylinux8-x86_64',
    'Rocky Linux 9 (x86_64)' => 'no-appstream-9-result-rockylinux-9-x86_64',
    'Ubuntu 20.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2004-pool-amd64-uyuni',
    'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => 'ubuntu-22.04-pool-amd64-uyuni',
    'Debian 10 (buster) pool for amd64 for Uyuni' => 'debian-10-pool-amd64-uyuni',
    'Debian 11 (bullseye) pool for amd64 for Uyuni' => 'debian-11-pool-amd64-uyuni',
    'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian-12-pool-amd64-uyuni',
    'openSUSE Leap 15.4 (aarch64)' => 'opensuse_leap15_4-aarch64',
    'openSUSE Leap 15.5 (aarch64)' => 'opensuse_leap15_5-aarch64'
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
    'SLE-Product-SLES15-SP1-Pool for x86_64' => 'SLE-15-SP1-x86_64',
    'SLE-Product-SLES15-SP2-Pool for x86_64' => 'SLE-15-SP2-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'SLE-15-SP3-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'SLE-15-SP4-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'SLE-15-SP5-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'SLE-15-SP5-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'SLE-MICRO-5.1-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'SLE-MICRO-5.2-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'SLE-MICRO-5.3-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'SLE-MICRO-5.4-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'SLE-MICRO-5.5-x86_64',
    'almalinux8 for x86_64' => 'almalinux-8-x86_64',
    'almalinux9 for x86_64' => 'almalinux-9-x86_64',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'RHEL x86_64 Server 7' => 'RES7-x86_64',
    'EL9-Pool for x86_64' => 'SUSE-LibertyLinux9-x86_64',
    'oraclelinux9 for x86_64' => 'oracle-9-x86_64',
    'RHEL8-Pool for x86_64' => 'SLE-ES8-x86_64',
    'rockylinux-9 for x86_64' => 'rockylinux-9-x86_64',
    'ubuntu-2004-amd64-main for amd64' => 'ubuntu-20.04-amd64',
    'ubuntu-2204-amd64-main for amd64' => 'ubuntu-22.04-amd64',
    'debian-10-pool for amd64' => 'debian10-amd64',
    'debian-11-pool for amd64' => 'debian11-amd64',
    'debian-12-pool for amd64' => 'debian12-amd64',
    'openSUSE-Leap-15.4-Pool for aarch64' => 'openSUSE-Leap-15.4-aarch64',
    'openSUSE-Leap-15.5-Pool for aarch64' => 'openSUSE-Leap-15.5-aarch64'
  },
  'Uyuni' => {
    'openSUSE Leap 15.4 (x86_64)' => 'openSUSE-Leap-15.4-x86_64-uyuni',
    'openSUSE Leap 15.5 (x86_64)' => 'openSUSE-Leap-15.5-x86_64-uyuni',
    'SLES12-SP5-Pool for x86_64' => 'SLE-12-SP5-x86_64',
    'SLE-Product-SLES15-SP1-Pool for x86_64' => 'SLE-15-SP1-x86_64',
    'SLE-Product-SLES15-SP2-Pool for x86_64' => 'SLE-15-SP2-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'SLE-15-SP3-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'SLE-15-SP4-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'SLE-15-SP5-x86_64',
    'SLE-Product-SLES15-SP5-Pool for s390x' => 'SLE-15-SP5-s390x',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'SLE-MICRO-5.1-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'SLE-MICRO-5.2-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'SLE-MICRO-5.3-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'SLE-MICRO-5.4-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'SLE-MICRO-5.5-x86_64',
    'AlmaLinux 8 (x86_64)' => 'almalinux-8-x86_64-uyuni',
    'AlmaLinux 9 (x86_64)' => 'almalinux-9-x86_64-uyuni',
    'Fake-Base-Channel-SUSE-like' => 'fake-base-channel-suse-like',
    'CentOS 7 (x86_64)' => 'centos-7-x86_64-uyuni',
    'EL9-Pool for x86_64' => 'SUSE-LibertyLinux9-x86_64',
    'Oracle Linux 9 (x86_64)' => 'oracle-9-x86_64-uyuni',
    'Rocky Linux 8 (x86_64)' => 'rockylinux8-x86_64-uyuni',
    'Rocky Linux 9 (x86_64)' => 'rockylinux9-x86_64-uyuni',
    'Ubuntu 20.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2004-amd64-uyuni',
    'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => 'ubuntu-2204-amd64-uyuni',
    'Debian 10 (buster) pool for amd64 for Uyuni' => 'debian10-amd64-uyuni',
    'Debian 11 (bullseye) pool for amd64 for Uyuni' => 'debian11-amd64-uyuni',
    'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian12-amd64-uyuni',
    'openSUSE Leap 15.4 (aarch64)' => 'openSUSE-Leap-15.4-aarch64-uyuni',
    'openSUSE Leap 15.5 (aarch64)' => 'openSUSE-Leap-15.5-aarch64-uyuni'
  }
}.freeze

# Used for creating bootstrap repositories
# SUMA: The values can be found under Admin -> Setup Wizard -> Products
# Select the desired product and have a look at its product channels
# The required product has to be synced before
# Uyuni: You have to use `spacewalk-common-channels -l` with the appended architecture
# e.g. almalinux9 -> almalinux9-x86_64
PARENT_CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL = {
  'SUSE Manager' => {
    'SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
    'SLES12-SP5-Pool for x86_64' => nil,
    'SLE-Product-SLES15-SP1-Pool for x86_64' => 'sle-product-sles15-sp1-pool-x86_64',
    'SLE-Product-SLES15-SP2-Pool for x86_64' => 'sle-product-sles15-sp2-pool-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'almalinux8 for x86_64' => nil,
    'almalinux9 for x86_64' => nil,
    'Fake-Base-Channel-SUSE-like' => nil,
    'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
    'oraclelinux9 for x86_64' => nil,
    'RHEL8-Pool for x86_64' => nil,
    'rockylinux-9 for x86_64' => nil,
    'ubuntu-2004-amd64-main for amd64' => nil,
    'ubuntu-2204-amd64-main for amd64' => nil,
    'debian-10-pool for amd64' => 'debian-10-pool-amd64',
    'debian-11-pool for amd64' => 'debian-11-pool-amd64',
    'debian-12-pool for amd64' => 'debian-12-pool-amd64',
    'openSUSE-Leap-15.4-Pool for aarch64' => nil,
    'openSUSE-Leap-15.5-Pool for aarch64' => nil
  },
  'Uyuni' => {
    'openSUSE Leap 15.4 (x86_64)' => nil,
    'openSUSE Leap 15.5 (x86_64)' => nil,
    'SLES12-SP5-Pool for x86_64' => nil,
    'SLE-Product-SLES15-SP1-Pool for x86_64' => 'sle-product-sles15-sp1-pool-x86_64',
    'SLE-Product-SLES15-SP2-Pool for x86_64' => 'sle-product-sles15-sp2-pool-x86_64',
    'SLE-Product-SLES15-SP3-Pool for x86_64' => 'sle-product-sles15-sp3-pool-x86_64',
    'SLE-Product-SLES15-SP4-Pool for x86_64' => 'sle-product-sles15-sp4-pool-x86_64',
    'SLE-Product-SLES15-SP5-Pool for x86_64' => 'sle-product-sles15-sp5-pool-x86_64',
    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
    'SLE-Micro-5.5-Pool for x86_64' => 'sle-micro-5.5-pool-x86_64',
    'almalinux8 for x86_64' => nil,
    'almalinux9 for x86_64' => nil,
    'Fake-Base-Channel-SUSE-like' => nil,
    'CentOS 7 (x86_64)' => 'centos-7-x86_64-uyuni',
    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
    'Oracle Linux 9 (x86_64)' => nil,
    'Rocky Linux 8 (x86_64)' => nil,
    'Rocky Linux 9 (x86_64)' => nil,
    'Ubuntu 20.04 LTS AMD64 Base for Uyuni' => nil,
    'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => nil,
    'Debian 10 (buster) pool for amd64 for Uyuni' => 'debian10-amd64-uyuni',
    'Debian 11 (bullseye) pool for amd64 for Uyuni' => 'debian11-amd64-uyuni',
    'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian12-amd64-uyuni',
    'openSUSE Leap 15.4 (aarch64)' => nil,
    'openSUSE Leap 15.5 (aarch64)' => nil
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
  'sle15sp1_minion' => 'x86_64',
  'sle15sp1_ssh_minion' => 'x86_64',
  'sle15sp2_minion' => 'x86_64',
  'sle15sp2_ssh_minion' => 'x86_64',
  'sle15sp3_minion' => 'x86_64',
  'sle15sp3_ssh_minion' => 'x86_64',
  'sle15sp4_minion' => 'x86_64',
  'sle15sp4_ssh_minion' => 'x86_64',
  'sle15sp5_minion' => 'x86_64',
  'sle15sp5_ssh_minion' => 'x86_64',
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
  'alma8_minion' => 'x86_64',
  'alma8_ssh_minion' => 'x86_64',
  'alma9_minion' => 'x86_64',
  'alma9_ssh_minion' => 'x86_64',
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
  'ubuntu2004_minion' => 'amd64',
  'ubuntu2004_ssh_minion' => 'amd64',
  'ubuntu2204_minion' => 'amd64',
  'ubuntu2204_ssh_minion' => 'amd64',
  'debian10_minion' => 'amd64',
  'debian10_ssh_minion' => 'amd64',
  'debian11_minion' => 'amd64',
  'debian11_ssh_minion' => 'amd64',
  'debian12_minion' => 'amd64',
  'debian12_ssh_minion' => 'amd64',
  'opensuse154arm_minion' => 'aarch64',
  'opensuse154arm_ssh_minion' => 'aarch64',
  'opensuse155arm_minion' => 'aarch64',
  'opensuse155arm_ssh_minion' => 'aarch64',
  'sle15sp5s390_minion' => 's390x',
  'sle15sp5s390_ssh_minion' => 's390x'
}.freeze

# Explanations:
# - SLED channels for SUMA tools were removed as we are not currently synchronizing them
# - 'default' is required for auto-installation tests.
# - '# CHECKED' means that we verified that the list of channels matches the results in /var/log/rhn/reposync,
#   and that we took the occasion to evaluate a reasonable timeout for them
CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION = {
  'SUSE Manager' => {
    'default' => # CHECKED
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
        sle-manager-tools15-pool-x86_64-sp4
        sle-manager-tools15-updates-x86_64-sp4
        sle-manager-tools15-beta-pool-x86_64-sp4
        sle-manager-tools15-beta-updates-x86_64-sp4
        sle-module-devtools15-sp4-updates-x86_64
        sle-module-devtools15-sp4-pool-x86_64
        sle-module-containers15-sp4-pool-x86_64
        sle-module-containers15-sp4-updates-x86_64
        sle-product-sles15-sp4-ltss-updates-x86_64
      ],
    'almalinux8' =>
      %w[
        almalinux8-x86_64
        almalinux8-appstream-x86_64
      ],
    'almalinux9' => # CHECKED
      %w[
        almalinux9-x86_64
        almalinux9-appstream-x86_64
      ],
    'debian-10' => # CHECKED
      %w[
        debian-10-pool-amd64
        debian-10-main-updates-amd64
        debian-10-main-security-amd64
        debian-10-suse-manager-tools-amd64
      ],
    'debian-11' => # CHECKED
      %w[
        debian-11-pool-amd64
        debian-11-main-updates-amd64
        debian-11-main-security-amd64
        debian-11-suse-manager-tools-amd64
      ],
    'debian-12' => # CHECKED
      %w[
        debian-12-pool-amd64
        debian-12-main-security-amd64
        debian-12-main-updates-amd64
        debian-12-suse-manager-tools-amd64
      ],
    'sll-9' => # CHECKED
      %w[
        sll-cb-9-updates-x86_64
        sll-as-9-updates-x86_64
        sll-9-updates-x86_64
      ],
    'el9' => # CHECKED
      %w[
        el9-manager-tools-updates-x86_64-alma
        el9-manager-tools-pool-x86_64-alma
        el9-pool-x86_64
        el9-manager-tools-pool-x86_64
        el9-manager-tools-updates-x86_64
        el9-manager-tools-pool-x86_64-ol9
        el9-manager-tools-updates-x86_64-ol9
        el9-manager-tools-pool-x86_64-rocky
        el9-manager-tools-updates-x86_64-rocky
      ],
    'rockylinux8' =>
      %w[
        rockylinux8-x86_64
        rockylinux8-appstream-x86_64
      ],
    'rockylinux9' => # CHECKED
      %w[
        rockylinux-9-x86_64
        rockylinux-9-appstream-x86_64
      ],
    'oraclelinux9' => # CHECKED
      %w[
        oraclelinux9-x86_64
        oraclelinux9-appstream-x86_64
      ],
    'sles12-sp5' => # CHECKED
      %w[
        sles12-sp5-pool-x86_64
        sle-manager-tools12-updates-x86_64-sp5
        sles12-sp5-updates-x86_64
        sle-manager-tools12-pool-x86_64-sp5
        sles12-sp5-installer-updates-x86_64
      ],
    'sles15-sp1' => # CHECKED
      %w[
        sle-product-sles15-sp1-pool-x86_64
        sle-product-sles15-sp1-updates-x86_64
        sle15-sp1-installer-updates-x86_64
        sle-module-basesystem15-sp1-pool-x86_64
        sle-module-basesystem15-sp1-updates-x86_64
        sle-manager-tools15-pool-x86_64-sp1
        sle-manager-tools15-updates-x86_64-sp1
        sle-module-server-applications15-sp1-pool-x86_64
        sle-module-server-applications15-sp1-updates-x86_64
        sle-product-sles15-sp1-ltss-updates-x86_64
      ],
    'sles15-sp2' => # CHECKED
      %w[
        sle-product-sles15-sp2-pool-x86_64
        sle-product-sles15-sp2-updates-x86_64
        sle15-sp2-installer-updates-x86_64
        sle-module-basesystem15-sp2-pool-x86_64
        sle-module-basesystem15-sp2-updates-x86_64
        sle-manager-tools15-pool-x86_64-sp2
        sle-manager-tools15-updates-x86_64-sp2
        sle-module-server-applications15-sp2-pool-x86_64
        sle-module-server-applications15-sp2-updates-x86_64
        sle-product-sles15-sp2-ltss-updates-x86_64
        sle-module-desktop-applications15-sp2-updates-x86_64
        sle-module-desktop-applications15-sp2-pool-x86_64
        sle-module-devtools15-sp2-pool-x86_64
        sle-module-devtools15-sp2-updates-x86_64
      ],
    'sles15-sp3' => # CHECKED
      %w[
        sle-product-sles15-sp3-pool-x86_64
        sle-product-sles15-sp3-updates-x86_64
        sle15-sp3-installer-updates-x86_64
        sle-module-basesystem15-sp3-updates-x86_64
        sle-module-basesystem15-sp3-pool-x86_64
        sle-manager-tools15-updates-x86_64-sp3
        sle-manager-tools15-pool-x86_64-sp3
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
        sle-manager-tools15-pool-x86_64-sp4
        sle-manager-tools15-updates-x86_64-sp4
        sle-manager-tools15-beta-pool-x86_64-sp4
        sle-manager-tools15-beta-updates-x86_64-sp4
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
        sle-manager-tools15-updates-x86_64-sp5
        sle-manager-tools15-pool-x86_64-sp5
        sle-module-server-applications15-sp5-pool-x86_64
        sle-module-server-applications15-sp5-updates-x86_64
        sle-module-desktop-applications15-sp5-updates-x86_64
        sle-module-desktop-applications15-sp5-pool-x86_64
        sle-module-devtools15-sp5-pool-x86_64
        sle-module-devtools15-sp5-updates-x86_64
      ],
    'slesforsap15-sp5' =>
      %w[
        sle-manager-tools15-pool-x86_64-sap-sp5
        sle-manager-tools15-updates-x86_64-sap-sp5
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
        sle-manager-tools15-pool-s390x-sp5
        sle-manager-tools15-updates-s390x-sp5
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
        res7-suse-manager-tools-x86_64
      ],
    'res8' =>
      %w[
        rhel8-pool-x86_64
        res-8-updates-x86_64
        res-as-8-updates-x86_64
        res-cb-8-updates-x86_64
        res8-manager-tools-pool-x86_64
        res8-manager-tools-updates-x86_64
      ],
    'leap15.5-x86_64' =>
      %w[
        opensuse_leap15_5-x86_64
        opensuse_leap15_5-x86_64-non-oss
        opensuse_leap15_5-x86_64-non-oss-updates
        opensuse_leap15_5-x86_64-updates
        opensuse_leap15_5-x86_64-backports-updates
        opensuse_leap15_5-x86_64-sle-updates
      ],
    'leap15.4-aarch64' =>
      %w[
        opensuse-backports-15.4-updates-aarch64
        opensuse-leap-15.4-pool-aarch64
        opensuse-leap-15.4-updates-aarch64
        sle-manager-tools15-updates-aarch64-opensuse-15.4
        sle-manager-tools15-pool-aarch64-opensuse-15.4
      ],
    'leap15.5-aarch64' =>
      %w[
        opensuse-backports-15.5-updates-aarch64
        opensuse-leap-15.5-pool-aarch64
        opensuse-leap-15.5-updates-aarch64
        opensuse-sle-15.5-updates-aarch64
        sle-manager-tools15-updates-aarch64-opensuse-15.5
        sle-manager-tools15-pool-aarch64-opensuse-15.5
      ],
    'suse-microos-5.1' => # CHECKED
      %w[
        suse-microos-5.1-pool-x86_64
        suse-microos-5.1-updates-x86_64
        sle-manager-tools-for-micro5-updates-x86_64-5.1
        sle-manager-tools-for-micro5-pool-x86_64-5.1
      ],
    'suse-microos-5.2' => # CHECKED
      %w[
        suse-microos-5.2-pool-x86_64
        suse-microos-5.2-updates-x86_64
        sle-manager-tools-for-micro5-pool-x86_64-5.2
        sle-manager-tools-for-micro5-updates-x86_64-5.2
      ],
    'sle-micro-5.3' => # CHECKED
      %w[
        sle-micro-5.3-pool-x86_64
        sle-micro-5.3-updates-x86_64
        sle-manager-tools-for-micro5-pool-x86_64-5.3
        sle-manager-tools-for-micro5-updates-x86_64-5.3
      ],
    'sle-micro-5.4' => # CHECKED
      %w[
        sle-micro-5.4-pool-x86_64
        sle-micro-5.4-updates-x86_64
        sle-manager-tools-for-micro5-updates-x86_64-5.4
        sle-manager-tools-for-micro5-pool-x86_64-5.4
      ],
    'sle-micro-5.5' => # CHECKED
      %w[
        sle-micro-5.5-pool-x86_64
        sle-micro-5.5-updates-x86_64
        sle-manager-tools-for-micro5-pool-x86_64-5.5
        sle-manager-tools-for-micro5-updates-x86_64-5.5
      ],
    'ubuntu-2004' => # CHECKED
      %w[
        ubuntu-2004-amd64-main-amd64
        ubuntu-2004-amd64-main-security-amd64
        ubuntu-2004-amd64-main-updates-amd64
        ubuntu-20.04-suse-manager-tools-amd64
      ],
    'ubuntu-2204' => # CHECKED
      %w[
        ubuntu-2204-amd64-main-amd64
        ubuntu-2204-amd64-main-updates-amd64
        ubuntu-2204-amd64-main-security-amd64
        ubuntu-22.04-suse-manager-tools-amd64
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
      ]
  },
  'Uyuni' => {
    'default' => # CHECKED
      %w[
        sle-product-sles15-sp4-pool-x86_64
        sle-product-sles15-sp4-updates-x86_64
        sle15-sp4-installer-updates-x86_64
        sle-module-basesystem15-sp4-updates-x86_64
        sle-module-basesystem15-sp4-pool-x86_64
        sle-module-server-applications15-sp4-updates-x86_64
        sle-module-server-applications15-sp4-pool-x86_64
        sle-module-desktop-applications15-sp4-updates-x86_64
        sle-module-desktop-applications15-sp4-pool-x86_64
        sle-product-sles15-sp4-ltss-updates-x86_64
        sle-module-devtools15-sp4-pool-x86_64
        sle-module-devtools15-sp4-updates-x86_64
        sle-module-containers15-sp4-pool-x86_64
        sle-module-containers15-sp4-updates-x86_64
        sles15-sp4-uyuni-client-x86_64
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
    'centos7' => # CHECKED
      %w[
        centos7-x86_64
        centos7-x86_64-extras
        centos7-uyuni-client-devel-x86_64
      ],
    'debian-10' => # CHECKED
      %w[
        debian-10-pool-amd64-uyuni
        debian-10-amd64-main-updates-uyuni
        debian-10-amd64-main-security-uyuni
        debian-10-amd64-uyuni-client-devel
      ],
    'debian-11' => # CHECKED
      %w[
        debian-11-pool-amd64-uyuni
        debian-11-amd64-main-updates-uyuni
        debian-11-amd64-main-security-uyuni
        debian-11-amd64-uyuni-client-devel
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
        rockylinux8-x86_64
        rockylinux8-appstream-x86_64
        rockylinux8-extras-x86_64
        rockylinux8-uyuni-client-devel-x86_64
      ],
    'rockylinux9' => # CHECKED
      %w[
        rockylinux9-x86_64
        rockylinux9-appstream-x86_64
        rockylinux9-extras-x86_64
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
        sles12-sp5-uyuni-client-devel-x86_64
      ],
    'sles15-sp1' => # CHECKED
      %w[
        sle-product-sles15-sp1-ltss-updates-x86_64
        sle-product-sles15-sp1-pool-x86_64
        sle-product-sles15-sp1-updates-x86_64
        sle-module-server-applications15-sp1-pool-x86_64
        sle-module-server-applications15-sp1-updates-x86_64
        sle-module-basesystem15-sp1-pool-x86_64
        sle-module-basesystem15-sp1-updates-x86_64
        sle15-sp1-installer-updates-x86_64
        sles15-sp1-devel-uyuni-client-x86_64
      ],
    'sles15-sp2' => # CHECKED
      %w[
        sle-product-sles15-sp2-ltss-updates-x86_64
        sle-product-sles15-sp2-pool-x86_64
        sle-product-sles15-sp2-updates-x86_64
        sle-module-server-applications15-sp2-pool-x86_64
        sle-module-server-applications15-sp2-updates-x86_64
        sle-module-devtools15-sp2-pool-x86_64
        sle-module-devtools15-sp2-updates-x86_64
        sle-module-desktop-applications15-sp2-pool-x86_64
        sle-module-desktop-applications15-sp2-updates-x86_64
        sle-module-basesystem15-sp2-pool-x86_64
        sle-module-basesystem15-sp2-updates-x86_64
        sle15-sp2-installer-updates-x86_64
        sles15-sp2-devel-uyuni-client-x86_64
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
        sles15-sp5-devel-uyuni-client-x86_64
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
    'leap15.5-x86_64' => # CHECKED
      %w[
        opensuse_leap15_5-x86_64
        opensuse_leap15_5-x86_64-backports-updates
        opensuse_leap15_5-x86_64-non-oss
        opensuse_leap15_5-x86_64-non-oss-updates
        opensuse_leap15_5-x86_64-updates
        opensuse_leap15_5-x86_64-sle-updates
        opensuse_leap15_5-uyuni-client-x86_64
        opensuse_leap15_5-uyuni-client-devel-x86_64
        uyuni-proxy-devel-leap-x86_64
      ],
    'leap15.4-aarch64' => # CHECKED
      %w[
        opensuse_leap15_4-aarch64
        opensuse_leap15_4-aarch64-backports-updates
        opensuse_leap15_4-aarch64-non-oss
        opensuse_leap15_4-aarch64-non-oss-updates
        opensuse_leap15_4-aarch64-sle-updates
        opensuse_leap15_4-aarch64-updates
        opensuse_leap15_4-uyuni-client-devel-aarch64
      ],
    'leap15.5-aarch64' => # CHECKED
      %w[
        opensuse_leap15_5-aarch64
        opensuse_leap15_5-aarch64-backports-updates
        opensuse_leap15_5-aarch64-non-oss
        opensuse_leap15_5-aarch64-non-oss-updates
        opensuse_leap15_5-aarch64-sle-updates
        opensuse_leap15_5-aarch64-updates
        opensuse_leap15_5-uyuni-client-devel-aarch64
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
    'ubuntu-2004' => # CHECKED
      %w[
        ubuntu-2004-pool-amd64-uyuni
        ubuntu-2004-amd64-main-uyuni
        ubuntu-2004-amd64-main-security-uyuni
        ubuntu-2004-amd64-main-updates-uyuni
        ubuntu-2004-amd64-universe-uyuni
        ubuntu-2004-amd64-universe-backports-uyuni
        ubuntu-2004-amd64-universe-security-uyuni
        ubuntu-2004-amd64-universe-updates-uyuni
        ubuntu-2004-amd64-uyuni-client-devel
      ],
    'ubuntu-2204' => # CHECKED
      %w[
        ubuntu-22.04-pool-amd64-uyuni
        ubuntu-2204-amd64-main-security-uyuni
        ubuntu-2204-amd64-main-updates-uyuni
        ubuntu-2204-amd64-main-uyuni
        ubuntu-2204-amd64-universe-backports-uyuni
        ubuntu-2204-amd64-universe-security-uyuni
        ubuntu-2204-amd64-universe-updates-uyuni
        ubuntu-2204-amd64-universe-uyuni
        ubuntu-2204-amd64-uyuni-client-devel
      ],
    'uyuni-proxy' => # CHECKED
      %w[
        opensuse_leap15_5-x86_64
        opensuse_leap15_5-uyuni-client-x86_64
        opensuse_leap15_5-x86_64-backports-updates
        opensuse_leap15_5-x86_64-non-oss
        opensuse_leap15_5-x86_64-non-oss-updates
        opensuse_leap15_5-x86_64-sle-updates
        opensuse_leap15_5-x86_64-updates
        uyuni-proxy-devel-leap-x86_64
      ]
    # There are no channels for Retail under Uyuni
  }
}.freeze

# The timeouts are determining experimentally, by looking at the files in /var/log/rhn/reposync on the server
# Formula: (end date - startup date) * 2, rounded to upper 60 seconds
# Please keep this list sorted alphabetically
TIMEOUT_BY_CHANNEL_NAME = {
  'almalinux8-appstream-x86_64' => 480,
  'almalinux8-uyuni-client-devel-x86_64' => 60,
  'almalinux8-x86_64' => 420,
  'almalinux8-x86_64-appstream' => 1740,
  'almalinux8-x86_64-extras' => 60,
  'almalinux9-appstream-x86_64' => 480,
  'almalinux9-uyuni-client-devel-x86_64' => 60,
  'almalinux9-x86_64' => 120,
  'almalinux9-x86_64-appstream' => 720,
  'almalinux9-x86_64-extras' => 60,
  'centos-7-iso' => 300,
  'centos7-uyuni-client-devel-x86_64' => 60,
  'centos7-x86_64' => 960,
  'centos7-x86_64-extras' => 120,
  'debian-10-amd64-main-security-uyuni' => 780,
  'debian-10-amd64-main-updates-uyuni' => 60,
  'debian-10-amd64-uyuni-client-devel' => 60,
  'debian-10-main-security-amd64' => 540,
  'debian-10-main-updates-amd64' => 60,
  'debian-10-pool-amd64' => 19_860,
  'debian-10-pool-amd64-uyuni' => 21_060,
  'debian-10-suse-manager-tools-amd64' => 60,
  'debian-11-amd64-main-security-uyuni' => 240,
  'debian-11-amd64-main-updates-uyuni' => 60,
  'debian-11-amd64-uyuni-client-devel' => 60,
  'debian-11-main-security-amd64' => 240,
  'debian-11-main-updates-amd64' => 60,
  'debian-11-pool-amd64' => 22_920,
  'debian-11-pool-amd64-uyuni' => 23_580,
  'debian-11-suse-manager-tools-amd64' => 60,
  'debian-12-amd64-main-security-uyuni' => 240,
  'debian-12-amd64-main-updates-uyuni' => 120,
  'debian-12-amd64-uyuni-client-devel' => 60,
  'debian-12-main-security-amd64' => 240,
  'debian-12-main-updates-amd64' => 120,
  'debian-12-pool-amd64' => 27_960,
  'debian-12-pool-amd64-uyuni' => 28_260,
  'debian-12-suse-manager-tools-amd64' => 60,
  'el9-manager-tools-pool-x86_64' => 60,
  'el9-manager-tools-pool-x86_64-alma' => 60,
  'el9-manager-tools-pool-x86_64-ol9' => 60,
  'el9-manager-tools-pool-x86_64-rocky' => 60,
  'el9-manager-tools-updates-x86_64' => 60,
  'el9-manager-tools-updates-x86_64-alma' => 60,
  'el9-manager-tools-updates-x86_64-ol9' => 60,
  'el9-manager-tools-updates-x86_64-rocky' => 60,
  'el9-pool-x86_64' => 60,
  'fake-base-channel-debian-like' => 120,
  'fake-base-channel-rh-like' => 120,
  'fake-child-channel-i586' => 120,
  'fake-child-channel-suse-like' => 120,
  'fake-rpm-suse-channel' => 120,
  'fake-rpm-terminal-channel' => 120,
  'opensuse_leap15_4-aarch64' => 8940,
  'opensuse_leap15_4-aarch64-backports-updates' => 540,
  'opensuse_leap15_4-aarch64-non-oss' => 60,
  'opensuse_leap15_4-aarch64-non-oss-updates' => 60,
  'opensuse_leap15_4-aarch64-sle-updates' => 7740,
  'opensuse_leap15_4-aarch64-updates' => 180,
  'opensuse_leap15_4-uyuni-client-devel-aarch64' => 60,
  'opensuse_leap15_5-aarch64' => 10_020,
  'opensuse_leap15_5-aarch64-backports-updates' => 420,
  'opensuse_leap15_5-aarch64-non-oss' => 60,
  'opensuse_leap15_5-aarch64-non-oss-updates' => 60,
  'opensuse_leap15_5-aarch64-sle-updates' => 4140,
  'opensuse_leap15_5-aarch64-updates' => 60,
  'opensuse_leap15_5-uyuni-client-devel-aarch64' => 60,
  'opensuse_leap15_5-uyuni-client-devel-x86_64' => 60,
  'opensuse_leap15_5-uyuni-client-x86_64' => 60,
  'opensuse_leap15_5-x86_64' => 10_380,
  'opensuse_leap15_5-x86_64-backports-updates' => 360,
  'opensuse_leap15_5-x86_64-non-oss' => 60,
  'opensuse_leap15_5-x86_64-non-oss-updates' => 120,
  'opensuse_leap15_5-x86_64-sle-updates' => 5400,
  'opensuse_leap15_5-x86_64-updates' => 60,
  'opensuse_micro5_5-uyuni-client-x86_64' => 60,
  'opensuse_micro5_5-uyuni-client-devel-x86_64' => 60,
  'opensuse_micro5_5-x86_64' => 240,
  'opensuse_micro5_5-x86_64-sle-updates' => 5400,
  'oraclelinux9-appstream-x86_64' => 2100,
  'oraclelinux9-uyuni-client-devel-x86_64' => 60,
  'oraclelinux9-x86_64' => 840,
  'res7-suse-manager-tools-x86_64' => 300,
  'res7-x86_64' => 21_000,
  'rhel-x86_64-server-7' => 60,
  'rockylinux8-uyuni-client-devel-x86_64' => 60,
  'rocky-8-iso' => 600,
  'rockylinux8-x86_64' => 600,
  'rockylinux8-x86_64-appstream' => 1260,
  'rockylinux8-x86_64-extras' => 420,
  'rockylinux9-appstream-x86_64' => 480,
  'rockylinux9-uyuni-client-devel-x86_64' => 60,
  'rockylinux9-x86_64' => 120,
  'rockylinux9-x86_64-extras' => 120,
  'sle15-sp1-installer-updates-x86_64' => 60,
  'sle15-sp2-installer-updates-x86_64' => 60,
  'sle15-sp3-installer-updates-x86_64' => 60,
  'sle15-sp4-installer-updates-x86_64' => 60,
  'sle-manager-tools12-pool-x86_64-sp5' => 60,
  'sle-manager-tools12-updates-x86_64-sp5' => 60,
  'sle-manager-tools15-beta-pool-x86_64-sp4' => 60,
  'sle-manager-tools15-beta-updates-x86_64-sp4' => 60,
  'sle-manager-tools15-pool-s390x-sp5' => 60,
  'sle-manager-tools15-pool-x86_64-sp1' => 60,
  'sle-manager-tools15-pool-x86_64-sp2' => 60,
  'sle-manager-tools15-pool-x86_64-sp3' => 60,
  'sle-manager-tools15-pool-x86_64-sp4' => 60,
  'sle-manager-tools15-pool-x86_64-sp5' => 60,
  'sle-manager-tools15-updates-s390x-sp5' => 120,
  'sle-manager-tools15-updates-x86_64-sp1' => 180,
  'sle-manager-tools15-updates-x86_64-sp2' => 90,
  'sle-manager-tools15-updates-x86_64-sp3' => 90,
  'sle-manager-tools15-updates-x86_64-sp4' => 90,
  'sle-manager-tools15-updates-x86_64-sp5' => 90,
  'sle-manager-tools-for-micro5-pool-x86_64-5.1' => 60,
  'sle-manager-tools-for-micro5-pool-x86_64-5.2' => 60,
  'sle-manager-tools-for-micro5-pool-x86_64-5.3' => 60,
  'sle-manager-tools-for-micro5-pool-x86_64-5.4' => 60,
  'sle-manager-tools-for-micro5-pool-x86_64-5.5' => 60,
  'sle-manager-tools-for-micro5-updates-x86_64-5.1' => 60,
  'sle-manager-tools-for-micro5-updates-x86_64-5.2' => 60,
  'sle-manager-tools-for-micro5-updates-x86_64-5.3' => 60,
  'sle-manager-tools-for-micro5-updates-x86_64-5.4' => 60,
  'sle-manager-tools-for-micro5-updates-x86_64-5.5' => 60,
  'sle-micro-5.3-devel-uyuni-client-x86_64' => 60,
  'sle-micro-5.3-pool-x86_64' => 120,
  'sle-micro-5.3-updates-x86_64' => 240,
  'sle-micro-5.4-devel-uyuni-client-x86_64' => 60,
  'sle-micro-5.4-pool-x86_64' => 60,
  'sle-micro-5.4-updates-x86_64' => 60,
  'sle-micro-5.5-devel-uyuni-client-x86_64' => 60,
  'sle-micro-5.5-pool-x86_64' => 120,
  'sle-micro-5.5-updates-x86_64' => 120,
  'sle-module-basesystem15-sp1-pool-x86_64' => 180,
  'sle-module-basesystem15-sp1-updates-x86_64' => 660,
  'sle-module-basesystem15-sp2-pool-x86_64' => 180,
  'sle-module-basesystem15-sp2-updates-x86_64' => 660,
  'sle-module-basesystem15-sp3-pool-x86_64' => 240,
  'sle-module-basesystem15-sp3-updates-x86_64' => 1020,
  'sle-module-basesystem15-sp4-pool-x86_64' => 240,
  'sle-module-basesystem15-sp4-pool-x86_64-proxy-4.3' => 60,
  'sle-module-basesystem15-sp4-pool-x86_64-smrbs-4.3' => 60,
  'sle-module-basesystem15-sp4-updates-x86_64' => 1800,
  'sle-module-basesystem15-sp4-updates-x86_64-proxy-4.3' => 180,
  'sle-module-basesystem15-sp4-updates-x86_64-smrbs-4.3' => 180,
  'sle-module-basesystem15-sp5-pool-s390x' => 360,
  'sle-module-basesystem15-sp5-pool-x86_64' => 240,
  'sle-module-basesystem15-sp5-updates-s390x' => 600,
  'sle-module-basesystem15-sp5-updates-x86_64' => 540,
  'sle-module-containers15-sp4-pool-x86_64' => 60,
  'sle-module-containers15-sp4-pool-x86_64-proxy-4.3' => 60,
  'sle-module-containers15-sp4-pool-x86_64-smrbs-4.3' => 60,
  'sle-module-containers15-sp4-updates-x86_64' => 60,
  'sle-module-containers15-sp4-updates-x86_64-proxy-4.3' => 60,
  'sle-module-containers15-sp4-updates-x86_64-smrbs-4.3' => 60,
  'sle-module-desktop-applications15-sp2-pool-x86_64' => 180,
  'sle-module-desktop-applications15-sp2-updates-x86_64' => 180,
  'sle-module-desktop-applications15-sp3-pool-x86_64' => 120,
  'sle-module-desktop-applications15-sp3-updates-x86_64' => 60,
  'sle-module-desktop-applications15-sp4-pool-x86_64' => 240,
  'sle-module-desktop-applications15-sp4-updates-x86_64' => 120,
  'sle-module-desktop-applications15-sp5-pool-x86_64' => 120,
  'sle-module-desktop-applications15-sp5-updates-x86_64' => 60,
  'sle-module-devtools15-sp2-pool-x86_64' => 120,
  'sle-module-devtools15-sp2-updates-x86_64' => 420,
  'sle-module-devtools15-sp3-pool-x86_64' => 120,
  'sle-module-devtools15-sp3-updates-x86_64' => 600,
  'sle-module-devtools15-sp4-pool-x86_64' => 120,
  'sle-module-devtools15-sp4-updates-x86_64' => 600,
  'sle-module-devtools15-sp5-pool-x86_64' => 120,
  'sle-module-devtools15-sp5-updates-x86_64' => 300,
  'sle-module-public-cloud15-sp4-pool-x86_64' => 840,
  'sle-module-public-cloud15-sp4-updates-x86_64' => 600,
  'sle-module-public-cloud15-sp5-pool-x86_64' => 600,
  'sle-module-public-cloud15-sp5-updates-x86_64' => 420,
  'sle-module-python3-15-sp5-pool-x86_64' => 60,
  'sle-module-python3-15-sp5-updates-x86_64' => 60,
  'sle-module-server-applications15-sp1-pool-x86_64' => 60,
  'sle-module-server-applications15-sp1-updates-x86_64' => 120,
  'sle-module-server-applications15-sp2-pool-x86_64' => 60,
  'sle-module-server-applications15-sp2-updates-x86_64' => 120,
  'sle-module-server-applications15-sp3-pool-x86_64' => 60,
  'sle-module-server-applications15-sp3-updates-x86_64' => 120,
  'sle-module-server-applications15-sp4-pool-x86_64' => 60,
  'sle-module-server-applications15-sp4-pool-x86_64-smrbs-4.3' => 60,
  'sle-module-server-applications15-sp4-updates-x86_64-proxy-4.3' => 60,
  'sle-module-server-applications15-sp4-updates-x86_64' => 120,
  'sle-module-server-applications15-sp4-updates-x86_64-smrbs-4.3' => 60,
  'sle-module-server-applications15-sp5-pool-s390x' => 60,
  'sle-module-server-applications15-sp5-pool-x86_64' => 60,
  'sle-module-server-applications15-sp5-updates-s390x' => 120,
  'sle-module-server-applications15-sp5-updates-x86_64' => 60,
  'sle-product-sles15-sp1-ltss-updates-x86_64' => 1500,
  'sle-product-sles15-sp1-pool-x86_64' => 60,
  'sle-product-sles15-sp1-updates-x86_64' => 60,
  'sle-product-sles15-sp2-ltss-updates-x86_64' => 1200,
  'sle-product-sles15-sp2-pool-x86_64' => 60,
  'sle-product-sles15-sp2-updates-x86_64' => 60,
  'sle-product-sles15-sp3-ltss-updates-x86_64' => 960,
  'sle-product-sles15-sp3-pool-x86_64' => 60,
  'sle-product-sles15-sp3-updates-x86_64' => 60,
  'sle-product-sles15-sp4-ltss-updates-x86_64' => 960,
  'sle-product-sles15-sp4-pool-x86_64' => 60,
  'sle-product-sles15-sp4-updates-x86_64' => 60,
  'sle-product-sles15-sp5-pool-s390x' => 60,
  'sle-product-sles15-sp5-pool-x86_64' => 60,
  'sle-product-sles15-sp5-updates-s390x' => 60,
  'sle-product-sles15-sp5-updates-x86_64' => 60,
  'sles12-sp5-installer-updates-x86_64' => 60,
  'sles12-sp5-pool-x86_64' => 180,
  'sles12-sp5-updates-x86_64' => 2280,
  'sles12-sp5-uyuni-client-devel-x86_64' => 120,
  'sles15-sp1-devel-uyuni-client-x86_64' => 60,
  'sles15-sp2-devel-uyuni-client-x86_64' => 60,
  'sles15-sp3-devel-uyuni-client-x86_64' => 60,
  'sles15-sp4-devel-uyuni-client-x86_64' => 60,
  'sles15-sp5-devel-uyuni-client-x86_64' => 60,
  'sll-9-updates-x86_64' => 720,
  'sll-as-9-updates-x86_64' => 1620,
  'sll-cb-9-updates-x86_64' => 2640,
  'suse-manager-proxy-5.0-pool-x86_64' => 60,
  'suse-manager-proxy-5.0-updates-x86_64' => 60,
  'suse-manager-retail-branch-server-5.0-pool-x86_64' => 60,
  'suse-manager-retail-branch-server-5.0-updates-x86_64' => 60,
  'suse-microos-5.1-devel-uyuni-client-x86_64' => 60,
  'suse-microos-5.1-pool-x86_64' => 60,
  'suse-microos-5.1-updates-x86_64' => 300,
  'suse-microos-5.2-devel-uyuni-client-x86_64' => 60,
  'suse-microos-5.2-pool-x86_64' => 60,
  'suse-microos-5.2-updates-x86_64' => 60,
  'test-child-channel-x86_64' => 120,
  'ubuntu-2004-amd64-main-amd64' => 480,
  'ubuntu-2004-amd64-main-security-amd64' => 3480,
  'ubuntu-2004-amd64-main-security-uyuni' => 4200,
  'ubuntu-2004-amd64-main-updates-amd64' => 660,
  'ubuntu-2004-amd64-main-updates-uyuni' => 600,
  'ubuntu-2004-amd64-main-uyuni' => 660,
  'ubuntu-2004-amd64-universe-backports-uyuni' => 60,
  'ubuntu-2004-amd64-universe-security-uyuni' => 900,
  'ubuntu-2004-amd64-universe-updates-uyuni' => 240,
  'ubuntu-2004-amd64-universe-uyuni' => 19_560,
  'ubuntu-2004-amd64-uyuni-client-devel' => 60,
  'ubuntu-2004-pool-amd64-uyuni' => 60,
  'ubuntu-20.04-suse-manager-tools-amd64' => 60,
  'ubuntu-2204-amd64-main-amd64' => 780,
  'ubuntu-2204-amd64-main-security-amd64' => 2760,
  'ubuntu-2204-amd64-main-security-uyuni' => 2040,
  'ubuntu-2204-amd64-main-updates-amd64' => 180,
  'ubuntu-2204-amd64-main-updates-uyuni' => 300,
  'ubuntu-2204-amd64-main-uyuni' => 780,
  'ubuntu-2204-amd64-universe-backports-uyuni' => 60,
  'ubuntu-2204-amd64-universe-security-uyuni' => 1020,
  'ubuntu-2204-amd64-universe-updates-uyuni' => 240,
  'ubuntu-2204-amd64-universe-uyuni' => 24_000,
  'ubuntu-2204-amd64-uyuni-client-devel' => 60,
  'ubuntu-22.04-pool-amd64-uyuni' => 60,
  'ubuntu-22.04-suse-manager-tools-amd64' => 60,
  'uyuni-proxy-devel-leap-x86_64' => 60
}.freeze

EMPTY_CHANNELS = %w[
  suse-manager-proxy-5.0-updates-x86_64
  suse-manager-retail-branch-server-5.0-updates-x86_64
  sle-module-suse-manager-retail-branch-server-4.3-updates-x86_64
  sle-manager-tools15-beta-pool-x86_64-sp4
  fake-base-channel-suse-like
  fake-base-channel-i586
  test-base-channel-x86_64
].freeze
