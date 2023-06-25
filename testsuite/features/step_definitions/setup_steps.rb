# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains all steps concerning setting up a test environment.
### This may include the creation and handeling of SCC credentials, adding products, etc.

# setup wizard

Then(/^HTTP proxy verification should have succeeded$/) do
  raise 'Success icon not found' unless find('i.text-success', wait: DEFAULT_TIMEOUT)
end

When(/^I enter the address of the HTTP proxy as "([^"]*)"$/) do |hostname|
  step %(I enter "#{$server_http_proxy}" as "#{hostname}")
end

When(/^I ask to add new credentials$/) do
  raise 'Click on plus icon failed' unless find('i.fa-plus-circle').click
end

When(/^I enter the SCC credentials$/) do
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  steps %(
    And I enter "#{scc_username}" as "edit-user"
    And I enter "#{scc_password}" as "edit-password"
  )
end

When(/^I wait until the SCC credentials are valid$/) do
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  within(:xpath, "//h3[contains(text(), '#{scc_username}')]/../..") do
    raise 'Success icon not found' unless find('i.text-success', wait: 30)
  end
end

Then(/^the credentials for "([^"]*)" should be invalid$/) do |user|
  within(:xpath, "//h3[contains(text(), '#{user}')]/../..") do
    raise 'Failure icon not found' unless find('i.text-danger', wait: DEFAULT_TIMEOUT)
  end
end

When(/^I make the credentials for "([^"]*)" primary$/) do |user|
  within(:xpath, "//h3[contains(text(), '#{user}')]/../..") do
    raise 'Click on star icon failed' unless find('i.fa-star-o').click
  end
end

Then(/^the credentials for "([^"]*)" should be primary$/) do |user|
  within(:xpath, "//h3[contains(text(), '#{user}')]/../..") do
    raise 'Star icon not selected' unless find('i.fa-star')
  end
end

When(/^I wait for the trash icon to appear for "([^"]*)"$/) do |user|
  within(:xpath, "//h3[contains(text(), '#{user}')]/../..") do
    repeat_until_timeout(message: 'Trash icon is still greyed out') do
      break unless find('i.fa-trash-o')[:style].include? "not-allowed"
      sleep 1
    end
  end
end

When(/^I ask to delete the credentials for "([^"]*)"$/) do |user|
  within(:xpath, "//h3[contains(text(), '#{user}')]/../..") do
    raise 'Click on trash icon failed' unless find('i.fa-trash-o').click
  end
end

When(/^I view the subscription list for "([^"]*)"$/) do |user|
  within(:xpath, "//h3[contains(text(), '#{user}')]/../..") do
    raise 'Click on list icon failed' unless find('i.fa-th-list').click
  end
end

When(/^I select "(.*?)" in the dropdown list of the architecture filter$/) do |architecture|
  # let the the select2js box filter open the hidden options
  xpath_query = "//div[@id='s2id_product-arch-filter']/ul/li/input"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
  # select the desired option
  raise "Architecture #{architecture} not found" unless find(:xpath, "//div[@id='select2-drop']/ul/li/div[contains(text(), '#{architecture}')]").click
end

When(/^I (deselect|select) "([^\"]*)" as a product$/) do |select, product|
  # click on the checkbox to select the product
  xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/input[@type='checkbox']"
  raise "xpath: #{xpath} not found" unless find(:xpath, xpath).set(select == "select")
end

When(/^I wait at most (\d+) seconds until the tree item "([^"]+)" has no sub-list$/) do |timeout, item|
  repeat_until_timeout(timeout: timeout.to_i, message: "could still find a sub list for tree item #{item}") do
    xpath = "//span[contains(text(), '#{item}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/i[contains(@class, 'fa-angle-')]"
    begin
      find(:xpath, xpath)
      sleep 1
    rescue Capybara::ElementNotFound
      break
    end
  end
end

