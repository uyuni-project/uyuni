# Copyright (c) 2019-2023 SUSE LLC
# Licensed under the terms of the MIT license.

ENV_VAR_BY_HOST = { 'localhost' => 'HOSTNAME',
                    'proxy' => 'PROXY',
                    'server' => 'SERVER',
                    'kvm_server' => 'VIRTHOST_KVM_URL',
                    'sle_minion' => 'MINION',
                    'ssh_minion' => 'SSH_MINION',
                    'rhlike_minion' => 'RHLIKE_MINION',
                    'deblike_minion' => 'DEBLIKE_MINION',
                    'build_host' => 'BUILD_HOST',
                    'salt_migration_minion' => 'MIN_NESTED',
                    # Build Validation environment
                    'sle12sp4_minion' => 'SLE12SP4_MINION',
                    'sle12sp4_ssh_minion' => 'SLE12SP4_SSHMINION',
                    'sle12sp5_minion' => 'SLE12SP5_MINION',
                    'sle12sp5_ssh_minion' => 'SLE12SP5_SSHMINION',
                    'sle15sp1_minion' => 'SLE15SP1_MINION',
                    'sle15sp1_ssh_minion' => 'SLE15SP1_SSHMINION',
                    'sle15sp2_minion' => 'SLE15SP2_MINION',
                    'sle15sp2_ssh_minion' => 'SLE15SP2_SSHMINION',
                    'sle15sp3_minion' => 'SLE15SP3_MINION',
                    'sle15sp3_ssh_minion' => 'SLE15SP3_SSHMINION',
                    'sle15sp4_minion' => 'SLE15SP4_MINION',
                    'sle15sp4_ssh_minion' => 'SLE15SP4_SSHMINION',
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
                    'sle12sp5_buildhost' => 'SLE12SP5_BUILDHOST',
                    'sle15sp4_buildhost' => 'SLE15SP4_BUILDHOST',
                    'monitoring_server' => 'MONITORING_SERVER' }.freeze

ADDRESSES = { 'network'           => '0',
              'sle_minion'        => '3',
              'pxeboot_minion'    => '4',
              'sle12sp5_terminal' => '5',
              'sle15sp4_terminal' => '6',
              'range begin'       => '128',
              'range end'         => '253',
              'proxy'             => '254',
              'broadcast'         => '255' }.freeze

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

# Used for testing software installation/removal in BV
# The value is the package to be installed/removed
PACKAGE_BY_CLIENT = { 'sle_minion' => 'bison',
                      'ssh_minion' => 'bison',
                      'rhlike_client' => 'autoconf',
                      'rhlike_minion' => 'autoconf',
                      'deblike_minion' => 'bison',
                      'sle12sp4_minion' => 'bison',
                      'sle12sp4_ssh_minion' => 'bison',
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
                      'opensuse155arm_ssh_minion' => 'bison' }.freeze

# The values can be found under Software -> Channel List -> Create Channel
# Then have a look at Parent Channel and find the desired name

# For containers we do not have SCC, so we set the Fake Base Channel
# for sle_minion
sle_base_channel =
  if ENV['PROVIDER'].include? 'podman'
    'Fake Base Channel'
  elsif ENV['SERVER'].include?('uyuni') || ENV['SERVER'].include?('suma-pr') || $is_container_server
    'openSUSE Leap 15.5 (x86_64)'
  else
    'SLES15-SP4-Pool'
  end
