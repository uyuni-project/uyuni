# COPYRIGHT (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Pick dates
  In order to execute actions at a certain date
  As a authorized user
  I want to be able to easily pick dates

  Scenario: Date picker is by default set to today
    Given I am on the Systems overview page of this client
    And I follow "Remote Command" in the content area
    And I open the date picker
    Then the date picker title should be the current month and year

  Scenario: Pick a time should set the right hidden fields
    Given I am on the Systems overview page of this client
    And I follow "Remote Command" in the content area
    And I enter "ls" as "Script"
    And I pick "2016-08-27" as date
    And I pick "5:30 pm" as time
    Then the date field is set to "2016-08-27"
    And the time field is set to "17:30"
    And the date picker is closed
