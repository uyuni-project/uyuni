# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains all steps concerning content lifecycle and hostname management

# content lifecycle steps
When(/^I click the environment build button$/) do
  raise ScriptError, 'Click on environment build failed' unless find_button('cm-build-modal-save-button', disabled: false, wait: DEFAULT_TIMEOUT).click
end

When(/^I click promote from Development to QA$/) do
  begin
    promote_first = first(:xpath, '//button[contains(., \'Promote\')]')
    promote_first.click
  rescue Capybara::ElementNotFound => e
    raise ScriptError, "Click on promote from Development failed: #{e}"
  end
end

When(/^I click promote from QA to Production$/) do
  begin
    promote_second = find_all(:xpath, '//button[contains(., \'Promote\')]', minimum: 2)[1]
    promote_second.click
  rescue Capybara::ElementNotFound => e
    raise ScriptError, "Click on promote from QA failed: #{e}"
  end
end

Then(/^I should see a "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    raise ScriptError, "Text \"#{text}\" not found" unless check_text_and_catch_request_timeout_popup?(text)
  end
end

When(/^I wait at most (\d+) seconds until I see "([^"]*)" text in the environment "([^"]*)"$/) do |seconds, text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    step %(I wait at most #{seconds} seconds until I see "#{text}" text)
  end
end

When(/^I wait until I see "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    raise ScriptError, "Text \"#{text}\" not found" unless check_text_and_catch_request_timeout_popup?(text, timeout: DEFAULT_TIMEOUT)
  end
end

When(/^I add the "([^"]*)" channel to sources$/) do |channel|
  within(:xpath, "//span[text()='#{channel}']/../..") do
    raise ScriptError, 'Add channel failed' unless find(:xpath, './/input[@type="checkbox"]').set(true)
  end
end

When(/^I click the "([^"]*)" item (.*?) button$/) do |name, action|
  button =
    case action
    when /details/ then 'i[contains(@class, \'fa-list\')]'
    when /edit/ then 'i[contains(@class, \'fa-edit\')]'
    when /delete/ then 'i[contains(@class, \'fa-trash\')]'
    else raise ScriptError, "Unknown element with description '#{action}'"
    end

  td_element = find(:xpath, "//td[contains(text(), '#{name}')]")
  raise ScriptError, "xpath: #{name} item not found" unless td_element

  button_element = td_element.find(:xpath, "./ancestor::tr/td/button/#{button} | ./ancestor::tr/td/div/button/#{button}")
  raise ScriptError, "xpath: #{action} button not found" unless button_element.click
end

When(/^I backup the SSH authorized_keys file of host "([^"]*)"$/) do |host|
  # authorized_keys paths on the client
  auth_keys_path = '/root/.ssh/authorized_keys'
  auth_keys_sav_path = '/root/.ssh/authorized_keys.sav'
  target = get_target(host)
  _, ret_code = target.run("cp #{auth_keys_path} #{auth_keys_sav_path}")
  raise ScriptError, 'error backing up authorized_keys on host' if ret_code.nonzero?
end

When(/^I add pre-generated SSH public key to authorized_keys of host "([^"]*)"$/) do |host|
  key_filename = 'id_rsa_bootstrap-passphrase_linux.pub'
  target = get_target(host)
  ret_code = file_inject(target, "#{File.dirname(__FILE__)}/../upload_files/ssh_keypair/#{key_filename}", "/tmp/#{key_filename}")
  target.run("cat /tmp/#{key_filename} >> /root/.ssh/authorized_keys", timeout: 500)
  raise ScriptError, 'Error copying ssh pubkey to host' if ret_code.nonzero?
end

When(/^I restore the SSH authorized_keys file of host "([^"]*)"$/) do |host|
  # authorized_keys paths on the client
  auth_keys_path = '/root/.ssh/authorized_keys'
  auth_keys_sav_path = '/root/.ssh/authorized_keys.sav'
  target = get_target(host)
  target.run("cp #{auth_keys_sav_path} #{auth_keys_path}")
  target.run("rm #{auth_keys_sav_path}")
end

When(/^I add "([^"]*)" calendar file as url$/) do |file|
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/srv/www/htdocs/pub/#{file}"
  return_code = file_inject(get_target('server'), source, dest)
  raise ScriptError, 'File injection failed' unless return_code.zero?

  get_target('server').run("chmod 644 #{dest}")
  url = "https://#{get_target('server').full_hostname}/pub/" + file
  log "URL: #{url}"
  step %(I enter "#{url}" as "calendar-data-text")
end

When(/^I deploy testing playbooks and inventory files to "([^"]*)"$/) do |host|
  target = get_target(host)
  dest = '/srv/playbooks/orion_dummy/'
  target.run("mkdir -p #{dest}")
  source = "#{File.dirname(__FILE__)}/../upload_files/ansible/playbooks/orion_dummy/playbook_orion_dummy.yml"
  return_code = file_inject(target, source, "#{dest}playbook_orion_dummy.yml")
  raise ScriptError, 'File injection failed' unless return_code.zero?

  source = "#{File.dirname(__FILE__)}/../upload_files/ansible/playbooks/orion_dummy/hosts"
  return_code = file_inject(target, source, "#{dest}hosts")
  raise ScriptError, 'File injection failed' unless return_code.zero?

  source = "#{File.dirname(__FILE__)}/../upload_files/ansible/playbooks/orion_dummy/file.txt"
  return_code = file_inject(target, source, "#{dest}file.txt")
  raise ScriptError, 'File injection failed' unless return_code.zero?

  dest = '/srv/playbooks/'
  source = "#{File.dirname(__FILE__)}/../upload_files/ansible/playbooks/playbook_ping.yml"
  return_code = file_inject(target, source, "#{dest}playbook_ping.yml")
  raise ScriptError, 'File injection failed' unless return_code.zero?
end

When(/^I enter the reactivation key of "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  node_id = $api_test.system.retrieve_server_id(system_name)
  react_key = $api_test.system.obtain_reactivation_key(node_id)
  log "Reactivation Key: #{react_key}"
  step %(I enter "#{react_key}" as "reactivationKey")
end
