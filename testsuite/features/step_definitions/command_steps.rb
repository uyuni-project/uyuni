# Copyright (c) 2014-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'timeout'
require 'nokogiri'

# Sanity checks

Then(/^"([^"]*)" should have a FQDN$/) do |host|
  node = get_target(host)
  result, return_code = node.run('hostname -f', check_errors: false)
  result.delete!("\n")
  raise 'cannot determine hostname' unless return_code.zero?
  raise 'hostname is not fully qualified' unless result == node.full_hostname
end

Then(/^reverse resolution should work for "([^"]*)"$/) do |host|
  node = get_target(host)
  result, return_code = node.run("getent hosts #{node.full_hostname}", check_errors: false)
  result.delete!("\n")
  raise 'cannot do reverse resolution' unless return_code.zero?
  raise "reverse resolution for #{node.full_hostname} returned #{result}, expected to see #{node.full_hostname}" unless result.include? node.full_hostname
end

Then(/^"([^"]*)" should communicate with the server using public interface/) do |host|
  node = get_target(host)
  node.run("ping -c 1 -I #{node.public_interface} #{$server.public_ip}")
  $server.run("ping -c 1 #{node.public_ip}")
end

Then(/^"([^"]*)" should not communicate with the server using private interface/) do |host|
  node = get_target(host)
  node.run_until_fail("ping -c 1 -I #{node.private_interface} #{$server.public_ip}")
  $server.run_until_fail("ping -c 1 #{node.private_ip}")
end

Then(/^the clock from "([^"]*)" should be exact$/) do |host|
  node = get_target(host)
  clock_node, _rc = node.run("date +'%s'")
  clock_controller = `date +'%s'`
  difference = clock_node.to_i - clock_controller.to_i
  raise "clocks differ by #{difference} seconds" unless difference.abs < 2
end

Then(/^it should be possible to reach the test packages$/) do
  url = 'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Updates/rpm/x86_64/orion-dummy-1.1-1.1.x86_64.rpm'
  $server.run("curl --insecure --location #{url} --output /dev/null")
end

Then(/^it should be possible to use the HTTP proxy$/) do
  url = 'https://www.suse.com'
  proxy = "suma2:P4$$wordWith%and&@#{$server_http_proxy}"
  $server.run("curl --insecure --proxy '#{proxy}' --proxy-anyauth --location '#{url}' --output /dev/null")
end

Then(/^it should be possible to reach the build sources$/) do
  if $product == 'Uyuni'
    # TODO: move that internal resource to some other external location
    log 'Sanity check not implemented, move resource to external network first'
  else
    url = 'http://download.suse.de/ibs/SUSE/Products/SLE-SERVER/12-SP4/x86_64/product/media.1/products.key'
    $server.run("curl --insecure --location #{url} --output /dev/null")
  end
end

Then(/^it should be possible to reach the Docker profiles$/) do
  git_profiles = ENV['GITPROFILES']
  url = git_profiles.sub(/github\.com/, "raw.githubusercontent.com")
                    .sub(/\.git#:/, "/master/")
                    .sub(/$/, "/Docker/Dockerfile")
  $server.run("curl --insecure --location #{url} --output /dev/null")
end

Then(/^it should be possible to reach the authenticated registry$/) do
  if not ($auth_registry.nil? || $auth_registry.empty?)
    url = "https://#{$auth_registry}"
    $server.run("curl --insecure --location #{url} --output /dev/null")
  end
end

Then(/^it should be possible to reach the not authenticated registry$/) do
  if not ($no_auth_registry.nil? || $no_auth_registry.empty?)
    url = "https://#{$no_auth_registry}"
    $server.run("curl --insecure --location #{url} --output /dev/null")
  end
end

# Channels

When(/^I delete these channels with spacewalk\-remove\-channel:$/) do |table|
  channels_cmd = "spacewalk-remove-channel "
  table.raw.each { |x| channels_cmd = channels_cmd + " -c " + x[0] }
  $command_output, return_code = $server.run(channels_cmd, check_errors: false)
end

When(/^I list channels with spacewalk\-remove\-channel$/) do
  $command_output, return_code = $server.run("spacewalk-remove-channel -l")
  raise "Unable to run spacewalk-remove-channel -l command on server" unless return_code.zero?
end

When(/^I add "([^"]*)" channel$/) do |channel|
  $server.run("echo -e \"admin\nadmin\n\" | mgr-sync add channel #{channel}", buffer_size: 1_000_000)
end

When(/^I use spacewalk\-channel to add "([^"]*)"$/) do |child_channel|
  command = "spacewalk-channel --add -c #{child_channel} -u admin -p admin"
  $command_output, _code = $client.run(command)
end

Then(/^spacewalk\-channel should fail adding "([^"]*)"$/) do |invalid_channel|
  command = "spacewalk-channel --add -c #{invalid_channel} -u admin -p admin"
  $command_output, code = $client.run(command, check_errors: false)
  raise "#{command} should fail, but hasn't" if code.zero?
end

When(/^I use spacewalk\-channel to remove "([^"]*)"$/) do |child_channel|
  command = "spacewalk-channel --remove -c #{child_channel} -u admin -p admin"
  $command_output, _code = $client.run(command)
end

When(/^I use spacewalk\-channel to list channels$/) do
  command = "spacewalk-channel --list"
  $command_output, _code = $client.run(command)
end

When(/^I use spacewalk\-channel to list available channels$/) do
  command = "spacewalk-channel --available-channels -u admin -p admin"
  $command_output, _code = $client.run(command)
end

