# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Red Hat-like minion via salt
#  2) subscribe it to a base channel for testing

@rhlike_minion
Feature: Bootstrap a Red Hat-like minion

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Red Hat-like minion
    When I follow the left menu "Salt > Keys"
    And I accept "rhlike_minion" key in the Salt master
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "rhlike_minion", refreshing the page
    Then the Salt master can reach "rhlike_minion"

