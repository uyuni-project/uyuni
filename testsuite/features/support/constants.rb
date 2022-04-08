# Copyright (c) 2019-2022 SUSE LLC
# Licensed under the terms of the MIT license.

ADDRESSES = { 'network'        => '0',
              'sle_client'     => '2',
              'sle_minion'     => '3',
              'pxeboot_minion' => '4',
              'range begin'    => '128',
              'range end'      => '253',
              'proxy'          => '254',
              'broadcast'      => '255' }.freeze

FIELD_IDS = { 'NIC'                             => 'branch_network#nic',
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
              'third reserved IP'               => 'dhcpd#hosts#2#fixed_address',
              'first reserved hostname'         => 'dhcpd#hosts#0#$key',
              'second reserved hostname'        => 'dhcpd#hosts#1#$key',
              'third reserved hostname'         => 'dhcpd#hosts#2#$key',
              'first reserved MAC'              => 'dhcpd#hosts#0#hardware',
              'second reserved MAC'             => 'dhcpd#hosts#1#hardware',
              'third reserved MAC'              => 'dhcpd#hosts#2#hardware',
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
              'timezone name'                   => 'timezone#name',
              'language'                        => 'keyboard_and_language#language',
              'keyboard layout'                 => 'keyboard_and_language#keyboard_layout' }.freeze

BOX_IDS = { 'enable SLAAC with routing' => 'branch_network#firewall#enable_SLAAC_with_routing',
            'include forwarders'        => 'bind#config#include_forwarders',
            'enable route'              => 'branch_network#firewall#enable_route',
            'enable NAT'                => 'branch_network#firewall#enable_NAT' }.freeze

BULLET_STYLE = { 'failing' => 'fa-times text-danger',
                 'warning' => 'fa-hand-o-right text-danger',
                 'success' => 'fa-check text-success',
                 'pending' => 'fa-hand-o-right text-success',
                 'refreshing' => 'fa-refresh text-warning' }.freeze

PACKAGE_BY_CLIENT = { 'sle_client' => 'bison',
                      'sle_minion' => 'bison',
                      'ssh_minion' => 'bison',
                      'ceos_client' => 'autoconf',
                      'ceos_minion' => 'autoconf',
                      'ubuntu_minion' => 'bison',
                      'sle11sp4_client' => 'bison',
                      'sle11sp4_minion' => 'bison',
                      'sle11sp4_ssh_minion' => 'bison',
                      'sle12sp4_client' => 'bison',
                      'sle12sp4_minion' => 'bison',
                      'sle12sp4_ssh_minion' => 'bison',
                      'sle12sp5_client' => 'bison',
                      'sle12sp5_minion' => 'bison',
                      'sle12sp5_ssh_minion' => 'bison',
                      'sle15_client' => 'bison',
                      'sle15_minion' => 'bison',
                      'sle15_ssh_minion' => 'bison',
                      'sle15sp1_client' => 'bison',
                      'sle15sp1_minion' => 'bison',
                      'sle15sp1_ssh_minion' => 'bison',
                      'sle15sp2_client' => 'bison',
                      'sle15sp2_minion' => 'bison',
                      'sle15sp2_ssh_minion' => 'bison',
                      'sle15sp3_client' => 'bison',
                      'sle15sp3_minion' => 'bison',
                      'sle15sp3_ssh_minion' => 'bison',
                      'ceos7_client' => 'autoconf',
                      'ceos7_minion' => 'autoconf',
                      'ceos7_ssh_minion' => 'autoconf',
                      'ceos8_minion' => 'autoconf',
                      'ceos8_ssh_minion' => 'autoconf',
                      'ubuntu1804_minion' => 'bison',
                      'ubuntu1804_ssh_minion' => 'bison',
                      'ubuntu2004_minion' => 'bison',
                      'ubuntu2004_ssh_minion' => 'bison',
                      'debian9_minion' => 'bison',
                      'debian9_ssh_minion' => 'bison',
                      'debian10_minion' => 'bison',
                      'debian10_ssh_minion' => 'bison',
                      'debian11_minion' => 'bison',
                      'debian11_ssh_minion' => 'bison',
                      'opensuse153arm_minion' => 'bison' }.freeze

