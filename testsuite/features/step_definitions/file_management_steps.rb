# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains all steps concerning file management on a system

# generic file management steps

When(/^I destroy "([^"]*)" directory on server$/) do |directory|
  get_target('server').run("rm -rf #{directory}")
end

When(/^I destroy "([^"]*)" directory on "([^"]*)"$/) do |directory, host|
  node = get_target(host)
  node.run("rm -rf #{directory}")
end

When(/^I remove "([^"]*)" from "([^"]*)"$/) do |filename, host|
  node = get_target(host)
  file_delete(node, filename)
end

Then(/^file "([^"]*)" should exist on server$/) do |filename|
  get_target('server').run("test -f #{filename}")
end

Then(/^file "([^"]*)" should exist on "([^"]*)"$/) do |filename, host|
  node = get_target(host)
  node.run("test -f #{filename}")
end

Then(/^file "([^"]*)" should have ([0-9]+) permissions on "([^"]*)"$/) do |filename, permissions, host|
  node = get_target(host)
  node.run("test \"`stat -c '%a' #{filename}`\" = \"#{permissions}\"")
end

Then(/^file "([^"]*)" should not exist on server$/) do |filename|
  get_target('server').run("test ! -f #{filename}")
end

Then(/^file "([^"]*)" should not exist on "([^"]*)"$/) do |filename, host|
  node = get_target(host)
  node.run("test ! -f #{filename}")
end

When(/^I store "([^"]*)" into file "([^"]*)" on "([^"]*)"$/) do |content, filename, host|
  node = get_target(host)
  node.run("echo \"#{content}\" > #{filename}", timeout: 600)
end

When(/^I bootstrap (traditional|minion) client "([^"]*)" using bootstrap script with activation key "([^"]*)" from the (server|proxy)$/) do |client_type, host, key, target_type|
  # Use server if proxy is not defined as proxy is not mandatory
  target = get_target('proxy')
  if target_type.include?('server') || get_target('proxy').nil?
    log 'WARN: Bootstrapping to server, because proxy is not defined.' unless target_type.include? 'server'
    target = get_target('server')
  end

  # Prepare bootstrap script for different types of clients
  client = client_type == 'traditional' ? '--traditional' : ''
  force_bundle = use_salt_bundle ? '--force-bundle' : ''

  node = get_target(host)
  gpg_keys = get_gpg_keys(node, target)
  cmd = "mgr-bootstrap #{client} #{force_bundle} &&
  sed -i s\'/^exit 1//\' /srv/www/htdocs/pub/bootstrap/bootstrap.sh &&
  sed -i '/^ACTIVATION_KEYS=/c\\ACTIVATION_KEYS=#{key}' /srv/www/htdocs/pub/bootstrap/bootstrap.sh &&
  chmod 644 /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT &&
  sed -i '/^ORG_GPG_KEY=/c\\ORG_GPG_KEY=#{gpg_keys.join(',')}' /srv/www/htdocs/pub/bootstrap/bootstrap.sh &&
  cat /srv/www/htdocs/pub/bootstrap/bootstrap.sh"
  output, = target.run(cmd)
  unless output.include? key
    log output
    raise "Key: #{key} not included"
  end

  # Run bootstrap script and check for result
  boostrap_script = 'bootstrap-general.exp'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{boostrap_script}"
  dest = "/tmp/#{boostrap_script}"
  return_code = file_inject(target, source, dest)
  raise ScriptError, 'File injection failed' unless return_code.zero?

  system_name = get_system_name(host)
  output, = target.run("sed -i '/^set timeout /c\\set timeout #{DEFAULT_TIMEOUT}' /tmp/#{boostrap_script} && expect -f /tmp/#{boostrap_script} #{system_name}", verbose: true)
  unless output.include? '-bootstrap complete-'
    log output.encode('utf-8', invalid: :replace, undef: :replace, replace: '_')
    raise 'Bootstrap didn\'t finish properly'
  end
end

When(/^I bootstrap client "([^"]*)" using bootstrap script with activation key "([^"]*)" and reactivation key from the (server|proxy)$/) do |host, act_key, target_type|
  # Use server if proxy is not defined as proxy is not mandatory
  target = get_target('proxy')
  if target_type.include?('server') || target.nil?
    log 'WARN: Bootstrapping to server, because proxy is not defined.' unless target_type.include? 'server'
    target = get_target('server')
  end

  system_name = get_system_name(host)
  node_id = $api_test.system.retrieve_server_id(system_name)
  react_key = $api_test.system.obtain_reactivation_key(node_id)

  # generate bootstrap script
  bootstrap_script_name = 'bootstrap-trad-salt.sh'
  generate_bootstrap_script = "mgr-bootstrap --activation-keys=#{act_key} --script=#{bootstrap_script_name}"
  _, code = target.run(generate_bootstrap_script)
  raise 'Unable to generate bootstrap script' unless code.zero?

  bootstrap_script = "/srv/www/htdocs/pub/bootstrap/#{bootstrap_script_name}"

  # Run bootstrap script and check for result
  expect_file_to_bootstrap = 'bootstrap_trad_to_min.exp'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{expect_file_to_bootstrap}"
  dest = "/tmp/#{expect_file_to_bootstrap}"
  return_code = file_inject(target, source, dest)
  raise 'File injection failed' unless return_code.zero?

  output, = target.run("expect -f /tmp/#{expect_file_to_bootstrap} #{bootstrap_script} #{system_name} #{react_key}", verbose: true)
  unless output.include? '-bootstrap complete-'
    log output.encode('utf-8', invalid: :replace, undef: :replace, replace: '_')
    raise 'Bootstrap didn\'t finish properly'
  end
end

Then(/^file "([^"]*)" should contain "([^"]*)" on "([^"]*)"$/) do |filename, content, host|
  node = get_target(host)
  node.run("test -f #{filename}")
  node.run("grep \"#{content}\" #{filename}")
end

Then(/^I remove server hostname from hosts file on "([^"]*)"$/) do |host|
  node = get_target(host)
  node.run("sed -i \'s/#{get_target('server').full_hostname}//\' /etc/hosts")
end
