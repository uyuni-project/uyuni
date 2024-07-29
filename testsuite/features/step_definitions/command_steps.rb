# Copyright (c) 2014-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning the execution of commands on a system.

require 'timeout'
require 'nokogiri'
require 'pg'
require 'set'
require 'date'

# Sanity checks

Then(/^"([^"]*)" should have a FQDN$/) do |host|
  node = get_target(host)
  result, return_code = node.run('date +%s; hostname -f; date +%s', check_errors: false)
  lines = result.split("\n")
  initial_time = lines[0]
  result = lines[1]
  end_time = lines[2]
  resolution_time = end_time.to_i - initial_time.to_i
  raise 'cannot determine hostname' unless return_code.zero?
  raise "name resolution for #{node.full_hostname} took too long (#{resolution_time} seconds)" unless resolution_time <= 2
  raise 'hostname is not fully qualified' unless result == node.full_hostname
end

Then(/^reverse resolution should work for "([^"]*)"$/) do |host|
  node = get_target(host)
  result, return_code = node.run("date +%s; getent hosts #{node.full_hostname}; date +%s", check_errors: false)
  lines = result.split("\n")
  initial_time = lines[0]
  result = lines[1]
  end_time = lines[2]
  resolution_time = end_time.to_i - initial_time.to_i
  raise 'cannot do reverse resolution' unless return_code.zero?
  raise "reverse resolution for #{node.full_hostname} took too long (#{resolution_time} seconds)" unless resolution_time <= 2
  raise "reverse resolution for #{node.full_hostname} returned #{result}, expected to see #{node.full_hostname}" unless result.include? node.full_hostname
end

Then(/^I turn off disable_local_repos for all clients/) do
  get_target('server').run('echo "mgr_disable_local_repos: False" > /srv/pillar/disable_local_repos_off.sls')
  step 'I install a salt pillar top file for "salt_bundle_config, disable_local_repos_off" with target "*" on the server'
end

Then(/^the clock from "([^"]*)" should be exact$/) do |host|
  node = get_target(host)
  clock_node, _rc = node.run('date +\'%s\'')
  clock_controller = `date +'%s'`
  difference = clock_node.to_i - clock_controller.to_i
  raise "clocks differ by #{difference} seconds" unless difference.abs <= 2
end

Then(/^it should be possible to reach the test packages$/) do
  url = 'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Updates/rpm/x86_64/orion-dummy-1.1-1.1.x86_64.rpm'
  get_target('server').run("curl --insecure --location #{url} --output /dev/null")
end

Then(/^it should be possible to use the HTTP proxy$/) do
  url = 'https://www.suse.com'
  proxy = "suma2:P4$$wordWith%and&@#{$server_http_proxy}"
  get_target('server').run("curl --insecure --proxy '#{proxy}' --proxy-anyauth --location '#{url}' --output /dev/null")
end

Then(/^it should be possible to use the custom download endpoint$/) do
  url = "#{$custom_download_endpoint}/rhn/manager/download/fake-rpm-suse-channel/repodata/repomd.xml"
  get_target('server').run("curl --ipv4 --location #{url} --output /dev/null")
end

Then(/^it should be possible to reach the build sources$/) do
  if product == 'Uyuni'
    # TODO: move that internal resource to some other external location
    log 'Sanity check not implemented, move resource to external network first'
  else
    url = 'http://download.suse.de/ibs/SUSE/Products/SLE-SERVER/12-SP5/x86_64/product/media.1/products.key'
    get_target('server').run("curl --insecure --location #{url} --output /dev/null")
  end
end

Then(/^it should be possible to reach the Docker profiles$/) do
  git_profiles = ENV['GITPROFILES']
  url = git_profiles.sub(/github\.com/, 'raw.githubusercontent.com')
                    .sub(/\.git#:/, '/master/')
                    .sub(/$/, '/Docker/Dockerfile')
  get_target('server').run("curl --insecure --location #{url} --output /dev/null")
end

Then(/^it should be possible to reach the authenticated registry$/) do
  if not ($auth_registry.nil? || $auth_registry.empty?)
    url = "https://#{$auth_registry}"
    get_target('server').run("curl --insecure --location #{url} --output /dev/null")
  end
end

Then(/^it should be possible to reach the not authenticated registry$/) do
  if not ($no_auth_registry.nil? || $no_auth_registry.empty?)
    url = "https://#{$no_auth_registry}"
    get_target('server').run("curl --insecure --location #{url} --output /dev/null")
  end
end

# Channels

When(/^I delete these channels with spacewalk-remove-channel:$/) do |table|
  channels_cmd = 'spacewalk-remove-channel '
  table.raw.each { |x| channels_cmd = channels_cmd + ' -c ' + x[0] }
  $command_output, return_code = get_target('server').run(channels_cmd, check_errors: false)
end

When(/^I list channels with spacewalk-remove-channel$/) do
  $command_output, return_code = get_target('server').run('spacewalk-remove-channel -l')
  raise 'Unable to run spacewalk-remove-channel -l command on server' unless return_code.zero?
end

When(/^I add "([^"]*)" channel$/) do |channel|
  get_target('server').run("echo -e \"admin\nadmin\n\" | mgr-sync add channel #{channel}", buffer_size: 1_000_000)
end

When(/^I use spacewalk-channel to add "([^"]*)"$/) do |child_channel|
  command = "spacewalk-channel --add -c #{child_channel} -u admin -p admin"
  $command_output, _code = get_target('client').run(command)
end

Then(/^spacewalk-channel should fail adding "([^"]*)"$/) do |invalid_channel|
  command = "spacewalk-channel --add -c #{invalid_channel} -u admin -p admin"
  $command_output, code = get_target('client').run(command, check_errors: false)
  raise "#{command} should fail, but hasn't" if code.zero?
end

When(/^I use spacewalk-channel to remove "([^"]*)"$/) do |child_channel|
  command = "spacewalk-channel --remove -c #{child_channel} -u admin -p admin"
  $command_output, _code = get_target('client').run(command)
end

When(/^I use spacewalk-channel to list channels$/) do
  command = "spacewalk-channel --list"
  $command_output, _code = get_target('client').run(command)
end

When(/^I use spacewalk-channel to list available channels$/) do
  command = "spacewalk-channel --available-channels -u admin -p admin"
  $command_output, _code = get_target('client').run(command)
end

When(/^I use spacewalk-common-channel to add channel "([^"]*)" with arch "([^"]*)"$/) do |child_channel, arch|
  command = "spacewalk-common-channels -u admin -p admin -a #{arch} #{child_channel}"
  $command_output, _code = get_target('server').run(command)
end

When(/^I use spacewalk-common-channel to add all "([^"]*)" channels with arch "([^"]*)"$/) do |channel, architecture|
  channels_to_synchronize = CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION.dig(product, channel) ||
                            CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION.dig(product, "#{channel}-#{architecture}")
  channels_to_synchronize = filter_channels(channels_to_synchronize, ['beta']) unless $beta_enabled
  raise ScriptError, "Synchronization error, channel #{channel} or #{channel}-#{architecture} in #{product} product not found" if channels_to_synchronize.nil? || channels_to_synchronize.empty?

  channels_to_synchronize.each do |os_product_version_channel|
    command = "spacewalk-common-channels -u admin -p admin -a #{architecture} #{os_product_version_channel.gsub("-#{architecture}", '')}"
    _out, code = get_target('server').run(command, check_errors: false)
    if code.zero?
      log "Channel #{os_product_version_channel.gsub("-#{architecture}", '')} added"
    else
      command = "spacewalk-common-channels -u admin -p admin -a #{architecture} #{os_product_version_channel}"
      get_target('server').run(command)
      log "Channel #{os_product_version_channel} added"
    end
  end
end

When(/^I use spacewalk-repo-sync to sync channel "([^"]*)"$/) do |channel|
  $command_output, _code = get_target('server').run_until_ok("spacewalk-repo-sync -c #{channel}")
end

Then(/^I should get "([^"]*)"$/) do |value|
  raise "'#{value}' not found in output '#{$command_output}'" unless $command_output.include? value
end

Then(/^I shouldn't get "([^"]*)"$/) do |value|
  raise "'#{value}' found in output '#{$command_output}'" if $command_output.include? value
end

# Packages

Then(/^"([^"]*)" should be installed on "([^"]*)"$/) do |package, host|
  node = get_target(host)
  node.run("rpm -q #{package}")
end

Then(/^Deb package "([^"]*)" with version "([^"]*)" should be installed on "([^"]*)"$/) do |package, version, host|
  node = get_target(host)
  node.run("test $(dpkg-query -W -f='${Version}' #{package}) = \"#{version}\"")
end

Then(/^"([^"]*)" should not be installed on "([^"]*)"$/) do |package, host|
  node = get_target(host)
  node.run("rpm -q #{package}; test $? -ne 0")
end

When(/^I wait for "([^"]*)" to be (uninstalled|installed) on "([^"]*)"$/) do |package, status, host|
  if package.include?('suma') && product == 'Uyuni'
    package.gsub! 'suma', 'uyuni'
  end
  node = get_target(host)
  if deb_host?(host)
    pkg_version = package.split('-')[-1]
    pkg_name = package.delete_suffix("-#{pkg_version}")
    pkg_version_regexp = pkg_version.gsub('.', '\\.')
    if status == 'installed'
      node.run_until_ok("dpkg -l | grep -E '^ii +#{pkg_name} +#{pkg_version_regexp} +'")
    else
      node.run_until_fail("dpkg -l | grep -E '^ii +#{pkg_name} +#{pkg_version_regexp} +'")
    end
    node.wait_while_process_running('apt-get')
  else
    node.wait_while_process_running('zypper')
    if status == 'installed'
      node.run_until_ok("rpm -q #{package}")
    else
      node.run_until_fail("rpm -q #{package}")
    end
  end
end

When(/^I query latest Salt changes on "(.*?)"$/) do |host|
  node = get_target(host)
  salt = use_salt_bundle ? 'venv-salt-minion' : 'salt'
  if host == 'server'
    salt = 'salt'
  end
  result, return_code = node.run("LANG=en_US.UTF-8 rpm -q --changelog #{salt}")
  result.split("\n")[0, 15].each do |line|
    line.force_encoding('UTF-8')
    log line
  end
end

When(/^I query latest Salt changes on Debian-like system "(.*?)"$/) do |host|
  node = get_target(host)
  salt =
    if use_salt_bundle
      'venv-salt-minion'
    else
      'salt'
    end
  changelog_file = use_salt_bundle ? 'changelog.gz' : 'changelog.Debian.gz'
  result, return_code = node.run("zcat /usr/share/doc/#{salt}/#{changelog_file}")
  result.split("\n")[0, 15].each do |line|
    line.force_encoding('UTF-8')
    log line
  end
end

When(/^vendor change should be enabled for (?:[^"]*) on "([^"]*)"$/) do |host|
  node = get_target(host)
  pattern = '--allow-vendor-change'
  current_log = '/var/log/zypper.log'
  current_time = Time.now.localtime
  rotated_log = "#{current_log}-#{current_time.strftime('%Y%m%d')}.xz"
  day_after = (current_time.to_date + 1).strftime('%Y%m%d')
  next_day_rotated_log = "#{current_log}-#{day_after}.xz"
  begin
    _, return_code = node.run("xzdec #{next_day_rotated_log} | grep -- #{pattern}")
  rescue RuntimeError
    _, return_code = node.run("grep -- #{pattern} #{current_log} || xzdec #{rotated_log} | grep -- #{pattern}")
  end
  raise 'Vendor change option not found in logs' unless return_code.zero?
end

When(/^I wait until "([^"]*)" service is active on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  cmd = "systemctl is-active #{service}"
  node.run_until_ok(cmd)
end

When(/^I wait until "([^"]*)" service is inactive on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  cmd = "systemctl is-active #{service}"
  node.run_until_fail(cmd)
end

When(/^I wait until "([^"]*)" exporter service is active on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  # necessary since Debian-like OSes use different names for the services
  separator = deb_host?(host) ? '-' : '_'
  cmd = "systemctl is-active prometheus-#{service}#{separator}exporter"
  node.run_until_ok(cmd)
end

Then(/^I should see the "([^"]*)" paygo products$/) do |paygo_product|
  raise "Error : #{paygo_product} is an incorrect paygo product" unless %w[server sle15sp5_paygo_minion sle12sp5_paygo_minion sleforsap15sp5_paygo_minion].include?(paygo_product)
  command_output, _code = get_target('server').run('echo -e "admin\nadmin\n" | mgr-sync list products', check_errors: false, buffer_size: 1_000_000)
  output_lines = command_output.split("\n")
  raise "Error : Product list is empty" if output_lines.length.zero?
  missing_elements = PAYGO_DEFAULT_PRODUCTS[paygo_product] - output_lines
  raise "Error: Missing product(s) #{missing_elements}for the paygo product #{paygo_product}" unless missing_elements.empty?
end

When(/^I execute mgr-sync "([^"]*)" with user "([^"]*)" and password "([^"]*)"$/) do |arg1, u, p|
  get_target('server').run("echo -e \'mgrsync.user = \"#{u}\"\nmgrsync.password = \"#{p}\"\n\' > ~/.mgr-sync")
  $command_output, _code = get_target('server').run("echo -e '#{u}\n#{p}\n' | mgr-sync #{arg1}", check_errors: false, buffer_size: 1_000_000)
