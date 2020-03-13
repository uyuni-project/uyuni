# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp1_minion
Feature: Be able to bootstrap a sle15sp1 Salt minion via the GUI

  Scenario: Create the bootstrap repository for a Salt client
    Given I am authorized
    And I create the "x86_64" bootstrap repository for "sle15sp1_minion" on the server

  Scenario: Bootstrap a sle15sp1 minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle15sp1_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sle15sp1_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle15sp1_minion"

  Scenario: Check the new bootstrapped sle15sp1 minion in System Overview page
    Given I am authorized
    And I go to the minion onboarding page
    Then I should see a "accepted" text
    And the Salt master can reach "sle15sp1_minion"

@proxy
  Scenario: Check connection from sle15sp1 minion to proxy
    Given I am on the Systems overview page of this "sle15sp1_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
  Scenario: Check registration on proxy of sle15sp1 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle15sp1_minion" hostname

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel is working
    When I refresh the metadata for "sle15sp1_minion"

  Scenario: Detect latest Salt changes on the sle15sp1 minion
    When I query latest Salt changes on "sle15sp1_minion"

  Scenario: Check spacecmd system ID of bootstrapped sle15sp1 minion
    Given I am on the Systems overview page of this "sle15sp1_minion"
    Then I run spacecmd listevents for "sle15sp1_minion"

  Scenario: Check events history for failures on sle15sp1 minion
    Given I am on the Systems overview page of this "sle15sp1_minion"
    Then I check for failed events on history event page
