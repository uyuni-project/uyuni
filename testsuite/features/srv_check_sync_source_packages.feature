# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# requires core_srv_create_repository:
#   I enable source package syncing

Feature: Check if source packages were successfully synced

  Background:
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Channel List > All"

  Scenario: Check sources for noarch package
    When I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "virgo-dummy-2.0-1.1.noarch"
    Then I should see a "virgo-dummy-2.0-1.1.src.rpm" text

  Scenario: Check sources for noarch package
    When I follow "Test-Channel-x86_64"
    And I follow "Packages"
    And I follow "blackhole-dummy-1.0-1.1.x86_64"
    Then I should see a "blackhole-dummy-1.0-1.1.src.rpm" text
