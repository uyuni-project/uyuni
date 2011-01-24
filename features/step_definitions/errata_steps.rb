# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the errata page$/ do
  Given "I am authorized"
  within(:xpath, "//div[@id=\"mainNavWrap\"]") do
    find_link("Patches").click
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
  linknames = []
  if arch != "x86_64"
    linknames = [ "slessp1-kernel-3284-channel-ia32",
                  "slessp1-suseRegister-2953-channel-ia32",
                  "slessp1-aaa_base-sysvinit-2610-channel-ia32"]
  else
    linknames = [ "slessp1-kernel-3280-channel-x86_64",
                  "slessp1-suseRegister-2953-channel-x86_64",
                  "slessp1-aaa_base-sysvinit-2610-channel-x86_64"]
  end
  linknames.each() do |link|
    within(:xpath, "//form/table/tbody/tr") do
      fail if not find_link(link).visible?
    end
  end
end
