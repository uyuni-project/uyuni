# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

And(/^I enter the hostname of "([^"]*)" as hostname$/) do |minion|
  if minion == "sle-minion"
    step %(I enter "#{$minion_fullhostname}" as "hostname")

  elsif minion == "ceos-minion"
    step %(I enter "#{$ceos_minion_fullhostname}" as "hostname")
  else
    raise "no valid name of minion given! "
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

And(/^I remove pkg "([^"]*)" on minion$/) do |pkg|
  $minion.run("zypper -n rm #{pkg}")
end

Then(/^I run spacecmd listevents for sle-minion$/) do
  $server.run("spacecmd -u admin -p admin clear_caches")
  $server.run("spacecmd -u admin -p admin system_listevents #{$minion_fullhostname}")
end
