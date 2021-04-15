# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil) or if there is no private network ($private_net is nil)

@sle_minion
@sle_client
@scope_proxy
@scope_retail
Feature: Setup SUSE Manager for Retail branch network
  In order to deploy SUSE Manager for Retail solution
  As the system administrator
  I want to prepare the branch network

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
  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@proxy
@private_net
  Scenario: Enable the branch network formulas on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas" text
    And I should see a "Suse Manager For Retail" text
    And I should see a "General System Configuration" text
    When I check the "branch-network" formula
    And I check the "dhcpd" formula
    And I check the "bind" formula
    And I click on "Save"
    Then the "branch-network" formula should be checked
    And the "dhcpd" formula should be checked
    And the "bind" formula should be checked

@proxy
@private_net
  Scenario: Parametrize the branch network
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Branch Network" in the content area
    And I enter "eth1" in NIC field
    And I enter the local IP address of "proxy" in IP field
    # bsc#1132908 - Branch network formula closes IPv6 default route, potentially making further networking fail
    And I check enable SLAAC with routing box
    And I enter "example" in branch id field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

@proxy
@private_net
  Scenario: Parametrize DHCP on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I enter "example.org" in domain name field
    And I enter the local IP address of "proxy" in domain name server field
    And I enter "eth1" in listen interfaces field
    And I enter the local IP address of "network" in network IP field
    And I enter "255.255.255.0" in network mask field
    And I enter the local IP address of "range begin" in dynamic IP range begin field
    And I enter the local IP address of "range end" in dynamic IP range end field
    And I enter the local IP address of "broadcast" in broadcast address field
    And I enter the local IP address of "proxy" in routers field
    And I press "Add Item" in host reservations section
    And I enter "client" in first reserved hostname field
    And I enter the local IP address of "client" in first reserved IP field
    And I enter the MAC address of "sle_client" in first reserved MAC field
    And I press "Add Item" in host reservations section
    And I enter "minion" in second reserved hostname field
    And I enter the local IP address of "minion" in second reserved IP field
    And I enter the MAC address of "sle_minion" in second reserved MAC field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

@proxy
@private_net
  Scenario: Parametrize DNS on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    # general information:
    And I check include forwarders box
    And I press "Add Item" in config options section
    And I enter "empty-zones-enable" in first option field
    And I enter "no" in first value field
    And I enter "example.org" in first configured zone name field
    And I press "Add Item" in configured zones section
    And I enter the local zone name in second configured zone name field
    # direct zone example.org:
    And I enter "example.org" in first available zone name field
    And I enter "master/db.example.org" in first file name field
    And I enter "proxy" in first name server field
    And I enter "admin@example.org." in first contact field
    And I press "Add Item" in first A section
    And I enter "client" in first A name field
    And I enter the local IP address of "client" in first A address field
    And I press "Add Item" in first A section
    And I enter "minion" in second A name field
    And I enter the local IP address of "minion" in second A address field
    And I press "Add Item" in first A section
    And I enter "proxy" in third A name field
    And I enter the local IP address of "proxy" in third A address field
    And I press "Add Item" in first NS section
    And I enter "proxy.example.org." in first NS field
    # reverse zone xx.168.192.in-addr.arpa:
    And I press "Add Item" in available zones section
    And I enter the local zone name in second available zone name field
    And I enter the local file name in second file name field
    And I enter "proxy.example.org." in second name server field
    And I enter "admin@example.org." in second contact field
    And I press "Add Item" in second NS section
    And I enter "proxy.example.org." in second NS field
    And I enter the local network in second generate reverse network field
    And I press "Add Item" in second for zones section
    And I enter "example.org" in second for zones field
    # end
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

@proxy
@private_net
  Scenario: Enable repositories for installing branch services
    When I enable repositories before installing branch server

@proxy
@private_net
@pxeboot_minion
  Scenario: Parametrize DHCP and DNS for the PXE boot minion
    Given I am on the Systems overview page of this "proxy"
    # dhcpd:
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I press "Add Item" in host reservations section
    And I enter "pxeboot" in third reserved hostname field
    And I enter the local IP address of "pxeboot" in third reserved IP field
    And I enter the MAC address of "pxeboot_minion" in third reserved MAC field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
    # bind:
    When I follow first "Bind" in the content area
    And I press "Add Item" in first A section
    And I enter "pxeboot" in fourth A name field
    And I enter the local IP address of "pxeboot" in fourth A address field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

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
    And I disable repositories after installing branch server
    Then service "dhcpd" is enabled on "proxy"
    And service "dhcpd" is active on "proxy"
    And service "named" is enabled on "proxy"
    And service "named" is active on "proxy"
    And service "firewalld" is enabled on "proxy"
    And service "firewalld" is active on "proxy"

@proxy
@private_net
  Scenario: Set up the terminals too
    When I set up the private network on the terminals
    Then terminal "sle_client" should have got a retail network IP address
    And name resolution should work on terminal "sle_client"
    And terminal "sle_minion" should have got a retail network IP address
    And name resolution should work on terminal "sle_minion"
