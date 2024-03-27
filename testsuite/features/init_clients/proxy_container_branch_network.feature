# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
# * there is no scope @scope_containerized_proxy
#
# Make sure the proxy can be used as a Retail branch server


@containerized_server
@scope_containerized_proxy
@proxy
@private_net
Feature: Setup containerized proxy
  In order to use a containerized proxy as a Retail Branch server
  As the system administrator
  I make sure the network setup is as expected

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
