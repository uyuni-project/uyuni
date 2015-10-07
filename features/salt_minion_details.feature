# Copyright (c) 2015 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Verify the minion registration
  In order to validate the completeness of minion registration
  I want to see minion details and installed packages

  Scenario: Check for the SaltStack entitlement
    Given I am on the Systems overview page of this client
    Then I should see a "[SaltStack]" text

  Scenario: Check installed packages are visible
    Given I am on the Systems overview page of this client
    When I follow "Software"
    And I follow "List"
    Then I should see a "aaa_base" text
    And I should see a "aaa_base-extras" text
