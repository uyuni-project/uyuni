# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT

@sle_minion
Feature: Action descriptions on scheduled actions
  In order to know why an action was scheduled
  As an authorized user
  I want to add a description to a scheduled action

  Scenario: Log in as org admin user
    Given I am authorized for the "Admin" section

  Scenario: Schedule a remote command with a description on the SLE minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Remote Command" in the content area
    And I enter "#!/bin/sh\nls /tmp" as "script_body" text area
    And I enter "Test description" as "schedule-action-description"
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text

  Scenario: Check the description in the history of the SLE minion
    Given I am on the Systems overview page of this "sle_minion"
    When I wait at most 300 seconds until the event "Remote Command on" is picked up
    And I wait at most 300 seconds until the event "Remote Command on" is completed in the history
    And I follow "History"
    Then I should see a "Test description" text
