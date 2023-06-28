# Copyright (c) 2010-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning navigation through the Web UI
### as well as validating the UI output.

#
# Texts and links
#

Then(/^I should see a "(.*)" text in the content area$/) do |text|
  within('#spacewalk-content') do
    raise "Text '#{text}' not found" unless has_content?(text)
  end
end

Then(/^I should not see a "(.*)" text in the content area$/) do |text|
  within('#spacewalk-content') do
    raise "Text '#{text}' found" unless has_no_content?(text)
  end
end

When(/^I click on "([^"]+)" in row "([^"]+)"$/) do |link, item|
  within(:xpath, "//tr[td[contains(.,'#{item}')]]") do
    click_link_or_button_and_wait(link)
  end
end

When(/^I click on "([^"]+)" in tree item "(.*?)"$/) do |button, item|
  within(:xpath, "//span[contains(text(), '#{item}')]/ancestor::div[contains(@class, 'product-details-wrapper')]") do
    click_link_or_button_and_wait(button)
  end
end

Then(/^the current path is "([^"]*)"$/) do |arg1|
  raise "Path #{current_path} different than #{arg1}" unless current_path == arg1
end

When(/^I wait until I see "([^"]*)" text$/) do |text|
  raise "Text '#{text}' not found" unless has_text?(text, wait: DEFAULT_TIMEOUT)
end

When(/^I wait until I do not see "([^"]*)" text$/) do |text|
  raise "Text '#{text}' found" unless has_no_text?(text, wait: DEFAULT_TIMEOUT)
end

When(/^I wait at most (\d+) seconds until I see "([^"]*)" text$/) do |seconds, text|
  raise "Text '#{text}' not found" unless has_content?(text, wait: seconds.to_i)
end

When(/^I wait until I see "([^"]*)" text or "([^"]*)" text$/) do |text1, text2|
  raise "Text '#{text1}' or '#{text2}' not found" unless has_content?(text1, wait: DEFAULT_TIMEOUT) || has_content?(text2, wait: DEFAULT_TIMEOUT)
end

When(/^I wait until I see "([^"]*)" (text|regex), refreshing the page$/) do |text, type|
  text = Regexp.new(text) if type == 'regex'
  next if has_content?(text, wait: 3)
  repeat_until_timeout(message: "Couldn't find text '#{text}'") do
    break if has_content?(text, wait: 3)
    begin
      accept_prompt do
        execute_script 'window.location.reload()'
      end
    rescue Capybara::ModalNotFound
      # ignored
    end
  end
end

When(/^I wait at most (\d+) seconds until I do not see "([^"]*)" text, refreshing the page$/) do |seconds, text|
  next if has_no_text?(text, wait: 3)
  repeat_until_timeout(message: "I still see text '#{text}'", timeout: seconds.to_i) do
    break if has_no_text?(text, wait: 3)
    begin
      accept_prompt do
        execute_script 'window.location.reload()'
      end
    rescue Capybara::ModalNotFound
      # ignored
    end
  end
end

When(/^I wait at most "([^"]*)" seconds until I do not see "([^"]*)" text$/) do |seconds, text|
  next if has_no_text?(text, wait: 3)
  repeat_until_timeout(message: "I still see text '#{text}'", timeout: seconds.to_i) do
    break if has_no_text?(text, wait: 3)
  end
end

When(/^I wait at most (\d+) seconds until the event is completed, refreshing the page$/) do |timeout|
  last = Time.now
  next if has_content?("This action's status is: Completed.", wait: 3)
  repeat_until_timeout(timeout: timeout.to_i, message: 'Event not yet completed') do
    break if has_content?("This action's status is: Completed.", wait: 3)
    raise 'Event failed' if has_content?("This action's status is: Failed.", wait: 3)
    current = Time.now
    if current - last > 150
      log "#{current} Still waiting for action to complete..."
      last = current
    end
    begin
      accept_prompt do
        execute_script 'window.location.reload()'
      end
    rescue Capybara::ModalNotFound
      # ignored
    end
  end
end

When(/^I wait until I see the name of "([^"]*)", refreshing the page$/) do |host|
  system_name = get_system_name(host)
  step %(I wait until I see the "#{system_name}" system, refreshing the page)
end

When(/^I wait until I see the "([^"]*)" system, refreshing the page$/) do |system_name|
  steps %(
    And I wait until I do not see "Loading..." text
    And I wait until I see "#{system_name}" text, refreshing the page
  )
end

When(/^I wait until I do not see "([^"]*)" text, refreshing the page$/) do |text|
  next unless has_content?(text, wait: 3)
  repeat_until_timeout(message: "Text '#{text}' is still visible") do
    break unless has_content?(text, wait: 3)
    begin
      accept_prompt do
        execute_script 'window.location.reload()'
      end
    rescue Capybara::ModalNotFound
      # ignored
    end
  end
end

When(/^I wait until I do not see the name of "([^"]*)", refreshing the page$/) do |host|
  system_name = get_system_name(host)
  step %(I wait until I do not see "#{system_name}" text, refreshing the page)
end