When(/^I use spacewalk\-common\-channel to add channel "([^"]*)" with arch "([^"]*)"$/) do |child_channel, arch|
  command = "spacewalk-common-channels -u admin -p admin -a #{arch} #{child_channel}"
  $command_output, _code = $server.run(command)
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
  if package.include?("suma") && $product == "Uyuni"
    package.gsub! "suma", "uyuni"
  end
  node = get_target(host)
  if host.include? 'ubuntu'
    node.wait_while_process_running('apt-get')
    pkg_version = package.split('-')[-1]
    pkg_name = package.delete_suffix("-#{pkg_version}")
    pkg_version_regexp = pkg_version.gsub('.', '\\.')
    if status == 'installed'
      node.run_until_ok("dpkg -l | grep -E '^ii +#{pkg_name} +#{pkg_version_regexp} +'")
    else
      node.run_until_fail("dpkg -l | grep -E '^ii +#{pkg_name} +#{pkg_version_regexp} +'")
    end
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
  result, return_code = node.run("LANG=en_US.UTF-8 rpm -q --changelog salt")
  result.split("\n")[0, 15].each do |line|
    line.force_encoding("UTF-8")
    log line
  end
end

When(/^I query latest Salt changes on ubuntu system "(.*?)"$/) do |host|
  node = get_target(host)
  result, return_code = node.run("zcat /usr/share/doc/salt-minion/changelog.Debian.gz")
  result.split("\n")[0, 15].each do |line|
    line.force_encoding("UTF-8")
    log line
  end
end

When(/^vendor change should be enabled for SP migration on "([^"]*)"$/) do |host|
  node = get_target(host)
  pattern = '--allow-vendor-change'
  log = '/var/log/zypper.log'
  rotated_log = "#{log}-#{Time.now.strftime('%Y%m%d')}.xz"
  _, return_code = node.run("grep -- #{pattern} #{log} || xzdec #{rotated_log} | grep -- #{pattern}")

  raise 'Vendor change option not found in logs' unless return_code.zero?
end

When(/^I apply highstate on "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  if host.include? 'ssh_minion'
    cmd = 'runuser -u salt -- salt-ssh --priv=/srv/susemanager/salt/salt_ssh/mgr_ssh_id'
    extra_cmd = '-i --roster-file=/tmp/roster_tests -w -W'
    $server.run("printf '#{system_name}:\n  host: #{system_name}\n  user: root\n  passwd: linux\n' > /tmp/roster_tests")
  elsif host.include? 'minion' or host.include? 'build_host'
    cmd = 'salt'
    extra_cmd = ''
  end
  $server.run_until_ok("cd /tmp; #{cmd} #{system_name} state.highstate #{extra_cmd}")
end

Then(/^I wait until "([^"]*)" service is active on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  cmd = "systemctl is-active #{service}"
  node.run_until_ok(cmd)
end

When(/^I enable product "([^"]*)"$/) do |prd|
  list_output, _code = $server.run("mgr-sync list products", check_errors: false, buffer_size: 1_000_000)
  executed = false
  linenum = 0
  list_output.each_line do |line|
    next unless /^ *\[ \]/ =~ line
    linenum += 1
    next unless line.include? prd
    executed = true
    $command_output, _code = $server.run("echo '#{linenum}' | mgr-sync add product", check_errors: false, buffer_size: 1_000_000)
    break
  end
  raise $command_output.to_s unless executed
end

When(/^I enable product "([^"]*)" without recommended$/) do |prd|
  list_output, _code = $server.run("mgr-sync list products", check_errors: false, buffer_size: 1_000_000)
  executed = false
  linenum = 0
  list_output.each_line do |line|
    next unless /^ *\[ \]/ =~ line
    linenum += 1
    next unless line.include? prd
    executed = true
    $command_output, _code = $server.run("echo '#{linenum}' | mgr-sync add product --no-recommends", check_errors: false, buffer_size: 1_000_000)
    break
  end
  raise $command_output.to_s unless executed
end

When(/^I execute mgr\-sync "([^"]*)" with user "([^"]*)" and password "([^"]*)"$/) do |arg1, u, p|
  $command_output, _code = $server.run("echo -e '#{u}\n#{p}\n' | mgr-sync #{arg1}", check_errors: false, buffer_size: 1_000_000)
end

When(/^I execute mgr\-sync "([^"]*)"$/) do |arg1|
  $command_output, _code = $server.run("mgr-sync #{arg1}", buffer_size: 1_000_000)
end

When(/^I remove the mgr\-sync cache file$/) do
  $command_output, _code = $server.run('rm -f ~/.mgr-sync')
end

When(/^I refresh SCC$/) do
  refresh_timeout = 600
  $server.run('echo -e "admin\nadmin\n" | mgr-sync refresh', timeout: refresh_timeout)
end

When(/^I execute mgr\-sync refresh$/) do
  $command_output, _code = $server.run('mgr-sync refresh', check_errors: false)
end

# This function waits for all the reposyncs to complete.
#
# This function is written as a state machine. It bails out if no process is seen during
# 60 seconds in a row.
When(/^I wait until all spacewalk\-repo\-sync finished$/) do
  reposync_not_running_streak = 0
  reposync_left_running_streak = 0
  while reposync_not_running_streak <= 60
    command_output, _code = $server.run('ps axo pid,cmd | grep spacewalk-repo-sync | grep -v grep', check_errors: false)
    if command_output.empty?
      reposync_not_running_streak += 1
      reposync_left_running_streak = 0
      sleep 1
      next
    end
    reposync_not_running_streak = 0

    process = command_output.split("\n")[0]
    channel = process.split(' ')[5]
    log "Reposync of channel #{channel} left running" if (reposync_left_running_streak % 60).zero?
    reposync_left_running_streak += 1
    sleep 1
  end
end

# This function kills all spacewalk-repo-sync processes, excepted the ones in a whitelist.
# It waits for all the reposyncs in the whitelist to complete, and kills all others.
#
# This function is written as a state machine. It bails out if no process is seen during
# 60 seconds in a row, or if the whitelisted reposyncs last more than 7200 seconds in a row.
When(/^I kill all running spacewalk\-repo\-sync, excepted the ones needed to bootstrap$/) do
  do_not_kill = compute_list_to_leave_running
  reposync_not_running_streak = 0
  reposync_left_running_streak = 0
  while reposync_not_running_streak <= 60 && reposync_left_running_streak <= 7200
    command_output, _code = $server.run('ps axo pid,cmd | grep spacewalk-repo-sync | grep -v grep', check_errors: false)
    if command_output.empty?
      reposync_not_running_streak += 1
      reposync_left_running_streak = 0
      sleep 1
      next
    end
    reposync_not_running_streak = 0

    process = command_output.split("\n")[0]
    channel = process.split(' ')[5]
    if do_not_kill.include? channel
      log "Reposync of channel #{channel} left running" if (reposync_left_running_streak % 60).zero?
      reposync_left_running_streak += 1
      sleep 1
      next
    end
    reposync_left_running_streak = 0

    pid = process.split(' ')[0]
    $server.run("kill #{pid}", check_errors: false)
    log "Reposync of channel #{channel} killed"
  end