BASE_CHANNEL_BY_CLIENT = { 'proxy' => 'SLE-Product-SUSE-Manager-Proxy-4.3-Pool',
                           'sle_client' => 'SLES12-SP4-Pool',
                           'sle_minion' => 'SLES12-SP4-Pool',
                           'ssh_minion' => 'SLES12-SP4-Pool',
                           'ceos_minion' => 'RHEL7-Pool for x86_64',
                           'ubuntu_minion' => 'ubuntu-18.04-pool',
                           'sle11sp4_client' => 'SLES11-SP4-Pool',
                           'sle11sp4_minion' => 'SLES11-SP4-Pool',
                           'sle11sp4_ssh_minion' => 'SLES11-SP4-Pool',
                           'sle11sp4_buildhost' => 'SLES11-SP4-Pool',
                           'sle11sp3_terminal' => 'SLES11-SP3-Pool',
                           'sle12sp4_client' => 'SLES12-SP4-Pool',
                           'sle12sp4_minion' => 'SLES12-SP4-Pool',
                           'sle12sp4_ssh_minion' => 'SLES12-SP4-Pool',
                           'sle12sp5_client' => 'SLES12-SP5-Pool',
                           'sle12sp5_minion' => 'SLES12-SP5-Pool',
                           'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool',
                           'sle12sp5_buildhost' => 'SLES12-SP5-Pool',
                           'sle12sp5_terminal' => 'SLES12-SP5-Pool',
                           'sle15_client' => 'SLES15-Pool',
                           'sle15_minion' => 'SLES15-Pool',
                           'sle15_ssh_minion' => 'SLES15-Pool',
                           'sle15sp1_client' => 'SLES15-SP1-Pool',
                           'sle15sp1_minion' => 'SLES15-SP1-Pool',
                           'sle15sp1_ssh_minion' => 'SLES15-SP1-Pool',
                           'sle15sp2_client' => 'SLES15-SP2-Pool',
                           'sle15sp2_minion' => 'SLES15-SP2-Pool',
                           'sle15sp2_ssh_minion' => 'SLES15-SP2-Pool',
                           'sle15sp3_client' => 'SLES15-SP3-Pool',
                           'sle15sp3_minion' => 'SLES15-SP3-Pool',
                           'sle15sp3_ssh_minion' => 'SLES15-SP3-Pool',
                           'sle15sp3_buildhost' => 'SLES15-SP3-Pool',
                           'sle15sp3_terminal' => 'SLES15-SP3-Pool',
                           'ceos7_client' => 'RHEL x86_64 Server 7',
                           'ceos7_minion' => 'RHEL x86_64 Server 7',
                           'ceos7_ssh_minion' => 'RHEL x86_64 Server 7',
                           'ceos8_minion' => 'no-appstream-result-RHEL8-Pool for x86_64',
                           'ceos8_ssh_minion' => 'no-appstream-result-RHEL8-Pool for x86_64',
                           'ubuntu1804_minion' => 'ubuntu-18.04-pool',
                           'ubuntu1804_ssh_minion' => 'ubuntu-18.04-pool',
                           'ubuntu2004_minion' => 'ubuntu-2004-amd64-main',
                           'ubuntu2004_ssh_minion' => 'ubuntu-2004-amd64-main',
                           'debian9_minion' => 'debian-9-pool',
                           'debian9_ssh_minion' => 'debian-9-pool',
                           'debian10_minion' => 'debian-10-pool',
                           'debian10_ssh_minion' => 'debian-10-pool',
                           'debian11_minion' => 'debian-11-pool',
                           'debian11_ssh_minion' => 'debian-11-pool',
                           'opensuse153arm_minion' => 'openSUSE-Leap-15.3-Pool for aarch64' }.freeze

