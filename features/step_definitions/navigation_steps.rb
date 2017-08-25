# Copyright (c) 2010-2017 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Test the current path of the URL
#
Then(/^the current path is "([^"]*)"$/) do |arg1|
  raise unless current_path == arg1
end

When(/^I wait until i see "([^"]*)" text$/) do |text|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_content?(text)
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text} in webpage"
  end
end

When(/^I wait until i see "([^"]*)" text, refreshing the page$/) do |text|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_content?(text)
        sleep 3
        page.evaluate_script 'window.location.reload()'
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text} in webpage"
  end
end

When(/^I wait until I see the name of "([^"]*)", refreshing the page$/) do |target|
  node = get_target(target)
  step %(I wait until i see "#{node.full_hostname}" text, refreshing the page)
end

#
# Check a checkbox of the given id
#
When(/^I check "([^"]*)"$/) do |arg1|
  check(arg1)
end

When(/^I uncheck "([^"]*)"$/) do |arg1|
  uncheck(arg1)
end

When(/^I check "([^"]*)" if not checked$/) do |arg1|
  check(arg1) unless has_checked_field?(arg1)
end

When(/^I select "([^"]*)" from "([^"]*)"$/) do |arg1, arg2|
  select(arg1, from: arg2)
end

When(/^I choose "([^"]*)"$/) do |arg1|
  find(:xpath, "//input[@type='radio' and @value='#{arg1}']").set(true)
end

#
# Enter a text into a textfield
#
When(/^I enter "([^"]*)" as "([^"]*)"$/) do |arg1, arg2|
  fill_in arg2, with: arg1
end

When(/^I enter "(.*?)" as "(.*?)" in the content area$/) do |arg1, arg2|
  within(:xpath, '//section') do
    fill_in arg2, with: arg1
  end
end

#
# Click on a button
#
When(/^I click on "([^"]*)"$/) do |arg1|
  begin
    click_button arg1, match: :first
  rescue
    sleep 4
    click_button arg1, match: :first
  end
end
#
# Click on a button and confirm in alert box
When(/^I click on "([^"]*)" and confirm$/) do |arg1|
  accept_alert do
    step %(I click on "#{arg1}")
    sleep 1
  end
end
#
# Click on a link
#
When(/^I follow "([^"]*)"$/) do |text|
  begin
    click_link(text)
  rescue
    sleep 3
    click_link(text)
  end
end
#
# Click on the first link
#
When(/^I follow first "([^"]*)"$/) do |text|
  click_link(text, match: :first)
end

#
# Click on a link which appears inside of <div> with
# the given "id"
When(/^I follow "([^"]*)" in element "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    step %(I follow "#{arg1}")
  end
end

When(/^I want to add a new credential$/) do
  raise unless find('i.fa-plus-circle').click
end

When(/^I follow "([^"]*)" in the (.+)$/) do |arg1, arg2|
  tag = case arg2
        when /left menu/ then 'aside'
        when /tab bar|tabs/ then 'header'
        when /content area/ then 'section'
        else raise "Unknown element with description '#{desc}'"
        end
  within(:xpath, "//#{tag}") do
    step %(I follow "#{arg1}")
  end
end

When(/^I follow first "([^"]*)" in the (.+)$/) do |arg1, arg2|
  tag = case arg2
        when /left menu/ then 'aside'
        when /tab bar|tabs/ then 'header'
        when /content area/ then 'section'
        else raise "Unknown element with description '#{desc}'"
        end
  within(:xpath, "//#{tag}") do
    step "I follow first \"#{arg1}\""
  end
end

#
# Click on a link which appears inside of <div> with
# the given "class"
When(/^I follow "([^"]*)" in class "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"#{arg2}\"]") do
    step "I follow \"#{arg1}\""
  end
end

When(/^I follow "([^"]*)" on "(.*?)" row$/) do |text, host|
  node = get_target(host)
  xpath_query = "//tr[td[contains(.,'#{node.full_hostname}')]]//a[contains(., '#{text}')]"
  raise unless find(:xpath, xpath_query).click
end

