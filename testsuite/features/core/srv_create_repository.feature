# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Add a repository to a channel
  In order to distribute software to the clients
  As an authorized user
  I want to add a repository
  I want to add this repository to the base channel

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a test repository for x86_64
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "Test-Repository-x86_64" as "label"
    And I enter "http://localhost/pub/TestRepoRpmUpdates/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Disable metadata check for the x86_64 test repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Test-Repository-x86_64"
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
    And I select the "Test-Repository-x86_64" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-x86_64 repository information was successfully updated" text

  Scenario: Synchronize the repository in the x86_64 channel
    When I enable source package syncing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-x86_64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-x86_64." text
    And I wait until the channel "test-channel-x86_64" has been synced
    And I disable source package syncing

  Scenario: Add the repository to the x86_64 child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-x86_64 Child Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Test-Channel-x86_64 Child Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "Test-Repository-x86_64" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-x86_64 Child Channel repository information was successfully updated" text

  Scenario: Synchronize the repository in the x86_64 child channel
    When I enable source package syncing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-x86_64 Child Channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-x86_64 Child Channel." text

  Scenario: Add a test repository for i586
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "Test-Repository-i586" as "label"
    And I enter "file:///srv/www/htdocs/pub/TestRepoRpmUpdates/" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add the repository to the i586 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-i586"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Test-Channel-i586 updated" text
    When I follow "Repositories" in the content area
    And I select the "Test-Repository-i586" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-i586 repository information was successfully updated" text

  Scenario: Synchronize the repository in the i586 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-i586"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-i586." text

@deblike_minion
  Scenario: Add a test repository for Debian-like
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "Test-Repository-Deb" as "label"
    And I select "deb" from "contenttype"
    And I enter "http://localhost/pub/TestRepoDebUpdates/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

@deblike_minion
  Scenario: Add the Debian-like repository to the AMD64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-Deb-AMD64"
    And I follow "Repositories" in the content area
    And I select the "Test-Repository-Deb" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-Deb-AMD64 repository information was successfully updated" text

@deblike_minion
  Scenario: Synchronize the Debian-like repository in the AMD64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-Deb-AMD64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-Deb-AMD64." text

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
    And I follow "Test-Channel-Deb-AMD64"
    And I follow "Packages" in the content area
    And I wait until I see "blackhole-dummy" text, refreshing the page
