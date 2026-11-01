# Copyright (c) 2024 SUSE LLC.
# SPDX-License-Identifier: MIT

### This file contains the definitions for the steps extracting and reporting information from the system

When(/^I report the bootstrap duration for "([^"]*)"$/) do |host|
  next unless $quality_intelligence_mode

  duration = last_bootstrap_duration(host)
  $quality_intelligence.push_bootstrap_duration(host, duration)
end

When(/^I report the onboarding duration for "([^"]*)"$/) do |host|
  next unless $quality_intelligence_mode

  duration = last_onboarding_duration(host)
  $quality_intelligence.push_onboarding_duration(host, duration)
end

When(/^I report the synchronization duration for "([^"]*)"$/) do |product|
  next unless $quality_intelligence_mode

  duration = product_synchronization_duration(product)
  $quality_intelligence.push_synchronization_duration(product, duration)
end
