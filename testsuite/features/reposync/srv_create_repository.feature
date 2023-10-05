# Copyright (c) 2015-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If the fake-rpm-repo fails to be created:
# - features/reposync/srv_sync_fake_channels.feature
# If Fake-Deb-AMD64-Channel fails to be updated with the repository:
# - features/secondary/min_deblike_salt_install_package.feature
# - features/secondary/min_deblike_salt_install_with_staging.feature
# If Fake-RH-Like-Channel fails to be updated with the repository:
# - features/secondary/min_rhlike_salt_install_package_and_patch.feature
# - features/secondary/srv_maintenance_windows.feature

Feature: Add a repository to a channel
  In order to distribute software to the clients
  As an authorized user
  I want to add a repository
  I want to add this repository to the base channel

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
    And I enable source package syncing

  Scenario: Add a test repository for x86_64
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-rpm-repo" as "label"
    And I enter "http://localhost/pub/TestRepoRpmUpdates/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Disable metadata check for the x86_64 test repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "fake-rpm-repo"
    And I uncheck "metadataSigned"
    And I click on "Update Repository"
    Then I should see a "Repository updated successfully" text
    And I should see "metadataSigned" as unchecked

  Scenario: Add the repository to the x86_64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-x86_64"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Test-Channel-x86_64 updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-x86_64 repository information was successfully updated" text

  Scenario: Synchronize the repository in the x86_64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-x86_64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-x86_64." text
    And I wait until the channel "test-channel-x86_64" has been synced

  Scenario: Add a test repository for i586
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-i586-repo" as "label"
    And I enter "file:///srv/www/htdocs/pub/TestRepoRpmUpdates/" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repository to the i586 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-i586-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-i586-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-i586-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-i586-Channel repository information was successfully updated" text

  Scenario: Synchronize the repository in the i586 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-i586-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-i586-Channel." text

@deblike_minion
  Scenario: Add a test repository for Debian-like
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-debian-repo" as "label"
    And I select "deb" from "contenttype"
    And I enter "http://localhost/pub/TestRepoDebUpdates/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

@deblike_minion
  Scenario: Add the Debian-like repository to the AMD64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Deb-AMD64-Channel"
    And I follow "Repositories" in the content area
    And I select the "fake-debian-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-Deb-AMD64-Channel repository information was successfully updated" text

@deblike_minion
  Scenario: Synchronize the Debian-like repository in the AMD64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Deb-AMD64-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-Deb-AMD64-Channel." text

@rhlike_minion
  Scenario: Add the repository to the RedHat-like channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RH-Like-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RH-Like-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RH-Like-Channel repository information was successfully updated" text

@rhlike_minion
  Scenario: Synchronize the repository in the x86_64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RH-Like-Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Fake-RH-Like-Channel." text
    And I wait until the channel "fake-rh-like-channel" has been synced

  Scenario: Refresh the errata cache
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Refresh the channel's repository data
    When I follow the left menu "Admin > Task Schedules"
    And I follow "channel-repodata-default"
    And I follow "channel-repodata-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Reposync handles wrong encoding on RPM attributes
    When I follow the left menu "Software > Channel List"
    And I follow "Test-Channel-x86_64"
    And I follow "Packages" in the content area
    And I wait until I see "blackhole-dummy" text, refreshing the page

@deblike_minion
  Scenario: Reposync handles wrong encoding on DEB attributes
    When I follow the left menu "Software > Channel List"
    And I follow "Fake-Deb-AMD64-Channel"
    And I follow "Packages" in the content area
    And I wait until I see "blackhole-dummy" text, refreshing the page

  Scenario: Cleanup disable source package syncing
    Then I disable source package syncing
