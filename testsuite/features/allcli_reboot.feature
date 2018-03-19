# Copyright (c) 2017 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Reboot systems managed via SUSE Manager

@sshminion
  Scenario: Reboot the SSH-managed SLES minion
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I wait and check that "ssh-minion" has rebooted

  Scenario: Reboot a SLES Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    Then I wait and check that "sle-minion" has rebooted

  Scenario: Reboot a SLES tradional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    And I run "rhn_check -vvv" on "sle-client"
    Then I wait and check that "sle-client" has rebooted

@centosminion
  Scenario: Reboot the CentOS minion
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I wait and check that "ceos-minion" has rebooted