Then(/^I wait until I see the (VNC|spice) graphical console$/) do |type|
  repeat_until_timeout(message: "The #{type} graphical console didn't load") do
    break if find(:xpath, '//canvas')

    # If the connection failed try reloading since the VM may not have been ready
    if find(:xpath, '//*[contains(@class, "modal-title") and text() = "Failed to connect"]')
      begin
        accept_prompt do
          execute_script 'window.location.reload()'
        end
      rescue Capybara::ModalNotFound
        # ignored
      end
    end
  end
end

When(/^I switch to last opened window$/) do
  page.driver.browser.switch_to.window(page.driver.browser.window_handles.last)
end

When(/^I close the last opened window$/) do
  page.driver.browser.close
  page.driver.browser.switch_to.window(page.driver.browser.window_handles.first)
end

#
# Check a checkbox of the given id
#
When(/^I check "([^"]*)"$/) do |identifier|
  check(identifier)
  raise "Checkbox #{identifier} not checked." unless has_checked_field?(identifier)
end

When(/^I uncheck "([^"]*)"$/) do |identifier|
  uncheck(identifier)
  raise "Checkbox #{identifier} not unchecked." if has_checked_field?(identifier)
end

When(/^I select "([^"]*)" from "([^"]*)"$/) do |option, field|
  if has_select?(field, with_options: [option], wait: 1)
    select(option, from: field)
  else
    # Custom React selector
    xpath_field = "//*[contains(@class, 'data-testid-#{field}-child__control')]"
    xpath_option = ".//*[contains(@class, 'data-testid-#{field}-child__option') and contains(text(), '#{option}')]"
    find(:xpath, xpath_field).click
    find(:xpath, xpath_option, match: :first).click
  end
end

# select an item from any dropdown
When(/^I select "(.*?)" from "([^"]*)" dropdown/) do |selection, label|
  # let the the select2js box filter open the hidden options
  xpath_query = "//select[@name='#{label}']"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
  # select the desired option
  raise "#{label} #{selection} not found" unless find(:xpath, "//select[@name='#{label}']/option[contains(text(), '#{selection}')]").click
end

When(/^I select the parent channel for the "([^"]*)" from "([^"]*)"$/) do |client, from|
  select(BASE_CHANNEL_BY_CLIENT[client], from: from, exact: false)
end

When(/^I select "([^"]*)" from drop-down in table line with "([^"]*)"$/) do |value, line|
  select = find(:xpath, ".//div[@class='table-responsive']/table/tbody/tr[contains(td/a,'#{line}')]//select")
  select(value, from: select[:id])
end

When(/^I choose radio button "([^"]*)" for child channel "([^"]*)"$/) do |radio, channel|
  label = find(:xpath, "//dt[contains(.//div, '#{channel}')]//label[text()='#{radio}']")
  choose(label[:for])
end

When(/^I (include|exclude) the recommended child channels$/) do |action|
  toggle = "//span[@class='pointer']"
  step %(I wait until I see "include recommended" text)
  raise 'The toggle is not present' unless page.has_xpath?(toggle, wait: 5)
  if action == 'include'
    toggle_off = "//i[contains(@class, 'fa-toggle-off')]"
    find(:xpath, toggle).click if page.has_xpath?(toggle_off, wait: 5)
  else
    toggle_on = "//i[contains(@class, 'fa-toggle-on')]"
    find(:xpath, toggle).click if page.has_xpath?(toggle_on, wait: 5)
  end
end

When(/^I choose "([^"]*)"$/) do |arg1|
  find(:xpath, "//input[@type='radio' and @value='#{arg1}']").set(true)
end

#
# Enter a text into a textfield
#
When(/^I enter "([^"]*)" as "([^"]*)"$/) do |text, field|
  fill_in(field, with: text, fill_options: { clear: :backspace })
end

When(/^I enter (\d+) minutes from now as "([^"]*)"$/) do |minutes_to_add, field|
  future_time = Time.now + 60 * minutes_to_add.to_i
  future_time.strftime('%l:%M %P').to_s.strip
  fill_in(field, with: future_time, fill_options: { clear: :backspace })
end

When(/^I enter "([^"]*)" as "([^"]*)" text area$/) do |arg1, arg2|
  execute_script("document.getElementsByName('#{arg2}')[0].value = '#{arg1}'")
end

When(/^I enter "(.*?)" as "(.*?)" in the content area$/) do |text, field|
  within(:xpath, '//section') do
    fill_in(field, with: text, fill_options: { clear: :backspace })
  end
end

When(/^I enter the URI of the registry as "([^"]*)"$/) do |field|
  fill_in(field, with: $no_auth_registry, fill_options: { clear: :backspace })
end

When(/^I enter "([^"]*)" on the search field$/) do |search_text|
  step %(I enter "#{search_text}" as "search_string")
end

# Go back in the browser history
When(/^I go back$/) do
  page.driver.go_back
end

#
# Click on a button
#
When(/^I click on "([^"]*)"$/) do |text|
  click_button_and_wait(text, match: :first)
end

#
# Click on a button which appears inside of <div> with
# the given "id"
When(/^I click on "([^"]*)" in element "([^"]*)"$/) do |text, element_id|
  within(:xpath, "//div[@id=\"#{element_id}\"]") do
    click_button_and_wait(text, match: :first)
  end
end

#
# Click on a button and confirm in alert box
When(/^I click on "([^"]*)" and confirm$/) do |text|
  begin
    accept_alert do
      step %(I click on "#{text}")
    end
  rescue Capybara::ModalNotFound
    # ignored
  end