end

When(/^I execute mgr-sync "([^"]*)"$/) do |arg1|
  $command_output, _code = get_target('server').run("mgr-sync #{arg1}", buffer_size: 1_000_000)
end

When(/^I remove the mgr-sync cache file$/) do
  $command_output, _code = get_target('server').run('rm -f ~/.mgr-sync')
end

When(/^I refresh SCC$/) do
  refresh_timeout = 600
  get_target('server').run('echo -e "admin\nadmin\n" | mgr-sync refresh', timeout: refresh_timeout)
end

When(/^I execute mgr-sync refresh((?: with authentication)?)$/) do |authentication_required|
  command = authentication_required.empty? ? 'mgr-sync refresh' : 'echo -e "admin\nadmin\n | mgr-sync refresh'
  $command_output, _code = get_target('server').run(command, check_errors: false)
end

# This function kills spacewalk-repo-sync processes for a particular OS product version.
# It waits for all the reposyncs in the allow-list to complete, and kills all others.
When(/^I kill running spacewalk-repo-sync for "([^"]*)"$/) do |os_product_version|
  next if CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION.dig(product, os_product_version).nil?

  channels_to_kill = CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION[product][os_product_version]
  channels_to_kill = filter_channels(channels_to_kill, ['beta']) unless $beta_enabled
  log "Killing channels:\n#{channels_to_kill}"
  time_spent = 0
  checking_rate = 10
  repeat_until_timeout(timeout: 900, message: 'Some reposync processes were not killed properly', dont_raise: true) do
    # Kill a reposync process if it is running for a channel that is in the list to kill
    command_output, _code = get_target('server').run('ps axo pid,cmd | grep spacewalk-repo-sync | grep -v grep', check_errors: false)
    process = command_output.split("\n")[0]
    if process.nil?
      log "#{time_spent / 60.to_i} minutes waiting for '#{os_product_version}' remaining channels to start their repo-sync processes:\n#{channels_to_kill}" if ((time_spent += checking_rate) % 60).zero?
      sleep checking_rate
      next
    end
    channel = process.split(' ')[5].strip
    log "Repo-sync process for channel '#{channel}' running." if Time.now.sec % 5
    next unless CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION[product][os_product_version].include? channel
    channels_to_kill.delete(channel)
    pid = process.split(' ')[0]
    get_target('server').run("kill #{pid}", check_errors: false)
    log "Reposync of channel #{channel} killed"

    # Remove from the list to kill those channels that are already synced
    channels_to_kill.each do |remaining_channel|
      if channel_is_synced(remaining_channel)
        log "Channel '#{remaining_channel}' is already synced, so there is no need to kill repo-sync process."
        channels_to_kill.delete(remaining_channel)
      end
    end
    break if channels_to_kill.empty?
  end
end

Then(/^the reposync logs should not report errors$/) do
  result, code = get_target('server').run('grep -i "ERROR:" /var/log/rhn/reposync/*.log', check_errors: false)
  raise "Errors during reposync:\n#{result}" if code.zero?
end

Then(/^the "([^"]*)" reposync logs should not report errors$/) do |list|
  logfiles = list.split(',')
  logfiles.each do |logs|
    _result, code = get_target('server').run("test -f /var/log/rhn/reposync/#{logs}.log", check_errors: false)
    if code.zero?
      result, code = get_target('server').run("grep -i 'ERROR:' /var/log/rhn/reposync/#{logs}.log", check_errors: false)
      raise "Errors during #{logs} reposync:\n#{result}" if code.zero?
    end
  end
end

Then(/^"([^"]*)" package should have been stored$/) do |pkg|
  get_target('server').run("find /var/spacewalk/packages -name #{pkg}", verbose: true)
end

Then(/^solver file for "([^"]*)" should reference "([^"]*)"$/) do |channel, pkg|
  repeat_until_timeout(timeout: 600, message: "Reference #{pkg} not found in file.") do
    _result, code = get_target('server').run("dumpsolv /var/cache/rhn/repodata/#{channel}/solv | grep #{pkg}", verbose: false, check_errors: false)
    break if code.zero?
  end
end

When(/^I wait until the channel "([^"]*)" has been synced$/) do |channel|
  time_spent = 0
  checking_rate = 10
  if channel.include?('custom_channel') || channel.include?('ptf')
    log 'Timeout of 10 minutes for a custom channel'
    timeout = 600
  elsif TIMEOUT_BY_CHANNEL_NAME[channel].nil?
    log "Unknown timeout for channel #{channel}, assuming one minute"
    timeout = 60
  else
    timeout = TIMEOUT_BY_CHANNEL_NAME[channel]
  end
  begin
    repeat_until_timeout(timeout: timeout, message: 'Channel not fully synced') do
      break if channel_is_synced(channel)
      log "#{time_spent / 60.to_i} minutes out of #{timeout / 60.to_i} waiting for '#{channel}' channel to be synchronized" if ((time_spent += checking_rate) % 60).zero?
      sleep checking_rate
    end
  rescue StandardError => e
    log e.message
    unless $build_validation
      # It might be that the MU repository is wrong, but we want to continue in any case
      raise ScriptError, "This channel was not fully synced: #{channel}"
    end
  end
end

When(/^I wait until all synchronized channels for "([^"]*)" have finished$/) do |os_product_version|
  channels_to_wait = CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION.dig(product, os_product_version)
  channels_to_wait = filter_channels(channels_to_wait, ['beta']) unless $beta_enabled
  raise ScriptError, "Synchronization error, channels for #{os_product_version} in #{product} not found" if channels_to_wait.nil?

  time_spent = 0
  checking_rate = 10
  timeout = 0
  channels_to_wait.each do |channel|
    if TIMEOUT_BY_CHANNEL_NAME[channel].nil?
      log "Unknown timeout for channel #{channel}, assuming one minute"
      timeout += 60
    else
      timeout += TIMEOUT_BY_CHANNEL_NAME[channel]
    end
  end
  begin
    repeat_until_timeout(timeout: timeout, message: 'Product not fully synced') do
      channels_to_wait.each do |channel|
        if channel_is_synced(channel)
          channels_to_wait.delete(channel)
          log "Channel #{channel} finished syncing"
        end
      end
      break if channels_to_wait.empty?

      log "#{time_spent / 60.to_i} minutes out of #{timeout / 60.to_i} waiting for '#{os_product_version}' channels to be synchronized" if ((time_spent += checking_rate) % 60).zero?
      sleep checking_rate
    end
  rescue StandardError => e
    log e.message
    unless $build_validation
      # It might be that the MU repository is wrong, but we want to continue in any case
      raise ScriptError, "These channels were not fully synced:\n #{channels_to_wait}"
    end
  end
end

When(/^I execute mgr-bootstrap "([^"]*)"$/) do |arg1|
  $command_output, _code = get_target('server').run("mgr-bootstrap #{arg1}")
end

When(/^I fetch "([^"]*)" to "([^"]*)"$/) do |file, host|
  node = get_target(host)
  node.run("wget http://#{get_target('server').full_hostname}/#{file}")
end

When(/^I wait until file "([^"]*)" contains "([^"]*)" on server$/) do |file, content|
  repeat_until_timeout(message: "#{content} not found in file #{file}", report_result: true) do
    output, _code = get_target('server').run("grep #{content} #{file}", check_errors: false)
    break if output =~ /#{content}/
    sleep 2
    "\n-----\n#{output}\n-----\n"
  end
end

Then(/^I wait until "([^"]*)" is rejected on server$/) do |minion|
  file = '/var/log/rhn/rhn_web_ui.log'
  content = "Registration of '#{get_target(minion).full_hostname}' on #{product} Server rejected."
  repeat_until_timeout(message: "#{content} not found in file #{file}", report_result: true) do
    output, _code = get_target('server').run("grep \"#{content}\" #{file}", check_errors: false)
    break if output =~ /#{content}/
    sleep 2
    "\n-----\n#{output}\n-----\n"
  end
end

Then(/^file "([^"]*)" should contain "([^"]*)" on server$/) do |file, content|
  output, _code = get_target('server').run("grep -F '#{content}' #{file}", check_errors: false)
  raise "'#{content}' not found in file #{file}" if output !~ /#{content}/
  "\n-----\n#{output}\n-----\n"
end

Then(/^the tomcat logs should not contain errors$/) do
  output, _code = get_target('server').run('cat /var/log/tomcat/*')
  msgs = %w[ERROR NullPointer]
  msgs.each do |msg|
    raise "-#{msg}-  msg found on tomcat logs" if output.include? msg
  end
end

Then(/^the taskomatic logs should not contain errors$/) do
  output, _code = get_target('server').run('cat /var/log/rhn/rhn_taskomatic_daemon.log')
  msgs = %w[NullPointer]
  msgs.each do |msg|
    raise "-#{msg}-  msg found on taskomatic logs" if output.include? msg
  end
end

Then(/^the log messages should not contain out of memory errors$/) do
  output, code = get_target('server').run('grep -i "Out of memory: Killed process" /var/log/messages', check_errors: false)
  raise "Out of memory errors in /var/log/messages:\n#{output}" if code.zero?
end

When(/^I restart the spacewalk service$/) do
  get_target('server').run('spacewalk-service restart')
end

When(/^I shutdown the spacewalk service$/) do
  get_target('server').run('spacewalk-service stop')
end

When(/^I execute spacewalk-debug on the server$/) do
  get_target('server').run('spacewalk-debug')
  code = file_extract(get_target('server'), '/tmp/spacewalk-debug.tar.bz2', 'spacewalk-debug.tar.bz2')
  raise 'Download debug file failed' unless code.zero?
end

When(/^I extract the log files from all our active nodes$/) do
  $node_by_host.each do |_host, node|
    next if node.nil?

    extract_logs_from_node(node)
  end
end

Then(/^the susemanager repo file should exist on the "([^"]*)"$/) do |host|
  step %(file "/etc/zypp/repos.d/susemanager\:channels.repo" should exist on "#{host}")
