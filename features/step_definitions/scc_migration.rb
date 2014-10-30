# Copyright 2011-2014 SUSE
$migrated = false
$default_scc = false

When(/^I save migration state$/) do
  $migrated = sshcmd("test -e /var/lib/spacewalk/scc/migrated && echo 'E'", ignore_err: true)[:stdout].include? 'E'
  $default_scc = sshcmd("test -e /var/lib/spacewalk/scc/default_scc && echo 'E'", ignore_err: true)[:stdout].include? 'E'
  sshcmd("rm /var/lib/spacewalk/scc/*", ignore_err: true)
end

Given(/^there is no "(.*?)" file on the server$/) do |target_file|
  sshcmd("test #{target_file} && -e rm #{target_file}", ignore_err: true)
end

Given(/^there is "(.*?)" file on the server$/) do |target_file|
  sshcmd("touch #{target_file}", ignore_err: true)
end

Then(/^I restore migration state$/) do
  action = $migrated ? "touch" : "rm"
  sshcmd("#{action} /var/lib/spacewalk/scc/migrated", ignore_err: true)    

  action = $default_scc ? "touch" : "rm"
  sshcmd("#{action} /var/lib/spacewalk/scc/default_scc", ignore_err: true)    
end
