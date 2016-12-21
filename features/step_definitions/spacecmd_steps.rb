# Copyright (c) 2016 Novell, Inc.
# Licensed under the terms of the MIT license.

$space = "spacecmd -u admin -p admin "

And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  if target == "ssh-minion"
    cmd = "#{$space} system_listevents #{$ssh_minion_fullhostname} | head -n5"
    $server.run("#{$space} clear_caches")
    out, _code = $server.run(cmd)
    unless out.include? status
      raise "#{out} should contain #{status}"
    end
  end
end
