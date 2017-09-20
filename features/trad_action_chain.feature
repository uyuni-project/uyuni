# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test action chaining 
  The product feature is only for trad_client

  Scenario: Prequisite: downgrade repo to lower version
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-client" without error control
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0-4.1" on "sle-client"
    And I run "zypper -n ref" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row

  Scenario: I add a package installation to an action chain
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Install New Packages" in the content area
    And I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a remote command to the action chain
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

  Scenario: I add a patch installation to the action chain
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a remove package to the action chain
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I check "adaptec-firmware" in the list
    And I click on "Remove Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a verify package to the action chain
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Verify" in the content area
    And I check "andromeda-dummy-1.0-4.1" in the list
    And I click on "Verify Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: Create a config channel "Action Chain" for action-chain testing
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "Create Config Channel"
    And I enter "Action Chain Channel" as "cofName"
    And I enter "actionchainchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Action Chain Channel" text

  Scenario: Add a config file to Action Chain Channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "Action Chain Channel"
    And I follow "Create configuration file or directory"
    And I enter "/etc/action-chain.cnf" as "cffPath"
    And I enter "Testchain=YES_PLEASE" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/action-chain.cnf from channel Action Chain Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Subscribe system to channel "Action Chain channel"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Action Chain Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: I add a config file deployment to the action chain
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "Deploy Files" in the content area
    And I click on "Deploy All Files"
    And I check this client
    And I click on "Confirm & Deploy to Selected Systems"
    And I check radio button "schedule-by-action-chain"
    And I click on "Deploy Files to Selected Systems"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a reboot action to the action chain
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Schedule System Reboot" in the content area
    And I check radio button "schedule-by-action-chain"
    And I click on "Reboot system"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I verify the action chain list
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Install or update virgo-dummy on 1 system" text
    And I should see a "2. Run a remote command on 1 system" text
    And I should see a "3. Apply patch(es) andromeda-dummy-6789 on 1 system" text
    And I should see a "4. Remove adaptec-firmware from 1 system" text
    And I should see a "5. Verify andromeda-dummy on 1 system" text
    And I should see a text like "6. Deploy.*/etc/action-chain.cnf.*to 1 system"
    Then I should see a "7. Reboot 1 system" text

  Scenario: check that different user cannot see the action chain
    Given I am authorized as "testing" with password "testing"
    When I follow "Schedule"
    And I follow "Action Chains"
    Then I should not see a "new action chain" link

  Scenario: I delete the action chain
     Given I am authorized as "admin" with password "admin"
     Then I follow "Schedule"
     And I follow "Action Chains"
     And I follow "new action chain"
     And I follow "delete action chain" in the content area
     Then I click on "Delete"

  Scenario: I add a remote command to new action chain
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

  Scenario: I execute the action chain from the web ui
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Run a remote command on 1 system" text
    Then I click on "Save and Schedule"
    And I should see a "Action Chain new action chain has been scheduled for execution." text
    When I run "rhn_check -vvv" on "sle-client"

  Scenario: Basic chain operations xmlrpc
    Given I am logged in via XML-RPC/actionchain as user "admin" and password "admin"
    When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
    And I call actionchain.list_chains() if label "Quick Brown Fox" is there
    Then I delete the action chain
    And there should be no action chain with the label "Quick Brown Fox"
    When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
    Then I call actionchain.rename_chain() to rename it from "Quick Brown Fox" to "Slow Gray Elephant"
    And there should be a new action chain with the label "Slow Gray Elephant"
    And I delete an action chain, labeled "Slow Gray Elephant"
    And there should be no action chain with the label "Slow Gray Elephant"
    And no action chain with the label "Quick Brown Fox".

  Scenario: Schedule operations via XML-RPC
    Given I am logged in via XML-RPC/actionchain as user "admin" and password "admin"
    When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
    And I call actionchain.add_package_install()
    And I call actionchain.add_package_removal()
    And I call actionchain.add_package_upgrade()
    And I call actionchain.add_package_verify()
    And I call actionchain.add_script_run() with the script like "#!/bin/bash\nexit 1;"
    And I call actionchain.add_system_reboot()
    Then I should be able to see all these actions in the action chain
    When I call actionchain.remove_action on each action within the chain
    Then I should be able to see that the current action chain is empty
    And I delete the action chain

  Scenario: Run the action chain via XML-RPC
    Given I am logged in via XML-RPC/actionchain as user "admin" and password "admin"
    When I call XML-RPC/createChain with chainLabel "Quick Brown Fox"
    And I call actionchain.add_system_reboot()
    Then I should be able to see all these actions in the action chain
    When I schedule the action chain
    Then there should be no more my action chain
    And I should see scheduled action, called "System reboot scheduled by admin"
    Then I cancel all scheduled actions
    And there should be no more any scheduled actions

  Scenario: CLEAN_UP: remove system from conf channel "Action chain channel"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "Systems" in the content area
    And I check this client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: CLEAN_UP: remove configuration channel: Action chain channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Action Chain Channel"
    And I follow "delete channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: remove pkgs and repo used in action chain
    When I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-client" without error control
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client" without error control