end

Then(/^the repo file should contain the (custom|normal) download endpoint on the "([^"]*)"$/) do |type, target|
  node = get_target(target)
  base_url, _code = node.run('grep "baseurl" /etc/zypp/repos.d/susemanager\:channels.repo')
  base_url = base_url.strip.split('=')[1].delete '"'
  real_uri = URI.parse(base_url)
  log 'Real protocol: ' + real_uri.scheme + '  host: ' + real_uri.host + '  port: ' + real_uri.port.to_s
  normal_download_endpoint = "https://#{get_target('proxy').full_hostname}:443"
  expected_uri = URI.parse(type == 'custom' ? $custom_download_endpoint : normal_download_endpoint)
  log 'Expected protocol: ' + expected_uri.scheme + '  host: ' + expected_uri.host + '  port: ' + expected_uri.port.to_s
  raise 'Some parameters are not as expected' unless real_uri.scheme == expected_uri.scheme && real_uri.host == expected_uri.host && real_uri.port == expected_uri.port
end

When(/^I copy "([^"]*)" to "([^"]*)"$/) do |file, host|
  node = get_target(host)
  return_code = file_inject(node, file, File.basename(file))
  raise 'File injection failed' unless return_code.zero?
end

When(/^I copy "([^"]*)" file from "([^"]*)" to "([^"]*)"$/) do |file_path, from_host, to_host|
  from_node = get_target(from_host)
  to_node = get_target(to_host)
  return_code = file_extract(from_node, file_path, file_path)
  raise 'File extraction failed' unless return_code.zero?
  return_code = file_inject(to_node, file_path, file_path)
  raise 'File injection failed' unless return_code.zero?
end

Then(/^the PXE default profile should be enabled$/) do
  step 'I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT pxe-default-profile" on server'
end

Then(/^the PXE default profile should be disabled$/) do
  step 'I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT local" on server'
end

When(/^the server starts mocking an IPMI host$/) do
  ['ipmisim1.emu', 'lan.conf', 'fake_ipmi_host.sh'].each do |file|
    source = File.dirname(__FILE__) + '/../upload_files/' + file
    dest = '/etc/ipmi/' + file
    return_code = file_inject(get_target('server'), source, dest)
    raise 'File injection failed' unless return_code.zero?
  end
  get_target('server').run('chmod +x /etc/ipmi/fake_ipmi_host.sh')
  get_target('server').run('ipmi_sim -n < /dev/null > /dev/null &')
end

When(/^the server stops mocking an IPMI host$/) do
  get_target('server').run('pkill ipmi_sim')
  get_target('server').run('pkill fake_ipmi_host.sh || :')
end

When(/^the server starts mocking a Redfish host$/) do
  get_target('server').run('mkdir -p /root/Redfish-Mockup-Server/')
  %w[redfishMockupServer.py rfSsdpServer.py].each do |file|
    source = File.dirname(__FILE__) + '/../upload_files/Redfish-Mockup-Server/' + file
    dest = '/root/Redfish-Mockup-Server/' + file
    return_code = file_inject(get_target('server'), source, dest)
    raise 'File injection failed' unless return_code.zero?
  end
  get_target('server').run('curl --output DSP2043_2019.1.zip https://www.dmtf.org/sites/default/files/standards/documents/DSP2043_2019.1.zip')
  get_target('server').run('unzip DSP2043_2019.1.zip')
  cmd = "/usr/bin/python3 /root/Redfish-Mockup-Server/redfishMockupServer.py " \
        "-H #{get_target('server').full_hostname} -p 8443 " \
        "-S -D /root/DSP2043_2019.1/public-catfish/ " \
        "--ssl --cert /etc/pki/tls/certs/spacewalk.crt --key /etc/pki/tls/private/spacewalk.key " \
        "< /dev/null > /dev/null 2>&1 &"
  get_target('server').run(cmd)
end

When(/^the server stops mocking a Redfish host$/) do
  get_target('server').run('pkill -e -f /root/Redfish-Mockup-Server/redfishMockupServer.py')
end

When(/^I install a user-defined state for "([^"]*)" on the server$/) do |host|
  system_name = get_system_name(host)
  # copy state file to server
  file = 'user_defined_state.sls'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = '/srv/salt/' + file
  return_code = file_inject(get_target('server'), source, dest)
  raise 'File injection failed' unless return_code.zero?
  # generate top file and copy it to server
  script = "base:\n" \
           "  '#{system_name}':\n" \
           "    - user_defined_state\n"
  path = generate_temp_file('top.sls', script)
  return_code = file_inject(get_target('server'), path, '/srv/salt/top.sls')
  raise 'File injection failed' unless return_code.zero?
  `rm #{path}`
  # make both files readeable by salt
  get_target('server').run('chgrp salt /srv/salt/*')
end

When(/^I uninstall the user-defined state from the server$/) do
  get_target('server').run('rm /srv/salt/{user_defined_state.sls,top.sls}')
end

When(/^I uninstall the managed file from "([^"]*)"$/) do |host|
  node = get_target(host)
  node.run('rm /tmp/test_user_defined_state')
end

When(/^I configure tftp on the "([^"]*)"$/) do |host|
  raise "This step doesn't support #{host}" unless ['server', 'proxy'].include? host

  case host
  when 'server'
    get_target('server').run("/usr/sbin/configure-tftpsync.sh #{ENV['PROXY']}")
  when 'proxy'
    cmd = "/usr/sbin/configure-tftpsync.sh --non-interactive --tftpbootdir=/srv/tftpboot \
--server-fqdn=#{ENV['SERVER']} \
--proxy-fqdn='proxy.example.org'"
    get_target('proxy').run(cmd)
  else
    log "Host #{host} not supported"
  end
end

When(/^I set the default PXE menu entry to the (target profile|local boot) on the "([^"]*)"$/) do |entry, host|
  raise "This step doesn't support #{host}" unless ['server', 'proxy'].include? host

  node = get_target(host)
  target = '/srv/tftpboot/pxelinux.cfg/default'
  case entry
  when 'local boot'
    script = '-e "s/^TIMEOUT .*/TIMEOUT 2/" -e "s/ONTIMEOUT .*/ONTIMEOUT local/"'
  when 'target profile'
    script = '-e "s/^TIMEOUT .*/TIMEOUT 2/" -e "s/ONTIMEOUT .*/ONTIMEOUT 15-sp4-cobbler:1:SUSETest/"'
  else
    log "Entry #{entry} not supported"
  end
  node.run("sed -i #{script} #{target}")
end

When(/^I clean the search index on the server$/) do
  output, _code = get_target('server').run('/usr/sbin/rhn-search cleanindex', check_errors: false)
  log 'Search reindex finished.' if output.include?('Index files have been deleted and database has been cleaned up, ready to reindex')
  raise 'The output includes an error log' if output.include?('ERROR')
  step 'I wait until rhn-search is responding'
end

When(/^I wait until rhn-search is responding$/) do
  step 'I wait until "rhn-search" service is active on "server"'
  repeat_until_timeout(timeout: 60, message: 'rhn-search is not responding properly.') do
    begin
      log "Search by hostname: #{get_target('sle_minion').hostname}"
      result = $api_test.system.search.hostname(get_target('sle_minion').hostname)
      log result
      break if get_target('sle_minion').full_hostname.include? result.first['hostname']
    rescue StandardError => e
      log "rhn-search still not responding.\nError message: #{e.message}"
      sleep 3
    end
  end
end

When(/^I wait until mgr-sync refresh is finished$/) do
  # mgr-sync refresh is a slow operation, we don't use the default timeout
  # limit the result to important lines to have at least 1 match in the output we get
  cmd = 'spacecmd -u admin -p admin api sync.content.listProducts | grep SLES'
  repeat_until_timeout(timeout: 1800, message: '\'mgr-sync refresh\' did not finish') do
    result, code = get_target('server').run(cmd, check_errors: false)
    break if result.include? 'SLES'
    sleep 5
  end
end

Then(/^I should see "(.*?)" in the output$/) do |arg1|
  raise "Command Output #{@command_output} don't include #{arg1}" unless @command_output.include? arg1
end

When(/^I (start|stop|restart|reload|enable|disable) the "([^"]*)" service on "([^"]*)"$/) do |action, service, host|
  node = get_target(host)
  node.run("systemctl #{action} #{service}", check_errors: true, verbose: true)
end

Then(/^service "([^"]*)" is enabled on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  output, _code = node.run("systemctl is-enabled '#{service}'", check_errors: false)
  output = output.split(/\n+/)[-1]
  raise "Service #{service} not enabled" if output != 'enabled'
end

Then(/^service "([^"]*)" is active on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  output, _code = node.run("systemctl is-active '#{service}'", check_errors: false)
  output = output.split(/\n+/)[-1]
  raise "Service #{service} not active" if output != 'active'
end

Then(/^socket "([^"]*)" is enabled on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  output, _code = node.run("systemctl is-enabled '#{service}.socket'", check_errors: false)
  output = output.split(/\n+/)[-1]
  raise "Service #{service} not enabled" if output != 'enabled'
end

Then(/^socket "([^"]*)" is active on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  output, _code = node.run("systemctl is-active '#{service}.socket'", check_errors: false)
  output = output.split(/\n+/)[-1]
  raise "Service #{service} not active" if output != 'active'
end

When(/^I run "([^"]*)" on "([^"]*)"$/) do |cmd, host|
  node = get_target(host)
  node.run(cmd)
end

When(/^I force picking pending events on "([^"]*)" if necessary$/) do |host|
  if get_client_type(host) == 'traditional'
    node = get_target(host)
    node.run('rhn_check -vvv')
  end
end

