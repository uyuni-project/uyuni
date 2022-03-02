# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This test case does not test a valid scenario. In SUMA you normally do not
# delete a traditional client and then register it again as Salt minion. You
# would always just bootstrap the traditional client as a Salt minion again
# and Salt would recognize that it is a traditional client and will take the
# the necessary steps to migrate it.

@sle_client
@scope_traditional_client
Feature: Migrate a unregistered traditional client into a Salt minion
  As an authorized user
  I want to bootstrap a Salt minion after unregistering a traditional client

  Scenario: Migrate a SLES client into a Salt minion in a deleted client context
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > Bootstrapping"
    And I enter the hostname of "sle_client" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Wait until the Salt minion appears
    When I wait until onboarding is completed for "sle_client" salt minion

  Scenario: Check that the migrated system is now a minion in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page

  @proxy
  Scenario: Check connection from migrated minion to proxy in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of migrated minion in a deleted client context
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_client" hostname

  Scenario: Unregister migrated client in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_client" should not be registered

  @susemanager
  Scenario: Register minion again as traditional client in a deleted client context
    When I enable client tools repositories on "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I remove package "salt-minion" from this "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  @uyuni
  Scenario: Register minion again as traditional client in a deleted client context
    When I enable client tools repositories on "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I remove package "venv-salt-minion" from this "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Wait until the traditional client appears in a deleted client context
    When I wait until onboarding is completed for "sle_client"

  Scenario: Check that the migrated minion is again a traditional client in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Management" regex, refreshing the page

  Scenario: Unregister traditional client in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_client" should not be registered

  Scenario: Cleanup: remove leftover package of traditional client in a deleted client context
    # workaround for bsc#1195977
    # will not be fixed since traditional clients will be deprecated in future versions
    # this is the minimal clean up that has to be done to successfully register a Salt minion afterwards
    When I remove package "zypp-plugin-spacewalk" from this "sle_client"

  Scenario: Migrate a SLES client into a Salt minion in a deleted client context
    When I enable client tools repositories on "sle_client"
    When I follow the left menu "Systems > Bootstrapping"
    And I enter the hostname of "sle_client" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Wait until the Salt minion appears in a deleted client context
    When I wait until onboarding is completed for "sle_client" salt minion

  Scenario: Check that the migrated system is now a minion in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page

  Scenario: Cleanup: unregister migrated minion in a deleted client context
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_client" should not be registered