# Names of our base/parent channels
# The keys are the Twopence targets
# The values can be found in the webUI under Software -> Manage -> Channels -> Create Channel
# Then have a look at the the Parent Channel selections
BASE_CHANNEL_BY_CLIENT = { 'SUSE Manager' =>
                            {
                              'proxy' => 'SLE-Product-SUSE-Manager-Proxy-4.3-Pool',
                              'sle_minion' => sle_base_channel,
                              'ssh_minion' => 'SLES15-SP4-Pool',
                              'rhlike_minion' => 'RHEL8-Pool for x86_64',
                              'deblike_minion' => 'ubuntu-2004-amd64-main',
                              'sle12sp4_minion' => 'SLES12-SP4-Pool',
                              'sle12sp4_ssh_minion' => 'SLES12-SP4-Pool',
                              'sle12sp5_minion' => 'SLES12-SP5-Pool',
                              'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool',
                              'sle12sp5_buildhost' => 'SLES12-SP5-Pool',
                              'sle12sp5_terminal' => 'SLES12-SP5-Pool',
                              'sle15sp1_minion' => 'SLES15-SP1-Pool',
                              'sle15sp1_ssh_minion' => 'SLES15-SP1-Pool',
                              'sle15sp2_minion' => 'SLES15-SP2-Pool',
                              'sle15sp2_ssh_minion' => 'SLES15-SP2-Pool',
                              'sle15sp3_minion' => 'SLES15-SP3-Pool',
                              'sle15sp3_ssh_minion' => 'SLES15-SP3-Pool',
                              'sle15sp4_minion' => 'SLES15-SP4-Pool',
                              'sle15sp4_ssh_minion' => 'SLES15-SP4-Pool',
                              'sle15sp5_minion' => 'SLES15-SP5-Pool',
                              'sle15sp5_ssh_minion' => 'SLES15-SP5-Pool',
                              'slemicro51_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
                              'slemicro51_ssh_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
                              'slemicro52_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
                              'slemicro52_ssh_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
                              'slemicro53_minion' => 'SLE-Micro-5.3-Pool for x86_64',
                              'slemicro53_ssh_minion' => 'SLE-Micro-5.3-Pool for x86_64',
                              'slemicro54_minion' => 'SLE-Micro-5.4-Pool for x86_64',
                              'slemicro54_ssh_minion' => 'SLE-Micro-5.4-Pool for x86_64',
                              'sle15sp4_buildhost' => 'SLES15-SP4-Pool',
                              'monitoring_server' => 'SLES15-SP4-Pool',
                              'sle15sp4_terminal' => 'SLES15-SP4-Pool',
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
                              'ubuntu2004_minion' => 'ubuntu-2004-amd64-main',
                              'ubuntu2004_ssh_minion' => 'ubuntu-2004-amd64-main',
                              'ubuntu2204_minion' => 'ubuntu-2204-amd64-main',
                              'ubuntu2204_ssh_minion' => 'ubuntu-2204-amd64-main',
                              'debian10_minion' => 'debian-10-pool',
                              'debian10_ssh_minion' => 'debian-10-pool',
                              'debian11_minion' => 'debian-11-pool',
                              'debian11_ssh_minion' => 'debian-11-pool',
                              'debian12_minion' => 'debian-12-pool',
                              'debian12_ssh_minion' => 'debian-12-pool',
                              'opensuse154arm_minion' => 'openSUSE-Leap-15.4-Pool for aarch64',
                              'opensuse154arm_ssh_minion' => 'openSUSE-Leap-15.4-Pool for aarch64',
                              'opensuse155arm_minion' => 'openSUSE-Leap-15.5-Pool for aarch64',
                              'opensuse155arm_ssh_minion' => 'openSUSE-Leap-15.5-Pool for aarch64'
                            },
                            'Uyuni' =>
                            {
                              'proxy' => 'openSUSE Leap 15.5 (x86_64)',
                              'sle_minion' => sle_base_channel,
                              'ssh_minion' => 'SLES15-SP4-Pool',
                              'rhlike_minion' => 'RHEL8-Pool for x86_64',
                              'deblike_minion' => 'ubuntu-2004-amd64-main',
                              'sle12sp4_minion' => 'SLES12-SP4-Pool',
                              'sle12sp4_ssh_minion' => 'SLES12-SP4-Pool',
                              'sle12sp5_minion' => 'SLES12-SP5-Pool',
                              'sle12sp5_ssh_minion' => 'SLES12-SP5-Pool',
                              'sle12sp5_buildhost' => 'SLES12-SP5-Pool',
                              'sle12sp5_terminal' => 'SLES12-SP5-Pool',
                              'sle15sp1_minion' => 'SLES15-SP1-Pool',
                              'sle15sp1_ssh_minion' => 'SLES15-SP1-Pool',
                              'sle15sp2_minion' => 'SLES15-SP2-Pool',
                              'sle15sp2_ssh_minion' => 'SLES15-SP2-Pool',
                              'sle15sp3_minion' => 'SLES15-SP3-Pool',
                              'sle15sp3_ssh_minion' => 'SLES15-SP3-Pool',
                              'sle15sp4_minion' => 'SLES15-SP4-Pool',
                              'sle15sp4_ssh_minion' => 'SLES15-SP4-Pool',
                              'sle15sp5_minion' => 'SLES15-SP5-Pool',
                              'sle15sp5_ssh_minion' => 'SLES15-SP5-Pool',
                              'slemicro51_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
                              'slemicro51_ssh_minion' => 'SUSE-MicroOS-5.1-Pool for x86_64',
                              'slemicro52_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
                              'slemicro52_ssh_minion' => 'SUSE-MicroOS-5.2-Pool for x86_64',
                              'slemicro53_minion' => 'SLE-Micro-5.3-Pool for x86_64',
                              'slemicro53_ssh_minion' => 'SLE-Micro-5.3-Pool for x86_64',
                              'slemicro54_minion' => 'SLE-Micro-5.4-Pool for x86_64',
                              'slemicro54_ssh_minion' => 'SLE-Micro-5.4-Pool for x86_64',
                              'sle15sp4_buildhost' => 'SLES15-SP4-Pool',
                              'monitoring_server' => 'SLES15-SP4-Pool',
                              'sle15sp4_terminal' => 'SLES15-SP4-Pool',
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
                              'opensuse155arm_ssh_minion' => 'openSUSE Leap 15.5 (aarch64)'
                            } }.freeze