end

#
# Click on a link
#
When(/^I follow "([^"]*)"$/) do |text|
  click_link_and_wait(text)
end

#
# Click on the first link
#
When(/^I follow first "([^"]*)"$/) do |text|
  click_link_and_wait(text, match: :first)
end

#
# Click on a link which appears inside of <div> with
# the given "id"

When(/^I follow "([^"]*)" in the (.+)$/) do |arg1, arg2|
  tag = case arg2
        when /tab bar|tabs/ then 'header'
        when /content area/ then 'section'
        else raise "Unknown element with description '#{arg2}'"
        end
  within(:xpath, "//#{tag}") do
    step %(I follow "#{arg1}")
  end
end

When(/^I follow first "([^"]*)" in the (.+)$/) do |arg1, arg2|
  tag = case arg2
        when /tab bar|tabs/ then 'header'
        when /content area/ then 'section'
        else raise "Unknown element with description '#{arg2}'"
        end
  within(:xpath, "//#{tag}") do
    step "I follow first \"#{arg1}\""
  end
end

When(/^I follow "([^"]*)" on "(.*?)" row$/) do |text, host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//a[contains(., '#{text}')]"
  element = find_and_wait_click(:xpath, xpath_query)
  element.click
end

When(/^I enter "(.*?)" in the editor$/) do |arg1|
  execute_script("ace.edit('contents-editor').insert('#{arg1}')")
end

#
# Menu links
#

# Menu path link
When(/^I follow the left menu "([^"]*)"$/) do |menu_path|
  # split menu levels separated by '>' character
  menu_levels = menu_path.split('>').map(&:strip)

  # define reusable patterns
  prefix_path = "//aside/div[@id='nav']/nav"
  link_path = "/ul/li/div/a[contains(.,'%s')]"
  parent_wrapper_path = '/parent::div'
  parent_level_path = '/parent::li'

  # point the target to the nav menu
  target_link_path = prefix_path

  menu_levels.each_with_index do |menu_level, index|
    # append the current target link and replace the placeholder with the current level value
    target_link_path += (link_path % menu_level)
    # if this is the last element of the path
    break if index == (menu_levels.count - 1)
    # open the submenu if needed
    unless find(:xpath, target_link_path + parent_wrapper_path + parent_level_path)[:class].include?('open')
      find(:xpath, target_link_path + parent_wrapper_path).click
    end
    # point the target to the current menu level
    target_link_path += parent_wrapper_path + parent_level_path
  end
  # finally go to the target page
  find_and_wait_click(:xpath, target_link_path).click
end

#
# End of Menu links
#

Given(/^I am not authorized$/) do
  begin
    page.reset!
  rescue NoMethodError
    log 'The browser session could not be cleaned.'
  ensure
    visit Capybara.app_host
  end
  raise "Button 'Sign In' not visible" unless find_button('Sign In').visible?
end

When(/^I go to the home page$/) do
  visit Capybara.app_host
end

Given(/^I access the host the first time$/) do
  visit Capybara.app_host
  raise "Text 'Create #{product} Administrator' not found" unless has_content?("Create #{product} Administrator")
end

# Menu permission check
Given(/^I am authorized for the "([^"]*)" section$/) do |section|
  case section
  when 'Admin'
    step %(I am authorized as "admin" with password "admin")
  when 'Images'
    step %(I am authorized as "kiwikiwi" with password "kiwikiwi")
  else
    log "Section #{section} not supported"
  end
end

# access the clients
Given(/^I am on the Systems overview page of this "([^"]*)"$/) do |host|
  node = get_target(host)
  system_id = get_system_id(node)
  visit("/rhn/systems/details/Overview.do?sid=#{system_id}")
end

Given(/^I navigate to the Systems overview page of this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  steps %(
    Given I am on the Systems page
    When I enter "#{system_name}" as "criteria"
    And I wait until I do not see "Loading..." text
    And I follow "#{system_name}"
    And I wait until I see "System Status" text
  )
end

Given(/^I am on the "([^"]*)" page of this "([^"]*)"$/) do |page, host|
  steps %(
    Given I am on the Systems overview page of this "#{host}"
    And I follow "#{page}" in the content area
  )
end

When(/^I enter the hostname of "([^"]*)" as "([^"]*)"$/) do |host, field|
  system_name = get_system_name(host)
  log "The hostname of #{host} is #{system_name}"
  step %(I enter "#{system_name}" as "#{field}")
end

When(/^I select the hostname of "([^"]*)" from "([^"]*)"((?: if present)?)$/) do |host, field, if_present|
  begin
    system_name = get_system_name(host)
  rescue
    raise "Host #{host} not found" if if_present.empty?

    log "Host #{host} is not deployed, not trying to select it"
    return
  end
  step %(I select "#{system_name}" from "#{field}")
end

When(/^I follow this "([^"]*)" link$/) do |host|
  system_name = get_system_name(host)
  step %(I follow "#{system_name}")
end

When(/^I check the "([^"]*)" client$/) do |host|
  system_name = get_system_name(host)
  step %(I check "#{system_name}" in the list)
end

