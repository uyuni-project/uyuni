# Copyright (c) 2017-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/minssh_action_chain.feature
# If the current feature fails on bootstrapping,
# this feature won't be able to perform actions on it.
# - features/secondary/minssh_move_from_and_to_proxy.feature
# If the current feature fails on bootstrapping,
# this feature won't be able to delete the minion in its initial setup.

@scope_salt_ssh
@scope_onboarding
@ssh_minion
@skip_if_github_validation
Feature: Register a salt-ssh system via API

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Delete SSH minion system profile before API bootstrap test
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ssh_minion" should not be registered

@proxy
  Scenario: block direct access from server to sshminion to test proxy as jumphost
    Given I block connections from "server" on "ssh_minion"

  Scenario: Bootstrap a SLES SSH minion via API
    When I call system.bootstrap() on host "ssh_minion" and salt-ssh "enabled"

  Scenario: Check new API bootstrapped salt-ssh system in System Overview page
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "ssh_minion", refreshing the page
    And I wait until onboarding is completed for "ssh_minion"

  Scenario: Check contact method of this Salt SSH system
    Given I am on the Systems overview page of this "ssh_minion"
    Then I should see a "Push via SSH" text

@proxy
  Scenario: Check registration on proxy of SSH minion bootstrapped via API
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname

  Scenario: Check spacecmd system ID of SSH minion bootstrapped via API
    Given I am on the Systems overview page of this "ssh_minion"
    Then I run spacecmd listeventhistory for "ssh_minion"

  Scenario: Check events history for failures on SSH minion after API bootstrap
    Given I am on the Systems overview page of this "ssh_minion"
    Then I check for failed events on history event page

@susemanager
  Scenario: API bootstrap: subscribe SSH minion to base channel
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@uyuni
  Scenario: API bootstrap: subscribe SSH minion to base channel
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "openSUSE Leap 15.5 (x86_64)"
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@proxy
  Scenario: cleanup and flush the firewall rules
    When I flush firewall on "ssh_minion"
