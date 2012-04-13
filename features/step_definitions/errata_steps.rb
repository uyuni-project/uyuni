# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the errata page$/ do
  Given "I am authorized"
  within(:xpath, "//div[@id=\"mainNavWrap\"]") do
    find_link(debrand_string("Errata")).click
  end
end

Given /^I am on the "([^"]*)" errata Details page$/ do |arg1|
  Given "I am on the errata page"
    And "I follow \"All\" in the left menu"
    And "I follow \"#{arg1}\""
end

Then /^I should see three links to the errata in the list$/ do
  arch=`uname -m`
  arch.chomp!
  linknames = [ "slessp1-suseRegister-2953",
		"slessp1-aaa_base-sysvinit-2610"]
  if arch != "x86_64"
      linknames << "slessp1-kernel-3284"
  else
      linknames << "slessp1-kernel-3280"
  end
  linknames.each() do |link|
    fail if not has_xpath?("//form/table/tbody/tr/td/a[contains(.,'#{link}')]")
  end
end
