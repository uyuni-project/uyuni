# Copyright 2011-2018 SUSE

Given(/^a postgresql database is running$/) do
  $output = sshcmd('file /var/lib/pgsql/data/postgresql.conf', ignore_err: true)
  unless $output[:stdout].include? 'ASCII text'
    puts 'Tests require Postgresql database, skipping...'
    pending
  end

  if !sshcmd('smdba db-status')[:stdout].include? 'online'
    sshcmd('smdba db-start')
    assert_includes(sshcmd('smdba db-status')[:stdout], 'online')
  else
    puts 'Database is running'
  end
end

Given(/^there is no such "(.*?)" directory$/) do |bkp_dir|
  sshcmd("test -d #{bkp_dir} && rm -rf #{bkp_dir}")
end

When(/^I start database with the command "(.*?)"$/) do |start_command|
  $output = sshcmd(start_command)
end

When(/^when I stop the database with the command "(.*?)"$/) do |stop_command|
  $output = sshcmd(stop_command)
end

When(/^when I check the database status with the command "(.*?)"$/) do |check_command|
  $output = sshcmd(check_command)
end

When(/^I stop the database with the command "(.*?)"$/) do |stop_command|
  $output = sshcmd(stop_command)
end

Then(/^when I issue command "(.*?)"$/) do |command|
  $output = sshcmd(command, ignore_err: true)
end

When(/^when I see that the database is "(.*?)" or "(.*?)" as it might already running$/) do |scs_status, fl_status|
  assert_match(/#{scs_status}|#{fl_status}/, $output[:stdout])
end

Then(/^I want to see if the database is "(.*?)"$/) do |status|
  assert_includes($output[:stdout], status)
end

Then(/^I want to see if "(.*?)" is in the output$/) do |status|
  assert_includes($output[:stdout], status)
end

When(/^when I configure "(.*?)" parameter "(.*?)" to "(.*?)"$/) do |config_file, param, value|
  sshcmd("sed -i '/wal_level/d' #{config_file}", ignore_err: true)
  sshcmd("echo \"#{param} = #{value}\" >> #{config_file}", ignore_err: true)
  local_output = sshcmd("cat #{config_file} | grep #{param}", ignore_err: true)
  assert_includes(local_output[:stdout], value)
end

Then(/^when I check internally configuration for "(.*?)" option$/) do |_config_key|
  $current_checked_config_value = sshcmd("cd /;sudo -u postgres psql -c 'show wal_level;'")[:stdout]
end

Then(/^I expect to see the configuration is set to "(.*?)"$/) do |value|
  assert_includes($current_checked_config_value, value)
end

Then(/^I expect to see the configuration is not set to "(.*?)"$/) do |value|
  refute_includes($current_checked_config_value, value)
end

Then(/^I issue command "(.*?)"$/) do |cmd|
  $output = sshcmd(cmd, ignore_err: true)
end

Then(/^tablespace "([^"]*)" should be listed$/) do |ts|
  assert_includes($output[:stdout], ts)
end

Then(/^none of core examination, database analysis, and space reclamation should be "([^"]*)"$/) do |state|
  refute_includes($output[:stdout], state)
end

Then(/^table "([^"]*)" should be listed$/) do |tbl|
  assert_includes($output[:stdout], tbl)
end

#
# Backup-related tests
#
Given(/^database "(.*?)" has no table "(.*?)"$/) do |dbname, tbl|
  $db = dbname
  out = sshcmd("sudo -u postgres psql -d #{$db} -c 'drop table dummy'", ignore_err: true)
  refute_includes(out[:stdout], 'DROP TABLE')
  assert_includes(out[:stderr], "table \"#{tbl}\" does not exist")
end

When(/^I create backup directory "(.*?)" with UID "(.*?)" and GID "(.*?)"$/) do |bkp_dir, uid, gid|
  sshcmd("mkdir /#{bkp_dir};chown #{uid}:#{gid} /#{bkp_dir}")
  bkp_dir.sub!('/', '')
  puts 'Backup directory:'
  puts sshcmd("ls -la / | /usr/bin/grep #{bkp_dir}")[:stdout]
