# Copyright (c) 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)
#
# Alternative: Bootstrap the proxy as Salt minion from GUI

@proxy
Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server

  Scenario: Bootstrap the proxy as a Salt minion
    Given I am authorized
    And I create the "x86_64" bootstrap repository for "proxy_minion" on the server
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "proxy_key" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "proxy", refreshing the page

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel is working
    When I refresh the metadata for "proxy_minion"

  Scenario: Detect latest Salt changes on the proxy
    When I query latest Salt changes on "proxy"

  Scenario: Copy the keys and configure the proxy
    When I copy server's keys to the proxy
    And I remove package "zypp-plugin-spacewalk" from this "proxy"
    And I configure the proxy
    Then I should see "proxy" in spacewalk

  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" short hostname
    And I wait until I see "SUSE Manager Proxy" text, refreshing the page

  Scenario: Check events history for failures on the proxy
    Given I am on the Systems overview page of this "proxy"
    Then I check for failed events on history event page