When(/^I run "([^"]*)" on "([^"]*)" with logging$/) do |cmd, host|
  node = get_target(host)
  output, _code = node.run(cmd)
  log "OUT: #{output}"
end

When(/^I run "([^"]*)" on "([^"]*)" without error control$/) do |cmd, host|
  node = get_target(host)
  _out, $fail_code = node.run(cmd, check_errors: false)
end

Then(/^the command should fail$/) do
  raise 'Previous command must fail, but has NOT failed!' if $fail_code.zero?
end

When(/^I wait until file "([^"]*)" exists on "([^"]*)"$/) do |file, host|
  step %(I wait at most #{DEFAULT_TIMEOUT} seconds until file "#{file}" exists on "#{host}")
end

When(/^I wait at most (\d+) seconds until file "([^"]*)" exists on "([^"]*)"$/) do |seconds, file, host|
  node = get_target(host)
  repeat_until_timeout(timeout: seconds.to_i) do
    break if file_exists?(node, file)
    sleep(1)
  end
end

When(/^I wait until file "(.*)" exists on server$/) do |file|
  repeat_until_timeout do
    break if file_exists?(get_target('server'), file)
    sleep(1)
  end
end

Then(/^I wait and check that "([^"]*)" has rebooted$/) do |host|
  reboot_timeout = 800
  system_name = get_system_name(host)
  check_shutdown(system_name, reboot_timeout)
  check_restart(system_name, get_target(host), reboot_timeout)
end

When(/^I call spacewalk-repo-sync for channel "(.*?)" with a custom url "(.*?)"$/) do |arg1, arg2|
  # Notice that we are calling it until ok because we might collide with other running process of spacewalk-repo-sync,
  # and it can only run one process at a time.
  @command_output, _code = get_target('server').run_until_ok("spacewalk-repo-sync -c #{arg1} -u #{arg2}")
end

When(/^I call spacewalk-repo-sync to sync the channel "(.*?)"$/) do |channel|
  @command_output, _code = get_target('server').run_until_ok("spacewalk-repo-sync -c #{channel}")
end

When(/^I call spacewalk-repo-sync to sync the parent channel "(.*?)"$/) do |channel|
  @command_output, _code = get_target('server').run_until_ok("spacewalk-repo-sync -p #{channel}")
end

When(/^I get "(.*?)" file details for channel "(.*?)" via spacecmd$/) do |arg1, arg2|
  @command_output, _code = get_target('server').run("spacecmd -u admin -p admin -q -- configchannel_filedetails #{arg2} '#{arg1}'", check_errors: false)
end

When(/^I register this client for SSH push via tunnel$/) do
  # create backups of /etc/hosts and up2date config
  get_target('server').run('cp /etc/hosts /etc/hosts.BACKUP')
  get_target('server').run('cp /etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP')
  # generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  script = "spawn spacewalk-ssh-push-init --client #{get_target('client').full_hostname} --register #{bootstrap} --tunnel\n" \
           "while {1} {\n" \
           "  expect {\n" \
           "    eof                                                        {break}\n" \
	   "    -re \"Are you sure you want to continue connecting.*\" {send \"yes\r\"}\n" \
           "    \"Password:\"                                              {send \"linux\r\"}\n" \
           "  }\n" \
           "}\n"
  path = generate_temp_file('push-registration.exp', script)
  step 'I copy "' + path + '" to "server"'
  `rm #{path}`
  # perform the registration
  filename = File.basename(path)
  bootstrap_timeout = 600
  get_target('server').run("expect #{filename}", timeout: bootstrap_timeout, verbose: true)
  # restore files from backups
  get_target('server').run('mv /etc/hosts.BACKUP /etc/hosts')
  get_target('server').run('mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date')
end

# Repositories and packages management
When(/^I migrate the non-SUMA repositories on "([^"]*)"$/) do |host|
  node = get_target(host)
  salt_call = use_salt_bundle ? 'venv-salt-call' : 'salt-call'
  # use sumaform states to migrate to latest SP the system repositories:
  node.run("#{salt_call} --local --file-root /root/salt/ state.apply repos")
  # disable again the non-SUMA repositories:
  node.run('for repo in $(zypper lr | awk \'NR>7 && !/susemanager:/ {print $3}\'); do zypper mr -d $repo; done')
  # node.run('salt-call state.apply channels.disablelocalrepos') does not work
end

When(/^I (enable|disable) Debian-like "([^"]*)" repository on "([^"]*)"$/) do |action, repo, host|
  node = get_target(host)
  raise "#{node.hostname} is not a Debian-like host." unless deb_host?(host)

  source_repo = "deb http://archive.ubuntu.com/ubuntu/ $(lsb_release -sc) #{repo}"
  node.run("sudo add-apt-repository -y -u #{action == 'disable' ? '--remove' : ''} \"#{source_repo}\"")
end

When(/^I (enable|disable) (the repositories|repository) "([^"]*)" on this "([^"]*)"((?: without error control)?)$/) do |action, _optional, repos, host, error_control|
  node = get_target(host)
  os_family = node.os_family
  cmd = ''

  case os_family
  when /^opensuse/, /^sles/, /^suse/
    mand_repos = ''
    repos.split.map do |repo|
      mand_repos = "#{mand_repos} #{repo}"
    end
    cmd = "zypper mr --#{action} #{mand_repos}" unless mand_repos.empty?
  when /^centos/, /^rocky/
    repos.split.each do |repo|
      cmd = "#{cmd} && " unless cmd.empty?
      cmd =
        if action == 'enable'
          "#{cmd}sed -i 's/enabled=.*/enabled=1/g' /etc/yum.repos.d/#{repo}.repo"
        else
          "#{cmd}sed -i 's/enabled=.*/enabled=0/g' /etc/yum.repos.d/#{repo}.repo"
        end
    end
  when /^ubuntu/, /^debian/
    repos.split.each do |repo|
      cmd = "#{cmd} && " unless cmd.empty?
      cmd =
        if action == 'enable'
          "#{cmd}sed -i '/^#\\s*deb.*/ s/^#\\s*deb /deb /' /etc/apt/sources.list.d/#{repo}.list"
        else
          "#{cmd}sed -i '/^deb.*/ s/^deb /# deb /' /etc/apt/sources.list.d/#{repo}.list"
        end
    end
  end
  node.run(cmd, verbose: true, check_errors: error_control.empty?)
end

When(/^I enable source package syncing$/) do
  cmd = 'echo \'server.sync_source_packages = 1\' >> /etc/rhn/rhn.conf'
  get_target('server').run(cmd)
end

When(/^I disable source package syncing$/) do
  cmd = 'sed -i \'s/^server.sync_source_packages = 1.*//g\' /etc/rhn/rhn.conf'
  get_target('server').run(cmd)
end

When(/^I install pattern "([^"]*)" on this "([^"]*)"$/) do |pattern, host|
  if pattern.include?('suma') && product == 'Uyuni'
    pattern.gsub! 'suma', 'uyuni'
  end
  node = get_target(host)
  node.run('zypper ref')
  cmd = "zypper --non-interactive install -t pattern #{pattern}"
  node.run(cmd, successcodes: [0, 100, 101, 102, 103, 106])
end

When(/^I (install|remove) the traditional stack utils (on|from) "([^"]*)"$/) do |action, where, host|
  pkgs = 'spacewalk-client-tools spacewalk-check spacewalk-client-setup mgr-daemon mgr-osad mgr-cfg-actions'
  step %(I #{action} packages "#{pkgs}" #{where} this "#{host}")
end

When(/^I (install|remove) OpenSCAP dependencies (on|from) "([^"]*)"$/) do |action, where, host|
  node = get_target(host)
  os_family = node.os_family
  case os_family
  when /^opensuse/, /^sles/
    pkgs = 'openscap-utils openscap-content scap-security-guide'
  when /^centos/, /^rocky/
    pkgs = 'openscap-utils scap-security-guide-redhat'
  when /^ubuntu/
    pkgs = 'libopenscap8 scap-security-guide-ubuntu'
  else
    raise ScriptError, "The node #{node.hostname} has not a supported OS Family (#{os_family})"
  end
  pkgs += ' spacewalk-oscap' if host.include? 'client'
  step %(I #{action} packages "#{pkgs}" #{where} this "#{host}")
end

When(/^I install package(?:s)? "([^"]*)" on this "([^"]*)"((?: without error control)?)$/) do |package, host, error_control|
  node = get_target(host)
  if rh_host?(host)
    cmd = "yum -y install #{package}"
    successcodes = [0]
    not_found_msg = 'No package'
  elsif deb_host?(host)
    cmd = "apt-get --assume-yes install #{package}"
    successcodes = [0]
    not_found_msg = 'Unable to locate package'
  elsif slemicro_host?(host)
    cmd = "transactional-update pkg install -y #{package}"
    successcodes = [0, 100, 101, 102, 103, 106]
    not_found_msg = 'not found in package names'
  else
    cmd = "zypper --non-interactive install -y #{package}"
    successcodes = [0, 100, 101, 102, 103, 106]
    not_found_msg = 'not found in package names'
  end
  output, _code = node.run(cmd, check_errors: error_control.empty?, successcodes: successcodes)
  raise "A package was not found. Output:\n #{output}" if output.include? not_found_msg
end

When(/^I install old package(?:s)? "([^"]*)" on this "([^"]*)"((?: without error control)?)$/) do |package, host, error_control|
  node = get_target(host)
  if rh_host?(host)
    cmd = "yum -y downgrade #{package}"
    successcodes = [0]
    not_found_msg = 'No package'
  elsif deb_host?(host)
    cmd = "apt-get --assume-yes install #{package} --allow-downgrades"
    successcodes = [0]
    not_found_msg = 'Unable to locate package'
  else
    cmd = "zypper --non-interactive install --oldpackage -y #{package}"
    successcodes = [0, 100, 101, 102, 103, 106]
    not_found_msg = 'not found in package names'
  end
  output, _code = node.run(cmd, check_errors: error_control.empty?, successcodes: successcodes)
  raise "A package was not found. Output:\n #{output}" if output.include? not_found_msg
end

When(/^I remove package(?:s)? "([^"]*)" from this "([^"]*)"((?: without error control)?)$/) do |package, host, error_control|
  node = get_target(host)
  if rh_host?(host)
    cmd = "yum -y remove #{package}"
    successcodes = [0]
  elsif deb_host?(host)
    cmd = "dpkg --remove #{package}"
    successcodes = [0]
  elsif slemicro_host?(host)
    cmd = "transactional-update pkg rm -y #{package}"
    successcodes = [0, 100, 101, 102, 103, 106]
  else
    cmd = "zypper --non-interactive remove -y #{package}"
    successcodes = [0, 100, 101, 102, 103, 104, 106]
  end
  node.run(cmd, check_errors: error_control.empty?, successcodes: successcodes)
end

When(/^I install package tftpboot-installation on the server$/) do
  server = get_target('server')

  # set this variable to true when the server and the build host run the same operating system version
  # examples:
  # * the server is either SLES 15 SP5 or Leap 15.5, and the build host is SLES 15 SP4:
  #   same_version_on_server_and_build_host = false
  # * the server is either SLES 15 SP4 or Leap 15.4, and the build host is SLES 15 SP4:
  #   same_version_on_server_and_build_host = product != 'Uyuni'
  same_version_on_server_and_build_host = product != 'Uyuni'

  # set this variable to the name of the desired tftpboot package
  # beware that "-x86_64" is part of the package name, the package itself is noarch!
  tftpboot_package = 'tftpboot-installation-SLE-15-SP4-x86_64'

  # if same version, fetch the package directly, otherwise search it among the reposynced packages
  if same_version_on_server_and_build_host
    server.run("zypper --non-interactive install #{tftpboot_package}", verbose: true)
  else
    output, _code = server.run("find /var/spacewalk/packages -name #{tftpboot_package}-*.noarch.rpm")
    packages = output.split("\n")
    pattern = '/tftpboot-installation-([^/]+)*.noarch.rpm'
    # Reverse sort the package names to get the latest version first
    latest_version = packages.min { |a, b| b.match(pattern)[0] <=> a.match(pattern)[0] }
    server.run("rpm -q #{tftpboot_package} || rpm -i #{latest_version}", verbose: true)
  end
end

When(/^I reset tftp defaults on the proxy$/) do
  get_target('proxy').run("echo 'TFTP_USER=\"tftp\"\nTFTP_OPTIONS=\"\"\nTFTP_DIRECTORY=\"/srv/tftpboot\"\n' > /etc/sysconfig/tftp")
end

When(/^I wait until the package "(.*?)" has been cached on this "(.*?)"$/) do |pkg_name, host|
  node = get_target(host)
  if suse_host?(host)
    cmd = "ls /var/cache/zypp/packages/susemanager:fake-rpm-suse-channel/getPackage/*/*/#{pkg_name}*.rpm"
  elsif deb_host?(host)
    cmd = "ls /var/cache/apt/archives/#{pkg_name}*.deb"
  end
  repeat_until_timeout(message: "Package #{pkg_name} was not cached") do
    result, return_code = node.run(cmd, check_errors: false)
    break if return_code.zero?
  end
end

When(/^I create the bootstrap repository for "([^"]*)" on the server((?: without flushing)?)$/) do |host, without_flushing|
  base_channel = BASE_CHANNEL_BY_CLIENT[product][host]
  channel = CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL[product][base_channel]
  parent_channel = PARENT_CHANNEL_LABEL_TO_SYNC_BY_BASE_CHANNEL[product][base_channel]
  get_target('server').wait_while_process_running('mgr-create-bootstrap-repo')

  cmd =
    if parent_channel.nil?
      "mgr-create-bootstrap-repo --create #{channel} --with-custom-channels"
    else
      "mgr-create-bootstrap-repo --create #{channel} --with-parent-channel #{parent_channel} --with-custom-channels"
    end

  cmd += ' --flush' unless without_flushing

  log 'Creating the bootstrap repository on the server:'
  log "  #{cmd}"
  get_target('server').run(cmd)