end

Then(/^the reposync logs should not report errors$/) do
  result, code = $server.run('grep -i "ERROR:" /var/log/rhn/reposync/*.log', check_errors: false)
  raise "Errors during reposync:\n#{result}" if code.zero?
end

Then(/^"([^"]*)" package should have been stored$/) do |pkg|
  $server.run("find /var/spacewalk/packages -name #{pkg}")
end

Then(/^solver file for "([^"]*)" should reference "([^"]*)"$/) do |channel, pkg|
  $server.run("dumpsolv /var/cache/rhn/repodata/#{channel}/solv | grep #{pkg}")
end

When(/^I wait until the channel "([^"]*)" has been synced$/) do |channel|
  begin
    repeat_until_timeout(timeout: 7200, message: 'Channel not fully synced') do
      _result, code = $server.run("test -f /var/cache/rhn/repodata/#{channel}/repomd.xml", check_errors: false)
      break if code.zero?
      sleep 10
    end
  rescue StandardError => e
    log e.message # It might be that the MU repository is wrong, but we want to continue in any case
  end
end

When(/^I execute mgr\-bootstrap "([^"]*)"$/) do |arg1|
  arch = 'x86_64'
  $command_output, _code = $server.run("mgr-bootstrap --activation-keys=1-SUSE-KEY-#{arch} #{arg1}")
end

When(/^I fetch "([^"]*)" to "([^"]*)"$/) do |file, host|
  node = get_target(host)
  node.run("wget http://#{$server.full_hostname}/#{file}")
end

When(/^I wait until file "([^"]*)" contains "([^"]*)" on server$/) do |file, content|
  repeat_until_timeout(message: "#{content} not found in file #{file}", report_result: true) do
    output, _code = $server.run("grep #{content} #{file}", check_errors: false)
    break if output =~ /#{content}/
    sleep 2
    "\n-----\n#{output}\n-----\n"
  end
end

Then(/^file "([^"]*)" should contain "([^"]*)" on server$/) do |file, content|
  output, _code = $server.run("grep -F '#{content}' #{file}", check_errors: false)
  raise "'#{content}' not found in file #{file}" if output !~ /#{content}/
  "\n-----\n#{output}\n-----\n"
end

Then(/^file "([^"]*)" should not contain "([^"]*)" on server$/) do |file, content|
  output, _code = $server.run("grep -F '#{content}' #{file} || echo 'notfound'", check_errors: false)
  raise "'#{content}' found in file #{file}" if output != "notfound\n"
  "\n-----\n#{output}\n-----\n"
end

Then(/^the tomcat logs should not contain errors$/) do
  output, _code = $server.run('cat /var/log/tomcat/*')
  msgs = %w[ERROR NullPointer]
  msgs.each do |msg|
    raise "-#{msg}-  msg found on tomcat logs" if output.include? msg
  end
end

When(/^I restart the spacewalk service$/) do
  $server.run('spacewalk-service restart')
end

When(/^I shutdown the spacewalk service$/) do
  $server.run('spacewalk-service stop')
end

When(/^I restart cobbler on the server$/) do
  $server.run('systemctl restart cobblerd.service')
end

When(/^I execute spacewalk-debug on the server$/) do
  $server.run('spacewalk-debug')
  code = file_extract($server, "/tmp/spacewalk-debug.tar.bz2", "spacewalk-debug.tar.bz2")
  raise "Download debug file failed" unless code.zero?
end

When(/^I extract the log files from all our active nodes$/) do
  $nodes.each do |node|
    next if node.nil?

    extract_logs_from_node(node)
  end
end

Then(/^the susemanager repo file should exist on the "([^"]*)"$/) do |host|
  step %(file "/etc/zypp/repos.d/susemanager\:channels.repo" should exist on "#{host}")
end

Then(/^I should see "([^"]*)", "([^"]*)" and "([^"]*)" in the repo file on the "([^"]*)"$/) do |protocol, hostname, port, target|
  node = get_target(target)
  hostname = hostname == "proxy" ? $proxy.full_hostname : hostname
  base_url, _code = node.run('grep "baseurl" /etc/zypp/repos.d/susemanager\:channels.repo')
  base_url = base_url.strip.split('=')[1].delete '"'
  uri = URI.parse(base_url)
  log 'Protocol: ' + uri.scheme + '  Host: ' + uri.host + '  Port: ' + uri.port.to_s
  parameters_matches = (uri.scheme == protocol && uri.host == hostname && uri.port == port.to_i)
  if !parameters_matches
    raise 'Some parameters are not as expected'
  end
end

When(/^I copy "([^"]*)" to "([^"]*)"$/) do |file, host|
  node = get_target(host)
  return_code = file_inject(node, file, File.basename(file))
  raise 'File injection failed' unless return_code.zero?
end

Then(/^the PXE default profile should be enabled$/) do
  step %(I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT pxe-default-profile" on server)
end

Then(/^the PXE default profile should be disabled$/) do
  step %(I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT local" on server)
end

When(/^I import the GPG keys for "([^"]*)"$/) do |host|
  node = get_target(host)
  gpg_keys = get_gpg_keys(node)
  gpg_keys.each do |key|
    gpg_key_import_cmd = host.include?('ubuntu') ? 'apt-key add' : 'rpm --import'
    node.run("cd /tmp/ && curl --output #{key} #{$server.full_hostname}/pub/#{key} && #{gpg_key_import_cmd} /tmp/#{key}")
  end
end

When(/^the server starts mocking an IPMI host$/) do
  ['ipmisim1.emu', 'lan.conf', 'fake_ipmi_host.sh'].each do |file|
    source = File.dirname(__FILE__) + '/../upload_files/' + file
    dest = '/etc/ipmi/' + file
    return_code = file_inject($server, source, dest)
    raise 'File injection failed' unless return_code.zero?
  end
  $server.run('chmod +x /etc/ipmi/fake_ipmi_host.sh')
  $server.run('ipmi_sim -n < /dev/null > /dev/null &')
end

When(/^the server stops mocking an IPMI host$/) do
  $server.run('pkill ipmi_sim')
  $server.run('pkill fake_ipmi_host.sh || :')
end

When(/^the server starts mocking a Redfish host$/) do
  $server.run('mkdir -p /root/Redfish-Mockup-Server/')
  ['redfishMockupServer.py', 'rfSsdpServer.py'].each do |file|
    source = File.dirname(__FILE__) + '/../upload_files/Redfish-Mockup-Server/' + file
    dest = '/root/Redfish-Mockup-Server/' + file
    return_code = file_inject($server, source, dest)
    raise 'File injection failed' unless return_code.zero?
  end
  $server.run('curl --output DSP2043_2019.1.zip https://www.dmtf.org/sites/default/files/standards/documents/DSP2043_2019.1.zip')
  $server.run('unzip DSP2043_2019.1.zip')
  cmd = "/usr/bin/python3 /root/Redfish-Mockup-Server/redfishMockupServer.py " \
        "-H #{$server.full_hostname} -p 8443 " \
        "-S -D /root/DSP2043_2019.1/public-catfish/ " \
        "--ssl --cert /etc/pki/tls/certs/spacewalk.crt --key /etc/pki/tls/private/spacewalk.key " \
        "< /dev/null > /dev/null 2>&1 &"
  $server.run(cmd)
end

When(/^the server stops mocking a Redfish host$/) do
  $server.run('pkill -e -f /root/Redfish-Mockup-Server/redfishMockupServer.py')
end

When(/^I install a user-defined state for "([^"]*)" on the server$/) do |host|
  system_name = get_system_name(host)
  # copy state file to server
  file = 'user_defined_state.sls'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = "/srv/salt/" + file
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  # generate top file and copy it to server
  script = "base:\n" \
           "  '#{system_name}':\n" \
           "    - user_defined_state\n"
  path = generate_temp_file('top.sls', script)
  return_code = file_inject($server, path, '/srv/salt/top.sls')
  raise 'File injection failed' unless return_code.zero?
  `rm #{path}`
  # make both files readeable by salt
  $server.run('chgrp salt /srv/salt/*')
end

When(/^I uninstall the user-defined state from the server$/) do
  $server.run('rm /srv/salt/{user_defined_state.sls,top.sls}')
end

When(/^I uninstall the managed file from "([^"]*)"$/) do |host|
  node = get_target(host)
  node.run('rm /tmp/test_user_defined_state')
