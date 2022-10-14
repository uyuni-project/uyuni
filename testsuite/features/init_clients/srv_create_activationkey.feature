# Copyright (c) 2010-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel to the base product channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-RPM-SLES15SP4-Channel" as "Channel Name"
    And I enter "fake-rpm-sles15sp4-channel" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-RPM-SLES15SP4-Channel for testing" as "Channel Summary"
    And I enter "Description for Fake-RPM-SLES15SP4-Channel Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-RPM-SLES15SP4-Channel Child Channel created." text

  Scenario: Add the repository to the x86_64 child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SLES15SP4-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RPM-SLES15SP4-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RPM-SLES15SP4-Channel repository information was successfully updated" text

  Scenario: Synchronize the repository in the x86_64 channel
    When I enable source package syncing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SLES15SP4-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-RPM-SLES15SP4-Channel." text
    And I wait until the channel "fake-rpm-sles15sp4-channel" has been synced
    And I disable source package syncing

  Scenario: Create an activation key with a channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Test Key x86_64" as "description"
    And I enter "SUSE-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SLES15SP4-Channel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

@rhlike_minion
  Scenario: Create an activation key for RedHat-like minion
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "RedHat like Test Key" as "description"
    And I enter "SUSE-KEY-RH-LIKE" as "key"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key RedHat like Test Key has been created" text

@deblike_minion
  Scenario: Create an activation key for Debian-like minion
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "Debian-like Test Key" as "description"
    And I enter "DEBLIKE-KEY" as "key"
    And I select "Test-Channel-Deb-AMD64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Debian-like Test Key has been created" text

  Scenario: Create an activation key with a channel for salt-ssh
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE SSH Test Key x86_64" as "description"
    And I enter "SUSE-SSH-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SLES15SP4-Channel"
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been created" text

  Scenario: Create an activation key with a channel for salt-ssh via tunnel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE SSH Tunnel Test Key x86_64" as "description"
    And I enter "SUSE-SSH-TUNNEL-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SLES15SP4-Channel"
    And I select "Push via SSH tunnel" from "contact-method"
    And I click on "Create Activation Key"