When(/^I wait at most (\d+) seconds until the tree item "([^"]+)" contains "([^"]+)" text$/) do |timeout, item, text|
  within(:xpath, "//span[contains(text(), '#{item}')]/ancestor::div[contains(@class, 'product-details-wrapper')]") do
    raise "could not find text #{text} for tree item #{item}" unless has_text?(text, wait: timeout.to_i)
  end
end

When(/^I wait at most (\d+) seconds until the tree item "([^"]+)" contains "([^"]+)" button$/) do |timeout, item, button|
  xpath_query = "//span[contains(text(), '#{item}')]/"\
      "ancestor::div[contains(@class, 'product-details-wrapper')]/descendant::*[@title='#{button}']"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query, wait: timeout.to_i)
end

When(/^I open the sub-list of the product "(.*?)"$/) do |product|
  xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/i[contains(@class, 'fa-angle-right')]"
  raise "xpath: #{xpath} not found" unless find(:xpath, xpath).click
end

When(/^I select the addon "(.*?)"$/) do |addon|
  # click on the checkbox of the sublist to select the addon product
  xpath = "//span[contains(text(), '#{addon}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/input[@type='checkbox']"
  raise "xpath: #{xpath} not found" unless find(:xpath, xpath).set(true)
end

Then(/^I should see that the "(.*?)" product is "(.*?)"$/) do |product, recommended|
  xpath = "//span[text()[normalize-space(.) = '#{product}'] and ./span/text() = '#{recommended}']"
  raise "xpath: #{xpath} not found" unless find(:xpath, xpath)
end

Then(/^I should see the "(.*?)" selected$/) do |product|
  xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]"
  within(:xpath, xpath) do
    raise "#{find(:xpath, '.')['data-identifier']} is not checked" unless find(:xpath, "./div/input[@type='checkbox']").checked?
  end
end

When(/^I wait until I see "(.*?)" product has been added$/) do |product|
  repeat_until_timeout(message: "Couldn't find the installed product #{product} in the list") do
    xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]"
    begin
      product_class = find(:xpath, xpath)[:class]
      unless product_class.nil?
        break if product_class.include?('product-installed')
      end
    rescue Capybara::ElementNotFound => e
      log e
    end
    sleep 1
  end
end

When(/^I click the Add Product button$/) do
  raise "xpath: button#addProducts not found" unless find('button#addProducts').click
end

Then(/^the SLE15 (SP3|SP4) product should be added$/) do |sp_version|
  output, _code = $server.run('echo -e "admin\nadmin\n" | mgr-sync list channels', check_errors: false, buffer_size: 1_000_000)
  STDOUT.puts "Products list:\n#{output}"
  match = "[I] SLE-Product-SLES15-#{sp_version}-Pool for x86_64 SUSE Linux Enterprise Server 15 #{sp_version} x86_64 [sle-product-sles15-#{sp_version.downcase}-pool-x86_64]"
  raise "Not included:\n #{match}" unless output.include? match
  match = "[I] SLE-Module-Basesystem15-#{sp_version}-Updates for x86_64 Basesystem Module 15 #{sp_version} x86_64 [sle-module-basesystem15-#{sp_version.downcase}-updates-x86_64]"
  raise "Not included:\n #{match}" unless output.include? match
  match = "[I] SLE-Module-Server-Applications15-#{sp_version}-Pool for x86_64 Server Applications Module 15 #{sp_version} x86_64 [sle-module-server-applications15-#{sp_version.downcase}-pool-x86_64]"
  raise "Not included:\n #{match}" unless output.include? match
end

When(/^I click the channel list of product "(.*?)"$/) do |product|
  xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/button[contains(@class, 'showChannels')]"
  raise "xpath: #{xpath} not found" unless find(:xpath, xpath).click
end

# configuration management steps

Then(/^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/) do |arg1, arg2, arg3|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
    raise "Link #{arg2} not found" unless find_link(arg2)
    raise "Link #{arg3} not found" unless find_link(arg3)
  end
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
    raise "Link #{arg2} not found" unless find_link(arg2)
  end
end

Then(/^a table line should contain system "([^"]*)", "([^"]*)"$/) do |host, text|
  system_name = get_system_name(host)
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{system_name}')]]") do
    raise "Text #{text} not found" unless find_all(:xpath, "//td[contains(., '#{text}')]")
  end
end

# Register client

Given(/^I update the profile of "([^"]*)"$/) do |client|
  node = get_target(client)
  node.run('rhn-profile-sync', timeout: 500)
