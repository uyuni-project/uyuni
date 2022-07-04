# Copyright (c) 2018-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature will be fully tested only if we have a
# RedHat-like and a Debian-like client running

@scope_visualization
Feature: Work with Union and Intersection buttons in the group list

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a SLES group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "sles" as "name"
    And I enter "SLES systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group sles created." text

  Scenario: Add systems to the SLES group
    When I follow the left menu "Systems > System Groups"
    When I follow "sles"
    And I follow "Target Systems"
    And I check the "sle_client" client
    And I check the "sle_minion" client
    And I click on "Add Systems"
    Then I should see a "2 systems were added to sles server group." text

@rh_minion
  Scenario: Create a RedHat-like group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "rh" as "name"
    And I enter "RedHat-like systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group rh created." text

@rh_minion
  Scenario: Add systems to the RedHat-like group
    When I follow the left menu "Systems > System Groups"
    When I follow "rh"
    And I follow "Target Systems"
    And I check the "rh_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to rh server group." text

@deb_minion
   Scenario: Create a Debian-like group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "deb" as "name"
    And I enter "Debian-like systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group deb created." text

@deb_minion
  Scenario: Add systems to the Debian-like group
    When I follow the left menu "Systems > System Groups"
    When I follow "deb"
    And I follow "Target Systems"
    And I check the "deb_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to deb server group." text

  Scenario: Create a traditional group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "traditional" as "name"
    And I enter "Traditional systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group traditional created." text

  Scenario: Add systems to the traditional group
    When I follow the left menu "Systems > System Groups"
    When I follow "traditional"
    And I follow "Target Systems"
    And I check the "sle_client" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to traditional server group." text

  Scenario: Add the SLES group to SSM
    When I follow the left menu "Systems > System Groups"
    When I click on "Use in SSM" in row "sles"
    And I should see a "systems selected" text
    And I should see a "Selected Systems List" text
    Then I should see "sle_client" as link
    And I should see "sle_minion" as link

@rh_minion
  Scenario: Add a union of 2 groups to SSM - RedHat-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "rh" in the list
    And I click on "Work With Union"
    Then I should see "sle_client" as link
    And I should see "sle_minion" as link
    And I should see "rh_minion" as link

@rh_minion
  Scenario: Add an intersection of 2 groups to SSM - RedHat-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "traditional" in the list
    And I click on "Work With Intersection"
    Then I should see "sle_client" as link
    And I should not see a "sle_minion" link
    And I should not see a "rh_minion" link

@deb_minion
  Scenario: Add a union of 2 groups to SSM - Debian-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "deb" in the list
    And I click on "Work With Union"
    Then I should see "sle_client" as link
    And I should see "sle_minion" as link
    And I should see "deb_minion" as link

@deb_minion
  Scenario: Add an intersection of 2 groups to SSM - Debian-like
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "traditional" in the list
    And I click on "Work With Intersection"
    Then I should see "sle_client" as link
    And I should not see a "sle_minion" link
    And I should not see a "deb_minion" link

  Scenario: Cleanup: remove the SLES group
    When I follow the left menu "Systems > System Groups"
    When I follow "sles" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

@rh_minion
  Scenario: Cleanup: remove the RedHat-like group
    When I follow the left menu "Systems > System Groups"
    When I follow "rh" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

@deb_minion
  Scenario: Cleanup: remove the Debian-like group
    When I follow the left menu "Systems > System Groups"
    When I follow "deb" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

  Scenario: Cleanup: remove the traditional group
    When I follow the left menu "Systems > System Groups"
    When I follow "traditional" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

  Scenario: Cleanup: remove remaining systems from SSM after group union and intersection tests
    When I follow "Clear"
