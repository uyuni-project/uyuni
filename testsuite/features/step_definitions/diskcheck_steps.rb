# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains all steps concerning diskcheck and the following actions required

When(/^I (un)?deploy diskcheck scripts on "([^"]*)"$/) do |negative, host|
  error_msg = ''
  code = -1
  node = get_target(host)
  script_files = ['diskcheck_custom_mount.sh', 'diskcheck_space_mgmt.sh', 'diskcheck_uyuni_service_mgmt.sh']
  # undeployment
  if negative
    cmd = "rm #{script_files.map { |x| "/root/#{x}" }.join(' ')}"
    res_out, res_err, code = ssh_command(cmd, node.full_hostname)
    error_msg = "return code: #{code};  stdout: #{res_out};  stderr: #{res_err}"
  # deployment
  else
    script_files.each do |script_file|
      src = "#{File.dirname(__FILE__)}/../upload_files/#{script_file}"
      dst = "/root/#{script_file}"
      success = node.scp_upload(src, dst)
      unless success
        error_msg = "File \"#{src}\" upload failed."
        break
      end
    end
    # success
    code = 0
  end

  raise ScriptError, error_msg unless code.zero?
end

When(/^I change the disk space check schedule to (run every minute|defaults)$/) do |settings|
  step 'I follow the left menu "Admin > Task Schedules"'
  step 'I follow this "diskcheck-task-queue-default" link'
  if settings == 'run every minute'
    step 'I enter "0 * * * * ?" as "date_cron"'
  else
    step 'I enter "0 0 * * * ?" as "date_cron"'
  end
  step 'I click on "Update Schedule"'
  step 'I should see a "Schedule diskcheck-task-queue-default has been updated." text'
end

When(/^I wait for the diskcheck alert notification$/) do
  # the "minute" schedule is set so we need to wait 60 seconds at least for the alert
  # added 2 more seconds for safety trigger
  step 'I wait for "62" seconds'
end

When(/^I fill disk space in "([^"]*)" up to "([0-9]+)%" on "([^"]*)"$/) do |directory, percentage, host|
  node = get_target(host)
  script = '/root/diskcheck_space_mgmt.sh'
  result, code = node.run("bash #{script} -p #{percentage} -d #{directory}", check_errors: false, runs_in_container: false, timeout: 600)

  raise ScriptError, "Server space filled: #{result}" unless code.zero?
end

When(/^I create a "([0-9]+)MB" disk image in "([^"]*)" and mount it to "([^"]*)" on "([^"]*)"$/) do |disk_size, directory, mountpoint, host|
  node = get_target(host)
  script = '/root/diskcheck_custom_mount.sh'
  result, code = node.run("bash #{script} -s #{disk_size} -d #{directory} -m #{mountpoint} up", check_errors: false, runs_in_container: false, timeout: 600)

  raise ScriptError, "Disk image creation failed: #{result}" unless code.zero?
end

When(/^I unmount "([^"]*)" and remove the disk image in "([^"]*)" on "([^"]*)"$/) do |mountpoint, directory, host|
  node = get_target(host)
  script = '/root/diskcheck_custom_mount.sh'
  result, code = node.run("bash #{script} -d #{directory} -m #{mountpoint} down", check_errors: false, runs_in_container: false, timeout: 600)

  raise ScriptError, "Disk image removal failed: #{result}" unless code.zero?
end

When(/^I configure the uyuni server to watch the "([^"]*)" directory with alert set to "([0-9]+)%" and threshold set to "([0-9]+)%"$/) do |directory, alert, threshold|
  node = get_target('server')
  script = '/root/diskcheck_uyuni_service_mgmt.sh'
  result, code = node.run("bash #{script} -d #{directory} -a #{alert} -t #{threshold} up", runs_in_container: false, check_errors: false)

  raise ScriptError, "Server diskcheck custom directory setup failed: #{result}" unless code.zero?
end

When(/^I configure the uyuni server to watch the "([^"]*)" directory set in the rhn config file$/) do |directory|
  node = get_target('server')
  script = '/root/diskcheck_uyuni_service_mgmt.sh'
  result, code = node.run("bash #{script} -d #{directory} -r up", runs_in_container: false, check_errors: false)

  raise ScriptError, "Server diskcheck custom directory setup via rhn.conf failed: #{result}" unless code.zero?
end

When(/^I configure the uyuni server to watch the default directories(, cleaning the rhn config file)?$/) do |rhn_setup|
  node = get_target('server')
  script = '/root/diskcheck_uyuni_service_mgmt.sh'
  if rhn_setup
    result, code = node.run("bash #{script} -r down", check_errors: false, runs_in_container: false)
  else
    result, code = node.run("bash #{script} down", check_errors: false, runs_in_container: false)
  end

  raise ScriptError, "Server diskcheck custom directory setup to defaults failed: #{result}" unless code.zero?
end

When(/^I cleanup the "([^"]*)" on "([^"]*)"$/) do |directory, host|
  node = get_target(host)
  script = '/root/diskcheck_space_mgmt.sh'
  res_out, res_err, code = ssh_command("bash #{script} -c -d #{directory}", node.full_hostname)

  raise ScriptError, "Server space cleaned: #{res_out}  #{res_err}" unless code.zero?
end

When(/^I release the space in "([^"]*)" on "([^"]*)"$/) do |directory, host|
  node = get_target(host)
  script = '/root/diskcheck_space_mgmt.sh'
  res_out, res_err, code = ssh_command("bash #{script} -c -f -d #{directory}", node.full_hostname)

  raise ScriptError, "Server space cleaned: #{res_out}  #{res_err}" unless code.zero?
end
