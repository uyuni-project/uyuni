# Copyright 2011-2014 SUSE
$migrated = false
$default_scc = false
$SCC_PATH = "/var/lib/spacewalk/scc"

When(/^I save migration state$/) do
  $migrated = sshcmd("test -e #{$SCC_PATH}/migrated && echo 'E'", ignore_err: true)[:stdout].include? 'E'
  $default_scc = sshcmd("test -e #{$SCC_PATH}/default_scc && echo 'E'", ignore_err: true)[:stdout].include? 'E'
  sshcmd("rm #{$SCC_PATH}/scc/*", ignore_err: true)
end

Given(/^there is no "(.*?)" file on the server$/) do |target_file|
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
