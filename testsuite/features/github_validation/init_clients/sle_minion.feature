# Copyright (c) 2023 SUSE LLC
# SPDX-License-Identifier: MIT

@sle_minion
Feature: Bootstrap a Salt minion via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the new bootstrapped minion in System List page
    When I follow the left menu "Salt > Keys"
    And I accept "sle_minion" key in the Salt master
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle_minion", refreshing the page
    Then the Salt master can reach "sle_minion"

