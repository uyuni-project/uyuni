# Copyright (c) 2015-2018 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Verify the minion registration
  In order to validate the completeness of minion registration
  I want to see minion details and installed packages

  Scenario: Check the Salt entitlement
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "[Salt]" text

  Scenario: Check that installed packages are visible
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove"
    Then I should see a "aaa_base" text

  Scenario: Check that Hardware Refresh button works on a SLES Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    And I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled" is completed
