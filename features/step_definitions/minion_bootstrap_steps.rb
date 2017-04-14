# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

And(/^I enter the hostname of "([^"]*)" as hostname$/) do |minion|
  case minion
  when "sle-minion"
    step %(I enter "#{$minion_fullhostname}" as "hostname")
  when "ceos-minion"
    step %(I enter "#{$ceos_minion_fullhostname}" as "hostname")
  when "sle-migrated-minion"
    step %(I enter "#{$client_fullhostname}" as "hostname")
  else
    raise "No valid target."
  end
end

Given(/^the salt-master can reach "([^"]*)"$/) do |minion|
  begin
    # where it realizes the connection is stuck
    Timeout.timeout(DEFAULT_TIMEOUT + 300) do
      # only try 3 times
      3.times do
        if minion == "sle-minion"
          out, _code = $server.run("salt #{$minion_fullhostname} test.ping")
          break if out.include?($minion_fullhostname)
        elsif minion == "ceos-minion"
          out, _code = $server.run("salt #{$ceos_minion_fullhostname} test.ping")
          break if out.include?($ceos_minion_fullhostname)
        end
        sleep(1)
      end
    end
  rescue Timeout::Error
    fail "Master can not communicate with the minion: #{out}"
  end
end

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

Then(/^I run spacecmd listevents for sle-minion$/) do
  $server.run("spacecmd -u admin -p admin clear_caches")
  $server.run("spacecmd -u admin -p admin system_listevents #{$minion_fullhostname}")
end

And(/^I cleanup minion: "([^"]*)"$/) do |target|
  if target == "sle-minion"
    $minion.run("systemctl stop salt-minion")
    $minion.run("rm -Rf /var/cache/salt/minion")
  elsif target == "ceos-minion"
    $ceos_minion.run("systemctl stop salt-minion")
    $ceos_minion.run("rm -Rf /var/cache/salt/minion")
   end
end
