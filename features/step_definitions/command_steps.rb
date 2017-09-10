# Copyright (c) 2014-16 SUSE # Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'timeout'

Then(/^I apply highstate on "(.*?)"$/) do |minion|
  node = get_target(minion)
  cmd = "salt '#{node.full_hostname}' state.highstate"
  $server.run_until_ok(cmd)
end

Then(/^I wait until "([^"]*)" service is up and running on "([^"]*)"$/) do |service, target|
  cmd = "systemctl is-active #{service}"
  node = get_target(target)
  node.run_until_ok(cmd)
end

When(/^I execute mgr\-sync "([^"]*)" with user "([^"]*)" and password "([^"]*)"$/) do |arg1, u, p|
  $command_output = sshcmd("echo -e '#{u}\n#{p}\n' | mgr-sync #{arg1}", ignore_err: true)[:stdout]
end

When(/^I execute mgr\-sync "([^"]*)"$/) do |arg1|
  $command_output = sshcmd("mgr-sync #{arg1}")[:stdout]
end

When(/^I remove the mgr\-sync cache file$/) do
  $command_output = sshcmd('rm -f ~/.mgr-sync')[:stdout]
end

When(/^I execute mgr\-sync refresh$/) do
  $command_output = sshcmd('mgr-sync refresh', ignore_err: true)[:stderr]
end

When(/^I execute mgr\-bootstrap "([^"]*)"$/) do |arg1|
  arch = 'x86_64'
  $command_output = sshcmd("mgr-bootstrap --activation-keys=1-SUSE-PKG-#{arch} #{arg1}")[:stdout]
end

When(/^I fetch "([^"]*)" from server$/) do |arg1|
  $client.run("wget http://#{$server_ip}/#{arg1}", true, 500, 'root')
end

When(/^file "([^"]*)" exists on server$/) do |arg1|
  $server.run("test -f #{arg1}")
end

When(/^file "([^"]*)" not exists on server$/) do |arg1|
  $server.run("test -f #{arg1}")
end

When(/^file "([^"]*)" contains "([^"]*)"$/) do |arg1, arg2|
  sleep(3)
  output = {}
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        output = sshcmd("grep #{arg2} #{arg1}", ignore_err: true)
        break if output[:stdout] =~ /#{arg2}/
        sleep(2)
      end
    end
  rescue Timeout::Error
    $stderr.write("-----\n#{output[:stderr]}\n-----\n")
    raise "#{arg2} not found in File #{arg1}"
  end
end

When(/^I check the tomcat logs for errors$/) do
  output = $server.run('cat /var/log/tomcat/*')
  msgs = %w[ERROR NullPointer]
  msgs.each do |msg|
    raise "-#{msg}-  msg found on tomcat logs" if output.include? msg
  end
end

Then(/^I restart the spacewalk service$/) do
  sshcmd('spacewalk-service restart')
  sleep(5)
end

Then(/^I shutdown the spacewalk service$/) do
  $server.run('spacewalk-service stop')
end

Then(/^I execute spacewalk-debug on the server$/) do
  $server.run('spacewalk-debug')
  step %(I copy "/tmp/spacewalk-debug.tar.bz2")
end

When(/^I copy "([^"]*)"$/) do |arg1|
  user = 'root@'
  $command_output = `echo | scp -o StrictHostKeyChecking=no #{user}$TESTHOST:#{arg1} . 2>&1`
  raise "Execute command failed: #{$ERROR_INFO}: #{$command_output}" unless $CHILD_STATUS.success?
end

When(/^I copy to server "([^"]*)"$/) do |arg1|
  user = 'root@'
  $command_output = `echo | scp -o StrictHostKeyChecking=no #{arg1} #{user}$TESTHOST: 2>&1`
  raise "Execute command failed: #{$ERROR_INFO}: #{$command_output}" unless $CHILD_STATUS.success?
end

Then(/^the pxe-default-profile should be enabled$/) do
  sleep(1)
  step %(file "/srv/tftpboot/pxelinux.cfg/default" contains "'ONTIMEOUT pxe-default-profile'")
end

Then(/^the pxe-default-profile should be disabled$/) do
  sleep(1)
  step %(file "/srv/tftpboot/pxelinux.cfg/default" contains "'ONTIMEOUT local'")
end

Then(/^the cobbler report contains "([^"]*)"$/) do |arg1|
  output = sshcmd("cobbler system report --name #{$client.full_hostname}:1", ignore_err: true)[:stdout]
  raise "Not found: #{output}" unless output.include?(arg1)
end

