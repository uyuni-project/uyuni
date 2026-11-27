# Copyright (c) 2025 SUSE LLC.
# SPDX-License-Identifier: MIT

# Function to check or uncheck a checkbox in a the package list with the possibility to select the last package version
#
# @param action [String] Either 'check' or 'uncheck'
# @param text [String] The text to match in the row
# @param last_version [Boolean] Whether to select the row with the latest package version
def toggle_checkbox_in_package_list(action, text, last_version: false)
  return toggle_checkbox_in_list(action, text) unless last_version

  begin
    link_elements = all(:xpath, "//table/tbody/tr/td[contains(@class, 'sortedCol')]/a")
    packages      = link_elements.map(&:text)
    latest        = latest_package(packages)

    xpath = "//table/tbody/tr/td[contains(@class, 'sortedCol')]/a[text()='#{latest}']/ancestor::tr//input[@type='checkbox']"
    row   = find(:xpath, xpath, match: :first)
    row.set(action == 'check')
  rescue StandardError => e
    warn "[toggle_checkbox] fallback to text match: #{e.message}"
    toggle_checkbox_in_list(action, text)
  end
end

# Function to check or uncheck a checkbox in a table first row matching the provided text
#
# @param action [String] Either 'check' or 'uncheck'
# @param text [String] The text to match in the row
def toggle_checkbox_in_list(action, text)
  top_level_xpath_query = "//table/tbody/tr[.//td[contains(.,'#{text}')]]//input[@type='checkbox']"
  row = find(:xpath, top_level_xpath_query, match: :first)
  raise "xpath: #{top_level_xpath_query} not found" if row.nil?

  row.set(action == 'check')
end

# Function to enter a package name into the "Filter by Package Name" input field
#
# @param package_name [String] The package name to enter
# @raise [ArgumentError] If the package name is empty
def filter_by_package_name(package_name)
  raise ArgumentError, 'Package name is not set' if package_name.empty?

  find("input[placeholder='Filter by Package Name: ']").set(package_name)
end

# Toggles a checkbox based on the desired action.
#
# @param action [String] Either 'check' or 'uncheck'
# @param id [String] The HTML id of the checkbox input element
#
# This function ensures the checkbox ends in the desired state by comparing
# the current checked status with the intended one. It performs a click only
# if necessary (e.g., when current and desired states differ).
#
# Example:
#   toggle_checkbox('check', 'digitFlag')   # Ensures checkbox is checked
#   toggle_checkbox('uncheck', 'digitFlag') # Ensures checkbox is unchecked
def toggle_checkbox(action, id)
  checkbox = find("##{id}", visible: :all)
  desired_state = (action == 'check')

  checkbox.click if checkbox.checked? != desired_state
end

# Returns the textual state of a checkbox (toggle) based on its HTML ID.
#
# @param id [String] The ID of the checkbox element.
# @return [String] "checked" if the box is selected, "unchecked" otherwise.
def checkbox_state(id)
  checkbox = find("##{id}", visible: :all)
  checkbox.checked? ? 'checked' : 'unchecked'
end

# Menu path link
def follow_left_menu(menu_path)
  # split menu levels separated by '>' character
  menu_levels = menu_path.split('>').map(&:strip)

  # define reusable patterns
  prefix_path = '//aside/div[@id=\'nav\']/nav'
  link_path = '/ul/li/div/a[contains(.,\'%s\')]'
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
    begin
      unless find(:xpath, target_link_path + parent_wrapper_path + parent_level_path)[:class]&.include?('open')
        find(:xpath, target_link_path + parent_wrapper_path).click
        # wait for the 'open' class to be applied before navigating into the submenu
        find(:xpath, "#{target_link_path}#{parent_wrapper_path}#{parent_level_path}[contains(@class,'open')]")
      end
    rescue NoMethodError
      warn 'The browser session seems broken. See debug details below:'
      warn "target_link_path=#{target_link_path}, parent_wrapper_path=#{parent_wrapper_path}, parent_level_path=#{parent_level_path}"
      step 'I am authorized for the "Admin" section'
    end
    # point the target to the current menu level
    target_link_path += parent_wrapper_path + parent_level_path
  end
  # finally go to the target page
  find_and_wait_click(:xpath, target_link_path).click
end

# Navigates to a URL with retry if Playwright aborts the navigation (e.g. net::ERR_ABORTED).
def visit_with_retry(url)
  visit(url)
rescue Playwright::Error => e
  raise unless e.message.include?('net::ERR_ABORTED')

  # net::ERR_ABORTED can fire when previous in-flight navigations or SUMA's SSE/streaming
  # connections collide with a new page navigation. Wait briefly for connections to settle and retry once.
  warn "Navigation to #{url} aborted (#{e.message.lines.first.chomp}) — retrying once"
  sleep 1
  visit(url)
end

