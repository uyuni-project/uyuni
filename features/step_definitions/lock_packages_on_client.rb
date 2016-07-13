# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Then(/^"(.*?)" is locked on this client$/) do |pkg|
  zypp_lock_file = "/etc/zypp/locks"
  fail unless file_exist($client, zypp_lock_file)
  command = "zypper locks  --solvables | grep #{pkg}"
  out, _local, _remote, code = $client.test_and_store_results_together(command, "root", 600)
  puts out
  if code != 0
     puts out
     raise "Package is not locked on client #{out}"
  end
end

Then(/^Package "(.*?)" is reported as locked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")

  fail if locked_pkgs.empty?
  fail unless locked_pkgs.find { |a| a.text =~ /^#{pkg}/ }
end

Then(/^"(.*?)" is unlocked on this client$/) do |pkg|
  zypp_lock_file = "/etc/zypp/locks"
  fail unless file_exist($client, zypp_lock_file)

  locks = read_zypp_lock_file(zypp_lock_file)
  fail if locks.find { |lock| pkg =~ /^#{lock['solvable_name']}/ }
end

Then(/^Package "(.*?)" is reported as unlocked$/) do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")

  fail if locked_pkgs.find { |a| a.text =~ /^#{pkg}/ }
end

Then(/^The package scheduled is "(.*?)"$/) do |pkg|
  match = find(:xpath, "//li[@class='list-group-item']//li")

  fail unless match
  fail unless match.text =~ /^#{pkg}/
end

Then(/^The action status is "(.*?)"$/) do |status|
  step %(I should see a "This action's status is: #{status}" text)
end

Then(/^Package "(.*?)" is reported as pending to be locked$/) do |pkg|
  xpath_query = "//td[" + # Looking for 1 td element with...
    "a[text()='#{pkg}'] and " + # a link with a certain text
    "i[@class='fa fa-clock-o'] and " + # and the clock icon
    "span[@class='label label-info' and contains(text(), 'Locking...')]]" # and the "Locking..." label
  find(:xpath, xpath_query)
end

Then(/^Package "(.*?)" is reported as pending to be unlocked$/) do |pkg|
  xpath_query = "//td[" + # Looking for 1 td element with...
    "a[text()='#{pkg}'] and " + # a link with a certain text
    "i[@class='fa fa-clock-o'] and " + # and the clock icon
    "span[@class='label label-info' and contains(text(), 'Unlocking...')]]" # and the "Unlocking..." label
  find(:xpath, xpath_query)
end

Then(/^Package "(.*?)" cannot be selected$/) do |pkg|
  xpath_query = "//tr[" + # Looking for 1 table row where...
    "td[input[@type='checkbox' and @disabled]] and " + # the checkbox is disabled and...
    "td[ " + # there's another td which contains
      "a[text()='#{pkg}'] and " + # a link with a certain text
      "i[@class='fa fa-clock-o'] and " + # and the clock icon
      "span[@class='label label-info']" + # and the "Locking..."/"Unlocking" label
    "]]"
  find(:xpath, xpath_query)
end

Then(/^Only packages "(.*?)" are reported as pending to be unlocked$/) do |pkgs|
  pkgs = pkgs.split(",").map(&:strip)

  # ensure these packages are found as pending to be unlocked
  pkgs.each do |pkg|
    xpath_query = "//td[" + # Looking for 1 td element with...
      "a[text()='#{pkg}'] and " + # a link with a certain text
      "i[@class='fa fa-clock-o'] and " + # and the clock icon
      "span[@class='label label-info' and contains(text(), 'Unlocking...')]]" # and the "Unlocking..." label
    find(:xpath, xpath_query)
  end

  # ensure no other packages are set as pending to be unlocked
  xpath_query = "//td[" + # Looking for td elements with...
    "i[@class='fa fa-clock-o'] and " + # and the clock icon
    "span[@class='label label-info' and contains(text(), 'Unlocking...')]]" # and the "Unlocking..." label
  matches =  all(:xpath, xpath_query)

  fail if matches.size != pkgs.size
end

When(/^I select all the packages$/) do
  within(:xpath, "//section") do
    # use div/div/div for cve audit which has two tables
    top_level_xpath_query = "//div[@class='table-responsive']/table/thead/tr[.//input[@type='checkbox']]"
    row = first(:xpath, top_level_xpath_query)
    if row.nil?
      sleep 1
      $stderr.puts "ERROR - try again"
      row = first(:xpath, top_level_xpath_query)
    end
    row.first(:xpath, ".//input[@type=\"checkbox\"]").set(true)
  end
end
