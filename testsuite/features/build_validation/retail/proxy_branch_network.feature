# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@proxy
@private_net
Feature: Prepare the branch server for PXE booting
  In order to use SUSE Manager for Retail solution
  As the system administrator
  I prepare the branch network

  Scenario: Install the Retail pattern on the server
    When I install pattern "suma_retail" on this "server"
    And I wait for "patterns-suma_retail" to be installed on "server"
    And I synchronize all Salt dynamic modules on "proxy"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enable the Retail formulas on the Retail branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I check the "branch-network" formula
    And I check the "dhcpd" formula
    And I check the "bind" formula
    And I check the "tftpd" formula
    And I check the "vsftpd" formula
    And I check the "pxe" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "branch-network" formula should be checked
    And the "dhcpd" formula should be checked
    And the "bind" formula should be checked
    And the "tftpd" formula should be checked
    And the "vsftpd" formula should be checked
    And the "pxe" formula should be checked

  Scenario: Parametrize the branch network on the Retail branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Branch Network" in the content area
    And I click on "Expand All Sections"
    And I enter "eth1" in NIC field
    And I enter the local IP address of "proxy" in IP field
    And I uncheck enable route box
    And I uncheck enable NAT box
    And I enter "example" in branch id field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Let avahi work on the branch server
    When I open avahi port on the proxy

  Scenario: Create branch terminals group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "example" as "name"
    And I enter "Terminal branch: example.org" as "description"
    And I click on "Create Group"
    Then I should see a "System group example created." text
    When I follow "Target Systems"
    And I check the "proxy" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to example server group." text

  Scenario: Create all terminals group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "TERMINALS" as "name"
    And I enter "All terminals" as "description"
    And I click on "Create Group"
    Then I should see a "System group TERMINALS created." text

  Scenario: Create all branch servers group
    When I follow the left menu "Systems > System Groups"
    When I follow "Create Group"
    And I enter "SERVERS" as "name"
    And I enter "All branch servers" as "description"
    And I click on "Create Group"
    Then I should see a "System group SERVERS created." text
    When I follow "Target Systems"
    And I check the "proxy" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to SERVERS server group." text

  Scenario: Parametrize DHCP on the Retail branch server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Dhcpd" in the content area
    And I click on "Expand All Sections"
    And I enter "example.org" in domain name field
    And I enter the local IP address of "proxy" in next server field
    And I enter "boot/pxelinux.0" in filename field
    And I enter the local IP address of "proxy" in domain name server field
    And I enter "eth1" in listen interfaces field
    And I enter the local IP address of "network" in network IP field
    And I enter "255.255.255.0" in network mask field
    And I enter the local IP address of "range begin" in dynamic IP range begin field
    And I enter the local IP address of "range end" in dynamic IP range end field
    And I enter the local IP address of "broadcast" in broadcast address field
    And I press "Remove" in the routers section
    And I press "Add Item" in host reservations section
    And I enter "sle12sp5terminal" in first reserved hostname field
    And I enter the local IP address of "sle12sp5_terminal" in first reserved IP field
    And I enter the MAC address of "sle12sp5_terminal" in first reserved MAC field
    And I press "Add Item" in host reservations section
    And I enter "sle15sp4terminal" in second reserved hostname field
    And I enter the local IP address of "sle15sp4_terminal" in second reserved IP field
    And I enter the MAC address of "sle15sp4_terminal" in second reserved MAC field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize DNS on the Retail branch server
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
    And I press "Add Item" in A section of example.org zone
    And I enter "sle12sp5terminal" in second A name field of example.org zone
    And I enter the local IP address of "sle12sp5_terminal" in second A address field of example.org zone
    And I press "Add Item" in A section of example.org zone
    And I enter "sle15sp4terminal" in third A name field of example.org zone
    And I enter the local IP address of "sle15sp4_terminal" in third A address field of example.org zone
    And I press "Add Item" in NS section of example.org zone
    And I enter "proxy.example.org." in first NS field of example.org zone
    And I press "Add Item" in CNAME section of example.org zone
    And I enter "ftp" in first CNAME alias field of example.org zone
    And I enter "proxy" in first CNAME name field of example.org zone
    And I press "Add Item" in CNAME section of example.org zone
    And I enter "tftp" in second CNAME alias field of example.org zone
    And I enter "proxy" in second CNAME name field of example.org zone
    And I press "Add Item" in CNAME section of example.org zone
    And I enter "salt" in third CNAME alias field of example.org zone
    And I enter "proxy" in third CNAME name field of example.org zone
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

  Scenario: Parametrize TFTP on the branch server
    When I follow "Formulas" in the content area
    And I follow first "Tftpd" in the content area
    And I click on "Expand All Sections"
    And I enter the local IP address of "proxy" in internal network address field
    And I enter "/srv/saltboot" in TFTP base directory field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize vsFTPd on the branch server
    When I follow "Formulas" in the content area
    And I follow first "Vsftpd" in the content area
    And I click on "Expand All Sections"
    And I enter the local IP address of "proxy" in vsftpd internal network address field
    And I enter "/srv/saltboot" in FTP server directory field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Configure PXE itself on the branch server
    When I follow "Formulas" in the content area
    And I follow first "Pxe" in the content area
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply the branch network formulas via the highstate
    When I follow "States" in the content area
    And I enable repositories before installing branch server
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    # Network takes time to stabilize
    And I wait for "5" seconds

  Scenario: Let the server know about the new IP and FQDN of the proxy
    When I follow "Details" in the content area
    And I follow "Hardware" in the content area
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled by admin" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "proxy"
    And I follow "Details" in the content area
    And I follow "Hardware" in the content area
    Then I should see a "proxy.example.org" text

  Scenario: Also let squid know about the new IP and FQDN of the proxy
    When I restart squid service on the proxy