LABEL_BY_BASE_CHANNEL = { 'SLE-Product-SUSE-Manager-Proxy-4.3-Pool' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
                          'SLES11-SP3-Pool' => 'sles11-sp3-pool-i586',
                          'SLES11-SP4-Pool' => 'sles11-sp4-pool-x86_64',
                          'SLES12-SP4-Pool' => 'sles12-sp4-pool-x86_64',
                          'SLES12-SP5-Pool' => 'sles12-sp5-pool-x86_64',
                          'SLES15-Pool' => 'sle-product-sles15-pool-x86_64',
                          'SLES15-SP1-Pool' => 'sle-product-sles15-sp1-pool-x86_64',
                          'SLES15-SP2-Pool' => 'sle-product-sles15-sp2-pool-x86_64',
                          'SLES15-SP3-Pool' => 'sle-product-sles15-sp3-pool-x86_64',
                          'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
                          'no-appstream-result-RHEL8-Pool for x86_64' => 'no-appstream-result-rhel8-pool-x86_64',
                          'ubuntu-18.04-pool' => 'ubuntu-18.04-pool-amd64',
                          'ubuntu-2004-amd64-main' => 'ubuntu-2004-amd64-main-amd64',
                          'debian-9-pool' => 'debian-9-pool-amd64',
                          'debian-10-pool' => 'debian-10-pool-amd64',
                          'debian-11-pool' => 'debian-11-pool-amd64',
                          'openSUSE-Leap-15.3-Pool for aarch64' => 'opensuse_leap15_3-aarch64' }.freeze

CHANNEL_TO_SYNC_BY_BASE_CHANNEL = { 'SLE-Product-SUSE-Manager-Proxy-4.3-Pool' => 'SUMA-43-PROXY-x86_64',
                                    'SLES11-SP3-Pool' => 'SLE-11-SP3-i586',
                                    'SLES11-SP4-Pool' => 'SLE-11-SP4-x86_64',
                                    'SLES12-SP4-Pool' => 'SLE-12-SP4-x86_64',
                                    'SLES12-SP5-Pool' => 'SLE-12-SP5-x86_64',
                                    'SLES15-Pool' => 'SLE-15-x86_64',
                                    'SLES15-SP1-Pool' => 'SLE-15-SP1-x86_64',
                                    'SLES15-SP2-Pool' => 'SLE-15-SP2-x86_64',
                                    'SLES15-SP3-Pool' => 'SLE-15-SP3-x86_64',
                                    'RHEL x86_64 Server 7' => 'RES7-x86_64',
                                    'no-appstream-result-RHEL8-Pool for x86_64' => 'SLE-ES8-x86_64',
                                    'ubuntu-18.04-pool' => 'ubuntu-18.04-amd64',
                                    'ubuntu-2004-amd64-main' => 'ubuntu-20.04-amd64',
                                    'debian-9-pool' => 'debian9-amd64',
                                    'debian-10-pool' => 'debian10-amd64',
                                    'debian-11-pool' => 'debian11-amd64',
                                    'openSUSE-Leap-15.3-Pool for aarch64' => 'openSUSE-Leap-15.3-aarch64' }.freeze

PARENT_CHANNEL_TO_SYNC_BY_BASE_CHANNEL = { 'SLE-Product-SUSE-Manager-Proxy-4.3-Pool' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
                                           'SLES11-SP3-Pool' => nil,
                                           'SLES11-SP4-Pool' => nil,
                                           'SLES12-SP4-Pool' => nil,
                                           'SLES12-SP5-Pool' => nil,
                                           'SLES15-Pool' => 'sle-product-sles15-pool-x86_64',
                                           'SLES15-SP1-Pool' => 'sle-product-sles15-sp1-pool-x86_64',
                                           'SLES15-SP2-Pool' => 'sle-product-sles15-sp2-pool-x86_64',
                                           'SLES15-SP3-Pool' => 'sle-product-sles15-sp3-pool-x86_64',
                                           'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
                                           'no-appstream-result-RHEL8-Pool for x86_64' => nil,
                                           'ubuntu-18.04-pool' => nil,
                                           'ubuntu-2004-amd64-main' => nil,
                                           'debian-9-pool' => 'debian-9-pool-amd64',
                                           'debian-10-pool' => 'debian-10-pool-amd64',
                                           'debian-11-pool' => 'debian-11-pool-amd64',
                                           'openSUSE-Leap-15.3-Pool for aarch64' => nil }.freeze

