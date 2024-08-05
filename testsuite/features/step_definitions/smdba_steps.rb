# Copyright 2011-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions of all steps concerning the configuration of
### and access to the database.

Given(/^a postgresql database is running$/) do
  $output, _code = get_target('server').run('file /var/lib/pgsql/data/postgresql.conf', check_errors: false)
  unless $output.include? 'ASCII text'
    log 'Tests require Postgresql database, skipping...'
    pending
  end
  smdba_db_status, _code = get_target('server').run('smdba db-status', check_errors: false)
  if smdba_db_status.include? 'online'
    log 'Database is running'
  else
    get_target('server').run('smdba db-start')
    smdba_db_status, _code = get_target('server').run('smdba db-status', check_errors: false)
    assert_includes(smdba_db_status, 'online')
  end
end

Given(/^there is no such "(.*?)" directory$/) do |bkp_dir|
  get_target('server').run("test -d #{bkp_dir} && rm -rf #{bkp_dir}", check_errors: false)
end

When(/^I start database with the command "(.*?)"$/) do |start_command|
  $output, _code = get_target('server').run(start_command)
end

When(/^I stop the database with the command "(.*?)"$/) do |stop_command|
  $output, _code = get_target('server').run(stop_command)
end

When(/^I check the database status with the command "(.*?)"$/) do |check_command|
  $output, _code = get_target('server').run(check_command)
end

When(/^I see that the database is "(.*?)" or "(.*?)" as it might already running$/) do |scs_status, fl_status|
  assert_match(/#{scs_status}|#{fl_status}/, $output)
end

Then(/^the database should be "(.*?)"$/) do |status|
  assert_includes($output, status)
end

Then(/^"(.*?)" should be in the output$/) do |status|
  assert_includes($output, status)
end

When(/^I configure "(.*?)" parameter "(.*?)" to "(.*?)"$/) do |config_file, param, value|
  get_target('server').run("sed -i '/wal_level/d' #{config_file}", check_errors: false)
  get_target('server').run("echo \"#{param} = #{value}\" >> #{config_file}", check_errors: false)
  local_output, _code = get_target('server').run("cat #{config_file} | grep #{param}", check_errors: false)
  assert_includes(local_output, value)
end

Then(/^I check internally configuration for "(.*?)" option$/) do |_config_key|
  $current_checked_config_value, _code = get_target('server').run('cd /;sudo -u postgres psql -c \'show wal_level;\'', check_errors: false)
end

Then(/^the configuration should be set to "(.*?)"$/) do |value|
  assert_includes($current_checked_config_value, value)
end

Then(/^the configuration should not be set to "(.*?)"$/) do |value|
  refute_includes($current_checked_config_value, value)
end

Then(/^I issue command "(.*?)"$/) do |command|
  $output, _code = get_target('server').run(command, check_errors: false)
end

Then(/^tablespace "([^"]*)" should be listed$/) do |ts|
  assert_includes($output, ts)
end

Then(/^none of core examination, database analysis, and space reclamation should be "([^"]*)"$/) do |state|
  refute_includes($output, state)
end

Then(/^table "([^"]*)" should be listed$/) do |tbl|
  assert_includes($output, tbl)
end

#
# Backup-related tests
#
Given(/^database "(.*?)" has no table "(.*?)"$/) do |dbname, tbl|
  $db = dbname
  out, err, _code = get_target('server').run("sudo -u postgres psql -d #{$db} -c 'drop table dummy'", separated_results: true, check_errors: false)
  refute_includes(out, 'DROP TABLE')
  assert_includes(err, "table \"#{tbl}\" does not exist")
end

When(/^I create backup directory "(.*?)" with UID "(.*?)" and GID "(.*?)"$/) do |bkp_dir, uid, gid|
  get_target('server').run("mkdir /#{bkp_dir};chown #{uid}:#{gid} /#{bkp_dir}")
  bkp_dir.sub!('/', '')
  log 'Backup directory:'
  log get_target('server').run("ls -la / | /usr/bin/grep #{bkp_dir}", check_errors: false)[0]
