# Copyright (c) 2015-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If any fake channel or repository fails to be created:
# - features/reposync/srv_sync_fake_channels.feature

Feature: Create fake repositories for each fake child channel
  In order to distribute software to the clients
  As an authorized user
  I want to create a fake repository per fake child channel

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a fake repository for distributions using RPM
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-rpm-repo" as "label"
    And I enter "http://localhost/pub/TestRepoRpmUpdates/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Disable metadata check for the fake RPM repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "fake-rpm-repo"
    And I uncheck "metadataSigned"
    And I click on "Update Repository"
    Then I should see a "Repository updated successfully" text
    And I should see "metadataSigned" as unchecked

@rhlike_minion
  Scenario: Create a fake AppStream repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-appstream-repo" as "label"
    And I enter "http://localhost/pub/TestRepoAppStream/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

@rhlike_minion
  Scenario: Disable metadata check for the fake AppStream repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "fake-appstream-repo"
    And I uncheck "metadataSigned"
    And I click on "Update Repository"
    Then I should see a "Repository updated successfully" text
    And I should see "metadataSigned" as unchecked

@sle_minion
  Scenario: Add the fake RPM repository to the SUSE fake child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RPM-SUSE-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RPM-SUSE-Channel repository information was successfully updated" text

  Scenario: Add the fake RPM repository to the Test child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Child-Channel-x86_64"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Test-Child-Channel-x86_64 updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Child-Channel-x86_64 repository information was successfully updated" text

@rhlike_minion
  Scenario: Add the fake RPM repository to the RedHat-like base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel-RH-like"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-Base-Channel-RH-like updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-Base-Channel-RH-like repository information was successfully updated" text

@rhlike_minion
  Scenario: Add the fake AppStream repository to the AppStream base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel-AppStream"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-Base-Channel-AppStream updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-appstream-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-Base-Channel-AppStream repository information was successfully updated" text

  Scenario: Create a fake repository for i586
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-i586-repo" as "label"
    And I enter "file:///srv/www/htdocs/pub/TestRepoRpmUpdates/" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repository to the i586 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Child-Channel-i586"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-Child-Channel-i586 updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-i586-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-Child-Channel-i586 repository information was successfully updated" text

@sle_minion
  Scenario: Add the repository to the SUSE-like child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Child-Channel-SUSE-like"
    And I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-Child-Channel-SUSE-like repository information was successfully updated" text

@deblike_minion
  Scenario: Create a fake repository for Debian-like
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-debian-repo" as "label"
    And I select "deb" from "contenttype"
    And I enter "http://localhost/pub/TestRepoDebUpdates/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

@deblike_minion
  Scenario: Add the repository to the Debian-like base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Base-Channel-Debian-like"
    And I follow "Repositories" in the content area
    And I select the "fake-debian-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-Base-Channel-Debian-like repository information was successfully updated" text

@pxeboot_minion
@uyuni
@scc_credentials
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
