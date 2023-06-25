# Copyright 2021-2023 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning the different
### kinds of minions as well as PXE boot and Retail.

# This function returns the net prefix, caching it
def net_prefix
  $net_prefix = $private_net.sub(%r{\.0+/24$}, '.') if $net_prefix.nil?
  $net_prefix
end

# extract various data from Retail yaml configuration
def read_terminals_from_yaml
  name = File.dirname(__FILE__) + '/../upload_files/massive-import-terminals.yml'
  tree = YAML.load_file(name)
  tree['branches'].values[0]['terminals'].keys
end

def read_branch_prefix_from_yaml
  name = File.dirname(__FILE__) + '/../upload_files/massive-import-terminals.yml'
  tree = YAML.load_file(name)
  tree['branches'].values[0]['branch_prefix']
end

# determine profile for PXE boot and terminal tests
def compute_image(host)
  # TODO: now that the terminals derive from sumaform's pxe_boot module,
  #       we could also specify the desired image as an environment variable
  case host
  when 'pxeboot_minion'
    $pxeboot_image
  when 'sle12sp5_terminal'
    'sles12sp5o'
  when 'sle15sp3_terminal'
    'sles15sp3o'
  when 'sle15sp4_terminal'
    'sles15sp4o'
  else
    raise "Is #{host} a supported terminal?"
  end
end

def compute_kiwi_profile_filename(host)
  image = compute_image(host)
  case image
  when 'sles15sp3', 'sles15sp3o', 'sles15sp4', 'sles15sp4o'
    # 'Kiwi/POS_Image-JeOS7_42' for 4.2 branch
    $product == 'Uyuni' ? 'Kiwi/POS_Image-JeOS7_uyuni' : 'Kiwi/POS_Image-JeOS7_head'
  when 'sles15sp2', 'sles15sp2o'
    'Kiwi/POS_Image-JeOS7_41'
  when 'sles15sp1', 'sles15sp1o'
    raise 'This is not a supported image version.'
  when 'sles12sp5', 'sles12sp5o'
    # 'Kiwi/POS_Image-JeOS6_42' for 4.2 branch
    'Kiwi/POS_Image-JeOS6_head'
  else
    raise "Is #{image} a supported image version?"
  end
end

def compute_kiwi_profile_name(host)
  image = compute_image(host)
  case image
  when 'sles15sp3', 'sles15sp3o', 'sles15sp4', 'sles15sp4o'
    # 'POS_Image_JeOS7_42' for 4.2 branch
    $product == 'Uyuni' ? 'POS_Image_JeOS7_uyuni' : 'POS_Image_JeOS7_head'
  when 'sles15sp2', 'sles15sp2o'
    'POS_Image_JeOS7_41'
  when 'sles15sp1', 'sles15sp1o'
    raise 'This is not a supported image version.'
  when 'sles12sp5', 'sles12sp5o'
    # 'POS_Image_JeOS6_42' for 4.2 branch
    'POS_Image_JeOS6_head'
  else
    raise "Is #{image} a supported image version?"
  end
end

def compute_kiwi_profile_version(host)
  image = compute_image(host)
  case image
  when 'sles15sp3', 'sles15sp3o', 'sles15sp4', 'sles15sp4o'
    '7.0.0'
  when 'sles15sp1', 'sles15sp1o'
    raise 'This is not a supported image version.'
  when 'sles12sp5', 'sles12sp5o'
    '6.0.0'
  else
    raise "Is #{image} a supported image version?"
  end
end

When(/^I (enable|disable) repositories (before|after) installing branch server$/) do |action, _when|
  os_version = $proxy.os_version
  os_family = $proxy.os_family

  # Distribution
  repos = 'os_pool_repo os_update_repo testing_overlay_devel_repo'
  log $proxy.run("zypper mr --#{action} #{repos}")

  # Server Applications, proxy product and modules, proxy devel
  if os_family =~ /^sles/ && os_version =~ /^15/
    repos = 'proxy_module_pool_repo proxy_module_update_repo ' \
            'proxy_product_pool_repo proxy_product_update_repo ' \
            'proxy_devel_releasenotes_repo proxy_devel_repo ' \
            'module_server_applications_pool_repo module_server_applications_update_repo'

  elsif os_family =~ /^opensuse/
    repos = 'proxy_pool_repo'
  end
  log $proxy.run("zypper mr --#{action} #{repos}")
