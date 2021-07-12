# Copyright (c) 2017-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'date'

Given(/^I pick "([^"]*)" as date$/) do |desired_date|
  value = Date.parse(desired_date)
  date_input = find('input[data-testid="date-picker"]')
  date_input.click
  picker = find(:xpath, '//body').find('.datepicker')
  picker_years = picker.find('.datepicker-years', visible: false)
  picker_months = picker.find('.datepicker-months', visible: false)
  picker_days = picker.find('.datepicker-days', visible: false)
  picker_current_decade = picker_years.find('th.datepicker-switch', visible: false)
  picker_current_year = picker_months.find('th.datepicker-switch', visible: false)
  picker_current_month = picker_days.find('th.datepicker-switch', visible: false)
  picker_current_month.click if picker_days.visible?
  picker_current_year.click if picker_months.visible?
  decade_start, decade_end = picker_current_decade.text.split('-').map(&:to_i)
  if value.year < decade_start
    gap = decade_start / 10 - value.year / 10
    gap.times { picker_years.find('th.prev').click }
  elsif value.year > decade_end
    gap = value.year / 10 - decade_end / 10
    gap.times { picker_years.find('th.next').click }
  end
  picker_years.find('.year', text: value.year).click
  picker_months.find('.month', text: value.strftime('%b')).click
  CommonLib.days_find(picker_days, value.day)
end

Then(/^the date field is set to "([^"]*)"$/) do |arg1|
  value = Date.parse(arg1)
  # the fields that give backwards compatibility
  day_compat = find('input#date_day', visible: false)
  month_compat = find('input#date_month', visible: false)
  year_compat = find('input#date_year', visible: false)

  raise(StandardError, 'Wrong day') if day_compat.value.to_i != value.day
  # month field is 0-11, ruby 1-12
  raise(StandardError, 'Wrong month') if month_compat.value.to_i + 1 != value.month
  raise(StandardError, 'Wrong year') if year_compat.value.to_i != value.year
end

Given(/^I open the date picker$/) do
  find('input[data-testid="date-picker"]').click
end

Then(/^the date picker is closed$/) do
  raise unless has_no_css?('.datepicker')
end

Then(/^the date picker title should be the current month and year$/) do
  now = Time.now.strftime('%B %Y')
  step %(the date picker title should be "#{now}")
end

Then(/^the date picker title should be "([^"]*)"$/) do |arg1|
  step %(I open the date picker) if has_no_css?('.datepicker')
  switch = find('.datepicker .datepicker-days th.datepicker-switch')
  raise unless switch.has_content?(arg1)
end

Given(/^I pick "([^"]*)" as time$/) do |arg1|
  find('.ui-timepicker-input').click
  timepicker = find('ul.ui-timepicker-list', match: :first)
  time = timepicker.find(:xpath, "//*[normalize-space(text())='#{arg1}']")
  time.click
end

When(/^I pick "([^"]*)" as time from "([^"]*)"$/) do |arg1, arg2|
  find('.ui-timepicker-input', id: arg2).click
  timepicker = find('ul.ui-timepicker-list', match: :first)
  time = timepicker.find(:xpath, "//*[normalize-space(text())='#{arg1}']")
  time.click
end

When(/^I pick (\d+) minutes from now as schedule time$/) do |arg1|
  action_time = CommonLib.get_future_time(arg1)
  raise unless find(:xpath, "//*[@id='date_timepicker_widget_input']", wait: 2)

  execute_script("$('#date_timepicker_widget_input') .timepicker('setTime', '#{action_time}').trigger('changeTime');")
end

When(/^I schedule action to (\d+) minutes from now$/) do |minutes|
  action_time = (Time.now + Rational(
    1,
    1440
  ) * minutes.to_i + Rational(59, 86_400)).strftime('%Y-%m-%dT%H:%M%:z')
  execute_script("window.schedulePage.setScheduleTime('#{action_time}')")
end

Then(/^the time field is set to "([^"]*)"$/) do |arg1|
  h, m = arg1.chomp.split(':').map(&:to_i)
  # the fields that give backwards compatibility
  h_compat = find('input#date_hour', visible: false)
  m_compat = find('input#date_minute', visible: false)
  ampm_compat = find('input#date_am_pm', visible: false)

  raise(StandardError, 'Wrong hour') if h_compat.value.to_i != h % 12
  raise(StandardError, 'Wrong minute') if m_compat.value.to_i != m
  raise(StandardError, 'Wrong date') if ampm_compat.value.to_i != (h >= 12 ? 1 : 0)
end
