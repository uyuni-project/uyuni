# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Then /^I should see something$/ do
  steps %[
    Given I should see a "Sign In" text
    And I should see a "About" text
  ]
end

Then /^I should see "([^"]*)" systems selected for SSM$/ do |arg|
  within(:xpath, "//span[@id=\"spacewalk-set-system_list-counter\"]") do
    fail if not has_content?(arg)
  end
end

#
# Test for a text in the whole page
#
Then /^I should see a "([^"]*)" text$/ do |arg1|
  if not page.has_content?(debrand_string(arg1))
    sleep 1
    fail if not page.has_content?(debrand_string(arg1))
  end
end

#
# Test for text in a snippet textarea
#
Then /^I should see "([^"]*)" in the textarea$/ do |arg1|
  within('textarea') do
    fail if not page.has_content?(debrand_string(arg1))
  end
end

Then /^I should see that this system has been deleted$/ do
  system_id = client_system_id_to_i
  step %[I should see a "System profile #{system_id} has been deleted." text]
end

#
# Test for a text in the whole page using regexp
#
Then /^I should see a text like "([^"]*)"$/ do |arg1|
  if not page.has_content?(Regexp.new("#{debrand_string(arg1)}"))
    sleep 1
    fail if not page.has_content?(Regexp.new("#{debrand_string(arg1)}"))
  end
end


#
# Test for a text not allowed in the whole page
#
Then /^I should not see a "([^"]*)" text$/ do |arg1|
  fail unless page.has_no_content?(arg1)
end

#
# Test for a visible link in the whole page
#
Then /^I should see a "([^"]*)" link$/ do |arg1|
  link = first(:link, debrand_string(arg1))
  if link.nil?
    sleep 1
    $stderr.puts "ERROR - try again"
    fail if not first(:link, debrand_string(arg1)).visible?
  else
    fail if not link.visible?
  end
end

#
# Validate link is gone
#
Then /^I should not see a "([^"]*)" link$/ do |arg1|
  fail if not page.has_no_link?(arg1)
end


Then /^I should see a "([^"]*)" button$/ do |arg1|
  fail if not find_button(arg1).visible?
end

Then(/^I should see a "(.*?)" link in the text$/) do |linktext, text|
  within(:xpath, "//p/strong[contains(normalize-space(string(.)), '#{text}')]") do
    assert has_xpath?("//a[text() = '#{linktext}']")
  end
end

#
# Test for a visible link inside of a <div> with the attribute
# "class" or "id" of the given name
#
Then /^I should see a "([^"]*)" link in element "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    fail if not find_link(debrand_string(arg1)).visible?
  end
end

Then /^I should not see a "([^"]*)" link in element "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
      fail if not has_no_link?(debrand_string(arg1))
  end
end

Then /^I should see a "([^"]*)" text in element "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    fail if not has_content?(debrand_string(arg1))
  end
end

Then /^I should see a "([^"]*)" link in "([^"]*)" "([^"]*)"$/ do |arg1, arg2, arg3|
  fail if not page.has_xpath?("//#{arg2}[@id='#{arg3}' or @class='#{arg3}']/a[text()='#{debrand_string(arg1)}']")
end

Then /^I should see a "([^"]*)" link in the table (.*) column$/ do |link, column|
  idx = ['first', 'second', 'third', 'fourth'].index(column)
  unless idx
    # try column by name
    # unquote if neeeded
    colname = column.gsub(/\A['"]+|['"]+\Z/, '')
    cols = page.all(:xpath, '//table//thead/tr[1]/th').map(&:text)
    idx = cols.index(colname)
  end
  fail("Unknown column '#{column}'") unless idx
  #find(:xpath, "//table//thead//tr/td[#{idx + 1}]/a[text()='#{link}']")
  fail unless page.has_xpath?("//table//tr/td[#{idx + 1}]//a[text()='#{link}']")
end

Then /^I should see a "([^"]*)" link in the (left menu|tab bar|tabs|content area)$/ do |arg1, arg2|
  tag = case arg2
  when /left menu/ then "aside"
  when /tab bar|tabs/ then "header"
  when /content area/ then "section"
  else raise "Unknown element with description '#{arg2}'"
  end

  within(:xpath, "//#{tag}") do
    step "I should see a \"#{arg1}\" link"
  end
end

Then /^I should not see a "([^"]*)" link in the (.+)$/ do |arg1, arg2|
  tag = case arg2
  when /left menu/ then "aside"
  when /tab bar|tabs/ then "header"
  when /content area/ then "section"
  else raise "Unknown element with description '#{arg2}'"
  end

  within(:xpath, "//#{tag}") do
    step "I should not see a \"#{arg1}\" link"
  end
end

