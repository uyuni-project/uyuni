# Copyright (c) 2015-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_salt
Feature: Verify the minion registration
  In order to validate the completeness of minion registration
  I want to see minion details and installed packages

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Check the Salt entitlement
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "[Salt]" text

  Scenario: Check that installed packages are visible
    When I follow "Software" in the content area
    And I follow "List / Remove"
    Then I should see a "aaa_base" text

  Scenario: Check that Hardware Refresh button works on a SLES Salt minion
    When I follow "Details" in the content area
    And I follow "Hardware" in the content area
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "sle_minion"

  Scenario: Check that Update Properties button works
    When I follow "Details" in the content area
    And I follow "Hardware" in the content area
    And I click on "Update Properties"
    Then I should see a "Networking properties updated." text
