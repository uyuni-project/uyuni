# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When(/^I wait for "(\d+)" seconds$/) do |arg1|
  sleep(arg1.to_i)
end

When(/^I run rhn_check on this client$/) do
  $client.run("rhn_check -vvv", true, 500, 'root')
end

Then(/^I download the SSL certificate$/) do
  # download certicate on the client from the server via ssh protocol
  cert_path = "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT"
  wget = "wget --no-check-certificate -O"
  $client.run("#{wget} #{cert_path} http://#{$server_ip}/pub/RHN-ORG-TRUSTED-SSL-CERT", true, 500, 'root')
  $client.run("ls #{cert_path}")
end

Then(/^I should see the "([^"]*)" as link$/) do |target|
  step %(I should see a "#{$client_fullhostname}" link) if target == "sle-client"
  step %(I should see a "#{$minion_fullhostname}" link) if target == "sle-minion"
  step %(I should see a "#{$ssh_minion_fullhostname}" link) if target == "ssh-minion"
  step %(I should see a "#{$ceos_minion_fullhostname}" link) if target == "ceos-minion"
end

Then(/^I should not see the "([^"]*)" as text$/) do |target|
  step %(I should not see a "#{$client_fullhostname}" text) if target == "sle-client"
  step %(I should not see a "#{$minion_fullhostname}" text) if target == "sle-minion"
  step %(I should not see a "#{$ssh_minion_fullhostname}" text) if target == "ssh-minion"
  step %(I should not see a "#{$ceos_minion_fullhostname}" text) if target == "ceos-minion"
end