end

When(/^I start tftp on the proxy$/) do
  case $product
  # TODO: Should we handle this in Sumaform?
  when 'Uyuni'
    step %(I enable repositories before installing branch server)
    cmd = 'zypper --non-interactive --ignore-unknown remove atftp && ' \
          'zypper --non-interactive install tftp && ' \
          'systemctl enable tftp.service && ' \
          'systemctl start tftp.service'
    $proxy.run(cmd)
    step %(I disable repositories after installing branch server)
  else
    cmd = 'systemctl enable tftp.service && systemctl start tftp.service'
    $proxy.run(cmd)
  end
end

When(/^I stop tftp on the proxy$/) do
  $proxy.run('systemctl stop tftp.service')
end

When(/^I set up the private network on the terminals$/) do
  proxy = net_prefix + ADDRESSES['proxy']
  # /etc/sysconfig/network/ifcfg-eth1 and /etc/resolv.conf
  nodes = [$minion]
  conf = "STARTMODE='auto'\\nBOOTPROTO='dhcp'"
  file = '/etc/sysconfig/network/ifcfg-eth1'
  script2 = "-e '/^#/d' -e 's/^search /search example.org /' -e '$anameserver #{proxy}' -e '/^nameserver /d'"
  file2 = '/etc/resolv.conf'
  nodes.each do |node|
    next if node.nil?
    node.run("echo -e \"#{conf}\" > #{file} && sed -i #{script2} #{file2} && ifup eth1")
  end
  # /etc/sysconfig/network-scripts/ifcfg-eth1 and /etc/sysconfig/network
  nodes = [$rhlike_minion]
  file = '/etc/sysconfig/network-scripts/ifcfg-eth1'
  conf2 = 'GATEWAYDEV=eth0'
  file2 = '/etc/sysconfig/network'
  nodes.each do |node|
    next if node.nil?
    domain, _code = node.run("grep '^search' /etc/resolv.conf | sed 's/^search//'")
    conf = "DOMAIN='#{domain.strip}'\\nDEVICE='eth1'\\nSTARTMODE='auto'\\nBOOTPROTO='dhcp'\\nDNS1='#{proxy}'"
    service =
      if node.os_family =~ /^rocky/
        'NetworkManager'
      else
        'network'
      end
    node.run("echo -e \"#{conf}\" > #{file} && echo -e \"#{conf2}\" > #{file2} && systemctl restart #{service}")
  end
  # /etc/netplan/01-netcfg.yaml
  nodes = [$deblike_minion]
  source = File.dirname(__FILE__) + '/../upload_files/01-netcfg.yaml'
  dest = '/etc/netplan/01-netcfg.yaml'
  nodes.each do |node|
    next if node.nil?
    return_code = file_inject(node, source, dest)
    raise 'File injection failed' unless return_code.zero?
    node.run('netplan apply')
  end
  # PXE boot minion
  if $pxeboot_mac
    step %(I restart the network on the PXE boot minion)
  end
end

Then(/^terminal "([^"]*)" should have got a retail network IP address$/) do |host|
  node = get_target(host)
  output, return_code = node.run('ip -4 address show eth1')
  raise "Terminal #{host} did not get an address on eth1: #{output}" unless return_code.zero? and output.include? net_prefix
end