end

Then(/^the cobbler report should contain "([^"]*)" for "([^"]*)"$/) do |text, host|
  node = get_target(host)
  output, _code = $server.run("cobbler system report --name #{node.full_hostname}:1", check_errors: false)
  raise "Not found:\n#{output}" unless output.include?(text)
end

Then(/^the cobbler report should contain "([^"]*)" for cobbler system name "([^"]*)"$/) do |text, name|
  output, _code = $server.run("cobbler system report --name #{name}", check_errors: false)
  raise "Not found:\n#{output}" unless output.include?(text)
end

When(/^I configure tftp on the "([^"]*)"$/) do |host|
  raise "This step doesn't support #{host}" unless ['server', 'proxy'].include? host

  case host
  when 'server'
    $server.run("configure-tftpsync.sh #{ENV['PROXY']}")
  when 'proxy'
    cmd = "configure-tftpsync.sh --non-interactive --tftpbootdir=/srv/tftpboot \
--server-fqdn=#{ENV['SERVER']} \
--proxy-fqdn='proxy.example.org'"
    $proxy.run(cmd)
  end
end

When(/^I synchronize the tftp configuration on the proxy with the server$/) do
  out, _code = $server.run('cobbler sync')
  raise 'cobbler sync failed' if out.include? 'Push failed'
end

When(/^I set the default PXE menu entry to the "([^"]*)" on the "([^"]*)"$/) do |entry, host|
  raise "This step doesn't support #{host}" unless ['server', 'proxy'].include? host

  node = get_target(host)
  target = '/srv/tftpboot/pxelinux.cfg/default'
  case entry
  when 'local boot'
    script = "-e 's/^TIMEOUT .*/TIMEOUT 1/' -e 's/ONTIMEOUT .*/ONTIMEOUT local/'"
  when 'target profile'
    script = "-e 's/^TIMEOUT .*/TIMEOUT 1/' -e 's/ONTIMEOUT .*/ONTIMEOUT 15-sp2-cobbler:1:SUSETest/'"
  end
  node.run("sed -i #{script} #{target}")
end

When(/^I clean the search index on the server$/) do
  output, _code = $server.run('/usr/sbin/rcrhn-search cleanindex', check_errors: false)
  raise 'The output includes an error log' if output.include?('ERROR')
end

Then(/^I wait until mgr-sync refresh is finished$/) do
  # mgr-sync refresh is a slow operation, we don't use the default timeout
  cmd = "spacecmd -u admin -p admin api sync.content.listProducts"
  repeat_until_timeout(timeout: 1800, message: "'mgr-sync refresh' did not finish") do
    result, code = $server.run(cmd, check_errors: false)
    break if result.include? "SLES"
    sleep 5
  end
end

Then(/^I should see "(.*?)" in the output$/) do |arg1|
  raise "Command Output #{@command_output} don't include #{arg1}" unless @command_output.include? arg1
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

Then(/^service or socket "([^"]*)" is enabled on "([^"]*)"$/) do |name, host|
  node = get_target(host)
  output_service, _code_service = node.run("systemctl is-enabled '#{name}'", check_errors: false)
  output_service = output_service.split(/\n+/)[-1]
  output_socket, _code_socket = node.run(" systemctl is-enabled '#{name}.socket'", check_errors: false)
  output_socket = output_socket.split(/\n+/)[-1]
  raise if output_service != 'enabled' and output_socket != 'enabled'
end

Then(/^service or socket "([^"]*)" is active on "([^"]*)"$/) do |name, host|
  node = get_target(host)
  output_service, _code_service = node.run("systemctl is-active '#{name}'", check_errors: false)
  output_service = output_service.split(/\n+/)[-1]
  output_socket, _code_socket = node.run(" systemctl is-active '#{name}.socket'", check_errors: false)
  output_socket = output_socket.split(/\n+/)[-1]
  raise if output_service != 'active' and output_socket != 'active'
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
    break if file_exists?($server, file)
    sleep(1)
  end
end

Then(/^I wait and check that "([^"]*)" has rebooted$/) do |host|
  reboot_timeout = 800
  system_name = get_system_name(host)
  check_shutdown(system_name, reboot_timeout)
  check_restart(system_name, get_target(host), reboot_timeout)
