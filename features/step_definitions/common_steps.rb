# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Sleep for X seconds
#
When /^I wait for "(\d+)" seconds$/ do |arg1|
  sleep(arg1.to_i)
end

When /^I run rhn_check on this client$/ do
  output = `rhn_check -vvv 2>&1`
  if ! $?.success?
      raise "rhn_check failed: #{$!}: #{output}"
  end
end

Then /^I download the SSL certificate$/ do
  output = `curl -S -k -o /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT http://$TESTHOST/pub/RHN-ORG-TRUSTED-SSL-CERT`
  if ! $?.success?
      raise "Execute command failed: #{$!}: #{output}"
  end
end

