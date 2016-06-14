# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Sleep for X seconds
#
When(/^I wait for "(\d+)" seconds$/) do |arg1|
  sleep(arg1.to_i)
end

When(/^I run rhn_check on this client$/) do
  sshcmd("rhn_check -vvv 2>&1")
  code = sshcmd("echo $?")
  if code != 0
      raise "rhn_check failed: #{$!}: #{output}"
  end
end

Then(/^I download the SSL certificate$/) do
  # FIXME: this need to be run in client 1
  #   and we testhost must my parsed.
  server = ENV['TESTHOST'] 
  client = ENV['CLIENT']
  sshcmd("curl -S -k -o /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT http://#{server}/pub/RHN-ORG-TRUSTED-SSL-CERT",  host: client)
end

