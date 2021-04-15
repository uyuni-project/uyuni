# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Managing channels
  In Order to distribute software to the clients
  As an authorized user
  I want to manage channels

  Scenario: Fail when trying to add a duplicate channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test Base Channel" as "Channel Name"
    And I enter "test_base_channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Base channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "The channel name 'Test Base Channel' is already in use, please enter a different name" text

  Scenario: Fail when trying to use invalid characters in the channel label
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "test123" as "Channel Name"
    And I enter "tesT123" as "Channel Label"
    And I enter "test123" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Invalid channel label, please see the format described below" text

  Scenario: Fail when trying to use invalid characters in the channel name
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "!test123" as "Channel Name"
    And I enter "test123" as "Channel Label"
    And I enter "test123" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Invalid channel name, please see the format described below" text

  Scenario: Fail when trying to use reserved names for channels
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "SLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
    And I enter "test123" as "Channel Label"
    And I enter "test123" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "The channel name 'SLE-12-Cloud-Compute5-Pool for x86_64' is reserved, please enter a different name" text

  Scenario: Fail when trying to use reserved labels for channels
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "test123" as "Channel Name"
    And I enter "sle-we12-pool-x86_64-sap" as "Channel Label"
    And I enter "test123" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "The channel label 'sle-we12-pool-x86_64-sap' is reserved, please enter a different name" text

  Scenario: Create a channel that will be changed
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "aaaSLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
    And I enter "sle-we12aaa-pool-x86_64-sap" as "Channel Label"
    And I enter "test123" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel aaaSLE-12-Cloud-Compute5-Pool for x86_64 created." text

  Scenario: Fail when trying to change the channel name to a reserved name
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Software > Manage > Channels"
    And I follow "aaaSLE-12-Cloud-Compute5-Pool for x86_64"
    And I enter "SLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
    And I click on "Update Channel"
    Then I should see a "The channel name 'SLE-12-Cloud-Compute5-Pool for x86_64' is reserved, please enter a different name" text
