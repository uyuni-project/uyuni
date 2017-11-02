# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Then(/^"(.*?)" is locked on this client$/) do |pkg|
  zypp_lock_file = '/etc/zypp/locks'
  raise unless file_exists?($client, zypp_lock_file)
  command = "zypper locks  --solvables | grep #{pkg}"
  $client.run(command, true, 600, 'root')
end

Then(/^package "(.*?)" is reported as locked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")
  raise if locked_pkgs.empty?
  raise unless locked_pkgs.find { |a| a.text =~ /^#{pkg}/ }
end

Then(/^"(.*?)" is unlocked on this client$/) do |pkg|
  zypp_lock_file = '/etc/zypp/locks'
  raise unless file_exists?($client, zypp_lock_file)
  command = "zypper locks  --solvables | grep #{pkg}"
  $client.run(command, false, 600, 'root')
end

Then(/^package "(.*?)" is reported as unlocked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")

  raise if locked_pkgs.find { |a| a.text =~ /^#{pkg}/ }
end

Then(/^the package scheduled is "(.*?)"$/) do |pkg|
  match = find(:xpath, "//li[@class='list-group-item']//li")

  raise unless match
  raise unless match.text =~ /^#{pkg}/
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

  raise if matches.size != pkgs.size
end

When(/^I select all the packages$/) do
  within(:xpath, '//section') do
    # use div/div/div for cve audit which has two tables
    top_level_xpath_query = "//div[@class='table-responsive']/table/thead/tr[.//input[@type='checkbox']]"
    row = first(:xpath, top_level_xpath_query)
    if row.nil?
      sleep 1
      $stderr.puts 'ERROR - try again'
      row = first(:xpath, top_level_xpath_query)
    end
    row.first(:xpath, './/input[@type="checkbox"]').set(true)
  end
end
