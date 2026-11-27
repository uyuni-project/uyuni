# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT

# Steps for the access group form (Admin > Access Control). The access.* API steps are in api_common.rb.

When(/^I (check|uncheck) the "(View|Modify)" permission of the namespace "([^"]*)"$/) do |action, mode, namespace|
  set_permission_checkbox(namespace, mode, action == 'check')
end

When(/^I remove the user "([^"]*)" from the access group$/) do |login|
  remove_user_from_access_group_form(login)
end

When(/^I open the "([^"]*)" tab of the access group$/) do |title|
  open_access_group_tab(title)
end

Then(/^I should( not)? see the namespace row "([^"]*)" in the permission tree$/) do |negation, row_name|
  raise ScriptError, "The row '#{row_name}' is #{negation ? 'shown' : 'not shown'} in the permission tree" unless permission_row_shown?(row_name, negation.nil?)
end

Then(/^I should( not)? see API namespace rows in the permission tree$/) do |negation|
  raise ScriptError, "API namespace rows are #{negation ? 'shown' : 'not shown'} in the permission tree" unless api_permission_rows_shown?(negation.nil?)
end
