# Copyright (c) 2016 Novell, Inc.
# Licensed under the terms of the MIT license.

$space = "spacecmd -u admin -p admin "
And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  host = $ssh_minion_fullhostname if target == "ssh-minion"
  host = $ceos_minion_fullhostname if target == "ceos-minion"
  cmd = "#{$space} system_listevents #{host} | head -n5"
  $server.run("#{$space} clear_caches")
  out, _code = $server.run(cmd)
  unless out.include? status
    raise "#{out} should contain #{status}"
  end
end
