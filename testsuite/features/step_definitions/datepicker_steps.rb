# Copyright (c) 2017-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning the selection,
### handling or finding of dates and times including timestamps.

require 'date'

# Based on https://github.com/akarzim/capybara-bootstrap-datepicker
# (MIT license)

Given(/^I pick "([^"]*)" as date$/) do |desired_date|
  value = Date.parse(desired_date)
  date_input = find('input[data-testid="date-picker"]')
  date_input.click
  # TODO: Switch this over to .clear once we update Selenium
  date_input.send_keys [:control, 'a'], :backspace, value.strftime('%Y-%m-%d'), :enter
end

Then(/^the date field should be set to "([^"]*)"$/) do |expected_date|
  value = Date.parse(expected_date)
  # the fields that give backwards compatibility
  day_compat = find('input#date_day', visible: false)
  month_compat = find('input#date_month', visible: false)
  year_compat = find('input#date_year', visible: false)

  raise ScriptError if day_compat.value.to_i != value.day
  # month field is 0-11, ruby 1-12
  raise ScriptError if month_compat.value.to_i + 1 != value.month
  raise ScriptError if year_compat.value.to_i != value.year
end

Given(/^I open the date picker$/) do
  find('input[data-testid="date-picker"]').click
end

Then(/^the date picker should be closed$/) do
  raise ScriptError, 'The date picker is not closed' unless has_no_css?('.date-time-picker-popup')
end

Then(/^the date picker title should be the current month and year$/) do
  now = DateTime.now.strftime('%B %Y')
  step %(the date picker title should be "#{now}")
end

Then(/^the date picker title should be "([^"]*)"$/) do |arg1|
  step 'I open the date picker' if has_no_css?('.date-time-picker-popup')
  switch = find('.date-time-picker-popup .react-datepicker__current-month')
  raise ScriptError, 'The date picker title has a different value or it cant be found' unless switch.has_content?(arg1)
end

Given(/^I pick "([^"]*)" as time$/) do |desired_time|
  find('input[data-testid="time-picker"]').click
  timepicker = find('ul.react-datepicker__time-list', match: :first)
  time = timepicker.find(:xpath, "//*[normalize-space(text())='#{desired_time}']")
  time.click
end

When(/^I pick "([^"]*)" as time from "([^"]*)"$/) do |desired_time, element_id|
  find('input[data-testid="time-picker"]', id: element_id).click
  timepicker = find('ul.react-datepicker__time-list', match: :first)
  time = timepicker.find(:xpath, "//*[normalize-space(text())='#{desired_time}']")
  time.click
end

When(/^I pick (\d+) minutes from now as schedule time$/) do |arg1|
  action_time = get_future_time(arg1)
  raise ScriptError unless find(:xpath, '//*[@id=\'date_timepicker_widget_input\']', wait: 2)

  step %(I enter "#{action_time}" as "date_timepicker_widget_input")
end

When(/^I schedule action to (\d+) minutes from now$/) do |minutes|
  action_datetime = (DateTime.now + (Rational(1, 1440) * minutes.to_i) + Rational(59, 86_400)).strftime('%Y-%m-%dT%H:%M')
  action_date, action_time = action_datetime.split('T')

  date_input = find('input[data-testid="date-picker"]')
  date_input.click
  # TODO: Switch this over to .clear once we update Selenium
  date_input.send_keys [:control, 'a'], :backspace, action_date, :enter

  time_input = find('input[data-testid="time-picker"]')
  time_input.click
  # TODO: Switch this over to .clear once we update Selenium
  time_input.send_keys [:control, 'a'], :backspace, action_time, :enter
end

Then(/^the time field should be set to "([^"]*)"$/) do |expected_time|
  h, m = expected_time.chomp.split(':').map(&:to_i)
  # the fields that give backwards compatibility
  h_compat = find('input#date_hour', visible: false)
  m_compat = find('input#date_minute', visible: false)
  ampm_compat = find('input#date_am_pm', visible: false)

  raise StandardError, 'invalid hidden hour' if h_compat.value.to_i != h % 12
  raise StandardError, 'invalid hidden minute' if m_compat.value.to_i != m
  raise StandardError, 'invalid hidden AM/PM' if ampm_compat.value.to_i != (h >= 12 ? 1 : 0)
end
