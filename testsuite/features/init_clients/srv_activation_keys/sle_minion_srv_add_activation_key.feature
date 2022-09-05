# Copyright (c) 2010-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
Feature: Create an activation key for sle_minion
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clone the child custom channel including test repositories
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select the custom architecture channel for "sle_minion" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I enter "Test-Channel-x86_64 Child Channel for sle_minion" as "Channel Name"
    And I enter "test-channel-for-sle_minion" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I click on "Clone Channel"
    Then I should see a "Test-Channel-x86_64 Child Channel for sle_minion" text


  Scenario: Create an activation key with the channel and child channels for a sle_minion
    When I am logged in API as user "admin" and password "admin"
    And I create an activation key including custom test channels for "sle_minion" via API
    And I logout from API