When(/^I enter "(.*?)" in the editor$/) do |arg1|
  page.execute_script("ace.edit('contents-editor').setValue('#{arg1}')")
end

When(/^I click Systems, under Systems node$/) do
  find(:xpath, "//div[@id=\"nav\"]/nav/ul/li[contains(@class, 'active')
       and contains(@class, 'open')
       and contains(@class,'node')]/ul/li/div/a/span[contains(.,'Systems')]").click
end

Given(/^I am not authorized$/) do
  visit Capybara.app_host
  raise unless find_button('Sign In').visible?
end

When(/^I go to the home page$/) do
  visit Capybara.app_host
end

Given(/^I access the host the first time$/) do
  visit Capybara.app_host
  # fail if not page.has_content?("Create Spacewalk Administrator")
  raise unless page.has_content?('Create SUSE Manager Administrator')
end

# Admin Page steps
Given(/^I am on the Admin page$/) do
  steps %(
    When I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Setup Wizard"
    )
end

When(/^I am on the Organizations page$/) do
  steps %(
    When I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Organizations"
    )
end

# Credential Page steps
Given(/^I am on the Credentials page$/) do
  steps %(
    When I am authorized as "testing" with password "testing"
    And I follow "User Account"
    And I follow "Credentials"
    )
end

# access the multi-clients/minions
Given(/^I am on the Systems overview page of this "(.*?)"$/) do |target|
  node = get_target(target)
  steps %(
    Given I am on the Systems page
  )
  step %(I follow "#{node.full_hostname}")
end

When(/^I follow this "(.*?)" link$/) do |target|
  node = get_target(target)
  step %(I follow "#{node.full_hostname}")
end

When(/^I check the "(.*?)" client$/) do |target|
  node = get_target(target)
  step %(I check "#{node.full_hostname}" in the list)
end

Given(/^I am on the groups page$/) do
  steps %(
    Given I am on the Systems page
    And I follow "System Groups" in the left menu
    )
end

When(/^I check this client$/) do
  step %(I check "#{$client_fullhostname}" in the list)
end

Given(/^I am on the active Users page$/) do
  steps %(
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Users"
    And I follow "User List"
    And I follow "Active"
    )
end

Then(/^Table row for "([^"]*)" should contain "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//a[contains(.,'#{arg1}')]]") do
    raise unless has_content?(arg2)
  end
end

# login, logout steps

Given(/^I am authorized as "([^"]*)" with password "([^"]*)"$/) do |arg1, arg2|
  visit Capybara.app_host
  fill_in 'username', with: arg1
  fill_in 'password', with: arg2
  click_button 'Sign In'
  step %(I should be logged in)
end

Given(/^I am authorized$/) do
  step %(I am authorized as "testing" with password "testing")
end

When(/^I sign out$/) do
  page.find(:xpath, "//a[@href='/rhn/Logout.do']").click
end

Then(/^I should not be authorized$/) do
  raise unless page.has_no_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I should be logged in$/) do
  raise unless page.has_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I am logged-in$/) do
  raise unless page.find(:xpath, "//a[@href='/rhn/Logout.do']").visible?
  raise unless page.has_content?('You have just created your first SUSE Manager user. To finalize your installation please use the Setup Wizard')
end

When(/^I go to the admin configuration page$/) do
  find_link('Admin').click
  find_link('SUSE Manager Configuration').click
end

When(/^I go to the configuration page$/) do
  find_link('Configuration').click
end

Given(/^I am on the patches page$/) do
  step %(I am authorized)
  visit("https://#{$server_fullhostname}/rhn/errata/RelevantErrata.do")
end

Given(/^I am on the "([^"]*)" patches Details page$/) do |arg1|
  steps %(
    Given I am on the patches page
    And I follow "All" in the left menu
    And I follow "#{arg1}"
    )
end

Then(/^I should see an update in the list$/) do
  raise unless has_xpath?('//div[@class="table-responsive"]/table/tbody/tr/td/a')
end

When(/^I check test channel$/) do
  step %(I check "Test Base Channel" in the list)
end

When(/^I check "([^"]*)" patch$/) do |arg1|
  step %(I check "#{arg1}" in the list)
