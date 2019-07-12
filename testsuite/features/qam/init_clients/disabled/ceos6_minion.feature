# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new CentOS 6 minion via salt-ssh
#  2) subscribe it to a base channel for testing

@ceos6_minion
Feature: Bootstrap a CentOS 6 minion and do some basic operations on it

  Scenario: Bootstrap a CentOS 6 minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I enter the hostname of "ceos6_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "ceos6_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host!" text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ceos6_minion", refreshing the page
    And I wait until onboarding is completed for "ceos6_minion"

@proxy
  Scenario: Check connection from CentOS 6 minion to proxy
    Given I am on the Systems overview page of this "ceos6_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
  Scenario: Check registration on proxy of CentOS 6 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos6_minion" hostname

  Scenario: Prepare a CentOS 6 minion
    Given I am authorized
    When I enable SUSE Manager tools repositories on "ceos6_minion"
    When I enable repository "Devel_Galaxy_Manager_4.0_RES-Manager-Tools-6-x86_64" on this "ceos6_minion"
    And  I enable repository "SLE-Manager-Tools-RES-6-x86_64" on this "ceos6_minion"
    And  I enable repository "CentOS-Base" on this "ceos6_minion"
    And  I install package "hwdata m2crypto wget" on this "ceos6_client"
    And  I install package "rhn-client-tools rhn-check rhn-setup rhnsd osad rhncfg-actions" on this "ceos6_minion"
    And  I install package "spacewalk-oscap scap-security-guide" on this "ceos6_minion"

  Scenario: Check events history for failures on CentOS 6 minion
    Given I am on the Systems overview page of this "ceos6_minion"
    Then I check for failed events on history event page
