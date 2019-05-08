# Copyright (c) 2018-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Action chain on salt minions

  Scenario: Pre-requisite: downgrade repositories to lower version on Salt minion
    Given I am authorized as "admin" with password "admin"
    When I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n rm andromeda-dummy" on "sle-minion" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n in milkyway-dummy" on "sle-minion" without error control
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0" on "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "echo '/dev/vda1 / ext4 defaults 0 0' > /etc/fstab" on "sle-minion"

  Scenario: Pre-requisite: refresh package list and check installed packages after downgrade on SLE minion
    When I refresh packages list via spacecmd on "sle-minion"
    And I wait until refresh package list on "sle-minion" is finished
    Then spacecmd should show packages "milkyway-dummy andromeda-dummy-1.0" installed on "sle-minion"

  Scenario: Pre-requisite: wait until downgrade is finished
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter" until page does contain "andromeda-dummy-1.0" text

  Scenario: Pre-requisite: ensure the errata cache is computed before testing on Salt minion
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Pre-requisite: remove all action chains before testing on Salt minion
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    When I delete all action chains
    And I cancel all scheduled actions

  Scenario: Add a patch installation to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a package removal to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I check "milkyway-dummy" in the list
    And I click on "Remove Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a package installation to an action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Install New Packages" in the content area
    And I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Create a configuration channel for testing action chain on Salt minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Configuration Channels"
    And I follow "Create Config Channel"
    And I enter "Action Chain Channel" as "cofName"
    And I enter "actionchainchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Action Chain Channel" text

  Scenario: Add a configuration file to configuration channel for testing action chain on Salt minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Configuration Channels"
    And I follow "Action Chain Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/action-chain.cnf" as "cffPath"
    And I enter "Testchain=YES_PLEASE" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/action-chain.cnf from channel Action Chain Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Subscribe system to configuration channel for testing action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Action Chain Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Add a configuration file deployment to the action chain on Salt minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Configuration Channels"
    And I follow "Action Chain Channel"
    And I follow "Deploy Files" in the content area
    And I click on "Deploy All Files"
    And I check the "sle-minion" client
    And I click on "Confirm & Deploy to Selected Systems"
    And I check radio button "schedule-by-action-chain"
    And I click on "Deploy Files to Selected Systems"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add apply highstate to action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "States" in the content area
    And I check radio button "schedule-by-action-chain"
    And I click on "Apply Highstate"

  Scenario: Add a reboot action to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow first "Schedule System Reboot"
    And I check radio button "schedule-by-action-chain"
    And I click on "Reboot system"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Add a remote command to the action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Remote Command"
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/action_chain_one_system_done
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Verify the action chain list on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
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
    Then I should not see a "new action chain" link

  Scenario: Execute the action chain from the web UI on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I click on "Save and Schedule"
    Then I should see a "Action Chain new action chain has been scheduled for execution." text

  Scenario: Verify that the action chain was executed successfully
    When I wait for "virgo-dummy" to be installed on this "sle-minion"
    And I wait at most 300 seconds until file "/tmp/action_chain_one_system_done" exists on "sle-minion"

  Scenario: Add a remote command to the new action chain on Salt minion
    Given I am on the Systems overview page of this "sle-minion"
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
    Given I am authorized as "admin" with password "admin"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I follow "delete action chain" in the content area
    And I click on "Delete"

  Scenario: Add an action chain using system set manager for traditional client and Salt minion
    Given I am authorized as "admin" with password "admin"
    When I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm andromeda-dummy" on "sle-minion" without error control
    When I am on the System Overview page
    And I check the "sle-minion" client
    And I check the "sle-client" client
    And I am on System Set Manager Overview
    And I follow "Install" in the content area
    And I follow "Test-Channel-x86_64" in the content area
    And I enter "andromeda-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    Then I should see "sle-minion" hostname
    And I should see "sle-client" hostname
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
    Then I should see "sle-minion" hostname
    And I should see "sle-client" hostname

  Scenario: Verify action chain for two systems
    Given I am on the Systems overview page of this "sle-minion"
    When I run "rhn-actions-control --enable-all" on "sle-client"
    And I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Install or update andromeda-dummy on 2 systems" text
    And I should see a "2. Run a remote command on 2 systems" text
    And I click on "Save and Schedule"
    Then I should see a "Action Chain new action chain has been scheduled for execution." text

  Scenario: Verify that the action chain from the system set manager was executed successfully
    Given I am authorized as "admin" with password "admin"
    When I run "rhn_check -vvv" on "sle-client"
    And I wait until file "/tmp/action_chain_done" exists on "sle-client"
    And I wait until file "/tmp/action_chain_done" exists on "sle-minion"
    Then "andromeda-dummy" should be installed on "sle-client"
    And "andromeda-dummy" should be installed on "sle-minion"

  Scenario: Cleanup: roll back action chain effects
    Given I am on the Systems overview page of this "sle-minion"
    When I run "rm /tmp/action_chain_done" on "sle-minion" without error control
    And I run "zypper -n rm andromeda-dummy" on "sle-minion" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n in milkyway-dummy" on "sle-minion" without error control
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0" on "sle-minion"
    And I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter" until page does contain "andromeda-dummy-1.0" text
    And I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Add operations to the action chain via XML-RPC for Salt minions
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    And I want to operate on this "sle-minion"
    When I call XML-RPC createChain with chainLabel "throwaway_chain"
    And I call actionchain.add_package_install()
    And I call actionchain.add_package_removal()
    And I call actionchain.add_package_upgrade()
    And I call actionchain.add_script_run() with the script "exit 1;"
    And I call actionchain.add_system_reboot()
    Then I should be able to see all these actions in the action chain
    When I call actionchain.remove_action on each action within the chain
    Then the current action chain should be empty
    And I delete the action chain

  Scenario: Run an action chain via XML-RPC on Salt minion
    Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
    And I want to operate on this "sle-minion"
    When I call XML-RPC createChain with chainLabel "multiple_scripts"
    And I call actionchain.add_script_run() with the script "echo -n 1 >> /tmp/action_chain.log"
    And I call actionchain.add_script_run() with the script "echo -n 2 >> /tmp/action_chain.log"
    And I call actionchain.add_script_run() with the script "echo -n 3 >> /tmp/action_chain.log"
    And I call actionchain.add_script_run() with the script "touch /tmp/action_chain_done"
    Then I should be able to see all these actions in the action chain
    When I schedule the action chain
    And I wait until there are no more action chains
    And I wait until file "/tmp/action_chain_done" exists on "sle-minion"
    Then file "/tmp/action_chain.log" should contain "123" on "sle-minion"
    And I wait until there are no more scheduled actions

  Scenario: Cleanup: remove Salt client from configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Configuration Channels"
    And I follow "Action Chain Channel"
    And I follow "Systems" in the content area
    And I check the "sle-minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: Cleanup: remove configuration channel for Salt minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Configuration Channels"
    And I follow "Action Chain Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: remove packages and repository used in action chain for Salt minion
    When I run "zypper -n rm andromeda-dummy" on "sle-minion" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n rm milkyway-dummy" on "sle-minion" without error control
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-minion" without error control

  Scenario: Cleanup: remove temporary files for testing action chains on Salt minion
    When I run "rm -f /tmp/action_chain.log" on "sle-minion" without error control
    And I run "rm -f /tmp/action_chain_done" on "sle-minion" without error control
    And I run "rm -f /etc/action-chain.cnf" on "sle-minion" without error control
    And I run "rm -f /tmp/action_chain_one_system_done" on "sle-minion" without error control
