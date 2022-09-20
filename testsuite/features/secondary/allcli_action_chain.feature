# Copyright (c) 2018-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@rhlike_minion
@sle_minion
@scope_action_chains
Feature: Action chains on several systems at once

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: Use the Test-Channel-x86_64 in Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Pre-requisite: downgrade packages before action chain test on several systems
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I enable repository "test_repo_rpm_pool" on this "rhlike_minion"
    And I remove package "andromeda-dummy" from this "rhlike_minion" without error control
    And I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I install package "andromeda-dummy-1.0" on this "sle_minion"
    And I install package "andromeda-dummy-1.0" on this "rhlike_minion"
    And I refresh the metadata for "sle_minion"
    And I refresh the metadata for "rhlike_minion"

  Scenario: Pre-requisite: refresh package list and check installed packages before action chain test on several systems
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "andromeda-dummy-1.0" installed on "sle_minion"
    When I refresh packages list via spacecmd on "rhlike_minion"
    And I wait until refresh package list on "rhlike_minion" is finished
    Then spacecmd should show packages "andromeda-dummy-1.0" installed on "rhlike_minion"

  Scenario: Pre-requisite: wait until downgrade is finished before action chain test on several systems
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button until page does contain "andromeda-dummy-1.0" text

  Scenario: Pre-requisite: ensure the errata cache is computed before action chain test on several systems
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Pre-requisite: remove all action chains before testing on several systems
    Given I am logged in API as user "admin" and password "admin"
    When I delete all action chains
    And I cancel all scheduled actions
    And I logout from API

  Scenario: Add an action chain using system set manager for Red Hat-like minion and SLE minion
    When I follow the left menu "Systems > System List > All"
    And I check the "sle_minion" client
    And I check the "rhlike_minion" client
    And I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "Install" in the content area
    And I follow "SLE-Custom-Channel-x86_64" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    Then I should see "sle_minion" hostname
    And I should see "rhlike_minion" hostname
    When I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Package installations are being scheduled" text
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "remote commands" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/action_chain_done
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see "sle_minion" hostname
    And I should see "rhlike_minion" hostname

  Scenario: Verify action chain for two systems
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Install or update andromeda-dummy on 2 systems" text
    And I should see a "2. Run a remote command on 2 systems" text
    And I click on "Save and Schedule"
    Then I should see a "Action Chain new action chain has been scheduled for execution." text

  Scenario: Verify that the action chain from the system set manager was executed successfully
    When I wait until file "/tmp/action_chain_done" exists on "rhlike_minion"
    And I wait until file "/tmp/action_chain_done" exists on "sle_minion"
    Then "andromeda-dummy" should be installed on "rhlike_minion"
    And "andromeda-dummy" should be installed on "sle_minion"

  Scenario: Cleanup: remove package and repository used in action chain for several systems
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "andromeda-dummy" from this "rhlike_minion" without error control
    And I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I disable repository "test_repo_rpm_pool" on this "rhlike_minion" without error control

  Scenario: Cleanup: remove temporary files for testing action chains on several systems
    When I run "rm /tmp/action_chain_done" on "sle_minion" without error control
    And I run "rm /tmp/action_chain_done" on "rhlike_minion" without error control

  Scenario: Cleanup: Use the Test Base Channel in Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Cleanup: remove remaining systems from SSM after action chain tests on several systems
    When I click on "Clear"