end

When(/^I install "([^"]*)" product on the proxy$/) do |product|
  out, = get_target('proxy').run("zypper ref && zypper --non-interactive install --auto-agree-with-licenses --force-resolution -t product #{product}")
  log "Installed #{product} product: #{out}"
end

When(/^I install proxy pattern on the proxy$/) do
  pattern = product == 'Uyuni' ? 'uyuni_proxy' : 'suma_proxy'
  cmd = "zypper --non-interactive install -t pattern #{pattern}"
  get_target('proxy').run(cmd, timeout: 600, successcodes: [0, 100, 101, 102, 103, 106])
end

When(/^I let squid use avahi on the proxy$/) do
  file = '/usr/share/rhn/proxy-template/squid.conf'
  key = 'dns_multicast_local'
  val = 'on'
  get_target('proxy').run("grep '^#{key}' #{file} && sed -i -e 's/^#{key}.*$/#{key} #{val}/' #{file} || echo '#{key} #{val}' >> #{file}")
  key = 'ignore_unknown_nameservers'
  val = 'off'
  get_target('proxy').run("grep '^#{key}' #{file} && sed -i -e 's/^#{key}.*$/#{key} #{val}/' #{file} || echo '#{key} #{val}' >> #{file}")
end

When(/^I open avahi port on the proxy$/) do
  get_target('proxy').run('firewall-offline-cmd --zone=public --add-service=mdns')
end

When(/^I copy server\'s keys to the proxy$/) do
  %w[RHN-ORG-PRIVATE-SSL-KEY RHN-ORG-TRUSTED-SSL-CERT rhn-ca-openssl.cnf].each do |file|
    return_code = file_extract(get_target('server'), '/root/ssl-build/' + file, '/tmp/' + file)
    raise 'File extraction failed' unless return_code.zero?
    get_target('proxy').run('mkdir -p /root/ssl-build')
    return_code = file_inject(get_target('proxy'), '/tmp/' + file, '/root/ssl-build/' + file)
    raise 'File injection failed' unless return_code.zero?
  end
end

When(/^I configure the proxy$/) do
  # prepare the settings file
  settings = "RHN_PARENT=#{get_target('server').full_hostname}\n" \
             "HTTP_PROXY=''\n" \
             "VERSION=''\n" \
             "TRACEBACK_EMAIL=galaxy-noise@suse.de\n" \
             "USE_EXISTING_CERTS=n\n" \
             "INSTALL_MONITORING=n\n" \
             "SSL_PASSWORD=spacewalk\n" \
             "SSL_ORG=SUSE\n" \
             "SSL_ORGUNIT=SUSE\n" \
             "SSL_COMMON=#{get_target('proxy').full_hostname}\n" \
             "SSL_CITY=Nuremberg\n" \
             "SSL_STATE=Bayern\n" \
             "SSL_COUNTRY=DE\n" \
             "SSL_EMAIL=galaxy-noise@suse.de\n" \
             "SSL_CNAME_ASK=proxy.example.org\n" \
             "POPULATE_CONFIG_CHANNEL=y\n" \
             "RHN_USER=admin\n" \
             "ACTIVATE_SLP=y\n"
  path = generate_temp_file('config-answers.txt', settings)
  step 'I copy "' + path + '" to "proxy"'
  `rm #{path}`
  # perform the configuration
  filename = File.basename(path)
  cmd = "configure-proxy.sh --non-interactive --rhn-user=admin --rhn-password=admin --answer-file=#{filename}"
  proxy_timeout = 600
  get_target('proxy').run(cmd, timeout: proxy_timeout, verbose: true)
end

When(/^I allow all SSL protocols on the proxy's apache$/) do
  file = '/etc/apache2/ssl-global.conf'
  key = 'SSLProtocol'
  val = 'all -SSLv2 -SSLv3'
  get_target('proxy').run("grep '#{key}' #{file} && sed -i -e 's/#{key}.*$/#{key} #{val}/' #{file}")
  get_target('proxy').run('systemctl reload apache2.service', verbose: true)
end

When(/^I restart squid service on the proxy$/) do
  # We need to restart squid when we add a CNAME to the certificate
  get_target('proxy').run('systemctl restart squid.service')
end

Then(/^The metadata buildtime from package "(.*?)" match the one in the rpm on "(.*?)"$/) do |pkg, host|
  # for testing buildtime of generated metadata - See bsc#1078056
  node = get_target(host)
  cmd = "dumpsolv /var/cache/zypp/solv/spacewalk:fake-rpm-suse-channel/solv | grep -E 'solvable:name|solvable:buildtime'| grep -A1 '#{pkg}$'| perl -ne 'if($_ =~ /^solvable:buildtime:\\s*(\\d+)/) { print $1; }'"
  metadata_buildtime, return_code = node.run(cmd)
  raise "Command failed: #{cmd}" unless return_code.zero?
  cmd = "rpm -q --qf '%{BUILDTIME}' #{pkg}"
  rpm_buildtime, return_code = node.run(cmd)
  raise "Command failed: #{cmd}" unless return_code.zero?
  raise "Wrong buildtime in metadata: #{metadata_buildtime} != #{rpm_buildtime}" unless metadata_buildtime == rpm_buildtime
end

When(/^I create channel "([^"]*)" from spacecmd of type "([^"]*)"$/) do |name, type|
  command = "spacecmd -u admin -p admin -- configchannel_create -n #{name} -t  #{type}"
  get_target('server').run(command)
end

When(/^I update init.sls from spacecmd with content "([^"]*)" for channel "([^"]*)"$/) do |content, label|
  filepath = "/tmp/#{label}"
  get_target('server').run("echo -e \"#{content}\" > #{filepath}", timeout: 600)
  command = "spacecmd -u admin -p admin -- configchannel_updateinitsls -c #{label} -f  #{filepath} -y"
  get_target('server').run(command)
  file_delete(get_target('server'), filepath)
end

When(/^I update init.sls from spacecmd with content "([^"]*)" for channel "([^"]*)" and revision "([^"]*)"$/) do |content, label, revision|
  filepath = "/tmp/#{label}"
  get_target('server').run("echo -e \"#{content}\" > #{filepath}", timeout: 600)
  command = "spacecmd -u admin -p admin -- configchannel_updateinitsls -c #{label} -f #{filepath} -r #{revision} -y"
  get_target('server').run(command)
  file_delete(get_target('server'), filepath)
end

When(/^I schedule apply configchannels for "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  command = "spacecmd -y -u admin -p admin -- system_scheduleapplyconfigchannels  #{system_name}"
  get_target('server').run(command)
end

When(/^I create "([^"]*)" virtual machine on "([^"]*)"$/) do |vm_name, host|
  node = get_target(host)
  disk_path = "/tmp/#{vm_name}_disk.qcow2"

  # Create the throwable overlay image
  raise '/var/testsuite-data/leap-disk-image-template.qcow2 not found' unless file_exists?(node, '/var/testsuite-data/leap-disk-image-template.qcow2')
  node.run("cp /var/testsuite-data/leap-disk-image-template.qcow2 #{disk_path}")

  # Actually define the VM, but don't start it
  raise 'not found: virt-install' unless file_exists?(node, '/usr/bin/virt-install')
  # Use 'ide' bus for Xen and 'virtio' bus for KVM
  bus_type = 'virtio'
  node.run(
    "virt-install --name #{vm_name} --memory 512 --vcpus 1 --disk path=#{disk_path},bus=#{bus_type} "\
    "--network network=test-net0 --graphics vnc,listen=0.0.0.0 "\
    "--serial file,path=/tmp/#{vm_name}.console.log "\
    "--import --hvm --noautoconsole --noreboot --osinfo sle15sp4"
  )
end

When(/^I create ([^ ]*) virtual network on "([^"]*)"$/) do |net_name, host|
  node = get_target(host)

  networks = {
    "test-net0" => { "bridge" => "virbr0", "subnet" => 124 },
    "test-net1" => { "bridge" => "virbr1", "subnet" => 126 }
  }

  net = networks[net_name]
  netdef = "<network>" \
           "  <name>#{net_name}</name>"\
           "  <forward mode='nat'/>"\
           "  <bridge name='#{net['bridge']}' stp='on' delay='0'/>"\
           "  <ip address='192.168.#{net['subnet']}.1' netmask='255.255.255.0'>"\
           "    <dhcp>"\
           "      <range start='192.168.#{net['subnet']}.2' end='192.168.#{net['subnet']}.254'/>"\
           "    </dhcp>"\
           "  </ip>"\
           "</network>"

  # Some networks like the default one may already be defined.
  _output, code = node.run("virsh net-dumpxml #{net_name}", check_errors: false)
  node.run("echo -e \"#{netdef}\" >/tmp/#{net_name}.xml && virsh net-define /tmp/#{net_name}.xml") unless code.zero?

  # Ensure the network is started
  node.run("virsh net-start #{net_name}", check_errors: false)
end

When(/^I delete ([^ ]*) virtual network on "([^"]*)"((?: without error control)?)$/) do |net_name, host, error_control|
  node = get_target(host)
  _output, code = node.run("virsh net-dumpxml #{net_name}", check_errors: false)
  if code.zero?
    steps %(
      When I run "virsh net-destroy #{net_name}" on "#{host}"#{error_control}
      And I run "virsh net-undefine #{net_name}" on "#{host}"#{error_control}
    )
  end
end

When(/^I create ([^ ]*) virtual storage pool on "([^"]*)"$/) do |pool_name, host|
  node = get_target(host)

  pool_def = %(<pool type='dir'>
      <name>#{pool_name}</name>
      <capacity unit='bytes'>0</capacity>
      <allocation unit='bytes'>0</allocation>
      <available unit='bytes'>0</available>
      <source>
      </source>
      <target>
        <path>/var/lib/libvirt/images/#{pool_name}</path>
      </target>
    </pool>
  )

  # Some pools like the default one may already be defined.
  _output, code = node.run("virsh pool-dumpxml #{pool_name}", check_errors: false)
  node.run("echo -e \"#{pool_def}\" >/tmp/#{pool_name}.xml && virsh pool-define /tmp/#{pool_name}.xml") unless code.zero?
  node.run("mkdir -p /var/lib/libvirt/images/#{pool_name}")

  # Ensure the pool is started
  node.run("virsh pool-start #{pool_name}", check_errors: false)
end