PKGARCH_BY_CLIENT = { 'proxy' => 'x86_64',
                      'sle_client' => 'x86_64',
                      'sle_minion' => 'x86_64',
                      'ssh_minion' => 'x86_64',
                      'ceos_minion' => 'x86_64',
                      'ubuntu_minion' => 'amd64',
                      'sle11sp4_client' => 'x86_64',
                      'sle11sp4_minion' => 'x86_64',
                      'sle11sp4_ssh_minion' => 'x86_64',
                      'sle12sp4_client' => 'x86_64',
                      'sle12sp4_minion' => 'x86_64',
                      'sle12sp4_ssh_minion' => 'x86_64',
                      'sle12sp5_client' => 'x86_64',
                      'sle12sp5_minion' => 'x86_64',
                      'sle12sp5_ssh_minion' => 'x86_64',
                      'sle15_client' => 'x86_64',
                      'sle15_minion' => 'x86_64',
                      'sle15_ssh_minion' => 'x86_64',
                      'sle15sp1_client' => 'x86_64',
                      'sle15sp1_minion' => 'x86_64',
                      'sle15sp1_ssh_minion' => 'x86_64',
                      'sle15sp2_client' => 'x86_64',
                      'sle15sp2_minion' => 'x86_64',
                      'sle15sp2_ssh_minion' => 'x86_64',
                      'sle15sp3_client' => 'x86_64',
                      'sle15sp3_minion' => 'x86_64',
                      'sle15sp3_ssh_minion' => 'x86_64',
                      'ceos7_client' => 'x86_64',
                      'ceos7_minion' => 'x86_64',
                      'ceos7_ssh_minion' => 'x86_64',
                      'ceos8_minion' => 'x86_64',
                      'ceos8_ssh_minion' => 'x86_64',
                      'ubuntu1804_minion' => 'amd64',
                      'ubuntu1804_ssh_minion' => 'amd64',
                      'ubuntu2004_minion' => 'amd64',
                      'ubuntu2004_ssh_minion' => 'amd64',
                      'debian9_minion' => 'amd64',
                      'debian9_ssh_minion' => 'amd64',
                      'debian10_minion' => 'amd64',
                      'debian10_ssh_minion' => 'amd64',
                      'debian11_minion' => 'amd64',
                      'debian11_ssh_minion' => 'amd64',
                      'opensuse153arm_minion' => 'aarch64' }.freeze

