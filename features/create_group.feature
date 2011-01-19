# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Create a group
  In Order manage systems
  As the testing user
  I want to create a group

  Scenario: fail to create a group
    Given I am on the groups page
    When I follow "create new group"
     And I enter "newgroup" as "name"
     And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: fail to create a group
    Given I am on the groups page
    When I follow "create new group"
     And I enter "My new group" as "description"
     And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: create a group
    Given I am on the groups page
    When I follow "create new group"
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
    Given I am on the groups page
    When I follow "newgroup"
     And I follow "Systems" in class "content-nav"
    Then I should see this client as a link
