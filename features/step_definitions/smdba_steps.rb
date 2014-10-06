# Copyright 2011-2014 SUSE
$is_postgres = true
$msg = "This test has been disabled"

Given(/^database is running$/) do
  if not sshcmd("smdba db-status")[:stdout].include? "online"
    sshcmd("smdba db-start")
    fail if not sshcmd("smdba db-status")[:stdout].include? "online"
  else
    puts "Database is running"
  end
end

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
  #puts "Trying to call: #{cmd}"
  $output = sshcmd(cmd, ignore_err: true)
  #puts $output[:stdout]
end

Then(/^I find tablespaces "(.*?)" and "(.*?)"$/) do |suma_ts, pg_ts|
  fail if not $output[:stdout].include? suma_ts
  fail if not $output[:stdout].include? pg_ts
end

Then(/^I find core examination is "(.*?)", database analysis is "(.*?)" and space reclamation is "(.*?)"$/) do |arg1, arg2, arg3|
  fail if $output[:stdout].include? "failed"
end

Then(/^I find "(.*?)", "(.*?)" and "(.*?)" are in the list\.$/) do |rhn_tbl, rhn_tbl1, suse_tbl|
  [rhn_tbl, rhn_tbl1, suse_tbl].each do |tbl|
    if not $output[:stdout].include? tbl
      fail
    end
  end 
end


#
# Backup-related tests
#

When(/^I create backup directory "(.*?)" with UID "(.*?)" and GID "(.*?)"$/) do |bkp_dir, uid, gid|
  sshcmd("test -d #{bkp_dir} || mkdir #{bkp_dir};chown #{uid}:#{gid} #{bkp_dir}")
  bkp_dir.sub!("/", "")
  puts "Backup directory:"
  puts sshcmd("ls -la / | /usr/bin/grep #{bkp_dir}")[:stdout]
end

Then(/^I should see error message that asks "(.*?)" belong to the same UID\/GID as "(.*?)" directory$/) do |bkp_dir, data_dir|
  fail if not $output[:stderr].include? "The \"#{bkp_dir}\" directory must belong to the same user and group as \"#{data_dir}\" directory."
end

Then(/^I should see error message that asks "(.*?)" has same permissions as "(.*?)" directory$/) do |bkp_dir, data_dir|
  fail if not $output[:stderr].include? "The \"#{bkp_dir}\" directory must have the same permissions as \"#{data_dir}\" directory."
end

Then(/^I remove backup directory "(.*?)"$/) do |bkp_dir|
  sshcmd("test -d #{bkp_dir} && rm -rf #{bkp_dir}")
end