Then(/^table row for "([^"]*)" should contain "([^"]*)"$/) do |arg1, arg2|
  step %(I wait until table row for "#{arg1}" contains "#{arg2}")
end

Then(/^I wait until table row for "([^"]*)" contains "([^"]*)"$/) do |arg1, arg2|
  xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//*[contains(.,'#{arg1}')]]"
  within(:xpath, xpath_query) do
    raise "xpath: #{xpath_query} has no content #{arg2}" unless has_content?(arg2, wait: DEFAULT_TIMEOUT)
  end
end

Then(/^the table row for "([^"]*)" should( not)? contain "([^"]*)" icon$/) do |row, should_not, icon|
  case icon
  when 'retracted'
    content_selector = "i[class*='errata-retracted']"
  else
    raise "Unsupported icon '#{icon}' in the step definition"
  end

  xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//*[contains(.,'#{row}')]]"
  within(:xpath, xpath_query) do
    if should_not
      raise "xpath: #{xpath_query} has no icon #{icon}" unless has_no_css?(content_selector, wait: 2)
    else
      raise "xpath: #{xpath_query} has no icon #{icon}" unless has_css?(content_selector, wait: 2)
    end
  end
end

When(/^I wait at most ([0-9]+) seconds until table row for "([^"]*)" contains button "([^"]*)"$/) do |timeout, text, button|
  xpath_query = "//tr[td[contains(., '#{text}')]]/td/descendant::*[self::a or self::button][@title='#{button}']"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query, wait: timeout.to_f)
end

When(/^I wait until table row for "([^"]*)" contains button "([^"]*)"$/) do |text, button|
  step %(I wait at most #{DEFAULT_TIMEOUT} seconds until table row for "#{text}" contains button "#{button}")
end

When(/^I wait until table row contains a "([^"]*)" text$/) do |text|
  xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{text}')]]"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query, wait: DEFAULT_TIMEOUT)
end

When(/^I wait until button "([^"]*)" becomes enabled$/) do |text|
  raise "Button '#{text}' still disabled after #{DEFAULT_TIMEOUT} seconds" unless find_button(text, disabled: false, wait: DEFAULT_TIMEOUT)
end

# login, logout steps

Given(/^I am authorized as "([^"]*)" with password "([^"]*)"$/) do |user, passwd|
  begin
    page.reset!
  rescue NoMethodError => e
    log "The browser session could not be cleaned because there is no browser available: #{e.message}"
  rescue StandardError => e
    log "The browser session could not be cleaned for unknown issue: #{e.message}"
  ensure
    visit Capybara.app_host
  end
  next if all(:xpath, "//header//span[text()='#{user}']", wait: 0).any?

  find(:xpath, "//header//i[@class='fa fa-sign-out']").click if all(:xpath, "//header//i[@class='fa fa-sign-out']", wait: 0).any?

  fill_in('username', with: user)
  fill_in('password', with: passwd)
  click_button_and_wait('Sign In', match: :first)

  step %(I should be logged in)
end

Given(/^I am authorized$/) do
  step %(I am authorized as "testing" with password "testing")
end

When(/^I sign out$/) do
  find(:xpath, "//a[@href='/rhn/Logout.do']").click
end

Then(/^I should not be authorized$/) do
  raise 'User is authorized' if has_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I should be logged in$/) do
  xpath_query = "//a[@href='/rhn/Logout.do']"
  raise 'User is not logged in' unless find(:xpath, xpath_query)
end

Then(/^I am logged in$/) do
  raise 'User is not logged in' unless find(:xpath, "//a[@href='/rhn/Logout.do']").visible?
  # text = "You have just created your first #{product} user. To finalize your installation please use the Setup Wizard"
  # Workaround: Ignore the fact that the message is not shown
  # TODO: restore this as soon as the related issue is fixed: https://github.com/SUSE/spacewalk/issues/19369
  # raise 'The welcome message is not shown' unless has_content?(text)
end

Then(/^I should see an update in the list$/) do
  xpath_query = '//div[@class="table-responsive"]/table/tbody/tr/td/a'
  raise "xpath: #{xpath_query} not found" unless has_xpath?(xpath_query)
end

When(/^I check test channel$/) do
  step %(I check "Fake Base Channel" in the list)
end

When(/^I check "([^"]*)" patch$/) do |arg1|
  step %(I check "#{arg1}" in the list)
end

Then(/^I should see "([^"]*)" systems selected for SSM$/) do |arg|
  within(:xpath, '//span[@id="spacewalk-set-system_list-counter"]') do
    raise "There are not #{arg} systems selected" unless has_content?(arg)
  end
end

#
# Test for a text in the whole page
#
Then(/^I should see a "([^"]*)" text$/) do |text|
  raise "Text '#{text}' not found" unless has_content?(text)
end

Then(/^I should see a "([^"]*)" text or "([^"]*)" text$/) do |text1, text2|
  raise "Text '#{text1}' and '#{text2}' not found" unless has_content?(text1) || has_content?(text2)
end

Then(/^I should see "([^"]*)" short hostname$/) do |host|
  system_name = get_system_name(host).partition('.').first
  raise "Hostname #{system_name} is not present" unless has_content?(system_name)
end

Then(/^I should see "([^"]*)" hostname$/) do |host|
  system_name = get_system_name(host)
  raise "Hostname #{system_name} is not present" unless has_content?(system_name)
