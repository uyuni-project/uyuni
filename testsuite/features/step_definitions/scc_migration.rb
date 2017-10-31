# Copyright 2011-2014 SUSE
$migrated = false
$default_scc = false
$SCC_PATH = "/var/lib/spacewalk/scc"

Given(/^migration state saved$/) do
  $migrated = sshcmd("test -e #{$SCC_PATH}/migrated && echo 'E'", ignore_err: true)[:stdout].include? 'E'
  $default_scc = sshcmd("test -e #{$SCC_PATH}/default_scc && echo 'E'", ignore_err: true)[:stdout].include? 'E'
  sshcmd("rm #{$SCC_PATH}/scc/*", ignore_err: true)
end

Given(/^the server is not yet migrated to SCC$/) do
  step %(the file "/var/lib/spacewalk/scc/migrated" exists on the server)
end

Given(/^the SCC is not yet the default customer center$/) do
  step %(the file "/var/lib/spacewalk/scc/default_scc" exists on the server)
end

Given(/^the SCC is the default customer center$/) do
  step %(there is "/var/lib/spacewalk/scc/default_scc" file on the server)
end

Given(/^the server is migrated to SCC$/) do
  step %(there is "/var/lib/spacewalk/scc/migrated" file on the server)
end

When(/^the file "(.*?)" exists on the server$/) do |target_file|
  sshcmd("test -e #{target_file} && rm #{target_file}", ignore_err: true)
end

Given(/^there is "(.*?)" file on the server$/) do |target_file|
  sshcmd("touch #{target_file}", ignore_err: true)
end

Then(/^I restore migration state$/) do
  action = $migrated ? "touch" : "rm"
  sshcmd("#{action} #{$SCC_PATH}/migrated", ignore_err: true)

  action = $default_scc ? "touch" : "rm"
  sshcmd("#{action} #{$SCC_PATH}/default_scc", ignore_err: true)
end
