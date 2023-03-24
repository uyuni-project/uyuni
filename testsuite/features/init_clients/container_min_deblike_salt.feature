# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Debian-like minion
#  2) subscribe it to a base channel for testing

@deblike_minion
Feature: Bootstrap a Debian-like minion

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Debian-like minion
    When I follow the left menu "Salt > Keys"
    And I accept "deblike_minion" key in the Salt master
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "deblike_minion", refreshing the page
    And I query latest Salt changes on Debian-like system "deblike_minion"

