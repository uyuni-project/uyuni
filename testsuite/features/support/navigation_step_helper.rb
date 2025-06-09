# Copyright (c) 2025 SUSE LLC.
# Licensed under the terms of the MIT license.

# Function to check or uncheck a checkbox in a table row matching the provided text
#
# @param action [String] Either 'check' or 'uncheck'
# @param text [String] The text to match in the row
# @param last_version [Boolean] Whether to select the row with the latest version
def toggle_checkbox_in_package_list(action, text, last_version: false)
  if last_version
    link_elements = all(:xpath, "//div[@class='table-responsive']/table/tbody/tr/td[@class=' sortedCol']/a")
    packages_list = link_elements.map(&:text)
    latest = latest_package(packages_list)
    top_level_xpath_query = "//div[@class='table-responsive']/table/tbody/tr/td[@class=' sortedCol']/a[text()='#{latest}']/../../td/input[@type='checkbox']"
  else
    top_level_xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{text}')]]//input[@type='checkbox']"
  end

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
