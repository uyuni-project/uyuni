# Copyright (c) 2018-2025 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/minssh_action_chain.feature
# - features/secondary/allcli_action_chain.feature
# If the action chain fails to be completed and run.

@skip_if_github_validation
@sle_minion
@scope_action_chains
Feature: Action chains on Salt minions

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: downgrade repositories to lower version on Salt minion
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "virgo-dummy" from this "sle_minion" without error control
    And I install package "milkyway-dummy" on this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion"
    And I refresh the metadata for "sle_minion"

  Scenario: Pre-requisite: refresh package list and check installed packages after downgrade on SLE minion
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "milkyway-dummy andromeda-dummy-1.0" installed on "sle_minion"

  Scenario: Pre-requisite: wait until downgrade is finished on SLE minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button until page does contain "andromeda-dummy-1.0" text

  Scenario: Pre-requisite: ensure the errata cache is computed before testing on Salt minion
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Create a custom action chain for the Salt minion
    When I create an action chain with label "salt_minion_action_chain" via API 
    And I follow the left menu "Schedule > Action Chains"
    Then I should see a "salt_minion_action_chain" text

  Scenario: Add a patch installation to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I enter "andromeda" as the filtered synopsis
    And I click on the filter button
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a package removal to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    And I check row with "milkyway-dummy" and arch of "sle_minion"
    And I click on "Remove Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a package installation to an action chain on Salt minion
    When I follow "Software" in the content area
    And I follow "Install New Packages" in the content area
    And I enter "virgo-dummy" as the filtered package name
    And I click on the filter button
    And I check "virgo-dummy" in the list
    And I click on "Install Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Create a configuration channel for testing action chain on Salt minion
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Action Chain Channel" as "cofName"
    And I enter "actionchainchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Action Chain Channel" text

  Scenario: Add a configuration file to configuration channel for testing action chain on Salt minion
    When I follow the left menu "Configuration > Channels"
    And I follow "Action Chain Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/action-chain.cnf" as "cffPath"
    And I enter "Testchain=YES_PLEASE" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/action-chain.cnf from channel Action Chain Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Download the configuration file from configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Action Chain Channel"
    And I follow "List/Remove Files"
    And I follow "/etc/action-chain.cnf"
    And I follow "Download File"
    And I wait until file "/tmp/downloads/action-chain.cnf" exists on "localhost"
    Then file "/tmp/downloads/action-chain.cnf" should contain "Testchain=YES_PLEASE" on "localhost"

  Scenario: Subscribe system to configuration channel for testing action chain on Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Action Chain Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Add a configuration file deployment to the action chain on Salt minion
    When I follow the left menu "Configuration > Channels"
    And I follow "Action Chain Channel"
    And I follow "Deploy Files" in the content area
    And I click on "Deploy All Files"
    And I check the "sle_minion" client
    And I click on "Confirm & Deploy to Selected Systems"
    And I check radio button "schedule-by-action-chain"
    And I click on "Deploy Files to Selected Systems"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add apply highstate to action chain on Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "States" in the content area
    And I check radio button "schedule-by-action-chain"
    And I click on "Apply Highstate"

  Scenario: Add a reboot action to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow first "Schedule System Reboot"
    And I check radio button "schedule-by-action-chain"
    And I click on "Reboot system"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a remote command to the action chain on Salt minion
    When I follow "Details" in the content area
    And I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/action_chain_one_system_done
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Verify the action chain list on Salt minion
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "salt_minion_action_chain"
    Then I should see a "1. Apply patch(es) andromeda-dummy-6789 on 1 system" text
    And I should see a "2. Remove milkyway-dummy from 1 system" text
    And I should see a "3. Install or update virgo-dummy on 1 system" text
    And I should see a text like "4. Deploy.*/etc/action-chain.cnf.*to 1 system"
    And I should see a "5. Apply Highstate" text
    And I should see a "6. Reboot 1 system" text
    And I should see a "7. Run a remote command on 1 system" text

  Scenario: Check that a different user cannot see the action chain for Salt minion
    Given I am authorized as "testing" with password "testing"
    When I follow "Schedule"
    And I follow "Action Chains"
    Then I should not see a "salt_minion_action_chain" link

  Scenario: Execute the action chain from the web UI on Salt minion
    Given I am authorized for the "Admin" section
    When I am on the Systems overview page of this "sle_minion"
    And I follow "Schedule"
    And I follow "Action Chains"
    And I follow "salt_minion_action_chain"
    And I click on "Save and Schedule"
    Then I should see a "Action Chain salt_minion_action_chain has been scheduled for execution." text
    When I wait for "virgo-dummy" to be installed on "sle_minion"
    And I wait at most 300 seconds until file "/tmp/action_chain_one_system_done" exists on "sle_minion"

  # previous, completed, action chain will no longer be available
  Scenario: Create a custom action chain for the Salt minion
    When I create an action chain with label "salt_minion_action_chain_to_delete" via API
    And I follow the left menu "Schedule > Action Chains"
    Then I should see a "salt_minion_action_chain_to_delete" text

  Scenario: Add a remote command to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Remote Command"
    And I enter as remote command this script in
      """
      #!/bin/bash
      uptime
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Delete the action chain for Salt minion
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "salt_minion_action_chain_to_delete"
    And I follow "delete action chain" in the content area
    And I click on "Delete"

  Scenario: Downgrade again repositories to lower version on Salt minion
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "virgo-dummy" from this "sle_minion" without error control
    And I install package "milkyway-dummy" on this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion"

  Scenario: Refresh package list and check installed packages after second downgrade on SLE minion
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "milkyway-dummy andromeda-dummy-1.0" installed on "sle_minion"

  Scenario: Ensure again the errata cache is computed before testing on Salt minion
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Add operations to the action chain via API for Salt minions
    Given I want to operate on this "sle_minion"
    When I create an action chain with label "salt_minion_api_chain" via API 
    And I add a package install to the action chain via API
    And I add a package removal to the action chain via API
    And I add a package upgrade to the action chain via API
    And I add the script "exit 1;" to the action chain via API
    And I add a system reboot to the action chain via API
    Then I should be able to see all these actions in the action chain via API
    When I remove each action within the chain via API
    Then the current action chain should be empty
    And I delete the action chain via API

  Scenario: Run an action chain via API on Salt minion
    Given I want to operate on this "sle_minion"
    When I create an action chain with label "salt_minion_multiple_scripts" via API
    And I add the script "echo -n 1 >> /tmp/action_chain.log" to the action chain via API
    And I add the script "echo -n 2 >> /tmp/action_chain.log" to the action chain via API
    And I add the script "echo -n 3 >> /tmp/action_chain.log" to the action chain via API
    And I add the script "touch /tmp/action_chain_done" to the action chain via API
    Then I should be able to see all these actions in the action chain via API
    When I schedule the action chain via API
    And I wait until there are no more action chains listed via API
    And I wait until file "/tmp/action_chain_done" exists on "sle_minion"
    Then file "/tmp/action_chain.log" should contain "123" on "sle_minion"
    When I wait until there are no more scheduled actions listed via API

  Scenario: Cleanup: remove Salt minion from configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Action Chain Channel"
    And I follow "Systems" in the content area
    And I check the "sle_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: Cleanup: remove configuration channel for Salt minion
    When I follow the left menu "Configuration > Channels"
    And I follow "Action Chain Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: remove packages and repository used in action chain for Salt minion
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "virgo-dummy" from this "sle_minion" without error control
    And I remove package "milkyway-dummy" from this "sle_minion" without error control
    And I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control

  Scenario: Cleanup: remove temporary files for testing action chains on Salt minion
    When I run "rm -f /tmp/action_chain.log" on "sle_minion" without error control
    And I run "rm -f /tmp/action_chain_done" on "sle_minion" without error control
    And I run "rm -f /etc/action-chain.cnf" on "sle_minion" without error control
    And I run "rm -f /tmp/action_chain_one_system_done" on "sle_minion" without error control

  Scenario: Cleanup: remove downloaded files
    When I run "rm -f /tmp/downloads/action-chain.cnf" on "localhost" without error control
