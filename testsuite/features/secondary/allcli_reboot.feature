# Copyright (c) 2017-2024 SUSE LLC.
# Licensed under the terms of the MIT license.
#
# Idempotency note:
# * this feature is idempotent
#   (the tests of this feature can be run several times with no change in the results)
# * However, beware that firmware, kernel or library updates might be activated by the reboot
#   (thus making changes in the behaviour of the system after the reboot)
#
# This feature can cause failures in the following features:
# - features/secondary/min_rhlike_openscap_audit.feature
# - features/secondary/min_rhlike_remote_command.feature
# - features/secondary/min_rhlike_ssh.feature
# - features/secondary/min_deblike_openscap_audit.feature
# - features/secondary/min_deblike_remote_command.feature
# - features/secondary/min_deblike_ssh.feature
# If the minions take over the alloted 10 minutes to reboot,
# the following features could fail due to the minions not being reachable.
# Depending on how long they take to reboot, even more features could fail.

@skip_if_github_validation
@scope_onboarding
Feature: Reboot systems managed by Uyuni

  Scenario: Log in as org admin user
    Given I am authorized

@ssh_minion
  Scenario: Reboot the SSH-managed SLES minion
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I wait and check that "ssh_minion" has rebooted

  Scenario: Schedule a reboot on a SLES Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled" is completed
    And I should see a "Reboot completed." text

@rhlike_minion
  Scenario: Reboot the Red Hat-like minion and wait until reboot is completed
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled" is completed
    Then I should see a "Reboot completed." text

@deblike_minion
  Scenario: Reboot the Debian-like minion and wait until reboot is completed
    Given I am on the Systems overview page of this "deblike_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled" is completed
    Then I should see a "Reboot completed." text
