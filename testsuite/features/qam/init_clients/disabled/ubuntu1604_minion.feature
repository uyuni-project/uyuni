# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Ubuntu minion via salt-ssh
#  2) subscribe it to a base channel for testing

@ubuntu1604_minion
Feature: Bootstrap a Ubuntu 16.04 minion and do some basic operations on it

  Scenario: Bootstrap a Ubuntu 16.04 minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I enter the hostname of "ubuntu1604_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-ubuntu1604_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "ubuntu1604_minion"

@proxy
  Scenario: Check connection from Ubuntu 16.04 minion to proxy
    Given I am on the Systems overview page of this "ubuntu1604_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Ubuntu 16.04 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ubuntu1604_minion" hostname

  Scenario: Check events history for failures on Ubuntu 16.04 minion
    Given I am on the Systems overview page of this "ubuntu1604_minion"
    Then I check for failed events on history event page
