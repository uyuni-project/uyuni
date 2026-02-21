# Copyright 2021-2025 SUSE LLC
# Licensed under the terms of the MIT license.

require 'net/http'

### This file contains the definitions for all steps concerning the different
### kinds of minions as well as PXE boot and Retail.

When(/^I (enable|disable) repositories (before|after) installing branch server$/) do |action, _when|
  os_version = get_target('proxy').os_version
  os_family = get_target('proxy').os_family

  # Distribution
  repos = 'os_pool_repo os_update_repo '
  repos += 'testing_overlay_devel_repo ' unless $build_validation || $is_containerized_server || product_version_full.include?('-released')

  # Server Applications, proxy product and modules, proxy devel
  if os_family.match?(/^sles/) && os_version.match?(/^15/)
    repos += 'proxy_module_pool_repo proxy_module_update_repo ' \
             'proxy_product_pool_repo proxy_product_update_repo ' \
             'module_server_applications_pool_repo module_server_applications_update_repo '
    repos += 'proxy_devel_releasenotes_repo proxy_devel_repo ' unless $build_validation || product_version_full.include?('-released')
  elsif os_family.match?(/^opensuse/)
    repos += 'proxy_pool_repo ' unless $is_containerized_server
  end

  get_target('proxy').run("zypper mr --#{action} #{repos}", verbose: true)
end

When(/^I start tftp on the proxy$/) do
  case product
    # TODO: Should we handle this in Sumaform?
  when 'Uyuni'
    step 'I enable repositories before installing branch server'
    cmd = 'zypper --non-interactive --ignore-unknown remove atftp && ' \
          'zypper --non-interactive install tftp && ' \
          'systemctl enable tftp.service && ' \
          'systemctl start tftp.service'
    get_target('proxy').run(cmd)
    step 'I disable repositories after installing branch server'
  else
    cmd = 'systemctl enable tftp.service && systemctl start tftp.service'
    get_target('proxy').run(cmd)
  end
end

Then(/^"([^"]*)" should communicate with the server using public interface$/) do |host|
  node = get_target(host)
  _result, return_code = node.run("ping -n -c 1 -I #{node.public_interface} #{get_target('server').public_ip}", check_errors: false)
  unless return_code.zero?
    sleep 2
    puts 're-try ping'
    node.run("ping -n -c 1 -I #{node.public_interface} #{get_target('server').public_ip}")
  end
  get_target('server').run("ping -n -c 1 #{node.public_ip}")
end

When(/^I rename the proxy for Retail$/) do
  node = get_target('proxy')
  node.run('sed -i "s/^proxy_fqdn:.*$/proxy_fqdn: proxy.example.org/" /etc/uyuni/proxy/config.yaml')
end

When(/^I connect the second interface of the proxy to the private network$/) do
  node = get_target('proxy')
  _result, return_code = node.run('which nmcli', check_errors: false)
  if return_code.zero?
    # Network manager: we give second interface precedence over first interface
    #                  otherwise the name server we get from DHCP is lost at the end of the list
    #                  (the name servers list in resolv.conf is limited to 3 entries)
    cmd = 'nmcli connection modify "Wired connection 1" ipv4.dns-priority 20 && ' \
          "nmcli device modify #{node.public_interface} ipv4.dns-priority 20 && " \
          'nmcli connection modify "Wired connection 2" ipv4.dns-priority 10 && ' \
          "nmcli device modify #{node.private_interface} ipv4.dns-priority 10"
  else
    # Wicked: is there a way to give eth1 precedence?
    #         we use a static setting for the name server instead
    static_dns = net_prefix + PRIVATE_ADDRESSES['dhcp_dns']
    cmd = 'echo -e "BOOTPROTO=dhcp\nSTARTMODE=auto\n" > /etc/sysconfig/network/ifcfg-eth1 && ' \
          'ifup eth1 && ' \
          "sed -i 's/^NETCONFIG_DNS_STATIC_SERVERS=\".*\"/NETCONFIG_DNS_STATIC_SERVERS=\"#{static_dns}\"/' /etc/sysconfig/network/config && " \
          'netconfig update -f'
  end
  node.run(cmd)
