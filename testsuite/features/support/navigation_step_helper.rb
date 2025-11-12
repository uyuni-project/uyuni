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
    link_elements = all(:xpath, "//div[@class='table-responsive']/table/tbody/tr/td[@class=' sortedCol']/a")
    packages      = link_elements.map(&:text)
    latest        = latest_package(packages)

    xpath = "//div[@class='table-responsive']/table/tbody/tr/td[@class=' sortedCol']/a[text()='#{latest}']/../../td/input[@type='checkbox']"
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
  top_level_xpath_query = "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{text}')]]//input[@type='checkbox']"

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