end

Then(/^I should not see "([^"]*)" hostname$/) do |host|
  system_name = get_system_name(host)
  raise "Hostname #{system_name} is present" if has_content?(system_name)
end

#
# Test for text in a snippet textarea
#
Then(/^I should see "([^"]*)" in the textarea$/) do |text|
  within('textarea') do
    raise "Text '#{text}' not found" unless has_content?(text)
  end
end

Then(/^I should see "([^"]*)" or "([^"]*)" in the textarea$/) do |text1, text2|
  within('textarea') do
    raise "Text '#{text1}' and '#{text2}' not found" unless has_content?(text1) || has_content?(text2)
  end
end

#
# Test for a text in the whole page using regexp
#
Then(/^I should see a text like "([^"]*)"$/) do |title|
  raise "Regular expression '#{title}' not found" unless has_content?(Regexp.new(title))
end

#
# Test for a text not allowed in the whole page
#
Then(/^I should not see a "([^"]*)" text$/) do |text|
  raise "Text '#{text}' found on the page" unless has_no_content?(text)
end

#
# Test for a visible link in the whole page
#
Then(/^I should see a "([^"]*)" link$/) do |text|
  raise "Link #{text} is not visible" unless has_link?(text)
end

#
# Validate link is gone
#
Then(/^I should not see a "([^"]*)" link$/) do |arg1|
  raise "Link #{arg1} is present" unless has_no_link?(arg1)
end

Then(/^I should see a "([^"]*)" button$/) do |arg1|
  raise "Link #{arg1} is not visible" unless find_button(arg1).visible?
end

Then(/^I should see a "(.*?)" link in the text$/) do |linktext, text|
  within(:xpath, "//p/strong[contains(normalize-space(string(.)), '#{text}')]") do
    assert has_xpath?("//a[text() = '#{linktext}']")
  end
end

Then(/^I should see a "([^"]*)" text in element "([^"]*)"$/) do |text, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise "Text '#{text}' not found in #{element}" unless has_content?(text)
  end
end

Then(/^I should not see a "([^"]*)" text in element "([^"]*)"$/) do |text, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise "Text '#{text}' found in #{element}" if has_content?(text)
  end
end

Then(/^I should see a "([^"]*)" or "([^"]*)" text in element "([^"]*)"$/) do |text1, text2, element|
  within(:xpath, "//div[@id=\"#{element}\" or @class=\"#{element}\"]") do
    raise "Texts #{text1} and #{text2} not found in #{element}" unless has_content?(text1) || has_content?(text2)
  end
end

Then(/^I should see a "([^"]*)" link in the table (.*) column$/) do |link, column|
  idx = %w[first second third fourth].index(column)
  unless idx
    # try column by name
    # unquote if neeeded
    colname = column.gsub(/\A['"]+|['"]+\Z/, '')
    cols = all(:xpath, '//table//thead/tr[1]/th').map(&:text)
    idx = cols.index(colname)
  end
  raise("Unknown column '#{column}'") unless idx
  # find(:xpath, "//table//thead//tr/td[#{idx + 1}]/a[text()='#{link}']")
  raise unless has_xpath?("//table//tr/td[#{idx + 1}]//a[text()='#{link}']")
end

When(/^I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows$/) do
  # this step is used for long operations like refreshing caches, repositories, etc.
  # therefore we use a non-standard timeout
  repeat_until_timeout(timeout: 800, message: 'Task does not look FINISHED yet') do
    visit current_url
    # get all texts in the table column under the "Status" header
    status_tds = "//tr/td[count(//th[contains(*/text(), 'Status')]/preceding-sibling::*) + 1]"
    statuses = all(:xpath, status_tds).map(&:text)

    # get all texts in the table column under the "Start time" header
    start_time_tds = "//tr/td[count(//th[contains(*/text(), 'Start Time')]/preceding-sibling::*) + 1]"
    start_times = all(:xpath, start_time_tds).map(&:text)

    # disregard any number of initial unimportant rows, that is:
    #  - INTERRUPTED rows with no start time (expected when Taskomatic had been restarted)
    #  - SKIPPED rows (expected when Taskomatic triggers the same task concurrently)
    first_non_skipped = statuses.zip(start_times).drop_while do |status, start_time|
      (status == 'INTERRUPTED' && (start_time.empty? || start_time == 'Task never started')) || status == 'SKIPPED'
    end.first.first

    # halt in case we are done, or if an error is detected
    break if first_non_skipped == 'FINISHED'
    raise('Taskomatic task was INTERRUPTED') if first_non_skipped == 'INTERRUPTED'

    # otherwise either no row is shown yet, or the task is still RUNNING
    # continue waiting
    sleep 1
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

Then(/^I should see a "([^"]*)" button in "([^"]*)" form$/) do |arg1, arg2|
  within(:xpath, "//form[@id='#{arg2}' or @name=\"#{arg2}\"]") do
    raise "Button #{arg1} not found" unless find_button(arg1)
  end
end

Then(/^I should not see a warning sign$/) do
  raise 'Warning detected' unless page.has_no_xpath?("//*[contains(@class, 'fa fa-li fa-exclamation-triangle text-warning')]")
end

Then(/^I select the "([^"]*)" repo$/) do |repo|
  step %(I check "#{repo}" in the list)
end

Then(/^I check the row with the "([^"]*)" link$/) do |text|
  step %(I check "#{text}" in the list)
