# Copyright (c) 2014-2018 SUSE
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'
require 'timeout'

Then(/^"([^"]*)" should be installed on "([^"]*)"$/) do |package, host|
  node = get_target(host)
  node.run("rpm -q #{package}")
end

Then(/^"([^"]*)" should not be installed on "([^"]*)"$/) do |package, host|
  node = get_target(host)
  node.run("rpm -q #{package}; test $? -ne 0")
end

When(/^I query latest Salt changes on "(.*?)"$/) do |host|
  node = get_target(host)
  result, return_code = node.run("rpm -q --changelog salt")
  result.split("\n")[0, 15].each do |line|
    puts line.scrub
  end
end

When(/^I apply highstate on "(.*?)"$/) do |minion|
  node = get_target(minion)
  if minion == 'sle-minion'
    cmd = 'salt'
    extra_cmd = ''
  elsif minion == 'ssh-minion' or minion == 'ceos-minion'
    cmd = 'salt-ssh'
    extra_cmd = '-i --roster-file=/tmp/roster_tests -w -W'
    $server.run("printf '#{node.full_hostname}:\n  host: #{node.full_hostname}\n  user: root\n  passwd: linux\n' > /tmp/roster_tests")
  else
    raise 'Invalid target'
  end
  $server.run_until_ok("#{cmd} #{node.full_hostname} state.highstate #{extra_cmd}")
end

Then(/^I wait until "([^"]*)" service is up and running on "([^"]*)"$/) do |service, target|
  cmd = "systemctl is-active #{service}"
  node = get_target(target)
  node.run_until_ok(cmd)
end

When(/^I enable product "([^"]*)"$/) do |prd|
    list_output = sshcmd("mgr-sync list products", ignore_err: true)[:stdout]
    executed = false
    linenum = 0
    list_output.each_line do |line|
        if not /^ *\[ \]/.match(line)
            next
        end
        linenum += 1
        if line.include? prd
            executed = true
            $command_output = sshcmd("echo '#{linenum}' | mgr-sync add product", ignore_err: true)[:stdout]
            break
        end
    end
    raise "#{$command_output}" unless executed
end

When(/^I enable product "([^"]*)" without recommended$/) do |prd|
    list_output = sshcmd("mgr-sync list products", ignore_err: true)[:stdout]
    executed = false
    linenum = 0
    list_output.each_line do |line|
        if not /^ *\[ \]/.match(line)
            next
        end
        linenum += 1
        if line.include? prd
            executed = true
            $command_output = sshcmd("echo '#{linenum}' | mgr-sync add product --no-recommends", ignore_err: true)[:stdout]
            break
        end
    end
    raise "#{$command_output}" unless executed
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

When(/^I refresh SCC$/) do
  refresh_timeout = 600
  $server.run('echo -e "admin\nadmin\n" | mgr-sync refresh', true, refresh_timeout)
end

When(/^I execute mgr\-sync refresh$/) do
  $command_output = sshcmd('mgr-sync refresh', ignore_err: true)[:stderr]
end

When(/^I execute mgr\-bootstrap "([^"]*)"$/) do |arg1|
  arch = 'x86_64'
  $command_output = sshcmd("mgr-bootstrap --activation-keys=1-SUSE-PKG-#{arch} #{arg1}")[:stdout]
end

When(/^I fetch "([^"]*)" to "([^"]*)"$/) do |file, target|
  node = get_target(target)
  node.run("wget http://#{$server.ip}/#{file}")
end

When(/^I wait until file "([^"]*)" contains "([^"]*)" on server$/) do |file, content|
  sleep(3)
  output = {}
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        output = sshcmd("grep #{content} #{file}", ignore_err: true)
        break if output[:stdout] =~ /#{content}/
        sleep(2)
      end
    end
  rescue Timeout::Error
    $stderr.write("-----\n#{output[:stderr]}\n-----\n")
    raise "#{content} not found in file #{file}"
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
  $server.run('spacewalk-service restart')
  sleep(5)
end

Then(/^I shutdown the spacewalk service$/) do
  $server.run('spacewalk-service stop')
end

Then(/^I execute spacewalk-debug on the server$/) do
  $server.run('spacewalk-debug')
  step %(I copy "/tmp/spacewalk-debug.tar.bz2" from "server")
end

When(/^I copy "([^"]*)" from "([^"]*)"$/) do |file, target|
  node = get_target(target)
  return_code = file_extract(node, file, File.basename(file))
  raise 'File extraction failed' unless return_code.zero?
end

When(/^I copy "([^"]*)" to "([^"]*)"$/) do |file, target|
  node = get_target(target)
  return_code = file_inject(node, file, File.basename(file))
  raise 'File injection failed' unless return_code.zero?
end

Then(/^the PXE default profile should be enabled$/) do
  sleep(1)
  step %(I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT pxe-default-profile" on server)
end

Then(/^the PXE default profile should be disabled$/) do
  sleep(1)
  step %(I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT local" on server)
end

When(/^the server starts mocking an IPMI host$/) do
  ["ipmisim1.emu", "lan.conf", "fake_ipmi_host.sh"].each do |file|
    source = File.dirname(__FILE__) + '/../upload_files/' + file
    dest = "/etc/ipmi/" + file
    return_code = file_inject($server, source, dest)
    raise 'File injection failed' unless return_code.zero?
  end
  $server.run("chmod +x /etc/ipmi/fake_ipmi_host.sh")
  $server.run("ipmi_sim -n < /dev/null > /dev/null &")
end

When(/^the server stops mocking an IPMI host$/) do
  $server.run("kill $(pidof ipmi_sim)")
  $server.run("kill $(pidof -x fake_ipmi_host.sh)")
end

When(/^I install a user-defined state for "([^"]*)" on the server$/) do |host|
  node = get_target(host)
  # copy state file to server
  file = 'user_defined_state.sls'
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = "/srv/salt/" + file
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  # generate top file and copy it to server
  script = "base:\n" \
           "  '#{node.full_hostname}':\n" \
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
  $command_output, _code = $client.run(command)
end

When(/^spacewalk\-channel fails with "([^"]*)"$/) do |arg1|
  command = "spacewalk-channel #{arg1}"
  # we are checking that the cmd should fail here
  $command_output, code = $client.run(command, false)
  raise "#{command} should fail, but hasn't" if code.zero?
end

Then(/^I should get "([^"]*)"$/) do |arg1|
  found = false
  $command_output.each_line do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  raise "'#{arg1}' not found in output '#{$command_output}'" unless found
