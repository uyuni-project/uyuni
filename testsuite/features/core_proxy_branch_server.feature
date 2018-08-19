# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil) or if we are not testing retail ($retail is false)
#
# NOTE: many details in this file will change
#       when we will be able to use a minion as proxy

Feature: Setup SUSE Manager for Retail branch server
  In order to deploy SUSE Manager for Retail solution
  As the system administrator
  I want to prepare the branch server

@retail
@proxy
  Scenario: Bootstrap the branch server as a minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host! " text
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "proxy", refreshing the page

@retail
@proxy
  Scenario: Detect latest Salt changes on the branch server
    When I query latest Salt changes on "proxy"

@retail
@proxy
  Scenario: Install or update Retail formulas on the server
    When I manually install the "branch-network" formula on the server
    And I manually install the "dhcpd" formula on the server
    And I manually install the "bind" formula on the server
    And I wait for "16" seconds

@retail
@proxy
  Scenario: Enable the formulas on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I check the "branch-network" formula
    And I check the "dhcpd" formula
    And I check the "bind" formula
    And I click on "Save"
    Then the "branch-network" formula should be checked
    And the "dhcpd" formula should be checked
    And the "bind" formula should be checked

@retail
@proxy
  Scenario: Parametrize the branch network
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Branch Network" in the content area
    And I enter "eth1" in NIC field
    And I enter "192.168.5.254" in IP field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@retail
@proxy
  Scenario: Parametrize the DHCP server on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I enter "example.org" in domain name field
    And I enter the hostname of "proxy" in domain name server field
    And I enter "eth1" in listen interfaces field
    And I enter "192.168.5.0" in network IP field
    And I enter "255.255.255.0" in network mask field
    And I enter "192.168.5.2" in dynamic IP range begin field
    And I enter "192.168.5.253" in dynamic IP range end field
    And I enter "192.168.5.255" in broadcast address field
    And I enter "192.168.5.254" in routers field
    And I enter "192.168.5.254" in next server field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@retail
@proxy
  Scenario: Parametrize the DNS server on the branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I check include forwarders box
    And I enter "example.org" in configured zone name field
    And I enter "example.org" in available zone name field
    And I enter "master/db.example.org" in file name field
    And I enter "proxy" in name server field
    And I enter "admin@example.org." in contact field
    And I press "Add Item" in A section
    And I enter "client" in first A name field
    And I enter "192.168.5.2" in first A address field
    And I press "Add Item" in A section
    And I enter "minion" in second A name field
    And I enter "192.168.5.3" in second A address field
    And I press "Add Item" in A section
    And I enter "sshminion" in third A name field
    And I enter "192.168.5.4" in third A address field
    And I press "Add Item" in A section
    And I enter "proxy" in fourth A name field
    And I enter "192.168.5.254" in fourth A address field
    And I enter "192.168.5.0/24" in generate reverse network field
    And I press "Add Item" in NS section
    And I enter "proxy.example.org." in NS field
    And I press "Add Item" in for zones section
    And I enter "example.org" in for zones field
    And I click on "Save Formula"
    Then I should see a "Formula saved!" text

@retail
@proxy
  Scenario: Enable SSH and avahi on the branch server
    # work around bsc#1100505
    When I install package "SuSEfirewall2" on this "proxy"
    And I open SSH and avahi ports on the proxy

@retail
@proxy
  Scenario: Apply the formulas via the highstate
    Given I am on the Systems overview page of this "proxy"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then service "dhcpd" is enabled on "proxy"
    And service "dhcpd" is running on "proxy"
    And service "named" is enabled on "proxy"
    And service "named" is running on "proxy"

@retail
@proxy
  Scenario: Set up the terminals too
    When I set up the private network on the terminals
    Then terminal "sle-minion" got a retail network IP address
    And name resolution works on terminal "sle-minion"

@retail
@proxy
  Scenario: Cleanup: unregister the branch server
    Given I am on the Systems overview page of this "proxy"
    # let the proxy finish its initializations before we delete it again
    # TODO: try to detect when it has finished
    When I wait for "240" seconds
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "System profile" text
    And I should see a "has been deleted" text

@retail
@proxy
  Scenario: Cleanup: reinstall deleted packages
    When I enable SUSE Manager proxy repository on "proxy"
    And I install package "spacewalk-proxy-installer spacewalk-proxy-management" on this "proxy"
    And I remove package "salt-minion" from this "proxy"