Then(/^name resolution should work on terminal "([^"]*)"$/) do |host|
  node = get_target(host)
  # we need "host" utility
  step "I install package \"bind-utils\" on this \"#{host}\""
  # direct name resolution
  %w[proxy.example.org dns.google.com].each do |dest|
    output, return_code = node.run("host #{dest}", check_errors: false)
    raise "Direct name resolution of #{dest} on terminal #{host} doesn't work: #{output}" unless return_code.zero?
    log "#{output}"
  end
  # reverse name resolution
  [node.private_ip, '8.8.8.8'].each do |dest|
    output, return_code = node.run("host #{dest}", check_errors: false)
    raise "Reverse name resolution of #{dest} on terminal #{host} doesn't work: #{output}" unless return_code.zero?
    log "#{output}"
  end
end

When(/^I restart the network on the PXE boot minion$/) do
  # We have no IPv4 address on that machine yet,
  # so the only way to contact it is via IPv6 link-local.
  # We convert MAC address to IPv6 link-local address:
  mac = $pxeboot_mac.tr(':', '')
  hex = ((mac[0..5] + 'fffe' + mac[6..11]).to_i(16) ^ 0x0200000000000000).to_s(16)
  ipv6 = 'fe80::' + hex[0..3] + ':' + hex[4..7] + ':' + hex[8..11] + ':' + hex[12..15] + '%eth1'
  file = 'restart-network-pxeboot.exp'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = '/tmp/' + file
  return_code = file_inject($proxy, source, dest)
  raise 'File injection failed' unless return_code.zero?
  # We have no direct access to the PXE boot minion
  # so we run the command from the proxy
  $proxy.run("expect -f /tmp/#{file} #{ipv6}")
end

When(/^I reboot the (Retail|Cobbler) terminal "([^"]*)"$/) do |context, host|
  # we might have no or any IPv4 address on that machine
  # convert MAC address to IPv6 link-local address
  if host == 'pxeboot_minion'
    mac = $pxeboot_mac
  elsif host == 'sle12sp5_terminal'
    mac = $sle12sp5_terminal_mac
  elsif host == 'sle15sp4_terminal'
    mac = $sle15sp4_terminal_mac
  end
  mac = mac.tr(':', '')
  hex = ((mac[0..5] + 'fffe' + mac[6..11]).to_i(16) ^ 0x0200000000000000).to_s(16)
  ipv6 = 'fe80::' + hex[0..3] + ':' + hex[4..7] + ':' + hex[8..11] + ':' + hex[12..15] + '%eth1'
  log "Rebooting #{ipv6}..."
  file = 'reboot-pxeboot.exp'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = '/tmp/' + file
  return_code = file_inject($proxy, source, dest)
  raise 'File injection failed' unless return_code.zero?
  $proxy.run("expect -f /tmp/#{file} #{ipv6} #{context}")
end

