# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# requires core_srv_create_repository:
#   I enable source package syncing

@scope_configuration_channels
Feature: Check if source packages were successfully synced

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check sources for noarch package
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "virgo-dummy-2.0-1.1.noarch"
    Then I should see a "virgo-dummy-2.0-1.1.src.rpm" text

  Scenario: Check sources for x86_64 package
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "blackhole-dummy-1.0-1.1.x86_64"
    Then I should see a "blackhole-dummy-1.0-1.1.src.rpm" text