Then /^I should see a "([^"]*)" link in row ([0-9]+) of the content menu$/ do |arg1, arg2|
  within(:xpath, "//section") do
    within(:xpath, "//div[@class=\"spacewalk-content-nav\"]/ul[#{arg2}]") do
      step %[I should see a "#{arg1}" link]
    end
  end
end


Then /^I should see a "([^"]*)" link in list "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//ul[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    fail if not find_link(arg1).visible?
  end
end

Then /^I should see a "([^"]*)" button in "([^"]*)" form$/ do |arg1, arg2|
  within(:xpath, "//form[@id='#{arg2}' or @name=\"#{arg2}\"]") do
    fail if not find_button(arg1)
  end
end

Then /^I select the "([^"]*)" repo$/ do |arg1|
  within(:xpath, "//a[text()='#{arg1}']/../..") do
    first('input[type=checkbox]').set(true)
  end
end

Then /^I check the row with the "([^"]*)" link$/ do |arg1|
  within(:xpath, "//a[text()='#{arg1}']/../..") do
    first('input[type=checkbox]').set(true)
  end
end

Then /^I should see (\d+) "([^"]*)" links$/ do |count, text|
  page.all('a', :text => text, :count => count)
end
#
# Test if an option is selected
#
Then /^Option "([^"]*)" is selected as "([^"]*)"$/ do |arg1, arg2|
  fail if not has_select?(arg2, :selected => arg1)
end

#
# Test if a radio button is checked
#
Then /^radio button "([^"]*)" is checked$/ do |arg1|
  fail if not has_checked_field?(arg1)
end

#
# Test if a checkbox is checked
#
Then /^I should see "([^"]*)" as checked$/ do |arg1|
  fail if not has_checked_field?(arg1)
end

#
# Test if a checkbox is unchecked
#
Then /^I should see "([^"]*)" as unchecked$/ do |arg1|
  fail if not has_unchecked_field?(arg1)
end

#
# Test if a checkbox is disabled
#
Then /^the "([^\"]*)" checkbox should be disabled$/ do |arg1|
  page.has_css?("##{arg1}[disabled]")
end

Then /^the "([^\"]*)" field should be disabled$/ do |arg1|
  page.has_css?("##{arg1}[disabled]")
end

Then /^I should see "([^"]*)" in field "([^"]*)"$/ do |arg1, arg2|
  fail if not page.has_field?(arg2, :with => arg1)
end

Then /^I should see a "([^"]*)" element in "([^"]*)" form$/ do |arg1, arg2|
  within(:xpath, "//form[@id=\"#{arg2}\"] | //form[@name=\"#{arg2}\"]") do
    fail if not find_field(arg1, :match => :first).visible?
  end
end

Then /^I should see a "([^"]*)" editor in "([^"]*)" form$/ do |arg1, arg2|
  within(:xpath, "//form[@id=\"#{arg2}\"] | //form[@name=\"#{arg2}\"]") do
    fail if not page.find("textarea##{arg1}", :visible => false)
    fail if not page.has_css?("##{arg1}-editor")
  end
end

Then /^"([^"]*)" is installed$/ do |package|
  output = `rpm -q #{package} 2>&1`
  if ! $?.success?
    raise "exec rpm failed (Code #{$?}): #{$!}: #{output}"
  end
end

Then /^I should see a Sign Out link$/ do
  fail if not has_xpath?("//a[@href='/rhn/Logout.do']")
end

When /^I check "([^"]*)" in the list$/ do |arg1|
  within(:xpath, "//section") do
      # use div/div/div for cve audit which has two tables
      row = first(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]")
      if row.nil?
          sleep 1
          $stderr.puts "ERROR - try again"
          row = first(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]")
      end
      row.first(:xpath, ".//input[@type=\"checkbox\"]").set(true)
  end
end

When /^I uncheck "([^"]*)" in the list$/ do |arg1|
  within(:xpath, "//section") do
    # use div/div/div for cve audit which has two tables
    top_level_xpath_query = "//div[@class='table-responsive']/table/tbody/tr[.//td[contains(.,'#{arg1}')] and .//input[@type='checkbox' and @checked]]"
    row = first(:xpath, top_level_xpath_query)
    if row.nil?
      sleep 1
      $stderr.puts "ERROR - try again"
      row = first(:xpath, top_level_xpath_query)
    end
    row.first(:xpath, ".//input[@type=\"checkbox\"]").set(false)
  end
end

Then /^The table should have a column named "([^"]+)"$/ do |arg1|
  find(:xpath, "//div[@class=\"table-responsive\"]/table/thead[.//th[contains(.,'#{arg1}')]]")
end

Then /^I should see (\d+) "([^"]*)" fields in "([^"]*)" form$/ do |count, name, id|
    within(:xpath, "//form[@id=\"#{id}\" or  @name=\"#{id}\"]") do
        fail if not has_field?(name, :count => count.to_i)
    end
end