end

When(/^I wait until onboarding is completed for "([^"]*)"$/) do |host|
  steps %(
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "#{host}", refreshing the page
    And I follow this "#{host}" link
    And I wait 180 seconds until the event is picked up and 500 seconds until the event "Apply states" is completed
    And I wait 180 seconds until the event is picked up and 500 seconds until the event "Hardware List Refresh" is completed
    And I wait 180 seconds until the event is picked up and 500 seconds until the event "Package List Refresh" is completed
  )
end

Then(/^I should see "([^"]*)" via spacecmd$/) do |host|
  command = "spacecmd -u admin -p admin system_list"
  system_name = get_system_name(host)
  repeat_until_timeout(message: "system #{system_name} is not in the list yet") do
    $server.run("spacecmd -u admin -p admin clear_caches")
    result, _code = $server.run(command, check_errors: false, verbose: true)
    break if result.include? system_name
    sleep 1
  end
end

Then(/^I should see "([^"]*)" as link$/) do |host|
  system_name = get_system_name(host)
  step %(I should see a "#{system_name}" link)
end

When(/^I remember when I scheduled an action$/) do
  moment = "schedule_action"
  val = DateTime.now
  if defined?($moments)
    $moments[moment] = val
  else
    $moments = {moment => val}
  end
end

Then(/^I should see "([^"]*)" at least (\d+) minutes after I scheduled an action$/) do |text, minutes|
  # TODO: is there a better way then page.all ?
  elements = all('div', text: text)
  raise "Text #{text} not found in the page" if elements.nil?
  match = elements[0].text.match(/#{text}\s*(\d+\/\d+\/\d+ \d+:\d+:\d+ (AM|PM)+ [^\s]+)/)
  raise "No element found matching text '#{text}'" if match.nil?
  text_time = DateTime.strptime("#{match.captures[0]}", '%m/%d/%C %H:%M:%S %p %Z')
  raise "Time the action was scheduled not found in memory" unless defined?($moments) and $moments["schedule_action"]
  initial = $moments["schedule_action"]
  after = initial + Rational(1, 1440) * minutes.to_i
  raise "#{text_time.to_s} is not #{minutes} minutes later than '#{initial.to_s}'" unless (text_time + Rational(1, 1440)) >= after
end

# Valid claims:
#   - org
#   - onlyChannels
def token(secret, claims = {})
  payload = {}
  payload.merge!(claims)
  log secret
  JWT.encode payload, [secret].pack('H*').bytes.to_a.pack('c*'), 'HS256'
end

def server_secret
  rhnconf, _code = $server.run('cat /etc/rhn/rhn.conf', check_errors: false)
  data = /server.secret_key\s*=\s*(\h+)$/.match(rhnconf)
  data[1].strip
end

Given(/^I have a valid token for organization "([^"]*)"$/) do |org|
  @token = token(server_secret, org: org.to_i)
end

Given(/^I have an invalid token for organization "([^"]*)"$/) do |org|
  @token = token(SecureRandom.hex(64), org: org.to_i)
end

Given(/^I have an expired valid token for organization "([^"]*)"$/) do |org|
  yesterday = Time.now.to_i - 86_400
  @token = token(server_secret, org: org.to_i, exp: yesterday)
end

Given(/^I have a valid token expiring tomorrow for organization "([^"]*)"$/) do |org|
  tomorrow = Time.now.to_i + 86_400
  @token = token(server_secret, org: org.to_i, exp: tomorrow)
end

Given(/^I have a not yet usable valid token for organization "([^"]*)"$/) do |org|
  tomorrow = Time.now.to_i + 86_400
  @token = token(server_secret, org: org.to_i, nbf: tomorrow)
end

Given(/^I have a valid token for organization "(.*?)" and channel "(.*?)"$/) do |org, channel|
  @token = token(server_secret, org: org, onlyChannels: [channel])
end

