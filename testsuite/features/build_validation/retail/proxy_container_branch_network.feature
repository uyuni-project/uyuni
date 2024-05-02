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
    When I connect the second interface of the proxy to the private network
    And I restart all proxy containers to let them pick new network configuration
    Then the "dhcp_dns" host should be present on private network
    And name resolution should work on private network

  Scenario: Show the overview page of the containerized proxy
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "proxy"

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
    And I follow "Create Group"
    And I enter "TERMINALS" as "name"
    And I enter "All terminals" as "description"
    And I click on "Create Group"
    Then I should see a "System group TERMINALS created." text

  Scenario: Create all branch servers group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
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
    And I enter "proxy.example.org" as "Image download server"
    And I check containerized proxy box
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