end

Then(/^I shouldn't get "([^"]*)"$/) do |arg1|
  found = false
  $command_output.each_line do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  raise "'#{arg1}' found in output '#{$command_output}'" if found
end

$space = 'spacecmd -u admin -p admin '
Then(/^I wait until mgr-sync refresh is finished$/) do
  # mgr-sync refresh is a slow operation, we don't use the default timeout
  cmd = "#{$space} api sync.content.listProducts | grep ''"
  refresh_timeout = 600
  begin
    Timeout.timeout(refresh_timeout) do
      loop do
        result, code = $server.run(cmd, false)
        break if code.zero?
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "'mgr-sync refresh' did not finish in time"
  end
end

Then(/^I should see "(.*?)" in the output$/) do |arg1|
  assert_includes(@command_output, arg1)
end

Then(/^service "([^"]*)" is enabled on the server$/) do |service|
  output = sshcmd("systemctl is-enabled '#{service}'", ignore_err: true)[:stdout]
  output.chomp!
  raise if output != 'enabled'
end

Then(/^service "([^"]*)" is running on the server$/) do |service|
  output = sshcmd("systemctl is-active '#{service}'", ignore_err: true)[:stdout]
  output.chomp!
  raise if output != 'active'
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

When(/^I wait until file "(.*)" exists on "(.*)"$/) do |file, host|
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

When(/^I wait until file "(.*)" exists on server$/) do |file|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if file_exists?($server, file)
        sleep(1)
      end
    end
  rescue Timeout::Error
    raise unless file_exists?($server, file)
  end
end

Then(/^I wait and check that "([^"]*)" has rebooted$/) do |target|
  reboot_timeout = 800
  node = get_target(target)
  check_shutdown(node.full_hostname, reboot_timeout)
  check_restart(node.full_hostname, get_target(target), reboot_timeout)
end

When(/^I call spacewalk\-repo\-sync for channel "(.*?)" with a custom url "(.*?)"$/) do |arg1, arg2|
  @command_output = sshcmd("spacewalk-repo-sync -c #{arg1} -u #{arg2}")[:stdout]
end

