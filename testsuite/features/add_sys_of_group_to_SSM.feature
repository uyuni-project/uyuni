# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Add systems of a group to the System Set Manager (SSM)
  In Order to manage multiple systems
  As the testing user
  I want to add the systems of group newgroup to the System Set Manager

  Scenario: add newgroup to SSM
    Given I am on the groups page
    And I click on "Use in SSM" for "newgroup"
    And I should see a "system selected" text
    And I should see a "Selected Systems List" text
    Then I should see this client as link
