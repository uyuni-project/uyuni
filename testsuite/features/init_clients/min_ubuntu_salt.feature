# Copyright (c) 2016-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Ubuntu minion via salt-ssh
#  2) subscribe it to a base channel for testing

Feature: Bootstrap an Ubuntu minion and do some basic operations on it

@ubuntu_minion
  Scenario: Bootstrap an Ubuntu minion
    Given I am authorized with the feature's user
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ubuntu_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-UBUNTU-TEST" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ubuntu_minion", refreshing the page
    And I wait until onboarding is completed for "ubuntu_minion"
    And I query latest Salt changes on ubuntu system "ubuntu_minion"

@proxy
@ubuntu_minion
  Scenario: Check connection from the Ubuntu minion to proxy
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
@ubuntu_minion
  Scenario: Check registration on proxy of the Ubuntu minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ubuntu_minion" hostname

@ubuntu_minion
  Scenario: Subscribe the Ubuntu minion to a base channel
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-Deb-AMD64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@ubuntu_minion
  Scenario: Detect latest Salt changes on the Ubuntu minion
    When I query latest Salt changes on ubuntu system "ubuntu_minion"

@ubuntu_minion
  Scenario: Check events history for failures on Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    Then I check for failed events on history event page