end

When(/^I restart all proxy containers$/) do
  node = get_target('proxy')
  node.run('systemctl restart uyuni-proxy-httpd.service')
  node.run('systemctl restart uyuni-proxy-salt-broker.service')
  node.run('systemctl restart uyuni-proxy-squid.service')
  node.run('systemctl restart uyuni-proxy-ssh.service')
  node.run('systemctl restart uyuni-proxy-tftpd.service')
end

Then(/^the "([^"]*)" host should be present on private network$/) do |host|
  node = get_target('proxy')
  output, return_code = node.run("ping -n -c 1 -I #{node.private_interface} #{net_prefix}#{PRIVATE_ADDRESSES[host]}")
  raise SystemCallError, "Terminal #{host} does not answer on eth1: #{output}" unless return_code.zero?
end

Then(/^name resolution should work on private network$/) do
  node = get_target('proxy')
  # direct name resolution
  %w[proxy.example.org dns.google.com].each do |dest|
    output, return_code = node.run("host #{dest}", check_errors: false)
    raise SystemCallError, "Direct name resolution of #{dest} on proxy doesn't work: #{output}" unless return_code.zero?

    log output.to_s
  end
  # reverse name resolution
  [node.private_ip, '8.8.8.8'].each do |dest|
    output, return_code = node.run("host #{dest}", check_errors: false)
    raise SystemCallError, "Reverse name resolution of #{dest} on proxy doesn't work: #{output}" unless return_code.zero?

    log output.to_s
  end
end

When(/^I reboot the (Retail|Cobbler) terminal "([^"]*)"$/) do |context, host|
  execute_expect_command_proxy(host, 'reboot-pxeboot.exp', context)
end

When(/^I reboot the (Retail|Cobbler) terminal "([^"]*)" through the interface "([^"]*)"$/) do |context, host, interface|
  # we might have no or any IPv4 address on that machine
  # convert MAC address to IPv6 link-local address
  case host
  when 'pxeboot_minion'
    mac = $pxeboot_mac
  when 'sle15sp6_terminal'
    mac = $sle15sp6_terminal_mac
  when 'sle15sp7_terminal'
    mac = $sle15sp7_terminal_mac
  end
  mac = mac.tr(':', '')
  hex = (("#{mac[0..5]}fffe#{mac[6..11]}").to_i(16) ^ 0x0200000000000000).to_s(16)
  ipv6 = "fe80::#{hex[0..3]}:#{hex[4..7]}:#{hex[8..11]}:#{hex[12..15]}%#{interface}"
  log "Rebooting #{ipv6}..."
  file = 'reboot-pxeboot.exp'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/tmp/#{file}"
  success = file_inject(get_target('proxy'), source, dest)
  raise ScriptError, 'File injection failed' unless success

  get_target('proxy').run("expect -f /tmp/#{file} #{ipv6} #{context}")
end

When(/^I create the bootstrap script for "([^"]+)" hostname and "([^"]*)" activation key on "([^"]*)"$/) do |hostname, key, host|
  node = get_target(host)
  # WORKAROUND: Revert once pxeboot autoinstallation contains venv-salt-minion
  # force_bundle = $use_salt_bundle ? '--force-bundle' : ''
  # get_target(host).run("mgr-bootstrap #{force_bundle}")
  node.run("mgr-bootstrap --hostname=#{hostname} --activation-keys=#{key}")

  output, _code = node.run('cat /srv/www/htdocs/pub/bootstrap/bootstrap.sh')
  raise ScriptError, "Key: #{key} not included" unless output.include? key
  raise ScriptError, "Hostname: #{hostname} not included" unless output.include? hostname
end

When(/^I bootstrap pxeboot minion via bootstrap script on the proxy$/) do
  execute_expect_command_proxy('pxeboot_minion', 'bootstrap-pxeboot.exp', 'Retail')
end

When(/^I accept key of pxeboot minion in the Salt master$/) do
  get_target('server').run('salt-key -y --accept=pxeboot.example.org')
end

