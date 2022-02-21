# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
@centos_minion
Feature: Migrate a CentOS 7 traditional client into a Salt minion
  As an authorized user
  I want to migrate this CentOS 7 traditional client to a Salt minion

  Scenario: Prepare the CentOS 7 traditional client in the migration context
    Given I am authorized for the "Admin" section
    When I enable repository "CentOS-Base" on this "ceos_client"
    And I enable client tools repositories on "ceos_client"
    And I refresh the packages list via package manager on "ceos_client"
    And I install the traditional stack utils on "ceos_client"
    And I install OpenSCAP dependencies on "ceos_client"
    And I register "ceos_client" as traditional client
    And I run "rhn-actions-control --enable-all" on "ceos_client"

  Scenario: Wait until the traditional client appears
    When I wait until onboarding is completed for "ceos_client"

  Scenario: Check that the traditional client is really one
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Management" regex, refreshing the page

  @proxy
  Scenario: Check connection from CentOS 7 traditional to proxy in the migration context
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of traditional CentOS 7 in the migration context
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos_client" hostname

  Scenario: Migrate a CentOS client into a Salt minion in the migration context
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "ceos_minion", refreshing the page
    And I wait until onboarding is completed for "ceos_minion"

  Scenario: Check that the migrated system is now a Salt minion in the migration context
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page
