# Copyright (c) 2010-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps used to lock packages on a system.

Then(/^"(.*?)" is (locked|unlocked) on "(.*?)"$/) do |pkg, action, system|
  node = get_target(system)
  command = "zypper locks  --solvables | grep #{pkg}"
  if action == 'locked'
    node.run(command, timeout: 600)
  else
    node.run(command, check_errors: false, timeout: 600)
  end
end

Then(/^package "(.*?)" is reported as locked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, '//i[@class=\'fa fa-lock\']/../a')
  raise ScriptError, 'No packages locked' if locked_pkgs.empty?
  raise ScriptError, "Package #{pkg} not found as locked" unless locked_pkgs.find { |a| a.text.match?(/^#{pkg}/) }
end

Then(/^package "(.*?)" is reported as unlocked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, '//i[@class=\'fa fa-lock\']/../a')

  raise ScriptError, "Package #{pkg} found as locked" if locked_pkgs.find { |a| a.text.match?(/^#{pkg}/) }
end

Then(/^the package scheduled is "(.*?)"$/) do |pkg|
  match = find(:xpath, '//li[@class=\'list-group-item\']//li')

  raise ScriptError, 'List of packages not found' unless match
  raise ScriptError, "Package #{pkg} not found" unless match.text.match?(/^#{pkg}/)
end

Then(/^the action status is "(.*?)"$/) do |status|
  step %(I should see a "This action's status is: #{status}" text)
end

Then(/^package "(.*?)" is reported as pending to be locked$/) do |pkg|
  xpath_query = "//td[a[text()='#{pkg}'] and i[@class='fa fa-clock-o'] and span[@class='label label-info' and contains(text(), 'Locking...')]]"
  find(:xpath, xpath_query)
end

Then(/^package "(.*?)" is reported as pending to be unlocked$/) do |pkg|
  xpath_query = "//td[a[text()='#{pkg}'] and i[@class='fa fa-clock-o'] and span[@class='label label-info' and contains(text(), 'Unlocking...')]]"
  find(:xpath, xpath_query)
end

Then(/^package "(.*?)" cannot be selected$/) do |pkg|
  xpath_query = "//tr[td[input[@type='checkbox' and @disabled]] and td[ a[text()='#{pkg}'] and i[@class='fa fa-clock-o'] and span[@class='label label-info']]]"
  find(:xpath, xpath_query)
end

Then(/^only packages "(.*?)" are reported as pending to be unlocked$/) do |pkgs|
  pkgs = pkgs.split(',').map(&:strip)

  # ensure these packages are found as pending to be unlocked
  pkgs.each do |pkg|
    xpath_query = "//td[#{"a[text()='#{pkg}'] and " \
      "i[@class='fa fa-clock-o'] and " \
      "span[@class='label label-info' and contains(text(), 'Unlocking...')]]"}"
    find(:xpath, xpath_query)
  end

  # ensure no other packages are set as pending to be unlocked
  xpath_query = "//td[i[@class='fa fa-clock-o'] and span[@class='label label-info' and contains(text(), 'Unlocking...')]]"
  matches = all(:xpath, xpath_query)

  raise ScriptError, "Matches count #{matches.size} is different than packages count #{pkgs.size}" if matches.size != pkgs.size
end
