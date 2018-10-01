# Copyright (c) 2010-2018 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Texts and links
#

When(/^I follow "(.*?)" link$/) do |host|
  system_name = get_system_name(host)
  step %(I follow "#{system_name}")
end

When(/^I should see a "(.*)" text in the content area$/) do |txt|
  within('#spacewalk-content') do
    raise unless page.has_content?(txt)
  end
end

When(/^I click on "([^"]+)" in row "([^"]+)"$/) do |link, item|
  within(:xpath, "//tr[td[contains(.,'#{item}')]]") do
    click_link_or_button(link)
  end
end

Then(/^the current path is "([^"]*)"$/) do |arg1|
  raise unless current_path == arg1
end

When(/^I wait until I see "([^"]*)" text$/) do |text|
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

When(/^I wait until I do not see "([^"]*)" text$/) do |text|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break unless page.has_content?(text)
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "The #{text} was always there in webpage"
  end
end

When(/^I wait at most (\d+) seconds until I see "([^"]*)" text$/) do |seconds, text|
  begin
    Timeout.timeout(seconds.to_i) do
      loop do
        break if page.has_content?(text)
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text} in webpage"
  end
end

When(/^I wait until I see "([^"]*)" text or "([^"]*)" text$/) do |text1, text2|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_content?(text1) || page.has_content?(text2)
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text1} and #{text2} in webpage"
  end
end

When(/^I wait until I see "([^"]*)" text, refreshing the page$/) do |text|
  text.gsub! '$PRODUCT', $product
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_content?(text)
        sleep 1
        page.evaluate_script 'window.location.reload()'
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text} in webpage"
  end
end

When(/^I wait at most (\d+) seconds until the event is completed, refreshing the page$/) do |timeout|
  begin
    Timeout.timeout(timeout.to_i) do
      loop do
        break if page.has_content?("This action's status is: Completed.")
        raise 'Event failed' if page.has_content?("This action's status is: Failed.")
        sleep 1
        page.evaluate_script 'window.location.reload()'
      end
    end
  rescue Timeout::Error
    raise "Event not completed in #{timeout} seconds"
  end
end

When(/^I wait until I see the name of "([^"]*)", refreshing the page$/) do |host|
  system_name = get_system_name(host)
  step %(I wait until I see "#{system_name}" text, refreshing the page)
end

When(/^I wait until I do not see "([^"]*)" text, refreshing the page$/) do |text|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break unless page.has_content?(text)
        sleep 3
        page.evaluate_script 'window.location.reload()'
      end
    end
  rescue Timeout::Error
    raise "The #{text} was always there in webpage"
  end
end

When(/^I wait until I do not see the name of "([^"]*)", refreshing the page$/) do |host|
  system_name = get_system_name(host)
  step %(I wait until I do not see "#{system_name}" text, refreshing the page)
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

When(/^I select "([^"]*)" from drop-down in table line with "([^"]*)"$/) do |value, line|
  select = find(:xpath, ".//div[@class='table-responsive']/table/tbody/tr[contains(td,'#{line}')]//select")
  select(value, from: select[:id])
end

When(/^I choose radio button "([^"]*)" for child channel "([^"]*)"$/) do |radio, channel|
  label = find(:xpath, "//dt[contains(.//div, '#{channel}')]//label[text()='#{radio}']")
  choose(label[:for])
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
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//a[contains(., '#{text}')]"
  raise unless find(:xpath, xpath_query).click
end

When(/^I enter "(.*?)" in the editor$/) do |arg1|
  page.execute_script("ace.edit('contents-editor').insert('#{arg1}')")
end

When(/^I click System List, under Systems node$/) do
  find(:xpath, "//div[@id=\"nav\"]/nav/ul/li[contains(@class, 'active')
       and contains(@class, 'open')
       and contains(@class,'node')]/ul/li/div/a/span[contains(.,'System List')]").click
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
  raise unless page.has_content?('Create ' + product + ' Administrator')
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

# access the multi-clients/minions
Given(/^I am on the Systems overview page of this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  steps %(
    Given I am on the Systems page
  )
  step %(I follow "#{system_name}")
end

Given(/^I am on the "([^"]*)" page of this "([^"]*)"$/) do |page, host|
  steps %(
    Given I am on the Systems overview page of this "#{host}"
    And I follow "#{page}" in the content area
  )
end

When(/^I follow this "([^"]*)" link$/) do |host|
  system_name = get_system_name(host)
  step %(I follow "#{system_name}")
end

When(/^I check the "([^"]*)" client$/) do |host|
  system_name = get_system_name(host)
  step %(I check "#{system_name}" in the list)
end

When(/^I uncheck the "([^"]*)" client$/) do |host|
  system_name = get_system_name(host)
  step %(I uncheck "#{system_name}" in the list)
end

Given(/^I am on the groups page$/) do
  steps %(
    Given I am on the Systems page
    And I follow "System Groups" in the left menu
    )
end

