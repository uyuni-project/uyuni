# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Action chain on traditional clients

  Scenario: Pre-requisite: downgrade repositories to lower version on traditional client
    Given I am authorized as "admin" with password "admin"
    When I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-client" without error control
    And I run "zypper -n in milkyway-dummy" on "sle-client" without error control
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0" on "sle-client"
    And I run "zypper -n ref" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"

  Scenario: Pre-requisite: ensure the errata cache is computed before testing on traditional client
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Pre-requisite: remove all action chains before testing on traditional client
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    When I delete all action chains
    And I cancel all scheduled actions

  Scenario: Add a package installation to an action chain on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Install New Packages" in the content area
    And I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a remote command to the action chain on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Remote Command"
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /root/12345
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a patch installation to the action chain on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a package removal to the action chain on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I check "milkyway-dummy" in the list
    And I click on "Remove Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a package verification to the action chain on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Verify" in the content area
    And I check "andromeda-dummy-1.0" in the list
    And I click on "Verify Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Create a configuration channel for testing action chain on traditional client
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Config Channel"
    And I enter "Action Chain Channel" as "cofName"
    And I enter "actionchainchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Action Chain Channel" text

  Scenario: Add a configuration file to configuration channel for testing action chain on traditional client
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/action-chain.cnf" as "cffPath"
    And I enter "Testchain=YES_PLEASE" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/action-chain.cnf from channel Action Chain Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Subscribe system to configuration channel for testing action chain on traditional client
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    And I follow this "sle-client" link
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Action Chain Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Add a configuration file deployment to the action chain on traditional client
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "Deploy Files" in the content area
    And I click on "Deploy All Files"
    And I check the "sle-client" client
    And I click on "Confirm & Deploy to Selected Systems"
    And I check radio button "schedule-by-action-chain"
    And I click on "Deploy Files to Selected Systems"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a reboot action to the action chain on tradtional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow first "Schedule System Reboot"
    And I check radio button "schedule-by-action-chain"
    And I click on "Reboot system"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Verify the action chain list on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    Then I should see a "1. Install or update virgo-dummy on 1 system" text
    And I should see a "2. Run a remote command on 1 system" text
    And I should see a "3. Apply patch(es) andromeda-dummy-6789 on 1 system" text
    And I should see a "4. Remove milkyway-dummy from 1 system" text
    And I should see a "5. Verify andromeda-dummy on 1 system" text
    And I should see a text like "6. Deploy.*/etc/action-chain.cnf.*to 1 system"
    And I should see a "7. Reboot 1 system" text

  Scenario: Check that a different user cannot see the action chain for traditional client
    Given I am authorized as "testing" with password "testing"
    When I follow "Schedule"
    And I follow "Action Chains"
    Then I should not see a "new action chain" link

  Scenario: Delete the action chain for traditional client
     Given I am authorized as "admin" with password "admin"
     Then I follow "Schedule"
     And I follow "Action Chains"
     And I follow "new action chain"
     And I follow "delete action chain" in the content area
     Then I click on "Delete"

  Scenario: Add a remote command to the new action chain on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Remote Command"
    And I enter as remote command this script in
      """
      #!/bin/bash
      uptime
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Execute the action chain from the web UI on traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Run a remote command on 1 system" text
    Then I click on "Save and Schedule"
    And I should see a "Action Chain new action chain has been scheduled for execution." text
    When I run "rhn_check -vvv" on "sle-client"

  Scenario: Create an action chain via XML-RPC
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    When I call XML-RPC createChain with chainLabel "throwaway_chain"
    And I call actionchain.list_chains() if label "throwaway_chain" is there
    Then I delete the action chain
    And there should be no action chain with the label "throwaway_chain"
    When I call XML-RPC createChain with chainLabel "throwaway_chain"
    Then I call actionchain.rename_chain() to rename it from "throwaway_chain" to "throwaway_chain_renamed"
    And there should be a new action chain with the label "throwaway_chain_renamed"
    And I delete an action chain, labeled "throwaway_chain_renamed"
    And there should be no action chain with the label "throwaway_chain_renamed"
    And no action chain with the label "throwaway_chain"

  Scenario: Add operations to the action chain via XML-RPC for traditional client
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    And I want to operate on this "sle-client"
    When I call XML-RPC createChain with chainLabel "throwaway_chain"
    And I call actionchain.add_package_install()
    And I call actionchain.add_package_removal()
    And I call actionchain.add_package_upgrade()
    And I call actionchain.add_package_verify()
    And I call actionchain.add_script_run() with the script "exit 1;"
    And I call actionchain.add_system_reboot()
    Then I should be able to see all these actions in the action chain
    When I call actionchain.remove_action on each action within the chain
    Then the current action chain should be empty
    And I delete the action chain

  Scenario: Run and cancel an action chain via XML-RPC
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    And I want to operate on this "sle-client"
    When I call XML-RPC createChain with chainLabel "throwaway_chain"
    And I call actionchain.add_system_reboot()
    Then I should be able to see all these actions in the action chain
    When I schedule the action chain
    Then I wait until there are no more action chains
    And I should see scheduled action, called "System reboot scheduled by admin"
    Then I cancel all scheduled actions
    And I wait until there are no more scheduled actions
    And I delete the action chain

  Scenario: Run an action chain via XML-RPC on traditional client
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    And I want to operate on this "sle-client"
    And I run "rhn-actions-control --enable-all" on "sle-client"
    When I call XML-RPC createChain with chainLabel "multiple_scripts"
    And I call actionchain.add_script_run() with the script "echo -n 1 >> /tmp/action_chain.log"
    And I call actionchain.add_script_run() with the script "echo -n 2 >> /tmp/action_chain.log"
    And I call actionchain.add_script_run() with the script "echo -n 3 >> /tmp/action_chain.log"
    Then I should be able to see all these actions in the action chain
    When I schedule the action chain
    Then I wait until there are no more action chains
    When I run "rhn_check -vvv" on "sle-client"
    Then file "/tmp/action_chain.log" should contain "123" on "sle-client"
    And I wait until there are no more scheduled actions

  Scenario: Cleanup: remove traditional client from configuration channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "Systems" in the content area
    And I check the "sle-client" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: Cleanup: remove configuration channel for traditional client
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: remove packages and repository used in action chain for traditional client
    When I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-client" without error control
    And I run "zypper -n rm milkyway-dummy" on "sle-client" without error control
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client" without error control

  Scenario: Cleanup: remove temporary files for testing action chains on traditional client
    When I run "rm -f /tmp/action_chain.log" on "sle-minion" without error control
