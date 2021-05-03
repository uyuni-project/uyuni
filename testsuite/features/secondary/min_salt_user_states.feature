# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
Feature: Coexistence with user-defined states

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a user-defined state
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "States" in the content area
    And I install a user-defined state for "sle_minion" on the server
    And I follow "Highstate" in the content area
    And I click on "Show full highstate output"
    And I wait for "6" seconds
    Then I should see a "user_defined_state" or "running as PID" text in element "highstate"

  Scenario: Trigger highstate from XML-RPC
    And I am logged in via XML-RPC system as user "admin" and password "admin"
    When I schedule a highstate for "sle_minion" via XML-RPC
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then file "/tmp/test_user_defined_state" should exist on "sle_minion"
    And I logout from XML-RPC system namespace

  Scenario: Cleanup: remove user-defined state and the file it created
    When I follow "States" in the content area
    And I uninstall the user-defined state from the server
    And I uninstall the managed file from "sle_minion"
    And I follow "Highstate" in the content area
    And I click on "Show full highstate output"
    And I wait for "6" seconds
    Then I should not see a "user_defined_state" text in element "highstate"
