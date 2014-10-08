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

Given(/^there is no such "(.*?)" directory$/) do |bkp_dir|
  sshcmd("test -d #{bkp_dir} && rm -rf #{bkp_dir}")
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
    $output = sshcmd(command, ignore_err: true)
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
  sshcmd("mkdir /#{bkp_dir};chown #{uid}:#{gid} /#{bkp_dir}")
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

When(/^when I change Access Control List on "(.*?)" directory to "(.*?)"$/) do |bkp_dir, acl_octal|
  bkp_dir.sub!("/", "")
  sshcmd("test -d /#{bkp_dir} && chmod #{acl_octal} /#{bkp_dir}")
  puts "Backup directory, ACL to \"#{acl_octal}\":"
  puts sshcmd("ls -la / | /usr/bin/grep #{bkp_dir}")[:stdout]
  puts "\n*** Taking backup, this might take a while ***\n"
end

Then(/^base backup is taken$/) do
  fail if not $output[:stdout].include? "Finished"
end

Then(/^in "(.*?)" directory there is "(.*?)" file and at least one backup checkpoint file$/) do |bkp_dir, archive_file|
  fail if sshcmd("test -f #{bkp_dir}/#{archive_file} && echo \"exists\" || echo \"missing\"")[:stdout].include? "missing"
  fail if sshcmd("ls #{bkp_dir}/*.backup 1>/dev/null 2>/dev/null && echo \"exists\" || echo \"missing\"")[:stdout].include? "missing"
end

Then(/^parameter "(.*?)" in the configuration file "(.*?)" is "(.*?)"$/) do |param, cfg_file, fuzzy_value|
  $output = sshcmd("cat #{cfg_file} | grep #{param}")
  fail if not $output[:stdout].include? fuzzy_value
end

Then(/^"(.*?)" destination should be set to "(.*?)" in configuration file$/) do |arch_cmd, dest_dir|
  fail if not $output[:stdout].include? dest_dir
end

#When(/^in the database I create dummy table "(.*?)" with column "(.*?)" and value "(.*?)"$/) do |tbl, clm, val|
#  fn = "/tmp/smdba-data-test.sql"
#  db = "susemanager"
#  sshcmd("echo \"create table #{tbl} (#{clm} varchar);insert into #{tbl} (#{clm}) values (\'#{val}\');\" > #{fn}", ignore_err: false)
#  sshcmd("sudo -u postgres psql -d #{db} -c 'drop table dummy' 2>/dev/null", ignore_err: true)
#  sshcmd("sudo -u postgres psql -d #{db} -af #{fn}", ignore_err: true)
#  sshcmd("file -f #{fn} && rm #{fn}")
#  fail if not sshcmd("sudo -u postgres psql -d #{db} -c 'select * from dummy' 2>/dev/null", ignore_err: true)[:stdout].include? val
#  puts "Table \"#{tbl}\" has been created with some dummy data inside"
#end

When(/^when I restore database from the backup$/) do
  puts "\n*** Restoring database from the backup. This will may take a while. ***\n\n"
  sshcmd("smdba backup-restore")
end

Then(/^I disable backup in the directory "(.*?)"$/) do |arg1|
  fail if not sshcmd("smdba backup-hot --enable=off")[:stdout].include? "Finished"
end

