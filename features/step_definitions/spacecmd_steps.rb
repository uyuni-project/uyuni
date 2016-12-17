# Copyright (c) 2016 Novell, Inc.
# Licensed under the terms of the MIT license.

$space = "spacecmd -u admin -p admin "

And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  out, _code =  $server.run("#{$space} system_listevents #{target}"
  unless out.include? status
     raise "#{out} should contain #{status}"
  end
end
