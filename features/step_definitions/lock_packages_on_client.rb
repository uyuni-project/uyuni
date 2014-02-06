# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Then /^"(.*?)" is locked on this client$/ do |pkg|
  zypp_lock_file = "/etc/zypp/locks"
  fail unless File.exists?(zypp_lock_file)

  locks = read_zypp_lock_file(zypp_lock_file)
  fail unless locks.find{|lock| pkg =~ /^#{lock['solvable_name']}/ }
end

Then /^Package "(.*?)" is reported as locked$/ do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")

  fail if locked_pkgs.empty?
  fail unless locked_pkgs.find{|a| a.text =~ /^#{pkg}/}
end

Then /^"(.*?)" is unlocked on this client$/ do |pkg|
  zypp_lock_file = "/etc/zypp/locks"
  fail unless File.exists?(zypp_lock_file)

  locks = read_zypp_lock_file(zypp_lock_file)
  fail if locks.find{|lock| pkg =~ /^#{lock['solvable_name']}/ }
end

Then /^Package "(.*?)" is reported as unlocked$/ do |pkg|
  find(:xpath, "(//a[text()='#{pkg}'])[1]")
  locked_pkgs = all(:xpath, "//i[@class='fa fa-lock']/../a")

  fail if locked_pkgs.find{|a| a.text =~ /^#{pkg}/}
end

Then /^The package scheduled is "(.*?)"$/ do |pkg|
  match = find(:xpath, "//li[@class='action-summary-package-nvre']")

  fail unless match
  fail unless match.text =~ /^#{pkg}/
end

Then /^The action status is "(.*?)"$/ do |status|
  match = find(:xpath, "//td[@class='action-summary-details']")

  fail unless match
  fail unless match.text.include?("This action's status is: #{status}")
end

