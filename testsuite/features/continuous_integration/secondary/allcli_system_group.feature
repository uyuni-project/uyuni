# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Manage a group of systems

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Fail to create a group with only its name
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "new-systems-group" as "name"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: Fail to create a group with only its description
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: Create a group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "new-systems-group" as "name"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "System group new-systems-group created." text

  Scenario: Add the SLE minion system to the group
    When I follow the left menu "Systems > System Groups"
    When I follow "new-systems-group"
    And I follow "Target Systems"
    And I check the "sle_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to new-systems-group server group." text

  Scenario: Check that the SLE minion is part of the new group
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Groups" in the content area
    Then I should see a "new-systems-group" text

@rhlike_minion
  Scenario: Add the Red Hat-like minion to the group in a different way
    When I follow the left menu "Systems > System Groups"
    Then I should see a "System Groups" text
    When I follow "new-systems-group"
    And I follow "Target Systems"
    Then I should see a "The following are systems that may be added to this group." text
    When I check the "rhlike_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to new-systems-group server group" text

  Scenario: Add the new group to SSM
    When I follow the left menu "Systems > System Groups"
    When I click on "Use in SSM" in row "new-systems-group"
    Then I should see a "Selected Systems List" text
    And I should see "rhlike_minion" as link
    And I should see "sle_minion" as link

   #container already has locale formula installed
   @skip_if_container_server 
   Scenario: Install the locale formula package on the server
     When I manually install the "locale" formula on the server

   Scenario: I synchronize all Salt dynamic modules on "sle_minion"
     When I synchronize all Salt dynamic modules on "sle_minion"

  Scenario: New formula page is rendered for the system group
    When I follow the left menu "Systems > System Groups"
    When I follow "new-systems-group"
    And I follow "Formulas"
    Then I should see a "Choose formulas:" text
    And I should see a "General System Configuration" text
    And the "locale" formula should be unchecked

@rhlike_minion
  Scenario: Apply the highstate to the group
    When I follow the left menu "Systems > System Groups"
    Then I should see a "System Groups" text
    When I follow "new-systems-group"
    And I follow "States"
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "scheduled"
    Then I should see a "Apply states (highstate)" text
    And I should see a "Action Details" text
    And I wait until I see "2 systems successfully completed this action." text, refreshing the page

  Scenario: Remove SLE client from new group
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Groups"
    And I check "new-systems-group" in the list
    And I click on "Leave Selected Groups"
    Then I should see a "1 system groups removed." text

  Scenario: Remove SLE minion from new group
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Groups"
    And I check "new-systems-group" in the list
    And I click on "Leave Selected Groups"
    Then I should see a "1 system groups removed." text

  # Red Hat-like minion is intentionally not removed from group

  Scenario: Cleanup: uninstall formula from the server
    When I manually uninstall the "locale" formula from the server

  Scenario: Cleanup: remove the new group
    When I follow the left menu "Systems > System Groups"
    When I follow "new-systems-group" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "System group" text
    And I should see a "new-systems-group" text
    And I should see a "deleted" text
