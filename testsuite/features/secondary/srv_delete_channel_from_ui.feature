# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT License.
#
# This feature can cause failures in the following features:
# - features/secondary/srv_delete_channel_with_tool.feature:
# - features/secondary/srv_handle_software_channels_with_ISS_v2.feature:
# - features/secondary/srv_clone_channel_npn.feature
# If the deletion of "Clone of Fake-RPM-SUSE-Channel" fails, these features will have failing scenarios.
# - features/secondary/srv_dist_channel_mapping.feature
# - features/secondary/srv_patches_page.feature
# If the deletion of "Clone of Fake-Base-Channel-SUSE-like" fails, these features will have failing scenarios.

@scope_configuration_channels
Feature: Delete channels with child or clone is not allowed
  Using the UI, we cannot delete a channel if it has a child
  or a clone created from it

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Clone the first channel before deletion from UI test
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-Base-Channel-SUSE-like" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Fake-Base-Channel-SUSE-like" text

  Scenario: Clone the second channel using first channel as base
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Clone of Fake-Base-Channel-SUSE-like" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I click on "Clone Channel"
    Then I should see a "Clone of Clone of Fake-Base-Channel-SUSE-like" text

  Scenario: Try to delete channel with clone
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-Base-Channel-SUSE-like"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-Base-Channel-SUSE-like" text
    And I should see a "Unable to delete channel" text

  Scenario: Delete channel without clones neither children
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Clone of Fake-Base-Channel-SUSE-like"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Clone of Fake-Base-Channel-SUSE-like" text
    And I should see a "has been deleted" text

  Scenario: Clone a child channel to the clone of x86_64 test channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I click on "Clone Channel"
    Then I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    When I select "Clone of Fake-Base-Channel-SUSE-like" from "Parent Channel"
    And I click on "Clone Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Try delete channel with child
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-Base-Channel-SUSE-like"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-Base-Channel-SUSE-like" text
    And I should see a "channel has child channels associated" text
    And I should see a "must delete those channels first before deleting the parent." text

  Scenario: Cleanup: remove cloned child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text
    And I should see a "has been deleted." text

  Scenario: Cleanup: remove cloned parent channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-Base-Channel-SUSE-like"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-Base-Channel-SUSE-like" text
    And I should see a "has been deleted." text