When(/^I create bootstrap script for "([^"]+)" hostname and set the activation key "([^"]*)" in the bootstrap script on the proxy$/) do |host, key|
  # WORKAROUND: Revert once pxeboot autoinstallation contains venv-salt-minion
  # force_bundle = $use_salt_bundle ? '--force-bundle' : ''
  # $proxy.run("mgr-bootstrap #{force_bundle}")
  $proxy.run("mgr-bootstrap --hostname=#{host}")

  $proxy.run("sed -i '/^ACTIVATION_KEYS=/c\\ACTIVATION_KEYS=#{key}' /srv/www/htdocs/pub/bootstrap/bootstrap.sh")
  output, _code = $proxy.run('cat /srv/www/htdocs/pub/bootstrap/bootstrap.sh')
  raise "Key: #{key} not included" unless output.include? key
  raise "Hostname: #{host} not included" unless output.include? host
end

When(/^I bootstrap pxeboot minion via bootstrap script on the proxy$/) do
  file = 'bootstrap-pxeboot.exp'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = '/tmp/' + file
  return_code = file_inject($proxy, source, dest)
  raise 'File injection failed' unless return_code.zero?
  ipv4 = net_prefix + ADDRESSES['pxeboot_minion']
  $proxy.run("expect -f /tmp/#{file} #{ipv4}", verbose: true)
end

When(/^I accept key of pxeboot minion in the Salt master$/) do
  $server.run('salt-key -y --accept=pxeboot.example.org')
end

When(/^I install the GPG key of the test packages repository on the PXE boot minion$/) do
  file = 'uyuni.key'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = '/tmp/' + file
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  system_name = get_system_name('pxeboot_minion')
  $server.run("salt-cp #{system_name} #{dest} #{dest}")
  $server.run("salt #{system_name} cmd.run 'rpmkeys --import #{dest}'")
end

When(/^I wait until Salt client is inactive on the PXE boot minion$/) do
  file = 'wait-end-of-cleanup-pxeboot.exp'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = '/tmp/' + file
  return_code = file_inject($proxy, source, dest)
  raise 'File injection failed' unless return_code.zero?
  ipv4 = net_prefix + ADDRESSES['pxeboot_minion']
  $proxy.run("expect -f /tmp/#{file} #{ipv4}")
end

When(/^I prepare the retail configuration file on server$/) do
  source = File.dirname(__FILE__) + '/../upload_files/massive-import-terminals.yml'
  dest = '/tmp/massive-import-terminals.yml'
  return_code = file_inject($server, source, dest)
  raise "File #{file} couldn't be copied to server" unless return_code.zero?

  sed_values = "s/<PROXY_HOSTNAME>/#{$proxy.full_hostname}/; "
  sed_values << "s/<NET_PREFIX>/#{net_prefix}/; "
  sed_values << "s/<PROXY>/#{ADDRESSES['proxy']}/; "
  sed_values << "s/<RANGE_BEGIN>/#{ADDRESSES['range begin']}/; "
  sed_values << "s/<RANGE_END>/#{ADDRESSES['range end']}/; "
  sed_values << "s/<PXEBOOT>/#{ADDRESSES['pxeboot_minion']}/; "
  sed_values << "s/<PXEBOOT_MAC>/#{$pxeboot_mac}/; "
  sed_values << "s/<MINION>/#{ADDRESSES['sle_minion']}/; "
  sed_values << "s/<MINION_MAC>/#{get_mac_address('sle_minion')}/; "
  sed_values << "s/<IMAGE>/#{compute_kiwi_profile_name('pxeboot_minion')}/"
  $server.run("sed -i '#{sed_values}' #{dest}")
end

When(/^I import the retail configuration using retail_yaml command$/) do
  filepath = '/tmp/massive-import-terminals.yml'
  $server.run("retail_yaml --api-user admin --api-pass admin --from-yaml #{filepath}")
end

# Click on the terminal
When(/^I follow "([^"]*)" terminal$/) do |host|
  domain = read_branch_prefix_from_yaml
  if !host.include? 'pxeboot'
    step %(I follow "#{domain}.#{host}")
  else
    step %(I follow "#{host}.#{domain}")
  end
end

Then(/^I should see the terminals imported from the configuration file$/) do
  terminals = read_terminals_from_yaml
  terminals.each { |terminal| step %(I wait until I see the "#{terminal}" system, refreshing the page) }
end

Then(/^I should not see any terminals imported from the configuration file$/) do
  terminals = read_terminals_from_yaml
  terminals.each do |terminal|
    next if (terminal.include? 'minion') || (terminal.include? 'client')
    step %(I should not see a "#{terminal}" text)
  end
end

When(/^I delete all the imported terminals$/) do
  terminals = read_terminals_from_yaml
  terminals.each do |terminal|
    next if (terminal.include? 'minion') || (terminal.include? 'client')
    log "Deleting terminal with name: #{terminal}"
    steps %(
      When I follow "#{terminal}" terminal
      And I follow "Delete System"
      And I should see a "Confirm System Profile Deletion" text
      And I click on "Delete Profile"
      Then I should see a "has been deleted" text
    )
  end
end

