# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_software_channels_and_repositories
@scope_hibernate
Feature: Software channels and repositories management
  Software channels and repositories can be operated,
  related CRUD operation encapsultade with hibernate work.

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Create a custom channel with a repository
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Hibernate channel" as "Channel Name"
    And I enter "hibernate-test-channel" as "Channel Label"
    And I enter "hibernate-test-channel" as "Channel Summary"
    And I click on "Create Channel"
    And I wait until I see "Hibernate channel created." text
    And I follow "Repositories" in the content area
    And I follow "Add / Remove"
    And I follow "Create Repository"
    And I enter "hibernate-test-repository" as "label"
    And I enter "https://localhost" as "url"
    And I select "yum" from "contenttype"
    And I click on "Create Repository"
    And I wait until I see "Repository created successfully" text
    Then I should see a "hibernate-test-channel repository information was successfully updated" text

  Scenario: Create a repository from channel management
    When I follow the left menu "Software > Manage > Channels"
    And I should see a "Hibernate channel" text
    And I follow "Hibernate channel"
    And I follow "Repositories" in the content area
    And I follow "Add / Remove"
    And I follow "Create Repository"
    And I enter "hibernate-test-repository-2" as "label"
    And I enter "https://localhost.localdomain" as "url"
    And I select "yum" from "contenttype"
    And I click on "Create Repository"
    And I wait until I see "Repository created successfully" text
    Then I should see a "hibernate-test-channel repository information was successfully updated" text

  Scenario: Modify the channel Hibernate
    When I follow the left menu "Software > Manage > Channels"
    And I should see a "Hibernate channel" text
    And I follow "Hibernate channel"
    And I should not see a "hibernate-channel" text
    And I enter "Hibernate test channel" as "Channel Name"
    And I click on "Update Channel"
    Then I wait until I see "Channel Hibernate test channel updated." text

  Scenario: Modify the repository of the channel Hibernate
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "hibernate-test-repository-2"
    And I enter "hibernate-repository" as "label"
    And I select "deb" from "contenttype"
    And I click on "Update Repository"
    Then I wait until I see "Repository updated successfully" text

  Scenario: Check the Hibernate channel and repositories
    When I follow the left menu "Software > Manage > Channels"
    And I should see a "Hibernate test channel" text
    And I follow "Hibernate test channel"
    And I follow "Repositories" in the content area
    And I follow "Sync"
    Then I should see a "hibernate-repository" text
    And I should see a "hibernate-test-repository" text
    And I follow "hibernate-repository"
    And I should see a "deb" text

  Scenario: Cleanup: Delete Hibernate repository from Repositories
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "hibernate-repository"
    And I follow "Delete Repository"
    And I should see a "Confirm Repository Delete" text
    And I click on "Delete Repository"
    Then I wait until I see "Repository deleted successfully" text

  Scenario: Cleanup: Delete Hibernate repository from Channels
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Hibernate test channel"
    And I follow "Repositories" in the content area
    And I follow "hibernate-test-repository"
    And I follow "Delete Repository"
    And I should see a "Confirm Repository Delete" text
    And I click on "Delete Repository"
    Then I wait until I see "Repository deleted successfully" text

  Scenario: Cleanup: Delete Hibernate channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Hibernate test channel"
    And I follow "Delete Channel"
    And I should see a "Delete Channel" text
    And I click on "Delete Channel"
    Then I wait until I see "Channel Hibernate test channel has been deleted." text

@skip_if_github_validation
  # server log contains hibernate excepcions, please remove the skip when it's fixed
  Scenario: Check the cleanup succeeded and the errors in logs
    When I follow the left menu "Software > Manage > Repositories"
    And I should not see a "hibernate-repository" text
    And I should not see a "hibernate-test-repository" text
    And I should not see a "hibernate-test-repository-2" text
    And I follow the left menu "Software > Manage > Channels"
    Then I should not see a "Hibernate test channel" text
    And I should not see a "Hibernate channel" text
    And the server log should not contain "hibernate" errors
