# Copyright (c) 2015 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Verify packages of a registered system
  In Order to validate completeness registered system
  As a authorized user
  I want to see it has installed packages visible

  Background:
    Given I am authorized as "admin" with password "admin"
    Given this client hostname

  Scenario: This client is a registered system
    When I follow "Systems"
    Then I should see a "System Overview" text
    Then I should see this client as a registered system

  Scenario: Assign Management entitlement to this client
    When I follow "Systems"
    Then I follow this client as a registered system
    Then I follow "Properties"
    And I select "Management" from "baseentitlement"
    And I press "Update Properties"

  Scenario: Check installed packages are visible
    When I follow "Systems"
    Then I follow this client as a registered system
    Then I follow "Software"
    Then I follow "List / Remove"
    Then I should see a "aaa_base" text
    And I should see a "aaa_base-extras" text
