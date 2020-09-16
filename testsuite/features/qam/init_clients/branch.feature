# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)


@proxy
Feature: Setup SUSE Branch Server
  In order to use a branch server with the SUSE manager server
  As the system administrator
  I want to enable and configure branch server services at proxy

@proxy
@private_net
  Scenario: Install or update branch network formulas on the server
    When I manually install the "branch-network" formula on the server
    And I manually install the "dhcpd" formula on the server
    And I manually install the "bind" formula on the server
    And I synchronize all Salt dynamic modules on "proxy"

@proxy
@private_net
  Scenario: Install the Retail pattern on the server
    When I install pattern "suma_retail" on this "server"
    And I wait for "patterns-suma_retail" to be installed on "server"

@proxy
@private_net
  Scenario: Enable repositories for installing branch services
    When I install package "expect" on this "proxy"

@proxy
@private_net
  Scenario: Configure retail formulas using retail_branch_init command
    When I set "eth1" as NIC, "id" as prefix, "rbs" as branch server name and "branch.org" as domain

@proxy
@private_net
  Scenario: Let avahi work on the branch server
    When I open avahi port on the proxy

@proxy
@private_net
  Scenario: Apply the branch network formulas via the highstate
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then service "dhcpd" is enabled on "proxy"
    And service "dhcpd" is active on "proxy"
    And service "named" is enabled on "proxy"
    And service "named" is active on "proxy"
    And service "firewalld" is enabled on "proxy"
    And service "firewalld" is active on "proxy"