end

Then(/^I check the row with the "([^"]*)" text$/) do |text|
  step %(I check "#{text}" in the list)
end

When(/^I check the first patch in the list$/) do
  step %(I check the first row in the list)
end

When(/^I click on the red confirmation button$/) do
  find_and_wait_click('button.btn-danger').click
end

When(/^I click on the clear SSM button$/) do
  find_and_wait_click(:xpath, "//*[@id='clear-ssm']").click
end

When(/^I click on the filter button$/) do
  find_and_wait_click('button.spacewalk-button-filter').click
  has_text?('is filtered', wait: 10)
end

Then(/^I click on the filter button until page does not contain "([^"]*)" text$/) do |text|
  repeat_until_timeout(message: "'#{text}' still found") do
    break unless has_content?(text)
    find('button.spacewalk-button-filter').click
    has_text?('is filtered', wait: 10)
  end
end

Then(/^I click on the filter button until page does contain "([^"]*)" text$/) do |text|
  repeat_until_timeout(message: "'#{text}' was not found") do
    break if has_content?(text)
    find('button.spacewalk-button-filter').click
    has_text?('is filtered', wait: 10)
  end
end

When(/^I enter the hostname of "([^"]*)" as the filtered system name$/) do |host|
  system_name = get_system_name(host)
  find("input[placeholder='Filter by System Name: ']").set(system_name)
end

When(/^I enter "([^"]*)" as the filtered package name$/) do |input|
  find("input[placeholder='Filter by Package Name: ']").set(input)
end

When(/^I enter "([^"]*)" as the filtered synopsis$/) do |input|
  find("input[placeholder='Filter by Synopsis: ']").set(input)
end

When(/^I enter "([^"]*)" as the filtered channel name$/) do |input|
  find("input[placeholder='Filter by Channel Name: ']").set(input)
end

When(/^I enter "([^"]*)" as the filtered product description$/) do |input|
  find("input[name='product-description-filter']").set(input)
end

When(/^I enter "([^"]*)" as the filtered XCCDF result type$/) do |input|
  find("input[placeholder='Filter by Result: ']").set(input)
end

When(/^I enter "([^"]*)" as the filtered snippet name$/) do |input|
  find("input[placeholder='Filter by Snippet Name: ']").set(input)
end

When(/^I enter the package for "([^"]*)" as the filtered package name$/) do |host|
  step %(I enter "#{PACKAGE_BY_CLIENT[host]}" as the filtered package name)
end

When(/^I check the package for "([^"]*)" in the list$/) do |host|
  step %(I check "#{PACKAGE_BY_CLIENT[host]}" in the list)
end

When(/^I check row with "([^"]*)" and arch of "([^"]*)"$/) do |text, client|
  arch = PKGARCH_BY_CLIENT[client]
  step %(I check row with "#{text}" and "#{arch}" in the list)
end

When(/^I uncheck row with "([^"]*)" and arch of "([^"]*)"$/) do |text, client|
  arch = PKGARCH_BY_CLIENT[client]
  step %(I uncheck row with "#{text}" and "#{arch}" in the list)
end

When(/^I check row with "([^"]*)" and "([^"]*)" in the list$/) do |text1, text2|
  top_level_xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{text1}')] and .//td[contains(.,'#{text2}')]]//input[@type='checkbox']"
  row = find(:xpath, top_level_xpath_query)
  raise "xpath: #{top_level_xpath_query} not found" if row.nil?

  row.set(true)
end

When(/^I uncheck row with "([^"]*)" and "([^"]*)" in the list$/) do |text1, text2|
  top_level_xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{text1}')] and .//td[contains(.,'#{text2}')]]//input[@type='checkbox']"
  row = find(:xpath, top_level_xpath_query, match: :first)
  raise "xpath: #{top_level_xpath_query} not found" if row.nil?

  row.set(false)
end

When(/^I check the first row in the list$/) do
  within(:xpath, '//section') do
    row = find(:xpath, "//div[@class='table-responsive']/table/tbody/tr[.//td]", match: :first)
    row.find(:xpath, ".//input[@type='checkbox']", match: :first).set(true)
  end
end

When(/^I check "([^"]*)" in the list$/) do |text|
  top_level_xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{text}')]]//input[@type='checkbox']"
  row = find(:xpath, top_level_xpath_query, match: :first)
  raise "xpath: #{top_level_xpath_query} not found" if row.nil?

  row.set(true)
end

#
# Test if an option is selected
#
Then(/^option "([^"]*)" is selected as "([^"]*)"$/) do |option, field|
  next if has_select?(field, selected: option)

  # Custom React selector
  next if has_xpath?("//*[contains(@class, 'data-testid-#{field}-child__value-container')]/*[contains(text(),'#{option}')]")

  raise "#{option} is not selected as #{field}"
end

#
# Wait for an option to appear in a list
#
When(/^I wait until option "([^"]*)" appears in list "([^"]*)"$/) do |option, field|
  repeat_until_timeout(message: "#{option} has not been listed in #{field}") do
    break if has_select?(field, with_options: [option])

    # Custom React selector
    break if has_xpath?("//*[contains(@class, 'data-testid-#{field}-child__value-container')]/*[contains(text(),'#{option}')]")
  end
end

#
# Test if a radio button is checked
#
Then(/^radio button "([^"]*)" is checked$/) do |arg1|
  raise "#{arg1} is unchecked" unless has_checked_field?(arg1)
