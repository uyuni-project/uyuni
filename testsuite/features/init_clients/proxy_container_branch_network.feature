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
    When I connect the second interface of the proxy to the private network
    And I restart all proxy containers to let them pick new network configuration
    Then the "dhcp_dns" host should be present on private network
    And name resolution should work on private network
