# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test action chaining

  Scenario: wait for taskomatic finished required jobs
    Given Patches are visible for the registered client
    And I run "zypper -n in milkyway-dummy" on "sle-client" without error control

  Scenario: I add a package installation to an action chain
    Given I am on the Systems overview page of this client
    When I follow "Software" in the content area
    And I follow "Install New Packages" in the content area
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a remote command to the action chain
    Given I am on the Systems overview page of this client
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
    Given I am on the Systems overview page of this client
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a remove package to the action chain
    Given I am on the Systems overview page of this client
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I check "milkyway-dummy" in the list
    And I click on "Remove Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a verify package to the action chain
    Given I am on the Systems overview page of this client
    When I follow "Software" in the content area
    And I follow "Verify" in the content area
    And I check "andromeda-dummy-1.0-4.1" in the list
    And I click on "Verify Selected Packages"
    And I check radio button "schedule-by-action-chain"
    And I click on "Confirm"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I add a config file deployment to the action chain
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the tabs
    And I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Deploy Files" in the content area
    And I click on "Deploy All Files" 
    And I check this client
    And I click on "Confirm & Deploy to Selected Systems"
    And I check radio button "schedule-by-action-chain"
    And I click on "Deploy Files to Selected Systems"
    Then I should see a "3 actions are being added to Action Chain new action chain" text

  Scenario: I add a reboot action to the action chain
    Given I am on the Systems overview page of this client
    When I follow "Schedule System Reboot" in the content area
    And I check radio button "schedule-by-action-chain"
    And I click on "Reboot system"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I verify the action chain list
    Given I am on the Systems overview page of this client
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Install or update hoag-dummy on 1 system" text
    And I should see a "2. Run a remote command on 1 system" text
    And I should see a "3. Apply patch(es) andromeda-dummy-6789 on 1 system" text
    And I should see a "4. Remove milkyway-dummy from 1 system" text
    And I should see a "5. Verify andromeda-dummy on 1 system" text
    And I should see a text like "6. Deploy.*/etc/mgr-test-file.cnf.*to 1 system"
    Then I should see a "7. Reboot 1 system" text

  Scenario: check that different user cannot see the action chain
    Given I am authorized as "admin" with password "admin"
    When I follow "Schedule"
    And I follow "Action Chains"
    Then I should not see a "new action chain" link

  Scenario: I delete the action chain
     Given I am authorized as "testing" with password "testing"
     Then I follow "Schedule"
     And I follow "Action Chains"
     And I follow "new action chain"
     And I follow "delete action chain" in the content area
     Then I click on "Delete"

  Scenario: I add a remote command to new action chain
    Given I am on the Systems overview page of this client
    When I follow "Remote Command"
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /root/webui-actionchain-test
      """
    And I check radio button "schedule-by-action-chain"
    And I click on "Schedule"
    Then I should see a "Action has been successfully added to the Action Chain" text

  Scenario: I execute the action chain from the web ui
    Given I am on the Systems overview page of this client
    When I follow "Schedule"
    And I follow "Action Chains"
    And I follow "new action chain"
    And I should see a "1. Run a remote command on 1 system" text
    Then I click on "Save and Schedule"
    And I should see a "Action Chain new action chain has been scheduled for execution." text
    When I run rhn_check on this client
    Then "/root/webui-actionchain-test" exists on the filesystem of "sle-client"

  Scenario: Reset: downgrade milkyway-dummy to lower version
    Given I am authorized as "admin" with password "admin"
    When I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n in --oldpackage milkyway-dummy-1.0-2.1" on "sle-client"
    And I run "zypper -n ref" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"
    And I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
