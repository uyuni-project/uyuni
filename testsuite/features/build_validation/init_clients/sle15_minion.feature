# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15_minion
Feature: Be able to bootstrap a SLES 15 Salt minion

  Scenario: Clean up sumaform leftovers on a SLES 15 Salt minion
    When I perform a full salt minion cleanup on "sle15_minion"

  Scenario: Create the bootstrap repository for a Salt client
    Given I am authorized with the feature's user
    And I create the "x86_64" bootstrap repository for "sle15_minion" on the server

  Scenario: Bootstrap a SLES 15 minion
    Given I am authorized with the feature's user
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle15_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sle15_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle15_minion"

  Scenario: Check the new bootstrapped SLES 15 minion in System Overview page
    Given I am authorized with the feature's user
    And I go to the minion onboarding page
    Then I should see a "accepted" text
    And the Salt master can reach "sle15_minion"

@proxy
  Scenario: Check connection from SLES 15 minion to proxy
    Given I am on the Systems overview page of this "sle15_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES 15 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle15_minion" hostname

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel for SLES 15 minion is working
    When I refresh the metadata for "sle15_minion"

  Scenario: Detect latest Salt changes on the SLES 15 minion
    When I query latest Salt changes on "sle15_minion"

  Scenario: Run a remote command on normal SLES 15 minion
    Given I am authorized as "testing" with password "testing"
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "file /tmp"
    And I click on preview
    Then I should see "sle15_minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "sle15_minion"
    Then I should see "/tmp: sticky, directory" in the command output for "sle15_minion"

  Scenario: Check spacecmd system ID of bootstrapped SLES 15 minion
    Given I am on the Systems overview page of this "sle15_minion"
    Then I run spacecmd listevents for "sle15_minion"

  Scenario: Check events history for failures on SLES 15 minion
    Given I am on the Systems overview page of this "sle15_minion"
    Then I check for failed events on history event page