Then(/^I clean the search index on the server$/) do
  output = sshcmd('/usr/sbin/rcrhn-search cleanindex', ignore_err: true)
  raise if output[:stdout].include?('ERROR')
end

When(/^I execute spacewalk\-channel and pass "([^"]*)"$/) do |arg1|
  command = "spacewalk-channel #{arg1}"
  $command_output, _code = $client.run(command, true, 500, 'root')
end

When(/^spacewalk\-channel fails with "([^"]*)"$/) do |arg1|
  command = "spacewalk-channel #{arg1}"
  # we are checking that the cmd should fail here
  $command_output, code = $client.run(command, false, 500, 'root')
  raise "#{command} should fail, but hasn't" if code.zero?
end

Then(/^I want to get "([^"]*)"$/) do |arg1|
  found = false
  $command_output.each_line do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  raise "'#{arg1}' not found in output '#{$command_output}'" unless found
end

Then(/^I wont get "([^"]*)"$/) do |arg1|
  found = false
  $command_output.each_line do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  raise "'#{arg1}' found in output '#{$command_output}'" if found
end

Then(/^I wait for mgr-sync refresh is finished$/) do
  $server.run_until_ok('ls /var/lib/spacewalk/scc/scc-data/*organizations_orders.json')
end

Then(/^I should see "(.*?)" in the output$/) do |arg1|
  assert_includes(@command_output, arg1)
end

Then(/^Service "([^"]*)" is enabled on the Server$/) do |service|
  output = sshcmd("systemctl is-enabled '#{service}'", ignore_err: true)[:stdout]
  output.chomp!
  raise if output != 'enabled'
end

Then(/^Service "([^"]*)" is running on the Server$/) do |service|
  output = sshcmd("systemctl is-active '#{service}'", ignore_err: true)[:stdout]
  output.chomp!
  raise if output != 'active'
end

# snapshots
When(/^I take a snapshot "([^"]*)"$/) do |name|
  $sshout = ''
  $sshout = `echo | ssh -o StrictHostKeyChecking=no root@$VHOST qemu-img snapshot -c #{name} $IMGDIR/$VMDISK.qcow2`
  puts 'Creating snapsnot failed...' unless $CHILD_STATUS.success?
end

When(/^I run "([^"]*)" on "([^"]*)"$/) do |cmd, host|
  node = get_target(host)
  node.run(cmd)
end

When(/^I run "([^"]*)" on "([^"]*)" without error control$/) do |cmd, host|
  node = get_target(host)
  _out, $fail_code = node.run(cmd, false)
end

Then(/^the command should fail$/) do
  raise 'Previous command must fail, but has NOT failed!' if $fail_code.zero?
end

When(/^"(.*)" exists on the filesystem of "(.*)"$/) do |file, host|
  node = get_target(host)
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if file_exists?(node, file)
        sleep(1)
      end
    end
  rescue Timeout::Error
    raise unless file_exists?(node, file)
  end
end

Then(/^I remove "(.*)" from "(.*)"$/) do |file, host|
  node = get_target(host)
  file_delete(node, file)
end

Then(/^I wait and check that "([^"]*)" has rebooted$/) do |target|
  timeout = 800
  node = get_target(target)
  checkShutdown(node.full_hostname, timeout)
  checkRestart(node.full_hostname, get_target(target), timeout)
end

When(/^I call spacewalk\-repo\-sync for channel "(.*?)" with a custom url "(.*?)"$/) do |arg1, arg2|
  @command_output = sshcmd("spacewalk-repo-sync -c #{arg1} -u #{arg2}")[:stdout]
end

