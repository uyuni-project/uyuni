# Copyright (c) 2017-2020 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Register a salt-ssh system via XML-RPC

@ssh_minion
  Scenario: Setup XML-RPC bootstrap: delete SSH minion system profile
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I clean up the minion's cache on "ssh_minion"
    Then "ssh_minion" should not be registered

@ssh_minion
  Scenario: Bootstrap a SLES SSH minion via XML-RPC
    Given I am logged in via XML-RPC system with the feature's user
    When I call system.bootstrap() on host "ssh_minion" and salt-ssh "enabled"
    And I logout from XML-RPC system namespace

@ssh_minion
  Scenario: Check new XML-RPC bootstrapped salt-ssh system in System Overview page
     Given I am authorized with the feature's user
     And I am on the System Overview page
     And I wait until I see the name of "ssh_minion", refreshing the page
     And I wait until onboarding is completed for "ssh_minion"

# WORKAROUD for bsc#1124634
# Package 'sle-manager-tools-release' is automatically installed during bootstrap and
# stays installed after removal of channel containing it. So it is not possible to update it.
# Package needs to be removed from highstate to avoid failure when updating it.
@ssh_minion
  Scenario: Remove sle-manager-tools-release from state after bootstrap via XML-RPC
    Given I am on the Systems overview page of this "ssh_minion"
    When I remove package "sle-manager-tools-release" from highstate

@ssh_minion
  Scenario: Check contact method of this Salt SSH system
    Given I am on the Systems overview page of this "ssh_minion"
    Then I should see a "Push via SSH" text

@proxy
@ssh_minion
  Scenario: Check registration on proxy of SSH minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname

@ssh_minion
  Scenario: Check spacecmd system ID of SSH minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "ssh_minion"
    Then I run spacecmd listevents for "ssh_minion"

@ssh_minion
  Scenario: Check events history for failures on SSH minion after XML-RPC bootstrap
    Given I am on the Systems overview page of this "ssh_minion"
    Then I check for failed events on history event page

@ssh_minion
  Scenario: Cleanup: subscribe SSH minion to base channel
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed
