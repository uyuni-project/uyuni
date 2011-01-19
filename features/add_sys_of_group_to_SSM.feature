# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Add systems of a group to the System Set Manager (SSM)
  In Order to manage multiple systems
  As the testing user
  I want to add the systems of group newgroup to the System Set Manager

  Scenario: add newgroup to SSM
    Given I am on the groups page
     And I should see a "No systems selected" text
    When I click on "Use in SSM" for "newgroup"
    Then I should see a "1 system selected" text
     And I should see a "Selected Systems List" text
     And I should see this client as a link


