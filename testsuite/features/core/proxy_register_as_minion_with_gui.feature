# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)
#
# Alternative: Bootstrap the proxy as Salt minion from GUI

Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server

@proxy
  Scenario: Bootstrap the proxy as a Salt minion
    Given I am authorized as "admin" with password "admin"
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

@proxy
  Scenario: Wait until the proxy appears
    Given I am authorized as "admin" with password "admin"
    When I wait until onboarding is completed for "proxy"

@proxy
  Scenario: Detect latest Salt changes on the proxy
    When I query latest Salt changes on "proxy"

@proxy
  Scenario: Copy the keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" via spacecmd

@proxy
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    # TODO: uncomment when SCC product becomes available
    # When I wait until I see "$PRODUCT Proxy" text, refreshing the page
    Then I should see a "Proxy" link in the content area

@proxy
  Scenario: Install expect package on proxy for bootstrapping minion with GUI via script
    When I enable repositories before installing branch server
    And I install package "expect" on this "proxy"
    And I disable repositories after installing branch server

@proxy
  Scenario: Check events history for failures on the proxy
    Given I am on the Systems overview page of this "proxy"
    Then I check for failed events on history event page
