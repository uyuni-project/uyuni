# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT License.

@scope_configuration_channels
Feature: Deleting channels with children or clones is not allowed
  Using the tool spacewalk-remove-channel, we cannot delete a channel if it has a child
  or a clone created from it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clone the first channel before deletion from tool test
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Test-Channel-x86_64" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Test-Channel-x86_64" text

  Scenario: Clone a second channel using first channel as base
    Given I am on the manage software channels page
    When I follow "Clone Channel"
    And I select "Clone of Test-Channel-x86_64" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Clone of Test-Channel-x86_64" text

  Scenario: Verify if both clone channels exists
    When I list channels with spacewalk-remove-channel
    Then I should get "clone-test-channel-x86_64"
    And I should get "clone-clone-test-channel-x86_64"

  Scenario: Delete channel with one clone
    When I delete these channels with spacewalk-remove-channel:
      |clone-test-channel-x86_64|
    Then I should get "Error: cannot remove channel"
    And  I should get "clone channel(s) exist"
    And  I should get "clone-test-channel-x86_64"
    And  I should get "clone-clone-test-channel-x86_64"

  Scenario: Delete base channel and clone
    When I delete these channels with spacewalk-remove-channel:
      |clone-test-channel-x86_64|
      |clone-clone-test-channel-x86_64|
    And I list channels with spacewalk-remove-channel
    Then I shouldn't get "clone-test-channel-x86_64"
    And I shouldn't get "clone-clone-test-channel-x86_64"