Then(/^I should see the toggler "([^"]*)"$/) do |target_status|
  case target_status
  when 'enabled'
    xpath = "//i[contains(@class, 'fa-toggle-on')]"
    raise "xpath: #{xpath} not found" unless find(:xpath, xpath)
  when 'disabled'
    xpath = "//i[contains(@class, 'fa-toggle-off')]"
    raise "xpath: #{xpath} not found" unless find(:xpath, xpath)
  else
    raise 'Invalid target status.'
  end
end

When(/^I click on the "([^"]*)" toggler$/) do |target_status|
  case target_status
  when 'enabled'
    xpath = "//i[contains(@class, 'fa-toggle-on')]"
    raise "xpath: #{xpath} not found" unless find(:xpath, xpath).click
  when 'disabled'
    xpath = "//i[contains(@class, 'fa-toggle-off')]"
    raise "xpath: #{xpath} not found" unless find(:xpath, xpath).click
  else
    raise 'Invalid target status.'
  end
end

Then(/^I should see the child channel "([^"]*)" "([^"]*)"$/) do |target_channel, target_status|
  step %(I should see a "#{target_channel}" text)

  xpath = "//label[contains(text(), '#{target_channel}')]"
  channel_checkbox_id = find(:xpath, xpath)['for']

  case target_status
  when 'selected'
    raise "#{channel_checkbox_id} is not selected" unless has_checked_field?(channel_checkbox_id)
  when 'unselected'
    raise "#{channel_checkbox_id} is selected" if has_checked_field?(channel_checkbox_id)
  else
    raise 'Invalid target status.'
  end
end

Then(/^I should see the child channel "([^"]*)" "([^"]*)" and "([^"]*)"$/) do |target_channel, target_status, is_disabled|
  step %(I should see a "#{target_channel}" text)

  xpath = "//label[contains(text(), '#{target_channel}')]"
  channel_checkbox_id = find(:xpath, xpath)['for']
  "disabled".eql?(is_disabled) || raise('Invalid disabled flag value')

  case target_status
  when 'selected'
    raise "#{channel_checkbox_id} is not selected" unless has_checked_field?(channel_checkbox_id, disabled: true)
  when 'unselected'
    raise "#{channel_checkbox_id} is selected" if has_checked_field?(channel_checkbox_id, disabled: true)
  else
    raise 'Invalid target status.'
  end
end

When(/^I select the child channel "([^"]*)"$/) do |target_channel|
  step %(I should see a "#{target_channel}" text)

  xpath = "//label[contains(text(), '#{target_channel}')]"
  channel_checkbox_id = find(:xpath, xpath)['for']

  raise "Field #{channel_checkbox_id} is checked" if has_checked_field?(channel_checkbox_id)
  find(:xpath, "//input[@id='#{channel_checkbox_id}']").click
end

Then(/^I should see "([^"]*)" "([^"]*)" for the "([^"]*)" channel$/) do |target_radio, target_status, target_channel|
  xpath = "//a[contains(text(), '#{target_channel}')]"
  channel_id = find(:xpath, xpath)['href'].split('?')[1].split('=')[1]

  case target_radio
  when 'No change'
    xpath = "//input[@type='radio' and @name='ch_action_#{channel_id}' and @value='NO_CHANGE']"
  when 'Subscribe'
    xpath = "//input[@type='radio' and @name='ch_action_#{channel_id}' and @value='SUBSCRIBE']"
  when 'Unsubscribe'
    xpath = "//input[@type='radio' and @name='ch_action_#{channel_id}' and @value='UNSUBSCRIBE']"
  else
    log "Target Radio #{target_radio} not supported"
  end

  case target_status
  when 'selected'
    raise "xpath: #{xpath} is not selected" if find(:xpath, xpath)['checked'].nil?
  when 'unselected'
    raise "xpath: #{xpath} is selected" unless find(:xpath, xpath)['checked'].nil?
  else
    log "Target status #{target_status} not supported"
  end
end

