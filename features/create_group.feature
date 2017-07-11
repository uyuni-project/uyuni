# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create a group
  In Order manage systems
  As the testing user
  I want to create a group

  Scenario: fail to create a group, enter only Name
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "newgroup" as "name"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: fail to create a group, enter only Description
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: create a group
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "newgroup" as "name"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "System group newgroup created." text

  Scenario: add a system to the group
    Given I am on the groups page
    When I follow "newgroup"
    And I follow "Target Systems"
    And I check this client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to newgroup server group." text

  Scenario: check this client is part of newgroup
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    And I follow this "sle-client" link
    When I follow "Groups" in the content area
    Then I should see a "newgroup" text 

  Scenario: add newgroup to SSM
    Given I am on the groups page
    And I click on "Use in SSM" for "newgroup"
    And I should see a "system selected" text
    And I should see a "Selected Systems List" text
    Then I should see this client as link
  
  Scenario: remove client from group
   Given I am on the Systems overview page of this "sle-client"
   And I follow "Groups"
   And I check "newgroup" in the list
   And I click on "Leave Selected Groups"
   Then I should see a "1 system groups removed." text

  Scenario: Cleanup: remove newgroup group
   Given I am on the groups page
   And I follow "newgroup" in the content area
   And I follow "Delete Group" in the content area
   When I click on "Confirm Deletion"
   And I should see a "System group" text
   And I should see a "newgroup" text
   Then I should see a "deleted" text
