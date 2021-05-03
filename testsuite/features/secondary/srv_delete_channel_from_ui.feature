# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT License.

@scope_configuration_channels
Feature: Delete channels with child or clone is not allowed
  Using the UI, we cannot delete a channel if it has a child
  or a clone created from it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clone the first channel before deletion from UI test
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Test-Channel-x86_64" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Test-Channel-x86_64" text

  Scenario: Clone the second channel using first channel as base
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Clone of Test-Channel-x86_64" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Clone of Test-Channel-x86_64" text

  Scenario: Try to delete channel with clone
    Given I am on the manage software channels page
    When I follow "Clone of Test-Channel-x86_64"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Test-Channel-x86_64" text
    And I should see a "Unable to delete channel" text

  Scenario: Delete channel without clones neither children
    Given I am on the manage software channels page
    When I follow "Clone of Clone of Test-Channel-x86_64"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Clone of Test-Channel-x86_64" text
    And I should see a "has been deleted" text

  Scenario: Clone a child channel to the clone of x86_64 test channel
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Test-Channel-x86_64 Child Channel" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I select "Clone of Test-Channel-x86_64" from "Parent Channel"
    And I click on "Clone Channel"
    Then I should see a "Clone of Test-Channel-x86_64 Child Channel" text

  Scenario: Try delete channel with child
    Given I am on the manage software channels page
    When I follow "Clone of Test-Channel-x86_64"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Test-Channel-x86_64" text
    And I should see a "channel has child channels associated" text
    And I should see a "must delete those channels first before deleting the parent." text

  Scenario: Cleanup: remove cloned child channel
    Given I am on the manage software channels page
    When I follow "Clone of Test-Channel-x86_64 Child Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Test-Channel-x86_64 Child Channel" text
    And I should see a "has been deleted." text

  Scenario: Cleanup: remove cloned parent channel
    Given I am on the manage software channels page
    When I follow "Clone of Test-Channel-x86_64"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Test-Channel-x86_64" text
    And I should see a "has been deleted." text
