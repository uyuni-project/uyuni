# Copyright (c) 2016-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Red Hat-like minion via salt
#  2) subscribe it to a base channel for testing

@rhlike_minion
Feature: Bootstrap a Red Hat-like minion and do some basic operations on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Red Hat-like minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rhlike_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-RH-LIKE-KEY" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "rhlike_minion", refreshing the page
    And I wait until onboarding is completed for "rhlike_minion"

@proxy
  Scenario: Check connection from Red Hat-like minion to proxy
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Red Hat-like minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rhlike_minion" hostname

  Scenario: Subscribe the Red Hat-like minion to a base channel
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Detect latest Salt changes on the Red Hat-like minion
    When I query latest Salt changes on "rhlike_minion"

  Scenario: Check events history for failures on Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    Then I check for failed events on history event page