end

When(/^I call spacewalk\-repo\-sync for channel "(.*?)" with a custom url "(.*?)"$/) do |arg1, arg2|
  @command_output, _code = $server.run("spacewalk-repo-sync -c #{arg1} -u #{arg2}", check_errors: false)
end

When(/^I get "(.*?)" file details for channel "(.*?)" via spacecmd$/) do |arg1, arg2|
  @command_output, _code = $server.run("spacecmd -u admin -p admin -q -- configchannel_filedetails #{arg2} '#{arg1}'", check_errors: false)
end

When(/^I disable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run('sysctl net.ipv6.conf.all.forwarding=0')
end

When(/^I enable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run('sysctl net.ipv6.conf.all.forwarding=1')
end

When(/^I wait for the OpenSCAP audit to finish$/) do
  host = $server.full_hostname
  @sle_id = retrieve_server_id($minion.full_hostname)
  @client_api = XMLRPC::Client.new2('http://' + host + '/rpc/api')
  @sid = @client_api.call('auth.login', 'admin', 'admin')
  begin
    repeat_until_timeout(message: "process did not complete") do
      scans = @client_api.call('system.scap.list_xccdf_scans', @sid, @sle_id)
      # in the openscap test, we schedule 2 scans
      break if scans.length > 1
    end
  ensure
    @client_api.call('auth.logout', @sid)
  end
end

When(/^I register this client for SSH push via tunnel$/) do
  # create backups of /etc/hosts and up2date config
  $server.run('cp /etc/hosts /etc/hosts.BACKUP')
  $server.run('cp /etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP')
  # generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  script = "spawn spacewalk-ssh-push-init --client #{$client.full_hostname} --register #{bootstrap} --tunnel\n" \
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
  $server.run("expect #{filename}", timeout: bootstrap_timeout)
  # restore files from backups
  $server.run('mv /etc/hosts.BACKUP /etc/hosts')
  $server.run('mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date')
end

# Repositories and packages management
When(/^I migrate the non-SUMA repositories on "([^"]*)"$/) do |host|
  node = get_target(host)
  # use sumaform states to migrate to latest SP the system repositories:
  node.run('salt-call --local --file-root /root/salt/ state.apply repos')
  # disable again the non-SUMA repositories:
  node.run("for repo in $(zypper lr | awk 'NR>7 && !/susemanager:/ {print $3}'); do zypper mr -d $repo; done")
  # node.run('salt-call state.apply channels.disablelocalrepos') does not work
end

When(/^I (enable|disable) Ubuntu "([^"]*)" repository on "([^"]*)"$/) do |action, repo, host|
  node = get_target(host)
  _os_version, os_family = get_os_version(node)
  raise "#{node.hostname} is not a Ubuntu host." unless os_family =~ /^ubuntu/

  node.run("sudo add-apt-repository -y -u #{action == 'disable' ? '--remove' : ''} #{repo}")
end

# rubocop:disable Metrics/BlockLength
When(/^I (enable|disable) (the repositories|repository) "([^"]*)" on this "([^"]*)"((?: without error control)?)$/) do |action, _optional, repos, host, error_control|
  node = get_target(host)
  _os_version, os_family = get_os_version(node)
  cmd = if os_family =~ /^opensuse/ || os_family =~ /^sles/
          mand_repos = ""
          opt_repos = ""
          repos.split(' ').map do |repo|
            if repo =~ /_ltss_/
              opt_repos = "#{opt_repos} #{repo}"
            else
              mand_repos = "#{mand_repos} #{repo}"
            end
          end
          "zypper mr --#{action} #{opt_repos} ||:; zypper mr --#{action} #{mand_repos};"
        else
          cmd_list = if action == 'enable'
                       repos.split(' ').map do |repo|
                         if os_family =~ /^centos/
                           "sed -i 's/enabled=.*/enabled=1/g' /etc/yum.repos.d/#{repo}.repo; "
                         elsif (os_family =~ /^ubuntu/) || (os_family =~ /^debian/)
                           "sed -i '/^#\\s*deb.*/ s/^#\\s*deb /deb /' /etc/apt/sources.list.d/#{repo}.list; "
                         end
                       end
                     else
                       repos.split(' ').map do |repo|
                         if os_family =~ /^centos/
                           "sed -i 's/enabled=.*/enabled=0/g' /etc/yum.repos.d/#{repo}.repo; "
                         elsif (os_family =~ /^ubuntu/) || (os_family =~ /^debian/)
                           "sed -i '/^deb.*/ s/^deb /# deb /' /etc/apt/sources.list.d/#{repo}.list; "
                         end
                       end
                     end
          cmd_list.reduce(:+)
        end
  node.run(cmd, check_errors: error_control.empty?)
end
# rubocop:enable Metrics/BlockLength

When(/^I enable source package syncing$/) do
  cmd = "echo 'server.sync_source_packages = 1' >> /etc/rhn/rhn.conf"
  $server.run(cmd)
end

When(/^I disable source package syncing$/) do
  cmd = "sed -i 's/^server.sync_source_packages = 1.*//g' /etc/rhn/rhn.conf"
  $server.run(cmd)
end

When(/^I install pattern "([^"]*)" on this "([^"]*)"$/) do |pattern, host|
  if pattern.include?("suma") && $product == "Uyuni"
    pattern.gsub! "suma", "uyuni"
  end
  node = get_target(host)
  node.run('zypper ref')
  cmd = "zypper --non-interactive install -t pattern #{pattern}"
  node.run(cmd, successcodes: [0, 100, 101, 102, 103, 106])
end

When(/^I remove pattern "([^"]*)" from this "([^"]*)"$/) do |pattern, host|
  if pattern.include?("suma") && $product == "Uyuni"
    pattern.gsub! "suma", "uyuni"
  end
  node = get_target(host)
  node.run('zypper ref')
  cmd = "zypper --non-interactive remove -t pattern #{pattern}"
  node.run(cmd, successcodes: [0, 100, 101, 102, 103, 104, 106])
end

When(/^I (install|remove) the traditional stack utils (on|from) "([^"]*)"$/) do |action, where, host|
  pkgs = 'spacewalk-client-tools spacewalk-check spacewalk-client-setup mgr-daemon mgr-osad mgr-cfg-actions'
  step %(I #{action} packages "#{pkgs}" #{where} this "#{host}")
end

When(/^I (install|remove) OpenSCAP dependencies (on|from) "([^"]*)"$/) do |action, where, host|
  node = get_target(host)
  _os_version, os_family = get_os_version(node)
  if os_family =~ /^opensuse/ || os_family =~ /^sles/
    pkgs = 'openscap-utils openscap-content scap-security-guide'
  elsif os_family =~ /^centos/
    pkgs = 'openscap-utils scap-security-guide-redhat'
  elsif os_family =~ /^ubuntu/
    pkgs = 'libopenscap8 scap-security-guide-ubuntu'
  else
    raise "The node #{node.hostname} has not a supported OS Family (#{os_family})"
  end
  pkgs += ' spacewalk-oscap' if host.include? 'client'
  step %(I #{action} packages "#{pkgs}" #{where} this "#{host}")
end

When(/^I install package(?:s)? "([^"]*)" on this "([^"]*)"((?: without error control)?)$/) do |package, host, error_control|
  node = get_target(host)
  if host.include? 'ceos'
    cmd = "yum -y install #{package}"
    successcodes = [0]
    not_found_msg = 'No package'
  elsif host.include? 'ubuntu'
    cmd = "apt-get --assume-yes install #{package}"
    successcodes = [0]
    not_found_msg = 'Unable to locate package'
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
  if host.include? 'ceos'
    cmd = "yum -y downgrade #{package}"
    successcodes = [0]
    not_found_msg = 'No package'
  elsif host.include? 'ubuntu'
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
  if host.include? 'ceos'
    cmd = "yum -y remove #{package}"
    successcodes = [0]
  elsif host.include? 'ubuntu'
    cmd = "dpkg --remove #{package}"
    successcodes = [0]
  else
    cmd = "zypper --non-interactive remove -y #{package}"
    successcodes = [0, 100, 101, 102, 103, 104, 106]
  end
  node.run(cmd, check_errors: error_control.empty?, successcodes: successcodes)
end

When(/^I install package tftpboot-installation on the server$/) do
  output, _code = $server.run('find /var/spacewalk/packages -name tftpboot-installation-SLE-15-SP2-x86_64-*.noarch.rpm')
  packages = output.split("\n")
  pattern = '/tftpboot-installation-([^/]+)*.noarch.rpm'
  # Reverse sort the package name to get the latest version first and install it
  package = packages.min { |a, b| b.match(pattern)[0] <=> a.match(pattern)[0] }
  $server.run("rpm -i #{package}")
end

When(/^I reset tftp defaults on the proxy$/) do
  $proxy.run("echo 'TFTP_USER=\"tftp\"\nTFTP_OPTIONS=\"\"\nTFTP_DIRECTORY=\"/srv/tftpboot\"\n' > /etc/sysconfig/tftp")
end

When(/^I wait until the package "(.*?)" has been cached on this "(.*?)"$/) do |pkg_name, host|
  node = get_target(host)
  if host == 'sle_minion'
    cmd = "ls /var/cache/zypp/packages/susemanager:test-channel-x86_64/getPackage/*/*/#{pkg_name}*.rpm"
  elsif host.include? 'ubuntu'
    cmd = "ls /var/cache/apt/archives/#{pkg_name}*.deb"
  end
  repeat_until_timeout(message: "Package #{pkg_name} was not cached") do
    result, return_code = node.run(cmd, check_errors: false)
    break if return_code.zero?
    sleep 2
  end
end

# WORKAROUND: --flush option does not seem to work in case of hash mismatch with same version
When(/^I clean up all bootstrap repositories on the server$/) do
  $server.run('rm -rf /srv/www/htdocs/pub/repositories/*')
end

When(/^I create the bootstrap repository for "([^"]*)" on the server$/) do |host|
  base_channel = BASE_CHANNEL_BY_CLIENT[host]
  channel = CHANNEL_TO_SYNC_BY_BASE_CHANNEL[base_channel]
  parent_channel = PARENT_CHANNEL_TO_SYNC_BY_BASE_CHANNEL[base_channel]
  cmd = if parent_channel.nil?
          "mgr-create-bootstrap-repo --create #{channel} --with-custom-channels --flush"
        else
          "mgr-create-bootstrap-repo --create #{channel} --with-parent-channel #{parent_channel} --with-custom-channels --flush"
        end
  log 'Creating the boostrap repository on the server:'
  log '  ' + cmd
  $server.run(cmd)
