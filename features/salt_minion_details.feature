# Copyright (c) 2015 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Verify the minion registration
  In order to validate the completeness of minion registration
  I want to see minion details and installed packages

  Scenario: Check for the Salt entitlement
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Salt]" text
    And I wait until onboarding is completed for "sle-minion"

  Scenario: Check installed packages are visible
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "List / Remove"
    Then I should see a "aaa_base" text
    And I should see a "aaa_base-extras" text

  Scenario: Accepted minion has a base channel
    Given I am authorized as "testing" with password "testing"
    And that this minion is registered in Spacewalk
    And I follow the sle minion
    And I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then this minion should have a Base channel set