When(/^I delete ([^ ]*) virtual storage pool on "([^"]*)"((?: without error control)?)$/) do |pool_name, host, error_control|
  node = get_target(host)
  _output, code = node.run("virsh pool-dumpxml #{pool_name}", check_errors: false)
  if code.zero?
    steps %(
      When I run "virsh pool-destroy #{pool_name}" on "#{host}"#{error_control}
      And I run "virsh pool-undefine #{pool_name}" on "#{host}"#{error_control}
    )
  end

  # only delete the folders we created
  step %(I run "rm -rf /var/lib/libvirt/images/#{pool_name}" on "#{host}"#{error_control}) if pool_name.start_with? "test-"
end

Then(/^I should see "([^"]*)" virtual machine (shut off|running|paused) on "([^"]*)"$/) do |vm, state, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never reached state #{state}") do
    output, _code = node.run("virsh domstate #{vm}")
    break if output.strip == state
    sleep 3
  end
end

When(/^I wait until virtual machine "([^"]*)" on "([^"]*)" is started$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} OS failed did not come up yet") do
    _output, code = node.run("grep -i 'login\:' /tmp/#{vm}.console.log", check_errors: false)
    break if code.zero?
    sleep 1
  end
end

Then(/^I should not see a "([^"]*)" virtual machine on "([^"]*)"$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} still exists") do
    _output, code = node.run("virsh dominfo #{vm}", check_errors: false)
    break if code == 1
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have ([0-9]*)MB memory and ([0-9]*) vcpus$/) do |vm, host, mem, vcpu|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got #{mem}MB memory and #{vcpu} vcpus") do
    output, _code = node.run("virsh dumpxml #{vm}")
    has_memory = output.include? "<memory unit='KiB'>#{Integer(mem) * 1024}</memory>"
    has_vcpus = output.include? ">#{vcpu}</vcpu>"
    break if has_memory and has_vcpus
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have ([a-z]*) graphics device$/) do |vm, host, type|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got #{type} graphics device") do
    output, _code = node.run("virsh dumpxml #{vm}")
    check_nographics = type == "no" and not output.include? '<graphics'
    break if output.include? "<graphics type='#{type}'" or check_nographics
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have ([0-9]*) NIC using "([^"]*)" network$/) do |vm, host, count, net|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got #{count} network interface using #{net}") do
    output, _code = node.run("virsh dumpxml #{vm}")
    break if Nokogiri::XML(output).xpath("//interface/source[@network='#{net}']").size == count.to_i
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have a NIC with ([0-9a-zA-Z:]*) MAC address$/) do |vm, host, mac|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a network interface with #{mac} MAC address") do
    output, _code = node.run("virsh dumpxml #{vm}")
    break if output.include? "<mac address='#{mac}'/>"
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have a "([^"]*)" ([^ ]*) disk$/) do |vm, host, path, bus|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a #{path} #{bus} disk") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks = tree.xpath("//disk").select do |x|
      (x.xpath('source/@file')[0].to_s.include? path) && (x.xpath('target/@bus')[0].to_s == bus)
    end
    break if !disks.empty?
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have a "([^"]*)" ([^ ]+) disk from pool "([^"]*)"$/) do |vm, host, vol, bus, pool|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a #{vol} #{bus} disk from pool #{pool}") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks = tree.xpath("//disk").select do |x|
      (x.xpath('source/@pool')[0].to_s == pool) && (x.xpath('source/@volume')[0].to_s == vol) &&
        (x.xpath('target/@bus')[0].to_s == bus.downcase)
    end
    break if !disks.empty?
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have (no|a) ([^ ]*) ?cdrom$/) do |vm, host, presence, bus|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} #{presence == 'a' ? 'never got' : 'still has'} a #{bus} cdrom") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks = tree.xpath("//disk")
    disk_index = disks.find_index { |x| x.attribute('device').to_s == 'cdrom' }
    break if (disk_index.nil? && presence == 'no') ||
             (!disk_index.nil? && disks[disk_index].xpath('target/@bus')[0].to_s == bus && presence == 'a')
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should have "([^"]*)" attached to a cdrom$/) do |vm, host, path|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual machine on #{host} never got a #{path} attached to cdrom") do
    output, _code = node.run("virsh dumpxml #{vm}")
    tree = Nokogiri::XML(output)
    disks = tree.xpath("//disk")
    disk_index = disks.find_index { |x| x.attribute('device').to_s == 'cdrom' }
    source = !disk_index.nil? && disks[disk_index].xpath('source/@file')[0].to_s || ""
    break if source == path
    sleep 3
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should boot using autoyast$/) do |vm, host|
  node = get_target(host)
  output, _code = node.run("virsh dumpxml #{vm}")
  tree = Nokogiri::XML(output)
  has_kernel = tree.xpath('//os/kernel').size == 1
  has_initrd = tree.xpath('//os/initrd').size == 1
  has_autoyast = tree.xpath('//os/cmdline')[0].to_s.include? ' autoyast='
  unless has_kernel && has_initrd && has_autoyast
    raise 'Wrong kernel/initrd/cmdline configuration, '\
          "kernel: #{has_kernel ? '' : 'not'} set, "\
          "initrd: #{has_initrd ? '' : 'not'} set, "\
          "autoyast kernel parameter: #{has_autoyast ? '' : 'not'} set"
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should boot on hard disk at next start$/) do |vm, host|
  node = get_target(host)
  output, _code = node.run("virsh dumpxml --inactive #{vm}")
  tree = Nokogiri::XML(output)
  has_kernel = tree.xpath('//os/kernel').size == 1
  has_initrd = tree.xpath('//os/initrd').size == 1
  has_cmdline = tree.xpath('//os/cmdline').size == 1
  unless !has_kernel && !has_initrd && !has_cmdline
    raise 'Virtual machine will not boot on hard disk at next start, '\
          "kernel: #{has_kernel ? '' : 'not'} set, "\
          "initrd: #{has_initrd ? '' : 'not'} set, "\
          "cmdline: #{has_cmdline ? '' : 'not'} set"
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should (not stop|stop) on reboot((?: at next start)?)$/) do |vm, host, stop, next_start|
  node = get_target(host)
  inactive = next_start == ' at next start' ? '--inactive' : ''
  output, _code = node.run("virsh dumpxml #{inactive} #{vm}")
  tree = Nokogiri::XML(output)
  on_reboot = tree.xpath('//on_reboot/text()')[0].to_s
  unless on_reboot == 'destroy' && stop == 'stop' || on_reboot == 'restart' && stop == 'not stop'
    raise "Invalid reboot configuration #{next_start}: on_reboot: #{on_reboot}"
  end
end

Then(/^"([^"]*)" virtual machine on "([^"]*)" should be UEFI enabled$/) do |vm, host|
  node = get_target(host)
  output, _code = node.run("virsh dumpxml #{vm}")
  tree = Nokogiri::XML(output)
  has_loader = tree.xpath('//os/loader').size == 1
  has_nvram = tree.xpath('//os/nvram').size == 1
  unless has_loader && has_nvram
    raise "No loader and nvram set: not UEFI enabled"
  end
end

When(/^I create empty "([^"]*)" qcow2 disk file on "([^"]*)"$/) do |path, host|
  node = get_target(host)
  node.run("qemu-img create -f qcow2 #{path} 1G")
end

When(/^I delete all "([^"]*)" volumes from "([^"]*)" pool on "([^"]*)" without error control$/) do |volumes, pool, host|
  node = get_target(host)
  output, _code = node.run("virsh vol-list #{pool} | sed -n -e 's/^[[:space:]]*\([^[:space:]]\+\).*$/\1/;/#{volumes}/p'", check_errors: false)
  output.each_line { |volume| node.run("virsh vol-delete #{volume} #{pool}", check_errors: false) }
end

When(/^I refresh the "([^"]*)" storage pool of this "([^"]*)"$/) do |pool, host|
  node = get_target(host)
  node.run("virsh pool-refresh #{pool}")
end

Then(/^I should not see a "([^"]*)" virtual network on "([^"]*)"$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual network on #{host} still exists") do
    _output, code = node.run("virsh net-info #{vm}", check_errors: false)
    break if code == 1
    sleep 3
  end
end

Then(/^I should see a "([^"]*)" virtual network on "([^"]*)"$/) do |vm, host|
  node = get_target(host)
  repeat_until_timeout(message: "#{vm} virtual network on #{host} still doesn't exist") do
    _output, code = node.run("virsh net-info #{vm}", check_errors: false)
    break if code.zero?
    sleep 3
  end
end

Then(/^"([^"]*)" virtual network on "([^"]*)" should have "([^"]*)" IPv4 address with ([0-9]+) prefix$/) do |net, host, ip, prefix|
  node = get_target(host)
  repeat_until_timeout(message: "#{net} virtual net on #{host} never got #{ip}/#{prefix} IPv4 address") do
    output, _code = node.run("virsh net-dumpxml #{net}")
    tree = Nokogiri::XML(output)
    ips = tree.xpath('//ip[@family="ipv4"]')
    break if !ips.empty? && ips[0]['address'] == ip and ips[0]['prefix'] == prefix
    sleep 3
  end
end

# WORKAROUND
# Work around issue https://github.com/SUSE/spacewalk/issues/10360
# Remove as soon as the issue is fixed
When(/^I let Kiwi build from external repositories$/) do
  get_target('server').run('sed -i \'s/--ignore-repos-used-for-build//\' /usr/share/susemanager/salt/images/kiwi-image-build.sls')
end

When(/^I refresh packages list via spacecmd on "([^"]*)"$/) do |client|
  node = get_system_name(client)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  command = "spacecmd -u admin -p admin system_schedulepackagerefresh #{node}"
  get_target('server').run(command)
end

When(/^I refresh the packages list via package manager on "([^"]*)"$/) do |host|
  node = get_target(host)
  next unless rh_host?(host)

  node.run('yum -y clean all')
  node.run('yum -y makecache')
end

Then(/^I wait until refresh package list on "(.*?)" is finished$/) do |client|
  round_minute = 60 # spacecmd uses timestamps with precision to minutes only
  long_wait_delay = 600
  current_time = Time.now.strftime('%Y%m%d%H%M')
  timeout_time = (Time.now + long_wait_delay + round_minute).strftime('%Y%m%d%H%M')
  node = get_system_name(client)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  # Gather all the ids of package refreshes existing at SUMA
  refreshes, = get_target('server').run('spacecmd -u admin -p admin schedule_list | grep \'Package List Refresh\' | cut -f1 -d\' \'', check_errors: false)
  node_refreshes = ''
  refreshes.split(' ').each do |refresh_id|
    next unless refresh_id.match('/[0-9]{1,4}/')
    refresh_result, = get_target('server').run("spacecmd -u admin -p admin schedule_details #{refresh_id}") # Filter refreshes for specific system
    next unless refresh_result.include? node
    node_refreshes += "^#{refresh_id}|"
  end
  cmd = "spacecmd -u admin -p admin schedule_list #{current_time} #{timeout_time} | egrep '#{node_refreshes.delete_suffix('|')}'"
  repeat_until_timeout(timeout: long_wait_delay, message: '\'refresh package list\' did not finish') do
    result, code = get_target('server').run(cmd, check_errors: false)
    sleep 1
    next if result.include? '0    0    1'
    break if result.include? '1    0    0'
    raise 'refresh package list failed' if result.include? '0    1    0'
  end
end