When(/^I click on "([^"]+)" for "([^"]+)"$/) do |arg1, arg2|
  within(:xpath, '//section') do
    within(:xpath, "//table/tbody/tr[.//a[contains(.,'#{arg2}')]]") do
      find_link(arg1).click
    end
  end
end

When(/^I disable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run('sysctl net.ipv6.conf.all.forwarding=0')
end

When(/^I enable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run('sysctl net.ipv6.conf.all.forwarding=1')
end

And(/^I register the centos7 as tradclient$/) do
  cert_path = '/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT'
  wget = 'wget --no-check-certificate -O'
  register = 'rhnreg_ks --username=admin --password=admin --force \\' \
              "--serverUrl=https://#{$server_ip}/XMLRPC \\" \
              '--sslCACert=/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT \\' \
              '--activationkey=1-MINION-TEST'

  $ceos_minion.run("#{wget} #{cert_path} http://#{$server_ip}/pub/RHN-ORG-TRUSTED-SSL-CERT", true, 500)
  $ceos_minion.run(register)
end

When(/^I wait for the openSCAP audit to finish$/) do
  host = $server.full_hostname
  @sle_id = retrieve_server_id($minion.full_hostname)
  @cli = XMLRPC::Client.new2('http://' + host + '/rpc/api')
  @sid = @cli.call('auth.login', 'admin', 'admin')
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        scans = @cli.call('system.scap.listXccdfScans', @sid, @sle_id)
        # in the openscap test, we schedule 2 scans
        if scans.length > 1
          @cli.call('auth.logout', @sid)
          break
        end
      end
    end
  rescue Timeout::Error
    @cli.call('auth.logout', @sid)
    raise 'process did not stop after several tries'
  end
end

$space = 'spacecmd -u admin -p admin '
And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  host = $ssh_minion.full_hostname if target == 'ssh-minion'
  host = $ceos_minion.full_hostname if target == 'ceos-minion'
  cmd = "#{$space} system_listevents #{host} | head -n5"
  $server.run("#{$space} clear_caches")
  out, _code = $server.run(cmd)
  raise "#{out} should contain #{status}" unless out.include? status
end

And(/I create dockerized minions$/) do
  master, _code = $minion.run('cat /etc/salt/minion.d/susemanager.conf')
  # build everything
  distros = %w[rhel6 rhel7 sles11sp4 sles12 sles12sp1]
  distros.each do |os|
    $minion.run("docker build https://gitlab.suse.de/galaxy/suse-manager-containers.git#master:minion-fabric/#{os}/ -t #{os}", true, 2000)
    spawn_minion = '/etc/salt/minion; dbus-uuidgen > /etc/machine-id; salt-minion -l trace'
    $minion.run("docker run -h #{os} -d --entrypoint '/bin/sh' #{os} -c \"echo \'#{master}\' > #{spawn_minion}\"")
    puts "minion #{os} created and running"
  end
  # accept all the key on master, wait dinimically for last key
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        out, _code = $server.run('salt-key -l unaccepted')
        # if we see the last os, we can break
        if out.include? distros.last
          $server.run('salt-key -A -y')
          break
        end
        sleep 5
      end
    end
  rescue Timeout::Error
    raise 'something wrong with creation of minion docker'
  end
end

When(/^I register this client for SSH push via tunnel$/) do
  # Create backups of /etc/hosts and up2date config
  $server.run('cp /etc/hosts /etc/hosts.BACKUP')
  $server.run('cp /etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP')
  # Generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  expect_file = ExpectFileGenerator.new($client_ip, bootstrap)
  step 'I copy to server "' + expect_file.path + '"'
  filename = expect_file.filename
  # Perform the registration
  command = "expect #{filename}"
  $server.run(command, true, 600, 'root')
  # Restore files from backups
  $server.run('mv /etc/hosts.BACKUP /etc/hosts')
  $server.run('mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date')
end
# zypper

And(/^I remove pkg "([^"]*)" on this "(.*?)"$/) do |pkg, host|
  node = get_target(host)
  node.run("zypper -n rm #{pkg}")
end

Given(/^I enable repository "(.*?)" on this "(.*?)"$/) do |repo, host|
  node = get_target(host)
  node.run("zypper mr --enable #{repo}")
end

Then(/^I disable repository "(.*?)" on this "(.*?)"$/) do |repo, host|
  node = get_target(host)
  node.run("zypper mr --disable #{repo}")
end

Then(/^I install pkg "(.*?)" on this "(.*?)"$/) do |pkg, host|
  node = get_target(host)
  node.run("zypper in -y #{pkg}")
end

And(/^I wait until the package "(.*?)" has been cached on this "(.*?)"$/) do |pkg_name, host|
  node = get_target(host)
  cmd = "ls /var/cache/zypp/packages/susemanager:test-channel-x86_64/getPackage/#{pkg_name}.rpm"
  node.run_until_ok(cmd)
end

And(/^I create the "([^"]*)" bootstrap-repo for "([^"]*)" on the server$/) do |arch, target|
  node = get_target(target)
  os_version = get_os_version(node)
  sle11 = "#{os_version[0, 2]}-SP#{os_version[-1]}"
  cmd = "mgr-create-bootstrap-repo -c SLE-#{os_version}-#{arch}" if os_version.include? '12'
  cmd = "mgr-create-bootstrap-repo -c SLE-#{sle11}-#{arch}" if os_version.include? '11'
  puts 'Creating the boostrap-repo on the server: ' + cmd
  $server.run(cmd, false)
end