end

When(/^I am on System Set Manager Overview$/) do
  visit("https://#{$server_fullhostname}/rhn/ssm/index.do")
end

When(/^I am on Autoinstallation Overview page$/) do
  visit("https://#{$server_fullhostname}/rhn/kickstart/KickstartOverview.do")
end

When(/^I am on the System Manager System Overview page$/) do
  visit("https://#{$server_fullhostname}/rhn/systems/ssm/ListSystems.do")
end

When(/^I am on the Create Autoinstallation Profile page$/) do
  visit("https://#{$server_fullhostname}/rhn/kickstart/AdvancedModeCreate.do")
end

When(/^I am on the System Overview page$/) do
  visit("https://#{$server_fullhostname}/rhn/systems/Overview.do")
end

Then(/^I reload the page$/) do
  visit current_url
end

Then(/^I try to reload page until contains "([^"]*)" text$/) do |arg1|
  found = false
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        if page.has_content?(arg1)
          found = true
          break
        end
        sleep(5)
        visit current_url
      end
    end
  rescue Timeout::Error
    raise "'#{arg1}' cannot be found after wait and reload page"
  end
  raise unless found
end

Then(/^I try to reload page until it does not contain "([^"]*)" text$/) do |arg1|
  found = true
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        unless page.has_content?(arg1)
          found = false
          break
        end
        sleep(5)
        visit current_url
      end
    end
  rescue Timeout::Error
    raise "'#{arg1}' is still found after wait and reload page"
  end
  raise if found
end

Given(/^I am in the organization configuration page$/) do
  steps %(
    When I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Organizations"
    And I follow first "SUSE Test"
    And I follow first "Configuration"
  )
end

Then(/^I should see something$/) do
  steps %(
    Given I should see a "Sign In" text
    And I should see a "About" text
    )
end

Then(/^I should see "([^"]*)" systems selected for SSM$/) do |arg|
  within(:xpath, '//span[@id="spacewalk-set-system_list-counter"]') do
    raise unless has_content?(arg)
  end
end

#
# Test for a text in the whole page
#
Then(/^I should see a "([^"]*)" text$/) do |arg1|
  unless page.has_content?(arg1)
    sleep 2
    raise unless page.has_content?(arg1)
  end
end

#
# Test for text in a snippet textarea
#
Then(/^I should see "([^"]*)" in the textarea$/) do |arg1|
  within('textarea') do
    raise unless page.has_content?(arg1)
  end
end

Then(/^I should see "([^"]*)" loaded in the textarea$/) do |arg1|
  raise unless first('textarea').value.include?(arg1)
end

#
# Test for a text in the whole page using regexp
#
Then(/^I should see a text like "([^"]*)"$/) do |title|
  unless page.has_content?(Regexp.new(title))
    sleep 2
    raise unless page.has_content?(Regexp.new(title))
  end
end

#
# Test for a text not allowed in the whole page
#
Then(/^I should not see a "([^"]*)" text$/) do |text|
  raise "#{text} found on the page! FAIL" unless page.has_no_content?(text)
end

#
# Test for a visible link in the whole page
#
Then(/^I should see a "([^"]*)" link$/) do |arg1|
  link = first(:link, arg1)
  if link.nil?
    sleep 3
    $stderr.puts 'ERROR - try again'
    raise unless first(:link, arg1).visible?
  else
    raise unless link.visible?
  end
end

#
# Validate link is gone
#
Then(/^I should not see a "([^"]*)" link$/) do |arg1|
  raise unless page.has_no_link?(arg1)
end

Then(/^I should see a "([^"]*)" button$/) do |arg1|
  raise unless find_button(arg1).visible?
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
Then(/^I should see a "([^"]*)" link in element "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    raise unless find_link(arg1).visible?
  end
end

Then(/^I should not see a "([^"]*)" link in element "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    raise unless has_no_link?(arg1)
  end
end

Then(/^I should see a "([^"]*)" text in element "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    raise unless has_content?(arg1)
  end
end

