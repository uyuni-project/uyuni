# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If Fake-Base-Channel-Debian-like fails to be updated with the repository:
# - features/secondary/min_deblike_salt_install_package.feature
# - features/secondary/min_deblike_salt_install_with_staging.feature
# If Fake-Base-Channel-RH-like fails to be updated with the repository:
# - features/secondary/min_rhlike_salt_install_package_and_patch.feature
# - features/secondary/srv_maintenance_windows.feature

Feature: Synchronize fake channels
  In order to use the content provided inside the repositories of the fake channels
  As admin
  I want to synchronize the fake channels

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
    And I enable source package syncing

  Scenario: Synchronize Fake-Base-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-Base-Channel." text
    And I wait until the channel "fake-base-channel" has been synced

  Scenario: Synchronize Fake-RPM-SUSE-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-RPM-SUSE-Channel." text
    And I wait until the channel "fake-rpm-suse-channel" has been synced

  Scenario: Verify state of Fake-RPM-SUSE-Channel channel
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "fake-rpm-suse-channel" should reference "orion-dummy-1.1-1.1.x86_64.rpm"

  Scenario: Synchronize Test-Base-Channel-x86_64 channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Base-Channel-x86_64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Base-Channel-x86_64." text
    And I wait until the channel "test-base-channel-x86_64" has been synced

  Scenario: Synchronize Fake-Base-Channel-i586 channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel-i586"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-Base-Channel-i586." text
    And I wait until the channel "fake-base-channel-i586" has been synced

@deblike_minion
  Scenario: Synchronize Fake-Base-Channel-Debian-like channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel-Debian-like"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-Base-Channel-Debian-like." text
    And I wait until the channel "fake-base-channel-debian-like" has been synced

@rhlike_minion
  Scenario: Synchronize Fake-Base-Channel-RH-like channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel-RH-like"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-Base-Channel-RH-like." text
    And I wait until the channel "fake-base-channel-rh-like" has been synced

@uyuni
@scc_credentials
  Scenario: Synchronize the repository in the terminal channel
    Given I am authorized for the "Admin" section
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-Terminal-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-RPM-Terminal-Channel." text
    And I wait until the channel "fake-rpm-terminal-channel" has been synced


@uyuni
@scc_credentials
  Scenario: Verify state of Fake-RPM-Terminal-Channel custom channel
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "fake-rpm-terminal-channel" should reference "orion-dummy-1.1-1.1.x86_64.rpm"

  Scenario: Refresh the errata cache
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Refresh the channel's repository data
    When I follow the left menu "Admin > Task Schedules"
    And I follow "channel-repodata-default"
    And I follow "channel-repodata-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Reposync handles wrong encoding on RPM attributes
    When I follow the left menu "Software > Channel List"
    And I follow "Test-Base-Channel-x86_64"
    And I follow "Packages" in the content area
    And I wait until I see "blackhole-dummy" text, refreshing the page

@deblike_minion
  Scenario: Reposync handles wrong encoding on DEB attributes
    When I follow the left menu "Software > Channel List"
    And I follow "Fake-Base-Channel-Debian-like"
    And I follow "Packages" in the content area
    And I wait until I see "blackhole-dummy" text, refreshing the page

  Scenario: Cleanup disable source package syncing
    Then I disable source package syncing