When(/^I install the GPG key of the test packages repository on the PXE boot minion$/) do
  file = 'uyuni.key'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/tmp/#{file}"
  success = file_inject(get_target('server'), source, dest)
  raise ScriptError, 'File injection failed' unless success

  system_name = get_system_name('pxeboot_minion')
  get_target('server').run("salt-cp #{system_name} #{dest} #{dest}")
  get_target('server').run("salt #{system_name} cmd.run 'rpmkeys --import #{dest}'")
end

When(/^I wait until Salt client is inactive on the PXE boot minion$/) do
  execute_expect_command_proxy('pxeboot_minion', 'wait-end-of-cleanup-pxeboot.exp', 'Cleaning')
end

When(/^I prepare the retail configuration file on server$/) do
  source = "#{File.dirname(__FILE__)}/../upload_files/massive-import-terminals.yml"
  dest = '/tmp/massive-import-terminals.yml'
  success = file_inject(get_target('server'), source, dest)
  raise ScriptError, "File #{file} couldn't be copied to server" unless success

  sed_values = "s/<PROXY_HOSTNAME>/#{get_target('proxy').full_hostname}/; "
  sed_values << "s/<NET_PREFIX>/#{net_prefix}/; "
  sed_values << "s/<PROXY>/#{PRIVATE_ADDRESSES['proxy']}/; "
  sed_values << "s/<RANGE_BEGIN>/#{PRIVATE_ADDRESSES['range begin']}/; "
  sed_values << "s/<RANGE_END>/#{PRIVATE_ADDRESSES['range end']}/; "
  sed_values << "s/<PXEBOOT>/#{PRIVATE_ADDRESSES['pxeboot_minion']}/; "
  sed_values << "s/<PXEBOOT_MAC>/#{$pxeboot_mac}/; "
  sed_values << "s/<IMAGE>/#{compute_kiwi_profile_name('pxeboot_minion')}/"
  get_target('server').run("sed -i '#{sed_values}' #{dest}")
end

When(/^I import the retail configuration using retail_yaml command$/) do
  filepath = '/tmp/massive-import-terminals.yml'
  get_target('server').run("retail_yaml --api-user admin --api-pass admin --from-yaml #{filepath}")
end

# Click on the terminal
When(/^I follow "([^"]*)" terminal$/) do |host|
  domain = read_branch_prefix_from_yaml
  if host.include? 'pxeboot'
    step %(I follow "#{host}.#{domain}")
  else
    step %(I follow "#{domain}.#{host}")
  end
end

Then(/^I should see the terminals imported from the configuration file$/) do
  terminals = read_terminals_from_yaml
  terminals.each { |terminal| step %(I wait until I see the "#{terminal}" system, refreshing the page) }
end

Then(/^I should not see any terminals imported from the configuration file$/) do
  terminals = read_terminals_from_yaml
  domain = read_branch_prefix_from_yaml
  terminals.each do |terminal|
    next if (terminal.include? 'minion') || (terminal.include? 'client')

    # Construct full system name with domain prefix (e.g., "example.org.terminal1")
    full_system_name = terminal.include?('pxeboot') ? "#{terminal}.#{domain}" : "#{domain}.#{terminal}"
    step %(I wait at most 60 seconds until I do not see "#{full_system_name}" text, refreshing the page)
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
    'pxeboot next server'             => 'dhcpd#hosts#0#next_server',
    'first reserved IP'               => 'dhcpd#hosts#0#fixed_address',
    'second reserved IP'              => 'dhcpd#hosts#1#fixed_address',
    'internal network address'        => 'tftpd#listen_ip',
    'vsftpd internal network address' => 'vsftpd_config#listen_address'
  }
  fill_in(fieldids[field], with: net_prefix + PRIVATE_ADDRESSES[host], fill_options: { clear: :backspace })
end

