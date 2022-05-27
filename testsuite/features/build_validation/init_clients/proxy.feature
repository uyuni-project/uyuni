# Copyright (c) 2020-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)

@proxy
Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server

  Scenario: Clean up sumaform leftovers on a SUSE Manager proxy
    When I perform a full salt minion cleanup on "proxy"

  Scenario: Install proxy software for build validation
    # uncomment when product is out:
    # When I install "SUSE-Manager-Proxy" product on the proxy
    And I install proxy pattern on the proxy
    And I let squid use avahi on the proxy

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap the proxy as a Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-proxy_key" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "proxy"

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel for proxy is working
    When I refresh the metadata for "proxy"

  Scenario: Detect latest Salt changes on the proxy
    When I query latest Salt changes on "proxy"

  Scenario: Copy the keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    And I allow all SSL protocols on the proxy's apache
    Then I should see "proxy" via spacecmd
    And service "salt-broker" is active on "proxy"

  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" short hostname
    And I wait until I see "SUSE Manager Proxy" text, refreshing the page

  Scenario: Check events history for failures on the proxy
    When I check for failed events on history event page
