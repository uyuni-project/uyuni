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
Feature: Prepare the containerized branch server for PXE booting
  In order to use a containerized proxy as a Retail Branch server
  As the system administrator
  I make sure the network setup is as expected

  Scenario: Adapt the proxy for Retail
    When I rename the proxy for Retail
    And I connect the second interface of the proxy to the private network
    And I restart all proxy containers

  Scenario: Check the branch network
    Then the "dhcp_dns" host should be present on private network
    And name resolution should work on private network

  Scenario: Let the server know about the new IP and FQDN of the containerized proxy
    When I follow "Details" in the content area
    And I follow "Hardware" in the content area
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    When I wait until event "Hardware List Refresh scheduled by admin" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "proxy"
    And I follow "Details" in the content area
    And I follow "Hardware" in the content area
    Then I should see a "proxy.example.org" text
