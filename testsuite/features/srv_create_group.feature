# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create a group

  Scenario: Fail to create a group with only its name
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "newgroup" as "name"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: Fail to create a group with only its description
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: Create a group
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "newgroup" as "name"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "System group newgroup created." text

  Scenario: Add a system to the group
    Given I am on the groups page
    When I follow "newgroup"
    And I follow "Target"
    And I check the "sle-client" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to newgroup server group." text

  Scenario: Check that this client is part of the new group
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Groups" in the content area
    Then I should see a "newgroup" text 

  Scenario: Add the new group to SSM
    Given I am on the groups page
    When I click on "Use in SSM" for "newgroup"
    Then I should see a "system selected" text
    And I should see a "Selected Systems List" text
    And I should see "sle-client" as link
  
  Scenario: Remove client from new group
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Groups"
    And I check "newgroup" in the list
    And I click on "Leave Selected Groups"
    Then I should see a "1 system groups removed." text

  Scenario: Cleanup: remove the new group
    Given I am on the groups page
    When I follow "newgroup" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "System group" text
    And I should see a "newgroup" text
    And I should see a "deleted" text
