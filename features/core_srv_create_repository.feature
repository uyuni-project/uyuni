# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Adding repository to a channel
  In Order distribute software to the clients
  As an authorized user
  I want to add a repository
  And I want to add this repository to the base channel

  Scenario: Adding Test-Repository-x86_64 repository
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Manage Repositories" in the left menu
    And I follow "Create Repository"
    When I enter "Test-Repository-x86_64" as "label"
    And I enter "http://localhost/pub/TestRepo/" as "url"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text
    And I should see "metadataSigned" as checked

  Scenario: Disable Metadata check for Test-Repository-x86_64 repository
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Manage Repositories" in the left menu
    And I follow "Test-Repository-x86_64"
    When I uncheck "metadataSigned"
    And I click on "Update Repository"
    Then I should see a "Repository updated successfully" text
    And I should see "metadataSigned" as unchecked

  Scenario: Add repository to the x86_64 channel
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Overview" in the left menu
    And I follow "Test-Channel-x86_64"
    And I follow "Repositories" in the content area
    When I select the "Test-Repository-x86_64" repo
    And I click on "Update Repositories"
    Then I should see a "Test-Channel-x86_64 repository information was successfully updated" text

  Scenario: Sync the repository in the x86_64 channel
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Manage Repositories" in the left menu
    And I follow "Overview" in the left menu
    And I follow "Test-Channel-x86_64"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    When I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-x86_64." text

  Scenario: Adding Test-Repository-i586 repository
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Manage Repositories" in the left menu
    And I follow "Create Repository"
    When I enter "Test-Repository-i586" as "label"
    And I enter "file:///srv/www/htdocs/pub/TestRepo/" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

  Scenario: Add repository to the i586 channel
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Overview" in the left menu
    And I follow "Test-Channel-i586"
    And I follow "Repositories" in the content area
    When I select the "Test-Repository-i586" repo
    And I click on "Update Repositories"
    Then I should see a "Test-Channel-i586 repository information was successfully updated" text

  Scenario: Sync the repository in the i586 channel
    Given I am authorized as "testing" with password "testing"
    And I follow "Home" in the left menu
    And I follow "Channels"
    When I follow "Manage Software Channels" in the left menu
    And I follow "Overview" in the left menu
    And I follow "Test-Channel-i586"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    When I click on "Sync Now"
    Then I should see a "Repository sync scheduled for Test-Channel-i586." text

  Scenario: schedule "errata-cache" refresh
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row

  Scenario: schedule "channel-repodata" refresh
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "Task Schedules"
    And I follow "channel-repodata-default"
    And I follow "channel-repodata-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row