end

Then(/^I should see error message that asks "(.*?)" belong to the same UID\/GID as "(.*?)" directory$/) do |bkp_dir, data_dir|
  assert_includes($output, "The \"#{bkp_dir}\" directory must belong to the same user and group as \"#{data_dir}\" directory.")
end

Then(/^I should see error message that asks "(.*?)" has same permissions as "(.*?)" directory$/) do |bkp_dir, data_dir|
  assert_includes($output, "The \"#{bkp_dir}\" directory must have the same permissions as \"#{data_dir}\" directory.")
end

Then(/^I remove backup directory "(.*?)"$/) do |bkp_dir|
  get_target('server').run("test -d #{bkp_dir} && rm -rf #{bkp_dir}")
end

When(/^I change Access Control List on "(.*?)" directory to "(.*?)"$/) do |bkp_dir, acl_octal|
  bkp_dir.sub!('/', '')
  get_target('server').run("test -d /#{bkp_dir} && chmod #{acl_octal} /#{bkp_dir}")
  log "Backup directory, ACL to \"#{acl_octal}\":"
  log get_target('server').run("ls -la / | /usr/bin/grep #{bkp_dir}", check_errors: false)[0]
  log "\n*** Taking backup, this might take a while ***\n"
end

Then(/^base backup is taken$/) do
  assert_includes($output, 'Finished')
end

Then(/^in "(.*?)" directory there is "(.*?)" file and at least one backup checkpoint file$/) do |bkp_dir, archive_file|
  refute_includes(get_target('server').run("test -f #{bkp_dir}/#{archive_file} && echo \"exists\" || echo \"missing\"", check_errors: false), 'missing')
  refute_includes(get_target('server').run("ls #{bkp_dir}/*.backup 1>/dev/null 2>/dev/null && echo \"exists\" || echo \"missing\"", check_errors: false), 'missing')
end

Then(/^parameter "(.*?)" in the configuration file "(.*?)" is "(.*?)"$/) do |param, cfg_file, fuzzy_value|
  $output, _code = get_target('server').run("cat #{cfg_file} | grep #{param}")
  assert_includes($output, fuzzy_value)
end

Then(/^"(.*?)" destination should be set to "(.*?)" in configuration file$/) do |_arch_cmd, dest_dir|
  assert_includes($output, dest_dir)
end

When(/^I set a checkpoint$/) do
  get_target('server').run("sudo -u postgres psql -d #{$db} -c 'checkpoint' 2>/dev/null", check_errors: false)
end

When(/^in the database I create dummy table "(.*?)" with column "(.*?)" and value "(.*?)"$/) do |tbl, clm, val|
  fn = '/tmp/smdba-data-test.sql'
  get_target('server').run("echo \"create table #{tbl} (#{clm} varchar);insert into #{tbl} (#{clm}) values (\'#{val}\');\" > #{fn}", check_errors: false)
  get_target('server').run("sudo -u postgres psql -d #{$db} -c 'drop table dummy' 2>/dev/null", check_errors: false)
  get_target('server').run("sudo -u postgres psql -d #{$db} -af #{fn}", check_errors: false)
  get_target('server').run("file -f #{fn} && rm #{fn}")
  assert_includes(get_target('server').run("sudo -u postgres psql -d #{$db} -c 'select * from dummy' 2>/dev/null", check_errors: false)[0], val)
  log "Table \"#{tbl}\" has been created with some dummy data inside"
end

When(/^I restore database from the backup$/) do
  log "\n*** Restoring database from the backup. This will may take a while. ***\n\n"
  output, code = get_target('server').run('smdba backup-restore')
  log "#{output}\n\n"
  raise 'Restore Failed' unless code.zero?
end

Then(/^I disable backup in the directory "(.*?)"$/) do |_arg1|
  assert_includes(get_target('server').run('smdba backup-hot --enable=off', check_errors: false)[0], 'Finished')
end
