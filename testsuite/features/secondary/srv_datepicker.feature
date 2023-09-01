# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Pick dates
  In order to execute actions at a certain date
  As a authorized user
  I want to be able to easily pick dates

  Scenario: Log in as admin user
    Given I am authorized
    And I am on the Systems overview page of this "sle_minion"

  Scenario: Date picker is by default set to today
    When I follow "Remote Command" in the content area
    And I open the date picker
    Then the date picker title should be the current month and year

  Scenario: Picking a time sets the hidden fields
    When I follow "Details" in the content area
    And I follow "Remote Command" in the content area
    And I enter "ls" as "Script"
    And I pick "2022-08-27" as date
    And I pick "17:30" as time
    Then the date field should be set to "2022-08-27"
    And the time field should be set to "17:30"
    And the date picker should be closed
