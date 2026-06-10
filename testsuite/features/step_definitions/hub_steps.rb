# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning Hub operations.

require 'xmlrpc/client'

# Hub XMLRPC API authentication and operations

Given(/^I am connected to the hub XMLRPC API$/) do
  hub_host = get_target('server').full_hostname
  $hub_api = NamespaceHub.new(hub_host)
  response = $hub_api.login_with_autoconnect('admin', 'admin')
  raise ScriptError, 'Hub login failed' if response['SessionKey'].nil?
end

When(/^I call hub\.listServerIds via XMLRPC$/) do
  $hub_api.list_server_ids
end

Then(/^"([^"]*)" should be in the server IDs list$/) do |host|
  raise ScriptError, "#{host} not found in hub server IDs" if $hub_api.server_ids.empty?

  log "Hub server IDs: #{$hub_api.server_ids}"
end

When(/^I call multicast\.system\.list_systems via XMLRPC$/) do
  $multicast_response = $hub_api.multicast_system_list
end

Then(/^multicast response should have successful responses$/) do
  raise ScriptError, 'Multicast response missing Successful key' unless $multicast_response.key?('Successful')
  raise ScriptError, 'No successful responses' if $multicast_response['Successful']['Responses'].nil?

  log "Multicast successful responses: #{$multicast_response['Successful']['Responses'].length}"
end

Then(/^multicast response should contain systems from "([^"]*)"$/) do |host|
  successful = $multicast_response['Successful']['Responses']
  system_found = successful.any? { |response| !response.empty? }
  raise ScriptError, "No systems found in multicast response for #{host}" unless system_found
end

When(/^I logout from hub XMLRPC API$/) do
  $hub_api.logout
end

# Hub UI operations for peripheral registration

When(/^I add peripheral "([^"]*)" with credentials to hub via UI$/) do |host|
  peripheral_node = get_target(host)
  steps %(
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I click on "Add peripheral"
    And I wait until I see "Peripheral FQDN" text
    And I enter "#{peripheral_node.full_hostname}" as "peripheral_fqdn"
    And I select "Administrator User/Password" from "registration_mode"
    And I enter "admin" as "peripheral_username"
    And I enter "admin" as "peripheral_password"
    And I click on "Add Peripheral"
  )
end

Then(/^"([^"]*)" should appear in peripherals list$/) do |host|
  peripheral_node = get_target(host)
  steps %(
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I wait until I see "#{peripheral_node.full_hostname}" text
  )
end

Then(/^connection status for "([^"]*)" should be "([^"]*)"$/) do |host, status|
  peripheral_node = get_target(host)
  xpath = "//tr[contains(., '#{peripheral_node.full_hostname}')]//td[contains(., '#{status}')]"
  raise ScriptError, "Connection status '#{status}' not found for #{host}" unless has_xpath?(xpath)
end

# Hub channel synchronization

When(/^I configure hub to sync channel "([^"]*)" to "([^"]*)"$/) do |channel, host|
  peripheral_node = get_target(host)
  steps %(
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I follow "#{peripheral_node.full_hostname}"
    And I follow "Edit channels"
    And I check "#{channel}" in the list
    And I click on "Update"
    And I wait until I see "Channel configuration updated" text
  )
end

When(/^I select target organization "([^"]*)" for channel "([^"]*)" on "([^"]*)"$/) do |org, channel, _host|
  select org, from: "org_#{channel}"
end

When(/^I trigger channel sync from hub to "([^"]*)"$/) do |host|
  peripheral_node = get_target(host)
  steps %(
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I follow "#{peripheral_node.full_hostname}"
    And I click on "Sync Channels"
    And I click on "Confirm"
  )
end

When(/^I initiate channel sync from peripheral "([^"]*)"$/) do |host|
  steps %(
    Given I am on the Systems overview page of this "#{host}"
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Hub Details"
    And I click on "Sync Channels"
    And I click on "Confirm"
  )
end

Then(/^channel "([^"]*)" should exist on "([^"]*)"$/) do |channel, host|
  node = get_target(host)
  _result, code = node.run("spacecmd -u admin -p admin -- softwarechannel_list | grep -q '#{channel}'", check_errors: false)
  raise ScriptError, "Channel #{channel} not found on #{host}" unless code.zero?
end

Then(/^channel "([^"]*)" on "([^"]*)" should have "([^"]*)" packages?$/) do |channel, host, pkg_count|
  node = get_target(host)
  output, _code = node.run("spacecmd -u admin -p admin -- softwarechannel_listallpackages #{channel} | wc -l")
  actual_count = output.strip.to_i
  expected_count = pkg_count.to_i
  raise ScriptError, "Expected #{expected_count} packages, found #{actual_count}" unless actual_count >= expected_count
end

# Hub cleanup operations

When(/^I remove synced channels from "([^"]*)"$/) do |host|
  peripheral_node = get_target(host)
  steps %(
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I follow "#{peripheral_node.full_hostname}"
    And I follow "Edit channels"
    And I uncheck all channels
    And I click on "Update"
  )
end

When(/^I unregister "([^"]*)" from hub$/) do |host|
  peripheral_node = get_target(host)
  steps %(
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I follow "#{peripheral_node.full_hostname}"
    And I follow "Unregister"
    And I click on "Confirm"
    And I wait until I do not see "#{peripheral_node.full_hostname}" text
  )
end

# Hub service verification

Then(/^the Hub XMLRPC API should be running on "([^"]*)"$/) do |host|
  node = get_target(host)
  _result, code = node.run('curl -k https://localhost/hub/rpc/api -o /dev/null -s -w "%{http_code}"', check_errors: false)
  raise ScriptError, 'Hub XMLRPC API is not accessible' unless code.zero?
end

# Channel sync waiting

When(/^I wait at most (\d+) seconds until channel "([^"]*)" has been synced on "([^"]*)"$/) do |timeout, channel, host|
  node = get_target(host)
  repeat_until_timeout(timeout: timeout.to_i, message: "Channel #{channel} not synced on #{host}") do
    _result, code = node.run("spacecmd -u admin -p admin -- softwarechannel_list | grep -q '#{channel}'", check_errors: false)
    break if code.zero?

    sleep 10
  end
end