When(/^I enter the local IP address of "([^"]*)" in (.*) field$/) do |host, field|
  fieldids = {
    'IP'                              => 'branch_network#ip',
    'domain name server'              => 'dhcpd#domain_name_servers#0',
    'network IP'                      => 'dhcpd#subnets#0#$key',
    'dynamic IP range begin'          => 'dhcpd#subnets#0#range#0',
    'dynamic IP range end'            => 'dhcpd#subnets#0#range#1',
    'broadcast address'               => 'dhcpd#subnets#0#broadcast_address',
    'routers'                         => 'dhcpd#subnets#0#routers#0',
    'next server'                     => 'dhcpd#subnets#0#next_server',
    'pxeboot next server'             => 'dhcpd#hosts#1#next_server',
    'first reserved IP'               => 'dhcpd#hosts#0#fixed_address',
    'second reserved IP'              => 'dhcpd#hosts#1#fixed_address',
    'internal network address'        => 'tftpd#listen_ip',
    'vsftpd internal network address' => 'vsftpd_config#listen_address'
  }
  fill_in(fieldids[field], with: net_prefix + ADDRESSES[host], fill_options: { clear: :backspace })
end

When(/^I enter "([^"]*)" in (.*) field$/) do |value, field|
  fieldids = {
    'NIC'                          => 'branch_network#nic',
    'domain name'                  => 'dhcpd#domain_name',
    'listen interfaces'            => 'dhcpd#listen_interfaces#0',
    'network mask'                 => 'dhcpd#subnets#0#netmask',
    'filename'                     => 'dhcpd#subnets#0#filename',
    'pxeboot filename'             => 'dhcpd#hosts#1#filename',
    'first reserved hostname'      => 'dhcpd#hosts#0#$key',
    'second reserved hostname'     => 'dhcpd#hosts#1#$key',
    'virtual network IPv4 address' => 'default_net#ipv4#gateway',
    'first IPv4 address for DHCP'  => 'default_net#ipv4#dhcp_start',
    'last IPv4 address for DHCP'   => 'default_net#ipv4#dhcp_end',
    'first option'                 => 'bind#config#options#0#0',
    'first value'                  => 'bind#config#options#0#1',
    'second option'                => 'bind#config#options#1#0',
    'second value'                 => 'bind#config#options#1#1',
    'third option'                 => 'bind#config#options#2#0',
    'third value'                  => 'bind#config#options#2#1',
    'first configured zone name'   => 'bind#configured_zones#0#$key',
    'first available zone name'    => 'bind#available_zones#0#$key',
    'second configured zone name'  => 'bind#configured_zones#1#$key',
    'second available zone name'   => 'bind#available_zones#1#$key',
    'third configured zone name'   => 'bind#configured_zones#2#$key',
    'third available zone name'    => 'bind#available_zones#2#$key',
    'TFTP base directory'          => 'tftpd#root_dir',
    'branch id'                    => 'pxe#branch_id',
    'disk id'                      => 'partitioning#0#$key',
    'disk device'                  => 'partitioning#0#device',
    'first partition id'           => 'partitioning#0#partitions#0#$key',
    'first partition size'         => 'partitioning#0#partitions#0#size_MiB',
    'first mount point'            => 'partitioning#0#partitions#0#mountpoint',
    'first OS image'               => 'partitioning#0#partitions#0#image',
    'first partition password'     => 'partitioning#0#partitions#0#luks_pass',
    'second partition id'          => 'partitioning#0#partitions#1#$key',
    'second partition size'        => 'partitioning#0#partitions#1#size_MiB',
    'second mount point'           => 'partitioning#0#partitions#1#mountpoint',
    'second OS image'              => 'partitioning#0#partitions#1#image',
    'second partition password'    => 'partitioning#0#partitions#1#luks_pass',
    'third partition id'           => 'partitioning#0#partitions#2#$key',
    'third partition size'         => 'partitioning#0#partitions#2#size_MiB',
    'third filesystem format'      => 'partitioning#0#partitions#2#format',
    'third mount point'            => 'partitioning#0#partitions#2#mountpoint',
    'third OS image'               => 'partitioning#0#partitions#2#image',
    'third partition password'     => 'partitioning#0#partitions#2#luks_pass',
    'FTP server directory'         => 'vsftpd_config#anon_root'
  }
  fill_in(fieldids[field], with: value, fill_options: { clear: :backspace })
