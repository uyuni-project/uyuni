# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Prepare fake SUSE channels
  In order to have patches and packages to install on clients
  As admin
  I want to prepare the channels containing those patches and packages

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add the fake packages child channel to the base product channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-RPM-SUSE-Channel" as "Channel Name"
    And I enter "fake-rpm-suse-channel" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-RPM-SUSE-Channel for testing" as "Channel Summary"
    And I enter "Description for Fake-RPM-SUSE-Channel Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-RPM-SUSE-Channel created." text

  Scenario: Add the repository to the fake packages child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RPM-SUSE-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RPM-SUSE-Channel repository information was successfully updated" text

  Scenario: Synchronize the repository in the fake packages channel
    When I enable source package syncing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-RPM-SUSE-Channel." text
    And I wait until the channel "fake-rpm-suse-channel" has been synced
    And I disable source package syncing

  Scenario: Verify state of Fake-RPM-SUSE-Channel custom channel
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "fake-rpm-suse-channel" should reference "orion-dummy-1.1-1.1.x86_64.rpm"

@uyuni
  Scenario: Add the terminal child channel to the base product channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-RPM-Terminal-Channel" as "Channel Name"
    And I enter "fake-rpm-terminal-channel" as "Channel Label"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-RPM-Terminal-Channel for testing" as "Channel Summary"
    And I enter "Description for Fake-RPM-Terminal-Channel Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-RPM-Terminal-Channel created." text

@uyuni
  Scenario: Add the repository to the terminal child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-Terminal-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RPM-Terminal-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RPM-Terminal-Channel repository information was successfully updated" text

@uyuni
  Scenario: Synchronize the repository in the terminal channel
    When I enable source package syncing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-Terminal-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-RPM-Terminal-Channel." text
    And I wait until the channel "fake-rpm-terminal-channel" has been synced
    And I disable source package syncing

@uyuni
  Scenario: Verify state of Fake-RPM-Terminal-Channel custom channel
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "fake-rpm-terminal-channel" should reference "orion-dummy-1.1-1.1.x86_64.rpm"
