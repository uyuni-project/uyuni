# Copyright (c) 2015-2019 SUSE LLC
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
    And I follow "Repositories" in the content area
    And I select the "Test-Repository-x86_64" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-x86_64 repository information was successfully updated" text

  Scenario: Synchronize the repository in the x86_64 channel
    When I enable source package syncing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-x86_64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-x86_64." text

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
    And I follow "Repositories" in the content area
    And I select the "Test-Repository-i586" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-i586 repository information was successfully updated" text

  Scenario: Synchronize the repository in the i586 channel
    When I disable source package syncing
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-i586"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    And I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-i586." text

@ubuntu_minion
  Scenario: Add a test repository for Ubuntu
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "Test-Repository-Deb" as "label"
    And I select "deb" from "contenttype"
    And I enter "http://localhost/pub/TestRepoDebUpdates/" as "url"
    # WORKAROUND
    # GPG verification of Debian-like repos was added and the TestRepoDebUpdates repo
    # is signed by a GPG key that is not in the keyring. This workaround temporarily
    # disables GPG check, before this is properly handled at sumaform/terraform level.
    And I uncheck "metadataSigned"
    # End of WORKAROUND
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

@ubuntu_minion
  Scenario: Add the Ubuntu repository to the AMD64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-Deb-AMD64"
    And I follow "Repositories" in the content area
    And I select the "Test-Repository-Deb" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Channel-Deb-AMD64 repository information was successfully updated" text

@ubuntu_minion
  Scenario: Synchronize the Ubuntu repository in the AMD64 channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Channel-Deb-AMD64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
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
    Then I should see a "blackhole-dummy" text

@ubuntu_minion
  Scenario: Reposync handles wrong encoding on DEB attributes
    When I follow the left menu "Software > Channel List"
    And I follow "Test-Channel-Deb-AMD64"
    And I follow "Packages" in the content area
    Then I should see a "blackhole-dummy" text
