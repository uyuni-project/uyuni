# Copyright (c) 2016-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt host managed via salt-ssh

@ssh_minion
  Scenario: Bootstrap a SLES system managed via salt-ssh
    Given I am authorized with the feature's user
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ssh_minion", refreshing the page
    And I wait until onboarding is completed for "ssh_minion"

# WORKAROUD for bsc#1124634
# Package 'sle-manager-tools-release' is automatically installed during bootstrap and
# stays installed after removal of channel containing it. So it is not possible to update it.
# Package needs to be removed from highstate to avoid failure when updating it.
@ssh_minion
  Scenario: Remove sle-manager-tools-release from state after bootstrap
    Given I am on the Systems overview page of this "ssh_minion"
    When I remove package "sle-manager-tools-release" from highstate

@proxy
@ssh_minion
  Scenario: Check connection from SSH minion to proxy
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
@ssh_minion
  Scenario: Check registration on proxy of SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname

@ssh_minion
  Scenario: Subscribe the SSH-managed SLES minion to a base channel
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@ssh_minion
  Scenario: Check events history for failures on SSH minion
    Given I am on the Systems overview page of this "ssh_minion"
    Then I check for failed events on history event page
