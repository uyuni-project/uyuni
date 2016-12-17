# Copyright (c) 2016 Novell, Inc.
# Licensed under the terms of the MIT license.

$space = "spacecmd -u admin -p admin "

And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  if target == "sle-minion"
    cmd = "#{$space} system_listevents #{$minion_fullhostname} | head -n5"
    out, _code = $server.run(cmd)
    unless out.include? status
      raise "#{out} should contain #{status}"
    end
  end
end
