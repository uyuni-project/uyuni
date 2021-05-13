# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_client
@sle_minion
@scope_action_chains
Feature: Action chains on several systems at once

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: downgrade packages before action chain test on several systems
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I enable repository "test_repo_rpm_pool" on this "sle_client"
    And I remove package "andromeda-dummy" from this "sle_client" without error control
    And I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion"
    And I install old package "andromeda-dummy-1.0" on this "sle_client"
    And I run "zypper -n ref" on "sle_minion"
    And I run "zypper -n ref" on "sle_client"
    And I run "rhn_check -vvv" on "sle_client"

  Scenario: Pre-requisite: refresh package list and check installed packages before action chain test on several systems
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "andromeda-dummy-1.0" installed on "sle_minion"

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
    Given I am on the Systems overview page of this "sle_client"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Pre-requisite: remove all action chains before testing on several systems
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    When I delete all action chains
    And I cancel all scheduled actions

  Scenario: Add an action chain using system set manager for traditional client and Salt minion
    When I am on the System Overview page
    And I check the "sle_minion" client
    And I check the "sle_client" client
    And I am on System Set Manager Overview
    And I follow "Install" in the content area
    And I follow "Test-Channel-x86_64" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    Then I should see "sle_minion" hostname
    And I should see "sle_client" hostname
    When I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Package installations are being scheduled" text
    When I am on System Set Manager Overview
    And I follow "remote commands" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/action_chain_done
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see "sle_minion" hostname
    And I should see "sle_client" hostname

  Scenario: Verify action chain for two systems
    Given I am on the Systems overview page of this "sle_minion"
    When I run "rhn-actions-control --enable-all" on "sle_client"
    And I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Install or update andromeda-dummy on 2 systems" text
    And I should see a "2. Run a remote command on 2 systems" text
    And I click on "Save and Schedule"
    Then I should see a "Action Chain new action chain has been scheduled for execution." text

  Scenario: Verify that the action chain from the system set manager was executed successfully
    When I run "rhn_check -vvv" on "sle_client"
    And I wait until file "/tmp/action_chain_done" exists on "sle_client"
    And I wait until file "/tmp/action_chain_done" exists on "sle_minion"
    Then "andromeda-dummy" should be installed on "sle_client"
    And "andromeda-dummy" should be installed on "sle_minion"

  Scenario: Cleanup: remove package and repository used in action chain for several systems
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "andromeda-dummy" from this "sle_client" without error control
    And I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I disable repository "test_repo_rpm_pool" on this "sle_client" without error control

  Scenario: Cleanup: remove temporary files for testing action chains on several systems
    When I run "rm /tmp/action_chain_done" on "sle_minion" without error control
    And I run "rm /tmp/action_chain_done" on "sle_client" without error control

  Scenario: Cleanup: remove remaining systems from SSM after action chain tests on several systems
    When I follow "Clear"
