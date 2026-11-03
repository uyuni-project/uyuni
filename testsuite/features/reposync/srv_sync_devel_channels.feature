# Copyright (c) 2025 SUSE LLC
# SPDX-License-Identifier: MIT

@skip_if_github_validation
Feature: Synchronize development channels
  In order to use the content provided inside the repositories of the dev channels
  As admin
  I want to synchronize the dev channels

@sle_minion
  Scenario: Synchronize Devel-SUSE-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Devel-SUSE-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Devel-SUSE-Channel." text
    And I wait until the channel "devel-suse-channel" has been synced

@deblike_minion
  Scenario: Synchronize Devel-Debian-like-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Devel-Debian-like-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Devel-Debian-like-Channel." text
    And I wait until the channel "devel-debian-like-channel" has been synced

@rhlike_minion
  Scenario: Synchronize Devel-RH-like-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Devel-RH-like-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Devel-RH-like-Channel." text
    And I wait until the channel "devel-rh-like-channel" has been synced

@build_host
@uyuni
  Scenario: Synchronize Devel-Build-Host-Channel channel
    Given I am authorized for the "Admin" section
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Devel-Build-Host-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Devel-Build-Host-Channel." text
    And I wait until the channel "devel-build-host-channel" has been synced