end

#
# Test if a checkbox is checked
#
Then(/^I should see "([^"]*)" as checked$/) do |arg1|
  raise "#{arg1} is unchecked" unless has_checked_field?(arg1)
end

#
# Test if a checkbox is unchecked
#
Then(/^I should see "([^"]*)" as unchecked$/) do |arg1|
  raise "#{arg1} is checked" unless has_unchecked_field?(arg1)
end

#
# Test if a checkbox is disabled
#
Then(/^the "([^\"]*)" checkbox should be disabled$/) do |arg1|
  has_css?("##{arg1}[disabled]")
end

Then(/^the "([^\"]*)" field should be disabled$/) do |arg1|
  has_css?("##{arg1}[disabled]")
end

# identifier must be the value of name, ID or other element attribute, not the shown text
Then(/^I should see "([^"]*)" in field identified by "([^"]*)"$/) do |text, field|
  raise "'#{text}' not found in #{field}" unless find_field(field, with: /#{text}/).visible?
end

Then(/^I should see a "([^"]*)" field in "([^"]*)" form$/) do |field, form|
  within(:xpath, "//form[@id=\"#{form}\"] | //form[@name=\"#{form}\"]") do
    raise "Field #{field} not found" unless find_field(field, match: :first).visible?
  end
end

Then(/^I should see a "([^"]*)" editor in "([^"]*)" form$/) do |editor, form|
  within(:xpath, "//form[@id=\"#{form}\"] | //form[@name=\"#{form}\"]") do
    raise "xpath: textarea##{editor} not found" unless find("textarea##{editor}", visible: false)
    raise "css: ##{editor}-editor not found" unless has_css?("##{editor}-editor")
  end
end

Then(/^I should see a Sign Out link$/) do
  raise unless has_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I should see (\d+) "([^"]*)" fields in "([^"]*)" form$/) do |count, name, id|
  within(:xpath, "//form[@id=\"#{id}\" or  @name=\"#{id}\"]") do
    raise "#{id} form has not #{count} fields with name #{name}" unless has_field?(name, count: count.to_i)
  end
end

# Click on a button in a modal window with a specific title
When(/^I click on "([^"]*)" in "([^"]*)" modal$/) do |btn, title|
  path = "//*[text() = \"#{title}\"]" \
    '/ancestor::div[contains(@class, "modal-dialog")]'

  # We wait until the element becomes visible, because
  # the fade in animation might still be in progress
  repeat_until_timeout(message: "It couldn't find the #{title} modal dialog") do
    break if has_xpath?(path, wait: 1)
  end

  within(:xpath, path) do
    click_button(btn, wait: 5)
  end

  # We wait until the element is not shown, because
  # the fade out animation might still be in progress
  repeat_until_timeout(message: "The #{title} modal dialog is still present") do
    begin
      break if has_no_xpath?(path, wait: 1)
    rescue Selenium::WebDriver::Error::StaleElementReferenceError
      # We need to consider the case that after obtaining the element it is detached from the page document
      break
    end
  end
end

# Wait until a modal window with a specific content is shown
When(/^I wait at most (\d+) seconds until I see modal containing "([^"]*)" text$/) do |timeout, title|
  path = "//*[contains(@class, \"modal-content\") and contains(., \"#{title}\")]" \
    '/ancestor::div[contains(@class, "modal-dialog")]'

  dialog = find(:xpath, path, wait: timeout.to_i)
  raise "#{title} modal did not appear" unless dialog
end

# Check a Prometheus exporter
When(/^I check "([^"]*)" exporter$/) do |exporter_type|
  step %(I check "exporters##{exporter_type}_exporter#enabled")
end

# Check the Blackbox Exporter in the Prometheus formula
When(/^I check the blackbox exporter$/) do
  step %(I check "prometheus#blackbox_exporter#enabled")
end

# Navigate to a service endpoint
When(/^I visit "([^"]*)" endpoint of this "([^"]*)"$/) do |service, host|
  node = get_target(host)
  system_name = get_system_name(host)
  os_family = node.os_family
  port, protocol, path, text = case service
                               when 'Proxy' then [443, 'https', '/pub/', 'Index of /pub']
                               when 'Prometheus' then [9090, 'http', '', 'graph']
                               when 'Prometheus node exporter' then [9100, 'http', '', 'Node Exporter']
                               when 'Prometheus apache exporter' then [9117, 'http', '', 'Apache Exporter']
                               when 'Prometheus postgres exporter' then [9187, 'http', '', 'Postgres Exporter']
                               when 'Grafana' then [3000, 'http', '', 'Grafana Labs']
                               else raise "Unknown port for service #{service}"
                               end
  # debian based systems don't come with curl installed
  if (os_family.include? 'debian') || (os_family.include? 'ubuntu')
    node.run_until_ok("wget --no-check-certificate -qO- #{protocol}://#{system_name}:#{port}#{path} | grep -i '#{text}'")
  else
    node.run_until_ok("curl -s -k #{protocol}://#{system_name}:#{port}#{path} | grep -i '#{text}'")
  end
end

When(/^I enter the "([^"]*)" hostname as the Prometheus URL$/) do |host|
  node = get_target(host)
  step %(I enter "http://#{node.full_hostname}:9090" as "Prometheus URL")