When(/^spacecmd should show packages "([^"]*)" installed on "([^"]*)"$/) do |packages, client|
  node = get_system_name(client)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  command = "spacecmd -u admin -p admin system_listinstalledpackages #{node}"
  result, _code = get_target('server').run(command, check_errors: false)
  packages.split(' ').each do |package|
    pkg = package.strip
    raise "package #{pkg} is not installed" unless result.include? pkg
  end
end

When(/^I wait until package "([^"]*)" is installed on "([^"]*)" via spacecmd$/) do |pkg, client|
  node = get_system_name(client)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  command = "spacecmd -u admin -p admin system_listinstalledpackages #{node}"
  repeat_until_timeout(timeout: 600, message: "package #{pkg} is not installed yet") do
    result, _code = get_target('server').run(command, check_errors: false)
    break if result.include? pkg
    sleep 1
  end
end

When(/^I wait until package "([^"]*)" is removed from "([^"]*)" via spacecmd$/) do |pkg, client|
  node = get_system_name(client)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  command = "spacecmd -u admin -p admin system_listinstalledpackages #{node}"
  repeat_until_timeout(timeout: 600, message: "package #{pkg} is still present") do
    result, code = get_target('server').run(command, check_errors: false)
    sleep 1
    break unless result.include? pkg
  end
end

When(/^I (enable|disable) the necessary repositories before installing Prometheus exporters on this "([^"]*)"((?: without error control)?)$/) do |action, host, error_control|
  node = get_target(host)
  os_version = node.os_version.gsub('-SP', '.')
  os_family = node.os_family
  repositories = 'tools_pool_repo tools_update_repo'
  if os_family =~ /^opensuse/ || os_family =~ /^sles/
    if product != 'Uyuni'
      repositories.concat(' tools_additional_repo')
      # Needed because in SLES15SP3 and openSUSE 15.3 and higher, firewalld will replace this package.
      # But the tools_update_repo's priority doesn't allow to cope with the obsoletes option from firewalld.
      if os_version.to_f >= 15.3
        node.run('zypper addlock -r tools_additional_repo firewalld-prometheus-config')
      end
    end
  end
  step %(I #{action} the repositories "#{repositories}" on this "#{host}"#{error_control})
end

When(/^I apply "([^"]*)" local salt state on "([^"]*)"$/) do |state, host|
  node = get_target(host)
  salt_call = use_salt_bundle ? 'venv-salt-call' : 'salt-call'
  if host == 'server'
    salt_call = 'salt-call'
  end
  source = File.dirname(__FILE__) + '/../upload_files/salt/' + state + '.sls'
  remote_file = '/usr/share/susemanager/salt/' + state + '.sls'
  return_code = file_inject(node, source, remote_file)
  raise 'File injection failed' unless return_code.zero?
  node.run("#{salt_call} --local --file-root=/usr/share/susemanager/salt --module-dirs=/usr/share/susemanager/salt/ --log-level=info --retcode-passthrough state.apply " + state)
end

When(/^I copy unset package file on server$/) do
  base_dir = File.dirname(__FILE__) + '/../upload_files/unset_package/'
  return_code = file_inject(get_target('server'), base_dir + 'subscription-tools-1.0-0.noarch.rpm', '/root/subscription-tools-1.0-0.noarch.rpm')
  raise 'File injection failed' unless return_code.zero?
end

When(/^I copy vCenter configuration file on server$/) do
  base_dir = File.dirname(__FILE__) + '/../upload_files/virtualization/'
  return_code = file_inject(get_target('server'), base_dir + 'vCenter.json', '/var/tmp/vCenter.json')
  raise 'File injection failed' unless return_code.zero?
end

When(/^I export software channels "([^"]*)" with ISS v2 to "([^"]*)"$/) do |channel, path|
  get_target('server').run("inter-server-sync export --channels=#{channel} --outputDir=#{path}")
end

When(/^I export config channels "([^"]*)" with ISS v2 to "([^"]*)"$/) do |channel, path|
  get_target('server').run("inter-server-sync export --configChannels=#{channel} --outputDir=#{path}")
end

When(/^I import data with ISS v2 from "([^"]*)"$/) do |path|
  get_target('server').run("inter-server-sync import --importDir=#{path}")
end

Then(/^"(.*?)" folder on server is ISS v2 export directory$/) do |folder|
  raise "Folder #{folder} not found" unless file_exists?(get_target('server'), folder + '/sql_statements.sql.gz')
end

Then(/^export folder "(.*?)" shouldn't exist on "(.*?)"$/) do |folder, host|
  node = get_target(host)
  raise 'Folder exists' if folder_exists?(node, folder)
end

When(/^I ensure folder "(.*?)" doesn't exist on "(.*?)"$/) do |folder, host|
  node = get_target(host)
  folder_delete(node, folder) if folder_exists?(node, folder)
end

# ReportDB

Given(/^I can connect to the ReportDB on the Server$/) do
  # connect and quit database
  _result, return_code = get_target('server').run(reportdb_server_query('\\q'))
  raise 'Couldn\'t connect to the ReportDB on the server' unless return_code.zero?
end

Given(/^I have a user with admin access to the ReportDB$/) do
  users_and_permissions, return_code = get_target('server').run(reportdb_server_query('\\du'))
  raise 'Couldn\'t connect to the ReportDB on the server' unless return_code.zero?
  # extract only the line for the suma user
  suma_user_permissions = users_and_permissions[/pythia_susemanager(.*)/]
  raise 'ReportDB admin user pythia_susemanager doesn\'t have the required permissions' unless
    ['Superuser', 'Create role', 'Create DB'].all? { |permission| suma_user_permissions.include? permission }
end

When(/^I create a read-only user for the ReportDB$/) do
  $reportdb_ro_user = 'test_user'
  file = 'create_user_reportdb.exp'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/tmp/#{file}"
  return_code = file_inject(get_target('server'), source, dest)
  raise 'File injection in server failed' unless return_code.zero?
  get_target('server').run("expect -f /tmp/#{file} #{$reportdb_ro_user}")
end

Then(/^I should see the read-only user listed on the ReportDB user accounts$/) do
  users_and_permissions, _code = get_target('server').run(reportdb_server_query('\\du'))
  raise 'Couldn\'t find the newly created user on the ReportDB' unless users_and_permissions.include? $reportdb_ro_user
end

When(/^I delete the read-only user for the ReportDB$/) do
  file = 'delete_user_reportdb.exp'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/tmp/#{file}"
  return_code = file_inject(get_target('server'), source, dest)
  raise 'File injection in server failed' unless return_code.zero?
  get_target('server').run("expect -f /tmp/#{file} #{$reportdb_ro_user}")
end

Then(/^I shouldn't see the read-only user listed on the ReportDB user accounts$/) do
  users_and_permissions, _code = get_target('server').run(reportdb_server_query('\\du'))
  raise 'Created read-only user on the ReportDB remains listed' if users_and_permissions.include? $reportdb_ro_user
end

When(/^I connect to the ReportDB with read-only user from external machine$/) do
  # connection from the controller to the reportdb in the server
  $reportdb_ro_conn = PG.connect(host: get_target('server').public_ip, port: 5432, dbname: 'reportdb', user: $reportdb_ro_user, password: 'linux')
end

Then(/^I should be able to query the ReportDB$/) do
  query_result = $reportdb_ro_conn.exec('select * from system;')
  # raises exception if the query wasn't successful
  query_result.check
  raise 'ReportDB System table is unexpectedly empty after query' unless query_result.ntuples.positive?
end

Then(/^I should find the systems from the UI in the ReportDB$/) do
  reportdb_listed_systems = $reportdb_ro_conn.exec('select hostname from system;').values.flatten
  raise "Listed systems from the UI #{$systems_list} don't match the ones from the ReportDB System table #{reportdb_listed_systems}" unless $systems_list.all? { |ui_system| reportdb_listed_systems.include? ui_system }
end

Then(/^I should not be able to "([^"]*)" data in a ReportDB "([^"]*)" as a read-only user$/) do |db_action, table_type|
  table_and_views = { 'table' => 'system', 'view' => 'systeminactivityreport' }
  assert_raises PG::InsufficientPrivilege do
    case db_action
    when 'insert'
      $reportdb_ro_conn.exec("insert into #{table_and_views[table_type]} (mgm_id, system_id, synced_date) values (1, 1010101, current_timestamp);")
    when 'update'
      $reportdb_ro_conn.exec("update #{table_and_views[table_type]} set mgm_id = 2 where mgm_id = 1")
    when 'delete'
      $reportdb_ro_conn.exec("delete from #{table_and_views[table_type]} where mgm_id = 1")
    else
      raise 'Couldn\'t find command to manipulate the database'
    end
  end
end

Given(/^I know the ReportDB admin user credentials$/) do
  $reportdb_admin_user = get_variable_from_conf_file('server', '/etc/rhn/rhn.conf', 'report_db_user')
  $reportdb_admin_password = get_variable_from_conf_file('server', '/etc/rhn/rhn.conf', 'report_db_password')
end

Then(/^I should be able to connect to the ReportDB with the ReportDB admin user$/) do
  # connection from the controller to the reportdb in the server
  reportdb_admin_conn = PG.connect(host: get_target('server').public_ip, port: 5432, dbname: 'reportdb', user: $reportdb_admin_user, password: $reportdb_admin_password)
  raise 'Couldn\'t connect to ReportDB with admin from external machine' unless reportdb_admin_conn.status.zero?
end

Then(/^I should not be able to connect to product database with the ReportDB admin user$/) do
  dbname = product.delete(' ').downcase
  assert_raises PG::ConnectionBad do
    PG.connect(host: get_target('server').public_ip, port: 5432, dbname: dbname, user: $reportdb_admin_user, password: $reportdb_admin_password)
  end
end

Given(/^I know the current synced_date for "([^"]*)"$/) do |host|
  system_hostname = $node_by_host[host].full_hostname
  query_result = $reportdb_ro_conn.exec("select synced_date from system where hostname = '#{system_hostname}'")
  $initial_synced_date = Time.parse(query_result.tuple(0)[0])
end

Then(/^I should find the updated "([^"]*)" property as "([^"]*)" on the "([^"]*)", on ReportDB$/) do |property_name, property_value, host|
  system_hostname = $node_by_host[host].full_hostname
  property = property_name.split('/')[0].delete(' ').downcase
  query_result = $reportdb_ro_conn.exec("select #{property}, synced_date from system where hostname = '#{system_hostname}'")

  reportdb_property_value = query_result.tuple(0)[0]
  raise "#{property_name}'s value not updated - database still presents #{reportdb_property_value} instead of #{property_value}" unless reportdb_property_value == property_value

  final_synced_date = Time.parse(query_result.tuple(0)[1])
  raise "Column synced_date not updated. Inital synced_date was #{$initial_synced_date} while current synced_date is #{final_synced_date}" unless final_synced_date > $initial_synced_date
end

Given(/^I block connections from "([^"]*)" on "([^"]*)"$/) do |blockhost, target|
  blkhost = get_target(blockhost)
  node = get_target(target)
  node.run("iptables -A INPUT -s #{blkhost.public_ip} -j LOG")
  node.run("iptables -A INPUT -s #{blkhost.public_ip} -j DROP")
end

Then(/^I flush firewall on "([^"]*)"$/) do |target|
  node = get_target(target)
  node.run('iptables -F INPUT')
end

When(/^I generate the configuration "([^"]*)" of Containerized Proxy on the server$/) do |file_path|
  # Doc: https://www.uyuni-project.org/uyuni-docs/en/uyuni/reference/spacecmd/proxy_container.html
  command = "echo spacewalk > cert_pass && spacecmd -u admin -p admin proxy_container_config_generate_cert" \
            " -- -o #{file_path} -p 8022 #{get_target('proxy').full_hostname.sub('pxy', 'pod-pxy')} #{get_target('server').full_hostname}" \
            " 2048 galaxy-noise@suse.de --ca-pass cert_pass --ssl-cname proxy.example.org" \
            " && rm cert_pass"
  get_target('server').run(command)
end

When(/^I add avahi hosts in Containerized Proxy configuration$/) do
  if get_target('server').full_hostname.include? 'tf.local'
    hosts_list = ''
    $host_by_node.each do |node, _host|
      hosts_list += "--add-host=#{node.full_hostname}:#{node.public_ip} "
    end
    hosts_list = escape_regex(hosts_list)
    regex = "s/^#?EXTRA_POD_ARGS=.*$/EXTRA_POD_ARGS=#{hosts_list}/g;"
    get_target('proxy').run("sed -i.bak -Ee '#{regex}' /etc/sysconfig/uyuni-proxy-systemd-services")
    log "Avahi hosts added: #{hosts_list}"
    log 'The Development team has not been working to support avahi in Containerized Proxy, yet. This is best effort.'
  else
    log 'Record not added - avahi domain was not detected'
  end