Then(/^I should see a "([^"]*)" or "([^"]*)" text in element "([^"]*)"$/) do |arg1, arg2, arg3|
  within(:xpath, "//div[@id=\"#{arg3}\" or @class=\"#{arg3}\"]") do
    raise if !has_content?(arg1) && !has_content?(arg2)
  end
end

Then(/^I should see a "([^"]*)" link in "([^"]*)" "([^"]*)"$/) do |arg1, arg2, arg3|
  raise unless page.has_xpath?("//#{arg2}[@id='#{arg3}' or @class='#{arg3}']/a[text()='#{arg1}']")
end

Then(/^I should see a "([^"]*)" link in the table (.*) column$/) do |link, column|
  idx = %w[first second third fourth].index(column)
  unless idx
    # try column by name
    # unquote if neeeded
    colname = column.gsub(/\A['"]+|['"]+\Z/, '')
    cols = page.all(:xpath, '//table//thead/tr[1]/th').map(&:text)
    idx = cols.index(colname)
  end
  raise("Unknown column '#{column}'") unless idx
  # find(:xpath, "//table//thead//tr/td[#{idx + 1}]/a[text()='#{link}']")
  raise unless page.has_xpath?("//table//tr/td[#{idx + 1}]//a[text()='#{link}']")
end

Then(/^I reload the page until it does contain a "([^"]*)" text in the table (.*) row$/) do |text, row|
  found = false
  idx = %w[first second third fourth].index(row)
  raise("Unknown row '#{row}'") unless idx
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        if page.has_xpath?("//table//tr[#{idx + 1}]//td[contains(text(), '#{text}')]")
          found = true
          break
        end
        sleep(5)
        visit current_url
      end
    end
  rescue Timeout::Error
    raise "'#{text}' is not found in the '#{row}' row of the table after wait and reload page"
  end
  raise unless found
end

Then(/^I should see a "([^"]*)" link in the (left menu|tab bar|tabs|content area)$/) do |arg1, arg2|
  tag = case arg2
        when /left menu/ then 'aside'
        when /tab bar|tabs/ then 'header'
        when /content area/ then 'section'
        else raise "Unknown element with description '#{arg2}'"
        end
  within(:xpath, "//#{tag}") do
    step "I should see a \"#{arg1}\" link"
  end
end

Then(/^I should not see a "([^"]*)" link in the (.+)$/) do |arg1, arg2|
  tag = case arg2
        when /left menu/ then 'aside'
        when /tab bar|tabs/ then 'header'
        when /content area/ then 'section'
        else raise "Unknown element with description '#{arg2}'"
        end
  within(:xpath, "//#{tag}") do
    step "I should not see a \"#{arg1}\" link"
  end
end

Then(/^I should see a "([^"]*)" link in row ([0-9]+) of the content menu$/) do |arg1, arg2|
  within(:xpath, '//section') do
    within(:xpath, "//div[@class=\"spacewalk-content-nav\"]/ul[#{arg2}]") do
      step %(I should see a "#{arg1}" link)
    end
  end
end

Then(/^I should see a "([^"]*)" link in list "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//ul[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    raise unless find_link(arg1).visible?
  end
end

Then(/^I should see a "([^"]*)" button in "([^"]*)" form$/) do |arg1, arg2|
  within(:xpath, "//form[@id='#{arg2}' or @name=\"#{arg2}\"]") do
    raise unless find_button(arg1)
  end
end

Then(/^I select the "([^"]*)" repo$/) do |repo|
  within page.first('a', text: repo) do
    within(:xpath, '../..') do
      first('input[type=checkbox]').set(true)
    end
  end
end

Then(/^I check the row with the "([^"]*)" link$/) do |arg1|
  within(:xpath, "//a[text()='#{arg1}']/../..") do
    first('input[type=checkbox]').set(true)
  end
end

Then(/^I check the row with the "([^"]*)" text$/) do |text|
  within(:xpath, "//tr[td[contains(., '#{text}')]]") do
    first('input[type=checkbox]').set(true)
  end
end

Then(/^I check the row with the "([^"]*)" hostname$/) do |host|
  node = get_target(host)
  within(:xpath, "//tr[td[contains(., '#{node.full_hostname}')]]") do
    first('input[type=checkbox]').set(true)
  end
