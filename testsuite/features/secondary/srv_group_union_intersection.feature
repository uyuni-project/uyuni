# Copyright (c) 2018-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature will be fully tested only if we have a Red Hat-like client
# and a Debian-like client running

@scope_visualization
Feature: Work with Union and Intersection buttons in the group list

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Create a sles group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "sles" as "name"
    And I enter "SLES systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group sles created." text

  Scenario: Add systems to the sles group
    When I follow the left menu "Systems > System Groups"
    When I follow "sles"
    And I follow "Target Systems"
    And I check the "sle_minion" client
    And I click on "Add Systems"
    Then I should see a "added to sles server group." text

@rhlike_minion
  Scenario: Create a rhlike group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "rhlike" as "name"
    And I enter "Red Hat-like systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group rhlike created." text

@rhlike_minion
  Scenario: Add systems to the rhlike group
    When I follow the left menu "Systems > System Groups"
    When I follow "rhlike"
    And I follow "Target Systems"
    And I check the "rhlike_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to rhlike server group." text

@deblike_minion
   Scenario: Create a deblike group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "deblike" as "name"
    And I enter "Debian-like systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group deblike created." text

@deblike_minion
  Scenario: Add systems to the deblike group
    When I follow the left menu "Systems > System Groups"
    When I follow "deblike"
    And I follow "Target Systems"
    And I check the "deblike_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to deblike server group." text

  Scenario: Add the sles group to SSM
    When I follow the left menu "Systems > System Groups"
    When I click on "Use in SSM" in row "sles"
    And I should see a "Selected Systems List" text
    And I should see "sle_minion" as link

@rhlike_minion
  Scenario: Add a union of 2 groups to SSM - Red Hat-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "rhlike" in the list
    And I click on "Work With Union"
    And I should see "sle_minion" as link
    And I should see "rhlike_minion" as link

@rhlike_minion
  Scenario: Add an intersection of 2 groups to SSM - Red Hat-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "rhlike" in the list
    And I click on "Work With Intersection"
    And I should not see a "sle_minion" link
    And I should not see a "rhlike_minion" link

@deblike_minion
  Scenario: Add a union of 2 groups to SSM - Debian-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "deblike" in the list
    And I click on "Work With Union"
    And I should see "sle_minion" as link
    And I should see "deblike_minion" as link

@deblike_minion
  Scenario: Add an intersection of 2 groups to SSM - Debian-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "deblike" in the list
    And I click on "Work With Intersection"
    And I should not see a "sle_minion" link
    And I should not see a "deblike_minion" link

  Scenario: Cleanup: remove the sles group
    When I follow the left menu "Systems > System Groups"
    When I follow "sles" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

@rhlike_minion
  Scenario: Cleanup: remove the rhlike group
    When I follow the left menu "Systems > System Groups"
    When I follow "rhlike" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

@deblike_minion
  Scenario: Cleanup: remove the deblike group
    When I follow the left menu "Systems > System Groups"
    When I follow "deblike" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

  Scenario: Cleanup: remove remaining systems from SSM after group union and intersection tests
    When I click on the clear SSM button
