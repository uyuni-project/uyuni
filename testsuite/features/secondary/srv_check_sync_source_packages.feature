# Copyright (c) 2018-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# requires
# - reposync/srv_create_fake_channels.feature
# - reposync/srv_create_fake_repositories.feature
# - reposync/srv_sync_fake_channels.feature

@scope_configuration_channels
Feature: Check if source packages were successfully synced

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Check sources for noarch package
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Packages"
    # the package may be in the 2nd page of results
    And I enter "virgo-dummy" as the filtered package name
    And I click on the filter button
    And I follow "virgo-dummy-2.0-1.2.noarch"
    Then I should see a "virgo-dummy-2.0-1.2.src.rpm" text

  Scenario: Check sources for x86_64 package
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Packages"
    And I follow "blackhole-dummy-1.0-1.2.x86_64"
    Then I should see a "blackhole-dummy-1.0-1.2.src.rpm" text