end

When(/^I install "([^"]*)" product on the proxy$/) do |product|
  out, = $proxy.run("zypper ref && zypper --non-interactive install --auto-agree-with-licenses --force-resolution -t product #{product}")
  log "Installed #{product} product: #{out}"
end

When(/^I install proxy pattern on the proxy$/) do
  pattern = $product == 'Uyuni' ? 'uyuni_proxy' : 'suma_proxy'
  cmd = "zypper --non-interactive install -t pattern #{pattern}"
  $proxy.run(cmd, timeout: 600, successcodes: [0, 100, 101, 102, 103, 106])
end

When(/^I let squid use avahi on the proxy$/) do
  file = '/usr/share/rhn/proxy-template/squid.conf'
  key = 'dns_multicast_local'
  val = 'on'
  $proxy.run("grep '^#{key}' #{file} && sed -i -e 's/^#{key}.*$/#{key} #{val}/' #{file} || echo '#{key} #{val}' >> #{file}")
  key = 'ignore_unknown_nameservers'
  val = 'off'
  $proxy.run("grep '^#{key}' #{file} && sed -i -e 's/^#{key}.*$/#{key} #{val}/' #{file} || echo '#{key} #{val}' >> #{file}")
end

When(/^I open avahi port on the proxy$/) do
  $proxy.run('firewall-offline-cmd --zone=public --add-service=mdns')
end