end

When(/^I select the next maintenance window$/) do
  find(:xpath, "//select[@id='maintenance-window-select']/option", match: :first).select_option
end

When(/^I enter the server hostname as the redfish server address$/) do
  step %(I enter "#{$server.full_hostname}:8443" as "powerAddress")
end

When(/^I clear browser cookies$/) do
  page.driver.browser.manage.delete_all_cookies
end

When(/^I close the modal dialog$/) do
  find(:xpath, "//*[contains(@class, 'modal-header')]/button[contains(@class, 'close')]").click
end

When(/^I refresh the page$/) do
  begin
    accept_prompt do
      execute_script 'window.location.reload()'
    end
  rescue Capybara::ModalNotFound
    # ignored
  end
end

When(/^I make a list of the existing systems$/) do
  system_elements_list = find_all(:xpath, "//td[contains(@class, 'sortedCol')]")
  $systems_list = []
  system_elements_list.each { |el| $systems_list << el.text }
end

Given(/^I have a property "([^"]*)" with value "([^"]*)" on "([^"]*)"$/) do |property_name, property_value, host|
  steps %(
    When I am on the Systems overview page of this "#{host}"
    And I follow "Properties" in the content area
    And I enter "#{property_value}" as "#{property_name}"
    And I click on "Update Properties"
    Then I should see a "System properties changed" text
    And I clean the search index on the server
        )
end

Given(/^I have a combobox property "([^"]*)" with value "([^"]*)" on "([^"]*)"$/) do |property_name, property_value, host|
  steps %(
    When I am on the Systems overview page of this "#{host}"
    And I follow "Properties" in the content area
    And I select "#{property_value}" from "#{property_name}"
    And I click on "Update Properties"
    Then I should see a "System properties changed" text
    And I clean the search index on the server
        )
end

# Confirm user has landed on a system's overview page
Then(/^I should land on system's overview page$/) do
  steps %(
    Then I should see a "System Status" text
    And I should see a "System Info" text
    And I should see a "System Events" text
    And I should see a "System Properties" text
    And I should see a "Subscribed Channels" text
        )
end

# In case of a search reindex not having finished yet, keep retrying until successful search or timeout
When(/^I click on the search button$/) do
  click_button_and_wait('Search', match: :first)
  # after a search reindex, the UI will show a "Could not connect to search server" followed by a false "No matches found" for a while
  if has_text?('Could not connect to search server.', wait: 0)
    repeat_until_timeout(message: 'Could not perform a successful search after reindexation', timeout: 10) do
      break unless has_text?('Could not connect to search server.', wait: 0) || has_text?('No matches found', wait: 0)
      sleep 1
      click_button('Search', match: :first, wait: false)
    end
  end
end

When(/^I enter "([^"]*)" hostname on the search field$/) do |host|
  system_name = get_system_name(host)
  step %(I enter "#{system_name}" on the search field)
end

When(/^I enter "([^"]*)" hostname on grafana's host field$/) do |host|
  step %(I click on "var-hostname")
  system_name = get_system_name(host)
  step %(I enter "#{system_name}" as "Enter variable value")
  send_keys(:return)
end

Then(/^I should see "([^"]*)" hostname as first search result$/) do |host|
  system_name = get_system_name(host)
  within(:xpath, '//section') do
    row = find(:xpath, "//div[@class='table-responsive']/table/tbody/tr[.//td]", match: :first)
    within(row) do
      raise "Text '#{system_name}' not found" unless has_text?(system_name)
    end
  end
end

When(/^I enter "([^"]*)" as the left menu search field$/) do |search_text|
  step %(I enter "#{search_text}" as "nav-search")
end

Then(/^I should see left menu empty$/) do
  raise StandardError, 'The left menu is not empty.' unless page.has_no_xpath?("//*[contains(@class, 'level1')]/*/*[contains(@class, 'nodeLink')]")
end

Then(/^I should see the text "(.*?)" in the (Operating System|Architecture|Channel Label) field/) do |text, field|
  page.has_field?(text, with: field)
end

Then(/^I should see the correct timestamp for task "([^"]*)"/) do |task_name|
  now = Time.now
  execute_script 'window.stop()'
  # find row with corresponding task name
  page.find_all(:xpath, "//table[@class='table table-responsive']//tr").each do |tr|
    next unless tr.has_text?(task_name)
    # if task name is found, iterate through the columns to find the timestamp
    page.find_all(:xpath, "//table[@class='table table-responsive']//td").each do |td|
      # if a text matching the format xx:xx is found, get and save the text
      next unless td.text.match(/\d{2}:\d{2}/)
      # Text from cell, parsed to a Time object must match now +- 5 seconds
      Time.parse(td.text).to_i.between?(now.to_i - 5, now.to_i + 5)
    end
  end
end

When(/^I visit the grafana dashboards of this "([^"]*)"$/) do |host|
  node = get_target(host)
  visit("http://#{node.public_ip}:3000/dashboards")
end