end

When(/^I enter "([^"]*)" in (.*) field of (.*) zone$/) do |value, field, zone|
  fieldids = {
    'file name'                => '#file',
    'SOA name server'          => '#soa#ns',
    'SOA contact'              => '#soa#contact',
    'first A name'             => '#records#A#0#0',
    'first A address'          => '#records#A#0#1',
    'second A name'            => '#records#A#1#0',
    'second A address'         => '#records#A#1#1',
    'third A name'             => '#records#A#2#0',
    'third A address'          => '#records#A#2#1',
    'fourth A name'            => '#records#A#3#0',
    'fourth A address'         => '#records#A#3#1',
    'first NS'                 => '#records#NS#@#0',
    'first CNAME alias'        => '#records#CNAME#0#0',
    'first CNAME name'         => '#records#CNAME#0#1',
    'second CNAME alias'       => '#records#CNAME#1#0',
    'second CNAME name'        => '#records#CNAME#1#1',
    'third CNAME alias'        => '#records#CNAME#2#0',
    'third CNAME name'         => '#records#CNAME#2#1',
    'first for zones'          => '#generate_reverse#for_zones#0',
    'generate reverse network' => '#generate_reverse#net'
  }
  zone_xpath = "//input[@name='Name' and @value='#{zone}']/ancestor::div[starts-with(@id, 'bind#available_zones#')]"

  find(:xpath, "#{zone_xpath}//input[contains(@id, '#{fieldids[field]}')]").set(value)
end

When(/^I enter the local IP address of "([^"]*)" in (.*) field of (.*) zone$/) do |host, field, zone|
  step %(I enter "#{net_prefix + ADDRESSES[host]}" in #{field} field of #{zone} zone)
end

When(/^I enter the MAC address of "([^"]*)" in (.*) field$/) do |host, field|
  if host == 'pxeboot_minion'
    mac = $pxeboot_mac
  elsif host == 'sle12sp5_terminal'
    mac = $sle12sp5_terminal_mac
    mac = 'EE:EE:EE:00:00:05' if mac.nil?
  elsif host == 'sle15sp4_terminal'
    mac = $sle15sp4_terminal_mac
    mac = 'EE:EE:EE:00:00:06' if mac.nil?
  elsif (host.include? 'deblike') || (host.include? 'debian11') || (host.include? 'ubuntu')
    node = get_target(host)
    output, _code = node.run('ip link show dev ens4')
    mac = output.split("\n")[1].split[1]
  else
    node = get_target(host)
    output, _code = node.run('ip link show dev eth1')
    mac = output.split("\n")[1].split[1]
  end
  fill_in(FIELD_IDS[field], with: 'ethernet ' + mac, fill_options: { clear: :backspace })
end

