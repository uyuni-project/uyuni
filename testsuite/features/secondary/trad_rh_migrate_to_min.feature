# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
@rh_minion
Feature: Migrate a RedHat-like traditional client into a Salt minion
  As an authorized user
  I want to migrate this RedHat-like traditional client to a Salt minion

  Scenario: Delete the RedHat-like minion in the migration context
    Given I am authorized for the "Admin" section
    When I am on the Systems overview page of this "rh_client"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "rh_client" should not be registered

  Scenario: Prepare the RedHat-like traditional client in the migration context
    When I enable repository "CentOS-Base" on this "rh_client"
    And I enable client tools repositories on "rh_client"
    And I refresh the packages list via package manager on "rh_client"
    And I install the traditional stack utils on "rh_client"
    And I install OpenSCAP dependencies on "rh_client"
    And I register "rh_client" as traditional client
    And I run "rhn-actions-control --enable-all" on "rh_client"

  Scenario: Wait until the traditional client appears
    When I wait until onboarding is completed for "rh_client"

  Scenario: Check that the traditional client is really one
    Given I am on the Systems overview page of this "rh_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Management" regex, refreshing the page

  @proxy
  Scenario: Check connection from RedHat-like traditional to proxy in the migration context
    Given I am on the Systems overview page of this "rh_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of traditional RedHat-like in the migration context
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rh_client" hostname

  Scenario: Migrate a RedHat-like client into a Salt minion in the migration context
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rh_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "rh_minion", refreshing the page
    And I wait until onboarding is completed for "rh_minion"

  Scenario: Check that the migrated system is now a Salt minion in the migration context
    Given I am on the Systems overview page of this "rh_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page
