# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT License.

@scope_configuration_channels
Feature: Deleting channels with children or clones is not allowed
  Using the tool spacewalk-remove-channel, we cannot delete a channel if it has a child
  or a clone created from it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clone the first channel before deletion from tool test
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SLES-Channel" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Fake-RPM-SLES-Channel" text

  Scenario: Clone a second channel using first channel as base
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Clone of Fake-RPM-SLES-Channel" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Clone of Fake-RPM-SLES-Channel" text

  Scenario: Verify if both clone channels exists
    When I list channels with spacewalk-remove-channel
    Then I should get "clone-fake-rpm-sles-channel"
    And I should get "clone-clone-fake-rpm-sles-channel"

  Scenario: Delete channel with one clone
    When I delete these channels with spacewalk-remove-channel:
      |clone-fake-rpm-sles-channel|
    Then I should get "Error: cannot remove channel"
    And  I should get "clone channel(s) exist"
    And  I should get "clone-fake-rpm-sles-channel"
    And  I should get "clone-clone-fake-rpm-sles-channel"

  Scenario: Delete base channel and clone
    When I delete these channels with spacewalk-remove-channel:
      |clone-fake-rpm-sles-channel|
      |clone-clone-fake-rpm-sles-channel|
    And I list channels with spacewalk-remove-channel
    Then I shouldn't get "clone-fake-rpm-sles-channel"
    And I shouldn't get "clone-clone-fake-rpm-sles-channel"