When(/^I copy server\'s keys to the proxy$/) do
  ['RHN-ORG-PRIVATE-SSL-KEY', 'RHN-ORG-TRUSTED-SSL-CERT', 'rhn-ca-openssl.cnf'].each do |file|
    return_code = file_extract($server, '/root/ssl-build/' + file, '/tmp/' + file)
    raise 'File extraction failed' unless return_code.zero?
    $proxy.run('mkdir -p /root/ssl-build')
    return_code = file_inject($proxy, '/tmp/' + file, '/root/ssl-build/' + file)
    raise 'File injection failed' unless return_code.zero?
  end
end

When(/^I configure the proxy$/) do
  # prepare the settings file
  settings = "RHN_PARENT=#{$server.full_hostname}\n" \
             "HTTP_PROXY=''\n" \
             "VERSION=''\n" \
             "TRACEBACK_EMAIL=galaxy-noise@suse.de\n" \
             "USE_SSL=y\n" \
             "USE_EXISTING_CERTS=n\n" \
             "INSTALL_MONITORING=n\n" \
             "SSL_PASSWORD=spacewalk\n" \
             "SSL_ORG=SUSE\n" \
             "SSL_ORGUNIT=SUSE\n" \
             "SSL_COMMON=#{$proxy.full_hostname}\n" \
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
  $proxy.run(cmd, timeout: proxy_timeout)
end

When(/^I allow all SSL protocols on the proxy's apache$/) do
  file = '/etc/apache2/ssl-global.conf'
  key = 'SSLProtocol'
  val = 'all -SSLv2 -SSLv3'
  $proxy.run("grep '#{key}' #{file} && sed -i -e 's/#{key}.*$/#{key} #{val}/' #{file}")
  $proxy.run("systemctl reload apache2.service")
end

When(/^I restart squid service on the proxy$/) do
  # We need to restart squid when we add a CNAME to the certificate
  $proxy.run("systemctl restart squid.service")
end

Then(/^The metadata buildtime from package "(.*?)" match the one in the rpm on "(.*?)"$/) do |pkg, host|
  # for testing buildtime of generated metadata - See bsc#1078056
  node = get_target(host)
  cmd = "dumpsolv /var/cache/zypp/solv/spacewalk\:test-channel-x86_64/solv | grep -E 'solvable:name|solvable:buildtime'| grep -A1 '#{pkg}$'| perl -ne 'if($_ =~ /^solvable:buildtime:\\s*(\\d+)/) { print $1; }'"
  metadata_buildtime, return_code = node.run(cmd)
  raise "Command failed: #{cmd}" unless return_code.zero?
  cmd = "rpm -q --qf '%{BUILDTIME}' #{pkg}"
  rpm_buildtime, return_code = node.run(cmd)
  raise "Command failed: #{cmd}" unless return_code.zero?
  raise "Wrong buildtime in metadata: #{metadata_buildtime} != #{rpm_buildtime}" unless metadata_buildtime == rpm_buildtime
end

When(/^I create channel "([^"]*)" from spacecmd of type "([^"]*)"$/) do |name, type|
  command = "spacecmd -u admin -p admin -- configchannel_create -n #{name} -t  #{type}"
  $server.run(command)
end

When(/^I update init.sls from spacecmd with content "([^"]*)" for channel "([^"]*)"$/) do |content, label|
  filepath = "/tmp/#{label}"
  $server.run("echo -e \"#{content}\" > #{filepath}", timeout: 600)
  command = "spacecmd -u admin -p admin -- configchannel_updateinitsls -c #{label} -f  #{filepath} -y"
  $server.run(command)
  file_delete($server, filepath)
end

When(/^I update init.sls from spacecmd with content "([^"]*)" for channel "([^"]*)" and revision "([^"]*)"$/) do |content, label, revision|
  filepath = "/tmp/#{label}"
  $server.run("echo -e \"#{content}\" > #{filepath}", timeout: 600)
  command = "spacecmd -u admin -p admin -- configchannel_updateinitsls -c #{label} -f #{filepath} -r #{revision} -y"
  $server.run(command)
  file_delete($server, filepath)
end

When(/^I schedule apply configchannels for "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $server.run('spacecmd -u admin -p admin clear_caches')
  command = "spacecmd -y -u admin -p admin -- system_scheduleapplyconfigchannels  #{system_name}"
  $server.run(command)
end

When(/^I create "([^"]*)" virtual machine on "([^"]*)"$/) do |vm_name, host|
  node = get_target(host)
  disk_path = "/tmp/#{vm_name}_disk.qcow2"

  # Create the throwable overlay image
  raise '/var/testsuite-data/disk-image-template.qcow2 not found' unless file_exists?(node, '/var/testsuite-data/disk-image-template.qcow2')
  node.run("cp /var/testsuite-data/disk-image-template.qcow2 #{disk_path}")

  # Actually define the VM, but don't start it
  raise 'not found: virt-install' unless file_exists?(node, '/usr/bin/virt-install')
  node.run("virt-install --name #{vm_name} --memory 512 --vcpus 1 --disk path=#{disk_path} "\
           "--network network=test-net0 --graphics vnc "\
           "--serial file,path=/tmp/#{vm_name}.console.log "\
           "--import --hvm --noautoconsole --noreboot")
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

# WORKAROUND
# Work around issue https://github.com/SUSE/spacewalk/issues/10360
# Remove as soon as the issue is fixed
When(/^I let Kiwi build from external repositories$/) do
  $server.run("sed -i 's/--ignore-repos-used-for-build//' /usr/share/susemanager/salt/images/kiwi-image-build.sls")
end

When(/^I refresh packages list via spacecmd on "([^"]*)"$/) do |client|
  node = get_system_name(client)
  $server.run("spacecmd -u admin -p admin clear_caches")
  command = "spacecmd -u admin -p admin system_schedulepackagerefresh #{node}"
  $server.run(command)
end

When(/^I refresh the packages list via package manager on "([^"]*)"$/) do |host|
  node = get_target(host)
  next unless host.include? 'ceos'

  node.run('yum -y clean all')
  node.run('yum -y makecache')
end