When(/^I enter "([^"]*)" in (.*) field$/) do |value, field|
  fieldids = {
    'NIC'                          => 'branch_network#nic',
    'domain name'                  => 'dhcpd#domain_name',
    'listen interfaces'            => 'dhcpd#listen_interfaces#0',
    'network mask'                 => 'dhcpd#subnets#0#netmask',
    'filename'                     => 'dhcpd#subnets#0#filename',
    'pxeboot filename'             => 'dhcpd#hosts#0#filename',
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
  step %(I enter "#{net_prefix + PRIVATE_ADDRESSES[host]}" in #{field} field of #{zone} zone)
end

When(/^I enter the MAC address of "([^"]*)" in (.*) field$/) do |host, field|
  if host == 'pxeboot_minion'
    mac = $pxeboot_mac
  elsif host == 'sle15sp6_terminal'
    mac = $sle15sp6_terminal_mac
    mac = 'EE:EE:EE:00:00:06' if mac.nil?
  elsif host == 'sle15sp7_terminal'
    mac = $sle15sp7_terminal_mac
    mac = 'EE:EE:EE:00:00:07' if mac.nil?
  elsif (host.include? 'deblike') || (host.include? 'debian12') || (host.include? 'ubuntu')
    node = get_target(host)
    output, _code = node.run('ip link show dev ens4')
    mac = output.split("\n")[1].split[1]
  else
    node = get_target(host)
    output, _code = node.run('ip link show dev eth1')
    mac = output.split("\n")[1].split[1]
  end
  fill_in(FIELD_IDS[field], with: "ethernet #{mac}", fill_options: { clear: :backspace })
end

When(/^I enter the local zone name in (.*) field$/) do |field|
  reverse_net = get_reverse_net($private_net)
  $stdout.puts "#{$private_net} => #{reverse_net}"
  step %(I enter "#{reverse_net}" in #{field} field)
end

When(/^I enter the local file name in (.*) field of zone with local name$/) do |field|
  reverse_filename = "master/db.#{get_reverse_net($private_net)}"
  $stdout.puts "#{$private_net} => #{reverse_filename}"
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
    'config options' => 'bind#config#options#add_item',
    'configured zones' => 'bind#configured_zones#add_item',
    'available zones' => 'bind#available_zones#add_item',
    'partitions' => 'partitioning#0#partitions#add_item'
  }
  find(:xpath, "//i[@id='#{sectionids[section]}']").click
end

When(/^I press "Add Item" in (A|NS|CNAME|for zones) section of (.*) zone$/) do |field, zone|
  sectionids = {
    'for zones' => 'for_zones',
    'NS' => 'NS#@',
    'CNAME' => 'CNAME',
    'A' => 'A'
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
  cname_xpath = '//div[@id=\'dhcpd#subnets#0#routers#0\']/button'
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
  git_profiles = ENV.fetch('GITPROFILES', nil)
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

Then(/^I should see the image for "([^"]*)" is built$/) do |host|
  name = compute_kiwi_profile_name(host)

  begin
    tr = find('tr', text: name)
    tr.find('i[title="Built"]')
  rescue Capybara::ElementNotFound
    raise ScriptError, "Image #{name} is not present or not marked as built"
  end
end

When(/^I open the details page of the image for "([^"]*)"$/) do |host|
  name = compute_kiwi_profile_name(host)

  begin
    tr = find('tr', text: name)
    tr.find('button[aria-label="Details"]').click
  rescue Capybara::ElementNotFound
    raise ScriptError, "Can not open details page for image #{name}"
  end
end

Then(/^I should see a link to download the image for "([^"]*)"$/) do |host|
  name = compute_kiwi_profile_name(host)
  # Find the <a> whose href contains the image name and ends with .xz
  link = find("a[href*='#{name}'][href$='.xz']")
  img_url = link[:href]
  uri = URI.parse(img_url)
  # We use the HEAD method to verify the image is available, as it can be quite big for a GET
  Net::HTTP.start(uri.host, uri.port, use_ssl: uri.scheme == 'https') do |http|
    response = http.head(uri.request_uri)

    raise ScriptError, "Failed HEAD request for image #{name}" unless response.is_a?(Net::HTTPSuccess)
  end
end

Then(/^the image for "([^"]*)" should exist on the branch server$/) do |host|
  image = compute_kiwi_profile_name(host)
  images, _code = get_target('proxy').run('ls /srv/saltboot/image/')
  raise ScriptError, "Image #{image} for #{host} does not exist" unless images.include? image
end
