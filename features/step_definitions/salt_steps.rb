# Copyright 2015 SUSE LLC
require 'timeout'

Given(/^the Salt Minion is configured$/) do
  # cleanup the key in case the image was reused
  # to run the test twice
  key = '/etc/salt/pki/minion/minion_master.pub'
  if File.exist?(key)
    File.delete(key)
    puts "Key #{key} has been removed"
  end
  File.write('/etc/salt/minion.d/master.conf', "master: #{ENV['TESTHOST']}\n")
  step %[I restart salt-minion]
end

Given(/^that the master can reach this client$/) do
  begin
    start = Time.now
    # 300 is the default 1st keepalive interval for the minion
    # where it realizes the connection is stuck
    Timeout.timeout(DEFAULT_TIMEOUT + 300) do
      # only try 3 times
      3.times do
        @output = sshcmd("salt #{$myhostname} test.ping", ignore_err: true)
        if @output[:stdout].include?($myhostname) &&
           @output[:stdout].include?('True')
          finished = Time.now
          puts "Took #{finished.to_i - start.to_i} seconds to contact the minion"
          break
        end
        sleep(1)
      end
    end
  rescue Timeout::Error
      fail "Master can not communicate with the minion: #{@output[:stdout]}"
  end
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  @output = sshcmd("cat #{filename}")
end

When(/^I restart salt-minion$/) do
  system("systemctl restart salt-minion")
end

Then(/^the Salt Minion should be running$/) do
  out = ""
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        out = `systemctl status salt-minion`
        break if $?.success?
        sleep(1)
      end
    end
  rescue Timeout::Error
    fail "salt-minion status: #{out}"
  end
end

When(/^I list unaccepted keys at Salt Master$/) do
  @action = lambda do
    return sshcmd("salt-key --list unaccepted")
  end
end

When(/^I list accepted keys at Salt Master$/) do
  @action = lambda do
    return sshcmd("salt-key --list accepted")
  end
end

When(/^I list rejected keys at Salt Master$/) do
  @action = lambda do
    return sshcmd("salt-key --list rejected")
  end
end

Then(/^the list of the keys should contain this client's hostname$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        @output = @action.call
        break if @output[:stdout].include?($myhostname)
        sleep(1)
      end
    end
  rescue Timeout::Error
    puts "timeout waiting for the key to appear"
  end
  assert_match(/#{$myhostname}/, @output[:stdout], "#{$myhostname} is not listed in the key list")
end

Given(/^this minion key is unaccepted$/) do
  steps %{
    Then I delete this minion key in the Salt master
    And I restart salt-minion
    And we wait till Salt master sees this minion as unaccepted
  }
end

When(/^we wait till Salt master sees this minion as unaccepted$/) do
  steps %{
    When I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
  }
end

Given(/^this minion key is accepted$/) do
  steps %{
    Then I accept this minion key in the Salt master
    And we wait till Salt master sees this minion as accepted
  }
end

When(/^we wait till Salt master sees this minion as accepted$/) do
  steps %{
    When I list accepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
  }
end

Given(/^this minion key is rejected$/) do
  steps %{
    Then I reject this minion key in the Salt master
    And we wait till Salt master sees this minion as rejected
  }
end

When(/^we wait till Salt master sees this minion as rejected$/) do
  steps %{
    When I list rejected keys at Salt Master
    Then the list of the keys should contain this client's hostname
  }
end

When(/^I delete this minion key in the Salt master$/) do
  # workaround https://github.com/saltstack/salt/issues/27796
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        sshcmd("stat /var/cache/salt/master/.dfn")
        puts "Waiting for /var/cache/salt/master/.dfn to go away (issue#27796)"
        sleep(1)
      end
    end
  rescue RuntimeError => e
    puts ".dfn should be gone"
  rescue Timeout::Error => e
    puts "timeout witing for .dfn to vanish"
  end
  sshcmd("salt-key -y -d #{$myhostname}")
end

When(/^I accept this minion key in the Salt master$/) do
  sshcmd("salt-key -y --accept=#{$myhostname}")
end

When(/^I reject this minion key in the Salt master$/) do
  sshcmd("salt-key -y --reject=#{$myhostname}")
end

When(/^I delete all keys in the Salt master$/) do
  sshcmd("salt-key -y -D")
end

When(/^I accept all Salt unaccepted keys$/) do
  sshcmd("salt-key -y -A")
end

When(/^I get OS information of the Minion from the Master$/) do
  @output = sshcmd("salt #{$myhostname} grains.get osfullname")
end

Then(/^it should contain a "(.*?)" text$/) do |content|
  assert_match(/#{content}/, @output[:stdout])
end

Then(/^salt\-api should be listening on local port (\d+)$/) do |port|
  output = sshcmd("ss -nta | grep #{port}")
  assert_match(/127.0.0.1:#{port}/, output[:stdout])
end

Then(/^salt\-master should be listening on public port (\d+)$/) do |port|
  output = sshcmd("ss -nta | grep #{port}")
  assert_match(/\*:#{port}/, output[:stdout])
end

And(/^this minion is not registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new($myhostname)
  @rpc.login('admin', 'admin')
  @rpc.deleteSystem($myhostname)
  refute_includes(@rpc.listSystems.map {|s| s['id']}, $myhostname)
end
