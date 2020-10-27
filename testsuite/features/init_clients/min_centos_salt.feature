# Copyright (c) 2016-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new CentOS minion via salt
#  2) subscribe it to a base channel for testing

Feature: Be able to bootstrap a CentOS minion and do some basic operations on it

@centos_minion
  Scenario: Bootstrap a CentOS minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-PKG-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ceos_minion", refreshing the page
    And I wait until onboarding is completed for "ceos_minion"

@proxy
@centos_minion
  Scenario: Check connection from CentOS minion to proxy
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
@centos_minion
  Scenario: Check registration on proxy of CentOS minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos_minion" hostname

@centos_minion
  Scenario: Subscribe the CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@centos_minion
  Scenario: Detect latest Salt changes on the CentOS minion
    When I query latest Salt changes on "ceos_minion"

@centos_minion
  Scenario: Check events history for failures on CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    Then I check for failed events on history event page