# Used for creating activation keys
# The keys are the values of BASE_CHANNEL_BY_CLIENT
# The values can be found under Admin -> Setup Wizard -> Products for SUMA
# Select the desired product and have a look at its product channels
# The required product has to be synced before.
# For Uyuni, you have to use `spacewalk-common-channels -l` to get the proper values
LABEL_BY_BASE_CHANNEL = { 'SUSE Manager' =>
                          {
                            'SLE-Product-SUSE-Manager-Proxy-4.3-Pool' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
                            'SLES12-SP4-Pool' => 'sles12-sp4-pool-x86_64',
                            'SLES12-SP5-Pool' => 'sles12-sp5-pool-x86_64',
                            'SLES15-SP1-Pool' => 'sle-product-sles15-sp1-pool-x86_64',
                            'SLES15-SP2-Pool' => 'sle-product-sles15-sp2-pool-x86_64',
                            'SLES15-SP3-Pool' => 'sle-product-sles15-sp3-pool-x86_64',
                            'SLES15-SP4-Pool' => 'sle-product-sles15-sp4-pool-x86_64',
                            'SLES15-SP5-Pool' => 'sle-product-sles15-sp5-pool-x86_64',
                            'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
                            'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
                            'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
                            'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
                            'almalinux9 for x86_64' => 'no-appstream-alma-9-result-almalinux9-x86_64',
                            'Fake Base Channel' => 'fake_base_channel',
                            'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
                            'EL9-Pool for x86_64' => 'no-appstream-liberty-9-result-el9-pool-x86_64',
                            'oraclelinux9 for x86_64' => 'no-appstream-oracle-9-result-oraclelinux9-x86_64',
                            'RHEL8-Pool for x86_64' => 'no-appstream-8-result-rhel8-pool-x86_64',
                            'rockylinux-9 for x86_64' => 'no-appstream-9-result-rockylinux-9-x86_64',
                            'ubuntu-2004-amd64-main' => 'ubuntu-2004-amd64-main-amd64',
                            'ubuntu-2204-amd64-main' => 'ubuntu-2204-amd64-main-amd64',
                            'debian-10-pool' => 'debian-10-pool-amd64',
                            'debian-11-pool' => 'debian-11-pool-amd64',
                            'openSUSE-Leap-15.4-Pool for aarch64' => 'opensuse-leap-15.4-pool-aarch64',
                            'openSUSE-Leap-15.5-Pool for aarch64' => 'opensuse-leap-15.5-pool-aarch64'
                          },
                          'Uyuni' =>
                          {
                            'openSUSE Leap 15.5 (x86_64)' => 'opensuse_leap15_5-x86_64',
                            'openSUSE Leap 15.4 (x86_64)' => 'opensuse_leap15_4-x86_64',
                            'SLES12-SP4-Pool' => 'sles12-sp4-pool-x86_64',
                            'SLES12-SP5-Pool' => 'sles12-sp5-pool-x86_64',
                            'SLES15-SP1-Pool' => 'sle-product-sles15-sp1-pool-x86_64',
                            'SLES15-SP2-Pool' => 'sle-product-sles15-sp2-pool-x86_64',
                            'SLES15-SP3-Pool' => 'sle-product-sles15-sp3-pool-x86_64',
                            'SLES15-SP4-Pool' => 'sle-product-sles15-sp4-pool-x86_64',
                            'SLES15-SP5-Pool' => 'sle-product-sles15-sp5-pool-x86_64',
                            'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
                            'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
                            'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
                            'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
                            'AlmaLinux 9 (x86_64)' => 'no-appstream-alma-9-result-almalinux9-x86_64',
                            'Fake Base Channel' => 'fake_base_channel',
                            'CentOS 7 (x86_64)' => 'centos7-x86_64',
                            'EL9-Pool for x86_64' => 'no-appstream-liberty-9-result-el9-pool-x86_64',
                            'Oracle Linux 9 (x86_64)' => 'no-appstream-oracle-9-result-oraclelinux9-x86_64',
                            'Rocky Linux 8 (x86_64)' => 'no-appstream-8-result-rockylinux8-x86_64',
                            'Rocky Linux 9 (x86_64)' => 'no-appstream-9-result-rockylinux9-x86_64',
                            'Ubuntu 20.04 LTS AMD64 Base for Uyuni' => 'ubuntu-20.04-pool-amd64-uyuni',
                            'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => 'ubuntu-22.04-pool-amd64-uyuni',
                            'Debian 10 (buster) pool for amd64 for Uyuni' => 'debian-10-pool-amd64-uyuni',
                            'Debian 11 (bullseye) pool for amd64 for Uyuni' => 'debian-11-pool-amd64-uyuni',
                            'openSUSE Leap 15.4 (aarch64)' => 'opensuse_leap15_4-aarch64',
                            'openSUSE Leap 15.5 (aarch64)' => 'opensuse_leap15_5-aarch64'
                          } }.freeze

