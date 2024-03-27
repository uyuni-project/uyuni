# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@proxy
@private_net
@containerized_server
Feature: Prepare the containerized branch server for PXE booting
  In order to use SUSE Manager for Retail solution in a containerized setup
  As the system administrator
  I prepare the branch network in containerized setup

  Scenario: Activate the branch network on the proxy
    When I activate the private network on the proxy

  Scenario: The DHCP server works as expected
    Then the "dhcp_dns" host should be present on private network
    And name resolution should work on private network

  Scenario: Enable repositories for installing branch services
    When I enable repositories before installing branch server

  Scenario: Let the proxy manage the DHCP server
    When I install package "expect" on this "proxy"
    And I copy proxy's public key on "dhcp_dns"
    Then the proxy can run a command via SSH on "dhcp_dns"

  Scenario: The DHCP server should not reach the server
    Then the "dhcp_dns" host should not communicate with the server using private interface

  Scenario: Disable repositories after installing branch services
    When I disable repositories after installing branch server

  Scenario: Show the overview page of the containerized proxy
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "proxy"

  Scenario: Let the server know about the IP and FQDN of the containerized proxy
    When I follow "Details" in the content area
    And I follow "Hardware" in the content area
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled by admin" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "proxy"
    And I follow "Details" in the content area
    And I follow "Hardware" in the content area
    Then I should see a "proxy.example.org" text

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

  Scenario: Enable Saltboot Group formula for branch terminals group
    When I follow the left menu "Systems > System Groups"
    And I follow "example" in the content area
    And I follow "Formulas" in the content area
    And I check the "saltboot-group" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "saltboot-group" formula should be checked

  Scenario: Parametrize the Saltboot Group formula
    When I follow the left menu "Systems > System Groups"
    And I follow "example" in the content area
    And I follow "Formulas" in the content area
    And I follow first "Saltboot Group" in the content area
    And I enter the local IP address of "proxy" in image download server field
    And I check containerized proxy box
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
