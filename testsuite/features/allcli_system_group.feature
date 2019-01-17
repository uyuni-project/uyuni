# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create a group

  Scenario: Fail to create a group with only its name
    Given I am on the groups page
    When I follow "Create Group"
    And I enter "new-systems-group" as "name"
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
    And I enter "new-systems-group" as "name"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "System group new-systems-group created." text

  Scenario: Add the SLE minion system to the group
    Given I am on the groups page
    When I follow "new-systems-group"
    And I follow "Target Systems"
    And I check the "sle-minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to new-systems-group server group." text

  Scenario: Add the SLE client system to the group
    Given I am on the groups page
    When I follow "new-systems-group"
    And I follow "Target Systems"
    And I check the "sle-client" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to new-systems-group server group." text

  Scenario: Check that the SLE client is part of the new group
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Groups" in the content area
    Then I should see a "new-systems-group" text

  Scenario: Check that the SLE minion is part of the new group
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Groups" in the content area
    Then I should see a "new-systems-group" text

  Scenario: Add the new group to SSM
    Given I am on the groups page
    When I click on "Use in SSM" for "new-systems-group"
    Then I should see a "Selected Systems List" text
    And I should see "sle-client" as link
    And I should see "sle-minion" as link

  Scenario: Check formula page is rendered for the system group
    Given I am on the groups page
    When I follow "new-systems-group"
    And I follow "Formulas"
    Then I should see a "Choose formulas:" text
    And I should see a "General System Configuration" text
    And the "locale" formula should be unchecked

  Scenario: Remove SLE client from new group
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Groups"
    And I check "new-systems-group" in the list
    And I click on "Leave Selected Groups"
    Then I should see a "1 system groups removed." text

  Scenario: Remove SLE minion from new group
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Groups"
    And I check "new-systems-group" in the list
    And I click on "Leave Selected Groups"
    Then I should see a "1 system groups removed." text

  Scenario: Cleanup: remove the new group
    Given I am on the groups page
    When I follow "new-systems-group" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "System group" text
    And I should see a "new-systems-group" text
    And I should see a "deleted" text
