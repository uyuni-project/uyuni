# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When(/^I refresh the metadata$/) do
  sshcmd("rhn_check -vvv 2>&1")
  code = sshcmd("echo $?")
    if code =! 0
      raise "rhn_check failed: #{$!}: #{output}"
    end
  client_refresh_metadata
end

Then(/^I should have '([^']*)' in the metadata$/) do |text|
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  cmd ="zgrep '#{text}' #{client_raw_repodata_dir("sles11-sp3-updates-#{arch}-channel")}/primary.xml.gz"
  out, local, remote, code = $server.test_and_store_results_together(cmd, "root", 500)
  fail if code != 0
end

Then(/^I should not have '([^']*)' in the metadata$/) do |text|
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  cmd = "zgrep '#{text}' #{client_raw_repodata_dir("sles11-sp3-updates-#{arch}-channel")}/primary.xml.gz"
  out, local, remote, code = $server.test_and_store_results_together(cmd, "root", 500)
  fail if code != 0
end

Then(/^"([^"]*)" should exists in the metadata$/) do |file|
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  fail if not  file_exist($server, "#{client_raw_repodata_dir("sles11-sp3-updates-#{arch}-channel")}/#{file}")
end

Then(/^I should have '([^']*)' in the patch metadata$/) do |text|
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  cmd = "zgrep '#{text}' #{client_raw_repodata_dir("sles11-sp3-updates-#{arch}-channel")}/updateinfo.xml.gz"
  out, local, remote, code = $server.test_and_store_results_together(cmd, "root", 500)
  fail if code != 0
end