# Logs the web UI in as the given user, on the server the browser currently points at.
def login_as_user(user, passwd)
  # Record the identity for whichever host the web UI is currently pointed at, so
  # API/spacecmd calls against that same host authenticate as the right user.
  Credentials.login_as(user, passwd)
  begin
    page.reset!
  rescue NoMethodError => e
    log "The browser session could not be cleaned because there is no browser available: #{e.message}"
    capybara_register_driver
    # Reset only the current session's driver. Capybara.reset_sessions! would reset every
    # named session in the pool, including peripheral servers parked via switch_to_server/
    # using_server, silently logging them out and defeating session reuse across hosts.
    Capybara.current_session.reset!
  rescue StandardError => e
    log "The browser session could not be cleaned for unknown issue: #{e.message}"
    capybara_register_driver
    Capybara.current_session.reset!
  ensure
    begin
      visit Capybara.app_host
    rescue Playwright::Error => e
      # net::ERR_ABORTED can fire when SUMA's SSE/streaming connection collides with a new
      # page navigation. Wait briefly for connections to settle and retry once.
      warn "Navigation to #{Capybara.app_host} aborted (#{e.message.lines.first.chomp}) — retrying once"
      sleep 1
      visit Capybara.app_host
    rescue Playwright::Transport::AlreadyDisconnectedError => e
      # BUG-024/BUG-025: unlike Playwright::Error above, this means the Node driver's pipe is
      # already dead (browser process crashed or was killed, e.g. by the env.rb scenario
      # watchdog) - retrying visit on the same transport would just raise the same error again.
      # Rebuild the driver and session before retrying so the next scenario gets a live browser.
      warn "Browser transport already disconnected (#{e.message}) — rebuilding driver and retrying"
      capybara_register_driver
      Capybara.current_session.reset!
      visit Capybara.app_host
    end
  end
  begin
    return if all(:xpath, "//header//span[text()='#{user}']", wait: IMMEDIATE_WAIT).any?
  rescue NoMethodError, Capybara::NotSupportedByDriverError
    # driver is not ready yet after session reset, proceed to full login
  end

  begin
    find(:xpath, '//header//i[@class=\'fa fa-sign-out\']').click
  rescue Capybara::ElementNotFound, NoMethodError
    # No need to logout
  end

  raise ScriptError, 'Login page is not correctly loaded' unless has_field?('username')

  fill_in('username', with: user)
  fill_in('password', with: passwd)
  click_button_and_wait('Sign In', match: :first)

  step 'I should be logged in'
end

# Types the text in the description filter of the Products page.
def filter_products_by_description(input)
  find('input[name=\'product-description-filter\']').set(input)
end

# Selects or deselects ('select' or 'deselect') a product on the Products page.
def set_product_selection(select, product)
  # the product checkbox is a tri-state widget: selecting a parent whose subtree isn't
  # entirely selected yet legitimately leaves it "indeterminate" rather than fully checked,
  # so Capybara's #set click-and-verify (which waits for a plain checked=true/false) never
  # resolves and Playwright raises "Clicking the checkbox did not change its state"; click it
  # directly and poll until it joins/leaves the selection (checked or indeterminate) instead
  xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/input[@type='checkbox']"
  desired_selected = (select == 'select')
  checkbox = find(:xpath, xpath)
  indeterminate = page.evaluate_script("document.getElementById('#{checkbox[:id]}').indeterminate")
  checkbox.click unless (checkbox.checked? || indeterminate) == desired_selected
  repeat_until_timeout(message: "checkbox for product #{product} did not reach '#{select}' state") do
    checkbox = find(:xpath, xpath)
    indeterminate = page.evaluate_script("document.getElementById('#{checkbox[:id]}').indeterminate")
    break if (checkbox.checked? || indeterminate) == desired_selected

    sleep 1
  end
end

# Opens the sub-list of a product; a missing product is an error unless if_present is true.
def open_product_sublist(product, if_present: false)
  xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]/div/i[contains(@class, 'fa-angle-right')]"
  begin
    find(:xpath, xpath).click
  rescue Capybara::ElementNotFound
    raise ScriptError, "xpath: #{xpath} not found" unless if_present
  end
end

# Waits until the Products page shows the product as installed.
def wait_until_product_added(product)
  repeat_until_timeout(message: "Couldn't find the installed product #{product} in the list") do
    xpath = "//span[contains(text(), '#{product}')]/ancestor::div[contains(@class, 'product-details-wrapper')]"
    begin
      product_class = find(:xpath, xpath)[:class]
      break if !product_class.nil? && product_class.include?('product-installed')
    rescue Capybara::ElementNotFound => e
      log e
    end
    sleep 1
  end
end

# Clicks the Add Product button of the Products page.
def click_add_product_button
  raise ScriptError, 'xpath: button#addProducts not found' unless find('button#addProducts').click
end

# Logs in as admin on the server, opens the Products page of the Setup Wizard and filters it by description.
#
# @param description [String] The text to type in the description filter
def open_products_page_filtered_by(description)
  login_as_user('admin', 'admin')
  follow_left_menu('Admin > Setup Wizard > Products')
  raise ScriptError, "Text 'currently running' found" unless has_no_text?('currently running', wait: DEFAULT_TIMEOUT)
  raise ScriptError, "Text 'Loading' found" unless has_no_text?('Loading', wait: DEFAULT_TIMEOUT)

  filter_products_by_description(description)
end
