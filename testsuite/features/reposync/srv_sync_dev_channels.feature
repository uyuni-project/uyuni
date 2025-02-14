# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize development channels
  In order to use the content provided inside the repositories of the dev channels
  As admin
  I want to synchronize the dev channels

@sle_minion
  Scenario: Synchronize Dev-SUSE-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Dev-SUSE-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Dev-SUSE-Channel." text
    And I wait until the channel "dev-suse-channel" has been synced

@deblike_minion
  Scenario: Synchronize Dev-Debian-like-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Dev-Debian-like-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Dev-Debian-like-Channel." text
    And I wait until the channel "dev-debian-like-channel" has been synced

@rhlike_minion
  Scenario: Synchronize Dev-RH-like-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Dev-RH-like-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Dev-RH-like-Channel." text
    And I wait until the channel "dev-rh-like-channel" has been synced

@build_host
@uyuni
  Scenario: Synchronize Dev-Build-Host-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Dev-Build-Host-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Dev-Build-Host-Channel." text
    And I wait until the channel "dev-build-host-channel" has been synced