Given(/^I am on the active Users page$/) do
  steps %(
    Given I am authorized as "admin" with password "admin"
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

When(/^I wait until table row for "([^"]*)" contains button "([^"]*)"$/) do |text, button|
  within(:xpath, "//tr[td[contains(., '#{text}')]]") do
    begin
      Timeout.timeout(DEFAULT_TIMEOUT) do
        loop do
          break if find_button(button)
          sleep 1
        end
      end
    rescue Timeout::Error
      raise "Couldn't find #{button} in row with #{text} text"
    end
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

Then(/^I am logged in$/) do
  raise unless page.find(:xpath, "//a[@href='/rhn/Logout.do']").visible?
  raise unless page.has_content?('You have just created your first ' + product + ' user. To finalize your installation please use the Setup Wizard')
end

Given(/^I am on the patches page$/) do
  step %(I am authorized)
  visit("https://#{$server.full_hostname}/rhn/errata/RelevantErrata.do")
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
  visit("https://#{$server.full_hostname}/rhn/ssm/index.do")
end

When(/^I am on Autoinstallation Overview page$/) do
  visit("https://#{$server.full_hostname}/rhn/kickstart/KickstartOverview.do")
end

When(/^I am on the System Manager System Overview page$/) do
  visit("https://#{$server.full_hostname}/rhn/systems/ssm/ListSystems.do")
end

When(/^I am on the Create Autoinstallation Profile page$/) do
  visit("https://#{$server.full_hostname}/rhn/kickstart/AdvancedModeCreate.do")
end

When(/^I am on the System Overview page$/) do
  visit("https://#{$server.full_hostname}/rhn/systems/Overview.do")
end

Then(/^I reload the page$/) do
  visit current_url
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
Then(/^I should see a "([^"]*)" text$/) do |text|
  text.gsub! '$PRODUCT', $product
  unless page.has_content?(text)
    sleep 2
    raise unless page.has_content?(text)
  end
end

Then(/^I should see a "([^"]*)" text or "([^"]*)" text$/) do |text1, text2|
  unless page.has_content?(text1) || page.has_content?(text2)
    sleep 2
    raise unless page.has_content?(text1) || page.has_content?(text2)
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
Then(/^I should see a "([^"]*)" link in element "([^"]*)"$/) do |link, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise unless find_link(link).visible?
  end
end

Then(/^I should not see a "([^"]*)" link in element "([^"]*)"$/) do |link, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise unless has_no_link?(link)
  end
end

Then(/^I should see a "([^"]*)" text in element "([^"]*)"$/) do |text, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise unless has_content?(text)
  end
end

Then(/^I should not see a "([^"]*)" text in element "([^"]*)"$/) do |text, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise if has_content?(text)
  end
end

Then(/^I should see a "([^"]*)" or "([^"]*)" text in element "([^"]*)"$/) do |text1, text2, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise unless has_content?(text1) || has_content?(text2)
  end
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

When(/^I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows$/) do
  # this step is used for long operations like refreshing caches, repositories, etc.
  # therefore we use a non-standard timeout
  refresh_timeout = 800
  begin
    Timeout.timeout(refresh_timeout) do
      loop do
        visit current_url
        # get all texts in the table column under the "Status" header
        under_status = "//tr/td[count(//th[contains(*/text(), 'Status')]/preceding-sibling::*) + 1]"
        statuses = page.all(:xpath, under_status).map(&:text)

        # disregard any number of initial SKIPPED rows
        # this is expected when Taskomatic triggers the same task concurrently
        first_non_skipped = statuses.drop_while do |status|
          status == 'SKIPPED'
        end.first

        # halt in case we are done, or if an error is detected
        break if first_non_skipped == 'FINISHED'
        raise('Taskomatic task was INTERRUPTED') if first_non_skipped == 'INTERRUPTED'

        # otherwise either no row is shown yet, or the task is still RUNNING
        # continue waiting
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "Task does not look FINISHED after #{refresh_timeout} seconds"
  end
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
  system_name = get_system_name(host)
  within(:xpath, "//tr[td[contains(., '#{system_name}')]]") do
    first('input[type=checkbox]').set(true)
  end
end

#
# Test if an option is selected
#
Then(/^option "([^"]*)" is selected as "([^"]*)"$/) do |arg1, arg2|
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

# Click on a button in a modal window with a specific title
When(/^I click on "([^"]*)" in "([^"]*)" modal$/) do |btn, title|
  path = "//*[contains(@class, \"modal-title\") and text() = \"#{title}\"]" \
    '/ancestor::div[contains(@class, "modal-dialog")]'

  # We wait until the element becomes visible, because
  # the fade out animation might still be in progress
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_xpath?(path, visible: true)
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{title} modal"
  end

  within(:xpath, path, visible: :all) do
    find(:xpath, ".//button[@title = \"#{btn}\"]", visible: :all).click
  end
end

# Image-specific steps
When(/^I enter "([^"]*)" relative to profiles as "([^"]*)"$/) do |path, field|
  step %(I enter "#{$git_profiles}/#{path}" as "#{field}")
end

When(/^I enter uri, username and password for portus$/) do
  step %(I enter "#{ENV['PORTUS_URI']}" as "uri")
  step %(I enter "#{ENV['PORTUS_USER']}" as "username")
  step %(I enter "#{ENV['PORTUS_PASS']}" as "password")
end
