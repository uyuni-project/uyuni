# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
#
# Initial step for channel testing
#
Given(/^I am testing configuration$/) do
  steps %[
    Given I am authorized
    And I follow "Configuration"
  ]
end

When(/^I change the local file "([^"]*)" to "([^"]*)"$/) do |filename, content|
    out, local, remote, code = $client.test_and_store_results_together("echo \"#{content}\" > #{filename}", "root", 600)
    puts out
    if code != 0
      raise "Execute command failed #{out} !"
    end
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/) do |arg1, arg2, arg3|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
      fail if not find_link("#{arg3}")
  end
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
  end
end

Then(/^On this client the File "([^"]*)" should exists$/) do |arg1|
    out, local, remote, code = $client.test_and_store_results_together("test -f #{arg1}", "root", 600)
    puts out
    if code != 0
      raise "Execute command failed #{out} !"
    end

end

Then(/^On this client the File "([^"]*)" should have the content "([^"]*)"$/) do |filename, content|
    out, local, remote, code = $client.test_and_store_results_together("test -f #{filename}", "root", 600)
    puts out
    if code != 0
      raise "Execute command failed #{out} !"
    end
    # Example: 
    #And On this client the File "/etc/mgr-test-file.cnf" should have the content "MGR_PROXY=yes"'
    out, local, remote, code = $client.test_and_store_results_together("grep #{content} #{filename}", "root", 600)
    puts out
    if code != 0
      raise "content #{content} not found #{out} !"
    end
end

When(/^I enable all actions$/) do
   command = "rhn-actions-control --enable-all"
   out , local, remote, code = $client.test_and_store_results_together(command, "root", 600)
   puts out
   if code != 0
     raise "Execute command failed #{out} !"
   end
end
