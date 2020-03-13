# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Ubuntu minion via salt-ssh
#  2) subscribe it to a base channel for testing

@ubuntu1804_ssh_minion
Feature: Bootstrap a SSH-managed Ubuntu 18.04 minion and do some basic operations on it

  Scenario: Bootstrap a SSH-managed Ubuntu 18.04 minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I select "1-ubuntu1804_ssh_minion_key" from "activationKeys"
    And I enter the hostname of "ubuntu1804_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "ubuntu1804_ssh_minion"

  Scenario: Check events history for failures on SSH-managed Ubuntu 18.04 minion
    Given I am on the Systems overview page of this "ubuntu1804_ssh_minion"
    Then I check for failed events on history event page
