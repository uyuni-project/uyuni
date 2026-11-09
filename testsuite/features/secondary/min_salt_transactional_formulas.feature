# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slmicro62_minion
@scope_formulas
Feature: Salt formulas on transactional systems
  In order to configure transactional systems with simple forms
  As an authorized user
  I want formulas to follow the transactional and the reboot flow

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: install the custom formulas on the server
    When I install the custom formula "transactionalformula" on the server
    And I install the custom formula "thenliveformula" on the server
    And I install the custom formula "liveformula" on the server
    And I install the custom formula "invalidformula" on the server
    And I follow the left menu "Salt > Formula Catalog"
    And I enter "transactionalformula" as the filtered formula name
    Then I should see a "transactionalformula" text
    When I enter "thenliveformula" as the filtered formula name
    Then I should see a "thenliveformula" text
    When I enter "liveformula" as the filtered formula name
    Then I should see a "liveformula" text
    When I enter "invalidformula" as the filtered formula name
    Then I should see a "invalidformula" text

  Scenario: Pre-requisite: enable the transactional formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "transactionalformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "transactionalformula" formula should be checked

  Scenario: Show a transactional formula waiting for a reboot
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-transactional-formula-test" should not exist on "slmicro62_minion"

  Scenario: Apply the transactional formula after rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    Then I wait until I see "There is a pending transaction for this system" text, refreshing the page
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I do not see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-transactional-formula-test" should exist on "slmicro62_minion"

  Scenario: Cleanup: disable the transactional formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "transactionalformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "transactionalformula" formula should be unchecked

  Scenario: Pre-requisite: enable the transactional then live formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "thenliveformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "thenliveformula" formula should be checked

  Scenario: Show a transactional then live formula waiting for a reboot
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-then-live-formula-test" should not exist on "slmicro62_minion"
    And file "/tmp/mgr-then-live-formula-test" should not exist on "slmicro62_minion"

  Scenario: Apply the live part of the formula after rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    Then I wait until I see "There is a pending transaction for this system" text, refreshing the page
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I do not see "Waiting for reboot" text, refreshing the page
    When I follow first "Apply highstate scheduled by admin"
    Then I wait until I see "Post-reboot step: completed" text, refreshing the page
    And file "/usr/share/mgr-then-live-formula-test" should exist on "slmicro62_minion"
    And file "/tmp/mgr-then-live-formula-test" should exist on "slmicro62_minion"

  Scenario: Cleanup: disable the transactional then live formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "thenliveformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "thenliveformula" formula should be unchecked

  Scenario: Pre-requisite: enable the live formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "liveformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "liveformula" formula should be checked

  Scenario: Apply a formula that overrides the policy to live only
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I wait until file "/tmp/mgr-live-formula-test" exists on "slmicro62_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should not see a "Waiting for reboot" text

  Scenario: Cleanup: disable the live formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "liveformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "liveformula" formula should be unchecked

  Scenario: Pre-requisite: enable an unsupported formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "vsftpd" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "vsftpd" formula should be checked

  Scenario: Fail an unsupported formula before anything is applied
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I remember the latest "Apply highstate" event of "slmicro62_minion"
    And I follow "States" in the content area
    When I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    Then I wait until the system "slmicro62_minion" has a failed "Apply highstate" event newer than the remembered one
    And "vsftpd" should not be installed on "slmicro62_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    And I follow first "Apply highstate scheduled by admin"
    Then I should see a "Formulas do not support transactional systems" text

  Scenario: Cleanup: disable the unsupported formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "vsftpd" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "vsftpd" formula should be unchecked

  Scenario: Pre-requisite: enable the formula with an invalid policy on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "invalidformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "invalidformula" formula should be checked

  Scenario: Fail a formula with an invalid policy while the execution plan is built
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I remember the latest "Apply highstate" event of "slmicro62_minion"
    And I follow "States" in the content area
    When I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    Then I wait until the system "slmicro62_minion" has a failed "Apply highstate" event newer than the remembered one
    And file "/tmp/mgr-invalid-formula-test" should not exist on "slmicro62_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    And I follow first "Apply highstate scheduled by admin"
    Then I should see a "Invalid transactional metadata for formula" text

  Scenario: Cleanup: disable the formula with an invalid policy on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "invalidformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "invalidformula" formula should be unchecked

  Scenario: Pre-requisite: remove the tftp package of the image from the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nzypper --non-interactive remove tftp" as "script_body" text area
    And I check "use_transactional_update"
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    When I follow "Details" in the content area
    And I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    And "tftp" should not be installed on "slmicro62_minion"

  Scenario: Pre-requisite: enable the tftpd formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "tftpd" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "tftpd" formula should be checked

  Scenario: Show the tftpd formula waiting for a reboot
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    And "tftp" should not be installed on "slmicro62_minion"

  Scenario: Start the tftp service after rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    Then I wait until I see "There is a pending transaction for this system" text, refreshing the page
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I do not see "Waiting for reboot" text, refreshing the page
    When I follow first "Apply highstate scheduled by admin"
    Then I wait until I see "Post-reboot step: completed" text, refreshing the page
    And "tftp" should be installed on "slmicro62_minion"
    And service "tftp.socket" is active on "slmicro62_minion"
    And service "tftp.socket" is enabled on "slmicro62_minion"

  Scenario: Cleanup: disable the tftpd formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "tftpd" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "tftpd" formula should be unchecked

  Scenario: Cleanup: stop the tftp service on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I remember the latest "Remote Command" event of "slmicro62_minion"
    And I follow "Remote Command"
    And I enter "#!/bin/sh\nsystemctl disable --now tftp.socket" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I wait until the system "slmicro62_minion" has a completed "Remote Command" event newer than the remembered one
    Then file "/etc/systemd/system/sockets.target.wants/tftp.socket" should not exist on "slmicro62_minion"

  Scenario: Cleanup: remove the files created by the formulas
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -f /usr/share/mgr-transactional-formula-test /usr/share/mgr-then-live-formula-test" as "script_body" text area
    And I check "use_transactional_update"
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    When I follow "Details" in the content area
    And I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    And file "/usr/share/mgr-transactional-formula-test" should not exist on "slmicro62_minion"
    And file "/usr/share/mgr-then-live-formula-test" should not exist on "slmicro62_minion"
    And I remove "/tmp/mgr-live-formula-test" from "slmicro62_minion"

  Scenario: Cleanup: remove the custom formulas from the server
    When I remove the custom formula "transactionalformula" from the server
    And I remove the custom formula "thenliveformula" from the server
    And I remove the custom formula "liveformula" from the server
    And I remove the custom formula "invalidformula" from the server
    And I follow the left menu "Salt > Formula Catalog"
    And I enter "transactionalformula" as the filtered formula name
    Then I should not see a "transactionalformula" text
