# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Then(/^"(.*?)" is locked on this client$/) do |pkg|
  zypp_lock_file = '/etc/zypp/locks'
  raise "File already exist: #{zypp_lock_file}" unless file_exists?($client, zypp_lock_file)
  command = "zypper locks  --solvables | grep #{pkg}"
  $client.run(command, true, 600, 'root')
end

Then(/^package "(.*?)" is reported as locked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")
  raise 'No packages locked' if locked_pkgs.empty?
  raise "Package #{pkg} not found as locked" unless locked_pkgs.find { |a| a.text =~ /^#{pkg}/ }
end

Then(/^"(.*?)" is unlocked on this client$/) do |pkg|
  zypp_lock_file = '/etc/zypp/locks'
  raise "File #{zypp_lock_file} not found" unless file_exists?($client, zypp_lock_file)

  command = "zypper locks  --solvables | grep #{pkg}"
  $client.run(command, false, 600, 'root')
end

Then(/^package "(.*?)" is reported as unlocked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")

  raise "Package #{pkg} found as locked" if locked_pkgs.find { |a| a.text =~ /^#{pkg}/ }
end

Then(/^the package scheduled is "(.*?)"$/) do |pkg|
  match = find(:xpath, "//li[@class='list-group-item']//li")

  raise 'List of packages not found' unless match
  raise "Package #{pkg} not found" unless match.text =~ /^#{pkg}/
end

Then(/^the action status is "(.*?)"$/) do |status|
  step %(I should see a "This action's status is: #{status}" text)
end

Then(/^package "(.*?)" is reported as pending to be locked$/) do |pkg|
  xpath_query = '//td[' \
                "a[text()='#{pkg}'] and " \
                "i[@class='fa fa-clock-o'] and " \
                "span[@class='label label-info' and contains(text(), 'Locking...')]]"
  find(:xpath, xpath_query)
end

Then(/^package "(.*?)" is reported as pending to be unlocked$/) do |pkg|
  xpath_query = '//td[' \
                "a[text()='#{pkg}'] and " \
                "i[@class='fa fa-clock-o'] and " \
                "span[@class='label label-info' and contains(text(), 'Unlocking...')]]"
  find(:xpath, xpath_query)
end

Then(/^package "(.*?)" cannot be selected$/) do |pkg|
  xpath_query = '//tr[' \
                "td[input[@type='checkbox' and @disabled]] and " \
                'td[ ' \
                "a[text()='#{pkg}'] and " \
                "i[@class='fa fa-clock-o'] and " \
                "span[@class='label label-info']" \
                ']]'
  find(:xpath, xpath_query)
end

Then(/^only packages "(.*?)" are reported as pending to be unlocked$/) do |pkgs|
  pkgs = pkgs.split(',').map(&:strip)

  # ensure these packages are found as pending to be unlocked
  pkgs.each do |pkg|
    xpath_query = '//td[' + # Looking for 1 td element with...
                  "a[text()='#{pkg}'] and " \
                  "i[@class='fa fa-clock-o'] and " \
                  "span[@class='label label-info' and contains(text(), 'Unlocking...')]]"
    find(:xpath, xpath_query)
  end

  # ensure no other packages are set as pending to be unlocked
  xpath_query = '//td[' \
                "i[@class='fa fa-clock-o'] and " \
                "span[@class='label label-info' and contains(text(), 'Unlocking...')]]"
  matches = all(:xpath, xpath_query)

  raise "Matches count #{matches.size} is different than packages count #{pkgs.size}" if matches.size != pkgs.size
end
