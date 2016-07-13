# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Sleep for X seconds
#
When(/^I wait for "(\d+)" seconds$/) do |arg1|
  sleep(arg1.to_i)
end

When(/^I run rhn_check on this client$/) do
  output, _local, _remote, code = $client.test_and_store_results_together("rhn_check -vvv")
  if code != 0
      raise "rhn_check failed: #{$!}: #{output}"
  end
end

Then(/^I download the SSL certificate$/) do
  # download certicate on the client from the server via ssh protocol.
  local, _remote, command = $client.test_and_print_results("curl -S -k -o /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT http://#{server}/pub/RHN-ORG-TRUSTED-SSL-CERT", "root", 500)
  if command != 0 and local != 0 and remote != 0
	raise "fail to download the ssl certificate"
  end
  _local, _remote, command = $client.test_and_print_results("ls /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT", "root", 500)
end
