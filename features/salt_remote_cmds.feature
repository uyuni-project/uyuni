# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the remote commands via salt
  In Order to test the remote commands via salt
  As an authorized user
  I want to verify that the remote commands function

  Background:
    Given I am authorized as "testing" with password "testing"

  Scenario: Run a remote command
    Given I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I click on preview
    Then I should see my hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results
    And I verify the results