CHANNEL_TO_SYNCH_BY_OS_VERSION = {
  # 'default' is required for auto-installation tests.
  'default' =>
  %w[
    sle-product-sles15-sp2-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp2
    sle-module-basesystem15-sp2-pool-x86_64
    sle-product-sles15-sp2-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp2
    sle-module-basesystem15-sp2-updates-x86_64
  ],
  '12-SP4' =>
  %w[
    sles12-sp4-pool-x86_64
    sle-manager-tools12-pool-x86_64-sp4
    sle-module-containers12-pool-x86_64-sp4
    sles12-sp4-updates-x86_64
    sle-manager-tools12-updates-x86_64-sp4
    sle-module-containers12-updates-x86_64-sp4
  ],
  '12-SP5' =>
  %w[
    sles12-sp5-pool-x86_64
    sle-manager-tools12-pool-x86_64-sp5
    sle-module-containers12-pool-x86_64-sp5
    sles12-sp5-updates-x86_64
    sle-manager-tools12-updates-x86_64-sp5
    sle-module-containers12-updates-x86_64-sp5
  ],
  '15-SP1' =>
  %w[
    sle-product-sles15-sp1-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp1
    sle-module-containers15-sp1-pool-x86_64
    sle-module-basesystem15-sp1-pool-x86_64
    sle-module-server-applications15-sp1-pool-x86_64
    sle-product-sles15-sp1-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp1
    sle-module-containers15-sp1-updates-x86_64
    sle-module-basesystem15-sp1-updates-x86_64
    sle-module-server-applications15-sp1-updates-x86_64
  ],
  '15-SP2' =>
  %w[
    sle-product-sles15-sp2-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp2
    sle-manager-tools15-beta-pool-x86_64-sp2
    sle-module-containers15-sp2-pool-x86_64
    sle-module-basesystem15-sp2-pool-x86_64
    sle-module-server-applications15-sp2-pool-x86_64
    sle-product-sles15-sp2-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp2
    sle-manager-tools15-beta-updates-x86_64-sp2
    sle-module-containers15-sp2-updates-x86_64
    sle-module-basesystem15-sp2-updates-x86_64
    sle-module-server-applications15-sp2-updates-x86_64
  ],
  '15-SP3' =>
  %w[
    sle-product-sles15-sp3-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp3
    sle-manager-tools15-beta-pool-x86_64-sp3
    sle-module-containers15-sp3-pool-x86_64
    sle-module-basesystem15-sp3-pool-x86_64
    sle-module-server-applications15-sp3-pool-x86_64
    sle-product-sles15-sp3-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp3
    sle-manager-tools15-beta-updates-x86_64-sp3
    sle-module-containers15-sp3-updates-x86_64
    sle-module-basesystem15-sp3-updates-x86_64
    sle-module-server-applications15-sp3-updates-x86_64
  ]
}.freeze

MIGRATE_SSH_MINION_FROM = '15-SP2'.freeze
MIGRATE_SSH_MINION_TO = '15-SP3'.freeze

OS_REPOS_BY_OS_VERSION = {
  '11-SP4' =>
  %w[
    SLE-Module-Basesystem11-SP4-Pool
    SLE-Module-Basesystem11-SP4-Updates
    SLE-Module-Containers11-SP4-Pool
    SLE-Module-Containers11-SP4-Updates
  ],
  '12-SP3' =>
  %w[
    SLE-Module-Basesystem12-SP3-Pool
    SLE-Module-Basesystem12-SP3-Updates
  ],
  '12-SP4' =>
  %w[
    SLE-Module-Basesystem12-SP4-Pool
    SLE-Module-Basesystem12-SP4-Updates
  ],
  '15-SP1' =>
  %w[
    SLE-Module-Basesystem15-SP1-Pool
    SLE-Module-Basesystem15-SP1-Updates
    SLE-Module-DevTools15-SP1-Pool
    SLE-Module-DevTools15-SP1-Updates
    SLE-Module-Desktop-Applications15-SP1-Pool
    SLE-Module-Desktop-Applications15-SP1-Updates
  ],
  '15-SP2' =>
  %w[
    SLE-Module-Basesystem15-SP2-Pool
    SLE-Module-Basesystem15-SP2-Updates
    SLE-Module-DevTools15-SP2-Pool
    SLE-Module-DevTools15-SP2-Updates
    SLE-Module-Desktop-Applications15-SP2-Pool
    SLE-Module-Desktop-Applications15-SP2-Updates
  ],
  '15-SP3' =>
  %w[
    SLE-Module-Basesystem15-SP3-Pool
    SLE-Module-Basesystem15-SP3-Updates
    SLE-Module-DevTools15-SP3-Pool
    SLE-Module-DevTools15-SP3-Updates
    SLE-Module-Desktop-Applications15-SP3-Pool
    SLE-Module-Desktop-Applications15-SP3-Updates
  ]
}.freeze

SEARCH_VALUE = { 'ceos_minion' => '*centos*',
                 'ubuntu_minion' => '*ubuntu*',
                 'sle_client' => '*cli-sles*',
                 'sle_minion' => '*min-sles*',
                 'ssh_minion' => '*ssh-sles*',
                 'build_host' => '*build*' }.freeze
