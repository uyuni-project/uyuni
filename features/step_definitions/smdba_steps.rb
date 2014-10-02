# Copyright 2011-2014 SUSE
$is_postgres = true
$msg = "This test has been disabled"

When(/^I cannot find file "(.*?)"$/) do |target_file|
  $output = sshcmd("file #{target_file}", ignore_err: true)
end

When(/^I start database with the command "(.*?)"$/) do |start_command|
  if $is_postgres
    $output = sshcmd(start_command)
  else
    puts $msg
  end
end

When(/^when I stop the database with the command "(.*?)"$/) do |stop_command|
  if $is_postgres
    $output = sshcmd(stop_command)
  else
    puts $msg
  end
end

When(/^when I check the database status with the command "(.*?)"$/) do |check_command|
  if $is_postgres
    $output = sshcmd(check_command)
  else
    puts $msg
  end
end

When(/^I stop the database with the command "(.*?)"$/) do |stop_command|
  if $is_postgres
    $output = sshcmd(stop_command)
  else
    puts $msg
  end
end

Then(/^when I issue command "(.*?)"$/) do |command|
  if $is_postgres
    $output = sshcmd(command)
  else
    puts $msg
  end
end

Then(/^I disable all the tests below$/) do
  $is_postgres = $output[:stdout].include? "ASCII text"
end

When(/^when I see that the database is "(.*?)" or "(.*?)" as it might already running$/) do |scs_status, fl_status|
  if $is_postgres
    fail if not $output[:stdout].include? scs_status and not $output[:stdout].include? fl_status
  else
    puts $msg
  end
end

Then(/^I want to see if the database is "(.*?)"$/) do |status|
  if $is_postgres
    fail if not $output[:stdout].include? status
  else
    puts $msg
  end
end

Then(/^I want to see if "(.*?)" is in the output$/) do |status|
  if $is_postgres
    fail if not $output[:stdout].include? status
  else
    puts $msg
  end
end

When(/^when I configure "(.*?)" parameter "(.*?)" to "(.*?)"$/) do |config_file, param, value|
    sshcmd("sed -i '/wal_level/d' #{config_file}", ignore_err: true)
    sshcmd("echo \"#{param} = #{value}\" >> #{config_file}", ignore_err: true)
    local_output = sshcmd("cat #{config_file} | grep #{param}", ignore_err: true)
    fail if not local_output[:stdout].include? value
end

Then(/^when I check internally configuration for "(.*?)" option$/) do |config_key|
  $current_checked_config_value = sshcmd("cd /;sudo -u postgres psql -c 'show wal_level;'")[:stdout]
end

Then(/^I expect to see the configuration is set to "(.*?)"$/) do |value|
  fail if not $current_checked_config_value.include? value
end

Then(/^I issue command "(.*?)"$/) do |cmd|
    sshcmd(cmd)
end