When(/^I disable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run('sysctl net.ipv6.conf.all.forwarding=0')
end

When(/^I enable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run('sysctl net.ipv6.conf.all.forwarding=1')
end

And(/^I register "([^*]*)" as traditional client$/) do |client|
  node = get_target(client)
  command = 'wget --no-check-certificate ' \
            '-O /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT ' \
            "http://#{$server.ip}/pub/RHN-ORG-TRUSTED-SSL-CERT"
  node.run(command)
  command = 'rhnreg_ks --username=admin --password=admin --force ' \
            "--serverUrl=#{registration_url} " \
            '--sslCACert=/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT ' \
            '--activationkey=1-SUSE-DEV-x86_64'
  node.run(command)
end

When(/^I wait for the openSCAP audit to finish$/) do
  host = $server.full_hostname
  @sle_id = retrieve_server_id($minion.full_hostname)
  @cli = XMLRPC::Client.new2('http://' + host + '/rpc/api')
  @sid = @cli.call('auth.login', 'admin', 'admin')
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        scans = @cli.call('system.scap.list_xccdf_scans', @sid, @sle_id)
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

And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  host = get_target(target)
  cmd = "#{$space} system_listevents #{host.full_hostname} | head -n5"
  $server.run("#{$space} clear_caches")
  out, _code = $server.run(cmd)
  raise "#{out} should contain #{status}" unless out.include? status
end

And(/I create dockerized minions$/) do
  master, _code = $minion.run('cat /etc/salt/minion.d/susemanager.conf')
  # build everything
  distros = %w[rhel6 rhel7 sles11sp4 sles12 sles12sp1]
  docker_timeout = 2000
  distros.each do |os|
    $minion.run("docker build https://gitlab.suse.de/galaxy/suse-manager-containers.git#master:minion-fabric/#{os}/ -t #{os}", true, docker_timeout)
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
  # create backups of /etc/hosts and up2date config
  $server.run('cp /etc/hosts /etc/hosts.BACKUP')
  $server.run('cp /etc/sysconfig/rhn/up2date /etc/sysconfig/rhn/up2date.BACKUP')
  # generate expect file
  bootstrap = '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'
  script = "spawn spacewalk-ssh-push-init --client #{$client.ip} --register #{bootstrap} --tunnel\n" \
           "while {1} {\n" \
           "  expect {\n" \
           "    eof                                                        {break}\n" \
           "    \"Are you sure you want to continue connecting (yes/no)?\" {send \"yes\r\"}\n" \
           "    \"Password:\"                                              {send \"linux\r\"}\n" \
           "  }\n" \
           "}\n"
  path = generate_temp_file('push-registration.expect', script)
  step 'I copy "' + path + '" to "server"'
  `rm #{path}`
  # perform the registration
  filename = File.basename(path)
  bootstrap_timeout = 600
  $server.run("expect #{filename}", true, bootstrap_timeout, 'root')
  # restore files from backups
  $server.run('mv /etc/hosts.BACKUP /etc/hosts')
  $server.run('mv /etc/sysconfig/rhn/up2date.BACKUP /etc/sysconfig/rhn/up2date')
end
# zypper

When(/^I remove package "([^"]*)" from this "(.*?)"$/) do |package, host|
  node = get_target(host)
  if file_exists?(node, '/usr/bin/zypper')
    cmd = "zypper --non-interactive remove -y #{package}"
  elsif file_exists?(node, '/usr/bin/yum')
    cmd = "yum -y remove #{package}"
  else
    raise 'not found: zypper or yum'
  end
  node.run(cmd)
end

Given(/^I enable repository "(.*?)" on this "(.*?)"$/) do |repo, host|
  node = get_target(host)
  node.run("zypper mr --enable #{repo}")
end

Then(/^I disable repository "(.*?)" on this "(.*?)"$/) do |repo, host|
  node = get_target(host)
  node.run("zypper mr --disable #{repo}")
end

When(/^I install package "(.*?)" on this "(.*?)"$/) do |package, host|
  node = get_target(host)
  if file_exists?(node, '/usr/bin/zypper')
    cmd = "zypper --non-interactive install -y #{package}"
  elsif file_exists?(node, '/usr/bin/yum')
    cmd = "yum -y install #{package}"
  else
    raise 'not found: zypper or yum'
  end
  node.run(cmd)
end

When(/^I wait until the package "(.*?)" has been cached on this "(.*?)"$/) do |pkg_name, host|
  node = get_target(host)
  cmd = "ls /var/cache/zypp/packages/susemanager:test-channel-x86_64/getPackage/#{pkg_name}.rpm"
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        result, return_code = node.run(cmd, false)
        break if return_code.zero?
        sleep 2
      end
    end
  rescue Timeout::Error
    raise "Package #{pkg_name} was not cached after #{DEFAULT_TIMEOUT} seconds"
  end
end

And(/^I create the "([^"]*)" bootstrap-repo for "([^"]*)" on the server$/) do |arch, target|
  node = get_target(target)
  os_version = get_os_version(node)
  sle11 = "#{os_version[0, 2]}-SP#{os_version[-1]}"
  cmd = "mgr-create-bootstrap-repo -c SLE-#{os_version}-#{arch}" if os_version.include? '12'
  cmd = "mgr-create-bootstrap-repo -c SLE-#{sle11}-#{arch}" if os_version.include? '11'
  puts 'Creating the boostrap repository on the server: ' + cmd
  $server.run(cmd, false)
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
  settings = "RHN_PARENT=#{$server.ip}\n" \
             "HTTP_PROXY=''\n" \
             "VERSION=''\n" \
             "TRACEBACK_EMAIL=galaxy-noise@suse.de\n" \
             "USE_SSL=y\n" \
             "USE_EXISTING_CERTS=n\n" \
             "INSTALL_MONITORING=n\n" \
             "SSL_PASSWORD=spacewalk\n" \
             "SSL_ORG=SUSE\n" \
             "SSL_ORGUNIT=SUSE\n" \
             "SSL_COMMON=#{$proxy.ip}\n" \
             "SSL_CITY=Nuremberg\n" \
             "SSL_STATE=Bayern\n" \
             "SSL_COUNTRY=DE\n" \
             "SSL_EMAIL=galaxy-noise@suse.de\n" \
             "SSL_CNAME_ASK=''\n" \
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
  $proxy.run(cmd, true, proxy_timeout, 'root')
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
