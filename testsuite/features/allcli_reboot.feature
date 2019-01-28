# Copyright (c) 2017-2018 SUSE LLC.
# Licensed under the terms of the MIT license.
#
# Idempotency note:
# * this feature is idempotent
#   (the tests of this feature can be run several times with no change in the results)
# * However, beware that firmware, kernel or library updates might be activated by the reboot
#   (thus making changes in the behaviour of the system after the reboot)

Feature: Reboot systems managed by SUSE Manager

@ssh_minion
  Scenario: Reboot the SSH-managed SLES minion
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I wait and check that "ssh-minion" has rebooted

  Scenario: Schedule a reboot on a SLES Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    And I should see a "Reboot completed." text

  Scenario: Reboot a SLES tradional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    And I run "rhn_check -vvv" on "sle-client"
    Then I wait and check that "sle-client" has rebooted

@centos_minion
  Scenario: Reboot the CentOS minion and wait until reboot is completed
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text

@ubuntu_minion
  Scenario: Reboot the Ubuntu minion and wait until reboot is completed
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    And I follow "scheduled" in the content area
    And I follow first "System reboot scheduled by admin"
    And I wait until I see "This action's status is: Picked Up." text, refreshing the page
    And I wait and check that "ubuntu-minion" has rebooted
    Then I wait until I see "This action's status is: Completed." text, refreshing the page
    And I should see a "Reboot completed." text
