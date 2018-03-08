# Copyright (c) 2010-2018 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'pathname'

Given(/^I am on the Systems page$/) do
  step %(I am authorized)
  within(:xpath, "//header") do
    find_link("Systems").click
  end
end

Given(/cobblerd is running/) do
  ct = CobblerTest.new
  unless ct.is_running
    raise "cobblerd is not running"
  end
end

Then(/create distro "([^"]*)" as user "([^"]*)" with password "([^"]*)"/) do |arg1, arg2, arg3|
  ct = CobblerTest.new
  ct.login(arg2, arg3)
  if ct.distro_exists(arg1)
    raise "distro " + arg1 + " already exists"
  end
  ct.distro_create(arg1, "/install/SLES11-SP1-x86_64/DVD1/boot/x86_64/loader/linux", "install/SLES11-SP1-x86_64/DVD1/boot/x86_64/loader/initrd")
end

Given(/distro "([^"]*)" exists/) do |arg1|
  ct = CobblerTest.new
  unless ct.distro_exists(arg1)
    raise "distro " + arg1 + " does not exist"
  end
end

Then(/create profile "([^"]*)" as user "([^"]*)" with password "([^"]*)"/) do |arg1, arg2, arg3|
  ct = CobblerTest.new
  ct.login(arg2, arg3)
  if ct.profile_exists(arg1)
    raise "profile " + arg1 + " already exists"
  end
  ct.profile_create("testprofile", "testdistro", "/install/empty.xml")
end

Given(/profile "([^"]*)" exists/) do |arg1|
  ct = CobblerTest.new
  unless ct.profile_exists(arg1)
    raise "profile " + arg1 + " does not exist"
  end
end

When(/^I attach the file "(.*)" to "(.*)"$/) do |path, field|
  canonical_path = Pathname.new(File.join(File.dirname(__FILE__), '/../upload_files/', path)).cleanpath
  attach_file(field, canonical_path)
end

When(/I view system with id "([^"]*)"/) do |arg1|
  visit Capybara.app_host + "/rhn/systems/details/Overview.do?sid=" + arg1
end
