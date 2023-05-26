# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
@rhlike_minion
Feature: Migrate a Red Hat-like traditional client into a Salt minion
  As an authorized user
  I want to migrate this Red Hat-like traditional client to a Salt minion

  Scenario: Delete the Red Hat-like minion in the migration context
    Given I am authorized for the "Admin" section
    When I am on the Systems overview page of this "rhlike_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "rhlike_minion"
    Then "rhlike_client" should not be registered

  Scenario: Prepare the Red Hat-like traditional client in the migration context
    When I enable repository "CentOS-Base" on this "rhlike_client"
    And I enable the repositories "tools_update_repo tools_pool_repo" on this "rhlike_client"
    And I refresh the packages list via package manager on "rhlike_client"
    And I install the traditional stack utils on "rhlike_client"
    And I install OpenSCAP dependencies on "rhlike_client"
    And I register "rhlike_client" as traditional client
    And I run "rhn-actions-control --enable-all" on "rhlike_client"

  Scenario: Wait until the traditional client appears
    When I wait until onboarding is completed for "rhlike_client"

  Scenario: Check that the traditional client is really one
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Management" regex, refreshing the page

@proxy
  Scenario: Check connection from Red Hat-like traditional to proxy in the migration context
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of traditional Red Hat-like in the migration context
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rhlike_client" hostname

  Scenario: Migrate a Red Hat-like client into a Salt minion in the migration context
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rhlike_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "rhlike_minion", refreshing the page
    And I wait until onboarding is completed for "rhlike_minion"

  Scenario: Check that the migrated system is now a Salt minion in the migration context
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page
