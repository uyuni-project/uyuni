# Copyright (c) 2017-2026 SUSE LLC
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
@sshminion
@skip_if_github_validation
Feature: Register a salt-ssh system via API

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Delete SSH minion system profile before API bootstrap test
    When I delete "sshminion" system using the api
    And I perform a full salt minion cleanup on "sshminion"
    And I wait until Salt client is inactive on "sshminion"
    Then "sshminion" should not be registered

@proxy
  Scenario: Block direct access from server to sshminion to test proxy as jumphost
    Given I block connections from "server" on "sshminion"

  Scenario: Bootstrap a SLES SSH minion via API
    When I call system.bootstrap() on host "sshminion" and salt-ssh "enabled"

  Scenario: Check new API bootstrapped salt-ssh system in System Overview page
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sshminion", refreshing the page
    And I wait until onboarding is completed for "sshminion"

  Scenario: Check contact method of this Salt SSH system
    Given I am on the Systems overview page of this "sshminion"
    Then I should see a "Push via SSH" text

@proxy
  Scenario: Check registration on proxy of SSH minion bootstrapped via API
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sshminion" hostname

  Scenario: Check spacecmd system ID of SSH minion bootstrapped via API
    Given I am on the Systems overview page of this "sshminion"
    Then I run spacecmd listeventhistory for "sshminion"

  Scenario: Check events history for failures on SSH minion after API bootstrap
    Given I am on the Systems overview page of this "sshminion"
    Then I check for failed events on history event page

@susemanager
  Scenario: API bootstrap: subscribe SSH minion to base channel
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP7-Pool for x86_64"
    And I wait until I do not see "Loading..." text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP7-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@uyuni
  Scenario: API bootstrap: subscribe SSH minion to base channel
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "openSUSE Tumbleweed (x86_64)"
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@proxy
  Scenario: Cleanup and flush the firewall rules
    When I flush firewall on "sshminion"
