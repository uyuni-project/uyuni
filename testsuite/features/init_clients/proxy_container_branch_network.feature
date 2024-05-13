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
