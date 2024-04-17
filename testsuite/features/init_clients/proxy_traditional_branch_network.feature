# Copyright (c) 2018-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil) or if there is no private network ($private_net is nil)

@scope_proxy
@scope_retail
@proxy
@private_net
Feature: Setup SUSE Manager for Retail branch network
  In order to deploy SUSE Manager for Retail solution
  As the system administrator
  I want to prepare the branch network

  Scenario: Remove dependencies for dhcp-server and bind packages from the proxy
    # WORKAROUND
    When I remove package "dhcp dhcp-client bind-utils python3-bind" from this "proxy"
    # End of WORKAROUND

  Scenario: Install or update branch network formulas on the server
    When I manually install the "branch-network" formula on the server
    And I manually install the "dhcpd" formula on the server
    And I manually install the "bind" formula on the server

@susemanager
  Scenario: Install the Retail pattern on the SUSE Manager server
    When I refresh the metadata for "server"
    When I install pattern "suma_retail" on this "server"
    And I wait for "patterns-suma_retail" to be installed on "server"
    And I synchronize all Salt dynamic modules on "proxy"

@uyuni
  Scenario: Install the Retail pattern on the Uyuni server
    When I refresh the metadata for "server"
    When I install pattern "uyuni_retail" on this "server"
    And I wait for "patterns-uyuni_retail" to be installed on "server"
    And I synchronize all Salt dynamic modules on "proxy"

  Scenario: Restart spacewalk services
    When I restart the spacewalk service

  Scenario: Show the overview page of the traditional proxy
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "proxy"

  Scenario: Enable the branch network formulas on the branch server
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas" text
    And I should see a "Suse Manager For Retail" text
    And I should see a "General System Configuration" text
    When I check the "branch-network" formula
    And I check the "dhcpd" formula
    And I check the "bind" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "branch-network" formula should be checked
    And the "dhcpd" formula should be checked
    And the "bind" formula should be checked

  Scenario: Parametrize the branch network
    When I follow "Formulas" in the content area
    And I follow first "Branch Network" in the content area
    And I click on "Expand All Sections"
    And I enter "eth1" in NIC field
    And I enter the local IP address of "proxy" in IP field
    # bsc#1132908 - Branch network formula closes IPv6 default route, potentially making further networking fail
    And I check enable SLAAC with routing box
    And I uncheck enable route box
    And I uncheck enable NAT box
    And I enter "example" in branch id field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize DHCP on the branch server
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I click on "Expand All Sections"
    And I enter "example.org" in domain name field
    And I enter the local IP address of "proxy" in domain name server field
    And I enter "eth1" in listen interfaces field
    And I enter the local IP address of "network" in network IP field
    And I enter "255.255.255.0" in network mask field
    And I enter the local IP address of "range begin" in dynamic IP range begin field
    And I enter the local IP address of "range end" in dynamic IP range end field
    And I enter the local IP address of "broadcast" in broadcast address field
    And I press "Remove" in the routers section
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize DNS on the branch server
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I click on "Expand All Sections"
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
    And I enter "master/db.example.org" in file name field of example.org zone
    And I enter "proxy" in SOA name server field of example.org zone
    And I enter "admin@example.org." in SOA contact field of example.org zone
    And I press "Add Item" in A section of example.org zone
    And I enter "proxy" in first A name field of example.org zone
    And I enter the local IP address of "proxy" in first A address field of example.org zone
    And I press "Add Item" in NS section of example.org zone
    And I enter "proxy.example.org." in first NS field of example.org zone
    # reverse zone xx.168.192.in-addr.arpa:
    And I press "Add Item" in available zones section
    And I enter the local zone name in second available zone name field
    And I enter the local file name in file name field of zone with local name
    And I enter "proxy.example.org." in SOA name server field of zone with local name
    And I enter "admin@example.org." in SOA contact field of zone with local name
    And I press "Add Item" in NS section of zone with local name
    And I enter "proxy.example.org." in first NS field of zone with local name
    And I enter the local network in generate reverse network field of zone with local name
    And I press "Add Item" in for zones section of zone with local name
    And I enter "example.org" in first for zones field of zone with local name
    # end
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Enable repositories for installing branch services
    When I enable repositories before installing branch server

@pxeboot_minion
  Scenario: Parametrize DHCP and DNS for the PXE boot minion
    # dhcpd:
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I click on "Expand All Sections"
    And I press "Add Item" in host reservations section
    And I enter "pxeboot" in first reserved hostname field
    And I enter the local IP address of "pxeboot_minion" in first reserved IP field
    And I enter the MAC address of "pxeboot_minion" in first reserved MAC field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
    # bind:
    When I follow first "Bind" in the content area
    And I click on "Expand All Sections"
    And I press "Add Item" in A section of example.org zone
    And I enter "pxeboot" in second A name field of example.org zone
    And I enter the local IP address of "pxeboot_minion" in second A address field of example.org zone
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Let avahi work on the branch server
    When I open avahi port on the proxy

  Scenario: Apply the branch network formulas via the highstate
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    # This also triggers a "Package List Refresh" event that will fail
    # because the Salt connexion is disoriented after those changes
    Then service "dhcpd" is enabled on "proxy"
    And service "dhcpd" is active on "proxy"
    And service "named" is enabled on "proxy"
    And service "named" is active on "proxy"
    And service "firewalld" is enabled on "proxy"
    And service "firewalld" is active on "proxy"

  Scenario: Disable repositories after installing branch services
    Given the Salt master can reach "proxy"
    When I disable repositories after installing branch server

  Scenario: Let the server know about the new IP and FQDN of the traditional proxy
    When I follow "Details" in the content area
    And I follow "Hardware" in the content area
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled by admin" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "proxy"
    And I follow "Details" in the content area
    And I follow "Hardware" in the content area
    Then I should see a "proxy.example.org" text