Then(/^I wait until refresh package list on "(.*?)" is finished$/) do |client|
  round_minute = 60 # spacecmd uses timestamps with precision to minutes only
  long_wait_delay = 600
  current_time = Time.now.strftime('%Y%m%d%H%M')
  timeout_time = (Time.now + long_wait_delay + round_minute).strftime('%Y%m%d%H%M')
  node = get_system_name(client)
  $server.run("spacecmd -u admin -p admin clear_caches")
  # Gather all the ids of package refreshes existing at SUMA
  refreshes, = $server.run("spacecmd -u admin -p admin schedule_list | grep 'Package List Refresh' | cut -f1 -d' '", check_errors: false)
  node_refreshes = ""
  refreshes.split(' ').each do |refresh_id|
    next unless refresh_id.match('/[0-9]{1,4}/')
    refresh_result, = $server.run("spacecmd -u admin -p admin schedule_details #{refresh_id}") # Filter refreshes for specific system
    next unless refresh_result.include? node
    node_refreshes += "^#{refresh_id}|"
  end
  cmd = "spacecmd -u admin -p admin schedule_list #{current_time} #{timeout_time} | egrep '#{node_refreshes.delete_suffix('|')}'"
  repeat_until_timeout(timeout: long_wait_delay, message: "'refresh package list' did not finish") do
    result, code = $server.run(cmd, check_errors: false)
    sleep 1
    next if result.include? '0    0    1'
    break if result.include? '1    0    0'
    raise 'refresh package list failed' if result.include? '0    1    0'
  end
end

When(/^spacecmd should show packages "([^"]*)" installed on "([^"]*)"$/) do |packages, client|
  node = get_system_name(client)
  $server.run("spacecmd -u admin -p admin clear_caches")
  command = "spacecmd -u admin -p admin system_listinstalledpackages #{node}"
  result, _code = $server.run(command, check_errors: false)
  packages.split(' ').each do |package|
    pkg = package.strip
    raise "package #{pkg} is not installed" unless result.include? pkg
  end
end

When(/^I wait until package "([^"]*)" is installed on "([^"]*)" via spacecmd$/) do |pkg, client|
  node = get_system_name(client)
  $server.run("spacecmd -u admin -p admin clear_caches")
  command = "spacecmd -u admin -p admin system_listinstalledpackages #{node}"
  repeat_until_timeout(timeout: 600, message: "package #{pkg} is not installed yet") do
    result, _code = $server.run(command, check_errors: false)
    break if result.include? pkg
    sleep 1
  end
end

When(/^I wait until package "([^"]*)" is removed from "([^"]*)" via spacecmd$/) do |pkg, client|
  node = get_system_name(client)
  $server.run("spacecmd -u admin -p admin clear_caches")
  command = "spacecmd -u admin -p admin system_listinstalledpackages #{node}"
  repeat_until_timeout(timeout: 600, message: "package #{pkg} is still present") do
    result, code = $server.run(command, check_errors: false)
    sleep 1
    break unless result.include? pkg
  end
end

Then(/^the "([^"]*)" on "([^"]*)" grains does not exist$/) do |key, client|
  node = get_target(client)
  _result, code = node.run("grep #{key} /etc/salt/minion.d/susemanager.conf", check_errors: false)
  raise if code.zero?
end

When(/^I (enable|disable) the necessary repositories before installing Prometheus exporters on this "([^"]*)"((?: without error control)?)$/) do |action, host, error_control|
  common_repos = 'os_pool_repo os_update_repo tools_pool_repo tools_update_repo'
  step %(I #{action} the repositories "#{common_repos}" on this "#{host}"#{error_control})
  node = get_target(host)
  _os_version, os_family = get_os_version(node)
  if os_family =~ /^opensuse/ || os_family =~ /^sles/
    step %(I #{action} repository "tools_additional_repo" on this "#{host}"#{error_control}) unless $product == 'Uyuni'
  end
end

When(/^I apply "([^"]*)" local salt state on "([^"]*)"$/) do |state, host|
  node = get_target(host)
  source = File.dirname(__FILE__) + '/../upload_files/salt/' + state + '.sls'
  remote_file = '/usr/share/susemanager/salt/' + state + '.sls'
  return_code = file_inject(node, source, remote_file)
  raise 'File injection failed' unless return_code.zero?
  node.run('salt-call --local --file-root=/usr/share/susemanager/salt --module-dirs=/usr/share/susemanager/salt/ --log-level=info --retcode-passthrough state.apply ' + state)
end

When(/^I copy autoinstall mocked files on server$/) do
  target_dirs = "/var/autoinstall/Fedora_12_i386/images/pxeboot /var/autoinstall/SLES15-SP2-x86_64/DVD1/boot/x86_64/loader /var/autoinstall/mock"
  $server.run("mkdir -p #{target_dirs}")
  base_dir = File.dirname(__FILE__) + "/../upload_files/autoinstall/cobbler/"
  source_dir = "/var/autoinstall/"
  return_codes = []
  return_codes << file_inject($server, base_dir + 'fedora12/vmlinuz', source_dir + 'Fedora_12_i386/images/pxeboot/vmlinuz')
  return_codes << file_inject($server, base_dir + 'fedora12/initrd.img', source_dir + 'Fedora_12_i386/images/pxeboot/initrd.img')
  return_codes << file_inject($server, base_dir + 'mock/empty.xml', source_dir + 'mock/empty.xml')
  return_codes << file_inject($server, base_dir + 'sles15sp2/initrd', source_dir + 'SLES15-SP2-x86_64/DVD1/boot/x86_64/loader/initrd')
  return_codes << file_inject($server, base_dir + 'sles15sp2/linux', source_dir + 'SLES15-SP2-x86_64/DVD1/boot/x86_64/loader/linux')
  raise 'File injection failed' unless return_codes.all?(&:zero?)
end

When(/^I copy unset package file on server$/) do
  base_dir = File.dirname(__FILE__) + "/../upload_files/unset_package/"
  return_code = file_inject($server, base_dir + 'subscription-tools-1.0-0.noarch.rpm', '/root/subscription-tools-1.0-0.noarch.rpm')
  raise 'File injection failed' unless return_code.zero?
end

And(/^I copy vCenter configuration file on server$/) do
  base_dir = File.dirname(__FILE__) + "/../upload_files/virtualization/"
  return_code = file_inject($server, base_dir + 'vCenter.json', '/var/tmp/vCenter.json')
  raise 'File injection failed' unless return_code.zero?
end

When(/^I regenerate the boot RAM disk on "([^"]*)" if necessary$/) do |host|
  node = get_target(host)
  os_version, os_family = get_os_version(node)
  # HACK: initrd is not regenerated after patching SLES 11 kernel
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run('mkinitrd')
  end
end
