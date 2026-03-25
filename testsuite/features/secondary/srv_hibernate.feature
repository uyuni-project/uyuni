# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Hibernate
  Hibernate is a bridge between WebUI and database,
  so the CRUD operations should be triggered
  in order to test hibernate function.

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Create a custom channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Hibernate test channel" as "Channel Name"
    And I enter "hibernate-test-channel" as "Channel Label"
    And I enter "hibernate-test-channel" as "Channel Summary"
    And I click on "Create Channel"
    Then I wait until I see "Hibernate test channel created." text

  Scenario: Create a repository from channel management
    When I follow the left menu "Software > Manage > Channels"
    And I should see a "Hibernate test channel" text
    And I follow "Hibernate test channel"
    And I follow "Repositories" in the content area
    And I follow "Add / Remove"
    And I follow "Create Repository"
    And I enter "hibernate-test-repository" as "label"
    And I enter "https://localhost" as "url"
    And I click on "Create Repository"
    Then I wait until I see "hibernate-test-repository repository created." text
    And the server log does not contain "hibernate" errors

  Scenario: Cleanup: Delete Hibernate test repository
    When I follow the left menu "Software > Manage > Repositories"
    And I follow "hibernate-test-repository"
    And I follow "Delete Repository"
    And I should see a "Confirm Repository Delete" text
    And I click on "Delete Repository"
    Then I wait until I see "Repository deleted successfully" text

  Scenario: Cleanup: Delete Hibernate test channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Hibernate test channel"
    And I follow "Delete Channel"
    And I should see a "Delete Channel" text
    And I click on "Delete Channel"
    Then I wait until I see "Hibernate test channel has been deleted." text
