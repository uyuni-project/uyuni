# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@monitoring_server
Feature: Add a Maintenance Update custom channel and the custom repositories for monitoring_server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add the custom child channel for monitoring_server
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Custom Channel for monitoring_server" as "Channel Name"
    And I enter "custom_channel_monitoring_server" as "Channel Label"
    And I select the parent channel for the "sle15sp4_minion" from "Parent Channel"
    And I enter "Custom channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Custom Channel for monitoring_server created" text

  Scenario: Add the Maintenance update repositories for sle15sp4_minion
    When I create the MU repositories for "sle15sp4_minion"

  Scenario: Add the custom repositories to the custom channel for monitoring_server
    When I follow the left menu "Software > Manage > Channels"
    And I enter "monitoring_server" as the filtered channel name
    And I click on the filter button
    And I follow "Custom Channel for monitoring_server"
    And I follow "Repositories" in the content area
    And I select the MU repositories for "sle15sp4_minion" from the list
    And I click on "Save Repositories"
    Then I should see a "repository information was successfully updated" text

  Scenario: Synchronize the repositories in the custom channel for monitoring_server
    When I follow the left menu "Software > Manage > Channels"
    And I enter "monitoring_server" as the filtered channel name
    And I click on the filter button
    And I follow "Custom Channel for monitoring_server"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait until I do not see "Repository sync is running" text, refreshing the page
    And I wait until button "Sync Now" becomes enabled
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled" text or "No repositories are currently associated with this channel" text
