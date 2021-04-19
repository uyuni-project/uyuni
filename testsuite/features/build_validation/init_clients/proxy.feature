# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)

@proxy
Feature: Setup SUSE Manager proxy
  In order to use a proxy and retail branch server with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server and configure it also as branch server

  Scenario: Clean up sumaform leftovers on a SUSE Manager proxy
    When I perform a full salt minion cleanup on "proxy"

  Scenario: Install proxy software
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
    Then I should see "proxy" via spacecmd

  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" short hostname
    And I wait until I see "SUSE Manager Proxy" text, refreshing the page

  Scenario: Check events history for failures on the proxy
    When I check for failed events on history event page

@private_net
  Scenario: Install the Retail pattern on the server
    When I install pattern "suma_retail" on this "server"
    And I wait for "patterns-suma_retail" to be installed on "server"

@private_net
  Scenario: Enable repositories for installing branch services
    When I install package "expect" on this "proxy"

@private_net
  Scenario: Configure retail formulas using retail_branch_init command
    When I set "eth1" as NIC, "id" as prefix, "rbs" as branch server name and "branch.org" as domain

@private_net
  Scenario: Parametrize empty-zones-enable section in DNS formula
    # retail_branch_init command is not able to configure this
    # so we need to do it manually via web UI
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I check include forwarders box
    And I press "Add Item" in config options section
    And I enter "empty-zones-enable" in first option field
    And I enter "no" in first value field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

@private_net
  Scenario: Let avahi work on the branch server
    When I open avahi port on the proxy

@private_net
  Scenario: Apply the branch network formulas via the highstate
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then service "dhcpd" is enabled on "proxy"
    And service "dhcpd" is active on "proxy"
    And service "named" is enabled on "proxy"
    And service "named" is active on "proxy"
    And service "firewalld" is enabled on "proxy"
    And service "firewalld" is active on "proxy"