end

Then(/^I should see (\d+) "([^"]*)" links$/) do |count, text|
  page.all('a', text: text, count: count)
end
#
# Test if an option is selected
#
Then(/^Option "([^"]*)" is selected as "([^"]*)"$/) do |arg1, arg2|
  raise unless has_select?(arg2, selected: arg1)
end

#
# Test if a radio button is checked
#
Then(/^radio button "([^"]*)" is checked$/) do |arg1|
  raise unless has_checked_field?(arg1)
end

#
# Test if a checkbox is checked
#
Then(/^I should see "([^"]*)" as checked$/) do |arg1|
  raise unless has_checked_field?(arg1)
end

#
# Test if a checkbox is unchecked
#
Then(/^I should see "([^"]*)" as unchecked$/) do |arg1|
  raise unless has_unchecked_field?(arg1)
end

#
# Test if a checkbox is disabled
#
Then(/^the "([^\"]*)" checkbox should be disabled$/) do |arg1|
  page.has_css?("##{arg1}[disabled]")
end

Then(/^the "([^\"]*)" field should be disabled$/) do |arg1|
  page.has_css?("##{arg1}[disabled]")
end

Then(/^I should see "([^"]*)" in field "([^"]*)"$/) do |arg1, arg2|
  raise unless page.has_field?(arg2, with: arg1)
end

Then(/^I should see a "([^"]*)" element in "([^"]*)" form$/) do |arg1, arg2|
  within(:xpath, "//form[@id=\"#{arg2}\"] | //form[@name=\"#{arg2}\"]") do
    raise unless find_field(arg1, match: :first).visible?
  end
end

Then(/^I should see a "([^"]*)" editor in "([^"]*)" form$/) do |arg1, arg2|
  within(:xpath, "//form[@id=\"#{arg2}\"] | //form[@name=\"#{arg2}\"]") do
    raise unless page.find("textarea##{arg1}", visible: false)
    raise unless page.has_css?("##{arg1}-editor")
  end
end

Then(/^"([^"]*)" is installed on "([^"]*)"$/) do |package, target|
  node = get_target(target)
  node.run("rpm -q #{package}")
end

Then(/^I should see a Sign Out link$/) do
  raise unless has_xpath?("//a[@href='/rhn/Logout.do']")
end

When(/^I check "([^"]*)" in the list$/) do |arg1|
  within(:xpath, '//section') do
    # use div/div/div for cve audit which has two tables
    row = first(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]")
    if row.nil?
      sleep 10
      $stderr.puts 'ERROR - try again'
      row = first(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]")
    end
    row.first(:xpath, './/input[@type="checkbox"]').set(true)
  end
end

When(/^I check checkbox with title "([^"]*)" in the list$/) do |arg1|
  row = first(:xpath, "//tr[.//i[@title='#{arg1}']]")
  if row.nil?
    sleep 3
    $stderr.puts 'ERROR - try again'
    row = first(:xpath, "//tr[.//i[@title='#{arg1}']]")
  end
  row.first(:xpath, './/input[@type="checkbox"]').set(true)
end

When(/^I uncheck "([^"]*)" in the list$/) do |arg1|
  within(:xpath, '//section') do
    # use div/div/div for cve audit which has two tables
    top_level_xpath_query = "//div[@class='table-responsive']/table/tbody/tr[.//td[contains(.,'#{arg1}')] and .//input[@type='checkbox' and @checked]]"
    row = first(:xpath, top_level_xpath_query)
    if row.nil?
      sleep 3
      $stderr.puts 'ERROR - try again'
      row = first(:xpath, top_level_xpath_query)
    end
    row.first(:xpath, './/input[@type="checkbox"]').set(false)
  end
end

Then(/^I should see (\d+) "([^"]*)" fields in "([^"]*)" form$/) do |count, name, id|
  within(:xpath, "//form[@id=\"#{id}\" or  @name=\"#{id}\"]") do
    raise unless has_field?(name, count: count.to_i)
  end
end