# Used for creating bootstrap repositories
# The keys are the values of BASE_CHANNEL_BY_CLIENT
# The values can be found out on the server by running 'mgr-create-bootstrap-repo'
# Then select the correct name for the product you want
CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL = { 'SUSE Manager' =>
                                          {
                                            'SLE-Product-SUSE-Manager-Proxy-4.3-Pool' => 'SUMA-43-PROXY-x86_64',
                                            'SLES12-SP4-Pool' => 'SLE-12-SP4-x86_64',
                                            'SLES12-SP5-Pool' => 'SLE-12-SP5-x86_64',
                                            'SLES15-SP1-Pool' => 'SLE-15-SP1-x86_64',
                                            'SLES15-SP2-Pool' => 'SLE-15-SP2-x86_64',
                                            'SLES15-SP3-Pool' => 'SLE-15-SP3-x86_64',
                                            'SLES15-SP4-Pool' => 'SLE-15-SP4-x86_64',
                                            'SLES15-SP5-Pool' => 'SLE-15-SP5-x86_64',
                                            'SUSE-MicroOS-5.1-Pool for x86_64' => 'SLE-MICRO-5.1-x86_64',
                                            'SUSE-MicroOS-5.2-Pool for x86_64' => 'SLE-MICRO-5.2-x86_64',
                                            'SLE-Micro-5.3-Pool for x86_64' => 'SLE-MICRO-5.3-x86_64',
                                            'SLE-Micro-5.4-Pool for x86_64' => 'SLE-MICRO-5.4-x86_64',
                                            'almalinux9 for x86_64' => 'almalinux-9-x86_64',
                                            'Fake Base Channel' => 'fake_base_channel-x86_64',
                                            'RHEL x86_64 Server 7' => 'RES7-x86_64',
                                            'EL9-Pool for x86_64' => 'SUSE-LibertyLinux9-x86_64',
                                            'oraclelinux9 for x86_64' => 'oracle-9-x86_64',
                                            'RHEL8-Pool for x86_64' => 'SLE-ES8-x86_64',
                                            'rockylinux-9 for x86_64' => 'rockylinux-9-x86_64',
                                            'ubuntu-2004-amd64-main' => 'ubuntu-20.04-amd64',
                                            'ubuntu-2204-amd64-main' => 'ubuntu-22.04-amd64',
                                            'debian-10-pool' => 'debian10-amd64',
                                            'debian-11-pool' => 'debian11-amd64',
                                            'debian-12-pool' => 'debian12-amd64',
                                            'openSUSE-Leap-15.4-Pool for aarch64' => 'openSUSE-Leap-15.4-aarch64',
                                            'openSUSE-Leap-15.5-Pool for aarch64' => 'openSUSE-Leap-15.5-aarch64'
                                          },
                                          'Uyuni' =>
                                          {
                                            'openSUSE Leap 15.5 (x86_64)' => 'openSUSE-Leap-15.5-x86_64-uyuni',
                                            'openSUSE Leap 15.4 (x86_64)' => 'openSUSE-Leap-15.4-x86_64-uyuni',
                                            'SLES12-SP4-Pool' => 'SLE-12-SP4-x86_64',
                                            'SLES12-SP5-Pool' => 'SLE-12-SP5-x86_64',
                                            'SLES15-SP1-Pool' => 'SLE-15-SP1-x86_64',
                                            'SLES15-SP2-Pool' => 'SLE-15-SP2-x86_64',
                                            'SLES15-SP3-Pool' => 'SLE-15-SP3-x86_64',
                                            'SLES15-SP4-Pool' => 'SLE-15-SP4-x86_64',
                                            'SLES15-SP5-Pool' => 'SLE-15-SP5-x86_64',
                                            'SUSE-MicroOS-5.1-Pool for x86_64' => 'SLE-MICRO-5.1-x86_64',
                                            'SUSE-MicroOS-5.2-Pool for x86_64' => 'SLE-MICRO-5.2-x86_64',
                                            'SLE-Micro-5.3-Pool for x86_64' => 'SLE-MICRO-5.3-x86_64',
                                            'SLE-Micro-5.4-Pool for x86_64' => 'SLE-MICRO-5.4-x86_64',
                                            'AlmaLinux 9 (x86_64)' => 'almalinux-9-x86_64-uyuni',
                                            'Fake Base Channel' => 'fake_base_channel-x86_64',
                                            'CentOS 7 (x86_64)' => 'centos-7-x86_64-uyuni',
                                            'EL9-Pool for x86_64' => 'SUSE-LibertyLinux9-x86_64',
                                            'Oracle Linux 9 (x86_64)' => 'oracle-9-x86_64-uyuni',
                                            'Rocky Linux 8 (x86_64)' => 'rockylinux-8-x86_64-uyuni',
                                            'Rocky Linux 9 (x86_64)' => 'rockylinux-9-x86_64-uyuni',
                                            'Ubuntu 20.04 LTS AMD64 Base for Uyuni' => 'ubuntu-20.04-amd64-uyuni',
                                            'Ubuntu 22.04 LTS AMD64 Base for Uyuni' => 'ubuntu-22.04-amd64-uyuni',
                                            'Debian 10 (buster) pool for amd64 for Uyuni' => 'debian10-amd64-uyuni',
                                            'Debian 11 (bullseye) pool for amd64 for Uyuni' => 'debian11-amd64-uyuni',
                                            'Debian 12 (bookworm) pool for amd64 for Uyuni' => 'debian12-amd64-uyuni',
                                            'openSUSE Leap 15.4 (aarch64)' => 'openSUSE-Leap-15.4-aarch64-uyuni',
                                            'openSUSE Leap 15.5 (aarch64)' => 'openSUSE-Leap-15.5-aarch64-uyuni'
                                          } }.freeze