When(/^I enter the local zone name in (.*) field$/) do |field|
  reverse_net = get_reverse_net($private_net)
  STDOUT.puts "#{$private_net} => #{reverse_net}"
  step %(I enter "#{reverse_net}" in #{field} field)
end

When(/^I enter the local file name in (.*) field of zone with local name$/) do |field|
  reverse_filename = 'master/db.' + get_reverse_net($private_net)
  STDOUT.puts "#{$private_net} => #{reverse_filename}"
  step %(I enter "#{reverse_filename}" in #{field} field of zone with local name)
end

When(/^I enter "([^"]*)" in (.*) field of zone with local name$/) do |value, field|
  reverse_net = get_reverse_net($private_net)
  step %(I enter "#{value}" in #{field} field of #{reverse_net} zone)
end

When(/^I enter the local network in (.*) field of zone with local name$/) do |field|
  step %(I enter "#{$private_net}" in #{field} field of zone with local name)
end

When(/^I enter the image name for "([^"]*)" in (.*) field$/) do |host, field|
  name = compute_kiwi_profile_name(host)
  fill_in(FIELD_IDS[field], with: name, fill_options: { clear: :backspace })
end

When(/^I press "Add Item" in (.*) section$/) do |section|
  sectionids = {
    'host reservations' => 'dhcpd#hosts#add_item',
    'config options'    => 'bind#config#options#add_item',
    'configured zones'  => 'bind#configured_zones#add_item',
    'available zones'   => 'bind#available_zones#add_item',
    'partitions'        => 'partitioning#0#partitions#add_item'
  }
  find(:xpath, "//i[@id='#{sectionids[section]}']").click
end

When(/^I press "Add Item" in (A|NS|CNAME|for zones) section of (.*) zone$/) do |field, zone|
  sectionids = {
    'for zones' => 'for_zones',
    'NS'       => 'NS#@',
    'CNAME'    => 'CNAME',
    'A'        => 'A'
  }
  xpath = "//input[@name='Name' and @value='#{zone}']/ancestor::div[starts-with(@id, 'bind#available_zones#')]//i[contains(@id, '##{sectionids[field]}#add_item')]"
  find(:xpath, xpath).click
end

When(/^I press "Add Item" in (A|NS|CNAME|for zones) section of zone with local name$/) do |field|
  reverse_net = get_reverse_net($private_net)
  step %(I press "Add Item" in #{field} section of #{reverse_net} zone)
end

When(/^I press "Remove Item" in (.*) CNAME of (.*) zone section$/) do |alias_name, zone|
  cname_xpath = "//input[@name='Name' and @value='#{zone}']/ancestor::div[starts-with(@id, 'bind#available_zones#')]//input[@name='Alias' and @value='#{alias_name}']/ancestor::div[@class='form-group']"
  find(:xpath, "#{cname_xpath}/button").click
end

When(/^I press "Remove" in the routers section$/) do
  cname_xpath = "//div[@id='dhcpd#subnets#0#routers#0']/button"
  find(:xpath, cname_xpath).click
end

When(/^I check (.*) box$/) do |checkbox_name|
  check BOX_IDS[checkbox_name]
end

When(/^I uncheck (.*) box$/) do |checkbox_name|
  uncheck BOX_IDS[checkbox_name]
end

# OS image build
When(/^I enter the image filename for "([^"]*)" relative to profiles as "([^"]*)"$/) do |host, field|
  git_profiles = ENV['GITPROFILES']
  path = compute_kiwi_profile_filename(host)
  step %(I enter "#{git_profiles}/#{path}" as "#{field}")
end

When(/^I wait until the image build "([^"]*)" is completed$/) do |image_name|
  step %(I wait at most 3300 seconds until event "Image Build #{image_name}" is completed)
end

When(/^I wait until the image inspection for "([^"]*)" is completed$/) do |host|
  # After build, the name and version are updated from Kiwi sources
  name = compute_kiwi_profile_name(host)
  version = compute_kiwi_profile_version(host)
  step %(I wait at most 300 seconds until event "Image Inspect 1//#{name}:#{version}" is completed)
end

When(/^I am on the image store of the Kiwi image for organization "([^"]*)"$/) do |org|
  # There is no navigation step to access this URL, so we must use a visit call (https://github.com/SUSE/spacewalk/issues/15256)
  visit("https://#{$server.full_hostname}/os-images/#{org}/")
end

Then(/^I should see the name of the image for "([^"]*)"$/) do |host|
  name = compute_kiwi_profile_name(host)
  step %(I should see a "#{name}" text)
end

Then(/^the image for "([^"]*)" should exist on the branch server$/) do |host|
  image = compute_kiwi_profile_name(host)
  images, _code = $proxy.run('ls /srv/saltboot/image/')
  raise "Image #{image} for #{host} does not exist" unless images.include? image
end
