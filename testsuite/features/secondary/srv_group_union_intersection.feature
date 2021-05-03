# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature will be fully tested only if we have a CentOS and/or Ubuntu client
# running

@scope_visualization
Feature: Work with Union and Intersection buttons in the group list

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

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
    And I check the "sle_client" client
    And I check the "sle_minion" client
    And I click on "Add Systems"
    Then I should see a "2 systems were added to sles server group." text

@centos_minion
  Scenario: Create a centos group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "centos" as "name"
    And I enter "CentOS systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group centos created." text

@centos_minion
  Scenario: Add systems to the centos group
    When I follow the left menu "Systems > System Groups"
    When I follow "centos"
    And I follow "Target Systems"
    And I check the "ceos_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to centos server group." text

@ubuntu_minion
   Scenario: Create a ubuntu group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "ubuntu" as "name"
    And I enter "Ubuntu systems" as "description"
    And I click on "Create Group"
    Then I should see a "System group ubuntu created." text

@ubuntu_minion
  Scenario: Add systems to the ubuntu group
    When I follow the left menu "Systems > System Groups"
    When I follow "ubuntu"
    And I follow "Target Systems"
    And I check the "ubuntu_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to ubuntu server group." text

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

  Scenario: Add the sles group to SSM
    When I follow the left menu "Systems > System Groups"
    When I click on "Use in SSM" in row "sles"
    And I should see a "systems selected" text
    And I should see a "Selected Systems List" text
    Then I should see "sle_client" as link
    And I should see "sle_minion" as link

@centos_minion
  Scenario: Add a union of 2 groups to SSM - CentOS
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "centos" in the list
    And I click on "Work With Union"
    Then I should see "sle_client" as link
    And I should see "sle_minion" as link
    And I should see "ceos_minion" as link

@centos_minion
  Scenario: Add an intersection of 2 groups to SSM - CentOS
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "traditional" in the list
    And I click on "Work With Intersection"
    Then I should see "sle_client" as link
    And I should not see a "sle_minion" link
    And I should not see a "ceos_minion" link

@ubuntu_minion
  Scenario: Add a union of 2 groups to SSM - Ubuntu
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "ubuntu" in the list
    And I click on "Work With Union"
    Then I should see "sle_client" as link
    And I should see "sle_minion" as link
    And I should see "ubuntu_minion" as link

@ubuntu_minion
  Scenario: Add an intersection of 2 groups to SSM - Ubuntu
    When I follow the left menu "Systems > System Groups"
    When I check "sles" in the list
    And I check "traditional" in the list
    And I click on "Work With Intersection"
    Then I should see "sle_client" as link
    And I should not see a "sle_minion" link
    And I should not see a "ubuntu_minion" link

  Scenario: Cleanup: remove the sles group
    When I follow the left menu "Systems > System Groups"
    When I follow "sles" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

@centos_minion
  Scenario: Cleanup: remove the centos group
    When I follow the left menu "Systems > System Groups"
    When I follow "centos" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "deleted" text

@ubuntu_minion
  Scenario: Cleanup: remove the ubuntu group
    When I follow the left menu "Systems > System Groups"
    When I follow "ubuntu" in the content area
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