# Used for creating bootstrap repositories
# The values can be found under Admin -> Setup Wizard -> Products for SUMA
# Select the desired product and have a look at its product channels
# The required product has to be synced before.
# For Uyuni, you have to use `spacewalk-common-channels -l` with the appended architecture
# e.g. almalinux9 -> almalinux9-x86_64
PARENT_CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL = { 'SUSE Manager' =>
                                                  {
                                                    'SLE-Product-SUSE-Manager-Proxy-4.3-Pool' => 'sle-product-suse-manager-proxy-4.3-pool-x86_64',
                                                    'SLES12-SP4-Pool' => nil,
                                                    'SLES12-SP5-Pool' => nil,
                                                    'SLES15-SP1-Pool' => 'sle-product-sles15-sp1-pool-x86_64',
                                                    'SLES15-SP2-Pool' => 'sle-product-sles15-sp2-pool-x86_64',
                                                    'SLES15-SP3-Pool' => 'sle-product-sles15-sp3-pool-x86_64',
                                                    'SLES15-SP4-Pool' => 'sle-product-sles15-sp4-pool-x86_64',
                                                    'SLES15-SP5-Pool' => 'sle-product-sles15-sp5-pool-x86_64',
                                                    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
                                                    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
                                                    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
                                                    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
                                                    'almalinux9 for x86_64' => nil,
                                                    'Fake Base Channel' => nil,
                                                    'RHEL x86_64 Server 7' => 'rhel-x86_64-server-7',
                                                    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
                                                    'oraclelinux9 for x86_64' => nil,
                                                    'RHEL8-Pool for x86_64' => nil,
                                                    'rockylinux-9 for x86_64' => nil,
                                                    'ubuntu-2004-amd64-main' => nil,
                                                    'ubuntu-2204-amd64-main' => nil,
                                                    'debian-10-pool' => 'debian-10-pool-amd64',
                                                    'debian-11-pool' => 'debian-11-pool-amd64',
                                                    'debian-12-pool' => 'debian-12-pool-amd64',
                                                    'openSUSE-Leap-15.4-Pool for aarch64' => nil,
                                                    'openSUSE-Leap-15.5-Pool for aarch64' => nil
                                                  },
                                                  'Uyuni' =>
                                                  {
                                                    'openSUSE Leap 15.5 (x86_64)' => nil,
                                                    'openSUSE Leap 15.4 (x86_64)' => nil,
                                                    'SLES12-SP4-Pool' => nil,
                                                    'SLES12-SP5-Pool' => nil,
                                                    'SLES15-SP1-Pool' => 'sle-product-sles15-sp1-pool-x86_64',
                                                    'SLES15-SP2-Pool' => 'sle-product-sles15-sp2-pool-x86_64',
                                                    'SLES15-SP3-Pool' => 'sle-product-sles15-sp3-pool-x86_64',
                                                    'SLES15-SP4-Pool' => 'sle-product-sles15-sp4-pool-x86_64',
                                                    'SLES15-SP5-Pool' => 'sle-product-sles15-sp5-pool-x86_64',
                                                    'SUSE-MicroOS-5.1-Pool for x86_64' => 'suse-microos-5.1-pool-x86_64',
                                                    'SUSE-MicroOS-5.2-Pool for x86_64' => 'suse-microos-5.2-pool-x86_64',
                                                    'SLE-Micro-5.3-Pool for x86_64' => 'sle-micro-5.3-pool-x86_64',
                                                    'SLE-Micro-5.4-Pool for x86_64' => 'sle-micro-5.4-pool-x86_64',
                                                    'almalinux9 for x86_64' => nil,
                                                    'Fake Base Channel' => nil,
                                                    'RHEL x86_64 Server 7' => 'centos-7-x86_64-uyuni',
                                                    'EL9-Pool for x86_64' => 'el9-pool-x86_64',
                                                    'oraclelinux9 for x86_64' => nil,
                                                    'RHEL8-Pool for x86_64' => nil,
                                                    'rockylinux-9 for x86_64' => nil,
                                                    'ubuntu-2004-amd64-main' => nil,
                                                    'ubuntu-2204-amd64-main' => nil,
                                                    'debian-10-pool' => 'debian10-amd64-uyuni',
                                                    'debian-11-pool' => 'debian11-amd64-uyuni',
                                                    'debian-12-pool' => 'debian12-amd64-uyuni',
                                                    'openSUSE-Leap-15.4-Pool for aarch64' => nil,
                                                    'openSUSE-Leap-15.5-Pool for aarch64' => nil
                                                  } }.freeze

