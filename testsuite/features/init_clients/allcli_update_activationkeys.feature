# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Update activation keys
  In order to register systems to the spacewalk server
  As admin
  I want to update activation keys to use synchronized base products

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a child channel to the base product channel
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

  Scenario: Add the repository to the x86_64 child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RPM-SUSE-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RPM-SUSE-Channel repository information was successfully updated" text

  Scenario: Synchronize the repository in the x86_64 channel
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

@skip_if_github_validation
@scc_credentials
@susemanager
  Scenario: Update SLE key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I wait until "SLE-Module-Basesystem15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Basesystem15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-Containers15-SP4-Updates for x86_64" has been checked
    And I check "Fake-RPM-SUSE-Channel"
    When I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

@skip_if_github_validation
@uyuni
  Scenario: Update openSUSE Leap key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "openSUSE Leap 15.4 (x86_64)" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.4 non oss (x86_64)"
    And I check "openSUSE Leap 15.4 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.4 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.4 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.4 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.4 (x86_64)"
    And I check "Fake-RPM-SUSE-Channel"
    When I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

@skip_if_github_validation
@scc_credentials
@susemanager
  Scenario: Update SSH key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been modified" text

@skip_if_github_validation
@uyuni
  Scenario: Update SSH key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "openSUSE Leap 15.4 (x86_64)" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.4 non oss (x86_64)"
    And I check "openSUSE Leap 15.4 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.4 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.4 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.4 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.4 (x86_64)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been modified" text

@skip_if_github_validation
@scc_credentials
@susemanager
  Scenario: Update SSH tunnel key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Tunnel Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "SLE-Product-SLES15-SP4-Pool for x86_64" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Tunnel Test Key x86_64 has been modified" text

@skip_if_github_validation
@uyuni
  Scenario: Update SSH tunnel key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Tunnel Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "openSUSE Leap 15.4 (x86_64)" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.4 non oss (x86_64)"
    And I check "openSUSE Leap 15.4 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.4 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.4 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.4 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.4 (x86_64)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Tunnel Test Key x86_64 has been modified" text

@skip_if_github_validation
@scc_credentials
@susemanager
  Scenario: Update the Proxy key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "SLE-Product-SUSE-Manager-Proxy-4.3-Pool" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I wait until "SLE-Module-Basesystem15-SP4-Pool for x86_64 Proxy 4.3" has been checked
    And I wait until "SLE-Module-Basesystem15-SP4-Updates for x86_64 Proxy 4.3" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Pool for x86_64 Proxy 4.3" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Updates for x86_64 Proxy 4.3" has been checked
    And I wait until "SLE-Module-SUSE-Manager-Proxy-4.3-Pool for x86_64" has been checked
    And I wait until "SLE-Module-SUSE-Manager-Proxy-4.3-Updates for x86_64" has been checked
    When I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@skip_if_github_validation
@uyuni
  Scenario: Update the Proxy key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select "openSUSE Leap 15.4 (x86_64)" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.4 non oss (x86_64)"
    And I check "openSUSE Leap 15.4 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.4 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.4 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.4 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.4 (x86_64)"
    And I check "Uyuni Proxy Devel for openSUSE Leap 15.4 (x86_64)"
    When I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text