end

Then(/^I should see error message that asks "(.*?)" belong to the same UID\/GID as "(.*?)" directory$/) do |bkp_dir, data_dir|
  assert_includes($output[:stderr],
                  "The \"#{bkp_dir}\" directory must belong to the same user and group as \"#{data_dir}\" directory.")
end

Then(/^I should see error message that asks "(.*?)" has same permissions as "(.*?)" directory$/) do |bkp_dir, data_dir|
  assert_includes($output[:stderr],
                  "The \"#{bkp_dir}\" directory must have the same permissions as \"#{data_dir}\" directory.")
end

Then(/^I remove backup directory "(.*?)"$/) do |bkp_dir|
  sshcmd("test -d #{bkp_dir} && rm -rf #{bkp_dir}")
end

When(/^when I change Access Control List on "(.*?)" directory to "(.*?)"$/) do |bkp_dir, acl_octal|
  bkp_dir.sub!('/', '')
  sshcmd("test -d /#{bkp_dir} && chmod #{acl_octal} /#{bkp_dir}")
  puts "Backup directory, ACL to \"#{acl_octal}\":"
  puts sshcmd("ls -la / | /usr/bin/grep #{bkp_dir}")[:stdout]
  puts "\n*** Taking backup, this might take a while ***\n"
end

Then(/^base backup is taken$/) do
  assert_includes($output[:stdout], 'Finished')
end

Then(/^in "(.*?)" directory there is "(.*?)" file and at least one backup checkpoint file$/) do |bkp_dir, archive_file|
  refute_includes(
    sshcmd("test -f #{bkp_dir}/#{archive_file} && echo \"exists\" || echo \"missing\"")[:stdout],
    'missing'
  )
  refute_includes(
    sshcmd("ls #{bkp_dir}/*.backup 1>/dev/null 2>/dev/null && echo \"exists\" || echo \"missing\"")[:stdout],
    'missing'
  )
end

Then(/^parameter "(.*?)" in the configuration file "(.*?)" is "(.*?)"$/) do |param, cfg_file, fuzzy_value|
  $output = sshcmd("cat #{cfg_file} | grep #{param}")
  assert_includes($output[:stdout], fuzzy_value)
end

Then(/^"(.*?)" destination should be set to "(.*?)" in configuration file$/) do |_arch_cmd, dest_dir|
  assert_includes($output[:stdout], dest_dir)
end

When(/^I set a checkpoint$/) do
  sshcmd("sudo -u postgres psql -d #{$db} -c 'checkpoint' 2>/dev/null", ignore_err: true)
end

When(/^when in the database I create dummy table "(.*?)" with column "(.*?)" and value "(.*?)"$/) do |tbl, clm, val|
  fn = '/tmp/smdba-data-test.sql'
  sshcmd("echo \"create table #{tbl} (#{clm} varchar);insert into #{tbl} (#{clm}) values (\'#{val}\');\" > #{fn}", ignore_err: false)
  sshcmd("sudo -u postgres psql -d #{$db} -c 'drop table dummy' 2>/dev/null", ignore_err: true)
  sshcmd("sudo -u postgres psql -d #{$db} -af #{fn}", ignore_err: true)
  sshcmd("file -f #{fn} && rm #{fn}")
  assert_includes(
    sshcmd("sudo -u postgres psql -d #{$db} -c 'select * from dummy' 2>/dev/null", ignore_err: true)[:stdout],
    val
  )
  puts "Table \"#{tbl}\" has been created with some dummy data inside"
end

When(/^when I restore database from the backup$/) do
  puts "\n*** Restoring database from the backup. This will may take a while. ***\n\n"
  sshcmd('smdba backup-restore')
end

Then(/^I disable backup in the directory "(.*?)"$/) do |_arg1|
  assert_includes(
    sshcmd('smdba backup-hot --enable=off')[:stdout], 'Finished'
  )
end