PKGARCH_BY_CLIENT = { 'proxy' => 'x86_64',
                      'sle_minion' => 'x86_64',
                      'ssh_minion' => 'x86_64',
                      'rhlike_minion' => 'x86_64',
                      'deblike_minion' => 'amd64',
                      'sle12sp4_minion' => 'x86_64',
                      'sle12sp4_ssh_minion' => 'x86_64',
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
                      'opensuse155arm_ssh_minion' => 'aarch64' }.freeze

CHANNEL_TO_SYNCH_BY_OS_VERSION = {
  # 'default' is required for auto-installation tests.
  'default' =>
  %w[
    sle-product-sles15-sp4-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp4
    sle-manager-tools15-pool-x86_64-sled-sp4
    sle-manager-tools15-beta-pool-x86_64-sp4
    sle-module-containers15-sp4-pool-x86_64
    sle-module-basesystem15-sp4-pool-x86_64
    sle-module-server-applications15-sp4-pool-x86_64
    sle-product-sles15-sp4-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp4
    sle-manager-tools15-updates-x86_64-sled-sp4
    sle-manager-tools15-beta-updates-x86_64-sp4
    sle-module-containers15-sp4-updates-x86_64
    sle-module-basesystem15-sp4-updates-x86_64
    sle-module-server-applications15-sp4-updates-x86_64
    sle15-sp4-installer-updates-x86_64
    sle-module-desktop-applications15-sp4-pool-x86_64
    sle-module-desktop-applications15-sp4-updates-x86_64
    sle-module-devtools15-sp4-pool-x86_64
    sle-module-devtools15-sp4-updates-x86_64
    sle-module-containers15-sp4-pool-x86_64
    sle-module-containers15-sp4-updates-x86_64
  ],
  '15-SP3' =>
  %w[
    sle-product-sles15-sp3-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp3
    sle-manager-tools15-pool-x86_64-sled-sp3
    sle-manager-tools15-beta-pool-x86_64-sp3
    sle-module-containers15-sp3-pool-x86_64
    sle-module-basesystem15-sp3-pool-x86_64
    sle-module-server-applications15-sp3-pool-x86_64
    sle-product-sles15-sp3-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp3
    sle-manager-tools15-updates-x86_64-sled-sp3
    sle-manager-tools15-beta-updates-x86_64-sp3
    sle-module-containers15-sp3-updates-x86_64
    sle-module-basesystem15-sp3-updates-x86_64
    sle-module-server-applications15-sp3-updates-x86_64
    sle15-sp3-installer-updates-x86_64
    sle-module-desktop-applications15-sp3-pool-x86_64
    sle-module-desktop-applications15-sp3-updates-x86_64
    sle-module-devtools15-sp3-pool-x86_64
    sle-module-devtools15-sp3-updates-x86_64
    sle-module-containers15-sp3-pool-x86_64
    sle-module-containers15-sp3-updates-x86_64
  ],
  '15-SP4' =>
  %w[
    sle-product-sles15-sp4-pool-x86_64
    sle-manager-tools15-pool-x86_64-sp4
    sle-manager-tools15-pool-x86_64-sled-sp4
    sle-manager-tools15-beta-pool-x86_64-sp4
    sle-module-containers15-sp4-pool-x86_64
    sle-module-basesystem15-sp4-pool-x86_64
    sle-module-server-applications15-sp4-pool-x86_64
    sle-product-sles15-sp4-updates-x86_64
    sle-manager-tools15-updates-x86_64-sp4
    sle-manager-tools15-updates-x86_64-sled-sp4
    sle-manager-tools15-beta-updates-x86_64-sp4
    sle-module-containers15-sp4-updates-x86_64
    sle-module-basesystem15-sp4-updates-x86_64
    sle-module-server-applications15-sp4-updates-x86_64
    sle15-sp4-installer-updates-x86_64
    sle-module-desktop-applications15-sp4-pool-x86_64
    sle-module-desktop-applications15-sp4-updates-x86_64
    sle-module-devtools15-sp4-pool-x86_64
    sle-module-devtools15-sp4-updates-x86_64
    sle-module-containers15-sp4-pool-x86_64
    sle-module-containers15-sp4-updates-x86_64
    sle-module-suse-manager-proxy-4.3-pool-x86_64
    sle-module-suse-manager-proxy-4.3-updates-x86_64
    sle-module-server-applications15-sp4-pool-x86_64-proxy-4.3
    sle-module-server-applications15-sp4-updates-x86_64-proxy-4.3
    sle-module-basesystem15-sp4-pool-x86_64-proxy-4.3
    sle-module-basesystem15-sp4-updates-x86_64-proxy-4.3
    sle-product-suse-manager-proxy-4.3-pool-x86_64
    sle-product-suse-manager-proxy-4.3-updates-x86_64
    sle-product-suse-manager-retail-branch-server-4.3-pool-x86_64
    sle-product-suse-manager-retail-branch-server-4.3-updates-x86_64
    sle-module-suse-manager-retail-branch-server-4.3-pool-x86_64
    sle-module-suse-manager-retail-branch-server-4.3-updates-x86_64
    sle-module-basesystem15-sp4-pool-x86_64-smrbs-4.3
    sle-module-basesystem15-sp4-updates-x86_64-smrbs-4.3
    sle-module-suse-manager-proxy-4.3-pool-x86_64-smrbs
    sle-module-suse-manager-proxy-4.3-updates-x86_64-smrbs
    sle-module-server-applications15-sp4-updates-x86_64-smrbs-4.3
  ],
  '8' =>
  %w[
    res8-manager-tools-pool-x86_64
    res8-manager-tools-updates-x86_64
  ],
  '15.4' =>
  %w[
    opensuse_leap15_4-x86_64
    opensuse_leap15_4-x86_64-non-oss
    opensuse_leap15_4-x86_64-non-oss-updates
    opensuse_leap15_4-x86_64-updates
    opensuse_leap15_4-x86_64-backports-updates
    opensuse_leap15_4-x86_64-sle-updates
    uyuni-proxy-devel-leap-x86_64
    opensuse_leap15_4-uyuni-client-x86_64
  ],
  '15.5' =>
  %w[
    opensuse_leap15_5-x86_64
    opensuse_leap15_5-x86_64-non-oss
    opensuse_leap15_5-x86_64-non-oss-updates
    opensuse_leap15_5-x86_64-updates
    opensuse_leap15_5-x86_64-backports-updates
    opensuse_leap15_5-x86_64-sle-updates
    uyuni-proxy-devel-leap-x86_64
    opensuse_leap15_5-uyuni-client-x86_64
  ]
}.freeze

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
  ],
  '15-SP4' =>
  %w[
    SLE-Module-Basesystem15-SP4-Pool
    SLE-Module-Basesystem15-SP4-Updates
    SLE-Module-DevTools15-SP4-Pool
    SLE-Module-DevTools15-SP4-Updates
    SLE-Module-Desktop-Applications15-SP4-Pool
    SLE-Module-Desktop-Applications15-SP4-Updates
  ]
}.freeze