Then(/^the notification badge and the table should count the same amount of messages$/) do
  table_notifications_count = count_table_items

  badge_xpath = "//i[contains(@class, 'fa-bell')]/following-sibling::*[text()='#{table_notifications_count}']"

  if table_notifications_count == '0'
    log "All notification-messages are read, I expect no notification badge"
    raise "xpath: #{badge_xpath} found" if has_xpath?(badge_xpath)
  else
    log "Unread notification-messages count = " + table_notifications_count
    raise "xpath: #{badge_xpath} not found" unless find(:xpath, badge_xpath)
  end
end

When(/^I wait until radio button "([^"]*)" is checked, refreshing the page$/) do |arg1|
  unless has_checked_field?(arg1)
    repeat_until_timeout(message: "Couldn't find checked radio button #{arg1}") do
      break if has_checked_field?(arg1)
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

When(/^I wait until "([^"]*)" has been checked$/) do |text|
  unless has_checked_field?(text)
    repeat_until_timeout(message: "Couldn't find checked #{text}", timeout: 5) do
      break if has_checked_field?(text)

      sleep(1)
    end
  end
end

Then(/^I check the first notification message$/) do
  if count_table_items == '0'
    log "There are no notification messages, nothing to do then"
  else
    within(:xpath, '//section') do
      row = find(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td]", match: :first)
      row.find(:xpath, './/input[@type="checkbox"]', match: :first).set(true)
    end
  end
end

When(/^I delete it via the "([^"]*)" button$/) do |target_button|
  if count_table_items != '0'
    xpath_for_delete_button = "//button[@title='#{target_button}']"
    raise "xpath: #{xpath_for_delete_button} not found" unless find(:xpath, xpath_for_delete_button).click

    step %(I wait until I see "1 message deleted successfully." text)
  end
end

When(/^I mark as read it via the "([^"]*)" button$/) do |target_button|
  if count_table_items != '0'
    xpath_for_read_button = "//button[@title='#{target_button}']"
    raise "xpath: #{xpath_for_read_button} not found" unless find(:xpath, xpath_for_read_button).click

    step %(I wait until I see "1 message read status updated successfully." text)
  end
end

When(/^I check for failed events on history event page$/) do
  steps %(
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "System History" text
  )
  failings = ""
  event_table_xpath = "//div[@class='table-responsive']/table/tbody"
  rows = find(:xpath, event_table_xpath)
  rows.all('tr').each do |tr|
    if tr.all(:css, '.fa.fa-times-circle-o.fa-1-5x.text-danger').any?
      failings << "#{tr.text}\n"
    end
  end
  count_failures = failings.length
  raise "\nFailures in event history found:\n\n#{failings}" if count_failures.nonzero?
end

Then(/^I should see a list item with text "([^"]*)" and a (success|failing|warning|pending|refreshing) bullet$/) do |text, bullet_type|
  item_xpath = "//ul/li[text()='#{text}']/i[contains(@class, '#{BULLET_STYLE[bullet_type]}')]"
  find(:xpath, item_xpath)
end

When(/^I create the MU repositories for "([^"]*)"$/) do |client|
  repo_list = $custom_repositories[client]
  next if repo_list.nil?

  repo_list.each do |_repo_name, repo_url|
    unique_repo_name = generate_repository_name(repo_url)
    if repository_exist? unique_repo_name
      log "The MU repository #{unique_repo_name} was already created, we will reuse it."
    else
      content_type = deb_host?(client) ? 'deb' : 'yum'
      steps %(
        When I follow the left menu "Software > Manage > Repositories"
        And I follow "Create Repository"
        And I enter "#{unique_repo_name}" as "label"
        And I enter "#{repo_url.strip}" as "url"
        And I select "#{content_type}" from "contenttype"
        And I click on "Create Repository"
        Then I should see a "Repository created successfully" text or "The repository label '#{unique_repo_name}' is already in use" text
        And I should see "metadataSigned" as checked
      )
    end
  end
end

When(/^I select the MU repositories for "([^"]*)" from the list$/) do |client|
  repo_list = $custom_repositories[client]
  next if repo_list.nil?

  repo_list.each do |_repo_name, repo_url|
    unique_repo_name = generate_repository_name(repo_url)
    step %(I check "#{unique_repo_name}" in the list)
  end
end
