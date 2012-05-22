# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When /^I refresh the metadata$/ do
   output = `rhn_check -vvv 2>&1`
   if ! $?.success?
       raise "rhn_check failed: #{$!}: #{output}"
   end
   client_refresh_metadata
end

Then /^I should have "([^"]*)" in the metadata$/ do |text|
   arch=`uname -m`
   arch.chomp!
   if arch != "x86_64"
     arch = "i586"
   end
   `zgrep #{text} #{client_raw_repodata_dir("sles11-sp1-updates-#{arch}-channel")}/primary.xml.gz`
   fail if ! $?.success?
end

Then /^"([^"]*)" should exists in the metadata$/ do |file|
    arch=`uname -m`
    arch.chomp!
    if arch != "x86_64"
        arch = "i586"
    end
    fail if not File.exists?("#{client_raw_repodata_dir("sles11-sp1-updates-#{arch}-channel")}/#{file}")

