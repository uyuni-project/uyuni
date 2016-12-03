# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

require 'timeout'

When(/^I list packages with "(.*?)"$/) do |str|
  find('input#package-search').set(str)
  find('button#search').click
end

When(/^I change the state of "([^"]*)" to "([^"]*)" and "([^"]*)"$/) do |pkg, state, instd_state|
  # Options for state are Installed, Unmanaged and Removed
  # Options for instd_state are Any or Latest
  # Default if you pick Installed is Latest
  find("##{pkg}-pkg-state").select(state)
  if !instd_state.to_s.empty? && state == 'Installed'
    find("##{pkg}-version-constraint").select(instd_state)
  end
end

Then(/^"([^"]*)" is not installed$/) do |package|
  uninstalled = false
  output = ""
  begin
    Timeout.timeout(120) do
      loop do
        output, code = $minion.run("rpm -q #{package}", false)
        if code.nonzero?
          uninstalled = true
          sleep 15
          break
        end
        sleep 1
      end
    end
  end
  raise "exec rpm removal failed (Code #{$?}): #{$!}: #{output}" unless uninstalled
end

Then(/^I wait for "([^"]*)" to be installed$/) do |package|
  installed = false
  output = ""
  begin
    Timeout.timeout(120) do
      loop do
        output, code = $minion.run("rpm -q #{package}", false)
        if code.zero?
          installed = true
          sleep 15
          break
        end
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "exec rpm installation failed: timeout"
  end
  raise "exec rpm installation failed (Code #{$?}): #{$!}: #{output}" unless installed
end

When(/^I click undo for "(.*?)"$/) do |pkg|
  find("button##{pkg}-undo").click
end

When(/^I click apply$/) do
  find('button#apply').click
end

When(/^I click save$/) do
  find('button#save').click
end

When(/^I click system$/) do
  find('button#system').click
end
