# Copyright (c) 2021-2022 SUSE LLC
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

  Scenario: Enable repositories for installing branch services
    When I install package "expect" on this "proxy"

  Scenario: Configure retail formulas using retail_branch_init command
    When I set "eth1" as NIC, "id" as prefix, "rbs" as branch server name and "branch.org" as domain

  Scenario: Parametrize empty-zones-enable section in DNS formula
    # retail_branch_init command is not able to configure this
    # so we need to do it manually via web UI
    Given I am on the Systems overview page of this "proxy"
    When I follow "Formulas" in the content area
    And I follow first "Bind" in the content area
    And I click on "Expand All Sections"
    And I check include forwarders box
    And I press "Add Item" in config options section
    And I enter "empty-zones-enable" in first option field
    And I enter "no" in first value field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Parametrize the branch network
    When I follow first "Branch Network" in the content area
    And I click on "Expand All Sections"
    And I uncheck enable route box
    And I uncheck enable NAT box
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Let avahi work on the branch server
    When I open avahi port on the proxy

  Scenario: Apply the branch network formulas via the highstate
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then service "dhcpd" is enabled on "proxy"
    And service "dhcpd" is active on "proxy"
    And service "named" is enabled on "proxy"
    And service "named" is active on "proxy"
    And service "firewalld" is enabled on "proxy"
    And service "firewalld" is active on "proxy"
