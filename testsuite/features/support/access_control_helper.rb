# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT

# Helpers for the role-based access control tests: the access group form (Admin > Access Control)
# and the API calls a user may be denied.

# Ticks or unticks the View or Modify checkbox of a row of the permission tree.
#
# The first column of a row holds the expander, then come the name, the description, View and Modify.
# The rows of the tree are named after a namespace segment ('config', 'overview', ...). Rows of the
# API namespaces carry a plug icon and are skipped, as a segment can exist in both trees.
# Ticking a parent row ticks all its children.
#
# @param row_name [String] The name of the row, for example 'config'.
# @param mode [String] The column: 'View' or 'Modify'.
# @param checked [Boolean] Whether the checkbox must be ticked or unticked.
def set_permission_checkbox(row_name, mode, checked)
  column = { 'View' => 4, 'Modify' => 5 }[mode]
  row = "//tr[td[2][normalize-space(.)='#{row_name}' and not(.//i[contains(@class, 'fa-plug')])]]"
  checkbox = find(:xpath, "#{row}/td[#{column}]//input[@type='checkbox']", visible: :all)
  checkbox.set(checked)
  state = find(:xpath, "#{row}/td[#{column}]//input[@type='checkbox']", visible: :all).checked?
  raise ScriptError, "The #{mode} checkbox of '#{row_name}' is #{state ? 'ticked' : 'unticked'}, expected the opposite" unless state == checked
end

# Removes a user from the table of the Users tab of the access group form.
#
# @param login [String] The login of the user.
def remove_user_from_access_group_form(login)
  find(:xpath, "//tr[td[normalize-space(.)='#{login}']]//button[.//i[contains(@class, 'fa-trash')]]").click
end

# Opens a tab of the access group form (Details, Namespaces & Permissions, Users, Review).
# The tab links are matched by their anchor, as their labels also exist in the left menu.
#
# @param title [String] The title of the tab.
def open_access_group_tab(title)
  find(:xpath, "//a[@href='##{title.sub(' ', '-').downcase}']").click
end

# Tells whether the permission tree shows a row, waiting for the tree to settle.
#
# @param row_name [String] The name of the row, for example 'config'.
# @param visible [Boolean] Whether the row must be shown or absent.
# @return [Boolean] Whether the tree is in the expected state.
def permission_row_shown?(row_name, visible)
  row = "//tr[td[2][normalize-space(.)='#{row_name}' and not(.//i[contains(@class, 'fa-plug')])]]"
  visible ? has_xpath?(row) : has_no_xpath?(row)
end

# Tells whether the permission tree shows rows of API namespaces (rows with a plug icon).
#
# @param visible [Boolean] Whether such rows must be shown or absent.
# @return [Boolean] Whether the tree is in the expected state.
def api_permission_rows_shown?(visible)
  rows = "//tr[td[2]//i[contains(@class, 'fa-plug')]]"
  visible ? has_xpath?(rows) : has_no_xpath?(rows)
end

# Runs an API call that must be refused, and checks the refusal. Both API transports are covered:
# - XML-RPC raises a SystemCallError carrying the message of the fault, and so does the HTTP API for a fault
#   it reports with status 200. The message must contain the expected text, because a transport error is a
#   SystemCallError too.
# - The HTTP API refuses a user without permission with HTTP 403, which its client raises as a ScriptError.
#   Pass http_status: 403 to accept that. Any other ScriptError is raised as it is.
#
# @param expected_message [String] A part of the message of the fault.
# @param http_status [Integer, nil] The HTTP status that counts as the refusal over the HTTP API, if any.
def expect_api_fault(expected_message, http_status: nil)
  yield
rescue SystemCallError => e
  raise ScriptError, "The API fault '#{e.message}' does not contain '#{expected_message}'" unless e.message.include?(expected_message)
rescue ScriptError => e
  raise unless http_status && e.message.include?("HTTP status code #{http_status}")
else
  raise ScriptError, "The API call succeeded, expected a fault containing '#{expected_message}'"
end