end

When(/^I remove offending SSH key of "([^"]*)" at port "([^"]*)" for "([^"]*)" on "([^"]*)"$/) do |key_host, key_port, known_hosts_path, host|
  system_name = get_system_name(key_host)
  node = get_target(host)
  node.run("ssh-keygen -R [#{system_name}]:#{key_port} -f #{known_hosts_path}")
end

When(/^I wait until port "([^"]*)" is listening on "([^"]*)"$/) do |port, host|
  node = get_target(host)
  node.run_until_ok("lsof  -i:#{port}")
end

Then(/^port "([^"]*)" should be (open|closed)$/) do |port, selection|
  _output, code = get_target('server').run("ss --listening --numeric | grep :#{port}", check_errors: false, verbose: true)
  port_opened = code.zero?
  if selection == 'closed'
    raise "Port '#{port}' open although it should not be!" if port_opened
  else
    raise "Port '#{port}' not open although it should be!" unless port_opened
  end
end

# rebooting via SSH
When(/^I reboot the server through SSH$/) do
  temp_server = twopence_init('server')
  temp_server.run('reboot > /dev/null 2> /dev/null &')
  default_timeout = 300

  check_shutdown(get_target('server').public_ip, default_timeout)
  check_restart(get_target('server').public_ip, temp_server, default_timeout)

  repeat_until_timeout(timeout: default_timeout, message: 'Spacewalk didn\'t come up') do
    out, code = temp_server.run('spacewalk-service status', check_errors: false, timeout: 10)
    if !out.to_s.include? 'dead' and out.to_s.include? 'running'
      log 'Server spacewalk service is up'
      break
    end
    sleep 1
  end
end

When(/^I reboot the "([^"]*)" host through SSH, waiting until it comes back$/) do |host|
  node = get_target(host)
  node.run('reboot', check_errors: false, verbose: true)
  node.wait_until_offline
  node.wait_until_online
end

When(/^I reboot the "([^"]*)" minion through SSH$/) do |host|
  node = get_target(host)
  node.run('reboot')
end

When(/^I reboot the "([^"]*)" minion through the web UI$/) do |host|
  steps %(
    Given I am on the Systems overview page of this "#{host}"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    And I wait at most 600 seconds until event "System reboot scheduled by #{$current_user}" is completed
    Then I should see a "This action's status is: Completed" text
  )
end

When(/^I reboot the "([^"]*)" if it is a SLE Micro$/) do |host|
  if slemicro_host?(host)
    step %(I reboot the "#{host}" minion through the web UI)
  end
end

When(/^I change the server's short hostname from hosts and hostname files$/) do
  server_node = get_target('server')
  old_hostname = server_node.hostname
  new_hostname = old_hostname + '-renamed'
  log "Old hostname: #{old_hostname} - New hostname: #{new_hostname}"
  server_node.run("sed -i 's/#{old_hostname}/#{new_hostname}/g' /etc/hostname &&
                   hostname #{new_hostname} &&
                   echo '#{server_node.public_ip} #{server_node.full_hostname} #{old_hostname}' >> /etc/hosts &&
                   echo '#{server_node.public_ip} #{new_hostname}#{server_node.full_hostname.delete_prefix(server_node.hostname)} #{new_hostname}' >> /etc/hosts")
  # This will refresh the attributes of this node
  get_target('server', refresh: true)
  hostname, _result = get_target('server').run('hostname')
  hostname.strip!

  raise "Wrong hostname after changing it. Is: #{hostname}, should be: #{new_hostname}" unless hostname == new_hostname

  # Add the new hostname on controller's /etc/hosts to resolve in smoke tests
  `echo '#{server_node.public_ip} #{new_hostname}#{server_node.full_hostname.delete_prefix(server_node.hostname)} #{new_hostname}' >> /etc/hosts`
end

# changing hostname
When(/^I run spacewalk-hostname-rename command on the server$/) do
  server_node = get_target('server')
  command = 'spacecmd --nossl -q api api.getVersion -u admin -p admin; ' \
            "spacewalk-hostname-rename #{server_node.public_ip} " \
            '--ssl-country=DE --ssl-state=Bayern --ssl-city=Nuremberg ' \
            '--ssl-org=SUSE --ssl-orgunit=SUSE --ssl-email=galaxy-noise@suse.de ' \
            '--ssl-ca-password=spacewalk --overwrite_report_db_host=y'
  out_spacewalk, result_code = server_node.run(command, check_errors: false)
  log out_spacewalk.to_s

  server_node = get_target('server', refresh: true) # This will refresh the attributes of this node

  default_timeout = 300
  repeat_until_timeout(timeout: default_timeout, message: 'Spacewalk didn\'t come up') do
    out, _code = server_node.run('spacewalk-service status', check_errors: false, timeout: 10)
    if !out.to_s.include? 'dead' and out.to_s.include? 'running'
      log 'Server: spacewalk service is up'
      break
    end
    sleep 1
  end

  # Update the server CA certificate since it changed, otherwise all API and browser uses will fail
  log 'Update controller CA certificates'
  update_controller_ca

  # Reset the API client to take the new CA into account
  log 'Resetting the API client'
  $api_test = new_api_client

  raise 'Error while running spacewalk-hostname-rename command - see logs above' unless result_code.zero?
  raise 'Error in the output logs - see logs above' if out_spacewalk.include? 'No such file or directory'
end

When(/^I check all certificates after renaming the server hostname$/) do
  # get server certificate serial to compare it with the other minions
  command_server = "openssl x509 -noout -text -in /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT | grep -A1 'Serial' | grep -v 'Serial'"
  server_cert_serial, result_code = get_target('server').run(command_server)
  server_cert_serial.strip!
  log "Server certificate serial: #{server_cert_serial}"

  raise 'Error getting server certificate serial!' unless result_code.zero?

  targets = %w[proxy sle_minion ssh_minion rhlike_minion deblike_minion build_host kvm_server]
  targets.each do |target|
    os_family = get_target(target).os_family
    # get all defined minions from the environment variables and check their certificate serial
    next unless ENV.key? ENV_VAR_BY_HOST[target]
    # Red Hat-like and Debian-like minions store their certificates in a different location
    certificate = if os_family =~ /^centos/ || os_family =~ /^rocky/
                    '/etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT'
                  elsif os_family =~ /^ubuntu/ || os_family =~ /^debian/
                    '/usr/local/share/ca-certificates/susemanager/RHN-ORG-TRUSTED-SSL-CERT.crt'
                  else
                    '/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT'
                  end
    get_target(target).run("test -s #{certificate}", successcodes: [0], check_errors: true)

    command_minion = "openssl x509 -noout -text -in #{certificate} | grep -A1 'Serial' | grep -v 'Serial'"
    minion_cert_serial, result_code = get_target(target).run(command_minion)

    raise "#{target}: Error getting server certificate serial!" unless result_code.zero?

    minion_cert_serial.strip!
    log "#{target} certificate serial: #{minion_cert_serial}"

    raise "#{target}: Error, certificate does not match with server one" unless minion_cert_serial == server_cert_serial
  end
end

When(/^I change back the server's hostname$/) do
  server_node = get_target('server')
  old_hostname = server_node.hostname
  new_hostname = old_hostname.delete_suffix('-renamed')
  log "Old hostname: #{old_hostname} - New hostname: #{new_hostname}"
  server_node.run("sed -i 's/#{old_hostname}/#{new_hostname}/g' /etc/hostname &&
                   hostname #{new_hostname} &&
                   sed -i \'$d\' /etc/hosts &&
                   sed -i \'$d\' /etc/hosts")
  get_target('server', refresh: true) # This will refresh the attributes of this node
  hostname, _result = get_target('server').run('hostname')
  hostname.strip!

  raise "Wrong hostname after changing it. Is: #{hostname}, should be: #{new_hostname}" unless hostname == new_hostname

  # Cleanup the temporary entry in /etc/hosts on the controller
  `sed -i \'$d\' /etc/hosts`
end

When(/^I enable firewall ports for monitoring on this "([^"]*)"$/) do |host|
  add_ports = ''
  [9100, 9117, 9187].each do |port|
    add_ports += "firewall-cmd --add-port=#{port}/tcp --permanent && "
  end
  cmd = "#{add_ports.rstrip!} firewall-cmd --reload"
  node = get_target(host)
  node.run(cmd)
  output, _code = node.run('firewall-cmd --list-ports')
  raise StandardError, "Couldn't successfully enable all ports needed for monitoring. Opened ports: #{output}" unless
    output.include? '9100/tcp 9117/tcp 9187/tcp'
end

When(/^I delete the system "([^"]*)" via spacecmd$/) do |minion|
  node = get_system_name(minion)
  command = "spacecmd -u admin -p admin -y system_delete #{node}"
  get_target('server').run(command, check_errors: true, verbose: true)
end

When(/^I execute "([^"]*)" on the "([^"]*)"$/) do |command, host|
  node = get_target(host)
  node.run(command, check_errors: true, verbose: true)
end

When(/^I check the cloud-init status on "([^"]*)"$/) do |host|
  node = get_target(host)
  node.test_and_store_results_together('hostname', 'root', 500)
  node.run('cloud-init status --wait', check_errors: true, verbose: false)

  repeat_until_timeout(report_result: true) do
    command_output, code = node.run('cloud-init status --wait', check_errors: true, verbose: false)
    break if command_output.include?('done')
    sleep 2
    raise StandardError 'Error during cloud-init.' if code == 1
  end
end

When(/^I do a late hostname initialization of host "([^"]*)"$/) do |host|
  # special handling for e.g. nested VMs that will only be crated later in the test suite
  # this step is normally done in twopence_init.rb
  node = get_target(host)

  hostname, local, remote, code = node.test_and_store_results_together('hostname', 'root', 500)
  raise "Cannot connect to get hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero? || remote.nonzero? || local.nonzero?
  raise "No hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}" if hostname.empty?
  node.init_hostname(hostname)

  fqdn, local, remote, code = node.test_and_store_results_together('hostname -f', 'root', 500)
  raise "Cannot connect to get FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero? || remote.nonzero? || local.nonzero?
  raise "No FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}" if fqdn.empty?
  node.init_full_hostname(fqdn)

  STDOUT.puts "Host '#{$named_nodes[node.hash]}' is alive with determined hostname #{hostname.strip} and FQDN #{fqdn.strip}"
  os_version, os_family = get_os_version(node)
  node.init_os_family(os_family)
  node.init_os_version(os_version)
end

When(/^I wait until I see "([^"]*)" in file "([^"]*)" on "([^"]*)"$/) do |text, file, host|
  node = get_target(host)
  repeat_until_timeout(message: "Entry #{text} in file #{file} on #{host} not found") do
    _output, code = node.run("tail -n 10 #{file} | grep '#{text}' ", check_errors: false)
    break if code.zero?
  end
end

Then(/^the word "([^']*)" does not occur more than (\d+) times in "(.*)" on "([^"]*)"$/) do |word, threshold, path, host|
  count, _ret = get_target(host).run("grep -o -i \'#{word}\' #{path} | wc -l")
  occurences = count.to_i
  raise "The word #{word} occured #{occurences} times, which is more more than #{threshold} times in file #{path}" if occurences > threshold
end
